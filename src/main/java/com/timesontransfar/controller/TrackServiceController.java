package com.timesontransfar.controller;

import com.timesontransfar.customservice.common.PubFunc;
import com.transfar.common.utils.StringUtils;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.timesontransfar.customservice.worksheet.service.ItsSheetDealPub;
import com.timesontransfar.trackservice.serice.ITrackServcie;

@RestController
public class TrackServiceController {
	protected Logger log = LoggerFactory.getLogger(TrackServiceController.class);
	
	@Autowired
	private ITrackServcie trackServcie;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private ItsSheetDealPub tsSheetDealPubImpl;
	
	@PostMapping(value = "/workflow/trackService/getOrderHasten")
	public String getOrderHasten(@RequestParam(value="orderId", required=true) String orderId,
								@RequestParam(value="boo", required=true) boolean boo) {
		return trackServcie.findOrderHasten(orderId,boo);
	}
	
	@PostMapping(value = "/workflow/trackService/getWorkSheetHis")
	public String getWorkSheetHis(@RequestParam(value="prodNum", required=true) String prodNum,
			@RequestParam(value="relaInfo", required=true) String relaInfo,
			@RequestParam(value="regionId", required=true) int regionId,
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="cliqueFlag", required=false) boolean cliqueFlag) {
		return trackServcie.queryWorkSheetHis(prodNum, relaInfo, regionId, orderId, cliqueFlag);
	}
	
	@PostMapping(value = "/workflow/trackService/getWorkSheetHisCount")
	public int getWorkSheetHisCount(@RequestParam(value="prodNum", required=true) String prodNum,
			@RequestParam(value="relaInfo", required=true) String relaInfo,
			@RequestParam(value="regionId", required=true) int regionId,
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="cliqueFlag", required=false) boolean cliqueFlag) {
		return trackServcie.queryWorkSheetHisCount(prodNum, relaInfo, regionId, orderId, cliqueFlag);
	}
	
	@PostMapping(value = "/workflow/trackService/limiteInfo")
	public String limiteInfo(@RequestParam(value="sheetId", required=true) String sheetId) {
		return trackServcie.sheetLimiteInfo(sheetId);
	}
	
	@PostMapping(value = "/workflow/trackService/getCliqueSheet")
	public ComplaintRelation getCliqueSheet(@RequestParam(value="orderId", required=true) String orderId) {
		return pubFunc.queryListByOid(orderId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/workSheetAlot")
	public Object workSheetAlot(@RequestBody(required=false) String parm) {
		String querySysContolSwitch = pubFunc.querySysContolFlag("batchAllotWorkSheet.flag");
		if("2".equals(querySysContolSwitch)){//正在派发中
			return ResultUtil.error("当前正在派发！");
		}
		log.info("workSheetAlot param: {}", parm);
		String addStr = "";
		List<String> paramList = new ArrayList<>();
		if(StringUtils.isNotEmpty(parm)){
			JSONObject json = JSONObject.fromObject(parm);
			if(json.optString("servType").length()>0){
				addStr += " AND A.SERVICE_TYPE=?";
				paramList.add(json.optString("servType"));
			}
			if(json.optString("dealTachid").length()>0){
				addStr += " AND A.SHEET_TYPE=?";
				paramList.add(json.optString("dealTachid"));
			}
			if(json.optString("sheetStatu").length()>0){
				addStr += " AND A.SHEET_STATU=?";
				paramList.add(json.optString("sheetStatu"));
			}
			if(json.optBoolean("isSendDateTime")){
				addStr += " AND A.LOCK_DATE > STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')";
				addStr += " AND A.LOCK_DATE < STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s')";
				paramList.add(json.optJSONArray("sendDate").optString(0));
				paramList.add(json.optJSONArray("sendDate").optString(1));
			}
		}
		log.info("workSheetAlot addStr: {} param: {}", addStr, JSON.toJSON(paramList));
		String reuslt = tsSheetDealPubImpl.autoAllotWorkSheet(addStr, paramList);
		return ResultUtil.success(reuslt);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getCallOutRecord")
	public Object getCallOutRecord(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = json.optString("orderId");
		boolean curFlag = json.optBoolean("curFlag");
		return trackServcie.getCallOutRecord(orderId, curFlag);
	}

	@RequestMapping(value = "/workflow/dynamic/getCallOutForOrderInfo")
	public Object getCallOutForOrderInfo(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = json.optString("orderId");
		String orgId = json.optString("orgId");
		boolean curFlag = json.optBoolean("curFlag");
		return trackServcie.getCallOutForOrderInfo(orderId,orgId,curFlag);
	}

	@RequestMapping(value = "/workflow/dynamic/savePromise")
	public Object savePromise(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String staffId = json.optString("staffId");
		String staffName = json.optString("staffName");
		int type = json.optInt("type");
		return trackServcie.savePromise(staffId, staffName, type);
	}

	@RequestMapping(value = "/workflow/dynamic/getPromise")
	public Object getPromise(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String staffId = json.optString("staffId");
		int type = json.optInt("type");
		return trackServcie.getPromise(staffId, type);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getSatisfyCallOutRecord")
	public Object getSatisfyCallOutRecord(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = json.optString("orderId");
		boolean curFlag = json.optBoolean("curFlag");
		return trackServcie.getSatisfyCallOutRecord(orderId, curFlag);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getCallOutCount")
	public Object getCallOutCount(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = json.optString("orderId");
		int count = trackServcie.getCallOutCount(orderId);
		return ResultUtil.success(count);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getPlayVoiceFlag")
	public Object getPlayVoiceFlag(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = json.optString("orderId");
		boolean curFlag = json.optBoolean("curFlag");
		return trackServcie.getPlayVoiceFlag(orderId, curFlag);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getSatisfyPlayVoiceFlag")
	public Object getSatisfyPlayVoiceFlag(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = json.optString("orderId");
		boolean curFlag = json.optBoolean("curFlag");
		return trackServcie.getSatisfyPlayVoiceFlag(orderId, curFlag);
	}

	@PostMapping(value = "/workflow/trackService/getComplaintOrderDetail")
	public String getComplaintOrderDetail(@RequestParam(value="orderId", required=true) String orderId) {
		return pubFunc.getComplaintOrderDetail(orderId);
	}

	@PostMapping(value = "/workflow/trackService/getWorkSheetHisByPordNum")
	public String getWorkSheetHisByPordNum(@RequestParam(value="prodNum", required=true) String prodNum,
										   @RequestParam(value="beginTime", required=true) String beginTime,
										   @RequestParam(value="endTime", required=true) String endTime,
										   @RequestParam(value="orderId", required=true)String orderId){
		return trackServcie.getWorkSheetHisByPordNum(prodNum, beginTime,endTime,orderId);
	}

}
