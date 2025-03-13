/*
 * 创建日期 2006-8-28
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.impl;

import java.util.Map;

import com.timesontransfar.common.workflow.IWorkFlowAttemper;
import com.timesontransfar.common.workflow.IWorkFlowAttemper4Invoke;

/**
 * @author ationr
 *
 * 要更改此生成的类型注释的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class WorkFlowAttemper4Invoke implements IWorkFlowAttemper4Invoke {
	private IWorkFlowAttemper workFlowAttemper;
	/* （非 Javadoc）
	 * @see com.timesontransfar.common.workflow.IWorkFlowAttemper4Invoke#submitWorkflow(java.util.Map)
	 */
	
	public Map submitWorkflow(Map inParams) {
		// 自动生成方法存根
		String instanceId=(String)inParams.get("WF__RET__INSTANCE__ID");
		String nodeInstanceId=(String)inParams.get("WF__RET__NODE__INSTANCE__ID");
		if(instanceId==null || nodeInstanceId==null){
			throw new RuntimeException("提交流程的参数不完整!无法提交!");
		}else{
			inParams.remove("WF__RET__INSTANCE__ID");
			inParams.remove("WF__RET__NODE__INSTANCE__ID");
			this.workFlowAttemper.submitWorkFlow(instanceId,nodeInstanceId,inParams);
		}
		return null;
	}

	/**
	 * @return 返回 workFlowAttemper。
	 */
	public IWorkFlowAttemper getWorkFlowAttemper() {
		return workFlowAttemper;
	}
	/**
	 * @param workFlowAttemper 要设置的 workFlowAttemper。
	 */
	public void setWorkFlowAttemper(IWorkFlowAttemper workFlowAttemper) {
		this.workFlowAttemper = workFlowAttemper;
	}
}
