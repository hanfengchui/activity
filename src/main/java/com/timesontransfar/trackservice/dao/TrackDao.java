package com.timesontransfar.trackservice.dao;

import java.util.List;
import java.util.Map;

@SuppressWarnings("rawtypes")
public interface TrackDao {

	public List findOrderHasten(String orderId,boolean boo);
	
	public List workSheetHisInfo(String prodNum, String relaInfo, int regionId, String orderId, boolean cliqueFlag);
	
	public int workSheetHisCount(String prodNum, String relaInfo, int regionId, String orderId, boolean cliqueFlag);
	
	/**
	 * 查询工单时限数据
	 * Description: <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param sheetId
	 * @return <br>
	 * @CreateDate 2020年8月19日 下午6:15:35 <br>
	 */
	public List querySheetLimite(String sheetId);
	
	public int getCallOutCount(String orderId);
	
	public List getCallOutRecord(String orderId, boolean curFlag);

	public List getCallOutForOrderInfo(String orderId,String orgId, String tableName);

	public List getSatisfyCallOutRecord(String orderId, boolean curFlag);

	public Map getLastRecallByOrderId(String orderId);

	public boolean getPlayVoiceFlag(String orderId, boolean curFlag);
	
	public boolean getSatisfyPlayVoiceFlag(String orderId, boolean curFlag);

	public boolean savePromise(String staffId,String staffName, int type);

	public boolean getPromise(String staffId, int type);

	public List getWorkSheetHisByPordNum(String prodNum, String beginTime,String endTime,String orderId);
}