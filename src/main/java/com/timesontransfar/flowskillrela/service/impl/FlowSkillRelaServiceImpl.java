package com.timesontransfar.flowskillrela.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.flowskillrela.dao.impl.FlowSkillRelaDaoImpl;
import com.timesontransfar.flowskillrela.pojo.FlowSkillRela;
import com.timesontransfar.flowskillrela.service.IFlowSkillRelaService;
import com.timesontransfar.staffSkill.dao.impl.StaffSkillDaoImpl;

@Component(value="flowSkillRelaService")
public class FlowSkillRelaServiceImpl implements IFlowSkillRelaService {
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private FlowSkillRelaDaoImpl flowSkillRelaDao;
	@Autowired
	private StaffSkillDaoImpl staffSkillDao;
	
	public int modifyFlowSkillRela(FlowSkillRela oldRela) {
		oldRela.setOperLogonName(this.pubFunc.getLogonStaff().getLogonName());
		oldRela.setOperOrgId(this.pubFunc.getLogonStaff().getOrganizationId());
		oldRela.setStatus("N");
		FlowSkillRela rela = new FlowSkillRela();
		rela.setId(pubFunc.crtGuid());
		rela.setFlowOrgId(oldRela.getFlowOrgId());
		rela.setFlowOrgName(oldRela.getFlowOrgName());
		rela.setSkillType(oldRela.getSkillType());
		rela.setSkillTypeDesc(oldRela.getSkillTypeDesc());
		rela.setOperLogonName(this.pubFunc.getLogonStaff().getLogonName());
		rela.setOperOrgId(this.pubFunc.getLogonStaff().getOrganizationId());
		rela.setOperType("修改");
		rela.setStatus("Y");
		rela.setServiceDate(oldRela.getServiceDate());
		flowSkillRelaDao.modifyFlowSkillRela(oldRela);
		staffSkillDao.deleteSkillsByFlowOrgId(oldRela.getFlowOrgId(),oldRela.getServiceDate());
		return flowSkillRelaDao.addFlowSkillRela(rela);
	}
	
	public int addFlowSkillRela(FlowSkillRela rela){
		int i = 0;
		rela.setId(pubFunc.crtGuid());
		rela.setOperLogonName(this.pubFunc.getLogonStaff().getLogonName());
		rela.setOperOrgId(this.pubFunc.getLogonStaff().getOrganizationId());
		rela.setOperType("新增");
		rela.setStatus("Y");

		int flowSkillByOrgId = flowSkillRelaDao.getFlowSkillByOrgId(rela.getFlowOrgId(), rela.getServiceDate());
		if (flowSkillByOrgId == 0){
			i = flowSkillRelaDao.addFlowSkillRela(rela);
		}
		return i;
	}
	
	public int deleteFlowSkillRela(String id, String serviceDate,String orgId) {
		staffSkillDao.deleteSkillsByFlowOrgId(orgId,serviceDate);
		return this.flowSkillRelaDao.deleteFlowSkillRela(id);
	}
	
	public String getSkillType(String orgId, String serviceDate){
		return this.flowSkillRelaDao.getSkillType(orgId,serviceDate);
	}

}
