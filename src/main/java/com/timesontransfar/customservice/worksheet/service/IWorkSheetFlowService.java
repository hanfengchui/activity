package com.timesontransfar.customservice.worksheet.service;

import java.util.Map;

public interface IWorkSheetFlowService {
	
	@SuppressWarnings("rawtypes")
	String submitWorkFlow(String sheetId,int quryRegion,Map otherParam);
	
	@SuppressWarnings("rawtypes")
	void submitWorkFlow(String instanceId, String nodeInstanceId, Map inParams);
}
