package com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo;

import java.util.Map;

/**
 * 定义的动态展现Function POJO；
 * PubFunction generated by hbm2java
 */
@SuppressWarnings("rawtypes")
public class PubFunction {

	private String funId;

	private String funName;

	private Integer funType;

	private String enitityId;

	private Integer queryFlag;

	private String queryBean;

	private String queryMethod;

	private String queryClass;

	private String querySql;

	private Integer addFlag;

	private String addBean;

	private String addClass;

	private String addMethod;

	private String addSql;

	private Integer modifyFlag;

	private String modifyBean;

	private String modifyClass;

	private String modifyMethod;

	private String modifySql;

	private Integer delFlag;

	private String delBean;

	private String delClass;

	private String delMethod;

	private String delSql;

	private Integer displayWidth;

	private Integer displayHeight;

	private Integer recordCount;

	private boolean autoAdapt;

	private PubDisplayCtrl[] pubDisplayCtrl;

	private PubEvent[] pubEvent;

	private Map contraintMap;

	private Integer seq;

	private Integer queryFunType;

	private boolean reuse;//是否重用

	private int configQueryFlag;//如果是DBGrid，由这个标志来判断是否需要查询方法

	private Map tableMap;

	private Map aliasMap;

	private Map webIDMap;

	private String[] parameter;

	private Map columnAliasMap;

	//添加DBGrid新属性,是否展示查询框，是否展示选择框，是否展示打印按钮
	//add by 王景周
	private boolean showSearchBtn;
	private boolean showPrintBtn;
	private Integer selectBoxType;


	public Map getColumnAliasMap() {
		return columnAliasMap;
	}

	public void setColumnAliasMap(Map columnAliasMap) {
		this.columnAliasMap = columnAliasMap;
	}

	public String[] getParameter() {
		return parameter;
	}

	public void setParameter(String[] parameter) {
		this.parameter = parameter;
	}

	public Map getAliasMap() {
		return aliasMap;
	}

	public void setAliasMap(Map aliasMap) {
		this.aliasMap = aliasMap;
	}

	public Map getTableMap() {
		return tableMap;
	}

	public void setTableMap(Map tableMap) {
		this.tableMap = tableMap;
	}

	public Map getWebIDMap() {
		return webIDMap;
	}

	public void setWebIDMap(Map webIDMap) {
		this.webIDMap = webIDMap;
	}

	/** default constructor */
	public PubFunction() {
	}

	/** constructor with id */
	public PubFunction(String funId) {
		this.funId = funId;
	}

	// Property accessors
	/**
	 *
	 */

	public String getFunId() {
		return this.funId;
	}

	public void setFunId(String funId) {
		this.funId = funId;
	}

	/**
	 *
	 */

	public String getFunName() {
		return this.funName;
	}

	public void setFunName(String funName) {
		this.funName = funName;
	}

	/**
	 *
	 */

	public Integer getFunType() {
		return this.funType;
	}

	public void setFunType(Integer funType) {
		this.funType = funType;
	}

	/**
	 *
	 */

	public String getEnitityId() {
		return this.enitityId;
	}

	public void setEnitityId(String enitityId) {
		this.enitityId = enitityId;
	}

	/**
	 *
	 */

	public Integer getQueryFlag() {
		return this.queryFlag;
	}

	public void setQueryFlag(Integer queryFlag) {
		this.queryFlag = queryFlag;
	}

	/**
	 *
	 */

	public String getQueryBean() {
		return this.queryBean;
	}

	public void setQueryBean(String queryBean) {
		this.queryBean = queryBean;
	}

	/**
	 *
	 */

	public String getQueryMethod() {
		return this.queryMethod;
	}

	public void setQueryMethod(String queryMethod) {
		this.queryMethod = queryMethod;
	}

	/**
	 *
	 */

	public String getQueryClass() {
		return this.queryClass;
	}

	public void setQueryClass(String queryClass) {
		this.queryClass = queryClass;
	}

	/**
	 *
	 */

	public String getQuerySql() {
		return this.querySql;
	}

	public void setQuerySql(String querySql) {
		this.querySql = querySql;
	}

	/**
	 *
	 */

	public Integer getAddFlag() {
		return this.addFlag;
	}

	public void setAddFlag(Integer addFlag) {
		this.addFlag = addFlag;
	}

	/**
	 *
	 */

	public String getAddBean() {
		return this.addBean;
	}

	public void setAddBean(String addBean) {
		this.addBean = addBean;
	}

	/**
	 *
	 */

	public String getAddClass() {
		return this.addClass;
	}

	public void setAddClass(String addClass) {
		this.addClass = addClass;
	}

	/**
	 *
	 */

	public String getAddMethod() {
		return this.addMethod;
	}

	public void setAddMethod(String addMethod) {
		this.addMethod = addMethod;
	}

	/**
	 *
	 */

	public String getAddSql() {
		return this.addSql;
	}

	public void setAddSql(String addSql) {
		this.addSql = addSql;
	}

	/**
	 *
	 */

	public Integer getModifyFlag() {
		return this.modifyFlag;
	}

	public void setModifyFlag(Integer modifyFlag) {
		this.modifyFlag = modifyFlag;
	}

	/**
	 *
	 */

	public String getModifyBean() {
		return this.modifyBean;
	}

	public void setModifyBean(String modifyBean) {
		this.modifyBean = modifyBean;
	}

	/**
	 *
	 */

	public String getModifyClass() {
		return this.modifyClass;
	}

	public void setModifyClass(String modifyClass) {
		this.modifyClass = modifyClass;
	}

	/**
	 *
	 */

	public String getModifyMethod() {
		return this.modifyMethod;
	}

	public void setModifyMethod(String modifyMethod) {
		this.modifyMethod = modifyMethod;
	}

	/**
	 *
	 */

	public String getModifySql() {
		return this.modifySql;
	}

	public void setModifySql(String modifySql) {
		this.modifySql = modifySql;
	}

	/**
	 *
	 */

	public Integer getDelFlag() {
		return this.delFlag;
	}

	public void setDelFlag(Integer delFlag) {
		this.delFlag = delFlag;
	}

	/**
	 *
	 */

	public String getDelBean() {
		return this.delBean;
	}

	public void setDelBean(String delBean) {
		this.delBean = delBean;
	}

	/**
	 *
	 */

	public String getDelClass() {
		return this.delClass;
	}

	public void setDelClass(String delClass) {
		this.delClass = delClass;
	}

	/**
	 *
	 */

	public String getDelMethod() {
		return this.delMethod;
	}

	public void setDelMethod(String delMethod) {
		this.delMethod = delMethod;
	}

	/**
	 *
	 */

	public String getDelSql() {
		return this.delSql;
	}

	public void setDelSql(String delSql) {
		this.delSql = delSql;
	}

	/**
	 *
	 */

	public Integer getDisplayHeight() {
		return displayHeight;
	}

	public void setDisplayHeight(Integer displayHeight) {
		this.displayHeight = displayHeight;
	}

	public Integer getDisplayWidth() {
		return displayWidth;
	}

	public void setDisplayWidth(Integer displayWidth) {
		this.displayWidth = displayWidth;
	}

	public Integer getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Integer recordCount) {
		this.recordCount = recordCount;
	}

	public Map getContraintMap() {
		return contraintMap;
	}

	public void setContraintMap(Map contraintMap) {
		this.contraintMap = contraintMap;
	}

	public PubDisplayCtrl[] getPubDisplayCtrl() {
		return pubDisplayCtrl;
	}

	public void setPubDisplayCtrl(PubDisplayCtrl[] pubDisplayCtrl) {
		this.pubDisplayCtrl = pubDisplayCtrl;
	}

	public PubEvent[] getPubEvent() {
		return pubEvent;
	}

	public void setPubEvent(PubEvent[] pubEvent) {
		this.pubEvent = pubEvent;
	}

	public boolean isAutoAdapt() {
		return autoAdapt;
	}

	public void setAutoAdapt(boolean autoAdapt) {
		this.autoAdapt = autoAdapt;
	}

	public Integer getQueryFunType() {
		return queryFunType;
	}

	public void setQueryFunType(Integer queryFunType) {
		this.queryFunType = queryFunType;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public boolean getReuse() {
		return reuse;
	}

	public void setReuse(boolean reuse) {
		this.reuse = reuse;
	}


	public Object cloneWithoutSql() {
		try{
			PubFunction function=(PubFunction)super.clone();
			function.setQuerySql(null);
			function.setTableMap(null);
			function.setWebIDMap(null);
			function.setAliasMap(null);
			function.setColumnAliasMap(null);
			return function;
		}catch (CloneNotSupportedException e) {
			return null;
		}
	}

	public int getConfigQueryFlag() {
		return configQueryFlag;
	}

	public void setConfigQueryFlag(int configQueryFlag) {
		this.configQueryFlag = configQueryFlag;
	}

	public Integer getSelectBoxType() {
		return selectBoxType;
	}

	public void setSelectBoxType(Integer selectBoxType) {
		this.selectBoxType = selectBoxType;
	}

	public boolean isShowPrintBtn() {
		return showPrintBtn;
	}

	public void setShowPrintBtn(boolean showPrintBtn) {
		this.showPrintBtn = showPrintBtn;
	}

	public boolean isShowSearchBtn() {
		return showSearchBtn;
	}

	public void setShowSearchBtn(boolean showSearchBtn) {
		this.showSearchBtn = showSearchBtn;
	}

}