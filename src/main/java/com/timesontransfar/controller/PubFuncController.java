package com.timesontransfar.controller;

import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.tuschema.dao.IserviceContentTypeDao;
import com.timesontransfar.evaluation.dao.EvaluationDao;
import com.transfar.common.web.ResultUtil;
import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.google.gson.Gson;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceAcceptDir;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class PubFuncController {
	protected Logger log = LoggerFactory.getLogger(PubFuncController.class);
	
	@Autowired
	private PubFunc pubFunc;

	@Autowired
	private RedisUtils redisUtils;
	
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	
	@Autowired
	private IserviceContentDao serviceContentDao;

	@Autowired
	private EvaluationDao dao;

	@Autowired
	private IserviceContentTypeDao iserviceContentTypeDao;
	
	@PostMapping(value = "/workflow/pubFunc/getIntfMap")
	public Map getIntfMap(String intfName) {
		return pubFunc.getIntfMap(intfName);
	}

	@PostMapping(value = "/workflow/pubFunc/getExtendTsDir")
	public List getExtendTsDir(@RequestParam(value = "dirId", required = true) String dirId,
			@RequestParam(value = "flag", required = true) String flag) {
		return pubFunc.getExtendTsDir(dirId, flag);
	}

	@PostMapping(value = "/workflow/pubFunc/getProdNumHis")
	public List getProdNumHis(@RequestParam(value = "regionId", required = true) String regionId,
			@RequestParam(value = "prodNum", required = true) String prodNum,
			@RequestParam(value = "lastXX", required = true) String lastXX,
			@RequestParam(value = "relaInfo", required = true) String relaInfo,
			@RequestParam(value = "serviceType", required = true) String serviceType,
			@RequestParam(value = "acceptChannelId", required = true) String acceptChannelId,
			@RequestParam(value = "appealReasonId", required = true) String appealReasonId,
			@RequestParam(value = "emergency", required = true) String emergency) {
		// 重复工单848453
		// 单号：854988受理渠道为：省服务监督热线；受理信息里面“是否越级”字段：选择有越级倾向或有曝光倾向。受理目录为携号转网的；
		boolean cfFlag = true;
		if (!"720130000".equals(serviceType)) { // 非投诉
			cfFlag = false;
		}
		if (cfFlag && ("707907012".equals(acceptChannelId))) { // 省服务监督热线
			cfFlag = false;
		}
		if (cfFlag && checkXHZW(appealReasonId)) { // 携号转网
			cfFlag = false;
		}
		if (cfFlag && ("1".equals(emergency) || "2".equals(emergency) || "3".equals(emergency))) { // 有曝光倾向||有工信部越级倾向或通管局投诉倾向||有越级倾向
			cfFlag = false;
		}
		if (cfFlag) {
			List list = pubFunc.checkRepeatUnified(regionId, prodNum, relaInfo);
			if (!list.isEmpty()) {
				return list;
			}
		}
		return pubFunc.getProdNumHis(regionId, prodNum, lastXX);
	}

	// 判断是否为携号转网
	private boolean checkXHZW(String appealReasonId) {
		return appealReasonId.startsWith("111") || "23002503".equals(appealReasonId);
	}

	@PostMapping(value = "/workflow/pubFunc/getPlusOneList")
	public List getPlusOneList(String lastYY) {
		return pubFunc.getPlusOneList(lastYY);
	}

	@PostMapping(value = "/workflow/pubFunc/getPlusTwoList")
	public List getPlusTwoList(@RequestParam(value = "lastYY", required = true) String lastYY,
			@RequestParam(value = "plusOne", required = true) String plusOne) {
		return pubFunc.getPlusTwoList(lastYY, plusOne);
	}

	@PostMapping(value = "/workflow/pubFunc/getLastDealContentHis")
	public String getLastDealContentHis(@RequestParam(value="orderId", required=true) String orderId) {
		return pubFunc.getLastDealContentHis(orderId);
	}

	@PostMapping(value = "/workflow/pubFunc/saveQualitativeChooseLog")
	public Integer saveQualitativeChooseLog(
			@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId,
			@RequestParam(value = "workSheetId", required = true) String workSheetId,
			@RequestParam(value = "chooseFlag", required = true) String chooseFlag,
			@RequestParam(value = "chooseMs", required = true) String chooseMs,
			@RequestParam(value = "lastYY", required = true) String lastYY,
			@RequestParam(value = "tacheDesc", required = true) String tacheDesc) {
		return pubFunc.saveQualitativeChooseLog(serviceOrderId, workSheetId, chooseFlag, chooseMs, lastYY, tacheDesc);
	}

	@PostMapping(value = "/workflow/pubFunc/getLastDealContent")
	public String getLastDealContent(@RequestParam(value="orderId", required=true) String orderId) {
		return pubFunc.getLastDealContent(orderId);
	}

	@PostMapping(value = "/workflow/pubFunc/getLastDealInfo")
	public Map getLastDealInfo(@RequestParam(value="orderId", required=true) String orderId) {
		return pubFunc.getLastDealInfo(orderId);
	}
	@PostMapping(value = "/workflow/pubFunc/getLastDealInfoHis")
	public Map getLastDealInfoHis(@RequestParam(value="orderId", required=true) String orderId) {
		return pubFunc.getLastDealInfoHis(orderId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getTicket")
	public String getTicket(@RequestParam(value="areaCode", required=true) String areaCode,@RequestParam(value="callNo", required=true) String callNo) {
		return pubFunc.getTicket(areaCode,callNo);
	}
	
	@PostMapping(value = "/workflow/pubFunc/saveOperationLog")
	public Integer saveOperationLog(
			@RequestParam(value="opAction", required=true) String opAction, 
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="opMenu", required=true) String opMenu,
			@RequestParam(value="prodNum", required=true) String prodNum,
			@RequestParam(value="ip", required=true) String ip) {
		return pubFunc.saveOperationLog(opAction,orderId,opMenu,prodNum,ip);
	}
	
	@PostMapping(value = "/workflow/pubFunc/saveOperationLogByLn")
	public Integer saveOperationLogByLn(
			@RequestParam(value="opAction", required=true) String opAction, 
			@RequestParam(value="orderId", required=true) String orderId, 
			@RequestParam(value="opMenu", required=true) String opMenu,
			@RequestParam(value="prodNum", required=true) String prodNum,
			@RequestParam(value="logonName", required=true) String logonName) {
		return pubFunc.saveOperationLogByLn(opAction,orderId,opMenu,prodNum,logonName);
	}
		
	@GetMapping(value = "/workflow/pubFunc/jscpSSO")
	public String jscpSSO() {
		return pubFunc.jscpSSO();
	}

	@GetMapping(value = "/workflow/pubFunc/judgeOrgBelongSTS")
	public int judgeOrgBelongSTS(){
		return pubFunc.judgeOrgBelongSTS();
	}

	@GetMapping(value = "/workflow/pubFunc/getSysDateFormat")
	public String getSysDateFormat(@RequestParam(value="format", required=true) String format){
		return pubFunc.getSysDateFormat(format);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getBetweenSysDateSec")
	public Integer getBetweenSysDateSec(@RequestParam(value="startDate", required=true) String startDate){
		return pubFunc.getBetweenSysDateSec(startDate);
	}

	@PostMapping(value = "/workflow/pubFunc/loadColumnsByEntity")
	public List loadColumnsByEntity(
			@RequestParam(value="tableCode", required=true) String tableCode, 
			@RequestParam(value="colCode", required=true) String colCode,
			@RequestParam(value="entityId", required=false) String entityId) {
		return pubFunc.loadColumnsByEntity(tableCode,colCode,entityId);
	}

	@PostMapping(value = "/workflow/pubFunc/getAreaTmDaily")
	public String getAreaTmDaily() {
		return pubFunc.getAreaTmDaily();
	}

	/**
	 * 根据regionId查询区县
	 * */
	@PostMapping(value = "/workflow/pubFunc/getAreaTmDailyByRegionId")
	public List<Map<String, Object>> getAreaTmDailyByRegionId(@RequestParam(value="regionId", required=true) String regionId) {
		return pubFunc.getAreaTmDailyByRegionId(regionId);
	}

	@PostMapping(value = "/workflow/pubFunc/loadQdxx")
	public List loadQdxx(@RequestParam(value="refer_id", required=true) String referId){
		return pubFunc.loadQdxx(referId);
	}
	

	@PostMapping(value = "/workflow/pubFunc/loadColumnsByEntityNew")
	public JSONArray loadColumnsByEntityNew(
			@RequestParam(value="tableCode", required=true) String tableCode, 
			@RequestParam(value="colCode", required=true) String colCode,
			@RequestParam(value="entityId", required=false) String entityId) {
		List array= pubFunc.loadColumnsByEntity(tableCode,colCode,entityId);
		return JSONArray.fromObject(array);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getJsonTree")
	public String getJsonTree(
			@RequestParam(value="id", required=true) String id) {
		return pubFunc.getJsonTree(id);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getLinkIdByLoginName")
	public String getLinkIdByLoginName(@RequestParam(value="loginName", required=true) String loginName) {
		return pubFunc.getLinkIdByLoginName(loginName);
	}
	

	@PostMapping(value = "/workflow/pubFunc/getThreeDir")
	public List getThreeDir(@RequestParam(value="entityId", required=true) String entityId,
			@RequestParam(value="flag", required=true) boolean flag) {
		return pubFunc.getThreeDir(entityId,flag);
	}
	

	@PostMapping(value = "/workflow/pubFunc/getFourDir")
	public List getFourDir(@RequestParam(value="entityId", required=true) String entityId,
			@RequestParam(value="flag", required=true) String table) {
		return pubFunc.getFourDir(entityId,table);
	}
	

	@PostMapping(value = "/workflow/pubFunc/getFiveDir")
	public List getFiveDir(@RequestParam(value="entityId", required=true) String entityId,
			@RequestParam(value="flag", required=true) String table) {
		return pubFunc.getFiveDir(entityId,table);
	}
	

	@PostMapping(value = "/workflow/pubFunc/getSixDir")
	public List getSixDir(@RequestParam(value="entityId", required=true) String entityId,
			@RequestParam(value="table", required=true) String table) {
		return pubFunc.getSixDir(entityId,table);
	}
	

	@PostMapping(value = "/workflow/pubFunc/getDir")
	public List getDir(@RequestParam(value="tableName", required=true) String tableName,
			@RequestParam(value="entityId", required=false) String entityId,
			@RequestParam(value="refid", required=false) String refid) {
		return pubFunc.getDir(tableName,entityId,refid);
	}
	
	@GetMapping(value = "/workflow/pubFunc/crtGuid")
	public String crtGuid(){
		return pubFunc.crtGuid();
	}
	
	@GetMapping(value = "/workflow/pubFunc/isNewWorkFlow")
	public boolean isNewWorkFlow(@RequestParam(value = "orderId", required = true) String orderId) {
		return pubFunc.isNewWorkFlow(orderId);
	}

	@GetMapping(value = "/workflow/pubFunc/getAreaOrgId")
	public String getAreaOrgId(@RequestParam(value = "orgId", required = true) String orgId) {
		return pubFunc.getAreaOrgId(orgId);
	}

	@GetMapping(value = "/workflow/pubFunc/getAreaOrgIdByStaff")
	public String getAreaOrgIdByStaff(@RequestParam(value = "staffId", required = true) String staffId) {
		return pubFunc.getAreaOrgIdByStaff(staffId);
	}

	@PostMapping(value = "/workflow/pubFunc/checkUnified")
	public int checkUnified(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return pubFunc.checkUnified(serviceOrderId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/checkTsUnified")
	public int checkTsUnified(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return pubFunc.checkTsUnified(serviceOrderId);
	}

	@GetMapping(value = "/workflow/pubFunc/checkPDTime")
	public int checkPDTime(@RequestParam(value = "workSheetId", required = true) String workSheetId) {
		return pubFunc.checkPDTime(workSheetId);
	}

	@GetMapping(value = "/workflow/pubFunc/checkRecall")
	public int checkRecall(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		return pubFunc.checkRecall(serviceOrderId);
	}

	@PostMapping(value = "/workflow/pubFunc/queryAcceptDir", produces = "application/json;charset=utf-8")
	public List queryAcceptDir(@RequestParam(value = "n_id", required = true) int nId,
			@RequestParam(value = "vNum", required = true) int vNum) {
		return pubFunc.queryAcceptDir(nId, vNum);
	}

	@PostMapping(value = "/workflow/pubFunc/queryAcceptDirYs")
	public List queryAcceptDirYs(@RequestParam(value="entityId", required=true) String entityId,@RequestParam(value="colValueName", required=true) String colValueName) {
		return pubFunc.queryAcceptDirYs(entityId,colValueName);
	}

	@PostMapping(value = "/workflow/pubFunc/findAcceptDir")
	public ServiceAcceptDir[] findAcceptDir(@RequestParam(value = "queryChar", required = true) String queryChar) {
		return pubFunc.findAcceptDir(queryChar);
	}

	@PostMapping(value = "/workflow/pubFunc/findBanjieDir")
	public ServiceAcceptDir[] findBanjieDir(@RequestParam(value="queryChar", required=true) String queryChar){
		return pubFunc.findBanjieDir(queryChar);
	}

	@GetMapping(value = "/workflow/pubFunc/getBanjieDir")
	public List getBanjieDir(@RequestParam(value = "refid", required = true) String refid,
			@RequestParam(value = "vNum", required = true) int vNum) {
		return pubFunc.getBanjieDir(refid, vNum);
	}

	@GetMapping(value = "/workflow/pubFunc/getIntMonth")
	public Integer getIntMonth(){
		return pubFunc.getIntMonth();
	}
	
	@GetMapping(value = "/workflow/pubFunc/getLogonStaff")
	public TsmStaff getLogonStaff(){
		return pubFunc.getLogonStaff();
	}
	

	@PostMapping(value = "/workflow/pubFunc/getCallStaffInfo")
	public List getCallStaffInfo() {
		return pubFunc.getCallStaffInfo();
	}
	
	@PostMapping(value = "/workflow/pubFunc/getLogonStaffByLoginName")
	public TsmStaff getLogonStaffByLoginName(@RequestParam(value="loginName", required=true) String loginName){
		return pubFunc.getLogonStaffByLoginName(loginName);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getAvlStaffByLoginName")
	public TsmStaff getAvlStaffByLoginName(@RequestParam(value="loginName", required=true) String loginName){
		return  pubFunc.getAvlStaffByLoginName(loginName);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getStaff")
	public JSONObject getStaff(@RequestParam(value="staffId", required=true) int staffId){
		TsmStaff staff = pubFunc.getStaff(staffId);
		return JSONObject.fromObject(staff);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getStaffById", produces = "application/json;charset=utf-8")
	public TsmStaff getStaffById(@RequestParam(value="staffId", required=true) int staffId){
		return pubFunc.getStaff(staffId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getStaffName")
	public String getStaffName(@RequestParam(value="staffId", required=true) int staffId){
		return pubFunc.getStaffName(staffId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getStaffLongName")
	public String getStaffLongName(@RequestParam(value="staffId", required=true) int staffId){
		return pubFunc.getStaffLongName(staffId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getStaffOrgName")
	public String getStaffOrgName(@RequestParam(value="staffId", required=true) int staffId){
		return pubFunc.getStaffOrgName(staffId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getStaffId")
	public Integer getStaffId(@RequestParam(value="logonName", required=true) String logonName){
		return pubFunc.getStaffId(logonName);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getOrgName")
	public String getOrgName(@RequestParam(value="orgId", required=true) String orgId){
		return pubFunc.getOrgName(orgId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getSecOrgName")
	public String getSecOrgName(@RequestParam(value="orgId", required=true) String orgId){
		return pubFunc.getSecOrgName(orgId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getOrgRegion")
	public Integer getOrgRegion(@RequestParam(value="orgId", required=true) String orgId){
		return pubFunc.getOrgRegion(orgId);
	}
	

	@GetMapping(value = "/workflow/pubFunc/getLantInfoByStaffId")
	public Map getLantInfoByStaffId(@RequestParam(value="staffId", required=true) int staffId){
		return pubFunc.getLantInfoByStaffId(staffId);
	}
	

	@GetMapping(value = "/workflow/pubFunc/getLantInfoByOrgId")
	public Map getLantInfoByOrgId(@RequestParam(value="orgId", required=true) String orgId){
		return pubFunc.getLantInfoByOrgId(orgId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getOrgLink")
	public String getOrgLink(@RequestParam(value="orgId", required=true) String orgId){
		return pubFunc.getOrgLink(orgId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getStaticName")
	public String getStaticName(@RequestParam(value="referId", required=true) long referId){
		return pubFunc.getStaticName(referId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getSuperStaticId")
	public String getSuperStaticId(@RequestParam(value="referId", required=true) long referId){
		return pubFunc.getSuperStaticId(referId);
	}
	
	@SuppressWarnings("static-access")
	@GetMapping(value = "/workflow/pubFunc/getTimeBetween")
	public Long getTimeBetween(@RequestParam(value="beginTime", required=true) String beginTime,
			@RequestParam(value="endTime", required=true) String endTime,
			@RequestParam(value="timeBetweenFlag", required=true) int timeBetweenFlag){
		return pubFunc.getTimeBetween(beginTime,endTime,timeBetweenFlag);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getSysDate")
	public String getSysDate(){
		return pubFunc.getSysDate();
	}
	

	@GetMapping(value = "/workflow/pubFunc/addDate")
	public String addDate(@RequestParam(value="strDate", required=true) String strDate,
			@RequestParam(value="strDate", required=true) Map map){
		return pubFunc.addDate(strDate,map);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getRegionTelNo")
	public String getRegionTelNo(@RequestParam(value="regionId", required=true) int regionId){
		return pubFunc.getRegionTelNo(regionId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getRegionId")
	public String getRegionId(@RequestParam(value="regionTelNo", required=true) String regionTelNo){
		return pubFunc.getRegionId(regionTelNo);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getUpRegionId")
	public Integer getUpRegionId(@RequestParam(value="regionId", required=true) int regionId){
		return pubFunc.getUpRegionId(regionId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getRegionLevel")
	public String getRegionLevel(@RequestParam(value="regionId", required=true) int regionId){
		return pubFunc.getRegionLevel(regionId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getRegionName")
	public String getRegionName(@RequestParam(value="regionId", required=true) int regionId){
		return pubFunc.getRegionName(regionId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getRegionLinkId")
	public String getRegionLinkId(@RequestParam(value="regionId", required=true) int regionId){
		return pubFunc.getRegionLinkId(regionId);
	}
	
	@GetMapping(value = "/workflow/pubFunc/getCrmrefId")
	public Integer getCrmrefId(@RequestParam(value="table", required=true) String table,
			@RequestParam(value="colcode", required=true) String colcode,
			@RequestParam(value="entiyId", required=true) String entiyId){
		return pubFunc.getCrmrefId(table,colcode,entiyId);
	}

	@SuppressWarnings("static-access")
	@PostMapping(value = "/workflow/pubFunc/getSheetTypeByTacheId")
	public int[] getSheetTypeByTacheId(@RequestParam(value="tacheId", required=true) int tacheId) {
		return pubFunc.getSheetTypeByTacheId(tacheId);
	}
	
	@SuppressWarnings("static-access")
	@PostMapping(value = "/workflow/pubFunc/getSheetType")
	public int getSheetType(@RequestParam(value="servType", required=true) int servType,@RequestParam(value="tacheId", required=true) int tacheId) {
		return pubFunc.getSheetType(servType,tacheId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getSheetStatu")
	public int getSheetStatu(@RequestParam(value="tachId", required=true) int tachId,
			@RequestParam(value="lockFlag", required=true) int lockFlag,
			@RequestParam(value="sheetType", required=true) int sheetType) {
		return pubFunc.getSheetStatu(tachId,lockFlag,sheetType);
	}
	
	@PostMapping(value = "/workflow/pubFunc/isAffiliated")
	public boolean isAffiliated(@RequestParam(value="lowerOrgId", required=true) String lowerOrgId,
			@RequestParam(value="upperOrgId", required=true) String upperOrgId) {
		return pubFunc.isAffiliated(lowerOrgId,upperOrgId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getSendCount")
	public int getSendCount(@RequestParam(value="serverOrderId", required=true) String serverOrderId,
			@RequestParam(value="receiveOrgId", required=true) String receiveOrgId) {
		return pubFunc.getSendCount(serverOrderId,receiveOrgId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/updateOrderAndSheetHang")
	public int updateOrderAndSheetHang(@RequestParam(value="workSheetId", required=true) String workSheetId,
			@RequestParam(value="serviceOrderId", required=true) String serviceOrderId) {
		return pubFunc.updateOrderAndSheetHang(workSheetId,serviceOrderId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/updateOrderHang")
	public int updateOrderHang(@RequestParam(value="workSheetId", required=true) String workSheetId,
			@RequestParam(value="serviceOrderId", required=true) String serviceOrderId) {
		return pubFunc.updateOrderHang(workSheetId,serviceOrderId);
	}
	

	@PostMapping(value = "/workflow/pubFunc/getAllSecDuty")
	public List getAllSecDuty(){
		return pubFunc.getAllSecDuty();
	}
	

	@PostMapping(value = "/workflow/pubFunc/getSecDutyExceptSelf")
	public JSONArray getSecDutyExceptSelf(@RequestParam(value="orgid", required=true) String orgid){
		List list= pubFunc.getSecDutyExceptSelf(orgid);
		return JSONArray.fromObject(list);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getPersonalityJudge")
	public int getPersonalityJudge(@RequestParam(value="judgeId", required=true) String judgeId,
			@RequestParam(value="flag", required=true) String param) {
		return pubFunc.getPersonalityJudge(judgeId,param);
	}
	
	@PostMapping(value = "/workflow/pubFunc/querySysContolSwitch")
	public String querySysContolSwitch(@RequestParam(value="switchId", required=true) String switchId) {
		return pubFunc.querySysContolSwitchNew(switchId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/queryJtFtpStaff")
	public String queryJtFtpStaff() {
		return pubFunc.queryJtFtpStaff();
	}
	
	@PostMapping(value = "/workflow/pubFunc/updateJtFtpStaff")
	public int updateJtFtpStaff(@RequestParam(value="txtData", required=true) String txtData) {
		return pubFunc.updateJtFtpStaff(txtData);
	}
	@PostMapping(value = "/workflow/pubFunc/cmpRelation")
	public ComplaintRelation cmpRelation(@RequestParam(value="serviceOrderId", required=true) String serviceOrderId) {
		return pubFunc.queryListByOid(serviceOrderId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/batchDelRedis")
	public long batchDelRedis(@RequestParam(value="key", required=true) String key){
		return redisUtils.batchDelByPreKey(key, RedisType.WORKSHEET);
	}
	
	@PostMapping(value = "/workflow/pubFunc/setRedis")
	public String setRedis(@RequestParam(value="key", required=false) String key, 
			@RequestParam(value="value", required=false) String value){
		return redisUtils.set(key, value, RedisType.WORKSHEET);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getRedis")
	public String getRedis(@RequestParam(value="key", required=false) String key){
		return redisUtils.get(key, RedisType.WORKSHEET);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getKeyList")
	public String getKeyList(@RequestParam(value="key", required=false) String key){
		return new Gson().toJson(redisUtils.getKeysByPreKey(key, RedisType.WORKSHEET));
	}
	

	@PostMapping(value = "/workflow/pubFunc/loadColumnsByEntitySj")
	public List loadColumnsByEntitySj(
			@RequestParam(value="tableCode", required=true) String tableCode, 
			@RequestParam(value="colCode", required=true) String colCode,
			@RequestParam(value="entityId", required=false) String entityId) {
		return pubFunc.loadColumnsByEntityStatus(tableCode,colCode,entityId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/qryFullAddress")
	public List qryFullAddress(
			@RequestParam(value="telNo", required=true) String telNo, 
			@RequestParam(value="address", required=true) String address) {
		return pubFunc.qryFullAddress(telNo,address);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getHandleSubCardUrl")
	public String getHandleSubCardUrl(
			@RequestParam(value="prodNum", required=true) String prodNum, 
			@RequestParam(value="custName", required=true) String custName, 
			@RequestParam(value="relaInfo", required=true) String relaInfo, 
			@RequestParam(value="assistSellNo", required=true) String assistSellNo) {
		return pubFunc.getHandleSubCardUrl(prodNum, custName, relaInfo, assistSellNo);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getVersionList")
	public Object getVersionList(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		return pubFunc.getVersionList(json.optString("system"));
	}
	
	@RequestMapping(value = "/workflow/dynamic/getVersionObject")
	public Object getVersionObject(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		return pubFunc.getVersionObject(json.optString("system"));
	}
	
	@RequestMapping(value = "/workflow/dynamic/isShowCallOut")
	public Object isShowCallOut(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		int regionId = json.optInt("regionId");
		int serviceType = json.optInt("serviceType");
		int tacheId = json.optInt("tacheId");
		return pubFunc.isShowCallOut(regionId, serviceType, tacheId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getHotPointKeyList")
	public Object getHotPointKeyList(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		return pubFunc.getHotPointKeyList(json.optInt("key"));
	}

	@PostMapping(value = "/workflow/pubFunc/checkZpOrgYdxZdx")
	public int checkZpOrgYdxZdx(@RequestBody JSONObject jsonObject) {
		String orderId = jsonObject.optString("orderId");
		int tacheId = jsonObject.optInt("tacheId");
		
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		ServiceContent serviceContent = serviceContentDao.getServContentByOrderId(orderId, false, 0);
		String sixId = String.valueOf(pubFunc.getLastXX(serviceContent));
		String regionId = String.valueOf(orderAskInfo.getRegionId());
		int servType = orderAskInfo.getServType();
		int comeFrom = orderAskInfo.getChannelDetailId() > 0 ? orderAskInfo.getChannelDetailId() : orderAskInfo.getAskChannelId();//受理渠道
		// 对已配置直派目录的投诉单，增加直接预定性和直接终定性面板操作，同时提供禁用部门配置，当前禁用收单部门：南京_客户运营部
		Map config = pubFunc.getSheetAllotConfigNew(sixId, regionId, servType, tacheId, comeFrom, serviceContent);
		if(config != null && !config.isEmpty()) {
			String zpOrg = jsonObject.optString("zpOrg");
			return pubFunc.getPersonalityJudge("zp_org", zpOrg);
		}
		return 0;
	}

	@RequestMapping(value = "/workflow/dynamic/judgeOrderOvertime")
	public Object judgeOrderOvertime(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String serviceOrderId = json.optString("serviceOrderId");
		return ResultUtil.success(pubFunc.judgeOrderOvertime(serviceOrderId));
	}
	
	/**
	 * 获取受理渠道树、受理渠道全量数据
	 * @return
	 */
	@PostMapping(value = "/workflow/pubFunc/getAcceptChannel")
	public String getAcceptChannel() {
		Map<String, Object> obj = pubFunc.getAcceptChannel();
		return JSON.toJSONString(obj);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getBusinessDirNew")
	public Object getBusinessDirNew(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String referId = json.optString("referId");
		return ResultUtil.success(pubFunc.getBusinessDirNew(referId));
	}
	
	@PostMapping(value = "/workflow/pubFunc/getBusinessDirNewStr")
	public String getBusinessDirNewStr(@RequestParam(value="referId", required=true) String referId) {
		List<Map<String, Object>> obj = pubFunc.getBusinessDirNew(referId);
		return JSON.toJSONString(obj);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getBestOrderFlag")
	public Object getBestOrderFlag(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String keyId = json.optString("keyId");
		String conditionType = json.optString("conditionType");
		return ResultUtil.success(pubFunc.getBestOrderFlag(keyId, conditionType));
	}
	
	@RequestMapping(value = "/workflow/dynamic/getRepeatedOrderInfo")
	public Object getRepeatedOrderInfo(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String orderId = json.optString("orderId");
		String repeatType = json.optString("repeatType");
		return pubFunc.getRepeatedOrderInfo(orderId, repeatType);
	}

	@PostMapping(value = "/workflow/pubFunc/getDeductions")
	public Map getDeductions(@RequestParam(value = "orderId", required = true) String orderId ) {
		return pubFunc.getDeductions(orderId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/judgeRoleByMenuId")
	public Object judgeRoleByMenuId(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String menuId = json.optString("menuId");
		return ResultUtil.success(pubFunc.judgeRoleByMenuId(menuId));
	}

	@PostMapping(value = "/workflow/pubFunc/checkSpecialOrg")
	public boolean checkSpecialOrg(@RequestParam(value = "orgId", required = true) String orgId,
			@RequestParam(value = "orgType", required = true) int orgType) {
		return pubFunc.checkSpecialOrg(orgId, orgType);
	}

	@PostMapping(value = "/workflow/pubFunc/getRequireUninvited")
	public String getRequireUninvited(@RequestParam ("orderId") String orderId) {
		return dao.getRequireUninvited(orderId);
	}

	@PostMapping(value = "/workflow/pubFunc/getHisAnswerNameByOrderId")
	public String getHisAnswerNameByOrderId(@RequestParam ("orderId") String orderId, @RequestParam ("elementId") String elementId) {
		return iserviceContentTypeDao.getHisAnswerNameByOrderId(orderId,elementId);
	}
	
	@PostMapping(value = "/workflow/pubFunc/getConfigMapValue")
	public String getConfigMapValue(@RequestParam(value="key", required=true) String key){
		return pubFunc.getConfigMapValue(key);
	}
	
	@RequestMapping(value = "/workflow/dynamic/saveOperationRecord")
	public Object saveOperationRecord(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String uuid = json.optString("uuid");
		String orderId = json.optString("orderId");
		String sheetId = json.optString("sheetId");
		String prodNum = json.optString("prodNum");
		String opAction = json.optString("opAction");
		String opMenu = json.optString("opMenu");
		return ResultUtil.success(pubFunc.saveOperationRecord(uuid, orderId, sheetId, prodNum, opAction, opMenu));
	}
	
	@RequestMapping(value = "/workflow/dynamic/updateOperationRecord")
	public Object updateOperationRecord(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String uuid = json.optString("uuid");
		String orderId = json.optString("orderId");
		return ResultUtil.success(pubFunc.updateOperationRecord(uuid, orderId));
	}

	// 查询登录员工所属分公司列表，省投为全省
	@PostMapping(value = "/workflow/pubFunc/getCompanyByLogonStaff")
	public String getCompanyByLogonStaff() {
		return JSON.toJSONString(pubFunc.getCompanyByLogonStaff());
	}

	@RequestMapping(value = "/workflow/dynamic/querySensitiveData")
	public Object querySensitiveData(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String staffId = json.optString("staffId");
		return pubFunc.querySensitiveData(staffId);
	}

	@RequestMapping(value = "/workflow/dynamic/getOfferDetailByOrderId")
	public Object getOfferDetailByOrderId(@RequestBody(required = false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String orderId = json.optString("orderId");
		return pubFunc.getOfferDetailByOrderId(orderId);
	}

	@RequestMapping(value = "/workflow/dynamic/getOfferGradeByOfferId")
	public Object getOfferGradeByOfferId(@RequestBody(required = false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String offerId = json.optString("offerId");
		return ResultUtil.success(pubFunc.getOfferGradeByOfferId(offerId));
	}
}