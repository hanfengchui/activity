package com.timesontransfar.common.authorization.role;

import java.util.List;

import com.timesontransfar.common.authorization.model.TsmRole;
@SuppressWarnings("rawtypes")
public interface IRoleDAO {

	/**
	 * 得到该组织机构的LinkId
	 * @param orgId 组织机构编号
	 * @return  组织机构对应的LinkId关系
	 * add by qliang 2006-08-02
	 */
	public String getOrgLinkIdByOrgId(String orgId);

	/**
	 * 配置角色的数据权限
	 *
	 * @param roleId
	 * @param lstData
	 * @return
	 */
	public boolean configDataPermit(String roleId, List lstData);

	/**
	 * 配置角色有权限操作实体的权限因子
	 *
	 * @param roleId
	 *            角色ID
	 * @param objId
	 *            实体ID
	 * @param lstCdt
	 *            权限因子
	 * @return
	 */
	public boolean configPermitCondition(String roleId, List lstCdt);

	/**
	 * 配置角色的功能权限
	 *
	 * @param roleId
	 * @param lstFuncs
	 * @return
	 */
	public boolean configFuncPermit(String roleId, List lstFuncs);

	/**
	 * 修改角色信息
	 *
	 * @param staffId
	 *            操作员工Id
	 * @param roleId
	 *            角色Id
	 * @param role
	 *            新的角色信息
	 * @return
	 */
	public boolean modifyRoleInfo(String staffId, String roleId, TsmRole role);

	/**
	 * 根据组织编号取组织机构名称
	 */
	public String getOrgNameById(String id);

	/**
	 * 保存右键权限信息
	 *
	 * @param list
	 * @return
	 */
	public boolean savePopupPermitInfo(String roleId,List list);

	/**
	 * 保存功能权限是否为私有
	 */
	public boolean savePrivate(List list);

}
