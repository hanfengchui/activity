package com.timesontransfar.flowskillrela.service;

import com.timesontransfar.flowskillrela.pojo.FlowSkillRela;

public interface IFlowSkillRelaService {

	/**
	 * 修改流向部门与技能类型关系
	 * @param rela
	 * @return
	 */
	public int modifyFlowSkillRela(FlowSkillRela rela);
	
	/**
	 * 删除流向部门与技能类型关系
	 * @param id
	 * @param serviceDate
	 * @param orgId
	 * @return
	 */
	public int deleteFlowSkillRela(String id, String serviceDate,String orgId);
	
	/**
	 * 获取技能类型
	 * @param orgId
	 * @param serviceDate
	 * @return
	 */
	public String getSkillType(String orgId, String serviceDate);
	
	/**
	 * 新增流向部门与技能类型关系
	 * @param rela
	 * @return
	 */
	public int addFlowSkillRela(FlowSkillRela rela);
}
