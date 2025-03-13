/**
 * @author 万荣伟
 */
package com.timesontransfar.sheetCheck.dao;

import java.util.List;

import com.timesontransfar.sheetCheck.pojo.QualityContentSave;
import com.timesontransfar.sheetCheck.pojo.QualitySheet;
import com.timesontransfar.sheetCheck.pojo.SheetCheckSchem;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface IsheetCheckSchemDao {
	
	/**
	 * 保存质检模板
	 * @param bean
	 * @return
	 */
	public int saveCheckSchem(SheetCheckSchem bean);
	
	/**
	 * 更新模板
	 * @param bean
	 * @return
	 */
	public int updateCheckSchem(SheetCheckSchem bean);
	
	/**
	 * 根据质检类型得到质检模板
	 * @param typeId
	 * @return
	 */
	public List getSheetCheckSchem(int typeId);
	
	public List getTemplateList(int tacheId);
	
	public int saveQualitySheet(QualitySheet sheet);
	
	public List getRowListByTemplateId(String templateId);
	
	public List getEleListByRowId(String rowId);
	
	public int insertQualityContentSave(final List<QualityContentSave> saveList);
	
	public int updateQualitySheet(QualitySheet sheet);
	
	public List getEleAnswerListByRowId(String sheetId, String rowId);
	
	public int saveQualityAppeal(QualitySheet sheet);
	
	public int saveQualityApprove(QualitySheet sheet);
	
	public int deleteQualityContentSave(String sheetId);

}
