/**
 * <p>类名：ISheetPubInfoDao.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：2、增加工单预约功能 updatePrecontract 万荣伟 2008-08-12</p>
 * <p>  Create by:	lifeng	Mar 18, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.worksheet.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.cliqueWorkSheetWebService.pojo.ComplaintConnection;
import com.cliqueWorkSheetWebService.pojo.ComplaintConnectionRmp;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.service.IworkSheetToITSMWebService;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfoRmp;
import com.timesontransfar.customservice.worksheet.pojo.SheetReAssignReason;
import com.timesontransfar.customservice.worksheet.pojo.XcFlow;
import com.timesontransfar.customservice.worksheet.pojo.XcFlowRmp;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class SheetPubInfoDaoImpl implements ISheetPubInfoDao {
	private static final Logger log = LoggerFactory.getLogger(SheetPubInfoDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private IworkSheetToITSMWebService itsmWebservice;
	
	@Autowired
	private PubFunc pubFunc;
	
	private String saveSheetPubInfoSql;//保存工单sql
	private String saveSheetPubInfoHisSql;//保存工单sql
	private String querySheetBySheetIdSql;//根据单号查工单sql
	private String querySheetHisBySheetIdSql;//根据单号查历史工单sql
	private String querySheetStateSql;
	private String updateSheetStateSql;
	private String updateFetchSheetStaffSql;
	private String delSheetPubInfoByOrderIdSql;
	private String saveErrorSheetSql;
	private String saveErrorSheetHisSql;	
	private String qurySheetFlowSql;//查询工单流水(当前)
	private String qurySheetFlowSqlHis;//查询已竣工的工单流水
	private String quryRelatSheetSql;//查询
	private String updateRegionSql;
	private String updateAutoVisitSql;
	private String selectCurDealSheetByOrderIdSql;
	private String selectFirstDealSheetByOrderIdSql;
	private String insertWorkSheetAreaSql;
	private String insertWorkSheetAreaHisSql;
	private String deleteWorkSheetAreaByOrderIdSql;
	private String deleteWorkSheetAreaBySheetIdSql;
	private String updateWorkSheetAreaTacheDateSql;
	private String updateWorkSheetAreaSheetBySheetIdSql;
	private String countWorkSheetAreaByOrderIdSql;
	private String selectWorkSheetAreaBySheetIdSql;
	private String selectLastWorkSheetIdByOrderIdSql;
	private String deleteWorkSheetBySheetIdSql;
	private String selectSheetReceiveDateSql;
	private String updateDealLimitTimeByOrderIdSql;
	private String updateAuditLimitTimeByOrderIdSql;
	private String updateLastXcSheetIdBySheetIdSql;
	private String selectCalloutRecByOrderIdSql;
	private String insertCustomerJudgeSql;
	private String deleteCustomerJudgeByOrderIdSql;
	private String updateCustomerJudgeStatusExceptionSql;
	private String updateCustomerJudgeStatusFromIVRSql;
	private String updateCustomerJudgeStatusToZDHFSql;
	private String updateCustomerJudgeStatusFromZDHFSql;
	private String selectCustomerJudgeByOrderIdSql;
	private String insertCustomerJudgeHisByOrderIdSql;

	/**
	 * 取流向派发部门、流向部门、流向到达时间
	 * @param orderId
	 * @return
	 */
	public List getReturnTime(String orderId) {
		String sql = 
"SELECT B.RETURN_ORG_NAME RETURNORG_FLG, B.RECEIVE_ORG_NAME RETURNORG, DATE_FORMAT(B.CREAT_DATE, '%Y-%m-%d %H:%i:%s') RETURNCREATE\n" +
"  FROM CC_WORK_SHEET_AREA A, CC_WORK_SHEET B\n" + 
" WHERE A.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID\n" + 
"   AND A.SERVICE_ORDER_ID = ?\n" + 
"   AND A.WORK_SHEET_ID = B.WORK_SHEET_ID\n" + 
"   AND A.TACHE_DATE IS NULL\n" + 
"   AND A.AREA_FLAG IN (1, 3)\n" + 
" ORDER BY A.CREAT_DATE DESC LIMIT 1";
		return jdbcTemplate.queryForList(sql, orderId);
	}

	public int saveErrSheetHis(String orderId, String sheetId, int month){
		String insertHis = this.saveSheetPubInfoHisSql + " AND CC_WORK_SHEET.WORK_SHEET_ID = ?";
		int count = this.jdbcTemplate.update(insertHis, orderId, month, sheetId);
		if (count > 0) {
			String dealCur = this.delSheetPubInfoByOrderIdSql + " AND A.WORK_SHEET_ID = ?";
			count = this.jdbcTemplate.update(dealCur, orderId, month, sheetId);
		}
		return count;
	}

	/* (non-Javadoc)
	 * @see com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao#updateErrSheet(java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, int, java.lang.String)
	 */
	public int updateErrSheet(String orderId, String sheetId, String errAppeal,int statu,String statuDesc, int finishFlag){
		String strSql = "UPDATE CC_WORK_SHEET T\n" + 
		                " SET T.DEAL_REQUIRE = ?," +
						" T.SHEET_STATU = ?," +
						" T.SHEET_STATU_DESC = ?," +
						" T.RESPOND_DATE = NOW(), T.REPORT_NUM = ? \n" +
						" WHERE T.WORK_SHEET_ID = ?\n" + 
						" AND T.SERVICE_ORDER_ID = ?";
		return this.jdbcTemplate.update(strSql, errAppeal, statu, statuDesc, finishFlag, sheetId, orderId);
	}
	
	public int updateErrSheet(String orderId, String sheetId, String suredMsg, int statu,String statuDesc,
			Integer dealStaff, String dealStaffName, String dealOrg, String dealOrgName){
		String strSql = "UPDATE CC_WORK_SHEET T\n" + 
						"   SET T.DEAL_REQUIRE     = ?,\n" + 
						"       T.SHEET_STATU      = ?,\n" + 
						"       T.SHEET_STATU_DESC = ?,\n" + 
						"       T.DEAL_STAFF       = ?,\n" + 
						"       T.DEAL_STAFF_NAME  = ?,\n" + 
						"       T.DEAL_ORG_ID      = ?,\n" + 
						"       T.DEAL_ORG_NAME    = ?, T.RESPOND_DATE = NOW() \n" + 
						" WHERE T.SERVICE_ORDER_ID = ?\n" + 
						"   AND T.WORK_SHEET_ID = ?";
		return this.jdbcTemplate.update(strSql, suredMsg, statu, statuDesc, dealStaff,
				dealStaffName, dealOrg, dealOrgName, orderId, sheetId);
	}
       
	public int updateErrSheetType(String orderId, String sheetId, int type, String typeDesc){
		String strSql = "UPDATE CC_WORK_SHEET T\n" +
						"   SET T.SHEET_TYPE = ?, T.SHEET_TYPE_DESC = ?\n" + 
						" WHERE T.SERVICE_ORDER_ID = ?\n" + 
						"   AND T.WORK_SHEET_ID = ?";
		return this.jdbcTemplate.update(strSql, type, typeDesc, orderId, sheetId);
	}
	
	/**
	 * 生成错单
	 * @param sheetPubInfo 工单公共信息对像
	 * @param newSheetId   新生成工单号
	 * @return	保存的记录数
	 */
	public int saveErrSheet(SheetPubInfo sheetPubInfo,String newSheetId){
		String strsql = this.saveErrorSheetSql;
		int count = this.jdbcTemplate.update(strsql,
				sheetPubInfo.getSheetType(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getSheetTypeDesc(),null),
				newSheetId,
				StringUtils.defaultIfEmpty(sheetPubInfo.getServiceOrderId(),null),
				sheetPubInfo.getSheetStatu(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getSheetSatuDesc(),null),
				sheetPubInfo.getDealStaffId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealStaffName(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealOrgId(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealOrgName(),null),
				sheetPubInfo.getRcvStaffId(),
                StringUtils.defaultIfEmpty(sheetPubInfo.getRcvStaffName(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRcvOrgId(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRcvOrgName(),null),
				sheetPubInfo.getRetStaffId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRetStaffName(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRetOrgId(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRetOrgName(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealTypeDesc(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getStatuDate(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealContent(),null),
				sheetPubInfo.getSaveDealContent(),
				sheetPubInfo.getSheetPriValue(),
				sheetPubInfo.getSourceSheetId(),
				sheetPubInfo.getReportNum(),
				sheetPubInfo.getHomeSheet(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getWorkSheetId(),null),
				sheetPubInfo.getMonth()
		);
		log.info("生成错单成功，工单号为：{}，受理单号为：{}", sheetPubInfo.getWorkSheetId(), sheetPubInfo.getServiceOrderId());
		return count;
	}	

	/**
	 * 删除一个定单下所有的工单
	 * @param orderId	定单id
	 * @return	更新的记录数
	 */
	public int delSheetPubInfoByOrderId(String orderId,Integer month){
		//DELETE FROM cc_work_sheet A WHERE a.service_order_id = ?
		return jdbcTemplate.update(this.delSheetPubInfoByOrderIdSql, orderId, month);
	}

	/**
	 * 根据工单号查询工单公共信息
	 * @param sheetId  工单号
	 * @param hisFlag  true 历史 false 当前
	 * @return 工单公共信息对象
	 */
	public SheetPubInfo getSheetPubInfo(String sheetId, boolean hisFlag) {
		String strsql = "";
		if(hisFlag){
			strsql = this.querySheetHisBySheetIdSql;
		}
		else{
			strsql = this.querySheetBySheetIdSql;
		}

		List tmpList = jdbcTemplate.query(strsql, new Object[]{sheetId}, new SheetPubInfoRmp());
		if(tmpList.isEmpty()){
			log.warn("没有查询到单号为:{}的工单，hisflag:{}", sheetId, hisFlag);
			return null;
		}

		SheetPubInfo sheetPubInfo = (SheetPubInfo)tmpList.get(0);
		tmpList.clear();
		return sheetPubInfo;
	}
	/**
	 * 万荣伟 09-02-15增加按月分区地段
	 * 根据工单号和地域查询工单公共信息
	 * @param sheetId	工单号
	 *  @param region	地域
	 * @param hisFlag  当前/历史表
	 * @return	工单公共信息对象
	 */
	public SheetPubInfo getSheetObj(String sheetId,int region,Integer month,boolean hisFlag) {
		String strsql = "";
		if(hisFlag){
			strsql = "SELECT * FROM CC_WORK_SHEET T WHERE T.MONTH_FLAG=? AND T.REGION_ID=? AND T.WORK_SHEET_ID = ?";
		}
		else{
			strsql = "SELECT * FROM CC_WORK_SHEET_HIS T WHERE T.MONTH_FLAG=? AND T.REGION_ID=? AND T.WORK_SHEET_ID = ?";
		}

		List tmpList = jdbcTemplate.query(strsql, new Object[]{month,region,sheetId}, new SheetPubInfoRmp());
		if(tmpList.isEmpty()){
			log.warn("没有查询到单号为：{}的工单，hisflag：{}", sheetId, hisFlag);
			return null;
		}
		return (SheetPubInfo)tmpList.get(0);
	}
	/**
	 * 万荣伟 09-02-15增加按月分区地段
	 * 根据工单号和地域查询工单公共信息
	 * @param sheetId	工单号
	 *  @param region	地域
	 * @param hisFlag  当前/历史表
	 * @return	工单公共信息对象
	 */
	public SheetPubInfo getSheetObjNew(String sheetId,int region,Integer month,String suffix,boolean hisFlag) {
		/*
		 * SELECT * FROM CC_WORK_SHEET T WHERE T.WORK_SHEET_ID = ? //当前
		 * SELECT * FROM * CC_WORK_SHEET_HIS T WHERE T.WORK_SHEET_ID = ? //历史
		 */
		String strsql = "";
		if(suffix.length()>0){
			strsql = "SELECT * FROM CC_WORK_SHEET_HIS_BAK T WHERE T.MONTH_FLAG=? AND T.REGION_ID=? AND T.WORK_SHEET_ID = ?";
		}else{
			if(hisFlag){
				strsql = "SELECT * FROM CC_WORK_SHEET T WHERE T.MONTH_FLAG=? AND T.REGION_ID=? AND T.WORK_SHEET_ID = ?";
			}
			else{
				strsql = "SELECT * FROM CC_WORK_SHEET_HIS T WHERE T.MONTH_FLAG=? AND T.REGION_ID=? AND T.WORK_SHEET_ID = ?";
			}
		}
		
		List tmpList = jdbcTemplate.query(strsql, new Object[]{month,region,sheetId}, new SheetPubInfoRmp());
		if(tmpList.isEmpty()){
			if(log.isDebugEnabled()){
				log.debug("没有查询到单号为:" + sheetId + "的工单,  hisflag : " + hisFlag);
				log.debug("getSheetPubInfo(String sheetId, boolean hisFlag) --end");
			}
			return null;
		}

		SheetPubInfo sheetPubInfo = (SheetPubInfo)tmpList.get(0);
		tmpList.clear();
		tmpList = null;
		return sheetPubInfo;		
	}

	/**
	 * 查询工单的实际处理时限,减去夜间时差
	 * @param sheetId 工单号
	 * @param flag 是否历史	 
	 * @return
	 */
	public String getSheetObjByWorkTime(String sheetId, boolean hisFlag) {
		String workTime = "0";
		String strsql = "";
		if(hisFlag){
			strsql = 
"SELECT DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,"
+ "DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),W.HANGUP_START_TIME,W.RESPOND_DATE),"
+ "'%Y-%m-%d %H:%i:%s')END_DATE,W.HANGUP_TIME_COUNT,A.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE FROM CC_WORK_SHEET W,"
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_LABEL L WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND "
+ "W.WORK_SHEET_ID=?";
		}else{
			strsql = 
"SELECT DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,"
+ "DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),W.HANGUP_START_TIME,W.RESPOND_DATE),"
+ "'%Y-%m-%d %H:%i:%s')END_DATE,W.HANGUP_TIME_COUNT,A.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE FROM CC_WORK_SHEET_HIS W,"
+ "CC_SERVICE_ORDER_ASK_HIS A,CC_SERVICE_LABEL_HIS L WHERE A.ORDER_STATU IN(700000103,3000047,720130002,720130010)AND "
+ "W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=?";
		}
		List list = jdbcTemplate.queryForList(strsql, sheetId);
		if (!list.isEmpty()) {
			Map map = (Map) list.get(0);
			String beginDate = this.getStringByKey(map, "BEGIN_DATE");
			String orderDate = this.getStringByKey(map, "ACCEPT_DATE");
			String endDate = this.getStringByKey(map, "END_DATE");
			int hangupTimeCount = map.get("HANGUP_TIME_COUNT") == null ? 0 : Integer.parseInt(map.get("HANGUP_TIME_COUNT").toString());
			int serviceType = map.get("SERVICE_TYPE") == null ? 0 : Integer.parseInt(map.get("SERVICE_TYPE").toString());
			String sysDate = this.getStringByKey(map, "SYS_DATE");
			workTime = String.valueOf(pubFunc.getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate));
		}
		if (workTime.indexOf("E") > -1) {
			DecimalFormat df = new DecimalFormat("#");
			workTime = df.format(Double.parseDouble(workTime));
		}
		return workTime;
	}
	
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}

	/**
	 * 生成工单的公共信息
	 * @param sheetPubInfo 工单公共信息对像
	 * @param hisFlag  当前/历史表
	 * @return 生成的工单公共信息数量
	 */
	public int saveSheetPubInfo(SheetPubInfo sheetPubInfo) {
		/*
		 * INSERT INTO CC_WORK_SHEET (WORK_SHEET_ID, SERVICE_ORDER_ID,
		 * REGION_ID, REGION_NAME, SERVICE_TYPE, SERVICE_TYPE_DESC, CREAT_DATE,
		 * SHEET_RECEIVE_DATE, RECEIVE_ORG_ID, RECEIVE_ORG_NAME, RECEIVE_STAFF,
		 * RECEIVE_STAFF_NAME, DEAL_REQUIRE, RESPOND_DATE, DEAL_STAFF,
		 * DEAL_STAFF_NAME, DEAL_ORG_ID, DEAL_ORG_NAME, RETURN_STAFF,
		 * RETURN_STAFF_NAME, RETURN_ORG_ID, RETURN_ORG_NAME, SOURCE_SHEET_ID,
		 * SHEET_STATU, STATU_DATE, SHEET_TYPE, DEAL_LIMIT_TIME, TACHE_ID,
		 * WFL_INST_ID, WORKSHEET_SCHEMA_ID, TACHE_INST_ID, LOCK_FLAG,
		 * LOCK_DATE, SHEET_PRI_VALUE, HANGUP_START_TIME, HANGUP_TIME_COUNT,
		 * SHEET_STATU_DESC, TACHE_DESC, PRE_ALARM_VALUE,
		 * ALARM_VALUE,AUTO_VISIT_FLAG) VALUES (?, ?, ?, ?, ?, ?, SYSDATE,
		 * TO_DATE(?, 'yyyy-mm-dd hh24:mi:ss'), ?, ?, ?, ?, ?, TO_DATE(?,
		 * 'yyyy-mm-dd hh24:mi:ss'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, TO_DATE(?,
		 * 'yyyy-mm-dd hh24:mi:ss'), ?, ?, ?, ?, ?, ?, ?, TO_DATE(?, 'yyyy-mm-dd
		 * hh24:mi:ss'), ?, ?, ?, ?, ?, ?, ?,?)
		 */
		String strsql = this.saveSheetPubInfoSql;

		int count = this.jdbcTemplate.update(strsql, 
				StringUtils.defaultIfEmpty(sheetPubInfo.getWorkSheetId(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getServiceOrderId(),null),
				sheetPubInfo.getRegionId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRegionName(),null),
				sheetPubInfo.getServType(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getServTypeDesc(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getSheetRcvDate(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRcvOrgId(),"0"),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRcvOrgName(),null),
				sheetPubInfo.getRcvStaffId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRcvStaffName(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealRequire(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRespondDate(),null),
				sheetPubInfo.getDealStaffId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealStaffName(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealOrgId(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealOrgName(),null),
				sheetPubInfo.getRetStaffId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRetStaffName(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRetOrgId(),"0"),
				StringUtils.defaultIfEmpty(sheetPubInfo.getRetOrgName(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getSourceSheetId(),null),
				sheetPubInfo.getSheetStatu(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getStatuDate(),null),
				sheetPubInfo.getSheetType(),
				sheetPubInfo.getDealLimitTime(),
				sheetPubInfo.getTacheId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getWflInstId(),null),
				sheetPubInfo.getWorkSheetSchemaId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getTacheInstId(),null),
				sheetPubInfo.getLockFlag(),
				sheetPubInfo.getSheetPriValue(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getHangupStrTime(), null),
				sheetPubInfo.getHangupTimeSum(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getSheetSatuDesc(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getTacheDesc(),null),
				sheetPubInfo.getPreAlarmValue(),
				sheetPubInfo.getAlarmValue(),
				sheetPubInfo.getAutoVisitFlag(),
				sheetPubInfo.getPrecontractSign(),
				sheetPubInfo.getMonth(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getReceiveOrgDisplay(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealTypeDesc(),null),
				StringUtils.defaultIfEmpty(sheetPubInfo.getDealContent(),null),
				sheetPubInfo.getMainType(),
				sheetPubInfo.getReceiveRegionId(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getReceiveRegionName(),null),
				sheetPubInfo.getHastentNum(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getSheetTypeDesc(),null),
				sheetPubInfo.getStationLimit(),
				sheetPubInfo.getDealId(),
				sheetPubInfo.getReportNum(),
				sheetPubInfo.getNextTache(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getFlowSequence(),null),
				sheetPubInfo.getPrecontractFlag(),
				StringUtils.defaultIfEmpty(sheetPubInfo.getPrecontractTime(), null),
				sheetPubInfo.getHomeSheet()
		);

		if (log.isDebugEnabled()) {
			log.debug("生成工单成功,工单号为:" + sheetPubInfo.getWorkSheetId());
			log.debug("saveSheetPubInfo(SheetPubInfo sheetPubInfo)   --end");
		}

		return count;
	}	
	/**
	 * 当同一定单下的工单保存进历史表
	 *
	 * @param orderId
	 *            受理单号
	 * @return 保存的记录数
	 */
	public int saveSheetPubInfoHis(String orderId,Integer month) {
		/*
		 * INSERT INTO CC_WORK_SHEET_HIS (WORK_SHEET_ID, SERVICE_ORDER_ID,
		 * REGION_ID, REGION_NAME, SERVICE_TYPE, SERVICE_TYPE_DESC, CREAT_DATE,
		 * SHEET_RECEIVE_DATE, RECEIVE_ORG_ID, RECEIVE_ORG_NAME, RECEIVE_STAFF,
		 * RECEIVE_STAFF_NAME, DEAL_REQUIRE, RESPOND_DATE, DEAL_STAFF,
		 * DEAL_STAFF_NAME, DEAL_ORG_ID, DEAL_ORG_NAME, RETURN_STAFF,
		 * RETURN_STAFF_NAME, RETURN_ORG_ID, RETURN_ORG_NAME, SOURCE_SHEET_ID,
		 * SHEET_STATU, STATU_DATE, SHEET_TYPE, DEAL_LIMIT_TIME, TACHE_ID,
		 * WFL_INST_ID, WORKSHEET_SCHEMA_ID, TACHE_INST_ID, LOCK_FLAG,
		 * LOCK_DATE, SHEET_PRI_VALUE, HANGUP_START_TIME,
		 * HANGUP_TIME_COUNT,SHEET_STATU_DESC, TACHE_DESC, PRE_ALARM_VALUE,
		 * ALARM_VALUE) SELECT * FROM CC_WORK_SHEET B WHERE B.SERVICE_ORDER_ID = ?
		 */
		return jdbcTemplate.update(this.saveSheetPubInfoHisSql, orderId, month);
	}

	/**
	 * 得到一个定单下最新生成的工单信息
	 * @param orderId	受理单id
	 * @return	工单对像
	 */
	public SheetPubInfo getTheLastSheetInfo(String orderId) {
		String strsql = "SELECT * FROM CC_WORK_SHEET T WHERE T.SERVICE_ORDER_ID = ? "
				+ "ORDER BY T.CREAT_DATE DESC";
		List tmpList = jdbcTemplate.query(strsql, new Object[] { orderId },
				new SheetPubInfoRmp());
		if(tmpList.isEmpty()){
			return null;
		}
		SheetPubInfo sheetPubInfo = (SheetPubInfo)tmpList.get(0);

		return sheetPubInfo;
	}

	/**
	 * 得到一个定单下最新生成的处理工单信息
	 * 
	 * @param orderId 受理单id
	 * @return 工单对像
	 */
	public SheetPubInfo getTheLastDealSheetInfo(String orderId) {
		String strsql = "SELECT*FROM cc_work_sheet WHERE sheet_type IN(720130013,700000127)AND service_order_id=?"
				+ " ORDER BY creat_date DESC,work_sheet_id DESC";
		List tmpList = jdbcTemplate.query(strsql, new Object[] { orderId }, new SheetPubInfoRmp());
		if (tmpList.isEmpty()) {
			return null;
		}
		SheetPubInfo sheetPubInfo = (SheetPubInfo) tmpList.get(0);
		return sheetPubInfo;
	}

	/**
	 * 当前环节流程实例
	 * @param wfNodeInstId  当前流程实例
	 * @return
	 */
	public String getCurrentTacheInstId(String wfNodeInstId) {
		String strSql = "select * from tsp_process_instance_node t where t.wfinstanceid=? and t.node_type=1";
		List tmp = this.jdbcTemplate.queryForList(strSql, wfNodeInstId);
		if(tmp.isEmpty()) {
			if(log.isDebugEnabled()) {
				log.debug("没有找到当前环节流程实例");
			}
			return "1";
		}
		Map map = (Map)tmp.get(0);
		String tacheInstId = map.get("INSTANCEID").toString();
		return tacheInstId;
	}

	/* (non-Javadoc)
	 * @see com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao#getAuditSheet(java.lang.String, int)
	 */
	public SheetPubInfo getAuditSheet(String orderId){
		String strsql = "SELECT * FROM CC_WORK_SHEET W WHERE W.SERVICE_ORDER_ID=? AND W.SHEET_TYPE=700000129 AND W.SHEET_STATU IN('600000004','600000005') ORDER BY W.CREAT_DATE DESC";
		List tmpList = jdbcTemplate.query(strsql, new Object[] { orderId }, new SheetPubInfoRmp());
		if(tmpList.isEmpty()){
			return null;
		}
		return (SheetPubInfo)tmpList.get(0);
	}

	public SheetPubInfo getAuditSheetNew(String orderId) {
		String strsql = "SELECT * FROM cc_work_sheet WHERE service_order_id = ? AND tache_id = 700000088 ORDER BY creat_date DESC";
		List tmpList = jdbcTemplate.query(strsql, new Object[] { orderId }, new SheetPubInfoRmp());
		if (tmpList.isEmpty()) {
			return null;
		}
		return (SheetPubInfo) tmpList.get(0);
	}

	/**
	 * 查询工单是否有在途申请单
	 * @param sheetId	工单号
	 *  @param region	地域
	 * @return 状态值
	 */
	public int getRegSheetState(String sheetId,int region) {
		String strsql = "SELECT T.WORKSHEET_ID FROM CC_SHEET_STATU_APPLY T WHERE T.WORKSHEET_ID=? AND T.APPLY_AUD_STATU=0";
		List tmpList = jdbcTemplate.query(strsql, new Object[] { sheetId }, new KeyRowMapper());
		int size = 0;
		size = tmpList.size();
		if (size == 0) {
			log.warn("没有查询到工单号为{}的工单!", sheetId);
			return 0;
		}
		return size;
	}
	
	class KeyRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int arg1) throws SQLException {
			return rs.getString(1);
		}
	}

	public boolean updateSheetState(String sheetId, int state, String stateDesc, Integer month, int lookFlag) {
		int count = 0;
		if(month.equals(14)) {
			String strSql = "UPDATE CC_WORK_SHEET T\n" +
					"   SET T.SHEET_STATU      = ?,\n" + 
					"       T.SHEET_STATU_DESC = ?,\n" + 
					"       T.STATU_DATE       = NOW(),\n" + 
					"       T.LOCK_DATE        = NOW(),\n" + 
					"       T.LOCK_FLAG        = ?\n" + 
					" WHERE T.WORK_SHEET_ID = ?";
			count = this.jdbcTemplate.update(strSql, state, stateDesc, lookFlag, sheetId);
			log.warn("更新工单:{}的处理时间LOCK_DATE时间!", sheetId);
		} else {
			count = this.jdbcTemplate.update(this.updateSheetStateSql, state, stateDesc, lookFlag, sheetId);
		}
		
		if (count != 1) {
			log.warn("更新工单:{}的状态失败!", sheetId);
			return false;
		}
		log.debug("更新工单:{}的状态成功!", sheetId);
		return true;
	}
	
	/**
	 * 更新一个定单下所工单的状态
	 * @param orderId 定单号
	 * @param state	状态值
	 * @param stateDesc	状态描述
	 * @return	是否成功
	 */
	public boolean updateSheetStateByOrder(String orderId, int state,
			String stateDesc,int lookFlag,Integer month) {
		String strsql = "UPDATE CC_WORK_SHEET T SET T.SHEET_STATU = ?, "
				+ "T.SHEET_STATU_DESC = ?, T.STATU_DATE = NOW(),T.LOCK_FLAG=? "
				+ "WHERE T.SERVICE_ORDER_ID = ? AND T.MONTH_FLAG=?";
		this.jdbcTemplate.update(strsql, state, stateDesc, lookFlag,
				orderId, month);
		
		return true;
	}

    /**
     * 更新环节下所有工单为完成
     * @param orderId
     * @param state
     * @param stateDesc
     * @param lookFlag
     * @param month
     * @return
     */
	public int updateTachSheetFinsh(String orderId, int state, String stateDesc, int lookFlag,
            Integer month, int tachId) {

        String sql = "SELECT W.WORK_SHEET_ID FROM CC_WORK_SHEET W WHERE W.SERVICE_ORDER_ID = ? AND W.TACHE_ID = ? AND W.LOCK_FLAG NOT IN (2, 9) AND W.RECEIVE_ORG_ID = '362813'";
        List list = this.jdbcTemplate.queryForList(sql, orderId, tachId);

        String strSql = "UPDATE CC_WORK_SHEET W SET W.SHEET_STATU=?,W.SHEET_STATU_DESC=?,\n"
                + "W.STATU_DATE=NOW(),W.RESPOND_DATE=NOW(),W.LOCK_FLAG=?,W.DEAL_STAFF_NAME='SYSTEM',W.DEAL_ORG_NAME='SYSTEM' ,W.DEAL_CONTENT='系统自动处理' "
                + " WHERE W.MONTH_FLAG=? AND W.SERVICE_ORDER_ID=? AND\n"
                + "W.TACHE_ID=? AND W.LOCK_FLAG NOT IN (2,9) ";
        int size = this.jdbcTemplate.update(strSql, state, stateDesc, lookFlag, month, orderId, tachId);

        if (!list.isEmpty()) {
            Map resultMap = null;
            for (int i = 0; i < list.size(); i++) {
                resultMap = ((Map) list.get(i));
                this.itsmWebservice.executeForCANCEL(resultMap.get("WORK_SHEET_ID").toString(), "1");
            }
        }
        return size;
    }

    /**
     * 工单追回，把处理工单为作废
     * @param state 状态
     * @param stateDesc 状态名 
     * @param month 月分区
     * @param tachId 环节
     * @param where 条件
     * @return
     */
	public int updateDealDisannuul(int state,String stateDesc,Integer month,int tachId,String where) {
		String strSql =	
            "UPDATE CC_WORK_SHEET W\n" +
            "   SET W.SHEET_STATU      = ?,\n" + 
            "       W.SHEET_STATU_DESC = ?,\n" + 
            "       W.STATU_DATE       = NOW(),\n" +
            "       W.RESPOND_DATE     = NOW(),\n" +
            "       W.LOCK_FLAG        = 9,\n" + 
            "       W.DEAL_STAFF_NAME  = 'SYSTEM',\n" + 
            "       W.DEAL_ORG_NAME    = 'SYSTEM',\n" + 
            "       W.DEAL_CONTENT     = '系统自动处理'\n" + 
            " WHERE W.MONTH_FLAG = ?\n" + 
            "   AND W.TACHE_ID = ?\n" + where;
	 	int size = this.jdbcTemplate.update(strSql, state, stateDesc, month, tachId);//CodeSec未验证的SQL注入；CodeSec误报：2
        String sql = "SELECT W.WORK_SHEET_ID FROM CC_WORK_SHEET W WHERE W.RECEIVE_ORG_ID = '362813' " + where;
        List list = this.jdbcTemplate.queryForList(sql);//CodeSec未验证的SQL注入；CodeSec误报：1
        if (!list.isEmpty()) {
            Map resultMap = null;
            for (int i = 0; i < list.size(); i++) {
                resultMap = ((Map) list.get(i));
                this.itsmWebservice.executeForCANCEL(resultMap.get("WORK_SHEET_ID").toString(), "1");
            }
        }
		return size;		
	}

	/**
	 * 更新取工单的提单的相关信息
	 *
	 * @param sheetId
	 *            工单号
	 * @param staffId
	 *            员工id
	 * @param staffName
	 *            员工名
	 * @param orgId
	 *            组织机构id
	 * @param orgName
	 *            组织机构名
	 * @return 更新的记录数.
	 */
	public int updateFetchSheetStaff(String sheetId, int staffId,
			String staffName, String orgId, String orgName) {
		/*
		 * UPDATE CC_WORK_SHEET T SET T.DEAL_STAFF = ?, T.DEAL_STAFF_NAME = ?,
		 * T.DEAL_ORG_ID = ?, T.DEAL_ORG_NAME = ? WHERE T.WORK_SHEET_ID = ?
		 */
    	return jdbcTemplate.update(this.updateFetchSheetStaffSql, staffId, staffName, orgId, orgName, staffId, staffName,sheetId);
	}

	/**
	 * 更新工单的收单部门
	 *
	 * @param sheetId 工单号
	 * @param orgId   组织机构id
	 * @param orgName 组织机构名
	 * @return 更新的记录数.
	 */
	public int updateReceiveOrgBySheetId(String orgId, String orgName, String sheetId) {
		String sql = "UPDATE cc_work_sheet SET receive_org_id=?,receive_org_name=?,distill_date=NOW()WHERE work_sheet_id=?";
		return jdbcTemplate.update(sql, orgId, orgName, sheetId);
	}

	public int updateFetchSheetStaffNew(String sheetId, int staffId,
			String staffName, String orgId, String orgName, String distillDate) {
		String sql = "UPDATE CC_WORK_SHEET T SET T.DEAL_STAFF = ?," +
						"T.DEAL_STAFF_NAME = ?, T.DEAL_ORG_ID = ?," +
						"T.DEAL_ORG_NAME = ?, T.RECEIVE_STAFF= ?," +
						"T.RECEIVE_STAFF_NAME= ?,T.DISTILL_DATE= STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s') " +
						"WHERE T.WORK_SHEET_ID = ?";
		return jdbcTemplate.update(sql, staffId, staffName, orgId, orgName, staffId, staffName, distillDate, sheetId);
	}

	/**
	 * 更新工单的处理要求
	 * @param sheetId	工单号 
	 * @param require   要求
	 * @param regcOrgList 派单部门列表   
	 * @param dealType  操作类型   
	 * @param dealContent 操作内容
	 *  @param dealId 处理类型ID    
	 * @return	处理记录数
	 */
	public int updateSheetDealRequire(String sheetId, String require,String regcOrgList,String dealType,String dealContent,int dealId,int nextTache) {
		String strsql = "UPDATE CC_WORK_SHEET T SET T.DEAL_REQUIRE=?," +
				"T.RECEIVE_ORG_DISPLAY=?,T.DEAL_DESC=?,T.DEAL_CONTENT=?,T.DEAL_ID=?,T.NEST_TACHE=? WHERE  T.WORK_SHEET_ID=?";
		return jdbcTemplate.update(strsql,require,regcOrgList,dealType,dealContent,dealId,nextTache,sheetId);
	}
	
	private static final  String UPDATE_DEAL_CONTENT_SQL = "UPDATE CC_WORK_SHEET T SET T.DEAL_CONTENT = ? WHERE T.WORK_SHEET_ID = ?";
	public int updateDealContent(String sheetId, String dealContent){
	    return jdbcTemplate.update(UPDATE_DEAL_CONTENT_SQL, dealContent, sheetId);
	}
	
	/**
	 * 更新工单完成时间	
	 * @param sheetId 工单号 
	 * @return	处理记录数
	 */
	public int updateSheetFinishDate(String sheetId){
		String strsql = "UPDATE CC_WORK_SHEET T " +
				"SET T.RESPOND_DATE = NOW(),T.STATU_DATE=NOW() WHERE T.WORK_SHEET_ID = ?";
		return jdbcTemplate.update(strsql, sheetId);
	}
	
	/**
	 * 部门处理保存处理内容
	 * @param sheetId
	 * @param regionId
	 * @param content
	 * @return
	 */
	public int saveDealContent(String sheetId,int regionId,String content) {
		String strSql = "UPDATE CC_WORK_SHEET W SET W.SAVE_DEALCONTEN=? WHERE W.REGION_ID=? AND W.WORK_SHEET_ID=?";
    	return jdbcTemplate.update(strSql, content, regionId, sheetId);
	}
	
	/**
	 * 工单通用查询,自带WHERE条件
	 * @param strWhere
	 * @param boo true 当前
	 * @return
	 */
	public List getSheetCondition(String strWhere,boolean boo) {
		String strSql = "";
		if(boo) {
			strSql = "SELECT * FROM CC_WORK_SHEET WHERE 1=1";
		} else {
			strSql = "SELECT * FROM CC_WORK_SHEET_HIS WHERE 1=1";
		}
		strSql = strSql+strWhere;
		return this.jdbcTemplate.queryForList(strSql);//CodeSec未验证的SQL注入；CodeSec误报：1
	}
	/**
	 * 工单通用查询,自带WHERE条件
	 * @param strWhere
	 * @param boo true 当前
	 * @return
	 */
	public List getSheetListCondition(String strWhere,boolean boo) {
		String strSql = "";
		if(boo) {
			strSql = "SELECT * FROM CC_WORK_SHEET WHERE 1=1";
		} else {
			strSql = "SELECT * FROM CC_WORK_SHEET_HIS WHERE 1=1";
		}
		strSql = strSql+strWhere;
		return  this.jdbcTemplate.query(strSql, new SheetPubInfoRmp());//CodeSec未验证的SQL注入；CodeSec误报：1
	}
	/**
	 * 检查员工是否有处理的中工单,挂起工单除外
	 * @param strWhere
	 * @return 
	 */
	public int checkStaffSheet(String strWhere) {		
		String strSql =	"SELECT COUNT(W.WORK_SHEET_ID) FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A\n" +
						"WHERE W.REGION_ID = A.REGION_ID AND W.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID\n" + 
						"AND W.LOCK_FLAG IN (1,3) AND W.SHEET_STATU != 700000046 "+strWhere;
		return this.jdbcTemplate.queryForObject(strSql,Integer.class);//CodeSec未验证的SQL注入；CodeSec误报：1
	}
	/**
	 * 获取派单环节的数量
	 * @param strWhere
	 * @return
	 */
	public int getSheetNumOfSendTache(String strWhere){
		String strSql =	"SELECT COUNT(W.WORK_SHEET_ID) FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A\n" +
				"WHERE W.REGION_ID = A.REGION_ID AND W.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID\n" + 
				"AND TACHE_ID=" + StaticData.TACHE_ASSIGN + " AND W.SHEET_STATU != 700000046 AND W.LOCK_FLAG IN (1,3) " + strWhere;
		int size = 0; 
		size = this.jdbcTemplate.queryForObject(strSql,Integer.class);//CodeSec未验证的SQL注入；CodeSec误报：1
		return size;
	}
	/**
	 * 根据工单号取得该工单相同的来源工单
	 * @param sheetId 本工单ID
	 * @param month 月分区
	 * @return 返回Sting 数组
	 */
	public String[] getSouresheetObj(String sheetId,Integer month) {
		String strSql = 
			"select B.WORK_SHEET_ID\n" +
			"  from cc_work_sheet A, cc_work_sheet B\n" + 
			" where A.MONTH_FLAG = ?\n" + 
			"   AND A.work_sheet_id = ?\n" + 
			"   AND A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" + 
			"   AND A.SOURCE_SHEET_ID = B.SOURCE_SHEET_ID";
		List tmp = jdbcTemplate.query(strSql, new Object[]{month, sheetId}, new SheetRowMapper());
		int size = tmp.size();
		if(size == 0) {
			log.debug("为找到工单号为{}相同的来源工单的工单", sheetId);
			return new String[0];
		}
		String[] sheetIdList = new String[size];
		for(int i=0;i<size;i++) {
			sheetIdList[i] = (String)tmp.get(i);
		}
		return sheetIdList;
	}

	/**
	 * 得到定单流水最大顺序号
	 * @param orderId 定单号
	 * @param regionId 地域
	 * @return
	 */
	public int getFlowSeq(String orderId,int regionId) {
		String strSql = "SELECT W.FLOW_SEQUENCE FROM CC_WORK_SHEET W WHERE W.REGION_ID=? AND W.SERVICE_ORDER_ID=? ORDER BY W.FLOW_SEQUENCE DESC";
		List list = this.jdbcTemplate.queryForList(strSql, regionId, orderId);
		int seq = 1;
		if(!list.isEmpty()) {
			Map map = (Map)list.get(0);
			if(map.get("FLOW_SEQUENCE") != null ) {
				seq = Integer.parseInt(map.get("FLOW_SEQUENCE").toString());
				map.clear();
			}
			list.clear();
		}
		return seq;
	}
	/**
	 * 查询定单流水
	 * @param orderId
	 * @param month
	 * @param boo
	 * @return
	 */
	public SheetPubInfo[] getWorksheetFlow(String orderId,Integer month,boolean boo) {
		String strSql = "";
		if(boo) {
			strSql = this.qurySheetFlowSql;//查询未竣工的工单流水
		} else {
			strSql = this.qurySheetFlowSqlHis;//查询已竣工流水
		}
		List tmp = jdbcTemplate.query(strSql, new Object[]{orderId, month}, new SheetPubInfoRmp());
		int size = tmp.size();
		if(size == 0) {
			return new SheetPubInfo[0];
		}
		SheetPubInfo[] workFlow = new SheetPubInfo[size];
		for(int i=0;i<size;i++) {
			workFlow[i] = (SheetPubInfo) tmp.get(i);
		}
		return workFlow;
	}

	/**
	 * 查询部门审批单或审核工单的关联工单
	 * @param sheetId 工单号
	 * @param month 月分区标志
	 * @return 工单对象
	 */
	public SheetPubInfo[] getRelatSheet(String sheetId,Integer month) {
		String strSql = this.quryRelatSheetSql;
		List tmp = jdbcTemplate.query(strSql,new Object[]{month,sheetId},new SheetPubInfoRmp());
		int size = tmp.size();
		if(size == 0) {
			log.error("未查询到工单号为:"+sheetId+"相关联的工单");
		}
		SheetPubInfo[] workFlow = new SheetPubInfo[size];
		for(int i=0;i<size;i++) {
			workFlow[i] = (SheetPubInfo) tmp.get(i);
		}
		return workFlow;		
	}
	
	/**
	 * 更新工单的挂起时间
	 * @param startTime 挂起开始
	 * @param holdTime 挂起的总时间
	 * @param region 地域
	 * @param sheetId 工单号
	 * @return
	 */
	public int updateTotalHold(String startTime,int holdTime,int region,String sheetId) {
		String strSql = "UPDATE CC_WORK_SHEET W SET W.HANGUP_START_TIME=NOW(),W.STATU_DATE=NOW(),W.HANGUP_TIME_COUNT=?\n" +
						"WHERE W.REGION_ID=? AND W.WORK_SHEET_ID=?";
		return this.jdbcTemplate.update(strSql, holdTime, region, sheetId);
	}
	
	/**
	 * 更新工单的解挂时间
	 * @param startTime 解挂开始
	 * @param holdTime 挂起的总时间
	 * @param region 地域
	 * @param sheetId 工单号
	 * @return
	 */
	public int updateTotalHoldNew(int holdTime,int region,String sheetId) {
		String strSql = "UPDATE CC_WORK_SHEET W SET W.STATU_DATE=NOW(),W.HANGUP_TIME_COUNT=? WHERE W.REGION_ID=? AND W.WORK_SHEET_ID=?";
		return this.jdbcTemplate.update(strSql, holdTime, region, sheetId);
	}
	
	/* ====================以下为=get/set方法=============================== */

	class SheetRowMapper implements RowMapper {
		public Object mapRow(ResultSet rs, int arg1) throws SQLException {
			return rs.getString(1);
		}
	}

	private String queryLatestSheetByType;

	public SheetPubInfo getLatestSheetByType(String orderId, int sheetType, int mainFlag){
        List tmp = jdbcTemplate.query(queryLatestSheetByType, new Object[]{orderId, sheetType, mainFlag},
                new SheetPubInfoRmp());
        if(tmp.isEmpty()){
            return null;
        }
        return (SheetPubInfo)tmp.get(0);
	}

	private String queryLastSheetNoSystemByTypeSql;

	public SheetPubInfo queryLastSheetNoSystemByType(String orderId, int sheetType, int mainFlag){
        List tmp = jdbcTemplate.query(queryLastSheetNoSystemByTypeSql, new Object[]{orderId, sheetType, mainFlag},
                new SheetPubInfoRmp());
        if(tmp.isEmpty()){
            return null;
        }
        return (SheetPubInfo)tmp.get(0);
	}

	public int updateRegion(String serviceOrderId, int newRegion, String newRegionName, int oldRegion, Integer month){
		//UPDATE CC_WORK_SHEET S SET S.REGION_ID = ?, S.REGION_NAME = ? WHERE S.SERVICE_ORDER_ID = ? AND S.REGION_ID = ? AND S.MONTH_FLAG = ?
		return jdbcTemplate.update(updateRegionSql, newRegion, newRegionName, serviceOrderId, oldRegion, month);
	}
	
	public int updateAutoVisit(int autoVisitFlag, int reportNum, int homeSheet, String workSheetId) {
		return jdbcTemplate.update(updateAutoVisitSql, autoVisitFlag, reportNum, homeSheet, workSheetId);
	}

	public List queryCurDealSheetByOrderId(String orderId) {
		return jdbcTemplate.queryForList(selectCurDealSheetByOrderIdSql, orderId);
	}

	public List querytFirstDealSheetByOrderId(String orderId) {
		return jdbcTemplate.queryForList(selectFirstDealSheetByOrderIdSql, orderId);
	}

	public int insertWorkSheetArea(String serviceOrderId, String workSheetId, String receiveAreaOrgId, int areaFlag) {
		if (areaFlag == 1) {
			updateWorkSheetAreaTacheDate(serviceOrderId);
		}
		
		return jdbcTemplate.update(insertWorkSheetAreaSql, serviceOrderId, workSheetId, receiveAreaOrgId, areaFlag);
	}

	public int deleteWorkSheetAreaByOrderId(String serviceOrderId) {
		jdbcTemplate.update(insertWorkSheetAreaHisSql, serviceOrderId);
		return jdbcTemplate.update(deleteWorkSheetAreaByOrderIdSql, serviceOrderId);
	}

	public int deleteWorkSheetAreaBySheetId(String workSheetId) {
		return jdbcTemplate.update(deleteWorkSheetAreaBySheetIdSql, workSheetId);
	}

	public int updateWorkSheetAreaTacheDate(String serviceOrderId) {
		return jdbcTemplate.update(updateWorkSheetAreaTacheDateSql, serviceOrderId);
	}

	public int updateWorkSheetAreaSheetBySheetId(String newSheetId, String receiveAreaOrgId, int areaFlag, String oldSheetId) {
		return jdbcTemplate.update(updateWorkSheetAreaSheetBySheetIdSql, newSheetId, receiveAreaOrgId, areaFlag, oldSheetId);
	}

	public int countWorkSheetAreaByOrderId(String serviceOrderId, int areaFlag) {
		StringBuffer strSql = new StringBuffer(countWorkSheetAreaByOrderIdSql);
		if (areaFlag == 1) {
			strSql.append(" AND area_flag = 1");
		} else if (areaFlag == 2) {
			strSql.append(" AND area_flag = 2");
		} else if (areaFlag == 3) {
			strSql.append(" AND area_flag = 3");
		}
		return jdbcTemplate.queryForObject(strSql.toString(), new Object[] { serviceOrderId },Integer.class);
	}

	public int selectWorkSheetAreaBySheetId(String workSheetId) {
		try {
			List<Integer> tmp = jdbcTemplate.queryForList(selectWorkSheetAreaBySheetIdSql, new Object[] { workSheetId },Integer.class);
			if(!tmp.isEmpty()) {
				return tmp.get(0);
			}
			return 0;
		} catch (Exception e) {
			return 0;
		}
	}

	public String selectLastWorkSheetIdByOrderId(String serviceOrderId) {
		List list = jdbcTemplate.query(selectLastWorkSheetIdByOrderIdSql, new Object[] { serviceOrderId }, new KeyRowMapper());
		if (list.isEmpty()) {
			return "";
		}
		String workSheetId = "";
		if (null != list.get(0)) {
			workSheetId = list.get(0).toString();
		}
		list.clear();
		list = null;
		return workSheetId;
	}

	public int deleteWorkSheetBySheetId(String workSheetId) {
		return jdbcTemplate.update(deleteWorkSheetBySheetIdSql, workSheetId);
	}

	public String selectSheetReceiveDate(String workSheetId) {
		try {
			List<String> tmp = jdbcTemplate.queryForList(selectSheetReceiveDateSql, new Object[] { workSheetId }, String.class);
			if(tmp.isEmpty()) {
	        	return null;
	        }
			return tmp.get(0);
		} catch (Exception e) {
			return null;
		}
	}

	public int updateDealLimitTimeByOrderId(int dealLimitTime, String serviceOrderId) {
		return jdbcTemplate.update(updateDealLimitTimeByOrderIdSql, dealLimitTime, serviceOrderId);
	}

	public int updateAuditLimitTimeByOrderId(int auditLimitTime, String serviceOrderId) {
		return jdbcTemplate.update(updateAuditLimitTimeByOrderIdSql, auditLimitTime, serviceOrderId);
	}
	
	/**
	 * @return the saveSheetPubInfoSql
	 */
	public String getSaveSheetPubInfoSql() {
		return saveSheetPubInfoSql;
	}

	/**
	 * @param saveSheetPubInfoSql
	 *            the saveSheetPubInfoSql to set
	 */
	public void setSaveSheetPubInfoSql(String saveSheetPubInfoSql) {
		this.saveSheetPubInfoSql = saveSheetPubInfoSql;
	}

	/**
	 * @return the querySheetBySheetIdSql
	 */
	public String getQuerySheetBySheetIdSql() {
		return querySheetBySheetIdSql;
	}

	/**
	 * @param querySheetBySheetIdSql the querySheetBySheetIdSql to set
	 */
	public void setQuerySheetBySheetIdSql(String querySheetBySheetIdSql) {
		this.querySheetBySheetIdSql = querySheetBySheetIdSql;
	}

	/**
	 * @return the querySheetHisBySheetIdSql
	 */
	public String getQuerySheetHisBySheetIdSql() {
		return querySheetHisBySheetIdSql;
	}

	/**
	 * @param querySheetHisBySheetIdSql the querySheetHisBySheetIdSql to set
	 */
	public void setQuerySheetHisBySheetIdSql(String querySheetHisBySheetIdSql) {
		this.querySheetHisBySheetIdSql = querySheetHisBySheetIdSql;
	}

	/**
	 * @return the saveSheetPubInfoHisSql
	 */
	public String getSaveSheetPubInfoHisSql() {
		return saveSheetPubInfoHisSql;
	}

	/**
	 * @param saveSheetPubInfoHisSql the saveSheetPubInfoHisSql to set
	 */
	public void setSaveSheetPubInfoHisSql(String saveSheetPubInfoHisSql) {
		this.saveSheetPubInfoHisSql = saveSheetPubInfoHisSql;
	}

	/**
	 * @return the querySheetStateSql
	 */
	public String getQuerySheetStateSql() {
		return querySheetStateSql;
	}

	/**
	 * @param querySheetStateSql the querySheetStateSql to set
	 */
	public void setQuerySheetStateSql(String querySheetStateSql) {
		this.querySheetStateSql = querySheetStateSql;
	}

	/**
	 * @return the updateSheetStateSql
	 */
	public String getUpdateSheetStateSql() {
		return updateSheetStateSql;
	}

	/**
	 * @param updateSheetStateSql the updateSheetStateSql to set
	 */
	public void setUpdateSheetStateSql(String updateSheetStateSql) {
		this.updateSheetStateSql = updateSheetStateSql;
	}

	/**
	 * @return the updateFetchSheetStaffSql
	 */
	public String getUpdateFetchSheetStaffSql() {
		return updateFetchSheetStaffSql;
	}

	/**
	 * @param updateFetchSheetStaffSql the updateFetchSheetStaffSql to set
	 */
	public void setUpdateFetchSheetStaffSql(String updateFetchSheetStaffSql) {
		this.updateFetchSheetStaffSql = updateFetchSheetStaffSql;
	}

	/**
	 * @return the delSheetPubInfoByOrderIdSql
	 */
	public String getDelSheetPubInfoByOrderIdSql() {
		return delSheetPubInfoByOrderIdSql;
	}

	/**
	 * @param delSheetPubInfoByOrderIdSql the delSheetPubInfoByOrderIdSql to set
	 */
	public void setDelSheetPubInfoByOrderIdSql(String delSheetPubInfoByOrderIdSql) {
		this.delSheetPubInfoByOrderIdSql = delSheetPubInfoByOrderIdSql;
	}

	/**
	 * @return qurySheetFlowSql
	 */
	public String getQurySheetFlowSql() {
		return qurySheetFlowSql;
	}
	/**
	 * @param qurySheetFlowSql 要设置的 qurySheetFlowSql
	 */
	public void setQurySheetFlowSql(String qurySheetFlowSql) {
		this.qurySheetFlowSql = qurySheetFlowSql;
	}
	/**
	 * @return qurySheetFlowSqlHis
	 */
	public String getQurySheetFlowSqlHis() {
		return qurySheetFlowSqlHis;
	}
	/**
	 * @param qurySheetFlowSqlHis 要设置的 qurySheetFlowSqlHis
	 */
	public void setQurySheetFlowSqlHis(String qurySheetFlowSqlHis) {
		this.qurySheetFlowSqlHis = qurySheetFlowSqlHis;
	}
	/**
	 * @return quryRelatSheetSql
	 */
	public String getQuryRelatSheetSql() {
		return quryRelatSheetSql;
	}
	/**
	 * @param quryRelatSheetSql 要设置的 quryRelatSheetSql
	 */
	public void setQuryRelatSheetSql(String quryRelatSheetSql) {
		this.quryRelatSheetSql = quryRelatSheetSql;
	}

	public String getSaveErrorSheetSql() {
		return saveErrorSheetSql;
	}

	public void setSaveErrorSheetSql(String saveErrorSheetSql) {
		this.saveErrorSheetSql = saveErrorSheetSql;
	}

	public String getSaveErrorSheetHisSql() {
		return saveErrorSheetHisSql;
	}

	public void setSaveErrorSheetHisSql(String saveErrorSheetHisSql) {
		this.saveErrorSheetHisSql = saveErrorSheetHisSql;
	}

	/**
     * 取得queryLatestSheetByType
     * @return 返回queryLatestSheetByType。
     */
    public String getQueryLatestSheetByType() {
        return queryLatestSheetByType;
    }

    /**
     * 设置queryLatestSheetByType
     * @param queryLatestSheetByType 要设置的queryLatestSheetByType。
     */
    public void setQueryLatestSheetByType(String queryLatestSheetByType) {
        this.queryLatestSheetByType = queryLatestSheetByType;
    }

	public String getQueryLastSheetNoSystemByTypeSql() {
		return queryLastSheetNoSystemByTypeSql;
	}

	public void setQueryLastSheetNoSystemByTypeSql(String queryLastSheetNoSystemByTypeSql) {
		this.queryLastSheetNoSystemByTypeSql = queryLastSheetNoSystemByTypeSql;
	}

	public void setUpdateRegionSql(String updateRegionSql) {
		this.updateRegionSql = updateRegionSql;
	}

	public String getUpdateAutoVisitSql() {
		return updateAutoVisitSql;
	}

	public void setUpdateAutoVisitSql(String updateAutoVisitSql) {
		this.updateAutoVisitSql = updateAutoVisitSql;
	}

	public String getSelectCurDealSheetByOrderIdSql() {
		return selectCurDealSheetByOrderIdSql;
	}

	public void setSelectCurDealSheetByOrderIdSql(String selectCurDealSheetByOrderIdSql) {
		this.selectCurDealSheetByOrderIdSql = selectCurDealSheetByOrderIdSql;
	}

	public String getSelectFirstDealSheetByOrderIdSql() {
		return selectFirstDealSheetByOrderIdSql;
	}

	public void setSelectFirstDealSheetByOrderIdSql(String selectFirstDealSheetByOrderIdSql) {
		this.selectFirstDealSheetByOrderIdSql = selectFirstDealSheetByOrderIdSql;
	}

	public String getInsertWorkSheetAreaSql() {
		return insertWorkSheetAreaSql;
	}

	public void setInsertWorkSheetAreaSql(String insertWorkSheetAreaSql) {
		this.insertWorkSheetAreaSql = insertWorkSheetAreaSql;
	}

	public String getInsertWorkSheetAreaHisSql() {
		return insertWorkSheetAreaHisSql;
	}

	public void setInsertWorkSheetAreaHisSql(String insertWorkSheetAreaHisSql) {
		this.insertWorkSheetAreaHisSql = insertWorkSheetAreaHisSql;
	}

	public String getDeleteWorkSheetAreaByOrderIdSql() {
		return deleteWorkSheetAreaByOrderIdSql;
	}

	public void setDeleteWorkSheetAreaByOrderIdSql(String deleteWorkSheetAreaByOrderIdSql) {
		this.deleteWorkSheetAreaByOrderIdSql = deleteWorkSheetAreaByOrderIdSql;
	}

	public String getDeleteWorkSheetAreaBySheetIdSql() {
		return deleteWorkSheetAreaBySheetIdSql;
	}

	public void setDeleteWorkSheetAreaBySheetIdSql(String deleteWorkSheetAreaBySheetIdSql) {
		this.deleteWorkSheetAreaBySheetIdSql = deleteWorkSheetAreaBySheetIdSql;
	}

	public String getUpdateWorkSheetAreaTacheDateSql() {
		return updateWorkSheetAreaTacheDateSql;
	}

	public void setUpdateWorkSheetAreaTacheDateSql(String updateWorkSheetAreaTacheDateSql) {
		this.updateWorkSheetAreaTacheDateSql = updateWorkSheetAreaTacheDateSql;
	}

	public String getUpdateWorkSheetAreaSheetBySheetIdSql() {
		return updateWorkSheetAreaSheetBySheetIdSql;
	}

	public void setUpdateWorkSheetAreaSheetBySheetIdSql(String updateWorkSheetAreaSheetBySheetIdSql) {
		this.updateWorkSheetAreaSheetBySheetIdSql = updateWorkSheetAreaSheetBySheetIdSql;
	}

	public String getCountWorkSheetAreaByOrderIdSql() {
		return countWorkSheetAreaByOrderIdSql;
	}

	public void setCountWorkSheetAreaByOrderIdSql(String countWorkSheetAreaByOrderIdSql) {
		this.countWorkSheetAreaByOrderIdSql = countWorkSheetAreaByOrderIdSql;
	}

	public String getSelectWorkSheetAreaBySheetIdSql() {
		return selectWorkSheetAreaBySheetIdSql;
	}

	public void setSelectWorkSheetAreaBySheetIdSql(String selectWorkSheetAreaBySheetIdSql) {
		this.selectWorkSheetAreaBySheetIdSql = selectWorkSheetAreaBySheetIdSql;
	}

	public String getSelectLastWorkSheetIdByOrderIdSql() {
		return selectLastWorkSheetIdByOrderIdSql;
	}

	public void setSelectLastWorkSheetIdByOrderIdSql(String selectLastWorkSheetIdByOrderIdSql) {
		this.selectLastWorkSheetIdByOrderIdSql = selectLastWorkSheetIdByOrderIdSql;
	}

	public String getDeleteWorkSheetBySheetIdSql() {
		return deleteWorkSheetBySheetIdSql;
	}

	public void setDeleteWorkSheetBySheetIdSql(String deleteWorkSheetBySheetIdSql) {
		this.deleteWorkSheetBySheetIdSql = deleteWorkSheetBySheetIdSql;
	}

	public String getSelectSheetReceiveDateSql() {
		return selectSheetReceiveDateSql;
	}

	public void setSelectSheetReceiveDateSql(String selectSheetReceiveDateSql) {
		this.selectSheetReceiveDateSql = selectSheetReceiveDateSql;
	}

	public String getUpdateDealLimitTimeByOrderIdSql() {
		return updateDealLimitTimeByOrderIdSql;
	}

	public void setUpdateDealLimitTimeByOrderIdSql(String updateDealLimitTimeByOrderIdSql) {
		this.updateDealLimitTimeByOrderIdSql = updateDealLimitTimeByOrderIdSql;
	}

	public String getUpdateAuditLimitTimeByOrderIdSql() {
		return updateAuditLimitTimeByOrderIdSql;
	}

	public void setUpdateAuditLimitTimeByOrderIdSql(String updateAuditLimitTimeByOrderIdSql) {
		this.updateAuditLimitTimeByOrderIdSql = updateAuditLimitTimeByOrderIdSql;
	}

	public String getUpdateLastXcSheetIdBySheetIdSql() {
		return updateLastXcSheetIdBySheetIdSql;
	}

	public void setUpdateLastXcSheetIdBySheetIdSql(String updateLastXcSheetIdBySheetIdSql) {
		this.updateLastXcSheetIdBySheetIdSql = updateLastXcSheetIdBySheetIdSql;
	}

	@Override
	public List complaintCodeAndLogonName(String orderId) {
		String sql="select (select a.unified_complaint_code\n" + 
				"          from CC_CMP_UNIFIED_RETURN a\n" + 
				"         where a.complaint_worksheet_id = b.service_order_id) as UNIFIED_COMPLAINT_CODE," + 
				"       (select c.logonname " + 
				"          from tsm_staff c " + 
				"         where c.staff_id = b.accept_staff_id) as LOGONNAME " + 
				"  from cc_service_order_ask b " + 
				" where b.service_order_id =? ";
		
		return jdbcTemplate.queryForList(sql, orderId);
	}
	
	public int saveReAssignReason(SheetReAssignReason reason){
		String strSql = 
			"insert into cc_reassign_reason (WORK_SHEET_ID, SERVICE_ORDER_ID, TACHE_ID, TACHE_DESC, REASSIGN_REASON, REASSIGN_REASON_DESC, CREATE_STAFF, CREATE_STAFF_NAME, CREATE_DATE)\n" + 
			"values (?, ?, ?, ?, ?, ?, ?, ?, NOW())";
		return this.jdbcTemplate.update(strSql,
				reason.getWorkSheetId(),
				reason.getServiceOrderId(),
				reason.getTacheId(),
				reason.getTacheDesc(),
				reason.getReAssignReason(),
				reason.getReAssignReasonDesc(),
				reason.getCreateStaff(),
				reason.getCreateStaffName()
		);
	}
	
	public int updateHisSheetDealContent(String sheetId, String dealContent) {
		String strsql = "UPDATE CC_WORK_SHEET_HIS T SET T.DEAL_CONTENT=? WHERE T.WORK_SHEET_ID=?";
		return jdbcTemplate.update(strsql, dealContent, sheetId);
	}
	
	public SheetPubInfo getSheetObjBySourceId(int region,String sourceId,boolean hisFlag) {
		String strsql = "";
		if(hisFlag){
			strsql = "SELECT * FROM CC_WORK_SHEET_HIS T WHERE T.REGION_ID=? AND T.SOURCE_SHEET_ID = ?";
		}
		else{
			strsql = "SELECT * FROM CC_WORK_SHEET T WHERE T.REGION_ID=? AND T.SOURCE_SHEET_ID = ?";
		}

		List tmpList = jdbcTemplate.query(strsql, new Object[]{region,sourceId}, new SheetPubInfoRmp());
		if(tmpList.isEmpty()){
			return null;
		}

		SheetPubInfo sheetPubInfo = (SheetPubInfo)tmpList.get(0);
		tmpList.clear();
		return sheetPubInfo;		
	}

	public int updateLastXcSheetIdBySheetId(String lastXcSheetId, String workSheetId) {
		return jdbcTemplate.update(updateLastXcSheetIdBySheetIdSql, lastXcSheetId, workSheetId);
	}

	public List selectCalloutRecByOrderId(String orderId) {
		return jdbcTemplate.queryForList(selectCalloutRecByOrderIdSql, orderId);
	}

	public int insertCustomerJudge(String serviceOrderId, String unifiedComplaintCode, int tacheType) {
		return jdbcTemplate.update(insertCustomerJudgeSql, serviceOrderId, unifiedComplaintCode, tacheType);
	}

	public int deleteCustomerJudgeByOrderId(String serviceOrderId) {
		return jdbcTemplate.update(deleteCustomerJudgeByOrderIdSql, serviceOrderId);
	}

	public int updateCustomerJudgeStatusException(int judgeStatus, String serviceOrderId) {
		return jdbcTemplate.update(updateCustomerJudgeStatusExceptionSql, judgeStatus, serviceOrderId);
	}

	public int updateCustomerJudgeStatusFromIVR(String ivrJudgeDate, String ivrDegree, String serviceOrderId) {
		return jdbcTemplate.update(updateCustomerJudgeStatusFromIVRSql, StringUtils.defaultIfEmpty(ivrJudgeDate, null), ivrDegree, serviceOrderId);
	}

	public int updateCustomerJudgeStatusToZDHF(String serviceOrderId) {
		return jdbcTemplate.update(updateCustomerJudgeStatusToZDHFSql, serviceOrderId);
	}

	public int updateCustomerJudgeStatusFromZDHF(String callDegree, String serviceOrderId) {
		return jdbcTemplate.update(updateCustomerJudgeStatusFromZDHFSql, callDegree, serviceOrderId);
	}

	public List selectCustomerJudgeList() {
		String sql = "SELECT SERVICE_ORDER_ID FROM CC_CUSTOMER_JUDGE WHERE JUDGE_STATUS=0 AND TIMESTAMPDIFF(SECOND,CREATE_DATE,NOW())>10*60 ORDER BY CREATE_DATE LIMIT 10";
		return jdbcTemplate.queryForList(sql);
	}

	public List selectCustomerJudgeOvertimeList() {
		String sql = "SELECT SERVICE_ORDER_ID FROM CC_CUSTOMER_JUDGE WHERE JUDGE_STATUS=2 AND ((UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(CALL_BEGIN_DATE))/60/60) >= 45";
		return jdbcTemplate.queryForList(sql);
	}

	public Map selectCustomerJudgeByOrderId(String orderId) {
		List cjs = jdbcTemplate.queryForList(selectCustomerJudgeByOrderIdSql, orderId);
		if (cjs.isEmpty()) {
			return Collections.emptyMap();
		} else {
			return (Map) cjs.get(0);
		}
	}

	public int insertCustomerJudgeHisByOrderId(String serviceOrderId) {
		if (jdbcTemplate.update(insertCustomerJudgeHisByOrderIdSql, serviceOrderId) > 0) {
			return deleteCustomerJudgeByOrderId(serviceOrderId);
		}
		return 0;
	}

	public int insertXcFlow(XcFlow xcFlow) {
		String sql = "INSERT INTO cc_xc_flow(service_order_id,xc_type,main_sheet_id,creat_date,send_staff,cur_xc_sheet_id,cur_receive_org,is_finish,send_count,flow_record,modify_date)"
				+ "VALUES(?,?,?,NOW(),?,?,?,1,1,?,NOW())";
		return jdbcTemplate.update(sql, xcFlow.getServiceOrderId(), xcFlow.getXcType(), xcFlow.getMainSheetId(),
				xcFlow.getSendStaff(), xcFlow.getCurXcSheetId(), xcFlow.getCurReceiveOrg(), xcFlow.getCurXcSheetId());
	}

	public int sendXcFlow(String curXcSheetId, String curReceiveOrg, String oldXcSheetId) {
		String sql = "UPDATE cc_xc_flow SET cur_xc_sheet_id=?,cur_receive_org=?,send_count=send_count+1,flow_record=CONCAT(flow_record,'-',?),modify_date=NOW()WHERE cur_xc_sheet_id=?";
		return jdbcTemplate.update(sql, curXcSheetId, curReceiveOrg, curXcSheetId, oldXcSheetId);
	}

	public int finishXcFlow(String curXcSheetId) {
		String sql = "UPDATE cc_xc_flow SET is_finish=0,modify_date=NOW()WHERE cur_xc_sheet_id=?";
		return jdbcTemplate.update(sql, curXcSheetId);
	}

	public XcFlow getXcFlowByCurXcId(String curXcSheetId) {
		String sql = "SELECT SERVICE_ORDER_ID,XC_TYPE,MAIN_SHEET_ID,CREAT_DATE,SEND_STAFF,CUR_XC_SHEET_ID,CUR_RECEIVE_ORG,IS_FINISH,SEND_COUNT,FLOW_RECORD,MODIFY_DATE FROM cc_xc_flow WHERE cur_xc_sheet_id=?";
		List list = jdbcTemplate.query(sql, new Object[] { curXcSheetId }, new XcFlowRmp());
		if (list.isEmpty()) {
			return null;
		}
		XcFlow xcFlow = (XcFlow) list.get(0);
		list.clear();
		return xcFlow;
	}

	public XcFlow[] getXcFlowByMainId(String mainSheetId) {
		String sql = "SELECT SERVICE_ORDER_ID,XC_TYPE,MAIN_SHEET_ID,CREAT_DATE,SEND_STAFF,CUR_XC_SHEET_ID,CUR_RECEIVE_ORG,IS_FINISH,SEND_COUNT,FLOW_RECORD,MODIFY_DATE FROM cc_xc_flow WHERE main_sheet_id=? ORDER BY creat_date";
		List list = jdbcTemplate.query(sql, new Object[] { mainSheetId }, new XcFlowRmp());
		XcFlow[] xcFlows = new XcFlow[list.size()];
		for (int i = 0; i < list.size(); i++) {
			xcFlows[i] = (XcFlow) list.get(i);
		}
		return xcFlows;
	}

	public int insertXcFlowHis(String serviceOrderId) {
		String sql = "INSERT INTO cc_xc_flow_his(service_order_id,xc_type,main_sheet_id,creat_date,send_staff,cur_xc_sheet_id,cur_receive_org,is_finish,send_count,flow_record,modify_date)"
				+ "SELECT service_order_id,xc_type,main_sheet_id,creat_date,send_staff,cur_xc_sheet_id,cur_receive_org,is_finish,send_count,flow_record,modify_date FROM cc_xc_flow WHERE service_order_id=?";
		int res = jdbcTemplate.update(sql, serviceOrderId);
		if (res > 0) {
			sql = "DELETE FROM cc_xc_flow WHERE service_order_id=?";
			jdbcTemplate.update(sql, serviceOrderId);
		}
		return res;
	}

	// 扫描二级枢纽来单且受理渠道为部并案工单（38006个人库），比对是否有近30天已归档的同一部局编码部预处理工单
	public List<ComplaintConnection> selectCmpBBAByStaffId(String staffId) {
		String sql = "SELECT d.miit_code,g.service_order_id old_order_id,e.complaint_worksheet_id old_complaint_id,"
				+ "DATE_FORMAT(g.accept_date,'%Y-%m-%d %H:%i:%s')old_accept_date,DATE_FORMAT(g.finish_date,'%Y-%m-%d %H:%i:%s')old_finish_date,"
				+ "b.channel_detail_id,b.service_order_id new_order_id,d.complaint_worksheet_id new_complaint_id,"
				+ "DATE_FORMAT(b.accept_date,'%Y-%m-%d %H:%i:%s')new_accept_date,a.work_sheet_id,a.region_id,a.month_flag,b.region_name,b.area_name,"
				+ "b.prod_num,c.cust_name FROM cc_work_sheet a,cc_service_order_ask b,cc_order_cust_info c,cc_complaint_info d,cc_complaint_info_his e,"
				+ "cc_cmp_relation f,cc_service_order_ask_his g WHERE a.service_order_id=b.service_order_id AND a.service_order_id=d.service_order_id "
				+ "AND b.cust_guid=c.cust_guid AND d.miit_code=e.miit_code AND e.service_order_id=f.service_order_id AND "
				+ "f.service_order_id=g.service_order_id AND a.receive_staff=?AND a.deal_staff=?AND a.sheet_type=720130011 AND a.sheet_statu=720130032 "
				+ "AND a.lock_flag=1 AND a.respond_date IS NULL AND b.service_type=720130000 AND b.order_statu=720130004 AND "
				+ "b.channel_detail_id=707907098 AND d.complaint_worksheet_id<>'0'AND CHAR_LENGTH(d.miit_code)>4 AND f.statu=10 AND "
				+ "f.ask_source_srl='C003004'AND g.order_statu=720130010 AND g.finish_date>DATE_SUB(NOW(),INTERVAL 30 DAY)";
		return jdbcTemplate.query(sql, new Object[] { staffId, staffId }, new ComplaintConnectionRmp());
	}

	public int insertComplaintConnection(ComplaintConnection cc) {
		String sql = "INSERT INTO cc_complaint_connection(miit_code,old_order_id,old_complaint_id,old_accept_date,old_finish_date,channel_detail_id,"
				+ "new_order_id,new_complaint_id,new_accept_date,update_date)VALUES(?,?,?,STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),"
				+ "STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?,?,?,STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),NOW())";
		return jdbcTemplate.update(sql, cc.getMiitCode(), cc.getOldOrderId(), cc.getOldComplaintId(),
				cc.getOldAcceptDate(), cc.getOldFinishDate(), cc.getChannelDetailId(), cc.getNewOrderId(),
				cc.getNewComplaintId(), cc.getNewAcceptDate());
	}

	public int finishComplaintConnection(String newOrderId) {
		String sql = "UPDATE cc_complaint_connection SET new_finish_date=NOW(),update_date=NOW() WHERE new_finish_date IS NULL AND new_order_id=?";
		return jdbcTemplate.update(sql, newOrderId);
	}

	public int insertXcPenalty(String xcSheetId, String pdSheetId, String serviceOrderId, String penaltyMoney) {
		String sql = "INSERT INTO cc_xc_penalty(xc_sheet_id,pd_sheet_id,service_order_id,creat_date,penalty_money)VALUES(?,?,?,NOW(),?)";
		return jdbcTemplate.update(sql, xcSheetId, pdSheetId, serviceOrderId, penaltyMoney);
	}

	public String getSelectCalloutRecByOrderIdSql() {
		return selectCalloutRecByOrderIdSql;
	}

	public void setSelectCalloutRecByOrderIdSql(String selectCalloutRecByOrderIdSql) {
		this.selectCalloutRecByOrderIdSql = selectCalloutRecByOrderIdSql;
	}

	public String getInsertCustomerJudgeSql() {
		return insertCustomerJudgeSql;
	}

	public void setInsertCustomerJudgeSql(String insertCustomerJudgeSql) {
		this.insertCustomerJudgeSql = insertCustomerJudgeSql;
	}

	public String getDeleteCustomerJudgeByOrderIdSql() {
		return deleteCustomerJudgeByOrderIdSql;
	}

	public void setDeleteCustomerJudgeByOrderIdSql(String deleteCustomerJudgeByOrderIdSql) {
		this.deleteCustomerJudgeByOrderIdSql = deleteCustomerJudgeByOrderIdSql;
	}

	public String getUpdateCustomerJudgeStatusExceptionSql() {
		return updateCustomerJudgeStatusExceptionSql;
	}

	public void setUpdateCustomerJudgeStatusExceptionSql(String updateCustomerJudgeStatusExceptionSql) {
		this.updateCustomerJudgeStatusExceptionSql = updateCustomerJudgeStatusExceptionSql;
	}

	public String getUpdateCustomerJudgeStatusFromIVRSql() {
		return updateCustomerJudgeStatusFromIVRSql;
	}

	public void setUpdateCustomerJudgeStatusFromIVRSql(String updateCustomerJudgeStatusFromIVRSql) {
		this.updateCustomerJudgeStatusFromIVRSql = updateCustomerJudgeStatusFromIVRSql;
	}

	public String getUpdateCustomerJudgeStatusToZDHFSql() {
		return updateCustomerJudgeStatusToZDHFSql;
	}

	public void setUpdateCustomerJudgeStatusToZDHFSql(String updateCustomerJudgeStatusToZDHFSql) {
		this.updateCustomerJudgeStatusToZDHFSql = updateCustomerJudgeStatusToZDHFSql;
	}

	public String getUpdateCustomerJudgeStatusFromZDHFSql() {
		return updateCustomerJudgeStatusFromZDHFSql;
	}

	public void setUpdateCustomerJudgeStatusFromZDHFSql(String updateCustomerJudgeStatusFromZDHFSql) {
		this.updateCustomerJudgeStatusFromZDHFSql = updateCustomerJudgeStatusFromZDHFSql;
	}

	public String getSelectCustomerJudgeByOrderIdSql() {
		return selectCustomerJudgeByOrderIdSql;
	}

	public void setSelectCustomerJudgeByOrderIdSql(String selectCustomerJudgeByOrderIdSql) {
		this.selectCustomerJudgeByOrderIdSql = selectCustomerJudgeByOrderIdSql;
	}

	public String getInsertCustomerJudgeHisByOrderIdSql() {
		return insertCustomerJudgeHisByOrderIdSql;
	}

	public void setInsertCustomerJudgeHisByOrderIdSql(String insertCustomerJudgeHisByOrderIdSql) {
		this.insertCustomerJudgeHisByOrderIdSql = insertCustomerJudgeHisByOrderIdSql;
	}
}