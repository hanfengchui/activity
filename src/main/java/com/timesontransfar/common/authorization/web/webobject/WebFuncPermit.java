package com.timesontransfar.common.authorization.web.webobject;

import java.io.Serializable;

public class WebFuncPermit  implements Serializable{
	private String funcId; // 功能唯一ID

	private String funcName; // 功能名称

	private String funcDesc; // 功能描述

	private boolean privateFlag; // 公有私有

	private boolean inherit; // 是否为继承功能

	private boolean canUsed; // 是否可用

	private int dataSource; // 数据来源

	private String linkName;  //关联的Id名称

	private String linkId; // 连接Id

	private String menu_name;

	private String roleName;

	public String getMenu_name() {
		return menu_name;
	}

	public void setMenu_name(String menu_name) {
		this.menu_name = menu_name;
	}

	public boolean isCanUsed() {
		return canUsed;
	}

	public void setCanUsed(boolean canUsed) {
		this.canUsed = canUsed;
	}

	public int getDataSource() {
		return dataSource;
	}

	public void setDataSource(int dataSource) {
		this.dataSource = dataSource;
	}

	public String getFuncDesc() {
		return funcDesc;
	}

	public void setFuncDesc(String funcDesc) {
		this.funcDesc = funcDesc;
	}

	public String getFuncId() {
		return funcId;
	}

	public void setFuncId(String funcId) {
		this.funcId = funcId;
	}

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}




	public boolean isInherit() {
		return inherit;
	}

	public void setInherit(boolean inherit) {
		this.inherit = inherit;
	}

	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}

	public boolean isPrivateFlag() {
		return privateFlag;
	}

	public void setPrivateFlag(boolean privateFlag) {
		this.privateFlag = privateFlag;
	}

	public String getLinkName() {
		return linkName;
	}

	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
