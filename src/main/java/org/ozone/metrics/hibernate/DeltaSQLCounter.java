package org.ozone.metrics.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Counts the change in values for a given SQLCounter.  This is used
 * to measure nested contexts.  Each level of nesting tracks the per-thread
 * count when the context was entered, and uses that as the baseline for reporting.
 */
public class DeltaSQLCounter implements SQLStatistics {

	long baseCount;
	Map<String,Long> baseVerbs;
	SQLCounter referenceCounter;
	
	/** Create a counter that reports changes in the supplied counter */
	public DeltaSQLCounter(SQLCounter counter) {
		baseCount=counter.count;
		baseVerbs=new HashMap<>(counter.verbs);
		referenceCounter=counter;
	}

	@Override
	public long getCount() {
		return referenceCounter.getCount()-baseCount;
	}
	
	@Override
	public long getVerbCount(String verb) {
		Long baseVerbCount=baseVerbs.get(verb);
		if(baseVerbCount == null) {
			baseVerbCount=0l;
		}
		
		return referenceCounter.getVerbCount(verb)-baseVerbCount;
	}
	
	@Override
	public Set<String> getVerbs() {
		return referenceCounter.getVerbs();
	}
}
