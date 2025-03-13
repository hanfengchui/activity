/**
 * <p>类名：IserviceContentDao.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	Mar 18, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.orderask.dao;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
/**
 * @author lifeng
 *
 */
public interface IserviceContentDao {
	public int updateAcceptContent(String id,String content,Integer month);
	/**
	 * 
	 * @param PUB_COLUMN_REFERENCE referenceId
	 * @return 
	 */
	public int checkPubReference(int firstId,int secendId );
	
	/**
	 * 保存服务受理单相关受理内容信息
	 * @param serviceContent 受理单受理内容pojo
	 * @param hisFlag 当前/历史
	 * @return 保存记录成功记录数
	 */
	public int saveServiceContent(ServiceContent serviceContent, boolean hisFlag);
	/**
	 * 将当受理内容当前表的信息保存到历史表中
	 * @param currentOrderId	当前受量内容的受理单号
	 * @return	保存记录成功记录数
	 */
	public int saveServContentHis(String currentOrderId,Integer month);
	
	
	/**
	 * 根据受单号查询此受理单的受理内容
	 * @param orderId 受理单id
	 * @param hisFlag true历史;false 当前
	 * @param version 如果查询历史记录，需提供version值；如果查当前记录，该值无意义
	 * @return 受理单受理内容对象
	 */
	public ServiceContent getServContentByOrderId(String orderId, boolean hisFlag,int version);
	/**
	 * 根据受单号查询此受理单的受理内容
	 * @param orderId 受理单id
	 * @param suffix 如果suffix不为空查归档数据
	 * @param hisFlag true历史;false 当前
	 * @param version 如果查询历史记录，需提供version值；如果查当前记录，该值无意义
	 * @return 受理单受理内容对象
	 */
	public ServiceContent getServContentByOrderIdNew(String orderId,String suffix,boolean hisFlag,int version);
	
	/**
	 * @param orderId 受理单id
	 * @param suffix 表名的后缀，取值有三种,分别表示在途、历史、归档表："";"_HIS";"_HISBAK".
	 * @param version 订单的版本号
	 * @return
	 */
	public ServiceContent getServContentByOrderId(String orderId, String suffix, int version);
	
	/**
	 * 删除受理单的受理内容信息
	 * @param orderId	受理单号	 
	 * @return 删除的记录数
	 */
	public int delServContent(String orderId,Integer month);
	
	/**
	 * 更新定单的受理内容
	 * @param servContent	受理内容对像
	 * @return	更新的记录数
	 */
	public int updateServContent(ServiceContent servContent);
	/**
	 * 更新定单的受理内容
	 * @param id,content内容
	 * @return	更新的记录数
	 */
	public int updateAccpContent(String id,String content,Integer month);
	
	/**
	 * 更新客户的受理内容
	 * @param regionId 地域
	 * @param orderId 服务申请号
	 * @param acceptContent 受理内容
	 * @return
	 */
	public int updateAcceptContent(int regionId,String orderId,String acceptContent);
	
	/**
	 * 取消最严工单标识
	 */
	public int updateBestOrder(String orderId);
	
	/**
	 * 保存最严工单修改记录
	 */
	public void saveBestOrderModify(String orderId, String acceptDate, int bestOrderOrigin, int bestOrderFinal, int modifyType);
}