import java.awt.*;
import java.awt.event.*;
import java.util.Random;

import com.jogamp.opengl.util.FPSAnimator;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;

import com.jogamp.opengl.util.gl2.GLUT;

// We're currently seeking Phong's Volkswagen

public class ThingWeAreSeeking extends GameObject {

	off_file_object off_obj;
	float obj_material[] = { 0.8f, 0.0f, 0.0f, 1.0f }; // Red
	float specref[] = { 0.5f, 0.5f, 0.5f, 1.0f }; // material specular
													// reflectance
	int specexp = 128; // Initalize to maximum falloff factor
	
	Random random;

	ThingWeAreSeeking(double x, double y, double z, int degrees, double bounding_cir_rad, int display_list, the_game playing_field, GLAutoDrawable drawable)
	{
		super(x, y, z, degrees, bounding_cir_rad, display_list, playing_field, drawable);
		this.random = new Random();
		GL2 gl = drawable.getGL().getGL2();
		off_obj = new off_file_object("dodecahedron.off", false); // This off file
																// specifies
																// vertices in
																// CW
		off_obj.load_off_file();

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

		GLU glu = my_playing_field.glu;
		GLUquadric top = glu.gluNewQuadric();

		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, my_playing_field.red, 0);
		gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, my_playing_field.red, 0);

		// We'll see a circular disk for the overhead view
		gl.glPushMatrix();
		glu.gluQuadricDrawStyle(top, GLU.GLU_FILL); // smooth shaded
		glu.gluQuadricNormals(top, GLU.GLU_SMOOTH);
		gl.glTranslated(x, 40.0, z);
		gl.glRotated(-90.0, 1.0, 0.0, 0.0);
		glu.gluDisk(top, 0.0, bounding_cir_rad + 10.0, 15, 5);
		gl.glPopMatrix();

		// But the hero's eye will see an off object
		gl.glPushMatrix();
		gl.glTranslated(x, 10.0, z);
		gl.glScalef(5, 5, 5);
		gl.glCallList(my_display_list);
		gl.glPopMatrix();
	}
	
	void teleport() {
		this.x = this.random.nextInt(980) + 10;
		this.z = this.random.nextInt(980) - 990;
	}

}