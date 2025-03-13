package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timesontransfar.customservice.worksheet.dao.ItsCustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisitRmp;

@SuppressWarnings("rawtypes")
public class TscustomerVisitImpl implements ItsCustomerVisit{
	private static final Logger log = LoggerFactory.getLogger(TscustomerVisitImpl.class);
	
	@Autowired
	private JdbcTemplate jt;

	private String saveCustomerVisitSql;
	
	private String saveCustomerVisitTmpSql;

	private String deleteCustomerVisitSql;
	
	private String deleteCustomerVisitTmpSql;
	
	private String saveCustomerVisitHisSql;	
	
	/**
	 * 删除投诉回访记录表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteCustomerVisit(String serviceOrderId, String workSheetId, int region) {
		return jt.update(this.deleteCustomerVisitSql, serviceOrderId, region);
	}

	/**
	 * 删除投诉回访临时记录表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteCustomerVisitTmp(String serviceOrderId, int region) {
		return jt.update(this.deleteCustomerVisitTmpSql, serviceOrderId, region);
	}
	/**
	 * 投诉回访表添加记录
	 * @param bean
	 * @return
	 */
	public int saveCustomerVisit(TScustomerVisit bean) {		
		String strsql = this.saveCustomerVisitSql;
//		INSERT INTO CC_CUSTOMER_VISIT( SERVICE_ORDER_ID,
//		WORK_SHEET_ID, REGION_ID, REGION_NAME,
//		COLLECTIVITY_CIRCS, COLLECTIVITY_CIRCS_NAME,
//		TS_DEAL_ATTITUDE, TS_DEAL_ATTITUDE_NAME,
//		TS_DEAL_BETIMES, TS_DEAL_BETIMES_NAME, TS_DEAL_RESULT,
//		TS_DEAL_RESULT_NAME, TS_VISIT_RESULT, REPLY_DATA,
//		CREAT_DATA, MONTH_FLAG ) VALUES(
//		?,?,?,?,?,?,?,?,?,?,?,?,?, TO_DATE(?, 'yyyy-mm-dd
//		hh24:mi:ss'), TO_DATE(?, 'yyyy-mm-dd hh24:mi:ss'),? )		
		int count = this.jt.update(strsql,
				StringUtils.defaultIfEmpty(bean.getServiceOrderId(),null),
				StringUtils.defaultIfEmpty(bean.getWorkSheetId(),null),
				bean.getRegionId(),
				StringUtils.defaultIfEmpty(bean.getRegionName(),null),
				bean.getCollectivityCircs(),
				StringUtils.defaultIfEmpty(bean.getCollectivityCircsName(),null),
				bean.getTsDealAttitude(),
				StringUtils.defaultIfEmpty(bean.getTsDealAttitudeName(),null),
				bean.getTsDealBetimes(),
				StringUtils.defaultIfEmpty(bean.getTsDealBetimesName(),null),
				bean.getTsDealResult(),
				StringUtils.defaultIfEmpty(bean.getTsDealResultName(),null),
				StringUtils.defaultIfEmpty(bean.getTsVisitResult(),null),
				bean.getMonth(),
				bean.getVisitType()
		);
		if (log.isDebugEnabled()) {
			log.debug("保存投诉回访特性数据:" + count+"条");
			log.debug("saveCustomerVisit(TScustomerVisit bean)   --end");
		}
		return count;
	}
	/**
	 * 投诉回访临时表添加记录
	 * @param bean
	 * @return
	 */
	public int saveCustomerVisitTmp(TScustomerVisit bean) {
		String strsql = this.saveCustomerVisitTmpSql;
		int count = this.jt.update(strsql,
				StringUtils.defaultIfEmpty(bean.getServiceOrderId(),null),
				StringUtils.defaultIfEmpty(bean.getWorkSheetId(),null),
				bean.getRegionId(),
				StringUtils.defaultIfEmpty(bean.getRegionName(),null),
				bean.getCollectivityCircs(),
				StringUtils.defaultIfEmpty(bean.getCollectivityCircsName(),null),
				bean.getTsDealAttitude(),
				StringUtils.defaultIfEmpty(bean.getTsDealAttitudeName(),null),
				bean.getTsDealBetimes(),
				StringUtils.defaultIfEmpty(bean.getTsDealBetimesName(),null),
				bean.getTsDealResult(),
				StringUtils.defaultIfEmpty(bean.getTsDealResultName(),null),
				StringUtils.defaultIfEmpty(bean.getTsVisitResult(),null),
				bean.getMonth(),
				bean.getVisitType()
		);
		if (log.isDebugEnabled()) {
			log.debug("保存投诉回访特性临时数据:" + count+"条");
			log.debug("saveCustomerVisitTmp(TScustomerVisit bean)   --end");
		}
		return count;
	}
	
	/**
	 * 投诉回访历史表添加记录
	 * @param servid 定单号
	 * @param sheetid 工单号
	 * @param month 月分区
	 * @return
	 */
	public int saveCustomerVisitHis(String servid, String sheetid, int region) {
		return jt.update(this.saveCustomerVisitHisSql, servid, region);
	}
	
	/**
	 * 得到投诉工单的回访对象
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param boo true 查询当前  false 查询历史
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TScustomerVisit getCustomerVisitObj(String sheetId,int regionId,boolean boo) {
		String strSql="";
		if(boo) {
			strSql = "SELECT * FROM CC_CUSTOMER_VISIT A WHERE A.WORK_SHEET_ID=? AND A.REGION_ID=? ORDER BY A.CREAT_DATA DESC";
		} else {
			strSql = "SELECT * FROM CC_CUSTOMER_VISIT_HIS A WHERE A.WORK_SHEET_ID=? AND A.REGION_ID=? ORDER BY A.CREAT_DATA DESC";
		}
		List tmp = this.jt.query(strSql, new Object[]{sheetId, regionId},new TScustomerVisitRmp());
		int size = tmp.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("没有查询到工单号为:||"+sheetId+"的回访记录");
			}
			return null;
		}
		TScustomerVisit bean = (TScustomerVisit)tmp.get(0);
		tmp.clear();
		tmp = null;
		return bean;
	}

	@SuppressWarnings("unchecked")
	public TScustomerVisit getCustomerVisitByOrderId(String orderId, boolean boo) {
		String sql = "";
		if (boo) {
			sql = "SELECT * FROM cc_customer_visit WHERE service_order_id=? ORDER BY creat_data DESC LIMIT 1";
		} else {
			sql = "SELECT * FROM cc_customer_visit_his WHERE service_order_id=? ORDER BY creat_data DESC LIMIT 1";
		}
		List list = this.jt.query(sql, new Object[] { orderId }, new TScustomerVisitRmp());
		if (list.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("没有查询到服务单号为: {}的回访记录", orderId);
			}
			return null;
		}
		TScustomerVisit bean = (TScustomerVisit) list.get(0);
		list.clear();
		list = null;
		return bean;
	}

	/**
	 * 得到投诉工单的临时回访对象
	 * @param sheetId
	 * @param regionId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TScustomerVisit getCustomerVisitTmpObj(String sheetId,int regionId) {
		String strSql = "SELECT * FROM CC_CUSTOMER_VISIT_TMP A WHERE A.WORK_SHEET_ID=? AND A.REGION_ID=? ORDER BY A.CREAT_DATA DESC";
		List tmp = this.jt.query(strSql, new Object[]{sheetId, regionId},new TScustomerVisitRmp());
		int size = tmp.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("没有查询到工单号为:||"+sheetId+"的回访记录");
			}
			return null;
		}
		TScustomerVisit bean = (TScustomerVisit)tmp.get(0);
		tmp.clear();
		tmp = null;
		return bean;
	}
	
	/**
	 * @return deleteCustomerVisitSql
	 */
	public String getDeleteCustomerVisitSql() {
		return deleteCustomerVisitSql;
	}
	/**
	 * @param deleteCustomerVisitSql 要设置的 deleteCustomerVisitSql
	 */
	public void setDeleteCustomerVisitSql(String deleteCustomerVisitSql) {
		this.deleteCustomerVisitSql = deleteCustomerVisitSql;
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
	 * @return saveCustomerVisitHisSql
	 */
	public String getSaveCustomerVisitHisSql() {
		return saveCustomerVisitHisSql;
	}
	/**
	 * @param saveCustomerVisitHisSql 要设置的 saveCustomerVisitHisSql
	 */
	public void setSaveCustomerVisitHisSql(String saveCustomerVisitHisSql) {
		this.saveCustomerVisitHisSql = saveCustomerVisitHisSql;
	}
	/**
	 * @return saveCustomerVisitSql
	 */
	public String getSaveCustomerVisitSql() {
		return saveCustomerVisitSql;
	}
	/**
	 * @param saveCustomerVisitSql 要设置的 saveCustomerVisitSql
	 */
	public void setSaveCustomerVisitSql(String saveCustomerVisitSql) {
		this.saveCustomerVisitSql = saveCustomerVisitSql;
	}
	public String getSaveCustomerVisitTmpSql() {
		return saveCustomerVisitTmpSql;
	}
	public void setSaveCustomerVisitTmpSql(String saveCustomerVisitTmpSql) {
		this.saveCustomerVisitTmpSql = saveCustomerVisitTmpSql;
	}
	public String getDeleteCustomerVisitTmpSql() {
		return deleteCustomerVisitTmpSql;
	}
	public void setDeleteCustomerVisitTmpSql(String deleteCustomerVisitTmpSql) {
		this.deleteCustomerVisitTmpSql = deleteCustomerVisitTmpSql;
	}
}