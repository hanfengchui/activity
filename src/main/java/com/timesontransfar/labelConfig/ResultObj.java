package com.timesontransfar.labelConfig;


public class ResultObj {
    private String resultCode;

    private String resultMsg;

    private String resultFlag;

    private Object resultObj;

    private String eventId;

    public Object getResultObj() {
        return resultObj;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setResultObj(Object resultObj) {
        this.resultObj = resultObj;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }

    public String getResultFlag() {
        return resultFlag;
    }

    public void setResultFlag(String resultFlag) {
        this.resultFlag = resultFlag;
    }
}
