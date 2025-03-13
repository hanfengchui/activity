package com.timesontransfar.common.authorization.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@SuppressWarnings("rawtypes")
public class TsmEntityPermit implements Serializable{

	private String id;

	private String objId;

	private String objName;

	private int orgState;

	private String objDesc;

	private int operType;

	private int state;

	private boolean isInherit;

	private boolean isPrivate;

	private String roleName;

	private List geneElements = new ArrayList();

	public TsmEntityPermit() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public int getOperType() {
		return operType;
	}

	public void setOperType(int operType) {
		this.operType = operType;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getObjDesc() {
		return objDesc;
	}

	public void setObjDesc(String objDesc) {
		this.objDesc = objDesc;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public boolean isInherit() {
		return isInherit;
	}

	public void setInherit(boolean isInherit) {
		this.isInherit = isInherit;
	}

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public int getOrgState() {
		return orgState;
	}

	public void setOrgState(int orgState) {
		this.orgState = orgState;
	}


	/**
	 * @return 返回 geneElements。
	 */
	public List getGeneElements() {
		return geneElements;
	}
	/**
	 * @param geneElements 要设置的 geneElements。
	 */
	public void setGeneElements(List geneElements) {
		this.geneElements = geneElements;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
}
