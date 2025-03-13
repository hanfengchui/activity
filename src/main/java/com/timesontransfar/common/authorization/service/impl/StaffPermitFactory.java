package com.timesontransfar.common.authorization.service.impl;

import com.timesontransfar.common.authorization.service.IStaffPermit;
import com.timesontransfar.common.authorization.service.IStaffPermitFactory;

public class StaffPermitFactory implements IStaffPermitFactory {

	public StaffPermitFactory() {
		super();
	}

	public IStaffPermit getStaffPermit() {
		return new StaffPermit();
	}
}
