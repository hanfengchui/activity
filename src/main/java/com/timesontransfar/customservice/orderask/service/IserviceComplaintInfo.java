package com.timesontransfar.customservice.orderask.service;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;

public interface IserviceComplaintInfo {

    /**
     * 根据ip地址查询申诉单
     */
    public GridDataInfo getData(String acceptTime, String ipAddress,int currentPage,int pageSize,String activeName);
}