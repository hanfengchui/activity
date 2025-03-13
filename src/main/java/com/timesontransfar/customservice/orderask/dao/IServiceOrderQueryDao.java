package com.timesontransfar.customservice.orderask.dao;

import java.util.List;
import java.util.Map;

public interface IServiceOrderQueryDao {

	public Map<String, Object> getWarnRole(String staffId);
	
	@SuppressWarnings("rawtypes")
	public Map getStaffSheet(String staffId,String expiredDate);

	@SuppressWarnings("rawtypes")
	public List getViewStaff(String staffId, String queryType);
	
}
