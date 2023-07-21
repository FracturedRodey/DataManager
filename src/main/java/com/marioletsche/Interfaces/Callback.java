package com.marioletsche.Interfaces;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

public interface Callback {
	public void callbackCreate(JSONObject json);
	public void callbackRead(ObjectId id);
	public void callbackSend(String json);
	public void callbackUpdate(ObjectId id, JSONObject json);
	public void callbackDelete(ObjectId id);
	public void close();
}
