/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.service;

import com.timesontransfar.customservice.worksheet.pojo.SheetTodispatch;

/**
 * @author 万荣伟
 *
 */
public interface ItsSheetSumbit {
	/**
	 * 高权限的员工批量分派工单到
	 * 
	 * @param sheetId 格式为 sheetId@regionId@month sheetId:工单号,regionId地域;month月分区
	 * @param staffId 员工ID
	 * @type 处理类型 0为分派 1为释放
	 * @return
	 */
	public String allotBatchSheet(String[] sheetId, int staffId, int type);

	/**
	 * 系统自动完结审核工单
	 * 
	 * @since 2014-03-25
	 * @param orderId 受理单编号
	 * @return SUCCESS/ERROR
	 */
	public String audSheetFinishAuto(String orderId, String logonName);

	/**
	 * 保存“非投诉单，在回到审核环节前，不选择自动办结的原因及建议转派部门”
	 * 
	 * @param todispatch
	 * @return
	 */
	public String saveDispReason(SheetTodispatch todispatch);

	public void autoDispatchSheet(String orderId, int receiveStaff, String receiveOrg, int autoPdStaff);
}