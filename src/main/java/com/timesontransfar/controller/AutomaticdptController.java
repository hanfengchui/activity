package com.timesontransfar.controller;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.timesontransfar.autoAccept.service.IAutoAcceptService;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.staffability.service.IStaffAbilityService;
import com.timesontransfar.flowskillrela.pojo.FlowSkillRela;
import com.timesontransfar.flowskillrela.service.IFlowSkillRelaService;
import com.timesontransfar.staffSkill.StaffSkillInfo;
import com.timesontransfar.staffSkill.service.StaffWorkSkill;
import com.timesontransfar.staffWorkShift.pojo.StaffWorkShift;
import com.timesontransfar.staffWorkShift.service.IStaffWorkShiftService;
import com.timesontransfar.workshift.service.IWorkShiftService;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class AutomaticdptController {
	protected Logger log = LoggerFactory.getLogger(AutomaticdptController.class);
	
	@Autowired
	private IStaffAbilityService staffAbilityService;
	@Autowired
	private StaffWorkSkill staffWorkSkill;
	@Autowired
	private IFlowSkillRelaService flowSkillRelaService;
	@Autowired
	private IWorkShiftService workShiftService;
	@Autowired
	private IStaffWorkShiftService staffWorkShiftService; 
	@Autowired
	private IAutoAcceptService autoAcceptServiceImpl;
	
	@RequestMapping(value = "/workflow/ftp/uploadstaffAbility",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadstaffAbility(@RequestPart("file") MultipartFile file,@RequestParam(value="logonName",required=false) String logonName){
		log.info("/ftp/uploadstaffAbility");
		//解析excel文件
	    String result="";
	    try {
	    	result=staffAbilityService.saveStaffAbilityBatch(logonName, file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/workflow/ftp/uploadstaffWorkShift",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String uploadstaffWorkShift(@RequestPart("file") MultipartFile file,@RequestParam(value="logonName",required=false) String logonName){
		log.info("/ftp/uploadstaffWorkShift");
		//解析excel文件
	    String result="";
	    try {
	    	result=staffWorkShiftService.saveStaffWorkShiftBatch(logonName, file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/workflow/dynamic/getSkillType")
	public Object getSkillType(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		String skillType = flowSkillRelaService.getSkillType(json.optString("orgId"), json.optString("serviceDate"));
		return ResultUtil.success(skillType);
	}
	
	@RequestMapping(value = "/workflow/dynamic/updateStaffSkill")
	public Object deleteStaffAbility(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		int deleteStaffAbility = staffWorkSkill.updateStaffSkill(json.optString("guid"));
		return ResultUtil.success(deleteStaffAbility>0?"成功":"失败");
	}
	
	@RequestMapping(value = "/workflow/dynamic/deleteStaffSkillBatch")
	public Object deleteStaffSkillBatch(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		String[] guids=json.optString("guids").split(",");
		int deleteStaffSkillBatch = staffWorkSkill.deleteStaffSkillBatch(guids);
		return ResultUtil.success(deleteStaffSkillBatch>0?"成功":"失败");
	}
	
	@RequestMapping(value = "/workflow/dynamic/saveStaffSkillBatch")
	public Object saveStaffSkillBatch(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		JSONArray array=json.optJSONArray("arr");
		StaffSkillInfo[] beans=new StaffSkillInfo[array.size()];
		for (int i = 0; i < array.size(); i++) {
			JSONObject obj=array.getJSONObject(i);
			StaffSkillInfo staffSkillInfo=(StaffSkillInfo) JSONObject.toBean(obj,StaffSkillInfo.class);
			beans[i]=staffSkillInfo;
		}
		int num=staffWorkSkill.saveStaffSkillBatch(beans, 1);
		return ResultUtil.success(num>0?"成功":"失败");
	}
	
	@RequestMapping(value = "/workflow/dynamic/getWorkShift")
	public Object getWorkShift(@RequestBody(required=false) String parm) {
		List workshift = workShiftService.getWorkShift();
		return ResultUtil.success(workshift);
	}
	
	@RequestMapping(value = "/workflow/dynamic/updateStaffWorkShift")
	public Object updateStaffWorkShift(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		StaffWorkShift staffWorkShift=(StaffWorkShift) JSONObject.toBean(json,StaffWorkShift.class);
		int updateStaffWorkShift = staffWorkShiftService.updateStaffWorkShift(staffWorkShift);
		return ResultUtil.success(updateStaffWorkShift>0?"成功":"失败");
	}
	
	@RequestMapping(value = "/workflow/dynamic/deleteStaffWorkShift")
	public Object deleteStaffWorkShift(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		int updateStaffWorkShift = staffWorkShiftService.deleteStaffWorkShift(json.optString("guid"));
		return ResultUtil.success(updateStaffWorkShift>0?"成功":"失败");
	}
	
	@RequestMapping(value = "/workflow/dynamic/deleteBatchStaffWorkShift")
	public Object deleteBatchStaffWorkShift(@RequestBody(required = false) String parm) {
		JsonObject obj = new Gson().fromJson(parm, JsonObject.class);
		JsonArray guidArray = obj.getAsJsonArray("guids");
		List guids = new Gson().fromJson(guidArray, List.class);
		
		int result = staffWorkShiftService.deleteBatchStaffWorkShift(guids);
		return ResultUtil.success(result>0?"成功":"失败");
	}
	
	@RequestMapping(value = "/workflow/dynamic/deleteFlowSkillRela")
	public Object deleteFlowSkillRela(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		int updateStaffWorkShift = flowSkillRelaService.deleteFlowSkillRela(json.optString("guid"),json.optString("serviceDate"),json.optString("orgId"));
		return ResultUtil.success(updateStaffWorkShift>0?"成功":"失败");
	}
	
	@RequestMapping(value = "/workflow/dynamic/modifyFlowSkillRela")
	public Object modifyFlowSkillRela(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		FlowSkillRela oldRela=(FlowSkillRela) JSONObject.toBean(json,FlowSkillRela.class);
		int updateStaffWorkShift = flowSkillRelaService.modifyFlowSkillRela(oldRela);
		return ResultUtil.success(updateStaffWorkShift>0?"成功":"失败");
	}
	
	@RequestMapping(value = "/workflow/dynamic/addFlowSkillRela")
	public Object addFlowSkillRela(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		FlowSkillRela oldRela=(FlowSkillRela) JSONObject.toBean(json,FlowSkillRela.class);
		int updateStaffWorkShift = flowSkillRelaService.addFlowSkillRela(oldRela);
		return updateStaffWorkShift>0? ResultUtil.success("成功") : ResultUtil.error("该流向部门已配置技能类型，请重新选择部门！");
	}
	
	@RequestMapping(value = "/workflow/ftp/saveOrderInfoBatch",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String saveOrderInfoBatch(@RequestPart("file") MultipartFile file,@RequestParam(value="logonName",required=false) String logonName){
	    String result="";
	    try {
	    	result=autoAcceptServiceImpl.saveOrderInfoBatch(logonName, file.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/workflow/ftp/addComplaintInfo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public String addComplaintInfo(@RequestPart("file") MultipartFile file,@RequestParam(value="operator",required=false) String operator) {
		String result = "";
		try {
			result = autoAcceptServiceImpl.saveComplaintInfo(file.getInputStream(), operator);
		}catch (IOException e){
			e.printStackTrace();
		}
		return result;
	}
	
	@RequestMapping(value = "/workflow/dynamic/getComplaintSheet")
	public Object getComplaintSheet(@RequestBody(required = false) String param) {
		JSONObject obj = JSONObject.fromObject(param);
		JSONArray createDate = obj.optJSONArray("createDate");
		String state = obj.optString("state");
		int currentPage = obj.optInt("currentPage");
		int pageSize = obj.optInt("pageSize");
		GridDataInfo complaintSheet = autoAcceptServiceImpl.getComplaintSheet(currentPage, pageSize, createDate, state, false);
		return ResultUtil.success(complaintSheet);
	}
	
	@RequestMapping(value = "/workflow/dynamic/updateComplaintSheet")
	public Object updateComplaintSheet(@RequestBody(required = false) String param) {
		JSONObject obj = JSONObject.fromObject(param);
		int currentPage = obj.optInt("currentPage");
		int pageSize = obj.optInt("pageSize");
		int num = autoAcceptServiceImpl.updateComplaintSheet(currentPage, pageSize);
		return ResultUtil.success(num);
	}
}
