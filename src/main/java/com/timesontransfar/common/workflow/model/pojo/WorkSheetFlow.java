package com.timesontransfar.common.workflow.model.pojo;

public class WorkSheetFlow implements Cloneable,java.io.Serializable{
	private long wsFlowRuleId;

	private long wsNbr;

	private long worksheetSchemaId;

	private long regionId;

	private long ruleId;

	private long itemId;

	private String itemValue;

	private String flowOrgId;

	private long tacheId;

	public WorkSheetFlow() {
		super();
		//  自动生成构造函数存根
	}

	public String getFlowOrgId() {
		return flowOrgId;
	}

	public void setFlowOrgId(String flowOrgId) {
		this.flowOrgId = flowOrgId;
	}

	public long getItemId() {
		return itemId;
	}

	public void setItemId(long itemId) {
		this.itemId = itemId;
	}

	public String getItemValue() {
		return itemValue;
	}

	public void setItemValue(String itemValue) {
		this.itemValue = itemValue;
	}

	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}

	public long getRuleId() {
		return ruleId;
	}

	public void setRuleId(long ruleId) {
		this.ruleId = ruleId;
	}

	public long getTacheId() {
		return tacheId;
	}

	public void setTacheId(long tacheId) {
		this.tacheId = tacheId;
	}

	public long getWorksheetSchemaId() {
		return worksheetSchemaId;
	}

	public void setWorksheetSchemaId(long worksheetSchemaId) {
		this.worksheetSchemaId = worksheetSchemaId;
	}

	public long getWsFlowRuleId() {
		return wsFlowRuleId;
	}

	public void setWsFlowRuleId(long wsFlowRuleId) {
		this.wsFlowRuleId = wsFlowRuleId;
	}

	public long getWsNbr() {
		return wsNbr;
	}

	public void setWsNbr(long wsNbr) {
		this.wsNbr = wsNbr;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		WorkSheetFlow copy = null;
		try {
			copy = (WorkSheetFlow)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}

}
