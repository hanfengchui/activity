package com.timesontransfar.customservice.dbgridData.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.*;
import com.transfar.common.utils.IdUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@Component(value="configurationDataTs")
public class ConfigurationDataTsDataTsImpl implements ConfigurationDataTs {
	private static final Logger log = LoggerFactory.getLogger(ConfigurationDataTsDataTsImpl.class);
	
	@Autowired
	private IdbgridDataPub dbgridDataPub;

	@Autowired
	private JdbcTemplate jt;

	@Autowired
	private PubFunc pubFunc;

	public GridDataInfo getConfigurationData(int begion,int pageSize,String strWhere){
		StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% FROM CC_WORKSHEET_ALLOT_CONFIG_NEW_C");
		sql1.append(strWhere);
		sql1.append(" ORDER BY PHENOMENON_TYPE_ID,REGION_ID,SERVICE_TYPE ");
		String s1 = sql1.toString();
		String strSql = s1.replace("%PARAM%","GUID AS guid,\n" +
				"PHENOMENON_TYPE_ID AS phenomenonTypeId,\n" +
				"( SELECT N FROM ccs_st_mapping_all_new WHERE n_id = PHENOMENON_TYPE_ID ) AS phenomenonTypeIdDesc,\n" +
				"REGION_ID AS regionId,\n" +
				"( SELECT REGION_NAME FROM trm_region WHERE REGION_ID = CC_WORKSHEET_ALLOT_CONFIG_NEW_C.REGION_ID ) AS regionIdDesc,\n" +
				"RECEIVE_ORG AS receiveOrg,\n" +
				"( SELECT org_name FROM tsm_organization WHERE org_id = RECEIVE_ORG ) AS receiveOrgDesc,\n" +
				"RECEIVE_STAFF AS receiveStaff,\n" +
				"SERVICE_TYPE AS serviceType,\n" +
				"( SELECT col_value_name FROM pub_column_reference WHERE TABLE_CODE = 'CC_SERVICE_ORDER_ASK' AND COL_CODE = 'SERVICE_TYPE' AND REFER_ID = SERVICE_TYPE ) AS serviceTypeDesc,\n" +
				"TACHE_ID AS tacheId,\n" +
				"( SELECT col_value_name FROM pub_column_reference WHERE TABLE_CODE = 'CC_WORK_SHEET' AND COL_CODE = 'TACHE_ID' AND REFER_ID = TACHE_ID ) AS tacheIdDesc,\n" +
				"BEST_ORDER bestOrder,\n" +
				"CASE\n" +
				"\n" +
				"WHEN BEST_ORDER = 1 THEN\n" +
				"'全部' ELSE ( SELECT col_value_name FROM pub_column_reference WHERE TABLE_CODE = 'CC_SERVICE_CONTENT_ASK' AND COL_CODE = 'BEST_ORDER' AND REFER_ID = BEST_ORDER ) \n" +
				"END AS bestOrderDesc,\n" +
				"COME_CATEGORY AS comeCategory,\n" +
				"( SELECT CONCAT_WS(' > ', four.col_value_name, three.col_value_name, two.col_value_name, one.col_value_name) AS hierarchy FROM pub_column_reference AS one\n" +
				"LEFT JOIN pub_column_reference AS two ON one.entity_id = two.refer_id LEFT JOIN pub_column_reference AS three ON two.entity_id = three.refer_id\n" +
				"LEFT JOIN pub_column_reference AS four ON three.entity_id = four.refer_id WHERE one.refer_id = COME_CATEGORY ) AS comeCategoryDesc,\n" +
				"PROD_ID AS prodId,\n" +
				"CASE\n" +
				"\n" +
				"WHEN PROD_ID = 1 THEN\n" +
				"'全部' ELSE ( SELECT col_value_name FROM pub_column_reference WHERE TABLE_CODE = 'CC_SERVICE_CONTENT_ASK' AND COL_CODE = 'PROD_TWO' AND REFER_ID = PROD_ID ) \n" +
				"END AS prodIdDesc,\n" +
				"DISPUTE_CHNL_ID AS disputeChnlId,\n" +
				"CASE\n" +
				"\n" +
				"WHEN DISPUTE_CHNL_ID = 1 THEN\n" +
				"'全部' ELSE ( SELECT col_value_name FROM pub_column_reference WHERE TABLE_CODE = 'CC_SERVICE_CONTENT_ASK' AND COL_CODE = 'DEVT_CHS_ONE' AND REFER_ID = DISPUTE_CHNL_ID ) \n" +
				"END AS disputeChnlIdDesc,\n" +
				"DVLP_CHNL_ID AS dvlpChnlId,\n" +
				"CASE\n" +
				"\n" +
				"WHEN DVLP_CHNL_ID = 1 THEN\n" +
				"'全部' ELSE (\n" +
				"SELECT\n" +
				"CONCAT( one.col_value_name, ' > ', two.col_value_name, ' > ', three.col_value_name ) \n" +
				"FROM\n" +
				"pub_column_reference one\n" +
				"LEFT JOIN pub_column_reference two ON one.refer_id = two.entity_id\n" +
				"LEFT JOIN pub_column_reference three ON two.refer_id = three.entity_id \n" +
				"WHERE\n" +
				"one.col_code = 'DEVT_CHS_ONE' \n" +
				"AND three.refer_id = DVLP_CHNL_ID \n" +
				") \n" +
				"END AS dvlpChnlIdDesc,"+
				"KEY_WORD AS keyWord ") ;
		String countSql = s1.replace("%PARAM%", "COUNT(1)");
		return this.dbgridDataPub.getResultNewBySize(countSql, strSql, begion, pageSize, "", "");
	}

	@Override
	public boolean checkRepeat(String param){
		try{
			Integer count = 0;
			String sql = "SELECT COUNT(1) AS COUNT FROM CC_WORKSHEET_ALLOT_CONFIG_NEW_C WHERE 1 = 1";
			String strSql = sql + param;
			Integer i = jt.queryForObject(strSql, Integer.class);
			count += i;
			return count > 0;
		}catch (Exception e){
			log.error("查询是否重复: 失败 {}", e.getMessage(),e);
			return true;
		}
	}

	/**
	 * 查询是否重复
	 * */
	private boolean newCheckRepeat(String param){
		try{
			Integer count = 0;
			String sql = "SELECT COUNT(1) AS COUNT FROM CC_WORKSHEET_ALLOT_CONFIG_SECOND WHERE state=1";
			String strSql = sql + param;
			Integer i = jt.queryForObject(strSql, Integer.class);
			count += i;
			return count > 0;
		}catch (Exception e){
			log.error("newCheckRepeat查询是否重复: 失败 {}", e.getMessage(),e);
			return true;
		}
	}

	@Override
	public int checkNotice(Map<String,Object> param){
		try{
			JSONObject json = new JSONObject(param);
			List<String> tacheIdArr = JSON.parseArray(json.getString("tacheId")).toJavaList(String.class);
			Integer count = 0;
			for (String tacheId : tacheIdArr) {
				String whereStr = this.getNoticeWhereStr(json, tacheId);
				String sql = "SELECT COUNT(1) AS COUNT FROM CC_WORKSHEET_ALLOT_CONFIG_NEW_C WHERE 1 = 1";
				String strSql = sql + whereStr;
				Integer i = jt.queryForObject(strSql, Integer.class);
				count += i;
			}
			return count;
		}catch (Exception e){
			log.error("查询是否有相似数据: 失败 {}", e.getMessage(),e);
			return 0;
		}
	}

	public int updateConfigurationData(Map<String,Object> param){
		try{
			JSONObject json = new JSONObject(param);
			String guid = json.getString("guid");
			String phenomenonTypeId = json.getString("phenomenonTypeId");
			String regionId = json.getString("regionId");
			String receiveOrg = json.getString("receiveOrg");
			String receiveStaff = json.getString("receiveStaff");
			String serviceType = json.getString("serviceType");
			String tacheId =json.getString("tacheId").replace("[", "").replace("]", "");
			String bestOrder = json.getString("bestOrder");
			String comeCategory = json.getString("comeCategory");
			String prodId = json.getString("prodId");
			String disputeChnlId = json.getString("disputeChnlId");
			String dvlpChnlId = json.getString("dvlpChnlId");
			String keyWord = json.getString("keyWord");
			String strSql = "UPDATE CC_WORKSHEET_ALLOT_CONFIG_NEW_C A " +
					"SET A.PHENOMENON_TYPE_ID = ?, A.REGION_ID = ?, A.RECEIVE_ORG = ?, A.RECEIVE_STAFF = ?, A.SERVICE_TYPE = ?, A.TACHE_ID = ?, A.BEST_ORDER = ?, A.COME_CATEGORY = ?, A.PROD_ID = ?, A.DISPUTE_CHNL_ID = ?, A.DVLP_CHNL_ID = ? , A.KEY_WORD = ?" +
					" WHERE A.GUID = ?";
			int i = this.jt.update(strSql,
					StringUtils.defaultIfEmpty(phenomenonTypeId,null),
					StringUtils.defaultIfEmpty(regionId,null),
					StringUtils.defaultIfEmpty(receiveOrg,null),
					StringUtils.defaultIfEmpty(receiveStaff,null),
					StringUtils.defaultIfEmpty(serviceType,null),
					StringUtils.defaultIfEmpty(tacheId,null),
					StringUtils.defaultIfEmpty(bestOrder,null),
					StringUtils.defaultIfEmpty(comeCategory,null),
					StringUtils.defaultIfEmpty(prodId,null),
					StringUtils.defaultIfEmpty(disputeChnlId,null),
					StringUtils.defaultIfEmpty(dvlpChnlId,null),
					StringUtils.defaultIfEmpty(keyWord,null),
					guid
			);
			return i;
		}catch (Exception e){
			log.error("修改数据: 失败 {}", e.getMessage(),e);
			return 0;
		}
	}

	@Override
	public int addConfigurationData(Map<String, Object> param) {
		JSONObject json = new JSONObject(param);
		List<String> tacheIdArr = JSON.parseArray(json.getString("tacheId")).toJavaList(String.class);
		List<List<String>> comeCategoryArr = JSON.parseArray(json.getString("comeCategory")).stream()
				.map(arr -> ((JSONArray) arr).toJavaList(String.class))
				.collect(Collectors.toList());
		List<String> disputeChnlIdArr = JSON.parseArray(json.getString("disputeChnlId")).toJavaList(String.class);
		List<List<String>> dvlpChnlIdLists = JSON.parseArray(json.getString("dvlpChnlId")).stream()
				.map(arr -> ((JSONArray) arr).toJavaList(String.class))
				.collect(Collectors.toList());
		List<List<String>> phenomenonTypeIdArr = JSON.parseArray(json.getString("phenomenonTypeId")).stream()
				.map(arr -> ((JSONArray) arr).toJavaList(String.class))
				.collect(Collectors.toList());

		int addedCount = 0;
		for (String tacheId : tacheIdArr) {
			for (List<String> comeCategory : comeCategoryArr) {
				addedCount += this.processPhenomenonTypeId(json, disputeChnlIdArr, dvlpChnlIdLists, tacheId, comeCategory, phenomenonTypeIdArr);
			}
		}
		return addedCount;
	}

	/**
	 * 组装智能分派配置json（新）
	 * */
	@Override
	public int newAddConfigurationData(Map<String, Object> param) {
		JSONObject json = new JSONObject(param);
		List<String> areaIdArr = JSON.parseArray(json.getString("areaId")).toJavaList(String.class);
		List<String> bestOrderArr = JSON.parseArray(json.getString("bestOrder")).toJavaList(String.class);
		List<String> custStartArr = JSON.parseArray(json.getString("custStart")).toJavaList(String.class);
		List<String> emergencyArr = JSON.parseArray(json.getString("emergency")).toJavaList(String.class);
		List<List<String>> prodIdArr = JSON.parseArray(json.getString("prodId")).stream()
				.map(arr -> ((JSONArray) arr).toJavaList(String.class))
				.collect(Collectors.toList());
		List<List<String>> comeCategoryArr = JSON.parseArray(json.getString("comeCategory")).stream()
				.map(arr -> ((JSONArray) arr).toJavaList(String.class))
				.collect(Collectors.toList());
		List<List<String>> phenomenonTypeIdArr = JSON.parseArray(json.getString("phenomenonTypeId")).stream()
				.map(arr -> ((JSONArray) arr).toJavaList(String.class))
				.collect(Collectors.toList());
		int addedCount = 0;
		for (String areaId : areaIdArr) {
			for (String bestOrder : bestOrderArr) {
				for (String custStart: custStartArr) {
					addedCount += this.newProcessPhenomenonTypeId(json, areaId, bestOrder, custStart, emergencyArr,prodIdArr,comeCategoryArr,phenomenonTypeIdArr);
				}
			}
		}
		return addedCount;
	}

	private int processPhenomenonTypeId(JSONObject json, List<String> disputeChnlIdArr, List<List<String>> dvlpChnlIdLists, String tacheId, List<String> comeCategory, List<List<String>> phenomenonTypeIdArr) {
		int count = 0;
		for (List<String> phenomenonTypeIdList : phenomenonTypeIdArr) {
			count += this.processDisputeChnlIdAndDvlpChnlId(json, disputeChnlIdArr, dvlpChnlIdLists, tacheId, comeCategory, phenomenonTypeIdList);
		}
		return count;
	}

	/**
	 * 组装智能分派配置json（新）
	 * */
	private int newProcessPhenomenonTypeId(JSONObject json, String areaId,String bestOrder,String custStart,List<String> emergencyArr,List<List<String>> prodIdArr,List<List<String>> comeCategoryArr,List<List<String>> phenomenonTypeIdArr) {
		int count = 0;
		for (String emergency : emergencyArr) {
			for(List<String> prodId : prodIdArr){
				count += this.newProcessDisputeChnlIdAndDvlpChnlId(json, areaId,bestOrder, custStart, emergency,prodId,comeCategoryArr, phenomenonTypeIdArr);
			}
		}
		return count;
	}

	private int processDisputeChnlIdAndDvlpChnlId(JSONObject json, List<String> disputeChnlIdArr, List<List<String>> dvlpChnlIdLists, String tacheId, List<String> comeCategory, List<String> phenomenonTypeIdList) {
		int count = 0;
		for (String disputeChnlId : disputeChnlIdArr) {
			for (List<String> dvlpChnlId : dvlpChnlIdLists) {
				JSONObject newJson = new JSONObject(json);
				newJson.put("tacheId", tacheId);
				newJson.put("comeCategory", comeCategory.get(comeCategory.size() - 1));
				newJson.put("disputeChnlId", disputeChnlId);
				newJson.put("dvlpChnlId", dvlpChnlId.get(dvlpChnlId.size() - 1));
				newJson.put("phenomenonTypeId", phenomenonTypeIdList.get(phenomenonTypeIdList.size() - 1));
				if (!checkRepeat(getWhereStr(newJson))) {
					addData(newJson);
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 组装智能分派配置json（新）
	 * */
	private int newProcessDisputeChnlIdAndDvlpChnlId(JSONObject json, String areaId,String bestOrder, String custStart, String emergency,List<String> prodId,List<List<String>> comeCategoryArr,List<List<String>> phenomenonTypeIdArr) {
		int count = 0;
		for (List<String> phenomenonTypeId : phenomenonTypeIdArr) {
			for (List<String> comeCategory : comeCategoryArr) {
				JSONObject newJson = new JSONObject(json);
				newJson.put("areaId", areaId);
				newJson.put("bestOrder", bestOrder);
				newJson.put("custServGrade", custStart);
				newJson.put("upTendencyFlag", emergency);
				newJson.put("prodId", prodId.get(prodId.size() - 1));
				newJson.put("askChannelId", comeCategory.get(comeCategory.size() - 1));
				newJson.put("phenomenonTypeId", phenomenonTypeId.get(phenomenonTypeId.size() - 1));
				if (!this.newCheckRepeat(this.newGetWhereStr(newJson))) {
					this.newAddData(newJson);
					count++;
				}
			}
		}
		return count;
	}

	/**
	 * 新增智能分派配置数据（新）
	 * */
	private int newAddData(JSONObject json){
		try{
			String strSql = "INSERT INTO CC_WORKSHEET_ALLOT_CONFIG_SECOND"
					+ "(GUID,SOURCE_ORG,SERVICE_TYPE,SHEET_TYPE,REGION_ID,AREA_ID,ASK_CHANNEL_ID,PHENOMENON_TYPE_ID,BEST_ORDER,PROD_ID,CUST_SERV_GRADE,"
					+ "UP_TENDENCY_FLAG,PRIORITY_LEVEL,AUTO_ZP_STAFF,RECEIVE_ORG,UPDATE_BY,UPDATE_TIME,STATE,MODIFY_TIME) " +
					"VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(),1,NOW())";
			String sourceOrg = json.getString("sourceOrg");
			String serviceType = json.getString("serviceType");
			String sheetType = json.getString("sheetType");
			String regionId = json.getString("regionId");
			String areaId = json.getString("areaId");
			String askChannelId = json.getString("askChannelId");
			String phenomenonTypeId = json.getString("phenomenonTypeId");
			String bestOrder = json.getString("bestOrder");
			String prodId = json.getString("prodId");
			String custServGrade = json.getString("custServGrade");
			String upTendencyFlag = json.getString("upTendencyFlag");
			String priorityLevel = json.getString("priorityLevel");
			String autoZpStaff = json.getString("autoZpStaff");
			String receiveOrg = json.getString("receiveOrg");
			TsmStaff staff = pubFunc.getLogonStaff();
			String id = staff.getId();
			int update = this.jt.update(strSql,
					IdUtils.fastSimpleUUID(),
					StringUtils.defaultIfEmpty(sourceOrg, null),
					StringUtils.defaultIfEmpty(serviceType, null),
					StringUtils.defaultIfEmpty(sheetType, null),
					StringUtils.defaultIfEmpty(regionId, null),
					StringUtils.defaultIfEmpty(areaId, null),
					StringUtils.defaultIfEmpty(askChannelId, null),
					StringUtils.defaultIfEmpty(phenomenonTypeId, null),
					StringUtils.defaultIfEmpty(bestOrder, null),
					StringUtils.defaultIfEmpty(prodId, null),
					StringUtils.defaultIfEmpty(custServGrade, null),
					StringUtils.defaultIfEmpty(upTendencyFlag, null),
					StringUtils.defaultIfEmpty(priorityLevel, null),
					StringUtils.defaultIfEmpty(autoZpStaff, "0"),
					StringUtils.defaultIfEmpty(receiveOrg, null), id);
			int i = 0;
			i+=update;
			return i;
		}catch (Exception e){
			log.error("新增数据: 失败 {}", e.getMessage(),e);
			return 0;
		}
	}

	private int addData(JSONObject json){
		try{
			//获取工单环节
			String strSql = "INSERT INTO CC_WORKSHEET_ALLOT_CONFIG_NEW_C (GUID,PHENOMENON_TYPE_ID, REGION_ID, RECEIVE_ORG,RECEIVE_STAFF,SERVICE_TYPE,TACHE_ID,BEST_ORDER,COME_CATEGORY,PROD_ID,DISPUTE_CHNL_ID,DVLP_CHNL_ID,KEY_WORD) " +
					"VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
			String phenomenonTypeId = json.getString("phenomenonTypeId");
			String regionId = json.getString("regionId");
			String receiveOrg = json.getString("receiveOrg");
			String receiveStaff = json.getString("receiveStaff");
			String serviceType = json.getString("serviceType");
			String bestOrder = json.getString("bestOrder");
			String comeCategory = json.getString("comeCategory");
			String prodId = json.getString("prodId");
			String disputeChnlId = json.getString("disputeChnlId");
			String dvlpChnlId = json.getString("dvlpChnlId");
			String keyWord = json.getString("keyWord");
			String tacheId = json.getString("tacheId");
			int update = this.jt.update(strSql,
					IdUtils.fastSimpleUUID(),
					StringUtils.defaultIfEmpty(phenomenonTypeId, null),
					StringUtils.defaultIfEmpty(regionId, null),
					StringUtils.defaultIfEmpty(receiveOrg, null),
					StringUtils.defaultIfEmpty(receiveStaff, null),
					StringUtils.defaultIfEmpty(serviceType, null),
					StringUtils.defaultIfEmpty(tacheId, null),
					StringUtils.defaultIfEmpty(bestOrder, null),
					StringUtils.defaultIfEmpty(comeCategory, null),
					StringUtils.defaultIfEmpty(prodId, null),
					StringUtils.defaultIfEmpty(disputeChnlId, null),
					StringUtils.defaultIfEmpty(dvlpChnlId, null),
					StringUtils.defaultIfEmpty(keyWord, null)
			);
			int i = 0;
			i+=update;
			return i;
		}catch (Exception e){
			log.error("新增数据: 失败 {}", e.getMessage(),e);
			return 0;
		}
	}

	@Override
	public int delConfigurationData(Map<String,Object> guids){
		try{
			JSONObject json = new JSONObject(guids);
			List<String> guidArr = JSON.parseArray(json.getString("guids")).toJavaList(String.class);
			String strSql = "DELETE FROM CC_WORKSHEET_ALLOT_CONFIG_NEW_C WHERE GUID = ?" ;
			int count = 0;
			for (String s : guidArr){
				int i = this.jt.update(strSql, s);
				count += i;
			}
			return count;
		}catch (Exception e){
			log.error("删除数据: 失败 {}", e.getMessage(),e);
			return 0;
		}
	}

	/**
	 * 删除配置数据(新)
	 */
	@Override
	public int newDelConfigurationData(Map<String,Object> guids){
		try{
			TsmStaff staff = pubFunc.getLogonStaff();
			String id = staff.getId();
			JSONObject json = new JSONObject(guids);
			List<String> guidArr = JSON.parseArray(json.getString("guids")).toJavaList(String.class);
			String strSql = "UPDATE cc_worksheet_allot_config_second SET state=0,delete_by=?,delete_time=NOW(),modify_time=NOW() WHERE guid=?";
			int count = 0;
			for (String s : guidArr){
				int i = this.jt.update(strSql, id, s);
				count += i;
			}
			return count;
		}catch (Exception e){
			log.error("删除数据: 失败 {}", e.getMessage(),e);
			return 0;
		}
	}


	@Override
	public String getAllDirectory(String id) {
		List<String> parentIds = new ArrayList<>();
		String parentId = id;
		parentIds.add(id);
		try {
			while (StringUtils.isNotBlank(parentId)) {
				parentId = this.getParent(parentId);
				if (StringUtils.isNotBlank(parentId) && !parentId.equals("202307") && !parentId.equals("201112")) {
					parentIds.add(parentId);
				}
			}
		} catch (Exception e) {
			log.error("getAllDirectory获取父级ID: 失败 {}", e.getMessage(),e);
			return null;
		}
		Collections.reverse(parentIds); // 逆转列表顺序
		return String.join(",", parentIds);
	}

	@Override
	public String checkContent(Map<String,Object> param) {
		JSONObject json = new JSONObject(param);
		String input = json.getString("input");
		String content = json.getString("content");
		String sql = "SELECT COUNT(1) FROM (SELECT ? as content) as R WHERE" + content;
		Object[] params = new Object[] { input};
		try {
			List<String> results = jt.queryForList(sql, params, String.class);
			return results.isEmpty() ? "0" : results.get(0);
		} catch (Exception e) {
			log.error("checkContent: 失败 {}", e.getMessage(),e);
			return null;
		}
	}

	private String getParent(String id) {
		String sql = "SELECT ENTITY_ID FROM pub_column_reference WHERE REFER_ID = ?";
		Object[] params = new Object[] { id };
		try {
			List<String> results = jt.queryForList(sql, params, String.class);
			return results.isEmpty() ? null : results.get(0);
		} catch (Exception e) {
			log.error("getParent获取父级ID: 失败 {}", e.getMessage(),e);
			return null;
		}
	}


	private String getNoticeWhereStr(JSONObject json, String tacheId){
		String regionId = json.getString("regionId");
		String receiveOrg = json.getString("receiveOrg");
		String serviceType = json.getString("serviceType");

		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(regionId)){
			sb.append(" AND REGION_ID = " + regionId);
		}
		if(StringUtils.isNotBlank(receiveOrg)){
			sb.append(" AND RECEIVE_ORG = " + receiveOrg);
		}
		if(StringUtils.isNotBlank(serviceType)){
			sb.append(" AND SERVICE_TYPE = " + serviceType);
		}
		if(StringUtils.isNotBlank(tacheId)){
			sb.append(" AND TACHE_ID =" + tacheId);
		}
		return sb.toString();
	}


	@Override
	public String getWhereStr(JSONObject json){
		String phenomenonTypeId = json.getString("phenomenonTypeId");
		String regionId = json.getString("regionId");
		String receiveOrg = json.getString("receiveOrg");
		String receiveStaff = json.getString("receiveStaff");
		String serviceType = json.getString("serviceType");
		String bestOrder = json.getString("bestOrder");
		String comeCategory = json.getString("comeCategory");
		String prodId = json.getString("prodId");
		String disputeChnlId = json.getString("disputeChnlId");
		String dvlpChnlId = json.getString("dvlpChnlId");
		String keyWord = json.getString("keyWord");
		String tacheId = json.getString("tacheId");

		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(phenomenonTypeId)){
			sb.append(" AND PHENOMENON_TYPE_ID = " + phenomenonTypeId);
		}
		if(StringUtils.isNotBlank(regionId)){
			sb.append(" AND REGION_ID = " + regionId);
		}
		if(StringUtils.isNotBlank(receiveOrg)){
			sb.append(" AND RECEIVE_ORG = " + receiveOrg);
		}
		if(StringUtils.isNotBlank(receiveStaff)){
			sb.append(" AND RECEIVE_STAFF = " + receiveStaff);
		}
		if(StringUtils.isNotBlank(serviceType)){
			sb.append(" AND SERVICE_TYPE = " + serviceType);
		}
		if(StringUtils.isNotBlank(tacheId)){
			sb.append(" AND TACHE_ID =" + tacheId);
		}
		if(StringUtils.isNotBlank(bestOrder)){
			sb.append(" AND BEST_ORDER =" + bestOrder);
		}
		if(StringUtils.isNotBlank(comeCategory)){
			sb.append(" AND COME_CATEGORY = " + comeCategory);
		}
		if(StringUtils.isNotBlank(prodId)){
			sb.append(" AND PROD_ID = " + prodId);
		}
		if(StringUtils.isNotBlank(disputeChnlId)){
			sb.append(" AND DISPUTE_CHNL_ID = " + disputeChnlId);
		}
		if(StringUtils.isNotBlank(disputeChnlId)){
			sb.append(" AND DVLP_CHNL_ID = " + dvlpChnlId);
		}
		if(StringUtils.isNotBlank(keyWord)){
			sb.append(" AND KEY_WORD = \"" + keyWord + "\"");
		}else {
			sb.append(" AND KEY_WORD IS NULL");
		}
		return sb.toString();
	}


	/**
	 * 组装智能分派配置条件（新）
	 * */
	public String newGetWhereStr(JSONObject json){
		String sourceOrg = json.getString("sourceOrg");
		String serviceType = json.getString("serviceType");
		String sheetType = json.getString("sheetType");
		String regionId = json.getString("regionId");
		String areaId = json.getString("areaId");
		String askChannelId = json.getString("askChannelId");
		String phenomenonTypeId = json.getString("phenomenonTypeId");
		String bestOrder = json.getString("bestOrder");
		String prodId = json.getString("prodId");
		String custServGrade = json.getString("custServGrade");
		String upTendencyFlag = json.getString("upTendencyFlag");
		String priorityLevel = json.getString("priorityLevel");
		String autoZpStaff = json.getString("autoZpStaff");
		String receiveOrg = json.getString("receiveOrg");

		StringBuilder sb = new StringBuilder();
		if(StringUtils.isNotBlank(sourceOrg)){
			sb.append(" AND SOURCE_ORG = " + sourceOrg);
		}
		if(StringUtils.isNotBlank(serviceType)){
			sb.append(" AND SERVICE_TYPE = " + serviceType);
		}
		if(StringUtils.isNotBlank(sheetType)){
			sb.append(" AND SHEET_TYPE = " + sheetType);
		}
		if(StringUtils.isNotBlank(regionId)){
			sb.append(" AND REGION_ID = " + regionId);
		}
		if(StringUtils.isNotBlank(areaId)){
			sb.append(" AND AREA_ID = " + areaId);
		}
		if(StringUtils.isNotBlank(askChannelId)){
			sb.append(" AND ASK_CHANNEL_ID = " + askChannelId);
		}
		if(StringUtils.isNotBlank(phenomenonTypeId)){
			sb.append(" AND PHENOMENON_TYPE_ID = " + phenomenonTypeId);
		}
		if(StringUtils.isNotBlank(bestOrder)){
			sb.append(" AND BEST_ORDER = " + bestOrder);
		}
		if(StringUtils.isNotBlank(prodId)){
			sb.append(" AND PROD_ID = " + prodId);
		}
		if(StringUtils.isNotBlank(custServGrade)){
			sb.append(" AND CUST_SERV_GRADE = " + custServGrade);
		}
		if(StringUtils.isNotBlank(upTendencyFlag)){
			sb.append(" AND UP_TENDENCY_FLAG = " + upTendencyFlag);
		}
		if(StringUtils.isNotBlank(priorityLevel)){
			sb.append(" AND PRIORITY_LEVEL = " + priorityLevel);
		}
		if(StringUtils.isNotBlank(autoZpStaff)){
			sb.append(" AND AUTO_ZP_STAFF = " + autoZpStaff);
		}
		if(StringUtils.isNotBlank(receiveOrg)){
			sb.append(" AND RECEIVE_ORG = " + receiveOrg);
		}
		return sb.toString();
	}


	/**
	 * 查询智能分派配置（新）
	 * */
	public GridDataInfo newGetConfigurationData(int begion,int pageSize,String strWhere){
		StringBuilder column = new StringBuilder();
		column.append("guid,");
		column.append("source_org sourceOrg,");
		column.append("(SELECT org_name FROM tsm_organization WHERE org_id=source_org)sourceOrgDesc,");
		column.append("service_type serviceType,");
		column.append("(SELECT col_value_name FROM pub_column_reference WHERE refer_id=service_type)serviceTypeDesc,");
		column.append("sheet_type sheetType,");
		column.append("(SELECT col_value_name FROM pub_column_reference WHERE refer_id=sheet_type)sheetTypeDesc,");
		column.append("region_id regionId,");
		column.append("IF(region_id=1,'全部',(SELECT region_name FROM trm_region WHERE region_id=a.region_id))regionIdDesc,");
		column.append("area_id areaId,");
		column.append("IF(area_id=1,'全部',(SELECT area_name FROM area_tm_daily WHERE city_id>='A'AND LENGTH(sum_std_area_id)>0 AND area_id=a.area_id))areaDesc,");
		column.append("ask_channel_id comeCategory,");
		column.append("IF(ask_channel_id=1,'全部',(SELECT CONCAT_WS(' > ',four.col_value_name,three.col_value_name,two.col_value_name,one.col_value_name)FROM pub_column_reference one LEFT JOIN pub_column_reference two ON one.entity_id=two.refer_id LEFT JOIN pub_column_reference three ON two.entity_id=three.refer_id LEFT JOIN pub_column_reference four ON three.entity_id=four.refer_id WHERE one.refer_id=ask_channel_id LIMIT 1))comeCategoryDesc,");
		column.append("phenomenon_type_id phenomenonTypeId,");
		column.append("IF(phenomenon_type_id=1,'全部',(SELECT N FROM ccs_st_mapping_all_new WHERE n_id=phenomenon_type_id LIMIT 1))phenomenonTypeIdDesc,");
		column.append("best_order bestOrder,");
		column.append("IF(best_order=1,'全部',(SELECT col_value_name FROM pub_column_reference WHERE refer_id=best_order))bestOrderDesc,");
		column.append("prod_id prodId,");
		column.append("IF(PROD_ID=1,'全部',(SELECT CONCAT(col_name,'-',col_value_name)FROM pub_column_reference WHERE refer_id=prod_id))prodIdDesc,");
		column.append("cust_serv_grade custServGrade,");
		column.append("IF(cust_serv_grade=1,'全部',(SELECT col_value_name FROM pub_column_reference WHERE refer_id=cust_serv_grade))custServGradeDesc,");
		column.append("up_tendency_flag upTendencyFlag,");
		column.append("CASE WHEN up_tendency_flag=1 THEN'全部'WHEN up_tendency_flag=100 THEN'无'WHEN up_tendency_flag=101 THEN'有曝光倾向'WHEN up_tendency_flag=102 THEN'有工信部越级倾向或通管局投诉倾向'WHEN up_tendency_flag=103 THEN'有越级倾向'WHEN up_tendency_flag=104 THEN'最严重复'END upTendencyFlagDesc,");
		column.append("priority_level,");
		column.append("receive_org receiveOrg,");
		column.append("(SELECT org_name FROM tsm_organization WHERE org_id=receive_org)receiveOrgDesc,");
		column.append("IF(auto_zp_staff=0,'否','是')autoZpStaff,");
		column.append("DATE_FORMAT(update_time,'%Y-%m-%d %H:%i:%s')as updateTime");
		StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% FROM cc_worksheet_allot_config_second a");
		sql1.append(strWhere);
		sql1.append(" ORDER BY source_org,priority_level DESC,service_type,sheet_type,region_id DESC,area_id DESC,ask_channel_id DESC,phenomenon_type_id DESC,best_order DESC,prod_id DESC,cust_serv_grade DESC,up_tendency_flag DESC");
		String s1 = sql1.toString();
		String strSql = s1.replace("%PARAM%",column) ;
		String countSql = s1.replace("%PARAM%", "COUNT(1)");
		return this.dbgridDataPub.getResultNewBySize(countSql, strSql, begion, pageSize, "", "");
	}

	// 获取智能转派虚拟工号配置
	@Override
	public List<Map<String,Object>> getAllotConfigStaff(String receiveOrg) {
		String strsql = "SELECT b.STAFF_ID AS value,STAFFNAME AS label FROM cc_worksheet_allot_config_staff_map a,tsm_staff b WHERE a.auto_zp_staff_id=b.staff_id AND receive_org=? AND auto_zp_level=2";
		List<Map<String,Object>> list = null;
		try{
			list = jt.queryForList(strsql, receiveOrg);
		}catch (Exception e){
			log.error("getAllotConfigStaff error: {}",e.getMessage(),e);
		}
		return list;
	}

	// 判断工号ID是否转派虚拟工号
	public int isAllotConfigStaffByZpStaffId(int zpStaffId) {
		String sql = "SELECT COUNT(1)FROM cc_worksheet_allot_config_staff_map a WHERE auto_zp_staff_id=" + zpStaffId;
		return jt.queryForObject(sql, Integer.class);
	}
}