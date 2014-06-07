package org.ozone.metrics;

import java.io.StringWriter;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.WriterAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ozone.metrics.EventLogger;

import static org.junit.Assert.*;


public class EventLoggerTest {
	Logger log;
	StringWriter output=new StringWriter();
	EventLogger eventLog;
	@Before
	public void setup() {
		WriterAppender appender= new WriterAppender(new PatternLayout("%p %m"),output);
		appender.setName("StringAppender");
		appender.setThreshold(Level.ALL);
		
		log=Logger.getLogger(this.getClass());
		log.addAppender(appender);
		eventLog=new EventLogger(log);
	}
	@After
	public void teardown() {
		log.removeAllAppenders();
	}
	
	private void assertLogged(String key, String value) {
		String out=output.toString();
		assertTrue("Expected output to contain [\"" + key + "\":" + value + "] but got [" + out + "]",
				out.contains("\""+key+"\"" + ":" + value));
	}
	
	@Test
	public void logAtInfo() throws Exception {
		eventLog.info()
		        .set("key","value")
		        .log("Message");
		
		assertEquals("INFO Message -- {\"key\":\"value\"}",output.toString());
	}
	
	@Test
	public void logMultipleKeys() throws Exception {
		eventLog.info()
		        .set("key","value")
		        .set("1",2)
		        .log("Message");
		
		assertLogged("key","\"value\"");
		assertLogged("1","2");		
	}
	
	@Test
	public void nullKey() throws Exception {
		eventLog.info()
			.set(null, "")
			.log("Message");
		assertEquals("INFO Message -- {}",output.toString());
	}
	
	@Test
	public void nullValue() throws Exception {
		eventLog.info()
			.set("key", null)
			.log("Message");
		assertEquals("INFO Message -- {\"key\":null}",output.toString());
	}	
}
