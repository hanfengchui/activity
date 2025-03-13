package com.timesontransfar.staffWorkShift.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.staffability.dao.IStaffAbilityDao;
import com.timesontransfar.customservice.staffability.pojo.StaffAbility;
import com.timesontransfar.staffSkill.StaffWorkloadInfo;
import com.timesontransfar.staffSkill.dao.IStaffWorkloadDao;
import com.timesontransfar.staffSkill.service.IStaffWorkloadService;
import com.timesontransfar.staffWorkShift.dao.IStaffWorkShiftDao;
import com.timesontransfar.staffWorkShift.pojo.StaffWorkShift;
import com.timesontransfar.staffWorkShift.service.IStaffWorkShiftService;
import com.timesontransfar.workshift.dao.IWorkShiftDao;
import com.timesontransfar.workshift.pojo.WorkShift;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

@Component(value="staffWorkShiftService")
@SuppressWarnings("rawtypes")
public class StaffWorkShiftServiceImpl implements IStaffWorkShiftService {
    private static final Logger log = LoggerFactory.getLogger(StaffWorkShiftServiceImpl.class);

    /**
     * 员工班次表DAO
     */
    @Autowired
    private IStaffWorkShiftDao staffWorkShiftDao;

    /**
     * 员工技能熟练度表DAO
     */
    @Autowired
    private IStaffAbilityDao staffAbilityDao;

    /**
     * 员工工作量表DAO
     */
    @Autowired
    private IStaffWorkloadDao staffWorkloadDao;

    /**
     * 系统班次表DAO
     */
    @Autowired
    private IWorkShiftDao workShiftDao;

    /**
     * 员工工作量处理的服务类
     */
    @Autowired
    private IStaffWorkloadService staffWorkloadService;

    /**
     * 公共方法类实例
     */
    @Autowired
    private PubFunc pubFun;
    
    @SuppressWarnings("unchecked")
	public int updateStaffWorkShift(StaffWorkShift bean) {
        WorkShift ws = workShiftDao.getWorkShiftById(bean.getWorkShiftId()); // 获取系统班次信息
        StaffWorkShift sws = this.staffWorkShiftDao.getStaffWorkShiftById(bean.getId()); // 获取员工班次信息
        StaffWorkloadInfo workload = this.staffWorkloadDao.queryBySWSId(bean.getId()); // 获取员工工作量信息
        StaffAbility ability = staffAbilityDao.queryByStaffId(sws.getStaffId()); // 获取员工技能熟练度信息

        String[] workTime = ws.getTime().split("-");
        String workDate = sws.getWorkDate().substring(0, 10);

        staffWorkloadService.setTime(workDate, workTime[0], workTime[1], workload);
        workload.setThreshold(ability.getThreshold() < ws.getPercent() ? ability.getThreshold() : ws.getPercent());// 匹配较小的阀值
        workload.setCurRate((double) workload.getCurWorkload() / workload.getThreshold());
        List workloads = new ArrayList();
        workloads.add(workload);
        staffWorkloadDao.updateStaffWorkShift(workloads); // 修改员工工作量信息
        return staffWorkShiftDao.updateStaffWorkShift(bean); // 修改员工班次信息
    }

    public int deleteStaffWorkShift(String id) {
        this.staffWorkloadDao.deleteStaffShift(id);
        return this.staffWorkShiftDao.deleteStaffWorkShift(id);
    }

    public int deleteBatchStaffWorkShift(List guids) {
        if (null == guids || guids.isEmpty()) {
            log.error("唯一ID列表为空，没有需要删除的记录。");
            return -1;
        }
        staffWorkloadDao.deleteBatchStaffShift(guids);
        return staffWorkShiftDao.deleteBatchStaffWorkShitf(guids);
    }

    public StaffWorkShift getStaffWorkShiftById(String id) {
        return this.staffWorkShiftDao.getStaffWorkShiftById(id);
    }

    @SuppressWarnings("unchecked")
	public String saveStaffWorkShiftBatch(String username, InputStream file) {
        int length = 0;
        String errorInfo = "success";
        //Map workshitfKV = new HashMap(); // 存储 系统班次ID:系统班次的工作时间段String
        
        try {
            Workbook book = Workbook.getWorkbook(file);
            Sheet sheet = book.getSheet(0);
            Cell cell = null;
            int rows = sheet.getRows(); // 行数
            int columns = sheet.getColumns(); // 列数
            
            TsmStaff staff = pubFun.getLogonStaffByLoginName(username);
            int createStaffId = Integer.parseInt(staff.getId());
            String createTime = this.pubFun.getSysDate();
            
            List wlist=workShiftDao.getWorkShiftByStaff(username);
            List olist=staffAbilityDao.getOrgList(username);
            
            List sws = new ArrayList();
            List workloads = new ArrayList();
            for (int i = 1; i < rows; i++) { // 逐行分析
                if (sheet.getCell(0, i).getContents() == null
                        || "".equals(sheet.getCell(2, i).getContents())) {
                    continue;
                }
                String logname = sheet.getCell(2, i).getContents(); // 获取表格中的员工登录名
                int staffId = this.pubFun.getStaffId(logname);
                if (0 == staffId) {
                    errorInfo = "登录名为 " + logname + " 的员工不存在，请确认表格数据！";
                    return errorInfo;
                }
                StaffAbility sa = staffAbilityDao.queryByStaffId(staffId);
                if (null == sa) {
                    errorInfo = "登录名为 " + logname + " 的员工暂未配置熟练度信息。<br> 请先配置员工技能熟练度信息！";
                    return errorInfo;
                }                
                String orgId = this.pubFun.getLogonStaffByLoginName(logname).getOrganizationId();
                if(!olist.contains(orgId)){
                	errorInfo = "当前登陆用户没有配置登录名为"+logname+"的员工班次权限。<br> 请重新填写员工信息！";
                    return errorInfo;
                }

                String staffName = sheet.getCell(1, i).getContents(); // 获取表格中的员工姓名
                String workTeamName = sheet.getCell(0, i).getContents(); // 获取表格中的班组名称
                for (int j = 3; j < columns; j++) { // 逐列分析
                    cell = sheet.getCell(j, i);
                    if (cell == null) {
                        continue;
                    }
                    
                    String shiftName = cell.getContents(); // 获取表格中的系统班次名称
                    if (null == shiftName || "".equals(shiftName)
                            || StaticData.WORKSHIFT_TYPE_REST.equals(shiftName)) {
                        continue;
                    }

                    WorkShift ws = workShiftDao.getWorkShiftByName(shiftName); // 根据系统班次名称获取系统班次信息
                    if (null == ws) {
                        errorInfo = "名称为 " + shiftName + " 的系统班次不存在。<br> 请先配置系统班次信息！";
                        return errorInfo;
                    }
                    if(!wlist.contains(ws.getId())) {
                    	 errorInfo = "当前登陆用户没有配置班次名为"+ws.getName()+"的员工班次权限。<br> 请重新填写系统班次信息！";
                         return errorInfo;
                    }
                    
                    String workDate = sheet.getCell(j, 0).getContents(); // 获取工作日期  yyyy-mm-dd

                    // 判断员工在当天的班次信息是否配置
                    int existSize = staffWorkShiftDao.selectStaffWorkShift(staffId + "", workDate, ws.getId());
                    if (existSize > 0) {
                        continue;
                    }

                    // 组装员工班次信息
                    StaffWorkShift bean = new StaffWorkShift();
                    bean.setId(pubFun.crtGuid());
                    bean.setStaffId(staffId);
                    bean.setStaffName(staffName);
                    bean.setWorkTeamName(workTeamName);
                    bean.setCreateStaffId(createStaffId);
                    bean.setCreateTime(createTime);
                    bean.setUseable(0);
                    bean.setWorkDate(workDate);
                    bean.setWorkShiftId(ws.getId());
                    sws.add(bean);

                    // 同步生成StaffWorkload信息
                    int threshold = sa.getThreshold() < ws.getPercent() ? sa.getThreshold() : ws.getPercent();
                    StaffWorkloadInfo wl = new StaffWorkloadInfo();
                    wl.setGuid(pubFun.crtGuid());
                    String[] workTime = ws.getTime().split("-");
                    staffWorkloadService.setTime(workDate, workTime[0], workTime[1], wl);
                    wl.setStaffId(staffId);
                    wl.setWsId(bean.getId());
                    wl.setSkillLevel(sa.getSkillLevel());
                    wl.setThreshold(threshold);
                    workloads.add(wl);
                }
            }
            book.close();
            length = staffWorkShiftDao.saveStaffWorkShiftPatch(sws);
            staffWorkloadDao.saveBatch(workloads);
            log.info("成功更新了{}条员工班次数据。", length);
        } catch (BiffException | IOException e) {
            log.error("导入员工班次信息时发生异常。", e);
            errorInfo = "文件解析异常";
        }
        return errorInfo;
    }

}
