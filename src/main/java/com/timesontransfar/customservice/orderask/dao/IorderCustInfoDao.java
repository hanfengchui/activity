/**
 * <p>类名：IorderCustInfoDao.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	Mar 18, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.orderask.dao;

import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;

/**
 * @author lifeng
 *
 */
public interface IorderCustInfoDao {
	/**
	 * 保存受理单相关客户信息
	 * @param orderCustInfo 客户信息pojo
	 * @param hisFlag 是否操作历史表
	 * @return 保存成功记录数
	 */
	public int saveOrderCust(OrderCustomerInfo orderCust); 
	
	/**
	 * 将当前表中的客户信息存入到历史表
	 * @param currentCustGuid	当前表客户guid
	 * @return	保存成功记录数
	 */
	public int saveOrderCustHis(String currentCustGuid,Integer month);
	
	/**
	 * 根据客户guid查询
	 * @param custGuid
	 * @param hisFlag 是否操作历史表
	 * @return	客户信息对象
	 */
	public OrderCustomerInfo getOrderCustByGuid(String custGuid, boolean hisFlag);
	
	/**
	 * 根据受理单号查询此受理单的客户信息
	 * @param orderId	受理单号
	 * @param hisFlag	当前/历史
	 * @return	客户信息对象
	 */
	public OrderCustomerInfo getOrderCustByOrderId(String orderId);
	
	/**
	 * 删除受理单的客户信息
	 * @param custGuid	客户guid
	 * @return	删除的记录条数
	 */
	public int delOrderCustInfo(String custGuid,Integer month);
	
	/**
	 * 更新一个客户的信息
	 * @param custInfo	客户对像
	 * @return	更新的记录数
	 */
	public int updateCustInfo(OrderCustomerInfo custInfo);
	
}
