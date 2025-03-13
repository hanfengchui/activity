/**
 * <p>类名：ISheetActionInfoDao.java</p>
 * <p>功能描叙：工单动作信息数据操作接口</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	Mar 24, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.dao;

import java.util.List;

import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;

/**
 * @author wanrongwei
 *
 */
@SuppressWarnings("rawtypes")
public interface ISheetActionInfoDao {
	
	/**
	 * 通过orderid服务单号查询操作历史记录
	 */
	public List getSheetFlowAction(String orderId);
	
	/**
	 * 保存工单动作信息
	 * @param sheetActionInfo	动作信息对象	
	 * @return	保存的记录条数.
	 */
	public int saveSheetActionInfo(SheetActionInfo sheetActionInfo);
	
	/**
	 * 将一个定单下所有的工单运作保存进历史
	 * @param orderId	受理单号
	 * @return	保存成功的记录数
	 */
	public int saveSheetActionHisInfo(String orderId,Integer month);
	
	
	
	/**
	 * 删除一个工单的所有动作记录
	 * @param sheetId	工单号
	 * @return	删除的记录数
	 */
	public int delSheetActionInfo(String sheetId);
	
	/**
	 * 删除一个定单下所有工单的工运作
	 * @param orderId	定单号
	 * @return	删除的记录数
	 */
	public int delSheetActionByOrderId(String orderId,Integer month);
	
	/**
	 * 查询工单动作记录
	 * @param sheetId	工单号
	 * @param hisFlag	当前历史
	 * @return	工单动作记录对象数组
	 */
	public SheetActionInfo[] getSheetActionInfos(String sheetId, boolean hisFlag);
	
	/**
	 * 获取工单id 在actionCode动作类型的 记录列表
	 * @param sheetId
	 * @param actionCode
	 * @param hisFlag false: 当前表 true :历史表
	 * @return
	 */
	public SheetActionInfo[] getSheetActionInfos(String sheetId,int actionCode, boolean hisFlag);
	
	/**
	 * 修改地域
	 * @param serviceOrderId 受理单号
	 * @param newRegion 新地域ID
	 * @param oldRegion 旧地域ID
	 * @return
	 */
	public int updateRegion(String serviceOrderId, int newRegion, int oldRegion);

	/**
	 * 查询最近一次挂起动作类型
	 * @param workSheetId 工单号
	 * @return 动作ID
	 */
	public int queryLastActionCodeBySheetId(String workSheetId);

	/**
	 * 保存隐藏工单信息
	 * @param workSheetId 工单号
	 * @param serviceOrderId 受理单号
	 * @param opraStaff 操作员工
	 * @param endDate 隐藏到期时间
	 * @return 保存的记录条数.
	 */
	public int saveSheetHiddenAction(String workSheetId, String serviceOrderId, int opraStaff, String endDate);

	/**
	 * 保存隐藏工单信息（针对投诉，查询单）
	 * @param workSheetId 工单号
	 * @param serviceOrderId 受理单号
	 * @param opraStaff 操作员工
	 * @param endDate 隐藏到期时间
	 * @return 保存的记录条数.
	 */
	public int saveSheetHidden(String workSheetId, String serviceOrderId, int opraStaff, String endDate);

	/**
	 * 删除当前表插入历史隐藏工单信息（针对投诉，查询单）
	 * @return 保存的记录条数.
	 */
	public int saveSheetHideenHis(String serviceOrderId);

	/**
	 * 获取满足能够调账单提前终止隐藏操作
	 * @param where 查询条件
	 * @return list
	 */
	public List queryHiddenListByOpraStaff(String where);

	/**
	 * 修改隐藏状态
	 * @param hiddenState ：1、手动取消隐藏，2、自动取消隐藏
	 * @param workSheetId 工单号
	 * @return 保存成功的记录数
	 */
	public int updateSheetHiddenActionStateBySheetId(int hiddenState, String workSheetId);

	/**
	 * 修改工单隐藏状态（针对投诉、查询）
	 * @param hiddenState ：1、手动取消隐藏，2、自动取消隐藏
	 * @param workSheetId 工单号
	 * @return 保存成功的记录数
	 */
	public int updateSheetHiddenStateBySheetId(int hiddenState, String workSheetId);

	/**
	 * 将一个定单下所有的隐藏运作保存进历史
	 * @param serviceOrderId 受理单号
	 * @return 保存成功的记录数
	 */
	public int saveSheetHiddenActionHisByOrderId(String serviceOrderId);
}
