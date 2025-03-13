/**
 * <p>类名：SheetLimitTimeRule</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：chenjw</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by  chenjw 2008-3-28 14:48:19</p>
 * <p></p>
 */
package com.timesontransfar.customservice.paramconfig.pojo;

import java.io.Serializable;

/**
 * @author chenjw
 * @date 2008-3-28 14:48:19
 */
public class SheetLimitTimeRule implements Serializable{
	private static final long serialVersionUID = 1L;
	private String limitTimeRuleGuid;//LIMITTIME_RULE_GUID  VARCHAR(32)时限规则GUID
	private int regionId;//REGION_ID NUMBER(9)地区编码
	private int workSheetSchemaId;//WORKSHEET_SCHEMA_ID  NUMBER(9)工单模板ID
	private int limitTime;//LIMIT_TIME NUMBER(3)工单时限(小时)
	private int preAlarmValue;//PREALARM_VALUE NUMBER(9)预警时限(分)
	private int alarmValue;//ALARM_VALUE  NUMBER(9)告警时限(分)
	private int ruleType;//RULE_TYPE NUMBER(9)工单时限规则类型，包括工单环节处理时限和工单整体时限类型
	/**
	 * @return 返回 alarmValue。
	 */
	public int getAlarmValue() {
		return alarmValue;
	}
	/**
	 * @param alarmValue 要设置的 alarmValue。
	 */
	public void setAlarmValue(int alarmValue) {
		this.alarmValue = alarmValue;
	}
	/**
	 * @return 返回 limitTime。
	 */
	public int getLimitTime() {
		return limitTime;
	}
	/**
	 * @param limitTime 要设置的 limitTime。
	 */
	public void setLimitTime(int limitTime) {
		this.limitTime = limitTime;
	}
	/**
	 * @return 返回 limitTimeRuleGuid。
	 */
	public String getLimitTimeRuleGuid() {
		return limitTimeRuleGuid;
	}
	/**
	 * @param limitTimeRuleGuid 要设置的 limitTimeRuleGuid。
	 */
	public void setLimitTimeRuleGuid(String limitTimeRuleGuid) {
		this.limitTimeRuleGuid = limitTimeRuleGuid;
	}
	/**
	 * @return 返回 preAlarmValue。
	 */
	public int getPreAlarmValue() {
		return preAlarmValue;
	}
	/**
	 * @param preAlarmValue 要设置的 preAlarmValue。
	 */
	public void setPreAlarmValue(int preAlarmValue) {
		this.preAlarmValue = preAlarmValue;
	}
	/**
	 * @return 返回 regionId。
	 */
	public int getRegionId() {
		return regionId;
	}
	/**
	 * @param regionId 要设置的 regionId。
	 */
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	/**
	 * @return 返回 workSheetSchemaId。
	 */
	public int getWorkSheetSchemaId() {
		return workSheetSchemaId;
	}
	/**
	 * @param workSheetSchemaId 要设置的 workSheetSchemaId。
	 */
	public void setWorkSheetSchemaId(int workSheetSchemaId) {
		this.workSheetSchemaId = workSheetSchemaId;
	}
	/**
	 * @return 返回 ruleType。
	 */
	public int getRuleType() {
		return ruleType;
	}
	/**
	 * @param ruleType 要设置的 ruleType。
	 */
	public void setRuleType(int ruleType) {
		this.ruleType = ruleType;
	}
}
