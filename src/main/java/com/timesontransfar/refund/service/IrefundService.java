package com.timesontransfar.refund.service;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.refund.pojo.RefundPojo;

public interface IrefundService {
	
	public GridDataInfo getRefund(String parm);

	public String insertRefund(RefundPojo refund);

	public int updateRefund(RefundPojo refund);
}