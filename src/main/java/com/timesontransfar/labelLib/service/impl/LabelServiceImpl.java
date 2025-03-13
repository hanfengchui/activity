package com.timesontransfar.labelLib.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.labelLib.dao.ILabelInstanceDao;
import com.timesontransfar.labelLib.dao.ILabelTemplateDao;
import com.labelLib.pojo.LabelInstance;
import com.labelLib.pojo.LabelPoint;
import com.timesontransfar.labelLib.service.ILabelService;
import com.timesontransfar.labelLib.util.LabelLibStaticData;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.labelmanage.service.ILabelManageService;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;

@SuppressWarnings({"rawtypes", "unchecked"})
@Component(value="labelServiceImpl__FACADE__")
public class LabelServiceImpl implements ILabelService {
	
    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(LabelServiceImpl.class);
    
    @Autowired
    private ILabelTemplateDao labelTemplateDaoImpl;//标签模板DAO
    
    @Autowired
    private ILabelInstanceDao labelInstanceDaoImpl;//标签实例DAO
    
    @Autowired
    private ISheetPubInfoDao sheetPubInfoDaoImpl;   
    
    @Autowired
    private PubFunc pubFunc;
    
    @Autowired
	private ILabelManageService labelManageService;
    
    
    @Override
	public Map queryAllLabel(String orderId, String sheetId) {
    	//处理页的标签
		Map dealMap = queryLabelBySheetId("complaintDealNew.jsp", sheetId);
		
		//受理页的标签
		Map acceptMap = queryLabelHandByOrderId("serviceAccepte.jsp",orderId);
		
		Map<String,Map> m = new HashMap<>();
		m.put("dealMap", dealMap);
		m.put("acceptMap", acceptMap);
		return m;
	}

    @Override
    public Map queryLabelBySheetId(String url,String workSheetId){
        SheetPubInfo sheet = sheetPubInfoDaoImpl.getSheetPubInfo(workSheetId, false);
        if(sheet!=null){
            String condition = piecedCondition(sheet.getSheetType());
            if(condition.length()>0){
                TsmStaff staff = pubFunc.getLogonStaff();
                List<LabelPoint> ls = labelTemplateDaoImpl.queryLabelBySheetId(url,LabelLibStaticData.LABELPOINT_HAND,condition,staff.getOrganizationId());
                if(!ls.isEmpty()){
                    return changeMap(ls);
                }
            }
        }
        return null;
    }
    
    private String piecedCondition(int tacheId) {

        StringBuilder sb = new StringBuilder();
        List ls = labelTemplateDaoImpl.queryPointByTacheId(tacheId);
        for(int i=0;i<ls.size();i++){
            Map s = (Map)ls.get(i) ;
            String k = s.get("INSERT_POINTS_ID").toString();
            String p = i==ls.size()-1?k:k+",";
            sb.append(p);
        }
        return sb.toString();
    }
    

    @Override
    public Map queryLabelHandByOrderId(String url,String orderId){
        List<LabelPoint> ls = labelTemplateDaoImpl.queryLabelHandByOrderId(url, LabelLibStaticData.LABELPOINT_HAND,orderId);
        if(!ls.isEmpty()){
            return changeMap(ls);
        }
        return null;
    }
    
    private Map changeMap(List<LabelPoint> ls) {
        Map m = new HashMap();
        for (int i = 0; i < ls.size(); i++) {
            LabelPoint p = ls.get(i);
            String pn = p.getPointName();
            if(p.getPropFlag().equals("1")){
                List list = this.labelTemplateDaoImpl.queryLabelProperty(p.getLabelId());
                p.setPropertyList(list);
            }
            if (!m.containsKey(pn)) {
                ArrayList sb = new ArrayList();
                sb.add(p);
                m.put(pn, sb);
            } else {
                ArrayList ar = (ArrayList) m.get(pn);
                ar.add(p);
            }
        }
        return m;
    }

    @Override
    public GridDataInfo lableRightGrid(int begion, String strWhere) {
        return labelInstanceDaoImpl.lableRightGrid(begion,strWhere);
    }
    public int cancelLableById(String labelInstanceId){
        return labelInstanceDaoImpl.cancelLableById(labelInstanceId);
    }
    
    @Override
    public GridDataInfo lableCancel(int begion, String strWhere) {
        return labelInstanceDaoImpl.lableCancel(begion,strWhere);
    }

	public int saveLabel(OrderAskInfo order, ServiceContent content, OrderCustomerInfo cust, LabelInstance[] ls) {
		if (ls == null || ls.length == 0)
			return 0;
		for (int i = 0; i < ls.length; i++) {
			ls[i].setInstanceId(order.getServOrderId());
			ls[i].setStaffId(String.valueOf(order.getAskStaffId()));
			ls[i].setRegionId(order.getRegionId());
			ls[i].setServiceType(order.getServType());
			ls[i].setComeCategory(order.getComeCategory());
			ls[i].setAcceptComeFrom(order.getAskSource());
			ls[i].setAcceptChannelId(order.getAskChannelId());
			ls[i].setServiceOrderId(order.getServOrderId());
		}
		return saveLabelInstance(ls);
	}

	/**
	 * 保存标签实例
	 */
	@Override
	public int saveLabelInstance(LabelInstance[] ls) {
		return labelInstanceDaoImpl.saveLabelInstanceBatch(ls);
	}
	
	/**
	 * 如果处理内容中有关键字,更新标签表 UPDATE CC_SERVICE_LABEL L SET L.UNUSUAL_FLAG = 1 WHERE L.SERVICE_ORDER_ID = ?
	 * @param content
	 * @param orderId
	 */
	public void checkUnusualName(String content,String orderId) {
		//1:查出配置的判断字
		List ls = pubFunc.loadColumnsByEntity("CC_SERVICE_LABEL","UNUSUAL_FLAG",null);
		int unusualFlag = 0;
		for(int i=0;i<ls.size();i++) {
			Map m = (Map)ls.get(i);
			String columnValueName = m.get("COL_VALUE_NAME").toString();
			if(content.contains(columnValueName)) {
				unusualFlag = 1;
			}
		}
		
		//2:如果处理内容里包含了关键字就更新标签表
		if (unusualFlag == 1) {
			labelManageService.modifyUnusualFlag(orderId);
		}
	}

    public Map queryLabelHandByUrl(String url) {
        //所有切入点
        TsmStaff staff = pubFunc.getLogonStaff(); 
       
        List<LabelPoint> ls = labelTemplateDaoImpl.queryLabelInsertPoint(url,LabelLibStaticData.LABELPOINT_HAND,staff.getOrganizationId());
        if(!ls.isEmpty()){
            return changeMap(ls);
        }
        return null;
    }
    
	/**
	 * 得到工单标签数量
	 * 
	 * @param orderId 服务单号
	 * @return
	 */
	public int getLabelCountByOrderId(String orderId) {
		return labelInstanceDaoImpl.getLabelCountByOrderId(orderId);
	}

	/**
	 * 得到工单标签列表
	 * 
	 * @param orderId 服务单号
	 * @return
	 */
	public List getLabelListByOrderId(String orderId) {
		return labelInstanceDaoImpl.getLabelListByOrderId(orderId);
	}

}