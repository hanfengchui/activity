package com.timesontransfar.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.worksheet.pojo.SheetReadRecordInfo;
import com.timesontransfar.customservice.worksheet.service.ItsSheetDealAction;

@RestController
@SuppressWarnings("rawtypes")
public class TsSheetDealActionController {
	protected Logger log = LoggerFactory.getLogger(TsSheetDealActionController.class);
	
	@Autowired
	private PubFunc pubFunc;
	
	@Autowired
	private ItsSheetDealAction tsSheetDealService;

	
	@PostMapping(value = "/workflow/tsSheetDealAction/getTsdealFlowCount")
	public int getTsdealFlowCount(
			@RequestParam(value="orderId", required=true)String orderId, 
			@RequestParam(value="regionId", required=true)int regionId, 
			@RequestParam(value="boo", required=true)boolean boo) {
		return tsSheetDealService.getTsdealFlowCount(orderId,regionId,boo);
	}
	
	@PostMapping(value = "/workflow/tsSheetDealAction/getTsdealFlowObj_new")
	public List getTsdealFlowObjNew(
			@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="regionId", required=true)int regionId,
			@RequestParam(value="boo", required=true)boolean boo) {
		return tsSheetDealService.getTsdealFlowObjNew(orderId,regionId,boo);
	}

	@PostMapping(value = "/workflow/tsSheetDealAction/allotBatchSheet")
	public String allotBatchSheet(
			@RequestParam(value="sheetId", required=true)String[] sheetId,
			@RequestParam(value="staffId", required=true)int staffId,
			@RequestParam(value="type", required=true)int type) {
		return tsSheetDealService.allotBatchSheet(sheetId,staffId,type);
	}
	
	@PostMapping(value = "/workflow/tsSheetDealAction/allotBatchSheetStaff")
	public String allotBatchSheetStaff(@RequestParam(value="sheetIds", required=true)String[] sheetIds,@RequestParam(value="staffId", required=true)String[] staffId) {
		return tsSheetDealService.allotBatchSheet(sheetIds,staffId);
	}
	
	@PostMapping(value = "/workflow/tsSheetDealAction/getRecordFile")
	public String getRecordFile(
			@RequestParam(value="orderId", required=true)String orderId,
			@RequestParam(value="region", required=true)int region,
			@RequestParam(value="boo", required=true)boolean boo,
			@RequestParam(value="fileType", required=true)int fileType) {
		return tsSheetDealService.getRecordFile(orderId,region,boo,fileType);
	}
	
	@PostMapping(value = "/workflow/tsSheetDealAction/getVoiceFile")
	public String getVoiceFile(@RequestParam(value="flowId", required=true)String flowId) {
		return tsSheetDealService.getVoiceFile(flowId);
	}
	
	@PostMapping(value = "/workflow/tsSheetDealAction/getRelatingSheet")
	public List getRelatingSheet(@RequestParam(value="sheetId", required=true)String sheetId,@RequestParam(value="regionId", required=true)int regionId) {
		return tsSheetDealService.getRelatingSheet(sheetId,regionId);
	}
	
	@PostMapping(value = "/workflow/tsSheetDealAction/getSheetStatuAud")
	public List getSheetStatuAud(@RequestParam(value="sheetId", required=true)String sheetId) {
		return tsSheetDealService.getSheetStatuAud(sheetId);
	}
	
	@PostMapping(value = "/workflow/tsSheetDealAction/saveSheetRead")
	public String saveSheetRead(
			@RequestParam(value="sheetId", required=true)String sheetId,
			@RequestParam(value="readDate", required=true)String readDate ) {
		SheetReadRecordInfo bean = new SheetReadRecordInfo();
		bean.setSheetId(sheetId);
		bean.setReadDate(Integer.parseInt(readDate));
		//取当前登录员工信息
		TsmStaff staff = this.pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		bean.setReadStaffId(staffId);
		bean.setReadStaffName(staffName);
		return tsSheetDealService.saveSheetRead(bean);
	}

	@PostMapping(value = "/workflow/tsSheetDealAction/forceDistillList")
	public String forceDistillList(@RequestParam(value = "staffId", required = true) int staffId,
			@RequestParam(value = "orders", required = true) String[] orders) {
		return tsSheetDealService.forceDistillList(staffId, orders);
	}

	/**
	 * 强制提取一跟到底重复单监控箱的工单
	 * */
	@PostMapping(value = "/workflow/tsSheetDealAction/flowToEndForceDistill")
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String flowToEndForceDistill(@RequestParam(value = "staffId", required = true) int staffId,
									 @RequestParam(value = "orders", required = true) String[] orders) {
		return tsSheetDealService.flowToEndForceDistill(staffId, orders);
	}

	@PostMapping(value = "/workflow/tsSheetDealAction/checkFlowToEndConfigOrg")
	public int checkFlowToEndConfigOrg() {
		return tsSheetDealService.checkFlowToEndConfigOrg();
	}

	@PostMapping(value = "/workflow/tsSheetDealAction/getFlowToEndCountByOrderId")
	public int getFlowToEndCountByOrderId(@RequestParam(value = "orderId", required = true) String orderId) {
		return tsSheetDealService.getFlowToEndCountByOrderId(orderId);
	}

	@PostMapping(value = "/workflow/tsSheetDealAction/getFlowToEndListByOrderId")
	public List getFlowToEndListByOrderId(@RequestParam(value = "orderId", required = true) String orderId) {
		return tsSheetDealService.getFlowToEndListByOrderId(orderId);
	}
}