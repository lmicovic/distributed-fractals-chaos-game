package servent.message;



public class NewNodeMessage extends BasicMessage {

	private static final long serialVersionUID = 3899837286642127636L;

	private String firstServentIpAddressAndPort;
	
	public NewNodeMessage(String sednerIpAddress, int senderPort, String reciverIpAddress, int reciverPort, String firstServentIpAddressAndPort) {
		super(MessageType.NEW_NODE, sednerIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.firstServentIpAddressAndPort = firstServentIpAddressAndPort;
	}
	
	public String getFirstServentIpAddressAndPort() {
		return firstServentIpAddressAndPort;
	}
	
	
}
