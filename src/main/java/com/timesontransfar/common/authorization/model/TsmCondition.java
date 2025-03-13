package com.timesontransfar.common.authorization.model;

import java.io.Serializable;

public class TsmCondition implements Serializable{
/*
   REST_COND_ID         varchar2(32)    //id                  not null,
   ROLE_ID              varchar2(32)    //角色ID
   OBJ_ID               NUMBER(9),      //实体ID
   ISCOMMON             NUMBER(1),      //是否为公有
   DATAROLEID           varchar2(32),   //实体操作ID
   ATTRIBUTE_ID         varchar2(32),   //属性ID
   OPERATE_ID           NUMBER(2),      //操作匹配类型
   VALUE                varchar2(32),   //操作匹配值
   RELATION             varchar2(32),   //连接类型  1 表示AND 0  表示OR
   CONDLEVEL            NUMBER(2),      //连接顺序
 */
	private String id; //id
	private boolean commonFlag; //是否为公有
	private String objId;  //实体Id
	private String objName;  //实体Id

	private String attributeId; //属性ID
	private String attributeName;  //实体Id
	private String dataRoleId = "";
	private int operateType;    //操作匹配类型
	/*
	* 	<OPTION value="0" selected="selected">等于</OPTION>
	*	<OPTION value="1" selected="selected">大于</OPTION>
	*	<OPTION value="2" selected="selected">大于等于</OPTION>
	*	<OPTION value="3" selected="selected">小于</OPTION>
	*	<OPTION value="4" selected="selected">小于等于</OPTION>
	*	<OPTION value="5" selected="selected">不等于</OPTION>
	*	<OPTION value="6" selected="selected">like</OPTION>
	 */
	private String matchValue;  //操作匹配值
	private int linkSeq;        //连接顺序
	private String joinType;    //连接类型
	private int condType;  //-1 共用 0 查询 1 新增　2 修改　3 删除

	public int getCondType() {
		return condType;
	}

	public void setCondType(int condType) {
		this.condType = condType;
	}

	public String getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(String attributeId) {
		this.attributeId = attributeId;
	}

	public boolean isCommonFlag() {
		return commonFlag;
	}

	public void setCommonFlag(boolean commonFlag) {
		this.commonFlag = commonFlag;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJoinType() {
		return joinType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public int getLinkSeq() {
		return linkSeq;
	}

	public void setLinkSeq(int linkSeq) {
		this.linkSeq = linkSeq;
	}

	public String getMatchValue() {
		return matchValue;
	}

	public void setMatchValue(String matchValue) {
		this.matchValue = matchValue;
	}

	public int getOperateType() {
		return operateType;
	}

	public void setOperateType(int operateType) {
		this.operateType = operateType;
	}

	public String getDataRoleId() {
		return dataRoleId;
	}

	public void setDataRoleId(String dataRoleId) {
		this.dataRoleId = dataRoleId;
	}

	public String getObjId() {
		return objId;
	}

	public void setObjId(String objId) {
		this.objId = objId;
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

}
