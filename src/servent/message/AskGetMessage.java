package servent.message;

public class AskGetMessage extends BasicMessage {

	private static final long serialVersionUID = -8558031124520315033L;

	public AskGetMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String text) {
		super(MessageType.ASK_GET, senderIpAddress, senderPort, receiverIpAddress, receiverPort, text);
	}
}
