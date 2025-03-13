package com.timesontransfar.customservice.worksheet.dao;

import net.sf.json.JSONObject;

public interface ItsDealQualitative {

	/**
	 * 部门处理定性历史表添加记录
	 * @param servid 定单号
	 * @param sheetid 工单号
	 * @param month 月分区
	 * @return
	 */
	public int saveDealQualitativeHis(String servid ,String sheetid ,int region);

	/**
	 * 删除部门处理定性表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteDealQualitative(String serviceOrderId ,String workSheetId,int region);	
	
	/**
	 * 预定性、终定性环节保存回单质量评价
	 * @param 工单号、环节单号、环节ID、操作时间、两级评价ID
	 * @return int
	 */
	public int saveReceiptEval(JSONObject ins);
}
