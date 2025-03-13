/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.dao.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.workFlowOrg.dao.IworksheetRuleDao;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRule;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRuleRmp;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public class WorkSheetRuleDaoImpl implements IworksheetRuleDao{

	private static final Logger log =LoggerFactory.getLogger(WorkSheetRuleDaoImpl.class);
	@Autowired
	private JdbcTemplate jt;	
	private String quryFlowRuleSql;
	private String saveRuleObjSql;
	private String saveRuleSchemaRealSql;
	/**
	 * 根据工单模板ID得到规则类型
	 * @param worksheetSchemaId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public WorkSheetRule getSheetFlowRule(int worksheetSchemaId) {
		// SELECT B.* FROM TSP_WORKSHEET_RULE_RELA A,TSP_WORKSHEET_RULE B
		//WHERE A.RULE_ID = B.RULE_ID AND A.WORKSHEET_SCHEMA_ID=?
		String strSql = this.quryFlowRuleSql;
		List tmp = jt.query(strSql, new Object[]{worksheetSchemaId},new WorkSheetRuleRmp());
		int size = tmp.size();
		if(0==size) {
			if(log.isDebugEnabled()) {
				log.debug("未找模板ID为:"+worksheetSchemaId+"相对应的规则");
			}
			tmp.clear();
			tmp = null;
			return null;
		}
		WorkSheetRule bean = (WorkSheetRule)tmp.get(0);
		tmp.clear();
		tmp = null;		
		return bean;
	}
	/**
	 * 根据模板规则ID得到模板规则对象
	 * @param ruleId 模板规则ID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public WorkSheetRule getItemRule(int ruleId) {
		String strSql = "SELECT A.* FROM TSP_WORKSHEET_RULE A where A.RULE_ID=?";
		List tmp = jt.query(strSql, new Object[]{ruleId},new WorkSheetRuleRmp());
		int size = tmp.size();
		if(0==size) {
			if(log.isDebugEnabled()) {
				log.debug("根据模板规则ID:"+ruleId+"未找到对应的模板规则对象");
			}
			tmp.clear();
			tmp = null;
			return null;
		}
		WorkSheetRule bean = (WorkSheetRule)tmp.get(0);
		tmp.clear();
		tmp = null;		
		return bean;		
	}
	
	/**
	 * 保存工单模板规则对象
	 * @param bean
	 * @return
	 */
	public int saveRuleObj(WorkSheetRule bean) {
		String strSql = this.saveRuleObjSql;
		int size = this.jt.update(strSql,
				bean.getRuleId(),
				bean.getReuleName(),
				bean.getReuleDesc(),
				bean.getRuleType()
		);
		if(log.isDebugEnabled()) {
			log.debug("保存工单模板规则成功，规则ID为：:"+bean.getRuleId());
		}		
		return size;
	}
	
	/**
	 * 保存工单模板和模板规则关联表
	 * @param schemaId
	 * @param ruleId
	 * @return
	 */
	public int saveReleSchemaReal(int schemaId,int ruleId) {
		String strSql = this.saveRuleSchemaRealSql;
		int size = this.jt.update(strSql, schemaId, ruleId);
		if(log.isDebugEnabled()) {
			log.debug("保存工单模板规则关联成功，模板ID为：:"+schemaId);
		}
		return size;
	}
	/**
	 * 当更新流向部门时,更新模板规则时间
	 * @param ruleId
	 * @return
	 */
	public int updateRuleDate(int ruleId) {
		String strSql = "UPDATE TSP_WORKSHEET_RULE A SET A.PKG_EXP_DATE=SYSDATE WHERE A.RULE_ID=?";
		this.jt.update(strSql,ruleId);
		return ruleId;
	}
	//================================================================
	
	
	/**
	 * @return jt
	 */
	public JdbcTemplate getJt() {
		return jt;
	}
	/**
	 * @param jt 要设置的 jt
	 */
	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}
	/**
	 * @return quryFlowRuleSql
	 */
	public String getQuryFlowRuleSql() {
		return quryFlowRuleSql;
	}
	/**
	 * @param quryFlowRuleSql 要设置的 quryFlowRuleSql
	 */
	public void setQuryFlowRuleSql(String quryFlowRuleSql) {
		this.quryFlowRuleSql = quryFlowRuleSql;
	}
	/**
	 * @return saveRuleObjSql
	 */
	public String getSaveRuleObjSql() {
		return saveRuleObjSql;
	}
	/**
	 * @param saveRuleObjSql 要设置的 saveRuleObjSql
	 */
	public void setSaveRuleObjSql(String saveRuleObjSql) {
		this.saveRuleObjSql = saveRuleObjSql;
	}
	/**
	 * @return saveRuleSchemaRealSql
	 */
	public String getSaveRuleSchemaRealSql() {
		return saveRuleSchemaRealSql;
	}
	/**
	 * @param saveRuleSchemaRealSql 要设置的 saveRuleSchemaRealSql
	 */
	public void setSaveRuleSchemaRealSql(String saveRuleSchemaRealSql) {
		this.saveRuleSchemaRealSql = saveRuleSchemaRealSql;
	}

	
}
