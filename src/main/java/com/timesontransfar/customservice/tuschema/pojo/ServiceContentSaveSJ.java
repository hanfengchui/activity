package com.timesontransfar.customservice.tuschema.pojo;

public class ServiceContentSaveSJ {
	private String serviceOrderId;// 任务单号

	private String contentId;// 问题

	private String contentDesc;// 答案

	public String getServiceOrderId() {
		return serviceOrderId;
	}

	public void setServiceOrderId(String serviceOrderId) {
		this.serviceOrderId = serviceOrderId;
	}

	public String getContentId() {
		return contentId;
	}

	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	public String getContentDesc() {
		return contentDesc;
	}

	public void setContentDesc(String contentDesc) {
		this.contentDesc = contentDesc;
	}
}
