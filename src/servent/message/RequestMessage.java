package servent.message;

public class RequestMessage extends BasicMessage {
	
	private static final long serialVersionUID = 6412112105472671331L;
	
	private final Integer finalReciverId;
	
	public RequestMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int revierPort, int finalReciverId, int clock) {
		super(MessageType.REQUEST, senderIpAddress, senderPort, reciverIpAddress, revierPort);
		this.setClock(clock);
		this.finalReciverId = finalReciverId;
	}
	
	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
	@Override
	public boolean isFifo() {
		return true;
	}
	
}
