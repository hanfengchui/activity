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

import com.timesontransfar.sheetCheck.dao.IsheetCheckAdjuDao;
import com.timesontransfar.sheetCheck.pojo.SheetCheckAdju;
import com.timesontransfar.sheetCheck.pojo.SheetCheckAdjuRmp;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public class SheetCheckAdjuDao implements IsheetCheckAdjuDao {
	private static final Logger log = LoggerFactory.getLogger(SheetCheckAdjuDao.class);	
	
	private JdbcTemplate jt;
	
	private String saveSql;
	
	/**
	 * 保存工单质检评判标准
	 * @param bean 评判对象
	 * @param checkId质检流水
	 * @return
	 */
	public int saveSheetCheckAdjuDao(SheetCheckAdju[] bean,String checkId) {
		final SheetCheckAdju[] beanObj = bean;
		final String id = checkId;
		
		int[] i= this.jt.batchUpdate(this.saveSql, new BatchPreparedStatementSetter(){

			public int getBatchSize() {
				return beanObj.length;
			}

			public void setValues(PreparedStatement ps, int j) throws SQLException {
				ps.setString(1, id);
				ps.setInt(2, beanObj[j].getTypeId());
				ps.setString(3, StringUtils.defaultIfEmpty(beanObj[j].getTypeName(),null));
				ps.setInt(4, beanObj[j].getAttriId());
				ps.setString(5, StringUtils.defaultIfEmpty(beanObj[j].getAttriName(),null));
				ps.setString(6, StringUtils.defaultIfEmpty(beanObj[j].getSchemid(),null));
				ps.setString(7, StringUtils.defaultIfEmpty(beanObj[j].getSchemName(),null));
				ps.setString(8,StringUtils.defaultIfEmpty(beanObj[j].getContentDesc(),null) );
				ps.setInt(9, beanObj[j].getResult());
				ps.setString(10,StringUtils.defaultIfEmpty(beanObj[j].getProbliem(),null));
				ps.setString(11, StringUtils.defaultIfEmpty(beanObj[j].getOpinion(),null));
				ps.setInt(12, beanObj[j].getCheckEdition());
				
			}			
		});	
		if(log.isDebugEnabled()) {
			log.debug("保存评判标准记录:"+i.length+"条");
		}
		return i.length;
	}
	/**
	 * 得到质检评判标准
	 * @param checkId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SheetCheckAdju[] getSheetCheckAdjuHis(String checkId, int checkEdition) {
		String strSql = "SELECT GUID, CHECK_ID, TYPE_ID, TYPE_NAME, " +
				" ATTTIBUTE_ID, ATTTIBUTE_NAME, SCHEM_ID," +
				" SCHEM_NAME, CONTENT_DESC, RESULT, EXIST_PROBLIEM, " +
				"IMPROME_OPINION , CHECK_EDITION" +
				" FROM CC_SHEET_CHECK_ADJU_HIS WHERE CHECK_ID=? AND CHECK_EDITION = ?";
		List list = this.jt.query(strSql, new Object[]{checkId,checkEdition},new SheetCheckAdjuRmp());
		int size = list.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("根据质检流水号:"+checkId+";没有查到相关的质检评判");
			}
			list.clear();
			list=null;
			return new SheetCheckAdju[0];
		}
		SheetCheckAdju[] bean = new SheetCheckAdju[size];
		for(int i=0;i<size;i++){
			bean[i] = (SheetCheckAdju)list.get(i);
		}
		list.clear();
		list=null;		
		return bean;
	}
	/**
	 * 得到质检评判标准
	 * @param checkId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SheetCheckAdju[] getSheetCheckAdju(String checkId) {
		String strSql = "SELECT GUID, CHECK_ID, TYPE_ID, TYPE_NAME, " +
		" ATTTIBUTE_ID, ATTTIBUTE_NAME, SCHEM_ID," +
		" SCHEM_NAME, CONTENT_DESC, RESULT, EXIST_PROBLIEM, " +
		"IMPROME_OPINION , CHECK_EDITION" +
		" FROM CC_SHEET_CHECK_ADJU WHERE CHECK_ID=?";
		List list = this.jt.query(strSql, new Object[]{checkId},new SheetCheckAdjuRmp());
		int size = list.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("根据质检流水号:"+checkId+";没有查到相关的质检评判");
			}
			list.clear();
			list=null;
			return new SheetCheckAdju[0];
		}
		SheetCheckAdju[] bean = new SheetCheckAdju[size];
		for(int i=0;i<size;i++){
			bean[i] = (SheetCheckAdju)list.get(i);
		}
		list.clear();
		list=null;		
		return bean;
	}
	
	/**
	 * 删除质检模板信息
	 * @param checkId
	 * @return
	 */
	public int deleteSheetCheckAdju(String checkId){
		String delSql = "DELETE FROM CC_SHEET_CHECK_ADJU WHERE CHECK_ID = ?";
		return this.jt.update(delSql, checkId);
	}
	
	/**
	 * 质检模板信息移入历史表
	 * @param checkId
	 * @return
	 */
	public int moveSheetCheckAdjuToHis(String checkId){
		String delSql = 
			"INSERT INTO CC_SHEET_CHECK_ADJU_HIS\n" +
			"(GUID, CHECK_ID, TYPE_ID, TYPE_NAME, ATTTIBUTE_ID,\n" + 
			"ATTTIBUTE_NAME, SCHEM_ID, SCHEM_NAME, CONTENT_DESC, RESULT,\n" + 
			"EXIST_PROBLIEM, IMPROME_OPINION, CHECK_EDITION)\n" + 
			"SELECT GUID, CHECK_ID, TYPE_ID, TYPE_NAME,\n" + 
			"ATTTIBUTE_ID, ATTTIBUTE_NAME, SCHEM_ID, SCHEM_NAME,\n" + 
			"CONTENT_DESC, RESULT, EXIST_PROBLIEM, IMPROME_OPINION,\n" + 
			"CHECK_EDITION\n" + 
			"FROM CC_SHEET_CHECK_ADJU WHERE CHECK_ID = ?";
		return this.jt.update(delSql, checkId); 
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
}
