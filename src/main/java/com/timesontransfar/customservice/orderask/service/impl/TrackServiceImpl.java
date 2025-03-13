package com.timesontransfar.customservice.orderask.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.google.gson.Gson;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.handler.ComplaintDealHandler;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.orderask.dao.ITrackServiceDao;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.pojo.YNSJOrder;
import com.timesontransfar.customservice.worksheet.pojo.TrackInfo;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.impl.TsSheetSumbitImpl;
import com.timesontransfar.feign.custominterface.InterfaceFeign;
import com.timesontransfar.sheetHandler.ASheetDealHandler;
import com.timesontransfar.customservice.orderask.service.ITrackService;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.customservice.tuschema.dao.IserviceContentTypeDao;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetReAssignReason;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.RefundModeRecord;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.transfar.common.enums.ResultEnum;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class TrackServiceImpl implements ITrackService {
	private static Logger log = LoggerFactory.getLogger(TrackServiceImpl.class);
	
	@Autowired
    private PubFunc pubFunc;
	@Autowired
	private IserviceOrderAsk askImpl;
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;
    @Autowired
    private ITrackServiceDao trackServiceDao;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
    private InterfaceFeign interfaceFeign;
	@Autowired
    private IserviceContentTypeDao serviceContentType;
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	@Autowired
	private IserviceContentSchem serviceContentSchem;
	@Autowired
	private TsSheetSumbitImpl tsSheetSumbitImpl;
	@Autowired
	private ISheetActionInfoDao sheetActionInfoDao;
	@Autowired
    private ILabelManageDAO labelManageDAO;
    @Autowired
    private ASheetDealHandler sheetDealHandler;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String createTrackServiceTZ(String parm) {
		log.info("createTrackServiceTZ: {}", parm);
		
		JSONObject json = JSONObject.fromObject(parm);
		TrackInfo track = (TrackInfo)JSONObject.toBean(json.optJSONObject("track"),TrackInfo.class);
		
		TsmStaff staff = null;
		String zhzdFlag = "0";
		if(json.containsKey("acceptStaff")) {//接口受理
			String acceptStaff = json.optString("acceptStaff");
			staff = pubFunc.getLogonStaffByLoginName(acceptStaff);
			zhzdFlag = "1";
		} else {
			staff = pubFunc.getLogonStaff();
		}
		
		ServiceOrderInfo info = null;
		info = askImpl.getServOrderInfo(track.getOldOrderId(), false);//当前表
		ServiceContentSave[] saves = serviceContentType.selectContentSave(track.getOldOrderId());
		if(info == null) {
			info = askImpl.getServOrderInfo(track.getOldOrderId(), true);//历史表
			saves = serviceContentType.selectContentSaveHis(track.getOldOrderId());
		}
		if(info == null) {
			return ResultUtil.fail(ResultEnum.OBJERROR);
		}
		saves = serviceContentSchem.filterRefundData(saves);//过滤退费数据
		
		OrderAskInfo orderAskInfo = info.getOrderAskInfo();
		orderAskInfo.setServOrderId("");
		orderAskInfo.setCallSerialNo("N");
		orderAskInfo.setOrderVer(0);
		orderAskInfo.setServType(720200002);
		orderAskInfo.setServTypeDesc("特别跟踪");
		orderAskInfo.setCustId("");
		orderAskInfo.setAskStaffId(Integer.parseInt(staff.getId()));
		orderAskInfo.setAskStaffName(staff.getName());
		orderAskInfo.setAskOrgId(staff.getOrganizationId());
		orderAskInfo.setAskOrgName(staff.getOrgName());
		orderAskInfo.setModifyDate("");
		orderAskInfo.setAskCount(1);
		orderAskInfo.setOrderStatu(0);
		orderAskInfo.setOrderStatuDesc("");
		
		ServiceContent servContent = info.getServContent();
		servContent.setServOrderId("");
		servContent.setOrderVer(0);
		servContent.setServType(720200002);
		servContent.setServTypeDesc("特别跟踪");
		servContent.setServiceTypeDetail(this.getTrackTypeDesc(track.getTrackType()));
		servContent.setBestOrder(100122410);
		servContent.setBestOrderDesc("否");
		
		OrderCustomerInfo orderCustInfo = info.getOrderCustInfo();
		orderCustInfo.setCustGuid("");
		
		Map audMap = new HashMap();
		audMap.put("ORGIDSTR", "");
		audMap.put("DEALREQUIE", "");
		audMap.put("SENDFLAG", "false");
		audMap.put("STRFLOW", "DISPATCHSHEET");
		audMap.put("SEND_TO_OBJ_FLAG", json.optString("isDispatch"));//1-部门 0-员工
		audMap.put("SEND_TO_OBJ_ID", json.optString("sendToObjId"));
		audMap.put("ZHZDFLAG", zhzdFlag);
		
		String result = ResultUtil.fail(ResultEnum.FALL);
		try {
			result = askImpl.submitServiceOrderInstanceLabelNew(orderCustInfo, servContent, orderAskInfo, audMap, null, saves, null);
		} catch (Exception e) {
			result = ResultUtil.error(e.getMessage());
		}
		
		String code = JSONObject.fromObject(result).optString("code");
		if("0000".equals(code)){
			String newOrderId = orderAskInfo.getServOrderId();
			OrderAskInfo newOrder = orderAskInfoDao.getOrderAskInfo(newOrderId, false);
			if(newOrder != null) {
				track.setNewOrderId(newOrderId);
				track.setCreateDate(newOrder.getAskDate());
				int count = trackServiceDao.saveTrackInfoTz(track);
				if(count > 0) {
					this.saveRefundOrder(json, track);//保存退费数据
					return ResultUtil.success(newOrder.getServOrderId());
				}
			}
		}
		return ResultUtil.fail(ResultEnum.ERROR);
	}
	
	private void saveRefundOrder(JSONObject json, TrackInfo track) {
		if(json.containsKey("refundData")) {//退费数据
			String refundData = json.optString("refundData");
			if(StringUtils.isNotBlank(refundData)) {
				String refundsAccNum = json.optString("refundsAccNum");
				String refundAmount = json.optString("refundAmount");
				String prmRefundAmount = json.optString("prmRefundAmount");
				int num = trackServiceDao.saveRefundOrder(track, refundData, refundsAccNum, refundAmount, prmRefundAmount);
				if(num > 0) {
					labelManageDAO.updateRefundFlag(track.getNewOrderId(), 1);
				}
				
				//小额退赔跟踪单，工单池挂起
				this.hangupTrackOrder(track.getNewOrderId());
			}
		}
	}
	
	private void hangupTrackOrder(String orderId) {
		SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(orderId, StaticData.SHEET_TYPE_TS_ASSING, 0);
		if (ComplaintDealHandler.isHoldSheet(sheetInfo.getSheetStatu())) {
			return; // 已经挂起
		}
		int sheetState = sheetInfo.getLockFlag();
		int result = 0;
		if (sheetState == 0 && sheetInfo.getSheetStatu() != StaticData.WKST_ALLOT_STATE) { // 工单池
			result = sheetDealHandler.hangup(sheetInfo.getWorkSheetId(), "工单池调账挂起", 9, StaticData.WKST_HOLD_STATE, 0); // 调账单挂起
		}
		log.info("hangup orderId: {} result: {}", orderId, result);
	}
	
	private String getTrackTypeDesc(int trackType) {
		String result = "";
		switch (trackType) {
			case 1:
				result = "调账";
				break;
			case 2:
				result = "集团协同";
				break;
			case 3:
				result = "其他";
				break;
			default:
				break;
		}
		return result;
	}
	
	public String createTrackService(String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		TrackInfo track = (TrackInfo)JSONObject.toBean(json.optJSONObject("track"),TrackInfo.class);
		String newOrderId = json.optString("newOrderId");
		
		OrderAskInfo newOrder = orderAskInfoDao.getOrderAskInfo(newOrderId, false);
		if(newOrder != null) {
			track.setNewOrderId(newOrderId);
			track.setCreateDate(newOrder.getAskDate());
			int count = trackServiceDao.saveTrackInfoTz(track);
			if(count > 0) {
				return ResultUtil.success(newOrder.getServOrderId());
			}
		}
		return ResultUtil.fail(ResultEnum.ERROR);
	}
	
	public String saveReAssignReason(String parm) {
		TsmStaff staff = pubFunc.getLogonStaff();
		SheetReAssignReason reason = new Gson().fromJson(parm, SheetReAssignReason.class);
		reason.setCreateStaff(Integer.parseInt(staff.getId()));
		reason.setCreateStaffName(staff.getName());
		int cn = sheetPubInfoDao.saveReAssignReason(reason);
		if(cn > 0) {
			return ResultUtil.success();
		}
		return ResultUtil.fail(ResultEnum.ERROR);
	}
	
	@SuppressWarnings("rawtypes")
	public Map getTrackInfo(String orderId) {
		return trackServiceDao.getTrackInfo(orderId);
	}
	
	public String modifyTrackInfo(String parm, String dealStaff) {
		String modifyStaffId = "0";
		if(StringUtils.isNotBlank(dealStaff)) {
			modifyStaffId = pubFunc.getLogonStaffByLoginName(dealStaff).getId();
		} else {
			modifyStaffId = pubFunc.getLogonStaff().getId();
		}
		JSONObject json = JSONObject.fromObject(parm);
		boolean modiFlag = json.optBoolean("modiFlag");
		TrackInfo track = (TrackInfo)JSONObject.toBean(json.optJSONObject("track"),TrackInfo.class);
		if(modiFlag) {
			RefundModeRecord r = new RefundModeRecord();
			r.setServiceOrderId(track.getNewOrderId());
			r.setOldMode(json.optInt("oldMode"));
			r.setOldModeDesc(json.optString("oldModeDesc"));
			r.setNewMode(track.getRefundMode());
			r.setNewModeDesc(track.getRefundModeDesc());
			r.setModifyStaffId(Integer.parseInt(modifyStaffId));
			trackServiceDao.modifyRefundMode(r);
		}
		int cn = trackServiceDao.modifyTrackInfo(track);
		if(cn > 0) {
			return ResultUtil.success();
		}
		return ResultUtil.fail(ResultEnum.ERROR);
	}
	
	@SuppressWarnings("rawtypes")
	public void syncComplaintOrder(OrderAskInfo orderAskInfo, ServiceContent servContent, OrderCustomerInfo custInfo) {
		//投诉 最严工单
		if(StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType() && servContent.getBestOrder() > 100122410) {
			Map tmpMap = trackServiceDao.getComplaintOrderType(servContent.getBestOrder(), orderAskInfo.getRegionId());
			if(!tmpMap.isEmpty()) {
				log.info("最严场景的投诉单对接云脑系统");
				String tagCode = tmpMap.get("TYPE").toString();
				String tagName = tmpMap.get("HOT_NAME").toString();
				
				Map<String, String> orderDetail = new HashMap<>();
				orderDetail.put("complaintOrderSn", orderAskInfo.getServOrderId());
				orderDetail.put("tagCode", tagCode);
				orderDetail.put("tagName", tagName);
				orderDetail.put("custPurpose", servContent.getAcceptContent());
				orderDetail.put("custPhoneNo", orderAskInfo.getRelaInfo());
				orderDetail.put("custAccNumber", StringUtils.substring(orderAskInfo.getProdNum(), 0, 20));
//				orderDetail.put("custAddress", "");
				orderDetail.put("custName", StringUtils.substring(custInfo.getCustName(), 0, 50));
//				orderDetail.put("idCardNo", "");
//				orderDetail.put("assistStaffNo", "");
//				orderDetail.put("assistStaffName", "");
//				orderDetail.put("assistStaffPhoneNo", "");
//				orderDetail.put("assistStaffChannel", "");
//				orderDetail.put("remark", "");
				int num = trackServiceDao.saveComplaintOrderDeatail(orderDetail);
				log.info("syncComplaintOrder result: {}", num > 0 ? "成功" : "失败");
				if(num > 0) {
					interfaceFeign.sendComplaintOrder(orderAskInfo.getServOrderId());
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	public String finishTrackOrder(String oldOrderId, String trackOrderId) {
		log.info("自动完结小额退赔查询单: {} {}", oldOrderId, trackOrderId);
		SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(oldOrderId, StaticData.SHEET_TYPE_TS_ASSING, 0);
		
		TsmStaff dealStaff = new TsmStaff();
		String fetchFlag = this.fetchWorkSheet(sheetInfo, trackOrderId, dealStaff);
		log.info("fetchWorkSheet result: {}", fetchFlag);
		if(!"SUCCESS".equals(fetchFlag)) {
			return ResultUtil.error(fetchFlag);
		}

		SheetPubInfo sheetBean = new SheetPubInfo();
		sheetBean.setServiceOrderId(oldOrderId);
		sheetBean.setWorkSheetId(sheetInfo.getWorkSheetId());
		sheetBean.setRcvOrgId("ASSIGN_TO_AUD");
		sheetBean.setRegionId(sheetInfo.getRegionId());
		sheetBean.setMonth(sheetInfo.getMonth());
		sheetBean.setDealContent("已处理");
		String dispatchToPigeonhole = tsWorkSheetDeal.dispatchToPigeonhole(sheetBean, 600001141, "一般处理", "1");
		log.info("dispatchToPigeonhole result: {}", dispatchToPigeonhole);
		if (StringUtils.isNotEmpty(dispatchToPigeonhole)) {
			JSONObject result = JSONObject.fromObject(dispatchToPigeonhole);
			if (result.optString("code").equals("0000")) {
				//结案元素
				List<ServiceContentSave> saveList = this.getServiceContentSave();
				serviceContentSchem.saveDealContentSave(saveList, oldOrderId);

				int reasonId = 702020313;//业务规则 > 计费规则 > 有规则，用户不认可 > 小流量争议
				List banjieDirs = pubFunc.getBanjieDir(String.valueOf(reasonId), 2);
				Map banjieDir = (Map) banjieDirs.get(0);
				String ns = banjieDir.get("N").toString();
				String ids = banjieDir.get("ID").toString();
				TsSheetQualitative qualitative = new TsSheetQualitative();
				qualitative.setOrderId(oldOrderId);
				qualitative.setSheetId(sheetInfo.getWorkSheetId());
				qualitative.setRegion(sheetInfo.getRegionId());
				qualitative.setRegName(sheetInfo.getRegionName());
				qualitative.setMonthFlag(sheetInfo.getMonth());
				qualitative.setTsReasonId(reasonId);
				qualitative.setTsReasonName(ns.replace(" > ", "-"));
				qualitative.setTsifBeing(700001817);
				qualitative.setAppendCases(0);
				qualitative.setCasesId(0);
				qualitative.setCasesName("");
				qualitative.setDutyOrg("707907079");
				qualitative.setDutyOrgName("省公司其它");
				qualitative.setDutyOrgThird("");
				qualitative.setDutyOrgThirdName("");
				qualitative.setTsKeyWord(pubFunc.getSplitIdByIdx(ids, 0));
				qualitative.setTsKeyWordDesc(pubFunc.getSplitNameByIdx(ns, 0));
				qualitative.setSubKeyWord(pubFunc.getSplitIdByIdx(ids, 1));
				qualitative.setSubKeyWordDesc(pubFunc.getSplitNameByIdx(ns, 1));
				qualitative.setThreeCatalog(pubFunc.getSplitIdByIdx(ids, 2));
				qualitative.setThreeCatalogDesc(pubFunc.getSplitNameByIdx(ns, 2));
				qualitative.setThourCatalog(pubFunc.getSplitIdByIdx(ids, 3));
				qualitative.setThourCatalogDesc(pubFunc.getSplitNameByIdx(ns, 3));
				qualitative.setFiveCatalog(pubFunc.getSplitIdByIdx(ids, 4));
				qualitative.setFiveCatalogDesc(pubFunc.getSplitNameByIdx(ns, 4));
				qualitative.setSixCatalog(pubFunc.getSplitIdByIdx(ids, 5));
				qualitative.setSixCatalogDesc(pubFunc.getSplitNameByIdx(ns, 5));
				qualitative.setControlAreaFir(707907132);
				qualitative.setControlAreaFirDesc("企业无责");
				qualitative.setControlAreaSec(707907136);
				qualitative.setControlAreaSecDesc("客户原因");
				qualitative.setSatisfyId(600001166);
				qualitative.setSatisfyDesc("满意");
				qualitative.setForceFlag("70010106");
				qualitative.setForceFlagDesc("其他");
				qualitative.setUnsatisfyReason("");
				qualitative.setSysJudge("");
				qualitative.setLastDealContent("");
				qualitative.setPlusOne("");
				qualitative.setPlusOneDesc("");
				qualitative.setPlusTwo("");
				qualitative.setPlusTwoDesc("");
				qualitative.setOverTimeReasonId(0);
				qualitative.setOverTimeReasonDesc("");
				
				TScustomerVisit visit = new TScustomerVisit();
				visit.setServiceOrderId(oldOrderId);
				visit.setWorkSheetId(sheetInfo.getWorkSheetId());
				visit.setRegionId(sheetInfo.getRegionId());
				visit.setRegionName(sheetInfo.getRegionName());
				visit.setMonth(sheetInfo.getMonth());
				visit.setCollectivityCircs(600003380);
				visit.setCollectivityCircsName("未评价");
				visit.setTsDealAttitude(600003381);
				visit.setTsDealAttitudeName("未评价");
				visit.setTsDealBetimes(600003382);
				visit.setTsDealBetimesName("未评价");
				visit.setTsDealResult(600003383);
				visit.setTsDealResultName("未评价");
				visit.setTsVisitResult("未评价");
				visit.setVisitType("1");
				tsWorkSheetDeal.saveQualitativeAndVisit(qualitative, visit);
				tsSheetSumbitImpl.audSheetFinishAuto(oldOrderId, dealStaff.getLogonName());
			}
		}
		return dispatchToPigeonhole;
	}
	
	@SuppressWarnings("rawtypes")
	private TsmStaff getRefundDealStaff(String trackOrderId, int retStaffId) {
		log.info("trackOrderId: {} retStaffId: {}", trackOrderId, retStaffId);
		TsmStaff staff = null;
		try {
			if(StringUtils.isNotBlank(trackOrderId)) {//定时任务自动办结
				if("0".equals(trackOrderId)) {//未生成跟踪单
					staff = pubFunc.getStaff(retStaffId);
				} else {
					Map dealInfo = pubFunc.getLastDealInfoHis(trackOrderId);//查询跟踪单处理员工
					if(dealInfo != null) {
						String dealStaff = dealInfo.get("DEAL_STAFF").toString();
						staff = pubFunc.getStaff(Integer.parseInt(dealStaff));
					}
				}
			} else {//跟踪单处理员
				staff = pubFunc.getLogonStaff();
			}
		}catch(Exception e) {
			log.error("getRefundDealStaff: {}", e.getMessage(), e);
		}
		return staff;
	}
	
	private String fetchWorkSheet(SheetPubInfo sheetPubInfo, String trackOrderId, TsmStaff dealStaff) {
		if(sheetPubInfo == null) {
			return "NULL_SHEET";
		}
		
		int sheetState = sheetPubInfo.getLockFlag();
		if(ComplaintDealHandler.isHoldSheet(sheetPubInfo.getSheetStatu())) {//工单池挂起的工单不能直接提取
			return "WKST_HOLD_STATE";
		}
		
		if (sheetState == 0 && sheetPubInfo.getSheetStatu() != StaticData.WKST_ALLOT_STATE) {
			String sheetId = sheetPubInfo.getWorkSheetId();
			TsmStaff staff = this.getRefundDealStaff(trackOrderId, sheetPubInfo.getRetStaffId());
			if(staff == null) {
				return "STAFF_ERROR";
			}
			dealStaff.setLogonName(staff.getLogonName());
			log.info("dealStaff: {}", staff.getName() + "(" + staff.getLogonName() + ")");
			int staffId = Integer.parseInt(staff.getId());
			String staffName = staff.getName();
			String orgId = staff.getOrganizationId();
			String orgName = staff.getOrgName();
			
			// 更新提单员工信息及工单状态
			this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, staffId, staffName, orgId, orgName);
			
			// 得到要更新的状态
			int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());			
			String stateDesc = this.pubFunc.getStaticName(sheetStatu);
			this.sheetPubInfoDao.updateSheetState(sheetId, sheetStatu, stateDesc, sheetPubInfo.getMonth(), 1);
				
			// 记录工单动作			
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
		return "STATUS_ERROR";
	}
	
	private List<ServiceContentSave> getServiceContentSave() {
		List<ServiceContentSave> saveList = new ArrayList<>();
		ServiceContentSave save = new ServiceContentSave();
		save.setAliasName("reason");
		save.setAnswerName("小额退费");
		saveList.add(save);
		
		save = new ServiceContentSave();
		save.setAliasName("checkCtnt");
		save.setAnswerName("根据小额退赔规范退赔费用");
		saveList.add(save);
		
		save = new ServiceContentSave();
		save.setAliasName("isSlv");
		save.setAnswerName("已解决");
		saveList.add(save);
		
		save = new ServiceContentSave();
		save.setAliasName("unrslvRsn");
		save.setAnswerName("已解决");
		saveList.add(save);
		return saveList;
	}
	
	@SuppressWarnings("rawtypes")
	public void analysisOrder(OrderAskInfo orderAskInfo, ServiceContent servContent, OrderCustomerInfo custInfo) {
		try {
			Map tmpMap = trackServiceDao.getComplaintOrderType(orderAskInfo.getComeCategory(), orderAskInfo.getRelaType());
			if(!tmpMap.isEmpty()) {
				String status = tmpMap.get("STATUS").toString();//0-有效
				String type = tmpMap.get("TYPE").toString();//0-普通工单；1-包含最严工单
				log.info("雅典娜智能分析: {} status: {} type: {}", orderAskInfo.getRelaType(), status, type);
				if(!"0".equals(status)) {
					return;
				}
				
				TsmStaff logonStaff = pubFunc.getLogonStaff();
				Map<String, Object> orderDetail = new HashMap<>();
				orderDetail.put("query", servContent.getAcceptContent());
				orderDetail.put("serviceOrderId", orderAskInfo.getServOrderId());
				orderDetail.put("complaintType", orderAskInfo.getComment());
				orderDetail.put("dvlpChnl", servContent.getDvlpChnlNm());
				orderDetail.put("city", orderAskInfo.getRegionId());
				orderDetail.put("prodNum", orderAskInfo.getProdNum());
				orderDetail.put("brdNumber", custInfo.getPostCode());
				orderDetail.put("numberType", String.valueOf(custInfo.getProdType()));
				orderDetail.put("logonName", logonStaff.getLogonName());
				String jsonString = JSON.toJSONString(orderDetail);
				interfaceFeign.analysisOrder(jsonString);
			}
		}
		catch(Exception e) {
			log.error("analysisOrder: {}", e.getMessage(), e);
		}
	}
	
	/**
	 * 判断是否需要雅典娜赋能
	 */
	@SuppressWarnings("rawtypes")
	public boolean isAnalysisOrder(int comeCategory, int phnTypeId, int bestOrder) {
		try {
			Map tmpMap = trackServiceDao.getComplaintOrderType(comeCategory, phnTypeId);
			if(!tmpMap.isEmpty()) {
				String status = tmpMap.get("STATUS").toString();//0-有效
				String type = tmpMap.get("TYPE").toString();//0-普通工单；1-包含最严工单
				log.info("雅典娜智能分析: {} status: {} type: {}", phnTypeId, status, type);
				if(!"0".equals(status)) {
					return false;
				}
				if(bestOrder > 100122410 && "0".equals(type)){//最严工单
					return false;
				}
				return true;
			}
		}
		catch(Exception e) {
			log.error("isAnalysisOrder: {}", e.getMessage(), e);
		}
		return false;
	}
	
	public void getCouponByPhone(JSONObject requestJson, String orderId) {
		if(!requestJson.containsKey("cpSpecId")) {
			log.info("getCouponByPhone orderId: {} cpSpecId: null", orderId);
			return;
		}
		try {
			String cpSpecId = requestJson.optString("cpSpecId");
			String cpSpecName = requestJson.optString("cpSpecName");
			YNSJOrder order = (YNSJOrder)JSONObject.toBean(requestJson.optJSONObject("ynsjOrder"), YNSJOrder.class);
			
			JSONObject json = new JSONObject();
			json.put("cpSpecId", cpSpecId);
			json.put("cpSpecName", cpSpecName);
			json.put("prodNum", order.getProdNum());
			json.put("realInfo", order.getRealInfo());
			json.put("telNo", order.getTelNo());
			json.put("credentialNumbr", StringUtils.defaultIfBlank(order.getCredentialNumbr(), ""));
			json.put("custName", order.getCustName());
			json.put("orderId", orderId);
			interfaceFeign.getCouponByPhone(json.toString());
		}
		catch(Exception e) {
			log.error("getCouponByPhone: {}", e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	public int getUrgentOrderFlag(OrderAskInfo order, ServiceContent content) {
		//投诉 最严工单
		if(StaticData.SERV_TYPE_NEWTS != order.getServType() || content.getBestOrder() <= 100122410) {
    		return 0;
    	}
		try {
	    	Map tmpMap = trackServiceDao.getComplaintOrderType(16, content.getBestOrder());
			if(tmpMap.isEmpty()) {
				return 0;
			}
			
			String limitDay = pubFunc.querySysContolSwitchNew("urgentOrderDays");
			log.info("urgentOrderDays: {}", limitDay);
			if (!org.apache.commons.lang3.StringUtils.isNumeric(limitDay) || "0".equals(limitDay)) {
				return 0;
			}
	    	
			//判断近15天内是否有相同归属地、产品号码的投诉单（不区分是否最严）
			List orderList = orderAskInfoDao.queryUrgentOrder(Integer.parseInt(limitDay), order.getRegionId(), order.getProdNum());
			log.info("queryUrgentOrder size: {}", orderList.size());
			if (!orderList.isEmpty()) {
				log.info("queryUrgentOrder: {}", JSON.toJSON(orderList.get(0)));
				return 0;
			}
			
			//此单最严标识取消，打上标识“急”
			content.setBestOrder(100122410);
			content.setBestOrderDesc("否");
			log.info("最严工单改急: {}", order.getProdNum());
			return 1;
		} catch (Exception e) {
			log.error("getUrgentOrderFlag error: {}", e.getMessage(), e);
        }
		return 0;
    }
	
	public void saveComplaintOfferInfo(boolean cliqueFlag, JSONObject culiqueJson, OrderAskInfo orderAskInfo, OrderCustomerInfo custInfo) {
		if(StaticData.SERV_TYPE_NEWTS != orderAskInfo.getServType() || 707907003 != orderAskInfo.getComeCategory()) {// 申诉渠道 投诉单
			return;
		}
		if(!culiqueJson.has("complaintInfo")) {// 申诉单
			return;
		}
		ComplaintInfo info = (ComplaintInfo)JSONObject.toBean(culiqueJson.getJSONObject("complaintInfo"), ComplaintInfo.class);
		if(!pubFunc.judgeThirdLevel(info.getThirdLevel())) {// 分类码三级
			return;
		}
		
		String orderId = orderAskInfo.getServOrderId();
		int regionId = orderAskInfo.getRegionId();
		String prodNum = orderAskInfo.getProdNum();
		String miitCode = info.getMiitCode();
		String thirdLevel = StringUtils.trim(info.getThirdLevel());
		log.info("saveComplaintOfferInfo orderId: {} cliqueFlag: {}", orderId, cliqueFlag);
		
		Integer crmFlag = 0;//是否CRM用户：0-否；1-是
		int status = 0;//-1：无需匹配；0-未匹配
		if(cliqueFlag) {//集团来单
			if(StringUtils.isNotBlank(custInfo.getCrmCustId())) {
				crmFlag = 1;
			}
		}else {
			if(2 != regionId && 999 != regionId) {//江苏电信用户
				crmFlag = 1;
			}
		}
		trackServiceDao.saveComplaintOfferInfo(orderId, regionId, prodNum, miitCode, thirdLevel, crmFlag, status);
	}
	
	@SuppressWarnings("rawtypes")
	public String autofinishTrackOrder(String trackOrderId, String dealContent, int reasonId, String dealStaff) {
		log.info("自动办结小额退赔跟踪单: {} {}", trackOrderId, dealContent);
		SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(trackOrderId, StaticData.SHEET_TYPE_TS_ASSING, 0);
		
		TsmStaff staff = pubFunc.getLogonStaffByLoginName(dealStaff);
		if(staff == null) {
			return "STAFF_ERROR";
		}
		String fetchFlag = this.fetchWorkSheet(sheetInfo, staff);
		log.info("fetchWorkSheet result: {}", fetchFlag);
		if(!"SUCCESS".equals(fetchFlag)) {
			return ResultUtil.error(fetchFlag);
		}

		SheetPubInfo sheetBean = new SheetPubInfo();
		sheetBean.setServiceOrderId(trackOrderId);
		sheetBean.setWorkSheetId(sheetInfo.getWorkSheetId());
		sheetBean.setRcvOrgId("ASSIGN_TO_AUD");
		sheetBean.setRegionId(sheetInfo.getRegionId());
		sheetBean.setMonth(sheetInfo.getMonth());
		sheetBean.setDealContent(dealContent);
		String dispatchToPigeonhole = tsWorkSheetDeal.dispatchToPigeonhole(sheetBean, 600001141, "一般处理", "1");
		log.info("dispatchToPigeonhole result: {}", dispatchToPigeonhole);
		if (StringUtils.isNotEmpty(dispatchToPigeonhole)) {
			JSONObject result = JSONObject.fromObject(dispatchToPigeonhole);
			if (result.optString("code").equals("0000")) {
				List banjieDirs = pubFunc.getBanjieDir(String.valueOf(reasonId), 0);
				Map banjieDir = (Map) banjieDirs.get(0);
				String ns = banjieDir.get("N").toString();
				String ids = banjieDir.get("ID").toString();
				TsSheetQualitative qualitative = new TsSheetQualitative();
				qualitative.setOrderId(trackOrderId);
				qualitative.setSheetId(sheetInfo.getWorkSheetId());
				qualitative.setRegion(sheetInfo.getRegionId());
				qualitative.setRegName(sheetInfo.getRegionName());
				qualitative.setMonthFlag(sheetInfo.getMonth());
				qualitative.setTsReasonId(reasonId);
				qualitative.setTsReasonName(ns.replace(" > ", "-"));
				qualitative.setTsifBeing(700001817);
				qualitative.setAppendCases(0);
				qualitative.setCasesId(0);
				qualitative.setCasesName("");
				qualitative.setDutyOrg("707907079");
				qualitative.setDutyOrgName("省公司其它");
				qualitative.setDutyOrgThird("");
				qualitative.setDutyOrgThirdName("");
				qualitative.setTsKeyWord(pubFunc.getSplitIdByIdx(ids, 0));
				qualitative.setTsKeyWordDesc(pubFunc.getSplitNameByIdx(ns, 0));
				qualitative.setSubKeyWord(pubFunc.getSplitIdByIdx(ids, 1));
				qualitative.setSubKeyWordDesc(pubFunc.getSplitNameByIdx(ns, 1));
				qualitative.setThreeCatalog(pubFunc.getSplitIdByIdx(ids, 2));
				qualitative.setThreeCatalogDesc(pubFunc.getSplitNameByIdx(ns, 2));
				qualitative.setThourCatalog(pubFunc.getSplitIdByIdx(ids, 3));
				qualitative.setThourCatalogDesc(pubFunc.getSplitNameByIdx(ns, 3));
				qualitative.setFiveCatalog(pubFunc.getSplitIdByIdx(ids, 4));
				qualitative.setFiveCatalogDesc(pubFunc.getSplitNameByIdx(ns, 4));
				qualitative.setSixCatalog(pubFunc.getSplitIdByIdx(ids, 5));
				qualitative.setSixCatalogDesc(pubFunc.getSplitNameByIdx(ns, 5));
				qualitative.setControlAreaFir(707907132);
				qualitative.setControlAreaFirDesc("企业无责");
				qualitative.setControlAreaSec(707907136);
				qualitative.setControlAreaSecDesc("客户原因");
				qualitative.setSatisfyId(600001166);
				qualitative.setSatisfyDesc("满意");
				qualitative.setForceFlag("70010106");
				qualitative.setForceFlagDesc("其他");
				qualitative.setUnsatisfyReason("");
				qualitative.setSysJudge("");
				qualitative.setLastDealContent("");
				qualitative.setPlusOne("");
				qualitative.setPlusOneDesc("");
				qualitative.setPlusTwo("");
				qualitative.setPlusTwoDesc("");
				qualitative.setOverTimeReasonId(0);
				qualitative.setOverTimeReasonDesc("");
				qualitative.setReasonable(1);
				
				TScustomerVisit visit = new TScustomerVisit();
				visit.setServiceOrderId(trackOrderId);
				visit.setWorkSheetId(sheetInfo.getWorkSheetId());
				visit.setRegionId(sheetInfo.getRegionId());
				visit.setRegionName(sheetInfo.getRegionName());
				visit.setMonth(sheetInfo.getMonth());
				visit.setCollectivityCircs(600003380);
				visit.setCollectivityCircsName("未评价");
				visit.setTsDealAttitude(600003381);
				visit.setTsDealAttitudeName("未评价");
				visit.setTsDealBetimes(600003382);
				visit.setTsDealBetimesName("未评价");
				visit.setTsDealResult(600003383);
				visit.setTsDealResultName("未评价");
				visit.setTsVisitResult("未评价");
				visit.setVisitType("1");
				tsWorkSheetDeal.saveQualitativeAndVisit(qualitative, visit);
				tsSheetSumbitImpl.audSheetFinishAuto(trackOrderId, staff.getLogonName());
			}
		}
		return dispatchToPigeonhole;
	}
	
	private String fetchWorkSheet(SheetPubInfo sheetPubInfo, TsmStaff staff) {
		if (sheetPubInfo == null) {
			return "NULL_SHEET";
		}
		int lockFlag = sheetPubInfo.getLockFlag();
		log.info("orderId: {} sheetId: {} lockFlag: {}", sheetPubInfo.getServiceOrderId(), sheetPubInfo.getWorkSheetId(), lockFlag);
		if (ComplaintDealHandler.isHoldSheet(sheetPubInfo.getSheetStatu())) {//挂起的工单不能处理
			return "WKST_HOLD_STATE";
		}
		if (lockFlag == 0 && sheetPubInfo.getSheetStatu() != StaticData.WKST_ALLOT_STATE) {//工单池提取
			String sheetId = sheetPubInfo.getWorkSheetId();
			int staffId = Integer.parseInt(staff.getId());
			String staffName = staff.getName();
			String orgId = staff.getOrganizationId();
			String orgName = staff.getOrgName();
			
			// 更新提单员工信息及工单状态
			this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, staffId, staffName, orgId, orgName);
			
			// 得到要更新的状态
			int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());
			String stateDesc = this.pubFunc.getStaticName(sheetStatu);
			this.sheetPubInfoDao.updateSheetState(sheetId, sheetStatu, stateDesc, sheetPubInfo.getMonth(), 1);
				
			// 记录工单动作
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
		else if (lockFlag == 1 && sheetPubInfo.getSheetStatu() == StaticData.WKST_DEALING_STATE) {//我的任务处理中，无需提取
			return "SUCCESS";
		}
		return "STATUS_ERROR";
	}
	
	/**
	 * 投诉单生成小额退赔单
	 * @param parm
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String createRefundTrackOrder(String parm) {
		log.info("createRefundTrackOrder: {}", parm);
		
		JSONObject json = JSONObject.fromObject(parm);
		TrackInfo track = (TrackInfo)JSONObject.toBean(json.optJSONObject("track"),TrackInfo.class);
		
		TsmStaff staff = null;
		String zhzdFlag = "0";
		if(json.containsKey("acceptStaff")) {//接口受理
			String acceptStaff = json.optString("acceptStaff");
			staff = pubFunc.getLogonStaffByLoginName(acceptStaff);
			zhzdFlag = "1";
		} else {
			staff = pubFunc.getLogonStaff();
		}
		log.info("acceptStaff: {} {}", staff.getLogonName(), staff.getName());
		
		//查询原单信息
		ServiceOrderInfo info = askImpl.getServOrderInfo(track.getOldOrderId(), false);//当前表
		if(info == null) {
			info = askImpl.getServOrderInfo(track.getOldOrderId(), true);//历史表
		}
		if(info == null) {
			return ResultUtil.fail(ResultEnum.OBJERROR);
		}
		
		//受理模板
		JSONArray sas = json.optJSONArray("serviceContentSave");
		ServiceContentSave[] saves = null;
		if(!sas.isEmpty()) {
			saves = new ServiceContentSave[sas.size()];
			for(int i=0;i<sas.size();i++) {
				ServiceContentSave ww = (ServiceContentSave)JSONObject.toBean(sas.getJSONObject(i), ServiceContentSave.class);
				saves[i] = ww;
			}
		}
		//设置退费数据
		this.setRefundData(saves, json);
		saves = serviceContentSchem.filterRefundData(saves);//过滤退费数据
		
		//投诉现象
		int lastDirId = 2020501;//小额退赔
		List catalogList = pubFunc.queryAcceptDir(lastDirId, 2);
		Map catalogMap = (Map) catalogList.get(0);
		String ns = catalogMap.get("N").toString();
		String ids = catalogMap.get("ID").toString();
		
		//获取退费产品号码、产品类型
		String accNum = json.optString("accNum");
		String prodType = json.optString("prodType");
		String prodTypeDesc = json.optString("prodTypeDesc");
		
		//服务信息
		OrderAskInfo orderAskInfo = info.getOrderAskInfo();
		orderAskInfo.setServOrderId("");
		orderAskInfo.setCallSerialNo("N");
		orderAskInfo.setOrderVer(0);
		orderAskInfo.setServType(720200002);
		orderAskInfo.setServTypeDesc("特别跟踪");
		orderAskInfo.setCustId("");
		orderAskInfo.setAskStaffId(Integer.parseInt(staff.getId()));
		orderAskInfo.setAskStaffName(staff.getName());
		orderAskInfo.setAskOrgId(staff.getOrganizationId());
		orderAskInfo.setAskOrgName(staff.getOrgName());
		orderAskInfo.setModifyDate("");
		orderAskInfo.setAskCount(1);
		orderAskInfo.setOrderStatu(0);
		orderAskInfo.setOrderStatuDesc("");
		orderAskInfo.setRelaType(lastDirId);
		orderAskInfo.setComment(catalogMap.get("N").toString());
		//重新设置产品号码
		orderAskInfo.setProdNum(StringUtils.defaultIfBlank(accNum, orderAskInfo.getProdNum()));
		
		//受理内容
		ServiceContent servContent = info.getServContent();
		servContent.setServOrderId("");
		servContent.setOrderVer(0);
		servContent.setServType(720200002);
		servContent.setServTypeDesc("特别跟踪");
		servContent.setServiceTypeDetail(this.getTrackTypeDesc(track.getTrackType()));
		servContent.setBestOrder(100122410);
		servContent.setBestOrderDesc("否");
		servContent.setAppealProdId(pubFunc.getSplitIdByIdx(ids, 0));
		servContent.setAppealProdName(pubFunc.getSplitNameByIdx(ns, 0));
		servContent.setAppealReasonId(pubFunc.getSplitIdByIdx(ids, 1));
		servContent.setAppealReasonDesc(pubFunc.getSplitNameByIdx(ns, 1));
		servContent.setAppealChild(pubFunc.getSplitIdByIdx(ids, 2));
		servContent.setAppealChildDesc(pubFunc.getSplitNameByIdx(ns, 2));
		servContent.setFouGradeCatalog(pubFunc.getSplitIdByIdx(ids, 3));
		servContent.setFouGradeDesc(pubFunc.getSplitNameByIdx(ns, 3));
		servContent.setFiveCatalog(pubFunc.getSplitIdByIdx(ids, 4));
		servContent.setFiveGradeDesc(pubFunc.getSplitNameByIdx(ns, 4));
		servContent.setSixCatalog(pubFunc.getSplitIdByIdx(ids, 5));
		servContent.setSixGradeDesc(pubFunc.getSplitNameByIdx(ns, 5));
		servContent.setAcceptContent(StringUtils.substring(json.getString("acceptContent"), 0, 4000));//受理内容截取
		//重新设置产品号码
		servContent.setProdNum(StringUtils.defaultIfBlank(accNum, orderAskInfo.getProdNum()));
		
		//客户信息
		OrderCustomerInfo orderCustInfo = info.getOrderCustInfo();
		orderCustInfo.setCustGuid("");
		//根据传入的产品规格设置产品类型
		this.setProdType(prodType, prodTypeDesc, info.getOrderCustInfo(), orderCustInfo);
		
		Map audMap = new HashMap();
		audMap.put("ORGIDSTR", "");
		audMap.put("DEALREQUIE", "");
		audMap.put("SENDFLAG", "false");
		audMap.put("STRFLOW", "DISPATCHSHEET");
		audMap.put("SEND_TO_OBJ_FLAG", json.optString("isDispatch"));//1-部门 0-员工
		audMap.put("SEND_TO_OBJ_ID", json.optString("sendToObjId"));
		audMap.put("ZHZDFLAG", zhzdFlag);
		
		String result = ResultUtil.fail(ResultEnum.FALL);
		try {
			result = askImpl.submitServiceOrderInstanceLabelNew(orderCustInfo, servContent, orderAskInfo, audMap, null, saves, null);
		} catch (Exception e) {
			result = ResultUtil.error(e.getMessage());
		}
		
		String code = JSONObject.fromObject(result).optString("code");
		if("0000".equals(code)) {
			String newOrderId = orderAskInfo.getServOrderId();
			OrderAskInfo newOrder = orderAskInfoDao.getOrderAskInfo(newOrderId, false);
			if(newOrder != null) {
				track.setNewOrderId(newOrderId);
				track.setCreateDate(newOrder.getAskDate());
				int count = trackServiceDao.saveTrackInfoTz(track);
				if(count > 0) {
					this.saveRefundOrder(json, track);//保存退费数据
					return ResultUtil.success(newOrder.getServOrderId());
				}
			}
		}
		return ResultUtil.fail(ResultEnum.ERROR);
	}
	
	/**
	 * 设置产品类型
	 * @param prodType
	 * @param prodTypeDesc
	 * @param oldCustInfo
	 * @param newCustInfo
	 */
	private void setProdType(String prodType, String prodTypeDesc, OrderCustomerInfo oldCustInfo, OrderCustomerInfo newCustInfo) {
		int type = 0;
		String desc = "";
		try {
			if(StringUtils.isNotBlank(prodType) && StringUtils.isNotBlank(prodTypeDesc)) {
				type = Integer.parseInt(prodType);
				desc = prodTypeDesc;
			} else {
				type = oldCustInfo.getProdType();
				desc = oldCustInfo.getProdTypeDesc();
			}
		} catch (Exception e) {
			log.error("setProdType error: {}", e.getMessage(), e);
		}
		newCustInfo.setProdType(type);
		newCustInfo.setProdTypeDesc(desc);
	}
	
	private void setRefundData(ServiceContentSave[] saves, JSONObject parm) {
		String refundData = "";
		String refundsAccNum = "";
		String refundAmount = "";
		String prmRefundAmount = "";
		try {
			if(saves != null && saves.length > 0) {
				for(int i=0;i<saves.length;i++) {
					ServiceContentSave ww = saves[i];
					if("c2f9995733b843c8393cc78629cd9220".equals(ww.getElementId())) {//退费数据
						//去掉特殊符号
						refundData = org.apache.commons.lang3.StringUtils.removeEnd(org.apache.commons.lang3.StringUtils.removeStart(ww.getAnswerName(), "【"), "】");
					}
					if("b6b2882c9e1811ee89ee005056b35a1f".equals(ww.getElementId())) {//承诺退费金额
						prmRefundAmount = ww.getAnswerName();
					}
				}
			}
			if(org.apache.commons.lang3.StringUtils.isNotBlank(refundData)) {
				JSONObject json = JSONObject.fromObject(refundData);
				refundsAccNum = json.optString("refundsAccNum");
				refundAmount = json.optString("cashAmount");
			}
		} catch (Exception e) {
			log.error("setRefundData error: {}", e.getMessage(), e);
		}
		log.info("refundData: {}", refundData);
		
		parm.put("refundData", refundData);
		parm.put("refundsAccNum", refundsAccNum);
		parm.put("refundAmount", refundAmount);
		parm.put("prmRefundAmount", prmRefundAmount);
	}
	
	public String qryRepeatOrderByProdNum(String param) {
		com.alibaba.fastjson.JSONObject json = JSON.parseObject(param);
		String oldOrderId = json.getString("oldOrderId");
		String prodNum = json.getString("prodNum");
		int num = trackServiceDao.qryRepeatOrderByProdNum(oldOrderId, prodNum);
		return ResultUtil.success(num);
	}
	
	public String isExistRefundOrder(String param) {
		com.alibaba.fastjson.JSONObject json = JSON.parseObject(param);
		String oldOrderId = json.getString("oldOrderId");
		int num = trackServiceDao.isExistRefundOrder(oldOrderId);
		return ResultUtil.success(num);
	}
	
	/**
	 * 保存一键录单信息
	 * @param loginId
	 * @param info
	 */
	public void saveAcceptOrderInfo(String loginId, OrderAskInfo info) {
		log.info("loginId: {}", loginId);
		if(StringUtils.isNotBlank(loginId)) {
			trackServiceDao.saveAcceptOrderInfo(loginId, info);
		}
	}
	
	/**
	 * 判断是否受理时，是否能看到敏感话务现象
	 */
	@SuppressWarnings("rawtypes")
	public boolean isSensitiveFlag() {
		try {
			TsmStaff staff = pubFunc.getLogonStaff();
			Map tmpMap = trackServiceDao.getComplaintOrderType(17, Integer.parseInt(staff.getId()));
			log.info("isSensitiveFlag {} {}", staff.getLogonName(), JSON.toJSON(tmpMap));
			if(!tmpMap.isEmpty()) {
				String status = tmpMap.get("STATUS").toString();//0-有效
				if(!"0".equals(status)) {
					return false;
				}
				return true;
			}
		}
		catch(Exception e) {
			log.error("isSensitiveFlag: {}", e.getMessage(), e);
		}
		return false;
	}
	
	public String createMultiRefundTrackOrder(String param) {
		log.info("createMultiRefundTrackOrder: {}", param);
		JSONObject reqJson = JSONObject.fromObject(param);
		JSONArray arr = reqJson.optJSONArray("refundList");	
		int sum = 0;
		for(int i=0; i<arr.size(); i++) {
			JSONObject obj = arr.getJSONObject(i);
			
			String data = obj.getJSONObject("data").toString();
			//生成小额退赔跟踪单
			String result = this.createRefundTrackOrder(data);
			log.info("createRefundTrackOrder: {}", result);
			JSONObject resultJson = JSONObject.fromObject(result);
			
			String code = resultJson.optString("code");
			if("0000".equals(code)) {
				String trackOrderId = resultJson.optString("resultObj");
				String orderId = obj.optString("orderId");
				String refundNum = obj.optString("accNum");
				String prmRefundAmount = obj.optString("prmRefundAmount");
				String prmRefundSumAmount = obj.optString("prmRefundSumAmount");
				if(StringUtils.isBlank(orderId)) {//服务单号
					orderId = this.getServiceOrderId(obj);
					prmRefundSumAmount = prmRefundAmount;
				}
				int num = trackServiceDao.saveMultiProdRefundInfo(trackOrderId, orderId, refundNum, prmRefundAmount, prmRefundSumAmount);
				sum += num;
			}
		}
		if(sum > 0) {
			String xcSheetId = reqJson.optString("xcSheetId");//调账审批单号
			if(StringUtils.isNotBlank(xcSheetId)) {
		        String loginName = pubFunc.getLogonStaff().getLogonName();
		        //审批记录已生成调账
				trackServiceDao.updateApprovedRefundInfo(xcSheetId, 3, loginName);
			}
			return ResultUtil.success(sum);
		}
		return ResultUtil.error("");
	}
	
	private String getServiceOrderId(JSONObject obj) {
		JSONObject data = obj.getJSONObject("data");
		TrackInfo track = (TrackInfo)JSONObject.toBean(data.optJSONObject("track"),TrackInfo.class);
		return track.getOldOrderId();
	}
}
