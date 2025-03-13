/*
 * 文 件 名：CmpUnifiedContactDAOImpl.java
 * 版    权：
 * 描    述：
 * 修改时间：2017-12-11
 * 修改内容：新增
 */
package com.timesontransfar.customservice.worksheet.dao.cmpGroup;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedContact;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedContactRmp;

/**
 * 操作表cc_cmp_unified_contact
 * 
 * @version
 * @since
 */
@Component("cmpUnifiedContactDAOImpl")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CmpUnifiedContactDAOImpl {
	@Resource
	private JdbcTemplate jdbcTemplate;

	private String insertSql = "INSERT INTO cc_cmp_unified_contact (service_order_id, work_sheet_id, contact_status, oper_logon, oper_date, contact_type) VALUES (?, ?, ?, ?, NOW(), ?)";
	private String selectSql = "SELECT SERVICE_ORDER_ID, WORK_SHEET_ID, CONTACT_STATUS, OPER_LOGON, DATE_FORMAT(OPER_DATE, '%Y-%m-%d %H:%i:%s') OPER_DATE, CONTACT_TYPE FROM cc_cmp_unified_contact"
			+ " WHERE contact_status = 1 AND service_order_id = ? AND contact_type = ?";
	private String updateSql = "UPDATE cc_cmp_unified_contact SET contact_status = 2 WHERE contact_status = 1 AND service_order_id = ? AND contact_type = ?";
	private String insertHisSql = "INSERT INTO cc_cmp_unified_contact_his " + 
			"(service_order_id, work_sheet_id, contact_status, oper_logon, oper_date, contact_type) " + 
			"SELECT service_order_id, work_sheet_id, contact_status, oper_logon, oper_date, contact_type FROM cc_cmp_unified_contact WHERE service_order_id = ?";
	private String deleteSql = "DELETE FROM cc_cmp_unified_contact WHERE service_order_id = ?";

	public int saveUnifiedContact(ComplaintUnifiedContact cuc) {
		return jdbcTemplate.update(insertSql, cuc.getServiceOrderId(), cuc.getWorkSheetId(), "1", cuc.getOperLogon(), cuc.getContactType());
	}

	public ComplaintUnifiedContact queryUnifiedContactByOrderId(String orderId, String contactType) {
		List list = jdbcTemplate.query(selectSql, new Object[] { orderId, contactType }, new ComplaintUnifiedContactRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (ComplaintUnifiedContact) list.get(0);
	}

	public int cancelUnifiedContactStatusByOrderId(ComplaintUnifiedContact cuc) {
		if (jdbcTemplate.update(updateSql, cuc.getServiceOrderId(), cuc.getContactType()) == 1) {
			return jdbcTemplate.update(insertSql, cuc.getServiceOrderId(), cuc.getWorkSheetId(), "0", cuc.getOperLogon(), cuc.getContactType());
		}
		return 0;
	}

	public int saveUnifiedContactHisByOrderId(String orderId) {
		if (jdbcTemplate.update(insertHisSql, orderId) > 0) {
			return jdbcTemplate.update(deleteSql, orderId);
		}
		return 0;
	}
}