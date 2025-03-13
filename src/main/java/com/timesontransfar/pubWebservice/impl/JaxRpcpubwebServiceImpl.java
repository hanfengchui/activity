package com.timesontransfar.pubWebservice.impl;

import java.io.StringReader;
import java.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.timesontransfar.pubWebservice.IpubwebServiceDao;

@Component
public class JaxRpcpubwebServiceImpl {
    protected Logger logger = LoggerFactory.getLogger(JaxRpcpubwebServiceImpl.class);

    @Autowired
    private IpubwebServiceDao pubwebServiceDaoImpl;

    float excTime = 0;

	private Document returnSaxRed(String str) throws SAXException, DocumentException {
		SAXReader saxReader = new SAXReader();
		saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		StringReader reader = new StringReader(str);
		return saxReader.read(reader);
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
		if (e != null)
			return e.getStringValue().trim();
		else
			return "";
	}

    String xmlEnd =
            "<RET_INFO>"
                    +"<ORDER_INFO></ORDER_INFO>"
                    +"<ORDER_ID></ORDER_ID>"
                    +"<ORDER_STATU></ORDER_STATU>"
                    +"<ORDER_END_DATE></ORDER_END_DATE>"
                    +"<UNIFIED_COMPLAINT_CODE></UNIFIED_COMPLAINT_CODE>"
                    +"</RET_INFO>"
                    +"</REQUEST_XML>";
    String request="</REQUEST_XML>";
    String version = "<?xml version=\"1.0\" encoding=\"GBK\"?>";
    String request1 ="<REQUEST_XML>";
    String retflag="<RETFLAG>1</RETFLAG>";
    String retflag0="<RETFLAG>0</RETFLAG>";
    String reterrorid0="<RETERRORID>0</RETERRORID>";
    String reterrorinfo="<RETERRORINFO>查询成功</RETERRORINFO>";
    String orderStatuXML="ORDER_STATU";
    String accptContentXml="ACCEPT_CONTENT";
    String finishDateXml="FINISH_DATE";
    String unifiedComplaintCodeXML="UNIFIED_COMPLAINT_CODE";
    String retInfoOrderIdXML="<RET_INFO><ORDER_INFO>0</ORDER_INFO><ORDER_ID>";
    String orderIdXML="</ORDER_ID><ORDER_STATU>";
    String prodNumXml="<PROD_NUM>";
    String prodNum1 = "PROD_NUM";
    String proNumXml1="</PROD_NUM>";
    String accpInfoXML="<ACCETP_INFO>";
    String orderStatuEndDateXML="</ORDER_STATU><ORDER_END_DATE>";
    String orderEndDatexml="</ORDER_END_DATE>";
    String unifiedComplaintCodeXML1="<UNIFIED_COMPLAINT_CODE>";
    String unifiedComplaintCodeRetInfoXML="</UNIFIED_COMPLAINT_CODE></RET_INFO>";
    String orderStatuOrderEndDateXML="</ORDER_STATU><ORDER_END_DATE></ORDER_END_DATE>";
    String serviceOrderId="SERVICE_ORDER_ID";
    String queryTyep=null;
    String queryValue=null;
    String startData=null;
    String endData=null;
    String timeZX="执行时间";
    
    /**
     * 按订单号、产品号码、客户id进行查询
     * @param quryInfo
     * @return
     */
    public String getOrderStatu(String quryInfo) throws SAXException {
    	try {
            Document doc = returnSaxRed(quryInfo);
            queryTyep = this.getElementValue(doc, "/REQUEST_XML/QUERYINFO/QUERY_TYPE");//查询类型
            queryValue = this.getElementValue(doc, "/REQUEST_XML/QUERYINFO/QUERY_VALUE");//查询值
            startData = this.getElementValue(doc, "/REQUEST_XML/QUERYINFO/QUERY_START_DATA");//开始时间
            endData = this.getElementValue(doc, "/REQUEST_XML/QUERYINFO/QUERY_END_DATA");//结束时间
            //查询值为空
            if ("".equals(queryValue)) {
            	return version
                        + request1
                        + retflag
                        + "<RETERRORID>2</RETERRORID>"
                        + "<RETERRORINFO>查询值为空</RETERRORINFO>"
                        + xmlEnd;
            } else if ("1".equals(queryTyep)) {//批量定单查询
                String format = "批量定单查询==> " + "queryBatchOrders" + timeZX+excTime+"s"+","+"入参："+ quryInfo;
                logger.info(format);
                return queryBatchOrders();
            } else if ("3".equals(queryTyep)) {// 产品号码查询
                String format = "产品号码查询==> " + "queryProductNo" + timeZX+excTime+"s"+","+"入参："+ quryInfo;
                logger.info(format);
                return queryProductNo();
            } else if ("2".equals(queryTyep)) {//客户号查询
                String format = "客户号查询==> " + "queryCustomerNo" + timeZX+excTime+"s"+","+"入参："+ quryInfo;
                logger.info(format);
                return queryCustomerNo();
            } else if ("4".equals(queryTyep)) {//统一编码查询
                String format = "统一编码查询==> " + "queryUnifiedCode" + timeZX+excTime+"s"+","+"入参："+ quryInfo;
                logger.info(format);
                return queryUnifiedCode();
            }
            return version
                    +request1
                    +retflag
                    +"<RETERRORID>3</RETERRORID>"
                    +"<RETERRORINFO>查询类型不正确</RETERRORINFO>"
                    +xmlEnd;
        } catch (Exception e) {
            String format = "服务出错==> " + "getOrderStatu" + timeZX+excTime+"s"+" 出错："+ e.getMessage()+" 入参："+ quryInfo;
            logger.error(format);
            return version
                    + request1
                    + retflag
                    + "<RETERRORID>1</RETERRORID>"
                    + "<RETERRORINFO>接口异常,查询失败</RETERRORINFO>"
                    + xmlEnd;
        }
    }

    /**
     * 批量订单查询
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String queryBatchOrders(){
        String[] orderArry = queryValue.split("@");
        int size = orderArry.length;
        String retStr = version
                +request1
                +retflag0
                +reterrorid0
                +reterrorinfo;
        StringBuilder orderInfo = new StringBuilder();
        String acceptContent="";
        String finishDate="";
        String unifiedComplaintCode="";
        String orderStatu="";
        for(int i=0;i<size;i++) {
            String orderId = orderArry[i];
            List<Object> tmp = this.pubwebServiceDaoImpl.getOrderStatu(orderId);
            if(tmp != null && !tmp.isEmpty()) {
                Map<String,String> map = (Map)tmp.get(0);
                if(map != null) {
                    orderStatu= map.get(orderStatuXML);
                    acceptContent = map.get(accptContentXml);
                    finishDate = map.get(finishDateXml);
                    unifiedComplaintCode = map.get(unifiedComplaintCodeXML);
                    orderInfo.append(retInfoOrderIdXML+orderId
                            +orderIdXML+orderStatu
                            +prodNumXml+map.get(prodNum1)+proNumXml1
                            +accpInfoXML + acceptContent + "</ACCETP_INFO>"
                            +orderStatuEndDateXML + finishDate + orderEndDatexml
                            +unifiedComplaintCodeXML1 + unifiedComplaintCode + unifiedComplaintCodeRetInfoXML);
                    map.clear();
                }
                tmp.clear();
            } else {
            	orderInfo.append("<RET_INFO><ORDER_INFO>1</ORDER_INFO><ORDER_ID>"+orderId
                        +orderIdXML
                        +orderStatuOrderEndDateXML
                        +"<UNIFIED_COMPLAINT_CODE></UNIFIED_COMPLAINT_CODE></RET_INFO>");
            }
        }
        retStr=retStr+orderInfo+request;
        return retStr;
    }

    /**
     * 产品号码查询
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String queryProductNo(){
        String retStr = version
                + "<REQUEST_XML><RETFLAG>0</RETFLAG><RETERRORID>0</RETERRORID><RETERRORINFO>查询成功</RETERRORINFO>";
        List<Object> tmp = null;
        StringBuilder orderInfo = new StringBuilder("");
        String acceptContent="";
        String finishDate="";
        String unifiedComplaintCode="";
        String orderStatu="";
        tmp = this.pubwebServiceDaoImpl.getProdOrder(queryValue);
        if (tmp != null && !tmp.isEmpty()){
            for (int i = 0; i < tmp.size(); i++) {
                Map<String,String> map = (Map) tmp.get(i);
                if (map != null) {
                    orderStatu = map.get(orderStatuXML);
                    acceptContent =  map.get(accptContentXml);
                    finishDate =  map.get(finishDateXml);
                    unifiedComplaintCode = map.get(unifiedComplaintCodeXML);
                    orderInfo.append("<RET_INFO><ORDER_INFO>0</ORDER_INFO>");
                    orderInfo.append("<ORDER_ID>" + map.get(serviceOrderId) + "</ORDER_ID>");
                    orderInfo.append("<ORDER_STATU>" + orderStatu + prodNumXml + map.get(prodNum1) + proNumXml1);
                    orderInfo.append(accpInfoXML + acceptContent + "</ACCETP_INFO></ORDER_STATU>");
                    orderInfo.append("<ORDER_END_DATE>" + finishDate + orderEndDatexml);
                    orderInfo.append(unifiedComplaintCodeXML1 + unifiedComplaintCode + unifiedComplaintCodeRetInfoXML);
                    map.clear();
                }
            }
            tmp.clear();
        } else {
        	orderInfo.append("<RET_INFO><ORDER_INFO>1</ORDER_INFO><ORDER_ID></ORDER_ID><ORDER_STATU>"
                    + "</ORDER_STATU><ORDER_END_DATE></ORDER_END_DATE><UNIFIED_COMPLAINT_CODE></UNIFIED_COMPLAINT_CODE></RET_INFO>");
        }
        retStr = retStr + orderInfo + request;
        return retStr;
    }

    /**
     * 客户号查询
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String queryCustomerNo(){
        //查询时间段
        if("".equals(startData) || "".equals(endData)) {
        	return version
                    +request1
                    +retflag
                    +"<RETERRORID>4</RETERRORID>"
                    +"<RETERRORINFO>查询时间段不能为空</RETERRORINFO>"
                    +xmlEnd;
        }
        String retStr = version
                +request1
                +retflag0
                +reterrorid0
                +reterrorinfo;

        StringBuilder orderInfo = new StringBuilder();
        List<Object> tmp = this.pubwebServiceDaoImpl.getCrmCstOrder(queryValue, startData, endData);
        String finishDate="";
        String unifiedComplaintCode="";
        String orderStatu="";
        if(tmp != null && !tmp.isEmpty()) {
            int	size = tmp.size();
            for(int i=0;i<size;i++) {
                Map<String,String> map = (Map)tmp.get(i);
                if(map != null) {
                    orderStatu = map.get(orderStatuXML);
                    finishDate =  map.get(finishDateXml);
                    unifiedComplaintCode = map.get(unifiedComplaintCodeXML);
                    orderInfo.append(retInfoOrderIdXML+map.get(serviceOrderId)
                            +orderIdXML+orderStatu
                            +orderStatuEndDateXML + finishDate + orderEndDatexml
                            +unifiedComplaintCodeXML1 + unifiedComplaintCode + "</UNIFIED_COMPLAINT_CODE>"
                            +"</RET_INFO>");
                    map.clear();
                }
            }
            tmp.clear();
        } else {
        	orderInfo.append("<RET_INFO><ORDER_INFO>2</ORDER_INFO><ORDER_ID>"
                    +orderIdXML
                    +orderStatuOrderEndDateXML
                    +"<UNIFIED_COMPLAINT_CODE></UNIFIED_COMPLAINT_CODE></RET_INFO>");
        }
        retStr=retStr+orderInfo+request;
        return retStr;
    }

    /**
     * 统一编码查询
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public String queryUnifiedCode(){
        String retStr = version
                +request1
                +retflag0
                +reterrorid0
                +reterrorinfo;
        String orderInfo="";
        String acceptContent="";
        String finishDate="";
        String orderStatu="";
        List<Object> tmp = this.pubwebServiceDaoImpl.getUccStatu(queryValue);
        if(tmp != null && !tmp.isEmpty()) {
            Map<String,String> map = (Map)tmp.get(0);
            if(map != null) {
                orderStatu = map.get(orderStatuXML);
                acceptContent = map.get(accptContentXml);
                finishDate = map.get(finishDateXml);
                orderInfo +=retInfoOrderIdXML+map.get(serviceOrderId)
                        +orderIdXML+orderStatu
                        +prodNumXml+map.get(prodNum1)+proNumXml1
                        +accpInfoXML + acceptContent + "</ACCETP_INFO>"
                        +orderStatuEndDateXML + finishDate + orderEndDatexml
                        +unifiedComplaintCodeXML1 + queryValue + unifiedComplaintCodeRetInfoXML;
                map.clear();
            }
            tmp.clear();

        } else {
            orderInfo +="<RET_INFO><ORDER_INFO>1</ORDER_INFO><ORDER_ID></ORDER_ID><ORDER_STATU>"
                    +orderStatuOrderEndDateXML
                    +unifiedComplaintCodeXML1+queryValue
                    +unifiedComplaintCodeRetInfoXML;
        }
        retStr=retStr+orderInfo+request;
        return retStr;
    }

}
