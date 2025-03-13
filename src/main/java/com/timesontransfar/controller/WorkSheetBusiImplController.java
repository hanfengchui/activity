package com.timesontransfar.controller;

import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.worksheet.pojo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.NoteSendList;
import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SJSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.ServiceWorkSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class WorkSheetBusiImplController {
	protected Logger log = LoggerFactory.getLogger(WorkSheetBusiImplController.class);
	
	@Autowired
	private IworkSheetBusi workSheetBusi;
	
	@PostMapping(value = "/workflow/workSheetBusi/auitSheetFinishWithQualitative")
	public String auitSheetFinishWithQualitative(@RequestBody String sheetInfo) {
		log.info("商机审核入参：\n{}",sheetInfo);
		JSONObject json = JSONObject.fromObject(sheetInfo);
		RetVisitResult retVisitResult = (RetVisitResult)JSONObject.toBean(json.getJSONObject("retVisitResult"),RetVisitResult.class);
		SJSheetQualitative sjQualitative = (SJSheetQualitative)JSONObject.toBean(json.getJSONObject("sjQualitative"),SJSheetQualitative.class);
		BuopSheetInfo buopSheetInfo = (BuopSheetInfo)JSONObject.toBean(json.getJSONObject("customerBusiness"), BuopSheetInfo.class);
		String sheetId = json.optString("sheetId");
		int regionId = json.optInt("regionId");
		int month = json.optInt("month");
		String orgId = json.optString("orgId");
		String res = workSheetBusi.auitSheetFinishWithQualitativeNew(retVisitResult, buopSheetInfo,sheetId, orgId, regionId, month, sjQualitative);
		log.info("商机工单：{} 审核提交返回：{}",sjQualitative.getWorkSheetId(),res);
		return res;
	}
	
	@PostMapping(value = "/workflow/workSheetBusi/submitAuitSheetToDeal")
	public String submitAuitSheetToDeal(@RequestBody String sheetInfo) {
		log.info("商机审核转派入参：\n{}",sheetInfo);
		JSONObject json = JSONObject.fromObject(sheetInfo);
		JSONArray arr = json.optJSONArray("sheetPubInfo");
		SheetPubInfo[] sheetPubInfo = null;
		if(!arr.isEmpty()) {
			sheetPubInfo = new SheetPubInfo[arr.size()];
			for(int i=0;i<arr.size();i++) {
				sheetPubInfo[i] = (SheetPubInfo)JSONObject.toBean(arr.getJSONObject(i),SheetPubInfo.class);
			}
		}
		String acceptContent = json.optString("acceptContent");
		String res = workSheetBusi.submitAuitSheetToDeal(sheetPubInfo,acceptContent);
		log.info("商机工单审核转派工单提交返回：{}",res);
		return res;
	}
	
	@PostMapping(value = "/workflow/workSheetBusi/assignSheetToDeal")
	public String assignSheetToDeal(@RequestBody String sheetInfo) {
		log.info("派单入参：\n{}",sheetInfo);
		JSONArray arr=JSONArray.fromObject(sheetInfo);
		if(arr.isEmpty()) {
			return "no_parm";
		}
		SheetPubInfo[] sheetInfoArry = new SheetPubInfo[arr.size()];
		for(int i=0;i<arr.size();i++) {
			SheetPubInfo sheetPubInfo = (SheetPubInfo)JSONObject.toBean(arr.getJSONObject(i),SheetPubInfo.class);
			sheetInfoArry[i] = sheetPubInfo;
		}
		String res = workSheetBusi.assignSheetToDeal(sheetInfoArry);
		log.info("派单返回：{}",res);
		return res;
	}
		
	@PostMapping(value = "/workflow/workSheetBusi/getServiceSheetInfo")
	public ServiceWorkSheetInfo getServiceSheetInfo(
			@RequestParam(value="sheetId", required=true) String sheetId,
			@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="regionId", required=true) int regionId,
			@RequestParam(value="month", required=true) Integer month,
			@RequestParam(value="flg", required=true) boolean flg,
			@RequestParam(value="vesion", required=false) Integer vesion) {
		return workSheetBusi.getServiceSheetInfo(sheetId, orderId, regionId, month, flg, vesion);
	}

	@PostMapping(value = "/workflow/workSheetBusi/fetchBatchWorkSheet")
	public String fetchBatchWorkSheet(
			@RequestParam(value="sheetIdList", required=true) String[] sheetIdList,
			@RequestParam(value="flowType", required=true) int flowType,
			@RequestParam(value="fetchType", required=true) int fetchType) {
		return workSheetBusi.fetchBatchWorkSheet(sheetIdList, flowType, fetchType);
	}
	
	@PostMapping(value = "/workflow/workSheetBusi/getSheetObjByWorkTime")
	public String getSheetObjByWorkTime(@RequestParam(value="sheetId", required=true) String sheetId,
			@RequestParam(value="hisFlag", required=false) boolean hisFlag) {
		return workSheetBusi.getSheetObjByWorkTime(sheetId, hisFlag);
	}
	
	@PostMapping(value = "/workflow/workSheetBusi/workSheetStatuApply")
	public String workSheetStatuApply(@RequestParam(value="sheetId", required=true) String sheetId,
									@RequestParam(value="region", required=false) int region,
									@RequestParam(value="month", required=false) int month,
									@RequestParam(value="applyReason", required=false) String applyReason,
									@RequestParam(value="applyType", required=false) int applyType) {
		return workSheetBusi.workSheetStatuApply(sheetId, region, month, applyReason, applyType);
	}
	
	@PostMapping(value = "/workflow/workSheetBusi/saveAndqryDealContent")
	public String saveAndqryDealContent(@RequestParam(value="sheetId", required=true) String sheetId,
									@RequestParam(value="region", required=false) int region,
									@RequestParam(value="content", required=false) String content,
									@RequestParam(value="month", required=false) int month,
									@RequestParam(value="orderId", required=false) String orderId) {
		return workSheetBusi.saveAndqryDealContent(sheetId, region, content, month, orderId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/workSheetReplevy")
	public Object workSheetReplevy(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		String result=workSheetBusi.workSheetReplevy(obj.optString("orderId"), obj.optString("workSheetId"), obj.optInt("monthFlag"), obj.optInt("regionId"));
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/submitDealSheet")
	public Object submitDealSheet(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		JSONObject sheetPub=obj.optJSONObject("sheetPubInfo");
		SheetPubInfo sheetPubInfo=(SheetPubInfo) JSONObject.toBean(sheetPub,SheetPubInfo.class);
		String result=workSheetBusi.submitDealSheet(sheetPubInfo, null, null,obj.optString("dealResult"),700001447);
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/releaseBatchWorkSheet")
	public Object releaseBatchWorkSheet(@RequestBody(required = false) String parm) {
		log.info("releaseBatchWorkSheet 入参:{}", parm);
		JSONObject obj=JSONObject.fromObject(parm);
		JSONArray sheetPub=obj.optJSONArray("batchInfo");
		String [] guidList=new String[sheetPub.size()];
		String [] sheetList=new String[sheetPub.size()];
		int [] regionList=new int[sheetPub.size()];
		Integer [] month=new Integer[sheetPub.size()];
		for (int i = 0; i < sheetPub.size(); i++) {
			JSONObject item=sheetPub.optJSONObject(i);
			guidList[i]=item.optString("guid");
			sheetList[i]=item.optString("sheetId");
			regionList[i]=item.optInt("region");
			month[i]=item.optInt("month");
		}
		int releaseBatchWorkSheet = workSheetBusi.releaseBatchWorkSheet(guidList, sheetList, regionList, month, obj.optString("audResult"), obj.optInt("applyAudStatu"), obj.optInt("applyType"));
		return ResultUtil.success(releaseBatchWorkSheet);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getOrgsNotInSheetRegion")
	public Object getOrgsNotInSheetRegion(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		String[] orgIdArr=obj.optString("orgIdArr").split(",");
		String reginId=obj.optString("reginId");
		List result=workSheetBusi.getOrgsNotInSheetRegion(orgIdArr, reginId);
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getStaffNameNotInSheetRegion")
	public Object getStaffNameNotInSheetRegion(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		String[] staffIds=obj.optString("staffIds").split(",");
		String reginId=obj.optString("reginId");
		List result=workSheetBusi.getStaffNameNotInSheetRegion(staffIds, reginId);
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/updateNoteList")
	public Object updateNoteList(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		NoteSendList bean=(NoteSendList) JSONObject.toBean(obj.optJSONObject("noteSend"),NoteSendList.class);
		int month=obj.optInt("month");
		String updateNoteList = workSheetBusi.updateNoteList(bean, month);
		return ResultUtil.success(updateNoteList);
	}
	
	@PostMapping(value = "/workflow/workSheetBusi/cancelWorkFlow")
	public String cancelWorkFlow(@RequestParam(value="wfInstId", required=true)String wfInstId) {
		return workSheetBusi.cancelWorkFlow(wfInstId);
	}

	@PostMapping(value = "/workflow/workSheetBusi/getBuopSheetInfo")
	public Map<String, Object> getBuopSheetInfo(@RequestParam(value="serviceOrderId", required=true)String serviceOrderId) {
		return workSheetBusi.getBuopSheetInfo(serviceOrderId);
	}

	@RequestMapping(value = "/workflow/dynamic/updateReturnList")
	public Object updateReturnList(@RequestBody(required = false) String param) {
		JSONObject obj=JSONObject.fromObject(param);
		ReturnBlackList bean=(ReturnBlackList) JSONObject.toBean(obj.optJSONObject("returnSend"),ReturnBlackList.class);
		String updateReturnList = workSheetBusi.updateReturnList(bean);
		return ResultUtil.success(updateReturnList);
	}

	@RequestMapping(value = "/workflow/dynamic/saveReturnList")
	public Object saveReturnList(@RequestBody(required = false) String param) {
		JSONObject obj=JSONObject.fromObject(param);
		ReturnBlackList bean=(ReturnBlackList) JSONObject.toBean(obj.optJSONObject("returnSend"),ReturnBlackList.class);
		String saveReturnList = workSheetBusi.saveReturnList(bean);
		return ResultUtil.success(saveReturnList);
	}

	@PostMapping(value = "/workflow/workSheetBusi/finishOrderAndSheetByOrderId")
	public Map finishOrderAndSheetByOrderId(@RequestParam(value = "orderId", required = true) String orderId) {
		return workSheetBusi.finishOrderAndSheetByOrderId(orderId);
	}
}