/**
 * <p>类名：OrderCustInfoDaoImpl.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	Mar 18, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.orderask.dao.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderCustInfoRmp;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;

@SuppressWarnings("rawtypes")
public class OrderCustInfoDaoImpl implements IorderCustInfoDao {

	private static final Logger log = LoggerFactory.getLogger(OrderCustInfoDaoImpl.class);
	
	private JdbcTemplate jt;

	private String saveOrderCustSql;
	private String saveOrderCustHisByCustGudiSql;
	private String queryCustByGuidSql;
	private String queryCustHisByGuidSql;
	private String queryCustByOrderId;
	private String delCustByCustGuid;
	private String updateCustInfoSql;
	
	
	public int saveOrderCust(OrderCustomerInfo orderCust) {
		String strsql = this.saveOrderCustSql;
		return jt.update(strsql,
				StringUtils.defaultIfEmpty(orderCust.getCustGuid(),null),
				orderCust.getRegionId(),
				StringUtils.defaultIfEmpty(orderCust.getCustName(),null),
				orderCust.getCrmCustId(),
				orderCust.getCustSex(),
				StringUtils.defaultIfEmpty(orderCust.getFaxNum(),null),
				StringUtils.defaultIfEmpty(orderCust.getCustMail(),null),
				StringUtils.defaultIfEmpty(orderCust.getMailAddr(),null),
				StringUtils.defaultIfEmpty(orderCust.getPostCode(),null),
				orderCust.getIdType(),				
				StringUtils.defaultIfEmpty(orderCust.getIdCard(),null),
				orderCust.getCustType(),
				StringUtils.defaultIfEmpty(orderCust.getCustTypeName(),null),
				orderCust.getCustServGrade(),
				StringUtils.defaultIfEmpty(orderCust.getCustServGradeName(),null),
				orderCust.getCustBrand(),
				StringUtils.defaultIfEmpty(orderCust.getCustBrandDesc(),null),
				orderCust.getCustBrandContent(),
				StringUtils.defaultIfEmpty(orderCust.getCustBrandContenDesc(),null),
				orderCust.getProdTatus(),
				StringUtils.defaultIfEmpty(orderCust.getProdTatusDesc(),null),
				orderCust.getTradeType(),
				StringUtils.defaultIfEmpty(orderCust.getTradeTypeDesc(),null),
				orderCust.getProdType(),
				StringUtils.defaultIfEmpty(orderCust.getProdTypeDesc(),null),
				StringUtils.defaultIfEmpty(orderCust.getBranchNo(),null),
				StringUtils.defaultIfEmpty(orderCust.getInstalldate(),null),
				StringUtils.defaultIfEmpty(orderCust.getInstallAdd(),null),
				StringUtils.defaultIfEmpty(orderCust.getAddonesInfo(),null),
				orderCust.getMonth(),
				orderCust.getServiceGrande(),				
				StringUtils.defaultIfEmpty(orderCust.getServiceGrandeDesc(),null),
				StringUtils.defaultIfEmpty(orderCust.getOdsCity(),null),
				orderCust.getHighWarnId(),
				StringUtils.defaultIfEmpty(orderCust.getHighWarnDesc(),null),
				orderCust.getCustAge(),
				StringUtils.defaultIfEmpty(orderCust.getProdInstId(),null),
				StringUtils.defaultIfEmpty(orderCust.getAddressId(),null),
				StringUtils.defaultIfEmpty(orderCust.getAddressDesc(),null),
				orderCust.getNumRank(),
				StringUtils.defaultIfEmpty(orderCust.getIsRealname(),null),
				StringUtils.defaultIfEmpty(orderCust.getIdTypeName(),null)
		);
	}
	
	/**
	 * 将当前表中的客户信息存入到历史表
	 * @param currentCustGuid	当前表客户guid
	 * @return	保存成功记录数
	 */
	public int saveOrderCustHis(String currentCustGuid,Integer month) {
		return jt.update(this.saveOrderCustHisByCustGudiSql, currentCustGuid, month);
	}
	
	//根据客户guid查询客户信息
	@SuppressWarnings("unchecked")
	public OrderCustomerInfo getOrderCustByGuid(String custGuid, boolean hisFlag) {
		// SELECT * FROM cc_order_cust_info t WHERE t.cust_guid=?
		String strSql = "";
		if (hisFlag) {
			strSql = this.queryCustHisByGuidSql;
		}
		else {
			strSql = this.queryCustByGuidSql;
		}
		
		List tmpList = jt.query(strSql, new Object[] { custGuid },
				new OrderCustInfoRmp());
		if (tmpList.isEmpty()) {
			log.warn("没有查询到客户guid为:" + custGuid + "的客户信息,请查证guid正确性!");
			return null;
		}
		
		OrderCustomerInfo orderCustInfo = (OrderCustomerInfo)tmpList.get(0);
		tmpList.clear();
		tmpList = null;
		
		return orderCustInfo;
	}
	
	/**
	 * 根据受理单号查询此受理单的客户信息
	 * @param orderId	受理单号
	 * @return	客户信息对象
	 */
	@SuppressWarnings({ "unchecked" })
	public OrderCustomerInfo getOrderCustByOrderId(String orderId) {
		/*
		 * SELECT a.* FROM CC_ORDER_CUST_INFO A, CC_SERVICE_ORDER_ASK B WHERE
		 * b.service_order_id = ? AND A.CUST_GUID = B.CUST_GUID
		 */
		List tmpList = jt.query(this.queryCustByOrderId, new Object[] { orderId }, new OrderCustInfoRmp());
		if (tmpList.isEmpty()) {
			log.warn("没有查询到受量单号为:{}的客户信息,请查证数据正确性!", orderId);
			return null;
		}
		return (OrderCustomerInfo) tmpList.get(0);
	}
	
	/**
	 * 删除受理单的客户信息
	 * @param orderId	受理单号	 
	 * @return	删除的记录条数
	 */
	public int delOrderCustInfo(String custGuid,Integer month) {
		/*
		 * DELETE FROM CC_ORDER_CUST_INFO A WHERE A.CUST_GUID = ?
		 */
    	return jt.update(this.delCustByCustGuid, custGuid, month);
	}
	
	/**
	 * 更新一个客户的信息
	 * @param custInfo	客户对像
	 * @return	更新的记录数
	 */
	public int updateCustInfo(OrderCustomerInfo custInfo) {
		/*
		    UPDATE CC_ORDER_CUST_INFO T
			   SET REGION_ID            = ?,
			       CUST_NAME            = ?,
			       CRM_CUST_ID          = ?,
			       CUST_SEX             = ?,
			       FAX_NUM              = ?,
			       E_MAIL               = ?,
			       MAIL_ADDR            = ?,
			       POST_CODE            = ?,
			       ID_TYPE              = ?,
			       ID_CARD              = ?,
			       CUST_TYPE            = ?,
			       CUST_TYPE_NAME       = ?,
			       CUST_SERV_GRADE      = ?,
			       CUST_SERV_GRADE_NAME = ?,
			       CUST_BRAND           = ?,
			       CUST_BRAND_DESC      = ?,
			       CUST_CONTENT         = ?,
			       CUST_CONTENT_DESC    = ?,
			       PROD_STATUS          = ?,
			       PROD_STATUS_DESC     = ?,
			       TRADE_TYPE           = ?,
			       TRADE_TYPE_DESC      = ?,
			       PROD_TYPE            = ?,
			       PROD_TYPE_DESC       = ?,
			       BRANCH_NO            = ?,
			       INSTALL_ADDR         = ?,
			       ADDONES_INFO         = ?,			       
			       SERVICE_GRANDE       = ?,
			       SERVICE_GRANDE_DESC  = ?,
			       INSTALL_DATE         = TO_DATE(?, 'YYYY-MM-DD HH24:MI:SS')
			 WHERE T.CUST_GUID = ?
		 */
		return jt.update(this.updateCustInfoSql,
				custInfo.getRegionId(),
				StringUtils.defaultIfEmpty(custInfo.getCustName(),null),
				custInfo.getCrmCustId(),
				custInfo.getCustSex(),
				StringUtils.defaultIfEmpty(custInfo.getFaxNum(),null),
				StringUtils.defaultIfEmpty(custInfo.getCustMail(),null),
				StringUtils.defaultIfEmpty(custInfo.getMailAddr(),null),
				StringUtils.defaultIfEmpty(custInfo.getPostCode(),null),
				custInfo.getIdType(),
				StringUtils.defaultIfEmpty(custInfo.getIdCard(),null),
				custInfo.getCustType(),
				StringUtils.defaultIfEmpty(custInfo.getCustTypeName(),null),
				custInfo.getCustServGrade(),
				StringUtils.defaultIfEmpty(custInfo.getCustServGradeName(),null),
				custInfo.getCustBrand(),
				StringUtils.defaultIfEmpty(custInfo.getCustBrandDesc(),null),
				custInfo.getCustBrandContent(),
				StringUtils.defaultIfEmpty(custInfo.getCustBrandContenDesc(),null),
				custInfo.getHighWarnId(),
				StringUtils.defaultIfEmpty(custInfo.getHighWarnDesc(),null),
				custInfo.getProdTatus(),
				StringUtils.defaultIfEmpty(custInfo.getProdTatusDesc(),null),
				custInfo.getTradeType(),
				StringUtils.defaultIfEmpty(custInfo.getTradeTypeDesc(),null),
				custInfo.getProdType(),
				StringUtils.defaultIfEmpty(custInfo.getProdTypeDesc(),null),
				StringUtils.defaultIfEmpty(custInfo.getBranchNo(),null),
				StringUtils.defaultIfEmpty(custInfo.getInstallAdd(),null),
				StringUtils.defaultIfEmpty(custInfo.getAddonesInfo(),null),
				custInfo.getServiceGrande(),
				StringUtils.defaultIfEmpty(custInfo.getServiceGrandeDesc(),null),
				StringUtils.defaultIfEmpty(custInfo.getInstalldate(),null),
				StringUtils.defaultIfEmpty(custInfo.getProdInstId(),null),
				StringUtils.defaultIfEmpty(custInfo.getAddressId(),null),
				StringUtils.defaultIfEmpty(custInfo.getAddressDesc(),null),
				custInfo.getCustAge(),
				custInfo.getNumRank(),
				StringUtils.defaultIfEmpty(custInfo.getIsRealname(),null),
				StringUtils.defaultIfEmpty(custInfo.getIdTypeName(),null),
				StringUtils.defaultIfEmpty(custInfo.getCustGuid(),null),
				custInfo.getMonth()
		);
	}

	/**
	 * @return the jt
	 */
	public JdbcTemplate getJt() {
		return jt;
	}

	/**
	 * @param jt the jt to set
	 */
	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}

	/**
	 * @return the saveOrderCustSql
	 */
	public String getSaveOrderCustSql() {
		return saveOrderCustSql;
	}

	/**
	 * @param saveOrderCustSql the saveOrderCustSql to set
	 */
	public void setSaveOrderCustSql(String saveOrderCustSql) {
		this.saveOrderCustSql = saveOrderCustSql;
	}

	/**
	 * @return the queryCustByGuidSql
	 */
	public String getQueryCustByGuidSql() {
		return queryCustByGuidSql;
	}

	/**
	 * @param queryCustByGuidSql the queryCustByGuidSql to set
	 */
	public void setQueryCustByGuidSql(String queryCustByGuidSql) {
		this.queryCustByGuidSql = queryCustByGuidSql;
	}

	/**
	 * @return the queryCustHisByGuidSql
	 */
	public String getQueryCustHisByGuidSql() {
		return queryCustHisByGuidSql;
	}

	/**
	 * @param queryCustHisByGuidSql the queryCustHisByGuidSql to set
	 */
	public void setQueryCustHisByGuidSql(String queryCustHisByGuidSql) {
		this.queryCustHisByGuidSql = queryCustHisByGuidSql;
	}

	/**
	 * @return the queryCustByOrderId
	 */
	public String getQueryCustByOrderId() {
		return queryCustByOrderId;
	}

	/**
	 * @param queryCustByOrderId the queryCustByOrderId to set
	 */
	public void setQueryCustByOrderId(String queryCustByOrderId) {
		this.queryCustByOrderId = queryCustByOrderId;
	}

	
	/**
	 * @return the delCustByCustGuid
	 */
	public String getDelCustByCustGuid() {
		return delCustByCustGuid;
	}

	/**
	 * @param delCustByCustGuid the delCustByCustGuid to set
	 */
	public void setDelCustByCustGuid(String delCustByCustGuid) {
		this.delCustByCustGuid = delCustByCustGuid;
	}

	/**
	 * @return the updateCustInfoSql
	 */
	public String getUpdateCustInfoSql() {
		return updateCustInfoSql;
	}

	/**
	 * @param updateCustInfoSql the updateCustInfoSql to set
	 */
	public void setUpdateCustInfoSql(String updateCustInfoSql) {
		this.updateCustInfoSql = updateCustInfoSql;
	}

	/**
	 * @return the saveOrderCustHisByCustGudiSql
	 */
	public String getSaveOrderCustHisByCustGudiSql() {
		return saveOrderCustHisByCustGudiSql;
	}

	/**
	 * @param saveOrderCustHisByCustGudiSql the saveOrderCustHisByCustGudiSql to set
	 */
	public void setSaveOrderCustHisByCustGudiSql(
			String saveOrderCustHisByCustGudiSql) {
		this.saveOrderCustHisByCustGudiSql = saveOrderCustHisByCustGudiSql;
	}

}
