package com.timesontransfar.customservice.workFlowOrg.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
@SuppressWarnings("rawtypes")
public class WorkSheetRuleRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		WorkSheetRule bean = new WorkSheetRule();
		bean.setRuleId(rs.getInt("RULE_ID"));
		bean.setReuleName(rs.getString("RULE_NAME"));
		bean.setReuleDesc(rs.getString("RULE_DESC"));
		bean.setRuleType(rs.getInt("RULE_TYPE"));
		bean.setPkgEffDate(rs.getString("PKG_EFF_DATE"));
		bean.setPkgExpDate(rs.getString("PKG_EXP_DATE"));		
		return bean;
	}

}
