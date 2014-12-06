
/**
 * off_file_object -- given an off file and vertex winding direction,
 * read a Geomview off file into vertex, face, and normal arrays that
 * can be used by a client program to diplay the object
 *
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class off_file_object 
{
    // The following are intended to be accesible from a rendering client program
    public int num_vertex, num_faces; // No. of vertices and faces
    public double x_left, x_right, y_bottom, y_top, z_near, z_far; // Extreme min and max values
    public double [] [] vertices; // Each vertex an array of three values - x, y, z
    public int [] num_verts_in_face; // No. of vertices in each face
    public int [] [] verts_in_face;  // Index pointers to vertices in a given face
    public float [] [] normal_to_face; // Each face has normalized normal expressed as i,j,k coeffs

    String off_file_name;
    boolean normals_assume_ccw;

    public off_file_object(String off_file_name) { // Constructor assumes ccw for normals
	normals_assume_ccw = true;
	num_vertex = num_faces = 0;
	x_left = x_right = y_bottom = y_top = 0.0;
	this.off_file_name = off_file_name;
    }

    public off_file_object(String off_file_name, boolean ccw_normal_spec) {
	normals_assume_ccw = ccw_normal_spec;
	num_vertex = num_faces = 0;
	x_left = x_right = y_bottom = y_top = 0.0;
	this.off_file_name = off_file_name;
    }

    void load_off_file()	// Loads off file into arrays specified above
    {
	int i;
	// Initialization for max- and min-finding algorithms 
	x_left = Double.POSITIVE_INFINITY;
	x_right = Double.NEGATIVE_INFINITY;
	y_bottom = Double.POSITIVE_INFINITY;
	y_top = Double.NEGATIVE_INFINITY;
	z_far = Double.POSITIVE_INFINITY;
	z_near = Double.NEGATIVE_INFINITY;

        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(off_file_name)));
	} catch (Exception e) { System.out.println(e.toString()); };

	String line = null;
	String[] tokens;

	// throw away first line containing 'OFF' label
	try {
	    line = in.readLine();
	} catch (IOException e) { System.out.println(e.toString()); }

	// Second line tells us number of verts and faces
	try {
	    line = in.readLine();
	} catch (IOException e) { System.out.println(e.toString()); }

	tokens = line.trim().split("\\s+"); // Tokens separated by "whitespace"
	num_vertex = Integer.parseInt(tokens[0]);
	num_faces = Integer.parseInt(tokens[1]);

	vertices = new double [num_vertex] [3];

	for (i = 0; i < num_vertex; i++)
	    {
		try {
		    line = in.readLine();
		} catch (IOException e) { System.out.println(e.toString()); }
		tokens = line.trim().split("\\s+"); // Tokens separated by "whitespace"
		
		vertices[i][0] = Double.parseDouble(tokens[0]);
		vertices[i][1] = Double.parseDouble(tokens[1]);
		vertices[i][2] = Double.parseDouble(tokens[2]);
		if (vertices[i][0] < x_left) x_left = vertices[i][0];
		if (vertices[i][0] > x_right) x_right = vertices[i][0];
		if (vertices[i][1] < y_bottom) y_bottom = vertices[i][1];
		if (vertices[i][1] > y_top) y_top = vertices[i][1];
		if (vertices[i][2] < z_far) z_far = vertices[i][2];
		if (vertices[i][2] > z_near) z_near = vertices[i][2];
	    }

	num_verts_in_face = new int [ num_faces ];
	verts_in_face = new int [ num_faces ] [ ];
	normal_to_face = new float [ num_faces ] [3];

	for (i = 0; i < num_faces; i++)
	    {
		try {
		    line = in.readLine();
		} catch (IOException e) { System.out.println(e.toString()); }
		tokens = line.trim().split("\\s+"); // Tokens separated by "whitespace"
		
		num_verts_in_face[i] = Integer.parseInt(tokens[0]);
		verts_in_face[i] = new int [ num_verts_in_face[i] ];
		for (int j = 0; j < num_verts_in_face[i]; j++) {
		    verts_in_face[i][j] = Integer.parseInt(tokens[j+1]);
		}
		float points_for_normal [] [] = new float [3][3];
		if (normals_assume_ccw) {
		    for (int j = 0; j < 3; j++)
			for (int k = 0; k < 3; k++)
			    points_for_normal [j][k] = (float) vertices [verts_in_face[i][j]] [k];
		}
		else {
		    for (int j = 0; j < 3; j++)
			for (int k = 0; k < 3; k++)
			    points_for_normal [j][k] = (float) vertices [verts_in_face[i][2 - j]] [k];
		    //		    normal_to_face[i] = calcNormal(points_for_normal);
		}
		normal_to_face[i] = calcNormal(points_for_normal);
	    }
    }

    // Reduces a normal vector specified as a set of three coordinates,
    // to a unit normal vector of length one.
    float [] reduceToUnit(float vect [])
    {
	float length;

	float vect_new [] = new float [3];
	
	// Calculate the length of the vector		
	length = (float) Math.sqrt((vect[0]*vect[0]) + 
				   (vect[1]*vect[1]) +
				   (vect[2]*vect[2]));

	// Keep the program from blowing up by providing an acceptable
	// value for vectors that may calculated too close to zero.
	if (length == 0.0f) length = 1.0f;

	// Dividing each element by the length will result in a
	// unit normal vector.
	vect_new[0] = vect[0]/length;
	vect_new[1] = vect[1]/length;
	vect_new[2] = vect[2]/length;
	return vect_new;
    }


    // Points specified in counter clock-wise order arrive in v[0], v[1], v[2].
    // Normal vector to the plane determined by those three points is returned.
    float [] calcNormal(float v[][])
    {
	float out [] = new float [3];
	float v1 [] = new float [3];
	float v2 [] = new float [3];
	int x = 0;
	int y = 1;
	int z = 2;

	// Calculate two vectors from the three points
	v1[x] = v[0][x] - v[1][x];
	v1[y] = v[0][y] - v[1][y];
	v1[z] = v[0][z] - v[1][z];

	v2[x] = v[1][x] - v[2][x];
	v2[y] = v[1][y] - v[2][y];
	v2[z] = v[1][z] - v[2][z];

	// Take the cross product of the two vectors to get
	// the normal vector which will be stored in out
	out[x] = v1[y]*v2[z] - v1[z]*v2[y];
	out[y] = v1[z]*v2[x] - v1[x]*v2[z];
	out[z] = v1[x]*v2[y] - v1[y]*v2[x];

	// Normalize the vector (shorten length to one)
	out = reduceToUnit(out);
	return out;
    }




}
