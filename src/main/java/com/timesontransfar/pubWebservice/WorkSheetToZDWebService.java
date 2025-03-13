package com.timesontransfar.pubWebservice;

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

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.pojo.IntfLog;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.transfar.common.log.CustomLogger;
import com.transfar.common.log.LogBean;

@Component(value = "workSheetToZDWebService")
public class WorkSheetToZDWebService {
	protected Logger logger = LoggerFactory.getLogger(WorkSheetToZDWebService.class);
	
	@Autowired
	private CustomLogger log;
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
	private PubFunc pubFunc;
	@Autowired
	private IworkSheetBusi workSheetBusi;

	private static final String LOGTYPE = "interface";
	private static final String ASIGSERVICE = "zdAsigService";

	private String sqlWorksheetinfo = "SELECT W.MONTH_FLAG,W.REGION_ID,W.WORK_SHEET_ID,W.DEAL_REQUIRE,O.PROD_NUM,O.RELA_INFO,O.RELA_MAN,"
			+ "C.ACCEPT_CONTENT FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK O,CC_SERVICE_CONTENT_ASK C WHERE W.SERVICE_ORDER_ID=O.SERVICE_ORDER_ID AND "
			+ "W.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND O.SERVICE_DATE=3 AND W.SHEET_TYPE=720130028 AND C.APPEAL_REASON_ID=23002846 AND "
			+ "W.RECEIVE_ORG_ID='282'AND W.LOCK_FLAG=0 AND W.SERVICE_ORDER_ID=? ORDER BY W.CREAT_DATE DESC";

	/**
	 * 综调系统接收10000号工单接口
	 * 
	 * @param serviceOrderId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public boolean executeXML(String serviceOrderId) {
		log.info(LOGTYPE, new LogBean(ASIGSERVICE, serviceOrderId, null, serviceOrderId, null));
		List listWorksheetinfo = this.jdbcTemplate.queryForList(this.sqlWorksheetinfo, serviceOrderId);
		logger.info("listWorksheetinfo：{}", listWorksheetinfo);
		if (listWorksheetinfo.isEmpty()) {
			logger.info("目录不是通信大数据行程卡");
			return false;
		}
		Map map = pubFunc.getIntfMap(ASIGSERVICE);
		if (null == map) {
			logger.info("没有找到cc_address_itconfig配置");
			return false;
		}
		String urlStr = map.get("ADDRESS_IP").toString();
		
		Map resultWorksheetinfo = ((Map) listWorksheetinfo.get(0));
		int monthFlag = returnZero(resultWorksheetinfo.get("MONTH_FLAG").toString());
		int regionId = returnZero(resultWorksheetinfo.get("REGION_ID").toString());
		String workSheetId = resultWorksheetinfo.get("WORK_SHEET_ID").toString();
		String dealRequire = resultWorksheetinfo.get("DEAL_REQUIRE").toString();
		String prodNum = resultWorksheetinfo.get("PROD_NUM").toString();
		String relaInfo = resultWorksheetinfo.get("RELA_INFO").toString();
		String relaMan = resultWorksheetinfo.get("RELA_MAN").toString();
		String acceptContent = resultWorksheetinfo.get("ACCEPT_CONTENT").toString();
		
		StringBuilder xml = new StringBuilder();
		xml.append("<?xml version=\"1.0\" encoding=\"GBK\"?>");
		xml.append("<root>");
		xml.append("<functionCode>userFaultAutoStartFlow</functionCode>");
		xml.append("<paramDoc>");
		xml.append(returnStr("faultCode", prodNum));
		xml.append("<businessCode>IDB_SA_XCM_GZ</businessCode>");
		xml.append("<areaID>2</areaID>");
		xml.append("<complaintSrc>202</complaintSrc>");
		xml.append("<complaintCause>XCMGZ001</complaintCause>");
		xml.append(returnStr("complaintInfo", acceptContent));
		xml.append(returnStr("contactor", relaMan));
		xml.append(returnStr("contactPhone", relaInfo));
		xml.append(returnStr("remark", dealRequire));
		xml.append(returnStr("alarmID", workSheetId));
		xml.append("</paramDoc>");
		xml.append("</root>");
		
		IntfLog intfLog = new IntfLog();
		intfLog.setServOrderId(workSheetId);
		intfLog.setActionFlag("out");
		intfLog.setActionResult("-2");
		intfLog.setSystem(ASIGSERVICE);
		intfLog.setInMsg(xml.toString());
		
		String res = "";
		long startTime = System.currentTimeMillis();
		boolean resultFlag = false;
		try {
			Service service = new Service();
			Call call = (Call) service.createCall();
			call.setTargetEndpointAddress(new java.net.URL(urlStr));
			call.setOperationName(new QName("http://service.asip.regaltec.com/", map.get("METHOD").toString()));
			call.setTimeout(Integer.parseInt(map.get("TIME_OUT").toString()));
			call.addParameter("text", Constants.XSD_STRING, ParameterMode.IN);
			call.setReturnType(Constants.XSD_STRING);
			res = (String) call.invoke(new Object[] { xml.toString() });
			intfLog.setOutMsg(res);
			
			Document doc = DocumentHelper.parseText(res);
			String returnCodeOut = doc.selectSingleNode("/root/returnCode").getText();
			String returnCodeIn = doc.selectSingleNode("/root/resultDoc/returnCode").getText();
			if ("0".equals(returnCodeOut) && "0".equals(returnCodeIn)) {
				resultFlag = true;
				log.info(LOGTYPE, new LogBean(ASIGSERVICE, workSheetId, "0", xml.toString(), res));
				intfLog.setActionResult("1");
			} else {
				log.info(LOGTYPE, new LogBean(ASIGSERVICE, workSheetId, "1", xml.toString(), res));
				intfLog.setActionResult("0");
			}
		} catch (Exception e) {
			logger.error("Exception：{}", e.getMessage(), e);
			intfLog.setOutMsg(e.getMessage());
		} finally {
			logger.info("接口请求地址：{}\n请求内容：{}\n接口返回：{}\n请求耗时：{}",urlStr,xml,res,(System.currentTimeMillis()-startTime));
			pubFunc.saveYNSJIntfLog(intfLog);
		}
		if(resultFlag) {
			workSheetBusi.fetchWorkSheet(workSheetId + "@NOC001", regionId, monthFlag);
		}
		return resultFlag;
	}

	private int returnZero(Object obj) {
		return Integer.parseInt(obj == null ? "0" : obj.toString());
	}

	private String returnStr(String xml, String str) {
		return "<" + xml + "><![CDATA[" + str + "]]></" + xml + ">";
	}
}