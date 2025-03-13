/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.service;

import java.util.List;

import com.timesontransfar.customservice.worksheet.pojo.SheetReadRecordInfo;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface ItsSheetDealAction {
	
	/**
	 * 得到投诉工单的处理流水数
	 * @param orderId 定单号
	 * @param regionId 地域
	 * @param boo true查询当前 false 查询历史
	 * @return
	 */
	public int getTsdealFlowCount(String orderId, int regionId, boolean boo);
	
	public List getTsdealFlowObjNew(String orderId,int regionId,boolean boo);

	/**
	 * 高权限的员工批量分派工单到
	 * @param sheetId 格式为 sheetId@regionId@month;sheetId@regionId@month sheetId:工单号,regionId地域;month月分区
	 * @param staffId 员工ID
	 * @param type 处理类型 0为分派 1为释放
	 * @return
	 */
	public String allotBatchSheet(String[] sheetId,int staffId,int type);
	/**
	 * 高权限的员工批量分派工单到
	 * @param sheetIds[] 格式为 sheetId@regionId@month;sheetId@regionId@month sheetId:工单号,regionId地域;month月分区
	 * @param staffId[]  格式为id;id id:员工ID
	 * @return
	 */
	public String allotBatchSheet(String[] sheetIds,String[] staffId);
	/**
	 * 得到工单录音文件
	 * @param orderId 定单号
	 * @param region 地域
	 * @param boo true 查询当前  false 查询历史
	 * @param fileType 下载录音类型 0为老录音下载,1为新录音下载
	 * @return
	 */
	public String getRecordFile(String orderId,int region,boolean boo,int fileType);
	/**
	 * 得到工单录音文件
	 * @param flowId 录音流水号
	 * @return
	 */
	public String getVoiceFile(String flowId);
	/**
	 * 根据审核工单或审批工单得相关联的完成工单
	 * @param sheetId
	 * @param regionId
	 * @return
	 */
	public List getRelatingSheet(String sheetId,int regionId);
	
	/**
	 * 得到工单状态审批数据
	 * @param sheetId 工单号
	 */
	public List getSheetStatuAud(String sheetId);
	
	/**
	 * 保存员工读取工单记录
	 * @param bean
	 * @return
	 */
	public String saveSheetRead(SheetReadRecordInfo bean);

	// 业务工单监控箱强制提取
	public String forceDistillList(int staffId, String[] orders);

	/**
	 * 强制提取重复单监控箱的工单
	 * */
	String flowToEndForceDistill(int staffId, String[] orders);

	/*
	 * 校验当前登录员工是否属于一跟到底配置部门
	 */
	public int checkFlowToEndConfigOrg();

	/*
	 * 根据单号查询一跟到底前单和后单数量之和
	 */
	public int getFlowToEndCountByOrderId(String orderId);

	/*
	 * 根据单号查询一跟到底前单和后单信息
	 */
	public List getFlowToEndListByOrderId(String orderId);
}