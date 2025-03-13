package com.timesontransfar.evaluation.service.impl;

import java.util.List;

import com.timesontransfar.evaluation.PerceptionInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.util.DateUtil;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.evaluation.Evaluation;
import com.timesontransfar.evaluation.SatisfyInfo;
import com.timesontransfar.evaluation.dao.EvaluationDao;
import com.timesontransfar.evaluation.service.EvaluationService;
import com.timesontransfar.feign.activiti.CustomerJudgeFeign;
import com.timesontransfar.satisfy.common.CommonUtil;
import com.timesontransfar.satisfy.service.SatisfyService;
import com.transfar.common.web.ResultUtil;

@Service
public class EvaluationServiceImpl implements EvaluationService {
	protected Logger log = LoggerFactory.getLogger(EvaluationServiceImpl.class);

	@Autowired
	private CustomerJudgeFeign customerJudgeFeign;
	
	@Autowired
	private EvaluationDao dao;
	
	@Autowired
	private PubFunc pubfunc;
	
	@Autowired
    private SatisfyService satisfy;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;

	@SuppressWarnings("all")
    public String dealResultList(String jsonStr) {
		//解析测评结果
    	List<Evaluation> resultList = JSON.parseArray(jsonStr, Evaluation.class);
    	int sum = 0;
    	int rSum = 0;
    	if(resultList != null && !resultList.isEmpty()) {
    		for(Evaluation result : resultList) {
    			try {
    				log.info("Evaluation: {}", result);
    				if(StringUtils.isEmpty(result.getIsSolve())) {//是否已解决issolve为空时，按照未参评处理
    					log.error("orderId: {} issolve: {}", result.getWorkOrderId(), result.getIsSolve());
    					continue;
    				}
    				
	    			SatisfyInfo info = new SatisfyInfo();
	    			info.setServiceOrderId(result.getWorkOrderId());
	    			info.setEvaluateStatus(1);//集团测评已参评
	    			info.setOrderAssessId(result.getOrderAssessId());
	    			String orderCreateTime = StringUtils.defaultIfEmpty(DateUtil.dateStrPattern(result.getOrderCreateTime(), "yyyy-MM-dd HH:mm:ss"), null);//测评订单创建时间
	    			info.setOrderCreateTime(orderCreateTime);//测评订单创建时间
	    			info.setIsEvaluation(1);//已参评
	    			String assessMode = this.getAssessMode(result);//测评方式
	    			info.setAssessMode(Integer.parseInt(assessMode));
	    			info.setAssessModeDesc(this.getAssessModeDesc(assessMode));
	    			String joinMode = result.getJoinMode();
	    			info.setJoinMode(Integer.parseInt(joinMode));
	    			info.setJoinModeDesc(this.getJoinModeDesc(result.getJoinMode()));
	    			info.setAssessSendTime(DateUtil.dateStrPattern(result.getAssessSendTime(), "yyyy-MM-dd HH:mm:ss"));
	    			info.setUserSubTime(StringUtils.defaultIfEmpty(result.getUserSubTime(), null));
	    			int isSolve = Integer.parseInt(result.getIsSolve());
	    			info.setIsSolve(isSolve);
	    			info.setCheckAssessResult(StringUtils.defaultIfEmpty(result.getCheckAssessResult(), null));
	    			int score = Integer.parseInt(StringUtils.defaultIfEmpty(result.getCheckAssessScore(), "0"));
	    			info.setCheckAssessScore(score);
	    			String reason = this.getSatisfiedReason(result.getSatisfiedReason());
	    			info.setSatisfiedReason(StringUtils.defaultIfEmpty(reason, null));
	    			info.setGrid(StringUtils.defaultIfEmpty(result.getGrid(), null));
	    			info.setChannelCode(StringUtils.defaultIfEmpty(result.getChannelCode(), null));
	    			info.setChannelName(StringUtils.defaultIfEmpty(result.getChannelName(), null));
	    			info.setProvinceAssessResult(StringUtils.defaultIfEmpty(result.getProvinceAssessResult(), null));
	    			int createFlag = this.getCreateFlag(isSolve, score);
	    			info.setCreateFlag(createFlag);
	    			
	    			if("2".equals(StringUtils.defaultIfBlank(result.getDataType(), "1"))) {//下发数据类型（1：首次测评下发；2：修复单(二次测评)测评下发）
	    				int num = dao.insertPayReturnInfo(info);
	    				rSum += num;
	    				log.info("insertPayReturnInfo 更新结果: {}", num);
	    				continue;
	    			}
	    			
	    			//更新测评结果
	    			int num = dao.updateSatisfyInfo(info);
	    			sum += num;
	    			log.info("updateSatisfyInfo 更新结果: {}", num);
	    			
	    			if(num > 0) {//成功更新测评结果
	    				//生成修复单
		    			this.createSatisfyOrder(createFlag, info);
	    				
	    				//处理省内工单
	    				this.dealSatisfyResult(result.getWorkOrderId(), isSolve, score, reason, joinMode);
	    			}
    			} catch (Exception e) {
    	    		log.error("dealResult异常 workOrderId: {} {}", result.getWorkOrderId(), e.getMessage(), e);
    			}
    		}
    	}
    	log.info("测评结果size: {}, 首次测评: {}, 二次测评: {}", resultList.size(), sum, rSum);
    	return ResultUtil.success();
    }
    
    private String getAssessMode(Evaluation result) {
    	String assessMode = "0";
    	if(StringUtils.isNotBlank(result.getAssessMode())) {//测评方式
    		assessMode = result.getAssessMode();
    	} else if(StringUtils.isNotBlank(result.getAssessMode1())) {//测评方式1
    		assessMode = result.getAssessMode1();
    	}
    	return assessMode;
    }
    
    private String getAssessModeDesc(String assessMode) {
    	//测评方式编码（1、在线方式：短信+微信+APP 2、智能外呼 3、人工外呼）
    	String assessModeDesc = "";
		switch (assessMode) {
	        case "1":
	        	assessModeDesc = "在线方式：短信+微信+APP";
	            break;
	        case "2":
	        	assessModeDesc = "智能外呼";
	            break;
	        case "3":
	        	assessModeDesc = "人工外呼";
	            break;
	        default:
                break;
	    }
		return assessModeDesc;
	}
    
    private String getJoinModeDesc(String joinMode) {
    	//参评方式编码（10：短信、11：微信、12：APP；2：智能外呼；3：人工外呼）
    	String joinModeDesc = "";
		switch (joinMode) {
	        case "10":
	        	joinModeDesc = "短信";
	            break;
	        case "11":
	        	joinModeDesc = "微信";
	            break;
	        case "12":
	        	joinModeDesc = "APP";
	            break;
	        case "2":
	        	joinModeDesc = "智能外呼";
	            break;
	        case "3":
	        	joinModeDesc = "人工外呼";
	            break;
	        default:
                break;
	    }
		return joinModeDesc;
	}
    
    private String getSatisfiedReason(String reason) {
    	if(reason != null && reason.length() > 255) {
    		reason = reason.substring(0, 248) + " ......";
    	}
    	return reason;
    }
    
    private int getCreateFlag(int isSolve, int score) {
    	int createFlag = 0;//是否生成修复单（0-否；1-是）
    	if(isSolve == 0 || isSolve == 2) {
    		createFlag = 1;
    	}
    	if(score < 9) {
    		createFlag = 1;
    	}
    	return createFlag;
    }
    
    private void dealSatisfyResult(String serviceOrderId, int isSolve, int score, String reason, String joinMode) {
    	try {
	    	String assessCode = "";
	    	String assessMsg = "";
	    	if(isSolve == 0) {//是否已解决（0未解决，1已解决，2不清楚）
	    		assessCode = "1";
	    		assessMsg = "未解决";
	    	}
	    	else if(isSolve == 2) {
	    		assessCode = "2";
	    		assessMsg = "不清楚";
	    	}
	    	else if(score < 9) {
	    		assessCode = "3";
	    		assessMsg = "本次测评"+score+"分，需改进的方面："+reason;
	    	}
	    	else {
	    		assessCode = "4";
	    		assessMsg = "本次测评"+score+"分";
	    	}
	    	JSONObject json = new JSONObject();
	    	json.put("serviceOrderId", serviceOrderId);
	    	json.put("assessCode", assessCode);
	    	json.put("assessMsg", assessMsg);
	    	json.put("joinMode", joinMode);
	    	customerJudgeFeign.retrieveEvaluation(json.toJSONString());
    	}
    	catch(Exception e) {
			log.error("dealSatisfyResult orderId: {} 异常: {}", serviceOrderId, e.getMessage(), e);
		}
    }
    
    private void createSatisfyOrder(int createFlag, SatisfyInfo info) {
    	if(createFlag == 1) {
    		log.info("生成不满意修复单 {}", info);
    		
    		String cpOrderId = satisfy.createSatisfyOrder(info);
    		log.info("{} cpOrderId: {}", StringUtils.isNotBlank(cpOrderId) ? "成功" : "失败", cpOrderId);
    	}
    }
    
    public boolean insertSatisfyInfoYDX(SatisfyInfo info, String relaInfo, String prodNum) {
    	log.info("insertSatisfyInfoYDX info: {}", info);
    	TsmStaff staff = pubfunc.getLogonStaff();
    	info.setDealStaff(Integer.parseInt(staff.getId()));
    	info.setDealLogonname(staff.getLogonName());
    	info.setDealStaffName(staff.getName());
    	info.setDealOrgId(staff.getOrganizationId());
    	info.setDealOrgName(staff.getOrgName());
    	//黑名单号码
    	String blackPhone = dao.getBlackPhone(relaInfo, prodNum);
    	String isInvited = "Y";//邀评情况（Y-集团测评；21-用户要求不测评；0-未获取到集团编码）
    	boolean blackFlag = false;//是否黑名单号码
    	if(StringUtils.isNotEmpty(blackPhone)) {
    		isInvited = "21";
    		blackFlag = true;
    	} else {
    		isInvited = this.getIsInvited(info);
    	}
    	info.setIsInvited(isInvited);
    	info.setEvaluateStatus(this.getEvaluateStatus(info));
    	info.setCreateFlag(this.getCreateFlag(info));
    	
    	int num = dao.insertSatisfyInfo(info);
    	log.info("insertSatisfyInfoYDX 更新结果: {}", (num > 0 ? "成功" : "失败"));
    	if(num > 0 && blackFlag) {
    		//保存即时测评黑名单拦截记录
			dao.saveReturnBlackLog(info.getServiceOrderId(), relaInfo, prodNum, blackPhone, "");
    	}
    	return blackFlag;
    }

	public int insertSatisfyInfoZDX(SatisfyInfo info, String relaInfo, String prodNum) {
		int res = 0;// 0-默认值，1-黑名单，2-即时测评
		String isInvited = "Y";// 邀评情况（Y-集团测评；21-用户要求不测评；0-未获取到集团编码）
		boolean blackFlag = false;// 是否黑名单号码
		log.info("insertSatisfyInfoZDX info: {}", info);
		String blackPhone = dao.getBlackPhone(relaInfo, prodNum);// 黑名单号码
		if (StringUtils.isNotEmpty(blackPhone)) {
			res = 1;
			isInvited = "21";
			blackFlag = true;
		} else {
			isInvited = this.getIsInvited(info);
		}
		TsmStaff staff = pubfunc.getLogonStaff();
		String orgId = staff.getOrganizationId();
		int num = 0;
		if (pubfunc.isAffiliated(orgId, "361143") && "Y".equals(isInvited)) {// 判断当前处理员工归属于省投诉，终定性工单提交后，增加即时测评拦截记录
			num = sheetPubInfoDao.insertCustomerJudge(info.getServiceOrderId(), info.getUnifiedComplaintCode(), 2);
			log.info("insertCustomerJudge 更新结果: {}", (num > 0 ? "成功" : "失败"));
			res = 2;
		} else {
			info.setDealStaff(Integer.parseInt(staff.getId()));
			info.setDealLogonname(staff.getLogonName());
			info.setDealStaffName(staff.getName());
			info.setDealOrgId(orgId);
			info.setDealOrgName(staff.getOrgName());
			info.setIsInvited(isInvited);
			info.setEvaluateStatus(this.getEvaluateStatus(info));
			info.setCreateFlag(this.getCreateFlag(info));
			num = dao.insertSatisfyInfo(info);
			log.info("insertSatisfyInfoZDX 更新结果: {}", (num > 0 ? "成功" : "失败"));
			if (num > 0 && blackFlag) {
				dao.saveReturnBlackLog(info.getServiceOrderId(), relaInfo, prodNum, blackPhone, "");// 保存即时测评黑名单拦截记录
			}
		}
		return res;
	}

	public int insertSatisfyInfoJSCP(SatisfyInfo info, String relaInfo, String prodNum) {
		log.info("insertSatisfyInfoJSCP orderId: {} info: {}", info.getServiceOrderId(), info);
		String isInvited = info.getIsInvited();
		if (!"Y".equals(isInvited)) {
			info.setIsInvited("21");
		}
		info.setEvaluateStatus(this.getEvaluateStatus(info));
		info.setCreateFlag(this.getCreateFlag(info));
		int num = dao.insertSatisfyInfo(info);
		log.info("insertSatisfyInfoJSCP orderId: {} 更新结果: {}", info.getServiceOrderId(), (num > 0 ? "成功" : "失败"));
		if(num > 0 && (!"Y".equals(isInvited))) {
			dao.saveReturnBlackLog(info.getServiceOrderId(), relaInfo, prodNum, "", isInvited);// 保存即时测评黑名单拦截记录
		}
		return num;
	}

    private String getIsInvited(SatisfyInfo info) {
    	//邀评情况（Y-集团测评；21-用户要求不测评；0-未获取到集团编码）
    	String isInvited = "Y";
    	if(info.getContactStatus() == 1 || info.getRequireUninvited() == 1 || info.getIsRepeat() == 1 || info.getIsUpRepeat() == 1) {
    		isInvited = "21";
    	}
    	else if(StringUtils.isBlank(info.getUnifiedComplaintCode())) {
    		isInvited = "0";
    	}
    	return isInvited;
    }
    
    private int getEvaluateStatus(SatisfyInfo info) {
    	//参评状态（0-省内未邀评；1-集团测评已参评；2-集团测评超时未参评；3-集团未邀评，暂时获取不到此类数据；9-未到测评截止时间）
    	int evaluateStatus = 9;
    	if("21".equals(info.getIsInvited())) {//用户要求不测评
    		evaluateStatus = 0;
    	}
    	return evaluateStatus;
    }
    
    private int getCreateFlag(SatisfyInfo info) {
    	//是否生成修复单（0-否；1-是；9-未到测评截止时间）
    	int createFlag = 9;
    	if(info.getEvaluateStatus() == 0) {//省内未邀评
    		createFlag = 0;
    	}
    	return createFlag;
    }
    
    public int dealSatisfyOverTime(String orderId) {
    	log.info("dealSatisfyOverTime orderId: {}", orderId);
    	SatisfyInfo info = new SatisfyInfo();
    	info.setServiceOrderId(orderId);
    	info.setEvaluateStatus(2);//集团测评超时未参评
    	info.setIsEvaluation(0);//未参评
    	info.setCreateFlag(0);//不生成修复单
    	
    	//更新测评结果超时
		int num = dao.updateSatisfyOverTime(info);
		log.info("updateSatisfyOverTime orderId: {} 更新结果: {}", orderId, (num > 0 ? "成功" : "失败"));
		return num;
    }

	@Override
	public String createPerceptionOrder(String jsonStr) {
		String result = null;
		try {
			log.info("生成感知修复单 {}", jsonStr);
			PerceptionInfo perceptionInfo = JSON.toJavaObject(JSON.parseObject(jsonStr), PerceptionInfo.class);
			String checkMsg = CommonUtil.checkPerceptionInfo(perceptionInfo);
			if(StringUtils.isNotBlank(checkMsg)) {
				return ResultUtil.error("-1", checkMsg);
			}
			
			String cpOrderId = satisfy.createPerceptionOrder(perceptionInfo);
			if (StringUtils.isNotBlank(cpOrderId)){
				result = ResultUtil.success(cpOrderId);
			}else {
				result = ResultUtil.error("-1", "生成感知修复单失败");
			}
			log.info("{} cpOrderId: {}", StringUtils.isNotBlank(cpOrderId) ? "成功" : "失败", cpOrderId);
		}catch (Exception e){
			log.error("createPerceptionOrder异常 {}", jsonStr, e.getMessage(), e);
			result = ResultUtil.error("-1", "生成感知修复单异常");
		}
		return result;
	}
}
