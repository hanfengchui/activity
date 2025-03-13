package com.timesontransfar.satisfy.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.evaluation.EvaluationDetail;
import com.timesontransfar.evaluation.EvaluationOrderPojo;
import com.timesontransfar.evaluation.PerceptionInfo;
import com.timesontransfar.evaluation.SatisfyInfo;
import com.timesontransfar.evaluation.dao.EvaluationDao;
import com.timesontransfar.evaluation.pojo.OrganizationPojo;
import com.timesontransfar.satisfy.common.CommonUtil;
import com.timesontransfar.satisfy.dao.SatisfyDao;
import com.timesontransfar.satisfy.service.SatisfyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.service.ServiceOrderQuery;
import com.timesontransfar.evaluation.pojo.EvaluationSheetPojo;
import com.transfar.common.utils.StringUtils;

import net.sf.json.JSONObject;

@Service
public class SatisfyServiceImpl implements SatisfyService {
    private static final Logger log = LoggerFactory.getLogger(SatisfyServiceImpl.class);

    @Autowired
    private IdbgridDataPub dbgridDataPub;

    @Autowired
    private PubFunc pubFunc;
    
    @Autowired
	private EvaluationDao evaDao;

    @Autowired
    private SatisfyDao satisfyDao;
    
	@Autowired
	private ServiceOrderQuery orderQury;
	
	@Autowired
    private ISystemAuthorization systemAuthorization;
	

    private String success = "SUCCESS";
    private String error = "ERROR";
    
    
    @SuppressWarnings("rawtypes")
	public String createSatisfyOrder(SatisfyInfo info) {
    	String orderId = info.getServiceOrderId();
    	SatisfyInfo moreInfo = evaDao.getSatisfyInfo(orderId);
    	if(moreInfo == null) {
    		log.error("没有查询到单号为: {} 的其他满意度信息", orderId);
    		return null;
    	}
    	
    	boolean hisFlag = false;
    	Map lastDealInfo = pubFunc.getLastDealInfo(orderId);
    	if(lastDealInfo == null) {
    		log.error("没有查询到工单信息: {} hisFlag: {}", orderId, hisFlag);
    		
    		hisFlag = true;
    		lastDealInfo = pubFunc.getLastDealInfoHis(orderId);
    		if(lastDealInfo == null) {
    			log.error("没有查询到工单信息: {} hisFlag: {}", orderId, hisFlag);
    			return null;
    		}
    	}
        String dealStaff = this.defaultMapValueIfNull(lastDealInfo, "DEAL_STAFF", "0");
        String dealStaffName = this.defaultMapValueIfNull(lastDealInfo, "DEAL_STAFF_NAME", "");
        String dealOrgId = this.defaultMapValueIfNull(lastDealInfo, "DEAL_ORG_ID", "");
        String dealOrgName = this.defaultMapValueIfNull(lastDealInfo, "DEAL_ORG_NAME", "");
    	
        ServiceOrderInfo servOrderInfo = orderQury.getServOrderInfo(orderId, hisFlag);
        if (servOrderInfo == null){
        	log.error("没有查询到服务单信息: {}", orderId);
    		return null;
        }
        OrderAskInfo orderInfo = servOrderInfo.getOrderAskInfo();
        ServiceContent contentInfo = servOrderInfo.getServContent();
        OrderCustomerInfo custInfo = servOrderInfo.getOrderCustInfo();
        
        //修复单是否存在
        boolean isExist = satisfyDao.isExistEvaluationDetail(orderId);
        if(isExist) {
        	log.error("修复单已存在: {}", orderId);
        	return null;
        }
        
        String cpOrderId = satisfyDao.getCpOrderId(orderInfo.getRegionId());
        if (cpOrderId == null){
        	log.error("创建修复单号异常: {}", orderId);
    		return null;
        }
        
		//服务信息
        EvaluationOrderPojo order = new EvaluationOrderPojo();
		order.setWorkOrderId(cpOrderId);
		order.setCreateType(this.getCreateType(info.getIsSolve(), info.getCheckAssessScore()));
		order.setTopic(this.getTopic(order.getCreateType()));
		order.setRegionId(orderInfo.getRegionId());
		order.setRegionName(orderInfo.getRegionName());
		order.setAreaName(orderInfo.getAreaName());
		order.setRelaMan(orderInfo.getRelaMan());
		order.setRelaInfo(orderInfo.getRelaInfo());
		order.setProdNum(orderInfo.getProdNum());
		order.setSourceNum(orderInfo.getSourceNum());
		order.setBestOrder(contentInfo.getBestOrder());
		order.setBestOrderDesc(contentInfo.getBestOrderDesc());
		order.setFiveOrder(contentInfo.getFiveOrder());
		order.setFiveOrderDesc(contentInfo.getFiveOrderDesc());
		order.setTouchChannel("工单处理");
		order.setServiceOrgId(dealOrgId);
		order.setServiceOrgName(dealOrgName);
		order.setServiceStaffId(Integer.parseInt(dealStaff));
		order.setServiceStaffName(dealStaffName);
		order.setOrderLimit(this.getOrderLimit(order.getBestOrder()));
		order.setIsOverTime(0);
		order.setServiceOrderId(orderId);
		log.info("EvaluationOrder: {}", order);
		int num = satisfyDao.saveUnsatisfyOrder(order);
		log.info("saveUnsatisfyOrder 保存结果: {}", (num > 0 ? "成功" : "失败"));
		if(num == 0) {
			return null;
		}
		
		//修复详情
		EvaluationDetail detail = new EvaluationDetail();
		detail.setServiceOrderId(orderId);
		detail.setAcceptDate(orderInfo.getAskDate());
		detail.setUnifiedComplaintCode(moreInfo.getUnifiedComplaintCode());
		detail.setWorkOrderId(cpOrderId);
		detail.setOrderAssessId(info.getOrderAssessId());
		detail.setAssessMode(info.getAssessMode());
		detail.setAssessModeDesc(info.getAssessModeDesc());
		detail.setJoinMode(info.getJoinMode());
		detail.setJoinModeDesc(info.getJoinModeDesc());
		detail.setCrmCustId(org.apache.commons.lang3.StringUtils.defaultIfBlank(custInfo.getCrmCustId(), "0"));
		detail.setProdNum(orderInfo.getProdNum());
		detail.setRelaInfo(orderInfo.getRelaInfo());
		detail.setAssessSendTime(info.getAssessSendTime());
		detail.setUserSubTime(info.getUserSubTime());
		detail.setIsSolve(info.getIsSolve());
		detail.setCheckAssessResult(info.getCheckAssessResult());
		detail.setCheckAssessScore(info.getCheckAssessScore());
		detail.setSatisfiedReason(info.getSatisfiedReason());
		detail.setFinishFlag(0);
		log.info("EvaluationDetail: {}", detail);
		num = satisfyDao.saveUnsatisfyDetail(detail);
		log.info("saveUnsatisfyDetail 保存结果: {}", (num > 0 ? "成功" : "失败"));
		if(num == 0) {
			return null;
		}
		
		//修复工单
		return this.saveEvaluationSheet(order);
    }

    @Override
    public String createPerceptionOrder(PerceptionInfo info) {
    	int regionId = info.getRegionId();
    	String prodNum = info.getProdNum();
    	
        String cpOrderId = satisfyDao.getCpOrderId(regionId);
        if (cpOrderId == null){
            log.error("创建修复单号异常: {}", prodNum);
            return null;
        }

        //服务信息
        EvaluationOrderPojo order = new EvaluationOrderPojo();
        order.setWorkOrderId(cpOrderId);
        order.setCreateType(3);//感知修复
        order.setTopic("感知修复");
        order.setRegionId(regionId);
        order.setRegionName(CommonUtil.getRegionName(String.valueOf(regionId)));
        order.setAreaName(info.getAreaName());
        order.setRelaMan(info.getRelayMan());
        order.setRelaInfo(info.getRelayInfo());
        order.setProdNum(prodNum);
        order.setSourceNum(info.getSourceNum());
        order.setBestOrder(0);
        order.setBestOrderDesc(null);
        order.setFiveOrder(0);
        order.setFiveOrderDesc(null);
        order.setTouchChannel("感知修复");
        order.setServiceOrgId("0");
        order.setServiceOrgName("网上营业厅");
        order.setServiceStaffId(0);
		order.setServiceStaffName("网客用户");
        order.setOrderLimit(this.getOrderLimit(0));
        order.setIsOverTime(0);
        order.setServiceOrderId(prodNum);
        log.info("EvaluationOrder: {}", order);
        int num = satisfyDao.saveUnsatisfyOrder(order);
        log.info("saveUnsatisfyOrder 保存结果: {}", (num > 0 ? "成功" : "失败"));
        if(num == 0) {
            return null;
        }

        //修复详情
        EvaluationDetail detail = new EvaluationDetail();
        detail.setServiceOrderId(prodNum);
        detail.setAcceptDate(info.getAcceptDate());
        detail.setUnifiedComplaintCode(null);
        detail.setWorkOrderId(cpOrderId);
        detail.setOrderAssessId(null);
        detail.setAssessMode(0);
        detail.setAssessModeDesc(null);
        detail.setJoinMode(0);
        detail.setJoinModeDesc(null);
        detail.setCrmCustId(org.apache.commons.lang3.StringUtils.defaultIfBlank(info.getCrmCustId(), "0"));
        detail.setProdNum(prodNum);
        detail.setAssessSendTime(null);
        detail.setUserSubTime(null);
        detail.setIsSolve(info.getIsSolve());
        detail.setCheckAssessResult(null);
        detail.setCheckAssessScore(0);
        detail.setSatisfiedReason(info.getSatisfiedReason());
        detail.setFinishFlag(0);
        log.info("EvaluationDetail: {}", detail);
        num = satisfyDao.saveUnsatisfyDetail(detail);
        log.info("saveUnsatisfyDetail 保存结果: {}", (num > 0 ? "成功" : "失败"));
        if(num == 0) {
            return null;
        }
        
        //感知修复单指定流向省投诉_调帐班（363843）
        order.setServiceOrgId("363843");
        order.setServiceOrgName("省投诉_调帐班");
        return this.saveEvaluationSheet(order);
    }

    private int getOrderLimit(int bestOrder) {
    	//打标为最严工单的处理时限：24小时、其他的是168小时
    	int orderLimit = 168;
    	if(bestOrder > 100122410) {
    		orderLimit = 24;
    	}
    	return orderLimit;
    }
    
	@SuppressWarnings("rawtypes")
	private String defaultMapValueIfNull(Map map, String key, String defaultStr) {
		return map.get(key) == null ? defaultStr : map.get(key).toString();
	}
    
    private int getCreateType(int isSolve, int score) {
    	int createType = 0;//创建方式：0-未解决；1-不清楚；2-不满意
    	if(isSolve == 0) {
    		createType = 0;
    	}
    	else if(isSolve == 2) {
    		createType = 1;
    	}
    	else if(score < 9) {
    		createType = 2;
    	}
    	return createType;
    }
    
    private String getTopic(int createType) {
    	String topic;
    	if(createType == 0) {
    		topic = "未解决";
    	}
    	else if(createType == 1) {
    		topic = "不清楚";
    	}
    	else {
    		topic = "不满意";
    	}
    	return "投诉触点评价" + topic;
    }
    
	private String saveEvaluationSheet(EvaluationOrderPojo order) {
		String cpSheetId = satisfyDao.getCpSheetId(order.getRegionId());
        if (cpSheetId == null){
        	log.error("创建修复工单号异常: {}", order.getWorkOrderId());
    		return null;
        }
        
        EvaluationOrderPojo currentOrder = satisfyDao.getEvaluationOrder(order.getWorkOrderId(), false);
        if (currentOrder == null){
        	log.error("未查询到服务单: {}", order.getWorkOrderId());
    		return null;
        }
        
		EvaluationSheetPojo sheet = new EvaluationSheetPojo();
		sheet.setWorkSheetId(cpSheetId);
		sheet.setWorkOrderId(order.getWorkOrderId());
		sheet.setRegionId(order.getRegionId());
		sheet.setRegionName(order.getRegionName());
		sheet.setReceiveOrgId(order.getServiceOrgId());
		sheet.setReceiveOrgName(order.getServiceOrgName());
		sheet.setReceiveStaff(0);
		sheet.setReceiveStaffName("");
		sheet.setDealStaff(0);
		sheet.setDealStaffName("");
		sheet.setDealOrgId("0");
		sheet.setDealOrgName("");
		sheet.setReturnStaff(order.getServiceStaffId());
		sheet.setReturnStaffName(order.getServiceStaffName());
		sheet.setReturnOrgId(order.getServiceOrgId());
		sheet.setReturnOrgName(order.getServiceOrgName());
		sheet.setReturnRequire("");
		sheet.setSourceSheetId(order.getWorkOrderId());
		sheet.setSheetStatus(0);
		sheet.setLockFlag(0);
		sheet.setSheetLimit(order.getOrderLimit()*60);
		sheet.setTacheId(1);
		sheet.setTacheDesc("后台派单");
		sheet.setLimitDate(currentOrder.getLimitDate());
		sheet.setIsSheetOverTime(0);
		sheet.setResponseFlag(0);
		log.info("EvaluationSheetPojo: {}", sheet);
		int num = satisfyDao.saveUnsatisfySheet(sheet);
		log.info("saveUnsatisfySheet 保存结果: {}", (num > 0 ? "成功" : "失败"));
		if(num == 0) {
			return null;
		}
		return order.getWorkOrderId();
	}
	

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public GridDataInfo getSheetPoolList(String param) {
        JSONObject json = JSONObject.fromObject(param);
        int begion = json.optInt("begion");
        int pageSize = json.optInt("pageSize");
        String strWhere = this.getSheetListWhere(json.optString("strWhere"));
        
        String querySql = "SELECT S.WORK_ORDER_ID,S.WORK_SHEET_ID,O.SERVICE_ORDER_ID,O.PROD_NUM,O.RELA_INFO,S.RECEIVE_ORG_NAME,S.TACHE_DESC,"
                + "O.BEST_ORDER_DESC,O.FIVE_ORDER_DESC,DATE_FORMAT(O.CREATE_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,"
                + "DATE_FORMAT(S.CREATE_DATE,'%Y-%m-%d %H:%i:%s') CREATE_DATE,S.SHEET_LIMIT,"
                + "DATE_FORMAT(S.LIMIT_DATE,'%Y-%m-%d %H:%i:%s') LIMIT_DATE,"
                + "TIMESTAMPDIFF(MINUTE, now(), S.LIMIT_DATE) LETF_TIME,O.REGION_NAME,S.RETURN_STAFF_NAME,S.TACHE_ID,O.REGION_ID "
                + "FROM CC_UNSATISFY_SHEET S, CC_UNSATISFY_ORDER O WHERE S.WORK_ORDER_ID=O.WORK_ORDER_ID "
                + "AND S.LOCK_FLAG = 0 AND S.SHEET_STATUS = 0" + strWhere;
        Map map = new HashMap();
		map.put("CC_UNSATISFY_SHEET", "S");
		map.put("CC_UNSATISFY_ORDER", "O");
        querySql = systemAuthorization.getAuthedSql(map, querySql, "900018410");//满意度处理工单池实体
        return dbgridDataPub.getResultBySize(querySql, begion, pageSize, " ORDER BY S.CREATE_DATE", "不满意修复工单池列表");
    }


    @SuppressWarnings({ "rawtypes", "unchecked" })
	public GridDataInfo getMyTaskList(String param) {
        JSONObject json = JSONObject.fromObject(param);
        int begion = json.optInt("begion");
        int pageSize = json.optInt("pageSize");
        String strWhere = this.getSheetListWhere(json.optString("strWhere"));
        
        String querySql = "SELECT S.WORK_ORDER_ID,S.WORK_SHEET_ID,O.SERVICE_ORDER_ID,O.PROD_NUM,O.RELA_INFO,S.RECEIVE_ORG_NAME,S.TACHE_DESC,"
                + "O.BEST_ORDER_DESC,O.FIVE_ORDER_DESC,DATE_FORMAT(O.CREATE_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,"
                + "DATE_FORMAT(S.CREATE_DATE,'%Y-%m-%d %H:%i:%s') CREATE_DATE,S.SHEET_LIMIT,"
                + "DATE_FORMAT(S.LIMIT_DATE,'%Y-%m-%d %H:%i:%s') LIMIT_DATE,"
                + "TIMESTAMPDIFF(MINUTE, now(), S.LIMIT_DATE) LETF_TIME,O.REGION_NAME,S.RETURN_STAFF_NAME,S.TACHE_ID,O.REGION_ID "
                + "FROM CC_UNSATISFY_SHEET S, CC_UNSATISFY_ORDER O WHERE S.WORK_ORDER_ID=O.WORK_ORDER_ID "
                + "AND S.LOCK_FLAG = 1 AND S.SHEET_STATUS = 1" + strWhere;
        Map map = new HashMap();
		map.put("CC_UNSATISFY_SHEET", "S");
		map.put("CC_UNSATISFY_ORDER", "O");
        querySql = systemAuthorization.getAuthedSql(map, querySql, "900018411");//满意度处理我的任务实体
        return dbgridDataPub.getResultBySize(querySql, begion, pageSize, " ORDER BY S.CREATE_DATE", "不满意修复我的任务列表");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public GridDataInfo getReturnPool(String param) {
        JSONObject json = JSONObject.fromObject(param);
        int begion = json.optInt("begion");
        int pageSize = json.optInt("pageSize");
        String strWhere = this.getSheetListWhere(json.optString("strWhere"));
        
        String querySql = "SELECT S.WORK_ORDER_ID,S.WORK_SHEET_ID,O.SERVICE_ORDER_ID,O.PROD_NUM,O.RELA_INFO,S.RECEIVE_ORG_NAME,S.TACHE_DESC,"
                + "O.BEST_ORDER_DESC,O.FIVE_ORDER_DESC,DATE_FORMAT(O.CREATE_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,"
                + "DATE_FORMAT(S.CREATE_DATE,'%Y-%m-%d %H:%i:%s') CREATE_DATE,S.SHEET_LIMIT,"
                + "DATE_FORMAT(S.LIMIT_DATE,'%Y-%m-%d %H:%i:%s') LIMIT_DATE,"
                + "TIMESTAMPDIFF(MINUTE, now(), S.LIMIT_DATE) LETF_TIME,O.REGION_NAME,S.RETURN_STAFF_NAME,S.TACHE_ID,O.REGION_ID "
                + "FROM CC_UNSATISFY_SHEET S, CC_UNSATISFY_ORDER O WHERE S.WORK_ORDER_ID=O.WORK_ORDER_ID "
                + "AND S.LOCK_FLAG = 0 AND S.SHEET_STATUS = 3" + strWhere;
        Map map = new HashMap();
		map.put("CC_UNSATISFY_SHEET", "S");
		map.put("CC_UNSATISFY_ORDER", "O");
        querySql = systemAuthorization.getAuthedSql(map, querySql, "900018412");//满意度处理已派发实体
        return dbgridDataPub.getResultBySize(querySql, begion, pageSize, " ORDER BY S.CREATE_DATE", "不满意修复已派发列表");
    }

    private String getSheetListWhere(String map) {
        JSONObject obj = JSONObject.fromObject(map);

        String strwhere = "";
        if (StringUtils.isNotEmpty(obj.optString("orderId"))) {
            strwhere += " AND O.WORK_ORDER_ID='" + obj.optString("orderId") + "'";
        }
        if (StringUtils.isNotEmpty(obj.optString("sheetId"))) {
            strwhere += " AND S.WORK_SHEET_ID='" + obj.optString("sheetId") + "'";
        }
        if (StringUtils.isNotEmpty(obj.optString("serviceOrderId"))) {
            strwhere += " AND O.SERVICE_ORDER_ID='" + obj.optString("serviceOrderId") + "'";
        }
        if (StringUtils.isNotEmpty(obj.optString("prodNum"))) {
            strwhere += " AND O.PROD_NUM='" + obj.optString("prodNum") + "'";
        }
        if (StringUtils.isNotEmpty(obj.optString("createType"))) {
            strwhere += " AND O.CREATE_TYPE=" + obj.optString("createType");
        }
        if (StringUtils.isNotEmpty(obj.optString("regionId"))) {
            strwhere += " AND O.REGION_ID=" + obj.optString("regionId");
        }
        if (StringUtils.isNotEmpty(obj.optString("tacheId"))) {
            strwhere += " AND S.TACHE_ID=" + obj.optString("tacheId");
        }
        if (StringUtils.isNotEmpty(obj.optString("orderLabel"))) {
        	if("0".equals(obj.optString("orderLabel"))) {
        		strwhere += " AND O.BEST_ORDER <= 100122410";
        	} else {
        		strwhere += " AND O.BEST_ORDER > 100122410";
        	}
        }
        if (StringUtils.isNotNull(obj.optJSONArray("acceptDate")) && !obj.optJSONArray("acceptDate").isEmpty()) {
            strwhere += " AND O.CREATE_DATE > STR_TO_DATE('" + obj.optJSONArray("acceptDate").optString(0) + "','%Y-%m-%d %H:%i:%s')";
            strwhere += " AND O.CREATE_DATE < STR_TO_DATE('" + obj.optJSONArray("acceptDate").optString(1) + "','%Y-%m-%d %H:%i:%s')";
        }
        if (StringUtils.isNotNull(obj.optJSONArray("createDate")) && !obj.optJSONArray("createDate").isEmpty()) {
            strwhere += " AND S.CREATE_DATE > STR_TO_DATE('" + obj.optJSONArray("createDate").optString(0) + "','%Y-%m-%d %H:%i:%s')";
            strwhere += " AND S.CREATE_DATE < STR_TO_DATE('" + obj.optJSONArray("createDate").optString(1) + "','%Y-%m-%d %H:%i:%s')";
        }
        strwhere = this.getSheetListWhereStr(obj, strwhere);
        return strwhere;
    }
    
    private String getSheetListWhereStr(JSONObject obj, String strwhere) {
    	if (StringUtils.isNotEmpty(obj.optString("topic"))) {
            strwhere += " AND O.TOPIC = '" + obj.optString("topic") + "'";
        }
        if (StringUtils.isNotEmpty(obj.optString("regionId"))) {
            strwhere += " AND S.REGION_ID = " + obj.optString("regionId");
        }
        if (StringUtils.isNotEmpty(obj.optString("returnStaff"))) {
        	int staffId = pubFunc.getStaffId(obj.optString("returnStaff"));
        	if(staffId != 0) {
        		strwhere += " AND S.RETURN_STAFF = " + staffId;
        	}
        }
        return strwhere;
    }

    public List<String> fetchBatchWorkSheet(String param) {
        JSONObject json = JSONObject.fromObject(param);
        int fetchType = json.optInt("fetchType");
        String sheetIdListStr = json.optString("sheetIdList");
        List<String> sheetIdList = JSON.parseArray(sheetIdListStr, String.class);
        log.info("fetchBatchWorkSheet sheetIdList: {} fetchType : {}", sheetIdList, fetchType);

        List<String> deleSheet = new ArrayList<>();//返回被提取工单
        for (int i = 0; i < sheetIdList.size(); i++) {
            String sheetId = sheetIdList.get(i);
            String fetchInfo = this.fetchWorkSheet(sheetId);
            if (success.equals(fetchInfo)) {
                deleSheet.add(sheetId);
            }
        }
        log.info("fetchBatchWorkSheet result: {}", deleSheet.size());
        return deleSheet;
    }

    @Override
    public List<String> allotBatchWorkSheet(String param) {
        JSONObject json = JSONObject.fromObject(param);
        String sheetIdListStr = json.optString("sheetIdList");
        String staffId = json.optString("staffId");
        String staffName = json.optString("staffName");
        List<String> sheetIdList = JSON.parseArray(sheetIdListStr, String.class);
        OrganizationPojo organizationPojo = satisfyDao.getStaffOrganization(staffId);
        String orgId = organizationPojo.getReceiveOrgId();
        String orgName = organizationPojo.getReceiveOrgName();
        List<String> allotSheet = new ArrayList<>();
        for (String sheetId : sheetIdList) {
            boolean hisFlag = false;
            EvaluationSheetPojo sheetPubInfo = this.satisfyDao.getSheetObj(sheetId, hisFlag);
            if (sheetPubInfo == null) {
                allotSheet = new ArrayList<>();
            } else {
                if (sheetPubInfo.getLockFlag() == 0 && sheetPubInfo.getSheetStatus() != 3 && sheetPubInfo.getSheetStatus() != 9) {
                    satisfyDao.updateFetchSheetStaff(sheetId, Integer.valueOf(staffId), staffName, orgId, orgName, 1, 1);
                    allotSheet.add(sheetId);
                }
            }
        }
        return allotSheet;
    }

    /**
     * 后台派单，转派
     */
    public String dispatchAssignSheet(String param) {
        JSONObject json = JSONObject.fromObject(param);
        String sheetId = json.optString("sheetId");

        EvaluationSheetPojo currentSheet = satisfyDao.getCurrentSheet(sheetId);
        if (currentSheet == null) {
            log.error("未查询到工单号: {}", sheetId);
            return "ERROR";
        }
        if (this.notInDeal(currentSheet.getSheetStatus(), currentSheet.getLockFlag())) {//非处理中的工单
            return "STATUS_ERROR";
        }
        
        TsmStaff staff = this.pubFunc.getLogonStaff();// 取当前登录员工信息
        
        //后台派单环节，转派
        this.finishDealSheet(param, staff, currentSheet, 0, 0);
        //处理单生成
        this.createDealSheet(param, staff, currentSheet);
        //审核单生成
        this.createAuditSheet(staff, currentSheet);
        
        return "SUCCESS";
    }
    
    /**
     * 后台派单/工单审核，处理
     */
    public String submitAssignSheet(String param) {
    	EvaluationSheetPojo dealSheet = JSON.parseObject(param, EvaluationSheetPojo.class);
        String sheetId = dealSheet.getWorkSheetId();
    	
        EvaluationSheetPojo currentSheet = satisfyDao.getCurrentSheet(sheetId);
        if (currentSheet == null) {
            log.error("未查询到工单号: {}", sheetId);
            return "ERROR";
        }
        if (this.notInDeal(currentSheet.getSheetStatus(), currentSheet.getLockFlag())) {//非处理中的工单
            return "STATUS_ERROR";
        }
        
        String orderId = currentSheet.getWorkOrderId();
        TsmStaff staff = this.pubFunc.getLogonStaff();// 取当前登录员工信息
        
        //后台派单/工单审核环节，直接处理
        this.finishDealSheet(param, staff, currentSheet, 1, 1);
        //更新处理详情
        satisfyDao.updateDealDetail(dealSheet);
        //服务单完成
        satisfyDao.finishSatisfyOrder(currentSheet.getWorkOrderId(), currentSheet.getIsSheetOverTime());
        
        //更新修复单详情
        this.finishSatisfyDetail(dealSheet);
        //更新修复单外呼详情
        this.updateCallRecord(orderId);
        
        //满意度工单归档
        this.finishSatisfyOrder(orderId);
        
        return "SUCCESS";
    }
    
    private void updateCallRecord(String orderId) {
    	List<Map<String, Object>> callOutList = satisfyDao.getCallOutRecord(orderId);
    	if(callOutList.isEmpty()) {
    		return;
    	}
    	
    	int callNum = callOutList.size();
    	log.info("getCallOutRecord size: {}", callNum);
    	
    	Map<String, Object> firstCallMap = callOutList.get(0);
    	String firstCallTime = firstCallMap.get("CALL_ARRIVE").toString();//首次外呼时间
    	
    	Map<String, Object> lastCallMap = callOutList.get(callNum-1);
    	String staffId = lastCallMap.get("STAFF_ID").toString();//最后一次外呼的员工ID
    	String logonName = pubFunc.getStaffLongName(Integer.parseInt(staffId));
    	String callGuid = lastCallMap.get("CALL_GUID").toString();//最后一次外呼流水号
    	
    	EvaluationDetail detail = new EvaluationDetail();
    	detail.setWorkOrderId(orderId);
    	detail.setPayReturnTime(firstCallTime);
    	detail.setPayReturnWorkNo(logonName);
    	detail.setCallGuid(callGuid);
    	int num = satisfyDao.updateCallOutRecord(detail);
		log.info("updateCallRecord 保存结果: {}", (num > 0 ? "成功" : "失败"));
    }
    
    public void finishSatisfyOrder(String orderId) {
    	int i = satisfyDao.saveOrderHisByOrderId(orderId);
    	if(i > 0) {
    		satisfyDao.deleteOrder(orderId);
    	}
    	
    	int j = satisfyDao.saveSheetHisByOrderId(orderId);
    	if(j > 0) {
    		satisfyDao.deleteSheetByOrderId(orderId);
    	}
    }
    
    /**
     * 部门处理，处理
     */
    public String submitDealSheet(String param) {
    	EvaluationSheetPojo dealSheet = JSON.parseObject(param, EvaluationSheetPojo.class);
        String sheetId = dealSheet.getWorkSheetId();
    	
        EvaluationSheetPojo currentSheet = satisfyDao.getCurrentSheet(sheetId);
        if (currentSheet == null) {
            log.error("未查询到工单号: {}", sheetId);
            return "ERROR";
        }
        if (this.notInDeal(currentSheet.getSheetStatus(), currentSheet.getLockFlag())) {//非处理中的工单
            return "STATUS_ERROR";
        }
        
        TsmStaff staff = this.pubFunc.getLogonStaff();// 取当前登录员工信息
        
        //部门处理环节，直接处理
        this.finishDealSheet(param, staff, currentSheet, 1, 0);
        //更新处理详情
        satisfyDao.updateDealDetail(dealSheet);
        //激活审核工单
        this.updateAuditSheet(currentSheet);
        
        return "SUCCESS";
    }
    
    /**
     * 部门处理，转派
     */
    public String dispatchDealSheet(String param) {
        JSONObject json = JSONObject.fromObject(param);
        String sheetId = json.optString("sheetId");

        EvaluationSheetPojo currentSheet = satisfyDao.getCurrentSheet(sheetId);
        if (currentSheet == null) {
            log.error("未查询到工单号: {}", sheetId);
            return "ERROR";
        }
        if (this.notInDeal(currentSheet.getSheetStatus(), currentSheet.getLockFlag())) {//非处理中的工单
            return "STATUS_ERROR";
        }
        
        TsmStaff staff = this.pubFunc.getLogonStaff();// 取当前登录员工信息
        
        //部门处理环节，转派
        this.finishDealSheet(param, staff, currentSheet, 0, 0);
        //处理单生成
        this.createDealSheet(param, staff, currentSheet);
        
        return "SUCCESS";
    }
    
    private void updateAuditSheet(EvaluationSheetPojo currentSheet) {
    	String sheetId = satisfyDao.getAuditSheetId(currentSheet.getWorkOrderId());
    	EvaluationSheetPojo sheet = new EvaluationSheetPojo();
    	sheet.setSheetStatus(0);
    	sheet.setLockFlag(0);
    	sheet.setSheetLimit(currentSheet.getLeftLimit());
    	sheet.setIsSheetOverTime(currentSheet.getIsSheetOverTime());
    	sheet.setWorkSheetId(sheetId);
        int num = satisfyDao.updateAuditSheet(sheet);
        log.info("updateAuditSheet 更新结果: {}", (num > 0 ? "成功" : "失败"));
    }
    
    private void finishSatisfyDetail(EvaluationSheetPojo dealSheet) {
    	String payReturn = dealSheet.getContactResultId() == 1 ? "1" : "0";//是否回访成功（成功：1；不成功：0)（处理环节、联系情况，成功：1）
    	String repairResult = dealSheet.getRepairResultId() == 2 ? "0" : "1";//回访成功用户修复结果：0-不满意；1-满意
    	String failedReason = this.getFailReason(dealSheet.getContactResultId());//回访不成功原因
        String result = dealSheet.getReasonDetailDesc();
        String comment = dealSheet.getCustAdviceComfirm();
        satisfyDao.finishSatisfyDetail(dealSheet.getWorkOrderId(), payReturn, repairResult, failedReason, result, comment);
    }
    
    private String getFailReason(int contactResultId) {
    	String failReason = null;//回访不成功原因：1-联系不上（空号、停机、无人接听、呼叫失败）；2-不接受回访（用户拒接）；3-不具备修复条件（无需回访）；4-其他原因
        switch (contactResultId){//联系情况：1-成功；2-空号；3-停机；4-无人接听；5-用户拒接；6-呼叫失败；7-无需回访；8-其它原因
            case 2:
            case 3:
            case 4:
            case 6:
            	failReason = "1";
                break;
            case 5:
            	failReason = "2";
                break;
            case 7:
            	failReason = "3";
                break;
            case 8:
            	failReason = "4";
                break;
            default:
                break;
        }
        return failReason;
    }
    
    private void finishDealSheet(String param, TsmStaff staff, EvaluationSheetPojo currentSheet, int dealType, int responseFlag) {
    	String sheetId = currentSheet.getWorkSheetId();
    	JSONObject json = JSONObject.fromObject(param);
    	String dealContent = json.optString("dealContent");
    	
    	EvaluationSheetPojo sheet = new EvaluationSheetPojo();
    	sheet.setWorkSheetId(sheetId);
    	sheet.setDealStaff(Integer.parseInt(staff.getId()));
		sheet.setDealStaffName(staff.getName());
		sheet.setDealOrgId(staff.getOrganizationId());
		sheet.setDealOrgName(staff.getOrgName());
		sheet.setSheetStatus(2);
		sheet.setLockFlag(2);
		sheet.setDealType(dealType);
		sheet.setDealContent(dealContent);
		sheet.setIsSheetOverTime(sheet.getIsSheetOverTime());
        sheet.setResponseFlag(responseFlag);
        satisfyDao.finishAssginSheet(sheet);
    }
    
    private String createAuditSheet(TsmStaff staff, EvaluationSheetPojo currentSheet) {
		String cpSheetId = satisfyDao.getCpSheetId(currentSheet.getRegionId());
        if (cpSheetId == null){
        	log.error("创建修复工单号异常: {}", currentSheet.getWorkOrderId());
        	return null;
        }
        
    	EvaluationSheetPojo sheet = new EvaluationSheetPojo();
		sheet.setWorkSheetId(cpSheetId);
		sheet.setWorkOrderId(currentSheet.getWorkOrderId());
		sheet.setRegionId(currentSheet.getRegionId());
		sheet.setRegionName(currentSheet.getRegionName());
		sheet.setReceiveOrgId(staff.getOrganizationId());
		sheet.setReceiveOrgName(staff.getOrgName());
		sheet.setReceiveStaff(0);
		sheet.setReceiveStaffName("");
		sheet.setDealStaff(0);
		sheet.setDealStaffName("");
		sheet.setDealOrgId("0");
		sheet.setDealOrgName("");
		sheet.setReturnStaff(Integer.parseInt(staff.getId()));
		sheet.setReturnStaffName(staff.getName());
		sheet.setReturnOrgId(staff.getOrganizationId());
		sheet.setReturnOrgName(staff.getOrgName());
		sheet.setReturnRequire("");
		sheet.setSheetStatus(3);
		sheet.setLockFlag(0);
		sheet.setSourceSheetId(currentSheet.getWorkSheetId());
		sheet.setSheetLimit(currentSheet.getLeftLimit());
		sheet.setTacheId(3);
		sheet.setTacheDesc("工单审核");
		sheet.setLimitDate(currentSheet.getLimitDate());
		sheet.setIsSheetOverTime(currentSheet.getIsSheetOverTime());
		sheet.setResponseFlag(0);
		log.info("EvaluationSheetPojo: {}", sheet);
		int num = satisfyDao.saveUnsatisfySheet(sheet);
		log.info("createAuditSheet 保存结果: {}", (num > 0 ? "成功" : "失败"));
		if(num == 0) {
			return null;
		}
		return "SUCCESS";
    }
    
    private String createDealSheet(String param, TsmStaff staff, EvaluationSheetPojo currentSheet) {
		String cpSheetId = satisfyDao.getCpSheetId(currentSheet.getRegionId());
        if (cpSheetId == null){
        	log.error("创建修复工单号异常: {}", currentSheet.getWorkOrderId());
        	return null;
        }
        
    	EvaluationSheetPojo sheet = new EvaluationSheetPojo();
    	this.setSheetAssignObj(param, staff, sheet);
		sheet.setWorkSheetId(cpSheetId);
		sheet.setWorkOrderId(currentSheet.getWorkOrderId());
		sheet.setRegionId(currentSheet.getRegionId());
		sheet.setRegionName(currentSheet.getRegionName());
		sheet.setSourceSheetId(currentSheet.getWorkSheetId());
		sheet.setSheetLimit(currentSheet.getLeftLimit());
		sheet.setTacheId(2);
		sheet.setTacheDesc("部门处理");
		sheet.setLimitDate(currentSheet.getLimitDate());
		sheet.setIsSheetOverTime(currentSheet.getIsSheetOverTime());
		sheet.setResponseFlag(0);
		log.info("EvaluationSheetPojo: {}", sheet);
		int num = satisfyDao.saveUnsatisfySheet(sheet);
		log.info("createDealSheet 保存结果: {}", (num > 0 ? "成功" : "失败"));
		if(num == 0) {
			return null;
		}
		
		JSONObject json = JSONObject.fromObject(param);
		String isDispatch = json.optString("isDispatch");
		if("1".equals(isDispatch)){//员工
			 //更新提单员工信息及工单状态
	        satisfyDao.updateFetchSheetStaff(cpSheetId, sheet.getDealStaff(), sheet.getDealStaffName(), sheet.getDealOrgId(), sheet.getDealOrgName(), sheet.getSheetStatus(), sheet.getLockFlag());
		}
		return "SUCCESS";
    }
    
    private void setSheetAssignObj(String param, TsmStaff staff, EvaluationSheetPojo sheet) {
    	JSONObject json = JSONObject.fromObject(param);
        String isDispatch = json.optString("isDispatch");
        String mainStaff = json.optString("mainStaff");
        String mainOrg = json.optString("mainOrg");
        String mainOrgName = json.optString("mainOrgName");
        String desc = json.optString("dealContent");
        
        sheet.setReturnStaff(Integer.parseInt(staff.getId()));
		sheet.setReturnStaffName(staff.getName());
		sheet.setReturnOrgId(staff.getOrganizationId());
		sheet.setReturnOrgName(staff.getOrgName());
        if("0".equals(isDispatch)){//部门
        	sheet.setReceiveOrgId(mainOrg);
    		sheet.setReceiveOrgName(mainOrgName);
    		sheet.setReceiveStaff(0);
    		sheet.setReceiveStaffName("");
    		sheet.setDealStaff(0);
    		sheet.setDealStaffName("");
    		sheet.setDealOrgId("0");
    		sheet.setDealOrgName("");
    		sheet.setReturnRequire(desc);
    		sheet.setSheetStatus(0);
    		sheet.setLockFlag(0);
        } else {
        	TsmStaff receiveStaff = pubFunc.getStaff(Integer.parseInt(mainStaff));
        	sheet.setReceiveOrgId(receiveStaff.getOrganizationId());
    		sheet.setReceiveOrgName(receiveStaff.getOrgName());
    		sheet.setReceiveStaff(Integer.parseInt(receiveStaff.getId()));
    		sheet.setReceiveStaffName(receiveStaff.getName());
    		sheet.setDealStaff(Integer.parseInt(receiveStaff.getId()));
    		sheet.setDealStaffName(receiveStaff.getName());
    		sheet.setDealOrgId(receiveStaff.getOrganizationId());
    		sheet.setDealOrgName(receiveStaff.getOrgName());
    		sheet.setReturnRequire(desc);
    		sheet.setSheetStatus(1);
    		sheet.setLockFlag(1);
        }
    }

    public EvaluationSheetPojo getLastResponseSheet(String param) {
        JSONObject json = JSONObject.fromObject(param);
        String orderId = json.optString("orderId");
        return satisfyDao.getLastResponseSheet(orderId);
    }

    public List<Map<String, Object>> getDealSheetFlow(String param) {
        JSONObject json = JSONObject.fromObject(param);
        String orderId = json.optString("orderId");
        boolean hisFlag = json.optBoolean("hisFlag");
        List<Map<String, Object>> list = satisfyDao.getDealSheetFlow(orderId, hisFlag);
        for(Map<String, Object> obj : list) {
        	String dealOrgId = obj.get("dealOrgId").toString();
        	String fullDealOrgName = pubFunc.getOrgWater(dealOrgId);
        	obj.put("fullDealOrgName", fullDealOrgName);
        	Object responsibilityOrgId = obj.get("responsibilityOrgId");
        	if(responsibilityOrgId != null &&  org.apache.commons.lang3.StringUtils.isNotBlank(responsibilityOrgId.toString())) {
        		String fullResponsibilityOrgName = pubFunc.getOrgWater(responsibilityOrgId.toString());
        		obj.put("fullResponsibilityOrgName", fullResponsibilityOrgName);
        	}  	
        }
        return list;
    }

    /**
     * 从工单池提取工单方法
     *
     * @param sheetId
     * @return 取回的工单数
     */
    public String fetchWorkSheet(String sheetId) {
        boolean hisFlag = false;

        EvaluationSheetPojo sheetPubInfo = this.satisfyDao.getSheetObj(sheetId, hisFlag);
        if (sheetPubInfo == null) {
            log.error("未查询到工单号: {}", sheetId);
            return error;
        }
        if (sheetPubInfo.getSheetStatus() == 9) {//工单池挂起的工单不能直接提取
            return "STATUS_ERROR";
        }
        if (sheetPubInfo.getLockFlag() == 0 && sheetPubInfo.getSheetStatus() != 3) {
            TsmStaff staff = this.pubFunc.getLogonStaff();// 取当前登录员工信息
            int staffId = Integer.parseInt(staff.getId());
            String staffName = staff.getName();
            String orgId = staff.getOrganizationId();
            String orgName = staff.getOrgName();

            int sheetStatus = 1;//处理中
            int lockFlag = 1;
            //更新提单员工信息及工单状态
            this.satisfyDao.updateFetchSheetStaff(sheetId, staffId, staffName, orgId, orgName, sheetStatus, lockFlag);

            return success;
        }
        return error;
    }
    
    public EvaluationOrderPojo getEvaluationOrder(String orderId) {
    	return satisfyDao.getEvaluationOrder(orderId, false);
    }
    
    public JSONObject getUnSatisfyInfo(String orderId, boolean hisFlag, String sheetId) {
    	EvaluationOrderPojo order = satisfyDao.getEvaluationOrder(orderId, hisFlag);
    	EvaluationDetail detail = satisfyDao.getEvaluationDetail(orderId);
    	String saveDealContent = null;
    	if(org.apache.commons.lang3.StringUtils.isNotBlank(sheetId)) {
    		saveDealContent = satisfyDao.getSaveDealContent(sheetId);
    	}
    	
    	JSONObject orderJson = JSONObject.fromObject(order);
    	JSONObject detailJson = JSONObject.fromObject(detail);
    	JSONObject json = new JSONObject();
    	json.put("order", orderJson);
    	json.put("detail", detailJson);
    	json.put("saveDealContent", saveDealContent);
    	return json;
    }

    @Override
    public JSONObject saveStageContent(String sheetId, String content, String orderId) {
        boolean flag = insertDealContent(sheetId, content);
        JSONObject relJson = new JSONObject();
        if(flag) {
            String saveDealContent = satisfyDao.getSaveDealContent(sheetId);
            relJson.put("dealContent", saveDealContent);
        }
        relJson.put("flag", flag);
        return relJson;
    }

    public boolean insertDealContent(String sheetId, String content) {
        boolean dealContent = false;
        String tm = satisfyDao.getSaveDealContent(sheetId);

        TsmStaff staffInfo = this.pubFunc.getLogonStaff();
        String staffName = staffInfo.getName();
        String logonName = staffInfo.getLogonName();

        content += " -- " + staffName + "(" + logonName + ") " + this.pubFunc.getSysDate();
        if(org.apache.commons.lang3.StringUtils.isNotBlank(tm)) {
        	content += "\n" + tm;
        }

        int cont = this.satisfyDao.saveStageContent(sheetId, content);
        if(cont > 0) {
            dealContent = true;
        }
        return dealContent;
    }

    private boolean notInDeal(int sheetStatus, int lockFlag){
    	boolean flag = false;
    	if(lockFlag == 1){
    		switch (sheetStatus) {
				case 0:
				case 2:
				case 3:
				case 9:
					flag = true;
					break;
				default:
					break;
			}    		
    	}else{
    		flag = true;
    	}
    	return flag;
    }
    
    public GridDataInfo getOrderList(String param) {
    	JSONObject json = JSONObject.fromObject(param);
        int begion = json.optInt("begion");
        int pageSize = json.optInt("pageSize");
        String strWhere = json.optString("strWhere");
        String orderFlag = json.optString("orderFlag");
        
        String queryWhere = this.getOrderListWhere(strWhere);
        if("now".equals(orderFlag)) {
        	return this.getCurrentOrderList(begion, pageSize, queryWhere);
        } else {
        	return this.getHistoryOrderList(begion, pageSize, queryWhere);
        }
    }
    
	private GridDataInfo getCurrentOrderList(int begion, int pageSize, String queryWhere) {
        String querySql = "SELECT C.WORK_ORDER_ID, DATE_FORMAT(C.CREATE_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE, C.CREATE_TYPE, C.REGION_ID, C.REGION_NAME, "
        		+ "C.PROD_NUM, C.BEST_ORDER_DESC, C.SERVICE_ORG_NAME, C.SERVICE_STAFF_NAME, DATE_FORMAT(C.LIMIT_DATE,'%Y-%m-%d %H:%i:%s') LIMIT_DATE, "
        		+ "C.IS_OVERTIME, C.SERVICE_ORDER_ID FROM CC_UNSATISFY_ORDER C WHERE 1 = 1 " + queryWhere;
        return dbgridDataPub.getResultBySize(querySql, begion, pageSize, " ORDER BY C.CREATE_DATE", "满意度修复单当前查询");
    }
    
    private GridDataInfo getHistoryOrderList(int begion, int pageSize, String queryWhere) {
    	String querySql = "SELECT C.WORK_ORDER_ID, DATE_FORMAT(C.CREATE_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE, C.CREATE_TYPE, C.REGION_ID, C.REGION_NAME, "
	       		+ "C.PROD_NUM, C.BEST_ORDER_DESC, C.SERVICE_ORG_NAME, C.SERVICE_STAFF_NAME, DATE_FORMAT(C.LIMIT_DATE,'%Y-%m-%d %H:%i:%s') LIMIT_DATE, "
	       		+ "C.IS_OVERTIME, C.SERVICE_ORDER_ID FROM CC_UNSATISFY_ORDER_HIS C WHERE 1 = 1 " + queryWhere;
       return dbgridDataPub.getResultBySize(querySql, begion, pageSize, " ORDER BY C.CREATE_DATE", "满意度修复单历史查询");
   }
    
    private String getOrderListWhere(String map) {
        JSONObject obj = JSONObject.fromObject(map);

        String strwhere = "";
        if (StringUtils.isNotEmpty(obj.optString("orderId"))) {
            strwhere += " AND C.WORK_ORDER_ID='" + obj.optString("orderId") + "'";
        }
        if (StringUtils.isNotEmpty(obj.optString("serviceOrderId"))) {
            strwhere += " AND C.SERVICE_ORDER_ID='" + obj.optString("serviceOrderId") + "'";
        }
        if (StringUtils.isNotEmpty(obj.optString("prodNum"))) {
            strwhere += " AND C.PROD_NUM='" + obj.optString("prodNum") + "'";
        }
        if (StringUtils.isNotEmpty(obj.optString("createType"))) {
            strwhere += " AND C.CREATE_TYPE=" + obj.optString("createType");
        }
        if (StringUtils.isNotEmpty(obj.optString("regionId"))) {
            strwhere += " AND C.REGION_ID=" + obj.optString("regionId");
        }
        if (StringUtils.isNotEmpty(obj.optString("orderLabel"))) {
        	if("0".equals(obj.optString("orderLabel"))) {
        		strwhere += " AND C.BEST_ORDER <= 100122410";
        	} else {
        		strwhere += " AND C.BEST_ORDER > 100122410";
        	}
        }
        if (StringUtils.isNotEmpty(obj.optString("serviceStaff"))) {
        	String logonName = obj.optString("serviceStaff");
			int staffId = pubFunc.getStaffId(logonName);
            strwhere += " AND C.SERVICE_STAFF_ID=" + staffId;
        }
        if (StringUtils.isNotNull(obj.optJSONArray("acceptDate")) && !obj.optJSONArray("acceptDate").isEmpty()) {
            strwhere += " AND C.CREATE_DATE > STR_TO_DATE('" + obj.optJSONArray("acceptDate").optString(0) + "','%Y-%m-%d %H:%i:%s')";
            strwhere += " AND C.CREATE_DATE < STR_TO_DATE('" + obj.optJSONArray("acceptDate").optString(1) + "','%Y-%m-%d %H:%i:%s')";
        }
        return strwhere;
    }
}
