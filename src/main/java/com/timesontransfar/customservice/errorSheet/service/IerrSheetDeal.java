package com.timesontransfar.customservice.errorSheet.service;

/**
 * 提供错单确认、错单申诉处理的处理方法
 * 
 * @author LiJiahui
 */
public interface IerrSheetDeal {

	/**
	 * 错单确认方法
	 * 
	 * @date 2011-11-9 LiJiahui修改
	 * @param orderId
	 *            错单受理号
	 * @param sheetId
	 *            错单工单号
	 * @param monthFlag
	 *            月分区
	 * @param errInfo
	 *            错单确认信息
	 * @return 处理结果
	 */
	public String submitErrSheet(String orderId, String sheetId, int monthFlag,
			String[] errInfo);

	/**
	 * 错单申诉处理方法
	 * 
	 * @date 2011-11-9 LiJiahui修改
	 * @param orderId
	 *            错单受理号
	 * @param sheetId
	 *            错单工单号
	 * @param errFlag
	 *            是否错单
	 * @param suredMsg
	 *            审批意见
	 * @return 处理结果
	 */
	public String submitErrAuditSheet(String orderId, String sheetId, boolean errFlag, String suredMsg);
}
