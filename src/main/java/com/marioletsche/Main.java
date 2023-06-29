package com.marioletsche;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
	private static volatile boolean exitFlag = false;
	
	public static void main(String[] args) {
		System.out.println("Starting up...");
		AASBroker broker = null;
		
		try {
			broker = new AASBroker();
		} catch(MqttException e) {
			System.err.println("Something during insertion has failed.");
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
		
		System.out.println("Exiting the program.");
		if (broker != null) {
			broker.close();
		}
		System.exit(0);
	}
}
