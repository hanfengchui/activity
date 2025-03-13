package com.timesontransfar.customservice.orderask.dao;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.pojo.HarassmentScene;

public interface RegistrationDao {

	public int createRegistrationExcel(HarassmentScene harassmentScene);

	public GridDataInfo getHarassmentScene(int begion,int pageSize,String time, String status);

	public int setDownloadTime(String downloadTime,String status,String id);

	public int checkRepeatedSubmission(HarassmentScene harassmentScene);

}
