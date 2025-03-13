package com.timesontransfar.evaluation.dao;

import com.timesontransfar.evaluation.SatisfyInfo;

public interface EvaluationDao {

	public int updateSatisfyInfo(SatisfyInfo info);
	
	public int insertSatisfyInfo(SatisfyInfo info);
	
	public int updateSatisfyOverTime(SatisfyInfo info);

	public SatisfyInfo getSatisfyInfo(String orderId);

	public String getRequireUninvited(String orderId);

	public String getBlackPhone(String relaInfo, String prodNum);
	
	public void saveReturnBlackLog(String orderId, String mobilePhone, String complaintPhone, String blackPhone, String satisfyDegree);
	
	public int insertPayReturnInfo(SatisfyInfo info);
}