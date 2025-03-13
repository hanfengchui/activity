package com.timesontransfar.dapd.service.impl;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedRepeat;
import com.dapd.pojo.DapdSheetInfo;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.CustomerPersona;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.worksheet.dao.IForceDistillDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedRepeatDAOImpl;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.dapd.dao.IdapdSheetInfoDao;
import com.timesontransfar.dapd.service.IdapdSheetInfoService;
import com.timesontransfar.labelLib.dao.ILabelInstanceDao;
import com.timesontransfar.trackservice.dao.TrackDao;

@Service
@SuppressWarnings({ "rawtypes" })
public class DapdSheetInfoServiceImpl implements IdapdSheetInfoService {
	private static final Logger log = LoggerFactory.getLogger(DapdSheetInfoServiceImpl.class);
	
	@Autowired
	private IdapdSheetInfoDao dapdDao;
	@Autowired
	private PubFunc pubFun;
	@Autowired
	private ItsSheetQualitative qualitativeDao;
	@Autowired
	private TrackDao trkDao;
	@Autowired
	private IserviceContentDao contentDao;
	@Autowired
	private IForceDistillDao forceDao;
	@Autowired
	private ISheetPubInfoDao sheetDao;
	@Autowired
	private CmpUnifiedRepeatDAOImpl unifiedRepeatDao;
	@Autowired
	private ILabelInstanceDao instanceDao;
	@Autowired
	private IorderAskInfoDao orderDao;

	public void submitDapdSheetInfo(ServiceOrderInfo soInfo, ServiceContentSave[] saves, ServiceLabel label,
			CustomerPersona persona, ComplaintInfo compInfo) {
		OrderAskInfo order = soInfo.getOrderAskInfo();
		OrderCustomerInfo cust = soInfo.getOrderCustInfo();
		ServiceContent content = soInfo.getServContent();
		DapdSheetInfo dapd = new DapdSheetInfo();
		String sheetIdProv = order.getServOrderId();
		dapd.setPrvnceId(getPrvnceId(o2i(order.getRegionId())));// PRVNCE_ID 省份标识：客户来电反馈问题对应的业务号码的所属省份，参见公共主数据附录：地域编码，填写省份层级取值，非电信业务号码和非本省号码传-1
		dapd.setLatnId(getLatnId(o2i(order.getRegionId())));// LATN_ID 本地网标识：客户来电反馈问题对应的业务号码的所属本地网，参见公共主数据附录：地域编码，填写本地网层级取值，非电信业务号码和非本省号码传-1
		dapd.setReginId(z2n(o2i(order.getAreaId())));// REGIN_ID 区县标识：客户来电反馈问题对应的业务号码的所属区县，参考2.9.2.2地域维表 第三层电信区域ID ，非电信业务号码和非本省号码传-1
		dapd.setSubGridId(z2n(o2i(order.getSubStationId())));// SUB_GRID_ID 支局标识：可与产品实例表中的SUB_GRID_ID保持一致，代表区县下一层级，非电信业务号码和非本省号码传-1
		dapd.setRecordNo(getRecordNo(o2s(order.getCallSerialNo())));// RECORD_NO 录音流水号：单次通话的流水号，通过录音流水号可以定位用户录音，示例：402401171126479109323-2
		dapd.setCallId(order.getCallId());//CALL_ID 呼叫流水号：主通话流水号，一通呼叫以为人工座席转接产生的录音流水号
		dapd.setSheetOprtChnl(pubFun.getFullOrgName(o2s(order.getAskOrgId()), o2s(order.getAskOrgName())));// SHEET_OPRT_CHNL 工单受理渠道：客服系统受理工单起单的渠道名称
		dapd.setSheetIdProv(sheetIdProv);// SHEET_ID_PROV 省内工单编号：省内工单编号，客服系统受理的工单编号
		dapd.setCmplntId(order.getUnifiedComplaintCode());// CMPLNT_ID 投诉编码：客服系统受理工单时自动触发获取的投诉编码
		dapd.setSheetIdCroscmplnt(o2s(order.getComplaintWorksheetId()));// SHEET_ID_CROSCMPLNT 集团工单编号：口径改为：由集团二级平台下发到各省，客服系统将集团工单转省内系统时，带入集团工单编号
		int askStaffId = o2i(order.getAskStaffId());
		TsmStaff ts = pubFun.getStaff(askStaffId);
		if (null == ts) {
			dapd.setStaffId(z2n(askStaffId));
		} else {
			dapd.setStaffId(ts.getLogonName());// STAFF_ID 工单受理工号：客服系统受理员工
		}
		dapd.setCaller(StringUtils.defaultIfEmpty(order.getSourceNum(), order.getRelaInfo()));// CALLER 主叫号码：客服系统关联带入
		dapd.setBusiNbr(order.getProdNum());// BUSI_NBR 业务号码：客户主诉号码、问题号码，可根据客服系统关联或客户原声的智能打标结果带出，可由人工点选实际号码
		dapd.setProdInstId(cust.getProdInstId());// PROD_INST_ID 产品实例ID：业务号码对应产品实例id，可与产品实例表数据关联，非电信业务号码和非本省号码传-1
		dapd.setCustId(cust.getCrmCustId());// CUST_ID 客户ID：业务号码对应客户id，非电信业务号码和非本省号码传-1（与DAPD_CUST_INFO中的CUST_ID相同）
		dapd.setContactNo1(order.getRelaInfo());// CONTACT_NO1 联系电话1：客服系统关联带入，默认主叫号码
		dapd.setCmplntDesc(content.getAcceptContent());// CMPLNT_DESC 投/申诉内容：用户投、申诉内容
		setCmplntSource(dapd, order);
		int bestOrder = o2i(content.getBestOrder());
		if (bestOrder > 100122410) {
			dapd.setStrictestSheetId("1");// STRICTEST_SHEET_ID 是否市场最严工单：0否，1是，-1未知
			dapd.setStrictestSheetType(getStrictestSheetType(bestOrder));// STRICTEST_SHEET_TYPE 市场最严工单场景：0：业务退订、1：套餐变更、2：销户退网、3：携号转网、4：靓号低消、5：费用争议、-1：非市场最严工单，分类上传-1
		} else {
			dapd.setStrictestSheetId("0");
			dapd.setStrictestSheetType("-1");
		}
		dapd.setCmplntAprLevel1Nm(content.getAppealProdName());// CMPLNT_APR_LEVEL_1_NM 投诉现象一级：投诉现象一级名称，客户原声的智能打标结果，或人工点选的客户投诉现象，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		dapd.setCmplntAprLevel2Nm(content.getAppealReasonDesc());// CMPLNT_APR_LEVEL_2_NM 投诉现象二级：投诉现象二级名称，客户原声的智能打标结果，或人工点选的客户投诉现象，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		dapd.setCmplntAprLevel1(z2n(o2i(content.getAppealProdId())));// CMPLNT_APR_LEVEL_1 投诉现象一级编码：投诉现象编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		dapd.setCmplntAprLevel2(z2n(o2i(content.getAppealReasonId())));// CMPLNT_APR_LEVEL_2 投诉现象二级编码：投诉现象编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		int cmplntAprLevel3 = o2i(content.getAppealChild());
		if (cmplntAprLevel3 > 0) {
			dapd.setCmplntAprLevel3Nm(content.getAppealChildDesc());// CMPLNT_APR_LEVEL_3_NM 投诉现象三级：投诉现象三级名称，客户原声的智能打标结果，或人工点选的客户投诉现象，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
			dapd.setCmplntAprLevel3(z2n(cmplntAprLevel3));// CMPLNT_APR_LEVEL_3 投诉现象三级编码：投诉现象编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		}
		dapd.setCmplntProdLevel1Nm(content.getProdOneDesc());// CMPLNT_PROD_LEVEL_1_NM 产品一级：投诉产品名称，客户原声的智能打标结果，或人工点选的客户投诉产品，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-产品。
		dapd.setCmplntProdLevel2Nm(content.getProdTwoDesc());// CMPLNT_PROD_LEVEL_2_NM 产品二级：投诉产品名称，客户原声的智能打标结果，或人工点选的客户投诉产品，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-产品。
		dapd.setCmplntProdLevel1(z2n(o2i(content.getProdOne())));// CMPLNT_PROD_LEVEL_1 产品一级编码：投诉产品编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-产品。
		dapd.setCmplntProdLevel2(z2n(o2i(content.getProdTwo())));// CMPLNT_PROD_LEVEL_2 产品二级编码：投诉产品编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-产品。
		dapd.setDvlpChnlNm(content.getDvlpChnlNm());// DVLP_CHNL_NM 发展渠道：发展渠道名称，资产入网时的受理渠道（渠道视图中的渠道单元名称），渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		dapd.setDvlpChnl(content.getDvlpChnl());// DVLP_CHNL 发展渠道编码：渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		int dvlpChnlLevel1 = o2i(content.getDevtChsOne());
		if (dvlpChnlLevel1 > 0) {
			dapd.setDvlpChnlLevel1Nm(content.getDevtChsOneDesc());// DVLP_CHNL_LEVEL_1_NM 发展渠道一级：发展渠道一级名称，发展渠道对应集团统一视图的分类层级，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
			dapd.setDvlpChnlLevel1(z2n(dvlpChnlLevel1));// DVLP_CHNL_LEVEL_1 发展渠道一级编码：发展渠道一级编码，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		}
		int dvlpChnlLevel2 = o2i(content.getDevtChsTwo());
		if (dvlpChnlLevel2 > 0) {
			dapd.setDvlpChnlLevel2Nm(content.getDevtChsTwoDesc());// DVLP_CHNL_LEVEL_2_NM 发展渠道二级：发展渠道二级名称，发展渠道对应集团统一视图的分类层级，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
			dapd.setDvlpChnlLevel2(z2n(dvlpChnlLevel2));// DVLP_CHNL_LEVEL_2 发展渠道二级编码：发展渠道二级编码，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		}
		int dvlpChnlLevel3 = o2i(content.getDevtChsThree());
		if (dvlpChnlLevel3 > 0) {
			dapd.setDvlpChnlLevel3Nm(content.getDevtChsThreeDesc());// DVLP_CHNL_LEVEL_3_NM 发展渠道三级：发展渠道三级名称，发展渠道对应集团统一视图的分类层级，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
			dapd.setDvlpChnlLevel3(z2n(dvlpChnlLevel3));// DVLP_CHNL_LEVEL_3 发展渠道三级编码：发展渠道三级编码，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		}
		dapd.setRecmplntTimes30days(z2n(o2i(label.getRepeatNewFlag())));// RECMPLNT_TIMES_30DAYS 30天重复投诉次数：根据投诉号码关联最近30天用户是否有重复工单记录，并展示次数
		dapd.setCroscmplntTimes30days(z2n(o2i(persona.getUpTendencyNum())));// CROSCMPLNT_TIMES_30DAYS 30天越级投诉次数：根据投诉号码关联最近30天用户是否有越级工单记录，并展示次数
		dapd.setMoneyback90days(z2n(o2i(persona.getRefundNum())));// MONEYBACK_90DAYS 90天退费记录：根据投诉号码关联最近90天用户是否有退费工单记录
		dapd.setSheetEstima30days(getSheetEstima30days(o2i(persona.getUnsatisfyNum()), o2i(persona.getSatisfyNum())));// SHEET_ESTIMA_30DAYS 30天工单评价：置空，该字段不再上传
		dapd.setUpgradeTrend(getUpgradeTrend(o2i(label.getUpTendencyFlag())));// UPGRADE_TREND 客户升级投诉倾向：客户可能升级投诉倾向的预判结果，系统提供下拉选项供系统自动识别打标或手工点选，选项可参考：“工信部、通管局、媒体曝光”等，省内可根据客户业务号码、联系电话、历史接触记录等因素作判断和标识
		dapd.setCustType(order.getCustGroupDesc());// CUST_TYPE 客户类型：号码类型分类
		dapd.setMemberLevel(cust.getCustServGradeName());// MEMBER_LEVEL 用户星级：业务号码的星级等级，结合星级管理办法、省内系统对用户号码的星级标识获取结果，结果如：0星/1星/2星/3星/4星黄金/5星白金/6星黑金/7星钻石
		dapd.setServAge(getServAge(cust.getInstalldate()));// SERV_AGE 网龄：号码入网时长，按月取数，向下取整
		dapd.setAge(z2n(o2i(cust.getCustAge())));// AGE 用户年龄：客户年龄
		dapd.setImportCust(gtz2y(o2i(persona.getIsKeyCustomer())));// IMPORT_CUST 重要客户：客服系统-要客标签
		dapd.setDictKey(gtz2y(o2i(persona.getIsKeyPerson())));// DICT_KEY 政企关键人：客服系统-政企关键人标签
		dapd.setCityFlag(persona.getCityLabel());// CITY_FLAG 城乡标识：城镇、农村
		dapd.setMainOfferId(wu2n(dapd.getMainOfferId()));// MAIN_OFFER_ID 主套餐ID：CRM定价ID
		dapd.setMainOfferNm(wu2n(dapd.getMainOfferNm()));// MAIN_OFFER_NM 主套餐名称：主销售品名称
		dapd.setDisputeChnlNm(content.getDisputeChnlNm());// DISPUTE_CHNL_NM 争议渠道：与客诉相关的销售品受理渠道或服务问题发生渠道
		dapd.setDisputeChnl(content.getDisputeChnl());// DISPUTE_CHNL 争议渠道编码：争议渠道编码
		dapd.setDisputeChnl1Nm(content.getDisputeChnl1Nm()); // DISPUTE_CHNL_1_NM 争议渠道一级：争议渠道对应集团统一视图的分类层级
		dapd.setDisputeChnl1(z2n(content.getDisputeChnl1())); // DISPUTE_CHNL_1 争议渠道一级编码：争议渠道一级编码
		dapd.setDisputeChnl2Nm(content.getDisputeChnl2Nm()); // DISPUTE_CHNL_2_NM 争议渠道二级：争议渠道对应集团统一视图的分类层级
		dapd.setDisputeChnl2(z2n(content.getDisputeChnl2())); // DISPUTE_CHNL_2 争议渠道二级编码：争议渠道二级编码
		dapd.setDisputeChnl3Nm(content.getDisputeChnl3Nm()); // DISPUTE_CHNL_3_NM 争议渠道三级：争议渠道对应集团统一视图的分类层级
		dapd.setDisputeChnl3(z2n(content.getDisputeChnl3())); // DISPUTE_CHNL_3 争议渠道三级编码：争议渠道三级编码
		dapd.setRealNameStatus(cust.getIsRealname());// REAL_NAME_STATUS 实名状态：CRM系统中用户实名状态：0否，1是
		dapd.setResConChannel(getResConChannel(order));// RES_CON_CHANNEL 责任管控渠道：通过资产落地归属获取，资产落地网格的组织信息（例如*省分公司*市分公司-*分局-*支局-*网格）
		dapd.setNbrLocation(cust.getBranchNo());//NBR_LOCATION 号码归属地：根据业务号码关联归属地信息，在客服系统建单时关联带入，示例：成都
		dapd.setSheetType(getSheetType(order.getServType()));// SHEET_TYPE 工单类型：工单类型，示例：投诉单
		dapd.setCustName(StringUtils.defaultIfEmpty(cust.getCustName(), order.getRelaMan()));// CUST_NAME 客户名称：CRM系统登记的业务号码的客户名称，系统自动获取CRM登记信息，示例：张三
		dapd.setSheetEstima30daysSat(z2n(o2i(persona.getSatisfyNum())));// SHEET_ESTIMA_30DAYS_SAT 30天工单评价-满意：根据投诉号码关联最近30天用户历史投诉工单的满意度测评结果为“满意”的次数，上传结果为数字
		dapd.setSheetEstima30daysDissat(z2n(o2i(persona.getUnsatisfyNum())));// SHEET_ESTIMA_30DAYS_DISSAT 30天工单评价-不满意：根据投诉号码关联最近30天用户历史投诉工单的满意度测评结果为“不满意”的次数，上传结果为数字
		dapd.setCertType(cust.getIdTypeName());// CERT_TYPE 证件类型：客户入网时所使用的证件名称，如：居民身份证、护照、营业执照、医疗卫生机构许可证等，取不到数时用-1
		saveCompInfo(dapd, order, compInfo);
		int num = dapdDao.insertDapdSheetInfo(dapd);
		log.info("insertDapdSheetInfo num: {}", num);
		setAcceptTemplate(sheetIdProv, saves);
	}

	private void saveCompInfo(DapdSheetInfo dapd, OrderAskInfo order, ComplaintInfo compInfo) {
		if (null != compInfo && StaticData.SERV_TYPE_NEWTS == order.getServType()
				&& (order.getAskChannelId() == 707907026 || order.getAskChannelId() == 707907027)) {// 工信部、省管局
			dapd.setCompUserName(dapd.getCustName());// COMP_USER_NAME 申诉用户姓名：客户提交申诉工单留存的姓名，省内结合集团客服工单系统或工信部/管局申诉系统下发工单中带的申诉用户姓名作关联
			setCompInfo(dapd, compInfo);
		}
	}

	private void setCompInfo(DapdSheetInfo dapd, ComplaintInfo compInfo) {
		dapd.setSheetIdComp(compInfo.getMiitCode());// SHEET_ID_COMP 申诉工单编号：由集团二级平台下发到各省，客服系统将工信部申诉工单转省内系统时，带入工信部部编号
		dapd.setCompDate(compInfo.getComplaintDate());// COMP_DATE 申诉日期：客户提交申诉工单的时间，省内结合集团客服工单系统或工信部/管局申诉系统下发工单中带的申诉日期作关联，时间格式为YYYYMMDDhhmmss
		dapd.setClaimSorc(compInfo.getComplaintSource());// CLAIM_SORC 申告来源：置空，该字段不再上传
		dapd.setCompIp(compInfo.getIpAddress());// COMP_IP 申诉IP地址：
		dapd.setCompStatus("4");// COMP_STATUS 申诉状态：0办结归档/1并案/2不受理归档/3撤诉/4处理中/5调解办结/6调解归档/7关闭
	}

	public void modifyDapd(OrderAskInfo order, OrderCustomerInfo cust, ServiceContent content, ServiceContentSave[] saves, String acceptContent) {
		String sheetIdProv = order.getServOrderId();
		DapdSheetInfo dapd = dapdDao.selectDapdSheetInfoBySheetIdProv(sheetIdProv);
		if (null == dapd) {
			return;
		}
		dapd.setPrvnceId(getPrvnceId(o2i(order.getRegionId())));// PRVNCE_ID 省份标识：客户来电反馈问题对应的业务号码的所属省份，参见公共主数据附录：地域编码，填写省份层级取值，非电信业务号码和非本省号码传-1
		dapd.setLatnId(getLatnId(o2i(order.getRegionId())));// LATN_ID 本地网标识：客户来电反馈问题对应的业务号码的所属本地网，参见公共主数据附录：地域编码，填写本地网层级取值，非电信业务号码和非本省号码传-1
		dapd.setReginId(z2n(o2i(order.getAreaId())));// REGIN_ID 区县标识：客户来电反馈问题对应的业务号码的所属区县，参考2.9.2.2地域维表第三层电信区域ID，非电信业务号码和非本省号码传-1
		dapd.setSubGridId(z2n(o2i(order.getSubStationId())));// SUB_GRID_ID 支局标识：可与产品实例表中的SUB_GRID_ID保持一致，代表区县下一层级，非电信业务号码和非本省号码传-1
		dapd.setCaller(StringUtils.defaultIfEmpty(order.getSourceNum(), order.getRelaInfo()));// CALLER 主叫号码：客服系统关联带入
		dapd.setBusiNbr(order.getProdNum());// BUSI_NBR 业务号码：客户主诉号码、问题号码，可根据客服系统关联或客户原声的智能打标结果带出，可由人工点选实际号码
		dapd.setProdInstId(cust.getProdInstId());// PROD_INST_ID 产品实例ID：业务号码对应产品实例id，可与产品实例表数据关联，非电信业务号码和非本省号码传-1
		dapd.setCustId(cust.getCrmCustId());// CUST_ID 客户ID：业务号码对应客户id，非电信业务号码和非本省号码传-1（与DAPD_CUST_INFO中的CUST_ID相同）
		dapd.setContactNo1(order.getRelaInfo());// CONTACT_NO1 联系电话1：客服系统关联带入，默认主叫号码
		dapd.setCmplntDesc(acceptContent);// CMPLNT_DESC 投/申诉内容：用户投、申诉内容
		setCmplntSource(dapd, order);
		int bestOrder = o2i(content.getBestOrder());
		if (bestOrder > 100122410) {
			dapd.setStrictestSheetId("1");// STRICTEST_SHEET_ID 是否市场最严工单：0否，1是，-1未知
			dapd.setStrictestSheetType(getStrictestSheetType(bestOrder));// STRICTEST_SHEET_TYPE 市场最严工单场景：0：业务退订、1：套餐变更、2：销户退网、3：携号转网、4：靓号低消、5：费用争议、-1：非市场最严工单，分类上传-1
		} else {
			dapd.setStrictestSheetId("0");
			dapd.setStrictestSheetType("-1");
		}
		dapd.setCmplntAprLevel1Nm(content.getAppealProdName());// CMPLNT_APR_LEVEL_1_NM 投诉现象一级：投诉现象一级名称，客户原声的智能打标结果，或人工点选的客户投诉现象，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		dapd.setCmplntAprLevel2Nm(content.getAppealReasonDesc());// CMPLNT_APR_LEVEL_2_NM 投诉现象二级：投诉现象二级名称，客户原声的智能打标结果，或人工点选的客户投诉现象，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		dapd.setCmplntAprLevel1(z2n(o2i(content.getAppealProdId())));// CMPLNT_APR_LEVEL_1 投诉现象一级编码：投诉现象编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		dapd.setCmplntAprLevel2(z2n(o2i(content.getAppealReasonId())));// CMPLNT_APR_LEVEL_2 投诉现象二级编码：投诉现象编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		int cmplntAprLevel3 = o2i(content.getAppealChild());
		if (cmplntAprLevel3 > 0) {
			dapd.setCmplntAprLevel3Nm(content.getAppealChildDesc());// CMPLNT_APR_LEVEL_3_NM 投诉现象三级：投诉现象三级名称，客户原声的智能打标结果，或人工点选的客户投诉现象，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
			dapd.setCmplntAprLevel3(z2n(cmplntAprLevel3));// CMPLNT_APR_LEVEL_3 投诉现象三级编码：投诉现象编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-现象。
		}
		dapd.setCmplntProdLevel1Nm(content.getProdOneDesc());// CMPLNT_PROD_LEVEL_1_NM 产品一级：投诉产品名称，客户原声的智能打标结果，或人工点选的客户投诉产品，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-产品。
		dapd.setCmplntProdLevel2Nm(content.getProdTwoDesc());// CMPLNT_PROD_LEVEL_2_NM 产品二级：投诉产品名称，客户原声的智能打标结果，或人工点选的客户投诉产品，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-产品。
		dapd.setCmplntProdLevel1(z2n(o2i(content.getProdOne())));// CMPLNT_PROD_LEVEL_1 产品一级编码：投诉产品编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-产品。
		dapd.setCmplntProdLevel2(z2n(o2i(content.getProdTwo())));// CMPLNT_PROD_LEVEL_2 产品二级编码：投诉产品编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.307投诉和查询工单-产品。
		dapd.setDvlpChnlNm(content.getDvlpChnlNm());// DVLP_CHNL_NM 发展渠道：发展渠道名称，资产入网时的受理渠道（渠道视图中的渠道单元名称），渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		dapd.setDvlpChnl(content.getDvlpChnl());// DVLP_CHNL 发展渠道编码：渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		int dvlpChnlLevel1 = o2i(content.getDevtChsOne());
		if (dvlpChnlLevel1 > 0) {
			dapd.setDvlpChnlLevel1Nm(content.getDevtChsOneDesc());// DVLP_CHNL_LEVEL_1_NM 发展渠道一级：发展渠道一级名称，发展渠道对应集团统一视图的分类层级，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
			dapd.setDvlpChnlLevel1(z2n(dvlpChnlLevel1));// DVLP_CHNL_LEVEL_1 发展渠道一级编码：发展渠道一级编码，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		}
		int dvlpChnlLevel2 = o2i(content.getDevtChsTwo());
		if (dvlpChnlLevel2 > 0) {
			dapd.setDvlpChnlLevel2Nm(content.getDevtChsTwoDesc());// DVLP_CHNL_LEVEL_2_NM 发展渠道二级：发展渠道二级名称，发展渠道对应集团统一视图的分类层级，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
			dapd.setDvlpChnlLevel2(z2n(dvlpChnlLevel2));// DVLP_CHNL_LEVEL_2 发展渠道二级编码：发展渠道二级编码，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		}
		int dvlpChnlLevel3 = o2i(content.getDevtChsThree());
		if (dvlpChnlLevel3 > 0) {
			dapd.setDvlpChnlLevel3Nm(content.getDevtChsThreeDesc());// DVLP_CHNL_LEVEL_3_NM 发展渠道三级：发展渠道三级名称，发展渠道对应集团统一视图的分类层级，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
			dapd.setDvlpChnlLevel3(z2n(dvlpChnlLevel3));// DVLP_CHNL_LEVEL_3 发展渠道三级编码：发展渠道三级编码，渠道统一视图，参照渠道视图信息下发接口协议3.1.1CHANNEL_TYPE_CD
		}
		dapd.setCustType(order.getCustGroupDesc());// CUST_TYPE 客户类型：号码类型分类
		dapd.setMemberLevel(cust.getCustServGradeName());// MEMBER_LEVEL 用户星级：业务号码的星级等级，结合星级管理办法、省内系统对用户号码的星级标识获取结果，结果如：0星/1星/2星/3星/4星黄金/5星白金/6星黑金/7星钻石
		dapd.setServAge(getServAge(cust.getInstalldate()));// SERV_AGE 网龄：号码入网时长，按月取数，向下取整
		dapd.setAge(z2n(o2i(cust.getCustAge())));// AGE 用户年龄：客户年龄
		dapd.setDisputeChnlNm(content.getDisputeChnlNm());// DISPUTE_CHNL_NM 争议渠道：与客诉相关的销售品受理渠道或服务问题发生渠道
		dapd.setDisputeChnl(content.getDisputeChnl());// DISPUTE_CHNL 争议渠道编码：争议渠道编码
		dapd.setDisputeChnl1Nm(content.getDisputeChnl1Nm()); // DISPUTE_CHNL_1_NM 争议渠道一级：争议渠道对应集团统一视图的分类层级
		dapd.setDisputeChnl1(z2n(content.getDisputeChnl1())); // DISPUTE_CHNL_1 争议渠道一级编码：争议渠道一级编码
		dapd.setDisputeChnl2Nm(content.getDisputeChnl2Nm()); // DISPUTE_CHNL_2_NM 争议渠道二级：争议渠道对应集团统一视图的分类层级
		dapd.setDisputeChnl2(z2n(content.getDisputeChnl2())); // DISPUTE_CHNL_2 争议渠道二级编码：争议渠道二级编码
		dapd.setDisputeChnl3Nm(content.getDisputeChnl3Nm()); // DISPUTE_CHNL_3_NM 争议渠道三级：争议渠道对应集团统一视图的分类层级
		dapd.setDisputeChnl3(z2n(content.getDisputeChnl3())); // DISPUTE_CHNL_3 争议渠道三级编码：争议渠道三级编码
		dapd.setRealNameStatus(cust.getIsRealname());// REAL_NAME_STATUS 实名状态：CRM系统中用户实名状态：0否，1是
		dapd.setResConChannel(getResConChannel(order));// RES_CON_CHANNEL 责任管控渠道：通过资产落地归属获取，资产落地网格的组织信息（例如*省分公司*市分公司-*分局-*支局-*网格）
		dapd.setNbrLocation(cust.getBranchNo());//NBR_LOCATION 号码归属地：根据业务号码关联归属地信息，在客服系统建单时关联带入，示例：成都
		dapd.setCustName(StringUtils.defaultIfEmpty(cust.getCustName(), order.getRelaMan()));// CUST_NAME 客户名称：CRM系统登记的业务号码的客户名称，系统自动获取CRM登记信息，示例：张三
		dapd.setCertType(cust.getIdTypeName());// CERT_TYPE 证件类型：客户入网时所使用的证件名称，如：居民身份证、护照、营业执照、医疗卫生机构许可证等，取不到数时用-1
		dapdDao.updateDapdSheet(dapd);
		setAcceptTemplate(sheetIdProv, saves);
	}

	private String o2s(Object obj) {
		return null == obj ? "" : obj.toString();
	}

	private int o2i(Integer obj) {
		if (null != obj) {
			try {
				return obj.intValue();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	private String z2n(int zero) {
		return 0 == zero ? null : String.valueOf(zero);
	}

	private String z2n(String zero) {
		return (StringUtils.isBlank(zero) || "0".equals(zero)) ? null : zero;
	}

	private String getPrvnceId(int regionId) {
		return 999 == regionId ? null : "832";
	}

	private String getLatnId(int regionId) {
		switch (regionId) {
		case 3:
			return "83201";
		case 4:
			return "83211";
		case 15:
			return "83202";
		case 20:
			return "83205";
		case 26:
			return "83206";
		case 33:
			return "83210";
		case 39:
			return "83209";
		case 48:
			return "83203";
		case 60:
			return "83208";
		case 63:
			return "83207";
		case 69:
			return "83204";
		case 79:
			return "83212";
		case 84:
			return "83213";
		default:
			return null;
		}
	}

	private String getRecordNo(String callSerialNo) {
		return "N".equals(callSerialNo) ? null : callSerialNo;
	}

	// 2025-2，增加四类来源映射
	private void setCmplntSource(DapdSheetInfo dapd, OrderAskInfo order) {
		String cmplntSourceCd = "LY010101";// 默认来源
		String cmplntSource = "10000号语音坐席";// 默认来源
		String lastChannel = String.valueOf(pubFun.getLastChannel(order));
		Map map = pubFun.getCmpDataMapDstCode(lastChannel, "1", "CC_SERVICE_ORDER_ASK-CMPLNT_SOURCE");
		if (!map.isEmpty()) {
			String dstCode = map.get("DST_CODE") == null ? "" : map.get("DST_CODE").toString();
			String dstName = map.get("DST_NAME") == null ? "" : map.get("DST_NAME").toString();
			if (!"".equals(dstCode) && !"".equals(dstName)) {
				cmplntSourceCd = dstCode;
				cmplntSource = dstName;
			}
		}
		dapd.setCmplntSourceCd(cmplntSourceCd);// CMPLNT_SOURCE_CD 工单来源编码：工单来源来源二级对应的编码，示例：LY010101
		dapd.setCmplntSource(cmplntSource);// CMPLNT_SOURCE 工单来源：投诉来源字段名称更改为：工单来源。建单时选定的客户投诉的来源，包括所有来源可选，参照集团2024年9月迭代版客服运营规范工单来源的分类，取来源二级，示例：10000号语音坐席
	}

	private String getStrictestSheetType(int bestOrder) {
		switch (bestOrder) {
		case 100122411:// 1：套餐变更
			return "1";
		case 100122412:// 2：销户退网
			return "2";
		case 100122413:// 3：携号转网
			return "3";
		case 100122414:// 0：业务退订
			return "0";
		case 100122415:// 4：靓号低消
			return "4";
		case 100122416:// 5：费用争议
			return "5";
		default:// -1：非市场最严工单
			return "-1";
		}
	}

	private String getSheetEstima30days(int unsatisfyNum, int satisfyNum) {
		if (unsatisfyNum > 0) {
			return "不满意";
		} else if (satisfyNum > 0) {
			return "满意";
		} else {
			return null;
		}
	}

	private String getUpgradeTrend(int upTendencyFlag) {
		switch (upTendencyFlag) {
		case 1:
			return "有曝光倾向";
		case 2:
			return "有工信部越级倾向或通管局投诉倾向";
		case 3:
			return "有越级倾向";
		default:
			return null;
		}
	}

	private String getServAge(String installdate) {
		if (StringUtils.isEmpty(installdate)) {
			return null;
		}
		Date install = null;
		try {
			install = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(installdate);
		} catch (Exception e) {
			return null;
		}
		Calendar begin = Calendar.getInstance();
		begin.setTime(install);
		Calendar end = Calendar.getInstance();
		end.setTime(new Date());
		int year = end.get(Calendar.YEAR) - begin.get(Calendar.YEAR);
		return String.valueOf((end.get(Calendar.MONTH) - begin.get(Calendar.MONTH)) + year * 12);
	}

	private String gtz2y(int greaterThanZero) {
		return greaterThanZero > 0 ? "是" : "否";
	}

	private String wu2n(Object obj) {
		if (null != obj) {
			if ("无".equals(obj.toString())) {
				return null;
			}
			return obj.toString();
		}
		return null;
	}

	// 资产落地网格的组织信息（例如*省分公司-*市分公司-*分局-*支局-*网格）
	private String getResConChannel(OrderAskInfo order) {
		if (999 == order.getRegionId()) {
			return order.getRegionName();
		}
		String resConChannel = "江苏省分公司";
		if (2 == order.getRegionId()) {
			return resConChannel;
		}
		if (order.getRegionName().endsWith("市")) {
			resConChannel += "-" + order.getRegionName() + "分公司";
		} else {
			resConChannel += "-" + order.getRegionName() + "市分公司";
		}
		if (!"".equals(order.getAreaName())) {
			resConChannel += "-" + order.getAreaName();
		}
		if (!"".equals(order.getSubStationName())) {
			resConChannel += "-" + order.getSubStationName();
		}
		return resConChannel;
	}

	private String getSheetType(int servType) {
		return StaticData.SERV_TYPE_NEWTS == servType ? "投诉单" : "查询单";
	}

	private void setAcceptTemplate(String sheetIdProv, ServiceContentSave[] saves) {
		if (null == saves || saves.length == 0) {
			return;
		}
		JSONObject obj = new JSONObject();
		for (ServiceContentSave save : saves) {
			obj.put(save.getAliasName(), save.getAnswerName());
		}
		DapdSheetInfo dapd = JSON.parseObject(obj.toJSONString(), DapdSheetInfo.class);
		dapd.setMainOfferId(wu2n(dapd.getMainOfferId()));// MAIN_OFFER_ID 主套餐ID：CRM定价ID
		dapd.setMainOfferNm(wu2n(dapd.getMainOfferNm()));// MAIN_OFFER_NM 主套餐名称：主销售品名称
		dapd.setSheetIdProv(sheetIdProv);
		int num = dapdDao.updateAcceptTemplate(dapd);
		log.info("updateAcceptTemplate num: {}", num);
	}

	public void modifyDapdFlag(ServiceLabel label, CustomerPersona persona) {
		DapdSheetInfo dapd = dapdDao.selectDapdSheetInfoBySheetIdProv(label.getServiceOrderId());
		if (null == dapd) {
			return;
		}
		dapd.setRecmplntTimes30days(z2n(o2i(label.getRepeatNewFlag())));// RECMPLNT_TIMES_30DAYS 30天重复投诉次数：根据投诉号码关联最近30天用户是否有重复工单记录，并展示次数
		dapd.setCroscmplntTimes30days(z2n(o2i(persona.getUpTendencyNum())));// CROSCMPLNT_TIMES_30DAYS 30天越级投诉次数：根据投诉号码关联最近30天用户是否有越级工单记录，并展示次数
		dapd.setSheetEstima30days(getSheetEstima30days(o2i(persona.getUnsatisfyNum()), o2i(persona.getSatisfyNum())));// SHEET_ESTIMA_30DAYS 30天工单评价：置空，该字段不再上传
		dapd.setSheetEstima30daysSat(z2n(o2i(persona.getSatisfyNum())));// SHEET_ESTIMA_30DAYS_SAT 30天工单评价-满意：根据投诉号码关联最近30天用户历史投诉工单的满意度测评结果为“满意”的次数，上传结果为数字
		dapd.setSheetEstima30daysDissat(z2n(o2i(persona.getUnsatisfyNum())));// SHEET_ESTIMA_30DAYS_DISSAT 30天工单评价-不满意：根据投诉号码关联最近30天用户历史投诉工单的满意度测评结果为“不满意”的次数，上传结果为数字
		dapdDao.updateDapdFlag(dapd);
	}

	public void modifyDapdComp(ComplaintInfo compInfo) {
		DapdSheetInfo dapd = dapdDao.selectDapdSheetInfoBySheetIdProv(compInfo.getOrderId());
		if (null == dapd) {
			return;
		}
		setCompInfo(dapd, compInfo);
		dapdDao.updateCompInfo(dapd);
	}

	public void setDealTemplate(DapdSheetInfo dapd) {
		String isSlv = o2s(dapd.getIsSlv());
		if (isSlv.startsWith("已解决")) {
			dapd.setIsSlv("1");// IS_SLV 是否解决：置空，该字段不再上传，0否，1是，-1未知，客户是否认可作为是否解决的标准
		} else if (isSlv.startsWith("未解决")) {
			dapd.setIsSlv("0");
		} else {
			dapd.setIsSlv("-1");
		}
		dapdDao.updateDealTemplate(dapd);
	}

	// 预定性终定性提交
	public void setDapdEndDate(String orderId) {
		DapdSheetInfo dapd = dapdDao.selectDapdSheetInfoBySheetIdProv(orderId);
		if (null == dapd) {
			return;
		}
		if (!"".equals(o2s(dapd.getEndDate()))) {
			return;
		}
		List qList = qualitativeDao.getOrderQualitative(orderId);
		if (qList.isEmpty()) {
			return;
		}
		Map cmap = orderDao.getComplaintInfo(orderId, false);
		if (!cmap.isEmpty()) {
			dapd.setSheetIdComp(o2s(cmap.get("MIIT_CODE")));// SHEET_ID_COMP 申诉工单编号：由集团二级平台下发到各省，客服系统将工信部申诉工单转省内系统时，带入工信部部编号
			dapd.setCompDate(o2s(cmap.get("COMPLAINT_DATE")));// COMP_DATE 申诉日期：客户提交申诉工单的时间，省内结合集团客服工单系统或工信部/管局申诉系统下发工单中带的申诉日期作关联，时间格式为YYYYMMDDhhmmss
			dapd.setCompUserName(dapd.getCustName());// COMP_USER_NAME 申诉用户姓名：客户提交申诉工单留存的姓名，省内结合集团客服工单系统或工信部/管局申诉系统下发工单中带的申诉用户姓名作关联
			dapd.setIsValetCmplnt(parseIntNumber(o2s(cmap.get("IS_AGENT_COMPLAINT"))));// IS_VALET_CMPLNT 是否代客：0否，1是，-1未知
			dapd.setValetCmplntMap(o2s(cmap.get("VALET_CMPLNT_MAP")));// VALET_CMPLNT_MAP 代客场景：工信部323、324号文件内代客场景
			dapd.setClaimSorc(o2s(cmap.get("COMPLAINT_SOURCE")));// CLAIM_SORC 申告来源：置空，该字段不再上传
			dapd.setSpclClaimType(o2s(cmap.get("SPCL_CLAIM_TYPE")));// SPCL_CLAIM_TYPE 特定申告分类：特定申告场景
			dapd.setIsSameClaimType(parseIntNumber(o2s(cmap.get("IS_SAME_CLAIM_TYPE"))));// IS_SAME_CLAIM_TYPE 是否模板化申告：0否，1是，-1未知，模板化申告指在同一统计周期内，不同用户采用相同模板就相同事项进行申告。根据用户提交的书面材料，申告内容模板化明显的，可认定为模板化申告。
			dapd.setCompIp(o2s(cmap.get("IP_ADDRESS")));// COMP_IP 申诉IP地址：
			dapd.setCompStatus("4");// COMP_STATUS 申诉状态：0办结归档/1并案/2不受理归档/3撤诉/4处理中/5调解办结/6调解归档/7关闭
		}
		Map cMap = trkDao.getLastRecallByOrderId(orderId);
		if (!cMap.isEmpty()) {
			dapd.setRecallRecordNo(o2s(cMap.get("CALL_GUID")));// RECALL_RECORD_NO 回复录音流水号：处理人员回复用户外呼的最后一条流水号
			dapd.setRecallRecord(o2s(cMap.get("CALL_ID")));// RECALL_RECORD 回复录音：置空，该字段不再上传;处理人员回复用户外呼的最后一条录音文件
			dapd.setRecallTime(o2s(cMap.get("CALL_ANSWER")));// RECALL_TIME 回复时间：处理人员回复用户外呼的时间
			dapd.setRecallStaffId(o2s(cMap.get("AGENT_ID")));// RECALL_STAFF_ID 回复人员工号：处理人员回复用户外呼的工号，客服系统工号
		}
		if ("".equals(o2s(dapd.getReason()))) {
			ServiceContent content = contentDao.getServContentByOrderId(orderId, false, 0);
			dapd.setReason(getDefaultReason(content));// REASON 问题原因：置空，该字段不再上传;用户反映的问题
		}
		Map qMap = (Map) qList.get(qList.size() - 1);
		long compDurMS = getCompDurMS(dapd.getCreateDate());
		if (compDurMS > 48 * 3600 * 1000) {
			dapd.setIsTimeout("1");// IS_TIMEOUT 是否超时：0否，1是，-1未知，“处理时限”超48小时则视为超时
			dapd.setTimeoutRsn(o2s(qMap.get("OVERTIME_REASON_DESC")));// TIMEOUT_RSN 超时原因：本省根据省内实际情况对集中的超时原因作分类供员工点选打标，除分类以外的手工填写（原因描述要清晰简洁），分类如：未联系上用户/工单量激增处理人员不足/用户原因另行约定处理时间/退费审批时间长 /解约审批时间长
		} else {
			dapd.setIsTimeout("0");
		}
		if ("".equals(o2s(dapd.getIsSlv()))) {
			dapd.setIsSlv("-1");// IS_SLV 是否解决：置空，该字段不再上传，0否，1是，-1未知，客户是否认可作为是否解决的标准
		}
		dapd.setIsRaiseFinish(forceDao.selectForceStaffByOrderId(orderId) > 0 ? "1" : "0");// IS_RAISE_FINISH 是否提级办结：0否，1是，-1未知
		dapd.setRsnLevel1Nm(o2s(qMap.get("TS_KEY_WORD_DESC")));// RSN_LEVEL_1_NM 原因一级：原因一级名称，内部核查后的结果定性，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
		dapd.setRsnLevel1(o2s(qMap.get("TS_KEY_WORD")));// RSN_LEVEL_1 原因一级编码：原因一级编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
		dapd.setRsnLevel2Nm(o2s(qMap.get("SUB_KEY_WORD_DESC")));// RSN_LEVEL_2_NM 原因二级：原因二级名称，内部核查后的结果定性，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
		dapd.setRsnLevel2(o2s(qMap.get("SUB_KEY_WORD")));// RSN_LEVEL_2 原因二级编码：原因二级编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
		dapd.setRsnLevel3Nm(o2s(qMap.get("THREE_GRADE_CATALOG_DESC")));// RSN_LEVEL_3_NM 原因三级：原因三级名称，内部核查后的结果定性，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
		dapd.setRsnLevel3(o2s(qMap.get("THREE_GRADE_CATALOG")));// RSN_LEVEL_3 原因三级编码：原因三级编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
		String rsnLevel4 = o2s(qMap.get("FOUR_GRADE_CATALOG"));
		if (!"0".equals(rsnLevel4)) {
			dapd.setRsnLevel4Nm(o2s(qMap.get("FOUR_GRADE_CATALOG_DESC")));// RSN_LEVEL_4_NM 原因四级：原因四级名称，内部核查后的结果定性，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
			dapd.setRsnLevel4(rsnLevel4);// RSN_LEVEL_4 原因四级编码：原因四级编码，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
		}
		String rsnLevel5 = o2s(qMap.get("FIVE_GRADE_CATALOG"));
		if (!"0".equals(rsnLevel5)) {
			dapd.setRsnLevel5Nm(o2s(qMap.get("FIVE_GRADE_CATALOG_DESC")));// RSN_LEVEL_5_NM 原因五级：原因五级名称，内部核查后的结果定性，按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
			dapd.setRsnLevel5(rsnLevel5);// RSN_LEVEL_5 原因五级编码：原因五级编码（编码规则从四级编码新增“01”开始），按照集团客服下发的万号服务四类工单分类字典的标签规范上传，详见公共主数据分册2.308投诉和查询工单-原因。
		}
		SheetPubInfo sheet = sheetDao.getSheetPubInfo(qMap.get("WORK_SHEET_ID").toString(), false);
		dapd.setCompDepart(sheet.getDealOrgName());// COMP_DEPART 办结单位：省内工单办结的部门，示例：省中心本级处理组
		dapd.setCompConc(getCompConc(orderId, o2s(dapd.getIsSlv()), o2s(dapd.getUnrslvRsn())));// COMP_CONC 问题是否解决：例如：客户问题已解决、客户问题未解决-联系不上用户、客户问题未解决-用户不愿履约、客户问题未解决-用户不配合办理手续、客户问题未解决-用户要求过高、客户问题未解决-用户另行预约时间、重单 等
		dapd.setCompDur(String.valueOf(new BigDecimal(compDurMS / 1000)));// COMP_DUR 处理时长：单位：秒
		String outletsName = o2s(qMap.get("OUTLETS_NAME"));
		if (!"".equals(outletsName)) {
			dapd.setIfPunish("1");// IF_PUNISH 是否有责扣罚：0否，1是
			// PUNISH_MOUNT 扣罚金额：
			dapd.setPunishDepart(outletsName);// PUNISH_DEPART 扣罚责任部门：
		}
		dapd.setIsFairComplaint(parseIntNumber(o2s(qMap.get("IS_REASONABLE"))));// IS_FAIR_COMPLAINT 客户投诉是否合理：0否，1是，-1未知
		dapd.setActionFlag(getActionFlag(orderId));// ACTION_FLAG 专项行动标识：1新羽行动，2营业服务，如果命中多个标签，全部上传，用逗号分隔
		dapd.setArchiveCtnt(pubFun.getLastDealContent(orderId));// ARCHIVE_CTNT 归档内容：置空，该字段不再上传
		dapd.setResQuality(o2s(qMap.get("CONTROL_AREA_FIR_DESC")));// RES_QUALITY 责任定性：根据工单核查及处理结果判断，结果值：企业有责/企业无责
		dapdDao.updateDapdEndDate(dapd);
	}

	// 取现象二级，如为省自定，则带入现象三级
	private String getDefaultReason(ServiceContent content) {
		if (0 != content.getAppealChild()) {
			return content.getAppealChildDesc();
		}
		return content.getAppealReasonDesc();
	}

	private long getCompDurMS(String createDate) {
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date bDate = sdf1.parse(createDate);
			Date eDate = new Date();
			long bTime = bDate.getTime();
			long eTime = eDate.getTime();
			return eTime - bTime;
		} catch (Exception e) {
			return 0;
		}
	}

	private String parseIntNumber(String isValetCmplnt) {
		if ("1".equals(isValetCmplnt) || "0".equals(isValetCmplnt)) {
			return isValetCmplnt;
		} else {
			return "-1";
		}
	}

	// 办结结果：例如：客户问题已解决、客户问题未解决-联系不上用户、客户问题未解决-用户不愿履约、客户问题未解决-用户不配合办理手续、客户问题未解决-用户要求过高、客户问题未解决-用户另行预约时间、重单等
	private String getCompConc(String orderId, String isSlv, String unrslvRsn) {
		ComplaintUnifiedRepeat cur = unifiedRepeatDao.queryUnifiedRepeatByCurSoi(orderId, "1");
		if (null != cur) {
			return "重单";
		}
		if ("1".equals(isSlv)) {
			return "客户问题已解决";
		} else if ("0".equals(isSlv)) {
			return "客户问题未解决-" + unrslvRsn;
		}
		return "未知";
	}

	private String getActionFlag(String orderId) {
		String actionFlag = "";
		List xys = instanceDao.getLabelInstanceByLabelId(orderId, "038b7fccd41c11ed9490005056ae8349");
		if (!xys.isEmpty()) {
			actionFlag = "1";
		}
		return actionFlag;
	}

	// 客户满意评测
	public void setDapdSatEval(String orderId, String satEval) {
		DapdSheetInfo dapd = dapdDao.selectDapdSheetInfoBySheetIdProv(orderId);
		if (null == dapd) {
			return;
		}
		dapd.setSatEval(satEval);// SAT_EVAL 客户满意测评：集团对工单办结后即时测评的结果，取集团大数据记录结果，结果值为：0代表未回访、1代表用户未参评、2代表满意、3代表不满意
		dapdDao.updateDapdSatEval(dapd);
	}

	// 归档
	public void setDapdArchiveDate(String orderId) {
		setDapdEndDate(orderId);
		DapdSheetInfo dapd = dapdDao.selectDapdSheetInfoBySheetIdProv(orderId);
		if (null == dapd) {
			return;
		}
		if (!"".equals(o2s(dapd.getArchiveDate()))) {
			return;
		}
		ServiceContent content = contentDao.getServContentByOrderId(orderId, false, 0);
		int bestOrder = o2i(content.getBestOrder());
		if (bestOrder > 100122410) {
			dapd.setStrictestSheetId("1");// STRICTEST_SHEET_ID 是否市场最严工单：0否，1是，-1未知
			dapd.setStrictestSheetType(getStrictestSheetType(bestOrder));// STRICTEST_SHEET_TYPE 市场最严工单场景：0：业务退订、1：套餐变更、2：销户退网、3：携号转网、4：靓号低消、5：费用争议、-1：非市场最严工单，分类上传-1
		} else {
			dapd.setStrictestSheetId("0");
			dapd.setStrictestSheetType("-1");
		}
		if ("".equals(o2s(dapd.getSatEval()))) {
			dapd.setSatEval("0");// SAT_EVAL 客户满意测评：集团对工单办结后即时测评的结果，取集团大数据记录结果，结果值为：0代表未回访、1代表用户未参评、2代表满意、3代表不满意
		}
		Map cmap = orderDao.getComplaintInfo(orderId, false);
		if (!cmap.isEmpty()) {
			dapd.setSheetIdComp(o2s(cmap.get("MIIT_CODE")));// SHEET_ID_COMP 申诉工单编号：由集团二级平台下发到各省，客服系统将工信部申诉工单转省内系统时，带入工信部部编号
			dapd.setCompDate(o2s(cmap.get("COMPLAINT_DATE")));// COMP_DATE 申诉日期：客户提交申诉工单的时间，省内结合集团客服工单系统或工信部/管局申诉系统下发工单中带的申诉日期作关联，时间格式为YYYYMMDDhhmmss
			dapd.setCompUserName(dapd.getCustName());// COMP_USER_NAME 申诉用户姓名：客户提交申诉工单留存的姓名，省内结合集团客服工单系统或工信部/管局申诉系统下发工单中带的申诉用户姓名作关联
			dapd.setIsValetCmplnt(parseIntNumber(o2s(cmap.get("IS_AGENT_COMPLAINT"))));// IS_VALET_CMPLNT 是否代客：0否，1是，-1未知
			dapd.setValetCmplntMap(o2s(cmap.get("VALET_CMPLNT_MAP")));// VALET_CMPLNT_MAP 代客场景：工信部323、324号文件内代客场景
			dapd.setClaimSorc(o2s(cmap.get("COMPLAINT_SOURCE")));// CLAIM_SORC 申告来源：置空，该字段不再上传
			dapd.setSpclClaimType(o2s(cmap.get("SPCL_CLAIM_TYPE")));// SPCL_CLAIM_TYPE 特定申告分类：特定申告场景
			dapd.setIsSameClaimType(parseIntNumber(o2s(cmap.get("IS_SAME_CLAIM_TYPE"))));// IS_SAME_CLAIM_TYPE 是否模板化申告：0否，1是，-1未知，模板化申告指在同一统计周期内，不同用户采用相同模板就相同事项进行申告。根据用户提交的书面材料，申告内容模板化明显的，可认定为模板化申告。
			dapd.setCompIp(o2s(cmap.get("IP_ADDRESS")));// COMP_IP 申诉IP地址：
			dapd.setCompStatus("0");// COMP_STATUS 申诉状态：0办结归档/1并案/2不受理归档/3撤诉/4处理中/5调解办结/6调解归档/7关闭
		}
		dapd.setUrgeSheetCnt(getUrgeSheetCnt(orderId));// URGE_SHEET_CNT 催单次数：置空，该字段不再上传
		dapd.setRedirctTimes(String.valueOf(pubFun.getRedirctTimes(orderId)));// REDIRCT_TIMES 转派次数：置空，该字段不再上传
		dapdDao.updateDapdArchiveDate(dapd);
	}

	private String getUrgeSheetCnt(String orderId) {
		List list = trkDao.findOrderHasten(orderId, true);
		if (list.isEmpty()) {
			return "0";
		}
		return String.valueOf(list.size());
	}

	public DapdSheetInfo getDapdSheetInfo(String orderId) {
		return dapdDao.selectDapdSheetInfoBySheetIdProv(orderId);
	}
}