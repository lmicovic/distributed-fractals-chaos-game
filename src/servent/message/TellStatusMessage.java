package servent.message;

import java.util.Map;

public class TellStatusMessage extends BasicMessage {

	private static final long serialVersionUID = 4695631604979203057L;
	
	// Map<Key: jobName, Value: Map<Key: fractalId, Value: pointsCount>>
	private Map<String, Map<String, Integer>> resultMap;
	private Integer finalReciverId;
	private Integer version;
	
	public TellStatusMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int revierPort, int finalReciverId, Map<String, Map<String, Integer>> resultMap, int version) {
		super(MessageType.TELL_STATUS, senderIpAddress, senderPort, reciverIpAddress, revierPort);
		this.resultMap = resultMap;
		this.finalReciverId = finalReciverId;
		this.version = version;
	}
	
	public Map<String, Map<String, Integer>> getResultMap() {
		return resultMap;
	}
	
	public Integer getFinalReciverId() {
		return finalReciverId;
	}
	
	public Integer getVersion() {
		return version;
	}
	
}
