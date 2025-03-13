/**
 * @author 万荣伟
 */
package com.timesontransfar.sheetCheck.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.sheetCheck.dao.IsheetCheckSchemDao;
import com.timesontransfar.sheetCheck.pojo.QualityContentSave;
import com.timesontransfar.sheetCheck.pojo.QualitySheet;
import com.timesontransfar.sheetCheck.pojo.SheetCheckSchem;


/**
 * @author 万荣伟
 *
 */
public class SheetCheckSchemDao implements IsheetCheckSchemDao {
	static final Logger log = LoggerFactory.getLogger(SheetCheckSchemDao.class);
	
	private JdbcTemplate jt;
	
	private String saveSql;
	private String updateSql;
	
	/**
	 * 保存质检模板
	 * @param bean
	 * @return
	 */
	public int saveCheckSchem(SheetCheckSchem bean) {
		int size = this.jt.update(this.saveSql,
				StringUtils.defaultIfEmpty(bean.getSchemId(),null),
				StringUtils.defaultIfEmpty(bean.getSchemName(),null),
				StringUtils.defaultIfEmpty(bean.getUpSchemId(),null),
				bean.getAttributeId(),
				StringUtils.defaultIfEmpty(bean.getAttributeName(),null),
				bean.getTypeId(),
				StringUtils.defaultIfEmpty(bean.getTypeName(),null),
				StringUtils.defaultIfEmpty(bean.getContentdesc(),null)
		);
		if(log.isDebugEnabled()) {
			log.debug("保存质检模板数据"+size+"条");
		}
		return size;
	}
	
	/**
	 * 更新模板
	 * @param bean
	 * @return
	 */
	public int updateCheckSchem(SheetCheckSchem bean) {
		int size = this.jt.update(this.updateSql,
				StringUtils.defaultIfEmpty(bean.getSchemId(),null),
				StringUtils.defaultIfEmpty(bean.getSchemName(),null),
				StringUtils.defaultIfEmpty(bean.getUpSchemId(),null),
				bean.getAttributeId(),
				StringUtils.defaultIfEmpty(bean.getAttributeName(),null),
				bean.getTypeId(),
				StringUtils.defaultIfEmpty(bean.getTypeName(),null),
				StringUtils.defaultIfEmpty(bean.getContentdesc(),null),
				StringUtils.defaultIfEmpty(bean.getSchemId(),null)
		);
		if(log.isDebugEnabled()) {
			log.debug("更新质检模板数据"+size+"条");
		}
		return size;		
	}
	/**
	 * 根据质检类型得到质检模板
	 * @param typeId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List getSheetCheckSchem(int typeId) {
		String strSql="select * from cc_sheet_check_schem a where a.type_id=?";
		return this.jt.queryForList(strSql, typeId);
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
	 * @return saveSql
	 */
	public String getSaveSql() {
		return saveSql;
	}


	/**
	 * @param saveSql 要设置的 saveSql
	 */
	public void setSaveSql(String saveSql) {
		this.saveSql = saveSql;
	}

	/**
	 * @return updateSql
	 */
	public String getUpdateSql() {
		return updateSql;
	}

	/**
	 * @param updateSql 要设置的 updateSql
	 */
	public void setUpdateSql(String updateSql) {
		this.updateSql = updateSql;
	}
	
	@SuppressWarnings("rawtypes")
	public List getTemplateList(int tacheId) {
		String strSql = "select t.TEMPLATE_ID,t.TEMPLATE_NAME from " +
				"cc_quality_template_tache c,cc_quality_template t " + 
				"where c.template_id=t.template_id and c.tache_id = ?";
		return this.jt.queryForList(strSql, tacheId);
	}
	
	public int saveQualitySheet(QualitySheet sheet) {
		String sql = "insert into cc_quality_sheet(" + 
				"work_sheet_id," + 
				"service_order_id," + 
				"service_type," + 
				"service_type_desc," + 
				"tache_id," + 
				"tache_desc," + 
				"template_id," + 
				"return_org_id," + 
				"return_org_name," + 
				"return_staff," + 
				"return_staff_name," + 
				"allto_date," + 
				"check_limit," + 
				"check_org_id," + 
				"check_org_name," + 
				"check_staff," + 
				"check_staff_name," + 
				"check_status," + 
				"deal_staff," + 
				"deal_staff_name," + 
				"deal_org_id," + 
				"deal_org_name) " + 
				"select %s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,"
				+ "now(),%s,o.org_id,o.org_name,t.staff_id,t.staffname,%s,%s,%s,%s,%s "
				+ "from tsm_staff t,tsm_organization o where t.org_id = o.org_id and t.staff_id=%s";
		Object[] arr = new Object[18];
		arr[0] = "'"+sheet.getSheetId()+"'";
		arr[1] = "'"+sheet.getOrderId()+"'";
		arr[2] = sheet.getServType();
		arr[3] = "'"+sheet.getServTypeDesc()+"'";
		arr[4] = sheet.getTacheId();
		arr[5] = "'"+sheet.getTacheDesc()+"'";
		arr[6] = "'"+sheet.getTemplateId()+"'";
		arr[7] = "'"+sheet.getReturnOrdId()+"'";
		arr[8] = "'"+sheet.getReturnOrgName()+"'";
		arr[9] = sheet.getReturnStaff();
		arr[10] = "'"+sheet.getReturnStaffName()+"'";
		arr[11] = sheet.getChecklimit();
		arr[12] = sheet.getCheckStatus();
		arr[13] = sheet.getDealStaff();
		arr[14] = "'"+sheet.getDealStaffName()+"'";
		arr[15] = "'"+sheet.getDealOrgId()+"'";
		arr[16] = "'"+sheet.getDealOrgName()+"'";
		arr[17] = sheet.getCheckStaff();
		sql = String.format(sql, arr);
		int size = this.jt.update(sql);
		if(size > 0) {
			sql = "update cc_work_sheet_his set precontract_flag = 1 where work_sheet_id = ?";
			return this.jt.update(sql, sheet.getSheetId());
		}
		return size;
	}
	
	@SuppressWarnings("rawtypes")
	public List getRowListByTemplateId(String templateId) {
		String strSql = "select c.ROW_ID,c.ROW_NAME,c.ROW_SORT from cc_quality_template_row c where c.template_id=? order by c.row_sort";
		return this.jt.queryForList(strSql, templateId);
	}
	
	@SuppressWarnings("rawtypes")
	public List getEleListByRowId(String rowId) {
		String strSql = "select a.ELE_ID,b.ELE_NAME,a.ELE_SORT from cc_quality_template_sort a,cc_quality_template_ele b "
				+ "where a.ele_id=b.ele_id and a.row_id=? order by a.ele_sort";
		return this.jt.queryForList(strSql, rowId);
	}
	
    public int insertQualityContentSave(final List<QualityContentSave> saveList) {
    	String sql = "insert into cc_quality_content_save (WORK_SHEET_ID, SERVICE_ORDER_ID, TEMPLATE_ID, ROW_ID, ROW_NAME, ELE_ID, ELE_NAME, ELE_ANSWER, ELE_TEXT, ROW_SORT, ELE_SORT, STATUS)" + 
    			"values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int[] i = jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                ps.setString(1, saveList.get(j).getSheetId());
                ps.setString(2, saveList.get(j).getOrderId());
                ps.setString(3, saveList.get(j).getTemplateId());
                ps.setString(4, saveList.get(j).getRowId());
                ps.setString(5, saveList.get(j).getRowName());
                ps.setString(6, saveList.get(j).getEleId());
                ps.setString(7, saveList.get(j).getEleName());
                ps.setInt(8, saveList.get(j).isEleAnswer() ? 1 : 0);
                ps.setString(9, saveList.get(j).getEleText() == null ? "" : saveList.get(j).getEleText());
                ps.setInt(10, saveList.get(j).getRowSort());
                ps.setInt(11, saveList.get(j).getEleSort());
                ps.setString(12, saveList.get(j).getStatus());
            }
            public int getBatchSize() {
                return saveList.size();
            }
        });
        return i.length;
    }
    
    public int updateQualitySheet(QualitySheet sheet) {
    	String sql = "update cc_quality_sheet c set c.check_date=now(), c.check_status=?, c.approve_rate=? where c.work_sheet_id=?";
    	return this.jt.update(sql, sheet.getCheckStatus(), sheet.getApproveRate(), sheet.getSheetId());
    }
    
    @SuppressWarnings("rawtypes")
	public List getEleAnswerListByRowId(String sheetId, String rowId) {
		String strSql = "select a.ELE_ID,b.ELE_NAME,a.ELE_SORT,s.ELE_ANSWER,s.ELE_TEXT from "
				+ "cc_quality_template_sort a,cc_quality_template_ele b,cc_quality_content_save s " + 
				"where a.row_id=s.row_id and a.ele_id = s.ele_id and a.ele_id=b.ele_id "
				+ "and a.row_id=? and s.status='1' and s.work_sheet_id=? order by a.ele_sort";
		return this.jt.queryForList(strSql, rowId, sheetId);
	}
    
    public int saveQualityAppeal(QualitySheet sheet) {
    	String sql = "update cc_quality_sheet c set c.check_status=?,c.appeal_reason=?,c.appeal_date=now() where c.work_sheet_id=?";
    	return this.jt.update(sql, sheet.getCheckStatus(), sheet.getAppealReason(), sheet.getSheetId());
    }
    
    public int saveQualityApprove(QualitySheet sheet) {
    	int size = 0;
    	if(sheet.getCheckStatus() == 3) {
    		String sql = "update cc_quality_sheet c set c.check_status=?,c.approve_rate=?,c.approve_content=?,c.approve_date=now() where c.work_sheet_id=?";
    		size = this.jt.update(sql, sheet.getCheckStatus(), sheet.getApproveRate(), sheet.getApproveContent(), sheet.getSheetId());
    	}
    	else if(sheet.getCheckStatus() == 4) {
    		String sql = "update cc_quality_sheet c set c.check_status=?,c.approve_content=?,c.approve_date=now() where c.work_sheet_id=?";
    		size = this.jt.update(sql, sheet.getCheckStatus(), sheet.getApproveContent(), sheet.getSheetId());
    	}
    	return size;
    }
    
    public int deleteQualityContentSave(String sheetId) {
    	String sql = "update cc_quality_content_save c set c.status='0' where c.work_sheet_id=?";
    	return this.jt.update(sql, sheetId);
    }
	
}
