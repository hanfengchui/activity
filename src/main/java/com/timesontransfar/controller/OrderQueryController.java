package com.timesontransfar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.pubWebservice.WsKfServiceOrderQuery;

@RestController
public class OrderQueryController {
	
	@Autowired
	private WsKfServiceOrderQuery wskfQuery;

	@PostMapping(value = "/workflow/wskfserviceorder/query")
	public String query(@RequestBody String xml) {
		return wskfQuery.query(xml);
	}
	
	@PostMapping(value = "/workflow/wskfserviceorder/queryOrder")
	public String queryOrder(@RequestBody String json) {
		return wskfQuery.queryOrder(json);
	}
}