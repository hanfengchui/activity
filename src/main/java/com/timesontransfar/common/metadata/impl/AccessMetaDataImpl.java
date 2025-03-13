package com.timesontransfar.common.metadata.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.metadata.IAccessMetaData;
import com.timesontransfar.common.metadata.model.MetaAttribute;


@SuppressWarnings("rawtypes")
public class AccessMetaDataImpl implements IAccessMetaData{
	private static final Logger log = LoggerFactory.getLogger(AccessMetaDataImpl.class);
	
	private JdbcTemplate jdbcTemplate;

	private String getAttributeInfo; //

	private String getAttribute;

	private String getAttrMapInfo;

	private String getTargetAttribute;

	private String getAllAttributeByCondition;

	public AccessMetaDataImpl() {
		super();
	}

	/**
	 * 取给定的实体、表名、列名对应的元数据对象
	 */
	public Object getAttributeIdIn(Long objId,String tableCode,String colCode){
		List compareList =this.getAllAttributeByObjId(objId);
		Object retObject=null;
		for (int i=0;i<compareList.size();i++){
			MetaAttribute compareObject=(MetaAttribute)compareList.get(i);
			if (compareObject.getAttrtable().equals(tableCode) && compareObject.getAttrcolumn().equals(colCode)){
				retObject = compareList.get(i);
				break;
			}
		}
		return retObject;
	}

	public MetaAttribute getAttribute(String attrId) {
		// Auto-generated method stub
		String logicFlag= "";
		String realTableCode = "";
		List queryList = this.jdbcTemplate.queryForList(
				this.getAttributeInfo,  attrId);
		MetaAttribute retAttr = null;
		if (queryList.size()==1){
			Map queryMap = (Map) queryList.get(0);
			BigDecimal tempDecimal=(BigDecimal)queryMap.get("IS_LOGIC");
			if(tempDecimal==null){
				logicFlag="0";
			}else{
				logicFlag=tempDecimal.toString();
			}
			//logicFlag = ((BigDecimal) queryMap.get("IS_LOGIC")).toString();
			if (logicFlag.equals("1")){  //虚拟实体
				retAttr = buildMetaAttr(((BigDecimal)queryMap.get("ENTITY_ID")).toString(),attrId,(String)queryMap.get("ATTR_NAME"));
			} else {  //非虚拟实体
				retAttr = new MetaAttribute();
				retAttr.setAttrId(attrId);
				retAttr.setObjId(((BigDecimal)queryMap.get("ENTITY_ID")).toString());
				retAttr.setAttrName((String)queryMap.get("ATTR_NAME"));
				if (((String)queryMap.get("RELA_TABLE_CODE")).length()==1){
					//长度等于1，表示的为取到的是元数据配置的别名，需要查找其真实的表名
					realTableCode = this.getRealTableCodeByAlias(queryMap.get("ENTITY_ID")+"",(String)queryMap.get("RELA_TABLE_CODE"));
				}else {
					realTableCode = (String)queryMap.get("RELA_TABLE_CODE");
				}
				retAttr.setAttrtable(realTableCode);
				retAttr.setAttrcolumn((String)queryMap.get("RELA_COL_CODE"));
			}
		}
		return retAttr;
	}

	/*
	 * 取到别名对应的真正的表名
	 */
	private String getRealTableCodeByAlias(String objId,String aliasCode){
		if (objId==null || aliasCode==null){
			return  null;
		}
		String retString ="";
		String sqlStr = "SELECT A.REMOTE_TABLE_ID,A.LOCAL_TABLE_ID FROM PUB_ENTITY_JOINS A WHERE A.ENTITY_ID = A.REMOTE_ENTITY_ID AND A.ENTITY_ID=?";
		List queryList = this.jdbcTemplate.queryForList(sqlStr, objId);
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			String[] arryObj=((String)queryMap.get("REMOTE_TABLE_ID")).split(" ");
			if (arryObj.length>1 && (arryObj[1]).equals(aliasCode)){
				retString = arryObj[0];
				break;
			}
			arryObj=((String)queryMap.get("LOCAL_TABLE_ID")).split(" ");
			if (arryObj.length>1 && (arryObj[1]).equals(aliasCode)){
				retString = arryObj[0];
				break;
			}
		}
		return retString;
	}

	/*
	 * 建立虚拟属性对应的表名和列名
	 */
	private MetaAttribute buildMetaAttr(String objId,String attrId,String attrName){
		List tempList = this.jdbcTemplate.queryForList(this.getTargetAttribute, attrId);
		if(!tempList.isEmpty()){
			Map queryMap=(Map)tempList.get(0);
			BigDecimal tempTargetAttrId=(BigDecimal)queryMap.get("TARGETATTRIBUTE_ID");
			String targetAttrId = tempTargetAttrId.toString();
			List queryList = this.jdbcTemplate.queryForList(
					this.getAttributeInfo, targetAttrId);
			MetaAttribute retAttr = null;
			if (queryList.size()==1){
				queryMap = (Map) queryList.get(0);
				retAttr = new MetaAttribute();
				retAttr.setAttrId(attrId);
				retAttr.setObjId(objId);
				retAttr.setAttrName(attrName);
				retAttr.setAttrtable((String)queryMap.get("RELA_TABLE_CODE"));
				retAttr.setAttrcolumn((String)queryMap.get("RELA_COL_CODE"));
			}
			return retAttr;
		}else{
			return null;
		}
	}

	/*
	 * 取到所有的某实体的属性列表
	 */
	@SuppressWarnings("unchecked")
	public List getAllAttributeByObjId(Long objId) {
		String attributeId = "";
		List returnList = new ArrayList();
		String sqlStr = "SELECT ATTRIBUTE_ID FROM PUB_ATTRIBUTE WHERE ENTITY_ID=?";
		List queryList = this.jdbcTemplate.queryForList(sqlStr,objId);
		try {
			for (int i = 0; i < queryList.size(); i++) {
				Map queryMap = (Map) queryList.get(i);
				attributeId = ((BigDecimal) queryMap.get("ATTRIBUTE_ID")).toString();
				MetaAttribute attrObj = this.getAttribute(attributeId);
				if (attrObj != null){
					returnList.add(attrObj);
				}
			}
		} catch(Exception e) {
			log.error("attributeId：{}, Exception：{}", e.getMessage(), e);
		}
		return returnList;
	}

	@SuppressWarnings("unchecked")
	public List getAllAttribute() {
		String attributeId = "";
		List returnList = new ArrayList();
		List queryList = this.jdbcTemplate.queryForList(this.getAttribute);
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			attributeId = ((BigDecimal) queryMap.get("ATTRIBUTE_ID")).toString();
			MetaAttribute attrObj = this.getAttribute(attributeId);
			if (attrObj != null){
				returnList.add(attrObj);
			}
		}
		return returnList;
	}

	@SuppressWarnings("unchecked")
	public List getAllAttributeByCondition() {
		String attributeId = "";
		List returnList = new ArrayList();
		List queryList = this.jdbcTemplate.queryForList(this.getAllAttributeByCondition);
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			attributeId = ((BigDecimal) queryMap.get("ATTRIBUTE_ID")).toString();
			MetaAttribute attrObj = this.getAttribute(attributeId);
			if (attrObj != null){
				returnList.add(attrObj);
			}
		}
		return returnList;
	}

	public String getGetAttribute() {
		return getAttribute;
	}

	public void setGetAttribute(String getAttribute) {
		this.getAttribute = getAttribute;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String getGetAttributeInfo() {
		return getAttributeInfo;
	}

	public void setGetAttributeInfo(String getAttributeInfo) {
		this.getAttributeInfo = getAttributeInfo;
	}

	public String getGetAttrMapInfo() {
		return getAttrMapInfo;
	}

	public void setGetAttrMapInfo(String getAttrMapInfo) {
		this.getAttrMapInfo = getAttrMapInfo;
	}

	public String getGetTargetAttribute() {
		return getTargetAttribute;
	}

	public void setGetTargetAttribute(String getTargetAttribute) {
		this.getTargetAttribute = getTargetAttribute;
	}

	public String getGetAllAttributeByCondition() {
		return getAllAttributeByCondition;
	}

	public void setGetAllAttributeByCondition(String getAllAttributeByCondition) {
		this.getAllAttributeByCondition = getAllAttributeByCondition;
	}

}
