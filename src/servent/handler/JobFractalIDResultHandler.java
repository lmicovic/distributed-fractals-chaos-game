package servent.handler;

import java.util.List;

import app.AppConfig;
import app.Point;
import app.ServentInfo;
import app.util.ImageUtil;
import servent.message.JobFractalIDResultMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class JobFractalIDResultHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public JobFractalIDResultHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.JOB_FRACTALID_RESULT) {
				
				JobFractalIDResultMessage jobFractalIDResultMessage = (JobFractalIDResultMessage)clientMessage;
				
				int reciverId = jobFractalIDResultMessage.getFinalReciverId();
				String jobName = jobFractalIDResultMessage.getJobName();
				String fractalId = jobFractalIDResultMessage.getFractalId();
				int width = jobFractalIDResultMessage.getWidth();
				int height = jobFractalIDResultMessage.getHeight();
				double proportion = jobFractalIDResultMessage.getProportion();
				List<Point> resultPoints = jobFractalIDResultMessage.getComputedPoints();
				
				// Ako sam ja krajnji promalac poruk
				if(AppConfig.myServentInfo.getId() == reciverId) {
					
					// Render Result
					if(AppConfig.MUTEX_ENABLED) {
						AppConfig.lamportMutex.releaseMyCriticalSecntion();
					}
					ImageUtil.renderImage(jobName, fractalId, width, height, proportion, resultPoints);
					
				}
				// Ako ja nisam primalac poruke
				else {
					
					// Forward JobFractalIDResultMessage
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
					
					JobFractalIDResultMessage refactorFractalIDResultMessage = new JobFractalIDResultMessage(jobFractalIDResultMessage.getSenderIpAddress(), jobFractalIDResultMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), reciverId, jobName, fractalId, resultPoints, width, height, proportion);
					MessageUtil.sendMessage(refactorFractalIDResultMessage);
					
				}
				
				
				
			}
			else {
				AppConfig.timestampedErrorPrint("JobFractalIDResultHandler: got message that is not type of JOB_FRACTALID_RESULT: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
}
