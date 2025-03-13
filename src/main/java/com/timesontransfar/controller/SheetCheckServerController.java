package com.timesontransfar.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.sheetCheck.pojo.SheetCheckAdju;
import com.timesontransfar.sheetCheck.pojo.SheetCheckAppeal;
import com.timesontransfar.sheetCheck.pojo.SheetCheckInfo;
import com.timesontransfar.sheetCheck.pojo.SheetCheckObj;
import com.timesontransfar.sheetCheck.service.IsheetCheckServer;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@RefreshScope
@SuppressWarnings("rawtypes")
public class SheetCheckServerController {
	private static Logger log = LoggerFactory.getLogger(SheetCheckServerController.class);
	@Autowired
	private IsheetCheckServer  sheetCheckServerImpl__FACADE__;
	
	@RequestMapping(value = "/workflow/dynamic/validateCheckable")
	public Object validateCheckable(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		String result= sheetCheckServerImpl__FACADE__.validateCheckable(obj.optString("orderId"), obj.optString("sheetId"), obj.optInt("checkType"));
		return ResultUtil.success(result);
	}
	@RequestMapping(value = "/workflow/dynamic/getSheetCheckSchem")
	public Object getSheetCheckSchem(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		List result= sheetCheckServerImpl__FACADE__.getSheetCheckSchem(obj.optInt("typeId"));
		return ResultUtil.success(result);
	}
	@RequestMapping(value = "/workflow/dynamic/saveSheetCheetObj")
	public Object saveSheetCheetObj(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		SheetCheckInfo sheetCheckInfo=(SheetCheckInfo) JSONObject.toBean(obj.optJSONObject("sheetCheckInfo"),SheetCheckInfo.class);
		JSONArray array=obj.optJSONArray("sheetCheckAdju");
		SheetCheckAdju [] sheetCheckAdju=new  SheetCheckAdju[array.size()];
		for (int i = 0; i < array.size(); i++) {
			sheetCheckAdju[i]=(SheetCheckAdju) JSONObject.toBean(array.optJSONObject(i),SheetCheckAdju.class);
		}
		String result= sheetCheckServerImpl__FACADE__.saveSheetCheetObj(sheetCheckInfo,sheetCheckAdju);
		return ResultUtil.success(result);
	}
	@RequestMapping(value = "/workflow/dynamic/getSheetCheckObj")
	public Object getSheetCheckObj(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		SheetCheckObj result= sheetCheckServerImpl__FACADE__.getSheetCheckObj(obj.optString("checkId"));
		return ResultUtil.success(result);
	}
	@RequestMapping(value = "/workflow/dynamic/doAppealCheckSheet")
	public Object doAppealCheckSheet(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		SheetCheckAppeal appealBean=(SheetCheckAppeal) JSONObject.toBean(obj,SheetCheckAppeal.class);
		String result= sheetCheckServerImpl__FACADE__.doAppealCheckSheet(appealBean);
		return ResultUtil.success(result);
	}
	@RequestMapping(value = "/workflow/dynamic/submitAffirmance")
	public Object submitAffirmance(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		String result= sheetCheckServerImpl__FACADE__.submitAffirmance(obj.optString("checkId"));
		return ResultUtil.success(result);
	}
	@RequestMapping(value = "/workflow/dynamic/updateSheetCheetForResave")
	public Object updateSheetCheetForResave(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		SheetCheckInfo sheetCheckInfo=(SheetCheckInfo) JSONObject.toBean(obj.optJSONObject("sheetCheckInfo"),SheetCheckInfo.class);
		JSONArray array=obj.optJSONArray("sheetCheckAdju");
		SheetCheckAdju [] sheetCheckAdju=new  SheetCheckAdju[array.size()];
		for (int i = 0; i < array.size(); i++) {
			sheetCheckAdju[i]=(SheetCheckAdju) JSONObject.toBean(array.optJSONObject(i),SheetCheckAdju.class);
		}
		String result= sheetCheckServerImpl__FACADE__.updateSheetCheetForResave(sheetCheckInfo, sheetCheckAdju);
		return ResultUtil.success(result);
	}
	@RequestMapping(value = "/workflow/dynamic/updateSheetCheetObj")
	public Object updateSheetCheetObj(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		SheetCheckInfo sheetCheckInfo=(SheetCheckInfo) JSONObject.toBean(obj.optJSONObject("sheetCheckInfo"),SheetCheckInfo.class);
		JSONArray array=obj.optJSONArray("sheetCheckAdju");
		SheetCheckAdju [] sheetCheckAdju=new  SheetCheckAdju[array.size()];
		for (int i = 0; i < array.size(); i++) {
			sheetCheckAdju[i]=(SheetCheckAdju) JSONObject.toBean(array.optJSONObject(i),SheetCheckAdju.class);
		}
		String result= sheetCheckServerImpl__FACADE__.updateSheetCheetObj(sheetCheckInfo, sheetCheckAdju);
		return ResultUtil.success(result);
	}
	@RequestMapping(value = "/workflow/dynamic/submitCheckReply")
	public Object submitCheckReply(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		SheetCheckInfo sheetCheckInfo=(SheetCheckInfo) JSONObject.toBean(obj,SheetCheckInfo.class);
		String result= sheetCheckServerImpl__FACADE__.submitCheckReply(sheetCheckInfo);
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getSheetCheckObjHisList")
	public Object getSheetCheckObjHisList(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		List sheetCheckObjHisList = sheetCheckServerImpl__FACADE__.getSheetCheckObjHisList(obj.optString("checkId"));
		return ResultUtil.success(sheetCheckObjHisList);
	}
	@RequestMapping(value = "/workflow/dynamic/getSheetCheckObjHis")
	public Object getSheetCheckObjHis(@RequestBody(required=false) String parm) {
		log.info("进入 模板话, 参数： {}",parm);
		JSONObject obj=JSONObject.fromObject(parm);
		List sheetCheckObjList = sheetCheckServerImpl__FACADE__.getSheetCheckObjHisList(obj.optString("checkId"));
		return ResultUtil.success(sheetCheckObjList);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getQualitySheetList")
	public Object getQualitySheetList(@RequestBody(required=false) String parm) {
		log.info("getQualitySheetList 参数： {}",parm);
		return sheetCheckServerImpl__FACADE__.getQualitySheetList(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getTemplateList")
	public Object getTemplateList(@RequestBody(required=false) String parm) {
		log.info("getTemplateList 参数： {}",parm);
		JSONObject obj = JSONObject.fromObject(parm);
		return sheetCheckServerImpl__FACADE__.getTemplateList(obj.optInt("tacheId"));
	}
	
	@RequestMapping(value = "/workflow/dynamic/allotQualitySheet")
	public Object allotQualitySheet(@RequestBody(required=false) String parm) {
		log.info("allotQualitySheet 参数： {}",parm);
		int result = sheetCheckServerImpl__FACADE__.allotQualitySheet(parm);
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getQualityReturnList")
	public Object getQualityReturnList(@RequestBody(required=false) String parm) {
		log.info("getQualityReturnList 参数： {}",parm);
		return sheetCheckServerImpl__FACADE__.getQualityReturnList(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getCheckSheetList")
	public Object getCheckSheetList(@RequestBody(required=false) String parm) {
		log.info("getCheckSheetList 参数： {}",parm);
		return sheetCheckServerImpl__FACADE__.getCheckSheetList(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getQualityTemplate")
	public Object getQualityTemplate(@RequestBody(required=false) String parm) {
		log.info("getQualityTemplate 参数： {}",parm);
		JSONObject obj = JSONObject.fromObject(parm);
		return sheetCheckServerImpl__FACADE__.getTemplateInfo(obj.optString("templateId"));
	}
	
	@RequestMapping(value = "/workflow/dynamic/saveQualitySheet")
	public Object saveQualitySheet(@RequestBody(required=false) String parm) {
		log.info("saveQualitySheet 参数： {}",parm);
		int result = sheetCheckServerImpl__FACADE__.saveEleAnswer(parm);
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getCheckResultList")
	public Object getCheckResultList(@RequestBody(required=false) String parm) {
		log.info("getCheckResultList 参数： {}",parm);
		return sheetCheckServerImpl__FACADE__.getCheckResultList(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getQualityContent")
	public Object getQualityContent(@RequestBody(required=false) String parm) {
		log.info("getQualityContent 参数： {}",parm);
		JSONObject obj = JSONObject.fromObject(parm);
		return sheetCheckServerImpl__FACADE__.getTemplateResult(obj.optString("sheetId"), obj.optString("templateId"));
	}
	
	@RequestMapping(value = "/workflow/dynamic/saveQualityAppeal")
	public Object saveQualityAppeal(@RequestBody(required=false) String parm) {
		log.info("saveQualityAppeal 参数： {}",parm);
		int result = sheetCheckServerImpl__FACADE__.saveQualityAppeal(parm);
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getCheckAppealList")
	public Object getCheckAppealList(@RequestBody(required=false) String parm) {
		log.info("getCheckAppealList 参数： {}",parm);
		return sheetCheckServerImpl__FACADE__.getCheckAppealList(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/saveQualityApprove")
	public Object saveQualityApprove(@RequestBody(required=false) String parm) {
		log.info("saveQualityApprove 参数： {}",parm);
		int result = sheetCheckServerImpl__FACADE__.updateQualityContent(parm);
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getQualityQueryList")
	public Object getQualityQueryList(@RequestBody(required=false) String parm) {
		log.info("getQualityQueryList 参数： {}",parm);
		return sheetCheckServerImpl__FACADE__.getQualityQueryList(parm);
	}
	
	
}
