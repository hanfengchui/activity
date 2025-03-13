package com.timesontransfar.pubWebservice;

public interface IAutoCustomerVisit {
    /**
     * 调用客户服务质量系统自动回访接口
     * @param orderid
     * @param jtSourId
     * @return
     */
    public boolean autoCustomerVisit(String orderid, String jtSourId);
}
