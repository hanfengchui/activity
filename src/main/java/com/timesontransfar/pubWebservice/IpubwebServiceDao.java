/**
 * @author 万荣伟
 */
package com.timesontransfar.pubWebservice;

import java.util.List;

import com.timesontransfar.customservice.orderask.pojo.OrderRelation;
import com.timesontransfar.systemPub.entity.PubColumn;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface IpubwebServiceDao {
	
	/**
	 * 查询定单当天状态,先进行当天表查询,在当前表不存在进行历史查询
	 * @param orderId
	 * @param boo
	 * @return
	 */
	public List getOrderStatu(String orderId);

	/**
	 * 查询定单当天状态,先进行当天表查询,在当前表不存在进行历史查询
	 * @param unifiedComplaintCode集团统一编码
	 * @param boo
	 * @return
	 */
	public List getUccStatu(String unifiedComplaintCode);

	/**
	 * 查询定单当天状态,先进行当天表查询,在当前表不存在进行历史查询
	 * @param orderId
	 * @param boo
	 * @return
	 */
	public List getProdOrder(String prodNum);
	
	/**
	 * 根据CRM的客户ID得到某时间段
	 * @param crmCustId CRM客户ID
	 * @param startData 开始时间
	 * @param endData 结束时间
	 * @return
	 */
	public List getCrmCstOrder(String crmCustId,String startData,String endData);
	
	/**
	 * 根据referId得到静态数据名
	 * @param referId
	 * @return
	 */
	public String getStaticName(int referId);
	
	/**
	 * 得到97D级(县市)的上级本地网地域id
	 * @param regionId 地域id
	 * @return 97D级(县市)的上级本地网地域id
	 */
	public int getUpRegionId(int regionId);
	
	/**
	 * 根据地域得到地域名
	 * @param regionId 地域ID
	 * @return
	 */
	public String getRegionName(int regionId);
		
	public int saveOrderRelation(OrderRelation r);
	
	public PubColumn getPubColumn(String referId);

}
