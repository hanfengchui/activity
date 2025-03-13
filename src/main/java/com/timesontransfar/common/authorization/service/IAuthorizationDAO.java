package com.timesontransfar.common.authorization.service;

import java.util.List;

import com.timesontransfar.common.authorization.model.TsmControl;
import com.timesontransfar.common.authorization.model.TsmFunction;
import com.timesontransfar.common.authorization.model.TsmMainMenu;
import com.timesontransfar.common.authorization.model.TsmOrganization;
import com.timesontransfar.common.authorization.model.TsmPopupMenu;
import com.timesontransfar.common.authorization.model.TsmRole;
import com.timesontransfar.common.authorization.model.TsmStaff;

@SuppressWarnings("rawtypes")
public interface IAuthorizationDAO {

	/**
	 *
	 * @param staffId
	 * @return
	 */
	public TsmStaff getStaff(String staffId);

	/**
	 *
	 * @param orgId
	 * @return
	 */
	public TsmOrganization getOrganization(String orgId);

	/**
	 *
	 * @param roleId
	 * @return
	 */
	public TsmRole getRole(String roleId);

	/**
	 *
	 * @param menuId
	 * @return
	 */
	public TsmMainMenu getMainMenu(String menuId);

	/**
	 *
	 * @param functionId
	 * @return
	 */
	public TsmFunction getFunction(String functionId);

	public TsmStaff getStaffByLoginName(String loginName);

	/**
	 *
	 * @param orgId
	 * @return
	 */
	public List getChildOrgList(String orgId);

	/**
	 * 取得员工的拥有的角色
	 *
	 * @param staffId
	 * @return
	 */
	public List getRolesHoldInStaff(String staffId);

	/**
	 * 根据Control ID查找控件的实例（包括URL)
	 * @param id
	 * @return
	 */
	public TsmControl getControl(String id);
	
	/**
	 * 根据menu ID查找弹出菜单的实例
	 * @param id
	 * @return
	 */
	public TsmPopupMenu getPopupMenu(String id);

	/**
	 * 根据员工ID，获取该员工有权限看到的所有的角色的列表。根据权限实体888888。
	 * @param staffId 员工的ID
	 * @return 角色对象列表
	 */
	public List getStaffPermitRoles(String staffId);
}
