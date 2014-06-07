package org.ozone.metrics;

import org.hibernate.SessionFactory;
import org.hibernate.stat.CollectionStatistics;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;

import com.yammer.metrics.MetricRegistry;
import static com.yammer.metrics.MetricRegistry.name;
import com.yammer.metrics.Gauge;

/**
 * Captures Hibernate metrics available through the Hibernate Statistics interface.  
 * All ${name} elements are the fully qualified class name or field name of the domain objects.
 */
public final class HibernateMetrics {

	private HibernateMetrics() {
		// Disallow instantiation
	}
	/**
	 * Pulls the statistics off the sessionFactory and instruments everything.
	 */
	public static void measureAllStatistics(final SessionFactory sessionFactory,MetricRegistry registry) {
		measureAllStatistics(sessionFactory.getStatistics(), registry);
	}

	/**
	 * Grabs global, collection, entity, and query statistics.
	 * @param statistics
	 * @param registry
	 */
	public static void measureAllStatistics(final Statistics statistics,MetricRegistry registry) {
		measureStatistics(statistics, registry);
		measureAllCollectionStatistics(statistics, registry);
		measureAllEntityStatistics(statistics, registry);
		measureAllQueryStatistics(statistics, registry);
	}
	
	/**
	 * Measures global statistics under "hibernate.global".
	 * @param statistics
	 * @param registry
	 */
	public static void measureStatistics(final Statistics statistics,MetricRegistry registry) {
		String globalScope="hibernate.global";
		statistics.setStatisticsEnabled(true);
		
		//-----------------------------------
		// Entity Statistics
		//-----------------------------------

		
		registry.register(name(globalScope,"entity.delete_counter"),new Gauge<Long>(){ 
			@Override public Long getValue() {return statistics.getEntityDeleteCount();	} 
		}); 

		registry.register(name(globalScope,"entity.insert_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getEntityInsertCount();	} 
		}); 

		registry.register(name(globalScope,"entity.load_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getEntityLoadCount();	} 
		}); 

		registry.register(name(globalScope,"entity.fetch_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getEntityFetchCount();	} 
		}); 

		registry.register(name(globalScope,"entity.update_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getEntityUpdateCount();	} 
		}); 

		//-----------------------------------
		// QueryCache Statistics
		//-----------------------------------
		
		registry.register(name(globalScope,"queryCache.hit_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getQueryCacheHitCount();	} 
		}); 

		registry.register(name(globalScope,"queryCache.miss_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getQueryCacheMissCount();	} 
		}); 

		registry.register(name(globalScope,"queryCache.put_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getQueryCachePutCount();	} 
		}); 

		//-----------------------------------
		// Second level cache statistics
		//-----------------------------------
		registry.register(name(globalScope,"secondLevelCache.hit_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getSecondLevelCacheHitCount();	} 
		}); 

		registry.register(name(globalScope,"secondLevelCache.miss_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getSecondLevelCacheMissCount();	} 
		}); 

		registry.register(name(globalScope,"secondLevelCache.put_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getSecondLevelCachePutCount();	} 
		}); 

		//-----------------------------------
		// Collection Statistics
		//-----------------------------------

		registry.register(name(globalScope,"collection.load_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getCollectionLoadCount();	} 
		}); 

		registry.register(name(globalScope,"collection.fetch_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getCollectionFetchCount();	} 
		}); 

		registry.register(name(globalScope,"collection.update_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getCollectionUpdateCount();	} 
		}); 

		registry.register(name(globalScope,"collection.remove_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getCollectionRemoveCount();	} 
		}); 

		registry.register(name(globalScope,"collection.recreate_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getCollectionRecreateCount();	} 
		}); 

		registry.register(name(globalScope,"preparedStatement_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getPrepareStatementCount();	} 
		}); 

		
		//-----------------------------------
		// Transactions stats
		//-----------------------------------
		

		//The number of completed transactions(failed and successful). 
		registry.register(name(globalScope,"transactions.total_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return statistics.getTransactionCount();} 
		}); 

		//The number of transactions completed without failure 
		registry.register(name(globalScope,"transactions.success_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return statistics.getSuccessfulTransactionCount();	} 
		}); 

		
		//-----------------------------------
		// Session Statistics
		//-----------------------------------

		//The number of sessions your code has opened. 
		registry.register(name(globalScope,"session.open_counter"),new Gauge<Long>(){ 
			@Override 	public Long getValue(){ return statistics.getSessionOpenCount();} 
		}); 

		//The number of sessions your code has closed. 
		registry.register(name(globalScope,"session.close_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return statistics.getSessionCloseCount();} 
		}); 
		
		//-----------------------------------
		// Query Statistics
		//-----------------------------------
		//Total number of queries executed. 
		registry.register(name(globalScope,"query.executed_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return statistics.getQueryExecutionCount();} 
		}); 

		//Time of the slowest query executed. 
		registry.register(name(globalScope,"query.executionTime_max"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics.getQueryExecutionMaxTime();} 
		}); 
		
		//-----------------------------------
		// Misc Statistics
		//-----------------------------------
		registry.register(name(globalScope,"closeStatement_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics. getCloseStatementCount();	} 
		}); 

		registry.register(name(globalScope,"optimisticFailure_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){ return statistics. getOptimisticFailureCount();	} 
		}); 

		registry.register(name(globalScope,"connection_request_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return statistics.getConnectCount(); } 
		}); 

		//Number of flushes done on the session(either by client code or by hibernate). 
		registry.register(name(globalScope,"flush_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return statistics.getFlushCount();	} 
		}); 

	}
	
	/**
	 * Measures statistics for all hibernate entities.  @see measureEntityStatistic for more info
	 * @param statistics
	 * @param registry
	 */
	public static void measureAllEntityStatistics(final Statistics statistics,MetricRegistry registry) {
		String[] entities=statistics.getEntityNames();
		for(String e:entities) {
			measureEntityStatistic(statistics.getEntityStatistics(e),registry);
		}
	}
	
	/**
	 * Measures statistics for a single entity under the namespace "hibernate.entity.${name}"
	 * @param entityStatistics
	 * @param registry
	 */
	public static void measureEntityStatistic(final EntityStatistics entityStatistics, MetricRegistry registry) {
		String metricScope=name("hibernate.entity",entityStatistics.getCategoryName());
		
		registry.register(name(metricScope,"delete_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return entityStatistics.getDeleteCount();	} 
		}); 
		registry.register(name(metricScope,"fetch_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return entityStatistics.getFetchCount();	} 
		}); 
		registry.register(name(metricScope,"insert_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return entityStatistics.getInsertCount();	} 
		}); 
		registry.register(name(metricScope,"load_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return entityStatistics.getLoadCount();	} 
		}); 
		registry.register(name(metricScope,"optimisticFailure_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return entityStatistics.getOptimisticFailureCount();	} 
		}); 
		
		registry.register(name(metricScope,"update_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return entityStatistics.getUpdateCount();	} 
		}); 
	}

	/**
	 * Measures all collections statistics.  Collections are the lazily loaded sets, lists, etc
	 * in domain objects.  
	 * 
	 * For example, a Person has a list of Groups.  The Person and Group are
	 * entities.  The list of Groups is a collection.  Saving a Group in the list is tracked under entities.
	 * Saving the list (and thus relationship) is tracked under collections.
	 * 
	 * @param statistics
	 * @param registry
	 */
	public static void measureAllCollectionStatistics(final Statistics statistics,MetricRegistry registry) {
		String[] entities=statistics.getCollectionRoleNames();
		for(String e:entities) {
			measureCollectionStatistic(statistics.getCollectionStatistics(e),registry);
		}
	}
	
	/**
	 * Measures one particular collection under the namespace "hibernate.collection.${name}".
	 * @param collectionStatistics
	 * @param registry
	 */
	public static void measureCollectionStatistic(final CollectionStatistics collectionStatistics, MetricRegistry registry) {
		String metricScope=name("hibernate.collection",collectionStatistics.getCategoryName());

		registry.register(name(metricScope,"fetch_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return collectionStatistics.getFetchCount();	} 
		}); 
		registry.register(name(metricScope,"load_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return collectionStatistics.getLoadCount();	} 
		}); 
		registry.register(name(metricScope,"recreate_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return collectionStatistics.getRecreateCount();	} 
		}); 
		registry.register(name(metricScope,"remove_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return collectionStatistics.getRemoveCount();	} 
		}); 
		registry.register(name(metricScope,"update_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return collectionStatistics.getUpdateCount();	} 
		}); 
	}
	
	/**
	 * Measures all prepared queries.  
	 * @param statistics
	 * @param registry
	 */
	public static void measureAllQueryStatistics(final Statistics statistics,MetricRegistry registry) {
		String[] entities=statistics.getQueries();
		for(String e:entities) {
			measureQueryStatistic(statistics.getQueryStatistics(e),registry);
		}
	}
	
	/**
	 * Measures an individual query in the "hibernate.query.${name}"
	 * @param queryStatistics
	 * @param registry
	 */
	public static void measureQueryStatistic(final QueryStatistics queryStatistics, MetricRegistry registry) {
		String metricScope=name("hibernate.query",queryStatistics.getCategoryName());

		registry.register(name(metricScope,"cache.hit_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return queryStatistics.getCacheHitCount();	} 
		}); 
		registry.register(name(metricScope,"cache.miss_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return queryStatistics.getCacheMissCount();	} 
		}); 
		registry.register(name(metricScope,"cache.put_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return queryStatistics.getCachePutCount();	} 
		}); 
		
		registry.register(name(metricScope,"execution.time_average"),new Gauge<Long>(){ 
			@Override public Long getValue(){return queryStatistics.getExecutionAvgTime();	} 
		}); 
		registry.register(name(metricScope,"execution.count_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return queryStatistics.getExecutionCount();	} 
		}); 
		registry.register(name(metricScope,"execution.time_max"),new Gauge<Long>(){ 
			@Override public Long getValue(){return queryStatistics.getExecutionMaxTime();	} 
		}); 
		registry.register(name(metricScope,"execution.time_min"),new Gauge<Long>(){ 
			@Override public Long getValue(){return queryStatistics.getExecutionMinTime();	} 
		}); 
		registry.register(name(metricScope,"execution.row_counter"),new Gauge<Long>(){ 
			@Override public Long getValue(){return queryStatistics.getExecutionRowCount();	} 
		}); 
	}	
}
