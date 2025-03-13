package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.worksheet.dao.ISJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitativeRmp;

@SuppressWarnings("rawtypes")
public class SJSheetQualitativeImpl implements ISJSheetQualitative {
	@Autowired
	private JdbcTemplate jt;

	private String insertSJSheetQualitativeSql;
	private String insertSJSheetQualitativeHisSql;
	private String deleteSJSheetQualitativeSql;
	private String selectSJSheetQualitativeSql;
	private String selectSJSheetQualitativeHisSql;

	public int saveSJSheetQualitative(SJSheetQualitative bean) {
		return jt.update(
				insertSJSheetQualitativeSql,
					   StringUtils.defaultIfEmpty(bean.getServiceOrderId(),null),
					   StringUtils.defaultIfEmpty(bean.getWorkSheetId(),null),
					   bean.getMonthFlag(),
					   bean.getOneSjCatalog(),
					   StringUtils.defaultIfEmpty(bean.getOneSjCatalogDesc(),null),
					   bean.getTwoSjCatalog(),
					   StringUtils.defaultIfEmpty(bean.getTwoSjCatalogDesc(),null),
					   bean.getThreeSjCatalog(),
					   StringUtils.defaultIfEmpty(bean.getThreeSjCatalogDesc(),null),
					   StringUtils.defaultIfEmpty(bean.getFourSjCatalogDesc(),null),
					   StringUtils.defaultIfEmpty(bean.getSjValid(),null),
					   bean.getSjRegionId(),
					   StringUtils.defaultIfEmpty(bean.getSjRegionName(),null));
	}

	public int saveSJSheetQualitativeHis(String serviceOrderId) {
		if (jt.update(insertSJSheetQualitativeHisSql, serviceOrderId) > 0) {
			return jt.update(deleteSJSheetQualitativeSql, serviceOrderId);
		} else {
			return 0;
		}
	}

	@SuppressWarnings("unchecked")
	public SJSheetQualitative getSJSheetQualitative(String workSheetId, boolean boo) {
		String sql = "";
		if (boo) {
			sql = selectSJSheetQualitativeSql;
		} else {
			sql = selectSJSheetQualitativeHisSql;
		}
		List tmp = this.jt.query(sql, new Object[] { workSheetId }, new SJSheetQualitativeRmp());
		if (tmp.isEmpty()) {
			return null;
		}
		return (SJSheetQualitative) tmp.get(0);
	}
	
	public int saveHisSJSheetQualitative(SJSheetQualitative bean) {
		String strSql = 
				"INSERT INTO cc_sj_qualitative_his" + 
				"(service_order_id," + 
				"work_sheet_id," + 
				"creat_data," + 
				"month_flag," + 
				"one_sj_catalog," + 
				"one_sj_catalog_desc," + 
				"two_sj_catalog," + 
				"two_sj_catalog_desc," + 
				"three_sj_catalog," + 
				"three_sj_catalog_desc," + 
				"four_sj_catalog_desc," + 
				"sj_valid," + 
				"sj_region_id," + 
				"sj_region_name) " + 
				"VALUES " +
				"(?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		return jt.update(strSql, 
				StringUtils.defaultIfEmpty(bean.getServiceOrderId(),null),
				StringUtils.defaultIfEmpty(bean.getWorkSheetId(),null),
				bean.getMonthFlag(), 
				bean.getOneSjCatalog(),
				StringUtils.defaultIfEmpty(bean.getOneSjCatalogDesc(),null),
				bean.getTwoSjCatalog(), 
				StringUtils.defaultIfEmpty(bean.getTwoSjCatalogDesc(),null),
				bean.getThreeSjCatalog(),
				StringUtils.defaultIfEmpty(bean.getThreeSjCatalogDesc(),null),
				StringUtils.defaultIfEmpty(bean.getFourSjCatalogDesc(),null),
				StringUtils.defaultIfEmpty(bean.getSjValid(),null),
				bean.getSjRegionId(), 
				StringUtils.defaultIfEmpty(bean.getSjRegionName(),null)
			);
	}

	public JdbcTemplate getJt() {
		return jt;
	}

	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}

	public String getInsertSJSheetQualitativeSql() {
		return insertSJSheetQualitativeSql;
	}

	public void setInsertSJSheetQualitativeSql(String insertSJSheetQualitativeSql) {
		this.insertSJSheetQualitativeSql = insertSJSheetQualitativeSql;
	}

	public String getInsertSJSheetQualitativeHisSql() {
		return insertSJSheetQualitativeHisSql;
	}

	public void setInsertSJSheetQualitativeHisSql(String insertSJSheetQualitativeHisSql) {
		this.insertSJSheetQualitativeHisSql = insertSJSheetQualitativeHisSql;
	}

	public String getDeleteSJSheetQualitativeSql() {
		return deleteSJSheetQualitativeSql;
	}

	public void setDeleteSJSheetQualitativeSql(String deleteSJSheetQualitativeSql) {
		this.deleteSJSheetQualitativeSql = deleteSJSheetQualitativeSql;
	}

	public String getSelectSJSheetQualitativeSql() {
		return selectSJSheetQualitativeSql;
	}

	public void setSelectSJSheetQualitativeSql(String selectSJSheetQualitativeSql) {
		this.selectSJSheetQualitativeSql = selectSJSheetQualitativeSql;
	}

	public String getSelectSJSheetQualitativeHisSql() {
		return selectSJSheetQualitativeHisSql;
	}

	public void setSelectSJSheetQualitativeHisSql(String selectSJSheetQualitativeHisSql) {
		this.selectSJSheetQualitativeHisSql = selectSJSheetQualitativeHisSql;
	}
}
