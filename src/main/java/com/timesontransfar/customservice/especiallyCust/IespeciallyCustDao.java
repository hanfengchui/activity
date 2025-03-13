/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.especiallyCust;

import java.util.List;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface IespeciallyCustDao {
	
	/**
	 * 投诉特殊客户对象
	 * @param bean
	 * @return
	 */
	public int saveCustInfo(TsEspeciallyCustInfo bean);

	/**
	 * 更新投诉特殊客户信息，包括删除该记录，删除记录把该记录的状态置为0；
	 * @param bean
	 * @return
	 */
	public int updataCustInfo(TsEspeciallyCustInfo bean);
	
	public List qryTsEspecial(int regionId,String custName);

	public String queryTsEspeciallyByCustNum(String regionId, String custNum);
}