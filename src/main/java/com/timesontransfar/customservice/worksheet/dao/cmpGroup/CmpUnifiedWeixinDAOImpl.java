/*
 * 文 件 名：CmpUnifiedWeixinDAOImpl.java
 * 版    权：
 * 描    述：
 * 修改时间：2017-10-11
 * 修改内容：新增
 */
package com.timesontransfar.customservice.worksheet.dao.cmpGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.pojo.ComplaintInfo;
import com.timesontransfar.customservice.orderask.pojo.ComplaintInfoError;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;

/**
 * 操作表cc_cmp_unified_weixin
 * 
 * @version
 * @since
 */
@Component("Clq_UnifiedWeixinDAO_Impl")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CmpUnifiedWeixinDAOImpl {
	private static final Logger log = LoggerFactory.getLogger(CmpUnifiedWeixinDAOImpl.class);
	
	@Resource
	private JdbcTemplate jt;

    @Autowired
    private PubFunc pubFun;
    
    @Autowired
	private CustomerServiceFeign customerServiceFeign;

    public static final String ADDRESS_IP = "ADDRESS_IP";
    public static final String TIME_OUT = "TIME_OUT";
    public static final String CONTENT_TYPE = "Content-Type";
    public static final String MESSAGE = "接口请求地址：{}\n请求内容：{}\n接口返回：{}\n请求耗时：{}";

	private String insertUnifiedWeixinSql = 
"INSERT INTO CC_CMP_UNIFIED_WEIXIN(CALL_METHOD,PHONE_NUMBER,IN_INFO,CREATE_DATE,RETURN_CODE,MESSAGE,OUT_INFO,X_CTG_REQUEST_ID,SYS_SOURCE,METHOD_DETAIL,"
+ "CUST_EVAL_POINT,EVAL_CHANNEL_NBR)VALUES(?,?,?,NOW(),?,?,?,?,?,?,?,?)";

	public int insertUnifiedWeixin(String callMethod, String phoneNumber, String inInfo, String returnCode, String message, 
			String outInfo, String xCtgRequestId, String sysSource, String methodDetail, String custEvalPoint, String evalchannelNbr) {
		return jt.update(insertUnifiedWeixinSql, callMethod, phoneNumber, inInfo, returnCode, message,
				outInfo, xCtgRequestId, sysSource, methodDetail, custEvalPoint, evalchannelNbr);
	}

	private String sumUnifiedWeixinByPhoneNumberSql = 
"SELECT IFNULL(SUM(IF(CALL_METHOD IN('complaintcancel','cancel'),1,0)),0)CANCEL_NUM,IFNULL(SUM(IF(CALL_METHOD IN('complaintremind','hasten'),1,0)),0)"
+ "REMIND_NUM,IFNULL(SUM(IF(CALL_METHOD='complainteval',1,0)),0)EVAL_NUM,IFNULL(SUM(IF(TIMESTAMPDIFF(SECOND,CREATE_DATE,NOW())<=2*3600 AND CALL_METHOD "
+ "IN('complaintremind','hasten'),1,0)),0)RECENTLY_NUM FROM CC_CMP_UNIFIED_WEIXIN WHERE RETURN_CODE='1'AND PHONE_NUMBER=?";

	public List sumUnifiedWeixinByPhoneNumber(String phoneNumber) {
		return jt.queryForList(sumUnifiedWeixinByPhoneNumberSql, phoneNumber);
	}

	private String selectUnifiedShowSql = 
"SELECT B.COMPLAINT_STATUS COMPLAINTSTATUS,DATE_FORMAT(A.PUSH_DATE,'%Y-%m-%d %H:%i:%s')STATUSDATE,B.COMPLAINT_STATUS_DETAIL COMPLAINTSTATUSDETAIL,"
+ "A.PUSH_MSG COMPLAINTSTATUSDESC FROM CC_CMP_UNIFIED_SHOW A,CC_CMP_STATUS_INFO_CONFIG B WHERE A.STEP=B.LOCAL_STEP AND A.COMPLAINT_WORKSHEET_ID=?";

	private String selectUnifiedShowHisSql = 
"SELECT B.COMPLAINT_STATUS COMPLAINTSTATUS,DATE_FORMAT(A.PUSH_DATE,'%Y-%m-%d %H:%i:%s')STATUSDATE,B.COMPLAINT_STATUS_DETAIL COMPLAINTSTATUSDETAIL,"
+ "A.PUSH_MSG COMPLAINTSTATUSDESC FROM CC_CMP_UNIFIED_SHOW_HIS A,CC_CMP_STATUS_INFO_CONFIG B WHERE A.STEP=B.LOCAL_STEP AND A.COMPLAINT_WORKSHEET_ID=?";

	public List selectUnifiedShowAll(String complaintWorksheetId, String tacheId, boolean sysFlag) {
		String sql = "";
		if ("1004".equals(tacheId)) {
			sql = selectUnifiedShowHisSql;
		} else {
			sql = selectUnifiedShowSql;
		}
		if (sysFlag) {
			sql += " AND B.LOCAL_QUERY=0 ORDER BY B.COMPLAINT_STATUS,B.COMPLAINT_STATUS_DETAIL,A.PUSH_DATE";
		} else {
			sql += " AND B.CLIQUE_QUERY=0 ORDER BY B.COMPLAINT_STATUS,B.COMPLAINT_STATUS_DETAIL,A.PUSH_DATE";
		}
		return jt.queryForList(sql, complaintWorksheetId);
	}

	private String selectUnifiedProgSqlContentByTacheIdSql = "SELECT SQL_CONTENT FROM CC_CMP_UNIFIED_PROG WHERE PROG_ID='complaint' AND TACHE_ID=?";

	public List selectUnifiedProgSqlContentByTacheId(String unifiedComplaintCode, String tacheId) {
		List progList = jt.queryForList(selectUnifiedProgSqlContentByTacheIdSql, tacheId);
		if (progList.isEmpty()){
			return Collections.emptyList();
		}
		Map progMap = (Map) progList.get(0);
		String sqlContent = progMap.get("SQL_CONTENT").toString();
		progList.clear();
		progList = null;
		if (sqlContent.equals("")) {
			return Collections.emptyList();
		}
		try {
			return jt.queryForList(sqlContent.replace("%UNIFIEDCOMPLAINTCODE%", "'" + unifiedComplaintCode + "'"));//CodeSec未验证的SQL注入；CodeSec误报：2
		} catch (Exception e) {
			log.error("selectUnifiedProgSqlContentByTacheId: {}", e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	private String selectUnifiedTemplateByStepSql = 
"SELECT B.COMPLAINT_STATUS,B.COMPLAINT_STATUS_DETAIL,B.LOCAL_PUSH,B.CLIQUE_PUSH,A.SQL_MSG FROM CC_CMP_UNIFIED_TEMPLATE A,CC_CMP_STATUS_INFO_CONFIG B "
+ "WHERE A.STEP=B.LOCAL_STEP AND A.STEP=?";

	private String insertUnifiedShowSql = 
"INSERT INTO CC_CMP_UNIFIED_SHOW(SERVICE_ORDER_ID,COMPLAINT_WORKSHEET_ID,STEP,PUSH_DATE,PUSH_MSG,SHOW_FLAG,BAND_FLAG)VALUES(?,?,?,STR_TO_DATE(?,"
+ "'%Y%m%d%H%i%s'),?,?,?)";

	public Map insertUnifiedShow(int step, String serviceOrderId) {
		List templateList = jt.queryForList(selectUnifiedTemplateByStepSql, step);
		if (templateList.isEmpty()) {
			return Collections.emptyMap();
		}
		Map templateMap = (Map) templateList.get(0);
		String complaintStatus = templateMap.get("COMPLAINT_STATUS").toString();
		String complaintStatusDetail = "";
		if (null == templateMap.get("COMPLAINTSTATUSDETAIL")) {
			complaintStatusDetail = templateMap.get("COMPLAINT_STATUS_DETAIL").toString();
		}
		String localPush = templateMap.get("LOCAL_PUSH").toString();
		String cliquePush = templateMap.get("CLIQUE_PUSH").toString();
		String sqlMsg = templateMap.get("SQL_MSG").toString();
		templateList.clear();
		templateList = null;
		if (sqlMsg.equals("")) {
			return Collections.emptyMap();
		}
		List list = jt.queryForList(sqlMsg.replace("%SERVICEORDERID%", "'" + serviceOrderId + "'"));//CodeSec未验证的SQL注入；CodeSec误报：1
		if (list.isEmpty()) {
			return Collections.emptyMap();
		}
		Map map = (Map) list.get(0);
		list.clear();
		list = null;
		
		String smsFlag = "0";//该环节进度推送，是否只发短信：1-是；0-否
		if(map.containsKey("SMS_FLAG")) {
			smsFlag = map.get("SMS_FLAG").toString();
		}
		String bandFlag = "";//是否绑定微信：1-是；0-否
		if("1".equals(smsFlag)) {
			bandFlag = "0";
		} else {
			bandFlag = queryWechatBanding(map.get("ACCNBR").toString());
		}
		
		jt.update(insertUnifiedShowSql, serviceOrderId, map.get("COMPLAINTID").toString(), step, map.get("ACPTTIME").toString(),
				map.get("PUSHMSG").toString(), map.get("SHOWFLAG").toString(), bandFlag);
		map.put("BANDFLAG", bandFlag);
		map.put("COMPLAINTSTATUS", complaintStatus);
		map.put("COMPLAINTSTATUSDETAIL", complaintStatusDetail);
		map.put("LOCALPUSH", localPush);
		map.put("CLIQUEPUSH", cliquePush);
		if(map.containsKey("PUSHMSG_NOTE")) {//短信推送消息
			map.put("PUSHMSG_NOTE", map.get("PUSHMSG_NOTE").toString());
		} else {
			map.put("PUSHMSG_NOTE", map.get("PUSHMSG").toString());
		}
		if(map.containsKey("PUSHMSG_WECHAT")) {//微信推送消息
			map.put("PUSHMSG_WECHAT", map.get("PUSHMSG_WECHAT").toString());
		} else {
			map.put("PUSHMSG_WECHAT", "");
		}
		return map;
	}

	// 查询号码是否微信绑定
	private String queryWechatBanding(String accNum) {
		return customerServiceFeign.queryWechatBanding(accNum);
	}

	// 省内电渠推送
	public void complaintPush(String complaintPhone, String complaintWorksheetId, String acceptDate, String pushMsg) {
		if(StringUtils.isBlank(pushMsg)) {//推送消息为空时不发送
			return;
		}
		Map map = pubFun.getIntfMap("pushComplaint");
		if (null == map) {
			return;
		}
		String urlStr = map.get(ADDRESS_IP).toString();
		
		Map mHead = new HashMap();
		mHead.put("transId", pubFun.getWXTransactionId());
		Map mBody = new HashMap();
		mBody.put("complaintId", complaintWorksheetId);
		mBody.put("accnbr", complaintPhone);
		mBody.put("acptTime", acceptDate);
		mBody.put("pushMsg", pushMsg);
		Map mParam = new HashMap();
		mParam.put("head", mHead);
		mParam.put("body", mBody);
		Map ms = new HashMap();
		ms.put("param", mParam);
		String param = new Gson().toJson(ms);
		long startTime = System.currentTimeMillis();
		
		HttpURLConnection connection = null;
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(Integer.parseInt(map.get(TIME_OUT).toString()));
			connection.setReadTimeout(Integer.parseInt(map.get(TIME_OUT).toString()));
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty(CONTENT_TYPE, "application/json");
			connection.connect();
			try (
				OutputStream os = connection.getOutputStream();
			){
				os.write(param.getBytes(StandardCharsets.UTF_8));
				os.flush();
			}
			
			try (
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
			){
				String temp = "";
				while((temp = br.readLine())!=null) {
					sb.append(temp);
				}
			}
			
			JSONObject resultJson = JSON.parseObject(sb.toString());
			String tsrResult = resultJson.getString("TSR_RESULT");
			String tsrMsg = resultJson.getString("TSR_MSG");
			
			String result = "0000".equals(tsrResult) ? "0" : "1";
			insertUnifiedPush(complaintPhone, complaintWorksheetId, result, tsrResult, tsrMsg, "", "");
		} catch (Exception e) {
			log.error("省内电渠推送 complaintPush 异常: {}", e.getMessage(), e);
		} finally {
			log.info(MESSAGE,urlStr,param,sb,(System.currentTimeMillis()-startTime));
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Exception e) {
					log.error("Exception：{}", e.getMessage(), e);
				}
			}
		}
	}
	
	private String insertUnifiedPushSql =
"INSERT INTO CC_CMP_UNIFIED_PUSH(COMPLAINT_PHONE,COMPLAINT_WORKSHEET_ID,RESULT,CODE,MSG,CREATE_DATE,X_CTG_REQUEST_ID,RESPONSE_CODE)VALUES(?,?,?,?,?,"
+ "NOW(),?,?)";

	private int insertUnifiedPush(String complaintPhone, String complaintWorksheetId, String result, String code,
			String msg, String xCtgRequestId, String responseCode) {
		return jt.update(insertUnifiedPushSql,
				complaintPhone, complaintWorksheetId, result, code, msg, xCtgRequestId, responseCode);
	}

	public String complaintPostInfo(ComplaintInfo ci, String xCtgLanId) {
		long startTime=System.currentTimeMillis();
		Map map = pubFun.getIntfMap("complaintPostInfo");
		if (null == map) {
			return "";
		}
		
		HttpURLConnection connection = null;
		InputStream is = null;
		OutputStream os = null;
		BufferedReader br = null;
		StringBuffer sb = new StringBuffer();
		String urlStr = map.get(ADDRESS_IP).toString();
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setConnectTimeout(Integer.parseInt(map.get(TIME_OUT).toString()));
			connection.setReadTimeout(Integer.parseInt(map.get(TIME_OUT).toString()));
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(false);
			connection.setRequestProperty(CONTENT_TYPE, "application/json");
			connection.addRequestProperty("X-APP-ID", map.get("X_APP_ID").toString());
			connection.addRequestProperty("X-APP-KEY", map.get("X_APP_KEY").toString());
			String xCtgRequestId = UUID.randomUUID().toString();
			connection.addRequestProperty("X-CTG-Request-ID", xCtgRequestId);
			connection.addRequestProperty("X-CTG-Province-ID", "8320000");
			connection.addRequestProperty("X-CTG-Lan-ID", xCtgLanId);
			connection.connect();
			os = connection.getOutputStream();
			os.write(new Gson().toJson(ci).getBytes(StandardCharsets.UTF_8));
			os.flush();
			
			int responseCode = connection.getResponseCode();
			log.info("complaintPostInfo responseCode: {}", responseCode);
			if (responseCode == 200) {
				is = connection.getInputStream();
			} else {
				is = connection.getErrorStream();
			}
			br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			String temp = "";
			while ((temp = br.readLine()) != null) {
				sb.append(temp);
			}
			log.info("complaintPostInfo result：{}", sb);
			
			String result = "1";
			String code = "";
			String msg = "";
			if (sb.length() != 0) {
				result = "0";
				ComplaintInfoError cir = new Gson().fromJson(sb.toString(), ComplaintInfoError.class);
				code = cir.getCode();
				msg = cir.getReason();
			}
			insertUnifiedPush(ci.getContractPhoneNbr(), ci.getComplaintOrderId(), result, code, msg, xCtgRequestId, responseCode + "");
		} catch (Exception e) {
			log.error("集团电渠推送 complaintPostInfo 异常: {}", e.getMessage(), e);
		} finally {
			log.info(MESSAGE,urlStr,new Gson().toJson(ci),sb,(System.currentTimeMillis()-startTime));
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != os) {
				try {
					os.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			connection.disconnect();
		}
		return "";
	}

	private String selectUnifiedTemplateSqlMsgByStepSql = "SELECT SQL_MSG FROM CC_CMP_UNIFIED_TEMPLATE WHERE STEP=?";

	private String insertBusinessStatusSendSql =
"INSERT INTO CC_BUSINESS_STATUS_SEND(SERVICE_ORDER_ID,NEW_ORDER_ID,BUSINESS_ID,BUSINESS_STATUS,EXPECT_FINISH_DATE,SEND_DATE,RELA_INFO,"
+ "COMPLAINT_WORKSHEET_ID,SEND_FLAG,SEND_MSG)VALUES(?,?,?,?,?,NOW(),?,?,?,?)";
	
	private String getQuerySql(String sqlMsg, List<String> list, String serviceOrderId, String expectFinishDate) {
		if(StringUtils.countMatches(sqlMsg, "%SERVICEORDERID%") > 0 || StringUtils.countMatches(sqlMsg, "%EXPECTFINISHDATE%") > 0) {
			int a = StringUtils.indexOf(sqlMsg, "%SERVICEORDERID%");
			int b = StringUtils.indexOf(sqlMsg, "%EXPECTFINISHDATE%");
			if(a > -1 && b > -1) {
				if(a < b) {
					sqlMsg = StringUtils.replaceOnce(sqlMsg, "%SERVICEORDERID%", "?");
					list.add(serviceOrderId);
					return this.getQuerySql(sqlMsg, list, serviceOrderId, expectFinishDate);
				}
				else {
					sqlMsg = StringUtils.replaceOnce(sqlMsg, "%EXPECTFINISHDATE%", "', ?, '");
					list.add(expectFinishDate);
					return this.getQuerySql(sqlMsg, list, serviceOrderId, expectFinishDate);
				}
			}
			else if(a > -1) {
				sqlMsg = StringUtils.replaceOnce(sqlMsg, "%SERVICEORDERID%", "?");
				list.add(serviceOrderId);
				return this.getQuerySql(sqlMsg, list, serviceOrderId, expectFinishDate);
			}
			else {
				sqlMsg = StringUtils.replaceOnce(sqlMsg, "%EXPECTFINISHDATE%", "', ?, '");
				list.add(expectFinishDate);
				return this.getQuerySql(sqlMsg, list, serviceOrderId, expectFinishDate);
			}
		}
		return sqlMsg;
	}

	public Map insertBusinessStatusSend(int step, String serviceOrderId, String newOrderId, String businessId,
			String businessStatus, String expectFinishDate) {
		List stepList = jt.queryForList(selectUnifiedTemplateSqlMsgByStepSql, step);
		if (stepList.isEmpty()) {
			return Collections.emptyMap();
		}
		Map stepMap = (Map) stepList.get(0);
		String sqlMsg = stepMap.get("SQL_MSG").toString();
		stepList.clear();
		stepList = null;
		if (sqlMsg.equals("")) {
			return Collections.emptyMap();
		}
		List<String> args = new ArrayList<>();
		sqlMsg = this.getQuerySql(sqlMsg, args, serviceOrderId, expectFinishDate);
		log.info("insertBusinessStatusSend getQuerySql: {}", sqlMsg);
		log.info("insertBusinessStatusSend args: {}", JSON.toJSON(args));
		List list = jt.queryForList(sqlMsg, args.toArray());//CodeSec未验证的SQL注入；CodeSec误报：1
		log.info("insertBusinessStatusSend list: {}", JSON.toJSON(list));
		if (list.isEmpty()) {
			return Collections.emptyMap();
		}
		Map map = (Map) list.get(0);
		list.clear();
		list = null;
		String accnbr = map.get("ACCNBR") == null ? "" : map.get("ACCNBR").toString();
		String complaintid = map.get("COMPLAINTID") == null ? "" : map.get("COMPLAINTID").toString();
		String pushFlag = map.get("PUSHFLAG").toString();
		String pushmsg = map.get("PUSHMSG").toString();
		
		jt.update(insertBusinessStatusSendSql, serviceOrderId, newOrderId, businessId, businessStatus,
				expectFinishDate, accnbr, complaintid, pushFlag, pushmsg);
		return map;
	}

	private String checknoAnswerByOrderIdSql =
"SELECT IF(TIMESTAMPDIFF(SECOND,PUSH_DATE,NOW())<3600,1,0) INTERVAL_FLAG FROM CC_CMP_UNIFIED_SHOW A WHERE SERVICE_ORDER_ID=? AND STEP=? AND NOT EXISTS("
+ "SELECT 1 FROM CC_CMP_UNIFIED_SHOW B WHERE A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND STEP IN(8,9,10)) ORDER BY PUSH_DATE DESC";

	public List checknoAnswerByOrderId(String orderId, int step) {
		return jt.queryForList(checknoAnswerByOrderIdSql, orderId, step);
	}

	private String selectPushTimeSql =
"SELECT IF(DATE_FORMAT(NOW(),'%H%i%s')BETWEEN 080000 AND 120000 OR DATE_FORMAT(NOW(),'%H%i%s')BETWEEN 140000 AND 220000,0,1)PUSH_TIME";

	private String deleteUnifiedShowByOrderIdSql = "DELETE FROM CC_CMP_UNIFIED_SHOW WHERE SERVICE_ORDER_ID=?";

	private String insertUnifiedShowHisByOrderIdSql =
"INSERT INTO CC_CMP_UNIFIED_SHOW_HIS(SERVICE_ORDER_ID,COMPLAINT_WORKSHEET_ID,STEP,PUSH_DATE,PUSH_MSG,SHOW_FLAG,BAND_FLAG)SELECT SERVICE_ORDER_ID,"
+ "COMPLAINT_WORKSHEET_ID,STEP,PUSH_DATE,PUSH_MSG,SHOW_FLAG,BAND_FLAG FROM CC_CMP_UNIFIED_SHOW WHERE SERVICE_ORDER_ID=?";

	public int queryPushTime() {
		return jt.queryForObject(selectPushTimeSql, Integer.class);
	}

	public int saveUnifiedShowHisByOrderId(String orderId) {
		if (jt.update(insertUnifiedShowHisByOrderIdSql, orderId) > 0) {
			return jt.update(deleteUnifiedShowByOrderIdSql, orderId);
		}
		return 0;
	}
}