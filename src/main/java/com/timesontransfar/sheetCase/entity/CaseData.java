package com.timesontransfar.sheetCase.entity;

import lombok.Data;

@Data
public class CaseData {

    private String servOrderId; //服务单号

    private String prodNum; //产品号码

    private String serviceTypeDesc; //性质类别

    private String regionName;  //地域

    private String comment;  //受理目录

    private String acceptContent;//受理内容

    private String tsReasonName;//办结原因

    private boolean hisFlag; //历史表or当前表
}
