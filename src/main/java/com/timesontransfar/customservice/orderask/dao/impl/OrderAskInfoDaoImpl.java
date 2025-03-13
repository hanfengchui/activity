package com.timesontransfar.customservice.orderask.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.CallSummary;
import com.timesontransfar.customservice.orderask.pojo.CallSummaryOrder;
import com.timesontransfar.customservice.orderask.pojo.CallSummaryRmp;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfoRmp;
import com.timesontransfar.customservice.orderask.pojo.PreOrderResult;
import com.timesontransfar.customservice.orderask.pojo.YNSJOrder;
import com.timesontransfar.customservice.orderask.pojo.YNSJOrderRmp;
import com.timesontransfar.customservice.orderask.pojo.YNSJResult;
import com.timesontransfar.feign.custominterface.PortalInterfaceFeign;
import com.transfar.common.web.ResultUtil;

@SuppressWarnings({"rawtypes", "unchecked"})
public class OrderAskInfoDaoImpl implements IorderAskInfoDao {

    private static final Logger log = LoggerFactory.getLogger(OrderAskInfoDaoImpl.class);
    @Autowired
    private JdbcTemplate jt;
    @Autowired
    @Qualifier("pubJdbcTemplate")
    private JdbcTemplate pubjt;
    @Autowired
    private IdbgridDataPub dbgridDataPub;
    
    @Autowired
	private PortalInterfaceFeign portalFeign;

    private String queryAskInfoByIdMonthHisSql;
    private String queryAskInfoByIdMonthSql;
    private String saveServOrderSql;
    private String saveServOrderHisByOrderIdSql;
    private String queryOrderAskInfoByStatuSql;
    private String queryOrderAskInfoByStatuHisSql;
    private String queryOrderAskInfoSql;
    private String queryOrderAskInfoHisSql;
    private String delOrderAskInfoSql;
    private String updateOrderStatuSql;
    private String updateSubSheetHoldInfoSql;
    private String updateOrderAskInfoSql;
    private String queryOrderAskInfoByAlarmSql;
    private String updateOrderHisStatuSql;
    private String updateAcceptDateSql;
    private String selectAcceptDateSql;
    private String updateOrderLimitTimeSql;
    private String insertServiceWisdomTypeSql;
    private String insertServiceWisdomTypeHisSql;
    private String deleteServiceWisdomTypeSql;
    private String updateServiceWisdomTypeSql;
    private String insertServiceTrackSql;

    /**
     * 保存订单跟踪信息
     *
     * @return 保存成功记录数
     */
    public int saveServiceTrack(String newOrderId, String oldOrderId, int trackType) {
        return jt.update(this.insertServiceTrackSql, newOrderId, oldOrderId, trackType);
    }

    public int saveOrderAskInfo(OrderAskInfo order) {
        String strSql = this.saveServOrderSql;
        return jt.update(strSql,
                StringUtils.defaultIfEmpty(order.getServOrderId(), null),
                order.getOrderVer(),
                order.getRegionId(),
                StringUtils.defaultIfEmpty(order.getRegionName(), null),
                order.getServType(),
                StringUtils.defaultIfEmpty(order.getServTypeDesc(), null),
                StringUtils.defaultIfEmpty(order.getCallSerialNo(), null),
                StringUtils.defaultIfEmpty(order.getSourceNum(), null),
                StringUtils.defaultIfEmpty(order.getCustId(), null),
                StringUtils.defaultIfEmpty(order.getRelaMan(), null),
                order.getRelaType(),
                StringUtils.defaultIfEmpty(order.getProdNum(),"0"),
                StringUtils.defaultIfEmpty(order.getRelaInfo(), "0"),
                order.getAskChannelId(),
                StringUtils.defaultIfEmpty(order.getAskChannelDesc(), null),
                order.getAskSource(),
                StringUtils.defaultIfEmpty(order.getAskSourceDesc(), null),
                order.getAskStaffId(),
                StringUtils.defaultIfEmpty(order.getAskStaffName(), null),
                StringUtils.defaultIfEmpty(order.getAskOrgId(), null),
                StringUtils.defaultIfEmpty(order.getAskOrgName(), null),
                order.getCustServGrade(),
                StringUtils.defaultIfEmpty(order.getCustServGradeDesc(), null),
                order.getUrgencyGrade(),
                StringUtils.defaultIfEmpty(order.getUrgencyGradeDesc(), null),
                order.getCustEmotion(),
                StringUtils.defaultIfEmpty(order.getCustEmotionDesc(), null),
                order.getAskCount(),
                order.getOrderStatu(),
                order.getOrderLimitTime(),
                order.getSubSheetCount(),
                order.getHangTimeSum(),
                order.getPreAlarmValue(),
                order.getAlarmValue(),
                StringUtils.defaultIfEmpty(order.getVerifyReasonId(), null),
                StringUtils.defaultIfEmpty(order.getVerifyReasonName(), null),
                StringUtils.defaultIfEmpty(order.getReplyContent(), null),
                StringUtils.defaultIfEmpty(order.getComment(), null),
                order.getNetFlag(),
                order.getMonth(),
                StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(order.getModifyDate()), null),
                StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(order.getFinishDate()), null),
                StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(order.getBookingDate()), null),
                StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(order.getHangStartTime()), null),
                order.getCustGroup(),
                StringUtils.defaultIfEmpty(order.getCustGroupDesc(), null),
                order.getServiceDate(),
                StringUtils.defaultIfEmpty(order.getServiceDateDesc(), null),
                StringUtils.defaultIfEmpty(order.getOrderStatuDesc(), null),
                StringUtils.defaultIfEmpty(order.getAssistSellNo(), null),
                order.getYsQualiativeId(),
                StringUtils.defaultIfEmpty(order.getYsQualiativeName(), null),
                order.getTsKeyWord(),
                StringUtils.defaultIfEmpty(order.getTsKeyWordDesc(), null),
                order.getComeCategory(),
                StringUtils.defaultIfEmpty(order.getCategoryName(), null),
                order.getAreaId(),
                StringUtils.defaultIfEmpty(order.getAreaName(), null),
                order.getSubStationId(),
                StringUtils.defaultIfEmpty(order.getSubStationName(), null),
                StringUtils.defaultIfEmpty(order.getSendToOrgId(), null),
                StringUtils.defaultIfEmpty(order.getSendToOrgName(), null),
                StringUtils.defaultIfEmpty(order.getProductType(), null),
                StringUtils.defaultIfEmpty(order.getProductTypeName(), null),
                StringUtils.defaultIfEmpty(order.getNodeId(), null),
                StringUtils.defaultIfEmpty(order.getServiceKey(), null),
                order.getChannelDetailId(),
                StringUtils.defaultIfEmpty(order.getChannelDetailDesc(), null),
                order.getIsOwner(),
                StringUtils.defaultIfEmpty(order.getMoreRelaInfo(), null)
        );
    }

    /**
     * 将当前的定单信息保存到历史表
     *
     * @param currentOrderId 当前表的定单id
     * @return 保存的记录数
     */
    public int saveOrderAskInfoHis(String currentOrderId, Integer month) {
        /*
         * INSERT INTO CC_SERVICE_ORDER_ASK_HIS A SELECT * FROM
         * CC_SERVICE_ORDER_ASK B WHERE B.SERVICE_ORDER_ID = ?
         */
        return jt.update(this.saveServOrderHisByOrderIdSql, currentOrderId, month);
    }

    /**
     * 根据受理单号和地域ID查询到此单的受理信息
     *
     * @param orderId  受理单号
     * @param regionId
     * @param hisFlag  是否操作的是历史信息
     * @return 受理单受理信息
     */
    public OrderAskInfo getOrderAskObj(String orderId, Integer month, boolean hisFlag) {
        String strsql = "";
        if (hisFlag) {
            strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK_HIS A WHERE A.SERVICE_ORDER_ID = ?";
        } else {
            strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK A WHERE A.SERVICE_ORDER_ID = ?";
        }

        List tmpList = jt.query(strsql, new Object[]{orderId}, new OrderAskInfoRmp());
        if (tmpList.isEmpty()) {
            log.debug("没有查询到受理单号为:" + orderId + "的受理信息,请查证单号是否正确!");
            tmpList.clear();
            tmpList = null;
            return null;
        }
        OrderAskInfo orderAskInfo = (OrderAskInfo) tmpList.get(0);
        tmpList.clear();
        tmpList = null;
        return orderAskInfo;
    }

	/**
	 * 查询受理时间90天前的订单
	 */
	public OrderAskInfo getOrderAskOver180Day(String orderId) {
		String strsql = "SELECT * FROM cc_service_order_ask WHERE service_order_id = ? AND TIMESTAMPDIFF(DAY, accept_date, NOW()) > 90";
		List tmpList = jt.query(strsql, new Object[] { orderId }, new OrderAskInfoRmp());
		if (tmpList.isEmpty()) {
			tmpList.clear();
			tmpList = null;
			return null;
		}
		OrderAskInfo orderAskInfo = (OrderAskInfo) tmpList.get(0);
		tmpList.clear();
		tmpList = null;
		return orderAskInfo;
	}

    /**
     * 根据受理单号查询到此单的受理信息（工单打印使用）
     *
     * @param orderId 受理单号
     * @param hisFlag 是否操作的是历史信息
     * @return 受理单受理信息
     */
    public OrderAskInfo getOrderAskObjNew(String orderId, boolean hisFlag, String statu) {
        String strsql = "";
        if (hisFlag) {
            strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK_HIS A WHERE A.SERVICE_ORDER_ID = ?";
        } else {
            strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK A WHERE A.SERVICE_ORDER_ID = ?";
        }
        if (!"".equals(statu)) {
            int tmp = 0;
            try {
                tmp = Integer.parseInt(statu);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            strsql += " AND A.ORDER_STATU = " + tmp;
        }

        List tmpList = jt.query(strsql, new Object[]{orderId}, new OrderAskInfoRmp());
        if (tmpList.isEmpty()) {
            log.debug("没有查询到受理单号为:" + orderId + "的受理信息,请查证单号是否正确!");
            tmpList.clear();
            tmpList = null;
            return null;
        }
        OrderAskInfo orderAskInfo = (OrderAskInfo) tmpList.get(0);
        tmpList.clear();
        tmpList = null;
        return orderAskInfo;
    }

    /**
     * 根据受理单号和地域ID查询到此单的受理信息
     *
     * @param orderId  受理单号
     * @param regionId
     * @param hisFlag  是否操作的是历史信息
     * @return 受理单受理信息
     */
    public OrderAskInfo getOrderAskObjNew(String orderId, Integer month, String suffix, boolean hisFlag) {
        // SELECT * FROM cc_service_order_ask a WHERE a.service_order_id = ?
        //SELECT * FROM cc_service_order_ask_his a WHERE a.service_order_id = ?
        String strsql = "";
        if (suffix.length() > 0) {
            strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK_HIS_BAK A WHERE   A.SERVICE_ORDER_ID = ?";
        } else {
            if (hisFlag) {
                strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK_HIS A WHERE   A.SERVICE_ORDER_ID = ?";

            } else {
                strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK A WHERE A.SERVICE_ORDER_ID = ?";

            }
        }
        List tmpList = jt.query(strsql,
                new Object[]{orderId}, new OrderAskInfoRmp());
        if (tmpList.isEmpty()) {
            log.debug("没有查询到受理单号为:" + orderId + "的受理信息,请查证单号是否正确!");
            tmpList.clear();
            tmpList = null;
            return null;
        }
        OrderAskInfo orderAskInfo = (OrderAskInfo) tmpList.get(0);
        tmpList.clear();
        tmpList = null;
        return orderAskInfo;
    }

    /**
     * 根据受理单id客户CUSTID分区标志地域ID获取受理单
     *
     * @param serOrdId serviceOrderID
     * @param monthFlg 分区标志
     * @param regionId 地域ID
     * @param hisFlag  当前、历史标识
     * @return 受理单对象
     */
    public OrderAskInfo getOrderAskInfoByIdMonth(String serOrdId,
                                                 Integer monthFlg, String regionId, boolean hisFlag, int vesion) {
        //SELECT * FROM CC_SERVICE_ORDER_ASK A WHERE A.SERVICE_ORDER_ID = ? AND A.MONTH_FLAG = ? AND A.REGION_ID = ?
        //SELECT * FROM CC_SERVICE_ORDER_ASK_HIS A WHERE A.SERVICE_ORDER_ID = ? AND A.MONTH_FLAG = ? AND A.REGION_ID = ? AND A.ORDER_STATU = 700000103
        String strsql = "";
        List tmpList = null;
        if (hisFlag) {
            strsql = this.queryAskInfoByIdMonthHisSql;
            tmpList = jt.query(strsql,
                    new Object[]{serOrdId, vesion, regionId}, new OrderAskInfoRmp());
        } else {
            strsql = this.queryAskInfoByIdMonthSql;
            tmpList = jt.query(strsql,
                    new Object[]{serOrdId, regionId}, new OrderAskInfoRmp());
        }


        if (tmpList.isEmpty()) {
            log.debug("没有查询到受理单号为:" + serOrdId + "的受理信息,请查证单号是否正确!");
            return null;
        }
        OrderAskInfo orderAskInfo = (OrderAskInfo) tmpList.get(0);
        tmpList.clear();
        tmpList = null;
        return orderAskInfo;
    }

    public OrderAskInfo getOrderAskInfoByIdMonthNew(String serOrdId, String regionId, String suffix, int vesion) {
        //SELECT * FROM CC_SERVICE_ORDER_ASK A WHERE A.SERVICE_ORDER_ID = ? AND A.MONTH_FLAG = ? AND A.REGION_ID = ?
        //AND A.ORDER_VESION = ?
        String tbname = "CC_SERVICE_ORDER_ASK" + suffix;
        String strsql = queryAskInfoByIdMonthSql.replace("CC_SERVICE_ORDER_ASK", tbname);
        List tmpList = null;

        if (suffix.length() > 0) {
            tmpList = jt.query(strsql + " AND A.ORDER_VESION = ? ", new Object[]{serOrdId, regionId, vesion}, new OrderAskInfoRmp());
        } else {
            tmpList = jt.query(strsql, new Object[]{serOrdId, regionId}, new OrderAskInfoRmp());
        }
        if (tmpList.isEmpty()) {
            log.debug("没有查询到受理单号为:" + serOrdId + "的受理信息,请查证单号是否正确!");
            return null;
        }
        OrderAskInfo orderAskInfo = (OrderAskInfo) tmpList.get(0);
        tmpList.clear();
        tmpList = null;
        return orderAskInfo;
    }

    /**
     * 根据受理单id客户CUSTID分区标志地域ID获取受理单
     *
     * @param serOrdId  serviceOrderID
     * @param queryType 0表示当前表不存在时,需要查历史表，1表示只查历史表 2，只查当前表
     * @return 受理单对象
     */
    public OrderAskInfo getErrInfoById(String serOrdId, int queryType) {
        String str1 = "SELECT *  FROM CC_SERVICE_ORDER_ASK A WHERE A.SERVICE_ORDER_ID = ?  ";

        String str2 = "SELECT *  FROM CC_SERVICE_ORDER_ASK_HIS A WHERE A.SERVICE_ORDER_ID = ? ORDER BY A.ORDER_VESION DESC ";
        List tmpList = null;
        if (queryType == 0) {
            tmpList = jt.query(str1, new Object[]{serOrdId}, new OrderAskInfoRmp());
            if (tmpList.isEmpty()) {
                log.debug("当前表没有查询到受理单号为:" + serOrdId + "的受理信息,执行历史表查询!");

                tmpList = jt.query(str2, new Object[]{serOrdId}, new OrderAskInfoRmp());
                if (tmpList.isEmpty()) {
                    log.debug("历史表没有查询到受理单号为:" + serOrdId + "的受理信息!");
                    return null;
                }
            }
        } else if (queryType == 1) {
            String str3 = "SELECT * FROM CC_SERVICE_ORDER_ASK_HIS A WHERE A.SERVICE_ORDER_ID = ? AND A.ORDER_VESION = 0 ";
            tmpList = jt.query(str3, new Object[]{serOrdId}, new OrderAskInfoRmp());
            if (tmpList.isEmpty()) {
                log.debug("历史表没有查询到受理单号为:" + serOrdId + "的受理信息!");
                return null;
            }
        } else if (queryType == 2) {
            tmpList = jt.query(str1, new Object[]{serOrdId}, new OrderAskInfoRmp());
            if (tmpList.isEmpty()) {
                log.debug("当前表没有查询到受理单号为:" + serOrdId + "的受理信息!");
            }
        }
        if(tmpList == null) {
        	return null;
        }
        OrderAskInfo orderAskInfo = (OrderAskInfo) tmpList.get(0);
        tmpList.clear();
        tmpList = null;
        return orderAskInfo;
    }

    /**
     * 根据受理单号查询到此单的受理信息
     *
     * @param orderId 受理单号
     * @param hisFlag 当前、历史标识
     * @return 受理单受理信息
     */
	public OrderAskInfo getOrderAskInfo(String orderId, boolean hisFlag) {
        // SELECT * FROM cc_service_order_ask a WHERE a.service_order_id = ?
        //SELECT * FROM cc_service_order_ask_his a WHERE a.service_order_id = ?
        String strsql = "";
        if (hisFlag) {
            strsql = this.queryOrderAskInfoHisSql;
        } else {
            strsql = this.queryOrderAskInfoSql;
        }

        List tmpList = jt.query(strsql, new Object[]{orderId}, new OrderAskInfoRmp());
        if (tmpList.isEmpty()) {
            log.debug("没有查询到受理单号为:{}的受理信息,请查证单号是否正确!", orderId);
            return null;
        }

        return (OrderAskInfo) tmpList.get(0);
    }

    /**
     * 根据受理单号删除此单的受量信息
     *
     * @param orderId 受理单号
     * @param hisFlag 当前/历史
     * @return 删除的记录条数
     */
    public int delOrderAskInfo(String orderId, Integer month) {
        // DELETE FROM cc_service_order_ask a WHERE a.service_order_id = ?
        return jt.update(this.delOrderAskInfoSql, orderId, month);
    }


    /**
     * 更新定单受理信息的状态信息
     *
     * @param orderId 受理单id
     * @param statu   状态值
     * @return 是否成功
     */
    public boolean updateOrderStatu(String orderId, int statu, Integer month, String orderStatuDesc) {
    	/*  UPDATE CC_SERVICE_ORDER_ASK B SET B.ORDER_STATU =
			?,B.ORDER_STATU_DESC=?,B.MODIFY_DATE=SYSDATE WHERE
			B.SERVICE_ORDER_ID = ?  */
        if (log.isDebugEnabled()) {
            log.debug("updateOrderStatu(String orderId, int statu) --start");
        }
        int count = this.jt.update(this.updateOrderStatuSql, statu, orderStatuDesc, orderId);
        if (count != 1) {
            if (log.isDebugEnabled()) {
                log.debug("更新受理单状态不成功,异常返回!");
                log.debug("受理单号:{} 状态值:{} count:{}", orderId, statu, count);
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("状态更新成功,正常返回");
            log.debug("受理单号:{} 状态值:{} count:{}", orderId, statu, count);
        }
        return true;
    }

    /**
     * 保存定单后，进历史表的定单更新为作废状态
     *
     * @param region  地域
     * @param orderId 受理单ID
     * @param statu   状态值
     * @return
     */
    public boolean updateOrderHisStatu(int region, String orderId, int statu, String statuDesc, Integer month) {
        /*
         * UPDATE CC_SERVICE_ORDER_ASK_HIS B SET B.ORDER_STATU = ?,B.MODIFY_DATE=SYSDATE WHERE
         * B.REGION_ID=? AND B.SERVICE_ORDER_ID = ?
         */
        if (log.isDebugEnabled()) {
            log.debug("updateOrderStatu(String orderId, int statu) --start");
        }
        int count = 0;
        count = this.jt.update(this.updateOrderHisStatuSql, statu, statuDesc, region, orderId, month);
        if (count < 1) {
            if (log.isDebugEnabled()) {
                log.debug("更新历史受理单状态不成功,异常返回!");
                log.debug("受理单号:" + orderId + "状态值:" + statu + "count:" + count);
            }
            return false;
        }
        if (log.isDebugEnabled()) {
            log.debug("历史单状态更新成功,正常返回");
            log.debug("受理单号:" + orderId + "状态值:" + statu + "count:" + count);
        }
        return true;
    }

    /**
     * 保存定单后，进历史表的定单更新为作废状态
     *
     * @param region  地域
     * @param orderId 受理单ID
     * @param statu   状态值
     * @return
     */
    public boolean updateOrderHisStatuByVersion(int region, String orderId, int version, int statu, String statuDesc, Integer month) {
        String strSql = "UPDATE CC_SERVICE_ORDER_ASK_HIS B SET B.ORDER_STATU=?, B.ORDER_STATU_DESC=?, B.MODIFY_DATE=NOW() "
                + "WHERE B.REGION_ID=? AND B.MONTH_FLAG=? AND B.ORDER_VESION=? AND B.SERVICE_ORDER_ID=?";
        int count = this.jt.update(strSql,
                statu, statuDesc, region, month, version, orderId);
        return count > 0;
    }

    /**
     * 更新一条受理单的
     *
     * @param orderAskInfo 受理信息对象
     * @return 更新的记录数
     */
    public int updateOrderAskInfo(OrderAskInfo orderAskInfo) {
        return this.jt.update(this.updateOrderAskInfoSql,
                orderAskInfo.getOrderVer(),
                orderAskInfo.getRegionId(),
                StringUtils.defaultIfEmpty(orderAskInfo.getRegionName(), null),
                orderAskInfo.getServType(),
                StringUtils.defaultIfEmpty(orderAskInfo.getServTypeDesc(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getCallSerialNo(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getSourceNum(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getCustId(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getRelaMan(), null),
                orderAskInfo.getRelaType(),
                StringUtils.defaultIfEmpty(orderAskInfo.getProdNum(),"0"),
                StringUtils.defaultIfEmpty(orderAskInfo.getRelaInfo(), "0"),
                orderAskInfo.getAskChannelId(),
                StringUtils.defaultIfEmpty(orderAskInfo.getAskChannelDesc(), null),
                orderAskInfo.getAskSource(),
                StringUtils.defaultIfEmpty(orderAskInfo.getAskSourceDesc(), null),
                orderAskInfo.getAskStaffId(),
                StringUtils.defaultIfEmpty(orderAskInfo.getAskStaffName(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getAskOrgId(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getAskOrgName(), null),
                StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(orderAskInfo.getAskDate()), null),
                StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(orderAskInfo.getFinishDate()), null),
                orderAskInfo.getCustServGrade(),
                StringUtils.defaultIfEmpty(orderAskInfo.getCustServGradeDesc(), null),
                orderAskInfo.getUrgencyGrade(),
                StringUtils.defaultIfEmpty(orderAskInfo.getUrgencyGradeDesc(), null),
                StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(orderAskInfo.getBookingDate()), null),
                orderAskInfo.getCustEmotion(),
                StringUtils.defaultIfEmpty(orderAskInfo.getCustEmotionDesc(), null),
                orderAskInfo.getAskCount(),
                orderAskInfo.getOrderStatu(),
                orderAskInfo.getOrderLimitTime(),
                orderAskInfo.getSubSheetCount(),
                StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(orderAskInfo.getHangStartTime()), null),
                orderAskInfo.getHangTimeSum(),
                orderAskInfo.getPreAlarmValue(),
                orderAskInfo.getAlarmValue(),
                StringUtils.defaultIfEmpty(orderAskInfo.getVerifyReasonId(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getVerifyReasonName(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getReplyContent(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getComment(), null),
                orderAskInfo.getNetFlag(),
                orderAskInfo.getServiceDate(),
                StringUtils.defaultIfEmpty(orderAskInfo.getServiceDateDesc(), null),
                orderAskInfo.getCustGroup(),
                StringUtils.defaultIfEmpty(orderAskInfo.getCustGroupDesc(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getAssistSellNo(), null),
                orderAskInfo.getTsKeyWord(),
                StringUtils.defaultIfEmpty(orderAskInfo.getTsKeyWordDesc(), null),
                orderAskInfo.getComeCategory(),
                StringUtils.defaultIfEmpty(orderAskInfo.getCategoryName(), null),
                orderAskInfo.getAreaId(),//苏州受理单需要保存的字段                
                StringUtils.defaultIfEmpty(orderAskInfo.getAreaName(), null),
                orderAskInfo.getSubStationId(),
                StringUtils.defaultIfEmpty(orderAskInfo.getSubStationName(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getSendToOrgId(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getSendToOrgName(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getProductType(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getProductTypeName(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getNodeId(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getServiceKey(), null),
                orderAskInfo.getChannelDetailId(),
                StringUtils.defaultIfEmpty(orderAskInfo.getChannelDetailDesc(), null),
                orderAskInfo.getIsOwner(),
                StringUtils.defaultIfEmpty(orderAskInfo.getMoreRelaInfo(), null),
                StringUtils.defaultIfEmpty(orderAskInfo.getServOrderId(), null),
                orderAskInfo.getMonth()
        );
    }

    /**
     * 更新定单的挂起的子单数量及挂起时间
     *
     * @param orderId         受理单号
     * @param subSheetHoldNum 挂起字单时间
     * @param holdStrTime     挂起开始时间
     * @param holdSumTime     挂起总时间
     * @return 更新的记录数量
     */
    public int updateSubSheetHoldInfo(String orderId, int subSheetHoldNum,
                                      String holdStrTime, int holdSumTime) {
        return jt.update(this.updateSubSheetHoldInfoSql, subSheetHoldNum, 
        		StringUtils.defaultIfEmpty(PubFunc.dbDateToStr(holdStrTime), null), holdSumTime, orderId);
    }

    public OrderAskInfo[] getOrderAskInfoByCondition(String condition, boolean hisFlag) {
        String strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK WHERE 1=1 ";
        if (hisFlag) {
            strsql = "SELECT * FROM CC_SERVICE_ORDER_ASK_HIS WHERE 1=1 ";
        }
        strsql = strsql + condition;
        
        List tmpList = jt.query(strsql, new OrderAskInfoRmp());
        int size = tmpList.size();
        if (size == 0) {
            return new OrderAskInfo[0];
        }
        OrderAskInfo[] orderAskInfoList = new OrderAskInfo[size];
        for (int i = 0; i < size; i++) {
            orderAskInfoList[i] = (OrderAskInfo) tmpList.get(i);
        }
        return orderAskInfoList;
    }

    /**
     * 根据产品码查询该产品一个月内的投诉记录数
     *
     * @param prodNum
     * @param sql
     * @return
     */
    public int[] getOrderProHisCount(String prodNum, String sql) {
        int[] counts = new int[2];
        String prodNum0 = prodNum;
        if (prodNum.substring(0, 1).equals("0")) {
            prodNum = prodNum.substring(1);
        } else {
            prodNum0 = "0" + prodNum;
        }
        String strSql = "SELECT COUNT(1) FROM CC_SERVICE_ORDER_ASK WHERE (prod_num = ? OR prod_num = ?) AND ACCEPT_DATE >= ADDDATE(NOW(), INTERVAL -3 MONTH)";
        String strSqlHis = "SELECT COUNT(1) FROM CC_SERVICE_ORDER_ASK_HIS WHERE (prod_num = ? OR prod_num = ?) AND ACCEPT_DATE >= ADDDATE(NOW(), INTERVAL -3 MONTH) AND ORDER_STATU in (700000103, 720130010)";
        if (null != sql && sql.length() > 0) {
            strSql = strSql + sql;
            strSqlHis = strSqlHis + sql;
        }
        counts[0] = jt.queryForObject(strSql, new Object[]{prodNum, prodNum0}, Integer.class);
        counts[1] = jt.queryForObject(strSqlHis, new Object[]{prodNum, prodNum0}, Integer.class);
        return counts;
    }

    /**
     * 根据产品码查询该产品一个月内的投诉记录(竣工和未竣工的受理单)
     *
     * @param prodNum
     * @param sql
     * @return
     */
    public OrderAskInfo[][] getOrderProHis(String prodNum, String sql) {
        String prodNum0 = prodNum;
        if (prodNum.substring(0, 1).equals("0")) {
            prodNum = prodNum.substring(1);
        } else {
            prodNum0 = "0" + prodNum;
        }
        String strSql = "SELECT * FROM CC_SERVICE_ORDER_ASK WHERE (prod_num = ? OR prod_num = ?) AND ACCEPT_DATE >= ADDDATE(NOW(),INTERVAL -3 MONTH)";
        String strSqlHis = "SELECT * FROM CC_SERVICE_ORDER_ASK_HIS WHERE (prod_num = ? OR prod_num = ?) AND ACCEPT_DATE >= ADDDATE(NOW(),INTERVAL -3 MONTH) AND ORDER_STATU in (700000103, 720130010)";
        if (null != sql && sql.length() > 0) {
            strSql = strSql + sql;
            strSqlHis = strSqlHis + sql;
        }
        OrderAskInfo[][] allOrders = new OrderAskInfo[2][];

        List tmpList = jt.query(strSql, new Object[]{prodNum, prodNum0}, new OrderAskInfoRmp());
        if (!tmpList.isEmpty()) {
            int size = tmpList.size();
            OrderAskInfo[] curOrder = new OrderAskInfo[size];
            for (int i = 0; i < size; i++) {
                curOrder[i] = (OrderAskInfo) tmpList.get(i);
            }
            tmpList.clear();
            tmpList = null;
            allOrders[0] = curOrder;
        }
        List tmpListHis = jt.query(strSqlHis, new Object[]{prodNum, prodNum0}, new OrderAskInfoRmp());
        if (!tmpListHis.isEmpty()) {
            int sizeHis = tmpListHis.size();
            OrderAskInfo[] oldOrder = new OrderAskInfo[sizeHis];
            for (int j = 0; j < sizeHis; j++) {
                oldOrder[j] = (OrderAskInfo) tmpListHis.get(j);
            }
            tmpListHis.clear();
            tmpListHis = null;
            allOrders[1] = oldOrder;
        }

        return allOrders;
    }

    /**
     * 更新定单时间
     *
     * @param orderId 定单号
     * @return 更新记录数
     * @month 分区标志 取消 14 更新完成时间 15 更新完成时间我处理时间(BOOKING_DATE) 16 更新处理完成时间
     */
    public int updateOrderFinishDate(String orderId, Integer month) {
        String strsql = "UPDATE CC_SERVICE_ORDER_ASK T SET T.FINISH_DATE = NOW(),T.MODIFY_DATE = NOW(),T.BOOKING_DATE = NOW() WHERE T.SERVICE_ORDER_ID = ? ";

        if (month.equals(14)) {
            strsql = "UPDATE CC_SERVICE_ORDER_ASK T SET T.FINISH_DATE = NOW(),T.MODIFY_DATE = NOW() WHERE T.SERVICE_ORDER_ID = ? ";
        }
        if (month.equals(15)) {
            strsql = "UPDATE CC_SERVICE_ORDER_ASK T SET T.FINISH_DATE = NOW(),T.MODIFY_DATE = NOW(),T.BOOKING_DATE = NOW() WHERE T.SERVICE_ORDER_ID = ? ";
        }
        if (month.equals(16)) {
            strsql = "UPDATE CC_SERVICE_ORDER_ASK T SET T.BOOKING_DATE = NOW(),T.MODIFY_DATE = NOW() WHERE T.SERVICE_ORDER_ID = ? ";
        }
        return jt.update(strsql, orderId);
    }

    /**
     * 更新定单受理次数
     *
     * @param orderId 定单号
     * @param count   受量次数
     * @return
     */
    public int updateOrderAskCount(String orderId, int count) {
        String strsql = "UPDATE CC_SERVICE_ORDER_ASK T SET T.ACCEPT_COUNT = ? WHERE T.SERVICE_ORDER_ID = ?";
        return jt.update(strsql, count, orderId);
    }

    /**
     * 预受理是否成功,成功的话写CRM定单号
     *
     * @param orderAskInfo 定单对象,定单号,月分区,ACCEPT_CHANNEL_ID CRM定单号,ACCEPT_CHANNEL_DESC
     * @return
     */
    public int updateCrmAskSheet(OrderAskInfo orderAskInfo) {
    	String strSql = "UPDATE CC_SERVICE_ORDER_ASK A SET A.ACCEPT_CHANNEL_ID=?,A.ACCEPT_CHANNEL_DESC=? WHERE A.MONTH_FLAG=? AND A.SERVICE_ORDER_ID = ?";
        return this.jt.update(strSql,
                orderAskInfo.getAskChannelId(),
                StringUtils.defaultIfEmpty(orderAskInfo.getAskChannelDesc(), null),
                orderAskInfo.getMonth(),
                StringUtils.defaultIfEmpty(orderAskInfo.getServOrderId(), null)
        );
    }

    /**
     * 更新疑难工单的定性质
     *
     * @param orderAskInfo YS_QUALIATIVE_ID定性ID YS_QUALIATIVE_NAME 定性名
     * @return
     */
    public int updateQualiative(int ysQualiativeId, String ysQualiativeName, Integer month, String orderId) {
        String strSql = "UPDATE CC_SERVICE_ORDER_ASK A SET A.YS_QUALIATIVE_ID=?,A.YS_QUALIATIVE_NAME=? WHERE A.MONTH_FLAG=? AND A.SERVICE_ORDER_ID = ?";
        return this.jt.update(strSql, ysQualiativeId, ysQualiativeName, month, orderId);
    }

    /**
     * 更新定单的退单信息
     *
     * @param orderId  定单号
     * @param comments 备注
     * @return 更新记录数
     */
    public int updateOrderComments(String orderId, String comments) {
        String strsql = "UPDATE CC_SERVICE_ORDER_ASK T  " +
                "SET T.REPLY_CONTENT = ? WHERE T.SERVICE_ORDER_ID = ?";
        return jt.update(strsql, comments, orderId);
    }

    /**
     * 记录定单是在哪个环节竣工的
     *
     * @param orderId  定单
     * @param regionId 地域
     * @param tacheId  竣工的环节
     * @return
     */
    public int updateFinTache(String orderId, int regionId, int tacheId) {
        String strSql = "UPDATE CC_SERVICE_ORDER_ASK A SET A.NET_FLAG=? WHERE A.REGION_ID=? AND A.SERVICE_ORDER_ID=? ";
        return this.jt.update(strSql, tacheId, regionId, orderId);
    }

    /**
     * 根据地域和客户号码，查询该号码是否在投诉特殊客户表中存在
     *
     * @param custNum
     * @param regionId
     * @return
     */
    public int getTsEspeciallyCust(String custNum, int regionId) {
        String strSql = "SELECT COUNT(*) FROM CC_ESPECIALLY_CUST A WHERE A.REGION_ID=? AND A.CUST_NUM=? AND A.STATU=1 ";
        return this.jt.queryForObject(strSql, new Object[]{regionId, custNum}, Integer.class);
    }

    /**
     * 根据地域和客户号码，查询该号码用户的投诉特征
     *
     * @param custNum
     * @param regionId
     * @return
     */
    public String getTsEspeciallyContent(String custNum, int regionId) {
        String strSql = "SELECT TS_ESPECIALLY FROM CC_ESPECIALLY_CUST A WHERE A.REGION_ID=? AND A.CUST_NUM=? AND A.STATU=1 ";
        List tmp = this.jt.queryForList(strSql, regionId, custNum);
        String content = "";
        if (!tmp.isEmpty()) {
            Map map = (Map) tmp.get(0);
            content = (String) map.get("TS_ESPECIALLY");
        }
        return content;
    }

    public int saveYNSJOrder(YNSJOrder order) {
        String sql = "insert into CC_YNSJ_ORDERINFO (SERVICE_ORDER_ID, APPEAL_PROD_ID, APPEAL_REASON_ID, CUST_NAME, REAL_INFO, " +
                "CREDENTIAL_TYPE, CREDENTIAL_NUMBR, PROD_NUM, ACCEPT_DATE, ACCEPT_STAFF_NAME, ACCPET_STAFF_CODE, ACCPET_ORG_NAME, " +
                "CUST_REQUIRE, URGENCY_GRADE, ACCPET_SOURCE, ASSIST_SALE_STAFF, INSTALL_AREA, ACTION_DATE, ACTION_RESULT, " +
                "PROD_INST_ID, TELL_NO, PROD_TYPE_DESC, ACCOUNT, PRODUCT_TYPE_NAME, SOURCE_NUM, RELA_MAN, ACCEPT_CONTENT, " +
                "ORDER_TYPE, ADDRESS_ID, ADDRESS_DESC, FILL_ADDRESS, COUNTY_ID, IS_CLEARBUSINESS, STANDARD_ADDRESS_ID) " +
                "values (?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?, ?, ?, ?, ?, ?, ?, NOW(), '00', ?, " +
                "?, ?, ?, ?, ?, ? ,?, ?, ?, ?, ?, ?, ?, ?)";
        return this.jt.update(sql,
                StringUtils.defaultIfEmpty(order.getServOrderId(), null),
                order.getAppealProdId(),
                order.getAppealReasonId(),
                StringUtils.defaultIfEmpty(order.getCustName(), null),
                StringUtils.defaultIfEmpty(order.getRealInfo(), null),
                StringUtils.defaultIfEmpty(order.getCredentialType(), null),
                StringUtils.defaultIfEmpty(order.getCredentialNumbr(), null),
                StringUtils.defaultIfEmpty(order.getProdNum(), null),
                StringUtils.defaultIfEmpty(order.getAcceptStaffName(), null),
                StringUtils.defaultIfEmpty(order.getAccpetStaffCode(), null),
                StringUtils.defaultIfEmpty(order.getAccpetOrgName(), null),
                StringUtils.defaultIfEmpty(order.getCustRequire(), null),
                StringUtils.defaultIfEmpty(order.getUrgencyGrade(), null),
                StringUtils.defaultIfEmpty(order.getAccpetSource(), null),
                StringUtils.defaultIfEmpty(order.getAssistSaleStaff(), null),
                StringUtils.defaultIfEmpty(order.getInstallArea(), null),
                StringUtils.defaultIfEmpty(order.getProdInstId(), null),
                StringUtils.defaultIfEmpty(order.getTelNo(), null),
                StringUtils.defaultIfEmpty(order.getProdTypeDesc(), null),
                StringUtils.defaultIfEmpty(order.getAccount(), null),
                StringUtils.defaultIfEmpty(order.getProductTypeName(), null),
                StringUtils.defaultIfEmpty(order.getSourceNum(), null),
                StringUtils.defaultIfEmpty(order.getRelaMan(), null),
                StringUtils.defaultIfEmpty(order.getAcceptContent(), null),
                StringUtils.defaultIfEmpty(order.getOrderType(), null),
                StringUtils.defaultIfEmpty(order.getAddressId(), null),
                StringUtils.defaultIfEmpty(order.getAddressDesc(), null),
                StringUtils.defaultIfEmpty(order.getFillAddress(), null),
                StringUtils.defaultIfEmpty(order.getCountyId(), null),
                StringUtils.defaultIfEmpty(order.getIsClearBusiness(), null),
                StringUtils.defaultIfEmpty(order.getStandardAddressId(), null)
        );
    }

    public int updateYNSJOrder(String servOrderId, String actionResult) {
        String sql = "UPDATE CC_YNSJ_ORDERINFO SET ACTION_DATE=NOW(),ACTION_RESULT=? WHERE SERVICE_ORDER_ID=?";
        return this.jt.update(sql, actionResult, servOrderId);
    }

    /**
     * 保存中台返回的订单号码
     *
     * @param servOrderId  商机单号
     * @param actionResult 调用结果
     * @param orderId      订单号码
     * @return
     */
    public int updateBusinessOrder(String servOrderId, String actionResult, String orderId) {
        String sql = "UPDATE CC_YNSJ_ORDERINFO SET ACTION_DATE=NOW(),ACTION_RESULT=?,ORDERID=? WHERE SERVICE_ORDER_ID=?";
        return this.jt.update(sql, actionResult, orderId, servOrderId);
    }

    public YNSJOrder queryYNSJOrder(String orderId) {
        String sql = "select * from cc_ynsj_orderinfo c where c.service_order_id=? and c.action_result != '1'";
        List<YNSJOrder> list = jt.query(sql, new Object[]{orderId}, new YNSJOrderRmp());
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public int saveYNSJResult(YNSJResult result) {
        String sql = "insert into cc_ynsj_result (SERVICE_ORDER_ID, RECEIVE_ORDER_DATE, DEAL_STAFF_CODE, DEAL_STAFF_NAME, " +
                "DEAL_ORG_NAME, DEAL_ORDER_DATE, ONE_SJ_CATALOG, TWO_SJ_CATALOG, THREE_SJ_CATALOG, FOUR_SJ_CATALOG_DESC, " +
                "IS_VALID, IS_BSS_ACCEPT, BSS_WORK_ORDER_ID, IS_SEND_NOTE, BUSINESS_NAME, BUSI_WROK_TIME, DEAL_CONTENT, " +
                "ACCEPT_STATUS, ACCEPT_FAIL_REASON, CREATE_DATE, SYSTEM, END_STATUS) " +
                "values (?, str_to_date(? , '%Y-%m-%d %H:%i:%s'), ?, ?, ?, str_to_date(? , '%Y-%m-%d %H:%i:%s'), ?, ?, ?, " +
                "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)";
        return this.jt.update(sql,
                StringUtils.defaultIfEmpty(result.getServiceOrderId(), null),
                StringUtils.defaultIfEmpty(result.getReceiveOrderDate(), null),
                StringUtils.defaultIfEmpty(result.getDealStaffCode(), null),
                StringUtils.defaultIfEmpty(result.getDealStaffName(), null),
                StringUtils.defaultIfEmpty(result.getDealOrgName(), null),
                StringUtils.defaultIfEmpty(result.getDealOrderDate(), null),
                result.getOneSjCatalog(),
                result.getTwoSjCatalog(),
                result.getThreeSjCatalog(),
                StringUtils.defaultIfEmpty(result.getFourSjCatalogDesc(), null),
                result.getIsValid(),
                result.getIsBssAccept(),
                StringUtils.defaultIfEmpty(result.getBssWorkOrderId(), null),
                result.getIsSendNote(),
                StringUtils.defaultIfEmpty(result.getBusinessName(), null),
                StringUtils.defaultIfEmpty(result.getBusiWrokTime(), null),
                StringUtils.defaultIfEmpty(result.getDealContent(), null),
                result.getAcceptStatus(),
                StringUtils.defaultIfEmpty(result.getAcceptFailReason(), null),
                StringUtils.defaultIfEmpty(result.getSystem(), null),
		        result.getEndStatus()
        );
    }

    public int updateYNSJResult(YNSJResult result) {
        String sql = "update cc_ynsj_result set RECEIVE_ORDER_DATE=str_to_date(? , '%Y-%m-%d %H:%i:%s'), DEAL_STAFF_CODE=?, DEAL_STAFF_NAME=?, " +
                "DEAL_ORG_NAME=?, DEAL_ORDER_DATE=str_to_date(? , '%Y-%m-%d %H:%i:%s'), ONE_SJ_CATALOG=?, TWO_SJ_CATALOG=?, THREE_SJ_CATALOG=?, FOUR_SJ_CATALOG_DESC=?, " +
                "IS_VALID=?, IS_BSS_ACCEPT=?, BSS_WORK_ORDER_ID=?, IS_SEND_NOTE=?, BUSINESS_NAME=?, BUSI_WROK_TIME=?, DEAL_CONTENT=?, " +
                "ACCEPT_STATUS=?, ACCEPT_FAIL_REASON=?, CREATE_DATE=NOW() where SERVICE_ORDER_ID=?";
        return this.jt.update(sql,
                StringUtils.defaultIfEmpty(result.getReceiveOrderDate(), null),
                StringUtils.defaultIfEmpty(result.getDealStaffCode(), null),
                StringUtils.defaultIfEmpty(result.getDealStaffName(), null),
                StringUtils.defaultIfEmpty(result.getDealOrgName(), null),
                StringUtils.defaultIfEmpty(result.getDealOrderDate(), null),
                result.getOneSjCatalog(),
                result.getTwoSjCatalog(),
                result.getThreeSjCatalog(),
                StringUtils.defaultIfEmpty(result.getFourSjCatalogDesc(), null),
                result.getIsValid(),
                result.getIsBssAccept(),
                StringUtils.defaultIfEmpty(result.getBssWorkOrderId(), null),
                result.getIsSendNote(),
                StringUtils.defaultIfEmpty(result.getBusinessName(), null),
                StringUtils.defaultIfEmpty(result.getBusiWrokTime(), null),
                StringUtils.defaultIfEmpty(result.getDealContent(), null),
                result.getAcceptStatus(),
                StringUtils.defaultIfEmpty(result.getAcceptFailReason(), null),
                StringUtils.defaultIfEmpty(result.getServiceOrderId(), null)
        );
    }

    public int saveCallSummaryOrder(CallSummaryOrder order) {
    	int num = portalFeign.saveCallSummaryOrder(order.toString());
    	log.info("saveCallSummaryOrder Affected rows: {}", num);
    	return num;
    }

    public CallSummary getLastSummary(String orderId, boolean updateFlag) {
        List list = null;
        if (updateFlag) {
        	String strSql = "select s.SERIAL_NO,s.N_ID,s.EX_FIRST,s.EX_SECOND,s.EX_THIRD,s.COMMENTS "
        			+ "from cs_call_summary_update s where s.order_id=? order by s.create_date desc limit 1";
            list = this.jt.query(strSql, new Object[]{orderId}, new CallSummaryRmp());
        } else {
            list = portalFeign.getCsCallSummaryByOrderId(orderId);
        }

        if (list.isEmpty()) {
            return null;
        }
        return (CallSummary) list.get(0);
    }

    public int saveCallSummaryUpdate(CallSummary summary) {
        String sql = "insert into cs_call_summary_update (ORDER_ID, SERIAL_NO, OLD_N_ID, OLD_FIRST_LEVEL"
                + ", OLD_SECOND_LEVEL, OLD_THIRD_LEVEL, OLD_FOURTH_LEVEL, OLD_FIFTH_LEVEL, OLD_SIXTH_LEVEL"
                + ", OLD_EX_FIRST, OLD_EX_SECOND, OLD_EX_THIRD, N_ID, FIRST_LEVEL, SECOND_LEVEL, THIRD_LEVEL"
                + ", FOURTH_LEVEL, FIFTH_LEVEL, SIXTH_LEVEL, EX_FIRST, EX_SECOND, EX_THIRD, COMMENTS, LOGONNAME, CREATE_DATE, CREATE_CHANNEL)" +
                "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
        return this.jt.update(sql,
                summary.getOrderId(),
                summary.getSerialNo(),
                Integer.parseInt(summary.getOldNId()),
                StringUtils.defaultIfEmpty(summary.getOldFirstLevel(), null),
                StringUtils.defaultIfEmpty(summary.getOldSecondLevel(), null),
                StringUtils.defaultIfEmpty(summary.getOldThirdLevel(), null),
                StringUtils.defaultIfEmpty(summary.getOldFourthLevel(), null),
                StringUtils.defaultIfEmpty(summary.getOldFifthLevel(), null),
                StringUtils.defaultIfEmpty(summary.getOldSixthLevel(), null),
                StringUtils.defaultIfEmpty(summary.getOldExFirst(), null),
                StringUtils.defaultIfEmpty(summary.getOldExSecond(), null),
                StringUtils.defaultIfEmpty(summary.getOldExThird(), null),
                Integer.parseInt(summary.getnId()),
                StringUtils.defaultIfEmpty(summary.getFirstLevel(), null),
                StringUtils.defaultIfEmpty(summary.getSecondLevel(), null),
                StringUtils.defaultIfEmpty(summary.getThirdLevel(), null),
                StringUtils.defaultIfEmpty(summary.getFourthLevel(), null),
                StringUtils.defaultIfEmpty(summary.getFifthLevel(), null),
                StringUtils.defaultIfEmpty(summary.getSixthLevel(), null),
                StringUtils.defaultIfEmpty(summary.getExFirst(), null),
                StringUtils.defaultIfEmpty(summary.getExSecond(), null),
                StringUtils.defaultIfEmpty(summary.getExThird(), null),
                StringUtils.defaultIfEmpty(summary.getComments(), null),
                summary.getLogonname(),
                StringUtils.defaultIfEmpty(summary.getCreateChannel(), null)
        );
    }

    public int updateAcceptDate(String serviceOrderId) {
        return this.jt.update(updateAcceptDateSql, serviceOrderId);
    }

    public String selectAcceptDate(String serviceOrderId) {
        Object tmp = jt.queryForObject(selectAcceptDateSql, new Object[]{serviceOrderId}, String.class);
        return tmp.toString();
    }

    public List queryAllOrder(String serviceOrderId, String prodNum, String relaInfo, int regionId) {
    	String sql =
    			"SELECT\n"
    			+ " SERVICE_ORDER_ID,PROD_NUM,RELA_INFO,\n"
    			+ " SERVICE_TYPE,\n"
    			+ " COME_CATEGORY,\n"
    			+ " RELA_TYPE,\n"
    			+ " DATE_FORMAT( ACCEPT_DATE, '%Y-%m-%d %H:%i:%s' ) ACCEPT_DATE,\n"
    			+ " BEST_ORDER,\n"
    			+ " TS_DEAL_RESULT,\n"
    			+ " FINAL_OPTION_FLAG\n"
    			+ "FROM\n"
    			+ " (\n"
    			+ "SELECT\n"
    			+ " A.SERVICE_ORDER_ID,A.PROD_NUM,A.RELA_INFO,\n"
    			+ " A.SERVICE_TYPE,\n"
    			+ " A.COME_CATEGORY,\n"
    			+ " A.RELA_TYPE,\n"
    			+ " A.ACCEPT_DATE,\n"
    			+ " B.BEST_ORDER,\n"
    			+ " C.TS_DEAL_RESULT,\n"
    			+ " C.FINAL_OPTION_FLAG\n"
    			+ "FROM\n"
    			+ " CC_SERVICE_ORDER_ASK A,\n"
    			+ " CC_SERVICE_CONTENT_ASK B, \n"
    			+ " CC_SERVICE_LABEL C \n"
    			+ "WHERE\n"
    			+ " A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID \n"
    			+ " AND A.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID \n"
    			+ " AND A.ORDER_STATU NOT IN ( 700000099, 720130001 ) \n"
    			+ " AND A.ACCEPT_DATE > DATE_SUB( NOW( ), INTERVAL 30 DAY ) \n"
    			+ " AND A.SERVICE_TYPE IN ( 700006312, 720130000 ) \n"
    			+ " AND A.REGION_ID = ? \n"
    			+ " AND ( A.PROD_NUM IN ( ?, ? ) OR A.RELA_INFO IN ( ?, ? ) ) \n"
    			+ " AND A.SERVICE_ORDER_ID <> ? UNION\n"
    			+ "SELECT\n"
    			+ " A.SERVICE_ORDER_ID,A.PROD_NUM,A.RELA_INFO,\n"
    			+ " A.SERVICE_TYPE,\n"
    			+ " A.COME_CATEGORY,\n"
    			+ " A.RELA_TYPE,\n"
    			+ " A.ACCEPT_DATE,\n"
    			+ " B.BEST_ORDER,\n"
    			+ " C.TS_DEAL_RESULT,\n"
    			+ " C.FINAL_OPTION_FLAG\n"
    			+ "FROM\n"
    			+ " CC_SERVICE_ORDER_ASK_HIS A,\n"
    			+ " CC_SERVICE_CONTENT_ASK_HIS B, \n"
    			+ " CC_SERVICE_LABEL_HIS C \n"
    			+ "WHERE\n"
    			+ " A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID \n"
    			+ " AND A.ORDER_VESION = B.ORDER_VESION \n"
    			+ " AND A.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID \n"
    			+ " AND A.ORDER_STATU IN ( 700000103, 720130010 ) \n"
    			+ " AND A.ACCEPT_DATE > DATE_SUB( NOW( ), INTERVAL 30 DAY ) \n"
    			+ " AND A.SERVICE_TYPE IN ( 700006312, 720130000 ) \n"
    			+ " AND A.REGION_ID = ? \n"
    			+ " AND ( A.PROD_NUM IN ( ?, ? ) OR A.RELA_INFO IN ( ?, ? ) ) \n"
    			+ " AND A.SERVICE_ORDER_ID <> ? \n"
    			+ " ) X \n"
    			+ "ORDER BY\n"
    			+ " ACCEPT_DATE";
        return jt.queryForList(sql, regionId, prodNum, relaInfo, prodNum, relaInfo, serviceOrderId, regionId, prodNum, relaInfo, prodNum, relaInfo, serviceOrderId);
    }

    @Override
    public List queryOrderPreference(String serviceOrderId, String prodNum, int regionId) {
    	String sql = "select c.ACCEPT_CHANNEL_ID from cc_service_order_ask c where c.PROD_NUM = ? and c.REGION_ID = ? " +
                "and c.ACCEPT_DATE > DATE_SUB( NOW( ), INTERVAL 30 DAY ) and c.SERVICE_TYPE = 720130000 and c.ORDER_STATU NOT IN (700000099, 720130001)" +
                "and c.SERVICE_ORDER_ID <> ? UNION " +
                "select c.ACCEPT_CHANNEL_ID from cc_service_order_ask_his c where c.PROD_NUM = ? and c.REGION_ID = ? " +
                "and c.ACCEPT_DATE > DATE_SUB( NOW( ), INTERVAL 30 DAY ) and c.SERVICE_TYPE = 720130000 and c.ORDER_STATU IN (700000103, 720130010) and c.SERVICE_ORDER_ID <> ?";
        return jt.queryForList(sql,prodNum,regionId,serviceOrderId,prodNum,regionId,serviceOrderId);
    }
    
    public List queryAllOrderCX(String serviceOrderId, String prodNum, String relaInfo, int regionId) {
		String sql = "SELECT SERVICE_ORDER_ID, COME_CATEGORY, DATE_FORMAT(ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE, TS_DEAL_RESULT, SERVICE_TYPE, FINAL_OPTION_FLAG\n"
				+ "  FROM (SELECT A.SERVICE_ORDER_ID, A.COME_CATEGORY, A.ACCEPT_DATE, TS_DEAL_RESULT, A.SERVICE_TYPE, C.FINAL_OPTION_FLAG \n"
				+ "          FROM CC_SERVICE_ORDER_ASK A, CC_SERVICE_CONTENT_ASK B, CC_SERVICE_LABEL C\n"
				+ "         WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n"
				+ "           AND A.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID\n"
				+ "           AND A.ORDER_STATU NOT IN (700000099, 720130001)\n"
				+ "           AND A.ACCEPT_DATE > DATE_SUB(NOW(), INTERVAL 30 DAY)\n"
				+ "           AND A.SERVICE_TYPE IN (700006312, 720130000, 720200003)\n"
				+ "           AND A.REGION_ID = ?\n"
				+ "           AND (A.PROD_NUM IN (?, ?) OR A.RELA_INFO IN (?, ?))\n"
				+ "           AND A.SERVICE_ORDER_ID <> ?\n" + "        UNION\n"
				+ "        SELECT A.SERVICE_ORDER_ID, A.COME_CATEGORY, A.ACCEPT_DATE, TS_DEAL_RESULT, A.SERVICE_TYPE, C.FINAL_OPTION_FLAG \n"
				+ "          FROM CC_SERVICE_ORDER_ASK_HIS A, CC_SERVICE_CONTENT_ASK_HIS B, CC_SERVICE_LABEL_HIS C\n"
				+ "         WHERE A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID\n"
				+ "           AND A.ORDER_VESION = B.ORDER_VESION\n"
				+ "           AND A.SERVICE_ORDER_ID = C.SERVICE_ORDER_ID\n"
				+ "           AND A.ORDER_STATU IN (700000103, 720130010)\n"
				+ "           AND A.ACCEPT_DATE > DATE_SUB(NOW(), INTERVAL 30 DAY)\n"
				+ "           AND A.SERVICE_TYPE IN (700006312, 720130000, 720200003)\n"
				+ "           AND A.REGION_ID = ?\n"
				+ "           AND (A.PROD_NUM IN (?, ?) OR A.RELA_INFO IN (?, ?))\n"
				+ "           AND A.SERVICE_ORDER_ID <> ?) X\n" + " ORDER BY ACCEPT_DATE";
		return jt.queryForList(sql, regionId, prodNum, relaInfo, prodNum, relaInfo, serviceOrderId, regionId, prodNum,
				relaInfo, prodNum, relaInfo, serviceOrderId);
	}

    public int updateOrderLimitTime(int orderLimitTime, String serviceOrderId) {
        return this.jt.update(updateOrderLimitTimeSql, orderLimitTime, serviceOrderId);
    }

    public int insertServiceWisdomType(String orderIdOld, String holdFlag, String sendFlag) {
        return jt.update(insertServiceWisdomTypeSql, orderIdOld, holdFlag, sendFlag);
    }

    public int insertServiceWisdomTypeHis(String orderIdOld) {
        int res = 0;
        res = jt.update(insertServiceWisdomTypeHisSql, orderIdOld);
        if (res > 0) {
            return jt.update(deleteServiceWisdomTypeSql, orderIdOld);
        }
        return res;
    }

    public int updateServiceWisdomType(String resultCode, String resultMsg, String scenario, String wisdomType, String unifiedflag, String orderIdNew, String orderIdOld) {
        return jt.update(updateServiceWisdomTypeSql, resultCode, resultMsg, scenario, wisdomType, unifiedflag, orderIdNew, orderIdOld);
    }

    /**
     * @return the jt
     */
    public JdbcTemplate getJt() {
        return jt;
    }

    /**
     * @param jt the jt to set
     */
    public void setJt(JdbcTemplate jt) {
        this.jt = jt;
    }

    /**
     * @return the saveServOrderSql
     */
    public String getSaveServOrderSql() {
        return saveServOrderSql;
    }

    /**
     * @param saveServOrderSql the saveServOrderSql to set
     */
    public void setSaveServOrderSql(String saveServOrderSql) {
        this.saveServOrderSql = saveServOrderSql;
    }

    /**
     * @return the queryOrderAskInfoByStatuSql
     */
    public String getQueryOrderAskInfoByStatuSql() {
        return queryOrderAskInfoByStatuSql;
    }


    /**
     * @param queryOrderAskInfoByStatuSql the queryOrderAskInfoByStatuSql to set
     */
    public void setQueryOrderAskInfoByStatuSql(String queryOrderAskInfoByStatuSql) {
        this.queryOrderAskInfoByStatuSql = queryOrderAskInfoByStatuSql;
    }


    /**
     * @return the queryOrderAskInfoByStatuHisSql
     */
    public String getQueryOrderAskInfoByStatuHisSql() {
        return queryOrderAskInfoByStatuHisSql;
    }


    /**
     * @param queryOrderAskInfoByStatuHisSql the queryOrderAskInfoByStatuHisSql to set
     */
    public void setQueryOrderAskInfoByStatuHisSql(
            String queryOrderAskInfoByStatuHisSql) {
        this.queryOrderAskInfoByStatuHisSql = queryOrderAskInfoByStatuHisSql;
    }


    /**
     * @return the queryOrderAskInfoSql
     */
    public String getQueryOrderAskInfoSql() {
        return queryOrderAskInfoSql;
    }


    /**
     * @param queryOrderAskInfoSql the queryOrderAskInfoSql to set
     */
    public void setQueryOrderAskInfoSql(String queryOrderAskInfoSql) {
        this.queryOrderAskInfoSql = queryOrderAskInfoSql;
    }

    /**
     * @return the delOrderAskInfoSql
     */
    public String getDelOrderAskInfoSql() {
        return delOrderAskInfoSql;
    }


    /**
     * @param delOrderAskInfoSql the delOrderAskInfoSql to set
     */
    public void setDelOrderAskInfoSql(String delOrderAskInfoSql) {
        this.delOrderAskInfoSql = delOrderAskInfoSql;
    }


    /**
     * @return the updateOrderStatuSql
     */
    public String getUpdateOrderStatuSql() {
        return updateOrderStatuSql;
    }


    /**
     * @param updateOrderStatuSql the updateOrderStatuSql to set
     */
    public void setUpdateOrderStatuSql(String updateOrderStatuSql) {
        this.updateOrderStatuSql = updateOrderStatuSql;
    }


    /**
     * @return the updateSubSheetHoldInfoSql
     */
    public String getUpdateSubSheetHoldInfoSql() {
        return updateSubSheetHoldInfoSql;
    }


    /**
     * @param updateSubSheetHoldInfoSql the updateSubSheetHoldInfoSql to set
     */
    public void setUpdateSubSheetHoldInfoSql(String updateSubSheetHoldInfoSql) {
        this.updateSubSheetHoldInfoSql = updateSubSheetHoldInfoSql;
    }


    /**
     * @return the updateOrderAskInfoSql
     */
    public String getUpdateOrderAskInfoSql() {
        return updateOrderAskInfoSql;
    }


    /**
     * @param updateOrderAskInfoSql the updateOrderAskInfoSql to set
     */
    public void setUpdateOrderAskInfoSql(String updateOrderAskInfoSql) {
        this.updateOrderAskInfoSql = updateOrderAskInfoSql;
    }

    /**
     * @return the saveServOrderHisByOrderIdSql
     */
    public String getSaveServOrderHisByOrderIdSql() {
        return saveServOrderHisByOrderIdSql;
    }

    /**
     * @param saveServOrderHisByOrderIdSql the saveServOrderHisByOrderIdSql to set
     */
    public void setSaveServOrderHisByOrderIdSql(String saveServOrderHisByOrderIdSql) {
        this.saveServOrderHisByOrderIdSql = saveServOrderHisByOrderIdSql;
    }

    /**
     * @return 返回 queryOrderAskInfoByAlarmSql。
     */
    public String getQueryOrderAskInfoByAlarmSql() {
        return queryOrderAskInfoByAlarmSql;
    }

    /**
     * @param queryOrderAskInfoByAlarmSql 要设置的 queryOrderAskInfoByAlarmSql。
     */
    public void setQueryOrderAskInfoByAlarmSql(String queryOrderAskInfoByAlarmSql) {
        this.queryOrderAskInfoByAlarmSql = queryOrderAskInfoByAlarmSql;
    }

    /**
     * @return the queryOrderAskInfoHisSql
     */
    public String getQueryOrderAskInfoHisSql() {
        return queryOrderAskInfoHisSql;
    }

    /**
     * @param queryOrderAskInfoHisSql the queryOrderAskInfoHisSql to set
     */
    public void setQueryOrderAskInfoHisSql(String queryOrderAskInfoHisSql) {
        this.queryOrderAskInfoHisSql = queryOrderAskInfoHisSql;
    }

    /**
     * @return updateOrderHisStatuSql
     */
    public String getUpdateOrderHisStatuSql() {
        return updateOrderHisStatuSql;
    }

    /**
     * @param updateOrderHisStatuSql 要设置的 updateOrderHisStatuSql
     */
    public void setUpdateOrderHisStatuSql(String updateOrderHisStatuSql) {
        this.updateOrderHisStatuSql = updateOrderHisStatuSql;
    }

    public String getQueryAskInfoByIdMonthHisSql() {
        return queryAskInfoByIdMonthHisSql;
    }

    public void setQueryAskInfoByIdMonthHisSql(String queryAskInfoByIdMonthHisSql) {
        this.queryAskInfoByIdMonthHisSql = queryAskInfoByIdMonthHisSql;
    }

    public String getQueryAskInfoByIdMonthSql() {
        return queryAskInfoByIdMonthSql;
    }

    public void setQueryAskInfoByIdMonthSql(String queryAskInfoByIdMonthSql) {
        this.queryAskInfoByIdMonthSql = queryAskInfoByIdMonthSql;
    }

    public String getUpdateAcceptDateSql() {
        return updateAcceptDateSql;
    }

    public void setUpdateAcceptDateSql(String updateAcceptDateSql) {
        this.updateAcceptDateSql = updateAcceptDateSql;
    }

    public String getSelectAcceptDateSql() {
        return selectAcceptDateSql;
    }

    public void setSelectAcceptDateSql(String selectAcceptDateSql) {
        this.selectAcceptDateSql = selectAcceptDateSql;
    }

    public String getUpdateOrderLimitTimeSql() {
        return updateOrderLimitTimeSql;
    }

    public void setUpdateOrderLimitTimeSql(String updateOrderLimitTimeSql) {
        this.updateOrderLimitTimeSql = updateOrderLimitTimeSql;
    }

    public String getInsertServiceWisdomTypeSql() {
        return insertServiceWisdomTypeSql;
    }

    public void setInsertServiceWisdomTypeSql(String insertServiceWisdomTypeSql) {
        this.insertServiceWisdomTypeSql = insertServiceWisdomTypeSql;
    }

    public String getInsertServiceWisdomTypeHisSql() {
        return insertServiceWisdomTypeHisSql;
    }

    public void setInsertServiceWisdomTypeHisSql(String insertServiceWisdomTypeHisSql) {
        this.insertServiceWisdomTypeHisSql = insertServiceWisdomTypeHisSql;
    }

    public String getDeleteServiceWisdomTypeSql() {
        return deleteServiceWisdomTypeSql;
    }

    public void setDeleteServiceWisdomTypeSql(String deleteServiceWisdomTypeSql) {
        this.deleteServiceWisdomTypeSql = deleteServiceWisdomTypeSql;
    }

    public String getUpdateServiceWisdomTypeSql() {
        return updateServiceWisdomTypeSql;
    }

    public void setUpdateServiceWisdomTypeSql(String updateServiceWisdomTypeSql) {
        this.updateServiceWisdomTypeSql = updateServiceWisdomTypeSql;
    }

    public String getInsertServiceTrackSql() {
        return insertServiceTrackSql;
    }

    public void setInsertServiceTrackSql(String insertServiceTrackSql) {
        this.insertServiceTrackSql = insertServiceTrackSql;
    }

    @Override
    public Map getRoleWork(String staffId) {
        Map roleWork = new HashMap();
        String roleWorkSql = "SELECT VIEW_TYPE, VIEW_ORG FROM cc_view_work_role WHERE view_staff = ?";
        List roleWorks = this.jt.queryForList(roleWorkSql, staffId);
        if (!roleWorks.isEmpty()) {
            roleWork = (Map) roleWorks.get(0);
        }
        return roleWork;
    }

    @Override
    public Map getWorkView(String staffId) {
        Map returnMap = new HashMap();
        Map roleWork = getRoleWork(staffId);
        if (roleWork.isEmpty()) {
            returnMap = getWorkViewOneOra(staffId);
        } else {
            String viewType = roleWork.get("VIEW_TYPE").toString();
            String viewOrg = roleWork.get("VIEW_ORG") == null ? "" : roleWork.get("VIEW_ORG").toString();
            if ("2".equals(viewType)) {
                returnMap = getWorkViewTwoOra(viewOrg);
            } else if ("3".equals(viewType)) {
                returnMap = getWorkViewThreeOra(viewOrg);
            } else if ("4".equals(viewType)) {
                returnMap.put(0, getWorkViewThreeOra("361143"));
                returnMap.put(1, getWorkViewThreeOra("30,92,362991"));
                returnMap.put(2, getWorkViewFiveOra());
            } else if ("5".equals(viewType)) {
                returnMap = getWorkViewFiveOra();
            }
        }
        return returnMap;
    }

    class KeyRowMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int arg1) throws SQLException {
            return rs.getString(1);
        }
    }

	private Map getWorkViewOneOra(String staffId) {
		Map returnMap = new HashMap();
		String workSql = 
"SELECT c_zx, c_cx, c_ts, c_xt, c_tz, c_gz, c_sj, c_ysl, c_xc, l_cs, l_jj, l_cf, l_sj\n" +
"  FROM (SELECT IFNULL(SUM(c_zx), 0) c_zx,\n" + 
"               IFNULL(SUM(c_cx), 0) c_cx,\n" + 
"               IFNULL(SUM(c_ts), 0) c_ts,\n" + 
"               IFNULL(SUM(c_xt), 0) c_xt,\n" + 
"               IFNULL(SUM(c_tz), 0) c_tz,\n" + 
"               IFNULL(SUM(c_gz), 0) c_gz,\n" + 
"               IFNULL(SUM(c_sj), 0) c_sj,\n" + 
"               IFNULL(SUM(c_ysl), 0) c_ysl,\n" + 
"               IFNULL(SUM(c_xc), 0) c_xc,\n" + 
"               IFNULL(ROUND((SUM(c_zx) + SUM(c_cx) + SUM(c_ts) - SUM(l_cs)) * 100 / (SUM(c_zx) + SUM(c_cx) + SUM(c_ts)), 1), 100) l_cs,\n" + 
"               IFNULL(ROUND(SUM(l_cf) * 100 / (SUM(c_zx) + SUM(c_ts)), 1), 0) l_cf,\n" + 
"               IFNULL(ROUND(SUM(l_sj) * 100 / (SUM(c_zx) + SUM(c_ts)), 1), 0) l_sj\n" + 
"          FROM cc_view_routine_work\n" + 
"         WHERE work_staff = ?) x,\n" + 
"       (SELECT IFNULL(ROUND((SUM(zx_z) + SUM(zx_zx) + SUM(cx_z) + SUM(cx_zx) + SUM(ts1_z) + SUM(ts1_zx)) * 100 / (SUM(zx) + SUM(cx) + SUM(ts1)), 1), 100) l_jj\n" + 
"          FROM cc_view_summary_work\n" + 
"         WHERE deal_staff = ?) y";
		List works = this.jt.queryForList(workSql, staffId, staffId);
		if (!works.isEmpty()) {
			returnMap = (Map) works.get(0);
		}
		return returnMap;
	}

	private Map getWorkViewTwoOra(String viewOrg) {
		Map returnMap = new HashMap();
		String workSql = 
"SELECT c_zx, c_cx, c_ts, c_xt, c_tz, c_gz, c_sj, c_ysl, c_xc, l_cs, l_jj, l_cf, l_sj\n" +
"  FROM (SELECT IFNULL(SUM(c_zx), 0) c_zx,\n" + 
"               IFNULL(SUM(c_cx), 0) c_cx,\n" + 
"               IFNULL(SUM(c_ts), 0) c_ts,\n" + 
"               IFNULL(SUM(c_xt), 0) c_xt,\n" + 
"               IFNULL(SUM(c_tz), 0) c_tz,\n" + 
"               IFNULL(SUM(c_gz), 0) c_gz,\n" + 
"               IFNULL(SUM(c_sj), 0) c_sj,\n" + 
"               IFNULL(SUM(c_ysl), 0) c_ysl,\n" + 
"               IFNULL(SUM(c_xc), 0) c_xc,\n" + 
"               IFNULL(ROUND((SUM(c_zx) + SUM(c_cx) + SUM(c_ts) - SUM(l_cs)) * 100 / (SUM(c_zx) + SUM(c_cx) + SUM(c_ts)), 1), 100) l_cs,\n" + 
"               IFNULL(ROUND(SUM(l_cf) * 100 / (SUM(c_zx) + SUM(c_ts)), 1), 0) l_cf,\n" + 
"               IFNULL(ROUND(SUM(l_sj) * 100 / (SUM(c_zx) + SUM(c_ts)), 1), 0) l_sj\n" + 
"          FROM cc_view_routine_work\n" + 
"         WHERE work_org IN (SELECT a.org_id\n" + 
"                              FROM tsm_organization a, tsm_organization b\n" + 
"                             WHERE CONCAT(a.linkid, '-') LIKE CONCAT(b.linkid, '-%')\n" + 
"                               AND b.org_id IN (viewOrg))) x,\n" + 
"       (SELECT IFNULL(ROUND((SUM(zx_z) + SUM(zx_zx) + SUM(cx_z) + SUM(cx_zx) + SUM(ts1_z) + SUM(ts1_zx)) * 100 / (SUM(zx) + SUM(cx) + SUM(ts1)), 1), 100) l_jj\n" + 
"          FROM cc_view_summary_work\n" + 
"         WHERE deal_org_id IN (SELECT a.org_id\n" + 
"                                 FROM tsm_organization a, tsm_organization b\n" + 
"                                WHERE CONCAT(a.linkid, '-') LIKE CONCAT(b.linkid, '-%')\n" + 
"                                  AND b.org_id IN (viewOrg))) y";
		workSql = workSql.replace("viewOrg", viewOrg);
		List works = this.jt.queryForList(workSql);//CodeSec未验证的SQL注入；CodeSec误报：1
		if (!works.isEmpty()) {
			returnMap = (Map) works.get(0);
		}
		return returnMap;
	}

	private Map getWorkViewThreeOra(String viewOrg) {
		Map returnMap = new HashMap();
		String workSql = 
"SELECT s_zx, s_cx, s_ts1, s_ts2, s_ts3, c_zx, c_cx, c_ts1, c_ts2, c_ts3, l_cs, l_jj, l_cf, l_sj\n" +
"  FROM (SELECT IFNULL(SUM(IF(service_type = 700006312, 1, 0)), 0) s_zx,\n" + 
"               IFNULL(SUM(IF(service_type = 720200003, 1, 0)), 0) s_cx,\n" + 
"               IFNULL(SUM(IF(service_type = 720130000 AND come_category = 707907001, 1, 0)), 0) s_ts1,\n" + 
"               IFNULL(SUM(IF(service_type = 720130000 AND come_category = 707907002, 1, 0)), 0) s_ts2,\n" + 
"               IFNULL(SUM(IF(service_type = 720130000 AND come_category = 707907003, 1, 0)), 0) s_ts3\n" + 
"          FROM cc_rpt_service_order\n" + 
"         WHERE service_type IN (700006312, 720200003, 720130000)\n" + 
"           AND accept_date BETWEEN DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-01') AND\n" + 
"               DATE_FORMAT(LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 DAY)), '%Y-%m-%d 23:59:59')\n" + 
"           AND region_id IN (SELECT a.region_id\n" + 
"                               FROM trm_region a, trm_region b, tsm_organization c\n" + 
"                              WHERE a.region_level IN ('97B', '97C')\n" + 
"                                AND CONCAT(a.link_id, '-') LIKE CONCAT(b.link_id, '-%')\n" + 
"                                AND b.region_id = c.region_id\n" + 
"                                AND c.org_id IN (viewOrg))) x,\n" + 
"       (SELECT IFNULL(SUM(zx), 0) c_zx,\n" + 
"               IFNULL(SUM(cx), 0) c_cx,\n" + 
"               IFNULL(SUM(ts1), 0) c_ts1,\n" + 
"               IFNULL(SUM(ts2), 0) c_ts2,\n" + 
"               IFNULL(SUM(ts3), 0) c_ts3,\n" + 
"               IFNULL(ROUND((SUM(zx) + SUM(cx) + SUM(ts1) + SUM(ts2) + SUM(ts3) - SUM(zx_cs) - SUM(cx_cs) - SUM(ts1_cs) - SUM(ts2_cs) - SUM(ts3_cs)) * 100 /\n" + 
"                            (SUM(zx) + SUM(cx) + SUM(ts1) + SUM(ts2) + SUM(ts3)),\n" + 
"                            1),\n" + 
"                      100) l_cs,\n" + 
"               IFNULL(ROUND((SUM(zx_z) + SUM(zx_zx) + SUM(cx_z) + SUM(cx_zx) + SUM(ts1_z) + SUM(ts1_zx)) * 100 / (SUM(zx) + SUM(cx) + SUM(ts1)), 1), 100) l_jj,\n" + 
"               IFNULL(ROUND((SUM(zx_cf) + SUM(ts1_cf) + SUM(ts2_cf) + SUM(ts3_cf)) * 100 / (SUM(zx) + SUM(ts1) + SUM(ts2) + SUM(ts3)), 1), 0) l_cf,\n" + 
"               IFNULL(ROUND((SUM(zx_sj) + SUM(ts1_sj) + SUM(ts2_sj) + SUM(ts3_sj)) * 100 / (SUM(zx) + SUM(ts1) + SUM(ts2) + SUM(ts3)), 1), 0) l_sj\n" + 
"          FROM cc_view_summary_work\n" + 
"         WHERE deal_org IN (viewOrg)) y";
		workSql = workSql.replace("viewOrg", viewOrg);
		List works = this.jt.queryForList(workSql);//CodeSec未验证的SQL注入；CodeSec误报：1
		if (!works.isEmpty()) {
			returnMap = (Map) works.get(0);
		}
		return returnMap;
	}

	private Map getWorkViewFiveOra() {
		Map returnMap = new HashMap();
		String workSql = 
"SELECT s_zx,\n" +
"       s_cx,\n" + 
"       s_ts1,\n" + 
"       s_ts2,\n" + 
"       s_ts3,\n" + 
"       IFNULL(ROUND((s_cx - cx_cs) * 100 / s_cx, 1), 100) l_cx_cs,\n" + 
"       IFNULL(ROUND((s_zx + s_ts1 + s_ts2 + s_ts3 - a_cs) * 100 / (s_zx + s_ts1 + s_ts2 + s_ts3), 1), 100) l_ts_cs,\n" + 
"       IFNULL(ROUND(a_cf * 100 / (s_zx + s_ts1 + s_ts2 + s_ts3), 1), 0) l_cf,\n" + 
"       IFNULL(ROUND(a_sj * 100 / (s_zx + s_ts1 + s_ts2 + s_ts3), 1), 0) l_sj\n" + 
"  FROM (SELECT IFNULL(SUM(IF(service_type = 700006312, 1, 0)), 0) s_zx,\n" + 
"               IFNULL(SUM(IF(service_type = 720200003, 1, 0)), 0) s_cx,\n" + 
"               IFNULL(SUM(IF(service_type = 720130000 AND come_category = 707907001, 1, 0)), 0) s_ts1,\n" + 
"               IFNULL(SUM(IF(service_type = 720130000 AND come_category = 707907002, 1, 0)), 0) s_ts2,\n" + 
"               IFNULL(SUM(IF(service_type = 720130000 AND come_category = 707907003, 1, 0)), 0) s_ts3,\n" + 
"               IFNULL(SUM(IF(service_type = 720200003 AND over_time_flag = 1, 1, 0)), 0) cx_cs,\n" + 
"               IFNULL(SUM(IF(service_type IN (700006312, 720130000) AND over_time_flag = 1, 1, 0)), 0) a_cs,\n" + 
"               IFNULL(SUM(IF(service_type IN (700006312, 720130000) AND passive_repeat_flag = 1, 1, 0)), 0) a_cf,\n" + 
"               IFNULL(SUM(IF(service_type IN (700006312, 720130000) AND passive_upgrade_flag = 1, 1, 0)), 0) a_sj\n" + 
"          FROM cc_rpt_service_order\n" + 
"         WHERE service_type IN (700006312, 720200003, 720130000)\n" + 
"           AND accept_date BETWEEN DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), '%Y-%m-01') AND\n" + 
"               DATE_FORMAT(LAST_DAY(DATE_SUB(CURDATE(), INTERVAL 1 DAY)), '%Y-%m-%d 23:59:59')) x";
		List works = this.jt.queryForList(workSql);
		if (!works.isEmpty()) {
			returnMap = (Map) works.get(0);
		}
		return returnMap;
	}

    public String queryCurOrder(String curStaff, int curType) {
        String curOrder = "";
        String selectCurOrderSql = "SELECT CUR_ORDER FROM cc_cur_work_order WHERE cur_staff = ? AND cur_type = ?";
        List curOrders = this.jt.queryForList(selectCurOrderSql, curStaff, curType);
        if (!curOrders.isEmpty()) {
            Map co = (Map) curOrders.get(0);
            curOrder = co.get("CUR_ORDER") == null ? "" : co.get("CUR_ORDER").toString();
        }
        return curOrder;
    }

    public int saveCurOrder(String curStaff, int curType, String curOrder) {
        String deleteCurOrderSql = "DELETE FROM cc_cur_work_order WHERE cur_staff = ? AND cur_type = ?";
        this.jt.update(deleteCurOrderSql, curStaff, curType);
        String insertCurOrderSql = "INSERT INTO cc_cur_work_order (cur_staff, cur_type, create_date, cur_order) VALUES (?, ?, NOW(), ?)";
        return this.jt.update(insertCurOrderSql, curStaff, curType, curOrder);
    }

    @Override
    public Map qrySheetList(String sql) {
        Map map = new HashMap();
        String[] sqlList = sql.split("@");
        List<Map<String, Object>> list = jt.queryForList(sqlList[0]);//CodeSec未验证的SQL注入；CodeSec误报：2
        List<Map<String, Object>> count = jt.queryForList(sqlList[1]);//CodeSec未验证的SQL注入；CodeSec误报：2
        map.put("list", list);
        map.put("count", count.get(0).get("COUNTS").toString());
        return map;
    }

    @Override
    public Map qryOrderAskList(Map param) {
        String trSerType = param.get("trSerType").toString();
        String regionId = param.get("regionId").toString();
        String acceptTimes = param.get("acceptTimes").toString();
        String archiveTimes = param.get("archiveTimes").toString();
        int currentPage = Integer.parseInt(param.get("currentPage").toString());
        int pageSize = Integer.parseInt(param.get("pageSize").toString());
        String orderType = param.get("orderType").toString();
        
        String baseSql = "SELECT %PARAM% FROM";
        StringBuilder countStr = new StringBuilder(baseSql);
        StringBuilder resultStr = new StringBuilder(baseSql);
        this.getWhere(false,countStr,orderType,trSerType,regionId,acceptTimes,archiveTimes);
        this.getWhere(true,resultStr,orderType,trSerType,regionId,acceptTimes,archiveTimes);
        String countSql = countStr.toString().replace("%PARAM%", "COUNT(1)");
        String resultSql = resultStr.toString().replace("%PARAM%","e.UNIFIED_COMPLAINT_CODE,c.SERVICE_ORDER_ID," +
                "c.ORDER_STATU,c.ORDER_STATU_DESC,c.MONTH_FLAG,DATE_FORMAT(c.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,c.PROD_NUM," +
                "c.SERVICE_TYPE_DESC,f.BEST_ORDER_DESC,c.ACCEPT_STAFF_NAME,c.ACCEPT_COUNT,c.REGION_ID," +
                "(SELECT COUNT(1) FROM " + ("his".equals(orderType) ? "CC_HASTEN_SHEET_HIS" : "CC_HASTEN_SHEET") + " H WHERE H.SERVICE_ORDER_ID = c.SERVICE_ORDER_ID) AS CUIDANCOUNT," +
                "c.REGION_NAME,c.ACCEPT_ORG_NAME,g.CUST_SERV_GRADE_NAME AS CUST_GRADE_NAME,f.APPEAL_PROD_NAME AS APPEAL_PROD_NAME," +
                "(select b.logonname from tsm_staff b where b.staff_id=ACCEPT_STAFF_ID) as LOGONNAME,"+
                "c.ORDER_VESION,DATE_FORMAT(c.MODIFY_DATE,'%Y-%m-%d %H:%i:%s') AS MODIFY_DATE");
        String orderByStr = this.getOrderByStr(trSerType, acceptTimes, archiveTimes);
        GridDataInfo result = this.dbgridDataPub.getResultNewBySize(countSql, resultSql, currentPage, pageSize, orderByStr, "四类工单查询");
        Map map = new HashMap();
        map.put("list",result.getList());
        map.put("count",result.getQuryCount());
        return map;
    }

    private void getWhere(boolean resultFlag,StringBuilder sql,String orderType,String trSerType,String regionId,String acceptTimes,String archiveTimes){
        if("his".equals(orderType)){
        	this.setHisWhere(resultFlag, sql, trSerType);
        }else if("now".equals(orderType)){
            this.setNowWhere(resultFlag, sql, trSerType);
        }
        sql.append(" and c.SERVICE_TYPE = " + trSerType);
        if(StringUtils.isNotBlank(regionId)){
            sql.append(" and c.REGION_ID = " + regionId);
        }
       //商机单受理时间和办结时间条件
        if("700001171".equals(trSerType) || "600000074".equals(trSerType)){
            if(StringUtils.isNotBlank(acceptTimes)){
                ArrayList<String> list = JSON.parseObject(acceptTimes,new TypeReference<ArrayList<String>>(){});
                sql.append(" and c.ACCEPT_DATE >= '"+list.get(0)+"' ");
                sql.append(" and c.ACCEPT_DATE <= '"+list.get(1)+"'");
            }
            if(StringUtils.isNotBlank(archiveTimes)){
                ArrayList<String> list = JSON.parseObject(archiveTimes,new TypeReference<ArrayList<String>>(){});
                sql.append(" and c.FINISH_DATE >= '"+list.get(0)+"' ");
                sql.append(" and c.FINISH_DATE <= '"+list.get(1)+"'");
            }
        }else{
            if(StringUtils.isNotBlank(acceptTimes)){
                ArrayList<String> list = JSON.parseObject(acceptTimes,new TypeReference<ArrayList<String>>(){});
                sql.append(" and d.CREATE_DATE >= '"+list.get(0)+"' ");
                sql.append(" and d.CREATE_DATE <= '"+list.get(1)+"' ");
                sql.append(" and c.ACCEPT_DATE >= '"+list.get(0)+"' ");
                sql.append(" and c.ACCEPT_DATE <= '"+list.get(1)+"' ");
            }
            if(StringUtils.isNotBlank(archiveTimes)){
                ArrayList<String> list = JSON.parseObject(archiveTimes,new TypeReference<ArrayList<String>>(){});
                sql.append(" and d.ARCHIVE_DATE >= '"+list.get(0)+"' ");
                sql.append(" and d.ARCHIVE_DATE <= '"+list.get(1)+"' ");
                sql.append(" and c.FINISH_DATE >= '"+list.get(0)+"' ");
                sql.append(" and c.FINISH_DATE <= '"+list.get(1)+"' ");
            }
        }
    }
    
    private String getOrderByStr(String trSerType,String acceptTimes,String archiveTimes) {
    	String whereStr = " ORDER BY C.ACCEPT_DATE";
    	//商机单受理时间和办结时间条件
        if("700001171".equals(trSerType) || "600000074".equals(trSerType)){
            if(StringUtils.isNotBlank(archiveTimes)){
                whereStr = " ORDER BY C.FINISH_DATE";
            }
        }else{
            if(StringUtils.isNotBlank(acceptTimes)){
                whereStr = " ORDER BY d.CREATE_DATE";
            }
            if(StringUtils.isNotBlank(archiveTimes)){
                whereStr = " ORDER BY d.ARCHIVE_DATE";
            }
        }
        return whereStr;
    }
    
    private void setHisWhere(boolean resultFlag,StringBuilder sql,String trSerType) {
    	if(!"700001171".equals(trSerType) && !"600000074".equals(trSerType)){
            sql.append(" dapd_sheet_info d,");
        }
        sql.append(" cc_service_order_ask_his c");
        if(resultFlag) {
        	sql.append(" LEFT JOIN CC_CMP_UNIFIED_RETURN_HIS e ON c.SERVICE_ORDER_ID = e.COMPLAINT_WORKSHEET_ID "
        			+ "LEFT JOIN CC_SERVICE_CONTENT_ASK_HIS f ON c.SERVICE_ORDER_ID = f.SERVICE_ORDER_ID "
        			+ "LEFT JOIN CC_ORDER_CUST_INFO_HIS g ON c.CUST_GUID = g.CUST_GUID");
        }
        sql.append(" WHERE 1=1");
        if(!"700001171".equals(trSerType) && !"600000074".equals(trSerType)){
            sql.append(" and c.SERVICE_ORDER_ID = d.SHEET_ID_PROV");
//            sql.append(" and c.ACCEPT_DATE >= '2023-10-30 21:02:45'");
        }
        
        if("720130000".equals(trSerType)){
            sql.append(" and c.order_statu in (720130010,720130002)");
        }else{
            sql.append(" and c.order_statu = 700000103");
        }
    }
    
    private void setNowWhere(boolean resultFlag,StringBuilder sql,String trSerType) {
    	if(!"700001171".equals(trSerType) && !"600000074".equals(trSerType)){
            sql.append(" dapd_sheet_info d,");
        }
        sql.append(" cc_service_order_ask c");
        if(resultFlag) {
        	sql.append(" LEFT JOIN CC_CMP_UNIFIED_RETURN e ON c.SERVICE_ORDER_ID = e.COMPLAINT_WORKSHEET_ID "
        			+ "LEFT JOIN CC_SERVICE_CONTENT_ASK f ON c.SERVICE_ORDER_ID = f.SERVICE_ORDER_ID "
        			+ "LEFT JOIN CC_ORDER_CUST_INFO g ON c.CUST_GUID = g.CUST_GUID");
        }
        sql.append(" WHERE 1=1");
        if(!"700001171".equals(trSerType) && !"600000074".equals(trSerType)){
            sql.append(" and c.SERVICE_ORDER_ID = d.SHEET_ID_PROV");
//            sql.append(" and c.ACCEPT_DATE >= '2023-10-30 21:02:45'");
        }
        if("720130000".equals(trSerType)){
            sql.append(" and c.order_statu <> 720130001");
        }else{
            sql.append(" and c.order_statu <> 700000099");
        }
    }

    @Override
    public Map qrCliqueList(String sql) {
        Map<String, Object> map = new HashMap();
        String[] sqlList = sql.split("@");
        List<Map<String, Object>> list = jt.queryForList(sqlList[0]);
        List<Map<String, Object>> count = jt.queryForList(sqlList[1]);
        map.put("list", list);
        map.put("count", count.get(0).get("COUNTS").toString());
        return map;
    }

    @Override
    public Map getSpecialInfoStr(String tableType, String sql) {
        Map map = new HashMap();
        String[] sqlList = sql.split("@");
        List<Map<String, Object>> list = pubjt.queryForList(sqlList[0]);
        int count = pubjt.queryForObject(sqlList[1], Integer.class);
        map.put("list", list);
        map.put("count", count);
        return map;
    }

    @Override
    public int addSpecialInf(Map param) {
        String sql = "insert into cs_custom_special_info(source_num,area_code,special_info,state,region_id,create_staff,active_type)" +
                "values(?,?,?,?,?,?,?)";
        String sourceNum = param.get("sourceNum").toString();
        String specialInfo = param.get("special_info").toString();
        String regionId = param.get("region_id").toString();
        int areaId = Integer.parseInt(regionId);
        String createStaff = param.get("create_staff").toString();
        String activeType = param.get("active_type").toString();
        String areaCode = param.get("area_code").toString();
        String state = "0";
        return pubjt.update(sql, sourceNum, areaCode, specialInfo, state, areaId, new Integer(createStaff), new Integer(activeType));
    }

    @Override
    public int addSpecialIvr(Map param) {
        String sql = "insert into jscsc_ct_ivr.ccs_st_special_customer(prod_number,area_code,state,type,create_staff,region_id,type_2) values(?,?,?,?,?,?,?)";
        String sourceNum = param.get("sourceNum").toString();
        String activeType = param.get("active_type").toString();
        String areaCode = param.get("area_code").toString();
        String createStaff = param.get("create_staff").toString();
        String regionId = param.get("region_id").toString();
        int areaId = Integer.parseInt(regionId);
        String state = "1";
        String type2 = "0";
        //高龄类型 ‘3’  额外往type_2存1
        if ("3".equals(activeType)) {
            activeType = "1";
            type2 = "1";
        }
        return pubjt.update(sql, sourceNum, areaCode, state, activeType, new Integer(createStaff), areaId, type2);
    }

    @Override
    public int removeSpecial(Map param) {
        String type = param.get("type").toString();
        String sourceNum = param.get("sourceNum").toString();
        String sql = "";
        int num = 0;
        if ("0".equals(type)) {
            sql = "delete from cs_custom_special_info where source_num='" + sourceNum + "'";
            num = pubjt.update(sql);
        } else if ("1".equals(type)) {
            sql = "delete from jscsc_ct_ivr.ccs_st_special_customer where prod_number='" + sourceNum + "'";
            num = pubjt.update(sql);
        }
        return num;
    }

    @Override
    public int updateSpecialIvr(Map param) {
        String sourceNum = param.get("sourceNum").toString();
        String areaCode = param.get("area_code").toString();
        String activeType = param.get("active_type").toString();
        String regionId = param.get("region_id").toString();
        int areaId = Integer.parseInt(regionId);
        String createStaff = param.get("create_staff").toString();
        String sql = "update jscsc_ct_ivr.ccs_st_special_customer t set t.area_code=?,t.type=?,t.create_staff=?,t.type_2=?," +
                "t.create_date=now(),t.region_id=? where t.prod_number=?";
        String type2 = "0";
        if ("3".equals(activeType)) {
            activeType = "1";
            type2 = "1";
        }
        return pubjt.update(sql, areaCode, activeType, new Integer(createStaff), type2, areaId, sourceNum);
    }

    @Override
    public int updateSpecialInfo(Map param) {
        String sourceNum = param.get("sourceNum").toString();
        String specialInfo = param.get("special_info").toString();
        String regionId = param.get("region_id").toString();
        int areaId = Integer.parseInt(regionId);
        String createStaff = param.get("create_staff").toString();
        String activeType = param.get("active_type").toString();
        String areaCode = param.get("area_code").toString();
        String sql = "update cs_custom_special_info t set t.area_code=?,t.special_info=?,t.region_id=?,t.create_staff=?," +
                "t.active_type=?,t.create_date=NOW() where t.source_num=?";
        return pubjt.update(sql, areaCode, specialInfo, areaId, new Integer(createStaff), new Integer(activeType), sourceNum);
    }

    public boolean isZHYXBusinessOrder(String orderId) {
        String strSql = "select count(1) from cc_ynsj_orderinfo c where c.service_order_id=? and c.order_type='ZHYX' and c.action_result='1'";
        int count = jt.queryForObject(strSql, new Object[]{orderId}, Integer.class);
        return count > 0;
    }

    public String isBusinessOrder(String orderId) {
        Map tmpMap = null;
        String strSql = "select c.ORDER_TYPE,c.ORDERID from cc_ynsj_orderinfo c where c.service_order_id=? and c.action_result='1'";
        List list = jt.queryForList(strSql, orderId);
        if (!list.isEmpty()) {
            tmpMap = (Map) list.get(0);
        }
        return ResultUtil.success(tmpMap);
    }

    @Override
    public List getGzOorderListByNewId(String orderId) {
        String sql = "SELECT C.NEW_ORDER_ID,C.OLD_ORDER_ID,C.TRACK_TYPE,DATE_FORMAT(C.CREATE_DATE,'%Y-%m-%d %H:%i:%s') CREATE_DATE,C.REFUND_MODE_DESC,"
                + "C.DEAL_MODE,C.REFUND_REASON_DESC,C.REFUND_AMOUNT,C.REFUND_CONTENT,C.PAID_REASON_DESC,C.PAID_AMOUNT,C.PAID_CONTENT,C.OP_STAFF,C.RECHARGE_MODE_DESC,C.RECHARGE_NUMBER,"
                + "C.REFUND_NUMBER,C.APPROVE_PERSON,C.OWNER_NAME,DATE_FORMAT(C.FORM_DATE,'%Y-%m-%d %H:%i:%s') FORM_DATE,"
                + "CASE WHEN A.SERVICE_ORDER_ID IS NULL THEN '1' ELSE '0' END HIS_FLAG FROM CC_SERVICE_TRACK_TZ C LEFT JOIN CC_SERVICE_ORDER_ASK A ON "
                + "C.NEW_ORDER_ID=A.SERVICE_ORDER_ID WHERE C.OLD_ORDER_ID IN(SELECT T.OLD_ORDER_ID FROM CC_SERVICE_TRACK_TZ T"
                + " WHERE T.NEW_ORDER_ID=?) ORDER BY C.CREATE_DATE";
        return jt.queryForList(sql, orderId);
    }

    @Override
    public List getGzOorderListByOldId(String orderId) {
        String sql = "SELECT C.NEW_ORDER_ID,C.OLD_ORDER_ID,C.TRACK_TYPE,DATE_FORMAT(C.CREATE_DATE,'%Y-%m-%d %H:%i:%s') CREATE_DATE,C.REFUND_MODE_DESC,"
                + "C.DEAL_MODE,C.REFUND_REASON_DESC,C.REFUND_AMOUNT,C.REFUND_CONTENT,C.PAID_REASON_DESC,C.PAID_AMOUNT,C.PAID_CONTENT,C.OP_STAFF,C.RECHARGE_MODE_DESC,C.RECHARGE_NUMBER,"
                + "C.REFUND_NUMBER,C.APPROVE_PERSON,C.OWNER_NAME,DATE_FORMAT(C.FORM_DATE,'%Y-%m-%d %H:%i:%s') FORM_DATE,"
                + "CASE WHEN A.SERVICE_ORDER_ID IS NULL THEN '1' ELSE '0' END HIS_FLAG FROM CC_SERVICE_TRACK_TZ C LEFT JOIN CC_SERVICE_ORDER_ASK A ON "
                + "C.NEW_ORDER_ID=A.SERVICE_ORDER_ID WHERE C.OLD_ORDER_ID=? ORDER BY C.CREATE_DATE";
        return jt.queryForList(sql, orderId);
    }

    public boolean isExistGZOrder(String orderId) {
        String strSql = "select count(1) from cc_service_track_tz c where c.old_order_id=?";
        int count = jt.queryForObject(strSql, new Object[]{orderId}, Integer.class);
        return count > 0;
    }

    /**
     * 预受理是否成功,成功的话写CRM定单号
     */
    public int updateHisCrmAskSheet(OrderAskInfo orderAskInfo) {
        String strSql = "UPDATE CC_SERVICE_ORDER_ASK_HIS A SET A.ACCEPT_CHANNEL_ID=?,A.ACCEPT_CHANNEL_DESC=? WHERE A.MONTH_FLAG=? AND A.SERVICE_ORDER_ID=? AND A.ORDER_VESION=?";
        return this.jt.update(strSql,
                orderAskInfo.getAskChannelId(),
                StringUtils.defaultIfEmpty(orderAskInfo.getAskChannelDesc(), null),
                orderAskInfo.getMonth(),
                StringUtils.defaultIfEmpty(orderAskInfo.getServOrderId(), null),
                orderAskInfo.getOrderVer()
        );
    }

    public String getBusinessOrderId(String orderId) {
        String servOrderId = "";
        String strSql = "select c.SERVICE_ORDER_ID from cc_ynsj_orderinfo c where c.orderid=? and c.action_result='1'";
        List list = jt.queryForList(strSql, orderId);
        if (!list.isEmpty()) {
            Map tmpMap = (Map) list.get(0);
            servOrderId = tmpMap.get("SERVICE_ORDER_ID").toString();
        }
        return servOrderId;
    }

    @Override
    public int getPreferAppealFlag(String orderId, String accNum, int region) {
        int preferAppealFlag = 0;
        String curSql =
                "SELECT COUNT(1)\n" +
                        "  FROM cc_service_order_ask\n" +
                        " WHERE service_type = 720130000\n" +
                        "   AND come_category = 707907003\n" +
                        "   AND order_statu NOT IN (700000099, 720130001)\n" +
                        "   AND accept_date >= date_sub(now(),interval 180 day)\n" +
                        "   AND region_id = ?\n" +
                        "   AND prod_num = ?\n" +
                        "   AND service_order_id <> ?";
        String hisSql =
                "SELECT COUNT(1)\n" +
                        "  FROM cc_service_order_ask_his\n" +
                        " WHERE service_type = 720130000\n" +
                        "   AND come_category = 707907003\n" +
                        "   AND order_statu IN (700000103, 720130010)\n" +
                        "   AND accept_date >= date_sub(now(),interval 180 day)\n" +
                        "   AND region_id = ?\n" +
                        "   AND prod_num = ?\n" +
                        "   AND service_order_id <> ?";
        int curCount = this.jt.queryForObject(curSql, new Object[]{region, accNum, orderId}, Integer.class);
        if (curCount > 1) {
            preferAppealFlag = 1;
        } else {
            int hisCount = this.jt.queryForObject(hisSql, new Object[]{region, accNum, orderId}, Integer.class);
            if ((curCount + hisCount) > 1) {
                preferAppealFlag = 1;
            }
        }
        return preferAppealFlag;
    }

    @Override
    public int getPreferComplaintFlag(String orderId, String accNum, int region) {
        int preferComplaintFlag = 0;
        String curSql =
                "SELECT COUNT(1)\n" +
                        "  FROM cc_service_order_ask\n" +
                        " WHERE service_type = 720130000\n" +
                        "   AND order_statu NOT IN (700000099, 700000100, 720130001, 720130003)\n" +
                        "   AND accept_date >= date_sub(now(),interval 30 day)\n" +
                        "   AND region_id = ?\n" +
                        "   AND prod_num = ?\n" +
                        "   AND service_order_id <> ?";
        String hisSql =
                "SELECT COUNT(1)\n" +
                        "  FROM cc_service_order_ask_his\n" +
                        " WHERE service_type = 720130000\n" +
                        "   AND order_statu IN (700000103, 720130010)\n" +
                        "   AND accept_date >= date_sub(now(),interval 30 day)\n" +
                        "   AND region_id = ?\n" +
                        "   AND prod_num = ?\n" +
                        "   AND service_order_id <> ?";
        int curCount = this.jt.queryForObject(curSql, new Object[]{region, accNum, orderId}, Integer.class);
        if (curCount > 4) {
            preferComplaintFlag = 1;
        } else {
            int hisCount = this.jt.queryForObject(hisSql, new Object[]{region, accNum, orderId}, Integer.class);
            if (curCount + hisCount > 4) {
                preferComplaintFlag = 1;
            }
        }
        return preferComplaintFlag;
    }

    @Override
    public String getLastFinalOptionOrderId(String accNum, int region) {
        String lastFinalOptionOrderId = "";
        String sql =
                "SELECT SERVICE_ORDER_ID\n" +
                        "          FROM (SELECT a.service_order_id, a.accept_date\n" +
                        "                  FROM cc_service_order_ask a, cc_service_label b\n" +
                        "                 WHERE a.service_order_id = b.service_order_id\n" +
                        "                   AND b.final_option_flag IS NOT NULL\n" +
                        "                   AND a.service_type = 720130000\n" +
                        "                   AND a.order_statu NOT IN (700000099, 720130001)\n" +
                        "                   AND a.accept_date >= date_sub(now(),interval 30 day)\n" +
                        "                   AND a.region_id = ?\n" +
                        "                   AND a.prod_num = ?\n" +
                        "                UNION ALL\n" +
                        "                SELECT a.service_order_id, a.accept_date\n" +
                        "                  FROM cc_service_order_ask_his a, cc_service_label_his b\n" +
                        "                 WHERE a.service_order_id = b.service_order_id\n" +
                        "                   AND b.final_option_flag IS NOT NULL\n" +
                        "                   AND a.service_type = 720130000\n" +
                        "                   AND a.order_statu IN (700000103, 720130010)\n" +
                        "                   AND a.accept_date >= date_sub(now(),interval 30 day)\n" +
                        "                   AND a.region_id = ?\n" +
                        "                   AND a.prod_num = ?) AS R\n" +
                        "         ORDER BY accept_date DESC limit 1";
        List list = this.jt.queryForList(sql, region, accNum, region, accNum);
        if (!list.isEmpty()) {
            lastFinalOptionOrderId = ((Map) list.get(0)).get("SERVICE_ORDER_ID").toString();
        }
        return lastFinalOptionOrderId;
    }
    
    public int insertComplaintInfo(ComplaintInfo info) {
    	log.info("insertComplaintInfo info:{}", info);
    	boolean flag = StringUtils.isNotBlank(info.getAcceptDate());//受理时间不为空
    	String sql = "INSERT INTO cc_complaint_info(SERVICE_ORDER_ID, ACCEPT_DATE, COMPLAINT_WORKSHEET_ID, MIIT_CODE, COMPLAINT_DATE, MODIFY_DATE, COMPLAINT_SOURCE, FIRST_LEVEL, SECOND_LEVEL, THIRD_LEVEL, BIZ_CLASSIFY1,BIZ_CLASSIFY2,BIZ_CLASSIFY3,IP_ADDRESS,PROVINCIAL_OWNER) "
				+ "VALUES (?, now(), ?, ?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), now(),?,?,?,?,?,?,?,?,?)";
    	if(flag) {
    		sql = "INSERT INTO cc_complaint_info(SERVICE_ORDER_ID, ACCEPT_DATE, COMPLAINT_WORKSHEET_ID, MIIT_CODE, COMPLAINT_DATE, MODIFY_DATE, COMPLAINT_SOURCE, FIRST_LEVEL, SECOND_LEVEL, THIRD_LEVEL, BIZ_CLASSIFY1,BIZ_CLASSIFY2,BIZ_CLASSIFY3,IP_ADDRESS,PROVINCIAL_OWNER) "
    				+ "VALUES (?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), ?, ?, str_to_date(?, '%Y-%m-%d %H:%i:%s'), now(),?,?,?,?,?,?,?,?,?)";
    	}
        try {
        	if(flag) {
        		return jt.update(sql, info.getOrderId(), info.getAcceptDate(), StringUtils.defaultIfEmpty(info.getComplaintWorsheetId(), "0"), 
        				StringUtils.defaultIfEmpty(info.getMiitCode(), null), info.getComplaintDate(), StringUtils.defaultIfEmpty(info.getComplaintSource(), null), 
						StringUtils.defaultIfEmpty(info.getFirstLevel(), null), StringUtils.defaultIfEmpty(info.getSecondLevel(), null), 
						StringUtils.defaultIfEmpty(info.getThirdLevel(), null), StringUtils.defaultIfEmpty(info.getBizClassify1(), null),
                        StringUtils.defaultIfEmpty(info.getBizClassify2(), null),StringUtils.defaultIfEmpty(info.getBizClassify3(), null),
                        StringUtils.defaultIfEmpty(info.getIpAddress(), null),StringUtils.defaultIfEmpty(info.getProvincialOwner(), null));
        	} else {
        		return jt.update(sql, info.getOrderId(), StringUtils.defaultIfEmpty(info.getComplaintWorsheetId(), "0"), 
        				StringUtils.defaultIfEmpty(info.getMiitCode(), "0"), info.getComplaintDate(), StringUtils.defaultIfEmpty(info.getComplaintSource(), null), 
						StringUtils.defaultIfEmpty(info.getFirstLevel(), null), StringUtils.defaultIfEmpty(info.getSecondLevel(), null), 
						StringUtils.defaultIfEmpty(info.getThirdLevel(), null), StringUtils.defaultIfEmpty(info.getBizClassify1(), null),
                        StringUtils.defaultIfEmpty(info.getBizClassify2(), null),StringUtils.defaultIfEmpty(info.getBizClassify3(), null),
                        StringUtils.defaultIfEmpty(info.getIpAddress(), null),StringUtils.defaultIfEmpty(info.getProvincialOwner(), null));
        	}
		}
		catch(Exception e) {
			log.error("insertComplaintInfo mysql异常：{}", e.getMessage(), e);
		}
        return 0;
    }
    
    public Map getComplaintInfo(String orderId, boolean hisFlag) {
    	String sqlStr = "SELECT C.SERVICE_ORDER_ID,DATE_FORMAT(C.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,C.COMPLAINT_WORKSHEET_ID,C.MIIT_CODE,DATE_FORMAT(C.COMPLAINT_DATE,'%Y-%m-%d %H:%i:%s') COMPLAINT_DATE, "
    			+ "C.COMPLAINT_SOURCE, C.FIRST_LEVEL, C.SECOND_LEVEL, C.THIRD_LEVEL, C.BIZ_CLASSIFY1,C.BIZ_CLASSIFY2,C.BIZ_CLASSIFY3,C.IP_ADDRESS, IP_OPERATORS, UNREASON_SCENE, UNREASON_SCENE_DESC, IS_AGENT_COMPLAINT, REMARK,VALET_CMPLNT_MAP,SPCL_CLAIM_TYPE,IS_SAME_CLAIM_TYPE, "
    			+ "C.PROVINCIAL_OWNER FROM CC_COMPLAINT_INFO C WHERE C.SERVICE_ORDER_ID = ?";
    	if(hisFlag) {
        	sqlStr = "SELECT C.SERVICE_ORDER_ID,DATE_FORMAT(C.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,C.COMPLAINT_WORKSHEET_ID,C.MIIT_CODE,DATE_FORMAT(C.COMPLAINT_DATE,'%Y-%m-%d %H:%i:%s') COMPLAINT_DATE, "
        			+ "C.COMPLAINT_SOURCE, C.FIRST_LEVEL, C.SECOND_LEVEL, C.THIRD_LEVEL, C.BIZ_CLASSIFY1,C.BIZ_CLASSIFY2,C.BIZ_CLASSIFY3,C.IP_ADDRESS, IP_OPERATORS, UNREASON_SCENE, UNREASON_SCENE_DESC, IS_AGENT_COMPLAINT, REMARK,VALET_CMPLNT_MAP,SPCL_CLAIM_TYPE,IS_SAME_CLAIM_TYPE, "
        			+ "C.PROVINCIAL_OWNER FROM CC_COMPLAINT_INFO_HIS C WHERE C.SERVICE_ORDER_ID = ?";
    	}
    	List tmp = this.jt.queryForList(sqlStr, orderId);
        if (!tmp.isEmpty()) {
        	return (Map) tmp.get(0);
        }
        return Collections.emptyMap();
    }
    
    public List queryComplaintListByMiitCode(String miitCode, boolean hisFlag) {
    	String sqlStr = "SELECT C.SERVICE_ORDER_ID,DATE_FORMAT(C.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,C.COMPLAINT_WORKSHEET_ID,C.MIIT_CODE,DATE_FORMAT(C.COMPLAINT_DATE,'%Y-%m-%d %H:%i:%s') COMPLAINT_DATE, "
    			+ "C.COMPLAINT_SOURCE, C.FIRST_LEVEL, C.SECOND_LEVEL, C.THIRD_LEVEL, C.BIZ_CLASSIFY1,C.BIZ_CLASSIFY2,C.BIZ_CLASSIFY3,C.IP_ADDRESS, IP_OPERATORS, UNREASON_SCENE, UNREASON_SCENE_DESC, IS_AGENT_COMPLAINT, REMARK,VALET_CMPLNT_MAP,SPCL_CLAIM_TYPE,IS_SAME_CLAIM_TYPE, "
    			+ "C.PROVINCIAL_OWNER FROM CC_COMPLAINT_INFO C WHERE C.MIIT_CODE = ?";
    	if(hisFlag) {
        	sqlStr = "SELECT C.SERVICE_ORDER_ID,DATE_FORMAT(C.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') ACCEPT_DATE,C.COMPLAINT_WORKSHEET_ID,C.MIIT_CODE,DATE_FORMAT(C.COMPLAINT_DATE,'%Y-%m-%d %H:%i:%s') COMPLAINT_DATE, "
        			+ "C.COMPLAINT_SOURCE, C.FIRST_LEVEL, C.SECOND_LEVEL, C.THIRD_LEVEL, C.BIZ_CLASSIFY1,C.BIZ_CLASSIFY2,C.BIZ_CLASSIFY3,C.IP_ADDRESS, IP_OPERATORS, UNREASON_SCENE, UNREASON_SCENE_DESC, IS_AGENT_COMPLAINT, REMARK,VALET_CMPLNT_MAP,SPCL_CLAIM_TYPE,IS_SAME_CLAIM_TYPE, "
        			+ "C.PROVINCIAL_OWNER FROM CC_COMPLAINT_INFO_HIS C WHERE C.MIIT_CODE = ?";
    	}
    	return this.jt.queryForList(sqlStr, miitCode);
    }
    
    public int updateComplaintInfo(ComplaintInfo info) {
    	String sql = "UPDATE CC_COMPLAINT_INFO SET MIIT_CODE = ?, COMPLAINT_DATE = str_to_date(?, '%Y-%m-%d %H:%i:%s'), MODIFY_DATE = NOW(), "
    			+ "COMPLAINT_SOURCE = ?, FIRST_LEVEL = ?, SECOND_LEVEL = ?, THIRD_LEVEL = ?, BIZ_CLASSIFY1 = ? , BIZ_CLASSIFY2 = ? , BIZ_CLASSIFY3 = ? ,IP_ADDRESS = ?, PROVINCIAL_OWNER = ? WHERE SERVICE_ORDER_ID = ?";
        try {
            return jt.update(sql, StringUtils.defaultIfEmpty(info.getMiitCode(), null), info.getComplaintDate(), 
            		StringUtils.defaultIfBlank(info.getComplaintSource(), null), StringUtils.defaultIfBlank(info.getFirstLevel(), null), 
            		StringUtils.defaultIfBlank(info.getSecondLevel(), null), StringUtils.defaultIfBlank(info.getThirdLevel(), null),
                    StringUtils.defaultIfEmpty(info.getBizClassify1(), null), StringUtils.defaultIfEmpty(info.getBizClassify2(), null),
                    StringUtils.defaultIfEmpty(info.getBizClassify3(), null), StringUtils.defaultIfEmpty(info.getIpAddress(), null),
                    StringUtils.defaultIfEmpty(info.getProvincialOwner(), null), info.getOrderId());
		}
		catch(Exception e) {
			log.error("updateComplaintInfo mysql异常：{}", e.getMessage(), e);
		}
        return 0;
    }
    
    public int updateOtherComplaintInfo(ComplaintInfo info) {
    	String sql = "UPDATE CC_COMPLAINT_INFO SET MODIFY_DATE = NOW(), IP_OPERATORS = ?, UNREASON_SCENE = ?, "
    			+ "UNREASON_SCENE_DESC = ?, IS_AGENT_COMPLAINT = ?, REMARK = ?,VALET_CMPLNT_MAP = ?," +
                "SPCL_CLAIM_TYPE = ?, IS_SAME_CLAIM_TYPE = ? WHERE SERVICE_ORDER_ID = ?";
        try {
            return jt.update(sql, StringUtils.defaultIfEmpty(info.getIpOperators(), null), StringUtils.defaultIfEmpty(info.getUnreasonScene(), null), 
    				StringUtils.defaultIfEmpty(info.getUnreasonSceneDesc(), null), info.getIsAgentComplaint(), 
    				StringUtils.defaultIfEmpty(info.getRemark(), null),StringUtils.defaultIfEmpty(info.getValetCmplntMap(), null),
                    StringUtils.defaultIfEmpty(info.getSpclClaimType(), null),StringUtils.defaultIfEmpty(info.getIsSameClaimType(), null),info.getOrderId());
		}
		catch(Exception e) {
			log.error("updateComplaintInfo mysql异常：{}", e.getMessage(), e);
		}
        return 0;
    }
    
	public int insertComplaintInfoHisByOrderId(String orderId) {
		String deleteSql = "DELETE FROM cc_complaint_info WHERE SERVICE_ORDER_ID=?";
		try {
			String insertHisSql = "INSERT INTO cc_complaint_info_his(SERVICE_ORDER_ID, ACCEPT_DATE, COMPLAINT_WORKSHEET_ID, MIIT_CODE, COMPLAINT_DATE, MODIFY_DATE, "
					+ "COMPLAINT_SOURCE, FIRST_LEVEL,SECOND_LEVEL, THIRD_LEVEL, BIZ_CLASSIFY1,BIZ_CLASSIFY2,BIZ_CLASSIFY3,IP_ADDRESS, IP_OPERATORS, UNREASON_SCENE, UNREASON_SCENE_DESC, IS_AGENT_COMPLAINT, REMARK,VALET_CMPLNT_MAP,SPCL_CLAIM_TYPE,IS_SAME_CLAIM_TYPE,PROVINCIAL_OWNER) "
					+ "SELECT SERVICE_ORDER_ID, ACCEPT_DATE, COMPLAINT_WORKSHEET_ID, MIIT_CODE, COMPLAINT_DATE, MODIFY_DATE, COMPLAINT_SOURCE, FIRST_LEVEL, SECOND_LEVEL, "
					+ "THIRD_LEVEL, BIZ_CLASSIFY1,BIZ_CLASSIFY2,BIZ_CLASSIFY3,IP_ADDRESS, IP_OPERATORS, UNREASON_SCENE, UNREASON_SCENE_DESC, IS_AGENT_COMPLAINT, REMARK,VALET_CMPLNT_MAP,SPCL_CLAIM_TYPE,IS_SAME_CLAIM_TYPE,PROVINCIAL_OWNER FROM cc_complaint_info WHERE SERVICE_ORDER_ID=?";
			int j = jt.update(insertHisSql, orderId);
			if(j > 0) {
				return jt.update(deleteSql, orderId);
			}
		}
		catch(Exception e) {
			log.error("insertComplaintInfoHisByOrderId mysql异常：{}", e.getMessage(), e);
		}
		return 0;
	}
	
	public int updateOrderRelationFinish(String orderId) {
		String updateSql = "update cc_order_relation c set c.status = 1 where c.SERVICE_ORDER_ID = ?";
		try {
			return jt.update(updateSql, orderId);
		}
		catch(Exception e) {
			log.error("updateOrderRelationFinish {} mysql异常：{}", orderId, e.getMessage(), e);
		}
		return 0;
	}
	
	public int savePreOrderResult(String serviceOrderId, String orderId, String goodsId, String goodsName) {
		String insertSql = "insert into cc_preorder_result(SERVICE_ORDER_ID, ORDER_ID, GOODS_ID, GOODS_NAME) VALUES (?, ?, ?, ?)";
		try {
			return jt.update(insertSql, serviceOrderId, orderId, goodsId, goodsName);
		}
		catch(Exception e) {
			log.error("savePreOrderResult {} mysql异常：{}", serviceOrderId, e.getMessage(), e);
		}
		return 0;
	}
	
	public String getPreOrderId(String orderId) {
        String servOrderId = "";
        String strSql = "select c.SERVICE_ORDER_ID from cc_preorder_result c where c.ORDER_ID=? and c.DEAL_FLAG is null";
        List list = jt.queryForList(strSql, orderId);
        if (!list.isEmpty()) {
            Map tmpMap = (Map) list.get(0);
            servOrderId = tmpMap.get("SERVICE_ORDER_ID").toString();
        }
        return servOrderId;
    }
	
	public int updatePreOrderResult(PreOrderResult result) {
		String updateSql = "update cc_preorder_result c set c.DEAL_FLAG = ?, c.IS_BSS_ACCEPT = ?, "
				+ "c.BSS_WORK_ORDER_ID = ?, c.DEAL_CONTENT = ? where c.ORDER_ID = ? and c.SERVICE_ORDER_ID = ?";
		try {
			return jt.update(updateSql, 
					result.getDealFlag(),
					result.getIsBssAccept(),
					StringUtils.defaultIfBlank(result.getBssWorkOrderId(), null),
					StringUtils.defaultIfBlank(result.getDealContent(), null),
					result.getOrderId(),
					result.getServiceOrderId());
		}
		catch(Exception e) {
			log.error("updatePreOrderResult {} mysql异常：{}", result.getServiceOrderId(), e.getMessage(), e);
		}
		return 0;
	}
	
	public int updateComplaintInfoByList(ComplaintInfo info) {
    	String sql = "UPDATE CC_COMPLAINT_INFO SET COMPLAINT_DATE = str_to_date(?, '%Y-%m-%d %H:%i:%s'), MODIFY_DATE = NOW(), "
    			+ "COMPLAINT_SOURCE = ?, FIRST_LEVEL = ?, SECOND_LEVEL = ?, THIRD_LEVEL = ?, IP_ADDRESS = ? WHERE MIIT_CODE = ?";
        try {
            return jt.update(sql, StringUtils.defaultIfBlank(info.getComplaintDate(), null), StringUtils.defaultIfBlank(info.getComplaintSource(), null), 
    				StringUtils.defaultIfBlank(info.getFirstLevel(), null), StringUtils.defaultIfBlank(info.getSecondLevel(), null), 
    				StringUtils.defaultIfBlank(info.getThirdLevel(), null), StringUtils.defaultIfEmpty(info.getIpAddress(), null),
    				info.getMiitCode());
		}
		catch(Exception e) {
			log.error("updateComplaintInfoByList mysql异常：{}", e.getMessage(), e);
		}
        return 0;
    }
	
	public String getLastPreOrderId(String servOrderId) {
        String orderId = "";
        String strSql = "select c.ORDER_ID from cc_preorder_result c where c.SERVICE_ORDER_ID = ? order by c.MODIFY_DATE desc";
        List list = jt.queryForList(strSql, servOrderId);
        if (!list.isEmpty()) {
            Map tmpMap = (Map) list.get(0);
            orderId = tmpMap.get("ORDER_ID").toString();
        }
        return ResultUtil.success(orderId);
    }
	
	public String getLastPreOrderInfo(String servOrderId) {
		Map tmpMap = null;
        String strSql = "select c.* from cc_preorder_result c where c.SERVICE_ORDER_ID = ? order by c.MODIFY_DATE desc";
        List list = jt.queryForList(strSql, servOrderId);
        if (!list.isEmpty()) {
            tmpMap = (Map) list.get(0);
        }
        return ResultUtil.success(tmpMap);
    }
	
	public int saveBuopSheetInfo(BuopSheetInfo info) {
		String sql = "INSERT INTO CC_BUOP_SHEET_INFO(LATN_ID, SERVICE_ORDER_ID ,SERVICE_ID, CREATE_DATE, MODIFY_DATE, SHEET_STATUS, CALL_NBR, "
				+ "BUSI_NBR, PROD_INST_ID, CUST_ID, BUSI_NAME, HANDLE_LVL, CUST_NAME, CERT_TYPE, CERT_NBR, CONTACT_NBR1, CONTACT_NBR2, CREATE_REMARK, INSTALL_ADDRESS, "
				+ "POST_ADDRESS, MOVE_ADDRESS, HANDLE_COUNT, HANDLE_TYPE, HANDLE_CHNL_ID, ACCEPT_SHEET_ID, BUSI_TYPE, BUOP_CODE) VALUES "
				+ "(?, ?, ?,now(), now(), '0', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            return jt.update(sql, info.getLatnId(), info.getServiceOrderId(), StringUtils.defaultIfBlank(info.getServiceId(), null), StringUtils.defaultIfBlank(info.getCallNbr(), "0"),
            		StringUtils.defaultIfBlank(info.getBusiNbr(), null), StringUtils.defaultIfBlank(info.getProdInstId(), null),
            		StringUtils.defaultIfBlank(info.getCustId(), null), StringUtils.defaultIfBlank(info.getBusiName(), null),
    				StringUtils.defaultIfBlank(info.getHandleLvl(), null), StringUtils.defaultIfBlank(info.getCustName(), null), 
    				StringUtils.defaultIfBlank(info.getCertType(), null), StringUtils.defaultIfEmpty(info.getCertNbr(), null),
    				StringUtils.defaultIfBlank(info.getContactNbr1(), null), StringUtils.defaultIfBlank(info.getContactNbr2(), null),
    				StringUtils.defaultIfBlank(info.getCreateRemark(), null), StringUtils.defaultIfBlank(info.getInstallAddress(), null),
    				StringUtils.defaultIfBlank(info.getPostAddress(), null), StringUtils.defaultIfBlank(info.getMoveAddress(), null),
    				info.getHandleCount(), StringUtils.defaultIfBlank(info.getHandleType(), null),
    				StringUtils.defaultIfBlank(info.getHandleChnlId(), null), StringUtils.defaultIfBlank(info.getAcceptSheetId(), null),
    				StringUtils.defaultIfBlank(info.getBusiType(), null), StringUtils.defaultIfBlank(info.getBuopCode(), null));
		}
		catch(Exception e) {
			log.error("saveBuopSheetInfo mysql异常：{}", e.getMessage(), e);
		}
        return 0;
	}
	
	public int updateBuopSheetInfo(BuopSheetInfo info) {
		String sql = "UPDATE CC_BUOP_SHEET_INFO SET LATN_ID=?, MODIFY_DATE=now(), CALL_NBR=?, "
				+ "BUSI_NBR=?, PROD_INST_ID=?, CUST_ID=?, BUSI_NAME=?, HANDLE_LVL=?, CUST_NAME=?, CERT_TYPE=?, CERT_NBR=?, CONTACT_NBR1=?, CONTACT_NBR2=?, CREATE_REMARK=?, INSTALL_ADDRESS=?, "
				+ "POST_ADDRESS=?, MOVE_ADDRESS=?, HANDLE_COUNT=?, HANDLE_TYPE=?, HANDLE_CHNL_ID=?, ACCEPT_SHEET_ID=?, BUSI_TYPE=?, BUOP_CODE=? WHERE SERVICE_ORDER_ID=?";
        try {
            return jt.update(sql, info.getLatnId(), StringUtils.defaultIfBlank(info.getCallNbr(), "0"), 
            		StringUtils.defaultIfBlank(info.getBusiNbr(), null), StringUtils.defaultIfBlank(info.getProdInstId(), null),
            		StringUtils.defaultIfBlank(info.getCustId(), null), StringUtils.defaultIfBlank(info.getBusiName(), null),
    				StringUtils.defaultIfBlank(info.getHandleLvl(), null), StringUtils.defaultIfBlank(info.getCustName(), null), 
    				StringUtils.defaultIfBlank(info.getCertType(), null), StringUtils.defaultIfEmpty(info.getCertNbr(), null),
    				StringUtils.defaultIfBlank(info.getContactNbr1(), null), StringUtils.defaultIfBlank(info.getContactNbr2(), null),
    				StringUtils.defaultIfBlank(info.getCreateRemark(), null), StringUtils.defaultIfBlank(info.getInstallAddress(), null),
    				StringUtils.defaultIfBlank(info.getPostAddress(), null), StringUtils.defaultIfBlank(info.getMoveAddress(), null),
    				info.getHandleCount(), StringUtils.defaultIfBlank(info.getHandleType(), null),
    				StringUtils.defaultIfBlank(info.getHandleChnlId(), null), StringUtils.defaultIfBlank(info.getAcceptSheetId(), null),
    				StringUtils.defaultIfBlank(info.getBusiType(), null), 
    				StringUtils.defaultIfBlank(info.getBuopCode(), null), info.getServiceOrderId());
		}
		catch(Exception e) {
			log.error("updateBuopSheetInfo mysql异常：{}", e.getMessage(), e);
		}
        return 0;
	}
	
	public int updateBuopSheetInfoNew(BuopSheetInfo info) {
		String sql = "UPDATE CC_BUOP_SHEET_INFO SET MODIFY_DATE=now(), CALL_NBR=?, "
                + "BUSI_NBR=?, BUSI_NAME=?, HANDLE_LVL=?, CUST_NAME=?, CONTACT_NBR1=?, CONTACT_NBR2=?, CREATE_REMARK=?, INSTALL_ADDRESS=?, "
                + "POST_ADDRESS=?, MOVE_ADDRESS=?, HANDLE_COUNT=?,HANDLE_TYPE=?, HANDLE_CHNL_ID=?, ACCEPT_SHEET_ID=?, BUSI_TYPE=?, BUOP_CODE=? WHERE SERVICE_ORDER_ID=?";
		try {
			return jt.update(sql, StringUtils.defaultIfBlank(info.getCallNbr(), "0"),
                    StringUtils.defaultIfBlank(info.getBusiNbr(), null), StringUtils.defaultIfBlank(info.getBusiName(), null), 
                    StringUtils.defaultIfBlank(info.getHandleLvl(), null), StringUtils.defaultIfBlank(info.getCustName(), null), 
                    StringUtils.defaultIfBlank(info.getContactNbr1(), null),StringUtils.defaultIfBlank(info.getContactNbr2(), null),
                    StringUtils.defaultIfBlank(info.getCreateRemark(), null), StringUtils.defaultIfEmpty(info.getInstallAddress(), null),
                    StringUtils.defaultIfBlank(info.getPostAddress(), null), StringUtils.defaultIfBlank(info.getMoveAddress(), null),
                    info.getHandleCount(), StringUtils.defaultIfBlank(info.getHandleType(), null),
                    StringUtils.defaultIfBlank(info.getHandleChnlId(), null), StringUtils.defaultIfBlank(info.getAcceptSheetId(), null),
                    StringUtils.defaultIfBlank(info.getBusiType(), null), 
                    StringUtils.defaultIfBlank(info.getBuopCode(), null), info.getServiceOrderId());
        }
		catch(Exception e) {
            log.error("updateBuopSheetInfoNew mysql异常：{}", e.getMessage(), e);
        }
        return 0;
    }
	
	public List queryRepeatBestOrder(int limitDay, int regionId, String prodNum) {
    	String sql =
    			  " SELECT \n"
    			+ " A.SERVICE_ORDER_ID, A.PROD_NUM, \n"
    			+ " A.SERVICE_TYPE, \n"
    			+ " A.RELA_TYPE, \n"
    			+ " DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE, \n"
    			+ " DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s') FINISH_DATE, \n"
    			+ " B.BEST_ORDER \n"
    			+ " FROM \n"
    			+ " CC_SERVICE_ORDER_ASK_HIS A, \n"
    			+ " CC_SERVICE_CONTENT_ASK_HIS B \n"
    			+ " WHERE \n"
    			+ " A.SERVICE_ORDER_ID = B.SERVICE_ORDER_ID \n"
    			+ " AND A.ORDER_VESION = B.ORDER_VESION \n"
    			+ " AND A.SERVICE_TYPE = 720130000 \n"
    			+ " AND A.ORDER_STATU = 720130010 \n"
    			+ " AND A.FINISH_DATE > DATE_SUB( NOW(), INTERVAL ? DAY ) \n"
    			+ " AND B.BEST_ORDER > 100122410 \n"
    			+ " AND A.REGION_ID = ? \n"
    			+ " AND A.PROD_NUM = ? ORDER BY A.ACCEPT_DATE DESC";
        return jt.queryForList(sql, limitDay, regionId, prodNum);
    }

    public String getCompDealContent(String level) {
        String content = "";
        try{
            String strSql = "SELECT c.INTRODUCTION_CONTENT FROM cc_introduction_content c WHERE c.THIRD_LEVEL = ?";
            List<Map<String, Object>> tmpList = jt.queryForList(strSql, level);
            if(!tmpList.isEmpty()) {
            	Map<String,Object> map = tmpList.get(0);
            	content = map.get("INTRODUCTION_CONTENT") == null ? "" : map.get("INTRODUCTION_CONTENT").toString();
            }
        }catch (Exception e){
            log.error("getCompDealContent error: {}", e.getMessage(), e);
        }
        return ResultUtil.success(content);
    }
    
    public List queryUrgentOrder(int limitDay, int regionId, String prodNum) {
    	String sql =
    			" SELECT * FROM ( SELECT \n"
		    	+ " A.SERVICE_ORDER_ID, A.PROD_NUM, \n"
				+ " A.RELA_TYPE, \n"
				+ " DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE, \n"
				+ " DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s') FINISH_DATE \n"
				+ " FROM \n"
				+ " CC_SERVICE_ORDER_ASK A \n"
				+ " WHERE 1 = 1 \n"
				+ " AND A.SERVICE_TYPE = 720130000 \n"
				+ " AND A.ORDER_STATU <> 720130001 \n"
				+ " AND A.ACCEPT_DATE > DATE_SUB( NOW(), INTERVAL ? DAY ) \n"
				+ " AND A.REGION_ID = ? \n"
				+ " AND A.PROD_NUM = ? \n"
				+ " UNION ALL SELECT \n"
    			+ " A.SERVICE_ORDER_ID, A.PROD_NUM, \n"
    			+ " A.RELA_TYPE, \n"
    			+ " DATE_FORMAT(A.ACCEPT_DATE, '%Y-%m-%d %H:%i:%s') ACCEPT_DATE, \n"
    			+ " DATE_FORMAT(A.FINISH_DATE, '%Y-%m-%d %H:%i:%s') FINISH_DATE \n"
    			+ " FROM \n"
    			+ " CC_SERVICE_ORDER_ASK_HIS A \n"
    			+ " WHERE 1 = 1 \n"
    			+ " AND A.SERVICE_TYPE = 720130000 \n"
    			+ " AND A.ORDER_STATU = 720130010 \n"
    			+ " AND A.ACCEPT_DATE > DATE_SUB( NOW(), INTERVAL ? DAY ) \n"
    			+ " AND A.REGION_ID = ? \n"
    			+ " AND A.PROD_NUM = ? ) AS R ORDER BY ACCEPT_DATE DESC";
        return jt.queryForList(sql, limitDay, regionId, prodNum, limitDay, regionId, prodNum);
    }

    public String querySummaryContent(String orderId) {
        String sql ="SELECT HANDLING_RESULT,ESCALATION_TENDENCY,CONTACT_PLAN FROM CS_SUMMARY_CALLBACK_LOG WHERE SERVICE_ORDER_ID = ? ORDER BY CALLBACK_DATE DESC LIMIT 1";
        String result = "";
        try{
            List<Map<String, Object>> list = jt.queryForList(sql, orderId);
            if(!list.isEmpty()){
                Map<String, Object> stringObjectMap = list.get(0);
                result = (String)stringObjectMap.get("HANDLING_RESULT");
            }
        }catch (Exception e){
            log.error("querySummaryContent error: {}",e.getMessage(),e);
        }
        return ResultUtil.success(result);
    }

    public String getSummaryData(String orderId) {
        String sql ="SELECT SERVICE_ORDER_ID,KEY_INFO,DEAL_CONTENT_KEY1,DEAL_CONTENT_KEY2,DEAL_CONTENT_KEY3,SCENE_TYPE_NAME1,SCENE_TYPE_NAME2,ACCEPT_DATE,FINISH_DATE,CLNDR_DT_ID,CREATE_TIME FROM CC_SUMMARY_CONTENT WHERE SERVICE_ORDER_ID = ? ORDER BY CREATE_TIME DESC";
        Map<String, Object> result = null;

        try{
            List<Map<String, Object>> list = jt.queryForList(sql, orderId);
            if(!list.isEmpty()){
                result = list.get(0);
            }
        }catch (Exception e){
            log.error("getSummaryData error: {}",e.getMessage(),e);
        }
        return ResultUtil.success(result);
    }
}