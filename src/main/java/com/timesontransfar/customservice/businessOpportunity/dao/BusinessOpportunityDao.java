package com.timesontransfar.customservice.businessOpportunity.dao;


import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;

import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public interface BusinessOpportunityDao {
    /**
     * 修改商机单
     *
     * @param buopSheetInfo
     *            商机单对象
     * @return
     */
    public int updateBuopSheetInfo(BuopSheetInfo buopSheetInfo) ;



    /**
     * 查询商机单信息
     *
     * @param serviceId
     *            商机单对象
     * @return
     */
    public Map<String, Object> selectBuopSheetInfo(String serviceOrderId);
}
