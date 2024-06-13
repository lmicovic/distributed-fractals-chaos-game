package app;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

import servent.message.Message;
import servent.message.NewNodeMessage;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {
	
	// Saljemo Hail poruku Boostrap serveru
	// Dobijamo od Boostrap servera first i last servent
	private String getLastAndFistServent() {
		
		String boostrapIpAddress = AppConfig.bootstrapServer.getIp();
		int boostrapPort = AppConfig.bootstrapServer.getListenerPort();
		
		String firstServent = "";
		String lastServent = "";
		
		try {
			
			Socket bsSocket = new Socket(boostrapIpAddress, boostrapPort);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			
			// Saljemo: myIp: myPort
			bsWriter.write("Hail\n" + AppConfig.myServentInfo.getIpAddress() + "\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();
			
			// Boostrap vrati lastServent i firstServent
			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			lastServent = bsScanner.nextLine();
			firstServent = bsScanner.nextLine();
			
			bsSocket.close();
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("ServentInitializer: error contacting Bootstrap server...");
			AppConfig.timestampedErrorPrint(e.getMessage());
			System.exit(0);
		}
		
		return lastServent + " " + firstServent;
		
	}
	
	@Override
	public void run() {
		
		
		try {

			String servents = getLastAndFistServent();
			String firstServent = servents.split(" ")[1];
			String lastServent = servents.split(" ")[0];
			
			// Ako nema lastServent
			if(lastServent.equals("")) {
				AppConfig.timestampedErrorPrint("ServentInitializer: error contacting Bootstrap server...");
				System.exit(0);
			}
			
			// Ako nam Bootstrap server posalje za lastServent -1, onda znaci da smo vi prvi
			if(lastServent.equals("-1")) {
				AppConfig.myServentInfo.setId(0);
				
				this.printServentStart();
				
				AppConfig.chordState.getAllNodeIdInfoMap().put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
				AppConfig.timestampedStandardPrint("First node in system.");
			}
			
			// Ako nistmo prvi cvor u sistemu, onda lastServent treba da obavesti lastServent da zelimo da se povezemo
			else {
				
				String reciverIpAddress = lastServent.split(":")[0];
				int reciverPort = Integer.parseInt(lastServent.split(":")[1]);
				
				NewNodeMessage newNodeMessage = new NewNodeMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), reciverIpAddress, reciverPort, firstServent);
				MessageUtil.sendMessage(newNodeMessage);
				
			}

		} catch (Exception e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
		}
		
	}
	
	// Ispisuje da je servent poceo sa radom i redirektuje output u file
	private void printServentStart() {
		
		try {
			
			AppConfig.timestampedStandardPrint("Starting servent " + AppConfig.myServentInfo);
			
			// Postavljamo da nam print bude u file umesto na konzolu
//			PrintStream outPrintStream = new PrintStream(new FileOutputStream("chord/output/servent" + AppConfig.myServentInfo.getId() + "_out.txt"));
//			System.setOut(outPrintStream);
			
			AppConfig.timestampedStandardPrint("Starting servent " + AppConfig.myServentInfo);
			String log = "Servent jobs: ";
			for (Job serventJob : AppConfig.myServentInfo.getJobs()) {
				log += serventJob + ",";
			}
			
			log = log.substring(0, log.length()-2);
			AppConfig.timestampedStandardPrint(log);
			
		} catch (Exception e) {
			AppConfig.timestampedErrorPrint("ServentInitializer - file not found: chord/output/servent/" + AppConfig.myServentInfo.getId() + "_out.txt ");
			AppConfig.timestampedErrorPrint(e.getMessage());
			System.exit(0);
		}
		
	}
	
}
