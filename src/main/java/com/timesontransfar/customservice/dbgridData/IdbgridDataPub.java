/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData;

import com.timesontransfar.common.authorization.service.IStaffPermit;

/**
 * @author 万荣伟
 *
 */
public interface IdbgridDataPub {

	/**
	 * 超过配置数
	 * @param sql
	 * @param begin
	 * @param end
	 * @return
	 */
	public String generateBetweenSql(String sql, int begin, int end);
	
	/**
	 * 查询SQL语句数据数
	 * @param sql
	 * @return
	 */
	public String generateCountSql(String sql);
	
	/**
	 * 得到当前员工对象
	 * @return
	 */
	public IStaffPermit getStaffPermit();
	
	public GridDataInfo getResultBySize(String strSql,int begion,int pageSize,String orderBy,String funId);
	
	/**
	 * 传入countSql，避免子查询慢
	 */
	public GridDataInfo getResultNewBySize(String countSql,String strSql,int begion,int pageSize,String orderBy,String funId);
	
	/**
	 * 得到查询结果
	 * @param strSql 查询SQL
	 * @param begion 开始下标
	 * @param orderBy SQL中 ORDER BY
	 * @return
	 */
	public GridDataInfo getResult(String strSql,int begion,String orderBy,String funId);
	
	/**
     * @author LiJiahui
     * @date 2012-11-23
     * @param innerSQL
     * @param outerSurfix
     * @param outerWhere
     * @param begion
     * @param funId
     * @return
     */
    public GridDataInfo getResultByW(String innerSQL, String outerSurfix, String outerWhere, int begion, int pageSize, String funId);
    
	public GridDataInfo getAllResult(String strSql,String orderBy,String funId);
}
