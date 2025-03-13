package com.timesontransfar.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;

@RestController
@SuppressWarnings("rawtypes")
public class SheetPubInfoDaoController {
	protected Logger log = LoggerFactory.getLogger(SheetPubInfoDaoController.class);
	
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;

	@RequestMapping(value = "/workflow/sheetPubInfoDao/saveErrSheetHis", method = RequestMethod.POST)
	public int saveErrSheetHis(
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="sheetId", required=true) String sheetId, 
			@RequestParam(value="month", required=true) int month) {
		return sheetPubInfoDao.saveErrSheetHis(orderId, sheetId, month);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateErrSheet", method = RequestMethod.POST)
	public int updateErrSheet(
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="sheetId", required=true) String sheetId, 
			@RequestParam(value="errAppeal", required=true) String errAppeal, 
			@RequestParam(value="statu", required=true) int statu,
			@RequestParam(value="statuDesc", required=true) String statuDesc, 
			@RequestParam(value="finishFlag", required=true) int finishFlag) {
		return sheetPubInfoDao.updateErrSheet(orderId, sheetId, errAppeal, statu, statuDesc, finishFlag);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateErrSheetLong", method = RequestMethod.POST)
	public int updateErrSheet(
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="sheetId", required=true) String sheetId, 
			@RequestParam(value="suredMsg", required=false) String suredMsg, 
			@RequestParam(value="statu", required=false) int statu,
			@RequestParam(value="statuDesc", required=false) String statuDesc,
			@RequestParam(value="dealStaff", required=false) Integer dealStaff, 
			@RequestParam(value="dealStaffName", required=false) String dealStaffName, 
			@RequestParam(value="dealOrg", required=false) String dealOrg, 
			@RequestParam(value="dealOrgName", required=false) String dealOrgName) {
		return sheetPubInfoDao.updateErrSheet(orderId, sheetId, suredMsg, statu, statuDesc, dealStaff, dealStaffName, dealOrg, dealOrgName);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateErrSheetType", method = RequestMethod.POST)
	public int updateErrSheetType(
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="sheetId", required=true) String sheetId, 
			@RequestParam(value="type", required=true) int type, 
			@RequestParam(value="typeDesc", required=true) String typeDesc) {
		return sheetPubInfoDao.updateErrSheetType(orderId, sheetId, type, typeDesc);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/getSheetPubInfo", method = RequestMethod.POST)
	public SheetPubInfo getSheetPubInfo(@RequestParam(value="sheetId", required=true) String sheetId, @RequestParam(value="hisFlag", required=true) boolean hisFlag) {
		return sheetPubInfoDao.getSheetPubInfo(sheetId, hisFlag);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/getSheetObj", method = RequestMethod.POST)
	public SheetPubInfo getSheetObj(@RequestParam(value="sheetId", required=true) String sheetId,
			@RequestParam(value="region", required=true) int region, 
			@RequestParam(value="month", required=true) Integer month,
			@RequestParam(value="hisFlag", required=true) boolean hisFlag) {
		return sheetPubInfoDao.getSheetObj(sheetId, region, month, hisFlag);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/getSheetObjNew", method = RequestMethod.POST)
	public SheetPubInfo getSheetObjNew(
			@RequestParam(value="sheetId", required=true)String sheetId,
			@RequestParam(value="region", required=true)int region, 
			@RequestParam(value="month", required=true)Integer month,
			@RequestParam(value="suffix", required=true)String suffix,
			@RequestParam(value="hisFlag", required=true)boolean hisFlag) {
		return sheetPubInfoDao.getSheetObjNew(sheetId, region, month, suffix, hisFlag);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/saveSheetPubInfo", method = RequestMethod.POST)
	public int saveSheetPubInfo(@RequestBody SheetPubInfo sheetPubInfo) {
		return sheetPubInfoDao.saveSheetPubInfo(sheetPubInfo);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/saveSheetPubInfoHis", method = RequestMethod.POST)
	public int saveSheetPubInfoHis(@RequestParam(value="suffix", required=true) String orderId,
			@RequestParam(value="suffix", required=true) Integer month) {
		return sheetPubInfoDao.saveSheetPubInfoHis(orderId, month);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/saveErrSheet", method = RequestMethod.POST)
	public int saveErrSheet(@RequestBody SheetPubInfo sheetPubInfo ,@RequestParam(value="suffix", required=true)String newSheetId) {
		return sheetPubInfoDao.saveErrSheet(sheetPubInfo,newSheetId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateSheetStateLong", method = RequestMethod.POST)
	public boolean updateSheetState(
			@RequestParam(value="sheetId", required=true) String sheetId, 
			@RequestParam(value="state", required=true) int state, 
			@RequestParam(value="stateDesc", required=true) String stateDesc,
			@RequestParam(value="month", required=true) Integer month,
			@RequestParam(value="lookFlag", required=true) int lookFlag) {
		return sheetPubInfoDao.updateSheetState(sheetId, state, stateDesc,month,lookFlag);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateSheetStateByOrder", method = RequestMethod.POST)
	public boolean updateSheetStateByOrder(
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="state", required=true) int state, 
			@RequestParam(value="stateDesc", required=true) String stateDesc,
			@RequestParam(value="lookFlag", required=true) int lookFlag,
			@RequestParam(value="month", required=true) Integer month) {
		return sheetPubInfoDao.updateSheetStateByOrder(orderId, state, stateDesc, lookFlag, month);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/getRegSheetState", method = RequestMethod.POST)
	public int getRegSheetState(@RequestParam(value="sheetId", required=true)String sheetId,@RequestParam(value="sheetId", required=true)int region) {
		return sheetPubInfoDao.getRegSheetState(sheetId, region);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateFetchSheetStaff", method = RequestMethod.POST)
	public int updateFetchSheetStaff(
			@RequestParam(value="sheetId", required=true)String sheetId, 
			@RequestParam(value="staffId", required=true)int staffId,
			@RequestParam(value="staffName", required=true)String staffName, 
			@RequestParam(value="orgId", required=true)String orgId, 
			@RequestParam(value="orgName", required=true)String orgName) {
		return sheetPubInfoDao.updateFetchSheetStaff(sheetId, staffId, staffName, orgId, orgName);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/delSheetPubInfoByOrderId", method = RequestMethod.POST)
	public int delSheetPubInfoByOrderId(
			@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="month", required=true)Integer month) {
		return sheetPubInfoDao.delSheetPubInfoByOrderId(orderId,month);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/getTheLastSheetInfo", method = RequestMethod.POST)
	public SheetPubInfo getTheLastSheetInfo(@RequestParam(value="orderId", required=true)String orderId) {
		return sheetPubInfoDao.getTheLastSheetInfo(orderId);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/getCurrentTacheInstId", method = RequestMethod.POST)
	public String getCurrentTacheInstId(@RequestParam(value="wfNodeInstId", required=true)String wfNodeInstId) {
		return sheetPubInfoDao.getCurrentTacheInstId(wfNodeInstId);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateSheetDealRequire", method = RequestMethod.POST)
	public int updateSheetDealRequire(
			@RequestParam(value="sheetId", required=true)String sheetId, 
			@RequestParam(value="require", required=true)String require,
			@RequestParam(value="regcOrgList", required=true)String regcOrgList,
			@RequestParam(value="dealType", required=true)String dealType,
			@RequestParam(value="dealContent", required=true)String dealContent,
			@RequestParam(value="dealId", required=true)int dealId,
			@RequestParam(value="nextTache", required=true)int nextTache) {
		return sheetPubInfoDao.updateSheetDealRequire(sheetId, require, regcOrgList, dealType, dealContent, dealId, nextTache);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateDealContent", method = RequestMethod.POST)
	public String updateDealContent(@RequestParam(value="sheetId", required=true)String sheetId,@RequestParam(value="dealContent", required=true) String dealContent) {
		return sheetPubInfoDao.updateDealContent(sheetId, dealContent) > 0 ? "SUCCESS" : "FAIL";
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateSheetFinishDate", method = RequestMethod.POST)
	public int updateSheetFinishDate(@RequestParam(value="sheetId", required=true)String sheetId) {
		return sheetPubInfoDao.updateSheetFinishDate(sheetId);
	}	

	@RequestMapping(value = "/workflow/sheetPubInfoDao/saveDealContent", method = RequestMethod.POST)
	public int saveDealContent(@RequestParam(value="sheetId", required=true) String sheetId,
			@RequestParam(value="regionId", required=true)int regionId,
			@RequestParam(value="content", required=true) String content) {
		return sheetPubInfoDao.saveDealContent(sheetId, regionId, content);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/getWorksheetFlow", method = RequestMethod.POST)
	public SheetPubInfo[] getWorksheetFlow(@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="month", required=true)Integer month,
			@RequestParam(value="boo", required=true)boolean boo) {
		return sheetPubInfoDao.getWorksheetFlow(orderId, month, boo);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/getRelatSheet", method = RequestMethod.POST)
	public SheetPubInfo[] getRelatSheet(@RequestParam(value="sheetId", required=true) String sheetId,
			@RequestParam(value="month", required=true) Integer month) {
		return sheetPubInfoDao.getRelatSheet(sheetId, month);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateTachSheetFinsh", method = RequestMethod.POST)
	public int updateTachSheetFinsh(
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="state", required=true) int state,
			@RequestParam(value="stateDesc", required=true) String stateDesc,
			@RequestParam(value="lookFlag", required=true) int lookFlag,
			@RequestParam(value="month", required=true) Integer month,
			@RequestParam(value="tachId", required=true) int tachId) {
		return sheetPubInfoDao.updateTachSheetFinsh(orderId, state, stateDesc, lookFlag, month, tachId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateDealDisannuul", method = RequestMethod.POST)
	public int updateDealDisannuul(
			@RequestParam(value="state", required=true)int state,
			@RequestParam(value="stateDesc", required=true)String stateDesc,
			@RequestParam(value="month", required=true)Integer month,
			@RequestParam(value="tachId", required=true)int tachId,
			@RequestParam(value="where", required=true)String where) {
		return sheetPubInfoDao.updateDealDisannuul(state, stateDesc, month, tachId, where);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/getSheetCondition", method = RequestMethod.POST)
	public List getSheetCondition(@RequestParam(value="strWhere", required=true)String strWhere,
			@RequestParam(value="boo", required=true)boolean boo) {
		return sheetPubInfoDao.getSheetCondition(strWhere, boo);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/checkStaffSheet", method = RequestMethod.POST)
	public int checkStaffSheet(@RequestParam(value="strWhere", required=true)String strWhere) {
		return sheetPubInfoDao.checkStaffSheet(strWhere);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/getSheetNumOfSendTache", method = RequestMethod.POST)
	public int getSheetNumOfSendTache(@RequestParam(value="strWhere", required=true)String strWhere) {
		return sheetPubInfoDao.getSheetNumOfSendTache(strWhere);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateTotalHold", method = RequestMethod.POST)
	public int updateTotalHold(
			@RequestParam(value="angupStrTime", required=true)String angupStrTime, 
			@RequestParam(value="sheetTotalHoldTime", required=true)int sheetTotalHoldTime, 
			@RequestParam(value="region", required=true)int region, 
			@RequestParam(value="sheetId", required=true)String sheetId) {
		return sheetPubInfoDao.updateTotalHold(angupStrTime, sheetTotalHoldTime, region, sheetId);
	}
	
	@RequestMapping(value = "/workflow/sheetPubInfoDao/getFlowSeq", method = RequestMethod.POST)
	public int getFlowSeq(
			@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="regionId", required=true)int regionId) {
		return sheetPubInfoDao.getFlowSeq(orderId,regionId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/getLatestSheetByType", method = RequestMethod.POST)
	public SheetPubInfo getLatestSheetByType(
			@RequestParam(value="orderId", required=true)String orderId, 
			@RequestParam(value="sheetType", required=true)int sheetType, 
			@RequestParam(value="mainFlag", required=true)int mainFlag) {
		return sheetPubInfoDao.getLatestSheetByType(orderId, sheetType, mainFlag);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateRegion", method = RequestMethod.POST)
	public int updateRegion(
			@RequestParam(value="serviceOrderId", required=true)String serviceOrderId, 
			@RequestParam(value="newRegion", required=true)int newRegion, 
			@RequestParam(value="newRegionName", required=true)String newRegionName, 
			@RequestParam(value="oldRegion", required=true)int oldRegion, 
			@RequestParam(value="month", required=true)Integer month) {
		return sheetPubInfoDao.updateRegion(serviceOrderId, newRegion, newRegionName, oldRegion, month);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/insertWorkSheetArea", method = RequestMethod.POST)
	public int insertWorkSheetArea(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId,
			@RequestParam(value = "workSheetId", required = true) String workSheetId,
			@RequestParam(value = "receiveAreaOrgId", required = true) String receiveAreaOrgId,
			@RequestParam(value = "areaFlag", required = true) int areaFlag) {
		return sheetPubInfoDao.insertWorkSheetArea(serviceOrderId, workSheetId, receiveAreaOrgId, areaFlag);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/deleteWorkSheetAreaByOrderId", method = RequestMethod.POST)
	public int deleteWorkSheetAreaByOrderId(
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return sheetPubInfoDao.deleteWorkSheetAreaByOrderId(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/deleteWorkSheetAreaBySheetId", method = RequestMethod.POST)
	public int deleteWorkSheetAreaBySheetId(@RequestParam(value = "workSheetId", required = true) String workSheetId) {
		return sheetPubInfoDao.deleteWorkSheetAreaBySheetId(workSheetId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateWorkSheetAreaTacheDate", method = RequestMethod.POST)
	public int updateWorkSheetAreaTacheDate(
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return sheetPubInfoDao.updateWorkSheetAreaTacheDate(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateWorkSheetAreaSheetBySheetId", method = RequestMethod.POST)
	public int updateWorkSheetAreaSheetBySheetId(@RequestParam(value = "newSheetId", required = true) String newSheetId,
			@RequestParam(value = "receiveAreaOrgId", required = true) String receiveAreaOrgId,
			@RequestParam(value = "areaFlag", required = true) int areaFlag,
			@RequestParam(value = "oldSheetId", required = true) String oldSheetId) {
		return sheetPubInfoDao.updateWorkSheetAreaSheetBySheetId(newSheetId, receiveAreaOrgId, areaFlag, oldSheetId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/countWorkSheetAreaByOrderId", method = RequestMethod.POST)
	public int countWorkSheetAreaByOrderId(
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId,
			@RequestParam(value = "areaFlag", required = true) int areaFlag) {
		return sheetPubInfoDao.countWorkSheetAreaByOrderId(serviceOrderId, areaFlag);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/selectWorkSheetAreaBySheetId", method = RequestMethod.POST)
	public int selectWorkSheetAreaBySheetId(@RequestParam(value = "workSheetId", required = true) String workSheetId) {
		return sheetPubInfoDao.selectWorkSheetAreaBySheetId(workSheetId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/selectLastWorkSheetIdByOrderId", method = RequestMethod.POST)
	public String selectLastWorkSheetIdByOrderId(
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return sheetPubInfoDao.selectLastWorkSheetIdByOrderId(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/deleteWorkSheetBySheetId", method = RequestMethod.POST)
	public int deleteWorkSheetBySheetId(@RequestParam(value = "workSheetId", required = true) String workSheetId) {
		return sheetPubInfoDao.deleteWorkSheetBySheetId(workSheetId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/selectSheetReceiveDate", method = RequestMethod.POST)
	public String selectSheetReceiveDate(@RequestParam(value = "workSheetId", required = true) String workSheetId) {
		return sheetPubInfoDao.selectSheetReceiveDate(workSheetId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateDealLimitTimeByOrderId", method = RequestMethod.POST)
	public int updateDealLimitTimeByOrderId(@RequestParam(value = "dealLimitTime", required = true) int dealLimitTime,
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return sheetPubInfoDao.updateDealLimitTimeByOrderId(dealLimitTime, serviceOrderId);
	}

	@RequestMapping(value = "/workflow/sheetPubInfoDao/updateAuditLimitTimeByOrderId", method = RequestMethod.POST)
	public int updateAuditLimitTimeByOrderId(
			@RequestParam(value = "auditLimitTime", required = true) int auditLimitTime,
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return sheetPubInfoDao.updateAuditLimitTimeByOrderId(auditLimitTime, serviceOrderId);
	}
}