package servent.message;

import java.util.List;
import java.util.Map;

import app.FractalJob;
import app.Job;
import app.ScheduleType;

public class IdleMessage extends BasicMessage {
	
	private static final long serialVersionUID = 3743195518131975367L;
	
	private final Map<Integer, FractalJob> serventJobs;
	private final List<Job> activeJobs;
	private final Map<FractalJob, FractalJob> mappedFractals;
	private final ScheduleType scheduleType;
	private final int jobSchedulerServentId;
	private final int finalReciverId;
	
	public IdleMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort,
					   Map<Integer, FractalJob> serventJobs, int finalReciverId, Map<FractalJob, FractalJob> mappedFractalsJobs, List<Job> activeJobs, ScheduleType scheduleType, int jobSchedulerServentId) {
		super(MessageType.IDLE, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		
		this.serventJobs = serventJobs;
		this.activeJobs = activeJobs;
		this.mappedFractals = mappedFractalsJobs;
		this.scheduleType = scheduleType;
		this.jobSchedulerServentId = jobSchedulerServentId;
		this.finalReciverId = finalReciverId;
		
	}

	public Map<Integer, FractalJob> getServentJobs() {
		return serventJobs;
	}

	public List<Job> getActiveJobs() {
		return activeJobs;
	}

	public Map<FractalJob, FractalJob> getMappedFractals() {
		return mappedFractals;
	}

	public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public int getJobSchedulerServentId() {
		return jobSchedulerServentId;
	}

	public int getFinalReciverId() {
		return finalReciverId;
	}
	
}
