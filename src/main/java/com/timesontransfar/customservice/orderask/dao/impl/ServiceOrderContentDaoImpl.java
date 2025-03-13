/**
 * <p>类名：ServiceOrderContentDaoImpl.java</p>
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

import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.ServContentRmp;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;

@SuppressWarnings("rawtypes")
public class ServiceOrderContentDaoImpl implements IserviceContentDao {

	private static final Logger logger = LoggerFactory.getLogger(ServiceOrderContentDaoImpl.class);
	
	private JdbcTemplate jt;

	private String saveServContentSql;
	private String saveServContentHisByOrderIdSql;
	private String queryServContentSql;
	private String queryServContentHisSql;
	private String delServContentSql;
	private String updateServContentSql;	
	private String updateAccContSql;
		

	public int checkPubReference(int firstId,int secendId ) {
	   String strsql = "SELECT a.REFER_ID FROM pub_column_reference a WHERE a.REFER_ID =? AND a.ENTITY_ID =?";
	   List list=jt.queryForList(strsql,new Object[]{secendId,String.valueOf(firstId)},List.class);
	   if(list.isEmpty()){
		   return 0;
	   }
	   return list.size();
	}
	
	public int saveServiceContent(ServiceContent serviceContent, boolean hisFlag) {
		return jt.update(this.saveServContentSql,
				serviceContent.getServOrderId(),
				serviceContent.getOrderVer(),
				serviceContent.getRegionId(),
				StringUtils.defaultIfEmpty(serviceContent.getRegionName(),null),
				serviceContent.getServType(),
				StringUtils.defaultIfEmpty(serviceContent.getServTypeDesc(),null),
				serviceContent.getAppealProdId(),
				StringUtils.defaultIfEmpty(serviceContent.getAppealProdName(),null),
				serviceContent.getAppealReasonId(),
				StringUtils.defaultIfEmpty(serviceContent.getAppealReasonDesc(),null),
				serviceContent.getAppealDetailId(),
				StringUtils.defaultIfEmpty(serviceContent.getAppealDetailDesc(),null),
				StringUtils.defaultIfEmpty(serviceContent.getProdNum(),"0"),
				StringUtils.defaultIfEmpty(serviceContent.getAcceptContent(),null),
				StringUtils.defaultIfEmpty(serviceContent.getCustExpect(),null),
				StringUtils.defaultIfEmpty(serviceContent.getComplaintedOrg(),null),
				StringUtils.defaultIfEmpty(serviceContent.getComplaintedOrdName(),null),
				serviceContent.getUnsatisNum(),		
				serviceContent.getMonth(),			
				serviceContent.getAppealChild(),
				StringUtils.defaultIfEmpty(serviceContent.getAppealChildDesc(),null),
				serviceContent.getFouGradeCatalog(),
				StringUtils.defaultIfEmpty(serviceContent.getFouGradeDesc(),null),
				StringUtils.defaultIfEmpty(serviceContent.getTermiProductId(), null),
				StringUtils.defaultIfEmpty(serviceContent.getTermiProductDesc(),null),
				StringUtils.defaultIfEmpty(serviceContent.getTermiProductType(),null),
				serviceContent.getFiveCatalog(),
				StringUtils.defaultIfEmpty(serviceContent.getFiveGradeDesc(),null),
				serviceContent.getSixCatalog(),
				StringUtils.defaultIfEmpty(serviceContent.getSixGradeDesc(),null),
				StringUtils.defaultIfEmpty(serviceContent.getOutletsName(),null),//网点信息
				StringUtils.defaultIfEmpty(serviceContent.getOutletsGuid(),null),
				StringUtils.defaultIfEmpty(serviceContent.getServiceTypeDetail(),null),
				StringUtils.defaultIfEmpty(serviceContent.getOutletsAddress(),null),//网点信息
				StringUtils.defaultIfEmpty(serviceContent.getOutletsArCode(),null),
				StringUtils.defaultIfEmpty(serviceContent.getChannelTpName(),null),
				serviceContent.getFiveOrder(),
				StringUtils.defaultIfEmpty(serviceContent.getFiveOrderDesc(),null),
				serviceContent.getBestOrder(),
				StringUtils.defaultIfEmpty(serviceContent.getBestOrderDesc(),null),
				serviceContent.getProdOne(),
				StringUtils.defaultIfEmpty(serviceContent.getProdOneDesc(),null),
				serviceContent.getProdTwo(),
				StringUtils.defaultIfEmpty(serviceContent.getProdTwoDesc(),null),
				serviceContent.getDevtChsOne(),
				StringUtils.defaultIfEmpty(serviceContent.getDevtChsOneDesc(),null),
				serviceContent.getDevtChsTwo(),
				StringUtils.defaultIfEmpty(serviceContent.getDevtChsTwoDesc(),null),
				serviceContent.getDevtChsThree(),
				StringUtils.defaultIfEmpty(serviceContent.getDevtChsThreeDesc(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDvlpChnl(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDvlpChnlNm(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDisputeChnl(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDisputeChnlNm(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDisputeChnl1(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDisputeChnl1Nm(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDisputeChnl2(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDisputeChnl2Nm(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDisputeChnl3(),null),
				StringUtils.defaultIfEmpty(serviceContent.getDisputeChnl3Nm(),null),
				StringUtils.defaultIfEmpty(serviceContent.getOfferId(),null),
				StringUtils.defaultIfEmpty(serviceContent.getOfferName(),null)
		);
	}
	
	/**
	 * 更新定单的受理内容
	 * @param id,content内容
	 * @return	更新的记录数
	 */
	public int updateAccpContent(String id,String content,Integer month){
		return jt.update(this.updateAccContSql, content, id, month);
	}
	
	private String updateContentSql;
	public int updateAcceptContent(String id,String content,Integer month){
		return jt.update(this.updateContentSql, content, id, month);
	}
	
	
	
	public String getUpdateContentSql() {
		return updateContentSql;
	}

	public void setUpdateContentSql(String updateContentSql) {
		this.updateContentSql = updateContentSql;
	}

	/**
	 * 将当受理内容当前表的信息保存到历史表中
	 * @param currentOrderId	当前受量内容的受理单号
	 * @return	保存记录成功记录数
	 */
	public int saveServContentHis(String currentOrderId,Integer month) {
		return jt.update(this.saveServContentHisByOrderIdSql, currentOrderId, month);
	}
	
	/**
	 * 根据受单号查询此受理单的受理内容
	 * @param orderId 受理单id	
	 * @param hisFlag 当前/历史
	 * @return 受理单受理内容对象
	 */
	@SuppressWarnings("unchecked")
	public ServiceContent getServContentByOrderId(String orderId, boolean hisFlag, int version) {
		String strsql = "";
		List tmpList = null;
		if(hisFlag){
			strsql = this.queryServContentHisSql;
			tmpList = this.jt.query(strsql, new Object[]{orderId, version}, new ServContentRmp());
		}else{
			strsql = this.queryServContentSql;
			tmpList = this.jt.query(strsql, new Object[]{orderId}, new ServContentRmp());
		}
		
		if(tmpList.isEmpty()){
			logger.error("受理单号: {} 查询不到受理内容!", orderId);
			return null;
		}
		return (ServiceContent) tmpList.get(0);
	}
	/**
	 * 根据受单号查询此受理单的受理内容
	 * @param orderId 受理单id	
	 * @param hisFlag 当前/历史
	 * @return 受理单受理内容对象
	 */
	@SuppressWarnings("unchecked")
	public ServiceContent getServContentByOrderIdNew(String orderId, String suffix, boolean hisFlag, int version) {
		// SELECT * FROM cc_service_content_ask a WHERE a.service_order_id = ?	
		// SELECT * FROM cc_service_content_ask_his a WHERE a.service_order_id = ?
		if(suffix == null){
			suffix = "";
		}
		suffix = suffix.trim();
		String tbname = "cc_service_content_ask" + suffix;
		String strsql = queryServContentSql.replace("cc_service_content_ask", tbname);
		List tmpList = null;
		if(suffix.length() > 0){
			tmpList = this.jt.query(strsql, new Object[] { orderId }, new ServContentRmp());
		}else{
			if(hisFlag){
				strsql = this.queryServContentHisSql;
				tmpList = this.jt.query(strsql, new Object[] { orderId, version }, new ServContentRmp());
			}else{
				strsql = this.queryServContentSql;
				tmpList = this.jt.query(strsql, new Object[] { orderId }, new ServContentRmp());
			}
		}
		
		if(tmpList.isEmpty()){
			logger.error("查询不到受理单号为:" + orderId + "的受理内容！异常返回");
			return null;
		}
		
		ServiceContent servContent = (ServiceContent) tmpList.get(0);
		tmpList.clear();
		tmpList = null;
		
		return servContent;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public ServiceContent getServContentByOrderId(String orderId, String suffix, int version) {
		// SELECT * FROM cc_service_content_ask a WHERE a.service_order_id = ?	
		if(suffix == null){
			suffix = "";
		}
		suffix = suffix.trim();
		String tbname = "cc_service_content_ask" + suffix;
		String strsql = queryServContentSql.replace("cc_service_content_ask", tbname);
		List tmpList = null;
		if(suffix.length() > 0){
			tmpList = this.jt.query(strsql+" AND A.ORDER_VESION=?", new Object[] {orderId, version}, new ServContentRmp());
		}else{
			tmpList = this.jt.query(strsql, new Object[] {orderId}, new ServContentRmp());
		}
		if(tmpList.isEmpty()){
			logger.debug("查询不到受理单号为:" + orderId + "的受理内容！异常返回");
			return null;
		}
		ServiceContent servContent = (ServiceContent) tmpList.get(0);
		tmpList.clear();
		tmpList = null;
		return servContent;
	}
	
	/**
	 * 删除受理单的受理内容信息
	 * @param orderId	受理单号	
	 * @return 删除的记录数
	 */
	public int delServContent(String orderId,Integer month) {
		// DELETE FROM CC_SERVICE_CONTENT_ASK A WHERE A.SERVICE_ORDER_ID = ?
    	return this.jt.update(this.delServContentSql, orderId, month);
	}
	
	
	/**
	 * 更新定单的受理内容
	 * @param servContent	受理内容对像
	 * @return	更新的记录数
	 */
	public int updateServContent(ServiceContent servContent) {
    	return jt.update(this.updateServContentSql,
				servContent.getOrderVer(),
				servContent.getRegionId(),
				StringUtils.defaultIfEmpty(servContent.getRegionName(), null),
				servContent.getServType(),
				StringUtils.defaultIfEmpty(servContent.getServTypeDesc(), null),
				servContent.getAppealProdId(),
				StringUtils.defaultIfEmpty(servContent.getAppealProdName(), null),
				servContent.getAppealReasonId(),
				StringUtils.defaultIfEmpty(servContent.getAppealReasonDesc(), null),
				servContent.getAppealDetailId(),
				StringUtils.defaultIfEmpty(servContent.getAppealDetailDesc(), null),
				servContent.getFouGradeCatalog(),
                StringUtils.defaultIfEmpty(servContent.getFouGradeDesc(), null),
                servContent.getFiveCatalog(),
                StringUtils.defaultIfEmpty(servContent.getFiveGradeDesc(), null),
                servContent.getSixCatalog(),
                StringUtils.defaultIfEmpty(servContent.getSixGradeDesc(), null),
				StringUtils.defaultIfEmpty(servContent.getProdNum(), "0"),
				StringUtils.defaultIfEmpty(servContent.getAcceptContent(), null),
				StringUtils.defaultIfEmpty(servContent.getCustExpect(), null),
				StringUtils.defaultIfEmpty(servContent.getComplaintedOrg(), null),
				StringUtils.defaultIfEmpty(servContent.getComplaintedOrdName(), null),
				servContent.getUnsatisNum(),
				servContent.getAppealChild(),
				StringUtils.defaultIfEmpty(servContent.getAppealChildDesc(), null),
				StringUtils.defaultIfEmpty(servContent.getTermiProductId(), null),
				StringUtils.defaultIfEmpty(servContent.getTermiProductDesc(),null),
				StringUtils.defaultIfEmpty(servContent.getTermiProductType(),null),
				StringUtils.defaultIfEmpty(servContent.getServiceTypeDetail(),null),
				StringUtils.defaultIfEmpty(servContent.getOutletsName(),null),
				StringUtils.defaultIfEmpty(servContent.getOutletsGuid(),null),
				StringUtils.defaultIfEmpty(servContent.getOutletsAddress(),null),
				StringUtils.defaultIfEmpty(servContent.getOutletsArCode(),null),
				StringUtils.defaultIfEmpty(servContent.getChannelTpName(),null),
				servContent.getFiveOrder(),
				StringUtils.defaultIfEmpty(servContent.getFiveOrderDesc(),null),
				servContent.getBestOrder(),
				StringUtils.defaultIfEmpty(servContent.getBestOrderDesc(),null),
				servContent.getProdOne(),
				StringUtils.defaultIfEmpty(servContent.getProdOneDesc(),null),
				servContent.getProdTwo(),
				StringUtils.defaultIfEmpty(servContent.getProdTwoDesc(),null),
				servContent.getDevtChsOne(),
				StringUtils.defaultIfEmpty(servContent.getDevtChsOneDesc(),null),
				servContent.getDevtChsTwo(),
				StringUtils.defaultIfEmpty(servContent.getDevtChsTwoDesc(),null),
				servContent.getDevtChsThree(),
				StringUtils.defaultIfEmpty(servContent.getDevtChsThreeDesc(),null),
				servContent.getDvlpChnl(),
				servContent.getDvlpChnlNm(),
				StringUtils.defaultIfEmpty(servContent.getDisputeChnl(),null),
				StringUtils.defaultIfEmpty(servContent.getDisputeChnlNm(),null),
				StringUtils.defaultIfEmpty(servContent.getDisputeChnl1(),null),
				StringUtils.defaultIfEmpty(servContent.getDisputeChnl1Nm(),null),
				StringUtils.defaultIfEmpty(servContent.getDisputeChnl2(),null),
				StringUtils.defaultIfEmpty(servContent.getDisputeChnl2Nm(),null),
				StringUtils.defaultIfEmpty(servContent.getDisputeChnl3(),null),
				StringUtils.defaultIfEmpty(servContent.getDisputeChnl3Nm(),null),
				StringUtils.defaultIfEmpty(servContent.getOfferId(),null),
				StringUtils.defaultIfEmpty(servContent.getOfferName(),null),
				servContent.getServOrderId()
			);
	}
	
	/**
	 * 更新客户的受理内容
	 * @param regionId 地域
	 * @param orderId 服务申请号
	 * @param acceptContent 受理内容
	 * @return
	 */
	public int updateAcceptContent(int regionId,String orderId,String acceptContent){
		String strSql = "UPDATE CC_SERVICE_CONTENT_ASK T SET T.Accept_Content=? WHERE T.REGION_ID=? AND T.SERVICE_ORDER_ID=?";
		return jt.update(strSql, acceptContent, regionId, orderId);
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
	 * @return the saveServContentSql
	 */
	public String getSaveServContentSql() {
		return saveServContentSql;
	}

	/**
	 * @param saveServContentSql the saveServContentSql to set
	 */
	public void setSaveServContentSql(String saveServContentSql) {
		this.saveServContentSql = saveServContentSql;
	}

	/**
	 * @return the queryServContentSql
	 */
	public String getQueryServContentSql() {
		return queryServContentSql;
	}

	/**
	 * @param queryServContentSql the queryServContentSql to set
	 */
	public void setQueryServContentSql(String queryServContentSql) {
		this.queryServContentSql = queryServContentSql;
	}

	

	
	/**
	 * @return the delServContentSql
	 */
	public String getDelServContentSql() {
		return delServContentSql;
	}

	/**
	 * @param delServContentSql the delServContentSql to set
	 */
	public void setDelServContentSql(String delServContentSql) {
		this.delServContentSql = delServContentSql;
	}

	/**
	 * @return the updateServContentSql
	 */
	public String getUpdateServContentSql() {
		return updateServContentSql;
	}

	/**
	 * @param updateServContentSql the updateServContentSql to set
	 */
	public void setUpdateServContentSql(String updateServContentSql) {
		this.updateServContentSql = updateServContentSql;
	}

	/**
	 * @return the saveServContentHisByOrderIdSql
	 */
	public String getSaveServContentHisByOrderIdSql() {
		return saveServContentHisByOrderIdSql;
	}

	/**
	 * @param saveServContentHisByOrderIdSql the saveServContentHisByOrderIdSql to set
	 */
	public void setSaveServContentHisByOrderIdSql(
			String saveServContentHisByOrderIdSql) {
		this.saveServContentHisByOrderIdSql = saveServContentHisByOrderIdSql;
	}


	/**
	 * @return the queryServContentHisSql
	 */
	public String getQueryServContentHisSql() {
		return queryServContentHisSql;
	}


	/**
	 * @param queryServContentHisSql the queryServContentHisSql to set
	 */
	public void setQueryServContentHisSql(String queryServContentHisSql) {
		this.queryServContentHisSql = queryServContentHisSql;
	}



	public String getUpdateAccContSql() {
		return updateAccContSql;
	}

	public void setUpdateAccContSql(String updateAccContSql) {
		this.updateAccContSql = updateAccContSql;
	}
	
	public int updateBestOrder(String orderId){
		String strSql = "UPDATE CC_SERVICE_CONTENT_ASK T SET T.BEST_ORDER=100122410, T.BEST_ORDER_DESC='否' WHERE T.SERVICE_ORDER_ID=?";
		return jt.update(strSql, orderId);
	}
	
	public void saveBestOrderModify(String orderId, String acceptDate, int bestOrderOrigin, int bestOrderFinal, int modifyType) {
		try {
			String strSql = "insert into cc_order_best_modify "
					+ "(SERVICE_ORDER_ID,ACCEPT_DATE,BEST_ORDER_ORIGIN,BEST_ORDER_FINAL,MODIFY_TYPE,FINISH_DATE) "
					+ "values (?,str_to_date(?, '%Y-%m-%d %H:%i:%s'),?,?,?,now())";
			int num = jt.update(strSql, orderId, acceptDate, bestOrderOrigin, bestOrderFinal, modifyType);
			logger.info("saveBestOrderModify result: {}", num);
		}
		catch(Exception e) {
			logger.error("saveBestOrderModify error: {}", e.getMessage(), e);
		}
	}

}