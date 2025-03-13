package com.timesontransfar.pubWebservice;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.timesontransfar.complaintservice.pojo.FinAssessInfo;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
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
public class AutoCustomerVisitWebService {
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
	private static final String METHOD_NAME = "CONTACT_EVENT_RESULT";
	private static final String SUCCESS_CODE = "SUCCESS";
	private static final String ERROR_CODE = "ERROR";

	/**
	 * 自动回访结果反馈接口
	 * 
	 * @param orderId
	 * @param score
	 *            0超时；1不满意；3一般；5满意；6未评价
	 * @param infoXml
	 * @return
	 */
	public String contactEventResult(String orderId, String score, String infoXml) {
		log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, null, orderId + "|" + score + "|" + infoXml, null), "CONTACT_EVENT_RESULT: " + orderId);
		if (null == orderId || orderId.length() == 0) {
			return buildRspStr("2", "订单ID不能为空");
		}
		if (null == score || score.length() == 0) {
			return buildRspStr("3", "得分不能为空");
		}
		String returnInfo = "";
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		if (orderAskInfo != null) {
			if (StaticData.OR_AUTOVISIT_STATU != orderAskInfo.getOrderStatu()) {
				returnInfo = buildRspStr("5", "该订单:" + orderId + "已完成自动回访或已超时");
				log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
				return returnInfo;
			}
		} else {
			orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, true);
			if (orderAskInfo != null) {
				return dealResultHis(orderAskInfo, score, orderId);
			} else {
				returnInfo = buildRspStr("4", "没有找到订单ID:" + orderId + "的数据");
				log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
				return returnInfo;
			}
		}
		String str = " AND service_order_id = '" + orderId + "' AND tache_id = 720130025 ORDER BY creat_date DESC";
		List sheetList = this.sheetPubInfoDao.getSheetListCondition(str, true);
		if (null == sheetList || sheetList.isEmpty()) {
			returnInfo = buildRspStr("5", "该订单:" + orderId + "已完成自动回访或已超时");
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
			return returnInfo;
		}
		SheetPubInfo sheetPubInfo = (SheetPubInfo) sheetList.get(0);

		return this.dealResult(score, infoXml, orderAskInfo, sheetPubInfo);
	}

	private String dealResultHis(OrderAskInfo orderAskInfo, String score, String orderId) {
		String returnInfo = "";
		if (StaticData.OR_COMPLETE_STATU != orderAskInfo.getOrderStatu()) {
			returnInfo = buildRspStr("4", "没有找到" + orderAskInfo.getServTypeDesc() + "单ID:" + orderId + "的数据");
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
			return returnInfo;
		} else {
			Map cj = sheetPubInfoDao.selectCustomerJudgeByOrderId(orderId);
			if (cj.isEmpty()) {
				returnInfo = buildRspStr("5", orderAskInfo.getServTypeDesc() + "单:" + orderId + "评价记录不存在");
				log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
				return returnInfo;
			}
			String tacheType = cj.get("TACHE_TYPE").toString();
			if ("1".equals(tacheType) || "2".equals(tacheType)) {
				returnInfo = buildRspStr("5", "该投诉单:" + orderId + "已完成自动回访或已超时");
				log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
				return returnInfo;
			}
			String judgeStatus = cj.get("JUDGE_STATUS").toString();
			if (!"2".equals(judgeStatus)) {
				returnInfo = buildRspStr("5", "该" + orderAskInfo.getServTypeDesc() + "单:" + orderId + "已完成自动回访或已超时");
				log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
				return returnInfo;
			}
			sheetPubInfoDao.updateCustomerJudgeStatusFromZDHF(score, orderId);
			sheetPubInfoDao.insertCustomerJudgeHisByOrderId(orderId);
			setDapdSatEval(orderId, score, orderAskInfo.getServType());
			returnInfo = buildRspStr("0", "");
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, SUCCESS_CODE, null, returnInfo));
			return returnInfo;
		}
	}

	private void setDapdSatEval(String orderId, String score, int servType) {
		if (720200003 == servType) {
			// score0超时、3一般、5满意、6未评价，1不满意，
			// SAT_EVAL客户满意测评：0未回访、1用户未参评、2满意、3不满意
			String satEval = "2";
			if ("0".equals(score) || "6".equals(score)) {
				satEval = "1";
			} else if ("1".equals(score)) {
				satEval = "3";
			}
			dapdSheetService.setDapdSatEval(orderId, satEval);
		}
	}

	private String dealResult(String score, String infoXml, OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo) {
		String orderId = sheetPubInfo.getServiceOrderId();
		String returnInfo = "";
		Map cj = sheetPubInfoDao.selectCustomerJudgeByOrderId(orderId);
		if (cj.isEmpty()) {
			returnInfo = buildRspStr("5", orderAskInfo.getServTypeDesc() + "单:" + orderId + "评价记录不存在");
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
			return returnInfo;
		}
		// 进入自动回访环节：1、预定性结束，2、终定性结束
		int orderType = this.getOrderType(sheetPubInfo.getTacheId(), sheetPubInfo.getSheetStatu());

		if (0 == orderType) {
			returnInfo = buildRspStr("5", "该订单:" + orderId + "已完成自动回访或已超时");
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
			return returnInfo;
		}
		try {
			StringReader reader = new StringReader(infoXml);
			SAXReader saxReader = new SAXReader();
			saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			Document doc = saxReader.read(reader);
			Element itemsNode = doc.getRootElement().element("ITEMS");
			String callNum = this.getElementValue(doc, "/INFO/CALLNUM");// 回访次数
			String callTime = this.getElementValue(doc, "/INFO/CALLTIME");// 最后一次回访时间
			
			boolean isFinish = true; // score为3、5、6时，完结订单；否则进入终定性环节
			int autoVisitFlag = 1;
			int reportNum = Integer.parseInt(score);// 回访原因标识：【1】0超时、3一般、5满意、6未评价，【2】1不满意，
			int dealSatisfy = StaticData.TS_DEAL_RESULT_MY;// 投诉处理满意度 默认满意
			if ("1".equals(score)) { // 不满意
				isFinish = false;
				autoVisitFlag = 2;
				reportNum = 1;
			} else if ("3".equals(score)) { // 一般
				dealSatisfy = StaticData.TS_DEAL_RESULT_YB;
			}
			
			TScustomerVisit bean = new TScustomerVisit();
			bean.setServiceOrderId(orderId);
			this.setTScustomerVisit(bean, score, itemsNode, callNum);
			if ("".equals(callTime)) {
				bean.setReplyData(this.getSysDate());
			} else {
				bean.setReplyData(callTime);
			}
			bean.setMonth(orderAskInfo.getMonth());
			bean.setRegionId(sheetPubInfo.getRegionId());
			bean.setRegionName(sheetPubInfo.getRegionName());
			bean.setVisitType("2");
			sheetPubInfoDao.updateCustomerJudgeStatusFromZDHF(score, orderId);
			if (1 == orderType) {
				this.autoSubmitFinAssess(orderAskInfo, sheetPubInfo, isFinish, bean, autoVisitFlag, reportNum, dealSatisfy);
				if("".equals(bean.getWorkSheetId())) {//未查询到工单定性记录
					returnInfo = buildRspStr("6", "该订单:" + orderId + "已完成自动回访或已超时");
					log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
					return returnInfo;
				}
			} else if (2 == orderType) {
				this.finishFinAssess(sheetPubInfo, isFinish, bean, autoVisitFlag, reportNum);
			}
			returnInfo = buildRspStr("0", "");
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, SUCCESS_CODE, null, returnInfo));
			return returnInfo;
		} catch (Exception e) {
			returnInfo = buildRspStr("1", e.getMessage());
			log.info(LOGTYPE, new LogBean(METHOD_NAME, orderId, ERROR_CODE, null, returnInfo));
			return returnInfo;
		}
	}
	
	private void setTScustomerVisit(TScustomerVisit bean, String score, Element itemsNode, String callNum) {
		StringBuilder tsVisitResult = new StringBuilder();
		// 默认为：未评价
		int collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BPJ;// 总体情况
		int tsDealResult = StaticData.TS_DEAL_RESULT_BPJ;// 投诉处理结果
		int tsDealBetimes = StaticData.TS_DEAL_BETIMES_BPJ;// 投诉处理及时性
		int tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BPJ;// 投诉处理态度
		// 手机短信回访，score的取值为ITEMS中“总体评价”的值；外呼回访，只有score，没有ITEMS
		if ("5".equals(score)) { // 满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_MY;
			tsDealResult = StaticData.TS_DEAL_RESULT_MY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_MY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_MY;
		} else if ("1".equals(score)) { // 不满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BMY;
			tsDealResult = StaticData.TS_DEAL_RESULT_BMY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BMY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BMY;
		} else if ("3".equals(score)) { // 一般
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_YB;
			tsDealResult = StaticData.TS_DEAL_RESULT_YB;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_YB;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_YB;
		}
		
		Iterator itemItr = itemsNode.elementIterator("ITEM");
		while (itemItr.hasNext()) {
			Element item = (Element) itemItr.next();
			String name = item.elementTextTrim("NAME");
			String value = item.elementTextTrim("VALUE");
			if ("TSDEALRESULT".equals(name)) {
				tsDealResult = this.getTsDealResult(tsDealResult, value);
			} else if ("TSDEALBETIMES".equals(name)) {
				tsDealBetimes = this.getTsDealBetimes(tsDealBetimes, value);
			} else if ("TSDEALATTITUDE".equals(name)) {
				tsDealAttitude = this.getTsDealAttitude(tsDealAttitude, value);
			} else if ("TSVISITRESULT".equals(name)) {
				tsVisitResult = this.getTsVisitResult(tsVisitResult, value);
			} else {
				tsVisitResult.append(name + ":" + value + ",");
			}
		}
		tsVisitResult.append("回访次数：" + callNum);
		
		bean.setCollectivityCircs(collectivityCircs);
		bean.setCollectivityCircsName(pubFun.getStaticName(collectivityCircs));
		bean.setTsDealAttitude(tsDealAttitude);
		bean.setTsDealAttitudeName(pubFun.getStaticName(tsDealAttitude));
		bean.setTsDealBetimes(tsDealBetimes);
		bean.setTsDealBetimesName(pubFun.getStaticName(tsDealBetimes));
		bean.setTsDealResult(tsDealResult);
		bean.setTsDealResultName(pubFun.getStaticName(tsDealResult));
		bean.setTsVisitResult(tsVisitResult.toString());
	}
	
	private int getTsDealResult(int tsDealResult, String value) {
		if ("5".equals(value) || "4".equals(value)) { // 满意
			tsDealResult = StaticData.TS_DEAL_RESULT_MY;
		} else if ("1".equals(value)) { // 很不满意
			tsDealResult = StaticData.TS_DEAL_RESULT_HBMY;
		} else if ("3".equals(value)) { // 一般
			tsDealResult = StaticData.TS_DEAL_RESULT_YB;
		} else if ("2".equals(value)) { // 不满意
			tsDealResult = StaticData.TS_DEAL_RESULT_BMY;
		} else if ("0".equals(value)) { // 未评价
			tsDealResult = StaticData.TS_DEAL_RESULT_BPJ;
		}
		return tsDealResult;
	}
	
	private int getTsDealBetimes(int tsDealBetimes, String value) {
		if ("5".equals(value) || "4".equals(value)) { // 满意
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_MY;
		} else if ("1".equals(value)) { // 很不满意
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_HBMY;
		} else if ("3".equals(value)) { // 一般
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_YB;
		} else if ("2".equals(value)) { // 不满意
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BMY;
		} else if ("0".equals(value)) { // 未评价
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BPJ;
		}
		return tsDealBetimes;
	}
	
	private int getTsDealAttitude(int tsDealAttitude, String value) {
		if ("5".equals(value) || "4".equals(value)) { // 满意
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_MY;
		} else if ("1".equals(value)) { // 很不满意
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_HBMY;
		} else if ("3".equals(value)) { // 一般
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_YB;
		} else if ("2".equals(value)) { // 不满意
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BMY;
		} else if ("0".equals(value)) { // 未评价
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BPJ;
		}
		return tsDealAttitude;
	}
	
	private StringBuilder getTsVisitResult(StringBuilder tsVisitResult, String value) {
		if ("4".equals(value)) { // 1天
			tsVisitResult.append("服务完成时长:1天,");
		} else if ("3".equals(value)) { // 2天
			tsVisitResult.append("服务完成时长:2天,");
		} else if ("2".equals(value)) { // 3天
			tsVisitResult.append("服务完成时长:3天,");
		} else if ("1".equals(value)) { // 3天以上
			tsVisitResult.append("服务完成时长:3天以上,");
		}
		return tsVisitResult;
	}
	
	private int getOrderType(int tacheId, int sheetStatu) {
		int orderType = 0;// 进入自动回访环节：1、预定性结束，2、终定性结束
		if (720130025 == tacheId && 720130033 == sheetStatu) {
			orderType = 1;
		} else if (720130025 == tacheId && 720130036 == sheetStatu) {
			orderType = 2;
		}
		return orderType;
	}
	
	private void autoSubmitFinAssess(OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo, boolean isFinish, TScustomerVisit bean, int autoVisitFlag, int reportNum, int dealSatisfy) {
		String orderId = sheetPubInfo.getServiceOrderId();
		String finSheetId = sheetPubInfo.getWorkSheetId();
		String preSheetId = sheetPubInfo.getSourceSheetId();
		int regionId = sheetPubInfo.getRegionId();
		
		TsSheetQualitative[] sheetQuals = this.sheetQualitative.getTsSheetQualitative(preSheetId, regionId, true);
		TsSheetQualitative sheetQual = null;
		if (sheetQuals.length >= 1) {
			sheetQual = sheetQuals[sheetQuals.length - 1];
		
			sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 1, preSheetId);
			if (isFinish) {
				doFinish(orderAskInfo, sheetPubInfo, bean, sheetQual, dealSatisfy);
			} else {
				pubFun.updateOrderAndSheetHang(finSheetId, orderId);
				orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_FINQUALITATIVE_STATU, orderAskInfo.getMonth(),
						pubFun.getStaticName(StaticData.OR_FINQUALITATIVE_STATU));// 更新订单状态为终定性
				bean.setWorkSheetId(preSheetId);
		        tscustomerVisitDao.saveCustomerVisit(bean);
				String stateDesc = pubFun.getStaticName(StaticData.WKST_REPEAL_STATE_NEW);
				// 更新终定性工单状态为待处理 、 更新到岗时间、 更新lock_flag(0:部门工单池 1：我的任务 2：已完成)
				sheetPubInfoDao.updateSheetState(finSheetId, StaticData.WKST_REPEAL_STATE_NEW, stateDesc, 14, 0);
				// 自动派发终定性工单
				SheetPubInfo finSheet = sheetPubInfoDao.getSheetPubInfo(finSheetId, false);
				String result = workSheetAllot.allotToFinAssessAddForce(finSheet);
				if (SUCCESS_CODE.equals(result)) {
					sheetPubInfoDao.updateFetchSheetStaff(finSheetId, finSheet.getRcvStaffId(), finSheet.getRcvStaffName(), finSheet.getRcvOrgId(),
							finSheet.getRcvOrgName());
					sheetPubInfoDao.updateSheetState(finSheetId, finSheet.getSheetStatu(), finSheet.getSheetSatuDesc(), finSheet.getMonth(), 1);
				}
			}
		}
	}

	private void doFinish(OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo, TScustomerVisit bean, TsSheetQualitative sheetQual, int dealSatisfy) {
		String orderId = sheetPubInfo.getServiceOrderId();
		String finSheetId = sheetPubInfo.getWorkSheetId();
		String preSheetId = sheetPubInfo.getSourceSheetId();
		int month = sheetPubInfo.getMonth();
		int regionId = sheetPubInfo.getRegionId();
		pubFun.updateOrderAndSheetHang(finSheetId, orderId);
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_FINQUALITATIVE_STATU, orderAskInfo.getMonth(), pubFun.getStaticName(StaticData.OR_FINQUALITATIVE_STATU));// 更新订单状态为终定性
		// 投诉定性定责内容
		sheetQual.setSheetId(finSheetId);
		sheetQual.setSatisfyId(dealSatisfy);
		sheetQual.setSatisfyDesc(pubFun.getStaticName(dealSatisfy));
		bean.setWorkSheetId(preSheetId);
		if (sheetPubInfo.getLockFlag() == 0) {// 若终定性单在大库中，则将工单状态从待提取改为已提
			sheetPubInfoDao.updateSheetState(finSheetId, StaticData.WKST_DEALING_STATE_NEW, pubFun.getStaticName(StaticData.WKST_DEALING_STATE_NEW), 14, 1);
		}
		
		FinAssessInfo finAssessInfo = new FinAssessInfo();
		finAssessInfo.setQualitative(sheetQual);
		finAssessInfo.setCustomerVisit(bean);
		finAssessInfo.setRegionId(regionId);
		finAssessInfo.setMonth(month);
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

	private void finishFinAssess(SheetPubInfo sheetPubInfo, boolean isFinish, TScustomerVisit bean, int autoVisitFlag, int reportNum) {
		String orderId = sheetPubInfo.getServiceOrderId();
		String finSheetId = sheetPubInfo.getWorkSheetId();
		int month = sheetPubInfo.getMonth();
		int regionId = sheetPubInfo.getRegionId();
		bean.setWorkSheetId(finSheetId);
		tscustomerVisitDao.saveCustomerVisit(bean);
		pubFun.updateOrderHang(finSheetId, orderId);
		if (isFinish) {
			sheetPubInfoDao.updateAutoVisit(1, reportNum, 2, finSheetId);
			complaintWorksheetDeal.submitFinAssessFromVisit(finSheetId, orderId, regionId, month);
		} else {
			sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 2, finSheetId);
			complaintWorksheetDeal.toRGHFFromVisit(finSheetId, orderId, regionId, month);
		}
	}

	private String buildRspStr(String result, String message) {
		return "<?xml version=\"1.0\" encoding=\"GBK\"?><info><result>" + result + "</result><error><message>" + message + "</message></error></info>";
	}

	private String getSysDate() {
		return this.pubFun.getSysDate();
	}

	/**
	 * 返回xpath对应元素的值
	 * 
	 * @param doc
	 * @param xpath
	 * @return
	 */
	private String getElementValue(Document doc, String xpath) {
		Element e = (Element) doc.selectSingleNode(xpath);
		if (e != null)
			return e.getStringValue().trim();
		else
			return "";
	}
}