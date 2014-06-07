package org.ozone.metrics;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Creates log formats with an appended JSON object, useful for ingestion into LogStash for
 * faceting.
 * <p>
 * Usage:
 * <code><pre>
 * EventLogger eventLog=new EventLogger
 * eventLog.info()
 *         .set("key","value")
 *         .log("Message");
 * </pre></code>
 * Produces a message of "Message -- {"key":"value"}
 * <p> 
 * The EventLogger uses Jackson to serialize, and only serializes if the message is logged.
 */
public class EventLogger {
	static ObjectMapper mapper=new ObjectMapper();
	
	static public interface Builder {
		/** Sets the value of a single key */
		public Builder set(String key, Object value);
		/** Adds all values from the map to the JSON object */
		public Builder set(Map<String,Object> value);
		/** Log the message */
		public void log(String message);
		/** Log the message with a throwable */
		public void log(String message, Throwable e);
	}
	
	/**
	 * Null implementation that does nothing if the log level requested is not active.
	 */
	private static class NullChain implements Builder{
		@Override public Builder set(String key, Object value) { return this;}
		@Override public Builder set(Map<String,Object> value) { return this;}
		@Override public void log(String message) {/*noop*/}
		@Override public void log(String message, Throwable e) {/*noop*/}
	}

	/** 
	 * Builds the message
	 */
	private class LogChain implements Builder{
		private final Level level;
		private final Map<String,Object> params=new HashMap<String,Object>();
		
		public LogChain(Level level) {
			this.level=level;
		}

		@Override public Builder set(Map<String,Object> value) {
			params.putAll(value);
			return this;
		}
				
		@Override
		public Builder set(String key, Object value) {
			// silently ignore null keys
			if(key!=null) {
				params.put(key,value);
			}
			return this;
		}
		private String paramsToJson() {
			try {
				return mapper.writeValueAsString(params);
			} catch (JsonProcessingException e) {
				log.warn("Failed to serialize parameters " + params, e);
				return "{\"serializationError\":\"" + e.getLocalizedMessage().replaceAll("\"", "\\\"") + "\"}";
			}
		}
		
		@Override
		public void log(String message) {
			log.log(level, message + SEPERATOR + paramsToJson());
		}

		@Override
		public void log(String message, Throwable e) {
			log.log(level, message,e);
		}
	}

	Logger log;
	public static final String SEPERATOR=" -- ";
	
	/** Creates an EventLogger for the log4j logger */
	public EventLogger(Logger log) {
		this.log=log;
	}
	/** Convenience to create a builder at each log level*/
	public Builder debug() { return event(Level.DEBUG);}
	/** Convenience to create a builder at each log level*/
	public Builder info() { return event(Level.INFO);}
	/** Convenience to create a builder at each log level*/
	public Builder warn() { return event(Level.WARN);}
	/** Convenience to create a builder at each log level*/
	public Builder error() { return event(Level.ERROR);}
	/** Convenience to create a builder at each log level*/
	public Builder fatal() { return event(Level.FATAL);}
		
	/** Create an event builder at a parameterized log level */
	public Builder event(Level level) {
		if(log.isEnabledFor(level)) {
			return new LogChain(level);
		} else {
			return new NullChain();
		}
	}
}
