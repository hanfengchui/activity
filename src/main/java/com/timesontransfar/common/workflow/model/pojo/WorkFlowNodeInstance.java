/*
 * 创建日期 2006-8-15
 *
 *  要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

import java.sql.Timestamp;
import java.util.List;

import com.alibaba.fastjson.JSON;

/**
 * @author ationr
 *
 *  要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
@SuppressWarnings("rawtypes")
public class WorkFlowNodeInstance implements Cloneable,java.io.Serializable{
	private String instanceid;

	private long tacheId;

	private String wfguid;

	private Timestamp indatetime;

	private long costtime;

	private Timestamp outdatetime;

	private String getparament;

	private long regionId;

	private WorkFlowNode workFlowNode;

	private List parentid;

	private List childrenid;

	private String wfinstanceid;

	private long nodeType;

	public static final long NODE_TYPE_REACH = 0;
	public static final long NODE_TYPE_DEALING = 1;
	public static final long NODE_TYPE_OVER = 2;
	public static final long NODE_TYPE_RUBBISH = 3;


	public long getNodeType() {
		return nodeType;
	}

	public void setNodeType(long nodeType) {
		this.nodeType = nodeType;
	}

	public String getWfinstanceid() {
		return wfinstanceid;
	}

	public void setWfinstanceid(String wfinstanceid) {
		this.wfinstanceid = wfinstanceid;
	}

	public List getChildrenid() {
		return childrenid;
	}

	public void setChildrenid(List childrenid) {
		this.childrenid = childrenid;
	}

	public List getParentid() {
		return parentid;
	}

	public void setParentid(List parentid) {
		this.parentid = parentid;
	}

	public WorkFlowNode getWorkFlowNode() {
		return workFlowNode;
	}

	public void setWorkFlowNode(WorkFlowNode workFlowNode) {
		this.workFlowNode = workFlowNode;
	}

	public long getCosttime() {
		return costtime;
	}

	public void setCosttime(long costtime) {
		this.costtime = costtime;
	}

	public String getGetparament() {
		return getparament;
	}

	public void setGetparament(String getparament) {
		this.getparament = getparament;
	}

	public Timestamp getIndatetime() {
		return indatetime;
	}

	public void setIndatetime(Timestamp indatetime) {
		this.indatetime = indatetime;
	}

	public String getInstanceid() {
		return instanceid;
	}

	public void setInstanceid(String instanceid) {
		this.instanceid = instanceid;
	}

	public Timestamp getOutdatetime() {
		return outdatetime;
	}

	public void setOutdatetime(Timestamp outdatetime) {
		this.outdatetime = outdatetime;
	}

	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}

	public long getTacheId() {
		return tacheId;
	}

	public void setTacheId(long tacheId) {
		this.tacheId = tacheId;
	}

	public String getWfguid() {
		return wfguid;
	}

	public void setWfguid(String wfguid) {
		this.wfguid = wfguid;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() {
		if(this.childrenid!=null) this.childrenid.clear();
		if(this.parentid!=null) this.parentid.clear();
	}

	public Object clone() {
		WorkFlowNodeInstance copy = null;
		try {
			copy = (WorkFlowNodeInstance)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
	
	public String toString() {
		return JSON.toJSONString(this);
	}
}
