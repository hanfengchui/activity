package com.timesontransfar.customservice.worksheet.dao;

import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;

public interface ISJSheetQualitative {
	/**
	 * 商机定性表添加记录
	 * 
	 * @param bean
	 * @return
	 */
	public int saveSJSheetQualitative(SJSheetQualitative bean);

	/**
	 * 商机定性历史表添加记录
	 * 
	 * @param serviceOrderId
	 * @return
	 */
	public int saveSJSheetQualitativeHis(String serviceOrderId);

	/**
	 * 得到商机定性对象
	 * 
	 * @param workSheetId 工单号
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public SJSheetQualitative getSJSheetQualitative(String workSheetId, boolean boo);
	
	public int saveHisSJSheetQualitative(SJSheetQualitative bean);
}
