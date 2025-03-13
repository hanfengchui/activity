/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData;

/**
 * @author 万荣伟
 *
 */
public interface IdbGridStarStaff {
	
	/**
	 * 查询星级客户列表
	 * @param begin
	 * @param strWhere
	 * @return
	 */
	public GridDataInfo queryStarStaffList(int begion,String strWhere);
	 
}
