/*
 * 文 件 名：CmpUnifiedRepeatDAOImpl.java
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

import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedRepeat;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedRepeatRmp;

/**
 * 操作表cc_cmp_unified_repeat
 * 
 * @version
 * @since
 */
@Component("cmpUnifiedRepeatDAOImpl")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CmpUnifiedRepeatDAOImpl {
	@Resource
	private JdbcTemplate jdbcTemplate;

	private String insertSql = "INSERT INTO cc_cmp_unified_repeat"
			+ "(cur_soi, oper_logon, oper_date, new_ucc, repeat_status, cur_whi, repeat_type, come_category)"
			+ " VALUES(?, ?, NOW(), ?, ?, ?, ?, ?)";
	private String selectSql = "SELECT CUR_SOI, OPER_LOGON, DATE_FORMAT(OPER_DATE, '%Y-%m-%d %H:%i:%s') OPER_DATE, NEW_UCC, REPEAT_STATUS, CUR_WHI, REPEAT_TYPE, COME_CATEGORY " + 
			"FROM cc_cmp_unified_repeat " + 
			"WHERE repeat_status = 1 " + 
			"AND cur_soi = ?" + 
			"AND repeat_type = ?";
	private String updateSql = "UPDATE cc_cmp_unified_repeat " + 
			"SET repeat_status = 2 " + 
			"WHERE repeat_status = 1 " + 
			"AND cur_soi = ?" + 
			"AND repeat_type = ?";
	private String insertHisSql = "INSERT INTO cc_cmp_unified_repeat_his " + 
			"(cur_soi, oper_logon, oper_date, new_ucc, repeat_status, cur_whi, repeat_type, come_category)  " + 
			"SELECT cur_soi, oper_logon, oper_date, new_ucc, repeat_status, cur_whi, repeat_type, come_category FROM cc_cmp_unified_repeat WHERE cur_soi = ?";
	private String deleteUnifiedRepeatByCurSoiSql="DELETE FROM cc_cmp_unified_repeat WHERE cur_soi = ?";

	public int saveUnifiedRepeat(ComplaintUnifiedRepeat cur) {
		return jdbcTemplate.update(insertSql, cur.getCurSoi(), cur.getOperLogon(), cur.getNewUcc(), "1", cur.getCurWhi(), cur.getRepeatType(), cur.getComeCategory());
	}

	public ComplaintUnifiedRepeat queryUnifiedRepeatByCurSoi(String curSoi, String repeatType) {
		List list = jdbcTemplate.query(selectSql, new Object[] { curSoi, repeatType }, new ComplaintUnifiedRepeatRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (ComplaintUnifiedRepeat) list.get(0);
	}

	public int cancelUnifiedRepeatStatusByCurSoi(ComplaintUnifiedRepeat cur) {
		if (jdbcTemplate.update(updateSql, cur.getCurSoi(), cur.getRepeatType()) == 1) {
			return jdbcTemplate.update(insertSql, cur.getCurSoi(), cur.getOperLogon(), "", "0", cur.getCurWhi(), cur.getRepeatType(), 0);
		}
		return 0;
	}

	public int saveUnifiedRepeatHisByCurSoi(String curSoi) {
		if (jdbcTemplate.update(insertHisSql, curSoi) > 0) {
			return jdbcTemplate.update(deleteUnifiedRepeatByCurSoiSql, curSoi);
		}
		return 0;
	}
}