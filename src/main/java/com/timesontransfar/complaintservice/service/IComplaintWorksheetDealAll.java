package com.timesontransfar.complaintservice.service;

public interface IComplaintWorksheetDealAll {
	
	/**
	 * 
	 * Description:终定性-完成 <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @return <br>
	 * @CreateDate 2020年6月10日 上午10:45:35 <br>
	 */
	public String autoSheetFinish(String models);

	/**
	 * Description: 后台派单-工单审核-重新派单<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param workSheetObj
	 * @param acceptContent 处理内容
	 * @param dealType 处理类型 0为部门转派工单，1为后台派单 5 审核重新派单 3为审核退单，4为部门审批退单
	 * @return <br>
	 * @CreateDate 2020年6月9日 上午10:05:49 <br>
	 */
	public String submitAuitSheetToDeal(String sheetInfo,String acceptContent,int dealType);

    /**
     * Description: 预定性提交<br> 
     * @author huangbaijun<br>
     * @taskId <br>
     * @param json
     * @param regionId
     * @param month
     * @param dealContent
     * @param upgradeIncline
     * @param contactStatus
     * @param unifiedCode
     * @return <br>
     * @CreateDate 2020年6月15日 下午2:41:56 <br>
     */
	public String submitPreAssessYdx(String json);
	/**
	 * 
	 * Description: 新退单功能<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param worksheetId
	 * @param regionId
	 * @param month
	 * @param backReason
	 * @return <br>
	 * @CreateDate 2020年6月22日 下午6:19:46 <br>
	 */
	public String sumbitOrgBackNew(String worksheetId, int regionId, int month, String backReason);
	/**
	 * Description: 新得终定性转派方法<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param models
	 * @return <br>
	 * @CreateDate 2020年7月7日 下午3:41:19 <br>
	 */
	public String dispatchAssessNew(String models) ;
}