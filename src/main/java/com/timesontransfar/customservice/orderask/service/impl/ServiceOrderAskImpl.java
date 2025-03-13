/**
 * <p>类名：ServiceOrderAskImpl.java</p>
 * <p>功能描叙：业务受理功能实现类</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：1、增加定单预警/告警查询方法getServOrderByAlarm cjw April 9, 2008</p>
 * <p>  Create by:	lifeng	Mar 19, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.orderask.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.async.AsyncTask;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.workflow.IWorkFlowAttemper;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.dbgridData.IdbgridDataTs;
import com.timesontransfar.customservice.especiallyCust.IespeciallyCustDao;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.labelmanage.service.ILabelManageService;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.dao.IpersonaDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.CallSummary;
import com.timesontransfar.customservice.orderask.pojo.CustomerPersona;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServContentInstance;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.pojo.YNSJOrder;
import com.timesontransfar.customservice.orderask.service.ITrackService;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.customservice.orderask.service.ServiceOrderQuery;
import com.timesontransfar.customservice.paramconfig.pojo.SheetLimitTimeCollocate;
import com.timesontransfar.customservice.paramconfig.service.IsheetLimitTimeService;
import com.timesontransfar.customservice.tuschema.dao.IserviceContentTypeDao;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSaveSJ;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetMistakeDAO;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedReturnDAOImpl;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.ServiceWorkSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TSOrderMistake;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.labelLib.service.ILabelService;
import com.timesontransfar.sheetHandler.CompatHandler;
import com.transfar.common.enums.ResultEnum;
import com.transfar.common.exception.MyOwnRuntimeException;
import com.transfar.common.utils.StringUtils;
import com.transfar.common.web.ResultUtil;

@Component("ServiceOrderAskImpl__FACADE__")
public class ServiceOrderAskImpl implements IserviceOrderAsk {
 
    private static final Logger logger = LoggerFactory.getLogger(ServiceOrderAskImpl.class);
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;
    @Autowired
    private IorderCustInfoDao orderCustDao;
    @Autowired
    private IserviceContentDao servContentDao;
    @Autowired
    private PubFunc pubFun;
    @Autowired
    @Qualifier("WorkFlowAttemper__FACADE__")
    private IWorkFlowAttemper workFlowAttemperImpl;
    @Autowired
    private IsheetLimitTimeService sheetLimitTimeServ;
    @Autowired
    private ISheetPubInfoDao sheetPubInfoDao;
    @Autowired
    private ISheetActionInfoDao sheetActionInfoDao;
    @Autowired
    private InoteSenList noteSen;
    @Autowired
    private ILabelManageDAO labelManageDao;
    @Autowired
    private ILabelService labelServiceImpl;
    @Autowired
    private IserviceContentSchem serviceContentSchem;
    @Autowired
    private CmpUnifiedReturnDAOImpl cmpUnifiedReturnDAOImpl;
    @Autowired
    private IserviceContentTypeDao serviceContentType;
    
    @Autowired
    private ISheetMistakeDAO sheetMistakeDAO;
    
	@Autowired
	private IdbgridDataPub dbgridDataPub;

	@Autowired
	private IpersonaDao personaDao;
	@Autowired
	private IespeciallyCustDao especiallyCustDao;
	@Autowired
	private AsyncTask asyncTask;
	@Autowired
	private IdbgridDataTs dbgridDataTs;//投诉列表
	@Autowired
	private ServiceOrderQuery orderQury;
	@Autowired
	private ILabelManageService labelManageService;
	@Autowired
	private ITrackService trackServiceImpl;
	@Autowired
	private CustomerServiceFeign customerServiceFeign;

	public String checkSelect(ServiceContent servContent, int serviceDate) {
		int prodId = servContent.getAppealProdId();
		int reasonId = servContent.getAppealReasonId();
		if (prodId == 0 || servContent.getAppealProdName() == null) {
			return "ERROR";
		}
		if (reasonId == 0 || servContent.getAppealReasonDesc() == null) {
			return "ERROR";
		}
		if (servContentDao.checkPubReference(prodId, reasonId) == 0) {
			return "ERROR";
		}
		return "SUCCESS";
	}

    /*
     * 暂存工单删除方法
     */
    public boolean deleteHoldOrder(String orderId, Integer month) {
    	TsmStaff staff = this.pubFun.getLogonStaff();
    	logger.info("deleteHoldOrder staff: {} orderId: {}", staff.getLogonName(), orderId);
        
        // 删除当前表
        OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskObj(orderId, month, false);
        if (orderAskInfo == null) {
        	logger.info("deleteHoldOrder order: {} {}", orderId, "当前表不存在该工单");
        	return false;
        } else {
            int orderStatu = orderAskInfo.getOrderStatu();
            boolean isBack = (orderStatu == StaticData.OR_BACK_STATU || orderStatu == StaticData.OR_BACK_STATU_NEW); // 是否是派单环节退回定单
            boolean isHold = (orderStatu == StaticData.OR_HOLD_STATU || orderStatu == StaticData.SERV_TYPE_SLZC); // 是否是暂存订单
            if(!(isBack || isHold)){//不是暂存和退回，直接返回失败
                return false;
            }
        }
        
        int count = 0;
        count += servContentDao.delServContent(orderId, month);
        count += orderAskInfoDao.delOrderAskInfo(orderId, month);
        count += orderCustDao.delOrderCustInfo(orderAskInfo.getCustId(), month);

        if (count != 3) {
        	logger.error("删除当前表的暂存信息出问题 count: {}", count);
            return false;
        }
        return true;
    }

    /**
     * 根据受理单的单号查询此受理单的相关信息
     * 
     * @param orderId
     *            受理单号
     * @param hisFlag
     *            当前、历史标识
     * @return 受理单对像
     */
    public ServiceOrderInfo getServOrderInfo(String orderId, boolean hisFlag) {
        // 受理信息
        OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, hisFlag);

        if (orderAskInfo == null) {
        	logger.error("没有查到受理单的受理信息 orderId: {} hisFlag: {}", orderId, hisFlag);
            return null;// 如果没有受单号信息则返回空
        }

        // 受理内容
        ServiceContent servContent = servContentDao.getServContentByOrderId(orderId, hisFlag, orderAskInfo.getOrderVer());
        // 受理客户信息
        String custGuid = orderAskInfo.getCustId();
        OrderCustomerInfo orderCust = orderCustDao.getOrderCustByGuid(custGuid, hisFlag);

        ServiceOrderInfo servOrderInfo = new ServiceOrderInfo();
        servOrderInfo.setOrderAskInfo(orderAskInfo);
        if (servContent != null) {
            servOrderInfo.setServContent(servContent);
        } else {
            servOrderInfo.setServContent(new ServiceContent());
        }
        if (orderCust != null) {
            servOrderInfo.setOrderCustInfo(orderCust);
        } else {
            servOrderInfo.setOrderCustInfo(new OrderCustomerInfo());
        }
        return servOrderInfo;
    }
    
    /**
     * 服务受理、疑难号百、业务预受理、商机管理
     */
    @Transactional
    public String holdServiceOrderInstance(OrderCustomerInfo custInfo, ServiceContent servContent, OrderAskInfo orderAskInfo) {
        // 判断是否有未竣工的工单
        if (orderAskInfo.getServType() == StaticData.SERV_TYPE_YS || orderAskInfo.getServType() == StaticData.SERV_TYPE_SJ) {
        	boolean check = checkOrderSubmit(orderAskInfo.getProdNum(), orderAskInfo.getServType(), orderAskInfo.getRegionId());
            if (check) {
                return ResultUtil.error(ResultEnum.FALL_HAVE_ORDERSHEET);
            }
        }
        // 判断是否为第一次保存
        String orderId = orderAskInfo.getServOrderId();
        Integer month = this.pubFun.getIntMonth();
        boolean isNewCmp = orderAskInfo.getServType() == StaticData.SERV_TYPE_NEWTS;
        int orderStatu = isNewCmp ? StaticData.SERV_TYPE_SLZC : StaticData.OR_HOLD_STATU;
        if ("".equals(orderId)) {// 第一次保存
            orderAskInfo.setMonth(month);
            servContent.setMonth(month);
            custInfo.setMonth(month);
            orderId = this.saveServiceOrder(custInfo, servContent, orderAskInfo, orderAskInfo.getZhzdFlag());
            orderAskInfoDao.updateOrderStatu(orderId, orderStatu, month, pubFun.getStaticName(orderStatu));
        } else {// 非第一次保存
        	OrderAskInfo currentInfo = orderAskInfoDao.getOrderAskObj(orderId, month, false);
        	//判断服务单状态
        	boolean isHold = (currentInfo.getOrderStatu() == StaticData.OR_HOLD_STATU || currentInfo.getOrderStatu() == StaticData.SERV_TYPE_SLZC); // 是否是暂存订单
            if(!isHold){//不是暂存，直接返回失败
            	return ResultUtil.error(ResultEnum.FALL_HAVE_ORDERSHEET);
            }
        	
            orderId = this.updateServiceOrder(custInfo, servContent, orderAskInfo);
            // 保存投诉内容,如果不是第一次就先删除再添加
            orderAskInfoDao.updateOrderStatu(orderId, orderStatu, month, pubFun.getStaticName(orderStatu));
        }
        return ResultUtil.success(orderId);
    }

    /**
     * 重复受理、疑难工单受理、集团工单受理
     */
    @SuppressWarnings("all")
	public String submitServiceOrderInstance(ServContentInstance[] instance,
            OrderCustomerInfo custInfo, ServiceContent servContent, OrderAskInfo orderAskInfo, Map otherInfo, ComplaintInfo compInfo) {
        int regionId = orderAskInfo.getRegionId();
        int servType = orderAskInfo.getServType(); // 性质类别
        boolean isBack = false;
        boolean isHold = false;
        String orderId = orderAskInfo.getServOrderId(); // 定单号
        if (!("".equals(orderId))) { // 去掉重复提交的异常
            OrderAskInfo curOrderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
            if (curOrderAskInfo == null) {
                orderId = "";
            } else {
                int orderStatu = curOrderAskInfo.getOrderStatu();
                isBack = (orderStatu == StaticData.OR_BACK_STATU || orderStatu == StaticData.OR_BACK_STATU_NEW); // 是否是派单环节退回定单
                isHold = (orderStatu == StaticData.OR_HOLD_STATU || orderStatu == StaticData.SERV_TYPE_SLZC); // 是否是暂存订单
                if(!(isBack || isHold)){//不是暂存和退回，直接返回
                    return orderId;
                }
            }
        }

        // 对于预受理单、商机单，如果该产品号码下有未竣工的工单，则不予受理。后台退回的工单不考虑该情况
        if ((StaticData.SERV_TYPE_YS == servType || StaticData.SERV_TYPE_SJ == servType) && !isBack) {
            boolean check = checkOrderSubmit(orderAskInfo.getProdNum(), servType, regionId);
            if (check) {
                return "ORDERNOTFINISH";
            }
        }

        String sendFlag = "false"; // 是否需要发送短信
        boolean send = false; // 记录短信是否发送成功
        if (otherInfo.containsKey("SENDFLAG")) {
            sendFlag = otherInfo.get("SENDFLAG").toString();
        }

        String result = "";
        int isUnified = 0;
        if ("".equals(orderId)) { // 定单号为空说明是首次提交的定单
            // 首次提交的订单需要设置月分区
            Integer month = pubFun.getIntMonth();
            custInfo.setMonth(month);
            servContent.setMonth(month);
            orderAskInfo.setMonth(month);
            // 提交受理信息，生成新订单、新工单等信息
            result = submitNewOrder(custInfo, servContent, orderAskInfo, otherInfo);
            if ("true".equals(sendFlag)) {
                send = sendNoteCont(orderAskInfo.getRelaInfo(), regionId, MESSAGE_CONTENT,
                        orderAskInfo.getAskStaffId(), result);
            }
        } else {
            if (isHold) {
                result = submitHoldOrder(custInfo, servContent, orderAskInfo, otherInfo);
                if ("true".equals(sendFlag)) {
                    send = this.sendNoteCont(orderAskInfo.getRelaInfo(), regionId, MESSAGE_CONTENT,
                            orderAskInfo.getAskStaffId(), result);
                }
            } else if (isBack) { // 退回单的提交
                result = this.submitBackOrder(custInfo, servContent, orderAskInfo, otherInfo);
            }
        }

        if ("".equals(result)) {
            logger.warn("在submitServiceOrder方法中,没有进入任何方法体,可能是传进的参数有问题,请查证!");
        } else {
            // 如果是投诉大类的订单，且不是退单，则向标签库插入一条新记录
            if (orderAskInfo.getServiceDate() == 3 && !isBack) {
            	labelManageDao.insertNew(result);
				ServiceLabel label = new ServiceLabel(result);
				label.setIsUnified(isUnified);
				
				CustomerPersona persona = new CustomerPersona();
				if (pubFun.isNewWorkFlow(result)) {
					updateLimitTime(label, persona, orderAskInfo, servContent);
				}
                if(otherInfo.get("STRFLOW")!=null){
                	String flow = otherInfo.get("STRFLOW").toString();
                	if(!"DISPATCHSHEET".equals(flow)){
                		label.setFormalAnswerDate(result);
                		if("FINASSESS".equals(flow)){//2014-03-06投诉单直接办结，更新最后一次部门处理提交时间
                			label.setLastAnswerDate(result);
                		}
                	}
                }
                if (send) { // 如果短信发送成功，则记录首次回复时间
                	label.setFirstRevertDate(result);
                }
                //更新标识
				labelManageDao.updateServiceLabel(label);
				//10000号来单记录
				this.updateCallFlag(orderAskInfo);
				ServiceOrderInfo soInfo = buildServiceOrderInfo(orderAskInfo, custInfo, servContent);
				asyncTask.submitServiceOrderAsync(result, label, persona, soInfo, null, compInfo);
            }
        }
        logger.info("submitServiceOrderInstance result: {}", result);
        return result;
    }
    
	/**
	 * 商机管理提交
	 */
	@SuppressWarnings({ "rawtypes" })
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String submitServiceOrderInstanceWithLabel(OrderCustomerInfo custInfo, ServiceContent servContent,
			OrderAskInfo orderAskInfo, Map otherInfo, YNSJOrder order, BuopSheetInfo info) {
		int regionId = orderAskInfo.getRegionId();
		int servType = orderAskInfo.getServType(); // 性质类别
		boolean isHold = false;
		String orderId = orderAskInfo.getServOrderId(); // 定单号
		if (!("".equals(orderId))) { // 去掉重复提交的异常
			OrderAskInfo curOrderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
			if (curOrderAskInfo == null) {
				orderId = "";
			} else {
				int orderStatu = curOrderAskInfo.getOrderStatu();
				isHold = orderStatu == StaticData.OR_HOLD_STATU; // 是否是暂存订单
				if (!isHold) {// 不是暂存和退回，直接返回
					return ResultUtil.error("ORDERNOTFINISH");
				}
			}
		}
		
		String checkResult = this.checkReferIdMap(order, otherInfo);
		if(null != checkResult) {
			return ResultUtil.error(checkResult);
		}
		
		// 对于商机单，如果该产品号码下有未竣工的工单，则不予受理。
		boolean check = checkOrderSubmit(orderAskInfo.getProdNum(), servType, regionId);
		if (check) {
			return ResultUtil.error("ORDERNOTFINISH");
		}
		
		String result = "";
		if ("".equals(orderId)) { // 定单号为空说明是首次提交的定单
			// 首次提交的订单需要设置月分区
			Integer month = pubFun.getIntMonth();
			custInfo.setMonth(month);
			servContent.setMonth(month);
			orderAskInfo.setMonth(month);
			// 提交受理信息，生成新订单、新工单等信息
			result = submitNewOrder(custInfo, servContent, orderAskInfo, otherInfo);
            asyncTask.updateCustType(result, orderAskInfo.getProdNum(), regionId);
		} else {
			result = submitHoldOrder(custInfo, servContent, orderAskInfo, otherInfo);
            asyncTask.updateCustType(result, orderAskInfo.getProdNum(), regionId);
		}
		
		logger.info("受理成功 产品号码: {} 商机单号: {}", order.getProdNum(), result);
		if(!"".equals(result)) {
			// 保存商机单信息
	        this.saveBuopSheetInfo(info, result, true);
	        
			// 判断是否特殊商机单
			order.setServOrderId(result);
			dealSJOrder(order, custInfo, orderAskInfo.getAskStaffId());
			
			return ResultUtil.success(result);//成功受理
		}
		return ResultUtil.error("");
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String checkReferIdMap(YNSJOrder order, Map otherInfo) {
		//商机单受理时，需要根据目录校验相关信息
		Map checkMap = pubFun.getReferIdMap(order.getAppealReasonId());
		logger.info("appeal_reason_id: {}, checkMap: {}", order.getAppealReasonId(), checkMap);
		if(checkMap != null) {
			//校验产品实例ID是否存在
			if("1".equals(this.getStringByKey(checkMap, "IS_CHECK_BSS")) 
					&& "".equals(order.getProdInstId())){
				logger.info("产品实例ID不存在");
				return "BSS_NOT_EXIST";
			}
			//是否明确商机单 校验标准地址ID是否存在
			if((("1".equals(order.getIsClearBusiness()) && 
					"1".equals(this.getStringByKey(checkMap, "IS_CHECK_BUSINESS"))) || 
					"1".equals(this.getStringByKey(checkMap, "IS_CHECK_ADDRESS"))) 
					&& ("".equals(order.getCountyId()) || "".equals(order.getAddressId()) || "".equals(order.getStandardAddressId()) || "".equals(order.getAddressDesc()))){
				logger.info("标准地址ID不存在");
				return "ADDRESS_NOT_EXIST";
			}
			//客户经理归属为省政企的商机单 且 受理目录属于政企目录，流向省政企
			if("1".equals(checkMap.get("ASSIGN_TO_SZQ") == null ? "" : checkMap.get("ASSIGN_TO_SZQ").toString())){
				otherInfo.put("ASSIGN_TO_SZQ", "1");
			}
		}
		return null;
	}
	
	public void dealSJOrder(YNSJOrder order, OrderCustomerInfo custInfo, int staffId) {
		String operType = pubFun.getOperType(order.getAppealReasonId());
		logger.info("operType:{}", operType);
		if("YNSJ".equals(operType)){//云脑商机单
			order.setOrderType("YNSJ");
		}
		else if("ZHYX".equals(operType)){//智慧营销
			order.setOrderType("ZHYX");
			//智慧营销渠道，产品号码改传产品接入号；如果为null，传产品号码
			order.setProdNum(org.apache.commons.lang3.StringUtils.defaultIfEmpty(custInfo.getFaxNum(), order.getProdNum()));
		}
		else if("ZTSJ".equals(operType)){//中台商机
			order.setOrderType("ZTSJ");
		}
		else if("SJ_SYNC".equals(operType)){//商机管理平台
			order.setOrderType("SJ_SYNC");
		}
		
		if(StringUtils.isNotEmpty(order.getOrderType())) {
			TsmStaff staff = pubFun.getStaff(staffId);
			order.setAccpetStaffCode(staff.getLogonName());
			order.setAcceptStaffName(staff.getName());
			order.setAccpetOrgName(staff.getOrgName());
			order.setProdInstId(org.apache.commons.lang3.StringUtils.defaultIfEmpty(order.getProdInstId(), order.getRealInfo()));
			logger.info("orderId:{} SJOrder:{}", order.getServOrderId(), order);
			int num = orderAskInfoDao.saveYNSJOrder(order);
			logger.info("SJOrder save：{}", num);
		}
	}

	/**
	 * 重复标签、时限
	 */
	private void updateLimitTime(ServiceLabel label, CustomerPersona persona, OrderAskInfo orderAskInfo, ServiceContent servContent) {
		String serviceOrderId = label.getServiceOrderId();
		int servType = orderAskInfo.getServType();
		int comeCategory = orderAskInfo.getComeCategory();
		int bestOrder = servContent.getBestOrder();

		//更新重复标签
		if (StaticData.SERV_TYPE_CX == servType) {
			labelManageService.updateRepeatFlagCX(label, persona, orderAskInfo);
		} else {
			boolean complaintFlag = this.getComplaintFlag(servType, orderAskInfo.getAskChannelId());//申诉投诉
			boolean unifiedFlag = this.getUnifiedFlag(servType);//投诉咨询单
			labelManageService.updateRepeatFlag(label, persona, orderAskInfo, servContent, complaintFlag, unifiedFlag);
		}
		int[] limitTimeNew = this.sheetLimitTimeServ.getLimitTimeNew(servType, comeCategory, label.getRepeatNewFlag(), pubFun.getLastXX(servContent), bestOrder);
		label.setDealHours(limitTimeNew[0]);
		label.setAuditHours(limitTimeNew[1]);
		orderAskInfoDao.updateOrderLimitTime(limitTimeNew[2], serviceOrderId);
		sheetPubInfoDao.updateDealLimitTimeByOrderId(limitTimeNew[0], serviceOrderId);
	}
	
	@SuppressWarnings("rawtypes")
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	@SuppressWarnings("rawtypes")
	private int getIntByKey(Map map, String key) {
		return Integer.parseInt(map.get(key) == null ? "0" : map.get(key).toString());
	}
	
	private boolean getComplaintFlag(int servType, int askChannelId) {
		//投诉单 工信部、省管局
		return servType == StaticData.SERV_TYPE_NEWTS && (askChannelId == 707907026 || askChannelId == 707907027);
	}
	
	private boolean getUnifiedFlag(int servType) {
		//投诉咨询单
		return servType == StaticData.SERV_TYPE_NEWTS || servType == StaticData.SERV_TYPE_ZX;
	}

	/**
	 * 逐条保存暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @param ServiceContentSaveSJ
	 * @return 保存的记录数
	 */
	public int saveContentSaveSJ(String serviceOrderId, ServiceContentSaveSJ[] serviceContentSaveSJs) {
		serviceContentType.deleteContentSaveSJ(serviceOrderId);
		return serviceContentType.insertContentSaveSJ(serviceContentSaveSJs);
	}

	/**
	 * 删除暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @return 更新的记录数
	 */
	public int removeContentSaveSJ(String serviceOrderId) {
		return serviceContentType.deleteContentSaveSJ(serviceOrderId);
	}

	/**
	 * 得到暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @return ServiceContentSaveSJ
	 */
	public ServiceContentSaveSJ[] queryContentSaveSJ(String serviceOrderId) {
		return serviceContentType.selectContentSaveSJ(serviceOrderId);
	}

	/**
	 * 服务受理提交
	 */
    @SuppressWarnings("all")
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String submitServiceOrderInstanceLabelNew(OrderCustomerInfo custInfo, ServiceContent servContent,
			OrderAskInfo orderAskInfo, Map otherInfo, LabelInstance[] ls, ServiceContentSave[] saves, ComplaintInfo compInfo) {
    	int regionId = orderAskInfo.getRegionId();
		int servType = orderAskInfo.getServType(); // 性质类别
		boolean isHold = false;
		String orderId = orderAskInfo.getServOrderId(); // 定单号
		if (!("".equals(orderId))) { // 去掉重复提交的异常
			OrderAskInfo curOrderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
			if (curOrderAskInfo == null) {
				orderId = "";
			} else {
				int orderStatu = curOrderAskInfo.getOrderStatu();
				isHold = (orderStatu == StaticData.OR_HOLD_STATU || orderStatu == StaticData.SERV_TYPE_SLZC); // 是否是暂存订单
				if (!isHold) {// 不是暂存，直接返回
					return ResultUtil.success(orderId);
				}
			}
		}

		String sendFlag = "false"; // 是否需要发送短信
		boolean send = false; // 记录短信是否发送成功
		if (otherInfo.containsKey("SENDFLAG")) {
			sendFlag = otherInfo.get("SENDFLAG").toString();
		}
		String zhzdFlag = "0";
		if (otherInfo.containsKey("ZHZDFLAG")) {
			zhzdFlag = otherInfo.get("ZHZDFLAG").toString();
		}
		this.setDispatchFlag(orderAskInfo, servContent, otherInfo); // 工单直派
		int urgentOrder = trackServiceImpl.getUrgentOrderFlag(orderAskInfo, servContent);// 最严工单改急
		int modifyBestOrder = this.checkRepeatBestOrder(orderAskInfo, servContent);// 最严重复录单
		
		String result = "";
		int isUnified = 0;
		if ("".equals(orderId)) { // 定单号为空说明是首次提交的定单
			// 首次提交的订单需要设置月分区
			Integer month = pubFun.getIntMonth();
			custInfo.setMonth(month);
			servContent.setMonth(month);
			orderAskInfo.setMonth(month);
			// 提交受理信息，生成新订单、新工单等信息
			result = submitNewOrder(custInfo, servContent, orderAskInfo, otherInfo, true);
			//服务类型：投诉、咨询；投诉级别：省内投诉；受理渠道!=营业厅
			if (isTSZXCX(servType) && StaticData.getAskLevelId()[0] == orderAskInfo.getComeCategory() && 707907008 != orderAskInfo.getAskChannelId()) {
				if ("false".equals(sendFlag)) {
					isUnified = cmpUnifiedReturnDAOImpl.saveUnifiedReturn(result, "1", "1");
				} else {
					isUnified = cmpUnifiedReturnDAOImpl.saveUnifiedReturn(result, "0", "0");
				}
				sendFlag = "false";
			}
			if ("true".equals(sendFlag) && 720200002 != orderAskInfo.getServType() && 720200003 != orderAskInfo.getServType()
					&& StaticData.getAskLevelId()[2] != orderAskInfo.getComeCategory()) {//不为特别跟踪、查询、申诉渠道
				if ("1".equals(zhzdFlag)) {
					send = sendNoteCont(orderAskInfo.getRelaInfo(), regionId, MESSAGE_CONTENT, Integer.parseInt(zhzdFlag), result);
				} else {
					send = sendNoteCont(orderAskInfo.getRelaInfo(), regionId, MESSAGE_CONTENT, orderAskInfo.getAskStaffId(), result);
				}
			}
			// 添加标签实例
			labelServiceImpl.saveLabel(orderAskInfo, servContent, custInfo, ls);
		} else {
			if (isHold) {
				result = submitHoldOrder(custInfo, servContent, orderAskInfo, otherInfo);
				if (isTSZXCX(servType) && StaticData.getAskLevelId()[0] == orderAskInfo.getComeCategory() && 707907008 != orderAskInfo.getAskChannelId()) {
					if ("false".equals(sendFlag)) {
						isUnified = cmpUnifiedReturnDAOImpl.saveUnifiedReturn(result, "1", "1");
					} else {
						isUnified = cmpUnifiedReturnDAOImpl.saveUnifiedReturn(result, "0", "0");
					}
					sendFlag = "false";
				}
				if ("true".equals(sendFlag) && 720200002 != orderAskInfo.getServType() && 720200003 != orderAskInfo.getServType()
						&& StaticData.getAskLevelId()[2] != orderAskInfo.getComeCategory()) {
					send = this.sendNoteCont(orderAskInfo.getRelaInfo(), regionId, MESSAGE_CONTENT, orderAskInfo.getAskStaffId(), result);
				}
			}
		}
		if ("".equals(result)) {
			logger.warn("在submitServiceOrder方法中,没有进入任何方法体,可能是传进的参数有问题,请查证!");
		} else {
			//最严重复录单
			this.updateBestOrder(result, modifyBestOrder);
			
			//重复工单
			this.saveTrackOrder(orderAskInfo, servContent, result);
			
			labelManageDao.insertNew(result);
			ServiceLabel label = new ServiceLabel(result);
			label.setOrderType(urgentOrder);//最严改急
			label.setIsUnified(isUnified);
			
			CustomerPersona persona = new CustomerPersona();
			if (pubFun.isNewWorkFlow(result)) {
				updateLimitTime(label, persona, orderAskInfo, servContent);
			}
			if (otherInfo.get("STRFLOW") != null) {
				String flow = otherInfo.get("STRFLOW").toString();
				if (!"DISPATCHSHEET".equals(flow)) {
					label.setFormalAnswerDate(result);
					if ("FINASSESS".equals(flow)) {// 2014-03-06投诉单直接办结，更新最后一次部门处理提交时间
						label.setLastAnswerDate(result);
					}
				}
			}
			if (otherInfo.get("WORKSHEET_ALLOT") != null) {
				String allot = otherInfo.get("WORKSHEET_ALLOT").toString();
				if ("adjust_account".equals(allot)) {// 直接调账
					label.setAdjustAccountFlag(1);
					label.setDirectDispatchFlag(1);
				}
			}
			if (orderAskInfo.getEmergency()!= null && !"".equals(orderAskInfo.getEmergency()) && !"0".equals(orderAskInfo.getEmergency())) {
				int emergency = Integer.parseInt(orderAskInfo.getEmergency());
				label.setUpTendencyFlag(emergency);
			}
			if (send) { // 如果短信发送成功，则记录首次回复时间
				label.setFirstRevertDate(result);
			}
			//更新标识
			labelManageDao.updateServiceLabel(label);
			try {
				//10000号来单记录
				this.updateCallFlag(orderAskInfo);
				//保存建单模板
				serviceContentSchem.saveOrderContents(saves, result);
				ServiceOrderInfo soInfo = buildServiceOrderInfo(orderAskInfo, custInfo, servContent);
				asyncTask.submitServiceOrderAsync(result, label, persona, soInfo, saves, compInfo);
				// 判断录单为政企客户的投诉单，则发送受理短信至该客户的客户经理
				if (720130000 == servType && 700001878 == orderAskInfo.getCustGroup() && (2 != regionId && 999 != regionId)) {
					asyncTask.sendNoteToManagerPhone(result, orderAskInfo.getProdNum(), regionId, custInfo.getProdType(), orderAskInfo.getAskStaffId());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		logger.info("submitServiceOrderInstanceLabelNew result: {}", result);
		return ResultUtil.success(result);
	}
    
    private void updateBestOrder(String orderId, int modifyBestOrder) {
    	if(modifyBestOrder > 0) {
    		pubFun.saveOrderOperation(orderId, 1);
    	}
    }
    
    @SuppressWarnings("rawtypes")
	private int checkRepeatBestOrder(OrderAskInfo order, ServiceContent content) {
    	if(StaticData.SERV_TYPE_NEWTS != order.getServType() || content.getBestOrder() <= 100122410) {
    		return 0;
    	}
    	
    	try {
	    	String repeatDays = pubFun.querySysContolSwitch("repeatBestOrderDays");
	    	logger.info("repeatBestOrderDays: {}", repeatDays);
			if (!org.apache.commons.lang3.StringUtils.isNumeric(repeatDays) || "0".equals(repeatDays)) {
				return 0;
			}
			
			List orderList = orderAskInfoDao.queryRepeatBestOrder(Integer.parseInt(repeatDays), order.getRegionId(), order.getProdNum());
			logger.info("repeatBestOrder: {}", JSON.toJSON(orderList));
			if (orderList.isEmpty()) {
				return 0;
			}
			
			order.setEmergency("4");
			content.setBestOrder(100122410);
			content.setBestOrderDesc("否");
			logger.info("最严重复录单规则调整: {}", order.getProdNum());
			return 1;
    	}
    	catch(Exception e) {
    		logger.error("checkRepeatBestOrder error: {}", e.getMessage(), e);
    	}
    	return 0;
    }

	private ServiceOrderInfo buildServiceOrderInfo(OrderAskInfo order, OrderCustomerInfo cust, ServiceContent content) {
		ServiceOrderInfo soInfo = new ServiceOrderInfo();
		if (order != null) {
			soInfo.setOrderAskInfo(order);
		} else {
			return null;
		}
		if (content != null) {
			soInfo.setServContent(content);
		} else {
			return null;
		}
		if (cust != null) {
			soInfo.setOrderCustInfo(cust);
		} else {
			return null;
		}
		return soInfo;
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void setDispatchFlag(OrderAskInfo orderAskInfo, ServiceContent servContent, Map otherInfo) {
    	if(707907001 == orderAskInfo.getComeCategory() && (StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType() 
    			|| StaticData.SERV_TYPE_CX == orderAskInfo.getServType())) {//省内投诉/查询工单
    		//最终处理意见
    		String lastFinalOptionOrderId = orderAskInfoDao.getLastFinalOptionOrderId(orderAskInfo.getProdNum(), orderAskInfo.getRegionId());
        	if(!"".equals(lastFinalOptionOrderId)) {
        		logger.info("lastFinalOptionOrderId: {}", lastFinalOptionOrderId);
        		otherInfo.put("FINAL_OPTION_FLAG", "true");
        	}
        	
        	//争议渠道是“电子渠道”（除电子渠道-客服型电子渠道-VIP客户服务经理、电子渠道-客服型电子渠道-电话营销）
        	if("120000".equals(servContent.getDisputeChnl1()) && (!"120502".equals(servContent.getDisputeChnl3()) && !"120514".equals(servContent.getDisputeChnl3()))) {
        		logger.info("disputeChnl: {}", servContent.getDisputeChnl1Nm() + "-" + servContent.getDisputeChnl2Nm() + "-" + servContent.getDisputeChnl3Nm());
        		otherInfo.put("ELECTRONIC_CHANNEL", "true");
        	}
    	}
    }
    
    @SuppressWarnings("rawtypes")
	private void saveTrackOrder(OrderAskInfo orderAskInfo, ServiceContent servContent, String result) {
		// 单号：854988 受理渠道为：省服务监督热线；受理信息里面“是否越级”字段：选择有越级倾向或有曝光倾向。受理目录为携号转网的
		boolean cfFlag = false;
		if (707907001 != orderAskInfo.getComeCategory()) { // 非省内
			return;
		}
		
		if (720130000 != orderAskInfo.getServType()) { // 非投诉
			cfFlag = true;
		}
		if (!cfFlag && 707907012 == orderAskInfo.getAskChannelId()) { // 省服务监督热线
			cfFlag = true;
		}
		if (!cfFlag && checkXHZW(servContent.getAppealReasonId())) { // 携号转网
			cfFlag = true;
		}
		if (!cfFlag && Arrays.asList("1", "2", "3").contains(orderAskInfo.getEmergency())) { // 有曝光倾向||有工信部越级倾向或通管局投诉倾向||有越级倾向
			cfFlag = true;
		}
		if (!cfFlag) {
			return;
		}
		List repeatList = pubFun.checkRepeatUnified(String.valueOf(orderAskInfo.getRegionId()), orderAskInfo.getProdNum(), orderAskInfo.getRelaInfo());
		if (!repeatList.isEmpty()) {
			String oldOrderId = "";
			for (int i = 0; i < repeatList.size(); i++) {
				Map repeatMap = (Map) repeatList.get(i);
				oldOrderId = repeatMap.get("SERVICE_ORDER_ID").toString();
				if (!oldOrderId.equals(result)) {
					orderAskInfoDao.saveServiceTrack(result, oldOrderId, 0);
					break;
				}
			}
		}
    }

	// 判断是否为携号转网
	private boolean checkXHZW(int appealReasonId) {
		String str = String.valueOf(appealReasonId);
		return str.startsWith("111") || "23002503".equals(str);
	}
    
    /**
     * 近30天拨打10000号人工记录标签
     * @param orderAskInfo
     */
    private void updateCallFlag(OrderAskInfo orderAskInfo) {
    	if(StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType() || StaticData.SERV_TYPE_CX == orderAskInfo.getServType()) {//投诉查询
    		asyncTask.updateCallFlag(orderAskInfo.getServOrderId(), orderAskInfo.getProdNum(), orderAskInfo.getRelaInfo(), orderAskInfo.getRegionId());
    		//如意用户打标
    		asyncTask.updateCustType(orderAskInfo.getServOrderId(), orderAskInfo.getProdNum(), orderAskInfo.getRegionId());
    	}
    }

    /**
     * 成功受理订单后，向用户发送短信
     * 
     * @param soureNum
     *            短信接收号码
     * @param regionId
     *            受理地域ID
     * @param sendContent
     *            短信内容
     * @param type
     *            订单来源。1000表示来自网厅,10000来自集团转派。
     * @param serviceId
     *            订单ID
     * @return true表示成功发送短信
     */
    @SuppressWarnings("rawtypes")
	private boolean sendNoteCont(String soureNum, int regionId, String sendContent, int type,
            String serviceId) {
        if(type == StaticData.ACPT_STAFFID_JT){
            return false;
        }
        // 取当前登录员工信息
        int staffId = StaticData.ACPT_STAFFID_WT;
        String staffName = StaticData.ACPT_STAFFNAME_WT;
        String orgId = StaticData.ACPT_ORGID_WT;
        String orgName = StaticData.ACPT_ORGNAME_WT;
        if(type != StaticData.ACPT_STAFFID_WT && type != 1){
            TsmStaff staff = this.pubFun.getLogonStaff();
            staffId = Integer.parseInt(staff.getId());
            staffName = staff.getName();
            orgId = staff.getOrganizationId();
            orgName = staff.getOrgName();
        }
        String sheetGuid = this.pubFun.crtGuid();
        if ("".equals(soureNum)) {
            soureNum = " ";
        }
        String fir = soureNum.substring(0, 1);
        String checkNum = soureNum;
        // 如果第一个字不为0,就手动加0
        if (!"0".equals(fir)) {
            checkNum = "0" + soureNum;
        }
        // 1本省电信固话;2本省电信手机;4外省电信移动手机;6外省或其它
        Map map = this.noteSen.getNumInfo(checkNum);
        String numType = map.get("out_numtype").toString();
        if (!numType.equals("1")) {
            NoteSeand noteBean = new NoteSeand();
            noteBean.setSheetGuid(sheetGuid);
            noteBean.setRegionId(regionId);
            noteBean.setDestteRmid(soureNum);
            if ("2".equals(numType) || "4".equals(numType)) {
                noteBean.setClientType(1);
            } else if ("3".equals(numType)) {
                noteBean.setClientType(0);// 终端标志 3本省电信小灵通
            } else if (checkNum.length() == 12) {
                noteBean.setClientType(1);
            } else {
                return false;
            }
            noteBean.setSendContent(sendContent);
            noteBean.setOrgId(orgId);
            noteBean.setOrgName(orgName);
            noteBean.setStaffId(staffId);
            noteBean.setStaffName(staffName);
            noteBean.setBusiId(serviceId);
            this.noteSen.saveNoteContent(noteBean);
            return true;
        }
        return false;
    }

    /**
     * 根据产品码查询该产品一个月内的投诉记录(竣工和未竣工的受理单)（郑远贵修改为两月）
     * 
     * @param prodNum
     *            产品号码
     * @param sql
     *            区分定单类型
     * @return 受理单对像数组
     */
    public OrderAskInfo[] getServOrderProdNumHis(String prodNum, String sql) {
        OrderAskInfo[][] allOrders = this.orderAskInfoDao.getOrderProHis(prodNum, sql);
        OrderAskInfo[] curOrders = allOrders[0];
        OrderAskInfo[] oldOrders = allOrders[1];
        int curSize = 0;
        int oldSize = 0;
        if(curOrders != null){
        	curSize = curOrders.length;
        }
        if(oldOrders != null){
        	oldSize = oldOrders.length;
        }
        int size = curSize + oldSize;
        OrderAskInfo[] orders = new OrderAskInfo[size];
        if(size > 0){
            if(curSize > 0){
                System.arraycopy(curOrders, 0, orders, 0, curSize);
            }
            if(oldSize > 0){
                System.arraycopy(oldOrders, 0, orders, curSize, oldSize);
            }
        }
        return orders;
    }

    public GridDataInfo getServOrderProdNumHis(String parm) {
    	JSONObject json = JSONObject.fromObject(parm);
    	String begion = json.optString("begion");
    	
    	String prodNum = json.optString("prodNum");
    	String prodNum0 = prodNum;
        if (prodNum.substring(0, 1).equals("0")){
        	prodNum = prodNum.substring(1);
        } else {
        	prodNum0 = "0" + prodNum;
        }
        String sql = "SELECT C.SERVICE_ORDER_ID,C.PROD_NUM,DATE_FORMAT(C.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,C.SERVICE_TYPE_DESC,"
        					+ "C.ORDER_STATU_DESC,C.ACCEPT_STAFF_NAME,C.ACCEPT_ORG_NAME,C.CUST_SERV_GRADE_DESC,C.FINISH_DATE FROM (\n" + 
		        		"SELECT * FROM CC_SERVICE_ORDER_ASK WHERE (prod_num = '"+prodNum+"' OR prod_num = '"+prodNum0+"') AND ACCEPT_DATE >= ADDDATE(NOW(),INTERVAL -3 MONTH) \n" +
		        		"UNION ALL\n" + 
		        		"SELECT * FROM CC_SERVICE_ORDER_ASK_HIS WHERE (prod_num = '"+prodNum+"' OR prod_num = '"+prodNum0+"') AND ACCEPT_DATE >= ADDDATE(NOW(),INTERVAL -3 MONTH) AND ORDER_STATU in (700000103, 720130010)) C";
        return dbgridDataPub.getResult(sql, Integer.parseInt(begion), "", "");
    }

    /**
     * 更新服务受理单信息
     * 
     * @param custInfo
     *            受理单客户信息对象
     * @param servContent
     *            受理内容对像
     * @param servOrderInfo
     *            受理信息对像
     * @return 是否成功
     */
	public boolean updateServiceOrderInfo(OrderCustomerInfo custInfo, ServiceContent servContent,
            OrderAskInfo orderAskInfo, String sheetId) {
        // 查最新的受理次数
        String orderId = orderAskInfo.getServOrderId();
        // 取的原定单的基本信息
        OrderAskInfo orderAskObj = this.orderAskInfoDao.getOrderAskObj(orderId,
                orderAskInfo.getMonth(), false);

        int version = orderAskObj.getOrderVer();// 得到当前的版本号
        String oldCustGuid = orderAskObj.getCustId();// 得到当前的客户GUID
        // 先将修改前的相关信息进历史
        Integer monthHis = orderAskObj.getMonth();

        // 重新设置时限
        SheetLimitTimeCollocate limitBean = this.sheetLimitTimeServ.getSheetLimitime(
                orderAskObj.getRegionId(), orderAskObj.getServType(), orderAskObj.getRegionId(), 0,
                orderAskObj.getCustServGrade(), orderAskObj.getUrgencyGrade());
        if (limitBean != null) {
            orderAskInfo.setPreAlarmValue(limitBean.getPrealarmValue());
            orderAskInfo.setOrderLimitTime(limitBean.getLimitTime());
        }

        // 如果是退回工单执行修改操作
        if (StaticData.OR_BACK_STATU == orderAskInfo.getOrderStatu()) {
            orderCustDao.updateCustInfo(custInfo);// 在当前表里存入已经更新的客户信息
            orderAskInfoDao.updateOrderAskInfo(orderAskInfo);
            servContentDao.updateServContent(servContent);
            return true;
        } else {
            // 如果不是执行添加操作
            this.orderCustDao.saveOrderCustHis(oldCustGuid, monthHis);
            this.orderAskInfoDao.saveOrderAskInfoHis(orderId, monthHis);
            this.servContentDao.saveServContentHis(orderId, monthHis);
        }

        // 修改历史定单状态
        String tatuDesc = this.pubFun.getStaticName(StaticData.WKST_MODIFY_ACTION);
        this.orderAskInfoDao.updateOrderHisStatu(orderAskInfo.getRegionId(), orderId,
                StaticData.WKST_MODIFY_ACTION, tatuDesc, monthHis);
        // 更新当修改后的定单内容
        String newCustguid = pubFun.crtGuid();// 重新生成客户guid
        custInfo.setCustGuid(newCustguid);
        orderCustDao.saveOrderCust(custInfo);// 在当前表里存入已经更新的客户信息

        orderAskInfo.setCustId(newCustguid);// 更新受理信息表中的客户guid
        orderAskInfo.setOrderVer(version + 1);// 受理信息版本号加1
        orderAskInfo.setAskCount(orderAskObj.getAskCount());// 得到最新的受理次数
        orderAskInfoDao.updateOrderAskInfo(orderAskInfo);// 更新受理信息

        servContent.setOrderVer(version + 1);// 受理内容版本号加1
        servContentDao.updateServContent(servContent);// 更新受理内容
        // 在当前中删除已经进入历史表的客户信息
        orderCustDao.delOrderCustInfo(oldCustGuid, monthHis);
        
        // 记录工单动作
        int num = updateAction(orderAskObj, sheetId);
        return num > 0;
    }

    /**
     * 工单做保存操作的时候，将操作员工记录在动作表中
     * 
     * @param orderAskInfo
     * @return
     */
    private int updateAction(OrderAskInfo orderAskInfo, String sheetId) {
        if ("".equals(sheetId)) {
            return 0;
        }
        TsmStaff staff = this.pubFun.getLogonStaff();
        int staffId = Integer.parseInt(staff.getId());
        String staffName = staff.getName();
        String orgId = staff.getOrganizationId();
        String orgName = staff.getOrgName();

        // 记录工单动作
        SheetActionInfo sheetActionInfo = new SheetActionInfo();
        String guid = pubFun.crtGuid();
        sheetActionInfo.setActionGuid(guid);
        sheetActionInfo.setWorkSheetId(sheetId);
        int tacheId = StaticData.TACHE_ASSIGN;
        sheetActionInfo.setTacheId(tacheId);
        sheetActionInfo.setTacheName(pubFun.getStaticName(tacheId));
        sheetActionInfo.setActionCode(StaticData.WKST_MODIFY_ACTION);
        sheetActionInfo.setActionName(pubFun.getStaticName(StaticData.WKST_MODIFY_ACTION));
        sheetActionInfo.setOpraOrgId(orgId);
        sheetActionInfo.setOpraOrgName(orgName);
        sheetActionInfo.setOpraStaffId(staffId);
        sheetActionInfo.setOpraStaffName(staffName);
        sheetActionInfo.setRegionId(orderAskInfo.getRegionId());
        sheetActionInfo.setServOrderId(orderAskInfo.getServOrderId());
        sheetActionInfo.setMonth(orderAskInfo.getMonth());
        this.sheetActionInfoDao.saveSheetActionInfo(sheetActionInfo);

        return 1;
    }

    /**
     * 直接提交的定单的提交方法
     * 
     * @param custInfo
     *            客户
     * @param servContent
     *            受理内容
     * @param orderAskInfo
     *            受理信息
     * @return 是否提交成功
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private String submitNewOrder(OrderCustomerInfo custInfo, ServiceContent servContent,
            OrderAskInfo orderAskInfo, Map otherInfo, boolean... newFlag) {
        String dealOrg = "";
        String dealStaffId = "";
        String zhzdFlag = "0";
		if (otherInfo.containsKey("ZHZDFLAG")) {
			zhzdFlag = otherInfo.get("ZHZDFLAG").toString();
		}
		if ("1".equals(zhzdFlag) || orderAskInfo.getAskStaffId() == StaticData.ACPT_STAFFID_WT 
				|| orderAskInfo.getAskStaffId() == StaticData.ACPT_STAFFID_JT) {
            dealStaffId = String.valueOf(orderAskInfo.getAskStaffId());
            dealOrg = orderAskInfo.getAskOrgId();
		} else {
			TsmStaff staff = pubFun.getLogonStaff();
            dealStaffId = staff.getId();
            dealOrg = staff.getOrganizationId();
		}
        Map inParam = getRouteValue(otherInfo, orderAskInfo.getServType());
        if(custInfo.getOdsCity() != null && orderAskInfo.getServiceDate() == 1 && custInfo.getOdsCity().equals("Y")){
        	// 客户经理归属地为省政企、商机单
        	// 增加政企目录判断
        	if(otherInfo.containsKey("ASSIGN_TO_SZQ") &&  "1".equals(otherInfo.get("ASSIGN_TO_SZQ").toString())) {
            	inParam.put("SEND_TO_ORG_ID",StaticData.ODS_ORG_ID);
                inParam.put("SEND_TO_ORG_NAME",this.pubFun.getOrgName(StaticData.ODS_ORG_ID));
        	}
        }else if(orderAskInfo.getServType() == StaticData.SERV_TYPE_GZ) {
        	// 跟踪单直派
        	// SEND_TO_OBJ_FLAG 1-部门 0-员工
        	inParam.put("SEND_TO_OBJ_FLAG",otherInfo.get("SEND_TO_OBJ_FLAG"));
        	inParam.put("SEND_TO_OBJ_ID",otherInfo.get("SEND_TO_OBJ_ID"));
        }else {
            orderAskInfo.setSendToOrgId("");
            orderAskInfo.setSendToOrgName("");
        }

        // 保存定单相关的信息
        String orderId = this.saveServiceOrder(custInfo, servContent, orderAskInfo, zhzdFlag, newFlag);

        // 组装工作流入参
        String regionId = "" + orderAskInfo.getRegionId();
        inParam.put("WF__REGION_ID", regionId);
        inParam.put("SERV_ORDER_ID", orderId);
        inParam.put("MONTH_FALG", orderAskInfo.getMonth());
        inParam.put("DEAL_PR_ORGID", dealOrg);// 派发部门
        inParam.put("DEAL_PR_STAFFID", dealStaffId);// 派发员工
        inParam.put("FLOW_SEQUENCE", "1");// 新流程提交时，流程顺序号为1

        if (otherInfo.containsKey("WORKSHEET_ALLOT")) {// 直派单
            inParam.put("WORKSHEET_ALLOT", otherInfo.get("WORKSHEET_ALLOT"));
        } else if(otherInfo.containsKey("FINAL_OPTION_FLAG")) {// 最终处理意见
            inParam.put("WORKSHEET_ALLOT", "final_option");
        } else if(otherInfo.containsKey("ELECTRONIC_CHANNEL")) {// 电子渠道
            inParam.put("WORKSHEET_ALLOT", "electr_channel");
        }
        if (otherInfo.containsKey("ORDER_FLAG")) {// 订单标识
            inParam.put("ORDER_FLAG", otherInfo.get("ORDER_FLAG"));
        }
        activeWorkFlow(orderAskInfo.getServiceDate(),orderAskInfo.getServType(),inParam);
        return orderId;
    }
    
    @SuppressWarnings("rawtypes")
	private void activeWorkFlow(int servDate, int servType, Map inParam){
        switch (servDate) {
            case 3:// 投诉流程(包含投诉、建议、咨询、表扬、增值退订)
                if(servType == StaticData.SERV_TYPE_NEWTS){
                	workFlowAttemperImpl.activeWorkFlow(StaticData.WFL_ID_APPEAL_TS_NEW, inParam);//新投诉
                }else{
                	workFlowAttemperImpl.activeWorkFlow(StaticData.WFL_ID_APPEAL_TS, inParam);//老投诉
                }
                break;
            case 1:// 预受理调用工作流
            	workFlowAttemperImpl.activeWorkFlow(StaticData.WFL_ID_YS, inParam);
                break;
            case 0:// 疑难 调用工作流
            	workFlowAttemperImpl.activeWorkFlow(StaticData.WFL_ID_APPEAL, inParam);
                break;
            default:
                break;
        }
    }

    /**
     * 重复受理方法
     * 
     * @param orderId
     *            服务单ID
     * @return 是否受理成功
     */
    @SuppressWarnings("rawtypes")
	public boolean reatAccepte(ServContentInstance[] instance, OrderCustomerInfo custInfo,
            ServiceContent servContent, OrderAskInfo orderAskInfo, Map otherInfo) {
        // 如果该服务单为峻工状态生成新的服务定单,否则追加的重复受理内容
        int orderState = orderAskInfo.getOrderStatu();// 定单状态
        String newContent = servContent.getAcceptContent();// 新投诉内容
        // (张三_ JSZHANGSAN,2010-05-11)
        TsmStaff staff = this.pubFun.getLogonStaff();
        newContent = newContent.replace("@staff_@", staff.getName() + "_" + staff.getLogonName());
        newContent = newContent.replace("@rdata_@", this.pubFun.getSysDate());

        if (CompatHandler.isOStatuFinish(orderState)) {
            orderAskInfo.setServOrderId("");
            orderAskInfo.setMonth(pubFun.getIntMonth());
            servContent.setAcceptContent(newContent);

            // 新产生的单子为暂存状态
            int newStatu = orderAskInfo.getServType() == StaticData.SERV_TYPE_NEWTS ? StaticData.SERV_TYPE_SLZC:StaticData.OR_HOLD_STATU; 
            orderAskInfo.setOrderStatu(newStatu);
            String str = submitServiceOrderInstance(instance, custInfo, servContent, orderAskInfo, otherInfo, null);
            if (str.length() > 1) {
                return true;
            }
        } else {
            // 追加投诉内容
            int count = 0;
            count += servContentDao.updateAccpContent(orderAskInfo.getServOrderId(), newContent, orderAskInfo.getMonth());
            // 追加重复受理次数
            String servId = orderAskInfo.getServOrderId();
            OrderAskInfo ask = orderAskInfoDao.getOrderAskInfo(servId, false);
            int accCount = ask.getAskCount() + 1;
            count += this.orderAskInfoDao.updateOrderAskCount(servId, accCount);
            if (count == 2) {
                return true;
            }
        }
        return false;
    }

    /**
     * 暂存定单的提交方法
     * 
     * @param custInfo
     *            客户
     * @param servContent
     *            受理内容
     * @param orderAskInfo
     *            受理信息
     * @return 是否提交成功
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private String submitHoldOrder(OrderCustomerInfo custInfo, ServiceContent servContent,
            OrderAskInfo orderAskInfo, Map otherInfo) {
        // 组装工作流入参
        Map inParam = getRouteValue(otherInfo, orderAskInfo.getServType());// 路由
        
        if(custInfo.getOdsCity() != null && orderAskInfo.getServiceDate() == 1 && custInfo.getOdsCity().equals("Y")) {
        	// 客户经理归属地为省政企、商机单
        	// 增加政企目录判断
        	if(otherInfo.containsKey("ASSIGN_TO_SZQ") &&  "1".equals(otherInfo.get("ASSIGN_TO_SZQ").toString())) {
            	inParam.put("SEND_TO_ORG_ID",StaticData.ODS_ORG_ID);
                inParam.put("SEND_TO_ORG_NAME",this.pubFun.getOrgName(StaticData.ODS_ORG_ID));
        	}
        }else if(orderAskInfo.getServType() == StaticData.SERV_TYPE_GZ) {
        	// 跟踪单直派
        	// SEND_TO_OBJ_FLAG 1-部门 0-员工
        	inParam.put("SEND_TO_OBJ_FLAG",otherInfo.get("SEND_TO_OBJ_FLAG"));
        	inParam.put("SEND_TO_OBJ_ID",otherInfo.get("SEND_TO_OBJ_ID"));
        }else {// 清空内容
            orderAskInfo.setSendToOrgId("");
            orderAskInfo.setSendToOrgName("");
        }
        
        String orderId = orderAskInfo.getServOrderId();
        try {
	        // 更新定单修改的内容
	        orderCustDao.updateCustInfo(custInfo);
	        orderAskInfoDao.updateOrderAskInfo(orderAskInfo);
	        orderAskInfoDao.updateAcceptDate(orderId);
	        servContentDao.updateServContent(servContent);
        } catch (Exception e) {
            logger.error("未能成功更新服务受理单的信息!单号：{}", orderId);
            logger.error("更新受理单信息失败：{}", e.getMessage());
            throw new MyOwnRuntimeException("更新受理单信息失败!");
        }

        TsmStaff staff = pubFun.getLogonStaff();
        String dealOrg = staff.getOrganizationId();
        String dealStaffId = staff.getId();
        inParam.put("WF__REGION_ID", "" + orderAskInfo.getRegionId());
        inParam.put("SERV_ORDER_ID", orderId);
        inParam.put("MONTH_FALG", orderAskInfo.getMonth());
        inParam.put("DEAL_PR_ORGID", dealOrg);// 派发部门
        inParam.put("DEAL_PR_STAFFID", dealStaffId);// 派发员工
        inParam.put("FLOW_SEQUENCE", "1");// 新流程提交时，流程顺序号为1

        if (otherInfo.containsKey("WORKSHEET_ALLOT")) {// 直派单
            inParam.put("WORKSHEET_ALLOT", otherInfo.get("WORKSHEET_ALLOT"));
        } else if(otherInfo.containsKey("FINAL_OPTION_FLAG")) {// 最终处理意见
            inParam.put("WORKSHEET_ALLOT", "final_option");
        } else if(otherInfo.containsKey("ELECTRONIC_CHANNEL")) {// 电子渠道
            inParam.put("WORKSHEET_ALLOT", "electr_channel");
        }
        if (otherInfo.containsKey("ORDER_FLAG")) {// 订单标识
            inParam.put("ORDER_FLAG", otherInfo.get("ORDER_FLAG"));
        }
        activeWorkFlow(orderAskInfo.getServiceDate(),orderAskInfo.getServType(),inParam);
        return orderId;
    }

    /**
     * 提交后台退回修改后的定单
     * 
     * @param custInfo
     *            客户
     * @param servContent
     *            受理内容
     * @param orderAskInfo
     *            受理信息
     * @return 是否提交成功
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private String submitBackOrder(OrderCustomerInfo custInfo, ServiceContent servContent,
            OrderAskInfo orderAskInfo, Map otherInfo) {
        String orderId = orderAskInfo.getServOrderId();
        TsmStaff staff = pubFun.getLogonStaff();
        String dealOrg = staff.getOrganizationId();
        String dealStaffId = staff.getId();

        // 组装工作流入参
        Map inParam = getRouteValue(otherInfo, orderAskInfo.getServType());
        inParam.put("WF__REGION_ID", "" + orderAskInfo.getRegionId());
        inParam.put("SERV_ORDER_ID", orderId);
        inParam.put("MONTH_FALG", orderAskInfo.getMonth());
        inParam.put("DEAL_PR_ORGID", dealOrg);// 派发部门
        inParam.put("DEAL_PR_STAFFID", dealStaffId);// 派发员工

        // 更新受理单信息
        this.updateServiceOrderInfo(custInfo, servContent, orderAskInfo, "");
        // 找出退回工单,并更新
        String str = null;
        boolean isNewCmp = orderAskInfo.getServType() == StaticData.SERV_TYPE_NEWTS;
        if(isNewCmp){
            str = " AND CC_WORK_SHEET.SERVICE_ORDER_ID='" + orderAskInfo.getServOrderId()
                    + "' AND CC_WORK_SHEET.TACHE_ID=" + StaticData.TACHE_ORDER_BACK
                    + " AND CC_WORK_SHEET.SHEET_STATU=" + StaticData.WKST_DEALING_STATE_NEW;
        }else{
            str = " AND CC_WORK_SHEET.SERVICE_ORDER_ID='" + orderAskInfo.getServOrderId()
                    + "' AND CC_WORK_SHEET.TACHE_ID=" + StaticData.TACHE_ORDER_ASK
                    + " AND CC_WORK_SHEET.SHEET_STATU=" + StaticData.WKST_MODIFISHEET_STATE;
        }
        List tmp = this.sheetPubInfoDao.getSheetCondition(str, true);
        if (!tmp.isEmpty()) {
            Map map = (Map) tmp.get(0);
            String sheetId = map.get("WORK_SHEET_ID").toString();
            String monthStr = map.get("MONTH_FLAG").toString();
            String flowNo = "1";
            if (map.get("FLOW_SEQUENCE") != null) {
                flowNo = map.get("FLOW_SEQUENCE").toString();
                flowNo = String.valueOf(Integer.parseInt(flowNo) + 1);
            }
            inParam.put("FLOW_SEQUENCE", flowNo);

            // 更新提单员工信息及工单状态
            this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, Integer.parseInt(dealStaffId),
                    this.pubFun.getStaffName(Integer.parseInt(dealStaffId)), dealOrg,
                    this.pubFun.getOrgName(dealOrg));

            this.sheetPubInfoDao.updateSheetFinishDate(sheetId);// 更新完成时间
            int nxtStatu = isNewCmp ? StaticData.WKST_FINISH_STATE_NEW : StaticData.WKST_FINISH_STATE;
            this.sheetPubInfoDao.updateSheetState(sheetId, nxtStatu, pubFun.getStaticName(nxtStatu),
                    Integer.valueOf(monthStr), 2);// 更新为完成

            // 取新增加内容
            String newMOdfiContent = "工单修改内容为:";
            if (otherInfo.containsKey("NEW_MONDFI_CONTENT")) {
                newMOdfiContent = otherInfo.get("NEW_MONDFI_CONTENT").toString();
            }
            this.sheetPubInfoDao.updateSheetDealRequire(sheetId, "修改工单", " ", "前台修改工单",
                    newMOdfiContent, 15, isNewCmp ? StaticData.TACHE_ASSIGN_NEW : StaticData.TACHE_ASSIGN);
            inParam.put("BACKSHEET", sheetId);
        }
        
        if (otherInfo.containsKey("ORDER_FLAG")) {// 订单标识
            inParam.put("ORDER_FLAG", otherInfo.get("ORDER_FLAG"));
        }
        activeWorkFlow(orderAskInfo.getServiceDate(),orderAskInfo.getServType(),inParam);
        return orderId;
    }

    /**
     * 根据受理单id客户CUSTID分区标志地域ID获取受理单
     * 
     * @param queryType
     *            0表示当前表不存在时,需要查历史表，1表示只查历史表 2，只查当前表
     * @param serOrdId
     *            serviceOrderID
     * @return 受理单对象
     */
    public ServiceOrderInfo getErrOrderById(String serOrdId, int queryType, String sheetId) {
        OrderCustomerInfo orderCustInfo = null;
        OrderAskInfo orderAskInfo = null;
        ServiceContent servContent = null;
        SheetPubInfo sheetInfo = null;
        orderAskInfo = orderAskInfoDao.getErrInfoById(serOrdId, queryType);// 受理信息
        if(StringUtils.isNull(orderAskInfo)){
        	return null;
        }
        String custGuid = orderAskInfo.getCustId();
        if (queryType != 1) {
            // 先查询当前，当前不存在，再查询历史
            if (custGuid != null) {
                orderCustInfo = orderCustDao.getOrderCustByGuid(custGuid, false);
                if (null == orderCustInfo) {
                    orderCustInfo = orderCustDao.getOrderCustByGuid(custGuid, true);
                }
            }
            servContent = servContentDao.getServContentByOrderId(serOrdId, false, 0);// 受理内容
            if (null == servContent) {
                logger.warn("当前受理内容表没有查询到记录 orderId: {}", serOrdId);

                if (orderAskInfo.getOrderStatu() == StaticData.OR_COMPLETE_STATU) {
                    servContent = servContentDao.getServContentByOrderId(serOrdId, true,
                            orderAskInfo.getOrderVer());
                } else {
                    servContent = servContentDao.getServContentByOrderId(serOrdId, true, 0);
                }
            }
        } else {
            // 只查历史
            servContent = servContentDao.getServContentByOrderId(serOrdId, true, 0);
        }

        sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, orderAskInfo.getRegionId(),
                orderAskInfo.getMonth(), true);// 工单信息

        ServiceOrderInfo servOrderInfo = new ServiceOrderInfo();// 完整受单信息
        servOrderInfo.setOrderAskInfo(orderAskInfo);
        servOrderInfo.setOrderCustInfo(orderCustInfo);
        servOrderInfo.setServContent(servContent != null ? servContent : new ServiceContent());
        servOrderInfo.setSheetInfo(sheetInfo);
        return servOrderInfo;
    }

    /**
     * 根据受理单id客户CUSTID分区标志地域ID获取受理单
     * 
     * @param serOrdId
     *            serviceOrderID
     * @param custId
     *            客户ID
     * @param monthFlg
     *            作为版本号
     * @param regionId
     *            地域ID
     * @return 受理单对象
     */
    public ServiceOrderInfo getServiceOrderById(String serOrdId, String custId, Integer monthFlg, String regionId, int status, int vesion) {
    	OrderCustomerInfo orderCustInfo = null;
        OrderAskInfo orderAskInfo = null;
        ServiceContent servContent = null;
        ServiceOrderInfo servOrderInfo = new ServiceOrderInfo();// 完整受单信息

    	if (custId != null) {
            if (StaticData.OR_COMPLETE_STATU == status||StaticData.OR_LOGOFF_STATU == status||StaticData.OR_CANCEL_STATU == status||StaticData.OR_FINISH_STATU == status) {
                orderCustInfo = orderCustDao.getOrderCustByGuid(custId, true);// 客户信息
            } else {
                orderCustInfo = orderCustDao.getOrderCustByGuid(custId, false);// 客户信息
            }
        }
        if (StaticData.OR_COMPLETE_STATU == status||StaticData.OR_LOGOFF_STATU == status||StaticData.OR_CANCEL_STATU == status||StaticData.OR_FINISH_STATU == status) {
            servContent = servContentDao.getServContentByOrderId(serOrdId, true, vesion);// 受理内容
        } else {
            servContent = servContentDao.getServContentByOrderId(serOrdId, false, vesion);// 受理内容
        }
        if (StaticData.OR_COMPLETE_STATU == status||StaticData.OR_LOGOFF_STATU == status||StaticData.OR_CANCEL_STATU == status||StaticData.OR_FINISH_STATU == status) {
            orderAskInfo = orderAskInfoDao.getOrderAskInfoByIdMonth(serOrdId, monthFlg, regionId,
                    true, vesion);// 受理信息
        } else {
            orderAskInfo = orderAskInfoDao.getOrderAskInfoByIdMonth(serOrdId, monthFlg, regionId,
                    false, vesion);// 受理信息
        }
        
        servOrderInfo.setOrderAskInfo(orderAskInfo);
        servOrderInfo.setServContent(servContent==null?new ServiceContent():servContent);
        if (orderCustInfo != null) {
            servOrderInfo.setOrderCustInfo(orderCustInfo);
        }
        return servOrderInfo;
    }

    /**
     * 保存受量单信息到各实体表中
     * 
     * @param custInfo
     *            客户
     * @param servContent
     *            受理内容
     * @param orderAskInfo
     *            受理信息
     * @return 生成的定单号
     */
    private String saveServiceOrder(OrderCustomerInfo custInfo, ServiceContent servContent,
            OrderAskInfo orderAskInfo, String logonFlag, boolean... newFlag) {
        // 生成定单号和客户guid
        int servType = orderAskInfo.getServType();
        int regionId = orderAskInfo.getRegionId();
        String orderId = pubFun.crtOrderId(servType, regionId);
        String custGuid = pubFun.crtGuid();
        // 设置定单号和客户guid
        orderAskInfo.setServOrderId(orderId);
        servContent.setServOrderId(orderId);
        orderAskInfo.setCustId(custGuid);
        custInfo.setCustGuid(custGuid);
        // 要修改
        if(newFlag.length == 0) {
	        SheetLimitTimeCollocate limitBean = this.sheetLimitTimeServ.getSheetLimitime(
	                orderAskInfo.getRegionId(), orderAskInfo.getServType(), orderAskInfo.getRegionId(),
	                0, orderAskInfo.getCustServGrade(), orderAskInfo.getUrgencyGrade());
	        if (limitBean != null) {
	            orderAskInfo.setPreAlarmValue(limitBean.getPrealarmValue());
	            orderAskInfo.setOrderLimitTime(limitBean.getLimitTime());
	        }
        }

        boolean notFromNet = orderAskInfo.getAskStaffId() != StaticData.ACPT_STAFFID_WT
                && orderAskInfo.getAskStaffId() != StaticData.ACPT_STAFFID_JT;
        // 是否为网客用户受理、集团受理
        if (notFromNet && "0".equals(logonFlag)) {
            // 如果不是网客用户 ,则取得当前登录员工的信息,并设置受理员工,受理机构信息
        	TsmStaff staff = pubFun.getLogonStaff();
            orderAskInfo.setAskStaffId(Integer.parseInt(staff.getId()));
            orderAskInfo.setAskStaffName(staff.getName());
            orderAskInfo.setAskOrgId(staff.getOrganizationId());
            orderAskInfo.setAskOrgName(staff.getOrgName());
        }

        Integer month = this.pubFun.getIntMonth();
        custInfo.setMonth(month);
        orderAskInfo.setMonth(month);
        servContent.setMonth(month);
        
        // 保存各实体信息
        boolean hisFlag = false;// 当前表
        try {
            orderCustDao.saveOrderCust(custInfo);
            orderAskInfoDao.saveOrderAskInfo(orderAskInfo);
            servContentDao.saveServiceContent(servContent, hisFlag);
        } catch (Exception e) {
            logger.error("未能完整保存服务受理单的信息!产品号码：{}", orderAskInfo.getProdNum());
            logger.error("保存受理单信息失败：{}", e.getMessage());
            throw new MyOwnRuntimeException("保存受理单信息失败!");
        }
        return orderId;
    }
    
    /**
     * 
     * @param custInfo
     * @param servContent
     * @param orderAskInfo
     * @return
     */
    private String updateServiceOrder(OrderCustomerInfo custInfo, ServiceContent servContent,
            OrderAskInfo orderAskInfo) {
        String orderId = orderAskInfo.getServOrderId();
        if ("".equals(orderId)) {
            throw new MyOwnRuntimeException("没有提供要更新的受理单号信息!");
        }
        
        SheetLimitTimeCollocate limitBean = this.sheetLimitTimeServ.getSheetLimitime(
                orderAskInfo.getRegionId(), orderAskInfo.getServType(), orderAskInfo.getRegionId(),
                0, orderAskInfo.getCustServGrade(), orderAskInfo.getUrgencyGrade());
        if (limitBean != null) {
            orderAskInfo.setPreAlarmValue(limitBean.getPrealarmValue());
            orderAskInfo.setOrderLimitTime(limitBean.getLimitTime());
        }
        
        try {
            this.orderCustDao.updateCustInfo(custInfo);
            this.orderAskInfoDao.updateOrderAskInfo(orderAskInfo);
            this.servContentDao.updateServContent(servContent);
        } catch (Exception e) {
        	logger.error("未能成功更新服务受理单的信息!单号：{}", orderId);
        	logger.error("更新受理单信息失败：{}", e.getMessage());
        	throw new MyOwnRuntimeException("更新受理单信息失败!");
        }
        return orderId;
    }

    /**
     * 得到当前员工登录的基本信息
     * 
     * @return
     */
    public TsmStaff getLogonStaff() {
        return this.pubFun.getLogonStaff();
    }

    /**
     * 在受理时候,根据受理类型,业务号码查询该号码是否有未竣工的工单
     * 
     * @param proNum
     *            产品号码
     * @param servType
     *            服务类型
     * @return true 存在,false 不存在
     */
	private boolean checkOrderSubmit(String prodNum, int servType, int regionId) {
		String strWhere = " AND CC_SERVICE_ORDER_ASK.PROD_NUM = '" + prodNum + "'"
				+ " AND CC_SERVICE_ORDER_ASK.SERVICE_TYPE = " + servType
				+ " AND CC_SERVICE_ORDER_ASK.REGION_ID = " + regionId
				+ " AND CC_SERVICE_ORDER_ASK.ORDER_STATU NOT IN (" + StaticData.OR_HOLD_STATU + "," + StaticData.OR_AUTOVISIT_STATU  + ")";
		OrderAskInfo[] bean = this.orderAskInfoDao.getOrderAskInfoByCondition(strWhere, false);
		return (bean.length > 0);
	}

    /**
     * 得到路由
     * 
     * @param otherInfo
     *            前台提交map
     * @param servType
     *            订单服务类型
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getRouteValue(Map otherInfo, int servType) {
        Map inParam = new HashMap();
        String strFlow = otherInfo.get("STRFLOW").toString();
        if(StaticData.SERV_TYPE_NEWTS == servType){
            if (strFlow.equals("DISPATCHSHEET")) {// 派单
                inParam.put("ROUTE_VALUE", StaticData.NEW_TS_DISPL);
                inParam.put("BACKORDER", "BACKORDER");
            }else if (strFlow.equals("FINASSESS")) {// 新投诉受理到终定性
                inParam.put("ROUTE_VALUE", StaticData.ROUTE_ASK_TO_FINASSESS);
            }
        }else{
            if (strFlow.equals("DISPATCHSHEET")) {// 派单
                inParam.put("ROUTE_VALUE", StaticData.ROUTE_GOTO_NEXT);
                inParam.put("BACKORDER", "BACKORDER");
            }else if (strFlow.equals("AUDSHEET")) {// 审核
                inParam.put("ROUTE_VALUE", StaticData.ROUTE_ASK_TO_AUD_JS);
            }else if (strFlow.equals("ASSESS")) {// 定性
                inParam.put("ROUTE_VALUE", StaticData.ROUTE_ASK_TO_ASSESS);
            }else if (strFlow.equals("ORGDEAL")) {// 部门处理
                String dealRequire = otherInfo.get("DEALREQUIE").toString();
                inParam.put("ROUTE_VALUE", StaticData.ROUTE_ASK_TO_DEAL);
                inParam.put("DEAL_REQUIRE", dealRequire);
                inParam.put("AUD_FLAG", "1");
            }else if (strFlow.equals("AUDPIGEONHOLE")) {// 归档
                inParam.put("ROUTE_VALUE", StaticData.ROUTE_ASK_TO_PIGEONHOLE);
            }else if (strFlow.equals("ASKFINISH")) {// 竣工
                inParam.put("ROUTE_VALUE", StaticData.ROUTE_TSASK_TO_FINISHS);
            }
        }
        return inParam;
    }

	@Override
	public String getOrderCount(String staffId) {
		Map<String, String> orgMap = orderQury.getWarnOrgWhere(staffId);
		
		int cur0 = 0;
		GridDataInfo dataInfo0 = dbgridDataTs.getDealingSheetTs(0, 0, "", orgMap);
		if (dataInfo0.getQuryCount() > 0) {
			cur0 = dataInfo0.getQuryCount();
		}
		String where1 = " AND A.ACCEPT_DATE < DATE_SUB(NOW(), interval (A.ORDER_LIMIT_TIME + A.HANGUP_TIME_COUNT/60 + 72) HOUR)";
		int cur1 = getCurOverTimeCount(staffId, 1, where1, orgMap);
		String where2 = " AND L.REPEAT_NEW_FLAG = 1 AND EXISTS (SELECT 1 FROM CC_SERVICE_CONNECTION Z WHERE"
				+ " Z.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID AND Z.CONNECTION_STATE = 1 AND Z.CONNECTION_TYPE = 'Z')";
		int cur2 = getCurCount(staffId, 2, where2, orgMap);
		String where3 = " AND (D.SIX_GRADE_CATALOG = 23002270 OR D.APPEAL_REASON_ID IN (11504, 11505))";
		int cur3 = getCurCount(staffId, 3, where3, orgMap);
		String where4 = " AND (D.APPEAL_REASON_ID = 23002503 OR D.APPEAL_PROD_ID = 111)";
		int cur4 = getCurCount(staffId, 4, where4, orgMap);
		String where5 = " AND (D.APPEAL_PROD_ID = 23002556 OR D.PROD_TWO IN (50101,501002))";
		int cur5 = getCurCount(staffId, 5, where5, orgMap);
		String where6 = " AND D.BEST_ORDER > 100122410";
		int cur6 = getCurCount(staffId, 6, where6, orgMap);
		JSONObject json = new JSONObject();
		json.put("cur0", cur0);
		json.put("cur1", cur1);
		json.put("cur2", cur2);
		json.put("cur3", cur3);
		json.put("cur4", cur4);
		json.put("cur5", cur5);
		json.put("cur6", cur6);
		return json.toString();
	}

	@SuppressWarnings("rawtypes")
	private int getCurOverTimeCount(String staffId, int curType, String where, Map<String, String> orgMap) {
		int cur = 0;
		StringBuilder curOrders = new StringBuilder();
		curOrders.append("");
		GridDataInfo dataInfo = dbgridDataTs.getDealingSheetTs(0, 0, where, orgMap);
		if (dataInfo.getQuryCount() > 0) {
			List list = dataInfo.getList();
			for (int i = list.size() - 1; i >= 0; i--) {
				Map map = (Map) list.get(i);
				String beginDate = this.getStringByKey(map, "ACCEPT_DATE");
				String endDate = this.getStringByKey(map, "FINISH_DATE");
				int hangupTimeCount = this.getIntByKey(map, "HANGUP_ORDER_COUNT");
				int serviceType = this.getIntByKey(map, "SERVICE_TYPE");
				String sysDate = this.getStringByKey(map, "SYS_DATE");
				int workTime = pubFun.getWorkingTime(beginDate, beginDate, endDate, hangupTimeCount * 60, serviceType, sysDate);
				int orderLimitTime = this.getIntByKey(map, "ORDER_LIMIT_TIME");
				if (workTime < (orderLimitTime + 72) * 3600) {
					list.remove(i);
				} else {
					String orderId = this.getStringByKey(map, "SERVICE_ORDER_ID");
					curOrders.append(orderId + ",");
				}
			}
			cur = list.size();
			String curOrder = curOrders.toString();
			curOrder = curOrder.substring(0, curOrder.length() - 1);
			orderAskInfoDao.saveCurOrder(staffId, curType, curOrder);
		}
		return cur;
	}

	@SuppressWarnings("rawtypes")
	private int getCurCount(String staffId, int curType, String where, Map<String, String> orgMap) {
		int cur = 0;
		StringBuilder curOrders = new StringBuilder();
		curOrders.append("");
		GridDataInfo dataInfo = dbgridDataTs.getDealingSheetTs(0, 0, where, orgMap);
		if (dataInfo.getQuryCount() > 0) {
			cur = dataInfo.getQuryCount();
			List list = dataInfo.getList();
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				String orderId = map.get("SERVICE_ORDER_ID") == null ? "" : map.get("SERVICE_ORDER_ID").toString();
				curOrders.append(orderId + ",");
			}
			String curOrder = curOrders.toString();
			curOrder = curOrder.substring(0, curOrder.length() - 1);
			orderAskInfoDao.saveCurOrder(staffId, curType, curOrder);
		}
		return cur;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Map getRoleWork(String staffId) {
		return orderAskInfoDao.getRoleWork(staffId);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Map getWorkView(String staffId) {
		return orderAskInfoDao.getWorkView(staffId);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Map qrySheetList(String where) {
		return orderAskInfoDao.qrySheetList(where);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Map qryOrderAskList(Map param) {
		return orderAskInfoDao.qryOrderAskList(param);
	}
	
	@Override
	public CallSummary getLastCallSummary(String orderId) {
		CallSummary summary = orderAskInfoDao.getLastSummary(orderId, true);
		if(summary==null) {
			summary = orderAskInfoDao.getLastSummary(orderId, false);
		}
		return summary;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public int saveCallSummaryUpdate(CallSummary summary) {
		TsmStaff staff = pubFun.getLogonStaff();
	
		List catalogList = pubFun.queryAcceptDir(Integer.parseInt(summary.getOldNId()), 0);
		if (catalogList.isEmpty()) {
			return 0;
		}
		Map catalogMap = (Map) catalogList.get(0);
		String ns = catalogMap.get("N").toString();
		summary.setOldFirstLevel(pubFun.getSplitNameByIdx(ns, 0));
		summary.setOldSecondLevel(pubFun.getSplitNameByIdx(ns, 1));
		summary.setOldThirdLevel(pubFun.getSplitNameByIdx(ns, 2));
		summary.setOldFourthLevel(pubFun.getSplitNameByIdx(ns, 3));
		summary.setOldFifthLevel(pubFun.getSplitNameByIdx(ns, 4));
		summary.setOldSixthLevel(pubFun.getSplitNameByIdx(ns, 5));
		summary.setLogonname(staff.getLogonName());
		summary.setCreateChannel("工单小结修改");
		int rtn = orderAskInfoDao.saveCallSummaryUpdate(summary);

		String oldInfo = summary.getOldExFirst() + " > " + summary.getOldExSecond() + " > " + summary.getOldExThird();
		String newInfo = summary.getExFirst() + " > " + summary.getExSecond() + " > " + summary.getExThird();
		if (oldInfo.equals(newInfo)) {
			return rtn;
		} else {
			String orderId = summary.getOrderId();
			OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
			TSOrderMistake om = new TSOrderMistake();
			om.setServiceOrderId(summary.getOrderId());
			om.setWorkSheetId("");
			om.setMistakeOrgId(orderAskInfo.getAskOrgId());
			om.setMistakeStaffId(orderAskInfo.getAskStaffId());
			om.setMistakeType(2);
			om.setCheckOrgId(staff.getOrganizationId());
			om.setCheckStaffId(Integer.parseInt(staff.getId()));
			if ("".equals(summary.getOldExFirst())) {
				om.setOldInfo("");
			} else {
				om.setOldInfo(oldInfo);
			}
			if ("".equals(summary.getExFirst())) {
				om.setNewInfo("");
			} else {
				om.setNewInfo(newInfo);
			}
			return sheetMistakeDAO.insertOrderMistake(om);
		}
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Map qrCliqueList(String where) {
		return orderAskInfoDao.qrCliqueList(where);
	}
	
	
	public String getBssStaffCode(String logoName) {
		return pubFun.getBssStaffCode(logoName);
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Map qrySpecialInfo(String tableType, String sql) {
		return orderAskInfoDao.getSpecialInfoStr(tableType, sql);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int addSpecialInf(Map param) {
		String type=param.get("type").toString();
		String sourceNum=param.get("sourceNum").toString();
		String regionId=param.get("region_id").toString();
		int areaId=Integer.parseInt(regionId);
		String areaCode=pubFun.getRegionTelNo(areaId);
		param.put("area_code", areaCode);
		String num=sourceNum.substring(0, 1);
		if(!"0".equals(num)) {
			if(!"1".equals(num)) {
				sourceNum=areaCode+sourceNum;
			}else {
				sourceNum="0"+sourceNum;
			}
		}
		param.put("sourceNum", sourceNum);
		if("0".equals(type)){
			return orderAskInfoDao.addSpecialInf(param);
		}else {
			return orderAskInfoDao.addSpecialIvr(param);
		}
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public int removeSpecial(Map param) {
		return orderAskInfoDao.removeSpecial(param);
	}
	
	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int updateSpecial(Map param) {
		String type=param.get("type").toString();
		String regionId=param.get("region_id").toString();
		int areaId=Integer.parseInt(regionId);
		String areaCode=pubFun.getRegionTelNo(areaId);
		param.put("area_code", areaCode);
		if("0".equals(type)){
			return orderAskInfoDao.updateSpecialInfo(param);
		}else {
			return orderAskInfoDao.updateSpecialIvr(param);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getGzOorderList(String orderId) {
		//查原单数量
		List result = orderAskInfoDao.getGzOorderListByNewId(orderId);
		if(result.isEmpty()) {
			//查跟踪单数量
			result=orderAskInfoDao.getGzOorderListByOldId(orderId);
		}
		if(!result.isEmpty()) {
			Map m = (Map)result.get(0);
			String oldOrderId = m.get("OLD_ORDER_ID") == null ? null : m.get("OLD_ORDER_ID").toString();
			//小额退赔建单模板
			this.setContentSave(result);
			if(null != oldOrderId) {
				OrderAskInfo info = orderAskInfoDao.getOrderAskInfo(oldOrderId, false);
				Map o = new HashMap();
				o.put("NEW_ORDER_ID", oldOrderId);
				o.put("HIS_FLAG", info == null ? "1" : "0");
				result.add(0, o);
			}
		}
		return result;
	}
	
	@SuppressWarnings("rawtypes")
	private void setContentSave(List result) {
		for(int i=0; i<result.size(); i++) {
			Map m = (Map)result.get(i);
			String newOrderId = m.get("NEW_ORDER_ID").toString();
			String hisFlag = m.get("HIS_FLAG").toString();
			String trackType = m.get("TRACK_TYPE").toString();//1-调账
			if(!"1".equals(trackType)) {
				continue;
			}
			boolean flag = "1".equals(hisFlag);
			//查询投诉现象
			ServiceContent servContent = servContentDao.getServContentByOrderId(newOrderId, flag, 0);
			if(servContent.getAppealChild() != 2020501) {//小额退赔
				continue;
			}
			serviceContentSchem.setRefundInfo(m, newOrderId, flag);
		}
	}

	@SuppressWarnings("rawtypes")
	public String getPersonas(String serviceOrderId, String regionId, String prodNum, boolean curFlag) {
		List list = null;
		if (curFlag) {
			list = personaDao.queryPersonaByOrderId(serviceOrderId);
		} else {
			list = personaDao.queryPersonaHisByOrderId(serviceOrderId);
		}
		if (list.isEmpty()) {
			return null;
		}
			
		Map map = (Map) list.get(0);
		JsonObject personas = new JsonObject();
		personas.addProperty("faceUrl", map.get("CUST_FACE").toString());
		personas.addProperty("custName", map.get("CUST_NAME").toString());
		personas.addProperty("custStar", map.get("CUST_STAR").toString());
		personas.addProperty("custAge", map.get("CUSTAGE").toString());
		personas.addProperty("custSex", map.get("CUST_SEX").toString());
		if (null != map.get("MONTHS_BT")) {
			int months = Integer.parseInt(map.get("MONTHS_BT").toString());
			personas.addProperty("monthsBt", months / 12 + "年" + months % 12 + "月");
		}
		personas.addProperty("custType", map.get("CUST_GROUP_DESC").toString());
		personas.addProperty("isKeyCustomer", map.get("IS_KEY_CUSTOMER").toString());
		personas.addProperty("isKeyPerson", map.get("IS_KEY_PERSON").toString());
		personas.addProperty("cityLabel", map.get("CITY_LABEL").toString());
		personas.addProperty("repeatNum", map.get("REPEAT_NEW_FLAG").toString());
		personas.addProperty("upTendencyNum", map.get("UP_TENDENCY_NUM").toString());
		personas.addProperty("refundNum", map.get("REFUND_NUM").toString());
		personas.addProperty("upTendencyFlag", map.get("UP_TENDENCY_FLAG").toString());
		personas.addProperty("satisfyNum", map.get("SATISFY_NUM").toString());
		personas.addProperty("unsatisfyNum", map.get("UNSATISFY_NUM").toString());
		personas.addProperty("preferAppeal", map.get("PREFER_APPEAL").toString());
		personas.addProperty("callNum", map.get("CALL_FLAG").toString());
		personas.addProperty("finalOptionOrderNum", map.get("FINAL_OPTION_ORDER_NUM").toString());

		JsonArray personaList = new JsonArray();
		String tsEspecially = especiallyCustDao.queryTsEspeciallyByCustNum(regionId, prodNum);
		if (!"".equals(tsEspecially)) {
			String[] tes = tsEspecially.split(";");
			for (int i = tes.length - 1; i >= 0; i--) {
				JsonObject especiallys = new JsonObject();
				String val = tes[i];
				StringBuilder label = new StringBuilder();
				int len = this.getLabelLength(val, label);
				
				especiallys.addProperty("label", label.toString());
				especiallys.addProperty("value", val);
				especiallys.addProperty("len", len);
				personaList.add(especiallys);
			}
		}
		String sensitiveType = map.get("SENSITIVE_TYPE").toString();
		if(StringUtils.isNotEmpty(sensitiveType)) {
			List<String> sensitiveList = Arrays.asList(sensitiveType.split(","));
			if(sensitiveList.contains("1")) {
				JsonObject sensitive = new JsonObject();
				sensitive.addProperty("label", "专家型用户");
				sensitive.addProperty("value", "专家型用户");
				sensitive.addProperty("len", 5);
				personaList.add(sensitive);
			}
			if(sensitiveList.contains("2")) {
				JsonObject sensitive = new JsonObject();
				sensitive.addProperty("label", "免营销用户");
				sensitive.addProperty("value", "免营销用户");
				sensitive.addProperty("len", 5);
				personaList.add(sensitive);
			}
		}
		
//		if ("1".equals(map.get("PREFER_APPEAL").toString())) {
//			JsonObject preferAppeal = new JsonObject();
//			preferAppeal.addProperty("label", "易申诉");
//			personaList.add(preferAppeal);
//		}
//		if ("1".equals(map.get("PREFER_COMPLAINT").toString())) {
//			JsonObject preferComplaint = new JsonObject();
//			preferComplaint.addProperty("label", "偏好10000号投诉");
//			personaList.add(preferComplaint);
//		}
//		if (null != map.get("FINAL_OPTION_ORDER_ID")) {
//			String finalId = map.get("FINAL_OPTION_ORDER_ID").toString();
//			if (!"".equals(finalId)) {
//				JsonObject finalOptionOrderId = new JsonObject();
//				finalOptionOrderId.addProperty("label", "最终处理："+finalId);
//				finalOptionOrderId.addProperty("value", "最终处理："+finalId);
//				personaList.add(finalOptionOrderId);
//			}
//		}
			
		Map orderPreference = getOrderPreference(serviceOrderId, prodNum, Integer.valueOf(regionId));
		personas.addProperty("preferenceBusiness", orderPreference.get("preferenceBusiness").toString());
		personas.addProperty("preferencePalm", orderPreference.get("preferencePalm").toString());

		personas.add("infoList", personaList);
		String checkSensitive = customerServiceFeign.sensitiveQuery(prodNum);//敏感客户标识
		if(!"0".equals(checkSensitive)){
			checkSensitive = customerServiceFeign.sensitiveQuery(map.get("RELA_INFO").toString());
		}
		personas.addProperty("sensitive", checkSensitive);
		return new Gson().toJson(personas);
	}
	
	private int getLabelLength(String value, StringBuilder label) {
		int valueLength = 0;
        String chinese = "[\u0391-\uFFE5]";
        // 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1
        for (int i = 0; i < value.length(); i++) {
            // 获取一个字符
            String temp = value.substring(i, i + 1);
            // 判断是否为中文字符
            if (temp.matches(chinese)) {
            	// 中文字符长度为2
            	if(valueLength > 14) {
            		break;
            	}
                valueLength += 2;
            } else {
                // 其他字符长度为1
            	if(valueLength > 15) {
            		break;
            	}
                valueLength += 1;
            }
            label.append(temp);
        }
        if(valueLength == 0) {
        	return 0;
        }
        return valueLength / 2;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getOrderPreference(String serviceOrderId, String prodNum, int regionId){
		Map preferenceMap = new HashMap<>();
		int preferenceBusiness = 0;
		int preferencePalm = 0;
		List preferenceList = orderAskInfoDao.queryOrderPreference(serviceOrderId,prodNum, regionId);
		if (!preferenceList.isEmpty()) {
			for (int i = 0; i < preferenceList.size(); i++) {
				Map preference = (Map) preferenceList.get(i);
				int channelId = this.getIntByKey(preference, "ACCEPT_CHANNEL_ID");
				if (channelId == 707907008) {
					preferenceBusiness ++;
				} else if (channelId == 707907009 || channelId == 707907010) {
					preferencePalm ++;
				}
			}
		}
		preferenceMap.put("preferenceBusiness", preferenceBusiness);
		preferenceMap.put("preferencePalm", preferencePalm);
		return preferenceMap;
	}

    /**
     * 根据受理单的单号查询此受理单的相关信息
     * 
     * @param orderId
     *            受理单号
     * @param hisFlag
     *            当前、历史标识
     * @return 受理单对像
     */
    public ServiceWorkSheetInfo getServiceInfo(String orderId, boolean hisFlag) {
        // 受理信息
        OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, hisFlag);
        if (orderAskInfo == null) {
            return null;// 如果没有受理信息则返回空
        }

        // 受理内容
        ServiceContent servContent = servContentDao.getServContentByOrderId(orderId, hisFlag, orderAskInfo.getOrderVer());
        // 客户信息
        OrderCustomerInfo orderCust = orderCustDao.getOrderCustByGuid(orderAskInfo.getCustId(), hisFlag);
        // 服务单标签
        ServiceLabel serviceLabel = labelManageDao.queryServiceLabelById(orderId, hisFlag);

        ServiceWorkSheetInfo servOrderInfo = new ServiceWorkSheetInfo();
        servOrderInfo.setOrderAskInfo(orderAskInfo);
        if (servContent != null) {
            servOrderInfo.setServContent(servContent);
        } else {
            servOrderInfo.setServContent(new ServiceContent());
        }
        if (orderCust != null) {
            servOrderInfo.setOrderCustInfo(orderCust);
        } else {
            servOrderInfo.setOrderCustInfo(new OrderCustomerInfo());
        }
        if (serviceLabel != null) {
            servOrderInfo.setServiceLabel(serviceLabel);
        } else {
            servOrderInfo.setServiceLabel(new ServiceLabel());
        }
        return servOrderInfo;
    }
    
    public void autoPdAsync(String orderId) {
    	try {
			asyncTask.autoPdAsync(orderId);
		} catch (Exception e) {
			logger.error("autoPdAsync error: {}", e.getMessage(), e);
		}
    }

	public void autoFinish(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, boolean boo, String sendContent, int dealStaff) {
		try {
			asyncTask.autoFinish(sheetPubInfo, orderAskInfo, buopSheetInfo, boo, sendContent, dealStaff);
		} catch (Exception e) {
			logger.error("autoFinish error: {}", e.getMessage(), e);
		}
	}
    
	public void saveComplaintInfo(JSONObject culiqueJson, String orderId) {
		if(culiqueJson.has("complaintInfo")) {
			ComplaintInfo info = (ComplaintInfo)JSONObject.toBean(culiqueJson.getJSONObject("complaintInfo"), ComplaintInfo.class);
			info.setOrderId(orderId);
			int num = orderAskInfoDao.insertComplaintInfo(info);
			logger.info("saveComplaintInfo result:{}", num > 0 ? "成功" : "失败");
		}
	}
	
	@SuppressWarnings("rawtypes")
	public Map getComplaintInfo(String orderId, String hisFlag) {
		boolean flag = false;
		if(StringUtils.isNotEmpty(hisFlag) && "1".equals(hisFlag)) {
			flag = true;
		}
		return orderAskInfoDao.getComplaintInfo(orderId, flag);
	}
	
	
    /**
     * 业务预受理提交
     */
    @SuppressWarnings("rawtypes")
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String submitServiceOrderInstanceYS(OrderCustomerInfo custInfo, ServiceContent servContent, OrderAskInfo orderAskInfo, Map otherInfo, boolean finishCheckFlag, BuopSheetInfo info) {
        int regionId = orderAskInfo.getRegionId(); // 订单地域
        int servType = orderAskInfo.getServType(); // 性质类别
        boolean isBack = false;
        boolean isHold = false;
        String orderId = orderAskInfo.getServOrderId(); // 定单号
        if (org.apache.commons.lang3.StringUtils.isNotBlank(orderId)) { // 去掉重复提交的异常
            OrderAskInfo curOrderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
            if (curOrderAskInfo == null) {
                orderId = "";
            } else {
                int orderStatu = curOrderAskInfo.getOrderStatu();
                isBack = (orderStatu == StaticData.OR_BACK_STATU); // 是否是退回订单
                isHold = (orderStatu == StaticData.OR_HOLD_STATU); // 是否是暂存订单
                
                if(!(this.isBackOrHoldYS(isBack, isHold))){//不是暂存和退回，订单号置为空
                	orderId = "";
                }
            }
        }

        // 对于预受理单，如果该产品号码下有未竣工的工单，则不予受理。后台退回的工单不考虑该情况
        if (this.checkNotFinishFlag(finishCheckFlag, !isBack)) {
            boolean check = checkOrderSubmit(orderAskInfo.getProdNum(), servType, regionId);
            if (check) {
                return "ORDERNOTFINISH";
            }
        }
        
        String result = "";
        boolean saveFlag = true;// 保存商机单信息
        if ("".equals(orderId)) { // 定单号为空说明是首次提交的定单
            // 首次提交的订单需要设置月分区
            Integer month = pubFun.getIntMonth();
            custInfo.setMonth(month);
            servContent.setMonth(month);
            orderAskInfo.setMonth(month);
            // 提交受理信息，生成新订单、新工单等信息
            result = submitNewOrder(custInfo, servContent, orderAskInfo, otherInfo);
        } else {
            if (isHold) { // 暂存单的提交
                result = submitHoldOrder(custInfo, servContent, orderAskInfo, otherInfo);
            } else if (isBack) { // 退回单的提交
                submitBackOrder(custInfo, servContent, orderAskInfo, otherInfo);
                result = "ORDERBACK";
                saveFlag = false;
            }
        }
        // 保存商机单信息
        this.saveBuopSheetInfo(info, orderAskInfo.getServOrderId(), saveFlag);
        logger.info("submitServiceOrderInstanceYS prodNum: {} result: {}", orderAskInfo.getProdNum(), orderAskInfo.getServOrderId());
        
        return result;
    }
    
    private boolean checkNotFinishFlag(boolean finishCheckFlag, boolean isNotBack) {
    	return finishCheckFlag && isNotBack;
    }
    
    public boolean checkOrderNotFinish(OrderAskInfo orderAskInfo) {
    	int regionId = orderAskInfo.getRegionId(); // 订单地域
        int servType = orderAskInfo.getServType(); // 性质类别
        
        boolean isBack = false;
        String orderId = orderAskInfo.getServOrderId(); // 定单号
        if (org.apache.commons.lang3.StringUtils.isNotBlank(orderId)) { // 去掉重复提交的异常
            OrderAskInfo curOrderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
            if (curOrderAskInfo != null) {
                int orderStatu = curOrderAskInfo.getOrderStatu();
                isBack = (orderStatu == StaticData.OR_BACK_STATU); // 是否是退回订单
            }
        }

        // 对于预受理单，如果该产品号码下有未竣工的工单，则不予受理。后台退回的工单不考虑该情况
        if (!isBack) {
            boolean check = checkOrderSubmit(orderAskInfo.getProdNum(), servType, regionId);
            if (check) {
                return true;
            }
        }
        return false;
    }
    
	private boolean isBackOrHoldYS(boolean isBack, boolean isHold) {
		return isBack || isHold;
	}
    
    @SuppressWarnings("unused")
	private void dealYSOrder(String orderId, int staffId, YNSJOrder order) {
		// 判断是否特殊预受理
    	if(!"".equals(orderId)) {
			String operType = pubFun.getOperType(order.getAppealReasonId());
			logger.info("operType : {}", operType);
	
			if("ZTYS".equals(operType)){//中台预受理
				order.setOrderType("ZTYS");
			}
			if(StringUtils.isNotEmpty(order.getOrderType())) {
				TsmStaff staff = pubFun.getStaff(staffId);
				order.setAccpetStaffCode(staff.getLogonName());
				order.setAcceptStaffName(staff.getName());
				order.setAccpetOrgName(staff.getOrgName());
				order.setProdInstId(org.apache.commons.lang3.StringUtils.defaultIfEmpty(order.getProdInstId(), "0"));
				order.setServOrderId(orderId);
				logger.info("orderId:{} YSOrder:{}", order.getServOrderId(), order);
				int num = orderAskInfoDao.saveYNSJOrder(order);
				logger.info("YSOrder save：{}", num);
			}
		}
	}

    // 230912 会议取消查询单获取集团编码
	private boolean isTSZXCX(int servType) {
		return StaticData.SERV_TYPE_NEWTS == servType || StaticData.SERV_TYPE_ZX == servType;
	}
	
	private void saveBuopSheetInfo(BuopSheetInfo info, String serviceId, boolean saveFlag) {
		if(saveFlag) {
			info.setServiceOrderId(serviceId);
			int num = orderAskInfoDao.saveBuopSheetInfo(info);
			logger.info("saveBuopSheetInfo serviceId: {} save: {}", serviceId, num);
		} else {
			int num = orderAskInfoDao.updateBuopSheetInfo(info);
			logger.info("saveBuopSheetInfo serviceId: {} update: {}", serviceId, num);
		}
	}
	
}