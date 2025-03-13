package com.timesontransfar.customservice.tuschema.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class ServiceContentSaveSJRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		ServiceContentSaveSJ contentSaveSJ = new ServiceContentSaveSJ();
		contentSaveSJ.setServiceOrderId(rs.getString("SERVICE_ORDER_ID") == null ? "" : rs.getString("SERVICE_ORDER_ID"));
		contentSaveSJ.setContentId(rs.getString("CONTENT_ID") == null ? "" : rs.getString("CONTENT_ID"));
		contentSaveSJ.setContentDesc(rs.getString("CONTENT_DESC") == null ? "" : rs.getString("CONTENT_DESC"));
		return contentSaveSJ;
	}
}
