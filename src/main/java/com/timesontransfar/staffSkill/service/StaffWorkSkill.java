/**
 * @author 万荣伟
 */
package com.timesontransfar.staffSkill.service;

import com.timesontransfar.staffSkill.StaffSkillInfo;

/**
 * @author 万荣伟
 * 
 */
public interface StaffWorkSkill {

    /**
     * 更新员工专业技能为无效
     * 
     * @param bean
     *            员工技能对象
     * @return
     */
    public int updateStaffSkill(String guid);

    /**
     * 批量保存员工技能
     * 
     * @author LiJiahui
     * @date 2011-10-17
     * @param beans
     *            员工技能封装对象数组
     * @param flag
     *            标识量，1表示本地网；2表示一般
     * 
     * @return 成功更新的记录数
     */
    public int saveStaffSkillBatch(StaffSkillInfo[] beans, int flag);

    /**
     * 根据GUID数组，批量删除员工技能
     * 
     * @author LiJiahui
     * @date 2011-10-18
     * @param guids
     *            唯一ID数组
     * @return 成功删除的记录数
     */
    public int deleteStaffSkillBatch(String[] guids);
}
