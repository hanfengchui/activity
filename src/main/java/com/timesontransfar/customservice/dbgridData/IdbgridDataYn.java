/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData;

/**
 * @author 万荣伟
 *
 */
public interface IdbgridDataYn {
	
	/**
	 * 疑难工单工单池列表
	 * @param begin
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getWaitDealSheetYn(int begion,String strWhere);
	/**
	 * 疑难工单我的任务列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getDealingSheetYn(int begion,String strWhere);	
	/**
	 * 疑难工单我的已派发列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getAlreadySendSheetYn(int begion,String strWhere);

}
