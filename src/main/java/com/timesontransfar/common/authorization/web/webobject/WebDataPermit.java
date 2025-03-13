package com.timesontransfar.common.authorization.web.webobject;

import java.io.Serializable;

public class WebDataPermit  implements Serializable{

	private String dataId; // 数据权限唯一ID

	private String objId; // 实体Id

	private String objName; // 实体名称

	private int operType; // 操作类型

	private int org_state; //约束类型

	private boolean privateFlag; // 公有私有

	private boolean inherit = false; // 是否为继承功能

	private boolean canUsed; // 是否可用

	private String roleName;

	public boolean isCanUsed() {
		return canUsed;
	}

	public void setCanUsed(boolean canUsed) {
		this.canUsed = canUsed;
	}

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}



	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public int getOperType() {
		return operType;
	}

	public void setOperType(int operType) {
		this.operType = operType;
	}

	public boolean isPrivateFlag() {
		return privateFlag;
	}

	public void setPrivateFlag(boolean privateFlag) {
		this.privateFlag = privateFlag;
	}

	public int getOrg_state() {
		return org_state;
	}

	public void setOrg_state(int org_state) {
		this.org_state = org_state;
	}

	public boolean getInherit() {
		return inherit;
	}

	public void setInherit(boolean inherit) {
		this.inherit = inherit;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

}
