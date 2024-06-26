package servent;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.AppConfig;
import app.Cancellable;
import servent.handler.AckIdleHandler;
import servent.handler.AckJobExecutionHandler;
import servent.handler.AskGetHandler;
import servent.handler.AskJobFractalIdResultHandler;
import servent.handler.AskJobResultHandler;
import servent.handler.AskStatusHandler;
import servent.handler.ComputedPointsMessageHandler;
import servent.handler.IdleHandler;
import servent.handler.JobExecutionHandler;
import servent.handler.JobFractalIDResultHandler;
import servent.handler.JobResultHandler;
import servent.handler.JobScheduleHandler;
import servent.handler.MessageHandler;
import servent.handler.NewNodeHandler;
import servent.handler.NullHandler;
import servent.handler.PutHandler;
import servent.handler.QuitMessageHandler;
import servent.handler.ReleaseCriticalSectionHandler;
import servent.handler.SorryHandler;
import servent.handler.StopJobHandler;
import servent.handler.TellGetHandler;
import servent.handler.TellStatusHandler;
import servent.handler.UpdateHandler;
import servent.handler.WelcomeHandler;
import servent.message.Message;
import servent.message.util.MessageUtil;

public class SimpleServentListener implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	public SimpleServentListener() {
		
	}

	/*
	 * Thread pool for executing the handlers. Each client will get it's own handler thread.
	 */
	private final ExecutorService threadPool = Executors.newWorkStealingPool();
	
	@Override
	public void run() {
		ServerSocket listenerSocket = null;
		try {
			listenerSocket = new ServerSocket(AppConfig.myServentInfo.getListenerPort(), 100);
			/*
			 * If there is no connection after 1s, wake up and see if we should terminate.
			 */
			listenerSocket.setSoTimeout(1000);
		} catch (IOException e) {
			AppConfig.timestampedErrorPrint("Couldn't open listener socket on: " + AppConfig.myServentInfo.getListenerPort());
			System.exit(0);
		}
		
		
		while (working) {
			try {
				Message clientMessage;
				
				Socket clientSocket = listenerSocket.accept();
				
				//GOT A MESSAGE! <3
				clientMessage = MessageUtil.readMessage(clientSocket);
				
				MessageHandler messageHandler = new NullHandler(clientMessage);
				
				/*
				 * Each message type has it's own handler.
				 * If we can get away with stateless handlers, we will,
				 * because that way is much simpler and less error prone.
				 */
				switch (clientMessage.getMessageType()) {
				case NEW_NODE:
					messageHandler = new NewNodeHandler(clientMessage);
					break;
				case WELCOME:
					messageHandler = new WelcomeHandler(clientMessage);
					break;
				case SORRY:
					messageHandler = new SorryHandler(clientMessage);
					break;
				case UPDATE:
					messageHandler = new UpdateHandler(clientMessage);
					break;
				case PUT:
					messageHandler = new PutHandler(clientMessage);
					break;
				case ASK_GET:
					messageHandler = new AskGetHandler(clientMessage);
					break;
				case TELL_GET:
					messageHandler = new TellGetHandler(clientMessage);
					break;
				case POISON:
					break;
				case JOB_EXECUTION:
					messageHandler = new JobExecutionHandler(clientMessage);	// to do: !!!
					break;
				case ACK_JOB_EXECUTION:
					messageHandler = new AckJobExecutionHandler(clientMessage);
					break;
				case JOB_SCHEDULE:
					messageHandler = new JobScheduleHandler(clientMessage);  
					break;
				case IDLE:
					messageHandler = new IdleHandler(clientMessage);
					break;
				case ACK_IDLE:
					messageHandler = new AckIdleHandler(clientMessage);
					break;
				case STOP_JOB:
					messageHandler = new StopJobHandler(clientMessage);	
					break;
				case RELEASE_CRITICAL_SECTION:
					messageHandler = new ReleaseCriticalSectionHandler(clientMessage);
					break;
				case QUIT:
					messageHandler = new QuitMessageHandler(clientMessage);
					break;
				case COMPUTED_POINTS:
					messageHandler = new ComputedPointsMessageHandler(clientMessage);
					break;
				case ASK_STATUS:
					messageHandler = new AskStatusHandler(clientMessage);
					break;
				case TELL_STATUS:
					messageHandler = new TellStatusHandler(clientMessage);
					break;
				case ASK_JOB_RESULT:
					messageHandler = new AskJobResultHandler(clientMessage);
					break;
				case JOB_RESULT:
					messageHandler = new JobResultHandler(clientMessage);
					break;
				case ASK_JOB_FRACTALID_RESULT:
					messageHandler = new AskJobFractalIdResultHandler(clientMessage);
					break;
				case JOB_FRACTALID_RESULT:
					messageHandler = new JobFractalIDResultHandler(clientMessage);
					break;
				}
				
				threadPool.submit(messageHandler);
			} catch (SocketTimeoutException timeoutEx) {
				//Uncomment the next line to see that we are waking up every second.
//				AppConfig.timedStandardPrint("Waiting...");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		this.working = false;
	}

}
