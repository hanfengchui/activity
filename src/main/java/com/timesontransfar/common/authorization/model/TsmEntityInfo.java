package com.timesontransfar.common.authorization.model;

public class TsmEntityInfo implements java.io.Serializable{

	private String entityId;// 实体ID

	private String entityName;// 实体名称

	private String entityDesc;// 实体描述

	public String getEntityDesc() {
		return entityDesc;
	}

	public void setEntityDesc(String entityDesc) {
		this.entityDesc = entityDesc;
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

}
