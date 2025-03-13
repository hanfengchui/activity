/*
 * 说明：提供操作 CCS.CC_STAFF_WORKLOAD 表相关的服务
 * 时间： 2011-10-9
 * 作者：LiJiahui
 * 操作：新增
 */
package com.timesontransfar.staffSkill.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.staffSkill.FlowToEnd;
import com.timesontransfar.staffSkill.StaffWorkloadInfo;
import com.timesontransfar.staffSkill.StaffWorkloadInfoRmp;
import com.timesontransfar.staffSkill.dao.IFlowToEndDao;
import com.timesontransfar.staffSkill.dao.IStaffWorkloadDao;
import com.timesontransfar.staffSkill.service.IStaffWorkloadService;

/**
 * 实现类，提供操作 CCS.CC_STAFF_WORKLOAD 表相关的服务
 * 
 * @author LiJiahui
 * @date 2011-10-09
 */
@Component(value="staffWorkloadService")
public class StaffWorkloadServiceImpl implements IStaffWorkloadService {

    /**
     * 日志实例
     */
    private static final Logger log = LoggerFactory.getLogger(StaffWorkloadServiceImpl.class);

    /**
     * 数据库操作实例，由Spring注入
     */
    @Autowired
    private JdbcTemplate jt;

    /**
     * CC_STAFF_WORKLOAD的操作实例
     */
    @Autowired
    private IStaffWorkloadDao staffWorkloadDao;
   
    @Autowired
    private IFlowToEndDao flowToEndDao;


    public StaffWorkloadInfo queryInWork(int staffId) {
        String where = " AND THRESHOLD > 0" + " AND NOW() BETWEEN START_MOMENT AND END_MOMENT"
                + " AND STATE <> 1" + " AND STAFF_ID = " + staffId;
        try {
            return staffWorkloadDao.queryByWhere(where);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

	public StaffWorkloadInfo queryInOrAfterWork(int staffId) {
		StaffWorkloadInfo swi = queryInWork(staffId);
		if (null != swi) {// 当前班表
			return swi;
		}
		try {
			return staffWorkloadDao.selectStaffWorkloadInfoAfterByStaffId(staffId);// 未来班表
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map queryStaffWorkloadRepeat(int skillId, OrderAskInfo order, SheetPubInfo sheet) {
		Map map = new HashMap();
		// 判断当前被分派的工单为“投诉”类型工单时
		if (720130000 != order.getServType()) {
			return map;
		}
		String sql = 
"SELECT B.*, C.ORG_ID\n" +
"  FROM CC_STAFF_SKILL A, CC_STAFF_WORKLOAD B, TSM_STAFF C\n" + 
" WHERE A.STAFF_ID = B.STAFF_ID\n" + 
"   AND B.STAFF_ID = C.STAFF_ID\n" + 
"   AND A.FLOW_ORG_ID = ?\n" + 
"   AND A.SKILL_ID = ?\n" + 
"   AND A.TACHE_ID = ?\n" + 
"   AND A.SERVICE_DATE = ?\n" + 
"   AND A.SKILL_STATE = 1\n" + 
"   AND B.STATE = 0\n" + 
"   AND B.THRESHOLD > 0\n" + 
"   AND C.ORG_ID IN (SELECT D.APPOINT_ORG_ID FROM CC_STAFF_WORKLOAD_REPEAT D)\n" + 
"   AND NOW() BETWEEN B.START_MOMENT AND B.END_MOMENT";
		try {
			String flowOrgId = sheet.getRcvOrgId();
			int tacheId = sheet.getTacheId();
			String serviceDate = String.valueOf(order.getServiceDate());
			// 当前投诉单的分派员工包含配置表指定的部门员工
			List<StaffWorkloadInfo> swls = jt.query(sql, new Object[] { flowOrgId, skillId, tacheId, serviceDate }, new StaffWorkloadInfoRmp());
			if (!swls.isEmpty()) {
				String newOrderId = order.getServOrderId();
				String pNum = order.getProdNum();
				String rInfo = order.getRelaInfo();
				int rgId = order.getRegionId();
				sql = 
"SELECT SERVICE_ORDER_ID, HIS_FLAG\n" +
"  FROM (SELECT A.SERVICE_ORDER_ID, A.ACCEPT_DATE, 0 HIS_FLAG\n" + 
"          FROM CC_SERVICE_ORDER_ASK A, CC_SERVICE_CONTENT_ASK B\n" + 
"         WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" + 
"           AND A.ORDER_STATU != 720130001\n" + 
"           AND A.ACCEPT_DATE > DATE_SUB(NOW(), INTERVAL 30 DAY)\n" + 
"           AND A.SERVICE_TYPE = 720130000\n" + 
"           AND A.REGION_ID = ?\n" + 
"           AND (A.PROD_NUM IN (?, ?) OR A.RELA_INFO IN (?, ?))\n" + 
"           AND A.SERVICE_ORDER_ID <> ?\n" + 
"        UNION\n" + 
"        SELECT A.SERVICE_ORDER_ID, A.ACCEPT_DATE, 1\n" + 
"          FROM CC_SERVICE_ORDER_ASK_HIS A, CC_SERVICE_CONTENT_ASK_HIS B\n" + 
"         WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n" + 
"           AND A.ORDER_VESION = B.ORDER_VESION\n" + 
"           AND A.ORDER_STATU = 720130010\n" + 
"           AND A.ACCEPT_DATE > DATE_SUB(NOW(), INTERVAL 30 DAY)\n" + 
"           AND A.SERVICE_TYPE = 720130000\n" + 
"           AND A.REGION_ID = ?\n" + 
"           AND (A.PROD_NUM IN (?, ?) OR A.RELA_INFO IN (?, ?))\n" + 
"           AND A.SERVICE_ORDER_ID <> ?) C\n" + 
" ORDER BY ACCEPT_DATE DESC LIMIT 1";
				// 根据当前单（产品号码+联系号码）关联最近30天内单（产品号码+联系号码）是否有投诉工单记录（历史、在途的投诉单）
				List oList = jt.queryForList(sql, rgId, pNum, rInfo, pNum, rInfo, newOrderId, rgId, pNum, rInfo, pNum, rInfo, newOrderId);
				if (!oList.isEmpty()) {
					Map oMap = (Map) oList.get(0);
					String newSheetId = sheet.getWorkSheetId();
					String oldOrderId = oMap.get("SERVICE_ORDER_ID").toString();
					String hisFlag = oMap.get("HIS_FLAG").toString();
					map = getStaffWorkloadRepeat(newOrderId, newSheetId, swls, oldOrderId, hisFlag);
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return map;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getStaffWorkloadRepeat(String newOrderId, String newSheetId, List<StaffWorkloadInfo> swls, String oldOrderId, String hisFlag) {
		Map map = new HashMap();
		String lastDealOrgId = "";
		int lastYdxStaffId = 0;
		int isAllot = 0; // 是否符合自动分派到指定人规则（否）
		String sql = 
"SELECT DEAL_ORG_ID,\n" +
"       (SELECT IFNULL(DEAL_STAFF, RECEIVE_STAFF)\n" + 
"          FROM CC_WORK_SHEET_HIS B\n" + 
"         WHERE SHEET_TYPE = 720130016\n" + 
"           AND B.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID\n" + 
"           AND B.CREAT_DATE > A.CREAT_DATE\n" + 
"         ORDER BY CREAT_DATE DESC LIMIT 1) YDX_STAFF_ID\n" + 
"  FROM CC_WORK_SHEET_HIS A\n" + 
" WHERE SHEET_TYPE IN (720130013, 720130014)\n" + 
"   AND DEAL_CONTENT <> '系统自动处理'\n" + 
"   AND RESPOND_DATE IS NOT NULL\n" + 
"   AND SERVICE_ORDER_ID = ?\n" + 
" ORDER BY RESPOND_DATE DESC";
		if ("0".equals(hisFlag)) {
			sql = 
"SELECT DEAL_ORG_ID,\n" +
"       (SELECT IFNULL(DEAL_STAFF, RECEIVE_STAFF)\n" + 
"          FROM CC_WORK_SHEET B\n" + 
"         WHERE SHEET_TYPE = 720130016\n" + 
"           AND B.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID\n" + 
"           AND B.CREAT_DATE > A.CREAT_DATE\n" + 
"         ORDER BY CREAT_DATE DESC LIMIT 1) YDX_STAFF_ID\n" + 
"  FROM CC_WORK_SHEET A\n" + 
" WHERE SHEET_TYPE IN (720130013, 720130014)\n" + 
"   AND DEAL_CONTENT <> '系统自动处理'\n" + 
"   AND RESPOND_DATE IS NOT NULL\n" + 
"   AND SERVICE_ORDER_ID = ?\n" + 
" ORDER BY RESPOND_DATE DESC";
		}
		List dList = jt.queryForList(sql, oldOrderId);
		// 前一个投诉重复工单的最后一个处理部门、预定性人工号
		if (!dList.isEmpty()) {
			Map dMap = (Map) dList.get(0);
			lastDealOrgId = dMap.get("DEAL_ORG_ID").toString();
			lastYdxStaffId = dMap.get("YDX_STAFF_ID") == null ? 0 : Integer.parseInt(dMap.get("YDX_STAFF_ID").toString());
			if (0 != lastYdxStaffId) {
				for (StaffWorkloadInfo swl : swls) {
					// 判断前一个投诉重复工单的最后一个处理部门是否是配置的指定处理部门&&前工单的预定性人在分派列表里面
					if (swl.getOrgId().equals(lastDealOrgId) && swl.getStaffId() == lastYdxStaffId) {
						map.put("info", swl);
						map.put("apportion", "2");
						isAllot = 1;
						break;
					}
				}
			}
		}
		String saveSql = "INSERT INTO cc_one_order_end(creat_date,new_order_id,new_sheet_id,old_order_id,old_deal_org,old_ydx_staff,is_allot)"
				+ "VALUES(NOW(),?,?,?,?,?,?)";
		jt.update(saveSql, newOrderId, newSheetId, oldOrderId, lastDealOrgId, lastYdxStaffId, isAllot);
		return map;
	}

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Map getStaffWorkload(String flowOrg, int skillId, int tacheId, String serviceDate,int serviceType) {
        StaffWorkloadInfo info = new StaffWorkloadInfo();
        Map map = new HashMap();
        List listForApportion = new ArrayList();
        try {
            if(serviceType == 720200003 || serviceType == 720130000){
                String sql = "SELECT B.* FROM CC_STAFF_SKILL A, CC_STAFF_WORKLOAD B,cc_order_apportion C "
                        + " WHERE A.STAFF_ID = B.STAFF_ID and A.STAFF_ID = C.STAFF_ID  AND A.FLOW_ORG_ID = ?"
                        + " AND A.SKILL_ID = ? AND A.TACHE_ID = ? AND A.SERVICE_DATE = ?"
                        + " AND A.SKILL_STATE = 1 AND B.STATE = 0 AND B.THRESHOLD > 0"
                        + " AND NOW() BETWEEN B.START_MOMENT AND B.END_MOMENT AND C.APPORTION_STATUS = 0 AND C.APPORTION_NUMBER > C.REAL_APPORTION_NUMBER "
                        + "ORDER BY C.APPORTION_DATE LIMIT 1";
                listForApportion = jt.query(sql, new Object[]{flowOrg, skillId, tacheId, serviceDate}, new StaffWorkloadInfoRmp());
            }
            if(!listForApportion.isEmpty()){
                info = (StaffWorkloadInfo) listForApportion.get(0);
                map.put("info",info);
                map.put("apportion","1");
            }else {
                String sql1 = "SELECT * FROM (" + " SELECT B.* FROM CC_STAFF_SKILL A, CC_STAFF_WORKLOAD B"
                        + " WHERE A.STAFF_ID = B.STAFF_ID" + " AND A.FLOW_ORG_ID = ?"
                        + " AND A.SKILL_ID = ?" + " AND A.TACHE_ID = ? AND A.SERVICE_DATE = ?"
                        + " AND A.SKILL_STATE = 1 AND B.STATE = 0" + " AND B.THRESHOLD > 0"
                        + " AND B.CUR_WORKLOAD < B.THRESHOLD"
                        + " AND NOW() BETWEEN B.START_MOMENT AND B.END_MOMENT"
                        + " ORDER BY B.CUR_RATE) AS RT LIMIT 1";
                List list = jt.query(sql1, new Object[]{flowOrg, skillId, tacheId, serviceDate}, new StaffWorkloadInfoRmp());
                if (!list.isEmpty()){
                    info =  (StaffWorkloadInfo) list.get(0);
                    map.put("info",info);
                    map.put("apportion","0");
                }
            }
        } catch (Exception e) {
        	log.error("getStaffWorkload error: {}", e.getMessage(), e);
        }
        return map;
    }

    @Override
    public int updateApportion(String staffId) {
        String sql = "UPDATE cc_order_apportion \n" +
                "SET REAL_APPORTION_NUMBER = REAL_APPORTION_NUMBER + 1,\n" +
                "MODIFY_DATE = now(),\n" +
                "APPORTION_STATUS = (case when APPORTION_NUMBER >= REAL_APPORTION_NUMBER+1  then 0 else 1 end)\n" +
                "where APPORTION_NUMBER > REAL_APPORTION_NUMBER and STAFF_ID = ?\n";
        return jt.update(sql,staffId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.timesontransfar.staffSkill.service.IStaffWorkloadService#allotWork
     * (com.timesontransfar.staffSkill.StaffWorkloadInfo, boolean)
     */
    public int allotWork(StaffWorkloadInfo info, boolean flag) {
        if (null == info) {
            return -1;
        }

        if (flag) {
            // 当前完成总量
            int curWorkload = info.getCurWorkload() + 1;
            info.setCurWorkload(curWorkload);

            // 阀值
            int threshold = info.getThreshold();
            info.setCurRate((double) curWorkload / threshold);
        }
        return staffWorkloadDao.updateStaffWorkload(info);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.timesontransfar.staffSkill.service.IStaffWorkloadService#setTime(
     * java.lang.String, java.lang.String, java.lang.String,
     * com.timesontransfar.staffSkill.StaffWorkloadInfo)
     */
    public void setTime(String day, String startTime, String endTime, StaffWorkloadInfo workload) {
        String start = day + " " + startTime;
        String end = day + " " + endTime;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date;

        try {
            date = sdf.parse(end);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) - 30);
            if (startTime.compareToIgnoreCase(endTime) > 0
                    && "00:30".compareToIgnoreCase(endTime) >= 0) {
                calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) + 1);
            }
            end = sdf.format(calendar.getTime());
            workload.setStartMoment(start);
            workload.setEndMoment(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

	/*
	 * 根据ID和类型查询一跟到底配置存不存在，keyType：1、生效单位，2、生效渠道，3、不生效的号码
	 */
	public int checkFlowToEndConfigByIdType(String keyId, int keyType) {
		if (flowToEndDao.countFlowToEndConfigByIdType(keyId, keyType) > 0) {
			return 1;
		}
		return 0;
	}

	/**
	 * 1、时间范围：近30日； 3、重复逻辑：优先用产品号码比对，若没有再用联系电话比对；4、工单自动归集。按时间靠近的在库人员或办结人员自动跳入该员工库里。优先派至在途人员库中，无在途则派至办结人员库中（不判断是否在班）。
	 */
	public FlowToEnd getFlowToEnd30Day(OrderAskInfo askInfo, String orgPlace) {
		int regionId = askInfo.getRegionId();
		String prodNum = askInfo.getProdNum();
		String relaInfo = askInfo.getRelaInfo();
		FlowToEnd fte = new FlowToEnd();
		fte = flowToEndDao.selectFlowToEndByProdNum(regionId, prodNum, orgPlace);// 产品号码当前30天
		if (null == fte) {
			fte = flowToEndDao.selectFlowToEndDxhfByProdNum(regionId, prodNum, orgPlace);// 产品号码定性回访30天
			if (null == fte) {
				fte = flowToEndDao.selectFlowToEndHisByProdNum(regionId, prodNum, orgPlace);// 产品号码历史30天
				if (null == fte) {
					fte = flowToEndDao.selectFlowToEndByRelaInfo(regionId, relaInfo, orgPlace);// 联系号码当前30天
					if (null == fte) {
						fte = flowToEndDao.selectFlowToEndDxhfByRelaInfo(regionId, relaInfo, orgPlace);// 联系号码定性回访30天
						if (null == fte) {
							fte = flowToEndDao.selectFlowToEndHisByRelaInfo(regionId, relaInfo, orgPlace);// 联系号码历史30天
						}
					}
				}
			}
		}
		return fte;
	}

	/**
	 * 判断之前处理员工是否长休假
	 */
	public int checkDealStaffIdIsRest(int dealStaffId) {
		if (flowToEndDao.countFlowToEndRestConfigByDealStaffId(dealStaffId) > 0) {
			return 1;
		}
		return 0;
	}

	/*
	 * 由30天匹配后初始插入记录
	 */
	public int saveFlowToEnd(FlowToEnd fte) {
		if ("".equals(fte.getCountWorkloadGuid())) {// 未关联到工作量情况
			return flowToEndDao.insertFlowToEndEmptyWorkload(fte);
		} else {// 关联到工作量情况
			return flowToEndDao.insertFlowToEndWithWorkload(fte);
		}
	}

	/*
	 * 根据30天内员工Id查询未更新工作量情况表的记录
	 */
	public List<FlowToEnd> getEmptyWorkloadByDealStaffId(int dealStaffId) {
		return flowToEndDao.selectFlowToEndEmptyWorkloadByDealStaffId(dealStaffId);
	}

	/*
	 * 更新工作量情况表的记录
	 */
	public int setWorkloadByIncrementId(String countWorkloadGuid, int incrementId) {
		return flowToEndDao.updateFlowToEndWorkloadByIncrementId(countWorkloadGuid, incrementId);
	}
}