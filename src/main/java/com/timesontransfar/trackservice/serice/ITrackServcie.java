package com.timesontransfar.trackservice.serice;

import java.util.List;

public interface ITrackServcie {
	/**
	 * 
	 * Description: 查询催单详情<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param orderId
	 * @return <br>
	 * @CreateDate 2020年8月7日 下午2:16:40 <br>
	 */
	public String findOrderHasten(String orderId,boolean boo);
	
	/**
	 * 查询历史单数
	 */
	public String queryWorkSheetHis(String prodNum, String relaInfo, int regionId, String orderId, boolean cliqueFlag);
	
	public int queryWorkSheetHisCount(String prodNum, String relaInfo, int regionId, String orderId, boolean cliqueFlag);
	
	public String sheetLimiteInfo(String sheetId);
	
	public int getCallOutCount(String orderId);
	
	@SuppressWarnings("rawtypes")
	public List getCallOutRecord(String orderId, boolean curFlag);

	@SuppressWarnings("rawtypes")
	public List getCallOutForOrderInfo(String orderId,String orgId, boolean curFlag);

	@SuppressWarnings("rawtypes")
	public List getSatisfyCallOutRecord(String orderId, boolean curFlag);
	
	public boolean getPlayVoiceFlag(String orderId, boolean curFlag);
	
	public boolean getSatisfyPlayVoiceFlag(String orderId, boolean curFlag);

	public boolean savePromise(String staffId, String staffName, int type);

	public boolean getPromise(String staffId, int type);

	/**
	 * 根据受理时间和prodNum查询历史单
	 */
	public String getWorkSheetHisByPordNum(String prodNum, String beginTime,String endTime,String orderId);

}
