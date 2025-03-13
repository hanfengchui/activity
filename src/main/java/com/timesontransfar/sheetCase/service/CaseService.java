package com.timesontransfar.sheetCase.service;

import com.timesontransfar.customservice.worksheet.pojo.CaseDataEntity;
import com.timesontransfar.sheetCase.entity.CaseData;
import com.timesontransfar.sheetCase.entity.CaseEntity;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface CaseService {

    List<Map<String, Object>> getCaseData(CaseEntity caseEntity);

    List<Map<String, Object>> getCaseDataByAudit(CaseEntity caseEntity);

    CaseDataEntity getCase(CaseEntity caseEntity);

    List<Map<String,Object>> searchCase(String caseId);

    List<Object> getCityCode();

    int getCaseCount(CaseEntity caseEntity);

    int getCaseByAuditCount(CaseEntity caseEntity);


    /*
    新增案例
     */
    String saveCase(CaseEntity sheetCase);

    /*
    审核案例
     */
    String updateCase(JSONObject jsonObject);

    /*
    修改案例
     */
    String updateSheetCase(JSONObject jsonObject);

    /*
    删除案例
     */
    String deleteCase(String caseId);

    String endCase(String caseId,String logonName);

    /*
   根据单号获取数据
    */
    CaseData caseData(String orderId);
}
