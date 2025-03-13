package com.timesontransfar.dapd.service;

import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.dapd.pojo.DapdSheetInfo;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.orderask.pojo.CustomerPersona;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;

public interface IdapdSheetInfoService {
	public void submitDapdSheetInfo(ServiceOrderInfo soInfo, ServiceContentSave[] saves, ServiceLabel label,
			CustomerPersona persona, ComplaintInfo compInfo);

	public void modifyDapd(OrderAskInfo order, OrderCustomerInfo cust, ServiceContent content,
			ServiceContentSave[] saves, String acceptContent);

	public void modifyDapdFlag(ServiceLabel label, CustomerPersona persona);

	public void modifyDapdComp(ComplaintInfo compInfo);

	public void setDealTemplate(DapdSheetInfo dapd);

	public void setDapdEndDate(String orderId);

	public void setDapdSatEval(String orderId, String satEval);

	public void setDapdArchiveDate(String orderId);

	public DapdSheetInfo getDapdSheetInfo(String orderId);
}