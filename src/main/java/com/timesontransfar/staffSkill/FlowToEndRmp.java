package com.timesontransfar.staffSkill;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class FlowToEndRmp implements RowMapper {
	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		FlowToEnd bean = new FlowToEnd();
		bean.setIncrementId(rs.getInt("INCREMENT_ID"));// INCREMENT_ID
		bean.setCreateDate(rs.getString("CREATE_DATE") == null ? "" : rs.getString("CREATE_DATE"));// CREATE_DATE
		bean.setCurOrderId(rs.getString("CUR_ORDER_ID") == null ? "" : rs.getString("CUR_ORDER_ID"));// CUR_ORDER_ID
		bean.setCurSheetId(rs.getString("CUR_SHEET_ID") == null ? "" : rs.getString("CUR_SHEET_ID"));// CUR_SHEET_ID
		bean.setRegionId(rs.getInt("REGION_ID"));// REGION_ID
		bean.setProdNum(rs.getString("PROD_NUM") == null ? "" : rs.getString("PROD_NUM"));// PROD_NUM
		bean.setRelaInfo(rs.getString("RELA_INFO") == null ? "" : rs.getString("RELA_INFO"));// RELA_INFO
		bean.setOldOrderId(rs.getString("OLD_ORDER_ID") == null ? "" : rs.getString("OLD_ORDER_ID"));// OLD_ORDER_ID
		bean.setOldAcceptDate(rs.getString("OLD_ACCEPT_DATE") == null ? "" : rs.getString("OLD_ACCEPT_DATE"));// OLD_ACCEPT_DATE
		bean.setOldSheetId(rs.getString("OLD_SHEET_ID") == null ? "" : rs.getString("OLD_SHEET_ID"));// OLD_SHEET_ID
		bean.setDealStaffId(rs.getInt("DEAL_STAFF_ID"));// DEAL_STAFF_ID
		bean.setDealOrgId(rs.getString("DEAL_ORG_ID") == null ? "" : rs.getString("DEAL_ORG_ID"));// DEAL_ORG_ID
		bean.setDealOrg(rs.getString("DEAL_ORG") == null ? "" : rs.getString("DEAL_ORG"));// DEAL_ORG
		bean.setFlowType(rs.getInt("FLOW_TYPE"));// FLOW_TYPE
		bean.setCountWorkloadGuid(rs.getString("COUNT_WORKLOAD_GUID") == null ? "" : rs.getString("COUNT_WORKLOAD_GUID"));// COUNT_WORKLOAD_GUID
		bean.setCountWorkloadDate(rs.getString("COUNT_WORKLOAD_DATE") == null ? "" : rs.getString("COUNT_WORKLOAD_DATE"));// COUNT_WORKLOAD_DATE
		bean.setForceStaffId(rs.getString("FORCE_STAFF_ID") == null ? "" : rs.getString("FORCE_STAFF_ID"));// FORCE_STAFF_ID
		bean.setForceDate(rs.getString("FORCE_DATE") == null ? "" : rs.getString("FORCE_DATE"));// FORCE_DATE
		return bean;
	}
}