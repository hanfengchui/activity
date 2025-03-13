/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.especiallyCust.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.especiallyCust.IespeciallyCustDao;
import com.timesontransfar.customservice.especiallyCust.TsEspeciallyCustInfo;

/**
 * @author 万荣伟
 *
 */
@Component("especiallyCustDao")
public class EspeciallyCustDaoImpl implements IespeciallyCustDao {
	
	private static final Logger logger = LoggerFactory.getLogger(EspeciallyCustDaoImpl.class);
	private String saveCustInfoSql = "INSERT INTO "
			+ "CC_ESPECIALLY_CUST(REGION_ID,REGION_NAME,CUST_NUM,CUST_NAME,TS_ESPECIALLY,REMARK,MEET_PROCEEDING,STATU,MODIFI_DATA,REGION_TELNO)"
			+ "VALUES(?,?,?,?,?,?,?,?,NOW(),?)";
	private String updateCustInfoSql = "UPDATE CC_ESPECIALLY_CUST E SET "
			+ "E.CUST_NAME=?,E.TS_ESPECIALLY=?,E.REMARK=?,E.MEET_PROCEEDING=?,E.STATU=?,"
			+ "E.MODIFI_STAFF=?,E.MODIFI_DATA=STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s') WHERE E.REGION_ID=? AND E.CUST_NUM=?";
	
	@Autowired
	private JdbcTemplate jt;
			
	/**
	 * 投诉特殊客户对象
	 * @param bean
	 * @return
	 */
	public int saveCustInfo(TsEspeciallyCustInfo bean) {
		int size = this.jt.update(this.saveCustInfoSql, 
										bean.getRegionId(),
										StringUtils.defaultIfEmpty(bean.getRegionName(),null),
										StringUtils.defaultIfEmpty(bean.getCustNum(),null),
										StringUtils.defaultIfEmpty(bean.getCustName(),null),
										StringUtils.defaultIfEmpty(bean.getTsEspecially(), null),
										StringUtils.defaultIfEmpty(bean.getRemark(), null),
										StringUtils.defaultIfEmpty(bean.getMeetProceeding(), null),
										bean.getStatu(),
										StringUtils.defaultIfEmpty(bean.getRegionTel(),null)
									);
		if(logger.isDebugEnabled()) {
			logger.debug("保存投诉特殊客户条数：{}", size);
		}
		return size;
	}

	/**
	 * 更新投诉特殊客户信息，包括删除该记录，删除记录把该记录的状态置为0;
	 * @param bean
	 * @return
	 */
	public int updataCustInfo(TsEspeciallyCustInfo bean) {
		int size = this.jt.update(this.updateCustInfoSql,
										StringUtils.defaultIfEmpty(bean.getCustName(),null),
										StringUtils.defaultIfEmpty(bean.getTsEspecially(), null),
										StringUtils.defaultIfEmpty(bean.getRemark(), null),
										StringUtils.defaultIfEmpty(bean.getMeetProceeding(), null),
										bean.getStatu(),
										bean.getStaffId(),
										StringUtils.defaultIfEmpty(bean.getModifiData(),null),
										bean.getRegionId(),
										StringUtils.defaultIfEmpty(bean.getCustNum(),null)
									);
		if(logger.isDebugEnabled()) {
			logger.debug("更新客户信息条数：{}", size);
		}
		return size;
	}

	private String selectTsEspeciallyByCustNumSql = "SELECT TS_ESPECIALLY FROM cc_especially_cust WHERE statu = 1 AND region_id = ? AND cust_num = ?";

	@Override
	@SuppressWarnings("rawtypes")
	public String queryTsEspeciallyByCustNum(String regionId, String custNum) {
		String tsEspecially = "";
		List list = this.jt.queryForList(selectTsEspeciallyByCustNumSql, regionId, custNum);
		if (!list.isEmpty()) {
			Map map = (Map) list.get(0);
			tsEspecially = map.get("TS_ESPECIALLY") == null ? "" : map.get("TS_ESPECIALLY").toString();
		}
		return tsEspecially;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public List qryTsEspecial(int regionId, String custName) {
		String sql = "select REGION_NAME,CUST_NUM from CC_ESPECIALLY_CUST a where a.region_id=? and a.cust_num=?";
		return jt.queryForList(sql, regionId, custName);
	}

}
