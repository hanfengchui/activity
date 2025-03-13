package com.timesontransfar.common.workflow.model.pojo;

public class EsysMsgTmp implements Cloneable,java.io.Serializable{
	private String tempId;

	private String instanceId;

	private String nodeInstanceId;

	private String msgType;

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getNodeInstanceId() {
		return nodeInstanceId;
	}

	public void setNodeInstanceId(String nodeInstanceId) {
		this.nodeInstanceId = nodeInstanceId;
	}

	public String getTempId() {
		return tempId;
	}

	public void setTempId(String tempId) {
		this.tempId = tempId;
	}

	public EsysMsgTmp() {
		super();
		// 自动生成构造函数存根
	}
	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		EsysMsgTmp copy = null;
		try {
			copy = (EsysMsgTmp)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
}
