package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.IYnSheetDealService;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
public class YnSheetDealServiceController {
	protected Logger log = LoggerFactory.getLogger(YnSheetDealServiceController.class);
	
	@Autowired
	private IYnSheetDealService ynSheetDealServiceImpl;
	
	@Autowired
	private IworkSheetBusi workSheetBusi;
	
	@RequestMapping(value = "/workflow/dynamic/submitDealSheetToDeal")
	public Object submitDealSheetToDeal(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		JSONArray array=obj.optJSONArray("sheetPubInfo");
		SheetPubInfo[] workSheetObj=new SheetPubInfo[array.size()];
		for (int i = 0; i <array.size(); i++) {
			SheetPubInfo sheetPubInfo=(SheetPubInfo) JSONObject.toBean(array.optJSONObject(i),SheetPubInfo.class);
			workSheetObj[i]=sheetPubInfo;
		}
		String result=ynSheetDealServiceImpl.submitDealSheetToDealNew(workSheetObj,obj.optString("dealResult"));
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/auitSheetFinish")
	public Object auitSheetFinish(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		RetVisitResult retVisitResult=(RetVisitResult) JSONObject.toBean(obj.optJSONObject("retVisitResult"),RetVisitResult.class);
		String sheetId=obj.optString("sheetId");
		String orgId=obj.optString("orgId");
		String result=ynSheetDealServiceImpl.auitSheetFinish(retVisitResult, sheetId, orgId, obj.optInt("regionId"), obj.optInt("month"));
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/submitAuitSheetToDeal")
	public Object submitAuitSheetToDeal(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		JSONArray array=obj.optJSONArray("sheetPubInfo");
		SheetPubInfo[] workSheetObj=new SheetPubInfo[array.size()];
		for (int i = 0; i <array.size(); i++) {
			SheetPubInfo sheetPubInfo=(SheetPubInfo) JSONObject.toBean(array.optJSONObject(i),SheetPubInfo.class);
			workSheetObj[i]=sheetPubInfo;
		}
		String result=ynSheetDealServiceImpl.submitAuitSheetToDeal(workSheetObj,obj.optString("dealResult"));
		return ResultUtil.success(result);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getRelatSheet")
	public Object getRelatSheet(@RequestBody(required = false) String parm) {
		JSONObject obj=JSONObject.fromObject(parm);
		SheetPubInfo[] relatSheet = workSheetBusi.getRelatSheet(obj.optString("sheetId"),obj.optInt("month"));
		return ResultUtil.success(relatSheet);
	}
	
}
