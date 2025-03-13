/*
 * 创建日期 2006-8-15
 *
 */
package com.timesontransfar.common.workflow.model.service;

import java.util.List;
import java.util.Map;

import com.timesontransfar.common.workflow.model.pojo.OuterSystemInfo;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowInstance;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNode;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNodeInstance;

/**
 * @author ationr
 *
 */
@SuppressWarnings("rawtypes")
public interface IWFAccessDAO {
	/**
	 * 根据流程编号和版本号得到此流程的模版，如果版本号为空，则返回所有与此流程编号有关的流程模版
	 *
	 * @param schemaNo
	 *            流程编号
	 * @param version
	 *            版本号
	 * @return 流程模版对象WorkFlowSchema的列表
	 */
	public List getWFSchema(String schemaNo, String version);

	/**
	 * 根据环节编号和版本号得到此环节的模版，如果版本号为空，则返回所有与此环节编号有关的流程模版
	 *
	 * @param nodeNo
	 *            环节编号
	 * @param version
	 *            版本号
	 * @return 流程环节对象WorkFlowNode的列表
	 */
	public List getWFNode(String nodeNo, String version);

	/**
	 * 根据环节编号和版本号得到此环节下的所有的路由信息。
	 *
	 * @param nodeNo
	 *            节点编号
	 * @param version
	 *            节点版本号
	 * @return 路由对象WorkFlowRoute的列表
	 */
	public List getWFRouteByNode(String nodeNo, String version);

	/**
	 * 根据路由编号
	 *
	 * @param RouteNo
	 *            路由编号
	 * @param version
	 *            路由版本
	 * @return 条件对象WorkFlowCondition的列表
	 */
	public List getWFConditionByNode(String routeNo, String version);
	/**
	 * 保存流程实例
	 * @param instance
	 */
	public void saveWFInstance(WorkFlowInstance instance);
	/**
	 * 根据流程实例ID得到流程实例
	 * @param workFlowInstanceId
	 */
	public WorkFlowInstance getWFInstance(String workFlowInstanceId);
	/**
	 * 保存流程实例某一节点的输出参数
	 * @param instance
	 * @param nodeInstance
	 * @param outParams
	 */
	public void saveOutParam(WorkFlowInstance instance,WorkFlowNodeInstance nodeInstance,Map outParams);
	/**
	 * 创建一个节点实例
	 * @param instance 流程实例
	 * @param workFlowNode 节点定义
	 * @return
	 */
	public WorkFlowNodeInstance createNodeInstance(WorkFlowInstance instance,WorkFlowNode workFlowNode,String parent);
	/**
	 *
	 * @param workFlowSchemaId 工作流模版ID
	 * @return 产生的工作流实例的开始节点实例
	 */
	public WorkFlowInstance createWorkFlowInstance(String workFlowSchemaId,long regionId);

	/**
	 * 根据指定的ID产生流程实例，主要用于跨系统的流程调度
	 * @param workFlowSchemaId 工作流模版ID
	 * @param instanceId 实例ID
	 * @return 工作流实例
	 */
	public WorkFlowInstance createWorkFlowInstance(String workFlowSchemaId,String instanceId,long regionId);
	/**
	 * 根据工作流模版得到工作流的指定类型节点(如开始节点模版)
	 * @param workFlowSchemaId 工作流模版ID
	 * @return
	 */
	public WorkFlowNode getWFNodeOfType(String wflId,short ndType);
	/**
	 * 结束流程实例
	 * @param instance 流程实例
	 */
	public void endWorkFlowInstance(WorkFlowInstance instance);
	/**
	 * 获得外系统的JMS信息
	 * @param node 节点模版
	 * @return 外系统信息
	 */
	public OuterSystemInfo getExternalSystemInfo(WorkFlowNode node);

	/**
	 * 当环节是外系统的时候,需要保存发送前的状态在临时表里面,保存的表是TSP_ESYS_MSG_TEMP;
	 * @param instanceId
	 * @param nodeInstanceId
	 * @param msg_Type
	 */
	public void save2ESYSState(String instanceId,String nodeInstanceId,String msgType);
	/**
	 * 更新实例状态
	 * @param instance
	 */
	public void updateWFNodeInstance(WorkFlowNodeInstance instance);

	/**
	 * 取出从指定节点开始，可以连续回溯的所有父亲节点。如果遇到不能回溯的父亲节点，则该工作流实例只能回溯到该父亲节点。
	 * 约束1：如果指定节点本身不可以回溯，返回NULL
	 * 约束2：
	 * @param inWFInstanceId   开始回溯的节点实例
	 * @param nodeInstanceMap  存放可以回溯的父节点Map。必须New一个空的nodeInstanceMap传递给该方法！！！！！！！！！
	 * @return
	 */
	public Map getNodeInstanceCanRollback(String inNodeInstance,Map nodeInstanceMap);

	/**
	 * 找出工作流指定处理环节在特定地区负责处理的机构、工单模版等设置信息
	 * @param WorkFlowNodeId
	 * @param regionId
	 * @return Map    WF__ORG__ID  工位
	 * 					WF__SCHEMA_ID  工单模版
	 */
	public Map getNodeSettingsByTacheAndRegion(String workFlowNodeId,String regionId);

	/**
	 * 按照节点实例ID取得节点实例
	 * @param nodeInstanceId
	 * @return
	 */
	public WorkFlowNodeInstance getNodeInstanceByInstanceId(String nodeInstanceId);
	/**
	 * 在SPS中根据既定的工作流模板来取得特定地区负责处理的机构、工单模版等设置信息
	 * @param workSchemaId
	 * @param itemValue
	 * @param regionId
	 * @return
	 */
	public Map getNodeSettingsByCurrentWorkSchema(String workSchemaId,String itemValue,String regionId);


	/**
	 * 存档指定工作流实例以及所属的节点实例
	 * @param workFlowInstance
	 */
	public void archiveWorkFlowData(String workFlowInstance);

	public List getNodeInstanceNotOver(String instanceId);

	public void finishWorkFlowInstance(String instanceId);

}

