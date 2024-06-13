package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import app.bootstrap.BootstrapServer;
import app.lamport_mutex.LamportClock;
import app.lamport_mutex.LamportMutex;


/**
 * This class contains all the global application configuration stuff.
 * @author bmilojkovic
 *
 */
public class AppConfig {

	// to: implementirati lamportMutex
	public static LamportClock lamportClock;
	public static LamportMutex lamportMutex;
	
	
	/**
	 * Convenience access for this servent's information
	 */
	public static ServentInfo myServentInfo;
		
	public static boolean INITIALIZED = false;
	public static BootstrapServer bootstrapServer;
	
	public static int SOFT_FAILURE_TIME;
	public static int HARD_FAILURE_TIME;
	
	public static int JOB_COUNT;
	
	public static ChordState chordState;
	
	public static boolean MUTEX_ENABLED = false;
	
	public static void readConfig(String configName, int serventId){
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(configName)));
			
		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open properties file. Exiting...");
			System.exit(0);
		}
		
		// BOOTSTRAP
		try {
			String BOOTSTRAP_IP = properties.getProperty("bs.ip");
			int BOOTSTRAP_PORT = Integer.parseInt(properties.getProperty("bs.port"));
			bootstrapServer = new BootstrapServer(BOOTSTRAP_IP, BOOTSTRAP_PORT);
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading bootstrap_port. Exiting...");
			System.exit(0);
		}
		
		// CHORD_SIZE
		try {
			int chordSize = Integer.parseInt(properties.getProperty("chord_size"));
			
			ChordState.CHORD_SIZE = chordSize;
			chordState = new ChordState();
			
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading chord_size. Must be a number that is a power of 2. Exiting...");
			System.exit(0);
		}
		
		// FAILURE_TIMES
		try {
			SOFT_FAILURE_TIME = Integer.parseInt(properties.getProperty("soft_failure_time"));
			HARD_FAILURE_TIME = Integer.parseInt(properties.getProperty("hard_failure_time"));
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Problem reading failure_times...");
			e.printStackTrace();
			System.exit(0);
		}
		
		// SERVENTS
		String servantIp = "";
		int servantPort = -1;
		try {
			
			servantIp = properties.getProperty("servent" + serventId + ".ip");
			servantPort = Integer.parseInt(properties.getProperty("servent" + serventId + ".port"));
			
			new ServentInfo(servantIp, serventId, servantPort);
			
			myServentInfo = new ServentInfo(servantIp, servantPort, servantPort + 10);
			
			// Read otherServents
			
		} catch (Exception e) {
			timestampedErrorPrint("Problem reading Servents...");
			e.printStackTrace();
			System.exit(0);
		}
		
		// JOBS
		try {
			
			String jobCountString = properties.getProperty("job_count");
			if(jobCountString != null) {
			
				JOB_COUNT = Integer.parseInt(jobCountString);
				
				for (int i = 1; i <= JOB_COUNT; i++) {
					
					String jobName = properties.getProperty("servent" + serventId + ".job" + i + ".name");
					
					if(jobName == null) {
						continue;
					}
					
					int pointCount = Integer.parseInt(properties.getProperty("servent" + serventId + ".job" + i + ".n"));
					
					if(pointCount < 3 || pointCount > 10) {
						AppConfig.timestampedErrorPrint("Error reading " + configName + " - jobName:" + jobName + " - " + "servent" + serventId + ".job" + i + ".n" + " should be number between 3 and 10...");
						System.exit(0);
					}
					
					double proportion = Double.parseDouble(properties.getProperty("servent" + serventId + ".job" + i + ".p"));
					
					if(proportion < 0.0 || proportion > 1.0) {
						AppConfig.timestampedErrorPrint("Error reading " + configName + " - jobName:" + jobName + " - " + "servent" + serventId + ".job" + i + ".p" + " should be number between 0 and 1...");
						System.exit(0);
					}
					
					int width = Integer.parseInt(properties.getProperty("servent" + serventId + ".job" + i + ".w"));
					int height = Integer.parseInt(properties.getProperty("servent" + serventId + ".job" + i + ".h"));
					
					List<Point> points = new ArrayList<Point>();
					String[] pointString = properties.getProperty("servent" + serventId + ".job" + i + ".points").split(",");
					for(int j = 0; j < pointString.length; j += 2) {
						int x = Integer.parseInt(pointString[j]);
						int y = Integer.parseInt(pointString[j+1]);
						Point point = new Point(x, y);
						points.add(point);
					}
					
					if(points.size() % pointCount != 0) {
						AppConfig.timestampedErrorPrint("You should have define " + pointCount + " points for job " + jobName);
						System.exit(0);
					}
					
					Job job = new Job(jobName, pointCount, width, height, proportion, points);
					myServentInfo.addJob(job);
					
				}
				
			}
			
			
			
			lamportClock = new LamportClock();
			lamportMutex = new LamportMutex(myServentInfo);
			
		} catch (Exception e) {
			timestampedErrorPrint("Problem reading jobs...");
			e.printStackTrace();
			System.exit(0);
		}
		
		
		
		
	}
	
	// Vraca serventID za prosledjeni jobName i fractalId
	// Vraca -1 ako ne nadje
	public static int getServentIdForFractalIDandJob(String jobName, String jobFractalId) {
		
		FractalJob fractalJob = new FractalJob(jobFractalId, jobName);
		for (Entry<Integer, FractalJob> entity : chordState.getServentJobs().entrySet()) {
			
			int serventId = entity.getKey();
			FractalJob jobsFractalJob = entity.getValue();
			
			if(jobsFractalJob.equals(fractalJob)) {
				return serventId;
			}
		}
		
		return -1;
		
	}
	
	public static ServentInfo getNextNodeForServentId(int reciverId) {
		
		// Ako sam ja reciver, onda saljemo sebi
		if(AppConfig.myServentInfo.getId() == reciverId) {
			return AppConfig.myServentInfo;
		}
		
		// Ako je reciver moj direkti sledbenik onda slajemo direktno njemu
		if(isServentMySuccessor(reciverId)) {
			return chordState.getAllNodeIdInfoMap().get(reciverId);
		}
		
		//-------------------------------------------------------------------
		
		// Ako reciver nije moj direktni sledbenik u sistemu, onda trazimo iz liste nasih sledbenika cvor koji se nalazi izmedju njih
		int leftServentId = chordState.getSuccessorTable()[0].getId();
		for(int i = 1; i < chordState.getSuccessorTable().length; i++) {
			int rightServentId = chordState.getSuccessorTable()[i].getId();
			// Ako se reciver nalazi izmedju naseg levog i desnog sledbenika, onda vracamo taj node 
			
			
			
			if(isBetweenServents(reciverId, leftServentId, rightServentId)) {
				return chordState.getSuccessorTable()[i-1];		
			}
			
			// Pomeramo se dalje
			leftServentId = reciverId;
		}
		
		// Edge case - ako ima samo jedan rightServent
		if(isBetweenServents(reciverId, leftServentId, chordState.getSuccessorTable()[0].getId())) {
			return chordState.getSuccessorTable()[chordState.getSuccessorTable().length-1];
		}
		
		return chordState.getSuccessorTable()[0];
		
	}
	
	// Proverava da li je serventID moj sledbenik u sistemu
	private static boolean isServentMySuccessor(int serventId) {
		
		for (ServentInfo successor : chordState.getSuccessorTable()) {
			if(successor.getId() == serventId) {
				return true;
			}	
		}
		return false;
		
	}
	
 	// Proverava da liee se da li se targetServent nalazi izmedju dva serventa 
	private static boolean isBetweenServents(int targetServentId, int leftServentId, int rightServetId) {
		
		// Pocinjemo od trenutnog
		int tmp = targetServentId;
		while(true) {
			
			// Idemo redom
			tmp = (tmp + 1) % chordState.getAllNodeIdInfoMap().size();
			
			// Ako trenutni serventId leftServentId onda nije izmedju 
			if(tmp == leftServentId) {
				return false;
			}
			
			// Ako je trenutni serventId rightServetId onda je izmedju 
			if(tmp == rightServetId) {
				return true;
			}
			
		}
		
	}
	
	public static Job getJobByName(String jobName) {
		for (Job job : chordState.getActiveJobsList()) {
			if(job.getName().equals(jobName)) {
				return job;
			}
		}
		return null;
	}
	
	
	
	/**
	 * Print a message to stdout with a timestamp
	 * @param message message to print
	 */
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - " + message);
	}
	
	/**
	 * Print a message to stderr with a timestamp
	 * @param message message to print
	 */
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.err.println(timeFormat.format(now) + " - " + message);
	}
	
	
}
