package com.timesontransfar.common.authorization.service.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSON;
import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.timesontransfar.common.analyzer.ISqlAnalyzer;
import com.timesontransfar.common.authorization.model.TsmCondition;
import com.timesontransfar.common.authorization.model.TsmControl;
import com.timesontransfar.common.authorization.model.TsmEntityPermit;
import com.timesontransfar.common.authorization.model.TsmFunction;
import com.timesontransfar.common.authorization.model.TsmMainMenu;
import com.timesontransfar.common.authorization.model.TsmOrganization;
import com.timesontransfar.common.authorization.model.TsmPopupMenu;
import com.timesontransfar.common.authorization.model.TsmRole;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IAuthorizationDAO;
import com.timesontransfar.common.authorization.service.IAuthorizationGeneDAO;
import com.timesontransfar.common.authorization.service.IStaffPermit;
import com.timesontransfar.common.authorization.service.IStaffPermitFactory;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.common.cache.exceptions.CacheException;
import com.timesontransfar.common.constant.RedisTimes;
import com.timesontransfar.common.database.KeyGenerator;
import com.timesontransfar.common.exception.ExceptionTransformerImpl;
import com.timesontransfar.common.metadata.IAccessMetaData;
import com.timesontransfar.common.metadata.model.MetaAttribute;
import com.timesontransfar.common.util.ObjectUtil;
import com.timesontransfar.customservice.common.PubFunc;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SystemAuthorizationWas implements ISystemAuthorization {
	private static final Logger logger = LoggerFactory.getLogger(SystemAuthorizationWas.class);
	
	@Autowired
    private HttpSession session;

	private ISqlAnalyzer sqlAnalyzer;

	private IAuthorizationDAO authorizationDAO;

	private IStaffPermitFactory staffPermitFactory;

	private IAuthorizationGeneDAO authorizationGeneDAO;

	private IAccessMetaData accessMetaData;

	private KeyGenerator keyGenerator;// 生成guid
	
	@Autowired
	private PubFunc pubFunc;

	@Autowired
	private RedisUtils redisUtils;

	/**
	 * 根据系统中设置的系统变量,将权限因子中的值替换成需要转装的值,并将其替换 add by qliang on 2006-06-08
	 *
	 * @param matchStr
	 *            权限因子中的存储的字符串
	 * @param staffPermit
	 *            员工信息
	 * @return 替换成需要转换的字符串
	 */
	private String getCorrectValue(String matchStr, IStaffPermit staffPermit) {
		if ("<STAFFID>".equalsIgnoreCase(matchStr)) { // 员工ID
			matchStr = staffPermit.getStaff().getId();
		} else if ("<ORGID>".equalsIgnoreCase(matchStr)) { // 组织机构ID
			matchStr = staffPermit.getStaff().getOrganizationId();
		} else if ("<AREAID>".equalsIgnoreCase(matchStr)) {// 地域ID
			matchStr = staffPermit.getAreaId();
		} else if ("<ORGLINKID>".equalsIgnoreCase(matchStr)) { // 组织机构LINKID
			matchStr = staffPermit.getOrgLinkID();
		} else if ("<AREALINKID>".equalsIgnoreCase(matchStr)) {// 地域LINKID
			matchStr = staffPermit.getAreaLinkId();
		}
		return matchStr;
	}

	public SystemAuthorizationWas() {
		super();
	}
	
	public String getAuthedSql(Map tableMap, String sql, String objId, String loginName) {
		logger.info("用户名是:"+loginName);
		IStaffPermit staffPermit = this.loadAuthorization(loginName, this.getHttpSession());
		return this.generateAuthedSql(tableMap, sql, objId, staffPermit);
	}

	public String getAuthedSql(Map tableMap, String sql, String objId) {
		if (this.getHttpSession() == null) {
			return sql;
		}	
		String loginName = this.pubFunc.getLogonStaff().getLogonName();
		logger.info("getAuthedSql loginName: {} objId: {}", loginName, objId);
		IStaffPermit staffPermit = this.loadAuthorization(loginName, this.getHttpSession());
		return this.generateAuthedSql(tableMap, sql, objId, staffPermit);
	}

	/**
	 * modify by qliang
	 *
	 * @param tableMap
	 * @param sql
	 * @param objectId
	 * @param staffPermit
	 * @return
	 */
	public String generateAuthedSql(Map tableMap, String sql, String objectId,
			IStaffPermit staffPermit) {
		String condition = " ";
		String groupClause = " ";
		String executeSql = this.sqlAnalyzer.readSql(sql) + " ";
		boolean findFlag = false;

		int groupIndex = executeSql.lastIndexOf(" GROUP BY ");
		if (groupIndex > 0) {
			findFlag = true;
			groupClause = executeSql.substring(groupIndex);
			executeSql = executeSql.substring(0, groupIndex);
		}
		if (!findFlag) {
			groupIndex = executeSql.lastIndexOf(" ORDER BY ");
			if (groupIndex > 0) {
				int lastWhere = executeSql.lastIndexOf(" WHERE ");
				if(groupIndex > lastWhere) {
					findFlag = true;
					groupClause = executeSql.substring(groupIndex);
					executeSql = executeSql.substring(0, groupIndex);
				}
			}
		}

		TsmEntityPermit dataPermit = null;
		int index = executeSql.lastIndexOf(" WHERE ");
		if (index >= 0) {
			condition = " AND ";
		} else {
			condition = " WHERE ";
		}
		if (executeSql.trim().toUpperCase().startsWith("SELECT")) {
			Object dataPermitObj = staffPermit.getDataMap().get("SELECT__" + objectId);
			if (dataPermitObj != null) {
				dataPermit = JSON.parseObject(dataPermitObj.toString(), TsmEntityPermit.class);
				condition += this.generateWhereClause(tableMap, dataPermit, staffPermit);
			} else {
				return sql;
			}
		} else if (executeSql.trim().toUpperCase().startsWith("UPDATE")) {
			dataPermit = (TsmEntityPermit) staffPermit.getDataMap().get(
					"UPDATE__" + objectId);
			if (dataPermit != null) {
				condition += this.generateWhereClause(tableMap, dataPermit,staffPermit);
			} else {
				return sql;
			}
		} else if (executeSql.trim().toUpperCase().startsWith("DELETE")) {
			dataPermit = (TsmEntityPermit) staffPermit.getDataMap().get(
					"DELETE__" + objectId);
			if (dataPermit != null) {
				condition += this.generateWhereClause(tableMap, dataPermit,staffPermit);
			} else {
				return sql;
			}
		} else if (executeSql.trim().toUpperCase().startsWith("INSERT")) {
			dataPermit = (TsmEntityPermit) staffPermit.getDataMap().get(
					"INSERT__" + objectId);
			if (dataPermit != null) {
				return executeSql;
			} else {
				return sql;
			}
		}
		logger.info("执行权限查询的SQLgenerateAuthedSql:--->"
						+ (executeSql + convertCondition(condition.trim()) + groupClause));
		return (executeSql + " " + convertCondition(condition.trim()) + groupClause);
	}

	private String convertCondition(String condition) {
		String strRtn = "";
		int i = condition.toUpperCase().indexOf("AND");
		if (i >= 0 && i < 4) {
			strRtn = " AND ( " + condition.substring(i + 3, condition.length())
					+ " ) ";
			return strRtn;
		}
		i = condition.toUpperCase().indexOf("OR");
		if (i >= 0 && i < 4) {
			strRtn = " AND ( " + condition.substring(i + 2, condition.length())
					+ " ) ";
		} else {
			strRtn = condition;
		}
		return strRtn;
	}

	/**
	 * 根据数据权限产生授权的SQL语句的条件字句
	 *
	 * @param tableMap
	 * @param dataPermit
	 * @return
	 */
	private String generateWhereClause(Map tableMap, TsmEntityPermit dataPermit,IStaffPermit staffPermit) {
		StringBuffer conditionStr = new StringBuffer(" ");
		List geneList = dataPermit.getGeneElements();
		try {
			for (int i = 0; i < geneList.size(); i++) {

				TsmCondition gene = JSON.parseObject(geneList.get(i).toString(), TsmCondition.class);
				// modify by qliang on 2006-6-9
				// 增加根据系统变量来修改权限因子的值,匹配并替换


				String correctValue = this.getCorrectValue(gene
						.getMatchValue(), staffPermit);

				// modify end
				String attrId = gene.getAttributeId();
				String redisData = this.redisUtils.get("ATTR__" + attrId,RedisType.WORKSHEET);
				MetaAttribute attribute = JSON.parseObject(redisData,MetaAttribute.class);

				if (attribute == null) {

					attribute = this.accessMetaData.getAttribute(attrId);

					if (attribute != null) {
						String attributeString = JSON.toJSONString(attribute);
						this.redisUtils.setex("ATTR__" + attrId,RedisTimes.DAYS,attributeString,RedisType.WORKSHEET);
					}
				}

				if (attribute != null) {
					String relation = gene.getJoinType();
					conditionStr.append(relation + " ");
					String aliasTable = " ";
					if (tableMap != null) {
						/*
						 * 假如能从表名和别名对照Map里面取到别名，则使用别名，否则使用属性的表名
						 */
						aliasTable = (String) tableMap.get(attribute
								.getAttrtable()) == null ? attribute
								.getAttrtable() : (String) tableMap
								.get(attribute.getAttrtable());
					} else {
						aliasTable = attribute.getAttrtable();
					}
					conditionStr.append(aliasTable + "." + attribute.getAttrcolumn());
					String value = correctValue;
					switch (gene.getOperateType()) {
					case 0:
						conditionStr.append(" = " + "'" + value + "' ");
						break;
					case 1:
						conditionStr.append(" > " + "'" + value + "' ");
						break;
					case 2:
						conditionStr.append(" >= " + "'" + value + "' ");
						break;
					case 3:
						conditionStr.append(" < " + "'" + value + "' ");
						break;
					case 4:
						conditionStr.append(" <= " + "'" + value + "' ");
						break;
					case 5:
						conditionStr.append(" <> " + "'" + value.trim() + "' ");
						break;
					case 6:
						conditionStr.append(" LIKE " + "'" + value.trim() + "%' ");
						break;
					case 7:
						conditionStr.append(" IN (" + getInValue(value) + " )");
						break;							
					default:
						conditionStr.append(" = " + "'" + value + "' ");
						break;
					}
				}
			}
		} catch (Exception e) {
		//异常处理逻辑
		}
		return conditionStr.toString();
	}

	private String getInValue(String value) {
		String[] valueArry = value.split(",");
		if(valueArry == null ) {
			return "";
		}
		int size = valueArry.length;
		String retValue=" ";
		for(int i=0;i<size;i++) {
			if(i == 0) {
				retValue +="'" +valueArry[i]+"'";
			} else {
				retValue +=",'" +valueArry[i]+"'";
			}			
		}
		return retValue;
	}
	
	public Map getMenuMap(String loginName, HttpSession session) {
		IStaffPermit permit = this.loadAuthorization(loginName, session);
		return permit.getMenuMap();
	}

	public IStaffPermit loadAuthorization(String loginName, HttpSession session) {
		if(null == loginName) {
			logger.info("LoadAuthorization loginName is null");
			return null;
		}
		
		IStaffPermit staffPermit = null;
		String redisData = this.redisUtils.get("LOGONNAME__"+loginName,RedisType.WORKSHEET);
		staffPermit = JSON.parseObject(redisData,IStaffPermit.class);
		if(staffPermit != null) {
			return staffPermit;
		}
		else {
			TsmStaff staff = null;
			try {
				staff = this.authorizationDAO.getStaffByLoginName(loginName);
				staffPermit = this.staffPermitFactory.getStaffPermit();
				staffPermit = this.setStaff(staff, staffPermit, false);

				String staffPermitString = JSON.toJSONString(staffPermit);
				this.redisUtils.setex("LOGONNAME__"+loginName,86400,staffPermitString,RedisType.WORKSHEET);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return staffPermit;
		}	
	}

	/**
	 * 重新从数据库中取员工对象,以更新cache的员工对象数据
	 *
	 * @param loginName
	 * @param session
	 * @return
	 */
	public IStaffPermit loadNewAuthorization(String loginName,
			HttpSession session) {
		IStaffPermit staffPermit = this.staffPermitFactory.getStaffPermit();
		TsmStaff staff = this.authorizationDAO.getStaffByLoginName(loginName);
		staffPermit = this.setStaffWithOutCache(staff, staffPermit);
		logger.debug("staffpermit sizeof" + ObjectUtil.sizeof(staffPermit));
		staffPermit.setLastSessionActiveTime(new Date());
		return staffPermit;
	}

	public IStaffPermit setTsmRole(List roleList, IStaffPermit staffPermit) {
		for (int i = 0; i < roleList.size(); i++) {
			Map map = (Map) roleList.get(i);
			String id = (String) map.get("ID");
			IStaffPermit rolePermit = null;
			try {
				String rolePermitString = this.redisUtils.get("ROLEPERMIT__" + id,RedisType.WORKSHEET);
				rolePermit = JSON.parseObject(rolePermitString, IStaffPermit.class);
			} catch (Throwable e) {
				//异常处理逻辑
			}
			if (rolePermit == null) {
				rolePermit = this.staffPermitFactory.getStaffPermit();
				rolePermit = this.getAllFunction(id, id, rolePermit);
				rolePermit = this.getAllDataPermit(id, id, id, rolePermit);
				rolePermit.setMenuMap(this.orderMenuMap(
						rolePermit.getMenuMap(), rolePermit));
				try {
					String rolePermitString = JSON.toJSONString(rolePermit);
					this.redisUtils.setex("ROLEPERMIT__" + id,86400,rolePermitString,RedisType.WORKSHEET);
				} catch (Throwable e) {
					//异常处理逻辑
				}
			}
			staffPermit.getControlMap().putAll(rolePermit.getControlMap());
			staffPermit.getDataMap().putAll(rolePermit.getDataMap());
			staffPermit.getPopupMenu().putAll(rolePermit.getPopupMenu());
			staffPermit.getUrlMap().putAll(rolePermit.getUrlMap());
			staffPermit.getMenuMap().putAll(rolePermit.getMenuMap());
		}
		staffPermit.setMenuMap(this.orderMenuMap(staffPermit.getMenuMap(),
				staffPermit));
		return staffPermit;
		// Auto-generated method stub
	}

	private IStaffPermit setTsmRoleWithoutCache(List roleList,
			IStaffPermit staffPermit) {
		for (int i = 0; i < roleList.size(); i++) {
			Map map = (Map) roleList.get(i);
			String id = (String) map.get("ID");
			IStaffPermit rolePermit = this.staffPermitFactory.getStaffPermit();
			rolePermit = this.getAllFunction(id, id, rolePermit);
			rolePermit = this.getAllDataPermit(id, id, id, rolePermit);
			rolePermit.setMenuMap(this.orderMenuMap(rolePermit.getMenuMap(),
					rolePermit));
			try {
				String staffPermitString = JSON.toJSONString(staffPermit);
				this.redisUtils.setex("ROLEPERMIT__" + id,86400,staffPermitString,RedisType.WORKSHEET);
			} catch (Throwable e) {
				//异常处理逻辑
			}
			staffPermit.getControlMap().putAll(rolePermit.getControlMap());
			staffPermit.getDataMap().putAll(rolePermit.getDataMap());
			staffPermit.getPopupMenu().putAll(rolePermit.getPopupMenu());
			staffPermit.getUrlMap().putAll(rolePermit.getUrlMap());
			staffPermit.getMenuMap().putAll(rolePermit.getMenuMap());
		}
		staffPermit.setMenuMap(this.orderMenuMap(staffPermit.getMenuMap(),
				staffPermit));
		return staffPermit;
		// Auto-generated method stub
	}

	/**
	 * 排序菜单列表
	 */
	private Map orderMenuMap(Map menuMap, IStaffPermit staffPermit) {
		Iterator iterator = menuMap.values().iterator();
		Map orderedMap = new TreeMap();
		while (iterator.hasNext()) {
			Object obj = iterator.next();
			TsmMainMenu item = new TsmMainMenu();
			if(obj instanceof TsmMainMenu) {
				item = (TsmMainMenu)obj;
			}
			else {
				item = JSON.parseObject(obj.toString(), TsmMainMenu.class);
			}
			if (item.getChildList() == null || item.getChildList().isEmpty()) {// 判断是否到了叶子节点
				orderedMap = this.findParent(item, orderedMap, staffPermit);
			}
		}
		Map tempMap = new TreeMap(orderedMap);
		iterator = tempMap.values().iterator();
		while (iterator.hasNext()) {
			TsmMainMenu item = (TsmMainMenu) iterator.next();
			orderedMap = this.findChild(item, orderedMap, staffPermit);
		}
		return orderedMap;
	}

	private Map findParent(TsmMainMenu item, Map orderedMap,
			IStaffPermit staffPermit) {
		List parentList = item.getParentList();
		if (parentList == null) {
			orderedMap.put(item.getId(), item);
		} else if (parentList.isEmpty()) {
			orderedMap.put(item.getId(), item);
		} else {
			for (int i = 0; i < parentList.size(); i++) {
				String parentId = (String) parentList.get(i);
				Object parentItemObj = staffPermit.getMenuMap().get(parentId);
				if (parentItemObj != null) {
					TsmMainMenu parentItem = new TsmMainMenu();
					if(parentItemObj instanceof TsmMainMenu) {
						parentItem = (TsmMainMenu)parentItemObj;
					}
					else {
						parentItem = JSON.parseObject(parentItemObj.toString(), TsmMainMenu.class);
					}
					orderedMap = this.findParent(parentItem, orderedMap,
							staffPermit);
				}
			}
		}
		return orderedMap;
	}

	private Map findChild(TsmMainMenu item, Map orderedMap,
			IStaffPermit staffPermit) {
		List childList = item.getChildList();
		if (childList == null) {
			orderedMap.put(item.getId(), item);
		} else if (childList.isEmpty()) {
			orderedMap.put(item.getId(), item);
		} else {
			for (int i = 0; i < childList.size(); i++) {
				String childId = (String) childList.get(i);
				Object childItemObj = staffPermit.getMenuMap().get(childId);
				if (childItemObj != null) {
					TsmMainMenu childItem = new TsmMainMenu();
					if(childItemObj instanceof TsmMainMenu) {
						childItem = (TsmMainMenu)childItemObj;
					}
					else {
						childItem = JSON.parseObject(childItemObj.toString(), TsmMainMenu.class);
					}
					orderedMap = this.findChild(childItem, orderedMap,
							staffPermit);
				}
			}
			orderedMap.put(item.getId(), item);
		}
		return orderedMap;
	}

	/**
	 * 根据角色递归查找所有的数据权限
	 *
	 * @param roleId
	 * @param currentRoleId
	 */
	private IStaffPermit getAllDataPermit(String roleId, String currentRoleId,
			String finalRoleId, IStaffPermit staffPermit) {
		if (roleId == null) {
			return null;
		}
		TsmRole role = this.authorizationDAO.getRole(roleId);
		List geneList = this.authorizationGeneDAO
				.getAllDataPermitGene(finalRoleId);
		List dataPermitList = this.authorizationGeneDAO
				.getAllDataPermit(roleId);
		for (int i = 0; i < dataPermitList.size(); i++) {
			TsmEntityPermit dataPermit = (TsmEntityPermit) dataPermitList
					.get(i);

			// 如果角色是本身，则取得所有的数据权限，不判断公有和私有
			if (roleId.equals(currentRoleId) || !dataPermit.isPrivate()) {
				for (int j = 0; j < geneList.size(); j++) {
					TsmCondition condition = (TsmCondition) geneList.get(j);
					if (((condition.getCondType() == dataPermit.getOperType()) || (condition
							.isCommonFlag()))
							&& (condition.getObjId().equals(dataPermit
									.getObjId()))) {

						dataPermit.getGeneElements().add(condition);
						switch (dataPermit.getOperType()) {
						case 0:
							staffPermit.getDataMap().put(
									"SELECT__" + dataPermit.getObjId(),
									dataPermit);

							break;
						case 1:
							staffPermit.getDataMap().put(
									"INSERT__" + dataPermit.getObjId(),
									dataPermit);
							break;
						case 2:
							staffPermit.getDataMap().put(
									"UPDATE__" + dataPermit.getObjId(),
									dataPermit);
							break;
						case 3:
							staffPermit.getDataMap().put(
									"DELETE__" + dataPermit.getObjId(),
									dataPermit);
							break;
						default:
							break;
						}
					}
				}
			}
		}
		// 访问父亲节点,递归查找
		List roleList = role.getParentRoles();
		for (int i = 0; i < roleList.size(); i++) {
			String id = (String) roleList.get(i);
			staffPermit = this.getAllDataPermit(id, currentRoleId, finalRoleId,
					staffPermit);
		}
		return staffPermit;
	}

	// public String generateSQL(String querySql,)

	private IStaffPermit generateFunctionPermit(String id,IStaffPermit staffPermit){
		try {

			String redisData = this.redisUtils.get("FUNC__" + id,RedisType.WORKSHEET);
			TsmFunction function = JSON.parseObject(redisData,TsmFunction.class);

			if(function == null){
				function=this.authorizationDAO.getFunction(id);
				String functionString = JSON.toJSONString(function);
				this.redisUtils.setex("FUNC__" + id,86400,functionString,RedisType.WORKSHEET);
			}
			if (function != null) {
				String constraint = function.getConstraint();
				if (constraint != null) {
					String funcConstraint = constraint;
					// 功能权限:系统主菜单
					if (funcConstraint.startsWith("MENU__")) {
						String menuId = funcConstraint;
						String menuData = this.redisUtils.get(menuId,RedisType.WORKSHEET);
						TsmMainMenu menu = JSON.parseObject(menuData,TsmMainMenu.class);
						if (menu == null) {
							menu = this.authorizationDAO.getMainMenu(function.getId());
							if(menu!=null){
								String menuString = JSON.toJSONString(menu);
								this.redisUtils.setex(menuId,86400,menuString,RedisType.WORKSHEET);
							}
						}
						if(menu!=null){
							if (menu.getUrl() != null) {
								staffPermit.getUrlMap().put(menu.getUrl(), true);
							}
							staffPermit.getMenuMap()
									.put(menu.getId(), menu);
						}
					}
					// 功能权限:控件
					else if (funcConstraint.startsWith("CTRL__")) {
						String ctrlId = funcConstraint;
						String controlData = this.redisUtils.get(ctrlId,RedisType.WORKSHEET);
						TsmControl control = JSON.parseObject(controlData,TsmControl.class);
						if(control==null){
							control=this.authorizationDAO.getControl(function.getId());
							if(control!=null){
								String controlString = JSON.toJSONString(control);
								this.redisUtils.setex(ctrlId,86400,controlString,RedisType.WORKSHEET);
							}
						}
						if (control != null) {
							if (control.getUrl() != null) {

								staffPermit.getUrlMap()
										.put(control.getUrl(), true);
							}
							staffPermit.getControlMap().put(
									control.getId(), control);
						}
					}
					// 功能权限:系统右键菜单 by chenke added 2006-05-30
					else if (funcConstraint.startsWith("POP__")) {
						String ctrlId = funcConstraint;
						String menuData = this.redisUtils.get(ctrlId,RedisType.WORKSHEET);
						TsmPopupMenu popupMenu = JSON.parseObject(menuData,TsmPopupMenu.class);
						if(popupMenu==null){
							popupMenu=this.authorizationDAO.getPopupMenu(function.getId());
							if(popupMenu!=null){
								String popupMenuString = JSON.toJSONString(popupMenu);
								this.redisUtils.setex(ctrlId,86400,popupMenuString,RedisType.WORKSHEET);
							}
						}
						if (popupMenu != null) {
							if (popupMenu.getUrl() != null) {

								staffPermit.getUrlMap().put(popupMenu.getUrl(), true);
							}
							staffPermit.getPopupMenu().put(
									popupMenu.getMenuId(), popupMenu);
						}
					}
				}
			}else{
				throw new RuntimeException("权限数据配置存在问题，对应的功能不存在，功能ID是:"+id);
			}
		} catch (Throwable e) {
			if(e instanceof CacheException){
				e.printStackTrace();
			}else{
				String eInfo=ExceptionTransformerImpl.transformException(e);
				throw new RuntimeException("装载权限出现错误："+eInfo);
			}
		}
		return staffPermit;
	}

	// public String generateSQL(String querySql,)

	/**
	 * 取得用户能够访问的所有功能
	 *
	 * @param roleId
	 * @param currentRoleId
	 */
	private IStaffPermit getAllFunction(String roleId, String currentRoleId,
			IStaffPermit staffPermit) {
		if (roleId == null) {
			return null;
		}
		TsmRole role = this.authorizationDAO.getRole(roleId);
		// 取用户所有的功能权限
		List publicFunctionList = role.getPublicFunctionPermit();
		for (int i = 0; i < publicFunctionList.size(); i++) {
			String id = (String) publicFunctionList.get(i);
			staffPermit = this.generateFunctionPermit(id,staffPermit);
		}
		if (roleId.equals(currentRoleId)) {
			List privateFunctionList = role.getPrivateFunctionPermit();
			for (int i = 0; i < privateFunctionList.size(); i++) {
				String id = (String) privateFunctionList.get(i);
				staffPermit = this.generateFunctionPermit(id,staffPermit);
			}
		}
		// 访问父亲节点,递归查找
		List roleList = role.getParentRoles();
		for (int i = 0; i < roleList.size(); i++) {
			String id = (String) roleList.get(i);
			staffPermit = this.getAllFunction(id, currentRoleId, staffPermit);
		}
		return staffPermit;
	}

	public IStaffPermit setStaff(TsmStaff staff, IStaffPermit staffPermit,
			boolean inCache) {
		if (!inCache) {
			staffPermit.setStaff(staff);
			TsmOrganization org = this.authorizationDAO.getOrganization(staffPermit.getStaff().getOrganizationId());
			staffPermit.setOrgLinkID(org.getAppId());
			staffPermit.setAreaId(org.getRegionId());
			staffPermit.setAreaLinkId(org.getRegionLinkId());
			staffPermit.setOrgLinkID(org.getAppId());
			staffPermit.setAreaId(org.getRegionId());
			staffPermit.setAreaLinkId(org.getRegionLinkId());
			staffPermit.setAreaName(org.getRegionName());
			// 20061212
			String orgState = org.getState();
			int intOrgState = 20;
			if (StringUtils.isNotEmpty(orgState)) {
				intOrgState = Integer.parseInt(orgState);
			}
			staffPermit.setOrgState(intOrgState);
		}
		staffPermit = this.setTsmRole(staff.getRoleList(), staffPermit);
		
		try {
			String staffPermitString = JSON.toJSONString(staffPermit);
			this.redisUtils.setex("LOGONNAME__"+staff.getLogonName(),3600,staffPermitString,RedisType.WORKSHEET);
		} catch (Exception e) {
			logger.error("setStaff 异常：" + e.getMessage(), e);
		}
		return staffPermit;
	}

	private IStaffPermit setStaffWithOutCache(TsmStaff staff,
			IStaffPermit staffPermit) {
		staffPermit.setStaff(staff);
		TsmOrganization org = this.authorizationDAO.getOrganization(staffPermit
				.getStaff().getOrganizationId());
		staffPermit.setOrgLinkID(org.getAppId());
		staffPermit.setAreaId(org.getRegionId());
		staffPermit.setAreaLinkId(org.getRegionLinkId());
		staffPermit.setOrgLinkID(org.getAppId());
		staffPermit.setAreaId(org.getRegionId());
		staffPermit.setAreaLinkId(org.getRegionLinkId());
		staffPermit.setAreaName(org.getRegionName());
		// 20061212
		String orgState = org.getState();
		int intOrgState = 20;
		if (StringUtils.isNotEmpty(orgState)) {
			intOrgState = Integer.parseInt(orgState);
		}
		staffPermit.setOrgState(intOrgState);
		staffPermit = this.setTsmRoleWithoutCache(staff.getRoleList(),
				staffPermit);
		try {
			String staffPermitString = JSON.toJSONString(staffPermit);
			this.redisUtils.setex("LOGONNAME__"+staff.getLogonName(),3600,staffPermitString,RedisType.WORKSHEET);
		} catch (Exception e) {
			logger.error("setStaffWithOutCache 异常：" + e.getMessage(), e);
		}
		return staffPermit;
	}
	
	public HttpSession getHttpSession() {
		return this.session;
	}

	public IAuthorizationDAO getAuthorizationDAO() {
		return authorizationDAO;
	}

	public void setAuthorizationDAO(IAuthorizationDAO authorizationDAO) {
		this.authorizationDAO = authorizationDAO;
	}

	public IStaffPermitFactory getStaffPermitFactory() {
		return staffPermitFactory;
	}

	public void setStaffPermitFactory(IStaffPermitFactory staffPermitFactory) {
		this.staffPermitFactory = staffPermitFactory;
	}

	public IAuthorizationGeneDAO getAuthorizationGeneDAO() {
		return authorizationGeneDAO;
	}

	public void setAuthorizationGeneDAO(
			IAuthorizationGeneDAO authorizationGeneDAO) {
		this.authorizationGeneDAO = authorizationGeneDAO;
	}

	public IAccessMetaData getAccessMetaData() {
		return accessMetaData;
	}

	public void setAccessMetaData(IAccessMetaData accessMetaData) {
		this.accessMetaData = accessMetaData;
	}

	public ISqlAnalyzer getSqlAnalyzer() {
		return sqlAnalyzer;
	}

	public void setSqlAnalyzer(ISqlAnalyzer sqlAnalyzer) {
		this.sqlAnalyzer = sqlAnalyzer;
	}

	public KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}
}
