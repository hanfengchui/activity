package com.timesontransfar.common.authorization.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @JavaBean.TsmOrgnization 用以描述组织机构
 * @version 0.1
 * @author 罗翔 2005-12-05 创建
 *
 */
@SuppressWarnings("rawtypes")
public class TsmOrganization implements java.io.Serializable{
/*   ORG_ID               varchar2(32)                    not null,//组织ID,
   LINKID               varchar2(500),//组织连接ID
   ORG_NAME             VARCHAR2(64),//组织名称
   PRINCIPAL            VARCHAR2(32),//负责人
   ORG_LEVEL            NUMBER(2),//级别
   RELAPHONE            VARCHAR2(32),//联系电话
   FUNCTIONTYPE         number(2),//功能类型
   UP_ORG               varchar2(32),
   ADDR_DESC            VARCHAR2(256),//地址描述
   STATE                number(2),//状态
   CRE_DATE             DATE,//创建时间
   CRE_STAFF            varchar2(32),//创建人
   MODIFY_DATE          DATE,//最后修改时间
   MODIFY_STAFF         varchar2(32),//最后修改人
   PARTY_ID             varchar2(32),//参与人
   constraint PK_TSM_ORGANIZATION primary key (ORG_ID)

*/
	private String id = " ";//组织ID
	private String appId = "";//组织连接ID
	private String name = "";//组织名称
	private String principal = "";//负责人
	private int level =0;//级别
	private String relaPhone = "";//联系电话
	private int funtionType = 0;//功能类型
	private String addrDesc = "";//地址描述
	private String state = "";//状态
	private Date createDate;// = new Date();//创建时间
	private String createStaff = "";//创建人
	private Date modifyDate;// = new Date();//最后修改时间
	private String modifyStaff = "";//最后修改人
	private List childOrgList= new ArrayList();//直接下属的组织机构，存储的是下属组织机构的ID
	private String parentId = "";//上级组织机构
	private String areaCode = "";//地区码
	private String regionId = "";//地域id
	private String regionName = "";//地域名称
	private int organizationType=1; //机构类别
	/*
     * ################liangyong adds on 20061204 #####################################
     */
	private String orgOwner ="";
	private String orgFax ="";
    /*
     * ##################################################################################
     */
	private String regionLinkId = "";//地域linkID

	public int getOrganizationType() {
		return organizationType;
	}
	public void setOrganizationType(int organizationType) {
		this.organizationType = organizationType;
	}

	public String getRegionId() {
		return regionId;
	}
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}
	public String getAreaCode() {
		return areaCode;
	}
	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}
	public TsmOrganization() {
		super();
	}
	public String getAddrDesc() {
		return addrDesc;
	}
	public void setAddrDesc(String addrDesc) {
		this.addrDesc = addrDesc;
	}
	public String getAppId() {
		return appId;
	}
	public void setAppId(String appId) {
		this.appId = appId;
	}
	public String getCreateStaff() {
		return createStaff;
	}
	public void setCreateStaff(String createStaff) {
		this.createStaff = createStaff;
	}
	public int getFuntionType() {
		return funtionType;
	}
	public void setFuntionType(int funtionType) {
		this.funtionType = funtionType;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	public Date getModifyDate() {
		return modifyDate;
	}
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
	}
	public String getModifyStaff() {
		return modifyStaff;
	}
	public void setModifyStaff(String modifyStaff) {
		this.modifyStaff = modifyStaff;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPrincipal() {
		return principal;
	}
	public void setPrincipal(String principal) {
		this.principal = principal;
	}
	public String getRelaPhone() {
		return relaPhone;
	}
	public void setRelaPhone(String relaPhone) {
		this.relaPhone = relaPhone;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public List getChildOrgList() {
		return childOrgList;
	}
	public void setChildOrgList(List childOrgList) {
		this.childOrgList = childOrgList;
	}
	public void addChildOrg(String orgId){
		if(this.childOrgList==null){
			this.childOrgList=new ArrayList();
		}
		this.childOrgList.add(orgId);
	}
	public String getRegionLinkId() {
		return regionLinkId;
	}
	public void setRegionLinkId(String regionLinkId) {
		this.regionLinkId = regionLinkId;
	}
	public String getOrgFax() {
		return orgFax;
	}
	public void setOrgFax(String orgFax) {
		this.orgFax = orgFax;
	}
	public String getOrgOwner() {
		return orgOwner;
	}
	public void setOrgOwner(String orgOwner) {
		this.orgOwner = orgOwner;
	}
	public String getRegionName() {
		return regionName;
	}
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}


}
