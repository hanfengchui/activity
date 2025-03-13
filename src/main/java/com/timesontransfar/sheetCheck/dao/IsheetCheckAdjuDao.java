/**
 * @author 万荣伟
 */
package com.timesontransfar.sheetCheck.dao;

import com.timesontransfar.sheetCheck.pojo.SheetCheckAdju;

/**
 * @author 万荣伟
 *
 */
public interface IsheetCheckAdjuDao {

	/**
	 * 保存工单质检评判标准
	 * @param bean 评判对象
	 * @param checkId质检流水
	 * @return
	 */
	public int saveSheetCheckAdjuDao(SheetCheckAdju[] bean,String checkId);
	
	/**
	 * 得到质检评判标准
	 * @param checkId
	 * @return
	 */
	public SheetCheckAdju[] getSheetCheckAdju(String checkId);
	
	/**
	 * 删除质检模板信息
	 * @param checkId
	 * @return
	 */
	public int deleteSheetCheckAdju(String checkId);
	
	/**
	 * 质检模板信息移入历史表
	 * @param checkId
	 * @return
	 */
	public int moveSheetCheckAdjuToHis(String checkId);
	
	/**
	 * 得到质检评判标准历史
	 * @param checkId
	 * @return
	 */
	public SheetCheckAdju[] getSheetCheckAdjuHis(String checkId,int checkEdition) ;
}
