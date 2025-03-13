/**
 * <p>类名：ISheetPubInfoDao.java</p>
 * <p>功能描叙：工单公共信息数据操作接口</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：2、增加工单预约功能 updatePrecontract 万荣伟 2008-08-12</p>
 * <p>  Create by:	lifeng	Mar 18, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.dao;

import java.util.List;
import java.util.Map;

import com.cliqueWorkSheetWebService.pojo.ComplaintConnection;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetReAssignReason;
import com.timesontransfar.customservice.worksheet.pojo.XcFlow;
/**
 * @author lifeng
 *
 */
@SuppressWarnings("rawtypes")
public interface ISheetPubInfoDao {
	/**
	 * 取流向派发部门、流向部门、流向到达时间
	 * @param orderId
	 * @return
	 */
	public List getReturnTime(String orderId);

	/**
	 * 错单进历史
	 * @param orderId 错单受理号
	 * @param sheetId 错单工单号
	 * @param month 月分区
	 * @return 修改的记录数
	 */
	public int saveErrSheetHis(String orderId, String sheetId, int month);
	
	/**
	 * 修改错单工单的状态值
	 * @param orderId 错单受理号
	 * @param sheetId 错单工单号
	 * @param errAppeal 申诉理由
	 * @param statu 工单状态
	 * @param statuDesc 工单状态描述
	 * @param finishFlag 1表示未完结；0表示完结。
	 * @return 更新的记录数
	 */
	public int updateErrSheet(String orderId, String sheetId, String errAppeal, int statu,String statuDesc, int finishFlag);
	
	/**
	 * 保存错单申诉的审批结果
	 * @param orderId 错单受理号
	 * @param sheetId 错单工单号
	 * @param suredMsg 审批意见/结果
	 * @param statu 工单状态ID
	 * @param statuDesc 工单状态描述
	 * @param dealStaff 审批人ID
	 * @param dealStaffName 审批人姓名
	 * @param dealOrg 审批人所属部门ID
	 * @param dealOrgName 审批人所属部门名称
	 * @return 修改的记录数
	 */
	public int updateErrSheet(String orderId, String sheetId, String suredMsg, int statu,String statuDesc,
			Integer dealStaff, String dealStaffName, String dealOrg, String dealOrgName);
	
	/**
	 * 修改错单工单的工单类型。用于将“错误工单”改为“错判工单”
	 * @param orderId 错单受理号
	 * @param sheetId 错单工单号
	 * @param type 工单类型ID
	 * @param typeDesc 工单类型描述
	 * @return 修改的记录数
	 */
	public int updateErrSheetType(String orderId, String sheetId, int type, String typeDesc);
	
	/**
	 * 根据工单号查询工单公共信息
	 * @param sheetId	工单号
	 * @param hisFlag  true 历史 false 当前
	 * @return	工单公共信息对象
	 */
	public SheetPubInfo getSheetPubInfo(String sheetId, boolean hisFlag);
	
	/**
	 * 万荣伟 09-02-15增加按月分区地段
	 * 根据工单号和地域查询工单公共信息
	 * @param sheetId	工单号
	 *  @param region	地域
	 *  @param month	按月分区字段
	 * @param hisFlag  true当前/false历史表
	 * @return	工单公共信息对象
	 */
	public SheetPubInfo getSheetObj(String sheetId,int region, Integer month,boolean hisFlag);
	
	/**
	 * 万荣伟 09-02-15增加按月分区地段
	 * 根据工单号和地域查询工单公共信息
	 * @param sheetId	工单号
	 *  @param region	地域
	 *  @param month	按月分区字段
	 *  @param suffix	如果suffix不为空就查归档表
	 * @param hisFlag  true当前/false历史表
	 * @return	工单公共信息对象
	 */
	public SheetPubInfo getSheetObjNew(String sheetId,int region, Integer month,String suffix,boolean hisFlag);

	/**
	 * 查询工单的实际处理时限,减去夜间时差
	 * @param sheetId 工单号
	 * @param flag 是否历史	 
	 * @return
	 */
	public String getSheetObjByWorkTime(String sheetId,boolean hisFlag);

	/**
	 * 生成工单的公共信息
	 * @param sheetPubInfo 工单公共信息对像
	 * @param hisFlag  当前/历史表
	 * @return	生成的工单公共信息数量
	 */
	public int saveSheetPubInfo(SheetPubInfo sheetPubInfo);

	/**
	 * 当同一定单下的工单保存进历史表
	 * @param orderId	受理单号
	 * @return	保存的记录数
	 */
	public int saveSheetPubInfoHis(String orderId,Integer month);

	/**
	 * 生成错单
	 * @param sheetPubInfo 工单公共信息对像
	 * @param newSheetId   新生成工单号
	 * @return	保存的记录数
	 */
	public int saveErrSheet(SheetPubInfo sheetPubInfo ,String newSheetId);

	/**
	 * 更新工单状态
	 * 
	 * @param sheetId 工单号
	 * @param state 新状态的ID
	 * @param stateDesc 新状态的描述
	 * @param month 月分区标识.<br>
	 *     如果是审批单由待审批变为审批中，或者审核单由待审核变为审核中，那么该字段必须为14。<br>
     *     month为14时，lock date将被置为sysdate；month不等于14时，不改变lock date值。<br>
	 * @param lookFlag 0表示工单在工单池中，待处理；1表示工单被提取，在我的任务池；2表示工单已经完成
	 * @return true成功/false失败
	 */
	public boolean updateSheetState(String sheetId, int state, String stateDesc,Integer month,int lookFlag);
	
	/**
	 * 更新一个定单下所工单的状态
	 * @param orderId 定单号
	 * @param state	状态值
	 * @param stateDesc	状态描述
	 * @return	是否成功
	 */
	public boolean updateSheetStateByOrder(String orderId, int state, String stateDesc,int lookFlag,Integer month);

	/**
	 * 查询工单是否有在途申请单
	 * @param sheetId	工单号
	 *  @param region	地域
	 * @return 状态值
	 */
	public int getRegSheetState(String sheetId,int region);

	/**
	 * 更新取工单的提单的相关信息
	 *
	 * @param sheetId
	 *            工单号
	 * @param staffId
	 *            员工id
	 * @param staffName
	 *            员工名
	 * @param orgId
	 *            组织机构id
	 * @param orgName
	 *            组织机构名
	 * @return 更新的记录数.
	 */
	public int updateFetchSheetStaff(String sheetId, int staffId,
			String staffName, String orgId, String orgName);

	/**
	 * 更新工单的收单部门
	 *
	 * @param sheetId 工单号
	 * @param orgId   组织机构id
	 * @param orgName 组织机构名
	 * @return 更新的记录数.
	 */
	public int updateReceiveOrgBySheetId(String orgId, String orgName, String sheetId);

	public int updateFetchSheetStaffNew(String sheetId, int staffId,
			String staffName, String orgId, String orgName, String distillDate);

	/**
	 * 删除一个定单下所有的工单
	 * @param orderId	定单id
	 * @return	更新的记录数
	 */
	public int delSheetPubInfoByOrderId(String orderId,Integer month);

	/**
	 * 得到一个定单下最新生成的工单信息
	 * @param orderId	受理单id
	 * @return	工单对像
	 */
	public SheetPubInfo getTheLastSheetInfo(String orderId);

	/**
	 * 得到一个定单下最新生成的处理工单信息
	 * 
	 * @param orderId 受理单id
	 * @return 工单对像
	 */
	public SheetPubInfo getTheLastDealSheetInfo(String orderId);

	/**
	 * 当前环节流程实例
	 * @param wfNodeInstId  当前流程实例
	 * @return
	 */
	public String getCurrentTacheInstId(String wfNodeInstId);
	
	/**
	 * 更新工单的处理要求
	 * @param sheetId	工单号 
	 * @param require   要求
	 * @param regcOrgList 派单部门列表   
	 * @param dealType  操作类型   
	 * @param dealContent 操作内容 
	 * @param dealId 处理类型ID  
	 * @return	处理记录数
	 */
	public int updateSheetDealRequire(String sheetId, String require,String regcOrgList,String dealType,String dealContent,int dealId,int nextTache);	
	
	/**
	 * 更新工单的处理内容，即字段DEAL_CONTENT
	 * @param sheetId 工单ID
	 * @param dealContent 处理内容
	 * @return 处理记录数
	 */
	public int updateDealContent(String sheetId, String dealContent);
	
	/**
	 * 更新工单完成时间	
	 * @param sheetId 工单号 
	 * @return	处理记录数
	 */
	public int updateSheetFinishDate(String sheetId);	
	
	/**
	 * 根据受理单号，查询是否有待审核、审核中的审核工单
	 * @param orderId 受理单号
	 * @return 返回查询的结果。如果没有，返回null
	 */
	public SheetPubInfo getAuditSheet(String orderId);
	public SheetPubInfo getAuditSheetNew(String orderId);

	/**
	 * 部门处理保存处理内容
	 * @param sheetId
	 * @param regionId
	 * @param content
	 * @return
	 */
	public int saveDealContent(String sheetId,int regionId,String content);

	/**
	 * 根据工单号取得该工单相同的来源工单
	 * @param sheetId 本工单ID
	 * @param month 月分区
	 * @return 返回Sting 数组
	 */
	public String[] getSouresheetObj(String sheetId,Integer month);
	
	/**
	 * 查询定单流水
	 * @param orderId
	 * @param month
	 * @param boo
	 * @return
	 */
	public SheetPubInfo[] getWorksheetFlow(String orderId,Integer month,boolean boo);

	/**
	 * 查询部门审批单或审核工单的关联工单
	 * @param sheetId 工单号
	 * @param month 月分区标志
	 * @return 工单对象
	 */
	public SheetPubInfo[] getRelatSheet(String sheetId,Integer month);
	
	/**
	 * 更新环节下所有工单为完成
	 * @param orderId
	 * @param state
	 * @param stateDesc
	 * @param lookFlag
	 * @param month
	 * @return
	 */
	public int updateTachSheetFinsh(String orderId, int state,
			String stateDesc,int lookFlag,Integer month,int tachId);

	/**
	 * 工单追回，把处理工单为作废
	 * @param state 状态
	 * @param stateDesc 状态名 
	 * @param month 月分区
	 * @param tachId 环节
	 * @param where 条件
	 * @return
	 */
	public int updateDealDisannuul(int state,String stateDesc,Integer month,int tachId,String where);
	
	/**
	 * 工单通用查询,自带WHERE条件
	 * @param strWhere
	 * @param boo true 当前 false 为历史
	 * @return
	 */
	public List getSheetCondition(String strWhere,boolean boo);
	
	public List getSheetListCondition(String strWhere,boolean boo);
	
	/**
	 * 检查员工是否有处理的中工单,挂起工单除外
	 * @param strWhere
	 * @return 
	 */
	public int checkStaffSheet(String strWhere);
	
	/**
	 * 获取派单环节的数量
	 * @param strWhere
	 * @return
	 */
	public int getSheetNumOfSendTache(String strWhere);
	/**
	 * 更新工单的挂起时间
	 * @param startTime 挂起开始
	 * @param holdTime 挂起的总时间
	 * @param region 地域
	 * @param sheetId 工单号
	 * @return
	 */
	public int updateTotalHold(String angupStrTime, int sheetTotalHoldTime, int region, String sheetId);
	
	/**
	 * 更新工单的解挂时间
	 * @param startTime 解挂开始
	 * @param holdTime 挂起的总时间
	 * @param region 地域
	 * @param sheetId 工单号
	 * @return
	 */
	public int updateTotalHoldNew(int holdTime,int region,String sheetId);
	
	/**
	 * 得到定单流水最大顺序号
	 * @param orderId 定单号
	 * @param regionId 地域
	 * @return
	 */
	public int getFlowSeq(String orderId,int regionId);

	/**
	 * 根据订单号、工单类型，获取该订单下的最近生成的工单
	 * @param orderId 订单ID
	 * @param sheetType 工单类型
	 * @param mainFlag mainFlag.
	 * @author 2013-2 LiJiahui
	 * 
	 * @return 返回查询得到的结果。如果没有查询到结果，则返回null
	 */
	public SheetPubInfo getLatestSheetByType(String orderId, int sheetType, int mainFlag);

	/**
	 * 根据订单号、工单类型，获取该订单下的最近生成的工单，且NOT IN ('SYSTEM')
	 * @param orderId 订单ID
	 * @param sheetType 工单类型
	 * @param mainFlag mainFlag.
	 * @author 2013-2 LiJiahui
	 * 
	 * @return 返回查询得到的结果。如果没有查询到结果，则返回null
	 */
	public SheetPubInfo queryLastSheetNoSystemByType(String orderId, int sheetType, int mainFlag);

	/**
	 * 更改受理单号下的所有工单的地域信息
	 * @param serviceOrderId 受理单号
	 * @param newRegion 新地域ID
	 * @param newRegionName 新地域名
	 * @param oldRegion 旧地域ID
	 * @param month 月分区
	 * @return 成功修改的记录数
	 */
	public int updateRegion(String serviceOrderId, int newRegion, String newRegionName, int oldRegion, Integer month);

	/**
	 * 更改受理单号下的所有工单的地域信息
	 * @param autoVisitFlag 自动回访状态标识：0默认、1自动回访办结、2自动回访转人工、3自动回访接口异常、4未进入自动回访
	 * @param reportNum 回访原因标识：【1】0超时、3一般、5满意、6未评价，【2】1不满意，【3】2规定时间无结果、4 送自动回访失败，
	 * 					【4】1受理时直接办结、2后台派单直接办结、3部门处理直接办结（E通）、4投诉级别为申诉、5集团来单、6预定性选择转派、7地域不满足、8协办流程未结束
	 * @param homeSheet 1、预定性进入自动回访，2、终定性进入自动回访，3、定责申辩进入自动回访
	 * @param workSheetId 工单号
	 * @return 成功修改的记录数
	 */	
	public int updateAutoVisit(int autoVisitFlag, int reportNum, int homeSheet, String workSheetId);

	/**
	 * 根据受理单号查询的的
	 * @param serviceOrderId 受理单号
	 * @return
	 */
	public List queryCurDealSheetByOrderId(String orderId);

	/**
	 * 根据受理单号查询工单第一处理人，即第一个处理该工单后台派单环节的人
	 * @param serviceOrderId 受理单号
	 * @return
	 */
	public List querytFirstDealSheetByOrderId(String orderId);

	public int insertWorkSheetArea(String serviceOrderId, String workSheetId, String receiveAreaOrgId, int areaFlag);

	public int deleteWorkSheetAreaByOrderId(String serviceOrderId);

	public int deleteWorkSheetAreaBySheetId(String workSheetId);

	public int updateWorkSheetAreaTacheDate(String serviceOrderId);

	public int updateWorkSheetAreaSheetBySheetId(String newSheetId, String receiveAreaOrgId, int areaFlag, String oldSheetId);

	public int countWorkSheetAreaByOrderId(String serviceOrderId, int areaFlag);

	public int selectWorkSheetAreaBySheetId(String workSheetId);

	public String selectLastWorkSheetIdByOrderId(String serviceOrderId);

	public int deleteWorkSheetBySheetId(String workSheetId);

	public String selectSheetReceiveDate(String workSheetId);

	public int updateDealLimitTimeByOrderId(int dealLimitTime, String serviceOrderId);

	public int updateAuditLimitTimeByOrderId(int auditLimitTime, String serviceOrderId);
	
	public List complaintCodeAndLogonName(String orderId);
	
	public int saveReAssignReason(SheetReAssignReason reason);
	
	public int updateHisSheetDealContent(String sheetId, String dealContent);
	
	public SheetPubInfo getSheetObjBySourceId(int region,String sourceId,boolean hisFlag);

	public int updateLastXcSheetIdBySheetId(String lastXcSheetId, String workSheetId);

	public List selectCalloutRecByOrderId(String orderId);

	public int insertCustomerJudge(String serviceOrderId, String unifiedComplaintCode, int tacheType);

	public int deleteCustomerJudgeByOrderId(String serviceOrderId);

	public int updateCustomerJudgeStatusException(int judgeStatus, String serviceOrderId);

	public int updateCustomerJudgeStatusFromIVR(String ivrJudgeDate, String ivrDegree, String serviceOrderId);

	public int updateCustomerJudgeStatusToZDHF(String serviceOrderId);

	public int updateCustomerJudgeStatusFromZDHF(String callDegree, String serviceOrderId);

	public List selectCustomerJudgeList();

	public List selectCustomerJudgeOvertimeList();

	public Map selectCustomerJudgeByOrderId(String orderId);

	public int insertCustomerJudgeHisByOrderId(String serviceOrderId);

	public int insertXcFlow(XcFlow xcFlow);

	public int sendXcFlow(String curXcSheetId, String curReceiveOrg, String oldXcSheetId);

	public int finishXcFlow(String curXcSheetId);

	public XcFlow getXcFlowByCurXcId(String curXcSheetId);

	public XcFlow[] getXcFlowByMainId(String mainSheetId);

	public int insertXcFlowHis(String serviceOrderId);

	public List<ComplaintConnection> selectCmpBBAByStaffId(String staffId);

	public int insertComplaintConnection(ComplaintConnection cc);

	public int finishComplaintConnection(String newOrderId);

	public int insertXcPenalty(String xcSheetId, String pdSheetId, String serviceOrderId, String penaltyMoney);
}