/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.IStaffPermit;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.dbgridData.IdbgridDataYn;

/**
 * @author 万荣伟
 *
 */
@Component(value="dbgridDataYn")
@SuppressWarnings("rawtypes")
public class DbgridDataYnImpl implements IdbgridDataYn {
	/**
	 * Logger for this class
	 */
	private static final Logger log =LoggerFactory.getLogger(DbgridDataYnImpl.class);
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private JdbcTemplate yzjt;
	@Autowired
	private ISystemAuthorization systemAuthorization;
	@Autowired
	private IdbgridDataPub		  dbgridDataPub;
	
	
	/**
	 * 疑难工单工单池列表
	 * @param begin
	 * @param strWhere
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GridDataInfo getWaitDealSheetYn(int begion,String strWhere) {
		int staffId=0;
		IStaffPermit staffPermit = dbgridDataPub.getStaffPermit();
		staffId = Integer.parseInt(staffPermit.getStaff().getId());		
		String strSql = "SELECT B.WORK_SHEET_ID ,A.URGENCY_GRADE ," +
				    "DATE_FORMAT(B.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE," +
				    "DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE," +
					"A.PROD_NUM ,A.SERVICE_TYPE_DESC  ,A.SERVICE_ORDER_ID ,A.ACCEPT_COME_FROM_DESC ,A.MONTH_FLAG ,\n" + 
					" B.REGION_NAME ,B.REGION_ID ,B.TACHE_DESC ,B.TACHE_ID ,B.SHEET_TYPE ,B.WORKSHEET_SCHEMA_ID , A.CUST_GUID ,\n" + 
					"B.DEAL_REQUIRE ,B.SHEET_STATU_DESC , B.HASTENT_NUM ,B.HANGUP_TIME_COUNT ,B.SHEET_PRI_VALUE, B.DEAL_LIMIT_TIME ,A.ORDER_STATU_DESC,\n" + 
					" B.SHEET_TYPE_DESC,B.RETURN_STAFF_NAME ,DATE_FORMAT(B.LOCK_DATE,'%Y-%m-%d %H:%i:%s') AS LOCK_DATE,B.LOCK_FLAG,A.ORDER_LIMIT_TIME ,\n" +
					" B.STATIONLIMINT ,B.PRE_ALARM_VALUE ,A.ACCEPT_COUNT , A.URGENCY_GRADE_DESC ,D.APPEAL_PROD_NAME ,\n" + 
					"C.CUST_SERV_GRADE_NAME , B.HANGUP_START_TIME ,B.SHEET_STATU,\n" + 
					"  (SELECT COUNT(*) FROM CC_SHEET_READ_RECORD R WHERE NOW()< R.READ_END_DATE AND R.WORK_SHEET_ID=B.WORK_SHEET_ID " +
					" AND R.READ_STAFF_ID="+staffId+") AS SHEET_READ "+						
					"  FROM CC_SERVICE_ORDER_ASK A,\n" + 
					"       CC_WORK_SHEET B,\n" + 
					"       CC_ORDER_CUST_INFO C,\n" + 
					"       CC_SERVICE_CONTENT_ASK D\n" + 
					" WHERE B.LOCK_FLAG = 0\n" + 
					"   AND A.REGION_ID = B.REGION_ID AND A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" + 
					"   AND A.CUST_GUID = C.CUST_GUID  AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID\n" + 
					"   AND A.SERVICE_DATE = 0 AND B.SHEET_STATU != 600000450 ";
		if(!"".equals(strWhere)) {
			//strWhere = strWhere.toUpperCase();
			int andIndex = strWhere.indexOf("AND");
			if(andIndex == -1) {
				if(log.isDebugEnabled()) {
					log.debug("WHERE 条件中没有AND,请检查WHERE条件"+strWhere);
					return null;
				}
			} else {
				strSql=strSql+strWhere;
			}			
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "B");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");		 		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018257");
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY B.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_YN_SHEET);
	}
	/**
	 * 疑难工单我的任务列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GridDataInfo getDealingSheetYn(int begion,String strWhere) {
		int staffId=0;
		IStaffPermit staffPermit = dbgridDataPub.getStaffPermit();
		staffId = Integer.parseInt(staffPermit.getStaff().getId());				
		String strSql = 	"SELECT A.SERVICE_ORDER_ID,A.PROD_NUM,A.SERVICE_TYPE_DESC, A.ACCEPT_COME_FROM_DESC,\n" +
							"       A.URGENCY_GRADE_DESC, B.REGION_ID,B.TACHE_DESC,\n" + 
							"       B.TACHE_ID, B.WORK_SHEET_ID,A.CUST_GUID, B.DEAL_REQUIRE,\n" + 
							"       A.MONTH_FLAG, B.SHEET_TYPE,  B.WORKSHEET_SCHEMA_ID,\n" + 
							"       B.SHEET_STATU_DESC, B.HASTENT_NUM, B.HANGUP_TIME_COUNT,\n" + 
							"       B.SHEET_PRI_VALUE, B.DEAL_LIMIT_TIME, A.ORDER_STATU_DESC,\n" + 
							"       B.SHEET_TYPE_DESC, B.RETURN_STAFF_NAME, A.ORDER_LIMIT_TIME,\n" + 
							"       B.STATIONLIMINT, A.URGENCY_GRADE,A.ACCEPT_COUNT,B.LOCK_FLAG,\n" + 
							"       D.APPEAL_PROD_NAME, A.REGION_NAME,C.CUST_SERV_GRADE_NAME,B.SHEET_STATU," +
						
							"       DATE_FORMAT(B.HANGUP_START_TIME, '%Y-%m-%d %H:%i:%s') AS HANGUP_START_TIME ,\n" +
							"       DATE_FORMAT(B.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE ,\n" +
							"       DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" +
							"       DATE_FORMAT(B.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE ,B.PRE_ALARM_VALUE,\n" +
							"  (SELECT COUNT(*) FROM CC_SHEET_READ_RECORD R WHERE NOW() < R.READ_END_DATE AND R.WORK_SHEET_ID=B.WORK_SHEET_ID " +
							" AND R.READ_STAFF_ID="+staffId+") AS SHEET_READ "+		
							"  FROM CC_SERVICE_ORDER_ASK   A,\n" + 
							"       CC_WORK_SHEET          B,\n" + 
							"       CC_ORDER_CUST_INFO     C,\n" + 
							"       CC_SERVICE_CONTENT_ASK D\n" + 
							" WHERE B.LOCK_FLAG IN (1, 3)\n" + 
							"   AND A.REGION_ID = B.REGION_ID\n" + 
							"   AND A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" + 
							"   AND A.CUST_GUID = C.CUST_GUID\n" + 
							"   AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID\n" + 
							"   AND A.SERVICE_DATE = 0 ";

		if(!"".equals(strWhere)) {
			//strWhere = strWhere.toUpperCase();
			int andIndex = strWhere.indexOf("AND");
			if(andIndex == -1) {
				if(log.isDebugEnabled()) {
					log.debug("WHERE 条件中没有AND,请检查WHERE条件"+strWhere);
					return null;
				}
			} else {
				strSql=strSql+strWhere;
			}			
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "B");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");		 		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018256");
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY B.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_YN_MYSHEET);
	}
	/**
	 * 疑难工单我的已派发列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GridDataInfo getAlreadySendSheetYn(int begion,String strWhere) {			 	
		String strSql =	"SELECT B.URGENCY_GRADE, B.SERVICE_ORDER_ID,B.PROD_NUM, B.SERVICE_TYPE_DESC, B.ACCEPT_COME_FROM_DESC,\n" +
						"       C.SHEET_STATU_DESC,B.REGION_ID,C.TACHE_DESC,C.TACHE_ID,C.WORK_SHEET_ID,\n" + 
						"       C.SHEET_PRI_VALUE,C.RETURN_STAFF_NAME, C.SHEET_TYPE, B.MONTH_FLAG,C.WORKSHEET_SCHEMA_ID,\n" + 
						"       B.CUST_GUID,C.DEAL_REQUIRE,C.HASTENT_NUM,C.HANGUP_TIME_COUNT,C.DEAL_LIMIT_TIME,B.ORDER_STATU_DESC,\n" + 
						"       C.SHEET_TYPE_DESC,B.ORDER_LIMIT_TIME,C.STATIONLIMINT, C.PRE_ALARM_VALUE, B.ACCEPT_COUNT,\n" + 
						"       B.URGENCY_GRADE_DESC, A.APPEAL_PROD_NAME,B.REGION_NAME, D.CUST_SERV_GRADE_NAME, C.LOCK_FLAG,\n" + 
						"       DATE_FORMAT(C.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE ,\n" +
						"       DATE_FORMAT(B.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE ,\n" +
						"       DATE_FORMAT(C.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE\n" +
						
						"  FROM CC_SERVICE_CONTENT_ASK A,\n" + 
						"       CC_SERVICE_ORDER_ASK   B,\n" + 
						"       CC_WORK_SHEET          C,\n" + 
						"       CC_ORDER_CUST_INFO     D\n" + 
						" WHERE C.LOCK_FLAG = 0\n" + 
						"   AND B.REGION_ID = C.REGION_ID\n" + 
						"   AND B.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID\n" + 
						"   AND B.CUST_GUID = D.CUST_GUID\n" + 
						"   AND B.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID\n" + 
						"   AND B.SERVICE_DATE = 0\n" + 
						"   AND C.SHEET_STATU = 600000450";
		if(!"".equals(strWhere)) {
			//strWhere = strWhere.toUpperCase();
			int andIndex = strWhere.indexOf("AND");
			if(andIndex == -1) {
				if(log.isDebugEnabled()) {
					log.debug("WHERE 条件中没有AND,请检查WHERE条件"+strWhere);
					return null;
				}
			} else {
				strSql=strSql+strWhere;
			}			
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "C");
		tableMap.put("CC_SERVICE_ORDER_ASK", "B");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "A");
		tableMap.put("CC_ORDER_CUST_INFO", "D");		 		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018308");
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY C.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_YN_ALREADYSHEET);
	}
	
	//=====================================
	/**
	 * @return jdbcTemplate
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	/**
	 * @param jdbcTemplate 要设置的 jdbcTemplate
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	/**
	 * @return systemAuthorization
	 */
	public ISystemAuthorization getSystemAuthorization() {
		return systemAuthorization;
	}
	/**
	 * @param systemAuthorization 要设置的 systemAuthorization
	 */
	public void setSystemAuthorization(ISystemAuthorization systemAuthorization) {
		this.systemAuthorization = systemAuthorization;
	}
	/**
	 * @return yzjt
	 */
	public JdbcTemplate getYzjt() {
		return yzjt;
	}
	/**
	 * @param yzjt 要设置的 yzjt
	 */
	public void setYzjt(JdbcTemplate yzjt) {
		this.yzjt = yzjt;
	}
	/**
	 * @return dbgridDataPub
	 */
	public IdbgridDataPub getDbgridDataPub() {
		return dbgridDataPub;
	}
	/**
	 * @param dbgridDataPub 要设置的 dbgridDataPub
	 */
	public void setDbgridDataPub(IdbgridDataPub dbgridDataPub) {
		this.dbgridDataPub = dbgridDataPub;
	}
	
	
}
