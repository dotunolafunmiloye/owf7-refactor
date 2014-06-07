package org.ozone.metrics.hibernate;

import java.util.Locale;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * This is an appender that measures the number of sql statements in total and by type (insert, select, delete, etc)
 * that occur in on a thread.
 */
public class HibernateSQLCounterAppender extends AppenderSkeleton {

	public static final String APPENDER_NAME = "sqlCounter";
	
	/**
	 * Static class to prevent the implicit this pointer from hanging around
	 */
	static private class SqlCounterThreadLocal extends ThreadLocal<SQLCounter> {
		@Override
		protected SQLCounter initialValue() {
			return new SQLCounter();
		}
	}
	
	ThreadLocal<SQLCounter> counter=new SqlCounterThreadLocal();
	
	/**
	 * Extracts the verb from the log statement and increments the thread counter.
	 */
	@Override
	protected void append(LoggingEvent event) {
		String msg=event.getMessage().toString();
		int pos=msg.indexOf(' ');
		if(pos >0) {
			String sqlVerb=msg.substring(0, pos).toLowerCase(Locale.getDefault());
			counter.get().inc(sqlVerb);
		}
	}
	
	@Override
	public boolean requiresLayout() {
		return false;
	}
	
	@Override
	public void close() {
		// noop
	}
	
	/**
	 * Gets this thread's counter.
	 * @return statement counter for the current thread
	 */
	public SQLCounter getCounter() {
		return counter.get();
	}
	
	/** 
	 * No reason to use this, since the DeltaSQLCounter handles nexted contexts so long as you
	 * don't reset the per-thread counter. 
	*/
	@Deprecated
	public void resetCounter() {
		counter.get().reset();
	}
	
	static HibernateSQLCounterAppender appender=null;
	static ReadWriteLock appenderLock=new ReentrantReadWriteLock(false);
	
	/** 
	 * Convenience method to turn on hibernate sql logging and add this appender at the right point.
	 * @return reference to the HibernateSQLCounterAppender
	 */
	public static HibernateSQLCounterAppender inject() {
		appenderLock.writeLock().lock();
		try {
			Logger sqlLogger=Logger.getLogger("org.hibernate.SQL");
			appender=new HibernateSQLCounterAppender();
			appender.setName(APPENDER_NAME);
			appender.setThreshold(Level.ALL);
			
			sqlLogger.addAppender(appender);
			sqlLogger.setAdditivity(false);
			sqlLogger.setLevel(Level.DEBUG);
			
			return appender;
		} finally {
			appenderLock.writeLock().unlock();
		}
	}

	/**
	 * If there's a HibernateSQLCounterAppender configured, this returns that instance.
	 * @return
	 */
	public static HibernateSQLCounterAppender get() {
		appenderLock.readLock().lock();
		try {
			return appender;
		} finally {
			appenderLock.readLock().unlock();
		}
	}


	public static SQLStatistics startCounter() {
		appenderLock.readLock().lock();
		try {
			// if no appender, just mock it out with zero counts
			if(appender==null) {
				return new SQLCounter();
			}
			return new DeltaSQLCounter(appender.getCounter());
		} finally {
			appenderLock.readLock().unlock();
		}
	}
	
}
