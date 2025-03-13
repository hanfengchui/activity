package com.timesontransfar.customservice.dbgridData.impl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.timesontransfar.customservice.dbgridData.ComplaintMaterialsService;
import com.timesontransfar.feign.ComplaintOrderFeign;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ComplaintMaterialsServiceImpl implements ComplaintMaterialsService {

	private static final Logger log = LoggerFactory.getLogger(ComplaintMaterialsServiceImpl.class);

	@Autowired
	private JdbcTemplate jt;
	
	@Autowired
	private ComplaintOrderFeign compOrderFeign;

	/**
	 * 根据orderId查询数据
	 * */
	@Override
	public JSONObject getData(String orderId){
		JSONObject jsonObject = new JSONObject();
		String sql = "SELECT ID,SERVICE_ORDER_ID,CUST_ORDER_NBR,MATERIAL_ID,MATERIAL_NAME,DATE_FORMAT(CREATE_DATE, '%Y-%m-%d %H:%i:%s') CREATE_DATE,DATE_FORMAT(UPDATE_DATE, '%Y-%m-%d %H:%i:%s') UPDATE_DATE,MATERIAL_TYPE,FTP_ID,FTP_NAME,MATERIAL_DATA FROM CC_COMPLAINT_MATERIALS WHERE SERVICE_ORDER_ID = ? ORDER BY UPDATE_DATE";
		String sql1 = "SELECT CUST_ORDER_NBR FROM CC_COMPLAINT_OFFER_DETAIL WHERE SERVICE_ORDER_ID = ? GROUP BY CUST_ORDER_NBR";
		try{
			List<Map<String, Object>> maps1 = this.jt.queryForList(sql, orderId);
			List<Map<String, Object>> maps2 = this.jt.queryForList(sql1, orderId);
			// 将 maps 中的 CUST_ORDER_NBR 提取到一个 Set 集合中
			Set<Object> mapsCustOrderNbrSet = maps1.stream()
					.map(map -> map.get("CUST_ORDER_NBR"))
					.collect(Collectors.toSet());
			// 遍历 maps2，检查是否有 CUST_ORDER_NBR 不在 maps 中
			for (Map<String, Object> map2 : maps2) {
				Object custOrderNbr = map2.get("CUST_ORDER_NBR");
				if (!mapsCustOrderNbrSet.contains(custOrderNbr)) {
					// 如果 maps 中不包含此 CUST_ORDER_NBR，则添加一个新的 Map 记录
					Map<String, Object> newMap = new HashMap<>();
					newMap.put("ID", null);
					newMap.put("SERVICE_ORDER_ID", orderId);
					newMap.put("CUST_ORDER_NBR", custOrderNbr);
					newMap.put("MATERIAL_ID", null);
					newMap.put("MATERIAL_NAME", null);
					newMap.put("CREATE_DATE", null);
					newMap.put("UPDATE_DATE", null);
					newMap.put("MATERIAL_TYPE", null);
					newMap.put("FTP_ID", null);
					newMap.put("FTP_NAME", null);
					newMap.put("MATERIAL_DATA", null);
					maps1.add(newMap);
				}
			}
			jsonObject.put("data",maps1);
		}catch (Exception e){
			log.error("getData error:{}",e.getMessage(),e);
		}
		return jsonObject;
	}
	
	public Map<String, Object> getMaterialsData(String orderId){
		String sql1 = "SELECT D.OFFER_ID,CASE WHEN (D.PKG_NAME IS NOT NULL AND D.PKG_NAME <> '') THEN CONCAT(D.OFFER_NAME, '【', D.PKG_NAME, '】') ELSE D.OFFER_NAME END OFFER_NAME "
				+ "FROM CC_COMPLAINT_OFFER_DETAIL D WHERE D.SERVICE_ORDER_ID = ? AND D.OFFER_ID IS NOT NULL AND D.OFFER_ID <> '' GROUP BY D.OFFER_ID ORDER BY D.SYS_DATE DESC, D.OFFER_ID";
		List<Map<String, Object>> list1 = this.jt.queryForList(sql1, orderId);
		
		String sql2 = "SELECT D.OFFER_ID,CASE WHEN (D.PKG_NAME IS NOT NULL AND D.PKG_NAME <> '') THEN CONCAT(D.OFFER_NAME, '【', D.PKG_NAME, '】') ELSE D.OFFER_NAME END OFFER_NAME, "
				+ "D.CUST_ORDER_NBR FROM CC_COMPLAINT_OFFER_DETAIL D WHERE D.SERVICE_ORDER_ID = ? AND D.OFFER_ID IS NOT NULL AND D.OFFER_ID <> '' "
				+ "AND D.CUST_ORDER_NBR IS NOT NULL AND D.CUST_ORDER_NBR <> '' "
				+ "GROUP BY D.OFFER_ID,D.CUST_ORDER_NBR";
		List<Map<String, Object>> list2 = this.jt.queryForList(sql2, orderId);
		
		String sql3 = "SELECT ID,SERVICE_ORDER_ID,OFFER_ID,CUST_ORDER_NBR,MATERIAL_ID,MATERIAL_NAME,DATE_FORMAT(CREATE_DATE, '%Y-%m-%d %H:%i:%s') CREATE_DATE,"
				+ "DATE_FORMAT(UPDATE_DATE, '%Y-%m-%d %H:%i:%s') UPDATE_DATE,MATERIAL_TYPE,FTP_ID,FTP_NAME,MATERIAL_DATA,IFNULL(STATUS, '0') STATUS FROM CC_COMPLAINT_MATERIALS "
				+ "WHERE SERVICE_ORDER_ID = ? ORDER BY CUST_ORDER_NBR DESC, MATERIAL_ID, UPDATE_DATE";
		List<Map<String, Object>> list3 = this.jt.queryForList(sql3, orderId);
		
		for(int i=0; i<list1.size(); i++) {
			Map<String, Object> map = list1.get(i);
			String offerId = map.get("OFFER_ID").toString();
			
			//筛选出OFFER_ID关联的CUST_ORDER_NBR
			List<Map<String, Object>> tmpList = list2.stream().filter(o -> offerId.equals(o.get("OFFER_ID").toString())).collect(Collectors.toList());
			
			List<Map<String, Object>> tableData = new ArrayList<>();
			for(Map<String, Object> m : tmpList) {
				String custOrderNbr = m.get("CUST_ORDER_NBR").toString();
				List<Map<String, Object>> tmpList1 = list3.stream().filter(o -> custOrderNbr.equals(this.getStringByKey(o, "CUST_ORDER_NBR")) && offerId.equals(this.getStringByKey(o, "OFFER_ID"))).collect(Collectors.toList());
				tableData.addAll(tmpList1);
			}
			tableData = tableData.stream().sorted//按时间升序
					((c1, c2) -> MapUtils.getString(c1, "UPDATE_DATE").compareTo(MapUtils.getString(c2, "UPDATE_DATE"))).collect(Collectors.toList());
			map.put("tableData", tableData);
		}
		
		List<String> materialIdList = new ArrayList<>();
		for(Map<String, Object> m : list3) {
			String offerId = this.getStringByKey(m, "OFFER_ID");
			String custOrderNbr = this.getStringByKey(m, "CUST_ORDER_NBR");
			List<Map<String, Object>> tmpList = list2.stream().filter(o -> custOrderNbr.equals(this.getStringByKey(o, "CUST_ORDER_NBR")) && offerId.equals(this.getStringByKey(o, "OFFER_ID"))).collect(Collectors.toList());
			if(!tmpList.isEmpty()) {
				Map<String, Object> tmpMap = tmpList.get(0);
				String offerName = tmpMap.get("OFFER_NAME").toString();
				m.put("OFFER_NAME", offerName);
			}
			
			String materialId = this.getStringByKey(m, "MATERIAL_ID");
			if(StringUtils.isBlank(materialId)) {
				continue;
			}
			if(!materialIdList.contains(materialId)) {
				materialIdList.add(materialId);
			}
		}
		List<Map<String, Object>> materialList = new ArrayList<>();
		for(String materialId : materialIdList) {
			Map<String, Object> m = new HashMap<>();
			List<Map<String, Object>> tmpList = list3.stream().filter(o -> materialId.equals(this.getStringByKey(o, "MATERIAL_ID"))).collect(Collectors.toList());
			tmpList = tmpList.stream().sorted//按时间升序
					((c1, c2) -> MapUtils.getString(c1, "UPDATE_DATE").compareTo(MapUtils.getString(c2, "UPDATE_DATE"))).collect(Collectors.toList());
			m.put("MATERIAL_ID", materialId);
			m.put("tableData", tmpList);
			materialList.add(m);
		}
		
		//过滤删除状态的文件
		list3 = list3.stream().filter(o -> (!"1".equals(this.getStringByKey(o, "STATUS")))).collect(Collectors.toList());
		
		Map<String, Object> newMap = new HashMap<>();
		newMap.put("list1", list1);
		newMap.put("list2", materialList);
		newMap.put("list3", list3);
		return newMap;
	}
	
	@SuppressWarnings("rawtypes")
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}

	/**
	 * 根据id删除数据
	 * */
	@Override
	public int removeDataByid(String id){
		int result = 0;
		String sql = "DELETE FROM CC_COMPLAINT_MATERIALS WHERE ID = ?";
		try{
			result = this.jt.update(sql, id);
		}catch (Exception e){
			log.error("removeDataByid error :{}", e.getMessage(),e);
		}
		return result;
	}
	
	/**
	 * 根据id将材料临时删除
	 * */
	@Override
	public int updateMaterialStatus(String id){
		int result = 0;
		String sql = "UPDATE CC_COMPLAINT_MATERIALS SET STATUS = '1' WHERE ID = ?";
		try{
			result = this.jt.update(sql, id);
		}catch (Exception e){
			log.error("updateMaterialStatus error :{}", e.getMessage(),e);
		}
		return result;
	}
	
	/**
	 * 根据custOrderNbr删除数据
	 * */
	@Override
	public int removeDataByCustOrderNbr(String custOrderNbr,String orderId){
		int result = 0;
		String sql = "SELECT CUST_ORDER_NBR FROM CC_COMPLAINT_OFFER_DETAIL WHERE OFFER_ID = ? AND SERVICE_ORDER_ID = ?";
		String sql2 = "DELETE FROM CC_COMPLAINT_MATERIALS WHERE OFFER_ID = ? AND CUST_ORDER_NBR = ? AND SERVICE_ORDER_ID = ?";
		String sql3 = "DELETE FROM CC_COMPLAINT_OFFER_DETAIL WHERE OFFER_ID = ? AND SERVICE_ORDER_ID = ?";
		try{
			List<Map<String, Object>> list = this.jt.queryForList(sql, custOrderNbr,orderId);
			if(!list.isEmpty()){
				result = this.jt.update(sql3, custOrderNbr, orderId);
				if(result > 0) {
					for (int i = 0; i < list.size(); i++) {
						Map<String, Object> stringObjectMap = list.get(i);
						String orderNbr = (String)stringObjectMap.get("CUST_ORDER_NBR");
						this.jt.update(sql2, custOrderNbr, orderNbr, orderId);
					}
				}
			}
		}catch (Exception e){
			log.error("removeDataByCustOrderNbr error :{}", e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 添加数据
	 * */
	@Override
	public int addData(String param){
		String sql = "INSERT INTO CC_COMPLAINT_MATERIALS (SERVICE_ORDER_ID, OFFER_ID, CUST_ORDER_NBR, MATERIAL_ID,MATERIAL_NAME,UPDATE_DATE,CREATE_DATE,MATERIAL_TYPE,FTP_ID,FTP_NAME,MATERIAL_DATA) VALUES " +
				"(?,?,?,?,?,now(),now(),?,?,?,?);";
		int result = 0;
		try{
			JSONObject json = JSON.parseObject(param);
			String orderId = json.getString("orderId");
			String materialId = json.getString("materialId");
			String materialName = json.getString("materialName");
			String materialType = json.getString("materialType");
			String ftpId = json.getString("ftpId");
			String ftpName = json.getString("ftpName");
			String materialData = json.getString("materialData");
			String offerId = json.getString("offerId");
			String custOrderNbr = "";
			if(StringUtils.isNotBlank(offerId)){
				String qryCustNbrSql = "SELECT CUST_ORDER_NBR FROM CC_COMPLAINT_OFFER_DETAIL WHERE SERVICE_ORDER_ID = ? AND OFFER_ID = ? "
						+ "AND CUST_ORDER_NBR IS NOT NULL AND CUST_ORDER_NBR <> '' "
						+ "GROUP BY CUST_ORDER_NBR LIMIT 1";
				List<Map<String, Object>> list = this.jt.queryForList(qryCustNbrSql, orderId, offerId);
				if(!list.isEmpty()) {
					Map<String, Object> map = list.get(0);
					custOrderNbr = map.get("CUST_ORDER_NBR") == null ? null : map.get("CUST_ORDER_NBR").toString();
				}
			}
			result = this.jt.update(sql, orderId, offerId, StringUtils.defaultIfEmpty(custOrderNbr, null), 
					StringUtils.defaultIfEmpty(materialId, null), StringUtils.defaultIfEmpty(materialName, null), 
					StringUtils.defaultIfEmpty(materialType, null), StringUtils.defaultIfEmpty(ftpId, null), 
					StringUtils.defaultIfEmpty(ftpName, null), StringUtils.defaultIfEmpty(materialData, null));
		}catch (Exception e){
			log.error("addData error :{}", e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 添加数据
	 * */
	@Override
	public int stashInfo(String param){
		int result = 0;
		String sql = "UPDATE CC_APPEAL_INFORMATION SET PROD_NUM = ?,IS_FAULT = ?,APPEAL_REASON = ?,USER_INFO = ?,HANDLING_SITUATION = ?,PROCESS_RESULT = ?,UPDATE_DATE = now() WHERE SERVICE_ORDER_ID = ?";
		try{
			JSONObject jsonObject = JSON.parseObject(param);
			String orderId = jsonObject.getString("orderId");
			String prodNum = jsonObject.getString("prodNum");
			String isFault = jsonObject.getString("isFault");
			String appealReason = jsonObject.getString("appealReason");
			String userInfo = jsonObject.getString("userInfo");
			String handlingSituation = jsonObject.getString("handlingSituation");
			String processResult = jsonObject.getString("processResult");
			result = this.jt.update(sql, prodNum, isFault,
					StringUtils.defaultIfEmpty(appealReason, null), StringUtils.defaultIfEmpty(userInfo, null),
					StringUtils.defaultIfEmpty(handlingSituation, null), StringUtils.defaultIfEmpty(processResult, null), orderId);
		}catch (Exception e){
			log.error("stashInfo error: {}", e.getMessage(), e);
		}
		return result;
	}
	
	/**
	 * 工单修改，修改申诉信息
	 * */
	@Override
	public int modifyAppealInfo(String orderId, int regionId, String prodNum, String miitCode, String thirdLevel, int isOwner, String oldProdNum){
		try {
			JSONObject json = new JSONObject();
			json.put("orderId", orderId);
			json.put("regionId", regionId);
			json.put("prodNum", prodNum);
			json.put("miitCode", miitCode);
			json.put("thirdLevel", thirdLevel);
			json.put("isOwner", isOwner);
			compOrderFeign.updateAppealInfo(json.toJSONString());
		} catch (Exception e) {
			log.error("updateAppealInfo error: {}", e.getMessage(), e);
        }
		//清空销售品及佐证材料
		return this.removeDataByOrderId(orderId);
	}

	/**
	 * 根据orderId查询数据
	 * */
	@Override
	public JSONObject loadStashData(String orderId){
		JSONObject jsonObject = new JSONObject();
		String sql = "SELECT ID,SERVICE_ORDER_ID,PROD_NUM,CUST_NAME,MIIT_CODE,THIRD_LEVEL,IS_FAULT,APPEAL_REASON,USER_INFO,HANDLING_SITUATION,PROCESS_RESULT,CREATE_DATE,UPDATE_DATE FROM CC_APPEAL_INFORMATION WHERE SERVICE_ORDER_ID = ?";
		try{
			List<Map<String, Object>> maps = this.jt.queryForList(sql, orderId);
			jsonObject.put("data",maps);
		}catch (Exception e){
			log.error("loadStashData error:{}",e.getMessage(),e);
		}
		return jsonObject;
	}

	public JSONObject qryContractData(String orderId){
		JSONObject jsonObject = new JSONObject();
		String sql = "SELECT ID,SERVICE_ORDER_ID,OFFER_ID,OFFER_NAME,CUST_ORDER_NBR,DOC_KEY,SUMMARY,DETAIL,"
				+ "DATE_FORMAT(CREATE_TIME, '%Y-%m-%d %H:%i:%s') CREATE_TIME,IS_RESPONSIBLE,"
				+ "if(RES_TYPE=1, '电子协议', if(RES_TYPE=2,'短信', '')) RES_TYPE "
				+ "FROM CC_ELECTRONIC_CONTRACT WHERE SERVICE_ORDER_ID = ? ORDER BY CREATE_TIME";
		try{
			List<Map<String, Object>> maps = this.jt.queryForList(sql, orderId);
			jsonObject.put("data",maps);
		}catch (Exception e){
			log.error("qryContractData error:{}",e.getMessage(),e);
		}
		return jsonObject;
	}

	/**
	 * 根据orderId更新核查情况
	 * */
	@Override
	public int updateSituationById(String orderId,String situation){
		int result = 0;
		String qrySql = "SELECT HANDLING_SITUATION FROM CC_APPEAL_INFORMATION WHERE SERVICE_ORDER_ID = ?";
		String upSql = "UPDATE CC_APPEAL_INFORMATION SET HANDLING_SITUATION = ? WHERE SERVICE_ORDER_ID = ?";
		try{
			String handlingSituation = this.jt.queryForObject(qrySql, new Object[]{orderId}, String.class);
			if(StringUtils.isNotBlank(handlingSituation)){
				handlingSituation = situation + "\n" + handlingSituation;
				result = this.jt.update(upSql, handlingSituation, orderId);
			}else {
				result = this.jt.update(upSql, situation, orderId);
			}
		}catch (Exception e){
			log.error("updateSituationById error:{}",e.getMessage(),e);
		}
		return result;
	}
	
	/**
	 * 根据服务单号删除销售品及佐证材料
	 * @param orderId
	 * @return
	 */
	private int removeDataByOrderId(String orderId){
		int result = 0;
		String sql1 = "DELETE FROM CC_COMPLAINT_OFFER_DETAIL WHERE SERVICE_ORDER_ID = ?";
		String sql2 = "DELETE FROM CC_COMPLAINT_MATERIALS WHERE SERVICE_ORDER_ID = ?";
		String sql3 = "DELETE FROM CC_ELECTRONIC_CONTRACT WHERE SERVICE_ORDER_ID = ?";
		try{
			result += this.jt.update(sql1, orderId);
			result += this.jt.update(sql2, orderId);
			result += this.jt.update(sql3, orderId);
		}catch (Exception e){
			log.error("removeDataByOrderId error: {}", e.getMessage(), e);
		}
		return result;
	}
	
	public int deleteAppealInfo(String orderId){
		int result = 0;
		String sql = "DELETE FROM CC_APPEAL_INFORMATION WHERE SERVICE_ORDER_ID = ?";
		try{
			result = this.jt.update(sql, orderId);
		}catch (Exception e){
			log.error("deleteAppealInfo error: {}", e.getMessage(), e);
		}
		log.info("deleteAppealInfo orderId: {} result: {}", orderId, result);
		if(result > 0) {//清空销售品及佐证材料
			this.removeDataByOrderId(orderId);
		}
		return result;
	}

	public JSONObject qryDynamicScenario(String param){
		log.info("qryDynamicScenario param: {}",param);
		JSONObject jsonObject = new JSONObject();
		try{
			JSONObject req = JSON.parseObject(param);
			String sql = "SELECT ID,REGION,SALES_PRODUCT_NAME,SALES_PRODUCT_ID,REMINDER_CONTENT,USAGE_CHANNEL,OPERATOR,CREATE_TIME,UPDATE_TIME FROM CC_DYNAMIC_SCENARIO WHERE 1 = 1 ";
			String region = req.getString("region");
			String salesProductId = req.getString("salesProductId");
			String usageChannel = req.getString("usageChannel");
			String operator = req.getString("operator");
			StringBuilder where = new StringBuilder();
			List<Object> params = new ArrayList<>();
			if(StringUtils.isNotBlank(region)){
				where.append("AND REGION = ?");
				params.add(region);
			}
			if(StringUtils.isNotBlank(salesProductId)){
				where.append("AND SALES_PRODUCT_ID = ?");
				params.add(salesProductId);
			}
			if(StringUtils.isNotBlank(usageChannel)){
				where.append("AND USAGE_CHANNEL = ?");
				params.add(usageChannel);
			}
			if(StringUtils.isNotBlank(operator)){
				where.append("AND OPERATOR = ?");
				params.add(operator);
			}
			List<Map<String, Object>> list = this.jt.queryForList(sql + where, params.toArray());
			jsonObject.put("data",list);
		}catch (Exception e){
			log.error("qryDynamicScenario error: {}",e.getMessage(),e);
		}
		return jsonObject;
	}

	public JSONObject addDynamicScenario(String param){//新增时是否校验重复？
		log.info("addDynamicScenario param: {}",param);
		JSONObject result = new JSONObject();
		try{
			JSONObject jsonObject = JSON.parseObject(param);
			String region = jsonObject.getString("region");
			String salesProductId = jsonObject.getString("salesProductId");
			String reminderContent = jsonObject.getString("reminderContent");
			String usageChannel = jsonObject.getString("usageChannel");
			String operator = jsonObject.getString("operator");
			int i = this.checkDynamicScenarioRepet(region, salesProductId, reminderContent, usageChannel);
			if(i != 0){
				result.put("code",-1);
				result.put("num",0);
				result.put("msg","存在重复类型数据");
				return result;
			}
			String sql = "INSERT INTO CC_DYNAMIC_SCENARIO " +
					"(REGION,SALES_PRODUCT_NAME,SALES_PRODUCT_ID,REMINDER_CONTENT,USAGE_CHANNEL,OPERATOR,CREATE_TIME)" +
					"values" +
					"(?,?,?,?,?,?,now())";
			int update = this.jt.update(sql, region, "", salesProductId, reminderContent, usageChannel, operator);
			result.put("code",0);
			result.put("num",update);
			result.put("msg","添加成功");
		}catch (Exception e){
			log.error("addDynamicScenario error: {}",e.getMessage(),e);
		}
		return result;
	}

	private int checkDynamicScenarioRepet(String region,String salesProductId,String reminderContent,String usageChannel){
		log.info("checkDynamicScenarioRepet region: {} salesProductId: {} reminderContent: {} usageChannel: {}",region,salesProductId,reminderContent,usageChannel);
		int result = 0;
		try{
			String sql = "SELECT COUNT(*) FROM CC_DYNAMIC_SCENARIO WHERE REGION = ? AND SALES_PRODUCT_ID = ? AND REMINDER_CONTENT = ? AND USAGE_CHANNEL = ?";
			result = this.jt.queryForObject(sql, new Object[]{region, salesProductId, reminderContent, usageChannel}, Integer.class);
		}catch (Exception e){
			log.error("checkDynamicScenarioRepet: {}",e.getMessage(),e);
		}
		return result;
	}

	private int checkShortTermScenario(String param){
		int result = 0;
		try{
			JSONObject jsonObject = JSON.parseObject(param);
			String scenarioName = jsonObject.getString("scenarioName");
			String region = jsonObject.getString("region");
			String productNumber = jsonObject.getString("productNumber");
			String contactPhone = jsonObject.getString("contactPhone");
			String customerName = jsonObject.getString("customerName");
			String acceptanceContent = jsonObject.getString("acceptanceContent");
			String processingContent = jsonObject.getString("processingContent");
			String identifier = jsonObject.getString("identifier");
			String expirationTime = jsonObject.getString("expirationTime");
			String reminderContent = jsonObject.getString("reminderContent");
			String usageChannel = jsonObject.getString("usageChannel");
			String operator = jsonObject.getString("operator");

			String sql = "SELECT COUNT(*) FROM CC_DYNAMIC_SCENARIO WHERE SCENARIO_NAME = ? AND REGION = ? AND PRODUCT_NUMBER = ? AND CONTACT_PHONE = ? " +
					"AND CUSTOMER_NAME = ? AND ACCEPTANCE_CONTENT = ? AND PROCESSING_CONTENT = ? AND IDENTIFIER = ? EXPIRATION_TIME = ? AND REMINDER_CONTENT = ?" +
					"AND USAGE_CHANNEL = ? AND OPERATOR = ?";
			result = this.jt.queryForObject(sql, new Object[]{scenarioName,region,productNumber,contactPhone,customerName,acceptanceContent,processingContent,
																identifier,expirationTime,reminderContent,usageChannel,operator}, Integer.class);
		}catch (Exception e){
			log.error("checkShortTermScenario error: {}",e.getMessage(),e);
		}
		return result;
	}

	public int delDynamicScenario(String ids){
		log.info("delDynamicScenario id: {}",ids);
		int result = 0;
		String sql = "DELETE FROM CC_DYNAMIC_SCENARIO WHERE ID = ?";
		try{
			JSONArray array = JSON.parseArray(ids);
			for (int i = 0; i < array.size(); i++) {
				String id = array.getString(i);
				result += this.jt.update(sql, id);
			}
		}catch (Exception e){
			log.error("delDynamicScenario error: {}",e.getMessage(),e);
		}
		return result;
	}

	public JSONObject qryShortTermScenario(String param){
		log.info("qryShortTermScenario param: {}",param);
		JSONObject jsonObject = new JSONObject();
		try{
			JSONObject req = JSON.parseObject(param);
			String sql = "SELECT ID,SCENARIO_NAME,REGION,PRODUCT_NUMBER,CONTACT_PHONE,CUSTOMER_NAME,ACCEPTANCE_CONTENT,PROCESSING_CONTENT,IDENTIFIER,EXPIRATION_TIME,REMINDER_CONTENT,USAGE_CHANNEL,OPERATOR,CREATE_TIME,UPDATE_TIME FROM CC_SHORT_TERM_SCENARIO WHERE 1 = 1 ";
			String scenarioName = req.getString("scenarioName");
			String region = req.getString("region");
			String productNumber = req.getString("productNumber");
			String contactPhone = req.getString("contactPhone");
			String customerName = req.getString("customerName");
			String identifier = req.getString("identifier");
			String usageChannel = req.getString("usageChannel");
			String operator = req.getString("operator");

			StringBuilder where = new StringBuilder();
			List<Object> params = new ArrayList<>();
			if(StringUtils.isNotBlank(scenarioName)){
				where.append("AND SCENARIO_NAME = ?");
				params.add(region);
			}
			if(StringUtils.isNotBlank(region)){
				where.append("AND REGION = ?");
				params.add(region);
			}
			if(StringUtils.isNotBlank(productNumber)){
				where.append("AND PRODUCT_NUMBER = ?");
				params.add(region);
			}
			if(StringUtils.isNotBlank(contactPhone)){
				where.append("AND CONTACT_PHONE = ?");
				params.add(region);
			}
			if(StringUtils.isNotBlank(customerName)){
				where.append("AND CUSTOMER_NAME = ?");
				params.add(region);
			}
			if(StringUtils.isNotBlank(identifier)){
				where.append("AND IDENTIFIER = ?");
				params.add(region);
			}
			if(StringUtils.isNotBlank(usageChannel)){
				where.append("AND USAGE_CHANNEL = ?");
				params.add(region);
			}
			if(StringUtils.isNotBlank(operator)){
				where.append("AND OPERATOR = ?");
				params.add(region);
			}
			List<Map<String, Object>> list = this.jt.queryForList(sql + where, params.toArray());
			jsonObject.put("data",list);
		}catch (Exception e){
			log.error("qryShortTermScenario error: {}",e.getMessage(),e);
		}
		return jsonObject;
	}

	public JSONObject addShortTermScenario(String param){//新增时是否校验重复？
		log.info("addShortTermScenario param: {}",param);
		JSONObject result = new JSONObject();
		try{
			JSONObject jsonObject = JSON.parseObject(param);
			String scenarioName = jsonObject.getString("scenarioName");
			String region = jsonObject.getString("region");
			String productNumber = jsonObject.getString("productNumber");
			String contactPhone = jsonObject.getString("contactPhone");
			String customerName = jsonObject.getString("customerName");
			String acceptanceContent = jsonObject.getString("acceptanceContent");
			String processingContent = jsonObject.getString("processingContent");
			String identifier = jsonObject.getString("identifier");
			String expirationTime = jsonObject.getString("expirationTime");
			String reminderContent = jsonObject.getString("reminderContent");
			String usageChannel = jsonObject.getString("usageChannel");
			String operator = jsonObject.getString("operator");

			int i = this.checkShortTermScenario(param);
			if(i != 0){
				result.put("code",-1);
				result.put("num",0);
				result.put("msg","存在重复类型数据");
				return result;
			}
			String sql = "INSERT INTO CC_SHORT_TERM_SCENARIO " +
					"(SCENARIO_NAME,REGION,PRODUCT_NUMBER,CONTACT_PHONE,CUSTOMER_NAME,ACCEPTANCE_CONTENT,PROCESSING_CONTENT,IDENTIFIER,EXPIRATION_TIME,REMINDER_CONTENT,USAGE_CHANNEL,OPERATOR,CREATE_TIME)" +
					"values" +
					"(?,?,?,?,?,?,?,?,?,?,?,?,now())";
			int update = this.jt.update(sql, scenarioName, region, productNumber, contactPhone, customerName, acceptanceContent,processingContent,identifier,expirationTime,reminderContent,usageChannel,operator);
			result.put("code",0);
			result.put("num",update);
			result.put("msg","添加成功");
		}catch (Exception e){
			log.error("addShortTermScenario error: {}",e.getMessage(),e);
		}
		return result;
	}

	public int delShortTermScenario(String ids){
		log.info("delDynamicScenario id: {}",ids);
		int result = 0;
		String sql = "DELETE FROM CC_SHORT_TERM_SCENARIO WHERE ID = ?";
		try{
			JSONArray array = JSON.parseArray(ids);
			for (int i = 0; i < array.size(); i++) {
				String id = array.getString(i);
				result += this.jt.update(sql, id);
			}
		}catch (Exception e){
			log.error("delShortTermScenario error: {}",e.getMessage(),e);
		}
		return result;
	}
}