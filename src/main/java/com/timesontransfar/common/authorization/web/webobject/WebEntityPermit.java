package com.timesontransfar.common.authorization.web.webobject;

import java.io.Serializable;

public class WebEntityPermit  implements Serializable{

	private String entityid;

	private String entityName;

	private String entityDesc;

	private String operation;

	private boolean selectFlag = false;

	private boolean updateFlag = false;

	private boolean deleteFlag = false;

	private boolean insertFlag = false;

	public boolean getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getEntityDesc() {
		return entityDesc;
	}

	public void setEntityDesc(String entityDesc) {
		this.entityDesc = entityDesc;
	}

	public String getEntityid() {
		return entityid;
	}

	public void setEntityid(String entityid) {
		this.entityid = entityid;
	}

	public String getEntityName() {
		return entityName;
	}

	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}

	public boolean getInsertFlag() {
		return insertFlag;
	}

	public void setInsertFlag(boolean insertFlag) {
		this.insertFlag = insertFlag;
	}

	public boolean getSelectFlag() {
		return selectFlag;
	}

	public void setSelectFlag(boolean selectFlag) {
		this.selectFlag = selectFlag;
	}

	public boolean getUpdateFlag() {
		return updateFlag;
	}

	public void setUpdateFlag(boolean updateFlag) {
		this.updateFlag = updateFlag;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

}
