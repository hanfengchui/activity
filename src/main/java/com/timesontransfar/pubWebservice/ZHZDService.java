package com.timesontransfar.pubWebservice;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.dao.ITrackServiceDao;
import com.timesontransfar.customservice.orderask.pojo.IntfLog;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TrackInfo;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.impl.TsSheetSumbitImpl;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.feign.custominterface.WorkSheetFeign;
import com.transfar.common.enums.ResultEnum;
import com.transfar.common.web.ResultUtil;

import edu.emory.mathcs.backport.java.util.Arrays;
import net.sf.json.JSONObject;

@Component
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ZHZDService {
	protected static Logger logger = LoggerFactory.getLogger(ZHZDService.class);
	@Autowired
	private PubFunc pubFun;
	@Autowired
	private IserviceOrderAsk serviceOrderAskImpl;
	@Autowired
	private ITrackServiceDao trackServiceDao;
	@Autowired
	private CustomerServiceFeign customerServiceFeign;
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	@Autowired
	private TsSheetSumbitImpl tsSheetSumbitImpl;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
    @Autowired
    private WorkSheetFeign workSheetFeign;

	private static Map regions = new HashMap();
	static {
		regions.put("3", "南京市");
		regions.put("4", "镇江");
		regions.put("15", "无锡");
		regions.put("20", "苏州");
		regions.put("26", "南通");
		regions.put("33", "扬州");
		regions.put("39", "盐城");
		regions.put("48", "徐州");
		regions.put("60", "淮安");
		regions.put("63", "连云港");
		regions.put("69", "常州");
		regions.put("79", "泰州");
		regions.put("84", "宿迁");
	}
	
	private static String[] prodTypes = { "1", "2", "3", "9", "881" };
	private static String[] isOwners = { "1", "0" };
	
	private static Map serviceTypes = new HashMap();
	static {
		serviceTypes.put("720200003", "查询");
		serviceTypes.put("720130000", "投诉");
		serviceTypes.put("720200002", "特别跟踪");
	}
	
	private static Map custEmotions = new HashMap();
	static {
		custEmotions.put("700000025", "平和");
		custEmotions.put("700000027", "愤怒");
	}
	
	private static Map urgencyGrades = new HashMap(); 
	static {
		urgencyGrades.put("700000145", "普通");
		urgencyGrades.put("700000146", "加急");
		urgencyGrades.put("700000147", "紧急");
		urgencyGrades.put("700000148", "特急");
	}
	
	private static Map serviceTypeDetailXT = new HashMap();
	static{
		serviceTypeDetailXT.put("否", "否");
		serviceTypeDetailXT.put("建议", "建议");
		serviceTypeDetailXT.put("表扬", "表扬");
	}
	
	private static Map serviceTypeDetailGZ = new HashMap();
	static {
		serviceTypeDetailGZ.put("调账", "1");
		serviceTypeDetailGZ.put("其他", "3");
	}
	
	private Document returnSaxRed(String str) throws SAXException, DocumentException {
		SAXReader saxReader = new SAXReader();
		saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		StringReader reader = new StringReader(str);
		return saxReader.read(reader);
	}

	/**
	 * 给智慧诊断提供受理接口
	 * 
	 * @param orderInfo
	 * @return
	 */
	public String saveZHOrderId(String orderInfo) {
		IntfLog log = new IntfLog();
		log.setServOrderId("");
		log.setInMsg(orderInfo);
		log.setActionFlag("in");
		log.setActionResult("0");
		log.setSystem("zhzdService");
		TsmStaff tsmStaff = null;
		String prodNum = "";
		String regionId;
		String callSerialNo;
		String prodType;
		String relaMan;
		String relaInfo;
		String isOwner;
		String custName;
		String sourceNum;
		String acceptStaff;
		String serviceType;
		String sixCatalog;
		String custEmotion;
		String urgencyGrade;
		String acceptContent;
		String verifyReasonId;
		String verifyReasonName;
		String moreRelaInfo;
		String serviceTypeDetail;
		String toObjId;
		String refundMode;
		String installAddr = "";
		String isAutoComplete = "";
		String propCode = "";
		String prodCode = "";
		String refundModeDesc = "";
		String returnInfo;
		Document doc = null;
		try {
			doc = returnSaxRed(orderInfo);
			regionId = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");// 业务归属地
			callSerialNo = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/CALLSERIALNO");// 呼叫序列号(只用于一键录单)
			prodType = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODTYPE");// 产品类型
			prodNum = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");// 产品号码
			relaMan = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/RELAMAN");// 联系人
			relaInfo = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/RELAINFO");// 联系电话
			isOwner = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/ISOWNER");// 是否机主
			custName = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/CUSTNAME");// 客户姓名
			verifyReasonId = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/CUSTHANDSET");// 手机号码
			verifyReasonName = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/CUSTEMAIL");// 电子邮件
			moreRelaInfo = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/MORERELAINFO");// 其他联系方式
			sourceNum = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/SOURCENUM");// 主叫号码
			acceptStaff = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTSTAFF");// 受理员工
			serviceType = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPE");// 服务类型
			sixCatalog = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SIXCATALOG");// 受理目录
			custEmotion = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/CUSTEMOTION");// 客户情绪
			urgencyGrade = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/URGENCYGRADE");// 紧急程度
			acceptContent = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTCONTENT");// 受理内容
			serviceTypeDetail = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPEDETAIL");// 服务类型子类型
			toObjId = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/TOOBJID");// 分派部门
			refundMode = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/REFUNDMODE");// 退费方式
			installAddr = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/INSTALLADDR");// 装机地址
			isAutoComplete = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ISAUTOCOMPLETE");// 是否自动办结
			propCode = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/PROPCODE");// 建议处理编码
			prodCode = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/PROD_CODE");// 产品
		} catch (Exception e) {
			e.printStackTrace();
			returnInfo = bulidReturnInfo("1", "21", e.getMessage(), "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		String regionName = "";
		String serviceTypeDesc = "";
		String custEmotionDesc = "";
		String urgencyGradeDesc = "";
		String retErrorId = "";
		String retErrorInfo = "";
		if (!"".equals(installAddr) && installAddr.length() > 100) {
			retErrorId = "24";
			retErrorInfo = "INSTALLADDR装机地址字段过长";
		}
		if ("".equals(regionId)) {
			retErrorId = "22";
			retErrorInfo = "REGIONID业务归属地为空";
		} else if (!regions.containsKey(regionId)) {
			retErrorId = "24";
			retErrorInfo = "REGIONID业务归属地取值超过范围";
		} else {
			regionName = regions.get(regionId).toString();
		}
		if ("".equals(prodType)) {
			retErrorId = "22";
			retErrorInfo = "PRODTYPE产品类型为空";
		} else if (!Arrays.asList(prodTypes).contains(prodType)) {
			retErrorId = "24";
			retErrorInfo = "PRODTYPE产品类型取值超过范围";
		}
		if ("".equals(prodNum)) {
			retErrorId = "22";
			retErrorInfo = "PRODNUM产品号码为空";
		}
		if ("".equals(relaMan)) {
			retErrorId = "22";
			retErrorInfo = "RELAMAN联系人为空";
		}
		if (checkRelaInfo(relaInfo)) {
			retErrorId = "22";
			retErrorInfo = "RELAINFO联系电话为空或非数字";
		}
		if ("".equals(isOwner)) {
			retErrorId = "22";
			retErrorInfo = "ISOWNER是否机主为空";
		} else if (!Arrays.asList(isOwners).contains(isOwner)) {
			retErrorId = "24";
			retErrorInfo = "ISOWNER是否机主取值超过范围";
		}
		if ("".equals(custName)) {
			retErrorId = "22";
			retErrorInfo = "CUSTNAME客户姓名为空";
		}
		if ("".equals(sourceNum)) {
			retErrorId = "22";
			retErrorInfo = "SOURCENUM主叫号码为空";
		}
		if ("".equals(acceptStaff)) {
			retErrorId = "22";
			retErrorInfo = "ACCEPTSTAFF受理员工为空";
		}
		if ("".equals(serviceType)) {
			retErrorId = "22";
			retErrorInfo = "SERVICETYPE服务类型为空";
		} else if (!serviceTypes.containsKey(serviceType)) {
			retErrorId = "24";
			retErrorInfo = "SERVICETYPE服务类型取值超过范围";
		} else {
			serviceTypeDesc = serviceTypes.get(serviceType).toString();
		}
		if ("720200000".equals(serviceType)) {
			if ("".equals(serviceTypeDetail)) {
				retErrorId = "22";
				retErrorInfo = "协同单SERVICETYPEDETAIL服务类型子类型为空";
			} else if (!serviceTypeDetailXT.containsKey(serviceTypeDetail)) {
				retErrorId = "24";
				retErrorInfo = "协同单SERVICETYPEDETAIL服务类型子类型取值超过范围";
			}
		}
		if ("720200002".equals(serviceType)) {
			if ("".equals(serviceTypeDetail)) {
				retErrorId = "22";
				retErrorInfo = "跟踪单SERVICETYPEDETAIL服务类型子类型为空";
			} else if (!serviceTypeDetailGZ.containsKey(serviceTypeDetail)) {
				retErrorId = "24";
				retErrorInfo = "跟踪单SERVICETYPEDETAIL服务类型子类型取值超过范围";
			}
			if ("调账".equals(serviceTypeDetail)) {
				if ("".equals(refundMode)) {
					retErrorId = "22";
					retErrorInfo = "调账单REFUNDMODE退费方式为空";
				} else {
					refundModeDesc = pubFun.getSelRefundHotName(13, refundMode);
					if ("".equals(refundModeDesc)) {
						retErrorId = "24";
						retErrorInfo = "调账单REFUNDMODE退费方式取值超过范围";
					}
				}
			}
			if ("".equals(toObjId)) {
				retErrorId = "22";
				retErrorInfo = "跟踪单TOOBJID分派部门为空";
			} else if ("10".equals(pubFun.getOrgLink(toObjId))) {
				retErrorId = "24";
				retErrorInfo = "跟踪单TOOBJID分派部门取值超过范围";
			}
		}
		if ("".equals(sixCatalog)) {
			retErrorId = "22";
			retErrorInfo = "SIXCATALOG受理目录为空";
		} else if (null == pubFun.getSixCatalogMap(sixCatalog, 1)) {
			retErrorId = "24";
			retErrorInfo = "SIXCATALOG受理目录取值超过范围";
		} else {
			sixCatalog = pubFun.getSixCatalogMap(sixCatalog, 1);
		}
		if ("".equals(custEmotion)) {
			retErrorId = "22";
			retErrorInfo = "CUSTEMOTION客户情绪为空";
		} else if (!custEmotions.containsKey(custEmotion)) {
			retErrorId = "24";
			retErrorInfo = "CUSTEMOTION客户情绪取值超过范围";
		} else {
			custEmotionDesc = custEmotions.get(custEmotion).toString();
		}
		if ("".equals(urgencyGrade)) {
			retErrorId = "22";
			retErrorInfo = "URGENCYGRADE紧急程度为空";
		} else if (!urgencyGrades.containsKey(urgencyGrade)) {
			retErrorId = "24";
			retErrorInfo = "URGENCYGRADE紧急程度取值超过范围";
		} else {
			urgencyGradeDesc = urgencyGrades.get(urgencyGrade).toString();
		}
		if ("".equals(acceptContent)) {
			retErrorId = "22";
			retErrorInfo = "ACCEPTCONTENT受理内容为空";
		} else if (acceptContent.length() > 1000) {
			retErrorId = "25";
			retErrorInfo = "ACCEPTCONTENT受理内容超出字符限制";
		}
		
		//新增检验参数功能
		sixCatalog = changeSixCatalog(sixCatalog);// 24-12，四类数据改造
		String key = pubFun.getKeyById(sixCatalog);
		List<ServiceContentSave> list = new ArrayList<>();
		retErrorInfo = StringUtils.defaultIfEmpty(retErrorInfo, this.setServiceContentSave(key, list, doc));
		
		//校验产品
		prodCode = changeProdCode(prodCode);// 24-12，四类数据改造
		retErrorInfo = StringUtils.defaultIfEmpty(retErrorInfo, this.checkProdCode(prodCode));
		retErrorId = this.getErrorId(retErrorInfo);
		
		if (!"".equals(retErrorId)) {
			returnInfo = bulidReturnInfo("1", retErrorId, retErrorInfo, "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		try {
			tsmStaff = pubFun.getLogonStaffByLoginName(acceptStaff);
		} catch (Exception e) {
			e.printStackTrace();
			returnInfo = bulidReturnInfo("1", "23", "ACCEPTSTAFF受理员工不存在", "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		if (null == tsmStaff) {
			returnInfo = bulidReturnInfo("1", "23", "ACCEPTSTAFF受理员工不存在", "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		OrderCustomerInfo orderCustInfo = new OrderCustomerInfo();
		String qryResult = customerServiceFeign.getCustInfo(Integer.parseInt(prodType), prodNum, Integer.parseInt(regionId));
		JSONObject relJson = JSONObject.fromObject(qryResult);
		if ("0000".equals(relJson.getString("code"))) {
			orderCustInfo = (OrderCustomerInfo) JSONObject.toBean(relJson.getJSONObject("resultObj"), OrderCustomerInfo.class);
		}
		if (orderCustInfo == null || orderCustInfo.getCustName().equals("")) {
			returnInfo = bulidReturnInfo("1", "26", "查询客户信息失败", "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		try {
			orderCustInfo.setCustTypeName(pubFun.getStaticName(orderCustInfo.getCustType()));
			orderCustInfo.setCustServGradeName(pubFun.getStaticName(orderCustInfo.getCustServGrade()));
			orderCustInfo.setCustBrandDesc(pubFun.getStaticName(orderCustInfo.getCustBrand()));
			int lastXX = Integer.parseInt(sixCatalog);
			List catalogList = pubFun.queryAcceptDir(lastXX, 2);
			Map catalogMap = (Map) catalogList.get(0);
			String ns = catalogMap.get("N").toString();
			String ids = catalogMap.get("ID").toString();
			ServiceContent servContent = new ServiceContent();
			servContent.setRegionId(Integer.parseInt(regionId));
			servContent.setOrderVer(0);
			servContent.setRegionName(regionName);
			servContent.setServType(Integer.parseInt(serviceType));
			servContent.setServTypeDesc(serviceTypeDesc);
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
			servContent.setProdNum(prodNum);
			servContent.setAcceptContent(acceptContent);
			servContent.setFiveOrder(100122400);
			servContent.setFiveOrderDesc("否");
			servContent.setBestOrder(100122410);
			servContent.setBestOrderDesc("否");
			if ("720200000".equals(serviceType) || "720200002".equals(serviceType)) {
				servContent.setServiceTypeDetail(serviceTypeDetail);
			}
			int prodTwo = Integer.parseInt(prodCode);
			int prodOne = Integer.parseInt(pubFun.getSuperStaticId(prodTwo));
			String prodTwoDesc = pubFun.getStaticName(prodTwo);
			String prodOneDesc = pubFun.getStaticName(prodOne);
			servContent.setProdOne(prodOne);
			servContent.setProdOneDesc(prodOneDesc);
			servContent.setProdTwo(prodTwo);
			servContent.setProdTwoDesc(prodTwoDesc);
			this.setDevChnl(orderCustInfo, servContent);
			
			OrderAskInfo orderAskInfo = new OrderAskInfo();
			orderAskInfo.setRelaType(lastXX);
			orderAskInfo.setRegionId(Integer.parseInt(regionId));
			orderAskInfo.setOrderVer(0);
			orderAskInfo.setRegionName(regionName);
			orderAskInfo.setServType(Integer.parseInt(serviceType));
			orderAskInfo.setServTypeDesc(serviceTypeDesc);
			orderAskInfo.setCallSerialNo(callSerialNo);
			orderAskInfo.setSourceNum(sourceNum);
			orderAskInfo.setRelaMan(relaMan);
			orderAskInfo.setProdNum(prodNum);
			orderAskInfo.setRelaInfo(relaInfo);
			orderAskInfo.setIsOwner(Integer.parseInt(isOwner));
			orderAskInfo.setMoreRelaInfo(moreRelaInfo);
			orderAskInfo.setCustEmotion(Integer.parseInt(custEmotion));
			orderAskInfo.setCustEmotionDesc(custEmotionDesc);
			orderAskInfo.setComeCategory(707907001);
			orderAskInfo.setCategoryName("省内投诉");
			orderAskInfo.setAskSource(707907004);
			orderAskInfo.setAskSourceDesc("省内渠道");
			orderAskInfo.setAskChannelId(707907007);
			orderAskInfo.setAskChannelDesc("省集中10000号");
			orderAskInfo.setAskStaffId(Integer.parseInt(tsmStaff.getId()));
			orderAskInfo.setAskStaffName(tsmStaff.getName());
			orderAskInfo.setAskOrgId(tsmStaff.getOrganizationId());
			orderAskInfo.setAskOrgName(tsmStaff.getOrgName());
			orderAskInfo.setCustServGrade(orderCustInfo.getCustServGrade());
			orderAskInfo.setCustServGradeDesc(orderCustInfo.getCustServGradeName());
			orderAskInfo.setUrgencyGrade(Integer.parseInt(urgencyGrade));
			orderAskInfo.setUrgencyGradeDesc(urgencyGradeDesc);
			orderAskInfo.setVerifyReasonId(verifyReasonId);
			orderAskInfo.setVerifyReasonName(verifyReasonName);
			orderAskInfo.setComment(catalogMap.get("N").toString());
			orderAskInfo.setNetFlag(0);
			orderAskInfo.setCustGroup(orderCustInfo.getCustStratagemId());
			orderAskInfo.setCustGroupDesc(pubFun.getStaticName(orderCustInfo.getCustStratagemId()));
			orderAskInfo.setServiceDate(3);
			orderAskInfo.setServiceDateDesc("投诉");
			orderAskInfo.setAssistSellNo(" ");
			orderAskInfo.setAreaId(orderCustInfo.getAreaId());
			orderAskInfo.setAreaName(orderCustInfo.getAreaName());
			orderAskInfo.setSubStationId(orderCustInfo.getSubStationId());
			orderAskInfo.setSubStationName(orderCustInfo.getSubStationName());
			orderCustInfo.setRegionId(Integer.parseInt(regionId));
			orderCustInfo.setRegionName(regionName);
			orderCustInfo.setInstallAdd(installAddr);
			String productType = orderCustInfo.getProductType();
			orderAskInfo.setProductType(productType);
			if ("1".equals(productType)) {
				orderAskInfo.setProductTypeName("本省电信固话");
			} else if ("2".equals(productType)) {
				orderAskInfo.setProductTypeName("本省电信手机");
			} else if ("3".equals(productType)) {
				orderAskInfo.setProductTypeName("本省电信小灵通");
			} else if ("4".equals(productType)) {
				orderAskInfo.setProductTypeName("外省电信移动手机话");
			} else {
				orderAskInfo.setProductTypeName("外省或其它");
			}
			orderAskInfo.setSendToOrgId("");
			orderAskInfo.setSendToOrgName("");
			orderAskInfo.setAskCount(1);
			Map audMap = new HashMap();
			audMap.put("ORGIDSTR", "");
			audMap.put("STRFLOW", "DISPATCHSHEET");
			audMap.put("SENDFLAG", "true");
			audMap.put("DEALREQUIE", "");
			audMap.put("ZHZDFLAG", "1");
			if ("720200002".equals(serviceType)) {
				audMap.put("SEND_TO_OBJ_FLAG", 1);// 1-部门 0-员工
				audMap.put("SEND_TO_OBJ_ID", toObjId);
				audMap = sendToObjFlag(audMap, isAutoComplete, propCode, tsmStaff.getId());
			}
			ServiceContentSave[] array = new ServiceContentSave[list.size()];
			ServiceContentSave[] saves = list.toArray(array);
			
			String result = serviceOrderAskImpl.submitServiceOrderInstanceLabelNew(orderCustInfo, servContent,
					orderAskInfo, audMap, null, saves, null);
			String code = JSONObject.fromObject(result).optString("code");
			if ("0000".equals(code)) {
				String serviceOrderId = JSONObject.fromObject(result).optString("resultObj");
				serviceOrderAskImpl.autoPdAsync(serviceOrderId);// 自动转派
				//向智慧预警kafka发送数据
				if("720130000".equals(serviceType)) {
					workSheetFeign.sendServiceOrder(serviceOrderId);
				}
				else if ("720200002".equals(serviceType)) {
					TrackInfo track = new TrackInfo();
					track.setOldOrderId(serviceOrderId);
					int trackType = Integer.parseInt(serviceTypeDetailGZ.get(serviceTypeDetail).toString());
					track.setTrackType(trackType);
					track.setCreateType(3);
					if (1 == trackType) {
						track.setRefundMode(Integer.parseInt(refundMode));
						track.setRefundModeDesc(refundModeDesc);
					} else {
						track.setRefundMode(0);
						track.setRefundModeDesc("");
					}
					track.setNewOrderId(serviceOrderId);
					track.setCreateDate(pubFun.getSysDateFormat("%Y-%m-%d %H:%i:%s"));
					trackServiceDao.saveTrackInfoTz(track);
					autoComplete(tsmStaff, serviceOrderId, isAutoComplete, propCode);
				}
				returnInfo = bulidReturnInfo("0", "0", "生成工单成功", serviceOrderId);
				log.setServOrderId(serviceOrderId);
				log.setActionResult("1");
				log.setOutMsg(returnInfo);
				pubFun.saveYNSJIntfLog(log);
				return returnInfo;
			} else {
				returnInfo = bulidReturnInfo("1", "26", "生成工单失败", "");
				if (!"".equals(prodNum)) {
					log.setServOrderId(prodNum);
				}
				log.setOutMsg(returnInfo);
				pubFun.saveYNSJIntfLog(log);
				return returnInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnInfo = bulidReturnInfo("1", "26", e.getMessage(), "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
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

	private String changeProdCode(String prodCode) {
		if (5 == prodCode.length()) {// 如果是5位产品二级编码，则在第4位加0，变成6位
			prodCode = prodCode.substring(0, 3) + "0" + prodCode.substring(3);
		}
		return prodCode;
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
	
	private String checkProdCode(String prodCode) {
		int prodTwo = Integer.parseInt(prodCode);
		int prodOne = Integer.parseInt(pubFun.getSuperStaticId(prodTwo));
		String prodOneDesc = pubFun.getStaticName(prodOne);
		if(StringUtils.isBlank(prodOneDesc)){
			return "产品传值错误";
		}
		return "";
	}
	
	private String getErrorId(String retErrorInfo) {
		return !"".equals(retErrorInfo) ? "25" : "";
	}
	
	private String setServiceContentSave(String keys, List<ServiceContentSave> list, Document doc){
		String returnInfo = "";
		if(StringUtils.isBlank(keys)){
			returnInfo = "值未录入，请联系相关人员录值后录单";
			return returnInfo;
		}
		String[] split = keys.split(",");
		for (String s : split) {
			String value = getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/TEMPLATEINFO/" + s);
			if (value.isEmpty()) {
				returnInfo = s+"值为空，受理现象不允许录单";
				return returnInfo;
			}else if(value.length()>200){
				value = value.substring(0, 200);
			}
			ServiceContentSave serviceContentSave = new ServiceContentSave();
			serviceContentSave.setAliasName(s);
			serviceContentSave.setAnswerName(value);
			list.add(serviceContentSave);
		}
		return null;
	}

	private boolean checkRelaInfo(String relaInfo) {
		return "".equals(relaInfo) || (!StringUtils.isNumeric(relaInfo));
	}

	private Map sendToObjFlag(Map audMap, String isAutoComplete, String propCode, String staffId) {
		if ("1".equals(isAutoComplete)) {
			String dstCode = getDstCode(propCode, "2", "ITSM-PROPCODE");
			if (!"".equals(dstCode)) {
				audMap.put("SEND_TO_OBJ_FLAG", 0);// 1-部门 0-员工
				audMap.put("SEND_TO_OBJ_ID", staffId);
			}
		}
		return audMap;
	}

	private void autoComplete(TsmStaff tsmStaff, String serviceOrderId, String isAutoComplete, String propCode) {
		if ("1".equals(isAutoComplete)) {
			String dstCode = getDstCode(propCode, "2", "ITSM-PROPCODE");
			if (!"".equals(dstCode)) {
				SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(serviceOrderId, StaticData.SHEET_TYPE_TS_ASSING, 0);

				SheetPubInfo sheetBean = new SheetPubInfo();
				sheetBean.setServiceOrderId(serviceOrderId);
				sheetBean.setWorkSheetId(sheetInfo.getWorkSheetId());
				sheetBean.setRcvOrgId("ASSIGN_TO_AUD");
				sheetBean.setRegionId(sheetInfo.getRegionId());
				sheetBean.setMonth(sheetInfo.getMonth());
				sheetBean.setDealContent("通讯大数据行程卡投诉场景自动生成此单，用户满足修复条件，已在线处理，此单留痕。");
				sheetBean.setDealStaffId(Integer.parseInt(tsmStaff.getId()));
				sheetBean.setDealOrgId(tsmStaff.getOrganizationId());
				String dispatchToPigeonhole = tsWorkSheetDeal.dispatchToPigeonhole(sheetBean, 600001141, "一般处理", isAutoComplete);
				if (StringUtils.isNotEmpty(dispatchToPigeonhole)) {
					JSONObject result = JSONObject.fromObject(dispatchToPigeonhole);
					if (result.optString("code").equals("0000")) {
						List banjieDirs = pubFun.getBanjieDir(dstCode, 2);
						Map banjieDir = (Map) banjieDirs.get(0);
						String ns = banjieDir.get("N").toString();
						String ids = banjieDir.get("ID").toString();
						TsSheetQualitative qualitative = new TsSheetQualitative();
						qualitative.setOrderId(serviceOrderId);
						qualitative.setSheetId(sheetInfo.getWorkSheetId());
						qualitative.setRegion(sheetInfo.getRegionId());
						qualitative.setRegName(sheetInfo.getRegionName());
						qualitative.setMonthFlag(sheetInfo.getMonth());
						qualitative.setTsReasonId(Integer.parseInt(dstCode));
						qualitative.setTsReasonName(ns.replace(" > ", "-"));
						qualitative.setTsifBeing(700001817);
						qualitative.setAppendCases(0);
						qualitative.setCasesId(0);
						qualitative.setCasesName("");
						qualitative.setDutyOrg("707907097");
						qualitative.setDutyOrgName("集团公司");
						qualitative.setDutyOrgThird("");
						qualitative.setDutyOrgThirdName("");
						qualitative.setTsKeyWord(pubFun.getSplitIdByIdx(ids, 0));
						qualitative.setTsKeyWordDesc(pubFun.getSplitNameByIdx(ns, 0));
						qualitative.setSubKeyWord(pubFun.getSplitIdByIdx(ids, 1));
						qualitative.setSubKeyWordDesc(pubFun.getSplitNameByIdx(ns, 1));
						qualitative.setThreeCatalog(pubFun.getSplitIdByIdx(ids, 2));
						qualitative.setThreeCatalogDesc(pubFun.getSplitNameByIdx(ns, 2));
						qualitative.setThourCatalog(pubFun.getSplitIdByIdx(ids, 3));
						qualitative.setThourCatalogDesc(pubFun.getSplitNameByIdx(ns, 3));
						qualitative.setFiveCatalog(pubFun.getSplitIdByIdx(ids, 4));
						qualitative.setFiveCatalogDesc(pubFun.getSplitNameByIdx(ns, 4));
						qualitative.setSixCatalog(pubFun.getSplitIdByIdx(ids, 5));
						qualitative.setSixCatalogDesc(pubFun.getSplitNameByIdx(ns, 5));
						qualitative.setControlAreaFir(707907131);
						qualitative.setControlAreaFirDesc("企业有责");
						qualitative.setControlAreaSec(707907135);
						qualitative.setControlAreaSecDesc("企业三级责任");
						qualitative.setSatisfyId(600001166);
						qualitative.setSatisfyDesc("满意");
						qualitative.setForceFlag("70010106");
						qualitative.setForceFlagDesc("其他");
						qualitative.setUnsatisfyReason("");
						qualitative.setSysJudge("0");
						qualitative.setLastDealContent("");
						qualitative.setPlusOne("");
						qualitative.setPlusOneDesc("");
						qualitative.setPlusTwo("");
						qualitative.setPlusTwoDesc("");

						TScustomerVisit visit = new TScustomerVisit();
						visit.setServiceOrderId(serviceOrderId);
						visit.setWorkSheetId(sheetInfo.getWorkSheetId());
						visit.setRegionId(sheetInfo.getRegionId());
						visit.setRegionName(sheetInfo.getRegionName());
						visit.setMonth(sheetInfo.getMonth());
						visit.setCollectivityCircs(600001151);
						visit.setCollectivityCircsName("满意");
						visit.setTsDealAttitude(600001156);
						visit.setTsDealAttitudeName("满意");
						visit.setTsDealBetimes(600001161);
						visit.setTsDealBetimesName("满意");
						visit.setTsDealResult(600001166);
						visit.setTsDealResultName("满意");
						visit.setTsVisitResult("通讯大数据行程卡投诉场景自动生成此单，未进行回访。");
						visit.setVisitType("1");
						tsWorkSheetDeal.saveQualitativeAndVisit(qualitative, visit);
						tsSheetSumbitImpl.audSheetFinishAuto(serviceOrderId, tsmStaff.getLogonName());
					}
				}
			}
		}
	}

	private String getDstCode(String srcCode, String flag, String featureCode) {
		String dstCode = "";
		Map map = pubFun.getCmpDataMapDstCode(srcCode, flag, featureCode);
		if (!map.isEmpty()) {
			dstCode = map.get("DST_CODE") == null ? "" : map.get("DST_CODE").toString();
		}
		return dstCode;
	}

	/**
	 * 给智慧诊断提供暂存接口
	 * 
	 * @param orderInfo
	 * @return
	 */
	public String saveZCOrderId(String orderInfo) {
		IntfLog log = new IntfLog();
		log.setServOrderId("");
		log.setInMsg(orderInfo);
		log.setActionFlag("in");
		log.setActionResult("0");
		log.setSystem("zhzcService");
		TsmStaff tsmStaff = null;
		String prodNum = "";
		String regionId;
		String callSerialNo;
		String prodType;
		String relaMan;
		String relaInfo;
		String isOwner;
		String custName;
		String sourceNum;
		String acceptStaff;
		String serviceType;
		String sixCatalog;
		String custEmotion;
		String urgencyGrade;
		String acceptContent;
		String verifyReasonId;
		String verifyReasonName;
		String moreRelaInfo;
		String serviceTypeDetail;
		String installAddr;
		String prodCode = "";
		String returnInfo;
		Document doc = null;
		try {
			doc = returnSaxRed(orderInfo);
			regionId = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/REGIONID");// 业务归属地
			callSerialNo = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/CALLSERIALNO");// 呼叫序列号(只用于一键录单)
			prodType = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODTYPE");// 产品类型
			prodNum = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/PRODNUM");// 产品号码
			relaMan = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/RELAMAN");// 联系人
			relaInfo = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/RELAINFO");// 联系电话
			isOwner = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/ISOWNER");// 是否机主
			custName = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/CUSTNAME");// 客户姓名
			verifyReasonId = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/CUSTHANDSET");// 手机号码
			verifyReasonName = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/CUSTEMAIL");// 电子邮件
			moreRelaInfo = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/MORERELAINFO");// 其他联系方式
			sourceNum = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/CUSTINFO/SOURCENUM");// 主叫号码
			acceptStaff = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTSTAFF");// 受理员工
			serviceType = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPE");// 服务类型
			sixCatalog = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SIXCATALOG");// 受理目录
			custEmotion = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/CUSTEMOTION");// 客户情绪
			urgencyGrade = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/URGENCYGRADE");// 紧急程度
			acceptContent = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/ACCEPTCONTENT");// 受理内容
			serviceTypeDetail = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/SERVICETYPEDETAIL");// 服务类型子类型
			installAddr = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/INSTALLADDR");// 装机地址
			prodCode = this.getElementValue(doc, "/REQUEST_XML/SERVICEORDERID/ORDERINFO/PROD_CODE");// 产品
		} catch (Exception e) {
			e.printStackTrace();
			returnInfo = bulidReturnInfo("1", "21", e.getMessage(), "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		String regionName = "";
		String serviceTypeDesc = "";
		String custEmotionDesc = "";
		String urgencyGradeDesc = "";
		String retErrorId = "";
		String retErrorInfo = "";
		if (!"".equals(installAddr) && installAddr.length() > 100) {
			retErrorId = "24";
			retErrorInfo = "INSTALLADDR装机地址字段过长";
		}
		if ("".equals(regionId)) {
			retErrorId = "22";
			retErrorInfo = "REGIONID业务归属地为空";
		} else if (!regions.containsKey(regionId)) {
			retErrorId = "24";
			retErrorInfo = "REGIONID业务归属地取值超过范围";
		} else {
			regionName = regions.get(regionId).toString();
		}
		if ("".equals(prodType)) {
			retErrorId = "22";
			retErrorInfo = "PRODTYPE产品类型为空";
		} else if (!Arrays.asList(prodTypes).contains(prodType)) {
			retErrorId = "24";
			retErrorInfo = "PRODTYPE产品类型取值超过范围";
		}
		if ("".equals(prodNum)) {
			retErrorId = "22";
			retErrorInfo = "PRODNUM产品号码为空";
		}
		if ("".equals(relaMan)) {
			retErrorId = "22";
			retErrorInfo = "RELAMAN联系人为空";
		}
		if (checkRelaInfo(relaInfo)) {
			retErrorId = "22";
			retErrorInfo = "RELAINFO联系电话为空或非数字";
		}
		if ("".equals(isOwner)) {
			retErrorId = "22";
			retErrorInfo = "ISOWNER是否机主为空";
		} else if (!Arrays.asList(isOwners).contains(isOwner)) {
			retErrorId = "24";
			retErrorInfo = "ISOWNER是否机主取值超过范围";
		}
		if ("".equals(custName)) {
			retErrorId = "22";
			retErrorInfo = "CUSTNAME客户姓名为空";
		}
		if ("".equals(sourceNum)) {
			retErrorId = "22";
			retErrorInfo = "SOURCENUM主叫号码为空";
		}
		if ("".equals(acceptStaff)) {
			retErrorId = "22";
			retErrorInfo = "ACCEPTSTAFF受理员工为空";
		}
		if ("".equals(serviceType)) {
			retErrorId = "22";
			retErrorInfo = "SERVICETYPE服务类型为空";
		} else if (!serviceTypes.containsKey(serviceType)) {
			retErrorId = "24";
			retErrorInfo = "SERVICETYPE服务类型取值超过范围";
		} else {
			serviceTypeDesc = serviceTypes.get(serviceType).toString();
		}
		if ("720200000".equals(serviceType)) {
			if ("".equals(serviceTypeDetail)) {
				retErrorId = "22";
				retErrorInfo = "协同单SERVICETYPEDETAIL服务类型子类型为空";
			} else if (!serviceTypeDetailXT.containsKey(serviceTypeDetail)) {
				retErrorId = "24";
				retErrorInfo = "协同单SERVICETYPEDETAIL服务类型子类型取值超过范围";
			}
		}
		if ("720200002".equals(serviceType)) {
			if ("".equals(serviceTypeDetail)) {
				retErrorId = "22";
				retErrorInfo = "跟踪单SERVICETYPEDETAIL服务类型子类型为空";
			} else if (!serviceTypeDetailGZ.containsKey(serviceTypeDetail)) {
				retErrorId = "24";
				retErrorInfo = "跟踪单SERVICETYPEDETAIL服务类型子类型取值超过范围";
			}
		}
		if ("".equals(sixCatalog)) {
			retErrorId = "22";
			retErrorInfo = "SIXCATALOG受理目录为空";
		} else if (null == pubFun.getSixCatalogMap(sixCatalog, 1)) {
			retErrorId = "24";
			retErrorInfo = "SIXCATALOG受理目录取值超过范围";
		} else {
			sixCatalog = pubFun.getSixCatalogMap(sixCatalog, 1);
		}
		if ("".equals(custEmotion)) {
			retErrorId = "22";
			retErrorInfo = "CUSTEMOTION客户情绪为空";
		} else if (!custEmotions.containsKey(custEmotion)) {
			retErrorId = "24";
			retErrorInfo = "CUSTEMOTION客户情绪取值超过范围";
		} else {
			custEmotionDesc = custEmotions.get(custEmotion).toString();
		}
		if ("".equals(urgencyGrade)) {
			retErrorId = "22";
			retErrorInfo = "URGENCYGRADE紧急程度为空";
		} else if (!urgencyGrades.containsKey(urgencyGrade)) {
			retErrorId = "24";
			retErrorInfo = "URGENCYGRADE紧急程度取值超过范围";
		} else {
			urgencyGradeDesc = urgencyGrades.get(urgencyGrade).toString();
		}
		if ("".equals(acceptContent)) {
			retErrorId = "22";
			retErrorInfo = "ACCEPTCONTENT受理内容为空";
		} else if (acceptContent.length() > 1000) {
			retErrorId = "25";
			retErrorInfo = "ACCEPTCONTENT受理内容超出字符限制";
		}
		
		//校验产品
		prodCode = changeProdCode(prodCode);// 24-12，四类数据改造
		retErrorInfo = StringUtils.defaultIfEmpty(retErrorInfo, this.checkProdCode(prodCode));
		retErrorId = this.getErrorId(retErrorInfo);
				
		if (!"".equals(retErrorId)) {
			returnInfo = bulidReturnInfo("1", retErrorId, retErrorInfo, "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		try {
			tsmStaff = pubFun.getLogonStaffByLoginName(acceptStaff);
		} catch (Exception e) {
			e.printStackTrace();
			returnInfo = bulidReturnInfo("1", "23", "ACCEPTSTAFF受理员工不存在", "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		if (null == tsmStaff) {
			returnInfo = bulidReturnInfo("1", "23", "ACCEPTSTAFF受理员工不存在", "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		OrderCustomerInfo orderCustInfo = new OrderCustomerInfo();
		String qryResult = customerServiceFeign.getCustInfo(Integer.parseInt(prodType), prodNum,
				Integer.parseInt(regionId));
		JSONObject relJson = JSONObject.fromObject(qryResult);
		if ("0000".equals(relJson.getString("code"))) {
			orderCustInfo = (OrderCustomerInfo) JSONObject.toBean(relJson.getJSONObject("resultObj"),
					OrderCustomerInfo.class);
		}
		if (orderCustInfo == null || orderCustInfo.getCustName().equals("")) {
			returnInfo = bulidReturnInfo("1", "26", "查询客户信息失败", "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
		try {
			orderCustInfo.setCustTypeName(pubFun.getStaticName(orderCustInfo.getCustType()));
			orderCustInfo.setCustServGradeName(pubFun.getStaticName(orderCustInfo.getCustServGrade()));
			orderCustInfo.setCustBrandDesc(pubFun.getStaticName(orderCustInfo.getCustBrand()));

			sixCatalog = changeSixCatalog(sixCatalog);// 24-12，四类数据改造
			int lastXX = Integer.parseInt(sixCatalog);
			List catalogList = pubFun.queryAcceptDir(lastXX, 2);
			Map catalogMap = (Map) catalogList.get(0);
			String ns = catalogMap.get("N").toString();
			String ids = catalogMap.get("ID").toString();
			ServiceContent servContent = new ServiceContent();
			servContent.setRegionId(Integer.parseInt(regionId));
			servContent.setOrderVer(0);
			servContent.setRegionName(regionName);
			servContent.setServType(Integer.parseInt(serviceType));
			servContent.setServTypeDesc(serviceTypeDesc);
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
			servContent.setProdNum(prodNum);
			servContent.setAcceptContent(acceptContent);
			if ("720200000".equals(serviceType) || "720200002".equals(serviceType)) {
				servContent.setServiceTypeDetail(serviceTypeDetail);
			}
			int prodTwo = Integer.parseInt(prodCode);
			int prodOne = Integer.parseInt(pubFun.getSuperStaticId(prodTwo));
			String prodTwoDesc = pubFun.getStaticName(prodTwo);
			String prodOneDesc = pubFun.getStaticName(prodOne);
			servContent.setProdOne(prodOne);
			servContent.setProdOneDesc(prodOneDesc);
			servContent.setProdTwo(prodTwo);
			servContent.setProdTwoDesc(prodTwoDesc);
			this.setDevChnl(orderCustInfo, servContent);
			
			OrderAskInfo orderAskInfo = new OrderAskInfo();
			orderAskInfo.setRelaType(lastXX);
			orderAskInfo.setRegionId(Integer.parseInt(regionId));
			orderAskInfo.setOrderVer(0);
			orderAskInfo.setRegionName(regionName);
			orderAskInfo.setServType(Integer.parseInt(serviceType));
			orderAskInfo.setServTypeDesc(serviceTypeDesc);
			orderAskInfo.setCallSerialNo(callSerialNo);
			orderAskInfo.setSourceNum(sourceNum);
			orderAskInfo.setRelaMan(relaMan);
			orderAskInfo.setProdNum(prodNum);
			orderAskInfo.setRelaInfo(relaInfo);
			orderAskInfo.setIsOwner(Integer.parseInt(isOwner));
			orderAskInfo.setMoreRelaInfo(moreRelaInfo);
			orderAskInfo.setCustEmotion(Integer.parseInt(custEmotion));
			orderAskInfo.setCustEmotionDesc(custEmotionDesc);
			orderAskInfo.setComeCategory(707907001);
			orderAskInfo.setCategoryName("省内投诉");
			orderAskInfo.setAskSource(707907004);
			orderAskInfo.setAskSourceDesc("省内渠道");
			orderAskInfo.setAskChannelId(707907007);
			orderAskInfo.setAskChannelDesc("省集中10000号");
			orderAskInfo.setAskStaffId(Integer.parseInt(tsmStaff.getId()));
			orderAskInfo.setAskStaffName(tsmStaff.getName());
			orderAskInfo.setAskOrgId(tsmStaff.getOrganizationId());
			orderAskInfo.setAskOrgName(tsmStaff.getOrgName());
			orderAskInfo.setCustServGrade(orderCustInfo.getCustServGrade());
			orderAskInfo.setCustServGradeDesc(orderCustInfo.getCustServGradeName());
			orderAskInfo.setUrgencyGrade(Integer.parseInt(urgencyGrade));
			orderAskInfo.setUrgencyGradeDesc(urgencyGradeDesc);
			orderAskInfo.setVerifyReasonId(verifyReasonId);
			orderAskInfo.setVerifyReasonName(verifyReasonName);
			orderAskInfo.setComment(catalogMap.get("N").toString());
			orderAskInfo.setNetFlag(0);
			orderAskInfo.setCustGroup(orderCustInfo.getCustStratagemId());
			orderAskInfo.setCustGroupDesc(pubFun.getStaticName(orderCustInfo.getCustStratagemId()));
			orderAskInfo.setServiceDate(3);
			orderAskInfo.setServiceDateDesc("投诉");
			orderAskInfo.setAssistSellNo(" ");
			orderAskInfo.setAreaId(orderCustInfo.getAreaId());
			orderAskInfo.setAreaName(orderCustInfo.getAreaName());
			orderAskInfo.setSubStationId(orderCustInfo.getSubStationId());
			orderAskInfo.setSubStationName(orderCustInfo.getSubStationName());
			orderCustInfo.setRegionId(Integer.parseInt(regionId));
			orderCustInfo.setRegionName(regionName);
			orderCustInfo.setInstallAdd(installAddr);
			String productType = orderCustInfo.getProductType();
			orderAskInfo.setProductType(productType);
			if ("1".equals(productType)) {
				orderAskInfo.setProductTypeName("本省电信固话");
			} else if ("2".equals(productType)) {
				orderAskInfo.setProductTypeName("本省电信手机");
			} else if ("3".equals(productType)) {
				orderAskInfo.setProductTypeName("本省电信小灵通");
			} else if ("4".equals(productType)) {
				orderAskInfo.setProductTypeName("外省电信移动手机话");
			} else {
				orderAskInfo.setProductTypeName("外省或其它");
			}
			orderAskInfo.setSendToOrgId("");
			orderAskInfo.setSendToOrgName("");
			orderAskInfo.setAskCount(1);
			orderAskInfo.setZhzdFlag("1");
			String result = ResultUtil.fail(ResultEnum.FALL);
			result = serviceOrderAskImpl.holdServiceOrderInstance(orderCustInfo, servContent, orderAskInfo);
			String code = JSONObject.fromObject(result).optString("code");
			if ("0000".equals(code)) {
				String serviceOrderId = JSONObject.fromObject(result).optString("resultObj");
				returnInfo = bulidReturnInfo("0", "0", "暂存工单成功", serviceOrderId);
				log.setServOrderId(serviceOrderId);
				log.setActionResult("1");
				log.setOutMsg(returnInfo);
				pubFun.saveYNSJIntfLog(log);
				return returnInfo;
			} else {
				returnInfo = bulidReturnInfo("1", "26", "暂存工单失败", "");
				if (!"".equals(prodNum)) {
					log.setServOrderId(prodNum);
				}
				log.setOutMsg(returnInfo);
				pubFun.saveYNSJIntfLog(log);
				return returnInfo;
			}
		} catch (Exception e) {
			e.printStackTrace();
			returnInfo = bulidReturnInfo("1", "26", e.getMessage(), "");
			if (!"".equals(prodNum)) {
				log.setServOrderId(prodNum);
			}
			log.setOutMsg(returnInfo);
			pubFun.saveYNSJIntfLog(log);
			return returnInfo;
		}
	}

	/**
	 * 返回xpath对应元素的值
	 */
	private String getElementValue(Document doc, String xpath) {
		Element e = (Element) doc.selectSingleNode(xpath);
		if (e != null)
			return e.getStringValue().trim();
		else
			return "";
	}

	private String bulidReturnInfo(String retFlag, String retErrorId, String retErrorInfo, String serviceOrderId) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><REQUEST_XML><RETFLAG>" + retFlag + "</RETFLAG><RETERRORID>"
				+ retErrorId + "</RETERRORID><RETERRORINFO>" + retErrorInfo + "</RETERRORINFO><SERVICEID>"
				+ serviceOrderId + "</SERVICEID></REQUEST_XML>";
	}
}