package shapes;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

public class Cannon implements Shape {  
	Area area;
	
	public Cannon(double x, double y, double w, double h) { 
		float r = (float) (w/6);
		Shape s1 = new Rectangle2D.Double(x+r, y, 5*r, h-2*r);
		Shape s2 = new Ellipse2D.Double(x, y+(h-3*r), 2*r, 2*r);
	    area = new Area(s1);
	    Area areaS2 = new Area(s2);
	    area.add(areaS2);
	}
	
	public boolean contains(Rectangle2D rect) {
    return area.contains(rect);
	}

	public boolean contains(Point2D point) {
		return area.contains(point);
	}

	public boolean contains(double x, double y) {
		return area.contains(x, y);
    }

	public boolean contains(double x, double y, double w, double h) {
		return area.contains(x, y, w, h);
  }

	public Rectangle getBounds() {
    return area.getBounds();
  }
  
	public Rectangle2D getBounds2D() {
    return area.getBounds2D();
  }
	
  public PathIterator getPathIterator(AffineTransform at) {
    return area.getPathIterator(at);
  }

  public PathIterator getPathIterator(AffineTransform at, double flatness) {
    return area.getPathIterator(at, flatness);
  }

  public boolean intersects(Rectangle2D rect) {
    return area.intersects(rect);
  }

  public boolean intersects(double x, double y, double w, double h) {
    return area.intersects(x, y, w, h);
  }

}
