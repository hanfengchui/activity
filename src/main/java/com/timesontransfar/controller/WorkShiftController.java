package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.timesontransfar.workshift.pojo.WorkShift;
import com.timesontransfar.workshift.service.IWorkShiftService;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

@RestController
public class WorkShiftController {
	protected Logger log = LoggerFactory.getLogger(WorkShiftController.class);
	
	@Autowired
	private IWorkShiftService workShiftService;
	
	@RequestMapping(value = "/workflow/dynamic/modifyWorkShift")
	public Object modifyWorkShift(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		WorkShift workShift=(WorkShift) JSONObject.toBean(json,WorkShift.class);	
		int modifyWorkShift = workShiftService.modifyWorkShift(workShift);
		return ResultUtil.success(modifyWorkShift);
	}
	
	@RequestMapping(value = "/workflow/dynamic/addWorkShift")
	public Object addWorkShift(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		WorkShift workShift=(WorkShift) JSONObject.toBean(json,WorkShift.class);	
		String addWorkShift = workShiftService.addWorkShift(workShift);
		return ResultUtil.success(addWorkShift);
	}
	
}
