package com.timesontransfar.controller;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.customservice.common.QuryDbgridData;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.impl.DepartmentCountImpl;
import com.timesontransfar.customservice.errorSheet.errdbgridData.IerrorSheetGridData;
import com.timesontransfar.customservice.worksheet.dao.IForceDistillDao;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SheetReadRecordInfo;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetDealType;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetStatuApplyInfo;
import com.timesontransfar.staffSkill.dao.IFlowToEndDao;

import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class ItsWorkSheetDaoController {
	protected static Logger log = LoggerFactory.getLogger(ItsWorkSheetDaoController.class);
	
	@Autowired
	private ItsWorkSheetDao tsWorkSheetDao;
	@Autowired
	private IerrorSheetGridData errorSheetGridDataImpl;
	@Autowired
	private QuryDbgridData quryDbgridData;
	@Autowired
	private DepartmentCountImpl departmentCountImpl;
	@Autowired
	private IForceDistillDao forceDistillDao;
    @Autowired
    private IFlowToEndDao flowToEndDao;
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/saveSheetDealType")
	public int saveSheetDealType(@RequestBody TsSheetDealType bean) {
		return tsWorkSheetDao.saveSheetDealType(bean);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/deleteOrderDealType")
	public int deleteOrderDealType(@RequestParam(value="orderId", required=true) String orderId,@RequestParam(value="orderId", required=true) Integer month) {
		return tsWorkSheetDao.deleteOrderDealType(orderId, month);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/deleteDealTypeBySheetid")
	public int deleteDealTypeBySheetid(@RequestParam(value="orderId", required=true) String sheetId) {
		return tsWorkSheetDao.deleteDealTypeBySheetid(sheetId);
	}
	
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/saveSheetDealTypeHis")
	public int saveSheetDealTypeHis(@RequestParam(value="orderId", required=true) String orderId,@RequestParam(value="region", required=true) int region) {
		return tsWorkSheetDao.saveSheetDealTypeHis(orderId, region);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/saveResVisitResult")
	public int saveResVisitResult(@RequestBody RetVisitResult retVisitResult) {
		return tsWorkSheetDao.saveResVisitResult(retVisitResult);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/deleOrderResVisitResult")
	public int deleOrderResVisitResult(@RequestParam(value="orderId", required=true) String orderId,@RequestParam(value="month", required=true) Integer month) {
		return tsWorkSheetDao.deleOrderResVisitResult(orderId, month);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/saveSheetApply")
	public int saveSheetApply(@RequestParam(value="applyInfo", required=true) String applyInfo,@RequestParam(value="boo", required=true)  boolean boo) {
		WorkSheetStatuApplyInfo sheetApply=(WorkSheetStatuApplyInfo) JSONObject.toBean(JSONObject.fromObject(applyInfo),WorkSheetStatuApplyInfo.class);
		return tsWorkSheetDao.saveSheetApply(sheetApply, boo);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/deleteSheetApply")
	public int deleteSheetApply(@RequestParam(value="orderId", required=true)String orderId) {
		return tsWorkSheetDao.deleteSheetApply(orderId);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/getsheetApplyObj")
	public String getsheetApplyObj(@RequestParam(value="condition", required=true) String condition,@RequestParam(value="boo", required=true) boolean boo) {
		WorkSheetStatuApplyInfo[] applyBean = tsWorkSheetDao.getsheetApplyObj(condition, boo);
		if(applyBean.length == 0) {
			return null;
		}
		return JSON.toJSONString(applyBean);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/updateSheetApply")
	public int updateSheetApply(@RequestBody WorkSheetStatuApplyInfo sheetApply) {
		return tsWorkSheetDao.updateSheetApply(sheetApply);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/updateSheetApplyStatu")
	public String updateSheetApplyStatu(
			@RequestParam(value="worksheetId", required=true) String worksheetId, 
			@RequestParam(value="oldStatu", required=true) int oldStatu, 
			@RequestParam(value="newStatu", required=true) int newStatu) {
		return tsWorkSheetDao.updateSheetApplyStatu(worksheetId, oldStatu, newStatu) > 0 ? "SUCCESS" : "FAIL";
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/getTsSheetDealCount")
	public int getTsSheetDealCount(
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="regionId", required=true) int regionId,
			@RequestParam(value="boo", required=true)  boolean boo) {
		return tsWorkSheetDao.getTsSheetDealCount(orderId, regionId, boo);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/getTsSheetDealObj")
	public List getTsSheetDealObj(
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="regionId", required=true) int regionId,
			@RequestParam(value="boo", required=true) boolean boo) {
		return tsWorkSheetDao.getTsSheetDealObj(orderId, regionId, boo);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/getTsEspeciallyCustInfo")
	public List getTsEspeciallyCustInfo(@RequestParam(value="regionId", required=true)  int regionId,@RequestParam(value="custNum", required=true)  String custNum) {
		return tsWorkSheetDao.getTsEspeciallyCustInfo(regionId, custNum);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/getAudQualitative")
	public List getAudQualitative(@RequestParam(value="regionId", required=true) int regionId,@RequestParam(value="custNum", required=true) String custNum) {
		return tsWorkSheetDao.getAudQualitative(regionId, custNum);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/getRelatingSheet")
	public List getRelatingSheet(@RequestParam(value="sheetId", required=true) String sheetId,@RequestParam(value="regionId", required=true) int regionId) {
		return tsWorkSheetDao.getRelatingSheet(sheetId, regionId);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/getOrderDealData")
	public int getOrderDealData(@RequestParam(value="sheetId", required=true)  String orderId) {
		return tsWorkSheetDao.getOrderDealData(orderId);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/getSheetStatuAud")
	public List getSheetStatuAud(@RequestParam(value="sheetId", required=true)String sheetId) {
		return tsWorkSheetDao.getSheetStatuAud(sheetId);
	}
	
	@PostMapping(value = "/workflow/itsWorkSheetDao/intgetSheetStatuAud")
	public int saveSheetReadRecord(@RequestBody SheetReadRecordInfo bean) {
		return tsWorkSheetDao.saveSheetReadRecord(bean);
	}

	//获取错单列表
	@PostMapping(value = "/workflow/itsWorkSheetDao/getErrSheetList")
	public GridDataInfo getErrSheetList(@RequestParam(value="begion", required=true) int begion,@RequestParam(value="strWhere", required=true) String strWhere) {
		return errorSheetGridDataImpl.getErrSheetList(begion,strWhere);
	}
	
	// 本地网流向工单查询(按收单员工查询)
	@PostMapping(value = "/workflow/itsWorkSheetDao/getStaffMonitorNew")
	public GridDataInfo getStaffMonitorNew(@RequestBody String body){
		JSONObject getJson=JSONObject.fromObject(body);
		String areaId=getJson.optString("areaId");
		String staffId=getJson.optString("staffId");
		String begion=getJson.optString("begion");
		StringBuilder sb=new StringBuilder();
		JSONObject strWhere=getJson.optJSONObject("strWhere");
		if(StringUtils.isNotEmpty(strWhere.optString("servType"))){
			sb.append(" AND CC.SERVICE_TYPE = " + strWhere.optString("servType"));
		}
		if(StringUtils.isNotEmpty(strWhere.optString("returnOrg"))){
			sb.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			sb.append(" CONCAT_WS('',( SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + strWhere.optString("returnOrg") + "'),'%')) ");
		}
		return departmentCountImpl.getStaffMonitorNew(Integer.parseInt(begion), areaId, staffId, sb.toString());
	}

	@PostMapping(value = "/workflow/itsWorkSheetDao/getStaffSatisfyMonitorNew")
	public GridDataInfo getStaffSatisfyMonitorNew(@RequestBody String body){
		JSONObject getJson=JSONObject.fromObject(body);
		String areaId=getJson.optString("areaId");
		String staffId=getJson.optString("staffId");
		String begion=getJson.optString("begion");
		StringBuilder sb=new StringBuilder();
		JSONObject strWhere=getJson.optJSONObject("strWhere");
		if(StringUtils.isNotEmpty(strWhere.optString("returnOrg"))){
			sb.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			sb.append(" CONCAT_WS('',( SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + strWhere.optString("returnOrg") + "'),'%')) ");
		}
		return departmentCountImpl.getStaffSatisfyMonitorNew(Integer.parseInt(begion), areaId, staffId, sb.toString());
	}

	// 查询投诉强制释放列表
	@PostMapping(value = "/workflow/itsWorkSheetDao/getForceRelease")
	public GridDataInfo getForceRelease(@RequestBody String ins){
		JSONObject models=JSONObject.fromObject(ins);
		String begion=models.getString("begion");
		String where=quryDbgridData.getForceWhere(ins);
		return quryDbgridData.getForceRelease(Integer.parseInt(begion),where);
	}

	// 查询投诉分派放列表
	@PostMapping(value = "/workflow/itsWorkSheetDao/getWaitDealSheet")
	public GridDataInfo getWaitDealSheet(@RequestBody String ins){
		JSONObject models=JSONObject.fromObject(ins);
		int begion=models.getInt("begion");
		int pageSize=models.getInt("pageSize");
		String where=quryDbgridData.getForceWhere(ins);
		return quryDbgridData.getWaitDealSheet(begion,pageSize,where);
	}

	// 查询投诉分派放列表
	@PostMapping(value = "/workflow/itsWorkSheetDao/getAutoAcceptList")
	public GridDataInfo getAutoAcceptList(@RequestBody String ins){
		JSONObject models=JSONObject.fromObject(ins);
		String begion=models.getString("begion");
		return quryDbgridData.getAutoAcceptList(Integer.parseInt(begion));
	}

	// 业务工单监控箱列表
	@PostMapping(value = "/workflow/itsWorkSheetDao/getForceDistill")
	public GridDataInfo getForceDistill(@RequestBody String ins) {
		JSONObject models = JSONObject.fromObject(ins);
		String regionId = models.getString("regionId");
		String serviceType = models.getString("serviceType");
		String bestOrder = models.getString("bestOrder");
		String hours = models.getString("hours");
		String begin = models.getString("begin");
		return forceDistillDao.selectForceDistill(regionId, serviceType, bestOrder, hours, Integer.parseInt(begin));
	}

	// 一跟到底监控箱列表
	@PostMapping(value = "/workflow/itsWorkSheetDao/getFlowToEndCouldForce")
	public GridDataInfo getFlowToEndCouldForce(@RequestBody String ins) {
		JSONObject models = JSONObject.fromObject(ins);
		String dealOrg = models.getString("dealOrg");
		String serviceType = models.getString("serviceType");
		String hours = models.getString("hours");
		String begin = models.getString("begin");
		return flowToEndDao.selectFlowToEndCouldForce(dealOrg, serviceType, hours, Integer.parseInt(begin), "");
	}

	// 一跟到底豁免人员列表
	@PostMapping(value = "/workflow/itsWorkSheetDao/getExemptionData")
	public GridDataInfo getExemptionData(@RequestBody String ins) {
		JSONObject models = JSONObject.fromObject(ins);
		String createStaffId = models.getString("createStaffId");
		String restStaffId = models.getString("restStaffId");
		String begin = models.getString("begin");
		return flowToEndDao.getExemptionData(createStaffId, restStaffId,Integer.parseInt(begin));
	}

	// 查询配置数据列表
	@PostMapping(value = "/workflow/itsWorkSheetDao/getConfigData")
	public GridDataInfo getConfigData(@RequestBody String ins) {
		JSONObject models = JSONObject.fromObject(ins);
		String createStaffId = models.getString("createStaffId");
		String keyId = models.getString("keyId");
		String keyType = models.getString("keyType");
		String keyRemark = models.getString("keyRemark");
		String begin = models.getString("begin");
		return flowToEndDao.getConfigData(createStaffId, keyId, keyType, keyRemark, Integer.parseInt(begin));
	}

	/**
	 * 新增豁免数据
	 * */
	@PostMapping(value = "/workflow/itsWorkSheetDao/addExemptionData")
	public int addExemptionData(@RequestBody String param) {
		return flowToEndDao.addExemptionData(param);
	}

	/**
	 * 新增配置数据
	 * */
	@PostMapping(value = "/workflow/itsWorkSheetDao/addConfigData")
	public int addConfigData(@RequestBody String param) {
		return flowToEndDao.addConfigData(param);
	}

	/**
	 * 逻辑修改配置数据
	 * */
	@PostMapping(value = "/workflow/itsWorkSheetDao/delConfigData")
	public int delConfigData(@RequestBody String ids) {
		return flowToEndDao.delConfigData(ids);
	}

	/**
	 * 逻辑删除豁免人员数据
	 */
	@PostMapping(value = "/workflow/itsWorkSheetDao/delExemptionData")
	public int delExemptionData(@RequestBody String ids){
		return flowToEndDao.delExemptionData(ids);
	}
}