package org.ozone.metrics;


import static com.yammer.metrics.MetricRegistry.name;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;

import com.yammer.metrics.Counter;
import com.yammer.metrics.Meter;
import com.yammer.metrics.MetricRegistry;
import com.yammer.metrics.Timer;

/**
 * 
 * <h1>RECOMMEND TO AVOID THIS FILTER FOR NOW</h1>
 * It's needlessly chatty and overly coupled to spring.
 * <p>
 * Creates metrics both global and per-path metrics for the watched endpoints.  
 * Adds to the log4j MDC:<ul>
 * <li> request -- path and method  context on the way in
 * <li> status -- and duration_usec on the way out
 * </ul><p>
 * Logs "Request Complete" at info level so that the status and duration can be captured in logs.
 * <p>
 * Global Metrics:<ul>
 * <li>requests.active_counter -- counter for number of requests currently being handled
 * <li>requests.total_meter -- meters the rate of requests
 * </ul><p>
 * Per-endpoint (named URL.${PATH}, where "/" is replaced by "-" in paths) <ul>
 * <li>method_*_meter -- Meter of requests broken down by the verb (GET, PUT, POST, etc)
 * <li>status_*_meter -- Meter of each status returned (200, 404, etc)
 * <li>execution_timer -- Timer of how long the request took to serve
 * <li>invocation_meter -- Meter of how many times it has been invoked
 * <li>exception_meter -- Meter of how many times it threw an exception
 * </ul><p>
 * To use the MetricGatheringFilter, add this to web.xml:
 * <pre>{@code
 * <filter> <filter-name>metricsFilter</filter-name>
 *   <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class> 
 * </filter> 
 * <filter-mapping>
 *   <filter-name>metricsFilter</filter-name> 
 *   <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }</pre>
 * 
 * For an applicationContext.xml file, something like (not tested):
 * <pre>{@code
 * <bean id="metricRegistry" class="com.yammer.metrics.MetricRegistry">
 *    <constructor-arg><value>OWF</value></constructor-arg> 
 * </bean> 
 * <bean id="metricsFilter" class="org.ozone.metrics.MetricGatheringFilter" />
 * }</pre>
 */
public class MetricGatheringFilter implements Filter {

	@Autowired
	MetricRegistry registry;

	public MetricRegistry getRegistry() {
		return registry;
	}

	public void setRegistry(MetricRegistry registry) {
		this.registry = registry;
	}


	Counter activeRequests;
	Meter requests;

	Logger log=Logger.getLogger(this.getClass());
	EventLogger eventLog=new EventLogger(log);

	@PostConstruct
	public void initMetrics() {
		if(activeRequests==null) {
			activeRequests=registry.counter("requests.active_counter");
		}
		if(requests==null) {
			requests=registry.meter("requests.total_meter");
		}
		
	}
	
    @Override public void init(FilterConfig filterConfig) { /*noop*/}
    @Override public void destroy() { /*noop*/}
	
	/**

	 */
	
	@Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws ServletException, IOException 
    {
    	if(!(servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse)) {
    		filterChain.doFilter(servletRequest,servletResponse);
    		log.error("Request is " + servletRequest.getClass().getCanonicalName() + " and respons is " + servletResponse.getClass().getCanonicalName());
    	}
    	StatusCodeHttpServletResponse response=new StatusCodeHttpServletResponse((HttpServletResponse) servletResponse);
		HttpServletRequest request=(HttpServletRequest) servletRequest;
		
		String rootName="URL."+request.getRequestURI().replace('/', '-');
		
		// Global Metrics
		requests.mark();
		activeRequests.inc();
		
		//Request Metrics
//		registry.meter(name(rootName,"method_"+request.getMethod()+"_meter")).mark();
    	Timer.Context methodTimeContext=registry.timer(name(rootName,"execution_time")).time();

    	// Log4j context information
    	MDC.put("request", request.getRequestURI());
    	MDC.put("method", request.getMethod());
    	
        try {
            filterChain.doFilter(servletRequest, response);
        }  catch (ServletException|IOException t) {
			// increment the global exception counter for this class
			registry.meter(name(rootName,"invocation_meter")).mark();
			
			// increment the number of times this method has thrown an exception
			registry.meter(name(rootName,"exception_meter")).mark();
			
			throw t;
		} finally {
			// stop timer
			activeRequests.dec();
			long nanoTime=methodTimeContext.stop();
//			registry.meter(name(rootName,"status_"+response.statusCode+"_meter")).mark();

			// log the request
			MDC.put("status", response.statusCode);
			MDC.put("duration_usec",nanoTime/1000.0);
			eventLog.info().log("Request Complete");
		}
    }


    class StatusCodeHttpServletResponse extends HttpServletResponseWrapper {

        int statusCode=200;

        public StatusCodeHttpServletResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
    	public void setStatus(int sc) {
            super.setStatus(sc);
            statusCode = sc;
        }

        public void setStatus(int sc, String msg) {
            super.setStatus(sc, msg);
            statusCode = sc;
        }

        @Override
        public void sendError(int sc) throws IOException {
            super.sendError(sc);
            statusCode = sc;
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            super.sendError(sc, msg);
            statusCode = sc;
        }
    }
    
    
}

