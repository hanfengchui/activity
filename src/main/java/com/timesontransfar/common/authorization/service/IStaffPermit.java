package com.timesontransfar.common.authorization.service;

import java.util.Date;
import java.util.Map;

import com.timesontransfar.common.authorization.model.TsmEntityPermit;
import com.timesontransfar.common.authorization.model.TsmStaff;

@SuppressWarnings("rawtypes")
public interface IStaffPermit {
	/**
	 * 返回角色是否有访问URL的权限
	 * @param url
	 * @return
	 */
	public boolean urlPermit(String url);


	/**
	 * 初始化员工限制对象
	 * @param role
	 */
	public void setStaff(TsmStaff staff);
	/**
	 * 获得用户的CSS文件；
	 * @return
	 */
	public String getCssFile();
	/**
	 * 返回所有的授权菜单
	 * @return
	 */
	public Map getMenuMap();	//系统菜单

	public Map getDataMap();	//数据权限集合

	public void setDataMap(Map dataMap);

	public String getOrgLinkID();

	public void setOrgLinkID(String orgLinkID);

	public int getOrgState();

	public void setOrgState(int orgState);

	public Map getUrlMap();		//功能权限集合

	public void setUrlMap(Map urlMap);

	public void setMenuMap(Map menuMap);

	public TsmStaff getStaff();

	public void setCssFile(String cssFile);

	public Map getPopupMenu();	//右键菜单

	public void setPopupMenu(Map popupMenu);

	public Date getLastSessionActiveTime();

	public void setLastSessionActiveTime(Date lastSessionActiveTime);

	public Map getControlMap();

	public void setControlMap(Map controlMap);

	/**
	 * 返回数据权限因子
	 * @param entityId
	 * @param operType  0：保存  1：插入   2：修改  3：删除
	 * @return
	 */
	public TsmEntityPermit getDataPermitByObjAndOper(String entityId,int operType);

	public String getAreaId();

	public void setAreaId(String areaId);

	public String getAreaName();

	public void setAreaName(String areaName);

	public String getAreaLinkId();

	public void setAreaLinkId(String areaLinkId);
}
