package app;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JobExecution implements Runnable {
	
	private final String jobName;
	private final String fractalId;
	private final double proportion; 			// p
	private final int width;
	private final int height;
	private final List<Point> startingPoints;
	private List<Point> computedPoints;
	
	private volatile boolean working;
	
	public JobExecution(String jobName, String fractalId, double proportion, int width, int height, List<Point> starteingPoints) {
		
		this.jobName = jobName;
		this.fractalId = fractalId;
		this.proportion = proportion;
		this.width = width;
		this.height = height;
		this.startingPoints = starteingPoints;
		this.computedPoints = new ArrayList<Point>();
		
		this.working = true;
		
	}
	
	@Override
	public void run() {

		AppConfig.timestampedStandardPrint("Computing points for job: " + jobName);
		while(working) {
			
			Point newPoint = computeNewPoint();
			computedPoints.add(newPoint);
			
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		AppConfig.timestampedStandardPrint("JobExecution: stoped computing for job: " + jobName);
		
	}
	
	private Point getRandomStartPoint() {
		Random rand = new Random();
		int idx = rand.nextInt(startingPoints.size());
		
		return startingPoints.get(idx);
	}
	
	// Vraca random point
	private Point getRandomPoint() {
		Random rand = new Random();
		int x = rand.nextInt(width + 1);
		int y = rand.nextInt(height + 1);
		
		return new Point(x, y);
	}
	
	private Point computeNewPoint() {
		
		// Ako je list izracunatih tacaka pracna vracamo random izracunatu tacku
		if(computedPoints.isEmpty()) {
			return getRandomPoint();
		}
		
		// Ako lista izracunatih tacaka nije prazna
		Point lastPoint = computedPoints.get(computedPoints.size()-1);
		Point randomPoint = getRandomStartPoint();
		int newX = (int) (randomPoint.getX() + proportion * (lastPoint.getX() - randomPoint.getX()));
        int newY = (int) (randomPoint.getY() + proportion * (lastPoint.getY() - randomPoint.getY()));
		
        Point newPoint = new Point(newX, newY);
        return newPoint;
		
	}
	
	public String getJobName() {
		return jobName;
	}
	
	public String getFractalId() {
		return fractalId;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public double getProportion() {
		return proportion;
	}
	
	public List<Point> getComputedPoints() {
		return computedPoints;
	}
	
	public void stop() {
		this.working = false;
	}
	
	
}
