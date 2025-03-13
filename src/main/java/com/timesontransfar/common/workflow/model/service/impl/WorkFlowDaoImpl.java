package com.timesontransfar.common.workflow.model.service.impl;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.common.database.ISqlUtil;
import com.timesontransfar.common.database.KeyGenerator;

import com.timesontransfar.common.workflow.model.pojo.OuterSystemInfo;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowInstance;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNode;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNodeInstance;
import com.timesontransfar.common.workflow.model.service.IWFAccessDAO;
import com.timesontransfar.common.workflow.model.pojo.*;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class WorkFlowDaoImpl implements IWFAccessDAO {
	private static final Logger logger = LoggerFactory.getLogger(WorkFlowDaoImpl.class);

	private JdbcTemplate jdbcTemplate;

	private ISqlUtil sqlUtil;

	private KeyGenerator keyGenerator;

	private String sqlgetWFSchema;

	private String sqlgetNodeInstanceNotOver;

	private String sqlgetExceMethod;

	private String sqlgetExternalSystemInfo;

	private String sqlgetInCondition;

	private String sqlgetMethodParamsOfCondition;

	private String sqlgetMethodParamsOfNode;

	private String sqlgetNodeInstance;

	private String sqlgetNodeInstanceByInstanceId;

	private String sqlgetNodeInstanceCanRollback;

	private String sqlgetNodeInstanceOfCurrent;

	private String sqlgetOutCondition;

	private String sqlgetWFConditionByNode;

	private String sqlgetWFInstance;

	private String sqlgetWFNode;

	private String sqlgetWFNodeOfType;

	private String sqlgetWFRouteByNode;

	private String sqlinsertWFInstance;

	private String sqlinsertWFInstanceNode;

	private String sqlgetTachePos;

	private String sqlsave2ESYSState;

	private String sqlupdateWFInstance;

	private String sqlupdateWFNodeInstance;

	private String sqlupdateWFNodeInstance1;

	private String sqlgetOrgByTacheAndRegion;

	private String sqlgetOrgByCurrentWorkSheetSchema = "SELECT * FROM tsp_worksheet_flow T WHERE T.WORKSHEET_SCHEMA_ID=? AND T.REGION_ID=? ";

	private String sqlarchiveWorkFlowInstance;

	private String sqlarchiveWorkFlowInstanceNode;

	private String sqldeleteWorkFlowInstance;

	private String sqldeleteWorkFlowInstanceNode;

	public WorkFlowDaoImpl() {
		super();
	}

	public List getWFSchema(String schemaNo, String version) {
		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(0);

		if (isEmptyString(schemaNo))
			return Collections.emptyList();

		return (List) jdbcTemplate.query(sqlgetWFSchema, new Object[] { schemaNo },
				new RowMapperResultSetExtractor(rowMapper));
	}

	public List getWFNode(String nodeNo, String version) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(1);

		list = (List) jdbcTemplate.query(sqlgetWFNode, new Object[] { nodeNo },
				new RowMapperResultSetExtractor(rowMapper));

		if(list != null && !list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				WorkFlowNode node = (WorkFlowNode) list.get(i);
	
				// 
				List schemaList = getWFSchema("" + node.getWflId(), "");
				if (schemaList.isEmpty())
					node.setWorkFlowSchema(null);
				else
					node.setWorkFlowSchema((WorkFlowSchema) schemaList.get(0));
	
				this.getNodeRelative(node);
			}
		}
		return list;
	}

	public WorkFlowNode getWFNodeOfType(String wflId, short ndType) {
		List list = null;
		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);

		rowMapper.setIntClassType(1);

		list = (List) jdbcTemplate.query(sqlgetWFNodeOfType, new Object[] { wflId,
				String.valueOf(ndType) }, new RowMapperResultSetExtractor(
				rowMapper));

		if (list == null)
			return null;

		WorkFlowNode node = (WorkFlowNode) list.get(0);

		return this.getNodeRelative(node);
	}

	public List getWFRouteByNode(String nodeNo, String version) {
		List list = null;
		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(2);

		list = (List) jdbcTemplate.query(sqlgetWFRouteByNode,
				new Object[] { nodeNo }, new RowMapperResultSetExtractor(
						rowMapper));
		return list;
	}

	public WorkFlowInstance getWFInstance(String workFlowInstanceId) {
		WorkFlowInstance instance = null;
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(4);

		list = (List) jdbcTemplate.query(sqlgetWFInstance,
				new Object[] { workFlowInstanceId }, new RowMapperResultSetExtractor(
						rowMapper));
		
		if (list == null || list.isEmpty()) {
			return null;
		}
		instance = (WorkFlowInstance) list.get(0);

		List wfList = this.getWFSchema(""+instance.getWorkFlowSchema().getWflId(), "");
		instance.setWorkFlowSchema((WorkFlowSchema) wfList.get(0));

		// 获取最新的工作流节点
		List curNodeList = this.getNodeInstanceOfCurrent(instance
				.getInstanceid());
		instance.setCurNode(curNodeList);

		return instance;
	}

	public List getWFConditionByNode(String routeNo, String version) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(3);

		list = (List) jdbcTemplate.query(sqlgetWFConditionByNode,
				new Object[] { routeNo }, new RowMapperResultSetExtractor(
						rowMapper));

		if(list != null && !list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				WorkFlowCondition cond = (WorkFlowCondition) list.get(i);
	
				// 
				WorkFlowExecMethod execMethod = this.getExceMethod(cond
						.getExecMethod().getMethodcode());
				cond.setExecMethod(execMethod);
	
				// 
				List paramList = this.getMethodParamsOfCondition(cond.getCondno(),
						true);
				execMethod.setInParameterConfig(paramList.isEmpty() ? null : paramList);
				// 
				List outParamList = this.getMethodParamsOfCondition(cond
						.getCondno(), false);
				execMethod.setOutParameterConfig(outParamList.isEmpty() ? null : outParamList);
			}
		}
		return list;
	}

	public void saveWFInstance(WorkFlowInstance aInstance) {
		WorkFlowInstance instance = null;

		instance = this.getWFInstance(aInstance.getInstanceid());
		if (instance == null)
			this.insertWFInstance(aInstance);
		else
			this.updateWFInstance(aInstance);
	}

	public void updateWFInstance(WorkFlowInstance instance) {
		short endFlag = 0;

		if (instance.isEnd())
			endFlag = 1;
		else
			endFlag = 0;
		
		jdbcTemplate.update(sqlupdateWFInstance, String.valueOf(endFlag), instance.getInstanceid());
	}

	public void insertWFInstance(WorkFlowInstance instance) {
		short endFlag;

		if (instance.isEnd())
			endFlag = 1;
		else
			endFlag = 0;
		
		jdbcTemplate.update(sqlinsertWFInstance, 
				instance.getInstanceid(),
				String.valueOf(instance.getWorkFlowSchema().getWflId()),
				instance.getRegionId(), String.valueOf(endFlag));
	}

	public void updateWFNodeInstance(WorkFlowNodeInstance instance) {
		StringBuilder parents = new StringBuilder("");
		StringBuilder children = new StringBuilder("");
		this.setString(instance, parents, children);

		if (instance.getOutdatetime() == null) {
			jdbcTemplate.update(sqlupdateWFNodeInstance, 
					"" + instance.getTacheId(), instance.getIndatetime(),
					"" + instance.getRegionId(),
					parents, children, instance.getWfinstanceid(),
					String.valueOf(instance.getNodeType()),
					instance.getInstanceid());
		} else {
			jdbcTemplate.update(sqlupdateWFNodeInstance1, 
					"" + instance.getTacheId(), instance.getIndatetime(),
					"" + instance.getRegionId(),
					parents, children, instance.getWfinstanceid(),
					String.valueOf(instance.getNodeType()),
					String.valueOf(instance.getCosttime()),
					instance.getOutdatetime(), instance.getInstanceid());
		}
	}
	
	private void setString(WorkFlowNodeInstance instance, StringBuilder parents, StringBuilder children) {
		List list = null;
		list = instance.getParentid();
		if (this.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				if (i == 0) {
					parents.append((String) list.get(i));
				}else {
					parents.append("|" + list.get(i));
				}
			}
		}

		list = instance.getChildrenid();
		if (this.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				if (i == 0) {
					children.append((String) list.get(i));
				}else {
					children.append("|" + list.get(i));
				}
			}
		}
	}
	
	private boolean isNotEmpty(List list) {
		return (list != null && !list.isEmpty());
	}

	public void insertWFInstanceNode(WorkFlowNodeInstance instance) {
		String children = "";
		StringBuilder parents = new StringBuilder("");
		
		List list = null;
		list = instance.getParentid();
		if (this.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				if (i == 0) {
					parents.append((String) list.get(i));
				}else {
					parents.append("|" + list.get(i));
				}
			}
		}

		list = instance.getChildrenid();
		if (this.isNotEmpty(list)) {
			for (int i = 0; i < list.size(); i++) {
				if (i == 0) {
					children = (String) list.get(i);
				}else {
					parents.append("|" + list.get(i));
				}
			}
		}
		
		jdbcTemplate.update(sqlinsertWFInstanceNode, 
				"" + instance.getTacheId(), instance.getIndatetime(),
				"" + instance.getCosttime(), instance.getGetparament(),
				"" + instance.getRegionId(), parents.toString(), children,
				instance.getWfinstanceid(), "" + instance.getNodeType(),
				instance.getInstanceid());
	}

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	private boolean isEmptyString(String aStr) {
		return (aStr == null || aStr.equals(""));
	}

	/**
	 * 创建一个节点实例
	 * @param instance 流程实例
	 * @param workFlowNode 节点定义
	 * @return
	 */
	public WorkFlowNodeInstance createNodeInstance(WorkFlowInstance instance,
			WorkFlowNode workFlowNode, String parent) {
		ArrayList aList = null;
		WorkFlowNodeInstance nodeInstance = null;

		nodeInstance = this.getNodeInstance(instance.getInstanceid(),
				workFlowNode.getTacheId());
		if (nodeInstance == null) {
			// 
			nodeInstance = new WorkFlowNodeInstance();

			String instanceId = keyGenerator.generateKey("nodeInstance");
			nodeInstance.setInstanceid(instanceId);
			nodeInstance.setTacheId(workFlowNode.getTacheId());
			nodeInstance.setIndatetime(new Timestamp(Calendar.getInstance()
					.getTimeInMillis()));
			nodeInstance.setGetparament("");
			nodeInstance.setRegionId(instance.getRegionId());
			if (!this.isEmptyString(parent)) {
				aList = new ArrayList();
				aList.add(parent);
				nodeInstance.setParentid(aList);
			}
			nodeInstance.setChildrenid(null);
			nodeInstance.setWfinstanceid(instance.getInstanceid());
			nodeInstance.setNodeType(WorkFlowNodeInstance.NODE_TYPE_REACH);

			// 
			this.getNodeInstanceRelavite(nodeInstance);

			// 
			this.insertWFInstanceNode(nodeInstance);

			// 
			if (!this.isEmptyString(parent)) {
				WorkFlowNodeInstance parentNodeInstance = this
						.getNodeInstanceByInstanceId(parent);
				List sonList = parentNodeInstance.getChildrenid();
				if (sonList == null)
					sonList = new ArrayList();
				sonList.add(nodeInstance.getInstanceid());
				parentNodeInstance.setChildrenid(sonList);

				this.updateWFNodeInstance(parentNodeInstance);
			}

		} else {
			// 
			if (!this.isEmptyString(parent)) {
				List pList = nodeInstance.getParentid();
				if (pList == null)
					pList = new ArrayList();
				pList.add(parent);
				nodeInstance.setParentid(pList);

				// 
				this.updateWFNodeInstance(nodeInstance);

				// 
				WorkFlowNodeInstance parentNodeInstance = this
						.getNodeInstanceByInstanceId(parent);
				List sonList = parentNodeInstance.getChildrenid();
				if (sonList == null)
					sonList = new ArrayList();
				sonList.add(nodeInstance.getInstanceid());
				parentNodeInstance.setChildrenid(sonList);

				this.updateWFNodeInstance(parentNodeInstance);
			}
		}

		return nodeInstance;
	}

	public WorkFlowInstance createWorkFlowInstance(String workFlowSchemaId,
			long regionId) {
		WorkFlowInstance instance = new WorkFlowInstance();

		String instanceId = keyGenerator.generateKey("Instance");
		instance.setInstanceid(instanceId);
		List list = this.getWFSchema(workFlowSchemaId, "");
		WorkFlowSchema schema = (WorkFlowSchema) list.get(0);
		instance.setWorkFlowSchema(schema);
		instance.setRegionId(regionId);
		instance.setEnd(false);

		this.saveWFInstance(instance);
		return instance;
	}

	public void endWorkFlowInstance(WorkFlowInstance instance) {
		instance.setEnd(true);

		this.updateWFInstance(instance);

	}

	public void saveOutParam(WorkFlowInstance instance,
			WorkFlowNodeInstance nodeInstance, Map outParams) {
		logger.info("submitWorkFlow saveOutParam");
	}
	
	public WorkFlowExecMethod getExceMethod(String methodCode) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(6);

		if (this.isEmptyString(methodCode))
			return null;

		list = (List) jdbcTemplate.query(sqlgetExceMethod,
				new Object[] { methodCode }, new RowMapperResultSetExtractor(
						rowMapper));

		WorkFlowExecMethod eMethod = new WorkFlowExecMethod();
		if(list != null && !list.isEmpty()) {
			eMethod = (WorkFlowExecMethod) list.get(0);
		}
		return eMethod;
	}

	public WorkFlowInstance createWorkFlowInstance(String workFlowSchemaId,
			String instanceId, long regionId) {
		WorkFlowInstance instance = new WorkFlowInstance();

		instance.setInstanceid(instanceId);
		List schemaList = this.getWFSchema(workFlowSchemaId, "");
		WorkFlowSchema schema = (WorkFlowSchema) schemaList.get(0);
		instance.setWorkFlowSchema(schema);
		instance.setEnd(false);
		instance.setRegionId(regionId);

		this.saveWFInstance(instance);

		return instance;
	}

	public OuterSystemInfo getExternalSystemInfo(WorkFlowNode node) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(7);
		list = (List) jdbcTemplate.query(sqlgetExternalSystemInfo,
				new Object[] { node.getOutsystemid() },
				new RowMapperResultSetExtractor(rowMapper));

		if (list == null || list.isEmpty())
			return null;
		else
			return (OuterSystemInfo) list.get(0);
	}

	public List getInCondition(long tacheId) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(3);
		list = (List) jdbcTemplate.query(sqlgetInCondition, new Object[] { ""
				+ tacheId }, new RowMapperResultSetExtractor(rowMapper));
		
		if(list != null && !list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				WorkFlowCondition cond = (WorkFlowCondition) list.get(i);
	
				// 
				WorkFlowExecMethod execMethod = this.getExceMethod(cond
						.getExecMethod().getMethodcode());
				cond.setExecMethod(execMethod);
	
				// 
				List paramList = this.getMethodParamsOfCondition(cond.getCondno(),
						true);
				execMethod.setInParameterConfig(paramList.isEmpty() ? null : paramList);
				// 
				List outParamList = this.getMethodParamsOfCondition(cond
						.getCondno(), false);
				execMethod.setOutParameterConfig(outParamList.isEmpty() ? null : outParamList);
			}
		}
		return list;
	}

	public List getOutCondition(long tacheId) {
		List list = null;
		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(3);
		list = (List) jdbcTemplate.query(sqlgetOutCondition, new Object[] { ""
				+ tacheId }, new RowMapperResultSetExtractor(rowMapper));
		if(list != null && !list.isEmpty()) {
			for (int i = 0; i < list.size(); i++) {
				WorkFlowCondition cond = (WorkFlowCondition) list.get(i);
	
				// 
				WorkFlowExecMethod execMethod = this.getExceMethod(cond
						.getExecMethod().getMethodcode());
				cond.setExecMethod(execMethod);
	
				// 
				List paramList = this.getMethodParamsOfCondition(cond.getCondno(),
						true);
				execMethod.setInParameterConfig(paramList.isEmpty() ? null : paramList);
				// 
				List outParamList = this.getMethodParamsOfCondition(cond
						.getCondno(), false);
				execMethod.setOutParameterConfig(outParamList.isEmpty() ? null : outParamList);
			}
		}
		return list;
	}

	/***************************************************************************
	 * 
	 *
	 * @param workFlowInstanceId
	 * @return
	 */
	public List getNodeInstanceOfCurrent(String workFlowInstanceId) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(5);
		list = (List) jdbcTemplate.query(sqlgetNodeInstanceOfCurrent, new Object[] {
				workFlowInstanceId,
				String.valueOf(WorkFlowNodeInstance.NODE_TYPE_REACH),
				String.valueOf(WorkFlowNodeInstance.NODE_TYPE_DEALING) },
				new RowMapperResultSetExtractor(rowMapper));

		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				WorkFlowNodeInstance nodeInstance = (WorkFlowNodeInstance) list
						.get(i);

				// 
				this.getNodeInstanceRelavite(nodeInstance);
			}
		}
		return list;
	}

	public WorkFlowNodeInstance getNodeInstanceRelavite(
			WorkFlowNodeInstance nodeInstance) {
		// 
		WorkFlowNode flowNode = (WorkFlowNode) this.getWFNode(
				String.valueOf(nodeInstance.getTacheId()), "").get(0);
		nodeInstance.setWorkFlowNode(flowNode);

		return nodeInstance;
	}

	/**
	 * 取出从指定节点开始，可以连续回溯的所有父亲节点。如果遇到不能回溯的父亲节点，则该工作流实例只能回溯到该父亲节点。
	 * 约束1：如果指定节点本身不可以回溯，返回NULL
	 * 约束2：
	 * @param inWFInstanceId   开始回溯的节点实例
	 * @param nodeInstanceMap  存放可以回溯的父节点Map。必须New一个空的nodeInstanceMap传递给该方法！！！！！！！！！
	 * @return
	 */
	public Map getNodeInstanceCanRollback(String inNodeInstanceId,Map nodeInstanceMap) {
		List parentList = null;
		List parentObjectList = null;

		WorkFlowNodeInstance nodeInstance = this
				.getNodeInstanceByInstanceId(inNodeInstanceId);

		// 
		if (nodeInstance.getWorkFlowNode().getRollbackFlag() != 1)
			return nodeInstanceMap;

		// 
		parentList = nodeInstance.getParentid();
		if (parentList == null || parentList.isEmpty())
			return nodeInstanceMap;

		StringBuilder sb = new StringBuilder("");
		sb.append(sqlgetNodeInstanceCanRollback);
		for (int i = 0; i < parentList.size(); i++) {
			if (i == 0) {
				sb.append("'" + parentList.get(i) + "'");
			}else {
				sb.append(",'" + parentList.get(i) + "'");
			}
		}
		sb.append(") AND T2.TACHE_ID=T1.TACHE_ID AND T2.ROLLBACK_FLAG=1");

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(5);
		parentObjectList = (List) jdbcTemplate.query(sb.toString(), new RowMapperResultSetExtractor(
				rowMapper));//CodeSec未验证的SQL注入；CodeSec误报：1
		if (parentObjectList == null || parentObjectList.isEmpty())
			return nodeInstanceMap;
		for (int i = 0; i < parentObjectList.size(); i++) {
			WorkFlowNodeInstance pNodeInstance = (WorkFlowNodeInstance) parentObjectList
					.get(i);
			if (nodeInstanceMap.get(pNodeInstance.getInstanceid()) == null)
				nodeInstanceMap.put(pNodeInstance.getInstanceid(),
						pNodeInstance);

			// 
			this.getNodeInstanceCanRollback(pNodeInstance.getInstanceid(),
					nodeInstanceMap);
		}

		return nodeInstanceMap;
	}

	public void save2ESYSState(String instanceId, String nodeInstanceId,
			String msgType) {
		String tempId = keyGenerator.generateKey("esysmsg");
		
		jdbcTemplate.update(sqlsave2ESYSState, tempId, instanceId, nodeInstanceId, msgType);
	}

	public KeyGenerator getKeyGenerator() {
		return keyGenerator;
	}

	public void setKeyGenerator(KeyGenerator keyGenerator) {
		this.keyGenerator = keyGenerator;
	}

	/**
	 * 按照节点实例ID取得节点实例
	 * @param nodeInstanceId
	 * @return
	 */
	public WorkFlowNodeInstance getNodeInstance(String workFlowInstanceId,
			long tacheId) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(5);
		list = (List) jdbcTemplate.query(sqlgetNodeInstance, new Object[] {
				workFlowInstanceId, "" + tacheId,
				"" + WorkFlowNodeInstance.NODE_TYPE_REACH },
				new RowMapperResultSetExtractor(rowMapper));
		if (list == null || list.isEmpty())
			return null;

		WorkFlowNodeInstance nodeInstance = (WorkFlowNodeInstance) list.get(0);

		// 
		this.getNodeInstanceRelavite(nodeInstance);

		return nodeInstance;
	}

	public WorkFlowNodeInstance getNodeInstanceByInstanceId(
			String nodeInstanceId) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(5);

		list = (List) jdbcTemplate.query(sqlgetNodeInstanceByInstanceId,
				new Object[] { nodeInstanceId }, new RowMapperResultSetExtractor(
						rowMapper));
		if (list == null || list.isEmpty())
			return null;

		WorkFlowNodeInstance nodeInstance = (WorkFlowNodeInstance) list.get(0);

		// 
		this.getNodeInstanceRelavite(nodeInstance);

		return nodeInstance;
	}

	/**
	 * 
	 *
	 * @param tacheId
	 * @param methodCode
	 * @return
	 */
	private List getMethodParamsOfNode(long tacheId, String methodCode) {
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(11);
		list = (List) jdbcTemplate.query(sqlgetMethodParamsOfNode, new Object[] {
				"" + tacheId, methodCode }, new RowMapperResultSetExtractor(
				rowMapper));
		if (list == null || list.isEmpty())
			return Collections.emptyList();
		else
			return list;
	}

	private List getMethodParamsOfCondition(long condNo, boolean in) {
		String sql = "";

		sql = "" + sqlgetMethodParamsOfCondition;
		if (in) {
			sql = sql + " AND NODEMETHODTYPE=1";
		} else {
			sql = sql + " AND NODEMETHODTYPE=0";
		}
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(11);
		list = (List) jdbcTemplate.query(sql, new Object[] { "" + condNo },
				new RowMapperResultSetExtractor(rowMapper));
		if (list == null || list.isEmpty())
			return Collections.emptyList();
		else
			return list;
	}

	private WorkFlowNode getNodeRelative(WorkFlowNode node) {
		// 
		WorkFlowExecMethod inMethod = this.getExceMethod(node.getInmethod()
				.getMethodcode());
		node.setInmethod(inMethod);

		// 
		List inParamList = null;
		if (inMethod != null) {
			inParamList = this.getMethodParamsOfNode(node.getTacheId(),
					inMethod.getMethodcode());
			inMethod.setInParameterConfig(inParamList.isEmpty() ? null : inParamList);
		}

		// 
		WorkFlowExecMethod outMethod = this.getExceMethod(node.getOutmethod()
				.getMethodcode());
		node.setOutmethod(outMethod);

		// 
		List outParamList = null;
		if (outMethod != null) {
			outParamList = this.getMethodParamsOfNode(node.getTacheId(),
					outMethod.getMethodcode());
			outMethod.setInParameterConfig(outParamList.isEmpty() ? null : outParamList);
		}

		// 
		List inConditionList = this.getInCondition(node.getTacheId());
		node.setInCondition(inConditionList);

		// 
		List outConditionList = this.getOutCondition(node.getTacheId());
		node.setOutCondition(outConditionList);

		// 
		List posList = this.jdbcTemplate.queryForList(this.sqlgetTachePos,
				node.getTacheId());
		if(!posList.isEmpty()){
			Map posMap = (Map)posList.get(0);
			int xPos = Integer.parseInt(posMap.get("XPOS").toString());
			if(xPos == 0)
				xPos = 6;
			node.setXPos(xPos);

			int yPos = Integer.parseInt(posMap.get("YPOS").toString());
			if(yPos == 0)
				yPos = 6;
			node.setYPos(yPos);
		}else{
			node.setXPos(6);
			node.setYPos(6);
		}

		return node;
	}

	public Map getNodeSettingsByTacheAndRegion(String workFlowNodeId, String regionId) {
		List list = null;
		HashMap map = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(13);
		list = (List) jdbcTemplate.query(sqlgetOrgByTacheAndRegion, new Object[] {
				workFlowNodeId, regionId }, new RowMapperResultSetExtractor(
				rowMapper));
		if (list == null || list.isEmpty())
			return Collections.emptyMap();

		WorkSheetFlow flow = (WorkSheetFlow) list.get(0);
		if(flow == null)
			return Collections.emptyMap();

		map = new HashMap();
		map.put("WF__ORG__ID",flow.getFlowOrgId());
		map.put("WF__SCHEMA__ID",String.valueOf(flow.getWorksheetSchemaId()));
		return map;
	}

	public Map getNodeSettingsByCurrentWorkSchema(String workSchemaId, String itemValue, String regionId) {
		List list = null;
		HashMap map = null;
		String querySql=null;
		if(itemValue==null){
			querySql=this.sqlgetOrgByCurrentWorkSheetSchema + "AND ( T.ITEM_VALUE IS NULL OR T.ITEM_VALUE='"+regionId+"')";
		}else{
			querySql=this.sqlgetOrgByCurrentWorkSheetSchema +"AND ( T.ITEM_VALUE IS NULL OR T.ITEM_VALUE='"+regionId+"' OR T.ITEM_VALUE='"+itemValue+"')";
		}

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(13);
		list = (List) jdbcTemplate.query(querySql, new Object[] {
				workSchemaId, regionId}, new RowMapperResultSetExtractor(
				rowMapper));
		if (list == null || list.isEmpty())
			return Collections.emptyMap();

		WorkSheetFlow flow = (WorkSheetFlow) list.get(0);
		if(flow == null)
			return Collections.emptyMap();

		map = new HashMap();
		map.put("WF__ORG__ID",flow.getFlowOrgId());
		map.put("WF__SCHEMA__ID",String.valueOf(flow.getWorksheetSchemaId()));
		return map;
	}

	public ISqlUtil getSqlUtil() {
		return sqlUtil;
	}

	public void setSqlUtil(ISqlUtil sqlUtil) {
		this.sqlUtil = sqlUtil;
	}


	public void archiveWorkFlowData(String workFlowInstance){
		//保存到工作流实例历史表
		jdbcTemplate.update(sqlarchiveWorkFlowInstance, workFlowInstance);

		//保存到工作流节点实例存档表
		jdbcTemplate.update(sqlarchiveWorkFlowInstanceNode, workFlowInstance);

		//删除工作流实例表 删除工作流节点实例表
		jdbcTemplate.update(sqldeleteWorkFlowInstanceNode, workFlowInstance);
		jdbcTemplate.update(sqldeleteWorkFlowInstance, workFlowInstance);
	}

	public List getNodeInstanceNotOver(String instanceId){
		List list = null;

		WorkFlowRowMapper rowMapper = new WorkFlowRowMapper();
		rowMapper.setSqlUtil(this.sqlUtil);
		rowMapper.setIntClassType(5);

		list = (List) jdbcTemplate.query(this.sqlgetNodeInstanceNotOver, new Object[] {
				instanceId},
				new RowMapperResultSetExtractor(rowMapper));
		return list;
	}

	public void finishWorkFlowInstance(String instanceId){
		String sql = "UPDATE tsp_process_instance SET end_flag=2 WHERE instanceid=?";
		this.jdbcTemplate.update(sql, instanceId);
	}

	public String getSqlgetWFSchema() {
		return sqlgetWFSchema;
	}

	public void setSqlgetWFSchema(String sqlgetWFSchema) {
		this.sqlgetWFSchema = sqlgetWFSchema;
	}

	public String getSqlgetNodeInstanceNotOver() {
		return sqlgetNodeInstanceNotOver;
	}

	public void setSqlgetNodeInstanceNotOver(String sqlgetNodeInstanceNotOver) {
		this.sqlgetNodeInstanceNotOver = sqlgetNodeInstanceNotOver;
	}

	public String getSqlgetExceMethod() {
		return sqlgetExceMethod;
	}

	public void setSqlgetExceMethod(String sqlgetExceMethod) {
		this.sqlgetExceMethod = sqlgetExceMethod;
	}

	public String getSqlgetExternalSystemInfo() {
		return sqlgetExternalSystemInfo;
	}

	public void setSqlgetExternalSystemInfo(String sqlgetExternalSystemInfo) {
		this.sqlgetExternalSystemInfo = sqlgetExternalSystemInfo;
	}

	public String getSqlgetInCondition() {
		return sqlgetInCondition;
	}

	public void setSqlgetInCondition(String sqlgetInCondition) {
		this.sqlgetInCondition = sqlgetInCondition;
	}

	public String getSqlgetMethodParamsOfCondition() {
		return sqlgetMethodParamsOfCondition;
	}

	public void setSqlgetMethodParamsOfCondition(String sqlgetMethodParamsOfCondition) {
		this.sqlgetMethodParamsOfCondition = sqlgetMethodParamsOfCondition;
	}

	public String getSqlgetMethodParamsOfNode() {
		return sqlgetMethodParamsOfNode;
	}

	public void setSqlgetMethodParamsOfNode(String sqlgetMethodParamsOfNode) {
		this.sqlgetMethodParamsOfNode = sqlgetMethodParamsOfNode;
	}

	public String getSqlgetNodeInstance() {
		return sqlgetNodeInstance;
	}

	public void setSqlgetNodeInstance(String sqlgetNodeInstance) {
		this.sqlgetNodeInstance = sqlgetNodeInstance;
	}

	public String getSqlgetNodeInstanceByInstanceId() {
		return sqlgetNodeInstanceByInstanceId;
	}

	public void setSqlgetNodeInstanceByInstanceId(String sqlgetNodeInstanceByInstanceId) {
		this.sqlgetNodeInstanceByInstanceId = sqlgetNodeInstanceByInstanceId;
	}

	public String getSqlgetNodeInstanceCanRollback() {
		return sqlgetNodeInstanceCanRollback;
	}

	public void setSqlgetNodeInstanceCanRollback(String sqlgetNodeInstanceCanRollback) {
		this.sqlgetNodeInstanceCanRollback = sqlgetNodeInstanceCanRollback;
	}

	public String getSqlgetNodeInstanceOfCurrent() {
		return sqlgetNodeInstanceOfCurrent;
	}

	public void setSqlgetNodeInstanceOfCurrent(String sqlgetNodeInstanceOfCurrent) {
		this.sqlgetNodeInstanceOfCurrent = sqlgetNodeInstanceOfCurrent;
	}

	public String getSqlgetOutCondition() {
		return sqlgetOutCondition;
	}

	public void setSqlgetOutCondition(String sqlgetOutCondition) {
		this.sqlgetOutCondition = sqlgetOutCondition;
	}

	public String getSqlgetWFConditionByNode() {
		return sqlgetWFConditionByNode;
	}

	public void setSqlgetWFConditionByNode(String sqlgetWFConditionByNode) {
		this.sqlgetWFConditionByNode = sqlgetWFConditionByNode;
	}

	public String getSqlgetWFInstance() {
		return sqlgetWFInstance;
	}

	public void setSqlgetWFInstance(String sqlgetWFInstance) {
		this.sqlgetWFInstance = sqlgetWFInstance;
	}

	public String getSqlgetWFNode() {
		return sqlgetWFNode;
	}

	public void setSqlgetWFNode(String sqlgetWFNode) {
		this.sqlgetWFNode = sqlgetWFNode;
	}

	public String getSqlgetWFNodeOfType() {
		return sqlgetWFNodeOfType;
	}

	public void setSqlgetWFNodeOfType(String sqlgetWFNodeOfType) {
		this.sqlgetWFNodeOfType = sqlgetWFNodeOfType;
	}

	public String getSqlgetWFRouteByNode() {
		return sqlgetWFRouteByNode;
	}

	public void setSqlgetWFRouteByNode(String sqlgetWFRouteByNode) {
		this.sqlgetWFRouteByNode = sqlgetWFRouteByNode;
	}

	public String getSqlinsertWFInstance() {
		return sqlinsertWFInstance;
	}

	public void setSqlinsertWFInstance(String sqlinsertWFInstance) {
		this.sqlinsertWFInstance = sqlinsertWFInstance;
	}

	public String getSqlinsertWFInstanceNode() {
		return sqlinsertWFInstanceNode;
	}

	public void setSqlinsertWFInstanceNode(String sqlinsertWFInstanceNode) {
		this.sqlinsertWFInstanceNode = sqlinsertWFInstanceNode;
	}

	public String getSqlgetTachePos() {
		return sqlgetTachePos;
	}

	public void setSqlgetTachePos(String sqlgetTachePos) {
		this.sqlgetTachePos = sqlgetTachePos;
	}

	public String getSqlsave2ESYSState() {
		return sqlsave2ESYSState;
	}

	public void setSqlsave2ESYSState(String sqlsave2esysState) {
		sqlsave2ESYSState = sqlsave2esysState;
	}

	public String getSqlupdateWFInstance() {
		return sqlupdateWFInstance;
	}

	public void setSqlupdateWFInstance(String sqlupdateWFInstance) {
		this.sqlupdateWFInstance = sqlupdateWFInstance;
	}

	public String getSqlupdateWFNodeInstance() {
		return sqlupdateWFNodeInstance;
	}

	public void setSqlupdateWFNodeInstance(String sqlupdateWFNodeInstance) {
		this.sqlupdateWFNodeInstance = sqlupdateWFNodeInstance;
	}

	public String getSqlupdateWFNodeInstance1() {
		return sqlupdateWFNodeInstance1;
	}

	public void setSqlupdateWFNodeInstance1(String sqlupdateWFNodeInstance1) {
		this.sqlupdateWFNodeInstance1 = sqlupdateWFNodeInstance1;
	}

	public String getSqlgetOrgByTacheAndRegion() {
		return sqlgetOrgByTacheAndRegion;
	}

	public void setSqlgetOrgByTacheAndRegion(String sqlgetOrgByTacheAndRegion) {
		this.sqlgetOrgByTacheAndRegion = sqlgetOrgByTacheAndRegion;
	}

	public String getSqlarchiveWorkFlowInstance() {
		return sqlarchiveWorkFlowInstance;
	}

	public void setSqlarchiveWorkFlowInstance(String sqlarchiveWorkFlowInstance) {
		this.sqlarchiveWorkFlowInstance = sqlarchiveWorkFlowInstance;
	}

	public String getSqlarchiveWorkFlowInstanceNode() {
		return sqlarchiveWorkFlowInstanceNode;
	}

	public void setSqlarchiveWorkFlowInstanceNode(String sqlarchiveWorkFlowInstanceNode) {
		this.sqlarchiveWorkFlowInstanceNode = sqlarchiveWorkFlowInstanceNode;
	}

	public String getSqldeleteWorkFlowInstance() {
		return sqldeleteWorkFlowInstance;
	}

	public void setSqldeleteWorkFlowInstance(String sqldeleteWorkFlowInstance) {
		this.sqldeleteWorkFlowInstance = sqldeleteWorkFlowInstance;
	}

	public String getSqldeleteWorkFlowInstanceNode() {
		return sqldeleteWorkFlowInstanceNode;
	}

	public void setSqldeleteWorkFlowInstanceNode(String sqldeleteWorkFlowInstanceNode) {
		this.sqldeleteWorkFlowInstanceNode = sqldeleteWorkFlowInstanceNode;
	}

}
