package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.worksheet.dao.IForceDistillDao;
import com.timesontransfar.customservice.worksheet.pojo.ForceDistill;

@Component("ForceDistillDaoImpl")
public class ForceDistillDaoImpl implements IForceDistillDao {
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ISystemAuthorization systemAuthorization;

	public int insertForceDistill(ForceDistill forceDistill) {
		String countForceDistillByOrderIdSql = "SELECT COUNT(1)FROM CC_FORCE_DISTILL WHERE SERVICE_ORDER_ID='" + forceDistill.getServiceOrderId() + "'";
		if (jdbcTemplate.queryForObject(countForceDistillByOrderIdSql, Integer.class) > 0) {
			return 0;
		}
		String insertForceDistillSql = 
"INSERT INTO CC_FORCE_DISTILL(SERVICE_ORDER_ID,ACCEPT_DATE,WORK_SHEET_ID,FORCE_DATE,FORCE_STAFF,FORCED_RECEIVE_ORG_ID,FORCED_RECEIVE_STAFF_ID,"
+ "FORCED_DEAL_ORG_ID,FORCED_DEAL_STAFF_ID)VALUES(?,STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?,NOW(),?,?,?,?,?)";
		return jdbcTemplate.update(insertForceDistillSql, forceDistill.getServiceOrderId(),
				forceDistill.getAcceptDate(), forceDistill.getWorkSheetId(), forceDistill.getForceStaff(),
				forceDistill.getForcedReceiveOrgid(), forceDistill.getForcedReceiveStaffId(),
				StringUtils.defaultIfEmpty(forceDistill.getForcedDealOrgId(), null),
				forceDistill.getForcedDealStaffId());
	}

	@SuppressWarnings("rawtypes")
	public int selectForceStaffByOrderId(String orderId) {
		String selectForceStaffByOrderIdSql = "SELECT FORCE_STAFF FROM CC_FORCE_DISTILL WHERE SERVICE_ORDER_ID=?";
		List forceStaffs = jdbcTemplate.queryForList(selectForceStaffByOrderIdSql, orderId);
		if (forceStaffs.isEmpty()) {
			return 0;
		}
		Map map = (Map) forceStaffs.get(0);
		int forceStaff = Integer.parseInt(map.get("FORCE_STAFF").toString());
		forceStaffs.clear();
		forceStaffs = null;
		return forceStaff;
	}

	public int insertForceDistillHisByOrderId(String orderId) {
		String insertForceDistillHisByOrderIdSql = 
"INSERT INTO CC_FORCE_DISTILL_HIS(SERVICE_ORDER_ID,ACCEPT_DATE,WORK_SHEET_ID,FORCE_DATE,FORCE_STAFF,FORCED_RECEIVE_ORG_ID,FORCED_RECEIVE_STAFF_ID,"
+ "FORCED_DEAL_ORG_ID,FORCED_DEAL_STAFF_ID)SELECT SERVICE_ORDER_ID,ACCEPT_DATE,WORK_SHEET_ID,FORCE_DATE,FORCE_STAFF,FORCED_RECEIVE_ORG_ID,"
+ "FORCED_RECEIVE_STAFF_ID,FORCED_DEAL_ORG_ID,FORCED_DEAL_STAFF_ID FROM CC_FORCE_DISTILL WHERE SERVICE_ORDER_ID=?";
		String deleteForceDistillByOrderIdSql = "DELETE FROM CC_FORCE_DISTILL WHERE SERVICE_ORDER_ID=?";
		jdbcTemplate.update(insertForceDistillHisByOrderIdSql, orderId);
		return jdbcTemplate.update(deleteForceDistillByOrderIdSql, orderId);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public GridDataInfo selectForceDistill(String regionId, String serviceType, String bestOrder, String hours, int begin) {
		StringBuilder sb = new StringBuilder();
		if (!"".equals(regionId)) {
			sb.append(" AND A.REGION_ID=");
			sb.append(regionId);
		}
		if (!"".equals(serviceType)) {
			sb.append(" AND A.SERVICE_TYPE=");
			sb.append(serviceType);
		}
		if (!"".equals(bestOrder)) {
			sb.append(" AND B.BEST_ORDER=");
			sb.append(bestOrder);
		}
		sb.append(" AND TIMESTAMPDIFF(MINUTE,A.ACCEPT_DATE,NOW())>=");
		sb.append(Integer.parseInt(hours) * 60);
		String selectForceDistillSql = 
"SELECT A.SERVICE_ORDER_ID,A.REGION_NAME,A.PROD_NUM,A.SERVICE_TYPE_DESC,A.ACCEPT_COME_FROM_DESC,A.ACCEPT_CHANNEL_DESC,B.BEST_ORDER_DESC,DATE_FORMAT("
+ "A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,CONCAT_WS('',CASE WHEN TRUNCATE(TIMESTAMPDIFF(MINUTE,A.ACCEPT_DATE,NOW())/60,0)>0 THEN CONCAT(TRUNCATE"
+ "(TIMESTAMPDIFF(MINUTE,A.ACCEPT_DATE,NOW())/60,0),'时')END,CASE WHEN TRUNCATE(MOD(TIMESTAMPDIFF(MINUTE,A.ACCEPT_DATE,NOW()),60),0)>=0 THEN TRUNCATE("
+ "MOD(TIMESTAMPDIFF(MINUTE,A.ACCEPT_DATE,NOW()),60),0)END,'分')DEAL_TIME,A.ORDER_STATU,A.REGION_ID,A.MONTH_FLAG FROM CC_SERVICE_ORDER_ASK A,"
+ "CC_SERVICE_CONTENT_ASK B,CC_SERVICE_LABEL C WHERE A.SERVICE_ORDER_ID=B.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=C.SERVICE_ORDER_ID AND "
+ "A.SERVICE_ORDER_ID NOT IN(SELECT SERVICE_ORDER_ID FROM CC_FORCE_DISTILL)AND A.SERVICE_TYPE IN(720130000,700006312,720200003,720200000)AND A.ORDER_STATU IN("
+ "720130004,720130005,720130006,720130008,700000104,700001493,700000101)AND B.BEST_ORDER>100122410 AND C.DX_FINISH_DATE IS NULL" + sb.toString();
		//权限匹配
		Map tableMap = new HashMap();
		tableMap.put("CC_SERVICE_ORDER_ASK", "A");
		tableMap.put("CC_SERVICE_CONTENT_ASK", "B");
		selectForceDistillSql = this.systemAuthorization.getAuthedSql(tableMap, selectForceDistillSql, "900018415");
		GridDataInfo gdis = dbgridDataPub.getResult(selectForceDistillSql, begin, " ORDER BY A.ACCEPT_DATE", "900018415");
		if (gdis.getQuryCount() > 0) {
			List list = gdis.getList();
			for (int i = 0; i < list.size(); i++) {
				Map map = (Map) list.get(i);
				String orderId = map.get("SERVICE_ORDER_ID").toString();
				String orderStatu = map.get("ORDER_STATU").toString();
				List sheetInfos = getCurSheetInfo(orderId, orderStatu);
				if (!sheetInfos.isEmpty()) {
					Map sheetInfo = (Map) sheetInfos.get(0);
					map.put("SHEET_ID", sheetInfo.get("WORK_SHEET_ID").toString());
					map.put("SHEET_STATU", sheetInfo.get("SHEET_STATU_DESC").toString());
					map.put("RECEIVE_ORG", sheetInfo.get("RECEIVE_ORG_NAME").toString());
					map.put("DEAL_ORG", sheetInfo.get("DEAL_ORG_NAME") == null ? "" : sheetInfo.get("DEAL_ORG_NAME").toString());
				} else {
					map.put("SHEET_ID", "");
					map.put("SHEET_STATU", "");
					map.put("RECEIVE_ORG", "");
					map.put("DEAL_ORG", "");
				}
				list.set(i, map);
			}
			gdis.setList(list);
		}
		return gdis;
	}

	@SuppressWarnings("rawtypes")
	public List getCurSheetInfo(String orderId, String orderStatu) {
		if ("720130004".equals(orderStatu) || "700000104".equals(orderStatu)) {
			return selectHTPDSheet(orderId);
		} else if ("720130005".equals(orderStatu) || "700001493".equals(orderStatu)) {
			return selectBMCLSheet(orderId);
		} else if ("720130006".equals(orderStatu)) {
			return selectYDXSheet(orderId);
		} else if ("720130008".equals(orderStatu)) {
			return selectZDXSheet(orderId);
		} else if ("700000101".equals(orderStatu)) {
			return selectSHSheet(orderId);
		} else {
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("rawtypes")
	private List selectHTPDSheet(String orderId) {
		String strSql = 
"SELECT WORK_SHEET_ID,SHEET_STATU_DESC,RECEIVE_ORG_NAME,RECEIVE_STAFF,DEAL_ORG_NAME,DEAL_STAFF FROM CC_WORK_SHEET WHERE LOCK_FLAG IN(0,1)AND SHEET_TYPE "
+ "IN(720130011,700000126,600000075)AND SHEET_STATU IN(720130031,720130032,700000044,700000045)AND SERVICE_ORDER_ID=?ORDER BY CREAT_DATE DESC LIMIT 1";
		return jdbcTemplate.queryForList(strSql, orderId);
	}

	@SuppressWarnings("rawtypes")
	private List selectBMCLSheet(String orderId) {
		String strSql = 
"SELECT WORK_SHEET_ID,SHEET_STATU_DESC,RECEIVE_ORG_NAME,RECEIVE_STAFF,DEAL_ORG_NAME,DEAL_STAFF FROM CC_WORK_SHEET WHERE LOCK_FLAG IN(0,1)AND SHEET_TYPE "
+ "IN(720130013,720130014,720130015,700000127,700001002,600000077)AND SHEET_STATU IN(720130031,720130032,700000048,600000001,600000002,600000003)AND "
+ "SERVICE_ORDER_ID=?ORDER BY CREAT_DATE DESC,MAIN_SHEET_FLAG DESC";
		return jdbcTemplate.queryForList(strSql, orderId);
	}

	@SuppressWarnings("rawtypes")
	private List selectYDXSheet(String orderId) {
		String strSql = 
"SELECT WORK_SHEET_ID,SHEET_STATU_DESC,RECEIVE_ORG_NAME,RECEIVE_STAFF,DEAL_ORG_NAME,DEAL_STAFF FROM CC_WORK_SHEET WHERE LOCK_FLAG IN(0,1)AND SHEET_TYPE="
+ "720130016 AND SHEET_STATU IN(720130031,720130032)AND SERVICE_ORDER_ID=?ORDER BY CREAT_DATE DESC LIMIT 1";
		return jdbcTemplate.queryForList(strSql, orderId);
	}

	@SuppressWarnings("rawtypes")
	private List selectZDXSheet(String orderId) {
		String strSql = 
"SELECT WORK_SHEET_ID,SHEET_STATU_DESC,RECEIVE_ORG_NAME,RECEIVE_STAFF,DEAL_ORG_NAME,DEAL_STAFF FROM CC_WORK_SHEET WHERE LOCK_FLAG IN(0,1)AND SHEET_TYPE="
+ "720130017 AND SHEET_STATU IN(720130031,720130032)AND SERVICE_ORDER_ID=?ORDER BY CREAT_DATE DESC LIMIT 1";
		return jdbcTemplate.queryForList(strSql, orderId);
	}

	@SuppressWarnings("rawtypes")
	private List selectSHSheet(String orderId) {
		String strSql = 
"SELECT WORK_SHEET_ID,SHEET_STATU_DESC,RECEIVE_ORG_NAME,RECEIVE_STAFF,DEAL_ORG_NAME,DEAL_STAFF FROM CC_WORK_SHEET WHERE LOCK_FLAG IN(0,1)AND SHEET_TYPE="
+ "600000076 AND SHEET_STATU IN(600000004,600000005)AND SERVICE_ORDER_ID=?ORDER BY CREAT_DATE DESC LIMIT 1";
		return jdbcTemplate.queryForList(strSql, orderId);
	}
}