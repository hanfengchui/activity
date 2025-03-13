package com.timesontransfar.common.analyzer;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface ISqlAnalyzer {
	/**
	 *
	 * @param sql
	 * @return
	 * @throws IOException
	 */
	public String[] getParameter(String sql) throws RuntimeException;
	/**
	 *
	 * @param Sql
	 * @param webCondition
	 * @return
	 */
	public String addCondition(String sql,String webCondition);
	/**
	 * 增加自定义条件
	 * @param tableMap
	 * @param newSql
	 * @param webCondition
	 * @return
	 * @throws RuntimeException
	 */
	public String addCondition(Map tableMap,String newSql,String webCondition) throws RuntimeException;
	/**
	 * 自适应条件添加
	 * @param tableMap
	 * @param newSql
	 * @param inParam
	 * @return
	 * @throws RuntimeException
	 */
	public String addCondition(Map tableMap,Map columnAliasMap,String newSql, Map inParam) throws RuntimeException;
	/**
	 *
	 * @param sql
	 * @return
	 * @throws IOException
	 */
	public Map getWebId(String sql) throws RuntimeException;
	/**
	 *
	 * @param sql
	 * @return
	 * @throws IOException
	 */
	public String readSql(String sql) throws RuntimeException;

	/**
	 *
	 * @param Sql
	 * @param inParam
	 * @return
	 * @throws IOException
	 */
	public String addCondition(String sql,Map inParam);

	/**
	 *
	 * @param sql
	 * @return
	 * @throws IOException
	 */
	public String generateCountSql(String sql);

	/**
	 *
	 * @param sql
	 * @return
	 * @throws IOException
	 */
	public String generateBetweenSql(String sql,int begin,int end);
	/**
	 * 根据表的别名得到表的表的真实名字
	 * @param sql
	 * @return
	 */
	public Map getAliasMap(String sql);
	/**
	 *根据表名得到表的别名
	 * @param sql
	 * @return
	 */
	public Map getTableMap(String sql);
	/**
	 * 根据字段别名得到表的别名
	 * @param newSql
	 * @return
	 * @throws RuntimeException
	 */
	public Map getColumnAliasMap(String newSql) throws RuntimeException;
	
}
