package servent.handler;

import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellStatusMessage;
import servent.message.util.MessageUtil;

public class TellStatusHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public TellStatusHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.TELL_STATUS) {
				
				TellStatusMessage tellStatusMessage = (TellStatusMessage) clientMessage;
				
				int finalReciverId = tellStatusMessage.getFinalReciverId();
				// Map<Key: jobName, Value: Map<Key: fractalId, Value: pointsCount>>
				Map<String, Map<String, Integer>> resultMap = tellStatusMessage.getResultMap();
				int version = tellStatusMessage.getVersion();
				
				// Ako sam ja krajni primalac TellStatusMessage
				if(AppConfig.myServentInfo.getId() == finalReciverId) {
					
					if(AppConfig.MUTEX_ENABLED) {
						AppConfig.lamportMutex.releaseMyCriticalSecntion();
					}
					
					StringBuilder result = new StringBuilder("Status:\n");
					// Map<Key: jobName, Value: Map<Key: fractalId, Value: pointsCount>>
					for (Entry<String, Map<String, Integer>> entity : resultMap.entrySet()) {
						String jobName = entity.getKey();
						result.append("jobName=" + jobName + "\n");
						int totalPointCount = 0;
						int totalServentCount = 0;
						for (Entry<String, Integer> e : resultMap.get(jobName).entrySet()) {
							String fractalId = e.getKey();
							int fractalPointCount = e.getValue();
							result.append("fractalId=" + fractalId + ", pointsCount=" + fractalPointCount + "\n");
							totalPointCount+=fractalPointCount;
							totalServentCount++;
						}
						if(version != 0) {
							result.append("totalPointsCount=" + totalPointCount + ", totalServentCount=" + totalServentCount + "\n");
						}
					}
					
					AppConfig.timestampedStandardPrint(result.toString());
				}
				// Ako ja nisam krajnji primalac poruke, onda je samo prosledjujem
				else if(AppConfig.myServentInfo.getId() != finalReciverId) {
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);

					TellStatusMessage refactorTellStatusMessage = new TellStatusMessage(tellStatusMessage.getSenderIpAddress(), tellStatusMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciverId, resultMap, version);
					MessageUtil.sendMessage(refactorTellStatusMessage);
				}
			}
			else {
				AppConfig.timestampedStandardPrint("StatusTellHandler: got message that is not type of TELL_STATUS: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
}
