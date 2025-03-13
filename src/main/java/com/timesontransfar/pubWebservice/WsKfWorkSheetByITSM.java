package com.timesontransfar.pubWebservice;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.pojo.IntfLog;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.transfar.common.log.CustomLogger;
import com.transfar.common.log.LogBean;

@Component
public class WsKfWorkSheetByITSM {
	
	@Autowired
	private CustomLogger logger;
	
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	
	@Autowired
	private IworkSheetBusi workSheetBusi;
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
    @Autowired
    private PubFunc pubFun;

	private static final String SQL_DEAL = 
"SELECT W.SERVICE_ORDER_ID,W.REGION_ID,W.LOCK_FLAG,W.MONTH_FLAG FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK O,CC_SERVICE_CONTENT_ASK C WHERE "
+ "W.SERVICE_ORDER_ID=O.SERVICE_ORDER_ID AND W.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND O.SERVICE_DATE=3 AND W.SHEET_TYPE=720130028 AND "
+ "W.RECEIVE_ORG_ID='362813'AND W.LOCK_FLAG IN(0,1)AND W.WORK_SHEET_ID=?";

	private static final String SQL_FTP = 
"INSERT INTO CC_FILE_RELATING(FTP_GUID,SERVICE_ORDER_ID,REGION_ID,OLD_FILE_NAME,UPLOAD_STAFF_ID,UPLOAD_STAFF_NAME,FTP_DATE,MONTH_FLAG)VALUES"
+ "(replace(UUID(), '-', ''),?,?,?,?,?,NOW(),DATE_FORMAT(NOW(),'%m')+0)";

	/**
	 * ITSM系统向10000号写回单接口
	 * 
	 * @param xml 参数，xml格式的字符串
	 * @return 查询结果
	 */
	@SuppressWarnings("rawtypes")
	public String executeForRevist(String xml) {
		String workSheetId = "";
		String resultXml = "";
		try {
			Document doc = DocumentHelper.parseText(xml);
			workSheetId = getNode(doc, "/SERVICE/IDA_SVR_COMPLANTBILL/INPUT_XMLDATA/ClogOrder/WORK_SHEET_ID");
			String dealContent = getNode(doc, "/SERVICE/IDA_SVR_COMPLANTBILL/INPUT_XMLDATA/ClogOrder/DEAL_CONTENT");
			if (StringUtils.isEmpty(dealContent)) {
				resultXml = buildInfo(xml, workSheetId, -1, "The Request Parameter DEAL_CONTENT Is Null.");
				return resultXml;
			}
			if (dealContent.length() > 666) {
				resultXml = buildInfo(xml, workSheetId, -1, "The Request Parameter DEAL_CONTENT Is Too Long.");
				return resultXml;
			}
			List dealList = jdbcTemplate.queryForList(SQL_DEAL, workSheetId);
			if (dealList.isEmpty()) {
				resultXml = buildInfo(xml, workSheetId, 1, "WORK_SHEET_ID_1 " + workSheetId + " Can't Find.");
				return resultXml;
			}
			Map dealMap = ((Map) dealList.get(0));
			int lockFlag = this.getIntByKey(dealMap, "LOCK_FLAG");
			int regionId = this.getIntByKey(dealMap, "REGION_ID");
			Integer monthFlag = this.getIntByKey(dealMap, "MONTH_FLAG");
			if (lockFlag == 2) {
				resultXml = buildInfo(xml, workSheetId, 1, "WORK_SHEET_ID_2 " + workSheetId + " Is Finished.");
				return resultXml;
			} else if (lockFlag == 9) {
				resultXml = buildInfo(xml, workSheetId, 1, "WORK_SHEET_ID_3 " + workSheetId + " Is Been Replevy.");
				return resultXml;
			} else if (lockFlag == 0) {
				workSheetBusi.fetchWorkSheet(workSheetId + "@JS15301588119", regionId, monthFlag);
			}
			String serviceOrderId = dealMap.get("SERVICE_ORDER_ID").toString();
			String res = tsWorkSheetDeal.snxcSumbitOrgDeal(workSheetId, regionId, monthFlag, dealContent);
			if ("SUCCESS".equals(res)) {
				List attatchments = doc.selectNodes("/SERVICE/IDA_SVR_COMPLANTBILL/INPUT_XMLDATA/ClogOrder/ATTATCHMENTS/ATTATCHMENT");
				Iterator iter = attatchments.iterator();
				while (iter.hasNext()) {
					Element element = (Element) iter.next();
					String name = element.elementText("NAME");
					String url = element.elementText("URL");
					jdbcTemplate.update(SQL_FTP, serviceOrderId, regionId, url + "," + name, 2604457, "企信部");
				}
				resultXml = buildInfo(xml, workSheetId, 0, "success");
				return resultXml;
			} else {
				resultXml = buildInfo(xml, workSheetId, 1, res);
				return resultXml;
			}
		} catch (Exception e) {
			e.printStackTrace();
			resultXml = buildInfo(xml, workSheetId, -1, "Exception: " + e.getMessage());
			return resultXml;
		} finally {
			logger.info("interface", new LogBean("WsKfWorkSheetByITSM", null, null, null, resultXml), xml);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private int getIntByKey(Map map, String key) {
		return Integer.parseInt(map.get(key) == null ? "0" : map.get(key).toString());
	}

	private String getNode(Document doc, String xmlPath) {
		return doc.selectSingleNode(xmlPath) == null ? "" : doc.selectSingleNode(xmlPath).getText();
	}

	/**
	 * 组装结果字符串
	 * 
	 * @param workSheetId 工单编号
	 * @param returnCode  执行结果
	 * @param message     信息描述
	 * @return 结果字符串
	 */
	private String buildInfo(String xml, String workSheetId, int returnCode, String message) {
		String resultMsg = "<?xml version=\"1.0\" encoding=\"GBK\"?><SERVICE><IDA_SVR_COMPLANTBILL><CALL_METHOD>reVisitResult</CALL_METHOD><INPUT_XMLDATA>"
				+ "<WORK_SHEET_ID>" + workSheetId + "</WORK_SHEET_ID><ReturnCode>" + returnCode
				+ "</ReturnCode><Message>" + message + "</Message></INPUT_XMLDATA></IDA_SVR_COMPLANTBILL></SERVICE>";
		IntfLog log = new IntfLog();
		log.setServOrderId(StringUtils.defaultIfEmpty(workSheetId, "null"));
		log.setInMsg(xml);
		log.setOutMsg(resultMsg);
		log.setActionFlag("in");
		log.setActionResult(returnCode==0 ? "1":"0");
		log.setSystem("ITSM");
		pubFun.saveYNSJIntfLog(log);
		return resultMsg;
	}
}