package com.timesontransfar.customservice.common.message.pojo;

public class TipsPojo {

	private String remindId = "";
	private int staffId;//员工ID
	private String remindTitle = "";//提醒标题
	private String remindContent = "";//提醒内容
	private String createTime = "";//创建时间
	private String remindTime = "";//提醒时间
	private int remindType;//提醒方式 1.短信 2.右下角弹框 3.高亮显示
	private int createStaffId;//创建人 员工ID
	private String createStaffName = "";//创建人 员工姓名
	private int updateStaffId;//更新人 员工ID
	private String updateStaffName="";//更新人 员工姓名
	private int titleSort;//序号
	private int display;//显示隐藏
	private String linkOrg;//发布范围
	
	public String getRemindId() {
		return remindId;
	}
	public void setRemindId(String remindId) {
		this.remindId = remindId;
	}
	public int getStaffId() {
		return staffId;
	}
	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}
	public String getRemindTitle() {
		return remindTitle;
	}
	public void setRemindTitle(String remindTitle) {
		this.remindTitle = remindTitle;
	}
	public String getRemindContent() {
		return remindContent;
	}
	public void setRemindContent(String remindContent) {
		this.remindContent = remindContent;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getRemindTime() {
		return remindTime;
	}
	public void setRemindTime(String remindTime) {
		this.remindTime = remindTime;
	}
	public int getRemindType() {
		return remindType;
	}
	public void setRemindType(int remindType) {
		this.remindType = remindType;
	}
	public int getCreateStaffId() {
		return createStaffId;
	}
	public void setCreateStaffId(int createStaffId) {
		this.createStaffId = createStaffId;
	}
	public String getCreateStaffName() {
		return createStaffName;
	}
	public void setCreateStaffName(String createStaffName) {
		this.createStaffName = createStaffName;
	}
	public int getUpdateStaffId() {
		return updateStaffId;
	}
	public void setUpdateStaffId(int updateStaffId) {
		this.updateStaffId = updateStaffId;
	}
	public String getUpdateStaffName() {
		return updateStaffName;
	}
	public void setUpdateStaffName(String updateStaffName) {
		this.updateStaffName = updateStaffName;
	}
	public int getTitleSort() {
		return titleSort;
	}
	public void setTitleSort(int titleSort) {
		this.titleSort = titleSort;
	}
	public int getDisplay() {
		return display;
	}
	public void setDisplay(int display) {
		this.display = display;
	}
	public String getLinkOrg() {
		return linkOrg;
	}
	public void setLinkOrg(String linkOrg) {
		this.linkOrg = linkOrg;
	}
}
