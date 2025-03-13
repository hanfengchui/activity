package com.timesontransfar.customservice.worksheet.dao;

import java.util.List;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.worksheet.pojo.ForceDistill;

public interface IForceDistillDao {
	public int insertForceDistill(ForceDistill forceDistill);

	public int selectForceStaffByOrderId(String orderId);

	@SuppressWarnings("rawtypes")
	public List getCurSheetInfo(String orderId, String orderStatu);

	public int insertForceDistillHisByOrderId(String orderId);

	public GridDataInfo selectForceDistill(String regionId, String serviceType, String bestOrder, String hours, int begin);
}