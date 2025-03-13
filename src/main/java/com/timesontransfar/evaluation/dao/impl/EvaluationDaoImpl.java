package com.timesontransfar.evaluation.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.evaluation.SatisfyInfo;
import com.timesontransfar.evaluation.dao.EvaluationDao;

import java.util.List;
import java.util.Map;

@Component
public class EvaluationDaoImpl implements EvaluationDao {
	protected Logger log = LoggerFactory.getLogger(EvaluationDaoImpl.class);

	@Autowired
	private JdbcTemplate jt;
	
	public int updateSatisfyInfo(SatisfyInfo info) {
		try {
			String sqlStr = "update cc_satisfy_info "
					+ "set order_create_time = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), "
					+ "evaluate_status = ?, order_assess_id = ?, is_evaluation = ?, "
					+ "assess_mode = ?, assess_mode_desc = ?, "
					+ "join_mode = ?, join_mode_desc = ?, "
					+ "assess_send_time = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), "
	                + "user_subtime = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), "
	                + "issolve = ?, check_assess_result = ?, "
	                + "check_assess_score = ?, satisfied_reason = ?, create_flag = ?, "
	                + "grid = ?, channel_code = ?, channel_name = ?, province_assess_result = ? "
	                + "where service_order_id = ?";
	        return jt.update(sqlStr,
	        		info.getOrderCreateTime(),
	                info.getEvaluateStatus(),
	                info.getOrderAssessId(),
	                info.getIsEvaluation(),
	                info.getAssessMode(),
	                info.getAssessModeDesc(), 
	                info.getJoinMode(),
	                info.getJoinModeDesc(),
	                info.getAssessSendTime(),
	                info.getUserSubTime(),
	                info.getIsSolve(),
	                info.getCheckAssessResult(),
	                info.getCheckAssessScore(),
	                info.getSatisfiedReason(),
	                info.getCreateFlag(),
	                info.getGrid(),
	                info.getChannelCode(),
	                info.getChannelName(),
	                info.getProvinceAssessResult(),
	                info.getServiceOrderId());
		}
		catch(Exception e) {
			log.error("updateSatisfyInfo {} mysql异常: {}", info, e.getMessage(), e);
		}
		return 0;
	}
	
	public int insertSatisfyInfo(SatisfyInfo info) {
		try {
			String sqlStr = "INSERT INTO cc_satisfy_info(service_order_id, unified_complaint_code, "
					+ "contact_status, is_repeat, is_up_repeat, require_uninvited, work_sheet_id, "
					+ "deal_staff, deal_logonname, deal_staff_name, deal_org_id, deal_org_name, "
					+ "is_invited, evaluate_status, create_flag) "
					+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			return jt.update(sqlStr,
				info.getServiceOrderId(),
				StringUtils.defaultIfEmpty(info.getUnifiedComplaintCode(), null),
				info.getContactStatus(),
				info.getIsRepeat(),
				info.getIsUpRepeat(),
				info.getRequireUninvited(),
				info.getWorkSheetId(),
				info.getDealStaff(),
				info.getDealLogonname(),
				info.getDealStaffName(),
				info.getDealOrgId(),
				info.getDealOrgName(),
				info.getIsInvited(),
				info.getEvaluateStatus(),
				info.getCreateFlag()
			);
		}
		catch(Exception e) {
			log.error("insertSatisfyInfo {} mysql异常: {}", info, e.getMessage(), e);
		}
		return 0;
	}
	
	public int updateSatisfyOverTime(SatisfyInfo info) {
		try {
			String sqlStr = "update cc_satisfy_info "
					+ "set evaluate_status = ?, is_evaluation = ?, create_flag = ? "
	                + "where service_order_id = ?";
	        return jt.update(sqlStr,
	                info.getEvaluateStatus(),
	                info.getIsEvaluation(),
	                info.getCreateFlag(),
	                info.getServiceOrderId());
		}
		catch(Exception e) {
			log.error("updateSatisfyOverTime {} mysql异常: {}", info, e.getMessage(), e);
		}
		return 0;
	}
	
	public SatisfyInfo getSatisfyInfo(String orderId) {
		try {
			String strSql = "select c.unified_complaint_code unifiedComplaintCode from cc_satisfy_info c where c.service_order_id=?";
			
			List<Map<String, Object>> tmpList = jt.queryForList(strSql, orderId);
			if(tmpList.isEmpty()){
				log.error("没有查询到单号为: {} 的满意度数据", orderId);
				return null;
			}
			
			SatisfyInfo info = JSON.parseObject(JSON.toJSONString(tmpList.get(0)), SatisfyInfo.class);
			log.info("getSatisfyInfo orderId: {} {}", orderId, tmpList.get(0));
			return info;
		}
		catch(Exception e) {
			log.error("getSatisfyInfo orderId: {} 异常: {}", orderId, e.getMessage(), e);
		}
		return null;
	}

	public String getRequireUninvited(String orderId) {
		try {
			String strSql = "select c.require_uninvited requireUninvited from cc_satisfy_info c where c.service_order_id=?";
			String s = jt.queryForObject(strSql, new Object[]{orderId}, String.class);
			if(StringUtils.isBlank(s)){
				log.error("没有查询到单号为: {} 的满意度数据", orderId);
				return null;
			}
			log.info("getRequireUninvited orderId: {} {}", orderId, s);
			return s;
		}
		catch(Exception e) {
			log.error("getRequireUninvited orderId: {} 异常: {}", orderId, e.getMessage(), e);
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public String getBlackPhone(String relaInfo, String prodNum) {
		String result = "";
		String sqlStr = "select MOBILE_PHONE blackPhone from cc_return_blacklist where MOBILE_PHONE in (?,?) and STATE = '1'";
		List maps = jt.queryForList(sqlStr, relaInfo, prodNum);
		if (!maps.isEmpty()) {
			result = ((Map) maps.get(0)).get("blackPhone").toString();
		}
		log.info("getBlackPhone relaInfo: {} prodNum: {} blackPhone: {}", relaInfo, prodNum, result);
		return result;
	}
	
	public void saveReturnBlackLog(String orderId, String mobilePhone, String complaintPhone, String blackPhone, String satisfyDegree) {
		int num = 0;
		try {
			String insertLogSql = "insert into cc_return_blacklist_log (ORDER_ID,MOBILE_PHONE,COMPLAINT_PHONE,BLACKLIST_PHONE,SATISFY_DEGREE,CREATE_TIME)"
					+ " values(?,?,?,?,?,now())";
			num = jt.update(insertLogSql, orderId, mobilePhone, complaintPhone, blackPhone, satisfyDegree);
		} catch (Exception e) {
			log.error("saveReturnBlackLog error: {}", e.getMessage(), e);
		}
		log.info("saveReturnBlackLog result: {}", num);
	}
	
	public int insertPayReturnInfo(SatisfyInfo info) {
		try {
			String sqlStr = "INSERT INTO cc_payreturn_info(service_order_id, create_date, evaluate_status, order_assess_id, order_create_time, "
					+ "is_evaluation, assess_mode, assess_mode_desc, join_mode, join_mode_desc, "
					+ "assess_send_time, user_subtime, issolve, check_assess_result, check_assess_score, "
					+ "satisfied_reason, grid, channel_code, channel_name, province_assess_result) "
					+ "VALUES (?, NOW(), ?, ?, STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), "
					+ "?, ?, ?, ?, ?, "
					+ "STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), ?, ?, ?, "
					+ "?, ?, ?, ?, ?)";
			return jt.update(sqlStr,
				info.getServiceOrderId(),
				info.getEvaluateStatus(),
				info.getOrderAssessId(),
				info.getOrderCreateTime(),
                info.getIsEvaluation(),
                info.getAssessMode(),
                info.getAssessModeDesc(), 
                info.getJoinMode(),
                info.getJoinModeDesc(),
                info.getAssessSendTime(),
                info.getUserSubTime(),
                info.getIsSolve(),
                info.getCheckAssessResult(),
                info.getCheckAssessScore(),
                info.getSatisfiedReason(),
                info.getGrid(),
                info.getChannelCode(),
                info.getChannelName(),
                info.getProvinceAssessResult()
			);
		}
		catch(Exception e) {
			log.error("insertPayReturnInfo {} mysql异常: {}", info, e.getMessage(), e);
		}
		return 0;
	}

}