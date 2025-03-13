/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.timesontransfar.customservice.workFlowOrg.dao.IworkSheetFlowOrgDao;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetFlowOrg;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetFlowOrgRmp;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public class WorkSheetFlowOrgDaoImpl implements IworkSheetFlowOrgDao{

	private static final Logger log =LoggerFactory.getLogger(WorkSheetFlowOrgDaoImpl.class);
	@Autowired
	private JdbcTemplate jt;
	private String quryFlowOrgListSql;//得到流向工位的数组
	private String saveFlowOrgsql;//保存工单流向工位
	
	/**
	 * 根据工单模板,环节标志,地域,环节,规则标志得到匹配对象
	 * @param sheetFlowOrg
	 * @return 数组对象
	 */
	@SuppressWarnings("unchecked")
	public WorkSheetFlowOrg[] getFlowOrgId(WorkSheetFlowOrg sheetFlowOrg) {
		// SELECT * FROM TSP_WORKSHEET_FLOW A WHERE A.WORKSHEET_SCHEMA_ID=? AND A.TACHE_ID=? AND A.REGION_ID=? AND A.RULE_ID=?
		String strSql = this.quryFlowOrgListSql;
		List tmp = jt.query(strSql, new Object[]{ sheetFlowOrg.getWorksheetSchemaId(), 
				sheetFlowOrg.getTachId(), sheetFlowOrg.getRegionId(), sheetFlowOrg.getRuleId()}, new WorkSheetFlowOrgRmp());
		int size = tmp.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("未找到模板ID为:"+sheetFlowOrg.getWorksheetSchemaId()+"流向配置");
			}
			tmp.clear();
			tmp = null;
			return null;
		}
		WorkSheetFlowOrg[] bean = new WorkSheetFlowOrg[size];
		for(int i=0;i<size;i++) {
			bean[i] = (WorkSheetFlowOrg)tmp.get(i);
		}
		tmp.clear();
		tmp=null;
		return bean;
	}
	
	/**
	 * 保存工单流向对象
	 * @param bean
	 * @return
	 */
	public int saveflowOrgObj(WorkSheetFlowOrg bean) {
		String strSql = this.saveFlowOrgsql;
		int size = this.jt.update(strSql,
				bean.getWsFlowRuleId(),
				bean.getWsNbr(),
				bean.getWorksheetSchemaId(),
				bean.getRegionId(),
				bean.getRuleId(),
				bean.getItemId(),
				bean.getItemVaule(),
				bean.getFlowOrgid(),
				bean.getTachId()
		);
		if(log.isDebugEnabled()) {
			log.debug("保存工单流向数："+size);
		}
		return size;
	}
	/**
	 * 修改流向工位
	 * @param wsFlowruleId
	 * @param orgId
	 * @return
	 */
	public int updateFlowOrg(int wsFlowruleId,int orgId,int region) {
		String strSql = "UPDATE TSP_WORKSHEET_FLOW A SET A.FLOW_ORG_ID=? WHERE A.WS_FLOW_RULE_ID=? AND A.REGION_ID=?";
		//String strRuleSql = "UPDATE TSP_WORKSHEET_RULE A SET";
		return this.jt.update(strSql, orgId, wsFlowruleId, region);
		
	}
	/**
	 * 根据静态ID得到部门(SP,网厅/掌厅的投诉流程)
	 * @param refId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getSataticOrg(int refId) {
		String strSql = "SELECT R.COL_VALUE_HANDLING FROM PUB_COLUMN_REFERENCE R WHERE R.REFER_ID=?";
		List tmp = this.jt.query(strSql, new Object[]{refId},new KeyRowMapper());
		if(tmp.isEmpty()) {
			if(log.isDebugEnabled()) {
				log.debug("没有查询到REFID为:"+refId+"的信息");
			}
			return "A";
		}
		String orgId = tmp.get(0).toString();
		tmp.clear();
		tmp = null;
		return orgId;
	}
	
	class KeyRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int arg1) throws SQLException {
			if(rs.getString(1) == null) {
				return "A";
			}
			return rs.getString(1);
		}
	}
//========================================
	
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
	 * @return quryFlowOrgListSql
	 */
	public String getQuryFlowOrgListSql() {
		return quryFlowOrgListSql;
	}

	/**
	 * @param quryFlowOrgListSql 要设置的 quryFlowOrgListSql
	 */
	public void setQuryFlowOrgListSql(String quryFlowOrgListSql) {
		this.quryFlowOrgListSql = quryFlowOrgListSql;
	}

	/**
	 * @return saveFlowOrgsql
	 */
	public String getSaveFlowOrgsql() {
		return saveFlowOrgsql;
	}

	/**
	 * @param saveFlowOrgsql 要设置的 saveFlowOrgsql
	 */
	public void setSaveFlowOrgsql(String saveFlowOrgsql) {
		this.saveFlowOrgsql = saveFlowOrgsql;
	}
	
	
	

}
