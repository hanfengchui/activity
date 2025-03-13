/**
 * <p>类名：SheetLimitTimeItem</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：chenjw</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by  chenjw 2008-3-28 14:48:39</p>
 * <p></p>
 */
package com.timesontransfar.customservice.paramconfig.pojo;

import java.io.Serializable;

/**
 * @author chenjw
 * @date 2008-3-28 14:48:39
 */
public class SheetLimitTimeItem implements Serializable{
	private static final long serialVersionUID = 1L;
	private String limitTimeItemGuid;//LIMITTIME_ITEM_GUID  VARCHAR(32)时限规则项GUID
	private int regionId;//REGION_ID NUMBER(9)地区编码
	private int workSheetSchemaId;//WORKSHEET_SCHEMA_ID  NUMBER(9)工单模板ID
	private String limitTimeRuleGuid;//LIMITTIME_RULE_GUID  VARCHAR(32)工单时限规则项GUID
	private int attributeRefId;//ATTRIBUTE_REF_ID NUMBER(9)工单属性REFID
	private String attributeName;//ATTRIBUTE_NAME VARCHAR2(64)工单属性名称
	private int attributeTypeRefId;//ATTRIBUTE_TYPE_REF_ID NUMBER(9)工单属性类型REFID
	private String attributeTypeName;//ATTRIBUTE_TYPE_NAME  VARCHAR2(64)工单属性类型名称
	private int ruleType;//RULE_TYPE NUMBER(9)工单时限规则类型，包括工单环节处理时限和工单整体时限类型
	/**
	 * @return 返回 attributeName。
	 */
	public String getAttributeName() {
		return attributeName;
	}
	/**
	 * @param attributeName 要设置的 attributeName。
	 */
	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	/**
	 * @return 返回 attributeRefId。
	 */
	public int getAttributeRefId() {
		return attributeRefId;
	}
	/**
	 * @param attributeRefId 要设置的 attributeRefId。
	 */
	public void setAttributeRefId(int attributeRefId) {
		this.attributeRefId = attributeRefId;
	}
	/**
	 * @return 返回 attributeTypeName。
	 */
	public String getAttributeTypeName() {
		return attributeTypeName;
	}
	/**
	 * @param attributeTypeName 要设置的 attributeTypeName。
	 */
	public void setAttributeTypeName(String attributeTypeName) {
		this.attributeTypeName = attributeTypeName;
	}
	/**
	 * @return 返回 attributeTypeRefId。
	 */
	public int getAttributeTypeRefId() {
		return attributeTypeRefId;
	}
	/**
	 * @param attributeTypeRefId 要设置的 attributeTypeRefId。
	 */
	public void setAttributeTypeRefId(int attributeTypeRefId) {
		this.attributeTypeRefId = attributeTypeRefId;
	}
	/**
	 * @return 返回 limitTimeItemGuid。
	 */
	public String getLimitTimeItemGuid() {
		return limitTimeItemGuid;
	}
	/**
	 * @param limitTimeItemGuid 要设置的 limitTimeItemGuid。
	 */
	public void setLimitTimeItemGuid(String limitTimeItemGuid) {
		this.limitTimeItemGuid = limitTimeItemGuid;
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
