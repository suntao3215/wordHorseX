package org.sun.encrypted.init;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

public class InstantiationTracingBeanPostProcessor implements BeanPostProcessor {

	public Object postProcessAfterInitialization(Object arg0, String arg1)
			throws BeansException {
		return arg0;
	}

	public Object postProcessBeforeInitialization(Object bean, String arg1)
			throws BeansException {
		if (bean instanceof HibernateMappingUtil) {
			((HibernateMappingUtil) bean).loadMapping();
			// ((HibernateMappingUtil) arg0).printMap();
		}
		return bean;
	}

}
