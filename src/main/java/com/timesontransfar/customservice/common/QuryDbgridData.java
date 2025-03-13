/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;

import net.sf.json.JSONObject;

/**
 * @author 万荣伟
 *
 */
@Component("QuryDbgridData")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class QuryDbgridData {
	
	/**
	 * Logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(QuryDbgridData.class);
	@Autowired
	private ISystemAuthorization systemAuthorization;
	@Autowired
	private IworkSheetBusi workSheetBusi;
	@Autowired
    private IdbgridDataPub dbgridDataPub;
	@Autowired
	private PubFunc pubFunc;
	
	/**
	 * 取得 投诉处理分派  列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getWaitDealSheet(int begion,int pageSize,String strWhere) {	
		String strSql = 
"SELECT (SELECT IS_RUYI FROM CC_RUYI_LABEL WHERE SERVICE_ORDER_ID=L.SERVICE_ORDER_ID)CUST_TYPE,DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,"
+ "DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),W.HANGUP_START_TIME,W.RESPOND_DATE),'%Y-%m-%d %H:%i:%s')END_DATE,A.SERVICE_TYPE,DATE_FORMAT("
+ "NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.PROD_NUM,DATE_FORMAT("
+ "A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,"
+ "W.SHEET_STATU_DESC,(SELECT COUNT(1)FROM CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID)HASTENT_NUM,A.ACCEPT_COUNT,W.RECEIVE_STAFF_NAME,A.SERVICE_TYPE_DESC,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.PRE_ALARM_VALUE,"
+ "W.SHEET_TYPE_DESC,A.URGENCY_GRADE,A.URGENCY_GRADE_DESC,D.APPEAL_PROD_NAME,C.CUST_SERV_GRADE_NAME,A.REGION_NAME,W.SHEET_TYPE,A.COME_CATEGORY,A.ORDER_LIMIT_TIME,A.HANGUP_TIME_COUNT ORDER_TIME_COUNT,"
+ "W.TACHE_ID,A.ACCEPT_COME_FROM,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')LOCK_DATE,W.SHEET_STATU,DATE_FORMAT(W.HANGUP_START_TIME,"
+ "'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,W.RETURN_STAFF_NAME,W.REGION_ID,W.MONTH_FLAG,L.FORCE_PRE_FLAG,L.UPGRADE_INCLINE,D.FIVE_ORDER_DESC,D.BEST_ORDER_DESC FROM "
+ "CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C,CC_SERVICE_LABEL L WHERE W.REGION_ID=A.REGION_ID AND "
+ "A.SERVICE_DATE=3 AND W.LOCK_FLAG=0 AND W.SHEET_STATU NOT IN(600000450,720130033)AND W.SHEET_TYPE!=700001000 AND W.TACHE_ID!=3000025 AND "
+ "W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID AND A.CUST_GUID=C.CUST_GUID AND A.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
+ "AND ( W.SERVICE_TYPE <> 720200002 OR D.APPEAL_CHILD <> 2020501 OR W.TACHE_ID <> 700000085 OR W.LOCK_FLAG <> 0 OR W.SHEET_STATU <> 700000046 )";

		if(!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if(andIndex == -1) {
				log.warn("WHERE 条件中没有AND,请检查WHERE条件"+strWhere);
			}
			strSql=strSql+strWhere;
		}
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");		 
		
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018300");
		GridDataInfo bean = this.dbgridDataPub.getResultBySize(strSql, begion, pageSize, " ORDER BY CREAT_DATE ASC", "投诉处理分派");
		List<Map<String, Object>> list = bean.getList();
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
		bean.setList(list);
		return bean;
	}
	
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	private int getIntByKey(Map map, String key) {
		return map.get(key) == null ? 0 : Integer.parseInt(map.get(key).toString());
	}

	/**
	 * 得到投诉建议强制释放列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getForceRelease(int begion,String strWhere) {
		String strSql = 
"SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,W.TACHE_DESC,A.ORDER_STATU,A.ORDER_STATU_DESC,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')"
+ "ACCEPT_DATE,DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE,A.ACCEPT_CHANNEL_ID,A.ACCEPT_COME_FROM_DESC,W.SHEET_STATU_DESC,W.HASTENT_NUM,"
+ "A.ACCEPT_COUNT,W.HANGUP_TIME_COUNT,W.DEAL_LIMIT_TIME,W.PRE_ALARM_VALUE,W.RECEIVE_STAFF_NAME,W.SHEET_TYPE_DESC,A.SERVICE_TYPE_DESC,A.URGENCY_GRADE,"
+ "A.URGENCY_GRADE_DESC,A.ACCEPT_COME_FROM,DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s')LOCK_DATE,W.SHEET_STATU,DATE_FORMAT(W.HANGUP_START_TIME,"
+ "'%Y-%m-%d %H:%i:%s')HANGUP_START_TIME,W.DEAL_STAFF_NAME,D.APPEAL_PROD_NAME,C.CUST_SERV_GRADE_NAME,A.REGION_NAME,W.RETURN_STAFF_NAME,W.REGION_ID,"
+ "W.MONTH_FLAG FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK D,CC_ORDER_CUST_INFO C WHERE W.REGION_ID=A.REGION_ID AND "
+ "(A.SERVICE_DATE=3 OR A.SERVICE_TYPE=700001171) AND W.LOCK_FLAG=1 AND W.SHEET_STATU!=700000046 AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=D.SERVICE_ORDER_ID"
+ " AND A.CUST_GUID=C.CUST_GUID";
		if (!"".equals(strWhere)) {
			int andIndex = strWhere.indexOf("AND");
			if (andIndex == -1) {
				log.warn("WHERE 条件中没有AND,请检查WHERE条件" + strWhere);
			}
			strSql = strSql + strWhere;
		}
		// 权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
		tableMap.put("CC_ORDER_CUST_INFO", "C");
		strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018305");
		return this.dbgridDataPub.getResult(strSql, begion, " ORDER BY W.CREAT_DATE ASC", "");
	}

	/**
	 * 获取本地网的所有投诉类、预处理类、商机类待派发至员工的工单
	 * @param begion 开始
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getLocalWaitDealSheet(int begion, String strWhere) {
		Map tableMap = new HashMap();
		tableMap.put("CC_WORK_SHEET", "W");
		tableMap.put("CC_SERVICE_ORDER_ASK", "AA");
		String str = this.systemAuthorization.getAuthedSql(tableMap, "SELECT 1 FROM DUAL WHERE 1=1", "900018300");//投诉类
		//截取"SELECT 1 FROM DUAL WHERE 1=1"之后的查询条件
		String s1 = str.substring(28);
		String sql = 
"SELECT A.SERVICE_ORDER_ID,A.WORK_SHEET_ID,A.SERVICE_TYPE,A.SERVICE_TYPE_DESC,A.TACHE_ID,A.TACHE_DESC,DATE_FORMAT(A.CREAT_DATE,'%Y-%m-%d %H:%i:%s')"
+ "LOCK_DATE,A.SHEET_STATU,A.SHEET_STATU_DESC,A.RETURN_STAFF,A.RETURN_STAFF_NAME,A.SHEET_TYPE,A.SHEET_TYPE_DESC FROM("
+ "SELECT W.* FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK AA WHERE W.SERVICE_ORDER_ID=AA.SERVICE_ORDER_ID "
+ "AND W.LOCK_FLAG=0 AND W.SERVICE_TYPE IN(700006312,720200003,700001171,600000074,720130000,720200000,720200002) "
+ "AND W.SHEET_TYPE IN(700000126,700000127,600000075,600000077,600000069,720130011,720130013,720130014,720130016) AND "
+ "W.SHEET_STATU IN(700000044,700000048,720130031) "
+ strWhere + s1 +
" UNION SELECT W.* FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK AA WHERE W.SERVICE_ORDER_ID=AA.SERVICE_ORDER_ID "
+ "AND W.LOCK_FLAG=0 AND W.SERVICE_TYPE IN(700006312,720200003,600000074,720130000,720200000,720200002) "
+ "AND W.SHEET_TYPE IN(700001002,720130015)AND W.SHEET_STATU IN(600000002,720130031) "
+ strWhere + s1 +
" UNION SELECT W.* FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK AA WHERE W.SERVICE_ORDER_ID=AA.SERVICE_ORDER_ID "
+ "AND W.LOCK_FLAG=0 AND W.SERVICE_TYPE IN(700006312,720200003,600000074,720200000,720200002) "
+ "AND W.TACHE_ID=700000088 AND W.SHEET_STATU=600000004 "
+ strWhere + s1 +
")A WHERE 1=1 ";
		return this.dbgridDataPub.getResult(sql, begion, " ORDER BY A.CREAT_DATE", "");
	}

	public GridDataInfo getAutoAcceptList(int begion) {
		String sql = 
"SELECT CC_AUTO_ACCEPT_ORDER.SERVICE_TYPE_DESC SERVICE_TYPE_DESC,CC_AUTO_ACCEPT_ORDER.COME_CATEGORY_NAME COME_CATEGORY_NAME,"
+ "CC_AUTO_ACCEPT_ORDER.ACCEPT_COME_FROM_DESC ACCEPT_COME_FROM_DESC,CC_AUTO_ACCEPT_ORDER.ACCEPT_CHANNEL_DESC ACCEPT_CHANNEL_DESC,"
+ "CC_AUTO_ACCEPT_ORDER.REGION_NAME REGION_NAME,CC_AUTO_ACCEPT_ORDER.PROD_NUM PROD_NUM,CC_AUTO_ACCEPT_ORDER.PROD_TYPE_DESC PROD_TYPE_DESC,"
+ "CC_AUTO_ACCEPT_ORDER.CUST_NAME CUST_NAME,CC_AUTO_ACCEPT_ORDER.RELA_MAN RELA_MAN,CC_AUTO_ACCEPT_ORDER.RELA_INFO RELA_INFO,"
+ "CC_AUTO_ACCEPT_ORDER.SS_FLOW SS_FLOW,CC_AUTO_ACCEPT_ORDER.ACCEPT_CONTENT ACCEPT_CONTENT,DATE_FORMAT(CC_AUTO_ACCEPT_ORDER.CREATE_DATE,"
+ "'%Y-%m-%d %H:%i:%s')CREATE_DATE,CC_AUTO_ACCEPT_ORDER.STATU_DESC STATU_DESC,CC_AUTO_ACCEPT_ORDER.STATU STATU FROM CC_AUTO_ACCEPT_ORDER WHERE "
+ "STATU<>4";
		return this.dbgridDataPub.getResult(sql, begion, " ORDER BY CREATE_DATE", "");
	}
	
	//dealType 1分派 2释放
	public String getForceWhere(String ins){
		JSONObject j=JSONObject.fromObject(ins);
		String where="";
		JSONObject str=j.getJSONObject("str");
		String servOrderId=str.optString("servOrderId");
		String sheetId=str.optString("sheetId");
		String proNum=str.optString("proNum");
		String realPhone=str.optString("realPhone");
		String returnStaff=str.optString("returnStaff");
		String urgencyGrade=str.optString("urgencyGrade");
		String areaId=str.optString("areaId");
		String serviceType=str.optString("serviceType");
		String orderStatu=str.optString("orderStatu");
		String urgencyGradeFlag=str.optString("urgencyGradeFlag");
		String custServGrade=str.optString("custServGrade");
		String appealProdId=str.optString("appealProdId");
		String appealReasonId=str.optString("appealReasonId");
		String acceptCome=str.optString("acceptCome");
		String worksheetType=str.optString("worksheetType");
		String startTime=str.optString("startTime");
		String endTime=str.optString("endTime");
		String askChannelId=str.optString("askChannelId");
		String trChannelDetail=str.optString("trChannelDetail");
		String dealType=str.optString("dealType");
		String upTendencyFlag=str.optString("upTendencyFlag");
		String hastentNum=str.optString("hastentNum");
		String areaName=str.optString("areaName");
		String subStationName=str.optString("subStationName");
		String secFlag=str.optString("secFlag");
		String ruyiFlag = str.optString("ruyiFlag");
		String where1 = this.setOrderAskParam1(where, areaName, subStationName, acceptCome, askChannelId, trChannelDetail, proNum);
		String where2 = this.setOrderAskParam2(where1, realPhone, urgencyGrade, serviceType, areaId, servOrderId);
		String where3 = this.setOrderAskParam3(where2, startTime, endTime);
		String where4 = this.setParam1(where3, custServGrade, appealProdId, appealReasonId, upTendencyFlag, secFlag);
		String where5 = this.setParam2(where4, ruyiFlag, hastentNum, worksheetType, orderStatu, returnStaff, dealType);
		String where6 = this.setParam3(where5, sheetId, urgencyGradeFlag);
		where = this.buildForceWhere(str, where6, dealType);
		return where;
	}

	private String setOrderAskParam1(String where, String areaName, String subStationName, String acceptCome, String askChannelId, String trChannelDetail, String proNum){
		if(!"".equals(areaName)) {
			where+=" AND A.AREA_NAME like '%"+areaName+"%'";
		}
		if(!"".equals(subStationName)) {
			where+=" AND A.SUB_STATION_NAME like '%"+subStationName+"%'";
		}
		if(!"".equals(acceptCome)) {
			where+=" AND A.ACCEPT_COME_FROM="+Integer.parseInt(acceptCome);
		}
		if(!"".equals(askChannelId)) {
			where+=" AND A.ACCEPT_CHANNEL_ID ="+Integer.parseInt(askChannelId);
		}
		if(!"".equals(trChannelDetail)) {
			where+=" AND A.CHANNEL_DETAIL_ID ="+Integer.parseInt(trChannelDetail);
		}
		if(!"".equals(proNum)) {
			where+=" AND A.PROD_NUM='"+proNum+"'";
		}
		return where;
	}

	private String setOrderAskParam2(String where,String realPhone, String urgencyGrade, String serviceType, String areaId, String servOrderId){
		if(!"".equals(realPhone)) {
			where+=" AND A.RELA_INFO='"+realPhone+"'";
		}
		if(!"".equals(urgencyGrade)) {
			where+=" AND A.URGENCY_GRADE="+Integer.parseInt(urgencyGrade);
		}
		if(!"".equals(serviceType)) {
			where+=" AND A.SERVICE_TYPE="+Integer.parseInt(serviceType);
		}
		if(!"".equals(areaId)) {
			where+=" AND A.REGION_ID="+Integer.parseInt(areaId);
		}
		if(!"".equals(servOrderId)) {
			where+=" AND A.SERVICE_ORDER_ID='"+servOrderId+"'";
		}
		return where;
	}

	private String setOrderAskParam3(String where,String startTime,String endTime){
		if(!"".equals(startTime)) {
			where += " AND A.ACCEPT_DATE>STR_TO_DATE('"+startTime+"','%Y-%m-%d %H:%i:%s')";
			where += " AND A.ACCEPT_DATE<STR_TO_DATE('"+endTime+"','%Y-%m-%d %H:%i:%s')";
		}
		return where;
	}

	private String setParam1(String where ,String custServGrade,String appealProdId,String appealReasonId,String upTendencyFlag,String secFlag){
		if(!"".equals(custServGrade)) {
			where+=" AND C.CUST_SERV_GRADE ="+Integer.parseInt(custServGrade);
		}
		if(!"".equals(appealProdId)) {
			where+=" AND D.APPEAL_PROD_ID="+Integer.parseInt(appealProdId);
		}
		if(!"".equals(appealReasonId)) {
			where+=" AND D.APPEAL_REASON_ID="+Integer.parseInt(appealReasonId);
		}
		if(!"".equals(upTendencyFlag)) {
			where+=" AND L.UP_TENDENCY_FLAG = "+Integer.parseInt(upTendencyFlag);
		}
		if(!"".equals(secFlag)) {
			if(secFlag.equals("否")){
				where+=" AND L.SEC_FLAG IS NULL ";
			}else {
				where+=" AND L.SEC_FLAG >0 ";
			}
		}
		return where;
	}

	private String setParam2(String where,String ruyiFlag,String hastentNum,String worksheetType,String orderStatu,String returnStaff,String dealType){
		if(StringUtils.isNotBlank(ruyiFlag)) {
			if("1".equals(ruyiFlag)){
				where+=" AND A.SERVICE_ORDER_ID IN(SELECT SERVICE_ORDER_ID FROM cc_ruyi_label)";
			} else if ("2".equals(ruyiFlag)){
				where+=" AND A.SERVICE_ORDER_ID NOT IN(SELECT SERVICE_ORDER_ID FROM cc_ruyi_label)";
			}
		}
		if(!"".equals(hastentNum)) {
			where+=" AND (SELECT COUNT(1)FROM CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID) = "+Integer.parseInt(hastentNum);
		}

		if(!"".equals(worksheetType)) {
			where+=" AND W.SHEET_TYPE="+Integer.parseInt(worksheetType);
		}
		if(!"".equals(orderStatu)) {
			where+=" AND W.SHEET_STATU="+Integer.parseInt(orderStatu);
		}
		if(!"".equals(returnStaff)) {
			int staffId=workSheetBusi.getStaffId(returnStaff);
			if(staffId!=0) {
				if("1".equals(dealType)) {
					where+=" AND W.RETURN_STAFF="+staffId+"";
				}else if("2".equals(dealType)){
					where+=" AND W.DEAL_STAFF="+staffId+"";
				}
			}
		}
		return where;
	}

	private String setParam3(String where,String sheetId,String urgencyGradeFlag){
		if(!"".equals(sheetId)) {
			where+=" AND W.WORK_SHEET_ID='"+sheetId+"'";
		}
		if("1".equals(urgencyGradeFlag)) {
			where+=" AND ( W.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(MINUTE,W.CREAT_DATE,NOW()) - W.HANGUP_TIME_COUNT ) < 0) ";
		}
		if("2".equals(urgencyGradeFlag)) {
			where+=" AND ( W.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(MINUTE,W.CREAT_DATE,NOW()) - W.HANGUP_TIME_COUNT ) > 0) ";
		}
		return where;
	}
	
	private String buildForceWhere(JSONObject str, String where, String dealType) {
		if("1".equals(dealType)) {//1分派 2释放
			int bestOrder = str.optInt("bestOrder");
			if(bestOrder > 0) {
				where+=" AND D.BEST_ORDER="+bestOrder;
			}
			String labelFlag = str.optString("labelFlag");
			if(StringUtils.isNotEmpty(labelFlag)) {
				String[] labels = labelFlag.split("-");
				if(labelFlag.startsWith("refundFlag") && labels.length > 1) {
					String refundFlag = labels[1];
					where+=" AND L.REFUND_FLAG = " + refundFlag;
				}
			}
		}
		return where;
	}
}