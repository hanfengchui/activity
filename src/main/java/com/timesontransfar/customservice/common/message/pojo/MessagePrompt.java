package com.timesontransfar.customservice.common.message.pojo;

public class MessagePrompt {

	private String guid = "";//GUID
	private int staffId; // STAFF_ID NUMBER(9) 员工ID
	private String staffName = "";// STAFF_NAME VARCHAR2(32) 员工名
	private String orgId = "";// ORG_ID VARCHAR2(32) 组织机构
	private String orgName = "";// ORG_NAME VARCHAR2(32) 组织机构
	private int typeId; // TYPE_ID NUMBER(9)消息类型ID
	private String typeName = "";// TYPE_NAME VARCHAR2(32) 消息类型
	private String msgContent = "";// MESSAGE_CONTENT VARCHAR2(1000)消息内容
	private int readed; // READED NUMBER(1)已读标志
	private String crtDate = "";// CREAT_DATE VARCHAR2(1000)消息内容
	private String readDate = "";// READED_DATE DATE 消息阅读日期
	private String urlAddr = "";// URL_ADDR VARCHAR2(32) 页面跳转URL

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public int getStaffId() {
		return staffId;
	}

	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}

	public String getStaffName() {
		return staffName;
	}

	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(String msgContent) {
		this.msgContent = msgContent;
	}

	public int getReaded() {
		return readed;
	}

	public void setReaded(int readed) {
		this.readed = readed;
	}

	public String getCrtDate() {
		return crtDate;
	}

	public void setCrtDate(String crtDate) {
		this.crtDate = crtDate;
	}

	public String getReadDate() {
		return readDate;
	}

	public void setReadDate(String readDate) {
		this.readDate = readDate;
	}

	public String getUrlAddr() {
		return urlAddr;
	}

	public void setUrlAddr(String urlAddr) {
		this.urlAddr = urlAddr;
	}
}
