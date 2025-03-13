package com.timesontransfar.complaintservice.service;

import java.util.List;

import com.timesontransfar.customservice.orderask.pojo.ComplaintInfo;
import com.timesontransfar.customservice.orderask.pojo.ComplaintInfoError;

public interface IComplaint {
	public List<ComplaintInfo> complaintGetInfo(String xCtgRequestId, String accNbr, String custNumber,
			String complaintOrderId, String contractPhoneNbr, String beginDate, String endDate, String regionCode,
			String sysSource);

	public ComplaintInfoError complaintRemind(String xCtgRequestId, String complaintOrderId, String sysSource);

	public ComplaintInfoError complaintEval(String xCtgRequestId, String complaintOrderId, String custEvalPoint,
			String evalchannelNbr, String sysSource);

	public ComplaintInfoError complaintCancel(String xCtgRequestId, String complaintOrderId, String sysSource);

	public List<ComplaintInfo> travelCodeQry(String xCtgRequestId, String accNbr, String regionCode, String sysSource, String maxNum);

	public String complaintPostInfo(int step, String orderId);

	public String businessStatusSend(String serviceOrderId, String newOrderId, String businessId, String businessStatus,
			String expectFinishDate);
}