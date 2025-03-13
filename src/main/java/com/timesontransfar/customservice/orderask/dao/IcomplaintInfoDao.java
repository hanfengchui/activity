package com.timesontransfar.customservice.orderask.dao;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;

public interface IcomplaintInfoDao {

	/**
	 * 根据ip地址查询申诉单
	 */
	public GridDataInfo getData(String acceptTime, String ipAddress,int currentPage,int pageSize,String activeName);
}