package com.timesontransfar.common.framework.core.dynamicdisplay.service;

import java.util.Map;

import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubDisplayCtrl;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubEvent;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubFunction;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubQueryData;

@SuppressWarnings("rawtypes")
public interface IWebDynamicDisplay {

	/**
	 *
	 * @param id
	 * @return
	 */
	public PubFunction getFunction(String id);

	/**
	 *
	 * @param funcId
	 * @return
	 */
	public PubDisplayCtrl[] getDisplayCtrl(String funcId);

	/**
	 *
	 * @param funcId
	 * @param ctrlId
	 * @return PubEvent[]
	 */
	public PubEvent[] getEvent(String funcId);

	/**
	 *
	 * @param funcId
	 * @param inParams
	 * @return
	 */
	public PubQueryData getData(String funcId, Map inParams);

}
