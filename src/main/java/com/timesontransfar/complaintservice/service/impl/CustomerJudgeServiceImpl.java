package com.timesontransfar.complaintservice.service.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.pojo.FinAssessInfo;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
import com.timesontransfar.complaintservice.service.ICustomerJudgeService;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.service.impl.CompWorksheetFullWebService;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ItsCustomerVisit;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedReturnDAOImpl;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.timesontransfar.dapd.service.IdapdSheetInfoService;
import com.timesontransfar.evaluation.SatisfyInfo;
import com.timesontransfar.evaluation.service.EvaluationService;
import com.timesontransfar.feign.clique.AccessCliqueServiceFeign;
import com.timesontransfar.pubWebservice.IAutoCustomerVisit;

import net.sf.json.JSONObject;

@Service
public class CustomerJudgeServiceImpl implements ICustomerJudgeService {
	protected Logger log = LoggerFactory.getLogger(CustomerJudgeServiceImpl.class);

	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private ItsSheetQualitative tsSheetQualitative;
	@Autowired
	private ItsCustomerVisit tsCustomerVisit;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private ILabelManageDAO labelManageDAO;
	@Autowired
	private IAutoCustomerVisit autoCustomerVisit;
	@Autowired
	private WorkSheetAllot workSheetAllot;
	@Autowired
	private IComplaintWorksheetDeal complaintWorksheetDeal;
	@Autowired
	private AccessCliqueServiceFeign accessCliqueServiceFeign;
	@Autowired
	private CompWorksheetFullWebService compWorksheetFullWebService;
	@Autowired
	private CmpUnifiedReturnDAOImpl cmpUnifiedReturnDAOImpl;
	@Autowired
	private ItsWorkSheetDeal itsWorkSheetDeal;
	@Autowired
	private IworkSheetBusi workSheetBusi;
	@Autowired
	private IdapdSheetInfoService dapdSheetService;
	@Autowired
	private EvaluationService evaluation;

	private static final String SATISFY_DEGREE = "SATISFY_DEGREE";
	private static final String JUDGEDATE = "JUDGE_DATE";
	private static final String SUCCESS_CODE = "SUCCESS";
	private static final String SATISFY_DEGREE_1 = "即时测评结果满意";
	private static final String SATISFY_DEGREE_2 = "即时测评结果一般";
	private static final String SATISFY_DEGREE_3 = "服务态度冷淡";
	private static final String SATISFY_DEGREE_4 = "业务解释听不懂";
	private static final String SATISFY_DEGREE_5 = "处理速度慢";
	private static final String SATISFY_DEGREE_6 = "处理方案未达到期望";
	private static final String SATISFY_DEGREE_7 = "问题未解决";
	private static final String SATISFY_DEGREE_0 = "即时测评未评价";

	@SuppressWarnings("rawtypes")
	public String enterJudge(String orderId) {
		Map cj = sheetPubInfoDao.selectCustomerJudgeByOrderId(orderId);
		log.info("CustomerJudge：{}", cj);
		if (cj.isEmpty()) {
			return "评价表中数据不存在";
		}
		if (!"0".equals(cj.get("JUDGE_STATUS").toString())) {
			return "重复调用该单号";
		}
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		log.info("orderInfo：{}", orderInfo);
		if (orderInfo == null) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(7, orderId);
			return "订单表没有找到该单号";
		}
		if (StaticData.OR_AUTOVISIT_STATU != orderInfo.getOrderStatu()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(7, orderId);
			return "订单状态不是回访中";
		}
		String tacheType = cj.get("TACHE_TYPE").toString();
		String serviceType = judgeServiceType(tacheType);
		if ("TS".equals(serviceType)) {
			String unifiedCode = cj.get("UNIFIED_COMPLAINT_CODE").toString();
			return enterJudgeTS(orderInfo, tacheType, unifiedCode);
		} else if ("SJ".equals(serviceType)) {
			return enterJudgeSJ(orderInfo, tacheType);
		} else {
			return serviceType;
		}
	}

	private String judgeServiceType(String tacheType) {
		if ("1".equals(tacheType) || "2".equals(tacheType) || "3".equals(tacheType) || "6".equals(tacheType)) {
			return "TS";
		} else if ("4".equals(tacheType) || "5".equals(tacheType)) {
			return "SJ";
		} else {
			return "数据状态错误";
		}
	}

	@SuppressWarnings("rawtypes")
	private String enterJudgeTS(OrderAskInfo orderInfo, String tacheType, String unifiedCode) {
		String orderId = orderInfo.getServOrderId();
		String strWhere = " AND service_order_id='" + orderId + "' AND sheet_type IN(720130017,700000129) ORDER BY creat_date DESC";
		List sheetInfos = this.sheetPubInfoDao.getSheetListCondition(strWhere, true);
		log.info("TsSheetInfos：{}", sheetInfos);
		if (sheetInfos.isEmpty()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
			return "工单状态不是回访中";
		}
		SheetPubInfo sheetInfo = (SheetPubInfo) sheetInfos.get(0);
		int sheetStatu = sheetInfo.getSheetStatu();
		if ("1".equals(tacheType)) {
			if (StaticData.WKST_ALLOT_STATE_NEW != sheetStatu) {
				sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
				return "预定性单状态错误";
			}
			return customerJudgeYDX(unifiedCode, orderInfo, sheetInfo);
		} else if ("2".equals(tacheType)) {
			if (StaticData.WKST_FINISH_STATE_NEW != sheetStatu) {
				sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
				return "终定性单状态错误";
			}
			return customerJudgeZDX(unifiedCode, orderInfo, sheetInfo);
		} else if ("6".equals(tacheType)) {
			log.info("orderId：{}，sheetStatu：{}", orderId, sheetStatu);
			if (StaticData.WKST_FINISH_STATE != sheetStatu) {
				sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
				return "审核单状态错误";
			}
			return customerJudgeCX(orderInfo, sheetInfo);
		} else {
			log.info("unifiedCode：{}，sheetStatu：{}", unifiedCode, sheetStatu);
			if (StaticData.WKST_FINISH_STATE != sheetStatu) {
				sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
				return "审核单状态错误";
			}
			return customerJudgeZX(unifiedCode, orderInfo, sheetInfo);
		}
	}

	@SuppressWarnings("rawtypes")
	private String enterJudgeSJ(OrderAskInfo orderInfo, String tacheType) {
		String orderId = orderInfo.getServOrderId();
		String strWhere = "";
		if ("4".equals(tacheType)) {
			strWhere = " AND service_order_id ='" + orderId + "'AND sheet_type=600000075 ORDER BY creat_date DESC";
		} else {
			strWhere = " AND service_order_id= '" + orderId + "'AND sheet_type=600000076 ORDER BY creat_date DESC";
		}
		List sheetInfos = this.sheetPubInfoDao.getSheetListCondition(strWhere, true);
		log.info("sjSheetInfos：{}", sheetInfos);
		if (sheetInfos.isEmpty()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
			return "工单状态不是回访中";
		}
		SheetPubInfo sheetInfo = (SheetPubInfo) sheetInfos.get(0);
		if (StaticData.WKST_FINISH_STATE != sheetInfo.getSheetStatu()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
			return "商机单状态错误";
		} else {
			return customerJudgeSJ(tacheType, orderInfo, sheetInfo);
		}
	}

	@SuppressWarnings("rawtypes")
	private String customerJudgeYDX(String unifiedCode, OrderAskInfo orderInfo, SheetPubInfo sheetInfo) {
		String orderId = orderInfo.getServOrderId();
		String preSheetId = sheetInfo.getSourceSheetId();
		int nextStep = 0; // 下一环节：0、自动回访，1、终定性，2、归档
		int autoVisitFlag = 0;
		int reportNum = 0;
		List crs = sheetPubInfoDao.selectCalloutRecByOrderId(orderId);
		if (crs.isEmpty()) { // 未邀评
			uninvitedJudge(orderInfo, sheetInfo.getSourceSheetId());
		} else {
			Map cr = (Map) crs.get(0);
			String satisfyDegree = cr.get(SATISFY_DEGREE).toString();
			String judgeDate = cr.get(JUDGEDATE) == null ? "" : cr.get(JUDGEDATE).toString();
			String tsVisitResult = "";
			switch (satisfyDegree) {
			case "1": // 满意
				nextStep = 2;
				autoVisitFlag = 1;
				reportNum = 5;
				tsVisitResult = SATISFY_DEGREE_1;
				break;
			case "2":// 一般
				nextStep = 2;
				autoVisitFlag = 1;
				reportNum = 3;
				tsVisitResult = SATISFY_DEGREE_2;
				break;
			case "3":// 服务态度冷淡
				nextStep = 1;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_3;
				break;
			case "4":// 业务解释听不懂
				nextStep = 1;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_4;
				break;
			case "5":// 处理速度慢
				nextStep = 1;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_5;
				break;
			case "6":// 处理方案未达到期望值
				nextStep = 1;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_6;
				break;
			case "7":// 问题未解决
				nextStep = 1;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_7;
				break;
			default:// 未评价
				tsVisitResult = SATISFY_DEGREE_0;
			}
			invitedJudgeTS(orderInfo, sheetInfo.getSourceSheetId(), reportNum, judgeDate, tsVisitResult);
			sheetPubInfoDao.updateCustomerJudgeStatusFromIVR(judgeDate, satisfyDegree, orderId);
		}
		if (nextStep == 0) { // 自动回访
			if (autoCustomerVisit.autoCustomerVisit(orderId, unifiedCode)) {
				labelManageDAO.updateAutoVisitFlag(2, orderId);
				sheetPubInfoDao.updateCustomerJudgeStatusToZDHF(orderId);
			} else {
				nextStep = 1;
				autoVisitFlag = 3;
				reportNum = 4;
				sheetPubInfoDao.updateCustomerJudgeStatusException(4, orderId);
			}
		}
		pubFunc.updateOrderAndSheetHang(preSheetId, orderId);
		sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 1, preSheetId);
		uploadToGroup(orderId);
		if (nextStep == 1) { // 终定性
			ydxTozdx(orderInfo, sheetInfo);
		}
		if (nextStep == 2) { // 归档
			ydxTofinish(orderInfo, sheetInfo, reportNum);
		}
		return SUCCESS_CODE;
	}

	private void uninvitedJudge(OrderAskInfo orderInfo, String sheetId) {
		String orderId = orderInfo.getServOrderId();
		int score = 0;
		TsSheetQualitative qualitative = tsSheetQualitative.getLatestQualitativeByOrderId(orderId, orderInfo.getRegionId());
		if (null != qualitative) {
			switch ((int) qualitative.getSatisfyId()) {
			case 600001166: // 满意
				score = 5;
				break;
			case 600001167:// 一般
				score = 3;
				break;
			case 600001168:// 不满意
				score = 1;
				break;
			case 600001169:// 很不满意
				score = 1;
				break;
			default:// 未评价
				score = 6;
				break;
			}
		} else {
			score = 5;
		}
		TScustomerVisit tcv = getTScustomerVisit(orderInfo, sheetId, score, "", "即时测评未邀评");
		tsCustomerVisit.saveCustomerVisit(tcv);
		labelManageDAO.updateDealResult(orderId, tcv.getTsDealResult(), tcv.getTsDealResultName());
	}

	private TScustomerVisit getTScustomerVisit(OrderAskInfo orderInfo, String workSheetId, int score, String replyData, String tsVisitResult) {
		TScustomerVisit customerVisit = new TScustomerVisit();
		customerVisit.setServiceOrderId(orderInfo.getServOrderId());
		customerVisit.setWorkSheetId(workSheetId);
		customerVisit.setMonth(orderInfo.getMonth());
		customerVisit.setRegionId(orderInfo.getRegionId());
		customerVisit.setRegionName(orderInfo.getRegionName());
		if ("".equals(replyData)) {
			customerVisit.setReplyData(pubFunc.getSysDate());
		} else {
			customerVisit.setReplyData(replyData);
		}
		int collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BPJ;// 总体情况
		int tsDealResult = StaticData.TS_DEAL_RESULT_BPJ;// 投诉处理结果
		int tsDealBetimes = StaticData.TS_DEAL_BETIMES_BPJ;// 投诉处理及时性
		int tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BPJ;// 投诉处理态度
		if (5 == score) { // 满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_MY;
			tsDealResult = StaticData.TS_DEAL_RESULT_MY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_MY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_MY;
		} else if (1 == score) { // 不满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BMY;
			tsDealResult = StaticData.TS_DEAL_RESULT_BMY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BMY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BMY;
		} else if (3 == score) { // 一般
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_YB;
			tsDealResult = StaticData.TS_DEAL_RESULT_YB;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_YB;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_YB;
		}
		customerVisit.setCollectivityCircs(collectivityCircs);
		customerVisit.setCollectivityCircsName(pubFunc.getStaticName(collectivityCircs));
		customerVisit.setTsDealAttitude(tsDealAttitude);
		customerVisit.setTsDealAttitudeName(pubFunc.getStaticName(tsDealAttitude));
		customerVisit.setTsDealBetimes(tsDealBetimes);
		customerVisit.setTsDealBetimesName(pubFunc.getStaticName(tsDealBetimes));
		customerVisit.setTsDealResult(tsDealResult);
		customerVisit.setTsDealResultName(pubFunc.getStaticName(tsDealResult));
		customerVisit.setTsVisitResult(tsVisitResult);
		customerVisit.setVisitType("3");
		return customerVisit;
	}

	private void ydxTozdx(OrderAskInfo orderInfo, SheetPubInfo sheetInfo) {
		String orderId = orderInfo.getServOrderId();
		String sheetId = sheetInfo.getWorkSheetId();
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_FINQUALITATIVE_STATU, orderInfo.getMonth(), pubFunc.getStaticName(StaticData.OR_FINQUALITATIVE_STATU));
		sheetPubInfoDao.updateSheetState(sheetId, StaticData.WKST_REPEAL_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_REPEAL_STATE_NEW), 14, 0);
		String result = workSheetAllot.allotToFinAssessAddForce(sheetInfo);
		if (SUCCESS_CODE.equals(result)) {
			sheetPubInfoDao.updateFetchSheetStaff(sheetId, sheetInfo.getRcvStaffId(), sheetInfo.getRcvStaffName(), sheetInfo.getRcvOrgId(), sheetInfo.getRcvOrgName());
			sheetPubInfoDao.updateSheetState(sheetId, sheetInfo.getSheetStatu(), sheetInfo.getSheetSatuDesc(), sheetInfo.getMonth(), 1);
		}
	}

	private void ydxTofinish(OrderAskInfo orderInfo, SheetPubInfo sheetInfo, int reportNum) {
		String orderId = orderInfo.getServOrderId();
		String sheetId = sheetInfo.getWorkSheetId();
		int dealSatisfy = StaticData.TS_DEAL_RESULT_MY;
		if (3 == reportNum) {
			dealSatisfy = StaticData.TS_DEAL_RESULT_YB;
		}
		TsSheetQualitative[] sqs = tsSheetQualitative.getTsSheetQualitative(sheetInfo.getSourceSheetId(), sheetInfo.getRegionId(), true);
		if (sqs.length >= 1) {
			TsSheetQualitative sq = sqs[sqs.length - 1];
			sq.setSheetId(sheetId);
			sq.setSatisfyId(dealSatisfy);
			sq.setSatisfyDesc(pubFunc.getStaticName(dealSatisfy));
			orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_FINQUALITATIVE_STATU, orderInfo.getMonth(), pubFunc.getStaticName(StaticData.OR_FINQUALITATIVE_STATU));
			if (sheetInfo.getLockFlag() == 0) {
				sheetPubInfoDao.updateSheetState(sheetId, StaticData.WKST_DEALING_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_DEALING_STATE_NEW), 14, 1);
			}
			FinAssessInfo finAssessInfo = new FinAssessInfo();
			finAssessInfo.setQualitative(sq);
			finAssessInfo.setCustomerVisit(null);
			finAssessInfo.setRegionId(sheetInfo.getRegionId());
			finAssessInfo.setMonth(sheetInfo.getMonth());
			finAssessInfo.setDealContent("系统自动回访工单完结");
			finAssessInfo.setLogonFlag(0);
			finAssessInfo.setValiFlag(3);
			finAssessInfo.setUpgradeIncline(2);
			finAssessInfo.setContactStatus("");
			finAssessInfo.setRequireUninvited("");
			finAssessInfo.setUnifiedCode("");
			finAssessInfo.setUccJTSS("");
			String result = complaintWorksheetDeal.submitFinAssess(finAssessInfo);
			if (SUCCESS_CODE.equals(result)) {
				ComplaintRelation cmpRelaOper = pubFunc.queryListByOid(orderId);
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
	}

	@SuppressWarnings("rawtypes")
	private String customerJudgeZDX(String unifiedCode, OrderAskInfo orderInfo, SheetPubInfo sheetInfo) {
		String orderId = orderInfo.getServOrderId();
		String sheetId = sheetInfo.getWorkSheetId();
		String isInvited = "Y";// Y、集团测评，21、用户要求不测评
		int autoVisitFlag = 0;
		int reportNum = 0;
		List crs = sheetPubInfoDao.selectCalloutRecByOrderId(orderId);
		if (crs.isEmpty()) {// 未邀评
			sheetPubInfoDao.updateCustomerJudgeStatusException(2, orderId);
		} else {
			Map cr = (Map) crs.get(0);
			String satisfyDegree = cr.get(SATISFY_DEGREE).toString();
			String judgeDate = cr.get(JUDGEDATE) == null ? "" : cr.get(JUDGEDATE).toString();
			String tsVisitResult = "";
			switch (satisfyDegree) {
			case "1":// 满意
				autoVisitFlag = 1;
				reportNum = 5;
				tsVisitResult = SATISFY_DEGREE_1;
				break;
			case "2":// 一般
				autoVisitFlag = 1;
				reportNum = 3;
				tsVisitResult = SATISFY_DEGREE_2;
				break;
			case "3":// 服务态度冷淡
				isInvited = "3";
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_3;
				break;
			case "4":// 业务解释听不懂
				isInvited = "4";
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_4;
				break;
			case "5":// 处理速度慢
				isInvited = "5";
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_5;
				break;
			case "6":// 处理方案未达到期望值
				isInvited = "6";
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_6;
				break;
			case "7":// 问题未解决
				isInvited = "7";
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_7;
				break;
			default:// 未评价
				tsVisitResult = SATISFY_DEGREE_0;
			}
			invitedJudgeTS(orderInfo, sheetInfo.getWorkSheetId(), reportNum, judgeDate, tsVisitResult);
			sheetPubInfoDao.updateCustomerJudgeStatusFromIVR(judgeDate, satisfyDegree, orderId);
		}
		SatisfyInfo si = new SatisfyInfo();
		si.setServiceOrderId(orderId);
		si.setWorkSheetId(sheetId);
		si.setUnifiedComplaintCode(unifiedCode);
		si.setContactStatus(0);
		si.setRequireUninvited(0);
		si.setIsRepeat(0);
		si.setIsUpRepeat(0);
		TsmStaff staff = pubFunc.getStaff(sheetInfo.getDealStaffId());
		si.setDealStaff(Integer.parseInt(staff.getId()));
		si.setDealLogonname(staff.getLogonName());
		si.setDealStaffName(staff.getName());
		si.setDealOrgId(staff.getOrganizationId());
		si.setDealOrgName(staff.getOrgName());
		si.setIsInvited(isInvited);
		evaluation.insertSatisfyInfoJSCP(si, orderInfo.getRelaInfo(), orderInfo.getProdNum());
		sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 2, sheetId);
		ComplaintUnifiedReturn cur = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(orderId);
		if (null != cur) {
			compWorksheetFullWebService.insertSupplement(orderId);
		}
		if (!"Y".equals(isInvited)) {// 挂机满意度为3-7时增加即时测评拦截记录，集团回访送“用户要求不测评”，工单自动归档
			complaintWorksheetDeal.submitFinAssessFromVisit(sheetId, orderId, sheetInfo.getRegionId(), sheetInfo.getMonth());
		}
		return SUCCESS_CODE;
	}

	@SuppressWarnings("rawtypes")
	private String customerJudgeCX(OrderAskInfo orderInfo, SheetPubInfo sheetInfo) {
		String orderId = orderInfo.getServOrderId();
		String sheetId = sheetInfo.getWorkSheetId();
		int autoVisitFlag = 0;
		int reportNum = 0;
		String satEval = "1";// SAT_EVAL客户满意测评：0未回访、1用户未参评、2满意、3不满意
		List crs = sheetPubInfoDao.selectCalloutRecByOrderId(orderId);
		log.info("selectCalloutRecByOrderId：{}", crs);
		if (crs.isEmpty()) { // 未邀评
			satEval = "0";
		} else {
			Map cr = (Map) crs.get(0);
			String satisfyDegree = cr.get(SATISFY_DEGREE).toString();
			String judgeDate = cr.get(JUDGEDATE) == null ? "" : cr.get(JUDGEDATE).toString();
			String tsVisitResult = "";
			switch (satisfyDegree) {
			case "1": // 满意
				autoVisitFlag = 1;
				reportNum = 5;
				satEval = "2";
				tsVisitResult = SATISFY_DEGREE_1;
				break;
			case "2":// 一般
				autoVisitFlag = 1;
				reportNum = 3;
				satEval = "2";
				tsVisitResult = SATISFY_DEGREE_2;
				break;
			case "3":// 服务态度冷淡
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_3;
				break;
			case "4":// 业务解释听不懂
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_4;
				break;
			case "5":// 处理速度慢
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_5;
				break;
			case "6":// 处理方案未达到期望值
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_6;
				break;
			case "7":// 问题未解决
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_7;
				break;
			default:// 未评价
				tsVisitResult = SATISFY_DEGREE_0;
			}
			log.info("tsVisitResult:{}", tsVisitResult);
			invitedJudgeTS(orderInfo, sheetInfo.getWorkSheetId(), reportNum, judgeDate, tsVisitResult);
			sheetPubInfoDao.updateCustomerJudgeStatusFromIVR(judgeDate, satisfyDegree, orderId);
		}
		log.info("autoVisitFlag:{} reportNum:{}", autoVisitFlag, reportNum);
		pubFunc.updateOrderHang(sheetId, orderId);
		sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 3, sheetId);
		uploadToGroup(orderId);
		setDapdSatEval(orderId, satEval, orderInfo.getServType());
		itsWorkSheetDeal.audSheetFinishVisit(sheetId, orderId, sheetInfo.getRegionId(), sheetInfo.getMonth());
		return SUCCESS_CODE;
	}

	@SuppressWarnings("rawtypes")
	private String customerJudgeZX(String unifiedCode, OrderAskInfo orderInfo, SheetPubInfo sheetInfo) {
		String orderId = orderInfo.getServOrderId();
		String sheetId = sheetInfo.getWorkSheetId();
		int nextStep = 0; // 下一环节：0、自动回访后归档，2、直接归档
		int autoVisitFlag = 0;
		int reportNum = 0;
		String satEval = "1";// SAT_EVAL客户满意测评：0未回访、1用户未参评、2满意、3不满意
		List crs = sheetPubInfoDao.selectCalloutRecByOrderId(orderId);
		log.info("selectCalloutRecByOrderId：{}", crs);
		if (crs.isEmpty()) { // 未邀评
			satEval = "0";
		} else {
			Map cr = (Map) crs.get(0);
			String satisfyDegree = cr.get(SATISFY_DEGREE).toString();
			String judgeDate = cr.get(JUDGEDATE) == null ? "" : cr.get(JUDGEDATE).toString();
			String tsVisitResult = "";
			switch (satisfyDegree) {
			case "1": // 满意
				nextStep = 2;
				autoVisitFlag = 1;
				reportNum = 5;
				satEval = "2";
				tsVisitResult = SATISFY_DEGREE_1;
				break;
			case "2":// 一般
				nextStep = 2;
				autoVisitFlag = 1;
				reportNum = 3;
				satEval = "2";
				tsVisitResult = SATISFY_DEGREE_2;
				break;
			case "3":// 服务态度冷淡
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_3;
				break;
			case "4":// 业务解释听不懂
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_4;
				break;
			case "5":// 处理速度慢
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_5;
				break;
			case "6":// 处理方案未达到期望值
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_6;
				break;
			case "7":// 问题未解决
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				satEval = "3";
				tsVisitResult = SATISFY_DEGREE_7;
				break;
			default:// 未评价
				tsVisitResult = SATISFY_DEGREE_0;
			}
			log.info("tsVisitResult:{}", tsVisitResult);
			invitedJudgeTS(orderInfo, sheetInfo.getWorkSheetId(), reportNum, judgeDate, tsVisitResult);
			sheetPubInfoDao.updateCustomerJudgeStatusFromIVR(judgeDate, satisfyDegree, orderId);
		}
		log.info("nextStep:{} autoVisitFlag:{} reportNum:{}", nextStep, autoVisitFlag, reportNum);
		if (nextStep == 0) { // 自动回访
			if (autoCustomerVisit.autoCustomerVisit(orderId, unifiedCode)) {
				labelManageDAO.updateAutoVisitFlag(2, orderId);
				sheetPubInfoDao.updateCustomerJudgeStatusToZDHF(orderId);
			} else {
				autoVisitFlag = 3;
				reportNum = 4;
				sheetPubInfoDao.updateCustomerJudgeStatusException(4, orderId);
			}
		}
		pubFunc.updateOrderHang(sheetId, orderId);
		sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 3, sheetId);
		uploadToGroup(orderId);
		setDapdSatEval(orderId, satEval, orderInfo.getServType());
		itsWorkSheetDeal.audSheetFinishVisit(sheetId, orderId, sheetInfo.getRegionId(), sheetInfo.getMonth());
		return SUCCESS_CODE;
	}

	private void setDapdSatEval(String orderId, String satEval, int servType) {
		if (720200003 == servType && (!"0".equals(satEval))) {
			dapdSheetService.setDapdSatEval(orderId, satEval);
		}
	}

	@SuppressWarnings("rawtypes")
	private String customerJudgeSJ(String tacheType, OrderAskInfo orderInfo, SheetPubInfo sheetInfo) {
		String orderId = orderInfo.getServOrderId();
		String sheetId = sheetInfo.getWorkSheetId();
		int nextStep = 0; // 下一环节：0、自动回访后归档，2、直接归档
		int autoVisitFlag = 0;
		int reportNum = 0;
		List crs = sheetPubInfoDao.selectCalloutRecByOrderId(orderId);
		log.info("selectCalloutRecByOrderId：{}", crs);
		if (crs.isEmpty()) { // 未邀评
		} else {
			Map cr = (Map) crs.get(0);
			String satisfyDegree = cr.get(SATISFY_DEGREE).toString();
			String judgeDate = cr.get(JUDGEDATE) == null ? "" : cr.get(JUDGEDATE).toString();
			String tsVisitResult = "";
			switch (satisfyDegree) {
			case "1": // 满意
				nextStep = 2;
				autoVisitFlag = 1;
				reportNum = 5;
				tsVisitResult = SATISFY_DEGREE_1;
				break;
			case "2":// 一般
				nextStep = 2;
				autoVisitFlag = 1;
				reportNum = 3;
				tsVisitResult = SATISFY_DEGREE_2;
				break;
			case "3":// 服务态度冷淡
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_3;
				break;
			case "4":// 业务解释听不懂
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_4;
				break;
			case "5":// 处理速度慢
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_5;
				break;
			case "6":// 处理方案未达到期望值
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_6;
				break;
			case "7":// 问题未解决
				nextStep = 2;
				autoVisitFlag = 2;
				reportNum = 1;
				tsVisitResult = SATISFY_DEGREE_7;
				break;
			default:// 未评价
				tsVisitResult = SATISFY_DEGREE_0;
			}
			log.info("tsVisitResult:{}", tsVisitResult);
			invitedJudgeSJ(orderInfo, sheetInfo.getWorkSheetId(), reportNum, judgeDate, tsVisitResult);
			sheetPubInfoDao.updateCustomerJudgeStatusFromIVR(judgeDate, satisfyDegree, orderId);
		}
		String sjAutoVisitFlag = pubFunc.querySysContolSwitchNew("sjAutoVisitFlag");// 商机单送福富自动回访1开0关
		log.info("nextStep:{} autoVisitFlag:{} reportNum:{} sjAutoVisitFlag:{}", nextStep, autoVisitFlag, reportNum, sjAutoVisitFlag);
		if (nextStep == 0 && "1".equals(sjAutoVisitFlag)) {// 自动回访
			if (autoCustomerVisit.autoCustomerVisit(orderId, "")) {
				sheetPubInfoDao.updateCustomerJudgeStatusToZDHF(orderId);
			} else {
				autoVisitFlag = 3;
				reportNum = 4;
				sheetPubInfoDao.updateCustomerJudgeStatusException(4, orderId);
			}
		}
		pubFunc.updateOrderHang(sheetId, orderId);
		sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 3, sheetId);
		workSheetBusi.auitSheetFinishFromVisit(tacheType, sheetId, orderId, sheetInfo.getRegionId(), sheetInfo.getMonth());
		return SUCCESS_CODE;
	}

	private void invitedJudgeTS(OrderAskInfo orderInfo, String sheetId, int score, String replyData, String tsVisitResult) {
		TScustomerVisit tcv = getTScustomerVisit(orderInfo, sheetId, score, replyData, tsVisitResult);
		tsCustomerVisit.saveCustomerVisit(tcv);
		labelManageDAO.updateDealResult(orderInfo.getServOrderId(), tcv.getTsDealResult(), tcv.getTsDealResultName());
	}

	private void invitedJudgeSJ(OrderAskInfo orderInfo, String sheetId, int score, String replyData, String tsVisitResult) {
		TScustomerVisit tcv = getTScustomerVisit(orderInfo, sheetId, score, replyData, tsVisitResult);
		tsCustomerVisit.saveCustomerVisit(tcv);
	}

	private void uploadToGroup(String orderId) {
		String finishDate = labelManageDAO.queryFinishDate(orderId);
		if (null == finishDate || finishDate.length() == 0) {
			labelManageDAO.saveFinishDate(orderId);
			dapdSheetService.setDapdEndDate(orderId);
			labelManageDAO.updateOverTimeLabel(orderId);
			ComplaintUnifiedReturn cur = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(orderId);
			if (null != cur) {
				compWorksheetFullWebService.insertSupplement(orderId);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public void unlockAutoVisitJob() {
		List list = sheetPubInfoDao.selectCustomerJudgeOvertimeList();
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				unlockAutoVisit(map.get("SERVICE_ORDER_ID").toString());
			}
		}
	}

	@SuppressWarnings("rawtypes")
	private void unlockAutoVisit(String orderId) {
		Map cj = sheetPubInfoDao.selectCustomerJudgeByOrderId(orderId);
		if (cj.isEmpty()) {
			return;
		}
		if (!"2".equals(cj.get("JUDGE_STATUS").toString())) {
			return;
		}
		String tacheType = cj.get("TACHE_TYPE").toString();
		if ("1".equals(tacheType)) {
			unlockAutoVisitYDX(orderId);
		} else if ("2".equals(tacheType)) {
			unlockAutoVisitZDX(orderId);
		} else {
			unlockAutoVisitSHOrSJ(orderId);
		}
	}

	@SuppressWarnings("rawtypes")
	private void unlockAutoVisitYDX(String orderId) {
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		if (orderInfo == null) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(7, orderId);
			return;
		}
		if (StaticData.OR_AUTOVISIT_STATU != orderInfo.getOrderStatu()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(7, orderId);
			return;
		}
		List sheetInfos = this.sheetPubInfoDao.getSheetListCondition(" AND service_order_id ='" + orderId + "'AND sheet_type=720130017 ORDER BY creat_date DESC", true);
		if (sheetInfos.isEmpty()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
			return;
		}
		SheetPubInfo sheetInfo = (SheetPubInfo) sheetInfos.get(0);
		if (StaticData.WKST_ALLOT_STATE_NEW != sheetInfo.getSheetStatu()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
			return;
		}
		String preSheetId = sheetInfo.getSourceSheetId();
		pubFunc.updateOrderAndSheetHang(preSheetId, orderId);
		sheetPubInfoDao.updateAutoVisit(3, 2, 1, preSheetId);
		sheetPubInfoDao.updateCustomerJudgeStatusException(5, orderId);
		ydxTozdx(orderInfo, sheetInfo);
	}

	@SuppressWarnings("rawtypes")
	private void unlockAutoVisitZDX(String orderId) {
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		if (orderInfo == null) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(7, orderId);
			return;
		}
		if (StaticData.OR_AUTOVISIT_STATU != orderInfo.getOrderStatu()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(7, orderId);
			return;
		}
		List sheetInfos = this.sheetPubInfoDao.getSheetListCondition(" AND service_order_id= '" + orderId + "'AND sheet_type=720130017 ORDER BY creat_date DESC", true);
		if (sheetInfos.isEmpty()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
			return;
		}
		SheetPubInfo sheetInfo = (SheetPubInfo) sheetInfos.get(0);
		if (StaticData.WKST_FINISH_STATE_NEW != sheetInfo.getSheetStatu()) {
			sheetPubInfoDao.updateCustomerJudgeStatusException(8, orderId);
			return;
		}
		String sheetId = sheetInfo.getWorkSheetId();
		pubFunc.updateOrderHang(sheetId, orderId);
		sheetPubInfoDao.updateAutoVisit(3, 2, 2, sheetId);
		sheetPubInfoDao.updateCustomerJudgeStatusException(5, orderId);
		complaintWorksheetDeal.submitFinAssessFromVisit(sheetId, orderId, sheetInfo.getRegionId(), sheetInfo.getMonth());
	}

	private void unlockAutoVisitSHOrSJ(String orderId) {
		if (orderId.startsWith("CX")) {
			dapdSheetService.setDapdSatEval(orderId, "1");
		}
		sheetPubInfoDao.updateCustomerJudgeStatusException(5, orderId);
		sheetPubInfoDao.insertCustomerJudgeHisByOrderId(orderId);
	}
}