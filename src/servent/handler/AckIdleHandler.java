package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AckIdleMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AckIdleHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public AckIdleHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			
			if(clientMessage.getMessageType() == MessageType.ACK_IDLE) {
				
				
				AckIdleMessage ackIdleMessage = (AckIdleMessage) clientMessage;
				int finalReciverId = ackIdleMessage.getFinalReciverId();
				
				// Ako sam ja krajnji primalac poruke
				if(AppConfig.myServentInfo.getId() == finalReciverId) {
					
					String senderIpAddress = ackIdleMessage.getSenderIpAddress();
					int sendertPort = ackIdleMessage.getSenderPort();
					
					int senderId = AppConfig.chordState.getServentIdByServentPortAndIpAddress(sendertPort, senderIpAddress);
					AppConfig.timestampedStandardPrint("AckIdleMessage recived from: " + senderId);
					AppConfig.chordState.getReceivedAckMessagesCount().getAndIncrement();

				}
				//----------------------------------------------------------------------------------------------------
				// Ako nisam ja krajnji primalac poruke prosledimo poruku
				else {
					ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
					
					AckIdleMessage refactorAckIdleMessage = new AckIdleMessage(ackIdleMessage.getSenderIpAddress(), ackIdleMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciverId);
					MessageUtil.sendMessage(refactorAckIdleMessage);
				}
				
			}
			else {
				AppConfig.timestampedErrorPrint("AckIdleHandler: got message type that is not type of ACK_IDLE: " + clientMessage.getMessageType());
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
	
}
