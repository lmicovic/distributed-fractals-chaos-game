package app.lamport_mutex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import app.AppConfig;
import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.Message;
import servent.message.ReleaseMessage;
import servent.message.RequestMessage;
import servent.message.util.MessageUtil;

public class LamportMutex {
	
	private final ServentInfo servent;				
	private List<BasicMessage> messageRequestList;	// lista serventata koji su zatrazili kriticnu sekciju
	private List<ServentInfo> pendingReplies;		// ovde se nalaze odgovori kada servent zatrazi kriticnu sekciju
	private boolean requestMade;					// Da li je zatrazena kriticna sekcija
	
	public LamportMutex(ServentInfo servent) {
		this.servent = servent;
		messageRequestList = Collections.synchronizedList(new ArrayList<>() { 	// Redefinisemo default ponasalje liste da doda poruku na kraj liste, i onda sortiramo listu
			
			@Override
			public synchronized boolean add(BasicMessage message) {
				boolean retValue = super.add(message);
				Collections.sort(messageRequestList);
				return retValue;
			}
			
		});
		
		this.pendingReplies = new CopyOnWriteArrayList<ServentInfo>();
		this.requestMade = false;
		
	}
	
	public synchronized void addMessageRequest(BasicMessage message) {
		this.messageRequestList.add(message);
	}

	// Uklanja prosledjenu poruku iz messageRequestListe
	public synchronized void releaseMessageEvent(Message message) {
		String senderIpAddress = message.getSenderIpAddress();
		int senderPort = message.getSenderPort();
		
		// Ako je messateRequestLista prazna onda se vratimo posto nema sta da se ukloni
		if(messageRequestList.isEmpty()) {
			return;
		}
		
		// Ako messagteRequestList nije prazna, i ako su isti ipAddress i portovi na prvoj poziciji liste onda uklanjamo poruku
		if(messageRequestList.get(0).getSenderIpAddress().equals(senderIpAddress) && messageRequestList.get(0).getSenderPort() == senderPort) {
			messageRequestList.remove(0);
		}
	}
	
	// true: onda node moze da pristupi kristicnoj sekciji
	// false: ne moze da pristupi kriticnoj sekciji, node se blokira sve dok ne moze da pristupi kriticnoj sekciji
	public synchronized boolean requestCriticalSection() {
		
		// test
		
		// Ako nije vec zatrazena kriticna sekcija
		if(requestMade == false) {
			
			requestMade = true;
			
			// Prolazimo kroz sve servente i dodajemo u listu kako bi cekali njihove odgovore
			for (Entry<Integer, ServentInfo> entity: AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
				
				int serventId = entity.getKey();
				ServentInfo servent = entity.getValue();
				
				// Ne dodajemo sebe u listu cekanja za replayMessage
				if (serventId != AppConfig.myServentInfo.getId()) {
                    pendingReplies.add(servent);
                }
            }
			
			AppConfig.lamportClock.localEvent();			// Povecavamo brojac za event

			// Broadcast requestMessage to all servents
			for (ServentInfo reciverServent : pendingReplies) {
				int reciverId = reciverServent.getId();
				ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
				
				RequestMessage requestMessage = new RequestMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getFifoListenerPort(), nextServent.getIpAddress(), nextServent.getFifoListenerPort(), reciverId, AppConfig.lamportClock.getClock());
				MessageUtil.sendMessage(requestMessage);
				
			}
			
			// Dodajemo svoj RequestMessage u nas messageRequestList
			RequestMessage myRequestMessage = new RequestMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getFifoListenerPort(), AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getFifoListenerPort(), AppConfig.lamportClock.getClock(), AppConfig.myServentInfo.getId());
			this.addMessageRequest(myRequestMessage);
				
		}
		
		// Ako sam ja prvi u messageRequestList, dobijam kriticnu sekciju
		if(messageRequestList.get(0).getReceiverIpAddress().equals(servent.getIpAddress()) &&
				messageRequestList.get(0).getSenderPort() == servent.getFifoListenerPort()) {
			return pendingReplies.isEmpty();
		}
		
		// Ako nisam prvi
		return false;

	}
	
	// Poziva se kada je lokalni node zavrsio sa izvrsavanjem kriticne sekcije
	public synchronized void releaseMyCriticalSecntion() {
		
		try {
			
			AppConfig.timestampedStandardPrint("Releaseing my critical section...");
			
			requestMade = false;
			messageRequestList.remove(0);
			
			// Broadcastujemo ReleaseMessage svim serventima
			for (Entry<Integer, ServentInfo> entity : AppConfig.chordState.getAllNodeIdInfoMap().entrySet()) {
				int reciverId = entity.getKey();
				
				// Preskacemo sebe necemo sebi da saljemo poruku
				if(reciverId != AppConfig.myServentInfo.getId()) {
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
					
					ReleaseMessage releaseMessage = new ReleaseMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getFifoListenerPort(), nextServent.getIpAddress(), nextServent.getFifoListenerPort(), AppConfig.lamportClock.getClock(), reciverId);
					MessageUtil.sendMessage(releaseMessage);
					
				}
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}

	}
	
	// Uklanja posiljaoca replyMessage poruke iz liste pendingReplies
	public synchronized void replyMessageEvent(Message message) {
		
		// Ako je zatrazena kriticna sekcija
		if(requestMade == true) {
			
			// Uklanjamo posiljaoca replyMessege iz pendingReplies liste
			for (ServentInfo pendingServent : pendingReplies) {
				
				if(message.getSenderIpAddress().equals(pendingServent.getIpAddress())
						&& message.getSenderPort() == pendingServent.getFifoListenerPort()) {
					pendingReplies.remove(pendingServent);
					break;
				}	
			}
			AppConfig.timestampedStandardPrint("Pending replies: " + pendingReplies);
		}
		// Ako nije zatrazena kriticna sekcija, onda ispisemo samo da smo dobili reqplyMessage, a da nismo zatrazili kriticnu sekciju
		else if(requestMade == false) {
			AppConfig.timestampedErrorPrint("LamportMutex: got reqly message, but didn't request critical secton.");
		}
		
	}
	
	public void acquireLock() {
		
		AppConfig.timestampedStandardPrint("Waiting to get critical section...");
		// Waiting to get Critical section
		while(true) {
			try {
				
				boolean gotCriticalSection = this.requestCriticalSection();
				
//				AppConfig.timestampedStandardPrint("" + gotCriticalSection );
				
				if(gotCriticalSection) {
					// Got critical section
					break;
				}
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		AppConfig.timestampedStandardPrint("Got critical section.");
		
	}
	
	
}
