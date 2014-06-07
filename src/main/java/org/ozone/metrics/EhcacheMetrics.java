package org.ozone.metrics;

import static com.yammer.metrics.MetricRegistry.name;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.event.CacheManagerEventListener;
import net.sf.ehcache.statistics.LiveCacheStatistics;

import com.yammer.metrics.Gauge;
import com.yammer.metrics.MetricRegistry;

/**
 * Creates metrics for older versions of EhCache.
 * Metrics are named : ehcache.${cacheManagerName}.${cacheName}.${metric}
 */
public final class EhcacheMetrics {
	private EhcacheMetrics() {
		// Disallow instantiation
	}
	
	/**
	 * Adds metrics for all caches in all currently registered cache managers.
	 * 
	 * @param metricRegistry Registry to add the metrics to
	 */
	public static void measureAll(MetricRegistry metricRegistry) {
		for(CacheManager cm: CacheManager.ALL_CACHE_MANAGERS) {
			measureAllCaches(cm, metricRegistry);
		}
	}
	
	/**
	 * Measure all caches in the supplied cache manager.  If a new cache is added, it will be
	 * automatically measured.
	 * <p> 
	 * Does NOT currently disable measuring a cache that is removed.
	 * 
	 * @param cacheManager Measure all caches in this manager and listen for new caches.
	 * @param metricRegistry Put metrics here.
	 */
	public static void measureAllCaches(final CacheManager cacheManager,final MetricRegistry metricRegistry) {
		for(String n: cacheManager.getCacheNames()) {
			measureCache(cacheManager.getCache(n),metricRegistry);
		}
		
		cacheManager.getCacheManagerEventListenerRegistry().registerListener(new CacheManagerEventListener() {
			@Override	public void notifyCacheRemoved(String arg0) {
				// TODO Remove the cache info, in case this actually happens
			}
			@Override public void notifyCacheAdded(String cacheName) {
				measureCache(cacheManager.getCache(cacheName),metricRegistry);
			}			
			@Override public void init() throws CacheException {/*noop*/}
			@Override public Status getStatus() {	return Status.STATUS_ALIVE;	}
			@Override public void dispose() throws CacheException {/*noop*/}
		});
	}
	
	/**
	 * Measures an individual cache.
	 * 
	 * @param cache
	 * @param metricRegistry
	 */
	public static void measureCache(final Ehcache cache, MetricRegistry metricRegistry) {
		String metricScope=name("ehcache",cache.getCacheManager().getName(),cache.getName());
		final LiveCacheStatistics statistics=cache.getLiveCacheStatistics();
		cache.setStatisticsEnabled(true);
		
		metricRegistry.register(name(metricScope,"getTimeMs_average"),new Gauge<Float>(){ 
			@Override public Float getValue() {return statistics.getAverageGetTimeMillis();	} 
		}); 
		metricRegistry.register(name(metricScope,"hit_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getCacheHitCount();	} 
		}); 
		metricRegistry.register(name(metricScope,"miss_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getCacheMissCount();	} 
		}); 
		metricRegistry.register(name(metricScope,"expiredMiss_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getCacheMissCountExpired();	} 
		}); 
		metricRegistry.register(name(metricScope,"evicted_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getEvictedCount();	} 
		}); 
		metricRegistry.register(name(metricScope,"expired_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getExpiredCount();	} 
		}); 
		metricRegistry.register(name(metricScope,"inMemoryHits_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getInMemoryHitCount();	} 
		}); 
		metricRegistry.register(name(metricScope,"inMemorySize_max"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getInMemorySize();	} 
		}); 
		metricRegistry.register(name(metricScope,"onDiskHits_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getOnDiskHitCount();	} 
		}); 
		metricRegistry.register(name(metricScope,"put_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getPutCount();	}
		});
		metricRegistry.register(name(metricScope,"removed_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getRemovedCount();	} 
		}); 
		metricRegistry.register(name(metricScope,"size_average"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getSize();	} 
		}); 
		metricRegistry.register(name(metricScope,"update_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getUpdateCount();	} 
		}); 
	}
}
