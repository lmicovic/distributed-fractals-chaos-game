package servent.message;

import java.util.List;
import java.util.Map;

import javax.swing.JPopupMenu.Separator;

import app.ServentInfo;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;
	
	private String firstServentIpAddressAndPort;
	private Integer id;
	
	public WelcomeMessage(String senderIpAddress, int senderPort, String reciverIpAddress, int reciverPort, int id, String firstServentIpAddressAndPort) {
		super(MessageType.WELCOME, senderIpAddress, senderPort, reciverIpAddress, reciverPort);
		this.firstServentIpAddressAndPort = firstServentIpAddressAndPort;
		this.id = id;
	}
	
	public String getFirstServentIpAddressAndPort() {
		return firstServentIpAddressAndPort;
	}
	
	public Integer getId() {
		return id;
	}
}
