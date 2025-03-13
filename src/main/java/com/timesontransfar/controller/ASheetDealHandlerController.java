package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.sheetHandler.ASheetDealHandler;

import net.sf.json.JSONObject;

@RestController
public class ASheetDealHandlerController {
	protected Logger log = LoggerFactory.getLogger(ASheetDealHandlerController.class);
	
	@Autowired
	private ASheetDealHandler sheetDealHandler;
	
	@RequestMapping(value = "/workflow/ASheetDealHandler/inthangup", method = RequestMethod.POST)
	public String hangup(
			@RequestParam(value="sheetId", required=true) String sheetId, 
			@RequestParam(value="comment", required=false) String comment, 
			@RequestParam(value="flag", required=true) int flag, 
			@RequestParam(value="holdStateId", required=false) int holdStateId, 
			@RequestParam(value="newLockFlag", required=false) int newLockFlag) {
		return sheetDealHandler.hangup(sheetId,comment,flag,holdStateId,newLockFlag) > 0 ? "SUCCESS" : "FAIL";
	}
	
	@PostMapping(value = "/workflow/ASheetDealHandler/unHangup")
	public String unHangup(@RequestBody String sheetPubInfo) {
		SheetPubInfo sheetInfo=(SheetPubInfo) JSONObject.toBean(JSONObject.fromObject(sheetPubInfo),SheetPubInfo.class);
		return sheetDealHandler.unHangup(sheetInfo);
	}
	
	@PostMapping(value = "/workflow/ASheetDealHandler/unHangupAndRelease")
	public String unHangupAndRelease(@RequestBody String sheetPubInfo) {
		SheetPubInfo sheetInfo=(SheetPubInfo) JSONObject.toBean(JSONObject.fromObject(sheetPubInfo),SheetPubInfo.class);
		return sheetDealHandler.unHangupAndRelease(sheetInfo);
	}
}