package com.timesontransfar.complaintservice.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cliqueWorkSheetWebService.pojo.ComplaintConnection;
import com.timesontransfar.async.AsyncTask;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.pubFunction.IPUBFunctionService;
import com.timesontransfar.complaintservice.service.ICustomerJudgeJobService;
import com.timesontransfar.controller.TsWorkSheetDealController;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.uploadFile.dao.IAccessorieDao;
import com.timesontransfar.customservice.common.uploadFile.pojo.FileRelatingInfo;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ItsCustomerVisit;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.SheetOperation;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.feign.clique.AccessCliqueServiceFeign;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

@Service
@SuppressWarnings("rawtypes")
public class CustomerJudgeJobServiceImpl implements ICustomerJudgeJobService {
	protected Logger log = LoggerFactory.getLogger(CustomerJudgeJobServiceImpl.class);
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private AsyncTask asyncTask;
	@Autowired
	private PubFunc pFunc;
	@Autowired
	private ItsSheetQualitative sheetQualitative;
	@Autowired
	private TsWorkSheetDealController workSheetDealController;
	@Autowired
	private ItsCustomerVisit customerVisit;
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private IPUBFunctionService pUBFunctionService;
	@Autowired
	private IAccessorieDao accessorieDao;
	@Autowired
	private AccessCliqueServiceFeign cliqueServiceFeign;
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;

	public String enterJudgeJob(String orderId) {
		if ("".equals(orderId)) {
			List list = sheetPubInfoDao.selectCustomerJudgeList();
			if (!list.isEmpty()) {
				for (int i = 0; i < list.size(); i++) {
					Map map = (Map) list.get(i);
					asyncTask.enterJudgeAsync(map.get("SERVICE_ORDER_ID").toString());
				}
			}
		} else {
			asyncTask.enterJudgeAsync(orderId);
		}
		asyncTask.unlockAutoVisit();
		return "SUCCESS";
	}

	public String retrieveEvaluation(String orderId, String code, String msg, String joinMode) {
		asyncTask.retrieveEvaluation(orderId, code, msg, joinMode);
		return ResultUtil.success();
	}

	// 扫描二级枢纽来单且受理渠道为部并案工单（38006个人库），比对是否有近30天已归档的同一部局编码部预处理工单；存在同一部局编码部预处理工单的话，带入处理内容（含附件）后自动办结，并自动回复集团
	public String cmpAutoFinishBBAJob(String logonname) {
		TsmStaff tsmStaff = pFunc.getLogonStaffByLoginName(logonname);
		if (null != tsmStaff) {
			String staffId = tsmStaff.getId();
			List<ComplaintConnection> ccs = sheetPubInfoDao.selectCmpBBAByStaffId(staffId);
			if (!ccs.isEmpty()) {
				for (ComplaintConnection cc : ccs) {
					cmpAutoFinishBBA(cc, tsmStaff);
				}
			}
		}
		return "SUCCESS";
	}

	private void cmpAutoFinishBBA(ComplaintConnection cc, TsmStaff tsmStaff) {
		String newOrderId = cc.getNewOrderId();
		String dealContent = getDealContent(cc.getOldOrderId());
		TsSheetQualitative tsq = sheetQualitative.getLatestQualitativeHisByOrderId(cc.getOldOrderId());
		String parm = buildBBAParm(cc, dealContent, tsq, tsmStaff);
		log.info("cmpAutoFinishBBA,buildBBAParm orderId: {}, parm: {}", newOrderId, parm);
		String submitMsg = workSheetDealController.submitPDAndFinAssess(parm);
		log.info("cmpAutoFinishBBA,submitPDAndFinAssess orderId: {}, submitMsg: {}", newOrderId, submitMsg);
		if ("SUCCESS".equals(submitMsg)) {
			String feedBackMsg = feedBackBBA(cc, dealContent, tsq, tsmStaff);
			log.info("cmpAutoFinishBBA,feedBackBBA orderId: {}, feedBackMsg: {}", newOrderId, feedBackMsg);
			if ("SUCCESS".equals(feedBackMsg)) {
				sheetPubInfoDao.insertComplaintConnection(cc);
				SheetOperation operation = new SheetOperation();
				operation.setServiceOrderId(newOrderId);
				operation.setWorkSheetId(cc.getWorkSheetId());
				operation.setDealStaff(tsmStaff.getLogonName());
				operation.setDealStaffId(Integer.parseInt(tsmStaff.getId()));
				operation.setDealStaffName(tsmStaff.getName());
				operation.setDealOrgId(tsmStaff.getOrganizationId());
				operation.setDealOrgName(tsmStaff.getOrgName());
				operation.setRemark("部并案自动办结");
				int rt = tsWorkSheetDeal.saveSheetOperation(operation);
				log.info("cmpAutoFinishBBA,saveSheetOperation orderId: {}, result: {}", newOrderId, rt > 0 ? "success" : "fail");
			}
		}
	}

	private String buildBBAParm(ComplaintConnection cc, String dealContent, TsSheetQualitative tsq, TsmStaff tsmStaff) {
		JSONObject parm = new JSONObject();
		parm.put("serviceOrderId", cc.getNewOrderId());
		parm.put("workSheetId", cc.getWorkSheetId());
		parm.put("sheetType", 720130011);
		parm.put("sheetBeanZDX", buildSheetBeanZDX(cc, dealContent));
		parm.put("dealIdZDX", "600001141");
		parm.put("dealDescZDX", "一般处理");
		parm.put("isFinalOptionZDX", 0);
		parm.put("dealContentSaveZDX", buildDealContentSaveZDX(cc));
		parm.put("dealContent", dealContent);
		parm.put("upgradeIncline", 0);
		parm.put("contactStatus", "0");
		parm.put("requireUninvited", "0");
		parm.put("unifiedCode", "");
		parm.put("uccJTSS", "");
		parm.put("valiFlag", "0");
		parm.put("tsSheetQualitative", buildTsSheetQualitative(cc, tsq));
		parm.put("tscustomerVisit", buildTscustomerVisit(cc));
		parm.put("receiptEvalZDX", buildReceiptEvalZDX(cc));
		parm.put("chooseFlag", "2");
		parm.put("complaintInfo", buildComplaintInfo(cc));
		parm.put("autoCompleteStaff", tsmStaff.getId());
		return parm.toString();
	}

	private JSONObject buildSheetBeanZDX(ComplaintConnection cc, String dealContent) {
		JSONObject parm = new JSONObject();
		parm.put("serviceOrderId", cc.getNewOrderId());
		parm.put("workSheetId", cc.getWorkSheetId());
		parm.put("rcvOrgId", "ASSIGN_TO_FINASSESS");
		parm.put("regionId", cc.getRegionId());
		parm.put("month", cc.getMonthFlag());
		parm.put("dealContent", dealContent);
		return parm;
	}

	private String getDealContent(String oldOrderId) {
		String dealContent = pFunc.getLastDealContentHis(oldOrderId);
		if ("".equals(dealContent)) {
			dealContent = "请查看前期部预处理工单";
		}
		return dealContent;
	}

	private List<ServiceContentSave> buildDealContentSaveZDX(ComplaintConnection cc) {
		List<ServiceContentSave> list = new ArrayList<>();
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "a4272eaf993ff602e1129e6848539995",
				"a4272eaf993ff602e1129e6848539995", "业务号码", "0", cc.getProdNum()));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "3ffa11428479b7dcd45ee26d0d9e6056",
				"3ffa11428479b7dcd45ee26d0d9e6056", "用户姓名", "0", cc.getCustName()));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "0830f9f9166015c4320a4888683ed351",
				"0830f9f9166015c4320a4888683ed351", "申诉问题", "0", "属实"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "b97801a95ada6f1651db42de3631a504",
				"b97801a95ada6f1651db42de3631a504", "申诉前是否有最严工单", "radio_002", "否"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "a4e961a2e67d1e85318de7fa0925ce7c",
				"a4e961a2e67d1e85318de7fa0925ce7c", "最严工单核查及处理情况", "0", "不涉及"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "d6ecf1867e42ad1f4c2613865d7d0c80",
				"d6ecf1867e42ad1f4c2613865d7d0c80", "是否与前期处理结果一致", "select_0081", "不涉及"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "12911d93d9c7e7e7586baf2172e47862",
				"12911d93d9c7e7e7586baf2172e47862", "是否企业有责", "0", "不涉及"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "e0e6ca43eb3368539e832b20eab56e5d",
				"e0e6ca43eb3368539e832b20eab56e5d", "有责依据", "0", "不涉及"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "7047346eee3da06766c803562f61e2c4",
				"7047346eee3da06766c803562f61e2c4", "有责部门", "0", "不涉及"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "a704a1e1c94ce4d7c9d23ab515ab7a34",
				"a704a1e1c94ce4d7c9d23ab515ab7a34", "无责依据", "0", "不涉及"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "CB7892676F835F76E05392EAE0845CA7",
				"CB7892676F835F76E05392EAE0845CA7", "回复时间", "0", new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date())));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "9407c3b41dd4157365773c2a633bd59c",
				"checkCtnt", "本单投诉处理方案及结果", "0", "请查看前期部预处理工单"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "CB7892676F845F76E05392EAE0845CA7",
				"CB7892676F845F76E05392EAE0845CA7", "联系情况", "select_0043", "告知用户核查情况及解决方案"));
		list.add(bscs("ac4353ab2ddceb6a19c7b6512dba5182", "CB7892676F855F76E05392EAE0845CA7",
				"isAprvl", "是否认可处理方案", "select_0049", "认可"));
		return list;
	}

	private ServiceContentSave bscs(String complaintsId, String elementId, String aliasName, String elementName, String answerId, String answerName) {
		ServiceContentSave scs = new ServiceContentSave();
		scs.setComplaintsId(complaintsId);
		scs.setElementId(elementId);
		scs.setAliasName(aliasName);
		scs.setElementName(elementName);
		scs.setAnswerId(answerId);
		scs.setAnswerName(answerName);
		return scs;
	}

	private JSONObject buildTsSheetQualitative(ComplaintConnection cc, TsSheetQualitative tsq) {
		int overtime = pFunc.judgeOrderOvertime(cc.getNewOrderId());
		if (null != tsq) {
			tsq.setOrderId(cc.getNewOrderId());
			tsq.setSheetId(cc.getWorkSheetId());
			tsq.setRegion(cc.getRegionId());
			tsq.setRegName(cc.getRegionName());
			tsq.setMonthFlag(cc.getMonthFlag());
			if (1 == overtime) {
				tsq.setOverTimeReasonId(6);
				tsq.setOverTimeReasonDesc("其他");
			} else {
				tsq.setOverTimeReasonId(0);
				tsq.setOverTimeReasonDesc("");
			}
			return JSONObject.fromObject(tsq);
		}
		JSONObject parm = new JSONObject();
		parm.put("orderId", cc.getNewOrderId());
		parm.put("sheetId", cc.getWorkSheetId());
		parm.put("region", cc.getRegionId());
		parm.put("regName", cc.getRegionName());
		parm.put("monthFlag", cc.getMonthFlag());
		parm.put("tsReasonId", "702020311");
		parm.put("tsReasonName", "业务规则-计费规则-有规则，用户不认可-历史欠费不认可");
		parm.put("tsifBeing", "700001817");
		parm.put("appendCases", "0");
		parm.put("casesId", "0");
		parm.put("casesName", "");
		parm.put("tsKeyWord", "702");
		parm.put("tsKeyWordDesc", "业务规则");
		parm.put("subKeyWord", "70202");
		parm.put("subKeyWordDesc", "计费规则");
		parm.put("threeCatalog", "7020203");
		parm.put("threeCatalogDesc", "有规则，用户不认可");
		parm.put("thourCatalog", "702020311");
		parm.put("thourCatalogDesc", "历史欠费不认可");
		parm.put("fiveCatalog", 0);
		parm.put("fiveCatalogDesc", "");
		parm.put("sixCatalog", 0);
		parm.put("sixCatalogDesc", "");
		parm.put("forceFlag", "70010106");
		parm.put("forceFlagDesc", "其他");
		parm.put("sysJudge", "");
		parm.put("outletsGuid", "");
		parm.put("outletsName", "");
		parm.put("outletsAddress", "");
		parm.put("outletsArCode", "");
		parm.put("channelTpName", "");
		parm.put("outletsStaff", "");
		parm.put("outletsStaffId", "");
		parm.put("custOrderNbr", "");
		parm.put("createChannelName", "");
		parm.put("createStaffCode", "");
		parm.put("createStaffName", "");
		parm.put("orderOperType", "");
		parm.put("orderCreateDate", "");
		parm.put("orderStatus", "");
		parm.put("deductionsId", "");
		parm.put("deductionsName", "");
		parm.put("channelCode", "00000000");
		parm.put("channelClassification", "政企直销 (含校园、商客)");
		parm.put("channelDetails", "自营");
		parm.put("channelName", "1");
		parm.put("channelLocation", cc.getRegionName());
		parm.put("agentName", "1");
		parm.put("marketName", "无合适选项");
		parm.put("marketId", "00000000");
		parm.put("marketGrade", "A类");
		parm.put("principalDistrict", "地市级");
		parm.put("principalLocation", cc.getRegionName());
		parm.put("principalCounty", cc.getAreaName());
		parm.put("adminPrincipal", "1");
		parm.put("channelCounty", cc.getAreaName());
		parm.put("channelPrincipal", "1");
		parm.put("controlAreaFir", "707907132");
		parm.put("controlAreaFirDesc", "企业无责");
		parm.put("controlAreaSec", "707907136");
		parm.put("controlAreaSecDesc", "客户原因");
		parm.put("satisfyId", "600001166");
		parm.put("satisfyDesc", "满意");
		parm.put("unsatisfyReason", "");
		parm.put("dutyOrg", "707907301");
		parm.put("dutyOrgName", "非江苏电信用户");
		parm.put("principalDept", "非江苏电信用户");
		parm.put("plusOne", "");
		parm.put("plusOneDesc", "");
		parm.put("plusTwo", "");
		parm.put("plusTwoDesc", "");
		if (1 == overtime) {
			parm.put("overTimeReasonId", "6");
			parm.put("overTimeReasonDesc", "其他");
		} else {
			parm.put("overTimeReasonId", "0");
			parm.put("overTimeReasonDesc", "");
		}
		return parm;
	}

	private JSONObject buildTscustomerVisit(ComplaintConnection cc) {
		TScustomerVisit tcv = customerVisit.getCustomerVisitByOrderId(cc.getOldOrderId(), false);
		if (null != tcv) {
			tcv.setServiceOrderId(cc.getNewOrderId());
			tcv.setWorkSheetId(cc.getWorkSheetId());
			tcv.setRegionId(cc.getRegionId());
			tcv.setRegionName(cc.getRegionName());
			tcv.setMonth(cc.getMonthFlag());
			return JSONObject.fromObject(tcv);
		}
		JSONObject parm = new JSONObject();
		parm.put("serviceOrderId", cc.getNewOrderId());
		parm.put("workSheetId", cc.getWorkSheetId());
		parm.put("regionId", cc.getRegionId());
		parm.put("regionName", cc.getRegionName());
		parm.put("month", cc.getMonthFlag());
		parm.put("collectivityCircs", "600001151");
		parm.put("collectivityCircsName", "满意");
		parm.put("tsDealAttitude", "600001156");
		parm.put("tsDealAttitudeName", "满意");
		parm.put("tsDealBetimes", "600001161");
		parm.put("tsDealBetimesName", "满意");
		parm.put("tsDealResult", "600001166");
		parm.put("tsDealResultName", "满意");
		parm.put("tsVisitResult", "满意");
		parm.put("visitType", "1");
		return parm;
	}

	private JSONObject buildReceiptEvalZDX(ComplaintConnection cc) {
		JSONObject parm = new JSONObject();
		parm.put("service_order_id", cc.getNewOrderId());
		parm.put("work_sheet_id", cc.getWorkSheetId());
		parm.put("tache_id", 720130021);
		parm.put("tache_desc", "后台派单");
		parm.put("opration_first_id", "700000001");
		parm.put("opration_first_desc", "满意");
		parm.put("opration_secend_id", "");
		parm.put("opration_secend_desc", "");
		return parm;
	}

	private JSONObject buildComplaintInfo(ComplaintConnection cc) {
		Map cmap = orderAskInfoDao.getComplaintInfo(cc.getOldOrderId(), true);
		JSONObject parm = new JSONObject();
		parm.put("orderId", cc.getNewOrderId());
		if (!cmap.isEmpty()) {
			parm.put("ipOperators", o2s(cmap.get("IP_OPERATORS")));
			parm.put("unreasonScene", o2s(cmap.get("UNREASON_SCENE")));
			parm.put("unreasonSceneDesc", o2s(cmap.get("UNREASON_SCENE_DESC")));
			parm.put("isAgentComplaint", o2s(cmap.get("IS_AGENT_COMPLAINT")));
			parm.put("remark", o2s(cmap.get("REMARK")));
			parm.put("valetCmplntMap", o2s(cmap.get("VALET_CMPLNT_MAP")));
			parm.put("spclClaimType", o2s(cmap.get("SPCL_CLAIM_TYPE")));
			parm.put("isSameClaimType", o2s(cmap.get("IS_SAME_CLAIM_TYPE")));
			return parm;
		}
		parm.put("ipOperators", "");
		parm.put("unreasonScene", "");
		parm.put("unreasonSceneDesc", "");
		parm.put("isAgentComplaint", "0");
		parm.put("remark", "");
		parm.put("valetCmplntMap", "");
		parm.put("spclClaimType", "");
		parm.put("isSameClaimType", "0");
		return parm;
	}

	private String o2s(Object obj) {
		return null == obj ? "" : obj.toString();
	}

	@SuppressWarnings("unchecked")
	private String feedBackBBA(ComplaintConnection cc, String dealContent, TsSheetQualitative tsq, TsmStaff tsmStaff) {
		String newOrderId = cc.getNewOrderId();
		Map poolForm = new HashMap();
		poolForm.put("orderId", newOrderId);
		poolForm.put("complaintworksheetid", "");
		poolForm.put("prodNum", "");
		poolForm.put("cliqueOrderStatu", "selected");
		poolForm.put("assigntype", "selected");
		poolForm.put("receivecode", "selected");
		poolForm.put("assigncode", "selected");
		poolForm.put("openStaffType", "0");
		poolForm.put("comeFrom", "");
		poolForm.put("isTimeFlag", "false");
		poolForm.put("acceptDate", "");
		poolForm.put("sponFlag", "false");
		poolForm.put("sponsor", "");
		poolForm.put("sponsorId", "");
		poolForm.put("sponsorDate", "");
		Map param = new HashMap();
		param.put("currentPage", 1);
		param.put("poolForm", poolForm);
		String sql = pUBFunctionService.getQryClieque(param);
		Map poolMap = orderAskInfoDao.qrCliqueList(sql);
		String count = poolMap.get("count").toString();
		if (!"0".equals(count)) {
			List list = (List) poolMap.get("list");
			Map map = (Map) list.get(0);
			JSONObject info = pUBFunctionService.addFeedbackInfo(newOrderId);
			info.put("complaintWorksheetId", cc.getNewComplaintId());
			info.put("serviceOrderId", newOrderId);
			info.put("applyProcessingObj", map.get("ASSIGN_CODE").toString());
			info.put("content", dealContent);// 操作原因:传最后一个部门处理结果
			info.put("comment", "无");// 备注内容:填无就行
			if (null == tsq) {
				info.put("complaintReason", 702020311);
			} else {
				info.put("complaintReason", pFunc.getLastYY(tsq));// 办结原因:传省内办结原因，四类工单改造后传最新的办结原因
			}
			info.put("auditResult", "Y");// 审核结果:传通过
			info.put("dealPerson", tsmStaff.getLogonName());// 处理人:后台派单人
			info.put("dealPhone", "025-83559711");// 处理人电话:025-83559711
			info.put("reason", "不成立");// 定责理由:传不成立
			FileRelatingInfo[] fris = accessorieDao.quryFileInfoNotInJT(cc.getOldOrderId());
			if (fris.length == 0) {
				info.put("ftpFileIds", null);
			} else {
				int len = fris.length;
				String[] ftpFileIds = new String[len];
				for (int i = 0; i < len; i++) {
					ftpFileIds[i] = fris[i].getFtpId();
				}
				info.put("ftpFileIds", ftpFileIds);// 本地上传:自动上传省内工单中附件
			}
			info.put("type", "FEEDBACK");
			cliqueServiceFeign.accessCliqueNew(info.toString());
			return "SUCCESS";
		}
		return "NONE";
	}
}