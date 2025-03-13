package com.timesontransfar.pubWebservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.pojo.IntfLog;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.transfar.common.log.CustomLogger;
import com.transfar.common.log.LogBean;

import net.sf.json.JSONObject;

@Component
public class RecDealContentByZD {
	@Autowired
	private CustomLogger logger;
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	@Autowired
	private IworkSheetBusi workSheetBusi;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private PubFunc pubFun;

	private static final String SQL_DEAL = "SELECT W.SERVICE_ORDER_ID,W.REGION_ID,W.LOCK_FLAG,W.MONTH_FLAG FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK O "
			+ "WHERE W.SERVICE_ORDER_ID=O.SERVICE_ORDER_ID AND O.SERVICE_DATE=3 AND W.SHEET_TYPE=720130028 AND W.RECEIVE_ORG_ID='282'AND W.LOCK_FLAG "
			+ "IN(0,1)AND W.WORK_SHEET_ID=?";

	@SuppressWarnings("rawtypes")
	public String recZDDealContent(String reqJson) {
		JSONObject models = JSONObject.fromObject(reqJson);
		String sheetId = models.getString("sheetId");
		String dealResult = models.getString("dealResult");
		logger.info("interface", new LogBean("recZDDealContent", sheetId, null, reqJson, null));
		try {
			if (null == dealResult || dealResult.length() == 0) {
				return returnJson(reqJson, sheetId, 1, "处理结果为空");
			}
			if (dealResult.length() > 1500) {
				return returnJson(reqJson, sheetId, 1, "处理结果长度过长");
			}
			List dealList = jdbcTemplate.queryForList(SQL_DEAL, sheetId);
			if (dealList.isEmpty()) {
				return returnJson(reqJson, sheetId, 1, "工单号不存在或已处理");
			}
			Map dealMap = ((Map) dealList.get(0));
			int lockFlag = Integer.parseInt((dealMap.get("LOCK_FLAG") == null ? "0" : dealMap.get("LOCK_FLAG")).toString());
			int regionId = Integer.parseInt((dealMap.get("REGION_ID") == null ? "0" : dealMap.get("REGION_ID")).toString());
			Integer monthFlag = Integer.valueOf((dealMap.get("MONTH_FLAG") == null ? "0" : dealMap.get("MONTH_FLAG")).toString());
			if (lockFlag == 0) {
				workSheetBusi.fetchWorkSheet(sheetId + "@NOC001", regionId, monthFlag);
			}
			String res = tsWorkSheetDeal.snxcSumbitOrgDeal(sheetId, regionId, monthFlag, dealResult);
			if ("SUCCESS".equals(res)) {
				return returnJson(reqJson, sheetId, 0, "处理成功");
			} else {
				return returnJson(reqJson, sheetId, -1, res);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return returnJson(reqJson, sheetId, -1, e.getMessage());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private String returnJson(String reqJson, String sheetId, int code, String msg) {
		IntfLog log = new IntfLog();
		log.setServOrderId(StringUtils.defaultIfEmpty(sheetId, "null"));
		log.setInMsg(reqJson);
		log.setOutMsg(msg);
		log.setActionFlag("in");
		log.setActionResult(code == 0 ? "1" : "0");
		log.setSystem("ZDREC");
		pubFun.saveYNSJIntfLog(log);
		logger.info("interface", new LogBean("recZDDealContent", sheetId, String.valueOf(code), reqJson, msg));
		Map map = new HashMap();
		map.put("code", code);
		map.put("msg", msg);
		return new Gson().toJson(map);
	}
}