package com.timesontransfar.customservice.dbgridData.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IDepartmentCount;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.worksheet.dao.impl.NoteSenListDao;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component("departmentCount")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DepartmentCountImpl implements IDepartmentCount {
	/**日志*/
	private static final Logger log = LoggerFactory.getLogger(DepartmentCountImpl.class);
	
	/**jdbc模型**/
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	/** 共用数据访问接口**/
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	/**公共类*/
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private NoteSenListDao noteSen;
	@Autowired
	private IworkSheetBusi workSheetBusi; //workSheetBusi

	private static final String RETURNORG_FLG = "RETURNORG_FLG";
	private static final String RETURN_ORG_NAME = "RETURN_ORG_NAME";
	private static final String RETURNORG = "RETURNORG";
	private static final String RECEIVE_ORG_NAME = "RECEIVE_ORG_NAME";
	private static final String RETURNCREATE = "RETURNCREATE";
	private static final String CREATE_DATE = "CREATE_DATE";
	

	/**
	 * 导部门
	 */
	public GridDataInfo getMonitorExport(int begion,String strWhere){
		StringBuffer str = new StringBuffer();
str.append("SELECT B.LOCK_FLAG,ROUND(TIMESTAMPDIFF(MINUTE,A.ACCEPT_DATE,NOW())/60,1)ORDERTIME1,ROUND((A.ORDER_LIMIT_TIME*60-TIMESTAMPDIFF(MINUTE,"
+ "A.ACCEPT_DATE,NOW()))/60,1)ORDERTIME2,ROUND((B.DEAL_LIMIT_TIME*60-TIMESTAMPDIFF(MINUTE,B.LOCK_DATE,NOW()))/60,1)SHEETTIME1,ROUND(TIMESTAMPDIFF("
+ "MINUTE,B.LOCK_DATE,NOW())/60,1)SHEETTIME2,A.SERVICE_ORDER_ID,B.WORK_SHEET_ID,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
+ "DATE_FORMAT(B.LOCK_DATE,'%Y-%m-%d %H:%i:%s')CREATE_DATE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,B.RECEIVE_ORG_NAME,B.RECEIVE_STAFF_NAME,"
+ "B.TACHE_DESC,B.TACHE_ID,B.RECEIVE_ORG_ID,B.SHEET_STATU_DESC,B.DEAL_LIMIT_TIME,B.SERVICE_TYPE,(SELECT K.APPEAL_REASON_ID FROM CC_SERVICE_CONTENT_ASK"
+ " K WHERE K.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID)APPEAL_REASON_ID,A.ORDER_LIMIT_TIME,B.RETURN_ORG_NAME FROM CC_SERVICE_ORDER_ASK A,CC_WORK_SHEET B WHERE "
+ "A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND B.SHEET_STATU NOT IN(600000450,720130033)AND B.SHEET_STATU NOT IN(700000047,720130036)AND B.SHEET_STATU "
+ "NOT IN(700000049,720130037)AND B.LOCK_FLAG IN(0,1)AND B.SHEET_TYPE!=3000036 ");
		if(!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if(andIndex == -1){
				if(log.isDebugEnabled()){
					log.debug("WHERE 条件中没有AND,请检查WHERE条件"+strWhere);
					return null;
				}
			} else {
				str.append(strWhere);
			}			
		}
		List tmp = this.jdbcTemplate.queryForList(str.toString());	
		for(int i=0;i<tmp.size();i++){
			Map obj = (Map)tmp.get(i);
			String orderid = obj.get("SERVICE_ORDER_ID").toString();
			List list = workSheetBusi.getReturnTime(orderid);
			if (list.isEmpty()) {
				obj.put(RETURNORG_FLG, this.getStringByKey(obj, RETURN_ORG_NAME));
				obj.put(RETURNORG, this.getStringByKey(obj, RECEIVE_ORG_NAME));
				obj.put(RETURNCREATE, this.getStringByKey(obj, CREATE_DATE));
			} else {
				Map map = (Map) list.get(0);
				obj.put(RETURNORG_FLG, this.getStringByKey(map, RETURNORG_FLG));
				obj.put(RETURNORG, this.getStringByKey(map, RETURNORG));
				obj.put(RETURNCREATE, this.getStringByKey(map, RETURNCREATE));
			}
		}		
		GridDataInfo bean = new GridDataInfo();
		bean.setQuryCount(tmp.size());
		bean.setList(tmp);	
		return bean;
    }

    public GridDataInfo getSatisfyExport(int begion,String strWhere){
		StringBuffer str = new StringBuffer();
		str.append("SELECT B.LOCK_FLAG,TIMESTAMPDIFF(HOUR, A.CREATE_DATE, NOW()) ORDERTIME1,(CAST(A.order_limit AS SIGNED) - TIMESTAMPDIFF(HOUR, A.CREATE_DATE, NOW())) AS ORDERTIME2,\n" +
				" (B.sheet_limit / 60 - TIMESTAMPDIFF(HOUR, B.CREATE_DATE, NOW())) AS SHEETTIME1,TIMESTAMPDIFF(HOUR, B.CREATE_DATE, NOW()) SHEETTIME2,\n" +
				" B.WORK_ORDER_ID as SERVICE_ORDER_ID,B.WORK_SHEET_ID,A.PROD_NUM,DATE_FORMAT( A.CREATE_DATE, '%Y-%m-%d %H:%i:%s' ) ACCEPT_DATE,\n" +
				" DATE_FORMAT( B.CREATE_DATE, '%Y-%m-%d %H:%i:%s' ) CREATE_DATE,DATE_FORMAT( NOW(), '%Y-%m-%d %H:%i:%s' ) SYS_DATE,B.RECEIVE_ORG_NAME,\n" +
				" B.RECEIVE_STAFF_NAME,B.TACHE_DESC,B.TACHE_ID,B.RECEIVE_ORG_ID,\n" +
				"CASE B.sheet_status WHEN 0 THEN '待处理' WHEN 1 THEN '处理中' WHEN 2 THEN '已处理'  WHEN 3 THEN '已派发' WHEN 9 THEN '挂起' ELSE ' ' \n" +
				"END AS SHEET_STATU_DESC,(B.sheet_limit/60) DEAL_LIMIT_TIME,\n" +
				"( SELECT K.APPEAL_REASON_ID FROM CC_SERVICE_CONTENT_ASK K WHERE K.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID ) APPEAL_REASON_ID,\n" +
				"\tA.ORDER_LIMIT ORDER_LIMIT_TIME,B.RETURN_ORG_NAME AS RETURNORG_FLG,B.RECEIVE_ORG_NAME AS RETURNORG,\n" +
				"\tDATE_FORMAT(B.DISTILL_DATE, '%Y-%m-%d %H:%i:%s') AS RETURNCREATE\n" +
				"FROM cc_unsatisfy_order A,cc_unsatisfy_sheet B WHERE A.work_order_id = B.work_order_id AND B.sheet_status != 3 AND B.LOCK_FLAG IN ( 0, 1 ) ");
		if(!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if(andIndex == -1){
				if(log.isDebugEnabled()){
					log.debug("WHERE 条件中没有AND,请检查WHERE条件"+strWhere);
					return null;
				}
			} else {
				str.append(strWhere);
			}
		}
		List tmp = this.jdbcTemplate.queryForList(str.toString());
		GridDataInfo bean = new GridDataInfo();
		bean.setQuryCount(tmp.size());
		bean.setList(tmp);
		return bean;
    }
    
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	/**
	 * 导员工
	 */
	public GridDataInfo getMonitorExportStaff(int begion,String strWhere){
		StringBuffer str = new StringBuffer();		
str.append("SELECT B.LOCK_FLAG,ROUND(TIMESTAMPDIFF(MINUTE,A.ACCEPT_DATE,NOW())/60,1)ORDERTIME1,ROUND((A.ORDER_LIMIT_TIME*60-TIMESTAMPDIFF(MINUTE,"
+ "A.ACCEPT_DATE,NOW()))/60,1)ORDERTIME2,ROUND((B.DEAL_LIMIT_TIME*60-TIMESTAMPDIFF(MINUTE,B.LOCK_DATE,NOW()))/60,1)SHEETTIME1,ROUND(TIMESTAMPDIFF("
+ "MINUTE,B.LOCK_DATE,NOW())/60,1)SHEETTIME2,A.SERVICE_ORDER_ID,B.WORK_SHEET_ID,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
+ "DATE_FORMAT(B.LOCK_DATE,'%Y-%m-%d %H:%i:%s')CREATE_DATE,B.RECEIVE_ORG_NAME,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,B.RECEIVE_STAFF_NAME,"
+ "B.TACHE_DESC,B.TACHE_ID,B.RECEIVE_ORG_ID,B.SHEET_STATU_DESC,B.DEAL_LIMIT_TIME,B.SERVICE_TYPE,(SELECT K.APPEAL_REASON_ID FROM CC_SERVICE_CONTENT_ASK"
+ " K WHERE K.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID)APPEAL_REASON_ID,A.ORDER_LIMIT_TIME,B.RETURN_ORG_NAME FROM CC_SERVICE_ORDER_ASK A,CC_WORK_SHEET B WHERE "
+ "A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND B.SHEET_STATU NOT IN(600000450,720130033)AND B.SHEET_STATU NOT IN(700000047,720130036)AND B.SHEET_STATU "
+ "NOT IN(700000049,720130037)AND B.LOCK_FLAG IN(0,1)AND B.SHEET_TYPE!=3000036 ");			
		
		if(!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if(andIndex == -1){
				if(log.isDebugEnabled()){
					log.debug("WHERE 条件中没有AND,请检查WHERE条件"+strWhere);
					return null;
				}
			} else {
				str.append(strWhere);
			}			
		}
        List tmp = this.jdbcTemplate.queryForList(str.toString());	
        for(int i=0;i<tmp.size();i++){
			Map obj = (Map)tmp.get(i);			
			String orderid = obj.get("SERVICE_ORDER_ID").toString();
			List list = workSheetBusi.getReturnTime(orderid);
			if (list.isEmpty()) {
				obj.put(RETURNORG_FLG, this.getStringByKey(obj, RETURN_ORG_NAME));
				obj.put(RETURNORG, this.getStringByKey(obj, RECEIVE_ORG_NAME));
				obj.put(RETURNCREATE, this.getStringByKey(obj, CREATE_DATE));
			} else {
				Map map = (Map) list.get(0);
				obj.put(RETURNORG_FLG, this.getStringByKey(map, RETURNORG_FLG));
				obj.put(RETURNORG, this.getStringByKey(map, RETURNORG));
				obj.put(RETURNCREATE, this.getStringByKey(map, RETURNCREATE));
			}
		}		
        GridDataInfo bean = new GridDataInfo();
		bean.setQuryCount(tmp.size());
		bean.setList(tmp);	
		return bean;
    }

    public GridDataInfo getSatisfyExportStaff(int begion,String strWhere){
		StringBuffer str = new StringBuffer();
str.append("SELECT B.LOCK_FLAG,TIMESTAMPDIFF(HOUR, A.CREATE_DATE, NOW()) ORDERTIME1,(CAST(A.order_limit AS SIGNED) - TIMESTAMPDIFF(HOUR, A.CREATE_DATE, NOW())) AS ORDERTIME2,\n" +
		" (B.sheet_limit / 60 - TIMESTAMPDIFF(HOUR, B.CREATE_DATE, NOW())) AS SHEETTIME1,TIMESTAMPDIFF(HOUR, B.CREATE_DATE, NOW()) SHEETTIME2,\n" +
		" B.WORK_ORDER_ID as SERVICE_ORDER_ID,B.WORK_SHEET_ID,A.PROD_NUM,DATE_FORMAT( A.CREATE_DATE, '%Y-%m-%d %H:%i:%s' ) ACCEPT_DATE,\n" +
		" DATE_FORMAT( B.CREATE_DATE, '%Y-%m-%d %H:%i:%s' ) CREATE_DATE,DATE_FORMAT( NOW(), '%Y-%m-%d %H:%i:%s' ) SYS_DATE,B.RECEIVE_ORG_NAME,\n" +
		" B.RECEIVE_STAFF_NAME,B.TACHE_DESC,B.TACHE_ID,B.RECEIVE_ORG_ID,\n" +
		"CASE B.sheet_status WHEN 0 THEN '待处理' WHEN 1 THEN '处理中' WHEN 2 THEN '已处理'  WHEN 3 THEN '已派发' WHEN 9 THEN '挂起' ELSE ' ' \n" +
		"END AS SHEET_STATU_DESC,(B.sheet_limit/60) DEAL_LIMIT_TIME,\n" +
		"( SELECT K.APPEAL_REASON_ID FROM CC_SERVICE_CONTENT_ASK K WHERE K.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID ) APPEAL_REASON_ID,\n" +
		"\tA.ORDER_LIMIT ORDER_LIMIT_TIME,B.RETURN_ORG_NAME AS RETURNORG_FLG,B.RECEIVE_ORG_NAME AS RETURNORG,\n" +
		"\tDATE_FORMAT(B.DISTILL_DATE, '%Y-%m-%d %H:%i:%s') AS RETURNCREATE\n" +
		"FROM cc_unsatisfy_order A,cc_unsatisfy_sheet B WHERE A.work_order_id = B.work_order_id AND B.sheet_status != 3 AND B.LOCK_FLAG IN ( 0, 1 ) ");

		if(!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if(andIndex == -1){
				if(log.isDebugEnabled()){
					log.debug("WHERE 条件中没有AND,请检查WHERE条件"+strWhere);
					return null;
				}
			} else {
				str.append(strWhere);
			}
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("cc_unsatisfy_sheet", "b");
		tableMap.put("cc_unsatisfy_order", "a");
        List tmp = this.jdbcTemplate.queryForList(str.toString());
        GridDataInfo bean = new GridDataInfo();
		bean.setQuryCount(tmp.size());
		bean.setList(tmp);
		return bean;
    }

	/**
	 * 获取部门监控
	 */
	public GridDataInfo getOrgMonitor(int begion, String areaId ,String orgid,String departmentId){
		GridDataInfo gdf = null;
		
		StringBuffer str = new StringBuffer();
	    str.append("SELECT DISTINCT RECEIVE_ORG_ID,RECEIVE_ORG_NAME , COUNTNUM FROM (" );
		str.append("  SELECT COUNT(RECEIVE_ORG_ID) AS COUNTNUM," );
		str.append("  RECEIVE_ORG_ID,"  );
		str.append("  RECEIVE_ORG_NAME"  );
	    str.append(" FROM CC_WORK_SHEET CC ,cc_service_order_ask b" );
		str.append(" WHERE CC.SERVICE_ORDER_ID= B.SERVICE_ORDER_ID  AND CC.LOCK_FLAG IN (0,1) " );
		str.append(" AND CC.SHEET_STATU not in(600000450,720130033) "  );//已派发
		str.append(" AND CC.SHEET_STATU not in(700000047,720130036)"  );//已完成
		str.append(" AND CC.SHEET_STATU not in(700000049,720130037)" );//作废
		str.append(" AND CC.SHEET_TYPE != 3000036");//错单
		
		if(!areaId.equals("2")){
		    str.append(" AND RECEIVE_REGION_ID = "+areaId+" "  );
		}
		
		if(orgid != null && orgid.length() > 0){
			str.append(" AND RECEIVE_ORG_ID IN ( ");
			str.append(" SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE ");
			String[] sts = orgid.split(",");
			for (int i = 0; i < sts.length; i++) {
				str.append(i == 0 ? " A.LINKID LIKE CONCAT_WS('', (SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + sts[i] + "') , '%') "
						: " OR A.LINKID LIKE CONCAT_WS('', (SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + sts[i] + "') , '%') ");
			}
			str.append(" ) ");
		}
		if(null !=  departmentId && departmentId.length() > 0){
			str.append(departmentId);
		}
		
		str.append(" GROUP BY RECEIVE_ORG_NAME, RECEIVE_ORG_ID )X WHERE 1 = 1 " );
		gdf = dbgridDataPub.getResult(str.toString(), begion, "","");
		return gdf;
	}
	public GridDataInfo getOrgSatisfyMonitor(int begion, String areaId ,String orgid,String departmentId){
		GridDataInfo gdf = null;

		StringBuffer str = new StringBuffer();
	    str.append("SELECT DISTINCT RECEIVE_ORG_ID,RECEIVE_ORG_NAME , COUNTNUM FROM (" );
		str.append("  SELECT COUNT(RECEIVE_ORG_ID) AS COUNTNUM," );
		str.append("  RECEIVE_ORG_ID,"  );
		str.append("  RECEIVE_ORG_NAME"  );
	    str.append(" FROM cc_unsatisfy_sheet CC ,cc_unsatisfy_order b" );
		str.append(" WHERE CC.work_order_id= B.work_order_id  AND CC.LOCK_FLAG IN (0,1) " );
		str.append(" AND CC.sheet_status != 3  "  );//已派发
		str.append(" AND CC.sheet_status != 2"  );//已完成

		if(!areaId.equals("2")){
		    str.append(" AND cc.region_id = "+areaId+" "  );
		}

		if(orgid != null && orgid.length() > 0){
			str.append(" AND RECEIVE_ORG_ID IN ( ");
			str.append(" SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE ");
			String[] sts = orgid.split(",");
			for (int i = 0; i < sts.length; i++) {
				str.append(i == 0 ? " A.LINKID LIKE CONCAT_WS('', (SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + sts[i] + "') , '%') "
						: " OR A.LINKID LIKE CONCAT_WS('', (SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + sts[i] + "') , '%') ");
			}
			str.append(" ) ");
		}
		if(null !=  departmentId && departmentId.length() > 0){
			str.append(departmentId);
		}

		str.append(" GROUP BY RECEIVE_ORG_NAME, RECEIVE_ORG_ID )X WHERE 1 = 1 " );
		gdf = dbgridDataPub.getResult(str.toString(), begion, "","");
		return gdf;
	}
	
	public GridDataInfo getStaffMonitorNew(int begion,String areaId,String staffId,String departmentId){
        GridDataInfo gdf = null;        
        String[] sts = staffId.split(",");
		StringBuffer str = new StringBuffer();
	    str.append("SELECT DISTINCT RECEIVE_STAFF,RECEIVE_STAFF_NAME , COUNTNUM FROM (" );
		str.append("  SELECT COUNT(RECEIVE_STAFF) AS COUNTNUM," );
		str.append("  RECEIVE_STAFF,"  );
		str.append("  RECEIVE_STAFF_NAME "  );
	    str.append(" FROM CC_WORK_SHEET CC ,cc_service_order_ask b " );
		str.append(" WHERE CC.SERVICE_ORDER_ID= B.SERVICE_ORDER_ID AND CC.LOCK_FLAG IN (0,1)" );
		str.append(" AND CC.SHEET_STATU not in(600000450,720130033)"  );//已派发
		str.append(" AND CC.SHEET_STATU not in(700000047,720130036)"  );//已完成
		str.append(" AND CC.SHEET_STATU not in(700000049,720130037)" );//作废
		str.append(" AND CC.SHEET_TYPE != 3000036");//错单
		if(!areaId.equals("2")){
		    str.append(" AND RECEIVE_REGION_ID = "+areaId+""  );
        }
		str.append(" AND RECEIVE_STAFF in (");
		
		for(int i=0;i<sts.length;i++){
			str.append(i==0?sts[i]:","+sts[i]);
		}
		str.append(" )");
		
		if(null != departmentId){
			str.append(departmentId);
		}
		str.append(" GROUP BY RECEIVE_STAFF_NAME, RECEIVE_STAFF)X");
		
		gdf = dbgridDataPub.getResult(str.toString(), begion, "", "");
		return gdf;
	}

	public GridDataInfo getStaffSatisfyMonitorNew(int begion,String areaId,String staffId,String departmentId){
        GridDataInfo gdf = null;
        String[] sts = staffId.split(",");
		StringBuffer str = new StringBuffer();
	    str.append("SELECT DISTINCT RECEIVE_STAFF,RECEIVE_STAFF_NAME , COUNTNUM FROM (" );
		str.append("  SELECT COUNT(RECEIVE_STAFF) AS COUNTNUM," );
		str.append("  RECEIVE_STAFF,"  );
		str.append("  RECEIVE_STAFF_NAME "  );
	    str.append(" FROM cc_unsatisfy_sheet CC ,cc_unsatisfy_order b " );
		str.append(" WHERE CC.work_order_id= B.work_order_id AND CC.LOCK_FLAG IN (0,1)" );
		str.append(" AND CC.sheet_status != 3 "  );//已派发
		str.append(" AND CC.sheet_status != 2 "  );//已完成
		if(!areaId.equals("2")){
		    str.append(" AND CC.region_id = "+areaId+""  );
        }
		str.append(" AND CC.RECEIVE_STAFF in (");

		for(int i=0;i<sts.length;i++){
			str.append(i==0?sts[i]:","+sts[i]);
		}
		str.append(" )");

		if(null != departmentId){
			str.append(departmentId);
		}
		str.append(" GROUP BY RECEIVE_STAFF_NAME, RECEIVE_STAFF)X");

		gdf = dbgridDataPub.getResult(str.toString(), begion, "", "");
		return gdf;
	}
		
	/**
	 * 获取工单池统计数据
	 * @param  areaId   地区ID
	 * @param  departmentId  部门ID
	 * @return 工单池统计数据
	 */
	
	public GridDataInfo getWorkSinglePondCountMessages(int begion, String areaId, String departmentId, String serviceType){
		GridDataInfo gdf = null;
		boolean first = true;
		StringBuffer sb = new StringBuffer();
		String[] split = departmentId.split(",");
		sb.append("SELECT COUNT(RECEIVE_ORG_ID) AS COUNTNUM,RECEIVE_ORG_ID,RECEIVE_ORG_NAME,LOCK_FLAG,RECEIVE_REGION_ID");
		sb.append(" FROM CC_WORK_SHEET CC where cc.lock_flag = 0 AND cc.SHEET_STATU not in(600000450,720130033) and cc.SHEET_STATU not in(700000049,720130037)  AND RECEIVE_REGION_ID =");
		sb.append(areaId);
		if(StringUtils.isNotBlank(serviceType)){
			sb.append(" AND SERVICE_TYPE = ");
			sb.append(serviceType);
		}
		sb.append(" GROUP BY  RECEIVE_ORG_NAME , RECEIVE_ORG_ID,LOCK_FLAG,RECEIVE_REGION_ID having ");
		for (String s : split) {
			if (!first) {
				sb.append(" OR ");
			}
			sb.append("RECEIVE_ORG_ID = '").append(s);
			sb.append("'");
			first = false;
		}
		gdf = dbgridDataPub.getResult(sb.toString(), begion, "", "");
		return gdf;
	}

	public GridDataInfo getSatisfyCountMessages(String areaId ,String departmentId){
		GridDataInfo gdf = null;
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT COUNT(RECEIVE_ORG_ID) AS COUNTNUM,RECEIVE_ORG_ID,RECEIVE_ORG_NAME,LOCK_FLAG,region_id RECEIVE_REGION_ID ");
		sb.append(" FROM cc_unsatisfy_sheet CC where cc.lock_flag = 0 AND cc.sheet_status = 0  AND CC.region_id =");
		sb.append(areaId);
		sb.append(" GROUP BY  RECEIVE_ORG_NAME , RECEIVE_ORG_ID,LOCK_FLAG,region_id having RECEIVE_ORG_ID = '");
		sb.append(departmentId);
		sb.append("'");
		gdf = dbgridDataPub.getResult(sb.toString(), 0, "","");
		return gdf;
	}
	
	
	
	/**
	 * 获取我的任务统计数据
	 * @param  areaId   地区ID
	 * @param  departmentId  部门ID
	 * @return 我的任务统计数据
	 */
	public GridDataInfo getMyWorkSingleCountMessages(int begion, String areaId, String departmentId, String serviceType){
		GridDataInfo gdf = null;
		boolean first = true;
		String[] split = departmentId.split(",");
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT COUNTNUM,RECEIVE_ORG_ID,RECEIVE_ORG_NAME,RECEIVE_REGION_ID ,13 as LOCK_FLAG  FROM (");
		sb.append(" SELECT COUNT(RECEIVE_ORG_ID) AS countNum,DEAL_ORG_ID AS RECEIVE_ORG_ID,DEAL_ORG_NAME AS RECEIVE_ORG_NAME,RECEIVE_REGION_ID FROM");
		sb.append(" CC_WORK_SHEET CC where (cc.lock_flag =1 or cc.lock_flag = 3) and CC.SHEET_STATU not in(700000049,720130037)   AND RECEIVE_REGION_ID =");
		sb.append(areaId);
		if(StringUtils.isNotBlank(serviceType)){
			sb.append(" AND SERVICE_TYPE = ");
			sb.append(serviceType);
		}
		sb.append(" GROUP BY  DEAL_ORG_NAME,DEAL_ORG_ID,RECEIVE_REGION_ID having ");
		for (String s : split) {
			if (!first) {
				sb.append(" OR ");
			}
			sb.append("DEAL_ORG_ID ='").append(s);
			sb.append("'");
			first = false;
		}
		sb.append(" )X");
		gdf = dbgridDataPub.getResult(sb.toString(), begion, "", "");
		return gdf;
	}

	public GridDataInfo getMyWorkSatisfyCountMessages(String areaId ,String departmentId){
		GridDataInfo gdf = null;
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT COUNTNUM,RECEIVE_ORG_ID,RECEIVE_ORG_NAME,RECEIVE_REGION_ID RECEIVE_REGION_ID,13 as LOCK_FLAG  FROM (");
		sb.append(" SELECT COUNT(RECEIVE_ORG_ID) AS countNum,DEAL_ORG_ID AS RECEIVE_ORG_ID,DEAL_ORG_NAME AS RECEIVE_ORG_NAME,CC.region_id RECEIVE_REGION_ID FROM");
		sb.append(" cc_unsatisfy_sheet CC where (cc.lock_flag =1) and CC.sheet_status not in(0,3)   AND CC.region_id =");
		sb.append(areaId);
		sb.append(" GROUP BY  DEAL_ORG_NAME,DEAL_ORG_ID,region_id having DEAL_ORG_ID ='");
		sb.append(departmentId);
		sb.append("')X");
		gdf = dbgridDataPub.getResult(sb.toString(), 0, "","");
		return gdf;
	}
	
	/**
	 * 获取派发任务统计数据
	 * @param  areaId   地区ID
	 * @param  departmentId  部门ID
	 * @return 派发任务统计数据
	 */
	public GridDataInfo getSentWorkSingleCountMessages(int begion,String areaId ,String departmentId, String serviceType ){
		GridDataInfo gdf = null;
		boolean first = true;
		String[] split = departmentId.split(",");
		StringBuilder sql = new StringBuilder();
		sql.append("select COUNTNUM,");
		sql.append("RETURN_ORG_ID AS RECEIVE_ORG_ID,");
		sql.append("RETURN_ORG_NAME AS RECEIVE_ORG_NAME,");
		sql.append("RECEIVE_REGION_ID,");
		sql.append("'600000450' AS LOCK_FLAG");
		sql.append(" from (SELECT COUNT(RECEIVE_ORG_ID) AS countNum,");
		sql.append(" RETURN_ORG_ID,");
		sql.append(" RETURN_ORG_NAME,");
		sql.append(" RECEIVE_REGION_ID");
		sql.append(" FROM CC_WORK_SHEET CC");
		sql.append(" where CC.SHEET_STATU in(600000450,720130033)");
		sql.append(" AND RECEIVE_REGION_ID =");
		sql.append(areaId);
		if(StringUtils.isNotBlank(serviceType)){
			sql.append(" AND SERVICE_TYPE = ").append(serviceType);
		}
		sql.append(" GROUP BY RETURN_ORG_ID,RETURN_ORG_NAME, RECEIVE_REGION_ID having ");
		for (String s : split) {
			if (!first) {
				sql.append(" OR ");
			}
			sql.append("RETURN_ORG_ID = '");
			sql.append(s);
			sql.append("'");
			first = false;
		}
		sql.append(")X");
		gdf =dbgridDataPub.getResult(sql.toString(), begion, "",DbgridStatic.GRID_FUNID_SURPLUS_SHEET);
		return gdf;
	}

	public GridDataInfo getSentWorkSatisfyCountMessages(String areaId ,String departmentId){
		GridDataInfo gdf = null;
		String sql = "select COUNTNUM,"
			+"RETURN_ORG_ID AS RECEIVE_ORG_ID,"
			+"RETURN_ORG_NAME AS RECEIVE_ORG_NAME,"
			+"RECEIVE_REGION_ID RECEIVE_REGION_ID,"
			+"'600000450' AS LOCK_FLAG"
			+" from (SELECT COUNT(RECEIVE_ORG_ID) AS countNum,"
			+" RETURN_ORG_ID,"
			+" RETURN_ORG_NAME,"
			+" region_id RECEIVE_REGION_ID"
			+" FROM cc_unsatisfy_sheet CC"
			+" where CC.sheet_status = 3"
			+" AND region_id ="+areaId
			+" GROUP BY RETURN_ORG_ID,RETURN_ORG_NAME, region_id"
			+" having RETURN_ORG_ID ='" +departmentId+"')X";
		gdf =dbgridDataPub.getResult(sql, 0, "",DbgridStatic.GRID_FUNID_SURPLUS_SHEET);
		return gdf;
	}
	
	/**
	 * 获取我的任务统计数据根据员工
	 * @param  areaId   地区ID
	 * @param  departmentId  部门ID
	 * @return 获取我的任务统计数据根据员工
	 */
	public GridDataInfo getMyWorkSingleCountMessagesByStaff(int begion, String areaId, String departmentId,String serviceType ){
		GridDataInfo gdf = null;
		String[] split = departmentId.split(",");
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT COUNT(DEAL_ORG_ID) AS COUNTNUM,");
		sb.append("  DEAL_ORG_ID,");
		sb.append("  DEAL_STAFF_NAME,");
		sb.append("  DEAL_STAFF,");
		sb.append("  RECEIVE_REGION_ID");
		sb.append(" FROM CC_WORK_SHEET cc");
		sb.append(" where cc.lock_flag in (1,3)");
		sb.append(" AND cc.RECEIVE_REGION_ID = "+ areaId);
		if(StringUtils.isNotBlank(serviceType)){
			sb.append(" AND cc.SERVICE_TYPE = "+ serviceType);
		}
		if (split.length > 0) {
			sb.append(" AND (");
			for (int i = 0; i < split.length; i++) {
				if (i > 0) {
					sb.append(" OR ");
				}
				sb.append("cc.deal_org_id = '").append(split[i]).append("'");
			}
			sb.append(")");
		}
		sb.append(" GROUP BY DEAL_STAFF_NAME,DEAL_STAFF,DEAL_ORG_ID,RECEIVE_REGION_Id ");
		gdf = dbgridDataPub.getResult(sb.toString(), begion, "", "");
		return gdf;
	}

	public GridDataInfo getMyWorkSatisfyCountMessagesByStaff(int begion, String areaId, String departmentId){
		GridDataInfo gdf = null;
		String sql = " SELECT COUNT(DEAL_ORG_ID) AS COUNTNUM,"
		     +"  DEAL_ORG_ID,"
		     +"  DEAL_STAFF_NAME,"
		     +"  DEAL_STAFF,"
		     +"  region_id RECEIVE_REGION_ID"
		     +" FROM cc_unsatisfy_sheet cc"
		     +" where cc.lock_flag = 1"
		     +" AND cc.region_id = "+ areaId
		     +" and cc.deal_org_id = '"+departmentId+"'"
		     +" GROUP BY DEAL_STAFF_NAME,DEAL_STAFF,DEAL_ORG_ID,region_id ";
		    gdf = dbgridDataPub.getResult(sql, begion, "", "");
		return gdf;
	}
	
	/**
	 * 获取派发任务统计数据
	 * @param  areaId   地区ID
	 * @param  departmentId  部门ID
	 * @return 派发任务统计数据
	 */
	public GridDataInfo getSentWorkSingleCountMessagesByStaff(int begion, String areaId, String departmentId,String serviceType ){
		GridDataInfo gdf = null;
		String[] split = departmentId.split(",");
		StringBuffer sb = new StringBuffer();
		sb.append(" SELECT COUNT(RETURN_ORG_ID) AS COUNTNUM,");
		sb.append("  RETURN_ORG_ID AS DEAL_ORG_ID,");
		sb.append("  RETURN_STAFF_NAME AS DEAL_STAFF_NAME,");
		sb.append("  RETURN_STAFF AS DEAL_STAFF,");
		sb.append("  RECEIVE_REGION_ID");
		sb.append(" FROM CC_WORK_SHEET cc");
		sb.append(" where cc.SHEET_STATU in(600000450,720130033)");
		sb.append(" AND cc.RECEIVE_REGION_ID = "+ areaId);
		if(StringUtils.isNotBlank(serviceType)){
			sb.append(" AND cc.SERVICE_TYPE = "+ serviceType);
		}
		if (split.length > 0) {
			sb.append(" AND (");
			for (int i = 0; i < split.length; i++) {
				if (i > 0) {
					sb.append(" OR ");
				}
				sb.append("cc.RETURN_ORG_ID = '").append(split[i]).append("'");
			}
			sb.append(")");
		}
		sb.append(" GROUP BY RETURN_STAFF,RETURN_STAFF_NAME,RETURN_ORG_ID,RECEIVE_REGION_Id");
		    gdf = dbgridDataPub.getResult(sb.toString(), begion, "", "");
	    return gdf;
	}

	public GridDataInfo getSentWorkSatisfyCountMessagesByStaff(int begion, String areaId, String departmentId){
		GridDataInfo gdf = null;
		String sql = " SELECT COUNT(RETURN_ORG_ID) AS COUNTNUM,"
	     +"  RETURN_ORG_ID AS DEAL_ORG_ID,"
	     +"  RETURN_STAFF_NAME AS DEAL_STAFF_NAME,"
	     +"  RETURN_STAFF AS DEAL_STAFF,"
	     +"  region_id RECEIVE_REGION_ID "
	     +" FROM cc_unsatisfy_sheet cc"
	     +" where cc.sheet_status = 3"
	     +" AND cc.region_id = "+ areaId
	     +" and cc.RETURN_ORG_ID = '"+departmentId+"'"
	     +" GROUP BY RETURN_STAFF,RETURN_STAFF_NAME,RETURN_ORG_ID,region_id";
		    gdf = dbgridDataPub.getResult(sql, begion, "", "");
	    return gdf;

	}
	
	/**
	 * 根据参数查询工单明细
	 * @param areaId 地区ID
	 * @param departmentId 部门ID
	 * @param lockFlag 工单状态标识
	 * @param statffId 员工ID
	 * @return  工单明细
	 */
	public GridDataInfo getSentWorkSingleMessages(String areaId, String departmentId, int lockFlag, String staffId, String serviceType,String begion){
		GridDataInfo gdf = null;
		if(lockFlag == 13 && !staffId.equals("-1")) {
			lockFlag  = 131;
		}
		if(lockFlag == 600000450 && !staffId.equals("-1")) {
			lockFlag  = 600000451;
		}
		
		String sql  = buildSqlByLockFlag(areaId,departmentId ,lockFlag ,staffId,serviceType);
		gdf = dbgridDataPub.getResult(sql, Integer.parseInt(begion), "",DbgridStatic.GRID_FUNID_STAFF_SHEET);
	    return gdf;
		
	}

	public GridDataInfo getSentWorkSatisfyMessages(String areaId, String departmentId, int lockFlag, String staffId, String begion){
		GridDataInfo gdf = null;
		if(lockFlag == 13 && !staffId.equals("-1")) {
			lockFlag  = 131;
		}
		if(lockFlag == 600000450 && !staffId.equals("-1")) {
			lockFlag  = 600000451;
		}

		String sql  = selectSatisfyByLockFlag(areaId,departmentId ,lockFlag ,staffId);
		gdf = dbgridDataPub.getResult(sql, Integer.parseInt(begion), "",DbgridStatic.GRID_FUNID_STAFF_SHEET);
	    return gdf;

	}

	/**
	 * 根据参数查询所有工单明细
	 * @param areaId 地区ID
	 * @param departmentId 部门ID
	 * @param lockFlag 工单状态标识
	 * @param statffId 员工ID
	 * @return  工单明细
	 */
	public GridDataInfo getSentAllWorkSingleMessages(String areaId, String departmentId, int lockFlag, String staffId){
		GridDataInfo gdf = null;
		if(lockFlag == 13 && !staffId.equals("-1")) {
			lockFlag  = 131;
		}
		if(lockFlag == 600000450 && !staffId.equals("-1")) {
			lockFlag  = 600000451;
		}
		String sql  = selectSqlByLockFlag(areaId,departmentId ,lockFlag ,staffId);
		gdf = dbgridDataPub.getAllResult(sql,"",DbgridStatic.GRID_FUNID_STAFF_SHEET);
	    return gdf;
	}

	public GridDataInfo getSentAllWorkSatisfyMessages(String areaId, String departmentId, int lockFlag, String staffId){
		GridDataInfo gdf = null;
		if(lockFlag == 13 && !staffId.equals("-1")) {
			lockFlag  = 131;
		}
		if(lockFlag == 600000450 && !staffId.equals("-1")) {
			lockFlag  = 600000451;
		}
		String sql  = selectSatisfyByLockFlag(areaId,departmentId ,lockFlag ,staffId);
		gdf = dbgridDataPub.getAllResult(sql,"",DbgridStatic.GRID_FUNID_STAFF_SHEET);
	    return gdf;
	}

	/**
	 * 根据参数来组装SQL语句
	 * @param areaId 地区ID
	 * @param departmentId 部门ID
	 * @param lockFlag 工单状态标识
	 * @param statffId 员工ID
	 * @return  组装SQL语句
	 */
	private String buildSqlByLockFlag(String areaId, String departmentId, int lockFlag, String staffId,String serviceType) {
		String sql =
				"SELECT CC.WORK_SHEET_ID,CC.SERVICE_ORDER_ID,CC.SOURCE_SHEET_ID,DATE_FORMAT(COA.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(CC.LOCK_DATE,"
						+ "'%Y-%m-%d %H:%i:%s')LOCK_DATE,CC.HASTENT_NUM,CC.REPORT_NUM,COA.PROD_NUM,CC.SHEET_STATU_DESC,CC.DEAL_LIMIT_TIME,DEAL_LIMIT_TIME*3600-TIMESTAMPDIFF("
						+ "SECOND,CC.CREAT_DATE,NOW())OUTIME,CONCAT(TRUNCATE((DEAL_LIMIT_TIME*3600-TIMESTAMPDIFF(SECOND,CC.CREAT_DATE,NOW()))/3600,1),'小时')FINTIME,"
						+ "CC.TACHE_DESC FROM CC_WORK_SHEET CC LEFT JOIN CC_SERVICE_ORDER_ASK COA ON COA.SERVICE_ORDER_ID=CC.SERVICE_ORDER_ID WHERE CC.RECEIVE_REGION_ID=";
		StringBuffer sb = new StringBuffer();
		sb.append(sql);
		sb.append(areaId);
		sb.append(" and CC.SHEET_STATU not in(700000049,720130037)");
		if(StringUtils.isNotBlank(serviceType)){
			sb.append(" and CC.SERVICE_TYPE = '").append(serviceType).append("'");
		}

		switch(lockFlag) {
			case 0:
				sb.append("  and lock_flag = 0 and SHEET_STATU not in(600000450,720130033)");
				sb.append("  and RECEIVE_ORG_ID = '");
				sb.append(departmentId+"'");
				break;
			case 13:
				sb.append("  and (lock_flag = 1 or lock_flag = 3)");
				sb.append("  and DEAL_ORG_ID = '");
				sb.append(departmentId+"'");
				break;
			case 131:
				sb.append("  and (lock_flag = 1 or lock_flag = 3)");
				sb.append("  and DEAL_ORG_ID = '");
				sb.append(departmentId);
				sb.append("'  and  DEAL_STAFF =");
				sb.append(staffId);
				break;
			case 600000450:
				sb.append("  and SHEET_STATU in(600000450,720130033) ");
				sb.append("  and RETURN_ORG_ID = '");
				sb.append(departmentId+"'");
				break;
			case 600000451:
				sb.append("  and SHEET_STATU in(600000450,720130033) ");
				sb.append("  and RETURN_ORG_ID = '");
				sb.append(departmentId);
				sb.append("' and RETURN_STAFF=");
				sb.append(staffId);
				break;
			default:
				break;
		}
		sql = sb.toString();
		return sql;
	}
	
	/**
	 * 根据参数来组装SQL语句
	 * @param areaId 地区ID
	 * @param departmentId 部门ID
	 * @param lockFlag 工单状态标识
	 * @param statffId 员工ID
	 * @return  组装SQL语句
	 */
	private String selectSqlByLockFlag(String areaId, String departmentId, int lockFlag, String staffId) {
		String sql = 
"SELECT CC.WORK_SHEET_ID,CC.SERVICE_ORDER_ID,CC.SOURCE_SHEET_ID,DATE_FORMAT(COA.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(CC.LOCK_DATE,"
+ "'%Y-%m-%d %H:%i:%s')LOCK_DATE,CC.HASTENT_NUM,CC.REPORT_NUM,COA.PROD_NUM,CC.SHEET_STATU_DESC,CC.DEAL_LIMIT_TIME,DEAL_LIMIT_TIME*3600-TIMESTAMPDIFF("
+ "SECOND,CC.CREAT_DATE,NOW())OUTIME,CONCAT(TRUNCATE((DEAL_LIMIT_TIME*3600-TIMESTAMPDIFF(SECOND,CC.CREAT_DATE,NOW()))/3600,1),'小时')FINTIME,"
+ "CC.TACHE_DESC FROM CC_WORK_SHEET CC LEFT JOIN CC_SERVICE_ORDER_ASK COA ON COA.SERVICE_ORDER_ID=CC.SERVICE_ORDER_ID WHERE CC.RECEIVE_REGION_ID=";
		StringBuffer sb = new StringBuffer();
		sb.append(sql);
		sb.append(areaId);
		sb.append(" and CC.SHEET_STATU not in(700000049,720130037)");
		   
		switch(lockFlag) {
			case 0: 
			    sb.append("  and lock_flag = 0 and SHEET_STATU not in(600000450,720130033)");
			    sb.append("  and RECEIVE_ORG_ID = '");
			    sb.append(departmentId+"'");
			    break;
		    case 13: 
			    sb.append("  and (lock_flag = 1 or lock_flag = 3)");
			    sb.append("  and DEAL_ORG_ID = '");
			    sb.append(departmentId+"'");
			    break;
		    case 131: 
			    sb.append("  and (lock_flag = 1 or lock_flag = 3)");
			    sb.append("  and DEAL_ORG_ID = '");
			    sb.append(departmentId);
			    sb.append("'  and  DEAL_STAFF =");
			    sb.append(staffId);
			    break;
		    case 600000450: 
			    sb.append("  and SHEET_STATU in(600000450,720130033) ");
			    sb.append("  and RETURN_ORG_ID = '");
			    sb.append(departmentId+"'");
			    break;
		    case 600000451: 
			    sb.append("  and SHEET_STATU in(600000450,720130033) ");
			    sb.append("  and RETURN_ORG_ID = '");
			    sb.append(departmentId);
			    sb.append("' and RETURN_STAFF=");
			    sb.append(staffId);
			    break;
		    default:
		    	break;
		}
		sql = sb.toString();
		return sql;
	}

	private String selectSatisfyByLockFlag(String areaId, String departmentId, int lockFlag, String staffId) {
		String sql = "SELECT CC.WORK_SHEET_ID, CC.work_order_id SERVICE_ORDER_ID, CC.SOURCE_SHEET_ID, DATE_FORMAT( c.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s' ) ACCEPT_DATE, " +
				"DATE_FORMAT( CC.distill_date, '%Y-%m-%d %H:%i:%s' ) LOCK_DATE, COA.PROD_NUM, CASE sheet_status  WHEN 0 THEN '待处理'  WHEN 1 THEN '处理中'  WHEN 2 THEN " +
				" '已处理' WHEN 3 THEN '已派发' WHEN 9 THEN '挂起' ELSE ' ' END AS SHEET_STATU_DESC, CC.sheet_limit DEAL_LIMIT_TIME, " +
				" cast( coa.order_limit * 3600 AS signed ) - cast( TIMESTAMPDIFF ( SECOND, CC.create_date, NOW()) AS signed ) OUTIME, " +
				" CONCAT( TRUNCATE (( cast( coa.order_limit * 3600 AS signed ) - cast( TIMESTAMPDIFF ( SECOND, CC.create_date, NOW()) AS signed ))/ 3600, 1 ), '小时' ) FINTIME, CC.TACHE_DESC " +
				"FROM cc_unsatisfy_sheet CC LEFT JOIN cc_unsatisfy_order COA ON COA.work_order_id = CC.work_order_id " +
				" LEFT JOIN cc_unsatisfy_detail c ON c.work_order_id = cc.work_order_id WHERE CC.region_id = ";
		StringBuffer sb = new StringBuffer();
		sb.append(sql);
		sb.append(areaId);
		switch(lockFlag) {
			case 0:
			    sb.append("  and lock_flag = 0 and sheet_status != 3");
			    sb.append("  and RECEIVE_ORG_ID = '");
			    sb.append(departmentId+"'");
			    break;
		    case 13:
			    sb.append("  and (lock_flag = 1 )");
			    sb.append("  and DEAL_ORG_ID = '");
			    sb.append(departmentId+"'");
			    break;
		    case 131:
			    sb.append("  and (lock_flag = 1)");
			    sb.append("  and DEAL_ORG_ID = '");
			    sb.append(departmentId);
			    sb.append("'  and  DEAL_STAFF =");
			    sb.append(staffId);
			    break;
		    case 600000450:
			    sb.append("  and sheet_status = 3 ");
			    sb.append("  and RETURN_ORG_ID = '");
			    sb.append(departmentId+"'");
			    break;
		    case 600000451:
			    sb.append("  and sheet_status = 3");
			    sb.append("  and RETURN_ORG_ID = '");
			    sb.append(departmentId);
			    sb.append("' and RETURN_STAFF=");
			    sb.append(staffId);
			    break;
		    default:
		    	break;
		}
		sql = sb.toString();
		return sql;
	}
	
	/**
	 * 剩余时限的查询2010-06-16 剩余时限匹配模式修改成小于 万荣伟
	 * @param begin 分页开始
	 * @param FUNID 查询ID
	 * @param params 参数
	 * @return 剩余时限的查询
	 */
	public GridDataInfo queryWorkSheet(@RequestBody String sheetObj){
		JSONObject ins = JSONObject.fromObject(sheetObj);
		String page = ins.optString("begion");
		int begion = Integer.parseInt(page);
		JSONObject str = ins.optJSONObject("where");
		GridDataInfo gridDataInfo = new  GridDataInfo();
		StringBuffer sb = new StringBuffer();
		String sql = "select cc.WORK_SHEET_ID,"
	       +" cc.SERVICE_ORDER_ID,"
	       +" cc.RECEIVE_ORG_NAME,"
	       +" cc.DEAL_ORG_NAME,"
	       +" cc.DEAL_ORG_ID,"
	       +" cc.DEAL_STAFF_NAME,"
	       +" cc.HASTENT_NUM,"
	       +" cc.TACHE_ID,"
	       +" cc.TACHE_DESC,"
	       +" cc.SHEET_STATU,"
	       +" cc.SHEET_STATU_DESC,"
	       +" cso.ACCEPT_STAFF_NAME,"
	       +" cso.ACCEPT_ORG_NAME,"
	       +" DATE_FORMAT( cso.accept_date,'%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE ,"
	       +" DATE_FORMAT( cc.LOCK_DATE,'%Y-%m-%d %H:%i:%s') AS LOCK_DATE ,"
	       +" cc.DEAL_LIMIT_TIME,"
	       +" cc.DEAL_LIMIT_TIME*60 - TIMESTAMPDIFF(SECOND, cc.LOCK_DATE, now())/60 + cc.HANGUP_TIME_COUNT  SURPUSTIME,"
	       +" cso.ORDER_LIMIT_TIME*60 - TIMESTAMPDIFF(SECOND, cso.ACCEPT_DATE, now())/60 TACHELIMITTIME"
	       +" from CC_WORK_SHEET cc"
	       +" left join CC_SERVICE_ORDER_ASK  cso on cso.service_order_id =  cc.service_order_id"
	       +" where cc.SHEET_STATU not in(700000046,700001177,600000450,700000047,720130035,720130033,720130036) ";
		     sb.append(sql);
		     //时间过滤
		     if(str.optString("startTime")!=null && !"".equals(str.optString("startTime"))) {
		    	 sb.append(" and cc.CREAT_DATE between ");
		    	 sb.append(" STR_TO_DATE('").append(str.optString("startTime")).append("','%Y-%m-%d %H:%i:%s')");
		    	 sb.append(" and STR_TO_DATE('").append(str.optString("endTime")).append("','%Y-%m-%d %H:%i:%s')");
		     }
			 //时限
			 if("1".equals(str.optString("timeLimitType"))){
				 sb.append(" and cso.ORDER_LIMIT_TIME*60 - TIMESTAMPDIFF(SECOND, cso.ACCEPT_DATE, now())/60 <").append(str.optString("timeLimit_minute"));
			 }
			 if("2".equals(str.optString("timeLimitType"))) {
				 sb.append(" and cc.DEAL_LIMIT_TIME*60 - TIMESTAMPDIFF(SECOND, cc.LOCK_DATE, now())/60 + cc.HANGUP_TIME_COUNT <").append(str.optString("timeLimit_minute"));
			 }
			 if(!"".equals(str.optString("deal_orgId"))) {
				 sb.append(" and cc.DEAL_ORG_ID ='").append(str.optString("deal_orgId")).append("'");
			}
			 if(!"".equals(str.optString("deal_staffId"))) {
				 sb.append(" and cc.deal_staff ='").append(str.optString("deal_staffId")).append("'");
			}
			 if(!"".equals(str.optString("acceptOrgId"))) {
				 sb.append(" and cso.ACCEPT_ORG_ID ='").append(str.optString("acceptOrgId")+"'");
			}
			 if(!"".equals(str.optString("current_orgId"))) {
				 sb.append(" and cc.RECEIVE_ORG_ID ='").append(str.optString("current_orgId")).append("'");
			}
			 if(!"".equals(str.optString("areaId"))) {
				 sb.append(" and cc.REGION_ID = ").append(str.optString("areaId"));
			}
		     gridDataInfo = dbgridDataPub.getResult(sb.toString(),begion, "","");
		return gridDataInfo;
	}
	
	/**
	 *  发送短信
	 * @param phoneType 手机类型
	 * @param phoneValue 电话号码
	 * @param message 电短信内容
	 * @param orderName  订单号
	 * @param workName  工单号 
	 * @return
	 */
	public int sendMassage(int phoneType,String phoneValue,String message,String orderName,String workName,String sysx) {
		NoteSeand noteBean = new NoteSeand();
		//获取系统的唯一编码
		String sheetGuid = this.pubFunc.crtGuid();
	    //1表示手机 0 表示小灵通
		int client = phoneType != 1 ? 0 :1;
		noteBean.setSheetGuid(sheetGuid);
		noteBean.setDestteRmid(phoneValue);
		noteBean.setClientType(client);
		if(sysx !=null && sysx.trim().equals("催单")) {
			noteBean.setSendContent("催单短信:服务单号为:"+orderName+",催单信息："+message+"，请及时处理！");
		} else {
		    noteBean.setSendContent("剩余时限短信:服务单号为:"+orderName+",环节剩余时间:"+sysx+",备注信息："+message+"，请及时处理！");
		}
		noteBean.setBusiId(workName);
		return this.noteSen.saveNoteContent(noteBean);
	}
	
	public String findStatffPhoneByOrgId(String orgId) {
		String sql = "select ts.relaphone,ts.staffname from tsm_staff ts  where ts.relaphone is not null and ts.org_id =?";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, orgId);
		String relaphone="";
		String staffname="";
		JSONArray arr=new JSONArray();
		JSONObject ob=new JSONObject();
		for(int i=0;i<list.size();i++) {
			relaphone=list.get(i).get("RELAPHONE").toString();
			staffname=list.get(i).get("STAFFNAME").toString();
			ob.put("RELAPHONE", relaphone);
			ob.put("STAFFNAME", staffname);
			arr.add(ob);
		}
      return arr.toString();
	}
	
	@Override
	public GridDataInfo monitorExport(int begion, String strWhere) {
		 GridDataInfo bean = new GridDataInfo();
		if(strWhere.contains("RECEIVE_STAFF")){
			bean=getMonitorExportStaff(begion, strWhere);
		}else if (strWhere.contains("RECEIVE_ORG_ID")) {
			bean=getMonitorExport(begion, strWhere);
		}
		
		return bean;
	}
	@Override
	public GridDataInfo satisfyExport(int begion, String strWhere) {
		 GridDataInfo bean = new GridDataInfo();
		if(strWhere.contains("RECEIVE_STAFF")){
			bean=getSatisfyExportStaff(begion, strWhere);
		}else if (strWhere.contains("RECEIVE_ORG_ID")) {
			bean=getSatisfyExport(begion, strWhere);
		}

		return bean;
	}
	
}
