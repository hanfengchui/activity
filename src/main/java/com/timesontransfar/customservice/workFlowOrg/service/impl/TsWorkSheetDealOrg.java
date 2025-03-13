/**
 * 根据配置的工单流向信息，获取工单的流向部门信息
 * @author LiJiahui
 */
package com.timesontransfar.customservice.workFlowOrg.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.sheetHandler.CompatHandler;

/**
 * @author LiJiahui
 * 
 */
@Component("tsWorkSheetDealOrg")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class TsWorkSheetDealOrg extends AbstractWorkSheetDealOrg {
	@Autowired
	private PubFunc pubFunc;
    /**
     * 得到工单处理流向
     * 
     * @param sheetInfo
     *            工单对象
     * @param orderAskInfo
     *            定单对象
     * @param inParam
     *            流程对象
     * @param sheetPubInfo
     *            生成的工单对象
     * @return
     */
	public SheetPubInfo getFlowOrgId(SheetPubInfo sheetInfo, OrderAskInfo orderAskInfo, Map inParam, SheetPubInfo sheetPubInfo) {
		if(StaticData.TACHE_FINISH_NEW == sheetInfo.getTacheId()) {
			return this.getFlowOrgEnd(sheetPubInfo);
		}
		
		int askChannel = orderAskInfo.getAskChannelId(); // 受理渠道ID
		int servType = sheetInfo.getServType(); // 服务性质类别
		// 如果不是越级单 并且 （不是投诉单或者不是中心渠道受理），则执行按目录派发
		if (3 == orderAskInfo.getServiceDate() && isCatalogFlow(askChannel, servType)) {
			int tacheId = sheetInfo.getTacheId(); // 环节ID
			int sheetType = sheetInfo.getSheetType(); // 工单类型
			int numRegion = orderAskInfo.getRegionId(); // 产品号码所属地域
			ServiceContent servContent = getServContentDao().getServContentByOrderId(orderAskInfo.getServOrderId(), false, 0);
			String orgId = getSheetFlowOrgId().getFlowOrgId(tacheId, sheetType, String.valueOf(pubFunc.getLastXX(servContent)), numRegion, servType);
			// 新流向使用最末级现象作为匹配
			if (!isEmptyOrg(orgId)) {
				Map orgMap = new HashMap();
				orgMap.put("FLOW_ORG", orgId);
				return setSheetOrg(sheetPubInfo, orgMap);
			}
		}
		return getFlowOrgQuDao(sheetInfo, orderAskInfo, sheetPubInfo);
	}
	
	private SheetPubInfo getFlowOrgEnd(SheetPubInfo bean) {
		int region = 0;
		String regionName = "";
		String orgId = "";
		String orgName = "";
		int revStaff = 0;
		String revStaffName = "";
		String dealOrgId = "NULLORG";
		String dealOrgName = "";
		bean.setReceiveRegionId(region);
		bean.setReceiveRegionName(regionName);
		bean.setRcvOrgId(orgId);
		bean.setRcvOrgName(orgName);
		bean.setRcvStaffId(revStaff);
		bean.setRcvStaffName(revStaffName);
		bean.setDealOrgId(dealOrgId);
		bean.setDealOrgName(dealOrgName);
		bean.setDealStaffId(revStaff);
		bean.setDealStaffName(revStaffName);
		return bean;
	}

	private SheetPubInfo getFlowOrgQuDao(SheetPubInfo sheetInfo, OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo) {
		int servType = sheetInfo.getServType(); // 服务性质类别
		int tacheId = sheetInfo.getTacheId(); // 环节ID
		int sheetType = sheetInfo.getSheetType(); // 工单类型
		String askOrgId = orderAskInfo.getAskOrgId(); // 受理部门
		int regionId = pubFunc.getUpRegionId(pubFunc.getOrgRegion(askOrgId)); // 受理部门所属地域
		//省集中10000号、政风热线、QQ客服、网媒、省服务监督热线
		if(orderAskInfo.getAskChannelId() == 707907007 
				|| orderAskInfo.getAskChannelId() == 707907035 
				|| orderAskInfo.getAskChannelId() == 707907014 
				|| orderAskInfo.getAskChannelId() == 707907015 
				|| orderAskInfo.getAskChannelId() == 707907012) {
			regionId = orderAskInfo.getRegionId();
		}
		int askSource = orderAskInfo.getAskSource(); // 受理来源
		int askChannelId = orderAskInfo.getAskChannelId(); // 受理渠道ID
		String askChannel = String.valueOf(askChannelId); // 匹配项
		int channelDetailId = orderAskInfo.getChannelDetailId();
		String channelDetail = String.valueOf(channelDetailId);
		String orgId = "0";

		/* 以下按照渠道派发 */
		switch (tacheId) {
		case StaticData.TACHE_ASSIGN: // 后台派单
		case StaticData.TACHE_ASSIGN_NEW:
		case StaticData.TACHE_AUIT: // 后台审核
		case StaticData.TACHE_TSQUALITATIVE: // 后台定性
		case StaticData.TACHE_ZHONG_DINGXING_NEW:
		case StaticData.TACHE_TSASSESS: // 后台考核
		case StaticData.TACHE_PIGEONHOLE: // 工单归档
			if (isWT(askSource, askChannelId)) {
				regionId = orderAskInfo.getRegionId();
				regionId = pubFunc.getUpRegionId(regionId);
			} else if (StaticData.ACPT_ORGID_JT.equals(askOrgId)) {
				regionId = orderAskInfo.getRegionId();
			}
			if (channelDetailId != 0) {
				orgId = getSheetFlowOrgId().getFlowOrgId(tacheId, sheetType, channelDetail, regionId, servType);
			}
			if (isEmptyOrg(orgId)) {
				orgId = getSheetFlowOrgId().getFlowOrgId(tacheId, sheetType, askChannel, regionId, servType);
			}
			break;
		case StaticData.TACHE_DEAL: // 部门处理
			orgId = getSheetFlowOrgId().getFlowOrgId(tacheId, sheetType, askChannel, regionId, servType);
			break;
		default:
		}
		Map orgMap = new HashMap();
		orgMap.put("FLOW_ORG", orgId);
		return setSheetOrg(sheetPubInfo, orgMap);
	}

	private boolean isEmptyOrg(String orgId) {
		return "0".equals(orgId) || "D".equals(orgId) || "C".equals(orgId) || "B".equals(orgId) || "A".equals(orgId);
	}

	private boolean isCatalogFlow(int askChannel, int servType) {
		boolean boo = pubFunc.isYueJi(askChannel) || (CompatHandler.isServTypeComplaint(servType) && isCenter(askChannel));
		boo = !boo;
		return boo;
	}

    /**
     * 根据受理来源、受理渠道信息，判断是否是从网厅掌厅受理的工单 为了兼容升级前后的单子，所以要传入受理来源、受理渠道信息，都进行匹配判断
     * 
     * @author LiJiahui
     * @date 2012-2-14
     * @param source
     *            2012年01月12日升级前的单子，根据受理来源判断
     * @param channel
     *            升级后的单子，根据受理渠道判断
     * @return true 是；false 否
     */
	private boolean isWT(int source, int channel) {
		if (source == StaticData.ACCEPT_COME_FROM_WT) {
			return true;
		}
		switch (channel) {
		case StaticData.NEW_ACCEPT_CHANNEL_ID_WT:
		case StaticData.NEW_ACCEPT_CHANNEL_ID_ZT:
		case StaticData.NEW_ACCEPT_CHANNEL_ID_SNWB:
		case StaticData.NEW_ACCEPT_CHANNEL_ID_QQKF:
			return true;
		default:
			return false;
		}
	}

    /**
     * 判断渠道是否属于“中心受理”的渠道之一
     * 
     * @author LiJiahui
     * @date 2012-5-28
     * @param channel
     *            受理渠道ID
     * @return true 是；false 否
     */
	private boolean isCenter(int channel) {
		int len = StaticData.getNewCenterChannelId().length;
		for (int i = 0; i < len; i++) {
			if (StaticData.getNewCenterChannelId()[i] == channel) {
				return true;
			}
		}
		return false;
	}
}