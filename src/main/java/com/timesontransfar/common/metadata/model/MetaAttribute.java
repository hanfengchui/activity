package com.timesontransfar.common.metadata.model;

import java.io.Serializable;

public class MetaAttribute implements Serializable{

	private String attrId;

	private String objId;

	private String attrName;

	private String attrtable;

	private String attrcolumn;

	public MetaAttribute() {
		super();
		// Auto-generated constructor stub
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

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public String getAttrcolumn() {
		return attrcolumn;
	}

	public void setAttrcolumn(String attrcolumn) {
		this.attrcolumn = attrcolumn;
	}

	public String getAttrtable() {
		return attrtable;
	}

	public void setAttrtable(String attrtable) {
		this.attrtable = attrtable;
	}

}
