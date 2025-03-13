package com.timesontransfar.customservice.worksheet.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.handler.ComplaintDealHandler;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.orderask.dao.ITrackServiceDao;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.dao.IorderCustInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.tuschema.dao.IserviceContentTypeDao;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.worksheet.dao.IHastenSheetInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.OrderRefundService;
import com.timesontransfar.feign.custominterface.InterfaceFeign;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component(value="orderRefundService")
public class OrderRefundServiceImpl implements OrderRefundService {
	protected Logger log = LoggerFactory.getLogger(OrderRefundServiceImpl.class);

	@Autowired
	private JdbcTemplate jt;
	
	@Autowired
    private PubFunc pubFunc;
	
	@Autowired
	private InterfaceFeign interfaceFeign;
	
	@Autowired
	private ILabelManageDAO labelManageDAO;
	
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;
    
	@Autowired
	private IorderCustInfoDao orderCustInfoDao;
	
	@Autowired
	private IHastenSheetInfoDao hastenSheetInfoDaoImpl;
	
	@Autowired
	private IComplaintWorksheetDeal complaintWorksheetDealImpl;
	
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;
	
    @Autowired
    private ITrackServiceDao trackServiceDao;
    
	@Autowired
    private IserviceContentTypeDao serviceContentType;
	
	@Override
	public JSONObject getOrderRefund(String orderId) {
		JSONObject jsonObject = new JSONObject();
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT C.TRACK_ORDER_ID, C.SERVICE_ORDER_ID, DATE_FORMAT(C.CREATE_DATE, '%Y-%m-%d %H:%i:%s') CREATE_DATE, C.REFUND_DATA, "
					+ "DATE_FORMAT(C.MODIFY_DATE, '%Y-%m-%d %H:%i:%s') MODIFY_DATE, C.MODIFY_STAFF, "
					+ "C.AUTO_STATUS, C.AUTO_AUDIT_REASON, DATE_FORMAT(C.AUTO_AUDIT_DATE, '%Y-%m-%d %H:%i:%s') AUTO_AUDIT_DATE, C.REFUND_STATUS, "
					+ "C.AUDIT_STAFF, DATE_FORMAT(C.AUDIT_DATE, '%Y-%m-%d %H:%i:%s') AUDIT_DATE, C.AUDIT_REASON, C.REFUND_REASON, C.REFUND_REASON_DESC, "
					+ "C.REFUND_NUM, C.REFUND_AMOUNT, C.PRM_REFUND_AMOUNT, C.RECHAG_STATUS, DATE_FORMAT(C.RECHAG_DATE, '%Y-%m-%d %H:%i:%s') RECHAG_DATE, "
					+ "C.ORDER_STATUS, DATE_FORMAT(C.UPDATE_DATE, '%Y-%m-%d %H:%i:%s') UPDATE_DATE, T.STAFFNAME AUDIT_STAFFNAME "
					+ "FROM CC_ORDER_REFUND C LEFT JOIN TSM_STAFF T ON C.AUDIT_STAFF = T.LOGONNAME WHERE C.TRACK_ORDER_ID = ?");
			List<Map<String, Object>> list = jt.queryForList(sql.toString(), orderId);
			if(!list.isEmpty()) {
				Map<String, Object> map = list.get(0);
				return JSON.parseObject(JSON.toJSONString(map), JSONObject.class);
			}
		}catch (Exception e){
			log.error("getOrderRefund error: {}", e.getMessage());
		}
		return jsonObject;
	}

	@Override
	public JSONObject checkAndRechage(String tuiRason, String tuiRasonDesc, String refundStatus, String orderId, String loginName) {
		JSONObject json = new JSONObject();
		JSONObject refundJson = this.getOrderRefund(orderId);
		log.info("getOrderRefund: {}", refundJson.toJSONString());
		if(refundJson.isEmpty()) {
			json.put("code", -1);
			json.put("msg", "没有查询到退费数据");
			return json;
		}
		try {
			String refundData = refundJson.getString("REFUND_DATA");
			String status = refundJson.getString("REFUND_STATUS");//退费状态 1：未审核、2：审核不通过、3：审核通过（退）、4：24小时未到账（超）
			if(!"1".equals(status)) {//非待审核状态
				json.put("code", -1);
				json.put("msg", "退费状态不正确");
				return json;
			}
			
			JSONObject dataJson = JSON.parseObject(refundData, JSONObject.class);
			if("3".equals(refundStatus)) {//审核通过调用退费接口
				dataJson.put("complain_order_id", orderId);
				dataJson.put("orderId", orderId);
				dataJson.put("staffId", loginName);//退费员工
				//判断是否需要添加审批单号
				String serviceOrderId = refundJson.getString("SERVICE_ORDER_ID");//服务单号
				this.judgeAuditRefund(serviceOrderId, dataJson);
				JSONObject rechageResult = interfaceFeign.rechargeRefundExcute(dataJson.toJSONString());
				String code = rechageResult.getString("code");
				if("0".equals(code)) {//充值成功
					dataJson.put("refundOrderId", rechageResult.getString("orderId"));
					this.updateRefundData(tuiRason,tuiRasonDesc,3,dataJson,orderId,loginName,"2");
					json.put("code", 0);
					json.put("msg", "退费成功");
				} else {//充值失败
					this.updateRefundData(tuiRason,tuiRasonDesc,3,dataJson,orderId,loginName,"1");
					json.put("code", 1);
					json.put("msg", "退费失败");
				}
			} else {
				//审核不通过
				this.updateRefundData(tuiRason,tuiRasonDesc,2,dataJson,orderId,loginName,null);
				json.put("code", 0);
				json.put("msg", "操作成功");
			}
		}catch (Exception e){
			log.error("checkAndRechage Error: {}", e.getMessage(), e);
			json.put("code", -1);
			json.put("msg", "接口异常");
		}
		return json;
	}

	private void updateRefundData(String tuiRason, String tuiRasonDesc, int refundStatus, JSONObject data, String orderId, String loginName, String rechageStatus){
		int num = 0;
		try {
			String refundsAccNum = data.getString("refundsAccNum");//退费号码
			String money = data.getString("money");//合计退费金额
			String refundOrderId = data.getString("refundOrderId");//退费工单号
			
			String sql = "UPDATE CC_ORDER_REFUND SET REFUND_STATUS=?,AUDIT_STAFF=?,AUDIT_DATE=now(),REFUND_REASON=?,REFUND_REASON_DESC=?,REFUND_NUM=?,REFUND_AMOUNT=?,RECHAG_STATUS=?" +
					",REFUND_ORDER_ID=? WHERE TRACK_ORDER_ID=?";
			num = this.jt.update(sql, refundStatus, loginName, tuiRason, tuiRasonDesc,
					StringUtils.substring(refundsAccNum, 0, 20), StringUtils.substring(money, 0, 20),
					rechageStatus, StringUtils.substring(refundOrderId, 0, 20), orderId);
		} catch (Exception e){
			log.error("updateRefundData Error: {}", e.getMessage(), e);
		}
		log.info("updateRefundData orderId: {} refundStatus: {} result: {}", orderId, refundStatus, num);
		if(num > 0) {
			labelManageDAO.updateRefundFlag(orderId, refundStatus);
		}
	}

	public JSONObject updateOrderRefund(String orderId, JSONObject refundInfo, String refundData, String refundsAccNum, String refundAmount, String prmRefundAmount) {
		String refundStatus = refundInfo.getString("REFUND_STATUS");//退费状态 1：未审核、2：审核不通过、3：审核通过（退）、4：24小时未到账（超）
		log.info("orderId: {} refundStatus: {}", orderId, refundStatus);
		
		JSONObject json = new JSONObject();
		if("2".equals(refundStatus) || "3".equals(refundStatus) || "4".equals(refundStatus)){//审核通过
			json.put("code", 0);
			json.put("msg", "审核通过的退费数据不能修改");
			return json;
		}
		
		String loginName = pubFunc.getLogonStaff().getLogonName();
		if("1".equals(refundStatus)){
			int num = this.updateRefundInfo(refundData, loginName, refundsAccNum, refundAmount, prmRefundAmount, orderId);
			json.put("code", num);
			json.put("msg", num > 0 ? "成功" : "失败");
		}
		return json;
	}
	
	private int updateRefundInfo(String refundData, String loginName, String refundsAccNum, String refundAmount, String prmRefundAmount, String orderId) {
		int num = 0;
		try {
			String strSql = "UPDATE CC_ORDER_REFUND SET REFUND_DATA=?,MODIFY_DATE=now(),MODIFY_STAFF=?,REFUND_STATUS=1,REFUND_NUM=?,REFUND_AMOUNT=?,PRM_REFUND_AMOUNT=? WHERE TRACK_ORDER_ID=?";
			num = jt.update(strSql, refundData, loginName, StringUtils.substring(refundsAccNum, 0, 20), StringUtils.substring(refundAmount, 0, 20), prmRefundAmount, orderId);
		} catch(Exception e) {
			log.error("updateRefundInfo error: {}", e.getMessage(), e);
		}
		log.info("updateRefundInfo orderId: {} loginName: {} result: {}", orderId, loginName, num);
		return num;
	}
	
	public int updateArchiveStatus(String orderId) {
		int num = 0;
		try {
			String strSql = "UPDATE CC_ORDER_REFUND SET ORDER_STATUS=1,UPDATE_DATE=now() WHERE TRACK_ORDER_ID=?";
			num = jt.update(strSql, orderId);
		} catch(Exception e) {
			log.error("updateArchiveStatus error: {}", e.getMessage(), e);
		}
		log.info("updateArchiveStatus orderId: {} ", orderId);
		return num;
	}
	
	public String auditTrackOrder(String orderId) {
		JSONObject refundJson = this.getOrderRefund(orderId);
		if(refundJson.isEmpty()) {
			return "ORDER_EMPTY";
		}
		String status = refundJson.getString("AUTO_STATUS");
		if(!"1".equals(status)) {//非待审批
			return "ERROR_STATUS";
		}
		int refundStatus = 1;//待审核
		int autoStatus = 1;//待审核
		String auditReason = "";//自动审核原因
		//自动审核
		boolean auditStatus = this.judgeAuditStatus(orderId, refundJson);
		log.info("judgeAuditStatus result: {}", auditStatus ? "通过" : "不通过");
		if(auditStatus) {//审核通过
			//审核退费原因默认为"基础功能费（来电名片、漏话助理等）"
			JSONObject result = this.checkAndRechage("4", "基础功能费（来电名片、漏话助理等）", "3", orderId, "SYSAT001");
			String code = result.getString("code");
			if("0".equals(code)) {//退费成功
				refundStatus = 3;//审核通过（退）
			}
			autoStatus = 3;//审核通过
		} else {
			autoStatus = 2;//审核不通过
			auditReason = refundJson.getString("auditReason");
		}
		log.info("judgeAuditStatus orderId: {} autoStatus: {} auditReason: {}", orderId, autoStatus, auditReason);
		//更新自动审核状态
		int flag = this.updateAutoAuditResult(refundStatus, autoStatus, auditReason, orderId);
		//判断是否需要解挂
		boolean needFlag = this.isNeedUnHangup(orderId, refundStatus);
		log.info("isNeedUnHangup orderId: {} result: {}", orderId, needFlag);
		if(needFlag) {
			//调账解挂
			flag = this.unHangupTrackOrder(orderId);
			log.info("trackOrderId: {} unHangupTrackOrder: {}", orderId, flag);
		}
		return flag > 0 ? "SUCCESS" : "FAIL";
	}
	
	/**
	 * 跟踪单是否需要解挂
	 * @param orderId
	 * @param refundStatus
	 * @return
	 */
	private boolean isNeedUnHangup(String orderId, int refundStatus) {
		log.info("isNeedUnHangup orderId: {} refundStatus: {}", orderId, refundStatus);
		if(refundStatus != 3) {//退费状态：非“审核通过（退）”，解挂人工处理
			return true;
		}
		ServiceContentSave[] saves = serviceContentType.selectContentSave(orderId);
		log.info("selectContentSave: {}", JSON.toJSON(saves));
		if(saves.length == 0) {
			return true;
		}
		for(int i=0; i<saves.length; i++) {
			ServiceContentSave s = saves[i];
			if("79e7a4b3ae2009b277285e0c8d13dd80".equals(s.getElementId()) && "radio_0053".equals(s.getAnswerId())) {//承诺退费周期：次月出账，解挂人工处理
				return true;
			}
			if("d86fc07758818171bb2b48ff64b4dec2".equals(s.getElementId()) && "radio_001".equals(s.getAnswerId())) {//是否多号码退费：是，解挂人工处理
				return true;
			}
		}
		return false;
	}
	
	private int updateAutoAuditResult(int refundStatus, int autoStatus, String auditReason, String orderId) {
		int num = 0;
		try {
			String strSql = "UPDATE CC_ORDER_REFUND SET REFUND_STATUS=?,AUTO_STATUS=?,AUTO_AUDIT_REASON=?,AUTO_AUDIT_DATE=now() WHERE TRACK_ORDER_ID=?";
			num = jt.update(strSql, refundStatus, autoStatus, auditReason, orderId);
		} catch(Exception e) {
			log.error("updateAutoAuditResult error: {}", e.getMessage(), e);
		}
		log.info("updateAutoAuditResult orderId: {} refundStatus: {} autoStatus: {} result: {}", orderId, refundStatus, autoStatus, num);
		return num;
	}
	
	public int unHangupTrackOrder(String orderId) {
		SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(orderId, StaticData.SHEET_TYPE_TS_ASSING, 0);
		if (!ComplaintDealHandler.isHoldSheet(sheetInfo.getSheetStatu())) {
			return 0; // 非挂起
		}
		int sheetState = sheetInfo.getLockFlag();
		if (sheetState == 0 && sheetInfo.getSheetStatu() != StaticData.WKST_ALLOT_STATE) { // 工单池
			// 调账解挂
			return complaintWorksheetDealImpl.unHangupForTZ(sheetInfo.getWorkSheetId());
		}
		return 0;
	}
	
	/** 系统自动审核的规则，若满足其中任一规则，则系统自动审核不通过
		1）客服代表勾选的账单总计金额与录单输入字段中承诺退费金额是不一致。
		2）判断退费号码是否存在历史退费。
		3）判断账单总计金额小于等于0。
		4）当前跟踪单有催单记录。
		5）判断客户姓名长度超过4位。
		6）承诺退费金额超过限制（小额退赔查询单：大于200；工单处理：大于等于500）。
	 */
	private boolean judgeAuditStatus(String orderId, JSONObject refundJson) {
		try {
			String refundNum = refundJson.getString("REFUND_NUM");//退费号码
			String refundAmount = refundJson.getString("REFUND_AMOUNT");//账单合计金额
			String prmRefundAmount = refundJson.getString("PRM_REFUND_AMOUNT");//承诺退费金额
			//客服代表勾选的账单总计金额与录单输入字段中承诺退费金额是不一致
			if(!StringUtils.equals(refundAmount, prmRefundAmount)) {
				refundJson.put("auditReason", "1");
				return false;
			}
			//判断账单总计金额小于等于0
			if(Double.parseDouble(refundAmount) <= 0) {
				refundJson.put("auditReason", "3");
				return false;
			}
			
			OrderAskInfo orderAskInfo = orderAskInfoDao.getOrderAskInfo(orderId, false);
			if(orderAskInfo == null) {
				refundJson.put("auditReason", "-1");
				return false;
			}
			OrderCustomerInfo custInfo = orderCustInfoDao.getOrderCustByGuid(orderAskInfo.getCustId(), false);
			if(custInfo == null) {
				refundJson.put("auditReason", "-1");
				return false;
			}
			//判断客户姓名长度是否超过4位
			if(StringUtils.length(custInfo.getCustName()) > 4) {
				refundJson.put("auditReason", "5");
				return false;
			}
			//工单处理过程中生成的小额退赔跟踪单，审核时需要判断退费号码是否存在一年内历史退费
			boolean dealFlag = this.isComplaintOrder(orderId);
			log.info("dealFlag: {}", dealFlag);
			if(dealFlag) {
				//判断承诺退费总金额大于等于500
				boolean judgeSumFlag = this.judgePrmRefundSumAmount(orderId, prmRefundAmount);
				if(judgeSumFlag) {
					refundJson.put("auditReason", "6");
					return false;
				}
				
				boolean adjustFlag = this.isExistAdjust(orderAskInfo.getRegionId(), refundNum, custInfo.getProdType());
				if(adjustFlag) {
					refundJson.put("auditReason", "2");
					return false;
				}
			} else {
				//承诺退费金额大于200
				if(Double.parseDouble(prmRefundAmount) > 200) {
					refundJson.put("auditReason", "6");
					return false;
				}
			}
			//当前跟踪单有催单记录
			HastenSheetInfo[] hastenInfo = hastenSheetInfoDaoImpl.getOrderHatenInfo(orderId, false);
			if(hastenInfo.length > 0) {
				refundJson.put("auditReason", "4");
				return false;
			}
		} catch(Exception e) {
			log.error("judgeAuditStatus error: {}", e.getMessage(), e);
			refundJson.put("auditReason", "-1");
			return false;
		}
		return true;
	}
	
	/**
	 * 工单处理过程中生成的小额退赔跟踪单，自动审核时需要判断是否有历史退费
	 * @param serviceOrderId
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean isComplaintOrder(String trackOrderId) {
		Map info = trackServiceDao.getTrackInfo(trackOrderId);
		log.info("TrackInfo: {}", JSON.toJSON(info));
		if(info == null) {
			return false;
		}
		String createType = info.get("CREATE_TYPE").toString();//新单创建方式：1、自动生成，2、服务受理，3、接口受理
		return "1".equals(createType);
	}
	
	/**
	 * 是否存在一年内退费
	 * @param regionId
	 * @param refundNum
	 * @param prodType
	 * @return
	 */
	private boolean isExistAdjust(int regionId, String refundNum, int prodType) {
		JSONObject param = new JSONObject();
		param.put("regionId", regionId);
		param.put("prodNum", refundNum);
		param.put("queryType", prodType);
		String adjustRecordStr = interfaceFeign.qryAdjustDetailByProdNum(param.toJSONString());
		if(adjustRecordStr != null) {
			JSONObject adjustRecord = JSON.parseObject(adjustRecordStr);
			String code = adjustRecord.getString("code");
			if("0".equals(code) && adjustRecord.containsKey("adjustRecord")) {
				JSONArray recordList = adjustRecord.getJSONArray("adjustRecord");
				if(!recordList.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 更新充值状态
	 * @param trackOrderId
	 * @param adjustFlag
	 * @param updateDate
	 * @param rechageStatus
	 */
	public int updateRechageStatus(String trackOrderId, String adjustFlag, String updateDate, String rechageStatus){
		log.info("updateRechageStatus trackOrderId: {} adjustFlag: {} updateDate: {} rechageStatus: {}", trackOrderId, adjustFlag, updateDate, rechageStatus);
		int num = 0;
		if(StringUtils.isNotBlank(adjustFlag)) {//查询成功
			String refundStatus = "3";//审核通过（退）
			String status = "";//最新充值状态
			if("success".equals(adjustFlag)) {//到账
				status = "3";
			} 
			else {//未到账
				if("2".equals(rechageStatus)) {//首次查询
					status = "5";

				} else if("5".equals(rechageStatus)) {//超过24小时查询
					refundStatus = "4";
					status = "4";
				}		
			}
			
			log.info("trackOrderId: {} rechageStatusNew: {}", trackOrderId, status);
			if("4".equals(status)) {//超过24小时未到账，需解挂工单，人工处理
				int result = this.unHangupTrackOrder(trackOrderId);
				log.info("trackOrderId: {} unHangupTrackOrder: {}", trackOrderId, result);
			}
			
			try {
				String sql = "UPDATE CC_ORDER_REFUND SET REFUND_STATUS = ?, RECHAG_STATUS = ?, RECHAG_DATE = str_to_date(?, '%Y-%m-%d %H:%i:%s'), UPDATE_DATE = now() WHERE TRACK_ORDER_ID = ?";
				num = this.jt.update(sql, refundStatus, status, updateDate, trackOrderId);
			} catch (Exception e){
				log.error("updateRechageStatus error: {}", e.getMessage(), e);
			}
			log.info("updateRechageStatus trackOrderId: {} result: {}", trackOrderId, num);
		}
		return num;
	}
	
	public String getDispatchStaff(String acceptOrgId) {
		String strsql = "SELECT ORG_ID,LINKID,ORG_NAME,UP_ORG,REGION_ID FROM TSM_ORGANIZATION WHERE ORG_ID=?";
		List<Map<String, Object>> tmpList = jt.queryForList(strsql, acceptOrgId);
		if(tmpList.isEmpty()) {
			return null;
		}
		Map<String, Object>	tmpMap = tmpList.get(0);
		String linkId = tmpMap.get("LINKID").toString();
		String sql = "SELECT C.LOGONNAME FROM cc_refund_dispatch_config C WHERE ? LIKE CONCAT('%', C.ORG_ID, '%') AND C.STATE = 1";
		List<Map<String, Object>> list = jt.queryForList(sql, linkId);
		if(list.isEmpty()) {
			return null;
		}
		Map<String, Object> map = list.get(0);
		String loginName = map.get("LOGONNAME").toString();
		TsmStaff staff = pubFunc.getLogonStaffByLoginName(loginName);
		if(staff == null) {
			return null;
		}
		JSONObject obj = new JSONObject();
		obj.put("loginName", staff.getLogonName());
		obj.put("staffId", staff.getId());
		obj.put("staffName", staff.getName());
		obj.put("orgId", staff.getOrganizationId());
		return obj.toJSONString();
	}
	
	/**
	 * 判断是否是协查台长
	 * @return
	 */
	public boolean isDispatchStaff(String loginName) {
		String sql = "select count(1) from cc_refund_dispatch_config c where c.LOGONNAME = ?";
		int count = jt.queryForObject(sql, new Object[] { loginName }, Integer.class);
		return count > 0;
	}
	
	public int updatePrmRefundAmount(String prmRefundAmount, String orderId) {
		String loginName = pubFunc.getLogonStaff().getLogonName();
		int num = 0;
		try {
			
			String strSql = "UPDATE CC_ORDER_REFUND SET MODIFY_DATE=now(),MODIFY_STAFF=?,PRM_REFUND_AMOUNT=? WHERE TRACK_ORDER_ID=?";
			num = jt.update(strSql, loginName, prmRefundAmount, orderId);
		} catch(Exception e) {
			log.error("updatePrmRefundAmount error: {}", e.getMessage(), e);
		}
		log.info("updatePrmRefundAmount orderId: {} loginName: {} prmRefundAmount: {} result: {}", orderId, loginName, prmRefundAmount, num);
		return num;
	}
	
	public int getRefundApproveCount(String orderId) {
		String sql = "select count(1) from cc_refund_approve_info c where c.SERVICE_ORDER_ID = ? and c.SHEET_STATE in (0, 1, 3)";
		return jt.queryForObject(sql, new Object[] { orderId }, Integer.class);
	}
	
	public int saveRefundApproveInfo(String curSheetId, String mainSheetId, String orderId, String totalAmount, String refundDetail, String refundData) {
		String loginName = pubFunc.getLogonStaff().getLogonName();
		int num = 0;
		try {
			String strSql = "INSERT INTO cc_refund_approve_info "
					+ "(CUR_XC_SHEET_ID,CREATE_DATE,CREATE_STAFF,MAIN_SHEET_ID,SERVICE_ORDER_ID,SHEET_STATE,MODIFY_DATE,PRM_REFUND_TL_AMOUNT,REFUND_DETAIL,REFUND_DATA) "
					+ "VALUES (?, now(), ?, ?, ?, ?, now(), ?, ?, ?)";
			num = jt.update(strSql, curSheetId, loginName, mainSheetId, orderId, 0, totalAmount, refundDetail, refundData);
		} catch(Exception e) {
			log.error("saveRefundApproveInfo error: {}", e.getMessage(), e);
		}
		log.info("saveRefundApproveInfo curSheetId: {} orderId: {} loginName: {} num: {}", curSheetId, orderId, loginName, num);
		return num;
	}
	
	/**
	 * 查询调账审批记录
	 */
	public List<Map<String, Object>> getRefundApproveInfo(String sheetId) {
		String sql = "SELECT CUR_XC_SHEET_ID,DATE_FORMAT(CREATE_DATE, '%Y-%m-%d %H:%i:%s') CREATE_DATE,CREATE_STAFF,MAIN_SHEET_ID,SERVICE_ORDER_ID,SHEET_STATE,"
				+ "DATE_FORMAT(APPROVE_DATE, '%Y-%m-%d %H:%i:%s') APPROVE_DATE,APPROVE_STAFF,DATE_FORMAT(OPER_DATE, '%Y-%m-%d %H:%i:%s') OPER_DATE,OPER_STAFF,"
				+ "DATE_FORMAT(MODIFY_DATE, '%Y-%m-%d %H:%i:%s') MODIFY_DATE,PRM_REFUND_TL_AMOUNT,REFUND_DETAIL,REFUND_DATA FROM cc_refund_approve_info "
				+ "WHERE CUR_XC_SHEET_ID = ?";
		return jt.queryForList(sql, sheetId);
	}
	
	public int updateRefundApproveInfo(String curSheetId, int state) {
		String loginName = pubFunc.getLogonStaff().getLogonName();
		int num = 0;
		try {
			String strSql = "update cc_refund_approve_info set SHEET_STATE = ?, APPROVE_DATE = now(), APPROVE_STAFF = ?, MODIFY_DATE = now() "
					+ "where CUR_XC_SHEET_ID = ?";
			num = jt.update(strSql, state, loginName, curSheetId);
		} catch(Exception e) {
			log.error("updateRefundApproveInfo error: {}", e.getMessage(), e);
		}
		log.info("updateRefundApproveInfo curSheetId: {} state: {} num: {}", curSheetId, state, num);
		return num;
	}
	
	/**
	 * 查询审批通过的调账审批记录
	 */
	public List<Map<String, Object>> getApprovedRefundInfo(String orderId, int state) {
		String sql = "SELECT CUR_XC_SHEET_ID,DATE_FORMAT(CREATE_DATE, '%Y-%m-%d %H:%i:%s') CREATE_DATE,CREATE_STAFF,MAIN_SHEET_ID,SERVICE_ORDER_ID,SHEET_STATE,"
				+ "DATE_FORMAT(APPROVE_DATE, '%Y-%m-%d %H:%i:%s') APPROVE_DATE,APPROVE_STAFF,DATE_FORMAT(OPER_DATE, '%Y-%m-%d %H:%i:%s') OPER_DATE,OPER_STAFF,"
				+ "DATE_FORMAT(MODIFY_DATE, '%Y-%m-%d %H:%i:%s') MODIFY_DATE,PRM_REFUND_TL_AMOUNT,REFUND_DETAIL,REFUND_DATA FROM cc_refund_approve_info "
				+ "WHERE SERVICE_ORDER_ID = ? AND SHEET_STATE = ?";
		return jt.queryForList(sql, orderId, state);
	}
	
	/**
	 * 放弃审批通过的调账记录
	 */
	public int quitApproveInfo(String curSheetId) {
        String loginName = pubFunc.getLogonStaff().getLogonName();
		return trackServiceDao.updateApprovedRefundInfo(curSheetId, 4, loginName);
	}
	
	/**
	 * 判断工单处理时，承诺退费总金额大于等于500
	 * @param trackOrderId
	 * @param prmRefundAmount
	 * @return
	 */
	private boolean judgePrmRefundSumAmount(String trackOrderId, String prmRefundAmount) {
		List<Map<String, Object>> list = this.getMultiProdRefund(trackOrderId);
		log.info("getMultiProdRefund: {}", JSON.toJSON(list));
		if(!list.isEmpty()) {
			Map<String, Object> map = list.get(0);
			//承诺退费总金额
			String prmRefundSumAmount = map.get("PRM_REFUND_TL_AMOUNT").toString();
			if(Double.parseDouble(prmRefundSumAmount) >= 500) {
				return true;
			}
		} else {
			//承诺退费金额大于等于500
			if(Double.parseDouble(prmRefundAmount) >= 500) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 查询多号码退费记录
	 */
	public List<Map<String, Object>> getMultiProdRefund(String trackOrderId) {
		String sql = "SELECT TRACK_ORDER_ID, SERVICE_ORDER_ID, REFUND_NUM, PRM_REFUND_AMOUNT, PRM_REFUND_TL_AMOUNT, DATE_FORMAT(CREATE_DATE, '%Y-%m-%d %H:%i:%s') CREATE_DATE "
				+ "FROM cc_multiprod_refund WHERE TRACK_ORDER_ID = ?";
		return jt.queryForList(sql, trackOrderId);
	}
	
	private void judgeAuditRefund(String serviceOrderId, JSONObject dataJson) {
		try {
			List<Map<String, Object>> list = this.getApprovedRefundInfo(serviceOrderId, 3);//已审批调账
			log.info("getApprovedRefundInfo: {}", JSON.toJSON(list));
			if(!list.isEmpty()) {
				Map<String, Object> map = list.get(0);
				//审批单号
				String xcSheetId = map.get("CUR_XC_SHEET_ID").toString();
				dataJson.put("approve_id", xcSheetId);
			}
		} catch(Exception e) {
			log.error("judgeAuditRefund error: {}", e.getMessage(), e);
		}
	}

}
