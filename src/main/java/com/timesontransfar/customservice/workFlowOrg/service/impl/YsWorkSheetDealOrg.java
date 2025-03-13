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
@Component("ysWorkSheetDealOrg")
@SuppressWarnings("rawtypes")
public class YsWorkSheetDealOrg  extends AbstractWorkSheetDealOrg {
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
		ServiceContent servContent = this.getServContentDao().getServContentByOrderId(orderAskInfo.getServOrderId(), false, 0);
		int tachId = sheetInfo.getTacheId();
		int schemaId = sheetInfo.getSheetType();
		int regionId = orderAskInfo.getRegionId();
		String itemValue = String.valueOf(servContent.getAppealReasonId());
		int sheetType = sheetInfo.getServType();
		String orgId = "0";
		//预受理 商机管理
		if(sheetType == StaticData.SERV_TYPE_YS || sheetType == StaticData.SERV_TYPE_SJ) {
			//派单环节
			if(tachId == StaticData.TACHE_ASSIGN) {
				orgId = this.getSheetFlowOrgId().getFlowOrgId(tachId, schemaId, itemValue, regionId);
			}
			if (orgId.equals("D") || orgId.equals("C") || orgId.equals("B") || orgId.equals("A")) {
				itemValue = String.valueOf(orderAskInfo.getAskChannelId());
				orgId = this.getSheetFlowOrgId().getFlowOrgId(tachId, schemaId, itemValue, regionId);
			}
		}
		Map orgMap = new HashMap();
		orgMap.put("FLOW_ORG", orgId);
		return this.setSheetOrg(sheetPubInfo, orgMap);
	}

}
