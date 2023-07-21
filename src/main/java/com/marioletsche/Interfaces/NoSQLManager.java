package com.marioletsche.Interfaces;

import org.bson.types.ObjectId;
import org.json.simple.JSONObject;

public interface NoSQLManager {
	public void create(JSONObject json);
	public void read(ObjectId id);
	public void update(ObjectId id, JSONObject json);
	public void delete(ObjectId id);
	public void close();
}
