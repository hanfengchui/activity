/**
 * <p>类名：SheetLimitTimeImpl</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：chenjw</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by  chenjw 2008-3-29 10:38:05</p>
 * <p></p>
 */
package com.timesontransfar.customservice.paramconfig.dao.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.paramconfig.dao.IsheetLimitTimeDao;
import com.timesontransfar.customservice.paramconfig.pojo.SheetLimitTimeCollocate;
import com.timesontransfar.customservice.paramconfig.pojo.SheetLimitTimeCollocateRmp;

/**
 * @author chenjw
 * @date 2008-3-29 10:38:05
 */
@Component(value="sheetLimitTimeDao")
public class SheetLimitTimeImpl implements IsheetLimitTimeDao {
	private static final Logger log = LoggerFactory.getLogger(SheetLimitTimeImpl.class);

	@Autowired
    private JdbcTemplate jdbcTemplate;


	
	/**
	 * 得到处理时限和预警时限
	 * @param region 地域
	 * @param serviceType 服务类型
	 * @param tacheId 环节
	 * @param limitType 时限类型
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SheetLimitTimeCollocate getSheetLimitime(int region,int serviceType,int tacheId,
		int limitType,int custGread,int urgency) {
		String strSql = "SELECT * FROM CC_SHEET_LIMITTIME A WHERE A.REGION_ID=? AND A.SERVICE_TYPE=? AND A.TACHE_ID=? AND " +
		" A.CUSTOM_GREAD=? AND A.URGENCY_GRADE=? AND A.LIMI_TYPE=?";
		strSql+=" AND A.ACCEPT_DIRECTORY_ID IS NULL";
		List tmp = this.jdbcTemplate.query(strSql, new Object[]{
			region,
			serviceType,
			tacheId,
			custGread,
			urgency,
			limitType},new SheetLimitTimeCollocateRmp());
		if(tmp.isEmpty()) {
			return null;
		}
		return (SheetLimitTimeCollocate) tmp.get(0);
	}

	@SuppressWarnings("rawtypes")
	public int[] getLimitTimeNew(String where) {
		int[] defaultLimitTime = { 45, 3, 48 };
		String strSql = "SELECT deal_hours, audit_hours, whole_hours FROM cc_sheet_limittime_new WHERE 1 = 1" + where
				+ " ORDER BY deal_hours, audit_hours, whole_hours";
		List list = this.jdbcTemplate.queryForList(strSql);
		if (list.isEmpty()) {
			log.info("根据SQL语句:{}>>为查询到时限", strSql);
			return defaultLimitTime;
		}
		Map map = (Map) list.get(0);
		defaultLimitTime[0] = Integer.parseInt(map.get("DEAL_HOURS").toString());
		defaultLimitTime[1] = Integer.parseInt(map.get("AUDIT_HOURS").toString());
		defaultLimitTime[2] = Integer.parseInt(map.get("WHOLE_HOURS").toString());
		return defaultLimitTime;
	}
	
}
