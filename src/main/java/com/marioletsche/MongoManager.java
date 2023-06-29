package com.marioletsche;

import java.util.Set;

import org.bson.Document;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.json.simple.JSONObject;
import org.bson.types.ObjectId;

public class MongoManager {
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Document> collection;
	private Callback callback;
	
	public MongoManager(Callback callback, String collection) {
		//Setup for the database
		this.callback = callback;
		client = MongoClients.create("mongodb://localhost:27017");
		database = client.getDatabase("aas");
		this.collection = database.getCollection(collection);
		
		System.out.println("Connection to Database successful.");
	}
	
	/*
	 * Set the current Collection used. If we need to change for some reason.
	 */
	public void setCollection(String collection) {
		this.collection = database.getCollection(collection);
		System.out.println("Collection added.");
	}
	
	/*
	 * Takes a JSON object and puts the data inside the database.
	 */
	public void create(JSONObject json) throws NullPointerException {
		Set<Object> keys = (Set<Object>) json.keySet();
		Document document = new Document();
		
		for (Object key : keys) {
			document.put(key.toString(), json.get(key));
		}
		collection.insertOne(document);
		System.out.println("Insertion into Database successful.");
	}
	
	public void read(ObjectId id) {
		Document query = new Document();
		query.put("_id", id);
		FindIterable<Document> cursor = this.collection.find(query);
		try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
			while (cursorIterator.hasNext()) {
				callback.callbackSend(cursorIterator.next().toJson());
			}
		}
	}
	
	public void close() {
		client.close();
		System.out.println("Disconnected from database.");
	}
}
