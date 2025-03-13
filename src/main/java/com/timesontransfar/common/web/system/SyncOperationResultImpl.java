package com.timesontransfar.common.web.system;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.handler.ComplaintDealHandler;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.IntfLog;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.PreOrderResult;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.IPreDealService;

@Service
public class SyncOperationResultImpl {
	private static final Logger log = LoggerFactory.getLogger(SyncOperationResultImpl.class);
	
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private ISheetActionInfoDao sheetActionInfoDao;
	@Autowired
	private IPreDealService preDealService;
	
	private String checkAcceptResult(int dealFlag, PreOrderResult result, BuopSheetInfo buopSheetInfo) {
		if(dealFlag != 1) {
			return null;
		}
		String checkMsg = null;
		
		int isBssAccept = result.getIsBssAccept();
		String acceptOrderId = result.getAcceptOrderId();
		String offerId = result.getOfferId();
		String offerName = result.getOfferName();
		String acceptDate = result.getAcceptDate();
		String acceptChnl = result.getAcceptChnl();
		String acceptStaff = result.getAcceptStaff();
		String busiState = result.getBusiState();
		String developStaff = result.getDevelopStaff();
		String failResult = result.getFailResult();
		String isTrans = result.getIsTrans();
		String satasfi = result.getSatasfi();
		String replyRemark = result.getReplyRemark();
		
		if(isBssAccept == 1){
			if(StringUtils.isBlank(acceptOrderId)){
				checkMsg = "受理单ID不能为空。";
			}else if(StringUtils.isBlank(offerId)){
				checkMsg = "销售品ID不能为空。";
			}else if(StringUtils.isBlank(offerName)){
				checkMsg = "销售品名称不能为空。";
			}else if(StringUtils.isBlank(acceptDate)){
				checkMsg = "受理时间不能为空。";
			}else if(StringUtils.isBlank(acceptChnl)){
				checkMsg = "受理渠道不能为空。";
			}else if(StringUtils.isBlank(acceptStaff)){
				checkMsg = "受理人不能为空。";
			}else if(StringUtils.isBlank(busiState)){
				checkMsg = "业务状态不能为空。";
			}else if(StringUtils.isBlank(developStaff)){
				checkMsg = "发展人不能为空。";
			}
			buopSheetInfo.setAcceptOrderId(acceptOrderId);
			buopSheetInfo.setOfferId(offerId);
			buopSheetInfo.setOfferName(offerName);
			buopSheetInfo.setAcceptDate(acceptDate);
			buopSheetInfo.setAcceptChnl(acceptChnl);
			buopSheetInfo.setAcceptStaff(acceptStaff);
			buopSheetInfo.setDealResult("办理成功");
			buopSheetInfo.setBusiState(busiState);
			buopSheetInfo.setDevelopStaff(developStaff);
		} else{
			if(StringUtils.isBlank(failResult)){
				checkMsg = "失败原因不能为空";
			}
			buopSheetInfo.setDealResult("办理失败");
			buopSheetInfo.setFailResult(failResult);
		}
		buopSheetInfo.setIsTrans(isTrans);
		buopSheetInfo.setSatasfi(satasfi);
		buopSheetInfo.setReplyRemark(replyRemark);
		buopSheetInfo.setSheetStatus("1");
		return checkMsg;
	}
	
	public String dealOperationResult(String reqJson) {
		log.info("syncOperationResult in: {}", reqJson);
		JsonObject rspJson = new JsonObject();//返回结果
		
		PreOrderResult result = new PreOrderResult();
		String system = "";
		String actionResult = "0";
		try {
			result = new Gson().fromJson(reqJson, PreOrderResult.class);
			rspJson.addProperty("flowNo", result.getFlowNo());
			
			system = StringUtils.defaultString(result.getSystem(), "");
			TsmStaff staff = this.getTsmStaff(system);
			if(StringUtils.isBlank(staff.getId())) {
				this.setResultJson(rspJson, "error", "system传值不符合规范");
			}
			
			String orderId = result.getOrderId();
			if("".equals(orderId)) {
				this.setResultJson(rspJson, "error", "orderId订单号码不能为空");
			}
			
			String servOrderId = orderAskInfoDao.getPreOrderId(orderId);
			if("".equals(servOrderId)) {
				this.setResultJson(rspJson, "error", "根据orderId订单号码没有查询到待处理的预受理号");
			}
			log.info("dealOperationResult serviceOrderId: {}", servOrderId);
			result.setServiceOrderId(servOrderId);
			rspJson.addProperty("serviceOrderId", servOrderId);
			
			if(!this.checkDealFlag(result.getDealFlag())) {
				this.setResultJson(rspJson, "error", "dealFlag处理方式不符合规范");
			}
			
			if(StringUtils.isEmpty(result.getDealContent())) {
				this.setResultJson(rspJson, "error", "dealContent处理内容不能为空");
			} else if(result.getDealContent().length() > 500) {
				this.setResultJson(rspJson, "error", "dealContent不能超过500字");
			}
			
			int dealFlag = result.getDealFlag();//是否直接处理
			BuopSheetInfo buopSheetInfo = new BuopSheetInfo();
			String checkMsg = this.checkAcceptResult(dealFlag, result, buopSheetInfo);
			if(StringUtils.isNotEmpty(checkMsg)) {
				this.setResultJson(rspJson, "error", checkMsg);
			}
			
			if(rspJson.get("code") == null) {//没有错误信息
				if(dealFlag == 1) {//直接处理
					OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(servOrderId, false);
					SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(servOrderId, StaticData.SHEET_TYPE_YS_ASSING, 0);
		
					OrderAskInfo orderAskInfo = new OrderAskInfo();
					orderAskInfo.setAskChannelId(result.getIsBssAccept());
					orderAskInfo.setAskChannelDesc(result.getIsBssAccept() == 1 ? result.getBssWorkOrderId() : "");
					orderAskInfo.setReplyContent(result.getDealContent());
					orderAskInfo.setServOrderId(servOrderId);
					orderAskInfo.setMonth(orderInfo.getMonth());
					orderAskInfo.setRelaInfo(orderInfo.getRelaInfo());
					
					SheetPubInfo sheetPubInfo = new SheetPubInfo();
					sheetPubInfo.setWorkSheetId(sheetInfo.getWorkSheetId());
					sheetPubInfo.setDealContent(result.getDealContent());
					sheetPubInfo.setServiceOrderId(servOrderId);
					sheetPubInfo.setRcvOrgId(sheetInfo.getRcvOrgId());
					sheetPubInfo.setRegionId(sheetInfo.getRegionId());
					sheetPubInfo.setMonth(sheetInfo.getMonth());
	
					boolean boo = 1 == result.getIsSendNote();
					String sendContent = "您办理的" + result.getBusinessName() + "业务已经受理成功，将于" + result.getBusiWrokTime() + "生效。";
					
					buopSheetInfo.setServiceOrderId(servOrderId);
					String rt = preDealService.updateBeforSheetNew(sheetPubInfo, orderAskInfo, buopSheetInfo, boo, sendContent, Integer.parseInt(staff.getId()));
					//记录处理结果
					actionResult = this.setDealResult(rt, result, rspJson);
				} else {//退回受理员工
					OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(servOrderId, false);
					SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(servOrderId, StaticData.SHEET_TYPE_YS_ASSING, 0);
					
					SheetPubInfo sheetPubInfo = new SheetPubInfo();
					sheetPubInfo.setWorkSheetId(sheetInfo.getWorkSheetId());
					sheetPubInfo.setDealRequire(result.getDealContent());//处理内容
					sheetPubInfo.setDealContent("您有一张受理单被退回。服务单号：" + servOrderId);
					sheetPubInfo.setRcvStaffId(orderInfo.getAskStaffId());
					sheetPubInfo.setRcvOrgId(orderInfo.getAskOrgId());
					sheetPubInfo.setRegionId(sheetInfo.getRegionId());
					sheetPubInfo.setMonth(sheetInfo.getMonth());
					
					String rt = preDealService.assignBackToAskFull(sheetPubInfo, Integer.parseInt(staff.getId()));
					//记录处理结果
					actionResult = this.setDealResult(rt, result, rspJson);
				}
			}
		}
		catch(Exception e) {
			log.error("预受理单同步结单接口 异常：{}", e.getMessage(), e);
			this.setResultJson(rspJson, "error", "Exception："+e);
		}
		
		IntfLog ilog = new IntfLog();
		ilog.setServOrderId(StringUtils.defaultIfEmpty(result.getOrderId(), "0"));
		ilog.setInMsg(reqJson);
		ilog.setOutMsg(rspJson.toString());
		ilog.setActionFlag("in");
		ilog.setActionResult(actionResult);
		ilog.setSystem(StringUtils.defaultIfEmpty(system, "operation"));
		pubFunc.saveYNSJIntfLog(ilog);
		
		log.info("dealOperationResult orderId: {} out: {}", result.getOrderId(), rspJson);
		return rspJson.toString();
	}
	
	private String setDealResult(String rt, PreOrderResult result, JsonObject rspJson) {
		String actionResult = "0";//0-失败；1-成功
		if("success".equals(rt)) {
			actionResult = "1";
			orderAskInfoDao.updatePreOrderResult(result);
			this.setResultJson(rspJson, "success", "处理成功");
		}
		else {
			this.setResultJson(rspJson, rt, "预受理单办结异常");
		}
		return actionResult;
	}
	
	/**
	 * @param dealFlag 是否直接处理（0-退回受理员工；1-直接处理）
	 * @return
	 */
	private boolean checkDealFlag(int dealFlag) {
		return (dealFlag == 0 || dealFlag == 1);
	}
	
	@SuppressWarnings({ "rawtypes" })
	private TsmStaff getTsmStaff(String system) {
		TsmStaff staff = new TsmStaff();
		Map config = pubFunc.getSheetAllotConfig(StaticData.SERV_TYPE_YS, StaticData.TACHE_ASSIGN, system, "1");
		if(config != null) {
			int receiveStaff = Integer.parseInt(config.get("RECEIVE_STAFF") == null ? "0" : config.get("RECEIVE_STAFF").toString());
			if(receiveStaff != 0) {
				staff = pubFunc.getStaff(receiveStaff);
			}
		}
		return staff;
	}
	
	@SuppressWarnings("unused")
	private String fetchWorkSheet(SheetPubInfo sheetPubInfo, TsmStaff staff) {
		if(sheetPubInfo == null) {
			return "NULL_SHEET";
		}
		String sheetId = sheetPubInfo.getWorkSheetId();
		int sheetState = sheetPubInfo.getLockFlag();
		if(ComplaintDealHandler.isHoldSheet(sheetPubInfo.getSheetStatu())) {//工单池挂起的工单不能直接提取
			return "WKST_HOLD_STATE";
		}
		
		if (sheetState == 0 && sheetPubInfo.getSheetStatu() != StaticData.WKST_ALLOT_STATE) {
			int staffId = Integer.parseInt(staff.getId());
			String staffName = staff.getName();
			String orgId = staff.getOrganizationId();
			String orgName = staff.getOrgName();
			
			// 更新提单员工信息及工单状态
			this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, staffId, staffName, orgId, orgName);
			
			//得到要更新的状态
			int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());
			String stateDesc = this.pubFunc.getStaticName(sheetStatu);
			this.sheetPubInfoDao.updateSheetState(sheetId, sheetStatu, stateDesc, sheetPubInfo.getMonth(), 1);
			
			//记录工单动作			
			SheetActionInfo sheetActionInfo = new SheetActionInfo();
			String guid = this.pubFunc.crtGuid();
			int tacheId = sheetPubInfo.getTacheId();
			sheetActionInfo.setWorkSheetId(sheetId);
			sheetActionInfo.setComments("提取工单");
			sheetActionInfo.setRegionId(sheetPubInfo.getRegionId());
			sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
			sheetActionInfo.setMonth(sheetPubInfo.getMonth());
			sheetActionInfo.setActionGuid(guid);
			sheetActionInfo.setTacheId(tacheId);
			sheetActionInfo.setTacheName(this.pubFunc.getStaticName(tacheId));
			sheetActionInfo.setActionCode(StaticData.WKST_FETCH_ACTION);
			sheetActionInfo.setActionName(this.pubFunc.getStaticName(StaticData.WKST_FETCH_ACTION));
			sheetActionInfo.setOpraOrgId(orgId);
			sheetActionInfo.setOpraOrgName(orgName);
			sheetActionInfo.setOpraStaffId(staffId);
			sheetActionInfo.setOpraStaffName(staffName);			
			this.sheetActionInfoDao.saveSheetActionInfo(sheetActionInfo);
			return "SUCCESS";
		}
		return "ERROR";
	}

	private void setResultJson(JsonObject json,String code,String msg){
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		json.addProperty("returnDate", formatter.format(currentTime));
		json.addProperty("code", code);
		json.addProperty("msg", msg);
	}
	
}