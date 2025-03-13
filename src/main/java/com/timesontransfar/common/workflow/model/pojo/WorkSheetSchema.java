package com.timesontransfar.common.workflow.model.pojo;

public class WorkSheetSchema implements Cloneable,java.io.Serializable{
	private long worksheetSchemaId;

	private long worksheetType;

	private long worksheetCategory;

	private String flowOptMethod;

	private String flowOptParam;

	private String sendXmlSchema;

	private String receiveXmlSchema;

	private int prodConfigFlag;

	private long reverseWorksheetSchemaId;

	private WorkFlowNode workFlowNode;

	public String getFlowOptMethod() {
		return flowOptMethod;
	}

	public void setFlowOptMethod(String flowOptMethod) {
		this.flowOptMethod = flowOptMethod;
	}

	public String getFlowOptParam() {
		return flowOptParam;
	}

	public void setFlowOptParam(String flowOptParam) {
		this.flowOptParam = flowOptParam;
	}

	public int getProdConfigFlag() {
		return prodConfigFlag;
	}

	public void setProdConfigFlag(int prodConfigFlag) {
		this.prodConfigFlag = prodConfigFlag;
	}

	public String getReceiveXmlSchema() {
		return receiveXmlSchema;
	}

	public void setReceiveXmlSchema(String receiveXmlSchema) {
		this.receiveXmlSchema = receiveXmlSchema;
	}

	public long getReverseWorksheetSchemaId() {
		return reverseWorksheetSchemaId;
	}

	public void setReverseWorksheetSchemaId(long reverseWorksheetSchemaId) {
		this.reverseWorksheetSchemaId = reverseWorksheetSchemaId;
	}

	public String getSendXmlSchema() {
		return sendXmlSchema;
	}

	public void setSendXmlSchema(String sendXmlSchema) {
		this.sendXmlSchema = sendXmlSchema;
	}

	public WorkFlowNode getWorkFlowNode() {
		return workFlowNode;
	}

	public void setWorkFlowNode(WorkFlowNode workFlowNode) {
		this.workFlowNode = workFlowNode;
	}

	public long getWorksheetCategory() {
		return worksheetCategory;
	}

	public void setWorksheetCategory(long worksheetCategory) {
		this.worksheetCategory = worksheetCategory;
	}

	public long getWorksheetSchemaId() {
		return worksheetSchemaId;
	}

	public void setWorksheetSchemaId(long worksheetSchemaId) {
		this.worksheetSchemaId = worksheetSchemaId;
	}

	public long getWorksheetType() {
		return worksheetType;
	}

	public void setWorksheetType(long worksheetType) {
		this.worksheetType = worksheetType;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		WorkSheetSchema copy = null;
		try {
			copy = (WorkSheetSchema)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
}
