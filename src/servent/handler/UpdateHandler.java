package servent.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.FractalJob;
import app.Job;
import app.ScheduleType;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseCriticalSectionMessage;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;
	private UpdateMessage updateMessage;
	
	private Map<Integer, ServentInfo> allNodesMap;
	private Map<Integer, FractalJob> serventJobsMap;
	private List<Job> activeJobs;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
		
		this.updateMessage = (UpdateMessage) clientMessage;
		this.allNodesMap = updateMessage.getServentsMap();
		this.serventJobsMap = updateMessage.getServentJobsMap();
		this.activeJobs = updateMessage.getActiveJobs();
		
	}
	

	// Ako ima newServent kao suseda, onda samo prepise preko njega, ako ne onda doda
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.UPDATE) {
				
				// Dodamo sebe u listu svih servenata
				allNodesMap.put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
				
				AppConfig.chordState.addNodes(allNodesMap);
				
				// Ako se meni vratila update poruka koju sam ja poslao znaci da je napravila krug
				if(updateMessage.getSenderIpAddress().equals(AppConfig.myServentInfo.getIpAddress()) 
						&& updateMessage.getSenderPort() == AppConfig.myServentInfo.getListenerPort()) {
					AppConfig.timestampedStandardPrint("UpdateMessageHandler: update message mapde a circle.");
					AppConfig.chordState.setServentJobs(serventJobsMap);
					AppConfig.chordState.addNewJobs(activeJobs);
					
					if(AppConfig.MUTEX_ENABLED) {
						this.sendRequestToReleaseCriticalSection();
					}
					
					if(AppConfig.chordState.getActiveJobsCount() > 0) {
						StopJobHandler.sendReschedulingMessage(ScheduleType.ADD_SERVENT);
					}
					return;
				}
				
				// Forward Update message
				// Ako nisam ja posiljalac UpdatePoruke, prosledjujemo Update poruku mom prvom sledecem sledbeniku
				this.forwardUpdateMessage();
				
				
				
				
			} else {
				AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
	}
	
	private void forwardUpdateMessage() {

		allNodesMap = new HashMap<Integer, ServentInfo>(AppConfig.chordState.getAllNodeIdInfoMap());
		serventJobsMap = new HashMap<Integer, FractalJob>(AppConfig.chordState.getServentJobs());
		activeJobs = new ArrayList<Job>(AppConfig.chordState.getActiveJobsList());
		
		
		String nextNodeIpAddress = AppConfig.chordState.getNextNodeIpAddress();
		int nextNodePort = AppConfig.chordState.getNextNodePort();
		
		UpdateMessage refactorUpdateMessage = new UpdateMessage(updateMessage.getSenderIpAddress(), updateMessage.getSenderPort(), nextNodeIpAddress, nextNodePort, allNodesMap, serventJobsMap, activeJobs);
		MessageUtil.sendMessage(refactorUpdateMessage);
		
	}
	
	// Saljemo poruku za oslobadjanie kriticne sekcije
	private void sendRequestToReleaseCriticalSection() {
		
		int reciverId = AppConfig.myServentInfo.getId() - 1;
		ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
		
		ReleaseCriticalSectionMessage releaseMessage = new ReleaseCriticalSectionMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), reciverId);
		MessageUtil.sendMessage(releaseMessage);
		
	}
	
}
