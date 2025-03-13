package com.timesontransfar.customservice.orderask.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import org.apache.axis.Constants;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.orderask.pojo.IntfLog;
import com.timesontransfar.customservice.orderask.service.IworkSheetToITSMWebService;
import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.timesontransfar.feign.file.FtpCommonFeign;
import com.transfar.common.log.CustomLogger;
import com.transfar.common.log.LogBean;

import net.sf.json.JSONObject;

@Component(value = "workSheetToITSMWebService")
public class WorkSheetToITSMWebService implements IworkSheetToITSMWebService {
	protected Logger logger = LoggerFactory.getLogger(WorkSheetToITSMWebService.class);
	
	@Autowired
	private CustomLogger log;
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
	private IComplaintWorksheetDeal complaintWorksheetDealImpl;
	@Resource
	private PubFunc pubFunc;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	@Autowired
	private IworkSheetBusi workSheetBusi;
	
	@Autowired
	private FtpCommonFeign ftpFeign;

    private String sqlWorksheetinfo = 
"SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,O.PROD_NUM,DATE_FORMAT(O.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(W.CREAT_DATE,"
+ "'%Y-%m-%d %H:%i:%s')CREAT_DATE,O.ACCEPT_COME_FROM_DESC,W.DEAL_LIMIT_TIME,W.SHEET_STATU_DESC,O.ACCEPT_COUNT,W.RETURN_STAFF_NAME,"
+ "W.SHEET_TYPE_DESC,W.REGION_NAME,I.CUST_NAME,I.ID_CARD,O.CUST_GROUP_DESC CUST_TYPE_NAME,I.CUST_SERV_GRADE_NAME,I.POST_CODE,I.TRADE_TYPE_DESC,I.PROD_TYPE_DESC,"
+ "I.ADDONES_INFO,I.INSTALL_DATE,I.CUST_BRAND_DESC,O.CUST_GROUP_DESC,I.PROD_STATUS_DESC,I.MAIL_ADDR,I.BRANCH_NO,I.INSTALL_ADDR,O.NODE_ID,"
+ "O.SERVICE_KEY,O.CUST_SERV_GRADE_DESC,O.PRODUCT_TYPE_NAME,O.RELA_MAN,O.RELA_INFO,O.SOURCE_NUM,O.VERIFY_REASON_ID,O.VERIFY_REASON_NAME,"
+ "O.ACCEPT_STAFF_NAME,O.ACCEPT_ORG_NAME,O.SERVICE_TYPE_DESC,O.URGENCY_GRADE_DESC,C.APPEAL_PROD_NAME,C.APPEAL_REASON_DESC,C.APPEAL_CHILD_DESC,"
+ "C.FOU_GRADE_CATALOG_DESC,O.SEND_TO_ORG_NAME,O.COME_CATEGORY_NAME,O.ACCEPT_CHANNEL_DESC,C.OUTLETS_NAME,C.ACCEPT_CONTENT,O.COMMENTS,W.MONTH_FLAG,"
+ "C.APPEAL_CHILD,W.REGION_ID FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK O,CC_SERVICE_CONTENT_ASK C,CC_ORDER_CUST_INFO I WHERE "
+ "W.SERVICE_ORDER_ID=O.SERVICE_ORDER_ID AND W.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND O.CUST_GUID=I.CUST_GUID AND O.SERVICE_DATE=3 AND "
+ "W.SHEET_TYPE=720130028 AND W.RECEIVE_ORG_ID='362813' AND W.LOCK_FLAG IN(0,1) AND W.SERVICE_ORDER_ID=? ORDER BY W.CREAT_DATE DESC";

	private String sqlWorksheetinfoBySheetId = 
"SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,O.PROD_NUM,DATE_FORMAT(O.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(W.CREAT_DATE,"
+ "'%Y-%m-%d %H:%i:%s')CREAT_DATE,O.ACCEPT_COME_FROM_DESC,W.DEAL_LIMIT_TIME,W.SHEET_STATU_DESC,O.ACCEPT_COUNT,W.RETURN_STAFF_NAME,"
+ "W.SHEET_TYPE_DESC,W.REGION_NAME,I.CUST_NAME,I.ID_CARD,O.CUST_GROUP_DESC CUST_TYPE_NAME,I.CUST_SERV_GRADE_NAME,I.POST_CODE,I.TRADE_TYPE_DESC,I.PROD_TYPE_DESC,"
+ "I.ADDONES_INFO,I.INSTALL_DATE,I.CUST_BRAND_DESC,O.CUST_GROUP_DESC,I.PROD_STATUS_DESC,I.MAIL_ADDR,I.BRANCH_NO,I.INSTALL_ADDR,O.NODE_ID,"
+ "O.SERVICE_KEY,O.CUST_SERV_GRADE_DESC,O.PRODUCT_TYPE_NAME,O.RELA_MAN,O.RELA_INFO,O.SOURCE_NUM,O.VERIFY_REASON_ID,O.VERIFY_REASON_NAME,"
+ "O.ACCEPT_STAFF_NAME,O.ACCEPT_ORG_NAME,O.SERVICE_TYPE_DESC,O.URGENCY_GRADE_DESC,C.APPEAL_PROD_NAME,C.APPEAL_REASON_DESC,C.APPEAL_CHILD_DESC,"
+ "C.FOU_GRADE_CATALOG_DESC,O.SEND_TO_ORG_NAME,O.COME_CATEGORY_NAME,O.ACCEPT_CHANNEL_DESC,C.OUTLETS_NAME,C.ACCEPT_CONTENT,O.COMMENTS,W.MONTH_FLAG,"
+ "C.APPEAL_CHILD,W.REGION_ID FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK O,CC_SERVICE_CONTENT_ASK C,CC_ORDER_CUST_INFO I WHERE "
+ "W.SERVICE_ORDER_ID=O.SERVICE_ORDER_ID AND W.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND O.CUST_GUID=I.CUST_GUID AND O.SERVICE_DATE=3 "
+ "AND W.SHEET_TYPE=720130028 AND W.RECEIVE_ORG_ID='362813'AND W.LOCK_FLAG IN(0,1) AND W.WORK_SHEET_ID=? ORDER BY W.CREAT_DATE DESC";

	private String sqlFileinfo = 
"SELECT R.OLD_FILE_NAME,'FTP_HOST' FTP_IP,CONCAT('FTP://FTP_HOST:FTP_PORT/',S.FTP_FILE_DIR) FTP_ADDRESS FROM CC_FILE_RELATING R,CC_FTP_SERV_INFO S "
+ "WHERE R.FTP_GUID=S.FTP_GUID AND R.SERVICE_ORDER_ID=? UNION ALL SELECT SUBSTR(R.OLD_FILE_NAME,INSTR(R.OLD_FILE_NAME,',')+1),'132.228.218.165',"
+ "LTRIM(SUBSTR(R.OLD_FILE_NAME,1,INSTR(R.OLD_FILE_NAME,',')-1)) FROM CC_FILE_RELATING R WHERE R.OLD_FILE_NAME LIKE'%SERVLET%' AND R.SERVICE_ORDER_ID=?";

	private String sqlWaterinfo = 
"SELECT WATER_INFO FROM(SELECT CONCAT('到达时间：', DATE_FORMAT(W.LOCK_DATE,'%Y-%m-%d %H:%i:%s'), '|完成时间：', DATE_FORMAT(W.RESPOND_DATE,'%Y-%m-%d %H:%i:%s'), "
+ "'|处理员工：', W.DEAL_STAFF_NAME, '|处理部门：', W.DEAL_ORG_NAME, char(13), char(10), '处理内容：', W.DEAL_CONTENT) WATER_INFO FROM CC_WORK_SHEET W WHERE "
+ "W.SHEET_STATU IN(700000047,720130036) AND W.RESPOND_DATE IS NOT NULL AND W.SERVICE_ORDER_ID=? ORDER BY W.RESPOND_DATE ASC) AS RT UNION ALL SELECT CONCAT('到达时间："
+ "', DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s'), '|协查时间：', DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s'), '|协查员工：', W.RETURN_STAFF_NAME, '|协查部门："
+ "', W.RETURN_ORG_NAME, char(13), char(10), '协查要求：', W.DEAL_REQUIRE) WATER_INFO FROM CC_WORK_SHEET W WHERE W.SHEET_TYPE=720130028 AND W.WORK_SHEET_ID=?";
	
	private static final String WORK_SHEET_ID = "WORK_SHEET_ID";
	private static final String CREAT_DATE = "CREAT_DATE";
	private static final String ADDRESS_IP = "ADDRESS_IP";
	private static final String METHOD = "METHOD";
	private static final String LOGTYPE = "interface";
	private static final String RECEPT_BILL = "itsmReceptBill";
	private static final String HASTEN_BILL = "itsmHastenBill";
	private static final String CANCEL_BILL = "itsmCancelBill";
	private static final String SUCCESS_CODE = "SUCCESS";
	private static final String ERROR_CODE = "ERROR";
	private static final String CONFIG_MSG = "没有找到cc_address_itconfig配置";
	private static final String END_XML = "</ClogOrder></INPUT_XMLDATA></IDA_SVR_COMPLANTBILL></SERVICE>";
	private static final String RETURN_CODE = "/SERVICE/IDA_SVR_COMPLANTBILL/INPUT_XMLDATA/ClogOrder/ReturnCode";

    /**
     * ITSM网管系统接收10000号工单接口
     * @param serviceOrderId
     * @return
     */
	@SuppressWarnings("rawtypes")
	public boolean executeXML(String serviceOrderId) {
		log.info(LOGTYPE, new LogBean(RECEPT_BILL, serviceOrderId, null, serviceOrderId, null));
		List listWorksheetinfo = this.jdbcTemplate.queryForList(this.sqlWorksheetinfo, serviceOrderId);
		logger.info("listWorksheetinfo：{}", listWorksheetinfo);
		if (listWorksheetinfo.isEmpty()) {
			logger.info("没有找到此服务单");
			return false;
		}
		Map map = pubFunc.getIntfMap(RECEPT_BILL);
		if (null == map) {
			log.info(CONFIG_MSG);
			return false;
		}
		String urlStr = map.get(ADDRESS_IP).toString();
		String nameSpaceUri = map.get("REMARK").toString();
		String method = map.get(METHOD).toString();
		
		Map resultWorksheetinfo = ((Map) listWorksheetinfo.get(0));
		String workSheetId = resultWorksheetinfo.get(WORK_SHEET_ID).toString();
		int monthFlag = returnZero(resultWorksheetinfo.get("MONTH_FLAG"));
		int lockFlag = returnZero(resultWorksheetinfo.get("LOCK_FLAG"));
		if (lockFlag == 0) {
			int regionId = returnZero(resultWorksheetinfo.get("REGION_ID"));
			workSheetBusi.fetchWorkSheet(workSheetId + "@JS15301588119", regionId, monthFlag);
		}
		
		StringBuilder xml = new StringBuilder();
		xml.append(returnHead("receptBill"));
		xml.append(returnStr(WORK_SHEET_ID, workSheetId));
		xml.append(returnStr(resultWorksheetinfo, "PROD_NUM"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_DATE"));
		xml.append(returnStr(resultWorksheetinfo, CREAT_DATE));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_COME_FROM_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "DEAL_LIMIT_TIME"));
		xml.append(returnStr(resultWorksheetinfo, "SHEET_STATU_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_COUNT"));
		xml.append(returnStr(resultWorksheetinfo, "RETURN_STAFF_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "SHEET_TYPE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "REGION_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ID_CARD"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_TYPE_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_SERV_GRADE_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "POST_CODE"));
		xml.append(returnStr(resultWorksheetinfo, "TRADE_TYPE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "PROD_TYPE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "ADDONES_INFO"));
		xml.append(returnStr(resultWorksheetinfo, "INSTALL_DATE"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_BRAND_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_GROUP_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "PROD_STATUS_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "MAIL_ADDR"));
		xml.append(returnStr(resultWorksheetinfo, "BRANCH_NO"));
		xml.append(returnStr(resultWorksheetinfo, "INSTALL_ADDR"));
		xml.append(returnStr(resultWorksheetinfo, "NODE_ID"));
		xml.append(returnStr(resultWorksheetinfo, "SERVICE_KEY"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_SERV_GRADE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "PRODUCT_TYPE_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "RELA_MAN"));
		xml.append(returnStr(resultWorksheetinfo, "RELA_INFO"));
		xml.append(returnStr(resultWorksheetinfo, "SOURCE_NUM"));
		xml.append(returnStr(resultWorksheetinfo, "VERIFY_REASON_ID"));
		xml.append(returnStr(resultWorksheetinfo, "VERIFY_REASON_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_STAFF_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_ORG_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "SERVICE_TYPE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "URGENCY_GRADE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "APPEAL_PROD_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "APPEAL_REASON_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "APPEAL_CHILD_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "FOU_GRADE_CATALOG_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "SEND_TO_ORG_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "COME_CATEGORY_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_CHANNEL_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "OUTLETS_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_CONTENT"));
		xml.append(returnStr(resultWorksheetinfo, "COMMENTS"));
		xml.append("<ATTATCHMENTS>");
		String configJson = ftpFeign.getSheetFtpConfig();
		String ftpIp = this.getFtpConfig(configJson, "ftpHost");
		String port = this.getFtpConfig(configJson, "port");
		List listFileinfo = this.jdbcTemplate.queryForList(this.sqlFileinfo, serviceOrderId, serviceOrderId);
		if (!listFileinfo.isEmpty()) {
			int countFileinfo = listFileinfo.size();
			Map resultFileinfo = null;
			for (int i = 0; i < countFileinfo; i++) {
				resultFileinfo = ((Map) listFileinfo.get(i));
				xml.append("<ATTATCHMENT>");
				xml.append(returnStr(resultFileinfo, "OLD_FILE_NAME"));
				xml.append(returnStr(resultFileinfo, "FTP_IP").replace("FTP_HOST", ftpIp));
				xml.append(returnStr(resultFileinfo, "FTP_ADDRESS").replace("FTP_HOST:FTP_PORT", ftpIp+":"+port));
				xml.append("</ATTATCHMENT>");
			}
		}
		xml.append("</ATTATCHMENTS>");
		xml.append("<WATER_INFO><![CDATA[");
		List listWaterinfo = this.jdbcTemplate.queryForList(this.sqlWaterinfo, serviceOrderId, workSheetId);
		if (!listWaterinfo.isEmpty()) {
			int countWaterinfo = listWaterinfo.size();
			Map resultWaterinfo = null;
			for (int i = 0; i < countWaterinfo; i++) {
				resultWaterinfo = ((Map) listWaterinfo.get(i));
				for (int j = 0; j < 80; j++) {
					xml.append("-");
				}
				xml.append("\n");
				xml.append(returnEmpty(resultWaterinfo.get("WATER_INFO")));
				xml.append("\n");
			}
		}
		xml.append("]]></WATER_INFO>");
		xml.append("<IS_PRE_ASSESS>0</IS_PRE_ASSESS>");
		xml.append(END_XML);
		
		IntfLog intfLog = new IntfLog();
		intfLog.setServOrderId(serviceOrderId);
		intfLog.setActionFlag("out");
		intfLog.setActionResult("-2");
		intfLog.setSystem(RECEPT_BILL);
		intfLog.setInMsg(xml.toString());
		
		String res = "";
		long startTime = System.currentTimeMillis();
		boolean resultFlag = false;
		try {
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setOperationName(new QName(nameSpaceUri, method));
			call.setTargetEndpointAddress(new java.net.URL(urlStr));
			call.addParameter("Strparamxml", Constants.XSD_STRING, ParameterMode.IN);
			call.setReturnType(Constants.XSD_STRING);

			res = (String) call.invoke(new Object[] { xml.toString() });
			intfLog.setOutMsg(res);
			
			Document doc = DocumentHelper.parseText(res);
			String returnCode = doc.selectSingleNode(RETURN_CODE).getText();
			if ("0".equals(returnCode)) {
				resultFlag = true;
				log.info(LOGTYPE, new LogBean(RECEPT_BILL, serviceOrderId, SUCCESS_CODE, xml.toString(), res));
				intfLog.setActionResult("1");
			} else {
				log.info(LOGTYPE, new LogBean(RECEPT_BILL, serviceOrderId, ERROR_CODE, xml.toString(), res));
				intfLog.setActionResult("0");
			}
		} catch (Exception e) {
			logger.error("Exception：{}", e.getMessage(), e);
			intfLog.setOutMsg(e.getMessage());
		} finally {
			logger.info("接口请求地址：{}\n请求内容：{}\n接口返回：{}\n请求耗时：{}",urlStr,xml,res,(System.currentTimeMillis()-startTime));
			pubFunc.saveYNSJIntfLog(intfLog);
		}
		return resultFlag;
	}
	
	private String getFtpConfig(String configJson, String configName) {
		try {
			com.alibaba.fastjson.JSONObject json = JSON.parseObject(configJson);
			if(!json.isEmpty() && json.containsKey(configName)) {
				return json.getString(configName);
			}
		}
		catch(Exception e) {
			logger.error("getFtpConfig error: {}", e.getMessage(), e);
		}
		return "";
	}

    /**
     * 10000号调用ITSM服务进行工单催单接口
     * 
     * @param hastenSheetInfo
     * @return
     */
	@SuppressWarnings("rawtypes")
	public boolean executeForHASTEN(HastenSheetInfo hastenSheetInfo) {
		String workSheetId = hastenSheetInfo.getWorkSheetId();
		log.info(LOGTYPE, new LogBean(HASTEN_BILL, workSheetId, null, workSheetId, null));
		IntfLog intfLog = new IntfLog();
		intfLog.setServOrderId(workSheetId);
		intfLog.setInMsg(workSheetId);
		intfLog.setActionFlag("out");
		intfLog.setActionResult("-2");
		intfLog.setSystem(HASTEN_BILL);
		TsmStaff staff = this.pubFunc.getLogonStaff();
		String info = "催单部门:" + staff.getOrgName() + "; 催单员工联系电话:" + staff.getRelaPhone() + "\n催单内容：" + hastenSheetInfo.getHastenInfo();
		StringBuilder xml = new StringBuilder();
		xml.append(returnHead("hastenBill"));
		xml.append(returnStr(WORK_SHEET_ID, workSheetId));
		xml.append(returnStr("SEND_STAFF_NAME", staff.getName()));
		xml.append(returnStr(CREAT_DATE, hastenSheetInfo.getCreatdate()));
		xml.append(returnStr("HASTEN_REASON_DESC", hastenSheetInfo.getHastenReasonDesc()));
		xml.append(returnStr("HASTEN_INFO", info));
		xml.append(END_XML);
		Map map = pubFunc.getIntfMap(HASTEN_BILL);
		if (null == map) {
			log.info(LOGTYPE, new LogBean(HASTEN_BILL, workSheetId, ERROR_CODE, null, CONFIG_MSG));
			intfLog.setOutMsg(CONFIG_MSG);
			pubFunc.saveYNSJIntfLog(intfLog);
			return false;
		}
		intfLog.setInMsg(xml.toString());
		Service service = new Service();
		try {
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(map.get(ADDRESS_IP).toString());
			call.setOperationName(map.get(METHOD).toString());
			String res = (String) call.invoke(new Object[] { xml.toString() });
			intfLog.setOutMsg(res);
			Document doc = DocumentHelper.parseText(res);
			String returnCode = doc.selectSingleNode(RETURN_CODE).getText();
			if ("0".equals(returnCode)) {
				log.info(LOGTYPE, new LogBean(HASTEN_BILL, workSheetId, SUCCESS_CODE, xml.toString(), res));
				intfLog.setActionResult("1");
				pubFunc.saveYNSJIntfLog(intfLog);
				return true;
			} else {
				log.info(LOGTYPE, new LogBean(HASTEN_BILL, workSheetId, ERROR_CODE, xml.toString(), res));
				intfLog.setActionResult("0");
				pubFunc.saveYNSJIntfLog(intfLog);
				return false;
			}
		} catch (Exception e) {
			log.info(LOGTYPE, new LogBean(HASTEN_BILL, workSheetId, ERROR_CODE, xml.toString(), e.getMessage()));
			intfLog.setOutMsg(e.getMessage());
			pubFunc.saveYNSJIntfLog(intfLog);
			return false;
		}
	}

    /**
     * 10000号系统调用ITSM系统撤单接口
     * 
     * @param workSheetId,cancelFlag(1、工单追回，2、工单竣工)
     * @return
     */
	@SuppressWarnings("rawtypes")
	public boolean executeForCANCEL(String workSheetId, String cancelFlag) {
		log.info(LOGTYPE, new LogBean(CANCEL_BILL, workSheetId, null, workSheetId + "|" + cancelFlag, null));
		IntfLog intfLog = new IntfLog();
		intfLog.setServOrderId(workSheetId);
		intfLog.setInMsg(workSheetId + "|" + cancelFlag);
		intfLog.setActionFlag("out");
		intfLog.setActionResult("-2");
		intfLog.setSystem(CANCEL_BILL);
		String cancelTime = this.pubFunc.getSysDate();
		String cancelMessage = "";
		if ("1".equals(cancelFlag)) {
			cancelMessage = "工单追回";
		} else {
			cancelMessage = "工单竣工";
		}
		StringBuilder xml = new StringBuilder();
		xml.append(returnHead("CancelBill"));
		xml.append(returnStr(WORK_SHEET_ID, workSheetId));
		xml.append(returnStr("CANCEL_FLAG", cancelFlag));
		xml.append(returnStr("CANCEL_DATE", cancelTime));
		xml.append(returnStr("CANCEL_MESSAGE", cancelMessage));
		xml.append(END_XML);
		Map map = pubFunc.getIntfMap(CANCEL_BILL);
		if (null == map) {
			log.info(LOGTYPE, new LogBean(CANCEL_BILL, workSheetId, ERROR_CODE, null, CONFIG_MSG));
			intfLog.setOutMsg(CONFIG_MSG);
			pubFunc.saveYNSJIntfLog(intfLog);
			return false;
		}
		intfLog.setInMsg(xml.toString());
		Service service = new Service();
		try {
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(map.get(ADDRESS_IP).toString());
			call.setOperationName(map.get(METHOD).toString());
			String res = (String) call.invoke(new Object[] { xml.toString() });
			intfLog.setOutMsg(res);
			Document doc = DocumentHelper.parseText(res);
			String returnCode = doc.selectSingleNode(RETURN_CODE).getText();
			if ("0".equals(returnCode)) {
				log.info(LOGTYPE, new LogBean(CANCEL_BILL, workSheetId, SUCCESS_CODE, xml.toString(), res));
				intfLog.setActionResult("1");
				pubFunc.saveYNSJIntfLog(intfLog);
				return true;
			} else {
				log.info(LOGTYPE, new LogBean(CANCEL_BILL, workSheetId, ERROR_CODE, xml.toString(), res));
				intfLog.setActionResult("0");
				pubFunc.saveYNSJIntfLog(intfLog);
				return false;
			}
		} catch (Exception e) {
			log.info(LOGTYPE, new LogBean(CANCEL_BILL, workSheetId, ERROR_CODE, xml.toString(), e.getMessage()));
			intfLog.setOutMsg(e.getMessage());
			pubFunc.saveYNSJIntfLog(intfLog);
			return false;
		}
	}

	/**
	 * 获取满足能够发送到ITSM系统的任务单
	 * 
	 * @param
	 * @return list
	 */
	public GridDataInfo getITSMList(String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String begion = json.optString("begion");
		String serviceOrderId = json.optString("serviceOrderId");
		String sql = 
"SELECT W.SERVICE_ORDER_ID,W.WORK_SHEET_ID,O.SERVICE_TYPE_DESC,W.REGION_NAME,O.PROD_NUM,DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s')CREAT_DATE FROM "
+ "CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK O,CC_SERVICE_CONTENT_ASK C WHERE W.SERVICE_ORDER_ID=O.SERVICE_ORDER_ID AND "
+ "W.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND O.SERVICE_DATE=3 AND W.SHEET_TYPE=720130028 AND W.RECEIVE_ORG_ID='362813'AND W.LOCK_FLAG IN(0,1)";
		if (!"".equals(serviceOrderId)) {
			sql += "AND W.SERVICE_ORDER_ID = '" + serviceOrderId.trim() + "'";
		}
		return dbgridDataPub.getResult(sql, Integer.parseInt(begion), "ORDER BY W.CREAT_DATE", "");
	}

    /**
     * ITSM网管系统接收10000号工单接口(重复发送)
     * 
     * @param workSheetId
     * @return
     */
	@SuppressWarnings("rawtypes")
	public String[] reExecuteXML(String workSheetId) {
		String[] result = new String[3];
		StringBuilder sb = new StringBuilder();
		sb.append(">>>调用WebService接口executeXML方法--开始--");
		sb.append("\n\r");
		List listWorksheetinfo = this.jdbcTemplate.queryForList(this.sqlWorksheetinfoBySheetId, workSheetId);
		if (listWorksheetinfo.isEmpty()) {
			sb.append(">>>没有找到此工单" + workSheetId);
			sb.append("\n\r");
			sb.append(">>>调用WebService接口executeXML方法--结束--");
			sb.append("\n\r");
			result[2] = sb.toString();
			return result;
		}
		Map resultWorksheetinfo = ((Map) listWorksheetinfo.get(0));
		String serviceOrderId = resultWorksheetinfo.get("SERVICE_ORDER_ID").toString();
		StringBuilder xml = new StringBuilder();
        xml.append(returnHead("receptBill"));
		xml.append(returnStr(WORK_SHEET_ID, workSheetId));
		xml.append(returnStr(resultWorksheetinfo, "PROD_NUM"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_DATE"));
		xml.append(returnStr(resultWorksheetinfo, CREAT_DATE));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_COME_FROM_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "DEAL_LIMIT_TIME"));
		xml.append(returnStr(resultWorksheetinfo, "SHEET_STATU_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_COUNT"));
		xml.append(returnStr(resultWorksheetinfo, "RETURN_STAFF_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "SHEET_TYPE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "REGION_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ID_CARD"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_TYPE_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_SERV_GRADE_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "POST_CODE"));
		xml.append(returnStr(resultWorksheetinfo, "TRADE_TYPE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "PROD_TYPE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "ADDONES_INFO"));
		xml.append(returnStr(resultWorksheetinfo, "INSTALL_DATE"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_BRAND_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_GROUP_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "PROD_STATUS_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "MAIL_ADDR"));
		xml.append(returnStr(resultWorksheetinfo, "BRANCH_NO"));
		xml.append(returnStr(resultWorksheetinfo, "INSTALL_ADDR"));
		xml.append(returnStr(resultWorksheetinfo, "NODE_ID"));
		xml.append(returnStr(resultWorksheetinfo, "SERVICE_KEY"));
		xml.append(returnStr(resultWorksheetinfo, "CUST_SERV_GRADE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "PRODUCT_TYPE_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "RELA_MAN"));
		xml.append(returnStr(resultWorksheetinfo, "RELA_INFO"));
		xml.append(returnStr(resultWorksheetinfo, "SOURCE_NUM"));
		xml.append(returnStr(resultWorksheetinfo, "VERIFY_REASON_ID"));
		xml.append(returnStr(resultWorksheetinfo, "VERIFY_REASON_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_STAFF_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_ORG_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "SERVICE_TYPE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "URGENCY_GRADE_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "APPEAL_PROD_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "APPEAL_REASON_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "APPEAL_CHILD_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "FOU_GRADE_CATALOG_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "SEND_TO_ORG_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "COME_CATEGORY_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_CHANNEL_DESC"));
		xml.append(returnStr(resultWorksheetinfo, "OUTLETS_NAME"));
		xml.append(returnStr(resultWorksheetinfo, "ACCEPT_CONTENT"));
		xml.append(returnStr(resultWorksheetinfo, "COMMENTS"));
		xml.append("<ATTATCHMENTS>");
		String configJson = ftpFeign.getSheetFtpConfig();
		String ftpIp = this.getFtpConfig(configJson, "ftpHost");
		String port = this.getFtpConfig(configJson, "port");
		List listFileinfo = this.jdbcTemplate.queryForList(this.sqlFileinfo, serviceOrderId, serviceOrderId);
		if (!listFileinfo.isEmpty()) {
			int countFileinfo = listFileinfo.size();
			Map resultFileinfo = null;
			for (int i = 0; i < countFileinfo; i++) {
				resultFileinfo = ((Map) listFileinfo.get(i));
				xml.append("<ATTATCHMENT>");
				xml.append(returnStr(resultFileinfo, "OLD_FILE_NAME"));
				xml.append(returnStr(resultFileinfo, "FTP_IP").replace("FTP_HOST", ftpIp));
				xml.append(returnStr(resultFileinfo, "FTP_ADDRESS").replace("FTP_HOST_IP:FTP_PORT", ftpIp+":"+port));
				xml.append("</ATTATCHMENT>");
			}
		}
		xml.append("</ATTATCHMENTS>");
		xml.append("<WATER_INFO><![CDATA[");
		List listWaterinfo = this.jdbcTemplate.queryForList(this.sqlWaterinfo, serviceOrderId, workSheetId);
		if (!listWaterinfo.isEmpty()) {
			int countWaterinfo = listWaterinfo.size();
			Map resultWaterinfo = null;
			for (int i = 0; i < countWaterinfo; i++) {
				resultWaterinfo = ((Map) listWaterinfo.get(i));
				for (int j = 0; j < 80; j++) {
					xml.append("-");
				}
				xml.append("\n");
				xml.append(returnEmpty(resultWaterinfo.get("WATER_INFO")));
				xml.append("\n");
			}
		}
		xml.append("]]></WATER_INFO>");
		xml.append("<IS_PRE_ASSESS>0</IS_PRE_ASSESS>");
		xml.append(END_XML);
		result[0] = xml.toString();
		Map map = pubFunc.getIntfMap(RECEPT_BILL);
		if (null == map) {
			sb.append(">>>没有找到cc_address_itconfig表配置");
			sb.append("\n\r");
			sb.append(">>>调用WebService接口executeXML方法--结束--");
			sb.append("\n\r");
			result[2] = sb.toString();
			return result;
		}
		String urlStr = map.get(ADDRESS_IP).toString();
		String nameSpaceUri = map.get("REMARK").toString();
		String method = map.get(METHOD).toString();
		
        Service service = new Service();
        try {
            Call call = (Call) service.createCall();
            call.setOperationName(new QName(nameSpaceUri, method));
			call.setTargetEndpointAddress(new java.net.URL(urlStr));
			call.addParameter("Strparamxml", Constants.XSD_STRING,ParameterMode.IN);
			call.setReturnType(Constants.XSD_STRING);
            String res = (String) call.invoke(new Object[] {xml.toString()});
            result[1] = res;
            Document doc = DocumentHelper.parseText(res);
            String returnCode = doc.selectSingleNode(RETURN_CODE).getText();
            if ("0".equals(returnCode)) {
                sb.append(">>>调用WebService接口executeXML方法--服务端返回成功--");
                sb.append("\n\r");
                result[2] = sb.toString();
                return result;
            } else if ("1".equals(returnCode)) {
                sb.append(">>>调用WebService接口executeXML方法--服务端返回失败--");
                sb.append("\n\r");
                result[2] = sb.toString();
                return result;
            } else {
                sb.append(">>>调用WebService接口executeXML方法--服务端返回异常--");
                sb.append("\n\r");
                result[2] = sb.toString();
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            sb.append(">>>调用WebService接口executeXML方法--调用失败--");
            sb.append("\n\r");
            sb.append(e.getMessage());
            sb.append("\n\r");
            result[2] = sb.toString();
            return result;
        }
    }

	private String returnHead(String str) {
		return "<?xml version=\"1.0\" encoding=\"GBK\"?><SERVICE><IDA_SVR_COMPLANTBILL><CALL_METHOD>" + str + "</CALL_METHOD><INPUT_XMLDATA><ClogOrder>";
	}

	private int returnZero(Object obj) {
		return Integer.parseInt(obj == null ? "0" : obj.toString());
	}

	private String returnStr(String xml, String str) {
		return "<" + xml + "><![CDATA[" + str + "]]></" + xml + ">";
	}

	@SuppressWarnings("rawtypes")
	private String returnStr(Map map, String xml) {
		return "<" + xml + "><![CDATA[" + returnEmpty(map.get(xml)) + "]]></" + xml + ">";
	}

	private String returnEmpty(Object obj) {
		return obj == null ? "" : obj.toString();
	}
}