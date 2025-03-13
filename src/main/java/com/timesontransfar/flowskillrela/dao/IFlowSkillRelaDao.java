package com.timesontransfar.flowskillrela.dao;

import com.timesontransfar.flowskillrela.pojo.FlowSkillRela;

public interface IFlowSkillRelaDao {

	/**
	 * 修改流向部门与技能类型关系
	 * @param rela
	 * @return
	 */
	public int modifyFlowSkillRela(FlowSkillRela rela);
	
	/**
	 * 新增流向部门与技能类型关系
	 * @param rela
	 * @return
	 */
	public int addFlowSkillRela(FlowSkillRela rela);
	
	/**
	 * 删除流向部门与技能类型关系
	 * @param id
	 * @return
	 */
	public int deleteFlowSkillRela(String id);
	
	/**
	 * 获取技能类型
	 * @param orgId
	 * @param serviceDate
	 * @return
	 */
	public String getSkillType(String orgId, String serviceDate);
	
	/**
	 * 获取技能ID
	 * @param orgId
	 * @param servOrderId
	 * @return
	 */
	public int getSkillIdWithDate3(String orgId,String servOrderId);

	/**
	 * 获取技能ID
	 * @param orgId
	 * @param servOrderId
	 * @return
	 */
	public int getSkillIdWithDate1(String orgId,String servOrderId);
}
