/*
 * 说明：提供操作 CCS.CC_STAFF_WORKLOAD 表相关的服务
 * 时间：2011-10-08
 * 作者：LiJiahui
 * 操作：新增
 */
package com.timesontransfar.staffSkill.service;

import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.staffSkill.FlowToEnd;
import com.timesontransfar.staffSkill.StaffWorkloadInfo;

import java.util.List;
import java.util.Map;

/**
 * 接口类，提供操作 CCS.CC_STAFF_WORKLOAD 表相关的服务
 * 
 * @author LiJiahui
 */
public interface IStaffWorkloadService {
    /**
     * 获取一条员工工作量阀值记录，不考虑工作量是否到达阀值<br>
     * 该员工的班次状态为 0、 2
     * 
     * @author LiJiahui
     * @date 2011-10-9
     * @param staffId
     *            员工ID
     * 
     * @return 员工工作量阀值信息封装对象。如果没有查询到相关记录，则返回null
     */
    public StaffWorkloadInfo queryInWork(int staffId);

	/**
	 * 获取一条员工当前工作量，没有再查询之后的工作量
	 */
	public StaffWorkloadInfo queryInOrAfterWork(int staffId);

	@SuppressWarnings("rawtypes")
	public Map queryStaffWorkloadRepeat(int skillId, OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo);

    @SuppressWarnings("rawtypes")
	public Map getStaffWorkload(String flowOrg, int skillId, int tacheId, String serviceDate,int serviceType);

    public int updateApportion(String staffId);

    /**
     * 分配一条工单任务
     * 
     * @param info
     *            员工工作量阀值信息封装对象
     * @param flag
     *            是否算工作量，true算工作量
     * 
     * @return 被更新的数据记录数
     */
    public int allotWork(StaffWorkloadInfo info, boolean flag);

    /**
     * 根据给定的时间字符串，设置员工工作量的分配时间
     * 
     * @author LiJiahui
     * @date 2011-10-15
     * @param day
     *            开始日期
     * @param startTime
     *            系统班次的开始时间
     * @param endTime
     *            系统班次的结束时间
     * @param workload
     *            员工工作量封装对象
     */
    public void setTime(String day, String startTime, String endTime, StaffWorkloadInfo workload);

	/*
	 * 根据ID和类型查询一跟到底配置存不存在，keyType：1、生效单位，2、生效渠道，3、不生效的号码
	 */
	public int checkFlowToEndConfigByIdType(String keyId, int keyType);

	/**
	 * 1、时间范围：近30日；3、重复逻辑：优先用产品号码比对，若没有再用联系电话比对；4、工单自动归集。按时间靠近的在库人员或办结人员自动跳入该员工库里。优先派至在途人员库中，无在途则派至办结人员库中（不判断是否在班）。
	 */
	public FlowToEnd getFlowToEnd30Day(OrderAskInfo askInfo, String orgPlace);

	/**
	 * 判断之前处理员工是否长休假
	 */
	public int checkDealStaffIdIsRest(int dealStaffId);

	/*
	 * 由30天匹配后初始插入记录
	 */
	public int saveFlowToEnd(FlowToEnd fte);

	/*
	 * 根据30天内员工Id查询未更新工作量情况表的记录
	 */
	public List<FlowToEnd> getEmptyWorkloadByDealStaffId(int dealStaffId);

	/*
	 * 更新工作量情况表的记录
	 */
	public int setWorkloadByIncrementId(String countWorkloadGuid, int incrementId);
}