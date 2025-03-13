package com.timesontransfar.staffWorkShift.service;

import java.io.InputStream;
import java.util.List;

import com.timesontransfar.staffWorkShift.pojo.StaffWorkShift;

@SuppressWarnings("rawtypes")
public interface IStaffWorkShiftService {

    /**
     * 解析导入的Excel文件，批量保存员工班次信息
     * 
     * @author LiJiahui
     * @date 2012-3-23
     * @param username
     *            导入配置表格的员工登录名，即当前登录员工的登录名
     * @param file
     *            配置表格文件流
     * @return 处理结果
     */
    public String saveStaffWorkShiftBatch(String username, InputStream file);

    /**
     * 删除单条员工班次记录，包含同步删除对应的员工工作量记录
     * 
     * @param id
     *            员工班次ID
     * @return 删除的记录数
     */
    public int deleteStaffWorkShift(String id);

    /**
     * 根据唯一ID列表，批量删除员工的班次配置记录
     * 
     * @author LiJiahui
     * @date 2011-10-08
     * @param guids
     *            待删除记录的唯一ID列表
     * @return 删除的记录数
     */
    public int deleteBatchStaffWorkShift(List guids);

    /**
     * 修改一条员工班次记录
     * 
     * @param bean
     *            员工班次实例
     * @return 修改成功的记录数目
     */
    public int updateStaffWorkShift(StaffWorkShift bean);

    /**
     * 根据唯一ID，查询一条员工班次记录
     * 
     * @param id
     *            唯一ID
     * @return 员工班次实例
     */
    public StaffWorkShift getStaffWorkShiftById(String id);

}
