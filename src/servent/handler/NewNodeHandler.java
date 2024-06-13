package servent.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	
	public NewNodeHandler (Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	
	// Dobija newNodeMessage od serventa koji zeli da se poveze na sistem
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
				
				NewNodeMessage newNodeMessage = (NewNodeMessage) clientMessage;
				
				String newNodeIpAddress = newNodeMessage.getSenderIpAddress();
				int newNodePort = newNodeMessage.getSenderPort();
				
				String firstServentInfo = newNodeMessage.getFirstServentIpAddressAndPort();
				int newNodeId = AppConfig.myServentInfo.getId() + 1;		// Id serventa koji zeli da se poveze na sistem
				
				// Zatrazimo mutex sve dok se novi cvor ne poveze na sistem
				if(AppConfig.MUTEX_ENABLED) {
					AppConfig.lamportMutex.acquireLock();
				}
				
				
				// Saljemo serventu koji zeli da se poveze na sistem WelcomeMessage sa njegovim serventId i podacima prvog serventa u sistemu
				WelcomeMessage welcomeMessage = new WelcomeMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), newNodeIpAddress, newNodePort, newNodeId, firstServentInfo);
				MessageUtil.sendMessage(welcomeMessage);
				
			}
			else {
				AppConfig.timestampedErrorPrint("NewNodeMessageHandler got message that is not NEW_NODE: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
			System.out.println(e.getMessage());
		}
		
	}	
	
}
