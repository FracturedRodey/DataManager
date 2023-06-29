package com.marioletsche;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

public interface Callback {
	public void callbackCreate(JSONObject json);
	public void callbackRead(ObjectId id);
	public void callbackSend(String json);
	public void close();
}
