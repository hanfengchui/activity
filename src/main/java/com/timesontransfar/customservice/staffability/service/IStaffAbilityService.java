package com.timesontransfar.customservice.staffability.service;

import java.io.InputStream;

import com.timesontransfar.customservice.staffability.pojo.StaffAbility;

public interface IStaffAbilityService {
		
	public int updateStaffAbility(StaffAbility bean);
	
	public String saveStaffAbilityBatch(String loginName, InputStream file);
	
	public int deleteStaffAbility(String guid);
	
	public StaffAbility getStaffAbilityById(String guid);
	
	public StaffAbility isStaffAbilityExists(int staffId);
}
