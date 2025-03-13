package com.timesontransfar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;

import net.sf.json.JSONObject;

@RestController
public class SheetActionInfoDaoController {
	
	@Autowired
	private ISheetActionInfoDao  sheetActionInfoDao;
	
	@RequestMapping(value = "/workflow/sheetActionInfoDao/saveSheetActionInfo", method = RequestMethod.POST)
	public int saveSheetActionInfo(@RequestParam(value="sheetActionInfo", required=true) String sheetActionInfo) {
		SheetActionInfo info = (SheetActionInfo)JSONObject.toBean(JSONObject.fromObject(sheetActionInfo),SheetActionInfo.class);
		return sheetActionInfoDao.saveSheetActionInfo(info);
	}
	
}
