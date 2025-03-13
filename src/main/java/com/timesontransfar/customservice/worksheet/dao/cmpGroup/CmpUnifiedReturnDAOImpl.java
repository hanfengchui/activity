/*
 * 文 件 名：CmpUnifiedReturnDAOImpl.java
 * 版    权：
 * 描    述：
 * 修改时间：2017-9-11
 * 修改内容：新增
 */
package com.timesontransfar.customservice.worksheet.dao.cmpGroup;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturnRmp;
import com.cliqueWorkSheetWebService.util.StaticDataClique;
import com.google.gson.Gson;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.feign.activiti.PubFuncFeign;

/**
 * 操作表cc_cmp_unified_return
 * 
 * @version
 * @since
 */
@SuppressWarnings("rawtypes")
@Component("cmpUnifiedReturnDAOImpl")
public class CmpUnifiedReturnDAOImpl {
	protected static Logger log = LoggerFactory.getLogger(CmpUnifiedReturnDAOImpl.class);
	
	@Resource
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	@Qualifier("PubFuncFeign")
	private PubFuncFeign pubFuncFeign;
	
	@Autowired
	private InoteSenList noteSenListDao;

	private String queryComplaintInfoByWhereSql = 
"SELECT A.UNIFIED_COMPLAINT_CODE COMPLAINTORDERID,IFNULL(D.CRM_CUST_ID,'-1')CUSTID,A.COMPLAINT_PHONE ACCNBR,D.CUST_NAME CUSTNAME,A.RELA_INFO "
+ "CONTRACTPHONENBR,DATE_FORMAT(B.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')COMPLAINTDATE,IF(B.SERVICE_TYPE=720130000,2,1)COMPLAINTTYPE,IFNULL((SELECT "
+ "Y.ACCEPT_TYPE FROM CC_DYNAMIC_TEMPLATE_SIX X,CC_DYNAMIC_TEMPLATE Y WHERE X.SIX_ID=IF(C.SIX_GRADE_CATALOG=0,B.RELA_TYPE,C.SIX_GRADE_CATALOG)AND X.TEMPLATE_ID=Y.TEMPLATE_ID LIMIT 1),"
+ "1099)ACCEPTTYPE,IFNULL((SELECT Y.N_DESC FROM CC_CMP_DATA_MAP X,CCS_ST_MAPPING_JT_F_N Y WHERE X.FLAG=1 AND X.FEATURE_CODE="
+ "'CC_SERVICE_CONTENT_ASK-SIX_GRADE_CATALOG'AND X.DST_CODE=Y.FUN_ID AND X.SRC_CODE=C.SIX_GRADE_CATALOG LIMIT 1),C.APPEAL_PROD_NAME)COMPLAINTMESSAGE,IFNULL(("
+ "SELECT Z.LOGONNAME FROM TSM_STAFF Z WHERE Z.STAFF_ID=B.ACCEPT_STAFF_ID LIMIT 1),B.ACCEPT_STAFF_ID)SALESCODE,CASE WHEN B.ACCEPT_CHANNEL_ID=707907008"
+ " THEN 110101 WHEN B.ACCEPT_CHANNEL_ID=707907009 THEN 120101 WHEN B.ACCEPT_CHANNEL_ID=707907010 THEN 120304 WHEN B.ACCEPT_CHANNEL_ID IN(707907007,"
+ "707907036,707907037,707907039,707907040)THEN 120501 WHEN B.ACCEPT_CHANNEL_ID=707907115 THEN 120502 WHEN B.ACCEPT_CHANNEL_ID=707907014 THEN 120503 "
+ "WHEN B.ACCEPT_CHANNEL_ID=707907013 THEN 120506 WHEN B.ACCEPT_CHANNEL_ID=707907038 THEN 120514 ELSE 120516 END SALESTHIRDTYPE,B.ACCEPT_CHANNEL_ID "
+ "CHANNELNBR,IFNULL((SELECT Z.CUST_EVAL_POINT FROM CC_CMP_UNIFIED_WEIXIN Z WHERE Z.CALL_METHOD='complaintEval'AND Z.RETURN_CODE="
+ "'1'AND Z.PHONE_NUMBER=A.UNIFIED_COMPLAINT_CODE ORDER BY Z.CREATE_DATE DESC LIMIT 1),0)CUSTEVALPOINT,IFNULL((SELECT Z.EVAL_CHANNEL_NBR FROM "
+ "CC_CMP_UNIFIED_WEIXIN Z WHERE Z.CALL_METHOD='complaintEval'AND Z.RETURN_CODE='1'AND Z.PHONE_NUMBER=A.UNIFIED_COMPLAINT_CODE ORDER BY Z.CREATE_DATE "
+ "DESC LIMIT 1),0)EVALCHANNELNBR,IF(B.ORDER_STATU=720130007,0,IF((SELECT COUNT(1)FROM CC_CMP_UNIFIED_WEIXIN Z WHERE Z.PHONE_NUMBER="
+ "A.UNIFIED_COMPLAINT_CODE AND Z.CALL_METHOD IN('complaintCancel','CANCEL')AND Z.RETURN_CODE='1')=0,1,0))ISREMIND,IF(B.ORDER_STATU=720130007,0,IF(("
+ "SELECT COUNT(1)FROM CC_CMP_UNIFIED_WEIXIN Z WHERE Z.PHONE_NUMBER=A.UNIFIED_COMPLAINT_CODE AND Z.CALL_METHOD IN('complaintCancel','CANCEL')AND "
+ "Z.RETURN_CODE='1')=0,IF((SELECT COUNT(1)FROM CC_CMP_UNIFIED_SHOW Z WHERE Z.COMPLAINT_WORKSHEET_ID=A.UNIFIED_COMPLAINT_CODE AND Z.STEP IN(8,9,10))=0,"
+ "1,0),0))ISCANCEL,IF(B.ORDER_STATU=720130007,1003,IF((SELECT COUNT(1)FROM CC_WORK_SHEET Z WHERE Z.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND "
+ "Z.SHEET_TYPE IN(700000126,720130011)AND Z.LOCK_FLAG=0)=0,1002,1001))TACHE_ID,DATE_FORMAT(B.FINISH_DATE,'%Y-%m-%d %H:%i:%s')FINISH_DATE,"
+ "B.ACCEPT_DATE FROM CC_CMP_UNIFIED_RETURN A,CC_SERVICE_ORDER_ASK B,CC_SERVICE_CONTENT_ASK C,CC_ORDER_CUST_INFO D WHERE A.UNIFIED_COMPLAINT_CODE IS "
+ "NOT NULL AND A.COMPLAINT_WORKSHEET_ID=B.SERVICE_ORDER_ID AND B.ACCEPT_DATE>DATE_SUB(NOW(),INTERVAL 366 DAY)AND B.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID"
+ " AND B.CUST_GUID=D.CUST_GUID";

	private String queryComplaintInfoHisByWhereSql = 
"SELECT A.UNIFIED_COMPLAINT_CODE COMPLAINTORDERID,IFNULL(D.CRM_CUST_ID,'-1')CUSTID,A.COMPLAINT_PHONE ACCNBR,D.CUST_NAME CUSTNAME,A.RELA_INFO "
+ "CONTRACTPHONENBR,DATE_FORMAT(B.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')COMPLAINTDATE,IF(B.SERVICE_TYPE=720130000,2,1)COMPLAINTTYPE,IFNULL((SELECT "
+ "Y.ACCEPT_TYPE FROM CC_DYNAMIC_TEMPLATE_SIX X,CC_DYNAMIC_TEMPLATE Y WHERE X.SIX_ID=IF(C.SIX_GRADE_CATALOG=0,B.RELA_TYPE,C.SIX_GRADE_CATALOG)AND X.TEMPLATE_ID=Y.TEMPLATE_ID LIMIT 1)"
+ ",1099)ACCEPTTYPE,IFNULL((SELECT Y.N_DESC FROM CC_CMP_DATA_MAP X,CCS_ST_MAPPING_JT_F_N Y WHERE X.FLAG=1 AND X.FEATURE_CODE="
+ "'CC_SERVICE_CONTENT_ASK-SIX_GRADE_CATALOG'AND X.DST_CODE=Y.FUN_ID AND X.SRC_CODE=C.SIX_GRADE_CATALOG LIMIT 1),C.APPEAL_PROD_NAME)COMPLAINTMESSAGE,IFNULL(("
+ "SELECT Z.LOGONNAME FROM TSM_STAFF Z WHERE Z.STAFF_ID=B.ACCEPT_STAFF_ID LIMIT 1),B.ACCEPT_STAFF_ID)SALESCODE,CASE WHEN B.ACCEPT_CHANNEL_ID=707907008"
+ " THEN 110101 WHEN B.ACCEPT_CHANNEL_ID=707907009 THEN 120101 WHEN B.ACCEPT_CHANNEL_ID=707907010 THEN 120304 WHEN B.ACCEPT_CHANNEL_ID IN(707907007,"
+ "707907036,707907037,707907039,707907040)THEN 120501 WHEN B.ACCEPT_CHANNEL_ID=707907115 THEN 120502 WHEN B.ACCEPT_CHANNEL_ID=707907014 THEN 120503 "
+ "WHEN B.ACCEPT_CHANNEL_ID=707907013 THEN 120506 WHEN B.ACCEPT_CHANNEL_ID=707907038 THEN 120514 ELSE 120516 END SALESTHIRDTYPE,B.ACCEPT_CHANNEL_ID "
+ "CHANNELNBR,IFNULL((SELECT Z.CUST_EVAL_POINT FROM CC_CMP_UNIFIED_WEIXIN Z WHERE Z.CALL_METHOD='complaintEval'AND Z.RETURN_CODE="
+ "'1'AND Z.PHONE_NUMBER=A.UNIFIED_COMPLAINT_CODE ORDER BY Z.CREATE_DATE DESC LIMIT 1),0)CUSTEVALPOINT,IFNULL((SELECT Z.EVAL_CHANNEL_NBR FROM "
+ "CC_CMP_UNIFIED_WEIXIN Z WHERE Z.CALL_METHOD='complaintEval'AND Z.RETURN_CODE='1'AND Z.PHONE_NUMBER=A.UNIFIED_COMPLAINT_CODE ORDER BY Z.CREATE_DATE "
+ "DESC LIMIT 1),0)EVALCHANNELNBR,0 ISREMIND,0 ISCANCEL,1004 TACHE_ID,DATE_FORMAT(B.FINISH_DATE,'%Y-%m-%d %H:%i:%s')FINISH_DATE,B.ACCEPT_DATE FROM "
+ "CC_CMP_UNIFIED_RETURN_HIS A,CC_SERVICE_ORDER_ASK_HIS B,CC_SERVICE_CONTENT_ASK_HIS C,CC_ORDER_CUST_INFO_HIS D WHERE A.UNIFIED_COMPLAINT_CODE IS NOT "
+ "NULL AND A.COMPLAINT_WORKSHEET_ID=B.SERVICE_ORDER_ID AND B.ORDER_STATU IN(700000103,3000047,720130002,720130010)AND B.ACCEPT_DATE>DATE_SUB(NOW(),"
+ "INTERVAL 366 DAY)AND B.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND B.ORDER_VESION=C.ORDER_VESION AND B.CUST_GUID=D.CUST_GUID";

	public List queryComplaintInfoByWhere(String where) {
		String qci = queryComplaintInfoByWhereSql + where;
		String qcih = queryComplaintInfoHisByWhereSql + where;
		String querySql = "SELECT*FROM(" + qci + " UNION ALL " + qcih + ")Z ORDER BY ACCEPT_DATE DESC";
		return jdbcTemplate.queryForList(querySql);//CodeSec未验证的SQL注入；CodeSec误报：1
	}

	private String queryCustProdByOrderIdSql = 
"SELECT B.CUST_NAME,A.PROD_NUM,DATE_FORMAT(A.ACCEPT_DATE,'%Y%m%d%H%i%s')ACCEPT_DATE,A.RELA_INFO,A.SOURCE_NUM,IF(A.SERVICE_TYPE=720130000,3,IF("
+ "A.SERVICE_TYPE IN(700006312,720200003),1,7))SERVICE_TYPE,IFNULL((SELECT REGION_ID FROM BSS_REGION WHERE LOCAL_ID=A.REGION_ID LIMIT 1),8320100)LAN_ID "
+ "FROM CC_SERVICE_ORDER_ASK A,CC_ORDER_CUST_INFO B WHERE A.CUST_GUID=B.CUST_GUID AND A.SERVICE_ORDER_ID=?";

	private String insertUnifiedReturnSql = 
"INSERT INTO CC_CMP_UNIFIED_RETURN(COMPLAINT_WORKSHEET_ID,PROVINCE_CODE,CUST_NAME,COMPLAINT_PHONE,NEW_DATE,RELA_INFO,SOURCE_NUM,NOTE_FLAG,MSG_FLAG,"
+ "CREATE_DATE,WORKSHEET_TYPES,LAN_ID)VALUES(?,?,?,?,STR_TO_DATE(?,'%Y%m%d%H%i%s'),?,?,?,?,NOW(),?,?)";

	public int saveUnifiedReturn(String orderId, String noteFlag, String msgFlag) {
		List tmpList = this.jdbcTemplate.queryForList(queryCustProdByOrderIdSql, orderId);
		if (tmpList.isEmpty()) {
			return 0;
		}
		Map tmpMap = (Map) tmpList.get(0);
		return jdbcTemplate.update(insertUnifiedReturnSql,
						orderId, StaticDataClique.PROV_CODE_JS, tmpMap.get("CUST_NAME").toString(),
						checkNumber(tmpMap.get("PROD_NUM").toString()), tmpMap.get("ACCEPT_DATE").toString(),
						checkNumber(tmpMap.get("RELA_INFO").toString()),
						tmpMap.get("SOURCE_NUM") == null ? "" : checkNumber(tmpMap.get("SOURCE_NUM").toString()),
						noteFlag, msgFlag, tmpMap.get("SERVICE_TYPE").toString(), tmpMap.get("LAN_ID").toString());
	}

	private String selectUnifiedReturnByOrderIdSql = 
"SELECT COMPLAINT_WORKSHEET_ID,PROVINCE_CODE,CUST_NAME,COMPLAINT_PHONE,DATE_FORMAT(NEW_DATE,'%Y%m%d%H%i%s')NEW_DATE,RESULT,UNIFIED_COMPLAINT_CODE,CODE,"
+ "MSG,CALL_TYPE,OPER_LOGON,DATE_FORMAT(CREATE_DATE,'%Y%m%d%H%i%s')CREATE_DATE,DATE_FORMAT(UPDATE_DATE,'%Y%m%d%H%i%s')UPDATE_DATE,RELA_INFO,SOURCE_NUM,"
+ "NOTE_FLAG,MSG_FLAG,FAIL_COUNT,IFNULL(WORKSHEET_TYPES,3)WORKSHEET_TYPES,LAN_ID FROM CC_CMP_UNIFIED_RETURN WHERE COMPLAINT_WORKSHEET_ID=?";

	@SuppressWarnings("unchecked")
	public ComplaintUnifiedReturn queryUnifiedReturnByOrderId(String orderId) {
		List list = jdbcTemplate.query(selectUnifiedReturnByOrderIdSql, new Object[] { orderId }, new ComplaintUnifiedReturnRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (ComplaintUnifiedReturn) list.get(0);
	}
	
	private String selectUnifiedReturnHisByOrderIdSql = 
"SELECT COMPLAINT_WORKSHEET_ID complaintWorksheetId,COMPLAINT_PHONE complaintPhone,DATE_FORMAT(NEW_DATE,'%Y%m%d%H%i%s') newDate,"
+ "UNIFIED_COMPLAINT_CODE unifiedComplaintCode,CALL_TYPE callType,OPER_LOGON operLogon,DATE_FORMAT(UPDATE_DATE,'%Y%m%d%H%i%s') updateDate,"
+ "RELA_INFO relaInfo,SOURCE_NUM sourceNum FROM CC_CMP_UNIFIED_RETURN_HIS WHERE COMPLAINT_WORKSHEET_ID=?";
	
	public ComplaintUnifiedReturn queryUnifiedReturnHisByOrderId(String orderId) {
		List list = jdbcTemplate.queryForList(selectUnifiedReturnHisByOrderIdSql, orderId);
		if (list.isEmpty()) {
			return null;
		}
		List<ComplaintUnifiedReturn> clist = JSON.parseArray(JSON.toJSONString(list), ComplaintUnifiedReturn.class);
		return clist.get(0);
	} 

	private String selectUnifiedReturnHisByUnifiedCodeSql = 
"SELECT COMPLAINT_WORKSHEET_ID,COMPLAINT_PHONE,DATE_FORMAT(NEW_DATE,'%Y%m%d%H%i%s')NEW_DATE,UNIFIED_COMPLAINT_CODE,CALL_TYPE,OPER_LOGON,DATE_FORMAT("
+ "UPDATE_DATE,'%Y%m%d%H%i%s')UPDATE_DATE,RELA_INFO,SOURCE_NUM FROM CC_CMP_UNIFIED_RETURN_HIS WHERE UNIFIED_COMPLAINT_CODE=?";

	public List queryUnifiedReturnHisByUnifiedCode(String unifiedCode) {
		return jdbcTemplate.queryForList(selectUnifiedReturnHisByUnifiedCodeSql,unifiedCode);
	}

	private String selectUnifiedReturnByWhereSql = 
"SELECT COMPLAINT_WORKSHEET_ID,PROVINCE_CODE,CUST_NAME,COMPLAINT_PHONE,DATE_FORMAT(NEW_DATE,'%Y%m%d%H%i%s')NEW_DATE,RESULT,UNIFIED_COMPLAINT_CODE,CODE,"
+ "MSG,CALL_TYPE,OPER_LOGON,DATE_FORMAT(CREATE_DATE,'%Y%m%d%H%i%s')CREATE_DATE,DATE_FORMAT(UPDATE_DATE,'%Y%m%d%H%i%s')UPDATE_DATE,RELA_INFO,SOURCE_NUM,"
+ "NOTE_FLAG,MSG_FLAG,FAIL_COUNT,IFNULL(WORKSHEET_TYPES,3)WORKSHEET_TYPES,LAN_ID FROM CC_CMP_UNIFIED_RETURN WHERE 1=1 ";

	@SuppressWarnings("unchecked")
	public List<ComplaintUnifiedReturn> queryUnifiedReturnByWhere(String where) {
		return jdbcTemplate.query(selectUnifiedReturnByWhereSql + where, new ComplaintUnifiedReturnRmp());//CodeSec未验证的SQL注入
	}

	private String insertUnifiedReturnHisByOrderIdSql = 
"INSERT INTO CC_CMP_UNIFIED_RETURN_HIS(COMPLAINT_WORKSHEET_ID,NEW_DATE,UNIFIED_COMPLAINT_CODE,CALL_TYPE,OPER_LOGON,UPDATE_DATE,COMPLAINT_PHONE,"
+ "RELA_INFO,SOURCE_NUM)SELECT COMPLAINT_WORKSHEET_ID,NEW_DATE,UNIFIED_COMPLAINT_CODE,CALL_TYPE,OPER_LOGON,UPDATE_DATE,COMPLAINT_PHONE,RELA_INFO,"
+ "SOURCE_NUM FROM CC_CMP_UNIFIED_RETURN WHERE COMPLAINT_WORKSHEET_ID=?";

	private String deleteUnifiedReturnByOrderIdSql = 
"DELETE FROM CC_CMP_UNIFIED_RETURN WHERE COMPLAINT_WORKSHEET_ID=?";

	public boolean sendNoteCont(String soureNum, String sendContent, String serviceId) {
		if ("".equals(soureNum)) {
			soureNum = " ";
		}
		String checkNum = soureNum;
		if (!"0".equals(soureNum.substring(0, 1))) {
			checkNum = "0" + soureNum;
		}
		Map map = this.noteSenListDao.getNumInfo(checkNum);
		String json = new Gson().toJson(map);
		log.info("getNumInfo result：{}",json);
		String numType = map.get("out_numtype").toString();// 1本省电信固话;2本省电信手机;4外省电信移动手机;6外省或其它
		if (!numType.equals("1")) {
			NoteSeand noteBean = new NoteSeand();
			noteBean.setSheetGuid(pubFuncFeign.crtGuid());
			noteBean.setRegionId(Integer.parseInt(map.get("out_regionid").toString()));
			noteBean.setDestteRmid(soureNum);
			if ("2".equals(numType) || "4".equals(numType)) {
				noteBean.setClientType(1);
			} else if ("3".equals(numType)) {
				noteBean.setClientType(0);// 终端标志 3本省电信小灵通
			} else if (checkNum.length() == 12) {
				noteBean.setClientType(1);
			} else {
				return false;
			}
			noteBean.setSendContent(sendContent);
			noteBean.setOrgId(StaticData.ACPT_ORGID_JT);
			noteBean.setOrgName(StaticData.ACPT_ORGNAME_JT);
			noteBean.setStaffId(StaticData.ACPT_STAFFID_JT);
			noteBean.setStaffName(StaticData.ACPT_STAFFNAME_JT);
			noteBean.setBusiId(serviceId);
			this.noteSenListDao.saveNoteContent(noteBean);
			return true;
		}
		return false;
	}

	public int saveUnifiedReturnHisByOrderId(String orderId) {
		if (1 == jdbcTemplate.update(insertUnifiedReturnHisByOrderIdSql, orderId)) {
			return jdbcTemplate.update(deleteUnifiedReturnByOrderIdSql, orderId);
		}
		return 0;
	}

	private String checkNumber(String number) {
		if (number.length() >= 11) {
			if (!number.substring(0, 1).equals("0")) {
				number = "0" + number;
			}
			Matcher m = Pattern.compile("^1[1-9]$").matcher(number.substring(1, 3));
			if (m.matches()) {
				number = number.substring(1);
			}
		}
		return number;
	}

	private String queryTravelCodeComplaintInfoByWhereSql =
"SELECT a.unified_complaint_code COMPLAINTORDERID,a.complaint_phone ACCNBR,DATE_FORMAT(b.accept_date,'%Y-%m-%d %H:%i:%s')COMPLAINTDATE,if(b.service_type"
+ "=720130000,2,1)COMPLAINTTYPE,b.service_order_id SERVICEORDERID FROM cc_cmp_unified_return a,cc_service_order_ask b,cc_service_content_ask c WHERE "
+ "a.unified_complaint_code IS NOT NULL AND b.accept_date>date_sub(now(),interval 1 day)AND c.appeal_reason_id=23002846 AND a.complaint_worksheet_id="
+ "b.service_order_id AND b.service_order_id=c.service_order_id AND b.region_id=? AND b.prod_num=? ORDER BY b.accept_date DESC LIMIT ";

	public List queryTravelCodeComplaintInfoByWhere(String accNbr, String regionCode, String maxNum) {
		List list = jdbcTemplate.queryForList(queryTravelCodeComplaintInfoByWhereSql + maxNum, regionCode, accNbr);//CodeSec未验证的SQL注入；CodeSec误报：1
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		return list;
	}
}