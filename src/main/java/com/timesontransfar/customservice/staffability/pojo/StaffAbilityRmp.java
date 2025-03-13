package com.timesontransfar.customservice.staffability.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
@SuppressWarnings("rawtypes")
public class StaffAbilityRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		//  Auto-generated method stub
		StaffAbility bean = new StaffAbility();
		bean.setGuid(rs.getString("GUID")==null?"":rs.getString("GUID"));
		bean.setStaffId(rs.getInt("STAFF_ID"));
		bean.setStaffName(rs.getString("STAFF_NAME")==null?"":rs.getString("STAFF_NAME"));
		bean.setSkillLevel(rs.getInt("SKILL_LEVEL"));
		bean.setThreshold(rs.getInt("THRESHOLD"));
		bean.setCreateStaffId(rs.getInt("CREATE_STAFF_ID"));
		bean.setCreateTime(rs.getString("CREATE_TIME"));
		bean.setUseable(rs.getInt("SS_USEABLE"));
		return bean;
	}

}
