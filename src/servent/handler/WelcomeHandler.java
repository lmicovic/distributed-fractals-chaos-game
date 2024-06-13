package servent.handler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import app.AppConfig;
import app.Job;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class WelcomeHandler implements MessageHandler {

	private Message clientMessage;
	
	public WelcomeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	
	// WelcomeMessage prima servent koji zeli da se poveze na sistem od cvora koji je prvobitno kontaktirao.
	// U WelcomeMessage se nalazi newId cvora koji zeli da se poveze na sistem i Ip i port prvog cvora u sistemu.
	@Override
	public void run() {
		
		try {
			
			if (clientMessage.getMessageType() == MessageType.WELCOME) {
			
				WelcomeMessage welcomeMessage = (WelcomeMessage) clientMessage;
				
				int myId = welcomeMessage.getId();
				AppConfig.myServentInfo.setId(myId);
				AppConfig.chordState.init(welcomeMessage);
				
				// Ovde ispisujemo da je poceo sa radom servent zato sto ako servent nije prvi u sistemu onda tek ovde dobije svoj serventId koji nam treba da bi odredili u koji output file se upisuje output
				this.printServentStart(myId);
				
				String nextNodeIpAddress = AppConfig.chordState.getNextNodeIpAddress();
				int nextNodePort = AppConfig.chordState.getNextNodePort();
				
				Map<Integer, ServentInfo> serventsMap = new HashMap<Integer, ServentInfo>(AppConfig.chordState.getAllNodeIdInfoMap());
				UpdateMessage updateMessage = new UpdateMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNodeIpAddress, nextNodePort, serventsMap);
				
				MessageUtil.sendMessage(updateMessage);
				
				
			} else {
				AppConfig.timestampedErrorPrint("Welcome handler got a message that is not WELCOME");
			}
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}

	}
	
	private void printServentStart(int myServentId) {
		
		try {
			
			// Postavljamo da nam print bude u file umesto na konzolu
//			PrintStream outPrintStream = new PrintStream(new FileOutputStream("chord/output/servent" + myServentId + "_out.txt"));
//			System.setOut(outPrintStream);
			
			AppConfig.timestampedStandardPrint("Starting servent " + AppConfig.myServentInfo);
			String log = "Servent jobs: ";
			for (Job serventJob : AppConfig.myServentInfo.getJobs()) {
				log += serventJob + ",";
			}
			
			log = log.substring(0, log.length()-2);
			AppConfig.timestampedStandardPrint(log);
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("ServentInitializer - file not found: chord/output/servent/" + myServentId + "_out.txt ");
			AppConfig.timestampedErrorPrint(e.getMessage());
			System.exit(0);
		}
		
	}
	
	
	

}
