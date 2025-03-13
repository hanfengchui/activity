/*
 * 创建日期 2006-8-17
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.impl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.timesontransfar.common.workflow.IMessageSenderFactory;
import com.timesontransfar.common.workflow.IWorkFlowAttemper;
import com.timesontransfar.common.workflow.IWorkFlowInvoke;
import com.timesontransfar.common.workflow.model.pojo.NodeOParamConfig;
import com.timesontransfar.common.workflow.model.pojo.ParameterComSign;
import com.timesontransfar.common.workflow.model.pojo.ParameterType;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowCondition;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowExecMethod;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowInstance;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowMessage;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNode;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNodeInstance;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNodeType;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowRoute;
import com.timesontransfar.common.workflow.model.service.IWFAccessDAO;
import com.transfar.common.exception.MyOwnRuntimeException;

/**
 * @author ationr
 *
 * 在这个类中WF__INSTANCE__ID，WF__NODE_INSTANCE__ID，WF__ORG__ID都是专有的关键字
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class WorkFlowAttemperImpl implements IWorkFlowAttemper {
	private static final Logger log = LoggerFactory.getLogger(WorkFlowAttemperImpl.class);

	private IWFAccessDAO workFlowAccessDAO;

	private IWorkFlowInvoke workFlowInvoke;

	private IMessageSenderFactory messageSenderFactory;

	
	public List getNextNode(String currentInstanceId, String currentNodeId,
			Map inParams) {
		WorkFlowInstance wfInstance = this.workFlowAccessDAO
				.getWFInstance(currentInstanceId);
		inParams.put("WF__INSTANCE__ID", currentInstanceId);
		return this.getNextNode(wfInstance, currentNodeId, inParams);
	}

	/**
	 * 从实例中针对当前某特定的节点查找可选的下步节点的列表
	 *
	 * @param instance
	 * @param currentNodeId
	 * @param inParams
	 * @return
	 */
	private List getNextNode(WorkFlowInstance instance, String currentNodeId,
			Map inParams) {
		List currentNodeList = instance.getCurNode();// 此处有问题,当前节点应该是多个节点的集合
		List optionalList = null;
		int size = currentNodeList.size();
		for (int i = 0; i < size; i++) {
			WorkFlowNodeInstance nodeInstance = (WorkFlowNodeInstance) currentNodeList.get(i);
			if (String.valueOf(nodeInstance.getTacheId()).equals(currentNodeId)) {
				this.selectParameter(nodeInstance.getWorkFlowNode().getInmethod(), inParams);
				this.putOrgId(instance, currentNodeId, inParams);
				inParams.put("WF__NODE_INSTANCE__ID", nodeInstance
						.getInstanceid());
				optionalList = this.selectRoute(nodeInstance.getWorkFlowNode(),
						null, inParams);
			}
		}
		return optionalList;
	}

	private void putOrgId(WorkFlowInstance instance, String currentNodeId,
			Map inParams) {
		Map nodeSet = null;
		if (inParams.containsKey("WF__SCHEMA__ID")) {
			String schemaId = (String) inParams.get("WF__SCHEMA__ID");
			String itemValue = (String) inParams.get("WF__ITEM__VALUE");
			nodeSet = this.workFlowAccessDAO
					.getNodeSettingsByCurrentWorkSchema(schemaId, itemValue,
							String.valueOf(instance.getRegionId()));
		} else {
			nodeSet = this.workFlowAccessDAO.getNodeSettingsByTacheAndRegion(
					currentNodeId, String.valueOf(instance.getRegionId()));
		}
		if (nodeSet != null && !nodeSet.isEmpty()) {
			inParams.put("WF__ORG__ID", nodeSet.get("WF__ORG__ID"));
			inParams.put("WF__SCHEMA__ID", nodeSet.get("WF__SCHEMA__ID"));
		}
	}

	private List selectRoute(WorkFlowNode workflowNode, String version,
			Map inParams) {
		List optionalList = new ArrayList();
		List routeList = this.workFlowAccessDAO.getWFRouteByNode(String
				.valueOf(workflowNode.getTacheId()), version);
		for (int i = 0; i < routeList.size(); i++) {
			WorkFlowRoute workFlowRoute = (WorkFlowRoute) routeList.get(i);
			if (this.accordWithRoute(workFlowRoute, inParams)) {
				optionalList.add(workFlowRoute);
			}
		}
		return optionalList;
	}

	private boolean accordWithRoute(WorkFlowRoute route, Map inParams) {
		// 此处需要修改模型,修改方法执行后的参数集为一个Map;由Map里面的值来判断是否满足条件
		List conditionList = this.workFlowAccessDAO.getWFConditionByNode(route
				.getRtno(), String.valueOf(route.getVersion()));
		boolean accordCondition = true;
		boolean tempResult = true;
		if (conditionList != null && !conditionList.isEmpty()) {
			WorkFlowCondition condition = (WorkFlowCondition) conditionList
					.get(0);
			accordCondition = this.estimateCondition(condition, inParams);
			short joinType = condition.getRelation();
			int size = conditionList.size();
			for (int i = 1; i < size; i++) {
				condition = (WorkFlowCondition) conditionList.get(i);
				tempResult = this.estimateCondition(condition, inParams);
				switch (joinType) {
				case 0:
					accordCondition = accordCondition && tempResult;
					break;
				case 1:
					accordCondition = accordCondition || tempResult;
					break;
				default:
					accordCondition = false;
					break;
				}
				// this.workFlowInvoke.
			}
		}
		return accordCondition;

	}

	private boolean estimateCondition(WorkFlowCondition condition, Map inParams) {
		Map returnMap = null;
		WorkFlowExecMethod execMethod = condition.getExecMethod();
		// 此处需要讨论，需要定义是不是需要特定的参数
		Map selectedParams = this.selectParameter(execMethod, inParams);
		if (execMethod != null) {
			returnMap = (Map) this.workFlowInvoke.invoke(execMethod.getUrl(),
					execMethod.getJavaclass(), execMethod.getJavamethod(),
					new Object[] { selectedParams });
			return this.compareParam(execMethod.getOutParameterConfig(), returnMap);
		}
		return false;
	}

	private Map selectParameter(WorkFlowExecMethod execMethod, Map inParams) {
		if ((execMethod != null) && (execMethod.getInParameterConfig() != null)) {
			int size = execMethod.getInParameterConfig().size();
			for (int i = 0; i < size; i++) {
				NodeOParamConfig paramConfig = (NodeOParamConfig) execMethod
						.getInParameterConfig().get(i);
				inParams.put(paramConfig.getParamKey(), paramConfig.getParamValue());
			}
		}
		return inParams;
	}

	public void submitWorkFlow(WorkFlowInstance instance,
			WorkFlowNodeInstance nodeInstance, Map inParams) {
		// 如果该节点实例已经提交，不再处理
		if (nodeInstance.getNodeType() == WorkFlowNodeInstance.NODE_TYPE_OVER)
			return;

		try {
			// 自动生成方法存根
			// 退出节点
			inParams.put("WF__INSTANCE__ID", instance.getInstanceid());
			inParams.put("WF__NODE_INSTANCE__ID", nodeInstance.getInstanceid());
			log.info("Submit Tache id is " + nodeInstance.getTacheId());
			this.putOrgId(instance, String.valueOf(nodeInstance.getTacheId()),
					inParams);
			Map exitMap = this.exitNode(nodeInstance, inParams);
			if (exitMap != null) {
				inParams.putAll(exitMap);
			}

			if (nodeInstance.getWorkFlowNode().getNdtype() == WorkFlowNodeType.END_NODE) {
				log.info("Submit Tache is end Node............");

				// 完成该节点的运行
				finishNodeInstace(nodeInstance);

				// 竣工该工作流
				this.workFlowAccessDAO.endWorkFlowInstance(instance);

				// 将竣工的工作流存档
				this.workFlowAccessDAO.archiveWorkFlowData(instance
						.getInstanceid());

				// 在工作流的最后清空参数
				inParams.clear();
			} else {
				log.info("Submit Tache is common Node............");
				WorkFlowNode node = nodeInstance.getWorkFlowNode();

				// 得到可选的下一步节点
				List optionalList = this.getNextNode(instance.getInstanceid(),
						String.valueOf(node.getTacheId()), inParams);
				// 完成该节点
				finishNodeInstace(nodeInstance);

				if (optionalList != null) {
					int nextSize = optionalList.size();
					for (int i = 0; i < nextSize; i++) {
						WorkFlowRoute workFlowRoute = (WorkFlowRoute) optionalList
								.get(i);
						String nextNode = workFlowRoute.getNextNode();
						WorkFlowNode workFlowNode = (WorkFlowNode) this.workFlowAccessDAO
								.getWFNode(nextNode, null).get(0);
						WorkFlowNodeInstance nextNodeInstance = this.workFlowAccessDAO
								.createNodeInstance(instance, workFlowNode,
										nodeInstance.getInstanceid());
						log.info("After Submit ,entering Tache ID is "
								+ workFlowNode.getTacheId());
						Map returnMap = this.enterNode(instance,
								nextNodeInstance, inParams);
						if (returnMap != null) {
							this.workFlowAccessDAO.saveOutParam(instance,
									nodeInstance, returnMap);
							inParams.putAll(returnMap);
						}
						if (workFlowNode.getAutoFlag()
								&& nextNodeInstance.getNodeType() > 0) {
							this.submitWorkFlow(instance, nextNodeInstance,
									inParams);
						}
					}
					optionalList.clear();
				}
			}

		} catch (Exception e) {
			log.error("submitWorkFlow 异常{}", e.getMessage(), e);
		}
	}

	/*
	 * （非 Javadoc）
	 *
	 * @see com.timesontransfar.common.workflow.IWorkFlowAttemper#submitWorkFlow(String
	 *      instanceId,String nodeInstanceId,Map inParams)
	 */
	public void submitWorkFlow(String instanceId, String nodeInstanceId,
			Map inParams) {
		log.info("instanceId {} nodeInstanceId {} inParams {} ", instanceId, nodeInstanceId, inParams);

		try {
			WorkFlowInstance instance = this.workFlowAccessDAO
					.getWFInstance(instanceId);
			log.info("WorkFlowInstance: {}", instance);
			List currentNodeList = instance.getCurNode();// 此处有问题,当前节点应该是多个节点的集合
			
			log.info("currentNodeList: {}", currentNodeList);
			int size = currentNodeList.size();
			for (int i = 0; i < size; i++) {
				
				WorkFlowNodeInstance nodeInstance = (WorkFlowNodeInstance) currentNodeList
						.get(i);
				log.info("nodeInstance: {}", nodeInstance);
				if (nodeInstance.getInstanceid().equals(nodeInstanceId)) {
					this.submitWorkFlow(instance, nodeInstance, inParams);
				}
			}
		} catch (Exception e) {
			log.error("submitWorkFlow：{}", e.getMessage(), e);
		}
	}

	/**
	 * 执行进入节点的方法
	 *
	 * @param instance
	 *            工作流实例
	 * @param nodeInstance
	 *            工作流节点实例
	 * @param inParams
	 *            传入参数
	 * @return 执行进入节点方法后返回的参数
	 */
	private Map enterNode(WorkFlowInstance instance,
			WorkFlowNodeInstance workFlowNodeInstance, Map inParams) {
		inParams.put("WF__INSTANCE__ID", instance.getInstanceid());
		inParams.put("WF__NODE_INSTANCE__ID", workFlowNodeInstance
				.getInstanceid());
		this.selectParameter(workFlowNodeInstance.getWorkFlowNode().getInmethod(), inParams);
		this.putOrgId(instance, String.valueOf(workFlowNodeInstance
				.getTacheId()), inParams);
		Map returnMap = null;
		WorkFlowNode workFlowNode = workFlowNodeInstance.getWorkFlowNode();
		List conditionList = workFlowNode.getInCondition();
		boolean accordCondition = true;
		boolean tempResult = true;
		if (!conditionList.isEmpty()) {
			WorkFlowCondition condition = (WorkFlowCondition) conditionList
					.get(0);
			accordCondition = this.estimateCondition(condition, inParams);
			short joinType = condition.getRelation();
			int size = conditionList.size();
			for (int i = 1; i < size; i++) {
				inParams.put("WF__INSTANCE__ID", instance.getInstanceid());
				inParams.put("WF__NODE_INSTANCE__ID", workFlowNodeInstance
						.getInstanceid());
				condition = (WorkFlowCondition) conditionList.get(i);
				tempResult = this.estimateCondition(condition, inParams);
				switch (joinType) {
				case 0:
					accordCondition = accordCondition && tempResult;
					break;
				case 1:
					accordCondition = accordCondition || tempResult;
					break;
				default:
					accordCondition = false;
					break;
				}
			}
		}
		if (accordCondition) {
			// 设置节点状态为已经进入
			workFlowNodeInstance
					.setNodeType(WorkFlowNodeInstance.NODE_TYPE_DEALING);
			this.workFlowAccessDAO.updateWFNodeInstance(workFlowNodeInstance);
		
			if(WorkFlowNodeType.EXTERNAL_SYSTEM == workFlowNode.getNdtype()) {
				returnMap = this.enterExternalSysNode(instance,
						workFlowNodeInstance, inParams);
			} else {
				returnMap = this.enterCommonNode(workFlowNodeInstance, inParams);
			}
		}
		return returnMap;
	}

	/**
	 * 进入外部系统节点,压缩并发送消息
	 *
	 * @param instance
	 * @param workFlowNode
	 * @param inParams
	 * @return
	 */
	private Map enterExternalSysNode(WorkFlowInstance instance,
			WorkFlowNodeInstance workFlowNodeInstance, Map inParams) {
		WorkFlowNode workFlowNode = workFlowNodeInstance.getWorkFlowNode();
		WorkFlowMessage workFlowMessage = new WorkFlowMessage();
		workFlowMessage.setInstanceId(instance.getInstanceid());
		workFlowMessage.setMsgType(workFlowNode.getOutmsgtype());
		inParams.put("WF__REGION_ID", String.valueOf(instance.getRegionId()));
		workFlowMessage.setOutParams(inParams);
		try {
			this.workFlowAccessDAO.save2ESYSState(instance.getInstanceid(),
					workFlowNodeInstance.getInstanceid(), workFlowNode
							.getOutmsgtype());
		} catch (Exception e) {
			StringWriter writer = new StringWriter();
			PrintWriter s = new PrintWriter(writer);
			e.printStackTrace(s);
			String exception = "\n错误信息为：" + writer.toString() + " ";
			throw new MyOwnRuntimeException("不能向外系统发送消息!" + exception);
		}
		return inParams;
	}

	/**
	 * 进入普通节点
	 *
	 * @param instance
	 * @param workFlowNode
	 * @param inParams
	 * @return
	 */
	private Map enterCommonNode(WorkFlowNodeInstance workFlowNodeInstance, Map inParams) {
		Map returnMap = null;
		WorkFlowNode workFlowNode = workFlowNodeInstance.getWorkFlowNode();
		WorkFlowExecMethod execMethod = workFlowNode.getInmethod();
		// 此处需要讨论，需要定义是不是需要特定的参数
		if (execMethod != null) {
			Map selectedParams = this.selectParameter(execMethod, inParams);
			log.info("Executing entering Method  " + execMethod.getJavaclass()
					+ "." + execMethod.getJavamethod());
			returnMap = (Map) this.workFlowInvoke.invoke(execMethod.getUrl(),
					execMethod.getJavaclass(), execMethod.getJavamethod(),
					new Object[] { selectedParams });
		}
		return returnMap;

	}

	/**
	 * 退出节点,首先判断是否能够满足退出条件,然后执行退出节点的方法
	 *
	 * @param instance
	 * @param nodeInstance
	 * @param inParams
	 * @return 执行退出方法得到的参数
	 */
	private Map exitNode(WorkFlowNodeInstance nodeInstance, Map inParams) {
		// 首先判断是否满足退出节点的条件
		WorkFlowNode workFlowNode = nodeInstance.getWorkFlowNode();
		List conditionList = workFlowNode.getOutCondition();
		boolean accordCondition = true;
		boolean tempResult = true;
		if (conditionList != null && !conditionList.isEmpty()) {
			WorkFlowCondition condition = (WorkFlowCondition) conditionList
					.get(0);
			accordCondition = this.estimateCondition(condition, inParams);
			short joinType = condition.getRelation();
			int size = conditionList.size();
			for (int i = 1; i < size; i++) {
				condition = (WorkFlowCondition) conditionList.get(i);
				tempResult = this.estimateCondition(condition, inParams);
				switch (joinType) {
				case 0:
					accordCondition = accordCondition && tempResult;
					break;
				case 1:
					accordCondition = accordCondition || tempResult;
					break;
				default:
					accordCondition = false;
					break;
				}
				// this.workFlowInvoke.
			}
		}

		if (accordCondition) {
			// 如果满足退出条件，则执行退出方法
			WorkFlowExecMethod execMethod = workFlowNode.getOutmethod();
			// 此处需要讨论，需要定义是不是需要特定的参数
			Map returnMap = null;
			if (execMethod != null) {
				Map selectedParams = this.selectParameter(execMethod, inParams);
				log.info("Executing exit Method  " + execMethod.getJavaclass()
						+ "." + execMethod.getJavamethod());
				returnMap = (Map) this.workFlowInvoke.invoke(execMethod
						.getUrl(), execMethod.getJavaclass(), execMethod
						.getJavamethod(), new Object[] { selectedParams });
			}
			return returnMap;
		} else {
			throw new MyOwnRuntimeException("不能退出此节点，因为不满足该节点的退出条件！");
		}
	}

	/**
	 * 比较参数
	 *
	 * @param paramConfigMap
	 * @param paramMap
	 * @return
	 */
	private boolean compareParam(List paramConfigs, Map paramMap) {
		boolean compareResult = true;
		boolean tempResult = true;
		if (paramConfigs != null) {
			int size = paramConfigs.size();
			if (size > 0) {
				NodeOParamConfig nodeOParamConfig = (NodeOParamConfig) paramConfigs
						.get(0);
				String parameterValue = (String) paramMap.get(nodeOParamConfig
						.getParamKey());
				if (parameterValue == null) {
					compareResult = false;
				} else {
					compareResult = this.compareValue(nodeOParamConfig
							.getParamCom(), nodeOParamConfig.getParamType(),
							nodeOParamConfig.getParamValue(), parameterValue);
				}
				short joinSign = nodeOParamConfig.getParamJoin();
				for (int i = 1; i < size; i++) {
					nodeOParamConfig = (NodeOParamConfig) paramConfigs.get(i);
					parameterValue = (String) paramMap.get(nodeOParamConfig
							.getParamKey());
					if (parameterValue == null) {
						tempResult = false;
					} else {
						tempResult = this.compareValue(nodeOParamConfig
								.getParamCom(),
								nodeOParamConfig.getParamType(),
								nodeOParamConfig.getParamValue(),
								parameterValue);
					}
					switch (joinSign) {
					case 0:
						compareResult = compareResult && tempResult;
						break;
					case 1:
						compareResult = compareResult || tempResult;
						break;
					default:
						compareResult = false;
						break;
					}
					joinSign = nodeOParamConfig.getParamJoin();
				}
			}

		}
		return compareResult;
	}

	private boolean compareValue(short compareSign, short type,
			String compValue, String strValue) {
		switch (type) {
		case ParameterType.TYPE_BOOLEAN:
			Boolean boolValue = Boolean.parseBoolean(strValue);
			return boolValue.booleanValue();
		case ParameterType.TYPE_CHAR:
			char charCompValue = compValue.charAt(0);
			char charValue = strValue.charAt(0);
			switch (compareSign) {
			case ParameterComSign.EQUAL:
				return charCompValue == charValue;
			case ParameterComSign.BIGGER:
				return charValue > charCompValue;
			case ParameterComSign.BIGGER_EQUAL:
				return charValue >= charCompValue;
			case ParameterComSign.LESS:
				return charValue < charCompValue;
			case ParameterComSign.LESS_EQUAL:
				return charValue <= charCompValue;
			default:
				return false;
			}
		case ParameterType.TYPE_FLOAT:
		case ParameterType.TYPE_INT:
		case ParameterType.TYPE_LONG:
			float floatCompValue = Float.parseFloat(compValue);
			float floatValue = Float.parseFloat(strValue);
			switch (compareSign) {
			case ParameterComSign.EQUAL:
				return floatCompValue == floatValue;
			case ParameterComSign.BIGGER:
				return floatValue > floatCompValue;
			case ParameterComSign.BIGGER_EQUAL:
				return floatValue >= floatCompValue;
			case ParameterComSign.LESS:
				return floatValue < floatCompValue;
			case ParameterComSign.LESS_EQUAL:
				return floatValue <= floatCompValue;
			default:
				return false;
			}
		case ParameterType.TYPE_STRING:
			int comResult = strValue.compareToIgnoreCase(compValue);
			switch (compareSign) {
			case ParameterComSign.EQUAL:
				return comResult == 0;
			case ParameterComSign.BIGGER:
				return comResult > 0;
			case ParameterComSign.BIGGER_EQUAL:
				return comResult >= 0;
			case ParameterComSign.LESS:
				return comResult < 0;
			case ParameterComSign.LESS_EQUAL:
				return comResult <= 0;
			case ParameterComSign.LIKE:
				return strValue.indexOf(compValue) >= 0;
			default:
				return false;
			}
		default:
			return false;
		}

	}

	/*
	 * （非 Javadoc）
	 *
	 * @see com.timesontransfar.common.workflow.IWorkFlowAttemper#activeWorkFlow(String
	 *      workFlowSchemaId,Map inParams)
	 */
	public void activeWorkFlow(String workFlowSchemaId, Map inParams) {
		String regionStr = (String) inParams.get("WF__REGION_ID");
		long regionId = -1;

		try {

			if (regionStr != null) {
				regionId = Long.parseLong(regionStr);
			}
			if (regionId == -1) {
				throw new MyOwnRuntimeException("没有得到地域信息，激活工作流失败");
			}
			WorkFlowInstance instance = this.workFlowAccessDAO
					.createWorkFlowInstance(workFlowSchemaId, regionId);
			WorkFlowNode beginNode = this.workFlowAccessDAO.getWFNodeOfType(
					workFlowSchemaId, WorkFlowNodeType.BEGIN_NODE);
			WorkFlowNodeInstance nextNodeInstance = this.workFlowAccessDAO
					.createNodeInstance(instance, beginNode, null);
			Map returnMap = this
					.enterNode(instance, nextNodeInstance, inParams);
			if (returnMap != null) {
				inParams.putAll(returnMap);
			}
			if (beginNode.getAutoFlag()) {
				this.submitWorkFlow(instance, nextNodeInstance, inParams);
			}
		} catch (Exception e) {
			throw new MyOwnRuntimeException(e.toString());
		}

	}

	/**
	 * @return 返回 workFlowAccessDAO。
	 */
	public IWFAccessDAO getWorkFlowAccessDAO() {
		return workFlowAccessDAO;
	}

	/**
	 * @param workFlowAccessDAO
	 *            要设置的 workFlowAccessDAO。
	 */
	public void setWorkFlowAccessDAO(IWFAccessDAO workFlowAccessDAO) {
		this.workFlowAccessDAO = workFlowAccessDAO;
	}

	/**
	 * @return 返回 workFlowInvoke。
	 */
	public IWorkFlowInvoke getWorkFlowInvoke() {
		return workFlowInvoke;
	}

	/**
	 * @param workFlowInvoke
	 *            要设置的 workFlowInvoke。
	 */
	public void setWorkFlowInvoke(IWorkFlowInvoke workFlowInvoke) {
		this.workFlowInvoke = workFlowInvoke;
	}

	/**
	 * @return 返回 messageSenderFactory。
	 */
	public IMessageSenderFactory getMessageSenderFactory() {
		return messageSenderFactory;
	}

	/**
	 * @param messageSenderFactory
	 *            要设置的 messageSenderFactory。
	 */
	public void setMessageSenderFactory(
			IMessageSenderFactory messageSenderFactory) {
		this.messageSenderFactory = messageSenderFactory;
	}

	public Map checkRollbackable(String nodeInstanceId) {
		boolean rollbackable = true;
		Map retMap = new HashMap();
		List retList = new ArrayList();

		// 检查所有的子节点是否可以回退。如果可以，将子节点加入rollbackNodes
		WorkFlowNodeInstance nodeInstance = this.workFlowAccessDAO
				.getNodeInstanceByInstanceId(nodeInstanceId);
		List sonList = nodeInstance.getChildrenid();
		if (sonList != null) {
			for (int i = 0; i < sonList.size(); i++) {
				Map sonMap = checkRollbackable((String) sonList.get(i));
				if (sonMap == null || sonMap.get("ROLLABLE") == null
						|| !((String) sonMap.get("ROLLABLE")).equals("1")) {
					rollbackable = false;
					break;
				} else {
					retList.addAll((List) sonMap.get("ROLLABLE_LIST"));
				}
			}
		}

		if (!rollbackable) {
			retMap.put("ROLLABLE", "0");
			retMap.put("ROLLABLE_LIST", retList);
			return retMap;
		}

		// 检查自身是否可以回退
		if (nodeInstance.getWorkFlowNode().getRollbackFlag() != 1) {
			retMap.put("ROLLABLE", "0");
		} else {
			retList.add(nodeInstanceId);
			retMap.put("ROLLABLE", "1");
		}
		retMap.put("ROLLABLE_LIST", retList);
		return retMap;
	}

	/**
	 * 将指定节点的一个子节点换成另一个子节点
	 *
	 * @param nodeInstanceId
	 * @param childInstanceId
	 * @param newChildInstanceId
	 */
	public void replaceChildren(String nodeInstanceId, String childInstanceId,
			String newChildInstanceId) {
		WorkFlowNodeInstance nodeInstance = this.workFlowAccessDAO
				.getNodeInstanceByInstanceId(nodeInstanceId);

		List childList = nodeInstance.getChildrenid();
		if (childList == null) {
			childList = new ArrayList();
			nodeInstance.setChildrenid(childList);
		}
		if (childList.contains(childInstanceId)) {
			childList.remove(childInstanceId);
		}
		childList.add(newChildInstanceId);

		this.workFlowAccessDAO.updateWFNodeInstance(nodeInstance);
	}

	public boolean cancelWorkFlow(String instanceId) {
		boolean ret = false;

		// 将所有未完结的节点实例设置为完成
		List list = this.workFlowAccessDAO.getNodeInstanceNotOver(instanceId);
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				WorkFlowNodeInstance nodeInstance = (WorkFlowNodeInstance) list
						.get(i);
				finishNodeInstace(nodeInstance);
			}
		}

		// 更改工作流状态
		this.workFlowAccessDAO.finishWorkFlowInstance(instanceId);

		// 存档工作流数据
		this.workFlowAccessDAO.archiveWorkFlowData(instanceId);

		ret = true;
		return ret;
	}

	/**
	 * 将节点实例置为已完成
	 *
	 * @param nodeInstance
	 */
	public void finishNodeInstace(WorkFlowNodeInstance nodeInstance) {
		// 设置完成时间
		nodeInstance.setOutdatetime(new Timestamp(Calendar.getInstance()
				.getTimeInMillis()));
		// 修改节点的状态为已完成
		nodeInstance.setNodeType(WorkFlowNodeInstance.NODE_TYPE_OVER);

		// 计算运行时间
		nodeInstance.setCosttime(nodeInstance.getOutdatetime().getTime()
				- nodeInstance.getIndatetime().getTime());

		this.workFlowAccessDAO.updateWFNodeInstance(nodeInstance);
	}

	public void finishNodeInstace(String nodeInstanceId) {
		WorkFlowNodeInstance nodeInstance = this.workFlowAccessDAO
				.getNodeInstanceByInstanceId(nodeInstanceId);

		finishNodeInstace(nodeInstance);
	}

	/**
	 * 将工作流实例从当前节点调度到指定节点
	 *
	 * @param instanceId
	 * @param nodeInstanceId
	 * @param nextTacheId
	 * @param inParams
	 */
	public void attemperWorkFlowNode(String instanceId, String nodeInstanceId,
			String nextTacheId, Map inParams) {
		// 完成当前工作流节点
		finishNodeInstace(nodeInstanceId);

		// 标记将要创建的节点是因为调度产生的。在某些节点（如定单接收），调度到该节点后就会停留在那里。如果非调度到达，就会自动执行
		inParams.put("WF__ATTEMPER__FLAG", "1");

		// 创建目的工作节点实例
		WorkFlowInstance instance = this.workFlowAccessDAO
				.getWFInstance(instanceId);
		WorkFlowNode workFlowNode = (WorkFlowNode) this.workFlowAccessDAO
				.getWFNode(nextTacheId, null).get(0);
		WorkFlowNodeInstance nextNodeInstance = this.workFlowAccessDAO
				.createNodeInstance(instance, workFlowNode, nodeInstanceId);
		Map returnMap = this.enterNode(instance, nextNodeInstance, inParams);
		if (returnMap != null) {
			inParams.putAll(returnMap);
		}

		if (workFlowNode.getAutoFlag() && nextNodeInstance.getNodeType() > 0) {
			this.submitWorkFlow(instance, nextNodeInstance, inParams);
		}
	}

}
