package com.timesontransfar.common.framework.core.dynamicdisplay.service.impl;

import com.timesontransfar.common.database.ISqlUtil;
import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;
import java.sql.*;
import java.util.Map;
import java.util.TreeMap;
@SuppressWarnings("rawtypes")
class GenerateMetaMapRowMapper extends AbstractRowMapper{

	private ISqlUtil sqlUtil;

	GenerateMetaMapRowMapper()
	{
	}

	public ISqlUtil getSqlUtil()
	{
		return sqlUtil;
	}

	public void setSqlUtil(ISqlUtil sqlUtil)
	{
		this.sqlUtil = sqlUtil;
	}

	public Object mapRow(ResultSet rs, int index)throws SQLException{
		Map dataMap = new TreeMap();
		ResultSetMetaData metaData = rs.getMetaData();
		int columnSize = metaData.getColumnCount();
		for (int i = 1; i <= columnSize; i++)
		{
			String label = metaData.getColumnLabel(i);
			String dataString = "";
			switch (metaData.getColumnType(i))
			{
			case 91: // '['
			case 92: // '\\'
			case 93: // ']'
				dataString = rs.getString(i);
				if (dataString != null)
				{
					int dotIndex = dataString.lastIndexOf('.');
					dataString = dataString.substring(0, dotIndex);
				}
				break;

			case 2005: 
				dataString = sqlUtil.getClob(rs, i);
				break;

			default:
				dataString = rs.getString(i);
				break;
			}
			dataMap.put(label, dataString);
		}

		return dataMap;
	}
}
