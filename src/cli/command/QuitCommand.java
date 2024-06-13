package cli.command;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import app.AppConfig;
import app.JobExecution;
import app.Point;
import cli.CLIParser;
import servent.FifoListener;
import servent.SimpleServentListener;
import servent.message.QuitMessage;
import servent.message.util.FifoSendWorker;
import servent.message.util.MessageUtil;

public class QuitCommand implements CLICommand {
	
	private CLIParser cliParser;
	private SimpleServentListener simpleServentListener;
	private FifoListener fifoListener;
	
	public QuitCommand(CLIParser cliParser, SimpleServentListener simpleServentListener, FifoListener fifoListener) {
		this.cliParser = cliParser;
		this.simpleServentListener = simpleServentListener;
		this.fifoListener = fifoListener;
	}
	
	@Override
	public String commandName() {

		return "quit";
	}
	
	@Override
	public void execute(String args) {
		
		try {
			
			// Obavesti sledbenika da napustas mrezu kako bi mogao da te ukloni
			// Ako nismo jedini servent u sistemu
			if(AppConfig.chordState.getAllNodeIdInfoMap().size() > 1) {
				
				String nextNodeIpAddress = AppConfig.chordState.getNextNodeIpAddress();
				int nextNodePort = AppConfig.chordState.getNextNodePort();
				
				QuitMessage quitMessage;
				
				// Ako trenutni servent izvrsava vec neki posao, saljemo sa quitMessage sa posacima posla koji izvrsavamo
				if(AppConfig.chordState.getExecutionJob() != null) {
					
					JobExecution jobExecution = AppConfig.chordState.getExecutionJob();
					String myJobName = jobExecution.getJobName();
					String myFractalId = jobExecution.getFractalId();
					List<Point> myComputedPoints = new ArrayList<Point>(jobExecution.getComputedPoints());
					
					quitMessage = new QuitMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNodeIpAddress, nextNodePort, AppConfig.myServentInfo.getId(), myJobName, myFractalId, myComputedPoints);
					
					// Zaustavljamo izvrsavanje posla
					jobExecution.stop();
					
				}
				// Ako je nas cvor idle, ne izvrsava ni jedan posao, onda samo saljemo QuitMessage bez podataka o izvrsavalju posla
				else {
					quitMessage = new QuitMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNodeIpAddress, nextNodePort, AppConfig.myServentInfo.getId());
				}
				
				MessageUtil.sendMessage(quitMessage);
				
			}
			
			// send bootstrap server quit message - to remove us from active servents
	        try {
	            Socket bsSocket = new Socket(AppConfig.bootstrapServer.getIp(), AppConfig.bootstrapServer.getListenerPort());

	            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
	            bsWriter.write("Quit\n" +
	                    AppConfig.myServentInfo.getIpAddress() + "\n" +
	                    AppConfig.myServentInfo.getListenerPort() + "\n");
	            bsWriter.flush();

	            bsSocket.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			
	        // ???
//	        System.exit(0);
	        
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
		cliParser.stop();
		simpleServentListener.stop();
        fifoListener.stop();
        for (Map.Entry<Integer, FifoSendWorker> entry: AppConfig.chordState.getFifoSendWorkerMap().entrySet()) {
            entry.getValue().stop();
        }

        AppConfig.timestampedStandardPrint("Quitting...");
		
	}
	
}
