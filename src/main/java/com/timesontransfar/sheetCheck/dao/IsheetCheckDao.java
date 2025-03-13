/**
 * @author 万荣伟
 */
package com.timesontransfar.sheetCheck.dao;

import java.util.List;

import com.timesontransfar.sheetCheck.pojo.SheetCheckAppeal;
import com.timesontransfar.sheetCheck.pojo.SheetCheckInfo;
import com.timesontransfar.sheetCheck.pojo.SheetCheckState;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface IsheetCheckDao {
	
	/**
	 * 保存工单质检信息
	 * @param sheetCheckInfo
	 * @return
	 */
	public int saveSheetCheck(SheetCheckInfo bean);
	/**
	 * 得到质检流水ID
	 * @return
	 */
	public String getCheckId();
	
	/**
	 * 得到工单质检流水
	 * @param checkId
	 * @return
	 */
	public SheetCheckInfo getSheetCheck(String checkId);
	
	/**
	 * 保存申诉信息
	 * @param appealObj
	 * @return
	 */
	public int saveAppealContent(SheetCheckAppeal appealObj); 
	
	/**
	 * 修改工单质检信息
	 * @param sheetCheckInfo
	 * @return
	 */
	public int updateSheetCheckInfo(SheetCheckInfo bean);
	
	/**
	 * 将质检信息删除
	 * @param checkId
	 * @return
	 */
	public int deleteCheckSheet(String checkId);
	/**
	 * 将质检表信息移入历史表
	 * @param checkId
	 * @return
	 */
	public int moveCheckSheetToHis(String checkId);
	/**
	 * 保存质检状态
	 * @param appealObj
	 * @return
	 */
	public int saveCheckSheetState(SheetCheckState sheetState); 
	
	/**
	 * 得到工单质检流水历史
	 * @param checkId
	 * @return
	 */
	public List getSheetCheckHisList(String checkId);
	
	/**
	 * 根据服务单id获取质检列表
	 * @param ordId
	 * @return
	 */
	public List queryCheckBeansByOrdid(String ordId);
	
	/**
	 * 根据工单id查询质检列表
	 * @param sheetid
	 * @return
	 */
	public List queryCheckBeansBySheetid(String sheetid);
}
