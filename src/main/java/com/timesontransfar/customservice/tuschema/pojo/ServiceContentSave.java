package com.timesontransfar.customservice.tuschema.pojo;

public class ServiceContentSave {
    private String serviceOrderId;// 任务单号

    private String complaintsId;// 模板id

    private String elementId;// 元素ID

    private String elementName;// 元素name

    private String answerId;// 答案id

    private String answerName;// 答案name

    private String elementOrder;// 元素顺序

    private String isCompare;// 是否比较：1、比较

    private String isStat;// 是否统计：1、统计
    
    private String isShow;// 1必选项 0可选项

	private String aliasName; // 别名

    public String getAnswerId() {
        return answerId;
    }

    public void setAnswerId(String answerId) {
        this.answerId = answerId;
    }

    public String getAnswerName() {
        return answerName;
    }

    public void setAnswerName(String answerName) {
        this.answerName = answerName;
    }

    public String getElementId() {
        return elementId;
    }

    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getElementOrder() {
        return elementOrder;
    }

    public void setElementOrder(String elementOrder) {
        this.elementOrder = elementOrder;
    }

    public String getIsCompare() {
        return isCompare;
    }

    public void setIsCompare(String isCompare) {
        this.isCompare = isCompare;
    }

    public String getIsStat() {
        return isStat;
    }

    public void setIsStat(String isStat) {
        this.isStat = isStat;
    }

    public String getServiceOrderId() {
        return serviceOrderId;
    }

    public void setServiceOrderId(String serviceOrderId) {
        this.serviceOrderId = serviceOrderId;
    }

    public String getComplaintsId() {
        return complaintsId;
    }

    public void setComplaintsId(String complaintsId) {
        this.complaintsId = complaintsId;
    }
    public String getIsShow() {
        return isShow;
    }

    public void setIsShow(String isShow) {
        this.isShow = isShow;
    }

	public String getAliasName() {
		return aliasName;
	}

	public void setAliasName(String aliasName) {
		this.aliasName = aliasName;
	}
}