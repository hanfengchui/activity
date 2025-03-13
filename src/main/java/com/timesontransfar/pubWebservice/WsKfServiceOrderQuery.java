package com.timesontransfar.pubWebservice;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

@Service
public class WsKfServiceOrderQuery {

	/**
	 * 日志打印实例
	 */
	protected Logger logger = LoggerFactory.getLogger(WsKfServiceOrderQuery.class);

	/**
	 * 查询在途服务订单的SQL语句
	 */
	private static final String SQL_STR = 
			"SELECT C.APPEAL_PROD_ID,C.APPEAL_PROD_NAME,C.APPEAL_REASON_ID,C.APPEAL_REASON_DESC,C.APPEAL_CHILD,C.APPEAL_CHILD_DESC,C.FOU_GRADE_CATALOG,C.FOU_GRADE_CATALOG_DESC,"
			+ "S.SERVICE_ORDER_ID,S.ORDER_VESION,S.REGION_ID AS CITY_ID,S.REGION_NAME AS CITY_NAME,S.SERVICE_TYPE,S.SERVICE_TYPE_DESC,S.CALL_SERIAL_NO,S.SOURCE_NUM,S.CUST_GUID,"
			+ "S.RELA_MAN,S.RELA_TYPE,S.PROD_NUM,S.RELA_INFO,S.ACCEPT_CHANNEL_ID,S.ACCEPT_CHANNEL_DESC,S.ACCEPT_COME_FROM,S.ACCEPT_COME_FROM_DESC,S.ACCEPT_STAFF_ID,S.ACCEPT_STAFF_NAME,"
			+ "S.ACCEPT_ORG_ID,S.ACCEPT_ORG_NAME,S.ACCEPT_DATE,S.MODIFY_DATE,S.FINISH_DATE,S.CUST_SERV_GRADE,S.CUST_SERV_GRADE_DESC,S.URGENCY_GRADE,S.URGENCY_GRADE_DESC,S.ORDER_STATU,"
			+ "S.ORDER_LIMIT_TIME,S.SUB_SHEET_COUNT,S.HANGUP_START_TIME,S.HANGUP_TIME_COUNT,S.PRE_ALARM_VALUE,S.ALARM_VALUE,S.COMMENTS,S.SERVICE_DATE,S.SERVICE_DATE_DESC,"
			+ "S.CUST_GROUP,S.CUST_GROUP_DESC,S.ORDER_STATU_DESC,S.ASSIST_SELL_NO FROM CC_SERVICE_ORDER_ASK S,CC_SERVICE_CONTENT_ASK C WHERE S.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID "
			+ "AND S.REGION_ID = ? AND S.PROD_NUM = ?";

	/**
	 * Spring的数据库操作实例
	 */
	@Autowired
	private JdbcTemplate jdbcTemplate;

	/**
	 * 提供在途服务订单查询功能
	 * 
	 * @param xml
	 *            参数，xml格式的字符串
	 * @return 查询结果
	 */
	@SuppressWarnings("rawtypes")
	public String query(String xml) {
		logger.info("WsKfServiceOrderQuery query in: {}", xml);
		
		StringBuilder results = new StringBuilder("");
		StringBuilder message = new StringBuilder("");
		try {
			StringReader reader = new StringReader(xml.toUpperCase());
			SAXReader saxReader = new SAXReader();
			saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			Document doc = saxReader.read(reader);
			String prodNum = this.getElementValue(doc, "/IN_PARAM/SOURCE_NUM");
			String regionId = this.getElementValue(doc, "/IN_PARAM/CITY_ID");

			if (isEmpty(prodNum, regionId)) {
				logger.error("入参不完整");
				return buildInfo("", "Parameters Not Integrity");
			}

			Long regionid = Long.valueOf(regionId);

			// 组装SQL语句
			Object[] params = { regionid, prodNum };

			// 执行查询语句
			List records = jdbcTemplate.queryForList(SQL_STR, params);
			if (records.isEmpty()){
				logger.info("没有查询到相关记录");
				return buildInfo("", "No Results Match");
			}

			Iterator iterator = records.iterator();
			Map recordMap = null;

			// 遍历每一条记录
			while (iterator.hasNext()) {
				results.append("<result>");
				recordMap = (Map) iterator.next();
				Set entries = recordMap.entrySet();
				Iterator it = entries.iterator();
				while (it.hasNext()) {
					Entry e = (Entry) it.next();
					results.append("<").append(e.getKey()).append(">");
					results.append(e.getValue());
					results.append("</").append(e.getKey()).append(">");
				}
				results.append("</result>");
			}
		} catch (DocumentException e) {
			logger.error("Uncorrect XML Format: {}", e.getMessage(), e);
			message.append("Uncorrect XML Format");
		} catch (NumberFormatException e) {
			logger.error("CITY_ID Not A Number: {}", e.getMessage(), e);
			message.append("CITY_ID Not A Number");
		} catch (Exception e) {
			logger.error("WsKfServiceOrderQuery query error: {}", e.getMessage(), e);
			message.append("Internal Server Error");
		}
		return buildInfo(results.toString(), message.toString());
	}

	/**
	 * 返回xpath对应元素的值
	 * 
	 * @param doc
	 * @param xpath
	 * @return
	 */
	private String getElementValue(Document doc, String xpath) {
		Element e = (Element) doc.selectSingleNode(xpath);
		if (null != e)
			return e.getStringValue().trim();
		else
			return "";
	}

	/**
	 * 检查参数，看是否包含空值。
	 * 
	 * @param prodNum
	 *            待检验的字符串
	 * @param regionId
	 *            待检验的字符串
	 * @return 当且仅当两个字符串参数都是长度大于0时，返回false
	 */
	private boolean isEmpty(String prodNum, String regionId) {
		return null == prodNum || prodNum.length() == 0 || null == regionId || regionId.length() == 0;
	}

	/**
	 * 组装结果字符串
	 * 
	 * @param results
	 *            查询结果
	 * @param message
	 *            错误信息
	 * @return 结果字符串
	 */
	private String buildInfo(String results, String message) {
		if (results.equals("")) {
			results = "<result></result>";
		}
		String str = "<?xml version=\"1.0\" encoding=\"GBK\"?><info>" + results
				+ "<error><message>" + message + "</message></error></info>";
		logger.info("WsKfServiceOrderQuery query out: {}", str);
		return str;
	}
	
	private static final String SQL_QUERYORDERSBYPROD =  
            "select s.SERVICE_ORDER_ID,s.PROD_NUM,DATE_FORMAT(s.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\r\n"
            + "       s.SERVICE_TYPE_DESC, s.ORDER_STATU_DESC\r\n"
            + "  from cc_service_order_ask s\r\n"
            + " where s.prod_num = ?\r\n"
            + "   and s.region_id = ?\r\n"
            + "   and s.service_date = 3\r\n"
            + "union\r\n"
            + "select s.SERVICE_ORDER_ID,s.PROD_NUM,DATE_FORMAT(s.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE,\r\n"
            + "       s.SERVICE_TYPE_DESC, s.ORDER_STATU_DESC\r\n"
            + "  from cc_service_order_ask_his s\r\n"
            + " where s.accept_date > DATE_SUB(NOW(),INTERVAL -90 DAY)\r\n"
            + "   and s.order_statu in (700000103, 720130010)\r\n"
            + "   and s.prod_num = ?\r\n"
            + "   and s.region_id = ?\r\n"
            + "   and s.service_date = 3\r\n"
            + "   limit 10";
	
	private static final String SQL_QUERYORDERINFO = 
                    "SELECT ORDERID, REGION, SERVCETYPE, CHANNEL, CONTENT, DEALDESC\n" +
                    "  FROM (SELECT S.SERVICE_ORDER_ID    ORDERID,\n" + 
                    "               S.REGION_NAME         REGION,\n" + 
                    "               S.SERVICE_TYPE_DESC   SERVCETYPE,\n" + 
                    "               S.ACCEPT_CHANNEL_DESC CHANNEL,\n" + 
                    "               C.ACCEPT_CONTENT      CONTENT\n" + 
                    "          FROM CC_SERVICE_ORDER_ASK S, CC_SERVICE_CONTENT_ASK C\n" + 
                    "         WHERE S.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID\n" + 
                    "           AND S.SERVICE_ORDER_ID = ?\n" + 
                    "        UNION\n" + 
                    "        SELECT S.SERVICE_ORDER_ID    ORDERID,\n" + 
                    "               S.REGION_NAME         REGION,\n" + 
                    "               S.SERVICE_TYPE_DESC   SERVCETYPE,\n" + 
                    "               S.ACCEPT_CHANNEL_DESC CHANNEL,\n" + 
                    "               C.ACCEPT_CONTENT      CONTENT\n" + 
                    "          FROM CC_SERVICE_ORDER_ASK_HIS S, CC_SERVICE_CONTENT_ASK_HIS C\n" + 
                    "         WHERE S.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID\n" + 
                    "           AND S.ORDER_VESION = C.ORDER_VESION\n" + 
                    "           AND S.ORDER_STATU IN ('700000103', '720130010')\n" + 
                    "           AND S.SERVICE_ORDER_ID = ?" + 
                    "       ) A,\n" + 
                    "       (SELECT SERVICE_ORDER_ID,\n" + 
                    "               group_concat(DEAL_CONTENT) DEALDESC\n" + 
                    "          FROM (SELECT S.SERVICE_ORDER_ID, W.DEAL_CONTENT\n" + 
                    "                  FROM CC_SERVICE_ORDER_ASK_HIS S, CC_WORK_SHEET_HIS W\n" + 
                    "                 WHERE 1 = 1\n" + 
                    "                   AND S.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID\n" + 
                    "                   AND S.SERVICE_DATE = 3\n" + 
                    "                   AND S.SERVICE_ORDER_ID = ?\n" + 
                    "                   AND S.ORDER_STATU IN ('700000103', '720130010')\n" + 
                    "                 ORDER BY W.RESPOND_DATE,W.WORK_SHEET_ID) as a\n" + 
                    "         GROUP BY SERVICE_ORDER_ID\n" + 
                    "        UNION\n" + 
                    "        SELECT SERVICE_ORDER_ID,\n" + 
                    "            group_concat(DEAL_CONTENT) DEALDESC\n" + 
                    "          FROM (SELECT S.SERVICE_ORDER_ID, W.DEAL_CONTENT\n" + 
                    "                  FROM CC_SERVICE_ORDER_ASK S, CC_WORK_SHEET W\n" + 
                    "                 WHERE 1 = 1\n" + 
                    "                   AND S.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID\n" + 
                    "                   AND S.SERVICE_DATE = 3\n" + 
                    "                   AND S.SERVICE_ORDER_ID = ?\n" + 
                    "                 ORDER BY W.RESPOND_DATE,W.WORK_SHEET_ID) as c\n" + 
                    "         GROUP BY SERVICE_ORDER_ID" + 
                    "       ) B\n" + 
                    " WHERE A.ORDERID = B.SERVICE_ORDER_ID";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String queryOrder(String inJSON){
		logger.info("WsKfServiceOrderQuery queryOrder in: {}", inJSON);
        Gson gs = new Gson();
        Map<String, String> inMap = gs.fromJson(inJSON, Map.class);
        String outJSON = null;
        String type = inMap.get("QUERYTYPE"); // 0-只看列表；1-查看详情
        if("0".equals(type)){
            String prodNum = inMap.get("PRODNUM");
            String area = inMap.get("AREACODE");
            String regionId = this.getRegionId(area);
            Object[] params = {prodNum, regionId, prodNum, regionId};
            
            List records = new ArrayList();
            if(!"".equals(regionId)) {
            	records = jdbcTemplate.queryForList(SQL_QUERYORDERSBYPROD, params);
            }
            
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("PRODNUM", prodNum);
            resultMap.put("AREACODE", area);
            resultMap.put("QUERYTYPE", "0");
            if(records.isEmpty()){
                Map[] mm = new HashMap[1];
                mm[0] = new HashMap();
                resultMap.put("DATASIZE", 0);
                resultMap.put("DATALIST", new Gson().toJson(mm));
            }else{
                resultMap.put("DATASIZE", records.size());
                resultMap.put("DATALIST", new Gson().toJson(records));
            }
            outJSON = new Gson().toJson(resultMap);
        }else if("1".equals(type)){
            String orderId = inMap.get("ORDERID");
            Object[] params = {orderId, orderId, orderId, orderId};
            List records = jdbcTemplate.queryForList(SQL_QUERYORDERINFO, params);
            Map<String, String> map = null;
            int len = 0;
            if(records.isEmpty()){
                len = 0;
                map = new HashMap<String, String>();
            }else{
                len = 1;
                map = (Map<String, String>)records.get(0);
            }
            map.put("QUERYTYPE", "1");
            map.put("DATASIZE", len+"");
            map.put("ORDERID", orderId);
            outJSON = new Gson().toJson(map);
        }else{
            Map<String, String> map = new HashMap<String, String>();
            map.put("QUERYTYPE", "0");
            map.put("DATASIZE", "0");
            outJSON = new Gson().toJson(map);
        }
        logger.info("WsKfServiceOrderQuery queryOrder out: {}", outJSON);
        return outJSON;
    }
	
	public String getRegionId(String areaCode){
        String regionId = "";
        switch (areaCode){
            case "025":
                regionId = "3";
                break;
            case "0510":
            	regionId = "15";
                break;
            case "0511":
            	regionId = "4";
                break;
            case "0512":
            	regionId = "20";
                break;
            case "0513":
            	regionId = "26";
                break;
            case "0514":
            	regionId = "33";
                break;
            case "0515":
            	regionId = "39";
                break;
            case "0516":
            	regionId = "48";
                break;
            case "0517":
            	regionId = "60";
                break;
            case "0518":
            	regionId = "63";
                break;
            case "0519":
            	regionId = "69";
                break;
            case "0523":
            	regionId = "79";
                break;
            case "0527":
            	regionId = "84";
                break;
            default:
                break;
        }
        return regionId;
    }
}
