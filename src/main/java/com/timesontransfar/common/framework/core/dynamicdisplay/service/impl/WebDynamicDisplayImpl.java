package com.timesontransfar.common.framework.core.dynamicdisplay.service.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import com.timesontransfar.common.analyzer.ISqlAnalyzer;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.common.database.ISqlUtil;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubDisplayCtrl;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubEvent;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubFunction;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubQueryData;
import com.timesontransfar.common.framework.core.dynamicdisplay.service.IFunctionAmender;
import com.timesontransfar.common.framework.core.dynamicdisplay.service.IWebDynamicDisplay;
import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;
import com.timesontransfar.customservice.common.PubFunc;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class WebDynamicDisplayImpl implements ApplicationContextAware, IWebDynamicDisplay {
	
	protected Logger log = LoggerFactory.getLogger(WebDynamicDisplayImpl.class);

	private ApplicationContext applicationContext;

	private JdbcTemplate jdbcTemplate;

	private IFunctionAmender functionAmender;

	private AbstractRowMapper rowMapper;// Define rowMapper implementing

	private ISqlUtil sqlUtil;

	private ISqlAnalyzer sqlAnalyzer;

	private List queryList = new ArrayList();

	private int batchCount;

	private ISystemAuthorization systemAuthorization;
	
	@Autowired
	private PubFunc pubFunc;

	private List blackParameterList;

	private GenerateMetaMapRowMapper generateMetaMapRowMapper = new GenerateMetaMapRowMapper();
	
	public List getBlackParameterList() {
		return blackParameterList;
	}

	public void setBlackParameterList(List blackParameterList) {
		this.blackParameterList = blackParameterList;
	}

	/**
	 * 
	 */
	public void setApplicationContext(ApplicationContext arg0)
			throws BeansException {
		this.applicationContext = arg0;
	}

	/**
	 * 
	 */
	public PubFunction getFunction(String id) {
		return this.functionAmender.getFunction(id);
	}

	/**
	 * @param funcId
	 * @return PubDisplayCtrl[]
	 */
	public PubDisplayCtrl[] getDisplayCtrl(String funcId) {
		return this.functionAmender.getFunction(funcId).getPubDisplayCtrl();
	}

	public PubEvent[] getEvent(String funcId) {
		return this.functionAmender.getFunction(funcId).getPubEvent();
	}

	/**
	 * 
	 */
	public PubQueryData getData(String funcId, Map inParams) {
		String position = (String) inParams.get("WEB_DYNAMICDISPLAY_POSITION");// WEB_DYNAMICDISPLAY_POSITION
		
		int begin = 0;
		int pageSize = this.batchCount;
		if(com.transfar.common.utils.StringUtils.isNotNull(inParams.get("pageSize"))){
			pageSize = Integer.parseInt(inParams.get("pageSize").toString());
		}
		int limit = pageSize;
		if (position != null) {
			int intPos = Integer.parseInt(position);
			String operation = (String) inParams.get("WEB_DYNAMICDISPLAY_OPERATION");
			if (operation.equalsIgnoreCase("PREV")) {
				begin = (intPos) * pageSize;
			}
		}
		log.info("begin: "+begin + "   limit: " + limit + "  position：" + position);
		return this.getNewData(funcId, inParams, begin, limit);
	}
	
	/**
	 * 
	 * @param funcId
	 * @param inParams
	 * @return
	 */
	private PubQueryData getNewData(String funcId, Map inParams, int begin,
			int limit) {
		PubFunction function = this.getFunction(funcId);
		switch (function.getQueryFlag().intValue()) {
		case 0:
			return this.getNewDataBySql(inParams, begin, limit, function);
		case 1:
			return this.getNewDataByMethod(inParams, function);
		default:
			PubQueryData queryData = new PubQueryData();
			queryData.setBegin(0);
			queryData.setEnd(0);
			queryData.setKeyMap(new TreeMap());
			queryData.setTotalCount(0);
			queryData.setResultData(new ArrayList());
			return queryData;
		}
	}

	/**
	 * 
	 * @param inParams
	 * @return
	 */
	private PubQueryData getNewDataBySql(Map inParams, int begin, int limit,
			PubFunction function) {
		String querySql = function.getQuerySql();
		if (querySql == null) {
			return null;
		}
		if (querySql.trim().length() < 6) {
			return null;
		}
		
		try {
			if (inParams.containsKey("WEB_ADDITIONALCONDITION")) {
				String addtion = (String) inParams.get("WEB_ADDITIONALCONDITION");
				
				querySql = this.sqlAnalyzer.addCondition(function.getTableMap(), querySql, addtion);
				inParams.remove("WEB_ADDITIONALCONDITION");
			}
			String[] parameter = function.getParameter();
			inParams = this.voteParams(inParams, function, parameter);
			Object[] value = new Object[parameter.length];
			for (int i = 0; i < parameter.length; i++) {
				String webParam = "WEB__" + parameter[i];
				if (inParams.containsKey(webParam)) {
					value[i] = inParams.get(webParam);
				} else {
					Set keySet = inParams.keySet();
					Iterator iterator = keySet.iterator();
					boolean match = false;
					while (iterator.hasNext()) {
						String param = ((String) iterator.next()).toUpperCase()
								.trim();
						if (this.isLike(param, "WEB__" + parameter[i])) {
							value[i] = inParams.get(param);
							match = true;
							break;
						} else {
							String inParam = param;
							int underIndex = param.indexOf("WEB__");
							if (underIndex >= 0) {
								param = param.substring(underIndex + 5);
								underIndex = param.indexOf("__");
								if (underIndex >= 0) {
									param = param.substring(underIndex + 2);
									underIndex = param.indexOf("__");
									if (underIndex >= 0) {
										param = param.substring(0, underIndex);
									}
								}
							}
							if (param.equals(parameter[i])) {
								value[i] = inParams.get(inParam);
								match = true;
							}
						}
					}

					if (!match) {
						return null;
					}
				}
			}
			if (function.isAutoAdapt()) {
				querySql = this.autoAdaptParameter(inParams, querySql, function);
			}
			querySql = this.systemAuthorization.getAuthedSql(function.getTableMap(), querySql, function.getEnitityId());

			return this.getResult(querySql, value, begin, limit, function);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private Map voteParams(Map inParams, PubFunction function,
			String[] parameter) {
		Map actualParams = new TreeMap();
		String webRender = (String) inParams.get("WEB_RENDER");
		inParams = this.deleteInvalidParameter(inParams, webRender, function,
				parameter);
		if ((webRender != null) && (webRender.trim().length() > 1)) {
			PubFunction render = this.getFunction(webRender);
			if (render != null) {
				PubDisplayCtrl[] controls = render.getPubDisplayCtrl();
				for (int i = 0; i < controls.length; i++) {
					String id = "WEB__" + controls[i].getWebId();
					if (inParams.containsKey(id)) {
						actualParams.put(id, inParams.get(id));
						inParams.remove(id);
					}
				}
			}
			inParams.remove("WEB_RENDER");

			return actualParams;
		}
		Map allParams = new TreeMap(inParams);
		Iterator actualInterator = actualParams.keySet().iterator();
		while (actualInterator.hasNext()) {
			String webId1 = (String) actualInterator.next();
			Iterator allInterator = allParams.keySet().iterator();
			while (allInterator.hasNext()) {
				String webId2 = (String) allInterator.next();
				if (this.isLike(webId1, webId2)) {
					inParams.remove(webId2);
				}
			}
		}
		actualInterator = inParams.keySet().iterator();
		while (actualInterator.hasNext()) {
			String key = (String) actualInterator.next();
			actualParams.put(key, inParams.get(key));
		}
		allParams.clear();
		allParams = null;
		inParams.clear();
		inParams = null;
		return actualParams;
	}

	private boolean isLike(String webId1, String webId2) {
		String id1 = webId1.substring(5);
		String id2 = webId2.substring(5);
		String tableColumn1 = "WEB__";
		String tableColumn2 = "WEB__";
		int index1 = id1.indexOf("__");
		if (index1 >= 0) {
			tableColumn1 += id1.substring(0, index1) + "__";
			id1 = id1.substring(index1);
		}
		int index2 = id2.indexOf("__");
		if (index2 >= 0) {
			tableColumn2 += id2.substring(0, index2) + "__";
			id2 = id2.substring(index2);
		}
		index1 = id1.indexOf("__");
		if (index1 >= 0) {
			id1 = id1.substring(index1 + 2);
			index1 = id1.indexOf("__");
			if (index1 > 0) {
				id1 = id1.substring(0, index1);
			}
			tableColumn1 += id1;
		}
		index2 = id2.indexOf("__");
		if (index2 >= 0) {
			id2 = id2.substring(index2 + 2);
			index2 = id2.indexOf("__");
			if (index2 > 0) {
				id2 = id2.substring(0, index2);
			}
			tableColumn2 += id2;
		}
		if (tableColumn1.equals(tableColumn2)) {
			return true;
		}
		return false;
	}

	private Map deleteInvalidParameter(Map inParams, String webRender,
			PubFunction function, String[] parameter) {
		inParams = this.deleteSelfParameter(inParams, webRender, function);
		Map params = new TreeMap(inParams);
		List parameterList = new ArrayList();
		for (int i = 0; i < parameter.length; i++) {
			parameterList.add(parameter[i]);
		}
		Iterator iterator = inParams.keySet().iterator();
		while (iterator.hasNext()) {
			String webId = (String) iterator.next();
			boolean removed = false;
			if (!webId.startsWith("WEB__")) {
				params.remove(webId);
				removed = true;
			}
			if (!removed) {
				for (int i = 0; i < this.blackParameterList.size(); i++) {
					String blackParameter = (String) this.blackParameterList
							.get(i);
					if (!parameterList.contains(blackParameter)) {
						if (webId.startsWith((String) this.blackParameterList
								.get(i))) {
							params.remove(webId);
							removed = true;
						}
					}
				}
			}
			if (!removed) {
				Object value = inParams.get(webId);
				if (value == null) {
					params.remove(webId);
					removed = true;
				} else {
					String valueX = (String) value;
					if (valueX.trim().length() < 1)
						params.remove(webId);
					removed = true;
				}
			}
		}
		parameterList.clear();
		parameterList = null;
		return params;
	}

	private Map deleteSelfParameter(Map inParams, String webRender,
			PubFunction function) {
		if (webRender == null || function.getFunId().equals(webRender)) {
			return inParams;
		} else {
			PubDisplayCtrl[] pubDisplayCtrlArray = function.getPubDisplayCtrl();
			for (int i = 0; i < pubDisplayCtrlArray.length; i++) {
				if (inParams.containsKey("WEB__"
						+ pubDisplayCtrlArray[i].getWebId())) {
					inParams
							.remove("WEB__" + pubDisplayCtrlArray[i].getWebId());
				}
			}
			return inParams;
		}

	}

	private String autoAdaptParameter(Map inParams, String querySql, PubFunction function) {
		Map params = new TreeMap(inParams);
		String sql = querySql;
		
		sql = this.sqlAnalyzer.addCondition(function.getTableMap(), function
				.getColumnAliasMap(), querySql, params);
		return sql;
	}
	
	private PubQueryData getResult(String querySql, Object[] value, int begin, int limit, PubFunction function) {
		String logonName = pubFunc.getLogonStaff().getLogonName();
		
		PubQueryData resultData = new PubQueryData();
		if (function.getFunType().intValue() != 0) {
			String countSql = this.sqlAnalyzer.generateCountSql(querySql);
			log.info("logonName:{} funId:{} enitityId:{} count:\n{}", logonName, function.getFunId(), function.getEnitityId(), countSql);
			int resultCount = this.jdbcTemplate.queryForObject(countSql, value,Integer.class);//CodeSec未验证的SQL注入；CodeSec误报：4
			resultData.setTotalCount(resultCount);
			resultData.setBegin(begin + 1);
			resultData.setEnd(begin + limit);
			
			if(resultCount > limit) {
				querySql = this.sqlAnalyzer.generateBetweenSql(querySql, begin, limit);
			}
		}
		
		log.info("logonName:{} funId:{} enitityId:{} getResult:\n{}", logonName, function.getFunId(), function.getEnitityId(), querySql);
		List dataList = jdbcTemplate.queryForList(querySql, value);//CodeSec未验证的SQL注入；CodeSec误报：4
		this.queryList.clear();
		boolean firstRecord = true;
		Map keyMap = new TreeMap();
		int keySequence = 0;
		Map webIdMap = function.getWebIDMap();
		for (int k = 0; k < dataList.size(); k++) {
			Map rowMap = (Map) dataList.get(k);
			Set labelSet = rowMap.keySet();
			Iterator iterator = labelSet.iterator();
			if (firstRecord) {
				while (iterator.hasNext()) {
					String label = (String) iterator.next();
					String webId = (String) webIdMap.get(label);
					if (webId != null) {
						keyMap.put(webId, keySequence);
						keySequence++;
					}
				}
				firstRecord = false;
			}

			List resultList = new ArrayList();
			iterator = labelSet.iterator();
			while (iterator.hasNext()) {
				String label = (String) iterator.next();				
				resultList.add(rowMap.get(label));
			}
			this.queryList.add(resultList);
		}
		dataList.clear();
		resultData.setKeyMap(keyMap);
		resultData.setResultData(this.queryList);
		return resultData;
	}

	private PubQueryData getNewDataByMethod(Map inParams, PubFunction function) {
		try {
			Object instanceObject = this.applicationContext.getBean(function
					.getQueryBean());
			Method[] methodArray = instanceObject.getClass()
					.getDeclaredMethods();
			Object[] params = new Object[1];
			params[0] = inParams;
			PubQueryData result = null;
			for (int i = 0; i < methodArray.length; i++) {
				if (methodArray[i].getName().equals(function.getQueryMethod())) {
					result = (PubQueryData) methodArray[i].invoke(
							instanceObject, params);
				}
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @return
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	/**
	 * 
	 * @param jdbcTemplate
	 */
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/**
	 * 
	 * @return
	 */
	public AbstractRowMapper getRowMapper() {
		return rowMapper;
	}

	/**
	 * 
	 * @param rowMapper
	 */
	public void setRowMapper(AbstractRowMapper rowMapper) {
		this.rowMapper = rowMapper;
	}

	public ISqlAnalyzer getSqlAnalyzer() {
		return sqlAnalyzer;
	}

	/**
	 * 
	 * @param sqlAnalyzer
	 */
	public void setSqlAnalyzer(ISqlAnalyzer sqlAnalyzer) {
		this.sqlAnalyzer = sqlAnalyzer;
	}

	public int getBatchCount() {
		return batchCount;
	}

	public void setBatchCount(int batchCount) {
		this.batchCount = batchCount;
	}

	public ISystemAuthorization getSystemAuthorization() {
		return systemAuthorization;
	}

	public void setSystemAuthorization(ISystemAuthorization systemAuthorization) {
		this.systemAuthorization = systemAuthorization;
	}

	public IFunctionAmender getFunctionAmender() {
		return functionAmender;
	}

	public void setFunctionAmender(IFunctionAmender functionAmender) {
		this.functionAmender = functionAmender;
	}

	/**
	 * @return
	 */
	public ISqlUtil getSqlUtil() {
		return sqlUtil;
	}

	/**
	 * @param sqlUtil
	 */
	public void setSqlUtil(ISqlUtil sqlUtil) {
		this.sqlUtil = sqlUtil;
		this.generateMetaMapRowMapper.setSqlUtil(sqlUtil);
	}

}
