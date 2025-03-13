package com.timesontransfar.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.customservice.orderask.service.ServiceOrderQuery;

@RestController
public class IQryOrderCountController {
	protected static Logger log = LoggerFactory.getLogger(IQryOrderCountController.class);
	
	@Autowired
	private IserviceOrderAsk serviceOrderAskImpl;
	
	@Autowired
	private ServiceOrderQuery orderQury;

	@PostMapping(value = "/qryOrderCount/getOrderCount")
	public String getOrderCount(@RequestParam(value = "staffId", required = true) String staffId) {
		return serviceOrderAskImpl.getOrderCount(staffId);
	}

	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/qryOrderCount/getRoleWork")
	public Map getRoleWork(@RequestParam(value = "staffId", required = true) String staffId) {
		return serviceOrderAskImpl.getRoleWork(staffId);
	}

	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/qryOrderCount/getWorkView")
	public Map getWorkView(@RequestParam(value = "staffId", required = true) String staffId) {
		return serviceOrderAskImpl.getWorkView(staffId);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/qryOrderCount/getStaffSheet")
	public Map getStaffSheet(@RequestParam(value = "staffId", required = true) String staffId,
								@RequestParam(value = "expiredDate", required = true) String expiredDate) {
		return orderQury.getStaffSheet(staffId,expiredDate);
	}
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/qryOrderCount/getViewStaff")
	public List getViewStaff(@RequestParam(value = "staffId", required = true) String staffId,
							 @RequestParam(value = "queryType", required = true) String queryType) {
		return orderQury.getViewStaff(staffId,queryType);
	}

}