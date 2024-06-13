package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import servent.message.AskGetMessage;
import servent.message.PutMessage;
import servent.message.WelcomeMessage;
import servent.message.util.FifoSendWorker;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;	// ne pamtimo predecesor
	
	// Key: serventId, Value: serventInfo
	//we DO NOT use this to send messages, but only to construct the successor table
	private Map<Integer, ServentInfo> allNodeIdInfoMap;
	
	private Map<Integer, FifoSendWorker> fifoSenderWorkers = new HashMap<Integer, FifoSendWorker>(); 
	
	
	private JobExecution executionJob;							// Servent moze da ima samo jedan posao na izvrsavanju
	private List<Point> receivedComputedPoints = new ArrayList<>();
	private AtomicInteger receivedComputedPointsMessagesCount = new AtomicInteger(0);
	private AtomicInteger expectedComputedPointsMessagesCount = new AtomicInteger(0);
	
	private AtomicInteger receivedAckMessagesCount = new AtomicInteger(0);
	
	// [id -> fractalId + job]
	private Map<Integer, FractalJob> serventJobs;
	private List<Job> activeJobsList = new ArrayList<>();
	
	private Map<Integer, Integer> valueMap;
	
	
	
	public ChordState() {
		
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		allNodeIdInfoMap = new HashMap<Integer, ServentInfo>();
		serventJobs = new HashMap<Integer, FractalJob>();
		
	}
	
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	
	// Poziva se kada se novi cvor povezuje na mrezu, inicijalizuje svoj chordState
	// 1) Inicijalizuje liste svojih predhodnika i sledbenika
	// 2) Salje BoostrapServer-u NewMessage
	public void init(WelcomeMessage welcomeMsg) {
		
		// Postaviti predhodnika cvor koji je poslao poruku
		predecessorInfo = new ServentInfo(welcomeMsg.getSenderIpAddress(), welcomeMsg.getSenderPort(), welcomeMsg.getSenderPort() + 10);
		
		//set a temporary pointer to next node, for sending of update message
		// [0] - serventIp, [1] - serventPort
		String[] firstServentInfo = welcomeMsg.getFirstServentIpAddressAndPort().split(":");		
		
		// Postavimo u listu sledbenika prvi cvor u sistemu
		successorTable[0] = new ServentInfo(firstServentInfo[0], Integer.parseInt(firstServentInfo[1]), Integer.parseInt(firstServentInfo[1]) + 10);
		
		// Dodamo sebe u listu svih cvorova u sistemu
		allNodeIdInfoMap.put(AppConfig.myServentInfo.getId(), AppConfig.myServentInfo);
		
		AppConfig.timestampedStandardPrint("All nodes in system: " + allNodeIdInfoMap.toString());

		this.valueMap = new HashMap<Integer, Integer>();
		
		//tell bootstrap this node is not a collider
		// Saljemo BootstrapServeru New message kako bi nas dodao u listu servenata na sistemu
		// New: myIpAddress, myPort
		try {
			Socket bsSocket = new Socket(AppConfig.bootstrapServer.getIp(), AppConfig.bootstrapServer.getListenerPort());
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + "\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public String getNextNodeIpAddress() {
		if (successorTable[0] == null && allNodeIdInfoMap.size() <= 1) {
			return AppConfig.myServentInfo.getIpAddress();
		}
		return successorTable[0].getIpAddress();
	}
	
	public int getNextNodePort() {
		if (successorTable[0] == null && allNodeIdInfoMap.size() <= 1) {
			return AppConfig.myServentInfo.getListenerPort();
		}
		return successorTable[0].getListenerPort();
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	// Proverava da li kljuc pripada trenutnom cvoru
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		// Uzmem trenuni i predhodni cvor
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		// Ako je predhodnik manji od trenutnog cvora
		if (predecessorChordId < myChordId) { //no overflow
			// Ako je key izmedju trenutnog i predhodnog cvora, onda pripada trenutnom cvoru
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
			
		// Slucaj kada je overflow zbog mod, ako je prethodnig veci od trenutnog cvora i ako je kljuc manji od trenutnog ili veci od predhodnog onda kljuc pripdada trenutnom cvoru
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	// Za prosledjeni port i ipAddress vracaServentId
	public int getServentIdByServentPortAndIpAddress(int port, String address) {
		for (Entry<Integer, ServentInfo> entity : allNodeIdInfoMap.entrySet()) {
			
			int serventId = entity.getKey();
			ServentInfo servent = entity.getValue();
			
			if(servent.getListenerPort() == port && servent.getIpAddress().equals(address)) {
				return serventId;
			}
			
		}
		
		return -1;
	}
	
	// Za prosledjeni fifoPort i IpAddress vraca serventId
	public int getServentIdByFifoPortAndIpAddress(int fifoPort, String address) {
		
		for (Entry<Integer, ServentInfo> entity : allNodeIdInfoMap.entrySet()) {
			
			int serventId = entity.getKey();
			ServentInfo servent = entity.getValue();
			
			if(servent.getFifoListenerPort() == fifoPort && servent.getIpAddress().equals(address)) {
				return serventId;
			}
			
		}
		
		return -1;
	}
	
	public int getFirstSuccessorId() {
		// Ako nema sledbenika onda vraca sebe
		if(successorTable[0] == null && allNodeIdInfoMap.size() <= 1) {
			return AppConfig.myServentInfo.getId();
		}
		
		return successorTable[0].getId();
	}
	
	// Proverava da li je Servent moj sledbenik
	public boolean isServentMySuccessor(int serventId) {
		for (ServentInfo succesor : successorTable) {
			if(succesor.getId() == serventId) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isBetweenNodes(int target, int left, int right) {
		
		int temp = target;
		while (true) {
			temp = (temp + 1) % allNodeIdInfoMap.size();
			if (temp == left) {
				return false;
			}
			if (temp == right) {
				return true;
			}
		}
		
	}
	
	private void calculateChordLevel() {
		this.chordLevel = 1;
		int tmp = allNodeIdInfoMap.size();
		while (tmp > 2) {
			tmp /= 2;
			this.chordLevel++;
		}

		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
	}
	
	public ServentInfo getNextNodeForServentId(int receiverId) {
		
		// if I am receiver return myself
		if (receiverId == AppConfig.myServentInfo.getId()) {
			return AppConfig.myServentInfo;
		}
		
		
		
		// if it is my successor send directly to it
		if (isServentMySuccessor(receiverId)) {
			return allNodeIdInfoMap.get(receiverId);
		}

		
		
		int leftId = successorTable[0].getId();
		for (int i = 1; i < successorTable.length; i++) {
			int rightId = successorTable[i].getId();
		
			
			
			if (isBetweenNodes(leftId, rightId, receiverId)) {
				return successorTable[i-1];
			}
			leftId = rightId;
		}

		if (isBetweenNodes(leftId, successorTable[0].getId(), receiverId)) {
			return successorTable[successorTable.length - 1];
		}

		return successorTable[0];		
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	// Ako trazimo vrednost za neki kljuc i mi nismo vlanik za taj kljuc
	public ServentInfo getNextNodeForKey(int key) {
		
		// Ako kljuc pripada trenutnom cvoru, onda nista izlazimo napolje
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}

		int previousId = successorTable[0].getChordId();
		for (int i = 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}

			int successorId = successorTable[i].getChordId();

			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	
	// menjano
	// Formira tabelu sledbenika za trenudno cvor
	// Tabela se koristi za Chord pretragu - predtragu gde se nalazi vrednost koju trazimo
	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		AppConfig.timestampedStandardPrint(allNodeIdInfoMap.toString());

		calculateChordLevel();
		int firstSuccessorIndex = AppConfig.myServentInfo.getId() + 1;
		ServentInfo firstSuccessor = null;
		if (allNodeIdInfoMap.get(firstSuccessorIndex) != null) {
			firstSuccessor = allNodeIdInfoMap.get(firstSuccessorIndex);
		} else {
			if (AppConfig.myServentInfo.getId() != 0) {
				firstSuccessor = allNodeIdInfoMap.get(0);
			}
		}
		successorTable[0] = firstSuccessor;

		int currentIncrement = 2;
		//i is successorTable index
		int successorIndex = 1;
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			int id = (AppConfig.myServentInfo.getId() + (int)(Math.pow(2, i))) % allNodeIdInfoMap.size();
			if (allNodeIdInfoMap.containsKey(id)) {
				successorTable[successorIndex] = allNodeIdInfoMap.get(id);
				successorIndex++;
			}
		}
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	// menjano
	// Pronalazi predhodnika za trenutni cvor i osveza tabelu sledbenika
	public void addNodes(Map<Integer, ServentInfo> newNodes) {
		for (Map.Entry<Integer, ServentInfo> entry: newNodes.entrySet()) {
			int serventId = entry.getKey();
			allNodeIdInfoMap.put(serventId, entry.getValue());
			
			if (!fifoSenderWorkers.containsKey(serventId)) {	// add fifo worker for servent
				FifoSendWorker senderWorker = new FifoSendWorker(serventId);
				Thread senderThread = new Thread(senderWorker);
				senderThread.start();
				fifoSenderWorkers.put(serventId, senderWorker);
			}
		}

		MessageUtil.initializePendingMessages();
		updateSuccessorTable();
	}
	
	
	public void setFifoSendWorkerMap(Map<Integer, FifoSendWorker> newFifoWorkers) {
		for (Entry<Integer, FifoSendWorker> entity : newFifoWorkers.entrySet()) {
			fifoSenderWorkers.put(entity.getKey(), entity.getValue());
		}
	}
	
	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 */
	public void putValue(int key, int value) {
		if (isKeyMine(key)) {
			valueMap.put(key, value);
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNode.getIpAddress(), nextNode.getListenerPort(), key, value);
			MessageUtil.sendMessage(pm);
		}
	}
	
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */
	public int getValue(int key) {
		
		// Ako je kljuc moj, onda samo izvuce kljuc iz mape i vrati vrednost
		if (isKeyMine(key)) {
			if (valueMap.containsKey(key)) {
				return valueMap.get(key);
			
			// Ako kljuc pripada nama, ali nemamo vrednost za taj kljuc
			} else {
				return -1;
			}
		}
		
		// Ako kljuc ne pripada nama, onda trazimo kome kljuc pripada i saljemo AskGetMessage sledecemo node-u
		// AskGetMessageHandler isto proverava da li je on vlasnik za kljuc ako jeste vraca vrednost, ako nije onda salje askGetMessage poruku sledecem cvoru
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(), nextNode.getIpAddress(), nextNode.getListenerPort(), String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		return -2;
	}
	
	// Vraca serventId za prosledjeni jobName i fractalId 
	public int getIdForFractalIDAndJob(String jobName, String fractalId) {
		FractalJob fractalIdJob = new FractalJob(fractalId, jobName);
		for (Entry<Integer, FractalJob> entity: serventJobs.entrySet()) {
			if (entity.getValue().equals(fractalIdJob)) {
				return entity.getKey();
			}
		}
		return -1;
	}
	
	
	// Fraca prvi serventId za prosledjeni jobName
	public int getFirstIdForJob(String jobName) {
		return Collections.min(getAllIdsForJob(jobName));
	}
	
	// Vraca poslednji serventId za prosledjeni jobName
	public int getLastIdForJob(String jobName) {
		return Collections.max(getAllIdsForJob(jobName));
	}
	
	// Vraca sve serventId za prosledjeni jobName
	private List<Integer> getAllIdsForJob(String jobName) {
		List<Integer> ids = new ArrayList<>();
		for (Entry<Integer, FractalJob> entry: serventJobs.entrySet()) {
			if (entry.getValue().getJobName().equals(jobName)) {
				ids.add(entry.getKey());
			}
		}
		return ids;
	}
	
	public boolean addNewJob(Job job) {
		if (!activeJobsList.contains(job)) {
			activeJobsList.add(job);
			return true;
		}
		return false;
	}
	
	public void addNewJobs(List<Job> jobs) {
		for (Job job: jobs) {
			addNewJob(job);
		}
	}
	
	// Uklanja job
	public void removeJob(String jobName) {
		for (Job job: activeJobsList) {
			if (job.getName().equals(jobName)) {
				activeJobsList.remove(job);
				break;
			}
		}

		List<Integer> ids = new ArrayList<>();
		for (Entry<Integer, FractalJob> entry: serventJobs.entrySet()) {
			if (entry.getValue().getJobName().equals(jobName)) {
				ids.add(entry.getKey());
			}
		}

		for (Integer id: ids) {
			serventJobs.remove(id);
		}
	}
	
	public void addComputedPoints(List<Point> newPoints) {
		receivedComputedPoints.addAll(newPoints);
		receivedComputedPointsMessagesCount.getAndIncrement();
	}
	
	public void resetAfterReceivedComputedPoints() {
		receivedComputedPoints.clear();
		receivedComputedPointsMessagesCount.set(0);
		expectedComputedPointsMessagesCount.set(0);
	}
	
	public AtomicInteger getReceivedComputedPointsMessagesCount() {
		return receivedComputedPointsMessagesCount;
	}
	
	public AtomicInteger getExpectedComputedPointsMessagesCount() {
		return expectedComputedPointsMessagesCount;
	}
	
	public List<Point> getReceivedComputedPoints() {
		return receivedComputedPoints;
	}
	
	public Map<Integer, FifoSendWorker> getFifoSendWorkerMap() { 
		return fifoSenderWorkers; 
	}

    public AtomicInteger getReceivedAckMessagesCount() { 
    	return receivedAckMessagesCount; 
    }
    
    public void setReceivedAckMessagesCount(int count) {
		this.receivedAckMessagesCount = receivedAckMessagesCount;
	}
    
	public List<Job> getActiveJobsList() {
		return activeJobsList;
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}
	
	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}
	
	public Map<Integer, ServentInfo> getAllNodeIdInfoMap() {
		return allNodeIdInfoMap;
	}
	
	public JobExecution getExecutionJob() {
		return executionJob;
	}
	
	public void setExecutionJob(JobExecution executionJob) {
		receivedComputedPoints.clear();
		receivedComputedPointsMessagesCount.set(0);
		this.executionJob = executionJob;
	}
	
	public Map<Integer, FractalJob> getServentJobs() {
		return serventJobs;
	}
	
	public int getActiveJobsCount() {
		return activeJobsList.size();
	}
	
	public void setServentJobs(Map<Integer, FractalJob> serventJobs) {
		this.serventJobs = serventJobs;
	}
	
}
