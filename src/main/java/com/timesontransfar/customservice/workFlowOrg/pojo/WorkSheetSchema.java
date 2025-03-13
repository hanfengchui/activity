package com.timesontransfar.customservice.workFlowOrg.pojo;

public class WorkSheetSchema {
	
	private int worksheetSchemaId;//工单模版标识
	private int wflId;//工作流程模板编号
	private int wflSeqNbr;//流向顺序
	private int tachId;//环节ID
	private int workSheetType;//工单模版类型
	private int workSheetCategory;//工单模板类别
	private String flowOptMethod="";//流程操作方法
	private String flowOptParam="";//流程操作参数
	private String sendXml="";//发送XML
	private String receiveXml="";//接受的XML
	private int prodConfig=0;//工单配置标志
	private int reverseWorksheetSchemaId=0;//接收工单模版标识
	
	
	
	
	/**
	 * @return flowOptMethod
	 */
	public String getFlowOptMethod() {
		return flowOptMethod;
	}
	/**
	 * @param flowOptMethod 要设置的 flowOptMethod
	 */
	public void setFlowOptMethod(String flowOptMethod) {
		this.flowOptMethod = flowOptMethod;
	}
	/**
	 * @return flowOptParam
	 */
	public String getFlowOptParam() {
		return flowOptParam;
	}
	/**
	 * @param flowOptParam 要设置的 flowOptParam
	 */
	public void setFlowOptParam(String flowOptParam) {
		this.flowOptParam = flowOptParam;
	}
	/**
	 * @return prodConfig
	 */
	public int getProdConfig() {
		return prodConfig;
	}
	/**
	 * @param prodConfig 要设置的 prodConfig
	 */
	public void setProdConfig(int prodConfig) {
		this.prodConfig = prodConfig;
	}
	/**
	 * @return receiveXml
	 */
	public String getReceiveXml() {
		return receiveXml;
	}
	/**
	 * @param receiveXml 要设置的 receiveXml
	 */
	public void setReceiveXml(String receiveXml) {
		this.receiveXml = receiveXml;
	}
	/**
	 * @return reverseWorksheetSchemaId
	 */
	public int getReverseWorksheetSchemaId() {
		return reverseWorksheetSchemaId;
	}
	/**
	 * @param reverseWorksheetSchemaId 要设置的 reverseWorksheetSchemaId
	 */
	public void setReverseWorksheetSchemaId(int reverseWorksheetSchemaId) {
		this.reverseWorksheetSchemaId = reverseWorksheetSchemaId;
	}
	/**
	 * @return sendXml
	 */
	public String getSendXml() {
		return sendXml;
	}
	/**
	 * @param sendXml 要设置的 sendXml
	 */
	public void setSendXml(String sendXml) {
		this.sendXml = sendXml;
	}
	/**
	 * @return tachId
	 */
	public int getTachId() {
		return tachId;
	}
	/**
	 * @param tachId 要设置的 tachId
	 */
	public void setTachId(int tachId) {
		this.tachId = tachId;
	}
	/**
	 * @return wflId
	 */
	public int getWflId() {
		return wflId;
	}
	/**
	 * @param wflId 要设置的 wflId
	 */
	public void setWflId(int wflId) {
		this.wflId = wflId;
	}
	/**
	 * @return wflSeqNbr
	 */
	public int getWflSeqNbr() {
		return wflSeqNbr;
	}
	/**
	 * @param wflSeqNbr 要设置的 wflSeqNbr
	 */
	public void setWflSeqNbr(int wflSeqNbr) {
		this.wflSeqNbr = wflSeqNbr;
	}
	/**
	 * @return workSheetCategory
	 */
	public int getWorkSheetCategory() {
		return workSheetCategory;
	}
	/**
	 * @param workSheetCategory 要设置的 workSheetCategory
	 */
	public void setWorkSheetCategory(int workSheetCategory) {
		this.workSheetCategory = workSheetCategory;
	}
	/**
	 * @return worksheetSchemaId
	 */
	public int getWorksheetSchemaId() {
		return worksheetSchemaId;
	}
	/**
	 * @param worksheetSchemaId 要设置的 worksheetSchemaId
	 */
	public void setWorksheetSchemaId(int worksheetSchemaId) {
		this.worksheetSchemaId = worksheetSchemaId;
	}
	/**
	 * @return workSheetType
	 */
	public int getWorkSheetType() {
		return workSheetType;
	}
	/**
	 * @param workSheetType 要设置的 workSheetType
	 */
	public void setWorkSheetType(int workSheetType) {
		this.workSheetType = workSheetType;
	}
	
	
	
	

}
