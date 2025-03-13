package com.timesontransfar.common.workflow.model.pojo;

public class WorkFlowNodeCondition {
	private long condNo;
	private long condType;
	private long methodCode;
	private long seq;
	private String condName;
	private long condGuid;
	private String paramKey;
	private long paramCom;
	private String paramValue;
	private String paramType;
	public final long getCondGuid() {
		return condGuid;
	}
	public final void setCondGuid(long condGuid) {
		this.condGuid = condGuid;
	}
	public final String getCondName() {
		return condName;
	}
	public final void setCondName(String condName) {
		this.condName = condName;
	}
	public final long getCondNo() {
		return condNo;
	}
	public final void setCondNo(long condNo) {
		this.condNo = condNo;
	}
	public final long getCondType() {
		return condType;
	}
	public final void setCondType(long condType) {
		this.condType = condType;
	}
	public final long getMethodCode() {
		return methodCode;
	}
	public final void setMethodCode(long methodCode) {
		this.methodCode = methodCode;
	}
	public final long getParamCom() {
		return paramCom;
	}
	public final void setParamCom(long paramCom) {
		this.paramCom = paramCom;
	}
	public final String getParamKey() {
		return paramKey;
	}
	public final void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}
	public final String getParamType() {
		return paramType;
	}
	public final void setParamType(String paramType) {
		this.paramType = paramType;
	}
	public final String getParamValue() {
		return paramValue;
	}
	public final void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}
	public final long getSeq() {
		return seq;
	}
	public final void setSeq(long seq) {
		this.seq = seq;
	}



}
