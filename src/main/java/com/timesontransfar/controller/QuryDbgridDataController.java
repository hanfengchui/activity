package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.timesontransfar.customservice.common.QuryDbgridData;
import com.transfar.common.utils.StringUtils;

import net.sf.json.JSONObject;

@RestController
@RefreshScope
public class QuryDbgridDataController {
	protected Logger log = LoggerFactory.getLogger(QuryDbgridDataController.class);

	@Autowired
	private QuryDbgridData quryDbgridData;

	@RequestMapping(value = "/workflow/dynamic/getLocalWaitDealSheet")
	public Object getLocalWaitDealSheet(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		int begion = obj.optInt("begion");
		String addStr = "";
		if (StringUtils.isNotEmpty(obj.optString("strWhere"))) {
			JSONObject json = JSONObject.fromObject(obj.optString("strWhere"));
			if (json.optString("servType").length() > 0) {
				addStr += " AND W.SERVICE_TYPE=" + json.optString("servType");
			}
			if (json.optString("dealTachid").length() > 0) {
				addStr += " AND W.SHEET_TYPE=" + json.optString("dealTachid");
			}
			if (json.optString("sheetStatu").length() > 0) {
				addStr += " AND W.SHEET_STATU=" + json.optString("sheetStatu");
			}
			if (json.optBoolean("isSendDateTime")) {
				addStr += " AND W.CREAT_DATE>STR_TO_DATE('" + json.optJSONArray("sendDate").optString(0) + "','%Y-%m-%d %H:%i:%s')";
				addStr += " AND W.CREAT_DATE<STR_TO_DATE('" + json.optJSONArray("sendDate").optString(1) + "','%Y-%m-%d %H:%i:%s')";
			}
		}
		return quryDbgridData.getLocalWaitDealSheet(begion, addStr);
	}
}