package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.timesontransfar.customservice.staffability.pojo.StaffAbility;
import com.timesontransfar.customservice.staffability.service.IStaffAbilityService;

import net.sf.json.JSONObject;

@RestController
public class StaffAbilityController {
	protected Logger log = LoggerFactory.getLogger(StaffAbilityController.class);
	
	@Autowired
	private IStaffAbilityService staffAbilityService;
	
	@RequestMapping(value = "/workflow/dynamic/deleteStaffAbility")
	public Object deleteStaffAbility(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		int result=staffAbilityService.deleteStaffAbility(json.optString("guid"));
		return result;
	}
	
	@RequestMapping(value = "/workflow/dynamic/updateStaffAbility")
	public Object updateStaffAbility(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		StaffAbility staffAbility=(StaffAbility) JSONObject.toBean(json,StaffAbility.class);
		int result=staffAbilityService.updateStaffAbility(staffAbility);
		return result;
	}
	
}
