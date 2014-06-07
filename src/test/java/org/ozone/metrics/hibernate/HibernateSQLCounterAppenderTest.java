package org.ozone.metrics.hibernate;

import static org.junit.Assert.assertEquals;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HibernateSQLCounterAppenderTest {

	Logger sqlLog;
	HibernateSQLCounterAppender appender;
	
	@Before
	public void setupAppender() {
		sqlLog=Logger.getLogger("org.hibernate.SQL");
		appender=HibernateSQLCounterAppender.inject();
	}
	
	@After
	public void cleanAppenders() {
		sqlLog.removeAllAppenders();
	}
	
	protected LoggingEvent sqlEvent(String msg) {
		return new LoggingEvent("org.hibernate.SQL",Logger.getLogger("org.hibernate.SQL"),Level.DEBUG,msg,null);
	}
	
	@Test
	public void basicCounting() throws Exception {
		HibernateSQLCounterAppender appender=new HibernateSQLCounterAppender();
		
		appender.append(sqlEvent("insert foo into bar"));
		assertEquals("Should receive one sql statement",1,appender.getCounter().getCount());
		assertEquals("Should receive one insert statement",1,appender.getCounter().getVerbCount("insert"));
	}
	
	@Test
	public void returnZeroForUnseenVerbs() throws Exception {
		HibernateSQLCounterAppender appender=new HibernateSQLCounterAppender();
		
		appender.append(sqlEvent("insert foo into bar"));
		assertEquals("Should receive one sql statement",1,appender.getCounter().getCount());
		assertEquals("Should have no select statements",0,appender.getCounter().getVerbCount("select"));
	}

	@Test
	public void countSeveralVerbs() throws Exception {
		HibernateSQLCounterAppender appender=new HibernateSQLCounterAppender();
		
		appender.append(sqlEvent("insert foo into bar"));
		appender.append(sqlEvent("select * from foo"));
		appender.append(sqlEvent("update blah set id=0 where id < 10"));

		
		assertEquals("Should receive one sql statement",3,appender.getCounter().getCount());
		assertEquals("Should receive one insert statement",1,appender.getCounter().getVerbCount("insert"));
		assertEquals("Should receive one select statement",1,appender.getCounter().getVerbCount("select"));
		assertEquals("Should receive one select statement",1,appender.getCounter().getVerbCount("update"));
	}
	
	@Test
	public void testResetTotalCounter() throws Exception {
		HibernateSQLCounterAppender appender=new HibernateSQLCounterAppender();
		
		appender.append(sqlEvent("insert foo into bar"));
		
		assertEquals("Should receive one sql statement",1,appender.getCounter().getCount());
		
		appender.resetCounter();
		
		assertEquals("Should be at zero again",0,appender.getCounter().getCount());
		
	}
	@Test
	public void testResetVerbCounters() throws Exception {
		HibernateSQLCounterAppender appender=new HibernateSQLCounterAppender();
		
		appender.append(sqlEvent("insert foo into bar"));
		
		assertEquals("Should receive one insert statement",1,appender.getCounter().getVerbCount("insert"));
		
		appender.resetCounter();
		
		assertEquals("Should be zero inserts",0,appender.getCounter().getVerbCount("insert"));
		
	}

	class TestRunnable implements Runnable {
		SQLCounter counter;
		int count;
		HibernateSQLCounterAppender appender;
		
		public TestRunnable(HibernateSQLCounterAppender appender, int count) {
			this.appender = appender;
			this.count=count;
		}

		@Override
			public void run() {
				for(int i=0; i < count; ++i) {
					appender.append(sqlEvent("insert foo into bar"));
				}
				counter=appender.getCounter();
			}
	}
	
	@Test
	public void threadLocalCounts() throws Exception {
		HibernateSQLCounterAppender appender=new HibernateSQLCounterAppender();
		
		appender.append(sqlEvent("insert foo into bar"));
		TestRunnable runner1=new TestRunnable(appender,2);
		TestRunnable runner2=new TestRunnable(appender,3);
		Thread t1=new Thread(runner1);
		Thread t2=new Thread(runner2);

		t1.start();
		t2.start();
		t1.join(100);
		t2.join(100);
		
		assertEquals("Thread 1 got wrong total number",2,runner1.counter.getCount());
		assertEquals("Thread 1 got wrong insert number",2,runner1.counter.getVerbCount("insert"));
		
		assertEquals("Thread 2 got wrong total number",3,runner2.counter.getCount());
		assertEquals("Thread 2 got wrong insert number",3,runner2.counter.getVerbCount("insert"));
	}
	
	@Test
	public void wiringThroughInjection() throws Exception {
		sqlLog.debug("insert foo into bar");
		
		assertEquals("Wrong total number",1,appender.getCounter().getCount());
		assertEquals("Wrong insert number",1,appender.getCounter().getVerbCount("insert"));
	}
	
	class TestLoggerRunnable implements Runnable {
		SQLCounter counter;
		int count;
		
		public TestLoggerRunnable(int count) {
			this.count=count;
		}

		@Override
			public void run() {
				for(int i=0; i < count; ++i) {
					Logger.getLogger("org.hibernate.SQL").debug("insert foo into bar");
				}
				counter=appender.getCounter();
			}
	}

	
	@Test
	public void threadLocalCountsUsingLogger() throws Exception {
		TestLoggerRunnable runner1=new TestLoggerRunnable(2);
		TestLoggerRunnable runner2=new TestLoggerRunnable(3);
		Thread t1=new Thread(runner1);
		Thread t2=new Thread(runner2);

		t1.start();
		t2.start();
		t1.join();
		t2.join();
		
		assertEquals("Thread 1 got wrong total number",2,runner1.counter.getCount());
		assertEquals("Thread 1 got wrong insert number",2,runner1.counter.getVerbCount("insert"));
		
		assertEquals("Thread 2 got wrong total number",3,runner2.counter.getCount());
		assertEquals("Thread 2 got wrong insert number",3,runner2.counter.getVerbCount("insert"));
	}
	
	
}
