package app.util;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

import app.AppConfig;
import app.Point;

public class ImageUtil {
	
	public static void renderImage(String jobName, String fractalId, int width, int height, double proportion, List<Point> computedPoints) {
		
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		WritableRaster writableRaster = image.getRaster();
		
		int[] rgb = new int[3];
		rgb[0] = 255;
		
		for (Point point : computedPoints) {
			writableRaster.setPixel(point.getX(), point.getY(), rgb);
		}
		
		BufferedImage newImage = new BufferedImage(writableRaster.getWidth(), writableRaster.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
		newImage.setData(writableRaster);
				
		try {
			
			if(!fractalId.equals("")) {
				ImageIO.write(newImage, "PNG", new File("result/" + jobName + "_" + fractalId + "_" + proportion + ".png"));
			}
			else {
				ImageIO.write(newImage, "PNG", new File("result/" + jobName + "_" + proportion + ".png"));
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
			e.printStackTrace();
		}
		
		
	}
	
}
