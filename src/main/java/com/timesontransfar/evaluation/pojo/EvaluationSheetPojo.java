package com.timesontransfar.evaluation.pojo;

import com.alibaba.fastjson.JSON;

import lombok.Data;


@Data
public class EvaluationSheetPojo {

    private String workSheetId;
    private String workOrderId;
    private int regionId;
    private String regionName;
    private String createDate;
    private String receiveOrgId;
    private String receiveOrgName;
    private int receiveStaff;
    private String receiveStaffName;
    private String distillDate;
    private int dealStaff;
    private String dealStaffName;
    private String dealOrgId;
    private String dealOrgName;
    private int returnStaff;
    private String returnStaffName;
    private String returnOrgId;
    private String returnOrgName;
    private String returnRequire;
    private String sourceSheetId;
    /**
     * 工单状态：0-待处理；1-处理中；2-已处理；3-已派发；9-挂起
     */
    private int sheetStatus;
    /**
     * 工单提取状态：0为未提取，工单在工单池中；1为已提，在我的任务中；2为已完成
     */
    private int lockFlag;
    /**
     * 工单时限（分）
     */
    private int sheetLimit;
    /**
     * 环节id（后台派单、部门处理、部门审核）
     */
    private int tacheId;
    private String tacheDesc;
    private String statusDate;
    private int dealType;
    private String dealContent;
    private String responseDate;
    /**
     * 工单截止时间
     */
    private String limitDate;
    /**
     * 本单完成后的剩余时限（分）
     */
    private int leftLimit;
    /**
     * 工单是否超时：0-否；1-是
     */
    private int isSheetOverTime;
    /**
     * 处理标识（只给最后一次处理单打标）
     */
    private int responseFlag;
    
    private int contactResultId;
    private String contactResultDesc;
    
    private int unsatisfiedReasonId;
    private String unsatisfiedReasonDesc;
    
    private int reasonDetailId;
    private String reasonDetailDesc;
    
    private String custAdviceComfirm;
    
    private String dealSchemeId;
    private String dealSchemeDesc;
    
    private int repairResultId;
    private String repairResultDesc;
    
    private String responsibilityOrgId;
    private String responsibilityOrgName;
    
    public String toString() {
		return JSON.toJSONString(this);
	}
    
}
