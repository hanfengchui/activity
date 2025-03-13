package com.timesontransfar.common.authorization.model;


/**
 * @JavaBean.TsmMainMenu 菜单实体
 * @version 0.1
 * //控件权限 add by chenke 2005-12-22
 *
 */
public class TsmControl implements java.io.Serializable{
/*	   MENU_ID              varchar2(32) //唯一ID                    not null,
	   MENU_NAME            VARCHAR2(32),//菜单名称
	   URL                  VARCHAR2(512),//URL地址
	   MENU_SEQ             NUMBER(2),//显示顺序
	   FUNC_INTERFACE_ID    NUMBER(9),//功能接口ID
	   IS_MENUITEM          char(1),//是否菜单项
	   IS_TOP_MENU          NUMBER(1)                       not null,//是否为顶级菜单
	   MENU_DESC            VARCHAR2(512), //描述
	   OPENTYPE             char(1),//打开类型
*/
	private String id; //唯一ID;
	private String name;//菜单名称
	private String url;//URL地址
	private String ctrlType;//打开类型

	public TsmControl() {
		super();
		// Auto-generated constructor stub
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return 返回 ctrlType。
	 */
	public String getCtrlType() {
		return ctrlType;
	}
	/**
	 * @param ctrlType 要设置的 ctrlType。
	 */
	public void setCtrlType(String ctrlType) {
		this.ctrlType = ctrlType;
	}
}
