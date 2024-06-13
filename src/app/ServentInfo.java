package app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an immutable class that holds all the information for a servent.
 *
 * @author bmilojkovic
 */
public class ServentInfo implements Serializable {

	private static final long serialVersionUID = 5304170042791281555L;
	
	private int id;
	private final int chordId;
	
	private final String ipAddress;
	private final int listenerPort;
	private final int fifoListenerPort;
	
	private List<Job> jobs;

	
	public ServentInfo(String ipAddress, int listenerPort, int fifoListenerPort) {
		
		this.id = -1;
		this.chordId = ChordState.chordHash(listenerPort);
		
		this.ipAddress = ipAddress;
		this.listenerPort = listenerPort;
		this.fifoListenerPort = fifoListenerPort;
		this.jobs = new ArrayList<Job>();
	}
	
	
	
	public String getIpAddress() {
		return ipAddress;
	}

	public int getListenerPort() {
		return listenerPort;
	}

	public int getFifoListenerPort() {
		return fifoListenerPort;
	}
	
	public List<Job> getJobs() {
		return jobs;
	}
	
	public void addJob(Job job) {
		if(!jobs.contains(job)) {
			jobs.add(job);
		}
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getChordId() {
		return chordId;
	}
	
	public Job getJobByName(String jobName) {
		for (Job job : jobs) {
			if(job.getName().equals(jobName)) {
				return job;
			}
		}
		return null;
	}
	
	
	
	
	@Override
	public boolean equals(Object obj) {
		
		if(!(obj instanceof ServentInfo)) {
			return false;
		}
		
		ServentInfo other = (ServentInfo)obj;
		
		if(this.id == other.getId() && this.ipAddress.equals(other.getIpAddress()) && this.listenerPort == other.getListenerPort()) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "[" + id + "|" + ipAddress + "|" + listenerPort + ":"+ fifoListenerPort + "]";
	}

}
