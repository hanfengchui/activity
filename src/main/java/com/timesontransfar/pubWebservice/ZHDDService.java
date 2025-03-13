package com.timesontransfar.pubWebservice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.transfar.common.log.CustomLogger;
import com.transfar.common.log.LogBean;

@Component
public class ZHDDService {
	private static final Logger logger = LoggerFactory.getLogger(ZHDDService.class);
	
	@Autowired
	private CustomLogger log;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PubFunc pubFunc;
	
	private static final String SQL_ORDERLIST =
"SELECT SERVICE_ORDER_ID,\n" + 
"       SERVICE_TYPE_DESC,\n" + 
"       DATE_FORMAT(ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\n" + 
"       ORDER_STATU_DESC,\n" + 
"       ACCEPT_COME_FROM_DESC,\n" + 
"       ACCEPT_CHANNEL_DESC,\n" + 
"       ACCEPT_CHANNEL_ID\n" + 
"  FROM cc_service_order_ask\n" + 
" WHERE service_type IN (720130000, 700006312, 720200003)\n" + 
"   AND order_statu NOT IN (700000099, 700000100, 720130001, 720130003)\n" + 
"   AND (prod_num = ? OR source_num = ?)\n" + 
"   AND accept_date >= date_sub(?, interval 60 day)\n" + 
"   AND accept_date <= str_to_date(?, '%Y-%m-%d %H:%i:%s')\n" + 
"UNION ALL\n" + 
"SELECT SERVICE_ORDER_ID,\n" + 
"       SERVICE_TYPE_DESC,\n" + 
"       DATE_FORMAT(ACCEPT_DATE, '%Y-%m-%d %H:%i:%s'),\n" + 
"       ORDER_STATU_DESC,\n" + 
"       ACCEPT_COME_FROM_DESC,\n" + 
"       ACCEPT_CHANNEL_DESC,\n" + 
"       ACCEPT_CHANNEL_ID\n" + 
"  FROM cc_service_order_ask_his\n" + 
" WHERE service_type IN (720130000, 700006312, 720200003)\n" + 
"   AND order_statu IN (700000103, 720130010)\n" + 
"   AND (prod_num = ? OR source_num = ?)\n" + 
"   AND accept_date >= date_sub(?, interval 60 day)\n" + 
"   AND accept_date <= str_to_date(?, '%Y-%m-%d %H:%i:%s')";

	private static final String SQL_ORDERINFO =
"SELECT a.SERVICE_ORDER_ID,\n" +
"       a.SERVICE_TYPE_DESC,\n" + 
"       DATE_FORMAT(ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\n" + 
"       ORDER_STATU_DESC,\n" + 
"       ACCEPT_COME_FROM_DESC,\n" + 
"       ACCEPT_CHANNEL_DESC,\n" + 
"       ACCEPT_CHANNEL_ID,\n" + 
"       '' FINISH_DATE,\n" + 
"       REPLACE(a.COMMENTS, ' > ', '-')PHENOMENON,\n" + 
"       ACCEPT_CONTENT,\n" + 
"       '' REASON\n" + 
"  FROM cc_service_order_ask a, cc_service_content_ask b\n" + 
" WHERE a.service_type IN (720130000, 700006312, 720200003)\n" + 
"   AND order_statu NOT IN (700000099, 700000100, 720130001, 720130003)\n" + 
"   AND a.service_order_id = b.service_order_id\n" + 
"   AND a.service_order_id = ?\n" + 
"UNION ALL\n" + 
"SELECT a.SERVICE_ORDER_ID,\n" + 
"       a.SERVICE_TYPE_DESC,\n" + 
"       DATE_FORMAT(ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\n" + 
"       ORDER_STATU_DESC,\n" + 
"       ACCEPT_COME_FROM_DESC,\n" + 
"       ACCEPT_CHANNEL_DESC,\n" + 
"       ACCEPT_CHANNEL_ID,\n" + 
"       DATE_FORMAT(FINISH_DATE, '%Y-%m-%d %H:%i:%s'),\n" + 
"       REPLACE(a.COMMENTS, ' > ', '-'),\n" + 
"       ACCEPT_CONTENT,\n" + 
"       (SELECT REPLACE(n, ' > ', '-') FROM ccs_st_mapping_all_b_new WHERE n_id = c.six_grade_catalog)\n" + 
"  FROM cc_service_order_ask_his a, cc_service_content_ask_his b, cc_service_label_his c\n" + 
" WHERE a.service_type IN (720130000, 700006312, 720200003)\n" + 
"   AND order_statu IN (700000103, 720130010)\n" + 
"   AND a.service_order_id = b.service_order_id\n" + 
"   AND a.order_vesion = b.order_vesion\n" + 
"   AND a.service_order_id = c.service_order_id\n" + 
"   AND a.service_order_id = ?";

	private static final String SQL_SHEETINFO =
"SELECT DEAL_ORG_NAME, RESPOND_DATE, DEAL_CONTENT\n" +
"  FROM (SELECT deal_org_name, DATE_FORMAT(respond_date, '%Y-%m-%d %H:%i:%s') respond_date, deal_content, creat_date\n" + 
"          FROM cc_work_sheet\n" + 
"         WHERE service_type IN (720130000, 700006312, 720200003)\n" + 
"           AND tache_id IN (700000085, 700000086, 700000088, 720130021, 720130023, 720130024, 720130025)\n" + 
"           AND sheet_statu IN (700000047, 720130036)\n" + 
"           AND sheet_type NOT IN (3000035, 3000036, 720130012)\n" + 
"           AND service_order_id = ?\n" + 
"        UNION ALL\n" + 
"        SELECT deal_org_name, DATE_FORMAT(respond_date, '%Y-%m-%d %H:%i:%s'), deal_content, creat_date\n" + 
"          FROM cc_work_sheet_his\n" + 
"         WHERE service_type IN (720130000, 700006312, 720200003)\n" + 
"           AND tache_id IN (700000085, 700000086, 700000088, 720130021, 720130023, 720130024, 720130025)\n" + 
"           AND sheet_statu IN (700000047, 720130036)\n" + 
"           AND sheet_type NOT IN (3000035, 3000036, 720130012)\n" + 
"           AND service_order_id = ?) as r\n" + 
" ORDER BY creat_date";

	public String executeXML(String paramXml) {
		String callMethod = "";
		String rspType = "1";
		String outmsg = "";
		try {
			Document doc = DocumentHelper.parseText(paramXml);
			if(null == doc.selectSingleNode("/SERVICE/CALL_METHOD")) {
				rspType = "3";
				outmsg = wrongResult("3", "获取CALL_METHOD节点失败！");
				return outmsg;
			}
			callMethod = doc.selectSingleNode("/SERVICE/CALL_METHOD").getText();
			if (null == callMethod || callMethod.length() < 1) {
				callMethod = "NONE";
				rspType = "2";
				outmsg = wrongResult("2", "CALL_METHOD不能为空！");
				return outmsg;
			}
			if (callMethod.equals("ORDERLIST")) {//受理单列表
				Map<String, String > resultMap = this.getOrderList(doc);
				rspType = resultMap.get("rspType");
				outmsg = resultMap.get("outmsg");
				return outmsg;
			} else if (callMethod.equals("ORDERINFO")) {//受理单详情
				Map<String, String > resultMap = this.getOrderInfo(doc);
				rspType = resultMap.get("rspType");
				outmsg = resultMap.get("outmsg");
				return outmsg;
			} else {
				rspType = "3";
				outmsg = wrongResult("3", "CALL_METHOD不存在！");
				return outmsg;
			}
		} catch (Exception e) {
			logger.error("{} error:{}", callMethod, e.getMessage(), e);
			rspType = "999";
			outmsg = wrongResult("999", "Exception: " + e.getMessage());
			return wrongResult("999", "接口异常");
		} finally {
			try {
				log.info("interface", new LogBean("ZHDD", null, "1".equals(rspType) ? "SUCCESS" : "ERROR", null, outmsg), "ZHDD 入参: "+ paramXml);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Map<String, String> getOrderList(Document doc) {
		Map<String, String> map = new HashMap<>();
		String rspType = "1";
		String outmsg = "";
		String queryNumber = null;
		if(null == doc.selectSingleNode("/SERVICE/INPUT_XMLDATA/QUERY_NUMBER")) {
			rspType = "3";
			outmsg = wrongResult("3", "获取QUERY_NUMBER节点失败！");
			map.put("rspType", rspType);
			map.put("outmsg", outmsg);
			return map;
		}
		queryNumber = doc.selectSingleNode("/SERVICE/INPUT_XMLDATA/QUERY_NUMBER").getText();
		if (null == queryNumber || queryNumber.length() < 1) {
			rspType = "2";
			outmsg = wrongResult("2", "QUERY_NUMBER不能为空！");
			map.put("rspType", rspType);
			map.put("outmsg", outmsg);
			return map;
		}
		String queryDate = null;
		if(null == doc.selectSingleNode("/SERVICE/INPUT_XMLDATA/QUERY_DATE")) {
			rspType = "3";
			outmsg = wrongResult("3", "获取QUERY_DATE节点失败！");
			map.put("rspType", rspType);
			map.put("outmsg", outmsg);
			return map;
		}
		queryDate = doc.selectSingleNode("/SERVICE/INPUT_XMLDATA/QUERY_DATE").getText();
		if (null == queryDate || queryDate.length() < 1) {
			rspType = "2";
			outmsg = wrongResult("2", "QUERY_DATE不能为空！");
			map.put("rspType", rspType);
			map.put("outmsg", outmsg);
			return map;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			sdf.parse(queryDate);
		} catch (ParseException e) {
			rspType = "2";
			outmsg = wrongResult("3", "QUERY_DATE格式错误！");
			map.put("rspType", rspType);
			map.put("outmsg", outmsg);
			return map;
		}
		outmsg = orderList(queryNumber, queryDate);
		map.put("rspType", rspType);
		map.put("outmsg", outmsg);
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	private Map<String, String> getOrderInfo(Document doc) {
		Map<String, String> map = new HashMap<>();
		String rspType = "1";
		String outmsg = "";
		String serviceOrderId = null;
		if(null == doc.selectSingleNode("/SERVICE/INPUT_XMLDATA/SERVICE_ORDER_ID")) {
			rspType = "3";
			outmsg = wrongResult("3", "获取SERVICE_ORDER_ID节点失败！");
			map.put("rspType", rspType);
			map.put("outmsg", outmsg);
			return map;
		}
		serviceOrderId = doc.selectSingleNode("/SERVICE/INPUT_XMLDATA/SERVICE_ORDER_ID").getText();
		if (null == serviceOrderId || serviceOrderId.length() < 1) {
			rspType = "2";
			outmsg = wrongResult("2", "SERVICE_ORDER_ID不能为空！");
			map.put("rspType", rspType);
			map.put("outmsg", outmsg);
			return map;
		}
		List ol = jdbcTemplate.queryForList(SQL_ORDERINFO, serviceOrderId, serviceOrderId );
		if (ol.isEmpty()) {
			rspType = "2";
			outmsg = wrongResult("2", "受理单" + serviceOrderId + "不存在！");
			map.put("rspType", rspType);
			map.put("outmsg", outmsg);
			return map;
		}
		outmsg = orderInfo(serviceOrderId, ol);
		map.put("rspType", rspType);
		map.put("outmsg", outmsg);
		return map;
	}

	@SuppressWarnings("rawtypes")
	private String orderList(String queryNumber, String queryDate) {
		StringBuilder xml = new StringBuilder("");
		List lists = null;
		lists = jdbcTemplate.queryForList(SQL_ORDERLIST, queryNumber, queryNumber, queryDate, queryDate, queryNumber, queryNumber, queryDate,
				queryDate );
		int tsNum = 0;
		int zxNum = 0;
		for (Object obj : lists) {
			Map list = (Map) obj;
			
			if ("投诉".equals(list.get("SERVICE_TYPE_DESC").toString())) {
				tsNum = tsNum + 1;
			} else {
				zxNum = zxNum + 1;
			}
			xml.append("<LIST>");
			xml.append("<SERVICE_ORDER_ID>" + list.get("SERVICE_ORDER_ID") + "</SERVICE_ORDER_ID>");
			xml.append("<SERVICE_TYPE>" + list.get("SERVICE_TYPE_DESC") + "</SERVICE_TYPE>");
			xml.append("<ACCEPT_DATE><![CDATA[" + list.get("ACCEPT_DATE") + "]]></ACCEPT_DATE>");
			xml.append("<ORDER_STATU>" + list.get("ORDER_STATU_DESC") + "</ORDER_STATU>");
			xml.append("<ACCEPT_COME_FROM>" + list.get("ACCEPT_COME_FROM_DESC") + "</ACCEPT_COME_FROM>");
			xml.append("<ACCEPT_CHANNEL>" + list.get("ACCEPT_CHANNEL_DESC") + "</ACCEPT_CHANNEL>");
			if (pubFunc.isYueJi(Integer.parseInt(list.get("ACCEPT_CHANNEL_ID").toString()))) {
				xml.append("<IS_OVER>是</IS_OVER>");
			} else {
				xml.append("<IS_OVER>否</IS_OVER>");
			}
			xml.append("</LIST>");
		}
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SERVICE><TS_NUM>" + tsNum + "</TS_NUM><ZX_NUM>" + zxNum + "</ZX_NUM><LISTS>" + xml
				+ "</LISTS><RETURN_CODE>1</RETURN_CODE><MESSAGE></MESSAGE></SERVICE>";
	}

	@SuppressWarnings("rawtypes")
	private String orderInfo(String serviceOrderId, List ol) {
		StringBuilder ox = new StringBuilder("");
		Map om = (Map) ol.get(0);
		ox.append("<SERVICE_ORDER_ID>" + om.get("SERVICE_ORDER_ID") + "</SERVICE_ORDER_ID>");
		ox.append("<SERVICE_TYPE>" + om.get("SERVICE_TYPE_DESC") + "</SERVICE_TYPE>");
		ox.append("<ACCEPT_DATE><![CDATA[" + om.get("ACCEPT_DATE") + "]]></ACCEPT_DATE>");
		ox.append("<ORDER_STATU>" + om.get("ORDER_STATU_DESC") + "</ORDER_STATU>");
		ox.append("<ACCEPT_COME_FROM>" + om.get("ACCEPT_COME_FROM_DESC") + "</ACCEPT_COME_FROM>");
		ox.append("<ACCEPT_CHANNEL>" + om.get("ACCEPT_CHANNEL_DESC") + "</ACCEPT_CHANNEL>");
		if (pubFunc.isYueJi(Integer.parseInt(om.get("ACCEPT_CHANNEL_ID").toString()))) {
			ox.append("<IS_OVER>是</IS_OVER>");
		} else {
			ox.append("<IS_OVER>否</IS_OVER>");
		}
		String finishDate = om.get("FINISH_DATE") == null ? "" : "<![CDATA[" + om.get("FINISH_DATE") + "]]>";
		ox.append("<FINISH_DATE>" + finishDate + "</FINISH_DATE>");
		ox.append("<COMPLAINT_PHENOMENON>" + om.get("PHENOMENON") + "</COMPLAINT_PHENOMENON>");
		ox.append("<ACCEPT_CONTENT><![CDATA[" + om.get("ACCEPT_CONTENT") + "]]></ACCEPT_CONTENT>");
		String reason = om.get("REASON") == null ? "" : om.get("REASON").toString();
		ox.append("<COMPLAINT_REASON>" + reason + "</COMPLAINT_REASON>");
		ol.clear();
		StringBuilder sx = new StringBuilder("");
		List sl = null;
		sl = jdbcTemplate.queryForList(SQL_SHEETINFO, serviceOrderId, serviceOrderId );
		for (Object obj : sl) {
			Map sm = (Map)obj;
			sx.append("<SHEET>");
			sx.append("<DEAL_ORG>" + sm.get("DEAL_ORG_NAME") + "</DEAL_ORG>");
			sx.append("<RESPOND_DATE><![CDATA[" + sm.get("RESPOND_DATE") + "]]></RESPOND_DATE>");
			String dealContent = sm.get("DEAL_CONTENT") == null ? "" : "<![CDATA[" + sm.get("DEAL_CONTENT") + "]]>";
			sx.append("<DEAL_CONTENT>" + dealContent + "</DEAL_CONTENT>");
			sx.append("</SHEET>");
		}
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><SERVICE>" + ox + "<SHEETS>" + sx
				+ "</SHEETS><RETURN_CODE>1</RETURN_CODE><MESSAGE></MESSAGE></SERVICE>";
	}

	private String wrongResult(String returnCode, String message) {
		StringBuilder xml = new StringBuilder("");
		xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><SERVICE>");
		xml.append("<RETURN_CODE>" + returnCode + "</RETURN_CODE><MESSAGE>" + message + "</MESSAGE>");
		xml.append("</SERVICE>");
		return xml.toString();
	}

}