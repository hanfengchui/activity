package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.businessOpportunity.dao.BusinessOpportunityDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.workflow.IWorkFlowAttemper;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.dao.IworkSheetAllotRealDao;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetStatuApplyInfo;
import com.timesontransfar.customservice.worksheet.service.IBaseDealService;
import com.timesontransfar.staffSkill.service.StaffWorkSkill;

/**
 * 业务处理父类
 * @author 张显
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractDealService implements IBaseDealService{
	private static final Logger logger = LoggerFactory.getLogger(AbstractDealService.class);
	
	protected PubFunc pubFunc;
	protected ISheetPubInfoDao sheetPubInfoDao;
	private IworkSheetAllotRealDao workSheetAlllot;
	private IWorkFlowAttemper workFlowAttemper; 
	private IorderAskInfoDao orderAskInfoDao;
	private ISheetActionInfoDao sheetActionInfoDao;
	private ItsWorkSheetDao tsWorkSheetDao;
	private StaffWorkSkill	staffWorkSkill;
	private IserviceContentDao servContentDao;  
	private InoteSenList noteSen; 
	private IorderCustInfoDao orderCustInfoDao;
	private BusinessOpportunityDao businessOpportunityDao;
    
    /**
     * 工单分派操作的实例
     */
    private WorkSheetAllot workSheetAllot;


	/**
	 * 获取工单对象信息
	 * @param sheetId
	 * @param hisflag
	 * @return
	 */
	public SheetPubInfo querySheetPubInfo(String sheetId, boolean hisflag){ 
		return this.sheetPubInfoDao.getSheetPubInfo(sheetId, hisflag);
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
		
		content = this.pubFunc.getSysDate()+"  ("+staffName+" "+staffLonge +") "+content+"\n"+tm;
		int cont = this.sheetPubInfoDao.saveDealContent(sheetId, region, content);
		return cont > 0;
	} 
	
	/**
	 * 将派单工单退回受量台
	 * 
	 * @param sheetPubInfo
	 *            工单对象
	 * @return 是否成功
	 */
	@SuppressWarnings("unchecked")
	public boolean assignBackToAsk(SheetPubInfo sheetPubInfo, int dealStaff) {
		String sheetId = sheetPubInfo.getWorkSheetId();
		String dealRequire = sheetPubInfo.getDealRequire();
		int quryRegion = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(sheetId, quryRegion, month, hisFlag);
		
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;
		
		TsmStaff staff;
		if(dealStaff != 0){//中台回单
			staff = this.getPubFunc().getStaff(dealStaff);
		}
		else{
			staff = this.getPubFunc().getLogonStaff();
		}
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();
		
		//派单工单退回到工单修改环节
		this.getSheetPubInfoDao().updateSheetDealRequire(sheetId, " ", " ", "退单", dealRequire, 2, StaticData.TACHE_ORDER_ASK);
		
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE", dealRequire);//作为退单要求传到后台
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_ASSIGN_TO_ASK);
		otherParam.put("MONTH_FALG", month);
		otherParam.put("DEAL_PR_ORGID", dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID", dealStaffId);//派发员工
		otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeqNo));//流水号
		return this.submitWorkFlow(sheetId, quryRegion, otherParam);
	}
	
	/**
	 * 提交工作流方法.
	 */
	@SuppressWarnings("unchecked")
	protected boolean submitWorkFlow(String sheetId,int quryRegion,Map otherParam){		
		boolean hisFlag = true;
		String strMonth = "0";
		if(otherParam.containsKey("MONTH_FALG")){
			strMonth = otherParam.get("MONTH_FALG").toString();
		}
		Integer month = Integer.valueOf(strMonth);
		SheetPubInfo sheetPubInfo = this.getSheetPubInfoDao().getSheetObj(sheetId,quryRegion,month,hisFlag);
		if(sheetPubInfo == null){
			logger.warn("没有查询到工单号为 : " + sheetId + "的工单!");
			return false;
		}
		//状态非处理中的工单不能提交
		int sheetState = sheetPubInfo.getLockFlag();
		if(sheetState != 1 ){
			logger.warn("定单号为:" + sheetId + "的工单状态不是处理中,不能提交!");
			return false;
		}
		if(sheetPubInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			logger.warn("定单号为:" + sheetId + "的工单状态不是处理中,不能提交!");
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
		workFlowAttemper.submitWorkFlow(wfInstId, wfNodeInstId, inParam);		
		return true;
	}
	
	/**
	 * 得到审批工单对象
	 * @param sheetPubInfo
	 * @return
	 */
	protected WorkSheetAllotReal[] dealOrgAudSheet(SheetPubInfo sheetPubInfo) {
		String strWhere = "AND cc_worksheet_allot_rela.check_worksheet_id= '"+sheetPubInfo.getWorkSheetId()+"' " +
				"AND cc_worksheet_allot_rela.month_flag="+sheetPubInfo.getMonth()+" and cc_worksheet_allot_rela.main_sheet_flag=1";
		return this.workSheetAlllot.getWorkSheetAllotReal(strWhere, true);
	}
	
	 /**
	 * 保存工单动作
	 * @param sheetActionInfo 工单动作对象
	 * @param tacheId 所处环节
	 * @param actionType 动作类型
	 * @param type 动作 1为自动派发 2 联络部成功 3 提取工单 4 释放工单	5 挂起工单	6 解挂工单
	 * @return
	 */
	protected boolean saveSheetDealAction(SheetActionInfo sheetActionInfo,int tacheId,int actionType,int type) {
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
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId, region, month, hisFlag);
		if(sheetPubInfo == null) {
			return "STATUERROR";//工单状态不是处理中
		}
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
		this.tsWorkSheetDao.saveSheetApply(applyBean, true);
		return "";
	}
	
	/**
	 * 部门处理时解挂工单
	 * @param sheetId 工单id	
	 * @return	解挂工单数量
	 */
	public int unHoldWorkSheet(String sheetId,int region,Integer month) {
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,region,month, hisFlag);
		int state = sheetPubInfo.getSheetStatu();
		// 非挂起状态的单子不能解挂
		if (state != StaticData.WKST_HOLD_STATE) {
			return 0;
		}

		// 更新工单状态为处理中
		int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());
		String stateDesc = pubFunc.getStaticName(sheetStatu);
		this.sheetPubInfoDao.updateSheetState(sheetId,
				sheetStatu, stateDesc,sheetPubInfo.getMonth(),1);
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
	 * 取消工单挂起,释放申请 cancelSheetApply
	 * @param sheetId 工单号
	 * @param region 地域
	 * @param month 月分区
	 * @param canceReason 取消原因
	 * @return
	 */
	public String workSheetCancelApply(String sheetId,int region,Integer month,String canceReason) {
		String strWhere = " AND CC_SHEET_STATU_APPLY.WORKSHEET_ID='"+sheetId+"' "
				+ "AND CC_SHEET_STATU_APPLY.APPLY_AUD_STATU=0";
		WorkSheetStatuApplyInfo[] applyBean = this.tsWorkSheetDao.getsheetApplyObj(strWhere, true);
		if(applyBean.length == 0) {
			return "APPLYERROR";//该申请对象不存在
		}
		//取当前登录员工信息
		TsmStaff staff = this.pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String orgId = staff.getOrganizationId();
		String orgName = staff.getOrgName();
		
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,region,month, hisFlag);
		int size = applyBean.length;
		
		WorkSheetStatuApplyInfo bean = null;
		for(int i=0;i<size;i++) {
			bean = applyBean[i];
			bean.setAudOrg(orgId);
			bean.setAudOrgName(orgName);
			bean.setAudStaff(staffId);
			bean.setAudStaffName(staffName);
			bean.setApplyStatu(3);//取消申请
			bean.setAudResult(canceReason);
			this.tsWorkSheetDao.updateSheetApply(bean);
		}
		int sheetStatu = 0;
		String stateDesc =" ";
		//更新工单状态为处理中
		sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());
		stateDesc = pubFunc.getStaticName(sheetStatu);				
		this.sheetPubInfoDao.updateSheetState(sheetId,sheetStatu, stateDesc,sheetPubInfo.getMonth(),1);	
		return "SUCCESS";
	}
	
	/**
	 * 部门处理转派单提交方法
	 * @param sheetPubInfo	工单对象
	 * @return
	 */
	public String submitDealSheetToDeal(SheetPubInfo[] workSheetObj,String dealResult) {
		if(workSheetObj == null) {
			return "ERROR";
		}
		
		boolean hisFlag = true;
		//取得原工单对象
		SheetPubInfo sheetPubInfo = workSheetObj[0];
		if(sheetPubInfo == null){
			return "SHEETNULL";
		}
		SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(
				sheetPubInfo.getWorkSheetId(),sheetPubInfo.getRegionId(),sheetPubInfo.getMonth(), hisFlag);
		
		//状态非处理中的工单不能提交,状态不是挂起
		int sheetState = sheetInfo.getLockFlag();
		if(sheetState != 1 ){
			logger.warn("定单号为:" + sheetInfo.getWorkSheetId() + "的工单状态不是处理中,不能提交!");
			return "STATUSERROR";
		}
		//判断工单是否挂起,如果作释放,必须先解挂工单
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			logger.warn("定单号为:" + sheetInfo.getWorkSheetId() + "的工单已挂起,不能提交!");
			return "STATUSERROR";//该工单已挂起
		}			
		String allotWorkSheet="";//审批上级工单
		String flowSeq = "2";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		
		if(sheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
			//调用审批单判断审批单的主单单位是否完成
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
		int size = workSheetObj.length;		
		//取登录员工信息
		TsmStaff staff = this.getPubFunc().getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String dealorgId = staff.getOrganizationId();
		String dealorgName = staff.getOrgName();
		String info = "派单员工："+staffName+"  派单人联系电话："+staff.getRelaPhone()+"\n派发意见: "+dealResult;// +"\n"+sheetPubInfo.getDealRequire();//处理要求
		//组装新的工单对象
		SheetPubInfo newSheetPubInfo = new SheetPubInfo();
		newSheetPubInfo.setServiceOrderId(sheetInfo.getServiceOrderId());
		newSheetPubInfo.setRegionId(sheetInfo.getRegionId());
		newSheetPubInfo.setRegionName(sheetInfo.getRegionName());
		newSheetPubInfo.setServType(sheetInfo.getServType());
		newSheetPubInfo.setServTypeDesc(sheetInfo.getServTypeDesc());
		newSheetPubInfo.setSourceSheetId(sheetInfo.getWorkSheetId());
		newSheetPubInfo.setTacheId(sheetInfo.getTacheId());
		newSheetPubInfo.setTacheDesc(sheetInfo.getTacheDesc());
		newSheetPubInfo.setWflInstId(sheetInfo.getWflInstId());
		newSheetPubInfo.setTacheInstId(sheetInfo.getTacheInstId());
		
		newSheetPubInfo.setSheetType(PubFunc.getSheetType(sheetInfo.getServType(), sheetInfo.getTacheId()));
		newSheetPubInfo.setSheetTypeDesc(this.getPubFunc().getStaticName(newSheetPubInfo.getSheetType()));
		
		newSheetPubInfo.setSheetPriValue(sheetInfo.getSheetPriValue());
		newSheetPubInfo.setDealLimitTime(sheetInfo.getDealLimitTime());
		newSheetPubInfo.setPreAlarmValue(sheetInfo.getPreAlarmValue());
		newSheetPubInfo.setAlarmValue(sheetInfo.getAlarmValue());
		newSheetPubInfo.setDealRequire(info);//加上要求填写人的名字,电话
		newSheetPubInfo.setAutoVisitFlag(0);
		//部门转派，新的工单中的派发部门字段记录原处理部门
		newSheetPubInfo.setRetOrgId(dealorgId);
		newSheetPubInfo.setRetOrgName(dealorgName);
		newSheetPubInfo.setRetStaffId(staffId);
		newSheetPubInfo.setRetStaffName(staffName);
					
		newSheetPubInfo.setMonth(sheetInfo.getMonth());
		String workSheet = "";
		int count = 0;
		String strSheetId="";//保存主办单位
		String orgNamelist="";//下派部门的集合
		String[] strSheetList = new String[size];
		SheetPubInfo sheetObj = null;
		int sendType=0;
		for (int i = 0;i < size;i++) {
			workSheet = getPubFunc().crtSheetId(sheetPubInfo.getRegionId());
			sheetObj = workSheetObj[i];
			sendType=0;
			newSheetPubInfo.setWorkSheetId(workSheet);
			newSheetPubInfo.setFlowSequence((flowSeq+(i+1)));//流水号
			
			//如果派到个人,工单状态为处理中,派到部门就为待处理
			if(sheetObj.getRcvOrgId().equals("STFFID")) {
				sendType=1;
				String staOrgId = this.getPubFunc().getStaffOrgName(sheetObj.getRcvStaffId());
				String staOrgName = this.getPubFunc().getOrgName(staOrgId);
				newSheetPubInfo.setRcvOrgId(staOrgId);
				newSheetPubInfo.setRcvOrgName(staOrgName);
				newSheetPubInfo.setRcvStaffId(sheetObj.getRcvStaffId());
				newSheetPubInfo.setRcvStaffName(sheetObj.getRcvStaffName());
				newSheetPubInfo.setDealOrgId(staOrgId);
				newSheetPubInfo.setDealOrgName(staOrgName);
				newSheetPubInfo.setDealStaffId(sheetObj.getRcvStaffId());
				newSheetPubInfo.setDealStaffName(this.getPubFunc().getStaffName(sheetObj.getRcvStaffId()));			
				newSheetPubInfo.setSheetStatu(StaticData.WKST_ORGDEALING_STATE);//更新状态为处理中	
				newSheetPubInfo.setSheetSatuDesc(getPubFunc().getStaticName(StaticData.WKST_ORGDEALING_STATE));	
				newSheetPubInfo.setLockFlag(1);
				if(i==0) {
					orgNamelist = staOrgName+"("+newSheetPubInfo.getDealStaffName()+")";
				} else {
					orgNamelist = orgNamelist+"||"+staOrgName+"("+newSheetPubInfo.getDealStaffName()+")";
				}
			} else {
				newSheetPubInfo.setRcvOrgId(sheetObj.getRcvOrgId());
				newSheetPubInfo.setRcvOrgName(sheetObj.getRcvOrgName());
				newSheetPubInfo.setSheetStatu(StaticData.WKST_REPEAL_STATE);//更新状态为待处理		
				newSheetPubInfo.setSheetSatuDesc(getPubFunc().getStaticName(StaticData.WKST_REPEAL_STATE));	
				
				//  商机管理部门转派
				String result = workSheetAllot.allotToDeal(newSheetPubInfo);
				sendType = WorkSheetAllot.RST_SUCCESS.equals(result) ? 1 : 0;
			}
			//派单人重新定义了时限,就不查配置数据
			if(sheetObj.getDealLimitTime() != 0) {
				newSheetPubInfo.setDealLimitTime(sheetObj.getDealLimitTime());
			}
			//保存产生的部门处理工单的工单数组,给派单关系表用
			strSheetList[i] = workSheet;
						
			//为新加收单地域
			int recRegion = this.getPubFunc().getOrgRegion(newSheetPubInfo.getRcvOrgId());
			String recRegionName = this.getPubFunc().getRegionName(recRegion);
			newSheetPubInfo.setReceiveRegionId(recRegion);
			newSheetPubInfo.setReceiveRegionName(recRegionName);
			int mainFalg = 0;
			if(sheetObj.getMainType() == 1) {
				mainFalg=1;
				strSheetId = workSheet;
			}	
			newSheetPubInfo.setMainType(mainFalg);
			count += this.getSheetPubInfoDao().saveSheetPubInfo(newSheetPubInfo);
			sendNoteCont(newSheetPubInfo,sendType);
		}		
		if(count > 0) {
			//生成一张部门审批工单
			newSheetPubInfo.setFlowSequence(flowSeq+(size+1));//流水号
			workSheet = getPubFunc().crtSheetId(sheetPubInfo.getRegionId());
			newSheetPubInfo.setWorkSheetId(workSheet);
			newSheetPubInfo.setRcvOrgId(dealorgId);//收单部门
			newSheetPubInfo.setRcvOrgName(dealorgName);	
			//取派发员工所在部门本地网地域作为收单地域
			int recReg = this.getPubFunc().getOrgRegion(dealorgId);			
			newSheetPubInfo.setReceiveRegionId(recReg);//收单地域
			newSheetPubInfo.setReceiveRegionName(this.getPubFunc().getRegionName(recReg));
			
			newSheetPubInfo.setSourceSheetId(strSheetId);//处理工单
			newSheetPubInfo.setSheetType(StaticData.SHEET_TYPE_TS_CHECK_DEAL);
			newSheetPubInfo.setSheetTypeDesc(this.getPubFunc().getStaticName(newSheetPubInfo.getSheetType()));
			long dealTime = PubFunc.getTimeBetween(sheetInfo.getLockDate(), this.getPubFunc().getSysDate(), 1);
			int time = sheetInfo.getDealLimitTime() - ((int)dealTime);
			time = (time < 0 ? 0 : time); //2011-08-09 李佳慧新增该语句，修改审批单的时限 
			newSheetPubInfo.setDealLimitTime(time);//审批单为原处理时限减去已处理时限			
			newSheetPubInfo.setSheetStatu(StaticData.WKST_ALLOT_STATE);//更新状态为 已派发
			newSheetPubInfo.setSheetSatuDesc(getPubFunc().getStaticName(StaticData.WKST_ALLOT_STATE));
			newSheetPubInfo.setLockFlag(0);
			//审批单清理收到员工信息，设置收单部门为上级工单的处理部门
			newSheetPubInfo.setRcvStaffId(0);
			newSheetPubInfo.setRcvStaffName(" ");
			newSheetPubInfo.setRcvOrgId(dealorgId);
			newSheetPubInfo.setRcvOrgName(dealorgName);
			//审批单清除处理部门
			newSheetPubInfo.setDealStaffId(0);
			newSheetPubInfo.setDealStaffName(" ");
			newSheetPubInfo.setDealOrgId(" ");
			newSheetPubInfo.setDealOrgName(" ");
			newSheetPubInfo.setLockFlag(0);
			sheetPubInfo.setMainType(0);
			this.getSheetPubInfoDao().saveSheetPubInfo(newSheetPubInfo);			
			//保存到派单关系表中
			WorkSheetAllotReal workSheetAllo = null;
			for(int j = 0;j<size;j++) {
				workSheetAllo = new WorkSheetAllotReal();
				int mainFalg = 0;
				
				if(strSheetId.equals(strSheetList[j])) {
					mainFalg = 1;
				}
				workSheetAllo.setWorkSheetId(strSheetList[j]);
				workSheetAllo.setCheckWorkSheet(workSheet);
				//如果为审批工单,新产生的审批单记录现审批的上级工单
				if(sheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
					workSheetAllo.setPreDealSheet(allotWorkSheet);
				} else {
					workSheetAllo.setPreDealSheet(sheetInfo.getWorkSheetId());
				}				
				workSheetAllo.setCheckFalg(0);
				workSheetAllo.setMainSheetFlag(mainFalg);
				workSheetAllo.setDealStauts("待处理");
				workSheetAllo.setMonth(sheetInfo.getMonth());
				workSheetAllo.setOrderId(sheetInfo.getServiceOrderId());
				this.workSheetAlllot.saveWorkSheetAllotReal(workSheetAllo, true);				
			}			
			// 更新原工单状态为完成
			String stateDesc = getPubFunc().getStaticName(StaticData.WKST_FINISH_STATE);
			
			this.getSheetPubInfoDao().updateSheetState(sheetPubInfo.getWorkSheetId(),
					StaticData.WKST_FINISH_STATE, stateDesc,sheetInfo.getMonth(),2);
			
			this.getSheetPubInfoDao().updateSheetFinishDate(sheetPubInfo.getWorkSheetId());
			this.getSheetPubInfoDao().updateSheetDealRequire(sheetInfo.getWorkSheetId(), info,orgNamelist,"部门转派",info,1,StaticData.TACHE_DEAL);
			return "SUCCESS";
		}
		return "ERROR"; 
	}
	
	private String sendNoteCont(SheetPubInfo bean,int type) {
		NoteSeand noteBean = null;
		String phone = "";
		String client = "0";
		String sheetGuid;
		String relaPerson = "";
		List tmp = null;
		if(type == 0) {
			tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(),null, bean.getTacheId(),0);
		} else {
			tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(),String.valueOf(bean.getDealStaffId()), bean.getTacheId(),1);
		}
		if(tmp == null) {
			return "";
		}
		OrderAskInfo orderAskInfo = this.getOrderAskInfoDao().getOrderAskObj(bean.getServiceOrderId(),bean.getMonth(),false);
		String comment = orderAskInfo.getComment();		
		int size = tmp.size();
		Map map = null;
		if(size > 0) {
			//取当前登录员工信息
			TsmStaff staff = this.getPubFunc().getLogonStaff();
			int staffId = Integer.parseInt(staff.getId());
			String staffName = staff.getName();
			String orgId = staff.getOrganizationId();
			String orgName = staff.getOrgName();

			for(int i=0; i<size; i++) {
				map = (Map) tmp.get(i);
				noteBean = new NoteSeand();
				sheetGuid = this.getPubFunc().crtGuid();
				phone = map.get("RELAPHONE").toString();
				client = map.get("CLIENT_TYPE").toString();
				relaPerson = map.get("RELA_PERSON").toString();
				noteBean.setSheetGuid(sheetGuid);
				noteBean.setRegionId(bean.getRegionId());
				noteBean.setDestteRmid(phone);
				noteBean.setClientType(Integer.parseInt(client));
				noteBean.setSendContent(relaPerson+"您好:有一条新的"+bean.getServTypeDesc()+"单派发到你部门,服务单号为:"+bean.getServiceOrderId()+
						",处理时限为:"+bean.getDealLimitTime()+"小时,受理的内容概述为：" +
						comment + "， 请注意查收.");	
				noteBean.setOrgId(orgId);
				noteBean.setOrgName(orgName);
				noteBean.setStaffId(staffId);
				noteBean.setStaffName(staffName);
				noteBean.setBusiId(bean.getWorkSheetId());
				this.noteSen.saveNoteContent(noteBean);
			}
		}		
		return "";
	}
	
	/**
	 * @return the pubFunc
	 */
	public PubFunc getPubFunc() {
		return pubFunc;
	}

	/**
	 * @param pubFunc the pubFunc to set
	 */
	public void setPubFunc(PubFunc pubFunc) {
		this.pubFunc = pubFunc;
	}

	/**
	 * @return the sheetPubInfoDao
	 */
	public ISheetPubInfoDao getSheetPubInfoDao() {
		return sheetPubInfoDao;
	}

	/**
	 * @param sheetPubInfoDao the sheetPubInfoDao to set
	 */
	public void setSheetPubInfoDao(ISheetPubInfoDao sheetPubInfoDao) {
		this.sheetPubInfoDao = sheetPubInfoDao;
	}

	/**
	 * @return the workFlowAttemper
	 */
	public IWorkFlowAttemper getWorkFlowAttemper() {
		return workFlowAttemper;
	}

	/**
	 * @param workFlowAttemper the workFlowAttemper to set
	 */
	public void setWorkFlowAttemper(IWorkFlowAttemper workFlowAttemper) {
		this.workFlowAttemper = workFlowAttemper;
	}

	/**
	 * @return the workSheetAlllot
	 */
	public IworkSheetAllotRealDao getWorkSheetAlllot() {
		return workSheetAlllot;
	}

	/**
	 * @param workSheetAlllot the workSheetAlllot to set
	 */
	public void setWorkSheetAlllot(IworkSheetAllotRealDao workSheetAlllot) {
		this.workSheetAlllot = workSheetAlllot;
	}

	/**
	 * @return the orderAskInfoDao
	 */
	public IorderAskInfoDao getOrderAskInfoDao() {
		return orderAskInfoDao;
	}

	/**
	 * @return the BusinessOpportunityDao
	 */

	public BusinessOpportunityDao getBusinessOpportunityDao() {
		return businessOpportunityDao;
	}

	public void setBusinessOpportunityDao(BusinessOpportunityDao businessOpportunityDao) {
		this.businessOpportunityDao = businessOpportunityDao;
	}

	/**
	 * @param orderAskInfoDao the orderAskInfoDao to set
	 */
	public void setOrderAskInfoDao(IorderAskInfoDao orderAskInfoDao) {
		this.orderAskInfoDao = orderAskInfoDao;
	}

	/**
	 * @return the sheetActionInfoDao
	 */
	public ISheetActionInfoDao getSheetActionInfoDao() {
		return sheetActionInfoDao;
	}

	/**
	 * @param sheetActionInfoDao the sheetActionInfoDao to set
	 */
	public void setSheetActionInfoDao(ISheetActionInfoDao sheetActionInfoDao) {
		this.sheetActionInfoDao = sheetActionInfoDao;
	}

	/**
	 * @return the tsWorkSheetDao
	 */
	public ItsWorkSheetDao getTsWorkSheetDao() {
		return tsWorkSheetDao;
	}

	/**
	 * @param tsWorkSheetDao the tsWorkSheetDao to set
	 */
	public void setTsWorkSheetDao(ItsWorkSheetDao tsWorkSheetDao) {
		this.tsWorkSheetDao = tsWorkSheetDao;
	}

	/**
	 * @return the staffWorkSkill
	 */
	public StaffWorkSkill getStaffWorkSkill() {
		return staffWorkSkill;
	}

	/**
	 * @param staffWorkSkill the staffWorkSkill to set
	 */
	public void setStaffWorkSkill(StaffWorkSkill staffWorkSkill) {
		this.staffWorkSkill = staffWorkSkill;
	}

	/**
	 * @return the servContentDao
	 */
	public IserviceContentDao getServContentDao() {
		return servContentDao;
	}

	/**
	 * @param servContentDao the servContentDao to set
	 */
	public void setServContentDao(IserviceContentDao servContentDao) {
		this.servContentDao = servContentDao;
	}

	/**
	 * @return the noteSen
	 */
	public InoteSenList getNoteSen() {
		return noteSen;
	}

	/**
	 * @param noteSen the noteSen to set
	 */
	public void setNoteSen(InoteSenList noteSen) {
		this.noteSen = noteSen;
	}
	/**
	 * @return the orderCustInfoDao
	 */
	public IorderCustInfoDao getOrderCustInfoDao() {
		return orderCustInfoDao;
	}

	/**
	 * @param orderCustInfoDao the orderCustInfoDao to set
	 */
	public void setOrderCustInfoDao(IorderCustInfoDao orderCustInfoDao) {
		this.orderCustInfoDao = orderCustInfoDao;
	}

	public void setWorkSheetAllot(WorkSheetAllot workSheetAllot) {
        this.workSheetAllot = workSheetAllot;
    }

    public WorkSheetAllot getWorkSheetAllot() {
        return workSheetAllot;
    }
}
