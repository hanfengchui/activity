/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.dao.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.workFlowOrg.dao.IworkSheetSchemaDao;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetSchema;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetSchemaRmp;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public class WorkSheetSchemaDaoImpl implements IworkSheetSchemaDao {

	private static final Logger log = LoggerFactory.getLogger(WorkSheetSchemaDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jt;
	private String getWorkSchemaSql;
	private String saveSchemaobjSql;
	
	@SuppressWarnings("unchecked")
	public WorkSheetSchema getFlowSchema(int tacheId, int workSheetType, int serviceType) {
	    String sql = "SELECT * FROM TSP_WORKSHEET_SCHEMA A WHERE A.TACHE_ID = ? AND A.WORKSHEET_TYPE = ? AND A.WORKSHEET_CATEGORY = ?";
	    List tmp = jt.query(sql, 
	            new Object[]{tacheId, workSheetType, serviceType}, 
	            new WorkSheetSchemaRmp());
        if(tmp.isEmpty()) {
            if(log.isDebugEnabled()) {
                log.debug("没有找到工单模板： TacheId = "+ tacheId + ", WorksheetType = " + workSheetType 
                        + ", WorkSheetCategory = "+ serviceType + ", 程序正常返回");
            }
            tmp.clear();
            tmp = null;
            return null;
        }
        
        WorkSheetSchema bean = (WorkSheetSchema)tmp.get(0);
        tmp.clear();
        tmp = null;
        return bean;
	}
	
	/**
	 * 根据环节ID和工单类型ID得到工单模板
	 * @param tacheId 环节ID
	 * @param workSheetType 工单类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public WorkSheetSchema getFlowSchema(int tacheId, int workSheetType) {
		//SELECT * FROM TSP_WORKSHEET_SCHEMA A WHERE A.TACHE_ID=? AND A.WORKSHEET_TYPE=?
		String strSql = this.getWorkSchemaSql;
		List tmp = jt.query(strSql, new Object[]{tacheId, workSheetType},new WorkSheetSchemaRmp());
		int size = tmp.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("未找到环节ID为"+tacheId+"且工单类型为:"+workSheetType+"的工单模板,程序正常返回");
			}
			
			tmp.clear();
			tmp = null;
			return null;
		}
		WorkSheetSchema bean = (WorkSheetSchema)tmp.get(0);
		tmp.clear();
		tmp = null;
		return bean;
	}
	/**
	 * 根据模板规则ID得到模板对象
	 * @param ruleId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public WorkSheetSchema getRuleFlowObj(int ruleId) {
		
		String strSql = "SELECT A.* FROM TSP_WORKSHEET_SCHEMA A,TSP_WORKSHEET_RULE_RELA B WHERE A.WORKSHEET_SCHEMA_ID = B.WORKSHEET_SCHEMA_ID" +
				"  AND B.RULE_ID=?";
		List tmp = this.jt.query(strSql, new Object[]{ruleId},new WorkSheetSchemaRmp());
		int size = tmp.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("为找到模板规则ID为："+ruleId+"的工单模板,程序正常返回WorkSheetSchema getRuleFlowObj(int ruleId");
			}
			
			tmp.clear();
			tmp = null;
			return null;
		}
		WorkSheetSchema bean = (WorkSheetSchema)tmp.get(0);
		tmp.clear();
		tmp = null;
		return bean;		
	}
	/**
	 * 根据模板对象保存工单模板
	 * @param bean
	 * @return
	 */
	public int saveFlowSchema(WorkSheetSchema bean) {
		String strSql = this.saveSchemaobjSql;
		int size = this.jt.update(strSql,
						bean.getWorksheetSchemaId(),
						bean.getWflId(),
						bean.getWflSeqNbr(),
						bean.getTachId(),
						bean.getWorkSheetType(),
						bean.getWorkSheetCategory(),
						StringUtils.defaultIfEmpty(bean.getFlowOptMethod(),null),
						StringUtils.defaultIfEmpty(bean.getFlowOptParam(),null),
						StringUtils.defaultIfEmpty(bean.getSendXml(),null),
						StringUtils.defaultIfEmpty(bean.getReceiveXml(),null),
						bean.getProdConfig(),
						bean.getReverseWorksheetSchemaId()
					);
		if(log.isDebugEnabled()) {
			log.debug("保存工单模板数为:"+size);
		}
		return size;
	}
	//=====================以下为GET,SET方法==============================================
	/**
	 * @return getWorkSchemaSql
	 */
	public String getGetWorkSchemaSql() {
		return getWorkSchemaSql;
	}

	/**
	 * @param getWorkSchemaSql 要设置的 getWorkSchemaSql
	 */
	public void setGetWorkSchemaSql(String getWorkSchemaSql) {
		this.getWorkSchemaSql = getWorkSchemaSql;
	}

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
	 * @return saveSchemaobjSql
	 */
	public String getSaveSchemaobjSql() {
		return saveSchemaobjSql;
	}

	/**
	 * @param saveSchemaobjSql 要设置的 saveSchemaobjSql
	 */
	public void setSaveSchemaobjSql(String saveSchemaobjSql) {
		this.saveSchemaobjSql = saveSchemaobjSql;
	}

}
