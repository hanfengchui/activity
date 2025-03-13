package com.timesontransfar.common.authorization.service.impl;
/**
 * 用于保存员工对象
 * @author 罗翔 创建于2005-12-07
 * @modify lily  2006-02-24
 *    完善员工保存：创建人和创建时间、修改人和修改时间的正确赋值
 */

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IAuthPersist;
import com.timesontransfar.common.authorization.service.IAuthorizationDAO;
import com.timesontransfar.customservice.common.PubFunc;

@SuppressWarnings("rawtypes")
public class AuthRolePersist implements IAuthPersist {
	
	private JdbcTemplate jdbcTemplate;
	private IAuthorizationDAO authorizationDAO;
	private PubFunc pubFunc;
	
	private String insertStaffRoleRelaSql;
	private String deleteStaffRoleRelaSql;

	public AuthRolePersist() {
		super();
	}

	public String save(Object object) throws DataAccessException {
		TsmStaff staff = (TsmStaff)object;
		int ret = this.updateStaffRoleRela(staff);
		if(ret<0) {
			return String.valueOf(ret);
		}
		return staff.getId();
	}

	public String delete(Object object) throws DataAccessException {
		TsmStaff staff = (TsmStaff)object;
		//删除角色联系
		int affectRow=this.jdbcTemplate.update(this.deleteStaffRoleRelaSql,staff.getId());
		return String.valueOf(affectRow);
	}

	private int updateStaffRoleRela(TsmStaff staff) throws DataAccessException{
		Integer staffId=Integer.valueOf(staff.getId());
		int count = this.jdbcTemplate.update(this.deleteStaffRoleRelaSql,staffId);
		this.insertStaffRoleRelaSql = "INSERT INTO TSM_STAFF_ROLE_RELA(STAFF_ID,ROLE_ID) VALUES(?,?)";
		if(staff.getRoleList()==null) return -1;
		int size=staff.getRoleList().size();
		List tempList=staff.getRoleList();
		for(int i=0;i<size;i++){
			Map roleMap = (Map) tempList.get(i);
			String roleId = (String) roleMap.get("ID");
			count+=this.jdbcTemplate.update(this.insertStaffRoleRelaSql,staffId,roleId);
		}
		return count;
	}

	public String getDeleteStaffRoleRelaSql() {
		return deleteStaffRoleRelaSql;
	}

	public void setDeleteStaffRoleRelaSql(String deleteStaffRoleRelaSql) {
		this.deleteStaffRoleRelaSql = deleteStaffRoleRelaSql;
	}

	public String getInsertStaffRoleRelaSql() {
		return insertStaffRoleRelaSql;
	}

	public void setInsertStaffRoleRelaSql(String insertStaffRoleRelaSql) {
		this.insertStaffRoleRelaSql = insertStaffRoleRelaSql;
	}

	public IAuthorizationDAO getAuthorizationDAO() {
		return authorizationDAO;
	}

	public void setAuthorizationDAO(IAuthorizationDAO authorizationDAO) {
		this.authorizationDAO = authorizationDAO;
	}

	public JdbcTemplate getjdbcTemplate() {
		return jdbcTemplate;
	}

	public void setjdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public PubFunc getPubFunc() {
		return pubFunc;
	}

	public void setPubFunc(PubFunc pubFunc) {
		this.pubFunc = pubFunc;
	}
	
}
