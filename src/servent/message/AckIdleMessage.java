package servent.message;

public class AckIdleMessage extends BasicMessage {

	private static final long serialVersionUID = 7487818833358353850L;
	private Integer finalReciverId;
	
	public AckIdleMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId) {
		super(MessageType.ACK_IDLE, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.finalReciverId = finalReciverId;
	}
	
	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
}
