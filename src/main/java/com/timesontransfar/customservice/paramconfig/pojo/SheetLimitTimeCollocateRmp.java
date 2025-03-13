/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.paramconfig.pojo;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public class SheetLimitTimeCollocateRmp implements RowMapper {

	public Object mapRow(ResultSet rs, int arg1) throws SQLException {
		SheetLimitTimeCollocate bean = new SheetLimitTimeCollocate();
		bean.setLimitGuid(rs.getString("LIMITTME_ID"));
		bean.setRegionId(rs.getInt("REGION_ID"));
		bean.setRegionName(rs.getString("REGION_NAME"));
		bean.setSeviceType(rs.getInt("SERVICE_TYPE"));
		bean.setSeviceName(rs.getString("SERVICE_TYPE_DESC"));
		bean.setTacheId(rs.getInt("TACHE_ID"));
		bean.setTacheName(rs.getString("TACHE_NAME"));
		bean.setLimitTime(rs.getInt("LIMI_TIME"));
		bean.setPrealarmValue(rs.getInt("PREALARM_VALUE"));
		bean.setLimitType(rs.getInt("LIMI_TYPE"));
		bean.setCreatDate(rs.getString("CREAT_DATE"));
		bean.setStaffId(rs.getInt("STAFF_ID"));	
		bean.setCustGread(rs.getInt("CUSTOM_GREAD"));
		bean.setCustGreadName(rs.getString("CUSTOM_GREAD_DESC"));
		bean.setUrgencyGrade(rs.getInt("URGENCY_GRADE"));
		bean.setUrgencyGradeName(rs.getString("URGENCY_GRADE_DESC"));
		
		bean.setDirectoryId(rs.getInt("ACCEPT_DIRECTORY_ID"));
		return bean;
	}
}
