package com.timesontransfar.customservice.worksheet.dao;

import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;

public interface ItsCustomerVisit {
	/**
	 * 投诉回访表添加记录
	 * @param bean
	 * @return
	 */
	public int saveCustomerVisit(TScustomerVisit bean);	

	/**
	 * 投诉回访临时表添加记录
	 * @param bean
	 * @return
	 */
	public int saveCustomerVisitTmp(TScustomerVisit bean);
	/**
	 * 投诉回访历史表添加记录
	 * @param servid 定单号
	 * @param sheetid 工单号
	 * @param month 月分区
	 * @return
	 */
	public int saveCustomerVisitHis(String servid,String sheetid ,int region);	

	/**
	 * 得到投诉工单的回访对象
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param boo true 查询当前  false 查询历史
	 * @return
	 */
	public TScustomerVisit getCustomerVisitObj(String sheetId,int regionId,boolean boo);

	public TScustomerVisit getCustomerVisitByOrderId(String orderId, boolean boo);

	/**
	 * 得到投诉工单的临时回访对象
	 * @param sheetId
	 * @param regionId
	 * @return
	 */
	public TScustomerVisit getCustomerVisitTmpObj(String sheetId,int regionId);
	
	/**
	 * 删除投诉回访记录表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteCustomerVisit(String serviceOrderId ,String workSheetId,int region);	
	
	/**
	 * 删除投诉回访临时记录表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteCustomerVisitTmp(String serviceOrderId, int region);
}