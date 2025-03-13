package com.timesontransfar.common.database.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.timesontransfar.common.database.KeyGenerator;

public class KeyGeneratorGuidImpl implements KeyGenerator{
	
	private JdbcTemplate jdbcTemplate;
	private String sqlOfMakeKey;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String generateKey(String name) {
		List result = this.jdbcTemplate.query(this.sqlOfMakeKey, new KeyRowMapper());
		if(!result.isEmpty())
			return result.get(0).toString();
		else
			return "-1";
	}
		
	@SuppressWarnings("rawtypes")
	class KeyRowMapper implements RowMapper{

		public Object mapRow(ResultSet arg0, int arg1) throws SQLException {
			if(arg0.getString(1)==null)
				return "-1";
			else
				return arg0.getString(1);
		}
			
	}

	public String getSqlOfMakeKey() {
		return sqlOfMakeKey;
	}

	public void setSqlOfMakeKey(String sqlOfMakeKey) {
		this.sqlOfMakeKey = sqlOfMakeKey;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
