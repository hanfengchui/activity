package com.timesontransfar.customservice.chiefdesk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.worksheet.service.ItsSheetDealAction;

import net.sf.json.JSONObject;

@RestController
public class ChiefOperatorDeskController {
	protected Logger log = LoggerFactory.getLogger(ChiefOperatorDeskController.class);
    
	@Autowired
	private IChiefOperatorDeskServiceService chiefDeskService;
	@Autowired
	private ItsSheetDealAction tsSheetDealService;
	
	/**
	 * 班长台相关页面的列表数据查询
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@PostMapping(value = "/workflow/ChiefOperatorDeskAction/queryListData")
	public GridDataInfo queryChiefDeskGridData(
			@RequestParam(value="dataType", required=true)String dataType,
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="pageSize", required=true)int pageSize,
			@RequestParam(value="queryType", required=true)int queryType,
			@RequestParam(value="orderId", required=false)String orderId,
			@RequestParam(value="prodNum", required=false)String prodNum,
			@RequestParam(value="realNum", required=false)String realNum,
			@RequestParam(value="dealStaff", required=false)String dealStaff,
			@RequestParam(value="regionId", required=false)String regionId,
			@RequestParam(value="tacheId", required=false)String tacheId,	
			@RequestParam(value="biaoShi", required=false)String biaoShi,	
			@RequestParam(value="custServGrade", required=false)String custServGrade,	
			@RequestParam(value="worksheetType", required=false)String worksheetType,	
			@RequestParam(value="sheetStatus", required=false)String sheetStatus,	
			@RequestParam(value="cuiDanCtn", required=false)String cuiDanCtn,	
			@RequestParam(value="acceptDate", required=false)String acceptDate,	
			@RequestParam(value="lockStart", required=false)String lockStart,
			@RequestParam(value="servType", required=false)String servType) {

		Map<String, String> map = new HashMap<String, String>();
		map.put("dataType", dataType);
		map.put("begin", String.valueOf(begion));
		map.put("serviceOrderId", orderId);
		map.put("prodNum", prodNum);
		map.put("relaPhone", realNum);
		map.put("dealLogonName", dealStaff);
		map.put("regionId", regionId);
		map.put("serviceType", servType);
		map.put("sheetType", worksheetType);
		map.put("sheetTach", tacheId);
		map.put("sheetFlag", biaoShi);
		map.put("custGrade", custServGrade);
		map.put("sheetStatu", sheetStatus);
		map.put("orderHasten", cuiDanCtn);
		
		if(StringUtils.isNotEmpty(acceptDate)) {
			String[] arr = acceptDate.split(",");
			String date1 = arr[0];
			String date2 = arr[1];
			map.put("acceptStart", date1);
			map.put("acceptEnd", date2);
		}
		if(StringUtils.isNotEmpty(lockStart)) {
			String[] arr2 = lockStart.split(",");
			String date3 = arr2[0];
			String date4 = arr2[1];
			map.put("lockStart", date3);
			map.put("lockEnd", date4);
		}
		GridDataInfo list = null;
		Map params = chiefDeskService.buildParams(map, queryType);
		switch (queryType) {
			case 1:// 申请处理页面，申请审批 列表查询
				list = chiefDeskService.querySingleList(params);
				break;// 申请处理页面，批量申请 列表查询
			case 2:
				list = chiefDeskService.queryPatchList(params);
				break;
			case 3:// 班长台页面，列表数据查询
				list = chiefDeskService.queryChiefDesk(params);
				break;
			default:
				break;
		}
		if(list != null) {
			log.info("班长台数据查询 ====> \n{}", params);
			return list;
		}
		return null;
	}

	/**
	 * 班长台页面，列表下方按钮的操作提交
	 */
	@RequestMapping(value = "/workflow/ChiefOperatorDesk/chiefDeskDeal", method = RequestMethod.POST)
	public String chiefDeskDeal(@RequestBody String reqJson) {
		log.info("班长台页面处理入参：{}",reqJson );
		JSONObject models = JSONObject.fromObject(reqJson);
		String rst = "ERROR";
		String operType = models.optString("DEAL_TYPE");
		String staffId  = models.optString("staffId");
		String sheetId  = models.optString("sheetId");
		log.info("班长台页面，operType:{}",operType);
		if ("DISPATCH".equals(operType)) {// 派发工单
			int staff = Integer.parseInt(staffId);
			String[] sheetIds = { sheetId };
			rst = tsSheetDealService.allotBatchSheet(sheetIds, staff, 0);//返回sheetid@sheetid
		}else if ("RELEASE".equals(operType)) {// 释放工单
			String[] sheetIds = { sheetId };
			rst = tsSheetDealService.allotBatchSheet(sheetIds, 0, 1);// 返回sheetid@sheetid
		}  else if ("UPGRADE".equals(operType)) {// 管控升级
			rst = "fail";
		}else if ("SUPERVISE".equals(operType)) {// 建督办单
			rst = "fail";
		}else if ("PATCHDSP".equals(operType)) {
			String dspnum = models.optString("dspnum");
			String servType = models.optString("servType");
			String tachid = models.optString("tachid");
			String staffIds = models.optString("staffIds");
			int dspnums = Integer.parseInt(dspnum);// 平均派单量
			int servTypes = Integer.parseInt(servType);
			int tachids = Integer.parseInt(tachid);
			String[] staff = staffIds.split(",");
			String[] sheetIds = chiefDeskService.getPatchdspSheets(dspnums * staff.length, servTypes, tachids);
			if (staff.length == 1) {
				rst = tsSheetDealService.allotBatchSheet(sheetIds, Integer.parseInt(staff[0]), 0);// 返回sheetid@sheetid
			} else {
				rst = tsSheetDealService.allotBatchSheet(sheetIds, staff);
			}
		} else if ("ASSIGN".equals(operType)) {
			rst = "fail";
		}
		log.info("班长台页面，列表下方按钮的操作提交返回:{}",rst);
		return rst;
	}

	/**
	 * 班长台页面，选择一列后，触发的判断动作<br>
	 * 判断当前是否有督办申请、升级申请
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/workflow/ChiefOperatorDeskAction/checkRowData", method = RequestMethod.POST)
	public String checkRowData(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		Map<String, String> rst = new HashMap<String, String>();
		boolean upApply = false;
		boolean spApply = false;
		Map<String, String> prams = new HashMap<String, String>();
		prams.put("serviceOrderId", serviceOrderId);
		prams.put("dealStatu", "0");
		prams.put("begin", "0");
		 GridDataInfo data = chiefDeskService.querySingleList(prams);
		if (data != null) {
			//List rows = (List) data.get("rows");
			List rows = data.getList();
			if (rows != null && (!rows.isEmpty())){
				Map tmp = null;
				for (int i = 0; i < rows.size(); i++) {
					tmp = (Map) rows.get(i);
					int type = Integer.parseInt(tmp.get("APPLY_TYPE").toString());
					if (3 == type) {// 督办申请
						spApply = true;
					} else if (4 == type) {// 升级申请
						upApply = true;
					}
				}
			}
		}
		rst.put("upApply", upApply ? "Y" : "N");
		rst.put("spApply", spApply ? "Y" : "N");
		Gson gson = new Gson();
		String jsonRes = gson.toJson(rst);
		return jsonRes;
	}

	/**
	 * 组装管控升级的下拉框选项
	 */
	@RequestMapping(value = "/workflow/ChiefOperatorDeskAction/buildLevleOption", method = RequestMethod.POST)
	public String buildLevleOption(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId) {
		String rst = "";
		String orderId = serviceOrderId;
		rst = chiefDeskService.buildLevleOption(orderId);
		return rst;
	}
}