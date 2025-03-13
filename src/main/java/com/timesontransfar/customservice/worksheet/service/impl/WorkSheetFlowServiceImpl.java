package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.timesontransfar.common.workflow.IWorkFlowAttemper;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.IWorkSheetFlowService;

//@Transactional
@Component(value="workSheetFlowService")
public class WorkSheetFlowServiceImpl implements IWorkSheetFlowService {
	private static final Logger log =LoggerFactory.getLogger(WorkSheetFlowServiceImpl.class);
	
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private IWorkFlowAttemper WorkFlowAttemper;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public String submitWorkFlow(String sheetId,int quryRegion,Map otherParam){
		boolean hisFlag = true;
		String strMonth = "0";
		if(otherParam.containsKey("MONTH_FALG")){
			strMonth = otherParam.get("MONTH_FALG").toString();
		}
		Integer month = Integer.valueOf(strMonth);
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId,quryRegion,month,hisFlag);
		if(sheetPubInfo == null){
			log.warn("没有查询到工单号为 : " + sheetId + "的工单!");
			return "SHEETERROR";
		}		
		String regionId = "" + sheetPubInfo.getRegionId();
		String orderId = sheetPubInfo.getServiceOrderId();

		Map inParam = new HashMap();
		inParam.putAll(otherParam);
		inParam.put("SERV_ORDER_ID",orderId);
		inParam.put("SHEET_ID",sheetId);
		inParam.put("WF__REGION_ID",regionId);
		inParam.put("MONTH_FALG",strMonth);
		
		String wfInstId = sheetPubInfo.getWflInstId();
		String wfNodeInstId = sheetPubInfo.getTacheInstId();
		
		// 调用工作流
		WorkFlowAttemper.submitWorkFlow(wfInstId, wfNodeInstId, inParam);
		return "SUCCESS";
	}
	
	@SuppressWarnings({ "rawtypes" })
	@Override
	public void submitWorkFlow(String instanceId, String nodeInstanceId, Map inParams) {
		WorkFlowAttemper.submitWorkFlow(instanceId, nodeInstanceId, inParams);
	}
}
