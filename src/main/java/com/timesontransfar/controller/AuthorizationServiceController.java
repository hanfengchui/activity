package com.timesontransfar.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.common.PubFunc;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IAuthorizationService;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AuthorizationServiceController {
	protected static Logger log = LoggerFactory.getLogger(AuthorizationServiceController.class);
	
	@Autowired
	private IAuthorizationService authorizationServiceFacade;

	@Autowired
	private PubFunc pubFunc;

	@RequestMapping(value = "/workflow/dynamic/authGetStaff")
	public Object authGetStaff(@RequestBody(required=false) String parm) {
		log.info("authGetStaff parm:{}",parm);
		JSONObject json = JSONObject.fromObject(parm);
		TsmStaff staff = authorizationServiceFacade.getStaff(json.optString("staffId"));
		log.info("authGetStaff:{}",staff);
		return ResultUtil.success(staff);
	}
	@RequestMapping(value = "/workflow/dynamic/authSaveStaff")
	public Object authSaveStaff(@RequestBody(required=false) String parm) {
		log.info("authSaveStaff parm:{}",parm);
		JSONObject json = JSONObject.fromObject(parm);
		Map inParamMap = new HashMap();
		inParamMap.put("WEB__TSM_STAFF__STAFFNAME__ATTR_600011__2",json.optString("staffName"));
		inParamMap.put("WEB__TSM_STAFF__GENDER__ATTR_600019__2", "0");
		inParamMap.put("WEB__TSM_STAFF__ORG_ID__ATTR_600012__2", json.optString("orgId"));
		inParamMap.put("WEB__TSM_STAFF__LOGONNAME__ATTR_600014__2", json.optString("lognName"));
		inParamMap.put("WEB__TSM_STAFF__STAFF_ID__ATTR_600010__2",json.optString("staffId"));
		inParamMap.put("WEB__TSM_STAFF__PASSWORD__ATTR_600026__2",json.optString("passwd"));
		inParamMap.put("WEB__TSM_STAFF__STAFF_LEVEL__ATTR_600020__2",json.optString("level"));
		inParamMap.put("WEB__TSM_STAFF__RELAEMAIL__ATTR_600016__2",json.optString("relaemail"));
		inParamMap.put("WEB__TSM_STAFF__RELAPHONE__ATTR_600017__2",json.optString("relaphone"));
		String saveStaff = authorizationServiceFacade.saveStaff(inParamMap);
		return ResultUtil.success(saveStaff);
	}
	@RequestMapping(value = "/workflow/dynamic/authGetStaffRoleList")
	public Object authGetStaffRoleList(@RequestBody(required=false) String parm) {
		log.info("authGetStaffRoleList parm:{}",parm);
		JSONObject json = JSONObject.fromObject(parm);
		Map inParamMap = new HashMap();
		String staffId=json.getString("staffId");
		String logonStaffId=json.getString("logonStaffId");
		inParamMap.put("TSM_STAFF__LOGON_STAFF_ID",logonStaffId);
		inParamMap.put("TSM_STAFF__STAFF_ID",staffId);
		List staffRoleList = authorizationServiceFacade.getStaffRoleList(inParamMap);
		Map<String,String> object = (Map) staffRoleList.get(0);
		Map<String,String> object2 = (Map) staffRoleList.get(1);
		JSONArray arr1=new JSONArray();
		JSONArray arr2=new JSONArray();
	    JSONObject obj1=new JSONObject();
	    JSONObject obj2=new JSONObject();
	    // 使用For-Each迭代entries，通过Map.entrySet遍历key和value
	    for (Map.Entry<String, String> entry : object.entrySet()) {
	    	obj1.put("key", entry.getKey());
			obj1.put("label", entry.getValue());
			arr1.add(obj1);
			obj1.clear();
	    }
	    // 使用For-Each迭代entries，通过Map.entrySet遍历key和value
	    for (Map.Entry<String, String> entry : object2.entrySet()) {
	    	obj2.put("key", entry.getKey());
			obj2.put("label", entry.getValue());
			arr2.add(obj2);
			obj2.clear();
	    }
	    JSONArray arrstr=new JSONArray();
	    arrstr.add(arr2);
	    arrstr.add(arr1);
		return ResultUtil.success(arrstr);
	}
	@RequestMapping(value = "/workflow/dynamic/authSaveStaffRole")
	public Object authSaveStaffRole(@RequestBody(required=false) String parm) {
		log.info("authSaveStaff parm:{}",parm);
		JSONObject json = JSONObject.fromObject(parm);
		List list=(List)JSONArray.toCollection(json.optJSONArray("role"));
		String saveStaffRole = authorizationServiceFacade.saveStaffRole(json.optString("staffId"), list);
		return ResultUtil.success(saveStaffRole);
	}

	@RequestMapping(value = "/workflow/dynamic/upSummaryCallbackLog")
	public Object upSummaryCallbackLog(@RequestBody(required=false) String parm) {
		log.info("upSummaryCallbackLog parm: {}",parm);
		JSONObject json = JSONObject.fromObject(parm);
		String servOrderId = json.getString("servOrderId");
		String sheetId = json.getString("sheetId");
		int result = pubFunc.upSummaryCallbackLog(servOrderId,sheetId);
		log.info("upSummaryCallbackLog result: {}",result);
		return result;
	}

	@RequestMapping(value = "/workflow/dynamic/qrySummaryCallbackLogCache")
	public Object qrySummaryCallbackLogCache(@RequestBody(required=false) String parm) {
		log.info("qrySummaryCallbackLog parm: {}",parm);
		JSONObject json = JSONObject.fromObject(parm);
		String servOrderId = json.getString("servOrderId");
		String sheetId = json.getString("sheetId");
		String uuid = json.getString("uuid");
		if(StringUtils.isBlank(uuid)) {
			return "";
		}
		String summary = pubFunc.qrySummaryCallbackLog(servOrderId,sheetId,uuid);
		log.info("qrySummaryCallbackLog result: {}",summary);
		return summary;
	}
}
