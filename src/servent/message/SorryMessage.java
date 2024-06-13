package servent.message;

import app.ServentInfo;

public class SorryMessage extends BasicMessage {

	private static final long serialVersionUID = 8866336621366084210L;

	public SorryMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, String jobName) {
		super(MessageType.SORRY, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
	}
	
	
}
