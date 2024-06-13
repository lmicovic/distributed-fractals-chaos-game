package servent.handler;

import java.security.Identity;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.FractalJob;
import app.Job;
import app.ScheduleType;
import app.ServentInfo;
import servent.message.AckIdleMessage;
import servent.message.IdleMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class IdleHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public IdleHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.IDLE) {
				
				IdleMessage idleMessage = (IdleMessage) clientMessage;
				
				int finalReciverId = idleMessage.getFinalReciverId();
				Map<Integer, FractalJob> serventJobs = idleMessage.getServentJobs();
				Map<FractalJob, FractalJob> mappedFractalJobs = idleMessage.getMappedFractals();
				List<Job> activeJobs = idleMessage.getActiveJobs();
				ScheduleType scheduleType = idleMessage.getScheduleType();
				int serventJobSechedulerId = idleMessage.getJobSchedulerServentId();
				
				// Ako smo mi krajnji primalac idle poruke
				if(AppConfig.myServentInfo.getId() == finalReciverId) {
				
					AppConfig.chordState.setServentJobs(serventJobs);
					AppConfig.chordState.addNewJobs(activeJobs);
					AppConfig.chordState.resetAfterReceivedComputedPoints();
					AppConfig.timestampedStandardPrint("This servent is idle...");
					
					if(AppConfig.chordState.getExecutionJob() != null) {
						JobExecutionHandler.sendMyCurrentData(mappedFractalJobs, scheduleType);
						AppConfig.chordState.setExecutionJob(null);
					}
					
					// Saljemo ack cvoru koji je zapoceo posao
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(serventJobSechedulerId);
					AckIdleMessage ackIdleMessage = new AckIdleMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), serventJobSechedulerId);
					
					AppConfig.timestampedStandardPrint("" + ackIdleMessage.getFinalReciverId());
					
					MessageUtil.sendMessage(ackIdleMessage);
					
				}
				// Ako nismo mi krajnji primalac idle poruke, prosledjujemo poruku
				else {
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
					
					IdleMessage refactorIdleMessage = new IdleMessage(idleMessage.getSenderIpAddress(), idleMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), serventJobs, finalReciverId, mappedFractalJobs, activeJobs, scheduleType, serventJobSechedulerId);
					MessageUtil.sendMessage(refactorIdleMessage);
				}
				
			}
			else {
				AppConfig.timestampedErrorPrint("IdleHandler: got message that is not type of IDLE_MESSAGE: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
	
	
}
