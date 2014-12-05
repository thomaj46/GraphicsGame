
import java.awt.*;
import java.awt.event.*; 
import com.jogamp.opengl.util.FPSAnimator;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.*;
import com.jogamp.opengl.util.gl2.GLUT;

public abstract class GameObject {

    public double x,y,z;		// position
    public double startX, startY, startZ;
    public int degrees;			// Degree measure of direction 
    public int startDegrees;
    public double xdir,zdir;		// Vector measure of direction 
    public double startXdir;
    public double startZdir;
    public double bounding_cir_rad;	// Radius of bounding circle -- to detect collision
    public int my_display_list;
    the_game my_playing_field;

    GameObject (double x, double y, double z, int degrees, double bounding_cir_rad, int display_list, the_game playing_field, GLAutoDrawable drawable)
    {
		this.x = x;
		this.y = y;
		this.z = z;
		this.startX = x;
		this.startY = y;
		this.startZ = z;
		this.degrees = degrees;
		this.startDegrees = degrees;
		this.xdir = Math.cos(Math.toRadians(this.degrees));
		this.zdir = Math.sin(Math.toRadians(this.degrees));
		this.startXdir = this.xdir;
		this.startZdir = this.zdir;
		this.bounding_cir_rad = bounding_cir_rad;
		this.my_display_list = display_list;
		this.my_playing_field = playing_field;
    }

    void turn(int degrees_rotation) {
		degrees = (degrees + degrees_rotation) % 360;
		xdir = Math.cos(Math.toRadians(this.degrees));
		zdir = Math.sin(Math.toRadians(this.degrees));
    }

    void move(double speed) {		// Pass in negative speed for backward motion
		x = x + speed * xdir;
		z = z + speed * zdir;

		if (this.x <= 0)
		{
			this.xdir = -this.xdir;

			if (this.zdir > 0)
			{
				this.degrees = (int)Math.toDegrees(Math.atan(this.zdir/this.xdir));
			}
			else
			{
				this.degrees = (int)Math.toDegrees(Math.atan(this.zdir/this.xdir));
			}
		}

		if (this.x >= 1000)
		{
			this.xdir = -this.xdir;

			if (this.zdir > 0)
			{
				this.degrees = (int)Math.toDegrees(Math.atan(this.zdir/this.xdir)) + 180;
			}
			else
			{
				this.degrees = (int)Math.toDegrees(Math.atan(this.zdir/this.xdir)) + 180;
			}
		}

		if (this.z >= 0 || this.z <= -1000)
		{
			this.zdir = -this.zdir;

			if (this.xdir > 0)
			{
				this.degrees = (int)Math.toDegrees(Math.atan(this.zdir/this.xdir));
			}
			else
			{
				this.degrees = (int)Math.toDegrees(Math.atan(this.zdir/this.xdir)) + 180;
			}
		}
    }

    abstract void draw_self (GLAutoDrawable drawable);
    
    boolean willCollide(GameObject other)
    {
    	double deltaX = this.x - other.x;
    	double deltaZ = this.z - other.z;
    	double distance = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
    	return distance <= this.bounding_cir_rad || distance <= other.bounding_cir_rad;
    }
    
    void reset() {
    	this.x = this.startX;
    	this.y = this.startY;
    	this.z = this.startZ;
    	this.degrees = this.startDegrees;
    	this.xdir = this.startXdir;
    	this.zdir = this.startZdir;
    }
    
  
}
