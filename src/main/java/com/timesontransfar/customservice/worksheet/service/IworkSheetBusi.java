/**
 * <p>类名：IworkSheetBusi.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：增加回访信息查询　getVisiResult 万荣伟 2008/06/05</p>
 * <p>  Create by:	lifeng	Mar 24, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.service;

import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.pojo.NoteSendList;
import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.ServiceWorkSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetTodispatch;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.ReturnBlackList;

/**
 * @author wanrongwei
 */
@SuppressWarnings("rawtypes")
public interface IworkSheetBusi {	
	/**
	 * 退单给受理人发短信
	 * 
	 * @return
	 */
	public void sendNoteToAccept(int askStaffId, int regionId, String workSheetId, String sendContent);
    /**
     * 部门处理时解挂工单
     * @param sheetId 工单id  
     * @param lookFlag 1为我的任务，0为工单池
     * @return  解挂工单数量
     */ 
    public int unHoldWorkSheetNew(String sheetId,int region,Integer month,int lookFlag);
    /**
     * 挂起工单
     * @param sheetId 工单id   
     * @param comments 操作备注
     * @param flag 0为必须进行申请才能挂起,1为直接挂起
     * @lookFlag 1为我的任务，0为工单池
     * @return  挂起工单数量
     */
    public int holdWorkSheetNew(String sheetId, String comments,int region,Integer month,int flag,int lookFlag);
	/**
	 * 查询工单的实际处理时限,减去夜间时差
	 * @param sheetId 工单号
	 * @param flag 是否历史	 
	 * @return
	 */
	public String getSheetObjByWorkTime(String sheetid ,boolean flag);
	/**
	 * 取流向派发部门、流向部门、流向到达时间
	 * @param orderId
	 * @return
	 */
	public List getReturnTime(String orderId);
	/**
	 * 从工单池提取工单方法
	 * @param sheetId
	 * @return	取单结果
	 */
	public String fetchWorkSheet(String sheetId,int region,Integer month);

	/**
	 * 释放工单到工单池中,请使用releaseWorkSheetNew
	 * @param sheetId	工单id
	 * @return	释放的工单数量
	 * @deprecated
	 */
	public String releaseWorkSheet(String sheetId,int region,Integer month);
	
	/**
	 * 释放工单到工单池中新方法
	 * @param sheetId	工单id
	 * @param falg 0必须经过申请，1为不经过申请
	 * @return	释放的工单数量
	 */
	public String releaseWorkSheetNew(String sheetId,int region,Integer month,int falg);

	/**
	 * 我的任务挂起工单
	 * @param sheetId 工单id	 
	 * @param comments 操作备注
	 * @param flag 0为必须进行申请才能挂起,1为直接挂起
	 * @return	挂起工单数量
	 */
	public int holdWorkSheet(String sheetId, String comments,int region,Integer month,int flag);
	/**
	 * 部门非工作时间自动挂起工单
	 * @param sheetId 工单id	 
	 * @param comments 操作备注
	 * @param flag 0为必须进行申请才能挂起,1为直接挂起
	 * @return	挂起工单数量
	 */
	public int holdWorkSheetPub(SheetPubInfo sheetPubInfo);
	/**
	 * 我的任务工单池解挂工单
	 * @param sheetId 工单id	 
	 * @return	解挂工单数量
	 */
	public int unHoldWorkSheet(String sheetId,int region,Integer month);
	/**
	 * 工单池中挂起工单批量释放
	 * @param bean
	 * @return
	 */
	public int unHoldWorkSheetPub(SheetPubInfo[] bean);

	/**
	 * 从工单池批量取回工单
	 * 
	 * @param sheetIdList 格式为 sheetId@regionId@month
	 *            工单号字符数组
	 * @param  flowType 流程号
	 * @param fetchType 提取方式，0为系统自动提取，1为人工选择提取          
	 * @return 取单结果
	 */
	public String fetchBatchWorkSheet(String[] sheetIdList,int flowType,int fetchType);

	/**
	 * 批量释放工单到工单池中
	 * @param guidList      申请ID
	 * @param sheetList     工单ID
	 * @param regionList    地域ID
	 * @param month         月分区ID
	 * @param audResult     审批内容
	 * @param applyAudStatu 审批状态 1为同意 2 为不同意
	 * @param applyType     申请类型 0挂起  1释放
	 * @return 释放的工单数量
	 */
	public int releaseBatchWorkSheet(String[] guidList,String[] sheetList,int[] regionList,
			   Integer[] month,String audResult,int applyAudStatu,int applyType);

	/**
	 * 根据工单号查询工单完整信息
	 * @param sheetId 工单号
	 * @return	工单对象
	 */
	public WorkSheetInfo getWorkSheetInfo(String sheetId,int region,Integer month);

	/**
	 * 查询系统时间
	 * @return	系统时间
	 */
	public String getSysDate();

	/**
	 * 将派单工单退回受量台
	 * @param sheetPubInfo
	 *            工单对象
	 * @return	是否成功
	 */
	public boolean assignBackToAsk(SheetPubInfo sheetPubInfo);
	
	/**
	 * 派单台人工派单方法
	 * 
	 * @param sheetPubInfo
	 *            工单对象
	 * @return 是否成功
	 */
	public String assignSheetToDeal(SheetPubInfo[] sheetPubInfo);

	/**
	 * 部门处理单提交方法
	 * @param strReasonId	回单原因id字符串
	 * @param strReasonDesc	回单原因描述字符串
	 * @param dealResult    处理结果描述
	 * @return	是否成功
	 */
	public String submitDealSheet(SheetPubInfo sheetPubInfo,
			String strReasonId,String strReasonDesc, String dealResult,int netFlag);
	
	/**
	 * 预受理部门处理单提交方法
	 * sheetPubInfo 工单对象
	 * orderAskInfo  定单对象
	 * @return	是否成功
	 */
	public String submitDealSheetYs(SheetPubInfo sheetPubInfo,OrderAskInfo orderAskInfo);

	/**
	 * 审核环节重新派单
	 * 
	 * @param sheetPubInfo
	 *            工单对像
	 * @return 是否成功
	 */
	public String submitAuitSheetToDeal(SheetPubInfo[] sheetPubInfo,String acceptContent);

	/**
	 * 审核环节归档，不走工作流的自动回访环节
	 * @param retVisitResult
	 * @param sheetId
	 * @param orgId
	 * @param region
	 * @param month
	 * @return
	 */
	public String auitSheetFinish(RetVisitResult retVisitResult,
			String sheetId, String orgId,int region,Integer month);

	/**
	 * 商机单审核环节直接处理增加办结原因
	 */	
	public String auitSheetFinishWithQualitative(RetVisitResult retVisitResult, String sheetId, String orgId, int regionId, Integer month, SJSheetQualitative sjQualitative);

	/**
	 * 商机单审核环节直接处理增加办结原因
	 */
	public String auitSheetFinishWithQualitativeNew(RetVisitResult retVisitResult, BuopSheetInfo buopSheetInfo, String sheetId, String orgId, int regionId, Integer month, SJSheetQualitative sjQualitative);


	// 商机单进入自动回访队列
	public boolean autoVisitSJ(String orderId, Integer month, String sheetId, int tacheType, String serviceTypeDetail);

	// 即时测评办结商机单
	public void auitSheetFinishFromVisit(String tacheType, String sheetId, String orderId, int region, Integer month);

	/**
	 * 回访环节重新派单
	 * 
	 * @param sheetPubInfo
	 *            工单对像
	 * @return 是否成功
	 */
	public boolean submitReplySheetToDeal(SheetPubInfo[] sheetPubInfo,String acceptContent);
	
	/**
	 * 定单工单注销方法
	 * @param orderAskInfo 受理单受理信息
	 * @return 是否成功
	 */
	public boolean cancelServiceOrder(OrderAskInfo orderAskInfo);

	/**
	 * 根据员工登录名查询到员工STAFFID
	 * @param logname 员工登录名
	 * @return 员工ID
	 */
	public int getStaffId(String logname);
	
	/**
	 * 处理环节保存处理内容
	 * @param sheetid 工单ID
	 * @param region 地域ID
	 * @param content 处理结果
	 * @return true 保存成功
	 */
	public boolean saveDealContent(String sheetid,int region,String content);
	
	/**
	 * 查询地域LINK_ID
	 * @param region 地域ID
	 * @return
	 */
	public String getRegionLinkId(int region);
	
	/**
	 * 查询工单的基本信息
	 * @param sheetId 工单号
	 * @param orderId 定单号码
	 * @param regionId 地域ID
	 * @param flg 查询当前或历史标识
	 * @return
	 */
	public ServiceWorkSheetInfo getServiceSheetInfo(String sheetId,String orderId,int regionId,Integer month,boolean flg,Integer vesion);

	/**
	 * 回访完成到工单归档环节
	 * @param sheetId 工单号
	 * @param orderId 定单号
	 * @returnPIGEONHOLE Pigeonhole
	 */
	public String replySheetToPigeonhole(String sheetId,String orderId);
	
	/**
	 * 工单归档环节到工单定性
	 * @param sheetId 工单号
	 * @param orderId 定单号
	 * @return
	 */
	public String pigeonholeToQualitative(String sheetId,String orderId);
	
	
	/**
	 * 工单定性到工单竣工
	 * @param sheetId 工单号
	 * @param orderId 定单号
	 * @return
	 */
	public String qualitativeTifinsh(String sheetId,String orderId);
	
	/**
	 * 工单归档到竣工环节
	 * @param sheetId 工单号
	 * @param orderId 定单号
	 * @return
	 */
	public String pigeonholeTofinsh(String sheetId,String orderId);
	
	/**
	 * 得到商机定性对象
	 * 
	 * @param workSheetId 工单号
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public SJSheetQualitative getSJSheetQualitativeFlow(String workSheetId, boolean boo);

	/**
	 * 得到部门审批单或审核工单的关联工单
	 * @param sheetId 工单号
	 * @param month 月分区标志
	 * @return 工单对象
	 */
	public SheetPubInfo[] getRelatSheet(String sheetId,Integer month);
	
	/**
	 * 更新或保存短信发送列表
	 * @param bean
	 * @param month 1为保存 2为修改
	 * @return
	 */
	public String updateNoteList(NoteSendList bean,int month);
	
	/**
	 * 把派出去的工单追回,可以使审批单或审核单可以做审核
	 * @param orderId
	 * @param workSheetId
	 * @param monthFlag
	 * @return
	 */
	public String workSheetReplevy(String orderId,String workSheetId,Integer monthFlag,int regionId);
	
	/**
	 * 工单挂起,释放申请
	 * @param sheetId 工单号
	 * @param region 地域
	 * @param month 月分区
	 * @param applyReason 申请原因
	 * @param applyType 申请类型
	 * @return workSheetStatuApply
	 */
	public String workSheetStatuApply(String sheetId,int region,Integer month,String applyReason,int applyType);
	/**
	 * 审批工单申请的挂起和释放 audWorkSheetApply
	 * @applyGuid 申请唯一ID
	 * @param sheetId 工单号
	 * @param region 地域编码
	 * @param month //月分区
	 * @param audResult 审批结果
	 * @param applyAudStatu 审批状态 1为同意 2 为不同意
	 * @param applyType 申请类型
	 * @return
	 */
	public String workSheetAudApply(String applyGuid,String sheetId,int region,Integer month,String audResult,int applyAudStatu,int applyType);

	/**
	 * 分拣退回到前台受理
	 * (苏州业务受理)
	 * 
	 * @param sheetPubInfo
	 *            工单对象
	 * @return success
	 */
	public String doPickToAskBack(SheetPubInfo sheetPubInfo,Map otherInfo);
	
	/**
	 * 根据部门id获取不在工单地域机构列表
	 * @param orgIdArr
	 * @param sheetReginId
	 * @return
	 */
	public List getOrgsNotInSheetRegion(String[] orgIdArr,String sheetReginId);
	
	/**
	 * 根据staffid获取不在工单地域机构列表
	 * @param staffIds
	 * @param sheetReginId
	 * @return
	 */
	public List getStaffNameNotInSheetRegion(String[] staffIds,String sheetReginId);
	
	/**
	 * 保存处理人员对于审核时转派动作的建议信息
	 * @param todispatch 建议信息
	 * @return
	 */
	public int saveSheetTodispatch(SheetTodispatch todispatch);
	
	public String saveAndqryDealContent(String sheetId,int region,String content,int month,String orderId);
	
	public String cancelWorkFlow(String wfInstId);

	public Map<String, Object> getBuopSheetInfo(String serviceOrderId);

	public String updateReturnList(ReturnBlackList bean);

	public String saveReturnList(ReturnBlackList bean);

	public Map finishOrderAndSheetByOrderId(String orderId);
}