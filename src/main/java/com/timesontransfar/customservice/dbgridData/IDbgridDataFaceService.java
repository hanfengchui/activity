package com.timesontransfar.customservice.dbgridData;

import java.util.List;
import java.util.Map;

public interface IDbgridDataFaceService {
	
	GridDataInfo getGridData(String funId,String strWhere,int begion);
	GridDataInfo getGridDataSunli(String funId,String strWhere,int begion);
	GridDataInfo getGridDataBySize(String funId,String strWhere,int begion,int pageSize);
	GridDataInfo getBatchGridDataBySize(String funId,String strWhere,int begion,int pageSize);
	GridDataInfo queryValue(String funId,String strWhere,int begion,int pageSize);
	int deleteKey(List<String> redisKeys);
	int addRow(Map<String, String> newRowData);
	int getApportion(String staffId);
	GridDataInfo getApportionData(String staffId,String orgId,String status,String appStatus,int begin,int pageSize);
	int saveApportion(String staffId,String staffName,String orgId,String orgName,String apportionNumber);
	String updateApportion(String param);
	GridDataInfo getCallBackGridDataBySize(String funId,String strWhere,int begion,int pageSize);
}