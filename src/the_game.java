import java.awt.*;
import java.awt.event.*;
import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import com.jogamp.opengl.util.gl2.GLUT;

public class the_game extends JFrame implements GLEventListener, KeyListener {
	static GLU glu;
	static GLUT glut;

	static GLCapabilities caps;
	static FPSAnimator animator;

	static float WALLHEIGHT = 70.0f; // Some playing field parameters
	static float ARENASIZE = 1000.0f;
	static float EYEHEIGHT = 15.0f;
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

	float ga[] = { 0.2f, 0.2f, 0.2f, 1.0f }; // global ambient light intensity
	float la0[] = { 0.0f, 0.0f, 0.0f, 1.0f }; // light 0 ambient intensity
	float ld0[] = { 1.0f, 1.0f, 1.0f, 1.0f }; // light 0 diffuse intensity
	float lp0[] = { 0.0f, 1.0f, 1.0f, 0.0f }; // light 0 position
	float ls0[] = { 1.0f, 1.0f, 1.0f, 1.0f }; // light 0 specular
	float ma[] = { 0.02f, 0.2f, 0.02f, 1.0f }; // material ambient
	float md[] = { 0.08f, 0.6f, 0.08f, 1.0f }; // material diffuse
	float ms[] = { 0.6f, 0.7f, 0.6f, 1.0f }; // material specular
	int me = 75; // shininess exponent
	float red[] = { 1.0f, 0.0f, 0.0f, 1.0f }; // pure red
	float blue[] = { 0.0f, 0.0f, 1.0f, 1.0f }; // pure blue
	float yellow[] = { 1.0f, 1.0f, 0.0f, 1.0f }; // pure yellow
	int displayListBase;
	int score;
	int lastScore;
	int highScore;

	Hero the_hero; // Three objects on the playing field to
	ThingWeAreSeeking the_thing; // start with, each with its own display list.
	Villain the_villain; // Adding more will be good for GGW points

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

		JFrame frame = new JFrame("the_game");
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
		the_hero = new Hero(eyex, 0.0, eyez, 135, 10.0, displayListBase, this, drawable);
		the_thing = new ThingWeAreSeeking(ARENASIZE / 4.0, 0.0, -ARENASIZE / 4.0, 0, 30.0, displayListBase + 1, this, drawable);
		the_villain = new Villain((3 * ARENASIZE) / 4.0, 0.0, -ARENASIZE / 4.0, 0, 10.0, displayListBase + 2, this, drawable);

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

		int horiz_offset, vert_offset;
		
		gl.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
		gl.glClear(GL2.GL_DEPTH_BUFFER_BIT | GL2.GL_COLOR_BUFFER_BIT);

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
		horiz_offset = (int) (width * (1.0 - HERO_VP) / 6.0);
		vert_offset = height / 6;
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
		checkCollisions();
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

		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, blue, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, blue, 0);
		gl.glBegin(GL2.GL_POLYGON);
		gl.glNormal3f(0.0f, 1.0f, 0.0f);
		gl.glVertex3f(0.0f, 0.0f, 0.0f);
		gl.glVertex3f(ARENASIZE, 0.0f, 0.0f);
		gl.glVertex3f(ARENASIZE, 0.0f, -ARENASIZE);
		gl.glVertex3f(0.0f, 0.0f, -ARENASIZE);
		gl.glEnd();

	}

	void showObjects(GLAutoDrawable drawable) {
		the_thing.draw_self(drawable);
		the_hero.draw_self(drawable);
		the_villain.draw_self(drawable);
	}
	
	void checkCollisions() {
		if(the_hero.willCollide(the_thing)) {
			the_thing.teleport();
			this.score += 1;
		}
		
		if (the_villain.willCollide(the_hero)) {
			this.score = 0;
			the_hero.reset();
			the_villain.reset();
			the_thing.reset();
			checkForHighScore();
		}
	}
	
	void checkForHighScore() {
		if (score > highScore) {
			highScore = score;
		}
		lastScore = score;
	}
	
	void chaseHero() {
		the_villain.chase(the_hero);
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
		default:
			break;
		}

	}

	public void keyReleased(KeyEvent key) {
	}

}
