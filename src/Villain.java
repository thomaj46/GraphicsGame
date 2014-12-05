import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import com.jogamp.opengl.util.gl2.GLUT;

// The Villain currently is a gluQuadric

public class Villain extends GameObject {
	
	off_file_object off_obj;
	float obj_material[] = { 0.8f, 0.0f, 0.0f, 1.0f }; // Red
	float specref[] = { 0.5f, 0.5f, 0.5f, 1.0f }; // material specular reflectance
	int specexp = 128; // Initalize to maximum falloff factor
	
	Villain(double x, double y, double z, int degrees, double bounding_cir_rad,
			int display_list, the_game playing_field, GLAutoDrawable drawable) {
		super(x, y, z, degrees, bounding_cir_rad, display_list, playing_field,
				drawable);
		
		GL2 gl = drawable.getGL().getGL2();
		
		off_obj = new off_file_object("king.off", false); // This off file specifies vertices in CW 
		// Good ones:
			// head
			// helm
			// helix2
		off_obj.load_off_file();
		
		GLU glu = my_playing_field.glu;
		GLUquadric cyl = glu.gluNewQuadric();
		GLUquadric top = glu.gluNewQuadric();

		gl.glNewList(my_display_list, GL2.GL_COMPILE);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, obj_material, 0);
		gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specref, 0);
		gl.glMateriali(GL2.GL_FRONT_AND_BACK, GL2.GL_SHININESS, specexp);

		for (int i = 0; i < off_obj.num_faces; i++) // For each face
		{
			gl.glColor3f(1.0f, 1.0f, 1.0f);
			gl.glBegin(GL2.GL_POLYGON);
			for (int j = 0; j < off_obj.num_verts_in_face[i]; j++) // Go through
																	// verts in
																	// that face
			{
				gl.glNormal3fv(off_obj.normal_to_face[i], 0); // Normals same
																// for all verts
																// in face
				int n = off_obj.verts_in_face[i][j];
				gl.glVertex3d(off_obj.vertices[n][0], off_obj.vertices[n][1], off_obj.vertices[n][2]);
			}
			gl.glEnd();
		}

		gl.glPopMatrix();
		gl.glEndList();
	}

	void draw_self(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();

		gl.glPushMatrix();
		gl.glTranslated(x, 0.0, z);
		gl.glScalef(30, 20, 20);
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