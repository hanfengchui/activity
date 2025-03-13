/**
 * @author 万荣伟
 * @2010-11-13
 */
package com.timesontransfar.customservice.workFlowOrg.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;

/**
 * @author 万荣伟
 *
 */
@Component("ynWorkSheetDealOrg")
@SuppressWarnings("rawtypes")
public class YnWorkSheetDealOrg extends AbstractWorkSheetDealOrg {
	/**
	 * 得到工单处理流向
	 * @param sheetInfo 工单对象
	 * @param orderAskInfo 定单对象
	 * @param inParam 流程对象
	 * @param sheetPubInfo 生成的工单对象
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SheetPubInfo getFlowOrgId(SheetPubInfo sheetInfo, OrderAskInfo orderAskInfo, Map inParam, SheetPubInfo sheetPubInfo) {
		int tachId = sheetInfo.getTacheId();
		int schemaId = sheetInfo.getSheetType();
		int regionId = orderAskInfo.getRegionId();
		String itemValue = String.valueOf(orderAskInfo.getAskChannelId());
		String orgId = "0";		
		//派单环节
		if(tachId ==StaticData.TACHE_ASSIGN) {
			String askOrgId = orderAskInfo.getAskOrgId();
			regionId = this.getPubFunc().getOrgRegion(askOrgId);
			orgId = this.getSheetFlowOrgId().getFlowOrgId(tachId, schemaId, itemValue,regionId);
		}
		//部门环节
		if(tachId ==StaticData.TACHE_DEAL) {
			ServiceContent servContent = this.getServContentDao().getServContentByOrderId(orderAskInfo.getServOrderId(), false,0);
			//如果为疑难号百，更改匹配值
			if(servContent.getAppealProdId() == StaticData.APPEAL_PROD_ID_YN_HB) {
				itemValue = String.valueOf(StaticData.APPEAL_PROD_ID_YN_HB);
			}
			orgId=this.getSheetFlowOrgId().getFlowOrgId(tachId, schemaId, itemValue,regionId);
		}			
		//审核环节
		if(tachId ==StaticData.TACHE_AUIT) {
			String askOrgId = orderAskInfo.getAskOrgId();
			regionId = this.getPubFunc().getOrgRegion(askOrgId);
			orgId=this.getSheetFlowOrgId().getFlowOrgId(tachId, schemaId, itemValue,regionId);
		}
		Map orgMap = new HashMap();
		orgMap.put("FLOW_ORG", orgId);
		return this.setSheetOrg(sheetPubInfo, orgMap);		
	}

}
