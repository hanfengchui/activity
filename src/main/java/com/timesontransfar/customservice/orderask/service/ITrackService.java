package com.timesontransfar.customservice.orderask.service;

import java.util.Map;

import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;

import net.sf.json.JSONObject;

@SuppressWarnings("rawtypes")
public interface ITrackService {
	
	public String createTrackServiceTZ(String parm);
	
	public String createTrackService(String parm);
	
	public String saveReAssignReason(String parm);
	
	public Map getTrackInfo(String orderId);
	
	public String modifyTrackInfo(String parm, String dealStaff);
	
	public void syncComplaintOrder(OrderAskInfo orderAskInfo, ServiceContent servContent, OrderCustomerInfo custInfo);
	
	public String finishTrackOrder(String oldOrderId, String trackOrderId);
	
	public void analysisOrder(OrderAskInfo orderAskInfo, ServiceContent servContent, OrderCustomerInfo custInfo);
	
	public boolean isAnalysisOrder(int comeCategory, int phnTypeId, int bestOrder);
	
	/**
	 * 万象平台-根据手机号查询优惠券
	 * @param requestJson
	 * @param orderId
	 */
	public void getCouponByPhone(JSONObject requestJson, String orderId);
	
	/**
	 * 此单最严标识取消，打上标识“急”
	 */
	public int getUrgentOrderFlag(OrderAskInfo order, ServiceContent content);
	
	public void saveComplaintOfferInfo(boolean cliqueFlag, JSONObject culiqueJson, OrderAskInfo orderAskInfo, OrderCustomerInfo custInfo);
	
	public String autofinishTrackOrder(String trackOrderId, String dealContent, int reasonId, String dealStaff);
	
	public String createRefundTrackOrder(String parm);

	public String qryRepeatOrderByProdNum(String param);
	
	public String isExistRefundOrder(String param);
	
	/**
	 * 保存一键录单信息
	 * @param loginId
	 * @param info
	 * @return
	 */
	public void saveAcceptOrderInfo(String loginId, OrderAskInfo info);
	
	public boolean isSensitiveFlag();
	
	public String createMultiRefundTrackOrder(String param);
}
