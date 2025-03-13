/*
 * 文 件 名：SheetDealHandlerCImpl.java
 * 版    权：
 * 描    述：为服务类型为“投诉”的工单处理编写的帮助类
 * 修 改 人：LiJiahui
 * 修改时间：2013-2-16
 * 修改内容：新增
 */
package com.timesontransfar.sheetHandler.impl;

import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.sheetHandler.ASheetDealHandler;

/**
 * 服务类型为“投诉”的工单处理帮助类
 * 
 * @author LiJiahui
 * @version
 * @since 2013-02-16
 */
@Component("sheetDealHandler")
public class SheetDealHandlerCImpl extends ASheetDealHandler {

    /**
     * {@inheritDoc}
     */
    public String hangup(String sheetId, String comment) {
        ISheetPubInfoDao sheetDao = getSheetPubInfoDao();
        PubFunc pf = getPubFunc();
        IorderAskInfoDao orderDao = getOrderAskInfoDao();

        SheetPubInfo sheetPubInfo = sheetDao.getSheetPubInfo(sheetId, false);
        int state = StaticData.WKST_HOLD_STATE_NEW;
        if (sheetPubInfo.getSheetStatu() == state) {
            return "HANGING";
        }
        String stateDesc = pf.getStaticName(state);
        sheetDao.updateSheetState(sheetId, state, stateDesc, sheetPubInfo.getMonth(),
                sheetPubInfo.getLockFlag());
        String orderId = sheetPubInfo.getServiceOrderId();
        OrderAskInfo orderAskInfo = orderDao.getOrderAskInfo(orderId, false);
        int subSheetHoldNum = orderAskInfo.getSubSheetCount();
        String firstSheetHoldTime = orderAskInfo.getHangStartTime();// 首张单挂起始时间
        int totalHoldTime = orderAskInfo.getHangTimeSum();// 挂单总时间
        if (subSheetHoldNum == 0) {
            orderDao.updateSubSheetHoldInfo(orderId, subSheetHoldNum + 1, pf.getSysDate(),
                    totalHoldTime);
        } else {
            orderDao.updateSubSheetHoldInfo(orderId, subSheetHoldNum + 1, firstSheetHoldTime,
                    totalHoldTime);
        }
        // 更新工单的挂起时间
        int sheetTotalHoldTime = sheetPubInfo.getHangupTimeSum();
        String angupStrTime = pf.getSysDate();
        sheetDao.updateTotalHold(angupStrTime, sheetTotalHoldTime, sheetPubInfo.getRegionId(),
                sheetId);

        // 记录挂动作信息
        saveSheetDealAction(orderId, sheetId, sheetPubInfo.getRegionId(), comment,
                sheetPubInfo.getMonth(), sheetPubInfo.getTacheId(), StaticData.WKST_HOLD_ACTION);
        return "SUCCESS";
    }
}
