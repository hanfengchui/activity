package com.timesontransfar.customservice.orderask.dao;

import java.util.Map;
import java.util.List;

import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.orderask.pojo.CallSummary;
import com.timesontransfar.customservice.orderask.pojo.CallSummaryOrder;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.PreOrderResult;
import com.timesontransfar.customservice.orderask.pojo.YNSJOrder;
import com.timesontransfar.customservice.orderask.pojo.YNSJResult;

/**
 * @author lifeng
 *
 */
@SuppressWarnings("rawtypes")
public interface IorderAskInfoDao {

	/**
	 * 保存订单跟踪信息
	 * 
	 * @return 保存成功记录数
	 */
	public int saveServiceTrack(String newOrderId, String oldOrderId, int trackType);

	/**
	 * 保存受理单相关受理信息
	 * @param order 受理单受理信息pojo
	 * @return	保存成功记录数
	 */
	public int saveOrderAskInfo(OrderAskInfo order);

	/**
	 * 将当前的定单信息保存到历史表
	 * @param currentOrderId 当前表的定单id
	 * @return 保存的记录数
	 */
	public int saveOrderAskInfoHis(String currentOrderId,Integer month);

	/**
	 * 根据受理单号查询到此单的受理信息
	 * @param orderId 受理单号
	 * @param hisFlag true查询历史表；false查询当前表
	 * @return	受理单受理信息
	 */
	public OrderAskInfo getOrderAskInfo(String orderId, boolean hisFlag);
	
	
	/**
	 * 根据受理单号和地域ID查询到此单的受理信息
	 * @param orderId 受理单号
	 * @param  regionId
	 * @param hisFlag 是否操作的是历史信息,true 为历史查询
	 * @return	受理单受理信息
	 */
	public OrderAskInfo getOrderAskObj(String orderId,Integer month,boolean hisFlag);

	/**
	 * 查询受理时间180天前的订单
	 */
	public OrderAskInfo getOrderAskOver180Day(String orderId);

	/**
     * 根据受理单号查询到此单的受理信息（工单打印使用）
     * @param orderId 受理单号
     * @param hisFlag 是否操作的是历史信息
     * @return  受理单受理信息
     */
    public OrderAskInfo getOrderAskObjNew(String orderId,boolean hisFlag,String statu);
    
	/**
	 * 根据受理单号和地域ID查询到此单的受理信息
	 * @param orderId 受理单号
	 * @param  regionId
	 * @param  suffix 是否操的是归档信息，不为空为归档查询
	 * @param hisFlag 是否操作的是历史信息,true 为历史查询
	 * @return	受理单受理信息
	 */
	public OrderAskInfo getOrderAskObjNew(String orderId,Integer month,String suffix,boolean hisFlag);

	/**
	 * 根据受理单号删除此单的受量信息
	 * @param orderId	受理单号
	 * @param hisFlag	当前/历史
	 * @return 删除的记录条数
	 */
	public int delOrderAskInfo(String orderId,Integer month);

	/**
	 * 更新定单受理信息的状态信息
	 * @param orderId 受理单id
	 * @param statu	状态值
	 * @return	是否成功
	 */
	public boolean updateOrderStatu(String orderId, int statu,Integer month,String orderStatuDesc);
	
	/**
	 * 保存定单后，进历史表的定单更新为作废状态
	 * @param region 地域
	 * @param orderId 受理单ID
	 * @param statu 状态值
	 * @return
	 */
	public boolean updateOrderHisStatu(int region,String orderId, int statu,String statuDesc,Integer month);
	
	public boolean updateOrderHisStatuByVersion(int region, String orderId, int version, int statu, String statuDesc, Integer month);

	/**
	 * 更新一条受理单的
	 * @param orderAskInfo 受理信息对象
	 * @return 更新的记录数
	 */
	public int updateOrderAskInfo(OrderAskInfo orderAskInfo);

	/**
	 * 更新定单的挂起的子单数量及挂起时间
	 * @param orderId	受理单号
	 * @param subSheetHoldNum	挂起字单时间
	 * @param holdStrTime	挂起开始时间
	 * @param holdSumTime	挂起总时间
	 * @return	更新的记录数量
	 */
	public int updateSubSheetHoldInfo(String orderId, int subSheetHoldNum,
			String holdStrTime, int holdSumTime);

    /**
     * 根据条件进行受理单查询
     * @param condition 查询条件
     * @param hisFlag   false当前/true历史
     * @return  
     */
	public OrderAskInfo[] getOrderAskInfoByCondition(String condition,
			boolean hisFlag);

	/**
	 * 更新定单完成时间
	 * @param orderId	定单号
	 * @return	更新记录数
	 */
	public int updateOrderFinishDate(String orderId,Integer month);
	
	/**
	 * 更新定单受理次数
	 * @param orderId	定单号
	 * @param count 受量次数
	 * @return 更新记录数
	 */
	public int updateOrderAskCount(String orderId, int count);
	
	/**
	 * 更新定单的备注信息
	 * @param orderId	定单号
	 * @param comments	备注
	 * @return	更新记录数
	 */
	public int updateOrderComments(String orderId, String comments);

	/**
	 * 根据产品码查询该产品一个月内的投诉记录数
	 * @param prodNum
	 * @param sql
	 * @return
	 */
	public int[] getOrderProHisCount(String prodNum, String sql);

	/**
	 *  根据产品码查询该产品一个月内的投诉记录(竣工和未竣工的受理单)
	 * @param prodNum
	 * @param sql
	 * @return
	 */
	public OrderAskInfo[][] getOrderProHis(String prodNum ,String sql);
	
	/**
	 * 根据受理单id客户CUSTID分区标志地域ID获取受理单
	 * @param serOrdId serviceOrderID
	 * @param monthFlg 分区标志
	 * @param regionId 地域ID
	 * @param hisFlag 当前、历史标识
	 * @return 受理单对象
	 */
	public OrderAskInfo getOrderAskInfoByIdMonth(String serOrdId,
			Integer monthFlg,String regionId , boolean hisFlag,int vesion);
	
	/**
	 * 
	 * @param serOrdId serviceOrderID
	 * @param regionId 地域ID
	 * @param suffix 表的后缀，取值有三种，分别表示在途表、历史表、归档表："";"_HIS";"_HIS_BAK".
	 * @param vesion 受理单的变更版本号。如果查在途表，该参数无意义。
	 * @return
	 */
	public OrderAskInfo getOrderAskInfoByIdMonthNew(String serOrdId,String regionId , String suffix, int vesion);
	
	/**
	 * 根据受理单id客户CUSTID分区标志地域ID获取受理单
	 * @param serOrdId serviceOrderID	
	 * @param queryType 0表示当前表不存在时,需要查历史表，1表示只查历史表 2，只查当前表
	 * @return 受理单对象
	 */
	public OrderAskInfo getErrInfoById(String serOrdId ,int queryType);
	
	/**
	 * 预受理是否成功,成功的话写CRM定单号
	 * @param orderAskInfo 定单对象,定单号,月分区,ACCEPT_CHANNEL_ID CRM定单号,ACCEPT_CHANNEL_DESC
	 * @return
	 */
	public int updateCrmAskSheet(OrderAskInfo orderAskInfo);
	
	/**
	 * 更新疑难工单的定性
	 * @param orderAskInfo YS_QUALIATIVE_ID定性ID YS_QUALIATIVE_NAME 定性名
	 * @return
	 */
	public int updateQualiative(int ysQualiativeId,String ysQualiativeName,Integer month,String orderId);
	
	/**
	 * 记录定单是在哪个环节竣工的
	 * @param orderId 定单
	 * @param regionId 地域
	 * @param tacheId 竣工的环节
	 * @return
	 */
	public int updateFinTache(String orderId,int regionId,int tacheId);
	
	/**
	 * 根据地域和客户号码，查询该号码是否在投诉特殊客户表中存在
	 * @param custNum
	 * @param regionId
	 * @return
	 */
	public int getTsEspeciallyCust(String custNum,int regionId);
	
	 /**
     * 根据地域和客户号码，查询该号码用户的投诉特征
     * @param custNum
     * @param regionId
     * @return
     */
    public String getTsEspeciallyContent(String custNum,int regionId);
	
	public int saveYNSJOrder(YNSJOrder order);
	
	public int updateYNSJOrder(String servOrderId,String actionResult);
	
	public int updateBusinessOrder(String servOrderId,String actionResult,String orderId);
	
	public YNSJOrder queryYNSJOrder(String orderId);
	
	public int saveYNSJResult(YNSJResult result);
	
	public int updateYNSJResult(YNSJResult result);
	
	public int saveCallSummaryOrder(CallSummaryOrder order);

	public Map getRoleWork(String staffId);

	public Map getWorkView(String staffId);

	public String queryCurOrder(String curStaff, int curType);

	public int saveCurOrder(String curStaff, int curType, String curOrder);

	public int updateAcceptDate(String serviceOrderId);

	public String selectAcceptDate(String serviceOrderId);

	public List queryAllOrder(String serviceOrderId, String prodNum, String relaInfo, int regionId);

	public List queryOrderPreference(String serviceOrderId,String prodNum,int regionId);

	public List queryAllOrderCX(String serviceOrderId, String prodNum, String relaInfo, int regionId);

	public int updateOrderLimitTime(int orderLimitTime, String serviceOrderId);

	public int insertServiceWisdomType(String orderIdOld, String holdFlag, String sendFlag);

	public int insertServiceWisdomTypeHis(String orderIdOld);

	public int updateServiceWisdomType(String resultCode, String resultMsg, String scenario, String wisdomType, String unifiedflag, String orderIdNew, String orderIdOld);

	public Map qrySheetList(String where);

	public Map qryOrderAskList(Map param);
	
	public CallSummary getLastSummary(String orderId,boolean updateFlag);
	
	public int saveCallSummaryUpdate(CallSummary summary);
	

	public Map qrCliqueList(String sql);
	
	public Map getSpecialInfoStr(String tableType, String sql);
	
	int addSpecialInf(Map param);//添加坐席特殊用户
	
	int addSpecialIvr(Map param);//添加ivr特殊用户
	
	int removeSpecial(Map param);//删除特殊用户
	
	int updateSpecialIvr(Map param);//更新ivr特殊用户
	
	int updateSpecialInfo(Map param);//更新坐席特殊用户

	public boolean isZHYXBusinessOrder(String orderId);//是否智慧营销商机单

	public List getGzOorderListByNewId(String orderId);//根据跟踪单查原单列表
	
	public List getGzOorderListByOldId(String orderId);// 根据原单号查跟踪单列表
	
	public boolean isExistGZOrder(String orderId);//原单是否已有跟踪单
	
	public int updateHisCrmAskSheet(OrderAskInfo orderAskInfo);
	
	public String isBusinessOrder(String orderId);
	
	public String getBusinessOrderId(String orderId);//根据中台订单号码获取服务单号

	public int getPreferAppealFlag(String orderId, String accNum, int region);

	public int getPreferComplaintFlag(String orderId, String accNum, int region);

	public String getLastFinalOptionOrderId(String accNum, int region);
	
	public int insertComplaintInfo(ComplaintInfo info);
	
	public Map getComplaintInfo(String orderId, boolean hisFlag);
	
	public List queryComplaintListByMiitCode(String miitCode, boolean hisFlag);
	
	public int updateComplaintInfo(ComplaintInfo info);
	
	public int insertComplaintInfoHisByOrderId(String orderId);
	
	public int updateOrderRelationFinish(String orderId);
	
	public int savePreOrderResult(String serviceOrderId, String orderId, String goodsId, String goodsName);
	
	public String getPreOrderId(String orderId);
	
	public int updateOtherComplaintInfo(ComplaintInfo info);
	
	public int updatePreOrderResult(PreOrderResult result);
	
	public int updateComplaintInfoByList(ComplaintInfo info);
	
	public String getLastPreOrderId(String servOrderId);
	
	public String getLastPreOrderInfo(String servOrderId);
	
	public int saveBuopSheetInfo(BuopSheetInfo info);
	
	public int updateBuopSheetInfo(BuopSheetInfo info);
	
	public int updateBuopSheetInfoNew(BuopSheetInfo info);
	
	public List queryRepeatBestOrder(int limitDay, int regionId, String prodNum);

	public String getCompDealContent(String level);
	
	public List queryUrgentOrder(int limitDay, int regionId, String prodNum);

	public String querySummaryContent(String orderId);

	public String getSummaryData(String orderId);
}