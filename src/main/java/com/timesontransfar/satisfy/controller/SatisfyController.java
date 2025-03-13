package com.timesontransfar.satisfy.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.satisfy.service.SatisfyService;
import com.transfar.common.web.ResultUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SatisfyController {
	private static Logger log = LoggerFactory.getLogger(SatisfyController.class);

    @Autowired
    private SatisfyService satisfy;

    @RequestMapping(value = "/workflow/dynamic/getSatisfyPoolList")
    public Object getSheetPoolList(@RequestBody(required=false) String param) {
    	log.info("getSheetPoolList 参数: {}", param);
    	return satisfy.getSheetPoolList(param);
    }

    @RequestMapping(value = "/workflow/dynamic/getMyTaskList")
    public Object getMyTaskList(@RequestBody(required=false) String param) {
    	log.info("getMyTaskList 参数: {}", param);
    	return satisfy.getMyTaskList(param);
    }
    
    @RequestMapping(value = "/workflow/dynamic/getReturnPool")
    public Object getReturnPool(@RequestBody(required=false) String param) {
    	log.info("getReturnPool 参数: {}", param);
    	return satisfy.getReturnPool(param);
    }
    
    @RequestMapping(value = "/workflow/dynamic/fetchBatchWorkSheet")
    public Object fetchBatchWorkSheet(@RequestBody(required=false) String param) {
    	log.info("fetchBatchWorkSheet 参数: {}", param);
    	return satisfy.fetchBatchWorkSheet(param);
    }

    @RequestMapping(value = "/workflow/dynamic/allotBatchWorkSheet")
    public Object allotBatchWorkSheet(@RequestBody(required=false) String param) {
        return satisfy.allotBatchWorkSheet(param);
    }

    @RequestMapping(value = "/workflow/dynamic/satisfyDispatchAssignSheet")
    public Object dispatchAssignSheet(@RequestBody(required=false) String param) {
    	String result = satisfy.dispatchAssignSheet(param);
    	if("SUCCESS".equals(result)) {
    		return ResultUtil.success();
    	}
        return ResultUtil.error(result);
    }
    
    @RequestMapping(value = "/workflow/dynamic/satisfySubmitAssignSheet")
    public Object submitAssignSheet(@RequestBody(required=false) String param) {
    	String result = satisfy.submitAssignSheet(param);
    	if("SUCCESS".equals(result)) {
    		return ResultUtil.success();
    	}
        return ResultUtil.error(result);
    }
    
    @RequestMapping(value = "/workflow/dynamic/satisfySubmitDealSheet")
    public Object submitDealSheet(@RequestBody(required=false) String param) {
    	String result = satisfy.submitDealSheet(param);
    	if("SUCCESS".equals(result)) {
    		return ResultUtil.success();
    	}
        return ResultUtil.error(result);
    }
    
    @RequestMapping(value = "/workflow/dynamic/satisfyDispatchDealSheet")
    public Object dispatchDealSheet(@RequestBody(required=false) String param) {
    	String result = satisfy.dispatchDealSheet(param);
    	if("SUCCESS".equals(result)) {
    		return ResultUtil.success();
    	}
        return ResultUtil.error(result);
    }
    
    @RequestMapping(value = "/workflow/dynamic/getLastResponseSheet")
    public Object getLastResponseSheet(@RequestBody(required=false) String param) {
        return satisfy.getLastResponseSheet(param);
    }

    @RequestMapping(value = "/workflow/dynamic/getSatisfyDealSheetFlow")
    public Object getSatisfyDealSheetFlow(@RequestBody(required=false) String param) {
        return satisfy.getDealSheetFlow(param);
    }
    
    @RequestMapping(value = "/workflow/dynamic/getUnSatisfyInfo")
    public Object getUnSatisfyInfo(@RequestBody(required=false) String param) {
    	JSONObject json = JSON.parseObject(param);
    	String orderId = json.getString("orderId");
    	String sheetId = json.getString("sheetId");
    	boolean hisFlag = json.getBoolean("hisFlag");
        return satisfy.getUnSatisfyInfo(orderId, hisFlag, sheetId);
    }

    @RequestMapping(value = "/workflow/dynamic/saveStageContent")
    public Object saveStageContent(@RequestBody(required=false) String param) {
        JSONObject json = JSON.parseObject(param);
        String orderId = json.getString("orderId");
        String sheetId = json.getString("sheetId");
        String content = json.getString("content");
        return satisfy.saveStageContent(sheetId, content, orderId);
    }

    @RequestMapping(value = "/workflow/dynamic/getEvaluationOrder")
    public Object getEvaluationOrder(@RequestBody(required=false) String param) {
    	JSONObject json = JSON.parseObject(param);
    	String orderId = json.getString("orderId");
        return satisfy.getEvaluationOrder(orderId);
    }
    
    @RequestMapping(value = "/workflow/dynamic/getOrderList")
    public Object getOrderList(@RequestBody(required=false) String param) {
    	log.info("getOrderList 参数: {}", param);
    	return satisfy.getOrderList(param);
    }
    
}
