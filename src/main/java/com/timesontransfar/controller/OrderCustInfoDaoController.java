package com.timesontransfar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;

@RestController
public class OrderCustInfoDaoController {
	
	@Autowired
	private IorderCustInfoDao orderCustInfoDao;
	
	@PostMapping(value = "/workflow/orderCustInfoDao/saveOrderCust")
	public int saveOrderCust(@RequestBody OrderCustomerInfo orderCust) {
		return orderCustInfoDao.saveOrderCust(orderCust);
	}
	
	@PostMapping(value = "/workflow/orderCustInfoDao/saveOrderCustHis")
	public int saveOrderCustHis(@RequestParam(value="currentCustGuid", required=true)String currentCustGuid, @RequestParam(value="month", required=true) Integer month) {
		return orderCustInfoDao.saveOrderCustHis(currentCustGuid, month);
	}
	
	@PostMapping(value = "/workflow/orderCustInfoDao/getOrderCustByGuid")
	public OrderCustomerInfo getOrderCustByGuid(@RequestParam(value="custGuid", required=true)String custGuid,@RequestParam(value="hisFlag", required=true) boolean hisFlag) {
		return orderCustInfoDao.getOrderCustByGuid(custGuid, hisFlag);
	}
	
	@PostMapping(value = "/workflow/orderCustInfoDao/getOrderCustByOrderId")
	public OrderCustomerInfo getOrderCustByOrderId(@RequestParam(value="orderId", required=true)String orderId) {
		return orderCustInfoDao.getOrderCustByOrderId(orderId);
	}
	
	@PostMapping(value = "/workflow/orderCustInfoDao/delOrderCustInfo")
	public int delOrderCustInfo(@RequestParam(value="custGuid", required=true)String custGuid,@RequestParam(value="month", required=true)Integer month) {
		return orderCustInfoDao.delOrderCustInfo(custGuid, month);
	}
	
	@PostMapping(value = "/workflow/orderCustInfoDao/updateCustInfo")
	public int updateCustInfo(@RequestBody OrderCustomerInfo custInfo) {
		return orderCustInfoDao.updateCustInfo(custInfo);
	}
	
}