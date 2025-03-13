/**
 * <p>类名：IworkFlowBusiServ.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	Apr 10, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.service;

import java.util.Map;

/**
 * @author lifeng
 *
 */
@SuppressWarnings("rawtypes")
public interface IworkFlowBusiServ {
	/**
	 * 将工单至为完成状态
	 * @param inParam	工作流伟入的入参
	 * @return
	 */
	public Map updateSheetFinish(Map inParam);

	/**
	 * 将定单至为处理中状态
	 * @param inParam	工作流伟入的入参
	 * @return
	 */
	public Map updateOrderDealState(Map inParam);
	
	/**
	 * 将定单至为后台退回状态
	 * @param inParam	工作流伟入的入参
	 * @return
	 */
	public Map updateOrderBackState(Map inParam);

	/**
	 * 服务单以及其所有工单等信息进历史
	 * @param ipParam	工作流传入的参数
	 * @return
	 */
	public Map finishOrderAndSheet(Map inParam);

	/**
	 * 服务单以及其所有工单等信息进历史
	 * 
	 * @param orderId 订单号
	 * @return
	 */
	public Map finishOrderAndSheetByOrderId(String orderId);

	/**
	 * 根据来源工单信息生成新的工单
	 * @param inParam	工作流传入的参数
	 * @return	生成的新的工单号
	 */
	public Map crtWorkSheet(Map inParam);
	
	/**
	 * 到得到路由条件值
	 * @param inParam	工作流传入的参数
	 * @return	条件值map
	 */
	public Map getRouteInfo(Map inParam);
	
	/**
	 * 更新部门处理单路由条件值
	 * @param inParam	工作流传入的参数
	 * @return	条件值map
	 */
	public Map updateDealSheetRouteInfo(Map inParam);
	
	/**
	 * 审核环节出方法
	 * @param inParam	工作流传入的参数
	 * @return	条件值map
	 */
	public Map updateAuitSheetFinish(Map inParam);
	
	/**
	 *审核环节入方法调用,生成审核工单
	 * @param inParam	来源工单相关信息map
	 * @return
	 */
	public Map crtAudWorkSheet(Map inParam);

}
