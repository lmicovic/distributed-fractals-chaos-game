package servent.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.FractalJob;
import app.Job;
import app.JobExecution;
import app.Point;
import app.ScheduleType;
import app.ServentInfo;
import app.util.GeometryUtil;
import app.util.JobSchedule;
import servent.message.AckJobExecutionMessage;
import servent.message.ComputedPointsMessage;
import servent.message.JobExecutionMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class JobExecutionHandler implements MessageHandler{

	private Message clientMessage;
	
	public JobExecutionHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		
		try {
			
			if(clientMessage.getMessageType() == MessageType.JOB_EXECUTION) {
				
				JobExecutionMessage jobExecutionMessage = (JobExecutionMessage) clientMessage;
				
				int finalReciverId = jobExecutionMessage.getFinalReciverId();
				List<String> fractalIds = jobExecutionMessage.getJobFractalIds();
				List<Point> points = jobExecutionMessage.getStartPoints();
				Job job = jobExecutionMessage.getJob();
				int currentLevel = jobExecutionMessage.getLevel();
				Map<FractalJob, FractalJob> mappedFractalJobs = jobExecutionMessage.getMappedFractalsJobs();
				ScheduleType scheduleType = jobExecutionMessage.getScheduleType();
				int serventJobSchedulerId = jobExecutionMessage.getJobSchedulerId();
				
				AppConfig.chordState.setServentJobs(jobExecutionMessage.getServentJobs());
				
				// Ako sam ja krajni primalac poruke
				if(AppConfig.myServentInfo.getId() == finalReciverId) {
					
					AppConfig.timestampedStandardPrint("FractalIds: " + fractalIds);
					AppConfig.timestampedStandardPrint("Job: " + job.getName());
					AppConfig.timestampedStandardPrint("Staring points: " + points);
					
					// Ako je fractalIds 1 onda nema daljeg deljenja
					if(fractalIds.size() == 1) {
						
						// Saljemo ACK serventu koji je zapoceo posao
						ServentInfo nextJobInfo = AppConfig.chordState.getNextNodeForServentId(serventJobSchedulerId);
						
						AckJobExecutionMessage ackJobExecutionMessage = new AckJobExecutionMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextJobInfo.getIpAddress(), nextJobInfo.getListenerPort(), serventJobSchedulerId);
						MessageUtil.sendMessage(ackJobExecutionMessage);
						
						if(AppConfig.chordState.getExecutionJob() != null) {
							this.sendMyCurrentData(mappedFractalJobs, scheduleType);
						}
						
						String myNewFractalId = fractalIds.get(0);
						FractalJob myNewFractalJob = new FractalJob(myNewFractalId, job.getName());
						if(scheduleType.equals(ScheduleType.ADD_JOB) || scheduleType.equals(ScheduleType.REMOVE_SERVENT)) {
							// Izracunati koliko dataMessage poruke treba da primim od drugih servenata
							for (Entry<FractalJob, FractalJob> entity : mappedFractalJobs.entrySet()) {

								FractalJob oldFractaljob = entity.getKey();
								FractalJob newFractalJob = entity.getValue();
								
								if(newFractalJob.equals(myNewFractalJob)) {
									AppConfig.chordState.getExpectedComputedPointsMessagesCount().getAndIncrement();
								}
							}
						}
						else if(mappedFractalJobs.containsKey(myNewFractalJob)) {
							AppConfig.chordState.getExpectedComputedPointsMessagesCount().set(1);
						}
						
						// Cekamo da nam drugi serventi posalju svoje podatke
						int expectedMessageCount = AppConfig.chordState.getExpectedComputedPointsMessagesCount().get();
						AppConfig.timestampedStandardPrint("Waintig for " + expectedMessageCount + " servents to send me their data...");
						
						while(true) {
							if(AppConfig.chordState.getReceivedComputedPointsMessagesCount().get() == expectedMessageCount ||
									expectedMessageCount == 0) {
								break;
							}
						}
						
						AppConfig.timestampedStandardPrint("All computed points recived from all servents.");
						AppConfig.chordState.addNewJob(job);
						
						JobExecution jobExecution = new JobExecution(job.getName(), myNewFractalId, job.getProportion(), job.getWidth(), job.getHeight(), points);
						
						List<Point> receivedComputedPoints = new ArrayList<Point>(AppConfig.chordState.getReceivedComputedPoints());
						
						if(scheduleType.equals(ScheduleType.ADD_JOB) || scheduleType.equals(ScheduleType.REMOVE_SERVENT)) {
							jobExecution.getComputedPoints().addAll(receivedComputedPoints);
						}
						else {
							List<Point> insidePoints = GeometryUtil.getPointsInseidePolygon(points, receivedComputedPoints);
							jobExecution.getComputedPoints().addAll(insidePoints);
						}
						
						AppConfig.chordState.setExecutionJob(jobExecution);
						Thread jobExecutionThread = new Thread(jobExecution);
						jobExecutionThread.start();
						
						// Resetujemo recived data
						AppConfig.chordState.resetAfterReceivedComputedPoints();
						return;
					}
					
					// Ako je fractalIds.size() > 1 onda treba da radimo split
					else {
						
						int level = jobExecutionMessage.getLevel() + 1;
						int pointCount = job.getPointCount();
						double proportion = job.getProportion();
						
						for(int i = 0; i < pointCount; i++) {
							
							List<Point> regionPoints = JobSchedule.computeRegionPoints(points, i, proportion);
							List<String> partialFractalIds = new ArrayList<String>();
							for (String fractalId : fractalIds) {
								if(fractalId.charAt(level) - '0' == i) {
									partialFractalIds.add(fractalId);
								}
							}
							
							// Saljemo partialId, regionPoints i job sledecem serventu
							int finalReciverID = AppConfig.chordState.getIdForFractalIDAndJob(job.getName(), partialFractalIds.get(0));
							ServentInfo receiverServent = AppConfig.chordState.getNextNodeForServentId(finalReciverID);
							
							JobExecutionMessage executionMessage = new JobExecutionMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), receiverServent.getIpAddress(), receiverServent.getListenerPort(), partialFractalIds, regionPoints, job, AppConfig.chordState.getServentJobs(), level, finalReciverID, mappedFractalJobs, scheduleType, serventJobSchedulerId);
							
							executionMessage.setText("" + finalReciverID);
							
							MessageUtil.sendMessage(executionMessage);
							
							
							
						}	
					}
					
					
				}
				// Ako nisam ja kranji primalac poruke, onda prosledjujem poruku samo
				else {
					
					// Forward message
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
					
					JobExecutionMessage newJobeExecutionMessage = new JobExecutionMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), fractalIds, points, job, AppConfig.chordState.getServentJobs(), currentLevel, finalReciverId, mappedFractalJobs, scheduleType, serventJobSchedulerId);
					MessageUtil.sendMessage(newJobeExecutionMessage);
					
				}
				
			}
			else {
				AppConfig.timestampedErrorPrint("JobExecutionHandler: got message that is not type of JOB_EXECUTION: " + clientMessage.getMessageType());
			}
			
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
			e.printStackTrace();
		}
		
		
	}
	
	public static void sendMyCurrentData(Map<FractalJob, FractalJob> mappedFractalJobs, ScheduleType scheduleType) {
		
		JobExecution jobExecution = AppConfig.chordState.getExecutionJob();
		
		List<Point> myComputedPoints = new ArrayList<Point>(jobExecution.getComputedPoints());
		FractalJob myOldFractalJob = new FractalJob(jobExecution.getFractalId(), jobExecution.getJobName());
		
		if(mappedFractalJobs.containsKey(myOldFractalJob) 
				&& (scheduleType.equals(ScheduleType.ADD_JOB) || scheduleType.equals(ScheduleType.REMOVE_SERVENT))) {				

			sendComputedPointsMessage(myComputedPoints, myOldFractalJob, mappedFractalJobs.get(myOldFractalJob));
			
		}
		
		// ???
		
		else {
			for (Entry<FractalJob, FractalJob> entity : mappedFractalJobs.entrySet()) {
				if(entity.getValue().equals(myOldFractalJob)) {
					sendComputedPointsMessage(myComputedPoints, myOldFractalJob, entity.getKey());
				}
			}
		}
		
		jobExecution.stop();
		
		//---------------------------------------------------
		
	}
	
	private static void sendComputedPointsMessage(List<Point> compitedPoints, FractalJob myOldFractalJob, FractalJob receiverFractalJob) {
		
		int finalReciverId = AppConfig.chordState.getIdForFractalIDAndJob(receiverFractalJob.getJobName(), receiverFractalJob.getFractalId());
		ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
		
		ComputedPointsMessage computedPointsMessage = new ComputedPointsMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), myOldFractalJob.getJobName(), myOldFractalJob.getFractalId(), compitedPoints, finalReciverId);
		MessageUtil.sendMessage(computedPointsMessage);
		
	}
	
	
	
}
