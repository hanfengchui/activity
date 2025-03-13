package com.timesontransfar.flowskillrela.pojo;

import java.util.Date;

/**
 * 流向部门与技能类型的映射表
 * @author Administrator
 *  
 */
public class FlowSkillRela {
	private String id;
	private String flowOrgId;
	private String flowOrgName;
	private String skillType;
	private String skillTypeDesc;
	private String operLogonName;
	private String operOrgId;
	private Date createTime;
	private String operType;
	private Date modifyTime;
	private String status;
	private String serviceDate;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFlowOrgId() {
		return flowOrgId;
	}
	public void setFlowOrgId(String flowOrgId) {
		this.flowOrgId = flowOrgId;
	}
	public String getFlowOrgName() {
		return flowOrgName;
	}
	public void setFlowOrgName(String flowOrgName) {
		this.flowOrgName = flowOrgName;
	}
	public String getSkillType() {
		return skillType;
	}
	public void setSkillType(String skillType) {
		this.skillType = skillType;
	}
	public String getSkillTypeDesc() {
		return skillTypeDesc;
	}
	public void setSkillTypeDesc(String skillTypeDesc) {
		this.skillTypeDesc = skillTypeDesc;
	}
	public String getOperLogonName() {
		return operLogonName;
	}
	public void setOperLogonName(String operLogonName) {
		this.operLogonName = operLogonName;
	}
	public String getOperOrgId() {
		return operOrgId;
	}
	public void setOperOrgId(String operOrgId) {
		this.operOrgId = operOrgId;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public String getOperType() {
		return operType;
	}
	public void setOperType(String operType) {
		this.operType = operType;
	}
	public Date getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getServiceDate() {
		return serviceDate;
	}
	public void setServiceDate(String serviceDate) {
		this.serviceDate = serviceDate;
	}
}
