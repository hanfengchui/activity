package com.timesontransfar.evaluation.pojo;

import java.util.List;

public class QuestionsPojo {

    private String qtitle;
    private String qtype;
    private String answertext;
    private List<OptionsPojo> options;

    @Override
    public String toString() {
        return "QuestionsPojo{" +
                "qtitle='" + qtitle + '\'' +
                ", qtype='" + qtype + '\'' +
                ", answertext='" + answertext + '\'' +
                ", options=" + options +
                '}';
    }

    public String getQtitle() {
        return qtitle;
    }

    public void setQtitle(String qtitle) {
        this.qtitle = qtitle;
    }

    public String getQtype() {
        return qtype;
    }

    public void setQtype(String qtype) {
        this.qtype = qtype;
    }

    public String getAnswertext() {
        return answertext;
    }

    public void setAnswertext(String answertext) {
        this.answertext = answertext;
    }

    public List<OptionsPojo> getOptions() {
        return options;
    }

    public void setOptions(List<OptionsPojo> options) {
        this.options = options;
    }
}
