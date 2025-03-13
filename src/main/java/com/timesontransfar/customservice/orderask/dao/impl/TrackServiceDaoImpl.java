package com.timesontransfar.customservice.orderask.dao.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.timesontransfar.customservice.orderask.dao.ITrackServiceDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.pojo.TrackInfo;
import com.timesontransfar.customservice.worksheet.pojo.RefundModeRecord;

@Service
@SuppressWarnings("rawtypes")
public class TrackServiceDaoImpl implements ITrackServiceDao{
	private static final Logger logger = LoggerFactory.getLogger(TrackServiceDaoImpl.class);
	
    @Autowired
    private JdbcTemplate jt;
	
	public int saveTrackInfoTz(TrackInfo info) {
		String strSql =  "insert into cc_service_track_tz (NEW_ORDER_ID, OLD_ORDER_ID, TRACK_TYPE, CREATE_TYPE, CREATE_DATE, REFUND_MODE, REFUND_MODE_DESC) " + 
				"values (?, ?, ?, ?, STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), ?, ?)";
		return jt.update(strSql,
				info.getNewOrderId(),
				info.getOldOrderId(),
				info.getTrackType(),
				info.getCreateType(),
				info.getCreateDate(),
				info.getRefundMode(),
				StringUtils.defaultIfEmpty(info.getRefundModeDesc(), null)
	        );
	}
	
	public int saveRefundOrder(TrackInfo info, String refundData, String refundsAccNum, String refundAmount, String prmRefundAmount) {
		int num = 0;
		try {
			String strSql = "INSERT INTO cc_order_refund"
					+ "(TRACK_ORDER_ID, SERVICE_ORDER_ID, CREATE_DATE, REFUND_DATA, REFUND_STATUS, AUTO_STATUS, REFUND_NUM, REFUND_AMOUNT, PRM_REFUND_AMOUNT, ORDER_STATUS) "
					+ "VALUES(?, ?, now(), ?, 1, 1, ?, ?, ?, 0)";
			num = jt.update(strSql, info.getNewOrderId(), info.getOldOrderId(), refundData, StringUtils.substring(refundsAccNum, 0, 20), StringUtils.substring(refundAmount, 0, 20), prmRefundAmount);
		} catch(Exception e) {
			logger.error("saveRefundOrder error: {}", e.getMessage(), e);
		}
		logger.info("saveRefundOrder orderId: {} num: {}", info.getNewOrderId(), num);
		return num;
	}
	
	public Map getTrackInfo(String orderId) {
        String strsql = "SELECT * FROM cc_service_track_tz c WHERE c.NEW_ORDER_ID = ?";
        List tmpList = jt.queryForList(strsql, orderId);
        if(!tmpList.isEmpty()) {
        	return (Map) tmpList.get(0);
        }
        return null;
	}
	
	
	public int modifyRefundMode(RefundModeRecord r) {
		String strSql = "insert into cc_refund_mode_record (SERVICE_ORDER_ID, OLD_MODE, OLD_MODE_DESC, NEW_MODE, NEW_MODE_DESC, MODIFY_STAFF_ID, MODIFY_DATE) " + 
				"values (?, ?, ?, ?, ?, ?, NOW())";
		return jt.update(strSql, 
				r.getServiceOrderId(),
				r.getOldMode(),
				r.getOldModeDesc(),
				r.getNewMode(),
				r.getNewModeDesc(),
				r.getModifyStaffId()
		);
	}
	
	
	public int modifyTrackInfo(TrackInfo o) {
		String strSql = "update CC_SERVICE_TRACK_TZ c set c.refund_mode=?, c.refund_mode_desc=?, c.deal_mode=?, " + 
						"c.REFUND_REASON=?, c.REFUND_REASON_DESC=?, c.REFUND_AMOUNT=?, c.REFUND_CONTENT=?, c.PAID_REASON=?, " + 
						"c.PAID_REASON_DESC=?,c.PAID_AMOUNT=?, c.PAID_CONTENT=?, c.OP_STAFF=?, c.RECHARGE_MODE=?, c.RECHARGE_MODE_DESC=?, " + 
						"c.RECHARGE_NUMBER=?, c.REFUND_NUMBER=?, c.APPROVE_PERSON=?, c.OWNER_NAME=?, c.FORM_DATE=NOW() " + 
						"where c.new_order_id = ?";
		return jt.update(strSql,
				o.getRefundMode(),
				StringUtils.defaultIfEmpty(o.getRefundModeDesc(), null),
				StringUtils.defaultIfEmpty(o.getDealMode(), null),
				StringUtils.defaultIfEmpty(o.getRefundReason(), null),
				StringUtils.defaultIfEmpty(o.getRefundReasonDesc(), null),
				o.getRefundAmount(),
				StringUtils.defaultIfEmpty(o.getRefundContent(), null),
				StringUtils.defaultIfEmpty(o.getPaidReason(), null),
				StringUtils.defaultIfEmpty(o.getPaidReasonDesc(), null),
				o.getPaidAmount(),
				StringUtils.defaultIfEmpty(o.getPaidContent(), null),
				StringUtils.defaultIfEmpty(o.getOpStaff(), null),
				StringUtils.defaultIfEmpty(o.getRechargeMode(), null),
				StringUtils.defaultIfEmpty(o.getRechargeModeDesc(), null),
				StringUtils.defaultIfEmpty(o.getRechargeNumber(), null),
				StringUtils.defaultIfEmpty(o.getRefundNumber(), null),
				StringUtils.defaultIfEmpty(o.getApprovePerson(), null),
				StringUtils.defaultIfEmpty(o.getOwnerName(), null),
				StringUtils.defaultIfEmpty(o.getNewOrderId(), null)
	        );
	}
	
	public Map getComplaintOrderType(int keyId, int hotId) {
		String sql = "select * from ccs_hotpoint_key c where c.KEY_ID = ? and c.HOT_ID = ?";
		List tmpList = jt.queryForList(sql, keyId, hotId);
        if(!tmpList.isEmpty()) {
        	return (Map) tmpList.get(0);
        }
		return Collections.emptyMap();
	}
	
	public int saveComplaintOrderDeatail(Map<String, String> tmpMap) {
		try {
			String strSql = "insert into CC_COMPLAINT_ORDER_DETAIL(COMPLAINT_ORDER_SN,TAG_CODE,TAG_NAME,CUST_PURPOSE,"
					+ "CUST_PHONE_NO,CUST_ACC_NUMBER,CUST_ADDRESS,CUST_NAME,ID_CARD_NO,COMPLAINT_ORDER_TIME,ASSIST_STAFF_NO,"
					+ "ASSIST_STAFF_NAME,ASSIST_STAFF_PHONE_NO,ASSIST_STAFF_CHANNEL,REMARK,CREATE_DATE,ORDER_STATUS,MODIFY_DATE) "
					+ "VALUE (?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?, now(), 0, now())";
			return jt.update(strSql, 
					tmpMap.get("complaintOrderSn"),
					tmpMap.get("tagCode"),
					tmpMap.get("tagName"),
					tmpMap.get("custPurpose"),
					tmpMap.get("custPhoneNo"),
					tmpMap.get("custAccNumber"),
					tmpMap.get("custAddress"),
					tmpMap.get("custName"),
					tmpMap.get("idCardNo"),
					tmpMap.get("assistStaffNo"),
					tmpMap.get("assistStaffName"),
					tmpMap.get("assistStaffPhoneNo"),
					tmpMap.get("assistStaffChannel"),
					tmpMap.get("remark"));
		}catch(Exception e) {
			logger.error("saveComplaintOrderDeatail error: {}", e.getMessage(), e);
		}
		return 0;
	}
	
	public int saveComplaintOfferInfo(String orderId, int regionId, String prodNum, String miitCode, String thirdLevel, Integer crmFlag, int status) {
		int num = 0;
		try {
			String strSql = "INSERT INTO cc_complaint_offer_info (SERVICE_ORDER_ID, CREATE_DATE, REGION_ID, PROD_NUM, MIIT_CODE, THIRD_LEVEL, CRM_FLAG, STATUS, MODIFY_DATE) "
					+ "VALUES (?, now(), ?, ?, ?, ?, ?, ?, now())";
			num = jt.update(strSql, orderId, regionId, prodNum, miitCode, thirdLevel, crmFlag, status);
		}catch(Exception e) {
			logger.error("saveComplaintOfferInfo orderId: {} error: {}", orderId, e.getMessage(), e);
		}
		logger.info("saveComplaintOfferInfo orderId: {} num: {}", orderId, num);
		return num;
	}
	
	/**
	 * 查询该产品号码通过原单生成的调账跟踪单量
	 */
	public int qryRepeatOrderByProdNum(String oldOrderId, String prodNum) {
		String strSql = "select count(1) from ( "
				+ "select c.SERVICE_ORDER_ID,c.PROD_NUM from cc_service_order_ask c where c.SERVICE_ORDER_ID in "
				+ "(select c.NEW_ORDER_ID from cc_service_track_tz c where c.OLD_ORDER_ID = ? and c.TRACK_TYPE = 1) "
				+ "union "
				+ "select c.SERVICE_ORDER_ID,c.PROD_NUM from cc_service_order_ask_his c where c.SERVICE_ORDER_ID in "
				+ "(select c.NEW_ORDER_ID from cc_service_track_tz c where c.OLD_ORDER_ID = ? and c.TRACK_TYPE = 1)) as rt "
				+ "where PROD_NUM = ?";
		return jt.queryForObject(strSql, new Object[] { oldOrderId, oldOrderId, prodNum }, Integer.class);
	}
	
    public int isExistRefundOrder(String orderId) {
        String strSql = "select count(1) from cc_service_track_tz c where c.OLD_ORDER_ID=? and c.TRACK_TYPE=1";
        return jt.queryForObject(strSql, new Object[]{orderId}, Integer.class);
    }
	
	/**
	 * 保存一键录单信息
	 * @param loginId
	 * @param info
	 * @return
	 */
	public int saveAcceptOrderInfo(String loginId, OrderAskInfo info) {
		int num = 0;
		try {
			String strSql = "INSERT INTO cc_accept_order_info (LOGIN_ID, SERVICE_ORDER_ID, REGION_ID, SERVICE_TYPE, PROD_NUM, ACCEPT_STAFF_ID, CREATE_TIME) "
					+ "VALUES (?, ?, ?, ?, ?, ?, now())";
			num = jt.update(strSql, loginId, info.getServOrderId(), info.getRegionId(), info.getServType(), info.getProdNum(), info.getAskStaffId());
		}catch(Exception e) {
			logger.error("saveAcceptOrderInfo orderId: {} error: {}", info.getServOrderId(), e.getMessage(), e);
		}
		logger.info("saveAcceptOrderInfo orderId: {} num: {}", info.getServOrderId(), num);
		return num;
	}
	
	/**
	 * 保存投诉处理多号码退费信息
	 * @param trackOrderId
	 * @param orderId
	 * @param refundNum
	 * @param prmRefundAmount
	 * @param prmRefundSumAmount
	 * @return
	 */
	public int saveMultiProdRefundInfo(String trackOrderId, String orderId, String refundNum, String prmRefundAmount, String prmRefundSumAmount) {
		int num = 0;
		try {
			String strSql = "INSERT INTO cc_multiprod_refund "
					+ "(TRACK_ORDER_ID, SERVICE_ORDER_ID, REFUND_NUM, PRM_REFUND_AMOUNT, PRM_REFUND_TL_AMOUNT, CREATE_DATE) "
					+ "VALUES (?, ?, ?, ?, ?, now())";
			num = jt.update(strSql, trackOrderId, orderId, refundNum, prmRefundAmount, prmRefundSumAmount);
		} catch(Exception e) {
			logger.error("saveMultiProdRefundInfo error: {}", e.getMessage(), e);
		}
		logger.info("saveMultiProdRefundInfo trackOrderId: {} prmRefundAmount: {} num: {}", trackOrderId, prmRefundAmount, num);
		return num;
	}
	
	/**
	 * 更新审批记录状态
	 */
	public int updateApprovedRefundInfo(String curSheetId, int state, String operStaff) {
		int num = 0;
		try {
			String strSql = "update cc_refund_approve_info set SHEET_STATE = ?, OPER_DATE = now(), OPER_STAFF = ?, MODIFY_DATE = now() "
					+ "where CUR_XC_SHEET_ID = ?";
			num = jt.update(strSql, state, operStaff, curSheetId);
		} catch(Exception e) {
			logger.error("updateApprovedRefundInfo error: {}", e.getMessage(), e);
		}
		logger.info("updateApprovedRefundInfo curSheetId: {} state: {} num: {}", curSheetId, state, num);
		return num;
	}
}
