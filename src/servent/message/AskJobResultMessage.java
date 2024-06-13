package servent.message;

import java.util.ArrayList;
import java.util.List;

import app.Point;

public class AskJobResultMessage extends BasicMessage {
	
	private static final long serialVersionUID = 4997260351603311165L;
	
	private String jobName;
	private Integer lastServentId;
	private List<Point> computedPoints;
	private Integer finalReciverId;
	
	public AskJobResultMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, String jobName, int lastServentId, int finalReciverId, List<Point> computedPoints) {
		super(MessageType.ASK_JOB_RESULT, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
		this.lastServentId = lastServentId;
		this.computedPoints = computedPoints;
		this.finalReciverId = finalReciverId;
	}
	
	public AskJobResultMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, String jobName, int lastServentId, int finalReceiverId) {
		super(MessageType.ASK_JOB_RESULT, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
		this.lastServentId = lastServentId;
		this.finalReciverId = finalReceiverId;
		this.computedPoints = new ArrayList<>();
	}

	public String getJobName() {
		return jobName;
	}

	public Integer getLastServentId() {
		return lastServentId;
	}

	public List<Point> getComputedPoints() {
		return computedPoints;
	}

	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
}
