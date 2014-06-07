package org.ozone.metrics;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import com.yammer.metrics.MetricRegistry;

import static com.yammer.metrics.MetricRegistry.*;
import com.yammer.metrics.Timer;

/**
 * AspectJ "around" advice for arbitrary classes. 
 * 
 * Per-endpoint (base of ${fqcn}.${method})
 *   execution_timer -- Timer of how long the request took to serve
 *   exception_meter -- Meter of how many times it threw an exception
 *   
 * Intended to be wired in via the spring.xml:
 * 
 * <bean id="metricRegistry" class="com.yammer.metrics.MetricRegistry">
 *    <constructor-arg><value>OWF</value></constructor-arg> 
 * </bean> 
 * <bean id="metricsAdvice" class="org.ozone.metrics.MetricsAdvice">
 * 
 * <aop:config>
 *    <aop:aspect id="metricsGatheringAspect" ref="metricsAdvice">
 *       <aop:around pointcut="pointcutSpec()" method="logMetrics"/>
 *    </aop:aspect>
 * <aop:config>
 *  
 */
public class MetricsAdvice  {
	
	@Autowired
	MetricRegistry registry;
	

	public Object logMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
		Signature signature=joinPoint.getSignature();
		Class<?> clazz=signature.getDeclaringType();
		String methodName="unknownMethod";
		if(signature instanceof MethodSignature) {
			Method method=((MethodSignature)signature).getMethod();
			if(method != null) {
				methodName=method.getName();
			}
		}
				
		Timer.Context methodTimeContext=registry.timer(name(clazz, methodName,"execution_timer")).time();
				
		try {
			return joinPoint.proceed();
		} catch (Throwable t) {
			// increment the number of times this method has thrown an exception
			registry.meter(name(clazz,methodName,"exception_meter")).mark();
			
			throw t;
		} finally {
			// stop timer
			methodTimeContext.stop();
		}
	}
	
}
