import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;

// The Villain currently is a gluQuadric

public class Villain extends GameObject {

	off_file_object off_obj;
	float obj_material[] = { 0.8f, 0.0f, 0.0f, 1.0f }; // Red
	float specref[] = { 0.5f, 0.5f, 0.5f, 1.0f }; // material specular reflectance
	int specexp = 128; // Initalize to maximum falloff factor
	int rotation = 10;

	Villain(double x, double y, double z, int degrees, double bounding_cir_rad,
			int display_list, the_game playing_field, GLAutoDrawable drawable, Texture texture) {
		super(x, y, z, degrees, bounding_cir_rad, display_list, playing_field,
				drawable);

		GL2 gl = drawable.getGL().getGL2();
		GLU glu = my_playing_field.glu;
		GLUquadric cyl = glu.gluNewQuadric();
		GLUquadric top = glu.gluNewQuadric();

		gl.glNewList(my_display_list, GL2.GL_COMPILE);

		texture.enable(gl);
		texture.bind(gl);

		// Set material properties.
		float[] rgba = {1f, 1f, 1f};
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
		gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, 0.5f);
		glu.gluQuadricTexture(cyl, true);
		gl.glPushMatrix();

		gl.glRotated(-90.0,  1.0,0.0,0.0);
		glu.gluCylinder(cyl, bounding_cir_rad, bounding_cir_rad, 25.0, 15, 5);
		gl.glPopMatrix();

		gl.glPushMatrix();

		gl.glTranslated(0, 25.0, 0 );
		gl.glRotated(-90.0,  1.0,0.0,0.0);
		glu.gluDisk(top, 0.0, bounding_cir_rad, 15, 5);

		texture.disable(gl);
		gl.glPopMatrix();
		gl.glEndList();
	}

	void draw_self(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		rotation += 3;
		gl.glPushMatrix();
		gl.glTranslated(x, 0.0, z);
		gl.glRotated(rotation, 0, 1, 0);
		gl.glCallList(my_display_list);
		gl.glPopMatrix();
	}

	void chase(GameObject hero) {
		double deltaX = Math.abs(this.x - hero.x);
		double deltaZ = Math.abs(this.z - hero.z);
		Random rand = new Random();

		if ((rand.nextInt(10) % 2) == 0) {
			if (deltaX > deltaZ) {
				if (this.x > hero.x) {
					this.x -= the_game.villainSpeed;
				} else {
					this.x += the_game.villainSpeed;
				}
			} else {
				if (this.z >= hero.z) {
					this.z -= the_game.villainSpeed;
				} else {
					this.z += the_game.villainSpeed;
				}
			}
		} else if ((rand.nextInt(10) % 3) == 0) {
			if (deltaX > deltaZ) {
				if (this.z >= hero.z) {
					this.z += the_game.villainSpeed + .02;
				} else {
					this.z += the_game.villainSpeed - .12;
				}
			} else {
				if (this.x > hero.x) {
					this.x -= the_game.villainSpeed - .8;
				} else {
					this.x -= the_game.villainSpeed + .2;
				}
			}
		} else {
			if (deltaX > deltaZ) {
				if (this.z >= hero.z) {
					this.z -= the_game.villainSpeed;
				} else {
					this.z += the_game.villainSpeed;
				}
			} else {
				if (this.x > hero.x) {
					this.x -= the_game.villainSpeed;
				} else {
					this.x += the_game.villainSpeed;
				}
			}
		}
	}
}