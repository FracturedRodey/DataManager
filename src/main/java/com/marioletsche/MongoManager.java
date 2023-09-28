package com.marioletsche;

import java.util.Set;

import org.bson.Document;

import com.marioletsche.Interfaces.Callback;
import com.marioletsche.Interfaces.NoSQLManager;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.marioletsche.Logging.DataLogger;

import org.json.simple.JSONObject;
import org.bson.types.ObjectId;

public class MongoManager implements NoSQLManager {
	private MongoClient client;
	private MongoDatabase database;
	private MongoCollection<Document> collection;
	private Callback callback;
	private DataLogger logger = new DataLogger();
	
	public MongoManager(Callback callback, String collection) {
		//Setup for the database
		this.callback = callback;
		client = MongoClients.create("mongodb://localhost:27017");
		database = client.getDatabase("aas");
		this.collection = database.getCollection(collection);
		
		logger.logInfo("Connection to database was successful.");
	}
	
	/*
	 * Set the current Collection used. If we need to change for some reason.
	 */
	public void setCollection(String collection) {
		this.collection = database.getCollection(collection);
		logger.logInfo("Collection of database has been changed");
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
		logger.logInfo("Insertion into Database was successful." + "\n" + json.toString());
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
	
	/*
	 * Updates the values of the object
	 */
	public void update(ObjectId id, JSONObject json) {
		Set<Object> keys = (Set<Object>) json.keySet();
		Document document = new Document();
		
		// I'm not sure about this part, it seems that this must be done.
		Document updateObject = new Document();
		updateObject.put("$set", document);
		
		// Creating the document.
		for (Object key : keys) {
			document.put(key.toString(), json.get(key));
		}
		
		// Searching the document to be changed.
		Document query = new Document();
		query.put("_id", id);
		FindIterable<Document> cursor = this.collection.find(query);
		try (final MongoCursor<Document> cursorIterator = cursor.cursor()) {
			while (cursorIterator.hasNext()) {
				collection.updateOne(cursorIterator.next(), updateObject);
			}
		}
		
		logger.logInfo("Update in Database was successful" + "\n" + "The following data was updated: " + "\n" + json.toString());
	}
	
	public void delete(ObjectId id) {
		Document query = new Document();
		query.put("_id", id);
		collection.deleteOne(query);
		
		logger.logInfo("Deletion of the following object was successful." + "\n" + id.toString());
	}
	
	public void close() {
		client.close();
		logger.logInfo("Disconnected from database.");
	}
}
