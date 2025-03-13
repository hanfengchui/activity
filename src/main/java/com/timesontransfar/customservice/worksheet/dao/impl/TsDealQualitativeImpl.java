package com.timesontransfar.customservice.worksheet.dao.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.worksheet.dao.ItsDealQualitative;

import net.sf.json.JSONObject;

public class TsDealQualitativeImpl implements ItsDealQualitative {

	@Autowired
	private JdbcTemplate jt;

	private String updateDealQualitativeSql;

	private String deleteDealQualitativeSql;
	
	private String saveDealQualitativeHisSql;
	

	/**
	 * 删除部门处理定性表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteDealQualitative(String serviceOrderId, String workSheetId,int region) {
		return jt.update(this.deleteDealQualitativeSql, serviceOrderId, region);
	}

	/**
	 * 部门处理定性历史表添加记录
	 * @param servid 定单号
	 * @param sheetid 工单号
	 * @param month 月分区
	 * @return
	 */
	public int saveDealQualitativeHis(String servid, String sheetid, int region) {		
		return jt.update(this.saveDealQualitativeHisSql, servid, region);
	}
	
	public String getDeleteDealQualitativeSql() {
		return deleteDealQualitativeSql;
	}
	public void setDeleteDealQualitativeSql(String deleteDealQualitativeSql) {
		this.deleteDealQualitativeSql = deleteDealQualitativeSql;
	}
	public JdbcTemplate getJt() {
		return jt;
	}
	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}
	public String getUpdateDealQualitativeSql() {
		return updateDealQualitativeSql;
	}
	public void setUpdateDealQualitativeSql(String updateDealQualitativeSql) {
		this.updateDealQualitativeSql = updateDealQualitativeSql;
	}
	public String getSaveDealQualitativeHisSql() {
		return saveDealQualitativeHisSql;
	}
	public void setSaveDealQualitativeHisSql(String saveDealQualitativeHisSql) {
		this.saveDealQualitativeHisSql = saveDealQualitativeHisSql;
	}

	@Override
	public int saveReceiptEval(JSONObject ins) {
		String sql=" insert into CC_SERVICE_RECEIPT_EVAL\n" + 
				"   (SERVICE_ORDER_ID,\n" + 
				"    WORK_SHEET_ID,\n" + 
				"    OPRATION_TIME,\n" + 
				"    TACHE_ID,\n" + 
				"    TACHE_DESC,\n" + 
				"    OPRATION_FIRST_ID,\n" + 
				"    OPRATION_FIRST_DESC,\n" + 
				"    OPRATION_SECEND_ID,\n" + 
				"    OPRATION_SECEND_DESC)\n" + 
				" values(?,?,NOW(),?,?,?,?,?,?)";
		String oprationFirstId = ins.getString("opration_first_id");
		int firstId = StringUtils.isBlank(oprationFirstId) ? 0 : Integer.parseInt(oprationFirstId);
		return jt.update(sql,
				ins.getString("service_order_id"),
				ins.getString("work_sheet_id"),
				new Integer(ins.getString("tache_id")),
				StringUtils.defaultIfEmpty(ins.getString("tache_desc"), null),
				firstId,
				StringUtils.defaultIfEmpty(ins.getString("opration_first_desc"), null),
				StringUtils.defaultIfEmpty(ins.getString("opration_secend_id"), null),
				StringUtils.defaultIfEmpty(ins.getString("opration_secend_desc"), null));
	}

}
