package servent.handler;

import java.util.HashMap;
import java.util.Map;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskStatusMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellStatusMessage;
import servent.message.util.MessageUtil;

public class AskStatusHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public AskStatusHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.ASK_STATUS) {
				
				AskStatusMessage askStatusMessage = (AskStatusMessage) clientMessage;
				
				int finalReciverId = askStatusMessage.getFinalReciverId();
				String jobName = askStatusMessage.getJobName();
				String fractalId = askStatusMessage.getFractalId();
				int version = askStatusMessage.getVersion();
				
				String myFractalId = "";
				int myPointCount = 0;
				if(AppConfig.chordState.getExecutionJob() != null) {
					myFractalId = AppConfig.chordState.getExecutionJob().getFractalId();
					myPointCount = AppConfig.chordState.getExecutionJob().getComputedPoints().size();
				}
				
				// Ako ja nisam krajnji primalac AskStatusMessage poruke, prosledjujemo poruku
				if(AppConfig.myServentInfo.getId() != finalReciverId) {
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
					
					AskStatusMessage newAskStatusMessage = new AskStatusMessage(askStatusMessage.getSenderIpAddress(), askStatusMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciverId, jobName, fractalId, askStatusMessage.getResultMap(), version);
					MessageUtil.sendMessage(newAskStatusMessage);					
					return;
				}
				//-------------------------------------------------------------------------------------------------------

				// Ako sam ja krajnji primalac poruke

				// Ako je version = 0, onda saljemo nazad rezultat za posao i njegov fractalId
				if(version == 0) {
					
					// Map<Key: jobName, Value: Map<Key: fractalId, Value: pointsCount>>
					Map<String, Map<String, Integer>> resultMap = new HashMap<String, Map<String,Integer>>();
					
					String myJobName = AppConfig.chordState.getExecutionJob().getJobName();
					resultMap.put(myJobName, new HashMap<String, Integer>());
					resultMap.get(jobName).put(myFractalId, myPointCount);
					
					int finalReciver = AppConfig.chordState.getServentIdByServentPortAndIpAddress(askStatusMessage.getSenderPort(), askStatusMessage.getSenderIpAddress());
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciver);
					
					TellStatusMessage tellStatusMessage = new TellStatusMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciver, resultMap, 0);
					MessageUtil.sendMessage(tellStatusMessage);
				}
				else if(version == 1) {
					
					int lastServentId = AppConfig.chordState.getLastIdForJob(jobName);
					
					// Dodajemo moj info u resultMap
					// Map<Key: jobName, Value: Map<Key: fractalId, Value: pointsCount>>
					Map<String, Map<String, Integer>> resultMap = askStatusMessage.getResultMap();
					
					AppConfig.timestampedStandardPrint("--------------------------");
					AppConfig.timestampedStandardPrint("JobName: " + jobName);
					AppConfig.timestampedStandardPrint("MyFractalId: " + myFractalId);
					AppConfig.timestampedStandardPrint("MyPointCount: " + myPointCount);
					AppConfig.timestampedStandardPrint("--------------------------");
					
					resultMap.putIfAbsent(jobName, new HashMap<String, Integer>());
					resultMap.get(jobName).put(myFractalId, myPointCount);
					
					if(AppConfig.myServentInfo.getId() == lastServentId) {
						int finalReciver = AppConfig.chordState.getServentIdByServentPortAndIpAddress(askStatusMessage.getSenderPort(), askStatusMessage.getSenderIpAddress());
						ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciver);
						
						TellStatusMessage tellStatusMessage = new TellStatusMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciver, resultMap, 1);
						MessageUtil.sendMessage(tellStatusMessage);
					}
					else {
						String nextServentIpAddress = AppConfig.chordState.getNextNodeIpAddress();
						int nextServentPort = AppConfig.chordState.getNextNodePort();
						
						int myFirstSuccessorId = AppConfig.chordState.getFirstSuccessorId();
												
						AskStatusMessage newAskStatusMessage = new AskStatusMessage(askStatusMessage.getSenderIpAddress(), askStatusMessage.getSenderPort(), nextServentIpAddress, nextServentPort, myFirstSuccessorId, jobName, resultMap, 1);
						MessageUtil.sendMessage(newAskStatusMessage);
					}
				}
				else {	
					Map<String, Map<String, Integer>> resultMap = askStatusMessage.getResultMap();
					// Ako trenutni cvor izvrsava neki job
					if(AppConfig.chordState.getExecutionJob() != null) {
						// Dodajemo moj rezultat ako ga izvrsavamo
						String myJobName = AppConfig.chordState.getExecutionJob().getJobName();
						resultMap.putIfAbsent(myJobName, new HashMap<String, Integer>());
						resultMap.get(myJobName).put(myFractalId, myPointCount);
					}
					
					// Ako sam ja poslao prvobidtno AskStatusMessage poruku, znaci da je poruka napravila krug
					// Odna saljemo rezultat sebi
					if(AppConfig.myServentInfo.getIpAddress().equals(askStatusMessage.getSenderIpAddress()) && 
							AppConfig.myServentInfo.getListenerPort() == askStatusMessage.getSenderPort()) {
						
						TellStatusMessage tellStatusMessage = new TellStatusMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getId(), resultMap, version);
						MessageUtil.sendMessage(tellStatusMessage);
						
					}
					// Ako trenutni node nije poslao AskStatusMessage onda prosledjuje poruku njegovom prvom sledbeniku
					else {
						
						// Forward AskStatusMessage
						String nextServentIpAddress = AppConfig.chordState.getNextNodeIpAddress();
						int nextServentPort = AppConfig.chordState.getNextNodePort();

						int nextServentId = AppConfig.chordState.getFirstSuccessorId();
					
						
						AskStatusMessage refactorAskStatusMessage = new AskStatusMessage(askStatusMessage.getSenderIpAddress(), askStatusMessage.getSenderPort(), nextServentIpAddress, nextServentPort, nextServentId, resultMap, version);
						MessageUtil.sendMessage(refactorAskStatusMessage);
					}
					
				}
					
				
			}
			else {
				AppConfig.timestampedErrorPrint("AskStatusHandler: got messsage type that is not type of ASK_STATUS: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
}
