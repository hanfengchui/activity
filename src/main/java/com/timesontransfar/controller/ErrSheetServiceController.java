package com.timesontransfar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.common.ErrorSheetQuery;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.conditions.GridDateConditions;
import com.timesontransfar.customservice.errorSheet.errdbgridData.IerrorSheetGridData;
import com.timesontransfar.customservice.errorSheet.service.IerrSheetDeal;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.transfar.common.utils.StringUtils;
import com.transfar.common.web.ResultUtil;
import net.sf.json.JSONObject;

@RestController
public class ErrSheetServiceController {
	protected Logger log = LoggerFactory.getLogger(ErrSheetServiceController.class);
	
	@Autowired
	private IerrorSheetGridData errorSheetGridDataImpl;
	@Autowired
	private GridDateConditions gridDateConditions;
	@Autowired
	private IserviceOrderAsk serviceOrderAskImpl;
	@Autowired
	private IerrSheetDeal errSheetDeal;
	@Autowired
	private ErrorSheetQuery errorSheetQuery;

	@RequestMapping(value = "/workflow/dynamic/getErrAcceptSheet")
	public Object getErrAcceptSheet(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		int currentPage = obj.optInt("currentPage");
		String funId = obj.optString("funcId");
		String strWhere = obj.optString("strWhere");
		String str = gridDateConditions.getStrWhere(funId, strWhere);
		GridDataInfo errAcceptSheet = errorSheetGridDataImpl.getErrAcceptSheet(currentPage, str);
		return ResultUtil.success(errAcceptSheet);
	}

	@RequestMapping(value = "/workflow/dynamic/getErrAuditSheet")
	public Object getErrAuditSheet(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		int currentPage = obj.optInt("currentPage");
		String funId = obj.optString("funcId");
		String strWhere = obj.optString("strWhere");
		String str = gridDateConditions.getStrWhere(funId, strWhere);
		GridDataInfo errAcceptSheet = errorSheetGridDataImpl.getErrAuditSheet(currentPage, str);
		return ResultUtil.success(errAcceptSheet);
	}

	@RequestMapping(value = "/workflow/dynamic/getErrOrderById")
	public Object getErrOrderById(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		String orderId = obj.optString("orderId");
		String sheetId = obj.optString("sheetId");
		int queryType = obj.optInt("queryType");
		ServiceOrderInfo errOrderById = serviceOrderAskImpl.getErrOrderById(orderId, queryType, sheetId);
		return errOrderById;
	}

	@RequestMapping(value = "/workflow/dynamic/submitErrSheet")
	public Object submitErrSheet(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		String orderId = obj.optString("orderId");
		String sheetId = obj.optString("sheetId");
		int monthFlag = obj.optInt("monthFlag");
		String[] errInfo = obj.optString("errInfo").split("\\,");
		String submitErrSheet = errSheetDeal.submitErrSheet(orderId, sheetId, monthFlag, errInfo);
		return ResultUtil.success(submitErrSheet);
	}

	@RequestMapping(value = "/workflow/dynamic/submitErrAuditSheet")
	public Object submitErrAuditSheet(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		String orderId = obj.optString("orderId");
		String sheetId = obj.optString("sheetId");
		String submitErrSheet = errSheetDeal.submitErrAuditSheet(orderId, sheetId, obj.optBoolean("errFlag"),
				obj.optString("suredMsg"));
		return ResultUtil.success(submitErrSheet);
	}

	@RequestMapping(value = "/workflow/dynamic/getErrSheetList")
	public Object getErrSheetList(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		int currentPage = obj.optInt("currentPage");
		String funId = obj.optString("funcId");
		String strWhere = obj.optString("strWhere");
		String str = gridDateConditions.getStrWhere(funId, strWhere);
		String queryFlag = obj.optString("queryFlag");
		GridDataInfo errAcceptSheet = new GridDataInfo();
		if ("all".equals(queryFlag)) {
			errAcceptSheet = errorSheetGridDataImpl.getErrAllSheetList(currentPage, str, queryFlag);
		} else {
			errAcceptSheet = errorSheetGridDataImpl.getErrSheetList(currentPage, str);
		}
		return ResultUtil.success(errAcceptSheet);
	}

	@RequestMapping(value = "/workflow/dynamic/getErrSheetHisList")
	public Object getErrSheetHisList(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		int currentPage = obj.optInt("currentPage");
		String funId = obj.optString("funcId");
		String strWhere = obj.optString("strWhere");
		String str = gridDateConditions.getStrWhere(funId, strWhere);
		String queryFlag = obj.optString("queryFlag");
		GridDataInfo errAcceptSheet = new GridDataInfo();
		if ("all".equals(queryFlag)) {
			errAcceptSheet = errorSheetGridDataImpl.getErrAllSheetHisList(currentPage, str, queryFlag);
		} else {
			errAcceptSheet = errorSheetGridDataImpl.getErrSheetHisList(currentPage, str);
		}
		return ResultUtil.success(errAcceptSheet);
	}

	@RequestMapping(value = "/workflow/dynamic/getNullOrgSheet")
	public Object getNullOrgSheet(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		int currentPage = obj.optInt("currentPage");
		JSONObject strWhere = obj.optJSONObject("strWhere");
		StringBuffer strwhere = new StringBuffer();
		if (StringUtils.isNotEmpty(strWhere.optString("orderId"))) {
			strwhere.append(" AND A.SERVICE_ORDER_ID='" + strWhere.optString("orderId") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("sheetId"))) {
			strwhere.append(" AND W.WORK_SHEET_ID='" + strWhere.optString("sheetId") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("prodNum"))) {
			strwhere.append(" AND A.PROD_NUM='" + strWhere.optString("prodNum") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("regionId"))) {
			strwhere.append(" AND A.REGION_ID=" + strWhere.optString("regionId"));
		}
		if (strWhere.optBoolean("timeFlag")) {
			strwhere.append(" AND A.ACCEPT_DATE > STR_TO_DATE('" + strWhere.optString("acceptDate") + "','%Y-%m-%d %H:%i:%s')");
		}
		GridDataInfo nullOrgSheet = errorSheetQuery.getNullOrgSheet(currentPage, strwhere.toString());
		return ResultUtil.success(nullOrgSheet);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getNullDealOrgSheet")
	public Object getNullDealOrgSheet(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		int currentPage = obj.optInt("currentPage");
		JSONObject strWhere = obj.optJSONObject("strWhere");
		StringBuffer strwhere = new StringBuffer();
		if (StringUtils.isNotEmpty(strWhere.optString("orderId"))) {
			strwhere.append(" AND A.SERVICE_ORDER_ID='" + strWhere.optString("orderId") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("sheetId"))) {
			strwhere.append(" AND W.WORK_SHEET_ID='" + strWhere.optString("sheetId") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("prodNum"))) {
			strwhere.append(" AND A.PROD_NUM='" + strWhere.optString("prodNum") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("regionId"))) {
			strwhere.append(" AND A.REGION_ID=" + strWhere.optString("regionId"));
		}
		GridDataInfo nullOrgSheet = errorSheetQuery.getNullDealOrgSheet(currentPage, strwhere.toString());
		return ResultUtil.success(nullOrgSheet);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getTimeOutSheet")
	public Object getTimeOutSheet(@RequestBody(required = false) String parm) {
		JSONObject obj = JSONObject.fromObject(parm);
		int currentPage = obj.optInt("currentPage");
		JSONObject strWhere = obj.optJSONObject("strWhere");
		StringBuffer strwhere = new StringBuffer();
		if (StringUtils.isNotEmpty(strWhere.optString("orderId"))) {
			strwhere.append(" AND A.SERVICE_ORDER_ID='" + strWhere.optString("orderId") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("sheetId"))) {
			strwhere.append(" AND W.WORK_SHEET_ID='" + strWhere.optString("sheetId") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("prodNum"))) {
			strwhere.append(" AND A.PROD_NUM='" + strWhere.optString("prodNum") + "'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("regionId"))) {
			strwhere.append(" AND A.REGION_ID=" + strWhere.optString("regionId"));
		}
		if (StringUtils.isNotEmpty(strWhere.optString("orgId"))) {
			strwhere.append(" AND W.DEAL_ORG_ID='"+strWhere.optString("orgId")+"'");
		}
		if (StringUtils.isNotEmpty(strWhere.optString("orgRevId"))) {
			strwhere.append(" AND W.RECEIVE_ORG_ID='"+strWhere.optString("orgRevId")+"'");
		}
		if (strWhere.optBoolean("timeFlag")) {
			strwhere.append(" AND A.ACCEPT_DATE > STR_TO_DATE('" + strWhere.optString("acceptDate") + "','%Y-%m-%d %H:%i:%s')");
		}
		int hour = obj.optInt("hour");
		GridDataInfo nullOrgSheet = errorSheetQuery.getTimeOutSheet(currentPage, strwhere.toString(), hour);
		return ResultUtil.success(nullOrgSheet);
	}

}
