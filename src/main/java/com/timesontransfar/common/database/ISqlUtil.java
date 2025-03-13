package com.timesontransfar.common.database;

import java.sql.ResultSet;

public interface ISqlUtil {
	/**
	 * 根据列名取Clob字段
	 * @param rs
	 * @param column
	 * @return
	 */
	String getClob(ResultSet rs,String column);
	/**
	 * 根据列索引取Clob字段
	 * @param rs
	 * @param column
	 * @return
	 */
	String getClob(ResultSet rs,int column);
}
