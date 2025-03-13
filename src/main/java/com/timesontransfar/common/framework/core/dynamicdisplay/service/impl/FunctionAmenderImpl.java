package com.timesontransfar.common.framework.core.dynamicdisplay.service.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;

import com.alibaba.fastjson.JSON;
import com.transfar.common.exception.MyOwnRuntimeException;
import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;
import com.timesontransfar.common.analyzer.ISqlAnalyzer;
import com.timesontransfar.common.cache.ICache;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.WebDisplayRowMapper;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubConstraint;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubDisplayCtrl;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubEvent;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubFunction;
import com.timesontransfar.common.framework.core.dynamicdisplay.service.IFunctionAmender;
import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class FunctionAmenderImpl implements IFunctionAmender {
	private static final Log log = LogFactory.getLog(FunctionAmenderImpl.class);
	
	@Autowired
	private RedisUtils redisUtils;

	private ISqlAnalyzer sqlAnalyzer;

	private ICache modelCache;

	private JdbcTemplate jdbcTemplate;

	private AbstractRowMapper rowMapper;

	private String displayCtrlSql;

	private String eventCtrlSql;

	private String constraintSql;

	public String getConstraintSql() {
		return constraintSql;
	}

	public void setConstraintSql(String constraintSql) {
		this.constraintSql = constraintSql;
	}

	public String getDisplayCtrlSql() {
		return displayCtrlSql;
	}

	public void setDisplayCtrlSql(String displayCtrlSql) {
		this.displayCtrlSql = displayCtrlSql;
	}

	public String getEventCtrlSql() {
		return eventCtrlSql;
	}

	public void setEventCtrlSql(String eventCtrlSql) {
		this.eventCtrlSql = eventCtrlSql;
	}

	public AbstractRowMapper getRowMapper() {
		return rowMapper;
	}

	public void setRowMapper(AbstractRowMapper rowMapper) {
		this.rowMapper = rowMapper;
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public ICache getModelCache() {
		return modelCache;
	}

	public void setModelCache(ICache modelCache) {
		this.modelCache = modelCache;
	}

	public ISqlAnalyzer getSqlAnalyzer() {
		return sqlAnalyzer;
	}

	public void setSqlAnalyzer(ISqlAnalyzer sqlAnalyzer) {
		this.sqlAnalyzer = sqlAnalyzer;
	}

	public FunctionAmenderImpl() {
		super();
	}

	public PubFunction amendFunction(PubFunction function)
			throws RuntimeException {
		try {
			function.setQuerySql(this.sqlAnalyzer.readSql(function
					.getQuerySql()));
			Map aliasMap = this.sqlAnalyzer.getAliasMap(function.getQuerySql());
			Map tableMap = this.sqlAnalyzer.getTableMap(function.getQuerySql());
			Map webIdMap = this.sqlAnalyzer.getWebId(function.getQuerySql());
			Map columnAliasMap = this.sqlAnalyzer.getColumnAliasMap(function
					.getQuerySql());
			String[] parameter = this.sqlAnalyzer.getParameter(function
					.getQuerySql());
			webIdMap = this.amendWebId(webIdMap, function);
			function.setAliasMap(aliasMap);
			function.setTableMap(tableMap);
			function.setWebIDMap(webIdMap);
			function.setColumnAliasMap(columnAliasMap);
			function.setParameter(parameter);
			return this.analyzeFunction(function);
		} catch (Exception e) {
			log.debug("PubFunction设置异常ID：" + function.getFunId());
			e.printStackTrace();
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			String message = new String(writer.getBuffer());
			throw new MyOwnRuntimeException("PubFunction设置异常：" + message);
		}
	}

	private Map amendWebId(Map webIdMap, PubFunction function) {
		if (function == null) {
			return webIdMap;
		} else {
			PubDisplayCtrl[] displayCtrl = function.getPubDisplayCtrl();
			Iterator iterator = webIdMap.keySet().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				String webId = (String) webIdMap.get(key);
				for (int i = 0; i < displayCtrl.length; i++) {
					if (displayCtrl[i].getWebId() != null && displayCtrl[i].getWebId().startsWith(webId)) {
						webIdMap.put(key, displayCtrl[i].getWebId());
						break;
					}
				}
			}
			return webIdMap;
		}
	}

	/**
	 * 锟斤拷Function锟斤拷锟斤拷锟絃ookUP锟截硷拷锟斤拷锟街碉拷WebID,锟斤拷锟睫革拷SQL锟斤拷锟�
	 *
	 * @param function
	 * @return
	 * @throws RuntimeException
	 */
	private PubFunction analyzeFunction(PubFunction function)
			throws RuntimeException {
		List displayCtrlList = new ArrayList();
		List hiddenList = new ArrayList();
		PubDisplayCtrl[] displayCtrlArray = function.getPubDisplayCtrl();
		String querySql = function.getQuerySql();
		for (int i = 0; i < displayCtrlArray.length; i++) {
			if (displayCtrlArray[i].getCtrlType() != null && ("LOOKUP".equalsIgnoreCase(displayCtrlArray[i].getCtrlType()) || "HLOOKUP".equalsIgnoreCase(displayCtrlArray[i].getCtrlType()))) {
				String funcId = displayCtrlArray[i].getContent();
				if (function.getFunType().intValue() == 1) {
					int atIndex = funcId.indexOf("@");
					if (atIndex > 0) {
						displayCtrlArray[i].setContent(funcId.substring(0,
								atIndex));
						funcId = funcId.substring(atIndex + 1);
					}
					if (funcId != null) {
						String id = "";
						String webId = "";
						if (funcId.trim().length() > 0) {
							PubFunction lookupFunction = this
									.getFunction(funcId);
							if (lookupFunction != null) {
								id = "LOOKUP_TXT" + i;
								webId = "TXT_"
										+ displayCtrlArray[i].getWebId();
								function.getWebIDMap().put(id, webId);
								querySql = this.generateLookUPSql(querySql,
										displayCtrlArray[i].getWebId(),
										function, lookupFunction, id);
							}
							PubDisplayCtrl hiddenCtrl = (PubDisplayCtrl) displayCtrlArray[i]
									.clone();
							if("LOOKUP".equalsIgnoreCase(displayCtrlArray[i].getCtrlType())){
								hiddenCtrl.setCtrlType("HIDDEN");
								hiddenList.add(hiddenCtrl);
								displayCtrlArray[i].setCtrlType("LABEL");
								displayCtrlArray[i].setWebId(webId);
							} else {
								hiddenCtrl.setCtrlType("HIDDEN");
								hiddenList.add(hiddenCtrl);
								displayCtrlArray[i].setCtrlType("HIDDEN");
								displayCtrlArray[i].setWebId(webId);
							}
						}
					}
				} else {
					if (funcId != null) {
						String id = "";
						String webId = "";
						if (funcId.trim().length() > 0) {
							PubFunction lookupFunction = this
									.getFunction(funcId);
							if (lookupFunction != null) {
								id = "LOOKUP_TXT" + i;
								webId = "TXT_"
										+ displayCtrlArray[i].getWebId();
								function.getWebIDMap().put(id, webId);
								querySql = this.generateLookUPSql(querySql,
										displayCtrlArray[i].getWebId(),
										function, lookupFunction, id);
							}
						}
					}
				}
			}
			displayCtrlList.add(displayCtrlArray[i]);
		}
		if (!hiddenList.isEmpty()) {
			displayCtrlList.addAll(hiddenList);
		}
		PubDisplayCtrl[] newCtrlArray = new PubDisplayCtrl[displayCtrlList
				.size()];
		for (int i = 0; i < newCtrlArray.length; i++) {
			PubDisplayCtrl ctrl = (PubDisplayCtrl) displayCtrlList.get(i);
			newCtrlArray[i] = ctrl;
		}
		function.setPubDisplayCtrl(null);
		function.setPubDisplayCtrl(newCtrlArray);
		function.setQuerySql(querySql);
		hiddenList.clear();
		hiddenList = null;
		displayCtrlList.clear();
		displayCtrlList = null;
		return function;
	}

	/**
	 * 锟斤拷锟斤拷Lookup锟截硷拷锟斤拷原始锟斤拷SQL锟斤拷锟斤拷锟斤拷锟接诧拷询锟斤拷锟�
	 *
	 * @param sql
	 * @param webId
	 * @param originalFunction
	 * @param lookupFunction
	 * @return
	 */
	private String generateLookUPSql(String sql, String webId,
			PubFunction originalFunction, PubFunction lookupFunction,
			String lookupAlias) throws RuntimeException {
		try {
			String originalField = this.getWhereField(webId, originalFunction
					.getTableMap(), originalFunction.getColumnAliasMap());
			if (originalField == null) {
				return sql;
			}
			String tmpId = (String) lookupFunction.getWebIDMap().get("ID");
			if (tmpId == null) {
				return sql;
			}
			String lookupField = this.getWhereField(tmpId, lookupFunction
					.getTableMap(), lookupFunction.getColumnAliasMap());
			if (lookupField == null) {
				return sql;
			}
			tmpId = (String) lookupFunction.getWebIDMap().get("NAME");
			if (tmpId == null) {
				tmpId = (String) lookupFunction.getWebIDMap().get("NAMES");
			}
			if (tmpId == null) {
				return sql;
			}
			String nameField = this.getWhereField(tmpId, lookupFunction
					.getTableMap(), lookupFunction.getColumnAliasMap());
			return this.addLookUPSql(sql, lookupFunction.getQuerySql(),
					originalField, lookupField, nameField, lookupAlias);
		} catch (Exception e) {
			log.error("generateLookUPSql error: " + e.getMessage(), e);
			StringWriter writer = new StringWriter();
			PrintWriter printWriter = new PrintWriter(writer);
			e.printStackTrace(printWriter);
			String message = new String(writer.getBuffer());
			throw new MyOwnRuntimeException("generateLookUPSql异常: " + message);
		}
	}

	/**
	 * 锟斤拷SQL锟斤拷锟斤拷锟斤拷锟接讹拷应锟侥诧拷锟斤拷
	 *
	 * @param originalSql
	 * @param lookupSql
	 * @param originalField
	 * @param lookupField
	 * @return
	 */
	private String addLookUPSql(String originalSql, String lookupSql,
			String originalField, String lookupField, String nameField,
			String lookupAlias) {
		String addSql = "(SELECT " + nameField + " ";
		String groupSql = " ";
		int fromIndex = lookupSql.indexOf(" FROM ");
		int whereIndex = lookupSql.indexOf(" WHERE ");
		int groupIndex = lookupSql.indexOf(" GROUP BY ");
		if (groupIndex > 0) {
			groupSql = lookupSql.substring(groupIndex);
		}
		if (whereIndex > 0) {
			addSql += lookupSql.substring(fromIndex, whereIndex);
			String whereSql = "";
			if (groupIndex > 0) {
				whereSql = lookupSql.substring(whereIndex, groupIndex);
			} else {
				whereSql = lookupSql.substring(whereIndex);
			}
			addSql += whereSql + " AND (" + originalField + "=" + lookupField
					+ ") AND ROWNUM=1";
			addSql += groupSql;
		} else {
			if (groupIndex > 0) {
				addSql += lookupSql.substring(fromIndex, groupIndex);
			} else {
				addSql += lookupSql.substring(fromIndex);
			}
			addSql += " WHERE (" + originalField + "=" + lookupField
					+ ") AND ROWNUM=1 ";
			addSql += groupSql;
		}
		addSql += ") AS " + lookupAlias + " ";
		String selectString = "";
		if (!originalSql.equals("")) {
			fromIndex = originalSql.lastIndexOf(" FROM ");
			selectString = originalSql.substring(0, fromIndex);
			selectString += "," + addSql;
			selectString += originalSql.substring(fromIndex);
		}
		return selectString ;
	}

	/**
	 * 锟斤拷锟斤拷WebID取锟矫匡拷锟斤拷锟斤拷为Where锟斤拷锟斤拷锟斤拷锟街讹拷
	 *
	 * @param webId
	 * @param tableMap
	 * @return
	 */
	private String getWhereField(String webId, Map tableMap, Map columnAliasMap) {
		if (webId == null) {
			return null;
		}
		int index = webId.indexOf("__");
		String originalField = "";
		if (index > 0) {
			String table = webId.substring(0, index);
			String column = webId.substring(index + 2);
			int tempIndex = column.indexOf("__");
			if (tempIndex > 0) {
				String alias = column.substring(tempIndex + 2);// 取锟斤拷锟斤拷
				column = column.substring(0, tempIndex);
				if (alias != null) {
					tempIndex = alias.indexOf("__");
					if (tempIndex > 0) {
						alias = alias.substring(0, tempIndex);
					}
				}
				if (alias != null) {// 锟斤拷锟斤拷锟斤拷锟斤拷锟轿拷眨锟斤拷锟斤拷锟叫的憋拷锟斤拷Map锟斤拷取锟斤拷谋锟斤拷锟�
					table = (String) columnAliasMap.get(alias) == null ? (String) tableMap
							.get(table)
							: (String) columnAliasMap.get(alias);
				} else {
					table = (String) tableMap.get(table);
				}
			} else {
				table = (String) tableMap.get(table);
			}
			originalField = table + "." + column;
			return originalField;
		} else {
			return null;
		}
	}
	
	private static final String PUB_FUNCTION_KEY = "PUB_FUNCTION@FUN_ID_";

	public PubFunction getFunction(String id) {
		PubFunction pubFunction = null;
		try {
			String redisData = this.redisUtils.get(PUB_FUNCTION_KEY + id, RedisType.WORKSHEET);
			pubFunction = JSON.parseObject(redisData,PubFunction.class);
		} catch (Exception e) {
			log.info("Redis读取PUB_FUNCTION异常，FUN_ID：" + id);
			e.printStackTrace();
		}
		if (pubFunction != null) {
			return pubFunction;
		}
		String functionSql = "Select * from PUB_FUNCTION WHERE FUN_ID=?";
		AbstractRowMapper webDisplayRowMapper = new WebDisplayRowMapper();
		webDisplayRowMapper.setIntClassType(0);

		List functionList = (List)jdbcTemplate.query(functionSql,new Object[] { id }, new RowMapperResultSetExtractor(webDisplayRowMapper));

		if (functionList == null || functionList.isEmpty()) {
			return null;
		} else {
			pubFunction = (PubFunction) functionList.get(0);
			pubFunction.setPubDisplayCtrl(this.getDisplayCtrl(pubFunction
					.getFunId()));
			pubFunction.setPubEvent(this.getEvent(pubFunction.getFunId()));
			pubFunction.setContraintMap(this.getConstraint(pubFunction
					.getFunId()));
			pubFunction = this.amendFunction(pubFunction);
			try {
				String pubFunctionString = JSON.toJSONString(pubFunction);
				this.redisUtils.setex(PUB_FUNCTION_KEY + id,86400,pubFunctionString,RedisType.WORKSHEET);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return pubFunction;
		}
	}

	/**
	 * @param funcId
	 * @return PubDisplayCtrl[]
	 */
	private PubDisplayCtrl[] getDisplayCtrl(String funcId) {
		AbstractRowMapper webDisplayRowMapper=new WebDisplayRowMapper();
		webDisplayRowMapper.setIntClassType(1);
		List displayCtrlList = (List)jdbcTemplate.query(this.displayCtrlSql,new Object[] { funcId }, new RowMapperResultSetExtractor(webDisplayRowMapper));
		PubDisplayCtrl[] displayCtrl = null;
		if(displayCtrlList != null && !displayCtrlList.isEmpty()) {
			displayCtrl = new PubDisplayCtrl[displayCtrlList.size()];
			for (int i = 0; i < displayCtrl.length; i++) {
				PubDisplayCtrl ctrl = (PubDisplayCtrl) displayCtrlList.get(i);
				ctrl.setChildRelaList(this.getChildRelaCtrl(ctrl.getCtrlId()));
				ctrl.setParentRelaList(this.getParentRelaCtrl(ctrl.getCtrlId()));
				displayCtrl[i] = ctrl;
			}
		}
		return displayCtrl;
	}

	/**
	 * 锟矫碉拷锟截硷拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷丶锟�
	 *
	 * @param ctrlId
	 * @return
	 */
	private List getChildRelaCtrl(String ctrlId) {
		List childList = new ArrayList();
		String querySql = "SELECT A.CTRL_ID,B.WEB_ID FROM PUB_CTRL_RELA A,PUB_DISPLAY_CTRL B "
				+ "WHERE A.CTRL_ID=B.CTRL_ID AND PARENT_CTRL_ID = ?";
		List queryList = this.jdbcTemplate.queryForList(querySql,
				ctrlId);
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			String webId = (String) queryMap.get("WEB_ID");
			String childCtrlId = (String) queryMap.get("CTRL_ID");
			childList.add(webId);
			childList.addAll(this.getChildRelaCtrl(childCtrlId));
		}
		return childList;
	}

	/**
	 * 锟矫碉拷锟截硷拷锟斤拷锟斤拷锟斤拷锟斤拷锟节控硷拷
	 *
	 * @param ctrlId
	 * @return
	 */
	private List getParentRelaCtrl(String ctrlId) {
		List parentList = new ArrayList();
		String querySql = "SELECT A.PARENT_CTRL_ID,B.WEB_ID FROM PUB_CTRL_RELA A,PUB_DISPLAY_CTRL B "
				+ "WHERE A.PARENT_CTRL_ID=B.CTRL_ID AND A.CTRL_ID = ?";
		List queryList = this.jdbcTemplate.queryForList(querySql,
				ctrlId);
		for (int i = 0; i < queryList.size(); i++) {
			Map queryMap = (Map) queryList.get(i);
			String webId = (String) queryMap.get("WEB_ID");
			String parentCtrlId = (String) queryMap.get("PARENT_CTRL_ID");
			parentList.add(webId);
			parentList.addAll(this.getParentRelaCtrl(parentCtrlId));
		}
		return parentList;
	}

	private PubEvent[] getEvent(String funcId) {
		AbstractRowMapper webDisplayRowMapper=new WebDisplayRowMapper();
		webDisplayRowMapper.setIntClassType(2);
		List eventList = (List)jdbcTemplate.query(this.eventCtrlSql,
				new Object[] { funcId }, new RowMapperResultSetExtractor(webDisplayRowMapper));
		PubEvent[] event = null;
		if(eventList != null && !eventList.isEmpty()) {
			event = new PubEvent[eventList.size()];
			for (int i = 0; i < event.length; i++) {
				event[i] = (PubEvent) eventList.get(i);
			}
		}
		return event;
	}

	private Map getConstraint(String funcId) {
		AbstractRowMapper webDisplayRowMapper=new WebDisplayRowMapper();
		webDisplayRowMapper.setIntClassType(3);
		List constraintList = (List)jdbcTemplate.query(this.constraintSql,new Object[] { funcId }, new RowMapperResultSetExtractor(webDisplayRowMapper));

		Map constraintMap = new TreeMap();
		if(constraintList != null && !constraintList.isEmpty()) {
			for (int i = 0; i < constraintList.size(); i++) {
	
				PubConstraint constraint = (PubConstraint) constraintList.get(i);
				String sTempJS = "";
				String sCtrlId = constraint.getCtrlId();
				if(constraint.getJavaClass()!=null) sTempJS = constraint.getJavaClass();
	
				if (constraintMap.containsKey(sCtrlId)) {
					Map jsMap = null;
					Map oldJSMap = (TreeMap)constraintMap.get(sCtrlId);
					oldJSMap.put(constraint.getConstType().toString(),sTempJS);
					jsMap = oldJSMap;
					constraintMap.put(sCtrlId, jsMap);
				}
				else{
					Map jsMap = new TreeMap();
					jsMap.put(constraint.getConstType().toString(),sTempJS);
					constraintMap.put(sCtrlId, jsMap);
				}
			}
		}
		return constraintMap;
	}

}
