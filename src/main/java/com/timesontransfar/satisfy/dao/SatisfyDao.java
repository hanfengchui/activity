package com.timesontransfar.satisfy.dao;

import com.timesontransfar.evaluation.EvaluationDetail;
import com.timesontransfar.evaluation.EvaluationOrderPojo;
import com.timesontransfar.evaluation.pojo.EvaluationSheetPojo;
import com.timesontransfar.evaluation.pojo.OrganizationPojo;

import java.util.List;
import java.util.Map;

public interface SatisfyDao {
	
    EvaluationSheetPojo getSheetObj(String sheetId, boolean hisFlag);

    public EvaluationSheetPojo getLastResponseSheet(String orderId);

    OrganizationPojo getStaffOrganization(String staffId);

    int updateFetchSheetStaff(String sheetId, int staffId, String staffName, String orgId, String orgName, int status, int lockFlag);

    public int updateDealDetail(EvaluationSheetPojo evaluationSheetPojo);

    EvaluationSheetPojo getCurrentSheet(String sheetId);

    public String getAuditSheetId(String orderId);

    public int updateAuditSheet(EvaluationSheetPojo sheet);

    int finishAssginSheet(EvaluationSheetPojo evaluationSheetPojo);

    List<EvaluationSheetPojo> getSheetDetail(String orderId);

    public List<Map<String, Object>> getDealSheetFlow(String orderId, boolean hisFlag);

    public int saveSheetHisByOrderId(String orderId);

    public int saveOrderHisByOrderId(String orderId);

    public int deleteSheetByOrderId(String orderId);

    int finishSatisfyDetail(String orderId, String payReturn, String repairResult, String failedReason, String result, String comment);

    EvaluationOrderPojo getEvaluationOrder(String orderId, boolean hisFlag);

    public int deleteOrder(String orderId);
    
    public String getCpOrderId(int regionId);
    
    public int saveUnsatisfyOrder(EvaluationOrderPojo order);
    
    public int saveUnsatisfyDetail(EvaluationDetail detail);
    
    public String getCpSheetId(int regionId);
    
    public int saveUnsatisfySheet(EvaluationSheetPojo sheet);
    
    public int finishSatisfyOrder(String orderId, int isOverTime);
    
    public List<Map<String, Object>> getCallOutRecord(String orderId);
    
    public int updateCallOutRecord(EvaluationDetail detail);
    
    public EvaluationDetail getEvaluationDetail(String orderId);

    public String getSaveDealContent(String sheetId);

    public int saveStageContent(String sheetId, String content);
    
    public boolean isExistEvaluationDetail(String serviceOrderId);
}
