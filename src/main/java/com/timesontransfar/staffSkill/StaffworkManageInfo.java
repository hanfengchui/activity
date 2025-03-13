/**
 * @author 万荣伟
 */
package com.timesontransfar.staffSkill;

/**
 * @author 万荣伟
 *
 */
public class StaffworkManageInfo {
	
	private String staffworkGuid="";//唯一性ID
	private int staffId;//员工
	private String workdate="";//工作日期;
	private String startDate;//开始工作时间
	private String endDate;//结束时间
	private String creatDate;//创建时间
	private int creatStaff;//创建员工
	private int modifyStaff;//修改员工
	private String modifyDate;//修改时间
	private int state;//是否有效
	
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
	 * @return endDate
	 */
	public String getEndDate() {
		return endDate;
	}
	/**
	 * @param endDate 要设置的 endDate
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	/**
	 * @return modifyDate
	 */
	public String getModifyDate() {
		return modifyDate;
	}
	/**
	 * @param modifyDate 要设置的 modifyDate
	 */
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}
	/**
	 * @return modifyStaff
	 */
	public int getModifyStaff() {
		return modifyStaff;
	}
	/**
	 * @param modifyStaff 要设置的 modifyStaff
	 */
	public void setModifyStaff(int modifyStaff) {
		this.modifyStaff = modifyStaff;
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
	 * @return staffworkGuid
	 */
	public String getStaffworkGuid() {
		return staffworkGuid;
	}
	/**
	 * @param staffworkGuid 要设置的 staffworkGuid
	 */
	public void setStaffworkGuid(String staffworkGuid) {
		this.staffworkGuid = staffworkGuid;
	}
	/**
	 * @return startDate
	 */
	public String getStartDate() {
		return startDate;
	}
	/**
	 * @param startDate 要设置的 startDate
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	/**
	 * @return state
	 */
	public int getState() {
		return state;
	}
	/**
	 * @param state 要设置的 state
	 */
	public void setState(int state) {
		this.state = state;
	}
	/**
	 * @return workdate
	 */
	public String getWorkdate() {
		return workdate;
	}
	/**
	 * @param workdate 要设置的 workdate
	 */
	public void setWorkdate(String workdate) {
		this.workdate = workdate;
	}
	
}
