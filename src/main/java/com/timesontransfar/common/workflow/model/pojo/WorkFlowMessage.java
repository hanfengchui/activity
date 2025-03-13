/*
 * 创建日期 2006-8-26
 *
 *  要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

import java.io.Serializable;
import java.util.Map;

/**
 * @author ationr
 *
 * 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
@SuppressWarnings("rawtypes")
public class WorkFlowMessage implements Serializable,Cloneable{
	private String instanceId;
		
	private Map outParams;
	
	private String msgType;

	/**
	 * @return 返回 instanceId。
	 */
	public String getInstanceId() {
		return instanceId;
	}
	/**
	 * @param instanceId 要设置的 instanceId。
	 */
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	/**
	 * @return 返回 msgType。
	 */
	public String getMsgType() {
		return msgType;
	}
	/**
	 * @param msgType 要设置的 msgType。
	 */
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	/**
	 * @return 返回 outParams。
	 */
	public Map getOutParams() {
		return outParams;
	}
	/**
	 * @param outParams 要设置的 outParams。
	 */
	public void setOutParams(Map outParams) {
		this.outParams = outParams;
	}
	
	protected void finalize() throws Throwable{
		super.finalize();
		if(this.outParams!=null){
			this.outParams.clear();			
		}
	}

}
