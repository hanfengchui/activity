/**
 * <p>类名：WorkFlowBusiServImpl.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	Apr 10, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.timesontransfar.customservice.worksheet.dao.*;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.service.IComplaint;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.dao.IpersonaDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServContentInstance;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.paramconfig.pojo.SheetLimitTimeCollocate;
import com.timesontransfar.customservice.paramconfig.service.IsheetLimitTimeService;
import com.timesontransfar.customservice.tuschema.dao.IserviceContentTypeDao;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.customservice.workFlowOrg.service.impl.FlowOrgFactory;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedContactDAOImpl;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedRepeatDAOImpl;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedReturnDAOImpl;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedWeixinDAOImpl;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.SheetOperation;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetStatuApplyInfo;
import com.timesontransfar.customservice.worksheet.service.IhastenSheet;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.IworkFlowBusiServ;
import com.timesontransfar.customservice.worksheet.service.OrderRefundService;
import com.timesontransfar.dapd.service.IdapdSheetInfoService;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.sheetHandler.CompatHandler;
import com.timesontransfar.staffSkill.FlowToEnd;
import com.timesontransfar.staffSkill.StaffWorkloadInfo;
import com.timesontransfar.staffSkill.service.IStaffWorkloadService;
import com.transfar.common.exception.MyOwnRuntimeException;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Component("WorkFlowBusiServImpl__FACADE__")
public class WorkFlowBusiServImpl implements IworkFlowBusiServ {
	private static final Logger logger = LoggerFactory.getLogger(WorkFlowBusiServImpl.class);
	
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private IorderCustInfoDao orderCustInfoDao;
	@Autowired
	private IserviceContentDao serviceContentDao;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private ISheetActionInfoDao sheetActionInfoDao;
	@Autowired
	private IsheetLimitTimeService sheetLimitTimeServ;
	@Autowired
	private IhastenSheet hastenSheetServ;
	@Autowired
	private IworkSheetAllotRealDao workSheetAlllot;
	@Autowired
	private InoteSenList noteSen;
	@Autowired
	private ItsWorkSheetDao tsWorkSheetDao;
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	@Autowired
	private IserviceContentSchem serviceContentSchem;
	@Autowired
    private ILabelManageDAO labelManageDAO;
	@Autowired
    private ISheetMistakeDAO sheetMistakeDAO;
	@Autowired
	private ISJSheetQualitative sjSheetQualitative;
    @Autowired
    private CmpUnifiedContactDAOImpl cmpUnifiedContactDAOImpl;
    @Autowired
    private CmpUnifiedRepeatDAOImpl cmpUnifiedRepeatDAOImpl;
    @Autowired
    private CmpUnifiedReturnDAOImpl cmpUnifiedReturnDAOImpl;
    @Autowired
    private CmpUnifiedWeixinDAOImpl clqUnifiedWeixinDAOImpl;
	@Autowired
	private IComplaint complaintImpl;
	@Autowired
    private IserviceContentTypeDao serviceContentType;
	@Autowired
    private ItsSheetQualitative sheetQualitative;// 投诉定性表
	@Autowired
	private OrderRefundService orderRefundService;
	
    /**
     * 派发工单的操作实例
     */
    @Autowired
    private WorkSheetAllot workSheetAllot;
	@Autowired
	private IpersonaDao personaDao;
	@Autowired
	private IForceDistillDao forceDistillDao;
	@Autowired
	private IdapdSheetInfoService dapdSheetService;
    @Autowired
    private IStaffWorkloadService staffWorkloadService;
	@Autowired
	private CustomerServiceFeign customerServiceFeign;
	
    public static final String BACKORDER = "BACKORDER";
    public static final String NULLORG = "NULLORG";
    public static final String SERV_ORDER_ID = "SERV_ORDER_ID";
    public static final String MONTH_FALG = "MONTH_FALG";
    public static final String ORDER_ID_STATU = "ORDER_ID_STATU";
    public static final String CH1 = "由来源工单生成新的工单时,工单保存失败!";
    public static final String SHEET_ID = "SHEET_ID";
    public static final String ROUTE_VALUE = "ROUTE_VALUE";
    public static final String DEAL_REQUIRE = "DEAL_REQUIRE";
    public static final String FLOW_SEQUENCE = "FLOW_SEQUENCE";
    public static final String DEAL_PR_ORGID = "DEAL_PR_ORGID";
    public static final String DEAL_PR_STAFFID = "DEAL_PR_STAFFID";
    public static final String AUD_FLAG = "AUD_FLAG";
    public static final String SYSTEM = "SYSTEM";
    
    private String sendNoteCont(SheetPubInfo bean, int type) {
        if(pubFunc.getSystemAuthorization().getHttpSession() == null){
            return null;
        }
		//type 为0发送部门 为1发送个人
		NoteSeand noteBean = null; //new NoteSeand();
		String phone = "";
		String client = "0";
		String sheetGuid;
		String relaPerson = "";
		List tmp = null;
		if(type == 0) {
			tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(),null, bean.getTacheId(),0);
		} else {
			tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(),String.valueOf(bean.getDealStaffId()), bean.getTacheId(),1);
		}			
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskObj(bean.getServiceOrderId(), bean.getMonth(), false);
		ServiceContent serviceContent = serviceContentDao.getServContentByOrderId(bean.getServiceOrderId(), false, 0);
		boolean fzFlag = false;
		if (checkZPDH(pubFunc.getLastXX(serviceContent))) {
			fzFlag = true;
		}
		String comment = orderAskInfo.getComment();
		int size = tmp.size();
		Map map = null;
		if(size > 0) {
			//取当前登录员工信息
			TsmStaff staff = this.pubFunc.getLogonStaff();
			if (null == staff) {
				staff = pubFunc.getStaff(bean.getRetStaffId());
			}
			int staffId = Integer.parseInt(staff.getId());
			String staffName = staff.getName();
			String orgId = staff.getOrganizationId();
			String orgName = staff.getOrgName();

			for(int i=0; i<size; i++) {
				map = (Map) tmp.get(i);
				noteBean = new NoteSeand();
				sheetGuid = this.pubFunc.crtGuid();
				phone = map.get("RELAPHONE").toString();
				client = map.get("CLIENT_TYPE").toString();
				relaPerson = map.get("RELA_PERSON").toString();
				noteBean.setSheetGuid(sheetGuid);
				noteBean.setRegionId(bean.getRegionId());
				noteBean.setDestteRmid(phone);
				noteBean.setClientType(Integer.parseInt(client));
				
				OrderCustomerInfo cus = orderCustInfoDao.getOrderCustByGuid(orderAskInfo.getCustId(), false);
				String cn= cus.getCustName()==null?"":cus.getCustName();
				String ph = StringUtils.defaultIfEmpty(orderAskInfo.getRelaInfo(), "");
				String sc = relaPerson + "您好:有一条新的" + bean.getServTypeDesc() + "单派发到你部门,服务单号为:" + bean.getServiceOrderId()
						+ "," + "处理时限为:" + bean.getDealLimitTime() + "小时,受理的内容概述为：" + comment + ",客户姓名:" + cn + ",联系电话:"
						+ ph + ",请注意查收.";
				if (fzFlag) {
					sc = "【诈】" + sc;
				}
				noteBean.setSendContent(sc);
				noteBean.setOrgId(orgId);
				noteBean.setOrgName(orgName);
				noteBean.setStaffId(staffId);
				noteBean.setStaffName(staffName);
				noteBean.setBusiId(bean.getWorkSheetId());
				this.noteSen.saveNoteContent(noteBean);
			}
		}		
		return "";
	}

	// 判断是否是诈骗电话
	private boolean checkZPDH(int lastXX) {
		String str = String.valueOf(lastXX);
		return str.startsWith("11504") || str.startsWith("11505") || "23002270".equals(str);
	}

    /**
     * 根据来源工单信息生成新的工单
     * 
     * @param inParam
     *            来源工单相关信息map
     * @return 包含新的工单号的map key值NEW_SHEET_ID
     */
	@SuppressWarnings("all")
	public Map crtWorkSheet(Map inParam) {
		logger.info("crtWorkSheet inParam: {}", JSON.toJSON(inParam));
		// 根据工作流来源得到工单对象list
		List sheetPubInfoList = getSheetPubInfoFromOrder(inParam);
		if(sheetPubInfoList ==  null){
			logger.warn("在生成工单时,不能根据入参生成任何工单!");
			throw new MyOwnRuntimeException("在生成工单时,不能根据入参生成任何工单!");
		}
		
		int size = sheetPubInfoList.size() - 1;
		OrderAskInfo orderAskInfo = (OrderAskInfo)sheetPubInfoList.get(size);//得到定单对象
		
		int count = 0;
		String sheetId = "";
		int tachId = 0;
		String mainSheetId = "";
		String mainOrgId = "";
	    // 判断是否为前台退回的工单提交
		String backOrder = inParam.get(BACKORDER)==null?"":inParam.get(BACKORDER).toString();
		int sndType = 0; // 0为发送短信到部门,1为发送短信到个人
		for (int i = 0; i < size; i++) {
			SheetPubInfo sheetPubInfo = (SheetPubInfo) sheetPubInfoList.get(i);
			if(backOrder.equals(BACKORDER) && inParam.get("BACKSHEET")!=null){
				this.setBackSheet(orderAskInfo, sheetPubInfo, inParam);
			}
			
			// 参与自动分派  后台派单环节
			String result = autoAllotSheetAddForce(sheetPubInfo, orderAskInfo, inParam);
			
			count += this.sheetPubInfoDao.saveSheetPubInfo(sheetPubInfo);
			//发送短信			
			if(CompatHandler.isTachOrgDeal(sheetPubInfo.getTacheId())) {
			    sndType = WorkSheetAllot.RST_SUCCESS.equals(result) ? 1 : 0;
				sendNoteCont(sheetPubInfo, sndType);
			}
			sheetId = sheetPubInfo.getWorkSheetId();						
			if(sheetPubInfo.getMainType() == 1) {
				mainSheetId = sheetId;
				mainOrgId = sheetPubInfo.getRetOrgId();//主要解决前台直接到综调
			}
			tachId =  sheetPubInfo.getTacheId();
			//工单状态
			int sheetStatu=0;
			String stateDesc = "";
			//余额先判和后判断
			if(sheetPubInfo.getSheetStatu()!=StaticData.WKST_JUDGING && sheetPubInfo.getSheetStatu()!=StaticData.WKST_JUDGED ){
				//如果不等于NULLORG,说明派到个人
				if(sheetPubInfo.getDealOrgId().equals(NULLORG)) {
					sheetStatu = pubFunc.getSheetStatu(tachId, 0, sheetPubInfo.getSheetType());
					stateDesc = pubFunc.getStaticName(sheetStatu);
					sheetPubInfoDao.updateSheetState(sheetId,sheetStatu, stateDesc,sheetPubInfo.getMonth(),0);
				} else {
					//将工单设置为处理中
					sheetStatu = pubFunc.getSheetStatu(tachId, 1, sheetPubInfo.getSheetType());
					stateDesc = pubFunc.getStaticName(sheetStatu);
					sheetPubInfoDao.updateSheetState(sheetId,sheetStatu, stateDesc,sheetPubInfo.getMonth(),1);
				}
			}
		}
		//更新定单状态
		String srcOrderId = inParam.get(SERV_ORDER_ID).toString();
		String strMonth = inParam.get(MONTH_FALG).toString();
		Integer month = Integer.valueOf(strMonth);
		String strOrderStatu = inParam.get(ORDER_ID_STATU)==null ? "0" : inParam.get(ORDER_ID_STATU).toString();
		int orderStatu=Integer.parseInt(strOrderStatu);
		orderAskInfoDao.updateOrderStatu(srcOrderId,orderStatu,month,pubFunc.getStaticName(orderStatu));
		if (count != size) {
			logger.error(CH1);
			logger.info("请查证sheetPubInfoDao.saveSheetPubInfo(sheetPubInfo)");
			throw new MyOwnRuntimeException(CH1);
		}

		Map outParam = new HashMap();
		if(CompatHandler.isTachOrgDeal(tachId)) {
			outParam.put(SHEET_ID, mainSheetId);
			outParam.put("MAINORGID", mainOrgId);//综调绿色通道
		} else {
			outParam.put(SHEET_ID, sheetId);
		}	
		return outParam;
	}
	
	/**
	 * 设置退回工单状态
	 * @param orderAskInfo 定单对象
	 * @param sheetPubInfo 新生成的工单对象
	 * @param inParam 工作流返回的map对象
	 * @return
	 */
	private SheetPubInfo setBackSheet(OrderAskInfo orderAskInfo,SheetPubInfo sheetPubInfo,Map inParam) {

			// 服务单受理员工不在苏州本地网 
		Map lantInfo = this.pubFunc.getLantInfoByStaffId(orderAskInfo.getAskStaffId() );
		if(lantInfo==null || !(lantInfo.get("LANT_ID").toString()).equals( String.valueOf(StaticData.REGION_ID))){
		 
			//退回单提交后,回到原退回员工那里处理
			String backSheet = inParam.get("BACKSHEET").toString();			
			SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
					backSheet,sheetPubInfo.getRegionId(),sheetPubInfo.getMonth(), true);
			if(sheetInfo != null) {
				sheetPubInfo.setRcvOrgId(sheetInfo.getRetOrgId());
				sheetPubInfo.setRcvOrgName(sheetInfo.getRetOrgName());
				sheetPubInfo.setDealOrgId(sheetInfo.getRetOrgId());
				sheetPubInfo.setDealOrgName(sheetInfo.getRetOrgName());
				sheetPubInfo.setDealStaffId(sheetInfo.getRetStaffId());
				sheetPubInfo.setDealStaffName(sheetInfo.getRetStaffName());
				int recRegion = this.pubFunc.getOrgRegion(sheetInfo.getRetOrgId());
				String recRegionName = this.pubFunc.getRegionName(recRegion);			
				sheetPubInfo.setReceiveRegionId(recRegion);
				sheetPubInfo.setReceiveRegionName(recRegionName);		
			}
		}
		return sheetPubInfo;		
	}

	private String autoAllotSheetAddForce(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, Map inParam) {
		String result = WorkSheetAllot.RST_NONE;
		int forceStaff = forceDistillDao.selectForceStaffByOrderId(orderAskInfo.getServOrderId());
		if (forceStaff > 0) {
			// 参与业务工单监控箱自动分派
			result = autoForceDistill(sheetPubInfo, forceStaff);
		}
		if (WorkSheetAllot.RST_NONE.equals(result)) {
			// 参与自动分派 后台派单环节
			result = autoAllotSheet(sheetPubInfo, inParam);
		}
		return result;
	}

	// 业务工单监控箱自动分派
	private String autoForceDistill(SheetPubInfo sheetPubInfo, int forceStaff) {
		int tacheId = sheetPubInfo.getTacheId();
		if (CompatHandler.isTachAssign(tacheId) || StaticData.TACHE_RGHF == tacheId || StaticData.TACHE_DINGXING_NEW == tacheId) {
			return workSheetAllot.allotForceDistill(sheetPubInfo, forceStaff);
		}
		if (StaticData.TACHE_ZHONG_DINGXING_NEW == tacheId) {
			SheetPubInfo sourceSheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetPubInfo.getSourceSheetId(), false);
			if (StaticData.TACHE_DINGXING_NEW != sourceSheetInfo.getTacheId()) {
				return workSheetAllot.allotForceDistill(sheetPubInfo, forceStaff);
			}
		}
		return WorkSheetAllot.RST_NONE;
	}

	/**
	 * 参与自动分派
	 * @author LiJiahui 修改
     * @date 2011-10-21
	 * @param sheetPubInfo 新生成工单对象
	 * @param orderAskInfo 定单对象
	 * @param inParam
	 * @return
	 */
	private String autoAllotSheet(SheetPubInfo sheetPubInfo, Map inParam) {
		// 不进行自动分派
		if(inParam.containsKey("NOT_AUTO_ALLOT")) {
			logger.info("后台派单直接终定性，生成的终定性工单不进行自动分派");
			return WorkSheetAllot.RST_NONE; 
		}
		
		if(inParam.containsKey("LAST_DEAL_STAFFID")){
	        // 审核退单，将新生成工单派给最后处理的本地网员工
	        int dealStaffId = Integer.parseInt(inParam.get("LAST_DEAL_STAFFID").toString());
	        String dealStaffName = inParam.get("LAST_DEAL_STAFFNAME").toString();
	        String dealOrgId = inParam.get("LAST_DEAL_ORGID").toString();
	        String dealOrgName = inParam.get("LAST_DEAL_ORGNAME").toString();
	        sheetPubInfo.setRcvStaffId(dealStaffId); // 收单员工ID
	        sheetPubInfo.setRcvStaffName(dealStaffName); // 收单员工姓名
	        sheetPubInfo.setDealStaffId(dealStaffId); // 处理单位ID，即收单员工所在部门ID
	        sheetPubInfo.setDealStaffName(dealStaffName); // 处理单位名称，即收单员工所在部门名称
	        sheetPubInfo.setDealOrgId(dealOrgId); // 处理员工ID，即收单员工ID
	        sheetPubInfo.setDealOrgName(dealOrgName); // 处理员工名，即收单员工名
	        sheetPubInfo.setLockFlag(1); // Lock置为1
	        sheetPubInfo.setSheetStatu(StaticData.WKST_DEALING_STATE);
            sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_DEALING_STATE));
            String comment = "审核退单，系统自动分派新工单，工单号为" + sheetPubInfo.getWorkSheetId() + " 分派给员工" + dealStaffName + dealStaffId;
            workSheetAllot.saveSheetDealAction(sheetPubInfo, StaticData.WKST_SYSTEM_AUTO, 1, comment);
            return WorkSheetAllot.RST_SUCCESS;
	    }else{
	        if(sheetPubInfo.getDealOrgId().equals(NULLORG)) {//判断该工单在部门工单池中,没有直接派发到个人
	        	int tach = sheetPubInfo.getTacheId();
		        String route = inParam.get(ROUTE_VALUE).toString();
	        	// 后台派单、部门处理、后台审核、人工回访、终定性（现场办结到终定性、后台派单到终定性、部门处理到终定性）
	            if (CompatHandler.isTachAssign(tach) 
	            		|| CompatHandler.isTachOrgDeal(tach) 
	            		|| StaticData.TACHE_AUIT == tach 
	            		|| StaticData.TACHE_RGHF == tach 
	                    || (StaticData.TACHE_ZHONG_DINGXING_NEW == tach && 
	                    		(StaticData.ROUTE_ASK_TO_FINASSESS.equals(route) 
	                    			|| StaticData.ROUTE_ASSIGN_TO_FINASSESS.equals(route) 
	                    			|| StaticData.ROUTE_DEAL_TO_FINASSESS.equals(route)))) {
	            	String result = workSheetAllot.allotSheet(sheetPubInfo);
	            	logger.info("系统自动派发: {} 环节: {} result: {}", sheetPubInfo.getWorkSheetId(), sheetPubInfo.getTacheDesc(), result);
	            	return result;
	            }
	        }
	    }
		return WorkSheetAllot.RST_NONE;
	}	
	
	/**
	 *审核环节入方法调用,生成审核工单
	 * @param inParam	来源工单相关信息map
	 * @return
	 */
	public Map crtAudWorkSheet(Map inParam) {			
		//取得来源定单号
		String srcOrderId = inParam.get(SERV_ORDER_ID).toString();
		String strMonth = inParam.get(MONTH_FALG).toString();
		Integer month = Integer.valueOf(strMonth);
		//查询定单的受量信息,如果没有受理信息则不生成工单
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskObj(srcOrderId,month,false);
		if (orderAskInfo == null) {
			logger.warn("没有查询到定单号为" + srcOrderId + "的受理单!");
			return null;
		}
		/* =================以下代码取得工单需要的其它信息========= */
		//取来源定单号,如果没有来源定单号则取来源工单号
		String srcSheetId = srcOrderId;
		if(inParam.get(SHEET_ID) != null){
			srcSheetId = inParam.get(SHEET_ID).toString();
		}
		//工作流实例,和环节实例
		String wfInstId = inParam.get("WF__INSTANCE__ID").toString();
		String wfNodeInstId = inParam.get("WF__NODE_INSTANCE__ID").toString();
		//业务环节,环节名
		int tacheId = Integer.parseInt(inParam.get("TACHE_ID").toString());
		String tacheDesc = this.pubFunc.getStaticName(tacheId);
		//取工单类型
		int servType = orderAskInfo.getServType();
		int regionId = orderAskInfo.getRegionId();
		int sheetType = PubFunc.getSheetType(servType,tacheId);//工单类型
		
		//取处理要求
		String require = "";
		if (inParam.containsKey(DEAL_REQUIRE)) {
			require = inParam.get(DEAL_REQUIRE).toString();
		}			
		//取得工单的优先级,预告警,时限信息
		int sheetPriValue = 0;//优先权重值
		int dealLimitTime = 0;
		int preAlarmValue = 0;
		int alarmValue = 0;
		//计算优先级和预告警时间
		OrderCustomerInfo custInfo = this.orderCustInfoDao.getOrderCustByOrderId(srcOrderId);
		if(custInfo != null){
			//计算优先级
			int[] queryInfo = new int[4];
			queryInfo[0] = servType;//服务类型
			queryInfo[1] = custInfo.getCustBrand();//客户品牌
			queryInfo[2] = orderAskInfo.getCustServGrade();//服务等级
			queryInfo[3] = orderAskInfo.getUrgencyGrade();//紧急程度			
			
			SheetLimitTimeCollocate limitBean = this.sheetLimitTimeServ.getSheetLimitime(
					orderAskInfo.getRegionId(),
					orderAskInfo.getServType(), tacheId, 1,
					orderAskInfo.getCustServGrade(),
					orderAskInfo.getUrgencyGrade()
			);	
			if(limitBean != null) {
				dealLimitTime = limitBean.getLimitTime();
				preAlarmValue = limitBean.getPrealarmValue();
			}
			
		}		
		//流水顺序号
		int flowNo=1;
		if(inParam.containsKey(FLOW_SEQUENCE)){
			flowNo = Integer.parseInt(inParam.get(FLOW_SEQUENCE).toString());
		} else { //没有流水号传过来，根据定单号去最大的流水顺序号
			flowNo = this.sheetPubInfoDao.getFlowSeq(srcOrderId, regionId)+1;
		}
				
		/* ==============以下代码为组装工单信息======================== */
/*		List orgList = new ArrayList();		
		if(inParam.containsKey("ORG_LIST")){
			orgList = (List)inParam.get("ORG_LIST");
		}*/				
			// 生成工单属性并进行设置
			SheetPubInfo sheetPubInfo = new SheetPubInfo();
			
			String sheetId = pubFunc.crtSheetId(orderAskInfo.getRegionId());
			sheetPubInfo.setWorkSheetId(sheetId);
			sheetPubInfo.setServiceOrderId(srcOrderId);
			sheetPubInfo.setRegionId(regionId);
			sheetPubInfo.setRegionName(orderAskInfo.getRegionName());
			sheetPubInfo.setServType(servType);
			sheetPubInfo.setServTypeDesc(orderAskInfo.getServTypeDesc());
			sheetPubInfo.setSourceSheetId(srcSheetId);
			sheetPubInfo.setTacheId(tacheId);
			sheetPubInfo.setTacheDesc(tacheDesc);
			sheetPubInfo.setWflInstId(wfInstId);
			sheetPubInfo.setTacheInstId(wfNodeInstId);
			sheetPubInfo.setSheetType(sheetType);
			sheetPubInfo.setSheetTypeDesc(this.pubFunc.getStaticName(sheetType));
			sheetPubInfo.setSheetPriValue(sheetPriValue);
			if (pubFunc.isNewWorkFlow(srcOrderId) && orderAskInfo.getServiceDate()==3) {
				sheetPubInfo.setDealLimitTime(labelManageDAO.selectAuditHours(srcOrderId));
				sheetPubInfo.setStationLimit(labelManageDAO.selectAuditHours(srcOrderId));
			} else {
				sheetPubInfo.setDealLimitTime(dealLimitTime);
				sheetPubInfo.setStationLimit(dealLimitTime);
			}
			sheetPubInfo.setPreAlarmValue(preAlarmValue);
			sheetPubInfo.setAlarmValue(alarmValue);
			sheetPubInfo.setFlowSequence(String.valueOf(flowNo));//流程流水号
			flowNo=flowNo+1;			
			//派单环节处理部门,产生部门的审核单
			String disDealOrgId = "";
			if(inParam.containsKey(DEAL_PR_ORGID)){
				disDealOrgId = inParam.get(DEAL_PR_ORGID).toString();
			}
			String retStaffId="";
			if(inParam.containsKey(DEAL_PR_STAFFID)){
				retStaffId = inParam.get(DEAL_PR_STAFFID).toString();
			}
			int recRegion = this.pubFunc.getOrgRegion(disDealOrgId);
			String recRegionName = this.pubFunc.getRegionName(recRegion);	
			
			//作判断 如果为前台直接派单 去找对应的工位
			String audFlag="0";
			if(inParam.containsKey(AUD_FLAG)){
				audFlag = inParam.get(AUD_FLAG).toString();
			}
			if(audFlag.equals("1") || orderAskInfo.getServiceDate() == 3) {
				//得到工位
				sheetPubInfo = getFlowOrgId(sheetPubInfo,orderAskInfo,sheetPubInfo,inParam);
				
/*				int newRegion = this.pubFunc.getOrgRegion(audOrgId);
				sheetPubInfo.setReceiveRegionId(newRegion);//收单地域
				sheetPubInfo.setReceiveRegionName(this.pubFunc.getRegionName(newRegion));
				sheetPubInfo.setRcvOrgId(audOrgId);//收单部门
				sheetPubInfo.setRcvOrgName(this.pubFunc.getOrgName(audOrgId));	*/				
			} else {
				sheetPubInfo.setReceiveRegionId(recRegion);//收单地域
				sheetPubInfo.setReceiveRegionName(recRegionName);			
				sheetPubInfo.setRcvOrgId(disDealOrgId);//收单部门
				sheetPubInfo.setRcvOrgName(this.pubFunc.getOrgName(disDealOrgId));				
			}					
			sheetPubInfo.setRetOrgId(disDealOrgId);//派发部门
			sheetPubInfo.setRetOrgName(this.pubFunc.getOrgName(disDealOrgId));
			sheetPubInfo.setRetStaffId(Integer.parseInt(retStaffId));//派发员工
			sheetPubInfo.setRetStaffName(this.pubFunc.getStaffName(Integer.parseInt(retStaffId)));
			
			sheetPubInfo.setDealRequire(require);//加上要求填写人的名字,电话
			sheetPubInfo.setMonth(month);						
					
		int count = 0;
		//String sheetId = "";
			count += this.sheetPubInfoDao.saveSheetPubInfo(sheetPubInfo);

			sheetId = sheetPubInfo.getWorkSheetId();
			// 将此工单设置为待处理
/*			int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 0, sheetPubInfo.getSheetType());
			String stateDesc = pubFunc
					.getStaticName(sheetStatu);*/
			
			
			// 将此工单设置为已派发
			int sheetStatu = StaticData.WKST_ALLOT_STATE;
			if(sheetPubInfo.getServType()==720130000){
			    sheetStatu = StaticData.WKST_ALLOT_STATE_NEW;
			}			
			
			String route = inParam.get(ROUTE_VALUE).toString();
			boolean booOrg=false;//是否从部门处理环节流过来
			if(route.equals("GOTO_NEXT")) {
				booOrg=true;
			}
			if(!booOrg) {//不是从部门流转过来的工单
				sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 0, sheetPubInfo.getSheetType());
				//更新定单状态
				String strOrderStatu = "0";
				if (inParam.containsKey(ORDER_ID_STATU)) {
					strOrderStatu = inParam.get(ORDER_ID_STATU).toString();
				}
				int orderStatu=0;
				orderStatu = Integer.parseInt(strOrderStatu);
				this.orderAskInfoDao.updateOrderStatu(srcOrderId,orderStatu,month,this.pubFunc.getStaticName(orderStatu));
			}
			String stateDesc = pubFunc.getStaticName(sheetStatu);			
			this.sheetPubInfoDao.updateSheetState(sheetId,
					sheetStatu, stateDesc,sheetPubInfo.getMonth(),0);
			
			if(booOrg) {//从部门自动流转插派单关系表
				//以下为保存派单关系表 srcSheetId
				WorkSheetAllotReal workSheetAllo = null;
				if (pubFunc.isNewWorkFlow(srcOrderId) && orderAskInfo.getServiceDate() == 3) {
					//未编写逻辑
				} else {
				String[] sheetIdList = this.sheetPubInfoDao.getSouresheetObj(srcSheetId, month);
				int orgLongth=0;
				if(sheetIdList.length > 0) {
					orgLongth = sheetIdList.length;
				}
				for(int i=0;i<orgLongth;i++) {
					workSheetAllo = new WorkSheetAllotReal();
					int mainFalg = 0;
					
					if(srcSheetId.equals(sheetIdList[i])) {
						mainFalg = 1;
					}
					workSheetAllo.setWorkSheetId(sheetIdList[i]);
					workSheetAllo.setCheckWorkSheet(sheetId);
					workSheetAllo.setPreDealSheet("0");
					workSheetAllo.setCheckFalg(0);
					workSheetAllo.setMainSheetFlag(mainFalg);
					workSheetAllo.setDealStauts("待处理");
					workSheetAllo.setMonth(month);
					workSheetAllo.setOrderId(srcOrderId);
					this.workSheetAlllot.saveWorkSheetAllotReal(workSheetAllo, true);
				}
				}
			}			
			
		if (count == 0) {
			logger.error(CH1);
			logger.info("请查证sheetPubInfoDao.saveSheetPubInfo(sheetPubInfo)");
			throw new MyOwnRuntimeException(CH1);
		}

		Map outParam = new HashMap();
		outParam.put(SHEET_ID, sheetId);

		return outParam;		
	}

	/**
	 * 将工单至为完成状态
	 * @param inParam	工作流伟入的入参
	 * @return
	 */
	public Map updateSheetFinish(Map inParam) {
		String sheetId = "";
		int month=0;
		if (inParam.containsKey(SHEET_ID)) {
			sheetId = inParam.get(SHEET_ID).toString();
		}
		if (inParam.containsKey(MONTH_FALG)) {
			month = Integer.parseInt(inParam.get(MONTH_FALG).toString());
		}
		
		int state = StaticData.WKST_FINISH_STATE;
		String stateDesc = pubFunc.getStaticName(state);
		
		
		
		SheetPubInfo sheetObj = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
		if(null != sheetObj && sheetObj.getServType() == StaticData.SERV_TYPE_NEWTS){
	        state = StaticData.WKST_FINISH_STATE_NEW;
	        stateDesc = pubFunc.getStaticName(StaticData.WKST_FINISH_STATE_NEW);
		}
		
		this.sheetPubInfoDao.updateSheetState(sheetId, state, stateDesc, month, 2);
		this.sheetPubInfoDao.updateSheetFinishDate(sheetId);

		return new HashMap();
	}

	/**
	 * 将定单至为处理中状态
	 * 
	 * @param inParam
	 *            工作流伟入的入参
	 * @return
	 */
	public Map updateOrderDealState(Map inParam) {		
		String orderId = "";
		if(inParam.containsKey(SERV_ORDER_ID)){
			orderId = inParam.get(SERV_ORDER_ID).toString();
		}
		String strMonth = inParam.get(MONTH_FALG).toString();
		Integer month = Integer.valueOf(strMonth);		
		boolean updateFlag = this.orderAskInfoDao.updateOrderStatu(orderId,
				StaticData.OR_REPEAL_STATU,month,this.pubFunc.getStaticName(StaticData.OR_REPEAL_STATU));
		if (!updateFlag) {
			logger.warn("更新" + orderId + "定单的状态失败!");
		}	
		
		return new HashMap();
	}
	
	/**
	 * 将定单至为后台退回状态
	 * @param inParam	工作流伟入的入参
	 * @return
	 */
	public Map updateOrderBackState(Map inParam) {		
		// 自动生成退单工单
		Map outParam = this.crtWorkSheet(inParam);
		String sheetId = outParam.get(SHEET_ID).toString();//已变为新生成的工单
		int month=0;
		month = Integer.parseInt(inParam.get(MONTH_FALG).toString());
		int sheetState = 0;
		int orderStatu = 0;
		SheetPubInfo po = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
		if(po.getServType() == StaticData.SERV_TYPE_NEWTS){
		    sheetState = StaticData.WKST_DEALING_STATE_NEW;
		    orderStatu = StaticData.OR_BACK_STATU_NEW;
		}else{
		    sheetState = StaticData.WKST_MODIFISHEET_STATE;
		    orderStatu = StaticData.OR_BACK_STATU;
		}
		sheetPubInfoDao.updateSheetState(sheetId, sheetState, pubFunc.getStaticName(sheetState), month, 2);
		// 更新定单状态为后台退回
		String orderId = inParam.get(SERV_ORDER_ID).toString();
		orderAskInfoDao.updateOrderStatu(orderId, orderStatu, month,this.pubFunc.getStaticName(orderStatu));
		// 更新退单要求
		String comments = inParam.get(DEAL_REQUIRE).toString();
		orderAskInfoDao.updateOrderComments(orderId, comments);
		return new HashMap();
	}
	

	/**
	 * 服务单以及其所有工单等信息进历史
	 * @param ipParam 工作流传入的参数
	 * @return
	 */
	//@Transactional(rollbackFor=Exception.class)
	public Map finishOrderAndSheet(Map inParam) {
		//try {
			// 取得传入的参数
			String orderId = inParam.get(SERV_ORDER_ID).toString();
			String strMonth = inParam.get(MONTH_FALG).toString();
			Integer month = Integer.valueOf(strMonth);
			String typeFlag = "0";
			if (inParam.containsKey("TYPEFLAG")) {
				typeFlag = inParam.get("TYPEFLAG").toString();
			}
			String finishSheetId = "";
			if (inParam.containsKey(SHEET_ID)) {
				finishSheetId = inParam.get(SHEET_ID).toString();
			}
			
			OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskObj(orderId, month, false);
			String custGuid = orderAskInfo.getCustId();//将当前表定单中客户的guid查出来
			int netFlag = orderAskInfo.getNetFlag();//得到定单是在那个环节归档的
			
			// 最严工单标识更新
			this.updateBestOrder(orderAskInfo, orderId);
			// 投诉查询单归档信息
			dapdSheetService.setDapdArchiveDate(orderId);
			
			if(typeFlag.equals("0")) {//工单注销就不调用该段
				//将峻工单的状态设置为完成
				updateSheetFinish(inParam);
				sheetPubInfoDao.updateFetchSheetStaff(finishSheetId, 0, SYSTEM, SYSTEM, SYSTEM);
				int oStatu = orderAskInfo.getServType() == StaticData.SERV_TYPE_NEWTS ? StaticData.OR_FINISH_STATU : StaticData.OR_COMPLETE_STATU;
				// 更新定单的状态为完成
				orderAskInfoDao.updateOrderStatu(orderId, oStatu, month, pubFunc.getStaticName(oStatu));
				//更新定单完成时间 如果是在定性环节和归档环节竣工的话就只单独更新完成时间
				if(netFlag == StaticData.TACHE_TSQUALITATIVE || netFlag == StaticData.TACHE_PIGEONHOLE) {
					orderAskInfoDao.updateOrderFinishDate(orderId, 14);
				} else {
					orderAskInfoDao.updateOrderFinishDate(orderId, 15);
				}
			}
			complaintImpl.complaintPostInfo(10, orderId);
			
			// 将当前表的信息保存到历史表中
			orderCustInfoDao.saveOrderCustHis(custGuid,month);// 保存客户信息到历史表
			orderAskInfoDao.saveOrderAskInfoHis(orderId,month);// 保存受理信息到历史表
			serviceContentDao.saveServContentHis(orderId,month);// 保受量内容到历史表
            labelManageDAO.saveLabelHisById(orderId);//保存标签到历史表
            labelManageDAO.insertRuyiLabelHis(orderId);//保存如意标识到历史表
            personaDao.savePersonaHis(orderId);
            cmpUnifiedContactDAOImpl.saveUnifiedContactHisByOrderId(orderId);
            cmpUnifiedRepeatDAOImpl.saveUnifiedRepeatHisByCurSoi(orderId);
			ComplaintUnifiedReturn cur = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(orderId);
			if (null != cur && "0".equals(cur.getResult()) && cur.getUnifiedComplaintCode().length() >= 0) {
				cmpUnifiedReturnDAOImpl.saveUnifiedReturnHisByOrderId(orderId);
			}
			clqUnifiedWeixinDAOImpl.saveUnifiedShowHisByOrderId(orderId);
			sheetMistakeDAO.insertOrderMistakeHisByOrderId(orderId);
			insertCustomerJudgeHisByOrderId(orderId);
			sheetPubInfoDao.insertXcFlowHis(orderId);
			sheetPubInfoDao.saveSheetPubInfoHis(orderId,month);// 此定单下所有的工单到历史表
			sheetPubInfoDao.finishComplaintConnection(orderId);
			this.sheetActionInfoDao.saveSheetActionHisInfo(orderId,month);// 保存工单运作到历史
			this.sheetActionInfoDao.saveSheetHiddenActionHisByOrderId(orderId);// 保存隐藏运作到历史
			this.hastenSheetServ.saveHastenSheetInfoHis(orderId,month);		//保存催单到历史表
			ServContentInstance servContInst = new ServContentInstance();
			servContInst.setServOrderId(orderId);
			servContInst.setMonth(month);
			WorkSheetStatuApplyInfo sheetApply = new WorkSheetStatuApplyInfo();
			sheetApply.setOrderId(orderId);
			sheetApply.setMonth(month);
			this.tsWorkSheetDao.saveSheetApply(sheetApply, false);//工单挂起和释放申请进历史表
			//如果要删除派单关系表数据
			WorkSheetAllotReal workSheetAllo = new WorkSheetAllotReal();
			workSheetAllo.setOrderId(orderId);
			workSheetAllo.setMonth(month);
			this.workSheetAlllot.saveWorkSheetAllotReal(workSheetAllo, false);

			// 删除当前表的信息
			this.sheetActionInfoDao.delSheetActionByOrderId(orderId,month);
			this.sheetPubInfoDao.delSheetPubInfoByOrderId(orderId,month);
			this.sheetPubInfoDao.deleteWorkSheetAreaByOrderId(orderId);
			this.serviceContentDao.delServContent(orderId,month);
			this.orderAskInfoDao.delOrderAskInfo(orderId,month);
			this.orderCustInfoDao.delOrderCustInfo(custGuid,month);
			this.workSheetAlllot.deleteSheetAlloReal(orderId, month);
			this.tsWorkSheetDao.deleteSheetApply(orderId);//工单挂起和释放申请从历史表删除
			this.forceDistillDao.insertForceDistillHisByOrderId(orderId);
			this.saveSheetHiddenHis(orderId,orderAskInfo.getServType());
			
			//投诉单 工信部、省管局
			this.finishComplaintInfo(orderAskInfo, orderId);
			//投诉单 定性信息更新归档状态
			this.updateTsSheetQualitative(orderAskInfo.getServType(),orderId);
			//商机单
			if(orderAskInfo.getServType() == StaticData.SERV_TYPE_SJ) {
				sjSheetQualitative.saveSJSheetQualitativeHis(orderId);
				serviceContentType.deleteContentSaveSJ(orderId);
			}
			if(orderAskInfo.getServType() == StaticData.SERV_TYPE_GZ) {
				orderRefundService.updateArchiveStatus(orderId);
			}
			//投诉流程
			if(orderAskInfo.getServiceDate() == 3) {
				this.tsWorkSheetDeal.orderShetFinish(orderId, orderAskInfo.getRegionId());
				serviceContentSchem.insertServiceContentSaveHis(orderId);
				serviceContentSchem.insertDealContentSaveHis(orderId);
				serviceContentType.finishAnalysisInfo(orderId);
				//关联录单记录状态变更
				this.orderAskInfoDao.updateOrderRelationFinish(orderId);
			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//			throw new RuntimeException(ex.getMessage());
//		}

		Map outParam = new HashMap();
		return outParam;
	}

	/**
	 * 服务单以及其所有工单等信息进历史
	 * 
	 * @param orderId 订单号
	 * @return
	 */
	// @Transactional(rollbackFor=Exception.class)
	public Map finishOrderAndSheetByOrderId(String orderId) {
		Map map = new HashMap();
		String code = "1";
		String msg = "已完成";
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskOver180Day(orderId);
		if (null == orderAskInfo) {
			code = "2";
			msg = "该订单不存在或已归档或在180天内";
		} else {
			Integer month = orderAskInfo.getMonth();
			String custGuid = orderAskInfo.getCustId();// 将当前表定单中客户的guid查出来
			// 将当前表的信息保存到历史表中
			try {
				orderCustInfoDao.saveOrderCustHis(custGuid, month);// 保存客户信息到历史表
				orderAskInfoDao.saveOrderAskInfoHis(orderId, month);// 保存受理信息到历史表
				serviceContentDao.saveServContentHis(orderId, month);// 保受量内容到历史表
				labelManageDAO.saveLabelHisById(orderId);// 保存标签到历史表
				labelManageDAO.insertRuyiLabelHis(orderId);// 保存如意标识到历史表
				personaDao.savePersonaHis(orderId);
				cmpUnifiedContactDAOImpl.saveUnifiedContactHisByOrderId(orderId);
				cmpUnifiedRepeatDAOImpl.saveUnifiedRepeatHisByCurSoi(orderId);
				ComplaintUnifiedReturn cur = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(orderId);
				if (null != cur) {
					cmpUnifiedReturnDAOImpl.saveUnifiedReturnHisByOrderId(orderId);
				}
				clqUnifiedWeixinDAOImpl.saveUnifiedShowHisByOrderId(orderId);
				sheetMistakeDAO.insertOrderMistakeHisByOrderId(orderId);
				sheetPubInfoDao.insertXcFlowHis(orderId);
				sheetPubInfoDao.saveSheetPubInfoHis(orderId, month);// 此定单下所有的工单到历史表
				sheetPubInfoDao.finishComplaintConnection(orderId);
				sheetActionInfoDao.saveSheetActionHisInfo(orderId, month);// 保存工单运作到历史
				sheetActionInfoDao.saveSheetHiddenActionHisByOrderId(orderId);// 保存隐藏运作到历史
				hastenSheetServ.saveHastenSheetInfoHis(orderId, month); // 保存催单到历史表
				WorkSheetStatuApplyInfo sheetApply = new WorkSheetStatuApplyInfo();
				sheetApply.setOrderId(orderId);
				sheetApply.setMonth(month);
				tsWorkSheetDao.saveSheetApply(sheetApply, false);// 工单挂起和释放申请进历史表
				// 如果要删除派单关系表数据
				WorkSheetAllotReal workSheetAllo = new WorkSheetAllotReal();
				workSheetAllo.setOrderId(orderId);
				workSheetAllo.setMonth(month);
				workSheetAlllot.saveWorkSheetAllotReal(workSheetAllo, false);
				// 删除当前表的信息
				sheetActionInfoDao.delSheetActionByOrderId(orderId, month);
				sheetPubInfoDao.delSheetPubInfoByOrderId(orderId, month);
				sheetPubInfoDao.deleteWorkSheetAreaByOrderId(orderId);
				serviceContentDao.delServContent(orderId, month);
				orderAskInfoDao.delOrderAskInfo(orderId, month);
				orderCustInfoDao.delOrderCustInfo(custGuid, month);
				workSheetAlllot.deleteSheetAlloReal(orderId, month);
				tsWorkSheetDao.deleteSheetApply(orderId);// 工单挂起和释放申请从历史表删除
				forceDistillDao.insertForceDistillHisByOrderId(orderId);
				saveSheetHiddenHis(orderId, orderAskInfo.getServType());
				// 投诉单 工信部、省管局
				finishComplaintInfo(orderAskInfo, orderId);
				// 商机单
				if (orderAskInfo.getServType() == StaticData.SERV_TYPE_SJ) {
					sjSheetQualitative.saveSJSheetQualitativeHis(orderId);
					serviceContentType.deleteContentSaveSJ(orderId);
				}
				if (orderAskInfo.getServType() == StaticData.SERV_TYPE_GZ) {
					orderRefundService.updateArchiveStatus(orderId);
				}
				// 投诉流程
				if (orderAskInfo.getServiceDate() == 3) {
					tsWorkSheetDeal.orderShetFinish(orderId, orderAskInfo.getRegionId());
					serviceContentSchem.insertServiceContentSaveHis(orderId);
					serviceContentSchem.insertDealContentSaveHis(orderId);
					pubFunc.archiveCmpRelationByOrderId(orderId);
				}
			} catch (Exception e) {
				code = "3";
				msg = e.getMessage();
			}
		}
		map.put("code", code);
		map.put("msg", msg);
		return map;
	}

	/**
	 * 删除隐藏表（当前），插入历史表
	 * */
	private void saveSheetHiddenHis(String orderId,int servType){
		logger.info("saveSheetHiddenHis serviceOrderId: {}",orderId);
		try{
			if(720200003 == servType || 720130000 == servType){
				this.sheetActionInfoDao.saveSheetHideenHis(orderId);
			}
		}catch (Exception e){
			logger.error("saveSheetHiddenHis error: {}",e.getMessage(),e);
		}
	}
	
	private void updateBestOrder(OrderAskInfo orderAskInfo, String orderId) {
		if(orderAskInfo.getServType() != StaticData.SERV_TYPE_NEWTS) {
			return;
		}
		
		ServiceContent content = serviceContentDao.getServContentByOrderId(orderId, false, 0);
		logger.info("orderId: {} version: {} bestOrder: {}", orderId, content.getOrderVer(), content.getBestOrder());
		int num = 0;
		if(content.getBestOrder() > 100122410) {
			Map lastDealInfo = pubFunc.getLastDealInfo(orderId);
			if(lastDealInfo != null) {
				String lastDealOrg = this.defaultMapValueIfNull(lastDealInfo, "DEAL_ORG_ID", "");
				logger.info("lastDealOrgId: {}", lastDealOrg);
				if(pubFunc.isAffiliated(lastDealOrg, "11")) {//全渠道下属部门
					num = serviceContentDao.updateBestOrder(orderId);
					if(num > 0) {
						logger.info("归档取消最严工单标识: {}", orderId);
						pubFunc.saveOrderOperation(orderId, 2);
						//取消最严客户工单重复
						labelManageDAO.updateRepeatBestFlag(orderId);
					}
				}
			}
		}
		//保存最严工单修改记录
		this.saveBestOrderModify(num, orderId, orderAskInfo.getAskDate(), content.getOrderVer(), content.getBestOrder());
	}
	
	private void saveBestOrderModify(int num, String orderId, String accpetDate, int orderVer, int bestOrder) {
		try {
			int bestOrderOrigin;//最严工单（初始）
			if(orderVer > 0) {//工单修改
				ServiceContent hisContent = serviceContentDao.getServContentByOrderId(orderId, true, 0);//初始受理内容
				if(hisContent == null) {
					return;
				}
				bestOrderOrigin = hisContent.getBestOrder();
			} else {
				bestOrderOrigin = bestOrder;
			}
			int bestOrderFinal = num > 0 ? 100122410 : bestOrder;//最严工单（最终）
			int modifyType = 0;//1-最严改否；2-否改最严；3-归档取消最严
			if(bestOrderOrigin > 100122410 && bestOrderFinal <= 100122410) {
				modifyType = 1;
				if(num > 0) {
					modifyType = 3;
				}
			} else if(bestOrderOrigin <= 100122410 && bestOrderFinal > 100122410) {
				modifyType = 2;
			}
			logger.info("saveBestOrderModify orderId: {} bestOrderOrigin: {} bestOrderFinal: {} modifyType: {}", orderId, bestOrderOrigin, bestOrderFinal, modifyType);
			if(modifyType > 0) {
				serviceContentDao.saveBestOrderModify(orderId, PubFunc.dbDateToStr(accpetDate), bestOrderOrigin, bestOrderFinal, modifyType);
			}
		} catch (Exception e) {
			logger.error("saveBestOrderModify error: {}", e.getMessage(), e);
        }
	}
	
	private void updateTsSheetQualitative(int servType, String orderId) {
		if(servType == StaticData.SERV_TYPE_NEWTS) {
			//更新扣罚项目
			sheetQualitative.updateOutlets(orderId);
			//工信部并案信息
			sheetQualitative.updateOrderChannel(orderId);
		}
	}
	
	private void finishComplaintInfo(OrderAskInfo orderAskInfo, String orderId) {
		if(orderAskInfo.getServType() == StaticData.SERV_TYPE_NEWTS && (orderAskInfo.getAskChannelId() == 707907026 || orderAskInfo.getAskChannelId() == 707907027)) {
			this.orderAskInfoDao.insertComplaintInfoHisByOrderId(orderId);
		}
	}

	private void insertCustomerJudgeHisByOrderId(String orderId) {
		Map cj = sheetPubInfoDao.selectCustomerJudgeByOrderId(orderId);
		if (!cj.isEmpty()) {
			String tacheType = cj.get("TACHE_TYPE").toString();
			String judgeStatus = cj.get("JUDGE_STATUS").toString();
			if (!judgeZDHFHis(tacheType) || !"2".equals(judgeStatus)) {
				sheetPubInfoDao.insertCustomerJudgeHisByOrderId(orderId);
			}
		}
	}

	// 咨询单、商机单自动回访中的数据暂时不归档，等获取到自动回访结果再归档
	private boolean judgeZDHFHis(String tacheType) {
		return "3".equals(tacheType) || "4".equals(tacheType) || "5".equals(tacheType);
	}

	/**
	 * 到得到路由条件值
	 * @param inParam	工作流传入的参数
	 * @return	条件值map
	 */
	public Map getRouteInfo(Map inParam){
		return inParam;
	}
	
	/**
	 * 更新部门处理单路由条件值
	 * @param inParam	工作流传入的参数
	 * @return	条件值map
	 */
	public Map updateDealSheetRouteInfo(Map inParam){
/*		//先将工单至为完成状态
		this.updateSheetFinish(inParam);
		String orderId = inParam.get("SERV_ORDER_ID").toString();
		String currWFNodeInstId = inParam.get("WF__NODE_INSTANCE__ID").toString();
		
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getTheLastSheetInfo(orderId);
		String newWFNodeInstId = sheetPubInfo.getTacheInstId();
		Map outParam = new HashMap();
		//如果当前流程环节和此定单下最新的工单的环节实例id不一样,则说明已经产生了审核工单
		if(!currWFNodeInstId.equals(newWFNodeInstId)){
			outParam.put("ROUTE_VALUE",StaticData.ROUTE_DEAL_TO_USELESS);
		}*/
		//Map outParam = new HashMap();
		inParam.put(ROUTE_VALUE,StaticData.ROUTE_GOTO_NEXT);
		return inParam;
	}
	
	/**
	 * 审核环节出方法
	 * @param inParam	工作流传入的参数
	 * @return	条件值map
	 */
	public Map updateAuitSheetFinish(Map inParam) {
		// 先将自身至为完成状态
		this.updateSheetFinish(inParam);
		String orderId = inParam.get(SERV_ORDER_ID).toString();
		String month = inParam.get(MONTH_FALG).toString();
		int state = StaticData.WKST_FINISH_STATE;
		String stateDesc = pubFunc.getStaticName(state);			
		this.sheetPubInfoDao.updateTachSheetFinsh(orderId, state, stateDesc, 2, Integer.valueOf(month), StaticData.TACHE_DEAL);
		return new HashMap();

	}

	/**
	 * 从来源受理单取得信息并组装工单公共信息对象
	 * @param inParam来源受理单的相关信息
	 * @return 工单公共信息对象
	 */
	@Resource
	private JdbcTemplate jdbcTemplate;
	
	@SuppressWarnings("all")
	private List getSheetPubInfoFromOrder(Map inParam) {
		// 取得来源定单号
		String srcOrderId = inParam.get(SERV_ORDER_ID).toString();
		String strMonth = inParam.get(MONTH_FALG).toString();
		Integer month = Integer.valueOf(strMonth);

		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskObj(srcOrderId,month,false);
		if (orderAskInfo == null) {
			logger.warn("没有查询到定单号为" + srcOrderId + "的受理单!");
			return null;
		}
		/* =================以下代码取得工单需要的其它信息========= */
		//取来源定单号,如果没有来源定单号则取来源工单号
		String srcSheetId = srcOrderId;
		if(inParam.get(SHEET_ID) != null){
			srcSheetId = inParam.get(SHEET_ID).toString();
		}
		
		String wfInstId = inParam.get("WF__INSTANCE__ID").toString();//工作流实例
		String wfNodeInstId = inParam.get("WF__NODE_INSTANCE__ID").toString();//环节实例

		int tacheId = Integer.parseInt(inParam.get("TACHE_ID").toString());
		if (tacheId == StaticData.TACHE_DINGXING_NEW || tacheId == StaticData.TACHE_ZHONG_DINGXING_NEW) {
			sheetPubInfoDao.updateWorkSheetAreaTacheDate(srcOrderId);
		}
		String tacheDesc = pubFunc.getStaticName(tacheId);
		int servType = orderAskInfo.getServType();
		int regionId = orderAskInfo.getRegionId();
		int sheetType = PubFunc.getSheetType(servType,tacheId);
		if(tacheId==StaticData.TACHE_DEAL_NEW){ //2013-02 如果环节为新投诉的部门处理 
            SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetPubInfo(srcSheetId,false);
            if(StaticData.SHEET_TYPE_TS_ASSING_NEW==sheetInfo.getSheetType() || StaticData.SHEET_TYPE_FINASSESS==sheetInfo.getSheetType()){
                sheetType = StaticData.SHEET_TYPE_TS_DEAL_NEW;
            }
		}
		String require = inParam.containsKey(DEAL_REQUIRE) ? inParam.get(DEAL_REQUIRE).toString(): ""; //取处理要求

		//取得工单的优先级,预告警,时限信息
		int sheetPriValue = 0;//优先权重值
		int dealLimitTime = 0;
		int preAlarmValue = 0;
		int alarmValue = 0;
		//计算优先级和预告警时间
		OrderCustomerInfo custInfo = orderCustInfoDao.getOrderCustByOrderId(srcOrderId);
    	ServiceContent cont = serviceContentDao.getServContentByOrderId(srcOrderId, false, 0);
		if(custInfo != null){
			//计算优先级
			int[] queryInfo = new int[4];
			queryInfo[0] = servType;//服务类型
			queryInfo[1] = custInfo.getCustBrand();//客户品牌
			queryInfo[2] = orderAskInfo.getCustServGrade();//服务等级

			SheetLimitTimeCollocate limitBean = sheetLimitTimeServ.getSheetLimitime(
					orderAskInfo.getRegionId(),
					orderAskInfo.getServType(), tacheId, 1,
					orderAskInfo.getCustServGrade(),
					orderAskInfo.getUrgencyGrade()
					);
			if(limitBean != null) {
				dealLimitTime = limitBean.getLimitTime();
				preAlarmValue = limitBean.getPrealarmValue();
			}
		}
		// 派发部门
		String disDealOrgId = inParam.containsKey(DEAL_PR_ORGID) ? inParam.get(DEAL_PR_ORGID).toString() : "";
		String disDealOrgName = pubFunc.getOrgName(disDealOrgId);
		// 派发员工
		String retStaffId= inParam.containsKey(DEAL_PR_STAFFID) ? inParam.get(DEAL_PR_STAFFID).toString() : "0";
		int retStaId = Integer.parseInt(retStaffId);
		String retStaName = pubFunc.getStaffName(retStaId);
		// 流水顺序号
        String flowSeq="1";
        if(inParam.containsKey(FLOW_SEQUENCE)){
            flowSeq=inParam.get(FLOW_SEQUENCE).toString();
        }else{
            String sql="SELECT W.FLOW_SEQUENCE FROM CC_WORK_SHEET W WHERE W.REGION_ID=" +
                    regionId+" AND W.SERVICE_ORDER_ID='" +srcOrderId+"' ORDER BY W.FLOW_SEQUENCE DESC LIMIT 1";
            Map tm=jdbcTemplate.queryForMap(sql);
            flowSeq=tm.get(FLOW_SEQUENCE).toString();
            flowSeq=pubFunc.crtFlowSeq(flowSeq, "1", 1);
        }

		/* ==============以下代码为组装工单信息======================== */
		//派往的部门处理环节的部门
		SheetPubInfo[] workSheetObj = inParam.containsKey("SHEETARRAY") ? (SheetPubInfo[]) inParam.get("SHEETARRAY") : new SheetPubInfo[0];
		int size = workSheetObj.length;
		List sheetPubInfoList =  new ArrayList();
		do {
			// 生成工单,并设置属性		
			SheetPubInfo sheetPubInfo = new SheetPubInfo();
			String sheetId = pubFunc.crtSheetId(orderAskInfo.getRegionId());
			sheetPubInfo.setWorkSheetId(sheetId);
			sheetPubInfo.setServiceOrderId(srcOrderId);
			sheetPubInfo.setRegionId(regionId);
			sheetPubInfo.setRegionName(orderAskInfo.getRegionName());
			sheetPubInfo.setServType(servType);
			sheetPubInfo.setServTypeDesc(orderAskInfo.getServTypeDesc());
			sheetPubInfo.setSourceSheetId(srcSheetId);
			sheetPubInfo.setTacheId(tacheId);
			sheetPubInfo.setTacheDesc(tacheDesc);
			sheetPubInfo.setWflInstId(wfInstId);
			sheetPubInfo.setTacheInstId(wfNodeInstId);
			sheetPubInfo.setSheetType(sheetType);
			sheetPubInfo.setSheetTypeDesc(pubFunc.getStaticName(sheetType));
			sheetPubInfo.setSheetPriValue(sheetPriValue);
			sheetPubInfo.setDealLimitTime(dealLimitTime);
			sheetPubInfo.setStationLimit(dealLimitTime);
			sheetPubInfo.setPreAlarmValue(preAlarmValue);
			sheetPubInfo.setAlarmValue(alarmValue);
			sheetPubInfo.setDealOrgId(NULLORG);//提前设置处理部门
			sheetPubInfo.setFlowSequence(flowSeq);//流程流水号
			flowSeq=pubFunc.crtFlowSeq(flowSeq, "1", 1);
			String orgId = "";//收单部门ID
			String orgName = "";//收单部门名字
			//部门处理时候,把主单传给审核环节插入到派单关系表中,就可以确定主单标示
			if(size > 0) {
				//如果为直接派到个人,还要更新处理部门和员工
				if(workSheetObj[size-1].getRcvOrgId().equals("STFFID")) {
					int dalStaffId = workSheetObj[size-1].getRcvStaffId();
					orgId = this.pubFunc.getStaffOrgName(dalStaffId);
					orgName = pubFunc.getOrgName(orgId);
					String staffName = this.pubFunc.getStaffName(dalStaffId);
					sheetPubInfo.setRcvOrgId(orgId);
					sheetPubInfo.setRcvOrgName(orgName);
					
					//派发到个人,处理员工为个人
					sheetPubInfo.setDealOrgId(orgId);
					sheetPubInfo.setDealOrgName(orgName);
					sheetPubInfo.setDealStaffId(dalStaffId);
					sheetPubInfo.setDealStaffName(staffName);
					sheetPubInfo.setRcvStaffId(dalStaffId);
					sheetPubInfo.setRcvStaffName(staffName);
				} else {
					orgId = workSheetObj[size-1].getRcvOrgId();
					orgName = pubFunc.getOrgName(orgId);
					sheetPubInfo.setRcvOrgId(orgId);
					sheetPubInfo.setRcvOrgName(orgName);
					sheetPubInfo.setDealOrgId(NULLORG);//说明不是直接派单到个人
				}
				sheetPubInfo.setMainType(workSheetObj[size-1].getMainType());				
			}
			
			
			if(orderAskInfo.getServiceDate()==1 && inParam.containsKey("SEND_TO_ORG_ID") && inParam.get("SEND_TO_ORG_ID")!=null 
					&& !inParam.get("SEND_TO_ORG_ID").toString().trim().equals("")){
				logger.info("客户经理归属地为省政企、商机单");
				// 客户经理归属地为省政企、商机单
				orgId = inParam.get("SEND_TO_ORG_ID").toString().trim();
				Map orgMap = new HashMap();
				orgMap.put("FLOW_ORG", orgId);
				sheetPubInfo = FlowOrgFactory.factoryMethod("tsFlowOrg").setSheetOrg(sheetPubInfo, orgMap);
			}else if(orderAskInfo.getServType() == StaticData.SERV_TYPE_GZ && inParam.containsKey("SEND_TO_OBJ_FLAG")
					&& inParam.get("SEND_TO_OBJ_FLAG") != null && !inParam.get("SEND_TO_OBJ_FLAG").toString().trim().equals("")){
				// 跟踪单受理 直派部门或员工
				if(inParam.containsKey("SEND_TO_OBJ_ID") && inParam.get("SEND_TO_OBJ_ID") != null 
						&& !inParam.get("SEND_TO_OBJ_ID").toString().equals("")) {
					if("1".equals(inParam.get("SEND_TO_OBJ_FLAG").toString())) {//1-部门 0-员工
						sheetPubInfo.setRcvOrgId(inParam.get("SEND_TO_OBJ_ID").toString());
						sheetPubInfo.setRcvOrgName(pubFunc.getOrgName(inParam.get("SEND_TO_OBJ_ID").toString()));
					}else if("0".equals(inParam.get("SEND_TO_OBJ_FLAG").toString())) {
						TsmStaff rcvStaff = this.pubFunc.getStaff(Integer.parseInt(inParam.get("SEND_TO_OBJ_ID").toString()));
						sheetPubInfo.setRcvOrgId(rcvStaff.getOrganizationId());
						sheetPubInfo.setRcvOrgName(rcvStaff.getOrgName());
						sheetPubInfo.setRcvStaffId(Integer.parseInt(rcvStaff.getId()));
						sheetPubInfo.setRcvStaffName(rcvStaff.getName());
						sheetPubInfo.setDealOrgId(rcvStaff.getOrganizationId());
						sheetPubInfo.setDealOrgName(rcvStaff.getOrgName());
						sheetPubInfo.setDealStaffId(Integer.parseInt(rcvStaff.getId()));
						sheetPubInfo.setDealStaffName(rcvStaff.getName());
					}
				}
			}else if(tacheId == StaticData.TACHE_RGHF){// 2020-10 人工回访的收单部门
				sheetPubInfo.setSheetRcvDate(pubFunc.getSysDateFormat("%Y-%m-%d %H:%i:%s"));
				sheetPubInfo.setDealLimitTime(6);
				sheetPubInfo.setStationLimit(6);
				if (inParam.containsKey("RGHF_ORG_ID")) {
					orgId = inParam.get("RGHF_ORG_ID").toString();
					orgName = pubFunc.getOrgName(orgId);
					sheetPubInfo.setRcvOrgId(orgId);
					sheetPubInfo.setRcvOrgName(orgName);
					if (inParam.containsKey("RGHF_STAFF_ID")) {
						int rcvStaffId = Integer.parseInt(inParam.get("RGHF_STAFF_ID").toString());
						TsmStaff rcvStaff = this.pubFunc.getStaff(rcvStaffId);
						sheetPubInfo.setRcvStaffId(rcvStaffId);
						sheetPubInfo.setRcvStaffName(rcvStaff.getName());
						sheetPubInfo.setDealStaffId(rcvStaffId);
						sheetPubInfo.setDealStaffName(rcvStaff.getName());
						sheetPubInfo.setDealOrgId(rcvStaff.getOrganizationId());
						sheetPubInfo.setDealOrgName(rcvStaff.getOrgName());
						sheetPubInfo.setLockFlag(1);
						sheetPubInfo.setSheetStatu(StaticData.WKST_DEALING_STATE_NEW);
						sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_DEALING_STATE_NEW));
					}
				} else {
					SheetPubInfo sheetPubInfoHTPD = sheetPubInfoDao.queryLastSheetNoSystemByType(sheetPubInfo.getServiceOrderId(), StaticData.SHEET_TYPE_TS_ASSING_NEW,
							0);
					if (null != sheetPubInfoHTPD) {
						TsmStaff rcvStaff = this.pubFunc.getStaff(sheetPubInfoHTPD.getDealStaffId());
						sheetPubInfo.setRcvStaffId(sheetPubInfoHTPD.getDealStaffId()); // 收单员工ID
						sheetPubInfo.setRcvStaffName(rcvStaff.getName()); // 收单员工姓名
						sheetPubInfo.setRcvOrgId(sheetPubInfoHTPD.getRcvOrgId());
						sheetPubInfo.setRcvOrgName(sheetPubInfoHTPD.getRcvOrgName());
						sheetPubInfo.setDealStaffId(sheetPubInfoHTPD.getDealStaffId()); // 处理员工ID，即收单员工ID
						sheetPubInfo.setDealStaffName(rcvStaff.getName()); // 处理员工名，即收单员工名
						sheetPubInfo.setDealOrgId(rcvStaff.getOrganizationId()); // 处理单位ID，即收单员工所在部门ID
						sheetPubInfo.setDealOrgName(rcvStaff.getOrgName()); // 处理单位名称，即收单员工所在部门名称
						sheetPubInfo.setLockFlag(1); // Lock置为1
						sheetPubInfo.setSheetStatu(StaticData.WKST_DEALING_STATE_NEW);
						sheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_DEALING_STATE_NEW));
						sheetPubInfoHTPD = null;
					}
				}
			}else if(tacheId == StaticData.TACHE_DINGXING_NEW){
				sheetPubInfo = setYDXFlow(orderAskInfo, cont, sheetPubInfo);
            }else if(CompatHandler.isTachOrderBack(tacheId)) {
                // 退单到前台的工单，收单部门取订单的受理部门
                String strOrderStatu = inParam.containsKey(ORDER_ID_STATU) ? inParam.get(ORDER_ID_STATU).toString() : "0";
                int orderStatu = Integer.parseInt(strOrderStatu);
                if(CompatHandler.isOStatuBack(orderStatu)) {
                    sheetPubInfo.setRcvOrgId(orderAskInfo.getAskOrgId());
                    sheetPubInfo.setRcvOrgName(orderAskInfo.getAskOrgName());
                }
            }else if(!CompatHandler.isTachOrgDeal(tacheId)) {//后台派单、终定性
	            // 如果为部门处理环节，就不去找对应的工单流向规则 (审核环节生成工单crtAudWorkSheet)
	            sheetPubInfo = getFlowOrgId(sheetPubInfo,orderAskInfo,sheetPubInfo,inParam);
	            
	            // 投诉派单工单
				if(sheetPubInfo.getSheetType() == StaticData.SHEET_TYPE_TS_ASSING_NEW && size==0){
					/* 以下代码  -------------     2014-07-02 需求单号796875 */
					switch(orderAskInfo.getAskChannelId()){
						case StaticData.CHANNEL_JT_ZS15:
						case StaticData.CHANNEL_JT_ZS16:
						case StaticData.CHANNEL_JT_ZS17:
						case StaticData.CHANNEL_JT_ZS18:
							int hour = Integer.parseInt(pubFunc.getSysDateFormat("%H"));
							if((hour >= 18 || hour < 8) && !StaticData.ACPT_ORGID_JT.equals(orderAskInfo.getAskOrgId())){
								sheetPubInfo.setRcvOrgId(StaticData.ORG_NOV_NOC);
								sheetPubInfo.setRcvOrgName(pubFunc.getOrgName(StaticData.ORG_NOV_NOC));
							}
							break;
					}
					/* 以上代码  -------------     2014-07-02 需求单号796875 */
				}
				
				//商机派单工单
				this.setSJAssignFlow(sheetPubInfo, cont, size);
				
				//预受理派单工单
				this.setYSAssignFlow(sheetPubInfo, inParam);
				
				//省内投诉特殊目录流向
				boolean rcvFlag = this.checkPhenomenonTypeId(orderAskInfo, sheetPubInfo);
				if(!rcvFlag) {
	            	/***** 特殊分派 *****/
					if(inParam.containsKey("WORKSHEET_ALLOT") && inParam.get("WORKSHEET_ALLOT") != null) {
						String key = inParam.get("WORKSHEET_ALLOT").toString();
						logger.info("WORKSHEET_ALLOT: {}", key);
						Map config = pubFunc.getSheetAllotConfig(sheetPubInfo.getServType(), sheetPubInfo.getTacheId(), key, String.valueOf(orderAskInfo.getRegionId()));
						if(config != null) {
							int receiveStaff = Integer.parseInt(config.get("RECEIVE_STAFF") == null ? "0" : config.get("RECEIVE_STAFF").toString());
							String receiveOrg = config.get("RECEIVE_ORG") == null ? "" : config.get("RECEIVE_ORG").toString();
							if(receiveStaff != 0) {
								TsmStaff tsmStaff = pubFunc.getStaff(receiveStaff);
								sheetPubInfo.setRcvOrgId(tsmStaff.getOrganizationId());
								sheetPubInfo.setRcvOrgName(tsmStaff.getOrgName());
								sheetPubInfo.setRcvStaffId(Integer.parseInt(tsmStaff.getId()));
								sheetPubInfo.setRcvStaffName(tsmStaff.getName());
								sheetPubInfo.setDealOrgId(tsmStaff.getOrganizationId());
								sheetPubInfo.setDealOrgName(tsmStaff.getOrgName());
								sheetPubInfo.setDealStaffId(Integer.parseInt(tsmStaff.getId()));
								sheetPubInfo.setDealStaffName(tsmStaff.getName());
							}
							else if (!"".equals(receiveOrg)) {
								sheetPubInfo.setRcvOrgId(receiveOrg);
								sheetPubInfo.setRcvOrgName(pubFunc.getOrgName(receiveOrg));
							}
						}
					} else {
						//工单直派
						this.checkDealStaffBySixCatalog(orderAskInfo, cont, sheetPubInfo);
					}
				}
			}
			
			sheetPubInfo.setReportNum(0);
			//综调绿色通道或疑难工单的一级目录为号百,去流向里找工位
			String audFlag="0";
			if(inParam.containsKey(AUD_FLAG)){
				audFlag = inParam.get(AUD_FLAG).toString();
				if(audFlag.equals("1")) {
					//得到工位
					sheetPubInfo.setMainType(1);
					sheetPubInfo = getFlowOrgId(sheetPubInfo,orderAskInfo,sheetPubInfo,inParam);
				}
			}
			
			sheetPubInfo.setDealRequire(require);//加上要求填写人的名字,电话 
			sheetPubInfo.setAutoVisitFlag(0);
			//如果为派单的工单,部门处理工单不是通过转派的工单 precontractSign标志为1
			if(CompatHandler.isTachOrgDeal(tacheId)) {
				String  precontractsign ="1";
				if (inParam.containsKey("PRECONTRACTSIGN")) {
					precontractsign = inParam.get("PRECONTRACTSIGN").toString();//0为部门转派工单，1为后台派单 5 审核重新派单 3 为审核退单，4为部门审批退单
				}
				sheetPubInfo.setPrecontractSign(Integer.parseInt(precontractsign));
				sourceAllotByConfigSecondBMCL(orderAskInfo, cont, sheetPubInfo);
				if (sheetPubInfo.getMainType() == 1 && pubFunc.isNewWorkFlow(srcOrderId) && orderAskInfo.getServiceDate() == 3) {
					SheetPubInfo upSheetInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetPubInfo.getSourceSheetId(), false);
					if (pubFunc.getAreaOrgId(upSheetInfo.getRcvOrgId()).equals(pubFunc.getAreaOrgId(sheetPubInfo.getRcvOrgId()))) { // 非跨二级部门派单
						sheetPubInfoDao.insertWorkSheetArea(srcOrderId, sheetPubInfo.getWorkSheetId(), pubFunc.getAreaOrgId(sheetPubInfo.getRcvOrgId()), 2);
					} else {
						sheetPubInfoDao.insertWorkSheetArea(srcOrderId, sheetPubInfo.getWorkSheetId(), pubFunc.getAreaOrgId(sheetPubInfo.getRcvOrgId()), 1);
					}
					complaintImpl.complaintPostInfo(2, srcOrderId);
				}
			}
			if (pubFunc.isNewWorkFlow(srcOrderId) && orderAskInfo.getServiceDate() == 3) {
				if (StaticData.TACHE_ASSIGN_NEW == tacheId || StaticData.TACHE_ASSIGN == tacheId) {
					sheetPubInfo.setSheetRcvDate(orderAskInfoDao.selectAcceptDate(srcOrderId));
					sheetPubInfo.setDealLimitTime(labelManageDAO.selectDealHours(srcOrderId));
					sheetPubInfo.setStationLimit(labelManageDAO.selectDealHours(srcOrderId));
				}
				if (CompatHandler.isTachOrgDeal(tacheId)) {
					String zdxCpDate = labelManageDAO.selectZdxCpDate(srcOrderId);
					if (null != zdxCpDate) {
						sheetPubInfo.setSheetRcvDate(zdxCpDate);
					} else {
						String acceptDate = orderAskInfoDao.selectAcceptDate(srcOrderId);
						if (null != acceptDate) {
							sheetPubInfo.setSheetRcvDate(acceptDate);
						}
					}
					sheetPubInfo.setDealLimitTime(labelManageDAO.selectDealHours(srcOrderId));
					sheetPubInfo.setStationLimit(labelManageDAO.selectDealHours(srcOrderId));
				}
				if (tacheId == StaticData.TACHE_DINGXING_NEW) {
					String zdxCpDate = labelManageDAO.selectZdxCpDate(srcOrderId);
					if (null != zdxCpDate) {
						sheetPubInfo.setSheetRcvDate(zdxCpDate);
					} else {
						String acceptDate = orderAskInfoDao.selectAcceptDate(srcOrderId);
						if (null != acceptDate) {
							sheetPubInfo.setSheetRcvDate(acceptDate);
						}
					}
					sheetPubInfo.setDealLimitTime(labelManageDAO.selectDealHours(srcOrderId));
					sheetPubInfo.setStationLimit(labelManageDAO.selectDealHours(srcOrderId));
				}
				if (StaticData.TACHE_ZHONG_DINGXING_NEW == tacheId) {
					labelManageDAO.updateFirstAuditDate(srcOrderId);
					labelManageDAO.updateLastAuditDate(srcOrderId);
					String lastAuditDate = labelManageDAO.selectLastAuditDate(srcOrderId);
					if (null != lastAuditDate) {
						sheetPubInfo.setSheetRcvDate(lastAuditDate);
					} else {
						String firstAuditDate = labelManageDAO.selectFirstAuditDate(srcOrderId);
						if (null != firstAuditDate) {
							sheetPubInfo.setSheetRcvDate(firstAuditDate);
						}
					}
					sheetPubInfo.setDealLimitTime(labelManageDAO.selectAuditHours(srcOrderId));
					sheetPubInfo.setStationLimit(labelManageDAO.selectAuditHours(srcOrderId));
				}
			}
			// 记录派发员工信息
			sheetPubInfo.setRetOrgId(disDealOrgId);
			sheetPubInfo.setRetOrgName(disDealOrgName);
			sheetPubInfo.setRetStaffId(retStaId);
			sheetPubInfo.setRetStaffName(retStaName);
			if(sheetPubInfo.getRcvOrgId() !=null) {
				int recOrgRegion = this.pubFunc.getOrgRegion(sheetPubInfo.getRcvOrgId());
				String recOrgRegionName = this.pubFunc.getRegionName(recOrgRegion);
				sheetPubInfo.setReceiveRegionId(recOrgRegion);
				sheetPubInfo.setReceiveRegionName(recOrgRegionName);				
			}			
			sheetPubInfo.setMonth(month);
			sheetPubInfoList.add(sheetPubInfo);
			size = size -1;
		} while (size > 0);
		inParam.put(FLOW_SEQUENCE, flowSeq);
		sheetPubInfoList.add(orderAskInfo);
		return sheetPubInfoList;
	}
	
	/**
	 * 特殊目录配置优先于其他规则
	 * @param orderAskInfo
	 * @param cont
	 * @param sheetPubInfo
	 */
	private boolean checkPhenomenonTypeId(OrderAskInfo orderAskInfo, SheetPubInfo sheetPubInfo) {
		boolean rcvFlag = false;
		if(StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType() //投诉
				&& StaticData.getAskLevelId()[0] == orderAskInfo.getComeCategory() //省内投诉
				&& StaticData.TACHE_ASSIGN_NEW == sheetPubInfo.getTacheId() //后台派单
				&& 1059901 == orderAskInfo.getRelaType()) { //规则政策类 > 省自定 > 敏感话务
			Map config = pubFunc.getSheetAllotConfig(orderAskInfo.getServType(), sheetPubInfo.getTacheId(), 
					String.valueOf(orderAskInfo.getRelaType()), String.valueOf(orderAskInfo.getComeCategory()));
			if(config != null) {
				int receiveStaff = Integer.parseInt(config.get("RECEIVE_STAFF") == null ? "0" : config.get("RECEIVE_STAFF").toString());
				String receiveOrg = config.get("RECEIVE_ORG") == null ? "" : config.get("RECEIVE_ORG").toString();
				if(receiveStaff != 0) {
					rcvFlag = true;
					TsmStaff tsmStaff = pubFunc.getStaff(receiveStaff);
					sheetPubInfo.setRcvOrgId(tsmStaff.getOrganizationId());
					sheetPubInfo.setRcvOrgName(tsmStaff.getOrgName());
					sheetPubInfo.setRcvStaffId(Integer.parseInt(tsmStaff.getId()));
					sheetPubInfo.setRcvStaffName(tsmStaff.getName());
					sheetPubInfo.setDealOrgId(tsmStaff.getOrganizationId());
					sheetPubInfo.setDealOrgName(tsmStaff.getOrgName());
					sheetPubInfo.setDealStaffId(Integer.parseInt(tsmStaff.getId()));
					sheetPubInfo.setDealStaffName(tsmStaff.getName());
				}
				else if (!"".equals(receiveOrg)) {
					rcvFlag = true;
					sheetPubInfo.setRcvOrgId(receiveOrg);
					sheetPubInfo.setRcvOrgName(pubFunc.getOrgName(receiveOrg));
				}
			}
		}
		return rcvFlag;
	}

	private void checkDealStaffBySixCatalog(OrderAskInfo orderAskInfo, ServiceContent cont, SheetPubInfo sheetPubInfo) {
		if (StaticData.TACHE_FINISH_NEW == sheetPubInfo.getTacheId() || 3 != orderAskInfo.getServiceDate()) {
			return;
		}
		int servType = sheetPubInfo.getServType(); // 服务类型
		int tacheId = sheetPubInfo.getTacheId(); // 工单环节
		String numRegion = String.valueOf(orderAskInfo.getRegionId()); // 产品号码所属地域
		String sixId = String.valueOf(pubFunc.getLastXX(cont)); // 末级目录ID
		int comeFrom = pubFunc.getLastChannel(orderAskInfo); // 受理渠道
		Map config = null;
		if (cont.getSixCatalog() == 0) {// 新投诉现象
			config = pubFunc.getSheetAllotConfigNew(sixId, numRegion, servType, tacheId, comeFrom, cont);
		} else {
			config = pubFunc.getSheetAllotConfigNew(servType, tacheId, sixId, numRegion, cont.getBestOrder(), comeFrom);
		}
		if (config != null && !config.isEmpty()) {
			setDealStaffBySixCatalog(config, sheetPubInfo);
		} else if (720130011 == sheetPubInfo.getSheetType() || 700000126 == sheetPubInfo.getSheetType()) {// 后台派单
			String sourceOrg = sheetPubInfo.getRcvOrgId();
			if (!"".equals(sourceOrg)) {
				boolean fteFlag = doFlowToEnd(orderAskInfo, sheetPubInfo, "一跟到底后台派单到个人");// 优先判断一跟到底
				if(!fteFlag) {//再判断分公司智能转派-后台派单工单
					sheetAllotByConfigSecondHTPD(orderAskInfo, cont, sheetPubInfo);// 分公司智能转派-后台派单工单
				}
			}
		}
	}

	private void setDealStaffBySixCatalog(Map config, SheetPubInfo sheetPubInfo) {
		String orderId = sheetPubInfo.getServiceOrderId();
		String sheetId = sheetPubInfo.getWorkSheetId();
		int tacheId = sheetPubInfo.getTacheId();
		int receiveStaff = Integer.parseInt(this.defaultMapValueIfNull(config, "RECEIVE_STAFF", "0"));
		String receiveOrg = this.defaultMapValueIfNull(config, "RECEIVE_ORG", "0");
		String guid = defaultMapValueIfNull(config, "GUID", "");
		SheetOperation operation = new SheetOperation();
		operation.setServiceOrderId(orderId);
		operation.setWorkSheetId(sheetId);
		operation.setMatchGuid(guid);
		if (receiveStaff != 0) {
			TsmStaff tsmStaff = pubFunc.getStaff(receiveStaff);
			sheetPubInfo.setRcvOrgId(tsmStaff.getOrganizationId());
			sheetPubInfo.setRcvOrgName(tsmStaff.getOrgName());
			sheetPubInfo.setRcvStaffId(Integer.parseInt(tsmStaff.getId()));
			sheetPubInfo.setRcvStaffName(tsmStaff.getName());
			sheetPubInfo.setDealOrgId(tsmStaff.getOrganizationId());
			sheetPubInfo.setDealOrgName(tsmStaff.getOrgName());
			sheetPubInfo.setDealStaffId(Integer.parseInt(tsmStaff.getId()));
			sheetPubInfo.setDealStaffName(tsmStaff.getName());
			operation.setDealStaffId(0);
			operation.setDispatchOrg(tsmStaff.getOrganizationId());
			operation.setDispatchOrgName(tsmStaff.getOrgName());
			operation.setDispatchStaff(tsmStaff.getId());
			operation.setDispatchStaffName(tsmStaff.getName() + "(" + tsmStaff.getLogonName() + ")");
			operation.setRemark(getRemarkByTacheId(tacheId, 1));// 到个人
		} else if (!"0".equals(receiveOrg)) {
			String receiveOrgName = pubFunc.getOrgName(receiveOrg);
			sheetPubInfo.setRcvOrgId(receiveOrg);
			sheetPubInfo.setRcvOrgName(receiveOrgName);
			// 直派分公司 后台派单转派分公司
			operation.setDispatchOrg(receiveOrg);
			operation.setDispatchOrgName(receiveOrgName);
			operation.setDealStaffId(0);
			operation.setRemark(getRemarkByTacheId(tacheId, 0));// 到部门
			if (CompatHandler.isTachAssign(tacheId) && !pubFunc.isAffiliated(receiveOrg, "11")) {// 后台派单-虚拟岗转派分公司
				logger.info("直派分公司 orderId: {} rcvOrgId: {}", orderId, receiveOrg);
				TsmStaff htpd = pubFunc.getStaff(20001797);// 省投派单岗
				sheetPubInfo.setRcvOrgId(htpd.getOrganizationId());
				sheetPubInfo.setRcvOrgName(htpd.getOrgName());
				sheetPubInfo.setRcvStaffId(Integer.parseInt(htpd.getId()));
				sheetPubInfo.setRcvStaffName(htpd.getName());
				sheetPubInfo.setDealOrgId(htpd.getOrganizationId());
				sheetPubInfo.setDealOrgName(htpd.getOrgName());
				sheetPubInfo.setDealStaffId(Integer.parseInt(htpd.getId()));
				sheetPubInfo.setDealStaffName(htpd.getName());
				if (sheetPubInfoDao.countWorkSheetAreaByOrderId(orderId, 3) == 0) {
					sheetPubInfoDao.insertWorkSheetArea(orderId, sheetId, htpd.getOrgName(), 3);
				}
				operation.setDealStaff(htpd.getLogonName());
				operation.setDealStaffId(Integer.parseInt(htpd.getId()));
				operation.setDealStaffName(htpd.getName());
				operation.setDealOrgId(htpd.getOrganizationId());
				operation.setDealOrgName(htpd.getOrgName());
				operation.setRemark("系统自动转派");
			}
		}
		tsWorkSheetDeal.saveSheetOperation(operation);
	}

	private String getRemarkByTacheId(int tacheId, int type) {
		if (1 == type) {// 到个人
			if (CompatHandler.isTachAssign(tacheId)) {// 后台派单
				return "智能直派后台派单到个人";
			} else if (StaticData.TACHE_ZHONG_DINGXING_NEW == tacheId) {// 终定性
				return "智能直派终定性到个人";
			} else if (StaticData.TACHE_DINGXING_NEW == tacheId) {// 预定性
				return "智能直派预定性到个人";
			}
		} else {// 到部门
			if (CompatHandler.isTachAssign(tacheId)) {// 后台派单
				return "智能直派后台派单到部门";
			} else if (StaticData.TACHE_ZHONG_DINGXING_NEW == tacheId) {// 终定性
				return "智能直派终定性到部门";
			} else if (StaticData.TACHE_DINGXING_NEW == tacheId) {// 预定性
				return "智能直派预定性到部门";
			}
		}
		return "";
	}

	// 优先判断一跟到底逻辑
	private boolean doFlowToEnd(OrderAskInfo askInfo, SheetPubInfo sheetInfo, String remark) {
		String orderId = askInfo.getServOrderId();
		if (720130000 != askInfo.getServType() && 720200003 != askInfo.getServType()) {// 校验服务类型
			return false;
		}
		String orgPlace = pubFunc.getAreaOrgId(sheetInfo.getRcvOrgId());// 获取单位
		int checkOrg = staffWorkloadService.checkFlowToEndConfigByIdType(orgPlace, 1);
		logger.info("doFlowToEnd orderId: {} remark: {} orgPlace: {} checkOrg: {}", orderId, remark, orgPlace, checkOrg);
		if (0 == checkOrg) {// 校验部门在不在配置中
			return false;
		}
		String lastChannel = String.valueOf(pubFunc.getLastChannel(askInfo));
		int checkChannel = staffWorkloadService.checkFlowToEndConfigByIdType(lastChannel, 2);
		logger.info("doFlowToEnd orderId: {} lastChannel: {} checkChannel: {}", orderId, lastChannel, checkChannel);
		if (0 == checkChannel) {// 校验渠道在不在配置中
			return false;
		}
		String prodNum = askInfo.getProdNum();
		int checkProdNum = staffWorkloadService.checkFlowToEndConfigByIdType(prodNum, 3);
		logger.info("doFlowToEnd orderId: {} prodNum: {} checkProdNum: {}", orderId, prodNum, checkProdNum);
		if (checkProdNum > 0) {// 一是产品号码是58810000，这种不能纳入一跟到底，原因是这大多数是营业厅扫码录单且用户已无电信产品了，没号码录才录的58810000，重复情况比较多；
			return false;
		}
		String checkSensitive = customerServiceFeign.sensitiveQuery(prodNum);
		logger.info("doFlowToEnd orderId: {} prodNum: {} checkSensitiveProdNum: {}", orderId, prodNum, checkSensitive);
		if ("0".equals(checkSensitive)) {// 敏感用户，产品号码
			return false;
		}
		String relaInfo = askInfo.getRelaInfo();
		checkSensitive = customerServiceFeign.sensitiveQuery(relaInfo);
		logger.info("doFlowToEnd orderId: {} relaInfo: {} checkSensitiveRelaInfo: {}", orderId, relaInfo, checkSensitive);
		if ("0".equals(checkSensitive)) {// 敏感用户，联系号码
			return false;
		}
		FlowToEnd fte = staffWorkloadService.getFlowToEnd30Day(askInfo, orgPlace);
		logger.info("doFlowToEnd orderId: {} fte: {}", orderId, fte);
		if (null != fte) {// 匹配到前单
			int lastStaffId = fte.getDealStaffId();
			String sheetId = sheetInfo.getWorkSheetId();
			fte.setCurOrderId(orderId);
			fte.setCurSheetId(sheetId);
			TsmStaff lastStaff = pubFunc.getStaff(lastStaffId);
			fte.setCountWorkloadGuid("");
			sheetInfo.setRcvOrgId(lastStaff.getOrganizationId());
			sheetInfo.setRcvOrgName(lastStaff.getOrgName());
			sheetInfo.setRcvStaffId(Integer.parseInt(lastStaff.getId()));
			sheetInfo.setRcvStaffName(lastStaff.getName());
			sheetInfo.setDealOrgId(lastStaff.getOrganizationId());
			sheetInfo.setDealOrgName(lastStaff.getOrgName());
			sheetInfo.setDealStaffId(Integer.parseInt(lastStaff.getId()));
			sheetInfo.setDealStaffName(lastStaff.getName());
			sheetInfo.setMonth(askInfo.getMonth());
			StringBuilder sb = new StringBuilder();
			sb.append("一跟到底，工单号为");
			sb.append(sheetInfo.getWorkSheetId());
			sb.append("，分派给员工");
			sb.append(lastStaff.getName());
			sb.append(lastStaff.getId());
			sb.append("，前单号为");
			sb.append(fte.getOldOrderId());
            workSheetAllot.saveSheetDealAction(sheetInfo, StaticData.WKST_SYSTEM_AUTO, 1, sb.toString());
			SheetOperation operation = new SheetOperation();
			operation.setServiceOrderId(orderId);
			operation.setWorkSheetId(sheetId);
			operation.setMatchGuid("");
			operation.setDealStaffId(0);
			operation.setDispatchOrg(lastStaff.getOrganizationId());
			operation.setDispatchOrgName(lastStaff.getOrgName());
			operation.setDispatchStaff(lastStaff.getId());
			operation.setDispatchStaffName(lastStaff.getName() + "(" + lastStaff.getLogonName() + ")");
			operation.setRemark(remark);
			tsWorkSheetDeal.saveSheetOperation(operation);
			StaffWorkloadInfo swi = staffWorkloadService.queryInOrAfterWork(lastStaffId);// 查询当前报表，查不到查询未来班表
	    	if (null != swi) {// 关联到工作量情况
	    		staffWorkloadService.allotWork(swi, true);
	    		fte.setCountWorkloadGuid(swi.getGuid());
	    	}
			staffWorkloadService.saveFlowToEnd(fte);
			return true;
		}
		return false;
	}

	// 分公司智能转派-后台派单工单
	private void sheetAllotByConfigSecondHTPD(OrderAskInfo askInfo, ServiceContent content, SheetPubInfo sheetInfo) {
		String orderId = askInfo.getServOrderId();
		OrderCustomerInfo customer = orderCustInfoDao.getOrderCustByOrderId(orderId);
		ServiceLabel label = labelManageDAO.queryServiceLabelById(orderId, false);
		Map config = pubFunc.getSheetAllotConfigSecond(sheetInfo.getRcvOrgId(), sheetInfo.getSheetType(), askInfo, content, customer, label);
		logger.info("sheetAllotByConfigSecondHTPD orderId: {} config: {}", orderId, config);
		if (!config.isEmpty()) {
			int autoZpStaff = Integer.parseInt(defaultMapValueIfNull(config, "AUTO_ZP_STAFF", "0"));
			String receiveOrg = defaultMapValueIfNull(config, "RECEIVE_ORG", "0");
			String receiveOrgName = pubFunc.getOrgName(receiveOrg);
			int receiveStaff = Integer.parseInt(defaultMapValueIfNull(config, "RECEIVE_STAFF", "0"));
			String guid = defaultMapValueIfNull(config, "GUID", "");
			SheetOperation operation = new SheetOperation();
			operation.setServiceOrderId(orderId);
			operation.setWorkSheetId(sheetInfo.getWorkSheetId());
			operation.setMatchGuid(guid);
			if (0 != autoZpStaff) {// 经虚拟岗，自动将后台派单单继续派单区县部门
				logger.info("智能转派后台派单虚拟岗 orderId: {} autoZpStaff: {} rcvOrgId: {}", orderId, autoZpStaff, receiveOrg);
				TsmStaff zpStaff = pubFunc.getStaff(autoZpStaff);
				sheetInfo.setRcvOrgId(zpStaff.getOrganizationId());
				sheetInfo.setRcvOrgName(zpStaff.getOrgName());
				sheetInfo.setRcvStaffId(Integer.parseInt(zpStaff.getId()));
				sheetInfo.setRcvStaffName(zpStaff.getName());
				sheetInfo.setDealOrgId(zpStaff.getOrganizationId());
				sheetInfo.setDealOrgName(zpStaff.getOrgName());
				sheetInfo.setDealStaffId(Integer.parseInt(zpStaff.getId()));
				sheetInfo.setDealStaffName(zpStaff.getName());
				operation.setDealStaff(zpStaff.getLogonName());
				operation.setDealStaffId(Integer.parseInt(zpStaff.getId()));
				operation.setDealStaffName(zpStaff.getName());
				operation.setDealOrgId(zpStaff.getOrganizationId());
				operation.setDealOrgName(zpStaff.getOrgName());
				operation.setDispatchOrg(receiveOrg);
				operation.setDispatchOrgName(receiveOrgName);
				operation.setRemark("智能转派后台派单虚拟岗");
			} else {// 不经虚拟岗，后台派单收单部门直接为区县部门
				operation.setDealStaffId(0);
				if (0 != receiveStaff) {// 到个人
					logger.info("智能转派后台派单到个人 orderId: {} rcvStaffId: {}", orderId, receiveStaff);
					TsmStaff rcvStaff = pubFunc.getStaff(receiveStaff);
					sheetInfo.setRcvOrgId(rcvStaff.getOrganizationId());
					sheetInfo.setRcvOrgName(rcvStaff.getOrgName());
					sheetInfo.setRcvStaffId(Integer.parseInt(rcvStaff.getId()));
					sheetInfo.setRcvStaffName(rcvStaff.getName());
					sheetInfo.setDealOrgId(rcvStaff.getOrganizationId());
					sheetInfo.setDealOrgName(rcvStaff.getOrgName());
					sheetInfo.setDealStaffId(Integer.parseInt(rcvStaff.getId()));
					sheetInfo.setDealStaffName(rcvStaff.getName());
					operation.setDispatchOrg(rcvStaff.getOrganizationId());
					operation.setDispatchOrgName(rcvStaff.getOrgName());
					operation.setDispatchStaff(rcvStaff.getId());
					operation.setDispatchStaffName(rcvStaff.getName() + "(" + rcvStaff.getLogonName() + ")");
					operation.setRemark("智能转派后台派单到个人");
				} else {// 到部门
					logger.info("智能转派后台派单到部门 orderId: {} rcvOrgId: {}", orderId, receiveOrg);
					sheetInfo.setRcvOrgId(receiveOrg);
					sheetInfo.setRcvOrgName(receiveOrgName);
					operation.setDispatchOrg(receiveOrg);
					operation.setDispatchOrgName(receiveOrgName);
					operation.setRemark("智能转派后台派单到部门");
				}
			}
			tsWorkSheetDeal.saveSheetOperation(operation);
		}
	}

	// 判断是后台派单虚拟岗配置还是部门处理虚拟岗配置还是一根到底配置
	private void sourceAllotByConfigSecondBMCL(OrderAskInfo askInfo, ServiceContent content, SheetPubInfo sheetInfo) {
		if (720130013 == sheetInfo.getSheetType() || 700000127 == sheetInfo.getSheetType()) {// 部门处理
			Map map = tsWorkSheetDao.getDispatchOrgMap(askInfo.getServOrderId(), "智能转派后台派单虚拟岗");
			if (map.isEmpty()) {// 后台派单虚拟岗转派不再判断其他规则
				String sourceOrg = sheetInfo.getRcvOrgId();
				if (!"".equals(sourceOrg)) {
					boolean fteFlag = false;
					map = tsWorkSheetDao.getDispatchOrgMap(askInfo.getServOrderId(), "一跟到底后台派单到个人");
					if (map.isEmpty()) {// 后台派单一跟到底判断成功后不再判断部门处理一跟到底
						fteFlag = doFlowToEnd(askInfo, sheetInfo, "一跟到底部门处理到个人");// 优先判断一跟到底
					}
					if (!fteFlag) {// 再判断分公司智能转派-部门处理工单
						sheetAllotByConfigSecondBMCL(askInfo, content, sheetInfo);// 部门处理虚拟岗
					}
				}
			}
		}
	}

	// 分公司智能转派-部门处理工单
	private void sheetAllotByConfigSecondBMCL(OrderAskInfo askInfo, ServiceContent content, SheetPubInfo sheetInfo) {
		String orderId = askInfo.getServOrderId();
		OrderCustomerInfo customer = orderCustInfoDao.getOrderCustByOrderId(orderId);
		ServiceLabel label = labelManageDAO.queryServiceLabelById(orderId, false);
		Map config = pubFunc.getSheetAllotConfigSecond(sheetInfo.getRcvOrgId(), sheetInfo.getSheetType(), askInfo, content, customer, label);
		logger.info("sheetAllotByConfigSecondBMCL orderId: {} config: {}", orderId, config);
		if (!config.isEmpty()) {
			int autoZpStaff = Integer.parseInt(defaultMapValueIfNull(config, "AUTO_ZP_STAFF", "0"));
			String receiveOrg = defaultMapValueIfNull(config, "RECEIVE_ORG", "0");
			String receiveOrgName = pubFunc.getOrgName(receiveOrg);
			int receiveStaff = Integer.parseInt(defaultMapValueIfNull(config, "RECEIVE_STAFF", "0"));
			String guid = defaultMapValueIfNull(config, "GUID", "");
			SheetOperation operation = new SheetOperation();
			operation.setServiceOrderId(orderId);
			operation.setWorkSheetId(sheetInfo.getWorkSheetId());
			operation.setMatchGuid(guid);
			if (0 != autoZpStaff) {// 经虚拟岗，自动将部门处理单继续转派区县部门
				logger.info("智能转派部门处理虚拟岗 orderId: {} autoZpStaff: {} rcvOrgId: {}", orderId, autoZpStaff, receiveOrg);
				TsmStaff zpStaff = pubFunc.getStaff(autoZpStaff);
				sheetInfo.setRcvOrgId(zpStaff.getOrganizationId());
				sheetInfo.setRcvOrgName(zpStaff.getOrgName());
				sheetInfo.setRcvStaffId(Integer.parseInt(zpStaff.getId()));
				sheetInfo.setRcvStaffName(zpStaff.getName());
				sheetInfo.setDealOrgId(zpStaff.getOrganizationId());
				sheetInfo.setDealOrgName(zpStaff.getOrgName());
				sheetInfo.setDealStaffId(Integer.parseInt(zpStaff.getId()));
				sheetInfo.setDealStaffName(zpStaff.getName());
				operation.setDealStaff(zpStaff.getLogonName());
				operation.setDealStaffId(Integer.parseInt(zpStaff.getId()));
				operation.setDealStaffName(zpStaff.getName());
				operation.setDealOrgId(zpStaff.getOrganizationId());
				operation.setDealOrgName(zpStaff.getOrgName());
				operation.setDispatchOrg(receiveOrg);
				operation.setDispatchOrgName(receiveOrgName);
				operation.setRemark("分公司智能转派");// 智能转派部门处理虚拟岗
			} else {// 不经虚拟岗，部门处理收单部门直接为区县部门
				operation.setDealStaffId(0);
				if (0 != receiveStaff) {// 到个人
					logger.info("智能转派部门处理到个人 orderId: {} rcvStaffId: {}", orderId, receiveStaff);
					TsmStaff rcvStaff = pubFunc.getStaff(receiveStaff);
					sheetInfo.setRcvOrgId(rcvStaff.getOrganizationId());
					sheetInfo.setRcvOrgName(rcvStaff.getOrgName());
					sheetInfo.setRcvStaffId(Integer.parseInt(rcvStaff.getId()));
					sheetInfo.setRcvStaffName(rcvStaff.getName());
					sheetInfo.setDealOrgId(rcvStaff.getOrganizationId());
					sheetInfo.setDealOrgName(rcvStaff.getOrgName());
					sheetInfo.setDealStaffId(Integer.parseInt(rcvStaff.getId()));
					sheetInfo.setDealStaffName(rcvStaff.getName());
					operation.setDispatchOrg(rcvStaff.getOrganizationId());
					operation.setDispatchOrgName(rcvStaff.getOrgName());
					operation.setDispatchStaff(rcvStaff.getId());
					operation.setDispatchStaffName(rcvStaff.getName() + "(" + rcvStaff.getLogonName() + ")");
					operation.setRemark("智能转派部门处理到个人");
				} else {// 到部门
					logger.info("智能转派部门处理到部门 orderId: {} rcvOrgId: {}", orderId, receiveOrg);
					sheetInfo.setRcvOrgId(receiveOrg);
					sheetInfo.setRcvOrgName(receiveOrgName);
					operation.setDispatchOrg(receiveOrg);
					operation.setDispatchOrgName(receiveOrgName);
					operation.setRemark("智能转派部门处理到部门");
				}
			}
			tsWorkSheetDeal.saveSheetOperation(operation);
		}
	}

	private SheetPubInfo setYDXFlow(OrderAskInfo orderAskInfo, ServiceContent cont, SheetPubInfo sheetPubInfo) {
		int servType = sheetPubInfo.getServType(); // 服务类型
		int tacheId = sheetPubInfo.getTacheId(); // 工单环节
		String numRegion = String.valueOf(orderAskInfo.getRegionId()); // 产品号码所属地域
		String sixId = String.valueOf(pubFunc.getLastXX(cont)); // 末级目录ID
		int comeFrom = pubFunc.getLastChannel(orderAskInfo); // 受理渠道
		String orderId = orderAskInfo.getServOrderId();
		Map config = null;
		if(cont.getSixCatalog() == 0) {//新投诉现象
			config = pubFunc.getSheetAllotConfigNew(sixId, numRegion, servType, tacheId, comeFrom, cont);
		} else {
			config = pubFunc.getSheetAllotConfigNew(servType, tacheId, sixId, numRegion, cont.getBestOrder(), comeFrom);
		}
		if (null != config && !config.isEmpty()) {
			String guid = "";
			if(cont.getSixCatalog() == 0) {//新投诉现象
				guid = defaultMapValueIfNull(config, "GUID", "");
			} else {
				guid = defaultMapValueIfNull(config, "CONFIG_KEY", "");
			}
			SheetOperation operation = new SheetOperation();
			operation.setServiceOrderId(orderId);
			operation.setWorkSheetId(sheetPubInfo.getWorkSheetId());
			operation.setMatchGuid(guid);
			operation.setDealStaffId(0);
			int receiveStaff = Integer.parseInt(this.defaultMapValueIfNull(config, "RECEIVE_STAFF", "0"));
			if (0 != receiveStaff) {
				TsmStaff tsmStaff = pubFunc.getStaff(receiveStaff);
				operation.setDispatchOrg(tsmStaff.getOrganizationId());
				operation.setDispatchOrgName(tsmStaff.getOrgName());
				operation.setDispatchStaff(tsmStaff.getId());
				operation.setDispatchStaffName(tsmStaff.getName() + "(" + tsmStaff.getLogonName() + ")");
				operation.setRemark(getRemarkByTacheId(tacheId, 1));// 到个人
				logger.info("预定性直派到个人 orderId: {} receiveStaff: {}", orderId, receiveStaff);
				tsWorkSheetDeal.saveSheetOperation(operation);
				Map orgMap = new HashMap();
				orgMap.put("STAFF_ID", receiveStaff);
				return FlowOrgFactory.factoryMethod("tsFlowOrg").setSheetOrg(sheetPubInfo, orgMap);
			}
			String receiveOrg = this.defaultMapValueIfNull(config, "RECEIVE_ORG", "0");
			if (!"0".equals(receiveOrg)) {
				String receiveOrgName = pubFunc.getOrgName(receiveOrg);
				operation.setDispatchOrg(receiveOrg);
				operation.setDispatchOrgName(receiveOrgName);
				operation.setRemark(getRemarkByTacheId(tacheId, 0));// 到部门
				logger.info("预定性直派分公司 orderId: {} rcvOrgId: {} rcvOrgName: {}", orderId, receiveOrg, receiveOrgName);
				tsWorkSheetDeal.saveSheetOperation(operation);
				Map orgMap = new HashMap();
				orgMap.put("FLOW_ORG", receiveOrg);
				return FlowOrgFactory.factoryMethod("tsFlowOrg").setSheetOrg(sheetPubInfo, orgMap);
			}
		}
		// 预定性单的收单部门取部门处理单的收单部门
		SheetPubInfo latestDS = sheetPubInfoDao.getLatestSheetByType(orderId, StaticData.SHEET_TYPE_TS_DEAL_NEW, 1);
		if (null != latestDS) {
			Map orgMap = new HashMap();
			orgMap.put("FLOW_ORG", latestDS.getRcvOrgId());
			return FlowOrgFactory.factoryMethod("tsFlowOrg").setSheetOrg(sheetPubInfo, orgMap);
		}
		return sheetPubInfo;
	}
	
	@SuppressWarnings("all")
	private void setSJAssignFlow(SheetPubInfo sheetPubInfo, ServiceContent cont, int workSheetObjSize) {
		//商机派单工单
		if(sheetPubInfo.getSheetType() == StaticData.SHEET_TYPE_SJ_ASSING && workSheetObjSize == 0){
			List tmpColumn = pubFunc.getDir("CC_SERVICE_CONTENT_ASK", null, String.valueOf(cont.getAppealReasonId()));
			String judgeId = "";
			if(tmpColumn != null && !tmpColumn.isEmpty()) {
				Object obj = tmpColumn.get(0);
				if(obj != null) {
					obj = ((Map) obj).get("COL_VALUE_HANDLING");
					judgeId = obj == null ? "" : obj.toString().trim();
				}
			}
			
			//云脑商机单
			if("YNSJ".equals(judgeId)){
				Map config = pubFunc.getSheetAllotConfig(sheetPubInfo.getServType(), sheetPubInfo.getTacheId(), judgeId, String.valueOf(sheetPubInfo.getRegionId()));
				if(config != null) {
					String receiveOrg = config.get("RECEIVE_ORG") == null ? "" : config.get("RECEIVE_ORG").toString();
					if(!"".equals(receiveOrg)) {
						sheetPubInfo.setRcvOrgId(receiveOrg);
						sheetPubInfo.setRcvOrgName(pubFunc.getOrgName(receiveOrg));
					}
				}
			}
			//商机智慧营销、中台商机、商机管理平台
			if("ZHYX".equals(judgeId) || "ZTSJ".equals(judgeId) || "SJ_SYNC".equals(judgeId)){
				Map config = pubFunc.getSheetAllotConfig(sheetPubInfo.getServType(), sheetPubInfo.getTacheId(), judgeId, "0");
				if(config != null) {
					String receiveOrg = config.get("RECEIVE_ORG") == null ? "" : config.get("RECEIVE_ORG").toString();
					if(!"".equals(receiveOrg)) {
						sheetPubInfo.setRcvOrgId(receiveOrg);
						sheetPubInfo.setRcvOrgName(pubFunc.getOrgName(receiveOrg));
					}
				}
			}
		}
	}
	
	private void setYSAssignFlow(SheetPubInfo sheetPubInfo, Map inParam) {
		//预受理派单工单 订单标识
		if(sheetPubInfo.getSheetType() == StaticData.SHEET_TYPE_YS_ASSING && inParam.containsKey("ORDER_FLAG")){
			String orderFlag = this.defaultMapValueIfNull(inParam, "ORDER_FLAG", "");
			if("ZTYS".equals(orderFlag)) {//中台预受理单流向配置部门
				Map config = pubFunc.getSheetAllotConfig(sheetPubInfo.getServType(), sheetPubInfo.getTacheId(), "ZTYS", "1");
				if(config != null) {
					int receiveStaff = Integer.parseInt(this.defaultMapValueIfNull(config, "RECEIVE_STAFF", "0"));
					String receiveOrg = this.defaultMapValueIfNull(config, "RECEIVE_ORG", "0");
					if(receiveStaff != 0) {
						TsmStaff tsmStaff = pubFunc.getStaff(receiveStaff);
						sheetPubInfo.setRcvOrgId(tsmStaff.getOrganizationId());
						sheetPubInfo.setRcvOrgName(tsmStaff.getOrgName());
						sheetPubInfo.setRcvStaffId(Integer.parseInt(tsmStaff.getId()));
						sheetPubInfo.setRcvStaffName(tsmStaff.getName());
						sheetPubInfo.setDealOrgId(tsmStaff.getOrganizationId());
						sheetPubInfo.setDealOrgName(tsmStaff.getOrgName());
						sheetPubInfo.setDealStaffId(Integer.parseInt(tsmStaff.getId()));
						sheetPubInfo.setDealStaffName(tsmStaff.getName());
					}
					else if(!"0".equals(receiveOrg)) {
						sheetPubInfo.setRcvOrgId(receiveOrg);
						sheetPubInfo.setRcvOrgName(pubFunc.getOrgName(receiveOrg));
					}
				}
			}
		}
	}
	
	private String defaultMapValueIfNull(Map map, String key, String defaultStr) {
		return map.get(key) == null ? defaultStr : map.get(key).toString();
	}

	/**
	 * 得到工单流向的工位
	 * @param sheetInfo
	 * @param itemValue
	 * @param sheetPubInfo 新生成工单对象
	 * @return
	 */
	private SheetPubInfo getFlowOrgId(SheetPubInfo sheetInfo,OrderAskInfo orderAskInfo,SheetPubInfo sheetPubInfo,Map inParam) {		
		if(orderAskInfo.getServiceDate() == 3) {//投诉
			sheetPubInfo = FlowOrgFactory.factoryMethod("tsFlowOrg").getFlowOrgId(sheetInfo, orderAskInfo, inParam, sheetPubInfo);	
		}
		if(orderAskInfo.getServiceDate() == 0) {//疑难
			sheetPubInfo = FlowOrgFactory.factoryMethod("ynFlowOrg").getFlowOrgId(sheetInfo, orderAskInfo, inParam, sheetPubInfo);		
		}
		if(orderAskInfo.getServiceDate() == 1) {//预受理
			sheetPubInfo = FlowOrgFactory.factoryMethod("ysFlowOrg").getFlowOrgId(sheetInfo, orderAskInfo, inParam, sheetPubInfo);		
		}
		return sheetPubInfo;		
	}
}
