package com.timesontransfar.common.authorization.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@SuppressWarnings("rawtypes")
public class TsmDataPermit implements java.io.Serializable{

/*
   DATAOPERID           varchar(32) //id
   OBJ_ID               NUMBER(9)   //实体ID
   OPERTYPE             NUMBER(1)   //操作类型  0 查询  1 新增   2 修改  3 删除
*/
	private String id;
	private String objectId;
	private boolean privateFlag;
	private Map permitElements;

	public TsmDataPermit() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List getQueryPermit() {
		return filtePermitElements(0);
	}

	public List getInsertPermit() {
		return filtePermitElements(1);
	}

	public List getUpdatePermit() {
		return filtePermitElements(2);
	}

	public List getDeletePermit() {
		return filtePermitElements(3);
	}

	private List filtePermitElements(int operType){
		List retElements = new ArrayList();
		Object tempElements;
		tempElements = permitElements.get(operType)==null?permitElements.get(-1):permitElements.get(operType);
		if (tempElements!=null) {
			retElements = (List)tempElements;
		}
		return retElements;
	}

	public void setPermitElements(Map permitElements) {
		this.permitElements = permitElements;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public boolean isPrivateFlag() {
		return privateFlag;
	}

	public void setPrivateFlag(boolean privateFlag) {
		this.privateFlag = privateFlag;
	}

}
