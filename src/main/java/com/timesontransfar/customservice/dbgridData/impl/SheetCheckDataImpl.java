/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.dbgridData.IsheetCheckData;

@Component(value="sheetCheckData")
@SuppressWarnings("rawtypes")
public class SheetCheckDataImpl implements IsheetCheckData {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ISystemAuthorization systemAuthorization;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	
	/**
	 * 得到质检流水
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getSheetCheck(int begion,String strWhere) {
		String strSql = "SELECT A.CHECK_ID,A.TYPE_ID,A.TYPE_NAME,A.SERVICE_ORDER_ID, A.WORK_SHEET_ID, "+
						"A.CHECK_ORG_ID,A.CHECK_ORG_NAME,A.CHECK_STAFF_ID,A.CHECK_STAFF_NAME,A.ORG_ID, "+
						"A.ORG_NAME,A.STAFF_ID,A.STAFF_NAME,A.CONTENT_DESC,A.EVALUSTE, "+
						"date_format(A.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE FROM CC_SHEET_CHECK A WHERE 1=1";
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		return this.dbgridDataPub.getResult(strSql, begion, " ", DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK);
	}
	/**
	 * 获取质检列表在途工单
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryOnRoadSheets(int begion,String strWhere) {
		String strSql = 
			"SELECT C.WORK_SHEET_ID,S.SERVICE_ORDER_ID," +
			" DATE_FORMAT(C.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE," +
			" DATE_FORMAT(S.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" +
			" S.PROD_NUM,C.DEAL_ORG_NAME,C.DEAL_STAFF_NAME," +
			" S.MONTH_FLAG , C.REGION_ID,C.REGION_NAME,\n" + 
			" DATE_FORMAT(C.RESPOND_DATE, '%Y-%m-%d %H:%i:%s') AS RESPOND_DATE," +
			" C.RETURN_STAFF_NAME ,C.TACHE_DESC ," + 
			" S.ACCEPT_COME_FROM,S.ACCEPT_COME_FROM_DESC,S.CUST_GUID,S.ORDER_STATU,L.TS_DEAL_RESULT_NAME, \n" + 
			" (SELECT COUNT(1) FROM CC_SHEET_CHECK C WHERE C.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID) SFYZJ \n" + 
			" FROM CC_SERVICE_CONTENT_ASK A, CC_SERVICE_LABEL L, CC_SERVICE_ORDER_ASK S\n" + 
			" JOIN CC_WORK_SHEET C\n" + 
			" ON S.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID WHERE 1=1 AND C.SHEET_STATU in('700000047','720130036') ";
		strSql=strSql+strWhere+" AND S.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID = L.SERVICE_ORDER_ID";
		return this.dbgridDataPub.getResult(strSql, begion, " ", DbgridStatic.GRID_FUNID_QUERY_SHEETCHECK_ONROAD);
	}
	
	/**
	 * 获取质检列表竣工工单
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryFinishSheets(int begion,String strWhere) {
	    String strSql = "SELECT C.WORK_SHEET_ID," +
	            " C.SERVICE_ORDER_ID," +
	            " DATE_FORMAT(C.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE," +
	            " DATE_FORMAT(S.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE," +
	            " S.PROD_NUM," +
	            " C.DEAL_ORG_NAME," +
	            " C.DEAL_STAFF_NAME," +
	            " C.REGION_ID," +
	            " C.REGION_NAME," +
	            " DATE_FORMAT(C.RESPOND_DATE, '%Y-%m-%d %H:%i:%s') AS RESPOND_DATE," +
	            " C.RETURN_STAFF_NAME," +
	            " S.ACCEPT_COME_FROM," +
	            " S.ACCEPT_COME_FROM_DESC," +
	            " C.TACHE_DESC," +
	            " C.MONTH_FLAG," +
	            " S.CUST_GUID," +
	            " S.ORDER_STATU," +
	            " L.TS_DEAL_RESULT_NAME," +
	            "(SELECT COUNT(1) FROM CC_SHEET_CHECK C WHERE C.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID) SFYZJ" +
	            " FROM CC_WORK_SHEET_HIS C,CC_SERVICE_ORDER_ASK_HIS S,CC_SERVICE_CONTENT_ASK_HIS A, CC_SERVICE_LABEL_HIS L" +
	            " WHERE C.SHEET_STATU IN ( '700000047','720130036')" +
	            " AND C.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID" +
	            " AND S.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID" +
	            " AND S.SERVICE_ORDER_ID = L.SERVICE_ORDER_ID" +
                " AND S.ORDER_VESION = A.ORDER_VESION" +
                " AND S.ORDER_STATU IN (700000103, 3000047, 720130002, 720130010) ";
		return this.dbgridDataPub.getResult(strSql+strWhere, begion, " ORDER BY ACCEPT_DATE ", DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_FINISH);
	}
	
	/**
	 * 获取质检列表-申诉列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GridDataInfo queryAppealCheckSheets(int begion,String strWhere) {
		String strSql = 
			"SELECT T.CHECK_ID,T.TYPE_ID,T.TYPE_NAME,T.SERVICE_ORDER_ID,T.WORK_SHEET_ID\n" +
			",T.CHECK_ORG_NAME,T.CHECK_STAFF_NAME,T.ORG_NAME,T.STAFF_NAME,DATE_FORMAT(T.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE ,T.CHECK_OUT \n" +
			" FROM CC_SHEET_CHECK  T WHERE  T.CHECK_STATE='600006272' "; 
		
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		//900018324
		//权限匹配
		
		Map tableMap = new HashMap();
		tableMap.put("CC_SHEET_CHECK", "T");	 		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018324");//工单质检申诉确认实体
		return this.dbgridDataPub.getResult(strSql, begion, " ", DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_APPEALLIST);
	}
	
	/**
	 ** 获取质检列表-修改列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public GridDataInfo queryUpdateCheckSheets(int begion, String strWhere) {
		String strSql = 
			"SELECT " +
			"T.CHECK_ID,\n" +
			"T.TYPE_ID,\n" +
			"T.TYPE_NAME,\n" +
			"T.SERVICE_ORDER_ID,\n" +
			"T.WORK_SHEET_ID,\n" +
			"T.CHECK_ORG_NAME,\n" +
			"T.CHECK_STAFF_NAME,\n" +
			"T.ORG_NAME,\n" +
			"T.STAFF_NAME,\n" +
			"T.CHECK_OUT,\n" +
			"DATE_FORMAT(T.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE  \n" +
			" FROM CC_SHEET_CHECK  T WHERE   1=1  "; 
		
		if(!"".equals(strWhere)) {
			strSql=strSql+strWhere;
		}
		Map tableMap = new HashMap();
		tableMap.put("CC_SHEET_CHECK", "T");	 		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018325");//工单质检修改实体 
		return this.dbgridDataPub.getResult(strSql, begion, " ", DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_UPDATES);
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
}