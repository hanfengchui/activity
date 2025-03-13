/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IStaffPermit;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;


@Component(value="dbgridDataPub")
public class DbgridDataPubImpl implements IdbgridDataPub {
	protected Logger log = LoggerFactory.getLogger(DbgridDataPubImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ISystemAuthorization systemAuthorization;
	
	@Autowired
	private PubFunc pubFunc;

	
	@SuppressWarnings("rawtypes")
	public GridDataInfo getResultBySize(String strSql,int begion,int pageSize,String orderBy,String funId) {
		String logonName = "系统自动执行";
		if(pubFunc.isLogonFlag()) {
			logonName = pubFunc.getLogonStaff().getLogonName();
		}
	    String countSql = this.generateCountSql(strSql);
	    log.info("logonName: {} funId: {} count:\n{}", logonName, funId, countSql);
		int count = this.jdbcTemplate.queryForObject(countSql,new java.lang.Object[]{},Integer.class);

		strSql += orderBy;
		if(count > 10) {
			if(begion == 0) {
				pageSize = pageSize == 0 ? 100: pageSize;
				strSql = this.generateBetweenSql(strSql, 0, pageSize);
			}else {
				strSql = this.generateBetweenSql(strSql, (begion-1)*pageSize, pageSize);
			}
		}
		log.info("logonName: {} funId: {} getResultBySize:\n{}", logonName, funId, strSql);
		List tmp = this.jdbcTemplate.queryForList(strSql);
		GridDataInfo bean = new GridDataInfo();
		bean.setQuryCount(count);
		bean.setList(tmp);
		return bean;
	}
	
	@SuppressWarnings("rawtypes")
	public GridDataInfo getResultNewBySize(String countSql,String strSql,int begion,int pageSize,String orderBy,String funId) {
		String logonName = pubFunc.getLogonStaff().getLogonName();
		log.info("logonName: {} funId: {} count:\n{}", logonName, funId, countSql);
		int count = this.jdbcTemplate.queryForObject(countSql,new java.lang.Object[]{},Integer.class);

		strSql += orderBy;
		if(count > 10) {
			if(begion == 0) {
				pageSize = pageSize == 0 ? 100 : pageSize;
				strSql = this.generateBetweenSql(strSql, 0, pageSize);
			}else {
				strSql = this.generateBetweenSql(strSql, (begion-1)*pageSize, pageSize);
			}
		}
		log.info("logonName: {} funId: {} getResultNewBySize:\n{}", logonName, funId, strSql);
		List tmp = this.jdbcTemplate.queryForList(strSql);
		GridDataInfo bean = new GridDataInfo();
		bean.setQuryCount(count);
		bean.setList(tmp);
		return bean;
	}
	
	/**
	 * 得到查询结果
	 * @param strSql 查询SQL
	 * @param begion 开始下标
	 * @param orderBy SQL中 ORDER BY
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public GridDataInfo getResult(String strSql,int begion,String orderBy,String funId) {
		String logonName = pubFunc.getLogonStaff().getLogonName();
	    String countSql = this.generateCountSql(strSql);
	    log.info("logonName: {} funId: {} count:\n{}", logonName, funId, countSql);
		int count = this.jdbcTemplate.queryForObject(countSql,new java.lang.Object[]{},Integer.class);

		strSql += orderBy;
		if(count > 10) {
			if(begion == 0){
				strSql = this.generateBetweenSql(strSql, 0, 10);
			}else{
				strSql = this.generateBetweenSql(strSql, (begion-1)*10, 10);
			}
		}
		log.info("logonName: {} funId: {} getResult:\n{}", logonName, funId, strSql);
		List tmp = this.jdbcTemplate.queryForList(strSql);
		GridDataInfo bean = new GridDataInfo();
		bean.setQuryCount(count);
		bean.setList(tmp);
		return bean;
	}
	
	/**
	 * 超过配置数
	 * @param sql
	 * @param begin
	 * @param end
	 * @return
	 */
	public String generateBetweenSql(String sql, int begin, int limit) {
		return sql + " LIMIT " + begin + ", " + limit;
	}
	
	/**
	 * 查询SQL语句数据数
	 * @param sql
	 * @return
	 */
	public String generateCountSql(String sql) {
		return "SELECT COUNT(1) FROM (" + sql + ") AS COUNT";
	}
	
	/**
	 * 得到当前员工对象
	 * @return
	 */
	public IStaffPermit getStaffPermit() {
		String loginName = (String) systemAuthorization.getHttpSession().getAttribute("ACEGI__USERNAME");
		if(null == loginName) {
			TsmStaff st = pubFunc.getLogonStaff();
			if(null != st) {
				loginName = st.getLogonName();
			}
		}
		return systemAuthorization.loadAuthorization(loginName, systemAuthorization.getHttpSession());	
	}
	
	public GridDataInfo getResultByW(String innerSQL, String outerSurfix, String outerWhere, int begion, int pageSize, String funId){
        String sql = null;
        boolean flag = null == outerWhere || "".equals(outerWhere);
        if(flag){
            sql = innerSQL;
        }else{
            sql = "SELECT * FROM ("+outerSurfix + " FROM (" + innerSQL + ")X)Y WHERE 1 = 1 " + outerWhere;
        }
        if(flag){
            sql = "SELECT * FROM (" + outerSurfix + " FROM (" + innerSQL +")X)Y WHERE 1 = 1";
        }else{
            sql = "SELECT * FROM (SELECT AL.* FROM (" + sql + ") AL )Y WHERE 1 = 1";
        }
        return this.getResultBySize(sql, begion, pageSize, "", funId);
    }

	@Override
	@SuppressWarnings("rawtypes")
	public GridDataInfo getAllResult(String strSql, String orderBy, String funId) {
	    String countSql = this.generateCountSql(strSql);
		int count = this.jdbcTemplate.queryForObject(countSql,new java.lang.Object[]{},Integer.class);
		strSql += orderBy;
		if(log.isDebugEnabled()) {
			log.debug("查询SQL语句为:{}", strSql);
		}
		List tmp = this.jdbcTemplate.queryForList(strSql);	
		GridDataInfo bean = new GridDataInfo();
		bean.setQuryCount(count);
		bean.setList(tmp);		
		return bean;
	}
	
}
