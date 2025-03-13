package com.timesontransfar.staffWorkShift.pojo;


public class StaffWorkShift {
	private String id = "";
	private int staffId = 0;
	private String staffName = "";
	private String workTeamName = "";
	private String workDate;
	private String workShiftId = "";
	private String desc = "";
	private int createStaffId = 0;
	private String createTime;
	private int useable = 0;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
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
	public String getWorkTeamName() {
		return workTeamName;
	}
	public void setWorkTeamName(String workTeamName) {
		this.workTeamName = workTeamName;
	}
	public String getWorkDate() {
		return workDate;
	}
	public void setWorkDate(String workDate) {
		this.workDate = workDate;
	}
	public String getWorkShiftId() {
		return workShiftId;
	}
	public void setWorkShiftId(String workShiftId) {
		this.workShiftId = workShiftId;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getCreateStaffId() {
		return createStaffId;
	}
	public void setCreateStaffId(int createStaffId) {
		this.createStaffId = createStaffId;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public int getUseable() {
		return useable;
	}
	public void setUseable(int useable) {
		this.useable = useable;
	}
	
}
