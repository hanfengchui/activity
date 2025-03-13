package com.timesontransfar.customservice.orderask.service;

import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;

public interface RegistrationService {

	public JSONObject createRegistration(String parm);

	public GridDataInfo getHarassmentScene(int begion, int pageSize, String time, String status);

	public int setDownloadTime(String ids);

}
