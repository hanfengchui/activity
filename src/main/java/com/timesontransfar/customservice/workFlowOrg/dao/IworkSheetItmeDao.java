/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.dao;

import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRuleItem;

/**
 * @author 万荣伟
 *
 */
public interface IworkSheetItmeDao {
	
	/**
	 * 保存细类规则
	 * @param bena
	 * @return
	 */
	public int saveItmeObj(WorkSheetRuleItem bena);
	
	/**
	 * 保存模板规则和规则细类的关系
	 * @param ruleId
	 * @param itmeid
	 * @return
	 */
	public int saveRuleitmeReal(int ruleId,int itmeid);
	
	/**
	 * 根据细类ID得到模板规则ID
	 * @param itemId
	 * @return
	 */
	public int getRuleId(int itemId);

}
