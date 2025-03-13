package com.timesontransfar.common.authorization.model;


public class TsmFunction implements java.io.Serializable{
	/*
	 * FUNC_OPERATE_ID varchar2(32) //id not null, FUNC_OPERATE_NAME
	 * varchar2(32), //名称 FUNC_OPERATE_TYPE NUMBER(2), //数据来源 FUNC_OPERATE_DESC
	 * VARCHAR2(256), //描述
	 */

	private String id; // id

	private String funcName; // 名称

	private String funcDesc; // 描述

	private boolean canBeUse;  //是否可用

	private boolean isprivate; //是否为私有

	private boolean isInherit;  //是否为继承

	private String menuName ; //

	private int operaType;

	private String operaTypeId;//操作类型

	private String roleName;
	
	private String constraint;

	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}
	
	private IConstraint constraintObject; // 约束对象

	public IConstraint getConstraintObject() {
		return constraintObject;
	}

	public void setConstraintObject(IConstraint constraintObject) {
		this.constraintObject = constraintObject;
	}

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isCanBeUse() {
		return canBeUse;
	}

	public void setCanBeUse(boolean canBeUse) {
		this.canBeUse = canBeUse;
	}

	public boolean isIsprivate() {
		return isprivate;
	}

	public void setIsprivate(boolean isprivate) {
		this.isprivate = isprivate;
	}

	public String getFuncDesc() {
		return funcDesc;
	}

	public void setFuncDesc(String funcDesc) {
		this.funcDesc = funcDesc;
	}

	public boolean isInherit() {
		return isInherit;
	}

	public void setInherit(boolean isInherit) {
		this.isInherit = isInherit;
	}

	public int getOperaType() {
		return operaType;
	}

	public void setOperaType(int operaType) {
		this.operaType = operaType;
	}

	public String getOperaTypeId() {
		return operaTypeId;
	}

	public void setOperaTypeId(String operaTypeId) {
		this.operaTypeId = operaTypeId;
	}

	public String getMenuName() {
		return menuName;
	}

	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
