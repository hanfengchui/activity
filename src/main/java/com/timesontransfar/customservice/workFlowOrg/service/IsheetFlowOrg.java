/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.service;

import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetFlowOrg;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRule;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRuleItem;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetSchema;

/**
 * @author 万荣伟
 *
 */
public interface IsheetFlowOrg {

    /**
     * 根据环节、工单类型、匹配值、地域、服务类型，查询工单的流向部门
     * 
     * @author LiJiahui
     * @date 2012-3-1
     * @param tacheId
     *            环节ID
     * @param workSheetType
     *            工单类型ID
     * @param itemVaule
     *            匹配项
     * @param regionId
     *            地域Id
     * @param serviceType
     *            服务类型Id
     * @return 返回查询到的部门Id。A表示无模板；B表示无规则；C表示无细项；D表示无流向
     */
    public String getFlowOrgId(int tacheId, int workSheetType, String itemVaule, int regionId,
            int serviceType);

    /**
     * 根据环节的工单类型和匹配值来找到对应的工位
     * 
     * @param tacheId
     *            环节ID
     * @param workSheetType工单类型
     * @param itemVaule匹配值
     * @return 返回工位ID
     */
    public String getFlowOrgId(int tacheId, int workSheetType, String itemVaule, int regionId);

    /**
     * 根据模板规则ID来找到对应的模板规则类型
     * 
     * @param ruleId
     * @return
     */
    public String getRuleType(int ruleId);

    /**
     * 保存工单模板
     * 
     * @param scheamBean
     * @param ruleBean
     * @return
     */
    public String saveSchemaRule(WorkSheetSchema scheamBean, WorkSheetRule ruleBean);

    /**
     * 保存工单细类规则
     * 
     * @param itemBean
     * @param ruleId
     * @return
     */
    public String saveItmeObj(WorkSheetRuleItem itemBean, int ruleId);

    /**
     * 保存工单流向
     * 
     * @param flowBean
     * @return
     */
    public String saveSheetFlowOrg(WorkSheetFlowOrg flowBean);

    /**
     * 更新工单流向工位
     * 
     * @param wsFlowOrg
     * @param orgId
     * @return
     */
    public String updateSheetFlowOrg(int wsFlowOrg, int orgId, int region, int ruleId);

    /**
     * 根据静态ID得到部门(SP,网厅/掌厅的投诉流程)
     * 
     * @param refId
     * @return
     */
    public String getSataticOrg(int refId);

}
