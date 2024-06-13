package servent.message;

import java.util.HashMap;
import java.util.Map;

public class AskStatusMessage extends BasicMessage {

	private static final long serialVersionUID = 7450126876447982717L;
	
	private String jobName;
	private String fractalId;
	// Map<Key: jobName, Value: Map<Key: fractalId, Value: pointsCount>>
	private Map<String, Map<String, Integer>> resultMap;
	private int finalReciverId;
	private int version;
	
	public AskStatusMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId, String jobName, String fractalId, Map<String, Map<String, Integer>> resultMap, int version) {
		super(MessageType.ASK_STATUS, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
		this.fractalId = fractalId;
		this.resultMap = resultMap;
		this.finalReciverId = finalReciverId;
		this.version = version;
	}
	
	public AskStatusMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReceiverId, String jobName, String fractalId, int version) {
		super(MessageType.ASK_STATUS, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
		this.fractalId = fractalId;
		this.finalReciverId = finalReceiverId;
		this.version = version;
		this.resultMap = new HashMap<String, Map<String,Integer>>();
	}
	
	public AskStatusMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReceiverId, Map<String, Map<String, Integer>> resultMap, int version) {
		super(MessageType.ASK_STATUS, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.finalReciverId = finalReceiverId;
		this.resultMap = resultMap;
		this.version = version;
		this.jobName = "";
		this.fractalId = "";
	}
	
	public AskStatusMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId, String jobName, int version) {
		super(MessageType.ASK_STATUS, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.finalReciverId = finalReciverId;
		this.jobName = jobName;
		this.version = version;
		this.fractalId = "";
		this.resultMap = new HashMap<String, Map<String,Integer>>();
	}
	
	public AskStatusMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId, String jobName, Map<String, Map<String, Integer>> resultMap, int version) {
		super(MessageType.ASK_STATUS, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.jobName = jobName;
		this.resultMap = resultMap;
		this.finalReciverId = finalReciverId;
		this.version = version;
		this.fractalId = "";
	}
	
	public AskStatusMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int finalReciverId, int version) {
		super(MessageType.ASK_STATUS, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.finalReciverId = finalReciverId;
		this.version = version;
		this.jobName = "";
		this.fractalId = "";
		this.resultMap = new HashMap<String, Map<String,Integer>>();
	}

	public String getJobName() {
		return jobName;
	}

	public String getFractalId() {
		return fractalId;
	}

	public Map<String, Map<String, Integer>> getResultMap() {
		return resultMap;
	}

	public int getFinalReciverId() {
		return finalReciverId;
	}

	public int getVersion() {
		return version;
	}
	
}
