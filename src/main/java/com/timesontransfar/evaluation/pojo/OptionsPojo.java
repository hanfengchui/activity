package com.timesontransfar.evaluation.pojo;

public class OptionsPojo {

    private String otitle;
    private String check;
    private String answertext;

    @Override
    public String toString() {
        return "OptionsPojo{" +
                "otitle='" + otitle + '\'' +
                ", check='" + check + '\'' +
                ", answertext='" + answertext + '\'' +
                '}';
    }

    public String getOtitle() {
        return otitle;
    }

    public void setOtitle(String otitle) {
        this.otitle = otitle;
    }

    public String getCheck() {
        return check;
    }

    public void setCheck(String check) {
        this.check = check;
    }

    public String getAnswertext() {
        return answertext;
    }

    public void setAnswertext(String answertext) {
        this.answertext = answertext;
    }
}
