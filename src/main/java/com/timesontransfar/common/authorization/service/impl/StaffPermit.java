package com.timesontransfar.common.authorization.service.impl;

/**
 * 用于封装Role的权限，保存在Cache中
 * @author 罗翔创建于2005-12-20
 */
import java.io.Serializable;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import com.timesontransfar.common.authorization.model.TsmEntityPermit;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.IStaffPermit;
@SuppressWarnings("rawtypes")
public class StaffPermit implements IStaffPermit, Serializable {

	private TsmStaff staff;

	private int orgState; // 0：查询本人数据 1：查询本组织机构数据 2：查询本组织机构及组织机构以下数据 3：不限组织机构

	private Map dataMap = new Hashtable(); // 私有数据权限

	private Map urlMap = new Hashtable(); // 能够访问的URL;

	private Map menuMap = new Hashtable(); // 系统菜单

	private Map popupMenu = new Hashtable(); // 弹出菜单

	private Map controlMap = new Hashtable(); // 页面控件

	private String orgLinkID;

	private String cssFile;

	private String areaId; // 地域ID

	private String areaName;// 地域名称

	private String areaLinkId; // 地域LINKID

	private Date lastSessionActiveTime; // 最近一次session活动时间

	public String getCssFile() {
		return cssFile;
	}

	public void setCssFile(String cssFile) {
		this.cssFile = cssFile;
	}

	public StaffPermit() {
		super();
		// Auto-generated constructor stub
	}

	public boolean urlPermit(String url) {
		// Auto-generated method stub
		if (urlMap.containsKey(url)) {
			return true;
		}
		return false;
	}

	/**
	 * 返回数据权限因子
	 *
	 * @param entityId
	 * @param operType
	 *            0：保存 1：插入 2：修改 3：删除
	 * @return
	 */
	public TsmEntityPermit getDataPermitByObjAndOper(String entityId,
			int operType) {
		TsmEntityPermit dataPermit = null;
		if (operType == 0) {
			dataPermit = (TsmEntityPermit) this.getDataMap().get(
					"SELECT__" + entityId);
		} else if (operType == 1) {
			dataPermit = (TsmEntityPermit) this.getDataMap().get(
					"INSERT__" + entityId);
		} else if (operType == 2) {
			dataPermit = (TsmEntityPermit) this.getDataMap().get(
					"UPDATE__" + entityId);
		} else if (operType == 3) {
			dataPermit = (TsmEntityPermit) this.getDataMap().get(
					"DELETE__" + entityId);
		}
		return dataPermit;
	}

	protected void finalize() throws Throwable {
		if (this.controlMap != null)
			this.controlMap.clear();
		if (this.dataMap != null)
			this.dataMap.clear();
		if (this.menuMap != null)
			this.menuMap.clear();
		if (this.popupMenu != null)
			this.popupMenu.clear();
		if (this.urlMap != null)
			this.urlMap.clear();
		super.finalize();
	}

	public Map getMenuMap() {
		return this.menuMap;
	}

	public TsmStaff getStaff() {
		return staff;
	}

	public void setStaff(TsmStaff staff) {
		this.staff = staff;
	}

	public Map getDataMap() {
		return dataMap;
	}

	public void setDataMap(Map dataMap) {
		this.dataMap = dataMap;
	}

	public String getOrgLinkID() {
		return orgLinkID;
	}

	public void setOrgLinkID(String orgLinkID) {
		this.orgLinkID = orgLinkID;
	}

	public int getOrgState() {
		return orgState;
	}

	public void setOrgState(int orgState) {
		this.orgState = orgState;
	}

	public Map getUrlMap() {
		return urlMap;
	}

	public void setUrlMap(Map urlMap) {
		this.urlMap = urlMap;
	}

	public void setMenuMap(Map menuMap) {
		this.menuMap = menuMap;
	}

	/**
	 * @return 返回 popupMenu。
	 */
	public Map getPopupMenu() {
		return popupMenu;
	}

	/**
	 * @param popupMenu
	 *            要设置的 popupMenu。
	 */
	public void setPopupMenu(Map popupMenu) {
		this.popupMenu = popupMenu;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getAreaLinkId() {
		return areaLinkId;
	}

	public void setAreaLinkId(String areaLinkId) {
		this.areaLinkId = areaLinkId;
	}

	/**
	 * @return 返回 lastSessionActiveTime。
	 */
	public Date getLastSessionActiveTime() {
		return lastSessionActiveTime;
	}

	/**
	 * @param lastSessionActiveTime
	 *            要设置的 lastSessionActiveTime。
	 */
	public void setLastSessionActiveTime(Date lastSessionActiveTime) {
		this.lastSessionActiveTime = lastSessionActiveTime;
	}

	/**
	 * @return 返回 controlMap。
	 */
	public Map getControlMap() {
		return controlMap;
	}

	/**
	 * @param controlMap
	 *            要设置的 controlMap。
	 */
	public void setControlMap(Map controlMap) {
		this.controlMap = controlMap;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}
}
