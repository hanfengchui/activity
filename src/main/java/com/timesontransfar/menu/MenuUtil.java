package com.timesontransfar.menu;

import java.util.HashMap;

public class MenuUtil {
	
	private MenuUtil() {
		throw new IllegalStateException("MenuUtil class");
	}
	
	private static HashMap<String,String> iconMap = null;
	public static String getMenuIcon(String menuName) {
		if(iconMap == null) {
			iconMap = new HashMap<>();
			
			iconMap.put("系统管理", "el-icon-s-tools");
			iconMap.put("工单查询", "el-icon-search");
			iconMap.put("工单受理", "el-icon-s-check");
			iconMap.put("工单处理", "el-icon-s-order");
			iconMap.put("业务报表", "el-icon-s-data");
			iconMap.put("辅助系统", "el-icon-suitcase");
			iconMap.put("取数专用", "el-icon-wallet");
			iconMap.put("调查问卷", "el-icon-document");
			iconMap.put("标签库管理", "el-icon-tickets");
			iconMap.put("工单案例", "el-icon-s-opportunity");
		}
		
		return iconMap.get(menuName) == null ? "el-icon-kfzc" : iconMap.get(menuName);
	}
}
