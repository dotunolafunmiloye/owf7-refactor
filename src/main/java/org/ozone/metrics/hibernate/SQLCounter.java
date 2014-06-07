package org.ozone.metrics.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple, always incrementing counter for statements.
 */
public class SQLCounter implements SQLStatistics{
	long count=0;
	Map<String,Long> verbs=new HashMap<String, Long>();

	/** Increment the global and specific verb counters by one */
	public void inc(String verb) {
		count++;
		Long c=verbs.get(verb);
		verbs.put(verb, c!=null?c +1:1);
	}
	
	/** sets all counters back to zero */
	public void reset() {
		count=0;
		verbs.clear();
	}
	
	/** Gets the count for a given verb.  Returns zero for unknown verbs.  */
	public long getVerbCount(String verb) {
		Long c=verbs.get(verb);
		return c==null?0:c.longValue();
	}
	
	/** Global count */
	public long getCount() {
		return count;
	}
	
	/** Returns the list of verbs that have values */
	public Set<String> getVerbs() {
		return verbs.keySet();
	}
}
