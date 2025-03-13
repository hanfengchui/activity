package com.timesontransfar.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

@RestController
@SuppressWarnings("rawtypes")
public class ItsSheetQualitativeController {
	protected static Logger log = LoggerFactory.getLogger(ItsSheetQualitativeController.class);
	
	@Autowired
	private ItsSheetQualitative sheetQualitative;
	

	@RequestMapping(value = "/workflow/itsSheetQualitative/getQualitativeAndVisit", method = RequestMethod.POST)
	public List getQualitativeAndVisit(@RequestParam(value = "orderId", required = true) String orderId,
			@RequestParam(value = "boo", required = true) boolean boo) {
		return sheetQualitative.getQualitativeAndVisit(orderId, boo);
	}

	@RequestMapping(value = "/workflow/itsSheetQualitative/getQualitativeOrVisit")
	public List getQualitativeOrVisit(@RequestParam(value = "orderId", required = true) String orderId,
			@RequestParam(value = "boo", required = true) boolean boo,
			@RequestParam(value = "cliqueFlag", required = false) boolean cliqueFlag) {
		return sheetQualitative.getQualitativeOrVisit(orderId, boo, cliqueFlag);
	}

	@RequestMapping(value = "/workflow/itsSheetQualitative/getOrderOperationLogs")
	public List getOrderOperationLogs(@RequestParam(value = "orderId", required = true) String orderId,
			@RequestParam(value = "boo", required = true) boolean boo) {
		return sheetQualitative.getOrderOperationLogs(orderId, boo);
	}

	@RequestMapping(value = "/workflow/itsSheetQualitative/getLatestQualitativeByOrderId", method = RequestMethod.POST)
	public TsSheetQualitative getLatestQualitativeByOrderId(@RequestParam(value="orderId", required=true)String orderId,@RequestParam(value="regionId", required=true)int regionId) {
		return sheetQualitative.getLatestQualitativeByOrderId(orderId, regionId);
	}
	
	@RequestMapping(value = "/workflow/itsSheetQualitative/getTsSheetQualitative", method = RequestMethod.POST)
	public TsSheetQualitative[] getTsSheetQualitative(
			@RequestParam(value="sheetId", required=true)String sheetId,
			@RequestParam(value="regionId", required=true)int regionId,
			@RequestParam(value="boo", required=true)boolean boo) {
		return sheetQualitative.getTsSheetQualitative(sheetId, regionId, boo);
	}

	@RequestMapping(value = "/workflow/dynamic/getReceiptEvalObj")
	public Object getReceiptEvalObj(@RequestBody(required=false) String parm) {
		log.info("getReceiptEvalObj 参数： {}",parm);
		JSONObject obj = JSONObject.fromObject(parm);
		String result = sheetQualitative.getReceiptEvalObj(obj.optString("orderId"));
		return ResultUtil.success(result);
	}
}