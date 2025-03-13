/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.cliqueWorkSheetWebService.pojo.ComplaintUnifiedReturn;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.handler.ComplaintDealHandler;
import com.timesontransfar.complaintservice.service.IComplaint;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.common.message.pojo.MessagePrompt;
import com.timesontransfar.customservice.common.message.service.IMessageManager;
import com.timesontransfar.customservice.common.uploadFile.dao.IAccessorieDao;
import com.timesontransfar.customservice.dbgridData.ComplaintMaterialsService;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.labelmanage.service.ILabelManageService;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.CustomerPersona;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.service.impl.CompWorksheetFullWebService;
import com.timesontransfar.customservice.paramconfig.pojo.SheetLimitTimeCollocate;
import com.timesontransfar.customservice.paramconfig.service.IsheetLimitTimeService;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.customservice.worksheet.dao.IHastenSheetInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetMistakeDAO;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetTodispatchDao;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.dao.ItsCustomerVisit;
import com.timesontransfar.customservice.worksheet.dao.ItsDealQualitative;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetAuditing;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.dao.IworkSheetAllotRealDao;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedReturnDAOImpl;
import com.timesontransfar.customservice.worksheet.dao.cmpGroup.CmpUnifiedWeixinDAOImpl;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.ResponsiBilityOrg;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetOperation;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TSOrderMistake;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetAuditing;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetDealType;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitativeGrid;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetAllotReal;
import com.timesontransfar.customservice.worksheet.pojo.XcFlow;
import com.timesontransfar.customservice.worksheet.service.IWorkSheetFlowService;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.OrderRefundService;
import com.timesontransfar.dapd.service.IdapdSheetInfoService;
import com.timesontransfar.feign.ComplaintOrderFeign;
import com.timesontransfar.feign.clique.AccessCliqueServiceFeign;
import com.transfar.common.enums.ResultEnum;
import com.transfar.common.utils.StringUtils;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONObject;

@SuppressWarnings("all")
@Component("tsWorkSheetDeal__FACADE__")
public class TsWorkSheetDeal implements ItsWorkSheetDeal{
	private static final Logger logger = LoggerFactory.getLogger(TsWorkSheetDeal.class);
	
	@Autowired
	private IWorkSheetFlowService workSheetFlowService;
	@Autowired
	private IserviceContentSchem serviceContentSchem;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private IorderCustInfoDao orderCustInfoDao;
	@Autowired
	private IserviceContentDao servContentDao;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
	private ISheetActionInfoDao sheetActionInfoDao;
	@Autowired
	private IworkSheetAllotRealDao workSheetAlllot;
	@Autowired
	private InoteSenList noteSen;
	@Autowired
	private IsheetLimitTimeService sheetLimitTimeServ;//时限
	@Autowired
	private ItsWorkSheetDao	tsWorkSheetDao;//投诉DAO
	@Autowired
	private ItsDealQualitative dealQualitative;//部门处理定性表
	@Autowired
	private ItsSheetAuditing tsSheetAuditingDaoImpl;//审核表
	@Autowired
	private ItsCustomerVisit customerVisit;//投诉回访表
	@Autowired
	private ItsSheetQualitative sheetQualitative;//	投诉定性表
	@Autowired
	private IMessageManager messageManager;//投诉处理功能方法
	@Autowired
    private CmpUnifiedReturnDAOImpl cmpUnifiedReturnDAOImpl;
	@Autowired
    private CompWorksheetFullWebService compFull;
    @Autowired
    private CmpUnifiedWeixinDAOImpl clqUnifiedWeixinDAOImpl;
	@Autowired
	private IComplaint complaintImpl;
	@Autowired
	private ComplaintDealHandler complaintDealHandler;
	@Autowired
	private ComplaintOrderFeign compOrderFeign;
	
	/**
	 * 标签存储表的操作实例
	 */
	@Autowired
	private ILabelManageDAO labelManageDao;
    
    /**
     * 派发工单的操作实例
     */
	@Autowired
    private WorkSheetAllot workSheetAllot;
    
    /**
     * 催单信息表的操作实例
     */
    @Autowired
    private IHastenSheetInfoDao hastenDao;
    @Autowired
    private IAccessorieDao relateFileDAO;
    @Autowired
    private ISheetTodispatchDao todispatchDao;
    @Autowired
    private ILabelManageService labelManageService;
    @Autowired
    private ISheetMistakeDAO sheetMistakeDAO;
	@Autowired
	private AccessCliqueServiceFeign accessCliqueServiceFeign;
	@Autowired
	private IdapdSheetInfoService dapdSheetService;
	@Autowired
	private OrderRefundService refundService;
	@Autowired
	private ComplaintMaterialsService complaintMaterialsService;

	// 检验按钮是否显示
	public int checkPushButton(String orderId) {
		List shows = clqUnifiedWeixinDAOImpl.checknoAnswerByOrderId(orderId, 0);
		if (shows.isEmpty()) {
			return 1; // 不显示
		}
		return 0;
	}

	// 无人接听时（工单有无人接听按钮）两次触发间隔超过一小时
	public String noAnswerPush(String orderId) {
		logger.info("noAnswerPush orderId: {}", orderId);
		int checkButtonFlag = checkPushButton(orderId);
		logger.info("checkPushButton: {}", checkButtonFlag);
		if (1 == checkButtonFlag) {
			return "INTERVALTIME";
		}
		int checkTimeFlag = clqUnifiedWeixinDAOImpl.queryPushTime();
		logger.info("queryPushTime: {}", checkTimeFlag);
		if (1 == checkTimeFlag) {
			return "RESTTIME"; // 休息时间不推送
		}
		List shows = clqUnifiedWeixinDAOImpl.checknoAnswerByOrderId(orderId, 7);
		logger.info("checknoAnswerByOrderId: {}", JSON.toJSON(shows));
		if (!shows.isEmpty()) {
			Map show = (Map) shows.get(0);
			if ("1".equals(show.get("INTERVAL_FLAG").toString())) {
				return "INTERVALTIME"; // 间隔一小时
			}
		}
		return complaintImpl.complaintPostInfo(7, orderId);
	}

	public int getXcSheetCount(String sourceSheetId, String receiveOrgId, int xcType) {
		int res = 0;
		XcFlow[] xcFlows = sheetPubInfoDao.getXcFlowByMainId(sourceSheetId);
		for (XcFlow xcFlow : xcFlows) {
			if (1 == xcType) { // 省内协查
				if (receiveOrgId.equals(xcFlow.getCurReceiveOrg()) && 1 == xcFlow.getIsFinish() && xcType == xcFlow.getXcType()) {
					return 1;
				}
			} else {
				if (1 == xcFlow.getIsFinish() && xcType == xcFlow.getXcType()) {
					return 1;
				}
			}
		}
		logger.info("getXcSheetCount sourceSheetId:{},receiveOrgId:{},xcType:{},res:{}", sourceSheetId, receiveOrgId, xcType, res);
		return res;
	}

	/**
	 * 协查派单
	 * @param sheetPubInfos  派往部门对象
	 * @param penaltyMoney 违约金金额
	 * @return
	 */
    //@Transactional
	@Transactional(propagation = Propagation.REQUIRED)
	public String xcDispathSheet(SheetPubInfo[] sheetPubInfos, int xcType, String penaltyMoney) {
		logger.info("xcDispathSheet sheetPubInfos:{},xcType:{},penaltyMoney:{}", sheetPubInfos, xcType, penaltyMoney);
		// 取得原工单对象
		SheetPubInfo curSheet = sheetPubInfos[0];
		String oldSheetId = curSheet.getWorkSheetId();
		SheetPubInfo oldSheet = this.sheetPubInfoDao.getSheetObj(oldSheetId, curSheet.getRegionId(), curSheet.getMonth(), true);
		String orderId = oldSheet.getServiceOrderId();
		int servType = oldSheet.getServType();
		TsmStaff staff = pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String dealorgId = staff.getOrganizationId();
		String dealorgName = staff.getOrgName();

		SheetPubInfo newSheet = new SheetPubInfo();
		newSheet.setServiceOrderId(orderId);
		newSheet.setRegionId(oldSheet.getRegionId());
		newSheet.setRegionName(oldSheet.getRegionName());
		newSheet.setServType(servType);
		newSheet.setServTypeDesc(oldSheet.getServTypeDesc());
		newSheet.setSourceSheetId(oldSheetId);
		int tachId = getXcTachId(servType);
		newSheet.setTacheId(tachId);
		newSheet.setTacheDesc(pubFunc.getStaticName(tachId));
		newSheet.setWflInstId(oldSheet.getWflInstId());
		newSheet.setTacheInstId(oldSheet.getTacheInstId());
		String dealType = "协查工单";
		if (1 == xcType) { // 省内协查
			newSheet.setSheetType(StaticData.SHEET_TYPE_XC_SN);
			newSheet.setSheetTypeDesc(this.pubFunc.getStaticName(StaticData.SHEET_TYPE_XC_SN));
			newSheet.setSheetRcvDate(this.pubFunc.getSysDateFormat("%Y-%m-%d %H:%i:%s"));
			newSheet.setDealLimitTime(curSheet.getDealLimitTime());
			newSheet.setStationLimit(curSheet.getDealLimitTime());
			dealType = "省内协查派单";
		} else { // 集团协查
			newSheet.setSheetType(StaticData.SHEET_TYPE_XC_JT);
			newSheet.setSheetTypeDesc(this.pubFunc.getStaticName(StaticData.SHEET_TYPE_XC_JT));
			newSheet.setSheetRcvDate(sheetPubInfoDao.selectSheetReceiveDate(oldSheetId));
			int dealLimitTime = getJtxcDealLimitTime(oldSheet.getDealLimitTime());
			newSheet.setDealLimitTime(dealLimitTime);
			newSheet.setStationLimit(dealLimitTime);
			dealType = "集团协查派单";
			jtxcCancel(oldSheetId);
		}
		newSheet.setSheetPriValue(oldSheet.getSheetPriValue());
		newSheet.setPreAlarmValue(oldSheet.getPreAlarmValue());
		newSheet.setAlarmValue(oldSheet.getAlarmValue());
		String info = "处理要求填写人：" + staff.getName() + "  联系电话：" + staff.getRelaPhone() + "\n" + curSheet.getDealRequire();
		newSheet.setDealRequire(info);
		newSheet.setAutoVisitFlag(0);
		newSheet.setPrecontractSign(0);
		newSheet.setRetOrgId(dealorgId);
		newSheet.setRetOrgName(dealorgName);
		newSheet.setRetStaffId(staffId);
		newSheet.setRetStaffName(staffName);
		newSheet.setMonth(oldSheet.getMonth());
		String newSheetId = pubFunc.crtSheetId(curSheet.getRegionId());
		newSheet.setWorkSheetId(newSheetId);
		String flowSequence = getFlowSequence(oldSheet);
		newSheet.setFlowSequence(flowSequence);// 流水号 flowSeq
		String mainOrg = "协查单位: ";
		int sendType = 0;
		// 如果派到个人,工单状态为处理中,派到部门就为待处理
		String curReceiveOrg = "";
		if (curSheet.getRcvOrgId().equals("STFFID")) {
			sendType = 1;
			TsmStaff recStaff = this.pubFunc.getStaff(curSheet.getRcvStaffId());
			curReceiveOrg = recStaff.getOrganizationId();
			newSheet.setRcvOrgId(curReceiveOrg);
			newSheet.setRcvOrgName(recStaff.getOrgName());
			newSheet.setRcvStaffId(curSheet.getRcvStaffId());
			newSheet.setRcvStaffName(recStaff.getName());
			newSheet.setDealOrgId(recStaff.getOrganizationId());
			newSheet.setDealOrgName(recStaff.getOrgName());
			newSheet.setDealStaffId(curSheet.getRcvStaffId());
			newSheet.setDealStaffName(recStaff.getName());
			int sheetStatu = getDealing(servType);
			newSheet.setSheetStatu(sheetStatu);
			newSheet.setSheetSatuDesc(pubFunc.getStaticName(sheetStatu));
			newSheet.setLockFlag(1);
			mainOrg = mainOrg + recStaff.getOrgName() + "(" + newSheet.getDealStaffName() + ")";
		} else {
			curReceiveOrg = curSheet.getRcvOrgId();
			newSheet.setRcvOrgId(curSheet.getRcvOrgId());
			newSheet.setRcvOrgName(curSheet.getRcvOrgName());
			int sheetStatu = getRepeal(servType);
			newSheet.setSheetStatu(sheetStatu);
			newSheet.setSheetSatuDesc(pubFunc.getStaticName(sheetStatu));
			newSheet.setLockFlag(0);

			// 判断协查单是否自动分派
			boolean allotToDealFlag = false;
			OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
			if (!pubFunc.isYueJi(orderInfo.getAskChannelId())) {
				allotToDealFlag = true;
			}
			if (orderInfo.getAskSource() == StaticData.ACCEPT_COME_FROM_JT) {
				allotToDealFlag = true;
			}
			if (allotToDealFlag) {
				String result = workSheetAllot.allotToDeal(newSheet);
				sendType = WorkSheetAllot.RST_SUCCESS.equals(result) ? 1 : 0;
				mainOrg = mainOrg + curSheet.getRcvOrgName() + "( )";
			}
		}
		int recRegion = this.pubFunc.getOrgRegion(newSheet.getRcvOrgId());
		String recRegionName = this.pubFunc.getRegionName(recRegion);
		newSheet.setReceiveRegionId(recRegion);
		newSheet.setReceiveRegionName(recRegionName);
		newSheet.setMainType(1);
		this.sheetPubInfoDao.saveSheetPubInfo(newSheet);
		logger.info("xcDispathSheet newSheet:{}", newSheet);
		sendNoteCont(newSheet, sendType, 0, 0);
		// 记录处理类型
		TsSheetDealType typeBean = new TsSheetDealType();
		typeBean.setDealType(dealType);
		typeBean.setDealTypeId(pubFunc.crtGuid());
		typeBean.setOrderId(orderId);
		typeBean.setWorkSheetId(newSheetId);
		typeBean.setDealTypeDesc("协查派单");
		typeBean.setDealId(0);// 处理定性ID 如果为审批单,0为不同意,1为同意
		typeBean.setDealDesc(mainOrg);// 处理定性名
		typeBean.setMonth(oldSheet.getMonth());
		tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型
		if (0 == xcType) { // 集团协查通知
			MessagePrompt p = new MessagePrompt();
			p.setMsgContent("集团协查申请通知，服务单号:" + orderId);
			p.setTypeId(StaticData.MESSAGE_PROMPT_JTXCSQ); // 集团协查申请通知
			p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_JTXCSQ));
			p.setStaffId(newSheet.getRcvStaffId());
			p.setStaffName(newSheet.getRcvStaffName());
			p.setOrgId(newSheet.getRcvOrgId());
			p.setOrgName(newSheet.getRcvOrgName());
			messageManager.createMsgPrompt(p);
		} else if (1 == xcType) { // 省内协查通知
			MessagePrompt p = new MessagePrompt();
			p.setMsgContent("省内协查申请通知，服务单号:" + orderId);
			p.setTypeId(StaticData.MESSAGE_PROMPT_SNXCSQ); // 省内协查申请通知
			p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_SNXCSQ));
			p.setStaffId(newSheet.getRcvStaffId());
			p.setStaffName(newSheet.getRcvStaffName());
			p.setOrgId(newSheet.getRcvOrgId());
			p.setOrgName(newSheet.getRcvOrgName());
			messageManager.createMsgPrompt(p);
		}
		sheetPubInfoDao.updateLastXcSheetIdBySheetId(newSheetId, oldSheetId);// 更新最后一次协查单号
		updateXcFlow(curSheet, xcType, staffId, newSheetId, curReceiveOrg);
		saveXcPenalty(newSheetId, oldSheetId, orderId, penaltyMoney);
		return "SUCCESS";
	}

	private String getFlowSequence(SheetPubInfo oldSheet) {
		String flowSeq = "2";
		if (oldSheet.getFlowSequence() != null && !(oldSheet.getFlowSequence().equals(""))) {
			flowSeq = oldSheet.getFlowSequence();
		}
		return flowSeq + 1;
	}

	private int getJtxcDealLimitTime(int dealLimitTime) {
		if (dealLimitTime <= 2) {
			dealLimitTime = 2;
		} else {
			dealLimitTime = dealLimitTime - 2;
		}
		return dealLimitTime;
	}

	private void updateXcFlow(SheetPubInfo curSheet, int xcType, int sendStaff, String curXcSheetId, String curReceiveOrg) {
		String serviceOrderId = curSheet.getServiceOrderId();
		String mainSheetId = curSheet.getWorkSheetId();
		XcFlow xcFlow = sheetPubInfoDao.getXcFlowByCurXcId(mainSheetId);// 查询原单是否存在协查记录
		logger.info("updateXcFlow curSheet:{},xcType:{},xcFlow:{}", curSheet, xcType, xcFlow);
		if (null == xcFlow) {// 不存在，新增
			xcFlow = new XcFlow();
			xcFlow.setServiceOrderId(serviceOrderId);
			xcFlow.setXcType(xcType);
			xcFlow.setMainSheetId(mainSheetId);
			xcFlow.setSendStaff(sendStaff);
			xcFlow.setCurXcSheetId(curXcSheetId);
			xcFlow.setCurReceiveOrg(curReceiveOrg);
			sheetPubInfoDao.insertXcFlow(xcFlow);
		} else {// 存在，更新，并完成原单
			sheetPubInfoDao.sendXcFlow(curXcSheetId, curReceiveOrg, mainSheetId);
			snxcSumbitOrgDeal(mainSheetId, curSheet.getRegionId(), curSheet.getMonth(), curSheet.getDealRequire());
		}
	}

	private void saveXcPenalty(String xcSheetId, String pdSheetId, String serviceOrderId, String penaltyMoney) {
		logger.info("saveXcPenalty xcSheetId:{},pdSheetId:{},serviceOrderId:{},penaltyMoney:{}", xcSheetId, pdSheetId,
				serviceOrderId, penaltyMoney);
		if (!"".equals(penaltyMoney)) {
			sheetPubInfoDao.insertXcPenalty(xcSheetId, pdSheetId, serviceOrderId, penaltyMoney);
		}
	}

	// 省内协查处理
	public String snxcSumbitOrgDeal(String worksheetId, int regionId, int month, String xcContent) {
		SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(worksheetId, regionId, month, true);
		int servType = sheetInfo.getServType();
		if (1 != sheetInfo.getLockFlag()) {
			return "STATUERROR";
		}
		String workSheetId = sheetInfo.getWorkSheetId();
		tsWorkSheetDao.deleteDealTypeBySheetid(workSheetId);
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetInfo.getServiceOrderId());
		typeBean.setWorkSheetId(workSheetId);
		typeBean.setDealType("省内协查");
		typeBean.setDealTypeDesc("协查处理");
		typeBean.setDealId(0);// 处理定性ID
		typeBean.setDealDesc("");// 处理定性名
		typeBean.setDealContent(xcContent);// 处理内容
		typeBean.setMonth(sheetInfo.getMonth());
		tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型
		int tmpNxtTach = getXcTachId(servType);
		sheetPubInfoDao.updateSheetDealRequire(worksheetId, sheetInfo.getDealRequire(), "", "省内协查处理", xcContent, 99, tmpNxtTach);
		int finish = getFinish(servType);
		sheetPubInfoDao.updateSheetState(workSheetId, finish, pubFunc.getStaticName(finish), sheetInfo.getMonth(), 2);
		sheetPubInfoDao.updateSheetFinishDate(workSheetId);
		sheetPubInfoDao.finishXcFlow(workSheetId);
		return "SUCCESS";
	}

	// 集团协查处理
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public String jtxcSumbitOrgDeal(String worksheetId, int regionId, int month, int xcType, String xcContent) {
		Integer monthObj = month;
		SheetPubInfo curSheet = sheetPubInfoDao.getSheetObj(worksheetId, regionId, monthObj, true);
		String orderId = curSheet.getServiceOrderId();
		int servType = curSheet.getServType();
		if (1 != curSheet.getLockFlag()) {
			return "STATUERROR";
		}
		SheetPubInfo oldSheet = sheetPubInfoDao.getSheetObj(curSheet.getSourceSheetId(), regionId, monthObj, true);
		int tacheId = oldSheet.getTacheId();
		int lockFlag = oldSheet.getLockFlag();
		xcContent = getXcContent(xcContent, xcType, tacheId, lockFlag);
		TsmStaff staffObj = pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staffObj.getId());
		String staffName = staffObj.getName();
		tsWorkSheetDao.deleteDealTypeBySheetid(worksheetId);
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(orderId);
		typeBean.setWorkSheetId(worksheetId);
		typeBean.setDealType("集团协查");
		typeBean.setDealTypeDesc("协查处理");
		typeBean.setDealId(0);// 处理定性ID
		typeBean.setDealDesc("");// 处理定性名
		typeBean.setDealContent(xcContent);// 处理内容
		typeBean.setMonth(curSheet.getMonth());
		tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型
		int tmpNxtTach = getXcTachId(servType);
		sheetPubInfoDao.updateSheetDealRequire(worksheetId, curSheet.getDealRequire(), "", "集团协查处理", xcContent, 99, tmpNxtTach);
		int finish = getFinish(servType);
		sheetPubInfoDao.updateSheetState(worksheetId, finish, pubFunc.getStaticName(finish), curSheet.getMonth(), 2);
		sheetPubInfoDao.updateSheetFinishDate(worksheetId);
		sheetPubInfoDao.finishXcFlow(worksheetId);
		if (0 == xcType) {
			if (tacheId != StaticData.TACHE_ASSIGN && tacheId != StaticData.TACHE_DEAL && tacheId != StaticData.TACHE_ASSIGN_NEW
					&& tacheId != StaticData.TACHE_DEAL_NEW) {
				return "WRONGTACHE";
			}
			if (1 != lockFlag) {
				return "WRONGLOCK";
			}
			int sheetType = oldSheet.getSheetType();
			SheetPubInfo newSheet = new SheetPubInfo();
			newSheet.setWorkSheetId(oldSheet.getWorkSheetId());
			newSheet.setServiceOrderId(orderId);
			newSheet.setRcvOrgId("STFFID");
			newSheet.setRcvStaffId(staffId);
			newSheet.setRcvStaffName(staffName);
			newSheet.setDealRequire("同意集团协查申请系统自动流转");
			newSheet.setMonth(oldSheet.getMonth());
			newSheet.setRegionId(oldSheet.getRegionId());
			newSheet.setStationLimit(0);
			newSheet.setDealLimitTime(0);
			newSheet.setMainType(1);
			newSheet.setDealStaffName(StaticData.FLAG_SYS_DO);// 该标识表示系统自动办结
			SheetPubInfo[] sheetArray = { newSheet };
			try {
				switch (sheetType) {
				case StaticData.SHEET_TYPE_TS_ASSING:// 非投诉，后台派单
				case StaticData.SHEET_TYPE_TS_ASSING_NEW:// 投诉，后台派单
					dispatchSheet(sheetArray);
					break;
				case StaticData.SHEET_TYPE_TS_DEAL_NEW:// 投诉，部门处理工单
				case StaticData.SHEET_TYPE_TS_IN_DEAL:// 投诉，部门内处理工单
				case StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW:// 投诉，部门内审批工单
				case StaticData.SHEET_TYPE_TS_DEAL:// 非投诉，部门处理工单
				case StaticData.SHEET_TYPE_TS_CHECK_DEAL:// 非投诉，部门审批工单
					orgDealDispathSheet(sheetArray, "同意集团协查申请系统自动流转", 0, 0, 0);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				//异常处理逻辑
				logger.error("jtxcSumbitOrgDeal Exception：{}", e.getMessage(), e);
			}
		} else {
			MessagePrompt p = new MessagePrompt();
			p.setMsgContent("集团协查不同意通知，服务单号:" + orderId);
			p.setTypeId(StaticData.MESSAGE_PROMPT_JTXCBTY); // 集团协查申请通知
			p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_JTXCBTY));
			p.setStaffId(oldSheet.getDealStaffId());
			p.setStaffName(oldSheet.getDealStaffName());
			p.setOrgId(oldSheet.getDealOrgId());
			p.setOrgName(oldSheet.getDealOrgName());
			messageManager.createMsgPrompt(p);
		}
		return "SUCCESS";
	}

	private String getXcContent(String xcContent, int xcType, int tacheId, int lockFlag) {
		if (0 == xcType) {
			xcContent = "同意协查集团";
			if (tacheId != StaticData.TACHE_ASSIGN && tacheId != StaticData.TACHE_DEAL
					&& tacheId != StaticData.TACHE_ASSIGN_NEW && tacheId != StaticData.TACHE_DEAL_NEW) {
				xcContent = "非派单和处理环节，不能协查集团";
			}
			if (1 != lockFlag) {
				xcContent = "该工单已处理，不能协查集团";
			}
		}
		return xcContent;
	}

	// 集团协查作废
	@Transactional(propagation=Propagation.REQUIRED) 
	public String jtxcCancel(String worksheetId) {
		String str = " AND source_sheet_id ='" + worksheetId + "' AND sheet_type = 720130029 AND sheet_statu NOT IN (700000047, 720130036, 720130037)";
		List jtxcList = this.sheetPubInfoDao.getSheetCondition(str, true);
		if (jtxcList != null && !jtxcList.isEmpty()) {
			String serviceOrderId = "";
			String workSheetId = "";
			int servType = 0;
			int month = 0;
			String dealRequire = "";
			for (int i = 0; i < jtxcList.size(); i++) {
				Map map = (Map) jtxcList.get(i);
				serviceOrderId = map.get("SERVICE_ORDER_ID").toString();
				workSheetId = map.get("WORK_SHEET_ID").toString();
				servType = Integer.parseInt(map.get("SERVICE_TYPE").toString());
				month = Integer.parseInt(map.get("MONTH_FLAG").toString());
				dealRequire = map.get("DEAL_REQUIRE").toString();
				boolean isNewCmp = servType == StaticData.SERV_TYPE_NEWTS;
				int state = isNewCmp ? StaticData.WKST_DISANNUUL_STATE_NEW : StaticData.WKST_DISANNUUL_STATE;
				int tachId = isNewCmp ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
				String stateDesc = pubFunc.getStaticName(state);
				String where = " AND W.WORK_SHEET_ID ='" + workSheetId + "'";
				sheetPubInfoDao.updateDealDisannuul(state, stateDesc, month, tachId, where);
				sheetPubInfoDao.updateSheetDealRequire(workSheetId, dealRequire, " ", "集团协查作废", "集团协查作废", 46, tachId);
				sheetPubInfoDao.finishXcFlow(workSheetId);
				TsSheetDealType typeBean = new TsSheetDealType();
				typeBean.setDealTypeId(this.pubFunc.crtGuid());
				typeBean.setOrderId(serviceOrderId);
				typeBean.setWorkSheetId(workSheetId);
				typeBean.setDealType("集团协查作废");
				typeBean.setDealTypeDesc("集团协查作废");
				typeBean.setDealId(0);
				typeBean.setDealDesc("是");
				typeBean.setDealContent("集团协查作废");
				typeBean.setMonth(month);
				tsWorkSheetDao.saveSheetDealType(typeBean);
			}
		}
		return "SUCCESS";
	}

	private int getXcTachId(int servType) {
		return servType == StaticData.SERV_TYPE_NEWTS ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
	}

	private int getDealing(int servType) {
		return servType == StaticData.SERV_TYPE_NEWTS ? StaticData.WKST_DEALING_STATE_NEW : StaticData.WKST_ORGDEALING_STATE;
	}

	private int getRepeal(int servType) {
		return servType == StaticData.SERV_TYPE_NEWTS ? StaticData.WKST_REPEAL_STATE_NEW : StaticData.WKST_REPEAL_STATE;
	}

	private int getFinish(int servType) {
		return servType == StaticData.SERV_TYPE_NEWTS ? StaticData.WKST_FINISH_STATE_NEW : StaticData.WKST_FINISH_STATE;
	}

	/**
	 * 保存办结原因和回防记录
	 */
	public String saveQualitativeAndVisit(TsSheetQualitative bean, TScustomerVisit tscustomerVisit) {
		String sheetId="";
		if(StringUtils.isNull(bean)) {
			sheetId = tscustomerVisit.getWorkSheetId();
		}else {
			sheetId=bean.getSheetId();
		}
		 
		SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
		String serviceId = sheetInfo.getServiceOrderId();
		if(StringUtils.isNotNull(bean)) {
			sheetQualitative.saveTsSheetQualitative(bean); // 记录投诉定性内容
			labelManageDao.saveQualitative(bean.getControlAreaSec(), bean.getDutyOrg(), pubFunc.getLastYY(bean), serviceId);
		}
		if (tscustomerVisit != null) {
			tscustomerVisit.setRegionName(sheetInfo.getRegionName());
			tscustomerVisit.setMonth(sheetInfo.getMonth());
			customerVisit.saveCustomerVisit(tscustomerVisit);
			labelManageDao.updateDealResult(serviceId, tscustomerVisit.getTsDealResult(), tscustomerVisit.getTsDealResultName());
		}
		return "SUCCESS";
	}

	public SheetPubInfo getTheLastSheetInfo(String orderId) {
		return sheetPubInfoDao.getTheLastSheetInfo(orderId);
	}

    /**
	 *  派单环节派单
	 * sheetInfoArry 工单数组对象
	 */
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String dispatchSheet(SheetPubInfo[] sheetInfoArry) {
		if(sheetInfoArry == null) {
			return "ERROR";	//前台传入数组对象为空		
		}
		int size = sheetInfoArry.length;
		SheetPubInfo sheetPubInfo = sheetInfoArry[0];
		String sheetId = sheetPubInfo.getWorkSheetId();
		Integer month = sheetPubInfo.getMonth();
		int region = sheetPubInfo.getRegionId();
		
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId,region,month,true);
		// 非处理中状态的单子不能提交
		if(complaintDealHandler.notInDeal(sheetInfo.getSheetStatu(), sheetInfo.getLockFlag())) {
			return "STATUERROR";
		}
		//流水号
		String flowSeq = "1";
		if (org.apache.commons.lang3.StringUtils.isNotBlank(sheetInfo.getFlowSequence())) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;
		
		String dealOrg = "";
        String dealStaffId = "0";
        String require = "";
		if (StaticData.FLAG_SYS_DO.equals(sheetPubInfo.getDealStaffName())) {
			require = "该工单由系统自动办结. " + sheetPubInfo.getDealRequire();
		} else {
			TsmStaff staff = getPdStaff(sheetPubInfo);
			dealOrg = staff.getOrganizationId();
			dealStaffId = staff.getId();
			require = getPdRequire(sheetPubInfo, staff);
		}
		
		String tempName = "";
		StringBuilder mainOrg = new StringBuilder();
		mainOrg.append("主办单位: ");
		StringBuilder assitOrg = new StringBuilder();
		assitOrg.append("      协办单位: ");
		for(int i=0;i<size;i++) {
			sheetPubInfo = sheetInfoArry[i];
			if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
				String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
				tempName =this.pubFunc.getOrgName(orgId);
				if(StaticData.FLAG_SYS_DO.equals(sheetPubInfo.getDealStaffName())){
				    dealOrg = orgId;
				    dealStaffId = sheetPubInfo.getRcvStaffId()+"";
				}
			} else {
				tempName =sheetPubInfo.getRcvOrgName();
				sheetPubInfo.setRcvStaffName(" ");
			}
			//判断是否为主单单位
			if(sheetPubInfo.getMainType() == 1) {
				mainOrg.append(tempName).append("(").append(sheetPubInfo.getRcvStaffName()).append(")");
			} else {
				assitOrg.append(tempName).append("(").append(sheetPubInfo.getRcvStaffName()).append(")").append("; ");
			}
		}
		String rcvOrgName = mainOrg.toString()+assitOrg.toString();
		int tmpNxtTach = getNxtTach(sheetInfo);
		sheetPubInfoDao.updateSheetDealRequire(sheetId, require,rcvOrgName,"分派工单",sheetPubInfo.getDealRequire(),16,tmpNxtTach);
		
		
		//记录处理类型
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetPubInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetId);
		typeBean.setDealType("分派工单");
		typeBean.setDealTypeDesc("分派列表");
		typeBean.setDealDesc(rcvOrgName);//分派部门 
		typeBean.setDealContent(sheetPubInfo.getDealRequire());//处理内容
		typeBean.setMonth(month);
		saveSheetDealType(typeBean);
		updateSSDealHour(sheetPubInfo);
		Map otherParam = new HashMap();
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		otherParam.put("DEAL_REQUIRE",require);
		otherParam.put("ROUTE_VALUE",StaticData.ROUTE_GOTO_NEXT);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("SHEETARRAY", sheetInfoArry);
		otherParam.put("PRECONTRACTSIGN", "1");
		String dispatchSheetFlag = workSheetFlowService.submitWorkFlow(sheetId, region, otherParam);
		jtxcCancel(sheetId);
		getAllotConfigSecond(sheetPubInfo.getServiceOrderId(), dispatchSheetFlag);
		return dispatchSheetFlag;
	}

	private TsmStaff getPdStaff(SheetPubInfo sheetPubInfo) {
		if ("AUTOPD".equals(sheetPubInfo.getDealStaffName())) {
			return pubFunc.getStaff(sheetPubInfo.getDealStaffId());
		} else {
			return pubFunc.getLogonStaff();
		}
	}

	private String getPdRequire(SheetPubInfo sheetPubInfo, TsmStaff pdStaff) {
		if ("AUTOPD".equals(sheetPubInfo.getDealStaffName())) {
			return "派单员工:" + pdStaff.getName() + "(" + pdStaff.getId() + ")  \n派发意见:" + sheetPubInfo.getDealRequire();
		} else {
			return "派单员工:" + pdStaff.getName() + "(" + pdStaff.getId() + ")  派单人联系电话:" + pdStaff.getRelaPhone() + "\n派发意见:" + sheetPubInfo.getDealRequire();
		}
	}

	private int getNxtTach(SheetPubInfo sheetInfo) {
		return sheetInfo.getServType() == StaticData.SERV_TYPE_NEWTS ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
	}

	private void saveSheetDealType(TsSheetDealType typeBean) {
		int sheetDealType = this.tsWorkSheetDao.querySheetDealType(typeBean);
		if (sheetDealType < 1) {
			tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型
		}
	}

	private void updateSSDealHour(SheetPubInfo sheetPubInfo) {
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(sheetPubInfo.getServiceOrderId(), false);
		if (orderInfo.getComeCategory() == 707907003) {
			labelManageDao.updateDealHours(sheetPubInfo.getDealLimitTime(), sheetPubInfo.getServiceOrderId());
		}
	}

	private void getAllotConfigSecond(String orderId, String dispatchSheetFlag) {
		logger.info("getAllotConfigSecond orderId: {} orderId: {}", orderId, dispatchSheetFlag);
		if ("SUCCESS".equals(dispatchSheetFlag)) {
			Map map = tsWorkSheetDao.getDispatchOrgMap(orderId, "分公司智能转派");
			logger.info("getAllotConfigSecond: map: {}", map);
			if (!map.isEmpty()) {// 部门处理虚拟岗
				String receiveOrg = defaultMapValueIfNull(map, "DISPATCH_ORG", "");
				int dealStaffId = Integer.parseInt(defaultMapValueIfNull(map, "DEAL_STAFF_ID", "0"));
				logger.info("getAllotConfigBMCL: {}", map);
				if (org.apache.commons.lang3.StringUtils.isNotBlank(receiveOrg) && 0 != dealStaffId) {
					autoDealDispathSheet(orderId, receiveOrg, dealStaffId);
				}
			} else {// 后台派单虚拟岗
				map = tsWorkSheetDao.getDispatchOrgMap(orderId, "智能转派后台派单虚拟岗");
				if (!map.isEmpty()) {
					String receiveOrg = defaultMapValueIfNull(map, "DISPATCH_ORG", "");
					int dealStaffId = Integer.parseInt(defaultMapValueIfNull(map, "DEAL_STAFF_ID", "0"));
					logger.info("getAllotConfigHTPD: {}", map);
					if (org.apache.commons.lang3.StringUtils.isNotBlank(receiveOrg) && 0 != dealStaffId) {
						autoDealDispathSheet(orderId, receiveOrg, dealStaffId);
					}
				}
			}
		}
	}

	private String defaultMapValueIfNull(Map map, String key, String defaultStr) {
		return map.get(key) == null ? defaultStr : map.get(key).toString();
	}

	private void autoDealDispathSheet(String orderId, String receiveOrg, int dealStaffId) {
		SheetPubInfo source = sheetPubInfoDao.getTheLastDealSheetInfo(orderId);
		if (null != source) {
			SheetPubInfo newSheet = new SheetPubInfo();
			newSheet.setServiceOrderId(orderId);
			newSheet.setWorkSheetId(source.getWorkSheetId());
			newSheet.setRegionId(source.getRegionId());
			newSheet.setMonth(source.getMonth());
			newSheet.setDealContent("请核实处理并回复用户。");
			newSheet.setRcvOrgId(receiveOrg);
			newSheet.setRcvOrgName(pubFunc.getOrgName(receiveOrg));
			newSheet.setStationLimit(0);
			newSheet.setMainType(1);
			SheetPubInfo[] sheetArray = { newSheet };
			try {
				orgDealDispathSheet(sheetArray, "请核实处理并回复用户。", 0, 0, dealStaffId);
			} catch (Exception e) {
				logger.error("autoDealDispathSheet error: {}", e.getMessage(), e);
			}
		}
	}

	/**
	 * 派单环节直接归档，派单-->归档环节
	 * @param sheetBean 工单对象 ，基本值：工单号，月分区，地域，处理内容
	 * @retResult 客户回访内容 答复时间ASK_DATE 工单号CUST_SATISFY_DEGREE 定单号SERVICE_ORDER_ID 受理地域REGION_ID 回访内容 BACK_VISIT_CONTENT
	 * @boo 是否需要答复 true 为需要答复
	 * @dealId 处理定性ID
	 * @dealDesc 处理定性描述
	 * @return
	 */
    @Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String dispatchToPigeonhole(SheetPubInfo sheetBean,int dealId,String dealDesc, String isAutoComplete) {
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(sheetBean.getServiceOrderId(), false);
		if(null == orderInfo){
			return ResultUtil.fail(ResultEnum.OBJERROR);
		}
		
		int region = sheetBean.getRegionId();
		Integer monthFlag = sheetBean.getMonth();
		String sheetId = sheetBean.getWorkSheetId();
		/* 取工单对象*/
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId,region,monthFlag,true);
		// 非处理中状态的单子不能提交
		if(complaintDealHandler.notInDeal(sheetInfo.getSheetStatu(), sheetInfo.getLockFlag())) {
			return ResultUtil.fail(ResultEnum.STATUERROR);
		}
		Integer month = sheetBean.getMonth();
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;
		String route = sheetBean.getRcvOrgId();
		boolean notAutoAllot = false;//不进行自动分派
		if("ASSIGN_TO_FINISH".equals(route)) {//后台派单直接终定性
			route = "ASSIGN_TO_FINASSESS";
			notAutoAllot = true;
		}
		String dealRequ = sheetInfo.getDealRequire() == null ?"":sheetInfo.getDealRequire();
		sheetPubInfoDao.updateSheetDealRequire(sheetId, dealRequ," ","直接办结",sheetBean.getDealContent(),17,getNextTacheId(route));
		
		labelManageDao.saveFormalAnswerDate(sheetInfo.getServiceOrderId());
		
		/*2013-10-28 LiJiahui 对于直接办结的后台派单工单，更改处理时限*/
		if(sheetInfo.getServType() == StaticData.SERV_TYPE_NEWTS){
	        labelManageDao.updateLastAnswerDate(sheetInfo.getServiceOrderId());
		}
		
		//记录处理类型
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetBean.getServiceOrderId());
		typeBean.setWorkSheetId(sheetId);
		typeBean.setDealType("直接办结");
		typeBean.setDealTypeDesc("处理定性");
		typeBean.setDealId(dealId);//ID
		typeBean.setDealDesc(dealDesc);
		typeBean.setDealContent(sheetBean.getDealContent());//处理内容
		typeBean.setMonth(month);
		tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型

		String dealOrg = "";
		String dealStaffId = "";
		if("1".equals(isAutoComplete)) {
			dealOrg = sheetBean.getDealOrgId();
			dealStaffId = sheetBean.getDealStaffId() + "";
		} else {
			TsmStaff staff = pubFunc.getLogonStaff();
			dealOrg = staff.getOrganizationId();
			dealStaffId = staff.getId();
		}
		Map otherParam = new HashMap();
		otherParam.put("ROUTE_VALUE", route);
		if(notAutoAllot) {
			otherParam.put("NOT_AUTO_ALLOT", true);
		}
		otherParam.put("MONTH_FALG", month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		sheetPubInfoDao.updateWorkSheetAreaTacheDate(sheetInfo.getServiceOrderId());
		
		//派单办结修改事务
		String dispatchToPigeonholeFlag = workSheetFlowService.submitWorkFlow(sheetId, region, otherParam);
		if ("SUCCESS".equals(dispatchToPigeonholeFlag)) {
			jtxcCancel(sheetId);
		}
		return ResultUtil.success();
	}
	private int getNextTacheId(String route) {
	    if(route.equals(StaticData.ROUTE_ASSIGN_TO_FINASSESS)){
	        return StaticData.TACHE_ZHONG_DINGXING_NEW;
	    }else if(route.equals(StaticData.ROUTE_ASSIGN_TO_ASSESS)) {
			return StaticData.TACHE_PIGEONHOLE;
		}else if(route.equals(StaticData.ROUTE_ASSIGN_TO_AUD)) {//到审核
			return StaticData.TACHE_AUIT;
		}		
		return StaticData.TACHE_FINISH;
	}
	
	/**
	 * 派单环节直接处理
	 * @param regionId 地域ID
	 * @param orderId 定单号
	 * @param sheetId 工单号
	 * @param month 月分区
	 * @param dealContent 处理内容
	 * @param delalId 处理定性ID
	 * @param dealName 处理定性名
	 * @return
	 */
	public String assignToFinish(int regionId,String orderId,String sheetId,Integer month,
								 String dealContent,int delalId,String dealName) {
		/* 取工单对象*/
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId,regionId,month,true);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (state != 1) {
			return "STATUERROR";
		}
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "STATUERROR";
		}
		TsmStaff staff = pubFunc.getLogonStaff();
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;
		Map otherParam = new HashMap();
		otherParam.put("DEAL_REQUIRE", dealContent);//作为退单要求传到后台
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_ASSIGN_TO_REPLY_TO_FINISH);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		sheetPubInfoDao.updateSheetDealRequire(sheetId, " "," ","审核派单直接竣工",dealContent,19,StaticData.TACHE_FINISH);
		//记录定单竣工的环节
		orderAskInfoDao.updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
		//记录处理类型
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(orderId);
		typeBean.setWorkSheetId(sheetId);
		typeBean.setDealType("审核派单直接处理");
		typeBean.setDealTypeDesc("处理定性");
		typeBean.setDealId(delalId);//处理定性ID
		typeBean.setDealDesc(dealName);//处理定性名
		typeBean.setDealContent(dealContent);//处理内容
		typeBean.setMonth(month);
		this.tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型
		
		workSheetFlowService.submitWorkFlow(sheetId, regionId, otherParam);
		return "SUCCESS";
	}
	
	/**
	 * 部门处理环节转派工单
	 * @param workSheetObj  派往部门对象
	 * @param dealResult 处理要求
	 * @param dealType 0为部门转派工单，1为后台派单 5 审核重新派单 3 为审核退单，4为部门审批退单
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String orgDealDispathSheet(SheetPubInfo[] workSheetObj,String dealResult,int channel, int dealType,int autoStaffId) {
		//取得原工单对象
		SheetPubInfo sheetPubInfo = workSheetObj[0];
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
				sheetPubInfo.getWorkSheetId(),sheetPubInfo.getRegionId(),sheetPubInfo.getMonth(), true);
		
		//状态非处理中的工单不能提交
		int sheetState = sheetInfo.getLockFlag();
		if(StaticData.SHEET_LOCK_FLAG_1 != sheetState){
			logger.warn("定单号为:" + sheetInfo.getWorkSheetId() + "的工单状态不是处理中,不能提交!");
			return "STATUSERROR";
		}
		
		// 挂起的工单不能提交
		if(ComplaintDealHandler.isHoldSheet(sheetInfo.getSheetStatu())) {
			return "STATUSERROR";
		}

		String serviceOrderId = sheetInfo.getServiceOrderId();
		String allotWorkSheet = "";//审批上级工单
		String flowSeq = "2";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		boolean isCurCheckSheet = isCheckSheet(sheetInfo.getSheetType());
		if(isCurCheckSheet) {
			//调用审批单判断审批单的主单单位是否完成
			WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(sheetInfo);
			int sizeAllot = 0;
			if(sheetAllotRealList.length > 0) {
				sizeAllot = sheetAllotRealList.length;
			}

			WorkSheetAllotReal sheetAllotRealobj = null;
			if(sizeAllot > 0) {
				for(int j=0;j<sizeAllot;j++) {
					sheetAllotRealobj = sheetAllotRealList[j];
					if(sheetAllotRealobj.getMainSheetFlag() == 1) {
						if(sheetAllotRealobj.getCheckFalg() == 0) {
							return "ALLOTREAL";
						}
						allotWorkSheet = sheetAllotRealobj.getPreDealSheet();
						break;
					}
				}
			}
		}
		
		boolean isSys = StaticData.FLAG_SYS_DO.equals(sheetPubInfo.getDealStaffName());
		int staffId = 0;
		String staffName = "SYSTEM";
		String dealorgId = "";
		String dealorgName = "SYSTEM";
        String info = sheetPubInfo.getDealRequire();
        if(isSys){
            info = "该工单由系统自动办结. " + info;
        }else{
            TsmStaff staff = getDealStaff(autoStaffId);
            info =  "处理要求填写人：" + staff.getName() + "  联系电话：" + staff.getRelaPhone() + 
                    "\n"+info;
            staffId = Integer.parseInt(staff.getId());
            staffName = staff.getName();
            dealorgId = staff.getOrganizationId();
            dealorgName = staff.getOrgName();
        }

		//组装新的工单对象
		SheetPubInfo newSheetPubInfo = new SheetPubInfo();
		newSheetPubInfo.setServiceOrderId(serviceOrderId);
		newSheetPubInfo.setRegionId(sheetInfo.getRegionId());
		newSheetPubInfo.setRegionName(sheetInfo.getRegionName());
		newSheetPubInfo.setServType(sheetInfo.getServType());
		newSheetPubInfo.setServTypeDesc(sheetInfo.getServTypeDesc());
		newSheetPubInfo.setSourceSheetId(sheetInfo.getWorkSheetId());
		newSheetPubInfo.setTacheId(sheetInfo.getTacheId());
		newSheetPubInfo.setTacheDesc(sheetInfo.getTacheDesc());
		newSheetPubInfo.setWflInstId(sheetInfo.getWflInstId());
		newSheetPubInfo.setTacheInstId(sheetInfo.getTacheInstId());
		
		int sheetType = PubFunc.getSheetType(sheetInfo.getServType(), sheetInfo.getTacheId());
		newSheetPubInfo.setSheetType(sheetType);
		newSheetPubInfo.setSheetTypeDesc(this.pubFunc.getStaticName(sheetType));
		
		newSheetPubInfo.setSheetPriValue(sheetInfo.getSheetPriValue());
		newSheetPubInfo.setDealLimitTime(sheetInfo.getDealLimitTime());
		newSheetPubInfo.setStationLimit(sheetInfo.getDealLimitTime());
		newSheetPubInfo.setPreAlarmValue(sheetInfo.getPreAlarmValue());
		newSheetPubInfo.setAlarmValue(sheetInfo.getAlarmValue());
		newSheetPubInfo.setDealRequire(info);//加上要求填写人的名字,电话
		newSheetPubInfo.setAutoVisitFlag(0);
		
		newSheetPubInfo.setPrecontractSign(dealType);
		//部门转派，新的工单中的派发部门字段记录原处理部门
		newSheetPubInfo.setRetOrgId(dealorgId);
		newSheetPubInfo.setRetOrgName(dealorgName);
		newSheetPubInfo.setRetStaffId(staffId);
		newSheetPubInfo.setRetStaffName(staffName);
					
		newSheetPubInfo.setMonth(sheetInfo.getMonth());
		String newSheetId = "";
		int count = 0;
		String strSheetId="";//保存主办单位
		String orgNamelist="";//下派部门的集合
		StringBuilder mainOrgStr = new StringBuilder("主办单位: ");
		StringBuilder assitOrgStr = new StringBuilder("      协办单位: ");
		
		int size = workSheetObj.length;
		String[] strSheetList = new String[size];
		SheetPubInfo sheetObj = null;
		boolean isNewCmp = sheetInfo.getServType() == StaticData.SERV_TYPE_NEWTS;
		int sendType=0;
		for (int i = 0; i < size; i++) {
			newSheetId = pubFunc.crtSheetId(sheetPubInfo.getRegionId());
			newSheetPubInfo.setWorkSheetId(newSheetId);
			sheetObj = workSheetObj[i];
			newSheetPubInfo.setFlowSequence(flowSeq+(i+1));//流水号 flowSeq
			
			sendType=0;
			//如果派到个人,工单状态为处理中,派到部门就为待处理
			if(sheetObj.getRcvOrgId().equals("STFFID")) {
				sendType=1;
				String staOrgId = this.pubFunc.getStaffOrgName(sheetObj.getRcvStaffId());
				String staOrgName = this.pubFunc.getOrgName(staOrgId);
				if(isSys){
				    newSheetPubInfo.setRetOrgId(staOrgId);
			        newSheetPubInfo.setRetOrgName(staOrgName);
			        newSheetPubInfo.setRetStaffId(sheetObj.getRcvStaffId());
			        newSheetPubInfo.setRetStaffName(sheetObj.getRcvStaffName());
			        staffId = sheetObj.getRcvStaffId();
		            staffName = sheetObj.getRcvStaffName();
		            dealorgId = staOrgId;
		            dealorgName = staOrgName;
				}
				newSheetPubInfo.setRcvOrgId(staOrgId);
				newSheetPubInfo.setRcvOrgName(staOrgName);
				newSheetPubInfo.setRcvStaffId(sheetObj.getRcvStaffId());
				newSheetPubInfo.setRcvStaffName(sheetObj.getRcvStaffName());
				newSheetPubInfo.setDealOrgId(staOrgId);
				newSheetPubInfo.setDealOrgName(staOrgName);
				newSheetPubInfo.setDealStaffId(sheetObj.getRcvStaffId());
				newSheetPubInfo.setDealStaffName(sheetObj.getRcvStaffName());
                int sheetStatu = isNewCmp ? StaticData.WKST_DEALING_STATE_NEW:StaticData.WKST_ORGDEALING_STATE;
				newSheetPubInfo.setSheetStatu(sheetStatu);
				newSheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(sheetStatu));
				newSheetPubInfo.setLockFlag(1);
				//判断是否为主单单位
				if(sheetObj.getMainType() == 1) {
					mainOrgStr.append(staOrgName + "(" + newSheetPubInfo.getDealStaffName() + ")");
				} else {
					assitOrgStr.append(staOrgName + "(" + newSheetPubInfo.getDealStaffName() + ")" + "; ");
				}
			} else {
				newSheetPubInfo.setRcvOrgId(sheetObj.getRcvOrgId());
				newSheetPubInfo.setRcvOrgName(sheetObj.getRcvOrgName());
				int sheetStatu = isNewCmp ? StaticData.WKST_REPEAL_STATE_NEW:StaticData.WKST_REPEAL_STATE;
                newSheetPubInfo.setSheetStatu(sheetStatu);
                newSheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(sheetStatu));
				newSheetPubInfo.setLockFlag(0);
				
				// 申诉投诉流向工单池
				if(!pubFunc.isYueJi(channel)) {
				    // 转派工单
				    String result = workSheetAllot.allotToDeal(newSheetPubInfo);
				    sendType = WorkSheetAllot.RST_SUCCESS.equals(result) ? 1 : 0;
				}
				//判断是否为主单单位
				if(sheetObj.getMainType() == 1) {
					mainOrgStr.append(sheetObj.getRcvOrgName() + "( )");
				} else {
					assitOrgStr.append(sheetObj.getRcvOrgName() + "( ); ");
				}
			}
			
			//保存产生的部门处理工单的工单数组,给派单关系表用
			strSheetList[i] = newSheetId;
			//为新加收单地域
			int recRegion = this.pubFunc.getOrgRegion(newSheetPubInfo.getRcvOrgId());
			String recRegionName = this.pubFunc.getRegionName(recRegion);
			newSheetPubInfo.setReceiveRegionId(recRegion);
			newSheetPubInfo.setReceiveRegionName(recRegionName);
			int mainFalg = 0;
			if(sheetObj.getMainType() == 1) {
				mainFalg=1;
				strSheetId = newSheetId;
			}	
			newSheetPubInfo.setMainType(mainFalg);
			// 2020-4
			if (pubFunc.isNewWorkFlow(serviceOrderId)) {
				String upWorkSheetId = sheetInfo.getWorkSheetId();
				String upRcvOrgId = sheetInfo.getRcvOrgId();
				if (isCurCheckSheet) {
					SheetPubInfo upSheetInfo = this.sheetPubInfoDao.getSheetPubInfo(sheetInfo.getSourceSheetId(), false);
					upWorkSheetId = upSheetInfo.getWorkSheetId();
					upRcvOrgId = upSheetInfo.getRcvOrgId();
				}
				if (sheetPubInfoDao.selectWorkSheetAreaBySheetId(upWorkSheetId) == 0) {
					//未编写逻辑
				} else { // 主办单流程下的主办单
					if (mainFalg == 1) {// 主办
						if (pubFunc.getAreaOrgId(upRcvOrgId).equals(pubFunc.getAreaOrgId(newSheetPubInfo.getRcvOrgId()))) { // 非跨二级部门派单
							sheetPubInfoDao.insertWorkSheetArea(serviceOrderId, newSheetPubInfo.getWorkSheetId(),
									pubFunc.getAreaOrgId(newSheetPubInfo.getRcvOrgId()), 2);
							count = 1; // 审批
						} else {
							newSheetPubInfo.setPrecontractSign(1);
							if (isNewCmp) {
								newSheetPubInfo.setSheetType(720130013);
								newSheetPubInfo.setSheetTypeDesc(this.pubFunc.getStaticName(720130013));
							}
							sheetPubInfoDao.insertWorkSheetArea(serviceOrderId, newSheetPubInfo.getWorkSheetId(),
									pubFunc.getAreaOrgId(newSheetPubInfo.getRcvOrgId()), 1);
							complaintImpl.complaintPostInfo(3, serviceOrderId);
						}
					} else { // 协办
					}
				}
				newSheetPubInfo.setSheetRcvDate(sheetPubInfoDao.selectSheetReceiveDate(sheetInfo.getWorkSheetId()));
				newSheetPubInfo.setDealLimitTime(sheetInfo.getDealLimitTime());
				newSheetPubInfo.setStationLimit(sheetInfo.getDealLimitTime());
				this.sheetPubInfoDao.saveSheetPubInfo(newSheetPubInfo);
			} else {
				count += this.sheetPubInfoDao.saveSheetPubInfo(newSheetPubInfo);
			}
			sendNoteCont(newSheetPubInfo,sendType,0,autoStaffId);
		}
		if(count > 0) {
			//生成一张部门审批工单
			newSheetPubInfo.setFlowSequence(flowSeq+(size+1));//流水号 flowSeq
			newSheetId = pubFunc.crtSheetId(sheetPubInfo.getRegionId());
			newSheetPubInfo.setWorkSheetId(newSheetId);
			//取派发员工所在部门本地网地域作为收单地域
			int autRecRegion = this.pubFunc.getOrgRegion(dealorgId);
			String autRecRegionName = this.pubFunc.getRegionName(autRecRegion);
			newSheetPubInfo.setReceiveRegionId(autRecRegion);
			newSheetPubInfo.setReceiveRegionName(autRecRegionName);			
			newSheetPubInfo.setSourceSheetId(strSheetId);//处理工单
			
			int tmpStatu = isNewCmp ? StaticData.WKST_ALLOT_STATE_NEW : StaticData.WKST_ALLOT_STATE;
			int tmpType = isNewCmp ? StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW : StaticData.SHEET_TYPE_TS_CHECK_DEAL;
            newSheetPubInfo.setSheetStatu(tmpStatu);
            newSheetPubInfo.setSheetSatuDesc(pubFunc.getStaticName(tmpStatu));
            newSheetPubInfo.setSheetType(tmpType);
            newSheetPubInfo.setSheetTypeDesc(pubFunc.getStaticName(tmpType));

			if (pubFunc.isNewWorkFlow(serviceOrderId)) {
				//未编写逻辑
			} else {
			long dealTime = 0;
			dealTime = PubFunc.getTimeBetween(sheetInfo.getLockDate(), this.pubFunc.getSysDate(), 1);
			int time = sheetInfo.getDealLimitTime() - ((int)dealTime);
			time = (time < 0 ? 0 : time); // 审批单的时限非负
			newSheetPubInfo.setDealLimitTime(time);//审批单为原处理时限减去已处理时限
			newSheetPubInfo.setStationLimit(time);//审批单为原处理时限减去已处理时限
			}
			//如果为派单的工单,部门处理工单不是通过转派的工单 precontractSign标志为1
			newSheetPubInfo.setPrecontractSign(0);
			//审批单清理收到员工信息，设置收单部门为上级工单的处理部门
			newSheetPubInfo.setRcvStaffId(0);
			newSheetPubInfo.setRcvStaffName(" ");
			newSheetPubInfo.setRcvOrgId(dealorgId);
			newSheetPubInfo.setRcvOrgName(dealorgName);
			//审批单清除处理部门
			newSheetPubInfo.setDealStaffId(0);
			newSheetPubInfo.setDealStaffName(" ");
			newSheetPubInfo.setDealOrgId(" ");
			newSheetPubInfo.setDealOrgName(" ");
			newSheetPubInfo.setLockFlag(0);
			newSheetPubInfo.setMainType(1);
			this.sheetPubInfoDao.saveSheetPubInfo(newSheetPubInfo);	
			// 2020-4
			if (pubFunc.isNewWorkFlow(serviceOrderId)) {
				WorkSheetAllotReal workSheetAllo = null;
				workSheetAllo = new WorkSheetAllotReal();
				workSheetAllo.setWorkSheetId(strSheetId);
				workSheetAllo.setCheckWorkSheet(newSheetId);
				if (isCurCheckSheet) {
					workSheetAllo.setPreDealSheet(allotWorkSheet);
				} else if (sheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_DEAL_NEW) {
					workSheetAllo.setPreDealSheet("0");
				} else if (sheetInfo.getPrecontractSign() == 1) {
					workSheetAllo.setPreDealSheet("0");
				} else {
					workSheetAllo.setPreDealSheet(sheetInfo.getWorkSheetId());
				}
				workSheetAllo.setCheckFalg(0);
				workSheetAllo.setMainSheetFlag(1);
				workSheetAllo.setDealStauts("待处理");
				workSheetAllo.setMonth(sheetInfo.getMonth());
				workSheetAllo.setOrderId(serviceOrderId);
				this.workSheetAlllot.saveWorkSheetAllotReal(workSheetAllo, true);
			} else {
			//保存到派单关系表中
			WorkSheetAllotReal workSheetAllo = null;
			for(int j = 0;j<size;j++) {
				workSheetAllo = new WorkSheetAllotReal();
				int mainFalg = 0;
				if(strSheetId.equals(strSheetList[j])) {
			        mainFalg = 1;
				}
				workSheetAllo.setWorkSheetId(strSheetList[j]);
				workSheetAllo.setCheckWorkSheet(newSheetId);
				//如果为审批工单,新产生的审批单记录现审批的上级工单
				if(isCurCheckSheet) {
					workSheetAllo.setPreDealSheet(allotWorkSheet);
				} else if(sheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_DEAL_NEW){
				    if(sheetInfo.getMainType() == 1){
				        workSheetAllo.setPreDealSheet("0");
				    }else{
				        // "非主办、部门处理单" 转派生成的主单派单关系记录,PRE_DEAL_WORKSHEET_ID字段置为1
				        workSheetAllo.setPreDealSheet("1");
				    }
				}else{
				    workSheetAllo.setPreDealSheet(sheetInfo.getWorkSheetId());
				}
				
				workSheetAllo.setCheckFalg(0);
				workSheetAllo.setMainSheetFlag(mainFalg);
				workSheetAllo.setDealStauts("待处理");
				workSheetAllo.setMonth(sheetInfo.getMonth());
				workSheetAllo.setOrderId(serviceOrderId);
				this.workSheetAlllot.saveWorkSheetAllotReal(workSheetAllo, true);				
			}
			}
		}
			int tmpFinState = isNewCmp ? StaticData.WKST_FINISH_STATE_NEW : StaticData.WKST_FINISH_STATE;
			String stateDesc = pubFunc.getStaticName(tmpFinState);
			sheetPubInfoDao.updateSheetState(sheetPubInfo.getWorkSheetId(),tmpFinState, stateDesc,sheetInfo.getMonth(),2);
			sheetPubInfoDao.updateSheetFinishDate(sheetPubInfo.getWorkSheetId());
			if(sheetInfo.getPrecontractSign() == 1){
			    labelManageDao.saveFormalAnswerDate(serviceOrderId);
			}
			
			orgNamelist = mainOrgStr.toString() + assitOrgStr.toString();
			//记录处理类型
			TsSheetDealType typeBean = new TsSheetDealType();
			int tmpNxtTach = isNewCmp ? StaticData.TACHE_DEAL_NEW : StaticData.TACHE_DEAL;
			if(dealType == 0) {
				sheetPubInfoDao.updateSheetDealRequire(sheetInfo.getWorkSheetId(), sheetInfo.getDealRequire(),orgNamelist,"部门转派",dealResult,1,tmpNxtTach);
				typeBean.setDealType("部门处理转派工单");
			}else if(dealType == 4) {
				sheetPubInfoDao.updateSheetDealRequire(sheetInfo.getWorkSheetId(), sheetInfo.getDealRequire(),orgNamelist,"部门审批退单",dealResult,20,tmpNxtTach);
				typeBean.setDealType("部门审批退单");
			}
			String guid = pubFunc.crtGuid();
			typeBean.setDealTypeId(guid);
			typeBean.setOrderId(serviceOrderId);
			typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
			typeBean.setDealTypeDesc("部门分派");
			typeBean.setDealId(0);//处理定性ID 如果为审批单,0为不同意,1为同意
			typeBean.setDealDesc(orgNamelist);//处理定性名
			typeBean.setDealContent(dealResult);//处理内容
			typeBean.setMonth(sheetInfo.getMonth());
			tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型
			jtxcCancel(sheetInfo.getWorkSheetId());
			return "SUCCESS";
	}

	private TsmStaff getDealStaff(int autoStaffId) {
		if (0 == autoStaffId) {
			return pubFunc.getLogonStaff();
		} else {
			return pubFunc.getStaff(autoStaffId);
		}
	}

	/**
	 * 保存工单动作
	 * @param sheetActionInfo 工单动作对象
	 * @param tacheId 所处环节
	 * @param actionType 动作类型
	 * @param type 动作 1为自动派发 2为修改投诉定单内容,3为人工派发 4为强制释放工单
	 * @return
	 */
	private boolean saveSheetDealAction(SheetActionInfo sheetActionInfo,int tacheId,int actionType,int type) {
		//取当前登录员工信息
		TsmStaff staff = this.pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String orgId = staff.getOrganizationId();
		String orgName = staff.getOrgName();
		
		String guid = pubFunc.crtGuid();
		sheetActionInfo.setActionGuid(guid);
		sheetActionInfo.setTacheId(tacheId);
		sheetActionInfo.setTacheName(pubFunc.getStaticName(tacheId));
		sheetActionInfo.setActionCode(actionType);
		sheetActionInfo.setActionName(pubFunc.getStaticName(actionType));
		if(type != 1) {
			sheetActionInfo.setOpraOrgId(orgId);
			sheetActionInfo.setOpraOrgName(orgName);
			sheetActionInfo.setOpraStaffId(staffId);
			sheetActionInfo.setOpraStaffName(staffName);			
		}
		this.sheetActionInfoDao.saveSheetActionInfo(sheetActionInfo);
		return true;
	}
	
	/**
	 * 投诉工单部门处理完成提交方法
	 * 
	 * @param sheetPubInfo 被处理的工单的信息，包含：工单号，定单号，地域，月分区，处理内容
	 * @param tsdealQualitative 部门处理定性信息 
	 * @param tsSheetAuditing 
	 * @param tsassess 
	 * @param delalId 处理定性ID
	 * @param dealName 处理定性名
	 * @param servContent 投诉单内容
	 * 
	 * @return 描述处理结果的字符串
	 */
	@SuppressWarnings("all")
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public String sumbitOrgDeal(SheetPubInfo sheetPubInfo, TsSheetAuditing tsSheetAuditing, int delalId, String dealName) {
		String curSheetId = sheetPubInfo.getWorkSheetId();
		int curRegionId = sheetPubInfo.getRegionId();
		Integer curMonth = sheetPubInfo.getMonth();
		
		// 通过工单的基本信息获取其完整信息
		SheetPubInfo curSheetInfo = sheetPubInfoDao.getSheetObj(curSheetId,curRegionId,curMonth, true);
		// 非处理中状态的单子不能提交
		if (curSheetInfo.getLockFlag() != 1) {
			return "statusError";
		}
		// 挂起的单子不能提交
		if(ComplaintDealHandler.isHoldSheet(curSheetInfo.getSheetStatu())) {
			return "statusError";
		}
		
		String allotWorkSheet = ""; // 审批上级工单
		WorkSheetAllotReal workSheetAllotReal = new WorkSheetAllotReal(); // 更新派单关系表审批状态或处理状态
		
		if(StaticData.SHEET_TYPE_TS_CHECK_DEAL == curSheetInfo.getSheetType()) {
			// 查询本审批单对应的派单关系记录
			WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(curSheetInfo);
			if(sheetAllotRealList.length > 0){
				WorkSheetAllotReal sheetAllotRealobj = null;
				for(int j = sheetAllotRealList.length-1; j >= 0; j--){
					sheetAllotRealobj = sheetAllotRealList[j];
					if(sheetAllotRealobj.getMainSheetFlag() == 1){
						if(sheetAllotRealobj.getCheckFalg() == 0){
							return "ALLOTREAL";
						}
						// 获得上级工单的工单号
						allotWorkSheet = sheetAllotRealobj.getPreDealSheet();
						break;
					}
				}
			}
			workSheetAllotReal.setDealStauts("审批通过");
			workSheetAllotReal.setWorkSheetId(allotWorkSheet);
		} else {
			workSheetAllotReal.setDealStauts("处理完成");
			workSheetAllotReal.setWorkSheetId(curSheetId);
		}
		workSheetAllotReal.setMonth(curMonth);
		workSheetAllotReal.setCheckFalg(1);
		// 更新派单关系
		workSheetAlllot.updateWorkSheetAllotReal(workSheetAllotReal);
		
		// 根据处理工单号，得到派单关系对象
		WorkSheetAllotReal sheetAllotObj = workSheetAlllot.getSheetAllotObj(workSheetAllotReal.getWorkSheetId(), curMonth);
        if (null == sheetAllotObj) {
			if (pubFunc.isNewWorkFlow(curSheetInfo.getServiceOrderId())) {
				boolean updateSH = false;
				if (StaticData.SHEET_TYPE_TS_DEAL == curSheetInfo.getSheetType()) {
					if (curSheetInfo.getMainType() == 1 && curSheetInfo.getPrecontractSign() == 1) {
						updateSH = true;
					}
				} else if (StaticData.SHEET_TYPE_TS_CHECK_DEAL == curSheetInfo.getSheetType()) {
					if (canFinishAuto(curSheetId)) {
						updateSH = true;
					}
				}
				if (updateSH) {
					sheetPubInfoDao.updateWorkSheetAreaTacheDate(curSheetInfo.getServiceOrderId());
					SheetPubInfo audSheet = sheetPubInfoDao.getAuditSheetNew(curSheetInfo.getServiceOrderId());
					sheetPubInfoDao.updateSheetState(audSheet.getWorkSheetId(), StaticData.WKST_AUD_STATE, pubFunc.getStaticName(StaticData.WKST_AUD_STATE),
							14, 0);
				}
			}
        } else {
			// 根据上级派单关系对象的check工单ID，得到待审批/核工单的对象
			SheetPubInfo orderSheetInfo = sheetPubInfoDao.getSheetObj(sheetAllotObj.getCheckWorkSheet(),curRegionId,curMonth, true);
			int orderTach = 0;	// 审批或审核工单的环节
			int orderSheetType = 0;
			if(orderSheetInfo != null) {
				orderTach = orderSheetInfo.getTacheId();
				orderSheetType = orderSheetInfo.getSheetType();
			}
			
			// 派单关系中的主办标志为'主办'
			if(this.isMainSheetFlag(sheetAllotObj, orderSheetInfo)){
			    String result = "";
			    int sendType = -1;
				// 待审单为'审批单'
				if(orderSheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
				    // 投诉单处理完成 自动回单
				    result = workSheetAllot.allotToApprove(orderSheetInfo);
				    if(WorkSheetAllot.RST_NONE.equals(result)){
				        orderSheetInfo.setSheetStatu(StaticData.WKST_ORGAUD_STATE);
				        orderSheetInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_ORGAUD_STATE));
				        orderSheetInfo.setMonth(14);
				        orderSheetInfo.setLockFlag(0);
				        sendType = 0;
				    }else{
				        sheetPubInfoDao.updateFetchSheetStaff(orderSheetInfo.getWorkSheetId(), orderSheetInfo.getRcvStaffId(),
				                orderSheetInfo.getRcvStaffName(),orderSheetInfo.getRcvOrgId(),orderSheetInfo.getRcvOrgName());
				        sendType = 1;
				    }
				    sheetPubInfoDao.updateSheetState(orderSheetInfo.getWorkSheetId(),orderSheetInfo.getSheetStatu(),
	                        orderSheetInfo.getSheetSatuDesc(), 14, orderSheetInfo.getLockFlag());
				}else if(orderTach == StaticData.TACHE_AUIT) { // 待审单所处环节为'后台审核'
				    orderAskInfoDao.updateOrderStatu(curSheetInfo.getServiceOrderId(),
				            StaticData.OR_WAIT_DEAL_STATU,curMonth,this.pubFunc.getStaticName(StaticData.OR_WAIT_DEAL_STATU));
	
					    result = workSheetAllot.allotToVerify(orderSheetInfo);
						if(WorkSheetAllot.RST_NONE.equals(result)){
						    orderSheetInfo.setSheetStatu(StaticData.WKST_AUD_STATE);
			                orderSheetInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_AUD_STATE));
			                orderSheetInfo.setMonth(14);
			                orderSheetInfo.setLockFlag(0);
			                sendType = 0;
						}else{
						    sheetPubInfoDao.updateFetchSheetStaff(orderSheetInfo.getWorkSheetId(), orderSheetInfo.getRcvStaffId(),
						            orderSheetInfo.getRcvStaffName(),orderSheetInfo.getRcvOrgId(),orderSheetInfo.getRcvOrgName());
						    sendType = 1;
						}
		                sheetPubInfoDao.updateSheetState(orderSheetInfo.getWorkSheetId(),orderSheetInfo.getSheetStatu(),
		                        orderSheetInfo.getSheetSatuDesc(), 14, orderSheetInfo.getLockFlag());
					
				}
				if(sendType >= 0){
				    sendNoteCont(orderSheetInfo,sendType,curSheetInfo.getDealStaffId(),0);
				}
			}
        }
		String stateDesc = pubFunc.getStaticName(StaticData.WKST_FINISH_STATE);
		sheetPubInfoDao.updateSheetState(curSheetId,StaticData.WKST_FINISH_STATE, stateDesc,curMonth,2);
		sheetPubInfoDao.updateSheetFinishDate(curSheetId);
			   
		//记录处理类型
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(curSheetInfo.getServiceOrderId());
		typeBean.setWorkSheetId(curSheetInfo.getWorkSheetId());
		typeBean.setDealType("部门处理回单");
		typeBean.setDealTypeDesc("处理定性");
		typeBean.setDealId(delalId);//处理定性ID
		typeBean.setDealDesc(dealName);//处理定性名
		typeBean.setDealContent(sheetPubInfo.getDealContent());//处理内容
		typeBean.setMonth(curSheetInfo.getMonth());		
		
		if(curSheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
			typeBean.setDealType("部门审批回单");
			typeBean.setDealTypeDesc("审批意见");
			typeBean.setDealId(1);//处理定性ID 如果为审批单,0为不同意,1为同意
			typeBean.setDealDesc("审批同意");//处理定性名			 
			saveDealOrgTrait(curSheetInfo, tsSheetAuditing);
		}
		
		//如果为已经处理
		if(curSheetInfo.getPrecontractSign() == 1 && curSheetInfo.getSheetType() != StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
		    labelManageDao.saveFormalAnswerDate(curSheetInfo.getServiceOrderId());
			saveDealOrgTrait(curSheetInfo, tsSheetAuditing);
		}			
		tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型
		
		//更新操作,10代表部门处理完成,没流向下一环节
		sheetPubInfoDao.updateSheetDealRequire(curSheetId, curSheetInfo.getDealRequire(),
				" ", "部门处理环节回单", sheetPubInfo.getDealContent(), 6, 10);
		jtxcCancel(curSheetId);
		return "success";
	}
	
	private boolean isMainSheetFlag(WorkSheetAllotReal sheetAllotObj, SheetPubInfo orderSheetInfo) {
		return sheetAllotObj.getMainSheetFlag() == 1 && orderSheetInfo != null;
	}
	
	/**
	 * 审核单处理完成提交<br>
	 * 修改受理信息；更新工单、订单状态；记录处理类型；记录审核信息；记录责任部门；记录回访信息；标签库与判定；提交工作流
	 * 
	 * @param sheetPubInfo 工单对象
	 * @param tsSheetAuditing 审核对象
	 * @param tscustomerVisit 回访对象
	 * @param sumbitType 0为竣工 1到考核环节 2 为到归档环节 
	 * @param servContent 受理内容
	 * @param dutyOrg 责任部门对象数组
	 * @return 结果字符串
	 */
	public String audSheetFinish(SheetPubInfo sheetPubInfo, TsSheetAuditing tsSheetAuditing, TScustomerVisit tscustomerVisit, 
		int sumbitType, ServiceContent servContent, ResponsiBilityOrg[] dutyOrg){
		logger.info("audSheetFinish  ======> \n{}", sheetPubInfo);
		String sheetId = sheetPubInfo.getWorkSheetId();
		int regionId = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth(); 
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId,regionId,month,hisFlag);
		if (1 != sheetInfo.getLockFlag()) {
			return "STATUERROR"; // 非处理中状态的单子不能提交
		}
		if(ComplaintDealHandler.isHoldSheet(sheetInfo.getSheetStatu())) {
			return "STATUERROR";
		}
		String flowSeq = "1"; // 流水号
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;
		String orderId = sheetPubInfo.getServiceOrderId();
		/*
		 * 记录处理类型 CC_WORKSHEET_DEAL_TYPE
		 */
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(orderId);
		typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
		typeBean.setDealType("审核工单竣工");
		typeBean.setDealTypeDesc("审核意见");
		typeBean.setDealId(1);//处理定性ID 审核单,0为不同意,1为同意
		typeBean.setDealDesc("审核同意");//处理定性名
		typeBean.setDealContent(sheetPubInfo.getDealContent());//处理内容
		typeBean.setMonth(sheetInfo.getMonth());
		this.tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型
		/*
		 * 记录审核信息  CC_SHEET_AUDITING
		 */
		tsSheetAuditing.setRegName(sheetInfo.getRegionName());
		tsSheetAuditing.setTacheId(sheetInfo.getTacheId());
		tsSheetAuditing.setTacheName(sheetInfo.getTacheDesc());
		tsSheetAuditing.setSheetType(sheetInfo.getSheetType());
		tsSheetAuditing.setSheetTypeDesc(sheetInfo.getSheetTypeDesc());
		tsSheetAuditing.setMonthFlag(sheetInfo.getMonth());
		this.tsSheetAuditingDaoImpl.saveTsSheetAuditing(tsSheetAuditing);
		/*
		 * 记录责任部门 CC_RESPONSIBILITY_ORG
		 */
		if(dutyOrg.length > 0){
			int dutySize = tsSheetAuditingDaoImpl.saveResponsiBilityOrg(dutyOrg);
			logger.debug("新增责任部门: " + dutySize +"条");
		}		
		/*
		 * 记录回访信息  CC_CUSTOMER_VISIT
		 */
		if (null != tscustomerVisit) {
			tscustomerVisit.setRegionName(sheetInfo.getRegionName());
			tscustomerVisit.setMonth(sheetInfo.getMonth());
			this.customerVisit.saveCustomerVisit(tscustomerVisit);
			labelManageDao.updateDealResult(orderId, tscustomerVisit.getTsDealResult(), tscustomerVisit.getTsDealResultName());
		}
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId,sheetInfo.getDealRequire(),"","审核工单竣工",sheetPubInfo.getDealContent(),21,StaticData.TACHE_FINISH);
		//记录定单竣工的环节
		this.orderAskInfoDao.updateFinTache(orderId, sheetInfo.getRegionId(), sheetInfo.getTacheId());

		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		logger.info("before autoVisitSH  ======> \n{}", orderInfo);
		if (autoVisitSH(orderInfo, sheetId)) {
	        String finDate = labelManageDao.queryFinishDate(orderId);
	        logger.info("order：{} finDate：{}", orderId, finDate);
	        if (StringUtils.isEmpty(finDate)) {
	            // 在订单标签表中，记录订单处理完成的时间
	            labelManageDao.saveFinishDate(orderId);
	            dapdSheetService.setDapdEndDate(orderId);
	            // 记录是否超时
	            labelManageDao.updateOverTimeLabel(orderId);
				ComplaintUnifiedReturn cur = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(orderId);
				if (null != cur) {
					updateIVRDegree(orderInfo, sheetId); // 上传集团之前更新IVR满意度
					compFull.insertSupplement(orderId);
				}
	        }
	        // 提交工作流
			Map otherParam = new HashMap();
			String routeValue = StaticData.ROUTE_AUD_TO_FINISH; // 默认为结束
			otherParam.put("ROUTE_VALUE", routeValue);
			otherParam.put("MONTH_FALG", month);		
			otherParam.put("DEAL_PR_ORGID", sheetInfo.getDealOrgId());//派发部门
			otherParam.put("DEAL_PR_STAFFID", sheetInfo.getDealStaffId());//派发员工
			otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeqNo));//流水号
			workSheetFlowService.submitWorkFlow(sheetId, regionId, otherParam);
			accessClique(orderId);
		}
		compFull.updateFullPayDateByNewOrderId(orderId);
		return "SUCCESS";
	}

	private boolean autoVisitSH(OrderAskInfo orderInfo, String sheetId) {
		String orderId = orderInfo.getServOrderId();
		int autoVisitFlag = 0;
		int reportNum = 0;
		if (labelManageDao.selectAutoVisitFlag(orderId) == 0) {// 这是订单第一次进入自动回访
			String unifiedComplaintCode = orderId;
			int tacheType = 3;
			if (StaticData.SERV_TYPE_CX == orderInfo.getServType()) {
				tacheType = 6;
			} else {
				ComplaintUnifiedReturn cur = cmpUnifiedReturnDAOImpl.queryUnifiedReturnByOrderId(orderId);
				if (null == cur) { // 没有集团编码
					autoVisitFlag = 4;
					reportNum = 10;
					sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 2, sheetId);
					return true;
				}
				if ("0".equals(cur.getResult()) && cur.getUnifiedComplaintCode().length() > 0) {// 没有集团编码
				} else {
					autoVisitFlag = 4;
					reportNum = 10;
					sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 2, sheetId);
					return true;
				}
				unifiedComplaintCode = cur.getUnifiedComplaintCode();
			}
			labelManageDao.updateAutoVisitFlag(1, orderId);
			sheetPubInfoDao.insertCustomerJudge(orderId, unifiedComplaintCode, tacheType);
			orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_AUTOVISIT_STATU, orderInfo.getMonth(), pubFunc.getStaticName(StaticData.OR_AUTOVISIT_STATU));
			sheetPubInfoDao.updateSheetState(sheetId, StaticData.WKST_FINISH_STATE, pubFunc.getStaticName(StaticData.WKST_FINISH_STATE), orderInfo.getMonth(), 2);
			sheetPubInfoDao.updateSheetFinishDate(sheetId);
			sheetPubInfoDao.updateTachSheetFinsh(orderId, StaticData.WKST_FINISH_STATE, pubFunc.getStaticName(StaticData.WKST_FINISH_STATE), 2, orderInfo.getMonth(), StaticData.TACHE_DEAL);
			sheetPubInfoDao.updateAutoVisit(autoVisitFlag, reportNum, 2, sheetId);
			return false;
		}
		return true;
	}

	private void accessClique(String orderId) {
		ComplaintRelation cmpRelaOper = pubFunc.queryListByOid(orderId);
		if (cmpRelaOper != null) {
			if (cmpRelaOper.getAssignType() == 2) {
				accessCliqueServiceFeign.updateRelaStatu(cmpRelaOper.getRelaGuid(), 20, "省已处理");
			} else if (cmpRelaOper.getAssignType() == 3 || "C003021".equals(cmpRelaOper.getAskSourceSrl()) 
					//新增工信部立案、工信部预处理集团工单，省内处理后可自行归档集团
					|| "C003002".equals(cmpRelaOper.getAskSourceSrl()) || "C003004".equals(cmpRelaOper.getAskSourceSrl())) {
				JSONObject info = new JSONObject();
				info.put("complaintWorksheetId", cmpRelaOper.getComplaintWorksheetId());
				info.put("serviceOrderId", orderId);
				info.put("reason", "省内已处理，请集团归档");
				info.put("type", "FINISH");
				accessCliqueServiceFeign.accessCliqueNew(info.toString());
			}
		}
	}

    private void updateIVRDegree(OrderAskInfo orderInfo, String workSheetId) {
		List crs = sheetPubInfoDao.selectCalloutRecByOrderId(orderInfo.getServOrderId());
		if (!crs.isEmpty()) {
			int reportNum = 1;
			Map cr = (Map) crs.get(0);
			String satisfyDegree = cr.get("SATISFY_DEGREE").toString();
			String judgeDate = cr.get("JUDGE_DATE") == null ? "" : cr.get("JUDGE_DATE").toString();
			String tsVisitResult = "";
			switch (satisfyDegree) {
			case "1": // 满意
				reportNum = 5;
				tsVisitResult = "即时测评结果满意";
				invitedJudgeSH(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "2":// 一般
				reportNum = 3;
				tsVisitResult = "即时测评结果一般";
				invitedJudgeSH(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "3":// 服务态度冷淡
				tsVisitResult = "服务态度冷淡";
				invitedJudgeSH(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "4":// 业务解释听不懂
				tsVisitResult = "业务解释听不懂";
				invitedJudgeSH(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "5":// 处理速度慢
				tsVisitResult = "处理速度慢";
				invitedJudgeSH(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "6":// 处理方案未达到期望值
				tsVisitResult = "处理方案未达到期望";
				invitedJudgeSH(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			case "7":// 问题未解决
				tsVisitResult = "问题未解决";
				invitedJudgeSH(orderInfo, workSheetId, reportNum, judgeDate, tsVisitResult);
				break;
			default:// 未评价
			}
		}
	}

	private void invitedJudgeSH(OrderAskInfo orderInfo, String workSheetId, int score, String replyData, String tsVisitResult) {
		TScustomerVisit cv = new TScustomerVisit();
		cv.setServiceOrderId(orderInfo.getServOrderId());
		cv.setWorkSheetId(workSheetId);
		cv.setMonth(orderInfo.getMonth());
		cv.setRegionId(orderInfo.getRegionId());
		cv.setRegionName(orderInfo.getRegionName());
		if ("".equals(replyData)) {
			cv.setReplyData(pubFunc.getSysDate());
		} else {
			cv.setReplyData(replyData);
		}
		int collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BPJ;// 总体情况
		int tsDealResult = StaticData.TS_DEAL_RESULT_BPJ;// 投诉处理结果
		int tsDealBetimes = StaticData.TS_DEAL_BETIMES_BPJ;// 投诉处理及时性
		int tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BPJ;// 投诉处理态度
		if (5 == score) { // 满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_MY;
			tsDealResult = StaticData.TS_DEAL_RESULT_MY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_MY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_MY;
		} else if (1 == score) { // 不满意
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_BMY;
			tsDealResult = StaticData.TS_DEAL_RESULT_BMY;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_BMY;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_BMY;
		} else if (3 == score) { // 一般
			collectivityCircs = StaticData.COLLECTIVITY_CIRCS_YB;
			tsDealResult = StaticData.TS_DEAL_RESULT_YB;
			tsDealBetimes = StaticData.TS_DEAL_BETIMES_YB;
			tsDealAttitude = StaticData.TS_DEAL_ATTITUDE_YB;
		}
		cv.setCollectivityCircs(collectivityCircs);
		cv.setCollectivityCircsName(pubFunc.getStaticName(collectivityCircs));
		cv.setTsDealAttitude(tsDealAttitude);
		cv.setTsDealAttitudeName(pubFunc.getStaticName(tsDealAttitude));
		cv.setTsDealBetimes(tsDealBetimes);
		cv.setTsDealBetimesName(pubFunc.getStaticName(tsDealBetimes));
		cv.setTsDealResult(tsDealResult);
		cv.setTsDealResultName(pubFunc.getStaticName(tsDealResult));
		cv.setTsVisitResult(tsVisitResult);
		cv.setVisitType("3");
		customerVisit.saveCustomerVisit(cv);
		labelManageDao.updateDealResult(orderInfo.getServOrderId(), cv.getTsDealResult(), cv.getTsDealResultName());
	}

	public void audSheetFinishVisit(String sheetId, String orderId, int region, Integer month) {
		orderAskInfoDao.updateOrderStatu(orderId, StaticData.OR_WAIT_DEAL_STATU, month, pubFunc.getStaticName(StaticData.OR_WAIT_DEAL_STATU));// 更新订单状态为终定性
		SheetPubInfo sheetInfo = sheetPubInfoDao.getSheetObj(sheetId, region, month, true);
		String flowSeq = pubFunc.crtFlowSeq(sheetInfo.getFlowSequence(), "1", 1);
		String dealOrg = sheetInfo.getDealOrgId();
		String dealStaffId = sheetInfo.getDealStaffId() + "";
		Map otherParam = new HashMap();
		otherParam.put("ROUTE_VALUE", StaticData.ROUTE_AUD_TO_FINISH);
		otherParam.put("MONTH_FALG", sheetInfo.getMonth());
		otherParam.put("DEAL_PR_ORGID", dealOrg);
		otherParam.put("DEAL_PR_STAFFID", dealStaffId);
		otherParam.put("FLOW_SEQUENCE", String.valueOf(flowSeq));
		workSheetFlowService.submitWorkFlow(sheetId, sheetInfo.getRegionId(), otherParam);
		accessClique(orderId);
	}

	@Override
	public boolean finishAudSheetAuto(SheetPubInfo audSheet) {
		logger.info("finishAudSheetAuto  ======> \n{}", audSheet);
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(audSheet.getServiceOrderId(), false);
		if(null == orderInfo){
			return false;
		}
		// TsSheetAuditing
		TsSheetAuditing tsSheetAuditing = new TsSheetAuditing();
		tsSheetAuditing.setOrderId(audSheet.getServiceOrderId());
		tsSheetAuditing.setSheetId(audSheet.getWorkSheetId());
		tsSheetAuditing.setRegionId(audSheet.getRegionId());
		tsSheetAuditing.setRegName(audSheet.getRegionName());
		tsSheetAuditing.setMonthFlag(audSheet.getMonth());
		tsSheetAuditing.setCreatData("");
		tsSheetAuditing.setTsRisk(0);
		tsSheetAuditing.setResoundMass(StaticData.AUDITING_RESOUND_COMMON);
		tsSheetAuditing.setResoundMassName(pubFunc.getStaticName(StaticData.AUDITING_RESOUND_COMMON));
		tsSheetAuditing.setJobError(0);
		tsSheetAuditing.setAssessArticleName("");
		tsSheetAuditing.setSheetType(audSheet.getSheetType());
		tsSheetAuditing.setSheetTypeDesc(audSheet.getSheetTypeDesc());
		tsSheetAuditing.setTacheId(audSheet.getTacheId());
		tsSheetAuditing.setTacheName(audSheet.getTacheDesc());
		tsSheetAuditing.setDutyOrg("");
		tsSheetAuditing.setDutyOrgName("");
		tsSheetAuditing.setUpgradeTs(0);

		// ServiceContent
		ServiceContent servContent = new ServiceContent();
		servContent.setServOrderId(audSheet.getServiceOrderId());
		servContent.setMonth(audSheet.getMonth());
		servContent.setProdNum(orderInfo.getProdNum());
		// dutyOrg
		ResponsiBilityOrg[] dutyOrg = new ResponsiBilityOrg[0];
		return "SUCCESS".equals(audSheetFinish(audSheet, tsSheetAuditing, null, 0, servContent, dutyOrg));
	}

	@Override
	public SheetPubInfo getAudsheet(String orderId) {
		SheetPubInfo audSheet = null;
		orderId = orderId.trim();
		OrderAskInfo order = orderAskInfoDao.getOrderAskInfo(orderId, false);
		if(null != order){
			audSheet = sheetPubInfoDao.getAuditSheet(orderId);
		}
		return audSheet;
	}
	
	public boolean canFinishAuto(String worksheetId){
		SheetPubInfo curSheetInfo = sheetPubInfoDao.getSheetPubInfo(worksheetId, false);
		if(ComplaintDealHandler.isHoldSheet(curSheetInfo.getSheetStatu())){
			return false;
		}
		if(curSheetInfo.getLockFlag() != 1){
			return false;
		}
		if (pubFunc.isNewWorkFlow(curSheetInfo.getServiceOrderId())) {
			return this.isNewFlowCanFinishAuto(curSheetInfo);
		} else {
			return this.isOldFlowCanFinishAuto(curSheetInfo);
		}
	}
	
	private boolean isNewFlowCanFinishAuto(SheetPubInfo curSheetInfo) {
		boolean flag = true;
		WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(curSheetInfo);
		if (sheetAllotRealList.length > 0) {
			WorkSheetAllotReal sheetAllotRealobj = null;
			for (int j = sheetAllotRealList.length - 1; j >= 0; j--) {
				sheetAllotRealobj = sheetAllotRealList[j];
				if (sheetAllotRealobj.getMainSheetFlag() == 1) {
					if (sheetAllotRealobj.getCheckFalg() == 0) {
						return false;
					}
					if ("0".equals(sheetAllotRealobj.getPreDealSheet())) {
						flag = true;
						break;
					} else {
						return false;
					}
				}
			}
		}
		return flag;
	}
	
	private boolean isOldFlowCanFinishAuto(SheetPubInfo curSheetInfo) {
		String allotWorkSheet = null;
		if(StaticData.SHEET_TYPE_TS_CHECK_DEAL == curSheetInfo.getSheetType()){
			// 查询本审批单对应的派单关系记录
			WorkSheetAllotReal[] sheetAllotRealList = dealOrgAudSheet(curSheetInfo);
			if(sheetAllotRealList.length > 0){
				WorkSheetAllotReal sheetAllotRealobj = sheetAllotRealList[0];
				if(sheetAllotRealobj.getCheckFalg() != 0){
					// 获得上级工单的工单号
					allotWorkSheet = sheetAllotRealobj.getPreDealSheet();
				}
			}
		}else{
			allotWorkSheet = curSheetInfo.getWorkSheetId();
		}
		if(allotWorkSheet == null){
			return false;
		}
		// 根据处理工单号，得到派单关系对象
		WorkSheetAllotReal sheetAllotObj = workSheetAlllot.getSheetAllotObj(allotWorkSheet, curSheetInfo.getMonth());
		// 根据上级派单关系对象的check工单ID，得到待审批/核工单的对象
		SheetPubInfo checkSheetInfo = sheetPubInfoDao.getSheetObj(sheetAllotObj.getCheckWorkSheet(),curSheetInfo.getRegionId(),curSheetInfo.getMonth(), true);
		return checkSheetInfo.getSheetType() == StaticData.SHEET_TYPE_TS_AUIT;
	}
	
	/**
	 * 投诉审核环节重新派单
	 * 
	 * @param sheetPubInfo
	 * @param acceptContent 处理内容
	 * @param dealType 处理类型 5 为重新派单 3 为审核退单
	 *            工单对像
	 * @return 是否成功
	 */
	//@Transactional
	public String submitAuitSheetToDeal(SheetPubInfo[] workSheetObj,String acceptContent,int dealType) {
		if(workSheetObj == null) {
			return "ERROR";
		}
		// 投诉审核退单、重派单
		SheetPubInfo sheetPubInfo = workSheetObj[0];
		int size = workSheetObj.length;
		String sheetId = sheetPubInfo.getWorkSheetId();
		String require = sheetPubInfo.getDealRequire();
		int region = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();

		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId, region,month, true);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (state != 1 ) {
			return "STATUSERROR";
		}		
		if(ComplaintDealHandler.isHoldSheet(sheetInfo.getSheetStatu())) {
			return "STATUSERROR";
		}
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;		

		String dealOrg = "";
		String dealStaffId = "0";
		boolean isSys = StaticData.FLAG_SYS_DO.equals(sheetPubInfo.getDealStaffName());
        if(!isSys){
            TsmStaff staff = pubFunc.getLogonStaff();
            dealOrg = staff.getOrganizationId();
            dealStaffId = staff.getId();
        }
		
		String rcvOrgName = "";
		String mainOrg = "主办单位: ";
		String assitOrg="      协办单位: ";
		for(int i=0;i<size;i++) {
			sheetPubInfo = workSheetObj[i];				
			if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
				String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
				rcvOrgName =this.pubFunc.getOrgName(orgId);
			} else {
				rcvOrgName =sheetPubInfo.getRcvOrgName();
				sheetPubInfo.setRcvStaffName(" ");
			}
			//判断是否为主单单位
			if(sheetPubInfo.getMainType() == 1) {
				mainOrg = mainOrg+rcvOrgName+"("+sheetPubInfo.getRcvStaffName()+")";
			} else {
				assitOrg=assitOrg+rcvOrgName+"("+sheetPubInfo.getRcvStaffName()+")"+"; ";
			}				
		}
		rcvOrgName = mainOrg+assitOrg;
			
		//记录处理类型int delalId,String dealName
		TsSheetDealType typeBean = new TsSheetDealType();
		
		//记录操作类型,操作内容
		if(dealType == 5) {//重新派单
			this.sheetPubInfoDao.updateSheetDealRequire(sheetId,require,rcvOrgName,"审核环节重新派发工单",acceptContent ,10,StaticData.TACHE_DEAL);
			typeBean.setDealType("审核环节重新派发工单");
		}
		if(dealType == 3) {//退单
			this.sheetPubInfoDao.updateSheetDealRequire(sheetId,require,rcvOrgName,"审核环节退单",acceptContent ,24,StaticData.TACHE_DEAL);
			typeBean.setDealType("审核环节退单");
		}					
		Map otherParam = new HashMap();	
		otherParam.put("DEAL_REQUIRE",require);
		otherParam.put("ROUTE_VALUE",StaticData.ROUTE_AUIT_TO_DEAL);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工	
		otherParam.put("SHEETARRAY", workSheetObj);
		otherParam.put("PRECONTRACTSIGN", String.valueOf(dealType));//处理类型 5 审核重新派单 3 为审核退单
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		if(3 == dealType){
            WorkSheetAllotReal sheetAllotRealobj = getUpReal(sheetInfo);
            String lastDealWsId = "";
            if(sheetAllotRealobj.getCheckWorkSheet().equals(sheetInfo.getWorkSheetId())){
                lastDealWsId = sheetAllotRealobj.getWorkSheetId(); 
            }else{
                lastDealWsId = sheetAllotRealobj.getCheckWorkSheet();
            }
            SheetPubInfo lastDealWsInfo = this.sheetPubInfoDao.getSheetPubInfo(lastDealWsId, false);
    		// 属于南京客服中心的投诉审核退单
            if(lastDealWsInfo != null && 
            		pubFunc.isAffiliated(lastDealWsInfo.getDealOrgId(), StaticData.ORG_NJ_CUSTOM_SERVICE_CENTRAL)){
                // 如果最后的处理部门属于南京客服中心，则保存最后处理员工信息
                otherParam.put("LAST_DEAL_STAFFID",String.valueOf(lastDealWsInfo.getDealStaffId()));
                otherParam.put("LAST_DEAL_STAFFNAME", lastDealWsInfo.getDealStaffName());
                otherParam.put("LAST_DEAL_ORGID",lastDealWsInfo.getDealOrgId());
                otherParam.put("LAST_DEAL_ORGNAME", lastDealWsInfo.getDealOrgName());
            }
        }
		//提交流程
		workSheetFlowService.submitWorkFlow(sheetId, region, otherParam);

		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
		typeBean.setDealTypeDesc("分派列表");
		typeBean.setDealId(0);//处理定性ID 审核单,0为不同意,1为同意
		typeBean.setDealDesc(rcvOrgName);//处理定性名
		typeBean.setDealContent(acceptContent);//处理内容
		typeBean.setMonth(sheetInfo.getMonth());
		this.tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型
		return "SUCCESS";
	}
	
	/**
	 * 申诉单提交
	 * @param reportSheet 申诉工单号ID
	 * @param sheetId  进行考核的工单号
	 * @param regionId 地域
	 * @param month 月分区
	 * @param reportContent 申诉内容
	 * @param falg 申诉类型,report为申诉,confirm为确认
	 * @return
	 */
	public String submitReport(String reportSheet,String sheetId,int regionId,int month,String reportContent,String falg) {
		boolean hisFlag = true;
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(
				reportSheet,regionId,month,hisFlag);
		if(sheetInfo == null) {
			return null;
		}
		if(sheetInfo.getSheetStatu() == StaticData.WKST_FINISH_STATE) {
			return "FINISH";
		}
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;			
		TsmStaff staffObj = pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staffObj.getId());
		String staffName = staffObj.getName();
		String dealOrg = staffObj.getOrganizationId();
		String dealOrgname = this.pubFunc.getOrgName(dealOrg);
		String dealStaffId = staffObj.getId();	
		
		//记录处理类型int delalId,String dealName
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
		typeBean.setDealType("申诉确认");
		typeBean.setDealTypeDesc("");
		typeBean.setDealId(0);//处理定性ID
		typeBean.setDealDesc("");//处理定性名
		typeBean.setDealContent(reportContent);//处理内容
		typeBean.setMonth(sheetInfo.getMonth());
		this.tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型
		//更新处理员工和处理部门
		this.sheetPubInfoDao.updateFetchSheetStaff(reportSheet, staffId,
				staffName, dealOrg, dealOrgname);
		
		//流程
		Map otherParam = new HashMap();
		otherParam.put("MONTH_FALG", sheetInfo.getMonth());
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工
		if(falg.equals("confirm")) {//流程竣工
			if(sheetInfo.getSheetStatu() == StaticData.WKST_REPORT_SUPER) {
				this.sheetPubInfoDao.updateSheetDealRequire(reportSheet,"","","申诉确认超时到竣工",reportContent,34,0);
			} else {
				this.sheetPubInfoDao.updateSheetDealRequire(reportSheet,"","","申诉确认到竣工",reportContent,32,StaticData.TACHE_FINISH);
			}
			
			//记录定单竣工的环节
			this.orderAskInfoDao.updateFinTache(sheetInfo.getServiceOrderId(), sheetInfo.getRegionId(), sheetInfo.getTacheId());
			otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
			otherParam.put("ROUTE_VALUE", StaticData.ROUTE_NEW_REOPRT_TO_FINI);
			workSheetFlowService.submitWorkFlow(sheetId, regionId, otherParam);
		}
		if(falg.equals("report")) {//申诉,重新打分考核			
			this.sheetPubInfoDao.updateSheetDealRequire(reportSheet,"","","申诉确认到考核",reportContent,33,StaticData.TACHE_TSASSESS);
			
			otherParam.put("TSASSESS_ORG", sheetInfo.getRetOrgId());//考核部门
			otherParam.put("TSASSESS_ORGNAME", sheetInfo.getRetOrgName());
			otherParam.put("TSASSESS_STAFF", String.valueOf(sheetInfo.getRetStaffId()));//考核人
			otherParam.put("TSASSESS_STAFFNAME", sheetInfo.getRetStaffName());
			otherParam.put("REPORT_FLAG", "REPORT_TASS");//申诉确认环节申诉到考核
			otherParam.put("ROUTE_VALUE", StaticData.ROUTE_NEW_REOPRT_TO_TSASSESS);
			otherParam.put("REPORT_NUM", String.valueOf(sheetInfo.getReportNum()));//申诉次数
			otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
			
			workSheetFlowService.submitWorkFlow(sheetId, regionId, otherParam);
		}		
		return "SUCCESS";
	}
	/**
	 * 投诉归挡环节重新派单
	 * @param workSheetObj 派单对象
	 * @param dealContent 处理要求
	 * @return
	 */
	public String submitPigeonholeSheetToDeal(SheetPubInfo[] workSheetObj,String dealContent) {
		if(workSheetObj == null) {
			return "ERROR";
		}
		SheetPubInfo sheetPubInfo = workSheetObj[0];
		int size = workSheetObj.length;
		String sheetId = sheetPubInfo.getWorkSheetId();
		String require = sheetPubInfo.getDealRequire();
		int region = sheetPubInfo.getRegionId();
		Integer month = sheetPubInfo.getMonth();

		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId, region,month, true);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (state != 1 ) {
			return "STATUSERROR";
		}		
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "STATUSERROR";
		}
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;	
		TsmStaff staff = pubFunc.getLogonStaff();
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();	
		
		String rcvOrgName = "";
		String mainOrg = "主办单位: ";
		String assitOrg="      协办单位: ";
		for(int i=0;i<size;i++) {
			sheetPubInfo = workSheetObj[i];				
			if(sheetPubInfo.getRcvOrgId().equals("STFFID")) {
				String orgId = this.pubFunc.getStaffOrgName(sheetPubInfo.getRcvStaffId());
				rcvOrgName =this.pubFunc.getOrgName(orgId);
				
			} else {
				rcvOrgName =sheetPubInfo.getRcvOrgName();
				sheetPubInfo.setRcvStaffName(" ");
			}
			//判断是否为主单单位
			if(sheetPubInfo.getMainType() == 1) {
				mainOrg = mainOrg+rcvOrgName+"("+sheetPubInfo.getRcvStaffName()+")";
			} else {
				assitOrg=assitOrg+rcvOrgName+"("+sheetPubInfo.getRcvStaffName()+")"+"; ";
			}				
		}
		rcvOrgName = mainOrg+assitOrg;
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId,require,rcvOrgName,"归档环节重新派发工单",dealContent ,26,StaticData.TACHE_DEAL);
		Map otherParam = new HashMap();	
		otherParam.put("DEAL_REQUIRE",require);
		otherParam.put("ROUTE_VALUE",StaticData.ROUTE_PIGEONHOLE_TO_TSDEAL);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工	
		otherParam.put("SHEETARRAY", workSheetObj);
		otherParam.put("PRECONTRACTSIGN", "6");//处理类型 6 归档环节重新派单 
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		//提交流程
		workSheetFlowService.submitWorkFlow(sheetId, region, otherParam);
		//记录处理类型int delalId,String dealName
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
		typeBean.setDealType("归档环节重新派发工单");
		typeBean.setDealTypeDesc("分派列表");
		typeBean.setDealId(0);//处理定性ID 归档单,0为不同意,1为同意
		typeBean.setDealDesc(rcvOrgName);//处理定性名
		typeBean.setDealContent(dealContent);//处理内容
		typeBean.setMonth(sheetInfo.getMonth());
		this.tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型
		 
		return "SUCCESS";
	}
	/**
	 * 归档工单提交
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param submitType 提交类型 0 为结束 1为定性(考核)
	 * @param month 月分区
	 * @param dealContent 处理内容
	 * @return
	 */
	public String submitPigeonholeSheet(String sheetId,int regionId,int submitType,Integer month,String dealContent) {
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(sheetId, regionId,month, true);
		int state = sheetInfo.getLockFlag();
		// 非处理中状态的单子不能提交
		if (state != 1 ) {
			return "STATUSERROR";
		}		
		if(sheetInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE) {
			return "STATUSERROR";
		}	
		//流水号
		String flowSeq = "1";
		if(sheetInfo.getFlowSequence() != null && !(sheetInfo.getFlowSequence().equals(""))) {
			flowSeq = sheetInfo.getFlowSequence();
		}
		int flowSeqNo = Integer.parseInt(flowSeq)+1;			
		TsmStaff staff = pubFunc.getLogonStaff();
		String dealOrg = staff.getOrganizationId();
		String dealStaffId = staff.getId();
		String route = StaticData.ROUTE_PIGEONHOLE_TO_TSASK;//到考核
		String dealType = "归档环节到定性";
		int dealId=27;
		int nextTache = StaticData.TACHE_TSQUALITATIVE;
		if(submitType == 0) {
			route = StaticData.ROUTE_PIGEONHOLE_TO_FINISH;//到结束
			dealType = "归档环节到结束";
			dealId=28;
			nextTache=StaticData.TACHE_FINISH;
		}
		
		this.sheetPubInfoDao.updateSheetDealRequire(sheetId," "," ",dealType,dealContent ,dealId,nextTache);
		Map otherParam = new HashMap();	
		otherParam.put("ROUTE_VALUE",route);
		otherParam.put("MONTH_FALG",month);
		otherParam.put("DEAL_PR_ORGID",dealOrg);//派发部门
		otherParam.put("DEAL_PR_STAFFID",dealStaffId);//派发员工		
		otherParam.put("FLOW_SEQUENCE",String.valueOf(flowSeqNo));//流水号
		
		//提交流程
		workSheetFlowService.submitWorkFlow(sheetId, regionId, otherParam);
		
		TsSheetDealType typeBean = new TsSheetDealType();
		String guid = this.pubFunc.crtGuid();
		typeBean.setDealTypeId(guid);
		typeBean.setOrderId(sheetInfo.getServiceOrderId());
		typeBean.setWorkSheetId(sheetInfo.getWorkSheetId());
		typeBean.setDealType(dealType);
		typeBean.setDealTypeDesc("归档意见");
		typeBean.setDealId(1);//处理定性ID 审核单,0为不同意,1为同意
		typeBean.setDealDesc("同意");//处理定性名
		typeBean.setDealContent(dealContent);//处理内容
		typeBean.setMonth(sheetInfo.getMonth());
		this.tsWorkSheetDao.saveSheetDealType(typeBean);//保存处理类型
		return "SUCCESS";
	}
	/**
	 * 投诉工单竣工
	 * @param inParam
	 * @return
	 */
	public boolean orderShetFinish(String  orderId,int regionId) {
		this.tsWorkSheetDao.saveSheetDealTypeHis(orderId, regionId);
		this.dealQualitative.saveDealQualitativeHis(orderId, "", regionId);
		this.tsSheetAuditingDaoImpl.saveTsSheetAuditingHis(orderId, "", regionId);
		this.customerVisit.saveCustomerVisitHis(orderId, "", regionId);
		this.sheetQualitative.saveTsSheetQualitativeHis(orderId, "", regionId);
		
		this.tsWorkSheetDao.deleteOrderDealType(orderId, 0);//删除语句已取消月分区
		this.dealQualitative.deleteDealQualitative(orderId, "", regionId);
		this.tsSheetAuditingDaoImpl.deleteTsSheetAuditing(orderId, "", regionId);
		this.customerVisit.deleteCustomerVisit(orderId, "", regionId);
		this.customerVisit.deleteCustomerVisitTmp(orderId, regionId);
		this.sheetQualitative.deleteTsSheetQualitative(orderId, "", regionId);
		
		tsSheetAuditingDaoImpl.saveResponsiBilityOrgHis(orderId);//添加历史表
		tsSheetAuditingDaoImpl.deleteResponsiBilityOrg(orderId);//删除当前表
		todispatchDao.saveObjHis(orderId);
		todispatchDao.delete(orderId);
		return true;
		
	}
	/**
	 * 审核或审批退单
	 * @param workSheetObj 退单对象
	 * @param dealContent 处理内容
	 * @param dealType 处理类型 0为部门转派工单，1为后台派单 5 审核重新派单 3 为审核退单，4为部门审批退单
	 * @param acceptLevel 投诉级别
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public String submitQuitSheet(SheetPubInfo[] workSheetObj,String dealContent,int acceptLevel,int dealType) {
		if(dealType == 3) {//审核退单
			return this.submitAuitSheetToDeal(workSheetObj, dealContent, dealType);
		}
		if(dealType == 4) {
			return this.orgDealDispathSheet(workSheetObj, dealContent, acceptLevel, dealType, 0);
		}
		return "SUCCESS";
	}
	/**
	 * 更新定单信息
	 * @param custInfo 客户信息
	 * @param servContent 受理内容信息
	 * @param orderAskInfo 定单信息
	 * @param sheetId 工单
	 * @param errInfo errFlag errItem errAdvice
	 * @return
	 */
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public String updateServiceContent(OrderCustomerInfo custInfo,
			ServiceContent servContent, OrderAskInfo orderAskInfo,String sheetId,int tachId, String[] errInfo) {
		//查最新的受理次数
		TsmStaff logonStaff = pubFunc.getLogonStaff();
		String orderId = orderAskInfo.getServOrderId();
		ServiceContent servContentOld = servContentDao.getServContentByOrderId(orderId, false, 0);
		if (pubFunc.getLastXX(servContentOld) != pubFunc.getLastXX(servContent)) {
			TSOrderMistake om = new TSOrderMistake();
			om.setServiceOrderId(orderId);
			om.setWorkSheetId("");
			om.setMistakeOrgId(orderAskInfo.getAskOrgId());
			om.setMistakeStaffId(orderAskInfo.getAskStaffId());
			om.setMistakeType(1);
			om.setCheckOrgId(logonStaff.getOrganizationId());
			om.setCheckStaffId(Integer.parseInt(logonStaff.getId()));
			om.setOldInfo(pubFunc.getConcatXXDesc(servContentOld));
			om.setNewInfo(pubFunc.getConcatXXDesc(servContent));
			sheetMistakeDAO.insertOrderMistake(om);
		}
		//取的原定单的基本信息
		OrderAskInfo orderAskObj = this.orderAskInfoDao.getOrderAskObj(orderId,orderAskInfo.getMonth(),false);
		int version = orderAskObj.getOrderVer();//得到当前的版本号		
		String oldCustGuid = orderAskObj.getCustId();//得到当前的客户GUID
		//先将修改前的相关信息进历史
		Integer monthHis = orderAskObj.getMonth();

		if (pubFunc.isNewWorkFlow(orderId) && orderAskObj.getServiceDate()==3) {
			int countOrder = labelManageDao.selectRepeatFlag(orderId);
			int bestOrder = this.getBestOrder(tachId, servContent.getBestOrder());
			int[] limitTimeNew = this.sheetLimitTimeServ.getLimitTimeNew(orderAskInfo.getServType(), orderAskInfo.getComeCategory(), countOrder,
					pubFunc.getLastXX(servContent), bestOrder);
			labelManageDao.updateDealHours(limitTimeNew[0], orderId);
			labelManageDao.updateAuditHours(limitTimeNew[1], orderId);
			orderAskInfo.setOrderLimitTime(limitTimeNew[2]);
			sheetPubInfoDao.updateDealLimitTimeByOrderId(limitTimeNew[0], orderId);
			sheetPubInfoDao.updateAuditLimitTimeByOrderId(limitTimeNew[1], orderId);
		} else {
		//重新设置时限
		SheetLimitTimeCollocate limitBean = this.sheetLimitTimeServ.getSheetLimitime(
				orderAskObj.getRegionId(), 
				orderAskObj.getServType(), 
				orderAskObj.getRegionId(), 0,
				orderAskObj.getCustServGrade(),
				orderAskObj.getUrgencyGrade()
		);
		if(limitBean != null) {
			orderAskInfo.setPreAlarmValue(limitBean.getPrealarmValue());
			orderAskInfo.setOrderLimitTime(limitBean.getLimitTime());
		}
		}
		//如果不是执行添加操作
		this.orderCustInfoDao.saveOrderCustHis(oldCustGuid,monthHis);
		this.orderAskInfoDao.saveOrderAskInfoHis(orderId,monthHis);
		this.servContentDao.saveServContentHis(orderId,monthHis);
		
		//修改历史定单状态
		String tatuDesc = this.pubFunc.getStaticName(StaticData.WKST_MODIFY_ACTION);
		this.orderAskInfoDao.updateOrderHisStatuByVersion(orderAskInfo.getRegionId(), orderId, version, StaticData.WKST_MODIFY_ACTION, tatuDesc, monthHis);
		// 更新当修改后的定单内容
		String newCustguid = pubFunc.crtGuid();// 重新生成客户guid
		custInfo.setCustGuid(newCustguid);
		orderCustInfoDao.saveOrderCust(custInfo);// 在当前表里存入已经更新的客户信息

		orderAskInfo.setCustId(newCustguid);// 更新受理信息表中的客户guid
		orderAskInfo.setOrderVer(version + 1);// 受理信息版本号加1
		orderAskInfo.setAskCount(orderAskObj.getAskCount());//得到最新的受理次数
		if (servContent.getSixCatalog() == 0) {
			orderAskInfo.setRelaType(pubFunc.getLastXX(servContent));
		}
		orderAskInfoDao.updateOrderAskInfo(orderAskInfo);// 更新受理信息

		servContent.setOrderVer(version + 1);// 受理内容版本号加1
		servContentDao.updateServContent(servContent);// 更新受理内容
		// 在当前中删除已经进入历史表的客户信息
		orderCustInfoDao.delOrderCustInfo(oldCustGuid,monthHis);
		//记录工单动作
		SheetActionInfo sheetActionInfo = new SheetActionInfo();
		sheetActionInfo.setWorkSheetId(sheetId);
		sheetActionInfo.setRegionId(orderAskInfo.getRegionId());
		sheetActionInfo.setServOrderId(orderAskInfo.getServOrderId());
		sheetActionInfo.setComments("修改定单内容");
		sheetActionInfo.setMonth(orderAskInfo.getMonth());
		saveSheetDealAction(sheetActionInfo,tachId,StaticData.WKST_MODIFY_ACTION,2);
		
        //***************************
		if(null==sheetId || sheetId.trim().equals("") || null==errInfo || errInfo.length == 0){
		    return "SUCCESS";
		}
		
		boolean notFromNet = orderAskInfo.getAskStaffId() != StaticData.ACPT_STAFFID_WT
                && orderAskInfo.getAskStaffId() != StaticData.ACPT_STAFFID_JT;
		if(notFromNet && null != errInfo[0]){
		    Boolean errSheet = Boolean.valueOf(errInfo[0]);
            if(Boolean.TRUE.equals(errSheet)){//如果为错单sheetPubInfoDao
                SheetPubInfo errSheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
                String newOrderId = errSheetInfo.getServiceOrderId() + "W" ;
                errSheetInfo.setServiceOrderId(newOrderId);//错误单定单号后面加CW
                errSheetInfo.setSheetStatu(StaticData.WKST_VERIFY);//状态为错单确认
                errSheetInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_VERIFY));
                errSheetInfo.setSheetType(StaticData.SHEET_TYPE_ERROR);
                errSheetInfo.setSheetTypeDesc(pubFunc.getStaticName(StaticData.SHEET_TYPE_ERROR));
                
                errSheetInfo.setRcvOrgId(orderAskObj.getAskOrgId());
                errSheetInfo.setRcvOrgName(orderAskObj.getAskOrgName());
                errSheetInfo.setRcvStaffId(orderAskObj.getAskStaffId());
                errSheetInfo.setRcvStaffName(orderAskObj.getAskStaffName());
                
                errSheetInfo.setDealOrgId(null);
                errSheetInfo.setDealOrgName(null);           
                errSheetInfo.setDealStaffId(0);
                errSheetInfo.setDealStaffName(null);
                
                errSheetInfo.setRetStaffId(Integer.parseInt(logonStaff.getId()));
                errSheetInfo.setRetStaffName(logonStaff.getName());
                errSheetInfo.setRetOrgId(logonStaff.getOrganizationId());
                errSheetInfo.setRetOrgName(logonStaff.getOrgName());
                
                errSheetInfo.setDealTypeDesc(orderAskObj.getProdNum());//操作类型 : 记录产品号码
                errSheetInfo.setStatuDate(PubFunc.dbDateToStr(orderAskObj.getAskDate()));//状态类型:记录受理时间
                errSheetInfo.setDealContent(errInfo[1]); // 差错项
                errSheetInfo.setSaveDealContent(errInfo[2]); // 改进建议
                errSheetInfo.setSheetPriValue(version); // 记录错误受理单的版本号
                errSheetInfo.setHomeSheet(3);// 2015-06-15 开发了错单的新流程，为了与老流程区分，将这个字段设置为3
                errSheetInfo.setReportNum(0);// 2015-06-15 1表示非终结错单；0表示终结错单。
                
                String newErr = pubFunc.crtSheetId(errSheetInfo.getRegionId());
                sheetPubInfoDao.saveErrSheet(errSheetInfo,newErr);
                
                MessagePrompt p = new MessagePrompt();
                p.setMsgContent("定单号:"+ newOrderId + "发生错误 \n错单号:" + newErr + "\n差错项:" + errInfo[1]);
                p.setTypeId(StaticData.MESSAGE_PROMPT_ERROR); // 错单提示
                p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_ERROR));
                p.setStaffId(orderAskObj.getAskStaffId());
                p.setStaffName(orderAskObj.getAskOrgName());
                p.setOrgId(orderAskObj.getAskOrgId());
                p.setOrgName(orderAskObj.getAskOrgName());              
                messageManager.createMsgPrompt(p);          
            }
		}
	    int newRegion = orderAskInfo.getRegionId();
	    int oldRegion = orderAskObj.getRegionId();
	    //2014-05-26 有条件得允许修改受理地域
	    if(newRegion != oldRegion){
	    	String newRegionName = orderAskInfo.getRegionName();
			sheetPubInfoDao.updateRegion(orderId, newRegion, newRegionName, oldRegion, orderAskObj.getMonth());
			sheetActionInfoDao.updateRegion(orderId, newRegion, oldRegion);
			hastenDao.updateRegion(orderId, oldRegion, newRegion, newRegionName);
			relateFileDAO.updateRegion(orderId, newRegion, oldRegion);
		}
		return "SUCCESS";
	}
	
	private int getBestOrder(int tachId, int bestOrder) {
		//最强工单修改时限获取（后台派单）
		if (tachId == 700000085 || tachId == 720130021) {
			return bestOrder;
		}
		return 0;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
	public String updateServiceContentYSAndSJ(OrderCustomerInfo custInfo, ServiceContent servContent, 
			OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, String sheetId, int tachId, String[] errInfo) {
		//查最新的受理次数
		TsmStaff logonStaff = pubFunc.getLogonStaff();
		String orderId = orderAskInfo.getServOrderId();
		ServiceContent servContentOld = servContentDao.getServContentByOrderId(orderId, false, 0);
		if (pubFunc.getLastXX(servContentOld) != pubFunc.getLastXX(servContent)) {
			TSOrderMistake om = new TSOrderMistake();
			om.setServiceOrderId(orderId);
			om.setWorkSheetId("");
			om.setMistakeOrgId(orderAskInfo.getAskOrgId());
			om.setMistakeStaffId(orderAskInfo.getAskStaffId());
			om.setMistakeType(1);
			om.setCheckOrgId(logonStaff.getOrganizationId());
			om.setCheckStaffId(Integer.parseInt(logonStaff.getId()));
			om.setOldInfo(pubFunc.getConcatXXDesc(servContentOld));
			om.setNewInfo(pubFunc.getConcatXXDesc(servContent));
			sheetMistakeDAO.insertOrderMistake(om);
		}
		//取的原定单的基本信息
		OrderAskInfo orderAskObj = this.orderAskInfoDao.getOrderAskObj(orderId, orderAskInfo.getMonth(), false);
		int version = orderAskObj.getOrderVer();//得到当前的版本
		String oldCustGuid = orderAskObj.getCustId();//得到当前的客户GUID
		//先将修改前的相关信息进历史
		Integer monthHis = orderAskObj.getMonth();

		//重新设置时限
		SheetLimitTimeCollocate limitBean = this.sheetLimitTimeServ.getSheetLimitime(
				orderAskObj.getRegionId(), 
				orderAskObj.getServType(), 
				orderAskObj.getRegionId(), 0,
				orderAskObj.getCustServGrade(),
				orderAskObj.getUrgencyGrade()
		);
		if(limitBean != null) {
			orderAskInfo.setPreAlarmValue(limitBean.getPrealarmValue());
			orderAskInfo.setOrderLimitTime(limitBean.getLimitTime());
		}
		
		//如果不是执行添加操作
		this.orderCustInfoDao.saveOrderCustHis(oldCustGuid, monthHis);
		this.orderAskInfoDao.saveOrderAskInfoHis(orderId, monthHis);
		this.servContentDao.saveServContentHis(orderId, monthHis);
		
		//修改历史定单状态
		String tatuDesc = this.pubFunc.getStaticName(StaticData.WKST_MODIFY_ACTION);
		this.orderAskInfoDao.updateOrderHisStatuByVersion(orderAskInfo.getRegionId(), orderId, version, StaticData.WKST_MODIFY_ACTION, tatuDesc, monthHis);
		// 更新当修改后的定单内容
		String newCustguid = pubFunc.crtGuid();// 重新生成客户guid
		custInfo.setCustGuid(newCustguid);
		orderCustInfoDao.saveOrderCust(custInfo);// 在当前表里存入已经更新的客户信息

		orderAskInfo.setCustId(newCustguid);// 更新受理信息表中的客户guid
		orderAskInfo.setOrderVer(version + 1);// 受理信息版本号加1
		orderAskInfo.setAskCount(orderAskObj.getAskCount());//得到最新的受理次数
		orderAskInfoDao.updateOrderAskInfo(orderAskInfo);// 更新受理信息

		servContent.setOrderVer(version + 1);// 受理内容版本号加1
		servContentDao.updateServContent(servContent);// 更新受理内容
		// 在当前中删除已经进入历史表的客户信息
		orderCustInfoDao.delOrderCustInfo(oldCustGuid, monthHis);
		//记录工单动作
		SheetActionInfo sheetActionInfo = new SheetActionInfo();
		sheetActionInfo.setWorkSheetId(sheetId);
		sheetActionInfo.setRegionId(orderAskInfo.getRegionId());
		sheetActionInfo.setServOrderId(orderAskInfo.getServOrderId());
		sheetActionInfo.setComments("修改定单内容");
		sheetActionInfo.setMonth(orderAskInfo.getMonth());
		saveSheetDealAction(sheetActionInfo, tachId, StaticData.WKST_MODIFY_ACTION, 2);
		
        //***************************
		if(null==sheetId || sheetId.trim().equals("") || null==errInfo || errInfo.length == 0){
		    return "SUCCESS";
		}
		
		boolean notFromNet = orderAskInfo.getAskStaffId() != StaticData.ACPT_STAFFID_WT
                && orderAskInfo.getAskStaffId() != StaticData.ACPT_STAFFID_JT;
		if(notFromNet && null != errInfo[0]){
		    Boolean errSheet = Boolean.valueOf(errInfo[0]);
            if(Boolean.TRUE.equals(errSheet)){//如果为错单sheetPubInfoDao
                SheetPubInfo errSheetInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
                String newOrderId = errSheetInfo.getServiceOrderId() + "W" ;
                errSheetInfo.setServiceOrderId(newOrderId);//错误单定单号后面加W
                errSheetInfo.setSheetStatu(StaticData.WKST_VERIFY);//状态为错单确认
                errSheetInfo.setSheetSatuDesc(pubFunc.getStaticName(StaticData.WKST_VERIFY));
                errSheetInfo.setSheetType(StaticData.SHEET_TYPE_ERROR);
                errSheetInfo.setSheetTypeDesc(pubFunc.getStaticName(StaticData.SHEET_TYPE_ERROR));
                
                errSheetInfo.setRcvOrgId(orderAskObj.getAskOrgId());
                errSheetInfo.setRcvOrgName(orderAskObj.getAskOrgName());
                errSheetInfo.setRcvStaffId(orderAskObj.getAskStaffId());
                errSheetInfo.setRcvStaffName(orderAskObj.getAskStaffName());
                
                errSheetInfo.setDealOrgId(null);
                errSheetInfo.setDealOrgName(null);           
                errSheetInfo.setDealStaffId(0);
                errSheetInfo.setDealStaffName(null);
                
                errSheetInfo.setRetStaffId(Integer.parseInt(logonStaff.getId()));
                errSheetInfo.setRetStaffName(logonStaff.getName());
                errSheetInfo.setRetOrgId(logonStaff.getOrganizationId());
                errSheetInfo.setRetOrgName(logonStaff.getOrgName());
                
                errSheetInfo.setDealTypeDesc(orderAskObj.getProdNum());//操作类型 : 记录产品号码
                errSheetInfo.setStatuDate(PubFunc.dbDateToStr(orderAskObj.getAskDate()));//状态类型:记录受理时间
                errSheetInfo.setDealContent(errInfo[1]); // 差错项
                errSheetInfo.setSaveDealContent(errInfo[2]); // 改进建议
                errSheetInfo.setSheetPriValue(version); // 记录错误受理单的版本号
                errSheetInfo.setHomeSheet(3);// 2015-06-15 开发了错单的新流程，为了与老流程区分，将这个字段设置为3
                errSheetInfo.setReportNum(0);// 2015-06-15 1表示非终结错单；0表示终结错单。
                
                String newErr = pubFunc.crtSheetId(errSheetInfo.getRegionId());
                sheetPubInfoDao.saveErrSheet(errSheetInfo, newErr);
                
                MessagePrompt p = new MessagePrompt();
                p.setMsgContent("定单号:"+ newOrderId + "发生错误 \n错单号:" + newErr + "\n差错项:" + errInfo[1]);
                p.setTypeId(StaticData.MESSAGE_PROMPT_ERROR); // 错单提示
                p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_ERROR));
                p.setStaffId(orderAskObj.getAskStaffId());
                p.setStaffName(orderAskObj.getAskOrgName());
                p.setOrgId(orderAskObj.getAskOrgId());
                p.setOrgName(orderAskObj.getAskOrgName());              
                messageManager.createMsgPrompt(p);          
            }
		}
		//修改商机单数据
		orderAskInfoDao.updateBuopSheetInfoNew(buopSheetInfo);
		
		return "SUCCESS";
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String updateServiceContentNew(
			JSONObject models,
			ServiceOrderInfo serviceInfo,
			String sheetId,
			int tachId,
			ServiceContentSave[] saves,
			String oldAcceptCent,
			String[] errInfo) {
		
		OrderCustomerInfo custInfo = serviceInfo.getOrderCustInfo();
		ServiceContent servContent = serviceInfo.getServContent();
		OrderAskInfo orderAskInfo = serviceInfo.getOrderAskInfo();
		
		//1: 调用老的修改方法
		String acceptContent = servContent.getAcceptContent();
		servContent.setAcceptContent(oldAcceptCent); //老的受理内容加模板，保存
		String res = updateServiceContent(custInfo, servContent, orderAskInfo, sheetId, tachId, errInfo);
		servContentDao.updateAcceptContent(orderAskInfo.getServOrderId(), acceptContent, orderAskInfo.getMonth()); //修改回这次修改的受理内容

		if (!oldAcceptCent.equals(acceptContent)) {
			TsmStaff logonStaff = pubFunc.getLogonStaff();
			TSOrderMistake om = new TSOrderMistake();
			om.setServiceOrderId(orderAskInfo.getServOrderId());
			om.setWorkSheetId("");
			om.setMistakeOrgId(orderAskInfo.getAskOrgId());
			om.setMistakeStaffId(orderAskInfo.getAskStaffId());
			om.setMistakeType(3);
			om.setCheckOrgId(logonStaff.getOrganizationId());
			om.setCheckStaffId(Integer.parseInt(logonStaff.getId()));
			om.setOldInfo(oldAcceptCent);
			om.setNewInfo(acceptContent);
			sheetMistakeDAO.insertOrderMistake(om);
		}

		//2:如果新的模板不为空，先删除模板再添加模板内容
		if(saves != null) {
			//跟踪单修改，处理退费数据
			ServiceContentSave[] newSaves = this.filterTrackOrderSaves(orderAskInfo, saves);
			serviceContentSchem.saveOrderContents(newSaves, orderAskInfo.getServOrderId());
		} else {//202502，如果模板为空，删除cc_service_content_save表记录
			serviceContentSchem.saveOrderContents(saves, orderAskInfo.getServOrderId());
		}
		
		//工信部、省管局更新申诉信息
		this.updateComplaintInfo(models);
		//集团、申诉投诉单，修改业务归属地时重新打标
		this.updateRepeatFlag(models, orderAskInfo, servContent);
		dapdSheetService.modifyDapd(orderAskInfo, custInfo, servContent, saves, acceptContent);
		return res;
	}
	
	private ServiceContentSave[] filterTrackOrderSaves(OrderAskInfo orderAskInfo, ServiceContentSave[] saves) {
		//跟踪单、小额退赔
		if(720200002 == orderAskInfo.getServType() && 2020501 == orderAskInfo.getRelaType()) {
			//更新退费记录
			this.updateRefundData(orderAskInfo.getServOrderId(), saves);
			//模板内容过滤退费数据
			return serviceContentSchem.filterRefundData(saves);
		}
		return saves;
	}
	
	private void updateRefundData(String orderId, ServiceContentSave[] saves) {
		String refundData = "";
		String refundsAccNum = "";
		String refundAmount = "";
		String prmRefundAmount = "";
		try {
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
			if(org.apache.commons.lang3.StringUtils.isNotBlank(refundData)) {
				JSONObject json = JSONObject.fromObject(refundData);
				refundsAccNum = json.optString("refundsAccNum");
				refundAmount = json.optString("cashAmount");
			}
		} catch (Exception e) {
			logger.error("setRefundData error: {}", e.getMessage(), e);
		}
		logger.info("refundData: {}", refundData);
		
		if(org.apache.commons.lang3.StringUtils.isNotBlank(refundData)) {
			com.alibaba.fastjson.JSONObject refundInfo = refundService.getOrderRefund(orderId);
			logger.info("refundInfo: {}", refundInfo.toJSONString());
			if(refundInfo.isEmpty()) {
				return;
			}
			//存在退费记录
			com.alibaba.fastjson.JSONObject result = refundService.updateOrderRefund(orderId, refundInfo, refundData, refundsAccNum, refundAmount, prmRefundAmount);
			logger.info("updateOrderRefund result: {}", JSON.toJSONString(result));
		} else {
			com.alibaba.fastjson.JSONObject refundInfo = refundService.getOrderRefund(orderId);
			logger.info("refundInfo: {}", refundInfo.toJSONString());
			if(refundInfo.isEmpty()) {
				return;
			}
			String oldPrmRefundAmount = refundInfo.getString("PRM_REFUND_AMOUNT");//承诺退费金额
			logger.info("orderId: {} prmRefundAmount: {} oldPrmRefundAmount: {}", orderId, prmRefundAmount, oldPrmRefundAmount);
			if(!prmRefundAmount.equals(oldPrmRefundAmount)){
				refundService.updatePrmRefundAmount(prmRefundAmount, orderId);
			}
		}
	}
	
	private void updateRepeatFlag(JSONObject models, OrderAskInfo orderAskInfo, ServiceContent servContent) {
		if(models.has("updateRepeatFlag")){
			String serviceOrderId = orderAskInfo.getServOrderId();
			int servType = orderAskInfo.getServType();
			//清理旧的重复关系
			this.deleteOldConnection(serviceOrderId);
			//重复标签重新打标
			ServiceLabel label = new ServiceLabel();
			label.setServiceOrderId(serviceOrderId);
			CustomerPersona persona = new CustomerPersona();
			if (StaticData.SERV_TYPE_CX == servType) {
				labelManageService.updateRepeatFlagCX(label, persona, orderAskInfo);
			} else {
				boolean complaintFlag = this.getComplaintFlag(servType, orderAskInfo.getAskChannelId());//申诉投诉
				boolean unifiedFlag = this.getUnifiedFlag(servType);//投诉咨询单
				labelManageService.updateRepeatFlag(label, persona, orderAskInfo, servContent, complaintFlag, unifiedFlag);
			}
			labelManageDao.updateRepeatLabel(label);
			dapdSheetService.modifyDapdFlag(label, persona);
		}
	}
	
	private boolean getComplaintFlag(int servType, int askChannelId) {
		//投诉单 工信部、省管局
		return servType == StaticData.SERV_TYPE_NEWTS && (askChannelId == 707907026 || askChannelId == 707907027);
	}
	
	private boolean getUnifiedFlag(int servType) {
		//投诉咨询单
		return servType == StaticData.SERV_TYPE_NEWTS || servType == StaticData.SERV_TYPE_ZX;
	}
	
	private void deleteOldConnection(String serviceOrderId) {
		//查询重复关联关系
		List guidList = labelManageDao.getRepeatGuidList(serviceOrderId);
		for(Object obj : guidList) {
			Map map = (Map)obj;
			String guid = map.get("CONNECTION_GUID").toString();
			String type = map.get("CONNECTION_TYPE").toString();
			//保存旧的关联关系
			int num = labelManageDao.saveOldConnection(guid, serviceOrderId, type);
			if(num > 0) {
				//删除旧关联关系
				labelManageDao.deleteServiceConnection(guid);
			}
		}
	}

	public void updateComplaintInfo(JSONObject models) {
		if(models.has("complaintFlag") && models.has("complaintInfo")) {
			OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(models.optJSONObject("orderAskInfo"), OrderAskInfo.class);
			ComplaintInfo info = (ComplaintInfo)JSONObject.toBean(models.getJSONObject("complaintInfo"), ComplaintInfo.class);
			logger.info("updateComplaintInfo flag:{}", models.getString("complaintFlag"));
			int num = 0;
			if("update".equals(models.getString("complaintFlag"))) {
				num = orderAskInfoDao.updateComplaintInfo(info);
				//同步修改申诉信息
				this.updateAppealInfo(orderAskInfo, info);
			} else if("insert".equals(models.getString("complaintFlag"))) {
				num = orderAskInfoDao.insertComplaintInfo(info);
				//新增申诉信息
				this.saveAppealInfo(info.getOrderId(), orderAskInfo.getRegionId(), orderAskInfo.getProdNum(), info.getMiitCode(), info.getThirdLevel());
			}
			dapdSheetService.modifyDapdComp(info);
			logger.info("updateComplaintInfo result:{}", num > 0 ? "成功" : "失败");
		}
	}
	
	private void updateAppealInfo(OrderAskInfo orderAskInfo, ComplaintInfo info) {
		try {
			logger.info("orderId: {} prodNum: {} miitCode: {} thirdLevel: {}", info.getOrderId(), orderAskInfo.getProdNum(), info.getMiitCode(), info.getThirdLevel());
			com.alibaba.fastjson.JSONObject json = complaintMaterialsService.loadStashData(info.getOrderId());
			logger.info("oldAppealInfo: {}", JSON.toJSON(json));
			if(json.isEmpty()) {
				return;
			}
			JSONArray data = json.getJSONArray("data");
			if(data.isEmpty()) {
				//新增记录
				this.saveAppealInfo(info.getOrderId(), orderAskInfo.getRegionId(), orderAskInfo.getProdNum(), info.getMiitCode(), info.getThirdLevel());
				return;
			}
			boolean flag = pubFunc.judgeThirdLevel(info.getThirdLevel());
			logger.info("judgeThirdLevel: {}", flag);
			if(!flag) {
				//删除记录
				complaintMaterialsService.deleteAppealInfo(info.getOrderId());
				return;
			}
			com.alibaba.fastjson.JSONObject appeal = data.getJSONObject(0);
			//产品号码、部局编码、分类码三级修改
			String prodNum = appeal.getString("PROD_NUM");
			String miitCode = appeal.getString("MIIT_CODE");
			String thirdLevel = appeal.getString("THIRD_LEVEL");
			if((!StringUtils.equals(orderAskInfo.getProdNum(), prodNum)) 
					|| (!StringUtils.equals(info.getMiitCode(), miitCode)) 
					|| (!StringUtils.equals(info.getThirdLevel(), thirdLevel))) {
				complaintMaterialsService.modifyAppealInfo(info.getOrderId(), orderAskInfo.getRegionId(), orderAskInfo.getProdNum(), info.getMiitCode(), info.getThirdLevel(), 
						orderAskInfo.getIsOwner(), prodNum);
			}
		} catch (Exception e) {
			logger.error("updateAppealInfo error: {}", e.getMessage(), e);
        }
	}
	
	public void saveAppealInfo(String orderId, int regionId, String prodNum, String miitCode, String thirdLevel) {
		boolean flag = pubFunc.judgeThirdLevel(thirdLevel);
		logger.info("judgeThirdLevel: {}", flag);
		if(flag) {//符合场景
			try {
				com.alibaba.fastjson.JSONObject json = new com.alibaba.fastjson.JSONObject();
				json.put("orderId", orderId);
				json.put("regionId", regionId);
				json.put("prodNum", prodNum);
				json.put("miitCode", miitCode);
				json.put("thirdLevel", thirdLevel);
				compOrderFeign.saveAppealInfo(json.toJSONString());
			} catch (Exception e) {
				logger.error("saveAppealInfo error: {}", e.getMessage(), e);
	        }
		}
	}

	public int saveCmpSupplementModify(String cwi, String cpto, String cptn, String cio, String cin, String dro, String drn, String drs, String askStaffId,
			String dealStaff, String dealContentSaveStr) {
		if (!cpto.equals(cptn)) {
			List cptoList = this.pubFunc.queryAcceptDir(Integer.parseInt(cpto), 0);
			Map cptoMap = (Map) cptoList.get(0);
			String cptoN = cptoMap.get("N").toString().replace(" > ", "|");
			List cptnList = this.pubFunc.queryAcceptDir(Integer.parseInt(cptn), 2);
			Map cptnMap = (Map) cptnList.get(0);
			String cptnN = cptnMap.get("N").toString().replace(" > ", "|");
			MessagePrompt p = new MessagePrompt();
			p.setMsgContent("服务单号:" + cwi + ",原受理目录:" + cptoN + ",修改为:" + cptnN);
			p.setTypeId(StaticData.MESSAGE_PROMPT_SHXGYJ);
			p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_SHXGYJ));
			p.setStaffId(Integer.parseInt(askStaffId));
			messageManager.createMsgPrompt(p);
		}
		if (!cio.replaceAll("(\r\n|\n)", "").equals(cin.replaceAll("(\r\n|\n)", ""))) {
			MessagePrompt p = new MessagePrompt();
			String str="服务单号:" + cwi + ",原受理内容:" + cio.replaceAll("(\r\n|\n)", "") + ",修改为:" + cin.replaceAll("(\r\n|\n)", "");
			if(str.length()>1900) {
				str=str.substring(0, 1900)+"......内容过长，已省略";
			}
			p.setMsgContent(str);
			p.setTypeId(StaticData.MESSAGE_PROMPT_SHXGYJ);
			p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_SHXGYJ));
			p.setStaffId(Integer.parseInt(askStaffId));
			messageManager.createMsgPrompt(p);
		}
		if (!dro.equals(drn)) {
			MessagePrompt p = new MessagePrompt();
			String str="工单号:" + drs + ",原处理内容:" + dro.replaceAll("(\r\n|\n)", "") + ",修改为:" + drn.replaceAll("(\r\n|\n)", "");
			if(str.length()>1900) {
				str=str.substring(0, 1900)+"......内容过长，已省略";
			}
			p.setMsgContent(str);
			p.setTypeId(StaticData.MESSAGE_PROMPT_SHXGYJ);
			p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_SHXGYJ));
			p.setStaffId(Integer.parseInt(dealStaff));
			messageManager.createMsgPrompt(p);

			TsmStaff logonStaff = pubFunc.getLogonStaff();
			TSOrderMistake om = new TSOrderMistake();
			om.setServiceOrderId(cwi);
			om.setWorkSheetId(drs);
			om.setMistakeOrgId(pubFunc.getStaff(Integer.parseInt(dealStaff)).getOrganizationId());
			om.setMistakeStaffId(Integer.parseInt(dealStaff));
			om.setMistakeType(4);
			om.setCheckOrgId(logonStaff.getOrganizationId());
			om.setCheckStaffId(Integer.parseInt(logonStaff.getId()));
			om.setOldInfo(dro);
			om.setNewInfo(drn);
			sheetMistakeDAO.insertOrderMistake(om);
			
			//终定性修改：保存结案模板
			List<ServiceContentSave> saveList = new Gson().fromJson(dealContentSaveStr,new TypeToken<List<ServiceContentSave>>() {}.getType());
			serviceContentSchem.saveDealContentSave(saveList, cwi);
		}
		return this.pubFunc.saveCmpSupplementModify(cwi, cpto, cptn, cio, cin, dro, drn, drs);
	}

	/**
	 * 部门审批时候,填写特性内容
	 * @param sheetPubInfo 工单对象
	 * @param tsSheetAuditing 审核
	 * @return
	 */
	private boolean saveDealOrgTrait(SheetPubInfo sheetPubInfo, TsSheetAuditing tsSheetAuditing) {
		if(tsSheetAuditing != null) {
			//审核特性表
			tsSheetAuditing.setRegName(sheetPubInfo.getRegionName());
			tsSheetAuditing.setTacheId(sheetPubInfo.getTacheId());
			tsSheetAuditing.setTacheName(sheetPubInfo.getTacheDesc());
			tsSheetAuditing.setSheetType(sheetPubInfo.getSheetType());
			tsSheetAuditing.setSheetTypeDesc(sheetPubInfo.getSheetTypeDesc());
			tsSheetAuditing.setMonthFlag(sheetPubInfo.getMonth());
			this.tsSheetAuditingDaoImpl.saveTsSheetAuditing(tsSheetAuditing);			
		}
		return true;
	}
	
	/**
	 * 获取本受理单的第二条派单关系。如果不存在，则获取第一条派单关系。
	 * 
	 * @author LiJiahui
	 * @date 2011-10-21
	 * @param sheetPubInfo 审核单对象
	 * @return 派单关系对象
	 */
	private WorkSheetAllotReal getUpReal(SheetPubInfo sheetPubInfo){
		String strWhere=" and cc_worksheet_allot_rela.pre_deal_worksheet_id = " +
                "(select DISTINCT w.deal_worksheet_id from cc_worksheet_allot_rela w where w.check_worksheet_id = '" +
                sheetPubInfo.getWorkSheetId() +
                "' AND w.main_sheet_flag = 1 AND w.month_flag = " +
                sheetPubInfo.getMonth() +
                ") AND cc_worksheet_allot_rela.month_flag="+sheetPubInfo.getMonth()+" and cc_worksheet_allot_rela.main_sheet_flag=1";
        
        WorkSheetAllotReal[] workSheetAllotReal = null;
        try{
            workSheetAllotReal = this.workSheetAlllot.getWorkSheetAllotReal(strWhere, true);
        }catch (Exception e) {
            workSheetAllotReal = null;
        }
        
        if(null == workSheetAllotReal || workSheetAllotReal.length == 0){
            workSheetAllotReal = dealOrgAudSheet(sheetPubInfo);
        }
        return workSheetAllotReal[0];
	}
	
	/**
	 * 得到工单派发关系对象 
	 * @param sheetPubInfo 审核单对象
	 * @return 派单关系对象
	 */
	private WorkSheetAllotReal[] dealOrgAudSheet(SheetPubInfo sheetPubInfo) {
        String strWhere="AND cc_worksheet_allot_rela.check_worksheet_id= '"+sheetPubInfo.getWorkSheetId()+"' " +
                "AND cc_worksheet_allot_rela.month_flag="+sheetPubInfo.getMonth()+" and cc_worksheet_allot_rela.main_sheet_flag=1";
        return this.workSheetAlllot.getWorkSheetAllotReal(strWhere, true);
	}	
	
	/**
	 * 工单到岗，发送短信提醒
	 * @param bean 工单实例
	 * @param type 发送类型。1 发送到个人；0发送到部门
	 */
	private void sendNoteCont(SheetPubInfo bean,int type, int dealStaffId, int autoStaffId) {
	    if(!pubFunc.isLogonFlag()){
	        return;
	    }
		NoteSeand noteBean = null;
		String phone ="";
		String client = "0";
		String sheetGuid;
		String relaPerson="";
		List tmp = null;
		if(type==0) {
			tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(),null, bean.getTacheId(),0);
		} else {
			tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(),String.valueOf(bean.getDealStaffId()), bean.getTacheId(),1);
		}
		if(tmp == null) {
			return;
		}
		OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskObj(bean.getServiceOrderId(),bean.getMonth(),false);
		
		String comment = orderAskInfo.getComment();
		int size = tmp.size();
		Map map = null;
		if(size > 0) {
            TsmStaff staff = null;
            if (dealStaffId == 2604457){
                staff = this.pubFunc.getLogonStaffByLoginName("JS15301588119");//取企业信息化部－张静
            } else {
                staff = getDealStaff(autoStaffId);//取当前登录员工信息
            }
			int staffId = Integer.parseInt(staff.getId());
			String staffName = staff.getName();
			String orgId = staff.getOrganizationId();
			String orgName = staff.getOrgName();

			for(int i=0;i< size;i++) {
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
				String ph = orderAskInfo.getRelaInfo();
				
				noteBean.setSendContent(relaPerson+"您好:有一条新的"+bean.getServTypeDesc()+"单派发到你部门,服务单号为:"+bean.getServiceOrderId()+
						",处理时限:"+bean.getDealLimitTime()+"小时,受理的内容概述为:" +
						comment + ",客户姓名:"+ cn +",联系电话:"+ ph +",请注意查收.");
				noteBean.setOrgId(orgId);
				noteBean.setOrgName(orgName);
				noteBean.setStaffId(staffId);
				noteBean.setStaffName(staffName);	
				noteBean.setBusiId(bean.getWorkSheetId());
				this.noteSen.saveNoteContent(noteBean);
			}
			map = null;
		}		
	}

	/**
	 * 判断工单类型是否是审批单
	 * @param sheetType 工单类型
	 * @return true表示审批单
	 */
	private boolean isCheckSheet(int sheetType){
	    return sheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL || sheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL_NEW;
	}

	public GridDataInfo querySheetQualitativeGrid(String orderId, String startTime, String endTime, String begin, String pageSize) {
		TsmStaff staff = pubFunc.getLogonStaff();
		String orgId = pubFunc.getAreaOrgId(staff.getOrganizationId());
		return sheetQualitative.selectSheetQualitativeGrid(orderId, startTime, endTime, orgId, begin, pageSize);
	}

	public int saveSheetQualitativeGrid(TsSheetQualitativeGrid sqg) {
		TsmStaff staff = pubFunc.getLogonStaff();
		sqg.setCreateStaff(Integer.parseInt(staff.getId()));
		return sheetQualitative.insertSheetQualitativeGrid(sqg);
	}
	
	public String validationTurnOrg(String orderId, String workSheetId, String revOrgId, String strOrgId, int sendType, String xbStrOrgId, int xbSendType) {
		ComplaintRelation complaintRelation = pubFunc.queryListByOid(orderId);
		if(complaintRelation != null){//集团关联服务单
			return "";
		}
		OrderAskInfo orderInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
		if(StaticData.getAskLevelId()[0] != orderInfo.getComeCategory()) {//非省内投诉
			return "";
		}
		int result = pubFunc.checkPDTime(workSheetId);//剩余派单时间
		if(result > 0) {
			return "";
		}
		
		boolean checkMainFlag = this.checkMainAssignObj(revOrgId, strOrgId, sendType);
		if(checkMainFlag) {//判断主办对象是否是其他二级部门
			return "已超过环节时限不允许派单到其他二级部门";
		}
		
		boolean checkAssistFlag = this.checkAssistAssignObj(revOrgId, xbStrOrgId, xbSendType);
		if(checkAssistFlag) {//判断协办对象是否是其他二级部门
			return "已超过环节时限不允许派单到其他二级部门";
		}
		return "";
	}
	
	/**
	 * 判断主办对象是否是其他二级部门
	 */
	private boolean checkMainAssignObj(String revOrgId, String strOrgId, int sendType) {
		if(sendType == 1 ) {//主办部门
			//派单部门是否是其他二级部门
			return (!pubFunc.getAreaOrgId(revOrgId).equals(pubFunc.getAreaOrgId(strOrgId)));
		} else if(sendType==2) {//主办员工
			//派单员工所在部门是否是其他二级部门
			return (!pubFunc.getAreaOrgId(revOrgId).equals(pubFunc.getAreaOrgIdByStaff(strOrgId)));
		}
		return false;
	}
	
	/**
	 * 判断协办对象是否是其他二级部门
	 */
	private boolean checkAssistAssignObj(String revOrgId, String xbStrOrgId, int xbSendType) {
		if(xbSendType == 1) {//协办部门
			String[] xbOrgArr = xbStrOrgId.split("\\,");
			for (int i=0; i<xbOrgArr.length; i++){
				if(!pubFunc.getAreaOrgId(revOrgId).equals(pubFunc.getAreaOrgId(xbOrgArr[i]))){//派单部门是否是其他二级部门
					return true;
				}
			}
		}else if(xbSendType == 2) {//协办员工
			String[] xbStaffArr = xbStrOrgId.split("\\,");
			for (int i=0; i<xbStaffArr.length; i++){
				if(!pubFunc.getAreaOrgId(revOrgId).equals(pubFunc.getAreaOrgIdByStaff(xbStaffArr[i]))){//派单员工所在部门是否是其他二级部门
					return true;
				}
			}
		}
		return false;
	}

	public int saveSheetOperation(SheetOperation operation) {
		return tsWorkSheetDao.insertSheetOperation(operation);
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public String xcRefundDispathSheet(SheetPubInfo curSheet, String prmRefundAmount, String refundDetail, String refundData) {
		logger.info("xcRefundDispathSheet curSheet: {} refundData: {}", JSON.toJSON(curSheet), refundData);
		// 取得原工单对象
		String oldSheetId = curSheet.getWorkSheetId();
		SheetPubInfo oldSheet = this.sheetPubInfoDao.getSheetObj(oldSheetId, curSheet.getRegionId(), curSheet.getMonth(), true);
		String orderId = oldSheet.getServiceOrderId();
		int servType = oldSheet.getServType();
		TsmStaff staff = pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String staffName = staff.getName();
		String dealorgId = staff.getOrganizationId();
		String dealorgName = staff.getOrgName();

		SheetPubInfo newSheet = new SheetPubInfo();
		newSheet.setServiceOrderId(orderId);
		newSheet.setRegionId(oldSheet.getRegionId());
		newSheet.setRegionName(oldSheet.getRegionName());
		newSheet.setServType(servType);
		newSheet.setServTypeDesc(oldSheet.getServTypeDesc());
		newSheet.setSourceSheetId(oldSheetId);
		int tachId = getXcTachId(servType);
		newSheet.setTacheId(tachId);
		newSheet.setTacheDesc(pubFunc.getStaticName(tachId));
		newSheet.setWflInstId(oldSheet.getWflInstId());
		newSheet.setTacheInstId(oldSheet.getTacheInstId());
		newSheet.setSheetType(StaticData.SHEET_TYPE_XC_SN);
		newSheet.setSheetTypeDesc(this.pubFunc.getStaticName(StaticData.SHEET_TYPE_XC_SN));
		newSheet.setSheetRcvDate(this.pubFunc.getSysDateFormat("%Y-%m-%d %H:%i:%s"));
		newSheet.setDealLimitTime(curSheet.getDealLimitTime());
		newSheet.setStationLimit(curSheet.getDealLimitTime());
		newSheet.setSheetPriValue(oldSheet.getSheetPriValue());
		newSheet.setPreAlarmValue(oldSheet.getPreAlarmValue());
		newSheet.setAlarmValue(oldSheet.getAlarmValue());
		String info = "处理要求填写人：" + staff.getName() + "  联系电话：" + staff.getRelaPhone() + "\n协查原因：" + curSheet.getDealRequire() + "  总承诺退费金额：" + prmRefundAmount;
		newSheet.setDealRequire(info);
		newSheet.setAutoVisitFlag(0);
		newSheet.setPrecontractSign(0);
		newSheet.setRetOrgId(dealorgId);
		newSheet.setRetOrgName(dealorgName);
		newSheet.setRetStaffId(staffId);
		newSheet.setRetStaffName(staffName);
		newSheet.setMonth(oldSheet.getMonth());
		String newSheetId = pubFunc.crtSheetId(curSheet.getRegionId());
		newSheet.setWorkSheetId(newSheetId);
		String flowSequence = getFlowSequence(oldSheet);
		newSheet.setFlowSequence(flowSequence);// 流水号 flowSeq
		TsmStaff recStaff = this.pubFunc.getStaff(curSheet.getRcvStaffId());
		String curReceiveOrg = recStaff.getOrganizationId();
		newSheet.setRcvOrgId(curReceiveOrg);
		newSheet.setRcvOrgName(recStaff.getOrgName());
		newSheet.setRcvStaffId(curSheet.getRcvStaffId());
		newSheet.setRcvStaffName(recStaff.getName());
		newSheet.setDealOrgId(recStaff.getOrganizationId());
		newSheet.setDealOrgName(recStaff.getOrgName());
		newSheet.setDealStaffId(curSheet.getRcvStaffId());
		newSheet.setDealStaffName(recStaff.getName());
		int sheetStatu = getDealing(servType);
		newSheet.setSheetStatu(sheetStatu);
		newSheet.setSheetSatuDesc(pubFunc.getStaticName(sheetStatu));
		newSheet.setLockFlag(1);
		int recRegion = this.pubFunc.getOrgRegion(newSheet.getRcvOrgId());
		String recRegionName = this.pubFunc.getRegionName(recRegion);
		newSheet.setReceiveRegionId(recRegion);
		newSheet.setReceiveRegionName(recRegionName);
		newSheet.setMainType(1);
		logger.info("xcDispathSheet newSheet: {}", JSON.toJSON(newSheet));
		this.sheetPubInfoDao.saveSheetPubInfo(newSheet);
		this.sendNoteCont(newSheet, 1, 0, 0);
		
		String mainOrg = "协查单位: " + recStaff.getOrgName() + "(" + newSheet.getDealStaffName() + ")";
		// 记录处理类型
		TsSheetDealType typeBean = new TsSheetDealType();
		typeBean.setDealType("省内协查派单");
		typeBean.setDealTypeId(pubFunc.crtGuid());
		typeBean.setOrderId(orderId);
		typeBean.setWorkSheetId(newSheetId);
		typeBean.setDealTypeDesc("协查派单");
		typeBean.setDealId(0);// 处理定性ID 如果为审批单,0为不同意,1为同意
		typeBean.setDealDesc(mainOrg);// 处理定性名
		typeBean.setMonth(oldSheet.getMonth());
		tsWorkSheetDao.saveSheetDealType(typeBean);// 保存处理类型

		MessagePrompt p = new MessagePrompt();
		p.setMsgContent("省内协查申请通知，服务单号:" + orderId);
		p.setTypeId(StaticData.MESSAGE_PROMPT_SNXCSQ); // 省内协查申请通知
		p.setTypeName(pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_SNXCSQ));
		p.setStaffId(newSheet.getRcvStaffId());
		p.setStaffName(newSheet.getRcvStaffName());
		p.setOrgId(newSheet.getRcvOrgId());
		p.setOrgName(newSheet.getRcvOrgName());
		messageManager.createMsgPrompt(p);
		
		sheetPubInfoDao.updateLastXcSheetIdBySheetId(newSheetId, oldSheetId);// 更新最后一次协查单号
		updateXcFlow(curSheet, 1, staffId, newSheetId, curReceiveOrg);
		//保存调账审批记录
		refundService.saveRefundApproveInfo(newSheetId, oldSheetId, orderId, prmRefundAmount, refundDetail, refundData);
		return "SUCCESS";
	}
}