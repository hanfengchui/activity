package com.timesontransfar.complaintservice.service;

public interface IRetrieveEvaluation {
	public void retrieveEvaluation(String orderId, String code, String msg, String joinMode);
}