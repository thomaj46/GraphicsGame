
import java.awt.*;
import java.awt.event.*; 
import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import com.jogamp.opengl.util.gl2.GLUT;

public class Hero extends GameObject {

  Hero (double x, double y, double z, int degrees, double bounding_cir_rad, int display_list, the_game playing_field, GLAutoDrawable drawable) {
	super (x, y, z, degrees, bounding_cir_rad, display_list, playing_field, drawable);

	GL2 gl = drawable.getGL().getGL2();
	GLUT glut = my_playing_field.glut;

	gl.glNewList(my_display_list, GL2.GL_COMPILE);
	gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, new float[]{ 1.0f, 1.0f, 1.0f, 1.0f }, 0);
	glut.glutSolidCone( bounding_cir_rad + 5, 25.0, 8, 8 );
	gl.glEndList(); 
  }

  void draw_self (GLAutoDrawable drawable) {

      GL2 gl = drawable.getGL().getGL2();
		  gl.glPushMatrix();
		  gl.glTranslated(x, 0.0, z );
		  gl.glRotated(-90.0,  1.0,0.0,0.0);
		  gl.glCallList(my_display_list);
		  gl.glPopMatrix();
  }

}