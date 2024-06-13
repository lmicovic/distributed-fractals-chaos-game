package servent.message;

public class ReleaseCriticalSectionMessage extends BasicMessage {

	private static final long serialVersionUID = 8644660629114872019L;
	
	private final Integer finalReciverId;
	
	public ReleaseCriticalSectionMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId) {
		super(MessageType.RELEASE_CRITICAL_SECTION, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.finalReciverId = finalReciverId;
	}
	
	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
}
