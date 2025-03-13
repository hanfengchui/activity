/*
 * 创建日期 2006-8-15
 *
 *  要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

import java.util.List;

import com.alibaba.fastjson.JSON;

/**
 * @author ationr
 *
 */
@SuppressWarnings("rawtypes")
public class WorkFlowInstance implements Cloneable,java.io.Serializable{
	private WorkFlowSchema workFlowSchema;

	private String instanceid;

	private String wfguid;

	private List curNode;//此处应是多个节点的集合,对一个工作流实例来说,当前可能会存在多个等待的工作流节点

	private long regionId;

	private boolean end;//流程是否结束

	/**
	 * @return 返回 end。
	 */
	public boolean isEnd() {
		return end;
	}
	/**
	 * @param end 要设置的 end。
	 */
	public void setEnd(boolean end) {
		this.end = end;
	}

	public List getCurNode() {
		return curNode;
	}

	public void setCurNode(List curNode) {
		this.curNode = curNode;
	}
	/**
	 * 往当前节点集合中添加节点
	 * @param curNodeInstance
	 */
	public void addCurNode(WorkFlowNodeInstance curNodeInstance){
		this.curNode.add(curNodeInstance);
	}

	public String getInstanceid() {
		return instanceid;
	}

	public void setInstanceid(String instanceid) {
		this.instanceid = instanceid;
	}

	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}

	public String getWfguid() {
		return wfguid;
	}

	public void setWfguid(String wfguid) {
		this.wfguid = wfguid;
	}

	public WorkFlowSchema getWorkFlowSchema() {
		return workFlowSchema;
	}

	public void setWorkFlowSchema(WorkFlowSchema workFlowSchema) {
		this.workFlowSchema = workFlowSchema;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() {
		if(this.curNode!=null) curNode.clear();
	}

	public Object clone() {
		WorkFlowInstance copy = null;
		try {
			copy = (WorkFlowInstance)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
	
	public String toString() {
		return JSON.toJSONString(this);
	}
}
