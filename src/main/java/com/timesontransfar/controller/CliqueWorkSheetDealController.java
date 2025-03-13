package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@EnableAsync
public class CliqueWorkSheetDealController {
	
	protected Logger log = LoggerFactory.getLogger(CliqueWorkSheetDealController.class);
	
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	
	@PostMapping(value = "/workflow/cliqueWorkSheet/dispatchSheet")
	public String dispatchSheet(@RequestBody String model) {
		JSONObject obj = JSONObject.fromObject(model);
		JSONArray array = JSONArray.fromObject(obj.optString("workSheetObj"));
		SheetPubInfo[] sheetPubInfo = new SheetPubInfo[array.size()];
		for (int i = 0; i < array.size(); i++) {
			SheetPubInfo item = (SheetPubInfo)JSONObject.toBean(array.getJSONObject(i),SheetPubInfo.class);
			sheetPubInfo[i] = item;
		}

		String result = "";
		try {
			result = tsWorkSheetDeal.dispatchSheet(sheetPubInfo);
		} catch (Exception e) {
			return "FAIL";
		}
		return result;
	}

	@PostMapping(value = "/workflow/cliqueWorkSheet/orgDealDispathSheet")
	public String orgDealDispathSheet(@RequestBody String model) {
		JSONObject obj = JSONObject.fromObject(model);
		JSONArray array = JSONArray.fromObject(obj.optString("workSheetObj"));
		SheetPubInfo[] sheetPubInfo = new SheetPubInfo[array.size()];
		for (int i = 0; i < array.size(); i++) {
			SheetPubInfo item = (SheetPubInfo)JSONObject.toBean(array.getJSONObject(i),SheetPubInfo.class);
			sheetPubInfo[i] = item;
		}
		String dealResult = obj.optString("dealResult");
		int askSource = obj.optInt("askSource");
		int dealType = obj.optInt("dealType");
		return tsWorkSheetDeal.orgDealDispathSheet(sheetPubInfo, dealResult, askSource, dealType, 0);
	}
	
	@PostMapping(value = "/workflow/cliqueWorkSheet/submitAuitSheetToDeal")
	public String submitAuitSheetToDeal(@RequestBody String model) {
		JSONObject obj = JSONObject.fromObject(model);
		JSONArray array = JSONArray.fromObject(obj.optString("workSheetObj"));
		SheetPubInfo[] sheetPubInfo = new SheetPubInfo[array.size()];
		for (int i = 0; i < array.size(); i++) {
			SheetPubInfo item = (SheetPubInfo)JSONObject.toBean(array.getJSONObject(i),SheetPubInfo.class);
			sheetPubInfo[i] = item;
		}
		String acceptContent = obj.optString("acceptContent");
		int dealType = obj.optInt("dealType");
		return tsWorkSheetDeal.submitAuitSheetToDeal(sheetPubInfo, acceptContent, dealType);
	}

}