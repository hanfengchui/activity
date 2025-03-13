package com.timesontransfar.satisfy.dao.impl;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.evaluation.EvaluationDetail;
import com.timesontransfar.evaluation.EvaluationOrderPojo;
import com.timesontransfar.evaluation.pojo.*;

import com.timesontransfar.satisfy.dao.SatisfyDao;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Component
public class SatisfyDaoImpl implements SatisfyDao {
	private static final Logger log = LoggerFactory.getLogger(SatisfyDaoImpl.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private PubFunc pubFunc;

	public EvaluationSheetPojo getSheetObj(String sheetId, boolean hisFlag) {
		String strSql = "";
		if(hisFlag) {
			strSql = "SELECT c.lock_flag lockFlag,c.sheet_status sheetStatus FROM cc_unsatisfy_sheet_his c where c.work_sheet_id = ?";
		}
		else {
			strSql = "SELECT c.lock_flag lockFlag,c.sheet_status sheetStatus FROM cc_unsatisfy_sheet c where c.work_sheet_id = ?";
		}

		List<Map<String, Object>> tmpList = jdbcTemplate.queryForList(strSql, sheetId);
		if(tmpList.isEmpty()){
			log.warn("没有查询到单号为: {} 的工单，hisflag: {}", sheetId, hisFlag);
			return null;
		}		
		List<EvaluationSheetPojo> list = JSON.parseArray(JSON.toJSONString(tmpList), EvaluationSheetPojo.class);
		return list.get(0);
	}

	public EvaluationSheetPojo getLastResponseSheet(String orderId) {
		String sql = "SELECT t.contact_result_id contactResult," +
				" t.contact_result_desc resultDesc," +
				" t.unsatisfied_reason_id reason," +
				" t.unsatisfied_reason_desc reasonDesc," +
				" t.reason_detail_id detail," +
				" t.reason_detail_desc detailDesc," +
				" t.cust_advice_comfirm custAdvice," +
				" t.deal_scheme_id dealScheme," +
				" t.deal_scheme_desc schemeDesc," +
				" t.repair_result_id result," +
				" t.repair_result_desc resultDesc," +
				" t.responsibility_org_id orgId, t.responsibility_org_name orgName " +
				" FROM cc_unsatisfy_sheet t " +
				"WHERE " +
				" t.work_order_id = ? " +
				" AND t.tache_id = 2 " +
				" AND t.sheet_status = 2 " +
				" AND t.lock_flag = 2 " +
				"ORDER BY" +
				" t.response_date DESC " +
				" LIMIT 1";
		List<Map<String, Object>> tmpList = jdbcTemplate.queryForList(sql, orderId);
		if(tmpList.isEmpty()){
			log.warn("没有查询到服务单号为: {} 的工单", orderId);
			return null;
		}
		EvaluationSheetPojo evaluationSheetPojo = new EvaluationSheetPojo();
		evaluationSheetPojo.setContactResultId(Integer.parseInt(tmpList.get(0).get("contactResult").toString()));
		evaluationSheetPojo.setContactResultDesc(tmpList.get(0).get("resultDesc").toString());
		evaluationSheetPojo.setUnsatisfiedReasonId(Integer.parseInt(tmpList.get(0).get("reason").toString()));
		evaluationSheetPojo.setUnsatisfiedReasonDesc(tmpList.get(0).get("reasonDesc").toString());
		evaluationSheetPojo.setReasonDetailId(Integer.parseInt(tmpList.get(0).get("detail").toString()));
		evaluationSheetPojo.setReasonDetailDesc(tmpList.get(0).get("detailDesc").toString());
		evaluationSheetPojo.setCustAdviceComfirm(tmpList.get(0).get("custAdvice").toString());
		evaluationSheetPojo.setDealSchemeId(tmpList.get(0).get("dealScheme").toString());
		evaluationSheetPojo.setDealSchemeDesc(tmpList.get(0).get("schemeDesc").toString());
		evaluationSheetPojo.setRepairResultId(Integer.parseInt(tmpList.get(0).get("result").toString()));
		evaluationSheetPojo.setRepairResultDesc(tmpList.get(0).get("resultDesc").toString());
		evaluationSheetPojo.setResponsibilityOrgId(tmpList.get(0).get("orgId").toString());
		evaluationSheetPojo.setResponsibilityOrgName(tmpList.get(0).get("orgName").toString());
		return evaluationSheetPojo;
	}

	@Override
	public OrganizationPojo getStaffOrganization(String staffId) {
    	String sql = "SELECT o.ORG_ID receiveOrgId, " +
				"o.ORG_NAME receiveOrgName " +
				"FROM tsm_staff t " +
				"LEFT JOIN tsm_organization o ON t.ORG_ID = o.ORG_ID WHERE " +
				"t.STAFF_ID = ?";
		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql, staffId);
		List<OrganizationPojo> list = JSON.parseArray(JSON.toJSONString(queryForList), OrganizationPojo.class);
		return list.get(0);
	}

	public int updateFetchSheetStaff(String sheetId, int staffId, String staffName, String orgId, String orgName, int status, int lockFlag) {
		try {
			String strSql = "UPDATE CC_UNSATISFY_SHEET T SET T.RECEIVE_STAFF = ?, T.RECEIVE_STAFF_NAME = ?, "
					+ "T.DISTILL_DATE = NOW(), T.DEAL_STAFF = ?, T.DEAL_STAFF_NAME = ?, T.DEAL_ORG_ID = ?, "
					+ "T.DEAL_ORG_NAME = ?, T.SHEET_STATUS = ?, T.LOCK_FLAG = ? WHERE T.WORK_SHEET_ID = ?";
			return jdbcTemplate.update(strSql, staffId, staffName, staffId, staffName, orgId, orgName, status, lockFlag, sheetId);
		}
		catch(Exception e) {
			log.error("updateFetchSheetStaff {} mysql异常: {}", sheetId, e.getMessage(), e);
		}
		return 0;
	}

	public EvaluationSheetPojo getCurrentSheet(String sheetId) {
		String strSql = "select t.work_sheet_id workSheetId,t.work_order_id workOrderId,t.region_id regionId,t.region_name regionName,"
				+ "t.lock_flag lockFlag, t.sheet_status sheetStatus, DATE_FORMAT(t.limit_date,'%Y-%m-%d %H:%i:%s') limitDate, "
				+ "(now()>t.limit_date) isSheetOverTime, (t.sheet_limit - TIMESTAMPDIFF(MINUTE, t.create_date, now())) leftLimit "
				+ "from cc_unsatisfy_sheet t where t.work_sheet_id = ?";
		List<Map<String, Object>> tmpList = jdbcTemplate.queryForList(strSql, sheetId);
		if(tmpList.isEmpty()){
			log.warn("没有查询到单号为: {} 的工单", sheetId);
			return null;
		}
		List<EvaluationSheetPojo> list = JSON.parseArray(JSON.toJSONString(tmpList), EvaluationSheetPojo.class);
		return list.get(0);
	}

	public String getAuditSheetId(String orderId) {
		String sql = "select work_sheet_id sheetId from cc_unsatisfy_sheet c where c.sheet_status=3 and c.lock_flag=0 and c.tache_id=3 and c.work_order_id=?";
		List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql, orderId);
		Object sheetId = maps.get(0).get("sheetId");
		return sheetId.toString();
	}

	@Override
	public int updateAuditSheet(EvaluationSheetPojo sheet) {
		try {
			String sql = "UPDATE cc_unsatisfy_sheet "
					+ "SET sheet_status = ?, lock_flag = ?, sheet_limit = ?, is_sheet_overtime = ? "
					+ "WHERE work_sheet_id = ?";
			return jdbcTemplate.update(sql, sheet.getSheetStatus(), sheet.getLockFlag(), sheet.getSheetLimit(), 
					sheet.getIsSheetOverTime(),sheet.getWorkSheetId());
		}
		catch(Exception e) {
			log.error("updateAuditSheet {} mysql异常: {}", sheet.getWorkSheetId(), e.getMessage(), e);
		}
		return 0;
	}

	@Override
	public int finishAssginSheet(EvaluationSheetPojo evaluationSheetPojo) {
		try {
			String sql = "UPDATE cc_unsatisfy_sheet SET deal_staff = ?, deal_staff_name = ?, deal_org_id = ?, deal_org_name = ?, " +
					"sheet_status = ?, lock_flag = ?, deal_type = ?, deal_content = ?, response_date = now(), is_sheet_overtime = ?, response_flag = ? "
					+ "where work_sheet_id = ?";
			return jdbcTemplate.update(sql,
					evaluationSheetPojo.getDealStaff(),
					evaluationSheetPojo.getDealStaffName(),
					evaluationSheetPojo.getDealOrgId(),
					evaluationSheetPojo.getDealOrgName(),
					evaluationSheetPojo.getSheetStatus(),
					evaluationSheetPojo.getLockFlag(),
					evaluationSheetPojo.getDealType(),
					evaluationSheetPojo.getDealContent(),
					evaluationSheetPojo.getIsSheetOverTime(),
					evaluationSheetPojo.getResponseFlag(),				
					evaluationSheetPojo.getWorkSheetId());
		}
		catch(Exception e) {
			log.error("finishAssginSheet {} mysql异常: {}", evaluationSheetPojo.getWorkSheetId(), e.getMessage(), e);
		}
		return 0;
	}

	@Override
	public List<EvaluationSheetPojo> getSheetDetail(String orderId) {
    	String sql = "SELECT " +
				" work_sheet_id workSheetId, work_order_id workOrderId, DATE_FORMAT(create_date,'%Y-%m-%d %H:%i:%s') createDate," +
				" receive_org_id receiveOrgId, receive_org_name receiveOrgName, receive_staff receiveStaff," +
				" receive_staff_name receiveStaffName, DATE_FORMAT(distill_date,'%Y-%m-%d %H:%i:%s') distillDate, deal_staff dealStaff," +
				" deal_staff_name dealStaffName, deal_org_id dealOrgId, deal_org_name dealOrgName," +
				" return_staff returnStaff, return_staff_name returnStaffName, return_org_id returnOrgId," +
				" return_org_name returnOrgName, return_require returnRequire, source_sheet_id sourceSheetId," +
				" sheet_status sheetStatus, lock_flag lockFlag, sheet_limit sheetLimit, tache_id tacheId," +
				" tache_desc tacheDesc, DATE_FORMAT(status_date,'%Y-%m-%d %H:%i:%s') statusDate, deal_type dealType, deal_content dealContent," +
				" DATE_FORMAT(response_date,'%Y-%m-%d %H:%i:%s') responseDate, DATE_FORMAT(limit_date,'%Y-%m-%d %H:%i:%s') limitDate, is_sheet_overtime isSheetOverTime, response_flag responseFlag," +
				" contact_result_id contactResultId, contact_result_desc contactResultDesc, unsatisfied_reason_id unsatisfiedReasonId," +
				" unsatisfied_reason_desc unsatisfiedReasonDesc, reason_detail_id reasonDetailId, reason_detail_desc reasonDetailDesc, cust_advice_comfirm custAdviceComfirm," +
				" deal_scheme_id dealSchemeId, deal_scheme_desc dealSchemeDesc, repair_result_id repairResultId," +
				" repair_result_desc repairResultDesc, responsibility_org_id responsibilityOrgId," +
				" responsibility_org_name responsibilityOrgName " +
				"FROM " +
				" cc_unsatisfy_sheet " +
				" WHERE" +
				" work_order_id = ? order by create_date";
		List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(sql, orderId);
    	return JSON.parseArray(JSON.toJSONString(queryForList), EvaluationSheetPojo.class);
	}

	public List<Map<String, Object>> getDealSheetFlow(String orderId, boolean hisFlag) {
		String tableName = "cc_unsatisfy_sheet";
		if(hisFlag) {
			tableName = "cc_unsatisfy_sheet_his";
		}
		String sql = "SELECT " +
				" work_sheet_id workSheetId, work_order_id workOrderId, region_id regionId, region_name regionName, DATE_FORMAT(create_date,'%Y-%m-%d %H:%i:%s') createDate," +
				" receive_org_id receiveOrgId, receive_org_name receiveOrgName, receive_staff receiveStaff," +
				" receive_staff_name receiveStaffName, DATE_FORMAT(distill_date,'%Y-%m-%d %H:%i:%s') distillDate, deal_staff dealStaff," +
				" deal_staff_name dealStaffName, deal_org_id dealOrgId, deal_org_name dealOrgName," +
				" return_staff returnStaff, return_staff_name returnStaffName, return_org_id returnOrgId," +
				" return_org_name returnOrgName, return_require returnRequire, source_sheet_id sourceSheetId," +
				" sheet_status sheetStatus, lock_flag lockFlag, sheet_limit sheetLimit, tache_id tacheId," +
				" tache_desc tacheDesc, DATE_FORMAT(status_date,'%Y-%m-%d %H:%i:%s') statusDate, deal_type dealType, deal_content dealContent," +
				" DATE_FORMAT(response_date,'%Y-%m-%d %H:%i:%s') responseDate, DATE_FORMAT(limit_date,'%Y-%m-%d %H:%i:%s') limitDate, is_sheet_overtime isSheetOverTime, response_flag responseFlag," +
				" contact_result_id contactResultId, contact_result_desc contactResultDesc, unsatisfied_reason_id unsatisfiedReasonId," +
				" unsatisfied_reason_desc unsatisfiedReasonDesc, reason_detail_id reasonDetailId, reason_detail_desc reasonDetailDesc, cust_advice_comfirm custAdviceComfirm," +
				" deal_scheme_id dealSchemeId, deal_scheme_desc dealSchemeDesc, repair_result_id repairResultId," +
				" repair_result_desc repairResultDesc, responsibility_org_id responsibilityOrgId," +
				" responsibility_org_name responsibilityOrgName" +
				" FROM " + tableName +
				" WHERE sheet_status = 2 and work_order_id = ? ORDER BY response_date";
		return jdbcTemplate.queryForList(sql, orderId);
	}

	public int saveSheetHisByOrderId(String orderId) {
    	String sql = "INSERT INTO cc_unsatisfy_sheet_his"
    			+ "(work_sheet_id, work_order_id, region_id, region_name, create_date, receive_org_id, receive_org_name, receive_staff, receive_staff_name, "
    			+ "distill_date, deal_staff, deal_staff_name, deal_org_id, deal_org_name, return_staff, return_staff_name, return_org_id, return_org_name, "
    			+ "return_require, source_sheet_id, sheet_status, lock_flag, sheet_limit, tache_id, tache_desc, status_date, deal_type, deal_content, "
    			+ "response_date, limit_date, is_sheet_overtime, response_flag, contact_result_id, contact_result_desc, "
    			+ "unsatisfied_reason_id, unsatisfied_reason_desc, reason_detail_id, reason_detail_desc, cust_advice_comfirm, deal_scheme_id, deal_scheme_desc, "
    			+ "repair_result_id, repair_result_desc, responsibility_org_id, responsibility_org_name, save_dealcontent) select "
    			+ "work_sheet_id, work_order_id, region_id, region_name, create_date, receive_org_id, receive_org_name, receive_staff, receive_staff_name, "
    			+ "distill_date, deal_staff, deal_staff_name, deal_org_id, deal_org_name, return_staff, return_staff_name, return_org_id, return_org_name, "
    			+ "return_require, source_sheet_id, sheet_status, lock_flag, sheet_limit, tache_id, tache_desc, status_date, deal_type, deal_content, "
    			+ "response_date, limit_date, is_sheet_overtime, response_flag, contact_result_id, contact_result_desc, "
    			+ "unsatisfied_reason_id, unsatisfied_reason_desc, reason_detail_id, reason_detail_desc, cust_advice_comfirm, deal_scheme_id, deal_scheme_desc, "
    			+ "repair_result_id, repair_result_desc, responsibility_org_id, responsibility_org_name, save_dealcontent from cc_unsatisfy_sheet where work_order_id = ?";
		try {
			return jdbcTemplate.update(sql, orderId);
		}
		catch(Exception e) {
			log.error("saveSheetHisByOrderId {} mysql异常: {}", orderId, e.getMessage(), e);
		}
		return 0;
	}
	
	public int deleteOrder(String orderId) {
		String sql = "delete from cc_unsatisfy_order where work_order_id = ?";
		try {
			return jdbcTemplate.update(sql, orderId);
		}
		catch(Exception e) {
			log.error("deleteOrder {} mysql异常: {}", orderId, e.getMessage(), e);
		}
		return 0;
	}

	public int saveOrderHisByOrderId(String orderId) {
		String sql = "INSERT INTO cc_unsatisfy_order_his"
				+ "(work_order_id, create_date, create_type, topic, region_id, region_name, area_name, "
				+ "rela_man, rela_info, prod_num, source_num, best_order, best_order_desc, five_order, five_order_desc, "
				+ "touch_channel, service_org_id, service_org_name, service_staff_id, service_staff_name, "
				+ "order_limit, limit_date, finish_date, is_overtime, service_order_id) select "
				+ "work_order_id, create_date, create_type, topic, region_id, region_name, area_name, "
				+ "rela_man, rela_info, prod_num, source_num, best_order, best_order_desc, five_order, five_order_desc, "
				+ "touch_channel, service_org_id, service_org_name, service_staff_id, service_staff_name, "
				+ "order_limit, limit_date, finish_date, is_overtime, service_order_id "
				+ "from cc_unsatisfy_order where work_order_id = ?";
		try {
			return jdbcTemplate.update(sql, orderId);
		}
		catch(Exception e) {
			log.error("saveOrderHisByOrderId {} mysql异常: {}", orderId, e.getMessage(), e);
		}
		return 0;
	}

	public int deleteSheetByOrderId(String orderId) {
		String sql = "delete from cc_unsatisfy_sheet where work_order_id = ?";
		try {
			return jdbcTemplate.update(sql, orderId);
		}
		catch(Exception e) {
			log.error("deleteSheetByOrderId {} mysql异常: {}", orderId, e.getMessage(), e);
		}
		return 0;
	}

	@Override
	public int finishSatisfyDetail(String orderId, String payReturn, String repairResult, String failedReason, String result, String comment){
		try {
			String sql = "update cc_unsatisfy_detail set finish_flag = 1, is_payreturn = ?, repair_result = ?, " +
					"payreturn_failed_reason = ?, payreturn_result = ?, payreturn_comment = ? where work_order_id = ?";
	    	return jdbcTemplate.update(sql, payReturn, repairResult, failedReason, result, comment, orderId);
		}
		catch(Exception e) {
			log.error("finishSatisfyDetail {} mysql异常: {}", orderId, e.getMessage(), e);
		}
		return 0;
	}

	@Override
	public EvaluationOrderPojo getEvaluationOrder(String orderId, boolean hisFlag) {
		String tableName = "cc_unsatisfy_order";
		if(hisFlag) {
			tableName = "cc_unsatisfy_order_his";
		}
		String sql = "select work_order_id workOrderId, " +
				"DATE_FORMAT(create_date,'%Y-%m-%d %H:%i:%s') createDate, " +
				"create_type createType, " +
				"topic topic, " +
				"region_id regionId, " +
				"region_name regionName, " +
				"area_name areaName, " +
				"rela_man relaMan, " +
				"rela_info relaInfo, " +
				"prod_num prodNum, " +
				"source_num sourceNum, " +
				"best_order bestOrder, " +
				"best_order_desc bestOrderDesc, " +
				"five_order fiveOrder, " +
				"five_order_desc fiveOrderDesc, " +
				"touch_channel touchChannel, " +
				"service_org_id serviceOrgId, " +
				"service_org_name serviceOrgName, " +
				"service_staff_id serviceStaffId, " +
				"service_staff_name serviceStaffName, " +
				"order_limit orderLimit, " +
				"DATE_FORMAT(limit_date,'%Y-%m-%d %H:%i:%s') limitDate, " +
				"DATE_FORMAT(finish_date,'%Y-%m-%d %H:%i:%s') finishDate, " +
				"is_overtime isOverTime, " +
				"service_order_id serviceOrderId " +
				"from " + tableName + " where work_order_id = ?";
		List<Map<String, Object>> tmpList = jdbcTemplate.queryForList(sql, orderId);
		if(tmpList.isEmpty()){
			log.warn("没有查询到单号为: {} 的服务单", orderId);
			return null;
		}
		List<EvaluationOrderPojo> list = JSON.parseArray(JSON.toJSONString(tmpList), EvaluationOrderPojo.class);
		return list.get(0);
	}

	@Override
	public int updateDealDetail(EvaluationSheetPojo evaluationSheetPojo) {
		try {
			String strSql = "UPDATE CC_UNSATISFY_SHEET T SET T.contact_result_id = ?, T.contact_result_desc = ?, "
					+ "T.unsatisfied_reason_id = ?, T.unsatisfied_reason_desc = ?, "
					+ "T.reason_detail_id = ?, T.reason_detail_desc = ?, "
					+ "T.cust_advice_comfirm = ?, "
					+ "T.deal_scheme_id = ?, T.deal_scheme_desc = ?, "
					+ "T.repair_result_id = ?, T.repair_result_desc = ?, "
					+ "T.responsibility_org_id = ?, T.responsibility_org_name = ? "
					+ "WHERE T.WORK_SHEET_ID = ?";
			return jdbcTemplate.update(strSql,
					evaluationSheetPojo.getContactResultId(),
					evaluationSheetPojo.getContactResultDesc(),
					evaluationSheetPojo.getUnsatisfiedReasonId(),
					evaluationSheetPojo.getUnsatisfiedReasonDesc(),
					evaluationSheetPojo.getReasonDetailId(),
					evaluationSheetPojo.getReasonDetailDesc(),
					evaluationSheetPojo.getCustAdviceComfirm(),
					evaluationSheetPojo.getDealSchemeId(),
					evaluationSheetPojo.getDealSchemeDesc(),
					evaluationSheetPojo.getRepairResultId(),
					StringUtils.defaultIfBlank(evaluationSheetPojo.getRepairResultDesc(), null),
					evaluationSheetPojo.getResponsibilityOrgId(),
					evaluationSheetPojo.getResponsibilityOrgName(),
					evaluationSheetPojo.getWorkSheetId()
			);
		}
		catch(Exception e) {
			log.error("updateDealDetail {} mysql异常: {}", evaluationSheetPojo.getWorkSheetId(), e.getMessage(), e);
	    }
		return 0;
	}

	/**
	 * CP+区号+年月日+6位序列号
	 * @param regionId
	 */
    @SuppressWarnings("unchecked")
	public String getCpOrderId(int regionId) {
    	String seqNum = pubFunc.getSeqVal("SEQ_CP_ORDER_ID", 6);
    	if(seqNum == null) {
    		return null;
    	}
    	try {
    		String strsql = "SELECT CONCAT('CP', ifnull(A.REGION_TELNO,'2'), DATE_FORMAT(NOW(),'%y%m%d'), ?)  AS ORDER_ID "
            		+ "FROM TRM_REGION A WHERE A.REGION_ID = ?";
            return this.jdbcTemplate.query(strsql, new Object[] {seqNum, regionId}, new KeyRowMapper()).get(0).toString();
    	}
    	catch(Exception e) {
    		log.error("getCpOrderId seqNum: {} 异常: {}", seqNum, e.getMessage(), e);
    	}
        return null;
    }
    
	@SuppressWarnings("rawtypes")
	class KeyRowMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int arg1) throws SQLException {
            return rs.getString(1);
        }
    }
	
	public int saveUnsatisfyOrder(EvaluationOrderPojo order) {
		try {
			String strSql = "INSERT INTO cc_unsatisfy_order "
					+ "(work_order_id, create_type, topic, "
					+ "region_id, region_name, area_name, "
					+ "rela_man, rela_info, prod_num, source_num, "
					+ "best_order, best_order_desc, five_order, five_order_desc, "
					+ "touch_channel, service_org_id, service_org_name, service_staff_id, service_staff_name, "
					+ "order_limit, limit_date, is_overtime, service_order_id) values "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, DATE_ADD(NOW(),INTERVAL ? hour), ?, ?)";
	
			return jdbcTemplate.update(strSql,
					order.getWorkOrderId(),
					order.getCreateType(),
					order.getTopic(),
					order.getRegionId(),
					order.getRegionName(),
					StringUtils.defaultIfBlank(order.getAreaName(), null),
					order.getRelaMan(),
					order.getRelaInfo(),
					order.getProdNum(),
					StringUtils.defaultIfBlank(order.getSourceNum(), null),
					order.getBestOrder(),
					StringUtils.defaultIfBlank(order.getBestOrderDesc(), null),
					order.getFiveOrder(),
					StringUtils.defaultIfBlank(order.getFiveOrderDesc(), null),
					order.getTouchChannel(),
					order.getServiceOrgId(),
					order.getServiceOrgName(),
					order.getServiceStaffId(),
					order.getServiceStaffName(),
					order.getOrderLimit(),
					order.getOrderLimit(),
					order.getIsOverTime(),
					order.getServiceOrderId());
		}
		catch(Exception e) {
			log.error("saveUnsatisfyOrder {} mysql异常: {}", order, e.getMessage(), e);
		}
		return 0;	
	}
	
	public int saveUnsatisfyDetail(EvaluationDetail detail) {
		try {
			String strSql = "INSERT INTO cc_unsatisfy_detail "
					+ "(service_order_id, accept_date, unified_complaint_code, "
					+ "work_order_id, order_assess_id, "
					+ "assess_mode, assess_mode_desc, join_mode, join_mode_desc, "
					+ "crm_cust_id, prod_num, rela_info, assess_send_time, user_subtime, "
					+ "issolve, check_assess_result, check_assess_score, satisfied_reason, "
					+ "finish_flag) values "
					+ "(?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), str_to_date(?, '%Y-%m-%d %H:%i:%s'), ?, ?, ?, ?, ?)";
	
			return jdbcTemplate.update(strSql,
					detail.getServiceOrderId(),
					detail.getAcceptDate(),
					StringUtils.defaultIfEmpty(detail.getUnifiedComplaintCode(), null),
					detail.getWorkOrderId(),
					detail.getOrderAssessId(),
					detail.getAssessMode(),
					detail.getAssessModeDesc(), 
					detail.getJoinMode(),
					detail.getJoinModeDesc(),
					detail.getCrmCustId(),
					detail.getProdNum(),
					detail.getRelaInfo(),
					detail.getAssessSendTime(),
					detail.getUserSubTime(),
					detail.getIsSolve(),
					detail.getCheckAssessResult(),
					detail.getCheckAssessScore(),
					detail.getSatisfiedReason(),
					detail.getFinishFlag());
		}
		catch(Exception e) {
			log.error("saveUnsatisfyDetail {} mysql异常: {}", detail, e.getMessage(), e);
		}
		return 0;	
	}
	
	/**
	 * 区号+年月日+6位序列号
	 * @param regionId
	 */
    @SuppressWarnings("unchecked")
	public String getCpSheetId(int regionId) {
    	String seqNum = pubFunc.getSeqVal("SEQ_CP_SHEET_ID", 6);
    	if(seqNum == null) {
    		return null;
    	}
    	try {
    		String strsql = "SELECT CONCAT(ifnull(A.REGION_TELNO,'2'), DATE_FORMAT(NOW(),'%y%m%d'), ?)  AS SHEET_ID "
            		+ "FROM TRM_REGION A WHERE A.REGION_ID = ?";
            return this.jdbcTemplate.query(strsql, new Object[] {seqNum, regionId}, new KeyRowMapper()).get(0).toString();
    	}
    	catch(Exception e) {
    		log.error("getCpSheetId seqNum: {} 异常: {}", seqNum, e.getMessage(), e);
    	}
        return null;
    }
    
	public int saveUnsatisfySheet(EvaluationSheetPojo sheet) {
		try {
			String strSql = "INSERT INTO cc_unsatisfy_sheet "
					+ "(work_sheet_id, work_order_id, region_id, region_name, "
					+ "receive_org_id, receive_org_name, receive_staff, receive_staff_name, "
					+ "deal_staff, deal_staff_name, deal_org_id, deal_org_name, "
					+ "return_staff, return_staff_name, return_org_id, return_org_name, return_require, "
					+ "source_sheet_id, sheet_status, lock_flag, sheet_limit, tache_id, tache_desc, limit_date, "
					+ "is_sheet_overtime, response_flag) values "
					+ "(?, ?, ?, ?, "
					+ "?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, ?, ?, ?, "
					+ "?, ?, ?, ?, ?, ?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), "
					+ "?, ?)";
			return jdbcTemplate.update(strSql,
					sheet.getWorkSheetId(),
					sheet.getWorkOrderId(),
					sheet.getRegionId(),
					sheet.getRegionName(),
					sheet.getReceiveOrgId(),
					sheet.getReceiveOrgName(),
					sheet.getReceiveStaff(),
					sheet.getReceiveStaffName(),
					sheet.getDealStaff(),
					sheet.getDealStaffName(),
					sheet.getDealOrgId(),
					sheet.getDealOrgName(),
					sheet.getReturnStaff(),
					sheet.getReturnStaffName(),
					sheet.getReturnOrgId(),
					sheet.getReturnOrgName(),
					sheet.getReturnRequire(),
					sheet.getSourceSheetId(),
					sheet.getSheetStatus(),
					sheet.getLockFlag(),
					sheet.getSheetLimit(),
					sheet.getTacheId(),
					sheet.getTacheDesc(),
					sheet.getLimitDate(),
					sheet.getIsSheetOverTime(),
					sheet.getResponseFlag()
			);
		}
		catch(Exception e) {
			log.error("saveUnsatisfySheet {} mysql异常: {}", sheet, e.getMessage(), e);
		}
		return 0;	
	}
	
	public int finishSatisfyOrder(String orderId, int isOverTime) {
		try {
			String strSql = "update cc_unsatisfy_order set finish_date = now(), is_overtime = ? where work_order_id = ?";
			return jdbcTemplate.update(strSql, isOverTime, orderId);
		}
		catch(Exception e) {
			log.error("finishSatisfyOrder {} mysql异常: {}", orderId, e.getMessage(), e);
		}
		return 0;
	}
	
	public List<Map<String, Object>> getCallOutRecord(String orderId) {
		String querySql = "select c.CALL_GUID, DATE_FORMAT(c.CALL_ARRIVE,'%Y-%m-%d %H:%i:%s') CALL_ARRIVE, c.STAFF_ID from cc_order_callout_rec c where c.SERVICE_ORDER_ID=? order by c.CALL_ARRIVE";
		return jdbcTemplate.queryForList(querySql, orderId);
	}
	
	public int updateCallOutRecord(EvaluationDetail detail) {
		try {
			String sql = "update cc_unsatisfy_detail "
	    			+ "set payreturn_time = str_to_date(?, '%Y-%m-%d %H:%i:%s'), payreturn_workno = ?, call_guid = ? "
	    			+ "where work_order_id = ?";
	    	return jdbcTemplate.update(sql, detail.getPayReturnTime(), detail.getPayReturnWorkNo(), detail.getCallGuid(), detail.getWorkOrderId());
		}
		catch(Exception e) {
			log.error("updateCallOutRecord {} mysql异常: {}", detail.getWorkOrderId(), e.getMessage(), e);
		}
		return 0;
	}
	
	public EvaluationDetail getEvaluationDetail(String orderId) {
		String sql = "select service_order_id serviceOrderId, " +
				"accept_date acceptDate, " +
				"unified_complaint_code unifiedComplaintCode, " +
				"work_order_id workOrderId, " +
				"DATE_FORMAT(create_date,'%Y-%m-%d %H:%i:%s') createDate, " +
				"order_assess_id orderAssessId, " +
				"assess_mode assessMode, " +
				"assess_mode_desc assessModeDesc, " +
				"join_mode joinMode, " +
				"join_mode_desc joinModeDesc, " +
				"issolve isSolve, " +
				"check_assess_result checkAssessResult, " +
				"check_assess_score checkAssessScore " +
				"from cc_unsatisfy_detail where work_order_id = ?";
		List<Map<String, Object>> tmpList = jdbcTemplate.queryForList(sql, orderId);
		if(tmpList.isEmpty()){
			log.warn("没有查询到单号为: {} 的修复详情", orderId);
			return null;
		}
		List<EvaluationDetail> list = JSON.parseArray(JSON.toJSONString(tmpList), EvaluationDetail.class);
		return list.get(0);
	}

	@Override
	public String getSaveDealContent(String sheetId) {
		String strsql = "SELECT save_dealcontent saveDealContent "
				+ "FROM CC_UNSATISFY_SHEET A WHERE A.work_sheet_id = ?";
		List<Map<String, Object>> maps = jdbcTemplate.queryForList(strsql, sheetId);
		return maps.get(0).get("saveDealContent") == null ? "" : maps.get(0).get("saveDealContent").toString();
	}

	@Override
	public int saveStageContent(String sheetId, String content) {
		String strSql = "UPDATE CC_UNSATISFY_SHEET W SET W.save_dealcontent = ? WHERE W.work_sheet_id = ?";
		return jdbcTemplate.update(strSql, content, sheetId);
	}
	
	public boolean isExistEvaluationDetail(String serviceOrderId) {
		String sql = "select work_order_id from cc_unsatisfy_detail where service_order_id = ?";
		List<Map<String, Object>> tmpList = jdbcTemplate.queryForList(sql, serviceOrderId);
		return !tmpList.isEmpty();
	}

}
