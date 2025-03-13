package com.timesontransfar.sheetCase.controller;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.customservice.worksheet.pojo.CaseDataEntity;
import com.timesontransfar.sheetCase.entity.CaseData;
import com.timesontransfar.sheetCase.entity.CaseEntity;
import com.timesontransfar.sheetCase.service.CaseService;

import net.sf.json.JSONObject;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RefreshScope
public class CaseController {

    public static final String PAGE = "currentPage";
    public static final String SIZE = "pageSize";
    @Resource
    private CaseService caseService;

    /*
	查询我的案例
	 */
    @PostMapping(value = "/sheetCase/case/getCaseData")
    public CaseDataEntity getCaseData(@RequestBody JSONObject jsonObject) {
        CaseDataEntity caseDataEntity = new CaseDataEntity();

        int pageNum = jsonObject.getInt(PAGE);
        int pageSize = jsonObject.getInt(SIZE);
        int page = pageSize * (pageNum-1);
        int i = 0;
        List<Map<String, Object>> list = new ArrayList<>();
        CaseEntity caseEntity = JSON.parseObject(JSON.toJSONString(jsonObject), CaseEntity.class);
        caseEntity.setCurrentPage(page);
        caseEntity.setPageSize(pageSize);
        if (ObjectUtils.isNotEmpty(caseEntity)) {
            list = caseService.getCaseData(caseEntity);
            i = caseService.getCaseCount(caseEntity);
        }
        caseDataEntity.setCount(i);
        caseDataEntity.setList(list);
        return caseDataEntity;
    }
    /*
	审核案例查询
	 */
    @PostMapping(value = "/sheetCase/case/getCaseDataByAudit")
    public CaseDataEntity getCaseDataByAudit(@RequestBody JSONObject jsonObject) {
        CaseDataEntity caseDataEntity = new CaseDataEntity();

        int pageNum = jsonObject.getInt(PAGE);
        int pageSize = jsonObject.getInt(SIZE);
        int page = pageSize * (pageNum-1);
        int i = 0;
        List<Map<String, Object>> list = new ArrayList<>();
        CaseEntity caseEntity = JSON.parseObject(JSON.toJSONString(jsonObject), CaseEntity.class);
        caseEntity.setCurrentPage(page);
        caseEntity.setPageSize(pageSize);
        if (ObjectUtils.isNotEmpty(caseEntity)) {
            list = caseService.getCaseDataByAudit(caseEntity);
            i = caseService.getCaseByAuditCount(caseEntity);
        }
        caseDataEntity.setCount(i);
        caseDataEntity.setList(list);
        return caseDataEntity;
    }

    /*
    获取所有地市
     */
    @PostMapping(value = "/sheetCase/case/getCityCode")
    public List<Object> getCityCode(){
        return caseService.getCityCode();
    }

    /*
    查询某个案例的详细信息
     */
    @PostMapping(value = "/sheetCase/case/searchCase")
    public List<Map<String,Object>> searchCase(@RequestBody JSONObject jsonObject){
        String caseId = jsonObject.optString("orderId");
        return caseService.searchCase(caseId);
    }

    /*
	查询案例库
	 */
    @PostMapping(value = "/sheetCase/case/getCase")
    public CaseDataEntity getCase(@RequestBody JSONObject jsonObject){
        CaseDataEntity caseDataEntity = new CaseDataEntity();
        int pageNum = jsonObject.getInt(PAGE);
        int pageSize = jsonObject.getInt(SIZE);
        int page = pageSize * (pageNum -1);
        CaseEntity caseEntity = JSON.parseObject(JSON.toJSONString(jsonObject), CaseEntity.class);
        caseEntity.setCurrentPage(page);
        caseEntity.setPageSize(pageSize);
        if (ObjectUtils.isNotEmpty(caseEntity)) {
            caseDataEntity = caseService.getCase(caseEntity);
        }
        return caseDataEntity;
    }


    /*
    新增案例
     */
    @PostMapping(value = "/sheetCase/case/saveCase")
    public String enterJudgeJob(@RequestBody JSONObject jsonObject) {
        CaseEntity sheetCase=(CaseEntity) JSONObject.toBean(JSONObject.fromObject(jsonObject),CaseEntity.class);
        return caseService.saveCase(sheetCase);
    }

    /*
    审核案例
     */
    @PostMapping (value = "/sheetCase/put/status")
    public String updateStatus(@RequestBody JSONObject jsonObject) {
        return caseService.updateCase(jsonObject);
    }

    /*
    修改案例
     */
    @PostMapping (value = "/sheetCase/put/putCase")
    public String updateCase(@RequestBody JSONObject jsonObject) {
        return caseService.updateSheetCase(jsonObject);
    }

    /*
    删除案例
     */
    @PostMapping(value = "/sheetCase/delete/deleteCase")
    public String updateCase(@RequestParam("caseId") String caseId) {
        return caseService.deleteCase(caseId);
    }

    /*
    停用案例
     */
    @PostMapping(value = "/sheetCase/delete/endCase")
    public String endCase(@RequestBody JSONObject jsonObject) {
        String caseId = jsonObject.optString("caseId");
        String logonName = jsonObject.optString("logonName");
        return caseService.endCase(caseId,logonName);
    }

    /*
    根据单号获取数据
     */
    @PostMapping(value = "/sheetCase/get/caseData")
    public CaseData caseData(@RequestBody String orderId){
        return caseService.caseData(orderId);
    }
}
