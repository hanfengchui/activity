/*
 * 说明：管理受理单、工单标签的服务实现类
 * 时间： 2012-4-27
 * 作者：LiJiahui
 * 操作：新增
 */
package com.timesontransfar.customservice.labelmanage.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.dbgridData.conditions.GridDateConditions;
import com.timesontransfar.customservice.intf.pojo.ManagerInfo;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.labelmanage.service.ILabelManageService;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IpersonaDao;
import com.timesontransfar.customservice.orderask.pojo.CustomerPersona;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.feign.custominterface.InterfaceFeign;
import com.timesontransfar.feign.custominterface.PortalInterfaceFeign;

import net.sf.json.JSONObject;

/**
 * @author LiJiahui
 * 
 */
@Component(value="LabelManageService")
public class LabelManageServiceImpl implements ILabelManageService {

    /**
     * 日志实例
     */
    private static final Logger log = LoggerFactory.getLogger(LabelManageServiceImpl.class);

    /**
     * 操作标签库数据表的实例
     */
    @Autowired
    private ILabelManageDAO labelManageDAO;
	@Autowired
	private GridDateConditions gridDateConditions;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	@Autowired
	private CustomerServiceFeign customerServiceFeign;
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private IpersonaDao personaDao;
    @Autowired
    private InoteSenList noteSen;
    @Autowired
    private PubFunc pubFun;
    @Autowired
	private PortalInterfaceFeign portalFeign;
    @Autowired
	private InterfaceFeign interfaceFeign;

    /**
     * 标记省市总热线
     */
	public int updateHotlineFlag(String serviceOrderId, int staffId) {
		return this.labelManageDAO.updateHotlineFlag(serviceOrderId, staffId, 1);
	}

	/**
	 * 省服务监督热线(打标)在途单查询
	 */
	public GridDataInfo getHotlineGrid(String param) {
		JSONObject obj = JSONObject.fromObject(param);
		String begion = obj.optString("begion");
		
		String strWhere = gridDateConditions.getStrWhere(DbgridStatic.GRID_FUNID_JDRX_ZTDDATA, param);
		String sql = "SELECT a.SERVICE_ORDER_ID," + 
					"REGION_NAME," + 
					"ORDER_STATU_DESC," + 
					"date_format(accept_date, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE," + 
					"PROD_NUM," + 
					"SERVICE_TYPE_DESC," + 
					"ACCEPT_STAFF_NAME," + 
					"ACCEPT_COUNT," + 
					"ACCEPT_ORG_NAME," + 
					"staffname HOTLINE_STAFF_NAME," + 
					"ROUND((TIMESTAMPDIFF(SECOND, IFNULL(hotline_date, accept_date), NOW())/60/60/24) * 240) / 10 HOTLINE_HOUR," +
					"a.MONTH_FLAG," + 
					"a.REGION_ID," + 
					"a.CUST_GUID," + 
					"ORDER_VESION," + 
					"SERVICE_TYPE " + 
					"FROM cc_service_order_ask a, cc_service_label b left join tsm_staff f on b.hotline_staff = f.staff_id " +
					"WHERE a.service_order_id = b.service_order_id " + 
					"AND (hotline_flag = 1 OR accept_channel_id = 707907012)";
		sql += strWhere;
		return dbgridDataPub.getResult(sql, Integer.parseInt(begion), "", "");
	}

	public int modifyUnusualFlag(String serviceId) {
		return this.labelManageDAO.updateUnusualFlag(serviceId);
	}

	// 受理后异步标识
	public int submitServiceOrderAsync(String orderId, OrderAskInfo orderAskInfo, OrderCustomerInfo custInfo, CustomerPersona persona) {
		String accNum = orderAskInfo.getProdNum();
		int region = orderAskInfo.getRegionId();
		int servType = orderAskInfo.getServType();
		int comeCategory = orderAskInfo.getComeCategory();
		String custId = custInfo.getCrmCustId();
				
		if (720130000 == servType || 700006312 == servType) {
			labelManageDAO.updatePassiveRepeatFlag(orderId, accNum, region);
		}
		if (720130000 == servType && (707907002 == comeCategory || 707907003 == comeCategory)) {
			labelManageDAO.updatePassiveUpgradeFlag(orderId, accNum, region);
		}
		int preferAppeal = orderAskInfoDao.getPreferAppealFlag(orderId, accNum, region);
//		int preferComplaint = orderAskInfoDao.getPreferComplaintFlag(orderId, accNum, region);
		String consumeType = "0";//customerServiceFeign.getConsumeTypeFlag(accNum, region); 接口已下线
//		String finalOptionOrderId = orderAskInfoDao.getLastFinalOptionOrderId(accNum, region);
		int refundNum = portalFeign.queryRefundNum(accNum, null);
		Map<String, String> keyPersonMap = portalFeign.qryCustlevelAndKeyPerson(accNum, region);
		String sensitiveType = this.getSensitiveType(region, custId);
		
		persona.setOrderId(orderId);
		persona.setPreferAppeal(preferAppeal);
		persona.setConsumeType(consumeType);
		persona.setRefundNum(refundNum);
		persona.setCityLabel(orderAskInfo.getCityLabel());
		this.setKeyPerson(keyPersonMap, persona);
		persona.setSensitiveType(sensitiveType);
		int num = personaDao.savePersona(persona);
		log.info("savePersona num: {}", num);
		return num;
	}
	
	/**
	 * 四类工单打标
	 * @param orderAskInfo
	 */
	public void trafficLabel(OrderAskInfo orderAskInfo) {
		log.info("四类工单打标");
		try {
			if((StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType() || StaticData.SERV_TYPE_CX == orderAskInfo.getServType()) 
					&& orderAskInfo.getRelaType() > 0 && (!"N".equals(orderAskInfo.getCallSerialNo()))) {
				TsmStaff staff = pubFun.getStaff(orderAskInfo.getAskStaffId());
				Map<String, String> codeMap = new HashMap<>();
				codeMap.put("CODE_ID", String.valueOf(orderAskInfo.getRelaType()));
				codeMap.put("CODE_SOURCE", "3");
				codeMap.put("MARK_TYPE", "2");
				List<Object> codeList = new ArrayList<>();
				codeList.add(codeMap);
				Map<String, Object> trafficData = new HashMap<>();
				trafficData.put("SERVICE_TYPE", orderAskInfo.getServType());
				trafficData.put("REGION_ID", orderAskInfo.getRegionId());
				trafficData.put("CALLER_NUM", orderAskInfo.getSourceNum());
				trafficData.put("DEST_NUM", "");
				trafficData.put("PROD_NUM", orderAskInfo.getProdNum());
				trafficData.put("CALL_ANSWER", orderAskInfo.getAskDate());
				trafficData.put("CALL_ID", orderAskInfo.getCallId());
				trafficData.put("FLOW_ID", orderAskInfo.getCallSerialNo());
				trafficData.put("STAFF_ID", staff.getLogonName());
				trafficData.put("codeList", codeList);
				
				String jsonString = JSON.toJSONString(trafficData);
				portalFeign.trafficLabelListLog(jsonString, orderAskInfo.getServOrderId());
			}
		} catch (Exception e) {
			log.error("trafficLabel 异常：{}", e.getMessage(), e);
		}
	}
	
	private void setKeyPerson(Map<String, String> keyPersonMap, CustomerPersona persona) {
		int isKeyCustomer = 0;
		int isKeyPerson = 0;
		if(keyPersonMap != null && !keyPersonMap.isEmpty() && keyPersonMap.containsKey("keyPersonType")) {
			String keyPersonType = keyPersonMap.get("keyPersonType");
			if("2".equals(keyPersonType)) {
				isKeyCustomer = 1;//要客
				
				if(keyPersonMap.containsKey("custType")) {
					String custType = keyPersonMap.get("custType");
					if(Arrays.asList("0", "1").contains(custType) && Arrays.asList("1", "2").contains(keyPersonType)) {
						isKeyPerson = 1;//关键人
					}
				}
			}
		}
		persona.setIsKeyCustomer(isKeyCustomer);
		persona.setIsKeyPerson(isKeyPerson);
	}
	
	private String getSensitiveType(int regionId, String custId) {
		String regionIdForCrm = this.getCrmRegionId(regionId);
		if(StringUtils.isEmpty(regionIdForCrm) || StringUtils.isEmpty(custId)) {
			return "";
		}
		String sensitive = "";
		try {
			String result = interfaceFeign.queryComplaintsList(regionIdForCrm, custId);
			List<String> sensitiveTypeList = JSON.parseArray(result, String.class);
			StringBuilder sensitiveStr = new StringBuilder();
			for(String val : sensitiveTypeList) {
				if("1".equals(val) || "2".equals(val)) {//敏感类型：1专家型，2免营销
					sensitiveStr.append(val + ",");
				}
			}
			sensitive = StringUtils.removeEnd(sensitiveStr.toString(), ",");
		}
		catch(Exception e) {
			log.error("getSensitiveType error: {}", e.getMessage(), e);
		}
		return sensitive;
	}
	
	private String getCrmRegionId(int regionId){
		String regionIdForCrm = "";
		switch (regionId) {
			case 3:
				regionIdForCrm = "A";
				break;
			case 4:
				regionIdForCrm = "C";
				break;
			case 15:
				regionIdForCrm = "B";
				break;
			case 20:
				regionIdForCrm = "D";
				break;
			case 26:
				regionIdForCrm = "E";
				break;
			case 33:
				regionIdForCrm = "F";
				break;
			case 39:
				regionIdForCrm = "G";
				break;
			case 48:
				regionIdForCrm = "H";
				break;
			case 60:
				regionIdForCrm = "I";
				break;
			case 63:
				regionIdForCrm = "J";
				break;
			case 69:
				regionIdForCrm = "K";
				break;
			case 79:
				regionIdForCrm = "L";
				break;
			case 84:
				regionIdForCrm = "M";
				break;
			default:
				break;
		}
		return regionIdForCrm;
	}

	// 政企客户的投诉单，则发送受理短信至该客户的客户经理
	public int sendNoteToManagerPhone(String orderId, String accNum, int regionId, String accNbrType, int askStaffId) {
		String miReulst = customerServiceFeign.queryManagerInfo(accNum, regionId + "", accNbrType);
		if (StringUtils.isBlank((miReulst))) {
			return 0;
		}
		try {
			String managerPhone = "";
			String managerName = "";
			ManagerInfo mi = new Gson().fromJson(miReulst, ManagerInfo.class);
			if (mi.getManagerOnePhone().length() == 11) {
				managerName = mi.getManagerOneName();
				managerPhone = mi.getManagerOnePhone();
			} else if (mi.getManagerTwoPhone().length() == 11) {
				managerName = mi.getManagerTwoName();
				managerPhone = mi.getManagerTwoPhone();
			} else if (mi.getManagerThreePhone().length() == 11) {
				managerName = mi.getManagerThreeName();
				managerPhone = mi.getManagerThreePhone();
			}
			if (!"".equals(managerPhone)) {
				String sendContent = managerName + "经理，您好！您所负责区域的客户有一张投诉单，请您及时关注。产品号码为：" + accNum + "，投诉单号为：" + orderId;
				return sendNoteCont(managerPhone, regionId, sendContent, askStaffId, orderId);
			}
		}
		catch(Exception e) {
			log.error("sendNoteToManagerPhone error: {}", e.getMessage(), e);
		}
		return 0;
	}

	private int sendNoteCont(String destteRmid, int regionId, String sendContent, int askStaffId, String busiId) {
		int staffId = StaticData.ACPT_STAFFID_WT;
		String staffName = StaticData.ACPT_STAFFNAME_WT;
		String orgId = StaticData.ACPT_ORGID_WT;
		String orgName = StaticData.ACPT_ORGNAME_WT;
		if (askStaffId != StaticData.ACPT_STAFFID_WT && askStaffId != 1) {
			TsmStaff staff = this.pubFun.getStaff(askStaffId);
			staffId = Integer.parseInt(staff.getId());
			staffName = staff.getName();
			orgId = staff.getOrganizationId();
			orgName = staff.getOrgName();
		}
		String sheetGuid = this.pubFun.crtGuid();
		NoteSeand noteBean = new NoteSeand();
		noteBean.setSheetGuid(sheetGuid);
		noteBean.setRegionId(regionId);
		noteBean.setDestteRmid(destteRmid);
		noteBean.setClientType(1);
		noteBean.setSendContent(sendContent);
		noteBean.setOrgId(orgId);
		noteBean.setOrgName(orgName);
		noteBean.setStaffId(staffId);
		noteBean.setStaffName(staffName);
		noteBean.setBusiId(busiId);
		return noteSen.saveNoteContent(noteBean);
	}
	
	public void updateCallFlag(String orderId, String prodNum, String relaInfo, int regionId) {
		int num = 0;
		try {
			num = portalFeign.getCallRecordNum(prodNum, relaInfo, regionId);
			log.info("callRecordNum orderId: {} num: {}", orderId, num);
		}
		catch(Exception e) {
			log.error("callRecordNum error: {}", e.getMessage(), e);
		}
		int flag = num > 99 ? 99 : num;
		labelManageDAO.updateCallFlag(orderId, flag);
	}
	
	public void updateCustType(String orderId, String prodNum, int regionId) {
		String type = "";//1-如意用户
		try {
			type = customerServiceFeign.qryStatus(prodNum, String.valueOf(regionId));
			log.info("qryStatus orderId: {} prodNum: {} result: {}", orderId, prodNum, type);
		}
		catch(Exception e) {
			log.error("updateCustType error: {}", e.getMessage(), e);
		}
		if ("1".equals(type)) {
			labelManageDAO.insertRuyiLabel(orderId);
		}
	}
	
	/**
	 * 重复标签
	 */
	@SuppressWarnings("rawtypes")
	public void updateRepeatFlag(ServiceLabel label, CustomerPersona persona, OrderAskInfo orderAskInfo, ServiceContent servContent, boolean complaintFlag, boolean unifiedFlag) {
		String serviceOrderId = label.getServiceOrderId();
		String prodNum = orderAskInfo.getProdNum();
		String relaInfo = orderAskInfo.getRelaInfo();
		int regionId = orderAskInfo.getRegionId();
		int bestOrder = servContent.getBestOrder();
		int lastDirId = orderAskInfo.getRelaType();//投诉现象
		
		//产品号码、联系电话查询近30天投诉、咨询单
		List orderList = orderAskInfoDao.queryAllOrder(serviceOrderId, prodNum, relaInfo, regionId);
		label.setRepeatNewFlag(0);
		label.setRepeatFlag(0);
		label.setCptRepeatFlag(0);
		label.setCptRepeatBestFlag(0);
		label.setSensitiveNum(0);
		int repeatNewOrder = 0;
		int bestRepeatOrder = 0;
		int complaintRepeatOrder = 0;
		int complaintRepeatBestOrder = 0;
		
		int upTendencyNum = 0;
		int satisfyNum = 0;
		int unsatisfyNum = 0;
		int complaintNum = 0;
		String finalOptionOrderId = "";
		int finalOptionOrderNum = 0;
		int sensitiveNum = 0;//敏感话务标签
		sensitiveNum = this.setFirstSensitiveNum(lastDirId);//敏1打标
		if (!orderList.isEmpty()){
			String sysGuid1 = pubFun.crtGuid();
			String sysGuid2 = pubFun.crtGuid();
			String sysGuid3 = pubFun.crtGuid();
			String sysGuid4 = pubFun.crtGuid();
			for (int i = 0; i < orderList.size(); i++) {
				 Map hisOrder = (Map) orderList.get(i);
				 String hisOrderId = this.getStringByKey(hisOrder, "SERVICE_ORDER_ID");
				 String hisProdNum = this.getStringByKey(hisOrder, "PROD_NUM");
				 int serviceType = this.getIntByKey(hisOrder, "SERVICE_TYPE");
				 String hisAcceptDate = this.getStringByKey(hisOrder, "ACCEPT_DATE");
				 int hisBestOrder = this.getIntByKey(hisOrder, "BEST_ORDER");
				 int comeCategory = this.getIntByKey(hisOrder, "COME_CATEGORY");
				 int tsDealResult = this.getIntByKey(hisOrder, "TS_DEAL_RESULT");
				 String finalOptionFlag = this.getStringByKey(hisOrder, "FINAL_OPTION_FLAG");
				 int hisLastDirId = this.getIntByKey(hisOrder, "RELA_TYPE");
				 
				 repeatNewOrder++;//重复投诉
				 labelManageDAO.insertServiceConnection(sysGuid1, hisOrderId, 0, "Z", hisAcceptDate);//重复投诉打标
				 
				 boolean bestRepeatFlag = this.bestRepeatFlag(prodNum, hisProdNum, bestOrder, hisBestOrder);
				 if(bestRepeatFlag) {
					 bestRepeatOrder++;//最严工单重复
					 labelManageDAO.insertServiceConnection(sysGuid2, hisOrderId, 0, "Y", hisAcceptDate);//最严工单重复打标
				 }
				 
				 if(complaintFlag) {
					 complaintRepeatOrder++;//申诉投诉重复
					 labelManageDAO.insertServiceConnection(sysGuid3, hisOrderId, 0, "E", hisAcceptDate);//申诉投诉重复打标
				 }
				 
				 if(unifiedFlag && hisBestOrder > 100122410) {
					 complaintRepeatBestOrder++;//投诉咨询单最严工单重复
					 labelManageDAO.insertServiceConnection(sysGuid4, hisOrderId, 0, "F", hisAcceptDate);//投诉咨询单最严工单重复打标
				 }
				 
				 upTendencyNum = this.getUpTendencyNum(comeCategory, upTendencyNum);//越级工单次数
				 satisfyNum = this.getSatisfyNum(tsDealResult, satisfyNum);//满意工单次数
				 unsatisfyNum = this.getUnsatisfyNum(tsDealResult, unsatisfyNum);//不满意工单次数
				 complaintNum = this.getComplaintNum(serviceType, prodNum, hisProdNum, complaintNum);//投诉次数
				 finalOptionOrderId = this.getFinalOptionOrderId(finalOptionFlag, serviceType, hisOrderId, finalOptionOrderId);//最终处理意见投诉单
				 finalOptionOrderNum = this.getFinalOptionOrderNum(finalOptionOrderId, hisOrderId, finalOptionOrderNum);//30天内有最终处理意见工单量
				 sensitiveNum = this.getSensitiveNum(lastDirId, hisLastDirId, prodNum, hisProdNum, sensitiveNum);
			}
			
			//重复投诉
			this.setRepeatNewFlag(label, repeatNewOrder, sysGuid1, serviceOrderId);
			//最严工单重复
			this.setRepeatFlag(label, bestRepeatOrder, sysGuid2, serviceOrderId);
			//申诉投诉重复
			this.setCptRepeatFlag(label, complaintRepeatOrder, sysGuid3, serviceOrderId);
			//投诉咨询单最严工单重复
			this.setCptRepeatBestFlag(label, complaintRepeatBestOrder, sysGuid4, serviceOrderId);
		}
		//敏感话务标签打标
		label.setSensitiveNum(sensitiveNum);
		
		//30天越级投诉次数
		persona.setUpTendencyNum(upTendencyNum);
		//30天工单评价：满意
		persona.setSatisfyNum(satisfyNum);
		//30天工单评价：不满意
		persona.setUnsatisfyNum(unsatisfyNum);
		//偏好10000号投诉：30天内超过4次投诉
		persona.setPreferComplaint(this.getPreferComplaint(complaintNum));
		//30天内有最终处理意见的最近一张投诉单号
		persona.setFinalOptionOrderId(finalOptionOrderId);
		//30天内有最终处理意见工单量
		persona.setFinalOptionOrderNum(finalOptionOrderNum);
	}
	
	/**
	 * 敏1打标
	 * @param lastDirId
	 * @return
	 */
	private int setFirstSensitiveNum(int lastDirId) {
		if(lastDirId == 1059901) {//敏感标签号码的首次投诉为敏1
			return 1;
		}
		return 0;
	}
	
	private int getComplaintNum(int serviceType, String prodNum, String hisProdNum, int complaintNum) {
		if(serviceType == 720130000 && hisProdNum.equals(prodNum)) {
			complaintNum++;
		}
		return complaintNum;
	}
	
	private int getFinalOptionOrderNum(String finalOptionOrderId, String hisOrderId, int finalOptionOrderNum) {
		 if(finalOptionOrderId.equals(hisOrderId)) {
			 finalOptionOrderNum++;
		 }
		 return finalOptionOrderNum;
	}
	
	private int getPreferComplaint(int complaintNum) {
		return complaintNum > 4 ? 1 : 0;
	}
	
	private String getFinalOptionOrderId(String finalOptionFlag, int serviceType, String hisOrderId, String finalOptionOrderId) {
		if(StringUtils.isNotBlank(finalOptionFlag) && serviceType == 720130000) {
			return hisOrderId;
		}
		return finalOptionOrderId;
	}

	/**
	 * 重复查询
	 */
	@SuppressWarnings("rawtypes")
	public void updateRepeatFlagCX(ServiceLabel label, CustomerPersona persona, OrderAskInfo orderAskInfo) {
		String serviceOrderId = label.getServiceOrderId();
		String prodNum = orderAskInfo.getProdNum();
		String relaInfo = orderAskInfo.getRelaInfo();
		int regionId = orderAskInfo.getRegionId();
		// 产品号码、联系电话查询近30天投诉、咨询、查询单
		List orderList = orderAskInfoDao.queryAllOrderCX(serviceOrderId, prodNum, relaInfo, regionId);
		label.setRepeatNewFlag(0);
		label.setRepeatFlag(0);
		label.setCptRepeatFlag(0);
		label.setCptRepeatBestFlag(0);
		int repeatNewOrder = 0;
		
		int upTendencyNum = 0;
		int satisfyNum = 0;
		int unsatisfyNum = 0;
		int complaintNum = 0;
		String finalOptionOrderId = "";
		int finalOptionOrderNum = 0;
		if (!orderList.isEmpty()) {
			String sysGuid1 = pubFun.crtGuid();
			for (int i = 0; i < orderList.size(); i++) {
				Map hisOrder = (Map) orderList.get(i);
				String hisOrderId = this.getStringByKey(hisOrder, "SERVICE_ORDER_ID");
				String hisProdNum = this.getStringByKey(hisOrder, "PROD_NUM");
				String hisAcceptDate = this.getStringByKey(hisOrder, "ACCEPT_DATE");
				int comeCategory = this.getIntByKey(hisOrder, "COME_CATEGORY");
				int tsDealResult = this.getIntByKey(hisOrder, "TS_DEAL_RESULT");
				int serviceType = this.getIntByKey(hisOrder, "SERVICE_TYPE");
				 String finalOptionFlag = this.getStringByKey(hisOrder, "FINAL_OPTION_FLAG");
				if(StaticData.SERV_TYPE_CX == serviceType) {
					repeatNewOrder++;// 重复投诉
					labelManageDAO.insertServiceConnection(sysGuid1, hisOrderId, 0, "Z", hisAcceptDate);// 重复投诉打标
				} else {
					upTendencyNum = this.getUpTendencyNum(comeCategory, upTendencyNum);// 越级工单次数
					satisfyNum = this.getSatisfyNum(tsDealResult, satisfyNum);// 满意工单次数
					unsatisfyNum = this.getUnsatisfyNum(tsDealResult, unsatisfyNum);// 不满意工单次数
					complaintNum = this.getComplaintNum(serviceType, prodNum, hisProdNum, complaintNum);// 投诉次数
					finalOptionOrderId = this.getFinalOptionOrderId(finalOptionFlag, serviceType, hisOrderId, finalOptionOrderId);// 最终处理意见投诉单
					finalOptionOrderNum = this.getFinalOptionOrderNum(finalOptionOrderId, hisOrderId, finalOptionOrderNum);//30天内有最终处理意见工单量
				}
			}
			// 重复投诉
			this.setRepeatNewFlag(label, repeatNewOrder, sysGuid1, serviceOrderId);
		}
		
		//30天越级投诉次数
		persona.setUpTendencyNum(upTendencyNum);
		//30天工单评价：满意
		persona.setSatisfyNum(satisfyNum);
		//30天工单评价：不满意
		persona.setUnsatisfyNum(unsatisfyNum);
		//偏好10000号投诉：30天内超过4次投诉
		persona.setPreferComplaint(this.getPreferComplaint(complaintNum));
		//30天内有最终处理意见的最近一张投诉单号
		persona.setFinalOptionOrderId(finalOptionOrderId);
		//30天内有最终处理意见工单量
		persona.setFinalOptionOrderNum(finalOptionOrderNum);
}

	@SuppressWarnings("rawtypes")
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	@SuppressWarnings("rawtypes")
	private int getIntByKey(Map map, String key) {
		return Integer.parseInt(map.get(key) == null ? "0" : map.get(key).toString());
	}
	
	private boolean bestRepeatFlag (String prodNum, String hisProdNum, int bestOrder, int hisBestOrder) {
		return prodNum.equals(hisProdNum) && bestOrder > 100122410 && hisBestOrder > 100122410;
	}
	
	private void setRepeatNewFlag(ServiceLabel label, int repeatNewOrder, String sysGuid, String serviceOrderId) {
		if(repeatNewOrder > 0) {
			label.setRepeatNewFlag(repeatNewOrder);//重复投诉
			labelManageDAO.insertServiceConnection(sysGuid, serviceOrderId, 1, "Z", "");
		}
	}
	
	private void setRepeatFlag(ServiceLabel label, int bestRepeatOrder, String sysGuid, String serviceOrderId) {
		if(bestRepeatOrder > 0) {
			label.setRepeatFlag(1);//最严工单重复
			labelManageDAO.insertServiceConnection(sysGuid, serviceOrderId, 1, "Y", "");
		}
	}
	
	private void setCptRepeatFlag(ServiceLabel label, int complaintRepeatOrder, String sysGuid, String serviceOrderId) {
		if(complaintRepeatOrder > 0) {
			label.setCptRepeatFlag(1);//申诉是否产品号码、联系号码重复
			labelManageDAO.insertServiceConnection(sysGuid, serviceOrderId, 1, "E", "");
		}
	}
	
	private void setCptRepeatBestFlag(ServiceLabel label, int complaintRepeatBestOrder, String sysGuid, String serviceOrderId) {
		if(complaintRepeatBestOrder > 0) {
			label.setCptRepeatBestFlag(1);//投诉咨询单是否产品号码、联系号码最严工单重复
			labelManageDAO.insertServiceConnection(sysGuid, serviceOrderId, 1, "F", "");
		}
	}
	
	private int getUpTendencyNum(int comeCategory, int upTendencyNum) {
		if(comeCategory == 707907002 || comeCategory == 707907003) {//集团投诉、申诉
			upTendencyNum++;
		}
		return upTendencyNum;
	}
	
	private int getSatisfyNum(int tsDealResult, int satisfyNum) {
		if(tsDealResult == 600001166) {//满意
			satisfyNum++;
		}
		return satisfyNum;
	}
	
	private int getUnsatisfyNum(int tsDealResult, int unsatisfyNum) {
		if(tsDealResult == 600001168) {//不满意
			unsatisfyNum++;
		}
		return unsatisfyNum;
	}
	
	private int getSensitiveNum(int lastDirId, int hisLastDirId, String prodNum, String hisProdNum, int sensitiveNum) {
		if(lastDirId == 1059901 && lastDirId == hisLastDirId && prodNum.equals(hisProdNum)) {//30天内相同产品的敏感话务重复次数
			sensitiveNum++;
		}
		return sensitiveNum;
	}

}