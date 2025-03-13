package com.timesontransfar.customservice.worksheet.service.impl;

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.service.ITrackService;
import com.timesontransfar.customservice.worksheet.service.OrderRefundService;
import com.timesontransfar.customservice.worksheet.service.RefundTrackService;
import com.timesontransfar.feign.custominterface.InterfaceFeign;

@Service
public class RefundTrackServiceImpl implements RefundTrackService {
	protected Logger log = LoggerFactory.getLogger(RefundTrackServiceImpl.class);
	
	@Autowired
	private InterfaceFeign interfaceFeign;
	
	@Autowired
	private ITrackService trackServiceImpl;
	
	@Autowired
	private OrderRefundService orderRefundService;
	
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;
    
	@Autowired
	private IorderCustInfoDao orderCustInfoDao;

	
	/**
	 * 自动办结审核通过的小额退赔单
	 */
	public int autoFinishRefundOrder(String param) {
		log.info("autoFinishRefundOrder: {}", param);
		JSONObject json = JSON.parseObject(param);
		String trackOrderId = json.getString("TRACK_ORDER_ID");//跟踪单号
		String orderId = json.getString("SERVICE_ORDER_ID");//查询单号
		String refundOrderId = json.getString("REFUND_ORDER_ID");//退费工单号
		String refundAmount = json.getString("REFUND_AMOUNT");//退费金额
		String rechageStatus = json.getString("RECHAG_STATUS");//充值状态
		try {
			String adjustFlag = "";//到账状态
			String updateDate = null;//到账时间
			JSONObject reqJson = new JSONObject();
			reqJson.put("refundOrderId", refundOrderId);
			log.info("qryAdjustPayment refundOrderId: {}", refundOrderId);
			String qryResult = interfaceFeign.qryAdjustPayment(reqJson.toJSONString());//根据退费工单号查询充值流水记录
			JSONObject adjustResult = JSON.parseObject(qryResult);
			String code = adjustResult.getString("code");
			if("1".equals(code)) {//查询成功
				JSONObject data = adjustResult.getJSONObject("data");
				if(data == null || data.isEmpty()) {//无记录
					//按未到账处理
					adjustFlag = "fail";
				} else {
					BigDecimal amount = data.getBigDecimal("amount");//金额
					String paymentId = data.getString("paymentId");//支付流水
					if(amount.compareTo(new BigDecimal(refundAmount)) == 0 && StringUtils.isNotBlank(paymentId)) {//成功到账
						adjustFlag = "success";
						String accNbr = data.getString("accNbr");//号码
						updateDate = StringUtils.defaultIfBlank(data.getString("updateDate"), null) ;//充值时间
						String dealContent = "号码" + accNbr + "于" + updateDate + "成功充值" + amount + "，退费工单号：" + refundOrderId + "，支付流水：" + paymentId;
						
						//自动审核未解挂工单，需要先解挂再办结
						int result = orderRefundService.unHangupTrackOrder(trackOrderId);
						log.info("trackOrderId: {} unHangupTrackOrder: {}", trackOrderId, result);
						
						//办结跟踪单
						String finishResult = trackServiceImpl.autofinishTrackOrder(trackOrderId, dealContent, 702020313, "SYSAT001");//业务规则 > 计费规则 > 有规则，用户不认可 > 小流量争议
						log.info("autofinishTrackOrder result: {}", finishResult);
						//更新调账记录
						this.modifyTrackInfo(trackOrderId, accNbr, refundAmount, dealContent, "SYSAT001");
						//自动办结查询单
						boolean finishFlag = this.isNeedFinishOldOrder(trackOrderId);
						log.info("orderId: {} finishFlag: {}", orderId, finishFlag);
						if(finishFlag) {
							finishResult = trackServiceImpl.finishTrackOrder(orderId, trackOrderId);
							log.info("finishTrackOrder result: {}", finishResult);
						}
					} else {//金额不一致
						//按未到账处理
						adjustFlag = "fail";
					}
				}
			}
			//更新充值状态
			return orderRefundService.updateRechageStatus(trackOrderId, adjustFlag, updateDate, rechageStatus);
		} catch (Exception e){
			log.error("autoFinishRefundOrder error: {}", e.getMessage(), e);
		}
		return 0;
	}
	
	/**
	 * 判断原单是否需要自动办结
	 * @param trackOrderId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean isNeedFinishOldOrder(String trackOrderId) {
		Map info = trackServiceImpl.getTrackInfo(trackOrderId);
		log.info("TrackInfo: {}", JSON.toJSON(info));
		if(info == null) {
			return false;
		}
		String createType = info.get("CREATE_TYPE").toString();//新单创建方式：1、自动生成，2、服务受理，3、接口受理
		return !"1".equals(createType);
	}
	
	private void modifyTrackInfo(String trackOrderId, String refundNum, String refundAmount, String dealContent, String dealStaff) {
		try {
			OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(trackOrderId, true);//查询历史
			if(orderAskInfo == null) {
				log.error("trackOrderId: {} getOrderAskInfoHis: null", trackOrderId);
				return;
			}
			OrderCustomerInfo custInfo = orderCustInfoDao.getOrderCustByGuid(orderAskInfo.getCustId(), true);
			if(custInfo == null) {
				log.error("trackOrderId: {} getOrderCustByGuidHis: null", trackOrderId);
				return;
			}
			JSONObject track = new JSONObject();
			track.put("newOrderId", trackOrderId);
			track.put("refundMode", 1);
			track.put("refundModeDesc", "充值");
			track.put("dealMode", "1");
			track.put("refundReason", "4");
			track.put("refundReasonDesc", "基础功能费（来电名片、漏话助理等）");
			track.put("refundAmount", refundAmount);
			track.put("refundContent", dealContent);
			track.put("paidReason", "");
			track.put("paidReasonDesc", "");
			track.put("paidContent", "");
			track.put("paidAmount", 0);
			track.put("opStaff", "");
			track.put("rechargeMode", "");
			track.put("rechargeModeDesc", "");
			track.put("rechargeNumber", refundNum);
			track.put("approvePerson", "");
			track.put("ownerName", custInfo.getCustName());
			
			JSONObject json = new JSONObject();
			json.put("modiFlag", true);
			json.put("oldMode", 99);
			json.put("oldModeDesc", "无");
			json.put("track", track);
			log.info("modifyTrackInfo param: {}", json.toJSONString());
			String result = trackServiceImpl.modifyTrackInfo(json.toJSONString(), dealStaff);
			log.info("modifyTrackInfo result: {}", result);
		} catch (Exception e){
			log.error("modifyTrackInfo error: {}", e.getMessage(), e);
		}
	}
}
