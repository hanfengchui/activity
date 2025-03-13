package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.worksheet.dao.IHastenSheetInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;

import net.sf.json.JSONObject;

@RestController
public class HastenSheetInfoDaoController {
	protected static Logger log = LoggerFactory.getLogger(HastenSheetInfoDaoController.class);
	
	@Autowired
	private IHastenSheetInfoDao hastenSheetInfoDaoImpl;
	
	@RequestMapping(value = "/workflow/hastenSheetInfoDao/getHastenSheetInfo", method = RequestMethod.POST)
	public HastenSheetInfo[] getHastenSheetInfo(@RequestParam(value="sheetId", required=true) String sheetId,
			@RequestParam(value="boo", required=true) boolean boo) {
		return hastenSheetInfoDaoImpl.getHastenSheetInfo(sheetId, boo);
	}
	
	@RequestMapping(value = "/workflow/hastenSheetInfoDao/saveHastenSheet", method = RequestMethod.POST)
	public String saveHastenSheet(@RequestBody String hasten) {
		HastenSheetInfo bean = (HastenSheetInfo)JSONObject.toBean(JSONObject.fromObject(hasten),HastenSheetInfo.class);
		return hastenSheetInfoDaoImpl.saveHastenSheet(bean) > 0 ? "SUCCESS" : "FAIL";
	}
	
	@RequestMapping(value = "/workflow/hastenSheetInfoDao/delHastenSheet", method = RequestMethod.POST)
	public int delHastenSheet(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="month", required=true)Integer month) {
		return hastenSheetInfoDaoImpl.delHastenSheet(orderId, month);
	}
	
	@RequestMapping(value = "/workflow/hastenSheetInfoDao/savHastenSheetInfoHis", method = RequestMethod.POST)
	public int savHastenSheetInfoHis(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="month", required=true)Integer month) {
		return hastenSheetInfoDaoImpl.savHastenSheetInfoHis(orderId, month);
	}
	
	@RequestMapping(value = "/workflow/hastenSheetInfoDao/getOrderHatenInfo", method = RequestMethod.POST)
	public  HastenSheetInfo[] getOrderHatenInfo(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="boo", required=true)boolean boo) {
		return hastenSheetInfoDaoImpl.getOrderHatenInfo(orderId, boo);
	}
	
	@RequestMapping(value = "/workflow/hastenSheetInfoDao/getListHastenInfo", method = RequestMethod.POST)
	public HastenSheetInfo[] getListHastenInfo(@RequestParam(value="tachId", required=true)String strWhere,
			@RequestParam(value="tachId", required=true)boolean boo) {
		return hastenSheetInfoDaoImpl.getListHastenInfo(strWhere, boo);
	}
	
	@RequestMapping(value = "/workflow/hastenSheetInfoDao/updateSheetHastentNum", method = RequestMethod.POST)
	public int updateSheetHastentNum(@RequestParam(value="sheetId", required=true)String sheetId,@RequestParam(value="month", required=true)Integer month) {
		return hastenSheetInfoDaoImpl.updateSheetHastentNum(sheetId, month);
	}

	@RequestMapping(value = "/workflow/hastenSheetInfoDao/updateRegion", method = RequestMethod.POST)
	public int updateRegion(
			@RequestParam(value="serviceOrderId", required=true)String serviceOrderId, 
			@RequestParam(value="oldRegion", required=true)int oldRegion, 
			@RequestParam(value="newRegion", required=true)int newRegion, 
			@RequestParam(value="newRegionName", required=true)String newRegionName) {
		return hastenSheetInfoDaoImpl.updateRegion(serviceOrderId, oldRegion, newRegion, newRegionName);
	}
	
}
