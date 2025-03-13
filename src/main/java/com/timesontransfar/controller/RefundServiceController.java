package com.timesontransfar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.refund.pojo.RefundPojo;
import com.timesontransfar.refund.service.IrefundService;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

@RestController
@RefreshScope
public class RefundServiceController {
	
	@Autowired
	private IrefundService refundService;
	
	@RequestMapping(value = "/workflow/dynamic/getRefund")
	public Object getRefund(@RequestBody(required=false) String parm) {
		return refundService.getRefund(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/updateRefund")
	public Object updateRefund(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		RefundPojo refund=(RefundPojo) JSONObject.toBean(json,RefundPojo.class);
		int updateRefund = refundService.updateRefund(refund);
		return ResultUtil.success(updateRefund);
	}
	@RequestMapping(value = "/workflow/dynamic/insertRefund")
	public Object insertRefund(@RequestBody(required=false) String parm) {
		JSONObject json=JSONObject.fromObject(parm);
		RefundPojo refund=(RefundPojo) JSONObject.toBean(json,RefundPojo.class);
		String insertRefund = refundService.insertRefund(refund);
		return ResultUtil.success(insertRefund);
	}
}
