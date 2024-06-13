package servent.message;

import java.util.ArrayList;
import java.util.List;
import app.Point;

public class QuitMessage extends BasicMessage {

	private static final long serialVersionUID = -7303400660742118399L;
	
	private final Integer quitServentId;
	private final String jobName;
	private final String fractalId;
	private final List<Point> quitServentComputedPoints;
	
	public QuitMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int quitServentId, String jobName, String fractalId, List<Point> quitServentComputedPoints) {
		super(MessageType.QUIT, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.quitServentId = quitServentId;
		this.jobName = jobName;
		this.fractalId = fractalId;
		this.quitServentComputedPoints = quitServentComputedPoints;
	}
	
	public QuitMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int quitServentId) {
		super(MessageType.QUIT, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.quitServentId = quitServentId;
		this.jobName = "";
		this.fractalId = "";
		this.quitServentComputedPoints = new ArrayList<Point>();
	}

	public Integer getQuitServentId() {
		return quitServentId;
	}

	public String getJobName() {
		return jobName;
	}

	public String getFractalId() {
		return fractalId;
	}

	public List<Point> getQuitServentComputedPoints() {
		return quitServentComputedPoints;
	}
	
}
