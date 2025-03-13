package com.timesontransfar.common.workflow.model.pojo;

public class NodeOParamConfig implements Cloneable, java.io.Serializable {

	public NodeOParamConfig() {
		super();
		// 自动生成构造函数存根
	}

	private String paramId;

	private long tacheId;

	private long paramNo;

	private String paramName;

	private String paramKey;

	private short paramCom;

	private short paramJoin;

	private String paramValue;

	private String paramRemark;

	private short paramType; // 新增参数类型，此处需要在模型中添加相关内容

	private long condno;

	private long nodemethodtype;

	private long methodcode;

	public long getCondno() {
		return condno;
	}

	public void setCondno(long condno) {
		this.condno = condno;
	}

	public long getMethodcode() {
		return methodcode;
	}

	public void setMethodcode(long methodcode) {
		this.methodcode = methodcode;
	}

	public long getNodemethodtype() {
		return nodemethodtype;
	}

	public void setNodemethodtype(long nodemethodtype) {
		this.nodemethodtype = nodemethodtype;
	}

	/**
	 * @return 返回 paramType。
	 */
	public short getParamType() {
		return paramType;
	}

	/**
	 * @param paramType
	 *            要设置的 paramType。
	 */
	public void setParamType(short paramType) {
		this.paramType = paramType;
	}

	private WorkFlowNode workFlowNode;

	public short getParamCom() {
		return paramCom;
	}

	public void setParamCom(short paramCom) {
		this.paramCom = paramCom;
	}

	public String getParamId() {
		return paramId;
	}

	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	public short getParamJoin() {
		return paramJoin;
	}

	public void setParamJoin(short paramJoin) {
		this.paramJoin = paramJoin;
	}

	public String getParamKey() {
		return paramKey;
	}

	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public long getParamNo() {
		return paramNo;
	}

	public void setParamNo(long paramNo) {
		this.paramNo = paramNo;
	}

	public String getParamRemark() {
		return paramRemark;
	}

	public void setParamRemark(String paramRemark) {
		this.paramRemark = paramRemark;
	}

	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	public long getTacheId() {
		return tacheId;
	}

	public void setTacheId(long tacheId) {
		this.tacheId = tacheId;
	}

	public WorkFlowNode getWorkFlowNode() {
		return workFlowNode;
	}

	public void setWorkFlowNode(WorkFlowNode workFlowNode) {
		this.workFlowNode = workFlowNode;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		NodeOParamConfig copy = null;
		try {
			copy = (NodeOParamConfig)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}

}
