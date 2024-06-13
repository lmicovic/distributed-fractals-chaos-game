package app;

import java.io.Serializable;
import java.util.Objects;

public class FractalJob implements Serializable{

	private static final long serialVersionUID = -891791787249647345L;
	
	private String fractalId;
	private String jobName;
	
	public FractalJob(String fractalId, String jobName) {
		this.fractalId = fractalId;
		this.jobName = jobName;
	}
	
	public String getFractalId() {
		return fractalId;
	}
	
	public String getJobName() {
		return jobName;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this == obj) {
			return true;
		}
		
		if(obj == null || getClass() != obj.getClass()) {
			return false;
		}
		
		FractalJob object = (FractalJob) obj;
		return Objects.equals(fractalId, object.getFractalId()) && Objects.equals(jobName, object.getJobName());
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(fractalId, jobName);
	}
	
	@Override
	public String toString() {
		return "[FractalJob|" + fractalId + "|" + jobName;
	}
	
}
