package com.timesontransfar.evaluation.pojo;

import com.alibaba.fastjson.annotation.JSONField;

public class EvaluationReturnPojo {

    @JSONField(name = "order_assess_id")
    private String orderAssessId; //集约测评平台唯一标识ID
    @JSONField(name = "work_order_id")
    private String workOrderId; //省公司原始订单编号（投诉单号/工单号）
    @JSONField(name = "province_code")
    private String provinceCode; //省编码
    @JSONField(name = "province_name")
    private String provinceName; //省名称
    @JSONField(name = "contact_type")
    private String contactType; //触点渠道
    @JSONField(name = "user_id")
    private String userId; //用户ID
    @JSONField(name = "user_dn")
    private String userDn; //用户设备号
    @JSONField(name = "is_payreturn")
    private String isPayReturn; //是否回访成功
    @JSONField(name = "payreturn_time")
    private String payReturnTime; //回访时间
    @JSONField(name = "payreturn_workno")
    private String payReturnWorkNo; //回访人员员信息
    @JSONField(name = "payreturn_resulet")
    private String payReturnResult; //回访结果
    @JSONField(name = "payreturn_comment")
    private String payReturnComment; //客户意见


    @Override
    public String toString() {
        return "EvaluationReturnPojo{" +
                "orderAssessId='" + orderAssessId + '\'' +
                ", workOrderId='" + workOrderId + '\'' +
                ", provinceCode='" + provinceCode + '\'' +
                ", provinceName='" + provinceName + '\'' +
                ", contactType='" + contactType + '\'' +
                ", userId='" + userId + '\'' +
                ", userDn='" + userDn + '\'' +
                ", isPayReturn='" + isPayReturn + '\'' +
                ", payReturnTime='" + payReturnTime + '\'' +
                ", payReturnWorkNo='" + payReturnWorkNo + '\'' +
                ", payReturnResult='" + payReturnResult + '\'' +
                ", payReturnComment='" + payReturnComment + '\'' +
                '}';
    }

    public String getOrderAssessId() {
        return orderAssessId;
    }

    public void setOrderAssessId(String orderAssessId) {
        this.orderAssessId = orderAssessId;
    }

    public String getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(String workOrderId) {
        this.workOrderId = workOrderId;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserDn() {
        return userDn;
    }

    public void setUserDn(String userDn) {
        this.userDn = userDn;
    }

    public String getIsPayReturn() {
        return isPayReturn;
    }

    public void setIsPayReturn(String isPayReturn) {
        this.isPayReturn = isPayReturn;
    }

    public String getPayReturnTime() {
        return payReturnTime;
    }

    public void setPayReturnTime(String payReturnTime) {
        this.payReturnTime = payReturnTime;
    }

    public String getPayReturnWorkNo() {
        return payReturnWorkNo;
    }

    public void setPayReturnWorkNo(String payReturnWorkNo) {
        this.payReturnWorkNo = payReturnWorkNo;
    }

    public String getPayReturnResult() {
        return payReturnResult;
    }

    public void setPayReturnResult(String payReturnResult) {
        this.payReturnResult = payReturnResult;
    }

    public String getPayReturnComment() {
        return payReturnComment;
    }

    public void setPayReturnComment(String payReturnComment) {
        this.payReturnComment = payReturnComment;
    }
}
