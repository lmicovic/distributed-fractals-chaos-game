package cli.command;

import app.AppConfig;
import app.Job;

public class JobCommand implements CLICommand {

	@Override
	public String commandName() {
		
		return "jobs";
	}
	
	@Override
	public void execute(String args) {
		
		String log = "My jobs:\n";
		for (Job job : AppConfig.myServentInfo.getJobs()) {
			log += job + "\n";
		}
		
		AppConfig.timestampedStandardPrint(log);
		
	}
	
	
	
}
