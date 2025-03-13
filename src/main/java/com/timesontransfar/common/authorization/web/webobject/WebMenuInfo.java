package com.timesontransfar.common.authorization.web.webobject;

import java.io.Serializable;

public class WebMenuInfo  implements Serializable{

	private String menuId;

	private String menuName;

	private String menuDesc;

	private boolean itemFlag;

	private String openType;

	private String parentId;

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}


	public boolean isItemFlag() {
		return itemFlag;
	}

	public void setItemFlag(boolean itemFlag) {
		this.itemFlag = itemFlag;
	}

	public String getMenuDesc() {
		return menuDesc;
	}

	public void setMenuDesc(String menuDesc) {
		this.menuDesc = menuDesc;
	}

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getOpenType() {
		return openType;
	}

	public void setOpenType(String openType) {
		this.openType = openType;
	}

}
