package com.timesontransfar.customservice.orderask.service;

import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;

public interface ServiceOrderQuery {

	public ServiceOrderInfo getServOrderInfo(String orderId, boolean hisFlag);
	
	public Map<String, String> getWarnOrgWhere(String staffId);
	
	@SuppressWarnings("rawtypes")
	public Map getStaffSheet(String staffId,String expiredDate);

	@SuppressWarnings("rawtypes")
	public List getViewStaff(String staffId, String queryType);
	
	public String getOrderIdStrByMiitCode(String miitCode, boolean hisFlag);
	
}
