package com.timesontransfar.customservice.errorSheet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.message.service.IMessageManager;
import com.timesontransfar.customservice.errorSheet.service.IerrSheetDeal;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;

@Component(value="errSheetDeal")
public class ErrSheetDealImpl implements IerrSheetDeal {
	private static final Logger logger = LoggerFactory.getLogger(ErrSheetDealImpl.class);
	
	@Autowired
    private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
    private PubFunc pubFunc;
	@Autowired
    private IMessageManager messagePrompt;
	@Transactional
    public String submitErrSheet(String orderId, String sheetId, int monthFlag, String[] errInfo) {
    	if (null == errInfo || errInfo.length == 0) {
            return "ERROR";
        }
        SheetPubInfo errSheetInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetId, false);
        int count = 0;
        Boolean flag = Boolean.valueOf(errInfo[0]);
        // 是错单
        if (flag.booleanValue()) {
        	count = sheetPubInfoDao.updateErrSheet(orderId, sheetId, "",
        			StaticData.WKST_FINISH_STATE, this.pubFunc.getStaticName(StaticData.WKST_FINISH_STATE), 0);
        	if(count > 0){
        		sheetPubInfoDao.saveErrSheetHis(orderId, sheetId, errSheetInfo.getMonth());
        	}
        } else {
        	// 不是错单，申诉
            count = sheetPubInfoDao.updateErrSheet(orderId, sheetId, errInfo[1],
                    StaticData.WKST_FINISH_STATE, this.pubFunc.getStaticName(StaticData.WKST_FINISH_STATE), 1);
            
            errSheetInfo.setSaveDealContent(errInfo[1]);
        	errSheetInfo.setSheetStatu(StaticData.WKST_APPEAL);//状态为错单确认
            errSheetInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_APPEAL));
            errSheetInfo.setSheetType(StaticData.SHEET_TYPE_ERROR);
            errSheetInfo.setSheetTypeDesc(pubFunc.getStaticName(StaticData.SHEET_TYPE_ERROR));
            errSheetInfo.setSourceSheetId(sheetId);
            String newErr = pubFunc.crtSheetId(errSheetInfo.getRegionId());
            sheetPubInfoDao.saveErrSheet(errSheetInfo,newErr);
        }
        if (count > 0)
            return "SUCCESS";
        else
            return "ERROR";
    }

	@Transactional
    public String submitErrAuditSheet(String orderId, String sheetId, boolean errFlag, String suredMsg) {
    	try {
	    	TsmStaff staff = pubFunc.getLogonStaff();
	        int count = 0;
	        count = sheetPubInfoDao.updateErrSheet(orderId, sheetId, suredMsg, StaticData.WKST_FINISH_STATE,
	        		pubFunc.getStaticName(StaticData.WKST_FINISH_STATE), Integer.valueOf(staff.getId()), staff.getName(),
	        		staff.getOrganizationId(), staff.getOrgName());
	        if(count > 0){
	        	SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
	        	if(!errFlag){
	            	sheetPubInfoDao.updateErrSheetType(orderId, sheetId, StaticData.SHEET_TYPE_NOT_ERROR,
	            			pubFunc.getStaticName(StaticData.SHEET_TYPE_NOT_ERROR));
	            	sheetPubInfoDao.updateErrSheetType(orderId, sheetInfo.getSourceSheetId(), StaticData.SHEET_TYPE_NOT_ERROR,
	            			pubFunc.getStaticName(StaticData.SHEET_TYPE_NOT_ERROR));
	        	}
	        	sheetPubInfoDao.saveErrSheetHis(orderId, sheetInfo.getWorkSheetId(), sheetInfo.getMonth());
	        	sheetPubInfoDao.saveErrSheetHis(orderId, sheetInfo.getSourceSheetId(), sheetInfo.getMonth());
	        }
	        if (count > 0)
	            return "SUCCESS";
	        else
	            return "ERROR";
    	} catch(Exception e) {
    		logger.error("submitErrAuditSheet 异常：{}", e.getMessage(), e);
    	}
    	return "ERROR";
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

    public IMessageManager getMessagePrompt() {
        return messagePrompt;
    }

    public void setMessagePrompt(IMessageManager messagePrompt) {
        this.messagePrompt = messagePrompt;
    }

}
