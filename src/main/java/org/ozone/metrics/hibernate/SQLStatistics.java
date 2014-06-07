package org.ozone.metrics.hibernate;

import java.util.Set;

/**
 * Interface for any class that returns the total and per-verb sql statement counts.
 */
public interface SQLStatistics {
	long getCount();
	long getVerbCount(String verb);
	Set<String> getVerbs();

}
