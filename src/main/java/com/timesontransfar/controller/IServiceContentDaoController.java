package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;

@RestController
@RefreshScope
public class IServiceContentDaoController {
	protected Logger log = LoggerFactory.getLogger(IServiceContentDaoController.class);
	
	@Autowired
	private IserviceContentDao serviceContentDao;
	
	@RequestMapping(value = "/workflow/serviceContentDao/checkPubReference", method = RequestMethod.POST)
	public int checkPubReference(@RequestParam(value="firstId", required=true) int firstId,@RequestParam(value="secendId", required=true) int secendId ) {
		return serviceContentDao.checkPubReference(firstId, secendId);
	}
	
	@RequestMapping(value = "/workflow/serviceContentDao/saveServContentHis", method = RequestMethod.POST)
	public int saveServContentHis(@RequestParam(value="hisFlag", required=true) String currentOrderId,@RequestParam(value="hisFlag", required=true) Integer month) {
		return serviceContentDao.saveServContentHis(currentOrderId, month);
	}
	
	@RequestMapping(value = "/workflow/serviceContentDao/getServContentByOrderId", method = RequestMethod.POST)
	public ServiceContent getServContentByOrderId(
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="hisFlag", required=true) boolean hisFlag,
			@RequestParam(value="version", required=true) int version) {
		return serviceContentDao.getServContentByOrderId(orderId, hisFlag, version);
	}
	
	@RequestMapping(value = "/workflow/serviceContentDao/getServContentByOrderIdNew", method = RequestMethod.POST)
	public ServiceContent getServContentByOrderIdNew(
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="suffix", required=true) String suffix,
			@RequestParam(value="hisFlag", required=true) boolean hisFlag,
			@RequestParam(value="version", required=true) int version) {
		return serviceContentDao.getServContentByOrderIdNew(orderId, suffix, hisFlag, version);
	}
	
	@RequestMapping(value = "/workflow/serviceContentDao/getServContentByOrderIdAndSuffix", method = RequestMethod.POST)
	public ServiceContent getServContentByOrderId(
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="suffix", required=true) String suffix, 
			@RequestParam(value="version", required=true)  int version) {
		return serviceContentDao.getServContentByOrderId(orderId, suffix, version);
	}
	
	@RequestMapping(value = "/workflow/serviceContentDao/delServContent", method = RequestMethod.POST)
	public int delServContent(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="month", required=true)Integer month) {
		return serviceContentDao.delServContent(orderId, month);
	}
	
	@RequestMapping(value = "/workflow/serviceContentDao/updateAccpContent", method = RequestMethod.POST)
	public int updateAccpContent(
			@RequestParam(value="id", required=true)String id,
			@RequestParam(value="content", required=true)String content,
			@RequestParam(value="month", required=true)Integer month) {
		return serviceContentDao.updateAccpContent(id, content, month);
	}
	
	@RequestMapping(value = "/workflow/serviceContentDao/updateAcceptContent", method = RequestMethod.POST)
	public int updateAcceptContent(
			@RequestParam(value="regionId", required=true)int regionId,
			@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="acceptContent", required=true)String acceptContent) {
		return serviceContentDao.updateAcceptContent(regionId, orderId, acceptContent);
	}

}