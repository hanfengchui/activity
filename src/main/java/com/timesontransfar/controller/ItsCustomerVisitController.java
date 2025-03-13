package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.worksheet.dao.ItsCustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;

@RestController
@RefreshScope
public class ItsCustomerVisitController {
	protected static Logger log = LoggerFactory.getLogger(ItsCustomerVisitController.class);
	
	@Autowired
	private ItsCustomerVisit customerVisit;
	
	@RequestMapping(value = "/workflow/itsCustomerVisit/saveCustomerVisit", method = RequestMethod.POST)
	public int saveCustomerVisit(@RequestBody  TScustomerVisit bean) {
		return customerVisit.saveCustomerVisit(bean);
	}

	@RequestMapping(value = "/workflow/itsCustomerVisit/saveCustomerVisitTmp", method = RequestMethod.POST)
	public int saveCustomerVisitTmp(@RequestBody TScustomerVisit bean) {
		return customerVisit.saveCustomerVisitTmp(bean);
	}
	
	@RequestMapping(value = "/workflow/itsCustomerVisit/saveCustomerVisitHis", method = RequestMethod.POST)
	public int saveCustomerVisitHis(@RequestParam(value="servid", required=true) String servid,
			@RequestParam(value="sheetid", required=true)String sheetid , @RequestParam(value="region", required=true)int region) {
		return customerVisit.saveCustomerVisitHis(servid, sheetid, region);
	}
	
	@RequestMapping(value = "/workflow/itsCustomerVisit/getCustomerVisitObj", method = RequestMethod.POST)
	public TScustomerVisit getCustomerVisitObj(
			@RequestParam(value="sheetId", required=true)String sheetId,
			@RequestParam(value="regionId", required=true)int regionId,
			@RequestParam(value="serboovid", required=true)boolean boo) {
		return customerVisit.getCustomerVisitObj(sheetId, regionId, boo);
	}
	
	@RequestMapping(value = "/workflow/itsCustomerVisit/getCustomerVisitTmpObj", method = RequestMethod.POST)
	public TScustomerVisit getCustomerVisitTmpObj(@RequestParam(value="sheetId", required=true)String sheetId,@RequestParam(value="regionId", required=true)int regionId) {
		return customerVisit.getCustomerVisitTmpObj(sheetId, regionId);
	}
	
	@RequestMapping(value = "/workflow/itsCustomerVisit/deleteCustomerVisit", method = RequestMethod.POST)
	public int deleteCustomerVisit(@RequestParam(value="serviceOrderId", required=true)String serviceOrderId ,
			@RequestParam(value="workSheetId", required=true)String workSheetId,@RequestParam(value="region", required=true) int region) {
		return customerVisit.deleteCustomerVisit(serviceOrderId, workSheetId, region);
	}
	
	@RequestMapping(value = "/workflow/itsCustomerVisit/deleteCustomerVisitTmp", method = RequestMethod.POST)
	public int deleteCustomerVisitTmp(@RequestParam(value="serviceOrderId", required=true)String serviceOrderId,@RequestParam(value="region", required=true) int region) {
		return customerVisit.deleteCustomerVisitTmp(serviceOrderId, region);
	}
}