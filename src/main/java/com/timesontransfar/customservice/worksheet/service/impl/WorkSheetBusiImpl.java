/**
 * <p>类名：WorkSheetBusiImpl.java</p>
 * <p>功能描叙：工单业务功能实现</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：增加回访信息查询　getVisiResult 万荣伟 2008/06/05</p>
 * <p>  Create by:	lifeng	Mar 20, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.timesontransfar.customservice.worksheet.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.dapd.pojo.DapdSheetInfo;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.workflow.IWorkFlowAttemper;
import com.timesontransfar.complaintservice.handler.ComplaintDealHandler;
import com.timesontransfar.customservice.businessOpportunity.dao.BusinessOpportunityDao;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.worksheet.dao.ISJSheetQualitative;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetTodispatchDao;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.dao.IworkSheetAllotRealDao;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedReturnDAOImpl;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.NoteSendList;
import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.ServiceWorkSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetTodispatch;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetDealType;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetStatuApplyInfo;
import com.timesontransfar.customservice.worksheet.service.IWorkSheetFlowService;
import com.timesontransfar.customservice.worksheet.service.IworkFlowBusiServ;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.timesontransfar.dapd.dao.IdapdSheetInfoDao;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Component("workSheetBusi")
public class WorkSheetBusiImpl implements IworkSheetBusi {
	private static final Logger logger = LoggerFactory.getLogger(WorkSheetBusiImpl.class);
    @Autowired
	private PubFunc pubFunc;
    @Autowired
	private IorderAskInfoDao orderAskInfoDao ; //orderAskInfoDao
    @Autowired
	private IorderCustInfoDao orderCustInfoDao; //orderCustInfoDao
    @Autowired
	private IserviceContentDao servContentDao;//serviceContentDao
    @Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
    @Autowired
	private ISheetActionInfoDao sheetActionInfoDao;//sheetActionInfoDao
    @Autowired
	private IworkSheetAllotRealDao workSheetAlllot;//
    @Autowired
	private InoteSenList noteSen;//
	//工作流bean
    @Autowired
	private IWorkFlowAttemper WorkFlowAttemper__FACADE__;//
	@Autowired
	private IWorkSheetFlowService workSheetFlowService;//要用这个提交
	@Autowired
	private ItsWorkSheetDao tsWorkSheetDaoImpl;//
	@Autowired
	private IworkFlowBusiServ workFlowBusiServImpl;
	@Autowired
	private ISheetTodispatchDao sheetTodispatchDaoImpl;//
	@Autowired
	private BusinessOpportunityDao businessOpportunityDao;//

    /**
     * 工单分派操作的实例
     */
    @Autowired
    private WorkSheetAllot workSheetAllot;//
    @Autowired
	private ISJSheetQualitative sjSheetQualitativeDaoImpl;//
    @Autowired
    private ILabelManageDAO labelManageDAO;
    @Autowired
	private CmpUnifiedReturnDAOImpl cmpUnifiedReturnDAOImpl;
	@Autowired
	private IdapdSheetInfoDao dapdDao;
	
	/**
	 * 退单给受理人发短信
	 * 
	 * @return
	 */
	public void sendNoteToAccept(int askStaffId, int regionId, String workSheetId, String sendContent) {
		TsmStaff staff = this.pubFunc.getStaff(askStaffId);
		if (isMobile(staff.getRelaPhone())) {
			NoteSeand noteBean = new NoteSeand();
			noteBean.setSheetGuid(this.pubFunc.crtGuid());
			noteBean.setRegionId(regionId);
			noteBean.setDestteRmid(staff.getRelaPhone());
			noteBean.setClientType(1);
			noteBean.setSendContent(sendContent);
			noteBean.setOrgId(staff.getOrganizationId());
			noteBean.setOrgName(staff.getOrgName());
			noteBean.setStaffId(askStaffId);
			noteBean.setStaffName(staff.getName());
			noteBean.setBusiId(workSheetId);
			this.noteSen.saveNoteContent(noteBean);
		}
	}

	/**
	 * 手机号验证
	 * 
	 * @param str
	 * @return 验证通过返回true
	 */
	private boolean isMobile(String str) {
		Pattern p = null;
		Matcher m = null;
		boolean b = false;
		p = Pattern.compile("^[1][0-9]{10}$"); // 验证手机号
		m = p.matcher(str);
		b = m.matches();
		return b;
	}

	/**
	 * 取流向派发部门、流向部门、流向到达时间
	 * @param orderId
	 * @return
	 */
	public List getReturnTime(String orderId) {
		return sheetPubInfoDao.getReturnTime(orderId);
	}

	/**
	 * 得到商机定性对象
	 * 
	 * @param workSheetId 工单号
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public SJSheetQualitative getSJSheetQualitativeFlow(String workSheetId, boolean boo) {
		return this.sjSheetQualitativeDaoImpl.getSJSheetQualitative(workSheetId, boo);
	}

	/**
	 * 得到部门审批单或审核工单的关联工单
	 * @param sheetId 工单号
	 * @param month 月分区标志
	 * @return 工单对象
	 */
	public SheetPubInfo[] getRelatSheet(String sheetId,Integer month) {
		return this.sheetPubInfoDao.getRelatSheet(sheetId, month);
		
	}
	/**
	 * 从工单池提取工单方法
	 * @param sheetId
	 * @return	取回的工单数
	 */
	public String fetchWorkSheet(String sheetId,int regionId,Integer month) {
		boolean hisFlag = true;
		String[] strs = sheetId.split("@");
		int strNum = strs.length;
		sheetId = strs[0];
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId,regionId,month, hisFlag);
		if(sheetPubInfo == null) {
			logger.error("未查询到工单号:{}",sheetId);
			return "ERROR";
		}
		int sheetState = sheetPubInfo.getLockFlag();
		if(ComplaintDealHandler.isHoldSheet(sheetPubInfo.getSheetStatu())) {//工单池挂起的工单不能直接提取
			return "WKST_HOLD_STATE";
		}
		if (sheetState == 0 && sheetPubInfo.getSheetStatu() != StaticData.WKST_ALLOT_STATE) {				
			TsmStaff staff = null;
			if (strNum > 1) {//企业信息化部ITSM接口取登录员工信息
				staff = this.pubFunc.getLogonStaffByLoginName(strs[1]);
			} else {
				//取当前登录员工信息
				staff = this.pubFunc.getLogonStaff();// 取当前登录员工信息
			}
			int staffId = Integer.parseInt(staff.getId());
			String staffName = staff.getName();
			String orgId = staff.getOrganizationId();
			String orgName = staff.getOrgName();
			int num = 0;
			if (sheetPubInfo.getSheetType() != StaticData.SHEET_TYPE_TS_AUIT) {
			//  由南京客服中心的员工提单计算工作量改为全省适用
				num = workSheetAllot.countWorkload(sheetPubInfo.getSheetType(), sheetPubInfo.getSheetStatu(), sheetPubInfo.getRetStaffId(),
			        staffId, orgId, 1);
			}
			// 更新提单员工信息及工单状态
			this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, staffId,
					staffName, orgId, orgName);
			//得到要更新的状态
			int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());			
			String stateDesc = pubFunc
					.getStaticName(sheetStatu);			
			this.sheetPubInfoDao.updateSheetState(sheetId,
					sheetStatu, stateDesc,sheetPubInfo.getMonth(),1);
			
			// 记录工单动作			
			SheetActionInfo sheetActionInfo = new SheetActionInfo();
			String guid = pubFunc.crtGuid();
			int tacheId = sheetPubInfo.getTacheId();
			sheetActionInfo.setWorkSheetId(sheetId);
			sheetActionInfo.setComments("提取工单"+",工作量为"+num);
			sheetActionInfo.setRegionId(sheetPubInfo.getRegionId());
			sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
			sheetActionInfo.setMonth(sheetPubInfo.getMonth());
			sheetActionInfo.setActionGuid(guid);
			sheetActionInfo.setTacheId(tacheId);
			sheetActionInfo.setTacheName(pubFunc.getStaticName(tacheId));
			sheetActionInfo.setActionCode(StaticData.WKST_FETCH_ACTION);
			sheetActionInfo.setActionName(pubFunc.getStaticName(StaticData.WKST_FETCH_ACTION));
			sheetActionInfo.setOpraOrgId(orgId);
			sheetActionInfo.setOpraOrgName(orgName);
			sheetActionInfo.setOpraStaffId(staffId);
			sheetActionInfo.setOpraStaffName(staffName);			
			this.sheetActionInfoDao.saveSheetActionInfo(sheetActionInfo);
			return "SUCCESS";
		}

		return "ERROR";
	}

	/**
	 * 释放工单到工单池中
	 * @param sheetId
	 *            工单id
	 * @return 释放的工单数量
	 */
	public String releaseWorkSheet(String sheetId,int regionId,Integer month) {
		return this.releaseWorkSheetNew(sheetId, regionId, month, 0);
	}
	/**
	 * 释放工单到工单池中新方法
	 * @param sheetId	工单id
	 * @param falg 0必须经过申请，1为不经过申请
	 * @return	释放的工单数量
	 */
	public String releaseWorkSheetNew(String sheetId,int regionId,Integer month,int falg) {
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,regionId,month,hisFlag);
		int sheetState = sheetPubInfo.getLockFlag();
		// 释放工单
		//挂起前必须做审批,审批为3,原来为1
		if(falg == 0 && sheetState != 3) {
			return "ERROR";
		}
		if(falg == 1 && sheetState != 1) {
			return "ERROR";
		}
		
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(sheetPubInfo.getServiceOrderId(), false);
		if (StaticData.OR_AUTOVISIT_STATU == orderAskInfo.getOrderStatu()) {
			return "ERROR";
		}
		
		// 更新提单员工信息及工单状态
		this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, 0, "", "", "");
		
		int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 0, sheetPubInfo.getSheetType());
		
		String stateDesc = pubFunc
				.getStaticName(sheetStatu);
		this.sheetPubInfoDao.updateSheetState(sheetId,
				sheetStatu, stateDesc,sheetPubInfo.getMonth(),0);
		
		// 记录工单动作
		SheetActionInfo sheetActionInfo = new SheetActionInfo();
		sheetActionInfo.setWorkSheetId(sheetId);
		int tacheId = sheetPubInfo.getTacheId();
		sheetActionInfo.setComments("释放工单");
		sheetActionInfo.setRegionId(sheetPubInfo.getRegionId());
		sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
		sheetActionInfo.setMonth(sheetPubInfo.getMonth());
		saveSheetDealAction(sheetActionInfo,tacheId,StaticData.WKST_RELEASE_ACTION,4);
		return "success";
	}
	/**
	 * 工单挂起,释放申请
	 * @param sheetId 工单号
	 * @param region 地域
	 * @param month 月分区
	 * @param applyReason 申请原因
	 * @param applyType 申请类型
	 * @return
	 */
	public String workSheetStatuApply(String sheetId,int region,Integer month,String applyReason,int applyType) {
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,region,month, hisFlag);
		int sheetState = sheetPubInfo.getLockFlag();
		if(sheetState != 1) {
			return "STATUERROR";//工单状态不是处理中
		}
		//判断是否有为审批完的单子
		int size = this.sheetPubInfoDao.getRegSheetState(sheetId, region);
		if(size != 0) {
			return "APPLYERROR";//该工单已有申请单
		}
		//判断工单是否挂起,如果作释放,必须先解挂工单
		if(sheetPubInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "WKST_HOLD_STATE";//该工单已挂起
		}		
		int statu=0;
		if(applyType == 0) {//挂起
			statu = StaticData.WKST_HOLD_APPLY_STATE;
		}
		if(applyType == 1) {
			statu = StaticData.WKST_DEAL_APPLY_STATE;
		}
		if(applyType == 5) {//交办
			statu = StaticData.WKST_DEAL_ASSIGN_STATE;
		}
		String statuDesc = this.pubFunc.getStaticName(statu);
		//更新工单状态
		this.sheetPubInfoDao.updateSheetState(sheetId,
				statu, statuDesc,sheetPubInfo.getMonth(),3);
		//取当前登录员工信息
		TsmStaff staff = this.pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String orgId = staff.getOrganizationId();
		String orgName = staff.getOrgName();		
		//记录申请
		String applyGuid = this.pubFunc.crtGuid();
		WorkSheetStatuApplyInfo applyBean = new WorkSheetStatuApplyInfo();
		applyBean.setApplyGuid(applyGuid);
		applyBean.setOrderId(sheetPubInfo.getServiceOrderId());
		applyBean.setSheetId(sheetId);
		applyBean.setApplyOrg(orgId);
		applyBean.setApplyOrgName(orgName);
		applyBean.setApplyStaff(staffId);
		applyBean.setApplyStaffName(staffName);
		applyBean.setApplyReason(applyReason);
		applyBean.setApplyStatu(0);
		applyBean.setMonth(month);
		applyBean.setApplyType(applyType);
		this.tsWorkSheetDaoImpl.saveSheetApply(applyBean, true);
		return "success";
	}
	/**
	 * 审批工单申请的挂起和释放audWorkSheetApply
	 * @applyGuid 申请唯一ID
	 * @param sheetId 工单号
	 * @param region 地域编码
	 * @param month //月分区
	 * @param audResult 审批结果
	 * @param applyAudStatu 审批状态 1为同意 2 为不同意
	 * @param applyType 申请类型
	 * @return
	 */
	public String workSheetAudApply(String applyGuid, String sheetId, int region, 
			Integer month, String audResult, int applyAudStatu, int applyType) {
		String strWhere = " AND CC_SHEET_STATU_APPLY.APPLY_GUID='"+applyGuid+"' AND CC_SHEET_STATU_APPLY.WORKSHEET_ID='"+sheetId+"' "
				+ "AND CC_SHEET_STATU_APPLY.APPLY_AUD_STATU=0";
		WorkSheetStatuApplyInfo[] applyBean = this.tsWorkSheetDaoImpl.getsheetApplyObj(strWhere, true);
		if(applyBean.length == 0) {
			return "APPLYERROR";//该申请对象不存在
		}
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,region,month, hisFlag);
		
		//取当前登录员工信息
		TsmStaff staff = this.pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String orgId = staff.getOrganizationId();
		String orgName = staff.getOrgName();
		WorkSheetStatuApplyInfo bean = applyBean[0];
		//记录审批员工
		bean.setAudOrg(orgId);
		bean.setAudOrgName(orgName);
		bean.setAudStaff(staffId);
		bean.setAudStaffName(staffName);
		
		if(applyAudStatu == 1) {//审批同意
			audResult = "同意";
			bean.setApplyStatu(1);
			bean.setAudResult(audResult);
			if(applyType == 0) {
				//调用挂起方法
				holdWorkSheet(sheetId,bean.getApplyReason(),region,month,0);
			}
			if(applyType == 1) {
				//调用释放方法
				releaseWorkSheet(sheetId,region,month);
			}			
			
		}
		if(applyAudStatu == 2) {//审批不同意
			bean.setApplyStatu(2);
			bean.setAudResult(audResult);
			int sheetStatu = 0;
			String stateDesc =" ";
			//更新工单状态为处理中
			sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());
			stateDesc = pubFunc.getStaticName(sheetStatu);				
			this.sheetPubInfoDao.updateSheetState(sheetId,sheetStatu, stateDesc,sheetPubInfo.getMonth(),1);
		}
		this.tsWorkSheetDaoImpl.updateSheetApply(bean);
		return "SUCCESS";
	}

	/**
	 * 我的任务工单池挂起工单
	 * @param sheetId 工单id	 
	 * @param comments 操作备注
	 * @param flag 0为必须进行申请才能挂起,1为直接挂起
	 * @return	挂起工单数量
	 */
	public int holdWorkSheet(String sheetId, String comments,int region,Integer month,int flag) {
		return this.holdWorkSheetNew(sheetId, comments, region, month, flag,1);
	}
	/**
	 * 部门非工作时间自动挂起工单
	 * @param sheetId 工单id	 
	 * @param comments 操作备注
	 * @param flag 0为必须进行申请才能挂起,1为直接挂起
	 * @return	挂起工单数量
	 */
	public int holdWorkSheetPub(SheetPubInfo sheetPubInfo) {
		return this.holdWorkSheetNew(sheetPubInfo.getWorkSheetId(), "系统自动挂起", sheetPubInfo.getRegionId(),
				sheetPubInfo.getMonth(), 1,0);
	}
	/**
	 * 挂起工单
	 * @param sheetId 工单id	 
	 * @param comments 操作备注
	 * @param flag 0为必须进行申请才能挂起,1为直接挂起
	 * @lookFlag 1为我的任务，0为工单池
	 * @return	挂起工单数量
	 */
	public int holdWorkSheetNew(String sheetId, String comments,int region,Integer month,int flag,int lookFlag) {
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,region,month, hisFlag);
		int state = sheetPubInfo.getLockFlag();
		//挂起前必须做审批,审批为3
		if(state != 3 && flag == 0) {//为可以直接挂起
			return 0;
		}
		
		// 更新工单状态为挂起
		String stateDesc = pubFunc.getStaticName(StaticData.WKST_HOLD_STATE);
		this.sheetPubInfoDao.updateSheetState(sheetId,
				StaticData.WKST_HOLD_STATE, stateDesc,sheetPubInfo.getMonth(),lookFlag);
		
		// 更受理单下挂起工单的数量和时间等信息
		String orderId = sheetPubInfo.getServiceOrderId();
		OrderAskInfo orderAskInfo = this.orderAskInfoDao
				.getOrderAskInfo(orderId, false);

		int subSheetHoldNum = orderAskInfo.getSubSheetCount();// 挂单数量
		String firstSheetHoldTime = orderAskInfo.getHangStartTime();// 首张单挂起始时间
		int totalHoldTime = orderAskInfo.getHangTimeSum();// 挂单总时间

		// 为同一张定单下第一张挂起的工单
		if (subSheetHoldNum == 0) {
			this.orderAskInfoDao.updateSubSheetHoldInfo(orderId,
					subSheetHoldNum + 1, pubFunc.getSysDate(), totalHoldTime);
		}
		else {
			this.orderAskInfoDao.updateSubSheetHoldInfo(orderId,
					subSheetHoldNum + 1, firstSheetHoldTime, totalHoldTime);
		}
		//更新工单的挂起时间
		int sheetTotalHoldTime = sheetPubInfo.getHangupTimeSum();
		String angupStrTime = pubFunc.getSysDate();
		this.sheetPubInfoDao.updateTotalHold(angupStrTime, sheetTotalHoldTime, region, sheetId);
		// 记录挂动作信息
		SheetActionInfo sheetAction = new SheetActionInfo();
		sheetAction.setWorkSheetId(sheetId);				
		sheetAction.setRegionId(sheetPubInfo.getRegionId());
		sheetAction.setServOrderId(sheetPubInfo.getServiceOrderId());
		sheetAction.setComments(comments);
		sheetAction.setMonth(sheetPubInfo.getMonth());
		saveSheetDealAction(sheetAction,sheetPubInfo.getTacheId(),StaticData.WKST_HOLD_ACTION,5);
		return 1;		
	}
	/**
	 * 我的任务工单池解挂工单
	 * @param sheetId 工单id	
	 * @return	解挂工单数量
	 */
	public int unHoldWorkSheet(String sheetId,int region,Integer month) {
		return this.unHoldWorkSheetNew(sheetId, region, month,1);
	}
	/**
	 * 工单池中挂起工单批量解挂
	 * @param bean
	 * @return
	 */
	public int unHoldWorkSheetPub(SheetPubInfo[] bean) {
		if(bean == null) {
			return 0;
		}
		int size = bean.length;
		int cont=0;
		for(int i=0;i<size;i++) {
			cont +=this.unHoldWorkSheetNew(bean[i].getWorkSheetId(), bean[i].getRegionId(), bean[i].getMonth(),0);
		}
		return cont;
	}
	/**
	 * 部门处理时解挂工单
	 * @param sheetId 工单id	
	 * @param lookFlag 1为我的任务，0为工单池
	 * @return	解挂工单数量
	 */	
	public int unHoldWorkSheetNew(String sheetId,int region,Integer month,int lookFlag) {
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,region,month, hisFlag);
		int state = sheetPubInfo.getSheetStatu();
		// 非挂起状态的单子不能解挂
		if (state != StaticData.WKST_HOLD_STATE) {
			return 0;
		}

		// 更新工单状态为处理中
		int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), lookFlag, sheetPubInfo.getSheetType());
		String stateDesc = pubFunc.getStaticName(sheetStatu);
		this.sheetPubInfoDao.updateSheetState(sheetId,
				sheetStatu, stateDesc,sheetPubInfo.getMonth(),lookFlag);
		// 更受理单下挂起工单的数量和时间等信息
		String orderId = sheetPubInfo.getServiceOrderId();
		OrderAskInfo orderAskInfo = this.orderAskInfoDao
				.getOrderAskInfo(orderId, false);
		int subSheetHoldNum = orderAskInfo.getSubSheetCount();// 挂单数量
		String firstSheetHoldTime = orderAskInfo.getHangStartTime();// 首张单挂起始时间
		firstSheetHoldTime = PubFunc.dbDateToStr(firstSheetHoldTime);
		int totalHoldTime = orderAskInfo.getHangTimeSum();// 挂单总时间 (分)
		// 最后一张挂起的工单解挂时
		if (subSheetHoldNum - 1 == 0) {
			String sysdate = pubFunc.getSysDate();
			// 时间差标识，1、标识小时差；2标识分钟差；3、标识秒时间差；4、标识毫秒时间差
			int timeBetweenFlag =2;
			int holdTime = (int)PubFunc.getTimeBetween(firstSheetHoldTime,sysdate,timeBetweenFlag);
			this.orderAskInfoDao.updateSubSheetHoldInfo(orderId,
					subSheetHoldNum - 1, "", totalHoldTime + holdTime);
		}
		else {
			this.orderAskInfoDao.updateSubSheetHoldInfo(orderId,
					subSheetHoldNum - 1, firstSheetHoldTime, totalHoldTime);
		}
		
		String startTime = sheetPubInfo.getHangupStrTime();
		String sheetSysdate = pubFunc.getSysDate();
		// 时间差标识，1、标识小时差；2标识分钟差；3、标识秒时间差；4、标识毫秒时间差
		int timeFlag =2;
		int sheetHoldTime = 0;
		if(startTime != null) {
			sheetHoldTime =	(int)PubFunc.getTimeBetween(startTime,sheetSysdate,timeFlag);
		}
			
		
		int sumTotalTime = sheetPubInfo.getHangupTimeSum()+sheetHoldTime;
		this.sheetPubInfoDao.updateTotalHoldNew(sumTotalTime, region, sheetId);

		// 记录挂动作信息
		SheetActionInfo sheetAction = new SheetActionInfo();	
		sheetAction.setWorkSheetId(sheetId);
		sheetAction.setComments("解挂工单");
		sheetAction.setRegionId(sheetPubInfo.getRegionId());
		sheetAction.setServOrderId(sheetPubInfo.getServiceOrderId());
		sheetAction.setMonth(sheetPubInfo.getMonth());
		saveSheetDealAction(sheetAction,sheetPubInfo.getTacheId(),StaticData.WKST_UNHOLD_ACTION,6);
		return 1;		
	}

	/**
	 * 从工单池批量取回工单
	 * 
	 * @param sheetIdList 格式为 sheetId@regionId@month
	 *            工单号字符数组
	 * @param flowType 流程号
	 * @param fetchType 提取方式，0为系统自动提取，1为人工选择提取
	 * @return 取单结果
	 */
	public String fetchBatchWorkSheet(String[] sheetIdList, int flowType, int fetchType){
		//取当前登录员工信息
	    TsmStaff staff = this.pubFunc.getLogonStaff();
	    int staffId = Integer.parseInt(staff.getId());
	    String orgId = staff.getOrganizationId();
	    
	    //新投诉处理 苏州 限制提单数量为10条
	    if(flowType == 3 && this.pubFunc.getOrgRegion(orgId) == StaticData.REGION_ID){
	        return this.fetchCmpSheetsBySuZhouOrg(sheetIdList, staff);
		}
	    
	    //判断商机、预受理（flowType == 1）是否有正在处理工单
	    String judgeStr = this.judgeNotCmpSheet(flowType, fetchType, staffId, orgId);
	    if(StringUtils.isNotBlank(judgeStr)) {
	    	return judgeStr;
	    }
	    
		int size = sheetIdList.length;
		int count = 0;
		StringBuilder deleSheetStr = new StringBuilder("");//返回被提取工单 格式为 sheetId@sheetId
		for(int i=0; i<size; i++){
			String strSheet = sheetIdList[i];
			String[] fetchWorkSheet = strSheet.split("@");
			String sheetId = fetchWorkSheet[0];
			int region = Integer.parseInt(fetchWorkSheet[1]);
			Integer month = Integer.valueOf(fetchWorkSheet[2]);
			String fetchInfo = this.fetchWorkSheet(sheetId, region, month);
			if(fetchInfo.equals("SUCCESS")) {
				if(count == 0) {
					deleSheetStr.append(sheetId);
				} else {
					deleSheetStr.append("@"+sheetId);
				}
				count++;
			}
			if(count == 10 && fetchType == 0){//批量提取限制
				break;
			}
		}
		return deleSheetStr.toString();
	}
	
	private String judgeNotCmpSheet(int flowType, int fetchType, int staffId, String orgId) {
		if(flowType == 1) {//商机、预受理
			String strWhere = " AND W.DEAL_ORG_ID='" + orgId + "' " + "AND W.DEAL_STAFF='" + staffId + "' AND A.SERVICE_DATE='1'";
			if(fetchType == 2) {//用作预受理
				strWhere += " AND A.SERVICE_TYPE='700001171'";
			}else if(fetchType == 3) {//用作商机
				strWhere += " AND A.SERVICE_TYPE='600000074'";
			}
			int dealNum = this.sheetPubInfoDao.checkStaffSheet(strWhere);
			
			//dealNum = 0 跳过验证有处理中工单
			if(dealNum > 0){
				logger.warn("员工ID为: {} 的员工有处理中的工单", staffId);
				return "DEALSHEET";//有处理的工单不能提取工单
			}
		}
		return null;
	}
	
	private String fetchCmpSheetsBySuZhouOrg(String[] sheetIdList, TsmStaff staff) {
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
	    String orgId = staff.getOrganizationId();
	    String orgName = staff.getOrgName();
	    
		String strWhere = "AND W.DEAL_ORG_ID='" + orgId + "' AND W.DEAL_STAFF='" + staffId + "' AND A.SERVICE_DATE='3'";
        //当前派单环节工单数量
        int numOfSendTache = this.sheetPubInfoDao.getSheetNumOfSendTache(strWhere);
        
        int size = sheetIdList.length;
        String deleSheet = "";//返回被提取工单 格式为 sheetId@sheetId
        for(int i=0; i < size; i++){
            String strSheet = sheetIdList[i];
            String[] fetchWorkSheet = strSheet.split("@");
            String sheetId = fetchWorkSheet[0];
            int region = Integer.parseInt(fetchWorkSheet[1]);
            Integer month = Integer.valueOf(fetchWorkSheet[2]);
			SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId, region, month, true); 
			if(sheetPubInfo==null){
				continue;
			}  
			if(ComplaintDealHandler.isHoldSheet(sheetPubInfo.getSheetStatu())) {//工单池挂起的工单不能直接提取
				continue;
			}
			if(this.judgeSheetWaitDealing(sheetPubInfo.getLockFlag(), sheetPubInfo.getSheetStatu())){//是否工单池
				//派单环节工单限定为10条 总数不限
				if(sheetPubInfo.getTacheId() == StaticData.TACHE_ASSIGN && numOfSendTache>=10){
					continue;
				}else if(sheetPubInfo.getTacheId() == StaticData.TACHE_ASSIGN) {
					numOfSendTache++;
				}
				//由南京客服中心的员工提单计算工作量改为全省适用
				int num = workSheetAllot.countWorkload(sheetPubInfo.getSheetType(), sheetPubInfo.getSheetStatu(), sheetPubInfo.getRetStaffId(),
				        staffId, orgId, 1);
				//更新提单员工信息及工单状态
				this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, staffId,
						staffName, orgId, orgName);
				//得到要更新的状态
				int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());
				String stateDesc = pubFunc.getStaticName(sheetStatu);
				this.sheetPubInfoDao.updateSheetState(sheetId,
						sheetStatu, stateDesc,sheetPubInfo.getMonth(),1);
				
				// 记录工单动作			
				SheetActionInfo sheetActionInfo = new SheetActionInfo();
				sheetActionInfo.setWorkSheetId(sheetId);
				int tacheId = sheetPubInfo.getTacheId();
				sheetActionInfo.setComments("批量提取工单" +",工作量为"+num);
				sheetActionInfo.setRegionId(sheetPubInfo.getRegionId());
				sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
				sheetActionInfo.setMonth(sheetPubInfo.getMonth());
				saveSheetDealAction(sheetActionInfo,tacheId,StaticData.WKST_FETCH_ACTION,3);
				
				//记录返回信息
				deleSheet = deleSheet.length()<=0 ? (sheetId) : (deleSheet+"@"+sheetId);
			}
		}
		return deleSheet;
	}
	
	/**
	 * 判断是否工单池工单
	 */
	private boolean judgeSheetWaitDealing(int lockFlag, int sheetStatus) {
		return lockFlag == 0 && sheetStatus != StaticData.WKST_ALLOT_STATE;
	}

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
			   Integer[] month,String audResult,int applyAudStatu,int applyType){
		int size = guidList.length;
		int count = 0;
		for(int i=0; i<size; i++){
			count += this.workSheetAudApply(guidList[i],sheetList[i],regionList[i],month[i],
					audResult,applyAudStatu,applyType).equals("SUCCESS")?1:0;
		}
		return count;
	}

	/**
	 * 根据工单号查询工单完整信息
	 * @param sheetId 工单号
	 * @return	工单对象
	 */
	public WorkSheetInfo getWorkSheetInfo(String sheetId,int region,Integer month){
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId, region, month, hisFlag);
		if(sheetPubInfo == null){
			return null;
		}

		String orderId = sheetPubInfo.getServiceOrderId();
		WorkSheetInfo workSheetInfo = new WorkSheetInfo();
		//工单公共信息
		workSheetInfo.setSheetPubInfo(sheetPubInfo);
		//受理信息
		OrderAskInfo orderAskInfo = this.orderAskInfoDao.getOrderAskObj(orderId, month, false);
		if(null == orderAskInfo){
		    return null;
		}
		workSheetInfo.setOrderAskInfo(orderAskInfo);
		//客户信息
		String custGuid = orderAskInfo.getCustId();
		OrderCustomerInfo custInfo = this.orderCustInfoDao.getOrderCustByGuid(custGuid,false);
		if(custInfo != null){
			workSheetInfo.setCustInfo(custInfo);
		}
		//受理内容
		ServiceContent servContent = this.servContentDao.getServContentByOrderId(orderId,false,0);
		if(servContent != null){
			workSheetInfo.setServContent(servContent);
		}

		return workSheetInfo;
	}
	
	
	/**
	 * 将派单工单退回受量台
	 * 
	 * @param sheetPubInfo
	 *            工单对象
	 * @return 是否成功
	 */
	public boolean assignBackToAsk(SheetPubInfo sheetPubInfo) {
		String sheetId = sheetPubInfo.getWorkSheetId();		
		String dealRequire = sheetPubInfo.getDealRequire();
		int quryRegion = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,sheetPubInfo.getRegionId(),month,hisFlag);
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;
		
		TsmStaff staff = pubFunc.getLogonStaff();
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();
		
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE", dealRequire);//作为退单要求传到后台
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_ASSIGN_TO_ASK);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId, " "," ","退单",dealRequire,2,StaticData.TACHE_ORDER_ASK);
		return this.submitWorkFlow(sheetId,quryRegion,otherParam);
	}
	
	/**
	 * 分拣退回到前台受理
	 * (苏州业务受理)
	 * 
	 * @param sheetPubInfo
	 *            工单对象
	 * @return success
	 */
	public String doPickToAskBack(SheetPubInfo sheetPubInfo,Map otherInfo) {
		String sheetId = sheetPubInfo.getWorkSheetId();		
		String dealRequire = sheetPubInfo.getDealRequire();
		boolean hisFlag = false;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetId, hisFlag );
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;
		
		TsmStaff staff = pubFunc.getLogonStaff();
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();
		
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE", dealRequire);//作为退单要求传到后台
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_PICK_TO_ASK);
		otherParam.put("MONTH_FALG",sheetInfo.getMonth());
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId, " "," ","退单",dealRequire,2,StaticData.TACHE_ORDER_ASK);
		this.submitWorkFlow(sheetId,sheetInfo.getRegionId(),otherParam);
		
		return "SUCCESS";
	}

	/**
	 * 派单台人工派单方法
	 * @param sheetPubInfo  工单对象
	 * @return 是否成功
	 */
	public String assignSheetToDeal(SheetPubInfo[] sheetInfoArry){
		if(sheetInfoArry == null) {
			return "OBJNULL";			
		}
		
		int size = sheetInfoArry.length;
		SheetPubInfo sheetPubInfo = sheetInfoArry[0];
		
		String sheetId = sheetPubInfo.getWorkSheetId();
		String require = sheetPubInfo.getDealRequire();
		Integer month = sheetPubInfo.getMonth();
		int region = sheetPubInfo.getRegionId();
		
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,region,month,hisFlag);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交状态不是挂起
		if (state != 1 ) {
			return "statusError";
		}
		//判断工单是否挂起,如果作释放,必须先解挂工单
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "statusError";//该工单已挂起
		}	
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;			
		TsmStaff staff = pubFunc.getLogonStaff();
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();
		String staffName = staff.getName();
		String phone = staff.getRelaPhone();
		require = "派单员工:"+staffName+"  派单人联系电话:"+phone+"\n派发意见:  "+require;
		//记录操作类型,操作内容
		String rcvOrgName = "";
		for(int i=0;i<size;i++) {
			sheetPubInfo = sheetInfoArry[i];
			if(i == 0) {
				if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
					String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
					rcvOrgName = this.pubFunc.getOrgName(orgId)+"("+sheetPubInfo.getRcvStaffName()+")";
					
				} else {
					rcvOrgName = sheetPubInfo.getRcvOrgName();

				}
				continue;
			}
			
			if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
				String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
				rcvOrgName =rcvOrgName + "||" + this.pubFunc.getOrgName(orgId)+"("+sheetPubInfo.getRcvStaffName()+")";
				

			} else {
				rcvOrgName =rcvOrgName+"||"+ sheetPubInfo.getRcvOrgName();

			}
			
		}
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId, require,rcvOrgName,"审核派单环节派单",require,3,StaticData.TACHE_DEAL);
			
		
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE",require);
		otherParam.put("ROUTE_VALUE",StaticData.ROUTE_GOTO_NEXT);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("SHEETARRAY", sheetInfoArry);
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号		
		//提交流程
		workSheetFlowService.submitWorkFlow(sheetId,region,otherParam);
		return "SUCCESS";
	}

	/**
	 * 部门处理单提交方法 待修改
	 * @param strReasonId	回单原因id字符串
	 * @param strReasonDesc	回单原因描述字符串
	 * @param dealResult    处理结果描述
	 * @return	是否成功
	 */
	@SuppressWarnings("all")
	public String submitDealSheet(SheetPubInfo sheetPubInfo,
			String strReasonId, String strReasonDesc, String dealResult,int netFlag) {

		String sheetId = sheetPubInfo.getWorkSheetId();
		String orgId = pubFunc.getLogonStaff().getOrganizationId();		
		int regionId = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();
		//取得员工所在部门的地域
		int staffIegion = pubFunc.getOrgRegion(orgId);
		//本地网地域
		int upRegion = this.pubFunc.getUpRegionId(staffIegion);
		if(upRegion == 1) {
			upRegion = staffIegion;
		}
		
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,regionId,month, hisFlag);
		int state = sheetInfo.getLockFlag();
		
		// 非处理中状态的单子不能提交 工单状态不为挂起
		if (state != 1) {
			return "statusError";
		}
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "statusError";
		}
		String allotWorkSheet="";//审批上级工单
		String dealType = "部门处理环节回单";
		int dealId = 6;
		if(sheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
			dealType = "部门处理环节审批";
			dealId = 7;
			//调用审批单
			WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(sheetInfo);
			int sizeAllot = 0;
			WorkSheetAllotReal sheetAllotRealobj = null;
			if(sheetAllotRealList.length > 0) {
				sizeAllot = sheetAllotRealList.length;
			}
			if(sizeAllot > 0) {
				for(int j=0;j<sizeAllot;j++) {
					sheetAllotRealobj = sheetAllotRealList[j];
					if(sheetAllotRealobj.getMainSheetFlag() == 1) {
						if(sheetAllotRealobj.getCheckFalg() == 0) {
							return "ALLOTREAL";
						}
						allotWorkSheet = sheetAllotRealobj.getPreDealSheet();
						break;
					}
				}
			}
			
		}
		//更新派单关系表审批状态和处理状态
		WorkSheetAllotReal workSheetAllotReal = new WorkSheetAllotReal();
		//如果是部门审批单话,更新上级处理单的状态
		if(sheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
			workSheetAllotReal.setCheckFalg(1);
			workSheetAllotReal.setDealStauts("审批通过");
			workSheetAllotReal.setWorkSheetId(allotWorkSheet);
			workSheetAllotReal.setMonth(month);
		} else {
			workSheetAllotReal.setCheckFalg(1);
			workSheetAllotReal.setDealStauts("处理完成");
			workSheetAllotReal.setWorkSheetId(sheetId);
			workSheetAllotReal.setMonth(month);
		}
				
		this.workSheetAlllot.updateWorkSheetAllotReal(workSheetAllotReal);
		//根据要更新派单关系表的处理工单来得到派单关系表中的对象
		//得到workSheetAlllot对象,在判断审批工单是否为审核单,标志为主单
		WorkSheetAllotReal sheetAllotObj = this.workSheetAlllot.getSheetAllotObj(workSheetAllotReal.getWorkSheetId(), month);
		//判断sheetAllotObj不为空
		boolean boo = true;
		//得到审批或审核工单的工单对象
		SheetPubInfo orderSheetInfo = this.sheetPubInfoDao.getSheetObj(sheetAllotObj != null ?
				sheetAllotObj.getCheckWorkSheet() : "", regionId, month, boo);
		int orderTach = 0;//审批或审核工单的环节
		int orderSheetType = 0;
		String orderSheetId = "";
		Integer orderMoth = 1;
		if(orderSheetInfo != null) {
			orderTach = orderSheetInfo.getTacheId();
			orderSheetType = orderSheetInfo.getSheetType();
			orderSheetId = orderSheetInfo.getWorkSheetId();
			orderMoth = orderSheetInfo.getMonth();
		}
		int mainType = 0;//在派单关系表的主办标志
		if(sheetAllotObj != null) {
			mainType = sheetAllotObj.getMainSheetFlag();
		}
		
		//主办单位完成或者审批单完成 该单修改成待审批
		if(mainType==1 && orderSheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
			//工单修改成待审批
			this.sheetPubInfoDao.updateSheetState(orderSheetId,
					StaticData.WKST_ORGAUD_STATE, this.pubFunc.getStaticName(StaticData.WKST_ORGAUD_STATE), 14, 0);
			
		}
		
		//在派单关系表为主办,审批工单在工单表中的环节为审核环节 工单修改成待审核
		boolean audBoo = false;//判断是否可以进行自动归档
		if(mainType == 1 && orderTach == StaticData.TACHE_AUIT) {
			this.orderAskInfoDao.updateOrderStatu(sheetInfo.getServiceOrderId(),StaticData.OR_WAIT_DEAL_STATU,month,
					this.pubFunc.getStaticName(StaticData.OR_WAIT_DEAL_STATU));
			//工单修改成待审核 带月分区14进去，修改LOCK_DATE时间
			this.sheetPubInfoDao.updateSheetState(orderSheetId,
					StaticData.WKST_AUD_STATE, this.pubFunc.getStaticName(StaticData.WKST_AUD_STATE), 14, 0);
			audBoo = true;
		}		
		String stateDesc = pubFunc.getStaticName(StaticData.WKST_FINISH_STATE);
		this.sheetPubInfoDao.updateSheetState(sheetId,
				StaticData.WKST_FINISH_STATE, stateDesc, month, 2);
		this.sheetPubInfoDao.updateSheetFinishDate(sheetId);
		//更新操作
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId, dealResult, " ", dealType, dealResult, dealId, 10);//10无下一环节
		if(audBoo){
			//如果是疑难工单中绿色通道，自动归档，不进行审核
			String flowOrderId = sheetInfo.getServiceOrderId();
			OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskObj(flowOrderId,orderMoth,false);//得到定单对象			
			boolean finiBoo = false;
			
			if(orderAskInfo.getServiceDate()==0 && orderAskInfo.getUrgencyGrade() == StaticData.SERV_GRADE_VERY_BICE_DYPASS) {
				finiBoo = true;
			}	else {
				ServiceContent servContent = this.servContentDao.getServContentByOrderId(orderAskInfo.getServOrderId(), false, 0);
				if(servContent.getAppealProdId() == StaticData.APPEAL_PROD_ID_YN_HB) {
					finiBoo = true;
				}
			}
			if(this.finishFlag(finiBoo, orderSheetInfo)) {
				this.sheetPubInfoDao.updateSheetDealRequire(sheetId, dealResult," ",dealType,dealResult,dealId,StaticData.TACHE_FINISH);
				//记录定单竣工的环节
				this.orderAskInfoDao.updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
				this.sheetPubInfoDao.updateFetchSheetStaff(orderSheetId, 0, "SYSTEM", "SYSTEM", "SYSTEM");
				Map otherParam = new HashMap();
				TsmStaff staffObj = pubFunc.getLogonStaff();
				String dealOrg = staffObj.getOrganizationId();
				String dealStaffId = staffObj.getId();
				otherParam.put("ROUTE_VALUE", StaticData.ROUTE_AUD_TO_FINISH);
				otherParam.put("MONTH_FALG", month);
				otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
				otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
				otherParam.put("SERV_ORDER_ID",flowOrderId);
				otherParam.put("SHEET_ID",orderSheetId);
				otherParam.put("WF__REGION_ID",String.valueOf(regionId));
				this.WorkFlowAttemper__FACADE__.submitWorkFlow(orderSheetInfo.getWflInstId(), orderSheetInfo.getTacheInstId(), otherParam);					
			}	
		}
		return "success";
	}
	
	private boolean finishFlag(boolean finiBoo, SheetPubInfo orderSheetInfo) {
		return finiBoo && orderSheetInfo != null;
	}
	
	/**
	 * 预受理部门处理单提交方法
	 * sheetPubInfo 工单对象
	 * orderAskInfo  定单对象
	 * @return	是否成功
	 */
	@SuppressWarnings("all")
	public String submitDealSheetYs(SheetPubInfo sheetPubInfo,OrderAskInfo orderAskInfo) {
				String sheetId = sheetPubInfo.getWorkSheetId();
				int regionId = sheetPubInfo.getRegionId();
				Integer month = sheetPubInfo.getMonth();								
				boolean hisFlag = true;
				SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
						sheetId,regionId,month, hisFlag);
				int state = sheetInfo.getLockFlag();
				
				// 非处理中状态的单子不能提交 工单状态不为挂起
				if (state != 1) {
					return "statusError";
				}
				if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
					return "statusError";
				}
				
				String allotWorkSheet="";//审批上级工单
				if(sheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
					//调用审批单
					WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(sheetInfo);
					int sizeAllot = 0;
					WorkSheetAllotReal sheetAllotRealobj = null;
					if(sheetAllotRealList.length > 0) {
						sizeAllot = sheetAllotRealList.length;
					}
					if(sizeAllot > 0) {
						for(int j=0;j<sizeAllot;j++) {
							sheetAllotRealobj = sheetAllotRealList[j];
							if(sheetAllotRealobj.getMainSheetFlag() ==1) {
								if(sheetAllotRealobj.getCheckFalg() ==0) {
									return "ALLOTREAL";
								}
								allotWorkSheet = sheetAllotRealobj.getPreDealSheet();
								break;
							}
						}
					}
					
				}
				//更新派单关系表审批状态和处理状态
				WorkSheetAllotReal workSheetAllotReal = new WorkSheetAllotReal();
				//如果是部门审批单话,更新上级处理单的状态
				if(sheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
					workSheetAllotReal.setCheckFalg(1);
					workSheetAllotReal.setDealStauts("审批通过");
					workSheetAllotReal.setWorkSheetId(allotWorkSheet);
					workSheetAllotReal.setMonth(month);
				} else {
					workSheetAllotReal.setCheckFalg(1);
					workSheetAllotReal.setDealStauts("处理完成");
					workSheetAllotReal.setWorkSheetId(sheetId);
					workSheetAllotReal.setMonth(month);
				}
						
				this.workSheetAlllot.updateWorkSheetAllotReal(workSheetAllotReal);
				//根据要更新派单关系表的处理工单来得到派单关系表中的对象
				//得到workSheetAlllot对象,在判断审批工单是否为审核单,标志为主单
				WorkSheetAllotReal sheetAllotObj = this.workSheetAlllot.getSheetAllotObj(workSheetAllotReal.getWorkSheetId(), month);
				boolean boo = true;
				//得到审批或审核工单的工单对象
				SheetPubInfo orderSheetInfo = this.sheetPubInfoDao.getSheetObj(
						sheetAllotObj.getCheckWorkSheet(),regionId,month, boo);
				int orderTach = 0;//审批或审核工单的环节
				if(orderSheetInfo != null) {
					orderTach = orderSheetInfo.getTacheId();
				}
				int mainType = 0;//在派单关系表的主办标志
				if(sheetAllotObj != null) {
					mainType = sheetAllotObj.getMainSheetFlag();
				}
				//主办单位完成或者审批单完成 该单修改成待审批
				if(this.isMainType(mainType, orderSheetInfo) && orderSheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
					//工单修改成待审批
					this.sheetPubInfoDao.updateSheetState(orderSheetInfo.getWorkSheetId(),
							StaticData.WKST_ORGAUD_STATE, this.pubFunc.getStaticName(StaticData.WKST_ORGAUD_STATE),14,0);
					
				}
				
				//在派单关系表为主办,审批工单在工单表中的环节为审核环节
				if(this.isMainType(mainType, orderSheetInfo) && orderTach == StaticData.TACHE_AUIT) {
					//工单修改成待审核 带月分区14进去，修改LOCK_DATE时间
					this.sheetPubInfoDao.updateSheetState(orderSheetInfo.getWorkSheetId(),
							StaticData.WKST_AUD_STATE, this.pubFunc.getStaticName(StaticData.WKST_AUD_STATE), 14, 0);
					
					this.orderAskInfoDao.updateOrderStatu(sheetInfo.getServiceOrderId(),StaticData.OR_WAIT_DEAL_STATU,month,
							this.pubFunc.getStaticName(StaticData.OR_WAIT_DEAL_STATU));
				}								
				
				String stateDesc = pubFunc.getStaticName(StaticData.WKST_FINISH_STATE);
				this.sheetPubInfoDao.updateSheetState(sheetId,
						StaticData.WKST_FINISH_STATE, stateDesc,month,2);
				this.sheetPubInfoDao.updateSheetFinishDate(sheetId);
				//更新操作
				this.sheetPubInfoDao.updateSheetDealRequire(sheetId, sheetInfo.getDealRequire()," ",
						sheetPubInfo.getServTypeDesc()+"部门处理回单",sheetPubInfo.getDealContent(),sheetInfo.getServType()-1,10);
				this.orderAskInfoDao.updateCrmAskSheet(orderAskInfo);/*在CRM系统中受理是否成功*/
				return "success";		
	}
	
	private boolean isMainType(int mainType, SheetPubInfo orderSheetInfo) {
		return mainType == 1 && orderSheetInfo != null;
	}
	
	/**
	 * 得到审批工单对象
	 * @param sheetPubInfo
	 * @return
	 */
	private WorkSheetAllotReal[] dealOrgAudSheet(SheetPubInfo sheetPubInfo) {
		String strWhere = "AND cc_worksheet_allot_rela.check_worksheet_id= '"+sheetPubInfo.getWorkSheetId()+"' " +
				"AND cc_worksheet_allot_rela.month_flag="+sheetPubInfo.getMonth()+" and cc_worksheet_allot_rela.main_sheet_flag=1";
		return this.workSheetAlllot.getWorkSheetAllotReal(strWhere, true);
	}

	/**
	 * 审核环节重新派单(待修改)
	 * 
	 * @param sheetPubInfo
	 *            工单对像
	 * @return 是否成功
	 */
	public String submitAuitSheetToDeal(SheetPubInfo[] workSheetObj,String acceptContent) {
		if(workSheetObj == null) {
			return "ERROR";
		}
		SheetPubInfo sheetPubInfo = workSheetObj[0];
		int size = workSheetObj.length;
		String sheetId = sheetPubInfo.getWorkSheetId();
		String require = sheetPubInfo.getDealRequire();
		int   region = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();

		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId, region,month, true);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (state != 1) {
			return "STATUSERROR";
		}	
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "STATUSERROR";
		}
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;			
		//判断该审核单是否可以审核

			//调用审批单
			WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(sheetInfo);
			int sizeAllot = 0;
			WorkSheetAllotReal sheetAllotRealobj = null;
			if(sheetAllotRealList.length > 0) {
				sizeAllot = sheetAllotRealList.length;
			}
			
			if(sizeAllot > 0) {
				for(int j=0;j<sizeAllot;j++) {
					sheetAllotRealobj = sheetAllotRealList[j];
					if(sheetAllotRealobj.getMainSheetFlag() ==1) {
						if(sheetAllotRealobj.getCheckFalg() ==0) {
							return "ALLOTREAL";
						}
						break;
					}
				}
			}
			TsmStaff staff = pubFunc.getLogonStaff();
			String dealOrg = staff.getOrganizationId();
			String dealStaffId = staff.getId();	
			
			String rcvOrgName = "";
			//String chekLink = "SUCCESS";//上级判断
			for(int i=0;i<size;i++) {
				sheetPubInfo = workSheetObj[i];
				if(i == 0) {
					if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
						String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
						rcvOrgName = this.pubFunc.getOrgName(orgId)+"("+sheetPubInfo.getRcvStaffName()+")";
						
					} else {
						rcvOrgName = sheetPubInfo.getRcvOrgName();
					}
					continue;
				}
				
				if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
					String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
					rcvOrgName =rcvOrgName + "||" + this.pubFunc.getOrgName(orgId)+"("+sheetPubInfo.getRcvStaffName()+")";
					
				} else {
					rcvOrgName =rcvOrgName+"||"+ sheetPubInfo.getRcvOrgName();

				}
				
			}	

		//记录操作类型,操作内容
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId,require,rcvOrgName,"审核环节重新派发工单",acceptContent ,10,StaticData.TACHE_DEAL);		
			
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE",require);
		otherParam.put("ROUTE_VALUE",StaticData.ROUTE_AUIT_TO_DEAL);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工	
		otherParam.put("SHEETARRAY", workSheetObj);
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		//提交流程
		this.workSheetFlowService.submitWorkFlow(sheetId,region, otherParam);
		return "SUCCESS";
	}

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
			String sheetId, String orgId,int regionId,Integer month) {
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,regionId,month,hisFlag);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (state != 1 ) {
			return "STATUERROR";
		}	
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "STATUERROR";
		}
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;		
		TsmStaff staffObj = pubFunc.getLogonStaff();
		String dealOrg = staffObj.getOrganizationId();
		String dealStaffId = staffObj.getId();
		Map otherParam = new HashMap();
		
		//记录操作类型,操作内容
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId,sheetInfo.getDealRequire(),"","审核环节直接竣工",retVisitResult.getRetVisitContent(),11,StaticData.TACHE_FINISH);
		this.orderAskInfoDao.updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
		
		//疑难工单更新定性
		this.orderAskInfoDao.updateQualiative(retVisitResult.getConclusionId(), 
												retVisitResult.getConclusionDesc(), month, sheetInfo.getServiceOrderId());
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号		
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_AUD_TO_FINISH);
		otherParam.put("MONTH_FALG", month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		this.submitWorkFlow(sheetId,regionId,otherParam);
		
		return "SUCCESS";
	}	

	/**
	 * 商机单审核环节直接处理增加办结原因
	 */	
	public String auitSheetFinishWithQualitative(RetVisitResult retVisitResult, String sheetId, String orgId, int regionId, Integer month, SJSheetQualitative sjQualitative) {
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId, regionId, month, hisFlag);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (state != 1) {
			return "STATUERROR";
		}
		if (sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "STATUERROR";
		}
		// 记录操作类型,操作内容
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId, sheetInfo.getDealRequire(), "", "审核环节直接竣工", retVisitResult.getRetVisitContent(), 11,
				StaticData.TACHE_FINISH);
		this.orderAskInfoDao.updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
		// 疑难工单更新定性
		this.orderAskInfoDao.updateQualiative(retVisitResult.getConclusionId(), retVisitResult.getConclusionDesc(), month, sheetInfo.getServiceOrderId());
		sjQualitative.setSjRegionName(this.pubFunc.getRegionName(Integer.parseInt(sjQualitative.getSjRegionId())));
		this.sjSheetQualitativeDaoImpl.saveSJSheetQualitative(sjQualitative);
		ServiceContent servContent = this.servContentDao.getServContentByOrderId(sheetInfo.getServiceOrderId(), false, 0);
		if (autoVisitSJ(sheetInfo.getServiceOrderId(), month, sheetId, 5, servContent.getServiceTypeDetail())) {
			Map otherParam = new HashMap();
			// 流水号
			String flowSeq = "1";
			if (sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
				flowSeq = sheetInfo.getFlowSequence();
			}
			int flowSeqNo = Integer.parseInt(flowSeq) + 1;
			TsmStaff staffObj = pubFunc.getLogonStaff();
			String dealOrg = staffObj.getOrganizationId();
			String dealStaffId = staffObj.getId();
			otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeqNo));// 流水号
			otherParam.put("ROUTE_VALUE", StaticData.ROUTE_AUD_TO_FINISH);
			otherParam.put("MONTH_FALG", month);
			otherParam.put("DEAL_PR_ORGID", dealOrg);// 派发部门
			otherParam.put("DEAL_PR_STAFFID", dealStaffId);// 派发员工
			this.submitWorkFlow(sheetId, regionId, otherParam);
		}
		return "SUCCESS";
	}

	/**
	 * 商机单审核环节直接处理增加办结原因
	 */
	public String auitSheetFinishWithQualitativeNew(RetVisitResult retVisitResult, BuopSheetInfo buopSheetInfo, String sheetId, String orgId, int regionId, Integer month, SJSheetQualitative sjQualitative) {
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId, regionId, month, hisFlag);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (state != 1) {
			return "STATUERROR";
		}
		if (sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "STATUERROR";
		}
		// 记录操作类型,操作内容
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId, sheetInfo.getDealRequire(), "", "审核环节直接竣工", retVisitResult.getRetVisitContent(), 11,
				StaticData.TACHE_FINISH);
		this.orderAskInfoDao.updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
		// 疑难工单更新定性
		this.orderAskInfoDao.updateQualiative(retVisitResult.getConclusionId(), retVisitResult.getConclusionDesc(), month, sheetInfo.getServiceOrderId());
		sjQualitative.setSjRegionName(this.pubFunc.getRegionName(Integer.parseInt(sjQualitative.getSjRegionId())));
		this.sjSheetQualitativeDaoImpl.saveSJSheetQualitative(sjQualitative);
		ServiceContent servContent = this.servContentDao.getServContentByOrderId(sheetInfo.getServiceOrderId(), false, 0);
		if (autoVisitSJ(sheetInfo.getServiceOrderId(), month, sheetId, 5, servContent.getServiceTypeDetail())) {
			Map otherParam = new HashMap();
			// 流水号
			String flowSeq = "1";
			if (sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
				flowSeq = sheetInfo.getFlowSequence();
			}
			int flowSeqNo = Integer.parseInt(flowSeq) + 1;
			TsmStaff staffObj = pubFunc.getLogonStaff();
			String dealOrg = staffObj.getOrganizationId();
			String dealStaffId = staffObj.getId();
			otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeqNo));// 流水号
			otherParam.put("ROUTE_VALUE", StaticData.ROUTE_AUD_TO_FINISH);
			otherParam.put("MONTH_FALG", month);
			otherParam.put("DEAL_PR_ORGID", dealOrg);// 派发部门
			otherParam.put("DEAL_PR_STAFFID", dealStaffId);// 派发员工
			this.submitWorkFlow(sheetId, regionId, otherParam);
		}
		//修改商机单
		this.businessOpportunityDao.updateBuopSheetInfo(buopSheetInfo);
		return "SUCCESS";
	}


	public boolean autoVisitSJ(String orderId, Integer month, String sheetId, int tacheType, String serviceTypeDetail) {
		if (!"外部受理".equals(serviceTypeDetail)) {
			sheetPubInfoDao.deleteCustomerJudgeByOrderId(orderId);
			sheetPubInfoDao.insertCustomerJudge(orderId, orderId, tacheType);
			orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_AUTOVISIT_STATU, month, pubFunc.getStaticName(StaticData.OR_AUTOVISIT_STATU));
			sheetPubInfoDao.updateSheetState(sheetId, StaticData.WKST_FINISH_STATE, pubFunc.getStaticName(StaticData.WKST_FINISH_STATE), month, 2);
			sheetPubInfoDao.updateSheetFinishDate(sheetId);
			sheetPubInfoDao.updateTachSheetFinsh(orderId, StaticData.WKST_FINISH_STATE, pubFunc.getStaticName(StaticData.WKST_FINISH_STATE), 2, month, StaticData.TACHE_DEAL);
			return false;
		}
		return true;
	}

	public void auitSheetFinishFromVisit(String tacheType, String sheetId, String orderId, int region, Integer month) {
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_COMPLETE_STATU, month, pubFunc.getStaticName(StaticData.OR_COMPLETE_STATU));
		SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, region, month, true);
		Map otherParam = new HashMap();
		otherParam.put("FLOW_SEQUENCE", String.valueOf(pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(), "1", 1)));
		otherParam.put("DEAL_PR_ORGID", sheetInfo.getDealOrgId());
		otherParam.put("DEAL_PR_STAFFID", sheetInfo.getDealStaffId() + "");
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_AUD_TO_FINISH);
		otherParam.put("MONTH_FALG", sheetInfo.getMonth());
		otherParam.put("WF__REGION_ID", region);
		otherParam.put("SERV_ORDER_ID", orderId);
		otherParam.put("SHEET_ID", sheetId);
		if ("4".equals(tacheType)) {
			otherParam.put("DEAL_REQUIRE", "");
		}
		workSheetFlowService.submitWorkFlow(sheetInfo.getWflInstId(), sheetInfo.getTacheInstId(), otherParam);
	}

	/**
	 * 回访环节重新派单
	 * 
	 * @param sheetPubInfo
	 *            工单对像
	 * @return 是否成功
	 */
	public boolean submitReplySheetToDeal(SheetPubInfo[] worksheetObj,String acceptContent){
		if(worksheetObj == null) {
			return false;
		}
		SheetPubInfo sheetPubInfo = worksheetObj[0];
		int size = worksheetObj.length;
		String sheetId = sheetPubInfo.getWorkSheetId();
		String require = sheetPubInfo.getDealRequire();
		int region = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();
		
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,region,month, true);
		
		int states = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (states != 1 ) {
			return false;
		}	
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return false;
		}
		String rcvOrgName = "";
		for(int i=0;i<size;i++) {
			sheetPubInfo = worksheetObj[i];
			if(i == 0) {
				if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
					String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
					rcvOrgName = this.pubFunc.getOrgName(orgId);

				} else {
					rcvOrgName = sheetPubInfo.getRcvOrgName();
				}
				continue;
			}
			
			if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
				String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
				rcvOrgName =rcvOrgName + "||" + this.pubFunc.getOrgName(orgId);
			} else {
				rcvOrgName =rcvOrgName+"||"+ sheetPubInfo.getRcvOrgName();
			}
			
		}		
		//记录操作类型,操作内容
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId,require,rcvOrgName,"回访环节重新派发工单",sheetPubInfo.getDealContent(),13,StaticData.TACHE_DEAL);		
		
		
		TsmStaff staff = pubFunc.getLogonStaff();
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();		
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE",require);
		otherParam.put("ROUTE_VALUE",StaticData.ROUTE_REPLY_TO_DEAL);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("SHEETARRAY", worksheetObj);
		//提交流程
		return this.submitWorkFlow(sheetId,region, otherParam);
	}
	/**
	 * 保存工单动作
	 * @param sheetActionInfo 工单动作对象
	 * @param tacheId 所处环节
	 * @param actionType 动作类型
	 * @param type 动作 1为自动派发 2 联络部成功 3 提取工单 4 释放工单	5 挂起工单	6 解挂工单    7追回工单
	 * @return
	 */
	private boolean saveSheetDealAction(SheetActionInfo sheetActionInfo,int tacheId,int actionType,int type) {
		//取当前登录员工信息
		TsmStaff staff = this.pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String orgId = staff.getOrganizationId();
		String orgName = staff.getOrgName();
		
		String guid = pubFunc.crtGuid();
		sheetActionInfo.setActionGuid(guid);
		sheetActionInfo.setTacheId(tacheId);
		sheetActionInfo.setTacheName(pubFunc.getStaticName(tacheId));
		sheetActionInfo.setActionCode(actionType);
		sheetActionInfo.setActionName(pubFunc.getStaticName(actionType));
		if(type != 1) {
			sheetActionInfo.setOpraOrgId(orgId);
			sheetActionInfo.setOpraOrgName(orgName);
			sheetActionInfo.setOpraStaffId(staffId);
			sheetActionInfo.setOpraStaffName(staffName);			
		}
		this.sheetActionInfoDao.saveSheetActionInfo(sheetActionInfo);
		return true;
	}
	
	/**
	 * 查询系统时间
	 * 
	 * @return 系统时间
	 */
	public String getSysDate(){
		return this.pubFunc.getSysDate();
	}

	/**
	 * 根据员工登录名查询到员工STAFFID
	 * @param logname 员工登录名
	 * @return 员工ID
	 */
	public int getStaffId(String logname) {
		return this.pubFunc.getStaffId(logname);
	}

	/**
	 * 定单工单注销方法 万荣伟 2010-05-31 修改，注销内容放在受理内容里面。
	 * @param orderAskInfo 受理单受理信息
	 * @return 是否成功
	 */
	public boolean cancelServiceOrder(OrderAskInfo orderAskInfo) {
		String orderId = orderAskInfo.getServOrderId();		
		OrderAskInfo orderAskObj = orderAskInfoDao.getOrderAskObj(orderId, orderAskInfo.getMonth(), false);
		
		//填写受理单注销原因
		TsmStaff staffInfo = pubFunc.getLogonStaff();
		String staffName = staffInfo.getName();//注销人
		String date = pubFunc.getSysDate();//注销时间
		String cancel =  date +"  注销员工："+ staffName+"("+staffInfo.getId()+")\n注销内容：";
		//@cancel@
		String comment = orderAskInfo.getComment().replace("@cancel@", cancel);
		servContentDao.updateAcceptContent(orderAskInfo.getRegionId(), orderAskInfo.getServOrderId(), comment);
		boolean isNewCmp = orderAskObj.getServType() == StaticData.SERV_TYPE_NEWTS;
		
		//更新所有工单为作废状态
		int state = isNewCmp ? StaticData.WKST_CANCEL_NEW : StaticData.WKST_LOGOFF_STATE;
		String stateDesc = pubFunc.getStaticName(state);
		sheetPubInfoDao.updateSheetStateByOrder(orderId,state,stateDesc,9,orderAskObj.getMonth());
		
		// 更新定单的作废状态
		int oState = isNewCmp ? StaticData.OR_CANCEL_STATU : StaticData.OR_LOGOFF_STATU; 
		orderAskInfoDao.updateOrderStatu(orderId,oState,orderAskObj.getMonth(),this.pubFunc.getStaticName(oState));

		Integer monthHis = orderAskObj.getMonth();
		
		// 更新定单完成时间
		orderAskInfoDao.updateOrderFinishDate(orderId,monthHis);
		// 注销工单流
		String wfInstId = sheetPubInfoDao.getTheLastSheetInfo(orderId).getWflInstId();
		WorkFlowAttemper__FACADE__.cancelWorkFlow(wfInstId);		
	
		// 记录工单动作
		SheetActionInfo sheetActionInfo = new SheetActionInfo();
		String guid = pubFunc.crtGuid();
		sheetActionInfo.setActionGuid(guid);
		sheetActionInfo.setWorkSheetId(orderId);
		int tacheId = 0;
		sheetActionInfo.setTacheId(tacheId);
		sheetActionInfo.setTacheName("处理中");		
		sheetActionInfo.setActionCode(0);
		sheetActionInfo.setActionName("工单注销");			
		sheetActionInfo.setOpraOrgId(staffInfo.getOrganizationId());
		sheetActionInfo.setOpraOrgName(staffInfo.getOrgName());
		sheetActionInfo.setOpraStaffId(Integer.parseInt(staffInfo.getId()));
		sheetActionInfo.setOpraStaffName(staffName);
		sheetActionInfo.setRegionId(orderAskInfo.getRegionId());
		sheetActionInfo.setServOrderId(orderId);
		sheetActionInfo.setMonth(orderAskObj.getMonth());
		//保存动作
		sheetActionInfoDao.saveSheetActionInfo(sheetActionInfo);
		
		Map map = new HashMap();
		map.put("SERV_ORDER_ID", orderAskObj.getServOrderId());
		map.put("MONTH_FALG", orderAskObj.getMonth());
		map.put("TYPEFLAG", "1");
			
		workFlowBusiServImpl.finishOrderAndSheet(map);//移历史表
		return true;
	}
	// =====================以下为get/set方法和私有方法===============================
	/**
	 * 提交工作流方法.
	 */
	private boolean submitWorkFlow(String sheetId,int quryRegion,Map otherParam){		
		boolean hisFlag = true;
		String strMonth = "0";
		if(otherParam.containsKey("MONTH_FALG")){
			strMonth = otherParam.get("MONTH_FALG").toString();
		}
		Integer month = Integer.valueOf(strMonth);
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId,quryRegion,month,hisFlag);
		if(sheetPubInfo == null){
			logger.warn("没有查询到工单号为: {} 的工单!", sheetId);
			return false;
		}
		//状态非处理中的工单不能提交
		int sheetState = sheetPubInfo.getLockFlag();
		if(sheetState != 1 || sheetPubInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE){
			logger.warn("定单号为: {}的工单状态不是处理中,不能提交!", sheetId);
			return false;
		}

		String regionId = "" + sheetPubInfo.getRegionId();
		String orderId = sheetPubInfo.getServiceOrderId();

		Map inParam = new HashMap();
		inParam.putAll(otherParam);
		inParam.put("SERV_ORDER_ID",orderId);
		inParam.put("SHEET_ID",sheetId);
		inParam.put("WF__REGION_ID",regionId);
		inParam.put("MONTH_FALG",strMonth);
		
		String wfInstId = sheetPubInfo.getWflInstId();
		String wfNodeInstId = sheetPubInfo.getTacheInstId();
		
		//修改前的方法
		workSheetFlowService.submitWorkFlow(wfInstId, wfNodeInstId, inParam);
		return true;
	}
	
	/**
	 * 回访完成到工单归档环节
	 * @param sheetId 工单号
	 * @param orderId 定单号
	 * @return
	 */
	public String replySheetToPigeonhole(String sheetId,String orderId) {
		boolean hisFlag = false;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetId,hisFlag);
		if(sheetPubInfo == null){
			logger.warn("没有查询到工单号为: {} 的工单!", sheetId);
			return "error";
		}	
		String regionId = "" + sheetPubInfo.getRegionId();
		Map inParam = new HashMap();
		inParam.put("SERV_ORDER_ID",orderId);
		inParam.put("SHEET_ID",sheetId);
		inParam.put("WF__REGION_ID",regionId);	
		inParam.put("ROUTE_VALUE",StaticData.ROUTE_REPLY_TO_PIGEONHOLE);
		String wfInstId = sheetPubInfo.getWflInstId();
		String wfNodeInstId = sheetPubInfo.getTacheInstId();	
		this.WorkFlowAttemper__FACADE__.submitWorkFlow(wfInstId, wfNodeInstId, inParam);
		return "success";
	}
	
	public String pigeonholeToQualitative(String sheetId,String orderId) {
		boolean hisFlag = false;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetId,hisFlag);
		if(sheetPubInfo == null){
			logger.warn("没有查询到工单号为: {} 的工单!", sheetId);
			return "error";
		}	
		String regionId = "" + sheetPubInfo.getRegionId();
		Map inParam = new HashMap();
		inParam.put("SERV_ORDER_ID",orderId);
		inParam.put("SHEET_ID",sheetId);
		inParam.put("WF__REGION_ID",regionId);	
		inParam.put("ROUTE_VALUE",StaticData.ROUTE_PIGEONHOLE_TO_QUALITATIVE);
		String wfInstId = sheetPubInfo.getWflInstId();
		String wfNodeInstId = sheetPubInfo.getTacheInstId();	
		this.WorkFlowAttemper__FACADE__.submitWorkFlow(wfInstId, wfNodeInstId, inParam);
		return "success";
	}
	
	/**
	 * 工单定性到工单竣工
	 * @param sheetId 工单号
	 * @param orderId 定单号
	 * @return
	 */
	public String qualitativeTifinsh(String sheetId,String orderId) {
		boolean hisFlag = false;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetId,hisFlag);
		if(sheetPubInfo == null){
			logger.warn("没有查询到工单号为: {} 的工单!", sheetId);
			return "error";
		}	
		String regionId = "" + sheetPubInfo.getRegionId();
		Map inParam = new HashMap();
		inParam.put("SERV_ORDER_ID",orderId);
		inParam.put("SHEET_ID",sheetId);
		inParam.put("WF__REGION_ID",regionId);
		inParam.put("ROUTE_VALUE",StaticData.ROUTE_QUALITATIVE_TO_FINISH);
		String wfInstId = sheetPubInfo.getWflInstId();
		String wfNodeInstId = sheetPubInfo.getTacheInstId();	
		this.WorkFlowAttemper__FACADE__.submitWorkFlow(wfInstId, wfNodeInstId, inParam);
		return "success";		
	}
	/**
	 * 工单归档到竣工环节
	 * @param sheetId 工单号
	 * @param orderId 定单号
	 * @return
	 */
	public String pigeonholeTofinsh(String sheetId,String orderId) {
		boolean hisFlag = false;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetId,hisFlag);
		if(sheetPubInfo == null){
			logger.warn("没有查询到工单号为: {} 的工单!", sheetId);
			return "error";
		}	
		String regionId = "" + sheetPubInfo.getRegionId();
		Map inParam = new HashMap();
		inParam.put("SERV_ORDER_ID",orderId);
		inParam.put("SHEET_ID",sheetId);
		inParam.put("WF__REGION_ID",regionId);	
		inParam.put("ROUTE_VALUE",StaticData.ROUTE_PIGEONHOLE_TO_FINISH);
		String wfInstId = sheetPubInfo.getWflInstId();
		String wfNodeInstId = sheetPubInfo.getTacheInstId();	
		this.WorkFlowAttemper__FACADE__.submitWorkFlow(wfInstId, wfNodeInstId, inParam);
		return "success";		
	}
	
	/**
     * 提交工作流方法
     * 
     * @param sheetId
     * @param quryRegion
     * @param otherParam
     * @return
     */
    private String submitWorkFlow(String sheetId, int quryRegion, Integer month, Map otherParam) {
        SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId, quryRegion, month,
                true);
        if (sheetPubInfo == null) {
            logger.warn("没有查询到工单号为: {} 的工单!", sheetId);
            return "SHEETERROR";
        }
        Map inParam = new HashMap();
        inParam.putAll(otherParam);
        inParam.put("SERV_ORDER_ID", sheetPubInfo.getServiceOrderId());
        inParam.put("SHEET_ID", sheetId);
        inParam.put("WF__REGION_ID", quryRegion + "");
        inParam.put("MONTH_FALG", month);

        String wfInstId = sheetPubInfo.getWflInstId();
        String wfNodeInstId = sheetPubInfo.getTacheInstId();
        WorkFlowAttemper__FACADE__.submitWorkFlow(wfInstId, wfNodeInstId, inParam);
        return "SUCCESS";
    }
	/**
	 * 投诉部门处理单追回方法
	 * @param worksheetId
	 * @param month
	 * @param regionId
	 * @return
	 */
    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public String workSheetReplevyTsSheet(String worksheetId,Integer month,int regionId){
    	logger.info("进入工单追回页面");
	    String backReason = "工单被追回";
        Integer monthObj = month;
        SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(worksheetId, regionId, monthObj, true);
        String flowSeq = "1";
        String tmpFlowSeq = sheetInfo.getFlowSequence();
        if (null != tmpFlowSeq && tmpFlowSeq.length() > 0) {
            flowSeq = tmpFlowSeq;
        }
        int flowSeqNo = Integer.parseInt(flowSeq) + 1;
        TsmStaff staff = pubFunc.getLogonStaff();

        // 记录处理类型
        TsSheetDealType typeBean = new TsSheetDealType();
        String guid = this.pubFunc.crtGuid();
        typeBean.setDealTypeId(guid);
        typeBean.setOrderId(sheetInfo.getServiceOrderId());
        typeBean.setWorkSheetId(worksheetId);
        typeBean.setDealType("部门处理追回派单");
        typeBean.setDealTypeDesc("追回派单");
        typeBean.setDealId(0);// ID
        typeBean.setDealDesc("是");
        typeBean.setDealContent(backReason);// 处理内容
        typeBean.setMonth(monthObj);
        tsWorkSheetDaoImpl.saveSheetDealType(typeBean);// 保存处理类型
        sheetPubInfoDao.updateSheetDealRequire(worksheetId, " ", " ", "部门处理被追回到派单", backReason, 
                46, StaticData.TACHE_ASSIGN_NEW);
        
        if(sheetInfo.getMainType() == 1){// 如果是主办 部门处理单，则走工作流
            Map otherParam = new HashMap();
            otherParam.put("DEAL_REQUIRE", backReason);
            otherParam.put("ROUTE_VALUE", StaticData.ROUTE_DEAL_TO_ASSIGN);
            otherParam.put("DEAL_PR_ORGID", staff.getOrganizationId());// 派发部门
            otherParam.put("DEAL_PR_STAFFID", staff.getId());// 派发员工
            otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeqNo));
            submitWorkFlow(worksheetId, regionId, monthObj, otherParam);
        }
        // 将工单状态改为 作废
        String where = " AND W.WORK_SHEET_ID = " + sheetInfo.getWorkSheetId();
        sheetPubInfoDao.updateDealDisannuul(StaticData.WKST_DISANNUUL_STATE_NEW, 
                pubFunc.getStaticName(StaticData.WKST_DISANNUUL_STATE_NEW),month,StaticData.TACHE_DEAL_NEW, where);
        return "success";
	}
	
	/**
	 * 把派出去的工单追回,可以使审批单或审核单可以做审核
	 * @param orderId
	 * @param workSheetId
	 * @param monthFlag
	 * @return
	 */
    @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public String workSheetReplevy(String orderId,String workSheetId,Integer monthFlag,int regionId) {
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(workSheetId,regionId,monthFlag, true);
		if(sheetPubInfo == null){
			logger.warn("没有查询到工单号为: {} 的工单!", workSheetId);
			return "error";
		}
		boolean flag = false;
		boolean isNewCmp = sheetPubInfo.getServType() == StaticData.SERV_TYPE_NEWTS;
		int tachId = sheetPubInfo.getTacheId();
        int sheetType = sheetPubInfo.getSheetType();
	    int sheetStatu = sheetPubInfo.getSheetStatu();
		if (pubFunc.isNewWorkFlow(sheetPubInfo.getServiceOrderId())) {
			if (StaticData.SHEET_TYPE_TS_DEAL == sheetType) { // 咨询单部门
				if (StaticData.WKST_REPEAL_STATE == sheetStatu || StaticData.WKST_ORGDEALING_STATE == sheetStatu) {
					if (sheetPubInfo.getMainType() == 1) {
						if (sheetPubInfo.getPrecontractSign() == 1) {
							// 咨询单部门处理追回到部门处理
							// 咨询单部门处理追回到部门审批
							recallBmclToBmcl(sheetPubInfo);
						} else {
							// 咨询单部门内处理追回到部门审批
							if ("error".equals(recallBmclToSp(sheetPubInfo))) {
								return "error";
							}
						}
					} else {
						// 咨询单协办单追回作废
						recallBmclToZf(sheetPubInfo);
					}
				} else {
					return "error";
				}
			} else if (StaticData.SHEET_TYPE_TS_DEAL_NEW == sheetType) { // 投诉单部门处理
				if (StaticData.WKST_REPEAL_STATE_NEW == sheetStatu || StaticData.WKST_DEALING_STATE_NEW == sheetStatu) {
					if (sheetPubInfo.getMainType() == 1) {
						SheetPubInfo retSheet = this.sheetPubInfoDao.getSheetPubInfo(sheetPubInfo.getSourceSheetId(), false);
						if (StaticData.SHEET_TYPE_TS_ASSING_NEW == retSheet.getSheetType()) {
							// 投诉单部门处理追回到后台派单
							recallBmclToHtpd(sheetPubInfo);
						} else {
							// 投诉单部门处理追回到部门处理
							// 投诉单部门处理追回到部门内审批
							recallBmclToBmcl(sheetPubInfo);
						}
					} else {
						// 投诉单协办单追回作废
						recallBmclToZf(sheetPubInfo);
					}
				} else {
					return "error";
				}
			} else if (StaticData.SHEET_TYPE_TS_IN_DEAL == sheetType) { // 投诉单部门内处理
				if (StaticData.WKST_REPEAL_STATE_NEW == sheetStatu || StaticData.WKST_DEALING_STATE_NEW == sheetStatu) {
					if (sheetPubInfo.getMainType() == 1) {
						// 投诉单部门内处理追回到部门内审批
						if ("error".equals(recallBmclToSp(sheetPubInfo))) {
							return "error";
						}
					} else {
						// 投诉单协办单追回作废
						recallBmclToZf(sheetPubInfo);
					}
				} else {
					return "error";
				}
			} else {
				return "error";
			}
			flag = true;
		} else {
		/******************************如果为投诉启用新追回开始**************************************/
		if(sheetType==StaticData.SHEET_TYPE_TS_DEAL_NEW){
		    if(StaticData.WKST_REPEAL_STATE_NEW==sheetStatu || StaticData.WKST_DEALING_STATE_NEW==sheetStatu){
	            return workSheetReplevyTsSheet(workSheetId,monthFlag,regionId);
		    }else{
		        return "error";
		    }
		}
		/******************************如果为投诉启用新追回结束**************************************/
		
		//部门处理审批工单
		if(sheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL || sheetType==StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW) {
			//找出审批单以下的工单做完成
			boolean boo = true;
			String checkWhere = " AND CC_WORKSHEET_ALLOT_RELA.CHECK_WORKSHEET_ID='" + workSheetId + "' AND CC_WORKSHEET_ALLOT_RELA.MONTH_FLAG=" + monthFlag;
			WorkSheetAllotReal[] bean = workSheetAlllot.getWorkSheetAllotReal(checkWhere,boo);//取出跟改审批工单关联的处理单
			String allSheetIds = "";
			if(bean.length > 0) {
				while(true) {
					if(bean.length == 0){
						break;
					}
					String dealSheetIds = "";
					String checkSheetIds = "";
					int beanSize = bean.length;
					for(int i = 0; i < beanSize; i++) {
						if("".equals(dealSheetIds)) {
							dealSheetIds = "'" + bean[i].getWorkSheetId() + "'";
							checkSheetIds = "'" + bean[i].getCheckWorkSheet() + "'";
						}else{
							dealSheetIds = dealSheetIds + ",'" + bean[i].getWorkSheetId() + "'";
							checkSheetIds = checkSheetIds + ",'" + bean[i].getCheckWorkSheet() + "'";
						}
					}
					//根据上级工单得到处理工单
					bean = null;
					String quryWhere = " AND CC_WORKSHEET_ALLOT_RELA.PRE_DEAL_WORKSHEET_ID in ( " + dealSheetIds + ")";
					bean = workSheetAlllot.getWorkSheetAllotReal(quryWhere,boo);//取出跟上级工单关联的处理单

					if("".equals(allSheetIds)) {
						allSheetIds = checkSheetIds + "," + dealSheetIds;
					} else {
						allSheetIds = allSheetIds + "," + checkSheetIds + "," + dealSheetIds;
					}
				}//找到所有下级处理单	
				
				//更新所有工单、审批单
				allSheetIds = allSheetIds.replaceAll(workSheetId + ",", ""); //提出当前check单
				allSheetIds = " AND W.WORK_SHEET_ID IN (" + allSheetIds.replaceAll(workSheetId + ",", "") + ")"
				        + " AND W.LOCK_FLAG != 2";
				
				int state = isNewCmp ? StaticData.WKST_DISANNUUL_STATE_NEW : StaticData.WKST_DISANNUUL_STATE;
				int tach = isNewCmp ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
				String stateDesc = pubFunc.getStaticName(state);
				int rows = sheetPubInfoDao.updateDealDisannuul(state, stateDesc, monthFlag, tach, allSheetIds);
				logger.info("共有 " + rows + " 条工单被废除。");
				workSheetAlllot.updateCheckSheet(workSheetId, "工单被追回", monthFlag);
				
				//工单修改成待审批
				if(tachId==StaticData.TACHE_DEAL_NEW){//新投诉改成待处理 
				    this.sheetPubInfoDao.updateSheetState(workSheetId, StaticData.WKST_REPEAL_STATE_NEW,
				            pubFunc.getStaticName(StaticData.WKST_REPEAL_STATE_NEW), 14, 0);                    
				}else{//老投诉 
				    this.sheetPubInfoDao.updateSheetState(workSheetId, StaticData.WKST_ORGAUD_STATE, 
	                        pubFunc.getStaticName(StaticData.WKST_ORGAUD_STATE), 14, 0);
				}
				flag = true;
			}
		}else if(tachId == StaticData.TACHE_AUIT) { 		//审核工单
			int state = StaticData.WKST_DISANNUUL_STATE;
			String stateDesc = this.pubFunc.getStaticName(state);
			
			//部门环节
			this.sheetPubInfoDao.updateTachSheetFinsh(orderId, state, stateDesc, 9, monthFlag, StaticData.TACHE_DEAL);
			this.workSheetAlllot.updateCheckSheet(workSheetId, "工单被追回", monthFlag);
			
			//定单状态
			this.orderAskInfoDao.updateOrderStatu(orderId,StaticData.OR_WAIT_DEAL_STATU,monthFlag,
					this.pubFunc.getStaticName(StaticData.OR_WAIT_DEAL_STATU));
			
			//工单修改成待审核
			this.sheetPubInfoDao.updateSheetState(workSheetId,
					StaticData.WKST_AUD_STATE, this.pubFunc.getStaticName(StaticData.WKST_AUD_STATE),14,0);
			flag = true;
		}
		}
		if(flag) {
			// 记录工单动作			
			SheetActionInfo sheetActionInfo = new SheetActionInfo();
			sheetActionInfo.setWorkSheetId(workSheetId);
			sheetActionInfo.setComments("追回工单");
			sheetActionInfo.setRegionId(sheetPubInfo.getRegionId());
			sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
			sheetActionInfo.setMonth(sheetPubInfo.getMonth());
			saveSheetDealAction(sheetActionInfo, sheetPubInfo.getTacheId(), StaticData.WKST_SHEET_REPLEVY, 7);
			return "success";
		}
		
		//只有审批和审核的工单才有追回功能
		return "TACHERROR";
	}

	private String recallBmclToHtpd(SheetPubInfo sheetPubInfo) {
		boolean isNewCmp = sheetPubInfo.getServType() == StaticData.SERV_TYPE_NEWTS;
		if (!isNewCmp) {
			SheetPubInfo audSheet = sheetPubInfoDao.getAuditSheetNew(sheetPubInfo.getServiceOrderId());
			sheetPubInfoDao.deleteWorkSheetBySheetId(audSheet.getWorkSheetId());
		}
		int state = isNewCmp ? StaticData.WKST_DISANNUUL_STATE_NEW : StaticData.WKST_DISANNUUL_STATE;
		int tachId = isNewCmp ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
		String stateDesc = pubFunc.getStaticName(state);
		String where = " AND W.WORK_SHEET_ID ='" + sheetPubInfo.getWorkSheetId() + "'";
		sheetPubInfoDao.updateSheetDealRequire(sheetPubInfo.getWorkSheetId(), " ", " ", "部门处理被追回到派单", "工单被追回", 46, tachId);
		sheetPubInfoDao.updateDealDisannuul(state, stateDesc, sheetPubInfo.getMonth(), tachId, where);
		String flowSeq = "1";
		String tmpFlowSeq = sheetPubInfo.getFlowSequence();
		if (null != tmpFlowSeq && tmpFlowSeq.length() > 0) {
			flowSeq = tmpFlowSeq;
		}
		int flowSeqNo = Integer.parseInt(flowSeq) + 1;
		if (sheetPubInfo.getMainType() == 1) {//
			Map otherParam = new HashMap();
			otherParam.put("DEAL_REQUIRE", "工单被追回");
			otherParam.put("ROUTE_VALUE", StaticData.ROUTE_DEAL_TO_ASSIGN);
			otherParam.put("DEAL_PR_ORGID", sheetPubInfo.getRetOrgId());
			otherParam.put("DEAL_PR_STAFFID", sheetPubInfo.getRetStaffId());
			otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeqNo));
			submitWorkFlow(sheetPubInfo.getWorkSheetId(), sheetPubInfo.getRegionId(), sheetPubInfo.getMonth(), otherParam);
		}

		String str = " AND source_sheet_id ='" + sheetPubInfo.getWorkSheetId()
				+ "' AND sheet_type IN (700000126, 720130011) AND sheet_statu IN (700000044, 700000045, 720130031, 720130032) ";
		String pdSheetId = "";
		String pdRcvOrgId = "";
		List sheetList = this.sheetPubInfoDao.getSheetCondition(str, true);
		if (sheetList != null && !sheetList.isEmpty()) {
			Map map = (Map) sheetList.get(0);
			pdSheetId = map.get("WORK_SHEET_ID").toString();
			pdRcvOrgId = map.get("RECEIVE_ORG_ID").toString();
			sheetPubInfoDao.updateWorkSheetAreaSheetBySheetId(pdSheetId, pdRcvOrgId, 3, sheetPubInfo.getWorkSheetId());
		}

		TsSheetDealType typeBean = new TsSheetDealType();
		typeBean.setDealTypeId(this.pubFunc.crtGuid());
		typeBean.setOrderId(sheetPubInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetPubInfo.getWorkSheetId());
		typeBean.setDealType("部门处理追回");
		typeBean.setDealTypeDesc("部门处理被追回到派单");
		typeBean.setDealId(0);
		typeBean.setDealDesc("是");
		typeBean.setDealContent("工单被追回");
		typeBean.setMonth(sheetPubInfo.getMonth());
		tsWorkSheetDaoImpl.saveSheetDealType(typeBean);
		return "success";
	}

	private String recallBmclToBmcl(SheetPubInfo sheetPubInfo) {
		boolean isNewCmp = sheetPubInfo.getServType() == StaticData.SERV_TYPE_NEWTS;
		SheetPubInfo newSheetPubInfo = new SheetPubInfo();
		newSheetPubInfo.setServiceOrderId(sheetPubInfo.getServiceOrderId());
		newSheetPubInfo.setRegionId(sheetPubInfo.getRegionId());
		newSheetPubInfo.setRegionName(sheetPubInfo.getRegionName());
		newSheetPubInfo.setServType(sheetPubInfo.getServType());
		newSheetPubInfo.setServTypeDesc(sheetPubInfo.getServTypeDesc());
		newSheetPubInfo.setSourceSheetId(sheetPubInfo.getWorkSheetId());
		newSheetPubInfo.setTacheId(sheetPubInfo.getTacheId());
		newSheetPubInfo.setTacheDesc(sheetPubInfo.getTacheDesc());
		newSheetPubInfo.setWflInstId(sheetPubInfo.getWflInstId());
		newSheetPubInfo.setTacheInstId(sheetPubInfo.getTacheInstId());
		int sheetType = PubFunc.getSheetType(sheetPubInfo.getServType(), sheetPubInfo.getTacheId());
		newSheetPubInfo.setSheetType(sheetType);
		newSheetPubInfo.setSheetTypeDesc(this.pubFunc.getStaticName(sheetType));
		if (isNewCmp) {
			newSheetPubInfo.setSheetType(720130013);
			newSheetPubInfo.setSheetTypeDesc(this.pubFunc.getStaticName(720130013));
		}
		newSheetPubInfo.setSheetPriValue(sheetPubInfo.getSheetPriValue());
		newSheetPubInfo.setSheetRcvDate(sheetPubInfoDao.selectSheetReceiveDate(sheetPubInfo.getWorkSheetId()));
		newSheetPubInfo.setDealLimitTime(sheetPubInfo.getDealLimitTime());
		newSheetPubInfo.setStationLimit(sheetPubInfo.getDealLimitTime());
		newSheetPubInfo.setPreAlarmValue(sheetPubInfo.getPreAlarmValue());
		newSheetPubInfo.setAlarmValue(sheetPubInfo.getAlarmValue());
		newSheetPubInfo.setDealRequire("工单被追回");
		newSheetPubInfo.setAutoVisitFlag(0);
		newSheetPubInfo.setPrecontractSign(1);
		newSheetPubInfo.setRetOrgId(sheetPubInfo.getRetOrgId());
		newSheetPubInfo.setRetOrgName(sheetPubInfo.getRetOrgName());
		newSheetPubInfo.setRetStaffId(sheetPubInfo.getRetStaffId());
		newSheetPubInfo.setRetStaffName(sheetPubInfo.getRetStaffName());
		newSheetPubInfo.setMonth(sheetPubInfo.getMonth());
		newSheetPubInfo.setWorkSheetId(pubFunc.crtSheetId(sheetPubInfo.getRegionId()));
		String flowSeq = "2";
		if (sheetPubInfo.getFlowSequence() != null && !(sheetPubInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetPubInfo.getFlowSequence();
		}
		newSheetPubInfo.setFlowSequence(flowSeq + 1);
		newSheetPubInfo.setRcvOrgId(sheetPubInfo.getRetOrgId());
		newSheetPubInfo.setRcvOrgName(sheetPubInfo.getRetOrgName());
		newSheetPubInfo.setRcvStaffId(sheetPubInfo.getRetStaffId());
		newSheetPubInfo.setRcvStaffName(sheetPubInfo.getRetStaffName());
		newSheetPubInfo.setDealOrgId(sheetPubInfo.getRetOrgId());
		newSheetPubInfo.setDealOrgName(sheetPubInfo.getRetOrgName());
		newSheetPubInfo.setDealStaffId(sheetPubInfo.getRetStaffId());
		newSheetPubInfo.setDealStaffName(sheetPubInfo.getRetStaffName());
		int sheetStatu = isNewCmp ? StaticData.WKST_DEALING_STATE_NEW : StaticData.WKST_ORGDEALING_STATE;
		newSheetPubInfo.setSheetStatu(sheetStatu);
		newSheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(sheetStatu));
		newSheetPubInfo.setLockFlag(1);
		int recRegion = this.pubFunc.getOrgRegion(newSheetPubInfo.getRcvOrgId());
		String recRegionName = this.pubFunc.getRegionName(recRegion);
		newSheetPubInfo.setReceiveRegionId(recRegion);
		newSheetPubInfo.setReceiveRegionName(recRegionName);
		newSheetPubInfo.setMainType(1);
		this.sheetPubInfoDao.saveSheetPubInfo(newSheetPubInfo);

		int workSheetArea = sheetPubInfoDao.selectWorkSheetAreaBySheetId(sheetPubInfo.getSourceSheetId());
		if (workSheetArea == 1) {
			// 投诉单部门处理追回到部门处理
			// 咨询单部门处理追回到部门处理
			sheetPubInfoDao.deleteWorkSheetAreaBySheetId(sheetPubInfo.getWorkSheetId());
			sheetPubInfoDao.updateWorkSheetAreaSheetBySheetId(newSheetPubInfo.getWorkSheetId(), pubFunc.getAreaOrgId(newSheetPubInfo.getRcvOrgId()), 1,
					sheetPubInfo.getSourceSheetId());
		} else if (workSheetArea == 3) {
			// 咨询单部门处理追回到部门处理
			sheetPubInfoDao.updateWorkSheetAreaSheetBySheetId(newSheetPubInfo.getWorkSheetId(), pubFunc.getAreaOrgId(newSheetPubInfo.getRcvOrgId()), 1,
					sheetPubInfo.getWorkSheetId());
		} else {
			// 投诉单部门处理追回到部门内审批
			// 咨询单部门处理追回到部门审批
			SheetPubInfo retSheet = this.sheetPubInfoDao.getSheetPubInfo(sheetPubInfo.getSourceSheetId(), false);
			if (sheetPubInfoDao.selectWorkSheetAreaBySheetId(retSheet.getSourceSheetId()) == 2) {
				// 投诉单部门处理追回到部门处理
				// 咨询单部门处理追回到部门处理
				sheetPubInfoDao.deleteWorkSheetAreaBySheetId(sheetPubInfo.getWorkSheetId());
				sheetPubInfoDao.updateWorkSheetAreaSheetBySheetId(newSheetPubInfo.getWorkSheetId(), pubFunc.getAreaOrgId(newSheetPubInfo.getRcvOrgId()), 1,
						sheetPubInfoDao.selectLastWorkSheetIdByOrderId(sheetPubInfo.getServiceOrderId()));
			}
		}

		int state = isNewCmp ? StaticData.WKST_DISANNUUL_STATE_NEW : StaticData.WKST_DISANNUUL_STATE;
		int tachId = isNewCmp ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
		String stateDesc = pubFunc.getStaticName(state);
		String where = " AND W.WORK_SHEET_ID ='" + sheetPubInfo.getWorkSheetId() + "'";
		sheetPubInfoDao.updateDealDisannuul(state, stateDesc, sheetPubInfo.getMonth(), tachId, where);
		sheetPubInfoDao.updateSheetDealRequire(sheetPubInfo.getWorkSheetId(), " ", " ", "部门处理被追回到部门处理", "工单被追回", 46, tachId);

		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetPubInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetPubInfo.getWorkSheetId());
		typeBean.setDealType("部门处理追回");
		typeBean.setDealTypeDesc("部门处理被追回到部门处理");
		typeBean.setDealId(0);
		typeBean.setDealDesc("是");
		typeBean.setDealContent("工单被追回");
		typeBean.setMonth(sheetPubInfo.getMonth());
		tsWorkSheetDaoImpl.saveSheetDealType(typeBean);
		return "success";
	}

	private String recallBmclToSp(SheetPubInfo sheetPubInfo) {
		String str = " AND source_sheet_id ='" + sheetPubInfo.getWorkSheetId()
				+ "' AND sheet_type IN (700001002, 720130015) AND sheet_statu IN (600000450, 720130033) ";
		String spSheetId = "";
		List sheetList = this.sheetPubInfoDao.getSheetCondition(str, true);
		if (sheetList != null && !sheetList.isEmpty()) {
			boolean isNewCmp = sheetPubInfo.getServType() == StaticData.SERV_TYPE_NEWTS;
			Map map = (Map) sheetList.get(0);
			spSheetId = map.get("WORK_SHEET_ID").toString();
			int spState = isNewCmp ? StaticData.WKST_REPEAL_STATE_NEW : StaticData.WKST_ORGAUD_STATE;
			String spStateDesc = pubFunc.getStaticName(spState);
			this.sheetPubInfoDao.updateSheetState(spSheetId, spState, spStateDesc, 14, 0);

			int state = isNewCmp ? StaticData.WKST_DISANNUUL_STATE_NEW : StaticData.WKST_DISANNUUL_STATE;
			int tachId = isNewCmp ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
			String stateDesc = pubFunc.getStaticName(state);
			String where = " AND W.WORK_SHEET_ID ='" + sheetPubInfo.getWorkSheetId() + "'";
			sheetPubInfoDao.updateSheetDealRequire(sheetPubInfo.getWorkSheetId(), " ", " ", "部门处理被追回到审批", "工单被追回", 46, tachId);
			sheetPubInfoDao.updateDealDisannuul(state, stateDesc, sheetPubInfo.getMonth(), tachId, where);
			workSheetAlllot.updateCheckSheet(spSheetId, "工单被追回", sheetPubInfo.getMonth());

			TsSheetDealType typeBean = new TsSheetDealType();
			String guid = pubFunc.crtGuid();
			typeBean.setDealTypeId(guid);
			typeBean.setOrderId(sheetPubInfo.getServiceOrderId());
			typeBean.setWorkSheetId(sheetPubInfo.getWorkSheetId());
			typeBean.setDealType("部门处理追回");
			typeBean.setDealTypeDesc("部门处理被追回到审批");
			typeBean.setDealId(0);
			typeBean.setDealDesc("是");
			typeBean.setDealContent("工单被追回");// 处理内容
			typeBean.setMonth(sheetPubInfo.getMonth());
			tsWorkSheetDaoImpl.saveSheetDealType(typeBean);// 保存处理类型
			return "success";
		}
		return "error";
	}

	private String recallBmclToZf(SheetPubInfo sheetPubInfo) {
		boolean isNewCmp = sheetPubInfo.getServType() == StaticData.SERV_TYPE_NEWTS;
		int state = isNewCmp ? StaticData.WKST_DISANNUUL_STATE_NEW : StaticData.WKST_DISANNUUL_STATE;
		int tachId = isNewCmp ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
		String stateDesc = pubFunc.getStaticName(state);
		String where = " AND W.WORK_SHEET_ID ='" + sheetPubInfo.getWorkSheetId() + "'";
		sheetPubInfoDao.updateSheetDealRequire(sheetPubInfo.getWorkSheetId(), " ", " ", "协办单追回", "工单被追回", 46, tachId);
		sheetPubInfoDao.updateDealDisannuul(state, stateDesc, sheetPubInfo.getMonth(), tachId, where);

		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetPubInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetPubInfo.getWorkSheetId());
		typeBean.setDealType("部门处理追回");
		typeBean.setDealTypeDesc("协办单被追回");
		typeBean.setDealId(0);
		typeBean.setDealDesc("是");
		typeBean.setDealContent("工单被追回");// 处理内容
		typeBean.setMonth(sheetPubInfo.getMonth());
		tsWorkSheetDaoImpl.saveSheetDealType(typeBean);// 保存处理类型
		return "success";
	}

	/**
	 * 处理环节保存处理内容
	 * @param sheetid 工单ID
	 * @param region 地域ID
	 * @param content 处理结果
	 * @return true 保存成功
	 */
	public boolean saveDealContent(String sheetId,int region,String content) {
		//判断是否是处理中的工单		
		boolean hisFlag = false;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetPubInfo(
				sheetId, hisFlag);
		
		String tm = sheetInfo.getSaveDealContent();
		if(tm == null){
			tm =" ";
		}
		TsmStaff staffInfo = this.pubFunc.getLogonStaff();
		String staffName = staffInfo.getName();
		String staffLonge = staffInfo.getLogonName();
		
		content = content+" -- "+staffName+"("+staffLonge+") "+this.pubFunc.getSysDate()+"\n"+tm;
		int cont = this.sheetPubInfoDao.saveDealContent(sheetId, region, content);
		if(cont > 0) {
			return true;
		}
		return false;
		
	}

	/**
	 * 查询工单的实际处理时限,减去夜间时差
	 * @param sheetId 工单号
	 * @param flag 是否历史	 
	 * @return
	 */
	public String getSheetObjByWorkTime(String sheetid ,boolean flag){
		return sheetPubInfoDao.getSheetObjByWorkTime(sheetid,flag);
	}
	
	/**注意方法的修改
	 * 查询工单的基本信息
	 * @param sheetId 工单号
	 * @param orderId 定单号码
	 * @param regionId 地域ID
	 * @return
	 */
	public ServiceWorkSheetInfo getServiceSheetInfo(String sheetId,String orderId,int regionId,Integer month,boolean flg,Integer vesion) {		
		OrderAskInfo orderAskInfo = null;
		ServiceContent servContent = null;
		OrderCustomerInfo orderCust = null;
		SheetPubInfo sheetInfo = null;
		if(flg){// 查询当前
			// 受理单信息
		    orderAskInfo = orderAskInfoDao.getOrderAskObj(orderId, month, false);
			if (orderAskInfo == null){
				return null;// 如果没有受单号信息则返回空
			}
			// 受理内容
		    servContent = servContentDao.getServContentByOrderId(orderId, false, 0);
			// 受理客户信息
			String custGuid = orderAskInfo.getCustId();
		    orderCust = orderCustInfoDao.getOrderCustByGuid(custGuid, false);
			if(StringUtils.isNotEmpty(sheetId)){
				// 工单信息
			    sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, regionId, month, true);
			    sheetInfo.setWorkTime(sheetPubInfoDao.getSheetObjByWorkTime(sheetId, true));
			}
		}else{// 查询历史
			if(vesion == null) {
				orderAskInfo = orderAskInfoDao.getOrderAskObj(orderId, month, true);
			}
			else {
				orderAskInfo = orderAskInfoDao.getOrderAskInfoByIdMonth(orderId, month, String.valueOf(regionId), true, vesion);
			}
			if (orderAskInfo == null){
				return null;// 如果没有受单号信息则返回空
			}
			// 受理内容
		    servContent = servContentDao.getServContentByOrderId(orderId, true, orderAskInfo.getOrderVer());
			// 受理客户信息
			String custGuid = orderAskInfo.getCustId();
		    orderCust = orderCustInfoDao.getOrderCustByGuid(custGuid, true);
		    if(StringUtils.isNotEmpty(sheetId)){
		    	//工单信息 
			    sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, regionId, month, false);
			    sheetInfo.setWorkTime(sheetPubInfoDao.getSheetObjByWorkTime(sheetId, false));
		    }
		}
		orderAskInfo.setAskFullOrgName(pubFunc.getFullOrgName(orderAskInfo.getAskOrgId(), orderAskInfo.getAskOrgName()));
		
		ServiceWorkSheetInfo workSheetInfo = new ServiceWorkSheetInfo();
		boolean hisFlag = !flg;//历史单为true
		this.setServiceInfo(orderId, hisFlag, orderAskInfo, workSheetInfo);
		
		workSheetInfo.setOrderAskInfo(orderAskInfo);
		workSheetInfo.setServContent(servContent);
		workSheetInfo.setOrderCustInfo(orderCust);
		workSheetInfo.setSheetInfo(sheetInfo);
		return workSheetInfo;
	}

	private void setServiceInfo(String orderId, boolean hisFlag, OrderAskInfo orderAskInfo, ServiceWorkSheetInfo workSheetInfo) {
		ServiceLabel serviceLabel = null;
		if (orderAskInfo.getServiceDate() == 3) {
			serviceLabel = labelManageDAO.queryServiceLabelById(orderId, hisFlag);
		}
		boolean isDapd = true;
		DapdSheetInfo dapdSheetInfo = dapdDao.selectDapdSheetInfoBySheetIdProv(orderId);
		if (null != dapdSheetInfo) {
			String cmplntId = StringUtils.defaultIfEmpty(dapdSheetInfo.getCmplntId(), "");
			if (!"".equals(cmplntId)) {
				orderAskInfo.setUnifiedComplaintCode(cmplntId);
				isDapd = false;
			}
		}
		if (isDapd && serviceLabel != null && serviceLabel.getIsUnified() == 1) {// 投诉编码
			ComplaintUnifiedReturn unifiedInfo = null;
			if (hisFlag) {
				unifiedInfo = cmpUnifiedReturnDAOImpl.queryUnifiedReturnHisByOrderId(orderId);
			} else {
				unifiedInfo = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(orderId);
			}
			if (unifiedInfo != null) {
				orderAskInfo.setUnifiedComplaintCode(unifiedInfo.getUnifiedComplaintCode());
			}
		}
		workSheetInfo.setServiceLabel(serviceLabel);
		workSheetInfo.setDapdSheetInfo(dapdSheetInfo);
	}

	/**
	 * 查询地域LINK_ID
	 * @param region 地域ID
	 * @return
	 */
	public String getRegionLinkId(int region) {
		return this.pubFunc.getRegionLinkId(region);
	}
	
	/**
	 * 更新或保存短信发送列表
	 * @param bean
	 * @param month 1为保存 2为修改
	 * @return
	 */
	public String updateNoteList(NoteSendList bean,int month) {
		int size = 0;
		if(month == 1) {
			bean.setNoteGuid(this.pubFunc.crtGuid());
			size = this.noteSen.saveNoteSendList(bean);
		}
		if(month == 2) {
			size = this.noteSen.updateNoteList(bean);
		}
		if(month == 3) {
			String[] guidArray = bean.getNoteGuid().split("@");
			int longth = guidArray.length;
			for(int i=0;i<longth;i++) {
				size += this.noteSen.deleteNoteList(guidArray[i]);
			}
			
			
		}		
		if(size > 0) {
			return "SUCCESS";
		}
		return "ERROR";
	}

	public String updateReturnList(ReturnBlackList bean) {
		int size = this.noteSen.updateReturnList(bean);
		if(size > 0) {
			return "SUCCESS";
		}
		return "ERROR";
	}

	public String saveReturnList(ReturnBlackList bean) {
		String saveReturn = "ERROR";
		int returnExist = this.noteSen.getReturnExist(bean);
		if (returnExist > 0) {
			saveReturn = "EXIST";
		} else {
			int size = this.noteSen.saveReturnList(bean);
			if (size > 0) {
				saveReturn = "SUCCESS";
			}
		}
		return saveReturn;
	}



	/**
	 * 根据部门id获取不在工单地域机构列表
	 * @param orgIdArr
	 * @param sheetReginId
	 * @return
	 */
	public List getOrgsNotInSheetRegion(String[] orgIdArr,String sheetReginId){
		List reList = new ArrayList();
		for(int i=0;i<orgIdArr.length;i++){ 
			Map lantInfo = this.pubFunc.getLantInfoByOrgId(orgIdArr[i]); 
			if(lantInfo==null || !lantInfo.get("LANT_ID").toString().equals(sheetReginId  ) ){
				reList.add( this.pubFunc.getOrgName(  orgIdArr[i] ) ); 
			}
		} 
		return reList;
	}
	
	/**
	 * 根据staffid获取不在工单地域机构列表
	 * @param staffIds
	 * @param sheetReginId
	 * @return
	 */
	public List getStaffNameNotInSheetRegion(String[] staffIds,String sheetReginId){
		List reList = new ArrayList();
		for(int i=0;i<staffIds.length;i++){ 
			Map lantInfo = this.pubFunc.getLantInfoByStaffId(Integer.parseInt(staffIds[i])); 
			if(lantInfo==null || !lantInfo.get("LANT_ID").toString().equals(sheetReginId  ) ){
				reList.add( this.pubFunc.getStaffName( Integer.parseInt(staffIds[i]))); 
			}
		} 
		return reList;
	}

	@Override
	public int saveSheetTodispatch(SheetTodispatch todispatch) {
		return sheetTodispatchDaoImpl.saveObj(todispatch);
	}
	
	@Override
	public String saveAndqryDealContent(String sheetId,int region,String content,int month,String orderId) {
		boolean flag = saveDealContent(sheetId, region, content);
		JSONObject relJson = new JSONObject();
		if(flag) {
			SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, region, month, true);
			String dealContent = sheetInfo.getSaveDealContent();
			relJson.put("dealContent", dealContent);
		}
		relJson.put("flag", flag);
		return relJson.toString();
	}
	
	public String cancelWorkFlow(String wfInstId) {
		return this.WorkFlowAttemper__FACADE__.cancelWorkFlow(wfInstId) ? "SUCCESS" : "FAIL";
	}

	@Override
	public Map<String, Object> getBuopSheetInfo(String serviceOrderId){
		return businessOpportunityDao.selectBuopSheetInfo(serviceOrderId);
	}

	public Map finishOrderAndSheetByOrderId(String orderId) {
		return workFlowBusiServImpl.finishOrderAndSheetByOrderId(orderId);
	}
}