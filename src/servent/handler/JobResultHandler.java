package servent.handler;

import java.util.List;

import app.AppConfig;
import app.Job;
import app.Point;
import app.ServentInfo;
import app.util.ImageUtil;
import servent.message.JobResultMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class JobResultHandler implements MessageHandler {

	private Message clientMessage;
	
	public JobResultHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		
		try {
			
			if(clientMessage.getMessageType() == MessageType.JOB_RESULT) {
				
				JobResultMessage jobResultMessage = (JobResultMessage)clientMessage;
				
				int reciverId = jobResultMessage.getFinalReciverId();
				String jobName = jobResultMessage.getJobName();
				int width = jobResultMessage.getWidth();
				int height = jobResultMessage.getHeight();
				double proprotion = jobResultMessage.getProportion();
				List<Point> resultPoints = jobResultMessage.getComputedPoints();
				
				// Ako sam ja krajnji primalac poruke
				if(AppConfig.myServentInfo.getId() == reciverId) {
					
					// Render result
					if(AppConfig.MUTEX_ENABLED) {
						AppConfig.lamportMutex.releaseMyCriticalSecntion();
					}
					ImageUtil.renderImage(jobName, "", width, height, proprotion, resultPoints);
					
				}
				// Ako nisam ja krajnji primalac poruke, onda je samo prosledjujem
				else {
					// Forward JobResultMessage
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
					
					JobResultMessage refactorJobResultMessage = new JobResultMessage(jobResultMessage.getSenderIpAddress(), jobResultMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), reciverId, jobName, resultPoints, width, height, proprotion);
					MessageUtil.sendMessage(refactorJobResultMessage);					
				}
			}
			else {
				AppConfig.timestampedErrorPrint("JobResultHandler: got message that is not type of JOB_RESULT: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
	}
	
}
