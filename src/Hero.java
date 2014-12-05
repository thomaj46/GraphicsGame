
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

//	gl.glBegin(GL2.GL_LINE_LOOP);
//	gl.glVertex2d (x-1, y-1);
//	gl.glVertex2d (x-1, y+1);
//	gl.glVertex2d (x+1, y+1);
//	gl.glVertex2d (x+1, y-1);
//	gl.glEnd();
	if (the_game.heroAlive) {
		// Draw the usual Hero on the 2D
		gl.glNewList(my_display_list, GL2.GL_COMPILE);
		gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, new float[]{ 1.0f, 1.0f, 1.0f, 1.0f }, 0);
		glut.glutSolidCone( bounding_cir_rad + 5, 25.0, 8, 8 );
		gl.glEndList(); 
    } else {
    	// Draw a Tombstone (Grey rectangle) here that appears for 5 seconds, on the 2D box after the villain catches the hero.
    	gl.glBegin(GL2.GL_QUADS);
    	gl.glColor3d(1,0,0);
    	gl.glVertex3f(-1,-1,-10);
    	gl.glColor3d(1,1,0);
    	gl.glVertex3f(1,-1,-10);
    	gl.glColor3d(1,1,1);
    	gl.glVertex3f(1,1,-10);
    	gl.glColor3d(0,1,1);
    	gl.glVertex3f(-1,1,-10);
    	gl.glEnd();
    }
//	gl.glNewList(my_display_list, GL2.GL_COMPILE);
//	glut.glutSolidCone( bounding_cir_rad + 5, 25.0, 8, 8 );
//	gl.glEndList();
  }

  void draw_self (GLAutoDrawable drawable) {

      GL2 gl = drawable.getGL().getGL2();
      
//      if (the_game.heroAlive) {
		  gl.glPushMatrix();
		  gl.glTranslated(x, 0.0, z );
		  gl.glRotated(-90.0,  1.0,0.0,0.0);
		  gl.glCallList(my_display_list);
		  gl.glPopMatrix();
//      } else {
//    	  
//      }
  }

}