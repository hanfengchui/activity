package com.timesontransfar.common.workflow.impl;

import java.util.List;
import java.util.Map;

import com.timesontransfar.common.workflow.IWorkFlowAttemper;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowInstance;
import com.timesontransfar.common.workflow.model.pojo.WorkFlowNodeInstance;

@SuppressWarnings("rawtypes")
public class WorkFlowAttemperFacade implements IWorkFlowAttemper {
	
	private IWorkFlowAttemper workFlowAttemper;

	public void activeWorkFlow(String workFlowSchemaId, Map inParams) {
		this.workFlowAttemper.activeWorkFlow(workFlowSchemaId, inParams);
	}
	
	public Map checkRollbackable(String nodeInstanceId) {
		return this.workFlowAttemper.checkRollbackable(nodeInstanceId);
	}

	public List getNextNode(String currentInstanceId, String currentNodeId,
			Map inParams) {
		return this.workFlowAttemper.getNextNode(currentInstanceId, currentNodeId, inParams);
	}

	public void submitWorkFlow(WorkFlowInstance instance,
			WorkFlowNodeInstance nodeInstance, Map inParams) {
		this.workFlowAttemper.submitWorkFlow(instance, nodeInstance, inParams);
	}

	public void submitWorkFlow(String instanceId, String nodeInstanceId,
			Map inParams) {
		this.workFlowAttemper.submitWorkFlow(instanceId, nodeInstanceId, inParams);
	}

	public IWorkFlowAttemper getWorkFlowAttemper() {
		return workFlowAttemper;
	}

	public void setWorkFlowAttemper(IWorkFlowAttemper workFlowAttemper) {
		this.workFlowAttemper = workFlowAttemper;
	}

	public void attemperWorkFlowNode(String instanceId, String nodeInstanceId, String nextTacheId, Map inParams) {
		 this.workFlowAttemper.attemperWorkFlowNode(instanceId, nodeInstanceId, nextTacheId, inParams);
	}

	public boolean cancelWorkFlow(String instanceId) {
		return this.workFlowAttemper.cancelWorkFlow(instanceId);
	}

	public void finishNodeInstace(WorkFlowNodeInstance nodeInstance) {
		this.workFlowAttemper.finishNodeInstace(nodeInstance);
	}

	public void finishNodeInstace(String nodeInstanceId) {
		this.workFlowAttemper.finishNodeInstace(nodeInstanceId);

	}
	
}
