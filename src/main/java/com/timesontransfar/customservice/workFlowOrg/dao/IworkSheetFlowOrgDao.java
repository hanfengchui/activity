/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.dao;

import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetFlowOrg;

/**
 * @author 万荣伟
 *
 */
public interface IworkSheetFlowOrgDao {
	
	/**
	 * 根据工单模板,环节标志,地域,环节,规则标志得到匹配对象
	 * @param sheetFlowOrg
	 * @return 数组对象
	 */
	public WorkSheetFlowOrg[] getFlowOrgId(WorkSheetFlowOrg sheetFlowOrg);
	
	/**
	 * 保存工单流向对象
	 * @param bean
	 * @return
	 */
	public int saveflowOrgObj(WorkSheetFlowOrg bean);
	
	/**
	 * 修改流向工位
	 * @param wsFlowruleId
	 * @param orgId
	 * @return
	 */
	public int updateFlowOrg(int wsFlowruleId,int orgId,int region);
	
	/**
	 * 根据静态ID得到部门(SP,网厅/掌厅的投诉流程)
	 * @param refId
	 * @return
	 */
	public String getSataticOrg(int refId);

}
