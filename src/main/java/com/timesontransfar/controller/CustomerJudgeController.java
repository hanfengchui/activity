package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.complaintservice.service.ICustomerJudgeJobService;

import net.sf.json.JSONObject;

@RestController
public class CustomerJudgeController {
	protected Logger log = LoggerFactory.getLogger(CustomerJudgeController.class);
	@Autowired
	private ICustomerJudgeJobService customerJudgeJobService;

	@PostMapping(value = "/workflow/complaintWorksheetDeal/enterJudgeJob")
	public String enterJudgeJob(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return customerJudgeJobService.enterJudgeJob(serviceOrderId);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/retrieveEvaluation", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
	public String retrieveEvaluation(@RequestBody String json) {
		JSONObject models = JSONObject.fromObject(json);
		String orderId = models.getString("serviceOrderId");
		String code = models.getString("assessCode");
		String msg = models.getString("assessMsg");
		String joinMode = models.getString("joinMode");
		return customerJudgeJobService.retrieveEvaluation(orderId, code, msg, joinMode);
	}

	@PostMapping(value = "/workflow/complaintWorksheetDeal/cmpAutoFinishBBAJob")
	public String cmpAutoFinishBBAJob(@RequestParam(value = "logonname", required = true) String logonname) {
		return customerJudgeJobService.cmpAutoFinishBBAJob(logonname);
	}
}