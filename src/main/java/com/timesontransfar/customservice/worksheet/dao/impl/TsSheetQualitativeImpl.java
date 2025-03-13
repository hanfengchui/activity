package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.google.gson.Gson;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.OrderOperationLog;
import com.timesontransfar.customservice.worksheet.pojo.OrderOperationLogRmp;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitativeGrid;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitativeRmp;

import edu.emory.mathcs.backport.java.util.Collections;

@SuppressWarnings("rawtypes")
public class TsSheetQualitativeImpl implements ItsSheetQualitative{
	protected Logger log = LoggerFactory.getLogger(TsSheetQualitativeImpl.class);
	
	@Autowired
	private JdbcTemplate jt;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	@Autowired
	private PubFunc pubFun;

	private String saveItsSheetQualitativeSql;
	private String saveItsSheetQualitativeSqlHis;	
	private String deleteItsSheetQualitativeSql;
	private String insertSheetQualitativeGridSql;
	private String deleteSheetQualitativeGridSql;
	private String updateOutletsSql;

	/**
	 * 删除投诉定性表记录
	 * @param serviceOrderId 定单号
	 * @param workSheetId 工单号
	 * @param month 月分区
	 * @return
	 */
	public int deleteTsSheetQualitative(String serviceOrderId, String workSheetId, int region){
		return jt.update(this.deleteItsSheetQualitativeSql, serviceOrderId, region);
	}
	/**
	 * 投诉定性表添加记录
	 * @param bean
	 * @return
	 */
    public int saveTsSheetQualitative(TsSheetQualitative bean) {
    	this.saveOutlets(bean);//保存金大掌柜扣罚项目
		this.saveChannel(bean);//保存渠道相关信息
    	
    	String strsql = this.saveItsSheetQualitativeSql;
    	return this.jt.update(strsql, 
				StringUtils.defaultIfEmpty(bean.getOrderId(),null),
				StringUtils.defaultIfEmpty(bean.getSheetId(),null),
				bean.getRegion(),
				StringUtils.defaultIfEmpty(bean.getRegName(), null),
				bean.getSortId(),
				StringUtils.defaultIfEmpty(bean.getSortName(), null),
				bean.getTsReasonId(),
				StringUtils.defaultIfEmpty(bean.getTsReasonName(), null),
				bean.getTsifBeing(),
				bean.getAppendCases(),
				bean.getCasesId(),
				StringUtils.defaultIfEmpty(bean.getCasesName(), null),			
				bean.getMonthFlag(),
				bean.getTsKeyWord(),
				StringUtils.defaultIfEmpty(bean.getTsKeyWordDesc(), null),
				StringUtils.defaultIfEmpty(bean.getDutyOrg(), null),
				StringUtils.defaultIfEmpty(bean.getDutyOrgName(), null),
				bean.getSubKeyWord(),
				StringUtils.defaultIfEmpty(bean.getSubKeyWordDesc(), null),
				bean.getThreeCatalog(),
				StringUtils.defaultIfEmpty(bean.getThreeCatalogDesc(), null),
				bean.getThourCatalog(),
				StringUtils.defaultIfEmpty(bean.getThourCatalogDesc(), null),
				bean.getFiveCatalog(),
				StringUtils.defaultIfEmpty(bean.getFiveCatalogDesc(), null),
				bean.getSixCatalog(),
				StringUtils.defaultIfEmpty(bean.getSixCatalogDesc(), null),
				bean.getControlAreaFir(),
				StringUtils.defaultIfEmpty(bean.getControlAreaFirDesc(), null),
				bean.getControlAreaSec(),
				StringUtils.defaultIfEmpty(bean.getControlAreaSecDesc(), null),
				bean.getSatisfyId(),
				StringUtils.defaultIfEmpty(bean.getSatisfyDesc(), null),
				StringUtils.defaultIfEmpty(bean.getDutyOrgThird(), null),
				StringUtils.defaultIfEmpty(bean.getDutyOrgThirdName(), null),
				StringUtils.defaultIfEmpty(bean.getForceFlag(), null),
				StringUtils.defaultIfEmpty(bean.getForceFlagDesc(), null),
				StringUtils.defaultIfEmpty(bean.getUnsatisfyReason(), null),
				StringUtils.defaultIfEmpty(bean.getSysJudge(), null),
				StringUtils.defaultIfEmpty(bean.getLastDealContent(), null),
				StringUtils.defaultIfEmpty(bean.getPlusOne(), null),
				StringUtils.defaultIfEmpty(bean.getPlusOneDesc(), null),
				StringUtils.defaultIfEmpty(bean.getPlusTwo(), null),
				StringUtils.defaultIfEmpty(bean.getPlusTwoDesc(), null),
				StringUtils.defaultIfEmpty(bean.getOutletsGuid(), null),
				StringUtils.defaultIfEmpty(bean.getOutletsName(), null),
				StringUtils.defaultIfEmpty(bean.getOutletsAddress(), null),
				StringUtils.defaultIfEmpty(bean.getOutletsArCode(), null),
				StringUtils.defaultIfEmpty(bean.getChannelTpName(), null),
				StringUtils.defaultIfEmpty(bean.getOutletsStaff(), null),
				StringUtils.defaultIfEmpty(bean.getOutletsStaffId(), null),
				StringUtils.defaultIfEmpty(bean.getCustOrderNbr(), null),
				StringUtils.defaultIfEmpty(bean.getCreateChannelName(), null),
				StringUtils.defaultIfEmpty(bean.getCreateStaffCode(), null),
				StringUtils.defaultIfEmpty(bean.getCreateStaffName(), null),
				StringUtils.defaultIfEmpty(bean.getOrderOperType(), null),
				StringUtils.defaultIfEmpty(bean.getOrderCreateDate(), null),
				StringUtils.defaultIfEmpty(bean.getOrderStatus(), null),
				bean.getOverTimeReasonId(),
				bean.getOverTimeReasonDesc(),
				bean.getReasonable()
				
		);
	}
    
    private int getOutletRegionId(int regionId) {
    	int realRegionId = -1;
		switch (regionId) {
			case 3:
				realRegionId = 1;
				break;
			case 15:
				realRegionId = 2;
				break;
			case 4:
				realRegionId = 3;
				break;
			case 20:
				realRegionId = 4;
				break;
			case 26:
				realRegionId = 5;
				break;
			case 33:
				realRegionId = 6;
				break;
			case 39:
				realRegionId = 7;
				break;
			case 48:
				realRegionId = 8;
				break;
			case 60:
				realRegionId = 9;
				break;
			case 63:
				realRegionId = 10;
				break;
			case 69:
				realRegionId = 11;
				break;
			case 79:
				realRegionId = 12;
				break;
			case 84:
				realRegionId = 13;
				break;
			default:
				break;
		}
    	return realRegionId;
    }

	private void saveChannel(TsSheetQualitative bean) {
		if (StringUtils.isBlank(bean.getChannelName())) {
			return;
		}
		int num = 0;
		String sql = "";
		try {
			if (getChannel(bean.getOrderId()) == 0){
				log.info("insert cc_order_channel: {}", bean.getOrderId());
				sql = "insert into cc_order_channel (SERVICE_ORDER_ID,CHANNEL_CODE,CHANNEL_CLASSIFICATION,CHANNEL_DETAILS,CHANNEL_NAME,CHANNEL_LOCATION,AGENT_NAME,CREATE_DATE,UPDATE_DATE,ORDER_STATE," +
						"PRINCIPAL_DISTRICT,PRINCIPAL_LOCATION,PRINCIPAL_COUNTY,MARKET_NAME,MARKET_ID,MARKET_GRADE,PRINCIPAL_DEPT,ADMIN_PRINCIPAL,CHANNEL_COUNTY,CHANNEL_PRINCIPAL) "
						+ "values (?,?,?,?,?,?,?,now(),now(),0,?,?,?,?,?,?,?,?,?,?)";
				num = jt.update(sql,bean.getOrderId(),bean.getChannelCode(),bean.getChannelClassification(),bean.getChannelDetails(),
						bean.getChannelName(),bean.getChannelLocation(),bean.getAgentName(),bean.getPrincipalDistrict(),bean.getPrincipalLocation(),bean.getPrincipalCounty(),
						bean.getMarketName(),bean.getMarketId(),bean.getMarketGrade(),bean.getPrincipalDept(),bean.getAdminPrincipal(),bean.getChannelCounty(),bean.getChannelPrincipal());
			}else {
				log.info("update cc_order_channel: {}", bean.getOrderId());
				sql = "update cc_order_channel set CHANNEL_CODE = ?, CHANNEL_CLASSIFICATION = ?, CHANNEL_DETAILS = ?, CHANNEL_NAME = ?, CHANNEL_LOCATION = ?, AGENT_NAME = ?," +
						"PRINCIPAL_DISTRICT = ?,PRINCIPAL_LOCATION = ?,PRINCIPAL_COUNTY = ?,MARKET_NAME = ?,MARKET_ID = ?,MARKET_GRADE = ?,PRINCIPAL_DEPT = ?,ADMIN_PRINCIPAL = ?," +
						"CHANNEL_COUNTY = ?,CHANNEL_PRINCIPAL = ?,UPDATE_DATE = now() where SERVICE_ORDER_ID = ?";
				num = jt.update(sql,bean.getChannelCode(),bean.getChannelClassification(),bean.getChannelDetails(),
						bean.getChannelName(),bean.getChannelLocation(),bean.getAgentName(),bean.getPrincipalDistrict(),bean.getPrincipalLocation(),bean.getPrincipalCounty(),
						bean.getMarketName(),bean.getMarketId(),bean.getMarketGrade(),bean.getPrincipalDept(),bean.getAdminPrincipal(),bean.getChannelCounty(),bean.getChannelPrincipal(),bean.getOrderId());
			}
		}catch (Exception e){
			log.error("saveChannel error: {}", e.getMessage(), e);
		}
		log.info("saveChannel result: {}", num);
	}
    
    private void saveOutlets(TsSheetQualitative bean) {
    	if (StringUtils.isBlank(bean.getDeductionsId())) {
    		return;
    	}
    	
    	int num = 0;
    	try {
	    	int outletRegionId = this.getOutletRegionId(bean.getRegion());
	    	int outletInType = (StringUtils.isBlank(bean.getOutletsGuid()) ? 0 : 1);
	    	int staffInType = (StringUtils.isBlank(bean.getOutletsStaffId()) ? 0 : 1);
	    	
			String sql = "";
			
			if (getOutlets(bean.getOrderId()) == 0) {
				sql = "insert into cc_sheet_outlets (SERVICE_ORDER_ID,WORK_SHEET_ID,CREATE_DATE,UPDATE_DATE,OUTLETS_GUID,OUTLETS_NAME,OUTLET_IN_TYPE,OUTLETS_STAFF_ID," +
						"OUTLETS_STAFF_NAME,STAFF_IN_TYPE,DEDUCTIONS_ID,DEDUCTIONS_NAME,REGION_ID,REGION_NAME,ORDER_STATE)values(?,?,now(),now(),?,?,?,?,?,?,?,?,?,?,0)";
				num = jt.update(sql, bean.getOrderId(), bean.getSheetId(), bean.getOutletsGuid(), bean.getOutletsName(), outletInType, 
						bean.getOutletsStaffId(), bean.getOutletsStaff(), staffInType, 
						bean.getDeductionsId(), bean.getDeductionsName(), outletRegionId, bean.getRegName());
			} else {
				sql = "update cc_sheet_outlets set WORK_SHEET_ID = ?,UPDATE_DATE = now(),OUTLETS_GUID = ?,OUTLETS_NAME = ?,OUTLET_IN_TYPE = ?,"
						+ "OUTLETS_STAFF_ID = ?,OUTLETS_STAFF_NAME = ?,STAFF_IN_TYPE = ?," +
						"DEDUCTIONS_ID = ?,DEDUCTIONS_NAME = ?,REGION_ID = ?,REGION_NAME = ? where SERVICE_ORDER_ID = ?";
				num = jt.update(sql, bean.getSheetId(), bean.getOutletsGuid(), bean.getOutletsName(), outletInType, 
						bean.getOutletsStaffId(), bean.getOutletsStaff(), staffInType, 
						bean.getDeductionsId(), bean.getDeductionsName(), outletRegionId, bean.getRegName(), bean.getOrderId());
			}
    	} catch(Exception e) {
    		log.error("saveOutlets error: {}", e.getMessage(), e);
    	}
		log.info("saveOutlets result: {}", num);
	}
    
	private int getOutlets(String orderId) {
		String sql = "select count(1) from cc_sheet_outlets where SERVICE_ORDER_ID = ?";
		return jt.queryForObject(sql, new Object[]{orderId}, Integer.class);
	}

	public int updateOutlets(String orderId) {
		String sql = "update cc_sheet_outlets set ARCHIVE_DATE = now(),UPDATE_DATE = now(),ORDER_STATE = 1 where SERVICE_ORDER_ID = ?";
		return jt.update(sql,orderId);
	}
	
	public int updateOrderChannel(String orderId) {
		String sql = "update cc_order_channel set UPDATE_DATE = now(),ORDER_STATE = 1 where SERVICE_ORDER_ID = ?";
		return jt.update(sql,orderId);
	}

	private int getChannel(String orderId) {
		String sql = "select count(1) from cc_order_channel where SERVICE_ORDER_ID = ?";
		return jt.queryForObject(sql, new Object[]{orderId}, Integer.class);
	}

    /**
	 * 投诉定性表添加记录
	 * @param servid
	 * @param sheetid
	 * @param month
	 * @return
	 */
	public int saveTsSheetQualitativeHis(String servid,String sheetid,int region){
    	return jt.update(this.saveItsSheetQualitativeSqlHis, servid, region);
	}
	/**
	 * 得到工单定性对象
	 * @param sheetId 工单号
	 * @param regionId 地域
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TsSheetQualitative[] getTsSheetQualitative(String sheetId, int regionId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = "SELECT a.service_order_id,a.work_sheet_id,a.region_id,a.region_name,a.pigeonhole_sort_id,a.pigeonhole_sort_name,a.ts_reason_id,"
					+ "a.ts_reason_name,a.ts_if_being,a.append_cases,a.cases_id,a.cases_name,a.creat_data,a.month_flag,a.ts_key_word,a.ts_key_word_desc,"
					+ "a.duty_org,a.duty_org_name,a.sub_key_word,a.sub_key_word_desc,a.three_grade_catalog,a.three_grade_catalog_desc,"
					+ "a.four_grade_catalog,a.four_grade_catalog_desc,a.five_grade_catalog,a.five_grade_catalog_desc,a.control_area_fir,"
					+ "a.control_area_fir_desc,a.control_area_sec,a.control_area_sec_desc,a.satisfaction_id,a.satisfaction_desc,a.duty_org_third,"
					+ "a.duty_org_third_desc,a.force_flag,a.force_flag_desc,a.six_grade_catalog,a.six_grade_catalog_disc,a.unsatisfy_reason,a.sys_judge,"
					+ "a.last_deal_content,a.plus_one,a.plus_one_desc,a.plus_two,a.plus_two_desc,a.outlets_guid,a.outlets_name,a.outlets_address,"
					+ "a.outlets_ar_code,a.channel_tp_name,a.outlets_staff,a.outlets_staff_id,a.cust_order_nbr,a.create_channel_name,"
					+ "a.create_staff_code,a.create_staff_name,a.order_oper_type,a.order_create_date,a.order_status,a.overtime_reason_id,"
					+ "a.overtime_reason_desc,a.is_reasonable,b.channel_code,b.channel_classification,b.channel_details,b.channel_name,"
					+ "b.channel_location,b.agent_name,b.principal_district,b.principal_location,b.principal_county,b.principal_dept,b.admin_principal,"
					+ "b.channel_county,b.channel_principal,b.market_name,b.market_grade,b.market_id,c.deductions_id,c.deductions_name FROM "
					+ "cc_sheet_qualitative a LEFT JOIN cc_order_channel b ON a.service_order_id=b.service_order_id LEFT JOIN cc_sheet_outlets c "
					+ "ON  a.service_order_id=c.service_order_id WHERE a.work_sheet_id=? AND a.region_id=? ORDER BY a.creat_data";
		} else {
			strSql = "SELECT a.service_order_id,a.work_sheet_id,a.region_id,a.region_name,a.pigeonhole_sort_id,a.pigeonhole_sort_name,a.ts_reason_id,"
					+ "a.ts_reason_name,a.ts_if_being,a.append_cases,a.cases_id,a.cases_name,a.creat_data,a.month_flag,a.ts_key_word,a.ts_key_word_desc,"
					+ "a.duty_org,a.duty_org_name,a.sub_key_word,a.sub_key_word_desc,a.three_grade_catalog,a.three_grade_catalog_desc,"
					+ "a.four_grade_catalog,a.four_grade_catalog_desc,a.five_grade_catalog,a.five_grade_catalog_desc,a.control_area_fir,"
					+ "a.control_area_fir_desc,a.control_area_sec,a.control_area_sec_desc,a.satisfaction_id,a.satisfaction_desc,a.duty_org_third,"
					+ "a.duty_org_third_desc,a.force_flag,a.force_flag_desc,a.six_grade_catalog,a.six_grade_catalog_disc,a.unsatisfy_reason,a.sys_judge,"
					+ "a.last_deal_content,a.plus_one,a.plus_one_desc,a.plus_two,a.plus_two_desc,a.outlets_guid,a.outlets_name,a.outlets_address,"
					+ "a.outlets_ar_code,a.channel_tp_name,a.outlets_staff,a.outlets_staff_id,a.cust_order_nbr,a.create_channel_name,"
					+ "a.create_staff_code,a.create_staff_name,a.order_oper_type,a.order_create_date,a.order_status,a.overtime_reason_id,"
					+ "a.overtime_reason_desc,a.is_reasonable,b.channel_code,b.channel_classification,b.channel_details,b.channel_name,"
					+ "b.channel_location,b.agent_name,b.principal_district,b.principal_location,b.principal_county,b.principal_dept,b.admin_principal,"
					+ "b.channel_county,b.channel_principal,b.market_name,b.market_grade,b.market_id,c.deductions_id,c.deductions_name FROM "
					+ "cc_sheet_qualitative_his a LEFT JOIN cc_order_channel b ON a.service_order_id=b.service_order_id LEFT JOIN cc_sheet_outlets c "
					+ "ON a.service_order_id=c.service_order_id WHERE a.work_sheet_id=? AND a.region_id=? ORDER BY a.creat_data";
		}
		List tmp = this.jt.query(strSql, new Object[] { sheetId, regionId }, new TsSheetQualitativeRmp());
		int size = tmp.size();
		if (size == 0) {
			return new TsSheetQualitative[0];
		}
		TsSheetQualitative[] bean = new TsSheetQualitative[size];
		for (int i = 0; i < size; i++) {
			bean[i] = (TsSheetQualitative) tmp.get(i);
		}
		return bean;
	}

	/**
	 * 得到工单定性和回访对象
	 * 
	 * @param orderId 服务单号
	 * @param boo     true 查询当前 false 查询历史
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List getQualitativeAndVisit(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql =
"SELECT * FROM (SELECT A.WORK_SHEET_ID,DATE_FORMAT(RESPOND_DATE,'%Y-%m-%d %H:%i:%s')RESPOND_DATE,DATE_FORMAT(B.CREAT_DATA,'%Y-%m-%d %H:%i:%s')DX_DATE," +
"DEAL_ORG_ID DX_ORG_NAME,DEAL_STAFF_NAME DX_STAFF_NAME,TS_IF_BEING,APPEND_CASES,TS_REASON_NAME,PLUS_ONE_DESC,PLUS_TWO_DESC,DUTY_ORG_NAME," +
"CONTROL_AREA_FIR_DESC,CONTROL_AREA_SEC_DESC,DUTY_ORG_THIRD_DESC,OVERTIME_REASON_ID,OVERTIME_REASON_DESC,IS_REASONABLE,DEAL_CONTENT,DATE_FORMAT(C.CREAT_DATA,'%Y-%m-%d %H:%i:%s')HF_DATE," +
"IF(AUTO_VISIT_FLAG=1,'',IF(AUTO_VISIT_FLAG=2,'',DEAL_ORG_NAME))HF_ORG_NAME,IF(AUTO_VISIT_FLAG=1,'',IF(AUTO_VISIT_FLAG=2,'',DEAL_STAFF_NAME))HF_STAFF_NAME," +
"IFNULL(IF(visit_type='1','人工回访',IF(visit_type='2','自动回访',IF(visit_type='3','即时测评',''))),IF(AUTO_VISIT_FLAG=1,'自动回访',IF(AUTO_VISIT_FLAG=2,'自动回访',IF(ISNULL(C.CREAT_DATA),'','人工回访'))))HF_FLAG,COLLECTIVITY_CIRCS_NAME,TS_DEAL_ATTITUDE_NAME,TS_DEAL_BETIMES_NAME," +
"TS_DEAL_RESULT_NAME,TS_VISIT_RESULT FROM(SELECT*FROM CC_WORK_SHEET WHERE SERVICE_ORDER_ID=?)A " + 
"LEFT JOIN (SELECT c.* FROM cc_sheet_qualitative c,(SELECT max(CREAT_DATA) CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM cc_sheet_qualitative where SERVICE_ORDER_ID=? GROUP BY WORK_SHEET_ID) md " + 
"where c.CREAT_DATA = md.CREAT_DATA and c.WORK_SHEET_ID=md.WORK_SHEET_ID and c.SERVICE_ORDER_ID=md.SERVICE_ORDER_ID)B ON A.WORK_SHEET_ID=B.WORK_SHEET_ID " + 
"LEFT JOIN (SELECT c.* FROM CC_CUSTOMER_VISIT c,(SELECT max(CREAT_DATA) CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_CUSTOMER_VISIT where SERVICE_ORDER_ID=? GROUP BY WORK_SHEET_ID) md " + 
"where c.CREAT_DATA = md.CREAT_DATA and c.WORK_SHEET_ID=md.WORK_SHEET_ID and c.SERVICE_ORDER_ID=md.SERVICE_ORDER_ID)C ON A.WORK_SHEET_ID=C.WORK_SHEET_ID) AS RT " + 
"WHERE DX_DATE IS NOT NULL OR HF_DATE IS NOT NULL ORDER BY RESPOND_DATE";
		} else {
			strSql = 
"SELECT * FROM (SELECT A.WORK_SHEET_ID,DATE_FORMAT(RESPOND_DATE,'%Y-%m-%d %H:%i:%s')RESPOND_DATE,DATE_FORMAT(B.CREAT_DATA,'%Y-%m-%d %H:%i:%s')DX_DATE," +
"DEAL_ORG_ID DX_ORG_NAME,DEAL_STAFF_NAME DX_STAFF_NAME,TS_IF_BEING,APPEND_CASES,TS_REASON_NAME,PLUS_ONE_DESC,PLUS_TWO_DESC,DUTY_ORG_NAME," +
"CONTROL_AREA_FIR_DESC,CONTROL_AREA_SEC_DESC,DUTY_ORG_THIRD_DESC,OVERTIME_REASON_ID,OVERTIME_REASON_DESC,IS_REASONABLE,DEAL_CONTENT,DATE_FORMAT(C.CREAT_DATA,'%Y-%m-%d %H:%i:%s')HF_DATE," +
"IF(AUTO_VISIT_FLAG=1,'',IF(AUTO_VISIT_FLAG=2,'',DEAL_ORG_NAME))HF_ORG_NAME,IF(AUTO_VISIT_FLAG=1,'',IF(AUTO_VISIT_FLAG=2,'',DEAL_STAFF_NAME))HF_STAFF_NAME," +
"IFNULL(IF(visit_type='1','人工回访',IF(visit_type='2','自动回访',IF(visit_type='3','即时测评',''))),IF(AUTO_VISIT_FLAG=1,'自动回访',IF(AUTO_VISIT_FLAG=2,'自动回访',IF(ISNULL(C.CREAT_DATA),'','人工回访'))))HF_FLAG,COLLECTIVITY_CIRCS_NAME,TS_DEAL_ATTITUDE_NAME,TS_DEAL_BETIMES_NAME," +
"TS_DEAL_RESULT_NAME,TS_VISIT_RESULT FROM(SELECT*FROM CC_WORK_SHEET_HIS WHERE SERVICE_ORDER_ID=?)A " + 
"LEFT JOIN (SELECT c.* FROM cc_sheet_qualitative_HIS c,(SELECT max(CREAT_DATA) CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM cc_sheet_qualitative_HIS where SERVICE_ORDER_ID=? GROUP BY WORK_SHEET_ID) md " + 
"where c.CREAT_DATA = md.CREAT_DATA and c.WORK_SHEET_ID=md.WORK_SHEET_ID and c.SERVICE_ORDER_ID=md.SERVICE_ORDER_ID)B ON A.WORK_SHEET_ID=B.WORK_SHEET_ID " + 
"LEFT JOIN (SELECT c.* FROM CC_CUSTOMER_VISIT_HIS c,(SELECT max(CREAT_DATA) CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_CUSTOMER_VISIT_HIS where SERVICE_ORDER_ID=? GROUP BY WORK_SHEET_ID) md " + 
"where c.CREAT_DATA = md.CREAT_DATA and c.WORK_SHEET_ID=md.WORK_SHEET_ID and c.SERVICE_ORDER_ID=md.SERVICE_ORDER_ID)C ON A.WORK_SHEET_ID=C.WORK_SHEET_ID) AS RT " + 
"WHERE DX_DATE IS NOT NULL OR HF_DATE IS NOT NULL ORDER BY RESPOND_DATE";
		}
		List list = this.jt.queryForList(strSql, orderId, orderId, orderId);
		if (list.isEmpty()) {
			return Collections.emptyList();
		} else {
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				String don = "DX_ORG_NAME";

				String dxOrgName = map.get(don) == null ? "" : pubFun.getOrgWater(map.get(don).toString());
				map.put(don, dxOrgName);
				list.set(i, map);
			}
		}
		return list;
	}

	/**
	 * 得到工单定性或回访对象
	 * @param orderId 服务单号
	 * @param boo     true 查询当前 false 查询历史
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public List getQualitativeOrVisit(String orderId, boolean boo, boolean cliqueFlag) {
		String strSql = "";
		if (cliqueFlag) {
			return getQualitativeOrVisitJt(orderId, boo);
		} else {
			if (boo) {
				strSql = 
"SELECT*FROM(SELECT*FROM(SELECT A.WORK_SHEET_ID,DATE_FORMAT(RESPOND_DATE,'%Y-%m-%d %H:%i:%s')RESPOND_DATE,DATE_FORMAT(B.CREAT_DATA,'%Y-%m-%d %H:%i:%s')"
+ "DX_DATE,DEAL_ORG_ID DX_ORG_NAME,DEAL_STAFF_NAME DX_STAFF_NAME,TS_IF_BEING,APPEND_CASES,TS_REASON_NAME,PLUS_ONE_DESC,PLUS_TWO_DESC,DUTY_ORG_NAME,"
+ "CONTROL_AREA_FIR_DESC,CONTROL_AREA_SEC_DESC,DUTY_ORG_THIRD_DESC,OVERTIME_REASON_ID,OVERTIME_REASON_DESC,IS_REASONABLE,DEAL_CONTENT,DATE_FORMAT(C.CREAT_DATA,'%Y-%m-%d %H:%i:%s')HF_DATE,IF(visit_type='1',"
+ "DEAL_ORG_NAME,'')HF_ORG_NAME,IF(visit_type='1',DEAL_STAFF_NAME,'')HF_STAFF_NAME,IF(visit_type='2','自动回访',IF(visit_type='3','即时测评',IF(visit_type="
+ "'4','集团-短信',IF(visit_type='5','集团-微信',IF(visit_type='6','集团-APP',IF(visit_type='7','集团-智能外呼',IF(visit_type='8','集团-人工外呼','人工回访')))))))"
+ "HF_FLAG,COLLECTIVITY_CIRCS_NAME,TS_DEAL_ATTITUDE_NAME,TS_DEAL_BETIMES_NAME,"
+ "TS_DEAL_RESULT_NAME,TS_VISIT_RESULT,IFNULL((SELECT IF(W.CONTACT_STATUS IN(1,2),'是','否')FROM CC_CMP_UNIFIED_CONTACT W WHERE W.CONTACT_TYPE=1 AND "
+ "W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')CONTACTSTATUS,IFNULL((SELECT IF("
+ "W.CONTACT_STATUS IN(1,2),'是','否')FROM CC_CMP_UNIFIED_CONTACT W WHERE W.CONTACT_TYPE=2 AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.WORK_SHEET_ID="
+ "B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')REQUIREUNINVITED,IFNULL((SELECT IF(W.REPEAT_STATUS IN(1,2),NEW_UCC,'否')FROM CC_CMP_UNIFIED_REPEAT"
+ " W WHERE W.REPEAT_TYPE=1 AND W.CUR_SOI=A.SERVICE_ORDER_ID AND W.CUR_WHI=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')ISREPEAT,IFNULL(("
+ "SELECT IF(W.REPEAT_STATUS IN(1,2),NEW_UCC,'否')FROM CC_CMP_UNIFIED_REPEAT W WHERE W.REPEAT_TYPE=2 AND W.CUR_SOI=A.SERVICE_ORDER_ID AND W.CUR_WHI="
+ "B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')ISUPREPEAT,A.SERVICE_TYPE FROM(SELECT*FROM CC_WORK_SHEET WHERE SERVICE_ORDER_ID=?)A LEFT JOIN(SELECT C.*FROM"
+ " CC_SHEET_QUALITATIVE C,(SELECT MAX(CREAT_DATA)CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_SHEET_QUALITATIVE WHERE SERVICE_ORDER_ID=?GROUP BY "
+ "WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID=MD.SERVICE_ORDER_ID)B ON A.WORK_SHEET_ID"
+ "=B.WORK_SHEET_ID LEFT JOIN(SELECT C.*FROM CC_CUSTOMER_VISIT C,(SELECT MAX(CREAT_DATA)CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_CUSTOMER_VISIT"
+ " WHERE SERVICE_ORDER_ID=?GROUP BY WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID="
+ "MD.SERVICE_ORDER_ID)C ON A.WORK_SHEET_ID=C.WORK_SHEET_ID)AS RT WHERE DX_DATE IS NOT NULL OR HF_DATE IS NOT NULL UNION ALL SELECT SERVICE_ORDER_ID,"
+ "DATE_FORMAT(CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,DATE_FORMAT(CALL_JUDGE_DATE,"
+ "'%Y-%m-%d %H:%i:%s'),NULL,NULL,'自动回访',IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF("
+ "CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,"
+ "'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),NULL,"
+ "NULL,NULL,NULL,IF(TACHE_TYPE IN (1,2),'720130000',IF(TACHE_TYPE=3,'720200003','600000074')) FROM CC_CUSTOMER_JUDGE WHERE TACHE_TYPE IN(3,4,5)AND "
+ "JUDGE_STATUS=3 AND CALL_DEGREE IS NOT NULL AND CALL_JUDGE_DATE IS NOT NULL AND SERVICE_ORDER_ID=?)X ORDER BY RESPOND_DATE";
			} else {
				strSql = 
"SELECT*FROM(SELECT*FROM(SELECT A.WORK_SHEET_ID,DATE_FORMAT(RESPOND_DATE,'%Y-%m-%d %H:%i:%s')RESPOND_DATE,DATE_FORMAT(B.CREAT_DATA,'%Y-%m-%d %H:%i:%s')"
+ "DX_DATE,DEAL_ORG_ID DX_ORG_NAME,DEAL_STAFF_NAME DX_STAFF_NAME,TS_IF_BEING,APPEND_CASES,TS_REASON_NAME,PLUS_ONE_DESC,PLUS_TWO_DESC,DUTY_ORG_NAME,"
+ "CONTROL_AREA_FIR_DESC,CONTROL_AREA_SEC_DESC,DUTY_ORG_THIRD_DESC,OVERTIME_REASON_ID,OVERTIME_REASON_DESC,IS_REASONABLE,DEAL_CONTENT,DATE_FORMAT(C.CREAT_DATA,'%Y-%m-%d %H:%i:%s')HF_DATE,IF(visit_type='1',"
+ "DEAL_ORG_NAME,'')HF_ORG_NAME,IF(visit_type='1',DEAL_STAFF_NAME,'')HF_STAFF_NAME,IF(visit_type='2','自动回访',IF(visit_type='3','即时测评',IF(visit_type="
+ "'4','集团-短信',IF(visit_type='5','集团-微信',IF(visit_type='6','集团-APP',IF(visit_type='7','集团-智能外呼',IF(visit_type='8','集团-人工外呼','人工回访')))))))"
+ "HF_FLAG,COLLECTIVITY_CIRCS_NAME,TS_DEAL_ATTITUDE_NAME,TS_DEAL_BETIMES_NAME,"
+ "TS_DEAL_RESULT_NAME,TS_VISIT_RESULT,IFNULL((SELECT IF(W.CONTACT_STATUS IN(1,2),'是','否')FROM CC_CMP_UNIFIED_CONTACT_HIS W WHERE W.CONTACT_TYPE=1 AND "
+ "W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')CONTACTSTATUS,IFNULL((SELECT IF("
+ "W.CONTACT_STATUS IN(1,2),'是','否')FROM CC_CMP_UNIFIED_CONTACT_HIS W WHERE W.CONTACT_TYPE=2 AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND "
+ "W.WORK_SHEET_ID=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')REQUIREUNINVITED,IFNULL((SELECT IF(W.REPEAT_STATUS IN(1,2),NEW_UCC,'否')FROM "
+ "CC_CMP_UNIFIED_REPEAT_HIS W WHERE W.REPEAT_TYPE=1 AND W.CUR_SOI=A.SERVICE_ORDER_ID AND W.CUR_WHI=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),"
+ "'否')ISREPEAT,IFNULL((SELECT IF(W.REPEAT_STATUS IN(1,2),NEW_UCC,'否')FROM CC_CMP_UNIFIED_REPEAT_HIS W WHERE W.REPEAT_TYPE=2 AND W.CUR_SOI="
+ "A.SERVICE_ORDER_ID AND W.CUR_WHI=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')ISUPREPEAT,A.SERVICE_TYPE FROM(SELECT*FROM CC_WORK_SHEET_HIS WHERE "
+ "SERVICE_ORDER_ID=?)A LEFT JOIN(SELECT C.*FROM CC_SHEET_QUALITATIVE_HIS C,(SELECT MAX(CREAT_DATA)CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM "
+ "CC_SHEET_QUALITATIVE_HIS WHERE SERVICE_ORDER_ID=?GROUP BY WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND "
+ "C.SERVICE_ORDER_ID=MD.SERVICE_ORDER_ID)B ON A.WORK_SHEET_ID=B.WORK_SHEET_ID LEFT JOIN(SELECT C.*FROM CC_CUSTOMER_VISIT_HIS C,(SELECT MAX(CREAT_DATA)"
+ "CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_CUSTOMER_VISIT_HIS WHERE SERVICE_ORDER_ID=?GROUP BY WORK_SHEET_ID)MD WHERE C.CREAT_DATA="
+ "MD.CREAT_DATA AND C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID=MD.SERVICE_ORDER_ID)C ON A.WORK_SHEET_ID=C.WORK_SHEET_ID)AS RT WHERE "
+ "DX_DATE IS NOT NULL OR HF_DATE IS NOT NULL UNION ALL SELECT SERVICE_ORDER_ID,DATE_FORMAT(CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),NULL,NULL,NULL,NULL,"
+ "NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,DATE_FORMAT(CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),NULL,NULL,'自动回访',IF(CALL_DEGREE=1,'不满意',IF("
+ "CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1"
+ ",'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF("
+ "CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),NULL,NULL,NULL,NULL,IF(TACHE_TYPE IN (1,2),'720130000',IF(TACHE_TYPE=3,"
+ "'720200003','600000074')) FROM CC_CUSTOMER_JUDGE_HIS WHERE TACHE_TYPE IN(3,4,5)AND JUDGE_STATUS=3 AND CALL_DEGREE IS NOT NULL AND CALL_JUDGE_DATE IS NOT NULL AND SERVICE_ORDER_ID=?)X ORDER BY RESPOND_DATE";
			}
			List list = this.jt.queryForList(strSql, orderId, orderId, orderId, orderId);
			if (list.isEmpty()) {
				return Collections.emptyList();
			} else {
				for (int i = 0; i < list.size(); i++) {
					Map map = (Map) list.get(i);
					String don = "DX_ORG_NAME";
					String dxOrgName = map.get(don) == null ? "" : pubFun.getOrgWater(map.get(don).toString());
					map.put(don, dxOrgName);
					list.set(i, map);
				}
			}
			return list;
		}
	}

	@SuppressWarnings({ "unchecked" })
	private List getQualitativeOrVisitJt(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT*FROM(SELECT*FROM(SELECT A.WORK_SHEET_ID,DATE_FORMAT(RESPOND_DATE,'%Y-%m-%d %H:%i:%s')RESPOND_DATE,DATE_FORMAT(B.CREAT_DATA,'%Y-%m-%d %H:%i:%s')DX_DATE,"
+ "DEAL_ORG_ID DX_ORG_NAME,DEAL_STAFF_NAME DX_STAFF_NAME,TS_IF_BEING,APPEND_CASES,TS_REASON_NAME,PLUS_ONE_DESC,PLUS_TWO_DESC,DUTY_ORG_NAME,"
+ "CONTROL_AREA_FIR_DESC,CONTROL_AREA_SEC_DESC,DUTY_ORG_THIRD_DESC,OVERTIME_REASON_ID,OVERTIME_REASON_DESC,IS_REASONABLE,DEAL_CONTENT,DATE_FORMAT(C.CREAT_DATA,'%Y-%m-%d %H:%i:%s')HF_DATE,IF(visit_type='1',"
+ "DEAL_ORG_NAME,'')HF_ORG_NAME,IF(visit_type='1',DEAL_STAFF_NAME,'')HF_STAFF_NAME,IF(visit_type='2','自动回访',IF(visit_type='3','即时测评',IF(visit_type="
+ "'4','集团-短信',IF(visit_type='5','集团-微信',IF(visit_type='6','集团-APP',IF(visit_type='7','集团-智能外呼',IF(visit_type='8','集团-人工外呼','人工回访')))))))"
+ "HF_FLAG,COLLECTIVITY_CIRCS_NAME,TS_DEAL_ATTITUDE_NAME,TS_DEAL_BETIMES_NAME,TS_DEAL_RESULT_NAME,TS_VISIT_RESULT,IFNULL((SELECT IF(W.CONTACT_STATUS IN"
+ "(1,2),'是','否')FROM CC_CMP_UNIFIED_CONTACT W WHERE W.CONTACT_TYPE=1 AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=B.WORK_SHEET_ID "
+ "ORDER BY W.OPER_DATE DESC LIMIT 1),'否')CONTACTSTATUS,IFNULL((SELECT IF(W.CONTACT_STATUS IN(1,2),'是','否')FROM CC_CMP_UNIFIED_CONTACT W WHERE "
+ "W.CONTACT_TYPE=2 AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')"
+ "REQUIREUNINVITED,IFNULL((SELECT IF(W.REPEAT_STATUS IN(1,2),NEW_UCC,'否')FROM CC_CMP_UNIFIED_REPEAT W WHERE W.REPEAT_TYPE=1 AND W.CUR_SOI="
+ "A.SERVICE_ORDER_ID AND W.CUR_WHI=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')ISREPEAT,IFNULL((SELECT IF(W.REPEAT_STATUS IN(1,2),NEW_UCC,"
+ "'否')FROM CC_CMP_UNIFIED_REPEAT W WHERE W.REPEAT_TYPE=2 AND W.CUR_SOI=A.SERVICE_ORDER_ID AND W.CUR_WHI=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC "
+ "LIMIT 1),'否')ISUPREPEAT,A.SERVICE_TYPE "
+ "FROM(SELECT*FROM CC_WORK_SHEET WHERE SERVICE_ORDER_ID=?)A LEFT JOIN(SELECT C.*FROM CC_SHEET_QUALITATIVE C,(SELECT MAX(CREAT_DATA)CREAT_DATA,"
+ "SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_SHEET_QUALITATIVE WHERE SERVICE_ORDER_ID=?GROUP BY WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND "
+ "C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID=MD.SERVICE_ORDER_ID)B ON A.WORK_SHEET_ID=B.WORK_SHEET_ID LEFT JOIN(SELECT C.*FROM "
+ "CC_CUSTOMER_VISIT C WHERE SERVICE_ORDER_ID=?AND SERVICE_ORDER_ID NOT IN(SELECT SERVICE_ORDER_ID FROM CC_CUSTOMER_JUDGE WHERE TACHE_TYPE IN(3,4,5)AND "
+ "JUDGE_STATUS=3 AND CALL_DEGREE IS NOT NULL AND CALL_JUDGE_DATE IS NOT NULL AND SERVICE_ORDER_ID=?)ORDER BY CREAT_DATA DESC LIMIT 1)C ON "
+ "A.WORK_SHEET_ID=C.WORK_SHEET_ID)AS RT WHERE DX_DATE IS NOT NULL OR HF_DATE IS NOT NULL UNION ALL SELECT SERVICE_ORDER_ID,DATE_FORMAT(CALL_JUDGE_DATE,"
+ "'%Y-%m-%d %H:%i:%s'),NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,DATE_FORMAT(CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),NULL,NULL,"
+ "'自动回访',IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE"
+ "=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF("
+ "CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),NULL,NULL,NULL,NULL,IF(TACHE_TYPE IN "
+ "(1,2),'720130000',IF(TACHE_TYPE=3,'720200003','600000074')) FROM CC_CUSTOMER_JUDGE WHERE TACHE_TYPE "
+ "IN(3,4,5)AND JUDGE_STATUS=3 AND CALL_DEGREE IS NOT NULL AND CALL_JUDGE_DATE IS NOT NULL AND SERVICE_ORDER_ID=?)X ORDER BY RESPOND_DATE";
		} else {
			strSql = 
"SELECT*FROM(SELECT*FROM(SELECT A.WORK_SHEET_ID,DATE_FORMAT(RESPOND_DATE,'%Y-%m-%d %H:%i:%s')RESPOND_DATE,DATE_FORMAT(B.CREAT_DATA,'%Y-%m-%d %H:%i:%s')DX_DATE,"
+ "DEAL_ORG_ID DX_ORG_NAME,DEAL_STAFF_NAME DX_STAFF_NAME,TS_IF_BEING,APPEND_CASES,TS_REASON_NAME,PLUS_ONE_DESC,PLUS_TWO_DESC,DUTY_ORG_NAME,"
+ "CONTROL_AREA_FIR_DESC,CONTROL_AREA_SEC_DESC,DUTY_ORG_THIRD_DESC,OVERTIME_REASON_ID,OVERTIME_REASON_DESC,IS_REASONABLE,DEAL_CONTENT,DATE_FORMAT(C.CREAT_DATA,'%Y-%m-%d %H:%i:%s')HF_DATE,IF(visit_type='1',"
+ "DEAL_ORG_NAME,'')HF_ORG_NAME,IF(visit_type='1',DEAL_STAFF_NAME,'')HF_STAFF_NAME,IF(visit_type='2','自动回访',IF(visit_type='3','即时测评',IF(visit_type="
+ "'4','集团-短信',IF(visit_type='5','集团-微信',IF(visit_type='6','集团-APP',IF(visit_type='7','集团-智能外呼',IF(visit_type='8','集团-人工外呼','人工回访')))))))"
+ "HF_FLAG,COLLECTIVITY_CIRCS_NAME,TS_DEAL_ATTITUDE_NAME,TS_DEAL_BETIMES_NAME,TS_DEAL_RESULT_NAME,TS_VISIT_RESULT,IFNULL((SELECT IF(W.CONTACT_STATUS IN"
+ "(1,2),'是','否')FROM CC_CMP_UNIFIED_CONTACT_HIS W WHERE W.CONTACT_TYPE=1 AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=B.WORK_SHEET_ID"
+ " ORDER BY W.OPER_DATE DESC LIMIT 1),'否')CONTACTSTATUS,IFNULL((SELECT IF(W.CONTACT_STATUS IN(1,2),'是','否')FROM CC_CMP_UNIFIED_CONTACT_HIS W WHERE "
+ "W.CONTACT_TYPE=2 AND W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')"
+ "REQUIREUNINVITED,IFNULL((SELECT IF(W.REPEAT_STATUS IN(1,2),NEW_UCC,'否')FROM CC_CMP_UNIFIED_REPEAT_HIS W WHERE W.REPEAT_TYPE=1 AND W.CUR_SOI="
+ "A.SERVICE_ORDER_ID AND W.CUR_WHI=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC LIMIT 1),'否')ISREPEAT,IFNULL((SELECT IF(W.REPEAT_STATUS IN(1,2),NEW_UCC,"
+ "'否')FROM CC_CMP_UNIFIED_REPEAT_HIS W WHERE W.REPEAT_TYPE=2 AND W.CUR_SOI=A.SERVICE_ORDER_ID AND W.CUR_WHI=B.WORK_SHEET_ID ORDER BY W.OPER_DATE DESC "
+ "LIMIT 1),'否')ISUPREPEAT,A.SERVICE_TYPE "
+ "FROM(SELECT*FROM CC_WORK_SHEET_HIS WHERE SERVICE_ORDER_ID=?)A LEFT JOIN(SELECT C.*FROM CC_SHEET_QUALITATIVE_HIS C,(SELECT MAX(CREAT_DATA)CREAT_DATA,"
+ "SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_SHEET_QUALITATIVE_HIS WHERE SERVICE_ORDER_ID=?GROUP BY WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND "
+ "C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID=MD.SERVICE_ORDER_ID)B ON A.WORK_SHEET_ID=B.WORK_SHEET_ID LEFT JOIN(SELECT C.*FROM "
+ "CC_CUSTOMER_VISIT_HIS C WHERE SERVICE_ORDER_ID=?AND SERVICE_ORDER_ID NOT IN(SELECT SERVICE_ORDER_ID FROM CC_CUSTOMER_JUDGE_HIS WHERE TACHE_TYPE IN(3,"
+ "4,5)AND JUDGE_STATUS=3 AND CALL_DEGREE IS NOT NULL AND CALL_JUDGE_DATE IS NOT NULL AND SERVICE_ORDER_ID=?)ORDER BY CREAT_DATA DESC LIMIT 1)C ON "
+ "A.WORK_SHEET_ID=C.WORK_SHEET_ID)AS RT WHERE DX_DATE IS NOT NULL OR HF_DATE IS NOT NULL UNION ALL SELECT SERVICE_ORDER_ID,DATE_FORMAT(CALL_JUDGE_DATE,"
+ "'%Y-%m-%d %H:%i:%s'),NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,DATE_FORMAT(CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),NULL,NULL,"
+ "'自动回访',IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE"
+ "=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF("
+ "CALL_DEGREE=5,'满意','未评价'))),IF(CALL_DEGREE=1,'不满意',IF(CALL_DEGREE=3,'一般',IF(CALL_DEGREE=5,'满意','未评价'))),NULL,NULL,NULL,NULL,IF(TACHE_TYPE IN "
+ "(1,2),'720130000',IF(TACHE_TYPE=3,'720200003','600000074')) FROM CC_CUSTOMER_JUDGE_HIS WHERE "
+ "TACHE_TYPE IN(3,4,5)AND JUDGE_STATUS=3 AND CALL_DEGREE IS NOT NULL AND CALL_JUDGE_DATE IS NOT NULL AND SERVICE_ORDER_ID=?)X ORDER BY RESPOND_DATE";
		}
		List list = this.jt.queryForList(strSql, orderId, orderId, orderId, orderId, orderId);
		if (list.isEmpty()) {
			return Collections.emptyList();
		} else {
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				String don = "DX_ORG_NAME";
				String dxOrgName = map.get(don) == null ? "" : pubFun.getOrgWater(map.get(don).toString());
				map.put(don, dxOrgName);
				list.set(i, map);
			}
		}
		return list;
	}

	/**
	 * 得到工单操作日志
	 * @param orderId 服务单号
	 * @param boo     true 查询当前 false 查询历史
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List getOrderOperationLogs(String orderId, boolean boo) {
		List list = getTJGDSLLogs(orderId, boo);
		if (list.isEmpty()) {
			return Collections.emptyList();
		}
		addList(list, getHQJTBMLogs(orderId, boo));
		addList(list, getGDFPLogs(orderId, boo));
		addList(list, getCDLogs(orderId, boo));
		addList(list, getHTPDLogs(orderId, boo));
		addList(list, getBMCLLogs(orderId, boo));
		addList(list, getSPLogs(orderId, boo));
		addList(list, getXCLogs(orderId, boo));
		addList(list, getBMYDXLogs(orderId, boo));
		addList(list, getZDXBJLogs(orderId, boo));
		addList(list, getZDXZPLogs(orderId, boo));
		addList(list, getJSCPLogs(orderId, boo));
		addList(list, getZDHFLogs(orderId, boo));
		addList(list, getRGHFLogs(orderId, boo));
		addList(list, getGDLogs(orderId, boo));
		list.sort(Comparator.comparing(OrderOperationLog::getDealDate));//正序
		return list;
	}

	@SuppressWarnings("unchecked")
	private List addList(List list, List temp) {
		if (!temp.isEmpty()) {
			for (int i = 0; i < temp.size(); i++) {
				list.add(temp.get(i));
			}
		}
		return list;
	}

	// 提交工单受理
	@SuppressWarnings("unchecked")
	private List getTJGDSLLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'申告环节'TACHE_DESC,'提交工单受理'OPER_NAME,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','产品号码：',A.PROD_NUM,'\n最强工单：',"
+ "B.BEST_ORDER_DESC,'\n工单受理内容：',B.ACCEPT_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID="
+ "A.ACCEPT_STAFF_ID),A.ACCEPT_STAFF_NAME)DEAL_STAFF_NAME,A.ACCEPT_ORG_NAME DEAL_ORG_NAME FROM CC_SERVICE_ORDER_ASK A,CC_SERVICE_CONTENT_ASK B WHERE "
+ "A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=?ORDER BY A.ORDER_VESION LIMIT 1";
		} else {
			strSql = 
"SELECT'申告环节'TACHE_DESC,'提交工单受理'OPER_NAME,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','产品号码：',A.PROD_NUM,'\n最强工单：',"
+ "B.BEST_ORDER_DESC,'\n工单受理内容：',B.ACCEPT_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID="
+ "A.ACCEPT_STAFF_ID),A.ACCEPT_STAFF_NAME)DEAL_STAFF_NAME,A.ACCEPT_ORG_NAME DEAL_ORG_NAME FROM CC_SERVICE_ORDER_ASK_HIS A,CC_SERVICE_CONTENT_ASK_HIS B "
+ "WHERE A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND A.ORDER_VESION=B.ORDER_VESION AND A.SERVICE_ORDER_ID=?ORDER BY A.ORDER_VESION LIMIT 1";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 获取集团编码
	@SuppressWarnings("unchecked")
	private List getHQJTBMLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT''TACHE_DESC,'获取集团编码'OPER_NAME,DATE_FORMAT(IFNULL(A.UPDATE_DATE,A.CREATE_DATE),'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','【系统自动触发短信】',"
+ "'\n接收号码：',A.RELA_INFO,'\n短信内容：',B.PUSH_MSG)DEAL_CONTENT,''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_CMP_UNIFIED_RETURN A,CC_CMP_UNIFIED_SHOW B "
+ "WHERE A.COMPLAINT_WORKSHEET_ID=b.SERVICE_ORDER_ID AND B.SERVICE_ORDER_ID=?AND B.STEP=0";
		} else {
			strSql = 
"SELECT''TACHE_DESC,'获取集团编码'OPER_NAME,DATE_FORMAT(IFNULL(A.UPDATE_DATE,A.NEW_DATE),'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','【系统自动触发短信】',"
+ "'\n接收号码：',A.RELA_INFO,'\n短信内容：',B.PUSH_MSG)DEAL_CONTENT,''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_CMP_UNIFIED_RETURN_HIS A,CC_CMP_UNIFIED_SHOW_HIS"
+ " B WHERE A.COMPLAINT_WORKSHEET_ID=b.SERVICE_ORDER_ID AND B.SERVICE_ORDER_ID=?AND B.STEP=0";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 工单分派
	@SuppressWarnings("unchecked")
	private List getGDFPLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'工单分派'TACHE_DESC,IF(B.ACTION_CODE=700001807,'自动分派','人工分派')OPER_NAME,DATE_FORMAT(B.OPRA_START_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,"
+ "B.OPRA_COMMENTS DEAL_CONTENT,IF(B.ACTION_CODE=700001807,'',IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID="
+ "B.OPRA_STAFF),B.OPRA_STAFF_NAME))DEAL_STAFF_NAME,IF(B.ACTION_CODE=700001807,'',B.OPRA_ORG_NAME)DEAL_ORG_NAME FROM CC_WORK_SHEET A,"
+ "CC_SHEET_FLOW_ACTION B WHERE A.WORK_SHEET_ID=B.WORK_SHEET_ID AND A.SOURCE_SHEET_ID=A.SERVICE_ORDER_ID AND B.ACTION_CODE IN(700001807,700001808,"
+ "700001811)AND A.SERVICE_ORDER_ID=?ORDER BY B.OPRA_START_DATE DESC LIMIT 1";
		} else {
			strSql = 
"SELECT'工单分派'TACHE_DESC,IF(B.ACTION_CODE=700001807,'自动分派','人工分派')OPER_NAME,DATE_FORMAT(B.OPRA_START_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,"
+ "B.OPRA_COMMENTS DEAL_CONTENT,IF(B.ACTION_CODE=700001807,'',IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID="
+ "B.OPRA_STAFF),B.OPRA_STAFF_NAME))DEAL_STAFF_NAME,IF(B.ACTION_CODE=700001807,'',B.OPRA_ORG_NAME)DEAL_ORG_NAME FROM CC_WORK_SHEET_HIS A,"
+ "CC_SHEET_FLOW_ACTION_HIS B WHERE A.WORK_SHEET_ID=B.WORK_SHEET_ID AND A.SOURCE_SHEET_ID=A.SERVICE_ORDER_ID AND B.ACTION_CODE IN(700001807,700001808,"
+ "700001811)AND A.SERVICE_ORDER_ID=?ORDER BY B.OPRA_START_DATE DESC LIMIT 1";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 催单
	@SuppressWarnings("unchecked")
	private List getCDLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT''TACHE_DESC,'催单'OPER_NAME,DATE_FORMAT(A.CREAT_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','第',@ROWNUM:=@ROWNUM+1,'次催单\n',A.HASTEN_INFO)"
+ "DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.SEND_STAFF_ID),A.SEND_STAFF_NAME)DEAL_STAFF_NAME,"
+ "A.SEND_ORG_NAME DEAL_ORG_NAME FROM(SELECT @ROWNUM:=0,CREAT_DATE,SEND_ORG_NAME,SEND_STAFF_ID,SEND_STAFF_NAME,IF(LOCATE('催单部门',HASTEN_INFO)=1,"
+ "HASTEN_INFO,CONCAT_WS('','催单内容：',HASTEN_INFO))HASTEN_INFO FROM CC_HASTEN_SHEET WHERE SERVICE_ORDER_ID=?"
+ "ORDER BY CREAT_DATE,WORK_SHEET_ID)A";
		} else {
			strSql = 
"SELECT''TACHE_DESC,'催单'OPER_NAME,DATE_FORMAT(A.CREAT_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','第',@ROWNUM:=@ROWNUM+1,'次催单\n',A.HASTEN_INFO)"
+ "DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.SEND_STAFF_ID),A.SEND_STAFF_NAME)DEAL_STAFF_NAME,"
+ "A.SEND_ORG_NAME DEAL_ORG_NAME FROM(SELECT @ROWNUM:=0,CREAT_DATE,SEND_ORG_NAME,SEND_STAFF_ID,SEND_STAFF_NAME,IF(LOCATE('催单部门',HASTEN_INFO)=1,"
+ "HASTEN_INFO,CONCAT_WS('','催单内容：',HASTEN_INFO))HASTEN_INFO FROM CC_HASTEN_SHEET_HIS WHERE SERVICE_ORDER_ID=?"
+ "ORDER BY CREAT_DATE,WORK_SHEET_ID)A";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 后台派单
	@SuppressWarnings("unchecked")
	private List getHTPDLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'派单环节'TACHE_DESC,'后台派单'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,IF(A.DEAL_DESC IN('分派工单','审核派单环节派单'),CONCAT_WS("
+ "'','派单部门：',A.DEAL_ORG_NAME,'\n收单部门：',A.RECEIVE_ORG_DISPLAY,'\n派单内容：',A.DEAL_CONTENT),CONCAT_WS('','处理内容：',A.DEAL_CONTENT))DEAL_CONTENT,"
+ "IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME "
+ "DEAL_ORG_NAME FROM(SELECT SHEET_STATU,RESPOND_DATE,DEAL_STAFF,DEAL_STAFF_NAME,DEAL_ORG_NAME,DEAL_CONTENT,DEAL_DESC,RECEIVE_ORG_DISPLAY FROM "
+ "CC_WORK_SHEET WHERE SHEET_TYPE IN(720130011,600000069,600000075,700000126)AND SERVICE_ORDER_ID=?ORDER BY CREAT_DATE DESC LIMIT 1)A WHERE "
+ "A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND A.SHEET_STATU IN(700000047,720130036)";
		} else {
			strSql = 
"SELECT'派单环节'TACHE_DESC,'后台派单'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,IF(A.DEAL_DESC IN('分派工单','审核派单环节派单'),CONCAT_WS("
+ "'','派单部门：',A.DEAL_ORG_NAME,'\n收单部门：',A.RECEIVE_ORG_DISPLAY,'\n派单内容：',A.DEAL_CONTENT),CONCAT_WS('','处理内容：',A.DEAL_CONTENT))DEAL_CONTENT,"
+ "IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME "
+ "DEAL_ORG_NAME FROM(SELECT SHEET_STATU,RESPOND_DATE,DEAL_STAFF,DEAL_STAFF_NAME,DEAL_ORG_NAME,DEAL_CONTENT,DEAL_DESC,RECEIVE_ORG_DISPLAY FROM "
+ "CC_WORK_SHEET_HIS WHERE SHEET_TYPE IN(720130011,600000069,600000075,700000126)AND SERVICE_ORDER_ID=?ORDER BY CREAT_DATE DESC LIMIT 1)A WHERE "
+ "A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND A.SHEET_STATU IN(700000047,720130036)";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 部门处理
	@SuppressWarnings("unchecked")
	private List getBMCLLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'处理环节'TACHE_DESC,'部门处理'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,IF(A.DEAL_DESC='部门转派',CONCAT_WS('','派单部门：',"
+ "A.DEAL_ORG_NAME,'\n收单部门：',A.RECEIVE_ORG_DISPLAY,'\n派单内容：',A.DEAL_CONTENT),CONCAT_WS('','处理内容：',A.DEAL_CONTENT))DEAL_CONTENT,IFNULL((SELECT "
+ "CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM "
+ "CC_WORK_SHEET A WHERE A.SHEET_TYPE IN(600000077,700000127,700000135,720130013,720130014)AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL "
+ "AND A.SHEET_STATU IN(700000047,720130036)AND SERVICE_ORDER_ID=?";
		} else {
			strSql = 
"SELECT'处理环节'TACHE_DESC,'部门处理'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,IF(A.DEAL_DESC='部门转派',CONCAT_WS('','派单部门：',"
+ "A.DEAL_ORG_NAME,'\n收单部门：',A.RECEIVE_ORG_DISPLAY,'\n派单内容：',A.DEAL_CONTENT),CONCAT_WS('','处理内容：',A.DEAL_CONTENT))DEAL_CONTENT,IFNULL((SELECT "
+ "CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM "
+ "CC_WORK_SHEET_HIS A WHERE A.SHEET_TYPE IN(600000077,700000127,700000135,720130013,720130014)AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT "
+ "NULL AND A.SHEET_STATU IN(700000047,720130036)AND SERVICE_ORDER_ID=?";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 审批
	@SuppressWarnings("unchecked")
	private List getSPLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'审批环节'TACHE_DESC,'审批'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,IF(A.DEAL_DESC='部门转派',CONCAT_WS('','派单部门：',"
+ "A.DEAL_ORG_NAME,'\n收单部门：',A.RECEIVE_ORG_DISPLAY,'\n派单内容：',A.DEAL_CONTENT),CONCAT_WS('','处理内容：',A.DEAL_CONTENT))DEAL_CONTENT,IFNULL((SELECT "
+ "CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM "
+ "CC_WORK_SHEET A WHERE A.SHEET_TYPE IN(700001002,720130015)AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND A.SHEET_STATU IN(700000047,"
+ "720130036)AND SERVICE_ORDER_ID=?";
		} else {
			strSql = 
"SELECT'审批环节'TACHE_DESC,'审批'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,IF(A.DEAL_DESC='部门转派',CONCAT_WS('','派单部门：',"
+ "A.DEAL_ORG_NAME,'\n收单部门：',A.RECEIVE_ORG_DISPLAY,'\n派单内容：',A.DEAL_CONTENT),CONCAT_WS('','处理内容：',A.DEAL_CONTENT))DEAL_CONTENT,IFNULL((SELECT "
+ "CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM "
+ "CC_WORK_SHEET_HIS A WHERE A.SHEET_TYPE IN(700001002,720130015)AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND A.SHEET_STATU IN("
+ "700000047,720130036)AND SERVICE_ORDER_ID=?";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 协查
	@SuppressWarnings("unchecked")
	private List getXCLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'处理环节'TACHE_DESC,'协查'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','协查发起单位：',A.RETURN_ORG_NAME,'\n协查要求：',"
+ "A.DEAL_REQUIRE,'\n协查接收单位：',A.DEAL_ORG_NAME,'\n协查结果：',A.DEAL_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF "
+ "WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET A WHERE A.SHEET_TYPE=720130028 AND "
+ "A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND A.SHEET_STATU IN(700000047,720130036)AND SERVICE_ORDER_ID=?";
		} else {
			strSql = 
"SELECT'处理环节'TACHE_DESC,'协查'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','协查发起单位：',A.RETURN_ORG_NAME,'\n协查要求：',"
+ "A.DEAL_REQUIRE,'\n协查接收单位：',A.DEAL_ORG_NAME,'\n协查结果：',A.DEAL_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF "
+ "WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET_HIS A WHERE A.SHEET_TYPE=720130028 "
+ "AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND A.SHEET_STATU IN(700000047,720130036)AND SERVICE_ORDER_ID=?";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 部门预定性
	@SuppressWarnings("unchecked")
	private List getBMYDXLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'定性环节'TACHE_DESC,'部门预定性'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','投诉是否成立：',IF(B.TS_IF_BEING IN("
+ "700001818,700001819),'成立','不成立'),'\n责任定性：',CONCAT_WS('-',B.CONTROL_AREA_FIR_DESC,B.CONTROL_AREA_SEC_DESC),'\n责任部门二级：',B.DUTY_ORG_NAME,"
+ "'\n责任部门三级：',B.DUTY_ORG_THIRD_DESC,'\n办结原因：',B.TS_REASON_NAME,'\n定性情况：',A.DEAL_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',"
+ "LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET A,("
+ "SELECT C.*FROM CC_SHEET_QUALITATIVE C,(SELECT MAX(CREAT_DATA)CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_SHEET_QUALITATIVE WHERE "
+ "SERVICE_ORDER_ID=?GROUP BY WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID="
+ "MD.SERVICE_ORDER_ID)B WHERE A.WORK_SHEET_ID=B.WORK_SHEET_ID AND A.SHEET_TYPE=720130016 AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND "
+ "A.SHEET_STATU IN(700000047,720130036)AND A.SERVICE_ORDER_ID=?";
		} else {
			strSql = 
"SELECT'定性环节'TACHE_DESC,'部门预定性'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','投诉是否成立：',IF(B.TS_IF_BEING IN("
+ "700001818,700001819),'成立','不成立'),'\n责任定性：',CONCAT_WS('-',B.CONTROL_AREA_FIR_DESC,B.CONTROL_AREA_SEC_DESC),'\n责任部门二级：',B.DUTY_ORG_NAME,"
+ "'\n责任部门三级：',B.DUTY_ORG_THIRD_DESC,'\n办结原因：',B.TS_REASON_NAME,'\n定性情况：',A.DEAL_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',"
+ "LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET_HIS A,("
+ "SELECT C.*FROM CC_SHEET_QUALITATIVE_HIS C,(SELECT MAX(CREAT_DATA)CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_SHEET_QUALITATIVE_HIS WHERE "
+ "SERVICE_ORDER_ID=?GROUP BY WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID="
+ "MD.SERVICE_ORDER_ID)B WHERE A.WORK_SHEET_ID=B.WORK_SHEET_ID AND A.SHEET_TYPE=720130016 AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND "
+ "A.SHEET_STATU IN(700000047,720130036)AND A.SERVICE_ORDER_ID=?";
		}
		return jt.query(strSql, new Object[] { orderId, orderId }, new OrderOperationLogRmp());
	}

	// 终定性办结
	@SuppressWarnings("unchecked")
	private List getZDXBJLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'定性环节'TACHE_DESC,'终定性'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','投诉是否成立：',IF(B.TS_IF_BEING IN(700001818"
+ ",700001819),'成立','不成立'),'\n责任定性：',CONCAT_WS('-',B.CONTROL_AREA_FIR_DESC,B.CONTROL_AREA_SEC_DESC),'\n责任部门二级：',B.DUTY_ORG_NAME,'\n责任部门三级：',"
+ "B.DUTY_ORG_THIRD_DESC,'\n办结原因：',B.TS_REASON_NAME,'\n定性情况：',A.DEAL_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM "
+ "TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET A,(SELECT C.*FROM "
+ "CC_SHEET_QUALITATIVE C,(SELECT MAX(CREAT_DATA)CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_SHEET_QUALITATIVE WHERE SERVICE_ORDER_ID=?GROUP BY "
+ "WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID=MD.SERVICE_ORDER_ID)B WHERE "
+ "A.WORK_SHEET_ID=B.WORK_SHEET_ID AND A.SHEET_TYPE=720130017 AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IN('终定性到竣工','终定性到人工回访')AND "
+ "A.SHEET_STATU IN(700000047,720130036)AND A.SERVICE_ORDER_ID=?";
		} else {
			strSql = 
"SELECT'定性环节'TACHE_DESC,'终定性'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','投诉是否成立：',IF(B.TS_IF_BEING IN(700001818"
+ ",700001819),'成立','不成立'),'\n责任定性：',CONCAT_WS('-',B.CONTROL_AREA_FIR_DESC,B.CONTROL_AREA_SEC_DESC),'\n责任部门二级：',B.DUTY_ORG_NAME,'\n责任部门三级：',"
+ "B.DUTY_ORG_THIRD_DESC,'\n办结原因：',B.TS_REASON_NAME,'\n定性情况：',A.DEAL_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM "
+ "TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET_HIS A,(SELECT C.*FROM "
+ "CC_SHEET_QUALITATIVE_HIS C,(SELECT MAX(CREAT_DATA)CREAT_DATA,SERVICE_ORDER_ID,WORK_SHEET_ID FROM CC_SHEET_QUALITATIVE_HIS WHERE SERVICE_ORDER_ID=?"
+ "GROUP BY WORK_SHEET_ID)MD WHERE C.CREAT_DATA=MD.CREAT_DATA AND C.WORK_SHEET_ID=MD.WORK_SHEET_ID AND C.SERVICE_ORDER_ID=MD.SERVICE_ORDER_ID)B WHERE "
+ "A.WORK_SHEET_ID=B.WORK_SHEET_ID AND A.SHEET_TYPE=720130017 AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IN('终定性到竣工','终定性到人工回访')AND "
+ "A.SHEET_STATU IN(700000047,720130036)AND A.SERVICE_ORDER_ID=?";
		}
		return jt.query(strSql, new Object[] { orderId, orderId }, new OrderOperationLogRmp());
	}

	// 终定性转派
	@SuppressWarnings("unchecked")
	private List getZDXZPLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'定性环节'TACHE_DESC,'终定性'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','派单部门：',A.DEAL_ORG_NAME,'\n收单部门：',"
+ "A.RECEIVE_ORG_DISPLAY,'\n派单内容：',A.DEAL_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID="
+ "A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET A WHERE A.SHEET_TYPE=720130017 AND A.RESPOND_DATE IS "
+ "NOT NULL AND A.DEAL_DESC='终定性重新派发工单'AND A.SHEET_STATU IN(700000047,720130036)AND A.SERVICE_ORDER_ID=?";
		} else {
			strSql = 
"SELECT'定性环节'TACHE_DESC,'终定性'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','派单部门：',A.DEAL_ORG_NAME,'\n收单部门：',"
+ "A.RECEIVE_ORG_DISPLAY,'\n派单内容：',A.DEAL_CONTENT)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')')FROM TSM_STAFF WHERE STAFF_ID="
+ "A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET_HIS A WHERE A.SHEET_TYPE=720130017 AND A.RESPOND_DATE"
+ " IS NOT NULL AND A.DEAL_DESC='终定性重新派发工单'AND A.SHEET_STATU IN(700000047,720130036)AND A.SERVICE_ORDER_ID=?";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 即时测评
	@SuppressWarnings("unchecked")
	private List getJSCPLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'回访环节'TACHE_DESC,'即时测评'OPER_NAME,DATE_FORMAT(A.IVR_JUDGE_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','回访方式：','即时测评','\n回访时间：',"
+ "DATE_FORMAT(A.IVR_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),'\n回访结果：',IF(A.IVR_DEGREE=0,'未评价',IF(A.IVR_DEGREE=1,'满意',IF(A.IVR_DEGREE=2,'一般',IF("
+ "A.IVR_DEGREE=3,'服务态度冷淡',IF(A.IVR_DEGREE=4,'业务解释听不懂',IF(A.IVR_DEGREE=5,'处理速度慢',IF(A.IVR_DEGREE=6,'处理方案未达到期望值','问题未解决'))))))))DEAL_CONTENT,"
+ "''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_CUSTOMER_JUDGE A WHERE A.JUDGE_STATUS IN(1,3)AND A.IVR_DEGREE IS NOT NULL AND A.IVR_JUDGE_DATE IS NOT NULL "
+ "AND A.SERVICE_ORDER_ID=?";
		} else {
			strSql = 
"SELECT'回访环节'TACHE_DESC,'即时测评'OPER_NAME,DATE_FORMAT(A.IVR_JUDGE_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','回访方式：','即时测评','\n回访时间：',"
+ "DATE_FORMAT(A.IVR_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),'\n回访结果：',IF(A.IVR_DEGREE=0,'未评价',IF(A.IVR_DEGREE=1,'满意',IF(A.IVR_DEGREE=2,'一般',IF("
+ "A.IVR_DEGREE=3,'服务态度冷淡',IF(A.IVR_DEGREE=4,'业务解释听不懂',IF(A.IVR_DEGREE=5,'处理速度慢',IF(A.IVR_DEGREE=6,'处理方案未达到期望值','问题未解决'))))))))DEAL_CONTENT,"
+ "''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_CUSTOMER_JUDGE_HIS A WHERE A.JUDGE_STATUS IN(1,3)AND A.IVR_DEGREE IS NOT NULL AND A.IVR_JUDGE_DATE IS NOT "
+ "NULL AND A.SERVICE_ORDER_ID=?";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 自动回访
	@SuppressWarnings("unchecked")
	private List getZDHFLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'回访环节'TACHE_DESC,'自动回访'OPER_NAME,DATE_FORMAT(A.CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','回访方式：','自动回访','\n回访时间：',"
+ "DATE_FORMAT(A.CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),'\n回访结果：',IF(A.CALL_DEGREE=1,'不满意',IF(A.CALL_DEGREE=3,'一般',IF(A.CALL_DEGREE=5,'满意','未评价'))))"
+ "DEAL_CONTENT,''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_CUSTOMER_JUDGE A WHERE A.JUDGE_STATUS=3 AND A.CALL_DEGREE IS NOT NULL AND A.CALL_JUDGE_DATE IS "
+ "NOT NULL AND A.SERVICE_ORDER_ID=?";
		} else {
			strSql = 
"SELECT'回访环节'TACHE_DESC,'自动回访'OPER_NAME,DATE_FORMAT(A.CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','回访方式：','自动回访','\n回访时间：',"
+ "DATE_FORMAT(A.CALL_JUDGE_DATE,'%Y-%m-%d %H:%i:%s'),'\n回访结果：',IF(A.CALL_DEGREE=1,'不满意',IF(A.CALL_DEGREE=3,'一般',IF(A.CALL_DEGREE=5,'满意','未评价'))))"
+ "DEAL_CONTENT,''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_CUSTOMER_JUDGE_HIS A WHERE A.JUDGE_STATUS=3 AND A.CALL_DEGREE IS NOT NULL AND A.CALL_JUDGE_DATE"
+ " IS NOT NULL AND A.SERVICE_ORDER_ID=?";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 人工回访
	@SuppressWarnings("unchecked")
	private List getRGHFLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			strSql = 
"SELECT'回访环节'TACHE_DESC,'人工回访'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','回访方式：','人工回访','\n回访时间：',"
+ "DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s'),'\n回访结果：',B.COLLECTIVITY_CIRCS_NAME)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')'"
+ ")FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET A,CC_CUSTOMER_VISIT "
+ "B WHERE A.WORK_SHEET_ID=B.WORK_SHEET_ID AND A.SHEET_TYPE=720130018 AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL AND A.SHEET_STATU IN("
+ "700000047,720130036)AND A.SERVICE_ORDER_ID=?LIMIT 1";
		} else {
			strSql = 
"SELECT'回访环节'TACHE_DESC,'人工回访'OPER_NAME,DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s')DEAL_DATE,CONCAT_WS('','回访方式：','人工回访','\n回访时间：',"
+ "DATE_FORMAT(A.RESPOND_DATE,'%Y-%m-%d %H:%i:%s'),'\n回访结果：',B.COLLECTIVITY_CIRCS_NAME)DEAL_CONTENT,IFNULL((SELECT CONCAT(STAFFNAME,'(',LOGONNAME,')'"
+ ")FROM TSM_STAFF WHERE STAFF_ID=A.DEAL_STAFF),A.DEAL_STAFF_NAME)DEAL_STAFF_NAME,A.DEAL_ORG_NAME DEAL_ORG_NAME FROM CC_WORK_SHEET_HIS A,"
+ "CC_CUSTOMER_VISIT_HIS B WHERE A.WORK_SHEET_ID=B.WORK_SHEET_ID AND A.SHEET_TYPE=720130018 AND A.RESPOND_DATE IS NOT NULL AND A.DEAL_DESC IS NOT NULL "
+ "AND A.SHEET_STATU IN(700000047,720130036)AND A.SERVICE_ORDER_ID=?LIMIT 1";
		}
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	// 归档
	@SuppressWarnings("unchecked")
	private List getGDLogs(String orderId, boolean boo) {
		String strSql = "";
		if (boo) {
			return Collections.emptyList();
		}
		strSql = 
"SELECT'回访环节'TACHE_DESC,'回访完成'OPER_NAME,DATE_FORMAT(DATE_ADD(A.CALL_JUDGE_DATE,INTERVAL 1 SECOND),'%Y-%m-%d %H:%i:%s')DEAL_DATE,"
+ "'【系统自动信息】工单自动回访完成，直接归档'DEAL_CONTENT,''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_CUSTOMER_JUDGE_HIS A WHERE(A.TACHE_TYPE IN(3,4,5)OR(A.TACHE_TYPE"
+ " IN(1,2)AND A.CALL_DEGREE!=1))AND A.JUDGE_STATUS=3 AND A.CALL_DEGREE IS NOT NULL AND A.CALL_JUDGE_DATE IS NOT NULL AND A.SERVICE_ORDER_ID=?";
		List zdhf = jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
		if (!zdhf.isEmpty()) {
			return zdhf;
		}
		strSql = 
"SELECT'回访环节'TACHE_DESC,'回访完成'OPER_NAME,DATE_FORMAT(DATE_ADD(GREATEST(A.CREATE_DATE,A.IVR_JUDGE_DATE,B.FINISH_DATE),INTERVAL 1 SECOND),"
+ "'%Y-%m-%d %H:%i:%s')DEAL_DATE,'【系统自动信息】工单即时测评完成，直接归档'DEAL_CONTENT,''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_CUSTOMER_JUDGE_HIS A,"
+ "CC_SERVICE_ORDER_ASK_HIS B WHERE A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND(A.TACHE_TYPE IN(3,4,5)OR(A.TACHE_TYPE IN(1,2)AND A.IVR_DEGREE IN(1,2)))AND"
+ " A.JUDGE_STATUS=1 AND A.IVR_DEGREE IS NOT NULL AND A.IVR_JUDGE_DATE IS NOT NULL AND A.SERVICE_ORDER_ID=?ORDER BY B.ORDER_VESION DESC LIMIT 1";
		List jscp = jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
		if (!jscp.isEmpty()) {
			return jscp;
		}
		strSql = 
"SELECT'归档环节'TACHE_DESC,'工单归档'OPER_NAME,DATE_FORMAT(DATE_ADD(A.FINISH_DATE,INTERVAL 1 SECOND),'%Y-%m-%d %H:%i:%s')DEAL_DATE,"
+ "'【系统自动信息】工单完成，直接归档'DEAL_CONTENT,''DEAL_STAFF_NAME,''DEAL_ORG_NAME FROM CC_SERVICE_ORDER_ASK_HIS A,CC_SERVICE_CONTENT_ASK_HIS B WHERE "
+ "A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND A.ORDER_VESION=B.ORDER_VESION AND A.SERVICE_ORDER_ID=?ORDER BY A.ORDER_VESION LIMIT 1";
		return jt.query(strSql, new Object[] { orderId }, new OrderOperationLogRmp());
	}

	/**
	 * 根据订单ID查询最近一条定性记录
	 * @param orderId 订单号
	 * @param regionId 地域
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public TsSheetQualitative getLatestQualitativeByOrderId(String orderId, int regionId) {
		String strSql = "SELECT a.service_order_id,a.work_sheet_id,a.region_id,a.region_name,a.pigeonhole_sort_id,a.pigeonhole_sort_name,a.ts_reason_id,"
				+ "a.ts_reason_name,a.ts_if_being,a.append_cases,a.cases_id,a.cases_name,a.creat_data,a.month_flag,a.ts_key_word,a.ts_key_word_desc,"
				+ "a.duty_org,a.duty_org_name,a.sub_key_word,a.sub_key_word_desc,a.three_grade_catalog,a.three_grade_catalog_desc,a.four_grade_catalog,"
				+ "a.four_grade_catalog_desc,a.five_grade_catalog,a.five_grade_catalog_desc,a.control_area_fir,a.control_area_fir_desc,"
				+ "a.control_area_sec,a.control_area_sec_desc,a.satisfaction_id,a.satisfaction_desc,a.duty_org_third,a.duty_org_third_desc,a.force_flag,"
				+ "a.force_flag_desc,a.six_grade_catalog,a.six_grade_catalog_disc,a.unsatisfy_reason,a.sys_judge,a.last_deal_content,a.plus_one,"
				+ "a.plus_one_desc,a.plus_two,a.plus_two_desc,a.outlets_guid,a.outlets_name,a.outlets_address,a.outlets_ar_code,a.channel_tp_name,"
				+ "a.outlets_staff,a.outlets_staff_id,a.cust_order_nbr,a.create_channel_name,a.create_staff_code,a.create_staff_name,a.order_oper_type,"
				+ "a.order_create_date,a.order_status,a.overtime_reason_id,a.overtime_reason_desc,a.is_reasonable,b.channel_code,"
				+ "b.channel_classification,b.channel_details,b.channel_name,b.channel_location,b.agent_name,b.principal_district,b.principal_location,"
				+ "b.principal_county,b.principal_dept,b.admin_principal,b.channel_county,b.channel_principal,b.market_name,b.market_grade,b.market_id,"
				+ "c.deductions_id,c.deductions_name FROM cc_sheet_qualitative a LEFT JOIN cc_order_channel b ON a.service_order_id=b.service_order_id "
				+ "LEFT JOIN cc_sheet_outlets c ON a.service_order_id=c.service_order_id WHERE a.service_order_id=? AND a.region_id=? ORDER BY "
				+ "a.creat_data";
		List tmp = this.jt.query(strSql, new Object[] { orderId, regionId }, new TsSheetQualitativeRmp());
		int size = tmp.size();
		if (size == 0) {
			return null;
		}
		return (TsSheetQualitative) tmp.get(tmp.size() - 1);
	}

	@SuppressWarnings("unchecked")
	public TsSheetQualitative getLatestQualitativeHisByOrderId(String orderId) {
		String sql = "SELECT a.service_order_id,a.work_sheet_id,a.region_id,a.region_name,a.pigeonhole_sort_id,a.pigeonhole_sort_name,a.ts_reason_id,"
				+ "a.ts_reason_name,a.ts_if_being,a.append_cases,a.cases_id,a.cases_name,a.creat_data,a.month_flag,a.ts_key_word,a.ts_key_word_desc,"
				+ "a.duty_org,a.duty_org_name,a.sub_key_word,a.sub_key_word_desc,a.three_grade_catalog,a.three_grade_catalog_desc,a.four_grade_catalog,"
				+ "a.four_grade_catalog_desc,a.five_grade_catalog,a.five_grade_catalog_desc,a.control_area_fir,a.control_area_fir_desc,"
				+ "a.control_area_sec,a.control_area_sec_desc,a.satisfaction_id,a.satisfaction_desc,a.duty_org_third,a.duty_org_third_desc,a.force_flag,"
				+ "a.force_flag_desc,a.six_grade_catalog,a.six_grade_catalog_disc,a.unsatisfy_reason,a.sys_judge,a.last_deal_content,a.plus_one,"
				+ "a.plus_one_desc,a.plus_two,a.plus_two_desc,a.outlets_guid,a.outlets_name,a.outlets_address,a.outlets_ar_code,a.channel_tp_name,"
				+ "a.outlets_staff,a.outlets_staff_id,a.cust_order_nbr,a.create_channel_name,a.create_staff_code,a.create_staff_name,a.order_oper_type,"
				+ "a.order_create_date,a.order_status,a.overtime_reason_id,a.overtime_reason_desc,a.is_reasonable,b.channel_code,"
				+ "b.channel_classification,b.channel_details,b.channel_name,b.channel_location,b.agent_name,b.principal_district,b.principal_location,"
				+ "b.principal_county,b.principal_dept,b.admin_principal,b.channel_county,b.channel_principal,b.market_name,b.market_grade,b.market_id,"
				+ "c.deductions_id,c.deductions_name FROM cc_sheet_qualitative_his a LEFT JOIN cc_order_channel b ON "
				+ "a.service_order_id=b.service_order_id LEFT JOIN cc_sheet_outlets c ON a.service_order_id=c.service_order_id WHERE "
				+ "a.service_order_id=? ORDER BY a.creat_data DESC LIMIT 1";
		List list = jt.query(sql, new Object[] { orderId }, new TsSheetQualitativeRmp());
		if (list.isEmpty()) {
			if (log.isDebugEnabled()) {
				log.debug("没有查询到历史服务单号为: {}的定性记录", orderId);
			}
			return null;
		}
		TsSheetQualitative bean = (TsSheetQualitative) list.get(0);
		list.clear();
		list = null;
		return bean;
	}

	private String queryOrderQuaSQL;
    public List getOrderQualitative(String serviceID){
    	return jt.queryForList(queryOrderQuaSQL, serviceID);
	}

    private String queryOrderQuaHisSQL;
    public List getOrderQualitativeHis(String serviceID){
    	return jt.queryForList(queryOrderQuaHisSQL, serviceID);
	}

    public GridDataInfo selectSheetQualitativeGrid(String orderId, String startTime, String endTime, String orgId, String begin, String pageSize) {
		StringBuilder sb = new StringBuilder();
		sb.append(
"SELECT A.SERVICE_ORDER_ID,A.SERVICE_TYPE_DESC,A.REGION_ID,A.REGION_NAME,A.PROD_NUM,A.COME_CATEGORY,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,"
+ "A.ACCEPT_ORG_NAME,A.ACCEPT_STAFF_NAME,(SELECT RH.UNIFIED_COMPLAINT_CODE FROM CC_CMP_UNIFIED_RETURN_HIS RH WHERE RH.COMPLAINT_WORKSHEET_ID="
+ "A.SERVICE_ORDER_ID)UNIFIED_COMPLAINT_CODE,(SELECT SH.DEAL_ORG_ID FROM CC_WORK_SHEET_HIS SH WHERE SH.SHEET_TYPE IN(720130011,720130013,720130014,"
+ "700000126,700000127)AND SH.DEAL_CONTENT<>'系统自动处理'AND SH.RESPOND_DATE IS NOT NULL AND SH.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID ORDER BY "
+ "SH.CREAT_DATE DESC LIMIT 1)DEAL_ORG_ID,B.OUTLETS_GUID,B.OUTLETS_NAME,B.OUTLETS_ADDRESS,B.OUTLETS_AR_CODE,B.CHANNEL_TP_NAME,CASE WHEN "
+ "B.OUTLETS_STAFF IS NULL THEN '' ELSE '营业员' END DISPUTED_STAFF_TYPE_OLD,B.OUTLETS_STAFF DISPUTED_STAFF_OLD,B.OUTLETS_STAFF_ID DISPUTED_STAFF_ID_OLD,"
+ "B.CONTROL_AREA_FIR,B.CONTROL_AREA_SEC,B.CONTROL_AREA_SEC_DESC,B.DUTY_ORG,B.DUTY_ORG_NAME,B.DUTY_ORG_THIRD,B.DUTY_ORG_THIRD_DESC FROM CC_SERVICE_ORDER_ASK_HIS A,"
+ "CC_SHEET_QUALITATIVE_HIS B WHERE A.ORDER_STATU IN(700000103,720130010)AND A.ACCEPT_DATE>DATE_SUB(NOW(),INTERVAL 30 DAY)AND "
+ "A.SERVICE_TYPE=720130000 AND A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID");
		if (!"".equals(orderId)) {
			sb.append(" AND A.SERVICE_ORDER_ID='").append(orderId).append("'");
		}
		if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
			sb.append(" AND A.ACCEPT_DATE>STR_TO_DATE('").append(startTime).append("','%Y-%m-%d %H:%i:%s')");
			sb.append(" AND A.ACCEPT_DATE<STR_TO_DATE('").append(endTime).append("','%Y-%m-%d %H:%i:%s')");
		}
		String frontSql = 
"SELECT F.*,G.CREATE_DATE MODIFY_DATE,G.DEDUCTIONS_ID_OLD,G.DEDUCTIONS_NAME_OLD,G.STORENM_NEW,G.DISPUTED_STAFF_TYPE_NEW,G.DISPUTED_STAFF_NEW,G.CONTROL_AREA_DESC_NEW,G.DUTY_ORG_NAME_NEW,G.DUTY_ORG_THIRD_DESC_NEW FROM("
+ "SELECT C.*FROM(SELECT*,IF(@ORDERID=SERVICE_ORDER_ID,@RANK:=@RANK+1,@RANK:=1)RANK,@ORDERID:=SERVICE_ORDER_ID FROM(";
		String behindSql = 
" ORDER BY A.SERVICE_ORDER_ID ASC,B.CREAT_DATA DESC)X,(SELECT @ORDERID:=NULL,@rank:=0)Y)C,TSM_ORGANIZATION D,TSM_ORGANIZATION E WHERE RANK=1 AND "
+ "C.DEAL_ORG_ID=D.ORG_ID AND IF(SUBSTRING_INDEX(D.LINKID,'-',2)='10-11',SUBSTRING_INDEX(D.LINKID,'-',3),SUBSTRING_INDEX(D.LINKID,'-',2))=E.LINKID AND "
+ "E.ORG_ID='"+orgId+"')F LEFT JOIN CC_SHEET_QUALITATIVE_GRID G ON F.SERVICE_ORDER_ID=G.SERVICE_ORDER_ID";
		return dbgridDataPub.getResultBySize(frontSql + sb.toString() + behindSql, Integer.parseInt(begin), Integer.parseInt(pageSize), " ORDER BY ACCEPT_DATE", "");
	}

	public int insertSheetQualitativeGrid(TsSheetQualitativeGrid sqg) {
		jt.update(deleteSheetQualitativeGridSql, sqg.getServiceOrderId());
    	jt.update(updateOutletsSql,sqg.getStoreidNew(),sqg.getStorenmNew(),sqg.getDeductionsIdNew(),sqg.getDeductionsNameNew(),
				sqg.getDisputedStaffNew(),sqg.getOutletsStaffName(),sqg.getServiceOrderId());
		return jt.update(insertSheetQualitativeGridSql, sqg.getServiceOrderId(), sqg.getCreateStaff(),
				sqg.getStoreidOld(), sqg.getStorenmOld(), sqg.getAddressOld(), sqg.getArcodeOld(),
				sqg.getChanneltpnameOld(), sqg.getDisputedStaffTypeOld(), sqg.getDisputedStaffOld(),sqg.getOutletsStaffNameOld(),
				sqg.getControlAreaOld(), sqg.getControlAreaDescOld(), sqg.getDutyOrgOld(), sqg.getDutyOrgNameOld(),
				sqg.getDutyOrgThirdOld(), sqg.getDutyOrgThirdDescOld(), sqg.getStoreidNew(), sqg.getStorenmNew(),
				sqg.getAddressNew(), sqg.getArcodeNew(), sqg.getChanneltpnameNew(), sqg.getDisputedStaffTypeNew(),
				sqg.getDisputedStaffNew(),sqg.getOutletsStaffName(), sqg.getControlAreaNew(), sqg.getControlAreaDescNew(), sqg.getDutyOrgNew(),
				sqg.getDutyOrgNameNew(), sqg.getDutyOrgThirdNew(), sqg.getDutyOrgThirdDescNew(),sqg.getDeductionsIdOld(),
                sqg.getDeductionsNameOld(),sqg.getDeductionsIdNew(),sqg.getDeductionsNameNew());
	}

	public String getDeleteItsSheetQualitativeSql() {
		return deleteItsSheetQualitativeSql;
	}

	public void setDeleteItsSheetQualitativeSql(String deleteItsSheetQualitativeSql) {
		this.deleteItsSheetQualitativeSql = deleteItsSheetQualitativeSql;
	}

	public JdbcTemplate getJt() {
		return jt;
	}

	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}

	public String getSaveItsSheetQualitativeSql() {
		return saveItsSheetQualitativeSql;
	}

	public void setSaveItsSheetQualitativeSql(String saveItsSheetQualitativeSql) {
		this.saveItsSheetQualitativeSql = saveItsSheetQualitativeSql;
	}

	public String getSaveItsSheetQualitativeSqlHis() {
		return saveItsSheetQualitativeSqlHis;
	}

	public void setSaveItsSheetQualitativeSqlHis(
			String saveItsSheetQualitativeSqlHis) {
		this.saveItsSheetQualitativeSqlHis = saveItsSheetQualitativeSqlHis;
	}
    /**
     * 设置queryOrderQuaSQL
     * @param queryOrderQuaSQL 要设置的queryOrderQuaSQL。
     */
    public void setQueryOrderQuaSQL(String queryOrderQuaSQL) {
        this.queryOrderQuaSQL = queryOrderQuaSQL;
    }

	public void setQueryOrderQuaHisSQL(String queryOrderQuaHisSQL) {
		this.queryOrderQuaHisSQL = queryOrderQuaHisSQL;
	}
	
	public String getReceiptEvalObj(String orderId) {
		String strSql = "SELECT * FROM cc_service_receipt_eval c where c.service_order_id=? order by c.opration_time desc limit 1";
		String obj = null;
		List tmpList = this.jt.queryForList(strSql, orderId);
    	if (!tmpList.isEmpty()) {
    		Map map = (Map) tmpList.get(0);
    		obj = new Gson().toJson(map);
    	}
    	return obj;
	}

	public String getInsertSheetQualitativeGridSql() {
		return insertSheetQualitativeGridSql;
	}
	public void setInsertSheetQualitativeGridSql(String insertSheetQualitativeGridSql) {
		this.insertSheetQualitativeGridSql = insertSheetQualitativeGridSql;
	}
	public String getDeleteSheetQualitativeGridSql() {
		return deleteSheetQualitativeGridSql;
	}
	public void setDeleteSheetQualitativeGridSql(String deleteSheetQualitativeGridSql) {
		this.deleteSheetQualitativeGridSql = deleteSheetQualitativeGridSql;
	}
	public String getUpdateOutletsSql() {
		return updateOutletsSql;
	}
	public void setUpdateOutletsSql(String updateOutletsSql) {
		this.updateOutletsSql = updateOutletsSql;
	}
}