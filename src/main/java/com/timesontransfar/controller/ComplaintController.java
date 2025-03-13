package com.timesontransfar.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.complaintservice.service.IComplaint;
import com.timesontransfar.customservice.orderask.pojo.ComplaintInfo;
import com.timesontransfar.customservice.orderask.pojo.ComplaintInfoError;
import com.transfar.common.aspect.annotation.LogInfo;

@RestController
@RefreshScope
@LogInfo
public class ComplaintController {
	@Autowired
	private IComplaint complaintImpl;

	@PostMapping(value = "/workflow/complaint/complaintGetInfo")
	public List<ComplaintInfo> complaintGetInfo(
			@RequestParam(value = "xCtgRequestId", required = true) String xCtgRequestId,
			@RequestParam(value = "accNbr", required = true) String accNbr,
			@RequestParam(value = "custNumber", required = true) String custNumber,
			@RequestParam(value = "complaintOrderId", required = true) String complaintOrderId,
			@RequestParam(value = "contractPhoneNbr", required = true) String contractPhoneNbr,
			@RequestParam(value = "beginDate", required = true) String beginDate,
			@RequestParam(value = "endDate", required = true) String endDate,
			@RequestParam(value = "regionCode", required = true) String regionCode,
			@RequestParam(value = "sysSource", required = true) String sysSource) {
		return complaintImpl.complaintGetInfo(xCtgRequestId, accNbr, custNumber, complaintOrderId,
				contractPhoneNbr, beginDate, endDate, regionCode, sysSource);
	}

	@PostMapping(value = "/workflow/complaint/complaintRemind")
	public ComplaintInfoError complaintRemind(
			@RequestParam(value = "xCtgRequestId", required = true) String xCtgRequestId,
			@RequestParam(value = "complaintOrderId", required = true) String complaintOrderId,
			@RequestParam(value = "sysSource", required = true) String sysSource) {
		return complaintImpl.complaintRemind(xCtgRequestId, complaintOrderId, sysSource);
	}

	@PostMapping(value = "/workflow/complaint/complaintEval")
	public ComplaintInfoError complaintEval(
			@RequestParam(value = "xCtgRequestId", required = true) String xCtgRequestId,
			@RequestParam(value = "complaintOrderId", required = true) String complaintOrderId,
			@RequestParam(value = "custEvalPoint", required = true) String custEvalPoint,
			@RequestParam(value = "evalchannelNbr", required = true) String evalchannelNbr,
			@RequestParam(value = "sysSource", required = true) String sysSource) {
		return complaintImpl.complaintEval(xCtgRequestId, complaintOrderId, custEvalPoint, evalchannelNbr,
				sysSource);
	}

	@PostMapping(value = "/workflow/complaint/complaintCancel")
	public ComplaintInfoError complaintCancel(
			@RequestParam(value = "xCtgRequestId", required = true) String xCtgRequestId,
			@RequestParam(value = "complaintOrderId", required = true) String complaintOrderId,
			@RequestParam(value = "sysSource", required = true) String sysSource) {
		return complaintImpl.complaintCancel(xCtgRequestId, complaintOrderId, sysSource);
	}

	@PostMapping(value = "/workflow/complaint/travelCodeQry")
	public List<ComplaintInfo> travelCodeQry(
			@RequestParam(value = "xCtgRequestId", required = true) String xCtgRequestId,
			@RequestParam(value = "accNbr", required = true) String accNbr,
			@RequestParam(value = "regionCode", required = true) String regionCode,
			@RequestParam(value = "sysSource", required = true) String sysSource,
			@RequestParam(value = "maxNum", required = true) String maxNum) {
		return complaintImpl.travelCodeQry(xCtgRequestId, accNbr, regionCode, sysSource, maxNum);
	}

	@PostMapping(value = "/workflow/complaint/businessStatusSend")
	public String businessStatusSend(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId,
			@RequestParam(value = "newOrderId", required = true) String newOrderId,
			@RequestParam(value = "businessId", required = true) String businessId,
			@RequestParam(value = "businessStatus", required = true) String businessStatus,
			@RequestParam(value = "expectFinishDate", required = true) String expectFinishDate) {
		return complaintImpl.businessStatusSend(serviceOrderId, newOrderId, businessId, businessStatus,
				expectFinishDate);
	}
}