package com.timesontransfar.controller;

import java.util.List;
import java.util.Map;

import com.timesontransfar.pubWebservice.impl.JaxRpcpubwebServiceImpl;
import com.timesontransfar.pubWebservice.impl.ServiceOrderAccept;

import com.timesontransfar.pubWebservice.impl.ServiceRefundOrderAccept;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.cliqueWorkSheetWebService.util.ReasultUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.complaintservice.pojo.FinAssessInfo;
import com.timesontransfar.complaintservice.pojo.PreAssessInfo;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDealAll;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataFace;
import com.timesontransfar.customservice.labelmanage.service.ILabelManageService;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.timesontransfar.feign.clique.AccessCliqueServiceFeign;
import com.timesontransfar.labelLib.service.ILabelService;
import com.timesontransfar.pubWebservice.AutoCustomerVisitWebService;
import com.timesontransfar.pubWebservice.RecDealContentByZD;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.xml.sax.SAXException;

@RestController
@EnableAsync
@SuppressWarnings("rawtypes")
public class ComplaintWorksheetDealController {
	protected Logger log = LoggerFactory.getLogger(ComplaintWorksheetDealController.class);
	
	@Autowired
	private IComplaintWorksheetDeal complaintWorksheetDealImpl;
	@Autowired 
	private ILabelService labelServiceImpl;
	@Autowired
	private IComplaintWorksheetDealAll complaintWorksheetDealAll;
	@Autowired
	private ILabelManageService labelManageService;
	@Autowired
	private IdbgridDataFace dbgridDataFaceImpl;
	@Autowired
	private IworkSheetBusi workSheetBusi;
	@Autowired
	private AccessCliqueServiceFeign accessCliqueServiceFeign;
    @Autowired
	private IserviceContentSchem serviceContentSchem;
    @Autowired
	private AutoCustomerVisitWebService autoCustomerVisit;
	@Autowired
	private JaxRpcpubwebServiceImpl jaxRpcpubwebServiceImpl;
	@Autowired
	private RecDealContentByZD recDealContentByZD;
	@Autowired
    private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private ServiceOrderAccept acceptImpl;
	@Autowired
	private ServiceRefundOrderAccept refundAcceptImpl;
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/hangupForTZ")
	public int hangupForTZ(@RequestParam(value="workSheetId", required=true)String workSheetId,@RequestParam(value="endDate", required=true) String endDate) {
		return complaintWorksheetDealImpl.hangupForTZ(workSheetId, endDate);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/hiddenSheet")
	public int hiddenSheet(@RequestParam(value="workSheetId", required=true)String workSheetId,@RequestParam(value="endDate", required=true) String endDate) {
		return complaintWorksheetDealImpl.hiddenSheet(workSheetId, endDate);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/stopHide")
	public int stopHide(@RequestParam(value="workSheetId", required=true)String workSheetId) {
		return complaintWorksheetDealImpl.stopHide(workSheetId);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/queryLastActionCodeBySheetId")
	public int queryLastActionCodeBySheetId(@RequestParam(value="workSheetId", required=true)String workSheetId) {
		log.info("queryLastActionCodeBySheetId workSheetId: {}", workSheetId);
		int x = complaintWorksheetDealImpl.queryLastActionCodeBySheetId(workSheetId);
		if(x != 700001828) {
			log.info("非调账挂起工单");
			return 3;
		}
		boolean autoAuditFlag = pubFunc.isAutoAuditSheet(workSheetId);
		log.info("autoAuditFlag: {}", autoAuditFlag);
		if(autoAuditFlag) {
			log.info("待自动审核调账单，无法解挂");
			return 4;
		}
		boolean autoFinishFlag = pubFunc.isAutoFinishSheet(workSheetId);
		log.info("autoFinishFlag: {}", autoFinishFlag);
		if(autoFinishFlag) {
			log.info("待自动办结调账单，无法解挂");
			return 5;
		}
		int y = complaintWorksheetDealImpl.unHangupForTZ(workSheetId);
		if (1 == y) {
			log.info("非调账挂起工单解挂成功：{}",workSheetId);
			return 6;
		}
		return 9999;
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/unHangupForTZ")
	public int unHangupForTZ(@RequestParam(value="workSheetId", required=true)String workSheetId) {
		return complaintWorksheetDealImpl.unHangupForTZ(workSheetId);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/getHiddenListByOpraStaff")
	public GridDataInfo getHiddenListByOpraStaff(@RequestParam(value="where", required=true)String where) {
		return complaintWorksheetDealImpl.getHiddenSheetList(where);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/unHidden")
	public int unHidden(@RequestParam(value="rows", required=true)String rows) {
		return complaintWorksheetDealImpl.unHidden(rows);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/sumbitOrgBack")
	public String sumbitOrgBack(
			@RequestParam(value="worksheetId", required=true)String worksheetId, 
			@RequestParam(value="regionId", required=true)int regionId, 
			@RequestParam(value="month", required=true)int month, 
			@RequestParam(value="backReason", required=true)String backReason,
			@RequestParam(value="labelArr", required=true)String labelArr ) {
		String res = complaintWorksheetDealAll.sumbitOrgBackNew(worksheetId, regionId, month, backReason);
		
		JSONArray labelList = JSONArray.fromObject(labelArr);
		if(null != labelList) {
			LabelInstance[] labelArray = new LabelInstance[labelList.size()];
			for (int i = 0; i < labelList.size(); i++) {
				labelArray[i] = (LabelInstance) JSONObject.toBean(labelList.optJSONObject(i),LabelInstance.class);
			}
			labelServiceImpl.saveLabelInstance(labelArray);
		}
		return res;
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/checkLastDeal")
	public boolean checkLastDeal(
			@RequestParam(value="sheetId", required=true)String sheetId, 
			@RequestParam(value="sheetType", required=true)int sheetType, 
			@RequestParam(value="curMonth", required=true)int curMonth) {
		return complaintWorksheetDealImpl.checkLastDeal(sheetId, sheetType, curMonth);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/checkYDXCreate")
	public boolean checkYDXCreate(
			@RequestParam(value="curSheetId", required=true)String curSheetId, 
			@RequestParam(value="sheetType", required=true)int sheetType, 
			@RequestParam(value="rcvOrgId", required=true)String rcvOrgId, 
			@RequestParam(value="curMonth", required=true)int curMonth) {
		return complaintWorksheetDealImpl.checkYDXCreate(curSheetId, sheetType, rcvOrgId, curMonth);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/checkYDXAuthority")
	public int checkYDXAuthority(@RequestParam(value="servOrderId", required=true)String servOrderId) {
		return complaintWorksheetDealImpl.checkYDXAuthority(servOrderId);
	}
	/**
	 * Description: 投诉部门处理方法<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param models
	 * @param delalId
	 * @param dealName
	 * @return <br>
	 * @CreateDate 2020年6月22日 上午11:50:38 <br>
	 */
	@PostMapping(value = "/workflow/complaintWorksheetDeal/sumbitOrgDeal")
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String sumbitOrgDeal(@RequestBody String json) {
		log.info("投诉部门处理: {}", json);
		JSONObject models=JSONObject.fromObject(json);
		String yudingFlag=models.getString("yudingFlag");
		JSONObject sheetJson=models.optJSONObject("sheetPubInfo");
		JSONObject sheetQualitative=models.optJSONObject("sheetQualitative");
		SheetPubInfo sheetPubInfo = (SheetPubInfo)JSONObject.toBean(sheetJson,SheetPubInfo.class);
		int delalId = models.optInt("delalId");
		String dealName = models.optString("dealName");
		String batchFinish = getBatchFinish(models);
		String result = complaintWorksheetDealImpl.sumbitOrgDeal(sheetPubInfo, delalId, dealName, batchFinish);
		log.info("投诉部门处理 result: {}", result);
		if(StaticData.SUCCESS.equals(result)){
			JSONArray labelInstance = models.optJSONArray("labelInstance");
			if(null != labelInstance) {
				LabelInstance[] labelArray = new LabelInstance[labelInstance.size()];
				for (int i = 0; i < labelInstance.size(); i++) {
					labelArray[i] = (LabelInstance) JSONObject.toBean(labelInstance.optJSONObject(i),LabelInstance.class);
				}
				labelServiceImpl.saveLabelInstance(labelArray);
			}
			if(models.containsKey("dealContentSave")) {//投诉部门处理（非审批）：保存结案模板
				JSONArray saveArray = models.optJSONArray("dealContentSave");
				List<ServiceContentSave> saveList = new Gson().fromJson(saveArray.toString(),new TypeToken<List<ServiceContentSave>>() {}.getType());
				serviceContentSchem.saveDealContentSave(saveList, sheetPubInfo.getServiceOrderId());
			}
			
			log.info("orderId: {} 是否预定性: {}", sheetPubInfo.getServiceOrderId(), yudingFlag);
			//提交预定性
			if(Boolean.parseBoolean(yudingFlag)) {
				String where=" AND A.SERVICE_ORDER_ID='" + sheetJson.getString("serviceOrderId") + "' AND W.SHEET_TYPE = 720130016";
				GridDataInfo dataInfo=dbgridDataFaceImpl.getGridDataBySize(0, 0, where, "TSSHEET10003");
				log.info("预定性工单: {}", dataInfo.getQuryCount());
				if(1 != dataInfo.getQuryCount() || dataInfo.getList() == null) {
					result="YUDING_ERROR";
				}else {
					JSONObject yudingJson=models.optJSONObject("yudingInfo");
					JSONArray dataArr=JSONArray.fromObject(dataInfo);
					JSONObject dataJson=dataArr.getJSONObject(0).getJSONArray("list").getJSONObject(0);
					String ucc=yudingJson.getString("ucc");
					String uccJTSS=yudingJson.getString("uccJTSS");
					String sheetId=dataJson.getString("WORK_SHEET_ID");
					int regionId= Integer.parseInt(dataJson.getString("REGION_ID"));
					int monthFlag=Integer.parseInt(dataJson.getString("MONTH_FLAG"));
					String assessCircs=yudingJson.getString("assessCircs");
					String orderId=dataJson.getString("SERVICE_ORDER_ID"); 
					String qryReuslt=workSheetBusi.fetchWorkSheet(sheetId,regionId, monthFlag);
					int upIncline=Integer.parseInt(yudingJson.getString("upIncline"));
					if("ERROR".equals(qryReuslt) || "WKST_HOLD_STATE".equals(qryReuslt)) {
						result="YUDINGGET_ERROR";
					}else {
						//投诉申诉信息
						this.updateOtherComplaintInfo(models);
						
						String contactStatus=yudingJson.getString("contactStatus");
						String requireUninvited=yudingJson.getString("requireUninvited");
						complaintWorksheetDealImpl.setUnifiedContact(orderId, sheetId, contactStatus, "1");
						complaintWorksheetDealImpl.setUnifiedContact(orderId, sheetId, requireUninvited, "2");
						complaintWorksheetDealImpl.setUnifiedRepeat(orderId, sheetId, ucc, "1");
						complaintWorksheetDealImpl.setUnifiedRepeat(orderId, sheetId, uccJTSS, "2");
						TsSheetQualitative sheetQualitativeOb=(TsSheetQualitative)JSONObject.toBean(sheetQualitative,TsSheetQualitative.class);
						sheetQualitativeOb.setSheetId(sheetId);
						PreAssessInfo preInfo = new PreAssessInfo();
						preInfo.setBean(sheetQualitativeOb);
						preInfo.setRegionId(regionId);
						preInfo.setMonth(monthFlag);
						preInfo.setDealContent(assessCircs);
						preInfo.setUpgradeIncline(upIncline);
						preInfo.setContactStatus(contactStatus);
						preInfo.setRequireUninvited(requireUninvited);
						preInfo.setUnifiedCode(ucc);
						preInfo.setUccJTSS(uccJTSS);
						String flag = complaintWorksheetDealImpl.submitPreAssess(models, preInfo);
						if("SUCCESS".equals(flag)) {
							result="YDSUCCESS";
						}else {
							result=flag;
							log.error("部门处理提交预定性失败:{}", result);
						}
					}
				}
			}
		}else {
			log.error("部门处理提交失败:{}", result);
		}
		return ReasultUtil.toResult(result);
	}

	private String getBatchFinish(JSONObject models) {
		String batchFinish = "";
		if (models.containsKey("batchFinish")) {
			batchFinish = models.optString("batchFinish");
		}
		return batchFinish;
	}

	/**
	 * Description:新的预定性提交方法 <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param json
	 * @param regionId
	 * @param month
	 * @param dealContent
	 * @param upgradeIncline
	 * @param contactStatus
	 * @param unifiedCode
	 * @return <br>
	 * @CreateDate 2020年6月15日 下午2:46:59 <br>
	 */
	@PostMapping(value = "/workflow/complaintWorksheetDeal/submitPreAssessNew")
	public String submitPreAssessNew(@RequestBody String json) {
		log.info("投诉预定性: {}", json);
		return complaintWorksheetDealAll.submitPreAssessYdx(json);
	}

	/**
	 * 终定性办结
	 * @param models
	 * @return
	 */
	@PostMapping(value = "/workflow/complaintWorksheetDeal/submitFinAssessNew")
	public String submitFinAssessNew(@RequestBody String models) {
		return complaintWorksheetDealAll.autoSheetFinish(models);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/queryWorkSheetAreaBySheetId")
	public int queryWorkSheetAreaBySheetId(
			@RequestParam(value="workSheetId", required=true)String workSheetId, 
			@RequestParam(value="sheetType", required=true)int sheetType) {
		return complaintWorksheetDealImpl.queryWorkSheetAreaBySheetId(workSheetId, sheetType);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/submitFinAssess")
	public String submitFinAssess(@RequestBody String models) {
		JSONObject json = JSONObject.fromObject(models);
		String result = "";
		try {
			FinAssessInfo finAssessInfo = new FinAssessInfo();
			finAssessInfo.setQualitative((TsSheetQualitative)JSONObject.toBean(json.optJSONObject("tsSheetQualitative"),TsSheetQualitative.class));
			finAssessInfo.setCustomerVisit((TScustomerVisit)JSONObject.toBean(json.optJSONObject("tscustomerVisit"),TScustomerVisit.class));
			finAssessInfo.setRegionId(json.optInt("regionId"));
			finAssessInfo.setMonth(json.optInt("month"));
			finAssessInfo.setDealContent(json.optString("dealContent"));
			finAssessInfo.setLogonFlag(json.optInt("flag"));
			finAssessInfo.setValiFlag(json.optInt("valiFlag"));
			finAssessInfo.setUpgradeIncline(json.optInt("upgradeIncline"));
			finAssessInfo.setContactStatus("");
			finAssessInfo.setRequireUninvited("");
			finAssessInfo.setUnifiedCode("");
			finAssessInfo.setUccJTSS("");
			result = complaintWorksheetDealImpl.submitFinAssess(finAssessInfo);
		} catch (Exception e) {
			return "FAIL";
		}
		return result;
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/submitFinAssessFromVisit")
	public void submitFinAssessFromVisit(
			@RequestParam(value="sheetId", required=true)String sheetId, 
			@RequestParam(value="orderId", required=true)String orderId, 
			@RequestParam(value="region", required=true)int region, 
			@RequestParam(value="month", required=true)Integer month) {
		complaintWorksheetDealImpl.submitFinAssessFromVisit(sheetId, orderId, region, month);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/submitRGHF")
	public String submitRGHF(@RequestBody String json) {
		JSONObject models = JSONObject.fromObject(json);
		String worksheetId = models.getString("worksheetId");
		int regionId = models.optInt("regionId");
		int month = models.optInt("month");
		JSONObject tscustomerVisitJson = models.optJSONObject("tscustomerVisit");
		TScustomerVisit tscustomerVisit = (TScustomerVisit) JSONObject.toBean(tscustomerVisitJson, TScustomerVisit.class);
		return complaintWorksheetDealImpl.submitRGHF(tscustomerVisit, worksheetId, regionId, month);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/hangupAssess")
	public String hangupAssess(@RequestBody Map<String, Object> models) {
		log.info("终定性待确认: {}", JSON.toJSON(models));
		JSONObject obj = JSONObject.fromObject(models);
		//投诉申诉信息
		this.updateOtherComplaintInfo(obj);
		
		String dealContent = obj.optString("dealContent");
		TsSheetQualitative tsdealQualitative = (TsSheetQualitative)JSONObject.toBean(obj.optJSONObject("tsSheetQualitative"), TsSheetQualitative.class);
		TScustomerVisit tscustomerVisit = (TScustomerVisit)JSONObject.toBean(obj.optJSONObject("tscustomerVisit"), TScustomerVisit.class);
		String res = complaintWorksheetDealImpl.hangupAssess(tsdealQualitative, tscustomerVisit, dealContent);
		log.info("终定性待确认 result: {}", res);
		if(res.equals("UPDATED") || res.equals("SUCCESS")) {
			//更新集团单状态
			ComplaintRelation sb = pubFunc.queryListByOid(tsdealQualitative.getOrderId());
			if(null != sb) {
				try {
					accessCliqueServiceFeign.updateRelaStatu(sb.getRelaGuid(), 20, "请尽快回复集团！");
				} catch (Exception e) {
					log.info("调用集团报错");
				}
			}
		}
		return res;
	}
	
	private void updateOtherComplaintInfo(JSONObject json) {
		if(json.containsKey("complaintInfo")) {//投诉申诉信息
			ComplaintInfo info = (ComplaintInfo)JSONObject.toBean(json.optJSONObject("complaintInfo"), ComplaintInfo.class);
			int num = orderAskInfoDao.updateOtherComplaintInfo(info);
			log.info("updateOtherComplaintInfo 更新结果: {}", (num > 0 ? "成功" : "失败"));
		}
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/dispatchAssess")
	public String dispatchAssess(@RequestBody String models) {
		return complaintWorksheetDealAll.dispatchAssessNew(models);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/getLatestQualitative")
	public Map getLatestQualitative(@RequestParam(value="orderID", required=true)String orderID, @RequestParam(value="regionId", required=true) int regionId) {
		return complaintWorksheetDealImpl.getLatestQualitative(orderID, regionId);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/saveUnsatisfyTemplate")
	public int saveUnsatisfyTemplate(@RequestParam(value="reason", required=true)String reason, 
			@RequestParam(value="template", required=true)String template, 
			@RequestParam(value="colOrder", required=true)int colOrder) {
		return complaintWorksheetDealImpl.saveUnsatisfyTemplate(reason, template, colOrder);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/modifyUnsatisfyTemplate")
	public int modifyUnsatisfyTemplate(
			@RequestParam(value="reason", required=true)String reason, 
			@RequestParam(value="template", required=true)String template, 
			@RequestParam(value="colOrder", required=true)int colOrder, 
			@RequestParam(value="unsatisfyId", required=true)String unsatisfyId) {
		return complaintWorksheetDealImpl.modifyUnsatisfyTemplate(reason, template, colOrder, unsatisfyId);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/queryUnsatisfyTemplate")
	public List queryUnsatisfyTemplate() {
		return complaintWorksheetDealImpl.queryUnsatisfyTemplate();
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/getDefaultMistakeOrg")
	public String getDefaultMistakeOrg(@RequestParam(value="orderId", required=true)String orderId) {
		return complaintWorksheetDealImpl.getDefaultMistakeOrg(orderId);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/getJTSSCode")
	public String getJTSSCode(@RequestParam(value = "orderId", required = true) String orderId) {
		return complaintWorksheetDealImpl.getJTSSCode(orderId);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/checkJTSSCode")
	public boolean checkJTSSCode(@RequestParam(value = "uccJTSS", required = true) String uccJTSS) {
		return complaintWorksheetDealImpl.checkJTSSCode(uccJTSS);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/checkUnifiedComplaintCode")
	public boolean checkUnifiedComplaintCode(@RequestParam(value="ucc", required=true) String ucc) {
		return complaintWorksheetDealImpl.checkUnifiedComplaintCode(ucc);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/queryUnifiedReturnByWhere")
	public List<ComplaintUnifiedReturn> queryUnifiedReturnByWhere(
			@RequestParam(value = "strWhere", required = true) String strWhere) {
		return complaintWorksheetDealImpl.queryUnifiedReturnByWhere(strWhere);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/modifyUnusualFlag")
	public String modifyUnusualFlag(@RequestParam(value="orderId") String orderId){
		int result=labelManageService.modifyUnusualFlag(orderId);
		return ReasultUtil.toResult(StaticData.SUCCESS, result);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/complaint_codeAndLogonName")
	public List complaintCodeAndLogonName(@RequestParam(value="orderId") String orderId) {
		return complaintWorksheetDealImpl.complaintCodeAndLogonName(orderId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/queryUnsatisfyTemplate")
	public Object queryUnsatisfyTemplate(@RequestBody(required=false) String parm) {
		List addWorkShift = complaintWorksheetDealImpl.queryUnsatisfyTemplate();
		return ResultUtil.success(addWorkShift);
	}
	
	@RequestMapping(value = "/workflow/dynamic/modifyUnsatisfyTemplate")
	public Object modifyUnsatisfyTemplate(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		int modifyUnsatisfyTemplate = complaintWorksheetDealImpl.modifyUnsatisfyTemplate(json.optString("reason"), json.optString("template"),
				json.optInt("colOrder"), json.optString("unsatisfyId"));
		return ResultUtil.success(modifyUnsatisfyTemplate);
	}
	
	@RequestMapping(value = "/workflow/dynamic/saveUnsatisfyTemplate")
	public Object saveUnsatisfyTemplate(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		int modifyUnsatisfyTemplate = complaintWorksheetDealImpl.saveUnsatisfyTemplate(json.optString("reason"), json.optString("template"),
				json.optInt("colOrder"));
		return ResultUtil.success(modifyUnsatisfyTemplate);
	}
	
	@RequestMapping(value = "/workflow/dynamic/delUnsatisfyTemplateNew")
	public Object delUnsatisfyTemplateNew(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		int modifyUnsatisfyTemplate = complaintWorksheetDealImpl.delUnsatisfyTemplate(json.optString("unsatisfyId"));
		return ResultUtil.success(modifyUnsatisfyTemplate);
	}
	
	@PostMapping(value = "/workflow/complaintWorksheetDeal/contactEventResult")
	public String contactEventResult(@RequestBody String reqJson) {
		JSONObject models = JSONObject.fromObject(reqJson);
		String orderId = models.getString("orderId");
		String score = models.getString("score");
		String infoXml = models.getString("infoXml");
		return autoCustomerVisit.contactEventResult(orderId, score, infoXml);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/saveWtOrderId")
	public String saveWtOrderId(@RequestBody String reqJson) {
		return acceptImpl.serviceAccpet(reqJson);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/getOrderStatu")
	public String getOrderStatu(@RequestBody String quryInfo) throws SAXException {
		return jaxRpcpubwebServiceImpl.getOrderStatu(quryInfo);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/recZDDealContent")
	public String recZDDealContent(@RequestBody String reqJson) {
		return recDealContentByZD.recZDDealContent(reqJson);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/saveServOrderForIVR")
	public String saveServOrderForIVR(@RequestBody String reqJson) {
		return refundAcceptImpl.saveServOrderForIVR(reqJson);
	}
}