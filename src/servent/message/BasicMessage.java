package servent.message;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;

/**
 * A default message implementation. This should cover most situations.
 * If you want to add stuff, remember to think about the modificator methods.
 * If you don't override the modificators, you might drop stuff.
 * @author bmilojkovic
 *
 */
public class BasicMessage implements Message, Comparable<Message> {

	private static final long serialVersionUID = -9075856313609777945L;
	private final MessageType type;
	private final String senderIpAddress;
	private final int senderPort;
	private final String recieverIpAddress;
	private final int recieverPort;
	private String messageText;
	
	//This gives us a unique id - incremented in every natural constructor.
	private static AtomicInteger messageCounter = new AtomicInteger(0);
	private final int messageId;
	
	private int clock;	// koristi se za lock
	
	public BasicMessage(MessageType messageType, String senderIpAddress, int senderPort, String recieverIpAddress, int recieverPort) {
		this.type = messageType;
		this.senderIpAddress = senderIpAddress;
		this.senderPort = senderPort;
		this.recieverIpAddress = recieverIpAddress;
		this.recieverPort = recieverPort;
		this.messageText = "";
		
		this.messageId = messageCounter.getAndIncrement();
	}
	
	public BasicMessage(MessageType messageType, String senderIpAddress, int senderPort, String recieverIpAddress, int recieverPort, String messageText) {
		this.type = messageType;
		this.senderIpAddress = senderIpAddress;
		this.senderPort = senderPort;
		this.recieverIpAddress = recieverIpAddress;
		this.recieverPort = recieverPort;
		this.messageText = messageText;
		
		this.clock = 0;
		
		this.messageId = messageCounter.getAndIncrement();
	}
	
	public void setText(String text) {
		this.messageText = text;
	}
	
	@Override
	public int getMessageId() {
		return messageId;
	}
	
	public String getSenderIpAddress() {
		return senderIpAddress;
	}
	
	public int getSenderPort() {
		return senderPort;
	}
	
	public String getReceiverIpAddress() {
		return recieverIpAddress;
	}
	
	public int getReceiverPort() {
		return recieverPort;
	}
	
	public void setClock(int clock) {
		this.clock = clock;
	}
	
	public int getClock() {
		return clock;
	}
	
	@Override
	public String getMessageText() {
		return messageText;
	}
	
	@Override
	public MessageType getMessageType() {
		return type;
	}
	
	@Override
	public boolean isFifo() {
		return false;
	}
	
	/**
	 * Comparing messages is based on their unique id and the original sender port.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BasicMessage) {
			BasicMessage other = (BasicMessage)obj;
			
			if (getMessageId() == other.getMessageId() &&
				getSenderIpAddress().equals(other.getSenderIpAddress()) &&
				getSenderPort() == other.getSenderPort()) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Hash needs to mirror equals, especially if we are gonna keep this object
	 * in a set or a map. So, this is based on message id, original sender ip address and port.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(getMessageId(), getSenderIpAddress(), getSenderPort());
	}
	
	@Override
	public int compareTo(Message other) {
		if (this.clock > other.getClock()) {
			return 1;
		} else if (this.clock < other.getClock()) {
			return -1;
		} else if (this.getMessageId() > other.getMessageId()) { // todo: check poredjenje?
			return 1;
		}
		return -1;
	}
	
	/**
	 * Returns the message in the format: <code>[sender_id|sender_port|message_id|text|type|receiver_port|receiver_id]</code>
	 */
	@Override
	public String toString() {
		return "[" + senderIpAddress + ":" + senderPort + "|" + getMessageId() + "|" +
					getMessageText() + "|" + getMessageType() + "|" +
					recieverIpAddress + ":" + recieverPort + "]";
	}

}
