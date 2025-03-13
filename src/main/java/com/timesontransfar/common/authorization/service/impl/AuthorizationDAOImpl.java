package com.timesontransfar.common.authorization.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import com.timesontransfar.common.authorization.model.TsmControl;
import com.timesontransfar.common.authorization.model.TsmFunction;
import com.timesontransfar.common.authorization.model.TsmMainMenu;
import com.timesontransfar.common.authorization.model.TsmOrganization;
import com.timesontransfar.common.authorization.model.TsmPopupMenu;
import com.timesontransfar.common.authorization.model.TsmRole;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.model.service.AuthRowMapper;
import com.timesontransfar.common.authorization.service.IAuthorizationDAO;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AuthorizationDAOImpl implements IAuthorizationDAO {
	
	private JdbcTemplate jdbcTemplate;

	private String staffSql;

	private String orgSql;

	private String childOrgSql;

	private String roleSql;

	private String parentRoleSql;

	private String childRoleSql;

	private String roleDataPermitSql;

	private String menuSql;

	private String parentMenuSql;

	private String childMenuSql;

	
	public AuthorizationDAOImpl() {
		super();
	}


	public TsmRole getRole(String roleId) {
		/*
		 * SELECT * FROM TSM_ROLE WHERE ROLE_ID=?
		 */
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(3);
		List roleList = (List)this.jdbcTemplate
				.query(this.roleSql, new Object[] { roleId },
						new RowMapperResultSetExtractor(rowMapperIn));
		TsmRole role = (TsmRole) roleList.get(0);
		/*
		 * SELECT B.PARENT_ROLE_ID FROM TSM_ROLE A,TSM_ROLE_ROLE_RELA B
		 * WHERE A.ROLE_ID=B.ROLE_ID AND A.ROLE_ID=?
		 */
		List queryList = this.jdbcTemplate.queryForList(this.parentRoleSql,
				roleId);
		List parentList = role.getParentRoles();
		if (parentList == null) {
			parentList = new ArrayList();
		}
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			String parentId = (String) queryMap.get("PARENT_ROLE_ID");
			parentList.add(parentId);
		}
		role.setParentRoles(parentList);
		/*
		 * SELECT B.ROLE_ID FROM TSM_ROLE A,TSM_ROLE_ROLE_RELA B WHERE
		 * A.ROLE_ID=B.ROLE_ID AND B.PARENT_ROLE_ID=?
		 */
		queryList = this.jdbcTemplate.queryForList(this.childRoleSql,
				roleId);
		List childList = role.getChildRoles();
		if (childList == null) {
			childList = new ArrayList();
		}
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			String childId = (String) queryMap.get("ROLE_ID");
			childList.add(childId);
		}
		role.setChildRoles(childList);
		/*
		 * SELECT B.FUNC_OPERATE_ID,B.ISPRIVATE FROM TSM_ROLE
		 * A,TSM_ROLE_FUNCOPR_RELA B WHERE A.ROLE_ID=B.ROLE_ID AND A.ROLE_ID=?
		 */
		//wanrongwei
		String newRoleFuncPermitSql = "SELECT * FROM TSM_ROLE_FUNC_RELA A WHERE A.ROLE_ID=?";
		queryList = this.jdbcTemplate.queryForList(newRoleFuncPermitSql,
				roleId);
		Map roleFuncMap = role.getFunctionPermit();
		if (roleFuncMap == null) {
			roleFuncMap = new TreeMap();
			role.setFunctionPermit(roleFuncMap);
		}
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			String funcId = (String) queryMap.get("FUNC_ID");
			BigDecimal privateNum = new BigDecimal("0");
			role.addFuncPermit(funcId, privateNum.intValue());
		}
		/*
		 * SELECT B.DATAOPERID,B.ISPRIVATE FROM TSM_ROLE
		 * A,TSM_ROLE_DATAPERMIT_RELA B WHERE A.ROLE_ID=B.ROLE_ID AND
		 * A.ROLE_ID=?
		 */
		queryList = this.jdbcTemplate.queryForList(this.roleDataPermitSql,
				roleId);
		Map roleDataMap = role.getDataPermit();
		if (roleDataMap == null) {
			roleDataMap = new TreeMap();
			role.setDataPermit(roleDataMap);
		}
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			String funcId = (String) queryMap.get("DATAOPERID");
			BigDecimal privateNum = (BigDecimal) queryMap.get("ISPRIVATE");
			role.addDataPermit(funcId, privateNum.intValue());
		}
		rowMapperIn = null;
		return role;
	}

	public TsmMainMenu getMainMenu(String menuId) {
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(4);
		List menuList = (List)this.jdbcTemplate
				.query(this.menuSql, new Object[] { menuId },
						new RowMapperResultSetExtractor(rowMapperIn));
		if (menuList != null && !menuList.isEmpty()) {
			TsmMainMenu menu = (TsmMainMenu) menuList.get(0);
			List queryList = this.jdbcTemplate.queryForList(this.parentMenuSql,
					menuId);
			List parentList = menu.getParentList();
			if (parentList == null) {
				parentList = new ArrayList();
			}
			for (int i = 0; i < queryList.size(); i++) {
				Map queryMap = (Map) queryList.get(i);
				String parentMenuId = (String) queryMap.get("TOP_MENU_ID");
				parentList.add(parentMenuId);
			}
			menu.setParentList(parentList);
			queryList = this.jdbcTemplate.queryForList(this.childMenuSql,
					menuId);
			List childList = menu.getChildList();
			if (childList == null) {
				childList = new ArrayList();
			}
			for (int i = 0; i < queryList.size(); i++) {
				Map queryMap = (Map) queryList.get(i);
				String childMenuId = (String) queryMap.get("MENU_ID");
				childList.add(childMenuId);
			}
			menu.setChildList(childList);
			rowMapperIn = null;
			return menu;
		} else {
			return null;
		}
	}

	public TsmFunction getFunction(String functionId) {
		TsmFunction function = new TsmFunction();
		function.setFuncName("");
		function.setId(functionId);
		function.setFuncDesc("");
		function.setOperaType(1);
		String newFuncMenuSql = "SELECT * FROM TSM_MAINMENU B WHERE B.MENU_ID=? AND B.SYSTEM_ID=1";
		List queryList = this.jdbcTemplate.queryForList(newFuncMenuSql,
				functionId);
		if (!queryList.isEmpty()) { // 2006-8-25
			Map queryMap = (Map) queryList.get(0);
			String id = (String) queryMap.get("MENU_ID");
			function.setConstraint("MENU__"+id);
		}
		return function;
	}

	/**
	 * 取得员工的拥有的角色
	 *
	 * @param staffId
	 * @return
	 */
	public List getRolesHoldInStaff(String staffId) {
		String staffRoleSql = "SELECT A.* FROM TSM_ROLE A,TSM_STAFF_ROLE_RELA B WHERE A.ROLE_ID=B.ROLE_ID AND B.STAFF_ID = ?";
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(3);
		List rolesList = (List)this.jdbcTemplate.query(staffRoleSql, new Object[] { staffId },
				new RowMapperResultSetExtractor(rowMapperIn));
		rowMapperIn = null;
		return rolesList;
	}

	
	public TsmStaff getStaff(String staffId) {
		/*
		 * SELECT A.*,B.ORG_NAME FROM TSM_STAFF A,TSM_ORGANIZATION B WHERE
		 * A.ORG_ID=B.ORG_ID AND A.STAFF_ID=?
		 */
		List staffList = null;
		try {
			AuthRowMapper rowMapperIn = new AuthRowMapper();
			rowMapperIn.setIntClassType(1);
			staffList = (List)this.jdbcTemplate.query(this.staffSql,
					new Object[] { staffId }, new RowMapperResultSetExtractor(
							rowMapperIn));
			rowMapperIn = null;
		} catch (Exception e) {
			return null;
		}

		String staffRoleSql = " SELECT A.* FROM TSM_ROLE A,TSM_STAFF_ROLE_RELA B "
				+ " WHERE A.ROLE_ID=B.ROLE_ID AND B.STAFF_ID= '" + staffId + "'";
		List queryList = null;
		try {
			AuthRowMapper rowMapperIn = new AuthRowMapper();
			rowMapperIn.setIntClassType(3);
			queryList = (List)this.jdbcTemplate.query(staffRoleSql,//CodeSec未验证的SQL注入；CodeSec误报：1
					new RowMapperResultSetExtractor(rowMapperIn));
			rowMapperIn = null;
		} catch (Exception e) {
			return null;
		}
		List roleList = new ArrayList();
		if(queryList != null) {
			for (int i = 0; i < queryList.size(); i++) {
				TsmRole role = (TsmRole) queryList.get(i);
	
				Map roleMap = new TreeMap();
				roleMap.put("ID", role.getId());
				roleMap.put("NAME", role.getName());
				roleList.add(roleMap);
			}
		}
		TsmStaff tsmStaff = null;
		if(staffList != null && !staffList.isEmpty()) {
			tsmStaff = (TsmStaff) staffList.get(0);
			tsmStaff.setRoleList(roleList);
		}
		return tsmStaff;
	}

	public TsmStaff getStaffByLoginName(String loginName) {
		String querySql = " SELECT A.STAFF_ID FROM TSM_STAFF A WHERE A.LOGONNAME = ? AND A.STATE=8 ";
		List staffList = this.jdbcTemplate.queryForList(querySql,
				loginName);

		TsmStaff staff = null;
		if (!staffList.isEmpty()) {
			Map temp = (Map) staffList.get(0);
			String staffId = String.valueOf(temp.get("STAFF_ID"));
			staff = this.getStaff(staffId);
		}
		return staff;
	}

	public TsmOrganization getOrganization(String orgId) {
		/*
		 * SELECT * FROM TSM_ORGANIZATION WHERE ORG_ID=?
		 */
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(23);
		List orgList = (List)this.jdbcTemplate.query(this.orgSql,
				new Object[] { orgId.trim() }, new RowMapperResultSetExtractor(
						rowMapperIn));
		TsmOrganization org = null;
		if(orgList != null && !orgList.isEmpty()) {
			org = (TsmOrganization) orgList.get(0);
			org.setChildOrgList(this.getChildOrgIdList(orgId));
		}
		return org;
	}

	private List getChildOrgIdList(String orgId) {
		/*
		 * SELECT ORG_ID FROM TSM_ORGANIZATION WHERE UP_ORG=?
		 */
		List queryList = this.jdbcTemplate.queryForList(this.childOrgSql,
				 orgId);
		List childList = new ArrayList();
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(0);
			String childOrgId = queryMap.get("ORG_ID").toString();
			childList.add(childOrgId);
		}
		return childList;
	}

	public List getChildOrgList(String orgId) {
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(2);
		List retList = (List)this.jdbcTemplate.query(this.childOrgSql,
				new Object[] { orgId }, new RowMapperResultSetExtractor(rowMapperIn));
		rowMapperIn = null;
		return retList;
	}

	/**
	 * 根据Control ID查找控件的实例（包括URL)
	 * @param id
	 * @return
	 */
	public TsmControl getControl(String id) {
		String getControlSql = "SELECT * FROM TSM_CONTROLURL K WHERE K.CONTROL_ID=?";
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(12);
		List retList = (List)this.jdbcTemplate.query(getControlSql,
				new Object[] { id }, new RowMapperResultSetExtractor(rowMapperIn));
		if (retList != null && !retList.isEmpty()) {
			return (TsmControl) retList.get(0);
		} else {
			return null;
		}
	}

	/**
	 * 根据menu ID查找弹出菜单的实例
	 * @param id
	 * @return
	 */
	public TsmPopupMenu getPopupMenu(String id) {
		String getPopupMenuSql = "SELECT * FROM TSM_POPUP_MENU X WHERE X.MENU_ID=?";
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(17);
		List retList = (List)this.jdbcTemplate.query(getPopupMenuSql,
				new Object[] { id }, new RowMapperResultSetExtractor(rowMapperIn));
		if (retList != null && !retList.isEmpty()) {
			return (TsmPopupMenu) retList.get(0);
		} else {
			return null;
		}
	}

	public List getStaffPermitRoles(String staffId){
		String staffPermitRoleSql = "select * from tsm_role r where r.org_id in"
				+ "(SELECT ORG_ID FROM TSM_ORGANIZATION WHERE ORG_ID IN "
				+ "(SELECT DISTINCT C.COND_VALUE FROM TSM_STAFF_ROLE_RELA A, TSM_ROLE B, TSM_CONDITION C "
				+ "WHERE A.ROLE_ID = B.ROLE_ID AND B.ROLE_ID = C.ROLE_ID AND C.OBJ_ID = '888888' "
				+ "AND A.STAFF_ID = ?))";
		AuthRowMapper rowMapperIn = new AuthRowMapper();
		rowMapperIn.setIntClassType(3);
		List rolesList = (List) this.jdbcTemplate.query(staffPermitRoleSql,new Object[]{staffId},
				new RowMapperResultSetExtractor(rowMapperIn));
		rowMapperIn = null;
		return rolesList;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public String getStaffSql() {
		return staffSql;
	}

	public void setStaffSql(String staffSql) {
		this.staffSql = staffSql;
	}

	public String getOrgSql() {
		return orgSql;
	}

	public void setOrgSql(String orgSql) {
		this.orgSql = orgSql;
	}

	public String getChildOrgSql() {
		return childOrgSql;
	}

	public void setChildOrgSql(String childOrgSql) {
		this.childOrgSql = childOrgSql;
	}

	public String getRoleSql() {
		return roleSql;
	}

	public void setRoleSql(String roleSql) {
		this.roleSql = roleSql;
	}

	public String getParentRoleSql() {
		return parentRoleSql;
	}

	public void setParentRoleSql(String parentRoleSql) {
		this.parentRoleSql = parentRoleSql;
	}

	public String getChildRoleSql() {
		return childRoleSql;
	}

	public void setChildRoleSql(String childRoleSql) {
		this.childRoleSql = childRoleSql;
	}

	public String getRoleDataPermitSql() {
		return roleDataPermitSql;
	}

	public void setRoleDataPermitSql(String roleDataPermitSql) {
		this.roleDataPermitSql = roleDataPermitSql;
	}

	public String getMenuSql() {
		return menuSql;
	}

	public void setMenuSql(String menuSql) {
		this.menuSql = menuSql;
	}

	public String getParentMenuSql() {
		return parentMenuSql;
	}

	public void setParentMenuSql(String parentMenuSql) {
		this.parentMenuSql = parentMenuSql;
	}

	public String getChildMenuSql() {
		return childMenuSql;
	}

	public void setChildMenuSql(String childMenuSql) {
		this.childMenuSql = childMenuSql;
	}

}