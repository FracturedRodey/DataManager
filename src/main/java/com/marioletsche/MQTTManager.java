package com.marioletsche;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.marioletsche.Interfaces.Callback;
import com.marioletsche.Interfaces.DataTransferManager;

public class MQTTManager implements DataTransferManager {
	private ArrayList<Callback> listeners = new ArrayList<Callback>();
	private String serverURI = "tcp://broker.hivemq.com:1883";
	private String publisherID = UUID.randomUUID().toString();
	private static final String CREATE_TOPIC = "testAAS";
	private static final String READ_TOPIC = "readTestAAS";
	private static final String SEND_TOPIC = "sendTestAAS";
	private static final String UPDATE_TOPIC = "updateTestAAS";
	private static final String DELETE_TOPIC = "deleteTestAAS";
	private boolean taggedToClose = false;
	
	private IMqttClient publisher;
	private MqttConnectOptions options;
	
	public MQTTManager(Callback listener) throws MqttException {
		listeners.add(listener);
		
		publisher = new MqttClient(serverURI, publisherID);
		options = new MqttConnectOptions();
		options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        publisher.connect(options);
        
        System.out.println("Connection to Mqtt-Broker successful.");
        
        publisher.subscribe(CREATE_TOPIC, (unsure, msg) -> {
        	if (taggedToClose) {
        		System.err.println("Cannot receive information while closing connection.");
        		return;
        	}
        	
        	// Handle pay load and convert into JSon
            byte[] payload = msg.getPayload();
            String received = new String(payload, StandardCharsets.UTF_8);
            
            // Exit the program if "exit" is sent, else create entry
            if (!checkExit(received))
            	create(received);
        });
        
        // Same for send topic
        // TODO: Maybe get rid of code duplication.
        publisher.subscribe(READ_TOPIC, (unsure, msg) -> {
        	if (taggedToClose) {
        		System.err.println("Cannot receive information while closing connection.");
        		return;
        	}
        	
        	// Handle pay load and convert into JSon
            byte[] payload = msg.getPayload();
            String received = new String(payload, StandardCharsets.UTF_8);
            
            // Exit the program if "exit" is sent, else create entry
            if (!checkExit(received))
            	read(received);
        });
        
        publisher.subscribe(UPDATE_TOPIC, (unsure, msg) -> {
        	if (taggedToClose) {
        		System.err.println("Cannot receive information while closing connection.");
        		return;
        	}
        	
        	// Handle pay load and convert into JSon
            byte[] payload = msg.getPayload();
            String received = new String(payload, StandardCharsets.UTF_8);
            
            // Exit the program if "exit" is sent, else create entry
            if (!checkExit(received))
            	update(received);
        });
        
        publisher.subscribe(DELETE_TOPIC, (unsure, msg) -> {
        	if (taggedToClose) {
        		System.err.println("Cannot receive information while closing connection.");
        		return;
        	}
        	
        	// Handle pay load and convert into JSon
            byte[] payload = msg.getPayload();
            String received = new String(payload, StandardCharsets.UTF_8);
            
            // Exit the program if "exit" is sent, else create entry
            if (!checkExit(received))
            	delete(received);
        });
	}
	
	/*
	 * If signal from connection is 0, this sends the data to all listeners.
	 */
	public void create(String message) {
		try {
			System.out.println("Received create message from broker");
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(message);
            
            for (Callback callback : listeners) {
            	callback.callbackCreate(json);
            }
        } catch (ParseException e) {
        	System.err.println("Error parsing to JSON. Please verify the input.");
        	return;
        }           
	}
	
	public void read(String message) {
		System.out.println("Received read message from broker");
		ObjectId id = new ObjectId(message);
		for (Callback callback : listeners) {
			callback.callbackRead(id);
		}
	}
	
	public void update(String message) {
		System.out.println("Received update message from broker");
		String[] message_split = message.split(";");
		
		if (message_split.length < 2) {
			System.out.println("Not enough parameters for an update");
			return;
		}
		
		try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(message_split[1]);
            ObjectId id = new ObjectId(message_split[0]);
            
            for (Callback callback : listeners) {
            	callback.callbackUpdate(id ,json);
            }
        } catch (ParseException e) {
        	System.err.println("Error parsing to JSON. Please verify the input.");
        	return;
        }           
	}
	
	public void delete(String message) {
		System.out.println("Received delete message from broker.");
		
		ObjectId id = new ObjectId(message);
		for (Callback callback : listeners) {
			callback.callbackDelete(id);
		}
	}
	
	/*
	 * Sends the received JSON to the broker.
	 */
	public void send(String json) {
		MqttMessage message = new MqttMessage(json.getBytes());
		try {
			publisher.publish(SEND_TOPIC, message);
		} catch (MqttException e) {
			System.err.println("Failed to send message to broker.");
		}
	}
	
	public boolean checkExit(String message) {
		if (message.equals("exit")) {
			System.out.println("Exit message received. Exiting the program...");
        	for (Callback callback : listeners) {
        		taggedToClose = true;
        		callback.close();
        	}
        	return true;
		}
		return false;
	}
	
	/*
	 * Closes connection to the broker.
	 */
	public void close() {
		try {
			publisher.disconnect();
			publisher.close();
		} catch (MqttException e) {
			System.err.println("Failed to close connection to broker");
			return;
		}
		System.out.println("Disconnected from broker.");
	}
}
