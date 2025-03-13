package com.timesontransfar.customservice.dbgridData;

/**
 * 部门工单处理
 * @author 郑远贵
 */
public interface IDepartmentCount {

	public GridDataInfo getMonitorExport(int begion, String strWhere);

	public GridDataInfo getSatisfyExport(int begion, String strWhere);

	public GridDataInfo getMonitorExportStaff(int begion, String strWhere);

	public GridDataInfo getSatisfyExportStaff(int begion, String strWhere);

	/**
	 * 获取部门监控
	 */
	public GridDataInfo getOrgMonitor(int begion, String areaId, String orgid,
			String departmentId);


	/**
	 * 获取工单池统计数据
	 * 
	 * @param areaId
	 *            地区ID
	 * @param departmentId
	 *            部门ID
	 * @return 工单池统计数据
	 */
	public GridDataInfo getWorkSinglePondCountMessages(int begion, String areaId, String departmentId, String serviceType);

	public GridDataInfo getSatisfyCountMessages(String areaId,
			String departmentId);

	/**
	 * 获取我的任务统计数据
	 * 
	 * @param areaId
	 *            地区ID
	 * @param departmentId
	 *            部门ID
	 * @return 我的任务统计数据
	 */
	public GridDataInfo getMyWorkSingleCountMessages(int begion, String areaId, String departmentId, String serviceType);

	public GridDataInfo getMyWorkSatisfyCountMessages(String areaId,
			String departmentId);

	/**
	 * 获取派发任务统计数据
	 * 
	 * @param areaId
	 *            地区ID
	 * @param departmentId
	 *            部门ID
	 * @return 派发任务统计数据
	 */
	public GridDataInfo getSentWorkSingleCountMessages(int begion, String areaId, String departmentId, String serviceType);

	/**
	 * 获取我的任务统计数据根据员工
	 * 
	 * @param areaId
	 *            地区ID
	 * @param departmentId
	 *            部门ID
	 * @return 获取我的任务统计数据根据员工
	 */
	public GridDataInfo getMyWorkSingleCountMessagesByStaff(int begion, String areaId, String departmentId ,String serviceType );

	/**
	 * 获取派发任务统计数据
	 * 
	 * @param areaId
	 *            地区ID
	 * @param departmentId
	 *            部门ID
	 * @return 派发任务统计数据
	 */
	public GridDataInfo getSentWorkSingleCountMessagesByStaff(int begion, String areaId, String departmentId, String serviceType );

	/**
	 * 得到部门底下所有的员工以及联系方式
	 * 
	 * @param orgId
	 * @return
	 */
	public String findStatffPhoneByOrgId(String orgId);

	/**
	 * 催单短信发送
	 * Description: <br> 
	 *  
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param phoneType
	 * @param phoneValue
	 * @param message
	 * @param orderName
	 * @param workName
	 * @param sysx
	 * @return <br>
	 * @CreateDate 2020年5月28日 下午2:41:02 <br>
	 */
	public int sendMassage(int phoneType,String phoneValue,String message,String orderName,String workName,String sysx);

	public GridDataInfo monitorExport(int begion, String strWhere);

	public GridDataInfo satisfyExport(int begion, String strWhere);
}
