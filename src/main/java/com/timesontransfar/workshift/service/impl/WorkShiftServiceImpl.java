package com.timesontransfar.workshift.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.staffability.dao.IStaffAbilityDao;
import com.timesontransfar.customservice.staffability.pojo.StaffAbility;
import com.timesontransfar.staffSkill.StaffWorkloadInfo;
import com.timesontransfar.staffSkill.dao.IStaffWorkloadDao;
import com.timesontransfar.staffSkill.service.IStaffWorkloadService;
import com.timesontransfar.workshift.dao.IWorkShiftDao;
import com.timesontransfar.workshift.pojo.WorkShift;
import com.timesontransfar.workshift.service.IWorkShiftService;
import com.transfar.common.utils.StringUtils;

@Component(value="workShiftService")
@SuppressWarnings("rawtypes")
public class WorkShiftServiceImpl implements IWorkShiftService {
    /**
     * 日志实例
     */
    private static final Logger log = LoggerFactory.getLogger(WorkShiftServiceImpl.class);
    
    @Autowired
    private IWorkShiftDao workShiftDao;
    
    @Autowired
    private PubFunc pubFunc;
    
    @Autowired
    private IStaffWorkloadDao staffWorkloadDao;

    /**
     * 员工工作量处理的服务类
     */
    @Autowired
    private IStaffWorkloadService staffWorkloadService;

	/**
     * 员工熟练度操作实例
     */
    @Autowired
    private IStaffAbilityDao staffAbilityDao;

    
    public String addWorkShift(WorkShift workShift) {
    	workShift.setId(pubFunc.crtGuid());
        workShift.setCreateStaffId(Integer.parseInt(this.pubFunc.getLogonStaff().getId()));
        workShift.setCreateOrgId(this.pubFunc.getLogonStaff().getOrganizationId());
        workShift.setCreateOrgName(this.pubFunc.getLogonStaff().getOrgName());
        workShift.setCreateLogonname(this.pubFunc.getLogonStaff().getLogonName());
        WorkShift workShiftByName = this.workShiftDao.getWorkShiftByName(workShift.getName());
        if(StringUtils.isNotNull(workShiftByName)){
        	return "REPEAT";
        }
        int size = this.workShiftDao.addWorkShift(workShift);
        if (size > 0)
            return "SUCCESS";
        else
            return "ERROR";
    }

    public int modifyWorkShift(WorkShift workShift) {
        workShift.setCreateStaffId(Integer.parseInt(this.pubFunc.getLogonStaff().getId()));
        workShift.setCreateOrgId(this.pubFunc.getLogonStaff().getOrganizationId());
        workShift.setCreateOrgName(this.pubFunc.getLogonStaff().getOrgName());
        workShift.setCreateLogonname(this.pubFunc.getLogonStaff().getLogonName());
        /*
         * 更新系统班次信息，同步修改 员工工作量表的工作时间字段
         */
        List workloadList = null;
        try {
            workloadList = this.staffWorkloadDao.queryByWSId(workShift.getId());
            String[] workTime = workShift.getTime().split("-");
            if (null != workloadList && !workloadList.isEmpty()){
                StaffAbility ability = null;
                StaffWorkloadInfo workload = null;
                for (int i = 0; i < workloadList.size(); i++) {
                    workload = (StaffWorkloadInfo) workloadList.get(i);
                    String day = workload.getStartMoment();
                    day = day.substring(0, day.indexOf(" "));
                    staffWorkloadService.setTime(day, workTime[0], workTime[1], workload); // 更新时间
                    ability = staffAbilityDao.queryByStaffId(workload.getStaffId());
                    workload.setThreshold(ability.getThreshold() < workShift.getPercent() ? ability.getThreshold() : workShift.getPercent());// 匹配较小的阀值
                    workload.setCurRate((double)workload.getCurWorkload()/workload.getThreshold());
                }
                this.staffWorkloadDao.updateStaffWorkShift(workloadList);
            }
        } catch (Exception e) {
            log.debug("",e);
        }
        return this.workShiftDao.modifyWorkShift(workShift);
    }

    public WorkShift getWorkShiftById(String id) {
        return this.workShiftDao.getWorkShiftById(id);
    }

    public WorkShift getWorkShiftByName(String name) {
        return this.workShiftDao.getWorkShiftByName(name);
    }

    public int shiftNameExist(String id, String name) {
        return this.workShiftDao.shiftNameExist(id, name);
    }

    public List getWorkShift() {
        return this.workShiftDao.getWorkShift();
    }

}
