package com.timesontransfar.complaintservice.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.google.gson.Gson;
import com.timesontransfar.complaintservice.service.IComplaint;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.ComplaintInfo;
import com.timesontransfar.customservice.orderask.pojo.ComplaintInfoError;
import com.timesontransfar.customservice.orderask.pojo.ComplaintStatusInfo;
import com.timesontransfar.customservice.orderask.pojo.CustEval;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedReturnDAOImpl;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedWeixinDAOImpl;
import com.timesontransfar.customservice.worksheet.service.IhastenSheet;

@Component(value = "ComplaintImpl__FACADE__")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ComplaintImpl implements IComplaint {
	private static final Logger logger = LoggerFactory.getLogger(ComplaintImpl.class);
	
	@Autowired
	private CmpUnifiedReturnDAOImpl cmpUnifiedReturnDAOImpl;
	@Autowired
	private CmpUnifiedWeixinDAOImpl clqUnifiedWeixinDAOImpl;
	@Autowired
	private IhastenSheet hastenSheet;
	@Autowired
	private IorderAskInfoDao orderAskInfo;

	private static final String ACCNBR = "ACCNBR";
	private static final String COMPLAINTDATE = "COMPLAINTDATE";
	private static final String COMPLAINTTYPE = "COMPLAINTTYPE";

	public List<ComplaintInfo> complaintGetInfo(String xCtgRequestId, String accNbr, String custNumber,
			String complaintOrderId, String contractPhoneNbr, String beginDate, String endDate, String regionCode,
			String sysSource) {
		List<ComplaintInfo> cis = new ArrayList<ComplaintInfo>();
		boolean sysFlag = true; // 非集团
		if ("0".equals(sysSource)) {
			sysFlag = false; // 集团
		}
		String phoneNumber = "";
		StringBuffer inInfo = new StringBuffer();
		String methodDetail = "";
		StringBuffer where = new StringBuffer();
		inInfo.append(beginDate + ",");
		inInfo.append(endDate + ",");
		inInfo.append(regionCode);
		if (!"".equals(complaintOrderId)) {
			complaintOrderId = complaintOrderId.replace("-", "").replaceAll("(.{4})", "$1-");
			phoneNumber = complaintOrderId;
			methodDetail = "complaintOrderId";
			where.append(" AND a.unified_complaint_code = '" + complaintOrderId + "'");
		} else {
			where.append(" AND b.accept_date >= str_to_date('" + beginDate + "', '%Y-%m-%d')");
			where.append(" AND b.accept_date < date_add(str_to_date('" + endDate + "', '%Y-%m-%d'),interval 1 day)");
			if (!"2".equals(regionCode)) {
				where.append(" AND b.region_id = " + regionCode);
			}
			if (!"".equals(contractPhoneNbr)) {
				phoneNumber = contractPhoneNbr;
				methodDetail = "contractPhoneNbr";
				where.append(" AND a.rela_info = '" + contractPhoneNbr + "'");
			} else if (!"".equals(accNbr)) {
				phoneNumber = accNbr;
				methodDetail = "accNbr";
				where.append(" AND a.complaint_phone = '" + accNbr + "'");
			} else if (!"".equals(custNumber)) {
				phoneNumber = custNumber;
				methodDetail = "custNumber";
				where.append(" AND d.crm_cust_id = '" + custNumber + "'");
			}
		}
		List ciList = cmpUnifiedReturnDAOImpl.queryComplaintInfoByWhere(where.toString());
		if (ciList.isEmpty()) {
			clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintInfo", phoneNumber, inInfo.toString(), "2", "未查到数据",
					"", xCtgRequestId, sysSource, methodDetail, "", "");
			return cis;
		}
		for (int i = 0; i < ciList.size(); i++) {
			Map ciMap = (Map) ciList.get(i);
			ComplaintInfo ci = new ComplaintInfo();
			String complaintWorksheetId = ciMap.get("COMPLAINTORDERID").toString();
			ci.setComplaintOrderId(complaintWorksheetId.replace("-", ""));
			ci.setCustId(ciMap.get("CUSTID").toString());
			ci.setAccNbr(dataMasking(ciMap.get(ACCNBR).toString()));
			ci.setCustName(dataMasking(ciMap.get("CUSTNAME").toString()));
			ci.setContractPhoneNbr(dataMasking(ciMap.get("CONTRACTPHONENBR").toString()));
			ci.setComplaintDate(ciMap.get(COMPLAINTDATE).toString());
			ci.setComplaintType(ciMap.get(COMPLAINTTYPE).toString());
			ci.setAcceptType(ciMap.get("ACCEPTTYPE").toString());
			List<ComplaintStatusInfo> csis = new ArrayList();
			String tacheId = ciMap.get("TACHE_ID").toString();
			List csiList = clqUnifiedWeixinDAOImpl.selectUnifiedShowAll(complaintWorksheetId, tacheId, sysFlag);
			if (csiList.isEmpty()) {
				csiList = clqUnifiedWeixinDAOImpl.selectUnifiedProgSqlContentByTacheId(complaintWorksheetId, tacheId);
			}
			if (csiList.isEmpty()) {
				ComplaintStatusInfo csi = new ComplaintStatusInfo();
				if ("1004".equals(tacheId)) {
					csi.setComplaintStatus("1004");
					csi.setStatusDate(ciMap.get("FINISH_DATE").toString());
					csi.setComplaintStatusDetail("JS1040-工单归档###本次服务已完成,感谢您的关注与支持!");
				} else {
					csi.setComplaintStatus("1001");
					csi.setStatusDate(ciMap.get(COMPLAINTDATE).toString());
					csi.setComplaintStatusDetail(
							"JS1010-已下单###尊敬的客户,您反映的问题已受理,受理单号为:" + complaintWorksheetId.replace("-", "")
									+ ".您可发送工单查询至江苏电信微信公众号或点击http://wapjs.189.cn/tsgd 查询处理进度.");
				}
				csis.add(csi);
			} else {
				for (int j = 0; j < csiList.size(); j++) {
					Map csiMap = (Map) csiList.get(j);
					ComplaintStatusInfo csi = new ComplaintStatusInfo();
					csi.setComplaintStatus(csiMap.get("COMPLAINTSTATUS").toString());
					csi.setStatusDate(csiMap.get("STATUSDATE").toString());
					csi.setComplaintStatusDetail(csiMap.get("COMPLAINTSTATUSDETAIL").toString() + "###"
							+ fullWidthToHalfWidth(csiMap.get("COMPLAINTSTATUSDESC").toString()).replace("-", ""));
					csis.add(csi);
				}
			}
			ci.setComplaintStatusInfos(csis);
			ci.setComplaintMessage(ciMap.get("COMPLAINTMESSAGE").toString());
			ci.setSalesCode(ciMap.get("SALESCODE").toString());
			ci.setSalesThirdType(ciMap.get("SALESTHIRDTYPE").toString());
			ci.setChannelNbr(ciMap.get("CHANNELNBR").toString());
			CustEval ce = new CustEval();
			if (!"0".equals(ciMap.get("CUSTEVALPOINT").toString())) {
				ce.setCustEvalPoint(ciMap.get("CUSTEVALPOINT").toString());
				ce.setEvalchannelNbr(ciMap.get("EVALCHANNELNBR").toString());
			}
			ci.setCustEval(ce);
			if (sysFlag) {
				ci.setSysSource(sysSource);
				ci.setIsRemind(ciMap.get("ISREMIND").toString());
				ci.setIsCancel(ciMap.get("ISCANCEL").toString());
			}
			cis.add(ci);
		}
		clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintInfo", phoneNumber, inInfo.toString(), "1", "", "",
				xCtgRequestId, sysSource, methodDetail, "", "");
		return cis;
	}

	private String dataMasking(String value) {
/**
		int length = value.length();
		if (length == 2) {
			value = value.substring(0, 1) + "*";
		} else if (length == 3) {
			value = value.substring(0, 1) + "*" + value.substring(length - 1);
		} else if (length > 3 && length <= 5) {
			value = value.substring(0, 1) + "**" + value.substring(length - 2);
		} else if (length > 5 && length <= 7) {
			value = value.substring(0, 2) + "***" + value.substring(length - 2);
		} else if (length > 7) {
			value = value.substring(0, 3) + "*****" + value.substring(length - 3);
		}
*/
		return value;
	}

	private String fullWidthToHalfWidth(String fullWidth) {
		return fullWidth.replace("，", ",").replace("。", ".").replace("！", "!").replace("：", ":").replace("“", "").replace("”", "");
	}

	public ComplaintInfoError complaintRemind(String xCtgRequestId, String complaintOrderId, String sysSource) {
		ComplaintInfoError cie = new ComplaintInfoError();
		complaintOrderId = complaintOrderId.replace("-", "").replaceAll("(.{4})", "$1-");
		List curList = cmpUnifiedReturnDAOImpl
				.queryUnifiedReturnByWhere(" AND unified_complaint_code = '" + complaintOrderId + "'");
		if (curList.isEmpty()) {
			clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintRemind", complaintOrderId, "", "2", "无投诉单信息或已归档",
					"", xCtgRequestId, sysSource, "", "", "");
			cie.setCode("310002");
			cie.setReason("无投诉单信息或已归档");
			return cie;
		}
		List sumList = clqUnifiedWeixinDAOImpl.sumUnifiedWeixinByPhoneNumber(complaintOrderId);
		Map sumMap = (Map) sumList.get(0);
		if (!"0".equals(sumMap.get("CANCEL_NUM").toString())) {
			clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintRemind", complaintOrderId, "", "2", "已撤单无法再催单", "",
					xCtgRequestId, sysSource, "", "", "");
			cie.setCode("5002");
			cie.setReason("已撤单无法再催单");
			return cie;
		}
		if (!"0".equals(sumMap.get("RECENTLY_NUM").toString())) {
			clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintRemind", complaintOrderId, "", "2", "2小时内重复催单", "",
					xCtgRequestId, sysSource, "", "", "");
			cie.setCode("5002");
			cie.setReason("2小时内重复催单");
			return cie;
		}
		try {
			ComplaintUnifiedReturn cur = (ComplaintUnifiedReturn) curList.get(0);
			String orderId = cur.getComplaintWorksheetId();
			String relaInfo = cur.getRelaInfo();
			int remindNum = Integer.parseInt(sumMap.get("REMIND_NUM").toString());
			String whoWhere = "用户电子渠道";
			switch (sysSource) {
			case "2":
				whoWhere = "用户IVR自助";
				break;
			case "3":
				whoWhere = "客服代表门户";
				break;
			default:
				break;
			}
			hastenSheet.weiXinHastenSheet(orderId, complaintOrderId, relaInfo, remindNum, false, true, whoWhere);
		} catch (Exception e) {
			cie.setCode("9999");
			cie.setReason("催单错误");
			return cie;
		}
		clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintRemind", complaintOrderId, "", "1", "", "",
				xCtgRequestId, sysSource, "", "", "");
		cie.setCode("0");
		cie.setReason("催单成功");
		return cie;
	}

	public ComplaintInfoError complaintEval(String xCtgRequestId, String complaintOrderId, String custEvalPoint,
			String evalchannelNbr, String sysSource) {
		ComplaintInfoError cie = new ComplaintInfoError();
		complaintOrderId = complaintOrderId.replace("-", "").replaceAll("(.{4})", "$1-");
		List curList = cmpUnifiedReturnDAOImpl
				.queryUnifiedReturnByWhere(" AND unified_complaint_code = '" + complaintOrderId + "'");
		if (curList.isEmpty()) {
			List curHisList = cmpUnifiedReturnDAOImpl.queryUnifiedReturnHisByUnifiedCode(complaintOrderId);
			if (curHisList.isEmpty()) {
				clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintEval", complaintOrderId, "", "2", "无投诉单信息", "",
						xCtgRequestId, sysSource, "", custEvalPoint, evalchannelNbr);
				cie.setCode("310002");
				cie.setReason("无投诉单信息");
				return cie;
			}
		}
		List sumList = clqUnifiedWeixinDAOImpl.sumUnifiedWeixinByPhoneNumber(complaintOrderId);
		Map sumMap = (Map) sumList.get(0);
		if (!"0".equals(sumMap.get("EVAL_NUM").toString())) {
			clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintEval", complaintOrderId, "", "2", "已评价无法再评价", "",
					xCtgRequestId, sysSource, "", custEvalPoint, evalchannelNbr);
			cie.setCode("5002");
			cie.setReason("已评价无法再评价");
			return cie;
		}
		clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintEval", complaintOrderId, "", "1", "", "", xCtgRequestId,
				sysSource, "", custEvalPoint, evalchannelNbr);
		cie.setCode("0");
		cie.setReason("评价成功");
		return cie;
	}

	public ComplaintInfoError complaintCancel(String xCtgRequestId, String complaintOrderId, String sysSource) {
		ComplaintInfoError cie = new ComplaintInfoError();
		complaintOrderId = complaintOrderId.replace("-", "").replaceAll("(.{4})", "$1-");
		List curList = cmpUnifiedReturnDAOImpl
				.queryUnifiedReturnByWhere(" AND unified_complaint_code = '" + complaintOrderId + "'");
		if (curList.isEmpty()) {
			clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintCancel", complaintOrderId, "", "2", "无投诉单信息或已归档",
					"", xCtgRequestId, sysSource, "", "", "");
			cie.setCode("310002");
			cie.setReason("无投诉单信息或已归档");
			return cie;
		}
		List sumList = clqUnifiedWeixinDAOImpl.sumUnifiedWeixinByPhoneNumber(complaintOrderId);
		Map sumMap = (Map) sumList.get(0);
		if (!"0".equals(sumMap.get("CANCEL_NUM").toString())) {
			clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintCancel", complaintOrderId, "", "2", "已撤单无法再撤单", "",
					xCtgRequestId, sysSource, "", "", "");
			cie.setCode("5002");
			cie.setReason("已撤单无法再撤单");
			return cie;
		}
		ComplaintUnifiedReturn cur = (ComplaintUnifiedReturn) curList.get(0);
		String orderId = cur.getComplaintWorksheetId();
		try {
			OrderAskInfo oai = orderAskInfo.getOrderAskInfo(orderId, false);
			if (null == oai) {
				clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintCancel", complaintOrderId, "", "2", "已归档无法再撤单",
						"", xCtgRequestId, sysSource, "", "", "");
				cie.setCode("5002");
				cie.setReason("已归档无法再撤单");
				return cie;
			}
			if (oai.getOrderStatu() == 720130007) {
				clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintCancel", complaintOrderId, "", "2", "回访中无法撤单",
						"", xCtgRequestId, sysSource, "", "", "");
				cie.setCode("5002");
				cie.setReason("回访中无法撤单");
				return cie;
			}
			hastenSheet.weiXinCancelSheet(orderId);
		} catch (Exception e) {
			cie.setCode("9999");
			cie.setReason("撤单错误");
			return cie;
		}
		complaintPostInfo(9, orderId);
		clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("complaintCancel", complaintOrderId, "", "1", "", "",
				xCtgRequestId, sysSource, "", "", "");
		cie.setCode("0");
		cie.setReason("撤单成功");
		return cie;
	}

	public List<ComplaintInfo> travelCodeQry(String xCtgRequestId, String accNbr, String regionCode, String sysSource, String maxNum) {
		List<ComplaintInfo> cis = new ArrayList<>();
		StringBuilder inInfo = new StringBuilder();
		inInfo.append(accNbr + ",");
		inInfo.append(regionCode);
		List ciList = cmpUnifiedReturnDAOImpl.queryTravelCodeComplaintInfoByWhere(accNbr, regionCode, maxNum);
		if (ciList.isEmpty()) {
			clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("travelCodeQry", accNbr, inInfo.toString(), "2", "未查到数据", "", xCtgRequestId, sysSource, "", "", "");
			return Collections.emptyList();
		}
		for (int i = 0; i < ciList.size(); i++) {
			Map ciMap = (Map) ciList.get(i);
			ComplaintInfo ci = new ComplaintInfo();
			ci.setComplaintOrderId(ciMap.get("COMPLAINTORDERID").toString().replace("-", ""));
			ci.setAccNbr(dataMasking(ciMap.get(ACCNBR).toString()));
			ci.setComplaintDate(ciMap.get(COMPLAINTDATE).toString());
			ci.setComplaintType(ciMap.get(COMPLAINTTYPE).toString());
			ci.setServiceOrderId(ciMap.get("SERVICEORDERID").toString());
			cis.add(ci);
		}
		clqUnifiedWeixinDAOImpl.insertUnifiedWeixin("travelCodeQry", accNbr, inInfo.toString(), "1", "", "", xCtgRequestId, sysSource, "", "", "");
		return cis;
	}

	public String complaintPostInfo(int step, String orderId) {
		logger.info("投诉进度推送 order: {} step: {}", orderId, step);
		try {
			Map map = clqUnifiedWeixinDAOImpl.insertUnifiedShow(step, orderId);//获取推送内容
			logger.info("insertUnifiedShow map: {}", new Gson().toJson(map));
			if (map.isEmpty()) {
				return "EMPTY";
			}
			String localPush = map.get("LOCALPUSH").toString();
			String cliquePush = map.get("CLIQUEPUSH").toString();
			String complaintid = map.get("COMPLAINTID").toString();
			String pushmsg = map.get("PUSHMSG").toString();//APP推送消息
			String pushmsgNote = map.get("PUSHMSG_NOTE").toString();//短信推送消息
			String pushmsgWechat = map.get("PUSHMSG_WECHAT").toString();//微信推送消息
			if ("0".equals(localPush)) {
				logger.info("省内推送");
				if ("0".equals(map.get("PUSHFLAG").toString())) {
					String accnbr = map.get("ACCNBR").toString();
					String acpttime = map.get("ACPTTIME").toString();
					if ("1".equals(map.get("BANDFLAG").toString())) { // 是否绑定微信公众号
						clqUnifiedWeixinDAOImpl.complaintPush(accnbr, complaintid, acpttime, pushmsgWechat);
					} else {
						cmpUnifiedReturnDAOImpl.sendNoteCont(accnbr, pushmsgNote, orderId);
					}
				}
			}
			if ("0".equals(cliquePush)) {
				logger.info("集团推送");
				String where = " AND a.unified_complaint_code = '" + complaintid.replace("-", "").replaceAll("(.{4})", "$1-") + "'";
				List ciList = cmpUnifiedReturnDAOImpl.queryComplaintInfoByWhere(where);
				logger.info("ciList result: {} complaintid: {}", ciList.isEmpty(), complaintid);
				if (!ciList.isEmpty()) {
					Map ciMap = (Map) ciList.get(0);
					ComplaintInfo ci = new ComplaintInfo();
					ci.setComplaintOrderId(complaintid.replace("-", ""));
					ci.setCustId(ciMap.get("CUSTID").toString());
					ci.setAccNbr(ciMap.get("ACCNBR").toString());
					ci.setCustName(dataMasking(ciMap.get("CUSTNAME").toString()));
					ci.setContractPhoneNbr(ciMap.get("CONTRACTPHONENBR").toString());
					ci.setComplaintDate(ciMap.get("COMPLAINTDATE").toString());
					ci.setComplaintType(ciMap.get("COMPLAINTTYPE").toString());
					ci.setAcceptType(ciMap.get("ACCEPTTYPE").toString());
					List<ComplaintStatusInfo> csis = new ArrayList();
					ComplaintStatusInfo csi = new ComplaintStatusInfo();
					csi.setComplaintStatus(map.get("COMPLAINTSTATUS").toString());
					csi.setStatusDate(map.get("STATUSDATE").toString());
					csi.setComplaintStatusDetail(map.get("COMPLAINTSTATUSDETAIL").toString() + "###"
							+ fullWidthToHalfWidth(pushmsg).replace("-", ""));
					csis.add(csi);
					ci.setComplaintStatusInfos(csis);
					ci.setComplaintMessage(ciMap.get("COMPLAINTMESSAGE").toString());
					ci.setSalesCode(ciMap.get("SALESCODE").toString());
					ci.setSalesThirdType(ciMap.get("SALESTHIRDTYPE").toString());
					ci.setChannelNbr(ciMap.get("CHANNELNBR").toString());
					//设置CustEval
					this.setCustEval(ciMap, ci);
					clqUnifiedWeixinDAOImpl.complaintPostInfo(ci, map.get("XCTGLANID").toString());
				}
			}
		} catch (Exception e) {
			logger.error("complaintPostInfo 异常：{}", e.getMessage(), e);
			return "FAILURE";
		}
		return "SUCCESS";
	}
	
	private void setCustEval(Map ciMap, ComplaintInfo ci) {
		if (!"0".equals(ciMap.get("CUSTEVALPOINT").toString())) {
			CustEval ce = new CustEval();
			ce.setCustEvalPoint(ciMap.get("CUSTEVALPOINT").toString());
			ce.setEvalchannelNbr(ciMap.get("EVALCHANNELNBR").toString());
			ci.setCustEval(ce);
		} else {
			ci.setCustEval(null);
		}
	}

	public String businessStatusSend(String serviceOrderId, String newOrderId, String businessId, String businessStatus,
			String expectFinishDate) {
		int step = 0;
		if ("申请".equals(businessStatus)) {
			if ("".equals(expectFinishDate)) {
				step = 12; // 有预计完成时间
			} else {
				step = 11; // 无预计完成时间
			}
		} else if ("审核".equals(businessStatus)) {
			step = 13;
		} else if ("审核结束".equals(businessStatus)) {
			step = 14;
		} else if ("退费失败".equals(businessStatus)) {
			step = 15;
		}
		if (step == 0) {
			return "WRONGSTATUS";
		}
		Map map = clqUnifiedWeixinDAOImpl.insertBusinessStatusSend(step, serviceOrderId, newOrderId, businessId,
				businessStatus, expectFinishDate);
		if (map.isEmpty()) {
			return "WRONGORDERID";
		} else {
			if ("0".equals(map.get("PUSHFLAG").toString())) {
				cmpUnifiedReturnDAOImpl.sendNoteCont(map.get(ACCNBR).toString(), map.get("PUSHMSG").toString(), serviceOrderId);
			}
		}
		return "SUCCESS";
	}
}