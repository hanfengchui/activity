package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.worksheet.dao.IUnsatisfyTemplateDao;

@Component(value="unsatisfyTemplate")
public class UnsatisfyTemplateDaoImpl implements IUnsatisfyTemplateDao {
	@Autowired
	private JdbcTemplate jt;
	
    @Autowired
    private PubFunc pubFunc;

	public int insertUnsatisfyTemplate(String reason, String template, int colOrder) {
		String guid = pubFunc.crtGuid();
		String sql = "INSERT INTO cc_unsatisfy_template (unsatisfy_reason, unsatisfy_template, col_order, unsatisfy_id) VALUES (?, ?, ?, ?)";
		return jt.update(sql, reason, template, colOrder, guid);
	}

	public int deleteUnsatisfyTemplate(String unsatisfyId) {
		String sql = "DELETE from cc_unsatisfy_template WHERE unsatisfy_id = ?";
		return jt.update(sql, unsatisfyId);
	}

	public int updateUnsatisfyTemplate(String reason, String template, int colOrder, String unsatisfyId) {
		String sql = "UPDATE cc_unsatisfy_template SET unsatisfy_reason = ?, unsatisfy_template = ?, col_order = ? WHERE unsatisfy_id = ?";
		return jt.update(sql, reason, template, colOrder, unsatisfyId);
	}

	@SuppressWarnings("rawtypes")
	public List selectUnsatisfyTemplate() {
		String sql = "SELECT UNSATISFY_REASON, UNSATISFY_TEMPLATE, COL_ORDER, UNSATISFY_ID FROM cc_unsatisfy_template ORDER BY col_order";
		return jt.queryForList(sql);
	}

	public JdbcTemplate getJt() {
		return jt;
	}

	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}
}
