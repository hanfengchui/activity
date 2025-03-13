package com.timesontransfar.customservice.tuschema.service;

import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;

public interface IserviceContentSchem {
	
	public ServiceContentSave[] filterRefundData(ServiceContentSave[] saves);
	
	public String saveOrderContents(ServiceContentSave[] saves,  String serviceId);
	
	public String saveDealContentSave(List<ServiceContentSave> saveList, String serviceId);

    /**
     * 将指定受理单的逐条受理内容移入历史
     * 
     * @param serviceOrderId
     *            受理单号
     * @return 操作成功的记录数
     */
    public int insertServiceContentSaveHis(String serviceOrderId);
    
    public int insertDealContentSaveHis(String serviceOrderId);
    
    /**
     * 小额退赔建单信息
     * @param map
     * @param orderId
     * @param hisFlag
     */
    @SuppressWarnings("rawtypes")
	public void setRefundInfo(Map map, String orderId, boolean hisFlag);

}