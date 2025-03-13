package com.timesontransfar.autoAccept.service;

import java.io.InputStream;

import com.timesontransfar.autoAccept.pojo.ZQCustInfoData;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;

import net.sf.json.JSONArray;

public interface IAutoAcceptService {

	/**
	 * 解析导入的Excel文件
	 * @param username	员工登陆工号
	 * @param file		表格文件流
	 * @return
	 */
    public String saveOrderInfoBatch(String username, InputStream file);

    public int deleteAutoAcceptOrder();
    
    public int analyseData(String staffId);
    
    public String submitOrder(int staffId);
    
    public int submitDealContent(ZQCustInfoData data);
    
    public String saveComplaintInfo(InputStream file, String operator);
    
    public GridDataInfo getComplaintSheet(int currentPage, int pageSize, JSONArray createDate, String state, boolean forRelation);
    
    public int updateComplaintSheet(int currentPage, int pageSize);
}
