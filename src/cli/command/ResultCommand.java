package cli.command;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.AskJobFractalIdResultMessage;
import servent.message.AskJobResultMessage;
import servent.message.util.MessageUtil;

public class ResultCommand implements CLICommand {

	@Override
	public String commandName() {
		return "result";
	}
	
	@Override
	public void execute(String args) {
		
		try {
			
			//----------------------------------------------------------------
			// Check parameters
			//----------------------------------------------------------------
			String jobName = null;
			String fractalId = null;
			
			List<String> argsList = checkParameters(args);
			if(argsList == null) {
				return;
			}
			
			if(argsList.size() >= 1) {
				jobName = argsList.get(0);
			}
			if(argsList.size() == 2) {
				fractalId = argsList.get(1);
			}
			
			
			
			
			//----------------------------------------------------------------
			// JobResult
			//----------------------------------------------------------------
			if(fractalId == null) {
			
				AppConfig.timestampedStandardPrint("Collectiong result for job: " + jobName);
				
				int firstServentId = AppConfig.chordState.getFirstIdForJob(jobName);
				int lastServentId = AppConfig.chordState.getLastIdForJob(jobName);
				
				ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(firstServentId);				
				
				if(AppConfig.MUTEX_ENABLED) {
					AppConfig.lamportMutex.acquireLock();
				}
				
				// to do: AskJobResultMessage handler
				AskJobResultMessage askJobResultMessage = new AskJobResultMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), jobName, lastServentId, firstServentId);
				MessageUtil.sendMessage(askJobResultMessage);
				
			}
			
			//----------------------------------------------------------------
			// FractalId result
			//----------------------------------------------------------------
			else {
				
				int reciverId = AppConfig.chordState.getIdForFractalIDAndJob(jobName, fractalId);
				ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(reciverId);
				
				AppConfig.lamportMutex.acquireLock();
				
				// to do: AskJobFractalIdResultMessage handler
				AskJobFractalIdResultMessage askJobFractalIdResultMessage = new AskJobFractalIdResultMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), jobName, reciverId);
				MessageUtil.sendMessage(askJobFractalIdResultMessage);
				
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
	}
	
	// List: [0] - jobName, [1] - fractalId
	private List<String> checkParameters(String args) {
		
		String[] argsList = args.split(" ");
		
		if(argsList.length < 1) {
			AppConfig.timestampedErrorPrint("ResultCommand: enter paramters.");
			return null;
		}
		
		if(argsList.length > 2) {
			AppConfig.timestampedErrorPrint("ResultCommand: wrong parameters.");
			return null;
		}
		
		String jobName = null;
		String fractalId = null;
		
		Job job = null;
		if(args.length() >= 1) {
			jobName = argsList[0];
			job = new Job(jobName, 0, 0, 0, 0, null);
		}
		
		if(argsList.length >= 2) {
			fractalId = argsList[1];
		}
		
//		if(!AppConfig.chordState.getServentJobs().containsKey(job)) {
//			AppConfig.timestampedErrorPrint("Unknown job: " + jobName);
//			return null;
//		}
		
		if(!AppConfig.chordState.getActiveJobsList().contains(job)) {
			AppConfig.timestampedErrorPrint("Job with name " + jobName + " is not active, please start job...");
			return null;
		}
		
		List<String> result = new ArrayList<String>();
		if(jobName != null) {
			result.add(jobName);
		}
		if(fractalId != null) {
			result.add(fractalId);
		}
		
		return result;
	}
	
	
}
