/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import com.timesontransfar.staffSkill.dao.IFlowToEndDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.WorkSheetAllot;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.dao.IserviceContentDao;
import com.timesontransfar.customservice.paramconfig.service.IsheetLimitTimeService;
import com.timesontransfar.customservice.workFlowOrg.service.IsheetFlowOrg;
import com.timesontransfar.customservice.worksheet.dao.IForceDistillDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetActionInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.dao.ItsCustomerVisit;
import com.timesontransfar.customservice.worksheet.dao.ItsDealQualitative;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetAuditing;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.dao.IworkSheetAllotRealDao;
import com.timesontransfar.customservice.worksheet.pojo.ForceDistill;
import com.timesontransfar.customservice.worksheet.pojo.SheetActionInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetReadRecordInfo;
import com.timesontransfar.customservice.worksheet.service.ItsSheetDealAction;
import com.timesontransfar.staffSkill.service.StaffWorkSkill;

import javax.annotation.Resource;

/**
 * @author 万荣伟
 *
 */
@Component(value="tsSheetDealService")
@SuppressWarnings("rawtypes")
public class TsSheetDealActionImpl implements ItsSheetDealAction {
	protected Logger log = LoggerFactory.getLogger(TsSheetDealActionImpl.class);
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
	private IsheetFlowOrg sheetFlowOrgId;//流向
	@Autowired
	private ItsWorkSheetDao	tsWorkSheetDao;//投诉DAO
	@Autowired
	private ItsDealQualitative    dealQualitative;//部门处理定性表
	@Autowired
	private ItsSheetAuditing	   sheetAuditing;//审核表
	@Autowired
	private ItsCustomerVisit	   customerVisit;//投诉回访表
	@Autowired
	private ItsSheetQualitative   sheetQualitative;//	投诉定性表
	@Autowired
	private StaffWorkSkill	staffWorkSkill;
	@Autowired
	private IForceDistillDao forceDistillDao;

	@Resource
	private IFlowToEndDao flowToEndDao;
	
	/**
     * 工单分派操作的实例
     */
	@Autowired
    private WorkSheetAllot workSheetAllot;

	private static final String SUCCESS = "SUCCESS";
	private static final String CALL_SERIAL_NO = "CALL_SERIAL_NO";
	

	/* （非 Javadoc）得到投诉工单的处理流水数
	 * @see com.timesontransfar.customservice.worksheet.service.ItsSheetDealAction#getTsdealFlowCount(java.lang.String, int, boolean)
	 * @param orderId 定单号
	 * @param regionId 地域
	 * @param boo true查询当前 false 查询历史
	 * @return
	 */
	public int getTsdealFlowCount(String orderId, int regionId, boolean boo) {
		return this.tsWorkSheetDao.getTsSheetDealCount(orderId, regionId, boo);
	}

	public String allotBatchSheet(String[] sheetId,int staffId,int type) {
		int size = 0;
		if(sheetId != null) {
			size = sheetId.length;
		}
		String returnStr="";
		//分派
		if(type == 0) {
			for(int i=0;i<size;i++) {
				String[] sheetObj=sheetId[i].split("@");
				String satue = fetchWorkSheet(sheetObj[0],Integer.parseInt(sheetObj[1]),new Integer(sheetObj[2]),staffId);
				if(satue.equals(SUCCESS)) {
					returnStr+=sheetObj[0]+"@";
				}
			}			
		}
		//释放
		if(type == 1) {
			for(int i=0;i<size;i++) {
				String[] sheetObj=sheetId[i].split("@");
				String satue = sheetForceRelease(sheetObj[0],Integer.parseInt(sheetObj[1]),new Integer(sheetObj[2]));
				if(satue.equals(SUCCESS)) {
					returnStr+=sheetObj[0]+"@";
				}
			}			
		}		
		return returnStr;
	}
	
	public String allotBatchSheet(String[] sheetIds,String[] staffId) {
		int size = 0;
		if(sheetIds != null) {
			size = sheetIds.length;
		}
		int dspnum = 0;//平均派单量
		if(size % staffId.length == 0) {
			dspnum = size / staffId.length;
		}
		else {
			dspnum = size / staffId.length + 1;
		}
		String returnStr="";
		for(int i=0;i<staffId.length;i++) {
			for(int j=0;j<dspnum;j++) {
				if(i*dspnum+j>=size) {
					break;
				}
				String[] sheetObj=sheetIds[i*dspnum+j].split("@");
				String satue = fetchWorkSheet(sheetObj[0],Integer.parseInt(sheetObj[1]),new Integer(sheetObj[2]),Integer.parseInt(staffId[i]));
				if(satue.equals(SUCCESS)) {
					returnStr+=sheetObj[0]+"@";
				}
			}			
		}
		return returnStr;
	}
	
	/**
	 * 人工派单
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param 
	 * @param
	 * @return	取回的工单数
	 */
	private String fetchWorkSheet(String sheetId,int regionId,Integer month,int allotStaffId) {
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(sheetId,regionId,month, hisFlag);
		if(sheetPubInfo == null) {
			
			log.debug("未查询到工单号"+sheetId);
			return "ERROR";
		}
		int sheetState = sheetPubInfo.getLockFlag();
		//工单不为已派发状态
		if (sheetState == 0 && sheetPubInfo.getSheetStatu() != StaticData.WKST_ALLOT_STATE) {
			//取当前登录员工信息
			TsmStaff staff = this.pubFunc.getLogonStaff();
			String staffName = staff.getName();
			//分派员工的信息
			String allotStaffName = this.pubFunc.getStaffName(allotStaffId);
			String allotOrg = this.pubFunc.getStaffOrgName(allotStaffId);
			String allotOrgName = this.pubFunc.getOrgName(allotOrg);
			
			
			//  由南京客服中心班长派单计算工作量 改成所有班长手动派单都计算工作量
			int num = workSheetAllot.countWorkload(sheetPubInfo.getSheetType(), sheetPubInfo.getSheetStatu(), sheetPubInfo.getRetStaffId(), 
			        allotStaffId, allotOrg, 2);
			// 更新提单员工信息及工单状态
			this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, allotStaffId,
					allotStaffName, allotOrg, allotOrgName);
			//得到要更新的状态
			int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());			
			String stateDesc = pubFunc
					.getStaticName(sheetStatu);			
			this.sheetPubInfoDao.updateSheetState(sheetId,
					sheetStatu, stateDesc,sheetPubInfo.getMonth(),1);
			
			// 记录工单动作			
			SheetActionInfo sheetActionInfo = new SheetActionInfo();
			sheetActionInfo.setWorkSheetId(sheetId); 
			
			sheetActionInfo.setRegionId(sheetPubInfo.getRegionId());
			sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
			sheetActionInfo.setComments("人工分派工单,员工"+staffName+"工单号为"+sheetId+"的工单分派给员工"+allotStaffName+allotStaffId+",工作量为"+num);
			sheetActionInfo.setMonth(sheetPubInfo.getMonth());
			saveSheetDealAction(sheetActionInfo,sheetPubInfo.getTacheId(),StaticData.WKST_SHEET_ALLOCATE,3);
			return SUCCESS;
		}

		return "ERROR";
	}
	
	/**
	 * 工单强制释放
	 * @param sheetId 工单号
	 * @param regionId 所属地域
	 * @param month 月分区
	 * @return
	 */
	private String sheetForceRelease(String sheetId,int regionId,Integer month) {
		boolean hisFlag = true;
		SheetPubInfo sheetPubInfo = this.sheetPubInfoDao.getSheetObj(
				sheetId,regionId,month, hisFlag);
		int sheetState = sheetPubInfo.getLockFlag();
		//不处理中的工单 或者是挂起中的工单都不能做强制释放
		if( sheetState != 1 || sheetPubInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE
				|| sheetPubInfo.getSheetStatu() == StaticData.WKST_HOLD_STATE_NEW) {
			return "STATUERROR";
		}
		// 更新提单员工信息及工单状态
		this.sheetPubInfoDao.updateFetchSheetStaff(sheetId, 0, "", "", "");
		
		int sheetStatu = this.pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 0, sheetPubInfo.getSheetType());
		
		String stateDesc = pubFunc
				.getStaticName(sheetStatu);
		this.sheetPubInfoDao.updateSheetState(sheetId,
				sheetStatu, stateDesc,sheetPubInfo.getMonth(),0);
		// 记录工单动作
		SheetActionInfo sheetActionInfo = new SheetActionInfo();
		sheetActionInfo.setWorkSheetId(sheetId);
		int tacheId = sheetPubInfo.getTacheId();
		sheetActionInfo.setComments("强制释放工单");
		sheetActionInfo.setRegionId(sheetPubInfo.getRegionId());
		sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
		sheetActionInfo.setMonth(sheetPubInfo.getMonth());
		saveSheetDealAction(sheetActionInfo,tacheId,StaticData.WKST_FORCE_RELEASE_ALLOCATE,4);
		return SUCCESS;
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
	 * 得到工单录音文件
	 * @param orderId 定单号
	 * @param region 地域
	 * @param boo true 查询当前  false 查询历史
	 * @param fileType 下载录音类型 1为新录音下载,2为通话文本
	 * @return
	 */
	public String getRecordFile(String orderId,int region,boolean boo,int fileType) {
		List orderList = this.tsWorkSheetDao.getRecordOrderInfo(orderId, region, boo);
		if(orderList.isEmpty()) {
			return "";
		}
		Map orderMap = (Map)orderList.get(0);
		log.info("定单信息：{}", orderMap);
		
		if(null == orderMap.get(CALL_SERIAL_NO) || "N".equals(orderMap.get(CALL_SERIAL_NO))) {
			return "";
		}
		String flowId = orderMap.get(CALL_SERIAL_NO).toString();
		
		List recordList = this.tsWorkSheetDao.getFtpFilesByFlowId(flowId);
		if(recordList.isEmpty()) {
			return "";
		}
		Map recordMap = (Map)recordList.get(0);
		log.info("录音文件FTP信息：{}", recordMap);
		
		String callCenterId = recordMap.get("CALLCENTER_ID").toString();//4-扬州 5-南京 7-苏州
		String filePath = recordMap.get("FILE_PATH").toString();
		String recordFile = recordMap.get("VOICE_FILENAME").toString();
		if(fileType == 2){
			recordFile = recordFile.split("\\.")[0]+".txt";
		}
		log.info("录音所属FTP：{}", callCenterId);
		return this.getFtpFile(callCenterId, filePath, recordFile);
	}
	
	public String getVoiceFile(String flowId) {
		List recordList = this.tsWorkSheetDao.getFtpFilesByFlowId(flowId);
		if(recordList.isEmpty()) {
			return null;
		}
		Map recordMap = (Map)recordList.get(0);
		log.info("录音文件FTP信息：{}", recordMap);
		
		String callCenterId = recordMap.get("CALLCENTER_ID").toString();//4-扬州 5-南京 7-苏州
		String filePath = recordMap.get("FILE_PATH").toString();
		String recordFile = recordMap.get("VOICE_FILENAME").toString();
		String callType = recordMap.get("CALL_TYPE").toString();//呼入、呼出类型
		if("0".equals(callType)) {//0为呼出
			String orgId = recordMap.get("ORG_ID").toString();//外呼部门
			String areaId = this.tsWorkSheetDao.getAreaId(orgId);
			if(!"".equals(areaId)) {//5-南京FTP //4-扬州FTP //7-苏州FTP
				callCenterId = areaId;
			}
		}
		log.info("录音所属FTP：{}", callCenterId);
		return this.getFtpFile(callCenterId, filePath, recordFile);
	}
	
	private String getFtpFile(String callCenterId, String filePath, String recordFile) {
		List ftpList = null;
		String hostIp = "new_sheet_voice";
		if("5".equals(callCenterId)) {
			ftpList = this.tsWorkSheetDao.getRecordFtp(10005, hostIp);//南京
		}
		else if("4".equals(callCenterId)) {
			ftpList = this.tsWorkSheetDao.getRecordFtp(10006, hostIp);//扬州
		}
		else if("7".equals(callCenterId)) {
			ftpList = this.tsWorkSheetDao.getRecordFtp(10007, hostIp);//苏州
		}
		if(ftpList == null || ftpList.isEmpty()) {
			log.info("ftpList 没有查出记录");
			return null;
		}

		Map ftpMap = (Map) ftpList.get(0);
		//FTP 用户名,密码,HOST,PORT
		String ftpUser = ftpMap.get("USER_NAME").toString()+"@"+new String(Base64.getDecoder().decode(ftpMap.get("PASSWORD").toString()))+"@"+
				ftpMap.get("FTP_HOST").toString()+"@"+ftpMap.get("PORT").toString();
		//FTP下载文件路径
		String ftpFile = ftpMap.get("AREA_CODE").toString()+"/"+ filePath +"@"+recordFile;
		log.info("ftpUser：{}，ftpFile：{}", ftpUser, ftpFile);
		return ftpUser+"FTP_FTP"+ftpFile;
	}
	/**
	 * 保存员工读取工单记录
	 * @param bean
	 * @return
	 */
	public String saveSheetRead(SheetReadRecordInfo bean) {
		return this.tsWorkSheetDao.saveSheetReadRecord(bean) > 0 ? SUCCESS : "FAIL";
	}
	/**
	 * 根据审核工单或审批工单得相关联的完成工单
	 * @param sheetId
	 * @param regionId
	 * @return
	 */
	public List getRelatingSheet(String sheetId,int regionId) {
		return this.tsWorkSheetDao.getRelatingSheet(sheetId, regionId);
	}
	/**
	 * 得到工单状态审批数据
	 * @param sheetId 工单号
	 */
	public List getSheetStatuAud(String sheetId) {
		return this.tsWorkSheetDao.getSheetStatuAud(sheetId);
	}

	public String forceDistillList(int staffId, String[] orders) {
		int countT = 0;
		int countF = 0;
		for (int i = 0; i < orders.length; i++) {
			int out = doForceDistill(staffId, orders[i]);
			if (out == 0) {
				countF += 1;
			} else {
				countT += 1;
			}
		}
		if (0 == countF) {
			return countT + "条强制提取成功";
		} else {
			return countT + "条强制提取成功，" + countF + "条强制提取失败";
		}
	}

	private int doForceDistill(int staffId, String orderInfos) {
		int out = 0;
		String[] orderInfo = orderInfos.split("@");
		String orderId = orderInfo[0];
		int regionId = Integer.parseInt(orderInfo[3]);
		int monthFlag = Integer.parseInt(orderInfo[4]);
		List sheets = forceDistillDao.getCurSheetInfo(orderId, orderInfo[1]);
		if (!sheets.isEmpty()) {
			Map sheetInfo = (Map) sheets.get(0);
			String sheetId = sheetInfo.get("WORK_SHEET_ID").toString();
			String fetchFlag = forceDistillSheet(sheetId, regionId, monthFlag, staffId, "业务工单监控箱");
			if (SUCCESS.equals(fetchFlag)) {
				out = saveForceDistill(orderInfos, sheetInfo, staffId);
				for (int j = 1; j < sheets.size(); j++) {
					sheetInfo = (Map) sheets.get(j);
					sheetId = sheetInfo.get("WORK_SHEET_ID").toString();
					fetchFlag = forceDistillSheet(sheetId, regionId, monthFlag, staffId, "业务工单监控箱");
				}
			}
		}
		return out;
	}

	private String forceDistillSheet(String sheetId, int regionId, int monthFlag, int staffId, String desc) {
		SheetPubInfo sheetPubInfo = sheetPubInfoDao.getSheetObj(sheetId, regionId, monthFlag, true);
		if (sheetPubInfo == null) {
			return "ERROR";
		}
		TsmStaff staff = pubFunc.getStaff(staffId);
		// 更新提单员工信息及工单状态
		sheetPubInfoDao.updateFetchSheetStaff(sheetId, staffId, staff.getName(), staff.getOrganizationId(), staff.getOrgName());
		// 得到要更新的状态
		int sheetStatu = pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1, sheetPubInfo.getSheetType());
		String stateDesc = pubFunc.getStaticName(sheetStatu);
		sheetPubInfoDao.updateSheetState(sheetId, sheetStatu, stateDesc, monthFlag, 1);
		// 记录工单动作
		SheetActionInfo sheetActionInfo = new SheetActionInfo();
		sheetActionInfo.setWorkSheetId(sheetId);
		sheetActionInfo.setRegionId(regionId);
		sheetActionInfo.setServOrderId(sheetPubInfo.getServiceOrderId());
		sheetActionInfo.setComments(desc + ",强制提取工单,员工" + staff.getName() + ",工单号为" + sheetId);
		sheetActionInfo.setMonth(monthFlag);
		saveSheetDealAction(sheetActionInfo, sheetPubInfo.getTacheId(), StaticData.WKST_ACTION_FORCEDISTILL, 3);
		return SUCCESS;
	}

	private int saveForceDistill(String orders, Map sheetInfo, int staffId) {
		String[] orderInfo = orders.split("@");
		ForceDistill fd = new ForceDistill();
		fd.setServiceOrderId(orderInfo[0]);
		fd.setAcceptDate(orderInfo[2]);
		fd.setWorkSheetId(sheetInfo.get("WORK_SHEET_ID").toString());
		fd.setForceStaff(staffId);
		fd.setForcedReceiveOrgid(sheetInfo.get("RECEIVE_ORG_NAME").toString());
		fd.setForcedReceiveStaffId(sheetInfo.get("RECEIVE_STAFF") == null ? 0 : Integer.parseInt(sheetInfo.get("RECEIVE_STAFF").toString()));
		fd.setForcedDealOrgId(sheetInfo.get("DEAL_ORG_NAME") == null ? "" : sheetInfo.get("DEAL_ORG_NAME").toString());
		fd.setForcedDealStaffId(sheetInfo.get("DEAL_STAFF") == null ? 0 : Integer.parseInt(sheetInfo.get("DEAL_STAFF").toString()));
		return forceDistillDao.insertForceDistill(fd);
	}

	/**
	 * @return customerVisit
	 */
	public ItsCustomerVisit getCustomerVisit() {
		return customerVisit;
	}

	/**
	 * @param customerVisit 要设置的 customerVisit
	 */
	public void setCustomerVisit(ItsCustomerVisit customerVisit) {
		this.customerVisit = customerVisit;
	}

	/**
	 * @return dealQualitative
	 */
	public ItsDealQualitative getDealQualitative() {
		return dealQualitative;
	}

	/**
	 * @param dealQualitative 要设置的 dealQualitative
	 */
	public void setDealQualitative(ItsDealQualitative dealQualitative) {
		this.dealQualitative = dealQualitative;
	}

	/**
	 * @return noteSen
	 */
	public InoteSenList getNoteSen() {
		return noteSen;
	}

	/**
	 * @param noteSen 要设置的 noteSen
	 */
	public void setNoteSen(InoteSenList noteSen) {
		this.noteSen = noteSen;
	}

	/**
	 * @return orderAskInfoDao
	 */
	public IorderAskInfoDao getOrderAskInfoDao() {
		return orderAskInfoDao;
	}

	/**
	 * @param orderAskInfoDao 要设置的 orderAskInfoDao
	 */
	public void setOrderAskInfoDao(IorderAskInfoDao orderAskInfoDao) {
		this.orderAskInfoDao = orderAskInfoDao;
	}

	/**
	 * @return orderCustInfoDao
	 */
	public IorderCustInfoDao getOrderCustInfoDao() {
		return orderCustInfoDao;
	}

	/**
	 * @param orderCustInfoDao 要设置的 orderCustInfoDao
	 */
	public void setOrderCustInfoDao(IorderCustInfoDao orderCustInfoDao) {
		this.orderCustInfoDao = orderCustInfoDao;
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
	 * @return servContentDao
	 */
	public IserviceContentDao getServContentDao() {
		return servContentDao;
	}

	/**
	 * @param servContentDao 要设置的 servContentDao
	 */
	public void setServContentDao(IserviceContentDao servContentDao) {
		this.servContentDao = servContentDao;
	}

	/**
	 * @return sheetActionInfoDao
	 */
	public ISheetActionInfoDao getSheetActionInfoDao() {
		return sheetActionInfoDao;
	}

	/**
	 * @param sheetActionInfoDao 要设置的 sheetActionInfoDao
	 */
	public void setSheetActionInfoDao(ISheetActionInfoDao sheetActionInfoDao) {
		this.sheetActionInfoDao = sheetActionInfoDao;
	}

	/**
	 * @return sheetAuditing
	 */
	public ItsSheetAuditing getSheetAuditing() {
		return sheetAuditing;
	}

	/**
	 * @param sheetAuditing 要设置的 sheetAuditing
	 */
	public void setSheetAuditing(ItsSheetAuditing sheetAuditing) {
		this.sheetAuditing = sheetAuditing;
	}

	/**
	 * @return sheetFlowOrgId
	 */
	public IsheetFlowOrg getSheetFlowOrgId() {
		return sheetFlowOrgId;
	}

	/**
	 * @param sheetFlowOrgId 要设置的 sheetFlowOrgId
	 */
	public void setSheetFlowOrgId(IsheetFlowOrg sheetFlowOrgId) {
		this.sheetFlowOrgId = sheetFlowOrgId;
	}

	/**
	 * @return sheetLimitTimeServ
	 */
	public IsheetLimitTimeService getSheetLimitTimeServ() {
		return sheetLimitTimeServ;
	}

	/**
	 * @param sheetLimitTimeServ 要设置的 sheetLimitTimeServ
	 */
	public void setSheetLimitTimeServ(IsheetLimitTimeService sheetLimitTimeServ) {
		this.sheetLimitTimeServ = sheetLimitTimeServ;
	}

	/**
	 * @return sheetPubInfoDao
	 */
	public ISheetPubInfoDao getSheetPubInfoDao() {
		return sheetPubInfoDao;
	}

	/**
	 * @param sheetPubInfoDao 要设置的 sheetPubInfoDao
	 */
	public void setSheetPubInfoDao(ISheetPubInfoDao sheetPubInfoDao) {
		this.sheetPubInfoDao = sheetPubInfoDao;
	}

	/**
	 * @return sheetQualitative
	 */
	public ItsSheetQualitative getSheetQualitative() {
		return sheetQualitative;
	}

	/**
	 * @param sheetQualitative 要设置的 sheetQualitative
	 */
	public void setSheetQualitative(ItsSheetQualitative sheetQualitative) {
		this.sheetQualitative = sheetQualitative;
	}

	/**
	 * @return staffWorkSkill
	 */
	public StaffWorkSkill getStaffWorkSkill() {
		return staffWorkSkill;
	}

	/**
	 * @param staffWorkSkill 要设置的 staffWorkSkill
	 */
	public void setStaffWorkSkill(StaffWorkSkill staffWorkSkill) {
		this.staffWorkSkill = staffWorkSkill;
	}

	/**
	 * @return tsWorkSheetDao
	 */
	public ItsWorkSheetDao getTsWorkSheetDao() {
		return tsWorkSheetDao;
	}

	/**
	 * @param tsWorkSheetDao 要设置的 tsWorkSheetDao
	 */
	public void setTsWorkSheetDao(ItsWorkSheetDao tsWorkSheetDao) {
		this.tsWorkSheetDao = tsWorkSheetDao;
	}


	/**
	 * @return workSheetAlllot
	 */
	public IworkSheetAllotRealDao getWorkSheetAlllot() {
		return workSheetAlllot;
	}

	/**
	 * @param workSheetAlllot 要设置的 workSheetAlllot
	 */
	public void setWorkSheetAlllot(IworkSheetAllotRealDao workSheetAlllot) {
		this.workSheetAlllot = workSheetAlllot;
	}

	public void setWorkSheetAllot(WorkSheetAllot workSheetAllot) {
        this.workSheetAllot = workSheetAllot;
    }
	
	@Override
	public List getTsdealFlowObjNew(String orderId, int regionId, boolean boo) {
		 return this.tsWorkSheetDao.getTsSheetDealObjNew(orderId,boo);
	}

	/**
	 * 强制提取重复单监控箱的工单
	 * */
	@Override
	public String flowToEndForceDistill(int staffId, String[] orders) {
		int countT = 0;
		int countF = 0;
		for (int i = 0; i < orders.length; i++) {
			int out = doFlowToEndForceDistill(staffId, orders[i]);
			if (out == 0) {
				countF += 1;
			} else {
				countT += 1;
			}
		}
		if (0 == countF) {
			return countT + "条强制提取成功";
		} else {
			return countT + "条强制提取成功，" + countF + "条失败";
		}
	}

	/**
	 * 强制提取一跟到底工单
	 * */
	private int doFlowToEndForceDistill(int staffId, String orderInfos) {
		int out = 0;
		String[] orderInfo = orderInfos.split("@");
		int regionId = Integer.parseInt(orderInfo[3]);
		int monthFlag = Integer.parseInt(orderInfo[4]);
		String incrementId = orderInfo[5];
		GridDataInfo gdi = flowToEndDao.selectFlowToEndCouldForce("", "", "", 0, incrementId);
		if (gdi.getQuryCount() > 0) {
			List sheets = gdi.getList();
			if (!sheets.isEmpty()) {
				Map sheetInfo = (Map) sheets.get(0);
				String sheetId = sheetInfo.get("SHEET_ID").toString();
				String fetchFlag = forceDistillSheet(sheetId, regionId, monthFlag, staffId, "一跟到底监控箱");
				if ("SUCCESS".equals(fetchFlag)) {
					out = flowToEndDao.updateFlowToEndForceByIncrementId(String.valueOf(staffId), Integer.parseInt(incrementId));
				}
			}
		}
		return out;
	}

	/*
	 * 校验当前登录员工是否属于一跟到底配置部门
	 */
	public int checkFlowToEndConfigOrg() {
		TsmStaff staff = pubFunc.getLogonStaff();
		String orgPlace = pubFunc.getAreaOrgId(staff.getOrganizationId());// 获取单位
		if (flowToEndDao.countFlowToEndConfigByIdType(orgPlace, 1) > 0) {
			return 1;
		}
		return 0;
	}

	/*
	 * 根据单号查询一跟到底前单和后单数量之和
	 */
	public int getFlowToEndCountByOrderId(String orderId) {
		List list = flowToEndDao.selectFlowToEndByOrderId(orderId);
		if (!list.isEmpty()) {
			return list.size();
		}
		return 0;
	}

	/*
	 * 根据单号查询一跟到底前单和后单信息
	 */
	public List getFlowToEndListByOrderId(String orderId) {
		return flowToEndDao.selectFlowToEndByOrderId(orderId);
	}
}