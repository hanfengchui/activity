/**
 * <p>类名：IhastenSheet.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：万荣伟>
 * <p>开发/维护历史：</p>
 * <p>  Create by  万荣伟 2008-6-13</p> 
 */
package com.timesontransfar.customservice.worksheet.service;

import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;

/**
 * @author 万荣伟
 *
 */
public interface IhastenSheet {
	
	/**
	 * 业务接口 根据工单号生成催单工单
	 * @param hasten
	 * @return
	 */
	public int sendHastenSheet(HastenSheetInfo hasten, boolean syncFlag);


	public int getHastenCondition(String orderId);

	public String getRefunded(String orderId);
	/**
	 * 业务接口 坐席商机管理平台催单
	 * @return
	 */	
	public String sendSyncHasten(String hastenJson);

	/**
	 * 根据服务单号,把当前催单信息移到历史表中并删除当前表的记录
	 * @param orderId 服务单号
	 * @return
	 */
	public int saveHastenSheetInfoHis(String orderId,Integer month);
	
	/**
	 * 提供催单集中查询
	 * @param strWhere 查询条件
	 * @param boo TRUE 为查询历史催单，false为查询在读催单
	 * @return HastenSheetInfo
	 */
	public HastenSheetInfo[] getListHasten(String strWhere,boolean boo);

	/**
	 * 微信接口催单
	 * 
	 * @param hasten
	 * @return
	 */
	public int weiXinHastenSheet(String orderId, String ucc, String newRelaInfo, int allNum, boolean invalidFlag, boolean unchangeFlag, String whoWhere);
	
	public HastenSheetInfo[] qryHastenList(String serId ,String  flag);

	/**
	 * 微信接口撤单
	 * 
	 * @param hasten
	 * @return
	 */
	public int weiXinCancelSheet(String orderId);
}