/**
 * @author 万荣伟
 */
package com.timesontransfar.staffSkill;

/**
 * @author 万荣伟
 *
 */
public class StaffSkillInfo {
	
	private String guid="";//唯一性ID
	private int staffId;//员工ID
	private String staffName="";//员工名
	private String orgId="";//员工所在部门ID
	private String orgName = "";//员工所在部门名
	private String flowOrgId="";//流向部门ID
	private String flowOrgName="";//流向部门名
	private int skillId;//技能ID
	private String skillName="";//技能名
	private int creatStaff;//创建员工
	private String creatDate;//创建时间
	private int tacheId;//环节ID
	private int skillState;//状态,0为无效,1为有效
	private String serviceDate;
	
	/**
	 * @return creatDate
	 */
	public String getCreatDate() {
		return creatDate;
	}
	/**
	 * @param creatDate 要设置的 creatDate
	 */
	public void setCreatDate(String creatDate) {
		this.creatDate = creatDate;
	}
	/**
	 * @return creatStaff
	 */
	public int getCreatStaff() {
		return creatStaff;
	}
	/**
	 * @param creatStaff 要设置的 creatStaff
	 */
	public void setCreatStaff(int creatStaff) {
		this.creatStaff = creatStaff;
	}
	/**
	 * @return flowOrgId
	 */
	public String getFlowOrgId() {
		return flowOrgId;
	}
	/**
	 * @param flowOrgId 要设置的 flowOrgId
	 */
	public void setFlowOrgId(String flowOrgId) {
		this.flowOrgId = flowOrgId;
	}
	/**
	 * @return flowOrgName
	 */
	public String getFlowOrgName() {
		return flowOrgName;
	}
	/**
	 * @param flowOrgName 要设置的 flowOrgName
	 */
	public void setFlowOrgName(String flowOrgName) {
		this.flowOrgName = flowOrgName;
	}
	/**
	 * @return guid
	 */
	public String getGuid() {
		return guid;
	}
	/**
	 * @param guid 要设置的 guid
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}
	/**
	 * @return orgId
	 */
	public String getOrgId() {
		return orgId;
	}
	/**
	 * @param orgId 要设置的 orgId
	 */
	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
	/**
	 * @return orgName
	 */
	public String getOrgName() {
		return orgName;
	}
	/**
	 * @param orgName 要设置的 orgName
	 */
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}
	/**
	 * @return skillId
	 */
	public int getSkillId() {
		return skillId;
	}
	/**
	 * @param skillId 要设置的 skillId
	 */
	public void setSkillId(int skillId) {
		this.skillId = skillId;
	}
	/**
	 * @return skillName
	 */
	public String getSkillName() {
		return skillName;
	}
	/**
	 * @param skillName 要设置的 skillName
	 */
	public void setSkillName(String skillName) {
		this.skillName = skillName;
	}
	/**
	 * @return skillState
	 */
	public int getSkillState() {
		return skillState;
	}
	/**
	 * @param skillState 要设置的 skillState
	 */
	public void setSkillState(int skillState) {
		this.skillState = skillState;
	}
	/**
	 * @return staffId
	 */
	public int getStaffId() {
		return staffId;
	}
	/**
	 * @param staffId 要设置的 staffId
	 */
	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}
	/**
	 * @return staffName
	 */
	public String getStaffName() {
		return staffName;
	}
	/**
	 * @param staffName 要设置的 staffName
	 */
	public void setStaffName(String staffName) {
		this.staffName = staffName;
	}
	/**
	 * @return tacheId
	 */
	public int getTacheId() {
		return tacheId;
	}
	/**
	 * @param tacheId 要设置的 tacheId
	 */
	public void setTacheId(int tacheId) {
		this.tacheId = tacheId;
	}
	public String getServiceDate() {
		return serviceDate;
	}
	public void setServiceDate(String serviceDate) {
		this.serviceDate = serviceDate;
	}
}
