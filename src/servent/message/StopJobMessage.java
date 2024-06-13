package servent.message;

public class StopJobMessage extends BasicMessage {
	
	private static final long serialVersionUID = -1587779697573815803L;
	
	private final String jobName;
	
	public StopJobMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, String jobName) {
		super(MessageType.STOP_JOB, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
	}
	
	public String getJobName() {
		return jobName;
	}
	
}
