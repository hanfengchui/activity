package com.timesontransfar.common.authorization.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import com.timesontransfar.common.authorization.model.TsmRole;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IAuthPersist;
import com.timesontransfar.common.authorization.service.IAuthorizationDAO;
import com.timesontransfar.common.authorization.service.IAuthorizationService;
import com.timesontransfar.common.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AuthorizationServiceImpl implements IAuthorizationService {
	private static final Logger logger = LoggerFactory.getLogger(AuthorizationServiceImpl.class);
	
	private IAuthorizationDAO authorizationDAO;
	private IAuthPersist staffPersist;
	private IAuthPersist rolePersist;
	
	@Autowired
	private RedisUtils redisUtils;
	

	public AuthorizationServiceImpl() {
		super();
	}
	
	public TsmStaff getStaff(String staffId){
		return this.authorizationDAO.getStaff(staffId) ;
	}

	/**
	 * 返回指定员工已有的角色列表；返回当前登录员工有权限看到的角色列表
	 * @param inParams
	 * @return
	 */
	public List getStaffRoleList(Map inParams){
		String staffId=(String)inParams.get("TSM_STAFF__STAFF_ID");
		String logonStaffId=(String)inParams.get("TSM_STAFF__LOGON_STAFF_ID");
		Map roleMap = new TreeMap();
		if(staffId!=null && !staffId.equals("")){
			List tempList = this.authorizationDAO.getRolesHoldInStaff(staffId);
			for(int i=0;i<tempList.size();i++){
				TsmRole role= (TsmRole)tempList.get(i);
				roleMap.put(role.getId(),role.getName());
			}
		}
		
		List tempList=this.authorizationDAO.getStaffPermitRoles(logonStaffId);
		Map roleOrgMap = new TreeMap();
		for(int i=0;i<tempList.size();i++){
			TsmRole role=(TsmRole)tempList.get(i);
			if(!roleMap.containsKey(role.getId())){
				roleOrgMap.put(role.getId(),role.getName());
			}
		}
		List returnList = new ArrayList();
		returnList.add(roleMap);
		returnList.add(roleOrgMap);
		return returnList;
	}

	/**
	 * 保存员工
	 * 2006-8-28 梁勇修改
	 * 2014-12-08 李佳慧为江苏10000重写
	 */
	public String saveStaff(Map inParams) throws BusinessException{
		/*		
			TSM_STAFF__STAFF_ID			【员工id】
			TSM_STAFF__STAFF_NAME		【员工姓名】
			TSM_STAFF__ORG_ID	【组织机构id】
			TSM_STAFF__LOGONNAME			【登录名】
			TSM_STAFF__PASSWORD		    【密码】
			TSM_STAFF__RELAPHONE			【联系电话】
			TSM_STAFF__RELAEMAIL			【Email】
			TSM_STAFF__GENDER				【性别】
			TSM_STAFF__STAFF_LEVEL		【员工级别】
        */
		String staffId=(String)inParams.get("WEB__TSM_STAFF__STAFF_ID__ATTR_600010__2");
		TsmStaff staff=null;
		if(staffId!=null && !staffId.equals("")){  //修改员工
			staff=this.authorizationDAO.getStaff(staffId);
		}else{ //新增员工
			staff = new TsmStaff();
		}
		staff.setGender(Integer.parseInt((String)inParams.get("WEB__TSM_STAFF__GENDER__ATTR_600019__2")));
		staff.setLevel(Integer.parseInt((String)inParams.get("WEB__TSM_STAFF__STAFF_LEVEL__ATTR_600020__2")));
		staff.setLogonName((String)inParams.get("WEB__TSM_STAFF__LOGONNAME__ATTR_600014__2"));
		staff.setName((String)inParams.get("WEB__TSM_STAFF__STAFFNAME__ATTR_600011__2"));
		staff.setOrganizationId((String)inParams.get("WEB__TSM_STAFF__ORG_ID__ATTR_600012__2"));
		staff.setRelaMail((String)inParams.get("WEB__TSM_STAFF__RELAEMAIL__ATTR_600016__2"));
		staff.setRelaPhone((String)inParams.get("WEB__TSM_STAFF__RELAPHONE__ATTR_600017__2"));
		staff.setState(8);//有效
		String password = "";
		password = (String)inParams.get("WEB__TSM_STAFF__PASSWORD__ATTR_600026__2");
		if(staffId == null || staffId.equals("")){// 表示新建员工
			 if(password == null || password.equals(""))// 新员工如果不设置密码，则使用默认密码123
				 staff.setPassword(this.pwdEncode("123"));
			 else
				 staff.setPassword(this.pwdEncode(password));
		}else{
			// 非新员工，如果密码为空，表示保持原密码不变。如果密码不为空，表示变更密码。
			if(password != null && password.length() > 0){
				staff.setPassword(this.pwdEncode(password));
			}
		}
		String retValue = this.staffPersist.save(staff);
		try{
			this.redisUtils.del(RedisType.WORKSHEET,"LOGONNAME__"+staff.getLogonName());
		}catch(Exception e){
			logger.error("saveStaff error:{}", e.getMessage(), e);
		}
		return retValue;
	}

	public String saveStaffRole(String staffId,List lstRole) throws BusinessException{
		TsmStaff staffObj = new TsmStaff();
		staffObj.setId(staffId);
		List lst = new ArrayList();
		for(int i=0;i<lstRole.size();i++){
			Map temp = new TreeMap();
			temp.put("ID",lstRole.get(i));
			lst.add(i,temp);
		}
		staffObj.setRoleList(lst);
		return this.rolePersist.save(staffObj);
	}
	
    private String pwdEncode(String toEncode){
    	StringBuffer pwd = new StringBuffer();
		byte[] bs = toEncode.getBytes(StandardCharsets.UTF_8);
		for(int i = 0; i < bs.length; i++){
			pwd.append(bs[i]).append("-");
		}
		return pwd.substring(0, pwd.length() - 1);
	}

	public IAuthorizationDAO getAuthorizationDAO() {
		return authorizationDAO;
	}

	public void setAuthorizationDAO(IAuthorizationDAO authorizationDAO) {
		this.authorizationDAO = authorizationDAO;
	}

	public IAuthPersist getStaffPersist() {
		return staffPersist;
	}

	public void setStaffPersist(IAuthPersist staffPersist) {
		this.staffPersist = staffPersist;
	}
    
	public IAuthPersist getRolePersist() {
		return rolePersist;
	}

	public void setRolePersist(IAuthPersist rolePersist) {
		this.rolePersist = rolePersist;
	}

}