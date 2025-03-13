package com.timesontransfar.staffWorkShift.dao;

import java.util.List;

import com.timesontransfar.staffWorkShift.pojo.StaffWorkShift;

@SuppressWarnings("rawtypes")
public interface IStaffWorkShiftDao {

    /**
     * 批量新增员工班次信息
     * 
     * @param list 员工班次信息列表
     * @return 保存的记录数
     */
    public int saveStaffWorkShiftPatch(final List list);

    /**
     * 修改一条员工班次记录
     * 
     * @param bean
     *            员工班次信息实例
     * @return 修改的记录数目
     */
    public int updateStaffWorkShift(StaffWorkShift bean);

    public int deleteStaffWorkShift(String id);

    /**
     * 根据主键数组，批量删除记录
     * 
     * @author LiJiahui
     * @date 2011-10-08
     * @param ids
     *            唯一主键
     * @return 删除的记录数
     */
    public int deleteBatchStaffWorkShitf(final List ids);

    /**
     * 根据员工ID、工作日期、系统班次ID查询符合条件的员工班次记录数目<br>
     * 传入的参数中，如果有一个为空字符串，或为null，则返回0
     * 
     * @author LiJiahui
     * @date 2012-3-23
     * @param staffId
     *            员工ID
     * @param workDate
     *            工作日期yyyy-mm-dd
     * @param workShiftId
     *            系统班次ID
     * @return 查询到的记录数目
     */
    public int selectStaffWorkShift(String staffId, String workDate, String workShiftId);

    public StaffWorkShift getStaffWorkShiftById(String id);
}
