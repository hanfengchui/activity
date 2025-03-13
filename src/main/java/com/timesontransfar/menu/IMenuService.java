package com.timesontransfar.menu;



public interface IMenuService {
	/**
	 * 左侧菜单
	 * @return
	 */
	String loadLeftMenu(String logonName);
	
	public String refreshMenu(String logonName);
}
