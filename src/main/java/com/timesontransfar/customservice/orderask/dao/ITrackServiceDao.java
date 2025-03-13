package com.timesontransfar.customservice.orderask.dao;

import java.util.Map;

import com.timesontransfar.customservice.worksheet.pojo.TrackInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.pojo.RefundModeRecord;
@SuppressWarnings("rawtypes")
public interface ITrackServiceDao {

	/*
	 * 保存跟踪单信息
	 */
	public int saveTrackInfoTz(TrackInfo info);
	
	public int saveRefundOrder(TrackInfo info, String refundData, String refundsAccNum, String refundAmount, String prmRefundAmount);
	
	public Map getTrackInfo(String orderId);
	
	public int modifyRefundMode(RefundModeRecord r);
	
	public int modifyTrackInfo(TrackInfo o);
	
	public Map getComplaintOrderType(int keyId, int hotId);
	
	public int saveComplaintOrderDeatail(Map<String, String> tmpMap);
	
	public int saveComplaintOfferInfo(String orderId, int regionId, String prodNum, String miitCode, String thirdLevel, Integer crmFlag, int status);
	
	public int qryRepeatOrderByProdNum(String oldOrderId, String prodNum);
	
	public int isExistRefundOrder(String orderId);
	
	/**
	 * 保存一键录单信息
	 * @param loginId
	 * @param info
	 * @return
	 */
	public int saveAcceptOrderInfo(String loginId, OrderAskInfo info);
	
	public int saveMultiProdRefundInfo(String trackOrderId, String orderId, String refundNum, String prmRefundAmount, String prmRefundSumAmount);
	
	public int updateApprovedRefundInfo(String curSheetId, int state, String operStaff);

}
