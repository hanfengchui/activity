package com.timesontransfar.customservice.worksheet.service;

import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;

public interface IYnSheetDealService {
	/**
	 * 审核环节重新派单(待修改)
	 * 
	 * @param sheetPubInfo
	 *            工单对像
	 * @return 是否成功
	 */
	public String submitAuitSheetToDeal(SheetPubInfo[] workSheetObj,String acceptContent) ;
	/**
	 * 疑难审核环节归档，不走工作流的自动回访环节
	 * @param retVisitResult
	 * @param sheetId
	 * @param orgId
	 * @param region
	 * @param month
	 * @return
	 */	
	public String auitSheetFinish(RetVisitResult retVisitResult,
			String sheetId, String orgId,int regionId,Integer month);
	
	/**
	 * 审核单提交
	 * 
	 * @param orderAskInfo
	 *            定单对像
	 * @param SheetId
	 *            工单id
	 * @param autoVisitFlag
	 *            是否自动回访
	 * @param dealRequire 处理要求
	 * @param orgId 责任部门ID
	 * @return 是否成功
	 */
	public String submitAuitSheet(OrderAskInfo orderAskInfo, String sheetId,
			int autoVisitFlag, String dealRequire, String orgId);
	
	
	/**
	 * 部门处理单提交方法 待修改
	 * @param strReasonId	回单原因id字符串
	 * @param strReasonDesc	回单原因描述字符串
	 * @param dealResult    处理结果描述
	 * @return	是否成功
	 */
	public String submitDealSheet(SheetPubInfo sheetPubInfo,
			String strReasonId, String strReasonDesc, String dealResult,int netFlag);
	
	public String submitDealSheetToDealNew(SheetPubInfo[] workSheetObj,String dealResult);
	
}
