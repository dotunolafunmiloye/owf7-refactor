package org.codehaus.groovy.grails.plugins.quartz;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.ClassUtils;

import java.util.TimeZone;
import java.util.Date;
import java.util.Map;
import java.text.ParseException;

/**
 * TODO: write javadoc
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 */
public class CustomTriggerFactoryBean implements FactoryBean, InitializingBean, BeanNameAware  {
  private Class triggerClass;
  private Trigger customTrigger;
  private JobDetail jobDetail;
  private String beanName;

  private Map attributes;

  public void afterPropertiesSet() throws ParseException {
      customTrigger = (Trigger) BeanUtils.instantiateClass(triggerClass);
      BeanWrapper customTriggerWrapper = PropertyAccessorFactory.forBeanPropertyAccess(customTrigger);
      customTriggerWrapper.setPropertyValues(attributes);

      if (jobDetail != null) {
          customTrigger.setJobName(jobDetail.getName());
          customTrigger.setJobGroup(jobDetail.getGroup());
      }
  }

  /**
   * {@inheritDoc}
   * @see org.springframework.beans.factory.FactoryBean#getObject()
   */
  public Object getObject() throws Exception {
      return customTrigger;
  }

  /**
   * {@inheritDoc}
   * @see org.springframework.beans.factory.FactoryBean#getObjectType()
   */
  public Class getObjectType() {
      return triggerClass;
  }

  /**
   * {@inheritDoc}
   * @see org.springframework.beans.factory.FactoryBean#isSingleton()
   */
  public boolean isSingleton() {
      return true;
  }

  /**
   * {@inheritDoc}
   * @see org.springframework.beans.factory.BeanNameAware#setBeanName(String)
   */
  public void setBeanName(String name) {
      this.beanName = name;
  }

  public void setJobDetail(JobDetail jobDetail) {
      this.jobDetail = jobDetail;
  }

  public void setTriggerClass(Class triggerClass) {
    this.triggerClass = triggerClass;
  }

  public void setAttributes(Map attributes) {
    this.attributes = attributes;
  }
}
