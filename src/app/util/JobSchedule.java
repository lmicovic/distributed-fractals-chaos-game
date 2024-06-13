package app.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.FractalJob;
import app.Job;
import app.Point;
import app.ScheduleType;
import app.ServentInfo;
import servent.message.IdleMessage;
import servent.message.JobExecutionMessage;
import servent.message.util.MessageUtil;

public class JobSchedule {

	public static Map<FractalJob, FractalJob> scheduleJob(int nodeCount, ScheduleType scheduleType) {
		
		// Mutex - lock
		
		if(AppConfig.MUTEX_ENABLED) {
			AppConfig.lamportMutex.acquireLock();
		}
		
		AppConfig.timestampedStandardPrint("Scheduling job...");
		
		List<Job> activeJobs = AppConfig.chordState.getActiveJobsList();
		
		//---------------------------------------------------------------------
		// Izracunati broj servenata za izracunavanje poslova
		//---------------------------------------------------------------------
		// Map(Key: Job, Key: brojServenata za izvrsavanje posla)
		Map<Job, Integer> jobServentCount = getServentCountForJobs(nodeCount, activeJobs); 
		AppConfig.timestampedStandardPrint("Servent Counts for executing all jobs: " + jobServentCount);
		
		//---------------------------------------------------------------------
		
		
		//---------------------------------------------------------------------
		// Izracunati fractalId za trenutni job
		//---------------------------------------------------------------------
		List<FractalJob> fractalJobs = new ArrayList<FractalJob>();
		// Key: job, Value: jobs fractalsIds
		Map<Job, List<String>> fractalsJobMap = new HashMap<Job, List<String>>();
		
		// Key: job Value: broj servenata za izvrsavanje posla 
		for (Entry<Job, Integer> entity : jobServentCount.entrySet()) {
			
			Job currentJob = entity.getKey();
			int assignedServentsCount = entity.getValue();		// broj servenata koji su dodeljeni za izvrsavanje posla
			
			// Izracunati broj servenata potreban da se uradi job
			int currentJobServentCount = comuteServentCountForJob(assignedServentsCount, currentJob.getPoints().size());			
			AppConfig.timestampedStandardPrint("Number of servent for job " + currentJob.getName() + ": " + assignedServentsCount);
			
			
			
			// Izracunati fractalId za trenutni posao
			List<String> currentFractals = computeFracatalIds(currentJobServentCount, currentJob.getPointCount());
			AppConfig.timestampedStandardPrint("FractalIDs for Job: " + currentJob + " - " + currentFractals);
			fractalsJobMap.put(currentJob, currentFractals);
			
			// Povezujemo job i fractalId
			for (String fractalId : currentFractals) {
				fractalJobs.add(new FractalJob(fractalId, currentJob.getName()));
			}
		}
		
		
		//---------------------------------------------------------------------
		// Dodeljujemo fractalJob serventima
		//---------------------------------------------------------------------
		Map<Integer, FractalJob> oldServentJobs = new HashMap<Integer, FractalJob>(AppConfig.chordState.getServentJobs());
		
		// Key: serventId, Value: fractalJob
		Map<Integer, FractalJob> newServentJobs = new HashMap<Integer, FractalJob>();
		
		// Dodeljujemo fractalJob serventima
		// Key: serventId, Value: serventInfo
		for (Entry<Integer, ServentInfo> entity : AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
			
			FractalJob fractalJob = fractalJobs.remove(0);
			newServentJobs.put(entity.getKey(), fractalJob);
			
			// Ako nemamo vise fractalJob
			if(fractalJobs.size() == 0) {
				break;
			}
			
		}
		
		// Dodajemo nove poslove za trenutnog serventa
		AppConfig.chordState.setServentJobs(newServentJobs);
		
		// Log
		AppConfig.timestampedStandardPrint("Old servent jobs: " + oldServentJobs);
		AppConfig.timestampedStandardPrint("New servent jobs: " + newServentJobs);
		//---------------------------------------------------------------------

		
		//---------------------------------------------------------------------
		// Mapiramo stare poslone na nove poslove
		//---------------------------------------------------------------------
		
		// FractalJob mappiranje za sve poslove
		Map<FractalJob, FractalJob> mappedFractals = new HashMap<FractalJob, FractalJob>();
		
		// Key: job, Value: jobs fractalsIds
		for (Entry<Job, List<String>> entity : fractalsJobMap.entrySet()) {
			
			String jobName = entity.getKey().getName();
			
			
			
			// Vraca sve fraktale koji pripadaju nekom jobu
			List<String> oldFractalIds = getJobFractals(jobName, oldServentJobs);
			
			// Mappiranje za trenutni posao
			Map<FractalJob, FractalJob> currentMappedFractals = mappFractals(jobName, oldFractalIds, entity.getValue(), scheduleType);
			
			for (Entry<FractalJob, FractalJob> e : currentMappedFractals.entrySet()) {
				// FractalJob mappiranje za sve poslove
				mappedFractals.put(e.getKey(), e.getValue());
			}
			
		}
		
		//---------------------------------------------------------------------
		// Sending JobExecutionMessage
		//---------------------------------------------------------------------
		AppConfig.timestampedStandardPrint("Mapped fractals: " + mappedFractals);
		AppConfig.timestampedStandardPrint("Fractals mapped for Job: " + fractalsJobMap);
		
		// Key: job, Value: fractalIds for Job
		for (Entry<Job, List<String>> entity : fractalsJobMap.entrySet()) {
			
			Job currentJob = entity.getKey();
			List<String> jobFractalIds = entity.getValue();
			
			List<Point> jobPoints = currentJob.getPoints();
			double proportion = currentJob.getProportion();
			
			// Ako samo jedan servent izvrsava posao, onda saljemo samo jednom
			if(jobFractalIds.size() == 1) {
				
				
				
				int originalReciverId = AppConfig.getServentIdForFractalIDandJob(currentJob.getName(), jobFractalIds.get(0));
				ServentInfo originalReciverServent = AppConfig.getNextNodeForServentId(originalReciverId);
				
				 JobExecutionMessage jobExecutionMessage = new JobExecutionMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), originalReciverServent.getIpAddress(), originalReciverServent.getListenerPort(),
						 jobFractalIds, jobPoints, currentJob, newServentJobs, 0, originalReciverId, mappedFractals, scheduleType, AppConfig.myServentInfo.getId());
				
				 jobExecutionMessage.setText("" + originalReciverId);
				 
				MessageUtil.sendMessage(jobExecutionMessage);
				continue;
			}
			
			
			
			// Ako vise servenata izvrsava posao, onda delimo posao i saljemo servetnima
			for(int i = 0; i < jobPoints.size(); i++) {
				// Delimo jobPoints na regione
				List<Point> regionPoints = computeRegionPoints(jobPoints, i, proportion);
				
				// Ono 01 02 03
				List<String> partialFractalIds = new ArrayList<String>();
				for (String fractal : jobFractalIds) {
					if(fractal.startsWith(Integer.toString(i))) {
						partialFractalIds.add(fractal);
					}
				}
				
				// Saljemo jednom serventu partalFractalIds, regionPoints i job
				int originalReciverId = AppConfig.getServentIdForFractalIDandJob(currentJob.getName(), partialFractalIds.get(0));
				ServentInfo originalReciver = AppConfig.getNextNodeForServentId(originalReciverId);
				
				JobExecutionMessage jobExecutionMessage = new JobExecutionMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), originalReciver.getIpAddress(), originalReciver.getListenerPort(),
						partialFractalIds, regionPoints, currentJob, newServentJobs,
                        0, originalReciverId, mappedFractals, scheduleType, AppConfig.myServentInfo.getId());
				
				jobExecutionMessage.setText("" + originalReciverId);
				
				MessageUtil.sendMessage(jobExecutionMessage);
				
			}
			
			
		}
		
		
		
		
		notifyIdleServents(mappedFractals, scheduleType);
		finishedJobScheduilng(nodeCount);		// mutex unlock
		
		return mappedFractals;
	}
	
	
	// Cekamo da ostali serventi posalju ack job execution ili ack idle message
	public static void finishedJobScheduilng(int serventCount) {
		
		
		
		
		// Ovde stane 
		
		
		
		AppConfig.timestampedStandardPrint("Waiting for ack messages...");
		while(true) {
			
//			try {
//				System.out.println(AppConfig.chordState.getReceivedAckMessagesCount().get() + " " + serventCount);
//				Thread.sleep(100);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			if(AppConfig.chordState.getReceivedAckMessagesCount().get() == serventCount) {
				break;
			}
		}
		
		AppConfig.timestampedStandardPrint("Recived all ack messages.");
		
		AppConfig.chordState.getReceivedAckMessagesCount().set(0);
		
		if(AppConfig.MUTEX_ENABLED) {
			AppConfig.lamportMutex.releaseMyCriticalSecntion();
		}
		
	}
	
	
	// Salje IdleMessage idle serventima da su idle i salje novi podelu posla
	public static void notifyIdleServents(Map<FractalJob, FractalJob> mappedFractals, ScheduleType scheduleType) { 
		
		for (Entry<Integer, ServentInfo> entity : AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
			
			int serventId = entity.getKey();
			
			// Ako se servent ne nalazi u listi serventJobs onda je Idle
			if(AppConfig.chordState.getServentJobs().containsKey(serventId) == false) {
				
				ServentInfo nextServent = AppConfig.getNextNodeForServentId(serventId);

				IdleMessage idleMessage = new IdleMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(),
														  new HashMap<>(AppConfig.chordState.getServentJobs()), serventId, mappedFractals, new ArrayList<>(AppConfig.chordState.getActiveJobsList()), scheduleType, AppConfig.myServentInfo.getId());
				
				idleMessage.setText("" + serventId);
				
				MessageUtil.sendMessage(idleMessage);	
			}
		}
		
	}
	
	// Racuna koliko je potrebno servenata da bi se posao izvrsio
	// serventCount - broj servenata koji imamo
	// pointsCount - broj tacaka koje imamo
	public static int comuteServentCountForJob(int serventCound, int pointsCount) {
		
		int result = 1;
		int x = 0;
		while(true) {
			
			int possibleServentCount = 1 + x * (pointsCount - 1);
			if(possibleServentCount > serventCound) {
				break;
			}
			
			result = possibleServentCount;
			x++;
			
		}
		
		return result;
	}
	
	// Vraca koliko je potrebno servenata za odredjeni posao
	public static Map<Job, Integer> getServentCountForJobs(int nodeCount, List<Job> activeJobList) {
		
		Map<Job, Integer> result = new HashMap<Job, Integer>();
		int jobCount = activeJobList.size();
		for (int i = 0; i < jobCount; i++) {
			int assignedNodeCount = nodeCount / jobCount;
			if(i < nodeCount % jobCount) {
				assignedNodeCount++;
			}
			result.put(activeJobList.get(i), assignedNodeCount);
		}

		return result;
	}
	
	// Racuna FraktalID-jeve za prosledjeni broj servenata i tacaka
	// Vraca listu fractalId-jeva
	public static List<String> computeFracatalIds(int serventCount, int pointCount) {
		
		List<String> fractalIdList = new ArrayList<String>();
		int lenght = 0;
		String base = "";
		
		// Ako jedan node izvrsava posao, onda ne treba da delimo na fraktale
		if(serventCount == 1) {
			fractalIdList.add("0");
			return fractalIdList;
		}
		
		// Ako imamo vise node-ova, onda posao treba da delimo na fraktale
		while(serventCount > 0) {
			
			if(lenght >= 1) {
				boolean hasLength = false;
				for (String fractalId : fractalIdList) {
					if(fractalId.length() == lenght) {
						base = fractalId;
						fractalIdList.remove(fractalId);
						hasLength = true;
						break;
					}
				}
				if(!hasLength) {
					lenght++;
					continue;
				}
				
				serventCount++;	
			}
			
			for (int i = 0; i < pointCount; i++) {
				fractalIdList.add(base + i);
			}

			if(lenght == 0) {
				lenght++;
			}
			
			serventCount = serventCount - pointCount;
		}
		
		Collections.sort(fractalIdList);
		return fractalIdList;
		
		
	}
	
	// Za prosledjeni jobName vraca fractalId-jeve koji pripadaju jobName iz map serventJobs
	public static List<String> getJobFractals(String jobName, Map<Integer, FractalJob> serventJobs) {
		
		List<String> result = new ArrayList<String>();

		// Prilacimo kroz servent poslove i ako su nazivi jobName i naziv posla za fractal isti onda fraktal pripada jobu
		// Key: serventId, Value: jobs fractal jobs
		for (Entry<Integer, FractalJob> entity : serventJobs.entrySet()) {
			
			String fractalJobName = entity.getValue().getJobName();
			
			if(fractalJobName.equals(jobName)) {
				result.add(entity.getValue().getFractalId());
			}
		}
		
		// Svi fraktali koji pripadaju poslu sa nazivom jobName
		Collections.sort(result);
		return result;
		
	}
	
	// Mapira stare na nove ili nove na stare fractalId za odrejeni jobName
	public static Map<FractalJob, FractalJob> mappFractals(String jobName, List<String> oldFractalIds, List<String> newFractalIds, ScheduleType scheduleType) {
		

		Map<FractalJob, FractalJob> result = new HashMap<FractalJob, FractalJob>();
		
		// Key: oldFractalJob, Value: newFractalJob
		//---------------------------------------------------------------------		
		if(scheduleType == ScheduleType.ADD_JOB || scheduleType == ScheduleType.REMOVE_SERVENT) {
			for (String oldFractalId : oldFractalIds) {
				
				// Ako samo jedan servent izvrsava job, mapiraj na sve stare fractalIds
				if(newFractalIds.size() == 1) {
					result.put(new FractalJob(oldFractalId, jobName), new FractalJob(newFractalIds.get(0), jobName));	// get(0) zato sto je size od newFractalIds jednak 1
					continue;
				}

				// Ako vise servenata izvrsava job
				for (String newFractalId : newFractalIds) {
					// Ako pocinju isto onda treba da se mappiraju
					if(oldFractalId.startsWith(newFractalId)) {
						result.put(new FractalJob(oldFractalId, jobName), new FractalJob(newFractalId, jobName));
					}
				}
			
			}
			
			// Key: oldFractalJob, Value: newFractalJob
			return result;
		}
		//---------------------------------------------------------------------
		
		
		// Key: newFractalJob, Value: oldFractalJob
		// Isto kao ono gore samo obrnuto prvo prolazimo kroz newFractalIds pa proveravamo oldFractalIds
		if(scheduleType == ScheduleType.REMOVE_JOB || scheduleType == ScheduleType.ADD_SERVENT) {
			
			for (String newFractalId : newFractalIds) {
				
				// Ako posao jobName izvrsava samo jedan servent
				if(oldFractalIds.size() == 1) {
					result.put(new FractalJob(newFractalId, jobName), new FractalJob(oldFractalIds.get(0), jobName));
					continue;
				}
				
				// Ako posao izvrsava vise servenata
				for (String oldFractalId : oldFractalIds) {
					if(newFractalId.startsWith(oldFractalId)) {
						result.put(new FractalJob(newFractalId, jobName), new FractalJob(oldFractalId, jobName));
					}
				}
				
			}
			
			// Key: newFractalJob, Value: oldFractalJob
			return result;
		}
		
		return result;
		
	}
	
	// Racuna regione za prosledjene tacke u zavisnosti od proporcije
	public static List<Point> computeRegionPoints(List<Point> jobPoints, int i, double proportion) {
		
		List<Point> regionPoints = new ArrayList<Point>();
		Point point = jobPoints.get(i);
		
		for(int j = 0; j < jobPoints.size(); j++) {
			if(i == j) {
				regionPoints.add(point);
				continue;
			}
			
			Point otherPoint = jobPoints.get(j);
			int newX = (int) (point.getX() + proportion * (otherPoint.getX() - point.getX()));
			int newY = (int) (point.getY() + proportion * (otherPoint.getY() - point.getY()));
			
			Point newPoint = new Point(newX, newY);
			
			regionPoints.add(newPoint);
			
		}
		
		return regionPoints;
	}
	
}
