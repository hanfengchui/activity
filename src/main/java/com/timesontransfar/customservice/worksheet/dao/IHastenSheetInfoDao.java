/**
 * <p>类名：IHastenSheetInfoDao.java</p>
 * <p>功能描叙:</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：万荣伟>
 * <p>开发/维护历史：</p>
 * <p>  Create by  万荣伟 2008-6-13</p> 
 */
package com.timesontransfar.customservice.worksheet.dao;

import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;

/**
 * @author 万荣伟
 *
 */
public interface IHastenSheetInfoDao {
	
	/**
	 * 根据工单号查询相应的催单信息
	 * @param sheetId	工单号
	 *  @param boo	为TRUE 查询历史 FALSE为当前查询
	 * @return	催单信息
	 */	
	public HastenSheetInfo[] getHastenSheetInfo(String sheetId,boolean boo);
	
	/**
	 * 生成催单工单
	 * @param hasten 催单信息
	 * @return
	 */
	public int saveHastenSheet(HastenSheetInfo hasten);
	
	/**
	 * 根据工单号删除对应的催单
	 * @param sheetId 工单号
	 * @return
	 */
	public int delHastenSheet(String orderId,Integer month);
	
	/**
	 * 向催单历史表中插入数据
	 * @param hasten 催单信息
	 * @return
	 */
	public int savHastenSheetInfoHis(String orderId,Integer month);
	
	/**
	 * 根据服务单号查询催单信息
	 * @param orderId
	 * @param boo TRUE查询历史 FALSE查询当前
	 * @return
	 */
	public  HastenSheetInfo[] getOrderHatenInfo(String orderId,boolean boo);
	
	/**
	 * 根据不同的查询条件取得催单信息
	 * @param strWhere 带如的WHERE 条件
	 * @param boo 为TRUE 查询历史
	 * @return HastenSheetInfo
	 */
	public HastenSheetInfo[] getListHastenInfo(String strWhere,boolean boo);
	
	/**
	 * 更新工单催单数量
	 * @param sheetId
	 * @param month
	 * @return 更新数量
	 */
	public int updateSheetHastentNum(String sheetId,Integer month);

	/**
	 * 判断本次催单是否有效
	 * @param serviceOrderId 受理单号
	 * @return 1有效催单;0无效催单
	 */
	public int checkIsValid(String serviceOrderId);
	
	/**
	 * 根据受理单号、地域Id，更新地域信息
	 * @param serviceOrderId 受理单号
	 * @param oldRegion 旧地域ID
	 * @param newRegion 新地域ID
	 * @param newRegionName 新地域名
	 * @return
	 */
	public int updateRegion(String serviceOrderId, int oldRegion, int newRegion, String newRegionName);

	public int getHastenCondition(String orderId);

	public String getRefunded(String orderId);
}
