package com.timesontransfar.common.pubFunction;

import java.util.Map;

import com.timesontransfar.tablePage.PageResVO;

import net.sf.json.JSONObject;


@SuppressWarnings("rawtypes")
public interface IPUBFunctionService {
	
	PageResVO qryData(String funcId,Map inParams);
  
	Map addOrderStr(Map param);
	
	String addAccpetStr(Map param,String funcId);
	
	Map addLinkStr(Map param,String funcId);
	
	Map addActionWhere(Map param,String funcId);
	
	String getQryClieque(Map param);

	JSONObject addFeedbackInfo(String orderId);

	String getQrySn(Map param);
	
	String getSpecialInfoStr(String tableType, Map param);
	
	String qryZDSSOToken(JSONObject body);

	PageResVO getReturnBlackList(Map inParams);
}