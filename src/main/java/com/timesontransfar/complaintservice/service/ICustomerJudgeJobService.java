package com.timesontransfar.complaintservice.service;

public interface ICustomerJudgeJobService {
	// 获取评价满意度
	public String enterJudgeJob(String orderId);

	// 回收集团评价结果
	public String retrieveEvaluation(String orderId, String code, String msg, String joinMode);

	public String cmpAutoFinishBBAJob(String logonname);
}