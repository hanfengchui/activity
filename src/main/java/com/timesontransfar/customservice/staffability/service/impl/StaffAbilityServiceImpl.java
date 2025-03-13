package com.timesontransfar.customservice.staffability.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.staffability.dao.IStaffAbilityDao;
import com.timesontransfar.customservice.staffability.pojo.StaffAbility;
import com.timesontransfar.customservice.staffability.service.IStaffAbilityService;
import com.timesontransfar.staffSkill.StaffWorkloadInfo;
import com.timesontransfar.staffSkill.dao.IStaffWorkloadDao;
import com.timesontransfar.staffWorkShift.dao.IStaffWorkShiftDao;
import com.timesontransfar.staffWorkShift.pojo.StaffWorkShift;
import com.timesontransfar.workshift.dao.IWorkShiftDao;
import com.timesontransfar.workshift.pojo.WorkShift;

@Component(value="staffAbilityService")
@SuppressWarnings("rawtypes")
public class StaffAbilityServiceImpl implements IStaffAbilityService {

    private static final Logger logger = LoggerFactory.getLogger(StaffAbilityServiceImpl.class);
    
    @Autowired
    private IStaffAbilityDao staffAbilityDao;
    
    @Autowired
    private PubFunc pubFunc;
    
    /**
     * 员工工作量阀值的操作实例
     */
    @Autowired
    private IStaffWorkloadDao staffWorkloadDao;
    
    /**
     * 员工工作班次的操作实例
     */
    @Autowired
    private IStaffWorkShiftDao staffWorkShiftDao;
    
    /**
     * 系统班次表的操作实例
     */
    @Autowired
    private IWorkShiftDao workShiftDao;


    public int updateStaffAbility(StaffAbility bean) {
        String sql = "SELECT * FROM CC_STAFF_WORKLOAD A WHERE A.STAFF_ID='" + bean.getStaffId()
                + "' AND A.STATE <> 1";
        List workloadList = this.staffWorkloadDao.getAllListBySql(sql);
        StaffWorkloadInfo wl = null;
        for (int i = 0; i < workloadList.size(); i++) {
            wl = (StaffWorkloadInfo) workloadList.get(i);
            StaffWorkShift sws = staffWorkShiftDao.getStaffWorkShiftById(wl.getWsId());
            WorkShift ws = workShiftDao.getWorkShiftById(sws.getWorkShiftId());
            wl.setSkillLevel(bean.getSkillLevel());
            // 匹配较小的阀值
            wl.setThreshold(ws.getPercent() > bean.getThreshold() ? bean.getThreshold() : ws.getPercent());
            wl.setCurRate((double) wl.getCurWorkload() / wl.getThreshold());
        }
        if (!workloadList.isEmpty()) {
            this.staffWorkloadDao.updateAbility(workloadList);
        }
        bean.setCreateStaffId(Integer.parseInt(pubFunc.getLogonStaff().getId()));
        return this.staffAbilityDao.updateStaffAbility(bean);
    }

    @SuppressWarnings({ "unchecked", "serial" })
	public String saveStaffAbilityBatch(String loginName, InputStream file) {
        String errorInfo = "success";
        try {
            Workbook book = Workbook.getWorkbook(file);
            Sheet sheet = book.getSheet(0);
            int rows = sheet.getRows();// 行数
            
            TsmStaff staff = pubFunc.getLogonStaffByLoginName(loginName);
            int createStaffId = Integer.parseInt(staff.getId());
            
            //员工管辖OrgIdList
            List olist=this.staffAbilityDao.getOrgList(loginName);
            List addlist = new ArrayList() {
            };// 用于存储数据库中没有的需要新增对象
            List modifyList = new ArrayList() {
            };// 用于存储数据库中已有的只需修改的对象
            List modifyWorkloadList = new ArrayList();// 用于存储需要同步修改的员工工作量记录
            for (int i = 1; i < rows; i++) {
                if (sheet.getCell(0, i).getContents() == null
                        || sheet.getCell(2, i).getContents().equals("")) {
                    continue;
                }
                String logname = sheet.getCell(1, i).getContents();
                int staffId = this.pubFunc.getStaffId(logname);
                if (0 == staffId) {
                	errorInfo = "登录名为 " + logname + " 的员工不存在，请确认表格数据！";
                    return errorInfo;
                }
                String orgId = this.pubFunc.getLogonStaffByLoginName(logname).getOrganizationId();
                if(!olist.contains(orgId)){
                	errorInfo = "当前登陆用户没有配置登录名为"+logname+"的员工熟练度权限。<br> 请重新填写员工信息！";
                    return errorInfo;
                }
                String skillLevelContent = sheet.getCell(2, i).getContents();
                int skillLevel = 0;
                if (StaticData.STAFF_ABILITY_PRACTICED.equals(skillLevelContent)) {
                    skillLevel = 1; // 熟练
                } else if (StaticData.STAFF_ABILITY_NORMAL.equals(skillLevelContent)) {
                    skillLevel = 2; // 一般
                } else if (StaticData.STAFF_ABILITY_NEWER.equals(skillLevelContent)) {
                    skillLevel = 3; // 新学员
                }

                int threshold = Integer.parseInt(sheet.getCell(3, i).getContents());
                StaffAbility sa = this.staffAbilityDao.isStaffAbilityExists(staffId);
                // 判断是否该员工的熟练度信息是否已经存在于员工熟练度表中
                if (null != sa) {
                    if (sa.getThreshold() != threshold) {
                        sa.setThreshold(threshold);
                        buildModifyWorkloadList(sa, modifyWorkloadList);
                    }
                    sa.setSkillLevel(skillLevel);
                    sa.setCreateStaffId(createStaffId);
                    modifyList.add(sa);
                } else {
                    // 如果不存在于员工熟练度表中，则新增
                    String name = pubFunc.getStaffName(staffId).trim();
                    if (name.length() == 0){
                    	continue;
                    }
                    sa = new StaffAbility();
                    sa.setGuid(pubFunc.crtGuid());
                    sa.setStaffId(staffId);
                    sa.setStaffName(name);
                    sa.setSkillLevel(skillLevel);
                    sa.setThreshold(threshold);
                    sa.setCreateStaffId(createStaffId);
                    addlist.add(sa);
                }
            }
            book.close();
            staffWorkloadDao.updateAbility(modifyWorkloadList);
            staffAbilityDao.saveStaffAbilityBatch(addlist, modifyList);
        } catch (BiffException | IOException e) {
        	logger.error("导入员工熟练度信息时发生异常。", e);
            errorInfo = "文件解析异常";
        }
        return errorInfo;
    }

    /**
     * 根据待更新的员工熟练度，同步更新对应的员工工作量
     * 
     * @author LiJiahui
     * @date 2011-10-15
     * 
     * @param ability
     *            员工熟练度信息
     * @param modifyStaffWSList
     *            待更新的员工工作量对象列表
     */
    @SuppressWarnings("unchecked")
	private void buildModifyWorkloadList(StaffAbility ability, List modifyStaffWSList) {
        // 熟练度修改，同步修改工作量阀值
        String sql = "SELECT * FROM CC_STAFF_WORKLOAD A WHERE A.STAFF_ID='" + ability.getStaffId()
                + "' AND A.STATE <> 1";
        List workloadList = null;
        try {
            workloadList = this.staffWorkloadDao.getAllListBySql(sql);
        } catch (Exception e) {
            // 有可能还未给该员工配置工班，则此时查询数据库会出错
            return;
        }

        StaffWorkloadInfo workload = null;
        WorkShift workshift = null;
        for (int n = 0; n < workloadList.size(); n++) {
            workload = (StaffWorkloadInfo) workloadList.get(n);
            String wsId=staffWorkShiftDao.getStaffWorkShiftById(workload.getWsId()).getWorkShiftId();
            workshift = workShiftDao.getWorkShiftById(wsId);
            workload.setSkillLevel(ability.getSkillLevel());
            workload.setThreshold(workshift.getPercent() < ability.getThreshold() ? workshift
                    .getPercent() : ability.getThreshold());
            workload.setCurRate((double) workload.getCurWorkload() / workload.getThreshold());
            modifyStaffWSList.add(workload);
        }
        workloadList = null;
    }

    public int deleteStaffAbility(String guid) {
        StaffAbility sa = this.staffAbilityDao.getStaffAbilityById(guid);
        int staffId = sa.getStaffId();
        this.staffWorkloadDao.deleteByStaffAbility(staffId);
        return this.staffAbilityDao.deleteStaffAbility(guid);
    }

    public StaffAbility getStaffAbilityById(String guid) {
        return this.staffAbilityDao.getStaffAbilityById(guid);
    }

    public StaffAbility isStaffAbilityExists(int staffId) {
        return this.staffAbilityDao.isStaffAbilityExists(staffId);
    }

}
