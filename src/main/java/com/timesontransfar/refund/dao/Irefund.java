package com.timesontransfar.refund.dao;

import com.timesontransfar.refund.pojo.RefundPojo;

public interface Irefund {
	
	public int insertRefund(RefundPojo refund);

	public int updateRefund(RefundPojo refund);

    /**
     * 根据地域ID、月份值，查询
     * 
     * @param region
     *            region ID
     * @param month
     *            yyyymm
     * @return 查询得到的记录数
     */
	public int checkMonth(int region, String month);
}