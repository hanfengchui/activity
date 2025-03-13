package com.timesontransfar.workshift.dao;

import java.util.List;

import com.timesontransfar.workshift.pojo.WorkShift;

@SuppressWarnings("rawtypes")
public interface IWorkShiftDao {
    /**
     * 保存新的工作班次信息
     * 
     * @param workShift
     *            工作班次对象
     */
    public int addWorkShift(WorkShift workShift);

    /**
     * 修改工作班次信息
     * 
     * @param workShift
     *            工作班次对象
     */
    public int modifyWorkShift(WorkShift workShift);

    /**
     * 根据id取得工作班次对象
     * 
     * @param id
     *            工作班次编号
     */
    public WorkShift getWorkShiftById(String id);

    /**
     * 根据班次名称获得班次对象
     * 
     * @param name
     *            工作班次名称
     */
    public WorkShift getWorkShiftByName(String name);

    /**
     * 根据班次编号和名称在添加和修改时判断系统中该名称已存在
     */
    public int shiftNameExist(String id, String name);

    /**
     * 得到所有班次列表
     */
    public List getWorkShift();
    
    /**
     * 根据staff获取班次集合
     * @param staff
     * @return
     */
    public List getWorkShiftByStaff(String loginName);
}
