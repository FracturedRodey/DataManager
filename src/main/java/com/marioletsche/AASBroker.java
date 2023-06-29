package com.marioletsche;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.simple.JSONObject;
import org.bson.types.ObjectId;


public class AASBroker implements Callback {
	MongoManager database;
	MQTTManager data;
	
	public AASBroker() throws MqttException {
		this.database = new MongoManager(this, "shells");
		this.data = new MQTTManager(this);
	}
	
	/*
	 * Waits for JSON data to be received via the DataManager and gives it to the DBManager.
	 * Like this we can exchange the used Database and the DataManager.
	 */
	@Override
	public void callbackCreate(JSONObject json) {
		database.create(json);
	}

	@Override
	public void callbackRead(ObjectId id) {
		database.read(id);
	}

	@Override
	public void callbackSend(String json) {
		try {
			data.send(json);
		} catch (MqttException e) {
			System.err.println("Couldn't send the data to broker: " + e.getMessage());
		}
	}

	/*
	 * Close is used to disconnect to all databases and message brokers before exiting.
	 */
	@Override
	public void close() {
		Thread thread = new Thread(() -> {
			try {
				Thread.sleep(1000);
				this.database.close();
				this.data.close();
				System.out.println("Goodbye.");
				System.exit(0);
			} catch (InterruptedException e) {
				System.err.println("Thread got interrupted" + e.getMessage());
			} catch (MqttException e) {
				System.err.println("Data couldn't be closed properly." + e.getMessage());
			}
		});
		thread.start();
	}
}
