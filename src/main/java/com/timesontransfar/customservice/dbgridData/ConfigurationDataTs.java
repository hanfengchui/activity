/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData;

import com.alibaba.fastjson.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * @author 万荣伟
 *
 */
public interface ConfigurationDataTs {

	public GridDataInfo getConfigurationData(int begion,int pageSize,String strWhere);

	public GridDataInfo newGetConfigurationData(int begion,int pageSize,String strWhere);

	public int updateConfigurationData(Map<String,Object> param);

	public String getWhereStr(JSONObject json);

	public boolean checkRepeat(String param);

	public int checkNotice(Map<String,Object> param);

	public int addConfigurationData(Map<String,Object> param);

	public int newAddConfigurationData(Map<String,Object> param);

	public int delConfigurationData(Map<String,Object> guids);

	public int newDelConfigurationData(Map<String,Object> guids);

	public String getAllDirectory(String id);

	public String checkContent(Map<String,Object> param);

	public List<Map<String,Object>> getAllotConfigStaff(String receiveOrg);

	// 判断工号ID是否转派虚拟工号
	public int isAllotConfigStaffByZpStaffId(int zpStaffId);
}