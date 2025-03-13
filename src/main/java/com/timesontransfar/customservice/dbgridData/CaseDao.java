package com.timesontransfar.customservice.dbgridData;

import com.timesontransfar.customservice.worksheet.pojo.CaseDataEntity;
import com.timesontransfar.sheetCase.entity.CaseEntity;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public interface CaseDao {

    /*
    查询对应的地级市和部门名称
     */
    Map<String, Object> getCreator(String creator);

    /*
    新增案例
     */
    int addCase(CaseEntity sheetCase);

    /*
    修改审核状态
     */
    int updateCase(String status, JSONObject jsonObject, String orgName, String caseDetail, String level, String notPassCause, String caseReview);

    /*
    修改案例状态/亮点/差错点
     */
    int updateSheetCase(String level,String caseId, String caseDetail, String status);

    List<Map<String, Object>> getCaseData(CaseEntity caseEntity);

    List<Map<String, Object>> getCaseDataByAudit(CaseEntity caseEntity);

    /*
    删除案例
     */
    int deleteCase(String caseId);

    int endCase(String caseId,String logonName);

    boolean getLevel(String caseId,String logonName);

    CaseDataEntity getCase(CaseEntity caseEntity);

    List<Object> getCityCode();

    List<Map<String,Object>> searchCase(String caseId);

    int getCaseCount(CaseEntity caseEntity);

    int getCaseByAuditCount(CaseEntity caseEntity);

    /*
    查询案例级别和案例状态
     */
    Map<String, Object> getLevelStatus(String caseId);

    String getTsReasonName(String orderId, boolean hisFlag);

    /*
    根据服务单号查询案例是否存在
     */
    int getCaseNum(String orderId);

    //审核通过
    int updatePass(String caseId, String approver, String orgName, String caseDetail, String level,String caseReview);

    int getCaseCountS(CaseEntity caseEntity);
}
