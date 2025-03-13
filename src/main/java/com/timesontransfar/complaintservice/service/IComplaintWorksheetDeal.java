package com.timesontransfar.complaintservice.service;

import java.util.List;
import java.util.Map;

import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.timesontransfar.complaintservice.pojo.FinAssessInfo;
import com.timesontransfar.complaintservice.pojo.PreAssessInfo;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;

import net.sf.json.JSONObject;
@SuppressWarnings("rawtypes")
public interface IComplaintWorksheetDeal {
	/**
	 * 调账挂起
	 * @param workSheetId 工单号
	 * @param endDate 到期解除隐藏时间
	 * @return
	 */
	public int hangupForTZ(String workSheetId, String endDate);

	/**
	 * 隐藏工单
	 * @param workSheetId 工单号
	 * @param endDate 到期解除隐藏时间
	 * @return
	 */
	public int hiddenSheet(String workSheetId, String endDate);

	/**
	 * 手动解除隐藏
	 * @param workSheetId 工单号
	 * @return
	 */
	public int stopHide(String workSheetId);

	/**
	 * 查询最近一次挂起动作类型
	 * @param workSheetId 工单号
	 * @return 动作ID
	 */
    public int queryLastActionCodeBySheetId(String workSheetId);

	/**
	 * 调账解挂
	 * @param workSheetId 工单号
	 * @return
	 */
	public int unHangupForTZ(String workSheetId);
	
	public GridDataInfo getHiddenSheetList(String where);

	/**
	 * 手动取消隐藏
	 * @param workSheetId 工单号
	 * @return
	 */
	public int unHidden(String rows);

    public int countSheetAreaByOrderId(String serviceOrderId, int areaFlag);

	public int queryWorkSheetAreaBySheetId(String workSheetId, int sheetType);

    /**
     * 本地网处理省投诉派发的“部门处理单”时，认为派单错，将单子退回派单岗(到部门)
     * 
     * @param worksheetId
     *            当前部门处理工单ID
     * @param regionId
     *            工单地域ID
     * @param month
     *            工单月分区
     * @param backReason
     *            退单原因
     * @return 处理结果
     */
    public String sumbitOrgBack(String worksheetId, int regionId, int month, String backReason);

	public boolean checkLastDeal(String sheetId, int sheetType, int curMonth);

    /**
     * 判断是否能够直接生成预定性工单
     */
    public boolean checkYDXCreate(String curSheetId, int sheetType, String rcvOrgId, int curMonth);

    /**
     * 模拟生成预定性单，判断是否权限查看工单池，返回工单池预定性单的数量
     */
    public int checkYDXAuthority(String servOrderId);

    /**
     * 部门处理提交
     * 
     * @param sheetPubInfo
     *            工单信息
     * @param tsdealQualitative
     *            处理定性结果（回单处理工单或审批工单需填写该信息）
     * @param delalId
     *            处理定性ID
     * @param dealName
     *            处理定性描述（一般处理、特殊处理、重大隐患）
     * @return 处理结果
     */
    public String sumbitOrgDeal(SheetPubInfo sheetPubInfo, int delalId, String dealName, String batchFinish);

    /**
     * 预定性处理完成提交
     */
	public String submitPreAssess(JSONObject requestJson, PreAssessInfo preInfo);

    /**
     * 终定性处理完成提交
     * 
     * @param qualitative
     *            终定性的定性结果
     * @param customerVisit
     *            回访客户的结果
     * @param regionId
     *            工单地域ID
     * @param month
     *            工单月分区
     * @param dealContent
     *            定性意见
     * @return 处理结果
     */
    public String submitFinAssess(FinAssessInfo info);

	public void submitFinAssessFromVisit(String sheetId, String orderId, int region, Integer month);

	public void toRGHFFromVisit(String sheetId, String orderId, int region, Integer month);

	public String submitRGHF(TScustomerVisit tscustomerVisit, String worksheetId, int regionId, int month);
    /**
     * 终定性环节，将工单挂起
     * 
     * @param bean
     *            终定性的定性结果
     * @param tscustomerVisit
     *            回访记录           
     * @param dealContent
     *            定性意见
     * @return UPDATED 挂起状态下，修改定性内容成功；STATUERROR 不在待处理/处理中，不能挂起；<br>
     *         HANGING 挂起失败，因为工单已经挂起；SUCCESS 挂起成功
     */
    public String hangupAssess(TsSheetQualitative bean, TScustomerVisit tscustomerVisit, String dealContent);

    /**
     * 终定性环节，重新派发工单
     * 
     * @param worksheetId
     *            工单ID
     * @param main
     *            主办部门或个人信息
     * @param sub
     *            协办部门或个人信息
     * @param dealContent
     *            派发意见
     * @param dealLimit
     *            处理时限
     * @return 处理结果
     */
    public String dispatchAssess(String worksheetId, String[] main, String[] sub,
            String dealContent, int dealLimit, int upgradeIncline);

    /**
     * 根据订单ID查询最近一条定性记录，含责任部门一级目录、定性情况描述
     * 
     * @param orderID
     *            订单ID
     * @param regionId
     *            订单地域ID
     * @return 查询结果
     */
    public Map getLatestQualitative(String orderID, int regionId);

    public int saveUnsatisfyTemplate(String reason, String template, int colOrder);
    public int delUnsatisfyTemplate(String unsatisfyId);
    public int modifyUnsatisfyTemplate(String reason, String template, int colOrder, String unsatisfyId);
    public List queryUnsatisfyTemplate();
    /**
     * 根据订单ID，得到终定性时的默认处理差错部门
     * 
     * @param orderId
     *            订单ID
     * @return 查询结果
     */
    public String getDefaultMistakeOrg(String orderId);

    /**
     * 设置是否多次联系不上标识
     * @param orderId
     * @return
     */
	public String setUnifiedContact(String orderId, String sheetId, String contactStatus, String contactType);

    /**
     * 设置重单标识
     * @param orderId
     * @return
     */
	public String setUnifiedRepeat(String orderId, String sheetId, String ucc, String repeatType);

	/**
	 * 查找最近重单受理单号
	 * @param orderId
	 * @return
	 */
	public String getJTSSCode(String orderId);

	/**
	 * 校验重单受理单号是否存在当前表
	 * @param uccJTSS
	 * @return
	 */
	public boolean checkJTSSCode(String uccJTSS);

    /**
     * 校验集团编码是否存在当前表
     * @param ucc
     * @return
     */
	public boolean checkUnifiedComplaintCode(String ucc);

	public List<ComplaintUnifiedReturn> queryUnifiedReturnByWhere(String where);

	public List complaintCodeAndLogonName(String orderId);
	
	/**
	 * 预定性、终定性环节保存回单质量评价
	 * @param 工单号、环节单号、环节ID、操作时间、两级评价ID
	 * @return int
	 */
	public int saveReceiptEval(JSONObject ins);
}