package servent.message;

public class AskJobFractalIdResultMessage extends BasicMessage {
	
	private static final long serialVersionUID = 6060297728169081980L;
	
	private String jobName;
	private Integer finalReciverId;
	
	public AskJobFractalIdResultMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, String jobName, int finalReciverId) {
		super(MessageType.ASK_JOB_FRACTALID_RESULT, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
		this.finalReciverId = finalReciverId;
	}
	
	public String getJobName() {
		return jobName;
	}
	
	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
}
