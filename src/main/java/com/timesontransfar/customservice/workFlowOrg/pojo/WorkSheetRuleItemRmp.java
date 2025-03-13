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
public class WorkSheetRuleItemRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		WorkSheetRuleItem bean = new WorkSheetRuleItem();
		bean.setItemId(rs.getInt("ITEM_ID"));
		bean.setItemName(rs.getString("ITEM_NAME"));
		bean.setItemDesc(rs.getString("ITEM_DESC"));
		bean.setEntiyId(rs.getInt("ENTITY_ID"));
		bean.setAttributeId(rs.getInt("ATTRIBUTE_ID"));		
		return bean;
	}

}
