package servent.message;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	public PutMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int receiverPort, int key, int value) {
		super(MessageType.PUT, senderIpAddress, senderPort, reciverIpAddress, receiverPort, key + ":" + value);
	}
}
