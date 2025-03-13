package com.timesontransfar.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.orderask.service.IworkSheetToITSMWebService;

import net.sf.json.JSONObject;

@RestController
@RefreshScope
public class WorkSheetToITSMWebServiceController {
	protected Logger log = LoggerFactory.getLogger(WorkSheetToITSMWebServiceController.class);
	@Autowired
	private IworkSheetToITSMWebService workSheetToITSMWebService;
	
	@RequestMapping(value = "/workflow/dynamic/getITSMList")
	public Object getITSMList(@RequestBody(required = false) String parm) {
		return workSheetToITSMWebService.getITSMList(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/reExecuteXML")
	public Object reExecuteXML(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		String[] reExecuteXML = workSheetToITSMWebService.reExecuteXML(obj.optString("workSheetId"));
		return reExecuteXML;
	}
}