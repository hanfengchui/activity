package com.timesontransfar.common.framework.core.dynamicdisplay.service;

import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubFunction;

public interface IFunctionAmender {
	/**
	 *
	 * @param function
	 * @return
	 */
	public PubFunction amendFunction(PubFunction function);
	/**
	 * 根据FuncID获得Function
	 * @param id
	 * @return
	 */
	public PubFunction getFunction(String id);

}
