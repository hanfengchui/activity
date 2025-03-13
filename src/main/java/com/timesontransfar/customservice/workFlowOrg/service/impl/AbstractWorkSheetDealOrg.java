/**
 * @author 万荣伟
 * @2010-11-13
 */
package com.timesontransfar.customservice.workFlowOrg.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.workFlowOrg.service.IsheetFlowOrg;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractWorkSheetDealOrg {

	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IserviceContentDao servContentDao;
	@Autowired
	private IsheetFlowOrg sheetFlowOrgId;
	/**
	 * 得到工单处理流向
	 * @param sheetInfo 工单对象
	 * @param orderAskInfo 定单对象
	 * @param inParam 流程对象
	 * @param sheetPubInfo 生成的工单对象
	 * @return
	 */
	public abstract SheetPubInfo getFlowOrgId(SheetPubInfo sheetInfo,OrderAskInfo orderAskInfo,Map inParam,SheetPubInfo sheetPubInfo);
	
	/**
	 * 设置工单收单部门、处理部门、收单人
	 * @param bean
	 * @param flowOrgMap
	 * @return
	 */
	public SheetPubInfo setSheetOrg(SheetPubInfo bean,Map orgMap) {
		String orgId="";
		String orgName="";
		int revStaff=0;
		String revStaffName="";
		String dealOrgId="NULLORG";
		String dealOrgName="";
		if(orgMap != null) {
			if(orgMap.containsKey("FLOW_ORG")) {
				 orgId = orgMap.get("FLOW_ORG").toString();
				 if(!"0".equals(orgId)) {
					 orgName = pubFunc.getOrgName(orgId);
					 dealOrgId="NULLORG";
				 }
			}
			if(orgMap.containsKey("STAFF_ID")) {
				String staffId = orgMap.get("STAFF_ID").toString();
				revStaff = Integer.parseInt(staffId);
				revStaffName = this.pubFunc.getStaffName(revStaff);
				orgId = this.pubFunc.getStaffOrgName(revStaff);
				orgName = pubFunc.getOrgName(orgId);
				dealOrgId=orgId;
				dealOrgName=orgName;
			}
		}

		int region = this.pubFunc.getOrgRegion(orgId);
		String regionName = this.pubFunc.getRegionName(region);
		bean.setReceiveRegionId(region);
		bean.setReceiveRegionName(regionName);
		bean.setRcvOrgId(orgId);
		bean.setRcvStaffId(revStaff);
		bean.setRcvStaffName(revStaffName);
		bean.setRcvOrgName(orgName);
		bean.setDealOrgId(dealOrgId);
		bean.setDealOrgName(dealOrgName);
		bean.setDealStaffId(revStaff);
		bean.setDealStaffName(revStaffName);
		return bean;		
	}

	/**
	 * @return pubFunc
	 */
	public PubFunc getPubFunc() {
		return pubFunc;
	}

	/**
	 * @param pubFunc 要设置的 pubFunc
	 */
	public void setPubFunc(PubFunc pubFunc) {
		this.pubFunc = pubFunc;
	}

	/**
	 * @return servContentDao
	 */
	public IserviceContentDao getServContentDao() {
		return servContentDao;
	}

	/**
	 * @param servContentDao 要设置的 servContentDao
	 */
	public void setServContentDao(IserviceContentDao servContentDao) {
		this.servContentDao = servContentDao;
	}

	/**
	 * @return sheetFlowOrgId
	 */
	public IsheetFlowOrg getSheetFlowOrgId() {
		return sheetFlowOrgId;
	}

	/**
	 * @param sheetFlowOrgId 要设置的 sheetFlowOrgId
	 */
	public void setSheetFlowOrgId(IsheetFlowOrg sheetFlowOrgId) {
		this.sheetFlowOrgId = sheetFlowOrgId;
	}

}
