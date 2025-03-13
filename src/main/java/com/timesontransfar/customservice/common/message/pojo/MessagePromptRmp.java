package com.timesontransfar.customservice.common.message.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class MessagePromptRmp implements RowMapper{
	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
		MessagePrompt m = new MessagePrompt();
		m.setGuid(rs.getString("GUID"));
		m.setStaffId(rs.getInt("STAFF_ID"));
		m.setStaffName(rs.getString("STAFF_NAME")==null?"":rs.getString("STAFF_NAME"));
		m.setOrgId(rs.getString("ORG_ID")==null?"":rs.getString("ORG_ID"));
		m.setOrgName(rs.getString("ORG_NAME")==null?"":rs.getString("ORG_NAME"));
		m.setTypeId(rs.getInt("TYPE_ID"));
		m.setTypeName(rs.getString("TYPE_NAME")==null?"":rs.getString("TYPE_NAME"));
		m.setMsgContent(rs.getString("MESSAGE_CONTENT")==null?"":rs.getString("MESSAGE_CONTENT"));
		m.setReaded(rs.getInt("READED"));
		m.setCrtDate(rs.getString("CREAT_DATE")==null?"":rs.getString("CREAT_DATE"));
		m.setReadDate(rs.getString("READED_DATE")==null?"":rs.getString("READED_DATE"));
		m.setUrlAddr(rs.getString("URL_ADDR")==null?"":rs.getString("URL_ADDR"));
		return m;
	}
}
