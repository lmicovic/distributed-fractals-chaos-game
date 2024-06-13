package app;

import java.io.Serializable;
import java.util.List;

public class Job implements Serializable, Comparable<Job>{

	private static final long serialVersionUID = -1063145868243520246L;
	
	private final String name;
	private final int pointCount;				// n
	private final double proportion;			// p
	private final int width;
	private final int height;
	private final List<Point> points;			
	
	public Job(String name, int pointCount, int width, int height, double proportion, List<Point> points) {		
		this.name = name;
		this.proportion = proportion;
		this.width = width;
		this.height = height;
		this.pointCount = pointCount;
		this.points = points;
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public List<Point> getPoints() {
		return points;
	}

	public double getProportion() {
		return proportion;
	}

	public int getPointCount() {
		return pointCount;
	}

	@Override
	public boolean equals(Object obj) {
		
		Job other = (Job) obj;
		
		if(this.name.equals(other.getName())) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	@Override
	public int compareTo(Job o) {
		return name.compareTo(o.name);
	}
	
	@Override
	public String toString() {
		
		String pointsString = "";
		for (Point point : points) {
			double x = point.getX();
			double y = point.getY();
			String tmp = "(" + x + ", " + y + ")";
			pointsString += tmp + ", ";
		}
		
		pointsString = pointsString.substring(0, pointsString.length()-2);
		
		return "[" + name + "|" + pointCount + "|" + proportion + "|" + width + "|" + height + "|" + pointsString + "]";
	}
	
	
}
