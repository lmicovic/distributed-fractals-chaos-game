package app.bootstrap;

import java.io.Serializable;

public class BootstrapServer implements Serializable {

	private static final long serialVersionUID = -4769434101824330312L;

	private String ip;
	private int listenerPort;
	
	public BootstrapServer(String ip, int listenerPort) {
		this.ip = ip;
		this.listenerPort = listenerPort;
	}
	
	public String getIp() {
		return ip;
	}
	
	public int getListenerPort() {
		return listenerPort;
	}
	
	@Override
	public String toString() {
		return "[BoostrapServer|" + ip + ":" + listenerPort + "]";
	}
	
}
