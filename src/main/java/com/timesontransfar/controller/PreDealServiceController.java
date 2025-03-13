package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.common.web.system.SyncOperationResultImpl;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.IPreDealService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
public class PreDealServiceController {
	private static final Logger log = LoggerFactory.getLogger(PreDealServiceController.class);
	
	@Autowired
	private IPreDealService preDealServiceImpl;
	
	@Autowired
	private SyncOperationResultImpl operImpl;
	
	@PostMapping(value = "/workflow/preDealService/assignBackToAsk")
	public String assignBackToAsk(@RequestBody String sheetInfo) {
		log.info("预受理退单入参：\n{}", sheetInfo);
		SheetPubInfo sheetPubInfo = (SheetPubInfo)JSONObject.toBean(JSONObject.fromObject(sheetInfo), SheetPubInfo.class);
		return preDealServiceImpl.assignBackToAskFull(sheetPubInfo, 0);
	}
	
	@PostMapping(value = "/workflow/preDealService/updateBeforSheet")
	public String updateBeforSheet(@RequestBody String sheetInfo) {
		log.info("预受理工单派单入参:\n{}",sheetInfo);
		JSONObject json = JSONObject.fromObject(sheetInfo);
		SheetPubInfo sheetPubInfo = (SheetPubInfo)JSONObject.toBean(json.optJSONObject("sheetPubInfo"), SheetPubInfo.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(json.optJSONObject("orderInfo"), OrderAskInfo.class);
		BuopSheetInfo buopSheetInfo = (BuopSheetInfo)JSONObject.toBean(json.optJSONObject("customerBusiness"), BuopSheetInfo.class);
		boolean boo = json.optBoolean("boo");
		String sendContent = json.optString("sendContent");
		return preDealServiceImpl.updateBeforSheetNew(sheetPubInfo, orderAskInfo, buopSheetInfo, boo, sendContent, 0);
	}
	
	@PostMapping(value = "/workflow/preDealService/submitDealSheetToDeal")
	public String submitDealSheetToDeal(@RequestBody String sheetInfo) {
		log.info("商机部门和审批派单入参：\n{}",sheetInfo);
		JSONObject json = JSONObject.fromObject(sheetInfo);
		JSONArray arr = json.optJSONArray("sheetPubInfo");
		SheetPubInfo[] workSheetObj = null;
		if(!arr.isEmpty()) {
			workSheetObj = new SheetPubInfo[arr.size()];
			for(int i=0;i<arr.size();i++) {
				workSheetObj[i] = (SheetPubInfo)JSONObject.toBean(arr.getJSONObject(i),SheetPubInfo.class);
			}
		}
		String dealResult = json.optString("dealResult");
		return preDealServiceImpl.submitDealSheetToDeal(workSheetObj,dealResult);
	}
	
	@PostMapping(value = "/workflow/preDealService/saveDealContent")
	public String saveDealContent(
			@RequestParam(value = "sheetId", required = true) String sheetId,
			@RequestParam(value = "regionId", required = true) String regionId,
			@RequestParam(value = "content", required = true) String content) {
		log.info("暂存处理内容:====>  sheetId:{}  regionId:{}  content:{}", sheetId, regionId, content);
		return preDealServiceImpl.saveDealContent(sheetId, regionId, content);
	}
	
	@PostMapping(value = "/workflow/preDealService/workSheetStatuApply")
	public String workSheetStatuApply(
			@RequestParam(value = "sheetId", required = true) String sheetId,
			@RequestParam(value = "regionId", required = true) int regionId,
			@RequestParam(value = "month", required = true) int month,
			@RequestParam(value = "applyReason", required = true) String applyReason,
			@RequestParam(value = "applyType", required = true) int applyType) {
		log.info("释放申请:====>  sheetId:{}  regionId:{}  applyReason:{}  applyType:{}", sheetId, regionId, applyReason, applyType);
		return preDealServiceImpl.workSheetStatuApply(sheetId, regionId, month, applyReason, applyType);
	}
	
	@PostMapping(value = "/workflow/preDealService/unHoldWorkSheet")
	public int unHoldWorkSheet(
			@RequestParam(value = "sheetId", required = true) String sheetId,
			@RequestParam(value = "regionId", required = true) int regionId,
			@RequestParam(value = "month", required = true) int month) {
		log.info("解挂:====>  sheetId:{}  regionId:{}  month:{}", sheetId, regionId, month);
		return preDealServiceImpl.unHoldWorkSheet(sheetId, regionId, month);
	}
	
	@PostMapping(value = "/workflow/preDealService/workSheetCancelApply")
	public String workSheetCancelApply(
			@RequestParam(value = "sheetId", required = true) String sheetId,
			@RequestParam(value = "regionId", required = true) int regionId,
			@RequestParam(value = "month", required = true) int month,
			@RequestParam(value = "applyReason", required = true) String applyReason) {
		log.info("取消申请:====>  sheetId:{}  regionId:{}  month:{}  applyReason:{}", sheetId, regionId, month, applyReason);
		return preDealServiceImpl.workSheetCancelApply(sheetId, regionId, month,applyReason);
	}
	
	@PostMapping(value = "/workflow/preDealService/updateBeforSheetWithQualitative")
	public String updateBeforSheetWithQualitative(@RequestBody String sheetInfo) {
		log.info("商机单后台派单环节直接处理：\n{}",sheetInfo);
		JSONObject obj=JSONObject.fromObject(sheetInfo);
		
		SheetPubInfo sheetPubInfo = (SheetPubInfo)JSONObject.toBean(obj.optJSONObject("sheetPubInfo"),SheetPubInfo.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(obj.optJSONObject("orderAskInfo"),OrderAskInfo.class);
		SJSheetQualitative sjQualitative = (SJSheetQualitative)JSONObject.toBean(obj.optJSONObject("sjQualitative"),SJSheetQualitative.class);
		BuopSheetInfo buopSheetInfo = (BuopSheetInfo)JSONObject.toBean(obj.optJSONObject("customerBusiness"),BuopSheetInfo.class);
		int dealStaff = obj.optInt("dealStaff");
		boolean boo = obj.optBoolean("boo");
		String sendContent = obj.optString("sendContent");
		String res = preDealServiceImpl.updateBeforSheetWithQualitativeNew(sheetPubInfo, orderAskInfo, buopSheetInfo,boo, sendContent, sjQualitative, dealStaff);
		log.info("商机派单提交返回：{}",res);
		return res;
	}
	
	@PostMapping(value = "/workflow/preDealService/submitDealSheetYs")
	public String submitDealSheetYs(@RequestBody String sheetInfo) {
		log.info("商机单部门处理环节直接处理：\n{}",sheetInfo);
		JSONObject obj = JSONObject.fromObject(sheetInfo);
		SheetPubInfo sheetPubInfo = (SheetPubInfo)JSONObject.toBean(obj.optJSONObject("sheetPubInfo"),SheetPubInfo.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(obj.optJSONObject("orderAskInfo"),OrderAskInfo.class);
		BuopSheetInfo buopSheetInfo = (BuopSheetInfo)JSONObject.toBean(obj.optJSONObject("customerBusiness"),BuopSheetInfo.class);
		String res = preDealServiceImpl.submitDealSheetYsNew(sheetPubInfo,orderAskInfo,buopSheetInfo);
		log.info("商机部门处理直接处理提交返回：{}",res);
		return res;
	}
	
	@PostMapping(value = "/workflow/preDealService/dealOperationResult")
	public String dealOperationResult(@RequestBody String reqJson) {
		return operImpl.dealOperationResult(reqJson);
	}
	
}
