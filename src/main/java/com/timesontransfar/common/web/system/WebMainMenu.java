package com.timesontransfar.common.web.system;

import java.io.Serializable;
import java.util.List;
@SuppressWarnings("rawtypes")
public class WebMainMenu implements Serializable{
	private String id; // 唯一ID;

	private String name;// 菜单名称

	private String url;// URL地址

	private int sequence;// 显示顺序

	private boolean itemFlag;// 是否菜单项

	private boolean topFlag;// 是否为顶级菜单

	private String openType;// 打开类型

	private List childList;// 直接下属的菜单ID

	private List parentList;// 父亲菜单ID

	private boolean isPrivate=false;

	public boolean isPrivate() {
		return isPrivate;
	}

	public void setPrivate(boolean isPrivate) {
		this.isPrivate = isPrivate;
	}

	public List getChildList() {
		return childList;
	}

	public void setChildList(List childList) {
		this.childList = childList;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isItemFlag() {
		return itemFlag;
	}

	public void setItemFlag(boolean itemFlag) {
		this.itemFlag = itemFlag;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOpenType() {
		return openType;
	}

	public void setOpenType(String openType) {
		this.openType = openType;
	}

	public List getParentList() {
		return parentList;
	}

	public void setParentList(List parentList) {
		this.parentList = parentList;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public boolean isTopFlag() {
		return topFlag;
	}

	public void setTopFlag(boolean topFlag) {
		this.topFlag = topFlag;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
