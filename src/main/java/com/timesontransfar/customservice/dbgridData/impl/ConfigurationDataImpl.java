/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.customservice.dbgridData.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component(value="configurationDataImpl")
public class ConfigurationDataImpl implements ConfigurationData {

	private static final Logger log = LoggerFactory.getLogger(ConfigurationDataImpl.class);

	@Autowired
	private ConfigurationDataTs configurationDataTs;//投诉列表

	@Override
	public GridDataInfo getConfigurationData(int begion, int pageSize, String strWhere) {
		return configurationDataTs.getConfigurationData(begion,pageSize,strWhere);
	}

	@Override
	public GridDataInfo newGetConfigurationData(int begion, int pageSize, String strWhere) {
		return configurationDataTs.newGetConfigurationData(begion,pageSize,strWhere);
	}

	@Override
	public int updateConfigurationData(Map<String,Object> param) {
		//检查修改的数据是否与已有数据重复
		try {
			JSONObject json = new JSONObject(param);
			String tacheId = JSON.parseArray(json.getString("tacheId")).toJavaList(String.class).get(0);
			json.put("tacheId",tacheId);
			String where = configurationDataTs.getWhereStr(json);
			if(!configurationDataTs.checkRepeat(where)){
				return configurationDataTs.updateConfigurationData(param);
			}
			return 0;
		}catch (Exception e){
			log.error("updateConfigurationData 更新数据构建参数 : 失败 {}", e.getMessage(),e);
			return 0;
		}
	}

	@Override
	public int addConfigurationData(Map<String,Object> param) {
		return configurationDataTs.addConfigurationData(param);
	}

	@Override
	public int newAddConfigurationData(Map<String,Object> param) {
		return configurationDataTs.newAddConfigurationData(param);
	}

	@Override
	public int delConfigurationData(Map<String,Object> guids) {
		return configurationDataTs.delConfigurationData(guids);
	}

	@Override
	public int newDelConfigurationData(Map<String,Object> guids) {
		return configurationDataTs.newDelConfigurationData(guids);
	}

	@Override
	public String getAllDirectory(String id) {
		return configurationDataTs.getAllDirectory(id);
	}

	@Override
	public String checkContent(Map<String,Object> param) {
		return configurationDataTs.checkContent(param);
	}

	@Override
	public int checkRepeat(Map<String,Object> param) {
		return configurationDataTs.checkNotice(param);
	}

	@Override
	public List<Map<String,Object>> getAllotConfigStaff(String receiveOrg){return configurationDataTs.getAllotConfigStaff(receiveOrg);}

}