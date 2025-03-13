package com.timesontransfar.customservice.dbgridData;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;


public interface ComplaintMaterialsService {

	/**
	 * 根据orderId查询数据
	 * */
	public JSONObject getData(String orderId);
	
	public Map<String, Object> getMaterialsData(String orderId);

	/**
	 * 根据id删除数据
	 * */
	public int removeDataByid(String id);
	
	public int updateMaterialStatus(String id);

	/**
	 * 根据custOrderNbr删除数据
	 * */
	public int removeDataByCustOrderNbr(String custOrderNbr,String orderId);

	/**
	 * 添加数据
	 * */
	public int addData(String param);

	/**
	 * 暂存数据
	 * */
	public int stashInfo(String param);
	
	/**
	 * 工单修改，修改申诉信息
	 */
	public int modifyAppealInfo(String orderId, int regionId, String prodNum, String miitCode, String thirdLevel, int isOwner, String oldProdNum);

	/**
	 * 根据orderId查询数据
	 * */
	public JSONObject loadStashData(String orderId);

	/**
	 * 根据orderId查询电子协议定责数据
	 * */
	public JSONObject qryContractData(String orderId);

	/**
	 * 根据orderId更新核查情况
	 * */
	public int updateSituationById(String orderId,String situation);
	
	/**
	 * 根据orderId删除申诉信息
	 * @param orderId
	 * @return
	 */
	public int deleteAppealInfo(String orderId);

}
