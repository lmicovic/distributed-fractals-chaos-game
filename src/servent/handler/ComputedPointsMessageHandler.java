package servent.handler;

import java.util.List;

import app.AppConfig;
import app.Point;
import app.ServentInfo;
import servent.message.ComputedPointsMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class ComputedPointsMessageHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public ComputedPointsMessageHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.COMPUTED_POINTS) {
				
				ComputedPointsMessage computedPointsMessage = (ComputedPointsMessage) clientMessage;
				
				String jobName = computedPointsMessage.getJobName();
				String fractalId = computedPointsMessage.getFractalId();
				List<Point> computedPointsList = computedPointsMessage.getComputedPoints();
				int finalReciverId = computedPointsMessage.getFinalReceiverId();
				
				// Ako sam ja krajnji reciver, dodajemo computedPoints u svoje computedPoints
				if(AppConfig.myServentInfo.getId() == finalReciverId) {
					AppConfig.timestampedStandardPrint("Recived computed points from {fractalID: " + fractalId + ", jobName: " + jobName + "}");
					AppConfig.chordState.addComputedPoints(computedPointsList);
				}
				// Ako nisam ja reciver za ComputedPointsMessage, onda prosledjujemo sledecem cvoru
				else if(AppConfig.myServentInfo.getId() != finalReciverId) {
					ServentInfo nextServent = AppConfig.getNextNodeForServentId(finalReciverId);
					ComputedPointsMessage newComputedPointsMessage = new ComputedPointsMessage(computedPointsMessage.getSenderIpAddress(), computedPointsMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), jobName, fractalId, computedPointsList, finalReciverId);					
					MessageUtil.sendMessage(newComputedPointsMessage);
				}
			}
			else {
				AppConfig.timestampedErrorPrint("ComputedPointsMessageHandler: got message that is not type of COMPUTED_POINTS: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
}
