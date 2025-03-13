package com.timesontransfar.customservice.workFlowOrg.pojo;

public class WorkSheetRuleItem {
	
	private int itemId;//规则细目ID
	private String itemName;//规则细目名
	private String itemDesc;//规则细目描述
	private int entiyId=0;//实体ID
	private int attributeId=0;//属性ID
	/**
	 * @return attributeId
	 */
	public int getAttributeId() {
		return attributeId;
	}
	/**
	 * @param attributeId 要设置的 attributeId
	 */
	public void setAttributeId(int attributeId) {
		this.attributeId = attributeId;
	}
	/**
	 * @return entiyId
	 */
	public int getEntiyId() {
		return entiyId;
	}
	/**
	 * @param entiyId 要设置的 entiyId
	 */
	public void setEntiyId(int entiyId) {
		this.entiyId = entiyId;
	}
	/**
	 * @return itemDesc
	 */
	public String getItemDesc() {
		return itemDesc;
	}
	/**
	 * @param itemDesc 要设置的 itemDesc
	 */
	public void setItemDesc(String itemDesc) {
		this.itemDesc = itemDesc;
	}
	/**
	 * @return itemId
	 */
	public int getItemId() {
		return itemId;
	}
	/**
	 * @param itemId 要设置的 itemId
	 */
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	/**
	 * @return itemName
	 */
	public String getItemName() {
		return itemName;
	}
	/**
	 * @param itemName 要设置的 itemName
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
	
	
	

}
