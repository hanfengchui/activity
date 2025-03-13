package com.timesontransfar.common.authorization.web.webobject;

import java.io.Serializable;

/**
 * 供于页面层使用的权限因子对象
 * @author QLiang
 *
 */
public class WebPermitCondition  implements Serializable{

	/**
	 * REST_COND_ID         varchar2(32)                    not null,
	   ROLE_ID              varchar2(32),
	   OBJ_ID               NUMBER(9),
	   ISCOMMON             NUMBER(1),
	   DATAROLEID           varchar2(32),
	   ATTRIBUTE_ID         varchar2(32),
	   OPERATE_ID           NUMBER(2),
	   VALUE                varchar2(32),
	   RELATION             varchar2(32),
	   CONDLEVEL            NUMBER(2),
	 */
	private String id;   //因子Id

	private String objId;  //约束实体Id

	private String objName;

	private int condType;  //是-1公用还是 0 查询\ 1 新增\ 2修改\ 3删除

	private String dataRoleId = "";  //实体操作id 当isCommon为false时才有值

	private String attributeId;  //实体属性Id

	private String attributeName;

	private String operateType;    //条件操作符 0 = 1 > 2 < 3 like

	private String matchValue;  //匹配值

	private String linkType;   //于上一因子的条件连接符 AND OR

	private int linkOrder;  //因子间的连接顺序

	public String getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(String attributeId) {
		this.attributeId = attributeId;
	}

	public String getDataRoleId() {
		return dataRoleId;
	}

	public void setDataRoleId(String dataRoleId) {
		this.dataRoleId = dataRoleId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getLinkOrder() {
		return linkOrder;
	}

	public void setLinkOrder(int linkOrder) {
		this.linkOrder = linkOrder;
	}

	public String getLinkType() {
		return linkType;
	}

	public void setLinkType(String linkType) {
		this.linkType = linkType;
	}

	public String getMatchValue() {
		return matchValue;
	}

	public void setMatchValue(String matchValue) {
		this.matchValue = matchValue;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
	}

	public String getOperateType() {
		return operateType;
	}

	public void setOperateType(String operateType) {
		this.operateType = operateType;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public String getObjName() {
		return objName;
	}

	public void setObjName(String objName) {
		this.objName = objName;
	}

	public int getCondType() {
		return condType;
	}

	public void setCondType(int condType) {
		this.condType = condType;
	}
}
