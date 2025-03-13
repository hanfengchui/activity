package com.timesontransfar.customservice.worksheet.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public interface OrderRefundService {

	public JSONObject getOrderRefund(String orderId);

	public JSONObject checkAndRechage(String tuiRason, String tuiRasonDesc, String refundStatus,String orderId,String loginName);

	public JSONObject updateOrderRefund(String orderId, JSONObject refundInfo, String refundData, String refundsAccNum, String refundAmount, String prmRefundAmount);
	
	public int updatePrmRefundAmount(String prmRefundAmount, String orderId);
	
	public int updateArchiveStatus(String orderId);
	
	public String auditTrackOrder(String orderId);
	
	/**
	 * 跟踪单调账解挂
	 * @param orderId
	 * @return
	 */
	public int unHangupTrackOrder(String orderId);
	
	public int updateRechageStatus(String trackOrderId, String adjustFlag, String updateDate, String rechageStatus);
	
	/**
	 * 获取调账跟踪单协查班长员工
	 * @param acceptOrgId
	 * @return
	 */
	public String getDispatchStaff(String acceptOrgId);
	
	/**
	 * 判断是否是协查台长
	 * @return
	 */
	public boolean isDispatchStaff(String loginName);
	
	public int getRefundApproveCount(String orderId);
	
	public int saveRefundApproveInfo(String curSheetId, String mainSheetId, String orderId, String totalAmount, String refundDetail, String refundData);
	
	public List<Map<String, Object>> getRefundApproveInfo(String sheetId);
	
	public int updateRefundApproveInfo(String curSheetId, int state);
	
	public List<Map<String, Object>> getApprovedRefundInfo(String orderId, int state);
	
	public int quitApproveInfo(String curSheetId);

}
