package com.timesontransfar.customservice.worksheet.dao;
import java.util.List;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitativeGrid;

@SuppressWarnings("rawtypes")
public interface ItsSheetQualitative {
	/**
	 * 投诉定性表添加记录
	 * @param bean
	 * @return
	 */
	public int saveTsSheetQualitative(TsSheetQualitative bean);
	
	public int updateOutlets(String orderId);
		
	public int updateOrderChannel(String orderId);
	
	/**
	 * 投诉定性表添加记录
	 * @param servid
	 * @param sheetid
	 * @param month
	 * @return
	 */
	public int saveTsSheetQualitativeHis(String servid,String sheetid ,int region);
	/**
	 * 得到工单定性对象
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public TsSheetQualitative[] getTsSheetQualitative(String sheetId,int regionId,boolean boo);

	/**
	 * 得到工单定性和回访对象
	 * @param orderId 服务单号
	 * @param boo     true 查询当前 false 查询历史
	 * @return
	 */
	public List getQualitativeAndVisit(String orderId, boolean boo);

	/**
	 * 得到工单定性或回访对象
	 * 
	 * @param orderId 服务单号
	 * @param boo     true 查询当前 false 查询历史
	 * @return
	 */
	public List getQualitativeOrVisit(String orderId, boolean boo, boolean cliqueFlag);

	/**
	 * 得到工单操作日志
	 * 
	 * @param orderId 服务单号
	 * @param boo     true 查询当前 false 查询历史
	 * @return
	 */
	public List getOrderOperationLogs(String orderId, boolean boo);

	/**
	 * 根据订单ID查询最近一条定性记录
	 * @param orderId 订单号
	 * @param regionId 地域
	 * @return
	 */
	public TsSheetQualitative getLatestQualitativeByOrderId(String orderId,int regionId);

	public TsSheetQualitative getLatestQualitativeHisByOrderId(String orderId);

    /**
     * 根据订单ID，获取该订单的所有定性记录(含一级责任目录、定性情况描述)
     * @param serviceID 订单ID
     * @return 查询得到的结果
     */
    public List getOrderQualitative(String serviceID);
    
    public List getOrderQualitativeHis(String serviceID);
	
	/**
	 * 删除投诉定性表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteTsSheetQualitative(String serviceOrderId ,String workSheetId,int region);

	public String getReceiptEvalObj(String orderId);

	/**
	 * 判断当前登录工号所属哪个查询区域，仅可查询该工号所在的查询区域内最后处理的近30天工单。
	 * 
	 * @param orderId
	 * @param startTime
	 * @param endTime
	 * @param orgId
	 * @return
	 */
	public GridDataInfo selectSheetQualitativeGrid(String orderId, String startTime, String endTime, String orgId, String begin, String pageSize);

	/**
	 * 根据服务单号修改服务入网格
	 * 
	 * @param TsSheetQualitativeGrid
	 * @return
	 */
	public int insertSheetQualitativeGrid(TsSheetQualitativeGrid sqg);
}