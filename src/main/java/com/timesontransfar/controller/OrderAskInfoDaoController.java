package com.timesontransfar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.YNSJOrder;
import com.timesontransfar.customservice.orderask.pojo.YNSJResult;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

@RestController
public class OrderAskInfoDaoController {
	
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/saveOrderAskInfo", method = RequestMethod.POST)
	public int saveOrderAskInfo(@RequestBody OrderAskInfo order) {
		return orderAskInfoDao.saveOrderAskInfo(order);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/getOrderAskInfo", method = RequestMethod.POST)
	public OrderAskInfo getOrderAskInfo(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="hisFlag", required=true) boolean hisFlag) {
		return orderAskInfoDao.getOrderAskInfo(orderId, hisFlag);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/getOrderAskObj", method = RequestMethod.POST)
	public OrderAskInfo getOrderAskObj(@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="month", required=true)Integer month,@RequestParam(value="hisFlag", required=true)boolean hisFlag) {
		return orderAskInfoDao.getOrderAskObj(orderId, month, hisFlag);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/getOrderAskObjNew", method = RequestMethod.POST)
	public OrderAskInfo getOrderAskObjNew(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="hisFlag", required=true)boolean hisFlag,
			@RequestParam(value="statu", required=true)String statu) {
		return orderAskInfoDao.getOrderAskObjNew(orderId, hisFlag, statu);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/getOrderAskObjNewSuffix", method = RequestMethod.POST)
	public OrderAskInfo getOrderAskObjNewSuffix(
			@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="month", required=true)Integer month,
			@RequestParam(value="suffix", required=true)String suffix,
			@RequestParam(value="hisFlag", required=true)boolean hisFlag) {
		return orderAskInfoDao.getOrderAskObjNew(orderId, month, suffix, hisFlag);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/delOrderAskInfo", method = RequestMethod.POST)
	public int delOrderAskInfo(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="month", required=true)Integer month) {
		return orderAskInfoDao.delOrderAskInfo(orderId, month);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/updateOrderStatu", method = RequestMethod.POST)
	public boolean updateOrderStatu(
			@RequestParam(value="orderId", required=true)String orderId, 
			@RequestParam(value="statu", required=true)int statu,
			@RequestParam(value="month", required=true)Integer month,
			@RequestParam(value="orderStatuDesc", required=true)String orderStatuDesc) {
		return orderAskInfoDao.updateOrderStatu(orderId, statu, month, orderStatuDesc);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/updateOrderAskInfo", method = RequestMethod.POST)
	public int updateOrderAskInfo(@RequestBody OrderAskInfo orderAskInfo) {
		return orderAskInfoDao.updateOrderAskInfo(orderAskInfo);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/updateSubSheetHoldInfo", method = RequestMethod.POST)
	public int updateSubSheetHoldInfo(
			@RequestParam(value="orderId", required=true)String orderId, 
			@RequestParam(value="subSheetHoldNum", required=true)int subSheetHoldNum,
			@RequestParam(value="holdStrTime", required=true)String holdStrTime, 
			@RequestParam(value="holdSumTime", required=true)int holdSumTime) {
		return orderAskInfoDao.updateSubSheetHoldInfo(orderId, subSheetHoldNum, holdStrTime, holdSumTime);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/updateOrderFinishDate", method = RequestMethod.POST)
	public int updateOrderFinishDate(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="month", required=true)Integer month) {
		return orderAskInfoDao.updateOrderFinishDate(orderId, month);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/updateOrderAskCount", method = RequestMethod.POST)
	public int updateOrderAskCount(@RequestParam(value="orderId", required=true) String orderId,@RequestParam(value="count", required=true) int count) {
		return orderAskInfoDao.updateOrderAskCount(orderId, count);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/updateQualiative", method = RequestMethod.POST)
	public int updateQualiative(
			@RequestParam(value="ysQualiativeId", required=true)int ysQualiativeId,
			@RequestParam(value="ysQualiativeName", required=true) String ysQualiativeName,
			@RequestParam(value="month", required=true)Integer month,
			@RequestParam(value="orderId", required=true) String orderId) {
		return orderAskInfoDao.updateQualiative(ysQualiativeId, ysQualiativeName, month, orderId);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/updateFinTache", method = RequestMethod.POST)
	public int updateFinTache(
			@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="regionId", required=true)int regionId,
			@RequestParam(value="tacheId", required=true)int tacheId) {
		return orderAskInfoDao.updateFinTache(orderId, regionId, tacheId);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/getTsEspeciallyCust", method = RequestMethod.POST)
	public int getTsEspeciallyCust(@RequestParam(value="custNum", required=true)String custNum,@RequestParam(value="regionId", required=true)int regionId) {
		return orderAskInfoDao.getTsEspeciallyCust(custNum, regionId);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/getTsEspeciallyContent", method = RequestMethod.POST)
	public String getTsEspeciallyContent(@RequestParam(value="custNum", required=true)String custNum,@RequestParam(value="regionId", required=true)int regionId) {
		return orderAskInfoDao.getTsEspeciallyContent(custNum, regionId);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/saveYNSJOrder", method = RequestMethod.POST)
	public int saveYNSJOrder(@RequestBody YNSJOrder order) {
		return orderAskInfoDao.saveYNSJOrder(order);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/updateYNSJOrder", method = RequestMethod.POST)
	public int updateYNSJOrder(@RequestParam(value = "servOrderId", required = true) String servOrderId,
			@RequestParam(value = "actionResult", required = true) String actionResult) {
		return orderAskInfoDao.updateYNSJOrder(servOrderId, actionResult);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/updateBusinessOrder", method = RequestMethod.POST)
	public int updateBusinessOrder(@RequestParam(value = "servOrderId", required = true) String servOrderId,
			@RequestParam(value = "actionResult", required = true) String actionResult,
			@RequestParam(value = "orderId", required = true) String orderId) {
		return orderAskInfoDao.updateBusinessOrder(servOrderId, actionResult, orderId);
	}
	
	@RequestMapping(value = "/workflow/orderAskInfoDao/queryYNSJOrder", method = RequestMethod.POST)
	public YNSJOrder queryYNSJOrder(@RequestParam(value = "orderId", required = true) String orderId) {
		return orderAskInfoDao.queryYNSJOrder(orderId);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/saveYNSJResult", method = RequestMethod.POST)
	public int saveYNSJResult(@RequestBody YNSJResult result) {
		return orderAskInfoDao.saveYNSJResult(result);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/updateAcceptDate", method = RequestMethod.POST)
	public int updateAcceptDate(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return orderAskInfoDao.updateAcceptDate(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/selectAcceptDate", method = RequestMethod.POST)
	public String selectAcceptDate(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return orderAskInfoDao.selectAcceptDate(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/updateOrderLimitTime", method = RequestMethod.POST)
	public int updateOrderLimitTime(@RequestParam(value = "orderLimitTime", required = true) int orderLimitTime,
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return orderAskInfoDao.updateOrderLimitTime(orderLimitTime, serviceOrderId);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/insertServiceWisdomType", method = RequestMethod.POST)
	public int insertServiceWisdomType(@RequestParam(value = "orderIdOld", required = true) String orderIdOld,
			@RequestParam(value = "holdFlag", required = true) String holdFlag,
			@RequestParam(value = "sendFlag", required = true) String sendFlag) {
		return orderAskInfoDao.insertServiceWisdomType(orderIdOld, holdFlag, sendFlag);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/insertServiceWisdomTypeHis", method = RequestMethod.POST)
	public int insertServiceWisdomTypeHis(@RequestParam(value = "orderIdOld", required = true) String orderIdOld) {
		return orderAskInfoDao.insertServiceWisdomTypeHis(orderIdOld);
	}

	@RequestMapping(value = "/workflow/orderAskInfoDao/updateServiceWisdomType", method = RequestMethod.POST)
	public int updateServiceWisdomType(@RequestParam(value = "resultCode", required = true) String resultCode,
			@RequestParam(value = "resultMsg", required = true) String resultMsg,
			@RequestParam(value = "scenario", required = true) String scenario,
			@RequestParam(value = "wisdomType", required = true) String wisdomType,
			@RequestParam(value = "unifiedflag", required = true) String unifiedflag,
			@RequestParam(value = "orderIdNew", required = true) String orderIdNew,
			@RequestParam(value = "orderIdOld", required = true) String orderIdOld) {
		return orderAskInfoDao.updateServiceWisdomType(resultCode, resultMsg, scenario, wisdomType, unifiedflag,
				orderIdNew, orderIdOld);
	}

	@RequestMapping(value = "/workflow/dynamic/isZHYXBusinessOrder")
	public Object isZHYXBusinessOrder(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		boolean flag = orderAskInfoDao.isZHYXBusinessOrder(json.optString("orderId"));
		return ResultUtil.success(flag);
	}
	
	@RequestMapping(value = "/workflow/dynamic/isExistGZOrder")
	public Object isExistGZOrder(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		boolean flag = orderAskInfoDao.isExistGZOrder(json.optString("orderId"));
		return ResultUtil.success(flag);
	}
	
	@RequestMapping(value = "/workflow/dynamic/isBusinessOrder")
	public Object isBusinessOrder(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		return orderAskInfoDao.isBusinessOrder(json.optString("orderId"));
	}
	
	@RequestMapping(value = "/workflow/dynamic/getLastPreOrderId")
	public Object getLastPreOrderId(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		return orderAskInfoDao.getLastPreOrderId(json.optString("orderId"));
	}
	
	@RequestMapping(value = "/workflow/dynamic/getLastPreOrderInfo")
	public Object getLastPreOrderInfo(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		return orderAskInfoDao.getLastPreOrderInfo(json.optString("orderId"));
	}

	@RequestMapping(value = "/workflow/dynamic/getCompDealContent")
	public Object getCompDealContent(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		return orderAskInfoDao.getCompDealContent(json.optString("level"));
	}

	@RequestMapping(value = "/workflow/dynamic/querySummaryContent")
	public Object querySummaryContent(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		return orderAskInfoDao.querySummaryContent(json.optString("orderId"));
	}

	@RequestMapping(value = "/workflow/dynamic/getSummaryData")
	public Object getSummaryData(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		return orderAskInfoDao.getSummaryData(json.optString("orderId"));
	}
}