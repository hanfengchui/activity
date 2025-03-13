package com.timesontransfar.customservice.orderask.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.timesontransfar.customservice.orderask.dao.IServiceOrderQueryDao;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.service.ServiceOrderQuery;

@Service
public class ServiceOrderQueryImpl implements ServiceOrderQuery {
	private static final Logger logger = LoggerFactory.getLogger(ServiceOrderQueryImpl.class);
	 
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;
    
    @Autowired
    private IorderCustInfoDao orderCustDao;
    
    @Autowired
    private IserviceContentDao servContentDao;
	
    @Autowired
    private IServiceOrderQueryDao queryDao;
    
    
    /**
     * 根据受理单的单号查询此受理单的相关信息
     * 
     * @param orderId 受理单号
     * @param hisFlag 当前、历史标识
     * @return 受理单对像
     */
    public ServiceOrderInfo getServOrderInfo(String orderId, boolean hisFlag) {
        // 受理信息
        OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, hisFlag);
        if (orderAskInfo == null) {
        	logger.warn("没有查到受理信息 orderId: {} hisFlag: {}", orderId, hisFlag);
            return null;// 如果没有受理信息则返回空
        }

        // 受理内容
        ServiceContent servContent = servContentDao.getServContentByOrderId(orderId, hisFlag, orderAskInfo.getOrderVer());
        // 受理客户信息
        String custGuid = orderAskInfo.getCustId();
        OrderCustomerInfo orderCust = orderCustDao.getOrderCustByGuid(custGuid, hisFlag);

        ServiceOrderInfo servOrderInfo = new ServiceOrderInfo();
        servOrderInfo.setOrderAskInfo(orderAskInfo);
        if (servContent != null) {
            servOrderInfo.setServContent(servContent);
        } else {
            servOrderInfo.setServContent(new ServiceContent());
        }
        if (orderCust != null) {
            servOrderInfo.setOrderCustInfo(orderCust);
        } else {
            servOrderInfo.setOrderCustInfo(new OrderCustomerInfo());
        }
        return servOrderInfo;
    }
    
	@SuppressWarnings("rawtypes")
	public Map<String, String> getWarnOrgWhere(String staffId) {
		Map<String, String> warnOrgMap = new HashMap<String, String>();
    	Map roleMap = queryDao.getWarnRole(staffId);
    	if(!roleMap.isEmpty()) {
    		String roleClass = roleMap.get("role_class").toString();
    		String roleOrg = roleMap.get("role_department").toString();
    		if("2".equals(roleClass)){//班长
    			warnOrgMap.put("roleClass", "2");
    			warnOrgMap.put("orgWhere", "'" + roleOrg + "'");
    		} else if ("3".equals(roleClass)){//主管
    			warnOrgMap.put("roleClass", "3");
    			warnOrgMap.put("orgWhere", "select t.org_id from tsm_organization t where t.linkid like '" + roleOrg + "'");
    		}
    	}
    	return warnOrgMap;
    }
	
	@SuppressWarnings("rawtypes")
	public Map getStaffSheet(String staffId,String expiredDate) {
		Map staffSheet = queryDao.getStaffSheet(staffId, expiredDate);
		return staffSheet;
	}

	@SuppressWarnings("rawtypes")
	public List getViewStaff(String staffId, String queryType) {
        List viewStaff = queryDao.getViewStaff(staffId, queryType);
        return viewStaff;
	}
	
	@SuppressWarnings("unchecked")
	public String getOrderIdStrByMiitCode(String miitCode, boolean hisFlag) {
		List<Map<String, Object>> list = orderAskInfoDao.queryComplaintListByMiitCode(miitCode, hisFlag);
		if(list.isEmpty()) {
			return null;
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for(int i = 0; i < list.size(); i ++) {
			Map<String, Object> map = list.get(i);
			sb.append("'" + map.get("SERVICE_ORDER_ID").toString() + "'");
			if(i < list.size() -1) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
}