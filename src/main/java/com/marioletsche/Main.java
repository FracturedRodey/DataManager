package com.marioletsche;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttException;
import com.marioletsche.Logging.DataLogger;


public class Main {
	private static volatile boolean exitFlag = false;
	
	public static void main(String[] args) {
		AASBroker broker = null;
		DataLogger logger = new DataLogger();
		
		try {
			broker = new AASBroker();
		} catch(MqttException e) {
			logger.logInfo("Something during instantiation has failed." + "\n" + e.getMessage());
		}
		
		// Just for exiting
		Thread inputThread = new Thread(() -> {
			Scanner scanner = new Scanner(System.in);
			while (!exitFlag) {
				String input = scanner.nextLine();
				if (input.equalsIgnoreCase("exit")) {
					exitFlag = true;
				}
			}
			scanner.close();
		});
		inputThread.start();
		
		while (!exitFlag) {
			
		}
		
		logger.logInfo("Application is getting closed.");
		if (broker != null) {
			broker.close();
		}
	}
}
