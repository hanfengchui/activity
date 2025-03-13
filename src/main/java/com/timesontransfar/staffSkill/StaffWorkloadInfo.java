/*
 * 2011-08-25 LiJiahui新增该实现类
 */
package com.timesontransfar.staffSkill;

/**
 * 表CC_STAFF_WORKLOAD中记录的封装对象
 * 
 * @author LiJiahui
 * 
 */
public class StaffWorkloadInfo {

	/**
	 * 默认构造方法
	 */
	public StaffWorkloadInfo() { //构造函数
	}

	/**
	 * 唯一主键
	 */
	private String guid;

	/**
	 * 员工编号
	 */
	private int staffId;

	/**
	 * 班次编号
	 */
	private String wsId;

	/**
	 * 员工技能熟练度
	 */
	private int skillLevel;

	/**
	 * 该员工本班次开始时间
	 */
	private String startMoment;

	/**
	 * 该员工本班次结束时间
	 */
	private String endMoment;

	/**
	 * 当前完成的工作量，即接收的工单总数
	 */
	private int curWorkload;

	/**
	 * 当前完成的工作量百分比
	 */
	private double curRate;

	/**
	 * 该员工本班次的工作量总阀值
	 */
	private int threshold;

	/**
	 * 该记录的状态，0为可用，1为不可用
	 */
	private int state;

	/**
	 * 员工所在组织机构ID
	 */
	private String orgId = "";

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

	public String getWsId() {
		return wsId;
	}

	public void setWsId(String wsId) {
		this.wsId = wsId;
	}

	public int getSkillLevel() {
		return skillLevel;
	}

	public void setSkillLevel(int skillLevel) {
		this.skillLevel = skillLevel;
	}

	public String getStartMoment() {
		return startMoment;
	}

	public void setStartMoment(String startMoment) {
	    int i = startMoment.indexOf(".");
	    if(i != -1){
	        startMoment = startMoment.substring(0,i);
	    }
		this.startMoment = startMoment;
	}

	public String getEndMoment() {
		return endMoment;
	}

	public void setEndMoment(String endMoment) {
	    int i = endMoment.indexOf(".");
        if(i != -1){
            endMoment = endMoment.substring(0,i);
        }
		this.endMoment = endMoment;
	}

	public int getCurWorkload() {
		return curWorkload;
	}

	public void setCurWorkload(int curWorkload) {
		this.curWorkload = curWorkload;
	}

	public double getCurRate() {
		return curRate;
	}

	public void setCurRate(double curRate) {
		this.curRate = curRate;
	}

	public int getThreshold() {
		return threshold;
	}

	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}
}