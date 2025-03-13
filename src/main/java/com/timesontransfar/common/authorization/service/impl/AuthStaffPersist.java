package com.timesontransfar.common.authorization.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IAuthPersist;
import com.timesontransfar.common.authorization.service.IAuthorizationDAO;
import com.timesontransfar.common.database.KeyGenerator;
import com.timesontransfar.customservice.common.PubFunc;

public class AuthStaffPersist implements IAuthPersist {
	
	private JdbcTemplate jdbcTemplate; // 本地数据源

	private KeyGenerator keyGenerator;
	
	private IAuthorizationDAO authorizationDAO;
	
	@Autowired
	private PubFunc pubFunc;

	private String insertStaffSql;

	private String updateStaffSql;

	private String deleteStaffSql;

	private String deleteStaffRoleRelaSql;

	
	public AuthStaffPersist() {
		super();
	}

	public String save(Object object) throws DataAccessException {
		TsmStaff staff = (TsmStaff) object;
		int ret = this.saveStaff(staff);
		if (ret < 0)
			return String.valueOf(ret);
		return staff.getId();
	}

	public String delete(Object object) throws DataAccessException {
		TsmStaff staff = (TsmStaff) object;
		// 删除角色联系
		this.jdbcTemplate.update(this.deleteStaffRoleRelaSql, staff.getId());
		/*
		 * 删除员工SQL: DELETE FROM TSM_STAFF WHERE STAFF_ID=?
		 */
		this.jdbcTemplate.update(this.deleteStaffSql, staff.getId());
		return staff.getId();
	}

	private int saveStaff(TsmStaff staff) throws DataAccessException {
		String logonStaff = getCurrStaffId();// 取得当前登陆用户的staffid
		if (logonStaff.equals("")) {
			return -1; // 表示未取到当前登录人id
		}
		String querySql0 = "SELECT COUNT(*) FROM TSM_ORGANIZATION WHERE ORG_ID = ? AND STATE = (SELECT REFER_ID FROM PUB_COLUMN_REFERENCE WHERE TABLE_CODE='TSM_ORGANIZATION' AND COL_CODE='STATE' AND COL_VALUE='3')";
		//int countorg = this.jdbcTemplate.queryForInt(querySql0, new Object[] { staff.getOrganizationId() });
		int countorg = this.jdbcTemplate.queryForObject(querySql0,new java.lang.Object[]{staff.getOrganizationId()},Integer.class);
		if (countorg > 0) {
			return -7;// 该组织机构已经失效，不能维护员工！
		}
		int affectRow = 0;
		if (staff.getId().trim().length() > 0) {
			// 有效员工内是否存在同工号的
			String querySql = "SELECT COUNT(*) FROM TSM_STAFF WHERE STAFF_ID <> ? AND LOGONNAME = ? AND STATE in(8,7) ";
		//	int countTemp = this.jdbcTemplate.queryForInt(querySql,	new Object[] {staff.getId(), staff.getLogonName()});
			int countTemp = this.jdbcTemplate.queryForObject(querySql,new java.lang.Object[]{ staff.getId(), staff.getLogonName() },Integer.class);
			
			if (countTemp > 1) {
				return -5; // 登录名已存在！
			}
			staff.setModifyStaff(logonStaff);
			// 更新员工信息
			affectRow += this.updateStaff(staff);
		} else {
			// 判断该员工登录名和工号已经存在
			String logonNameTemp = staff.getLogonName();
			String querySql1 = "SELECT COUNT(*) FROM TSM_STAFF WHERE LOGONNAME = ?";
		//	int countTemp = this.jdbcTemplate.queryForInt(querySql1, new Object[] {logonNameTemp});

			int countTemp=this.jdbcTemplate.queryForObject(querySql1,new java.lang.Object[]{ logonNameTemp },Integer.class);
			if (countTemp > 0) {
				return -5;// 登录名已存在！
			}
			staff.setId(this.getNewStaffId());
			affectRow += this.insertStaff(staff, logonStaff);
		}
		return affectRow;
	}

	/***************************************************************************
	 * 保存员工修改信息
	 *
	 * @param staff
	 * @return
	 * @throws DataAccessException
	 *
	 * @modify 修改记录： 20060718 liangli 新增3个字段 EXP_DATE,IS_SSO,LOGIN_TIMES liangyong adds
	 * StaffStartDate,StaffEndDate and UnlockPassword on 2006-9-12 qliang
	 * 修改成员工修伽的过程中不能修改员工的密码
	 * 2014-12-27 LiJiahui 更改updateStaffSql，删除不符合江苏客服工单系统需求的代码
	 */
	private int updateStaff(TsmStaff staff) throws DataAccessException {
		Integer staffId = Integer.valueOf(staff.getId());
		int effect = this.jdbcTemplate.update(this.updateStaffSql, 
				StringUtils.defaultIfEmpty(staff.getName(),null),
				StringUtils.defaultIfEmpty(staff.getOrganizationId(),null),
				StringUtils.defaultIfEmpty(staff.getLogonName(),null),
				StringUtils.defaultIfEmpty(staff.getPassword(),null),
				StringUtils.defaultIfEmpty(staff.getRelaPhone(),null),
				StringUtils.defaultIfEmpty(staff.getRelaMail(),null),
				staff.getGender(),
				staff.getLevel(),
				StringUtils.defaultIfEmpty(staff.getModifyStaff(),null),
				staffId);
		return effect;
	}

	/***************************************************************************
	 * 新增用户
	 *
	 * @param staff
	 * @return
	 * @throws DataAccessException
	 *
	 * 修改记录： 20060718 liangli 新增3个字段 EXP_DATE,IS_SSO,LOGIN_TIMES liangyong adds
	 * StaffStartDate,StaffEndDate and UnlockPassword on 2006-9-12
	 */
	private int insertStaff(TsmStaff staff, String currentStaff)
			throws DataAccessException {
		/*
		 * 执行Insert操作： 
		 * INSERT INTO TSM_STAFF(STAFF_ID, CSS_STYLE, GENDER, STAFF_LEVEL, LOGONNAME, STAFFNAME, ORG_ID, 
		 * 						 RELAEMAIL, RELAPHONE, STATE, DUTY, PASSWORD, CRE_DATE, CRE_STAFF)
				VALUES(?,?,?, ?,?,?, ?,?,?, ?,?,?, sysdate,?)
		 */
		int effect = this.jdbcTemplate.update(this.insertStaffSql,
				StringUtils.defaultIfEmpty(staff.getId(),null),
				staff.getGender(),
				staff.getLevel(),
				StringUtils.defaultIfEmpty(staff.getLogonName(),null),
				StringUtils.defaultIfEmpty(staff.getName(),null),
				StringUtils.defaultIfEmpty(staff.getOrganizationId(),null),
				StringUtils.defaultIfEmpty(staff.getRelaMail(),null),
				StringUtils.defaultIfEmpty(staff.getRelaPhone(),null),
				staff.getState(),
				StringUtils.defaultIfEmpty(staff.getPassword(),null),
				currentStaff);
		return effect;
	}

	private String getNewStaffId() {
		String retStaffId = "";
		String idSql = "select seq_staff_id.nextval from dual";
		retStaffId = this.jdbcTemplate.queryForObject(idSql, String.class);
		return retStaffId;
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

	public String getDeleteStaffRoleRelaSql() {
		return deleteStaffRoleRelaSql;
	}

	public void setDeleteStaffRoleRelaSql(String deleteStaffRoleRelaSql) {
		this.deleteStaffRoleRelaSql = deleteStaffRoleRelaSql;
	}

	public String getDeleteStaffSql() {
		return deleteStaffSql;
	}

	public void setDeleteStaffSql(String deleteStaffSql) {
		this.deleteStaffSql = deleteStaffSql;
	}

	public String getInsertStaffSql() {
		return insertStaffSql;
	}

	public void setInsertStaffSql(String insertStaffSql) {
		this.insertStaffSql = insertStaffSql;
	}

	public String getUpdateStaffSql() {
		return updateStaffSql;
	}

	public void setUpdateStaffSql(String updateStaffSql) {
		this.updateStaffSql = updateStaffSql;
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
