/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.especiallyCust;

import java.io.InputStream;
import java.util.Map;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface IespeciallyCust {
	
	/**
	 * 通过EXCL批量导入数据
	 * @param regionId
	 * @param file
	 * @return
	 */
	public boolean saveEspeciallyCust(int regionId,InputStream file,int modlFlag);
	
	/**
	 * 保存投诉特殊对象
	 * @param bean
	 * @return
	 */
	public String saveEspeciallyCustObj(TsEspeciallyCustInfo bean);
	
	/**
	 * 更新投诉特殊客户
	 * @param bean
	 * @return
	 */
	public int updateEspeciallyCust(TsEspeciallyCustInfo bean);
	
	/**
	 * 删除投诉客户记录
	 * @param bean
	 * @return
	 */
	public int deleteEspeciallyCust(TsEspeciallyCustInfo bean);
	

	public TsEspeciallyCustInfo getSpeciaObj(Map map);
}
