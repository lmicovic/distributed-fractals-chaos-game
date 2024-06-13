package cli.command;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.StopJobMessage;
import servent.message.util.MessageUtil;

public class StopCommand implements CLICommand {

	@Override
	public String commandName() {

		return "stop";
	}
	
	
	@Override
	public void execute(String args) {

		try {
			
			if(AppConfig.MUTEX_ENABLED) {
				AppConfig.lamportMutex.acquireLock();
			}
			
			// Ako nismo jedini u sistemu, posalti prvom sledbeniku da se zaustavi posao
			if(AppConfig.chordState.getAllNodeIdInfoMap().size() > 1) {
				
				String nextNodeIp = AppConfig.chordState.getNextNodeIpAddress();
				int nextNodePort = AppConfig.chordState.getNextNodePort();
				
				String jobName = args;
				
				StopJobMessage stopJobMessage = new StopJobMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNodeIp, nextNodePort, jobName);
				MessageUtil.sendMessage(stopJobMessage);
				
			}
			// Ako sam jedini servent u sistemu, onda saljemo poruku samo sebi
			else {
				String jobName = args;
				
				StopJobMessage stopJobMessage = new StopJobMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), jobName);
				MessageUtil.sendMessage(stopJobMessage);	
			}
			
			
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		
	}
}
