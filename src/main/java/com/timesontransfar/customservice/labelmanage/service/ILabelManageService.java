/*
 * 说明：管理受理单、工单标签的服务接口
 * 时间： 2012-4-27
 * 作者：LiJiahui
 * 操作：新增
 */
package com.timesontransfar.customservice.labelmanage.service;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.orderask.pojo.CustomerPersona;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;

/**
 * @author LiJiahui
 * 
 */
public interface ILabelManageService {

    /**
     * 标记省市总热线
     */
	public int updateHotlineFlag(String serviceOrderId, int staffId);
	
	/**
	 * 省服务监督热线(打标)在途单查询
	 */
	public GridDataInfo getHotlineGrid(String param);

	/**
	 * 剔除投诉工单处理内容中关键字“互联网卡”、“屏蔽外呼”标识
	 */
	public int modifyUnusualFlag(String serviceId);

	// 受理后异步标识
	public int submitServiceOrderAsync(String orderId, OrderAskInfo orderAskInfo, OrderCustomerInfo custInfo, CustomerPersona persona);
	
	public void trafficLabel(OrderAskInfo orderAskInfo);

	// 政企客户的投诉单，则发送受理短信至该客户的客户经理
	public int sendNoteToManagerPhone(String orderId, String accNum, int regionId, String accNbrType, int askStaffId);
	
	/**
	 * 近30天是否拨打过10000号人工
	 */
	public void updateCallFlag(String orderId, String prodNum, String relaInfo, int regionId);
	
	/**
	 * 重复标签
	 */
	public void updateRepeatFlag(ServiceLabel label, CustomerPersona persona, OrderAskInfo orderAskInfo, ServiceContent servContent, boolean complaintFlag, boolean unifiedFlag);

	/**
	 * 重复查询
	 */
	public void updateRepeatFlagCX(ServiceLabel label, CustomerPersona persona, OrderAskInfo orderAskInfo);
	
	/**
	 * 如意用户
	 */
	public void updateCustType(String orderId, String prodNum, int regionId);
}