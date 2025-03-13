package com.timesontransfar.customservice.tuschema.dao;

import java.util.List;

import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSaveSJ;
import com.timesontransfar.customservice.tuschema.pojo.ServiceOfferInfo;

public interface IserviceContentTypeDao {
	/**
	 * 根据服务单号删除模板信息
	 * @param orderId
	 * @return
	 */
	public int deleteContentSaveByOrderId(String orderId);
	
	public int deleteDealContentSaveByOrderId(String orderId);

    /**
     * 逐条保存受理单的受理内容
     * 
     * @param serviceContentSaves
     *            受理内容元素数组
     * @return 返回操作成功的记录数
     */
    public int insertContentSave(ServiceContentSave[] serviceContentSaves);
    
    public int insertDealContentSave(final List<ServiceContentSave> saveList);

    /**
     * 同一定单下的受理模板保存进历史表
     * 
     * @param serviceOrderId
     * @return 保存的记录数
     */
    public int insertContentSaveHis(String serviceOrderId);
    
    public int insertDealContentSaveHis(String serviceOrderId);

	public String getAnswerNameByOrderId(String orderId, String elementId);

	public String getHisAnswerNameByOrderId(String orderId, String elementId);

    /**
     * 得到当前受理内容
     * 
     * @param serviceOrderId
     * @return ServiceContentSave
     */
    public ServiceContentSave[] selectContentSave(String serviceOrderId);
    
    public ServiceContentSave[] selectContentSaveHis(String serviceOrderId);

	/**
	 * 逐条保存暂存商机模板
	 * 
	 * @param ServiceContentSaveSJ
	 * @return 保存的记录数
	 */
	public int insertContentSaveSJ(final ServiceContentSaveSJ[] serviceContentSaveSJs);

	/**
	 * 删除暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @return 更新的记录数
	 */
	public int deleteContentSaveSJ(String serviceOrderId);

	/**
	 * 得到暂存商机模板
	 * 
	 * @param serviceOrderId
	 * @return ServiceContentSaveSJ
	 */
	public ServiceContentSaveSJ[] selectContentSaveSJ(String serviceOrderId);
	
	/**
	 * 更新雅典娜工单处理结果
	 * @param orderId
	 * @param verificationInfoNew
	 * @param processingResultNew
	 * @return
	 */
	public int updateAnalysisInfo(String orderId, String verificationInfoNew, String processingResultNew);

	/**
	 * 更新大模型工单处理结果
	 *
	 * */
	public int updateBigModel(String serviceId, String verificationInfoNew,String processingResultNew,String processingResultMore,String approve);

	/**
	 * 雅典娜工单处理结果归档
	 * @param orderId
	 * @return
	 */
	public int finishAnalysisInfo(String orderId);
	
	// 保存销售品信息
	public int insertServiceOfferInfo(ServiceOfferInfo soi);

	// 清空销售品信息
	public int clearServiceOfferInfo(String serviceOrderId);

	// 更新销售品信息
	public int updateServiceOfferInfo(ServiceOfferInfo soi);

	// 归档销售品信息
	public int insertServiceOfferInfoHis(String serviceOrderId);

	// 判断是否存在销售品信息
	public int countServiceOfferInfo(String serviceOrderId);

	// 保存退费业务信息
	public int saveRefundBusiness(String orderId,String offerId);

	// 退费业务重置
	public int upRefundBusiness(String orderId);
}