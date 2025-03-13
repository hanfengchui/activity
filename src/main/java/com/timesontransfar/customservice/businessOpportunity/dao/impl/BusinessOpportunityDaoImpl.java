package com.timesontransfar.customservice.businessOpportunity.dao.impl;

import com.timesontransfar.customservice.businessOpportunity.dao.BusinessOpportunityDao;

import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

public class BusinessOpportunityDaoImpl implements BusinessOpportunityDao {

    private static final Logger log = LoggerFactory.getLogger(BusinessOpportunityDaoImpl.class);

    @Autowired
    private JdbcTemplate jt;

    /**
     * 更新商机单信息
     *
     * @param buopSheetInfo 商机单对象
     * @return
     */
    @Override
    public int updateBuopSheetInfo(BuopSheetInfo buopSheetInfo) {
        String strSql;
        if("0".equals(buopSheetInfo.getSheetStatus())) {
        	strSql = "UPDATE CC_BUOP_SHEET_INFO A SET A.ACCEPT_ORDER_ID=?,A.OFFER_ID=?,A.OFFER_NAME=?,A.ACCEPT_DATE=?,A.ACCEPT_CHNL=?,A.ACCEPT_STAFF=?," +
                    "A.DEAL_RESULT=?,A.BUSI_STATE=?,A.FAIL_RESULT=?,A.IS_TRANS=?,A.SATASFI=?,A.REPLY_REMARK=?,A.DEVELOP_STAFF=?,A.MODIFY_DATE=now() WHERE A.SERVICE_ORDER_ID = ?";
            return this.jt.update(strSql,
                    StringUtils.defaultIfEmpty(buopSheetInfo.getAcceptOrderId(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getOfferId(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getOfferName(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getAcceptDate(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getAcceptChnl(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getAcceptStaff(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getDealResult(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getBusiState(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getFailResult(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getIsTrans(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getSatasfi(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getReplyRemark(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getDevelopStaff(), null),
                    buopSheetInfo.getServiceOrderId()
            );
        }else if("1".equals(buopSheetInfo.getSheetStatus())) {
        	strSql = "UPDATE CC_BUOP_SHEET_INFO A SET A.ACCEPT_ORDER_ID=?,A.OFFER_ID=?,A.OFFER_NAME=?,A.ACCEPT_DATE=?,A.ACCEPT_CHNL=?,A.ACCEPT_STAFF=?," +
                    "A.DEAL_RESULT=?,A.BUSI_STATE=?,A.FAIL_RESULT=?,A.IS_TRANS=?,A.SATASFI=?,A.REPLY_REMARK=?,A.DEVELOP_STAFF=?,A.MODIFY_DATE=now(),A.SHEET_STATUS=1,A.END_DATE=now() WHERE A.SERVICE_ORDER_ID = ?";
            return this.jt.update(strSql,
                    StringUtils.defaultIfEmpty(buopSheetInfo.getAcceptOrderId(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getOfferId(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getOfferName(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getAcceptDate(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getAcceptChnl(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getAcceptStaff(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getDealResult(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getBusiState(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getFailResult(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getIsTrans(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getSatasfi(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getReplyRemark(), null),
                    StringUtils.defaultIfEmpty(buopSheetInfo.getDevelopStaff(), null),
                    buopSheetInfo.getServiceOrderId()
            );
        }else if("2".equals(buopSheetInfo.getSheetStatus())) {
            strSql = "UPDATE CC_BUOP_SHEET_INFO A SET A.FAIL_RESULT=?,A.MODIFY_DATE=now(),A.SHEET_STATUS=1,A.END_DATE=now() WHERE A.SERVICE_ORDER_ID = ?";
            return this.jt.update(strSql,
            		StringUtils.defaultIfEmpty(buopSheetInfo.getFailResult(), null),
            		buopSheetInfo.getServiceOrderId()
            );
        }
        return 0;
    }

    /**
     * 查询商机单信息
     *
     * @param serviceOrderId 商机单服务流水号
     * @return
     */
    @Override
    public Map<String, Object> selectBuopSheetInfo(String serviceOrderId) {
    	String strSql = "SELECT LATN_ID latnId, SERVICE_ORDER_ID serviceOrderId,SERVICE_ID serviceId, DATE_FORMAT(CREATE_DATE,'%Y-%m-%d %H:%i:%s') createDate, "
    			+ "DATE_FORMAT(MODIFY_DATE,'%Y-%m-%d %H:%i:%s') modifyDate, SHEET_STATUS sheetStatus, CALL_NBR callNbr, "
    			+ "BUSI_NBR busiNbr, PROD_INST_ID prodInstId, CUST_ID custId, BUSI_NAME busiName, HANDLE_LVL handleLvl, CUST_NAME custName, CERT_TYPE certType, CERT_NBR certNbr, "
    			+ "CONTACT_NBR1 contactNbr1, CONTACT_NBR2 contactNbr2, CREATE_REMARK createRemark, INSTALL_ADDRESS installAddress, "
    			+ "POST_ADDRESS postAddress, MOVE_ADDRESS moveAddress, HANDLE_COUNT handleCount, HANDLE_TYPE handleType, "
    			+ "HANDLE_CHNL_ID handleChnlId, ACCEPT_SHEET_ID acceptSheetId, BUSI_TYPE busiType, BUOP_CODE buopCode, IFNULL(ACCEPT_ORDER_ID, '') AS acceptOrderId, "
    			+ "IFNULL(OFFER_ID, '') AS offerId, IFNULL(OFFER_NAME, '') AS offerName, ACCEPT_DATE acceptDate, IFNULL(ACCEPT_CHNL, '') AS acceptChnl, IFNULL(ACCEPT_STAFF, '') AS acceptStaff, "
    			+ "IFNULL(DEAL_RESULT, '') AS dealResult, IFNULL(BUSI_STATE, '') AS busiState, IFNULL(FAIL_RESULT, '') AS failResult, IS_TRANS isTrans, SATASFI satasfi, REPLY_REMARK replyRemark, "
    			+ "IFNULL(DEVELOP_STAFF, '') AS developStaff FROM CC_BUOP_SHEET_INFO WHERE SERVICE_ORDER_ID = ?";
        List<Map<String, Object>> tmpList = jt.queryForList(strSql, serviceOrderId);
        if(tmpList.isEmpty()){
            log.warn("没有查询到服务单号为: {} 的工单", serviceOrderId);
            return null;
        }
        return tmpList.get(0);
    }
}
