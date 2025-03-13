package com.timesontransfar.common.authorization.web.webobject;

import java.io.Serializable;

public class WebMenuPriPermit  implements Serializable{
	private String id;
	private String isPrivate;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIsPrivate() {
		return isPrivate;
	}
	public void setIsPrivate(String isPrivate) {
		this.isPrivate = isPrivate;
	}
}
