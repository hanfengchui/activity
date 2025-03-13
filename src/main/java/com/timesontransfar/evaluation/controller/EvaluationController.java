package com.timesontransfar.evaluation.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.evaluation.service.EvaluationService;

@RestController
public class EvaluationController {
	
	@Autowired
	private EvaluationService service;

	@PostMapping(value = "/workflow/evaluation/dealResultList", produces = "application/json;charset=utf-8", consumes = "application/json;charset=utf-8")
	public String dealResultList(@RequestBody String jsonStr) {
		return service.dealResultList(jsonStr);
	}
	
	@PostMapping(value = "/workflow/evaluation/dealSatisfyOverTime")
	public int dealSatisfyOverTime(@RequestParam(value="orderId", required=true) String orderId) {
		return service.dealSatisfyOverTime(orderId);
	}

	@PostMapping(value = "/workflow/evaluation/createPerceptionOrder")
	public String createPerceptionOrder(@RequestBody String jsonStr) {
		return service.createPerceptionOrder(jsonStr);
	}



}
