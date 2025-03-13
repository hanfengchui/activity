package com.timesontransfar.staffSkill.dao;

import java.util.List;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.staffSkill.FlowToEnd;

public interface IFlowToEndDao {
	/*
	 * 根据ID和类型查询一跟到底配置存不存在，keyType：1、生效单位，2、生效渠道，3、不生效的号码
	 */
	public int countFlowToEndConfigByIdType(String keyId, int keyType);

	/*
	 * 产品号码当前30天
	 */
	public FlowToEnd selectFlowToEndByProdNum(int regionId, String prodNum, String orgPlace);

	/*
	 * 产品号码定性回访30天
	 */
	public FlowToEnd selectFlowToEndDxhfByProdNum(int regionId, String prodNum, String orgPlace);

	/*
	 * 产品号码历史30天
	 */
	public FlowToEnd selectFlowToEndHisByProdNum(int regionId, String prodNum, String orgPlace);

	/*
	 * 联系号码当前30天
	 */
	public FlowToEnd selectFlowToEndByRelaInfo(int regionId, String relaInfo, String orgPlace);

	/*
	 * 联系号码定性回访30天
	 */
	public FlowToEnd selectFlowToEndDxhfByRelaInfo(int regionId, String relaInfo, String orgPlace);

	/*
	 * 联系号码历史30天
	 */
	public FlowToEnd selectFlowToEndHisByRelaInfo(int regionId, String relaInfo, String orgPlace);

	/*
	 * 判断之前处理员工是否长休假
	 */
	public int countFlowToEndRestConfigByDealStaffId(int dealStaffId);

	/*
	 * 由30天匹配后初始插入记录，未关联到工作量情况
	 */
	public int insertFlowToEndEmptyWorkload(FlowToEnd fte);

	/*
	 * 由30天匹配后初始插入记录，关联到工作量情况
	 */
	public int insertFlowToEndWithWorkload(FlowToEnd fte);

	/*
	 * 根据30天内员工Id查询未更新工作量情况表的记录
	 */
	public List<FlowToEnd> selectFlowToEndEmptyWorkloadByDealStaffId(int dealStaffId);

	/*
	 * 根据单号查询一跟到底前单和后单信息
	 */
	@SuppressWarnings("rawtypes")
	public List selectFlowToEndByOrderId(String orderId);

	/*
	 * 更新工作量情况表的记录
	 */
	public int updateFlowToEndWorkloadByIncrementId(String countWorkloadGuid, int incrementId);

	/*
	 * 查询一跟到底没有被提取且还在处理中的信息
	 */
	public GridDataInfo selectFlowToEndCouldForce(String dealOrg, String serviceType, String hours, int begin, String incrementId);

	/*
	 * 查询一跟到底豁免人员列表
	 */
	public GridDataInfo getExemptionData(String createStaffId, String restStaffId, int begin);

	/*
	 * 查询配置数据列表
	 */
	public GridDataInfo getConfigData(String createStaffId,String keyId,String keyType,String keyRemark,int begin);

	/**
	 *新增豁免员工数据
	 */
	public int addExemptionData(String param);

	/**
	 *新增配置数据
	 */
	public int addConfigData(String param);

	/**
	 * 逻辑删除配置数据
	 * */
	public int delConfigData(String ids);


	/**
	 * 逻辑删除豁免人员数据
	 */
	public int delExemptionData(String ids);

	/*
	 * 更新提取信息
	 */
	public int updateFlowToEndForceByIncrementId(String forceStaffId, int incrementId);
}