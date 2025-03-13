package com.timesontransfar.common.authorization.model;

import java.io.Serializable;
import java.util.List;

/**
 * @JavaBean.TsmMainMenu 菜单实体
 * @version 0.1
 * @author 罗翔 2005-12-05创建
 * 		   李峰 2007-10-10 修改 添加系统类型字段
 *
 */
@SuppressWarnings("rawtypes")
public class TsmMainMenu implements Serializable{
/*	   MENU_ID              varchar2(32) //唯一ID                    not null,
	   MENU_NAME            VARCHAR2(32),//菜单名称
	   URL                  VARCHAR2(512),//URL地址
	   MENU_SEQ             NUMBER(2),//显示顺序
	   FUNC_INTERFACE_ID    NUMBER(9),//功能接口ID
	   IS_MENUITEM          char(1),//是否菜单项
	   IS_TOP_MENU          NUMBER(1)                       not null,//是否为顶级菜单
	   MENU_DESC            VARCHAR2(512), //描述
	   OPENTYPE             char(1),//打开类型
	   SYSTEM_ID            NUMBER(1)//系统类型
*/
	private String id; //唯一id
	private String name;//菜单名称
	private String url;//URL地址
	private int sequence;//显示顺序
	private boolean itemFlag;//是否菜单项
	private boolean topFlag;//是否为顶级菜单
	private String openType;//打开类型
	private String menuDesc;  //菜单描述信息
	private List childList;//直接下属的菜单ID
	private List parentList;//父亲菜单ID
	private int systemId;  //系统类型 1.公有 2.服务开通 3.资源管理
	private String workTime;	//菜单工作时间的表达式
	
	public TsmMainMenu() {
		super();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public boolean isItemFlag() {
		return itemFlag;
	}
	public void setItemFlag(boolean itemFlag) {
		this.itemFlag = itemFlag;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public boolean isTopFlag() {
		return topFlag;
	}
	public void setTopFlag(boolean topFlag) {
		this.topFlag = topFlag;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List getChildList() {
		return childList;
	}
	public void setChildList(List childList) {
		this.childList = childList;
	}
	public String getOpenType() {
		return openType;
	}
	public void setOpenType(String openType) {
		this.openType = openType;
	}
	public List getParentList() {
		return parentList;
	}
	public void setParentList(List parentList) {
		this.parentList = parentList;
	}
	public String getMenuDesc() {
		return menuDesc;
	}
	public void setMenuDesc(String menuDesc) {
		this.menuDesc = menuDesc;
	}
	public int getSystemId() {
		return systemId;
	}
	public void setSystemId(int systemId) {
		this.systemId = systemId;
	}
	public String getWorkTime() {
		return workTime;
	}
	public void setWorkTime(String workTime) {
		this.workTime = workTime;
	}

}
