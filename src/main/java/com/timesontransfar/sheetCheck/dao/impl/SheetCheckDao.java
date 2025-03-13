/**
 * @author 万荣伟
 */
package com.timesontransfar.sheetCheck.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.sheetCheck.dao.IsheetCheckDao;
import com.timesontransfar.sheetCheck.pojo.SheetCheckAppeal;
import com.timesontransfar.sheetCheck.pojo.SheetCheckInfo;
import com.timesontransfar.sheetCheck.pojo.SheetCheckInfoRmp;
import com.timesontransfar.sheetCheck.pojo.SheetCheckState;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SheetCheckDao implements IsheetCheckDao {
	private static final Logger log = LoggerFactory.getLogger(SheetCheckDao.class);
	
	private JdbcTemplate jt;
	
	private String saveSql;

	class KeyRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int arg1) throws SQLException {
			return rs.getString(1);
		}
	}

	public String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public int setSeqVal(String seqName, String seqId) {
		String sql = "INSERT INTO " + seqName + "(seq_id, create_date) VALUES (?, current_timestamp(3))";
		return this.jt.update(sql, seqId);
	}

	public String getSeqNum(String seqName, String seqId, int len) {
		String sql = "select lpad(right(seq_num," + len + "), " + len + ", 0) from " + seqName + " where seq_id = ?";
		List tmpList = this.jt.query(sql, new Object[] {seqId}, new KeyRowMapper());
		if(!tmpList.isEmpty()) {
			return tmpList.get(0).toString();
		}
		return null;
	}

	/**
	 * 保存工单质检信息
	 * @param sheetCheckInfo
	 * @return
	 */
	public int saveSheetCheck(SheetCheckInfo bean) {
		int size = this.jt.update(this.saveSql,
				StringUtils.defaultIfEmpty(bean.getCheckId(),null),
				bean.getTypeId(),
				StringUtils.defaultIfEmpty(bean.getTypeName(),null),
				StringUtils.defaultIfEmpty(bean.getServiceOrderId(),null),
				StringUtils.defaultIfEmpty(bean.getWorkSheetId(),null),
				StringUtils.defaultIfEmpty(bean.getCheckOrgId(),null),
				StringUtils.defaultIfEmpty(bean.getCheckOrgName(),null),
				bean.getCheckStaffId(),
				StringUtils.defaultIfEmpty(bean.getCheckStaffName(),null),
				StringUtils.defaultIfEmpty(bean.getOrgId(),null),
				StringUtils.defaultIfEmpty(bean.getOrgName(),null),
				bean.getStaffId(),
				StringUtils.defaultIfEmpty(bean.getStaffName(),null),
				StringUtils.defaultIfEmpty(bean.getContentDesc(),null),
				StringUtils.defaultIfEmpty(bean.getEvaluste(),null) ,
				StringUtils.defaultIfEmpty(bean.getAppealOrgId(),null),
				StringUtils.defaultIfEmpty(bean.getAppealOrgName(),null),
				bean.getAppealStaffId(),
				StringUtils.defaultIfEmpty(bean.getAppealStaffName(),null),
				StringUtils.defaultIfEmpty(bean.getAppealContent(),null),
				bean.getCheckState(),
				StringUtils.defaultIfEmpty(bean.getCheckStateName(),null),
				bean.getCheckEdition(),
				bean.getCheckOut(),
				bean.getMortalErr(),
				bean.getNotMortalErr()
		);
		return size;
	}
	
	/**
	 * 修改工单质检信息
	 * @param sheetCheckInfo
	 * @return
	 */
	public int updateSheetCheckInfo(SheetCheckInfo bean) {
		String sql = 
					"UPDATE  CC_SHEET_CHECK\n" +
					"   SET TYPE_ID = ? ,\n" + 
					"       TYPE_NAME = ?,\n" + 
					"       SERVICE_ORDER_ID = ?,\n" + 
					"       WORK_SHEET_ID = ?,\n" + 
					"       CHECK_ORG_ID = ?,\n" + 
					"       CHECK_ORG_NAME = ?,\n" + 
					"       CHECK_STAFF_ID = ?,\n" + 
					"       CHECK_STAFF_NAME = ?,\n" + 
					"       ORG_ID = ?,\n" + 
					"       ORG_NAME = ?,\n" + 
					"       STAFF_ID = ?,\n" + 
					"       STAFF_NAME = ?,\n" + 
					"       CONTENT_DESC = ?,\n" + 
					"       EVALUSTE = ?,\n" + 
					"       CREAT_DATE = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'),\n" +
					"       CHECK_STATE = ?,\n" + 
					"       CHECK_STATE_NAME = ?,\n" + 
					"       CHECK_EDITION = ?,\n" + 
					"       CHECK_OUT = ?,\n" + 
					"       APPEAL_REPLY =? \n" + 
					" WHERE CHECK_ID = ?" ;
		int size = this.jt.update(sql,
				bean.getTypeId(),
				bean.getTypeName(),
				bean.getServiceOrderId(),
				bean.getWorkSheetId(),
				bean.getCheckOrgId(),
				bean.getCheckOrgName(),
				bean.getCheckStaffId(),
				bean.getCheckStaffName(),
				bean.getOrgId(),
				bean.getOrgName(),
				bean.getStaffId(),
				bean.getStaffName(),
				bean.getContentDesc(),
				bean.getEvaluste() , 
				bean.getCreatDate(),
				bean.getCheckState(),
				bean.getCheckStateName(),
				bean.getCheckEdition(),
				bean.getCheckOut(),
				bean.getAppealReply(),
				bean.getCheckId()  
		);
		return size;
	}
	
	/**
	 * 得到质检流水ID
	 * @return
	 */
	public String getCheckId() {
		String seqId = this.getSeqVal("SEQ_SHEET_CHECK", 6);
		String strSql = "SELECT CONCAT(DATE_FORMAT(NOW(), '%y%m%d'), ?) FROM dual";
		String checkId = this.jt.queryForObject(strSql, new Object[] { seqId }, String.class);
		return checkId;
	}




	/**
	 * 获取指定长度的自增序列
	 */
	public String getSeqVal(String seqName, int len) {
		String seqId = this.getUUID();
		int ct = this.setSeqVal(seqName, seqId);
		if(ct > 0) {
			return this.getSeqNum(seqName, seqId, len);
		}
		return null;
	}


	/**
	 * 根据服务单id获取质检列表
	 * @param ordId
	 * @return
	 */
	public List queryCheckBeansByOrdid(String ordId){
		String strSql =
			"SELECT CHECK_ID,\n" +
			"       TYPE_ID,\n" + 
			"       TYPE_NAME,\n" + 
			"       SERVICE_ORDER_ID,\n" + 
			"       WORK_SHEET_ID,\n" + 
			"       CHECK_ORG_ID,\n" + 
			"       CHECK_ORG_NAME,\n" + 
			"       CHECK_STAFF_ID,\n" + 
			"       CHECK_STAFF_NAME,\n" + 
			"       ORG_ID,\n" + 
			"       ORG_NAME,\n" + 
			"       STAFF_ID,\n" + 
			"       STAFF_NAME,\n" + 
			"       CONTENT_DESC,\n" + 
			"       EVALUSTE,\n" + 
			"       DATE_FORMAT(CREAT_DATE, '%Y%m%d%H%i%s') CREAT_DATE,\n" +
			"       APPEAL_ORG_ID,\n" + 
			"       APPEAL_ORG_NAME,\n" + 
			"       APPEAL_STAFF_ID,\n" + 
			"       APPEAL_STAFF_NAME,\n" + 
			"       DATE_FORMAT(APPEAL_DATA, '%Y%m%d%H%i%s') APPEAL_DATA,\n" +
			"       APPEAL_CONTENT,\n" + 
			"       CHECK_STATE,\n" + 
			"       CHECK_STATE_NAME,\n" + 
			"       CHECK_EDITION,\n" + 
			"       CHECK_OUT,\n" + 
			"       APPEAL_REPLY,\n" + 
			"       NOT_MORTAL_ERR,\n" + 
			"       MORTAL_ERR" + 
			"  FROM  CC_SHEET_CHECK  WHERE SERVICE_ORDER_ID= ?"; 
		List list = this.jt.query(strSql,new Object[]{ordId},new SheetCheckInfoRmp());
		return list;
	}
	/**
	 * 根据工单id查询质检列表
	 * @param sheetid
	 * @return
	 */
	public List queryCheckBeansBySheetid(String sheetid){
		String strSql =
			"SELECT CHECK_ID,\n" +
			"       TYPE_ID,\n" + 
			"       TYPE_NAME,\n" + 
			"       SERVICE_ORDER_ID,\n" + 
			"       WORK_SHEET_ID,\n" + 
			"       CHECK_ORG_ID,\n" + 
			"       CHECK_ORG_NAME,\n" + 
			"       CHECK_STAFF_ID,\n" + 
			"       CHECK_STAFF_NAME,\n" + 
			"       ORG_ID,\n" + 
			"       ORG_NAME,\n" + 
			"       STAFF_ID,\n" + 
			"       STAFF_NAME,\n" + 
			"       CONTENT_DESC,\n" + 
			"       EVALUSTE,\n" + 
			"       DATE_FORMAT(CREAT_DATE, '%Y-%m-%d %H:%i:%s') CREAT_DATE,\n" +
			"       APPEAL_ORG_ID,\n" + 
			"       APPEAL_ORG_NAME,\n" + 
			"       APPEAL_STAFF_ID,\n" + 
			"       APPEAL_STAFF_NAME,\n" + 
			"       DATE_FORMAT(APPEAL_DATA, '%Y-%m-%d %H:%i:%s') APPEAL_DATA,\n" +
			"       APPEAL_CONTENT,\n" + 
			"       CHECK_STATE,\n" + 
			"       CHECK_STATE_NAME,\n" + 
			"       CHECK_EDITION,\n" + 
			"       CHECK_OUT,\n" + 
			"       APPEAL_REPLY,\n" +
			"       NOT_MORTAL_ERR,\n" + 
			"       MORTAL_ERR " + 
			"  FROM  CC_SHEET_CHECK  WHERE WORK_SHEET_ID= ?";
		List list = this.jt.query(strSql,new Object[]{sheetid},new SheetCheckInfoRmp());
		return list;
	}
	
	/**
	 * 保存申诉信息
	 * @param appealObj
	 * @return
	 */
	public int saveAppealContent(SheetCheckAppeal appealObj){
		String sql =
			"UPDATE  CC_SHEET_CHECK \n" +
			"SET APPEAL_ORG_ID     = ?,\n" + 
			"APPEAL_ORG_NAME   = ?,\n" + 
			"APPEAL_STAFF_ID   = ?,\n" + 
			"APPEAL_STAFF_NAME = ?,\n" + 
			"APPEAL_DATA       = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'),\n" +
			"APPEAL_CONTENT    = ? \n" + 
			"WHERE CHECK_ID    = ? ";
			int count = this.jt.update(sql,
					appealObj.getAppealOrgId(),
					appealObj.getAppealOrgName(),
					appealObj.getAppealStaffId(),
					appealObj.getAppealStaffName(),
					appealObj.getAppealData(),
					appealObj.getAppealContent(), 
					appealObj.getCheckId()
			);
			return count;
	}
	
	/**
	 * 保存质检状态
	 * @param appealObj
	 * @return
	 */
	public int saveCheckSheetState(SheetCheckState sheetState){
		String sql =
			"UPDATE  CC_SHEET_CHECK\n" +
			"SET CHECK_STATE       = ?,\n" + 
			"CHECK_STATE_NAME  = ? \n" +  
			"WHERE CHECK_ID    = ? ";
			int count = this.jt.update(sql,
					sheetState.getCheckState(),
					sheetState.getCheckStateName(), 
					sheetState.getCheckId()
			);
			return count;
	}
	/**
	 * 将质检信息删除
	 * @param checkId
	 * @return
	 */
	public int deleteCheckSheet(String checkId){
		String sql = " DELETE T FROM CC_SHEET_CHECK T WHERE T.CHECK_ID = ? ";
			int count = this.jt.update(sql,
					checkId
			);
			return count; 
	}
	/**
	 * 将质检表信息移入历史表
	 * @param checkId
	 * @return
	 */
	public int moveCheckSheetToHis(String checkId){
		String sql = "INSERT INTO CC_SHEET_CHECK_HIS\n" +
				"(CHECK_ID, TYPE_ID, TYPE_NAME, SERVICE_ORDER_ID, WORK_SHEET_ID, CHECK_ORG_ID,\n" + 
				"CHECK_ORG_NAME, CHECK_STAFF_ID, CHECK_STAFF_NAME, ORG_ID, ORG_NAME, STAFF_ID,\n" + 
				"STAFF_NAME, CONTENT_DESC, EVALUSTE, CREAT_DATE, APPEAL_ORG_ID, APPEAL_ORG_NAME,\n" + 
				"APPEAL_STAFF_ID, APPEAL_STAFF_NAME, APPEAL_DATA, APPEAL_CONTENT, CHECK_STATE, CHECK_STATE_NAME,\n" + 
				"CHECK_EDITION, CHECK_OUT  ,MORTAL_ERR,NOT_MORTAL_ERR)\n" + 
				"SELECT CHECK_ID, TYPE_ID, TYPE_NAME, SERVICE_ORDER_ID, WORK_SHEET_ID, CHECK_ORG_ID,\n" + 
				"CHECK_ORG_NAME, CHECK_STAFF_ID, CHECK_STAFF_NAME, ORG_ID, ORG_NAME, STAFF_ID, STAFF_NAME,\n" + 
				"CONTENT_DESC, EVALUSTE, CREAT_DATE, APPEAL_ORG_ID, APPEAL_ORG_NAME, APPEAL_STAFF_ID,\n" + 
				"APPEAL_STAFF_NAME, APPEAL_DATA, APPEAL_CONTENT, CHECK_STATE, CHECK_STATE_NAME, CHECK_EDITION , " +
				" CHECK_OUT ,MORTAL_ERR,NOT_MORTAL_ERR\n" + 
				"FROM CC_SHEET_CHECK  WHERE CHECK_ID= ? ";

			int count = this.jt.update(sql,
					checkId
			);
			return count; 
	}
	/**
	 * 得到工单质检流水历史
	 * @param checkId
	 * @return
	 */
	public List getSheetCheckHisList(String checkId){
		String strSql =

			"SELECT CHECK_ID,\n" +
			"       TYPE_ID,\n" + 
			"       TYPE_NAME,\n" + 
			"       SERVICE_ORDER_ID,\n" + 
			"       WORK_SHEET_ID,\n" + 
			"       CHECK_ORG_ID,\n" + 
			"       CHECK_ORG_NAME,\n" + 
			"       CHECK_STAFF_ID,\n" + 
			"       CHECK_STAFF_NAME,\n" + 
			"       ORG_ID,\n" + 
			"       ORG_NAME,\n" + 
			"       STAFF_ID,\n" + 
			"       STAFF_NAME,\n" + 
			"       CONTENT_DESC,\n" + 
			"       EVALUSTE,\n" + 
			"       DATE_FORMAT(CREAT_DATE, '%Y-%m-%d %H:%i:%s') CREAT_DATE,\n" +
			"       APPEAL_ORG_ID,\n" + 
			"       APPEAL_ORG_NAME,\n" + 
			"       APPEAL_STAFF_ID,\n" + 
			"       APPEAL_STAFF_NAME,\n" + 
			"       DATE_FORMAT(APPEAL_DATA, '%Y-%m-%d %H:%i:%s') APPEAL_DATA,\n" +
			"       APPEAL_CONTENT,\n" + 
			"       CHECK_STATE,\n" + 
			"       CHECK_STATE_NAME,\n" + 
			"       CHECK_EDITION,\n" + 
			"       CHECK_OUT,\n" + 
			"       APPEAL_REPLY," + 
			"       NOT_MORTAL_ERR," + 
			"       MORTAL_ERR" + 
			"  FROM  CC_SHEET_CHECK_HIS WHERE CHECK_ID= ?";
		List list = this.jt.query(strSql,new Object[]{checkId},new SheetCheckInfoRmp());
		if(list.isEmpty()) {
			log.warn("没有查到质检流水为: {} 的记录", checkId);
			return Collections.emptyList();
		}
		return list;
	}
	
	
	/**
	 * 得到工单质检流水
	 * @param checkId
	 * @return
	 */
	public SheetCheckInfo getSheetCheck(String checkId) {
		String strSql =
			"SELECT CHECK_ID,\n" +
			"       TYPE_ID,\n" + 
			"       TYPE_NAME,\n" + 
			"       SERVICE_ORDER_ID,\n" + 
			"       WORK_SHEET_ID,\n" + 
			"       CHECK_ORG_ID,\n" + 
			"       CHECK_ORG_NAME,\n" + 
			"       CHECK_STAFF_ID,\n" + 
			"       CHECK_STAFF_NAME,\n" + 
			"       ORG_ID,\n" + 
			"       ORG_NAME,\n" + 
			"       STAFF_ID,\n" + 
			"       STAFF_NAME,\n" + 
			"       CONTENT_DESC,\n" + 
			"       EVALUSTE,\n" + 
			"       DATE_FORMAT(CREAT_DATE, '%Y-%m-%d %H:%i:%s') CREAT_DATE,\n" +
			"       APPEAL_ORG_ID,\n" + 
			"       APPEAL_ORG_NAME,\n" + 
			"       APPEAL_STAFF_ID,\n" + 
			"       APPEAL_STAFF_NAME,\n" + 
			"       DATE_FORMAT(APPEAL_DATA, '%Y-%m-%d %H:%i:%s') APPEAL_DATA,\n" +
			"       APPEAL_CONTENT,\n" + 
			"       CHECK_STATE,\n" + 
			"       CHECK_STATE_NAME,\n" + 
			"       CHECK_EDITION,\n" + 
			"       CHECK_OUT,\n" +
			"       APPEAL_REPLY," + 
			"       NOT_MORTAL_ERR," + 
			"       MORTAL_ERR" + 
			"  FROM  CC_SHEET_CHECK WHERE CHECK_ID= ?";
		List list = this.jt.query(strSql,new Object[]{checkId},new SheetCheckInfoRmp());
		if(list.isEmpty()) {
			log.warn("没有查到质检流水为: {} 的记录", checkId);
			return null;
		}
		SheetCheckInfo bean = (SheetCheckInfo)list.get(0);
		return bean;
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
