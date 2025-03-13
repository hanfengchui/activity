/*
 * 文 件 名：CompatHandler.java
 * 版    权：
 * 描    述：由于系统的持续改版、新增功能等原因，需要考虑新旧ID兼容问题。该类提供的方法中，兼顾新旧ID的判断
 * 修 改 人：LiJiahui
 * 修改时间：2013-2-21
 * 修改内容：新增
 */
package com.timesontransfar.sheetHandler;

import com.timesontransfar.customservice.common.StaticData;

/**
 * 兼顾新旧ID进行判断
 * 
 * @author LiJiahui
 * @version
 * @since
 */
public class CompatHandler {
    /**
     * 判断工单环节是否是后台派单<br>
     * (包括新旧)
     * 
     * @param tachId
     *            工单环节ID
     * @return true/false
     */
    public static final boolean isTachAssign(int tachId) {
        return tachId == StaticData.TACHE_ASSIGN || tachId == StaticData.TACHE_ASSIGN_NEW;
    }

    /**
     * 判断是否是工单退回环节<br>
     * (包括新旧)
     * 
     * @param tachId
     *            工单环节ID
     * @return true/false
     */
    public static final boolean isTachOrderBack(int tachId) {
        return tachId == StaticData.TACHE_ORDER_ASK || tachId == StaticData.TACHE_ORDER_BACK;
    }

    /**
     * 判断是否是工单部门处理环节<br>
     * (包括新旧)
     * 
     * @param tachId
     *            工单环节ID
     * @return true/false
     */
    public static final boolean isTachOrgDeal(int tachId) {
        return tachId == StaticData.TACHE_DEAL || tachId == StaticData.TACHE_DEAL_NEW;
    }

    /**
     * 判断订单是否处于后台派单退回前台修改状态<br>
     * (包括新旧)
     * 
     * @param orderStatu
     *            订单状态ID
     * @return true/false
     */
    public static final boolean isOStatuBack(int orderStatu) {
        return orderStatu == StaticData.OR_BACK_STATU || orderStatu == StaticData.OR_BACK_STATU_NEW;
    }

    public static final boolean isOStatuFinish(int orderStatu){
        return orderStatu == StaticData.OR_COMPLETE_STATU || orderStatu == StaticData.OR_FINISH_STATU;
    }
    
    /**
     * 判断服务类型是否是投诉、查询<br>
     * 
     * @param servType
     *            服务类型ID
     * @return true/false
     */
    public static final boolean isServTypeComplaint(int servType) {
    	return servType == StaticData.SERV_TYPE_NEWTS || servType == StaticData.SERV_TYPE_CX;
    }
}
