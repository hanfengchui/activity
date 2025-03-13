package com.timesontransfar.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.cliqueWorkSheetWebService.util.ReasultUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.impl.DepartmentCountImpl;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetTodispatch;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetAuditing;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.impl.TsSheetSumbitImpl;
import com.timesontransfar.labelLib.service.ILabelService;
import com.transfar.common.enums.ResultEnum;
import com.transfar.common.web.ResultUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@EnableAsync
public class DepartmentDealController {
	protected Logger log = LoggerFactory.getLogger(DepartmentDealController.class);
	
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	@Autowired
	private TsSheetSumbitImpl tsSheetSumbitImpl;
	@Autowired 
	private ILabelService labelServiceImpl;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private DepartmentCountImpl departmentCount;
    @Autowired
    private ILabelManageDAO labelManageDao;
    @Autowired
	private IserviceContentSchem serviceContentSchem;
	
	/**
	 * Description: 部门处理-提交
	 * @author huangbaijun
	 * @CreateDate 2020年6月2日 下午2:44:58
	 */
	@PostMapping(value = "/workflow/departmentDeal/dealsheet")
	//@Transactional
	public String dispatchSheetZX(@RequestBody String dealsheet) {
		log.info("部门处理提交: {}", dealsheet);
		if(StringUtils.isEmpty(dealsheet)){
			return ResultUtil.fail(ResultEnum.OBJERROR);
		}
		JSONObject formatdealSheet=JSONObject.fromObject(dealsheet);
		SheetPubInfo sheetPubInfo=null;
		TsSheetAuditing tsSheetAuditing=null;
		int delalId=formatdealSheet.optInt("delalId");
		String dealName=formatdealSheet.optString("dealName");

		//1:保存转派原因
		if(formatdealSheet.optBoolean("autoFlag")){//保存预派内容
			SheetTodispatch todispatch=(SheetTodispatch) JSONObject.toBean(formatdealSheet.optJSONObject("sheetTodispatch"), SheetTodispatch.class);
			//执行保存建议转派
			String todispath=tsSheetSumbitImpl.saveDispReason(todispatch);
			if("ERROR".equals(todispath)){
				return ResultUtil.error("保存转派建议信息失败");
			}
		}
		
		//2：提交部门处理方法
		sheetPubInfo=(SheetPubInfo) JSONObject.toBean(formatdealSheet.optJSONObject("sheetPubInfo"),SheetPubInfo.class);
		tsSheetAuditing=(TsSheetAuditing) JSONObject.toBean(formatdealSheet.optJSONObject("tsSheetAuditing"),TsSheetAuditing.class);
		String result = tsWorkSheetDeal.sumbitOrgDeal(sheetPubInfo, tsSheetAuditing, delalId, dealName);
		
		JSONObject osj = new JSONObject();
		if(!result.equals("success")) {
			osj.put("code", "-1");
			String errInfo = "提交失败";
			if(result.equals("statusError")) {
				errInfo = "该工单不是处理工单";
			}else if(result.equals("reasonNull")) {
				errInfo = "没有填写回单原因";
			}else if(result.equals("ALLOTREAL")) {
				errInfo = "主办单位没有回单,不能做审批";
			}
			osj.put("message", errInfo);
			return osj.toString();
		}
		
		if(formatdealSheet.containsKey("dealContentSave")) {//非投诉部门处理（非审批）：保存结案模板
			JSONArray saveArray = formatdealSheet.optJSONArray("dealContentSave");
			List<ServiceContentSave> saveList = new Gson().fromJson(saveArray.toString(),new TypeToken<List<ServiceContentSave>>() {}.getType());
			serviceContentSchem.saveDealContentSave(saveList, sheetPubInfo.getServiceOrderId());
		}
		
		//保存最终处理意见标识：0-存空 1-存工单号
		if(formatdealSheet.containsKey("isFinalOption")) {
			if("1".equals(formatdealSheet.optString("isFinalOption"))) {
				labelManageDao.saveFinalOptionLabel(sheetPubInfo.getServiceOrderId(), sheetPubInfo.getWorkSheetId());
			}
		}
		
		//4: 提交成功后，保存标签
		JSONArray labelList=formatdealSheet.optJSONArray("labelInstance");
		if(null != labelList) {
			LabelInstance[] labelArray = new LabelInstance[labelList.size()];
			for (int i = 0; i < labelList.size(); i++) {
				labelArray[i] = (LabelInstance) JSONObject.toBean(labelList.optJSONObject(i),LabelInstance.class);
			}
			labelServiceImpl.saveLabelInstance(labelArray);
		}

		
		//6:自动办结方法
		boolean auto = formatdealSheet.optBoolean("autoFlag");
		log.info("是否自动办结标识:{}", auto);
		String msg = "处理成功！";
		if(auto){
			TsSheetQualitative qualitative =(TsSheetQualitative) JSONObject.toBean(formatdealSheet.optJSONObject("qualitative"),TsSheetQualitative.class);
			TScustomerVisit visit =(TScustomerVisit) JSONObject.toBean(formatdealSheet.optJSONObject("visit"),TScustomerVisit.class);
			tsWorkSheetDeal.saveQualitativeAndVisit(qualitative,visit);
			String dt = tsSheetSumbitImpl.audSheetFinishAuto(sheetPubInfo.getServiceOrderId(), "0");
			if("0".equals(dt)){
				msg +="自动完结服务单成功";
			}else{
				msg +="自动完结服务单失败，请继续手动完结服务单";
			}
		}
		osj.put("code", "0000");
		osj.put("message", msg);
		return osj.toString();
	}
	/**
	 * Description: 部门处理-转派<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param SheetObj
	 * @param dealResult
	 * @param askSource
	 * @param dealType
	 * @return <br>
	 * @CreateDate 2020年6月8日 下午2:55:59 <br>
	 */
	@PostMapping(value = "/workflow/departmentDeal/orgDealDispathSheet")
	public String orgDealDispathSheet(@RequestBody String sheetObj) {
		log.info("orgDealDispathSheet param: {}", sheetObj);
		JSONObject reqFormat = JSONObject.fromObject(sheetObj);
		String dealResult = reqFormat.optString("dealResult");
		if(StringUtils.isEmpty(dealResult)){
			return ResultUtil.error("派单内容不能为空");
		}
		JSONArray sheetInfo = reqFormat.optJSONArray("sheetInfoArry");
		SheetPubInfo[] workSheetObj = new SheetPubInfo[sheetInfo.size()];
		for (int i = 0; i < sheetInfo.size(); i++) {
			JSONObject obj = sheetInfo.optJSONObject(i);
			workSheetObj[i] = (SheetPubInfo)JSONObject.toBean(obj, SheetPubInfo.class);
		}
		int askSource = reqFormat.optInt("askSource");
		int dealType = reqFormat.optInt("dealType");
		JSONObject paidanArr = reqFormat.getJSONObject("paidanArr");
		String isDispatch = paidanArr.getString("isDispatch");
		String isXBDispatch = paidanArr.getString("isXBDispatch");
		String mainStaff = paidanArr.getString("mainStaff");
		int mainStaffId = 0;
		if(!"".contentEquals(mainStaff)) {
			mainStaffId = Integer.parseInt(mainStaff);
		}
		String mainOrg = paidanArr.getString("mainOrg");
		String slaveStaff = paidanArr.getString("slaveStaff");
		String slaveOrg = paidanArr.getString("slaveOrg");
		boolean boo = checkMainSheetDispatchFlag(isDispatch, isXBDispatch, mainStaffId, mainOrg, slaveStaff, slaveOrg);
		if(boo) {
			// 企业信息化部不允许同一张服务单主办和协办同时派往！
			return ReasultUtil.toResult("DISPACH_ITSM_ERROR");
		}
		JSONObject osj = new JSONObject();
		String result = "";
		try {
			result = tsWorkSheetDeal.orgDealDispathSheet(workSheetObj, dealResult, askSource, dealType, 0);
		} catch (Exception e) {
			osj.put("code", "9999");
			osj.put("message", "提交失败！");
			return osj.toString();
		}
		
		log.info("orgDealDispathSheet res: {}", result);
		if(StaticData.SUCCESS.equals(result)){
			osj.put("code", "0000");
			osj.put("message", "转派单成功！");
			JSONArray labelInstance = reqFormat.optJSONArray("labelInstance");
			if(null != labelInstance) {
				LabelInstance[] labelArray = new LabelInstance[labelInstance.size()];
				for (int i = 0; i < labelInstance.size(); i++) {
					labelArray[i] = (LabelInstance) JSONObject.toBean(labelInstance.optJSONObject(i),LabelInstance.class);
				}
				labelServiceImpl.saveLabelInstance(labelArray);
			}
		} else {
			osj.put("code", "-1");
			this.setErrorMessageObj(osj, result);
		}
	    return osj.toString();
	}
	
	private void setErrorMessageObj(JSONObject osj, String result) {
		if(result.equals("ERROR")) {
			osj.put("message", "转派单失败");
		}else if(result.equals("ALLOTREAL")) {
			osj.put("message", "该审批单的主办单位为完成");
		}else if(result.equals("STATUSERROR")) {
			osj.put("message", "该工单不是处理中");
		}else if(result.equals("CHECKlINK")) {
			osj.put("message", "不能往上级单位派单");
		}else {
			osj.put("message", "操作失败");
		}
	}
	
	private boolean checkMainSheetDispatchFlag(String isDispatch,String isXBDispatch,int mainStaff,String mainOrg,String slaveStaff,String slaveOrg) {
		String itsmFlag = "0";
		//主办部门 或者主办员工 包含企业信息化部
		if(isDispatch.equals("1")){
            if(mainOrg.equals("362813")){
            	itsmFlag = "1";
            }
        }else if(isDispatch.equals("0")){//主办员工
        	String orgId = pubFunc.getStaffOrgName(mainStaff);
        	if(orgId.equals("362813")){
        		itsmFlag = "1";
        	}
        }
		
		//协办部门 或者协办员工 包含企业信息化部
		if(StringUtils.isNotEmpty(slaveOrg) || StringUtils.isNotEmpty(slaveStaff)) {
			if(isXBDispatch.equals("1")){
				if(slaveOrg.contains("362813")){
				   if(itsmFlag.equals("1")){
					   itsmFlag = "2";
	               }else{
	            	   itsmFlag = "1";
	               }
				}
	        }else if(isXBDispatch.equals("0")){
	        	String[] staffList = slaveStaff.split(",");
	        	if(staffList.length > 0) {
	        		for(int i=0; i<staffList.length; i++) {
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
	
	@PostMapping(value = "/workflow/departmentDeal/queryWorkSheet")
	public GridDataInfo queryWorkSheet(@RequestBody String sheetObj){
		return departmentCount.queryWorkSheet(sheetObj);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/findStatffPhoneByOrgId")
	public String findStatffPhoneByOrgId(@RequestParam("orgId")String orgId) {
		return departmentCount.findStatffPhoneByOrgId(orgId);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/sendMassage")
	public int sendMassage(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		String phoneType=j.optString("type");
		String phone=j.optString("phone");
		String message=j.optString("content");
		String orderId=j.optString("orderId");
		String sheetId=j.optString("sheetId");
		String sysx=j.optString("sjsx");
		return departmentCount.sendMassage(Integer.parseInt(phoneType), phone, message, orderId, sheetId, sysx);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/getWorkSinglePondCountMessages")
	public GridDataInfo getWorkSinglePondCountMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		int begion=j.optInt("begion");
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String serviceType=j.optString("serviceType");
		return departmentCount.getWorkSinglePondCountMessages(begion, areaId, departmentId, serviceType);
	}

	@PostMapping(value = "/workflow/departmentDeal/getSatisfyCountMessages")
	public GridDataInfo getSatisfyCountMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		return departmentCount.getSatisfyCountMessages(areaId, departmentId);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/getMyWorkSingleCountMessages")
	public GridDataInfo getMyWorkSingleCountMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		int begion=j.optInt("begion");
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String serviceType = j.optString("serviceType");
		return departmentCount.getMyWorkSingleCountMessages(begion, areaId, departmentId , serviceType);
	}
	@PostMapping(value = "/workflow/departmentDeal/getMyWorkSatisfyCountMessages")
	public GridDataInfo getMyWorkSatisfyCountMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		return departmentCount.getMyWorkSatisfyCountMessages(areaId, departmentId);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/getSentWorkSingleCountMessages")
	public GridDataInfo getSentWorkSingleCountMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		int begion=j.optInt("begion");
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String serviceType=j.optString("serviceType");
		return departmentCount.getSentWorkSingleCountMessages(begion, areaId, departmentId, serviceType);
	}

	@PostMapping(value = "/workflow/departmentDeal/getSentWorkSatisfyCountMessages")
	public GridDataInfo getSentWorkSatisfyCountMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		return departmentCount.getSentWorkSatisfyCountMessages(areaId, departmentId);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/getMyWorkSingleCountMessagesByStaff")
	public GridDataInfo getMyWorkSingleCountMessagesByStaff(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		int begion = j.optInt("begion");
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String serviceType=j.optString("serviceType");
		return departmentCount.getMyWorkSingleCountMessagesByStaff(begion, areaId, departmentId , serviceType );
	}

	@PostMapping(value = "/workflow/departmentDeal/getMyWorkSatisfyCountMessagesByStaff")
	public GridDataInfo getMyWorkSatisfyCountMessagesByStaff(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		int begion = j.optInt("begion");
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		return departmentCount.getMyWorkSatisfyCountMessagesByStaff(begion, areaId, departmentId);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/getSentWorkSingleCountMessagesByStaff")
	public GridDataInfo getSentWorkSingleCountMessagesByStaff(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		int begion=j.optInt("begion");
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String serviceType=j.optString("serviceType");
		return departmentCount.getSentWorkSingleCountMessagesByStaff(begion, areaId, departmentId, serviceType );
	}

	@PostMapping(value = "/workflow/departmentDeal/getSentWorkSatisfyCountMessagesByStaff")
	public GridDataInfo getSentWorkSatisfyCountMessagesByStaff(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		int begion=j.optInt("begion");
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		return departmentCount.getSentWorkSatisfyCountMessagesByStaff(begion, areaId, departmentId);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/getSentWorkSingleMessages")
	public GridDataInfo getSentWorkSingleMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String lockFlag=j.optString("lockFlag");
		String staffId=j.optString("staffId");
		String begion=j.optString("begion");
		String serviceType=j.optString("serviceType");
		return departmentCount.getSentWorkSingleMessages(areaId, departmentId, Integer.parseInt(lockFlag), staffId,serviceType, begion);
	}

	@PostMapping(value = "/workflow/departmentDeal/getSentWorkSatisfyMessages")
	public GridDataInfo getSentWorkSatisfyMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String lockFlag=j.optString("lockFlag");
		String staffId=j.optString("staffId");
		String begion=j.optString("begion");
		return departmentCount.getSentWorkSatisfyMessages(areaId, departmentId, Integer.parseInt(lockFlag), staffId, begion);
	}
	
	@PostMapping(value = "/workflow/departmentDeal/getSentAllWorkSingleMessages")
	public GridDataInfo getSentAllWorkSingleMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String lockFlag=j.optString("lockFlag");
		String staffId=j.optString("staffId");
		return departmentCount.getSentAllWorkSingleMessages(areaId, departmentId, Integer.parseInt(lockFlag), staffId);
	}

	@PostMapping(value = "/workflow/departmentDeal/getSentAllWorkSatisfyMessages")
	public GridDataInfo getSentAllWorkSatisfyMessages(@RequestBody String param) {
		JSONObject j=JSONObject.fromObject(param);
		String areaId=j.optString("areaId");
		String departmentId=j.optString("departmentId");
		String lockFlag=j.optString("lockFlag");
		String staffId=j.optString("staffId");
		return departmentCount.getSentAllWorkSatisfyMessages(areaId, departmentId, Integer.parseInt(lockFlag), staffId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getOrgMonitor")
	public Object getOrgMonitor(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		StringBuilder sb=new StringBuilder();
		JSONObject strWhere=json.optJSONObject("strWhere");
		if(StringUtils.isNotEmpty(strWhere.optString("servType"))){
			sb.append(" AND CC.SERVICE_TYPE = " + strWhere.optString("servType"));
		}
		if(StringUtils.isNotEmpty(strWhere.optString("returnOrg"))){
			sb.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			sb.append(" CONCAT_WS('',( SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + strWhere.optString("returnOrg") + "'),'%')) ");
		}
		GridDataInfo orgMonitor = departmentCount.getOrgMonitor(json.optInt("begion"),json.optString("areaId"), json.optString("orgid"),
				sb.toString());
		return ResultUtil.success(orgMonitor);
	}

	@RequestMapping(value = "/workflow/dynamic/getOrgSatisfyMonitor")
	public Object getOrgSatisfyMonitor(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		StringBuilder sb=new StringBuilder();
		JSONObject strWhere=json.optJSONObject("strWhere");
		if(StringUtils.isNotEmpty(strWhere.optString("returnOrg"))){
			sb.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			sb.append(" CONCAT_WS('',( SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + strWhere.optString("returnOrg") + "'),'%')) ");
		}
		GridDataInfo orgMonitor = departmentCount.getOrgSatisfyMonitor(json.optInt("begion"),json.optString("areaId"), json.optString("orgid"),
				sb.toString());
		return ResultUtil.success(orgMonitor);
	}
}
