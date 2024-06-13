package servent.message;

import app.ScheduleType;

public class JobScheduleMessage extends BasicMessage {

	private static final long serialVersionUID = 8917623311938966807L;
	
	private Integer finalReciverId;
	private ScheduleType scheduleType;
	
	public JobScheduleMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId, ScheduleType scheduleType) {
		super(MessageType.JOB_SCHEDULE, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.finalReciverId = finalReciverId;
		this.scheduleType = scheduleType;
	}
	
	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
	public ScheduleType getScheduleType() {
		return scheduleType;
	}
	
}
