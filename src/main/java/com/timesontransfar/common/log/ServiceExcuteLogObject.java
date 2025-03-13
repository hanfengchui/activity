package com.timesontransfar.common.log;

public class ServiceExcuteLogObject {
	
	private String gid;
	
	private String serviceId;
	
	private int serviceType; 
	
	private String excuteTime;
	
	private String completeTime;
	
	private boolean retFlag;
	
	private String receiveMsg;
	
	private String outMsg;
	
	private String errorMsg;

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getOutMsg() {
		return outMsg;
	}

	public void setOutMsg(String outMsg) {
		this.outMsg = outMsg;
	}

	public String getCompleteTime() {
		return completeTime;
	}

	public void setCompleteTime(String completeTime) {
		this.completeTime = completeTime;
	}

	public String getExcuteTime() {
		return excuteTime;
	}

	public void setExcuteTime(String excuteTime) {
		this.excuteTime = excuteTime;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getReceiveMsg() {
		return receiveMsg;
	}

	public void setReceiveMsg(String receiveMsg) {
		this.receiveMsg = receiveMsg;
	}

	public boolean isRetFlag() {
		return retFlag;
	}

	public void setRetFlag(boolean retFlag) {
		this.retFlag = retFlag;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public int getServiceType() {
		return serviceType;
	}

	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}

}
