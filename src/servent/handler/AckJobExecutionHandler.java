package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AckJobExecutionMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class AckJobExecutionHandler implements MessageHandler {
	
	private Message clientMessage;
	
	public AckJobExecutionHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {

		try {
			if(clientMessage.getMessageType() == MessageType.ACK_JOB_EXECUTION) {
				
				AckJobExecutionMessage ackJobExecutionMessage = (AckJobExecutionMessage) clientMessage;
				
				// Ako sam ja krajnji primalac poruke, zabelizemo da smo primili Ack poruku
				if(AppConfig.myServentInfo.getId() == ackJobExecutionMessage.getFinalReciverId()) {
					int senderId = AppConfig.chordState.getServentIdByServentPortAndIpAddress(ackJobExecutionMessage.getSenderPort(), ackJobExecutionMessage.getSenderIpAddress());
					AppConfig.timestampedStandardPrint("AckJobExecutionHandler: ack message recived from " + senderId);
					AppConfig.chordState.getReceivedAckMessagesCount().getAndIncrement();
				}
				// Ako nismo mi krajnji primalac poruke onda je prosledjujemo
				else {
					this.forwardMessage(ackJobExecutionMessage);
				}
			}
			else {
				AppConfig.timestampedErrorPrint("AckJobExecutionHandler: got message that is not type of ACK_JOB_EXECUTION: " + clientMessage.getMessageType());
			}
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}

	private void forwardMessage(AckJobExecutionMessage ackJobExecutionMessage) {
		
		ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(ackJobExecutionMessage.getFinalReciverId());
		
		AckJobExecutionMessage newAckJobExecutionMessage = new AckJobExecutionMessage(ackJobExecutionMessage.getSenderIpAddress(), ackJobExecutionMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), ackJobExecutionMessage.getFinalReciverId());
		MessageUtil.sendMessage(newAckJobExecutionMessage);
	}
	
}
