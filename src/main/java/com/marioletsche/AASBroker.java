package com.marioletsche;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.simple.JSONObject;

import com.marioletsche.Interfaces.Callback;
import com.marioletsche.Interfaces.DataTransferManager;
import com.marioletsche.Interfaces.NoSQLManager;

import org.bson.types.ObjectId;


public class AASBroker implements Callback {
	NoSQLManager database;
	DataTransferManager data;
	
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

	/*
	 * Once we receive the read message, the Data Manager calls this method to read from the database.
	 */
	@Override
	public void callbackRead(ObjectId id) {
		database.read(id);
	}

	/*
	 * Belongs to the read function. Is called by the database with the information that was asked for.
	 */
	@Override
	public void callbackSend(String json) {
			data.send(json);
	}
	
	@Override
	public void callbackUpdate(ObjectId id, JSONObject json) {
		database.update(id, json);
	}

	@Override
	public void callbackDelete(ObjectId id) {
		database.delete(id);
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
			}
		});
		thread.start();
	}
}
