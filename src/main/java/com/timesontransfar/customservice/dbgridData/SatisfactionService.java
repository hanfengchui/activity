/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData;
import java.io.InputStream;

/**
 * @author 万荣伟
 *
 */
public interface SatisfactionService {

	public String importFile(InputStream file,String fileName,String logonName);

	public GridDataInfo getRechargeData(int begion,int pageSize,String strWhere);

	public GridDataInfo getRechargeItemData(int begion,int pageSize,String uniqueFlow);

	public String startRechargeTask(String uniqueFlow);
	
}
