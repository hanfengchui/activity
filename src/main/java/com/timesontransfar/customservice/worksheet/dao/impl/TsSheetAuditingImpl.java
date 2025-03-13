package com.timesontransfar.customservice.worksheet.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.worksheet.dao.ItsSheetAuditing;
import com.timesontransfar.customservice.worksheet.pojo.ResponsiBilityOrg;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetAuditing;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetAuditingRmp;

@SuppressWarnings("rawtypes")
public class TsSheetAuditingImpl implements ItsSheetAuditing{
	private static final Logger log = LoggerFactory.getLogger(TsSheetAuditingImpl.class);
	
	private JdbcTemplate jt;
	
	private String saveTsSheetAuditingSql;
 
	private String deleteTsSheetAuditingSql;
	
	private String saveTsSheetAuditingHisSql;	
	
	private String saveResponsiBilityOrgSql;
	/**
	 * 责任部门表添加记录
	 * @param bean
	 * @return
	 */
	public int saveResponsiBilityOrg(ResponsiBilityOrg[] bean){		
		if(null == bean )
			return 0;
		if(bean.length < 1 )
			return 0;
	
		final ResponsiBilityOrg[] beanObj = bean;
		
		int[] i = this.jt.batchUpdate(this.saveResponsiBilityOrgSql, new BatchPreparedStatementSetter(){

			public int getBatchSize() {
				return beanObj.length;
			}

			public void setValues(PreparedStatement ps, int j) throws SQLException {
				ps.setString(1, StringUtils.defaultIfEmpty(beanObj[j].getServiceOrderId(),null));
				ps.setString(2,StringUtils.defaultIfEmpty(beanObj[j].getWorkSheetId(),null));
				ps.setInt(3, beanObj[j].getRegionId());
				ps.setInt(4, beanObj[j].getTacheId());
				ps.setString(5, StringUtils.defaultIfEmpty(beanObj[j].getDutyOrgId(),null));
				ps.setString(6, StringUtils.defaultIfEmpty(beanObj[j].getDutyOrgName(),null));
			}			
		});	
		if(log.isDebugEnabled()) {
			log.debug("保存责任部门记录:"+i.length+"条");
		}
		return i.length;		
	}
	
	public int saveResponsiBilityOrgHis(String orderId){
		if(null == orderId)
			return 0;
		int count = 0;
		String sql = 
			"INSERT INTO CC_RESPONSIBILITY_ORG_HIS\n" +
			"  (GUID,\n" + 
			"   SERVICE_ORDER_ID,\n" + 
			"   WORK_SHEET_ID,\n" + 
			"   REGION_ID,\n" + 
			"   TACHE_ID,\n" + 
			"   DUTY_ORG,\n" + 
			"   DUTY_ORG_NAME,\n" + 
			"   SYS_DATE)\n" + 
			"  SELECT GUID,\n" + 
			"         SERVICE_ORDER_ID,\n" + 
			"         WORK_SHEET_ID,\n" + 
			"         REGION_ID,\n" + 
			"         TACHE_ID,\n" + 
			"         DUTY_ORG,\n" + 
			"         DUTY_ORG_NAME,\n" + 
			"         SYS_DATE\n" + 
			"    FROM CC_RESPONSIBILITY_ORG\n" + 
			"   WHERE SERVICE_ORDER_ID = ? ";
		count = jt.update(sql, orderId);
		log.debug("保存责任部门历史表记录{}条", count);
		return count;
	}
	/**
	 * 删除责任部门记录
	 * @param serviceOrderId 定单号	
	 * @return
	 */
	public int deleteResponsiBilityOrg(String serviceOrderId) {
		String delSql = "DELETE FROM CC_RESPONSIBILITY_ORG WHERE SERVICE_ORDER_ID = ?";
		return jt.update(delSql, serviceOrderId);
	}
	
	/**
	 * 删除审核表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteTsSheetAuditing(String serviceOrderId, String workSheetId, int region) {
		return jt.update(this.deleteTsSheetAuditingSql, serviceOrderId, region);
	}
	/**
	 * 审核表添加记录
	 * @param bean
	 * @return
	 */
    public int saveTsSheetAuditing(TsSheetAuditing bean) {
    	String strsql = this.saveTsSheetAuditingSql;
//    	INSERT INTO CC_SHEET_AUDITING( SERVICE_ORDER_ID ,
//				WORK_SHEET_ID, REGION_ID , REGION_NAME , CREAT_DATE,
//				TS_RISK, DUTY_ORG , DUTY_ORG_NAME, RESOUND_MASS,
//				RESOUND_MASS_NAME, JOB_ERROR, ASSESS_ARTICLE,
//				ASSESS_ARTICLE_NAME, TACHE_ID, TACHE_DESC, SHEET_TYPE ,
//				SHEET_TYPE_DESC , MONTH_FLAG ) VALUES (?, ?, ?, ?,
//				TO_DATE(?,'yyyy-mm-dd hh24:mi:ss'), ?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,? )
    	return this.jt.update(strsql, 
				StringUtils.defaultIfEmpty(bean.getOrderId(),null),
				StringUtils.defaultIfEmpty(bean.getSheetId(),null),
				bean.getRegionId(),
				StringUtils.defaultIfEmpty(bean.getRegName(),null),
				//bean.getCreatData(),
				bean.getTsRisk(),				
				StringUtils.defaultIfEmpty(bean.getDutyOrg(),null),
				StringUtils.defaultIfEmpty(bean.getDutyOrgName(),null),
				bean.getResoundMass(),
				StringUtils.defaultIfEmpty(bean.getResoundMassName(),null),
				bean.getJobError(),
				bean.getAssessArticle(),
				StringUtils.defaultIfEmpty(bean.getAssessArticleName(),null),
				bean.getTacheId(),
				StringUtils.defaultIfEmpty(bean.getTacheName(),null),
				bean.getSheetType(),
				StringUtils.defaultIfEmpty(bean.getSheetTypeDesc(),null),
				bean.getMonthFlag(),
				bean.getUpgradeTs()
		);
	}
    /**
	 * 审核历史表添加记录
	 * @param servid
	 * @param sheetid
	 * @param month
	 * @return
	 */
	public int saveTsSheetAuditingHis(String servid, String sheetid, int region) {
		return jt.update(this.saveTsSheetAuditingHisSql, servid, region);
	}
	
	/**
	 * 得到工单审核对象
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TsSheetAuditing getTsSheetAuditing(String sheetId,int regionId,boolean boo) {
		String strSql = "";
		if(boo) {
			strSql = "SELECT * FROM CC_SHEET_AUDITING A WHERE A.WORK_SHEET_ID=? AND A.REGION_ID=?";
		} else {
			strSql = "SELECT * FROM CC_SHEET_AUDITING_HIS A WHERE A.WORK_SHEET_ID=? AND A.REGION_ID=?";
		}
		List tmp = this.jt.query(strSql, new Object[]{sheetId, regionId},new TsSheetAuditingRmp());
		int size = tmp.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("没有查询到工单好为:||"+sheetId+"审核内容");
			}
			return null;
		}
		return (TsSheetAuditing)tmp.get(0);
	}
	

	public String getDeleteTsSheetAuditingSql() {
		return deleteTsSheetAuditingSql;
	}

	public void setDeleteTsSheetAuditingSql(String deleteTsSheetAuditingSql) {
		this.deleteTsSheetAuditingSql = deleteTsSheetAuditingSql;
	}

	public JdbcTemplate getJt() {
		return jt;
	}

	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}

	public String getSaveTsSheetAuditingSql() {
		return saveTsSheetAuditingSql;
	}

	public void setSaveTsSheetAuditingSql(String saveTsSheetAuditingSql) {
		this.saveTsSheetAuditingSql = saveTsSheetAuditingSql;
	}

	public String getSaveTsSheetAuditingHisSql() {
		return saveTsSheetAuditingHisSql;
	}

	public void setSaveTsSheetAuditingHisSql(String saveTsSheetAuditingHisSql) {
		this.saveTsSheetAuditingHisSql = saveTsSheetAuditingHisSql;
	}

	public String getSaveResponsiBilityOrgSql() {
		return saveResponsiBilityOrgSql;
	}

	public void setSaveResponsiBilityOrgSql(String saveResponsiBilityOrgSql) {
		this.saveResponsiBilityOrgSql = saveResponsiBilityOrgSql;
	}



}
