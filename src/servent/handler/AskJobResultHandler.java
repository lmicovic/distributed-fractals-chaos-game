package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.Point;
import app.ServentInfo;
import servent.message.AskJobResultMessage;
import servent.message.JobResultMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AskJobResultHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public AskJobResultHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.ASK_JOB_RESULT) {
				
				AskJobResultMessage askJobResultMessage = (AskJobResultMessage) clientMessage;
				
				int lastServentId = askJobResultMessage.getLastServentId();
				int reciverId = askJobResultMessage.getFinalReciverId();
				String jobName = askJobResultMessage.getJobName();
				List<Point> recivedComputedPoints = askJobResultMessage.getComputedPoints();
				
				// Ako sam ja krajnji primalac poruke
				if(AppConfig.myServentInfo.getId() == reciverId) {
					
					List<Point> myComputedPoints = new ArrayList<Point>(AppConfig.chordState.getExecutionJob().getComputedPoints());
					recivedComputedPoints.addAll(myComputedPoints);
					
					if(AppConfig.myServentInfo.getId() == lastServentId) {
						// Saljemo rezultat cvoru koji je zatrazio rezultat
						int width = AppConfig.chordState.getExecutionJob().getWidth();
						int height = AppConfig.chordState.getExecutionJob().getHeight();
						double proportion = AppConfig.chordState.getExecutionJob().getProportion();
						int finalReciverId = AppConfig.chordState.getServentIdByServentPortAndIpAddress(askJobResultMessage.getSenderPort(), askJobResultMessage.getSenderIpAddress());
						
						ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
						
						JobResultMessage jobResultMessage = new JobResultMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciverId, jobName, recivedComputedPoints, width, height, proportion);
						MessageUtil.sendMessage(jobResultMessage);
						
					}
					else if(AppConfig.myServentInfo.getId() != lastServentId) {
						// Saljemo AskJobresultMessage nasem prvom sledbeniku
						int firstSuccessorId = AppConfig.chordState.getFirstSuccessorId();
						
						String nextNodeIpAddress = AppConfig.chordState.getNextNodeIpAddress();
						int nextNodePort = AppConfig.chordState.getNextNodePort();
						
						AskJobResultMessage refactorAskJobResultMessage = new AskJobResultMessage(askJobResultMessage.getSenderIpAddress(), askJobResultMessage.getSenderPort(), nextNodeIpAddress, nextNodePort, jobName, lastServentId, firstSuccessorId, recivedComputedPoints);
						MessageUtil.sendMessage(refactorAskJobResultMessage);

					}					
				}
				
				// Ako nisam ja krajni primalac proruke
				else {
					// Forward AskJobResultMessage 
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
					
					AskJobResultMessage refactorAskJobMessage = new AskJobResultMessage(askJobResultMessage.getSenderIpAddress(), askJobResultMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getFifoListenerPort(), jobName, lastServentId, reciverId, recivedComputedPoints);
					MessageUtil.sendMessage(refactorAskJobMessage);
				}
			}
			else {
				AppConfig.timestampedErrorPrint("AskJobResultHandler: got message that is not type of ASK_JOB_RESULT: " + clientMessage.getMessageType());
			}

		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
			e.printStackTrace();
		}
		
	}
	
}
