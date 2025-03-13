package com.timesontransfar.customservice.worksheet.dao;
import com.timesontransfar.customservice.worksheet.pojo.ResponsiBilityOrg;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetAuditing;

public interface ItsSheetAuditing {
	
	/**
	 * 删除责任部门表
	 * @param serviceOrderId 定单号
	 * @return
	 */
	public int deleteResponsiBilityOrg(String serviceOrderId);
	
	/**
	 * 责任部门表添加记录
	 * @param bean
	 * @return
	 */
	public int saveResponsiBilityOrg(ResponsiBilityOrg[] bean);
	
	/**
	 * 责任部门历史表添加记录
	 * @param bean
	 * @return
	 */
	public int saveResponsiBilityOrgHis(String orderId);
	
	/**
	 * 审核表添加记录
	 * @param bean
	 * @return
	 */
	public int saveTsSheetAuditing(TsSheetAuditing bean);
	/**
	 * 审核历史表添加记录
	 * @param servid
	 * @param sheetid
	 * @param month
	 * @return
	 */
	public int saveTsSheetAuditingHis(String servid,String sheetid,int region);	
	/**
	 * 得到工单审核对象
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public TsSheetAuditing getTsSheetAuditing(String sheetId,int regionId,boolean boo);
	/**
	 * 删除审核表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteTsSheetAuditing(String serviceOrderId ,String workSheetId,int region);	
	
}
