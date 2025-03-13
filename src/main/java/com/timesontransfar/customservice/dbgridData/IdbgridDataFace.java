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
public interface IdbgridDataFace {
	
	public GridDataInfo getGridDataBySize(int begion,int pageSize,String strWhere,String funId);

	public GridDataInfo getBatchGridDataBySize(int begion,int pageSize,String strWhere,String funId);
	
	/**
	 * 得到列表数据
	 * @param begion 开始点
	 * @param strWhere 
	 * @param funId
	 * @return
	 */
	public GridDataInfo getGridData(int begion,String strWhere,String funId);

	public GridDataInfo queryValue(String funId, String strWhere, int begion, int pageSize);

	public int deleteKey (List<String> redisKeys);

	int addRow(Map<String, String> newRowData);

	int getApportion(String staffId);

	GridDataInfo getApportionData(String staffId,String orgId,String status,String appStatus,int begin,int pageSize);

	int saveApportion(String staffId,String staffName,String orgId,String orgName,String apportionNumber);

	String updateApportion(String param);


	
}
