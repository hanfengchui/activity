package com.timesontransfar.satisfy.common;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.timesontransfar.common.util.DateUtil;
import com.timesontransfar.evaluation.PerceptionInfo;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CommonUtil {
	
	private CommonUtil() {
		
	}
	
	private static Map regions = new HashMap();
	static {
		regions.put("3", "南京市");
		regions.put("4", "镇江");
		regions.put("15", "无锡");
		regions.put("20", "苏州");
		regions.put("26", "南通");
		regions.put("33", "扬州");
		regions.put("39", "盐城");
		regions.put("48", "徐州");
		regions.put("60", "淮安");
		regions.put("63", "连云港");
		regions.put("69", "常州");
		regions.put("79", "泰州");
		regions.put("84", "宿迁");
	}
	
	public static String getRegionName(String regionId) {
		return regions.get(regionId).toString();
	}
	
	public static String checkPerceptionInfo(PerceptionInfo info) {
		if(!regions.containsKey(String.valueOf(info.getRegionId()))) {
			return "regionId地域ID传值错误";
		}
		if(!DateUtil.isDateString(info.getAcceptDate()) || StringUtils.length(info.getAcceptDate()) != 19) {
			return "acceptDate投诉受理时间传值错误";
		}
		if(StringUtils.length(info.getAreaName()) > 50) {
			return "areaName区县长度不能超过50";
		}
		if(StringUtils.length(info.getSubStationName()) > 50) {
			return "subStationName支局长度不能超过50";
		}
		if(StringUtils.isBlank(info.getRelayMan())) {
			return "relayMan联系人不能为空";
		}
		if(StringUtils.length(info.getRelayMan()) > 50) {
			return "relayMan联系人长度不能超过50";
		}
		if(StringUtils.isBlank(info.getRelayInfo())) {
			return "relayInfo联系电话不能为空";
		}
		if(StringUtils.length(info.getRelayInfo()) > 25) {
			return "relayInfo联系电话长度不能超过25";
		}
		if(StringUtils.isBlank(info.getProdNum())) {
			return "prodNum产品号码不能为空";
		}
		if(StringUtils.length(info.getProdNum()) > 25) {
			return "prodNum产品号码长度不能超过25";
		}
		if(StringUtils.length(info.getSourceNum()) > 25) {
			return "sourceNum来电号码长度不能超过25";
		}
		if(StringUtils.length(info.getCrmCustId()) > 12) {
			return "crmCustId长度不能超过12";
		}
		if(!Arrays.asList(0, 1, 2).contains(info.getIsSolve())) {
			return "isSolve是否已解决传值不规范";
		}
		if(StringUtils.length(info.getSatisfiedReason()) > 255) {
			return "satisfiedReason不满意原因长度不能超过255";
		}
		return null;
	}
	

}
