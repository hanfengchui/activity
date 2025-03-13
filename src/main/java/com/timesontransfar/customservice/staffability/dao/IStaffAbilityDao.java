package com.timesontransfar.customservice.staffability.dao;

import java.util.List;

import com.timesontransfar.customservice.staffability.pojo.StaffAbility;
@SuppressWarnings("rawtypes")
public interface IStaffAbilityDao {
	/*
	 * 批量保存员工技能数量对象
	 * @param addList 上传文件获得的需要新增的对象列表
	 * @param modifyList 上传文件获得的需要修改的对象列表
	 */
	public int saveStaffAbilityBatch(final List addList, final List modifyList);
	/*
	 * 更新员工技能数量对象
	 * @param bean 员工技能熟练对象
	 */
	public int updateStaffAbility(StaffAbility bean);
	/*
	 * 删除员工技能数量对象
	 * @param bean 员工技能熟练对象编号
	 */
	public int deleteStaffAbility(String guid);
	/*
	 * 根据guid获得员工技能数量对象
	 * @param bean 员工技能熟练对象编号
	 */
	public StaffAbility getStaffAbilityById(String guid);
	/*
	 * 根据员工编号判断数据库中是否已有记录
	 * @param staffId 员工编号
	 */
	public StaffAbility isStaffAbilityExists(int staffId);
	
	/**
	 * 通过员工ID获取一条当前有效的员工熟练度记录
	 * 
	 * @author LiJiahui
	 * @date 2011-10-15
	 * @param staffId
	 *         员工ID
	 * @return 员工熟练度封装对象
	 */
	public StaffAbility queryByStaffId(int staffId);
	
	/**
	 * 通过员工权限获取OrgIdList
	 * @param staff
	 * @return OrgIdList
	 */
	public List getOrgList(String loginName);
	
}
