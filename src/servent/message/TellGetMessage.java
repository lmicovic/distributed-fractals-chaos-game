package servent.message;

public class TellGetMessage extends BasicMessage {

	private static final long serialVersionUID = -6213394344524749872L;

	public TellGetMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, String jobName) {
		super(MessageType.TELL_GET, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
	}
}
