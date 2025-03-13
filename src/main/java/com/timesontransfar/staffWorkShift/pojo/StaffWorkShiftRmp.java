package com.timesontransfar.staffWorkShift.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class StaffWorkShiftRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		StaffWorkShift bean = new StaffWorkShift();
		bean.setId(rs.getString("SWS_ID")==null?"":rs.getString("SWS_ID"));
		bean.setStaffId(rs.getInt("SWS_STAFF_ID"));
		bean.setStaffName(rs.getString("SWS_STAFF_NAME")==null?"":rs.getString("SWS_STAFF_NAME"));
		bean.setWorkTeamName(rs.getString("SWS_WORK_TEAM_NAME")==null?"":rs.getString("SWS_WORK_TEAM_NAME"));
		bean.setWorkDate(rs.getString("WSS_WORK_DATE")==null?"":rs.getString("WSS_WORK_DATE"));
		bean.setWorkShiftId(rs.getString("SWS_WORK_SHIFT_ID")==null?"":rs.getString("SWS_WORK_SHIFT_ID"));
		bean.setDesc(rs.getString("SWS_DESC")==null?"":rs.getString("SWS_DESC"));
		bean.setCreateStaffId(rs.getInt("SWS_CREATE_STAFF_ID"));
		bean.setCreateTime(rs.getString("SWS_CREATE_TIME")==null?"":rs.getString("SWS_CREATE_TIME"));
		bean.setUseable(rs.getInt("SWS_USEABLE"));
		return bean;
	}

}
