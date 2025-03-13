/*
 * 创建日期 2006-8-27
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

import java.util.Map;

/**
 * @author ationr
 *
 * 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
@SuppressWarnings("rawtypes")
public class MetaData4WorkFlow implements java.io.Serializable,Cloneable{
	//此三字段需要从数据库取值
	private String id;

	private long entityId;

	private String querySql;

	//以下为工作流组件进行处理的属性，来自工作流调度组件，而不是数据库。
	private Map tableMap;

	private Map aliasMap;

	private Map webIDMap;

	private String[] parameter;

	private Map columnAliasMap;


	/**
	 * @return 返回 aliasMap。
	 */
	public Map getAliasMap() {
		return aliasMap;
	}
	/**
	 * @param aliasMap 要设置的 aliasMap。
	 */
	public void setAliasMap(Map aliasMap) {
		this.aliasMap = aliasMap;
	}
	/**
	 * @return 返回 columnAliasMap。
	 */
	public Map getColumnAliasMap() {
		return columnAliasMap;
	}
	/**
	 * @param columnAliasMap 要设置的 columnAliasMap。
	 */
	public void setColumnAliasMap(Map columnAliasMap) {
		this.columnAliasMap = columnAliasMap;
	}


	public long getEntityId() {
		return entityId;
	}
	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}
	/**
	 * @return 返回 id。
	 */
	public String getId() {
		return id;
	}
	/**
	 * @param id 要设置的 id。
	 */
	public void setId(String id) {
		this.id = id;
	}
	/**
	 * @return 返回 parameter。
	 */
	public String[] getParameter() {
		return parameter;
	}
	/**
	 * @param parameter 要设置的 parameter。
	 */
	public void setParameter(String[] parameter) {
		this.parameter = parameter;
	}
	/**
	 * @return 返回 querySql。
	 */
	public String getQuerySql() {
		return querySql;
	}
	/**
	 * @param querySql 要设置的 querySql。
	 */
	public void setQuerySql(String querySql) {
		this.querySql = querySql;
	}
	/**
	 * @return 返回 tableMap。
	 */
	public Map getTableMap() {
		return tableMap;
	}
	/**
	 * @param tableMap 要设置的 tableMap。
	 */
	public void setTableMap(Map tableMap) {
		this.tableMap = tableMap;
	}
	/**
	 * @return 返回 webIDMap。
	 */
	public Map getWebIDMap() {
		return webIDMap;
	}
	/**
	 * @param webIDMap 要设置的 webIDMap。
	 */
	public void setWebIDMap(Map webIDMap) {
		this.webIDMap = webIDMap;
	}

	protected void finalize() throws Throwable
	{
		super.finalize();
		this.clear();
	}

	private void clear(){
		if(this.aliasMap!=null) this.aliasMap.clear();
		if(this.columnAliasMap!=null) this.columnAliasMap.clear();
		if(this.tableMap!=null) this.tableMap.clear();
		if(this.webIDMap!=null) this.webIDMap.clear();
	}

	public Object clone() {
		MetaData4WorkFlow copy = null;
		try{
			copy = (MetaData4WorkFlow)super.clone();
		}catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}

}
