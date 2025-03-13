package com.timesontransfar.workshift.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class WorkShiftRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		WorkShift bean = new WorkShift();
		bean.setId(rs.getString("WS_ID")==null?"":rs.getString("WS_ID"));
		bean.setName(rs.getString("WS_NAME")==null?"":rs.getString("WS_NAME"));
		bean.setTime(rs.getString("WS_TIME")==null?"":rs.getString("WS_TIME"));
		bean.setPercent(rs.getInt("WS_PERCENT"));
		bean.setDesc(rs.getString("WS_DESC")==null?"":rs.getString("WS_DESC"));
		bean.setCreateStaffId(rs.getInt("WS_CREATE_STAFF_ID"));
		bean.setCreateTime(rs.getDate("WS_CREATE_TIME"));
		bean.setUseable(rs.getInt("WS_USEABLE"));
		return bean;
	}

}
