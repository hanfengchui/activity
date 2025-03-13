package com.timesontransfar.customservice.orderask.service.impl;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.dao.IcomplaintInfoDao;
import com.timesontransfar.customservice.orderask.service.IserviceComplaintInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("IserviceComplaintInfo")
public class ServiceComplaintInfoImpl implements IserviceComplaintInfo {
 
    @Autowired
    private IcomplaintInfoDao icomplaintInfoDao;

	@Override
	public GridDataInfo getData(String acceptTime, String ipAddress, int currentPage, int pageSize, String activeName) {
		return icomplaintInfoDao.getData(acceptTime,ipAddress,currentPage,pageSize,activeName);
	}
}