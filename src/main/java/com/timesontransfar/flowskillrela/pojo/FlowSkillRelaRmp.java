package com.timesontransfar.flowskillrela.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class FlowSkillRelaRmp implements RowMapper{

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		FlowSkillRela bean = new FlowSkillRela();
		bean.setId(rs.getString("GUID")==null?"":rs.getString("GUID"));
		bean.setFlowOrgId(rs.getString("FLOW_ORG_ID")==null?"":rs.getString("FLOW_ORG_ID"));
		bean.setFlowOrgName(rs.getString("FLOW_ORG_NAME")==null?"":rs.getString("FLOW_ORG_NAME"));
		bean.setSkillType(rs.getString("SKILL_TYPE")==null?"":rs.getString("SKILL_TYPE"));
		bean.setSkillTypeDesc(rs.getString("SKILL_TYPE_DESC")==null?"":rs.getString("SKILL_TYPE_DESC"));
		bean.setOperLogonName(rs.getString("OPER_LOGONNAME")==null?"":rs.getString("OPER_LOGONNAME"));
		bean.setOperOrgId(rs.getString("OPER_ORG_ID")==null?"":rs.getString("OPER_ORG_ID"));
		bean.setCreateTime(rs.getDate("CREATE_TIME"));
		bean.setOperType(rs.getString("OPER_TYPE"));
		bean.setModifyTime(rs.getDate("MODIFY_TIME"));
		bean.setStatus(rs.getString("STATUS"));
		bean.setServiceDate(rs.getString("SERVICE_DATE")==null?"":rs.getString("SERVICE_DATE"));
		return bean;
	}

}
