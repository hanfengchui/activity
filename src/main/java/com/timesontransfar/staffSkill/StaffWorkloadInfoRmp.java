package com.timesontransfar.staffSkill;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class StaffWorkloadInfoRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		StaffWorkloadInfo bean = new StaffWorkloadInfo();
		bean.setGuid(rs.getString("GUID")==null?"":rs.getString("GUID"));
		bean.setStaffId(rs.getInt("STAFF_ID"));
		bean.setWsId(rs.getString("WS_ID")==null?"":rs.getString("WS_ID"));
		bean.setSkillLevel(rs.getInt("SKILL_LEVEL"));
		bean.setStartMoment(rs.getString("START_MOMENT")==null?"":rs.getString("START_MOMENT"));
		bean.setEndMoment(rs.getString("END_MOMENT")==null?"":rs.getString("END_MOMENT"));
		bean.setThreshold(rs.getInt("THRESHOLD"));
		bean.setState(rs.getInt("STATE"));
		bean.setCurRate(rs.getDouble("CUR_RATE"));
		bean.setCurWorkload(rs.getInt("CUR_WORKLOAD"));
		try {
			rs.findColumn("ORG_ID");
			bean.setOrgId(rs.getString("ORG_ID"));
		} catch (SQLException e) {
			bean.setOrgId("");
		}
		return bean;
	}
}