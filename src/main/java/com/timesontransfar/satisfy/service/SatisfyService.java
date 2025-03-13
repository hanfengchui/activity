package com.timesontransfar.satisfy.service;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.evaluation.EvaluationOrderPojo;
import com.timesontransfar.evaluation.PerceptionInfo;
import com.timesontransfar.evaluation.SatisfyInfo;
import com.timesontransfar.evaluation.pojo.EvaluationSheetPojo;

import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface SatisfyService {
	
	String createSatisfyOrder(SatisfyInfo info);

	String createPerceptionOrder(PerceptionInfo info);

    GridDataInfo getSheetPoolList(String param);

    GridDataInfo getMyTaskList(String param);
    
    public GridDataInfo getReturnPool(String param);

    List<String> fetchBatchWorkSheet(String param);

    List<String> allotBatchWorkSheet(String param);

    public String dispatchAssignSheet(String param);
    
    public String submitAssignSheet(String param);
    
    public String submitDealSheet(String param);
    
    public String dispatchDealSheet(String param);
    
    public EvaluationSheetPojo getLastResponseSheet(String param);

    public List<Map<String, Object>> getDealSheetFlow(String param);
    
    EvaluationOrderPojo getEvaluationOrder(String orderId);
    
    public JSONObject getUnSatisfyInfo(String orderId, boolean hisFlag, String sheetId);

    public JSONObject saveStageContent(String sheetId, String content, String orderId);

    public GridDataInfo getOrderList(String param);
}
