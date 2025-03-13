/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.dao;

import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;

/**
 * @author 万荣伟
 *
 */
public interface IworkSheetAllotRealDao {
	
	/**
	 * 将工单保存到派单关系表中
	 * @param workSheetAllotReal 派单关系对象
	 * @param boo true保存到当前表中 false 保存到历史表中
	 * @return 返回保存记录条数
	 */
	public int saveWorkSheetAllotReal(WorkSheetAllotReal workSheetAllotReal,boolean boo);
	
	/**
	 * 更新派单关系当前表中审批标示和处理状态
	 * @param workSheetAllotReal 派单关系对象
	 * @return 返回保存记录条数
	 */
	public int updateWorkSheetAllotReal(WorkSheetAllotReal workSheetAllotReal);
	
	/**
	 * 查询派单关系表对象
	 * @param strWhere 传入SQL的WHERE条件
	 * @param boo true 查询当前派单关系表 false查询历史派单关系表
	 * @return
	 */
	public WorkSheetAllotReal[] getWorkSheetAllotReal(String strWhere,boolean boo);
	
	
	/**
	 * 从当前表中删除定单的关系表
	 * @param orderId
	 * @param month
	 * @return
	 */
	public int deleteSheetAlloReal(String orderId,Integer month);
	
	/**
	 * 根据处理工单号,得到派单关系对象
	 * @param sheetId
	 * @param month
	 * @return
	 */
	public WorkSheetAllotReal getSheetAllotObj(String sheetId,Integer month);
	
	/**
	 * 更新审核或审批工单可以处理
	 * @param checkSheet 审核或审批 单
	 * @param month 月分区
	 * @return 更新数
	 */
	public int updateCheckSheet(String checkSheet,String dealDesc,Integer month);

}
