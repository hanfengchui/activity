package com.timesontransfar.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.timesontransfar.customservice.dbgridData.ConfigurationData;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
public class ConfigurationController {
	protected Logger log = LoggerFactory.getLogger(ConfigurationController.class);

	@Autowired
	private ConfigurationData configurationData;


	//配置数据
	@PostMapping(value = "/workflow/configuration/getConfigurationData")
	public GridDataInfo getConfigurationData(
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="pageSize", required=true)int pageSize,
			@RequestParam(value="phenomenonTypeId", required=false)String phenomenonTypeId,
			@RequestParam(value="regionId", required=false)String regionId,
			@RequestParam(value="receiveOrg", required=false)String receiveOrg,
			@RequestParam(value="receiveStaff", required=false)String receiveStaff,
			@RequestParam(value="serviceType", required=false)String serviceType,
			@RequestParam(value="tacheId", required=false)String tacheId,
			@RequestParam(value="bestOrder", required=false)String bestOrder,
			@RequestParam(value="comeCategory", required=false)String comeCategoryString,
			@RequestParam(value="prodId", required=false)String prodId,
			@RequestParam(value="disputeChnlId", required=false)String disputeChnlIdString,
			@RequestParam(value="dvlpChnlId", required=false) String dvlpChnlIdString,
			@RequestParam(value="keyWord", required=false) String keyWord){
		StringBuilder where = new StringBuilder();
		where.append(" WHERE 1 = 1");
		//投诉现象末级
		this.addPhenomenonTypeIdWhereStr(phenomenonTypeId, where);
		if(StringUtils.isNotBlank(regionId)){
			where.append(" AND REGION_ID = " + regionId);
		}
		if(StringUtils.isNotBlank(receiveOrg)){
			where.append(" AND RECEIVE_ORG = " + receiveOrg);
		}
		if(StringUtils.isNotBlank(receiveStaff)){
			where.append(" AND RECEIVE_STAFF = " + receiveStaff);
		}
		if(StringUtils.isNotBlank(serviceType)){
			where.append(" AND SERVICE_TYPE = " + serviceType);
		}
		if(StringUtils.isNotBlank(tacheId)){
			where.append(" AND TACHE_ID = " + tacheId);
		}
		if(StringUtils.isNotBlank(bestOrder)){
			where.append(" AND BEST_ORDER = " + bestOrder);
		}
		//受理渠道末级
		this.addAskChannelIdWhereStr(comeCategoryString, where," AND COME_CATEGORY in (");
		if(StringUtils.isNotBlank(prodId)){
			where.append(" AND PROD_ID = " + prodId);
		}
		if(StringUtils.isNotBlank(disputeChnlIdString)){
			this.buildSql(where,disputeChnlIdString,"DISPUTE_CHNL_ID = ");
		}
		if(StringUtils.isNotBlank(keyWord)){
			where.append(" AND KEY_WORD LIKE '%" + keyWord + "%'");
		}
		if(StringUtils.isNotBlank(dvlpChnlIdString)){
			this.buildCondition(where,dvlpChnlIdString,"DVLP_CHNL_ID = ");
		}
		return configurationData.getConfigurationData(begion,pageSize,where.toString());
	}

	//配置数据
	@PostMapping(value = "/workflow/configuration/newGetConfigurationData")
	public GridDataInfo newGetConfigurationData(
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="pageSize", required=true)int pageSize,
			@RequestParam(value="phenomenonTypeId", required=false)String phenomenonTypeId,
			@RequestParam(value="regionId", required=false)String regionId,
			@RequestParam(value="receiveOrg", required=false)String receiveOrg,
			@RequestParam(value="sourceOrg", required=false)String sourceOrg,
			@RequestParam(value="serviceType", required=false)String serviceType,
			@RequestParam(value="sheetType", required=false)String sheetType,
			@RequestParam(value="bestOrder", required=false)String bestOrder,
			@RequestParam(value="prodId", required=false)String prodIdString,
			@RequestParam(value="comeCategory", required=false)String comeCategoryString,
			@RequestParam(value="areaId", required=false)String areaIdString,
			@RequestParam(value="emergency", required=false)String emergencyString,
			@RequestParam(value="custStart", required=false)String custStartString,
			@RequestParam(value="priorityLevel", required=false)String priorityLevel){
		StringBuilder where = new StringBuilder();
		where.append(" WHERE STATE = 1");
		//投诉现象末级
		this.addPhenomenonTypeIdWhereStr(phenomenonTypeId, where);
		if(StringUtils.isNotBlank(regionId)){
			where.append(" AND REGION_ID = " + regionId);
		}
		if(StringUtils.isNotBlank(sourceOrg)){
			where.append(" AND SOURCE_ORG = " + sourceOrg);
		}
		if(StringUtils.isNotBlank(sheetType)){
			where.append(" AND SHEET_TYPE = " + sheetType);
		}
		if(StringUtils.isNotBlank(receiveOrg)){
			where.append(" AND RECEIVE_ORG = " + receiveOrg);
		}
		if(StringUtils.isNotBlank(serviceType)){
			where.append(" AND SERVICE_TYPE = " + serviceType);
		}
		if(StringUtils.isNotBlank(priorityLevel)){
			where.append(" AND PRIORITY_LEVEL = " + priorityLevel);
		}
		//受理渠道末级
		this.addAskChannelIdWhereStr(comeCategoryString, where," AND ASK_CHANNEL_ID in (");
		if(StringUtils.isNotBlank(bestOrder)){
			this.buildSql(where,bestOrder,"BEST_ORDER = ");
		}
		if(StringUtils.isNotBlank(prodIdString)){
			this.buildCondition(where,prodIdString,"PROD_ID = ");
		}
		if(StringUtils.isNotBlank(areaIdString)){
			this.buildSql(where,areaIdString,"AREA_ID = ");
		}
		if(StringUtils.isNotBlank(emergencyString)){
			this.buildSql(where,emergencyString,"UP_TENDENCY_FLAG = ");
		}
		if(StringUtils.isNotBlank(custStartString)){
			this.buildSql(where,custStartString,"CUST_SERV_GRADE = ");
		}
		return configurationData.newGetConfigurationData(begion,pageSize,where.toString());
	}
	
	private void addPhenomenonTypeIdWhereStr(String phenomenonTypeId, StringBuilder where) {
		if(StringUtils.isNotBlank(phenomenonTypeId)){
			JSONArray jsonArray = JSON.parseArray(phenomenonTypeId);
			StringBuilder phenomenonStringBuilder = new StringBuilder();
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONArray innerArray = jsonArray.getJSONArray(i);
				if (innerArray != null && !innerArray.isEmpty()) {
					String lastElement = innerArray.getString(innerArray.size() - 1);
					if (phenomenonStringBuilder.length() > 0) {
						phenomenonStringBuilder.append(",");
					}
					phenomenonStringBuilder.append(lastElement);
				}
			}
			where.append(" AND PHENOMENON_TYPE_ID in (").append(phenomenonStringBuilder).append(")");
		}
	}
	
	private void addAskChannelIdWhereStr(String comeCategoryString, StringBuilder where,String filed) {
		if(StringUtils.isNotBlank(comeCategoryString)){
			JSONArray jsonArray = JSON.parseArray(comeCategoryString);
			StringBuilder categoryStringBuilder = new StringBuilder();
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONArray innerArray = jsonArray.getJSONArray(i);
				if (innerArray != null && !innerArray.isEmpty()) {
					String lastElement = innerArray.getString(innerArray.size() - 1);
					if (categoryStringBuilder.length() > 0) {
						categoryStringBuilder.append(",");
					}
					categoryStringBuilder.append(lastElement);
				}
			}
			where.append(filed).append(categoryStringBuilder).append(")");
		}
	}


	//修改配置数据
	@PostMapping(value = "/workflow/configuration/updateConfigurationData")
	public int updateConfigurationData(@RequestBody(required=true) Map<String,Object> param){
		return configurationData.updateConfigurationData(param);
	}


	//新增直派数据
	@PostMapping(value = "/workflow/configuration/addConfigurationData")
	public int addConfigurationData(@RequestBody(required=true) Map<String,Object> param){
		return configurationData.addConfigurationData(param);
	}

	//新增直派数据(新)
	@PostMapping(value = "/workflow/configuration/newAddConfigurationData")
	public int newAddConfigurationData(@RequestBody(required=true) Map<String,Object> param){
		return configurationData.newAddConfigurationData(param);
	}

	//删除配置数据
	@PostMapping(value = "/workflow/configuration/delConfigurationData")
	public int delConfigurationData(@RequestBody(required=true) Map<String,Object> guids){
		return configurationData.delConfigurationData(guids);
	}

	//删除配置数据(新)
	@PostMapping(value = "/workflow/configuration/newDelConfigurationData")
	public int newDelConfigurationData(@RequestBody(required=true) Map<String,Object> guids){
		return configurationData.newDelConfigurationData(guids);
	}

	//查询三级Id
	@PostMapping(value = "/workflow/configuration/getAllDirectory")
	public String getAllDirectory(
			@RequestParam(value="id", required=true)String id) {
		return configurationData.getAllDirectory(id);
	}

	//检验参数
	@PostMapping(value = "/workflow/configuration/checkContent")
	public String checkContent(@RequestBody(required=true) Map<String,Object> param) {
		return configurationData.checkContent(param);
	}

	//校验数据是否重复
	@PostMapping(value = "/workflow/configuration/checkRepeat")
	public int checkRepeat(@RequestBody(required=true) Map<String,Object> param) {
		return configurationData.checkRepeat(param);
	}

	//获取智能转派虚拟工号配置
	@PostMapping(value = "/workflow/configuration/getAllotConfigStaff")
	public List<Map<String,Object>> getAllotConfigStaff(@RequestParam(value="receiveOrg", required=true)String receiveOrg) {
		return configurationData.getAllotConfigStaff(receiveOrg);
	}

	private void buildCondition(StringBuilder where,String dvlpChnlIdString,String field){
		where.append(" AND (");
		boolean first = true;
		JSONArray outerArray = JSON.parseArray(dvlpChnlIdString);
		if(!outerArray.isEmpty()){
			for (int i = 0; i < outerArray.size(); i++) {
				String innerArrayString = outerArray.getString(i);
				List<String> innerList = JSON.parseArray(innerArrayString, String.class);
				if(!innerList.isEmpty()){
					if (!first) {
						where.append(" OR ");
					}
					where.append(field).append(innerList.get(innerList.size() - 1));
					first = false;
				}
			}
		}
		where.append(")");
	}

	private void buildSql(StringBuilder where,String conditionString,String field){
		where.append(" AND (");
		boolean first = true;
		JSONArray outerArray = JSON.parseArray(conditionString);
		if(!outerArray.isEmpty()){
			for (int i = 0; i < outerArray.size(); i++) {
				if (!first) {
					where.append(" OR ");
				}
				where.append(field).append(outerArray.get(i));
				first = false;
			}
		}
		where.append(")");
	}
}
