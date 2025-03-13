package com.timesontransfar.sheetCase.entity;

import lombok.Data;



@Data
public class CaseEntity {

    private String caseId;  //案例编号

    private String orderId;  //服务单号

    private String creator;  //提交人

    private String level;  //案例级别:1：个人，2：地市，3省份

    private String encode;  //城市级别编码：级别编码

    private String status;  //案例状态1：草稿，2：审批，3：发布，4：停用 级别为1默认草稿，级别为2或3默认审批

    private String createTime;  //案例生效时间

    private String lapseTime;  //案例失效时间

    private String caseDetail;  //案例亮点/差错点

    private String caseState;  //案例类型0:劣质，1：优秀

    private String creatorDepartment; //提交员工部门

    private String updateTime;  //提交时间

    private String sheetId;  //产品号码

    private String auditStatus; //案例审核状态 0:待审核 1：已审核

    private int orderType; //案例类型

    private String approver;//审核人

    private String auditDepartment;//审核部门

    private String beginTime;

    private String endTime;

    private int currentPage;

    private int pageSize;

    private String passStatus;

    private String notPassCause;


}
