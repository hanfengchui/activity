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
import com.timesontransfar.customservice.orderask.pojo.YNSJResult;
import com.timesontransfar.customservice.worksheet.dao.ISJSheetQualitative;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.IPreDealService;

@Service
@SuppressWarnings("rawtypes")
public class SyncBusinessResultImpl {
	private static final Logger logger = LoggerFactory.getLogger(SyncBusinessResultImpl.class);
	
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
	@Autowired
	private ISJSheetQualitative sjSheetQualitative;
	
	private String checkParam(YNSJResult result, TsmStaff staff, BuopSheetInfo buopSheetInfo) {
		String checkMsg = this.setReceiveStaff(result.getSystem(), staff);
		if(StringUtils.isNotBlank(checkMsg)) {
			return checkMsg;
		}
		
		checkMsg = this.setServiceOrderId(result);
		if(StringUtils.isNotBlank(checkMsg)) {
			return checkMsg;
		}
		
		checkMsg = this.checkOtherParam(result);
		if(StringUtils.isNotBlank(checkMsg)) {
			return checkMsg;
		}
		
		checkMsg = this.checkAcceptResult(result, buopSheetInfo);
		if(StringUtils.isNotBlank(checkMsg)) {
			return checkMsg;
		}
		return null;
	}
	
	private String checkAcceptResult(YNSJResult result, BuopSheetInfo buopSheetInfo) {
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
		String acceptFailReason = result.getAcceptFailReason();
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
			if(StringUtils.isBlank(acceptFailReason)){
				checkMsg = "失败原因不能为空";
			}
			if(acceptFailReason.length()>30){
				checkMsg = "失败原因填写长度不能超过30。";
			}
			buopSheetInfo.setDealResult("办理失败");
			buopSheetInfo.setFailResult(acceptFailReason);
		}
		buopSheetInfo.setIsTrans(isTrans);
		buopSheetInfo.setSatasfi(satasfi);
		buopSheetInfo.setReplyRemark(replyRemark);
		buopSheetInfo.setSheetStatus("1");
		return checkMsg;
	}
	
	private String checkOtherParam(YNSJResult result) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			format.parse(result.getReceiveOrderDate());
		} catch (Exception e) {
			return "receiveOrderDate接单时间不符合规范";
		}
		
		if(StringUtils.isEmpty(result.getDealStaffCode())) {
			return "dealStaffCode处理工号不能为空";
		}
		
		if(StringUtils.isEmpty(result.getDealStaffName())) {
			return "dealStaffName处理人姓名不能为空";
		}
		
		if(StringUtils.isEmpty(result.getDealOrgName())) {
			return "dealOrgName处理人部门不能为空";
		}
		
		try {
			format.parse(result.getDealOrderDate());
		} catch (Exception e) {
			return "dealOrderDate处理时间不符合规范";
		}
		
		if(null == result.getOneSjCatalog()) {
			return "oneSjCatalog办结原因一级目录不能为空";
		}
		
		if(null == result.getTwoSjCatalog()) {
			return "twoSjCatalog办结原因二级目录不能为空";
		}
		
		if(null == result.getThreeSjCatalog()) {
			return "threeSjCatalog办结原因三级目录不能为空";
		}

		if(result.getOneSjCatalog().toString().length()>10) {
			return "oneSjCatalog办结原因一级目录长度不能超过10";
		}

		if(result.getTwoSjCatalog().toString().length()>10) {
			return "twoSjCatalog办结原因二级目录长度不能超过10";
		}

		if(result.getThreeSjCatalog().toString().length()>10) {
			return "threeSjCatalog办结原因三级目录长度不能超过10";
		}
		
		if(StringUtils.isEmpty(result.getDealContent())) {
			return "dealContent处理内容不能为空";
		}
		return null;
	}
	
	private String setServiceOrderId(YNSJResult result) {
		String orderId = result.getOrderId();
		String servOrderId = result.getServiceOrderId();
		String checkMsg = null;
		if("".equals(orderId) && "".equals(servOrderId)) {
			checkMsg = "orderId、servOrderId不能同时为空";
		}
		else if(!"".equals(orderId) && !"".equals(servOrderId)) {
			checkMsg = "orderId、servOrderId不能同时传值";
		}
		else if(!"".equals(orderId)) {//中台订单号码
			servOrderId = orderAskInfoDao.getBusinessOrderId(orderId);
			if("".equals(servOrderId)) {
				checkMsg = "根据orderId订单号码未查询到成功下单的商机单";
			}
			else {
				result.setServiceOrderId(servOrderId);
			}
		}
		return checkMsg;
	}
	
	private String setReceiveStaff(String system, TsmStaff staff) {
		if(StringUtils.isBlank(system)) {
			return "system传值不符合规范";
		}
		try {
			Map config = pubFunc.getSheetAllotConfig(StaticData.SERV_TYPE_SJ, StaticData.TACHE_ASSIGN, system, "1");
			if(config != null) {
				int receiveStaff = Integer.parseInt(config.get("RECEIVE_STAFF") == null ? "0" : config.get("RECEIVE_STAFF").toString());
				if(receiveStaff != 0) {
					TsmStaff s = pubFunc.getStaff(receiveStaff);
					if(s != null) {
						staff.setId(s.getId());
		        		staff.setName(s.getName());
		        		staff.setOrganizationId(s.getOrganizationId());
		        		staff.setOrgName(s.getOrgName());
		        		staff.setLinkId(s.getLinkId());
		        		staff.setRelaPhone(s.getRelaPhone());
		        		staff.setLogonName(s.getLogonName());
		        		staff.setPassword(s.getPassword());
		        		staff.setGender(s.getGender());
					}
				}
			}
		} catch (Exception e) {
			logger.error("查询员工信息异常: {}", e.getMessage(), e);
		}
		if(StringUtils.isBlank(staff.getId())) {
			return "system传值不符合规范";
		}
		return null;
	}
	
	public String dealBusinessResult(String reqJson) {
		logger.info("syncBusinessResult in: {}", reqJson);

		JsonObject rspJson = new JsonObject();//返回结果
		YNSJResult result = new YNSJResult();
		try {
			result = new Gson().fromJson(reqJson, YNSJResult.class);
			
			BuopSheetInfo buopSheetInfo = new BuopSheetInfo();
			TsmStaff staff = new TsmStaff();
			String checkMsg = this.checkParam(result, staff, buopSheetInfo);
			if(StringUtils.isNotEmpty(checkMsg)) {
				this.setResultJson(rspJson, "error", checkMsg);
			}
			
			if(rspJson.get("code") == null) {//没有错误信息
				if(!"1".equals(result.getUpdateHisFlag())) {//首次回单
					this.firstDealSheet(result, staff, rspJson, buopSheetInfo);
				}
				else {//二次回单
					this.secondDealSheet(result, staff, rspJson);
				}
			}
		}
		catch(Exception e) {
			logger.error("商机同步结单接口 异常：", e);
			this.setResultJson(rspJson, "error", "Exception："+e);
		}
		
		IntfLog ilog = new IntfLog();
		ilog.setServOrderId(result.getServiceOrderId());
		ilog.setInMsg(reqJson);
		ilog.setOutMsg(rspJson.toString());
		ilog.setActionFlag("in");
		ilog.setActionResult("success".equals(rspJson.get("code").getAsString()) ? "1" : "0");
		ilog.setSystem(StringUtils.defaultIfBlank(result.getSystem(), "business"));
		pubFunc.saveYNSJIntfLog(ilog);
		logger.info("syncBusinessResult out: {}", rspJson.toString());
		return rspJson.toString();
	}
	
	private void firstDealSheet(YNSJResult result, TsmStaff staff, JsonObject rspJson, BuopSheetInfo buopSheetInfo) {
		String servOrderId = result.getServiceOrderId();
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(servOrderId, false);
		SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(servOrderId, StaticData.SHEET_TYPE_SJ_ASSING, 0);

		String fetchFlag = this.fetchWorkSheet(result, sheetInfo, staff);
		if("SUCCESS".equals(fetchFlag)) {
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
			
			SJSheetQualitative sjQualitative = new SJSheetQualitative();
			sjQualitative.setServiceOrderId(servOrderId);
			sjQualitative.setWorkSheetId(sheetInfo.getWorkSheetId());
			sjQualitative.setMonthFlag(sheetInfo.getMonth());
			sjQualitative.setOneSjCatalog(result.getOneSjCatalog());
			sjQualitative.setOneSjCatalogDesc(this.pubFunc.getStaticName(result.getOneSjCatalog()));
			sjQualitative.setTwoSjCatalog(result.getTwoSjCatalog());
			sjQualitative.setTwoSjCatalogDesc(this.pubFunc.getStaticName(result.getTwoSjCatalog()));
			sjQualitative.setThreeSjCatalog(result.getThreeSjCatalog());
			sjQualitative.setThreeSjCatalogDesc(this.pubFunc.getStaticName(result.getThreeSjCatalog()));
			sjQualitative.setFourSjCatalogDesc(result.getFourSjCatalogDesc());
			sjQualitative.setSjValid(result.getIsValid()+"");
			sjQualitative.setSjRegionId(this.pubFunc.getOrgRegion(staff.getOrganizationId())+"");
			
			boolean boo = 1 == result.getIsSendNote();
			String sendContent = "您办理的" + result.getBusinessName() + "业务已经受理成功，将于" + result.getBusiWrokTime() + "生效。";
			
			buopSheetInfo.setServiceOrderId(servOrderId);
			String data = preDealService.updateBeforSheetWithQualitativeNew(sheetPubInfo, orderAskInfo, buopSheetInfo, boo, sendContent, sjQualitative, Integer.parseInt(staff.getId()));
			if("success".equals(data)) {
				orderAskInfoDao.saveYNSJResult(result);
				
				rspJson.addProperty("flowNo", result.getFlowNo());
				rspJson.addProperty("serviceOrderId", result.getServiceOrderId());
				this.setResultJson(rspJson, "success", "处理成功");
			}
			else {
				this.setResultJson(rspJson, data, "商机单办结异常");
			}
		}
		else  {
			this.setResultJson(rspJson, fetchFlag, "没有查询到待处理的商机单");
		}
	}
	
	private void secondDealSheet(YNSJResult result, TsmStaff staff, JsonObject rspJson) {
		String servOrderId = result.getServiceOrderId();
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(servOrderId, true);
		if(orderInfo != null) {
			SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObjBySourceId(orderInfo.getRegionId(), servOrderId, true);
			if(sheetInfo != null) {
				OrderAskInfo orderAskInfo = new OrderAskInfo();
				orderAskInfo.setAskChannelId(result.getIsBssAccept());
				orderAskInfo.setAskChannelDesc(result.getIsBssAccept() == 1 ? result.getBssWorkOrderId() : "");
				orderAskInfo.setServOrderId(servOrderId);
				orderAskInfo.setMonth(orderInfo.getMonth());
				orderAskInfo.setOrderVer(orderInfo.getOrderVer());
				orderAskInfo.setRelaInfo(orderInfo.getRelaInfo());
				orderAskInfoDao.updateHisCrmAskSheet(orderAskInfo);
				
				sheetPubInfoDao.updateHisSheetDealContent(sheetInfo.getWorkSheetId(), result.getDealContent());
				
				SJSheetQualitative sjQualitative = new SJSheetQualitative();
				sjQualitative.setServiceOrderId(servOrderId);
				sjQualitative.setWorkSheetId(sheetInfo.getWorkSheetId());
				sjQualitative.setMonthFlag(sheetInfo.getMonth());
				sjQualitative.setOneSjCatalog(result.getOneSjCatalog());
				sjQualitative.setOneSjCatalogDesc(this.pubFunc.getStaticName(result.getOneSjCatalog()));
				sjQualitative.setTwoSjCatalog(result.getTwoSjCatalog());
				sjQualitative.setTwoSjCatalogDesc(this.pubFunc.getStaticName(result.getTwoSjCatalog()));
				sjQualitative.setThreeSjCatalog(result.getThreeSjCatalog());
				sjQualitative.setThreeSjCatalogDesc(this.pubFunc.getStaticName(result.getThreeSjCatalog()));
				sjQualitative.setFourSjCatalogDesc(result.getFourSjCatalogDesc());
				sjQualitative.setSjValid(result.getIsValid()+"");
				sjQualitative.setSjRegionId(this.pubFunc.getOrgRegion(staff.getOrganizationId())+"");
				sjQualitative.setSjRegionName(this.pubFunc.getRegionName(Integer.parseInt(sjQualitative.getSjRegionId())));
				sjSheetQualitative.saveHisSJSheetQualitative(sjQualitative);
				
				if (1 == result.getIsSendNote()) {
					String sendContent = "您办理的" + result.getBusinessName() + "业务已经受理成功，将于" + result.getBusiWrokTime() + "生效。";
					String content = "尊敬的客户：您好！" + sendContent + "\n感谢您使用中国电信业务！";
					preDealService.sendNoteCont(orderAskInfo.getRelaInfo(), sheetInfo.getRegionId(), content, sheetInfo.getServiceOrderId());
				}
				
				int updateFlag = orderAskInfoDao.updateYNSJResult(result);
				if(updateFlag > 0) {								
					rspJson.addProperty("flowNo", result.getFlowNo());
					rspJson.addProperty("serviceOrderId", result.getServiceOrderId());
					this.setResultJson(rspJson, "success", "处理成功");
				}
				else {
					this.setResultJson(rspJson, "UPDATE_ERROR", "商机单回单更新异常");
				}
			}
			else {
				this.setResultJson(rspJson, "NULL_HIS_SHEET", "没有查询到该商机单的归档工单");
			}							
		}
		else {
			this.setResultJson(rspJson, "NULL_HIS_ORDER", "没有查询到归档商机单");
		}
	}
	
	public String fetchWorkSheet(YNSJResult result, SheetPubInfo sheetPubInfo, TsmStaff staff) {
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
			this.sheetPubInfoDao.updateFetchSheetStaffNew(sheetId, staffId,
					staffName, orgId, orgName, result.getReceiveOrderDate());
			
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