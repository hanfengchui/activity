package com.timesontransfar.autoAccept.dao;

import java.util.List;

import com.timesontransfar.autoAccept.pojo.ZQCustInfoData;
@SuppressWarnings("rawtypes")
public interface IAutoAcceptDao {

    /**
     * 批量保存申诉单
     * 
     * @param list 申诉单列表
     * @return 保存的记录数
     */
    public int saveAutoAcceptOrderPatch(final List list);

    public int deleteAutoAcceptOrder();
    
    public List getAutoAcceptOrderListByCondition(String condition);
    
    public int updateAutoAcceptOrderList(final List list);
    
    public int updateAutoAcceptOrderListStatu(final List list);
    
    public int updateZQCustInfoData(ZQCustInfoData data);
    
    public int getComplaint(String mitCode);
    
    public int saveComplaintListPatch(final List list, String operator);
    
    public int updateComplaintListPatch(final List list, String operator);
    
    public int updateComplaintList(int status, int state, String operator, String mitCode);

}
