package com.timesontransfar.customservice.chiefdesk;

import java.util.Map;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;

public interface IChiefOperatorDeskServiceService {
	GridDataInfo querySingleList(Map<String, String> params);
	
	GridDataInfo queryPatchList(Map<String, String> params);
	
	GridDataInfo queryChiefDesk(Map<String, String> params);
	
	String[] getPatchdspSheets(int dspnum,int servType,int tachid);
	
	String buildLevleOption(String serviceOrderId);
	
	Map<String, String> buildParams(Map<String, String> maps, int type);
}