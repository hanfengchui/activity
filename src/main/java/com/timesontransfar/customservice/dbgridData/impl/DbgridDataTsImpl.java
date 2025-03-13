/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IStaffPermit;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.dbgridData.IdbgridDataTs;

/**
 * @author 万荣伟
 */
@Component(value="dbgridDataTs")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DbgridDataTsImpl implements IdbgridDataTs {
	private static final Logger log = LoggerFactory.getLogger(DbgridDataTsImpl.class);
	
	@Autowired
	private ISystemAuthorization systemAuthorization;
	
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	
	@Autowired
	private PubFunc pubFunc;

	@Autowired
	private JdbcTemplate jt;

	@Autowired
	private RedisUtils redisUtils;
	
	public GridDataInfo getSheetPoolSJ(int begion,String strWhere) {
		String strSql = 
			"SELECT CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID      ," +
			"       CC_SERVICE_ORDER_ASK.PROD_NUM              ," + 
			"       CC_SERVICE_ORDER_ASK.SERVICE_TYPE_DESC     ," + 
			"       DATE_FORMAT(CC_SERVICE_ORDER_ASK.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE," +
			"       DATE_FORMAT(CC_WORK_SHEET.CREAT_DATE,'%Y-%m-%d %H:%i:%s') AS CREAT_DATE," +
			"      (CASE " +
			"          WHEN CC_WORK_SHEET.SHEET_TYPE = 700001002 THEN " + 
			"           ROUND(TIMESTAMPDIFF(SECOND,CC_WORK_SHEET.CREAT_DATE,NOW())/60) -  CC_WORK_SHEET.HANGUP_TIME_COUNT " +
			"         ELSE " + 
			"           ROUND(TIMESTAMPDIFF(SECOND,CC_WORK_SHEET.LOCK_DATE,NOW())/60) -  CC_WORK_SHEET.HANGUP_TIME_COUNT " +
			"       END) DEAL_DATE, " + 
			"       CC_SERVICE_ORDER_ASK.ACCEPT_COME_FROM_DESC ," + 
			"       CC_SERVICE_ORDER_ASK.URGENCY_GRADE_DESC    ," + 
			"       CC_WORK_SHEET.REGION_ID                    ," + 
			"       CC_WORK_SHEET.TACHE_DESC                   ," + 
			"       CC_WORK_SHEET.TACHE_ID                     ," + 
			"       CC_WORK_SHEET.WORK_SHEET_ID                ," + 
			"       CC_SERVICE_ORDER_ASK.CUST_GUID             ," + 
			"       CC_WORK_SHEET.DEAL_REQUIRE                 ," + 
			"       CC_WORK_SHEET.WORKSHEET_SCHEMA_ID          ," + 
			"       CC_SERVICE_ORDER_ASK.MONTH_FLAG            ," + 
			"       CC_WORK_SHEET.DEAL_LIMIT_TIME              ," + 
			"       CC_WORK_SHEET.HANGUP_TIME_COUNT            ," + 
			"       CC_WORK_SHEET.SHEET_PRI_VALUE              ," + 
			"       CC_WORK_SHEET.SHEET_STATU_DESC             ," + 
			"       CC_WORK_SHEET.HASTENT_NUM                  ," + 
			"       CC_SERVICE_ORDER_ASK.ORDER_STATU_DESC      ," + 
			"       CC_WORK_SHEET.PRE_ALARM_VALUE              ," + 
			"       CC_SERVICE_ORDER_ASK.ACCEPT_COUNT          ," + 
			"       CC_SERVICE_CONTENT_ASK.APPEAL_PROD_NAME    ," + 
			"        CC_WORK_SHEET.SHEET_TYPE_DESC              ," + 
			"       CC_ORDER_CUST_INFO.CUST_SERV_GRADE_NAME    AS CUST_SERV_GRADE_NAME," + 
			"       CC_SERVICE_ORDER_ASK.REGION_NAME           AS REGION_NAME," + 
			"       CC_SERVICE_ORDER_ASK.RELA_INFO             AS RELA_INFO," + 
			"       CC_ORDER_CUST_INFO.CUST_NAME               AS CUST_NAME," + 
			"       CC_WORK_SHEET.SHEET_TYPE                   AS SHEET_TYPE," + 
			"       CC_WORK_SHEET.LOCK_DATE                    AS LOCK_DATE," + 
			"       CC_WORK_SHEET.RETURN_STAFF_NAME            AS RETURN_STAFF_NAME," + 
			"       CC_SERVICE_ORDER_ASK.ASSIST_SELL_NO        AS ASSIST_SELL_NO," + 
			"       CC_SERVICE_CONTENT_ASK.APPEAL_REASON_DESC  AS APPEAL_REASON_DESC" + 
			"  FROM CC_SERVICE_ORDER_ASK, " + 
			"       CC_WORK_SHEET, " + 
			"       CC_ORDER_CUST_INFO, " + 
			"       CC_SERVICE_CONTENT_ASK " + 
			" WHERE CC_SERVICE_ORDER_ASK.REGION_ID = CC_WORK_SHEET.REGION_ID " + 
			"   AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID =  CC_WORK_SHEET.SERVICE_ORDER_ID" + 
			"   AND CC_ORDER_CUST_INFO.REGION_ID = CC_SERVICE_ORDER_ASK.REGION_ID " + 
			"   AND CC_ORDER_CUST_INFO.CUST_GUID = CC_SERVICE_ORDER_ASK.CUST_GUID " + 
			"   AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID = CC_SERVICE_CONTENT_ASK.SERVICE_ORDER_ID" + 
			"   AND CC_SERVICE_ORDER_ASK.SERVICE_DATE = 1" + 
			"   AND CC_SERVICE_ORDER_ASK.SERVICE_TYPE != '400000118'" ;
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "CC_WORK_SHEET");
		tableMap.put("CC_SERVICE_ORDER_ASK", "CC_SERVICE_ORDER_ASK");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "CC_SERVICE_CONTENT_ASK");
		tableMap.put("CC_ORDER_CUST_INFO", "CC_ORDER_CUST_INFO");		 		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018259");
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY CASE CC_SERVICE_CONTENT_ASK.APPEAL_REASON_DESC WHEN '天翼新入网' THEN '1' ELSE '2' END, CC_WORK_SHEET.CREAT_DATE ASC ", DbgridStatic.GRID_FUNID_SJGRID);
	}
	
	/**
	 * 取得受理页面查询历史信息
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 */
	public GridDataInfo getAcceptHistory(int begion, String strWhere) {
		String strSql = "SELECT * FROM ("
				+ " SELECT ifnull(b.hotline_flag, 0) HOTLINE_FLAG, CEIL(TIMESTAMPDIFF(SECOND, a.accept_date, NOW())/3600) HOTLINE_HOUR, a.SERVICE_ORDER_ID,"
				+ " PROD_NUM, DATE_FORMAT(a.accept_date,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE, a.SERVICE_TYPE, a.SERVICE_TYPE_DESC, a.ORDER_STATU, a.ORDER_STATU_DESC,"
				+ " a.ACCEPT_STAFF_NAME, a.ACCEPT_ORG_NAME, a.MONTH_FLAG, a.REGION_ID, a.CUST_GUID, a.ACCEPT_CHANNEL_ID, a.ORDER_VESION, if(b.FINAL_OPTION_FLAG is null, '否', '是') FINAL_OPTION_FLAG"
				+ " FROM cc_service_order_ask a"
				+ " left join cc_service_label b"
				+ " on a.service_order_id = b.service_order_id where a.accept_date >= date_sub(now(),interval 3 month) " + strWhere
				+ " UNION ALL "
				+ " SELECT ifnull(b.hotline_flag, 0) HOTLINE_FLAG, CEIL(TIMESTAMPDIFF(SECOND, a.accept_date, NOW())/3600) HOTLINE_HOUR, a.SERVICE_ORDER_ID,"
				+ " PROD_NUM, DATE_FORMAT(a.accept_date,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE, a.SERVICE_TYPE, a.SERVICE_TYPE_DESC, a.ORDER_STATU, a.ORDER_STATU_DESC,"
				+ " a.ACCEPT_STAFF_NAME, a.ACCEPT_ORG_NAME, a.MONTH_FLAG, a.REGION_ID, a.CUST_GUID, a.ACCEPT_CHANNEL_ID, a.ORDER_VESION, if(b.FINAL_OPTION_FLAG is null, '否', '是') FINAL_OPTION_FLAG"
				+ " FROM cc_service_order_ask_his a"
				+ " left join cc_service_label_his b"
				+ " on a.service_order_id = b.service_order_id where a.accept_date >= date_sub(now(),interval 3 month) " + strWhere
				+ " AND a.order_statu IN(700000103, 720130010)"
				+ ") AS RT WHERE 1 = 1 ";
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY accept_date desc", DbgridStatic.GRID_FUNID_ACCEPT_HISTORY);
	}

	/**
	 * 取得申诉确认列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getAppealSheet(int begion,String strWhere) {	
		int staffId=0;
		IStaffPermit staffPermit = dbgridDataPub.getStaffPermit();
		staffId = Integer.parseInt(staffPermit.getStaff().getId());
		
		String strSql = "SELECT W.SERVICE_ORDER_ID,\n" +
						"       W.WORK_SHEET_ID,\n" + 
						"       W.TACHE_DESC,\n" + 						
						"       A.ORDER_STATU,\n" + 
						"       A.ORDER_STATU_DESC,\n" + 
						"       A.PROD_NUM,\n" + 
						"       DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" +
						"       DATE_FORMAT(W.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
						"       A.ACCEPT_CHANNEL_ID,\n" + 
						"       A.ACCEPT_COME_FROM_DESC,\n" + 
						"       W.SHEET_STATU_DESC,\n" + 
						"       W.HASTENT_NUM,\n" + 
						"       A.ACCEPT_COUNT,\n" + 
						"       W.RECEIVE_STAFF_NAME,\n" + 						
						"       A.SERVICE_TYPE_DESC,\n" + 						
						"       W.HANGUP_TIME_COUNT,\n" + 
						"       W.DEAL_LIMIT_TIME,\n" + 
						"       W.PRE_ALARM_VALUE,\n" + 
						"       W.SHEET_TYPE_DESC,\n" + 
						"       W.REPORT_NUM,\n" + 
						"       A.URGENCY_GRADE,\n" + 						
						"       A.URGENCY_GRADE_DESC,\n" + 						
						"       D.APPEAL_PROD_NAME,\n" + 
						"       C.CUST_SERV_GRADE_NAME,\n" + 
						"       A.REGION_NAME,\n" + 
						"       W.SHEET_TYPE,\n" + 						
						"       A.ACCEPT_COME_FROM,\n" + 						
						"       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE   ,\n" +
						"       W.SHEET_STATU,\n" +                        			
						"       DATE_FORMAT(W.HANGUP_START_TIME, '%Y-%m-%d %H:%i:%s') AS HANGUP_START_TIME   ,\n" +
						"       W.RETURN_STAFF_NAME,\n" + 
						"       W.REGION_ID,W.MONTH_FLAG,\n" +							
						"  (SELECT COUNT(*) FROM CC_SHEET_READ_RECORD R WHERE NOW() < R.READ_END_DATE AND R.WORK_SHEET_ID=W.WORK_SHEET_ID " +
						" AND R.READ_STAFF_ID="+staffId+") AS SHEET_READ "+						
						" FROM CC_WORK_SHEET W, CC_SERVICE_ORDER_ASK A ," +
						" CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C\n" + 
						" WHERE W.REGION_ID = A.REGION_ID\n" + 						
						" AND A.SERVICE_DATE = 3" +						
						" AND W.SHEET_STATU = 3000026 " + //申诉确认
						" AND  date_sub(now(),interval 3 day) <= W.creat_date"+
						" AND W.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID " +
						" AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID" +
						" AND A.CUST_GUID = C.CUST_GUID" ;
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018313");//申诉确认实体
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_VERIFY_SHEET);
	}
	
	/**
	 * 取得申诉超时列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getAppealTimeOutSheet(int begion,String strWhere) {	
		int staffId=0;
		IStaffPermit staffPermit = dbgridDataPub.getStaffPermit();
		staffId = Integer.parseInt(staffPermit.getStaff().getId());
		
		String strSql = "SELECT W.SERVICE_ORDER_ID,\n" +
						"       W.WORK_SHEET_ID,\n" + 
						"       W.TACHE_DESC,\n" + 						
						"       A.ORDER_STATU,\n" + 
						"       A.ORDER_STATU_DESC,\n" + 
						"       A.PROD_NUM,\n" + 
						"       DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" +
						"       DATE_FORMAT(W.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
						"       A.ACCEPT_CHANNEL_ID,\n" + 
						"       A.ACCEPT_COME_FROM_DESC,\n" + 
						"       W.SHEET_STATU_DESC,\n" + 
						"       W.HASTENT_NUM,\n" + 
						"       A.ACCEPT_COUNT,\n" + 
						"       W.RECEIVE_STAFF_NAME,\n" + 						
						"       A.SERVICE_TYPE_DESC,\n" + 						
						"       W.HANGUP_TIME_COUNT,\n" + 
						"       W.DEAL_LIMIT_TIME,\n" + 
						"       W.PRE_ALARM_VALUE,\n" + 
						"       W.SHEET_TYPE_DESC,\n" + 
						"       W.REPORT_NUM,\n" + 
						"       A.URGENCY_GRADE,\n" + 						
						"       A.URGENCY_GRADE_DESC,\n" + 						
						"       D.APPEAL_PROD_NAME,\n" + 
						"       C.CUST_SERV_GRADE_NAME,\n" + 
						"       A.REGION_NAME,\n" + 
						"       W.SHEET_TYPE,\n" + 						
						"       A.ACCEPT_COME_FROM,\n" + 						
						"       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE   ,\n" +
						"       W.SHEET_STATU,\n" +                        			
						"       DATE_FORMAT(W.HANGUP_START_TIME, '%Y-%m-%d %H:%i:%s') AS HANGUP_START_TIME   ,\n" +
						"       W.RETURN_STAFF_NAME,\n" + 
						"       W.REGION_ID,W.MONTH_FLAG,\n" +							
						"  (SELECT COUNT(*) FROM CC_SHEET_READ_RECORD R WHERE NOW() < R.READ_END_DATE AND R.WORK_SHEET_ID=W.WORK_SHEET_ID " +
						" AND R.READ_STAFF_ID="+staffId+") AS SHEET_READ "+						
						" FROM CC_WORK_SHEET W, CC_SERVICE_ORDER_ASK A ," +
						" CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C\n" + 
						" WHERE W.REGION_ID = A.REGION_ID\n" + 						
						" AND A.SERVICE_DATE = 3" +
						" AND W.SHEET_STATU = 3000026 AND W.CREAT_DATE <= date_sub(now(),interval 3 day)" +
						" AND W.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID " +
						" AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID" +
						" AND A.CUST_GUID = C.CUST_GUID" ;
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");		 		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018314");//申诉超时实体
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_APPEAL_SHEET);
	}
	
	/**
	 * 取得待考核列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getWaitQualifySheet(int begion,String strWhere) {	
		int staffId=0;
		IStaffPermit staffPermit = dbgridDataPub.getStaffPermit();
		staffId = Integer.parseInt(staffPermit.getStaff().getId());
		
		String strSql = "SELECT W.SERVICE_ORDER_ID,\n" +
						"       W.WORK_SHEET_ID,\n" + 
						"       W.TACHE_DESC,\n" + 						
						"       A.ORDER_STATU,\n" + 
						"       A.ORDER_STATU_DESC,\n" + 
						"       A.PROD_NUM,\n" + 
						"       DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" +
						"       DATE_FORMAT(W.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
						"       A.ACCEPT_CHANNEL_ID,\n" + 
						"       A.ACCEPT_COME_FROM_DESC,\n" + 
						"       W.SHEET_STATU_DESC,\n" + 
						"       W.HASTENT_NUM,\n" + 
						"       A.ACCEPT_COUNT,\n" + 
						"       W.RECEIVE_STAFF_NAME,\n" +		
						"       A.SERVICE_TYPE_DESC,\n" + 						
						"       W.HANGUP_TIME_COUNT,\n" + 
						"       W.DEAL_LIMIT_TIME,\n" + 
						"       W.PRE_ALARM_VALUE,\n" + 
						"       W.SHEET_TYPE_DESC,\n" + 					
						"       A.URGENCY_GRADE,\n" + 						
						"       A.URGENCY_GRADE_DESC,\n" + 	
						"       W.REPORT_NUM,\n" + 
						"       D.APPEAL_PROD_NAME,\n" + 
						"       C.CUST_SERV_GRADE_NAME,\n" + 
						"       A.REGION_NAME,\n" + 
						"       W.SHEET_TYPE,\n" + 						
						"       A.ACCEPT_COME_FROM,\n" + 						
						"       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE   ,\n" +
						"       W.SHEET_STATU,\n" +                        			
						"       DATE_FORMAT(W.HANGUP_START_TIME, '%Y-%m-%d %H:%i:%s') AS HANGUP_START_TIME   ,\n" +
						"       W.RETURN_STAFF_NAME,\n" + 
						"       W.REGION_ID,W.MONTH_FLAG,\n" +							
						"  (SELECT COUNT(*) FROM CC_SHEET_READ_RECORD R WHERE NOW() < R.READ_END_DATE AND R.WORK_SHEET_ID=W.WORK_SHEET_ID " +
						" AND R.READ_STAFF_ID="+staffId+") AS SHEET_READ "+
						" FROM CC_WORK_SHEET W, CC_SERVICE_ORDER_ASK A ," +
						" CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C\n" + 
						" WHERE W.REGION_ID = A.REGION_ID\n" + 						
						" AND A.SERVICE_DATE = 3" +
						" AND W.LOCK_FLAG = 0" +
						" AND W.SHEET_TYPE = 700001000 " + //考核单
						" AND W.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID " +
						" AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID" +
						" AND A.CUST_GUID = C.CUST_GUID" ;
						
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");		 		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018317");//投诉考核工单池实体
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_WAITQUALIFY_SHEET);
	}
	
	/**
	 * 取得考核中列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getQualifingSheet(int begion,String strWhere) {	
		int staffId=0;
		IStaffPermit staffPermit = dbgridDataPub.getStaffPermit();
		staffId = Integer.parseInt(staffPermit.getStaff().getId());
		
		String strSql = "SELECT W.SERVICE_ORDER_ID,\n" +
						"       W.WORK_SHEET_ID,\n" + 
						"       W.TACHE_DESC,\n" + 						
						"       A.ORDER_STATU,\n" + 
						"       A.ORDER_STATU_DESC,\n" + 
						"       A.PROD_NUM,\n" + 
						"       DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" +
						"       DATE_FORMAT(W.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
						"       A.ACCEPT_CHANNEL_ID,\n" + 
						"       A.ACCEPT_COME_FROM_DESC,\n" + 
						"       W.SHEET_STATU_DESC,\n" + 
						"       W.HASTENT_NUM,\n" + 
						"       A.ACCEPT_COUNT,\n" + 
						"       W.RECEIVE_STAFF_NAME,\n" + 						
						"       A.SERVICE_TYPE_DESC,\n" + 						
						"       W.HANGUP_TIME_COUNT,\n" + 
						"       W.DEAL_LIMIT_TIME,\n" + 
						"       W.PRE_ALARM_VALUE,\n" + 
						"       W.SHEET_TYPE_DESC,\n" + 					
						"       A.URGENCY_GRADE,\n" + 						
						"       A.URGENCY_GRADE_DESC,\n" + 						
						"       D.APPEAL_PROD_NAME,\n" + 
						"       W.REPORT_NUM,\n" + 
						"       C.CUST_SERV_GRADE_NAME,\n" + 
						"       A.REGION_NAME,\n" + 
						"       W.SHEET_TYPE,\n" + 						
						"       A.ACCEPT_COME_FROM,\n" + 						
						"       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE   ,\n" +
						"       W.SHEET_STATU,\n" +                        			
						"       DATE_FORMAT(W.HANGUP_START_TIME, '%Y-%m-%d %H:%i:%s') AS HANGUP_START_TIME   ,\n" +
						"       W.RETURN_STAFF_NAME,\n" + 
						"       W.REGION_ID,W.MONTH_FLAG,\n" +							
						"  (SELECT COUNT(*) FROM CC_SHEET_READ_RECORD R WHERE NOW() < R.READ_END_DATE AND R.WORK_SHEET_ID=W.WORK_SHEET_ID " +
						" AND R.READ_STAFF_ID="+staffId+") AS SHEET_READ "+
						" FROM CC_WORK_SHEET W, CC_SERVICE_ORDER_ASK A ," +
						" CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C\n" + 
						" WHERE W.REGION_ID = A.REGION_ID\n" + 						
						" AND A.SERVICE_DATE = 3" +
						" AND W.SHEET_TYPE = 700001000 " + //考核单
						" AND W.LOCK_FLAG in (1,3) " +						
						" AND W.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID " +
						" AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID" +
						" AND A.CUST_GUID = C.CUST_GUID" ;
						
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");		
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018301");//我的任务实体
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_QUALIFY_SHEET);
	}

    /**
     * 取得投诉建议工单池列表数据
     * @param begion 开始
     * @param strWhere WHERE条件
     * @return
     */
    public GridDataInfo getWaitDealSheetTs(int begion, int pageSize, String strWhere) {
    	TsmStaff staff = pubFunc.getLogonStaff();

        StringBuffer sql1 = new StringBuffer().append(
"SELECT (SELECT RETURN_CODE FROM CC_COMPLAINT_ORDER_DETAIL WHERE COMPLAINT_ORDER_SN = W.SERVICE_ORDER_ID) RETURN_CODE,"
+ "L.REFUND_FLAG,L.ORDER_TYPE,IFNULL(L.SENSITIVE_NUM,0) SENSITIVE_NUM,(SELECT IS_RUYI FROM CC_RUYI_LABEL WHERE SERVICE_ORDER_ID=L.SERVICE_ORDER_ID) RUYI_FLAG,"
+ "W.RECEIVE_ORG_NAME,W.SERVICE_ORDER_ID,W.SHEET_RECEIVE_DATE,W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.PROD_NUM,"
+ "A.ACCEPT_DATE,W.CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,W.SHEET_STATU_DESC,(SELECT COUNT(1)FROM CC_HASTEN_SHEET HS WHERE "
+ "HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID)HASTENT_NUM,A.ACCEPT_COUNT,W.RECEIVE_STAFF_NAME,A.SERVICE_TYPE_DESC,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,"
+ "W.PRE_ALARM_VALUE,W.SHEET_TYPE_DESC,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,D.APPEAL_PROD_NAME,C.CUST_SERV_GRADE_NAME,A.REGION_NAME,W.SHEET_TYPE,"
+ "A.ACCEPT_COME_FROM,W.LOCK_DATE,W.SHEET_STATU,W.HANGUP_START_TIME,W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,W.PRECONTRACT_SIGN,W.TACHE_ID,"
+ "A.SEND_TO_ORG_NAME,W.MAIN_SHEET_FLAG,W.RESPOND_DATE,A.SERVICE_TYPE,D.APPEAL_REASON_ID,C.HIGH_WARN_ID,IF(W.SHEET_TYPE NOT IN(700000129,700001002,"
+ "720130015)OR(W.SHEET_TYPE=700000129 AND W.FLOW_SEQUENCE=1),W.RETURN_STAFF_NAME,(SELECT WS1.DEAL_STAFF_NAME FROM CC_WORK_SHEET WS1 WHERE "
+ "WS1.WORK_SHEET_ID=W.SOURCE_SHEET_ID))LAST_DEAL_STAFF,(SELECT COUNT(1)FROM CC_SHEET_READ_RECORD RR WHERE NOW()<RR.READ_END_DATE AND "
+ "RR.WORK_SHEET_ID=W.WORK_SHEET_ID AND RR.READ_STAFF_ID="+staff.getId()+")SHEET_READ,IF(D.APPEAL_REASON_ID IN(11504,11505),5,"
+ "IF(D.APPEAL_PROD_ID=111,6,IF(A.ACCEPT_CHANNEL_ID=707907012,7,IF(A.ACCEPT_CHANNEL_ID=707907011,8,9))))ORD,L.FORCE_PRE_FLAG,L.UPGRADE_INCLINE,"
+ "IFNULL(L.UP_LEVEL,1)UP_LEVEL,IFNULL(L.HOTLINE_FLAG,0)HOTLINE_FLAG,IF(L.REPEAT_MAN_FLAG=0,0,L.REPEAT_AUTO_FLAG)REPEAT_AUTO_FLAG,IF("
+ "L.UPGRADE_MAN_FLAG=0,0,L.UPGRADE_AUTO_FLAG)UPGRADE_AUTO_FLAG,IFNULL(L.REPEAT_NEW_FLAG,0)REPEAT_NEW_FLAG,L.SEC_FLAG,IF(D.APPEAL_PROD_ID=111,1,IF(D.APPEAL_REASON_ID=23002503,1,''))XHZW_FLAG,"
+ "IFNULL(L.ADJUST_ACCOUNT_FLAG,0)ADJUST_ACCOUNT_FLAG,IFNULL(L.DIRECT_DISPATCH_FLAG,0)DIRECT_DISPATCH_FLAG,(SELECT DATE_FORMAT(SA.CREAT_DATE,'%Y-%m-%d %H:%i:%s') FROM "
+ "CC_WORK_SHEET_AREA SA WHERE SA.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND SA.TACHE_DATE IS NULL AND SA.AREA_FLAG=IF(W.TACHE_ID IN(720130021,700000085),"
+ "3,IF(W.TACHE_ID IN(720130023,700000086),1,0))order by SA.CREAT_DATE DESC LIMIT 1)PD_BEGIN_DATE,IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN"
+ "(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE))BEGIN_DATE,IF(W.SHEET_STATU IN(700000046,720130035),W.HANGUP_START_TIME,W.RESPOND_DATE)END_DATE,"
+ "IFNULL((SELECT LP1.PD_MINUTES FROM "
+ "CC_SHEET_LIMITTIME_PD LP1 WHERE LP1.TACHE_ID=W.TACHE_ID AND LP1.COME_CATEGORY=A.COME_CATEGORY AND LP1.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDLIMIT,"
+ "IFNULL((SELECT LP2.WARN_MINUTES FROM CC_SHEET_LIMITTIME_PD LP2 WHERE LP2.TACHE_ID=W.TACHE_ID AND LP2.COME_CATEGORY=A.COME_CATEGORY AND "
+ "LP2.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDWARN,(SELECT IFNULL(SUM(is_finish),-1) FROM cc_xc_flow WHERE main_sheet_id=w.work_sheet_id)XC_ING_NUM,"
+ "C.CUST_TYPE,D.APPEAL_PROD_ID,IFNULL(L.UP_TENDENCY_FLAG,0)UP_TENDENCY_FLAG,D.SIX_GRADE_CATALOG,A.RELA_INFO,D.FIVE_ORDER,D.FIVE_ORDER_DESC,D.BEST_ORDER,D.BEST_ORDER_DESC"
+ ",D.PROD_ONE,D.PROD_ONE_DESC,D.PROD_TWO,D.PROD_TWO_DESC,D.DEVT_CHS_ONE,D.DEVT_CHS_ONE_DESC,D.DEVT_CHS_TWO,D.DEVT_CHS_TWO_DESC,D.DEVT_CHS_THREE,D.DEVT_CHS_THREE_DESC,A.RELA_TYPE"
+ " FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A,"
+ "CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND "
+ "A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND A.SERVICE_DATE=3 AND W.LOCK_FLAG=0 "
+ "AND W.SHEET_STATU NOT IN(600000450,720130033)AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025");

        String overtimeWhere = "";
        if (!"".equals(strWhere)) {
            String[] whereArr = strWhere.split("&&");
            sql1.append(whereArr[0]);
            if (whereArr.length == 2) {
                overtimeWhere = whereArr[1];
            }
        }

        // 权限匹配
        Map tableMap = new HashMap();
        tableMap.put("CC_WORK_SHEET", "W");
        tableMap.put("CC_SERVICE_ORDER_ASK", "A");
        tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
        tableMap.put("CC_ORDER_CUST_INFO", "C");
        String s1 = this.systemAuthorization.getAuthedSql(tableMap, sql1.toString(), "900018300");

        StringBuffer inner = new StringBuffer()
        		.append("SELECT J.* FROM (")
                .append(s1)
                .append(") J order by J.SENSITIVE_NUM DESC, J.UP_TENDENCY_FLAG DESC, J.ORD ASC, J.CREAT_DATE ASC");
        StringBuffer outer = new StringBuffer().append(
"SELECT SERVICE_ORDER_ID,RECEIVE_ORG_NAME,WORK_SHEET_ID,TACHE_ID,TACHE_DESC,ADJUST_ACCOUNT_FLAG,DIRECT_DISPATCH_FLAG,ORDER_STATU,ORDER_STATU_DESC,"
+ "PROD_NUM,DATE_FORMAT(ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,ACCEPT_CHANNEL_ID,"
+ "ACCEPT_COME_FROM,ACCEPT_COME_FROM_DESC,SHEET_STATU,SHEET_STATU_DESC,HASTENT_NUM,ACCEPT_COUNT,RECEIVE_STAFF_NAME,SERVICE_TYPE_DESC,"
+ "HANGUP_TIME_COUNT,DEAL_LIMIT_TIME,PRE_ALARM_VALUE,SHEET_TYPE,SHEET_TYPE_DESC,HIGH_WARN_ID,URGENCY_GRADE,URGENCY_GRADE_DESC,APPEAL_PROD_NAME,"
+ "CUST_SERV_GRADE_NAME,REGION_NAME,DATE_FORMAT(LOCK_DATE,'%Y-%m-%d %H:%i:%s')LOCK_DATE,DATE_FORMAT(HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')"
+ "HANGUP_START_TIME,RETURN_STAFF_NAME,REGION_ID,MONTH_FLAG,PRECONTRACT_SIGN,SEND_TO_ORG_NAME,MAIN_SHEET_FLAG,LAST_DEAL_STAFF,FORCE_PRE_FLAG,"
+ "REPEAT_AUTO_FLAG,UPGRADE_AUTO_FLAG,REPEAT_NEW_FLAG,PDLIMIT,PDWARN,SHEET_READ,UPGRADE_INCLINE,UP_LEVEL,HOTLINE_FLAG,SEC_FLAG,XHZW_FLAG,SERVICE_TYPE,"
+ "XC_ING_NUM,CUST_TYPE,APPEAL_PROD_ID,APPEAL_REASON_ID,UP_TENDENCY_FLAG,SIX_GRADE_CATALOG,RELA_INFO,FIVE_ORDER,FIVE_ORDER_DESC,BEST_ORDER,BEST_ORDER_DESC,"
+ "PROD_ONE,PROD_ONE_DESC,PROD_TWO,PROD_TWO_DESC,DEVT_CHS_ONE,DEVT_CHS_ONE_DESC,DEVT_CHS_TWO,DEVT_CHS_TWO_DESC,DEVT_CHS_THREE,DEVT_CHS_THREE_DESC,RELA_TYPE,"
+ "PD_BEGIN_DATE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,BEGIN_DATE,END_DATE,RUYI_FLAG,ORDER_TYPE,RETURN_CODE,REFUND_FLAG,SENSITIVE_NUM");
        log.info("inner: \n{}",inner);
        log.info("outer: \n{}",outer);
        GridDataInfo infos = this.dbgridDataPub.getResultByW(inner.toString(), outer.toString(), overtimeWhere, begion, pageSize, DbgridStatic.GRID_FUNID_TS_SHEET);
        return infos;
    }



	/**
	 * 获取redis数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo queryValue(int begion, int pageSize, String strWhere) {
		StringBuffer sql1 = new StringBuffer().append("SELECT REDIS_KEY_PRE as redisKeyPre,REDIS_VALUE_DESC as redisValueDesc,EXPIRED_TIME as expiredTime FROM cc_redis_list ORDER BY REDIS_VALUE_DESC");
		String sql = sql1.toString();
		return this.dbgridDataPub.getResultBySize(sql, begion, pageSize, "", DbgridStatic.GRID_FUNID_TS_MYSHEET);
	}


	/**
	 * 删除redis数据
	 * @param redisKeys
	 * @return
	 */
	public int deleteKey(List<String> redisKeys) {
		int num = 0;
		for (String redisKey : redisKeys) {
			//删除redis数据
			int size = redisUtils.batchDelByPreKey(redisKey, RedisType.WORKSHEET).intValue();
			num += size;
		}
		return num;
	}


	/**
	 * 删除redis数据
	 * @param newRowData
	 * @return
	 */
	public int addRow(Map<String, String> newRowData) {
		String redisKeyPre = newRowData.get("redisKeyPre");
		String redisValueDesc = newRowData.get("redisValueDesc");
		String expiredTime = newRowData.get("expiredTime");
		//新增该条数据
		StringBuffer sql1 = new StringBuffer().append("INSERT INTO cc_redis_list (REDIS_KEY_PRE,REDIS_VALUE_DESC,EXPIRED_TIME) VALUES ('"+redisKeyPre+"','"+redisValueDesc+"','"+expiredTime+"')" );
		String sql = sql1.toString();
		int update = this.jt.update(sql);
		return update;
	}

	@Override
	public int getApportion(String staffId) {
		int apportionStatus = -1;
		String sqlForStaff = "select count(*) from cc_order_apportion_staff b where b.STAFF_ID = ? and b.STAFF_STATUS = 1";
		int integer = this.jt.queryForObject(sqlForStaff, new Object[]{staffId}, Integer.class);
		if (integer == 0){
			String sql = "select count(*) from cc_order_apportion a where a.STAFF_ID = ? and a.APPORTION_STATUS = 0 ";
			apportionStatus = this.jt.queryForObject(sql, new Object[]{staffId}, Integer.class);
		}
		return apportionStatus;
	}

	@Override
	public GridDataInfo getApportionData(String staffId, String orgId, String status, String appStatus, int begin, int pageSize) {
		String strWhere = "";
		StringBuffer sb = new StringBuffer();
		if (StringUtils.isNotEmpty(staffId)) {
			sb.append(" AND s.LOGONNAME = " + staffId);
		}
		if (StringUtils.isNotEmpty(orgId)) {
			sb.append(" AND o.ORG_ID = " + orgId);
		}
		if (StringUtils.isNotEmpty(status)) {
			if (status.equals("0")) {
				sb.append(" AND (a.STAFF_STATUS is NULL OR a.STAFF_STATUS = 0)");
			} else {
				sb.append(" AND a.STAFF_STATUS = 1");
			}
		}
		if (StringUtils.isNotEmpty(appStatus)) {
			if (appStatus.equals("0")) {
				sb.append(" AND p.APPORTION_STATUS is NULL");
			} else {
				sb.append(" AND p.APPORTION_STATUS = 0");
			}
		}
		strWhere = sb.toString();
		String sql = "SELECT s.LOGONNAME,s.STAFF_ID, s.STAFFNAME, o.ORG_ID, o.ORG_NAME, "
				+ " IF ( IFNULL( a.STAFF_STATUS, 0 ) = 0, '启用', '停用' ) STAFF_STATUS, "
				+ " a.MAX_NUMBER, "
				+ " p.APPORTION_NUMBER, p.REAL_APPORTION_NUMBER, DATE_FORMAT(p.APPORTION_DATE, '%Y-%m-%d %H:%i:%s') AS APPORTION_DATE "
				+ " FROM tsm_staff s "
				+ " LEFT JOIN tsm_organization o ON s.ORG_ID = o.ORG_ID "
				+ " LEFT JOIN cc_order_apportion_staff a ON a.STAFF_ID = s.STAFF_ID "
				+ " LEFT JOIN cc_order_apportion p ON p.STAFF_ID = s.STAFF_ID AND p.APPORTION_STATUS = 0 "
				+ " WHERE o.LINKID LIKE '%10-11-361143%' AND s.STATE = 8 " + strWhere;
		return this.dbgridDataPub.getResultBySize(sql, begin, pageSize, "", "");
	}

	@Override
	public int saveApportion(String staffId, String staffName, String orgId, String orgName, String apportionNumber) {
		String sql = "insert into cc_order_apportion (STAFF_ID,STAFF_NAME,ORG_ID,ORG_NAME,APPORTION_NUMBER,REAL_APPORTION_NUMBER,APPORTION_DATE,MODIFY_DATE,APPORTION_STATUS) " +
				"values (?,?,?,?,?,0,now(),now(),0)";
		return this.jt.update(sql,staffId,staffName,orgId,orgName,apportionNumber);
	}

	@Override
	public String updateApportion(String param) {
		JSONObject json = net.sf.json.JSONObject.fromObject(param);
		JSONArray apportionList = json.optJSONArray("apportionArray");
		log.info("批量修改申请分派状态入参数组：" + apportionList);
		int updateApportion = 0;
		StringBuffer sb = new StringBuffer();
		int size = apportionList.size();
		for (int i = 0; i < size; i++) {
			String sheetInfo = apportionList.get(i).toString();
			log.info("批量修改申请分派状态入参：" + sheetInfo);
			String staffId = sheetInfo.split("@")[0];
			String staffName = sheetInfo.split("@")[1];
			String orgId = sheetInfo.split("@")[2];
			String orgName = sheetInfo.split("@")[3];
			String staffStatus = sheetInfo.split("@")[4];
			String sql = "select count(*) from cc_order_apportion_staff a where a.STAFF_ID = ?";
			Integer apportion = this.jt.queryForObject(sql, new Object[]{staffId}, Integer.class);
			if (apportion == 0) {
				String insertSql = "insert into cc_order_apportion_staff (STAFF_ID,STAFF_NAME,ORG_ID,ORG_NAME,STAFF_STATUS,CREATE_DATE,UPDATE_DATE,MAX_NUMBER) values "
						+ "(?,?,?,?,?,now(),now(),10)";
				updateApportion = this.jt.update(insertSql, staffId, staffName, orgId, orgName, staffStatus);
			} else {
				String updateSql = "update cc_order_apportion_staff o set o.STAFF_STATUS = ?,UPDATE_DATE = now() where o.STAFF_ID = ? ";
				updateApportion = this.jt.update(updateSql, staffStatus, staffId);
				if (staffStatus.equals("1")) {
					String updateApportionSql = "update cc_order_apportion a set a.APPORTION_STATUS = 1,a.MODIFY_DATE = now() where a.STAFF_ID = ? and a.APPORTION_STATUS = 0";
					this.jt.update(updateApportionSql, staffId);
				}
			}
			if (updateApportion == 0) {
				String status = staffStatus.equals("0") ? "停用" : "启用";
				sb.append("员工" + staffName + "（" + staffId + "）" + status + "失败！");
			}
		}
		return sb.toString();
	}

	/**
     * 取得投诉建议我的任务列表数据
     * 
     * @param begion
     *            开始
     * @param strWhere
     *            WHERE条件
     * @return
     */
	public GridDataInfo getDealingSheetTs(int begion, int pageSize, String strWhere) {
        StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% "
+ "FROM CC_WORK_SHEET W LEFT JOIN (SELECT H.WORK_SHEET_ID FROM CC_SHEET_HIDDEN_ACTION H WHERE H.HIDDEN_STATE = 0) HP ON W.WORK_SHEET_ID = HP.WORK_SHEET_ID "
+ "LEFT JOIN CC_SHEET_HIDDEN_INFO HO ON W.WORK_SHEET_ID = HO.WORK_SHEET_ID,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L "
+ "WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
+ "AND HP.WORK_SHEET_ID IS NULL AND A.SERVICE_DATE=3 AND W.LOCK_FLAG IN(1,3) AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025");

        if (!"".equals(strWhere)) {
            int andIndex = strWhere.indexOf("AND");
            if (andIndex == -1) {
                log.warn("WHERE条件中没有AND，请检查WHERE条件 {}", strWhere);
                return null;
            }
            sql1.append(strWhere);
        }
        // 权限匹配
        Map tableMap = new HashMap();
        tableMap.put("CC_WORK_SHEET", "W");
        tableMap.put("CC_SERVICE_ORDER_ASK", "A");
        tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
        tableMap.put("CC_ORDER_CUST_INFO", "C");
        String s1 = this.systemAuthorization.getAuthedSql(tableMap, sql1.toString(), "900018301");
        
        TsmStaff staff = pubFunc.getLogonStaff();
        String sql = s1.replace("%PARAM%", "(SELECT RETURN_CODE FROM CC_COMPLAINT_ORDER_DETAIL WHERE COMPLAINT_ORDER_SN = W.SERVICE_ORDER_ID) RETURN_CODE,"
        		+ "L.REFUND_FLAG,L.ORDER_TYPE,IFNULL(L.SENSITIVE_NUM,0) SENSITIVE_NUM,(SELECT IS_RUYI FROM CC_RUYI_LABEL WHERE SERVICE_ORDER_ID=L.SERVICE_ORDER_ID)RUYI_FLAG,W.MAIN_SHEET_FLAG,"
        		+ "W.RECEIVE_ORG_NAME,IFNULL((SELECT PD1.PD_MINUTES FROM CC_SHEET_LIMITTIME_PD PD1 WHERE PD1.TACHE_ID=W.TACHE_ID AND "
        		+ "PD1.COME_CATEGORY=A.COME_CATEGORY AND PD1.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDLIMIT,IFNULL((SELECT PD2.WARN_MINUTES FROM CC_SHEET_LIMITTIME_PD "
        		+ "PD2 WHERE PD2.TACHE_ID=W.TACHE_ID AND PD2.COME_CATEGORY=A.COME_CATEGORY AND PD2.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDWARN,W.SERVICE_ORDER_ID,"
        		+ "W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.COME_CATEGORY,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
        		+ "DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,W.SHEET_STATU_DESC,(SELECT COUNT(1)FROM "
        		+ "CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID)HASTENT_NUM,A.ACCEPT_COUNT,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.PRE_ALARM_VALUE,"
        		+ "W.RECEIVE_STAFF_NAME,W.SHEET_TYPE_DESC,A.SERVICE_TYPE_DESC,A.SOURCE_NUM,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')"
        		+ "LOCK_DATE,W.SHEET_STATU,A.SERVICE_TYPE,W.PRECONTRACT_SIGN,W.TACHE_ID,DATE_FORMAT(W.HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,"
        		+ "A.ACCEPT_COME_FROM,W.SHEET_TYPE,D.APPEAL_PROD_NAME,C.CUST_SERV_GRADE_NAME,C.HIGH_WARN_ID,A.REGION_NAME,A.ACCEPT_ORG_ID,A.ACCEPT_ORG_NAME,A.SEND_TO_ORG_NAME,"
        		+ "W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,D.SIX_GRADE_CATALOG,(SELECT COUNT(1)FROM CC_SHEET_READ_RECORD RR WHERE NOW()<RR.READ_END_DATE AND "
        		+ "RR.WORK_SHEET_ID=W.WORK_SHEET_ID AND RR.READ_STAFF_ID="+staff.getId()+")SHEET_READ,CASE WHEN W.SHEET_TYPE NOT IN"
        		+ "(700000129,700001002,720130015)OR(W.SHEET_TYPE=700000129 AND W.FLOW_SEQUENCE=1)THEN W.RETURN_STAFF_NAME ELSE(SELECT WS1.DEAL_STAFF_NAME FROM "
        		+ "CC_WORK_SHEET WS1 WHERE WS1.WORK_SHEET_ID=W.SOURCE_SHEET_ID)END LAST_DEAL_STAFF,IF(D.APPEAL_REASON_ID IN(11504,11505),5,IF(D.APPEAL_PROD_ID=111,6,"
        		+ "IF(A.ACCEPT_CHANNEL_ID=707907012,7,IF(A.ACCEPT_CHANNEL_ID=707907011,8,9))))ORD,(SELECT O.REGION_ID FROM TSM_ORGANIZATION O WHERE O.ORG_ID="
        		+ "A.ACCEPT_ORG_ID)ACCEPTREGION,L.FORCE_PRE_FLAG,IFNULL(L.ADJUST_ACCOUNT_FLAG,0)ADJUST_ACCOUNT_FLAG,IFNULL(L.DIRECT_DISPATCH_FLAG,0)DIRECT_DISPATCH_FLAG,L.UPGRADE_INCLINE,IFNULL(L.UP_LEVEL,1)UP_LEVEL,IFNULL("
        		+ "L.HOTLINE_FLAG,0)HOTLINE_FLAG,IF(L.REPEAT_MAN_FLAG=0,0,L.REPEAT_AUTO_FLAG)REPEAT_AUTO_FLAG,IF(L.UPGRADE_MAN_FLAG=0,0,L.UPGRADE_AUTO_FLAG)"
        		+ "UPGRADE_AUTO_FLAG,IFNULL(L.REPEAT_NEW_FLAG,0)REPEAT_NEW_FLAG,L.SEC_FLAG,IF(D.APPEAL_PROD_ID="
        		+ "111,1,IF(D.APPEAL_REASON_ID=23002503,1,''))XHZW_FLAG,W.RECEIVE_ORG_ID,IF(LENGTH(W.SAVE_DEALCONTEN)>10,CONCAT(SUBSTR(W.SAVE_DEALCONTEN,1,10),'...'),W.SAVE_DEALCONTEN)SAVE_DEALCONTEN,"
        		+ "(SELECT IFNULL(SUM(is_finish),-1) FROM cc_xc_flow WHERE main_sheet_id=w.work_sheet_id)XC_ING_NUM,C.CUST_TYPE,D.APPEAL_PROD_ID,D.APPEAL_REASON_ID,"
        		+ "IFNULL(L.UP_TENDENCY_FLAG,0)UP_TENDENCY_FLAG,D.SERVICE_TYPE_DETAIL,C.CUST_NAME,A.RELA_INFO,D.FIVE_ORDER,D.FIVE_ORDER_DESC,D.BEST_ORDER,D.BEST_ORDER_DESC,"
        		+ "A.RELA_MAN,(SELECT DATE_FORMAT(SA.CREAT_DATE,'%Y-%m-%d %H:%i:%s') FROM CC_WORK_SHEET_AREA SA "
        		+ "WHERE SA.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND SA.TACHE_DATE IS NULL AND SA.AREA_FLAG=IF(W.TACHE_ID IN(720130021,700000085),3,IF(W.TACHE_ID IN"
        		+ "(720130023,700000086),1,0))order by SA.CREAT_DATE DESC LIMIT 1)PD_BEGIN_DATE,DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN"
        		+ "(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),"
        		+ "W.HANGUP_START_TIME,W.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,DATE_FORMAT(A.FINISH_DATE,"
        		+ "'%Y-%m-%d %H:%i:%s')FINISH_DATE,A.HANGUP_TIME_COUNT HANGUP_ORDER_COUNT,A.ORDER_LIMIT_TIME,A.AREA_NAME"
        		+ ",D.PROD_ONE,D.PROD_ONE_DESC,D.PROD_TWO,D.PROD_TWO_DESC,D.DEVT_CHS_ONE,D.DEVT_CHS_ONE_DESC,D.DEVT_CHS_TWO,D.DEVT_CHS_TWO_DESC,D.DEVT_CHS_THREE,D.DEVT_CHS_THREE_DESC,"
        		+ "D.DVLP_CHNL_NM,D.ACCEPT_CONTENT,A.RELA_TYPE,A.CALL_SERIAL_NO,D.APPEAL_REASON_DESC,D.APPEAL_CHILD,D.APPEAL_CHILD_DESC,A.CHANNEL_DETAIL_ID,A.ACCEPT_STAFF_ID,A.ACCEPT_STAFF_NAME,C.PROD_TYPE,HO.HIDDEN_STATE ");
        String countSql = s1.replace("%PARAM%", "COUNT(1)");

        // 2012-09-04 LiJiahui 注意：此句中的order by 不可以写成大写字母ORDER BY
        // 因为getResult并不是对所有的记录排序，而且取出100条再排序 将order by写作小写可以规避该问题
        sql += " order by SENSITIVE_NUM DESC, UP_TENDENCY_FLAG DESC, ORD ASC, W.CREAT_DATE ASC";
        return this.dbgridDataPub.getResultNewBySize(countSql, sql, begion, pageSize, "", DbgridStatic.GRID_FUNID_TS_MYSHEET);
    }


	/**
	 * 取得投诉建议我的任务列表数据（关联查询预约回复）
	 *
	 * @param begion
	 *            开始
	 * @param strWhere
	 *            WHERE条件
	 * @return
	 */
	public GridDataInfo getCallBackDealingSheetTs(int begion, int pageSize, String strWhere) {
		StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% "
				+ "FROM CC_WORK_SHEET W LEFT JOIN (SELECT H.WORK_SHEET_ID FROM CC_SHEET_HIDDEN_ACTION H WHERE H.HIDDEN_STATE = 0) HP ON W.WORK_SHEET_ID = HP.WORK_SHEET_ID,"
				+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L , CC_CALL_BACK_INFO T "
				+ "WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
				+ "AND HP.WORK_SHEET_ID IS NULL AND A.SERVICE_DATE=3 AND W.LOCK_FLAG IN(1,3) AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025 AND A.SERVICE_ORDER_ID=T.SERVICE_ORDER_ID AND T.FLAG = '1'");

		if (!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if (andIndex == -1) {
				log.warn("WHERE条件中没有AND，请检查WHERE条件 {}", strWhere);
				return null;
			}
			sql1.append(strWhere);
		}
		// 权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");
		String s1 = this.systemAuthorization.getAuthedSql(tableMap, sql1.toString(), "900018301");

		TsmStaff staff = pubFunc.getLogonStaff();
		String sql = s1.replace("%PARAM%", "(SELECT RETURN_CODE FROM CC_COMPLAINT_ORDER_DETAIL WHERE COMPLAINT_ORDER_SN = W.SERVICE_ORDER_ID) RETURN_CODE,"
				+ "L.REFUND_FLAG,L.ORDER_TYPE,IFNULL(L.SENSITIVE_NUM,0) SENSITIVE_NUM,(SELECT IS_RUYI FROM CC_RUYI_LABEL WHERE SERVICE_ORDER_ID=L.SERVICE_ORDER_ID)RUYI_FLAG,W.MAIN_SHEET_FLAG,"
				+ "W.RECEIVE_ORG_NAME,IFNULL((SELECT PD1.PD_MINUTES FROM CC_SHEET_LIMITTIME_PD PD1 WHERE PD1.TACHE_ID=W.TACHE_ID AND "
				+ "PD1.COME_CATEGORY=A.COME_CATEGORY AND PD1.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDLIMIT,IFNULL((SELECT PD2.WARN_MINUTES FROM CC_SHEET_LIMITTIME_PD "
				+ "PD2 WHERE PD2.TACHE_ID=W.TACHE_ID AND PD2.COME_CATEGORY=A.COME_CATEGORY AND PD2.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDWARN,W.SERVICE_ORDER_ID,"
				+ "W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.COME_CATEGORY,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
				+ "DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,W.SHEET_STATU_DESC,(SELECT COUNT(1)FROM "
				+ "CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID)HASTENT_NUM,A.ACCEPT_COUNT,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.PRE_ALARM_VALUE,"
				+ "W.RECEIVE_STAFF_NAME,W.SHEET_TYPE_DESC,A.SERVICE_TYPE_DESC,A.SOURCE_NUM,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')"
				+ "LOCK_DATE,W.SHEET_STATU,A.SERVICE_TYPE,W.PRECONTRACT_SIGN,W.TACHE_ID,DATE_FORMAT(W.HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,"
				+ "A.ACCEPT_COME_FROM,W.SHEET_TYPE,D.APPEAL_PROD_NAME,C.CUST_SERV_GRADE_NAME,C.HIGH_WARN_ID,A.REGION_NAME,A.ACCEPT_ORG_ID,A.ACCEPT_ORG_NAME,A.SEND_TO_ORG_NAME,"
				+ "W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,D.SIX_GRADE_CATALOG,(SELECT COUNT(1)FROM CC_SHEET_READ_RECORD RR WHERE NOW()<RR.READ_END_DATE AND "
				+ "RR.WORK_SHEET_ID=W.WORK_SHEET_ID AND RR.READ_STAFF_ID="+staff.getId()+")SHEET_READ,CASE WHEN W.SHEET_TYPE NOT IN"
				+ "(700000129,700001002,720130015)OR(W.SHEET_TYPE=700000129 AND W.FLOW_SEQUENCE=1)THEN W.RETURN_STAFF_NAME ELSE(SELECT WS1.DEAL_STAFF_NAME FROM "
				+ "CC_WORK_SHEET WS1 WHERE WS1.WORK_SHEET_ID=W.SOURCE_SHEET_ID)END LAST_DEAL_STAFF,IF(D.APPEAL_REASON_ID IN(11504,11505),5,IF(D.APPEAL_PROD_ID=111,6,"
				+ "IF(A.ACCEPT_CHANNEL_ID=707907012,7,IF(A.ACCEPT_CHANNEL_ID=707907011,8,9))))ORD,(SELECT O.REGION_ID FROM TSM_ORGANIZATION O WHERE O.ORG_ID="
				+ "A.ACCEPT_ORG_ID)ACCEPTREGION,L.FORCE_PRE_FLAG,IFNULL(L.ADJUST_ACCOUNT_FLAG,0)ADJUST_ACCOUNT_FLAG,IFNULL(L.DIRECT_DISPATCH_FLAG,0)DIRECT_DISPATCH_FLAG,L.UPGRADE_INCLINE,IFNULL(L.UP_LEVEL,1)UP_LEVEL,IFNULL("
				+ "L.HOTLINE_FLAG,0)HOTLINE_FLAG,IF(L.REPEAT_MAN_FLAG=0,0,L.REPEAT_AUTO_FLAG)REPEAT_AUTO_FLAG,IF(L.UPGRADE_MAN_FLAG=0,0,L.UPGRADE_AUTO_FLAG)"
				+ "UPGRADE_AUTO_FLAG,IFNULL(L.REPEAT_NEW_FLAG,0)REPEAT_NEW_FLAG,L.SEC_FLAG,IF(D.APPEAL_PROD_ID="
				+ "111,1,IF(D.APPEAL_REASON_ID=23002503,1,''))XHZW_FLAG,W.RECEIVE_ORG_ID,IF(LENGTH(W.SAVE_DEALCONTEN)>10,CONCAT(SUBSTR(W.SAVE_DEALCONTEN,1,10),'...'),W.SAVE_DEALCONTEN)SAVE_DEALCONTEN,"
				+ "(SELECT IFNULL(SUM(is_finish),-1) FROM cc_xc_flow WHERE main_sheet_id=w.work_sheet_id)XC_ING_NUM,C.CUST_TYPE,D.APPEAL_PROD_ID,D.APPEAL_REASON_ID,"
				+ "IFNULL(L.UP_TENDENCY_FLAG,0)UP_TENDENCY_FLAG,D.SERVICE_TYPE_DETAIL,C.CUST_NAME,A.RELA_INFO,D.FIVE_ORDER,D.FIVE_ORDER_DESC,D.BEST_ORDER,D.BEST_ORDER_DESC,"
				+ "A.RELA_MAN,(SELECT DATE_FORMAT(SA.CREAT_DATE,'%Y-%m-%d %H:%i:%s') FROM CC_WORK_SHEET_AREA SA "
				+ "WHERE SA.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND SA.TACHE_DATE IS NULL AND SA.AREA_FLAG=IF(W.TACHE_ID IN(720130021,700000085),3,IF(W.TACHE_ID IN"
				+ "(720130023,700000086),1,0))order by SA.CREAT_DATE DESC LIMIT 1)PD_BEGIN_DATE,DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN"
				+ "(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),"
				+ "W.HANGUP_START_TIME,W.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,DATE_FORMAT(A.FINISH_DATE,"
				+ "'%Y-%m-%d %H:%i:%s')FINISH_DATE,A.HANGUP_TIME_COUNT HANGUP_ORDER_COUNT,A.ORDER_LIMIT_TIME,A.AREA_NAME"
				+ ",D.PROD_ONE,D.PROD_ONE_DESC,D.PROD_TWO,D.PROD_TWO_DESC,D.DEVT_CHS_ONE,D.DEVT_CHS_ONE_DESC,D.DEVT_CHS_TWO,D.DEVT_CHS_TWO_DESC,D.DEVT_CHS_THREE,D.DEVT_CHS_THREE_DESC,"
				+ "D.DVLP_CHNL_NM,D.ACCEPT_CONTENT,A.RELA_TYPE,A.CALL_SERIAL_NO,D.APPEAL_REASON_DESC,D.APPEAL_CHILD,D.APPEAL_CHILD_DESC,A.CHANNEL_DETAIL_ID,A.ACCEPT_STAFF_ID,A.ACCEPT_STAFF_NAME,C.PROD_TYPE,"
				+" DATE_FORMAT(T.SCHEDULED_TIME,'%Y-%m-%d %H:%i:%s')SCHEDULED_TIME,T.SCHEDULED_PERIOD");
		String countSql = s1.replace("%PARAM%", "COUNT(1)");

		// 2012-09-04 LiJiahui 注意：此句中的order by 不可以写成大写字母ORDER BY
		// 因为getResult并不是对所有的记录排序，而且取出100条再排序 将order by写作小写可以规避该问题
		sql += " order by SENSITIVE_NUM DESC, UP_TENDENCY_FLAG DESC, ORD ASC, W.CREAT_DATE ASC";
		return this.dbgridDataPub.getResultNewBySize(countSql, sql, begion, pageSize, "", DbgridStatic.GRID_FUNID_TS_MYSHEET);
	}
    
	public GridDataInfo getSimpleDealingSheetTs(int begion, int pageSize, String strWhere) {
        StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% "
+ "FROM CC_WORK_SHEET W LEFT JOIN (SELECT H.WORK_SHEET_ID FROM CC_SHEET_HIDDEN_ACTION H WHERE H.HIDDEN_STATE = 0) HP ON W.WORK_SHEET_ID = HP.WORK_SHEET_ID,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L "
+ "WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
+ "AND HP.WORK_SHEET_ID IS NULL AND A.SERVICE_DATE=3 AND W.LOCK_FLAG IN(1,3) AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025");

        if (!"".equals(strWhere)) {
            int andIndex = strWhere.indexOf("AND");
            if (andIndex == -1) {
                log.warn("WHERE条件中没有AND，请检查WHERE条件 {}", strWhere);
                return null;
            }
            sql1.append(strWhere);
        }
        // 权限匹配
        Map tableMap = new HashMap();
        tableMap.put("CC_WORK_SHEET", "W");
        tableMap.put("CC_SERVICE_ORDER_ASK", "A");
        tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
        tableMap.put("CC_ORDER_CUST_INFO", "C");
        String s1 = this.systemAuthorization.getAuthedSql(tableMap, sql1.toString(), "900018301");
        
        String sql = s1.replace("%PARAM%", 
        		  "A.SERVICE_ORDER_ID,"
        		+ "A.SERVICE_TYPE,"
        		+ "DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
        		+ "DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,"
        		+ "DATE_FORMAT(A.FINISH_DATE,'%Y-%m-%d %H:%i:%s')FINISH_DATE,"
        		+ "A.HANGUP_TIME_COUNT HANGUP_ORDER_COUNT,"
        		+ "A.ORDER_LIMIT_TIME,"
        		+ "IF(D.APPEAL_REASON_ID IN(11504,11505),5,IF(D.APPEAL_PROD_ID=111,6,IF(A.ACCEPT_CHANNEL_ID=707907012,7,IF(A.ACCEPT_CHANNEL_ID=707907011,8,9))))ORD");
        String countSql = s1.replace("%PARAM%", "COUNT(1)");

        // 2012-09-04 LiJiahui 注意：此句中的order by 不可以写成大写字母ORDER BY
        // 因为getResult并不是对所有的记录排序，而且取出100条再排序 将order by写作小写可以规避该问题
        sql += " order by ORD, W.CREAT_DATE ASC";
        return this.dbgridDataPub.getResultNewBySize(countSql, sql, begion, pageSize, "", DbgridStatic.GRID_FUNID_TS_MYSHEET);
    }
	
	public GridDataInfo getDealingSheetTs(int begion, int pageSize, String strWhere, Map<String, String> orgMap) {
		if(orgMap.containsKey("roleClass")) {
			String roleClass = orgMap.get("roleClass");
			String orgWhere = orgMap.get("orgWhere");
			if("2".equals(roleClass)) {
				return this.getSimpleDealingSheetTs(begion, pageSize, strWhere, orgWhere);
			} else {
				return this.getSimpleAllSheetTs(begion, pageSize, strWhere, orgWhere);
			}
		} else {
			return this.getSimpleDealingSheetTs(begion, pageSize, strWhere);
		}
	}
	
	public GridDataInfo getSimpleDealingSheetTs(int begion, int pageSize, String strWhere, String orgWhere) {
        StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% "
+ "FROM CC_WORK_SHEET W LEFT JOIN (SELECT H.WORK_SHEET_ID FROM CC_SHEET_HIDDEN_ACTION H WHERE H.HIDDEN_STATE = 0) HP ON W.WORK_SHEET_ID = HP.WORK_SHEET_ID,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L "
+ "WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
+ "AND HP.WORK_SHEET_ID IS NULL AND A.SERVICE_DATE=3 AND W.LOCK_FLAG IN(1,3) AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025");
        
        if (!"".equals(strWhere)) {
            sql1.append(strWhere);
        }
        sql1.append(" AND (W.DEAL_ORG_ID in ( " + orgWhere + "))");
        String sqlStr = sql1.toString();
        
        String sql = sqlStr.replace("%PARAM%", 
        		  "A.SERVICE_ORDER_ID,"
        		+ "A.SERVICE_TYPE,"
        		+ "DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
        		+ "DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,"
        		+ "DATE_FORMAT(A.FINISH_DATE,'%Y-%m-%d %H:%i:%s')FINISH_DATE,"
        		+ "A.HANGUP_TIME_COUNT HANGUP_ORDER_COUNT,"
        		+ "A.ORDER_LIMIT_TIME,"
        		+ "IF(D.APPEAL_REASON_ID IN(11504,11505),5,IF(D.APPEAL_PROD_ID=111,6,IF(A.ACCEPT_CHANNEL_ID=707907012,7,IF(A.ACCEPT_CHANNEL_ID=707907011,8,9))))ORD");
        String countSql = sqlStr.replace("%PARAM%", "COUNT(1)");

        sql += " order by ORD, W.CREAT_DATE ASC";
        return this.dbgridDataPub.getResultNewBySize(countSql, sql, begion, pageSize, "", "首视图班长权限");
    }
	
	public GridDataInfo getSimpleAllSheetTs(int begion, int pageSize, String strWhere, String orgWhere) {
		StringBuilder sql0 = new StringBuilder().append("SELECT %PARAM% "
+ "FROM CC_WORK_SHEET W LEFT JOIN (SELECT H.WORK_SHEET_ID FROM CC_SHEET_HIDDEN_ACTION H WHERE H.HIDDEN_STATE = 0) HP ON W.WORK_SHEET_ID = HP.WORK_SHEET_ID,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L "
+ "WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
+ "AND HP.WORK_SHEET_ID IS NULL AND A.SERVICE_DATE=3 AND W.LOCK_FLAG=0 AND W.SHEET_STATU NOT IN (600000450,720130033) AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025");
				        
        if (!"".equals(strWhere)) {
            sql0.append(strWhere);
        }
        sql0.append(" AND (W.RECEIVE_ORG_ID in ( " + orgWhere + "))");
        
        StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% "
+ "FROM CC_WORK_SHEET W LEFT JOIN (SELECT H.WORK_SHEET_ID FROM CC_SHEET_HIDDEN_ACTION H WHERE H.HIDDEN_STATE = 0) HP ON W.WORK_SHEET_ID = HP.WORK_SHEET_ID,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L "
+ "WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
+ "AND HP.WORK_SHEET_ID IS NULL AND A.SERVICE_DATE=3 AND W.LOCK_FLAG IN(1,3) AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025");
        
        if (!"".equals(strWhere)) {
            sql1.append(strWhere);
        }
        sql1.append(" AND (W.DEAL_ORG_ID in ( " + orgWhere + "))");
        String sqlStr = "SELECT RT.* FROM (" + sql0.toString() + " UNION " + sql1.toString() + ") RT";
        
        String sql = sqlStr.replace("%PARAM%", 
        		  "A.SERVICE_ORDER_ID,"
        		+ "A.SERVICE_TYPE,"
        		+ "DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
        		+ "DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,"
        		+ "DATE_FORMAT(A.FINISH_DATE,'%Y-%m-%d %H:%i:%s')FINISH_DATE,"
        		+ "A.HANGUP_TIME_COUNT HANGUP_ORDER_COUNT,"
        		+ "A.ORDER_LIMIT_TIME,"
        		+ "W.CREAT_DATE,"
        		+ "IF(D.APPEAL_REASON_ID IN(11504,11505),5,IF(D.APPEAL_PROD_ID=111,6,IF(A.ACCEPT_CHANNEL_ID=707907012,7,IF(A.ACCEPT_CHANNEL_ID=707907011,8,9))))ORD");
        String countSql = sqlStr.replace("%PARAM%", "W.SERVICE_ORDER_ID").replace("RT.*", "COUNT(1)");

        sql += " order by ORD, CREAT_DATE ASC";
        return this.dbgridDataPub.getResultNewBySize(countSql, sql, begion, pageSize, "", "首视图主管权限");
    }

	/**
	 * 取得投诉建议已派列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getAlreadySendSheetTs(int begion,String strWhere) {
		int staffId = 0;
		IStaffPermit staffPermit = dbgridDataPub.getStaffPermit();
		staffId = Integer.parseInt(staffPermit.getStaff().getId());

		String beginStr = 
"SELECT SERVICE_ORDER_ID,WORK_SHEET_ID,TACHE_DESC,ORDER_STATU,ORDER_STATU_DESC,PROD_NUM,ACCEPT_DATE,CREAT_DATE,ACCEPT_CHANNEL_ID,"
+ "ACCEPT_COME_FROM_DESC,SHEET_STATU_DESC,HASTENT_NUM,ACCEPT_COUNT,RECEIVE_STAFF_NAME,SERVICE_TYPE_DESC,HANGUP_TIME_COUNT,DEAL_LIMIT_TIME,"
+ "PRE_ALARM_VALUE,SHEET_TYPE,SHEET_TYPE_DESC,URGENCY_GRADE,URGENCY_GRADE_DESC,APPEAL_PROD_NAME,CUST_SERV_GRADE_NAME,REGION_NAME,LOCK_DATE,"
+ "ACCEPT_COME_FROM,SHEET_STATU,HANGUP_START_TIME,RETURN_STAFF_NAME,REGION_ID,MONTH_FLAG,SHEET_READ FROM(SELECT*,IF(@ORDERID=SERVICE_ORDER_ID,"
+ "@RANK:=@RANK+1,@RANK:=1)RANK,@ORDERID:=SERVICE_ORDER_ID FROM(";
		String endStr = 
"ORDER BY SERVICE_ORDER_ID ASC,CREAT_DATE DESC)X,(SELECT @ORDERID:=NULL,@rank:=0)Y)Z WHERE RANK=1";
		String strSql = 
"SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')"
+ "ACCEPT_DATE,DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,W.SHEET_STATU_DESC,(SELECT "
+ "COUNT(1)FROM CC_HASTEN_SHEET HP WHERE HP.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID)HASTENT_NUM,A.ACCEPT_COUNT,W.RECEIVE_STAFF_NAME,A.SERVICE_TYPE_DESC,"
+ "W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.PRE_ALARM_VALUE,W.SHEET_TYPE,W.SHEET_TYPE_DESC,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,D.APPEAL_PROD_NAME,"
+ "C.CUST_SERV_GRADE_NAME,A.REGION_NAME,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')LOCK_DATE,A.ACCEPT_COME_FROM,W.SHEET_STATU,DATE_FORMAT("
+ "W.HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,(SELECT COUNT(1)FROM CC_SHEET_READ_RECORD "
+ "RR WHERE NOW()<RR.READ_END_DATE AND RR.WORK_SHEET_ID=W.WORK_SHEET_ID AND RR.READ_STAFF_ID="+staffId+")SHEET_READ FROM CC_WORK_SHEET W,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.REGION_ID=A.REGION_ID AND "
+ "A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND A.SERVICE_DATE=3 AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025 AND(("
+ "W.SHEET_STATU IN(600000450,720130033)AND W.LOCK_FLAG=0)OR W.SHEET_TYPE=720130013)";
		if (!"".equals(strWhere)) {
			strSql = strSql + strWhere;
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018307");
		strSql = beginStr + strSql + endStr;
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY CREAT_DATE ASC", DbgridStatic.GRID_FUNID_TS_ALREADYSHEET);
	}

	/**
	 * 得到投诉建议强制释放列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getForceReleaseTs(int begion,String strWhere) {
				String strSql = "SELECT W.SERVICE_ORDER_ID,\n" +
				"       W.WORK_SHEET_ID,\n" + 
				"       W.TACHE_DESC,\n" + 		
				"       A.ORDER_STATU,\n" + 
				"       A.ORDER_STATU_DESC,\n" + 
				"       A.PROD_NUM,\n" + 
				"       DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" +
				"       DATE_FORMAT(W.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE,\n" +
				"       A.ACCEPT_CHANNEL_ID,\n" + 
				"       A.ACCEPT_COME_FROM_DESC,\n" + 
				"       W.SHEET_STATU_DESC,\n" + 
				"       W.HASTENT_NUM,\n" + 
				"       A.ACCEPT_COUNT,\n" + 						
				"       W.HANGUP_TIME_COUNT,\n" + 
				"       W.DEAL_LIMIT_TIME,\n" + 
				"       W.PRE_ALARM_VALUE,\n" + 						
				"       W.RECEIVE_STAFF_NAME,\n" +
				"       W.SHEET_TYPE_DESC,\n" +
				"       A.SERVICE_TYPE_DESC,\n" + 
				"       A.URGENCY_GRADE,\n" + 
				"       A.URGENCY_GRADE_DESC,\n" +		
				"       A.ACCEPT_COME_FROM,\n" + 
				"       DATE_FORMAT(W.LOCK_DATE, '%Y-%m-%d %H:%i:%s') AS LOCK_DATE   ,\n" +
				"       W.SHEET_STATU,\n" + //修改用于判断挂起工单
				"       DATE_FORMAT(W.HANGUP_START_TIME, '%Y-%m-%d %H:%i:%s') AS HANGUP_START_TIME   ,\n" +
				"       W.DEAL_STAFF_NAME ,"+
				"       D.APPEAL_PROD_NAME,\n" + 
				"       C.CUST_SERV_GRADE_NAME,\n" +
				"       A.REGION_NAME,\n" + 
				"       W.RETURN_STAFF_NAME,\n" + 
				"       W.REGION_ID,W.MONTH_FLAG\n" +	
				" FROM CC_WORK_SHEET W, CC_SERVICE_ORDER_ASK A ," +
				" CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C\n" + 
				" WHERE W.REGION_ID = A.REGION_ID\n" + 						
				" AND A.SERVICE_DATE = 3" +
				" AND W.LOCK_FLAG = 1" +
				" AND W.SHEET_STATU != 700000046 " +//挂起
				" AND W.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID " +
				" AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID" +
				" AND A.CUST_GUID = C.CUST_GUID " ;
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");		
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018305");
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", DbgridStatic.GRID_FUNID_TS_RELEASESHEET);		
	}

	@Override
	public GridDataInfo getMonitorOrderInfo(int begion, String strWhere) {
		String sql = sqlMntOrderInfo + strWhere;
		return this.dbgridDataPub.getResult(sql, begion, " ORDER BY S.ACCEPT_DATE", DbgridStatic.GRID_FUNID_MNT_ORDERINFO);
	}
	
	@Override
	public GridDataInfo getZqSheet(int begion, String strWhere) {
		String sql="SELECT CC_RECEIVE_CUSTINFO.FLOWNO AS FLOWNO,CC_RECEIVE_CUSTINFO.CALLSTAFFCODE AS CALLSTAFFCODE,\n" + 
				"CC_RECEIVE_CUSTINFO.CALLORGNAME AS CALLORGNAME,CC_RECEIVE_CUSTINFO.CUSTNAME AS CUSTNAME,CC_RECEIVE_CUSTINFO.REALPHONE AS REALPHONE,\n" + 
				"DATE_FORMAT(CC_RECEIVE_CUSTINFO.UPLOADDATE, '%Y-%m-%d %H:%i:%s') AS UPLOADDATE,CC_RECEIVE_CUSTINFO.BUSINESSDESC AS BUSINESSDESC,\n" +
				"CC_RECEIVE_CUSTINFO.DEALSTATUS AS DEALSTATUS,CC_RECEIVE_CUSTINFO.DEALSTAFFCODE AS DEALSTAFFCODE,\n" + 
				"CC_RECEIVE_CUSTINFO.DEALORGNAME AS DEALORGNAME,CC_RECEIVE_CUSTINFO.DEALCONTENT AS DEALCONTENT,\n" + 
				"CC_RECEIVE_CUSTINFO.DATASOURCE AS DATASOURCE,CC_RECEIVE_CUSTINFO.IMGURLLIST AS IMGURLLIST,\n" + 
				"CC_RECEIVE_CUSTINFO.REGIONID AS REGIONID,CC_RECEIVE_CUSTINFO.PRODNUM AS PRODNUM FROM CC_RECEIVE_CUSTINFO,TSM_ORGANIZATION\n" + 
				" WHERE 1 = 1 AND CC_RECEIVE_CUSTINFO.CALLORGID = TSM_ORGANIZATION.ORG_ID "
				+strWhere;
				
		return this.dbgridDataPub.getResult(sql, begion, " ORDER BY UPLOADDATE DESC ", DbgridStatic.GRID_FUNID_ZQ_CUST_DATA);
	}
	
	private String sqlMntOrderInfo= 
			"SELECT S.SERVICE_ORDER_ID,\n" +
					"       S.REGION_NAME,\n" + 
					"       S.ORDER_STATU_DESC,\n" + 
					"       TO_CHAR(S.ACCEPT_DATE, 'yyyy-mm-dd hh24:mi:ss') ACCEPT_DATE,\n" + 
					"       S.PROD_NUM,\n" + 
					"       S.SERVICE_TYPE_DESC,\n" + 
					"       S.ACCEPT_STAFF_NAME,\n" + 
					"       S.ACCEPT_COUNT,\n" + 
					"       S.ACCEPT_ORG_NAME,\n" + 
					"       S.CUST_SERV_GRADE_DESC,\n" + 
					"       U.CUST_BRAND_DESC,\n" + 
					"       C.APPEAL_PROD_NAME,\n" + 
					"       S.ORDER_VESION,\n" + 
					"       TO_CHAR(S.MODIFY_DATE, 'yyyy-mm-dd hh24:mi:ss') MODIFY_DATE,\n" + 
					"       S.MONTH_FLAG,S.REGION_ID,U.CUST_GUID\n" + 
					"  FROM CC_SERVICE_ORDER_ASK   S,\n" + 
					"       CC_SERVICE_CONTENT_ASK C,\n" + 
					"       CC_ORDER_CUST_INFO     U\n" + 
					" WHERE 1 = 1\n" + 
					"   AND S.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID\n" + 
					"   AND S.CUST_GUID = U.CUST_GUID";

	@Override
	public GridDataInfo getDealPatchData(int begion, String strWhere) {
		String sql="SELECT CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID      AS ATTR_110003878,\n" + 
				"       CC_SERVICE_ORDER_ASK.PROD_NUM              AS ATTR_110003884,\n" + 
				"       CC_SERVICE_ORDER_ASK.SERVICE_TYPE_DESC     AS ATTR_110003881,\n" + 
				"       DATE_FORMAT(CC_SERVICE_ORDER_ASK.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')   AS ATTR_110003888,\n" +
				"       CC_SERVICE_ORDER_ASK.ACCEPT_COME_FROM_DESC AS ATTR_110003887,\n" + 
				"       CC_SERVICE_ORDER_ASK.URGENCY_GRADE_DESC    AS ATTR_110003890,\n" + 
				"       CC_WORK_SHEET.REGION_NAME                  AS ATTR_110003880,\n" + 
				"       CC_WORK_SHEET.REGION_ID                    AS ATTR_110003879,\n" + 
				"       DATE_FORMAT(CC_WORK_SHEET.CREAT_DATE,'%Y-%m-%d %H:%i:%s')   AS ATTR_110003921,\n" +
				"       CC_WORK_SHEET.TACHE_DESC                   AS ATTR_110003925,\n" + 
				"       CC_WORK_SHEET.TACHE_ID                     AS ATTR_110003924,\n" + 
				"       CC_WORK_SHEET.WORK_SHEET_ID                AS ATTR_110003900,\n" + 
				"       CC_SERVICE_ORDER_ASK.CUST_GUID             AS ATTR_110003945,\n" + 
				"       CC_WORK_SHEET.DEAL_REQUIRE                 AS ATTR_110003901,\n" + 
				"       CC_WORK_SHEET.WORKSHEET_SCHEMA_ID          AS ATTR_110004042,\n" + 
				"       CC_SERVICE_ORDER_ASK.MONTH_FLAG            AS ATTR_110004036,\n" + 
				"       CC_WORK_SHEET.DEAL_LIMIT_TIME              AS ATTR_110004037,\n" + 
				"       CC_WORK_SHEET.HANGUP_TIME_COUNT            AS ATTR_110004038,\n" + 
				"       CC_WORK_SHEET.SHEET_PRI_VALUE              AS ATTR_110004039,\n" + 
				"       CC_WORK_SHEET.SHEET_STATU_DESC             AS ATTR_110004040,\n" + 
				"       CC_WORK_SHEET.HASTENT_NUM                  AS ATTR_110004041,\n" + 
				"       CC_SERVICE_ORDER_ASK.ORDER_STATU_DESC      AS ATTR_110004056,\n" + 
				"       CC_WORK_SHEET.PRE_ALARM_VALUE              AS ATTR_110004057,\n" + 
				"       CC_SERVICE_ORDER_ASK.ACCEPT_COUNT          AS ATTR_110004058,\n" + 
				"       CC_SERVICE_CONTENT_ASK.APPEAL_PROD_NAME    AS ATTR_110003894,\n" + 
				"       CC_ORDER_CUST_INFO.CUST_SERV_GRADE_NAME    AS CUST_SERV_GRADE_NAME,\n" + 
				"       CC_SERVICE_ORDER_ASK.REGION_NAME           AS REGION_NAME,\n" + 
				"       CC_WORK_SHEET.SHEET_TYPE_DESC              AS ATTR_110007057,\n" + 
				"       CC_WORK_SHEET.SHEET_TYPE                   AS SHEET_TYPE,\n" + 
				"        DATE_FORMAT(CC_WORK_SHEET.LOCK_DATE,'%Y-%m-%d %H:%i:%s')   AS LOCK_DATE,\n" +
				"       CC_WORK_SHEET.RETURN_STAFF_NAME            AS RETURN_STAFF_NAME,\n" + 
				"       CC_SERVICE_ORDER_ASK.MORE_RELA_INFO        AS MORE_RELA_INFO,\n" + 
				"       CC_SERVICE_CONTENT_ASK.APPEAL_REASON_DESC  AS APPEAL_REASON_DESC\n" + 
				"  FROM CC_SERVICE_ORDER_ASK,\n" + 
				"       CC_WORK_SHEET,\n" + 
				"       CC_ORDER_CUST_INFO,\n" + 
				"       CC_SERVICE_CONTENT_ASK\n" + 
				" WHERE CC_SERVICE_ORDER_ASK.REGION_ID = CC_WORK_SHEET.REGION_ID\n" + 
				"   AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID =\n" + 
				"       CC_WORK_SHEET.SERVICE_ORDER_ID\n" + 
				"   AND CC_ORDER_CUST_INFO.REGION_ID = CC_SERVICE_ORDER_ASK.REGION_ID\n" + 
				"   AND CC_ORDER_CUST_INFO.CUST_GUID = CC_SERVICE_ORDER_ASK.CUST_GUID\n" + 
				"   AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID =CC_SERVICE_CONTENT_ASK.SERVICE_ORDER_ID" + 
				"   AND CC_WORK_SHEET.LOCK_FLAG = 0" + 
				"   AND CC_SERVICE_ORDER_ASK.SERVICE_DATE = 1" + 
				"   AND CC_SERVICE_ORDER_ASK.Service_Type != '400000118' "+strWhere + 
				" ORDER BY DECODE(CC_SERVICE_CONTENT_ASK.APPEAL_REASON_DESC," + 
				"                 '天翼新入网',\n" + 
				"                 1," + 
				"                 2)," + 
				"          CC_WORK_SHEET.CREAT_DATE ASC ";
		return this.dbgridDataPub.getResult(sql, begion, "", "");
	}

	@Override
	public GridDataInfo getYNDealData(int begion, String strWhere) {
		 String sql="SELECT CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID      AS ATTR_510030856,\n" + 
		 		"       CC_WORK_SHEET.SHEET_STATU                  AS ATTR_510030806,\n" + 
		 		"       DATE_FORMAT(CC_WORK_SHEET.CREAT_DATE,'%Y-%m-%d %H:%i:%s') AS ATTR_510030809,\n" +
		 		"       CC_WORK_SHEET.TACHE_ID                     AS ATTR_510030823,\n" + 
		 		"       CC_WORK_SHEET.TACHE_DESC                   AS ATTR_510030811,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.SERVICE_TYPE          AS ATTR_510030818,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.URGENCY_GRADE         AS ATTR_510030821,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.REGION_NAME           AS ATTR_510030840,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.PROD_NUM              AS ATTR_510030844,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.SERVICE_TYPE_DESC     AS ATTR_510030841,\n" + 
		 		"       DATE_FORMAT(CC_SERVICE_ORDER_ASK.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')  AS ATTR_510030848,\n" +
		 		"       CC_SERVICE_ORDER_ASK.ACCEPT_COME_FROM_DESC AS ATTR_510030847,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.URGENCY_GRADE_DESC    AS URGENCY_GRADE_DESC,\n" + 
		 		"       CC_WORK_SHEET.REGION_ID                    AS REGION_ID,\n" + 
		 		"       CC_WORK_SHEET.WORK_SHEET_ID                AS ATTR_510030850,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.CUST_GUID             AS CUST_GUID,\n" + 
		 		"       CC_WORK_SHEET.DEAL_REQUIRE                 AS DEAL_REQUIRE,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.MONTH_FLAG            AS MONTH_FLAG,\n" + 
		 		"       CC_WORK_SHEET.SHEET_TYPE                   AS SHEET_TYPE,\n" + 
		 		"       CC_WORK_SHEET.WORKSHEET_SCHEMA_ID          AS WORKSHEET_SCHEMA_ID,\n" + 
		 		"       CC_WORK_SHEET.SHEET_STATU_DESC             AS ATTR_510030812,\n" + 
		 		"       CC_WORK_SHEET.HASTENT_NUM                  AS HASTENT_NUM,\n" + 
		 		"       CC_WORK_SHEET.HANGUP_TIME_COUNT            AS HANGUP_TIME_COUNT,\n" + 
		 		"       CC_WORK_SHEET.SHEET_PRI_VALUE              AS ATTR_510030830,\n" + 
		 		"       CC_WORK_SHEET.DEAL_LIMIT_TIME              AS DEAL_LIMIT_TIME,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.ORDER_STATU_DESC      AS ORDER_STATU_DESC,\n" + 
		 		"       CC_WORK_SHEET.SHEET_TYPE_DESC              AS SHEET_TYPE_DESC,\n" + 
		 		"       CC_WORK_SHEET.RETURN_STAFF_NAME            AS ATTR_510030804,\n" + 
		 		"       CC_WORK_SHEET.LOCK_FLAG                    AS LOCK_FLAG,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.ORDER_LIMIT_TIME      AS ORDER_LIMIT_TIME,\n" + 
		 		"       CC_WORK_SHEET.STATIONLIMINT                AS STATIONLIMINT,\n" + 
		 		"       CC_SERVICE_ORDER_ASK.ACCEPT_COUNT          AS ACCEPT_COUNT,\n" + 
		 		"       CC_SERVICE_CONTENT_ASK.APPEAL_PROD_NAME    AS APPEAL_PROD_NAME,\n" + 
		 		"       CC_ORDER_CUST_INFO.CUST_SERV_GRADE_NAME    AS CUST_SERV_GRADE_NAME,\n" + 
		 		"       CC_WORK_SHEET.HANGUP_START_TIME            AS HANGUP_START_TIME,\n" + 
		 		"       CC_WORK_SHEET.DEAL_STAFF_NAME              AS DEAL_STAFF_NAME\n" + 
		 		"  FROM CC_SERVICE_CONTENT_ASK,\n" + 
		 		"       CC_SERVICE_ORDER_ASK,\n" + 
		 		"       CC_WORK_SHEET,\n" + 
		 		"       CC_ORDER_CUST_INFO\n" + 
		 		" WHERE CC_WORK_SHEET.LOCK_FLAG = 1\n" + 
		 		"   AND CC_SERVICE_ORDER_ASK.REGION_ID = CC_WORK_SHEET.REGION_ID\n" + 
		 		"   AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID =\n" + 
		 		"       CC_WORK_SHEET.SERVICE_ORDER_ID\n" + 
		 		"   AND CC_SERVICE_ORDER_ASK.CUST_GUID = CC_ORDER_CUST_INFO.CUST_GUID\n" + 
		 		"   AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID =\n" + 
		 		"       CC_SERVICE_CONTENT_ASK.SERVICE_ORDER_ID\n" + 
		 		"   AND CC_SERVICE_ORDER_ASK.SERVICE_DATE = 0\n" + 
		 		"   AND CC_WORK_SHEET.SHEET_STATU != 700000046 "
		 		+strWhere+
		 		" ORDER BY CC_WORK_SHEET.CREAT_DATE ASC ";
		 return this.dbgridDataPub.getResult(sql, begion, "", "");
	}

	@Override
	public GridDataInfo getZCOrderData(int begion, String strWhere) {
		String sql="SELECT CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID   AS ATTR_110003962,\n" + 
				"       CC_SERVICE_ORDER_ASK.ORDER_STATU_DESC   AS ATTR_110003965,\n" + 
				"       CC_SERVICE_ORDER_ASK.PROD_NUM           AS ATTR_110003968,\n" + 
				"       CC_SERVICE_ORDER_ASK.URGENCY_GRADE_DESC AS ATTR_110003974,\n" + 
				"       CC_SERVICE_ORDER_ASK.ACCEPT_COUNT       AS ATTR_110003977,\n" + 
				"       DATE_FORMAT(CC_SERVICE_ORDER_ASK.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')        AS ATTR_110003972,\n" +
				"       CC_SERVICE_ORDER_ASK.CUST_GUID          AS ATTR_110004029,\n" + 
				"       CC_SERVICE_ORDER_ASK.MONTH_FLAG         AS ATTR_110004036,\n" + 
				"       CC_SERVICE_ORDER_ASK.Service_Type_Desc  AS ATTR_110004038,\n" + 
				"       CC_SERVICE_ORDER_ASK.REGION_ID          AS ATTR_510030242\n" + 
				"  FROM CC_SERVICE_ORDER_ASK\n" + 
				" WHERE CC_SERVICE_ORDER_ASK.ORDER_STATU = 700000099\n" + 
				"   AND CC_SERVICE_ORDER_ASK.Service_Type = '700001171'\n" + 
				" order by CC_SERVICE_ORDER_ASK.Accept_Date desc ";
		return this.dbgridDataPub.getResult(sql, begion, "", "");
	}

	@Override
	public GridDataInfo getBackOrderData(int begion, String strWhere) {
		String sql="SELECT CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID   AS ATTR_510030313,\n" + 
				"       CC_SERVICE_ORDER_ASK.SERVICE_TYPE_DESC  AS ATTR_510030316,\n" + 
				"       CC_SERVICE_ORDER_ASK.PROD_NUM           AS ATTR_510030319,\n" + 
				"       CC_SERVICE_ORDER_ASK.URGENCY_GRADE_DESC AS ATTR_510030325,\n" + 
				"       CC_SERVICE_ORDER_ASK.ACCEPT_COUNT       AS ATTR_510030328,\n" + 
				"       DATE_FORMAT(CC_SERVICE_ORDER_ASK.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')  AS ATTR_510030323,\n" +
				"       CC_SERVICE_ORDER_ASK.CUST_GUID          AS ATTR_510030296,\n" + 
				"       CC_SERVICE_ORDER_ASK.MONTH_FLAG         AS ATTR_110004036,\n" + 
				"       CC_SERVICE_ORDER_ASK.REGION_ID          AS ATTR_510030314,\n" + 
				"       CC_SERVICE_ORDER_ASK.REPLY_CONTENT      AS ATTR_110004038,\n" + 
				"       CC_SERVICE_ORDER_ASK.ACCEPT_STAFF_NAME  AS ATTR_510030278,\n" + 
				"       CC_SERVICE_ORDER_ASK.ACCEPT_ORG_NAME    AS ATTR_510030279,\n" + 
				"       CC_SERVICE_ORDER_ASK.ACCEPT_STAFF_ID    AS ATTR_510030291,\n" + 
				"       CC_SERVICE_ORDER_ASK.ACCEPT_ORG_ID      AS ATTR_510030292,\n" + 
				"       DATE_FORMAT(CC_SERVICE_ORDER_ASK.MODIFY_DATE,'%Y-%m-%d %H:%i:%s') AS MODIFY_DATE\n" +
				"  FROM CC_SERVICE_ORDER_ASK\n" + 
				" WHERE CC_SERVICE_ORDER_ASK.ORDER_STATU = 700000100\n" + 
				"   AND CC_SERVICE_ORDER_ASK.SERVICE_TYPE = 700001171 "+strWhere+ 
				" ORDER BY CC_SERVICE_ORDER_ASK.ACCEPT_DATE DESC";
		return this.dbgridDataPub.getResult(sql, begion, "", "");
	}

	@Override
	public GridDataInfo getTSSpeciaData(int begion, String strWhere) {
		String sql="SELECT CC_ESPECIALLY_CUST.REGION_NAME     AS REGION_NAME,\n" + 
				"       CC_ESPECIALLY_CUST.REGION_ID       AS REGION_ID,\n" + 
				"       CC_ESPECIALLY_CUST.CUST_NAME       AS CUST_NAME,\n" + 
				"       CC_ESPECIALLY_CUST.CUST_NUM        AS CUST_NUM,\n" + 
				"       CC_ESPECIALLY_CUST.TS_ESPECIALLY   AS TS_ESPECIALLY,\n" + 
				"       CC_ESPECIALLY_CUST.REMARK          AS REMARK,\n" + 
				"       CC_ESPECIALLY_CUST.MEET_PROCEEDING AS MEET_PROCEEDING,\n" + 
				"       CC_ESPECIALLY_CUST.STATU           AS STATU,\n" + 
				"      DATE_FORMAT(CC_ESPECIALLY_CUST.MODIFI_DATA,'%Y-%m-%d %H:%i:%s')     AS MODIFI_DATA,\n" +
				"       CC_ESPECIALLY_CUST.MODIFI_STAFF    AS MODIFI_STAFF,\n" + 
				"       CC_ESPECIALLY_CUST.REGION_TELNO    AS REGION_TELNO\n" + 
				"  FROM CC_ESPECIALLY_CUST" + 
				" WHERE 1 = 1 AND CC_ESPECIALLY_CUST.STATU = 1 "+strWhere;
		return this.dbgridDataPub.getResult(sql, begion, "", "");
	}

	/**
	 * 人工处理分派工单池列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getBatchWaitDealSheetTs(int begion, int pageSize, String strWhere) {
		TsmStaff staff = pubFunc.getLogonStaff();

		StringBuffer sql1 = new StringBuffer().append(
				"SELECT W.RECEIVE_ORG_ID,W.RECEIVE_ORG_NAME,A.HANGUP_TIME_COUNT ORDER_TIME_COUNT,W.SERVICE_ORDER_ID,A.ORDER_LIMIT_TIME,A.COME_CATEGORY,W.SHEET_RECEIVE_DATE,W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.PROD_NUM,"
						+ "A.ACCEPT_DATE,W.CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,W.SHEET_STATU_DESC,(SELECT COUNT(1)FROM CC_HASTEN_SHEET HS WHERE "
						+ "HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID)HASTENT_NUM,A.ACCEPT_COUNT,W.RECEIVE_STAFF_NAME,A.SERVICE_TYPE_DESC,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,"
						+ "W.PRE_ALARM_VALUE,W.SHEET_TYPE_DESC,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,D.APPEAL_PROD_NAME,D.APPEAL_REASON_DESC,D.APPEAL_CHILD_DESC,C.CUST_SERV_GRADE_NAME,A.REGION_NAME,W.SHEET_TYPE,"
						+ "A.ACCEPT_COME_FROM,W.LOCK_DATE,W.SHEET_STATU,W.HANGUP_START_TIME,W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,W.PRECONTRACT_SIGN,W.TACHE_ID,"
						+ "A.SEND_TO_ORG_NAME,W.MAIN_SHEET_FLAG,W.RESPOND_DATE,A.SERVICE_TYPE,D.APPEAL_REASON_ID,C.HIGH_WARN_ID,IF(W.SHEET_TYPE NOT IN(700000129,700001002,"
						+ "720130015)OR(W.SHEET_TYPE=700000129 AND W.FLOW_SEQUENCE=1),W.RETURN_STAFF_NAME,(SELECT WS1.DEAL_STAFF_NAME FROM CC_WORK_SHEET WS1 WHERE "
						+ "WS1.WORK_SHEET_ID=W.SOURCE_SHEET_ID))LAST_DEAL_STAFF,(SELECT COUNT(1)FROM CC_SHEET_READ_RECORD RR WHERE NOW()<RR.READ_END_DATE AND "
						+ "RR.WORK_SHEET_ID=W.WORK_SHEET_ID AND RR.READ_STAFF_ID="+staff.getId()+")SHEET_READ,IF(D.APPEAL_REASON_ID IN(11504,11505),5,"
						+ "IF(D.APPEAL_PROD_ID=111,6,IF(A.ACCEPT_CHANNEL_ID=707907012,7,IF(A.ACCEPT_CHANNEL_ID=707907011,8,9))))ORD,L.FORCE_PRE_FLAG,L.UPGRADE_INCLINE,"
						+ "IFNULL(L.UP_LEVEL,1)UP_LEVEL,IFNULL(L.HOTLINE_FLAG,0)HOTLINE_FLAG,IF(L.REPEAT_MAN_FLAG=0,0,L.REPEAT_AUTO_FLAG)REPEAT_AUTO_FLAG,IF("
						+ "L.UPGRADE_MAN_FLAG=0,0,L.UPGRADE_AUTO_FLAG)UPGRADE_AUTO_FLAG,IFNULL(L.REPEAT_NEW_FLAG,0)REPEAT_NEW_FLAG,L.SEC_FLAG,IF(D.APPEAL_PROD_ID=111,1,IF(D.APPEAL_REASON_ID=23002503,1,''))XHZW_FLAG,"
						+ "IFNULL(L.ADJUST_ACCOUNT_FLAG,0)ADJUST_ACCOUNT_FLAG,IFNULL(L.DIRECT_DISPATCH_FLAG,0)DIRECT_DISPATCH_FLAG,(SELECT DATE_FORMAT(SA.CREAT_DATE,'%Y-%m-%d %H:%i:%s') FROM "
						+ "CC_WORK_SHEET_AREA SA WHERE SA.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND SA.TACHE_DATE IS NULL AND SA.AREA_FLAG=IF(W.TACHE_ID IN(720130021,700000085),"
						+ "3,IF(W.TACHE_ID IN(720130023,700000086),1,0))order by SA.CREAT_DATE DESC LIMIT 1)PD_BEGIN_DATE,IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN"
						+ "(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE))BEGIN_DATE,IF(W.SHEET_STATU IN(700000046,720130035),W.HANGUP_START_TIME,W.RESPOND_DATE)END_DATE,"
						+ "IFNULL((SELECT LP1.PD_MINUTES FROM "
						+ "CC_SHEET_LIMITTIME_PD LP1 WHERE LP1.TACHE_ID=W.TACHE_ID AND LP1.COME_CATEGORY=A.COME_CATEGORY AND LP1.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDLIMIT,"
						+ "IFNULL((SELECT LP2.WARN_MINUTES FROM CC_SHEET_LIMITTIME_PD LP2 WHERE LP2.TACHE_ID=W.TACHE_ID AND LP2.COME_CATEGORY=A.COME_CATEGORY AND "
						+ "LP2.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDWARN,(SELECT IFNULL(SUM(is_finish),-1) FROM cc_xc_flow WHERE main_sheet_id=w.work_sheet_id)XC_ING_NUM,"
						+ "C.CUST_TYPE,D.APPEAL_PROD_ID,IFNULL(L.UP_TENDENCY_FLAG,0)UP_TENDENCY_FLAG,D.SIX_GRADE_CATALOG,A.RELA_INFO,D.FIVE_ORDER,D.BEST_ORDER,D.BEST_ORDER_DESC"
						+ ",D.PROD_ONE,D.PROD_ONE_DESC,D.PROD_TWO,D.PROD_TWO_DESC,D.DEVT_CHS_ONE,D.DEVT_CHS_ONE_DESC,D.DEVT_CHS_TWO,D.DEVT_CHS_TWO_DESC,D.DEVT_CHS_THREE,D.DEVT_CHS_THREE_DESC,A.RELA_TYPE,C.CUST_NAME,A.AREA_NAME"
						+ ",CASE WHEN L.UP_TENDENCY_FLAG=1 THEN'有曝光倾向'WHEN L.UP_TENDENCY_FLAG=2 THEN'有工信部越级倾向或通管局投诉倾向'WHEN L.UP_TENDENCY_FLAG=3 THEN'有越级倾向'WHEN L.UP_TENDENCY_FLAG=4 THEN'最严重复'ELSE''END UP_TENDENCY_FLAG_DESC"
						+ " FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A,"
						+ "CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND "
						+ "A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND A.SERVICE_DATE=3 AND W.LOCK_FLAG=0 "
						+ "AND W.SHEET_STATU NOT IN(600000450,720130033)AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025");

		String overtimeWhere = "";
		if (!"".equals(strWhere)) {
			String[] whereArr = strWhere.split("&&");
			sql1.append(whereArr[0]);
			if (whereArr.length == 2) {
				overtimeWhere = whereArr[1];
			}
		}

		// 权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");
		String s1 = this.systemAuthorization.getAuthedSql(tableMap, sql1.toString(), "900018300");

		StringBuffer inner = new StringBuffer()
				.append("SELECT J.* FROM (")
				.append(s1)
				.append(") J order by J.ORD, J.CREAT_DATE ASC");
		StringBuffer outer = new StringBuffer().append(
				"SELECT SERVICE_ORDER_ID,ORDER_TIME_COUNT,COME_CATEGORY,ORDER_LIMIT_TIME,RECEIVE_ORG_ID,RECEIVE_ORG_NAME,WORK_SHEET_ID,TACHE_ID,TACHE_DESC,ADJUST_ACCOUNT_FLAG,DIRECT_DISPATCH_FLAG,ORDER_STATU,ORDER_STATU_DESC,"
						+ "PROD_NUM,DATE_FORMAT(ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,ACCEPT_CHANNEL_ID,"
						+ "ACCEPT_COME_FROM,ACCEPT_COME_FROM_DESC,SHEET_STATU,SHEET_STATU_DESC,HASTENT_NUM,ACCEPT_COUNT,RECEIVE_STAFF_NAME,SERVICE_TYPE_DESC,"
						+ "HANGUP_TIME_COUNT,DEAL_LIMIT_TIME,PRE_ALARM_VALUE,SHEET_TYPE,SHEET_TYPE_DESC,HIGH_WARN_ID,URGENCY_GRADE,URGENCY_GRADE_DESC,APPEAL_PROD_NAME,APPEAL_REASON_DESC,APPEAL_CHILD_DESC,"
						+ "CUST_SERV_GRADE_NAME,REGION_NAME,DATE_FORMAT(LOCK_DATE,'%Y-%m-%d %H:%i:%s')LOCK_DATE,DATE_FORMAT(HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')"
						+ "HANGUP_START_TIME,RETURN_STAFF_NAME,REGION_ID,MONTH_FLAG,PRECONTRACT_SIGN,SEND_TO_ORG_NAME,MAIN_SHEET_FLAG,LAST_DEAL_STAFF,FORCE_PRE_FLAG,"
						+ "REPEAT_AUTO_FLAG,UPGRADE_AUTO_FLAG,REPEAT_NEW_FLAG,PDLIMIT,PDWARN,SHEET_READ,UPGRADE_INCLINE,UP_LEVEL,HOTLINE_FLAG,SEC_FLAG,XHZW_FLAG,SERVICE_TYPE,"
						+ "XC_ING_NUM,CUST_TYPE,APPEAL_PROD_ID,APPEAL_REASON_ID,UP_TENDENCY_FLAG,SIX_GRADE_CATALOG,RELA_INFO,FIVE_ORDER,BEST_ORDER,BEST_ORDER_DESC,"
						+ "PROD_ONE,PROD_ONE_DESC,PROD_TWO,PROD_TWO_DESC,DEVT_CHS_ONE,DEVT_CHS_ONE_DESC,DEVT_CHS_TWO,DEVT_CHS_TWO_DESC,DEVT_CHS_THREE,DEVT_CHS_THREE_DESC,RELA_TYPE,"
						+ "PD_BEGIN_DATE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,BEGIN_DATE,END_DATE,CUST_NAME,AREA_NAME,UP_TENDENCY_FLAG_DESC");
		log.info("inner: \n{}",inner);
		log.info("outer: \n{}",outer);
		GridDataInfo infos = this.dbgridDataPub.getResultByW(inner.toString(), outer.toString(), overtimeWhere, begion, pageSize, DbgridStatic.GRID_FUNID_TS_SHEET);
		List<Map<String, Object>> list = infos.getList();
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				String beginDate = this.getStringByKey(map, "BEGIN_DATE");
				String orderDate = this.getStringByKey(map, "ACCEPT_DATE");
				String endDate = this.getStringByKey(map, "END_DATE");
				int hangupTimeCount = this.getIntByKey(map, "HANGUP_TIME_COUNT");
				int serviceType = this.getIntByKey(map, "SERVICE_TYPE");
				String sysDate = this.getStringByKey(map, "SYS_DATE");
				int comeCategory = this.getIntByKey(map, "COME_CATEGORY");
				int orderLimitTime = this.getIntByKey(map, "ORDER_LIMIT_TIME");
				int orderLimitCount = this.getIntByKey(map, "ORDER_TIME_COUNT");
				int orderType = comeCategory == 707907001 ? serviceType : 0;
				map.put("WORKTIME", pubFunc.getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate));
				map.put("NEXT_HOUR", pubFunc.getWorkingEnd(orderDate, orderDate, orderLimitTime, orderLimitCount * 60, orderType, sysDate));
				list.set(i, map);
			}
		}
		infos.setList(list);
		return infos;
	}


	/**
	 * 取得人工批量分派我的任务列表数据
	 *
	 * @param begion
	 *            开始
	 * @param strWhere
	 *            WHERE条件
	 * @return
	 */
	public GridDataInfo getBatchDealingSheetTs(int begion, int pageSize, String strWhere) {
		StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% "
				+ "FROM CC_WORK_SHEET W LEFT JOIN (SELECT H.WORK_SHEET_ID FROM CC_SHEET_HIDDEN_ACTION H WHERE H.HIDDEN_STATE = 0) HP ON W.WORK_SHEET_ID = HP.WORK_SHEET_ID,"
				+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L "
				+ "WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
				+ "AND HP.WORK_SHEET_ID IS NULL AND A.SERVICE_DATE=3 AND W.LOCK_FLAG IN(1,3) AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025");

		if (!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if (andIndex == -1) {
				log.warn("WHERE条件中没有AND，请检查WHERE条件 {}", strWhere);
				return null;
			}
			sql1.append(strWhere);
		}
		// 权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");
		String s1 = this.systemAuthorization.getAuthedSql(tableMap, sql1.toString(), "900018301");

		TsmStaff staff = pubFunc.getLogonStaff();
		String sql = s1.replace("%PARAM%", "W.MAIN_SHEET_FLAG,A.HANGUP_TIME_COUNT ORDER_TIME_COUNT,W.RECEIVE_ORG_NAME,IFNULL((SELECT PD1.PD_MINUTES FROM CC_SHEET_LIMITTIME_PD PD1 WHERE PD1.TACHE_ID=W.TACHE_ID AND "
				+ "PD1.COME_CATEGORY=A.COME_CATEGORY AND PD1.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDLIMIT,IFNULL((SELECT PD2.WARN_MINUTES FROM CC_SHEET_LIMITTIME_PD "
				+ "PD2 WHERE PD2.TACHE_ID=W.TACHE_ID AND PD2.COME_CATEGORY=A.COME_CATEGORY AND PD2.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDWARN,W.SERVICE_ORDER_ID,"
				+ "W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.COME_CATEGORY,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
				+ "DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,W.SHEET_STATU_DESC,(SELECT COUNT(1)FROM "
				+ "CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID)HASTENT_NUM,A.ACCEPT_COUNT,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.PRE_ALARM_VALUE,"
				+ "W.RECEIVE_STAFF_NAME,W.SHEET_TYPE_DESC,A.SERVICE_TYPE_DESC,A.SOURCE_NUM,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')"
				+ "LOCK_DATE,W.SHEET_STATU,A.SERVICE_TYPE,W.PRECONTRACT_SIGN,W.TACHE_ID,DATE_FORMAT(W.HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,"
				+ "A.ACCEPT_COME_FROM,W.SHEET_TYPE,D.APPEAL_PROD_NAME,C.CUST_SERV_GRADE_NAME,C.HIGH_WARN_ID,A.REGION_NAME,A.ACCEPT_ORG_ID,A.SEND_TO_ORG_NAME,"
				+ "W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,D.SIX_GRADE_CATALOG,(SELECT COUNT(1)FROM CC_SHEET_READ_RECORD RR WHERE NOW()<RR.READ_END_DATE AND "
				+ "RR.WORK_SHEET_ID=W.WORK_SHEET_ID AND RR.READ_STAFF_ID="+staff.getId()+")SHEET_READ,CASE WHEN W.SHEET_TYPE NOT IN"
				+ "(700000129,700001002,720130015)OR(W.SHEET_TYPE=700000129 AND W.FLOW_SEQUENCE=1)THEN W.RETURN_STAFF_NAME ELSE(SELECT WS1.DEAL_STAFF_NAME FROM "
				+ "CC_WORK_SHEET WS1 WHERE WS1.WORK_SHEET_ID=W.SOURCE_SHEET_ID)END LAST_DEAL_STAFF,IF(D.APPEAL_REASON_ID IN(11504,11505),5,IF(D.APPEAL_PROD_ID=111,6,"
				+ "IF(A.ACCEPT_CHANNEL_ID=707907012,7,IF(A.ACCEPT_CHANNEL_ID=707907011,8,9))))ORD,(SELECT O.REGION_ID FROM TSM_ORGANIZATION O WHERE O.ORG_ID="
				+ "A.ACCEPT_ORG_ID)ACCEPTREGION,L.FORCE_PRE_FLAG,IFNULL(L.ADJUST_ACCOUNT_FLAG,0)ADJUST_ACCOUNT_FLAG,IFNULL(L.DIRECT_DISPATCH_FLAG,0)DIRECT_DISPATCH_FLAG,L.UPGRADE_INCLINE,IFNULL(L.UP_LEVEL,1)UP_LEVEL,IFNULL("
				+ "L.HOTLINE_FLAG,0)HOTLINE_FLAG,IF(L.REPEAT_MAN_FLAG=0,0,L.REPEAT_AUTO_FLAG)REPEAT_AUTO_FLAG,IF(L.UPGRADE_MAN_FLAG=0,0,L.UPGRADE_AUTO_FLAG)"
				+ "UPGRADE_AUTO_FLAG,IFNULL(L.REPEAT_NEW_FLAG,0)REPEAT_NEW_FLAG,L.SEC_FLAG,IF(D.APPEAL_PROD_ID="
				+ "111,1,IF(D.APPEAL_REASON_ID=23002503,1,''))XHZW_FLAG,W.RECEIVE_ORG_ID,IF(LENGTH(W.SAVE_DEALCONTEN)>10,CONCAT(SUBSTR(W.SAVE_DEALCONTEN,1,10),'...'),W.SAVE_DEALCONTEN)SAVE_DEALCONTEN,"
				+ "(SELECT IFNULL(SUM(is_finish),-1) FROM cc_xc_flow WHERE main_sheet_id=w.work_sheet_id)XC_ING_NUM,C.CUST_TYPE,D.APPEAL_PROD_ID,D.APPEAL_REASON_ID,"
				+ "IFNULL(L.UP_TENDENCY_FLAG,0)UP_TENDENCY_FLAG,D.SERVICE_TYPE_DETAIL,C.CUST_NAME,A.AREA_NAME,A.RELA_INFO,D.FIVE_ORDER,D.BEST_ORDER,D.BEST_ORDER_DESC,"
				+ "A.RELA_MAN,(SELECT DATE_FORMAT(SA.CREAT_DATE,'%Y-%m-%d %H:%i:%s') FROM CC_WORK_SHEET_AREA SA "
				+ "WHERE SA.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND SA.TACHE_DATE IS NULL AND SA.AREA_FLAG=IF(W.TACHE_ID IN(720130021,700000085),3,IF(W.TACHE_ID IN"
				+ "(720130023,700000086),1,0))order by SA.CREAT_DATE DESC LIMIT 1)PD_BEGIN_DATE,DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN"
				+ "(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),"
				+ "W.HANGUP_START_TIME,W.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,DATE_FORMAT(A.FINISH_DATE,"
				+ "'%Y-%m-%d %H:%i:%s')FINISH_DATE,A.HANGUP_TIME_COUNT HANGUP_ORDER_COUNT,A.ORDER_LIMIT_TIME"
				+ ",D.PROD_ONE,D.PROD_ONE_DESC,D.PROD_TWO,D.PROD_TWO_DESC,D.DEVT_CHS_ONE,D.DEVT_CHS_ONE_DESC,D.DEVT_CHS_TWO,D.DEVT_CHS_TWO_DESC,D.DEVT_CHS_THREE,D.DEVT_CHS_THREE_DESC,"
				+ "A.RELA_TYPE,A.CALL_SERIAL_NO,D.APPEAL_REASON_DESC,D.APPEAL_CHILD,D.APPEAL_CHILD_DESC,A.CHANNEL_DETAIL_ID,"
				+ "CASE WHEN L.UP_TENDENCY_FLAG=1 THEN'有曝光倾向'WHEN L.UP_TENDENCY_FLAG=2 THEN'有工信部越级倾向或通管局投诉倾向'WHEN L.UP_TENDENCY_FLAG=3 THEN'有越级倾向'WHEN L.UP_TENDENCY_FLAG=4 THEN'最严重复'ELSE''END UP_TENDENCY_FLAG_DESC");
		String countSql = s1.replace("%PARAM%", "COUNT(1)");

		// 2012-09-04 LiJiahui 注意：此句中的order by 不可以写成大写字母ORDER BY
		// 因为getResult并不是对所有的记录排序，而且取出100条再排序 将order by写作小写可以规避该问题
		sql += " order by ORD, W.CREAT_DATE ASC";
		GridDataInfo infos = this.dbgridDataPub.getResultNewBySize(countSql, sql, begion, pageSize, "", DbgridStatic.GRID_FUNID_TS_MYSHEET);
		List<Map<String, Object>> list = infos.getList();
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				String beginDate = this.getStringByKey(map, "BEGIN_DATE");
				String orderDate = this.getStringByKey(map, "ACCEPT_DATE");
				String endDate = this.getStringByKey(map, "END_DATE");
				int hangupTimeCount = this.getIntByKey(map, "HANGUP_TIME_COUNT");
				int serviceType = this.getIntByKey(map, "SERVICE_TYPE");
				String sysDate = this.getStringByKey(map, "SYS_DATE");
				int comeCategory = this.getIntByKey(map, "COME_CATEGORY");
				int orderLimitTime = this.getIntByKey(map, "ORDER_LIMIT_TIME");
				int orderLimitCount = this.getIntByKey(map, "ORDER_TIME_COUNT");
				int orderType = comeCategory == 707907001 ? serviceType : 0;
				map.put("WORKTIME", pubFunc.getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate));
				map.put("NEXT_HOUR", pubFunc.getWorkingEnd(orderDate, orderDate, orderLimitTime, orderLimitCount * 60, orderType, sysDate));
				list.set(i, map);
			}
		}
		infos.setList(list);
		return infos;
	}

	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}

	private int getIntByKey(Map map, String key) {
		return map.get(key) == null ? 0 : Integer.parseInt(map.get(key).toString());
	}
}
