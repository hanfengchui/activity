package com.timesontransfar.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.starStaff.service.IStarStaffServ;

import net.sf.json.JSONArray;

@RestController
@SuppressWarnings("rawtypes")
public class StarEmployeesController {
	protected Logger log = LoggerFactory.getLogger(StarEmployeesController.class);
	
	@Autowired
	private IStarStaffServ starStaffServImpl;
	
	
	@PostMapping("/starStaff/getStarStaffTop")
	public List getStarStaffTop(@RequestParam(value="orgId", required=true) String orgId) {
		List list=starStaffServImpl.getStarTop(orgId);
		return list;
	}
	
	@PostMapping("/starStaff/saveStarStaff")
	public int[] saveStarStaff(@RequestParam(value="staffList", required=true) String staffList,
								@RequestParam(value="orgId", required=true) String orgId) {
		JSONArray arr=JSONArray.fromObject(staffList);
		return starStaffServImpl.saveStarStaff(arr,orgId);
	}
	
	@PostMapping("/starStaff/editStaffTop")
	public int editStaffTop(@RequestParam(value="staff_id", required=true) String staffId,
							@RequestParam(value="staff_top", required=true)String staffTop) {
		return starStaffServImpl.editStaffTop(staffId, staffTop);
	}
	
	@PostMapping("/starStaff/deleteStarStaff")
	public int[] deleteStarStaff(@RequestParam(value="staffList", required=true) String staffList) {
		return starStaffServImpl.deleteStarStaff(staffList);
	}
	
	@PostMapping("/starStaff/editStaffTitle")
	public int editStaffTitle(@RequestParam(value="title", required=true) String title,@RequestParam(value="linkId", required=true) String linkId){
		return starStaffServImpl.editStaffTitle(title,linkId);
	}
}
