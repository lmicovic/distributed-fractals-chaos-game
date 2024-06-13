package servent.message;

public class ReplyMessage extends BasicMessage {

	private static final long serialVersionUID = -1697429348804543314L;

	private Integer finalReciverId;
	
	public ReplyMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int revierPort, int finalReciverId, int clock) {
		super(MessageType.REPLY, senderIpAddress, senderPort, reciverIpAddress, revierPort);
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
