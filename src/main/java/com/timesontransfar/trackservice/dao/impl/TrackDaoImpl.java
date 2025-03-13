package com.timesontransfar.trackservice.dao.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.feign.custominterface.PortalInterfaceFeign;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.trackservice.dao.TrackDao;
import com.transfar.common.utils.StringUtils;

@Component(value="trackDao")
@SuppressWarnings("rawtypes")
public class TrackDaoImpl implements TrackDao{
	
	protected Logger log = LoggerFactory.getLogger(TrackDaoImpl.class);
	@Autowired
	private JdbcTemplate jt;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private ISystemAuthorization systemAuthorization;
	
	@Autowired
	private PortalInterfaceFeign portalFeign;
	
	@Override
	public List findOrderHasten(String orderId,boolean boo) {
		String hastenSql="SELECT T.WORK_SHEET_ID," + 
				"       T.SERVICE_ORDER_ID," + 
				"       T.SEND_STAFF_NAME," + 
				"       T.SEND_ORG_NAME," + 
				"       T.HASTEN_INFO," + 
				"       T.REGION_NAME," + 
				"       T.HASTEN_REASON_DESC," + 
				"       DATE_FORMAT(T.CREAT_DATE, '%Y-%m-%d %H:%i:%s') CREAT_DATE" + 
				"  FROM CC_HASTEN_SHEET T" + 
				" WHERE T.SERVICE_ORDER_ID =? " + 
				" ORDER BY T.CREAT_DATE DESC";
		if(!boo){
			hastenSql="SELECT T.WORK_SHEET_ID,T.SERVICE_ORDER_ID,T.SEND_STAFF_NAME,T.SEND_ORG_NAME,T.HASTEN_INFO,DATE_FORMAT(T.CREAT_DATE, '%Y-%m-%d %H:%i:%s') CREAT_DATE FROM CC_HASTEN_SHEET_HIS T WHERE T.SERVICE_ORDER_ID=? ORDER BY T.CREAT_DATE DESC";
		}
		return this.jt.queryForList(hastenSql,orderId);
	}
	
	@Override
	public List workSheetHisInfo(String prodNum, String relaInfo, int regionId, String orderId, boolean cliqueFlag) {
		String serviceTypeWhere = this.getServiceTypeWhere(cliqueFlag);
		
		String sql = "select * from (" +
				"SELECT t.COMMENTS,t.SERVICE_ORDER_ID,DATE_FORMAT(t.FINISH_DATE,'%Y-%m-%d %H:%i:%s') FINISH_DATE,t.ORDER_STATU_DESC,DATE_FORMAT(t.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\r\n" + 
				"       t.PROD_NUM,\r\n" + 
				"       t.RELA_INFO,\r\n" + 
				"       t.SERVICE_TYPE_DESC,\r\n" + 
				"       t.ACCEPT_STAFF_NAME,\r\n" + 
				"       t.ACCEPT_ORG_NAME,if(b.FINAL_OPTION_FLAG is null, '否', '是') FINAL_OPTION_FLAG FROM cc_service_order_ask t left join cc_service_label b on t.service_order_id = b.service_order_id where " + serviceTypeWhere + " t.accept_date > date_sub(now(),interval 30 day) and " + 
				"       t.region_id = ? and ( t.prod_num in ( ?, ? ) OR t.rela_info in ( ?, ? ) )\n" +
						"union \n" + 
						"SELECT t.COMMENTS,t.SERVICE_ORDER_ID,DATE_FORMAT(t.FINISH_DATE,'%Y-%m-%d %H:%i:%s') FINISH_DATE,t.ORDER_STATU_DESC,DATE_FORMAT(t.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\r\n" + 
						"       t.PROD_NUM,\r\n" + 
						"       t.RELA_INFO,\r\n" + 
						"       t.SERVICE_TYPE_DESC,\r\n" + 
						"       t.ACCEPT_STAFF_NAME,\r\n" + 
						"       t.ACCEPT_ORG_NAME,if(b.FINAL_OPTION_FLAG is null, '否', '是') FINAL_OPTION_FLAG FROM cc_service_order_ask_his t left join cc_service_label_his b on t.service_order_id = b.service_order_id where " + serviceTypeWhere + " t.accept_date > date_sub(now(),interval 3 month) and " + 
						"       t.region_id = ? and ( t.prod_num in ( ?, ? ) OR t.rela_info in ( ?, ? ) ) and t.order_statu in (700000103,720130010) " +
						") t where t.service_order_id != ? order by t.accept_date desc";
		log.info("workSheetHisInfo sql \n{}", sql);
		return jt.queryForList(sql, regionId, prodNum, relaInfo, prodNum, relaInfo, regionId, prodNum, relaInfo, prodNum, relaInfo, orderId);
	}
	
	public int workSheetHisCount(String prodNum, String relaInfo, int regionId, String orderId, boolean cliqueFlag) {
		String serviceTypeWhere = this.getServiceTypeWhere(cliqueFlag);
		
		String sql = "select count(1) from (" +
				"SELECT t.COMMENTS,t.SERVICE_ORDER_ID,DATE_FORMAT(t.FINISH_DATE,'%Y-%m-%d %H:%i:%s') FINISH_DATE,t.ORDER_STATU_DESC,DATE_FORMAT(t.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\r\n" + 
				"       t.PROD_NUM,\r\n" + 
				"       t.RELA_INFO,\r\n" + 
				"       t.SERVICE_TYPE_DESC,\r\n" + 
				"       t.ACCEPT_STAFF_NAME,\r\n" + 
				"       t.ACCEPT_ORG_NAME FROM cc_service_order_ask t where " + serviceTypeWhere + " t.accept_date > date_sub(now(),interval 30 day) and " + 
				"       t.region_id = ? and ( t.prod_num in ( ?, ? ) OR t.rela_info in ( ?, ? ) )\n" +
						"union \n" + 
						"SELECT t.COMMENTS,t.SERVICE_ORDER_ID,DATE_FORMAT(t.FINISH_DATE,'%Y-%m-%d %H:%i:%s') FINISH_DATE,t.ORDER_STATU_DESC,DATE_FORMAT(t.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\r\n" + 
						"       t.PROD_NUM,\r\n" + 
						"       t.RELA_INFO,\r\n" + 
						"       t.SERVICE_TYPE_DESC,\r\n" + 
						"       t.ACCEPT_STAFF_NAME,\r\n" + 
						"       t.ACCEPT_ORG_NAME FROM cc_service_order_ask_his t where " + serviceTypeWhere + " t.accept_date > date_sub(now(),interval 3 month) and " + 
						"       t.region_id = ? and ( t.prod_num in ( ?, ? ) OR t.rela_info in ( ?, ? ) ) and t.order_statu in (700000103,720130010) " +
						") t where t.service_order_id != ?";
		log.info("workSheetHisCount sql \n{}", sql);
		return jt.queryForObject(sql, new Object[] {regionId, prodNum, relaInfo, prodNum, relaInfo, regionId, prodNum, relaInfo, prodNum, relaInfo, orderId}, Integer.class);
	}
	
	private String getServiceTypeWhere(boolean cliqueFlag) {
		if(cliqueFlag) {
			return " t.service_type in (720130000, 700006312, 720200003, 700001171) and ";
		}
		return "";
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public List querySheetLimite(String sheetId) {
		String sql =
"SELECT W.SHEET_STATU,(SELECT DATE_FORMAT(SA.CREAT_DATE,'%Y-%m-%d %H:%i:%s')FROM CC_WORK_SHEET_AREA SA WHERE SA.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND "
+ "SA.TACHE_DATE IS NULL AND SA.AREA_FLAG=IF(W.TACHE_ID IN(720130021,700000085),3,IF(W.TACHE_ID IN(720130023,700000086),1,0))order by SA.CREAT_DATE "
+ "DESC LIMIT 1)PD_BEGIN_DATE,IFNULL((SELECT PD.PD_MINUTES FROM CC_SHEET_LIMITTIME_PD PD WHERE PD.TACHE_ID=W.TACHE_ID AND PD.COME_CATEGORY="
+ "A.COME_CATEGORY AND PD.IS_UNIFIED=IFNULL(L.IS_UNIFIED,0)),0)PDLIMIT,DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN(700001002,720130015),"
+ "W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),W.HANGUP_START_TIME,W.RESPOND_DATE),"
+ "'%Y-%m-%d %H:%i:%s')END_DATE,W.HANGUP_TIME_COUNT,DATE_FORMAT(W.HANGUP_START_TIME,'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,W.DEAL_LIMIT_TIME,"
+ "DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,A.SERVICE_TYPE,A.ORDER_LIMIT_TIME,"
+ "A.HANGUP_TIME_COUNT ORDER_TIME_COUNT,IF(A.COME_CATEGORY=707907001,A.SERVICE_TYPE,0)ORDER_TYPE,ROUND(TIMESTAMPDIFF(SECOND,W.HANGUP_START_TIME,NOW())"
+ "/86400,2)HANGUP_NOWTIME FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A,CC_SERVICE_LABEL L WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND "
+ "W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=?";
		List list = jt.queryForList(sql, sheetId);
		if (list.isEmpty()) {
			return list;
		}
			
		for(int i=0;i<list.size();i++) {
			Map map = (Map) list.get(0);
			int sheetStatu = this.getIntByKey(map, "SHEET_STATU");
			String pdBeginDate = this.getStringByKey(map, "PD_BEGIN_DATE");
			int pdlimit = this.getIntByKey(map, "PDLIMIT");
			String beginDate = this.getStringByKey(map, "BEGIN_DATE");
			String endDate = this.getStringByKey(map, "END_DATE");
			int hangupTimeCount = this.getIntByKey(map, "HANGUP_TIME_COUNT");
			String hangupStartTime = map.get("HANGUP_START_TIME") == null ? null : map.get("HANGUP_START_TIME").toString();
			if (700000046 == sheetStatu || 720130035 == sheetStatu) {
				hangupStartTime = null;
			}
			int dealLimitTime = this.getIntByKey(map, "DEAL_LIMIT_TIME");
			String sysDate = this.getStringByKey(map, "SYS_DATE");
			String acceptDate = this.getStringByKey(map, "ACCEPT_DATE");
			int serviceType = this.getIntByKey(map, "SERVICE_TYPE");
			int orderLimitTime = this.getIntByKey(map, "ORDER_LIMIT_TIME");
			int orderTimeCount = this.getIntByKey(map, "ORDER_TIME_COUNT");
			int orderType = this.getIntByKey(map, "ORDER_TYPE");
			int workTime = pubFunc.getWorkingTime(beginDate, acceptDate, endDate, hangupTimeCount * 60, serviceType, sysDate);
			int orderTime = pubFunc.getWorkingTime(acceptDate, acceptDate, sysDate, orderTimeCount * 60, orderType, sysDate);
			float zanbi = this.getDealPercent(orderTime, orderLimitTime);
			String nextHour = pubFunc.getWorkingEnd(acceptDate, acceptDate, orderLimitTime, orderTimeCount * 60, orderType, sysDate);
			String remainingtime = Math.round(((float) dealLimitTime * 60) - ((float) workTime / 60)) + "";
			if (!StringUtils.isEmpty(pdBeginDate) && 0 != pdlimit) {
				int pdTime = pubFunc.getWorkingTime(pdBeginDate, acceptDate, sysDate, 0, serviceType, sysDate);
				if (pdlimit * 60 > pdTime) {
					remainingtime = (pdlimit * 60 - pdTime) / 60 + "";
				}
			}
			map.put("HANGUP_START_TIME", hangupStartTime);
			map.put("ZANBI", zanbi);
			map.put("NEXT_HOUR", nextHour);
			map.put("REMAININGTIME", remainingtime);
			list.set(i, map);
		}
		return list;
	}
	
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	private int getIntByKey(Map map, String key) {
		return map.get(key) == null ? 0 : Integer.parseInt(map.get(key).toString());
	}
	
	private float getDealPercent(int orderTime, int orderLimitTime) {
		if(orderLimitTime == 0) {
			return 1;
		} else {
			return Math.round(((float) orderTime * 100) / ((float) orderLimitTime * 3600));
		}
	}
	
	public int getCallOutCount(String orderId) {
		String sql = "select COUNT(a.SERVICE_ORDER_ID) AS cn "
				+ "from CC_ORDER_CALLOUT_REC a where "
				+ "a.service_order_id = ?";
		return jt.queryForObject(sql, new Object[] { orderId }, Integer.class);
	}
	
	@SuppressWarnings({ "unchecked" })
	public List getCallOutRecord(String orderId, boolean curFlag) {
		String tableName = curFlag ? "cc_work_sheet" : "cc_work_sheet_his";
		String sql = "select a.SERVICE_ORDER_ID,\r\n"
				+ "   a.WORK_SHEET_ID,\r\n"
				+ "   a.CALL_GUID as FLOWID,\r\n"
				+ "   a.CALL_ID,\r\n"
				+ "   a.ORG_NAME,\r\n"
				+ "   CONCAT(t.STAFFNAME,'(',t.LOGONNAME,')') as STAFF_NAME,\r\n"
				+ "   t.STAFF_ID as STAFF_ID,\r\n"
				+ "   case a.REC_TYPE when '1' then '失败' else '成功' end as RECTYPE,\r\n"
				+ "   b.SHEET_STATU_DESC,\r\n"
				+ "   b.SHEET_STATU,\r\n"
				+ "   a.SOURCE_NUM,\r\n"
				+ "   a.DEST_NUM,\r\n"
				+ "   a.TALKLONGTIME,\r\n"
				+ "   DATE_FORMAT(a.CALL_ARRIVE, '%Y-%m-%d %H:%i:%s') CALL_ARRIVE,\r\n"
				+ "   DATE_FORMAT(a.CALL_ANSWER, '%Y-%m-%d %H:%i:%s') CALL_ANSWER,\r\n"
				+ "   DATE_FORMAT(a.CALL_FINISH, '%Y-%m-%d %H:%i:%s') CALL_FINISH,\r\n"
				+ "   case a.CALLOUT_TYPE when '1' then '工单外呼' else '前台呼出' end as CALLOUTTYPE,\r\n"
				+ "   case a.ACTION_TYPE when '1' then '是' else '否' end as ACTION_TYPE,\r\n"
				+ "   a.SATISFY_DEGREE,'' AUTH_TYPE, '' AUTH_RESULT,"
				+ "   (case a.SATISFY_DEGREE when '0' then '放弃' when '1' then '满意' when '2' then '一般' when '3' then '服务态度冷淡' when '4' then '业务解释听不懂' when '5' then '处理速度慢' when '6' then '处理方案未达到期望值' when '7' then '问题未解决' else '未邀评' end) SATISFY_DEGREE_DESC\r\n"
				+ "from CC_ORDER_CALLOUT_REC a, " + tableName + " b,tsm_staff t where a.STAFF_ID=t.STAFF_ID and a.work_sheet_id = b.work_sheet_id "
				+ "and a.service_order_id = ? order by a.call_arrive desc";
		List callList = this.jt.queryForList(sql, orderId);
		for(int i=0; !callList.isEmpty() && i<callList.size(); i++) {
			Map map = (Map) callList.get(i);
			if(map.get("CALL_ID") != null) {
				String callId = map.get("CALL_ID").toString();
				Map authMap = this.getAuthMap(callId);
				map.put("AUTH_TYPE", authMap.get("AUTH_TYPE_DESC") == null ? "" : authMap.get("AUTH_TYPE_DESC").toString());
				map.put("AUTH_RESULT", authMap.get("AUTH_RESULT_DESC") == null ? "" : authMap.get("AUTH_RESULT_DESC").toString());
				String confirmResult = this.getSecondConfirmResult(callId, map);
				map.put("CONFIRM_RESULT", confirmResult);
				callList.set(i, map);
			}
		}
		return callList;
	}

	@Override
	@SuppressWarnings({ "unchecked" })
	public List getCallOutForOrderInfo(String orderId, String orgId, String tableName) {
		List callList = new ArrayList();
		int callOutOrg = this.getCallOutOrg(orgId);
		if (callOutOrg != 0) {
			String sql = "select a.SERVICE_ORDER_ID,\r\n"
					+ "   a.WORK_SHEET_ID,\r\n"
					+ "   a.CALL_GUID as FLOWID,\r\n"
					+ "   a.CALL_ID,\r\n"
					+ "   a.ORG_NAME,\r\n"
					+ "   CONCAT(t.STAFFNAME,'(',t.LOGONNAME,')') as STAFF_NAME,\r\n"
					+ "   t.STAFF_ID as STAFF_ID,\r\n"
					+ "   case a.REC_TYPE when '1' then '失败' else '成功' end as RECTYPE,\r\n"
					+ "   b.SHEET_STATU_DESC,\r\n"
					+ "   b.SHEET_STATU,\r\n"
					+ "   a.SOURCE_NUM,\r\n"
					+ "   a.DEST_NUM,\r\n"
					+ "   a.TALKLONGTIME,\r\n"
					+ "   DATE_FORMAT(a.CALL_ANSWER, '%Y-%m-%d %H:%i:%s') CALL_ANSWER,\r\n"
					+ "   DATE_FORMAT(a.CALL_FINISH, '%Y-%m-%d %H:%i:%s') CALL_FINISH,\r\n"
					+ "   case a.CALLOUT_TYPE when '1' then '工单外呼' else '前台呼出' end as CALLOUTTYPE,\r\n"
					+ "   a.SATISFY_DEGREE,'' AUTH_TYPE, '' AUTH_RESULT,"
					+ "   (case a.SATISFY_DEGREE when '0' then '放弃' when '1' then '满意' when '2' then '一般' when '3' then '服务态度冷淡' when '4' then '业务解释听不懂' when '5' then '处理速度慢' when '6' then '处理方案未达到期望值' when '7' then '问题未解决' else '未邀评' end) SATISFY_DEGREE_DESC\r\n"
					+ "from CC_ORDER_CALLOUT_REC a, " + tableName + " b,tsm_staff t, tsm_organization o where a.STAFF_ID=t.STAFF_ID and a.work_sheet_id = b.work_sheet_id "
					+ "and o.ORG_ID = t.ORG_ID and a.service_order_id = ? "
					+ "and (o.LINKID in ('10-97','10-182','10-284','10-285','10-286','10-287','10-288','10-289','10-290','10-291','10-292','10-293','10-294') or left(o.LINKID,7) IN('10-285-','10-182-','10-182-','10-284-','10-285-','10-286-','10-287-','10-288-','10-289-','10-290-','10-291-','10-292-','10-293-','10-294-') or left(o.LINKID,6) IN('10-97-')) "
					+ "order by a.call_arrive desc";
			callList = this.jt.queryForList(sql, orderId);
			for (int i = 0; !callList.isEmpty() && i < callList.size(); i++) {
				Map map = (Map) callList.get(i);
				if (map.get("CALL_ID") != null) {
					String callId = map.get("CALL_ID").toString();
					Map authMap = this.getAuthMap(callId);
					map.put("AUTH_TYPE", authMap.get("AUTH_TYPE_DESC") == null ? "" : authMap.get("AUTH_TYPE_DESC").toString());
					map.put("AUTH_RESULT", authMap.get("AUTH_RESULT_DESC") == null ? "" : authMap.get("AUTH_RESULT_DESC").toString());
					String confirmResult = this.getSecondConfirmResult(callId, map);
					map.put("CONFIRM_RESULT", confirmResult);
					callList.set(i, map);
				}
			}
		}
		return callList;
	}

	public int getCallOutOrg(String orgId) {
		String sql = "select count(1) from tsm_organization where ORG_ID = ? and (LINKID in ('10-97','10-182','10-284','10-285','10-286','10-287','10-288','10-289','10-290','10-291','10-292','10-293','10-294') or left(LINKID,7) IN('10-285-','10-182-','10-182-','10-284-','10-285-','10-286-','10-287-','10-288-','10-289-','10-290-','10-291-','10-292-','10-293-','10-294-') or left(LINKID,6) IN('10-97-'))";
		return jt.queryForObject(sql, new Object[]{orgId}, Integer.class);
	}

	@SuppressWarnings({ "unchecked" })
	public List getSatisfyCallOutRecord(String orderId, boolean curFlag) {
		String tableName = curFlag ? "cc_unsatisfy_sheet" : "cc_unsatisfy_sheet_his";
		String sql = "select a.SERVICE_ORDER_ID,\r\n"
				+ "   a.WORK_SHEET_ID,\r\n"
				+ "   a.CALL_GUID as FLOWID,\r\n"
				+ "   a.CALL_ID,\r\n"
				+ "   a.ORG_NAME,\r\n"
				+ "   CONCAT(t.STAFFNAME,'(',t.LOGONNAME,')') as STAFF_NAME,\r\n"
				+ "   t.STAFF_ID as STAFF_ID,\r\n"
				+ "   case a.REC_TYPE when '1' then '失败' else '成功' end as RECTYPE,\r\n"
				+ "   case b.SHEET_STATUS when 0 then '待处理' when 1 then '处理中' when 2 then '完成' when 3 then '已派发' else '' end as SHEET_STATU_DESC,\r\n"
				+ "   a.SOURCE_NUM,\r\n"
				+ "   a.DEST_NUM,\r\n"
				+ "   a.TALKLONGTIME,\r\n"
				+ "   DATE_FORMAT(a.CALL_ANSWER, '%Y-%m-%d %H:%i:%s') CALL_ANSWER,\r\n"
				+ "   DATE_FORMAT(a.CALL_FINISH, '%Y-%m-%d %H:%i:%s') CALL_FINISH,\r\n"
				+ "   case a.CALLOUT_TYPE when '1' then '工单外呼' else '前台呼出' end as CALLOUTTYPE,\r\n"
				+ "   a.SATISFY_DEGREE,'' AUTH_TYPE, '' AUTH_RESULT,"
				+ "   (case a.SATISFY_DEGREE when '0' then '放弃' when '1' then '满意' when '2' then '一般' when '3' then '服务态度冷淡' when '4' then '业务解释听不懂' when '5' then '处理速度慢' when '6' then '处理方案未达到期望值' when '7' then '问题未解决' else '未邀评' end) SATISFY_DEGREE_DESC\r\n"
				+ "from CC_ORDER_CALLOUT_REC a, " + tableName + " b, tsm_staff t where a.STAFF_ID=t.STAFF_ID and a.work_sheet_id = b.work_sheet_id "
				+ "and a.service_order_id = ? order by a.call_arrive desc";
		List callList = this.jt.queryForList(sql, orderId);
		for(int i=0; !callList.isEmpty() && i<callList.size(); i++) {
			Map map = (Map) callList.get(i);
			if(map.get("CALL_ID") != null) {
				String callId = map.get("CALL_ID").toString();
				Map authMap = this.getAuthMap(callId);
				map.put("AUTH_TYPE", authMap.get("AUTH_TYPE_DESC") == null ? "" : authMap.get("AUTH_TYPE_DESC").toString());
				map.put("AUTH_RESULT", authMap.get("AUTH_RESULT_DESC") == null ? "" : authMap.get("AUTH_RESULT_DESC").toString());
				String confirmResult = this.getSecondConfirmResult(callId, map);
				map.put("CONFIRM_RESULT", confirmResult);
				callList.set(i, map);
			}
		}
		return callList;
	}

	// 查询最后一次回复录音
	public Map getLastRecallByOrderId(String orderId) {
		String sql = "SELECT CALL_GUID,CALL_ID,CALL_ANSWER,AGENT_ID FROM cc_order_callout_rec WHERE callout_type=1 AND rec_type=0 AND talklongtime IS "
				+ "NOT NULL AND service_order_id=?ORDER BY call_answer DESC";
		List list = jt.queryForList(sql, orderId);
		if (list.isEmpty()) {
			return Collections.emptyMap();
		}
		return (Map) list.get(0);
	}

	private Map getAuthMap(String ivrId) {
		List tmpList = portalFeign.getCsCallAuthenticateLogByIvrId(ivrId);
		if(!tmpList.isEmpty()) {
			return (Map)tmpList.get(0);
		}
		return Collections.emptyMap();
	}
	
	private String getSecondConfirmResult(String callId, Map map) {
		String result = "";
		if(map.get("CALL_ANSWER") != null && map.get("CALL_FINISH") != null) {
			String startTime = map.get("CALL_ANSWER").toString();
			String endTime = map.get("CALL_FINISH").toString();
			result = portalFeign.secondConfirmResult(startTime, endTime, callId);
		}
		return result == null ? "" : result;
	}
	
	@SuppressWarnings({ "unchecked" })
	public boolean getPlayVoiceFlag(String orderId, boolean curFlag) {
		String tableFlag = curFlag ? "" :"_HIS";
		String objId = curFlag ? "900018407" :"900018408";//工单外呼录音播放实体
		
		String sql = "SELECT S.SERVICE_ORDER_ID,S.REGION_ID,S.ACCEPT_ORG_ID,S.ACCEPT_STAFF_ID,W.WORK_SHEET_ID,W.RECEIVE_ORG_ID,W.DEAL_ORG_ID "
				+ "FROM CC_SERVICE_ORDER_ASK" + tableFlag + " S,CC_WORK_SHEET" + tableFlag + " W"
				+ " WHERE 1=1 AND S.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID ";
		
		Map map = new HashMap();
		map.put("CC_SERVICE_ORDER_ASK" + tableFlag, "S");
		map.put("CC_WORK_SHEET" + tableFlag, "W");
		String authSql = systemAuthorization.getAuthedSql(map, sql, objId);
		if(sql.equals(authSql)) {//没有配置实体因子
			return false;
		}
		authSql += " AND S.SERVICE_ORDER_ID=?";
		
		List sheetList = this.jt.queryForList(authSql, orderId);//CodeSec未验证的SQL注入；CodeSec误报：2
		return !sheetList.isEmpty();//配置实体因子
	}
	
	@SuppressWarnings("unchecked")
	public boolean getSatisfyPlayVoiceFlag(String orderId, boolean curFlag) {
		String tableFlag = curFlag ? "" : "_HIS";
		String objId = curFlag ? "900018413" :"900018414";//修复单外呼录音播放实体
		
		String sql = "SELECT W.WORK_ORDER_ID,W.WORK_SHEET_ID,W.RECEIVE_ORG_ID,W.DEAL_ORG_ID,W.RECEIVE_STAFF,W.DEAL_STAFF "
				+ "FROM CC_UNSATISFY_SHEET" + tableFlag + " W WHERE 1=1 ";
		
		Map map = new HashMap();
		map.put("CC_UNSATISFY_SHEET" + tableFlag, "W");
		String authSql = systemAuthorization.getAuthedSql(map, sql, objId);
		if(sql.equals(authSql)) {//没有配置实体因子
			return false;
		}
		authSql += " AND W.WORK_ORDER_ID='"+orderId+"'";
		
		List sheetList = this.jt.queryForList(authSql);//CodeSec未验证的SQL注入；CodeSec误报：2
		return !sheetList.isEmpty();//配置实体因子
	}

	@Override
	public boolean savePromise(String staffId, String staffName, int type) {
		String sql = "INSERT INTO cc_confirm_record (CREATE_DATE,OPER_STAFF_CODE,OPER_STAFF_NAME,OPER_TYPE) VALUES (NOW(),?,?,?)";
		int update = jt.update(sql, staffId, staffName, type);
		return update == 1;
	}

	@Override
	public boolean getPromise(String staffId, int type) {
		String sql = "SELECT ID FROM cc_confirm_record WHERE OPER_STAFF_CODE = ? AND OPER_TYPE = ?";
		List maps = jt.queryForList(sql, staffId, type);
		return !maps.isEmpty();
	}

	/**
	 * 根据受理时间和prodNum查询历史单
	 */
	@Override
	public List getWorkSheetHisByPordNum(String prodNum, String beginTime,String endTime,String orderId) {
		String sql = "SELECT * FROM" +
				"(" +
				"SELECT t.COMMENTS,t.SERVICE_ORDER_ID,DATE_FORMAT( t.FINISH_DATE, '%Y-%m-%d %H:%i:%s' ) FINISH_DATE,t.CALL_SERIAL_NO," +
				"t.ORDER_STATU_DESC,DATE_FORMAT( t.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s' ) ACCEPT_DATE,t.PROD_NUM,t.RELA_INFO,B.ACCEPT_CONTENT " +
				"FROM cc_service_order_ask t ,cc_service_content_ask B WHERE " +
				"t.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID " +
				"AND t.PROD_NUM = ? " +
				"AND t.ACCEPT_DATE >= ? " +
				"AND t.ACCEPT_DATE <= ? " +
				"AND t.SERVICE_TYPE = 720130000 " +
				"AND t.order_statu = 720130007  " +
				"AND t.SERVICE_ORDER_ID != ? " +
				"union " +
				"SELECT t.COMMENTS,t.SERVICE_ORDER_ID,DATE_FORMAT( t.FINISH_DATE, '%Y-%m-%d %H:%i:%s' ) FINISH_DATE,t.CALL_SERIAL_NO," +
				"t.ORDER_STATU_DESC,DATE_FORMAT( t.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s' ) ACCEPT_DATE,t.PROD_NUM,t.RELA_INFO,B.ACCEPT_CONTENT " +
				"FROM cc_service_order_ask_his t ,cc_service_content_ask_his B WHERE " +
				"t.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID "+
				"AND t.ORDER_VESION = B.ORDER_VESION "+
				"AND t.PROD_NUM = ? " +
				"AND t.ACCEPT_DATE >= ? " +
				"AND t.ACCEPT_DATE <= ? " +
				"AND t.SERVICE_TYPE = 720130000 "+
				"AND t.order_statu IN ( 700000103, 720130010 ) " +
				"AND t.SERVICE_ORDER_ID != ? "+
				") t " +
				"ORDER BY " +
				"t.accept_date";
		log.info("getWorkSheetHisByPordNum sql \n{}", sql);
		List<Map<String, Object>> list = jt.queryForList(sql, prodNum, beginTime, endTime,orderId,prodNum, beginTime, endTime,orderId);
		for (Map<String, Object> map : list) {//查询处理结果
			String serviceOrderId = (String) map.get("SERVICE_ORDER_ID");//服务单号
			if (serviceOrderId != null) {
				String lastDealContent = pubFunc.getLastDealContentHis(serviceOrderId);//处理结果
				map.put("DEAL_CONTENT", lastDealContent);
			}
		}
		return list;
	}
}
