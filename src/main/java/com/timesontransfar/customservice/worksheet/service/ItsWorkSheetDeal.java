/**PIGEONHOLE
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.service;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.worksheet.pojo.ResponsiBilityOrg;
import com.timesontransfar.customservice.worksheet.pojo.SheetOperation;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetAuditing;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitativeGrid;

import net.sf.json.JSONObject;

/**
 * @author 万荣伟
 *
 */
public interface ItsWorkSheetDeal {
	/**
	 * 派单环节派单
	 * 派单环节直接归档-->到归档环节
	 * 派单退回前台
	 * 部门处理转派
	 * 部门工单提交
	 * 审核提交--回访
	 * 审核重新派单
	 * 审核竣工
	 * 回访重新派单
	 * 回访提交 -- 到归档
	 * 回访重新派单
	 * 回访不满意到审核
	 * 归档环节提交 到定性
	 * 归档环节竣工
	 */
	public int checkPushButton(String orderId);
	public String noAnswerPush(String orderId);
	public int getXcSheetCount(String sourceSheetId, String receiveOrgId, int xcType);
	public String xcDispathSheet(SheetPubInfo[] sheetPubInfos, int xcType, String penaltyMoney);
	public String snxcSumbitOrgDeal(String worksheetId, int regionId, int month, String xcContent);
	public String jtxcSumbitOrgDeal(String worksheetId, int regionId, int month, int xcType, String xcContent);
	public String jtxcCancel(String worksheetId);
	public String saveQualitativeAndVisit(TsSheetQualitative bean, TScustomerVisit tscustomerVisit);
	public SheetPubInfo getTheLastSheetInfo(String orderId);

	/**
	 * 派单环节派单*
	 *sheetInfoArry 工单数组对象
	 */
	public String dispatchSheet(SheetPubInfo[] sheetInfoArry);
	
	/**
	 * 派单环节直接归档，派单-->归档环节*
	 * @param sheetBean 工单对象 ，基本值：工单号，月分区，地域，处理内容
	 * @retResult 客户回访内容
	 * @boo 是否需要答复 true是需要答复
	 * @dealId 处理定性ID
	 * @dealDesc 处理定性描述
	 * @return
	 */
	public String dispatchToPigeonhole(SheetPubInfo sheetBean,int dealId,String dealDesc, String isAutoComplete);
	
	/**
	 * 派单环节直接处理*
	 * @param regionId 地域ID
	 * @param orderId 定单号
	 * @param sheetId 工单号
	 * @param month 月分区
	 * @param dealContent 处理内容
	 * @param delalId 处理定性ID
	 * @param dealName 处理定性名
	 * @return
	 */
	public String assignToFinish(int regionId,String orderId,String sheetId,
			Integer month,String dealContent,int delalId,String dealName);
	
	/**
	 * 部门处理环节转派工单*
	 * @param workSheetObj  派往部门对象
	 * @param dealResult 处理要求
	 * @param dealType 0为部门转派工单，1为后台派单 5 审核重新派单 3 为审核退单，4为部门审批退单
	 * @param autoStaffId 0表示人工触发取当前员工，非0表示系统触发取入参员工
	 * @return
	 */
	public String orgDealDispathSheet(SheetPubInfo[] workSheetObj,String dealResult,int askSource,int dealType,int autoStaffId);
	
	/**
	 * 投诉部门处理完成提交方法*
	 * @param sheetPubInfo 工单号，定单号，地域，月分区，处理内容
	 * @return
	 */
	public String sumbitOrgDeal(SheetPubInfo sheetPubInfo, TsSheetAuditing tsSheetAuditing, int delalId, String dealName);
	
	/**
	 * 审核直接归档*
	 * @param sheetPubInfo 工单对象
	 * @param tsSheetAuditing 审核对象
	 * @param tscustomerVisit 回访对象
	 * @param tsSheetQualitative 定性对象
	 * @param dutyOrg 责任部门对象数组
	 * @param sumbitType  审核提交类型,1为竣工,0为到考核环节 2 为到归档环节
	 * @return
	 */
	public String audSheetFinish(SheetPubInfo sheetPubInfo, TsSheetAuditing tsSheetAuditing, TScustomerVisit tscustomerVisit, 
			int sumbitType, ServiceContent servContent, ResponsiBilityOrg[] dutyOrg);
	
	/**
	 * 投诉审核环节重新派单*
	 * 
	 * @param sheetPubInfo
	 * @param acceptContent 处理内容
	 * @param dealType 处理类型 5 为审核重新派单 3 为审核退单
	 *            工单对像
	 * @return 是否成功
	 */
	public String submitAuitSheetToDeal(SheetPubInfo[] workSheetObj,String acceptContent,int dealType);
	
	/**
	 * 投诉工单竣工
	 * @param orderId 定单号
	 * @param orderId 工单号
	 * @return
	 */
	public boolean orderShetFinish(String  orderId,int regionId);
		
	/**
	 * 更新定单信息
	 * 
	 * @date 2011-11-7 LiJiahui 修改
	 * @param custInfo 客户信息
	 * @param servContent 受理内容信息
	 * @param orderAskInfo 定单信息
	 * @param sheetId 工单ID
	 * @param tachId 环节ID
	 * @param errInfo 错单判定相关的信息<br>
	 *     errFlag#errItem#errAdvice
	 * @return 操作结果
	 */
	public String updateServiceContent(OrderCustomerInfo custInfo,ServiceContent servContent, OrderAskInfo orderAskInfo,String sheetId,int tachId, String[] errInfo);
	
	public String updateServiceContentYSAndSJ(OrderCustomerInfo custInfo, ServiceContent servContent, 
			OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, String sheetId, int tachId, String[] errInfo);
	
	public String updateServiceContentNew(
			JSONObject models,
			ServiceOrderInfo serviceInfo,
			String sheetId,
			int tachId,
			ServiceContentSave[] saves,
			String oldAcceptCent,
			String[] errInfo);

	public int saveCmpSupplementModify(String cwi, String cpto, String cptn, String cio, String cin, String dro, String drn, String drs, String askStaffId, String dealStaff, String dealContentSaveStr);

	/**
	 * 审核或审批退单
	 * @param workSheetObj 退单对象
	 * @param dealContent 处理内容
	 * @param dealType 处理类型 0为部门转派工单，1为后台派单 5 审核重新派单 3 为审核退单，4为部门审批退单
	 * @param askSource 受理来源，审核时没用，审批有用
	 * @return
	 */
	public String submitQuitSheet(SheetPubInfo[] workSheetObj,String dealContent,int askSource,int dealType);
	
	/**
	 * 投诉归挡环节重新派单
	 * @param workSheetObj 派单对象
	 * @param dealContent 处理要求
	 * @return
	 */
	public String submitPigeonholeSheetToDeal(SheetPubInfo[] workSheetObj,String dealContent);
	
	/**
	 * 归档工单提交
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param submitType 提交类型 0 为结束 1为定性(考核)
	 * @param month 月分区
	 * @param dealContent 处理内容
	 * @return
	 */
	public String submitPigeonholeSheet(String sheetId,int regionId,int submitType,Integer month,String dealContent);
	
	/**
	 * 申诉单提交
	 * @param reportSheet 申诉工单号ID
	 * @param sheetId  进行考核的工单号
	 * @param regionId 地域
	 * @param month 月分区
	 * @param reportContent 申诉内容
	 * @param falg 申诉类型,report为申诉,confirm为确认
	 * @return
	 */
	public String submitReport(String reportSheet,String sheetId,int regionId,int month,String reportContent,String falg);

	/**
	 * 判断 该工单是否能进行自动归档
	 * @since 2014-03-25
	 * @param worksheetId
	 * @return true可以 /false 不可以
	 */
	public boolean canFinishAuto(String worksheetId);

	public void audSheetFinishVisit(String sheetId, String orderId, int region, Integer month);
	/**
	 * 系统自动完结非投诉单的审核工单
	 * @param audSheet 审核工单实例
	 * @return true 成功/false 失败
	 */
	public boolean finishAudSheetAuto(SheetPubInfo audSheet);
	
	/**
	 * 根据审核单的回单工单号或受理单号，获取处于待处理或处理中状态的审核单工单编码。适用于非投诉工单
	 * @since 2014-03-27
	 * @param sheetId 工单或受理单编号
	 * @param type 1 表示根据当前工单的编号自动完结审核工单；2 表示根据当前受理单编号自动完结审核工单
	 * @return 如果没有查询到符合条件的审核工单，返回null；否则返回审核单工单编号
	 */
	public SheetPubInfo getAudsheet(String sheetId);

	/**
	 * 查询服务入网格定性单
	 * 
	 * @param orderId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public GridDataInfo querySheetQualitativeGrid(String orderId, String startTime, String endTime, String begin, String pageSize);

	/**
	 * 根据服务单号修改服务入网格
	 * 
	 * @param TsSheetQualitativeGrid
	 * @return
	 */
	public int saveSheetQualitativeGrid(TsSheetQualitativeGrid sqg);
	
	/**
	 * 判断是否可以进行派单
	 * @param orderId
	 * @param workSheetId
	 * @param revOrgId 收单部门
	 * @param strOrgId 主办对象
	 * @param sendType 主办标识
	 * @param xbStrOrgId 协办对象
	 * @param xbSendType 协办标识
	 * @return
	 */
	public String validationTurnOrg(String orderId, String workSheetId, String revOrgId, String strOrgId, int sendType, String xbStrOrgId, int xbSendType);

	public int saveSheetOperation(SheetOperation operation);
	
	public String xcRefundDispathSheet(SheetPubInfo curSheet, String prmRefundAmount, String refundDetail, String refundData);
	
}