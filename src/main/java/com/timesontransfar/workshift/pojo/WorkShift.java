package com.timesontransfar.workshift.pojo;

import java.util.Date;

public class WorkShift {

	private String id;//工作班次编号
	private String name;//工作班次名称
	private String time;//工作班次上班时间
	private int percent;//阀值
	private String desc;//备注
	private int createStaffId;//班次信息创建员工编号
	private Date createTime;//班次信息创建时间
	private int useable;//班次信息是否可用
	private String createOrgId;//系统班次操作部门ID
	private String createOrgName;//系统班次操作部门名称
	private String createLogonname;//系统班次操作人工号
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int getPercent() {
		return percent;
	}
	public void setPercent(int percent) {
		this.percent = percent;
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
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public int getUseable() {
		return useable;
	}
	public void setUseable(int useable) {
		this.useable = useable;
	}
	public String getCreateOrgId() {
		return createOrgId;
	}
	public void setCreateOrgId(String createOrgId) {
		this.createOrgId = createOrgId;
	}
	public String getCreateOrgName() {
		return createOrgName;
	}
	public void setCreateOrgName(String createOrgName) {
		this.createOrgName = createOrgName;
	}
	public String getCreateLogonname() {
		return createLogonname;
	}
	public void setCreateLogonname(String createLogonname) {
		this.createLogonname = createLogonname;
	}
	
}
