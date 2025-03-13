package com.timesontransfar.customservice.tuschema.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

@SuppressWarnings("rawtypes")
public class ServiceContentSaveRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
        ServiceContentSave contentSave = new ServiceContentSave();
        contentSave.setServiceOrderId(rs.getString("SERVICE_ORDER_ID")==null?"":rs.getString("SERVICE_ORDER_ID"));
        contentSave.setComplaintsId(rs.getString("COMPLAINTS_ID")==null?"":rs.getString("COMPLAINTS_ID"));
        contentSave.setElementId(rs.getString("ELEMENT_ID")==null?"":rs.getString("ELEMENT_ID"));
        contentSave.setElementName(rs.getString("ELEMENT_NAME")==null?"":rs.getString("ELEMENT_NAME"));
        contentSave.setAnswerId(rs.getString("ANSWER_ID")==null?"":rs.getString("ANSWER_ID"));
        contentSave.setAnswerName(rs.getString("ANSWER_NAME")==null?"":rs.getString("ANSWER_NAME"));
        contentSave.setElementOrder(rs.getString("ELEMENT_ORDER")==null?"":rs.getString("ELEMENT_ORDER"));
        contentSave.setIsCompare(rs.getString("IS_COMPARE")==null?"":rs.getString("IS_COMPARE"));
        contentSave.setIsStat(rs.getString("IS_STAT")==null?"":rs.getString("IS_STAT"));
		return contentSave;
	}
}
