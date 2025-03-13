package com.timesontransfar.customservice.orderask.dao.impl;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.orderask.dao.IcomplaintInfoDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(value = "IcomplaintInfoDao")
public class ComplaintInfoDaoImpl implements IcomplaintInfoDao {
    private static final Logger log = LoggerFactory.getLogger(ComplaintInfoDaoImpl.class);

    @Autowired
    private IdbgridDataPub dbgridDataPub;//Grid公共类

    @Override
    public GridDataInfo getData(String acceptTime, String ipAddress, int currentPage,int pageSize,String activeName) {
        GridDataInfo result = null;
        try{
            String startTime = acceptTime.split(",")[0];
            String endTime = acceptTime.split(",")[1];
            StringBuilder baseSql = new StringBuilder("SELECT %PARAM% FROM ");
            if("now".equals(activeName)){
                baseSql.append(" CC_COMPLAINT_INFO h, CC_SERVICE_ORDER_ASK c ");
            }else {
                baseSql.append( " CC_COMPLAINT_INFO_HIS h, CC_SERVICE_ORDER_ASK_HIS c ");
            }
            String s = baseSql +" WHERE 1 = 1 " +
                    " AND c.SERVICE_ORDER_ID = h.SERVICE_ORDER_ID" +
                    " AND h.IP_ADDRESS = '"+ipAddress+"'" +
                    " AND h.ACCEPT_DATE >= '"+startTime + "'" +
                    " AND h.ACCEPT_DATE <= '"+endTime + "'";
            if(!"now".equals(activeName)){
            	s += " AND c.ORDER_STATU in (720130010,720130002)";
            }
            String countSql = s.replace("%PARAM%", "COUNT(1)");
            if("now".equals(activeName)){
                baseSql.append(
                        " LEFT JOIN CC_SERVICE_CONTENT_ASK f ON f.SERVICE_ORDER_ID = c.SERVICE_ORDER_ID " +
                                "LEFT JOIN CC_ORDER_CUST_INFO g ON g.CUST_GUID = c.CUST_GUID ");
            }else {
                baseSql.append(
                        " LEFT JOIN CC_SERVICE_CONTENT_ASK_HIS f ON f.SERVICE_ORDER_ID = c.SERVICE_ORDER_ID AND f.ORDER_VESION = c.ORDER_VESION " +
                                "LEFT JOIN CC_ORDER_CUST_INFO_HIS g ON g.CUST_GUID = c.CUST_GUID ");
            }
            baseSql.append(" WHERE 1 = 1 " +
                    " AND c.SERVICE_ORDER_ID = h.SERVICE_ORDER_ID" +
                    " AND h.IP_ADDRESS = '"+ipAddress+"'" +
                    " AND h.ACCEPT_DATE >= '"+startTime + "'" +
                    " AND h.ACCEPT_DATE <= '"+endTime + "'");
            if(!"now".equals(activeName)){
            	baseSql.append(" AND c.ORDER_STATU in (720130010,720130002)");
            }
            String strSql = baseSql.toString().replace("%PARAM%","c.SERVICE_ORDER_ID," +
                    "c.ORDER_STATU,c.ORDER_STATU_DESC,c.MONTH_FLAG,DATE_FORMAT(c.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,c.PROD_NUM," +
                    "c.SERVICE_TYPE_DESC,f.BEST_ORDER_DESC,c.ACCEPT_STAFF_NAME,c.ACCEPT_COUNT,c.REGION_ID," +
                    "(SELECT COUNT(1) FROM " + ("his".equals(activeName) ? "CC_HASTEN_SHEET_HIS" : "CC_HASTEN_SHEET") + " K WHERE K.SERVICE_ORDER_ID = c.SERVICE_ORDER_ID) AS CUIDANCOUNT," +
                    "c.REGION_NAME,c.ACCEPT_ORG_NAME,g.CUST_SERV_GRADE_NAME AS CUST_GRADE_NAME,f.APPEAL_PROD_NAME AS APPEAL_PROD_NAME," +
                    "(select b.logonname from tsm_staff b where b.staff_id=ACCEPT_STAFF_ID) as LOGONNAME,"+
                    "c.ORDER_VESION,DATE_FORMAT(c.MODIFY_DATE,'%Y-%m-%d %H:%i:%s') AS MODIFY_DATE");
            result = this.dbgridDataPub.getResultNewBySize(countSql, strSql, currentPage, pageSize, " ORDER BY h.ACCEPT_DATE", "IP地址查询");
        }catch (Exception e){
            log.error("getData error: {}", e.getMessage(), e);
        }
        return result;
    }
    
}