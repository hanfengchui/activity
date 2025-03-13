package com.timesontransfar.common.authorization.service;

import java.util.Map;

import javax.servlet.http.HttpSession;

@SuppressWarnings("rawtypes")
public interface ISystemAuthorization {

	public Map getMenuMap(String loginName,HttpSession session);
	
	public String getAuthedSql(Map tableMap, String sql, String objId, String loginName);

	/**
	 * 取得授权的语句
	 * @param tableMap 表名和表别名的对应关系表
	 * @param sql
	 * @param obj_id
	 * @return
	 */
	public String getAuthedSql(Map tableMap, String sql, String objId);

	public HttpSession getHttpSession();

	/**
	 * 重新从数据库中取员工对象,以更新cache的员工对象数据
	 * @param loginName
	 * @param session
	 * @return
	 */
	public IStaffPermit loadNewAuthorization(String loginName, HttpSession session);

	public IStaffPermit loadAuthorization(String loginName, HttpSession session);
	
}
