package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import app.AppConfig;
import app.ServentInfo;
import servent.handler.FifoHandler;
import servent.message.Message;
import servent.message.ReleaseMessage;
import servent.message.ReplyMessage;
import servent.message.RequestMessage;
import servent.message.util.MessageUtil;

public class FifoListener implements Runnable {
	
	private volatile boolean working;
	
	public FifoListener() {
		this.working = true;
	}
	
	@Override
	public void run() {
		
		ServerSocket listenerSocket = null;
		try {
			
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getFifoListenerPort(), 100);
			listenerSocket.setSoTimeout(1000);
			
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint(e.getMessage());
			System.exit(0);
		}
		
		while(working) {
			
			try {
				
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();	// blokirajuce
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				switch(clientMessage.getMessageType()) {
				
					// Trazimo Lamport kriticnu sekciju
					case REQUEST:
						RequestMessage requestMessage = (RequestMessage) clientMessage;
						int finalReciverId = requestMessage.getFinalReciverId();
						int clock = requestMessage.getClock();
						
						// Ako ja nisam reciver, prosledjujemo poruku
						if(AppConfig.myServentInfo.getId() != finalReciverId) {
							
							ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
							RequestMessage refactorRequestMessage = new RequestMessage(requestMessage.getSenderIpAddress(), requestMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getFifoListenerPort(), finalReciverId, clock);
							
							MessageUtil.sendMessage(refactorRequestMessage);
							
						}
						
						// Ako sam ja reciver, requestMessage handler
						else if(AppConfig.myServentInfo.getId() == finalReciverId) {
							
							FifoHandler.handleRequestMessage(requestMessage);
						}
						break;
						
					// Dobijamo odgovor za kriticnu sekciju, kada serventi odgovore da li neki servent moze da dobije kriticnu sekciju
					case REPLY:
						ReplyMessage replyMessage = (ReplyMessage) clientMessage;
						finalReciverId = replyMessage.getFinalReciverId();
						clock = replyMessage.getClock();
						
						// Ako nisam ja krajnji primalac reply poruke, odna je prosledjuejm
						if(finalReciverId != AppConfig.myServentInfo.getId()) {
							
							ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
							
							// Rekonstruisemo replyMesage
							ReplyMessage refactorReplyMessage = new ReplyMessage(replyMessage.getSenderIpAddress(), replyMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getFifoListenerPort(), finalReciverId, clock);
							MessageUtil.sendMessage(refactorReplyMessage);
							
						}
						// Ako sam ja krajnji primalac replyPoruke, onda izvrsiti replyMessageHandler
						else {
							FifoHandler.handleReplyMessage(replyMessage);
						}
						
						break;
					
					// Oslobadjamo kriticnu sekciju
					case RELEASE:

						ReleaseMessage releaseMessage = (ReleaseMessage) clientMessage;
						
						finalReciverId = releaseMessage.getFinalReciverId();
						clock = releaseMessage.getClock();
						
						// Ako nisam ja krajnji revicer
						if(AppConfig.myServentInfo.getId() != finalReciverId) {
							ServentInfo nextServent = AppConfig.chordState.getNextNodeForServentId(finalReciverId);
							
							// Rekonstruisemo releaseMessage
							ReleaseMessage refactorReleaseMessage = new ReleaseMessage(releaseMessage.getSenderIpAddress(), releaseMessage.getSenderPort(), nextServent.getIpAddress(), nextServent.getFifoListenerPort(), clock, finalReciverId);
							MessageUtil.sendMessage(refactorReleaseMessage);
							
						}
						
						// Ako sam ja krajnji reciver za releaseMessage
						else {
							FifoHandler.handleReleaseMessage(releaseMessage);
						}
						break;
						
						
				}
				
			} catch (SocketTimeoutException e ) {
//				AppConfig.timestampedErrorPrint(e.getMessage());
			} catch (IOException e) {
				AppConfig.timestampedErrorPrint(e.getMessage());
			}
		}
		
	}
	
	public void stop() {
		this.working = false;
	}
	
}
