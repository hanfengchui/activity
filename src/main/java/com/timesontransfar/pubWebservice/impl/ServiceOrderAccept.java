package com.timesontransfar.pubWebservice.impl;

import java.io.StringReader;
import java.util.*;

import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.util.DateUtil;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.pojo.IntfLog;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderRelation;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.feign.custominterface.WorkSheetFeign;
import com.timesontransfar.pubWebservice.IpubwebServiceDao;
import com.timesontransfar.systemPub.entity.PubColumn;

import org.apache.commons.lang3.StringUtils;

import net.sf.json.JSONObject;

@Component
public class ServiceOrderAccept {
    protected Logger logger = LoggerFactory.getLogger(ServiceOrderAccept.class);

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


	private Document returnSaxRed(String str) throws SAXException, DocumentException {
		SAXReader saxReader = new SAXReader();
		saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		StringReader reader = new StringReader(str);
		return saxReader.read(reader);
	}

	/**
	 * 录单接口
	 * 
	 * @param orderInfo
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String serviceAccpet(String orderInfo) {
		logger.info("serviceAccpet 入参: {}", orderInfo);
		String returnInfo = "";
		IntfLog log = new IntfLog();
		log.setServOrderId("");
		log.setInMsg(orderInfo);
		log.setActionFlag("in");
		log.setActionResult("0");
		try {
			Document doc = returnSaxRed(orderInfo);
			returnInfo = checkServiceOrderInfo(doc);
			if (!StringUtils.isEmpty(returnInfo)) {
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
			String relaman = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAMAN");// 联系人
			String relainfo = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAINFO");// 联系信息
			String acceptstaffid = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTSTAFFID");// 受理员工
			String acceptorgid = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTORGID");// 受理部门
			String acceptorgname = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTORGNAME");// 受理部门
			String moreRelaInfo = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/MORERELAINFO");// 更多联系方式
			String servicedate = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICEDATE");// SERVICEDATEDESC
			String servicedatedesc = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICEDATEDESC");// SERVICEDATEDESC
			String serviceType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPE");// 服务类型
			String serviceTypeDesc = pubFun.getStaticName(Integer.parseInt(serviceType));
			String serviceTypeDetail = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPEDETAIL");// 服务类型细项
			String bestOrder = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/BESTORDER");// 最强工单
			String upTendencyFlag = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/UPTENDENCYFLAG");// 越级倾向
			String sixCatalog = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SIXCATALOG");// 受理目录
			
			//受理员工
			TsmStaff accpetStaff = pubFun.getLogonStaffByLoginName(acceptstaffid);
			if(accpetStaff == null) {
				return buildReturnInfo("1", "1", "受理员工工号有误", "", "");
			}
			
			//校验受理现象
			sixCatalog = changeSixCatalog(sixCatalog);
			String key = pubFun.getKeyById(sixCatalog);
			List<ServiceContentSave> list = new ArrayList<>();
			String msg = this.setServiceContentSave(key, list, doc);
			if(StringUtils.isNotBlank(msg)){
				return msg;
			}
			ServiceContentSave[] array = new ServiceContentSave[list.size()];
			ServiceContentSave[] saves = list.toArray(array);
			int lastXX = Integer.parseInt(sixCatalog);
			List<Object> catalogList = pubFun.queryAcceptDir(lastXX, 2);
			if (catalogList.isEmpty()){
				return buildReturnInfo("1", "1", "受理现象传值错误", "", "");
			}
			Map<String, String> catalogMap = (Map) catalogList.get(0);
			String ns = catalogMap.get("N");
			String ids = catalogMap.get("ID");
			
			OrderAskInfo orderAskInfo = new OrderAskInfo();
			String sourceId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SOURCEID");// 来源
			msg = setAskChannelId(orderAskInfo, sourceId);
			if(StringUtils.isNotBlank(msg)){
				return msg;
			}
			
			String isOwner = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ISOWNER");// 是否机主
			int isO = 0;
			if ("1".equals(isOwner)) {
				isO = 1;
			}

			// 受理内容
			String acceptcontent = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SERVICECONTENT/ACCEPTCONTENT");// 投诉内容
			String remark = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SERVICECONTENT/REMARK");// 备注
			
			// 订单关联对象
			OrderRelation relation = this.getOrderRelation(doc);

			OrderCustomerInfo orderCustInfo = getOrderCustomerInfo(prodType, prodnum, regionId);
			if (orderCustInfo == null) {
				return buildReturnInfo("1", "1", "查询客户资料为空，请确认传入数据是否正确", "", "");
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
			orderAskInfo.setAskStaffId(Integer.parseInt(accpetStaff.getId()));
			orderAskInfo.setAskStaffName(accpetStaff.getName());
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
			ServiceContent servContent = new ServiceContent();
			servContent.setOrderVer(0);
			servContent.setRegionId(regionId);
			servContent.setRegionName(regionName);
			servContent.setServType(Integer.parseInt(serviceType));
			servContent.setServTypeDesc(serviceTypeDesc);
			servContent.setServiceTypeDetail(StringUtils.defaultIfBlank(serviceTypeDetail, "其他"));
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
			servContent.setAcceptContent(acceptcontent);
			servContent.setCustExpect(remark);
			servContent.setBestOrder(100122410);
			servContent.setBestOrderDesc("否");
			
			this.setProdCode(orderCustInfo, servContent);
			this.setSpecialContent(servContent, bestOrder);
			this.setDevChnl(orderCustInfo, servContent);

			Map<String, String> map = new HashMap<>();
			map.put("ORGIDSTR", "");
			map.put("STRFLOW", "DISPATCHSHEET");
			map.put("SENDFLAG", "true");
			map.put("DEALREQUIE", "");
			map.put("ZHZDFLAG", "1");

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
				log.setServOrderId(servId);
				log.setActionResult("1");
				returnInfo = buildReturnInfo("0", "0", "生成工单成功", servId, orderCustInfo.getCrmCustId());
				this.saveOrderRelation(relation, servId);//保存关联信息
			} else {
				returnInfo = buildReturnInfo("1", "1", "生成工单失败", "", "");
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		} catch (Exception e) {
			logger.error("serviceAccpet 生成工单异常：{}", e.getMessage(), e);
			returnInfo = buildReturnInfo("1", "1", e.getMessage(), "", "");
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
	}

	private String changeSixCatalog(String sixCatalog) {
		if ("10508".equals(sixCatalog)) {
			return "10599";
		}
		return sixCatalog;
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
	
	/**
	 * 获取订单关联对象
	 * @param doc
	 * @return
	 */
	private OrderRelation getOrderRelation(Document doc) {
		String orderSource = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SERVICECONTENT/ORDER_SOURCE");//订单来源
		String otherOrderId = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SERVICECONTENT/OTHER_ORDER_ID");//优问单号
		OrderRelation relation = null;
		if(StringUtils.isNotBlank(orderSource) && StringUtils.isNotBlank(otherOrderId)) {
			String addressName = pubFun.getConfigMapValue(orderSource);
			relation = new OrderRelation();
			relation.setOtherOrderId(otherOrderId);
			relation.setSourceName(orderSource);
			relation.setAddressName(addressName);
			relation.setStatus(0);//处理中
			int pushFlag = StringUtils.isNotBlank(addressName) ? 1 : 0;//0-无需推送；1-未推送
			relation.setPushFlag(pushFlag);
		}
		return relation;
	}
	
	private void saveOrderRelation(OrderRelation relation, String orderId) {
		if(relation != null) {
			relation.setServiceOrderId(orderId);
			int num = pubwebServiceDaoImpl.saveOrderRelation(relation);
			logger.info("saveOrderRelation 保存结果: {}", (num > 0 ? "成功" : "失败"));
		}
	}
	
	private void setSpecialContent(ServiceContent servContent, String bestOrder) {
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
		errorMsg = checkCustInfo(doc);
		if (!StringUtils.isEmpty(errorMsg)) {
			return errorMsg;
		}
		errorMsg = checkOrderInfo(doc);
		if (!StringUtils.isEmpty(errorMsg)) {
			return errorMsg;
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SOURCE_SYSTEM"))) {
			errorMsg = buildReturnInfo("1", "1", "录单来源为空", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/SERVICECONTENT/ACCEPTCONTENT"))) {
			errorMsg = buildReturnInfo("1", "1", "投诉内容为空", "", "");
		}
		return errorMsg;
	}

	private String checkCustInfo(Document doc) {
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID"))) {
			return buildReturnInfo("1", "1", "地域ID为空", "", "");
		}
		String prodType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODTYPE");
		if (StringUtils.isEmpty(prodType)) {
			return buildReturnInfo("1", "1", "产品类型为空", "", "");
		}
		int queryType = Integer.parseInt(prodType);
		if (!(1 == queryType || 2 == queryType || 3 == queryType || 9 == queryType || 881 == queryType)) {
			return buildReturnInfo("1", "1", "产品类型取值超过范围", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM"))) {
			return buildReturnInfo("1", "1", "产品号码为空", "", "");
		}
		return "";
	}

	private String checkOrderInfo(Document doc) {
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAMAN"))) {
			return buildReturnInfo("1", "1", "联系人为空", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/RELAINFO"))) {
			return buildReturnInfo("1", "1", "联系信息为空", "", "");
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
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICEDATE"))) {
			return buildReturnInfo("1", "1", "流程号为空", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICEDATEDESC"))) {
			return buildReturnInfo("1", "1", "流程名为空", "", "");
		}
		String serviceType = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPE");
		if (StringUtils.isEmpty(serviceType)) {
			return buildReturnInfo("1", "1", "服务类型为空", "", "");
		}
		if (isServiceType(serviceType)) {
			return buildReturnInfo("1", "1", "服务类型取值超过范围", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SIXCATALOG"))) {
			return buildReturnInfo("1", "1", "受理目录为空", "", "");
		}
		if (StringUtils.isEmpty(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SOURCEID"))) {
			return buildReturnInfo("1", "1", "来源为空", "", "");
		}
		if (!Arrays.asList("", "0", "1", "2", "3").contains(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/UPTENDENCYFLAG"))) {
			return buildReturnInfo("1", "1", "越级倾向传值不规范", "", "");
		}
		if (!Arrays.asList("", "否", "电子意见簿").contains(getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPEDETAIL"))) {
			return buildReturnInfo("1", "1", "是否建议表扬传值不规范", "", "");
		}
		return "";
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

	private String setServiceContentSave(String keys, List<ServiceContentSave> list, Document doc){
		if(keys == null) {
			return buildReturnInfo("1", "1", "受理现象未许可录单，请联系接口提供方", "", "");
		}
		if(keys.equals("")) {
			return null;
		}
		String[] split = keys.split(",");
		for (String s : split) {
			if(s.indexOf(":") < 0) {
				return buildReturnInfo("1", "1", s+"未设置数据类型，请联系接口提供方", "", "");
			}
			
			String key = s.split(":")[0];
			String valueType = s.split(":")[1];
			String value = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/" + key);
			if (value.isEmpty()) {
				return buildReturnInfo("1", "1", key+"值为空，受理现象不允许录单", "", "");
			}
			
			if(!this.checkValueType(value, valueType)) {
				return buildReturnInfo("1", "1", key+"值数据类型不正确，受理现象不允许录单", "", "");
			}
			
			ServiceContentSave serviceContentSave = new ServiceContentSave();
			serviceContentSave.setAliasName(key);
			serviceContentSave.setAnswerName(value);
			list.add(serviceContentSave);
		}
		return null;
	}
	
	private boolean checkValueType(String value, String valueType) {
		if("datetime".equals(valueType)) {
			return DateUtil.isValidDate(value, "yyyy-MM-dd HH:mm:ss");
		} else if("datetime_nf".equals(valueType)) {
			return DateUtil.isValidDate(value, "yyyyMMddHHmmss");
		} else if("date".equals(valueType)) {
			return DateUtil.isValidDate(value, "yyyy-MM-dd");
		} else if("date_nf".equals(valueType)) {
			return DateUtil.isValidDate(value, "yyyyMMdd");
		} else if("number".equals(valueType)) {
			return StringUtils.isNumeric(value);
		} else {
			return true;
		}
	}
}