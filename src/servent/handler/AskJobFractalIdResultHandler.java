package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.JobExecution;
import app.Point;
import app.ServentInfo;
import servent.message.AskJobFractalIdResultMessage;
import servent.message.JobFractalIDResultMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AskJobFractalIdResultHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public AskJobFractalIdResultHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.ASK_JOB_FRACTALID_RESULT) {
				
				AskJobFractalIdResultMessage askJobFractalIdResultMessage = (AskJobFractalIdResultMessage)clientMessage;
				
				int reciverId = askJobFractalIdResultMessage.getFinalReciverId();
				JobExecution jobExecution = AppConfig.chordState.getExecutionJob();
				
				// Ako sam ja krajni primalac poruke
				if(AppConfig.myServentInfo.getId() == reciverId) {
					
					int finalReciverId = AppConfig.chordState.getServentIdByServentPortAndIpAddress(askJobFractalIdResultMessage.getSenderPort(), askJobFractalIdResultMessage.getSenderIpAddress());
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
					
					String jobName = jobExecution.getJobName();
					String fractalId = jobExecution.getFractalId();
					int width = jobExecution.getWidth();
					int height = jobExecution.getHeight();
					double proportion = jobExecution.getProportion();
					
					List<Point> myComputedPoints = new ArrayList<Point>(jobExecution.getComputedPoints());
					JobFractalIDResultMessage fractalIDResultMessage = new JobFractalIDResultMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciverId, jobName, fractalId, myComputedPoints, width, height, proportion);
					
					MessageUtil.sendMessage(fractalIDResultMessage);
					
				}
				// Ako ja nisam krajnji primalac poruke
				else {
					
					// Forward AskJobFractalIdResultMessage
					String jobName = askJobFractalIdResultMessage.getJobName();

					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
					AskJobFractalIdResultMessage refactorAskJobFractalIdResultMessage = new AskJobFractalIdResultMessage(askJobFractalIdResultMessage.getSenderIpAddress(), askJobFractalIdResultMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), jobName, reciverId);
							
					MessageUtil.sendMessage(refactorAskJobFractalIdResultMessage);
					
				}
				
				
			}
			else {
				AppConfig.timestampedErrorPrint("AskJobFractalIdResultHandler: got message that is not type of ASK_JOB_FRACTALID_RESULT:" + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
}
