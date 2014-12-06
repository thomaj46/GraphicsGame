import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.GLBuffers;

import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

// We're currently seeking Phong's Volkswagen

public class ThingWeAreSeeking extends GameObject {

	off_file_object off_obj;
	float obj_material[] = { 0.8f, 0.0f, 0.0f, 1.0f }; // Red
	float specref[] = { 0.5f, 0.5f, 0.5f, 1.0f }; // material specular
	// reflectance
	int specexp = 128; // Initalize to maximum falloff factor

	Random random;

	ThingWeAreSeeking(double x, double y, double z, int degrees, double bounding_cir_rad, int display_list, the_game playing_field, GLAutoDrawable drawable, Texture texture)
	{
		super(x, y, z, degrees, bounding_cir_rad, display_list, playing_field, drawable);
		this.random = new Random();
		GL2 gl = drawable.getGL().getGL2();
		off_obj = new off_file_object("epcot.off", false); // This off file specifies vertices in CW
		off_obj.load_off_file();

		gl.glNewList(my_display_list, GL2.GL_COMPILE);

		// Set material properties.
		float[] rgba = {1f, 1f, 1f};
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, rgba, 0);
		gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, rgba, 0);
		gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, 0.5f);

		texture.enable(gl);
		texture.bind(gl);

		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_S, GL2.GL_REPEAT);
		gl.glTexParameteri(GL2.GL_TEXTURE_2D, GL2.GL_TEXTURE_WRAP_T, GL2.GL_REPEAT);
		gl.glTexEnvf(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, GL2.GL_DECAL);

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
				gl.glTexCoord2f((float)i/off_obj.num_faces, (float)j/off_obj.num_verts_in_face[i]);
			}
			gl.glEnd();
		}

		texture.disable(gl);

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
		gl.glScalef(-1, -1, -1);
		gl.glRotated(-90.0, 1.0, 0.0, 0.0);
		glu.gluDisk(top, 0.0, 25.0, 15, 5);
		gl.glPopMatrix();

		// But the hero's eye will see an off object

		gl.glPushMatrix();
		gl.glTranslated(x, 20.0, z);
		gl.glScalef(20, 20, 20);
		gl.glCallList(my_display_list);
		gl.glPopMatrix();

	}

	void teleport() {
		this.x = this.random.nextInt(980) + 10;
		this.z = this.random.nextInt(980) - 990;
	}

}