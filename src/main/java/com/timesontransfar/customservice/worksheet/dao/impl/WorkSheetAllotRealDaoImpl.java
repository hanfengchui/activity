/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.worksheet.dao.IworkSheetAllotRealDao;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotRealRmp;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public class WorkSheetAllotRealDaoImpl implements IworkSheetAllotRealDao {
	
	/**
	 * Logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(WorkSheetAllotRealDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jt;
	
	private String saveSql;
	private String saveHisSql;
	private String updateSql;
	private String qurySql;
	private String quryHisSql;

	/**
	 * 将工单保存到派单关系表中
	 * @param workSheetAllotReal 派单关系对象
	 * @param boo true保存到当前表中 false 保存到历史表中
	 * @return 返回保存记录条数
	 */
	public int saveWorkSheetAllotReal(WorkSheetAllotReal workSheetAllotReal,boolean boo) {
		String strSql = "";
		int size=0;
		if(boo) {
			strSql = this.saveSql;
			size = jt.update(strSql,
					StringUtils.defaultIfEmpty(workSheetAllotReal.getWorkSheetId(),null),
					StringUtils.defaultIfEmpty(workSheetAllotReal.getCheckWorkSheet(),null),
					StringUtils.defaultIfEmpty(workSheetAllotReal.getPreDealSheet(),null),
					workSheetAllotReal.getCheckFalg(),
					workSheetAllotReal.getMainSheetFlag(),
					StringUtils.defaultIfEmpty(workSheetAllotReal.getDealStauts(),null),
					workSheetAllotReal.getMonth(),
					workSheetAllotReal.getOrderId()
					
			);
		} else {
			strSql = this.saveHisSql;
			size = jt.update(strSql, workSheetAllotReal.getOrderId(), workSheetAllotReal.getMonth());
		}

		if(log.isDebugEnabled()) {
			log.debug("向派单关系表中保存了"+size+"条关系数据");
		}
		return size;
		
	}
	
	/**
	 * 更新派单关系当前表中审批标示和处理状态
	 * @param workSheetAllotReal 派单关系对象
	 * @return 返回保存记录条数
	 */
	public int updateWorkSheetAllotReal(WorkSheetAllotReal workSheetAllotReal) {
		String strSql = this.updateSql;
		int size = jt.update(strSql,
				workSheetAllotReal.getCheckFalg(),
				StringUtils.defaultIfEmpty(workSheetAllotReal.getDealStauts(),null),
				workSheetAllotReal.getMonth(),
				StringUtils.defaultIfEmpty(workSheetAllotReal.getWorkSheetId(),null)
				
		);
		if(log.isDebugEnabled()) {
			log.debug("更新派单关系表中处理工单号为"+workSheetAllotReal.getWorkSheetId()+"的工单"+size+"条");
		}
		return size;
		
	}
	/**
	 * 更新审核或审批工单可以处理
	 * @param checkSheet 审核或审批 单
	 * @param month 月分区
	 * @return 更新数
	 */
	public int updateCheckSheet(String checkSheet,String dealDesc,Integer month) {
		String strSql = "UPDATE cc_worksheet_allot_rela W SET W.CHECK_FLAG=1,W.DEAL_STATUS_DESC=? WHERE W.CHECK_WORKSHEET_ID=? AND W.MONTH_FLAG=?";
		return this.jt.update(strSql, dealDesc, checkSheet, month);
	}
	/**
	 * 查询派单关系表对象
	 * @param strWhere 传入SQL的WHERE条件
	 * @param boo true 查询当前派单关系表 false查询历史派单关系表
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public WorkSheetAllotReal[] getWorkSheetAllotReal(String strWhere, boolean boo) {
		String strSql = "";
		if(boo) {
			strSql = this.qurySql;
		} else {
			strSql = this.quryHisSql;
		}
		strSql = strSql + strWhere;
		List tmp = jt.query(strSql, new WorkSheetAllotRealRmp());
		int size = tmp.size();
		if(size == 0) {
			tmp.clear();
			tmp = null;
			return new WorkSheetAllotReal[0];
		}
		WorkSheetAllotReal[] workSheetAllotReal = new WorkSheetAllotReal[size];
		for(int i = 0;i < size; i++) {
			workSheetAllotReal[i] = (WorkSheetAllotReal) tmp.get(i);
		}
		tmp.clear();
		tmp = null;
		return workSheetAllotReal;
	}
	
	/**
	 * 从当前表中删除定单的关系表
	 * @param orderId
	 * @param month
	 * @return
	 */
	public int deleteSheetAlloReal(String orderId,Integer month) {
		String strSql = "DELETE FROM CC_WORKSHEET_ALLOT_RELA WHERE SERVICE_ORDER_ID=? AND MONTH_FLAG=?";
		int size = this.jt.update(strSql, orderId, month);
		if(size > 0) {
			log.info("删除定单"+orderId+"相关联的工单关系表成功!删除数目为:"+size+"条");
		}
		return size;
	}
	/**
	 * 根据处理工单号,得到派单关系对象
	 * @param sheetId
	 * @param month
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public WorkSheetAllotReal getSheetAllotObj(String sheetId, Integer month) {
		String strSql = "SELECT * FROM CC_WORKSHEET_ALLOT_RELA A WHERE A.DEAL_WORKSHEET_ID=? AND A.MONTH_FLAG=?";
		int size = 0;
		List tmp = this.jt.query(strSql, new Object[]{sheetId,month}, new WorkSheetAllotRealRmp());
		size = tmp.size();
		if(size == 0) {
			if(log.isDebugEnabled()) {
				log.debug("根据处理工单号"+sheetId+"没有在派单关系表找到数据");
			}
			tmp.clear();
			tmp = null;
			return null;
		}
		WorkSheetAllotReal workSheetAllotReal = (WorkSheetAllotReal) tmp.get(0);
		tmp.clear();
		tmp = null;
		return workSheetAllotReal;		
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
	 * @return qurySql
	 */
	public String getQurySql() {
		return qurySql;
	}

	/**
	 * @param qurySql 要设置的 qurySql
	 */
	public void setQurySql(String qurySql) {
		this.qurySql = qurySql;
	}

	/**
	 * @return saveHisSql
	 */
	public String getSaveHisSql() {
		return saveHisSql;
	}

	/**
	 * @param saveHisSql 要设置的 saveHisSql
	 */
	public void setSaveHisSql(String saveHisSql) {
		this.saveHisSql = saveHisSql;
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

	/**
	 * @return quryHisSql
	 */
	public String getQuryHisSql() {
		return quryHisSql;
	}

	/**
	 * @param quryHisSql 要设置的 quryHisSql
	 */
	public void setQuryHisSql(String quryHisSql) {
		this.quryHisSql = quryHisSql;
	}
	
}
