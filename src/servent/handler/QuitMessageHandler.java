package servent.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.FractalJob;
import app.Job;
import app.JobExecution;
import app.Point;
import app.ScheduleType;
import app.ServentInfo;
import app.util.JobSchedule;
import servent.message.ComputedPointsMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.QuitMessage;
import servent.message.util.FifoSendWorker;
import servent.message.util.MessageUtil;

public class QuitMessageHandler implements MessageHandler {

	private Message clientMessage;
	
	public QuitMessageHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		
		try {
		
			if(clientMessage.getMessageType() == MessageType.QUIT) {
				
				QuitMessage quitMessage = (QuitMessage) clientMessage;
				
				int quitServentId = quitMessage.getQuitServentId();
				int myId = AppConfig.myServentInfo.getId();
				String quitJobName = quitMessage.getJobName();
				String quitFractalId = quitMessage.getFractalId();
				List<Point> quitServentComputedPoints = quitMessage.getQuitServentComputedPoints();
				
				// Ovo je kada je jedan cvor u sistemu ili ako je poruka napravila krug
				// Ako sam sebi poslao poruku
				// Ili Ako je samo jedan servent u sistemu
				// Ili ako ne postoji quitServent u listi servenata
				if(myId == quitServentId ||
						!AppConfig.chordState.getAllNodeIdInfoMap().containsKey(quitServentId) || 
						AppConfig.chordState.getAllNodeIdInfoMap().size() == 1) {
					
					AppConfig.timestampedStandardPrint("QuitMessage " + quitMessage + " made a circle...");

					// Ako ima aktivnih poslova u sistemu
					if(AppConfig.chordState.getActiveJobsCount() > 0) {
						
						// Test
						
						int totalServentCount = AppConfig.chordState.getAllNodeIdInfoMap().size();
						Map<FractalJob, FractalJob> mappedFractals = JobSchedule.scheduleJob(totalServentCount, ScheduleType.REMOVE_SERVENT);
						
						// Ako je quitServent imao imao neki posao, saljemo ComputedPoinstMessage sledecem cvoru
						if(!quitJobName.equals("") && !quitFractalId.equals("")) {
							
							FractalJob hisFractalJob = mappedFractals.get(new FractalJob(quitFractalId, quitJobName));
							
							int serventDataReciverId = AppConfig.chordState.getIdForFractalIDAndJob(quitJobName, hisFractalJob.getFractalId());
							ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(serventDataReciverId);
							
							ComputedPointsMessage computedPointsMessage = new ComputedPointsMessage(quitMessage.getSenderIpAddress(), quitMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), quitJobName, quitFractalId, quitServentComputedPoints, serventDataReciverId);
							MessageUtil.sendMessage(computedPointsMessage);
						}
					}
					return;
				}
				
						
				this.setNewMapOfAllServents(myId, quitServentId);
				this.setNewMapOfAllFifoWorkers(quitServentId);
				
				// Ako sam ja jedini cvor u sistemu saljem sebi QuitMessage
				QuitMessage newQuitMessage;
				if(AppConfig.chordState.getAllNodeIdInfoMap().size() == 1) {
					newQuitMessage = new QuitMessage(quitMessage.getSenderIpAddress(), quitMessage.getSenderPort(), AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), quitServentId, quitJobName, quitFractalId, quitServentComputedPoints);	
				}
				// Ako ima vise cvorova u sistemu
				else {
					
					String nextNodeIpAddress = AppConfig.chordState.getNextNodeIpAddress();
					int nextNodePort = AppConfig.chordState.getNextNodePort();
					
					newQuitMessage = new QuitMessage(quitMessage.getSenderIpAddress(), quitMessage.getSenderPort(), nextNodeIpAddress, nextNodePort, quitServentId, quitJobName, quitFractalId, quitServentComputedPoints);
				}
				
				MessageUtil.sendMessage(newQuitMessage);
				
				
			}
			else {
				AppConfig.timestampedErrorPrint("QuitMessageHandler: got message that is not type of QUIT: " + clientMessage.getMessageType());
			}
			
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
	}
	
	private void setNewMapOfAllServents(int myId, int quitServentId) {
		
		Map<Integer, ServentInfo> newServentsMap = new HashMap<Integer, ServentInfo>();
		
		// Uklanjamo serventa iz liste svih servenata
		AppConfig.chordState.getAllNodeIdInfoMap().remove(quitServentId);
		
		for (Entry<Integer, ServentInfo> entity : AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
			
			int currentServentId = entity.getKey();
			ServentInfo currentServent = entity.getValue();
			
			if(currentServentId > quitServentId) {
				currentServent.setId(currentServentId - 1);
				newServentsMap.put(currentServentId - 1, currentServent);
			}
			else {
				newServentsMap.put(currentServentId, currentServent);
			}
			
		}
		
		if(myId > quitServentId) {
			AppConfig.myServentInfo.setId(myId - 1);
		}
		
		AppConfig.chordState.getAllNodeIdInfoMap().clear();
		AppConfig.chordState.addNodes(newServentsMap);
		
		
	}
	
	
	private void setNewMapOfAllFifoWorkers(int quitServentId) {
		
		Map<Integer, FifoSendWorker> newFifoWorkersMap = new HashMap<Integer, FifoSendWorker>();
		
		if(AppConfig.chordState.getFifoSendWorkerMap().containsKey(quitServentId)) {
			FifoSendWorker quiterFifoSendWorker = AppConfig.chordState.getFifoSendWorkerMap().get(quitServentId);
			quiterFifoSendWorker.stop();
			AppConfig.chordState.getFifoSendWorkerMap().remove(quitServentId);
		}
		
		for (Entry<Integer, FifoSendWorker> entity : AppConfig.chordState.getFifoSendWorkerMap().entrySet()) {
			
			int currentServentId = entity.getKey();
			FifoSendWorker currentServentFifoSendWorker = entity.getValue();
			
			if(currentServentId > quitServentId) {
				currentServentFifoSendWorker.setServentId(currentServentId - 1);
				newFifoWorkersMap.put(currentServentId - 1, currentServentFifoSendWorker);
			}
			else {
				newFifoWorkersMap.put(currentServentId, currentServentFifoSendWorker);
			}
			
		}
		AppConfig.chordState.setFifoSendWorkerMap(newFifoWorkersMap);
	}
	
}
