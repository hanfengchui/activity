/**
 * @author 万荣伟
 */
package com.timesontransfar.staffSkill.dao;


import com.timesontransfar.staffSkill.StaffSkillInfo;

/**
 * @author 万荣伟
 *
 */
public interface StaffSkillDao {
	
	/**
	 * 根据GUID删除员工技能ID,设置为无效
	 * @param guid
	 * @return
	 */
	public int deleteStaffSkill(String guid);
	
	/**
	 * 向数据库中批量保存员工技能信息
	 * 
	 * @author LiJiahui
	 * @date 2011-10-17
	 * @param beans 员工技能封装对象数组
	 * 
	 * @return 成功保存的记录数
	 */
	public int saveStaffSkillBatch(final StaffSkillInfo[] beans);
	
	/**
	 * 批量删除员工技能记录，即将该记录的状态置为0，不可用
	 * 
	 * @author LiJiahui
	 * @date 2011-10-18
	 * @param guids 唯一ID数组
	 * 
	 * @return 成功删除的记录数
	 */
	public int deleteStaffSkillBatch(final String[] guids);
	
	/**
	 * 删除流向部门所属员工技能
	 * @param flowOrgId 流向部门ID
	 * @param serviceDate
	 * @return
	 */
    public int deleteSkillsByFlowOrgId(String flowOrgId, String serviceDate);
}
