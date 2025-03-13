package com.timesontransfar.common.authorization.web.webobject;

import java.io.Serializable;

public class WebAttrInfo  implements Serializable{

	private String attrId;

	private String attrName;

	private String attrDesc;

	private String entityId;

	private String entityName;

	private boolean queryFlag;

	private boolean logicFlag;

	private String tableCode;

	private String columnCode;

	private String aliasCode;

	private String realTableCode;

	public String getRealTableCode() {
		return realTableCode;
	}

	public void setRealTableCode(String realTableCode) {
		this.realTableCode = realTableCode;
	}

	public boolean getLogicFlag() {
		return logicFlag;
	}

	public void setLogicFlag(boolean logicFlag) {
		this.logicFlag = logicFlag;
	}

	public boolean getQueryFlag() {
		return queryFlag;
	}

	public void setQueryFlag(boolean queryFlag) {
		this.queryFlag = queryFlag;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public String getAttrDesc() {
		return attrDesc;
	}

	public void setAttrDesc(String attrDesc) {
		this.attrDesc = attrDesc;
	}

	public String getAttrId() {
		return attrId;
	}

	public void setAttrId(String attrId) {
		this.attrId = attrId;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public String getAliasCode() {
		return aliasCode;
	}

	public void setAliasCode(String aliasCode) {
		this.aliasCode = aliasCode;
	}

	public String getColumnCode() {
		return columnCode;
	}

	public void setColumnCode(String columnCode) {
		this.columnCode = columnCode;
	}

	public String getTableCode() {
		return tableCode;
	}

	public void setTableCode(String tableCode) {
		this.tableCode = tableCode;
	}

}
