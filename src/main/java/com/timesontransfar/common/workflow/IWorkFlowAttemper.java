/*
 * 创建日期 2006-8-15
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow;

import java.util.List;
import java.util.Map;

import com.timesontransfar.common.workflow.model.pojo.WorkFlowInstance;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNodeInstance;

/**
 * @author ationr
 *
 * 要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
@SuppressWarnings("rawtypes")
public interface IWorkFlowAttemper {
	/**
	 * 根据当前流程实例的当前节点，得到可选的下一步节点
	 *
	 * @param currentInstanceId
	 *            当前流程实例ID
	 * @param curr
	 *            entNodeId 当前流程节点ID
	 * @param inParams
	 *            页面传入的参数集合
	 * @return 节点的集合。
	 *
	 */
	public List getNextNode(String currentInstanceId, String currentNodeId,
			Map inParams);

	/**
	 * 根据提交流程实例提交流程
	 *
	 * @param instance
	 *            流程实例
	 */
	public void submitWorkFlow(WorkFlowInstance instance,
			WorkFlowNodeInstance nodeInstance, Map inParams);

	/**
	 * 根据流程实例ID提交流程
	 *
	 * @param instanceId
	 */
	public void submitWorkFlow(String instanceId, String nodeInstanceId,
			Map inParams);

	/**
	 * 用指定的流程模版激活流程
	 *
	 * @param workFlowSchemaId
	 *            流程实例模版ID
	 * @param inParams
	 *            传入参数 此处存在一个约定，如果需要传入地域信息，需要在inParams传入以WF__REGION_ID为关键字的字符串信息
	 */
	public void activeWorkFlow(String workFlowSchemaId, Map inParams);

	/**
	 * 检查指定节点是否可以回退
	 *
	 * @param nodeInstanceId
	 * @return
	 */
	public Map checkRollbackable(String nodeInstanceId);

	public boolean cancelWorkFlow(String instanceId);

	public void finishNodeInstace(WorkFlowNodeInstance nodeInstance);

	public void finishNodeInstace(String nodeInstanceId);

	/**
	 * 将工作流实例从当前节点调度到指定节点
	 *
	 * @param instanceId
	 * @param nodeInstanceId
	 * @param nextTacheId
	 * @param inParams
	 */
	public void attemperWorkFlowNode(String instanceId, String nodeInstanceId,
			String nextTacheId, Map inParams);

}
