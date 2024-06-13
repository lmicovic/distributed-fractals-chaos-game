package app.bootstrap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

import app.AppConfig;
import app.ServentInfo;

public class BootstrapConfig {

	public static BootstrapServer bootstrapServer;
	
	public static List<ServentInfo> activeServents;
	
	public static void readBootstrapConfig(String bootstrapConfigPath) {
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(bootstrapConfigPath)));
		} catch (IOException e) {
			timestampedErrorPrint("Couldn't open boostrap properties file. Exiting...");
			e.printStackTrace();
			System.exit(0);
		}
		
		// Bootstrap Config
		try {
			
			String ip = properties.getProperty("bs.ip");
			int port = Integer.parseInt(properties.getProperty("bs.port"));
			
			bootstrapServer = new BootstrapServer(ip, port);
			
			
		} catch (NumberFormatException e) {
			timestampedErrorPrint("Could not parse bootstrap port number...");
			e.printStackTrace();
			System.exit(0);
		} catch (Exception e) {
			timestampedErrorPrint("Could't load Bootstrap configuration...");
			e.printStackTrace();
			System.exit(0);
		}
		
		activeServents = new CopyOnWriteArrayList<ServentInfo>();
		
		
	}
	
	public static void timestampedErrorPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.err.println(timeFormat.format(now) + " - " + message);
	}
	
	public static void timestampedStandardPrint(String message) {
		DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		Date now = new Date();
		
		System.out.println(timeFormat.format(now) + " - " + message);
	}
	
	public static int removeServentByIpAndPort(String ip, int port) {
		
		if(activeServents.isEmpty()) {
			BootstrapConfig.timestampedStandardPrint("No active servents.");
			return -2;
		}
		
		for (int i = 0; i < activeServents.size(); i++) {
			ServentInfo serventInfo = activeServents.get(i);
			if(serventInfo.getIpAddress().equals(ip) && serventInfo.getListenerPort() == port) {
				BootstrapConfig.timestampedStandardPrint("Removeing servent: " + serventInfo);
				activeServents.remove(i);
				return 0;
			}
		}
		
		BootstrapConfig.timestampedStandardPrint("No active servent with: [" + ip + ":" + port + "]");
		return -1;
		
	}
	
}
