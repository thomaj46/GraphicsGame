
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.jogamp.graph.curve.opengl.TextRenderer;
import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.texture.*;
import javax.swing.*;

import com.jogamp.opengl.util.gl2.GLUT;

public class the_game extends JFrame implements GLEventListener, KeyListener {
	static GLU glu;
	static GLUT glut;

	static GLCapabilities caps;
	static FPSAnimator animator;

	static float WALLHEIGHT = 130.0f; // Some playing field parameters
	static float ARENASIZE = 1000.0f;
	static float EYEHEIGHT = 25.0f;
	static float HERO_VP = 0.625f;

	static double upx = 0.0, upy = 1.0, upz = 0.0; // gluLookAt params

	static double fov = 60.0; // gluPerspective params
	static double near = 1.0;
	static double far = 10000.0;
	double aspect, eyex, eyez;

	static int width = 1000; // canvas size
	static int height = 625;
	static int vp1_left = 0; // Left viewport -- the hero's view
	static int vp1_bottom = 0;

	static double villainSpeed = .5;
	int gameStatus = 0; // 0 - Retry, 1, Start over
	static boolean heroAlive = true;
	Random random;

	float ga[] = { 0.2f, 0.2f, 0.2f, 1.0f }; // global ambient light intensity
	float la0[] = { 0.0f, 0.0f, 0.0f, 1.0f }; // light 0 ambient intensity
	float ld0[] = { 1.0f, 1.0f, 1.0f, 1.0f }; // light 0 diffuse intensity
	float lp0[] = { 0.0f, 1.0f, 1.0f, 0.0f }; // light 0 position
	float ls0[] = { 1.0f, 1.0f, 1.0f, 1.0f }; // light 0 specular
	// Original
//	float ma[] = { 0.02f, 0.2f, 0.02f, 1.0f }; // material ambient
//	float md[] = { 0.08f, 0.6f, 0.08f, 1.0f }; // material diffuse
//	float ms[] = { 0.6f, 0.7f, 0.6f, 1.0f }; // material specular
//	int me = 75; // shininess exponent
	float ma[] = { 0.02f, 0.0f, 0.02f, 1.0f }; // material ambient
	float md[] = { 0.9f, 0.0f, 0.01f, 1.0f }; // material diffuse
	float ms[] = { 0.2f, 0.2f, 0.3f, 1.0f }; // material specular
	int me = 8; // shininess exponent
	float red[] = { 1.0f, 0.0f, 0.0f, 1.0f }; // pure red
	float blue[] = { 0.0f, 0.0f, 1.0f, 1.0f }; // pure blue
	float grey[] = { 0.5f, 0.5f, 0.5f, 1.0f }; // pure grey
	float yellow[] = { 1.0f, 1.0f, 0.0f, 1.0f }; // pure yellow
	float tan[] = { 0.8549f, 0.7019f, 0.3372f, 1.0f }; // pure tan for the floor
	int displayListBase;
	int score;
	int lastScore;
	int highScore;
	Villain[] villain_array;
	int countVillains = 1;

	Hero the_hero; // Three objects on the playing field to
	ThingWeAreSeeking the_thing; // start with, each with its own display list.

	public the_game() {
		super("the_game");
	}

	public static void main(String[] args) {

		caps = new GLCapabilities(GLProfile.getGL2GL3());
		caps.setDoubleBuffered(true); // request double buffer display mode
		caps.setHardwareAccelerated(true);
		GLJPanel canvas = new GLJPanel();

		the_game myself = new the_game();
		canvas.addGLEventListener(myself);

		canvas.addKeyListener(myself);
		animator = new FPSAnimator(canvas, 60);

		JFrame frame = new JFrame("Grab Life by the Fireballs!");
		frame.setSize(width, height); // Size in pixels of the frame we draw on
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(canvas);
		frame.setVisible(true);
		canvas.requestFocusInWindow();
		myself.run();
	}

	public void run() {
		animator.start();
	}

	public void init(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();
		glu = new GLU();
		glut = new GLUT();

		gl.glEnable(GL2.GL_LIGHTING);
		gl.glEnable(GL2.GL_LIGHT0);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glEnable(GL2.GL_CULL_FACE); // Why?

		eyex = ARENASIZE / 2.0; // Where the hero starts
		eyez = -ARENASIZE / 2.0;

		displayListBase = gl.glGenLists(4); // Only three currently used for the
		// 3 objects

		// Load textures.
		Texture villainTexture = null;
		Texture thingTexture = null;
		try {
			TextureData data = TextureIO.newTextureData(GLProfile.getDefault(), getClass().getResourceAsStream("minecraft-creeper.jpg"), false, "jpg");
			villainTexture = TextureIO.newTexture(data);
			data = TextureIO.newTextureData(GLProfile.getDefault(), getClass().getResourceAsStream("firemap.jpg"), false, "jpg");
			thingTexture = TextureIO.newTexture(data);
		}
		catch (IOException exc) {
			exc.printStackTrace();
			System.exit(1);
		}

		the_hero = new Hero(eyex, 0.0, eyez, 135, 10.0, displayListBase, this, drawable);
		the_thing = new ThingWeAreSeeking(ARENASIZE / 4.0, 0.0, -ARENASIZE / 4.0, 0, 30.0, displayListBase + 1, this, drawable, thingTexture);

		//Create and fill Villain Array Max 30 Villains
		villain_array = new Villain[30];
		for (int i = 0; i < villain_array.length;i++) {
			villain_array[i] = new Villain((3 * ARENASIZE) / 4.0, 0.0, -ARENASIZE / 4.0, 0, 10.0, displayListBase + 2, this, drawable, villainTexture);
		}
		//the_villain = new Villain((3 * ARENASIZE) / 4.0, 0.0, -ARENASIZE / 4.0, 0, 10.0, displayListBase + 2, this, drawable);


		aspect = (double) width / (double) height;

		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, la0, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, ld0, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, ls0, 0);
		gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lp0, 0);
		gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, ga, 0);

		gl.glShadeModel(GL2.GL_SMOOTH);
	}

	public void display(GLAutoDrawable drawable) {

		GL2 gl = drawable.getGL().getGL2();

		//int horiz_offset, vert_offset;
		int horiz_offset = (int) (width * (1.0 - HERO_VP) / 6.0);
		int vert_offset = height / 6;

		// light grey background
		// gl.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
		// Other Background
		gl.glClearColor(0.0f, 0.0f, 1.0f, 0.2f);
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);

		// Score Box
		gl.glViewport(625, 575, 400, 100);
		gl.glDisable(GL2.GL_LIGHTING);
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glRasterPos2f(10.0f, 0.0f); // <-- position of text
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "High Score");
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glRasterPos2f(310.0f, 0.0f); // <-- position of text
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Last Score");
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glRasterPos2f(600.0f, 0.0f); // <-- position of text
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Current Score");
		gl.glViewport(625, 550, 400, 100);
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glRasterPos2f(110.0f, 0.0f); // <-- position of text
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, String.valueOf(highScore));
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glRasterPos2f(410.0f, 0.0f); // <-- position of text
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, String.valueOf(lastScore));
		gl.glColor3f(1.0f, 1.0f, 0.0f);
		gl.glRasterPos2f(700.0f, 0.0f); // <-- position of text
		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, String.valueOf(score));
		gl.glEnable(GL2.GL_LIGHTING);

		// Hero's eye viewport
		gl.glViewport(vp1_left, vp1_bottom, (int) (HERO_VP * width), height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(fov, HERO_VP * aspect, near, far);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		glu.gluLookAt(the_hero.x, EYEHEIGHT, the_hero.z, the_hero.x + the_hero.xdir, EYEHEIGHT, the_hero.z + the_hero.zdir, upx, upy, upz);
		showArena(drawable);
		showObjects(drawable);

		// Overhead viewport

		gl.glViewport(vp1_left + (int) (HERO_VP * width) + horiz_offset, vp1_bottom + vert_offset, 4 * horiz_offset, 4 * vert_offset);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(-500, 500, -500, 500, 0, 200);

		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
		glu.gluLookAt(500., 100., -500., 500., 0., -500., 0., 0., -1.);

		showArena(drawable);
		showObjects(drawable);
		chaseHero();
		checkCollisions(drawable);
		gl.glFlush();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int w, int h)
	{
		GL2 gl = drawable.getGL().getGL2();

		width = w;
		height = h;
		aspect = (double) width / (double) height;
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
		// Nothing for us to do here
	}

	void showArena(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, ma, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, md, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, ms, 0);
		gl.glMateriali(GL2.GL_FRONT, GL2.GL_SHININESS, me);

		gl.glPushMatrix();
		gl.glBegin(GL2.GL_POLYGON);
		gl.glNormal3f(1.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, -ARENASIZE);
		gl.glVertex3f(0.0f, WALLHEIGHT, -ARENASIZE);
		gl.glVertex3f(0.0f, WALLHEIGHT, 0.0f);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glNormal3f(-1.0f, 0.0f, 0.0f);
		gl.glVertex3f(ARENASIZE, 0.0f, 0.0f);
		gl.glVertex3f(ARENASIZE, WALLHEIGHT, 0.0f);
		gl.glVertex3f(ARENASIZE, WALLHEIGHT, -ARENASIZE);
		gl.glVertex3f(ARENASIZE, 0.0f, -ARENASIZE);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glNormal3f(0.0f, 0.0f, 1.0f);
		gl.glVertex3f(0.0f, 0.0f, -ARENASIZE);
		gl.glVertex3f(ARENASIZE, 0.0f, -ARENASIZE);
		gl.glVertex3f(ARENASIZE, WALLHEIGHT, -ARENASIZE);
		gl.glVertex3f(0.0f, WALLHEIGHT, -ARENASIZE);
		gl.glEnd();

		gl.glBegin(GL2.GL_POLYGON);
		gl.glNormal3f(0.0f, 0.0f, -1.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(0.0f, WALLHEIGHT, 0.0f);
		gl.glVertex3f(ARENASIZE, WALLHEIGHT, 0.0f);
		gl.glVertex3f(ARENASIZE, 0.0f, 0.0f);
		gl.glEnd();

		gl.glPopMatrix();

		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, tan, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, tan, 0);
		gl.glBegin(GL2.GL_POLYGON);
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(ARENASIZE, 0.0f, 0.0f);
		gl.glVertex3f(ARENASIZE, 0.0f, -ARENASIZE);
		gl.glVertex3f(0.0f, 0.0f, -ARENASIZE);
		gl.glEnd();

	}

	void showObjects(GLAutoDrawable drawable) {

		the_hero.draw_self(drawable);
		the_thing.draw_self(drawable);
		for(int i = 0; i < countVillains; i++) {
			villain_array[i].draw_self(drawable);
		}
		//the_villain.draw_self(drawable);
	}

	void checkCollisions(GLAutoDrawable drawable) {
		Random random = new Random();
		if(the_hero.willCollide(the_thing)) {
			this.score += 1;
			the_thing.teleport();
			if (this.score % 3 == 0) {
				countVillains += 1;
			}
			villainSpeed += .08;

		}
		for (int i = 0; i < countVillains; i++) {
			if (villain_array[i].willCollide(the_hero)) {
				gameStatus = 1;


				switch (gameStatus) {
					case 0: // Reset All Scores and Start Over Entirely
						the_hero.reset();
						villain_array[i].reset();
						the_thing.reset();
						this.highScore = 0;
						this.score = 0;
						this.lastScore = 0;
						villainSpeed = .3;
						heroAlive = false;
						break;
					case 1: // Keeps High Score and Last Score and Start Over
						the_hero.reset();
						villain_array[i].reset();
						the_thing.reset();
						checkForHighScore();
						this.score = 0;
						this.lastScore = this.score;
						this.score = 0;
						villainSpeed = .3;
						heroAlive = false;
						break;
				}
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					//Handle exception
				}
				countVillains = 1;
			}
		}
	}

	void checkForHighScore() {
		if (score > highScore) {
			highScore = score;
		}
		lastScore = score;
	}

	void chaseHero() {
		for (int i = 0; i < countVillains; i++) {
			villain_array[i].chase(the_hero);
		}
	}

	public void dispose(GLAutoDrawable arg0) {
		// GLEventListeners must implement
	}

	// ///////////////////////////////////////////////////////////////
	// Methods in the KeyListener interface are keyTyped, keyPressed,
	// keyReleased. Listeners should affect the animation by changing
	// state variables, NOT by directory making calls to GL graphic
	// methods -- that should be left for the display method.

	public void keyTyped(KeyEvent key) {
	}

	public void keyPressed(KeyEvent key) {
		int ch = key.getKeyCode();
		switch (ch) {
			case 27:
				new Thread() {
					public void run() {
						animator.stop();
					}
				}.start();
				System.exit(0);
				break;
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_S:
				// Move backward
				the_hero.move(-8.0);
				break;
			case KeyEvent.VK_UP:
			case KeyEvent.VK_W:
				// Move forward
				the_hero.move(8.0);
				break;
			case KeyEvent.VK_LEFT:
			case KeyEvent.VK_A:
				// Turn left
				the_hero.turn(-2);
				break;
			case KeyEvent.VK_RIGHT:
			case KeyEvent.VK_D:
				// Turn right
				the_hero.turn(2);
				break;
			case KeyEvent.VK_ENTER:
				// Reset the Game and all Scores
				//gameStatus = 0;
				break;
			case KeyEvent.VK_SPACE:
				// Try Again and Keep High Score
				//gameStatus = 1;
				break;
			default:
				break;
		}

	}

	public void keyReleased(KeyEvent key) {
	}

}
