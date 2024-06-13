package servent.message;

import java.util.List;

import app.Point;

public class JobResultMessage extends BasicMessage {
	
	private static final long serialVersionUID = -1293125898030603868L;
	
	private String jobName;
	private Integer finalReciverId;
	private List<Point> computedPoints;
	private Integer width;
	private Integer height;
	private Double proportion;
	
	public JobResultMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId, String jobName, List<Point> computedPoints, int width, int height, double proportion) {
		super(MessageType.JOB_RESULT, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
		this.finalReciverId = finalReciverId;
		this.computedPoints = computedPoints;
		this.width = width;
		this.height = height;
		this.proportion = proportion;
	}

	public String getJobName() {
		return jobName;
	}

	public Integer getFinalReciverId() {
		return finalReciverId;
	}

	public List<Point> getComputedPoints() {
		return computedPoints;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getHeight() {
		return height;
	}

	public Double getProportion() {
		return proportion;
	}
	
}
