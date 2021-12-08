/* Muhammet Emre Cebeci 1705950 */
package shapes;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class Enemy implements Shape {
	Area area;
	
	
	public Enemy(double x, double y, double height) { // d as a diameter
		double division = height/7;// to give proportional shape, diameter as shapes calculated by height
		double diameter = division * 3; // 2 * radius
		
		Shape s1 = new Ellipse2D.Double(x,y,diameter,diameter); // First Circle
		Shape s2 = new Ellipse2D.Double(x,y+ 2*division,diameter,diameter);// Second Circle
		Shape s3 = new Ellipse2D.Double(x,y+ 2*division + 2*division,diameter,diameter);// Third Circle
		
		area = new Area(s1);
		Area areaS2 = new Area(s2);
		area.add(areaS2); // Area add
		areaS2 = new Area(s3);
		area.add(areaS2); 
	}
	
	
	@Override
	public Rectangle getBounds() {
		// TODO Auto-generated method stub
		return area.getBounds();
	}

	@Override
	public Rectangle2D getBounds2D() {
		// TODO Auto-generated method stub
		return area.getBounds2D();
	}

	@Override
	public boolean contains(double x, double y) {
		// TODO Auto-generated method stub
		return area.contains(x,y);
	}

	@Override
	public boolean contains(Point2D p) {
		// TODO Auto-generated method stub
		return area.contains(p);
	}

	@Override
	public boolean intersects(double x, double y, double w, double h) {
		// TODO Auto-generated method stub
		return area.intersects(x,y,w,h);
	}

	@Override
	public boolean intersects(Rectangle2D r) {
		// TODO Auto-generated method stub
		return area.intersects(r);
	}

	@Override
	public boolean contains(double x, double y, double w, double h) {
		// TODO Auto-generated method stub
		return area.contains(x,y,w,h);
	}

	@Override
	public boolean contains(Rectangle2D r) {
		// TODO Auto-generated method stub
		return area.contains(r);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at) {
		// TODO Auto-generated method stub
		return area.getPathIterator(at);
	}

	@Override
	public PathIterator getPathIterator(AffineTransform at, double flatness) {
		// TODO Auto-generated method stub
		return area.getPathIterator(at,flatness);
	}

}
