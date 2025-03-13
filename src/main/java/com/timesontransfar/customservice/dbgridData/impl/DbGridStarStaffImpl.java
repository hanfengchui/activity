/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbGridStarStaff;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;

@Component(value="dbGridStarStaff")
public class DbGridStarStaffImpl implements  IdbGridStarStaff {
	
	private static final Logger log = LoggerFactory.getLogger(DbGridStarStaffImpl.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	
	/**
	 * 查询星级客户列表
	 * @param begin
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryStarStaffList(int begion,String strWhere){
		String strSql= "SELECT STAFF_ID,STAFFNAME,STAR_CLASS,STAR_CLASS_ID,GROUP_TYPE, "
				+ " DATE_FORMAT(IMPORT_DATE,'%Y-%m-%d %H:%i:%s')IMPORT_DATE,IMPORT_STAFF"
				+ " FROM CC_STAR_STAFF ";
		
		if (!"".equals(strWhere)) {
			// strWhere = strWhere.toUpperCase();
			int andIndex = strWhere.indexOf("AND");
			if (andIndex == -1) {
				if (log.isDebugEnabled()) {
					log.debug("WHERE 条件中没有AND,请检查WHERE条件" + strWhere);
					return null;
				}
			} else {
				strSql = strSql + strWhere;
			}
		}
		// 权限匹配
		/*Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "C");
		tableMap.put("CC_SERVICE_ORDER_ASK", "B");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "A");
		tableMap.put("CC_ORDER_CUST_INFO", "D");
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql,
				"900018308");*/
		return this.dbgridDataPub.getResult(strSql, begion,
				" ORDER BY STAFF_ID ASC",
				DbgridStatic.GRID_FUNID_STAR_STAFF_LIST);

	}
	
	
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}


	public IdbgridDataPub getDbgridDataPub() {
		return dbgridDataPub;
	}


	public void setDbgridDataPub(IdbgridDataPub dbgridDataPub) {
		this.dbgridDataPub = dbgridDataPub;
	} 
	
	 
}
