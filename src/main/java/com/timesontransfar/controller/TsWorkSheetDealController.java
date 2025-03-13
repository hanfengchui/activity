package com.timesontransfar.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.cliqueWorkSheetWebService.util.ReasultUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.async.WorkSheetToITSMAsync;
import com.timesontransfar.autoAccept.service.IAutoAcceptService;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDealAll;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.service.ITrackService;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.ResponsiBilityOrg;
import com.timesontransfar.customservice.worksheet.pojo.SheetOperation;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetAuditing;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitativeGrid;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.timesontransfar.customservice.worksheet.service.OrderRefundService;
import com.timesontransfar.customservice.worksheet.service.impl.TsSheetSumbitImpl;
import com.timesontransfar.labelLib.service.ILabelService;
import com.transfar.common.enums.ResultEnum;
import com.transfar.common.web.ResultUtil;
import com.timesontransfar.feign.clique.AccessCliqueServiceFeign;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


@RestController
@EnableAsync
public class TsWorkSheetDealController {
	protected Logger log = LoggerFactory.getLogger(TsWorkSheetDealController.class);
	
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	@Autowired
	private TsSheetSumbitImpl tsSheetSumbitImpl;
	@Autowired 
	private ILabelService labelServiceImpl;
	@Autowired
	private WorkSheetToITSMAsync workSheetToITSMAsync;
	@Autowired
	private IComplaintWorksheetDealAll complaintWorksheetDealAll;
	@Autowired
	private IserviceOrderAsk serviceOrderAskImpl;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	@Qualifier("AccessCliqueServiceFeign")
	private AccessCliqueServiceFeign accessCliqueServiceFeign;
	@Autowired
	private IAutoAcceptService autoAcceptServiceImpl;
	@Autowired
	private ITrackService trackServiceImpl;
    @Autowired
    private ILabelManageDAO labelManageDao;
    @Autowired
	private IserviceContentSchem serviceContentSchem;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private ComplaintWorksheetDealController complaintWorksheetDeal;
	@Autowired
	private IworkSheetBusi workSheetBusi;
	@Autowired
	private DepartmentDealController departmentDealController;
	@Autowired
	private OrderRefundService orderRefundService;

	private static final String SERVICE_ORDER_ID = "serviceOrderId";
	private static final String SHEET_ID = "sheetId";
	private static final String DEAL_CONTENT = "dealContent";
	private static final String REGION_ID = "regionId";
	private static final String MONTH_FLAG = "month";
	private static final String VISIT = "tscustomerVisit";

	/**
	 * 扩展原因获取
	 */
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/tsWorkSheetDeal/kuoZhanReason")
	public JSONArray kuoZhanReason(@RequestParam(value="lastYY", required=true)String lastYY) {
		JSONArray arr = new JSONArray();
		List oneList = pubFunc.getPlusOneList(lastYY);
		if(null == oneList)return arr;
		
		for(int i=0;i<oneList.size();i++) {
			Map m = (Map)oneList.get(i);
			String plusOne = this.getStringByKey(m, "plus_one");
			String plusOneDesc = this.getStringByKey(m, "plus_one_desc");
			JSONObject o = new JSONObject();
			o.put("value", plusOne);
			o.put("label", plusOneDesc);
			List towList = pubFunc.getPlusTwoList(lastYY,plusOne);
			if(towList != null ) {
				JSONArray arr2 = new JSONArray();
				for(int j=0;j<towList.size();j++) {
					Map m2 = (Map)towList.get(j);
					String plusTwo = this.getStringByKey(m2, "plus_two");
					String plusTwoDesc = this.getStringByKey(m2, "plus_two_desc");
					JSONObject o2 = new JSONObject();
					o2.put("value", plusTwo );
					o2.put("label", plusTwoDesc);
					arr2.add(o2);
				}
				
				//如果二级目录的数组长度大于0 二级添加 无合适选项:30000000
				if(!towList.isEmpty()) {
					JSONObject wu2 = new JSONObject();
					wu2.put("value", "30000000");
					wu2.put("label", "无合适选项");
					arr2.add(wu2);
					o.put("children", arr2);
				}
			}
			arr.add(o);
		}
		
		//如果一级目录的数组长度大于0 添加 无合适选项:30000000
		if(!oneList.isEmpty()) {
			JSONObject wu = new JSONObject();
			wu.put("value", "30000000");
			wu.put("label", "无合适选项");
			arr.add(wu);
		}
		return arr;
	}
	
	@SuppressWarnings("rawtypes")
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/getXcSheetCount")
	public int getXcSheetCount(@RequestParam(value = "sourceSheetId", required = true) String sourceSheetId,
			@RequestParam(value = "receiveOrgId", required = true) String receiveOrgId,
			@RequestParam(value = "xcType", required = true) int xcType) {
		return tsWorkSheetDeal.getXcSheetCount(sourceSheetId, receiveOrgId, xcType);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/snxcSumbitOrgDeal")
	public String snxcSumbitOrgDeal(@RequestParam(value = "worksheetId", required = true) String worksheetId,
			@RequestParam(value = REGION_ID, required = true) int regionId,
			@RequestParam(value = MONTH_FLAG, required = true) int month,
			@RequestParam(value = "xcContent", required = true) String xcContent) {
		return tsWorkSheetDeal.snxcSumbitOrgDeal(worksheetId, regionId, month, xcContent);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/jtxcSumbitOrgDeal")
	public String jtxcSumbitOrgDeal(@RequestParam(value = "worksheetId", required = true) String worksheetId,
			@RequestParam(value = REGION_ID, required = true) int regionId,
			@RequestParam(value = MONTH_FLAG, required = true) int month,
			@RequestParam(value = "xcType", required = true) int xcType,
			@RequestParam(value = "xcContent", required = true) String xcContent) {
		return tsWorkSheetDeal.jtxcSumbitOrgDeal(worksheetId, regionId, month, xcType, xcContent);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/dispatchSheetBatch")
	public int dispatchSheetBatch(@RequestBody String requestBoby) {
		log.info("dispatchSheetBatch requestBoby: {}", requestBoby);
		JSONObject models = JSONObject.fromObject(requestBoby);
		if (isRepeatSubmission("DISPATCH_SHEET_BATCH__STAFF_ID_" + models.getString("staffId"))) {
			return 0;
		}
		int num = 0;
		try {
			JSONArray array = models.getJSONArray("sheetInfoList");
			for (int i = 0; i < array.size(); i++) {
				JSONObject obj = array.getJSONObject(i);
				JSONObject checkInfo = obj.getJSONObject("checkInfo");// 转派部门校验对象
				String orderId = checkInfo.optString("orderId");
				String sheetId = checkInfo.optString("workSheetId");
				int sheetType = checkInfo.optInt("sheetType");
				log.info("orderId: {}, sheetId: {}, sheetType: {}", orderId, sheetId, sheetType);
				boolean bmclFlag = isBmclFlag(sheetType);// 是否部门处理
				String validRes = validateAndFetch(orderId, sheetId, checkInfo, bmclFlag);
				log.info("validationAndFetch orderId: {} result: {}", orderId, validRes);
				if (!"success".equals(validRes)) {// 校验和提取：success代表成功
					continue;
				}
				JSONObject sheetInfo = obj.getJSONObject("sheetInfo");// sheetInfoArry
				String resultMsg = "";
				if (bmclFlag) {// 部门处理
					resultMsg = departmentDealController.orgDealDispathSheet(sheetInfo.toString());
				} else {// 后台派单
					resultMsg = dispatchSheet(sheetInfo.toString());
				}
				JSONObject dispatchResult = JSONObject.fromObject(resultMsg);
				log.info("dispatchResult: {}", dispatchResult);
				String code = dispatchResult.optString("code");
				if ("0000".equals(code)) {
					TsmStaff staff = pubFunc.getLogonStaff();
					SheetOperation operation = new SheetOperation();
					operation.setServiceOrderId(orderId);
					operation.setWorkSheetId(sheetId);
					operation.setDealStaff(staff.getLogonName());
					operation.setDealStaffId(Integer.parseInt(staff.getId()));
					operation.setDealStaffName(staff.getName());
					operation.setDealOrgId(staff.getOrganizationId());
					operation.setDealOrgName(staff.getOrgName());
					operation.setDispatchOrg(checkInfo.optString("strOrgId"));
					operation.setDispatchOrgName(checkInfo.optString("strOrgName"));
					operation.setRemark("人工批量转派");
					int rt = tsWorkSheetDeal.saveSheetOperation(operation);
					log.info("saveSheetOperation result: {}", rt > 0 ? "success" : "fail");
					num++;
				}
			}
		} catch (Exception e) {
			log.error("dispatchSheetBatch error: {}", e.getMessage(), e);
		}
		return num;
	}

	private boolean isRepeatSubmission(String key) {
		String repeatFlag = pubFunc.querySysContolFlag(key);
		log.info("repeatFlag: {}", repeatFlag);
		if ("1".equals(repeatFlag)) {// 重复提交
			return true;
		} else {
			pubFunc.updateSysContolFlag(key, "1", 10);
			return false;
		}
	}

	private boolean isBmclFlag(int sheetType) {
		return 720130013 == sheetType || 700000127 == sheetType;
	}

	private String validateAndFetch(String orderId, String sheetId, JSONObject checkInfo, boolean bmclFlag) {
		if (bmclFlag) { // 部门处理：工单池（先校验，再提取）、我的任务（直接检验）
			return validationThenFetch(orderId, sheetId, checkInfo);
		} else { // 后台派单：工单池（先提取，再校验，如果失败工单留在操作人我的任务中）、我的任务（直接检验）
			return fetchThenValidation(orderId, sheetId, checkInfo);
		}
	}

	private String validationThenFetch(String orderId, String sheetId, JSONObject checkInfo) {
		SheetPubInfo sheetpubInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
		int lockFlag = sheetpubInfo.getLockFlag();
		if (0 != lockFlag && 1 != lockFlag) {
			return "lockFlag不为0或1";
		}
		String revOrgId = checkInfo.optString("revOrgId");
		String strOrgId = checkInfo.optString("strOrgId");
		int sendType = checkInfo.optInt("sendType");
		String xbStrOrgId = checkInfo.optString("xbStrOrgId");
		int xbSendType = checkInfo.optInt("xbSendType");
		String checkResult = tsWorkSheetDeal.validationTurnOrg(orderId, sheetId, revOrgId, strOrgId, sendType, xbStrOrgId, xbSendType);
		if (StringUtils.isNotBlank(checkResult)) {
			return checkResult;
		}
		if (0 == lockFlag) {
			int regionId = checkInfo.optInt("regionId");
			Integer monthFlag = checkInfo.optInt("monthFlag");
			String fetchInfo = workSheetBusi.fetchWorkSheet(sheetId, regionId, monthFlag);
			if (!fetchInfo.equals("SUCCESS")) {
				return "提取工单失败";
			}
		}
		return "success";
	}

	private String fetchThenValidation(String orderId, String sheetId, JSONObject checkInfo) {
		SheetPubInfo sheetpubInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
		int lockFlag = sheetpubInfo.getLockFlag();
		if (0 != lockFlag && 1 != lockFlag) {
			return "lockFlag不为0或1";
		}
		if (0 == lockFlag) {
			int regionId = checkInfo.optInt("regionId");
			Integer monthFlag = checkInfo.optInt("monthFlag");
			String fetchInfo = workSheetBusi.fetchWorkSheet(sheetId, regionId, monthFlag);
			if (!fetchInfo.equals("SUCCESS")) {
				return "提取工单失败";
			}
		}
		String revOrgId = checkInfo.optString("revOrgId");
		String strOrgId = checkInfo.optString("strOrgId");
		int sendType = checkInfo.optInt("sendType");
		String xbStrOrgId = checkInfo.optString("xbStrOrgId");
		int xbSendType = checkInfo.optInt("xbSendType");
		String checkResult = tsWorkSheetDeal.validationTurnOrg(orderId, sheetId, revOrgId, strOrgId, sendType, xbStrOrgId, xbSendType);
		if (StringUtils.isNotBlank(checkResult)) {
			return checkResult;
		}
		return "success";
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/validationTurnOrg")
	public String validationTurnOrg(
			@RequestParam(value="workSheetId", required=true) String workSheetId,
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="revOrgId", required=true) String revOrgId,
			@RequestParam(value="strOrgId", required=true) String strOrgId,
			@RequestParam(value="sendType", required=true) int sendType,
			@RequestParam(value="xbStrOrgId", required=true) String xbStrOrgId,
			@RequestParam(value="xbSendType", required=true) int xbSendType) {
		return tsWorkSheetDeal.validationTurnOrg(orderId, workSheetId, revOrgId, strOrgId, sendType, xbStrOrgId, xbSendType);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/finishSheetBatch")
	public int finishSheetBatch(@RequestBody String requestBoby) {
		log.info("finishSheetBatch requestBoby: {}", requestBoby);
		JSONObject models = JSONObject.fromObject(requestBoby);
		if (isRepeatSubmission("FINISH_SHEET_BATCH__STAFF_ID_" + models.getString("staffId"))) {
			return 0;
		}
		int num = 0;
		try {
			JSONArray array = models.getJSONArray("sheetInfoList");
			for (int i = 0; i < array.size(); i++) {
				JSONObject obj = array.getJSONObject(i);
				log.info("finishSheet JSONObject: {}", obj);
				String orderId = obj.getString("serviceOrderId");
				String sheetId = obj.getString("workSheetId");
				int sheetType = obj.getInt("sheetType");
				String resultMsg = ResultUtil.fail(ResultEnum.OBJERROR);
				if (720130011 == sheetType) {
					resultMsg = submitPDAndFinAssess(obj.toString());
				} else if (720130013 == sheetType) {
					resultMsg = submitBMCLAndFinAssess(orderId, obj.toString());
				} else if (720130017 == sheetType) {
					resultMsg = complaintWorksheetDealAll.autoSheetFinish(obj.toString());
				}
				JSONObject finishResult = JSONObject.fromObject(resultMsg);
				log.info("finishSheet orderId: {}, finishResult: {}", orderId, finishResult);
				String code = finishResult.optString("code");
				if ("0000".equals(code) || "openJiTuan".equals(code)) {
					TsmStaff staff = pubFunc.getLogonStaff();
					SheetOperation operation = new SheetOperation();
					operation.setServiceOrderId(orderId);
					operation.setWorkSheetId(sheetId);
					operation.setDealStaff(staff.getLogonName());
					operation.setDealStaffId(Integer.parseInt(staff.getId()));
					operation.setDealStaffName(staff.getName());
					operation.setDealOrgId(staff.getOrganizationId());
					operation.setDealOrgName(staff.getOrgName());
					operation.setRemark("人工批量办结");
					int rt = tsWorkSheetDeal.saveSheetOperation(operation);
					log.info("saveSheetOperation result: {}", rt > 0 ? "success" : "fail");
					num++;
				}
			}
		} catch (Exception e) {
			log.error("finishSheetBatch error: {}", e.getMessage(), e);
		}
		return num;
	}

	/**
	 * Description: 后台派单-转派<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param sheetInfoArry
	 * @return <br> WORK_SHEET_ID
	 * @CreateDate 2020年6月2日 下午2:44:58 <br>
	 */
	@PostMapping(value = "/workflow/tsWorkSheetDeal/dispatchSheet")
	public String dispatchSheet(@RequestBody String sheetInfoArry) {
		JSONObject requestJson = JSONObject.fromObject(sheetInfoArry);
		JSONArray array = requestJson.optJSONArray("sheetInfoArry");
		SheetPubInfo[] sheetpubInfo = new SheetPubInfo[array.size()];
		int mainStaff = 0;
		String mainOrg = "";
		StringBuilder slaveStaff = new StringBuilder("");
		StringBuilder slaveOrg = new StringBuilder("");
		for (int i = 0; i < array.size(); i++) {
			sheetpubInfo[i] = (SheetPubInfo) JSONObject.toBean(array.optJSONObject(i),SheetPubInfo.class);
			if(sheetpubInfo[i].getMainType() == 1) {
				mainOrg = sheetpubInfo[i].getRcvOrgId();
				mainStaff = sheetpubInfo[i].getRcvStaffId();
			} else {
				boolean lastIndex = (i == array.size() - 1);
				slaveOrg.append(this.getLastSlaveObj(lastIndex, sheetpubInfo[i].getRcvOrgId()));
				slaveStaff.append(this.getLastSlaveObj(lastIndex, sheetpubInfo[i].getRcvStaffId() + ""));
			}
		}
		
		//加企业信息化部不允许同一张服务单主办和协办同时派往，逻辑判断，不要写在前台，
		String isDispatch = requestJson.optString("isDispatch");
		String isXBDispatch = requestJson.optString("isXBDispatch");
		boolean boo = checkMainSheetDispatchFlag(isDispatch, isXBDispatch, mainStaff, mainOrg, slaveStaff.toString(), slaveOrg.toString());
		if(boo) {
			// 企业信息化部不允许同一张服务单主办和协办同时派往！
			return ReasultUtil.toResult("DISPACH_ITSM_ERROR");
		}
		
		String result = "";
		try {
			result = tsWorkSheetDeal.dispatchSheet(sheetpubInfo);
		} catch (Exception e) {
			return ReasultUtil.toResult("FAIL_SUBMIT");
		}
		
		//标签不为空就保存标签
		JSONArray labelList = requestJson.optJSONArray("labelArr");
		if(null != labelList) {
			LabelInstance[] labelArray = new LabelInstance[labelList.size()];
			for (int i = 0; i < labelList.size(); i++) {
				labelArray[i] = (LabelInstance) JSONObject.toBean(labelList.optJSONObject(i),LabelInstance.class);
			}
			labelServiceImpl.saveLabelInstance(labelArray);
		}
		
		//如果服务类型为投诉,并且派单内容中包含,投诉工单处理内容中关键字
		String content = requestJson.optString("content");
		if(content != null) {
			labelServiceImpl.checkUnusualName(content,sheetpubInfo[0].getServiceOrderId());
		}
		return ReasultUtil.toResult(result);
	}
	
	private String getLastSlaveObj(boolean lastFlag, String obj) {
		return lastFlag ? obj : obj + ",";
	}
	
	private boolean checkMainSheetDispatchFlag(String isDispatch,String isXBDispatch,int mainStaff,String mainOrg,String slaveStaff,String slaveOrg) {
		String itsmFlag="0";
		//主办部门 或者主办员工 包含企业信息化部
		if(isDispatch.equals("1")){
            if(mainOrg.equals("362813")){
            	itsmFlag="1";
            }
        }else if(isDispatch.equals("0")){//主办员工
        	String orgId = pubFunc.getStaffOrgName(mainStaff);
        	if(orgId.equals("362813")) {
        		itsmFlag="1";
        	}
        }
		
		//协办部门 或者协办员工 包含企业信息化部
		if(StringUtils.isNotEmpty(slaveOrg) || StringUtils.isNotEmpty(slaveStaff)) {
			if(isXBDispatch.equals("1")){
				if(slaveOrg.contains("362813")) {
				   if(itsmFlag.equals("1")){
					   itsmFlag="2";
	               }else{
	            	   itsmFlag="1";
	               }
				}
	        }else if(isXBDispatch.equals("0")){
	        	String[] staffList = slaveStaff.split(",");
	        	if(staffList.length > 0) {
	        		for(int i=0;i<staffList.length;i++) {
	        			String tmp = staffList[i];
	        			String s = pubFunc.getStaffOrgName(Integer.valueOf(tmp));
	        			if(s.equals("362813") && itsmFlag.equals("1")) {
	        				itsmFlag="2";
	        				break;
	                	}
	        		}
	        	}
	        }
		}
		return itsmFlag.equals("2");
	}
	
	/**
	 * 后台派单直接处理（投诉、非投诉）
	 */
	@PostMapping(value = "/workflow/tsWorkSheetDeal/dispatchToPigeonhole")
	public String dispatchToPigeonhole(@RequestBody String parm) {
		log.info("后台处理直接处理 parm: {}", parm);
		JSONObject request = JSONObject.fromObject(parm);
		SheetPubInfo sheetBean = (SheetPubInfo) JSONObject.toBean(request.optJSONObject("sheetBean"),SheetPubInfo.class);
		int dealId = request.optInt("dealId");
		String dealDesc = request.optString("dealDesc");
		
		String dispatchToPigeonhole = tsWorkSheetDeal.dispatchToPigeonhole(sheetBean, dealId, dealDesc, "0");
		log.info("后台处理直接处理 result: {}", dispatchToPigeonhole);
		if(StringUtils.isNotEmpty(dispatchToPigeonhole)){
			JSONObject result = JSONObject.fromObject(dispatchToPigeonhole);
			if(result.optString("code").equals("0000")){
				//保存标签
				this.saveLabelInstance(request);
				
				if(request.containsKey("dealContentSave")) {//后台派单直接处理：保存结案模板
					JSONArray saveArray = request.optJSONArray("dealContentSave");
					List<ServiceContentSave> saveList = new Gson().fromJson(saveArray.toString(),new TypeToken<List<ServiceContentSave>>() {}.getType());
					serviceContentSchem.saveDealContentSave(saveList, sheetBean.getServiceOrderId());
				}
				
				//保存最终处理意见标识：0-存空 1-存工单号
				if(request.containsKey("isFinalOption") && "1".equals(request.optString("isFinalOption"))) {
					labelManageDao.saveFinalOptionLabel(sheetBean.getServiceOrderId(), sheetBean.getWorkSheetId());
				}
				
				JSONObject qualitative = request.optJSONObject("qualitative");
				JSONObject visit = request.optJSONObject("visit");
				//非投诉类直接处理
				if(qualitative != null && visit != null) {
					TsSheetQualitative qua = (TsSheetQualitative) JSONObject.toBean(qualitative,TsSheetQualitative.class);
					TScustomerVisit tscustomerVisit = (TScustomerVisit) JSONObject.toBean(visit,TScustomerVisit.class);
					//20201116 非投诉提交，保存投诉原因
					tsWorkSheetDeal.saveQualitativeAndVisit(qua, tscustomerVisit);
				}
				tsSheetSumbitImpl.audSheetFinishAuto(sheetBean.getServiceOrderId(), "0");
			}
		}
		return dispatchToPigeonhole;
	}
	
	private void saveLabelInstance(JSONObject request) {
		//标签不为空就保存标签
		JSONArray labelList = request.optJSONArray("labelArr");
		if(null != labelList) {
			LabelInstance[] labelArray = new LabelInstance[labelList.size()];
			for (int i = 0; i < labelList.size(); i++) {
				labelArray[i] = (LabelInstance) JSONObject.toBean(labelList.optJSONObject(i),LabelInstance.class);
			}
			labelServiceImpl.saveLabelInstance(labelArray);
		}
	}
	
	/**
	 * Description: 后台派单-工单审核-重新派单<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param workSheetObj
	 * @param acceptContent 处理内容
	 * @param dealType 处理类型 0为部门转派工单，1为后台派单 5 审核重新派单 3为审核退单，4为部门审批退单
	 * @return <br>
	 * @CreateDate 2020年6月9日 上午10:05:49 <br>
	 */
	@PostMapping(value = "/workflow/tsWorkSheetDeal/submitAuitSheetToDeal")
	//@Transactional
	public String submitAuitSheetToDeal(@RequestBody String sheetInfo,
			@RequestParam(value="acceptContent", required=true)String acceptContent,
			@RequestParam(value="dealType", required=true)int dealType) {
		return complaintWorksheetDealAll.submitAuitSheetToDeal(sheetInfo, acceptContent, dealType);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/assignToFinish")
	public String assignToFinish(
			@RequestParam(value=REGION_ID, required=true)int regionId,
			@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value=SHEET_ID, required=true)String sheetId,
			@RequestParam(value=MONTH_FLAG, required=true)Integer month,
			@RequestParam(value=DEAL_CONTENT, required=true)String dealContent,
			@RequestParam(value="delalId", required=true)int delalId,
			@RequestParam(value="dealName", required=true)String dealName) {
		return tsWorkSheetDeal.assignToFinish(regionId, orderId, sheetId, month, dealContent, delalId, dealName);
	}

	// 后台派单终定性直接办结
	@PostMapping(value = "/workflow/tsWorkSheetDeal/submitPDAndFinAssess")
	public String submitPDAndFinAssess(@RequestBody String parm) {
		log.info("后台派单直接终定性: {}", parm);
		JSONObject request = JSONObject.fromObject(parm);
		SheetPubInfo sheetBean = (SheetPubInfo) JSONObject.toBean(request.optJSONObject("sheetBeanZDX"), SheetPubInfo.class);
		int chooseFlag = request.optInt("chooseFlag");
		if (2 == chooseFlag) {//待确认
			ComplaintRelation relation = pubFunc.queryListByOid(sheetBean.getServiceOrderId());
			if(relation != null && relation.getStatu() == 13) {
				return "ORDER_BACK";
			}
		}
		
		int dealId = request.optInt("dealIdZDX");
		String dealDesc = request.optString("dealDescZDX");
		//区分后台派单直接处理，用于判断生成的终定性工单不进行自动分派
		sheetBean.setRcvOrgId("ASSIGN_TO_FINISH");
		String dispatchToPigeonhole = tsWorkSheetDeal.dispatchToPigeonhole(sheetBean, dealId, dealDesc, getIsAutoComplete(request));
		if (StringUtils.isNotEmpty(dispatchToPigeonhole)) {
			JSONObject result = JSONObject.fromObject(dispatchToPigeonhole);
			if (result.optString("code").equals("0000")) {
				doSuccess(request);
				dispatchToPigeonhole = doFinAssess(sheetBean.getServiceOrderId(), request);
			}
		}
		return dispatchToPigeonhole;
	}

	private String getIsAutoComplete(JSONObject json) {
		if (json.containsKey("autoCompleteStaff")) {
			return "1";
		}
		return "0";
	}

	private void doSuccess(JSONObject request) {
		// 标签不为空就保存标签
		JSONArray labelList = request.optJSONArray("labelArrZDX");
		if (null != labelList) {
			LabelInstance[] labelArray = new LabelInstance[labelList.size()];
			for (int i = 0; i < labelList.size(); i++) {
				labelArray[i] = (LabelInstance) JSONObject.toBean(labelList.optJSONObject(i), LabelInstance.class);
			}
			labelServiceImpl.saveLabelInstance(labelArray);
		}
		SheetPubInfo sheetBean = (SheetPubInfo) JSONObject.toBean(request.optJSONObject("sheetBeanZDX"), SheetPubInfo.class);
		if (request.containsKey("dealContentSaveZDX")) {// 后台派单直接处理：保存结案模板
			JSONArray saveArray = request.optJSONArray("dealContentSaveZDX");
			List<ServiceContentSave> saveList = new Gson().fromJson(saveArray.toString(), new TypeToken<List<ServiceContentSave>>() {}.getType());
			serviceContentSchem.saveDealContentSave(saveList, sheetBean.getServiceOrderId());
		}
		// 保存最终处理意见标识：0-存空 1-存工单号
		if (request.containsKey("isFinalOptionZDX") && "1".equals(request.optString("isFinalOptionZDX"))) {
			labelManageDao.saveFinalOptionLabel(sheetBean.getServiceOrderId(), sheetBean.getWorkSheetId());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String doFinAssess(String orderId, JSONObject request) {
		String res = "WRONGCHOOSE";
		List sheetInfos = this.sheetPubInfoDao.getSheetListCondition(" AND service_order_id='" + orderId + "'AND sheet_type=720130017 ORDER BY creat_date DESC", true);
		if (!sheetInfos.isEmpty()) {
			SheetPubInfo sheetInfo = (SheetPubInfo) sheetInfos.get(0);
			String sheetId = sheetInfo.getWorkSheetId();
			int regionId = sheetInfo.getRegionId();
			String regionName = sheetInfo.getRegionName();
			int month = sheetInfo.getMonth();
			if (sheetInfo.getLockFlag() == 0) {
				TsmStaff s = getTsmStaff(request);
				sheetPubInfoDao.updateFetchSheetStaff(sheetId, Integer.parseInt(s.getId()), s.getName(), s.getOrganizationId(), s.getOrgName());
				sheetPubInfoDao.updateSheetState(sheetId, StaticData.WKST_DEALING_STATE_NEW, pubFunc.getStaticName(StaticData.WKST_DEALING_STATE_NEW), 14, 1);
			}
			JSONObject info = new JSONObject();
			info.put("regionId", regionId);
			info.put("month", month);
			info.put("dealContent", request.optString("dealContent"));
			info.put("upgradeIncline", request.optInt("upgradeIncline"));
			info.put("contactStatus", request.optString("contactStatus"));
			info.put("requireUninvited", request.optString("requireUninvited"));
			info.put("unifiedCode", request.optString("unifiedCode"));
			info.put("uccJTSS", request.optString("uccJTSS"));
			info.put("valiFlag", request.optInt("valiFlag"));
			if (request.containsKey("batchFinish")) {
				info.put("batchFinish", request.optString("batchFinish"));
			}
			JSONObject qualitativeJson = request.getJSONObject("tsSheetQualitative");
			qualitativeJson.put("orderId", orderId);
			qualitativeJson.put("sheetId", sheetId);
			qualitativeJson.put("region", regionId);
			qualitativeJson.put("regName", regionName);
			qualitativeJson.put("monthFlag", month);
			info.put("tsSheetQualitative", qualitativeJson);
			JSONObject visitJson = request.getJSONObject("tscustomerVisit");
			visitJson.put("serviceOrderId", orderId);
			visitJson.put("workSheetId", sheetId);
			visitJson.put("regionId", regionId);
			visitJson.put("regionName", regionName);
			visitJson.put("month", month);
			info.put("tscustomerVisit", visitJson);
			info.put("flag", 1);
			JSONObject receiptJson = new JSONObject();
			if (request.containsKey("receiptEvalZDX")) {
				receiptJson = request.getJSONObject("receiptEvalZDX");
				receiptJson.put("service_order_id", orderId);
				receiptJson.put("work_sheet_id", sheetId);
				receiptJson.put("tache_id", sheetInfo.getTacheId());
				receiptJson.put("tache_desc", sheetInfo.getTacheDesc());
			}
			info.put("receiptEval", receiptJson);
			//投诉申诉信息
			if (request.containsKey("complaintInfo")) {	
				info.put("complaintInfo", request.optJSONObject("complaintInfo"));
			}
			int chooseFlag = request.optInt("chooseFlag");
			if (1 == chooseFlag) {
				res = complaintWorksheetDealAll.autoSheetFinish(info.toString());
			} else if (2 == chooseFlag) {
				res = complaintWorksheetDeal.hangupAssess(info);
			}
		}
		return res;
	}

	private TsmStaff getTsmStaff(JSONObject json) {
		if (json.containsKey("autoCompleteStaff")) {
			return pubFunc.getStaff(json.optInt("autoCompleteStaff"));
		}
		return pubFunc.getLogonStaff();
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/audSheetFinish")
	public String audSheetFinish(@RequestBody String models) {
		//审核提交
		JSONObject obj=JSONObject.fromObject(models);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(obj.optJSONObject("servContent"),ServiceContent.class);
		SheetPubInfo sheetPubInfo = (SheetPubInfo)JSONObject.toBean(obj.optJSONObject("sheetPubInfo"),SheetPubInfo.class);
		TsSheetAuditing tsSheetAuditing = (TsSheetAuditing)JSONObject.toBean(obj.optJSONObject("tsSheetAuditing"),TsSheetAuditing.class);
		TScustomerVisit tscustomerVisit = (TScustomerVisit)JSONObject.toBean(obj.optJSONObject(VISIT),TScustomerVisit.class);
		JSONArray arr = obj.optJSONArray("dutyOrg");
		ResponsiBilityOrg[] dutyOrg = new ResponsiBilityOrg[arr.size()];
		for(int i=0;i<arr.size();i++) {
			ResponsiBilityOrg tmp = (ResponsiBilityOrg)JSONObject.toBean(JSONObject.fromObject(arr.get(i)),ResponsiBilityOrg.class);
			dutyOrg[i] = tmp;
		}
		int sumbitType=obj.optInt("sumbitType");
		//增加规则：修改工单得操作
		String result="SUCCESS";
		if(obj.optBoolean("updateFlag")){
			result=serviceOrderAskImpl.checkSelect(servContent, 3);
			if(!StaticData.SUCCESS.equals(result)){
				return ReasultUtil.toResult(result);
			}
		}
		result= tsWorkSheetDeal.audSheetFinish(sheetPubInfo, tsSheetAuditing, tscustomerVisit, sumbitType, servContent, dutyOrg);
		
		//标签不为空就保存标签
		JSONArray labelList = obj.optJSONArray("labelArr");
		if(null != labelList) {
			LabelInstance[] labelArray = new LabelInstance[labelList.size()];
			for (int i = 0; i < labelList.size(); i++) {
				labelArray[i] = (LabelInstance) JSONObject.toBean(labelList.optJSONObject(i),LabelInstance.class);
			}
			labelServiceImpl.saveLabelInstance(labelArray);
		}
		
		//新增页面逻辑
		JSONObject osj = new JSONObject();
		if (result.equals("SUCCESS")) {
			osj.put("code", "0000");
			if(sumbitType == 2) {
				osj.put("message", "归档成功！");
			}else{
				String str = "";
				//如果是集团单 : 通知集团归档操作
				boolean jiTuanFlag = false;
				ComplaintRelation bean = pubFunc.queryListByOid(sheetPubInfo.getServiceOrderId());
				if(bean != null) {
					jiTuanFlag = true;
				}
				boolean man=false;
				if(jiTuanFlag) {
					JSONObject info = new JSONObject();
					info.put("complaintWorksheetId", sheetPubInfo.getWorkSheetId());
					info.put(SERVICE_ORDER_ID, sheetPubInfo.getServiceOrderId());
					info.put("reason", "省内已处理，请集团归档");
					info.put("type", "FINISH");
					String[] res = accessCliqueServiceFeign.accessCliqueNew(info.toString());
					if("SUCCESS".equals(res[0])){
						str="已自动通知集团归档！";
					} else {
						str=res[1];
						man=true;
					}
				}
				
				if (sumbitType == 1) {//定性
					osj.put("message","提交成功！"+str);
					
				} else {//结束
					osj.put("message","工单结束成功！"+str);
				}
				
                //通知集团归档操作失败
				if(man){
					osj.put("code", "openJiTuan");
					osj.put("message","relaGuid");
				}
			}
		} else {
			osj.put("code", "-1");
			if("ALLOTREAL".equals(result)) {
				osj.put("message", "该审核单的主办单位未完成!");
			} else if("STATUSERROR".equals(result)) {
				osj.put("message", "该工单不是处理中!");
			} else {
				osj.put("message", "提交失败!");
			}
		}
		return osj.toString();
	}

	private String submitBMCLAndFinAssess(String orderId, String parm) {
		log.info("部门处理直接终定性: {}", parm);
		String result = complaintWorksheetDeal.sumbitOrgDeal(parm);
		log.info("部门处理 返回:{}", result);
		if (StringUtils.isNotEmpty(result)) {
			JSONObject res = JSONObject.fromObject(result);
			if (res.optString("code").equals("0000")) {
				JSONObject request = JSONObject.fromObject(parm);
				result = doFinAssess(orderId, request);
			}
		}
		return result;
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/orderShetFinish")
	public boolean orderShetFinish(@RequestParam(value="orderId", required=true) String  orderId,@RequestParam(value=REGION_ID, required=true)  int regionId) {
		return tsWorkSheetDeal.orderShetFinish(orderId, regionId);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/updateServiceContent")
	public String updateServiceContent(@RequestBody String parm) {
		log.info("工单修改 入参:{}", parm);
		JSONObject models = JSONObject.fromObject(parm);
		
		ServiceOrderInfo serviceInfo = new ServiceOrderInfo();
		serviceInfo.setOrderCustInfo((OrderCustomerInfo)JSONObject.toBean(models.optJSONObject("custInfo"),OrderCustomerInfo.class));
		serviceInfo.setServContent((ServiceContent)JSONObject.toBean(models.optJSONObject("servContent"),ServiceContent.class));
		serviceInfo.setOrderAskInfo((OrderAskInfo)JSONObject.toBean(models.optJSONObject("orderAskInfo"),OrderAskInfo.class));
		
		String serviceContentSave = models.optString("serviceContentSave");
		ServiceContentSave[] saves = null;
		if(com.transfar.common.utils.StringUtils.isNotEmpty(serviceContentSave)){
			JSONArray sas = JSONArray.fromObject(serviceContentSave);
			saves = new ServiceContentSave[sas.size()];
			for(int i=0;i<sas.size();i++) {
				ServiceContentSave ww = (ServiceContentSave)JSONObject.toBean(JSONObject.fromObject(sas.get(i)),ServiceContentSave.class);
				saves[i] = ww;
			}
		}
		
		String sheetId = models.optString("sheetId");
		int tachId = Integer.parseInt(models.optString("tachId"));
		String oldAcceptCent = models.optString("oldAcceptCent");//老的受理内容，拼装地模板内容
		
		//是否差错
		String[] errInfo = null;
		JSONArray arr = models.optJSONArray("err");
		log.info("err：{}", arr.toString());
		if(!arr.isEmpty()) {
			errInfo = new String[arr.size()];
			for(int i=0;i<arr.size();i++) {
				errInfo[i] = arr.optString(i);
			}
		}
		String res = tsWorkSheetDeal.updateServiceContentNew(models, serviceInfo, sheetId, tachId, saves, oldAcceptCent, errInfo);
		log.info("工单修改 返回:{}", res);
		return res;
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/submitQuitSheet")
	public String submitQuitSheet(@RequestBody String workSheetObj) {
		JSONObject obj=JSONObject.fromObject(workSheetObj);
		SheetPubInfo[] sheetPubInfo=new SheetPubInfo[obj.optJSONArray("sheetPubInfo").size()];
		for (int i = 0; i < obj.optJSONArray("sheetPubInfo").size(); i++) {
			sheetPubInfo[i]=(SheetPubInfo) JSONObject.toBean(obj.optJSONArray("sheetPubInfo").optJSONObject(i),SheetPubInfo.class);
		}
		String dealContent=obj.optString(DEAL_CONTENT);
		int askSource=obj.optInt("askSource");
		int dealType=obj.optInt("dealType");
		String result= tsWorkSheetDeal.submitQuitSheet(sheetPubInfo, dealContent, askSource, dealType);
		return ReasultUtil.toResult(result);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/submitPigeonholeSheetToDeal")
	public String submitPigeonholeSheetToDeal(@RequestBody SheetPubInfo[] workSheetObj,@RequestParam(value=DEAL_CONTENT, required=true) String dealContent) {
		return tsWorkSheetDeal.submitPigeonholeSheetToDeal(workSheetObj, dealContent);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/submitPigeonholeSheet")
	public String submitPigeonholeSheet(
			@RequestParam(value=SHEET_ID, required=true)String sheetId,
			@RequestParam(value=REGION_ID, required=true)int regionId,
			@RequestParam(value="submitType", required=true)int submitType,
			@RequestParam(value=MONTH_FLAG, required=true)Integer month,
			@RequestParam(value=DEAL_CONTENT, required=true)String dealContent) {
		return tsWorkSheetDeal.submitPigeonholeSheet(sheetId, regionId, submitType, month, dealContent);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/submitReport")
	public String submitReport(
			@RequestParam(value="reportSheet", required=true)String reportSheet,
			@RequestParam(value=SHEET_ID, required=true)String sheetId,
			@RequestParam(value=REGION_ID, required=true)int regionId,
			@RequestParam(value=MONTH_FLAG, required=true)int month,
			@RequestParam(value="reportContent", required=true)String reportContent,
			@RequestParam(value="falg", required=true)String falg) {
		return tsWorkSheetDeal.submitReport(reportSheet, sheetId, regionId, month, reportContent, falg);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/canFinishAuto")
	public boolean canFinishAuto(@RequestParam(value="worksheetId", required=true)String worksheetId) {
		return tsWorkSheetDeal.canFinishAuto(worksheetId);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/saveCmpSupplementModifySec")
	public int saveCmpSupplementModifySec(@RequestBody String parm) {
	    if(StringUtils.isEmpty(parm)) {
	    	log.info("saveCmpSupplementModifySec parm null");
	    	return 0;
	    }
		JSONObject obj=JSONObject.fromObject(parm);
		String cwi = obj.optString("cwi");
		String cpto = obj.optString("cpto");
		String cptn = obj.optString("cptn");
		String cio = obj.optString("cio");
		String cin = obj.optString("cin");
		String dro = obj.optString("dro");
		String drn = obj.optString("drn");
		String drs = obj.optString("drs");
		String askStaffId = obj.optString("askStaffId");
		String dealStaff = obj.optString("dealStaff");
		String dealContentSaveStr = obj.optString("dealContentSave");
		return tsWorkSheetDeal.saveCmpSupplementModify(cwi, cpto, cptn, cio, cin, dro, drn, drs, askStaffId, dealStaff, dealContentSaveStr);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/xcDispathSheet")
	public String xcDispathSheet(@RequestBody String model) {
		log.info("xcDispathSheet: {}", model);
		JSONObject obj = JSONObject.fromObject(model);
		JSONArray sheetPubInfoArr = obj.optJSONArray("sheetPubInfo");
		int xcType = obj.optInt("xcType");
		String orderId = obj.optString(SERVICE_ORDER_ID);
		SheetPubInfo[] arr = new SheetPubInfo[sheetPubInfoArr.size()];
		for (int i = 0; i < sheetPubInfoArr.size(); i++) {
			JSONObject sheetPubInfoItem = sheetPubInfoArr.getJSONObject(i);
			SheetPubInfo sheetPubInfo = (SheetPubInfo) JSONObject.toBean(sheetPubInfoItem, SheetPubInfo.class);
			arr[i] = sheetPubInfo;
			sheetPubInfoItem.clear();
		}
		String penaltyMoney = "";
		if (obj.containsKey("penaltyMoney")) {
			penaltyMoney = obj.optString("penaltyMoney");
		}
		String result = tsWorkSheetDeal.xcDispathSheet(arr, xcType, penaltyMoney);
		log.info("xcDispathSheet result: {}", result);
		if ("SUCCESS".equals(result)) {
			String itsmFlag = obj.optString("itsm_flag");
			if ("1".equals(itsmFlag)) {
				workSheetToITSMAsync.toITSMInfo(orderId);
			} else if ("2".equals(itsmFlag)) {
				workSheetToITSMAsync.toZDInfo(orderId);
			}
		}
		return ReasultUtil.toResult(result);
	}

	@PostMapping(value = "/workflow/tsWorkSheetDeal/analyseData")
	public int analyseData(@RequestParam(value="staffId", required=true) String staffId) {
		
		return autoAcceptServiceImpl.analyseData(staffId);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/submitOrder")
	public String submitOrder(@RequestParam(value="staffId", required=true) int staffId) {
		
		return autoAcceptServiceImpl.submitOrder(staffId);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/allotBatchSheet")
	public String allotBatchSheet(@RequestParam(SHEET_ID) String[] sheetId,
									@RequestParam("type") int type,
									@RequestParam("staffId") int staffId) {
		return tsSheetSumbitImpl.allotBatchSheet(sheetId, staffId, type);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/updateYSAndSJService")
	public String updateYSAndSJService(@RequestBody String body) {
		JSONObject obj = JSONObject.fromObject(body);
		OrderCustomerInfo custInfoObj = (OrderCustomerInfo)JSONObject.toBean(obj.optJSONObject("custInfo"), OrderCustomerInfo.class);
		ServiceContent servContentObj = (ServiceContent)JSONObject.toBean(obj.optJSONObject("servContent"), ServiceContent.class);
		OrderAskInfo orderAskInfObj = (OrderAskInfo)JSONObject.toBean(obj.optJSONObject("orderAskInfo"), OrderAskInfo.class);
		BuopSheetInfo buopSheetInfo = (BuopSheetInfo)JSONObject.toBean(obj.optJSONObject("businessInfo"), BuopSheetInfo.class);
		String sheetId = obj.optString("sheetId");
		String tachId = obj.optString("tachId");
		String errInfo = obj.optString("errInfo");
		String[] info = errInfo.split(",");
		return tsWorkSheetDeal.updateServiceContentYSAndSJ(custInfoObj, servContentObj, orderAskInfObj, buopSheetInfo, sheetId, Integer.parseInt(tachId), info);
	}

	@RequestMapping(value = "/workflow/dynamic/checkPushButton")
	public Object checkPushButton(@RequestBody(required = false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		int checkPushButton = tsWorkSheetDeal.checkPushButton(json.optString("orderId"));
		return ResultUtil.success(checkPushButton);
	}

	@RequestMapping(value = "/workflow/dynamic/noAnswerPush")
	public Object noAnswerPush(@RequestBody(required = false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String noAnswerPush = tsWorkSheetDeal.noAnswerPush(json.optString("orderId"));
		log.info("noAnswerPush result: {}", noAnswerPush);
		return ResultUtil.success(noAnswerPush);
	}
	
	@RequestMapping(value = "/workflow/dynamic/saveReAssignReason")
	public Object saveReAssignReason(@RequestBody(required=false) String parm) {
		return trackServiceImpl.saveReAssignReason(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getTrackInfo")
	public Object getTrackInfo(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		return trackServiceImpl.getTrackInfo(json.optString("orderId"));
	}
	
	@RequestMapping(value = "/workflow/dynamic/modifyTrackInfo")
	public Object modifyTrackInfo(@RequestBody(required=false) String parm) {
		return trackServiceImpl.modifyTrackInfo(parm, null);
	}

	@RequestMapping(value = "/workflow/dynamic/querySheetQualitativeGrid")
	public Object querySheetQualitativeGrid(@RequestBody(required = false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = json.optString("orderId");
		String startTime = json.optString("startTime");
		String endTime = json.optString("endTime");
		String begin = json.optString("begin");
		String pageSize = json.optString("pageSize");
		return tsWorkSheetDeal.querySheetQualitativeGrid(orderId, startTime, endTime, begin, pageSize);
	}

	@RequestMapping(value = "/workflow/dynamic/saveSheetQualitativeGrid")
	public int saveSheetQualitativeGrid(@RequestBody String models) {
		JSONObject obj = JSONObject.fromObject(models);
		TsSheetQualitativeGrid sqg = (TsSheetQualitativeGrid) JSONObject.toBean(obj.optJSONObject("tsSheetQualitativeGrid"), TsSheetQualitativeGrid.class);
		return tsWorkSheetDeal.saveSheetQualitativeGrid(sqg);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/updateComplaintSheet")
	public int updateComplaintSheet(@RequestParam(value="currentPage", required=true)int currentPage,
			@RequestParam(value="pageSize", required=true)int pageSize) {
		return autoAcceptServiceImpl.updateComplaintSheet(currentPage, pageSize);
	}
	
	@RequestMapping(value = "/workflow/dynamic/finishTrackOrder")
	public Object finishTrackOrder(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String oldOrderId = json.optString("oldOrderId");
		return trackServiceImpl.finishTrackOrder(oldOrderId, null);
	}
	
	@PostMapping(value = "/workflow/tsWorkSheetDeal/autoFinishTrackOrder", produces="application/json;charset=utf-8")
	public String autoFinishTrackOrder(@RequestBody String param) {
		JSONObject json = JSONObject.fromObject(param);
		String oldOrderId = json.optString("oldOrderId");
		String trackOrderId = json.optString("trackOrderId");
		return trackServiceImpl.finishTrackOrder(oldOrderId, trackOrderId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/isAnalysisOrder")
	public Object isAnalysisOrder(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String orderId = json.optString("orderId");
		int comeCategory = json.optInt("comeCategory");
		int phnTypeId = json.optInt("phnTypeId");
		int bestOrder = json.optInt("bestOrder");
		boolean flag = trackServiceImpl.isAnalysisOrder(comeCategory, phnTypeId, bestOrder);
		log.info("isAnalysisOrder orderId: {} flag: {}", orderId, flag);
		return ResultUtil.success(flag);
	}
	
	@RequestMapping(value = "/workflow/dynamic/xcRefundDispathSheet")
	public Object xcRefundDispathSheet(@RequestBody(required=false) String param) {
		log.info("xcRefundDispathSheet: {}", param);
		JSONObject obj = JSONObject.fromObject(param);
		
		JSONObject sheetPubInfo = obj.optJSONObject("sheetPubInfo");
		String prmRefundAmount = obj.optString("prmRefundAmount");
		String refundDetail = obj.optString("refundDetail");
		String refundData = obj.optString("refundData");
		SheetPubInfo curSheet = (SheetPubInfo) JSONObject.toBean(sheetPubInfo, SheetPubInfo.class);
		
		String result = "";
		try {
			result = tsWorkSheetDeal.xcRefundDispathSheet(curSheet, prmRefundAmount, refundDetail, refundData);
		} catch (Exception e) {
			result = "ERROR";
			log.error("xcRefundDispathSheet: {}", e.getMessage(), e);
		}
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/submitRefundApproveResult")
	public Object submitRefundApproveResult(@RequestBody(required=false) String param) {
		log.info("submitRefundApproveResult: {}", param);
		JSONObject obj = JSONObject.fromObject(param);
		
		String worksheetId = obj.optString("worksheetId");
		int regionId = obj.optInt("regionId");
		int month = obj.optInt("month");
		String xcContent = obj.optString("xcContent");
		int approveFlag = obj.optInt("approveFlag");
		
		String result = "";
		try {
			result = tsWorkSheetDeal.snxcSumbitOrgDeal(worksheetId, regionId, month, xcContent);
			if("SUCCESS".equals(result)) {
				orderRefundService.updateRefundApproveInfo(worksheetId, approveFlag);
			}
		} catch (Exception e) {
			result = "ERROR";
			log.error("snxcSumbitOrgDeal: {}", e.getMessage(), e);
		}
		return ResultUtil.success(result);
	}
	
}