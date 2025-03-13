package com.timesontransfar.common.authorization.model;
/**
 * @JavaBean.TsmRole 用于描述Role实体
 * @version 0.1
 * @author 罗翔 2005-12-05创建
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Date;

@SuppressWarnings("all")
public class TsmRole implements java.io.Serializable{
/*	   ROLE_ID              varchar2(32),//角色唯一ID
	   ROLE_NAME            varchar2(32),//角色名称
	   ROLE_TYPE            NUMBER(2),//角色类型
	   ROLE_ORG             varchar2(32),//组织类型
	   ORG_ID           varchar2(32),//组织ID
	   STATE                NUMBER(2),//状态
	   CRE_DATE             DATE,//创建日期
	   CRE_STAFF            varchar2(32),//创建员工
	   MODIFY_DATE          DATE,//最后修改日期
	   MODIFY_STAFF         varchar2(32)//最后修改时间
	   EXPIRE_DATE          date  //失效日期
	   EFFECT_DATE          date  //生效日期
*/
	private String id = ""; //角色唯一ID
	private String name = "";//角色名称
	private int type;//角色类型  1 可继承 0 不可继承
	private String orgId = "";//组织ID
	private String roleOrg = ""; //组织类型
	private boolean state=false;//状态(可用不可用)
	private Date createDate = new Date();//创建日期
	private String createStaff = "";//创建员工
	private Date modifyDate = new Date();//最后修改日期
	private String modifyStaff = "";//最后修改时间
	private List parentRoles=new ArrayList();//父亲角色列表
	private List childRoles=new ArrayList();
	private Date expireDate = new Date(); //失效日期
	private Date effectDate = new Date();//生效日期
	private String startDate;// 生效日期
	private String orgName;//部门名称

	private String endDate;// 失效日期

	private String roleReportGrade;//报表权限等级
	private String roleRuleType;//角色分类

	private Map dataPermit;      //数据权限
	private Map functionPermit;  //功能权限

	private String baseFlag;//是否为公用

	class DataPermit{
		protected String id;
		protected int privateFlag;
	}

	class FuncPermit{
		protected String id;
		protected int privateFlag;
	}

	/**
	 * 取得私有数据权限
	 * @return 私有数据权限ＩＤ列表
	 */
	public List getPrivateDataPermit(){
		Iterator iterator = this.dataPermit.values().iterator();
		List returnList = new ArrayList();
		while(iterator.hasNext()){
			DataPermit dp =(DataPermit)iterator.next();
			if(dp.privateFlag==1){
				returnList.add(dp.id);
			}
		}
		return returnList;

	}

	/**
	 * 取得公有数据权限
	 * @return 公有数据权限ＩＤ列表
	 */
	public List getPublicDataPermit(){
		Iterator iterator = this.dataPermit.values().iterator();
		List returnList = new ArrayList();
		while(iterator.hasNext()){
			DataPermit dp =(DataPermit)iterator.next();
			if(dp.privateFlag==0){
				returnList.add(dp.id);
			}
		}
		return returnList;

	}

	/**
	 * 取得私有数据权限
	 * @return 私有数据权限ＩＤ列表
	 */
	public List getPrivateFunctionPermit(){
		Iterator iterator = this.functionPermit.values().iterator();
		List returnList = new ArrayList();
		while(iterator.hasNext()){
			FuncPermit dp =(FuncPermit)iterator.next();
			if(dp.privateFlag==1){
				returnList.add(dp.id);
			}
		}
		return returnList;

	}

	/**
	 * 取得公有数据权限
	 * @return 公有数据权限ＩＤ列表
	 */
	public List getPublicFunctionPermit(){
		Iterator iterator = this.functionPermit.values().iterator();
		List returnList = new ArrayList();
		while(iterator.hasNext()){
			FuncPermit dp =(FuncPermit)iterator.next();
			if(dp.privateFlag==0){
				returnList.add(dp.id);
			}
		}
		return returnList;

	}

	public void addDataPermit(String id, int flag){
		DataPermit dp = new DataPermit();
		dp.id = id;
		dp.privateFlag = flag;
		this.dataPermit.put(id,dp);
	}

	public void removeDataPermit(String id){
		this.dataPermit.remove(id);
	}

	public void addFuncPermit(String id, int flag){
		FuncPermit dp = new FuncPermit();
		dp.id = id;
		dp.privateFlag = flag;
		this.functionPermit.put(id,dp);
	}

	public void removeFuncPermit(String id){
		this.functionPermit.remove(id);
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public String getCreateStaff() {
		return createStaff;
	}

	public void setCreateStaff(String createStaff) {
		this.createStaff = createStaff;
	}

	public Map getDataPermit() {
		return dataPermit;
	}

	public void setDataPermit(Map dataPermit) {
		this.dataPermit = dataPermit;
	}

	public Date getEffectDate() {
		return effectDate;
	}

	public void setEffectDate(Date effectDate) {
		this.effectDate = effectDate;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public Map getFunctionPermit() {
		return functionPermit;
	}

	public void setFunctionPermit(Map functionPermit) {
		this.functionPermit = functionPermit;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public List getParentRoles() {
		return parentRoles;
	}

	public void setParentRoles(List parentRoles) {
		this.parentRoles = parentRoles;
	}

	public void addParentRole(String roleId){
		this.parentRoles.add(roleId);
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public List getChildRoles() {
		return childRoles;
	}

	public void setChildRoles(List childRoles) {
		this.childRoles = childRoles;
	}

	public void addChildRole(String roleId){
		this.childRoles.add(roleId);
	}

	public String getRoleOrg() {
		return roleOrg;
	}

	public void setRoleOrg(String roleOrg) {
		this.roleOrg = roleOrg;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getRoleReportGrade() {
		return roleReportGrade;
	}

	public void setRoleReportGrade(String roleReportGrade) {
		this.roleReportGrade = roleReportGrade;
	}

	public String getRoleRuleType() {
		return roleRuleType;
	}

	public void setRoleRuleType(String roleRuleType) {
		this.roleRuleType = roleRuleType;
	}

	public String getBaseFlag() {
		return baseFlag;
	}

	public void setBaseFlag(String baseFlag) {
		this.baseFlag = baseFlag;
	}
}
