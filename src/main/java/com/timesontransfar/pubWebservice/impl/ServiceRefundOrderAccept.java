package com.timesontransfar.pubWebservice.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.util.DateUtil;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.pojo.*;
import com.timesontransfar.customservice.orderask.service.ITrackService;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.feign.custominterface.WorkSheetFeign;
import com.timesontransfar.pubWebservice.IpubwebServiceDao;
import com.timesontransfar.systemPub.entity.PubColumn;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ServiceRefundOrderAccept {
    protected Logger logger = LoggerFactory.getLogger(ServiceRefundOrderAccept.class);

    @Autowired
    private IpubwebServiceDao pubwebServiceDaoImpl;

    @Autowired
    private PubFunc pubFun;

    @Autowired
    private CustomerServiceFeign customerServiceFeign;

    @Autowired
    private IserviceOrderAsk serviceOrderAskImpl;
    
    @Autowired
    private WorkSheetFeign workSheetFeign;
    
	@Autowired
	private ITrackService trackServiceImpl;


	/**
	 * 录单接口(小额退赔专用)
	 * 
	 * @param orderInfo
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String saveServOrderForIVR(String orderInfo) {
		logger.info("serviceRefundAccpet 入参: {}", orderInfo);
		String returnInfo = "";
		IntfLog log = new IntfLog();
		log.setServOrderId("");
		log.setInMsg(orderInfo);
		log.setActionFlag("in");
		log.setActionResult("0");
		try {
			Document doc = returnSaxRed(orderInfo);
			returnInfo = checkServiceOrderInfo(doc);
			if (StringUtils.isNotBlank(returnInfo)) {
				return returnInfo;
			}
			// 来源系统
			String system = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SOURCE_SYSTEM");// 来源
			log.setSystem(system);
			
			// 客户基本信息
			String strRegion = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");// 地域
			int regionId = Integer.parseInt(strRegion);
			String regionName = pubFun.getRegionName(regionId);
			String prodType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODTYPE");// 产品类型
			String prodnum = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");// 产品号码
			log.setServOrderId(prodnum);

			// 受理单信息
			String serviceType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPE");// 服务类型
			String serviceTypeDesc = pubFun.getStaticName(Integer.parseInt(serviceType));
			String relaman = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAMAN");// 联系人
			String relainfo = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAINFO");// 联系信息
			String acceptStaff = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTSTAFFID");// 受理员工
			String acceptorgid = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTORGID");// 受理部门
			String acceptorgname = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTORGNAME");// 受理部门
			String servicedate = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICEDATE");// SERVICEDATEDESC
			String servicedatedesc = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICEDATEDESC");// SERVICEDATEDESC
			String moreRelaInfo = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/MORERELAINFO");// 更多联系方式
			String bestOrder = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/BESTORDER");// 最严工单
			String sixCatalog = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SIXCATALOG");// 受理目录
			String upTendencyFlag = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/UPTENDENCYFLAG");// 越级倾向
			String isOwner = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ISOWNER");// 是否机主
			String isAutoFinish = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/IS_AUTO_FINISH");// 是否自动办结
			
			//受理员工
			TsmStaff staff = pubFun.getLogonStaffByLoginName(acceptStaff);
			String msg = this.checkFields(staff, moreRelaInfo, isOwner);
			if(StringUtils.isNotBlank(msg)){
				return msg;
			}
			List<ServiceContentSave> list = new ArrayList<>();
			ServiceContent servContent = new ServiceContent();//先赋值受理内容
			OrderCustomerInfo orderCustInfo = getOrderCustomerInfo(prodType, prodnum, regionId);
			if (orderCustInfo == null) {
				return buildReturnInfo("1", "1", "查询客户资料为空，请确认传入数据是否正确", "", "");
			}
			String prodInstId = orderCustInfo.getProdInstId();
			String accNbr = orderCustInfo.getAccNbr();
			int custProdType = orderCustInfo.getProdType();
			String acctId = customerServiceFeign.qryAcctId(accNbr, strRegion,String.valueOf(custProdType));
			int lastXX = Integer.parseInt(sixCatalog);
			List<Object> catalogList = pubFun.queryAcceptDir(lastXX, 2);
			if (catalogList.isEmpty()){
				return buildReturnInfo("1", "1", "受理现象传值错误", "", "");
			}
			Map<String, String> catalogMap = (Map) catalogList.get(0);
			String ns = catalogMap.get("N");
			String ids = catalogMap.get("ID");
			String checkContentSave = this.setServiceContentSave(list, sixCatalog, doc,servContent,prodInstId,acctId,ns);
			if(StringUtils.isNotBlank(checkContentSave)){
				return checkContentSave;
			}
			ServiceContentSave[] array = new ServiceContentSave[list.size()];
			ServiceContentSave[] saves = list.toArray(array);

			
			OrderAskInfo orderAskInfo = new OrderAskInfo();
			String sourceId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SOURCEID");// 来源
			String checkSourceId = this.setAskChannelId(orderAskInfo, sourceId);
			if(StringUtils.isNotBlank(checkSourceId)){
				return checkSourceId;
			}

			int isO = 0;
			if ("1".equals(isOwner)) {
				isO = 1;
			}

			int custstratagemid = orderCustInfo.getCustStratagemId();// 客户群
			String custstratagemname = "";
			if(custstratagemid == 0) {
				custstratagemid = 700001879;
				custstratagemname = "公众客户";
			} else {
				custstratagemname = pubwebServiceDaoImpl.getStaticName(custstratagemid);// 客户群
			}
			orderCustInfo.setCustStratagemName(custstratagemname);
			orderCustInfo.setCustServGradeName(pubwebServiceDaoImpl.getStaticName(orderCustInfo.getCustServGrade()));
			orderCustInfo.setCustBrandContenDesc(pubwebServiceDaoImpl.getStaticName(orderCustInfo.getCustBrand()));
			orderCustInfo.setIdType(0);
			orderCustInfo.setIdCard("");

			orderAskInfo.setCallSerialNo("N");
			orderAskInfo.setRelaType(lastXX);
			orderAskInfo.setOrderVer(0);
			orderAskInfo.setRegionId(regionId);
			orderAskInfo.setRegionName(regionName);
			orderAskInfo.setAreaId(orderCustInfo.getAreaId());
			orderAskInfo.setAreaName(orderCustInfo.getAreaName());
			orderAskInfo.setSubStationId(orderCustInfo.getSubStationId());
			orderAskInfo.setSubStationName(orderCustInfo.getSubStationName());
			orderAskInfo.setServType(Integer.parseInt(serviceType));
			orderAskInfo.setServTypeDesc(serviceTypeDesc);
			orderAskInfo.setRelaMan(relaman);
			orderAskInfo.setProdNum(prodnum);
			orderAskInfo.setRelaInfo(relainfo);
			orderAskInfo.setAskStaffId(Integer.parseInt(staff.getId()));
			orderAskInfo.setAskStaffName(staff.getName());
			orderAskInfo.setAskOrgId(acceptorgid);
			orderAskInfo.setAskOrgName(acceptorgname);
			orderAskInfo.setCustServGrade(orderCustInfo.getCustServGrade());
			orderAskInfo.setCustServGradeDesc(orderCustInfo.getCustServGradeName());
			orderAskInfo.setAskCount(0);
			orderAskInfo.setCustGroup(custstratagemid);
			orderAskInfo.setCustGroupDesc(custstratagemname);
			orderAskInfo.setServiceDate(Integer.parseInt(servicedate));
			orderAskInfo.setServiceDateDesc(servicedatedesc);
			orderAskInfo.setAssistSellNo("");
			orderAskInfo.setIsOwner(isO);
			orderAskInfo.setMoreRelaInfo(StringUtils.substring(moreRelaInfo, 0, 20));
			orderAskInfo.setEmergency(upTendencyFlag);
			orderAskInfo.setUrgencyGrade(700000145);
			orderAskInfo.setUrgencyGradeDesc("普通");
			orderAskInfo.setCustEmotion(0);
			orderAskInfo.setCustEmotionDesc("");
			orderAskInfo.setComment(catalogMap.get("N"));
			orderAskInfo.setCityLabel("未知");

			// 设置对象
			servContent.setOrderVer(0);
			servContent.setRegionId(regionId);
			servContent.setRegionName(regionName);
			servContent.setServType(Integer.parseInt(serviceType));
			servContent.setServTypeDesc(serviceTypeDesc);
			servContent.setServiceTypeDetail(this.getServiceTypeDetail(serviceType, sixCatalog, system));
			servContent.setAppealProdId(pubFun.getSplitIdByIdx(ids, 0));
			servContent.setAppealProdName(pubFun.getSplitNameByIdx(ns, 0));
			servContent.setAppealReasonId(pubFun.getSplitIdByIdx(ids, 1));
			servContent.setAppealReasonDesc(pubFun.getSplitNameByIdx(ns, 1));
			servContent.setAppealChild(pubFun.getSplitIdByIdx(ids, 2));
			servContent.setAppealChildDesc(pubFun.getSplitNameByIdx(ns, 2));
			servContent.setFouGradeCatalog(pubFun.getSplitIdByIdx(ids, 3));
			servContent.setFouGradeDesc(pubFun.getSplitNameByIdx(ns, 3));
			servContent.setFiveCatalog(pubFun.getSplitIdByIdx(ids, 4));
			servContent.setFiveGradeDesc(pubFun.getSplitNameByIdx(ns, 4));
			servContent.setSixCatalog(pubFun.getSplitIdByIdx(ids, 5));
			servContent.setSixGradeDesc(pubFun.getSplitNameByIdx(ns, 5));
			servContent.setProdNum(prodnum);
			servContent.setAppealDetailId(0);
			servContent.setAppealDetailDesc("");
			servContent.setBestOrder(100122410);
			servContent.setBestOrderDesc("否");
			
			this.setProdCode(orderCustInfo, servContent);
			this.setSpecialContent(servContent, sixCatalog, bestOrder);
			this.setDevChnl(orderCustInfo, servContent);

			Map<String, String> map = new HashMap<>();
			map.put("ORGIDSTR", "");
			map.put("STRFLOW", "DISPATCHSHEET");
			map.put("SENDFLAG", "true");
			map.put("DEALREQUIE", "");
			map.put("ZHZDFLAG", "1");
			if(this.isRefundOrder(serviceType, sixCatalog)) {//小额退赔查询单
				map.put("WORKSHEET_ALLOT", "adjust_account");
			}
			logger.info("orderCustInfo: {}", JSON.toJSON(orderCustInfo));
			logger.info("serviceContents: {}", JSON.toJSON(servContent));
			logger.info("orderAskInfos: {}", JSON.toJSON(orderAskInfo));
			logger.info("otherInfo: {}", JSON.toJSON(map));
			logger.info("serviceContentSave: {}", JSON.toJSON(saves));
			String resultInfo = serviceOrderAskImpl.submitServiceOrderInstanceLabelNew(orderCustInfo, servContent,
					orderAskInfo, map, null, saves, null);
			String code = JSONObject.fromObject(resultInfo).optString("code");
			if ("0000".equals(code)) {
				String servId = JSONObject.fromObject(resultInfo).optString("resultObj");
				serviceOrderAskImpl.autoPdAsync(servId);// 自动转派
				//向智慧预警kafka发送数据
				if("720130000".equals(serviceType)) {
					workSheetFeign.sendServiceOrder(servId);
				}
				//保存小额退赔跟踪单
				this.submitRefundOrder(map, saves, servId, acceptStaff, isAutoFinish);
				log.setServOrderId(servId);
				log.setActionResult("1");
				returnInfo = buildReturnInfo("0", "0", "生成工单成功", servId, orderCustInfo.getCrmCustId());
			} else {
				returnInfo = buildReturnInfo("1", "1", "生成工单失败", "", "");
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		} catch (Exception e) {
			logger.error("serviceRefundAccpet 生成工单异常：{}", e.getMessage(), e);
			returnInfo = buildReturnInfo("1", "1", e.getMessage(), "", "");
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
	}

	private String checkFields(TsmStaff staff,String moreRelaInfo,String isOwner){
		if(staff == null) {
			return buildReturnInfo("1", "1", "受理员工工号有误", "", "");
		}
		if(StringUtils.isNotBlank(moreRelaInfo) && moreRelaInfo.length()>20){
			return buildReturnInfo("1", "1", "MORERELAINFO更多联系方式超长", "", "");
		}
		if(StringUtils.isNotBlank(isOwner) && (!"0".equals(isOwner) && !"1".equals(isOwner))){
			return buildReturnInfo("1", "1", "是否机主字段：ISOWNER 不符合规范", "", "");
		}
		return null;
	}
	
	private void submitRefundOrder(Map<String, String> otherInfo, ServiceContentSave[] saves, String orderId, String acceptStaff, String isAutoFinish) {
		logger.info("orderId: {} isAutoFinish: {}", orderId, isAutoFinish);
		if("1".equals(isAutoFinish)) {
			return;
		}
		if (otherInfo.containsKey("WORKSHEET_ALLOT") && otherInfo.get("WORKSHEET_ALLOT") != null) {
			try {
				String allot = otherInfo.get("WORKSHEET_ALLOT");
				if ("adjust_account".equals(allot)) {//查询单 全渠道 小额退赔 直接调账
					JSONObject track = new JSONObject();
					track.put("oldOrderId", orderId);
					track.put("trackType", "1");
					track.put("createType", 3);
					track.put("refundMode", 99);
					track.put("refundModeDesc", "无");
					
					JSONObject parm = new JSONObject();
					parm.put("acceptStaff", acceptStaff);
					parm.put("isDispatch", "1");
					parm.put("sendToObjId", "363843");
					parm.put("track", track);
					//退费数据
					this.setRefundData(saves, parm);
					String result = trackServiceImpl.createTrackServiceTZ(parm.toString());
					logger.info("createTrackServiceTZ result: {}", result);
				}
			}
			catch(Exception e) {
				logger.error("submitRefundOrder error: {}", e.getMessage(), e);
			}
		}
	}
	
	private void setRefundData(ServiceContentSave[] saves, JSONObject parm) {
		String refundData = "";
		String refundsAccNum = "";
		String refundAmount = "";
		String prmRefundAmount = "";
		try {
			if(saves.length > 0){
				for(int i=0;i<saves.length;i++) {
					ServiceContentSave ww = saves[i];
					if("c2f9995733b843c8393cc78629cd9220".equals(ww.getElementId())) {//退费数据
						//去掉特殊符号
						refundData = org.apache.commons.lang3.StringUtils.removeEnd(org.apache.commons.lang3.StringUtils.removeStart(ww.getAnswerName(), "【"), "】");
					}
					if("b6b2882c9e1811ee89ee005056b35a1f".equals(ww.getElementId())) {//承诺退费金额
						prmRefundAmount = ww.getAnswerName();
					}
				}
			}
			if(org.apache.commons.lang3.StringUtils.isNotBlank(refundData)) {
				JSONObject json = JSONObject.fromObject(refundData);
				refundsAccNum = json.optString("refundsAccNum");
				refundAmount = json.optString("cashAmount");
			}
		} catch (Exception e) {
			logger.error("setRefundData error: {}", e.getMessage(), e);
		}
		logger.info("refundData: {}", refundData);
		
		parm.put("refundData", refundData);
		parm.put("refundsAccNum", refundsAccNum);
		parm.put("refundAmount", refundAmount);
		parm.put("prmRefundAmount", prmRefundAmount);
	}

	private Document returnSaxRed(String str) throws SAXException, DocumentException {
		SAXReader saxReader = new SAXReader();
		saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		StringReader reader = new StringReader(str);
		return saxReader.read(reader);
	}
	
	private void setProdCode(OrderCustomerInfo custInfo, ServiceContent servContent) {
		switch(custInfo.getProdType()) {
			case 1:
			case 100000002:
				servContent.setProdOne(503);
				servContent.setProdOneDesc("普通电话");
				servContent.setProdTwo(503004);
				servContent.setProdTwoDesc("固话");
				break;
			case 2:
			case 100000009:
			case 100000011:
			case 100000012:
			case 100000014:
				servContent.setProdOne(502);
				servContent.setProdOneDesc("宽带");
				servContent.setProdTwo(502001);
				servContent.setProdTwoDesc("光纤宽带");
				break;
			case 9:
			case 100000379:
				servContent.setProdOne(501);
				servContent.setProdOneDesc("手机");
				servContent.setProdTwo(501002);
				servContent.setProdTwoDesc("移动数据");
				break;
			case 881:
			case 100000881:
				servContent.setProdOne(504);
				servContent.setProdOneDesc("天翼高清");
				servContent.setProdTwo(504001);
				servContent.setProdTwoDesc("基础功能");
				break;
			default:
				servContent.setProdOne(501);
				servContent.setProdOneDesc("手机");
				servContent.setProdTwo(501010);
				servContent.setProdTwoDesc("其他移动业务");
				break;
		}
	}
	
	private void setDevChnl(OrderCustomerInfo custInfo, ServiceContent servContent) {
		int devtChsOne = custInfo.getDevtChsOne();
		int devtChsTwo = custInfo.getDevtChsTwo();
		int devtChsThree = custInfo.getDevtChsThree();
		if(devtChsOne == 0 || devtChsTwo == 0 || devtChsThree == 0) {
			devtChsOne = 100000;//直销渠道
			devtChsTwo = 100100;//自有直销单元
			devtChsThree = 100101;//行业直销单元
		}
		servContent.setDvlpChnl(custInfo.getDvlpChnl());
		servContent.setDvlpChnlNm(custInfo.getDvlpChnlNm());
		servContent.setDevtChsOne(devtChsOne);
		servContent.setDevtChsOneDesc(pubFun.getStaticName(devtChsOne));
		servContent.setDevtChsTwo(devtChsTwo);
		servContent.setDevtChsTwoDesc(pubFun.getStaticName(devtChsTwo));
		servContent.setDevtChsThree(devtChsThree);
		servContent.setDevtChsThreeDesc(pubFun.getStaticName(devtChsThree));
	}
	
	private void setSpecialContent(ServiceContent servContent, String sixCatalog, String bestOrder) {
		String bestOrderFlag = pubFun.getBestOrderFlag(sixCatalog, "1");//获取投诉现象对应的最严场景
		if(StringUtils.isBlank(bestOrderFlag)) {
			return;
		}
		if(StringUtils.isNotEmpty(bestOrder) && StringUtils.isNumeric(bestOrder) && bestOrder.length() < 11) {
			String bestOrderDesc = pubFun.getStaticName(Integer.parseInt(bestOrder));
			if(StringUtils.isNotEmpty(bestOrderDesc)) {
				servContent.setBestOrder(Integer.parseInt(bestOrder));
				servContent.setBestOrderDesc(bestOrderDesc);
			}
		}
	}

	private String checkServiceOrderInfo(Document doc) {
		String errorMsg = "";
		String sourceSystem = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SOURCE_SYSTEM");
		if (StringUtils.isEmpty(sourceSystem)) {
			return buildReturnInfo("1", "1", "录单来源为空", "", "");
		}
		if(!Arrays.asList("IVR").contains(sourceSystem) && !Arrays.asList("10000").contains(sourceSystem)) {
			return buildReturnInfo("1", "1", "录单来源不在允许范围", "", "");
		}
		errorMsg = checkCustInfo(doc);
		if (!StringUtils.isEmpty(errorMsg)) {
			return errorMsg;
		}
		errorMsg = checkOrderInfo(doc);
		if (!StringUtils.isEmpty(errorMsg)) {
			return errorMsg;
		}
		return errorMsg;
	}

	private String checkCustInfo(Document doc) {
		String regionid = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");
		if (StringUtils.isEmpty(regionid)) {
			return buildReturnInfo("1", "1", "地域ID为空", "", "");
		}
		if(regionid.length()>2){
			return buildReturnInfo("1", "1", "地域ID格式不符合要求", "", "");
		}
		String prodType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODTYPE");
		if (StringUtils.isEmpty(prodType)) {
			return buildReturnInfo("1", "1", "产品类型为空", "", "");
		}
		if (prodType.length()>3) {
			return buildReturnInfo("1", "1", "产品类型格式不符合要求", "", "");
		}
		int queryType = Integer.parseInt(prodType);
		if (!(1 == queryType || 2 == queryType || 9 == queryType || 881 == queryType)) {
			return buildReturnInfo("1", "1", "产品类型取值超过范围", "", "");
		}
		String prodNum = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");
		if (StringUtils.isEmpty(prodNum)) {
			return buildReturnInfo("1", "1", "产品号码为空", "", "");
		}
		if (prodNum.length()>20) {
			return buildReturnInfo("1", "1", "产品号码位数超过限制", "", "");
		}
		return "";
	}

	private String checkOrderInfo(Document doc) {
		String serviceType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPE");
		if (StringUtils.isEmpty(serviceType)) {
			return buildReturnInfo("1", "1", "服务类型为空", "", "");
		}
		if (isServiceType(serviceType)) {
			return buildReturnInfo("1", "1", "服务类型取值超过范围", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAMAN"))) {
			return buildReturnInfo("1", "1", "联系人为空", "", "");
		}
		if (getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAMAN").length()>50) {
			return buildReturnInfo("1", "1", "联系人超长", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAINFO"))) {
			return buildReturnInfo("1", "1", "联系信息为空", "", "");
		}
		if (getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAINFO").length()>20) {
			return buildReturnInfo("1", "1", "联系信息超长", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTSTAFFID"))) {
			return buildReturnInfo("1", "1", "受理员工ID为空", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTORGID"))) {
			return buildReturnInfo("1", "1", "受理部门ID为空", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTORGNAME"))) {
			return buildReturnInfo("1", "1", "受理部门名为空", "", "");
		}
		if (!"3".equals(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICEDATE"))) {
			return buildReturnInfo("1", "1", "流程号不符合规范", "", "");
		}
		if (!"投诉".equals(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICEDATEDESC"))) {
			return buildReturnInfo("1", "1", "流程名不符合规范", "", "");
		}
		String sixCatalog = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SIXCATALOG");
		if (StringUtils.isEmpty(sixCatalog)) {
			return buildReturnInfo("1", "1", "受理目录为空", "", "");
		}
		if(!this.checkSixCatalog(serviceType, sixCatalog)) {//小额退赔查询单
			return buildReturnInfo("1", "1", "服务类型与受理目录不匹配", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SOURCEID"))) {
			return buildReturnInfo("1", "1", "投诉来源为空", "", "");
		}
		if (!Arrays.asList("", "0", "1", "2", "3").contains(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/UPTENDENCYFLAG"))) {
			return buildReturnInfo("1", "1", "越级倾向传值不规范", "", "");
		}
		return "";
	}
	
	/**
	 * 小额退赔查询单
	 */
	private boolean isRefundOrder(String serviceType, String sixCatalog) {
		return "720200003".equals(serviceType) && "2020501".equals(sixCatalog);
	}
	
	private String getServiceTypeDetail(String serviceType, String sixCatalog, String sourceSystem) {
		String detail = "";
		if("720200003".equals(serviceType) && "2020501".equals(sixCatalog)) {//小额退赔查询单
			if("IVR".equals(sourceSystem)) {
				detail = "IVR自动受理";
			} else if("10000".equals(sourceSystem)) {
				detail = "10000号自动受理";
			}
		}
		return detail;
	}
	
	/**
	 * 校验投诉现象
	 */
	private boolean checkSixCatalog(String serviceType, String sixCatalog) {
		return ("720200003".equals(serviceType) && "2020501".equals(sixCatalog)) || ("720130000".equals(serviceType) && "10401".equals(sixCatalog));
	}

	private boolean isServiceType(String serviceType) {
		return !("720130000".equals(serviceType) || "720200003".equals(serviceType));
	}

	private String buildReturnInfo(String retflag, String reterrorid, String reterrorinfo, String serviceid,
			String custid) {
		return "<?xml version=\"1.0\" encoding=\"GBK\"?><REQUEST_XML><RETFLAG>" + retflag + "</RETFLAG><RETERRORID>"
				+ reterrorid + "</RETERRORID><RETERRORINFO>" + reterrorinfo + "</RETERRORINFO><SERVICEID>" + serviceid
				+ "</SERVICEID><CUSTID>" + custid + "</CUSTID></REQUEST_XML>";
	}

	private OrderCustomerInfo getOrderCustomerInfo(String prodType, String prodnum, int regionId) {
		OrderCustomerInfo orderCustInfo = new OrderCustomerInfo();
		String queryCust = customerServiceFeign.getCustInfo(Integer.parseInt(prodType), prodnum, regionId);
		JSONObject relJson = JSONObject.fromObject(queryCust);
		if ("0000".equals(relJson.getString("code"))) {
			orderCustInfo = (OrderCustomerInfo) JSONObject.toBean(relJson.getJSONObject("resultObj"), OrderCustomerInfo.class);
			if (StringUtils.isEmpty(orderCustInfo.getCustName())) {
				orderCustInfo = null;
			}
		}
		return orderCustInfo;
	}

	private String setAskChannelId(OrderAskInfo orderAskInfo, String sourceId) {
		PubColumn column = pubwebServiceDaoImpl.getPubColumn(sourceId);
		if(column == null) {
			return buildReturnInfo("1", "1", "受理渠道ID传值有误，请核实", "", "");
		}
		if("CHANNEL_DETAIL_ID".equals(column.getColCode())) {
			orderAskInfo.setChannelDetailId(Integer.parseInt(column.getReferId()));
			orderAskInfo.setChannelDetailDesc(column.getColValueName());
			
			column = pubwebServiceDaoImpl.getPubColumn(column.getEntityId());
			orderAskInfo.setAskChannelId(Integer.parseInt(column.getReferId()));
			orderAskInfo.setAskChannelDesc(column.getColValueName());
			
			column = pubwebServiceDaoImpl.getPubColumn(column.getEntityId());
			orderAskInfo.setAskSource(Integer.parseInt(column.getReferId()));
			orderAskInfo.setAskSourceDesc(column.getColValueName());
			
			column = pubwebServiceDaoImpl.getPubColumn(column.getEntityId());
			orderAskInfo.setComeCategory(Integer.parseInt(column.getReferId()));
			orderAskInfo.setCategoryName(column.getColValueName());
		} else if("ACCEPT_CHANNEL_ID".equals(column.getColCode())) {
			orderAskInfo.setAskChannelId(Integer.parseInt(column.getReferId()));
			orderAskInfo.setAskChannelDesc(column.getColValueName());
			
			column = pubwebServiceDaoImpl.getPubColumn(column.getEntityId());
			orderAskInfo.setAskSource(Integer.parseInt(column.getReferId()));
			orderAskInfo.setAskSourceDesc(column.getColValueName());
			
			column = pubwebServiceDaoImpl.getPubColumn(column.getEntityId());
			orderAskInfo.setComeCategory(Integer.parseInt(column.getReferId()));
			orderAskInfo.setCategoryName(column.getColValueName());
		} else {
			return buildReturnInfo("1", "1", "受理渠道ID传值有误，请核实", "", "");
		}
		return null;
	}

	/**
	 * 返回xpath对应元素的值
	 * 
	 * @param doc
	 * @param xpath
	 * @return
	 */
	private String getElementValue(Document doc, String xpath) {
		Element e = (Element) doc.selectSingleNode(xpath);
		if (e != null)
			return e.getStringValue().trim();
		else
			return "";
	}

	private List<Node> getNodeList(Document doc, String xpath) {
		return doc.selectNodes(xpath);
	}

	private String getElementValue(Node node, String xpath) {
		Node n =  node.selectSingleNode(xpath);
		if (n != null)
			return n.getStringValue().trim();
		else
			return "";
	}
	
	private String setServiceContentSave(List<ServiceContentSave> list, String sixCatalog, Document doc,ServiceContent servContent,String prodInstId,String acctId,String ns){
		if("2020501".equals(sixCatalog)) {
			return this.setRechargeContentSave(list, doc,servContent,prodInstId,acctId);
		}
		if("10401".equals(sixCatalog)){
			return this.setDisputeContentSave(list,doc,ns,servContent);
		}
		return null;
	}

	private String setRechargeContentSave(List<ServiceContentSave> list, Document doc,ServiceContent servContent,String prodInstId,String acctId){
		JSONArray jsonArray = new JSONArray();
		String refundDetail = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/refundDetail");
		boolean checkRechargeFlag = buildRechargeArray(doc, jsonArray, refundDetail);
		if(!checkRechargeFlag){
			return buildReturnInfo("1", "1", "退费信息recharge节点不能为空", "", "");
		}
		if(StringUtils.isBlank(refundDetail)) {
			String checkRecargeValue = this.checkRecargeValue(jsonArray);
			if(StringUtils.isNotBlank(checkRecargeValue)){//检查recharge数据
				return checkRecargeValue;
			}
		}
		ServiceContentSave serviceContentSave = new ServiceContentSave();
		serviceContentSave.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
		String adjustLevel = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/adjustLevel");
		if ("1".equals(adjustLevel) || "2".equals(adjustLevel)) {
			serviceContentSave.setElementId("caf27c97f109b6783879743efe8d42f0");
			serviceContentSave.setAliasName("caf27c97f109b6783879743efe8d42f0");
			serviceContentSave.setElementName("退费级别");
			serviceContentSave.setAnswerId(adjustLevel.equals("1") ? "select_0217" : "select_0218");
			serviceContentSave.setAnswerName(adjustLevel.equals("1") ? "账户级" : "用户级");
		} else {
			return buildReturnInfo("1", "1", "调账级别adjustLevel值不规范，不允许录单", "", "");
		}
		list.add(serviceContentSave);
		com.alibaba.fastjson.JSONObject jsonObject = this.buildRechargeData(doc, jsonArray, prodInstId, acctId, refundDetail);
		refundDetail = StringUtils.defaultIfBlank(refundDetail, this.buildRefundDetail(jsonArray));
		this.buildServiceContentSave(list, doc, jsonObject, refundDetail, servContent);
		return null;
	}

	private boolean buildRechargeArray(Document doc, JSONArray jsonArray, String refundDetail) {
		if(StringUtils.isNotBlank(refundDetail)) {
			return true;
		}		
		List<Node> recharges = getNodeList(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/recharge");
		if (recharges != null && !recharges.isEmpty()) {
			for (Node n : recharges) {
				String acctItemID = getElementValue(n, "acctItemID");//账单ID
				String itemTypeId = getElementValue(n, "itemTypeId");//账目类型ID
				String acctItemType = getElementValue(n, "acctItemType");//账目类型名称
				String offerId = getElementValue(n, "offerId");//销售品ID
				String offerName = getElementValue(n, "offerName");//销售品名称
				String state = getElementValue(n, "state");//账单状态
				String amount = getElementValue(n, "amount");//账单金额（元）
				String itemSourceId = getElementValue(n, "itemSourceId");//账单来源ID
				String sourceName = getElementValue(n, "sourceName");//客户ID
				String cashFlag = getElementValue(n, "cashFlag");//是否直接新增数据
				String cashAmount = getElementValue(n, "cashAmount");//充值金额（元）
				String billingCycleId = getElementValue(n, "billingCycleId");//账期
				String pkgOfferId = getElementValue(n, "pkgOfferId");
				String pkgInstId = getElementValue(n, "pkgInstId");
				String pkgItemId = getElementValue(n, "pkgItemId");
				String pkgItemRangeId = getElementValue(n, "pkgItemRangeId");
				String tariffType = getElementValue(n, "tariffType");
				com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
				jsonObject.put("acctItemID", acctItemID);
				jsonObject.put("itemTypeId", itemTypeId);
				jsonObject.put("acctItemType", acctItemType);
				jsonObject.put("offerId", offerId);
				jsonObject.put("offerName", offerName);
				jsonObject.put("state", state);
				jsonObject.put("amount", amount);
				jsonObject.put("itemSourceId", itemSourceId);
				jsonObject.put("sourceName", sourceName);
				jsonObject.put("cashFlag", cashFlag);
				jsonObject.put("cashAmount", cashAmount);
				jsonObject.put("billingCycleId", billingCycleId);
				jsonObject.put("pkgOfferId", pkgOfferId);
				jsonObject.put("pkgInstId", pkgInstId);
				jsonObject.put("pkgItemId", pkgItemId);
				jsonObject.put("pkgItemRangeId", pkgItemRangeId);
				jsonObject.put("tariffType", tariffType);
				jsonArray.add(jsonObject);
			}
			return true;
		}
		return false;
	}

	private String buildRefundDetail(JSONArray jsonArray) {
		StringBuilder refundDetail = new StringBuilder();
		for (int i = 0; i < jsonArray.size(); i++) {
			com.alibaba.fastjson.JSONObject json = jsonArray.getJSONObject(i);
			String billingCycleId = this.formatDate(json.getString("billingCycleId"));
			String amount = json.getString("amount");
			String acctItemType = json.getString("acctItemType");
			refundDetail.append("退费账期：")
					.append(billingCycleId)
					.append(" 退费金额：")
					.append(amount)
					.append("元 账目类型：")
					.append(acctItemType)
					.append("\n");
		}
		return refundDetail.toString();
	}

	private com.alibaba.fastjson.JSONObject buildRechargeData(Document doc, JSONArray jsonArray, String prodInstId, String acctId, String refundDetail){
		if(StringUtils.isNotBlank(refundDetail)) {
			return new com.alibaba.fastjson.JSONObject();
		}
		String strRegion = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");// 地域
		String areaId = this.buildAreaId(strRegion);
		String prodNum = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");
		String acceptstaffid = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTSTAFFID");// 受理员工
		String relaman = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAMAN");// 联系人
		String adjustLevel = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/adjustLevel");
		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
		BigDecimal money = new BigDecimal("0.0");
		BigDecimal cashAmount = new BigDecimal("0.0");
		DecimalFormat df = new DecimalFormat("#.##");// 使用DecimalFormat来控制保留小数位
		for (int i = 0; i < jsonArray.size(); i++) {
			String amount = jsonArray.getJSONObject(i).getString("amount");
			String refundAmount = jsonArray.getJSONObject(i).getString("cashAmount");
			money = money.add(new BigDecimal(amount));
			cashAmount = cashAmount.add(new BigDecimal(refundAmount));
			jsonArray.getJSONObject(i).put("adjustLevel",adjustLevel);
			jsonArray.getJSONObject(i).put("itemGroupId","");
			jsonArray.getJSONObject(i).put("telephone",prodNum);
			jsonArray.getJSONObject(i).put("prodInstId",prodInstId);
			jsonArray.getJSONObject(i).put("acctId",acctId);
		}
		json.put("note","");
		json.put("refundsAccNum",prodNum);
		json.put("staffId",acceptstaffid);
		json.put("money",money);
		json.put("orderId","");
		json.put("complain_order_id","");
		json.put("accNumType","");
		json.put("customerName",relaman);
		json.put("source","0");//工单入口
		json.put("telephone",prodNum);
		json.put("cashAmount",df.format(cashAmount));//充值金额
		json.put("oAmount",df.format(money));//原金额
		json.put("adjustLevel","1".equals(adjustLevel)?"账户级":"用户级");
		json.put("itemGroupId","");
		json.put("areaId",areaId);
		json.put("rechargeList",jsonArray);
		return json;
	}

	private void buildServiceContentSave(List<ServiceContentSave> list, Document doc, com.alibaba.fastjson.JSONObject jsonObject, String refundDetail, ServiceContent servContent){
		//小额退赔场景
		String answerId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/answerId");
		String answerName = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/answerName");
		ServiceContentSave servContentSave1 = new ServiceContentSave();
		servContentSave1.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
		servContentSave1.setElementId("858a9cc67e1c1f68aeb2d336a41b8866");
		servContentSave1.setAliasName("858a9cc67e1c1f68aeb2d336a41b8866");
		servContentSave1.setElementName("小额退赔场景");
		servContentSave1.setAnswerId(StringUtils.isNotBlank(answerId)?answerId:"select_0216");
		servContentSave1.setAnswerName(StringUtils.isNotBlank(answerName)?answerName:"小额费用争议（公众）");
		list.add(servContentSave1);

		//退费号码
		ServiceContentSave servContentSave2 = new ServiceContentSave();
		String prodnum = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");// 产品号码
		servContentSave2.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
		servContentSave2.setElementId("b29428192062639284a0d86e197ed30f");
		servContentSave2.setAliasName("b29428192062639284a0d86e197ed30f");
		servContentSave2.setElementName("退费号码");
		servContentSave2.setAnswerId("0");
		servContentSave2.setAnswerName(prodnum);
		list.add(servContentSave2);

		//承诺退费周期
		ServiceContentSave servContentSave3 = new ServiceContentSave();
		servContentSave3.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
		servContentSave3.setElementId("79e7a4b3ae2009b277285e0c8d13dd80");
		servContentSave3.setAliasName("79e7a4b3ae2009b277285e0c8d13dd80");
		servContentSave3.setElementName("承诺退费周期");
		servContentSave3.setAnswerId("radio_0052");
		servContentSave3.setAnswerName("3-7个工作日");
		list.add(servContentSave3);

		if(!jsonObject.isEmpty()) {
			//账单合计金额
			ServiceContentSave servContentSave4 = new ServiceContentSave();
			servContentSave4.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
			servContentSave4.setElementId("2ca70165a432752d43dd0e2600a290e8");
			servContentSave4.setAliasName("2ca70165a432752d43dd0e2600a290e8");
			servContentSave4.setElementName("账单合计金额");
			servContentSave4.setAnswerId("0");
			servContentSave4.setAnswerName(jsonObject.getString("cashAmount"));
			list.add(servContentSave4);
	
			//退费数据
			ServiceContentSave servContentSave5 = new ServiceContentSave();
			servContentSave5.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
			servContentSave5.setElementId("c2f9995733b843c8393cc78629cd9220");
			servContentSave5.setAliasName("c2f9995733b843c8393cc78629cd9220");
			servContentSave5.setElementName("退费数据");
			servContentSave5.setAnswerId("0");
			servContentSave5.setAnswerName("【"+jsonObject+"】");
			list.add(servContentSave5);
			
			//承诺退费金额（使用账单合计金额）
			ServiceContentSave serviceContentSave8 = new ServiceContentSave();
			serviceContentSave8.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
			serviceContentSave8.setElementId("b6b2882c9e1811ee89ee005056b35a1f");
			serviceContentSave8.setAliasName("b6b2882c9e1811ee89ee005056b35a1f");
			serviceContentSave8.setElementName("承诺退费金额");
			serviceContentSave8.setAnswerId("0");
			serviceContentSave8.setAnswerName(jsonObject.getString("cashAmount"));
			list.add(serviceContentSave8);
			
			// 退费业务
			ServiceContentSave serviceContentSave9 = new ServiceContentSave();
			String refundBusiness = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/refundBusiness");
			if(StringUtils.isNotBlank(refundBusiness)){
				serviceContentSave9.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
				serviceContentSave9.setElementId("017df8368d3bcb35f8b8313f8e3d72e9");
				serviceContentSave9.setAliasName("017df8368d3bcb35f8b8313f8e3d72e9");
				serviceContentSave9.setElementName("退费业务");
				serviceContentSave9.setAnswerId("0");
				serviceContentSave9.setAnswerName(refundBusiness);
				list.add(serviceContentSave9);
			}
		} else {
			//账单合计金额
			ServiceContentSave servContentSave4 = new ServiceContentSave();
			servContentSave4.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
			servContentSave4.setElementId("2ca70165a432752d43dd0e2600a290e8");
			servContentSave4.setAliasName("2ca70165a432752d43dd0e2600a290e8");
			servContentSave4.setElementName("账单合计金额");
			servContentSave4.setAnswerId("0");
			servContentSave4.setAnswerName("-999.99");
			list.add(servContentSave4);
			
			//承诺退费金额
			String proRefundAmount = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/proRefundAmount");
			jsonObject.put("proRefundAmount", proRefundAmount);
			ServiceContentSave serviceContentSave8 = new ServiceContentSave();
			serviceContentSave8.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
			serviceContentSave8.setElementId("b6b2882c9e1811ee89ee005056b35a1f");
			serviceContentSave8.setAliasName("b6b2882c9e1811ee89ee005056b35a1f");
			serviceContentSave8.setElementName("承诺退费金额");
			serviceContentSave8.setAnswerId("0");
			serviceContentSave8.setAnswerName(proRefundAmount);
			list.add(serviceContentSave8);
		}

		//退费详情
		ServiceContentSave servContentSave6 = new ServiceContentSave();
		servContentSave6.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
		servContentSave6.setElementId("c80bc7608d47571d5c561c6948c4b1be");
		servContentSave6.setAliasName("c80bc7608d47571d5c561c6948c4b1be");
		servContentSave6.setElementName("退费详情");
		servContentSave6.setAnswerId("0");
		servContentSave6.setAnswerName(refundDetail);
		list.add(servContentSave6);

		//账期数据
		String billingCycleId = this.getBillingCycleId(doc, jsonObject);
		ServiceContentSave servContentSave7 = new ServiceContentSave();
		servContentSave7.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
		servContentSave7.setElementId("ae6488f59e1811ee89ee005056b35a1f");
		servContentSave7.setAliasName("ae6488f59e1811ee89ee005056b35a1f");
		servContentSave7.setElementName("退费账期");
		servContentSave7.setAnswerId("0");
		servContentSave7.setAnswerName(billingCycleId);
		list.add(servContentSave7);

		//是否多号码退费
		ServiceContentSave servContentSave8 = new ServiceContentSave();
		servContentSave8.setComplaintsId("69dd56faf686085b7349dbb0f7129a11");
		servContentSave8.setElementId("d86fc07758818171bb2b48ff64b4dec2");
		servContentSave8.setAliasName("d86fc07758818171bb2b48ff64b4dec2");
		servContentSave8.setElementName("是否多号码退费");
		servContentSave8.setAnswerId("radio_002");
		servContentSave8.setAnswerName("否");
		list.add(servContentSave8);

		//赋值受理内容
		String acceptcontent = this.buildAcceptcontent(doc, jsonObject);
		servContent.setAcceptContent(StringUtils.substring(acceptcontent, 0, 4000));
	}

	private String checkRecargeValue(JSONArray value){
		for (int i = 0; i < value.size(); i++) {
			com.alibaba.fastjson.JSONObject recharge = value.getJSONObject(i);
			String checkMsg = this.checkRechargeDetail(recharge);
			if(StringUtils.isNotBlank(checkMsg)) {
				return checkMsg;
			}
		}
		return null;
	}
	
	private String checkRechargeDetail(com.alibaba.fastjson.JSONObject recharge) {
		String acctItemID = recharge.getString("acctItemID");//账单ID
		String itemTypeId = recharge.getString("itemTypeId");//账目类型ID
		String acctItemType = recharge.getString("acctItemType");//账目类型名称
		String offerId = recharge.getString("offerId");//销售品ID
		String offerName = recharge.getString("offerName");//销售品名称
		String state = recharge.getString("state");//账单状态
		String amount = recharge.getString("amount");//账单金额（元）
		String itemSourceId = recharge.getString("itemSourceId");//账单来源ID
		String cashFlag = recharge.getString("cashFlag");//是否直接新增数据
		String cashAmount = recharge.getString("cashAmount");//充值金额（元）
		String billingCycleId = recharge.getString("billingCycleId");
		if(StringUtils.isBlank(acctItemID)){
			return buildReturnInfo("1", "1", "acctItemID值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(itemTypeId)) {
			return buildReturnInfo("1", "1", "itemTypeId值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(acctItemType)) {
			return buildReturnInfo("1", "1", "acctItemType值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(offerId)) {
			return buildReturnInfo("1", "1", "offerId值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(offerName)) {
			return buildReturnInfo("1", "1", "offerName值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(state)) {
			return buildReturnInfo("1", "1", "state值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(amount)) {
			return buildReturnInfo("1", "1", "amount值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(itemSourceId)) {
			return buildReturnInfo("1", "1", "itemSourceId值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(cashFlag)) {
			return buildReturnInfo("1", "1", "cashFlag值为空，受理现象不允许录单", "", "");
		}
		if (StringUtils.isBlank(cashAmount)) {
			return buildReturnInfo("1", "1", "cashAmount值为空，受理现象不允许录单", "", "");
		}
		if(StringUtils.isBlank(billingCycleId)){
			return buildReturnInfo("1", "1", "billingCycleId值为空，受理现象不允许录单", "", "");
		}
		if(!DateUtil.isValidDate(billingCycleId, "yyyyMM")){
			return buildReturnInfo("1", "1", "billingCycleId值格式不正确，受理现象不允许录单", "", "");
		}
		try {
			double amountValue = Double.parseDouble(amount);
			double cashAmountValue = Double.parseDouble(cashAmount);
			if (cashAmountValue > amountValue) {
				return buildReturnInfo("1", "1", "充值金额不得大于账单金额", "", "");
			}
		} catch (NumberFormatException e) {
			return buildReturnInfo("1", "1", "金额格式不正确", "", "");
		}
		return null;
	}

	private String buildAcceptcontent(Document doc,com.alibaba.fastjson.JSONObject jsonObject){
		StringBuilder acceptcontent = new StringBuilder();
		String sourceSystem = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SOURCE_SYSTEM");
		String prodNum = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");// 产品号码
		String proRefundAmount = StringUtils.defaultIfBlank(jsonObject.getString("cashAmount"), jsonObject.getString("proRefundAmount"));//使用账单合计金额
		String adjustLevel = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/adjustLevel");
		String billingCycleId = this.getBillingCycleId(doc, jsonObject);
		String isAutoFinish = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/IS_AUTO_FINISH");// 是否自动办结
		String rechargeNote = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/rechargeNote");//受理内容备注
		String answerName = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/answerName");
		if("IVR".equals(sourceSystem)){
			acceptcontent.append("IVR自动受理：");
		} else if("10000".equals(sourceSystem) && "1".equals(isAutoFinish)){
			acceptcontent.append("10000号在线退费办结：");
		}
		if(StringUtils.isNotBlank(answerName)){
			acceptcontent.append("小额退赔场景："+answerName+"\n");
		}else {
			acceptcontent.append("小额退赔场景：小额费用争议（公众）\n");
		}
		acceptcontent.append("退费账期及金额：" + billingCycleId + "，共（"+proRefundAmount+"）元\n");
		acceptcontent.append("退费号码："+ prodNum+"\n");
		acceptcontent.append("退费级别："+ ("1".equals(adjustLevel)?"账户级":"用户级")+"\n");
		acceptcontent.append("承诺退费周期：3-7个工作日");
		if(StringUtils.isNotBlank(rechargeNote)){
			acceptcontent.append("\n").append(rechargeNote);
		}
		return acceptcontent.toString();
	}

	private String getBillingCycleId(Document doc, com.alibaba.fastjson.JSONObject jsonObject){
		String cycleId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/billingCycleId");
		if(StringUtils.isNotBlank(cycleId)) {
			return cycleId;
		}
		JSONArray rechargeList = jsonObject.getJSONArray("rechargeList");
		Set<String> billingCycleIds = new HashSet<>();
		for (int i = 0; i < rechargeList.size(); i++) {
			String billingCycleId = rechargeList.getJSONObject(i).getString("billingCycleId");
			if (billingCycleId != null && billingCycleId.matches("\\d{6}")) {
				billingCycleIds.add(billingCycleId);
			}
		}
		return billingCycleIds.stream().sorted().map(this::formatDate)
				.collect(Collectors.joining(","));
	}

	private String setDisputeContentSave(List<ServiceContentSave> list, Document doc,String ns,ServiceContent servContent){
		String disputeMonth = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/disputeMonth");
		String accountType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/accountType");
		String disputeBill = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/disputeBill");
		String disputeDetail = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/disputeDetail");
		String prodnum = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");
		String regionId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");
		String sourceSystem = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SOURCE_SYSTEM");



		String msg = this.checkParam(doc);
		if(StringUtils.isNotBlank(msg)){
			return msg;
		}

		com.alibaba.fastjson.JSONObject jsonObject = this.qryMainInfoAndAccUse(doc);
		String mainInfoId = jsonObject.getString("mainInfoId");//主套餐id
		String mainInfoName = jsonObject.getString("mainInfoName");//主套餐名称
		String voiceUsed = jsonObject.getString("voiceUsed");//语音使用量
		String voiceResidue = jsonObject.getString("voiceResidue");//语音剩余量
		String flowUsed = jsonObject.getString("flowUsed");//流量使用量
		String flowResidue = jsonObject.getString("flowResidue");//流量剩余量

		com.alibaba.fastjson.JSONObject offerAndChannelJson = this.qryOfferAndChannel(doc, list, mainInfoId);
		String offerId = offerAndChannelJson.getString("offerId");
		String offerName = offerAndChannelJson.getString("offerName");
		String effDate = offerAndChannelJson.getString("effDate");
		String expDate = offerAndChannelJson.getString("expDate");

		ServiceContentSave serviceContentSave1 = new ServiceContentSave();
		serviceContentSave1.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave1.setElementId("c06f9cb43c22f4193520d6bc7cd7d494");
		serviceContentSave1.setAliasName("disputeMonth");
		serviceContentSave1.setElementName("争议月份");
		serviceContentSave1.setAnswerId("0");
		serviceContentSave1.setAnswerName(disputeMonth);
		list.add(serviceContentSave1);

		ServiceContentSave serviceContentSave2 = new ServiceContentSave();
		serviceContentSave2.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave2.setElementId("c0fa226a0cd7790761de35cd3e5c4ebc");
		serviceContentSave2.setAliasName("accountType");
		serviceContentSave2.setElementName("账目类型");
		serviceContentSave2.setAnswerId("0");
		serviceContentSave2.setAnswerName(accountType);
		list.add(serviceContentSave2);

		ServiceContentSave serviceContentSave3 = new ServiceContentSave();
		serviceContentSave3.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave3.setElementId("860a581115c3b197220b496f567e31a1");
		serviceContentSave3.setAliasName("disputeBill");
		serviceContentSave3.setElementName("争议金额");
		serviceContentSave3.setAnswerId("0");
		serviceContentSave3.setAnswerName(disputeBill);
		list.add(serviceContentSave3);


		ServiceContentSave serviceContentSave4 = new ServiceContentSave();
		serviceContentSave4.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave4.setElementId("63e4207ad4670cee80df74646c3a44d1");
		serviceContentSave4.setAliasName("mainOfferNm");
		serviceContentSave4.setElementName("主套餐名称");
		serviceContentSave4.setAnswerId("0");
		serviceContentSave4.setAnswerName(mainInfoName);
		list.add(serviceContentSave4);

		ServiceContentSave serviceContentSave5 = new ServiceContentSave();
		serviceContentSave5.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave5.setElementId("63b44d04c58dc815c4a491cd14f1750c");
		serviceContentSave5.setAliasName("mainInfoId");
		serviceContentSave5.setElementName("主套餐ID");
		serviceContentSave5.setAnswerId("0");
		serviceContentSave5.setAnswerName(mainInfoId);
		list.add(serviceContentSave5);

		ServiceContentSave serviceContentSave6 = new ServiceContentSave();
		serviceContentSave6.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave6.setElementId("10fa998baff96b7238d4e85940ff521d");
		serviceContentSave6.setAliasName("offerNm");
		serviceContentSave6.setElementName("销售品名称");
		serviceContentSave6.setAnswerId("0");
		serviceContentSave6.setAnswerName(offerName);
		list.add(serviceContentSave6);

		ServiceContentSave serviceContentSave7 = new ServiceContentSave();
		serviceContentSave7.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave7.setElementId("92f48b5a6b2d408ecd6ca36664cdbd12");
		serviceContentSave7.setAliasName("offerNbr");
		serviceContentSave7.setElementName("销售品ID");
		serviceContentSave7.setAnswerId("0");
		serviceContentSave7.setAnswerName(offerId);
		list.add(serviceContentSave7);

		ServiceContentSave serviceContentSave8 = new ServiceContentSave();
		serviceContentSave8.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave8.setElementId("36379033d43a476226a1a7e833bf6b94");
		serviceContentSave8.setAliasName("effTime");
		serviceContentSave8.setElementName("生效时间");
		serviceContentSave8.setAnswerId("0");
		serviceContentSave8.setAnswerName(effDate);
		list.add(serviceContentSave8);

		ServiceContentSave serviceContentSave9 = new ServiceContentSave();
		serviceContentSave9.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave9.setElementId("52f4ea0be5ed5183a363fd57fa020b92");
		serviceContentSave9.setAliasName("expTime");
		serviceContentSave9.setElementName("失效时间");
		serviceContentSave9.setAnswerId("0");
		serviceContentSave9.setAnswerName(expDate);
		list.add(serviceContentSave9);

		String offerLevel = "";
		String offerLevelName = "";
		if(!"-1".equals(offerId)){
			com.alibaba.fastjson.JSONObject offerlevelJson = this.getofferlevel(offerId, regionId);
			if(!offerlevelJson.isEmpty()){
				offerLevel = offerlevelJson.getString("offerLevel");
				offerLevelName = offerlevelJson.getString("offerLevel");
			}
		}
		ServiceContentSave serviceContentSave10 = new ServiceContentSave();
		serviceContentSave10.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave10.setElementId("e3dceef36363fee198d31562b06454d6");
		serviceContentSave10.setAliasName("e3dceef36363fee198d31562b06454d6");
		serviceContentSave10.setElementName("销售品等级");
		serviceContentSave10.setAnswerId(StringUtils.isNotBlank(offerLevel)?offerLevel:"select_0303");
		serviceContentSave10.setAnswerName(StringUtils.isNotBlank(offerLevelName)?offerLevelName:"省内集约");
		list.add(serviceContentSave10);

		ServiceContentSave serviceContentSave11 = new ServiceContentSave();
		serviceContentSave11.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave11.setElementId("d85e55865d70d395d6fc529bfabf1cd5");
		serviceContentSave11.setAliasName("useFlux");
		serviceContentSave11.setElementName("套内流量使用量");
		serviceContentSave11.setAnswerId("0");
		serviceContentSave11.setAnswerName(flowUsed);
		list.add(serviceContentSave11);

		ServiceContentSave serviceContentSave12 = new ServiceContentSave();
		serviceContentSave12.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave12.setElementId("1ca1a235537f8b16b91ba5c99997336c");
		serviceContentSave12.setAliasName("rmngFlux");
		serviceContentSave12.setElementName("套内流量剩余量");
		serviceContentSave12.setAnswerId("0");
		serviceContentSave12.setAnswerName(flowResidue);
		list.add(serviceContentSave12);

		ServiceContentSave serviceContentSave13 = new ServiceContentSave();
		serviceContentSave13.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave13.setElementId("967ebefa358ec7c5d1cdb07342582fb5");
		serviceContentSave13.setAliasName("967ebefa358ec7c5d1cdb07342582fb5");
		serviceContentSave13.setElementName("套内语音使用量");
		serviceContentSave13.setAnswerId("0");
		serviceContentSave13.setAnswerName(voiceUsed);
		list.add(serviceContentSave13);

		ServiceContentSave serviceContentSave14 = new ServiceContentSave();
		serviceContentSave14.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave14.setElementId("84f700971f89d3c4e2636c0a1756e1ca");
		serviceContentSave14.setAliasName("rmngVoice");
		serviceContentSave14.setElementName("套内语音剩余量");
		serviceContentSave14.setAnswerId("0");
		serviceContentSave14.setAnswerName(voiceResidue);
		list.add(serviceContentSave14);

		ServiceContentSave serviceContentSave15 = new ServiceContentSave();
		serviceContentSave15.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave15.setElementId("989cd7728e7a03de725f791a9848bda3");
		serviceContentSave15.setAliasName("989cd7728e7a03de725f791a9848bda3");
		serviceContentSave15.setElementName("客户要求");
		serviceContentSave15.setAnswerId("checkbox_046");
		serviceContentSave15.setAnswerName("退赔费用");
		list.add(serviceContentSave15);

		ServiceContentSave serviceContentSave16 = new ServiceContentSave();
		serviceContentSave16.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave16.setElementId("e8fb274cfefb9ae33327d50382b3d7ba");
		serviceContentSave16.setAliasName("e8fb274cfefb9ae33327d50382b3d7ba");
		serviceContentSave16.setElementName("特殊要求");
		serviceContentSave16.setAnswerId("0");
		serviceContentSave16.setAnswerName("");
		list.add(serviceContentSave16);

		StringBuilder acceptContent = new StringBuilder();
		if("IVR".equals(sourceSystem)){
			acceptContent.append("IVR自动受理：");
		}
		acceptContent.append("客户反映").append(prodnum).append("号码，办理主套餐名称");
		acceptContent.append(mainInfoName).append("，争议销售品名称").append(offerName).append("、生效时间").append(effDate);
		acceptContent.append("、失效时间").append(expDate).append("，").append(disputeDetail).append("，");
		acceptContent.append("套内流量使用量").append(flowUsed).append("M，套内流量剩余量").append(flowResidue).append("M，套内语音使用量");
		acceptContent.append(voiceUsed).append("分钟，套内语音剩余量").append(voiceResidue).append("分钟，客户对").append(pubFun.getSplitNameByIdx(ns, 1));
		acceptContent.append("有争议，要求").append("退赔费用，请协助跟进。");
		servContent.setAcceptContent(acceptContent.toString());
		return null;
	}

	private String checkParam(Document doc){
		String disputeMonth = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/disputeMonth");
		String accountType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/accountType");
		String disputeBill = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/disputeBill");
		String disputeDetail = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/disputeDetail");
		if(StringUtils.isBlank(disputeMonth)){
			return buildReturnInfo("1", "1", "争议月份disputeMonth节点不能为空", "", "");
		}
		if(!DateUtil.isValidDate(disputeMonth, "yyyyMM")){
			return buildReturnInfo("1", "1", "争议月份disputeMonth不符合规范", "", "");
		}
		if(StringUtils.isBlank(accountType)){
			return buildReturnInfo("1", "1", "账目类型accountType节点不能为空", "", "");
		}
		if(StringUtils.isBlank(accountType) && accountType.length()>200){
			return buildReturnInfo("1", "1", "账目类型accountType节点长度不能超过200", "", "");
		}
		if(StringUtils.isBlank(disputeBill)){
			return buildReturnInfo("1", "1", "争议金额disputeBill节点不能为空", "", "");
		}
		if(!this.isDouble(disputeBill)){
			return buildReturnInfo("1", "1", "争议金额disputeBill数据不符合规范", "", "");
		}
		if(StringUtils.isBlank(disputeDetail)){
			return buildReturnInfo("1", "1", "争议详情disputeDetail节点不能为空", "", "");
		}
		if(disputeDetail.length()>500){
			return buildReturnInfo("1", "1", "争议详情disputeDetail内容长度超500", "", "");
		}
		return null;
	}

	private com.alibaba.fastjson.JSONObject getofferlevel(String offerId, String regionId){
		logger.info("getofferlevel param:{}",offerId,regionId);
		com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
		String offerLevel = "";
		String offerLevelName = "";
		try {
			String s = customerServiceFeign.qryOfferDetail(offerId, regionId);
			if(StringUtils.isNotBlank(s)){
				com.alibaba.fastjson.JSONObject detail = JSON.parseObject(s);
				String manageGrade = detail.getString("manageGrade");
				offerLevelName = detail.getString("manageGradeName");
				switch (manageGrade){
					case "10":
					case "11":
						offerLevel = "select_0302";
						break; //集团集约
					case "12":
					case "14":
					case "15":
						offerLevel = "select_0303";
						break; //省内集约
					case "13":
						offerLevel = "select_0304";
						break; //本地套餐
					default:
						break;
				}
				json.put("offerLevel",offerLevel);
				json.put("offerLevelName",offerLevelName);
			}
		}catch (Exception e){
			logger.error("getofferlevel error : {}",e.getMessage(),e);
		}
		return json;
	}

	private com.alibaba.fastjson.JSONObject qryMainInfoAndAccUse(Document doc){
		String mainInfoId = "";//主套餐id
		String mainInfoName = "";//主套餐名称
		StringBuilder voiceUsed = new StringBuilder();//语音使用量
		StringBuilder voiceResidue = new StringBuilder();//语音剩余量
		StringBuilder flowUsed = new StringBuilder();//流量使用量
		StringBuilder flowResidue = new StringBuilder();//流量剩余量
		com.alibaba.fastjson.JSONObject param = new com.alibaba.fastjson.JSONObject();
		String prodnum = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");
		String regionId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");
		String prodType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODTYPE");// 产品类型
		String disputeMonth = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/disputeMonth");
		com.alibaba.fastjson.JSONObject mainInfoParam = new com.alibaba.fastjson.JSONObject();
		mainInfoParam.put("accNum",prodnum);
		mainInfoParam.put("regionId",regionId);
		try{
			String s = customerServiceFeign.qryMainOfferInfo(mainInfoParam.toString());
			if(StringUtils.isNotBlank(s)){
				com.alibaba.fastjson.JSONObject mainOfferInfojson = JSON.parseObject(s);
				mainInfoId = StringUtils.isNotBlank(mainOfferInfojson.getString("id"))?mainOfferInfojson.getString("id"):"无";//主套餐id
				mainInfoName = StringUtils.isNotBlank(mainOfferInfojson.getString("name"))?mainOfferInfojson.getString("name"):"无";//主套餐名称
			}

			com.alibaba.fastjson.JSONObject accuUseParam = new com.alibaba.fastjson.JSONObject();
			accuUseParam.put("accNum",prodnum);
			accuUseParam.put("regionId",regionId);
			accuUseParam.put("queryType",prodType);
			accuUseParam.put("qryMonth",disputeMonth);
			accuUseParam.put("rspType","1");
			this.setAccuUseValue(accuUseParam,voiceUsed,voiceResidue,flowUsed,flowResidue);

			if(StringUtils.isBlank(voiceUsed) && StringUtils.isBlank(voiceResidue) && StringUtils.isBlank(flowUsed) && StringUtils.isBlank(flowResidue)){
				voiceUsed = new StringBuilder("-1");
				voiceResidue = new StringBuilder("-1");
				flowUsed = new StringBuilder("-1");
				flowResidue = new StringBuilder("-1");
			}
			param.put("mainInfoId",mainInfoId);
			param.put("mainInfoName",mainInfoName);
			param.put("voiceUsed",voiceUsed);
			param.put("voiceResidue",voiceResidue);
			param.put("flowUsed",flowUsed);
			param.put("flowResidue",flowResidue);
		}catch (Exception e){
			logger.error("qryMainInfoAndAccUse 查询主套餐信息异常:{}", e.getMessage(), e);
		}
		return param;
	}

	private void setAccuUseValue(com.alibaba.fastjson.JSONObject accuUseParam,StringBuilder voiceUsed,StringBuilder voiceResidue,StringBuilder flowUsed,StringBuilder flowResidue){
		try{
			String result = customerServiceFeign.qryAccuUse(accuUseParam.toString());
			if(StringUtils.isNotBlank(result)){
				JSONArray accuUseJson = JSON.parseArray(result);
				for (int i = 0; i < accuUseJson.size(); i++) {
					com.alibaba.fastjson.JSONObject json = accuUseJson.getJSONObject(i);
					if("1".equals(json.getString("unitTypeId"))){////语音
						voiceUsed.append(json.getString("usageVal") + "分钟");
						voiceResidue.append(json.getString("accuVal") + "分钟");
					}else if("3".equals(json.getString("unitTypeId"))){
						flowUsed.append(json.getString("usageVal") + "MB");
						flowResidue.append(json.getString("accuVal") + "MB");
					}
				}
			}
		}catch (Exception e){
			logger.error("qryAccuUse 查询剩余量信息异常:{}", e.getMessage(), e);
		}
	}

	private com.alibaba.fastjson.JSONObject qryOfferAndChannel(Document doc,List<ServiceContentSave> list,String mainInfoId){
		com.alibaba.fastjson.JSONObject param = new com.alibaba.fastjson.JSONObject();
		try{
			com.alibaba.fastjson.JSONObject offerInfo = this.getOfferInfo(doc, mainInfoId);
			String offerId = offerInfo.getString("offerId");
			String offerName = offerInfo.getString("offerName");
			String effDate = offerInfo.getString("effDate");
			String expDate = offerInfo.getString("expDate");
			String createOrgId = offerInfo.getString("createOrgId");

			if(StringUtils.isBlank(offerId) && StringUtils.isBlank(offerName) && StringUtils.isBlank(effDate) && StringUtils.isBlank(expDate)){
				offerId = "-1";
				offerName = "无合适选项";
				effDate = "3000-01-01 00:00:00";
				expDate = "3000-01-01 00:00:00";
			}

			if(StringUtils.isNotBlank(createOrgId)){
				String regionId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");
				String strChannel = customerServiceFeign.qryChannel(createOrgId, regionId);
				if(StringUtils.isNotBlank(strChannel)){
					com.alibaba.fastjson.JSONObject channelJson = JSON.parseObject(strChannel);
					if(!channelJson.isEmpty()){
						String dvlpChnl = channelJson.getString("dvlpChnl");//渠道id
						String dvlpChnlNm = channelJson.getString("dvlpChnlNm");//渠道名称
						String devtChsOneName = channelJson.getString("devtChsOneName");//渠道一级名称
						String devtChsTwoName = channelJson.getString("devtChsTwoName");//渠道二级名称
						String devtChsThreeName = channelJson.getString("devtChsThreeName");//渠道三级名称

						if(StringUtils.isNotBlank(devtChsOneName) && StringUtils.isNotBlank(devtChsTwoName) && StringUtils.isNotBlank(devtChsThreeName)) {
							String devtChsOne = channelJson.getString("devtChsOne");//渠道一级id
							String devtChsTwo = channelJson.getString("devtChsTwo");//渠道二级id
							String devtChsThree = channelJson.getString("devtChsThree");//渠道三级id

							//渠道赋值
							ServiceContentSave serviceContentSave1 = new ServiceContentSave();
							serviceContentSave1.setComplaintsId("85c9490b26e2e72a83530285baee155e");
							serviceContentSave1.setElementId("7d7c731e047cdf3545ef3c035bddeb1c");
							serviceContentSave1.setAliasName("offerChnl");
							serviceContentSave1.setElementName("渠道ID");
							serviceContentSave1.setAnswerId("0");
							serviceContentSave1.setAnswerName(dvlpChnl);
							list.add(serviceContentSave1);

							ServiceContentSave serviceContentSave2 = new ServiceContentSave();
							serviceContentSave2.setComplaintsId("85c9490b26e2e72a83530285baee155e");
							serviceContentSave2.setElementId("790179ab73969671b3da3b078dba75db");
							serviceContentSave2.setAliasName("offerChnlNm");
							serviceContentSave2.setElementName("渠道名称");
							serviceContentSave2.setAnswerId("0");
							serviceContentSave2.setAnswerName(dvlpChnlNm);
							list.add(serviceContentSave2);
							this.setSaveChannel(list,devtChsOneName,devtChsTwoName,devtChsThreeName,devtChsOne,devtChsTwo,devtChsThree);
						}
					}
				}
			}
			param.put("offerId",offerId);
			param.put("offerName",offerName);
			param.put("effDate",effDate);
			param.put("expDate",expDate);
		}catch (Exception e){
			logger.error("qryOfferAndChannel 查询销售品与渠道异常 :{}",e.getMessage(),e);
		}
		return param;
	}

	private com.alibaba.fastjson.JSONObject getOfferInfo(Document doc, String mainInfoId){
		com.alibaba.fastjson.JSONObject param = new com.alibaba.fastjson.JSONObject();
		if(StringUtils.isBlank(mainInfoId)){
			return param;
		}
		String prodnum = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");
		String regionId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");
		String offerId = "";
		String offerName = "";
		String effDate = "";
		String expDate = "";
		String createOrgId = "";
		try{
			//销售品
			String offerInfo = customerServiceFeign.getOfferInfo(prodnum, regionId, "", "1", "");
			if(StringUtils.isNotBlank(offerInfo)){
				com.alibaba.fastjson.JSONObject offerInfoJson = JSON.parseObject(offerInfo);
				JSONArray offerArray = JSON.parseArray(offerInfoJson.getString("resultObj"));
				for (int i = 0; i < offerArray.size(); i++) {
					com.alibaba.fastjson.JSONObject offerJson = offerArray.getJSONObject(i);
					String jsonOfferId = offerJson.getString("offerId");
					String jsonPkgId = offerJson.getString("pkgId");
					if(StringUtils.isNotBlank(jsonOfferId) && mainInfoId.equals(jsonOfferId)){
						offerId = jsonOfferId;
						offerName = offerJson.getString("offerName");
						effDate = offerJson.getString("effDate");
						expDate = offerJson.getString("expDate");
						createOrgId = offerJson.getString("createOrgId");
						break;
					}
					if(StringUtils.isNotBlank(jsonPkgId) && mainInfoId.equals(jsonPkgId)){
						offerId = jsonPkgId;
						offerName = offerJson.getString("pkgName");
						effDate = offerJson.getString("effDate");
						expDate = offerJson.getString("expDate");
						createOrgId = offerJson.getString("createOrgId");
						break;
					}
				}
			}
		}catch (Exception e){
			logger.error("getOfferInfo 查询销售品异常:{}",e.getMessage(),e);
		}
		param.put("offerId",offerId);
		param.put("offerName",offerName);
		param.put("effDate",effDate);
		param.put("expDate",expDate);
		param.put("createOrgId",createOrgId);
		return param;
	}

	private void setSaveChannel(List<ServiceContentSave> list,String devtChsOneName,String devtChsTwoName,
								String devtChsThreeName,String devtChsOne,String devtChsTwo,String devtChsThree){
		ServiceContentSave serviceContentSave1 = new ServiceContentSave();
		serviceContentSave1.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave1.setElementId("888c9982c585f56b7eadb576e9c08337");
		serviceContentSave1.setAliasName("offerChnlOne");
		serviceContentSave1.setElementName("渠道类型一级ID");
		serviceContentSave1.setAnswerId("0");
		serviceContentSave1.setAnswerName(devtChsOne);
		list.add(serviceContentSave1);

		ServiceContentSave serviceContentSave2 = new ServiceContentSave();
		serviceContentSave2.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave2.setElementId("0c5f7712fb714540a36a3e194366752b");
		serviceContentSave2.setAliasName("offerChnlOneNm");
		serviceContentSave2.setElementName("渠道类型一级名称");
		serviceContentSave2.setAnswerId("0");
		serviceContentSave2.setAnswerName(devtChsOneName);
		list.add(serviceContentSave2);

		ServiceContentSave serviceContentSave3 = new ServiceContentSave();
		serviceContentSave3.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave3.setElementId("162cf7b7cba91a9c1127d0fb2e29016e");
		serviceContentSave3.setAliasName("offerChnlTwo");
		serviceContentSave3.setElementName("渠道类型二级ID");
		serviceContentSave3.setAnswerId("0");
		serviceContentSave3.setAnswerName(devtChsTwo);
		list.add(serviceContentSave3);

		ServiceContentSave serviceContentSave4 = new ServiceContentSave();
		serviceContentSave4.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave4.setElementId("a6efee1120b50035f24135dd0c35077c");
		serviceContentSave4.setAliasName("offerChnlTwoNm");
		serviceContentSave4.setElementName("渠道类型二级名称");
		serviceContentSave4.setAnswerId("0");
		serviceContentSave4.setAnswerName(devtChsTwoName);
		list.add(serviceContentSave4);

		ServiceContentSave serviceContentSave5 = new ServiceContentSave();
		serviceContentSave5.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave5.setElementId("0707a0449ed6a10dba8a55c728cc7122");
		serviceContentSave5.setAliasName("offerChnlThree");
		serviceContentSave5.setElementName("渠道类型三级ID");
		serviceContentSave5.setAnswerId("0");
		serviceContentSave5.setAnswerName(devtChsThree);
		list.add(serviceContentSave5);

		ServiceContentSave serviceContentSave6 = new ServiceContentSave();
		serviceContentSave6.setComplaintsId("85c9490b26e2e72a83530285baee155e");
		serviceContentSave6.setElementId("23f92e1872045ccbc23c17f3a8250643");
		serviceContentSave6.setAliasName("offerChnlThreeNm");
		serviceContentSave6.setElementName("渠道类型三级名称");
		serviceContentSave6.setAnswerId("0");
		serviceContentSave6.setAnswerName(devtChsThreeName);
		list.add(serviceContentSave6);
	}

	private String formatDate(String date) {
		String year = date.substring(0, 4);
		String month = date.substring(4, 6);
		return year + "年" + month + "月";
	}

	private boolean isDouble(String value) {
		if (value == null) {
			return false;
		}
		if (value.length() > 20) {
			return false;
		}
		String regex = "^\\d+(\\.\\d+)?$";
		return value.matches(regex);
	}

	private String buildAreaId(String regionId){
		String areaId = "";
		switch (regionId){
			case "3":
				areaId = "250";
				break;
			case "15":
				areaId = "510";
				break;
			case "4":
				areaId = "511";
				break;
			case "20":
				areaId = "512";
				break;
			case "26":
				areaId = "513";
				break;
			case "33":
				areaId = "514";
				break;
			case "39":
				areaId = "515";
				break;
			case "48":
				areaId = "516";
				break;
			case "60":
				areaId = "517";
				break;
			case "63":
				areaId = "518";
				break;
			case "69":
				areaId = "519";
				break;
			case "79":
				areaId = "523";
				break;
			case "84":
				areaId = "527";
				break;
			default:
				break;
		}
		return areaId;
	}	
}