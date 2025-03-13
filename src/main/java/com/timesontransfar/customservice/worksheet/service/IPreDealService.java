package com.timesontransfar.customservice.worksheet.service;


import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;


/**
 * 业务预受理 业务接口
 * @author 张显
 *
 */
public interface IPreDealService {
	/**
	 * 预受理退回前台派单提交方法
	 * @param sheetPubInfo	工单对象
	 * @return
	 */
	String assignBackToAskFull(SheetPubInfo sheetPubInfo, int dealStaff);
	
	
	/**
	 * 部门处理转派单提交方法
	 * @param sheetPubInfo	工单对象
	 * @return
	 */
	String submitDealSheetToDeal(SheetPubInfo[] workSheetObj,String dealResult);
	
	/**
	 * 暂存处理内容
	 * @param sheetId
	 * @param regionId
	 * @param content
	 * @return
	 */
	String saveDealContent(String sheetId,String regionId,String content);
	
	/**
	 * 释放申请
	 * @param sheetId
	 * @param regionId
	 * @param month
	 * @param applyReason
	 * @param applyType
	 * @return
	 */
	String workSheetStatuApply(String sheetId,int regionId,int month,String applyReason,int applyType);
	
	/**
	 * 解挂
	 * @param sheetId
	 * @param region
	 * @param month
	 * @return
	 */
	int unHoldWorkSheet(String sheetId,int region,int month);
	
	/**
	 * 取消申请
	 * @param sheetId
	 * @param region
	 * @param month
	 * @return
	 */
	String workSheetCancelApply(String sheetId,int region,int month,String applyReason);
	
	
	

	/**
	 * 将派单工单退回受量台
	 * 
	 * @param sheetPubInfo
	 *            工单对象
	 * @return 是否成功
	 */
	public boolean assignBackToAsk(SheetPubInfo sheetPubInfo, int dealStaff);
	
	
	/**
	 * 预受理部门处理单提交方法
	 * sheetPubInfo 工单对象
	 * orderAskInfo  定单对象
	 * @return	是否成功
	 */
	public String submitDealSheetYs(SheetPubInfo sheetPubInfo,OrderAskInfo orderAskInfo);


	/**
	 * 预受理部门处理单提交方法
	 * sheetPubInfo 工单对象
	 * orderAskInfo  定单对象
	 * buopSheetInfo 商机单对象
	 * @return	是否成功
	 */
	public String submitDealSheetYsNew(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo);
	
	/**
	 * 预受理工单完成
	 * @param sheetPubInfo 工单对象
	 * @param orderAskInfo 定单对象 必须带联系电话
	 * @param boo 是否发送短信 true 发送短信 false 不送短信
	 * @param buopSheetInfo 商机单对象
	 * @param sendContent 发送内容  您办理的***业务已经受理成功，将于 “时间” 生效。
	 * @return
	 */
	public String updateBeforSheetNew(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, boolean boo, String sendContent, int dealStaff);

	/**
	 * 商机单后台派单环节直接处理增加办结原因
	 * @param sheetPubInfo 工单对象
	 * @param orderAskInfo 定单对象 必须带联系电话
	 * @param boo 是否发送短信 true 发送短信 false 不送短信
	 * @param sendContent 发送内容  您办理的***业务已经受理成功，将于 “时间” 生效。
	 * @param sjQualitative 办结原因目录
	 * @param sjQualitative 办结原因目录
	 * @return
	 */
	public String updateBeforSheetWithQualitativeNew(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, boolean boo, String sendContent, SJSheetQualitative sjQualitative, int dealStaff);


	public void sendNoteCont(String soureNum,int regionId,String sendContent, String serviceId);
}
