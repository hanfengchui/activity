package com.timesontransfar.evaluation.service;

import com.timesontransfar.evaluation.SatisfyInfo;

public interface EvaluationService {

	public String dealResultList(String jsonStr);

	public boolean insertSatisfyInfoYDX(SatisfyInfo info, String relaInfo, String prodNum);

	public int insertSatisfyInfoZDX(SatisfyInfo info, String relaInfo, String prodNum);

	public int insertSatisfyInfoJSCP(SatisfyInfo info, String relaInfo, String prodNum);

	public int dealSatisfyOverTime(String orderId);

	public String createPerceptionOrder(String jsonStr);
}