/**
 * <p>类名：SheetActionInfoDaoImpl.java</p>
 * <p>功能描叙：工单动作信息数据操作实现</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	Mar 24, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.complaintservice.service.IComplaint;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfoRmp;

/** 
 * @author lifeng
 *
 */
@SuppressWarnings("rawtypes")
public class SheetActionInfoDaoImpl implements ISheetActionInfoDao {
	private static final Logger log = LoggerFactory.getLogger(SheetActionInfoDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private PubFunc pubFunc;
	
	private ISheetPubInfoDao sheetPubInfoDao;
	private String delSheetActionInfoSql;
	private String querySheetActionBySheetIdSql;
	private String querySheetActionHisBySheetIdSql;
	private String saveSheetActionSql;
	private String savaSheetActionHisSql;
	private String saveSheetActionHisByOrderIdSql;
	private String delSheetActionByOrderIdSql;
	private String updateRegion;
	private String queryLastActionCodeBySheetIdSql;
	private String saveSheetHiddenActionSql;
	private String queryHiddenListByOpraStaffSql;
	private String updateSheetHiddenActionStateBySheetIdSql;
	private String saveSheetHiddenActionHisByOrderIdSql;
	private String delSheetHiddenActionHisByOrderIdSql;
    @Autowired
	private IComplaint complaintImpl;
	
	/**
	 * 通过orderid服务单号查询操作历史记录
	 */
	@SuppressWarnings("unchecked")
	public List getSheetFlowAction(String orderId){
		String sql ="SELECT T.ACTION_RECORD_GUID, T.WORK_SHEET_ID,T.TACHE_ID, T.TACHE_NAME, " +
				"T.ACTION_CODE, T.ACTION_NAME,T.OPRA_ORG_ID, T.OPRA_ORG_NAME," +
				" T.OPRA_STAFF,T.OPRA_STAFF_NAME," +
				"DATE_FORMAT(T.OPRA_START_DATE,'%Y-%m-%d %H:%i:%s') as OPRA_START_DATE," +
				"DATE_FORMAT(T.OPRA_END_DATE, '%Y-%m-%d %H:%i:%s') as OPRA_END_DATE, " +
				"T.OPRA_COMMENTS, T.REGION_ID,T.SERVICE_ORDER_ID, T.MONTH_FLAG " +
				"FROM CC_SHEET_FLOW_ACTION T " +
				"WHERE T.SERVICE_ORDER_ID ='"+orderId+"'";	
		return jdbcTemplate.query(sql,new SheetActionInfoRmp());
	}
	
	/**
	 * 删除一个定单下所有工单的工运作
	 * @param orderId	定单号
	 * @return	删除的记录数
	 */
	public int delSheetActionByOrderId(String orderId,Integer month) {
		/*
		 *DELETE FROM CC_SHEET_FLOW_ACTION A WHERE A.SERVICE_ORDER_ID = ?
		 */
		return jdbcTemplate.update(this.delSheetActionByOrderIdSql, orderId, month);
	}
	
	

	/* (non-Javadoc)
	 * @see com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao#delSheetActionInfo(java.lang.String)
	 */
	public int delSheetActionInfo(String sheetId) {
		// DELETE FROM CC_SHEET_FLOW_ACTION T WHERE T.WORK_SHEET_ID = ?
		int count = jdbcTemplate.update(this.delSheetActionInfoSql, sheetId);
		
		if(log.isDebugEnabled()){
			log.debug("sheetId = " + sheetId + "count = " + count);
			log.debug("delSheetActionInfo(String sheetId)  ----end");
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao#getSheetActionInfos(java.lang.String, boolean)
	 */
	@SuppressWarnings("unchecked")
	public SheetActionInfo[] getSheetActionInfos(String sheetId, boolean hisFlag) {
		// SELECT * FROM CC_SHEET_FLOW_ACTION T WHERE T.WORK_SHEET_ID = ?
		// SELECT * FROM CC_SHEET_FLOW_ACTION_HIS T WHERE T.WORK_SHEET_ID = ?
		String strsql;
		if(hisFlag){
			strsql = this.querySheetActionHisBySheetIdSql;
		}
		else{
			strsql = this.querySheetActionBySheetIdSql;
		}
		
		List tmpList = jdbcTemplate.query(strsql, new Object[]{sheetId},new SheetActionInfoRmp());
		int size = tmpList.size();
		
		if(size == 0){			
			return new SheetActionInfo[0];
		}
		
		SheetActionInfo[] sheetActionInfoList = new SheetActionInfo[size];
		
		for(int i=0; i<size; i++){
			sheetActionInfoList[i] = (SheetActionInfo)tmpList.get(i);
		}		
		tmpList.clear();
		tmpList = null;
		
		return sheetActionInfoList;
	}
	/**
	 * 获取工单id 在actionCode动作类型的 记录列表
	 * @param sheetId
	 * @param actionCode
	 * @param hisFlag false: 当前表 true :历史表
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SheetActionInfo[] getSheetActionInfos(String sheetId,int actionCode, boolean hisFlag) {
		// SELECT * FROM CC_SHEET_FLOW_ACTION T WHERE T.WORK_SHEET_ID = ?
		// SELECT * FROM CC_SHEET_FLOW_ACTION_HIS T WHERE T.WORK_SHEET_ID = ?
		String strsql;
		if(hisFlag){
			strsql =
				"SELECT T.ACTION_RECORD_GUID, T.WORK_SHEET_ID,\n" +
				"T.TACHE_ID, T.TACHE_NAME, T.ACTION_CODE, T.ACTION_NAME,\n" + 
				"T.OPRA_ORG_ID, T.OPRA_ORG_NAME, T.OPRA_STAFF,\n" + 
				"T.OPRA_STAFF_NAME, DATE_FORMAT(T.OPRA_START_DATE,\n" +
				" '%Y-%m-%d %H:%i:%s') as OPRA_START_DATE,\n" +
				"DATE_FORMAT(T.OPRA_END_DATE, '%Y-%m-%d %H:%i:%s') as\n" +
				"OPRA_END_DATE, T.OPRA_COMMENTS, T.REGION_ID,\n" + 
				"T.SERVICE_ORDER_ID, T.MONTH_FLAG FROM\n" + 
				"CC_SHEET_FLOW_ACTION_HIS T WHERE T.WORK_SHEET_ID = ? AND AND T.ACTION_CODE=? ";

		}
		else{
			strsql = 
				"SELECT T.ACTION_RECORD_GUID, T.WORK_SHEET_ID,\n" +
				"T.TACHE_ID, T.TACHE_NAME, T.ACTION_CODE, T.ACTION_NAME,\n" + 
				"T.OPRA_ORG_ID, T.OPRA_ORG_NAME, T.OPRA_STAFF,\n" + 
				"T.OPRA_STAFF_NAME, DATE_FORMAT(T.OPRA_START_DATE,\n" +
				"'%Y-%m-%d %H:%i:%s') as OPRA_START_DATE,\n" +
				"DATE_FORMAT(T.OPRA_END_DATE, '%Y-%m-%d %H:%i:%s') as\n" +
				"OPRA_END_DATE, T.OPRA_COMMENTS, T.REGION_ID,\n" + 
				"T.SERVICE_ORDER_ID, T.MONTH_FLAG FROM\n" + 
				"CC_SHEET_FLOW_ACTION T WHERE T.WORK_SHEET_ID = ? AND T.ACTION_CODE=? ";
		}

		List tmpList = jdbcTemplate.query(strsql, new Object[]{sheetId,actionCode},new SheetActionInfoRmp());
		int size = tmpList.size();
		
		if(size == 0){			
			return new SheetActionInfo[0];
		}
		
		SheetActionInfo[] sheetActionInfoList = new SheetActionInfo[size];
		
		for(int i=0; i<size; i++){
			sheetActionInfoList[i] = (SheetActionInfo)tmpList.get(i);
		}		
		tmpList.clear();
		tmpList = null;
		
		return sheetActionInfoList;
	}

	/**
	 * 将一个定单下所有的工单运作保存进历史
	 * 
	 * @param orderId
	 *            受理单号
	 * @return 保存成功的记录数
	 */
	public int saveSheetActionHisInfo(String orderId,Integer month) {
		/*
		 * INSERT INTO CC_SHEET_FLOW_ACTION_HIS A SELECT * FROM
		 * CC_SHEET_FLOW_ACTION B WHERE B.SERVICE_ORDER_ID = ?
		 */
		return jdbcTemplate.update(this.saveSheetActionHisByOrderIdSql, orderId, month);
	}

	/* (non-Javadoc)
	 * @see com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao#saveSheetActionInfo(com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo)
	 */
	public int saveSheetActionInfo(SheetActionInfo sheetActionInfo) {
		 //2020-4 记录首次提取后台派单的时间
		int tacheId = sheetActionInfo.getTacheId();
		int actionCode = sheetActionInfo.getActionCode();
		if (tacheId == 700000085 || tacheId == 720130021) {
			if (actionCode == 700000070 || actionCode == 700001807 || actionCode == 700001808 || actionCode == 700001811) {
				String serviceOrderId = sheetActionInfo.getServOrderId();
				if (pubFunc.isNewWorkFlow(serviceOrderId)) {
					if (sheetPubInfoDao.countWorkSheetAreaByOrderId(serviceOrderId, 3) == 0) {
						sheetPubInfoDao.insertWorkSheetArea(serviceOrderId, sheetActionInfo.getWorkSheetId(), sheetActionInfo.getOpraOrgId(), 3);
					}
				}
				complaintImpl.complaintPostInfo(1, serviceOrderId);
			}
		}
		/*
		 * INSERT INTO CC_SHEET_FLOW_ACTION
		 * VALUES(?,?,?,?,?,?,?,?,?,?,SYSDATE,?,?,?,?)
		 */
		return jdbcTemplate.update(this.saveSheetActionSql, 
				StringUtils.defaultIfEmpty(sheetActionInfo.getActionGuid(),null),
				StringUtils.defaultIfEmpty(sheetActionInfo.getWorkSheetId(),null),
				sheetActionInfo.getTacheId(),
				StringUtils.defaultIfEmpty(sheetActionInfo.getTacheName(),null),
				sheetActionInfo.getActionCode(),
				StringUtils.defaultIfEmpty(sheetActionInfo.getActionName(),null),
				StringUtils.defaultIfEmpty(sheetActionInfo.getOpraOrgId(),null),
				StringUtils.defaultIfEmpty(sheetActionInfo.getOpraOrgName(),null),
				sheetActionInfo.getOpraStaffId(),
				StringUtils.defaultIfEmpty(sheetActionInfo.getOpraStaffName(),null),
				StringUtils.defaultIfEmpty(sheetActionInfo.getOpraEndDate(),null),
				StringUtils.defaultIfEmpty(sheetActionInfo.getComments(),null),
				sheetActionInfo.getRegionId(),
				StringUtils.defaultIfEmpty(sheetActionInfo.getServOrderId(),null),
				sheetActionInfo.getMonth()
		);
	}

	@Override
	public int updateRegion(String serviceOrderId, int newRegion, int oldRegion) {
		return jdbcTemplate.update(updateRegion, newRegion, serviceOrderId, oldRegion);
	}

	public int queryLastActionCodeBySheetId(String workSheetId) {
		List list = jdbcTemplate.queryForList(queryLastActionCodeBySheetIdSql, workSheetId );
		if (list.isEmpty()) {
			return 0;
		}
		Map map = (Map) list.get(0);
		list.clear();
		list = null;
		return Integer.parseInt(map.get("ACTION_CODE").toString());
	}

	public int saveSheetHiddenAction(String workSheetId, String serviceOrderId, int opraStaff, String endDate) {
		return jdbcTemplate.update(this.saveSheetHiddenActionSql, workSheetId, serviceOrderId, opraStaff, endDate);
	}

	public int saveSheetHidden(String workSheetId, String serviceOrderId, int opraStaff, String endDate) {
		String deleteSql = "DELETE FROM cc_sheet_hidden_info WHERE WORK_SHEET_ID = ? AND SERVICE_ORDER_ID = ?";
		String sql = "insert into cc_sheet_hidden_info (WORK_SHEET_ID,SERVICE_ORDER_ID,OPRA_STAFF,OPRA_DATE,END_DATE,HIDDEN_STATE) " +
				"values " +
				"(?,?,?,now(),str_to_date(?, '%Y-%m-%d %H:%i:%s'),0)";
		jdbcTemplate.update(deleteSql, workSheetId, serviceOrderId);
		return jdbcTemplate.update(sql, workSheetId, serviceOrderId, opraStaff, endDate);
	}

	public int saveSheetHideenHis(String serviceOrderId) {
		log.info("saveSheetHideenHis serviceOrderId: {}",serviceOrderId);
		String saveSql =
				"INSERT INTO CC_SHEET_HIDDEN_INFO_HIS(WORK_SHEET_ID,SERVICE_ORDER_ID,OPRA_STAFF,MODIFY_DATE,OPRA_DATE,END_DATE,HIDDEN_STATE,"
						+ "HIS_DATE)SELECT WORK_SHEET_ID,SERVICE_ORDER_ID,OPRA_STAFF,MODIFY_DATE,OPRA_DATE,END_DATE,"
						+ "HIDDEN_STATE,now() FROM CC_SHEET_HIDDEN_INFO WHERE SERVICE_ORDER_ID = ?";
		String deleteSql = "DELETE FROM cc_sheet_hidden_info WHERE SERVICE_ORDER_ID = ?";
		jdbcTemplate.update(saveSql,serviceOrderId);
		int count = jdbcTemplate.update(deleteSql,serviceOrderId);
		return count;
	}

	public List queryHiddenListByOpraStaff(String where) {
		return this.jdbcTemplate.queryForList(queryHiddenListByOpraStaffSql + where);
	}

	public int updateSheetHiddenActionStateBySheetId(int hiddenState, String workSheetId) {
		return jdbcTemplate.update(this.updateSheetHiddenActionStateBySheetIdSql, hiddenState, workSheetId);
	}

	public int updateSheetHiddenStateBySheetId(int hiddenState, String workSheetId) {
		String sql = "UPDATE CC_SHEET_HIDDEN_INFO SET MODIFY_DATE = now(), HIDDEN_STATE = ? WHERE HIDDEN_STATE = 0 AND WORK_SHEET_ID = ?";
		return jdbcTemplate.update(sql, hiddenState, workSheetId);
	}

	public int saveSheetHiddenActionHisByOrderId(String serviceOrderId) {
		if (jdbcTemplate.update(this.saveSheetHiddenActionHisByOrderIdSql, serviceOrderId) > 0) {
			return jdbcTemplate.update(this.delSheetHiddenActionHisByOrderIdSql, serviceOrderId);
		}
		return 0;
	}

	public PubFunc getPubFunc() {
		return pubFunc;
	}

	public void setPubFunc(PubFunc pubFunc) {
		this.pubFunc = pubFunc;
	}

	public ISheetPubInfoDao getSheetPubInfoDao() {
		return sheetPubInfoDao;
	}

	public void setSheetPubInfoDao(ISheetPubInfoDao sheetPubInfoDao) {
		this.sheetPubInfoDao = sheetPubInfoDao;
	}

	/**
	 * @return the delSheetActionInfoSql
	 */
	public String getDelSheetActionInfoSql() {
		return delSheetActionInfoSql;
	}

	/**
	 * @param delSheetActionInfoSql the delSheetActionInfoSql to set
	 */
	public void setDelSheetActionInfoSql(String delSheetActionInfoSql) {
		this.delSheetActionInfoSql = delSheetActionInfoSql;
	}
	

	/**
	 * @return the saveSheetActionSql
	 */
	public String getSaveSheetActionSql() {
		return saveSheetActionSql;
	}

	/**
	 * @param saveSheetActionSql the saveSheetActionSql to set
	 */
	public void setSaveSheetActionSql(String saveSheetActionSql) {
		this.saveSheetActionSql = saveSheetActionSql;
	}

	/**
	 * @return the savaSheetActionHisSql
	 */
	public String getSavaSheetActionHisSql() {
		return savaSheetActionHisSql;
	}

	/**
	 * @param savaSheetActionHisSql the savaSheetActionHisSql to set
	 */
	public void setSavaSheetActionHisSql(String savaSheetActionHisSql) {
		this.savaSheetActionHisSql = savaSheetActionHisSql;
	}

	/**
	 * @return the querySheetActionBySheetIdSql
	 */
	public String getQuerySheetActionBySheetIdSql() {
		return querySheetActionBySheetIdSql;
	}

	/**
	 * @param querySheetActionBySheetIdSql the querySheetActionBySheetIdSql to set
	 */
	public void setQuerySheetActionBySheetIdSql(String querySheetActionBySheetIdSql) {
		this.querySheetActionBySheetIdSql = querySheetActionBySheetIdSql;
	}

	/**
	 * @return the querySheetActionHisBySheetIdSql
	 */
	public String getQuerySheetActionHisBySheetIdSql() {
		return querySheetActionHisBySheetIdSql;
	}

	/**
	 * @param querySheetActionHisBySheetIdSql the querySheetActionHisBySheetIdSql to set
	 */
	public void setQuerySheetActionHisBySheetIdSql(
			String querySheetActionHisBySheetIdSql) {
		this.querySheetActionHisBySheetIdSql = querySheetActionHisBySheetIdSql;
	}

	/**
	 * @return the saveSheetActionHisByOrderIdSql
	 */
	public String getSaveSheetActionHisByOrderIdSql() {
		return saveSheetActionHisByOrderIdSql;
	}

	/**
	 * @param saveSheetActionHisByOrderIdSql the saveSheetActionHisByOrderIdSql to set
	 */
	public void setSaveSheetActionHisByOrderIdSql(
			String saveSheetActionHisByOrderIdSql) {
		this.saveSheetActionHisByOrderIdSql = saveSheetActionHisByOrderIdSql;
	}

	/**
	 * @return the delSheetActionByOrderIdSql
	 */
	public String getDelSheetActionByOrderIdSql() {
		return delSheetActionByOrderIdSql;
	}

	/**
	 * @param delSheetActionByOrderIdSql the delSheetActionByOrderIdSql to set
	 */
	public void setDelSheetActionByOrderIdSql(String delSheetActionByOrderIdSql) {
		this.delSheetActionByOrderIdSql = delSheetActionByOrderIdSql;
	}

	public void setUpdateRegion(String updateRegion) {
		this.updateRegion = updateRegion;
	}

	public String getQueryLastActionCodeBySheetIdSql() {
		return queryLastActionCodeBySheetIdSql;
	}

	public void setQueryLastActionCodeBySheetIdSql(String queryLastActionCodeBySheetIdSql) {
		this.queryLastActionCodeBySheetIdSql = queryLastActionCodeBySheetIdSql;
	}

	public String getSaveSheetHiddenActionSql() {
		return saveSheetHiddenActionSql;
	}

	public void setSaveSheetHiddenActionSql(String saveSheetHiddenActionSql) {
		this.saveSheetHiddenActionSql = saveSheetHiddenActionSql;
	}

	public String getQueryHiddenListByOpraStaffSql() {
		return queryHiddenListByOpraStaffSql;
	}

	public void setQueryHiddenListByOpraStaffSql(String queryHiddenListByOpraStaffSql) {
		this.queryHiddenListByOpraStaffSql = queryHiddenListByOpraStaffSql;
	}

	public String getUpdateSheetHiddenActionStateBySheetIdSql() {
		return updateSheetHiddenActionStateBySheetIdSql;
	}

	public void setUpdateSheetHiddenActionStateBySheetIdSql(String updateSheetHiddenActionStateBySheetIdSql) {
		this.updateSheetHiddenActionStateBySheetIdSql = updateSheetHiddenActionStateBySheetIdSql;
	}

	public String getSaveSheetHiddenActionHisByOrderIdSql() {
		return saveSheetHiddenActionHisByOrderIdSql;
	}

	public void setSaveSheetHiddenActionHisByOrderIdSql(String saveSheetHiddenActionHisByOrderIdSql) {
		this.saveSheetHiddenActionHisByOrderIdSql = saveSheetHiddenActionHisByOrderIdSql;
	}

	public String getDelSheetHiddenActionHisByOrderIdSql() {
		return delSheetHiddenActionHisByOrderIdSql;
	}

	public void setDelSheetHiddenActionHisByOrderIdSql(String delSheetHiddenActionHisByOrderIdSql) {
		this.delSheetHiddenActionHisByOrderIdSql = delSheetHiddenActionHisByOrderIdSql;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	
}
