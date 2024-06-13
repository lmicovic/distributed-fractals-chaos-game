package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.ReleaseCriticalSectionMessage;
import servent.message.util.MessageUtil;

public class ReleaseCriticalSectionHandler implements MessageHandler {
	
	private Message clientMessage;
	private ReleaseCriticalSectionMessage releaseCriticalSectionMessage;
	
	public ReleaseCriticalSectionHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
		this.releaseCriticalSectionMessage = (ReleaseCriticalSectionMessage) clientMessage;
	}
	
	@Override
	public void run() {

		try {
				
			if(clientMessage.getMessageType() == MessageType.RELEASE_CRITICAL_SECTION) {
				
				// Ako mi nismo krajnji primalac poruke, onda je samo prosledimo dalje
				if(AppConfig.myServentInfo.getId() != releaseCriticalSectionMessage.getFinalReciverId()) {
					this.forwardMessage();
					return;
				}
				
				// Ako smo mi krajnji primalac poruke, oslobadjamo kriticnu sekciju
				if(AppConfig.MUTEX_ENABLED) {
					AppConfig.lamportMutex.releaseMyCriticalSecntion();
				}
				
			}
			else {
				AppConfig.timestampedErrorPrint("ReleaseCriticalSectionHandler: got message that is not type RELEASE_CRITICAL_SECTION: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
	// Prosledjuje nasem sledecem sledbenuku ReleaseCriticalSectionMessage
	private void forwardMessage() {
		
		int finalReciverId = releaseCriticalSectionMessage.getFinalReciverId();
		ServentInfo nextNode = AppConfig.chordState.getNextNodeForServentId(releaseCriticalSectionMessage.getFinalReciverId());
		
		// Refaktorisemo
		ReleaseCriticalSectionMessage refactorReleaseCriticalSectionMessage = new ReleaseCriticalSectionMessage(releaseCriticalSectionMessage.getSenderIpAddress(), releaseCriticalSectionMessage.getSenderPort(), nextNode.getIpAddress(), nextNode.getListenerPort(), finalReciverId);		
		MessageUtil.sendMessage(refactorReleaseCriticalSectionMessage);
		
	}
	
	
	
	
}
