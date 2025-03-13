package com.timesontransfar.labelLib.dao;

import java.util.List;

import com.labelLib.pojo.LabelPoint;

@SuppressWarnings("rawtypes")
public interface ILabelTemplateDao {
    /**
     * 查询半自动和自动的标签
     * @return
     */
    List queryAutoRuleLable();
    /**
     * 查询标签关联属性
     * @param labelId
     * @return
     */
    List queryLabelProperty(String labelId);
    
    /**
     * 根据环节查询所有嵌入点
     * @param tacheId
     * @return
     */
    List queryPointByTacheId(int tacheId);
    /**
     * 根据工单号查询嵌入点标签
     * @param url
     * @param labelWayId
     * @param condition
     * @return
     */
    List<LabelPoint>queryLabelBySheetId(String url,String labelWayId,String condition,String orgId);
    /**
     * 根据URL查出该页面所有标签id称名称
     * @param url
     * @param labelPoint 嵌入点ID 
     * @param orgId 
     * @return
     */
    List<LabelPoint> queryLabelInsertPoint(String url,String labelPoint,String orgId);
    /**
     * 根据URL和服务单号查出该页面所有标签id称名称
     * @return
     */
    List<LabelPoint>queryLabelHandByOrderId(String url,String labelPoint,String orderId);
}