package com.timesontransfar.common.authorization.service.impl;

import com.timesontransfar.common.authorization.model.TsmOrganization;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IAuthPersist;
import com.timesontransfar.common.authorization.service.IAuthPersistFactory;

public class AuthPersistFactory implements IAuthPersistFactory {
	private IAuthPersist authStaffPersist;
	private IAuthPersist authOrgPersist;

	public AuthPersistFactory() {
		super();
	}

	public IAuthPersist getPersist(Class clazz) {
		if(clazz.equals(TsmStaff.class)){
			return this.authStaffPersist;
		}else if(clazz.equals(TsmOrganization.class)){
			return this.authOrgPersist;
		}
		return null;
	}

	public IAuthPersist getAuthStaffPersist() {
		return authStaffPersist;
	}

	public void setAuthStaffPersist(IAuthPersist authStaffPersist) {
		this.authStaffPersist = authStaffPersist;
	}

	public IAuthPersist getAuthOrgPersist() {
		return authOrgPersist;
	}

	public void setAuthOrgPersist(IAuthPersist authOrgPersist) {
		this.authOrgPersist = authOrgPersist;
	}

}
