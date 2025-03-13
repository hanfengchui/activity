package com.timesontransfar.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.customservice.worksheet.service.OrderRefundService;
import com.timesontransfar.customservice.worksheet.service.RefundTrackService;
import com.transfar.common.web.ResultUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderRefundController {
	protected Logger log = LoggerFactory.getLogger(OrderRefundController.class);

	@Autowired
	private OrderRefundService orderRefundService;
	
	@Autowired
	private RefundTrackService refundTrack;

	//获取退款信息
	@PostMapping(value = "/workflow/rechage/getRechageMsg")
	public JSONObject getRechageMsg(@RequestParam(value="orderId", required=true)String orderId) {
		log.info("getRechageMsg 入参: {}", orderId);
		JSONObject rechageMsg = orderRefundService.getOrderRefund(orderId);
		log.info("getRechageMsg 回参: {}", rechageMsg.toJSONString());
		if(rechageMsg.isEmpty()) {
			return null;
		}
		return rechageMsg;
	}

	//审核功能,更新数据，并且根据审核结果决定是否退费
	@PostMapping(value = "/workflow/rechage/checkAndRechage")
	public JSONObject checkAndRechage(@RequestParam(value="tuiRason", required=true)String tuiRason,
									  @RequestParam(value="tuiRasonDesc", required=true)String tuiRasonDesc,
									  @RequestParam(value="refundStatus", required=true)String refundStatus,
									  @RequestParam(value="orderId", required=true)String orderId,
									  @RequestParam(value="loginName", required=true)String loginName) {
		log.info("checkAndRechage 入参: orderId={} loginName={} refundStatus={} tuiRason={}", orderId, loginName, refundStatus, tuiRason);
		JSONObject rechageMsg = orderRefundService.checkAndRechage(tuiRason,tuiRasonDesc,refundStatus,orderId,loginName);
		log.info("checkAndRechage 回参: {}", rechageMsg.toJSONString());
		return rechageMsg;
	}
	
	@PostMapping(value = "/workflow/rechage/auditTrackOrder", produces = "application/json;charset=utf-8")
	public String auditTrackOrder(@RequestParam(value="orderId", required=true)String orderId) {
		log.info("auditTrackOrder orderId: {}", orderId);
		return ResultUtil.success(orderRefundService.auditTrackOrder(orderId));
	}
	
	@PostMapping(value = "/workflow/rechage/autoFinishRefundOrder", consumes = "application/json;charset=utf-8", produces = "application/json;charset=utf-8")
	public String autoFinishRefundOrder(@RequestBody String reqJson) {
		return ResultUtil.success(refundTrack.autoFinishRefundOrder(reqJson));
	}
	
	@RequestMapping(value = "/workflow/dynamic/getDispatchStaff")
	public Object getDispatchStaff(@RequestBody(required=false) String param) {
		log.info("getDispatchStaff param: {}", param);
		JSONObject json = JSON.parseObject(param);
		String orgId = json.getString("acceptOrgId");
		return ResultUtil.success(orderRefundService.getDispatchStaff(orgId));
	}
	
	@RequestMapping(value = "/workflow/dynamic/isDispatchStaff")
	public Object isDispatchStaff(@RequestBody(required=false) String param) {
		log.info("isDispatchStaff param: {}", param);
		JSONObject json = JSON.parseObject(param);
		String loginName = json.getString("loginName");
		return ResultUtil.success(orderRefundService.isDispatchStaff(loginName));
	}
	
	@RequestMapping(value = "/workflow/dynamic/getRefundApproveCount")
	public Object getRefundApproveCount(@RequestBody(required=false) String param) {
		log.info("getRefundApproveCount param: {}", param);
		JSONObject json = JSON.parseObject(param);
		String orderId = json.getString("orderId");
		return ResultUtil.success(orderRefundService.getRefundApproveCount(orderId));
	}
	
	@RequestMapping(value = "/workflow/dynamic/getRefundApproveInfo")
	public Object getRefundApproveInfo(@RequestBody(required=false) String param) {
		log.info("getRefundApproveInfo param: {}", param);
		JSONObject json = JSON.parseObject(param);
		String sheetId = json.getString("sheetId");
		return orderRefundService.getRefundApproveInfo(sheetId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getApprovedRefundInfo")
	public Object getApprovedRefundInfo(@RequestBody(required=false) String param) {
		log.info("getApprovedRefundInfo param: {}", param);
		JSONObject json = JSON.parseObject(param);
		String orderId = json.getString("orderId");
		return orderRefundService.getApprovedRefundInfo(orderId, 1);
	}
	
	@RequestMapping(value = "/workflow/dynamic/quitApproveInfo")
	public Object quitApproveInfo(@RequestBody(required=false) String param) {
		log.info("quitApproveInfo param: {}", param);
		JSONObject json = JSON.parseObject(param);
		String sheetId = json.getString("sheetId");
		return ResultUtil.success(orderRefundService.quitApproveInfo(sheetId));
	}
	
}
