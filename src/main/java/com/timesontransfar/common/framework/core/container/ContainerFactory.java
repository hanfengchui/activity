/**
 * Used for Create a Container
 * @author Ation Row
 * copyright timesontransfer
 */
package com.timesontransfar.common.framework.core.container;


import com.timesontransfar.common.framework.core.container.impl.SpringContainer;

public class ContainerFactory {
	public ContainerFactory(){
		//构造函数
	}
	/**
	 * return Container Instance;
	 *
	 * @return IContainer
	 */
	public IContainer getContainer(){
		IContainer container = new SpringContainer();
		return container;
	}


}
