// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   WebDynamicDisplayImpl.java

package com.timesontransfar.common.framework.core.dynamicdisplay.service.impl;

import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;
import java.sql.*;

class optionRowMapper extends AbstractRowMapper
{

	optionRowMapper()
	{
	}

	public Object mapRow(ResultSet rs, int index)
		throws SQLException
	{
		String data[] = new String[rs.getMetaData().getColumnCount()];
		for (int i = 0; i < data.length; i++)
			data[i] = rs.getString(i);

		return data;
	}
}
