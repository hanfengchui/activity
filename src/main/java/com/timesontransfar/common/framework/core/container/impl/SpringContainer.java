/**
 * Implementing the IContainer interface and Creating a Spring Container 
 * Access Instance.
 */

package com.timesontransfar.common.framework.core.container.impl;

import com.timesontransfar.common.framework.core.container.IContainer;
import org.springframework.context.ApplicationContext;


public class SpringContainer implements IContainer {
	private ApplicationContext applicationContext;

	public Object getBean(String name) {
		
		return this.applicationContext.getBean(name);
	}

	public Object getBean(Class type) {
		// Auto-generated method stub
		return null;
	}

	public Object autoWireComponent(Object src) {
		// Auto-generated method stub
		return null;
	}
	
	public void setConfig(String config){ //空白方法
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}
