package com.timesontransfar.customservice.staffability.pojo;

public class StaffAbility {
	private String guid = "";
	private int staffId = 0;
	private String staffName = "";
	private int skillLevel = 0;
	private int threshold = 0;
	private int createStaffId = 0;
	private String createTime;
	private int useable = 0;
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
	public int getSkillLevel() {
		return skillLevel;
	}
	public void setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
	}
	public int getThreshold() {
		return threshold;
	}
	public void setThreshold(int threshold) {
		this.threshold = threshold;
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
