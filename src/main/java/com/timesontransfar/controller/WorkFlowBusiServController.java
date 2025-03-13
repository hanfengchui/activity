package com.timesontransfar.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.worksheet.service.IworkFlowBusiServ;

import net.sf.json.JSONObject;

@RestController
public class WorkFlowBusiServController {
	protected Logger log = LoggerFactory.getLogger(WorkFlowBusiServController.class);
	
	@Autowired
	private IworkFlowBusiServ workFlowBusiServImpl;
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/workFlowBusiServImpl/updateSheetFinish")
	public Map updateSheetFinish(@RequestBody Map inParam) {
		return workFlowBusiServImpl.updateSheetFinish(inParam);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/workFlowBusiServImpl/updateOrderDealState")
	public Map updateOrderDealState(@RequestBody Map inParam) {
		return workFlowBusiServImpl.updateOrderDealState(inParam);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/workFlowBusiServImpl/finishOrderAndSheet")
	public Map finishOrderAndSheet(@RequestParam(value="inParam", required=true)String inParam) {
		JSONObject models = JSONObject.fromObject(inParam);
		Map map = (Map)JSONObject.toBean(models.optJSONObject("map"),Map.class);
		return workFlowBusiServImpl.finishOrderAndSheet(map);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/workFlowBusiServImpl/crtWorkSheet")
	public Map crtWorkSheet(@RequestBody Map inParam) {
		return workFlowBusiServImpl.crtWorkSheet(inParam);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/workFlowBusiServImpl/getRouteInfo")
	public Map getRouteInfo(@RequestBody Map inParam) {
		return workFlowBusiServImpl.getRouteInfo(inParam);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/workFlowBusiServImpl/updateDealSheetRouteInfo")
	public Map updateDealSheetRouteInfo(@RequestBody Map inParam) {
		return workFlowBusiServImpl.updateDealSheetRouteInfo(inParam);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/workFlowBusiServImpl/updateAuitSheetFinish")
	public Map updateAuitSheetFinish(@RequestBody Map inParam) {
		return workFlowBusiServImpl.updateAuitSheetFinish(inParam);
	}
	
	@SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/workFlowBusiServImpl/crtAudWorkSheet")
	public Map crtAudWorkSheet(@RequestBody Map inParam) {
		return workFlowBusiServImpl.crtAudWorkSheet(inParam);
	}
	
}
