package com.timesontransfar.customservice.tuschema.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.tuschema.dao.IserviceContentTypeDao;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSaveRmp;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSaveSJ;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSaveSJRmp;
import com.timesontransfar.customservice.tuschema.pojo.ServiceOfferInfo;

@Component("serviceContentType")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ServiceContentTypeDao implements IserviceContentTypeDao {

    private static final Logger log = LoggerFactory.getLogger(ServiceContentTypeDao.class);
    
    @Autowired
    private JdbcTemplate jt;

    private String insertSaveSql = 
    		"INSERT INTO cc_service_content_save\n"
    		+ "  (service_order_id,\n"
    		+ "   complaints_id,\n"
    		+ "   element_id,\n"
    		+ "   element_name,\n"
    		+ "   answer_id,\n"
    		+ "   answer_name,\n"
    		+ "   element_order,\n"
    		+ "   is_compare,\n"
    		+ "   is_stat)\n"
    		+ "VALUES  \n"
    		+ "  (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private String insertSaveSqlHis =
    		"INSERT INTO cc_service_content_save_his\n"
    		+ "(service_order_id,\n"
    		+ "complaints_id,\n"
    		+ "element_id,\n"
    		+ "element_name,\n"
    		+ "answer_id,\n"
    		+ "answer_name,\n"
    		+ "element_order,\n"
    		+ "is_compare,\n"
    		+ "is_stat)\n"
    		+ "SELECT service_order_id,\n"
    		+ "complaints_id,\n"
    		+ "element_id,\n"
    		+ "element_name,\n"
    		+ "answer_id,\n"
    		+ "SUBSTR(answer_name, 1, 4000),\n"
    		+ "element_order,\n"
    		+ "is_compare,\n"
    		+ "is_stat\n"
    		+ "FROM cc_service_content_save\n"
    		+ "WHERE cc_service_content_save.service_order_id = ?";

    private String selectSaveSql = "SELECT * FROM cc_service_content_save a WHERE a.service_order_id = ?";
    
    private String selectSaveHisSql = "SELECT * FROM cc_service_content_save_his a WHERE a.service_order_id = ?";

    private String insertSaveSJSql = "INSERT INTO cc_service_content_save_sj (service_order_id, content_id, content_desc) VALUES (?, ?, ?)";

    private String deleteSaveSJSql = "DELETE FROM cc_service_content_save_sj WHERE service_order_id = ?";

    private String selectSaveSJSql = "SELECT service_order_id, content_id, content_desc FROM cc_service_content_save_sj WHERE service_order_id = ?";


    /**
     * 逐条保存当前投诉受理模板
     * 
     * @param ServiceContentSave
     * @return 保存的记录数
     */
    public int insertContentSave(final ServiceContentSave[] serviceContentSaves) {
        int[] i = jt.batchUpdate(insertSaveSql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                ps.setString(1, serviceContentSaves[j].getServiceOrderId());
                ps.setString(2, serviceContentSaves[j].getComplaintsId());
                ps.setString(3, serviceContentSaves[j].getElementId());
                ps.setString(4, serviceContentSaves[j].getElementName());
                ps.setString(5, serviceContentSaves[j].getAnswerId());
                ps.setString(6, serviceContentSaves[j].getAnswerName());
                ps.setString(7, serviceContentSaves[j].getElementOrder());
                ps.setString(8, serviceContentSaves[j].getIsCompare());
                ps.setString(9, serviceContentSaves[j].getIsStat());
            }

            public int getBatchSize() {
                return serviceContentSaves.length;
            }
        });
        return i.length;
    }
    
    /**
     * 逐条保存当前结案模板
     * 
     * @param saveList
     * @return 保存的记录数
     */
    public int insertDealContentSave(final List<ServiceContentSave> saveList) {
    	String sql = "INSERT INTO CC_DEAL_CONTENT_SAVE(service_order_id, complaints_id, element_id, element_name, answer_id, answer_name, ALIAS_NAME) VALUES (?, ?, ?, ?, ?, ?, ?)";
        int[] i = jt.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                ps.setString(1, saveList.get(j).getServiceOrderId());
                ps.setString(2, saveList.get(j).getComplaintsId());
                ps.setString(3, saveList.get(j).getElementId());
                ps.setString(4, saveList.get(j).getElementName());
                ps.setString(5, saveList.get(j).getAnswerId());
                ps.setString(6, StringUtils.substring(saveList.get(j).getAnswerName(), 0, 4000));
                ps.setString(7, saveList.get(j).getAliasName());
            }
            public int getBatchSize() {
                return saveList.size();
            }
        });
        return i.length;
    }
    
    @Override
	public int deleteContentSaveByOrderId(String orderId) {
		String sql = "delete from cc_service_content_save where service_order_id = ?";
		return this.jt.update(sql, orderId);
	}
    
	public int deleteDealContentSaveByOrderId(String orderId) {
		String sql = "delete from CC_DEAL_CONTENT_SAVE where service_order_id = ?";
		return this.jt.update(sql, orderId);
	}

    /**
     * 同一定单下的受理模板保存进历史表
     * 
     * @param serviceOrderId
     * @return 保存的记录数
     */
    public int insertContentSaveHis(String serviceOrderId) {
        return jt.update(this.insertSaveSqlHis, serviceOrderId);
    }
    
    public int insertDealContentSaveHis(String serviceOrderId) {
    	String sql = "INSERT INTO CC_DEAL_CONTENT_SAVE_HIS(SERVICE_ORDER_ID, COMPLAINTS_ID, ELEMENT_ID, ELEMENT_NAME, ANSWER_ID, ANSWER_NAME, ALIAS_NAME) "
    			+ "SELECT SERVICE_ORDER_ID, COMPLAINTS_ID, ELEMENT_ID, ELEMENT_NAME, ANSWER_ID, ANSWER_NAME, ALIAS_NAME FROM CC_DEAL_CONTENT_SAVE WHERE service_order_id = ?";
        return jt.update(sql, serviceOrderId);
    }

	public String getAnswerNameByOrderId(String orderId, String elementId) {
		String sql = "SELECT ANSWER_NAME FROM cc_deal_content_save WHERE element_id=? AND service_order_id=? LIMIT 1";
		List list = jt.queryForList(sql, elementId, orderId);
		if (list.isEmpty()) {
			return "";
		}
		Map map = (Map) list.get(0);
		return map.get("ANSWER_NAME").toString();
	}

	public String getHisAnswerNameByOrderId(String orderId, String elementId) {
		String sql = "SELECT ANSWER_NAME FROM cc_deal_content_save_his WHERE element_id=? AND service_order_id=? LIMIT 1";
		List list = jt.queryForList(sql, elementId, orderId);
		if (list.isEmpty()) {
			return "";
		}
		Map map = (Map) list.get(0);
		return map.get("ANSWER_NAME").toString();
	}

    /**
     * 得到当前受理内容
     * 
     * @param serviceOrderId
     * @return ServiceContentSave
     */
	public ServiceContentSave[] selectContentSave(String serviceOrderId) {
        List temp = jt.query(this.selectSaveSql, new Object[] {serviceOrderId},
                new ServiceContentSaveRmp());
        int size = temp.size();
        if (size == 0) {
            return new ServiceContentSave[0];
        }
        ServiceContentSave[] serviceContentSave = new ServiceContentSave[size];
        for (int i = 0; i < size; i++) {
            serviceContentSave[i] = (ServiceContentSave) temp.get(i);
        }
        temp.clear();
        temp = null;
        return serviceContentSave;
    }
	
	/**
     * 得到历史受理内容
     * 
     * @param serviceOrderId
     * @return ServiceContentSave
     */
	public ServiceContentSave[] selectContentSaveHis(String serviceOrderId) {
        List temp = jt.query(this.selectSaveHisSql, new Object[] {serviceOrderId},
                new ServiceContentSaveRmp());
        int size = temp.size();
        if (size == 0) {
            return new ServiceContentSave[0];
        }
        ServiceContentSave[] serviceContentSave = new ServiceContentSave[size];
        for (int i = 0; i < size; i++) {
            serviceContentSave[i] = (ServiceContentSave) temp.get(i);
        }
        temp.clear();
        temp = null;
        return serviceContentSave;
    }

	/**
	 * 逐条保存暂存商机模板
	 * 
	 * @param ServiceContentSaveSJ
	 * @return 保存的记录数
	 */
	public int insertContentSaveSJ(final ServiceContentSaveSJ[] serviceContentSaveSJs) {
		int[] i = jt.batchUpdate(insertSaveSJSql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int j) throws SQLException {
				ps.setString(1, serviceContentSaveSJs[j].getServiceOrderId());
				ps.setString(2, serviceContentSaveSJs[j].getContentId());
				ps.setString(3, serviceContentSaveSJs[j].getContentDesc());
			}

			public int getBatchSize() {
				return serviceContentSaveSJs.length;
			}
		});
		return i.length;
	}

	/**
	 * 删除暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @return 更新的记录数
	 */
	public int deleteContentSaveSJ(String serviceOrderId) {
		return jt.update(this.deleteSaveSJSql, serviceOrderId);
	}

	/**
	 * 得到暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @return ServiceContentSaveSJ
	 */
	public ServiceContentSaveSJ[] selectContentSaveSJ(String serviceOrderId) {
		List temp = jt.query(this.selectSaveSJSql, new Object[] { serviceOrderId }, new ServiceContentSaveSJRmp());
		int size = temp.size();
		if (size == 0) {
			if (log.isDebugEnabled()) {
				log.debug("未查询到任务ID为" + serviceOrderId + "的投诉受理内容");
			}
			return new ServiceContentSaveSJ[0];
		}
		ServiceContentSaveSJ[] serviceContentSaveSJs = new ServiceContentSaveSJ[size];
		for (int i = 0; i < size; i++) {
			serviceContentSaveSJs[i] = (ServiceContentSaveSJ) temp.get(i);
		}
		return serviceContentSaveSJs;
	}
	
	public int updateAnalysisInfo(String orderId, String verificationInfoNew, String processingResultNew) {
		String sql = "select count(1) from cc_order_analysis_log c where c.ORDER_ID = ?";
		int count = jt.queryForObject(sql, new Object[] { orderId }, Integer.class);
		log.info("getAnalysisInfo result: {}", count);
		if(count > 0) {
			String updateSql = "UPDATE cc_order_analysis_log SET VERIFICATION_INFO_N = ?, PROCESSING_RESULT_N = ?, MODIFY_TIME = now() WHERE ORDER_ID = ?";
			int result = jt.update(updateSql, verificationInfoNew, processingResultNew, orderId);
			log.info("updateAnalysisInfo result: {}", result);
			return result;
		}
		return 0;
	}

	public int updateBigModel(String orderId, String verificationInfoNew, String processingResultNew, String processingResultMore, String approve) {
		String sql = "select LOG_ID from cs_summary_callback_log c where c.SERVICE_ORDER_ID = ? order by CALLBACK_DATE desc limit 1";
		List<Map<String, Object>> list = this.jt.queryForList(sql, orderId);
		if(list.isEmpty()){
			return 0;
		}
		Map<String, Object> map = list.get(0);
		String logId = map.get("LOG_ID").toString();
		log.info("updateBigModel result: {}", logId);
		if(StringUtils.isNotBlank(logId)) {
			String comparisonResults = "";
			boolean b = this.checkContent(orderId, verificationInfoNew, processingResultNew, processingResultMore, approve);//对比核查情况、处理结果、处理结果补充
			if(b){
				comparisonResults = "1";
			}else {
				comparisonResults = "0";
			}
			String updateSql = "UPDATE cs_summary_callback_log SET NEW_VERIFICATION_STATUS = ?, NEW_HANDLING_RESULT = ?, HANDLING_RESULT_MORE = ?,APPROVE = ?,FLAG = ?,COMMIT_TIME = now() WHERE LOG_ID = ?";
			int result = jt.update(updateSql, verificationInfoNew, processingResultNew, processingResultMore,approve,comparisonResults,logId);
			log.info("updateBigModel result: {}", result);
			return result;
		}
		return 0;
	}

	/**
	 * 对比核查情况、处理结果、处理结果补充
	 * */
	@SuppressWarnings("all")
	private boolean checkContent(String orderId,String verificationInfoNew,String processingResultNew, String processingResultMore,String approve){
		boolean resp = false;
		try{
			String callbackLog = this.qryCallbackLog(orderId);
			if(StringUtils.isNotBlank(callbackLog)){
				JSONObject jsonObject = JSON.parseObject(callbackLog);
				String userComplaintIssue = jsonObject.getString("USER_COMPLAINT_ISSUE").replaceAll("[\\pP\\p{Punct}]", "");//用户投诉问题
				String verificationStatus = jsonObject.getString("VERIFICATION_STATUS").replaceAll("[\\pP\\p{Punct}]", "");//核查情况
				String handlingResult = jsonObject.getString("HANDLING_RESULT").replaceAll("[\\pP\\p{Punct}]", "");//处理结果
				String userApprovalOfSolution = jsonObject.getString("USER_APPROVAL_OF_SOLUTION").replaceAll("[\\pP\\p{Punct}]", "");//用户是否认可处理方案
				String escalationTendency = jsonObject.getString("ESCALATION_TENDENCY").replaceAll("[\\pP\\p{Punct}]", "");//越级倾向
				String contactPlan = jsonObject.getString("CONTACT_PLAN").replaceAll("[\\pP\\p{Punct}]", "");//联系计划
				String resultApprove = "";//翻译后的是否认可处理方案
				if(StringUtils.isNotBlank(userApprovalOfSolution) && userApprovalOfSolution.startsWith("是")){
					resultApprove = "认可";
				}else if(StringUtils.isNotBlank(userApprovalOfSolution) && userApprovalOfSolution.startsWith("否")){
					resultApprove = "不认可";
				}else {
					resultApprove = "是否认可未予评价";
				}
				if(StringUtils.isNotBlank(verificationInfoNew) && StringUtils.isNotBlank(processingResultNew) && StringUtils.isNotBlank(approve)){
					String a1 = verificationInfoNew.replaceAll("[\\pP\\p{Punct}]", "");//前端传入的核查情况去除标点
					String b1 = processingResultNew.replaceAll("[\\pP\\p{Punct}]", "");//前端传入的处理结果去除标点
					int i1 = a1.indexOf(userComplaintIssue);
					int i2 = a1.indexOf(verificationStatus);
					int i3 = b1.indexOf(handlingResult);
					int i4 = b1.indexOf(escalationTendency);
					int i5 = b1.indexOf(contactPlan);
					boolean approveFlag = resultApprove.equals(approve);
					if(i1!=-1 && i2!=-1 && i3!=-1 && i4!=-1 && i5!=-1 && approveFlag){
						resp = true;
					}
				}
				if(StringUtils.isNotBlank(processingResultMore) && StringUtils.isNotBlank(approve)){
					String c1 = processingResultMore.replaceAll("[\\pP\\p{Punct}]", "");//前端传入的处理结果补充去除标点
					int i1 = c1.indexOf(userComplaintIssue);
					int i2 = c1.indexOf(verificationStatus);
					int i3 = c1.indexOf(handlingResult);
					int i4 = c1.indexOf(escalationTendency);
					int i5 = c1.indexOf(contactPlan);
					boolean approveFlag = resultApprove.equals(approve);
					if(i1!=-1 && i2!=-1 && i3!=-1 && i4!=-1 && i5!=-1 && approveFlag){
						resp = true;
					}
				}
			}
		}catch (Exception e){
			log.error("checkContent error: {}",e.getMessage(),e);
		}
		return resp;
	}

	private String qryCallbackLog(String orderId){
		String sql = "SELECT LOG_ID,SERVICE_ORDER_ID,ORDER_ID,CALL_ID,RECORD_FLOW,CALLBACK_DATE,USER_COMPLAINT_ISSUE,VERIFICATION_STATUS," +
				"HANDLING_RESULT,USER_APPROVAL_OF_SOLUTION,ESCALATION_TENDENCY,CONTACT_PLAN,IS_BACKFILL,USE_DATE FROM CS_SUMMARY_CALLBACK_LOG c where c.SERVICE_ORDER_ID = ? ORDER BY CALLBACK_DATE DESC limit 1";
		String callbackLog = "";
		try{
			List<Map<String, Object>> list = this.jt.queryForList(sql, orderId);
			Map<String, Object> map = null;
			if(!list.isEmpty()){
				map = list.get(0);
			}
			callbackLog = JSON.toJSONString(map);
		}catch (Exception e){
			log.error("qryCallbackLog error: {}",e.getMessage(),e);
		}
		return callbackLog;
	}
	
	public int finishAnalysisInfo(String orderId) {
		String sql = "update cc_order_analysis_log set MODIFY_TIME = now(), ORDER_STATUS = 1 where ORDER_ID = ?";
		return jt.update(sql,orderId);
	}

	// 保存销售品信息
	@Override
	public int insertServiceOfferInfo(ServiceOfferInfo soi) {
		String sql = "INSERT INTO cc_service_offer_info(service_order_id,offer_nbr,offer_nm,offer_chnl,offer_chnl_nm,offer_chnl_one,offer_chnl_one_nm,"
				+ "offer_chnl_two,offer_chnl_two_nm,offer_chnl_three,offer_chnl_three_nm)VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		return this.jt.update(sql, soi.getServiceOrderId(), soi.getOfferNbr(), soi.getOfferNm(), soi.getOfferChnl(),
				soi.getOfferChnlNm(), soi.getOfferChnlOne(), soi.getOfferChnlOneNm(), soi.getOfferChnlTwo(),
				soi.getOfferChnlTwoNm(), soi.getOfferChnlThree(), soi.getOfferChnlThreeNm());
	}

	// 清空销售品信息
	@Override
	public int clearServiceOfferInfo(String serviceOrderId) {
		String sql = "UPDATE cc_service_offer_info SET offer_nbr=NULL,offer_nm=NULL,offer_chnl=NULL,offer_chnl_nm=NULL,offer_chnl_one=NULL,"
				+ "offer_chnl_one_nm=NULL,offer_chnl_two=NULL,offer_chnl_two_nm=NULL,offer_chnl_three=NULL,offer_chnl_three_nm=NULL WHERE "
				+ "service_order_id=?";
		return this.jt.update(sql, serviceOrderId);
	}

	// 更新销售品信息
	@Override
	public int updateServiceOfferInfo(ServiceOfferInfo soi) {
		String sql = "UPDATE cc_service_offer_info SET offer_nbr=?,offer_nm=?,offer_chnl=?,offer_chnl_nm=?,offer_chnl_one=?,offer_chnl_one_nm=?,"
				+ "offer_chnl_two=?,offer_chnl_two_nm=?,offer_chnl_three=?,offer_chnl_three_nm=? WHERE service_order_id=?";
		return this.jt.update(sql, soi.getOfferNbr(), soi.getOfferNm(), soi.getOfferChnl(), soi.getOfferChnlNm(),
				soi.getOfferChnlOne(), soi.getOfferChnlOneNm(), soi.getOfferChnlTwo(), soi.getOfferChnlTwoNm(),
				soi.getOfferChnlThree(), soi.getOfferChnlThreeNm(), soi.getServiceOrderId());
	}

	// 归档销售品信息
	@Override
	public int insertServiceOfferInfoHis(String serviceOrderId) {
		String sql = "UPDATE cc_service_offer_info SET order_status=1 WHERE service_order_id=?";
		return this.jt.update(sql, serviceOrderId);
	}

	// 判断是否存在销售品信息
	@Override
	public int countServiceOfferInfo(String serviceOrderId) {
		String sql = "SELECT COUNT(1)FROM cc_service_offer_info WHERE service_order_id=?";
		return this.jt.queryForObject(sql, new Object[] { serviceOrderId }, Integer.class);
	}

	// 保存退费业务信息
	@Override
	public int saveRefundBusiness(String orderId,String offerId) {
		String sql = "INSERT INTO CC_REFUND_OFFER_INFO (service_order_id,offer_id,status) VALUES (?,?,'1')";
		return this.jt.update(sql,orderId,offerId);
	}

	// 退费业务重置
	@Override
	public int upRefundBusiness(String serviceOrderId) {
		String sql = "UPDATE CC_REFUND_OFFER_INFO SET STATUS = 0 WHERE SERVICE_ORDER_ID = ?";
		return this.jt.update(sql, serviceOrderId);
	}
}