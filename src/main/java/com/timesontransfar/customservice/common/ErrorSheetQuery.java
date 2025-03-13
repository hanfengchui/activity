package com.timesontransfar.customservice.common;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;

@Component("ErrorSheetQuery")
public class ErrorSheetQuery {
	private static final Logger log = LoggerFactory.getLogger(ErrorSheetQuery.class);
	
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	@Autowired
	private PubFunc pubFunc;
	private String begindate = "BEGIN_DATE";
	private String acceptdate = "ACCEPT_DATE";
	private String enddate = "END_DATE";
	private String hanguptimecount = "HANGUP_TIME_COUNT";
	private String servicetype = "SERVICE_TYPE";
	private String sysdate = "SYS_DATE";

    /**
	 * 取得无收单部门列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GridDataInfo getNullOrgSheet(int begion,String strWhere) {
		String strSql = 
"SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,A.ORDER_VESION,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,"
+ "'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,A.ACCEPT_CHANNEL_ID,DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_COME_FROM_DESC,"
+ "W.SHEET_STATU_DESC,W.HASTENT_NUM,A.ACCEPT_COUNT,W.RECEIVE_STAFF_NAME,A.SERVICE_TYPE_DESC,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.DEAL_ORG_NAME,"
+ "W.RECEIVE_ORG_NAME,W.PRE_ALARM_VALUE,W.SHEET_TYPE_DESC,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,D.APPEAL_PROD_NAME,C.CUST_SERV_GRADE_NAME,A.REGION_NAME,"
+ "W.SHEET_TYPE,A.ACCEPT_COME_FROM,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')LOCK_DATE,W.SHEET_STATU,DATE_FORMAT(W.HANGUP_START_TIME,"
+ "'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN("
+ "700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(720130035,700000046),"
+ "W.HANGUP_START_TIME, W.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,A.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE FROM CC_WORK_SHEET W,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C WHERE W.REGION_ID=A.REGION_ID AND W.LOCK_FLAG IN(0,1)AND W.RECEIVE_ORG_ID IN("
+ "'A','B','C','D')AND W.SHEET_STATU NOT IN(600000450,720130033)AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID "
+ "AND A.CUST_GUID=C.CUST_GUID";
		if (!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if (andIndex == -1) {
				log.info("getNullOrgSheet查询条件中没有AND,请检查WHERE条件");
				return null;
			}
			strSql = strSql + strWhere;
		}
		String format = String.format("getNullOrgSheet查询SQL语句为:%s", strSql);
		log.info(format);
		GridDataInfo bean = this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", "");
		List<Map<String, Object>> list = bean.getList();
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				String beginDate = map.get(begindate) == null ? "" : map.get(begindate).toString();
				String orderDate = map.get(acceptdate) == null ? "" : map.get(acceptdate).toString();
				String endDate = map.get(enddate) == null ? "" : map.get(enddate).toString();
				int hangupTimeCount = map.get(hanguptimecount) == null ? 0 : Integer.parseInt(map.get(hanguptimecount).toString());
				int serviceType = map.get(servicetype) == null ? 0 : Integer.parseInt(map.get(servicetype).toString());
				String sysDate = map.get(sysdate) == null ? "" : map.get(sysdate).toString();
				map.put("WORKTIME", pubFunc.getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate));
				list.set(i, map);
			}
		}
		bean.setList(list);
		return bean;
	}

	/**
	 * 取得无处理部门列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GridDataInfo getNullDealOrgSheet(int begion,String strWhere) {
		String strSql = 
"SELECT W.SERVICE_ORDER_ID,A.ORDER_VESION,W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,"
+ "'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,"
+ "W.SHEET_STATU_DESC,W.HASTENT_NUM,A.ACCEPT_COUNT,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.PRE_ALARM_VALUE,W.DEAL_ORG_NAME,W.RECEIVE_ORG_NAME,"
+ "W.RECEIVE_STAFF_NAME,W.SHEET_TYPE_DESC,A.SERVICE_TYPE_DESC,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')"
+ "LOCK_DATE,W.SHEET_STATU,DATE_FORMAT(W.HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,A.ACCEPT_COME_FROM,W.SHEET_TYPE,D.APPEAL_PROD_NAME,"
+ "C.CUST_SERV_GRADE_NAME,A.REGION_NAME,W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN("
+ "700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),"
+ "W.HANGUP_START_TIME,W.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,A.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE FROM CC_WORK_SHEET W,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C WHERE W.REGION_ID=A.REGION_ID AND(W.DEAL_ORG_ID IS NULL OR W.DEAL_ORG_ID='')AND "
+ "W.LOCK_FLAG=1 AND W.SHEET_STATU NOT IN(600000450,720130033,700000047,720130036)AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID="
+ "D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID";
		if (!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if (andIndex == -1) {
				log.info("getNullDealOrgSheet查询条件中没有AND,请检查WHERE条件");
				return null;
			}
			strSql = strSql + strWhere;
		}
		String format = String.format("getNullDealOrgSheet查询SQL语句为:%s", strSql);
		log.info(format);
		GridDataInfo bean = this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", "");
		List<Map<String, Object>> list = bean.getList();
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				String beginDate = map.get(begindate) == null ? "" : map.get(begindate).toString();
				String orderDate = map.get(acceptdate) == null ? "" : map.get(acceptdate).toString();
				String endDate = map.get(enddate) == null ? "" : map.get(enddate).toString();
				int hangupTimeCount = map.get(hanguptimecount) == null ? 0 : Integer.parseInt(map.get(hanguptimecount).toString());
				int serviceType = map.get(servicetype) == null ? 0 : Integer.parseInt(map.get(servicetype).toString());
				String sysDate = map.get(sysdate) == null ? "" : map.get(sysdate).toString();
				map.put("WORKTIME", pubFunc.getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate));
				list.set(i, map);
			}
		}
		bean.setList(list);
		return bean;
	}

	/**
	 * 取得超时列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public GridDataInfo getTimeOutSheet(int begion,String strWhere,int hour) {
		String strSql = 
"SELECT W.SERVICE_ORDER_ID,A.ORDER_VESION,W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,"
+ "'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,"
+ "W.SHEET_STATU_DESC,W.HASTENT_NUM,A.ACCEPT_COUNT,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.PRE_ALARM_VALUE,W.RECEIVE_STAFF_NAME,W.SHEET_TYPE_DESC,"
+ "W.DEAL_ORG_NAME,W.RECEIVE_ORG_NAME,A.SERVICE_TYPE_DESC,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')LOCK_DATE,"
+ "W.SHEET_STATU,DATE_FORMAT(W.HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,A.ACCEPT_COME_FROM,W.SHEET_TYPE,D.APPEAL_PROD_NAME,"
+ "C.CUST_SERV_GRADE_NAME,A.REGION_NAME,W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN("
+ "700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),"
+ "W.HANGUP_START_TIME,W.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,A.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE FROM CC_WORK_SHEET W,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C WHERE W.REGION_ID=A.REGION_ID AND A.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID AND "
+ "A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND (TIMESTAMPDIFF(SECOND,IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN(700001002,"
+ "720130015),W.CREAT_DATE,W.LOCK_DATE)),NOW())-W.HANGUP_TIME_COUNT*60)/3600>W.DEAL_LIMIT_TIME+"+hour+" AND W.SHEET_STATU NOT IN(700000046,720130035)AND "
+ "W.LOCK_FLAG IN(0,1)AND W.DEAL_LIMIT_TIME > 0";
		if (!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if (andIndex == -1) {
				log.info("getTimeOutSheet查询条件中没有AND,请检查WHERE条件");
				return null;
			}
			strSql = strSql + strWhere;
		}
		String format = String.format("getTimeOutSheet查询SQL语句为:%s", strSql);
		log.info(format);
		//改成模糊查询超时
		GridDataInfo bean = this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", "");
		List<Map<String, Object>> list = bean.getList();
		if (!list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				Map map = list.get(i);
				String beginDate = map.get(begindate) == null ? "" : map.get(begindate).toString();
				String orderDate = map.get(acceptdate) == null ? "" : map.get(acceptdate).toString();
				String endDate = map.get(enddate) == null ? "" : map.get(enddate).toString();
				int hangupTimeCount = map.get(hanguptimecount) == null ? 0 : Integer.parseInt(map.get(hanguptimecount).toString());
				int serviceType = map.get(servicetype) == null ? 0 : Integer.parseInt(map.get(servicetype).toString());
				String sysDate = map.get(sysdate) == null ? "" : map.get(sysdate).toString();
				map.put("WORKTIME", pubFunc.getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate));
				list.set(i, map);
			}
		}
		bean.setList(list);
		return bean;
	}
}