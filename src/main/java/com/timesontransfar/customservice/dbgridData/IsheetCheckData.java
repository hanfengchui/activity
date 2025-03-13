/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData;

/**
 * @author 万荣伟
 *
 */
public interface IsheetCheckData {
	/**
	 * 得到质检流水
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo getSheetCheck(int begion,String strWhere);
	/**
	 * 获取质检列表在途工单
	 * @param begion
	 * @param strWhere
	 * @return 
	 */
	public GridDataInfo queryOnRoadSheets(int begion,String strWhere) ;
	
	/**
	 * 获取质检列表竣工途工单
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryFinishSheets(int begion,String strWhere);
	
	/**
	 * 获取质检列表-申诉列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryAppealCheckSheets(int begion,String strWhere);
	
	/**
	 ** 获取质检列表-修改列表
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryUpdateCheckSheets(int begion,String strWhere);
	
}
