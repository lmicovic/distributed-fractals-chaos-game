package app.bootstrap;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import app.AppConfig;
import app.ServentInfo;


// Koristi se za povezivanje nodeova.
public class BootstrapServerWorker {

	private volatile boolean working = true;
	private List<String> activeServents;
	
	private class CLIWorker implements Runnable {
		@Override
		public void run() {
			Scanner sc = new Scanner(System.in);
			
			String line;
			while(true) {
				line = sc.nextLine();
				
				if (line.equals("stop")) {
					working = false;
					break;
				}
			}
			
			sc.close();
		}
	}
	
	public BootstrapServerWorker() {
		this.activeServents = new ArrayList<String>();
	}
	
	public void doBootstrap(int bsPort) {
		
		// Za primanje komandi
		Thread cliThread = new Thread(new CLIWorker());
		cliThread.start();
		
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(bsPort);
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e1) {
			BootstrapConfig.timestampedErrorPrint("Problem while opening listener socket.");
			System.exit(0);
		}
		
		Random rand = new Random(System.currentTimeMillis());
		
		while (working) {
			try {
				Socket newServentSocket = listenerSocket.accept();
				
				 /* 
				 * Handling these messages is intentionally sequential, to avoid problems with
				 * concurrent initial starts.
				 * 
				 * In practice, we would have an always-active backbone of servents to avoid this problem.
				 */
				
				Scanner socketScanner = new Scanner(newServentSocket.getInputStream());
				String message = socketScanner.nextLine();
				
				/*
				 * New servent has hailed us. He is sending us his own listener port.
				 * He wants to get a listener port from a random active servent,
				 * or -1 if he is the first one.
				 */
				
				BootstrapConfig.timestampedStandardPrint("Got message: " + message);
				
				// Hail serventIp serventPort
				// Hail 127.0.0.1 1200
				if(message.equals("Hail")) {
					
					try {
						
						String newServentIp = socketScanner.nextLine();
						int newServentPort = socketScanner.nextInt();
						
						BootstrapConfig.timestampedStandardPrint("Got Hail message from: [" + newServentIp + ":" + newServentPort + "]");
						
						PrintWriter socketWriter = new PrintWriter(newServentSocket.getOutputStream());
						
						// Ako je prvi servent u sistemu
						if(activeServents.size() == 0) {
							socketWriter.write("-1\n");
							socketWriter.write("-1\n");
							String newServent = newServentIp + ":" + newServentPort;
							BootstrapConfig.timestampedStandardPrint("First servent - adding " + newServent);
							activeServents.add(newServent);
						}
						// Send last servent and first servent ipAddress and port
						// Ako nije prvi servent u sistemu 
						else {
							
							String lastServent = activeServents.get(activeServents.size()-1);
							BootstrapConfig.timestampedStandardPrint("Sending last serent: [" + lastServent + "]");
							socketWriter.write(lastServent + "\n");
							
							String firstServent = activeServents.get(0);
							BootstrapConfig.timestampedStandardPrint("Sending first servent: [" + firstServent + "]");
							socketWriter.write(firstServent + "\n");

						}
						
						socketWriter.flush();
						newServentSocket.close();
						
						
					} catch (Exception e) {
						BootstrapConfig.timestampedErrorPrint(e.getMessage());
					}
					
				}
				
				// Kada se newServent poveze sa svim Serventima na mrezi on salje new, i mozemo ga dodati u listu aktivnih servenata
				// I ako nije serventId u koaliziji sa ostalim serventima
				// Message: newServentIp newServentPort
				else if(message.equals("New")) {
					
					try {
												
						String newServentIp = socketScanner.nextLine();
						int newServentPort = socketScanner.nextInt();
						String newServentInfo = newServentIp + ":" + newServentPort;
						
						BootstrapConfig.timestampedStandardPrint("Got New message from: [" + newServentIp + ":" + newServentPort + "]");						
						BootstrapConfig.timestampedStandardPrint("Adding new Servent: " + newServentInfo);
						activeServents.add(newServentInfo);
						
						
					} catch (Exception e) {
						BootstrapConfig.timestampedErrorPrint(e.getMessage());
					}
					
					newServentSocket.close();
					
				}
				// Ako servent posalje quit poruku
				else if(message.equals("Quit")) {
					
					try {
						
						String serventIpAddress = socketScanner.nextLine();
						int serventPort = socketScanner.nextInt();
						String serventInfo = serventIpAddress + ":" + serventPort;

						BootstrapConfig.timestampedStandardPrint("Removeing servent: " + serventInfo);
						
						activeServents.remove(serventInfo);

						newServentSocket.close();

					} catch (Exception e) {
						BootstrapConfig.timestampedErrorPrint(e.getMessage());
					}
				}
				else {
					BootstrapConfig.timestampedErrorPrint("BootstrapServer got message that is not new or hail: " + message);
				}
				
			} catch (SocketTimeoutException e) {
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Expects one command line argument - the port to listen on.
	 */
	public static void main(String[] args) {
		
		BootstrapConfig.readBootstrapConfig("chord/bootstrap.properties");
		
		int bsPort = BootstrapConfig.bootstrapServer.getListenerPort();
		
		BootstrapConfig.timestampedStandardPrint("Bootstrap server started on ip address: " + BootstrapConfig.bootstrapServer.getIp());
		BootstrapConfig.timestampedStandardPrint("Bootstrap server started on port: " + BootstrapConfig.bootstrapServer.getListenerPort());
		
		BootstrapServerWorker bs = new BootstrapServerWorker();
		bs.doBootstrap(bsPort);
	}
}
