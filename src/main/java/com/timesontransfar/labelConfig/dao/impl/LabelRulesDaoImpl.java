package com.timesontransfar.labelConfig.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.labelConfig.dao.ILabelRulesDao;
import com.timesontransfar.labelConfig.pojo.LabelRules;

@Component(value="labelRulesDao")
public class LabelRulesDaoImpl implements ILabelRulesDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private String saveSql = 
            "INSERT INTO CC_LABEL_RULES(" +
                "LABEL_RULES_ID," +
                "LABEL_ID," +
                "LEFT_FIELD_ID," +
                "LOGIC_SYMBOL," +
                "RIGHT_CONTENT," +
                "RULES_SORT," +
                "SIXTH_DIR_ID"+
            ")VALUES(?,?,?,?,?,?,?)";
    
    @Override
    public int saveLabelRules(LabelRules r) {
        return jdbcTemplate.update(saveSql,
                r.getLabelRulesId(),
                r.getLabelId(),
                r.getLeftFieldId(),
                r.getLogicSymbol(),
                r.getRightContent(),
                r.getRulesSort(),
                r.getSixId()
        );
    }
}