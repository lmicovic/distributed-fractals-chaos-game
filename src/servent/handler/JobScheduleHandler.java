package servent.handler;

import app.AppConfig;
import app.ScheduleType;
import app.ServentInfo;
import app.util.JobSchedule;
import servent.message.JobScheduleMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class JobScheduleHandler implements MessageHandler {

	private Message clientMessage;
	
	public JobScheduleHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.JOB_SCHEDULE) {
				
				JobScheduleMessage jobScheduleMessage = (JobScheduleMessage) clientMessage;
				
				int finalReciverId = jobScheduleMessage.getFinalReciverId();
				ScheduleType scheduleType = jobScheduleMessage.getScheduleType();
				
				// Ako sam ja krajni primalaca JobSchedule poruke
				if(AppConfig.myServentInfo.getId() == finalReciverId) {
					
					int serventCount = AppConfig.chordState.getAllNodeIdInfoMap().size();
					JobSchedule.scheduleJob(serventCount, scheduleType);
					return;
					
				}
				// Ako nisam ja krajnji primalac poruke prosledim JobSchedule poruku svom prvom sledbeniku
				else {
					this.forwardJobScheduleMessage(finalReciverId, scheduleType, jobScheduleMessage);
				}
				
			}
			else {
				AppConfig.timestampedErrorPrint("JobScheduleHandler got message that is not type of JOB_SCHEDULE: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
	private void forwardJobScheduleMessage(int finalReciverId, ScheduleType scheduleType, JobScheduleMessage jobScheduleMessage) {
		
		ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
		
		JobScheduleMessage refactorJobScheduleMessage = new JobScheduleMessage(jobScheduleMessage.getSenderIpAddress(), jobScheduleMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciverId, scheduleType);
		MessageUtil.sendMessage(refactorJobScheduleMessage);
		
	}
	
}
