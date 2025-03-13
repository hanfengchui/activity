/**
 * @author 万荣伟
 * @2010-11-14
 */
package com.timesontransfar.customservice.workFlowOrg.service.impl;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 万荣伟
 *
 */
@Component
public class FlowOrgFactory {
	@Autowired
	private AbstractWorkSheetDealOrg tsWorkSheetDealOrg;
	@Autowired
	private AbstractWorkSheetDealOrg ysWorkSheetDealOrg;
	@Autowired
	private AbstractWorkSheetDealOrg ynWorkSheetDealOrg;
	public static FlowOrgFactory flowOrgFactory;

	// 初始化静态参数
	@PostConstruct 
	public void init() { 
		flowOrgFactory = this; 
		flowOrgFactory.tsWorkSheetDealOrg = this.tsWorkSheetDealOrg; 
		flowOrgFactory.ysWorkSheetDealOrg = this.ysWorkSheetDealOrg; 
		flowOrgFactory.ynWorkSheetDealOrg = this.ynWorkSheetDealOrg; 
	}

	/**
	 * 得对应的流程子类
	 * 
	 * @param objName
	 * @return
	 */
	public static AbstractWorkSheetDealOrg factoryMethod(String objName) {
		if (objName.equals("tsFlowOrg")) {
			return flowOrgFactory.tsWorkSheetDealOrg;
		}
		if (objName.equals("ysFlowOrg")) {
			return flowOrgFactory.ysWorkSheetDealOrg;
		}
		if (objName.equals("ynFlowOrg")) {
			return flowOrgFactory.ynWorkSheetDealOrg;
		}
		return flowOrgFactory.tsWorkSheetDealOrg;
	}

}
