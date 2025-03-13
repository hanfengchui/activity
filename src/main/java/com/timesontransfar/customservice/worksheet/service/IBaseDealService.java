package com.timesontransfar.customservice.worksheet.service;

import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;

public interface IBaseDealService {
	/**
	 * 处理环节保存处理内容
	 * @param sheetid 工单ID
	 * @param region 地域ID
	 * @param content 处理结果
	 * @return true 保存成功
	 */
	public boolean saveDealContent(String sheetId,int region,String content);
	
	/**
	 * 将派单工单退回受量台
	 * 
	 * @param sheetPubInfo
	 *            工单对象
	 * @return 是否成功
	 */
	public boolean assignBackToAsk(SheetPubInfo sheetPubInfo, int dealStaff);
	

	/**
	 * 工单挂起,释放申请
	 * @param sheetId 工单号
	 * @param region 地域
	 * @param month 月分区
	 * @param applyReason 申请原因
	 * @param applyType 申请类型
	 * @return
	 */
	public String workSheetStatuApply(String sheetId,int region,Integer month,String applyReason,int applyType);
	
	
	/**
	 * 取消工单挂起,释放申请 cancelSheetApply
	 * @param sheetId 工单号
	 * @param region 地域
	 * @param month 月分区
	 * @param canceReason 取消原因
	 * @return
	 */
	public String workSheetCancelApply(String sheetId,int region,Integer month,String canceReason);
	
	/**
	 * 部门处理转派单提交方法
	 * @param sheetPubInfo	工单对象
	 * @return
	 */
	public String submitDealSheetToDeal(SheetPubInfo[] workSheetObj,String dealResult);
	

	/**
	 * 部门处理时解挂工单
	 * @param sheetId 工单id	
	 * @return	解挂工单数量
	 */
	public int unHoldWorkSheet(String sheetId,int region,Integer month);
	
	/**
	 * 获取工单对象信息
	 * @param sheetId
	 * @param hisflag
	 * @return
	 */
	public SheetPubInfo querySheetPubInfo(String sheetId, boolean hisflag);
}
