package org.ozone.metrics.hibernate;

import static com.yammer.metrics.MetricRegistry.name;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;

import com.yammer.metrics.MetricRegistry;

/**
 * AspectJ "around" advice for arbitrary classes
 * 
 * Per-endpoint (base of ${fqcn}.${method})
 *   sqlStatement.all -- A histogram of how many sql statements were invoked per call.
 *   sqlStatement.${verb} -- A histogram broken down by the sql verb (select, update, insert, etc) 
 * <p>
 * Intended to be wired in via the spring.xml:
 * <pre>{@code
 * <bean id="metricRegistry" class="com.yammer.metrics.MetricRegistry">
 *    <constructor-arg><value>OWF</value></constructor-arg> 
 * </bean> 
 * <bean id="hibernateSQLCountAdvice" class="org.ozone.metrics.hibernate.HibernateSQLCountAdvice">
 * 
 * <aop:config>
 *    <aop:aspect id="hibernateSQLCountAdvice" ref="metricsAdvice">
 *       <aop:around pointcut="pointcutSpec()" method="logMetrics"/>
 *    </aop:aspect>
 * <aop:config>
 * }</pre>
 */
public class HibernateSQLCountAdvice  {
	
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
				
		SQLStatistics c=HibernateSQLCounterAppender.startCounter();		
		
		try {
			return joinPoint.proceed();
		} finally {
			// measure info
			registry.histogram(name(clazz, methodName,"sqlStatements.all")).update(c.getCount());
			
			for(String v: c.getVerbs()) {
				registry.histogram(name(clazz, methodName,v)).update(c.getVerbCount(v));
			}
		}
	}
	
}
