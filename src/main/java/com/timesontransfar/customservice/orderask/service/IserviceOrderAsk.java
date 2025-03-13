/**
 * <p>类名：IserviceOrderAsk.java</p>
 * <p>功能描叙：服务单受理业务功能接口</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：1、增加定单预警/告警查询方法getServOrderByAlarm cjw April 9, 2008</p>
 * <p>  Create by:	lifeng	Mar 19, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.orderask.service;

import java.util.List;
import java.util.Map;

import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.pojo.CallSummary;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServContentInstance;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.pojo.YNSJOrder;
import com.timesontransfar.customservice.orderask.pojo.BuopSheetInfo;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSaveSJ;
import com.timesontransfar.customservice.worksheet.pojo.ServiceWorkSheetInfo;

import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import net.sf.json.JSONObject;

/**
 * @author wanrongwei
 *
 */
@SuppressWarnings("rawtypes")
public interface IserviceOrderAsk {

    String MESSAGE_CONTENT = "尊敬的客户：您好！您反映的问题，我们正在积极处理，给您带来的不便敬请谅解。\n中国电信";

    /**
     * 提交受理单业务功能接口
     * 
     * @param servContent
     *            受理内容对像
     * @return 目录级别是否合格
     */
    public String checkSelect(ServiceContent servContent, int serviceDate);

    /**
     * 提交受理单业务功能接口
     * 
     * @param instance
     *            投诉内容特性对象
     * @param custInfo
     *            受理单客户信息对象
     * @param servContent
     *            受理内容对像
     * @param servOrderInfo
     *            受理信息对像
     * @param otherInfo
     *            其它信息(派单信息和处理理要求)
     * @return 是否提交成功
     */
    public String submitServiceOrderInstance(ServContentInstance[] instance,
            OrderCustomerInfo custInfo, ServiceContent servContent, OrderAskInfo orderAskInfo, Map otherInfo, ComplaintInfo compInfo);
    
	/**
	 * 商机单带标签提交
	 */
	public String submitServiceOrderInstanceWithLabel(OrderCustomerInfo custInfo, ServiceContent servContent,
			OrderAskInfo orderAskInfo, Map otherInfo, YNSJOrder order, BuopSheetInfo info);
	
	/**
	 * 逐条保存暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @param ServiceContentSaveSJ
	 * @return 保存的记录数
	 */
	public int saveContentSaveSJ(String serviceOrderId, ServiceContentSaveSJ[] serviceContentSaveSJs);

	/**
	 * 删除暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @return 更新的记录数
	 */
	public int removeContentSaveSJ(String serviceOrderId);

	/**
	 * 得到暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @return ServiceContentSaveSJ
	 */
	public ServiceContentSaveSJ[] queryContentSaveSJ(String serviceOrderId);

    /**
     * 根据受理单的单号查询此受理单的相关信息
     * 
     * @param orderId
     *            受理单号
     * @param hisFlag
     *            当前、历史标识
     * @return 受理单对像
     */

    public ServiceOrderInfo getServOrderInfo(String orderId, boolean hisFlag);

    /**
     * 删除暂存工单
     * 
     * @param orderId
     * @return 是否删除成功
     */
    public boolean deleteHoldOrder(String orderId, Integer month);

    /**
     * 更新服务受理单信息
     * 
     * @param custInfo
     *            受理单客户信息对象
     * @param servContent
     *            受理内容对像
     * @param servOrderInfo
     *            受理信息对像
     * @return 是否成功
     */
    public boolean updateServiceOrderInfo(OrderCustomerInfo custInfo, ServiceContent servContent,
            OrderAskInfo orderAskInfo, String sheetId);

    /**
     * 得到当前员工登录的基本信息
     * 
     * @return
     */
    public TsmStaff getLogonStaff();

    /**
     * 根据产品码查询该产品三个月内的投诉记录(竣工和未竣工的受理单)
     * 
     * @param prodNum
     *            产品号码
     * @param sql
     *            其他查询条件，用于跟在where后面，请以 and 开头
     * @return 受理单对像数组
     */
    public OrderAskInfo[] getServOrderProdNumHis(String prodNum, String sql);

    public GridDataInfo getServOrderProdNumHis(String parm);

    /**
     * 根据受理单id客户CUSTID分区标志地域ID获取受理单
     * 
     * @param serOrdId
     *            serviceOrderID
     * @param custId
     *            客户ID
     * @param monthFlg
     *            分区标志
     * @param regionId
     *            地域ID
     * @param status
     *            是否峻工
     * @return 受理单对象
     */
    public ServiceOrderInfo getServiceOrderById(String serOrdId, String custId, Integer monthFlg,
            String regionId, int status, int vesion);
    
    /**
     * 根据受理单id获取受理单
     * 
     * @param serOrdId
     *            serviceOrderID
     * @param queryType
     *            0表示当前表不存在时,需要查历史表，1表示只查历史表 2，只查当前表
     * @param sheetId
     *            工单号
     * @return 受理单对象
     */
    public ServiceOrderInfo getErrOrderById(String serOrdId, int queryType, String sheetId);

    /**
     * 受理时，暂存受理单
     * 
     * @param custInfo
     *            受理单客户信息对象
     * @param servContent
     *            受理内容对像
     * @param orderAskInfo
     *            受理信息对像
     * @return 处理结果。
     */
    public String holdServiceOrderInstance(OrderCustomerInfo custInfo, ServiceContent servContent, OrderAskInfo orderAskInfo);
    
    /**
     * 重复受理方法
     * 
     * @param orderId
     *            服务单ID
     * @return 是否受理成功
     */
    public boolean reatAccepte(ServContentInstance[] instance, OrderCustomerInfo custInfo,
            ServiceContent servContent, OrderAskInfo orderAskInfo, Map otherInfo);
    
	/**
	 * 优化受理方法
	 */
	public String submitServiceOrderInstanceLabelNew(OrderCustomerInfo custInfo, ServiceContent servContent,
			OrderAskInfo orderAskInfo, Map otherInfo, LabelInstance[] ls, ServiceContentSave[] saves, ComplaintInfo compInfo);

	public String getOrderCount(String staffId);

	public Map getRoleWork(String staffId);

	public Map getWorkView(String staffId);

	public Map qrySheetList(String where);

	public Map qryOrderAskList(Map param);
	
	public CallSummary getLastCallSummary(String orderId);
	
	public int saveCallSummaryUpdate(CallSummary summary);
	
	public Map qrCliqueList(String where);
	
	public String getBssStaffCode(String logoName);

	public Map qrySpecialInfo(String tableType, String sql);
	
	int addSpecialInf(Map param);//添加特殊用户
	
	int removeSpecial(Map param);//删除特殊用户
	
	int updateSpecial(Map param);//更新特殊用户
	
	public List getGzOorderList(String orderId);

	public String getPersonas(String serviceOrderId, String regionId, String prodNum, boolean curFlag);
	
    /**
     * 根据受理单的单号查询此受理单的相关信息
     * 
     * @param orderId
     *            受理单号
     * @param hisFlag
     *            当前、历史标识
     * @return 受理单对像
     */
    public ServiceWorkSheetInfo getServiceInfo(String orderId, boolean hisFlag);
    
    public void saveComplaintInfo(JSONObject culiqueJson, String orderId);
    
	public Map getComplaintInfo(String orderId, String hisFlag);
	
	public String submitServiceOrderInstanceYS(OrderCustomerInfo custInfo, ServiceContent servContent, OrderAskInfo orderAskInfo, Map otherInfo, boolean finishCheckFlag, BuopSheetInfo info);
	
	public boolean checkOrderNotFinish(OrderAskInfo orderAskInfo);
	
	public void autoPdAsync(String orderId);

	public void autoFinish(SheetPubInfo sheetPubInfo, OrderAskInfo orderAskInfo, BuopSheetInfo buopSheetInfo, boolean boo, String sendContent, int dealStaff);
	
}