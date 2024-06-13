package servent.message;

import java.util.List;
import java.util.Map;

import app.FractalJob;
import app.Job;
import app.Point;
import app.ScheduleType;


public class JobExecutionMessage extends BasicMessage{

	
	private static final long serialVersionUID = -1716617355272047462L;
	
	private final Job job;
	private final List<String> jobFractalIds;
	private final List<Point> startPoints;
	private final Map<Integer, FractalJob> serventJobs;
	private final int level;
	private final int finalReciverId;
	private final Map<FractalJob, FractalJob> mappedFractalsJobs;
	private final ScheduleType scheduleType;
	private final int jobSchedulerId;			// Id serventa koji je rasporedio posao
	
	public JobExecutionMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
            List<String> jobFractalIds, List<Point> startPoints, Job job,
            Map<Integer, FractalJob> serventJobs, int level, int finalReceiverId,
            Map<FractalJob, FractalJob> mappedFractalsJobs, ScheduleType scheduleType,
            int jobSchedulerId) {

			super(MessageType.JOB_EXECUTION, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
			this.jobFractalIds = jobFractalIds;
			this.startPoints = startPoints;
			this.job = job;
			this.serventJobs = serventJobs;
			this.level = level;
			this.finalReciverId = finalReceiverId;
			this.mappedFractalsJobs = mappedFractalsJobs;
			this.scheduleType = scheduleType;
			this.jobSchedulerId = jobSchedulerId;
			
}

	public Job getJob() {
		return job;
	}

	public List<String> getJobFractalIds() {
		return jobFractalIds;
	}

	public List<Point> getStartPoints() {
		return startPoints;
	}

	public Map<Integer, FractalJob> getServentJobs() {
		return serventJobs;
	}

	public int getLevel() {
		return level;
	}

	public int getFinalReciverId() {
		return finalReciverId;
	}

	public Map<FractalJob, FractalJob> getMappedFractalsJobs() {
		return mappedFractalsJobs;
	}

	public ScheduleType getScheduleType() {
		return scheduleType;
	}

	public int getJobSchedulerId() {
		return jobSchedulerId;
	}
	
}
