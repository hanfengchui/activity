/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.dao.impl;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.workFlowOrg.dao.IworkSheetItmeDao;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRuleItem;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public class WorkSheetItmeDao implements IworkSheetItmeDao{
	private static final Logger log = LoggerFactory.getLogger(WorkSheetItmeDao.class);
	
	@Autowired
	private JdbcTemplate jt;
	private String saveItmeSql;
	private String saveItmeRuleSql;
	private String saveRuleItmeRealSql;
	
	/**
	 * 保存细类规则
	 * @param bena
	 * @return
	 */
	public int saveItmeObj(WorkSheetRuleItem bena) {
		String strSql = this.saveItmeSql;
		int size = this.jt.update(strSql,
				bena.getItemId(),
				bena.getItemName(),
				bena.getItemDesc(),
				bena.getEntiyId(),
				bena.getAttributeId()
		);
		if(log.isDebugEnabled()) {
			log.debug("保存细类规则对象成功"+bena.getItemId());
		}
		return size;
		
	}
	/**
	 * 保存模板规则和规则细类的关系
	 * @param ruleId
	 * @param itmeid
	 * @return
	 */
	public int saveRuleitmeReal(int ruleId,int itmeid) {
		String strSql = this.saveRuleItmeRealSql;
		int size = this.jt.update(strSql,
				ruleId,
				itmeid
		);
		if(log.isDebugEnabled()) {
			log.debug("保存模板规则和细类规则对象成功"+ruleId);
		}		
		return size;
	}
	/**
	 * 根据细类ID得到模板规则ID
	 * @param itemId
	 * @return
	 */
	public int getRuleId(int itemId) {
		String strSql = "SELECT A.RULE_ID FROM TSP_WORKSHEET_RULE_ITEM_RELA A WHERE A.ITEM_ID=?";
		int ruleId = 0;
		//= this.jt.queryForInt(strSql,new Object[]{new Integer(itemId)});
		List tmp  =  this.jt.queryForList(strSql, itemId);//(strSql,new Object[]{new Integer(itemId)});
		if(!tmp.isEmpty()) {
			Map map = (Map) tmp.get(0);
			ruleId = Integer.parseInt(map.get("RULE_ID").toString());
		}
		return ruleId;
	}
//=================================

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
	 * @return saveItmeRuleSql
	 */
	public String getSaveItmeRuleSql() {
		return saveItmeRuleSql;
	}

	/**
	 * @param saveItmeRuleSql 要设置的 saveItmeRuleSql
	 */
	public void setSaveItmeRuleSql(String saveItmeRuleSql) {
		this.saveItmeRuleSql = saveItmeRuleSql;
	}

	/**
	 * @return saveItmeSql
	 */
	public String getSaveItmeSql() {
		return saveItmeSql;
	}

	/**
	 * @param saveItmeSql 要设置的 saveItmeSql
	 */
	public void setSaveItmeSql(String saveItmeSql) {
		this.saveItmeSql = saveItmeSql;
	}
	/**
	 * @return saveRuleItmeRealSql
	 */
	public String getSaveRuleItmeRealSql() {
		return saveRuleItmeRealSql;
	}
	/**
	 * @param saveRuleItmeRealSql 要设置的 saveRuleItmeRealSql
	 */
	public void setSaveRuleItmeRealSql(String saveRuleItmeRealSql) {
		this.saveRuleItmeRealSql = saveRuleItmeRealSql;
	}
	
}
