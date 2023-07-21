package com.marioletsche.Logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataLogger {
	private static final Logger logger = LoggerFactory.getLogger(DataLogger.class);
	
	public void logInfo(String message) {
		logger.info(message);
	}
	
	public void logError(String message) {
		logger.error(message);
	}
}
