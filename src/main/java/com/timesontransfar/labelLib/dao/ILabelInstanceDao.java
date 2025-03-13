package com.timesontransfar.labelLib.dao;

import java.util.List;

import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;

@SuppressWarnings("rawtypes")
public interface ILabelInstanceDao {

    /**
     * 标签补打右边列表
     * @param begion
     * @param strWhere
     * @return
     */
    GridDataInfo lableRightGrid(int begion, String strWhere);
    /**
     * 取消标签关联
     * @param labelInstanceId
     * @return
     */
    int cancelLableById(String labelInstanceId);
    /**
     * 标签取消列表
     * @param begion
     * @param strWhere
     * @return
     */
    GridDataInfo lableCancel(int begion, String strWhere);
    /**
     * 批量保存标签实例
     * @param ls
     * @return
     */
    int saveLabelInstanceBatch(LabelInstance[] ls);

	/**
	 * 得到工单标签数量
	 * 
	 * @param orderId 服务单号
	 * @return
	 */
	int getLabelCountByOrderId(String orderId);

	/**
	 * 得到工单标签列表
	 * 
	 * @param orderId 服务单号
	 * @return
	 */
	List getLabelListByOrderId(String orderId);

	List getLabelInstanceByLabelId(String orderId, String labelId);
}