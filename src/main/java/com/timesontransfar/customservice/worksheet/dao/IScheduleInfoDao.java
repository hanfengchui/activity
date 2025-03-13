/**
 * <p>类名：IScheduleInfoDao.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	May 8, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.dao;

import com.timesontransfar.customservice.worksheet.pojo.ScheduleInfo;

/**
 * @author lifeng
 *
 */
public interface IScheduleInfoDao {
	
	/**
	 * 将接口信息存入到接口表中
	 * @param scheduleInfo
	 * @return
	 */
	public int saveScheduleInfo(ScheduleInfo scheduleInfo);

}
