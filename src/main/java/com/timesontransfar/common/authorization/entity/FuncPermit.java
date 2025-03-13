package com.timesontransfar.common.authorization.entity;

import java.io.Serializable;

public class FuncPermit  implements Serializable{

	private String funcid;

	private String funcName;

	private String funcDesc;

	private boolean funcState; // 0 有效 1 无效

	private FuncType LinkType;

	private String LinkKeyId;

	public String getFuncDesc() {
		return funcDesc;
	}

	public void setFuncDesc(String funcDesc) {
		this.funcDesc = funcDesc;
	}

	public String getFuncid() {
		return funcid;
	}

	public void setFuncid(String funcid) {
		this.funcid = funcid;
	}

	public String getFuncName() {
		return funcName;
	}

	public void setFuncName(String funcName) {
		this.funcName = funcName;
	}

	public boolean getFuncState() {
		return funcState;
	}

	public void setFuncState(boolean funcState) {
		this.funcState = funcState;
	}

	public String getLinkKeyId() {
		return LinkKeyId;
	}

	public void setLinkKeyId(String linkKeyId) {
		LinkKeyId = linkKeyId;
	}

	public FuncType getLinkType() {
		return LinkType;
	}

	public void setLinkType(FuncType linkType) {
		LinkType = linkType;
	}

}
