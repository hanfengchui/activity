/**
 * @author 万荣伟
 */
package com.timesontransfar.sheetCheck.service.impl;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.message.pojo.MessagePrompt;
import com.timesontransfar.customservice.common.message.service.IMessageManager;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.sheetCheck.dao.IsheetCheckAdjuDao;
import com.timesontransfar.sheetCheck.dao.IsheetCheckDao;
import com.timesontransfar.sheetCheck.dao.IsheetCheckSchemDao;
import com.timesontransfar.sheetCheck.pojo.QualityContentSave;
import com.timesontransfar.sheetCheck.pojo.QualitySheet;
import com.timesontransfar.sheetCheck.pojo.SheetCheckAdju;
import com.timesontransfar.sheetCheck.pojo.SheetCheckAppeal;
import com.timesontransfar.sheetCheck.pojo.SheetCheckInfo;
import com.timesontransfar.sheetCheck.pojo.SheetCheckObj;
import com.timesontransfar.sheetCheck.pojo.SheetCheckSchem;
import com.timesontransfar.sheetCheck.pojo.SheetCheckState;
import com.timesontransfar.sheetCheck.service.IsheetCheckServer;
import com.transfar.common.utils.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * @author 
 *
 */
@Component(value="sheetCheckServer")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class SheetCheckServerImpl implements IsheetCheckServer {
	
	private static final Logger log = LoggerFactory.getLogger(SheetCheckServerImpl.class);
	@Autowired
	private IsheetCheckSchemDao sheetCheckSchemDao;
	@Autowired
	private IsheetCheckDao sheetCheckDao;
	@Autowired
	private IsheetCheckAdjuDao sheetCheckAdju;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IMessageManager messageManager;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	@Autowired
    private ISystemAuthorization systemAuthorization;
	
	private static final String QUALITY_SHEET_OBJ_ID = "900018403";
	private static final String CC_QUALITY_SHEET = "CC_QUALITY_SHEET";
	private static final String ORDER_ID = "orderId";
	private static final String SHEET_ID = "sheetId";
	private static final String DEAL_STAFF_IDS = "dealStaffIds";
	
	/**
	 * 保存质检模板
	 * @param bean
	 * @return
	 */
	public String saveCheckSchem(SheetCheckSchem bean) {
		if(bean==null) {
			if(log.isDebugEnabled()) {
				log.debug("工单质检对象为空");
			}
			return "OBJNULL";
		}
		String schemId = this.sheetCheckDao.getCheckId();
		bean.setSchemId(schemId);
		int size = this.sheetCheckSchemDao.saveCheckSchem(bean);
		if(size > 0) {
			return "SUCCESS";
		}
		return "ERROR";
	}
	
	/**
	 * 更新模板
	 * @param bean
	 * @return
	 */
	public String updateCheckSchem(SheetCheckSchem bean) {
		if(bean==null) {
			if(log.isDebugEnabled()) {
				log.debug("工单质检对象为空");
			}
			return "OBJNULL";
		}
		int size = this.sheetCheckSchemDao.updateCheckSchem(bean);
		if(size > 0) {
			return "SUCCESS";
		}
		return "ERROR";		
	}

	/**
	 * 保存工单质检
	 * @param bean 质检对象
	 * @param beanObj 质检评判对象
	 * @return
	 */
	public String saveSheetCheetObj(SheetCheckInfo bean,SheetCheckAdju[] beanObj) {
		if(bean==null ) {
			if(log.isDebugEnabled()) {
				log.debug("工单质检对象为空");
			}
			return "OBJNULL";
		}
		if(beanObj==null) {
			if(log.isDebugEnabled()) {
				log.debug("工单质检对象为空");
			}
			return "OBJNULL";
		}		
		// 质检员工
		TsmStaff tsm = this.pubFunc.getLogonStaff();
		String checkId = this.sheetCheckDao.getCheckId();
		bean.setCheckId(checkId);
		bean.setOrgId(tsm.getOrganizationId());
		bean.setOrgName(tsm.getOrgName());
		bean.setStaffId(Integer.parseInt(tsm.getId()));
		bean.setStaffName(tsm.getName());
		
		bean.setCheckState(StaticData.SHEET_CHECK_STATE_APPEAL );//质检申诉
		bean.setCheckStateName(this.pubFunc.getStaticName(StaticData.SHEET_CHECK_STATE_APPEAL));//质检申诉
				
		MessagePrompt p = new MessagePrompt();
		p.setMsgContent("服务单号:" + bean.getServiceOrderId() + "\n工单号:"+ bean.getWorkSheetId());	
		p.setTypeId(StaticData.MESSAGE_PROMPT_QUALITY);
		p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_QUALITY));
		p.setStaffId(bean.getCheckStaffId());
		p.setStaffName(bean.getCheckStaffName());
		p.setOrgId(bean.getCheckOrgId());
		p.setOrgName(bean.getCheckOrgName());				
		messageManager.createMsgPrompt(p);
		
		int size = this.sheetCheckDao.saveSheetCheck(bean);
		if(size > 0) {
			size = this.sheetCheckAdju.saveSheetCheckAdjuDao(beanObj, checkId);
			if(size > 0) {
				return "SUCCESS";
			} else {
				return "ERROR";
			}
		}		
		return "ERROR";
	}
	
	/**
	 * 申诉修改 被质检人员对质检单进行申诉后进行修改的动作,称之为申诉修改, 对已经申诉修改质检单状态修改为完成
	 * @param bean 质检对象
	 * @param beanObj 质检评判对象
	 * @return
	 */
	public String updateSheetCheetObj(SheetCheckInfo bean, SheetCheckAdju[] beanObj) {
		if(bean==null || bean.getCheckId()==null || bean.getCheckId().trim().equals("")) { 
			log.error("工单质检对象为空");
			return "OBJNULL";
		}
		if(beanObj==null) {
			log.error("工单质检对象为空");
			return "OBJNULL";
		}
		String checkId = bean.getCheckId(); 
		
		List hisList = sheetCheckDao.getSheetCheckHisList(checkId); 
		if(hisList!=null){
			int hisSize = hisList.size();
			for(int i=0;i<hisSize;i++){
				SheetCheckInfo hisBean = (SheetCheckInfo) hisList.get(i);
				if(hisBean.getCheckState() == StaticData.SHEET_CHECK_STATE_FINISH){//如果质检完成
					hisList.clear();
					hisList = null;
					return "CKSHEET_UPDATED";
				}
			}
			hisList.clear();
			hisList = null;
		} 
		
		SheetCheckInfo oldBean = sheetCheckDao.getSheetCheck( checkId);//获取原对象
		
		if(oldBean.getCheckState()!= StaticData.SHEET_CHECK_STATE_UPDATE ){//如果申诉修改的情况下 则返回
			return "CKSHEET_UPDATE_FAILED";
		}
		
		int edtion  = oldBean.getCheckEdition()+1; //版本号加1
		
		//原质检信息移入历史表
		sheetCheckDao.moveCheckSheetToHis(checkId);
		this.sheetCheckAdju.moveSheetCheckAdjuToHis(checkId);
		sheetCheckDao.deleteCheckSheet(checkId);
		this.sheetCheckAdju.deleteSheetCheckAdju(checkId);
		
		//质检员工
		TsmStaff tsm = this.pubFunc.getLogonStaff();   
		bean.setOrgId(tsm.getOrganizationId());
		bean.setOrgName(tsm.getOrgName());
		bean.setStaffId(Integer.parseInt(tsm.getId()));
		bean.setStaffName(tsm.getName());
		
		bean.setCheckOrgName(oldBean.getCheckOrgName() );
		bean.setCheckOrgId(oldBean.getCheckOrgId());
		bean.setCheckStaffId(oldBean.getCheckStaffId() );
		bean.setCheckStaffName( oldBean.getCheckStaffName());
		bean.setServiceOrderId(oldBean.getServiceOrderId() );
		bean.setWorkSheetId( oldBean.getWorkSheetId());
		bean.setTypeId( oldBean.getTypeId());
		bean.setTypeName( oldBean.getTypeName());
		
		bean.setCheckEdition(edtion);
		
		bean.setCheckState(StaticData.SHEET_CHECK_STATE_FINISH );//质检完成
		bean.setCheckStateName( this.pubFunc.getStaticName( StaticData.SHEET_CHECK_STATE_FINISH));//质检完成
		
		int size = sheetCheckDao.saveSheetCheck(bean);
		
		for(int i=0;i<beanObj.length;i++){
			beanObj[i].setCheckEdition( edtion);//添加版本
		}
		if(size > 0) {
			size = this.sheetCheckAdju.saveSheetCheckAdjuDao(beanObj, checkId);
			if(size > 0) {
				return "SUCCESS";
			}
		}
		return "ERROR";
	}
	
	/**
	 * 质检修改 对质检第一次质检的并还没有进行申诉确认单子 可以进行修改,这个修改修改操作称之为 质检修改
	 * @param bean 质检对象
	 * @param beanObj 质检评判对象
	 * @return
	 */
	public String updateSheetCheetForResave(SheetCheckInfo bean,SheetCheckAdju[] beanObj) {
		if(bean==null || bean.getCheckId()==null || bean.getCheckId().trim().equals("")) { 
			if(log.isDebugEnabled()) {
				log.debug("工单质检对象为空");
			}
			return "OBJNULL"; 
		}
		if(beanObj==null) {
			if(log.isDebugEnabled()) {
				log.debug("工单质检对象为空");
			}
			return "OBJNULL";
		}
		String checkId = bean.getCheckId();  
		SheetCheckInfo oldBean = sheetCheckDao.getSheetCheck( checkId);//获取原对象
		if(oldBean.getCheckState()!= StaticData.SHEET_CHECK_STATE_APPEAL ){//如果当前状态不为质检申诉状态则返回不可进行质检修改
			return "UPDATED_FAILED";
		}
		
		oldBean.setCheckState( StaticData.SHEET_CHECK_STATE_RESAVE);
		oldBean.setCheckStateName(this.pubFunc.getStaticName( StaticData.SHEET_CHECK_STATE_RESAVE  ));//修改状态为质检修改 
		sheetCheckDao.updateSheetCheckInfo(oldBean);
		
		//原质检信息移入历史表
		sheetCheckDao.moveCheckSheetToHis(checkId);
		this.sheetCheckAdju.moveSheetCheckAdjuToHis(checkId); 
		sheetCheckDao.deleteCheckSheet(checkId);
		this.sheetCheckAdju.deleteSheetCheckAdju(checkId);
		
		int edtion  = oldBean.getCheckEdition()+1; //版本号加1
		//质检员工
		TsmStaff tsm = this.pubFunc.getLogonStaff();   
		bean.setOrgId(tsm.getOrganizationId());
		bean.setOrgName(tsm.getOrgName());
		bean.setStaffId(Integer.parseInt(tsm.getId()));
		bean.setStaffName(tsm.getName());
		
		bean.setCheckOrgName(oldBean.getCheckOrgName() );
		bean.setCheckOrgId(oldBean.getCheckOrgId());
		bean.setCheckStaffId(oldBean.getCheckStaffId() );
		bean.setCheckStaffName( oldBean.getCheckStaffName());
		bean.setServiceOrderId(oldBean.getServiceOrderId() );
		bean.setWorkSheetId( oldBean.getWorkSheetId());
		bean.setTypeId( oldBean.getTypeId());
		bean.setTypeName( oldBean.getTypeName());
		
		bean.setCheckEdition(edtion);
		
		bean.setCheckState(StaticData.SHEET_CHECK_STATE_APPEAL );//质检申诉状态
		bean.setCheckStateName( this.pubFunc.getStaticName( StaticData.SHEET_CHECK_STATE_APPEAL));//质检申诉状态
		
		int size = sheetCheckDao.saveSheetCheck(bean);
		
		for(int i=0;i<beanObj.length;i++){
			beanObj[i].setCheckEdition( edtion);//添加版本
		}
		if(size > 0) {
			size = this.sheetCheckAdju.saveSheetCheckAdjuDao(beanObj, checkId);
			if(size > 0) {
				return "SUCCESS";
			}
		}
		return "ERROR";
	}
	
	/**
	 * 保存申诉回复内容
	 * @param sheetCheckInfo
	 * @return
	 */
	public String submitCheckReply(SheetCheckInfo sheetCheckInfo){
		String checkId = sheetCheckInfo.getCheckId();
		
		/** //获取原对象**/
		SheetCheckInfo oldBean = sheetCheckDao.getSheetCheck( checkId); 
		
		
		if(oldBean.getCheckState()==StaticData.SHEET_CHECK_STATE_FINISH){  
			
			oldBean = null; 
			sheetCheckInfo = null; 
			/** 质检单已经完成不能回复**/
			return "ERROR";
		} 
		
		oldBean.setAppealReply(sheetCheckInfo.getAppealReply() );
		oldBean.setCheckState(StaticData.SHEET_CHECK_STATE_FINISH );//质检完成
		oldBean.setCheckStateName( this.pubFunc.getStaticName( StaticData.SHEET_CHECK_STATE_FINISH));//质检完成 
		
		int size =  sheetCheckDao.updateSheetCheckInfo(oldBean);
		
		//清空
		oldBean = null; 
		sheetCheckInfo = null;
		
		if(size > 0) {
			return "SUCCESS";
		} else {
			return "ERROR";
		}
	}
	
	/**
	 * 得到工单质检记录和评判标准
	 * @param checkId 质检ID
	 * @return
	 */
	public SheetCheckObj getSheetCheckObj(String checkId) {
		if(checkId == null || checkId.equals("")) {
			return null;
		}
		SheetCheckInfo sheetCheckInfo = sheetCheckDao.getSheetCheck(checkId);
		SheetCheckAdju[] bean = this.sheetCheckAdju.getSheetCheckAdju(checkId);
		SheetCheckObj obj = new SheetCheckObj();
		obj.setBean(bean);
		obj.setSheetCheckInfo(sheetCheckInfo);
		return obj;
	}
	
	/**
	 * 得到工单质检记录和评判标准历史
	 * @param checkId 质检ID
	 * @return
	 */
	public SheetCheckObj getSheetCheckObjHis(String checkId) {
		if(checkId == null || checkId.equals("")) {
			return null;
		}
		SheetCheckInfo sheetCheckInfo = null;
		List hisList = sheetCheckDao.getSheetCheckHisList(checkId); 
		SheetCheckObj obj = null;
		if(null != hisList){
			int hisSize = hisList.size();
			for(int i=0;i<hisSize;i++){
				SheetCheckInfo hisBean = (SheetCheckInfo) hisList.get(i);
				if(hisBean.getCheckState() == StaticData.SHEET_CHECK_STATE_FINISH){//如果质检完成
					sheetCheckInfo = hisBean;
				}
			}
			if(sheetCheckInfo == null) {
				return null;
			}
			SheetCheckAdju[] bean = this.sheetCheckAdju.getSheetCheckAdjuHis(checkId, sheetCheckInfo.getCheckEdition());
		    obj = new SheetCheckObj();
			obj.setBean(bean);
			obj.setSheetCheckInfo(sheetCheckInfo);
		}
		return obj;
	}
	
    public List getSheetCheckObjHisList(String checkId) {
        if(checkId == null || checkId.equals("")) {
            return Collections.emptyList();
        }
        List hisList = sheetCheckDao.getSheetCheckHisList(checkId); 
        List objs = new ArrayList();
        if(null !=hisList){
            int hisSize = hisList.size();
            SheetCheckInfo hisBean = null;
            SheetCheckObj obj = null;
            SheetCheckAdju[] adju = null;
            for(int i = 0; i < hisSize; i++){
                hisBean = (SheetCheckInfo) hisList.get(i);
                obj = new SheetCheckObj();
                adju = this.sheetCheckAdju.getSheetCheckAdjuHis(hisBean.getCheckId(), hisBean.getCheckEdition());
                obj.setBean(adju);
                obj.setSheetCheckInfo(hisBean);
                objs.add(obj);
            }  
        }
        return objs;
    }
    
	/**
	 * 根据质检类型得到质检模板
	 * @param typeId
	 * @return
	 */
	public List getSheetCheckSchem(int typeId) {
		return this.sheetCheckSchemDao.getSheetCheckSchem(typeId);
	}
	
	/**
	 * 质检申诉
	 * @return
	 */
	public String doAppealCheckSheet(SheetCheckAppeal appealBean){
		String sysdate = this.pubFunc.getSysDate();
		TsmStaff staff = this.pubFunc.getLogonStaff();
		//完善申诉信息
		appealBean.setAppealData(sysdate);
		appealBean.setAppealStaffId(Integer.parseInt(staff.getId()));
		appealBean.setAppealStaffName(staff.getName());
		appealBean.setAppealOrgId(staff.getOrganizationId());
		appealBean.setAppealOrgName(staff.getOrgName());  
		
		//定单质检单状态信息
		SheetCheckState checkState = new SheetCheckState();
		checkState.setCheckId(appealBean.getCheckId()); 
		checkState.setCheckState(StaticData.SHEET_CHECK_STATE_UPDATE );
		checkState.setCheckStateName(this.pubFunc.getStaticName( StaticData.SHEET_CHECK_STATE_UPDATE));
		sheetCheckDao.saveAppealContent(appealBean);//保存申诉内容
		sheetCheckDao.saveCheckSheetState(checkState);  //保存质检单状态
		
		/* 将被质检员工的申诉信息，推送给质检员 */
		SheetCheckInfo checkInfo = sheetCheckDao.getSheetCheck(appealBean.getCheckId());
		MessagePrompt p = new MessagePrompt();
		p.setMsgContent("质检单号:" + appealBean.getCheckId() + "\n申诉内容:"+ appealBean.getAppealContent());	
		p.setTypeId(StaticData.MESSAGE_PROMPT_QUALITY_VERIFY);
		p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_QUALITY_VERIFY));
		p.setStaffId(checkInfo.getStaffId());
		p.setStaffName(checkInfo.getStaffName());
		p.setOrgId(checkInfo.getOrgId());
		p.setOrgName(checkInfo.getOrgName());
		messageManager.createMsgPrompt(p);
		return "SUCCESS";
	}
	
	/**
	 * 检查可否质检
	 * @param orderId 定单id
	 * @param sheetId 工单id
	 * @param checkType 质检类型
	 * @return
	 */
	public String validateCheckable(String orderId,String sheetId,int checkType ){
		
		if(checkType == 2000001 ){//受理单质检
			List list  = sheetCheckDao.queryCheckBeansByOrdid(orderId);
			Iterator it = list.iterator();
			while(it.hasNext()){
				SheetCheckInfo checkInfo = (SheetCheckInfo) it.next();
				if(checkInfo.getTypeId()== checkType ){
					return "CHECKDISABLE";
				}
			}
		}else{
			List list  = sheetCheckDao.queryCheckBeansBySheetid(sheetId);
			Iterator it = list.iterator();
			while(it.hasNext()){
				SheetCheckInfo checkInfo = (SheetCheckInfo) it.next();
				if(checkInfo.getTypeId()== checkType ){
					return "CHECKDISABLE";
				}
			}
		}
		
		return "CHECKABLE";
	}
	/**
	 * 提交确认--质检确认
	 * @param checkId
	 * @return
	 */
	public String submitAffirmance(String checkId){
		SheetCheckState  checkState = new SheetCheckState();
		checkState.setCheckId(checkId ); 
		checkState.setCheckState(StaticData.SHEET_CHECK_STATE_FINISH );
		checkState.setCheckStateName( this.pubFunc.getStaticName( StaticData.SHEET_CHECK_STATE_FINISH));
		sheetCheckDao.saveCheckSheetState(checkState);
		return "SUCCESS";
	}
	
	/**
	 * 工单质检分派列表
	 */
	public GridDataInfo getQualitySheetList(String parm){
    	JSONObject json = JSONObject.fromObject(parm);
    	int begion = json.optInt("begion");
    	int pageSize = json.optInt("pageSize");
    	String strWhere = this.getQualitySheetListWhere(json.optString("strWhere"));
    	
        String sql = "select W.WORK_SHEET_ID,W.SERVICE_ORDER_ID,W.SERVICE_TYPE_DESC,W.REGION_NAME,S.PROD_NUM,DATE_FORMAT(S.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE," +
        		"DATE_FORMAT(S.FINISH_DATE,'%Y-%m-%d %H:%i:%s') FINISH_DATE,DATE_FORMAT(W.RESPOND_DATE,'%Y-%m-%d %H:%i:%s') RESPOND_DATE," +
        		"W.TACHE_DESC,W.DEAL_ORG_ID,W.DEAL_ORG_NAME,W.DEAL_STAFF,W.DEAL_STAFF_NAME,S.ACCEPT_CHANNEL_DESC,S.COMMENTS," + 
        		"(SELECT Q.TS_REASON_NAME FROM cc_sheet_qualitative_his Q WHERE Q.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID ORDER BY Q.CREAT_DATA DESC LIMIT 1) AS N " + 
        		"from cc_work_sheet_his w,cc_service_order_ask_his s,cc_service_content_ask_his a " + 
        		"where 1=1 " + 
        		"and w.service_order_id = s.service_order_id " + 
        		"and s.service_order_id = a.service_order_id " + 
        		"and s.ORDER_VESION = a.ORDER_VESION " + 
        		"and w.precontract_flag = 0 " + 
        		"and s.order_statu in (700000103, 3000047, 720130002, 720130010)" + 
        		"and w.sheet_type not in (700001002, 720130015)";
		Map map = new HashMap();
		map.put("CC_WORK_SHEET_HIS", "W");
		sql = systemAuthorization.getAuthedSql(map, sql, "900018404");//工单质检分派实体
        return dbgridDataPub.getResultBySize(sql + strWhere, begion, pageSize, " order by w.creat_date", "");
    }
    
	private String getQualitySheetListWhere(String map){
		JSONObject obj = JSONObject.fromObject(map);
		String strwhere = "";
		if(StringUtils.isNotEmpty(obj.optString(ORDER_ID))){
			strwhere += " AND S.SERVICE_ORDER_ID='"+obj.optString(ORDER_ID)+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString(SHEET_ID))){
			strwhere += " AND W.WORK_SHEET_ID='"+obj.optString(SHEET_ID)+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND S.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("regionId"))){
			strwhere += " AND S.REGION_ID="+obj.optString("regionId");
		}
		if(StringUtils.isNotEmpty(obj.optString("tacheId"))){
			strwhere += " AND W.TACHE_ID="+obj.optString("tacheId");
		}
		if(StringUtils.isNotEmpty(obj.optString("trSubFrom"))){
			strwhere += " AND S.ACCEPT_COME_FROM="+obj.optString("trSubFrom");
		}
		if(StringUtils.isNotEmpty(obj.optString("trChannel"))){
			strwhere += " AND S.ACCEPT_CHANNEL_ID="+obj.optString("trChannel");
		}
		if(StringUtils.isNotEmpty(obj.optString("dealOrgIds"))){
			strwhere += " AND W.DEAL_ORG_ID in ("+obj.optString("dealOrgIds")+")";
		}
		if(StringUtils.isNotEmpty(obj.optString(DEAL_STAFF_IDS))){
			strwhere += " AND W.DEAL_STAFF in ("+obj.optString(DEAL_STAFF_IDS)+")";
		}
		if(StringUtils.isNotEmpty(obj.optString("appealProd"))){
			strwhere += " AND A.APPEAL_PROD_ID="+obj.optString("appealProd");
		}
		if(StringUtils.isNotEmpty(obj.optString("trReasonId"))){
			strwhere += " AND A.APPEAL_REASON_ID="+obj.optString("trReasonId");
		}
		if(StringUtils.isNotEmpty(obj.optString("prodOne"))){
			strwhere += " AND A.PROD_ONE="+obj.optString("prodOne");
		}
		if(StringUtils.isNotEmpty(obj.optString("prodTwo"))){
			strwhere += " AND A.PROD_TWO="+obj.optString("prodTwo");
		}
		if(StringUtils.isNotEmpty(obj.optString("keyword"))){
			strwhere += " AND (A.ACCEPT_CONTENT like '%"+obj.optString("keyword")+"%' or W.DEAL_CONTENT like '%"+obj.optString("keyword")+"%')";
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere += " AND W.SERVICE_TYPE = "+obj.optString("servType");
		}
		if(StringUtils.isEmpty(obj.optString(ORDER_ID)) && StringUtils.isEmpty(obj.optString(SHEET_ID))){
			if(StringUtils.isNotNull(obj.optJSONArray("acceptDate")) && !obj.optJSONArray("acceptDate").isEmpty()){
				strwhere += " AND S.ACCEPT_DATE > STR_TO_DATE('"+obj.optJSONArray("acceptDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
				strwhere += " AND S.ACCEPT_DATE < STR_TO_DATE('"+obj.optJSONArray("acceptDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
			}
			if(StringUtils.isNotNull(obj.optJSONArray("dealDate")) && !obj.optJSONArray("dealDate").isEmpty()){
				strwhere += " AND W.CREAT_DATE > STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
				strwhere += " AND W.CREAT_DATE < STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
			}
		}
		return strwhere;
	}
	
	public List getTemplateList(int tacheId) {
		return this.sheetCheckSchemDao.getTemplateList(tacheId);
	}
	
	public int allotQualitySheet(String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String staffIds = json.optString("receiveStaffIds");
    	String[] staffId = staffIds.split(",");
    	
    	JSONArray sheetArray = json.optJSONArray("sheetArray");
    	int amount = sheetArray.size();
    	int staffSize = staffId.length;
    	int dspnum = amount%staffSize == 0 ? amount/staffSize : amount/staffSize+1;

    	TsmStaff returnStaff = pubFunc.getLogonStaff();
    	int num = 0;
		for(int i=0;i<dspnum;i++) {
    		for(int j=0;j<staffSize;j++) {
				if(i*staffSize+j+1 > amount) {
					break;
				}
				String sheetInfo = sheetArray.get(i*staffSize+j).toString();
				QualitySheet sheet = new QualitySheet();
				sheet.setSheetId(sheetInfo.split("@")[0]);
				sheet.setOrderId(sheetInfo.split("@")[1]);
				sheet.setServType(json.optInt("servType"));
				sheet.setServTypeDesc(json.optString("servTypeDesc"));
				sheet.setTacheId(json.optInt("tacheId"));
				sheet.setTacheDesc(json.optString("tacheDesc"));
				sheet.setTemplateId(json.optString("templateId"));
				sheet.setReturnOrdId(returnStaff.getOrganizationId());
				sheet.setReturnOrgName(returnStaff.getOrgName());
				sheet.setReturnStaff(Integer.parseInt(returnStaff.getId()));
				sheet.setReturnStaffName(returnStaff.getName());
				sheet.setChecklimit(json.optInt("limit"));
				sheet.setCheckStaff(Integer.parseInt(staffId[j]));
				sheet.setCheckStatus(0);
				sheet.setDealStaff(Integer.parseInt(sheetInfo.split("@")[2]));
				sheet.setDealStaffName(sheetInfo.split("@")[3]);
				sheet.setDealOrgId(sheetInfo.split("@")[4]);
				sheet.setDealOrgName(sheetInfo.split("@")[5]);
				num += sheetCheckSchemDao.saveQualitySheet(sheet);
    		}
    	}
		return num;
	}
	
	/**
	 * 工单质检已派发列表
	 */
	public GridDataInfo getQualityReturnList(String parm){
    	JSONObject json = JSONObject.fromObject(parm);
    	int begion = json.optInt("begion");
    	int pageSize = json.optInt("pageSize");
    	String strWhere = this.getCheckSheetListWhere(json.optString("strWhere"));
    	
    	String sql = "SELECT S.WORK_SHEET_ID,S.SERVICE_ORDER_ID,S.SERVICE_TYPE_DESC,S.TACHE_DESC,"
    			+ "DATE_FORMAT(S.ALLTO_DATE,'%Y-%m-%d %H:%i:%s') ALLTO_DATE,S.RETURN_ORG_NAME,S.RETURN_STAFF_NAME,"
    			+ "DATE_FORMAT(DATE_ADD(S.ALLTO_DATE,INTERVAL (S.CHECK_LIMIT/24)*24*60*60 SECOND),'%Y-%m-%d %H:%i:%s')LIMIT_DATE,"
    			+ "TIMESTAMPDIFF(SECOND, NOW(), DATE_ADD(S.ALLTO_DATE,INTERVAL S.CHECK_LIMIT*3600 SECOND))/3600 LIMIT_TIME,S.CHECK_ORG_NAME,S.CHECK_STAFF_NAME,S.CHECK_STATUS "
    			+ "FROM CC_QUALITY_SHEET S WHERE 1=1" + strWhere;
        Map map = new HashMap();
		map.put(CC_QUALITY_SHEET, "S");
        sql = systemAuthorization.getAuthedSql(map, sql, "900018405");//工单质检已派发实体
        return dbgridDataPub.getResultBySize(sql, begion, pageSize, " ORDER BY S.ALLTO_DATE", "");
    }
	
	/**
	 * 工单质检列表
	 */
	public GridDataInfo getCheckSheetList(String parm){
    	JSONObject json = JSONObject.fromObject(parm);
    	int begion = json.optInt("begion");
    	int pageSize = json.optInt("pageSize");
    	String strWhere = this.getCheckSheetListWhere(json.optString("strWhere"));
    	
        String sql = "SELECT S.WORK_SHEET_ID,S.SERVICE_ORDER_ID,S.SERVICE_TYPE,S.SERVICE_TYPE_DESC,S.TACHE_DESC,S.TEMPLATE_ID," + 
        		"DATE_FORMAT(S.ALLTO_DATE,'%Y-%m-%d %H:%i:%s') ALLTO_DATE,S.RETURN_ORG_NAME,S.RETURN_STAFF_NAME," +
        		"DATE_FORMAT(DATE_ADD(S.ALLTO_DATE,INTERVAL (S.CHECK_LIMIT/24)*24*60*60 SECOND),'%Y-%m-%d %H:%i:%s')LIMIT_DATE," +
        		"TIMESTAMPDIFF(SECOND, NOW(), DATE_ADD(S.ALLTO_DATE,INTERVAL S.CHECK_LIMIT*3600 SECOND))/3600 LIMIT_TIME " +
        		"FROM CC_QUALITY_SHEET S WHERE 1=1 AND S.CHECK_STATUS=0" + strWhere;
        Map map = new HashMap();
		map.put(CC_QUALITY_SHEET, "S");
        sql = systemAuthorization.getAuthedSql(map, sql, QUALITY_SHEET_OBJ_ID);//工单质检实体
        return dbgridDataPub.getResultBySize(sql, begion, pageSize, " ORDER BY LIMIT_DATE", QUALITY_SHEET_OBJ_ID);
    }
    
	private String getCheckSheetListWhere(String map){
		JSONObject obj = JSONObject.fromObject(map);
		String strwhere = "";
		if(StringUtils.isEmpty(obj.optString(ORDER_ID)) && StringUtils.isEmpty(obj.optString(SHEET_ID))){
			if(StringUtils.isNotNull(obj.optJSONArray("alltoDate")) && !obj.optJSONArray("alltoDate").isEmpty()){
				strwhere += " AND S.ALLTO_DATE > STR_TO_DATE('"+obj.optJSONArray("alltoDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
				strwhere += " AND S.ALLTO_DATE < STR_TO_DATE('"+obj.optJSONArray("alltoDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
			}
			if(StringUtils.isNotNull(obj.optJSONArray("checkDate")) && !obj.optJSONArray("checkDate").isEmpty()){
				strwhere += " AND S.CHECK_DATE > STR_TO_DATE('"+obj.optJSONArray("checkDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
				strwhere += " AND S.CHECK_DATE < STR_TO_DATE('"+obj.optJSONArray("checkDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
			}
			if(StringUtils.isNotNull(obj.optJSONArray("appealDate")) && !obj.optJSONArray("appealDate").isEmpty()){
				strwhere += " AND S.APPEAL_DATE > STR_TO_DATE('"+obj.optJSONArray("appealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
				strwhere += " AND S.APPEAL_DATE < STR_TO_DATE('"+obj.optJSONArray("appealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
			}
		}
		return this.getCheckSheetListWhere(obj, strwhere);
	}
	
	private String getCheckSheetListWhere(JSONObject obj, String strwhere) {
		if(StringUtils.isNotEmpty(obj.optString(ORDER_ID))){
			strwhere += " AND S.SERVICE_ORDER_ID='"+obj.optString(ORDER_ID)+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString(SHEET_ID))){
			strwhere += " AND S.WORK_SHEET_ID='"+obj.optString(SHEET_ID)+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("tacheId"))){
			strwhere += " AND S.TACHE_ID="+obj.optString("tacheId");
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere += " AND S.SERVICE_TYPE = "+obj.optString("servType");
		}
		if(StringUtils.isNotEmpty(obj.optString("checkStatus"))){
			strwhere += " AND S.CHECK_STATUS="+obj.optString("checkStatus");
		}
		if(StringUtils.isNotEmpty(obj.optString(DEAL_STAFF_IDS))){
			strwhere += " AND S.DEAL_STAFF in ("+obj.optString(DEAL_STAFF_IDS)+")";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkStaffIds"))){
			strwhere += " AND S.CHECK_STAFF in ("+obj.optString("checkStaffIds")+")";
		}
		return strwhere;
	}
	
	public String getTemplateInfo(String templateId) {
		List rowList = sheetCheckSchemDao.getRowListByTemplateId(templateId);
		if(rowList.isEmpty()) {
			return null;
		}
		
		JsonArray arr = new JsonArray();
		for(int i=0; i<rowList.size(); i++) {
			Map rowMap = (Map) rowList.get(i);
    		String rowId = rowMap.get("ROW_ID").toString();
    		
    		List eleList = sheetCheckSchemDao.getEleListByRowId(rowId);
    		JsonArray eleArr = new JsonArray();
    		for(int j=0; j<eleList.size(); j++) {
    			Map eleMap = (Map) eleList.get(j);
        		JsonObject eleObj = new JsonObject();
        		eleObj.addProperty("eleId", eleMap.get("ELE_ID").toString());
        		eleObj.addProperty("eleName", eleMap.get("ELE_NAME").toString());
        		eleObj.addProperty("eleSort", Integer.parseInt(eleMap.get("ELE_SORT").toString()));
        		eleObj.addProperty("value", true);
        		eleObj.addProperty("text", "");
        		eleArr.add(eleObj);
    		}
    		
    		JsonObject rowObj = new JsonObject();
    		rowObj.addProperty("rowId", rowId);
    		rowObj.addProperty("rowName", rowMap.get("ROW_NAME").toString());
    		rowObj.addProperty("rowSort", Integer.parseInt(rowMap.get("ROW_SORT").toString()));
    		rowObj.add("eleArr", eleArr);
    		arr.add(rowObj);
		}
		return new Gson().toJson(arr);
	}
	
	public int saveEleAnswer(String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String sheetId = json.optString(SHEET_ID);
    	String orderId = json.optString(ORDER_ID);
    	String templateId = json.optString("templateId");
    	JSONArray tempArr = json.optJSONArray("tempArr");
    	
    	int sNum = 0;
    	List<QualityContentSave> saveList = new ArrayList<>();
    	for(int i=0;i<tempArr.size();i++) {
    		JSONObject rowObj = (JSONObject)tempArr.get(i);
    		JSONArray eleArr = rowObj.optJSONArray("eleArr");
    		for(int j=0;j<eleArr.size();j++) {
    			JSONObject eleObj = (JSONObject)eleArr.get(j);
    			QualityContentSave save = new QualityContentSave();
    			save.setSheetId(sheetId);
    			save.setOrderId(orderId);
    			save.setTemplateId(templateId);
    			save.setRowId(rowObj.optString("rowId"));
    			save.setRowName(rowObj.optString("rowName"));
    			save.setEleId(eleObj.optString("eleId"));
    			save.setEleName(eleObj.optString("eleName"));
    			save.setEleAnswer(eleObj.optBoolean("value"));
    			save.setEleText(eleObj.optString("text"));
    			save.setRowSort(rowObj.optInt("rowSort"));
    			save.setEleSort(eleObj.optInt("eleSort"));
    			save.setStatus("1");
    			saveList.add(save);
    			if(save.isEleAnswer()) {
    				sNum++;
    			}
    		}
    	}
    	int saveNum = sheetCheckSchemDao.insertQualityContentSave(saveList);
    	int updateFlag = 0;
    	if(saveNum == saveList.size()) {
    		DecimalFormat df = new DecimalFormat("0.00");
        	String rateStr = df.format((float)sNum/saveList.size()*100);
        	
        	QualitySheet sheet = new QualitySheet();
        	sheet.setSheetId(sheetId);
        	sheet.setCheckStatus(1);
        	sheet.setApproveRate(Double.parseDouble(rateStr));
    		updateFlag = sheetCheckSchemDao.updateQualitySheet(sheet);
    	}
    	return updateFlag;
	}
	
	/**
	 * 工单质检申诉列表
	 */
	public GridDataInfo getCheckResultList(String parm){
    	JSONObject json = JSONObject.fromObject(parm);
    	int begion = json.optInt("begion");
    	int pageSize = json.optInt("pageSize");
    	String strWhere = this.getCheckSheetListWhere(json.optString("strWhere"));
    	
        String sql = "SELECT S.WORK_SHEET_ID,S.SERVICE_ORDER_ID,S.SERVICE_TYPE_DESC,S.TACHE_DESC,S.TEMPLATE_ID,S.CHECK_ORG_NAME," + 
        		"S.CHECK_STAFF_NAME,DATE_FORMAT(S.CHECK_DATE,'%Y-%m-%d %H:%i:%s') CHECK_DATE,S.CHECK_STATUS,CONCAT(S.APPROVE_RATE , '%') as APPROVE_RATE," +
        		"S.APPROVE_CONTENT " + 
        		"FROM CC_QUALITY_SHEET S WHERE 1=1 AND S.CHECK_STATUS !=0" + strWhere;
        Map map = new HashMap();
		map.put(CC_QUALITY_SHEET, "S");
        sql = systemAuthorization.getAuthedSql(map, sql, "900018406");//工单质检申诉实体
        return dbgridDataPub.getResultBySize(sql, begion, pageSize, " ORDER BY CHECK_DATE", "");
    }
	
	public String getTemplateResult(String sheetId,String templateId) {
		List rowList = sheetCheckSchemDao.getRowListByTemplateId(templateId);
		if(rowList.isEmpty()) {
			return null;
		}
		
		JsonArray arr = new JsonArray();
		for(int i=0; i<rowList.size(); i++) {
			Map rowMap = (Map) rowList.get(i);
    		String rowId = rowMap.get("ROW_ID").toString();
    		
    		List eleList = sheetCheckSchemDao.getEleAnswerListByRowId(sheetId, rowId);
    		JsonArray eleArr = new JsonArray();
    		for(int j=0; j<eleList.size(); j++) {
    			Map eleMap = (Map) eleList.get(j);
        		JsonObject eleObj = new JsonObject();
        		eleObj.addProperty("eleId", eleMap.get("ELE_ID").toString());
        		eleObj.addProperty("eleName", eleMap.get("ELE_NAME").toString());
        		eleObj.addProperty("eleSort", Integer.parseInt(eleMap.get("ELE_SORT").toString()));
        		eleObj.addProperty("value", "1".equals(eleMap.get("ELE_ANSWER").toString()));
        		eleObj.addProperty("text", eleMap.get("ELE_TEXT") == null ? "" : eleMap.get("ELE_TEXT").toString());
        		eleArr.add(eleObj);
    		}
    		
    		JsonObject rowObj = new JsonObject();
    		rowObj.addProperty("rowId", rowId);
    		rowObj.addProperty("rowName", rowMap.get("ROW_NAME").toString());
    		rowObj.addProperty("rowSort", Integer.parseInt(rowMap.get("ROW_SORT").toString()));
    		rowObj.add("eleArr", eleArr);
    		arr.add(rowObj);
		}
		return new Gson().toJson(arr);
	}
    
	public int saveQualityAppeal(String parm) {
		JSONObject json = JSONObject.fromObject(parm);
    	
		QualitySheet sheet = new QualitySheet();
		sheet.setSheetId(json.optString(SHEET_ID));
    	sheet.setAppealReason(json.optString("appealReason"));
    	sheet.setCheckStatus(2);
		return sheetCheckSchemDao.saveQualityAppeal(sheet);
	}
	
	//工单质检申诉审批列表
	public GridDataInfo getCheckAppealList(String parm){
    	JSONObject json = JSONObject.fromObject(parm);
    	int begion = json.optInt("begion");
    	int pageSize = json.optInt("pageSize");
    	String strWhere = this.getCheckSheetListWhere(json.optString("strWhere"));
    	
        String sql = "SELECT S.WORK_SHEET_ID,S.SERVICE_ORDER_ID,S.SERVICE_TYPE_DESC,S.TACHE_DESC,S.TEMPLATE_ID," + 
        		"S.DEAL_STAFF_NAME,DATE_FORMAT(S.APPEAL_DATE,'%Y-%m-%d %H:%i:%s') APPEAL_DATE,CONCAT(S.APPROVE_RATE , '%') as APPROVE_RATE," +
        		"S.APPEAL_REASON " +
        		"FROM CC_QUALITY_SHEET S WHERE 1=1 AND S.CHECK_STATUS=2" + strWhere;
        Map map = new HashMap();
		map.put(CC_QUALITY_SHEET, "S");
        sql = systemAuthorization.getAuthedSql(map, sql, QUALITY_SHEET_OBJ_ID);//工单质检实体
        return dbgridDataPub.getResultBySize(sql, begion, pageSize, " ORDER BY S.APPEAL_DATE", "");
    }
	
	public int updateQualityContent(String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String sheetId = json.optString(SHEET_ID);
    	String orderId = json.optString(ORDER_ID);
    	String templateId = json.optString("templateId");
    	boolean approveFlag = json.optBoolean("approveFlag");
    	String approveContent = json.optString("approveContent");
    	
    	int updateFlag = 0;
    	if(approveFlag) {//审批通过
    		int deleteFlag = sheetCheckSchemDao.deleteQualityContentSave(sheetId);//删除旧模板元素
    		if(deleteFlag > 0) {
    			int sNum = 0;
            	List<QualityContentSave> saveList = new ArrayList<>();
            	JSONArray tempArr = json.optJSONArray("tempArr");
            	for(int i=0;i<tempArr.size();i++) {
            		JSONObject rowObj = (JSONObject)tempArr.get(i);
            		JSONArray eleArr = rowObj.optJSONArray("eleArr");
            		for(int j=0;j<eleArr.size();j++) {
            			JSONObject eleObj = (JSONObject)eleArr.get(j);
            			QualityContentSave save = new QualityContentSave();
            			save.setSheetId(sheetId);
            			save.setOrderId(orderId);
            			save.setTemplateId(templateId);
            			save.setRowId(rowObj.optString("rowId"));
            			save.setRowName(rowObj.optString("rowName"));
            			save.setEleId(eleObj.optString("eleId"));
            			save.setEleName(eleObj.optString("eleName"));
            			save.setEleAnswer(eleObj.optBoolean("value"));
            			save.setEleText(eleObj.optString("text"));
            			save.setRowSort(rowObj.optInt("rowSort"));
            			save.setEleSort(eleObj.optInt("eleSort"));
            			save.setStatus("1");
            			saveList.add(save);
            			if(save.isEleAnswer()) {
            				sNum++;
            			}
            		}
            	}
            	int saveNum = sheetCheckSchemDao.insertQualityContentSave(saveList);//保存新模板元素
            	if(saveNum == saveList.size()) {
            		DecimalFormat df = new DecimalFormat("0.00");
                	String rateStr = df.format((float)sNum/saveList.size()*100);
                	
                	QualitySheet sheet = new QualitySheet();
                	sheet.setSheetId(sheetId);
                	sheet.setCheckStatus(3);
                	sheet.setApproveRate(Double.parseDouble(rateStr));
                	sheet.setApproveContent(approveContent);
            		updateFlag = sheetCheckSchemDao.saveQualityApprove(sheet);//更新质检结果表
            	}
    		}
    	}
    	else {//审批不通过
    		QualitySheet sheet = new QualitySheet();
        	sheet.setSheetId(sheetId);
        	sheet.setCheckStatus(4);
        	sheet.setApproveContent(approveContent);
    		updateFlag = sheetCheckSchemDao.saveQualityApprove(sheet);//更新质检结果表
    	}
    	return updateFlag;
	}
	
	
	/**
	 * 质检结果列表
	 */
	public GridDataInfo getQualityQueryList(String parm){
    	JSONObject json = JSONObject.fromObject(parm);
    	int begion = json.optInt("begion");
    	int pageSize = json.optInt("pageSize");
    	String strWhere = this.getCheckSheetListWhere(json.optString("strWhere"));
    	
        String sql = "SELECT S.WORK_SHEET_ID,S.SERVICE_ORDER_ID,S.SERVICE_TYPE_DESC,S.TACHE_DESC,S.TEMPLATE_ID,A.TEMPLATE_NAME,"
        		+ "S.RETURN_ORG_NAME,S.RETURN_STAFF_NAME,DATE_FORMAT(S.ALLTO_DATE,'%Y-%m-%d %H:%i:%s') ALLTO_DATE,"
        		+ "S.DEAL_ORG_NAME,S.DEAL_STAFF_NAME,S.CHECK_ORG_NAME,S.CHECK_STAFF_NAME,"
        		+ "DATE_FORMAT(S.CHECK_DATE,'%Y-%m-%d %H:%i:%s') CHECK_DATE,S.APPROVE_RATE,"
        		+ "TIMESTAMPDIFF(SECOND,S.CHECK_DATE,DATE_ADD(S.ALLTO_DATE,INTERVAL S.CHECK_LIMIT*3600 SECOND))/3600 AS OVER_FLAG,S.CHECK_STATUS,"
        		+ "S.APPEAL_REASON,DATE_FORMAT(S.APPEAL_DATE,'%Y-%m-%d %H:%i:%s') APPEAL_DATE,"
        		+ "S.APPROVE_CONTENT,DATE_FORMAT(S.APPROVE_DATE,'%Y-%m-%d %H:%i:%s') APPROVE_DATE "
        		+ "FROM CC_QUALITY_SHEET S,CC_QUALITY_TEMPLATE A WHERE 1=1 "
        		+ "AND S.TEMPLATE_ID=A.TEMPLATE_ID AND S.CHECK_STATUS !=0" + strWhere;
        Map map = new HashMap();
    	map.put("CC_QUALITY_SHEET", "S");
    	sql = systemAuthorization.getAuthedSql(map, sql, "900018409");//质检结果查询实体
        return dbgridDataPub.getResultBySize(sql, begion, pageSize, " ORDER BY CHECK_DATE", "");
    }
}