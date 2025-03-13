package com.timesontransfar.common.authorization.role.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.authorization.model.TsmCondition;
import com.timesontransfar.common.authorization.model.TsmEntityPermit;
import com.timesontransfar.common.authorization.model.TsmFunction;
import com.timesontransfar.common.authorization.model.TsmRole;
import com.timesontransfar.common.authorization.role.IRoleDAO;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.common.authorization.web.webobject.WebMenuPriPermit;
import com.timesontransfar.common.database.KeyGenerator;

@SuppressWarnings("rawtypes")
public class RoleDAOImpl implements IRoleDAO {
	protected final Log logger = LogFactory.getLog(getClass());

	private JdbcTemplate jdbcTemplate = null;

	private KeyGenerator keyGen = null;

	/* 修改角色信息 */
	private String sqlUpdateRole = null;

	/* 配置数据权限属性 */
	private String sqlInsertCdt = null;

	private String sqlInsertDataRela = null;

	private String sqlDeleteCdt = null;

	private String sqlDeleteDataRela = null;

	/* 功能权限配置部分 */
	private String sqlInsertFuncRela = null;

	private String sqlDeleteFuncRela = null;

	// 得到该组织机构的LinkId
	private String getOrgLinkIdByOrgIdSql;
	
	private ISystemAuthorization systemAuthorization;



	/**
	 * 得到该组织机构的LinkId
	 *
	 * @param orgId
	 *            组织机构编号
	 * @return 组织机构对应的LinkId关系 add by qliang 2006-08-02
	 */
	public String getOrgLinkIdByOrgId(String orgId) {
		if (orgId == null) {
			return "";
		}
		String linkId = "";
		List resultStr = this.jdbcTemplate.queryForList(this.getOrgLinkIdByOrgIdSql, orgId );
		Iterator iterator = resultStr.iterator();
		if (iterator.hasNext()) {
			Map item = (Map) iterator.next();
			if(item.get("LINKID")!=null){
				linkId = item.get("LINKID").toString();
			}else{
				linkId = "";
			}
		}
		return linkId;
	}

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
	public boolean modifyRoleInfo(String staffId, String roleId, TsmRole role) {
		/**
		 * UPDATE TSM_ROLE_ROLE_RELA SET PARENT_ROLE_ID=? WHERE ROLE_ID=?
		 */
		/**
		 * UPDATE TSM_ROLE SET
		 * ORG_ID=?,ROLE_NAME=?,ROLE_TYPE=?,STATE=?,ROLE_ORG=?,
		 * MODIFY_DATE=?,MODIFY_STAFF=?,EFFECT_DATE=?,EXPIRE_DATE=? WHERE
		 * ROLE_ID=?
		 */
		int rowCount = this.jdbcTemplate.update(this.sqlUpdateRole,
				role.getOrgId(), role.getName(),
						role.getType(),
						role.isState() ? 1 : 0,
						"".equals(role.getRoleOrg())? "1":role.getRoleOrg(), new Date(), staffId,
						role.getStartDate(), role.getEndDate(),
						role.getRoleReportGrade(), role.getRoleRuleType(),role.getBaseFlag(),
						roleId );
		boolean blnResult = rowCount > 0;
		/* 如果角色具有父亲角色(要考虑事务的问题) */
		if (blnResult && !(role.getParentRoles()).isEmpty()) {
			int delCount = this.jdbcTemplate.update(
					"delete from tsm_role_role_rela where ROLE_ID=?",
					 roleId);
			blnResult = delCount >= 0;
			
			String pRoleId = ((TsmRole) role.getParentRoles().get(0)).getId();
			if (!pRoleId.equals("")) {
				int uCount = this.jdbcTemplate
						.update(
								"insert into tsm_role_role_rela(role_id,parent_role_id) values(?,?)",
								roleId, pRoleId);
				blnResult = uCount > 0;
			}
		}
		return blnResult;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 根据组织机构编号取组织机构名称
	 */
	public String getOrgNameById(String id) {
		String sqlStr = "SELECT ORG_NAME FROM TSM_ORGANIZATION WHERE ORG_ID =  ? ";
		List resultStr = this.jdbcTemplate.queryForList(sqlStr, id);
		String orgName = "";
		Iterator iterator = resultStr.iterator();
		if (iterator.hasNext()) {
			Map item = (Map) iterator.next();
			if(item.get("ORG_NAME")!=null){
				orgName = item.get("ORG_NAME").toString();
			}else{
				orgName = "";
			}
		}

		return orgName;

	}

	public boolean configDataPermit(String roleId, List lstData) {
		/**
		 * DELETE FROM TSM_ROLE_DATAPERMIT_RELA WHERE ROLE_ID=?
		 */
		/**
		 * INSERT INTO
		 * TSM_ROLE_DATAPERMIT_RELA(DATAPERMIT_RELA_ID,ROLE_ID,ISPRIVATE,DATAOPERID,ORG_STATE)
		 * VALUES(?,?,?,?,?)
		 */

		/* 配置数据权限时先删除上次配置的记录 */
		this.jdbcTemplate.update(this.sqlDeleteDataRela,
				 roleId );

		/* 新增本次记录 */
		boolean flag = true;
		for (int i = 0; i < lstData.size(); i++) {
			TsmEntityPermit entity = (TsmEntityPermit) lstData.get(i);
			String id = this.keyGen.generateKey("TSM_ROLE_DATAPERMIT_RELA");
			int count = this.jdbcTemplate
					.update(this.sqlInsertDataRela,
									id,
									roleId,
									entity.isPrivate() ? 1 : 0,
									entity.getId(),
									entity.getOrgState());
			flag = count > 0;
		}
		return flag;
	}

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
	public boolean configPermitCondition(String roleId, List lstCdt) {
		/**
		 * DELETE FROM TSM_CONDITION WHERE ROLE_ID=?
		 */
		/**
		 * INSERT INTO TSM_CONDITION(REST_COND_ID,ROLE_ID,OBJ_ID,ISCOMMON,
		 * DATAROLEID,ATTRIBUTE_ID,OPERATE_ID,VALUE,RELATION,CONDLEVEL)
		 * VALUES(?,?,?,?,?,?,?,?,?,?)
		 */
		/* 先删除以前的配置权限因子 */
		this.jdbcTemplate.update(this.sqlDeleteCdt, roleId);

		boolean flag = true;
		for (int i = 0; i < lstCdt.size(); i++) {
			TsmCondition cdt = (TsmCondition) lstCdt.get(i);
			String id = this.keyGen.generateKey("TSM_CONDITION");
			int count = this.jdbcTemplate.update(this.sqlInsertCdt,
					 		id, roleId, cdt.getObjId(),
							cdt.isCommonFlag() ? 1 : 0,
							cdt.getDataRoleId(), cdt.getAttributeId(),
							cdt.getOperateType(),
							cdt.getMatchValue(), cdt.getJoinType(),
							cdt.getLinkSeq());
			flag = count > 0;
		}
		return flag;
	}

	/**
	 * 配置角色的功能权限
	 *
	 * @param roleId
	 *            角色Id
	 * @param lstFuncs
	 *            功能权限
	 */
	public boolean configFuncPermit(String roleId, List lstFuncs) {
		/**
		 * DELETE FROM TSM_ROLE_FUNCOPR_RELA WHERE ROLE_ID=?
		 */
		/**
		 * INSERT INTO TSM_ROLE_FUNCOPR_RELA(FUNC_OPERATE_ID,ROLE_ID,ISPRIVATE)
		 * VALUES(?,?,?)
		 */
		// 先删除以前的功能权限
		this.jdbcTemplate.update(this.sqlDeleteFuncRela,
				 roleId );

		boolean flag = true;
		for (int i = 0; i < lstFuncs.size(); i++) {
			TsmFunction func = (TsmFunction) lstFuncs.get(i);
			int count = this.jdbcTemplate.update(this.sqlInsertFuncRela,
							func.getId(),
							roleId,
							func.isIsprivate() ? 1 : 0);
			flag = count > 0;
		}
		return flag;
	}

	public boolean savePopupPermitInfo(String roleId, List list) {
		int j = -1;
		int k = -1;
		if (list != null) {
			int size = list.size();
			for (int i = 0; i < size; i++) {
				String popFunSql = "SELECT R.FUNC_OPERATE_ID FROM TSM_FUNC_POPUPMENU_RELA R WHERE R.MENU_ID = ? ";
				String roleRela = "INSERT INTO TSM_ROLE_FUNCOPR_RELA VALUES(?,?,'0')";
				String urlFunSql = "SELECT C.FUNC_OPERATE_ID FROM TSM_CTRL_OPER_RELA C   WHERE C.CONTROL_ID = ?";
				List popFunId = this.jdbcTemplate.queryForList(popFunSql,
						 list.get(i).toString());
				List urlFunId = this.jdbcTemplate.queryForList(urlFunSql,
						list.get(i).toString());
				String funOperId = " ";
				if (!popFunId.isEmpty()) {
					Map funOper = (Map) popFunId.get(0);
					funOperId = funOper.get("FUNC_OPERATE_ID").toString();
				} else {
					Map urlOper = (Map) urlFunId.get(0);
					funOperId = urlOper.get("FUNC_OPERATE_ID").toString();
				}

				this.jdbcTemplate.update(roleRela,  funOperId,
						roleId );
			}
		}
		return j > 0 && k > 0;
	}

	/**
	 * 保存数据为私有
	 *
	 */
	public boolean savePrivate(List list) {
		int size = list.size();
		int k = -1;
		for (int i = 0; i < size; i++) {
			WebMenuPriPermit webMp = (WebMenuPriPermit) list.get(i);
			if (webMp.getIsPrivate().equals("1")) {
				String sql = "UPDATE TSM_ROLE_FUNCOPR_RELA O "
						+ "SET O.ISPRIVATE = 1 WHERE O.FUNC_OPERATE_ID "
						+ "IN (SELECT FUNC_OPERATE_ID "
						+ "FROM TSM_MENU_OPER_RELA WHERE MENU_ID=?)";
				k = this.jdbcTemplate.update(sql,
						webMp.getId());
			}
		}
		return k > 0;
	}

	public String getSqlUpdateRole() {
		return sqlUpdateRole;
	}

	public void setSqlUpdateRole(String sqlUpdateRole) {
		this.sqlUpdateRole = sqlUpdateRole;
	}

	public KeyGenerator getKeyGen() {
		return keyGen;
	}

	public void setKeyGen(KeyGenerator keyGen) {
		this.keyGen = keyGen;
	}

	public String getSqlDeleteCdt() {
		return sqlDeleteCdt;
	}

	public void setSqlDeleteCdt(String sqlDeleteCdt) {
		this.sqlDeleteCdt = sqlDeleteCdt;
	}

	public String getSqlDeleteDataRela() {
		return sqlDeleteDataRela;
	}

	public void setSqlDeleteDataRela(String sqlDeleteDataRela) {
		this.sqlDeleteDataRela = sqlDeleteDataRela;
	}

	public String getSqlDeleteFuncRela() {
		return sqlDeleteFuncRela;
	}

	public void setSqlDeleteFuncRela(String sqlDeleteFuncRela) {
		this.sqlDeleteFuncRela = sqlDeleteFuncRela;
	}

	public String getSqlInsertCdt() {
		return sqlInsertCdt;
	}

	public void setSqlInsertCdt(String sqlInsertCdt) {
		this.sqlInsertCdt = sqlInsertCdt;
	}

	public String getSqlInsertDataRela() {
		return sqlInsertDataRela;
	}

	public void setSqlInsertDataRela(String sqlInsertDataRela) {
		this.sqlInsertDataRela = sqlInsertDataRela;
	}

	public String getSqlInsertFuncRela() {
		return sqlInsertFuncRela;
	}

	public void setSqlInsertFuncRela(String sqlInsertFuncRela) {
		this.sqlInsertFuncRela = sqlInsertFuncRela;
	}

	public String getGetOrgLinkIdByOrgIdSql() {
		return getOrgLinkIdByOrgIdSql;
	}

	public void setGetOrgLinkIdByOrgIdSql(String getOrgLinkIdByOrgIdSql) {
		this.getOrgLinkIdByOrgIdSql = getOrgLinkIdByOrgIdSql;
	}

	public ISystemAuthorization getSystemAuthorization() {
		return systemAuthorization;
	}

	public void setSystemAuthorization(ISystemAuthorization systemAuthorization) {
		this.systemAuthorization = systemAuthorization;
	}
}
