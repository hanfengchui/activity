/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData;

import java.util.List;
import java.util.Map;

/**
 * @author 万荣伟
 *
 */
public interface IdbgridDataTs {
	/**
	 * 取得受理页面查询历史信息
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getAcceptHistory(int begion,String strWhere);
	/**
	 * 取得申诉确认列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getAppealSheet(int begion,String strWhere);
	/**
	 * 取得申诉超时列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getAppealTimeOutSheet(int begion,String strWhere);
	/**
	 * 取得待考核列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getWaitQualifySheet(int begion,String strWhere);
	/**
	 * 取得考核中列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getQualifingSheet(int begion,String strWhere);
	
	/**
	 * 投诉工单工单池列表
	 * @param begin
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getWaitDealSheetTs(int begion,int pageSize,String strWhere);

	/**
	 * 人工批量分派工单池列表
	 * @param begin
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getBatchWaitDealSheetTs(int begion,int pageSize,String strWhere);


	/**
	 * 人工批量分派我的任务列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getBatchDealingSheetTs(int begion,int pageSize,String strWhere);





	/**
	 * redis数据列表
	 * @param begin
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryValue(int begion,int pageSize,String strWhere);


	/**
	 * 删除redis数据
	 * @param redisKeys
	 * @return
	 */
	public int deleteKey(List<String> redisKeys);


	/**
	 * 新增数据
	 * @param newRowData
	 * @return
	 */
	int addRow(Map<String, String> newRowData);

	int getApportion(String staffId);

	GridDataInfo getApportionData(String staffId,String orgId,String status,String appStatus,int begin,int pageSize);

	int saveApportion(String staffId,String staffName,String orgId,String orgName,String apportionNumber);

	String updateApportion(String param);

	/**
	 * 投诉工单我的任务列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getDealingSheetTs(int begion,int pageSize,String strWhere);

	/**
	 * 投诉工单我的任务列表（关联查询预约回复）
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getCallBackDealingSheetTs(int begion,int pageSize,String strWhere);
	
	public GridDataInfo getSimpleDealingSheetTs(int begion, int pageSize, String strWhere);
	
	public GridDataInfo getDealingSheetTs(int begion, int pageSize, String strWhere, Map<String, String> orgMap);
	
	/**
	 * 投诉工单我的已派发列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getAlreadySendSheetTs(int begion,String strWhere);
	
	/**
	 * 预受理商机处理，工单池列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getSheetPoolSJ(int begion,String strWhere);
	
	
	
	
	
	
	/**
	 * 得到投诉建议强制释放列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getForceReleaseTs(int begion,String strWhere) ;
	
	/**
	 * monitorOrderInfo.jsp
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getMonitorOrderInfo(int begion, String strWhere);
	
	/**
	 *获取政企传真单列表数据
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getZqSheet(int begion, String strWhere);
	
	/**
	 *获取处理工单分派列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getDealPatchData(int begion, String strWhere);
	
	/**
	 *获取疑难处理工单列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getYNDealData(int begion, String strWhere);
	
	/**
	 *获取预受理暂存工单
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getZCOrderData(int begion, String strWhere);
	
	/**
	 *获取预受理退回工单
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getBackOrderData(int begion, String strWhere);
	
	/**
	 *获取投诉特殊客户列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getTSSpeciaData(int begion, String strWhere);
	
}
