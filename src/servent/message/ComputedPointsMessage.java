package servent.message;

import java.util.List;

import app.Point;

public class ComputedPointsMessage extends BasicMessage {

	private static final long serialVersionUID = -1566695594474598124L;
	
	private final String jobName;
    private final String fractalId;
    private final List<Point> computedPoints;
    private final int finalReceiverId;
	
	public ComputedPointsMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, String jobName, String fractalId, List<Point> computedPoints, int finalReciverId) {
		super(MessageType.COMPUTED_POINTS, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
        this.fractalId = fractalId;
        this.computedPoints = computedPoints;
        this.finalReceiverId = finalReciverId;
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

	public int getFinalReceiverId() {
		return finalReceiverId;
	}
	
}
