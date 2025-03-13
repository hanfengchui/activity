package com.timesontransfar.common.framework.core.container;
@SuppressWarnings("rawtypes")
public interface IContainer {
	public Object getBean(String name);
	
	public Object getBean(Class type);
	
	public Object autoWireComponent(Object src);
	
	public void setConfig(String config);

}

