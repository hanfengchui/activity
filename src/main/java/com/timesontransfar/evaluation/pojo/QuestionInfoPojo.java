package com.timesontransfar.evaluation.pojo;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

public class QuestionInfoPojo {

    @JSONField(name = "evalid")
    private String evalNd;
    @JSONField(name = "evalname")
    private String evalName;

    private String id;
    @JSONField(name = "useraddtime")
    private String userAddTime;
    @JSONField(name = "userid")
    private String userId;
    @JSONField(name = "usersubtime")
    private String userSubTime;

    private List<QuestionsPojo> questions;

    @Override
    public String toString() {
        return "QuestionInfoPojo{" +
                "evalNd='" + evalNd + '\'' +
                ", evalName='" + evalName + '\'' +
                ", id='" + id + '\'' +
                ", userAddTime='" + userAddTime + '\'' +
                ", userId='" + userId + '\'' +
                ", userSubTime='" + userSubTime + '\'' +
                ", questions=" + questions +
                '}';
    }

    public String getEvalNd() {
        return evalNd;
    }

    public void setEvalNd(String evalNd) {
        this.evalNd = evalNd;
    }

    public String getEvalName() {
        return evalName;
    }

    public void setEvalName(String evalName) {
        this.evalName = evalName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserAddTime() {
        return userAddTime;
    }

    public void setUserAddTime(String userAddTime) {
        this.userAddTime = userAddTime;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserSubTime() {
        return userSubTime;
    }

    public void setUserSubTime(String userSubTime) {
        this.userSubTime = userSubTime;
    }

    public List<QuestionsPojo> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionsPojo> questions) {
        this.questions = questions;
    }
}
