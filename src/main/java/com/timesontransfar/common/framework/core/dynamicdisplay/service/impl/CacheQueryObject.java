// Decompiled by Jad v1.5.8e2. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://kpdus.tripod.com/jad.html
// Decompiler options: packimports(3) fieldsfirst ansi space 
// Source File Name:   WebDynamicDisplayImpl.java

package com.timesontransfar.common.framework.core.dynamicdisplay.service.impl;

import java.io.Serializable;

public class CacheQueryObject
	implements Serializable
{

	private String querySql;
	private Object parameterValue[];

	public CacheQueryObject(String querySql, Object parameterValue[])
	{
		setQuerySql(querySql);
		setParameterValue(parameterValue);
	}

	public Object[] getParameterValue()
	{
		return parameterValue;
	}

	public void setParameterValue(Object parameterValue[])
	{
		this.parameterValue = parameterValue;
	}

	public String getQuerySql()
	{
		return querySql;
	}

	public void setQuerySql(String querySql)
	{
		this.querySql = querySql;
	}
}
