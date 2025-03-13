package com.timesontransfar.complaintservice.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.timesontransfar.complaintservice.pojo.FinAssessInfo;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
import com.timesontransfar.complaintservice.service.IRetrieveEvaluation;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ItsCustomerVisit;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.dapd.service.IdapdSheetInfoService;
import com.timesontransfar.feign.clique.AccessCliqueServiceFeign;
import com.transfar.common.log.CustomLogger;
import com.transfar.common.log.LogBean;

import net.sf.json.JSONObject;

@Component
@SuppressWarnings("rawtypes")
public class RetrieveEvaluationServiceImpl implements IRetrieveEvaluation {
	@Autowired
	private CustomLogger log;
	@Autowired
	private ItsCustomerVisit tscustomerVisitDao;
	@Autowired
	private PubFunc pubFun;
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private IComplaintWorksheetDeal complaintWorksheetDeal;
	@Autowired
	private ItsSheetQualitative sheetQualitative;
	@Autowired
	private WorkSheetAllot workSheetAllot;
	@Autowired
	private AccessCliqueServiceFeign accessCliqueServiceFeign;
	@Autowired
	private IdapdSheetInfoService dapdSheetService;

	private static final String LOGTYPE = "interface";
	private static final String METHOD_NAME = "RETRIEVE_EVALUATION";
	private static final String SUCCESS_CODE = "SUCCESS";
	private static final String ERROR_CODE = "ERROR";
	private static final String OVER_MSG = "已完成自动回访或已超时";

	// code 集团结果 省内满意度 回访结果 不满意修复流程未上线前 上线后
	// 1 未解决 未解决（增加） 未解决 预定性进终定性，终定性进人工岗 进满意度修复
	// 2 不清楚 不清楚（增加） 不清楚 预定性进终定性，终定性进人工岗 进满意度修复
	// 3 不满意 不满意 本次测评1-8分，需改进的方面：投诉受理不通畅；投诉处理时间长。。。。 预定性进终定性，终定性进人工岗 进满意度修复
	// 4 满意 满意 本次测评9-10分 直接办结 直接办结
	// 5 超时 未评价 未评价 直接办结 直接办结
	public void retrieveEvaluation(String orderId, String code, String msg, String joinMode) {
		log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, null, orderId + "|" + code + "|" + msg + "|" + joinMode, null));
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		if (orderAskInfo == null) {
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, "没有找到订单ID:" + orderId + "的数据"));
			return;
		}
		if (StaticData.OR_AUTOVISIT_STATU != orderAskInfo.getOrderStatu()) {
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, "该订单:" + orderId + OVER_MSG));
			return;
		}
		String str = " AND service_order_id = '" + orderId + "' AND tache_id = 720130025 ORDER BY creat_date DESC";
		List sheetList = sheetPubInfoDao.getSheetListCondition(str, true);
		if (sheetList.isEmpty()) {
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, "该订单:" + orderId + OVER_MSG));
			return;
		}
		SheetPubInfo sheetPubInfo = (SheetPubInfo) sheetList.get(0);
		int tacheId = sheetPubInfo.getTacheId();
		int sheetStatu = sheetPubInfo.getSheetStatu();
		int orderType = 0;// 进入自动回访环节：1、预定性结束，2、终定性结束
		if (720130025 == tacheId && 720130033 == sheetStatu) {
			orderType = 1;
		} else if (720130025 == tacheId && 720130036 == sheetStatu) {
			orderType = 2;
		}
		if (0 == orderType) {
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, "该订单:" + orderId + OVER_MSG));
			return;
		}
		String visitType = "4";
		// 参评方式编码（10：短信、11：微信、12：APP；2：智能外呼；3：人工外呼）
		switch (joinMode) {
		case "10":
			visitType = "4";
			break;
		case "11":
			visitType = "5";
			break;
		case "12":
			visitType = "6";
			break;
		case "2":
			visitType = "7";
			break;
		case "3":
			visitType = "8";
			break;
		default:
			break;
		}
		dealEvaluation(orderAskInfo, sheetPubInfo, code, msg, orderType, visitType);
	}

	private void dealEvaluation(OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo, String code, String msg, int orderType, String visitType) {
		String orderId = sheetPubInfo.getServiceOrderId();
		boolean isFinish = true; // score为4、5时，完结订单；否则进入终定性环节
		int autoVisitFlag = 1; // 自动回访状态标识：0默认、1自动回访办结、2自动回访转人工、3自动回访接口异常
		int reportNum = 6;// 回访原因标识：【1】0超时、1不满意、3一般、5满意、6未评价、7未解决、8不清楚，【2】1不满意、5集团来单、6省内管控单超时和未评价、7未解决、8不清楚，【3】2规定时间无结果
		String satEval = "1";// SAT_EVAL客户满意测评：0未回访、1用户未参评、2满意、3不满意
		int collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BPJ;// 总体情况
		int tsDealResult = StaticData.TS_DEAL_RESULT_BPJ;// 投诉处理结果
		int tsDealBetimes = StaticData.TS_DEAL_BETIMES_BPJ;// 投诉处理及时性
		int tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BPJ;// 投诉处理态度
		// 以上默认为：未评价
		switch (code) {
		case "1": // 未解决
			reportNum = 7;
			satEval = "3";
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_WJJ;
			tsDealResult = StaticData.TS_DEAL_RESULT_WJJ;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_WJJ;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_WJJ;
			break;
		case "2": // 不清楚
			reportNum = 8;
			satEval = "3";
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BQC;
			tsDealResult = StaticData.TS_DEAL_RESULT_BQC;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BQC;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BQC;
			break;
		case "3":// 不满意
			reportNum = 1;
			satEval = "3";
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BMY;
			tsDealResult = StaticData.TS_DEAL_RESULT_BMY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BMY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BMY;
			break;
		case "4":// 满意
			reportNum = 5;
			satEval = "2";
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_MY;
			tsDealResult = StaticData.TS_DEAL_RESULT_MY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_MY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_MY;
			break;
		default:
			break;
		}
		TScustomerVisit visit = new TScustomerVisit();
		visit.setServiceOrderId(orderId);
		visit.setCollectivityCircs(collectivityCircs);
		visit.setCollectivityCircsName(pubFun.getStaticName(collectivityCircs));
		visit.setTsDealAttitude(tsDealAttitude);
		visit.setTsDealAttitudeName(pubFun.getStaticName(tsDealAttitude));
		visit.setTsDealBetimes(tsDealBetimes);
		visit.setTsDealBetimesName(pubFun.getStaticName(tsDealBetimes));
		visit.setTsDealResult(tsDealResult);
		visit.setTsDealResultName(pubFun.getStaticName(tsDealResult));
		visit.setTsVisitResult(msg);
		visit.setReplyData(pubFun.getSysDate());
		visit.setMonth(orderAskInfo.getMonth());
		visit.setRegionId(sheetPubInfo.getRegionId());
		visit.setRegionName(sheetPubInfo.getRegionName());
		visit.setVisitType(visitType);
		dapdSheetService.setDapdSatEval(orderId, satEval);
		if (1 == orderType) {
			submitYdx(orderAskInfo, sheetPubInfo, isFinish, visit, autoVisitFlag, reportNum, tsDealResult);
			if ("".equals(visit.getWorkSheetId())) {// 未查询到工单定性记录
				log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, "该订单:" + orderId + OVER_MSG));
			}
		} else if (2 == orderType) {
			finishFinAssess(sheetPubInfo, isFinish, visit, autoVisitFlag, reportNum);
		}
		log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, SUCCESS_CODE, null, ""));
	}

	private void submitYdx(OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo, boolean isFinish, TScustomerVisit visit, int autoVisitFlag, int reportNum, int dealSatisfy) {
		String preSheetId = sheetPubInfo.getSourceSheetId();
		int regionId = sheetPubInfo.getRegionId();
		TsSheetQualitative[] qualitatives = sheetQualitative.getTsSheetQualitative(preSheetId, regionId, true);
		if (qualitatives.length >= 1) {
			TsSheetQualitative qualitative = qualitatives[qualitatives.length - 1];
			sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 1, preSheetId);
			if (isFinish) {
				dinishYdx(orderAskInfo, sheetPubInfo, visit, qualitative, dealSatisfy);
			} else {
				toZdx(orderAskInfo, sheetPubInfo, visit);
			}
		}
	}

	private void dinishYdx(OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo, TScustomerVisit visit, TsSheetQualitative qualitative, int dealSatisfy) {
		String orderId = sheetPubInfo.getServiceOrderId();
		String finSheetId = sheetPubInfo.getWorkSheetId();
		String preSheetId = sheetPubInfo.getSourceSheetId();
		int month = sheetPubInfo.getMonth();
		int regionId = sheetPubInfo.getRegionId();
		pubFun.updateOrderAndSheetHang(finSheetId, orderId);
		// 更新订单状态为终定性
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_FINQUALITATIVE_STATU, orderAskInfo.getMonth(), pubFun.getStaticName(StaticData.OR_FINQUALITATIVE_STATU));
		// 投诉定性定责内容
		qualitative.setSheetId(finSheetId);
		qualitative.setSatisfyId(dealSatisfy);
		qualitative.setSatisfyDesc(pubFun.getStaticName(dealSatisfy));
		visit.setWorkSheetId(preSheetId);
		if (sheetPubInfo.getLockFlag() == 0) {// 若终定性单在大库中，则将工单状态从待提取改为已提
			sheetPubInfoDao.updateSheetState(finSheetId, StaticData.WKST_DEALING_STATE_NEW, pubFun.getStaticName(StaticData.WKST_DEALING_STATE_NEW), 14, 1);
		}
		FinAssessInfo finAssessInfo = new FinAssessInfo();
		finAssessInfo.setQualitative(qualitative);
		finAssessInfo.setCustomerVisit(visit);
		finAssessInfo.setRegionId(regionId);
		finAssessInfo.setMonth(month);
		finAssessInfo.setDealContent("集团即时测评工单完结");
		finAssessInfo.setLogonFlag(0);
		finAssessInfo.setValiFlag(3);
		finAssessInfo.setUpgradeIncline(2);
		finAssessInfo.setContactStatus("");
		finAssessInfo.setRequireUninvited("");
		finAssessInfo.setUnifiedCode("");
		finAssessInfo.setUccJTSS("");
		String result = complaintWorksheetDeal.submitFinAssess(finAssessInfo);
		if (SUCCESS_CODE.equals(result)) {
			ComplaintRelation cmpRelaOper = pubFun.queryListByOid(orderId);
			if (cmpRelaOper != null && cmpRelaOper.getAssignType() == 3) {
				JSONObject info = new JSONObject();
				info.put("complaintWorksheetId", cmpRelaOper.getComplaintWorksheetId());
				info.put("serviceOrderId", orderId);
				info.put("reason", "省内已处理，请集团归档");
				info.put("type", "FINISH");
				accessCliqueServiceFeign.accessCliqueNew(info.toString());
			}
		}
	}

	private void toZdx(OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo, TScustomerVisit visit) {
		String orderId = sheetPubInfo.getServiceOrderId();
		String finSheetId = sheetPubInfo.getWorkSheetId();
		String preSheetId = sheetPubInfo.getSourceSheetId();
		pubFun.updateOrderAndSheetHang(finSheetId, orderId);
		// 更新订单状态为终定性
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_FINQUALITATIVE_STATU, orderAskInfo.getMonth(), pubFun.getStaticName(StaticData.OR_FINQUALITATIVE_STATU));
		visit.setWorkSheetId(preSheetId);
		tscustomerVisitDao.saveCustomerVisit(visit);
		String stateDesc = pubFun.getStaticName(StaticData.WKST_REPEAL_STATE_NEW);
		// 更新终定性工单状态为待处理 、 更新到岗时间、 更新lock_flag(0:部门工单池 1：我的任务 2：已完成)
		sheetPubInfoDao.updateSheetState(finSheetId, StaticData.WKST_REPEAL_STATE_NEW, stateDesc, 14, 0);
		// 自动派发终定性工单
		SheetPubInfo finSheet = sheetPubInfoDao.getSheetPubInfo(finSheetId, false);
		String result = workSheetAllot.allotToFinAssessAddForce(finSheet);
		if (SUCCESS_CODE.equals(result)) {
			sheetPubInfoDao.updateFetchSheetStaff(finSheetId, finSheet.getRcvStaffId(), finSheet.getRcvStaffName(), finSheet.getRcvOrgId(), finSheet.getRcvOrgName());
			sheetPubInfoDao.updateSheetState(finSheetId, finSheet.getSheetStatu(), finSheet.getSheetSatuDesc(), finSheet.getMonth(), 1);
		}
	}

	private void finishFinAssess(SheetPubInfo sheetPubInfo, boolean isFinish, TScustomerVisit visit, int autoVisitFlag, int reportNum) {
		String orderId = sheetPubInfo.getServiceOrderId();
		String finSheetId = sheetPubInfo.getWorkSheetId();
		int month = sheetPubInfo.getMonth();
		int regionId = sheetPubInfo.getRegionId();
		visit.setWorkSheetId(finSheetId);
		tscustomerVisitDao.saveCustomerVisit(visit);
		pubFun.updateOrderHang(finSheetId, orderId);
		if (isFinish) {
			sheetPubInfoDao.updateAutoVisit(1, reportNum, 2, finSheetId);
			complaintWorksheetDeal.submitFinAssessFromVisit(finSheetId, orderId, regionId, month);
		} else {
			sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 2, finSheetId);
			complaintWorksheetDeal.toRGHFFromVisit(finSheetId, orderId, regionId, month);
		}
	}
}