package com.timesontransfar.async;

import java.util.Map;
import java.util.concurrent.Future;

import com.timesontransfar.customservice.orderask.pojo.*;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.impl.PreDealServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.command.AsyncResult;
import com.timesontransfar.complaintservice.service.ICustomerJudgeService;
import com.timesontransfar.complaintservice.service.IRetrieveEvaluation;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.labelmanage.service.ILabelManageService;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.service.impl.TsSheetSumbitImpl;
import com.timesontransfar.dapd.service.IdapdSheetInfoService;
import com.transfar.common.enums.ResultEnum;

/**
 * 异步调用
 */
@Component(value = "asyncTask")
@SuppressWarnings("rawtypes")
public class AsyncTask {
	protected Logger log = LoggerFactory.getLogger(AsyncTask.class);
	
	@Autowired
	private ILabelManageService labelManageService;
	@Autowired
	private ICustomerJudgeService customerJudgeService;
	@Autowired
	private IRetrieveEvaluation retrieveEvaluation;
	@Autowired
	private IdapdSheetInfoService dapdSheetService;
	@Autowired
	private TsSheetSumbitImpl tsSheetSumbitImpl;
	@Autowired
	private PreDealServiceImpl preDealService;

	@Autowired
	private ItsWorkSheetDao	tsWorkSheetDao;//投诉DAO

	// 异步方式
	@HystrixCommand(fallbackMethod = "setError")
	public Future<String> setResultAsync(String map, String method) {
		return new AsyncResult<String>() {
			@Override
			public String invoke() {
				try {
					log.info("异步请求方法：************************************");
					return "success";
				} catch (Exception e) {
					return "exception";
				}
			}
			@Override
			public String get() {
				return invoke();
			}
		};
	}

	public String setError(String map, String method) {
		log.error("{} 异步执行: 失败 {}", method, map);
		return ResultEnum.ERROR.getMessage();
	}

	@Async
	public void submitServiceOrderAsync(String orderId, ServiceLabel label, CustomerPersona persona,
			ServiceOrderInfo soInfo, ServiceContentSave[] saves, ComplaintInfo compInfo) {
		log.info("submitServiceOrderAsync begin");
		long startTime = System.currentTimeMillis();
		if (null == soInfo) {
			log.info("ServiceOrderInfo is null, orderId is {}, 耗时:{}", orderId,
					(System.currentTimeMillis() - startTime));
			return;
		}
		OrderAskInfo orderAskInfo = soInfo.getOrderAskInfo();
		ServiceContent servContent = soInfo.getServContent();
		OrderCustomerInfo custInfo = soInfo.getOrderCustInfo();
		labelManageService.submitServiceOrderAsync(orderId, orderAskInfo, custInfo, persona);
		if (0 == servContent.getSixCatalog() && (StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType()
				|| StaticData.SERV_TYPE_CX == orderAskInfo.getServType())) {
			dapdSheetService.submitDapdSheetInfo(soInfo, saves, label, persona, compInfo);
		}
		if(saves != null) {//投诉/查询工单打标
			labelManageService.trafficLabel(orderAskInfo);
		}
		log.info("submitServiceOrderAsync 耗时：{}", (System.currentTimeMillis() - startTime));
	}

	@Async
	public void sendNoteToManagerPhone(String orderId, String accNum, int regionId, int prodType, int askStaffId) {
		// 判断录单为政企客户的投诉单，则发送受理短信至该客户的客户经理
		String accNbrType = "9";
		if (prodType == 100000002 || prodType == 1) {
			accNbrType = "1";// 固话
		} else if (prodType == 100000009 || prodType == 100000011 || prodType == 100000012 || prodType == 100000014 || prodType == 2) {
			accNbrType = "2";// 宽带
		} else if (prodType == 100000379 || prodType == 9) {
			accNbrType = "9";// CDMA
		} else if (prodType == 100000881 || prodType == 881) {
			accNbrType = "881";// ITV
		}
		labelManageService.sendNoteToManagerPhone(orderId, accNum, regionId, accNbrType, askStaffId);
	}

	@Async
	public void autoPdAsync(String orderId) {
		log.info("autoPdAsync orderId: {}", orderId);
		Map map = tsWorkSheetDao.getDispatchOrgMap(orderId, "系统自动转派");
		if (!map.isEmpty()) {// 优先判断省投直派规则
			String receiveOrg = defaultMapValueIfNull(map, "DISPATCH_ORG", "");
			int dealStaffId = Integer.parseInt(defaultMapValueIfNull(map, "DEAL_STAFF_ID", "0"));
			log.info("autoPdAsync: {}", map);
			if (StringUtils.isNotBlank(receiveOrg) && 0 != dealStaffId) {
				tsSheetSumbitImpl.autoDispatchSheet(orderId, 0, receiveOrg, dealStaffId);
			}
		} else {// 再判断分公司智能转派后台派单工单规则
			map = tsWorkSheetDao.getDispatchOrgMap(orderId, "智能转派后台派单虚拟岗");
			if (!map.isEmpty()) {
				int dealStaffId = Integer.parseInt(defaultMapValueIfNull(map, "DEAL_STAFF_ID", "0"));
				log.info("autoHtpdAsync: {}", map);
				if (0 != dealStaffId) {
					tsSheetSumbitImpl.autoDispatchSheet(orderId, dealStaffId, "", dealStaffId);
				}
			}
		}
	}

	private String defaultMapValueIfNull(Map map, String key, String defaultStr) {
		return map.get(key) == null ? defaultStr : map.get(key).toString();
	}

	@Async
	public void autoFinish(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, boolean boo, String sendContent, int dealStaff) {
		log.info("updateBeforSheetNew orderId: {}", orderAskInfo.getServOrderId());
		preDealService.updateBeforSheetNew(sheetPubInfo, orderAskInfo, buopSheetInfo, boo, sendContent, dealStaff);
	}

	@Async
	public void enterJudgeAsync(String orderId) {
		long startTime = System.currentTimeMillis();
		log.info("enterJudgeAsync orderId：{}", orderId);
		customerJudgeService.enterJudge(orderId);
		log.info("enterJudgeAsync 耗时：{}", (System.currentTimeMillis() - startTime));
	}

	@Async
	public void unlockAutoVisit() {
		customerJudgeService.unlockAutoVisitJob();
	}

	@Async
	public void retrieveEvaluation(String orderId, String code, String msg, String joinMode) {
		log.info("serviceOrderId: {} assessCode: {} assessMsg: {} joinMode: {}", orderId, code, msg, joinMode);
		retrieveEvaluation.retrieveEvaluation(orderId, code, msg, joinMode);
	}
	
	@Async
	public void updateCallFlag(String orderId, String prodNum, String relaInfo, int regionId) {
		long startTime = System.currentTimeMillis();
		labelManageService.updateCallFlag(orderId, prodNum, relaInfo, regionId);
		log.info("updateCallFlag 异步打标 耗时：{}", (System.currentTimeMillis() - startTime));
	}
	
	@Async
	public void updateCustType(String orderId, String prodNum, int regionId) {
		long startTime = System.currentTimeMillis();
		labelManageService.updateCustType(orderId, prodNum, regionId);
		log.info("updateCustType 异步打标 耗时：{}", (System.currentTimeMillis() - startTime));
	}
}