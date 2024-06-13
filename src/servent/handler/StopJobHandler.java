package servent.handler;

import java.util.Map.Entry;

import app.AppConfig;
import app.FractalJob;
import app.Job;
import app.JobExecution;
import app.ScheduleType;
import app.ServentInfo;
import servent.message.JobExecutionMessage;
import servent.message.JobScheduleMessage;

import servent.message.Message;
import servent.message.MessageType;
import servent.message.StopJobMessage;
import servent.message.util.MessageUtil;

public class StopJobHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public StopJobHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.STOP_JOB) {
				
				
				StopJobMessage stopJobMessage = (StopJobMessage) clientMessage;
				
				String jobName = stopJobMessage.getJobName();
				
				// Ako ja radim taj posao onda zaustavljam izvrsavalje tog posla
				JobExecution jobExecution = AppConfig.chordState.getExecutionJob();
				if(jobExecution != null && jobExecution.getJobName().equals(jobName)) {
					jobExecution.stop();
					AppConfig.chordState.setExecutionJob(null);
				}
				
				// Uklonimo job iz liste liste aktivnih job-ova i liste serventJob
				AppConfig.chordState.removeJob(jobName);
				
				// Ako sam ja poslao StopJobMessage, onda se onda vratila znaci da je obisla sve servente u sistemu
				if(stopJobMessage.getSenderIpAddress().equals(AppConfig.myServentInfo.getIpAddress())
						&& stopJobMessage.getSenderPort() == AppConfig.myServentInfo.getListenerPort()) {
					
					AppConfig.timestampedStandardPrint("StopJobHandler: stopJobMessage  for job: " + jobName + " made circle.");
					
					if(AppConfig.MUTEX_ENABLED) {
						AppConfig.lamportMutex.releaseMyCriticalSecntion();
					}
					
					if(AppConfig.chordState.getActiveJobsCount() > 0) {
						AppConfig.timestampedStandardPrint("Rescheduling active jobs...");
						sendReschedulingMessage(ScheduleType.REMOVE_JOB);
					}
					
					return;

				}
				// Ako trenutni servent nije poslao stopJobMessage, onda treba da prosledi dalje stopJobMessage svoj sledbeniku
				else {
					this.forwardStopJobMessage(jobName, stopJobMessage);
				}
				
				
				
			}
			else {
				AppConfig.timestampedErrorPrint("StopJobHandler got message that is not type STOP_JOB: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
	// Prosledjuje svom sledecem sledbeniku stopJobMessage
	private void forwardStopJobMessage(String jobName, StopJobMessage stopJobMessage) {
		String nextNodeIpAddress = AppConfig.chordState.getNextNodeIpAddress();
		int nextNodePort = AppConfig.chordState.getNextNodePort();
		
		StopJobMessage refactorStopJobMessage = new StopJobMessage(stopJobMessage.getSenderIpAddress(), stopJobMessage.getSenderPort(), nextNodeIpAddress, nextNodePort, jobName);
		MessageUtil.sendMessage(refactorStopJobMessage);
	}
	
	// Salje svoj sledbeniku da rasporede opet poslove
	public static boolean sendReschedulingMessage(ScheduleType scheduleType) {
		
		for (Entry<Integer, FractalJob> entity : AppConfig.chordState.getServentJobs().entrySet()) {
			
			int scheduleExecutorServentId = entity.getKey();
			ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(scheduleExecutorServentId);
			
			JobScheduleMessage jobSchedulingMessage = new JobScheduleMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), scheduleExecutorServentId, scheduleType);
			MessageUtil.sendMessage(jobSchedulingMessage);
			
			return true;
			
		}
		
		return false;
		
	}
	
	
}
