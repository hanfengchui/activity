/*
 * 2011-08-25 LiJiahui新增该接口
 */
package com.timesontransfar.staffSkill.dao;

import java.util.List;
import com.timesontransfar.staffSkill.StaffWorkloadInfo;

/**
 * 提供表 CC_STAFF_WORKLOAD操作方法的接口<br>
 * 
 * @author LiJiahui
 */
@SuppressWarnings("rawtypes")
public interface IStaffWorkloadDao {
    /**
     * 批量新增员工工作量阀值记录
     * 
     * @author LiJiahui
     * @date 2012-3-27
     * @param workLoads
     *            工作量阀值列表
     * @return 成功新增的记录数目
     */
    public int saveBatch(final List workLoads);

    /**
     * 根据员工ID，删除其工作量阀值信息<br>
     * 在删除员工班次信息时，需要同时执行该方法
     * 
     * @param staffId
     *            员工ID
     */
    public void deleteByStaffAbility(int staffId);

    /**
     * 删除员工工班时删除工作阀值
     * 
     * @param staffShiftId
     *            员工班次ID
     * @return 删除的记录数
     */
    public int deleteStaffShift(String staffShiftId);

    /**
     * 批量删除员工工班时，同步删除工作量阀值
     * 
     * @date 2011-10-08
     * @author LiJiahui
     * @param staffShiftIds
     *            员工班次ID列表
     * @return 删除的记录数
     */
    public int deleteBatchStaffShift(final List staffShiftIds);

    /**
     * 分配工单时，员工的工作量、完成率改变，修改一条记录
     * 
     * @param bean
     *            一条数据记录的封装对象
     * 
     * @return 被更新的数据记录数
     */
    public int updateStaffWorkload(StaffWorkloadInfo bean);

    /**
     * 员工班次修改时，引起的员工工作量修改
     * 
     * @param workloads
     *            员工工作量表对象实例列表
     * @return 被更新的数据记录数
     */
    public int updateStaffWorkShift(List workloads);

    /**
     * 修改员工技能熟练度引起的员工工作量修改 <br>
     * 批量修改员工阀值记录
     * 
     * @param workloadList
     */
    public void updateAbility(final List workloadList);

    /**
     * 根据员工班次唯一ID，获取一条员工工作量记录
     * 
     * @author LiJiahui
     * @date 2012-3-27
     * @param swsId
     *            员工班次记录的唯一ID
     * @return 查询得到的结果。如果无记录，返回null
     */
    public StaffWorkloadInfo queryBySWSId(String swsId);

    /**
     * 根据系统班次唯一ID，获取多条员工工作量记录
     * 
     * @author LiJiahui
     * @date 2012-3-27
     * @param wsId
     * @return 查询得到的结果。如果无记录，返回null
     */
    public List queryByWSId(String wsId);

    /**
     * 查询一条符合where条件的员工工作量阀值信息
     * 
     * @author LiJiahui
     * @date 2011-10-9
     * @param where
     *            以And开头的where条件
     * @return 员工工作量阀值信息的封装对象，如果没有查询到相关记录，则返回null
     */
    public StaffWorkloadInfo queryByWhere(String where);

	/**
	 * 查询该员工之后工作量排序取第一条
	 * 
	 * @return 员工工作量阀值信息的封装对象，如果没有查询到相关记录，则返回null
	 */
	public StaffWorkloadInfo selectStaffWorkloadInfoAfterByStaffId(int staffId);

    /**
     * 获得所有满足条件的员工阀值记录
     * 
     * @param sql
     *            需要查询的条件
     */
    public List getAllListBySql(String sql);

}
