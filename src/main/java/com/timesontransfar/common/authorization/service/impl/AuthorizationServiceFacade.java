package com.timesontransfar.common.authorization.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IAuthorizationService;
import com.timesontransfar.common.exception.BusinessException;

@SuppressWarnings("rawtypes")
public class AuthorizationServiceFacade implements IAuthorizationService {
	
	@Autowired
	private IAuthorizationService authorizationService;

	
	public TsmStaff getStaff(String staffId) {
		return this.authorizationService.getStaff(staffId);
	}

	public List getStaffRoleList(Map inParams) {
		return this.authorizationService.getStaffRoleList(inParams);
	}

	public String saveStaff(Map inParams) throws BusinessException {
		return this.authorizationService.saveStaff(inParams);
	}

	public String saveStaffRole(String staffId, List lstRole) throws BusinessException {
		return this.authorizationService.saveStaffRole(staffId, lstRole);
	}

}
