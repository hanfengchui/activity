/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.workFlowOrg.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.workFlowOrg.dao.IworkSheetFlowOrgDao;
import com.timesontransfar.customservice.workFlowOrg.dao.IworkSheetItmeDao;
import com.timesontransfar.customservice.workFlowOrg.dao.IworkSheetSchemaDao;
import com.timesontransfar.customservice.workFlowOrg.dao.IworksheetRuleDao;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetFlowOrg;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRule;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetRuleItem;
import com.timesontransfar.customservice.workFlowOrg.pojo.WorkSheetSchema;
import com.timesontransfar.customservice.workFlowOrg.service.IsheetFlowOrg;
import com.timesontransfar.sheetHandler.CompatHandler;

/**
 * @author 万荣伟
 *
 */
@Component("sheetFlowOrgId")
public class SheetFlowOrgImpl implements IsheetFlowOrg{
	private static final Logger log = LoggerFactory.getLogger(SheetFlowOrgImpl.class);
	@Autowired
	private IworkSheetSchemaDao iworkSchemaDao;
	@Autowired
	private IworksheetRuleDao   iworkRuleDao;
	@Autowired
	private IworkSheetFlowOrgDao iworkFlowOrgDao;
	@Autowired
	private IworkSheetItmeDao	  iworkIremDao;
	@Autowired
	private PubFunc pubFunc;
	
	/* (non-Javadoc)
	 * @see com.timesontransfar.customservice.workFlowOrg.service.IsheetFlowOrg#getFlowOrgId(int, int, java.lang.String, int, int)
	 */
	public String getFlowOrgId(int tacheId,int workSheetType,String itemVaule,int regionId, int serviceType) {
	    WorkSheetSchema schemaBean = null;
	    if(CompatHandler.isServTypeComplaint(serviceType)){
	        schemaBean = iworkSchemaDao.getFlowSchema(tacheId, workSheetType, serviceType);
	    }else{
	        schemaBean = iworkSchemaDao.getFlowSchema(tacheId, workSheetType, workSheetType);
	    }
	    if(schemaBean == null) {
	    	 log.debug("在环节ID:"+tacheId+"且工单类型为:"+workSheetType+",在工单模板中未找到对应的模板");
             return "A";
        }
        int workSheetSchemaId = schemaBean.getWorksheetSchemaId();
        //在规则表中(TSP_WORKSHEET_RULE)找到对应的规则
        WorkSheetRule ruleBean = this.iworkRuleDao.getSheetFlowRule(workSheetSchemaId);
        if(ruleBean == null) {
        	log.debug("没有找模板ID为:"+workSheetSchemaId+"规则");
            return "B";       
        }
        WorkSheetFlowOrg sheetFlowOrg = new WorkSheetFlowOrg();
        sheetFlowOrg.setWorksheetSchemaId(workSheetSchemaId);
        sheetFlowOrg.setTachId(tacheId);
        sheetFlowOrg.setRegionId(regionId);
        sheetFlowOrg.setRuleId(ruleBean.getRuleId());
        WorkSheetFlowOrg[] itemBean = this.iworkFlowOrgDao.getFlowOrgId(sheetFlowOrg);
        if(itemBean == null) {
        	log.debug("没有找工单细目");
            return "C";        
        }
        String orgId="D";
        int orgSize = itemBean.length;
        for(int i=0;i<orgSize;i++) {
            if(itemVaule.equals(itemBean[i].getItemVaule())) {
                orgId = itemBean[i].getFlowOrgid();
                break;
            }
        }
        return orgId;
	}
	
	/**
	 * 根据环节的工单类型和匹配值来找到对应的工位
	 * @param tacheId 环节ID
	 * @param workSheetType工单类型
	 * @param itemVaule匹配值
	 * @return返回工位ID
	 */
	public String getFlowOrgId(int tacheId,int workSheetType,String itemVaule,int regionId) {
		// 在工单模板表中(TSP_WORKSHEET_SCHEMA) 找对应的模板
		WorkSheetSchema schemaBean = this.iworkSchemaDao.getFlowSchema(tacheId, workSheetType);//得到工单模板
		if(schemaBean == null) {
			log.warn("在环节ID:"+tacheId+"且工单类型为:"+workSheetType+",在工单模板中未找到对应的模板");
			return "A";
		}
		int workSheetSchemaId = schemaBean.getWorksheetSchemaId();
		//在规则表中(TSP_WORKSHEET_RULE)找到对应的规则
		WorkSheetRule ruleBean = this.iworkRuleDao.getSheetFlowRule(workSheetSchemaId);
		if(ruleBean == null) {
			log.warn("没有找模板ID为:"+workSheetSchemaId+"规则");
			return "B";
		}
		WorkSheetFlowOrg sheetFlowOrg = new WorkSheetFlowOrg();
		sheetFlowOrg.setWorksheetSchemaId(workSheetSchemaId);
		sheetFlowOrg.setTachId(tacheId);
		sheetFlowOrg.setRegionId(regionId);
		sheetFlowOrg.setRuleId(ruleBean.getRuleId());
		WorkSheetFlowOrg[] itemBean = this.iworkFlowOrgDao.getFlowOrgId(sheetFlowOrg);
		if(itemBean == null) {
			log.warn("没有找工单细目");
			return "C";
		}
		String orgId = "D";
		int orgSize = itemBean.length;
		for(int i=0;i<orgSize;i++) {
			if(itemVaule.equals(itemBean[i].getItemVaule())) {
				orgId = itemBean[i].getFlowOrgid();
				break;
			}
		}
		return orgId;
	}
	/**
	 * 根据静态ID得到部门(SP,网厅/掌厅的投诉流程)
	 * @param refId
	 * @return
	 */
	public String getSataticOrg(int refId) {
		return this.iworkFlowOrgDao.getSataticOrg(refId);
	}
	/**
	 * 根据模板规则ID来找到对应的模板规则类型
	 * @param ruleId
	 * @return
	 */
	public String getRuleType(int itemId) {
		int ruleId = this.iworkIremDao.getRuleId(itemId);//模板规则ID
		WorkSheetRule ruleBean = this.iworkRuleDao.getItemRule(ruleId);
		if(ruleBean == null) {
			return "null";
		}
		return String.valueOf(ruleBean.getRuleType());
		
	}
	/**
	 * 保存工单模板
	 * @param scheamBean
	 * @param ruleBean
	 * @return
	 */
	public String saveSchemaRule(WorkSheetSchema scheamBean,WorkSheetRule ruleBean) {
		//根据环节和工单类型来判断
		WorkSheetSchema bean = this.iworkSchemaDao.getFlowSchema(scheamBean.getTachId(), scheamBean.getWorkSheetType());
		if(bean != null) {
			if(log.isDebugEnabled()) {
				log.debug("在环节："+scheamBean.getTachId()+"工单类型的"+scheamBean.getWorkSheetType()+"模板已经存在");
			}
			return "ERRORTEMP";
		}
		
		int schemaId = this.pubFunc.schemaId();
		//模板
		scheamBean.setWorksheetSchemaId(schemaId);
		this.iworkSchemaDao.saveFlowSchema(scheamBean);
		//模板规则
		ruleBean.setRuleId(schemaId);
		this.iworkRuleDao.saveRuleObj(ruleBean);
		//保存模板和规则关联表
		this.iworkRuleDao.saveReleSchemaReal(schemaId, schemaId);
		
		return "SUCCESS";
	}
	
	
	/**
	 * 保存工单细类规则
	 * @param itemBean
	 * @param ruleId
	 * @return
	 */
	public String saveItmeObj(WorkSheetRuleItem itemBean,int ruleId) {
		int itemId = this.pubFunc.schemaId();
/*		int checkRuleId = this.iworkIremDao.getRuleId(itemId);
		if(checkRuleId == ruleId) {
			log.debug("该模板规则"+ruleId+"已有对应的细类规则");
			return "RULEID";
		}*/
		itemBean.setItemId(itemId);
		this.iworkIremDao.saveItmeObj(itemBean);
		//保存细类规则和模板规则关联关系
		this.iworkIremDao.saveRuleitmeReal(ruleId, itemId);
		return "SUCCESS";
	}
	
	/**
	 * 保存工单流向
	 * @param flowBean
	 * @return
	 */
	public String saveSheetFlowOrg(WorkSheetFlowOrg flowBean) {
		int itemId = flowBean.getItemId();//规则细类ID
		int ruleId = this.iworkIremDao.getRuleId(itemId);//模板规则ID
		int flowId = this.pubFunc.schemaId();
		WorkSheetSchema bean = this.iworkSchemaDao.getRuleFlowObj(ruleId);
		if(bean == null) {
			log.error("根据细类规则："+itemId+"没有找到相对应的工单模板，程序正常返回");
			return "TEMP";
		}
		flowBean.setWsFlowRuleId(flowId);
		flowBean.setTachId(bean.getTachId());
		flowBean.setWorksheetSchemaId(bean.getWorksheetSchemaId());
		flowBean.setRuleId(ruleId);
		WorkSheetFlowOrg[] itemBean = this.iworkFlowOrgDao.getFlowOrgId(flowBean);
		
		if(itemBean == null) {
			if(log.isDebugEnabled()) {
				log.debug("没有找工单细目");
			}	
			this.iworkFlowOrgDao.saveflowOrgObj(flowBean);
			return "SUCCESS";
		}
		String itemVaule = flowBean.getItemVaule();
		int orgSize = itemBean.length;
		for(int i=0;i<orgSize;i++) {
			if(itemVaule.equals(itemBean[i].getItemVaule())) {
				
				return "ITEMVALUE";
			}
		}
		this.iworkFlowOrgDao.saveflowOrgObj(flowBean);
		return "SUCCESS";
	}
	/**
	 * 更新工单流向工位
	 * @param wsFlowOrg
	 * @param orgId
	 * @return
	 */
	public String updateSheetFlowOrg(int wsFlowOrg,int orgId,int region,int ruleId) {
		int size = 0;
		size = this.iworkFlowOrgDao.updateFlowOrg(wsFlowOrg, orgId,region);
		size += this.iworkRuleDao.updateRuleDate(ruleId);
		if(size > 0) {
			return "SUCCESS";
		}
		return "ERROR";
	}
	//===========================================
	/**
	 * @return iworkFlowOrgDao
	 */
	public IworkSheetFlowOrgDao getIworkFlowOrgDao() {
		return iworkFlowOrgDao;
	}
	/**
	 * @param iworkFlowOrgDao 要设置的 iworkFlowOrgDao
	 */
	public void setIworkFlowOrgDao(IworkSheetFlowOrgDao iworkFlowOrgDao) {
		this.iworkFlowOrgDao = iworkFlowOrgDao;
	}
	/**
	 * @return iworkRuleDao
	 */
	public IworksheetRuleDao getIworkRuleDao() {
		return iworkRuleDao;
	}
	/**
	 * @param iworkRuleDao 要设置的 iworkRuleDao
	 */
	public void setIworkRuleDao(IworksheetRuleDao iworkRuleDao) {
		this.iworkRuleDao = iworkRuleDao;
	}
	/**
	 * @return iworkSchemaDao
	 */
	public IworkSheetSchemaDao getIworkSchemaDao() {
		return iworkSchemaDao;
	}
	/**
	 * @param iworkSchemaDao 要设置的 iworkSchemaDao
	 */
	public void setIworkSchemaDao(IworkSheetSchemaDao iworkSchemaDao) {
		this.iworkSchemaDao = iworkSchemaDao;
	}

	/**
	 * @return pubFunc
	 */
	public PubFunc getPubFunc() {
		return pubFunc;
	}

	/**
	 * @param pubFunc 要设置的 pubFunc
	 */
	public void setPubFunc(PubFunc pubFunc) {
		this.pubFunc = pubFunc;
	}

	/**
	 * @return iworkIremDao
	 */
	public IworkSheetItmeDao getIworkIremDao() {
		return iworkIremDao;
	}

	/**
	 * @param iworkIremDao 要设置的 iworkIremDao
	 */
	public void setIworkIremDao(IworkSheetItmeDao iworkIremDao) {
		this.iworkIremDao = iworkIremDao;
	}

	public static Logger getLog() {
		return log;
	}
	
}
