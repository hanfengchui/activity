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
public class WorkSheetSchemaRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		WorkSheetSchema bean = new WorkSheetSchema();
		bean.setWorksheetSchemaId(rs.getInt("WORKSHEET_SCHEMA_ID"));
		bean.setWflId(rs.getInt("WFL_ID"));
		bean.setWflSeqNbr(rs.getInt("WFL_SEQ_NBR"));
		bean.setTachId(rs.getInt("TACHE_ID"));
		bean.setWorkSheetType(rs.getInt("WORKSHEET_TYPE"));
		bean.setWorkSheetCategory(rs.getInt("WORKSHEET_CATEGORY"));
		bean.setFlowOptMethod(rs.getString("FLOW_OPT_METHOD"));
		bean.setFlowOptParam(rs.getString("FLOW_OPT_PARAM"));
		bean.setSendXml(rs.getString("SEND_XML_SCHEMA"));
		bean.setReceiveXml(rs.getString("RECEIVE_XML_SCHEMA"));
		bean.setProdConfig(rs.getInt("PROD_CONFIG_FLAG"));
		bean.setReverseWorksheetSchemaId(rs.getInt("REVERSE_WORKSHEET_SCHEMA_ID"));
		return bean;
	}

}
