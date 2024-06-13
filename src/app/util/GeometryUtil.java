package app.util;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import app.Point;

public class GeometryUtil {

	public static List<Point> getPointsInseidePolygon(List<Point> polygon, List<Point> targetPoints) {
		
		List<Point> result = new ArrayList<Point>();
		for (Point myPoint : targetPoints) {
			if(insidePolygon(polygon, myPoint)) {
				result.add(myPoint);
			}
		}
		return result;
	}
	
	private static boolean insidePolygon(List<Point> polygon, Point myPoint) {
		
		boolean result = false;
		for(int i = 0, j = polygon.size()-1; i < polygon.size(); j = i++) {
			if ((polygon.get(i).getY() > myPoint.getY()) != (polygon.get(j).getY() > myPoint.getY()) &&
                    (myPoint.getX() <
                            (polygon.get(j).getX() - polygon.get(i).getX()) *
                                    (myPoint.getY() - polygon.get(i).getY()) /
                                    (polygon.get(j).getY() - polygon.get(i).getY()) +
                                    polygon.get(i).getX())) {
				
				result = !result;
				
			}
		}
		return result;
	}
	
	
}
