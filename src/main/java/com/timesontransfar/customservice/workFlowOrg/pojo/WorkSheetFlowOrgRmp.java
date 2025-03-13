/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public class WorkSheetFlowOrgRmp implements RowMapper{

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		WorkSheetFlowOrg bean = new WorkSheetFlowOrg();
		bean.setWsFlowRuleId(rs.getInt("WS_FLOW_RULE_ID"));
		bean.setWsNbr(rs.getInt("WS_NBR"));
		bean.setWorksheetSchemaId(rs.getInt("WORKSHEET_SCHEMA_ID"));
		bean.setRegionId(rs.getInt("REGION_ID"));
		bean.setRuleId(rs.getInt("RULE_ID"));
		bean.setItemId(rs.getInt("ITEM_ID"));
		bean.setItemVaule(rs.getString("ITEM_VALUE"));
		bean.setFlowOrgid(rs.getString("FLOW_ORG_ID"));
		bean.setTachId(rs.getInt("TACHE_ID"));
		return bean;
	}

}
