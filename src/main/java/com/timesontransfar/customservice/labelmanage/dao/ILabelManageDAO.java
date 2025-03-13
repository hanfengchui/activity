/*
 * 说明：执行标签增删改查操作的接口
 * 时间： 2012-5-2
 * 作者：LiJiahui
 * 操作：新增
 */
package com.timesontransfar.customservice.labelmanage.dao;

import java.util.List;

import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;

/**
 * 执行标签增删改查操作的接口
 * 
 * @author LiJiahui
 */
public interface ILabelManageDAO {

    /**
     * 更新升级倾向
     * @return
     */   
    int updateUpgradeIncline(String serviceId,int upgradeIncline);

    /**
     * 更新申诉是否有效
     * @return
     */
    int updateValidFlag(String serviceId,int valiFlag);

    /**
     * 新增一条记录
     * 
     * @author LiJiahui
     * @date 2012-5-15
     * @param serviceId
     *            订单ID
     * @return 操作完成的记录数
     */
    public int insertNew(String serviceId);

    public int insertServiceConnection(String connectionGuid, String serviceOrderId, int connectionState, String connectionType, String acceptDate);

    /**
     * 保存三强终判定结果
     * 
     * @author LiJiahui
     * @date 2012-6-4
     * @param serviceId
     *            订单号
     * @param forceId
     *            判定结果
     * @return 返回结果。SUCCESS 表示成功；ERROR表示失败
     */
    public int updateForceCfmFlag(String serviceId, String forceId);

    /**
     * 将订单的首次回复时间，即短信的发送时间，保存到标签表中
     * 
     * @author LiJiahui
     * @date 2012-5-10
     * @param serviceId
     *            订单号
     * @return 返回执行结果，即更新的记录数
     */
    public int saveFirstRevertDate(String serviceId);
    
    /**
     * 将订单的部门正式回复时间，保存到标签表中
     * 
     * @author LiJiahui
     * @date 2013-10-24
     * @param serviceId
     *            订单号
     * @return 返回执行结果，即更新的记录数
     */
    public int saveFormalAnswerDate(String serviceId);
    
    /**
     * 更新受理单的最后回复时间<br>
     * 关于处理部门最后一次回复时间点标记的统计口径：<br>
	 * 1.省客服中心/投诉处理中心受理人员现场办结工单，取现场直接办结时间。<br>
	 * 2.省客服中心/投诉处理中心后台派单环节直接办结的工单，取后台派单直接办结时间<br>
	 * 3.分公司、省直单位、省专业公司接省投诉处理中心派单，取最后一次处理部门提交生成预定性单的时间。<br>
	 * 4.对于有主办和协办的受理单，取主办单最后一次部门提交生成预定性单的时间。<br>
     * 
     * @author LiJiahui
     * @date 2014-03-06
     * @param serviceId 受理单号
     * @return 返回执行结果，即更新的记录数
     */
    public int updateLastAnswerDate(String serviceId);

    /**
     * 将订单的处理完成时间保存到标签表中<br>
     * 有定性环节的，保存定性完成时间，无定性环节的，保存审核完成时间
     * 
     * @author LiJiahui
     * @date 2012-5-10
     * @param serviceId
     *            订单号
     * @return 返回执行结果，即更新的记录数
     */
    public int saveFinishDate(String serviceId);
    /**
     * 将投诉类订单的定性结果存储到标签表中<br>
     * 
     * @author LiJiahui
     * @date 2012-5-10
     * @param controlAreaSec
     *            责任定性二级目录ID
     * @param dutyOrgSec
     *            责任部门二级目录ID
     * @param lastYY
     *            办结原因，末级ID
     * @param serviceId
     *            订单号
     * @return 返回执行结果，即更新的记录数
     */
    public int saveQualitative(Integer controlAreaSec, String dutyOrgSec, Long lastYY, String serviceId);

    /**
     * 更新订单的是否超时标签值
     * 
     * @author LiJiahui
     * @date 2012-5-24
     * @param serviceId
     *            订单ID
     * @return 返回执行结果，即更新的记录数
     */
    public int updateOverTimeLabel(String serviceId);
    
    /**
     * 更新订单的是否受理时选择直接调账
     */
    public int updateAdjustAccountFlag(String serviceId);
    
    public int updateDirectDispatchFlag(String serviceId);
    
    /**
     * 更新订单的是否越级倾向
     */
    public int updateUpTendencyFlag(String serviceId,int upTendencyFlag);
    
    /**
     * @author LiJiahui
     * @date 2012-5-17
     * @param serviceId
     *            订单ID
     * @param hisFlag
     *            标识量。true历史；false当前
     * @return 查询得到的记录列表。如果未查询到符合条件的记录，则返回null
     */
    public ServiceLabel queryServiceLabelById(String serviceId, boolean hisFlag);

    /**
     * 获取订单的办结时间，查询当前表
     * 
     * @param serviceId
     *            订单ID
     * @return
     */
    public String queryFinishDate(String serviceId);

    /**
     * @date 2013-1-10
     * @param serviceId
     *            订单ID
     * @return 插入标签表进历史
     */
    public int saveLabelHisById(String serviceId);
    
    /**
     * @date 2013-12-19
     * @param serviceId
     * @param num
     * @return
     */
    public int updateValidHastenNum(String serviceId, int num);

    /**
     * 更新省市总热线标识
     */
	public int updateHotlineFlag(String serviceOrderId, int staffId, int hotlineFlag);

    /**
     * 更新终定性环节中“回访记录”区域的“投诉处理结果”值
     */	
	public int updateDealResult(String serviceOrderId, int dealResult, String dealResultName);
	
	/**
	 * 更新集团二次派单标识
	 */
	public int saveSecFlag(String serviceId);

	/**
	 * 剔除投诉工单处理内容中关键字“互联网卡”、“屏蔽外呼”标识
	 */	
	public int updateUnusualFlag(String serviceId);

	public int updateFirstAuditDate(String serviceOrderId);

	public String selectFirstAuditDate(String serviceOrderId);

	public int selectRepeatFlag(String serviceOrderId);

	public int updateDealHours(int dealHours, String serviceOrderId);

	public int selectDealHours(String serviceOrderId);

	public int updateAuditHours(int auditHours, String serviceOrderId);

	public int selectAuditHours(String serviceOrderId);

	public int updateIsUnified(int isUnified, String serviceOrderId);

	public int selectIsUnified(String serviceOrderId);

	public int updateAutoVisitFlag(int autoVisitFlag, String serviceOrderId);

	public int selectAutoVisitFlag(String serviceOrderId);

	public int updateZdxCpDate(String serviceOrderId);

	public String selectZdxCpDate(String serviceOrderId);

	public int updateLastAuditDate(String serviceOrderId);

	public String selectLastAuditDate(String serviceOrderId);
	
	public int saveFinalOptionLabel(String orderId, String sheetId);

	public int updatePassiveRepeatFlag(String orderId, String accNum, int region);

	public int updatePassiveUpgradeFlag(String orderId, String accNum, int region);
	
	public void updateServiceLabel(ServiceLabel label);
	
	public void updateCallFlag(String orderId, int callFlag);
	
	public void updateCustType(String orderId, int flag);

	public int insertRuyiLabel(String orderId);

	public int insertRuyiLabelHis(String orderId);

	@SuppressWarnings("rawtypes")
	public List getRepeatGuidList(String serviceOrderId);
	
	public int deleteServiceConnection(String connectionGuid);
	
	public int saveOldConnection(String guid, String orderId, String type);
	
	public void updateRepeatLabel(ServiceLabel label);
	
	/**
	 * 取消最严客户工单重复
	 * @param orderId
	 */
	public void updateRepeatBestFlag(String orderId);
	
	public void updateRefundFlag(String orderId, int flag);
}