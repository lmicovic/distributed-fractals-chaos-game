package servent.message;

public class AckJobExecutionMessage extends BasicMessage {

	private static final long serialVersionUID = -4721296221476120953L;
	
	private Integer finalReciverId;
	
	public AckJobExecutionMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId) {
		super(MessageType.ACK_JOB_EXECUTION, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.finalReciverId = finalReciverId;
	}
	
	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
}
