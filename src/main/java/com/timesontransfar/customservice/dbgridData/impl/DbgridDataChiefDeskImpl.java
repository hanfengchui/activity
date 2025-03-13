package com.timesontransfar.customservice.dbgridData.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IDbgridDataChiefDeskService;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;

@Component(value="deskGrid")
public class DbgridDataChiefDeskImpl implements IDbgridDataChiefDeskService {
	@Autowired
	private ISystemAuthorization systemAuthorization;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/**
	 * 班长台的申请审批页面，查询释放人工申请、升级人工申请、督办人工申请
	 * @param begin
	 * @param strWhere where&&applyType
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GridDataInfo querySheetStatuApply(int begin,String strWhere){
		String[] ar = strWhere.split("&&");
		String applyType = ar[1];
		
		String sql = querySheetStatuApply + ar[0];
		if("selected".equals(applyType)){
			String sql1 = sql + " AND A.APPLY_TYPE =1";
			//权限匹配 释放申请和升级申请，做了权限匹配
			Map tableMap = new HashMap();
			tableMap.put("CC_SHEET_STATU_APPLY", "A");
			tableMap.put("CC_SERVICE_ORDER_ASK", "S");
			tableMap.put("CC_WORK_SHEET", "W");
			sql1 = this.systemAuthorization.getAuthedSql(tableMap, sql1, "900018262");
			
			String sql2 = sql + " AND A.APPLY_TYPE IN (3,4)";
			
			sql = sql1 +" union " + sql2;
		}else if("1".equals(applyType)){//释放申请
			sql += " AND A.APPLY_TYPE =" + applyType;
			//权限匹配 释放申请和升级申请，做了权限匹配
			Map tableMap = new HashMap();
			tableMap.put("CC_SHEET_STATU_APPLY", "A");
			tableMap.put("CC_SERVICE_ORDER_ASK", "S");
			tableMap.put("CC_WORK_SHEET", "W");
			sql = this.systemAuthorization.getAuthedSql(tableMap, sql, "900018262");
		}else if("3".equals(applyType)||"4".equals(applyType)){//督办申请；升级申请
			sql += " AND A.APPLY_TYPE =" + applyType;
		}
		
		sql = " select * from (" + sql + ") T";
		return this.dbgridDataPub.getResult(sql, begin, " ORDER BY LOCK_DATE", DbgridStatic.GRID_FUNID_CHIEFDESK_RELEASE);
	}

	/**
	 * 班长台的申请审批页面，查批量申请
	 * @param begin
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryPatch(int begin,String strWhere){
		String sql = queryPatch + strWhere;
		return this.dbgridDataPub.getResult(sql, begin, "", DbgridStatic.GRID_FUNID_CHIEFDESK_PATCH);
	}
	
	/**
	 * 班长台，列表数据查询
	 * @param begin
	 * @param strWhere where&&type. <br>
	 * 			type ALL全部（我的任务和工单池的集合）；ASSIGNED已分派(我的任务)；UNASSIGN未派发（工单池）
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GridDataInfo queryChiefDesk(int begin, String strWhere){
		String sql = null;
		String [] args = strWhere.split("&&");
		if(args.length == 2){
			String type = args[1];
			if("ALL".equals(type)){
				Map tableMap1 = new HashMap();
				tableMap1.put("CC_WORK_SHEET", "W");
				tableMap1.put("CC_SERVICE_ORDER_ASK", "S");
		        tableMap1.put("CC_SERVICE_CONTENT_ASK", "C");
		        String tmp1 = this.systemAuthorization.getAuthedSql(tableMap1, queryChiefDeskWait, "900018300");
		        Map tableMap2 = new HashMap();
		        tableMap2.put("CC_WORK_SHEET", "W");
		        tableMap2.put("CC_SERVICE_ORDER_ASK", "S");
		        tableMap2.put("CC_SERVICE_CONTENT_ASK", "C");
		        String tmp2 = this.systemAuthorization.getAuthedSql(tableMap2, queryChiefDeskDeal, "900018301");
				sql = tmp1 + args[0] + " UNION " + tmp2 + args[0];
			}else if("UNASSIGN".equals(type)){
				Map tableMap1 = new HashMap();
				tableMap1.put("CC_WORK_SHEET", "W");
				tableMap1.put("CC_SERVICE_ORDER_ASK", "S");
		        tableMap1.put("CC_SERVICE_CONTENT_ASK", "C");
		        String tmp1 = this.systemAuthorization.getAuthedSql(tableMap1, queryChiefDeskWait, "900018300");
		        sql = tmp1 + args[0];
			}else if("ASSIGNED".equals(type)){
				Map tableMap2 = new HashMap();
		        tableMap2.put("CC_WORK_SHEET", "W");
		        tableMap2.put("CC_SERVICE_ORDER_ASK", "S");
		        tableMap2.put("CC_SERVICE_CONTENT_ASK", "C");
		        String tmp2 = this.systemAuthorization.getAuthedSql(tableMap2, queryChiefDeskDeal, "900018301");
		        sql = tmp2 + args[0];
			}else{
				GridDataInfo info = new GridDataInfo();
				info.setQuryCount(0);
				info.setList(new ArrayList());
				return info;
			}
			sql = 
"SELECT SHEET_TYPE,SERVICE_ORDER_ID,WORK_SHEET_ID,PROD_NUM,REGION_ID,REGION_NAME,RELA_INFO,SERVICE_TYPE,SERVICE_TYPE_DESC,DATE_FORMAT(ACCEPT_DATE,"
+ "'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,DATE_FORMAT(LOCK_DATE,'%Y-%m-%d %H:%i:%s')LOCK_DATE,"
+ "ACCEPT_COME_FROM,ACCEPT_COME_FROM_DESC,COME_CATEGORY,TACHE_ID,TACHE_DESC,SHEET_STATU,SHEET_STATU_DESC,RECEIVE_ORG_ID,RECEIVE_ORG_NAME,RECEIVE_STAFF,"
+ "RECEIVE_STAFF_NAME,DEAL_ORG_ID,DEAL_ORG_NAME,DEAL_STAFF,DEAL_STAFF_NAME,HASTENT_NUM,URGENCY_GRADE,URGENCY_GRADE_DESC,CUST_SERV_GRADE,"
+ "CUST_SERV_GRADE_DESC,APPEAL_PROD_ID,APPEAL_PROD_NAME,CUST_GUID,MONTH_FLAG,REPEAT_AUTO_FLAG,UPGRADE_AUTO_FLAG,FORCE_PRE_FLAG,UPGRADE_INCLINE,"
+ "UP_LEVEL,LOCK_FLAG FROM("+sql+")X WHERE 1=1 ";
			return this.dbgridDataPub.getResult(sql, begin, " ORDER BY CREAT_DATE ASC", DbgridStatic.GRID_FUNID_CHIEFDESK_LIST);
		}
		
		GridDataInfo info = new GridDataInfo();
		info.setQuryCount(0);
		info.setList(new ArrayList());
		return info;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List query4Dispatch(String where){
		String sql = query4Dispatch + " " + where;
		GridDataInfo info = dbgridDataPub.getResult(sql, 0, "", DbgridStatic.GRID_FUNID_CHIEFDESK_PATCHDSP);
		return info == null ? null : info.getList();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public List queryPatchdsp(String where, int num){
		String sql = queryPatchdsp + " " + where;
		 // 权限匹配
        Map tableMap = new HashMap();
        tableMap.put("CC_WORK_SHEET", "W");
        tableMap.put("CC_SERVICE_ORDER_ASK", "S");
        String s = this.systemAuthorization.getAuthedSql(tableMap, sql, "900018300");
        
        return this.jdbcTemplate.queryForList(s + " limit " + num);//CodeSec未验证的SQL注入；CodeSec误报：2
	}
	
	private String queryPatch = 
			"SELECT W.TACHE_ID, W.TACHE_DESC,\n" +
					"             A.APPLY_STAFF, A.APPLY_STAFF_NAME,\n" + 
					"             A.APPLY_TYPE, DECODE(A.APPLY_TYPE, 0, '挂起申请', 1, '释放申请', 2, '协办申请', 3, '建督办单申请', 4, '升级申请', '未知类型') APPLY_TYPE_DESC,\n" + 
					"             A.APPLY_AUD_STATU DEAL_STATU, DECODE(A.APPLY_AUD_STATU, 0, '待审批', 1, '同意', 2, '不同意', 3, '已协办', '未知状态') DEAL_STATU_DESC,\n" + 
					"             W.RECEIVE_ORG_ID, W.RECEIVE_ORG_NAME\n" + 
					"        FROM CC_SHEET_STATU_APPLY A, CC_SERVICE_ORDER_ASK S, CC_WORK_SHEET W\n" + 
					"       WHERE S.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID AND A.WORKSHEET_ID = W.WORK_SHEET_ID";

	private String queryChiefDeskWait = 
"SELECT W.SHEET_TYPE,S.SERVICE_ORDER_ID,W.WORK_SHEET_ID,S.PROD_NUM,S.REGION_ID,S.REGION_NAME,S.RELA_INFO,S.SERVICE_TYPE,S.SERVICE_TYPE_DESC,"
+ "S.ACCEPT_DATE,W.CREAT_DATE,W.LOCK_DATE,S.ACCEPT_COME_FROM,S.ACCEPT_COME_FROM_DESC,S.COME_CATEGORY,W.TACHE_ID,W.TACHE_DESC,W.SHEET_STATU,"
+ "W.SHEET_STATU_DESC,W.RECEIVE_ORG_ID,W.RECEIVE_ORG_NAME,W.RECEIVE_STAFF,W.RECEIVE_STAFF_NAME,W.DEAL_ORG_ID,W.DEAL_ORG_NAME,W.DEAL_STAFF,"
+ "W.DEAL_STAFF_NAME,(SELECT COUNT(1)FROM CC_HASTEN_SHEET PP WHERE PP.SERVICE_ORDER_ID=S.SERVICE_ORDER_ID)HASTENT_NUM,S.URGENCY_GRADE,"
+ "S.URGENCY_GRADE_DESC,S.CUST_SERV_GRADE,S.CUST_SERV_GRADE_DESC,C.APPEAL_PROD_ID,C.APPEAL_PROD_NAME,S.CUST_GUID,S.MONTH_FLAG,IF(L.REPEAT_MAN_FLAG=0,"
+ "0,L.REPEAT_AUTO_FLAG)REPEAT_AUTO_FLAG,IF(L.UPGRADE_MAN_FLAG=0,0,L.UPGRADE_AUTO_FLAG)UPGRADE_AUTO_FLAG,L.FORCE_PRE_FLAG,L.UPGRADE_INCLINE,IFNULL("
+ "L.UP_LEVEL,1)UP_LEVEL,W.LOCK_FLAG FROM CC_SERVICE_ORDER_ASK S,CC_SERVICE_CONTENT_ASK C,CC_WORK_SHEET W,CC_SERVICE_LABEL L WHERE S.SERVICE_ORDER_ID="
+ "C.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND S.SERVICE_DATE=3 AND W.LOCK_FLAG=0 AND "
+ "W.SHEET_TYPE NOT IN(700001000)AND W.TACHE_ID NOT IN(3000025)AND W.SHEET_STATU NOT IN(600000450,720130033)";

	private String queryChiefDeskDeal = 
"SELECT W.SHEET_TYPE,S.SERVICE_ORDER_ID,W.WORK_SHEET_ID,S.PROD_NUM,S.REGION_ID,S.REGION_NAME,S.RELA_INFO,S.SERVICE_TYPE,S.SERVICE_TYPE_DESC,"
+ "S.ACCEPT_DATE,W.CREAT_DATE,W.LOCK_DATE,S.ACCEPT_COME_FROM,S.ACCEPT_COME_FROM_DESC,S.COME_CATEGORY,W.TACHE_ID,W.TACHE_DESC,W.SHEET_STATU,"
+ "W.SHEET_STATU_DESC,W.RECEIVE_ORG_ID,W.RECEIVE_ORG_NAME,W.RECEIVE_STAFF,W.RECEIVE_STAFF_NAME,W.DEAL_ORG_ID,W.DEAL_ORG_NAME,W.DEAL_STAFF,"
+ "W.DEAL_STAFF_NAME,(SELECT COUNT(1)FROM CC_HASTEN_SHEET PP WHERE PP.SERVICE_ORDER_ID=S.SERVICE_ORDER_ID)HASTENT_NUM,S.URGENCY_GRADE,"
+ "S.URGENCY_GRADE_DESC,S.CUST_SERV_GRADE,S.CUST_SERV_GRADE_DESC,C.APPEAL_PROD_ID,C.APPEAL_PROD_NAME,S.CUST_GUID,S.MONTH_FLAG,IF(L.REPEAT_MAN_FLAG=0,"
+ "0,L.REPEAT_AUTO_FLAG)REPEAT_AUTO_FLAG,IF(L.UPGRADE_MAN_FLAG=0,0,L.UPGRADE_AUTO_FLAG)UPGRADE_AUTO_FLAG,L.FORCE_PRE_FLAG,L.UPGRADE_INCLINE,IFNULL("
+ "L.UP_LEVEL,1)UP_LEVEL,W.LOCK_FLAG FROM CC_SERVICE_ORDER_ASK S,CC_SERVICE_CONTENT_ASK C,CC_WORK_SHEET W,CC_SERVICE_LABEL L WHERE S.SERVICE_ORDER_ID="
+ "C.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND S.SERVICE_DATE=3 AND W.LOCK_FLAG IN(1,3) "
+ "AND W.SHEET_TYPE NOT IN(700001000)AND W.TACHE_ID NOT IN(3000025)";

	/**
	 * sql 查询 释放申请审批数据
	 */
	private String querySheetStatuApply = 
			"SELECT W.SHEET_TYPE,S.SERVICE_ORDER_ID,W.WORK_SHEET_ID,S.PROD_NUM,S.REGION_ID,S.REGION_NAME,\n" +
					"        DATE_FORMAT(S.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\n" +
					"        DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s') LOCK_DATE,\n" +
					"        W.TACHE_ID,W.TACHE_DESC,A.APPLY_STAFF,A.APPLY_STAFF_NAME,\n" + 
					"        A.APPLY_TYPE, CASE a.apply_type WHEN 0 THEN '挂起申请' WHEN 1 THEN '释放申请' WHEN 2 THEN '协办申请' WHEN 3 THEN '督办申请' WHEN 4 THEN '升级申请' ELSE '未知类型' END AS APPLY_TYPE_DESC,\n" +
					"        a.apply_aud_statu DEAL_STATU,CASE a.apply_aud_statu WHEN 0 THEN '待审批' WHEN 1 THEN '同意' WHEN 2 THEN '不同意' WHEN 3 THEN '已协办' ELSE '未知类型' END AS DEAL_STATU_DESC,\n" +
					"        W.SHEET_STATU,W.SHEET_STATU_DESC,W.RECEIVE_ORG_ID,W.RECEIVE_ORG_NAME,\n" + 
					"        S.ACCEPT_COME_FROM,S.ACCEPT_COME_FROM_DESC,S.URGENCY_GRADE,S.URGENCY_GRADE_DESC,\n" + 
					"        S.CUST_SERV_GRADE,S.CUST_SERV_GRADE_DESC,S.MONTH_FLAG,\n" + 
					"            A.APPLY_GUID,A.APPLY_REASON\n" + 
					"      FROM CC_SHEET_STATU_APPLY A, CC_SERVICE_ORDER_ASK S, CC_WORK_SHEET W\n" + 
					"      WHERE S.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID\n" + 
					"        AND A.WORKSHEET_ID = W.WORK_SHEET_ID";
	/**
	 * sql 查询 班长的工单池
	 */
	private String queryPatchdsp = 
			"SELECT W.WORK_SHEET_ID,S.REGION_ID,S.MONTH_FLAG FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK S\n" +
					"      WHERE W.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID\n" + 
					"      AND S.SERVICE_DATE = 3\n" + 
					"      AND W.LOCK_FLAG = 0\n" + 
					"      AND W.SHEET_STATU NOT IN (600000450, 720130033)\n" + 
					"      AND W.SHEET_TYPE != 700001000";
	/**
	 * sql 查询 符合批量派发条件的工单
	 */
	private String query4Dispatch = 
			"SELECT W.SHEET_TYPE,S.SERVICE_ORDER_ID,W.WORK_SHEET_ID,S.PROD_NUM,S.REGION_ID,S.SERVICE_TYPE,\n" +
					"             S.ACCEPT_DATE,W.CREAT_DATE,W.LOCK_DATE,W.TACHE_ID,W.SHEET_STATU,W.RECEIVE_ORG_ID,\n" + 
					"             W.RECEIVE_ORG_NAME,S.MONTH_FLAG,W.LOCK_FLAG\n" + 
					"        FROM CC_SERVICE_ORDER_ASK S, CC_SERVICE_CONTENT_ASK C, CC_WORK_SHEET W, CC_SERVICE_LABEL L\n" + 
					"       WHERE S.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID AND S.SERVICE_ORDER_ID = L.SERVICE_ORDER_ID\n" + 
					"         AND S.SERVICE_DATE = 3 AND W.LOCK_FLAG = 0\n" + 
					"         AND W.SHEET_STATU IN ('700000044','700000048','600000002','600000004','720130031')";
}
