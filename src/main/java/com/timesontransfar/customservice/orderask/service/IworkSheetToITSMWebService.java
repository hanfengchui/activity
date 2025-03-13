package com.timesontransfar.customservice.orderask.service;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;

public interface IworkSheetToITSMWebService {

	/**
	 * ITSM网管系统接收10000号工单接口
	 * 
	 * @param serviceOrderId
	 * @return
	 */
	public boolean executeXML(String serviceOrderId);

	/**
	 * 10000号调用ITSM服务进行工单催单接口
	 * 
	 * @param hastenSheetInfo
	 * @return
	 */
	public boolean executeForHASTEN(HastenSheetInfo hastenSheetInfo);

	/**
	 * 10000号系统调用ITSM系统工单竣工与工单追回接口
	 * 
	 * @param work_sheet_id,cancel_flag
	 * @return
	 */
	public boolean executeForCANCEL(String work_sheet_id, String cancel_flag);

	/**
	 * 获取满足能够发送到ITSM系统的任务单
	 * 
	 * @param
	 * @return list
	 */
	public GridDataInfo getITSMList(String parm);

	/**
	 * ITSM网管系统接收10000号工单接口(重复发送)
	 * 
	 * @param workSheetId
	 * @return
	 */
	public String[] reExecuteXML(String workSheetId);
}
