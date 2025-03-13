package com.timesontransfar.common.authorization.model.service;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.timesontransfar.common.authorization.model.TsmAttrInfo;
import com.timesontransfar.common.authorization.model.TsmCondition;
import com.timesontransfar.common.authorization.model.TsmControl;
import com.timesontransfar.common.authorization.model.TsmEntityInfo;
import com.timesontransfar.common.authorization.model.TsmEntityPermit;
import com.timesontransfar.common.authorization.model.TsmFunction;
import com.timesontransfar.common.authorization.model.TsmMainMenu;
import com.timesontransfar.common.authorization.model.TsmOrganization;
import com.timesontransfar.common.authorization.model.TsmPopupMenu;
import com.timesontransfar.common.authorization.model.TsmRole;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;

/**
 * 用于手工映射数据库到权限相关对象 继承对象:AbstractRowMapper
 *
 * @version 0.1
 * @author 罗翔 2005-12-8 创建
 *2006-8-23 梁勇增加 case :21
 *	李峰 2007-10-10 修改 mappingMainMenu中添加system_id 列
 */
public class AuthRowMapper extends AbstractRowMapper {
	
	public Object mapRow(ResultSet rs, int index) throws SQLException {
		Object object = null;
		switch (this.getIntClassType()) {
			case 1:
				object = this.mappingStaff(rs);
				break;
			case 2:
				object = this.mappingOrganization(rs);
				break;
			case 3:
				object = this.mappingRole(rs);
				break;
			case 4:
				object = this.mappingMainMenu(rs);
				break;
			case 5:
				object = this.mappingCondition(rs);
				break;
			case 6:
				object = this.mappingRoleDataPermit(rs);
				break;
			case 7:
				object = this.mappingFunction(rs);
				break;
			case 8:
				object = this.mappingEntityPermit(rs);
				break;
			case 9:
				object = this.mappingFuncPermit(rs);
				break;
			case 10:
				object = this.mappingEntityInfo(rs);
				break;
			case 11:
				object = this.mappingAttrInfo(rs);
				break;
			// 取控件权限 add by chenke 2005-12-22
			case 12:
				object = this.mappingControl(rs);
				break;
			case 13:
				object = this.mappingModifyFuncObj(rs);
				break;
			case 14:
				object = this.mappingFuncPermitAll(rs);
				break;
			case 15:
				object = this.mappingFuncPermitByName(rs);
				break;
			case 16:
				object = this.mappingAllRole(rs);
				break;
			// 取控件权限 add by chenke 2006-05-30
			case 17:
				object = this.mappingPopupMenu(rs);
				break;
			case 18:
				object = this.mappingRoleInfo(rs);
				break;
			case 19:
				object = this.mappingFuncPermitRole(rs);
				break;
			case 20:
				object = this.mappingRoleDataPermitRole(rs);
				break;
			case 22:
				object = this.mappingStaffObject(rs);
				break;
			case 23:
				object = this.mappingNewOrganization(rs);
				break;
			case 24:
				object = this.mappingNewStaffInfoByOrgId(rs);
				break;
			default:
				break;
		}
		return object;
	}



	private Object mappingEntityInfo(ResultSet rs) throws SQLException {
		TsmEntityInfo obj = new TsmEntityInfo();
		obj.setEntityId(rs.getString("OBJ_ID"));
		obj.setEntityName(rs.getString("OBJ_NAME"));
		obj.setEntityDesc(rs.getString("OBJ_DESC"));
		return obj;
	}

	private Object mappingAttrInfo(ResultSet rs) throws SQLException {
		TsmAttrInfo obj = new TsmAttrInfo();
		obj.setAttrId(rs.getString("ATTRIBUTE_ID"));
		obj.setAttrName(rs.getString("ATTR_NAME"));
		obj.setAttrDesc(rs.getString("ATTR_DESCRIPTION"));
		return obj;
	}

	private Object mappingStaffObject(ResultSet rs) throws SQLException {
		TsmStaff staff = new TsmStaff();
		staff.setId(rs.getString("STAFF_ID"));
		staff.setName(rs.getString("STAFFNAME"));
		staff.setOrgName(rs.getString("ORG_NAME"));
		staff.setCreateDate(rs.getTimestamp("CRE_DATE"));
		staff.setCreatedStaff(rs.getString("CRE_STAFF"));
		staff.setGender(rs.getInt("GENDER"));
		staff.setLevel(rs.getInt("STAFF_LEVEL"));
		staff.setLogonName(rs.getString("LOGONNAME"));
		staff.setModifyDate(rs.getTimestamp("MODIFY_DATE"));
		staff.setModifyStaff(rs.getString("MODIFY_STAFF"));
		staff.setOrganizationId(rs.getString("ORG_ID"));
		staff.setPassword(rs.getString("PASSWORD"));
		staff.setRelaMail(rs.getString("RELAEMAIL"));
		staff.setRelaPhone(rs.getString("RELAPHONE"));
		staff.setState(rs.getInt("STATE"));
//		此处梁勇修改于 2006-8-27 下午
		staff.setStaffStartDate(rs.getTimestamp("STAFFSTART_DATE"));
		staff.setStaffEndDate(rs.getTimestamp("STAFFEND_DATE"));
		staff.setIsSSO(rs.getInt("IS_SSO"));

		try {
			staff.setLimitDate(rs.getString("LIMITDATE") == null ? "" : rs
					.getString("LIMITDATE"));
		} catch (Exception ex) {
			staff.setLimitDate("");
		}
		// add by liangli 20070718 新增字段
		try {
			staff.setIsSSO(rs.getObject("IS_SSO") == null ? staff.getIsSSO()
					: rs.getInt("IS_SSO"));
		} catch (Exception ex) {
			//异常处理逻辑
		}
		try {
			staff.setExpDate(rs.getString("EXP_DATE") == null ? staff
					.getExpDate() : rs.getString("EXP_DATE"));
		} catch (Exception ex) {
			//异常处理逻辑
		}
		try {
			staff.setLoginTimes(rs.getObject("LOGIN_TIMES") == null ? staff
					.getLoginTimes() : rs.getInt("LOGIN_TIMES"));
		} catch (Exception ex) {
			//异常处理逻辑
		}
		/*
		 * liangyong adds on 20061206 #######################################################
		 */
		staff.setBirthday(rs.getTimestamp("BIRTHDAY"));
		staff.setIdentity(rs.getString("IDENTITY"));
		staff.setWorkTime(rs.getTimestamp("WORKTIME"));
		staff.setAddress(rs.getString("ADDRESS"));
		/*
		 * ##################################################################################
		 */
		return staff;
	}

	private Object mappingStaff(ResultSet rs) throws SQLException {
		TsmStaff staff = new TsmStaff();
		staff.setId(rs.getString("STAFF_ID"));
		staff.setName(rs.getString("STAFFNAME"));
		staff.setOrgName(rs.getString("ORG_NAME"));
		staff.setCreateDate(rs.getTimestamp("CRE_DATE"));
		staff.setCreatedStaff(rs.getString("CRE_STAFF"));
		staff.setGender(rs.getInt("GENDER"));
		staff.setLevel(rs.getInt("STAFF_LEVEL"));
		staff.setLogonName(rs.getString("LOGONNAME"));
		staff.setModifyDate(rs.getTimestamp("MODIFY_DATE"));
		staff.setModifyStaff(rs.getString("MODIFY_STAFF"));
		staff.setOrganizationId(rs.getString("ORG_ID"));
		staff.setPassword(rs.getString("PASSWORD"));
		staff.setRelaMail(rs.getString("RELAEMAIL"));
		staff.setRelaPhone(rs.getString("RELAPHONE"));
		staff.setState(rs.getInt("STATE"));
//		此处梁勇修改于 2006-8-27 下午
		staff.setStaffStartDate(rs.getTimestamp("STAFFSTART_DATE"));
		staff.setStaffEndDate(rs.getTimestamp("STAFFEND_DATE"));
		staff.setIsSSO(rs.getInt("IS_SSO"));
		try {
			staff.setLimitDate(rs.getString("LIMITDATE") == null ? "" : rs
					.getString("LIMITDATE"));
		} catch (Exception ex) {
			staff.setLimitDate("");
		}
		// add by liangli 20070718 新增字段
		try {
			staff.setIsSSO(rs.getObject("IS_SSO") == null ? staff.getIsSSO()
					: rs.getInt("IS_SSO"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			staff.setExpDate(rs.getString("EXP_DATE") == null ? staff
					.getExpDate() : rs.getString("EXP_DATE"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			staff.setLoginTimes(rs.getObject("LOGIN_TIMES") == null ? staff
					.getLoginTimes() : rs.getInt("LOGIN_TIMES"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		/*
		 * liangyong adds on 20061206 #######################################################
		 */
		staff.setBirthday(rs.getTimestamp("BIRTHDAY"));
		staff.setIdentity(rs.getString("IDENTITY"));
		staff.setWorkTime(rs.getTimestamp("WORKTIME"));
		staff.setAddress(rs.getString("ADDRESS"));
		/*
		 * ##################################################################################
		 */
		return staff;
	}

	private Object mappingOrganization(ResultSet rs) throws SQLException {
		TsmOrganization organization = new TsmOrganization();
		organization.setAddrDesc(rs.getString("ADDR_DESC"));
		organization.setAppId(rs.getString("LINKID"));
		organization.setCreateDate(rs.getDate("CRE_DATE"));
		organization.setCreateStaff(rs.getString("CRE_STAFF"));
		organization.setFuntionType(rs.getInt("FUNCTIONTYPE"));
		organization.setId(rs.getString("ORG_ID"));
		organization.setLevel(rs.getInt("ORG_LEVEL"));
		organization.setModifyDate(rs.getDate("MODIFY_DATE"));
		organization.setModifyStaff(rs.getString("MODIFY_STAFF"));
		organization.setName(rs.getString("ORG_NAME"));
		organization.setParentId(rs.getString("UP_ORG"));
		organization.setPrincipal(rs.getString("PRINCIPAL"));
		organization.setRelaPhone(rs.getString("RELAPHONE"));
		organization.setState(rs.getString("STATE"));
		organization.setRegionId(rs.getString("REGION_ID"));
		//organization.setRegionLinkId(rs.getString("LINK_ID"));
		try {
		    organization.setOrganizationType(rs.getInt("ORGANIZATION_TYPE"));//ORGANIZATION_TYPE 2006-8-23梁勇增加
		} catch (Exception ex){
			//异常处理逻辑
		}
		return organization;
	}

	private Object mappingNewOrganization(ResultSet rs) throws SQLException {
		TsmOrganization organization = new TsmOrganization();
		organization.setAddrDesc(rs.getString("ADDR_DESC"));
		organization.setAppId(rs.getString("LINKID"));
		organization.setCreateDate(rs.getDate("CRE_DATE"));
		organization.setCreateStaff(rs.getString("CRE_STAFF"));
		organization.setFuntionType(rs.getInt("FUNCTIONTYPE"));
		organization.setId(rs.getString("ORG_ID"));
		organization.setLevel(rs.getInt("ORG_LEVEL"));
		organization.setModifyDate(rs.getDate("MODIFY_DATE"));
		organization.setModifyStaff(rs.getString("MODIFY_STAFF"));
		organization.setName(rs.getString("ORG_NAME"));
		organization.setParentId(rs.getString("UP_ORG"));
		organization.setPrincipal(rs.getString("PRINCIPAL"));
		organization.setRelaPhone(rs.getString("RELAPHONE"));
		organization.setState(rs.getString("STATE"));
		organization.setRegionId(rs.getString("REGION_ID"));
		organization.setRegionLinkId(rs.getString("LINK_ID"));
		/*
	     * ################liangyong adds on 20061204 #####################################
	     */
	    organization.setOrgOwner(rs.getString("ORGOWNER"));
	    organization.setOrgFax(rs.getString("ORGFAX"));
	    /*
	     * ##################################################################################
	     */
	    organization.setRegionName(rs.getString("REGION_NAME"));	//地域名称
		try {
		    organization.setOrganizationType(rs.getInt("ORGANIZATION_TYPE"));//ORGANIZATION_TYPE 2006-8-23梁勇增加
		} catch (Exception ex){
			//异常处理逻辑
		}
		return organization;
	}

	private Object mappingRole(ResultSet rs) throws SQLException {
		TsmRole role = new TsmRole();
		role.setCreateDate(rs.getDate("CRE_DATE"));
		role.setCreateStaff(rs.getString("CRE_STAFF"));
		role.setId(rs.getString("ROLE_ID"));
		role.setModifyDate(rs.getDate("MODIFY_DATE"));
		role.setModifyStaff(rs.getString("MODIFY_STAFF"));
		role.setName(rs.getString("ROLE_NAME"));
		role.setOrgId(rs.getString("ORG_ID"));
		role.setRoleOrg(rs.getString("ROLE_ORG"));
		role.setState(rs.getBoolean("STATE"));
		role.setType(rs.getInt("ROLE_TYPE"));
		role.setExpireDate(rs.getDate("EXPIRE_DATE"));
		role.setEffectDate(rs.getDate("EFFECT_DATE"));
		role.setRoleReportGrade(rs.getString("ROLE_REPORT_GRADE"));
		role.setRoleRuleType(rs.getString("ROLE_RULE_TYPE"));
		role.setBaseFlag(rs.getString("BASE_FLAG"));
		return role;
	}

	private Object mappingAllRole(ResultSet rs) throws SQLException {
		TsmRole role = new TsmRole();
		role.setOrgName(rs.getString("ORG_NAME"));
		role.setCreateDate(rs.getDate("CRE_DATE"));
		role.setCreateStaff(rs.getString("CRE_STAFF"));
		role.setId(rs.getString("ROLE_ID"));
		role.setModifyDate(rs.getDate("MODIFY_DATE"));
		role.setModifyStaff(rs.getString("MODIFY_STAFF"));
		role.setName(rs.getString("ROLE_NAME"));
		role.setOrgId(rs.getString("ORG_ID"));
		role.setRoleOrg(rs.getString("ROLE_ORG"));
		role.setState(rs.getBoolean("STATE"));
		role.setType(rs.getInt("ROLE_TYPE"));
		role.setExpireDate(rs.getDate("EXPIRE_DATE"));
		role.setEffectDate(rs.getDate("EFFECT_DATE"));
		return role;
	}

	private Object mappingModifyFuncObj(ResultSet rs) throws SQLException {
		TsmFunction tsmFunction = new TsmFunction();
		tsmFunction.setId(rs.getString("FUNC_OPERATE_ID"));
		tsmFunction.setFuncName(rs.getString("FUNC_OPERATE_NAME"));
		tsmFunction.setFuncDesc(rs.getString("FUNC_OPERATE_DESC"));
		tsmFunction.setOperaType(rs.getInt("FUNC_OPERATE_TYPE"));
		tsmFunction.setMenuName(rs.getString("MENU_NAME"));
		tsmFunction.setOperaTypeId(rs.getString("MENU_ID"));
		if (rs.getString("STATE").equals("1")) {
			tsmFunction.setCanBeUse(true);
		} else {
			tsmFunction.setCanBeUse(false);
		}
		return tsmFunction;
	}

	private Object mappingRoleDataPermit(ResultSet rs) throws SQLException {
		TsmEntityPermit permit = new TsmEntityPermit();
		permit.setId(rs.getString("DATAOPERID"));
		permit.setObjId(rs.getString("OBJ_ID"));
		permit.setObjName(rs.getString("OBJ_NAME"));
		permit.setPrivate(rs.getBoolean("ISPRIVATE"));
		permit.setOperType(rs.getInt("OPERTYPE"));
		return permit;
	}

	private Object mappingEntityPermit(ResultSet rs) throws SQLException {
		TsmEntityPermit entityPermit = new TsmEntityPermit();
		entityPermit.setId(rs.getString("DATAOPERID"));
		entityPermit.setObjId(rs.getString("OBJ_ID"));
		entityPermit.setObjName(rs.getString("OBJ_NAME"));
		entityPermit.setObjDesc(rs.getString("OBJ_DESC"));
		entityPermit.setOperType(rs.getInt("OPERTYPE"));
		entityPermit.setState(rs.getInt("STATE"));
		return entityPermit;
	}

	private Object mappingCondition(ResultSet rs) throws SQLException {
		TsmCondition condition = new TsmCondition();
		condition.setId(rs.getString("REST_COND_ID"));
		condition.setAttributeId(rs.getString("ATTRIBUTE_ID"));
		condition.setCommonFlag(rs.getBoolean("ISCOMMON"));
		condition.setJoinType(rs.getString("RELATION") == null ? "" : rs
				.getString("RELATION"));
		condition.setLinkSeq(rs.getInt("CONDLEVEL"));
		condition.setMatchValue(rs.getString("COND_VALUE"));
		condition.setOperateType(rs.getInt("OPERATE_ID"));
		condition.setObjId(rs.getString("OBJ_ID"));
		condition.setObjName(rs.getString("OBJ_NAME"));
		condition.setAttributeName(rs.getString("ATTR_NAME"));
		condition.setDataRoleId(rs.getString("DATAROLEID"));
		condition.setCondType(rs.getInt("OPERTYPE"));
		return condition;
	}

	private Object mappingFunction(ResultSet rs) throws SQLException {
		TsmFunction function = new TsmFunction();
		function.setFuncName(rs.getString("FUNC_OPERATE_NAME"));
		function.setId(rs.getString("FUNC_OPERATE_ID"));
		function.setFuncDesc(rs.getString("FUNC_OPERATE_DESC"));
		function.setOperaType(rs.getInt("FUNC_OPERATE_TYPE"));
		return function;
	}

	private Object mappingMainMenu(ResultSet rs) throws SQLException {
		TsmMainMenu mainMenu = new TsmMainMenu();
		mainMenu.setId(rs.getString("MENU_ID"));
		mainMenu.setItemFlag(rs.getBoolean("IS_MENUITEM"));
		mainMenu.setName(rs.getString("MENU_NAME"));
		mainMenu.setOpenType(rs.getString("OPENTYPE"));
		mainMenu.setSequence(rs.getInt("MENU_SEQ"));
		mainMenu.setTopFlag(rs.getBoolean("IS_TOP_MENU"));
		mainMenu.setUrl(rs.getString("URL"));
		mainMenu.setSystemId(rs.getInt("SYSTEM_ID"));
		//WrokTime格式：* * 1,2,3 * * ?
		mainMenu.setWorkTime(rs.getString("WORKTIME"));
		return mainMenu;
	}

	private Object mappingFuncPermitAll(ResultSet rs) throws SQLException {
		TsmFunction tsmFunction = new TsmFunction();
		tsmFunction.setId(rs.getString("FUNC_OPERATE_ID"));
		tsmFunction.setFuncName(rs.getString("FUNC_OPERATE_NAME"));
		tsmFunction.setFuncDesc(rs.getString("FUNC_OPERATE_DESC"));
		tsmFunction.setOperaType(rs.getInt("FUNC_OPERATE_TYPE"));
		tsmFunction.setMenuName(rs.getString("MENU_NAME"));
		if (rs.getString("STATE").equals("1")) {
			tsmFunction.setCanBeUse(true);
		} else {
			tsmFunction.setCanBeUse(false);
		}
		return tsmFunction;
	}

	private Object mappingFuncPermit(ResultSet rs) throws SQLException {
		TsmFunction tsmFunction = new TsmFunction();
		tsmFunction.setId(rs.getString("FUNC_OPERATE_ID"));
		tsmFunction.setFuncName(rs.getString("FUNC_OPERATE_NAME"));
		tsmFunction.setFuncDesc(rs.getString("FUNC_OPERATE_DESC"));
		tsmFunction.setOperaType(rs.getInt("FUNC_OPERATE_TYPE"));
		if (rs.getString("STATE").equals("1")) {
			tsmFunction.setCanBeUse(true);
		} else {
			tsmFunction.setCanBeUse(false);
		}
		if (rs.getString("ISPRIVATE").equals("1"))
			tsmFunction.setIsprivate(true);
		else
			tsmFunction.setIsprivate(false);
		return tsmFunction;
	}

	private Object mappingFuncPermitByName(ResultSet rs) throws SQLException {
		TsmFunction tsmFunction = new TsmFunction();
		tsmFunction.setId(rs.getString("FUNC_OPERATE_ID"));
		tsmFunction.setFuncName(rs.getString("FUNC_OPERATE_NAME"));
		tsmFunction.setFuncDesc(rs.getString("FUNC_OPERATE_DESC"));
		tsmFunction.setOperaType(rs.getInt("FUNC_OPERATE_TYPE"));
		if (rs.getString("STATE").equals("1")) {
			tsmFunction.setCanBeUse(true);
		} else {
			tsmFunction.setCanBeUse(false);
		}
		return tsmFunction;
	}

	// 取控件权限 add by chenke 2005-12-22
	private Object mappingControl(ResultSet rs) throws SQLException {
		TsmControl obj = new TsmControl();
		obj.setId(rs.getString("CONTROL_ID"));
		obj.setName(rs.getString("CONTROL_NAME"));
		obj.setUrl(rs.getString("URL"));
		obj.setCtrlType(rs.getString("CONTROL_TYPE"));
		return obj;
	}

	// 取控件权限 add by chenke 2006-05-30
	private Object mappingPopupMenu(ResultSet rs) throws SQLException {
		TsmPopupMenu obj = new TsmPopupMenu();
		obj.setMenuId(rs.getString("MENU_ID")); // 菜单ID
		obj.setMenuName(rs.getString("MENU_NAME")); // 菜单名称
		obj.setUrl(rs.getString("URL")); // 功能URL
		obj.setMenuSeq(rs.getString("MENU_SEQ")); // 显示顺序
		obj.setFuncCode(rs.getString("FUNC_CODE")); // 功能编号
		obj.setIsMenuitem(rs.getString("IS_MENUITEM")); // 是否为菜单项
		obj.setIsTopMenu(rs.getString("IS_TOP_MENU")); // 是否为顶级菜单
		obj.setMenuGroup(rs.getString("MENU_GROUP")); // 菜单组编号
		return obj;
	}

	private Object mappingRoleInfo(ResultSet rs) throws SQLException {
		TsmRole role = new TsmRole();
		role.setCreateDate(rs.getDate("CRE_DATE"));
		role.setCreateStaff(rs.getString("CRE_STAFF"));
		role.setId(rs.getString("ROLE_ID"));
		role.setModifyDate(rs.getDate("MODIFY_DATE"));
		role.setModifyStaff(rs.getString("MODIFY_STAFF"));
		role.setName(rs.getString("ROLE_NAME"));
		role.setOrgId(rs.getString("ORG_ID"));
		role.setRoleOrg(rs.getString("ROLE_ORG"));
		role.setState(rs.getBoolean("STATE"));
		role.setType(rs.getInt("ROLE_TYPE"));
		role.setExpireDate(rs.getDate("EXPIRE_DATE"));
		role.setEffectDate(rs.getDate("EFFECT_DATE"));
		role.setRoleReportGrade(rs.getString("COL_VALUE_NAME"));
		return role;
	}

	private Object mappingFuncPermitRole(ResultSet rs) throws SQLException {
		TsmFunction tsmFunction = new TsmFunction();
		tsmFunction.setId(rs.getString("FUNC_OPERATE_ID"));
		tsmFunction.setFuncName(rs.getString("FUNC_OPERATE_NAME"));
		tsmFunction.setFuncDesc(rs.getString("FUNC_OPERATE_DESC"));
		tsmFunction.setOperaType(rs.getInt("FUNC_OPERATE_TYPE"));
		if (rs.getString("STATE").equals("1")) {
			tsmFunction.setCanBeUse(true);
		} else {
			tsmFunction.setCanBeUse(false);
		}
		if (rs.getString("ISPRIVATE").equals("1"))
			tsmFunction.setIsprivate(true);
		else
			tsmFunction.setIsprivate(false);
		tsmFunction.setRoleName(rs.getString("ROLE_NAME"));
		return tsmFunction;
	}

	private Object mappingRoleDataPermitRole(ResultSet rs) throws SQLException {
		TsmEntityPermit permit = new TsmEntityPermit();
		permit.setId(rs.getString("DATAOPERID"));
		permit.setObjId(rs.getString("OBJ_ID"));
		permit.setObjName(rs.getString("OBJ_NAME"));
		permit.setPrivate(rs.getBoolean("ISPRIVATE"));
		permit.setOperType(rs.getInt("OPERTYPE"));
		permit.setRoleName(rs.getString("ROLE_NAME"));
		return permit;
	}

	private Object mappingNewStaffInfoByOrgId(ResultSet rs) throws SQLException{
		TsmStaff staff = new TsmStaff();
		staff.setId(rs.getString("STAFF_ID"));
		staff.setName(rs.getString("STAFFNAME"));
		staff.setOrgName(rs.getString("ORG_NAME"));
		return staff;
	}
}

