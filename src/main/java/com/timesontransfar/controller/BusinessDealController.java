package com.timesontransfar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.common.web.system.SyncBusinessResultImpl;

@RestController
public class BusinessDealController {
	
	@Autowired
	private SyncBusinessResultImpl impl;

	@PostMapping(value = "/workflow/businessDeal/dealBusinessResult")
	public String dealBusinessResult(@RequestBody String reqJson) {
		return impl.dealBusinessResult(reqJson);
	}
}