/**
 * @author 万荣伟
 */
package com.timesontransfar.staffSkill.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.staffSkill.StaffSkillInfo;
import com.timesontransfar.staffSkill.dao.StaffSkillDao;
import com.timesontransfar.staffSkill.service.StaffWorkSkill;

/**
 * @author 万荣伟
 *
 */
@Component("staffWorkSkill")
public class StaffWorkSkillImpl implements StaffWorkSkill {
	
	@Autowired
	private StaffSkillDao staffSkillDao;
	@Autowired
	private PubFunc	pubFunc;

	/**
	 * 更新员工专业技能为无效
	 * @param bean 员工技能对象
	 * @return
	 */
	public int updateStaffSkill(String guid){	
		if(guid == null) {
			return 0;
		}
		String[] guidArry = guid.split("@_&");
		int size = guidArry.length;
		for(int i=0;i<size;i++) {
			staffSkillDao.deleteStaffSkill(guidArry[i]);
		}
		return 1;
	}

    public int saveStaffSkillBatch(StaffSkillInfo[] beans, int flag) {
        if(null == beans || beans.length == 0){
            return 0;
        }
        
        TsmStaff staff = this.pubFunc.getLogonStaff();
        int staffId = Integer.parseInt(staff.getId());
        int size = beans.length;
        if(1 == flag){
            for(int i = 0; i < size; i++){
            	beans[i].setGuid(this.pubFunc.crtGuid());
                beans[i].setCreatStaff(staffId);
                beans[i].setSkillState(1);
            }
            return staffSkillDao.saveStaffSkillBatch(beans);
        }
        return 0;
    }
    
    public int deleteStaffSkillBatch(String[] guids) {
        return staffSkillDao.deleteStaffSkillBatch(guids);
    }

}
