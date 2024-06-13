package servent.message;

public class ReleaseMessage extends BasicMessage {

	private static final long serialVersionUID = -8407573550410644455L;
	
	private Integer finalReciverId;
	
	public ReleaseMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int clock, int finalReciverId) {
		super(MessageType.RELEASE, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		super.setClock(clock);
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
