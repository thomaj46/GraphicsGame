import java.awt.*;
import java.awt.event.*;
import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import com.jogamp.opengl.util.gl2.GLUT;

// The Villain currently is a gluQuadric

public class Villain extends GameObject {

	Villain(double x, double y, double z, int degrees, double bounding_cir_rad,
			int display_list, the_game playing_field, GLAutoDrawable drawable) {
		super(x, y, z, degrees, bounding_cir_rad, display_list, playing_field,
				drawable);

		GL2 gl = drawable.getGL().getGL2();
		GLU glu = my_playing_field.glu;
		GLUquadric cyl = glu.gluNewQuadric();
		GLUquadric top = glu.gluNewQuadric();

		gl.glNewList(my_display_list, GL2.GL_COMPILE);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, my_playing_field.yellow,
				0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, my_playing_field.yellow,
				0);
		glu.gluQuadricDrawStyle(cyl, GLU.GLU_FILL); // smooth shaded
		glu.gluQuadricNormals(cyl, GLU.GLU_SMOOTH);
		gl.glPushMatrix();

		gl.glRotated(-90.0, 1.0, 0.0, 0.0);
		glu.gluCylinder(cyl, bounding_cir_rad, bounding_cir_rad, 25.0, 15, 5);
		gl.glPopMatrix();

		gl.glPushMatrix();

		gl.glTranslated(0, 25.0, 0);
		gl.glRotated(-90.0, 1.0, 0.0, 0.0);
		glu.gluDisk(top, 0.0, bounding_cir_rad, 15, 5);

		gl.glPopMatrix();
		gl.glEndList();

	}

	void draw_self(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix();
		gl.glTranslated(x, 0.0, z);
		gl.glCallList(my_display_list);
		gl.glPopMatrix();
	}

	void chase(GameObject hero) {
		double deltaX = Math.abs(this.x - hero.x);
		double deltaZ = Math.abs(this.z - hero.z);

		if (deltaX > deltaZ) {
			if (this.x > hero.x) {
				this.x -= .5;
			} else {
				this.x += .5;
			}
		} else {
			if (this.z >= hero.z) {
				this.z -= .5;
			} else {
				this.z += .5;
			}
		}
	}
}