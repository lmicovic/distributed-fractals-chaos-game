package cli.command;

import app.AppConfig;
import app.ServentInfo;
import servent.message.AskStatusMessage;
import servent.message.MessageType;
import servent.message.util.MessageUtil;

public class StatusCommand implements CLICommand {

	@Override
	public String commandName() {
		
		return "status";
	}
	
	@Override
	public void execute(String args) {

		try {
			// status
			// Ako ne navedemo argumente onda prikazujemo stanje izracunavanja svih poslova
			if(args == null || args.isEmpty() || args == "") {
				
				if(AppConfig.MUTEX_ENABLED) {
					AppConfig.lamportMutex.acquireLock();
				}
				
				String nextServentIpAddress = AppConfig.chordState.getNextNodeIpAddress();
				int nextServentPort = AppConfig.chordState.getNextNodePort();
				
				int nextServentId = AppConfig.chordState.getFirstSuccessorId();
				
				
				AskStatusMessage askStatusMessage = new AskStatusMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServentIpAddress, nextServentPort, nextServentId, 2);
				MessageUtil.sendMessage(askStatusMessage);
				return;
			}
			
			String[] argumentList = args.split(" ");
			String jobName = argumentList[0];
			
			// status jobName
			// Ako smo naveli samo jobName, onda trebamo da dohvatimo sve rezultate tog job-a
			if(argumentList.length == 1) {
				int firstServentId = AppConfig.chordState.getFirstIdForJob(jobName);
				ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(firstServentId);
				
				
				
				AppConfig.lamportMutex.acquireLock();
				
				AskStatusMessage askStatusMessage = new AskStatusMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), firstServentId, jobName, 1);
				MessageUtil.sendMessage(askStatusMessage);
			}
			// ???
			else if(argumentList.length == 2) {
				
				String fractalId = argumentList[1];
				int finalReciverId = AppConfig.chordState.getIdForFractalIDAndJob(jobName, fractalId);
				
				ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
				
				AppConfig.lamportMutex.acquireLock();
				
				AskStatusMessage askStatusMessage = new AskStatusMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextServent.getIpAddress(), nextServent.getListenerPort(), finalReciverId, jobName, fractalId, 0);
				MessageUtil.sendMessage(askStatusMessage);
			}
			else {
				AppConfig.timestampedErrorPrint("Status Command[error]: got wrong arguments");;
			}
			
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
	}
	
	
	
}
