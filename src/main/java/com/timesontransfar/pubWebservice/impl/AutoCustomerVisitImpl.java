package com.timesontransfar.pubWebservice.impl;

import java.text.SimpleDateFormat;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis2.transport.http.HTTPConstants;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.log.ServiceExcuteLogObject;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.pubWebservice.IAutoCustomerVisit;

@Component(value="autoCustomerVisitImpl")
public class AutoCustomerVisitImpl implements IAutoCustomerVisit {
	
    private static final Logger log = LoggerFactory.getLogger(AutoCustomerVisitImpl.class);
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;
    @Autowired
    private IorderCustInfoDao orderCustInfoDao;
    @Autowired
    private PubFunc pubFunc;

    private String otLength="4";// 超时时长（单位：小时）

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @SuppressWarnings("rawtypes")
	public boolean autoCustomerVisit(String orderId, String jtSourId) {
		log.info("autoCustomerVisit orderId：{}，jtSourId：{}", orderId, jtSourId);
    	ServiceExcuteLogObject excuObj = new ServiceExcuteLogObject();
        excuObj.setServiceType(1);
        excuObj.setServiceId("CONTACT_EVENT_KF__");
        excuObj.setReceiveMsg(orderId + "，" + jtSourId);
        excuObj.setExcuteTime(this.pubFunc.getSysDate());
        
        OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
        String prodNum = orderAskInfo.getProdNum();// 投诉号码
        String staffId = orderAskInfo.getAskStaffId() + "";// 受理员工ID
        String cityId = orderAskInfo.getRegionId() + "";// 区域（客服系统区域编号，南京：3）
        String relaMan = orderAskInfo.getRelaMan();// 联系人名称
        // 联系号码（包含固定电话）传送固化联系号码给对方时：需要加区号
        // 传手机号码：不需要加0
        String custSendNbr = orderAskInfo.getRelaInfo();
        String accDate = orderAskInfo.getAskDate();// 事件受理时间
        SimpleDateFormat forma = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
        	accDate = forma.format(forma.parse(accDate));
        } catch (Exception e) {
			log.error("autoCustomerVisit 异常：{}", e.getMessage());
            return false;
        }
        OrderCustomerInfo orderCustInfo = orderCustInfoDao.getOrderCustByGuid(orderAskInfo.getCustId(), false);
        String custId = orderCustInfo.getCrmCustId();// 客户ID
        if(null == custId || "null".equals(custId)){
            custId = "";
        }
        String custName = orderCustInfo.getCustName();// 客户名称
        String eventDate = pubFunc.getSysDate();// 客服系统当前时间
        Object[] param = new Object[] 
        {orderId, prodNum, staffId, staffId, 
        		cityId, custId, custName, accDate, eventDate, otLength, relaMan, custSendNbr, jtSourId, "1", "1", "1", "1"};
        
        //生产地址：http://132.228.237.71:9090/oss-webservice/services/mbkf
		//测试地址：http://132.254.28.44:9090/oss-webservice/services/mbkf
        Map intfMap = pubFunc.getIntfMap("contactEventKf");
        log.info("intfMap：{}", intfMap);
        if(null == intfMap){
        	return false;
        }
        String serviceUrl = intfMap.get("ADDRESS_IP").toString();
        String res = "";
        long startTime=System.currentTimeMillis();
        try {
            org.apache.axis2.rpc.client.RPCServiceClient client = new org.apache.axis2.rpc.client.RPCServiceClient();
            org.apache.axis2.client.Options options = client.getOptions();
            org.apache.axis2.addressing.EndpointReference epf = new org.apache.axis2.addressing.EndpointReference(serviceUrl.trim());
            options.setTo(epf);
            options.setProperty(HTTPConstants.SO_TIMEOUT, Integer.parseInt(intfMap.get("TIME_OUT").toString()));
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, Integer.parseInt(intfMap.get("TIME_OUT").toString()));
            String qName = "http://impl.service.mbkf.webservice.sqm.oss.ffcs.com";
            String method = "CONTACT_EVENT_KF";
            QName qname = new QName(qName, method);

            Object[] result = client.invokeBlocking(qname, param, new Class[]
            {String.class, String.class, String.class, String.class,
                    String.class, String.class, String.class, String.class,
                    String.class, String.class, String.class, String.class,
                    String.class, String.class, String.class, String.class, String.class});
            res = (String) result[0];
            excuObj.setRetFlag(true);
            excuObj.setOutMsg(res);
            excuObj.setCompleteTime(this.pubFunc.getSysDate());
            savelog(excuObj);
            
            Document doc = DocumentHelper.parseText(res);
            Element infoNode =  doc.getRootElement();
            String r = infoNode.elementTextTrim("result");
            if ("0".equals(r)) {
            	log.info(">>>调用WebService接口CONTACT_EVENT_KF方法--服务端返回成功--");
                return true;
            } else {
            	log.info(">>>调用WebService接口CONTACT_EVENT_KF方法--服务端返回失败--");
                return false;
            }
        } catch (Exception e) {
			log.error("autoCustomerVisit 异常：{}", e.getMessage(), e);
            return false;
        } finally {
        	log.info("接口请求地址：{}\n请求内容：{}\n接口返回：{}\n请求耗时：{}",serviceUrl,param,res,(System.currentTimeMillis()-startTime));
        }
    }

    private String sql = "INSERT INTO TIF_SERVICE_EXCUTE_LOG"
			+ " (GID,SERVICEID,SERVICETYPE,EXCUTETIME,COMPLETETIME,RETFLAG,INMSG,OUTMSG)"
			+ " VALUES (UPPER(replace(UUID(), '-', '')),?,?,STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'),STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'),"
			+ "?,?,?)";
  
    private void savelog(final ServiceExcuteLogObject obj){
    	try{
    		jdbcTemplate.update(sql, 
        			obj.getServiceId(),
    				obj.getServiceType(),
    				obj.getExcuteTime(),
    				obj.getCompleteTime(),
    				obj.isRetFlag() ? 1 : 0,
    				obj.getReceiveMsg(),
    				obj.getOutMsg()
        	);
        	
    	}catch(Exception e){
    		e.printStackTrace();
    	}
    }
}