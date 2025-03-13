package com.timesontransfar.labelLib.service;

import java.util.List;
import java.util.Map;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;

@SuppressWarnings("rawtypes")
public interface ILabelService {
	/**
	 * 查所有工单和服务单下的标签	
	 * @param orderId
	 * @param sheetId
	 * @return
	 */
	Map queryAllLabel(String orderId,String sheetId);

	/**
	 * 根据工单号查询所标签
	 * 
	 * @param url
	 * @param orderId
	 * @return
	 */
	public Map queryLabelBySheetId(String url, String workSheetId);

	/**
	 * 根据服务单号，页页名称查询所有已打标签
	 * 
	 * @param url
	 * @param orderId
	 * @return
	 */
	public Map queryLabelHandByOrderId(String url, String orderId);

	/**
	 * 标签补打右边列表
	 * 
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	GridDataInfo lableRightGrid(int begion, String strWhere);

	/**
	 * 取消标签关联
	 * 
	 * @param labelInstanceId
	 * @return
	 */
	int cancelLableById(String labelInstanceId);

	/**
	 * 标签取消列表
	 * 
	 * @param begion
	 * @param strWhere
	 * @return
	 */
	GridDataInfo lableCancel(int begion, String strWhere);

	/**
	 * 根据页面URL查出所有标签
	 * 
	 * @param url
	 * @return
	 */
	Map queryLabelHandByUrl(String url);

	/**
	 * 保存标签实例
	 * @param ls
	 * @return
	 */
	int saveLabelInstance(LabelInstance[] ls);
	
	/**
	 * 如果处理内容中有关键字,更新标签表 UPDATE CC_SERVICE_LABEL L SET L.UNUSUAL_FLAG = 1 WHERE L.SERVICE_ORDER_ID = ?
	 * @param content
	 * @param orderId
	 */
	void checkUnusualName(String content,String orderId);

	/**
	 * 保存标签
	 * 
	 * @param url
	 * @return
	 */
	int saveLabel(OrderAskInfo order, ServiceContent content, OrderCustomerInfo cust, LabelInstance[] ls);

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
}