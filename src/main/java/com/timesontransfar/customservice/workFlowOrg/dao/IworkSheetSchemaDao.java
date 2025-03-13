/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.dao;

import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetSchema;

/**
 * @author 万荣伟
 *
 */
public interface IworkSheetSchemaDao {

    /**
     * @author LiJiahui
     * @date 2012-3-1
     * @param tacheId
     *            环节ID
     * @param workSheetType
     *            工单类型ID
     * @param serviceType
     *            服务类型ID。如果不需要匹配服务类型，那么调用该方法时，请将该参数置为与workSheetType相同的值
     * @return 查询结果
     */
    public WorkSheetSchema getFlowSchema(int tacheId, int workSheetType, int serviceType);

    /**
     * 根据环节ID和工单类型ID得到工单模板
     * 
     * @param tacheId
     *            环节ID
     * @param workSheetType
     *            工单类型
     * @return
     */
    public WorkSheetSchema getFlowSchema(int tacheId, int workSheetType);

    /**
     * 根据模板对象保存工单模板
     * 
     * @param bean
     * @return
     */
    public int saveFlowSchema(WorkSheetSchema bean);

    /**
     * 根据模板规则ID得到模板对象
     * 
     * @param ruleId
     * @return
     */
    public WorkSheetSchema getRuleFlowObj(int ruleId);

}
