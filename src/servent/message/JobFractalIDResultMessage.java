package servent.message;

import java.util.List;

import app.Point;

public class JobFractalIDResultMessage extends BasicMessage {

	private static final long serialVersionUID = 3638714440504369938L;
	
	private String jobName;
	private String fractalId;
	private List<Point> computedPoints;
	private Integer width;
	private Integer height;
	private Double proportion;
	private Integer finalReciverId;
	
	public JobFractalIDResultMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId, String jobName, String fractalId, List<Point> computedPoints, int width, int height, double proportion ) {
		super(MessageType.JOB_FRACTALID_RESULT, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
		this.fractalId = fractalId;
		this.computedPoints = computedPoints;
		this.width = width;
		this.height = height;
		this.proportion = proportion;
		this.finalReciverId = finalReciverId;
	}

	public String getJobName() {
		return jobName;
	}

	public String getFractalId() {
		return fractalId;
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

	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
	
}
