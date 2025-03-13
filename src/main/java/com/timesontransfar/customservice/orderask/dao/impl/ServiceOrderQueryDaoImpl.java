package com.timesontransfar.customservice.orderask.dao.impl;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IDbgridDataFaceService;
import com.timesontransfar.customservice.orderask.dao.IServiceOrderQueryDao;

@Service
public class ServiceOrderQueryDaoImpl implements IServiceOrderQueryDao {
	private static final Logger log = LoggerFactory.getLogger(ServiceOrderQueryDaoImpl.class);
	@Autowired
	private JdbcTemplate jt;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IDbgridDataFaceService dbgridDataFaceService;
    
	public Map<String, Object> getWarnRole(String staffId) {
		String sql = "select r.role_class, r.role_department from cc_early_warning_role r, cc_early_warning_staff s "
				+ "where r.role_id = s.role_id and r.role_class in (2, 3) and s.staff_id = ?";
		List<Map<String, Object>> list = jt.queryForList(sql, staffId);
		if(list.isEmpty()) {
			return Collections.emptyMap();
		}
		return list.get(0);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map getStaffSheet(String staffId, String expiredDate) {
		Map<String, Object> mapForSheet = new HashMap();
		String pWhere = " AND W.SHEET_STATU NOT IN(720130035,700000046)";
		int pending = getMySheetCount(pWhere);// 未处理工单

		StringBuilder wsb = new StringBuilder();
		wsb.append(" AND W.SHEET_STATU NOT IN(720130035,700000046)");
		wsb.append("AND A.ORDER_LIMIT_TIME>0");
		wsb.append(" AND TIMESTAMPDIFF(SECOND,A.ACCEPT_DATE,IFNULL(L.DX_FINISH_DATE,IFNULL(A.FINISH_DATE,NOW())))");
		wsb.append("-A.HANGUP_TIME_COUNT*60");
		wsb.append(" BETWEEN A.ORDER_LIMIT_TIME*3600-");
		String willTimeoutMinute = pubFunc.querySysContolSwitchNew("willTimeoutMinute");
		int wtm = 120;
		if (!"".equals(willTimeoutMinute)) {
			try {
				wtm = Integer.parseInt(willTimeoutMinute);
			} catch (Exception e) {
				log.error("querySysContolSwitchNew(willTimeoutMinute) error: {}", e.getMessage());
			}
		}
		wsb.append(wtm * 60);
		wsb.append(" AND A.ORDER_LIMIT_TIME*3600");
		int willTimeout = getMySheetCount(wsb.toString());// 即将超时工单

		StringBuilder tsb = new StringBuilder();
		tsb.append(" AND W.SHEET_STATU NOT IN(720130035,700000046)");
		tsb.append("AND A.ORDER_LIMIT_TIME>0");
		tsb.append(" AND TIMESTAMPDIFF(SECOND,A.ACCEPT_DATE,IFNULL(L.DX_FINISH_DATE,IFNULL(A.FINISH_DATE,NOW())))");
		tsb.append("-A.HANGUP_TIME_COUNT*60");
		tsb.append(">A.ORDER_LIMIT_TIME*3600");
		int timeout = getMySheetCount(tsb.toString());// 已超时工单

		String hWhere = " AND W.SHEET_STATU NOT IN(720130035,700000046)AND(SELECT COUNT(1)FROM CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID)>=1";
		int hasten = getMySheetCount(hWhere);// 催单工单

		String bWhere = " AND W.SHEET_STATU NOT IN(720130035,700000046)AND D.BEST_ORDER>100122410";
		int bestOrder = getMySheetCount(bWhere);// 最严工单

		String uWhere = " AND W.SHEET_STATU NOT IN('720130035','700000046')AND IFNULL(L.UP_TENDENCY_FLAG,0)>0";
		int upTendency = getMySheetCount(uWhere);// 越级倾向工单

		mapForSheet.put("pending", pending);
		mapForSheet.put("willTimeout", willTimeout);
		mapForSheet.put("timeout", timeout);
		mapForSheet.put("hasten", hasten);
		mapForSheet.put("bestOrder", bestOrder);
		mapForSheet.put("upTendency", upTendency);
		return mapForSheet;
	}

	private int getMySheetCount(String where) {
		GridDataInfo gdi = dbgridDataFaceService.getGridDataBySize(DbgridStatic.GRID_FUNID_TS_MYSHEET, where, 1, 10);
		return gdi.getQuryCount();
	}

    @SuppressWarnings({ "all" })
    @Override
    public List getViewStaff(String staffId, String queryType) {
        List list = new ArrayList();
        String viewMonth = "select C_A,C_A_J,L_A_J,C_L_C,L_L_C,C_L_S,L_L_S,C_L_TS,C_L_TS_S,L_L_TS_S,C_L_TS_S_MY,C_L_TS_S_BMY,L_L_TS_S_MY,DEAL_STAFF from cc_view_month_cur where DEAL_STAFF = ?";
        List<Map<String, Object>> viewMonthList = this.jt.queryForList(viewMonth, staffId);
        String viewStaff = "select RANKING_RANGE,RANKING_TYPE,LAST_RANKING,CUR_RANKING from cc_view_staff_ranking where DEAL_STAFF = ?";
        List<Map<String, Object>> viewStaffList = this.jt.queryForList(viewStaff, staffId);

        if (!viewMonthList.isEmpty() && !viewStaffList.isEmpty()) {
            for (int i = 0; i < viewStaffList.size(); i++) {
                Map viewMonthMap = viewMonthList.get(0);
                String C_A = this.getStringByKey(viewMonthMap, "C_A");//工单总量
                String C_A_J = this.getStringByKey(viewMonthMap, "C_A_J");//工单总集约量
                String L_A_J = this.getStringByKey(viewMonthMap, "L_A_J");//工单总集约率
                String C_L_C = this.getStringByKey(viewMonthMap, "C_L_C");//最后处理重复量
                String L_L_C = this.getStringByKey(viewMonthMap, "L_L_C");//最后处理重复率
                String C_L_S = this.getStringByKey(viewMonthMap, "C_L_S");//最后处理申诉转化量
                String L_L_S = this.getStringByKey(viewMonthMap, "L_L_S");//最后处理申诉转化率
                String C_L_TS_S = this.getStringByKey(viewMonthMap, "C_L_TS_S");//最后处理投诉送评量
                String L_L_TS_S = this.getStringByKey(viewMonthMap, "L_L_TS_S");//最后处理投诉送评率
                String C_L_TS_S_MY = this.getStringByKey(viewMonthMap, "C_L_TS_S_MY");//最后处理投诉送评满意量
                String L_L_TS_S_MY = this.getStringByKey(viewMonthMap, "L_L_TS_S_MY");//最后处理投诉送评满意率
                Map<String, Object> mapForViewStaff = new HashMap();
                Map viewStaffMap = viewStaffList.get(i);
                int rankingRange = this.getIntByKeyView(viewStaffMap, "RANKING_RANGE");//排名范围
                int rankingType = this.getIntByKeyView(viewStaffMap, "RANKING_TYPE");//指标类型
                int lastRanking = this.getIntByKeyView(viewStaffMap, "LAST_RANKING");//上次排名值
                int curRanking = this.getIntByKeyView(viewStaffMap, "CUR_RANKING");//本次排名值
                String curRankingLog = "";
                if (lastRanking == curRanking){
                    curRankingLog = "2";
                }else {
                    curRankingLog = lastRanking < curRanking ? "1" : "0";
                }
                mapForViewStaff.put("rankingType", rankingType);
                mapForViewStaff.put("rankingClassify", rankingType < 7 ? 0 : 1);
                mapForViewStaff.put("rankingRange", rankingRange);
                mapForViewStaff.put("curRanking", curRanking);
                mapForViewStaff.put("curRankingLog", curRankingLog);
                switch (rankingType) {
                    case 1:
                        mapForViewStaff.put("rankingValue", C_A);
                        mapForViewStaff.put("rankingName", "工单总量");
                        break;
                    case 2:
                        mapForViewStaff.put("rankingValue", C_A_J);
                        mapForViewStaff.put("rankingName", "集约量");
                        break;
                    case 3:
                        mapForViewStaff.put("rankingValue", C_L_C);
                        mapForViewStaff.put("rankingName", "重复量");
                        break;
                    case 4:
                        mapForViewStaff.put("rankingValue", C_L_S);
                        mapForViewStaff.put("rankingName", "申诉转化量");
                        break;
                    case 5:
                        mapForViewStaff.put("rankingValue", C_L_TS_S);
                        mapForViewStaff.put("rankingName", "送评量");
                        break;
                    case 6:
                        mapForViewStaff.put("rankingValue", C_L_TS_S_MY);
                        mapForViewStaff.put("rankingName", "满意量");
                        break;
                    case 7:
                        mapForViewStaff.put("rankingValue", L_A_J);
                        mapForViewStaff.put("rankingName", "集约率");
                        break;
                    case 8:
                        mapForViewStaff.put("rankingValue", L_L_C);
                        mapForViewStaff.put("rankingName", "重复率");
                        break;
                    case 9:
                        mapForViewStaff.put("rankingValue", L_L_S);
                        mapForViewStaff.put("rankingName", "申诉转化率");
                        break;
                    case 10:
                        mapForViewStaff.put("rankingValue", L_L_TS_S);
                        mapForViewStaff.put("rankingName", "送评率");
                        break;
                    case 11:
                        mapForViewStaff.put("rankingValue", L_L_TS_S_MY);
                        mapForViewStaff.put("rankingName", "满意率");
                        break;
                }
                list.add(mapForViewStaff);
            }
        }
        return list;
    }

	@SuppressWarnings("rawtypes")
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}

	@SuppressWarnings("rawtypes")
	private int getIntByKeyView(Map map, String key) {
		return map.get(key) == null ? 99999 : Integer.parseInt(map.get(key).toString());
	}
}