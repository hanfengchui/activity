package com.timesontransfar.flowskillrela.dao.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.impl.SystemAuthorizationWas;
import com.timesontransfar.flowskillrela.dao.IFlowSkillRelaDao;
import com.timesontransfar.flowskillrela.pojo.FlowSkillRela;

@Component("flowSkillRelaDao")
@SuppressWarnings("rawtypes")
public class FlowSkillRelaDaoImpl implements IFlowSkillRelaDao {
	private static final Logger log = LoggerFactory.getLogger(FlowSkillRelaDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jt;
	
	@Autowired
	private SystemAuthorizationWas systemAuthorization;
	
	private String modifySql="update cc_flow_skill_rela c set c.oper_logonname=?,c.oper_org_id=?,c.modify_time=NOW(),c.status=? where c.guid=?";
	private String addSql="insert into cc_flow_skill_rela (GUID, FLOW_ORG_ID, FLOW_ORG_NAME, SKILL_TYPE, SKILL_TYPE_DESC, " +
			"OPER_LOGONNAME, OPER_ORG_ID, CREATE_TIME, OPER_TYPE, MODIFY_TIME, STATUS,SERVICE_DATE) values (?,?,?,?,?,?,?,NOW(),?,NOW(),?,?)";
	private String deleteSql="update cc_flow_skill_rela c set c.status=?,c.modify_time=NOW(),c.oper_type=? where c.guid=?";
	private String querySql="select c.SKILL_TYPE from cc_flow_skill_rela c where c.status='Y' and c.flow_org_id=? AND service_date=?";
	
	public int modifyFlowSkillRela(FlowSkillRela rela) {
		int size = this.jt.update(this.modifySql, 
				rela.getOperLogonName(),
				rela.getOperOrgId(),
				rela.getStatus(),
				rela.getId()
		);
		if(log.isDebugEnabled()) {
			log.debug("修改流向部门与技能类型关系"+size+"条");
		}
		return size;
	}

	public int getFlowSkillByOrgId(String orgId, String serviceDate){
		int count = 0;
		String sql = "select count(*) relaCount from cc_flow_skill_rela a where a.FLOW_ORG_ID = ? and a.STATUS = 'Y' and a.SERVICE_DATE = ?";
		List<Map<String, Object>> maps = this.jt.queryForList(sql, orgId, serviceDate);
		if(!maps.isEmpty()){
			Map map = maps.get(0);
			try{
				count = Integer.parseInt(map.get("relaCount").toString());
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return count;
	}




	public int addFlowSkillRela(FlowSkillRela rela) {
		int size = this.jt.update(this.addSql, 
				rela.getId(),
				rela.getFlowOrgId(),
				rela.getFlowOrgName(),
				rela.getSkillType(),
				rela.getSkillTypeDesc(),
				rela.getOperLogonName(),
				rela.getOperOrgId(),
				rela.getOperType(),
				rela.getStatus(),
				rela.getServiceDate()
		);
		if(log.isDebugEnabled()) {
			log.debug("新增流向部门与技能类型关系"+size+"条");
		}
		return size;
	}
	
	public int deleteFlowSkillRela(String id) {
		int size = this.jt.update(deleteSql, "N", "删除", id);
		if(log.isDebugEnabled()) {
			log.debug("删除流向部门与技能类型关系"+size+"条");
		}
		return size;
	}
	
	public String getSkillType(String orgId, String serviceDate){
		List list = this.jt.queryForList(querySql, orgId, serviceDate);
		String skillType="";
		if(!list.isEmpty()){
			Map map = (Map)list.get(0);
			skillType = map.get("SKILL_TYPE").toString();
		}
		return skillType;
	}
	
	/**
	 * 系统自动派发维度，新增“最严工单”，“受理渠道”
	 */
	public int getSkillIdWithDate3(String orgId,String servOrderId){
		String skillType = this.getSkillType(orgId,"3");
		int skillId = 0;
		if(!"".equals(skillType)){
			String sql="select o.SERVICE_TYPE,IF(a.six_grade_catalog=0,o.rela_type,a.fou_grade_catalog)FOU_GRADE_CATALOG,d.DIFFICULTY_ID,a.BEST_ORDER,if(o.CHANNEL_DETAIL_ID=0, o.ACCEPT_CHANNEL_ID, o.CHANNEL_DETAIL_ID) ACCEPT_CHANNEL "
					+ "from cc_service_content_ask a,cc_service_order_ask o,cc_sixgrade_difficulty d where "
					+ "a.service_order_id=o.service_order_id and IF(a.six_grade_catalog=0,o.rela_type,a.six_grade_catalog)=d.sixgrade_id and a.service_order_id=?";
			List list = this.jt.queryForList(sql, servOrderId);
			if(!list.isEmpty()){
				Map map = (Map)list.get(0);
				try{
					skillId = Integer.parseInt(map.get(skillType).toString());
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return skillId;
	}

	public int getSkillIdWithDate1(String orgId, String servOrderId) {
		String skillType = this.getSkillType(orgId, "1");
		int skillId = 0;
		if (!"".equals(skillType)) {
			String sql = "SELECT SERVICE_TYPE, APPEAL_REASON_ID FROM cc_service_content_ask a WHERE a.service_order_id = ?";
			List list = this.jt.queryForList(sql,servOrderId);
			if (!list.isEmpty()) {
				Map map = (Map) list.get(0);
				try {
					skillId = Integer.parseInt(map.get(skillType).toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return skillId;
	}

	public JdbcTemplate getJt() {
		return jt;
	}

	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}

	public SystemAuthorizationWas getSystemAuthorization() {
		return systemAuthorization;
	}

	public void setSystemAuthorization(SystemAuthorizationWas systemAuthorization) {
		this.systemAuthorization = systemAuthorization;
	}
}
