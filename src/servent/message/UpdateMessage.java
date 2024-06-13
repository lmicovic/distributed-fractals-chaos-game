package servent.message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import app.FractalJob;
import app.Job;
import app.ServentInfo;

public class UpdateMessage extends BasicMessage {

	private static final long serialVersionUID = 3586102505319194978L;

	private final Map<Integer, ServentInfo> serventsMap;
	private final Map<Integer, FractalJob> serventJobsMap;
	private final List<Job> activeJobs;
	
	public UpdateMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, Map<Integer, ServentInfo> serventsMap, Map<Integer, FractalJob> serventJobsMap, List<Job> activeJobs) {
		super(MessageType.UPDATE, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.serventsMap = serventsMap;
		this.serventJobsMap = serventJobsMap;
		this.activeJobs = activeJobs;
	}
	
	public UpdateMessage(String senderIpAddress, int sednerPort, String reciverIpAddress, int reciverPort, Map<Integer, ServentInfo> serventsMap) {
		super(MessageType.UPDATE, senderIpAddress, sednerPort, reciverIpAddress, reciverPort);
		this.serventsMap = serventsMap;
		this.serventJobsMap = new HashMap<Integer, FractalJob>();
		this.activeJobs = new ArrayList<Job>();
	}
	
	public Map<Integer, ServentInfo> getServentsMap() {
		return serventsMap;
	}
	
	public Map<Integer, FractalJob> getServentJobsMap() {
		return serventJobsMap;
	}
	
	public List<Job> getActiveJobs() {
		return activeJobs;
	}
	
}
