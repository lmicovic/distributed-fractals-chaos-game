package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.ReleaseMessage;
import servent.message.ReplyMessage;
import servent.message.RequestMessage;
import servent.message.util.MessageUtil;

// Handler za sve fifo poruke
public class FifoHandler {
	
	// Handler za RequestMessage poruke
	// Saljemo odgovor replyMessage na dobijenu requestMessage poruku
	// requestMessage se dobija kada neki cvor zatrazi LamportMutex
	public static void handleRequestMessage(RequestMessage requestMessage) {
		
		// Zabelezimo event
		AppConfig.lamportClock.messageEvent(requestMessage.getClock());
		
		// Dodajemo requestMessage u listu
		AppConfig.lamportMutex.addMessageRequest(requestMessage);
		AppConfig.lamportClock.localEvent();						// Zabelezimo dogadjaj, povecamo clock
		
		// Saljemo replyMessage
		int reciverId = AppConfig.chordState.getServentIdByFifoPortAndIpAddress(requestMessage.getSenderPort(), requestMessage.getSenderIpAddress());
		ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
		
		ReplyMessage replyMessage = new ReplyMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getFifoListenerPort(), nextServent.getIpAddress(), nextServent.getFifoListenerPort(), reciverId, AppConfig.lamportClock.getClock());
		MessageUtil.sendMessage(replyMessage);
			
	}
	
	// Zabelezimo messege event i zabelezimo da sbo dobili replreMessage
	public static void handleReplyMessage(ReplyMessage replyMessage) {
		
		// Zabelezimo event
		AppConfig.lamportClock.messageEvent(replyMessage.getClock());
		
		// Zabelezimo da sam dobio replyMessage
		AppConfig.lamportMutex.replyMessageEvent(replyMessage);
		
	}
	
	// Zabelezimo messageEvent i uklonimo sebe iz requestsMessageList
	public static void handleReleaseMessage(ReleaseMessage releaseMessage) {
		
		AppConfig.timestampedStandardPrint("Handle release message: " + releaseMessage);
		
		AppConfig.lamportClock.messageEvent(releaseMessage.getClock());
		AppConfig.lamportMutex.releaseMessageEvent(releaseMessage); 		// uklanja nas iz requestMessageList
		
		
	}
	
	
	
}
