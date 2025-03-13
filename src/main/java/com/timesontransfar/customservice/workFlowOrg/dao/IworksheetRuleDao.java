/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.dao;

import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRule;

/**
 * @author 万荣伟
 *
 */
public interface IworksheetRuleDao {
	
	/**
	 * 根据工单模板ID得到规则类型
	 * @param worksheetSchemaId
	 * @return
	 */
	public WorkSheetRule getSheetFlowRule(int worksheetSchemaId);
	
	/**
	 * 根据模板规则ID得到模板规则对象
	 * @param ruleId 模板规则ID
	 * @return
	 */
	public WorkSheetRule getItemRule(int ruleId);
	
	/**
	 * 保存工单模板规则对象
	 * @param bean
	 * @return
	 */
	public int saveRuleObj(WorkSheetRule bean);
	
	/**
	 * 保存工单模板和模板规则关联表
	 * @param schemaId
	 * @param ruleId
	 * @return
	 */
	public int saveReleSchemaReal(int schemaId,int ruleId);
	
	/**
	 * 当更新流向部门时,更新模板规则时间
	 * @param ruleId
	 * @return
	 */
	public int updateRuleDate(int ruleId);

}
