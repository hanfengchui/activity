package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;
import com.timesontransfar.customservice.worksheet.service.IYnSheetDealService;


/**
 * 业务预受理 业务类
 * 说明:如果父类公用方法中需要在页面中使用时,一定要在IPreDealService接口中定义该方法
 * @author 张显
 *
 */
@SuppressWarnings("rawtypes")
public class YnSheetDealServiceImpl extends AbstractDealService implements IYnSheetDealService{
	
	//工作流bean 
	 
	/**
	 * 审核环节重新派单(待修改)
	 * 
	 * @param sheetPubInfo
	 *            工单对像
	 * @return 是否成功
	 */
	@SuppressWarnings("unchecked")
	public String submitAuitSheetToDeal(SheetPubInfo[] workSheetObj,String acceptContent) {
		if(workSheetObj == null) {
			return "ERROR";
		}
		SheetPubInfo sheetPubInfo = workSheetObj[0];
		int size = workSheetObj.length;
		String sheetId = sheetPubInfo.getWorkSheetId();
		String require = sheetPubInfo.getDealRequire();
		int region = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();

		SheetPubInfo sheetInfo = super.getSheetPubInfoDao().getSheetObj(sheetId, region,month, true);
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
			TsmStaff staff = super.getPubFunc().getLogonStaff();
			String dealOrg = staff.getOrganizationId();
			String dealStaffId = staff.getId();	
			
			String rcvOrgName = "";
			for(int i=0;i<size;i++) {
				sheetPubInfo = workSheetObj[i];
				if(i == 0) {
					if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
						String orgId = super.getPubFunc().getStaffOrgName(sheetPubInfo.getRcvStaffId());
						rcvOrgName = super.getPubFunc().getOrgName(orgId)+"("+sheetPubInfo.getRcvStaffName()+")";
					} else {
						rcvOrgName = sheetPubInfo.getRcvOrgName();
					}
					continue;
				}
				
				if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
					String orgId = super.getPubFunc().getStaffOrgName(sheetPubInfo.getRcvStaffId());
					rcvOrgName =rcvOrgName + "||" + super.getPubFunc().getOrgName(orgId)+"("+sheetPubInfo.getRcvStaffName()+")";
				} else {
					rcvOrgName =rcvOrgName+"||"+ sheetPubInfo.getRcvOrgName();
				}
				
			}	
			
		//记录操作类型,操作内容
		super.getSheetPubInfoDao().updateSheetDealRequire(sheetId,require,rcvOrgName,"审核环节重新派发工单",acceptContent ,10,StaticData.TACHE_DEAL);		
			
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE",require);
		otherParam.put("ROUTE_VALUE",StaticData.ROUTE_AUIT_TO_DEAL);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工	
		otherParam.put("SHEETARRAY", workSheetObj);
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		//提交流程
		 this.submitWorkFlow(sheetId,region, otherParam);	
		 return "SUCCESS";
	}
	/**
	 * 疑难审核环节归档，不走工作流的自动回访环节
	 * @param retVisitResult
	 * @param sheetId
	 * @param orgId
	 * @param region
	 * @param month
	 * @return
	 */	
	@SuppressWarnings("unchecked")
	@Transactional
	public String auitSheetFinish(RetVisitResult retVisitResult,
			String sheetId, String orgId,int regionId,Integer month) {
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(
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
		TsmStaff staffObj = getPubFunc().getLogonStaff();
		String dealOrg = staffObj.getOrganizationId();
		String dealStaffId = staffObj.getId();
		Map otherParam = new HashMap();
		
		//记录操作类型,操作内容
		this.getSheetPubInfoDao().updateSheetDealRequire(sheetId,sheetInfo.getDealRequire(),"","审核环节直接竣工",retVisitResult.getRetVisitContent(),11,StaticData.TACHE_FINISH);
		this.getOrderAskInfoDao().updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
		
		//疑难工单更新定性
		this.getOrderAskInfoDao().updateQualiative(retVisitResult.getConclusionId(), 
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
	@SuppressWarnings("unchecked")
	public String submitAuitSheet(OrderAskInfo orderAskInfo, String sheetId,
			int autoVisitFlag, String dealRequire, String orgId) {
	
		boolean hisFlag = true;
		Integer month = orderAskInfo.getMonth(); 
		int region = orderAskInfo.getRegionId();
		SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(
				sheetId,region,month,hisFlag);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交 工单状态不为挂起
		if (state != 1 ) {
			return "STATUDERROR";
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
			WorkSheetAllotReal sheetAllotRealobj = null;
			int sizeAllot = 0;
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
		
		TsmStaff staff = this.getPubFunc().getLogonStaff();
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();
		
		String oldRequire = "";
		if(null != sheetInfo.getDealRequire()){
			oldRequire = sheetInfo.getDealRequire();
		}
        //记录操作类型,操作内容
		this.getSheetPubInfoDao().updateSheetDealRequire(sheetId,oldRequire,"","审核环节审核同意", dealRequire ,8,StaticData.TACHE_FINISH);		
		
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE",dealRequire);
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_GOTO_NEXT);
		otherParam.put("AUTO_VISIT_FLAG",autoVisitFlag);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		this.submitWorkFlow(sheetId,region,otherParam);
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
		String orgId = this.getPubFunc().getLogonStaff().getOrganizationId();		
		int regionId = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();
		//取得员工所在部门的地域
		int staffIegion =this.getPubFunc().getOrgRegion(orgId);
		//本地网地域
		int upRegion = this.getPubFunc().getUpRegionId(staffIegion);
		if(upRegion == 1 ) {
			upRegion = staffIegion;
		}
		
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(
				sheetId,regionId,month, hisFlag);
		int state = sheetInfo.getLockFlag();
		
		// 非处理中状态的单子不能提交 工单状态不为挂起
		if (state != 1  ) {
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
				
		this.getWorkSheetAlllot().updateWorkSheetAllotReal(workSheetAllotReal);
		//根据要更新派单关系表的处理工单来得到派单关系表中的对象
		//得到workSheetAlllot对象,在判断审批工单是否为审核单,标志为主单
		WorkSheetAllotReal sheetAllotObj = this.getWorkSheetAlllot().getSheetAllotObj(workSheetAllotReal.getWorkSheetId(), month);
		//判断sheetAllotObj不为空
		boolean boo = true;
		//得到审批或审核工单的工单对象
		SheetPubInfo orderSheetInfo = this.getSheetPubInfoDao().getSheetObj(
				sheetAllotObj.getCheckWorkSheet(),regionId,month, boo);
		int orderTach = 0;//审批或审核工单的环节
		int orderSheetType=0;
		String orderSheetId="";
		Integer orderMoth=1;
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
/*			this.getSheetPubInfoDao().updateSheetState(orderSheetId,
					StaticData.WKST_ORGAUD_STATE, this.getPubFunc().getStaticName(StaticData.WKST_ORGAUD_STATE),orderMoth,0);*/
			this.getSheetPubInfoDao().updateSheetState(orderSheetId,
					StaticData.WKST_ORGAUD_STATE, this.getPubFunc().getStaticName(StaticData.WKST_ORGAUD_STATE),14,0);
			
		}
		//在派单关系表为主办,审批工单在工单表中的环节为审核环节 工单修改成待审核
		boolean audBoo = false;//判断是否可以进行自动归档
		if(mainType == 1 && orderTach == StaticData.TACHE_AUIT) {
			this.getOrderAskInfoDao().updateOrderStatu(sheetInfo.getServiceOrderId(),StaticData.OR_WAIT_DEAL_STATU,month,
					this.getPubFunc().getStaticName(StaticData.OR_WAIT_DEAL_STATU));
			//工单修改成待审核 带月分区14进去，修改LOCK_DATE时间
			this.getSheetPubInfoDao().updateSheetState(orderSheetId,
					StaticData.WKST_AUD_STATE, this.getPubFunc().getStaticName(StaticData.WKST_AUD_STATE),14,0);
			audBoo = true;
		}		
		String stateDesc = this.getPubFunc().getStaticName(StaticData.WKST_FINISH_STATE);
		this.getSheetPubInfoDao().updateSheetState(sheetId,
				StaticData.WKST_FINISH_STATE, stateDesc,month,2);
		this.getSheetPubInfoDao().updateSheetFinishDate(sheetId);
		//更新操作
		this.getSheetPubInfoDao().updateSheetDealRequire(sheetId, dealResult," ",dealType,dealResult,dealId,10);//10无下一环节
		if(audBoo) {

			//如果是疑难工单中绿色通道，自动归档，不进行审核
			String flowOrderId =sheetInfo.getServiceOrderId();
			OrderAskInfo orderAskInfo = this.getOrderAskInfoDao().getOrderAskObj(flowOrderId,orderMoth,false);//得到定单对象
			
			boolean finiBoo = false;
			
			if(orderAskInfo.getServiceDate()==0 && orderAskInfo.getUrgencyGrade() == StaticData.SERV_GRADE_VERY_BICE_DYPASS) {
				finiBoo = true;
			}	else {
				ServiceContent servContent = this.getServContentDao().getServContentByOrderId(orderAskInfo.getServOrderId(), false,0);
				if(servContent.getAppealProdId() == StaticData.APPEAL_PROD_ID_YN_HB) {
					finiBoo = true;
				}
			}
			if(this.finishFlag(finiBoo, orderSheetInfo)) {
				this.getSheetPubInfoDao().updateSheetDealRequire(sheetId, dealResult," ",dealType,dealResult,dealId,StaticData.TACHE_FINISH);
				//记录定单竣工的环节
				this.getOrderAskInfoDao().updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
				this.getSheetPubInfoDao().updateFetchSheetStaff(orderSheetId, 0, "SYSTEM", "SYSTEM", "SYSTEM");
				Map otherParam = new HashMap();
				TsmStaff staffObj = this.getPubFunc().getLogonStaff();
				String dealOrg = staffObj.getOrganizationId();
				String dealStaffId = staffObj.getId();
				otherParam.put("ROUTE_VALUE", StaticData.ROUTE_AUD_TO_FINISH);
				otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
				otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
				otherParam.put("SERV_ORDER_ID",flowOrderId);
				otherParam.put("SHEET_ID",orderSheetId);
				otherParam.put("WF__REGION_ID",String.valueOf(regionId));
				otherParam.put("MONTH_FALG",month);
				this.getWorkFlowAttemper().submitWorkFlow(orderSheetInfo.getWflInstId(), orderSheetInfo.getTacheInstId(), otherParam);					
			}	
		}
		return "success";
	}
	
	public String submitDealSheetToDealNew(SheetPubInfo[] workSheetObj,String dealResult){
		return submitDealSheetToDeal(workSheetObj, dealResult);
	}
	
	private boolean finishFlag(boolean finiBoo, SheetPubInfo orderSheetInfo) {
		return finiBoo && orderSheetInfo != null;
	}
	
}
