package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.paramconfig.service.IsheetLimitTimeService;
import com.timesontransfar.customservice.workFlowOrg.service.IsheetFlowOrg;
import com.timesontransfar.customservice.worksheet.dao.ISJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;
import com.timesontransfar.customservice.worksheet.service.IPreDealService;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;

/**
 * 业务预受理 业务类
 * 说明:如果父类公用方法中需要在页面中使用时,一定要在IPreDealService接口中定义该方法
 * @author 张显
 */
@SuppressWarnings("rawtypes")
public class PreDealServiceImpl extends AbstractDealService implements IPreDealService{
	private static final Logger logger = LoggerFactory.getLogger(PreDealServiceImpl.class);
	
	private IsheetLimitTimeService sheetLimitTimeServ;//时限
	private IsheetFlowOrg sheetFlowOrg;//流向 
	private ISJSheetQualitative sjSheetQualitative;
	
	@Autowired
	private IworkSheetBusi workSheetBusi;

	public static final String SUCCESS = "success";
	public static final String STATUSERROR = "statusError";
	
	@Override
	public String assignBackToAskFull(SheetPubInfo sheetPubInfo, int dealStaff) {
		//1: 回退
		boolean boo = super.assignBackToAsk(sheetPubInfo, dealStaff);

		//2: 退单给受理人发短信
		workSheetBusi.sendNoteToAccept(sheetPubInfo.getRcvStaffId(), sheetPubInfo.getRegionId(), sheetPubInfo.getWorkSheetId(), sheetPubInfo.getDealContent());
		return boo ? "success" : "fail";
	}
	
	@Override
	public String submitDealSheetToDeal(SheetPubInfo[] workSheetObj,String dealResult) {
		return super.submitDealSheetToDeal(workSheetObj,dealResult);
	}
	
	@Override
	public String saveDealContent(String sheetId, String regionId, String content) {
		// 暂存处理内容
		boolean boo = super.saveDealContent(sheetId,Integer.valueOf(regionId),content);
		if(boo) {
		   SheetPubInfo workSheetObj = super.sheetPubInfoDao.getSheetPubInfo(sheetId, false);
		   logger.info("返回的暂存处理内容为：{}",workSheetObj.getSaveDealContent());
		   return workSheetObj.getSaveDealContent();
		}
		return "";
	}
	
	@Override
	public String workSheetStatuApply(String sheetId, int regionId, int month, String applyReason,int applyType) {
		return super.workSheetStatuApply(sheetId, regionId, month, applyReason, applyType);
	}
	
	@Override
	public int unHoldWorkSheet(String sheetId,int region,int month) {
		return super.unHoldWorkSheet(sheetId, region, month);
	}
	
	@Override
	public String workSheetCancelApply(String sheetId,int region,int month,String applyReason) {
		return super.workSheetCancelApply(sheetId, region, month,applyReason);
	}
	

	/**
	 * 预受理部门处理单提交方法
	 * sheetPubInfo 工单对象
	 * orderAskInfo  定单对象
	 * @return	是否成功
	 */
	@Override
	public String submitDealSheetYs(SheetPubInfo sheetPubInfo,OrderAskInfo orderAskInfo) {
				String sheetId = sheetPubInfo.getWorkSheetId();
				int regionId = sheetPubInfo.getRegionId();
				Integer month = sheetPubInfo.getMonth();
				boolean hisFlag = true;
				SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(
						sheetId,regionId,month, hisFlag);
				int state = sheetInfo.getLockFlag();
				
				// 非处理中状态的单子不能提交 工单状态不为挂起
				if (state != 1) {
					return STATUSERROR;
				}
				if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
					return STATUSERROR;
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
						
				getWorkSheetAlllot().updateWorkSheetAllotReal(workSheetAllotReal);
				//根据要更新派单关系表的处理工单来得到派单关系表中的对象
				//得到workSheetAlllot对象,在判断审批工单是否为审核单,标志为主单
				WorkSheetAllotReal sheetAllotObj = getWorkSheetAlllot().getSheetAllotObj(workSheetAllotReal.getWorkSheetId(), month);
				if(sheetAllotObj == null) {
					return STATUSERROR;
				}
				
				boolean boo = true;
				//得到审批或审核工单的工单对象
				SheetPubInfo orderSheetInfo = this.getSheetPubInfoDao().getSheetObj(
						sheetAllotObj.getCheckWorkSheet(),regionId,month, boo);
				int orderTach = 0;//审批或审核工单的环节
				if(orderSheetInfo != null) {
					orderTach = orderSheetInfo.getTacheId();
				}
				int mainType = sheetAllotObj.getMainSheetFlag();//在派单关系表的主办标志
				
				if(mainType==1){
				    String result = "";
				    if(orderSheetInfo != null) {
					    if(orderSheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL){
					        // 处理完成 自动回单
			                result = getWorkSheetAllot().allotToApprove(orderSheetInfo);
			                if(WorkSheetAllot.RST_NONE.equals(result)){
			                    orderSheetInfo.setSheetStatu(StaticData.WKST_ORGAUD_STATE);
			                    orderSheetInfo.setSheetSatuDesc(this.getPubFunc().getStaticName(StaticData.WKST_ORGAUD_STATE));
			                    orderSheetInfo.setMonth(14);
			                    orderSheetInfo.setLockFlag(0);
			                }else{
			                    this.getSheetPubInfoDao().updateFetchSheetStaff(orderSheetInfo.getWorkSheetId(), orderSheetInfo.getRcvStaffId(),
			                            orderSheetInfo.getRcvStaffName(),orderSheetInfo.getRcvOrgId(),orderSheetInfo.getRcvOrgName());
			                }
			                this.getSheetPubInfoDao().updateSheetState(orderSheetInfo.getWorkSheetId(),orderSheetInfo.getSheetStatu(),
			                        orderSheetInfo.getSheetSatuDesc(),14,orderSheetInfo.getLockFlag());
					    }else if(orderTach == StaticData.TACHE_AUIT){
					        getOrderAskInfoDao().updateOrderStatu(sheetInfo.getServiceOrderId(),StaticData.OR_WAIT_DEAL_STATU,month,
		                            this.getPubFunc().getStaticName(StaticData.OR_WAIT_DEAL_STATU));
					        result = getWorkSheetAllot().allotToVerify(orderSheetInfo);
					        if(WorkSheetAllot.RST_NONE.equals(result)){
		                        orderSheetInfo.setSheetStatu(StaticData.WKST_AUD_STATE);
		                        orderSheetInfo.setSheetSatuDesc(this.getPubFunc().getStaticName(StaticData.WKST_AUD_STATE));
		                        orderSheetInfo.setMonth(14);
		                        orderSheetInfo.setLockFlag(0);
		                    }else{
		                        this.getSheetPubInfoDao().updateFetchSheetStaff(orderSheetInfo.getWorkSheetId(), orderSheetInfo.getRcvStaffId(),
		                                orderSheetInfo.getRcvStaffName(),orderSheetInfo.getRcvOrgId(),orderSheetInfo.getRcvOrgName());
		                    }
					        this.getSheetPubInfoDao().updateSheetState(orderSheetInfo.getWorkSheetId(),orderSheetInfo.getSheetStatu(),
					                orderSheetInfo.getSheetSatuDesc(),14,orderSheetInfo.getLockFlag());
					    }
				    }
				}
				
				String stateDesc =this.getPubFunc().getStaticName(StaticData.WKST_FINISH_STATE);
				this.getSheetPubInfoDao().updateSheetState(sheetId,
						StaticData.WKST_FINISH_STATE, stateDesc,month,2);
				this.getSheetPubInfoDao().updateSheetFinishDate(sheetId);
				//更新操作
				this.getSheetPubInfoDao().updateSheetDealRequire(sheetId, sheetInfo.getDealRequire()," ",
						sheetPubInfo.getServTypeDesc()+"部门处理回单",sheetPubInfo.getDealContent(),sheetInfo.getServType()-1,10);
				getOrderAskInfoDao().updateCrmAskSheet(orderAskInfo);/*在CRM系统中受理是否成功*/
				return SUCCESS;
	}


	/**
	 * 预受理部门处理单提交方法
	 * sheetPubInfo 工单对象
	 * orderAskInfo  定单对象
	 * @return	是否成功
	 */
	@Override
	@SuppressWarnings("all")
	public String submitDealSheetYsNew(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo) {
		String sheetId = sheetPubInfo.getWorkSheetId();
		int regionId = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(
				sheetId,regionId,month, hisFlag);
		int state = sheetInfo.getLockFlag();

		// 非处理中状态的单子不能提交 工单状态不为挂起
		if (state != 1) {
			return STATUSERROR;
		}
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return STATUSERROR;
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

		getWorkSheetAlllot().updateWorkSheetAllotReal(workSheetAllotReal);
		//根据要更新派单关系表的处理工单来得到派单关系表中的对象
		//得到workSheetAlllot对象,在判断审批工单是否为审核单,标志为主单
		WorkSheetAllotReal sheetAllotObj = getWorkSheetAlllot().getSheetAllotObj(workSheetAllotReal.getWorkSheetId(), month);
		if(sheetAllotObj == null) {
			return STATUSERROR;
		}

		boolean boo = true;
		//得到审批或审核工单的工单对象
		SheetPubInfo orderSheetInfo = this.getSheetPubInfoDao().getSheetObj(
				sheetAllotObj.getCheckWorkSheet(),regionId,month, boo);
		int orderTach = 0;//审批或审核工单的环节
		if(orderSheetInfo != null) {
			orderTach = orderSheetInfo.getTacheId();
		}
		int mainType = sheetAllotObj.getMainSheetFlag();//在派单关系表的主办标志

		if(mainType==1){
			String result = "";
			if(orderSheetInfo != null) {
				if(orderSheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL){
					// 处理完成 自动回单
					result = getWorkSheetAllot().allotToApprove(orderSheetInfo);
					if(WorkSheetAllot.RST_NONE.equals(result)){
						orderSheetInfo.setSheetStatu(StaticData.WKST_ORGAUD_STATE);
						orderSheetInfo.setSheetSatuDesc(this.getPubFunc().getStaticName(StaticData.WKST_ORGAUD_STATE));
						orderSheetInfo.setMonth(14);
						orderSheetInfo.setLockFlag(0);
					}else{
						this.getSheetPubInfoDao().updateFetchSheetStaff(orderSheetInfo.getWorkSheetId(), orderSheetInfo.getRcvStaffId(),
								orderSheetInfo.getRcvStaffName(),orderSheetInfo.getRcvOrgId(),orderSheetInfo.getRcvOrgName());
					}
					this.getSheetPubInfoDao().updateSheetState(orderSheetInfo.getWorkSheetId(),orderSheetInfo.getSheetStatu(),
							orderSheetInfo.getSheetSatuDesc(),14,orderSheetInfo.getLockFlag());
					//修改商机单
					this.getBusinessOpportunityDao().updateBuopSheetInfo(buopSheetInfo);
				}else if(orderTach == StaticData.TACHE_AUIT){
					getOrderAskInfoDao().updateOrderStatu(sheetInfo.getServiceOrderId(),StaticData.OR_WAIT_DEAL_STATU,month,
							this.getPubFunc().getStaticName(StaticData.OR_WAIT_DEAL_STATU));
					result = getWorkSheetAllot().allotToVerify(orderSheetInfo);
					if(WorkSheetAllot.RST_NONE.equals(result)){
						orderSheetInfo.setSheetStatu(StaticData.WKST_AUD_STATE);
						orderSheetInfo.setSheetSatuDesc(this.getPubFunc().getStaticName(StaticData.WKST_AUD_STATE));
						orderSheetInfo.setMonth(14);
						orderSheetInfo.setLockFlag(0);
					}else{
						this.getSheetPubInfoDao().updateFetchSheetStaff(orderSheetInfo.getWorkSheetId(), orderSheetInfo.getRcvStaffId(),
								orderSheetInfo.getRcvStaffName(),orderSheetInfo.getRcvOrgId(),orderSheetInfo.getRcvOrgName());
					}
					//修改商机单
					this.getBusinessOpportunityDao().updateBuopSheetInfo(buopSheetInfo);
					this.getSheetPubInfoDao().updateSheetState(orderSheetInfo.getWorkSheetId(),orderSheetInfo.getSheetStatu(),
							orderSheetInfo.getSheetSatuDesc(),14,orderSheetInfo.getLockFlag());
				}
			}
		}

		String stateDesc =this.getPubFunc().getStaticName(StaticData.WKST_FINISH_STATE);
		this.getSheetPubInfoDao().updateSheetState(sheetId,
				StaticData.WKST_FINISH_STATE, stateDesc,month,2);
		this.getSheetPubInfoDao().updateSheetFinishDate(sheetId);
		//更新操作
		this.getSheetPubInfoDao().updateSheetDealRequire(sheetId, sheetInfo.getDealRequire()," ",
				sheetPubInfo.getServTypeDesc()+"部门处理回单",sheetPubInfo.getDealContent(),sheetInfo.getServType()-1,10);
		getOrderAskInfoDao().updateCrmAskSheet(orderAskInfo);/*在CRM系统中受理是否成功*/
		return SUCCESS;
	}
	
	/**
	 * 预受理工单完成
	 * @param sheetPubInfo 工单对象
	 * @param orderAskInfo 定单对象 必须带联系电话
	 * @param boo 是否发送短信 true 发送短信 false 不送短信
	 * @param buopSheetInfo 商机单对象
	 * @param sendContent 发送内容  您办理的***业务已经受理成功，将于 “时间” 生效。
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String updateBeforSheetNew(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, boolean boo, String sendContent, int dealStaff) {
		String sheetId = sheetPubInfo.getWorkSheetId();
		int regionId = sheetPubInfo.getRegionId();
		String orderId = sheetPubInfo.getServiceOrderId();
		Integer month = sheetPubInfo.getMonth();
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(sheetId, regionId, month, hisFlag);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交 工单状态不为挂起
		if (state != 1) {
			return STATUSERROR;
		}
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return STATUSERROR;
		}
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

		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE","");
		otherParam.put("ROUTE_VALUE",StaticData.ROUTE_AUD_TO_YSFINISH);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		otherParam.put("SERV_ORDER_ID",orderId);
		otherParam.put("SHEET_ID",sheetId);
		otherParam.put("WF__REGION_ID",sheetPubInfo.getRegionId());

		String wfInstId = sheetInfo.getWflInstId();
		String wfNodeInstId = sheetInfo.getTacheInstId();
		getOrderAskInfoDao().updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
		//更新操作
		this.getSheetPubInfoDao().updateSheetDealRequire(sheetId, ""," ",sheetInfo.getServTypeDesc()+"完成",sheetPubInfo.getDealContent(),sheetInfo.getServType()+1,StaticData.TACHE_FINISH);
		this.getOrderAskInfoDao().updateCrmAskSheet(orderAskInfo);/*在CRM系统中受理是否成功*/
		//新增商机单修改
		this.getBusinessOpportunityDao().updateBuopSheetInfo(buopSheetInfo);/*更新商机单*/
		this.getWorkFlowAttemper().submitWorkFlow(wfInstId, wfNodeInstId, otherParam);
		if(boo) {
			String content = "尊敬的客户：您好"+sendContent+"\n感谢您使用中国电信业务！";
			sendNoteCont(orderAskInfo.getRelaInfo(), regionId, content, sheetPubInfo.getServiceOrderId());
		}
		return SUCCESS;
	}

	/**
	 * 商机单后台派单环节直接处理增加办结原因
	 * @param sheetPubInfo 工单对象
	 * @param orderAskInfo 定单对象 必须带联系电话
	 * @param boo 是否发送短信 true 发送短信 false 不送短信
	 * @param sendContent 发送内容  您办理的***业务已经受理成功，将于 “时间” 生效。
	 * @param sjQualitative 办结原因目录
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public String updateBeforSheetWithQualitativeNew(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, boolean boo, String sendContent, SJSheetQualitative sjQualitative, int dealStaff) {
		String sheetId = sheetPubInfo.getWorkSheetId();
		int regionId = sheetPubInfo.getRegionId();
		String orderId = sheetPubInfo.getServiceOrderId();
		Integer month = sheetPubInfo.getMonth();
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.getSheetPubInfoDao().getSheetObj(sheetId, regionId, month, hisFlag);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交 工单状态不为挂起
		if (state != 1) {
			return STATUSERROR;
		}
		if (sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return STATUSERROR;
		}
		// 流水号
		String flowSeq = "1";
		if (sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq) + 1;
		TsmStaff staff;
		if(dealStaff != 0){//智慧商机单
			staff = this.getPubFunc().getStaff(dealStaff);
		}
		else{
			staff = this.getPubFunc().getLogonStaff();
		}
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();
		getOrderAskInfoDao().updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
		// 更新操作
		this.getSheetPubInfoDao().updateSheetDealRequire(sheetId, "", " ", sheetInfo.getServTypeDesc() + "完成", sheetPubInfo.getDealContent(),
				sheetInfo.getServType() + 1, StaticData.TACHE_FINISH);
		this.getOrderAskInfoDao().updateCrmAskSheet(orderAskInfo);/* 在CRM系统中受理是否成功 */
		sjQualitative.setSjRegionName(this.getPubFunc().getRegionName(Integer.parseInt(sjQualitative.getSjRegionId())));
		this.sjSheetQualitative.saveSJSheetQualitative(sjQualitative);
		ServiceContent servContent = this.getServContentDao().getServContentByOrderId(sheetInfo.getServiceOrderId(), false, 0);
		if (workSheetBusi.autoVisitSJ(orderId, month, sheetId, 4, servContent.getServiceTypeDetail())) {
			String wfInstId = sheetInfo.getWflInstId();
			String wfNodeInstId = sheetInfo.getTacheInstId();
			Map otherParam = new HashMap();
			otherParam.put("DEAL_REQUIRE", "");
			otherParam.put("ROUTE_VALUE", StaticData.ROUTE_AUD_TO_YSFINISH);
			otherParam.put("MONTH_FALG", month);
			otherParam.put("DEAL_PR_ORGID", dealOrg);// 派发部门
			otherParam.put("DEAL_PR_STAFFID", dealStaffId);// 派发员工
			otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeqNo));// 流水号
			otherParam.put("SERV_ORDER_ID", orderId);
			otherParam.put("SHEET_ID", sheetId);
			otherParam.put("WF__REGION_ID",sheetPubInfo.getRegionId());
			this.getWorkFlowAttemper().submitWorkFlow(wfInstId, wfNodeInstId, otherParam);
		}
		//修改商机单
		this.getBusinessOpportunityDao().updateBuopSheetInfo(buopSheetInfo);
		if (boo) {
			String content = "尊敬的客户：您好" + sendContent + "\n感谢您使用中国电信业务！";
			sendNoteCont(orderAskInfo.getRelaInfo(), regionId, content, sheetPubInfo.getServiceOrderId());
		}
		return SUCCESS;
	}

	public void sendNoteCont(String soureNum,int regionId,String sendContent, String serviceId) {
		String sheetGuid = this.getPubFunc().crtGuid();
		
		if("".equals(soureNum)) {
			return;
		}
		String fir = soureNum.substring(0, 1);
		String checkNum = soureNum;
		//如果第一个字不为0,就手动加0
		if(!"0".equals(fir)) {
			checkNum = "0"+soureNum;
		}
		//1本省电信固话;2本省电信手机;4外省电信移动手机;6外省或其它
		Map map = this.getNoteSen().getNumInfo(checkNum);
		String numType = map.get("out_numtype").toString();
		if(numType.equals("2")) {
			NoteSeand noteBean = new NoteSeand();
			noteBean.setSheetGuid(sheetGuid);
			noteBean.setRegionId(regionId);
			noteBean.setDestteRmid(soureNum);
			if("2".equals(numType)) {
				noteBean.setClientType(1);//终端标志 2本省电信手机
			}
			if("3".equals(numType)) {
				noteBean.setClientType(0);//终端标志 3本省电信小灵通
			}
			noteBean.setSendContent(sendContent);
			if(this.getPubFunc().isLogonFlag()) {
				//取当前登录员工信息
				TsmStaff staff = this.getPubFunc().getLogonStaff();
				int staffId = Integer.parseInt(staff.getId());
				String staffName = staff.getName();
				String orgId = staff.getOrganizationId();
				String orgName = staff.getOrgName();
				noteBean.setOrgId(orgId);
				noteBean.setOrgName(orgName);
				noteBean.setStaffId(staffId);
				noteBean.setStaffName(staffName);
			}
			noteBean.setBusiId(serviceId);
			this.getNoteSen().saveNoteContent(noteBean);
		}
	}

	public IsheetLimitTimeService getSheetLimitTimeServ() {
		return sheetLimitTimeServ;
	}


	public void setSheetLimitTimeServ(IsheetLimitTimeService sheetLimitTimeServ) {
		this.sheetLimitTimeServ = sheetLimitTimeServ;
	}


	public IsheetFlowOrg getSheetFlowOrg() {
		return sheetFlowOrg;
	}


	public void setSheetFlowOrg(IsheetFlowOrg sheetFlowOrg) {
		this.sheetFlowOrg = sheetFlowOrg;
	}


	public ISJSheetQualitative getSjSheetQualitative() {
		return sjSheetQualitative;
	}


	public void setSjSheetQualitative(ISJSheetQualitative sjSheetQualitative) {
		this.sjSheetQualitative = sjSheetQualitative;
	}

	


	
	
}
