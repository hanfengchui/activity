package com.timesontransfar.common.authorization.service.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.authorization.model.TsmOrganization;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IAuthPersist;
import com.timesontransfar.common.authorization.service.IAuthorizationDAO;
import com.timesontransfar.common.database.KeyGenerator;
import com.timesontransfar.customservice.common.PubFunc;

@SuppressWarnings("rawtypes")
public class AuthOrgPersist implements IAuthPersist {
	
	private JdbcTemplate jdbcTemplate; // 本地数据源

	private KeyGenerator keyGenerator;

	private IAuthorizationDAO authorizationDAO;
	
	@Autowired
	private PubFunc pubFunc;
	
	private String orgSql;

	private String updateOrgSql;

	private String insertOrgSql;

	private String deleteOrgSql;


	public AuthOrgPersist() {
		super();
	}

	private void updateLinkId(String orgId, String upLinkId) {
		List subList = authorizationDAO.getChildOrgList(orgId);
		if (!subList.isEmpty()) {
			int subCount = subList.size();
			for (int i = 0; i < subCount; i++) {
				String updateOrgId = ((TsmOrganization) subList.get(i)).getId();
				updateLinkId(updateOrgId, upLinkId + "__" + updateOrgId);
				updateOrgLinkIdByOrgId(updateOrgId, upLinkId + "__"
						+ updateOrgId, this.jdbcTemplate);
			}
		}
	}

	private int updateOrgLinkIdByOrgId(String orgId, String orgLinkId,
			JdbcTemplate jdbc) throws DataAccessException {
		return jdbc.update(
				"UPDATE TSM_ORGANIZATION SET LINKID=? WHERE ORG_ID=?",
				orgId, orgLinkId);
	}

	public String save(Object object) throws DataAccessException {
		// Auto-generated method stub
		/*
		 * 查询SQL: SELECT * FROM TSM_ORGANIZATION WHERE ORG_ID=?
		 */
		String tempStaffId = getCurrStaffId();// 取得当前登陆用户的staffid
		if (tempStaffId.equals("")) {
			return ""; // 表示未取到当前登录人id
		}

		TsmOrganization org = (TsmOrganization) object;

		if (org.getId().trim().length() > 0) {
			org.setAppId(org.getAppId() + "__" + org.getId());
			org.setModifyStaff(tempStaffId);
			org.setModifyDate(new Date());
			TsmOrganization oldOrg = authorizationDAO.getOrganization(org
					.getId()); // 2006-8-25

			// CRM、SPS数据库皆保存 2006-07-27
			this.update(org, this.jdbcTemplate);

			// 需要判断是否换了上级组织机构ID
			if (!oldOrg.getParentId().equals(org.getParentId())) {
				// 更新其下方的组织机构LINKID
				updateLinkId(org.getId(), org.getAppId());
			}
		} else {
			String id = this.keyGenerator.generateKey("GUID");
			org.setAppId(org.getAppId() + "__" + id);
			org.setId(id);
			org.setCreateStaff(tempStaffId);
			org.setCreateDate(new Date());
			org.setModifyDate(new Date());

			// CRM、SPS数据库皆保存 2006-07-27
			this.insert(org, this.jdbcTemplate);
		}
		return org.getId();
	}

	/**
	 * 保存组织机构信息
	 * 
	 * @param org
	 * @param jdbc
	 * @return
	 * @throws DataAccessException
	 * 
	 * 修改记录 : liangli 2006-07-27 新增入参jdbc：传数据源实例；修改删除方法也做同样的修改； 2006-8-23 梁勇修改
	 * 增加组织机构类别（ORGANIZATION_TYPE）时，本方法作相应修改，下一个方法insert修改亦是相同原因
	 */
	private int update(TsmOrganization org, JdbcTemplate jdbc)
			throws DataAccessException {
		/*
		 * UPDATE组织机构SQL:
		 * 
		 * this.updateOrgSql = " UPDATE TSM_ORGANIZATION SET
		 * ORG_ID=?,LINKID=?,ORG_NAME=?,"+ "
		 * PRINCIPAL=?,ORG_LEVEL=?,RELAPHONE=?,FUNCTIONTYPE=?,UP_ORG=?,
		 * ADDR_DESC=?,"+ "
		 * STATE=?,MODIFY_DATE=?,MODIFY_STAFF=?,AREACODE=?,REGION_ID=?
		 * ORGANIZATION_TYPE=?"+//CRE_DATE=?,CRE_STAFF=?, " WHERE ORG_ID=?";
		 */
		if (org.getCreateStaff() == null)
			org.setCreateStaff("");
		if (org.getModifyStaff() == null)
			org.setModifyStaff("");
		if (org.getAddrDesc() == null)
			org.setAddrDesc("");
		if (org.getAppId() == null)
			org.setAppId("");
		if (org.getRegionId() == null)
			org.setRegionId("");
		Integer tempIntState = 0;
		if (!org.getState().equals(""))
			tempIntState = new Integer(org.getState());
		return jdbc.update(this.updateOrgSql,
				// org.getId(),
				StringUtils.defaultIfEmpty(org.getAppId(),null),
				StringUtils.defaultIfEmpty(org.getName(),null),
				StringUtils.defaultIfEmpty(org.getPrincipal(),null),
				org.getLevel(),
				StringUtils.defaultIfEmpty(org.getRelaPhone(),null),
				org.getFuntionType(),
				StringUtils.defaultIfEmpty(org.getParentId(),null),
				StringUtils.defaultIfEmpty(org.getAddrDesc(),null),
				tempIntState,
				// org.getCreateDate(),
				// org.getCreateStaff(),
				org.getModifyDate(),
				StringUtils.defaultIfEmpty(org.getModifyStaff(),null),
				StringUtils.defaultIfEmpty(org.getAreaCode(),null),
				StringUtils.defaultIfEmpty(org.getRegionId(),null),
				org.getOrganizationType(),
				/*
				 * ################liangyong adds on 20061204
				 * #####################################
				 */
				StringUtils.defaultIfEmpty(org.getOrgOwner(),null),
				StringUtils.defaultIfEmpty(org.getOrgFax(),null),
				/*
				 * ##################################################################################
				 */
				StringUtils.defaultIfEmpty(org.getId(),null) );
	}

	private int insert(TsmOrganization org, JdbcTemplate jdbc)
			throws DataAccessException {
		/*
		 * INSERT 组织机构SQL: INSERT INTO
		 * TSM_ORGANIZATION(ORG_ID,LINKID,ORG_NAME,PRINCIPAL,ORG_LEVEL,
		 * RELAPHONE,FUNCTIONTYPE,UP_ORG,ADDR_DESC,STATE,CRE_DATE,CRE_STAFF,
		 * MODIFY_DATE,MODIFY_STAFF,AREACODE)
		 * VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
		 */
		/*
		 * ################liangyong deletes on 20061204
		 * #####################################
		 */
		/*
		 * this.insertOrgSql = "INSERT INTO
		 * TSM_ORGANIZATION(ORG_ID,LINKID,ORG_NAME,PRINCIPAL,"+ "
		 * ORG_LEVEL,RELAPHONE,FUNCTIONTYPE,UP_ORG,ADDR_DESC,STATE,CRE_DATE,"+ "
		 * CRE_STAFF,MODIFY_DATE,MODIFY_STAFF,AREACODE,REGION_ID,ORGANIZATION_TYPE)
		 * VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		 */
		/*
		 * ##################################################################################
		 */
		if (org.getCreateStaff() == null)
			org.setCreateStaff("");
		if (org.getModifyStaff() == null)
			org.setModifyStaff("");
		if (org.getAddrDesc() == null)
			org.setAddrDesc("");
		if (org.getAppId() == null)
			org.setAppId("");
		if (org.getRegionId() == null)
			org.setRegionId("");
		Integer tempIntState = 0;
		if (!org.getState().equals(""))
			tempIntState = new Integer(org.getState());
		return jdbc.update(this.insertOrgSql,
				StringUtils.defaultIfEmpty(org.getId(),null),
				StringUtils.defaultIfEmpty(org.getAppId(),null),
				StringUtils.defaultIfEmpty(org.getName(),null),
				StringUtils.defaultIfEmpty(org.getPrincipal(),null),
				org.getLevel(),
				StringUtils.defaultIfEmpty(org.getRelaPhone(),null),
				org.getFuntionType(),
				StringUtils.defaultIfEmpty(org.getParentId(),null),
				StringUtils.defaultIfEmpty(org.getAddrDesc(),null),
				tempIntState,
				org.getCreateDate(),
				StringUtils.defaultIfEmpty(org.getCreateStaff(),null),
				org.getModifyDate(),
				StringUtils.defaultIfEmpty(org.getModifyStaff(),null),
				StringUtils.defaultIfEmpty(org.getAreaCode(),null),
				StringUtils.defaultIfEmpty(org.getRegionId(),null),
				org.getOrganizationType(),
				/*
				 * ################liangyong adds on 20061204
				 * #####################################
				 */
				StringUtils.defaultIfEmpty(org.getOrgOwner(),null),
				StringUtils.defaultIfEmpty(org.getOrgFax(),null)
		/*
		 * ##################################################################################
		 */
		);
	}

	public String delete(Object object) throws DataAccessException {
		/*
		 * DELETE 组织机构SQL: DELETE FROM TSM_ORGANIZATION WHERE ORG_ID=?
		 */
		TsmOrganization org = (TsmOrganization) object;
		this.jdbcTemplate.update(this.deleteOrgSql,
				 org.getId());
		return org.getId();
	}

	/**
	 * 从Session中取得当前登录员工的ID
	 */
	private String getCurrStaffId() {
		String tempStaffId = "";
		TsmStaff tsmStaff = pubFunc.getLogonStaff();
		if(tsmStaff != null){
			tempStaffId = tsmStaff.getId();
		}
		return tempStaffId;
	}

	public String getDeleteOrgSql() {
		return deleteOrgSql;
	}

	public void setDeleteOrgSql(String deleteOrgSql) {
		this.deleteOrgSql = deleteOrgSql;
	}

	public String getUpdateOrgSql() {
		return updateOrgSql;
	}

	public void setUpdateOrgSql(String updateOrgSql) {
		this.updateOrgSql = updateOrgSql;
	}

	public String getInsertOrgSql() {
		return insertOrgSql;
	}

	public void setInsertOrgSql(String insertOrgSql) {
		this.insertOrgSql = insertOrgSql;
	}

	public String getOrgSql() {
		return orgSql;
	}

	public void setOrgSql(String orgSql) {
		this.orgSql = orgSql;
	}

	public KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	public IAuthorizationDAO getAuthorizationDAO() {
		return authorizationDAO;
	}

	public void setAuthorizationDAO(IAuthorizationDAO authorizationDAO) {
		this.authorizationDAO = authorizationDAO;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

}
