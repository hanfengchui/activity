/**
 * <p>类名：PubFunc.java</p>
 * <p>功能描叙：</p>
 * <p>设计依据：</p>
 * <p>开发者：lifeng</p>
 * <p>开发/维护历史：</p>
 * <p>  Create by:	lifeng	Mar 18, 2008 </p>
 * <p></p>
 */
package com.timesontransfar.customservice.common;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.cliqueWorkSheetWebService.pojo.ComplaintRelationRmp;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.ideal.sso.SsoUtil;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.common.web.pojo.ChannelPojo;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.orderask.pojo.IntfLog;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceAcceptDir;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.worksheet.dao.ItsSheetQualitative;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.feign.custominterface.PortalInterfaceFeign;
import com.transfar.common.utils.JWTUtils;
import com.transfar.config.RedisType;
import com.transfar.utils.RedisUtils;

@Component(value="pubFunc")
@SuppressWarnings("rawtypes")
public class PubFunc {
	protected Logger log = LoggerFactory.getLogger(PubFunc.class);
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
    private ISystemAuthorization systemAuthorization;
	@Autowired
	private RedisUtils redisUtils;
	
	@Autowired
	private PortalInterfaceFeign portalFeign;
	
	@Autowired
    private ItsSheetQualitative sheetQualitative;// 投诉定性表


	public String getBssStaffCode(String logonName){
    	String strSql = "select a.LOGONNAME, a.STAFF_ID, a.STAFFNAME, CASE WHEN c.REGION_LEVEL = '97D' THEN c.SUPER_ID WHEN c.REGION_LEVEL = '97C' THEN c.REGION_ID ELSE 2 END REGION_ID, a.AGENT_ID, c.REGION_TELNO " +
    			"from tsm_staff a, tsm_organization b, trm_region c where a.org_id = b.org_id and a.logonname = ? " +
    			"and c.region_id = b.region_id";
    	List staffList = this.jdbcTemplate.queryForList(strSql, logonName);
    	if (!staffList.isEmpty()) {
    		Map map = (Map) staffList.get(0);
    		String staffId = map.get("STAFF_ID").toString();
    		String regionId = map.get("REGION_ID") == null ? "" : map.get("REGION_ID").toString();
    		
    		strSql = "select p.EXTERNAL_USER from (select SSO_APP_NAME,REPLACE(DOMAIN_USER, '\\\\', '/') as DOMAIN_USER,EXTERNAL_USER from ccs_st_ssocredentialmapping) p, " +
    				"ccs_st_ssoapplicationinfo i, " +
    				"ccs_st_ssoapplicationtype t " +
    				"where p.sso_app_name = i.sso_app_name " +
    				"and (p.domain_user = ? or p.domain_user like concat('%/' , ?)) " +
    				"and t.sso_app_type = 'BSS' " +
    				"AND i.area_center = (SELECT r.area_center FROM trm_region r WHERE r.region_id = t.region_id limit 1) " +
    				"and t.region_id = ? " +
    				"and i.area_center = t.area_center " +
    				"and i.sso_app_name = t.sso_app_name " +
    				"limit 1";
    		List list = this.jdbcTemplate.queryForList(strSql, staffId, logonName, regionId);
    		if (!list.isEmpty()) {
    			map = (Map) list.get(0);
    			return map.get("EXTERNAL_USER") == null ? "" : map.get("EXTERNAL_USER").toString();
    		}
		}
    	return "";
    }

	public String getBestOrderFlag(String keyId, String conditionType) {
		String sql = "select VALUE_ID from cc_static_map where KEY_ID = ? and CONDITION_TYPE = ?";
		List list = this.jdbcTemplate.queryForList(sql, keyId, conditionType);
		if (!list.isEmpty()) {
			Map map = (Map) list.get(0);
			return map.get("VALUE_ID") == null ? "" : map.get("VALUE_ID").toString();
		}
		return "";
	}
    
    /**
     * 根据条件获取收单部门/员工
     */
	public Map getSheetAllotConfig(int servType,int tacheId,String configKey,String configType) {
		Map tmpMap = null;
    	try {
    		String tmpString = this.redisUtils.get("ALLOT_CONFIG.Map__s_"+servType+"__t_"+tacheId+"__c_"+configKey+"__c_"+configType, RedisType.WORKSHEET);
    		tmpMap = JSON.parseObject(tmpString, Map.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils.get异常: ALLOT_CONFIG.Map {}", e.getMessage());
    	}
    	
    	if(tmpMap == null) {
    		try {
	    		String strsql = "SELECT * FROM CC_WORKSHEET_ALLOT_CONFIG WHERE service_type=? and tache_id=? and config_key=? and config_type=?";
	    		List tmpList = jdbcTemplate.queryForList(strsql, servType, tacheId, configKey, configType);
	    		if(!tmpList.isEmpty()) {
	    			tmpMap = (Map)tmpList.get(0);
	    			String jsonString = JSON.toJSONString(tmpMap);
	                this.redisUtils.setex("ALLOT_CONFIG.Map__s_"+servType+"__t_"+tacheId+"__c_"+configKey+"__c_"+configType,86400*30,jsonString,RedisType.WORKSHEET);
	    		}
    		}
	    	catch(Exception e) {
	    		log.error("redisUtils.setex异常: ALLOT_CONFIG.Map {}", e.getMessage());
	    	}
    	}
    	return tmpMap;
    }
	
    /**
     * 工单流向配置
     */
	public Map getSheetAllotConfigNew(int servType,int tacheId,String configKey,String configType,int bestOrder,int comeFrom) {
		Map tmpMap = null;
    	try {
    		String tmpString = this.redisUtils.get("ALLOT_CONFIG.Map__s_"+servType+"__t_"+tacheId+"__c_"+configKey+"__c_"+configType+"__b_"+bestOrder+"__c_"+comeFrom, RedisType.WORKSHEET);
    		tmpMap = JSON.parseObject(tmpString, Map.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：ALLOT_CONFIG.Map {}", e.getMessage());
    	}
    	
    	if(tmpMap == null) {
    		String strsql = "SELECT * FROM CC_WORKSHEET_ALLOT_CONFIG WHERE service_type=? and tache_id=? and config_key=? and config_type=? and BEST_ORDER=? and COME_CATEGORY=?";
    		List tmpList = jdbcTemplate.queryForList(strsql, servType, tacheId, configKey, configType, bestOrder, comeFrom);
    		if(!tmpList.isEmpty()) {
    			tmpMap = (Map)tmpList.get(0);
    			String jsonString = JSON.toJSONString(tmpMap);
                this.redisUtils.setex("ALLOT_CONFIG.Map__s_"+servType+"__t_"+tacheId+"__c_"+configKey+"__c_"+configType+"__b_"+bestOrder+"__c_"+comeFrom,86400*30,jsonString,RedisType.WORKSHEET);
    		}
    	}
    	return tmpMap;
    }
	
    /**
     * 四类工单直派配置
     */
	public Map getSheetAllotConfigNew(String phnTypeId, String regionId, int servType, int tacheId, int comeFrom, ServiceContent cont) {
		int bestOrder = cont.getBestOrder();
		int prodId = cont.getProdTwo();
		int dvlpChnlId = cont.getDevtChsThree();
		
		List tmpList = null;
    	try {
    		String tmpString = this.redisUtils.get("ALLOT_CONFIG.LIST__p_"+phnTypeId+"__r_"+regionId+"__s_"+servType+"__t_"+tacheId+"__b_"
    				+bestOrder+"__c_"+comeFrom+"__p_"+prodId+"__d_"+dvlpChnlId, RedisType.WORKSHEET);
    		tmpList = JSON.parseObject(tmpString, List.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils.get异常: ALLOT_CONFIG.LIST {}", e.getMessage());
    	}
		if(tmpList == null) {
			try {
				String strsql = "SELECT * FROM CC_WORKSHEET_ALLOT_CONFIG_NEW_C "
						+ "WHERE PHENOMENON_TYPE_ID=? AND REGION_ID=? AND SERVICE_TYPE=? AND TACHE_ID=? "
						+ "AND (BEST_ORDER=? OR BEST_ORDER=1) AND COME_CATEGORY=? AND (PROD_ID=? OR PROD_ID=1) AND (DVLP_CHNL_ID=? OR DVLP_CHNL_ID=1)";
				tmpList = jdbcTemplate.queryForList(strsql, phnTypeId, regionId, servType, tacheId, bestOrder, comeFrom, prodId, dvlpChnlId);
				JSONArray jsonArray = JSONArray.fromObject(tmpList);
	            this.redisUtils.setex("ALLOT_CONFIG.LIST__p_"+phnTypeId+"__r_"+regionId+"__s_"+servType+"__t_"+tacheId+"__b_"
	            		+bestOrder+"__c_"+comeFrom+"__p_"+prodId+"__d_"+dvlpChnlId, 21600, jsonArray.toString(), RedisType.WORKSHEET);
			}
			catch(Exception e) {
	    		log.error("redisUtils.setex异常: ALLOT_CONFIG.LIST {}", e.getMessage());
	    	}
    	}
		if(tmpList != null && !tmpList.isEmpty()) {
			return getReceiveMap(tmpList, cont);
		}
    	return Collections.emptyMap();
    }

	@SuppressWarnings("unchecked")
	private Map getReceiveMap(List tmpList, ServiceContent cont){
		String orderId = cont.getServOrderId();
		List configList = this.getMatchKeyWordList(orderId, tmpList);
		if(configList.isEmpty()) {
			return Collections.emptyMap();
		}
		
		String disputeChnl = cont.getDisputeChnl1();
		for (Map<String, Object> stringObjectMap : (List<Map<String, Object>>)configList) {
			int disputeChnlId = stringObjectMap.get("DISPUTE_CHNL_ID") == null ? 0 : Integer.parseInt(stringObjectMap.get("DISPUTE_CHNL_ID").toString());
			if(1 == disputeChnlId){//配置项为所有属性
				return stringObjectMap;
			}
			else if(0 == disputeChnlId && (StringUtils.isBlank(disputeChnl) || "0".equals(disputeChnl))){//配置项为空 && 本单属性值为空
				return stringObjectMap;//匹配
			}
			else if (disputeChnlId > 1){//配置项为具体值
				log.info("config.disputeChnlId: {} disputeChnl: {}", disputeChnlId, disputeChnl);
				/* 本单属性值 != 空 && 本单属性值与配置值相同 */
				if((StringUtils.isNotBlank(disputeChnl) && !"0".equals(disputeChnl)) && disputeChnlId == Integer.parseInt(disputeChnl)){
					return stringObjectMap;//匹配流向部门
				} 
			}
		}
		return Collections.emptyMap();
	}
	
	@SuppressWarnings("unchecked")
	private List getMatchKeyWordList(String orderId, List tmpList) {
		List<Map<String, Object>> emptyList = new ArrayList<>();
		List<Map<String, Object>> matchList = new ArrayList<>();
		for (Map<String, Object> configMap : (List<Map<String, Object>>)tmpList) {
			String keyWord = this.getStringByKey(configMap, "KEY_WORD");
			log.info("matchKeyWord keyWord: {}", keyWord);
			
			String matchFlag = this.matchKeyWord(orderId, keyWord);
			log.info("matchKeyWord result: {}", matchFlag);
			if(StringUtils.isEmpty(matchFlag)) {//未配置关键字或关键字异常
				emptyList.add(configMap);
			} else if("1".equals(matchFlag)) {
				matchList.add(configMap);
			}
		}
		if(!matchList.isEmpty()) {
			log.info("keyWord match: {}", JSON.toJSON(matchList));
			return matchList;
		}
		log.info("keyWord isEmpty: {}", JSON.toJSON(emptyList));
		return emptyList;
	}
	
	/**
	 * null：未配置或配置异常；0-未匹配；1-匹配
	 */
	private String matchKeyWord(String orderId, String keyWord) {
		if(StringUtils.isBlank(keyWord)) {//直派配置关键字
			return null;
		}
		
		String flag = null;
		try {
			String sqlWhere = StringUtils.replace(keyWord, "受理内容", "ACCEPT_CONTENT");
			sqlWhere += ") AND SERVICE_ORDER_ID = '" + orderId + "'";
			
			String sqlStr = "SELECT COUNT(1) FROM CC_SERVICE_CONTENT_ASK WHERE (" + sqlWhere;
			int num = this.jdbcTemplate.queryForObject(sqlStr, Integer.class);
			if(num > 0) {
				flag = "1";
			} else {
				flag = "0";
			}
		} catch(Exception e) {
			log.error("matchKeyWord orderId: {} error: {}", orderId, e.getMessage(), e);
		}
		return flag;
	}

	// 分公司智能转派,241114
	public Map getSheetAllotConfigSecond(String sourceOrg, int sheetType, OrderAskInfo askInfo, ServiceContent content,
			OrderCustomerInfo customer, ServiceLabel label) {
		int serviceType = askInfo.getServType();
		int regionId = askInfo.getRegionId();
		int areaId = askInfo.getAreaId();
		int lastChannel = getLastChannel(askInfo);
		int xxId = getLastXX(content);
		int bestOrder = content.getBestOrder();
		int prodId = content.getProdTwo();
		int custServGrade = customer.getCustServGrade();
		int upTendencyFlag = convertUpTendency(label, askInfo);
		StringBuffer key = new StringBuffer("ALLOT_CONFIG_SECOND.LIST");
		StringBuffer sql = new StringBuffer("SELECT*FROM cc_worksheet_allot_config_second WHERE state=1");
		key.append("__o_").append(sourceOrg);// 原收单部门ID
		sql.append(" AND source_org='").append(sourceOrg).append("'");
		key.append("__s_").append(serviceType);// 服务类型
		sql.append(" AND service_type=").append(serviceType);
		key.append("__t_").append(sheetType);// 工单环节
		sql.append(" AND sheet_type=").append(sheetType);
		key.append("__r_").append(regionId);// 地域ID
		sql.append(" AND(region_id=").append(regionId).append(" OR region_id=1)");
		key.append("__a_").append(areaId);// 区县ID
		sql.append(" AND(area_id=").append(areaId).append(" OR area_id=1)");
		key.append("__c_").append(lastChannel);// 最末级渠道ID
		sql.append(" AND(ask_channel_id=").append(lastChannel).append(" OR ask_channel_id=1)");
		key.append("__x_").append(xxId);// 最末级投诉现象ID
		sql.append(" AND(phenomenon_type_id=").append(xxId).append(" OR phenomenon_type_id=1)");
		key.append("__b_").append(bestOrder);// 最严工单
		sql.append(" AND(best_order=").append(bestOrder).append(" OR best_order=1)");
		key.append("__p_").append(prodId);// 最末级产品ID
		sql.append(" AND(prod_id=").append(prodId).append(" OR prod_id=1)");
		key.append("__g_").append(custServGrade);// 客户星级
		sql.append(" AND(cust_serv_grade=").append(custServGrade).append(" OR cust_serv_grade=1)");
		key.append("__u_").append(upTendencyFlag);// 越级倾向
		sql.append(" AND(up_tendency_flag=").append(upTendencyFlag).append(" OR up_tendency_flag=1)");
		sql.append(" ORDER BY priority_level DESC");// 按照优先级：数字从大到小排序
		List list = null;
		try {
			String tmpString = this.redisUtils.get(key.toString(), RedisType.WORKSHEET);
			list = JSON.parseObject(tmpString, List.class);
		} catch (Exception e) {
			log.error("redisUtils.get异常: ALLOT_CONFIG_SECOND.LIST {}", e.getMessage());
		}
		if (null == list || list.isEmpty()) {
			try {
				list = jdbcTemplate.queryForList(sql.toString());
				JSONArray jsonArray = JSONArray.fromObject(list);
				this.redisUtils.setex(key.toString(), 21600, jsonArray.toString(), RedisType.WORKSHEET);
			} catch (Exception e) {
				log.error("redisUtils.setex异常: ALLOT_CONFIG_SECOND.LIST {}", e.getMessage());
			}
		}
		if (null != list && !list.isEmpty()) {
			return (Map) list.get(0);
		}
		return Collections.emptyMap();
	}

	public int saveApiHttpLog(String serverId, String serverName, String exeResult, String serviceOrderId, String remark) {
		String sql = "INSERT INTO cc_api_http_log (server_id, server_name, create_time, exe_result, service_order_id, remark) VALUES (?, ?, now(), ?, ?, ?)";
		try {
			return this.jdbcTemplate.update(sql, serverId, serverName, exeResult, serviceOrderId, remark );
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List qryFullAddress(String telNo,String address){
    	Map intfMap = this.getIntfMap("qryFullAddressFromOSS");
        if(null == intfMap){
        	return Collections.emptyList();
        }
		String urlStr = intfMap.get("ADDRESS_IP").toString();
		String areaCode = getRegionTelNo(Integer.parseInt(telNo));
		String jsonStr =
						"{" +
						"  \"body\": {" + 
						"    \"address\": \""+address+"\"," + 
						"    \"minScore\": \"90\"" + 
						"  }," + 
						"  \"head\": {" + 
						"    \"actionCode\": \"181113\"," + 
						"    \"appType\": \"4\"," + 
						"    \"areaCode\": \""+areaCode+"\"," + 
						"    \"auth\": \"4b20a1dc2775e8a9de3943f66d667411\"," + 
						"    \"channelId\": \"ZT\"," + 
						"    \"loginNbr\": \"18068982856\"," + 
						"    \"loginType\": \"2000004\"," + 
						"    \"requestTime\": \"2018111115450376687\"," + 
						"    \"tranId\": \"20181111154503636393\"," + 
						"    \"userAgent\": \"\"" + 
						"  }" + 
						"}";
		Map<String,String> strMap = getURLContentPostNew(urlStr, jsonStr, "application/json", 3000, "UTF-8");
		if(strMap.get("code").equals("0")) {
			try{
				String rspStr = strMap.get("msg");
				
				JsonObject rspObj =  new Gson().fromJson(rspStr, JsonObject.class);
				JsonObject head = rspObj.getAsJsonObject("head");
				if("00".equals(head.get("status").getAsString())){
					JsonObject body = rspObj.getAsJsonObject("body");
					JsonObject data = body.getAsJsonObject("data");
					JsonArray array = data.getAsJsonArray("address");
					
					return new Gson().fromJson(array, new TypeToken<List>(){}.getType());
				}
			} catch (Exception e) {
				log.error("qryFullAddress 中台封装标准地址接口异常：{}", e.toString());
			}
		}
		return Collections.emptyList();
    }

	public Map getReferIdMap(int referId){
		Map tmpMap = null;
    	try {
    		String tmpString = this.redisUtils.get("PUB_COLUMN_REFERENCE_STATUS.Map__"+referId, RedisType.WORKSHEET);
    		tmpMap = JSON.parseObject(tmpString, Map.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：PUB_COLUMN_REFERENCE_STATUS.Map {}", e.getMessage());
    	}
    	
    	if(tmpMap == null) {
    		String strsql = "SELECT * FROM PUB_COLUMN_REFERENCE_STATUS WHERE REFER_ID=?";
    		List tmpList = jdbcTemplate.queryForList(strsql, referId);
    		if(!tmpList.isEmpty()) {
    			tmpMap = (Map)tmpList.get(0);
    			String jsonString = JSON.toJSONString(tmpMap);
                this.redisUtils.setex("PUB_COLUMN_REFERENCE_STATUS.Map__"+referId,86400,jsonString,RedisType.WORKSHEET);
    		}
    	}
    	return tmpMap;
    }


    public Map getIntfMap(String intfName) {
    	Map intfMap = null;
    	try {
    		String tmpString = this.redisUtils.get("ADDRESSCONFIG__"+intfName, RedisType.WORKSHEET);
			Map tmp = JSON.parseObject(tmpString,Map.class);
			if(tmp != null) {
				intfMap = tmp;
			}
			else {
				String strsql = "SELECT * FROM CC_ADDRESS_ITCONFIG T WHERE T.ADDRESS_NAME=?";
		    	List list = jdbcTemplate.queryForList(strsql, intfName);
		    	if (!list.isEmpty()) {
		    		intfMap = (Map) list.get(0);
				}
				JSONObject jsonObject= JSONObject.fromObject(intfMap);
		    	this.redisUtils.setex("ADDRESSCONFIG__"+intfName,86400,jsonObject.toString(),RedisType.WORKSHEET);
			}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	return intfMap;
    }
    
    /**
     * 话术小结扩展目录
     */
	public List getExtendTsDir(String dirId,String flag) {
    	if("1".equals(flag)) {//扩展一级
    		return portalFeign.getExtendByDirId(dirId);
    	}
    	else {//扩展二三级
    		return portalFeign.getSummaryExpandId(dirId);
    	}
    }
    
    public List loadColumnsByEntityStatus(String tableCode, String colCode, String entityId) {
    	List result = new ArrayList();
    	if(entityId == null || entityId.trim().equals("")){
    		return result;
    	}
    	if(tableCode == null || tableCode.trim().equals("")){
    		return result;
    	}
    	if(colCode == null || colCode.trim().equals("")){
    		return result;
    	}
    	String strsql = "select r.*,s.BUSINESSID,s.BUSINESS_TYPE from pub_column_reference r left join pub_column_reference_status s " +
    			"on r.refer_id=s.refer_id where r.table_code=? and r.col_code=? and r.entity_id=? " +
    			"and (s.status is null or s.status <> '1') order by r.col_order";
		result = jdbcTemplate.queryForList(strsql, tableCode, colCode, entityId);
		return result;
	}
    
    public String getHandleSubCardUrl(String prodNum,String custName,String relaInfo,String assistSellNo) {
    	try {
    		String logonName = this.getLogonStaff().getLogonName();
        	
        	Map<String, String> paramMap = new HashMap<String, String>();
    		paramMap.put("prodNum", prodNum);//产品号码
    		paramMap.put("custName", custName);//客户姓名
    		paramMap.put("relaNum", relaInfo);//联系电话
    		paramMap.put("dealStaffCode", logonName);//客服工号
    		paramMap.put("assistSellNo", assistSellNo);//协销工号
    		String paramStr = new Gson().toJson(paramMap);//加密前的入参
    		
    		PortalDESEncrypt des = PortalDESEncrypt.getInstance("`[ XX6_Sun1n9@RXF ]`");
            String result = des.encrypt(paramStr);//加密后的入参
            
    		String url = "https://js.189.cn/umall/addFuCard/addFuIndex?parme=";//生产链接
//    		String url = "https://wttest.go189.cn/umall/addFuCard/addFuIndex?parme=";//测试链接
            url += result;
            return url;
    	}
    	catch(Exception e) {
    		//异常处理逻辑
    	}
    	return "fail";
    }

	public List checkRepeatUnified(String regionId, String prodNum, String relaInfo) {
		String repeatDays = this.querySysContolSwitch("repeatDays");
		if ("0".equals(repeatDays)) {
			return Collections.emptyList();
		}
		if (!StringUtils.isNumeric(repeatDays)) {
			repeatDays = "7";
		}
		String qrySql =
"SELECT SERVICE_TYPE, SERVICE_ORDER_ID\n" +
"  FROM (SELECT 'unified' service_type, a.service_order_id, a.accept_date\n" + 
"          FROM cc_service_order_ask a, cc_cmp_unified_return b\n" + 
"         WHERE a.service_order_id = b.complaint_worksheet_id\n" + 
"           AND a.service_type = 720130000\n" + 
"           AND a.region_id = ?\n" + 
"           AND (a.prod_num IN (?, ?) OR a.rela_info IN (?, ?))\n" + 
"           AND a.accept_date >= date_sub(now(), interval ? day)\n" + 
"        UNION ALL\n" + 
"        SELECT 'unified', a.service_order_id, a.accept_date\n" + 
"          FROM cc_service_order_ask_his a, cc_cmp_unified_return b\n" + 
"         WHERE a.service_order_id = b.complaint_worksheet_id\n" + 
"           AND a.service_type = 720130000\n" + 
"           AND a.region_id = ?\n" + 
"           AND (a.prod_num IN (?, ?) OR a.rela_info IN (?, ?))\n" + 
"           AND a.order_statu IN (700000103, 3000047, 720130002, 720130010)\n" + 
"           AND a.accept_date >= date_sub(now(), interval ? day)\n" + 
"        UNION ALL\n" + 
"        SELECT 'unified', a.service_order_id, a.accept_date\n" + 
"          FROM cc_service_order_ask_his a, cc_cmp_unified_return_his b\n" + 
"         WHERE a.service_order_id = b.complaint_worksheet_id\n" + 
"           AND a.service_type = 720130000\n" + 
"           AND a.region_id = ?\n" + 
"           AND (a.prod_num IN (?, ?) OR a.rela_info IN (?, ?))\n" + 
"           AND a.order_statu IN (700000103, 3000047, 720130002, 720130010)\n" + 
"           AND a.accept_date >= date_sub(now(), interval ? day)) AS RT\n" + 
" ORDER BY accept_date DESC";
		List<String> argList = new ArrayList<>();
		argList.add(regionId);
		argList.add(prodNum);
		argList.add(relaInfo);
		argList.add(prodNum);
		argList.add(relaInfo);
		argList.add(repeatDays);
		argList.add(regionId);
		argList.add(prodNum);
		argList.add(relaInfo);
		argList.add(prodNum);
		argList.add(relaInfo);
		argList.add(repeatDays);
		argList.add(regionId);
		argList.add(prodNum);
		argList.add(relaInfo);
		argList.add(prodNum);
		argList.add(relaInfo);
		argList.add(repeatDays);
		return jdbcTemplate.queryForList(qrySql, argList.toArray());
	}

	public List getProdNumHis(String regionId, String prodNum, String lastXX) {
		String qrySql =
"SELECT a.SERVICE_TYPE,\n" +
"       CASE\n" + 
"         WHEN  DATE_ADD(a.accept_date,INTERVAL 1 DAY) < NOW() THEN\n" + 
"          1\n" + 
"         ELSE\n" + 
"          0\n" + 
"       END H24\n" + 
"  FROM cc_service_order_ask a, cc_service_content_ask b\n" + 
" WHERE a.service_order_id = b.service_order_id \n" + 
"   AND a.accept_date >= date_sub(now(),interval 30 day)\n" + 
"   AND a.service_type IN (720130000, 700006312, 720200003)\n" + 
"   AND a.order_statu IN (700000101, 700000104, 700001493, 720130004, 720130005, 720130006)\n" + 
"   AND a.region_id = ?\n" + 
"   AND a.prod_num = ?\n" + 
"   AND a.rela_type = ?\n" + 
" ORDER BY a.accept_date DESC";
		return jdbcTemplate.queryForList(qrySql, regionId, prodNum, lastXX);
	}

    public String getSelRefundHotName(int keyId, String hotId) {
    	String strsql = "SELECT HOT_NAME FROM ccs_hotpoint_key WHERE key_id = ? AND hot_id = ?";
		List result = jdbcTemplate.queryForList(strsql, keyId, hotId);
        if(result.isEmpty()) {
        	return "";
        }
        return ((Map)result.get(0)).get("HOT_NAME").toString();
	}

    public int saveYNSJIntfLog(IntfLog log) {
    	String sql = "insert into cc_intf_log (GUID, SERVICE_ORDER_ID, CREATE_DATE, IN_MSG, OUT_MSG, ACTION_FLAG, ACTION_RESULT, SYSTEM) " +
    					"values (UPPER(replace(UUID(), '-', '')), ?, NOW(), ?, ?, ?, ?, ?)";
    	int i = 0;
    	try {
    		i = this.jdbcTemplate.update(sql, 
    				log.getServOrderId(),
    				log.getInMsg(),
    				log.getOutMsg(),
	    			log.getActionFlag(),
	    			log.getActionResult(),
	    			log.getSystem());
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	return i;
    }

    public String getOperType(int referId) {
		Map columnMap = this.getColumnMap(String.valueOf(referId));
    	if(columnMap == null) {
    		return "";
    	}
    	return columnMap.get("COL_VALUE_HANDLING") == null ? "" : columnMap.get("COL_VALUE_HANDLING").toString();
    }

	public List getPlusOneList(String lastYY) {
		String qrySql = "SELECT DISTINCT plus_one, plus_one_desc FROM ccs_st_qualitative_plus WHERE six_grade_catalog = ?";
		return jdbcTemplate.queryForList(qrySql, lastYY);
	}

	public List getPlusTwoList(String lastYY, String plusOne) {
		String qrySql = "SELECT plus_two, plus_two_desc FROM ccs_st_qualitative_plus WHERE six_grade_catalog = ? AND plus_one = ? AND plus_type = 2 ORDER BY plus_order";
		return jdbcTemplate.queryForList(qrySql, lastYY, plusOne);
	}

	@SuppressWarnings({ "unchecked" })
	public String getLastDealContentHis(String orderId) {
		String content = "";
		String qrySql = 
"SELECT deal_content\n" +
"  FROM cc_work_sheet_his\n" + 
" WHERE sheet_type IN\n" + 
"       (720130011, 720130013, 720130014, 700000126, 700000127)\n" + 
"   AND deal_content <> '系统自动处理'\n" + 
"   AND respond_date IS NOT NULL\n" + 
"   AND service_order_id = ?\n" + 
" ORDER BY respond_date DESC";
		List list = jdbcTemplate.query(qrySql, new Object[] { orderId }, new KeyRowMapper());
		if (list.isEmpty()) {
			return "";
		}
		if (null != list.get(0)) {
			content = list.get(0).toString();
		}
		list.clear();
		list = null;
		return content;
	}

	public String getLastSTSPDStaff(String orderId) {
		String dealStaff = "";
		String dealOrgId = "";
		String qrySql = "SELECT DEAL_STAFF, DEAL_ORG_ID FROM cc_work_sheet WHERE tache_id IN (720130021, 700000085) AND service_order_id = ? ORDER BY creat_date DESC";
		List list = jdbcTemplate.queryForList(qrySql, orderId);
		if (list.isEmpty()) {
			return "0";
		}
		dealStaff = ((Map) list.get(0)).get("DEAL_STAFF").toString();
		dealOrgId = ((Map) list.get(0)).get("DEAL_ORG_ID").toString();
		String areaOrgId = getAreaOrgId(dealOrgId);
		if (!"278".equals(areaOrgId)) {
			dealStaff = "0";
		}
		return dealStaff;
	}

	public String getLastPDLogonNameHis(String orderId) {
		String sql = "SELECT b.LOGONNAME FROM cc_work_sheet_his a,tsm_staff b WHERE a.deal_staff=b.staff_id AND a.tache_id IN(720130021,700000085)"
				+ "AND a.service_order_id=? ORDER BY a.creat_date DESC";
		List list = jdbcTemplate.queryForList(sql, orderId);
		if (list.isEmpty()) {
			return "0";
		}
		return ((Map) list.get(0)).get("LOGONNAME").toString();
	}

	public int saveQualitativeChooseLog(String serviceOrderId, String workSheetId, String chooseFlag, String chooseMs, String lastYY, String tacheDesc) {
		TsmStaff staff = getLogonStaff();
		String sql = "INSERT INTO cc_qualitative_choose_log(service_order_id,work_sheet_id,choose_flag,choose_ms,choose_date,six_grade_catalog,tache_desc,deal_staff)VALUES(?,?,?,?,NOW(),?,?,?)";
		return jdbcTemplate.update(sql, serviceOrderId, workSheetId, chooseFlag, chooseMs, lastYY, tacheDesc, staff.getId());
	}

	@SuppressWarnings({ "unchecked" })
	public String getLastDealContent(String orderId) {
		String content = "";
		String qrySql = 
"SELECT deal_content\n" +
"  FROM cc_work_sheet\n" + 
" WHERE sheet_type IN\n" + 
"       (720130011, 720130013, 720130014, 700000126, 700000127)\n" + 
"   AND deal_content <> '系统自动处理'\n" + 
"   AND respond_date IS NOT NULL\n" + 
"   AND service_order_id = ?\n" + 
" ORDER BY respond_date DESC";
		List list = jdbcTemplate.query(qrySql, new Object[] { orderId }, new KeyRowMapper());
		if (list.isEmpty()) {
			qrySql = "SELECT accept_content FROM cc_service_content_ask WHERE service_order_id = ?";
			list = jdbcTemplate.query(qrySql, new Object[] { orderId }, new KeyRowMapper());
		}
		if (null != list.get(0)) {
			content = list.get(0).toString();
		}
		list.clear();
		list = null;
		return content;
	}

	public Map getLastDealInfo(String orderId) {
		Map lastDealInfo = null;
		String qrySql = 
"SELECT c.WORK_SHEET_ID, c.DEAL_STAFF, c.DEAL_STAFF_NAME, c.DEAL_ORG_ID, c.DEAL_ORG_NAME, c.DEAL_CONTENT,t.LOGONNAME\n" +
"  FROM cc_work_sheet c left join tsm_staff t on c.DEAL_STAFF = t.staff_id\n" +
" WHERE sheet_type IN\n" + 
"       (720130011, 720130013, 720130014, 700000126, 700000127, 600000075, 600000077, 600000069)\n" +
"   AND deal_content <> '系统自动处理'\n" + 
"   AND respond_date IS NOT NULL\n" + 
"   AND service_order_id = ?\n" + 
" ORDER BY respond_date DESC";
		List list = jdbcTemplate.queryForList(qrySql, orderId);
		if (!list.isEmpty()) {
			lastDealInfo = (Map) list.get(0);
		}
		return lastDealInfo;
	}
	
	public Map getLastDealInfoHis(String orderId) {
		Map lastDealInfo = null;
		String qrySql = 
"SELECT c.WORK_SHEET_ID, c.DEAL_STAFF, c.DEAL_STAFF_NAME, c.DEAL_ORG_ID, c.DEAL_ORG_NAME, c.DEAL_CONTENT,t.LOGONNAME\n" +
"  FROM cc_work_sheet_his c left join tsm_staff t on c.DEAL_STAFF = t.staff_id\n" +
" WHERE sheet_type IN\n" + 
"       (720130011, 720130013, 720130014, 700000126, 700000127, 600000075, 600000077, 600000069)\n" +
"   AND deal_content <> '系统自动处理'\n" + 
"   AND respond_date IS NOT NULL\n" + 
"   AND service_order_id = ?\n" + 
" ORDER BY respond_date DESC";
		List list = jdbcTemplate.queryForList(qrySql, orderId);
		if (!list.isEmpty()) {
			lastDealInfo = (Map) list.get(0);
		}
		return lastDealInfo;
	}

	public int saveCmpSupplementModify(String cwi, String cpto, String cptn, String cio, String cin, String dro, String drn, String drs) {
		TsmStaff staff = getLogonStaff();
		if (cpto.equals(cptn)) {
			cpto = "";
			cptn = "";
		}
		if (cio.replaceAll("(\r\n|\n)", "").equals(cin.replaceAll("(\r\n|\n)", ""))) {
			cio = "";
			cin = "";
		}
		if (dro.equals(drn)) {
			dro = "";
			drn = "";
			drs = "";
		} else {
			modifyDealResult(drn, drs);
		}
		String sql = "INSERT INTO cc_cmp_supplement_modify(complaint_worksheet_id, complaint_phenomenon_type_o, complaint_info_o, deal_result_o, complaint_phenomenon_type_n, complaint_info_n, deal_result_n, modify_date, modify_staff, deal_result_sheet) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)";
		return jdbcTemplate.update(sql, cwi, cpto, cio, dro, cptn, cin, drn, staff.getId(), drs);
	}

	public void modifyDealResult(String drn, String drs) {
		String sql = "UPDATE cc_work_sheet SET deal_content = ? WHERE work_sheet_id = ?";
		jdbcTemplate.update(sql, drn, drs);
		sql = "UPDATE cc_worksheet_deal_type SET deal_content = ? WHERE worksheet_id = ?";
		jdbcTemplate.update(sql, drn, drs);
	}

	public String getTicket(String areaCode,String callNo){
    	DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
		Date now = new Date();
		
    	TsmStaff tsms = this.getLogonStaff();
		String loginName = tsms.getLogonName();
		
		String checkKey = "123456";
		String timestamp = df.format(now);
		String pageType = "301";
		String sign = YaXinMD5.getMD5Code(loginName + "|" + areaCode + "|"+ checkKey + "|"+ timestamp + "|"+ pageType);
		
		JsonObject json = new JsonObject();
		json.addProperty("areaCode", areaCode);
		json.addProperty("channelId", "20000");//本地客服:20000
		json.addProperty("staffcode", loginName);
		json.addProperty("callNo", callNo);
		json.addProperty("checkKey", checkKey);//接口系统校验码(默认123456)
		json.addProperty("timestamp", timestamp);//格式：yyyymmddhhmmss
		json.addProperty("pageType", pageType);//301.增值退订退费业务
		json.addProperty("randKey", sign);//staffcode|areaCode|checkKey|timestamp|pageType  MD5加密生成
		
		String key = "__AUTH_HANDLE_$#*&@!&%$#@_)#)(_1030|";
		String strEnc = DESEncryptUtil.encrypt(json.toString(), key);
		
		Map intfMap = this.getIntfMap("diagnosis");
        if(null == intfMap){
        	return "";
        }
		String url = intfMap.get("ADDRESS_IP").toString() + "/diag/diagnosis/do_user.do?jsondata=" + strEnc;
		String token = getURLContentGet(url);
		return intfMap.get("ADDRESS_IP").toString() + "/diag/diagnosis/do_authlocal.do?ticket=" + token;
    }
    
	public Map<String,String> getURLContentPostNew(String urlStr,String param,String contentType,int timeOut,String charsetName) {
		long startTime=System.currentTimeMillis();
		HttpURLConnection connection = null;
		Map<String,String> map = new HashMap<>();
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", contentType);
			connection.setConnectTimeout(timeOut);
			connection.setReadTimeout(timeOut);
			connection.connect();
			
			try (
				OutputStream out = connection.getOutputStream();
				){
				out.write(param.getBytes(StandardCharsets.UTF_8));
				out.flush();
			}
			
			StringBuilder sb = new StringBuilder();
			try (
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),charsetName));
				){
				String temp = "";
				while((temp = br.readLine())!=null) {
					sb.append(temp);
				}
			}
			connection.disconnect();
			connection = null;
			
			map.put("code", "0");
			map.put("msg", sb.toString());
			return map;
		} catch (Exception e) {
			map.put("code", "-1");
			map.put("msg", e.toString());
			return map;
		} finally {
			log.info("接口请求地址：{}\n请求内容：{}\n接口返回：{}\n请求耗时：{}",urlStr,param,map.get("msg"),(System.currentTimeMillis()-startTime));
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Exception e) {
					//异常处理逻辑
				}
			}
		}
	}
    
    public String getURLContentGet(String urlStr) {
		HttpURLConnection connection = null;
		StringBuilder sb = new StringBuilder();
		try {
			URL url = new URL(urlStr);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(3000);
			connection.connect();
			
			try (
				BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(),StandardCharsets.UTF_8));
				) {
				String temp = "";
				while((temp = br.readLine())!=null) {
					sb.append(temp);
				}
			}
			connection.disconnect();
			connection = null;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public String getAreaName(int areaId) {
		String qrySql = "select r.region_name from bss_region r where r.region_id = ?";
		List list = jdbcTemplate.query(qrySql, new Object[] { areaId }, new KeyRowMapper());
		if (list.isEmpty()) {
			return "";
		}
		String areaName = "";
		if (null != list.get(0)) {
			areaName = list.get(0).toString();
		}
		return areaName;
	}

	@SuppressWarnings({ "unchecked" })
	public String getTransactionId() {
		String seqNum = this.getSeqVal("SEQ_BSS_ID", 3);
		if(seqNum == null) {
    		return null;
    	}
		String strSql = "SELECT CONCAT('DMT', DATE_FORMAT(NOW(),'%Y%m%d%H%i%s'), 0, ?) transactionId";
		return this.jdbcTemplate.query(strSql, new Object[] { seqNum }, new KeyRowMapper()).get(0).toString();
	}

	@SuppressWarnings({ "unchecked" })
	public String getWXTransactionId() {
		String seqNum = this.getSeqVal("SEQ_BSS_ID", 3);
		if(seqNum == null) {
    		return null;
    	}
		String strSql = "SELECT CONCAT(DATE_FORMAT(NOW(),'%Y%m%d%H%i%s'), '000', ?) transactionId";
		return this.jdbcTemplate.query(strSql, new Object[] { seqNum }, new KeyRowMapper()).get(0).toString();
	}

    /** 
     * 获取当前网络ip 
     * @param request 
     * @return 
     */  
    public String getIpAddr(HttpServletRequest request){  
    	String ipAddress = request.getHeader("x-forwarded-for");
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }  
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }  
        if(ipAddress == null || ipAddress.length() == 0 || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();  
            if(ipAddress.equals("127.0.0.1") || ipAddress.equals("0:0:0:0:0:0:0:1")){
                //根据网卡取本机配置的IP
                try {
                	InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }
        //对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if(ipAddress!=null && ipAddress.length()>15 && ipAddress.indexOf(",")>-1){ //"***.***.***.***".length() = 15
        	ipAddress = ipAddress.substring(0,ipAddress.indexOf(","));
        }
        return ipAddress;
    }

    public int saveOperationLog(String opAction, String orderId, String opMenu, String prodNum, String ip) {
		TsmStaff staff = getLogonStaff();
		String sql = "INSERT INTO cc_operation_log(op_staff,op_logonname,op_staff_name,op_date,op_action,service_order_id,op_menu,op_org_id,op_org_name,op_ip,prod_num) VALUES(?,?,?,now(),?,?,?,?,?,?,?)";
		return jdbcTemplate.update(sql, staff.getId(), staff.getLogonName(), staff.getName(), opAction, orderId, opMenu, staff.getOrganizationId(), staff.getOrgName(), ip, prodNum);
	}
    
    public int saveOperationLogByLn(String opAction, String orderId, String opMenu, String prodNum, String logonName) {
		TsmStaff staff = this.getLogonStaffByLoginName(logonName);
		String sql = "INSERT INTO cc_operation_log(op_staff,op_logonname,op_staff_name,op_date,op_action,service_order_id,op_menu,op_org_id,op_org_name,prod_num) VALUES(?,?,?,now(),?,?,?,?,?,?)";
		return jdbcTemplate.update(sql, staff.getId(), staff.getLogonName(), staff.getName(), opAction, orderId, opMenu, staff.getOrganizationId(), staff.getOrgName(), prodNum);
	}
	
	public int saveLoginLog(HttpServletRequest request, String loginName) {
		String ip = getIpAddr(request);
		TsmStaff staff = getLogonStaffByLoginName(loginName);
		String sql = "INSERT INTO cc_login_log(op_staff,op_logonname,op_staff_name,op_date,op_org_id,op_org_name,op_ip) VALUES(?,?,?,now(),?,?,?)";
		return jdbcTemplate.update(sql, staff.getId(), loginName, staff.getName(), staff.getOrganizationId(), staff.getOrgName(), ip);
	}

	//即时测评工单处理获取Loginname并加密
	public String jscpSSO(){
		TsmStaff tsms = this.getLogonStaff();
		String loginName = tsms.getLogonName();
		String key = SsoUtil.getKey(600, "kfxt.cn", "kfxt", new String[]{loginName});
		return key.substring(5);
	}

	public int judgeSheetOvertime(String workSheetId) {
		int rtn = 0;
		String sql = 
"SELECT DATE_FORMAT(IFNULL(W.SHEET_RECEIVE_DATE,IF(W.SHEET_TYPE IN(700001002,720130015),W.CREAT_DATE,W.LOCK_DATE)),'%Y-%m-%d %H:%i:%s')BEGIN_DATE,"
+ "DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(IF(W.SHEET_STATU IN(700000046,720130035),W.HANGUP_START_TIME,W.RESPOND_DATE),"
+ "'%Y-%m-%d %H:%i:%s')END_DATE,W.HANGUP_TIME_COUNT,A.SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,W.DEAL_LIMIT_TIME FROM "
+ "CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A,CC_SERVICE_LABEL L WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID "
+ "AND W.WORK_SHEET_ID=?";
		List list = jdbcTemplate.queryForList(sql, workSheetId);
		if (!list.isEmpty()) {
			Map map = (Map) list.get(0);
			String beginDate = this.getStringByKey(map, "BEGIN_DATE");
			String orderDate = this.getStringByKey(map, "ACCEPT_DATE");
			String endDate = map.get("END_DATE") == null ? "" : map.get("END_DATE").toString();
			int hangupTimeCount = map.get("HANGUP_TIME_COUNT") == null ? 0 : Integer.parseInt(map.get("HANGUP_TIME_COUNT").toString());
			int serviceType = map.get("SERVICE_TYPE") == null ? 0 : Integer.parseInt(map.get("SERVICE_TYPE").toString());
			String sysDate = map.get("SYS_DATE") == null ? "" : map.get("SYS_DATE").toString();
			int workTime = getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate);
			int dealLimitTime = map.get("DEAL_LIMIT_TIME") == null ? 0 : Integer.parseInt(map.get("DEAL_LIMIT_TIME").toString());
			if (workTime > dealLimitTime * 3600 && dealLimitTime > 0) {
				rtn = 1;
			}
		}
    	return rtn;
    }

	public int judgeOrderOvertime(String serviceOrderId) {
		int rtn = 0;
		String sql = 
"SELECT DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(L.DX_FINISH_DATE,'%Y-%m-%d %H:%i:%s')DX_FINISH_DATE,A.HANGUP_TIME_COUNT,"
+ "IF(A.COME_CATEGORY=707907001,A.SERVICE_TYPE,0)SERVICE_TYPE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,A.ORDER_LIMIT_TIME FROM "
+ "CC_SERVICE_ORDER_ASK A,CC_SERVICE_LABEL L WHERE A.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND A.SERVICE_ORDER_ID=?";
		List list = jdbcTemplate.queryForList(sql, serviceOrderId);
		if (!list.isEmpty()) {
			Map map = (Map) list.get(0);
			String beginDate = this.getStringByKey(map, "ACCEPT_DATE");
			String endDate = map.get("DX_FINISH_DATE") == null ? "" : map.get("DX_FINISH_DATE").toString();
			int hangupTimeCount = map.get("HANGUP_TIME_COUNT") == null ? 0 : Integer.parseInt(map.get("HANGUP_TIME_COUNT").toString());
			int serviceType = map.get("SERVICE_TYPE") == null ? 0 : Integer.parseInt(map.get("SERVICE_TYPE").toString());
			String sysDate = map.get("SYS_DATE") == null ? "" : map.get("SYS_DATE").toString();
			int workTime = getWorkingTime(beginDate, beginDate, endDate, hangupTimeCount * 60, serviceType, sysDate);
			int orderLimitTime = map.get("ORDER_LIMIT_TIME") == null ? 0 : Integer.parseInt(map.get("ORDER_LIMIT_TIME").toString());
			if (workTime > orderLimitTime * 3600 && orderLimitTime > 0) {
				rtn = 1;
			}
		}
		log.info("judgeOrderOvertime orderId: {} result: {}", serviceOrderId, rtn);
    	return rtn;
    }
	
	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}

    //判断当前员工是否是省投诉下面
	public int judgeOrgBelongSTS() {
		TsmStaff staff = getLogonStaff();
		String sql = "SELECT COUNT(1)FROM TSM_ORGANIZATION WHERE CONCAT(LINKID,'-')LIKE'10-11-361143-%'AND ORG_ID=?";
		return this.jdbcTemplate.queryForObject(sql, new Object[] { staff.getOrganizationId() }, Integer.class);
	}

    @SuppressWarnings({ "unchecked" })
	public int checkYDXAuthority(String servOrderId) {
    	TsmStaff staff = getLogonStaff();
    	StringBuilder sb = new StringBuilder();
    	sb.append("SELECT COUNT(*) FROM (SELECT NOW() CREAT_DATE, B.RECEIVE_ORG_ID, C.REGION_ID RECEIVE_REGION_ID, '" + staff.getOrganizationId()
        + "' RETURN_ORG_ID, '" + staff.getOrgName() + "' RETURN_ORG_NAME, " + staff.getId() + " RETURN_STAFF, '" + staff.getName() + "' RETURN_STAFF_NAME, "
        + "720130031 SHEET_STATU, '待处理' SHEET_STATU_DESC, 720130024 TACHE_ID, '部门预定性' TACHE_DESC, B.SERVICE_ORDER_ID, B.REGION_ID, 0 LOCK_FLAG "
        + "FROM (SELECT A.SERVICE_ORDER_ID, A.REGION_ID, A.RECEIVE_ORG_ID FROM  "
        + "CC_WORK_SHEET A WHERE A.SERVICE_ORDER_ID = ? AND A.SHEET_TYPE = 720130013 AND A.MAIN_SHEET_FLAG = 1 "
        + "ORDER BY A.CREAT_DATE DESC LIMIT 1) B, TSM_ORGANIZATION C WHERE B.RECEIVE_ORG_ID = C.ORG_ID) ");
        String s2 = "SELECT * FROM CC_WORK_SHEET, CC_SERVICE_ORDER_ASK A, CC_SERVICE_CONTENT_ASK D, CC_ORDER_CUST_INFO C WHERE CC_WORK_SHEET.REGION_ID = A.REGION_ID "
                + "AND A.SERVICE_DATE = 3 AND CC_WORK_SHEET.LOCK_FLAG = 0 AND CC_WORK_SHEET.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID "
                + "AND A.SERVICE_ORDER_ID = D.SERVICE_ORDER_ID AND A.CUST_GUID = C.CUST_GUID";
        Map tableMap = new HashMap();
        tableMap.put("CC_WORK_SHEET", "CC_WORK_SHEET");
        tableMap.put("CC_SERVICE_ORDER_ASK", "A");
        tableMap.put("CC_SERVICE_CONTENT_ASK", "D");
        tableMap.put("CC_ORDER_CUST_INFO", "C");
        String s3 = this.systemAuthorization.getAuthedSql(tableMap, s2, "900018300");
        s3 = s3.replace("SELECT * FROM", " ");
        sb.append(s3);
        log.info("checkYDXAuthority sql: {}", sb.toString());
        return this.jdbcTemplate.queryForObject(sb.toString(), new Object[] { servOrderId }, Integer.class);//CodeSec未验证的SQL注入；CodeSec误报：2
    }

    /**
     * 根据格式得到当前时间字符串
     * @return
     */
    @SuppressWarnings({ "unchecked" })
	public String getSysDateFormat(String format){
        String strSql = "SELECT DATE_FORMAT(NOW(),?) FROM DUAL";
		List tmpList = this.jdbcTemplate.query(strSql, new Object[]{format}, new KeyRowMapper());
        String sysDate = tmpList.get(0).toString();
        tmpList.clear();
        tmpList = null; 
        return sysDate;
    }

	/**
	 * 获取时间差
	 * 
	 * @return秒
	 */
	public int getBetweenSysDateSec(String startDate) {
		String strSql = "SELECT FLOOR(TIMESTAMPDIFF(SECOND,str_to_date(?, '%Y-%m-%d %H:%i:%s'),NOW())) FROM dual";
		return this.jdbcTemplate.queryForObject(strSql, new Object[] { startDate },Integer.class);
	}

    public List loadColumnsByEntity(String tableCode, String colCode, String entityId) {
    	List result = new ArrayList();
    	if(tableCode == null || tableCode.trim().equals("")){
    		return result;
    	}
    	if(colCode == null || colCode.trim().equals("")){
    		return result;
    	}
    	String strsql = "SELECT REFER_ID,COL_VALUE_NAME,ifnull(COL_NAME,' ') as COL_NAME,ifnull(COL_VALUE,' ') as COL_VALUE FROM PUB_COLUMN_REFERENCE P WHERE 1=1 " +
    			"AND P.TABLE_CODE = ? " +
    			"AND P.COL_CODE = ? " ;
    	List<String> argList = new ArrayList<>();
        argList.add(tableCode);
        argList.add(colCode);
    	if(StringUtils.isNotEmpty(entityId)){
    		strsql += "AND P.ENTITY_ID = ?";
    		argList.add(entityId);
    	}
    	strsql += " ORDER BY P.COL_ORDER";
		result = jdbcTemplate.queryForList(strsql, argList.toArray());
		return result;
	}

	public String getAreaTmDaily() {
		String sql = "SELECT AREA_ID,AREA_NAME,SUM_STD_AREA_ID FROM AREA_TM_DAILY WHERE CITY_ID>='A' AND LENGTH(SUM_STD_AREA_ID)>0 ORDER BY CITY_ID,AREA_ID";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(sql);
		if (!list.isEmpty()) {
			JSONArray array = new JSONArray();
			for (Map<String, Object> map : list) {
				if ("801".equals(map.get("SUM_STD_AREA_ID").toString())) {
					String areaId = map.get("AREA_ID").toString();
					JSONObject item = new JSONObject();
					item.put("value", areaId);
					item.put("label", map.get("AREA_NAME").toString());
					JSONArray children = getAreaTmDaily(list, areaId);
					if (!children.isEmpty()) {
						item.put("children", children);
					}
					array.add(item);
					item.clear();
				}
			}
			JSONObject json = new JSONObject();
			json.put("array", array);
			return json.toString();
		}
		return null;
	}

	private JSONArray getAreaTmDaily(List<Map<String, Object>> list, String parentId) {
		JSONArray newArr = new JSONArray();
		for (Map<String, Object> map : list) {
			if (parentId.equals(map.get("SUM_STD_AREA_ID").toString())) {
				JSONObject item = new JSONObject();
				item.put("value", map.get("AREA_ID").toString());
				item.put("label", map.get("AREA_NAME").toString());
				newArr.add(item);
			}
		}
		return newArr;
	}

	/**
	 * 根据regionId查询区县
	 * */
	public List<Map<String, Object>> getAreaTmDailyByRegionId(String regionId){
		String stdAreaId = this.buildStdAreaId(regionId);
		if(StringUtils.isBlank(stdAreaId)){
			return Collections.emptyList();
		}
		String sql = "select AREA_ID as value,AREA_NAME as label,SUM_STD_AREA_ID from AREA_TM_DAILY WHERE CITY_ID>='A' AND LENGTH(SUM_STD_AREA_ID)>0 and sum_std_area_id = ?";
		List<Map<String, Object>> list = new ArrayList<>();
		try{
			list = jdbcTemplate.queryForList(sql,stdAreaId);
		}catch (Exception e){
			log.error("getAreaTmDailyByRegionId error: {}",e.getMessage(),e);
		}
		return list;
	}

	/**
	 * 根据regionId转换对应区县所属值
	 * */
	private String buildStdAreaId(String regionId) {
		Map<String, String> regionCodeMap = new HashMap<>();
		regionCodeMap.put("3", "250000001");
		regionCodeMap.put("4", "110000001");
		regionCodeMap.put("15", "100000001");
		regionCodeMap.put("20", "120000001");
		regionCodeMap.put("26", "130000001");
		regionCodeMap.put("33", "140000001");
		regionCodeMap.put("39", "150000001");
		regionCodeMap.put("48", "160000001");
		regionCodeMap.put("60", "170000001");
		regionCodeMap.put("63", "180000001");
		regionCodeMap.put("69", "190000000");
		regionCodeMap.put("79", "230000001");
		regionCodeMap.put("84", "270000001");
		return regionCodeMap.get(regionId);
	}


    public List loadQdxx(String referId) {
    	List result = new ArrayList();
    	String strsql = "SELECT d.REFER_ID,d.COL_VALUE_NAME" + 
    			"  FROM pub_column_reference a," + 
    			"       pub_column_reference b," + 
    			"       pub_column_reference c," + 
    			"       pub_column_reference d" + 
    			" WHERE a.refer_id = b.entity_id AND b.refer_id = c.entity_id AND c.refer_id = d.entity_id AND c.refer_id =?";
    	if(referId!=null) {
    		result = jdbcTemplate.queryForList(strsql, referId);
    	}
		return result;
	}
	

    public String getJsonTree(String id) {
        List s1 = getDir("CC_SERVICE_CONTENT_ASK", id, null);
        StringBuffer str = new StringBuffer("[");
        for (int i = 0; i < s1.size(); i++) {
            Map t = (Map) s1.get(i);
            String p = t.get("REFER_ID").toString();
            str.append("{");
            str.append("'id':'" + p + "',");
            str.append("'text':'" + t.get("COL_VALUE_NAME").toString() + "',");
            str.append("'value':'" + p + "',");
            str.append("'showcheck':true ,");
            str.append("'checkstate':0 ,");
            str.append("'complete':true ,");
            str.append("'hasChildren':true ,");
            str.append("'ChildNodes': [");
            List s2 = getDir("CC_SERVICE_CONTENT_ASK", p, null);
            for (int j = 0; j < s2.size(); j++) {
                Map t1 = (Map) s2.get(j);
                String p1 = t1.get("REFER_ID").toString();
                str.append("{");
                str.append("'id':'" + p1 + "',");
                str.append("'text':'" + t1.get("COL_VALUE_NAME").toString() + "',");
                str.append("'value':'" + p1 + "',");
                str.append("'showcheck':true ,");
                str.append("'checkstate':0 ,");
                str.append("'complete':true ,");
                str.append("'hasChildren':true ,");
                str.append("'ChildNodes': [");

                List s3 = getDir("CC_SERVICE_CONTENT_ASK", p1, null);
                for (int k = 0; k < s3.size(); k++) {
                    Map t2 = (Map) s3.get(k);
                    String p2 = t2.get("REFER_ID").toString();
                    str.append("{");
                    str.append("'id':'" + p2 + "',");
                    str.append("'text':'" + t2.get("COL_VALUE_NAME").toString() + "',");
                    str.append("'value':'" + p2 + "',");
                    str.append("'showcheck':true ,");
                    str.append("'checkstate':0 ,");
                    str.append("'complete':true ,");
                    str.append("'hasChildren':false");
                    str.append(k != s3.size() - 1 ? "}," : "}");
                }
                str.append("]");
                str.append(j != s2.size() - 1 ? "}," : "}");
            }
            str.append("]");

            str.append(i != s1.size() - 1 ? "}," : "}");
        }
        str.append("]");
        return str.toString();
    }
    
	public String getLinkIdByLoginName(String loginName){
    	TsmStaff staff = this.getLogonStaffByLoginName(loginName);
        if(staff == null){
        	return "10";
        }
    	return staff.getLinkId();
    }
    
    @SuppressWarnings({ "unchecked" })
	public List getThreeDir(String entityId, boolean flag) {

        String table = "CC_SHEET_QUALITATIVE";
        if (flag) {
            table = "CC_SERVICE_CONTENT_ASK";
        }
        List x = getDir(table, entityId, null);
        List threeDir = new ArrayList();
        for (int i = 0; i < x.size(); i++) {
            Map t = (Map) x.get(i);
            DirectoryDataInfo b = new DirectoryDataInfo();
            String refId = t.get("REFER_ID").toString();
            b.setId(refId);
            b.setColValue(t.get("COL_VALUE_NAME").toString());
            b.setList(getFourDir(refId, table));
            threeDir.add(b);
        }
        x = null;
        return threeDir;
    }

    @SuppressWarnings({ "unchecked" })
	public List getFourDir(String entityId, String table) {
        List x = getDir(table, entityId, null);
        List fouDir = new ArrayList();
        for (int i = 0; i < x.size(); i++) {
            Map t = (Map) x.get(i);
            DirectoryDataInfo b = new DirectoryDataInfo();
            String refId = t.get("REFER_ID").toString();
            b.setId(refId);
            b.setColValue(t.get("COL_VALUE_NAME").toString());
            b.setList(getFiveDir(refId, table));
            fouDir.add(b);
        }
        x = null;
        return fouDir;
    }

    @SuppressWarnings({ "unchecked" })
	public List getFiveDir(String entityId, String table) {
        List x = getDir(table, entityId, null);
        List fiveDir = new ArrayList();
        for (int i = 0; i < x.size(); i++) {
            Map t = (Map) x.get(i);
            DirectoryDataInfo b = new DirectoryDataInfo();
            String refId = t.get("REFER_ID").toString();
            b.setId(refId);
            b.setColValue(t.get("COL_VALUE_NAME").toString());
            b.setList(getSixDir(refId, table));
            String w = t.get("COL_VALUE") == null ? "" : t.get("COL_VALUE").toString();
            String k = t.get("COL_VALUE_HANDLING") == null ? "" : t.get("COL_VALUE_HANDLING")
                    .toString();
            b.setVal(w);
            b.setHandle(k);
            fiveDir.add(b);
        }
        return fiveDir;
    }
    
    @SuppressWarnings({ "unchecked" })
    public List getSixDir(String entityId, String table) {
        List x = getDir(table, entityId, null);
        List sixDir = new ArrayList();
        for (int i = 0; i < x.size(); i++) {
            Map t = (Map) x.get(i);
            DirectoryDataInfo b = new DirectoryDataInfo();
            String refId = t.get("REFER_ID").toString();
            b.setId(refId);
            b.setColValue(t.get("COL_VALUE_NAME").toString());
            sixDir.add(b);
        }
        return sixDir;
    }

    /**
     * 根据TABLE_CODE、REFER_ID、ENTITY_ID查询PUB_COLUMN_REFERENCE表，<br>
     * 获取COL_VALUE,COL_VALUE_HANDLING, REFER_ID,COL_VALUE_NAME,ENTITY_ID五个字段的信息。
     * @param tableName TABLE_CODE，必填参数
     * @param entityId REFER_ID，可选参数，可为null
     * @param refid ENTITY_ID，可选参数，可为null
     * @return 
     */
    public List getDir(String tableName, String entityId, String refid) {
        String strsql = "SELECT COL_VALUE,COL_VALUE_HANDLING,REFER_ID,COL_VALUE_NAME,ENTITY_ID FROM PUB_COLUMN_REFERENCE "
                + "WHERE TABLE_CODE = ?";
        List<String> argList = new ArrayList<>();
        argList.add(tableName);
        if (StringUtils.isNotEmpty(refid)) {
            strsql += " AND REFER_ID = ?";
            argList.add(refid);
        }
        if (StringUtils.isNotEmpty(entityId)) {
            strsql += " AND ENTITY_ID = ?";
            argList.add(entityId);
        }
        Object[] args = argList.toArray();
        return jdbcTemplate.queryForList(strsql, args);
    }

    // 生成guid
    @SuppressWarnings("unchecked")
	public String crtGuid() {
        String strsql = "SELECT replace(UUID(), '-', '') AS GUID";
        return this.jdbcTemplate.query(strsql, new KeyRowMapper()).get(0).toString();
    }

    /**
     * 工单流向的ID
     * 
     * @return
     */
    public int schemaId() {
        String strSql = " SELECT SEQ_WORKSHEET_SCHEMA.NEXTVAL AS ID FROM DUAL";
        return this.jdbcTemplate.queryForObject(strSql,Integer.class);
    }

	// 判断新流程2020-4
	public boolean isNewWorkFlow(String orderId) {
		if (orderId.substring(2, 3).equals("3")) {
			return true;
		} else {
			return false;
		}
	}

	public String getAreaOrgId(String orgId) {
		String sql = 
"SELECT B.ORG_ID FROM TSM_ORGANIZATION A,TSM_ORGANIZATION B WHERE IF(SUBSTRING_INDEX(A.LINKID,'-',2)='10-11',SUBSTRING_INDEX(A.LINKID,'-',3),"
+ "SUBSTRING_INDEX(A.LINKID,'-',2))=B.LINKID AND A.ORG_ID=?";
		List tmp = jdbcTemplate.queryForList(sql, orgId);
		if (tmp.isEmpty()) {
			return "10";
		}
		return ((Map) tmp.get(0)).get("ORG_ID").toString();
	}

	public String getAreaOrgIdByStaff(String staffId) {
		String sql =
"SELECT B.ORG_ID FROM TSM_ORGANIZATION A,TSM_ORGANIZATION B,TSM_STAFF C WHERE IF(SUBSTRING_INDEX(A.LINKID,'-',2)='10-11',SUBSTRING_INDEX(A.LINKID,'-',"
+ "3),SUBSTRING_INDEX(A.LINKID,'-',2))=B.LINKID AND A.ORG_ID=C.ORG_ID AND C.STAFF_ID=?";
		List tmp = jdbcTemplate.queryForList(sql, staffId);
		if (tmp.isEmpty()) {
			return "10";
		}
		return ((Map) tmp.get(0)).get("ORG_ID").toString();
	}

	// 2020-4判断集团编码
	public int checkUnified(String serviceOrderId) {
		String sql = "SELECT COUNT(1) FROM cc_cmp_unified_return WHERE complaint_worksheet_id = ?";
		return jdbcTemplate.queryForObject(sql, new Object[] {serviceOrderId}, Integer.class);
	}
	
	// 判断有集团编码投诉单
	public int checkTsUnified(String serviceOrderId) {
		String sql = "SELECT COUNT(1) FROM cc_service_order_ask s, cc_cmp_unified_return c WHERE s.SERVICE_ORDER_ID = c.complaint_worksheet_id and s.SERVICE_TYPE=720130000 and s.SERVICE_ORDER_ID=?";
		int num = jdbcTemplate.queryForObject(sql, new Object[] {serviceOrderId}, Integer.class);
		if (num > 0) {// 有集团编码
			if (judgeOrgBelongSTS() > 0) {// 属于服务管理部员工可以挂断评价
				return 0;
			} else {// 不可以挂断评价
				return 1;
			}
		} else {// 没有集团编码可以挂断评价
			return 0;
		}
	}

	// 2020-4环节时限 单位分钟
	public int checkPDTime(String workSheetId) {
		int locallefttime = 0;
		String sql = 
"SELECT(SELECT DATE_FORMAT(SA.CREAT_DATE,'%Y-%m-%d %H:%i:%s')FROM CC_WORK_SHEET_AREA SA WHERE SA.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND "
+ "SA.TACHE_DATE IS NULL AND SA.AREA_FLAG=IF(W.TACHE_ID IN(720130021,700000085),3,IF(W.TACHE_ID IN(720130023,700000086),1,0))ORDER BY SA.CREAT_DATE "
+ "DESC LIMIT 1)PD_BEGIN_DATE,DATE_FORMAT(A.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s')ACCEPT_DATE,DATE_FORMAT(NOW(),'%Y-%m-%d %H:%i:%s')SYS_DATE,A.SERVICE_TYPE,"
+ "IFNULL((SELECT PD.PD_MINUTES FROM CC_SHEET_LIMITTIME_PD PD WHERE PD.TACHE_ID=W.TACHE_ID AND PD.COME_CATEGORY=A.COME_CATEGORY AND PD.IS_UNIFIED="
+ "IFNULL(L.IS_UNIFIED,0)),0)PDLIMIT FROM CC_WORK_SHEET W,CC_SERVICE_ORDER_ASK A,CC_SERVICE_LABEL L WHERE W.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID AND "
+ "W.SERVICE_ORDER_ID=L.SERVICE_ORDER_ID AND W.WORK_SHEET_ID=?";
		List list = jdbcTemplate.queryForList(sql, workSheetId);
		if (!list.isEmpty()) {
			Map map = (Map) list.get(0);
			String pdBeginDate = this.getStringByKey(map, "PD_BEGIN_DATE");
			int pdlimit = map.get("PDLIMIT") == null ? 0 : Integer.parseInt(map.get("PDLIMIT").toString());
			String orderDate = this.getStringByKey(map, "ACCEPT_DATE");
			int serviceType = map.get("SERVICE_TYPE") == null ? 0 : Integer.parseInt(map.get("SERVICE_TYPE").toString());
			String sysDate = this.getStringByKey(map, "SYS_DATE");
			if (!StringUtils.isEmpty(pdBeginDate) && 0 != pdlimit) {
				int pdTime = getWorkingTime(pdBeginDate, orderDate, sysDate, 0, serviceType, sysDate);
				if (pdlimit * 60 > pdTime) {
					locallefttime = (pdlimit * 60 - pdTime) / 60;
				}
			}
		}
		return locallefttime;
	}

	// 2020-4判断是否追回
	public int checkRecall(String serviceOrderId) {
		String sql = "SELECT COUNT(1) FROM cc_work_sheet a WHERE a.service_order_id = ? AND tache_id = 700000086";
		return jdbcTemplate.queryForObject(sql, new Object[] {serviceOrderId}, Integer.class);
	}
	
	public String getUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}
	
	public int setSeqVal(String seqName, String seqId) {
		String sql = "INSERT INTO " + seqName + "(seq_id, create_date) VALUES (?, current_timestamp(3))";
		return this.jdbcTemplate.update(sql, seqId);
	}
	
	@SuppressWarnings({ "unchecked" })
	public String getSeqNum(String seqName, String seqId, int len) {
		String sql = "select lpad(right(seq_num," + len + "), " + len + ", 0) from " + seqName + " where seq_id = ?";
		List tmpList = this.jdbcTemplate.query(sql, new Object[] {seqId}, new KeyRowMapper());
		if(!tmpList.isEmpty()) {
			return tmpList.get(0).toString();
		}
        return null;
	}
	
	/**
	 * 获取指定长度的自增序列
	 */
	public String getSeqVal(String seqName, int len) {
		String seqId = this.getUUID();
		int ct = this.setSeqVal(seqName, seqId);
		if(ct > 0) {
			return this.getSeqNum(seqName, seqId, len);
		}
		return null;
	}

	// 生成定单号
    @SuppressWarnings("unchecked")
	public String crtOrderId(int servType, int regionId) {
    	String seqNum = this.getSeqVal("SEQ_SERVICE_ORDER_ID", 6);
    	if(seqNum == null) {
    		return null;
    	}
        String strsql = "SELECT CONCAT(B.COL_VALUE, if(B.COL_VALUE_HANDLING='TS', '3', ''), ifnull(A.REGION_TELNO,''), DATE_FORMAT(NOW(),'%y%m%d'), ?)  AS ORDER_ID "
        		+ "FROM TRM_REGION A, PUB_COLUMN_REFERENCE B WHERE A.REGION_ID = ?  AND B.REFER_ID = ?";
        return this.jdbcTemplate.query(strsql, new Object[] {seqNum, regionId, servType},new KeyRowMapper()).get(0).toString();
    }
    
    // 生成工单号
    @SuppressWarnings("unchecked")
	public String crtSheetId(int regionId) {
    	String seqNum = this.getSeqVal("SEQ_WORK_SHEET_ID", 6);
    	if(seqNum == null) {
    		return null;
    	}
        String strsql = "SELECT CONCAT(ifnull(A.REGION_TELNO,''),DATE_FORMAT(NOW(),'%y%m%d'), ?) AS SHEET_ID FROM TRM_REGION A WHERE A.REGION_ID = ?";
        return this.jdbcTemplate.query(strsql, new Object[] {seqNum, regionId}, new KeyRowMapper()).get(0).toString();
    }

    private static final  String QUERY_ACCEPT_DIR = "SELECT A.N, A.N_ID, A.ID FROM CCS_ST_MAPPING_ALL_NEW A WHERE 1 = 1 ";
	private static final  String QUERY_ACCEPT_DIR_YS = "SELECT a.refer_id a1, b.refer_id a2 FROM pub_column_reference a, pub_column_reference b"
			+ " WHERE a.refer_id = b.entity_id AND a.table_code = 'CC_SERVICE_CONTENT_ASK' AND a.col_code = 'APPEAL_PROD_ID_YS' AND a.entity_id = ?"
			+ " AND b.table_code = 'CC_SERVICE_CONTENT_ASK' AND b.col_code = 'APPEAL_REASON_ID_YS' AND b.col_value_name = ?";

    public List queryAcceptDir(int nId, int vNum) {
    	String sql = QUERY_ACCEPT_DIR + " AND A.N_ID = ?";
    	List<Integer> argList = new ArrayList<>();
    	argList.add(nId);
    	if (vNum > 0) {
    		sql += " AND a.v_num = ?";
    		argList.add(vNum);
    	}
		return jdbcTemplate.queryForList(sql, argList.toArray());
    }

	public List queryAcceptDirYs(String entityId, String colValueName) {
		return jdbcTemplate.queryForList(QUERY_ACCEPT_DIR_YS, entityId, colValueName);
	}

	public String getSixCatalogMap(String sixCatalog, int recordType) {
		String qrySql = "select M.N_ID from CC_SIXGRADE_MAP M where M.SIXGRADE_ID = ? AND record_type = ?";
		List catalogList = jdbcTemplate.queryForList(qrySql, sixCatalog, recordType);
		if (catalogList.isEmpty()) {
			return null;
		}
		Map catalogMap = (Map) catalogList.get(0);
		return catalogMap.get("N_ID").toString();
	}

	public Map getCmpDataMapDstCode(String srcCode, String flag, String featureCode) {
		String sql = "SELECT DST_CODE, DST_NAME FROM CC_CMP_DATA_MAP M WHERE M.SRC_CODE = ? AND M.FLAG = ? AND M.FEATURE_CODE = ?";
		List list = jdbcTemplate.queryForList(sql, srcCode, flag, featureCode);
		if (list.isEmpty()) {
			return Collections.emptyMap();
		}
		return (Map) list.get(0);
	}

    /**
     * 查找受理目录自动补齐
     * @return
     */
    public ServiceAcceptDir[] findAcceptDir(String queryChar) {
        if (queryChar == null || "".equals(queryChar)) {
        	return new ServiceAcceptDir[0];
        }
        queryChar = queryChar.trim();
        while (queryChar.indexOf("  ") > -1) {
            queryChar = queryChar.replace("  ", " ");
        }
        char[] letters = queryChar.toCharArray();
        int len = letters.length;
        char letter = ' ';
        for(int i = 0; i < len; i ++){
            letter = letters[i];
            if(letter >= 'a' && letter <= 'z'){
                letters[i] = (char)(letter - 32);
            }
        }
        String[] keys = new String(letters).split(" ");
        StringBuilder whereStr = new StringBuilder("AND a.v_num = 2 ");
        List<String> argList = new ArrayList<>();
        for(int i = keys.length-1; i>=0; i--){
            whereStr.append("AND A.F_N like concat('%',?,'%') ");
            argList.add(keys[i]);
        }
        String sql = QUERY_ACCEPT_DIR + whereStr.toString();
        List tmpList = jdbcTemplate.queryForList(sql, argList.toArray());

        int size = tmpList.size();
        // 如果没有查询结果返回空
        if (size == 0) {
            tmpList.clear();
            tmpList = null;
            return new ServiceAcceptDir[0];
        }
        ServiceAcceptDir[] acceptDirList = new ServiceAcceptDir[size];
        for (int i = 0; i < size; i++) {
            Map tmpMap = (Map) tmpList.get(i);
            String dirId = tmpMap.get("ID").toString();
            String dirDesc = tmpMap.get("N").toString();
            ServiceAcceptDir acceptDir = new ServiceAcceptDir();
            acceptDir.setDirId(dirId);
            acceptDir.setValue(dirDesc);
            acceptDirList[i] = acceptDir;
        }
        return acceptDirList;
    }

    private static final String QUERY_BANJIE_DIR = "SELECT A.N, A.N_ID, A.ID, A.IS_CHANNEL FROM CCS_ST_MAPPING_ALL_B_NEW A WHERE 1 = 1 ";

    /**
     * 根据办结原因六级ID查找办结原因
     */
	public List getBanjieDir(String refid, int vNum) {
		String strsql = QUERY_BANJIE_DIR + "AND A.N_ID=?";
		List<Object> argList = new ArrayList<>();
		argList.add(refid);
		if (vNum > 0) {
			strsql += " AND a.v_num = ?";
			argList.add(vNum);
		}
		return jdbcTemplate.queryForList(strsql, argList.toArray());
	}

    /**
     * 查找办结目录自动补齐
     * @return
     */
    public ServiceAcceptDir[] findBanjieDir(String queryChar) {
        if (queryChar == null || "".equals(queryChar)) {
            return new ServiceAcceptDir[0];
        }
        queryChar = queryChar.trim();
        queryChar = queryChar.replace("-", " ");
        while (queryChar.indexOf("  ") > -1) {
            queryChar = queryChar.replace("  ", " ");
        }
        char[] letters = queryChar.toCharArray();
        int len = letters.length;
        char letter = ' ';
        for(int i = 0; i < len; i ++){
            letter = letters[i];
            if(letter >= 'a' && letter <= 'z'){
                letters[i] = (char)(letter - 32);
            }
        }
        String[] keys = new String(letters).split(" ");
        StringBuilder whereStr = new StringBuilder("AND a.v_num = 2 ");
        List<String> argList = new ArrayList<>(); // 动态参数列表
        for(int i = keys.length-1; i>=0; i--){
			whereStr.append("AND A.F_N like concat('%',?,'%') ");
			argList.add(keys[i]);
        }
        String sql = QUERY_BANJIE_DIR + whereStr.toString();
		List tmpList = jdbcTemplate.queryForList(sql, argList.toArray());

		int size = tmpList.size();
        // 如果没有查询结果返回空
        if (size == 0) {
            tmpList.clear();
            tmpList = null;
            return new ServiceAcceptDir[0];
        }
        ServiceAcceptDir[] acceptDirList = new ServiceAcceptDir[size];
        for (int i = 0; i < size; i++) {
            Map tmpMap = (Map) tmpList.get(i);
            String dirId = tmpMap.get("ID").toString();
            String dirDesc = tmpMap.get("N").toString();
            ServiceAcceptDir acceptDir = new ServiceAcceptDir();
            acceptDir.setDirId(dirId);
            acceptDir.setValue(dirDesc);
            acceptDirList[i] = acceptDir;
        }
        return acceptDirList;
    }

    /**
     * 得到当前的月
     * 
     * @return
     */
    @SuppressWarnings({ "unchecked" })
	public Integer getIntMonth() {
        String strSql = "select CONVERT(DATE_FORMAT(NOW(),'%m'), SIGNED)";
        List tmpList = this.jdbcTemplate.query(strSql, new Object[] {}, new KeyRowMapper());
        String strMonth = tmpList.get(0).toString();
        return Integer.parseInt(strMonth);
    }

    /**
     * 查询当前登录员工信息
     * 
     * @return
     */
    public TsmStaff getLogonStaff() {
    	String loginName = JWTUtils.getLoginNameByHeadToken();
    	return this.getLogonStaffByLoginName(loginName);
    }
    
    /**
     * 获取当前是否登录
     */
    public boolean isLogonFlag() {
    	return (JWTUtils.getToken() != null);
    }
    
    /**
     * 查询当前登录员工话务信息
     * 
     * @return
     */
	public List getCallStaffInfo() {
		String querySql = "SELECT A.STAFF_ID, A.STAFFNAME, A.AGENT_ID, A.AGENTPWD, A.LOCALDN, C.REGION_TELNO FROM TSM_STAFF A, TSM_ORGANIZATION B, TRM_REGION C " +
				"WHERE A.ORG_ID = B.ORG_ID AND A.STAFF_ID = ? AND C.REGION_ID = B.REGION_ID";
		return this.jdbcTemplate.queryForList(querySql, getLogonStaff().getId());
    }
    
	/**
	 * 查询当前用户名员工信息
	 * 
	 * @return
	 */
	public TsmStaff getLogonStaffByLoginName(String loginName) {
		TsmStaff staff = null;
    	try {
			String staffJson = this.redisUtils.get("TsmStaff__loginName_"+loginName,RedisType.WORKSHEET);
			if(staffJson != null) {
				staff = JSON.parseObject(staffJson, TsmStaff.class);	
			}
    	}
    	catch(Exception e) {
    		log.error("Redis: getTsmStaff Exception：{}", e.getMessage());
    	}
    	if(staff == null) {
    		try {
    			staff = new TsmStaff();
        		String strSql = "SELECT A.STAFF_ID, A.STAFFNAME, A.ORG_ID, B.ORG_NAME, A.GENDER, A.RELAPHONE, A.LOGONNAME, A.PASSWORD, B.LINKID FROM TSM_STAFF A, TSM_ORGANIZATION B WHERE A.ORG_ID = B.ORG_ID AND A.LOGONNAME = ?";
        		List list = jdbcTemplate.queryForList(strSql, loginName);
        		if(list.isEmpty()) {
        			return null;
        		}
        		Map map = (Map) list.get(0);
        		staff.setId(map.get("STAFF_ID").toString());
        		staff.setName(map.get("STAFFNAME").toString());
        		staff.setOrganizationId(map.get("ORG_ID").toString());
        		staff.setOrgName(map.get("ORG_NAME").toString());
        		staff.setLinkId(this.getStringByKey(map, "LINKID"));
        		staff.setRelaPhone(this.getStringByKey(map, "RELAPHONE"));
        		staff.setLogonName(this.getStringByKey(map, "LOGONNAME"));
        		staff.setPassword(this.getStringByKey(map, "PASSWORD"));
        		staff.setGender(map.get("GENDER") == null ? 0 : Integer.parseInt(map.get("GENDER").toString()));

    			this.redisUtils.setex("TsmStaff__loginName_"+loginName,3600*4,JSON.toJSONString(staff),RedisType.WORKSHEET);
    		}
    		catch(Exception e) {
    			log.error("getLogonStaffByLoginName Exception：{}", e.getMessage());
        	}
		}
    	return staff;
	}

	public TsmStaff getAvlStaffByLoginName(String loginName) {
		TsmStaff tsmStaff = new TsmStaff();
		String strSql = "SELECT A.GENDER, A.STAFF_ID, A.STAFFNAME, A.ORG_ID, B.ORG_NAME, A.RELAPHONE, A.LOGONNAME, B.LINKID, A.PASSWORD FROM TSM_STAFF A, TSM_ORGANIZATION B WHERE A.ORG_ID = B.ORG_ID AND A.LOGONNAME = ? AND A.STATE = 8";
		List list = jdbcTemplate.queryForList(strSql, loginName);
		if(list.isEmpty()) return null;
		Map map = (Map) list.get(0);
		tsmStaff.setId(map.get("STAFF_ID").toString());
		tsmStaff.setName(map.get("STAFFNAME").toString());
		tsmStaff.setOrganizationId(map.get("ORG_ID").toString());
		tsmStaff.setOrgName(map.get("ORG_NAME").toString());
		tsmStaff.setLinkId(map.get("LINKID").toString());
		tsmStaff.setRelaPhone(map.get("RELAPHONE") == null ? "" : map.get("RELAPHONE").toString());
		tsmStaff.setLogonName(map.get("LOGONNAME") == null ? "" : map.get("LOGONNAME").toString());
		tsmStaff.setPassword(map.get("PASSWORD") == null ? "" : map.get("PASSWORD").toString());
		tsmStaff.setGender(map.get("GENDER") == null ? 0 : Integer.parseInt(map.get("GENDER").toString()));
		return tsmStaff;
	}
	
	/**
	 * 根据staffId查询员工信息
	 * 
	 * @return
	 */
	public TsmStaff getStaff(int staffId) {
		TsmStaff staff = null;
    	try {
			String staffJson = this.redisUtils.get("TsmStaff__staffId_"+staffId,RedisType.WORKSHEET);
			if(staffJson != null) {
				staff = JSON.parseObject(staffJson, TsmStaff.class);	
			}
    	}
    	catch(Exception e) {
    		log.error("Redis: getTsmStaff Exception：{}", e.getMessage());
    	}
    	if(staff == null) {
    		try {
    			staff = new TsmStaff();
        		String strSql = "SELECT A.STAFF_ID, A.STAFFNAME, A.ORG_ID, B.ORG_NAME, A.GENDER, A.RELAPHONE, A.LOGONNAME, A.PASSWORD, B.LINKID FROM TSM_STAFF A, TSM_ORGANIZATION B WHERE A.ORG_ID = B.ORG_ID AND A.STAFF_ID = ?";
        		List list = jdbcTemplate.queryForList(strSql, staffId);
        		if(list.isEmpty()) return null;
        		Map map = (Map) list.get(0);
        		staff.setId(map.get("STAFF_ID").toString());
        		staff.setName(map.get("STAFFNAME").toString());
        		staff.setOrganizationId(map.get("ORG_ID").toString());
        		staff.setOrgName(map.get("ORG_NAME").toString());
        		staff.setLinkId(map.get("LINKID").toString());
        		staff.setRelaPhone(map.get("RELAPHONE") == null ? "" : map.get("RELAPHONE").toString());
        		staff.setLogonName(map.get("LOGONNAME") == null ? "" : map.get("LOGONNAME").toString());
        		staff.setPassword(map.get("PASSWORD") == null ? "" : map.get("PASSWORD").toString());
        		staff.setGender(map.get("GENDER") == null ? 0 : Integer.parseInt(map.get("GENDER").toString()));

    			this.redisUtils.setex("TsmStaff__staffId_"+staffId,3600*4,JSON.toJSONString(staff),RedisType.WORKSHEET);
    		}
    		catch(Exception e) {
    			log.error("getTsmStaff Exception：{}", e.getMessage());
        	}
		}
    	return staff;
	}

    /**
     * 查询员工姓名
     * 
     * @param staffId员工id
     * @return 员工名
     */
    public String getStaffName(int staffId) {
		TsmStaff staff = this.getStaff(staffId);
        if (staff == null) {
            log.warn("没有查询到staffId为{}的员工!", staffId);
            return "";
        }
        return staff.getName();
    }

    /**
     * 根据员工ID得到员工登录工号
     * 
     * @param staffId
     *            员工id
     * @return 员工名
     */
    public String getStaffLongName(int staffId) {
		TsmStaff staff = this.getStaff(staffId);
        if (staff == null) {
            log.warn("没有查询到staffId为{}的员工!", staffId);
            return "";
        }
        return staff.getLogonName();
    }

    /**
     * 根据员工ID得到员工所在部门
     * 
     * @param staffId
     *            员工id
     * @return 员工名
     */
    public String getStaffOrgName(int staffId) {
		TsmStaff staff = this.getStaff(staffId);
        if (staff == null) {
            log.warn("没有查询到staffId为{}的员工!", staffId);
            return "";
        }
        return staff.getOrganizationId();
    }

    /**
     * 根据员工登录名字查询员工STAFFID
     * 
     * @param logonName
     *            员工登录名字
     * @return 员工ID
     */
    public int getStaffId(String logonName) {
		TsmStaff staff = this.getLogonStaffByLoginName(logonName);
        if (staff == null) {
            log.warn("没有查询到logonName为{}的员工!", logonName);
            return 0;
        }
        return Integer.parseInt(staff.getId());
    }

    /**
     * 查询组织机构名
     * 
     * @param orgId
     *            部门ID
     * @return orgName 部门名
     */
    public String getOrgName(String orgId) {
		Map orgMap = this.getOrgMapNew(orgId);
    	if(orgMap == null) {
    		return "";
    	}
    	return orgMap.get("ORG_NAME") == null ? "" : orgMap.get("ORG_NAME").toString();
    }
	
    @SuppressWarnings({ "unchecked" })
	public Map getOrgMap(String orgId) {
    	List tmpList = null;
    	try {
    		String tmpString = this.redisUtils.get("TSM_ORGANIZATION.List", RedisType.WORKSHEET);
    		tmpList = JSON.parseObject(tmpString, List.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：TSM_ORGANIZATION.List {}", e.getMessage());
    	}
    	
    	if(tmpList == null) {
    		String strsql = "SELECT ORG_ID,LINKID,ORG_NAME,UP_ORG,REGION_ID FROM TSM_ORGANIZATION";
    		tmpList = jdbcTemplate.queryForList(strsql);
            JSONArray jsonArray = JSONArray.fromObject(tmpList);
            this.redisUtils.setex("TSM_ORGANIZATION.List",86400,jsonArray.toString(),RedisType.WORKSHEET);
    	}
    	List<Map> list = tmpList;
    	list = list.stream().filter(item -> orgId.equals(item.get("ORG_ID") == null ? "" : item.get("ORG_ID").toString())).collect(Collectors.toList());
    	if(list.isEmpty()) {
    		return Collections.emptyMap();
    	}
    	return list.get(0);
    }

	public Map getOrgMapNew(String orgId) {
		Map tmpMap = null;
    	try {
    		String tmpString = this.redisUtils.get("TSM_ORGANIZATION.Map__"+orgId, RedisType.WORKSHEET);
    		tmpMap = JSON.parseObject(tmpString, Map.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：TSM_ORGANIZATION.Map {}", e.getMessage());
    	}
    	
    	if(tmpMap == null) {
    		String strsql = "SELECT ORG_ID,LINKID,ORG_NAME,UP_ORG,REGION_ID FROM TSM_ORGANIZATION WHERE ORG_ID=?";
    		List tmpList = jdbcTemplate.queryForList(strsql, orgId);
    		if(!tmpList.isEmpty()) {
    			tmpMap = (Map)tmpList.get(0);
    			String jsonString = JSON.toJSONString(tmpMap);
                this.redisUtils.setex("TSM_ORGANIZATION.Map__"+orgId,86400,jsonString,RedisType.WORKSHEET);
    		}
    	}
    	return tmpMap;
    }

	public Map getOrgMapOld(String orgId) {
		String strsql = "SELECT ORG_ID,LINKID,ORG_NAME,UP_ORG,REGION_ID FROM TSM_ORGANIZATION WHERE ORG_ID=?";
		List tmpList = jdbcTemplate.queryForList(strsql, orgId);
    	if(tmpList.isEmpty()) {
    		return Collections.emptyMap();
    	}
    	return (Map)tmpList.get(0);
    }

	/**
	 * 查询二级组织机构名
	 * 
	 * @param orgId
	 *            部门ID
	 * @return orgName 二级部门名
	 */
	public String getSecOrgName(String orgId) {
		Map orgMap = this.getOrgMapNew(orgId);
    	if(orgMap == null) {
    		return "";
    	}
    	String upOrg = orgMap.get("UP_ORG") == null ? "" : orgMap.get("UP_ORG").toString();
    	if("10".equals(upOrg)) {
    		return orgMap.get("ORG_NAME") == null ? "" : orgMap.get("ORG_NAME").toString();
    	}
    	return "";
	}

    /**
     * 根据部门ID取得该部门所属的地域
     * 
     * @param orgId
     *            部门ID
     * @return 部门所在地域
     */
    public int getOrgRegion(String orgId) {
		Map orgMap = this.getOrgMapNew(orgId);
    	if(orgMap == null) {
    		return 0;
    	}
    	return orgMap.get("REGION_ID") == null ? 0 : Integer.parseInt(orgMap.get("REGION_ID").toString());
    }

    /**
     * 根据 staffId获取本地网信息
     * 
     * @param staffId
     *            员工id
     * @return Map<LANT_ID,REGION_TELNO> 本地网id,区号
     */
    public Map getLantInfoByStaffId(int staffId) {
        Map lantInfo = null;
        String strsql = "SELECT CASE WHEN C.REGION_LEVEL = '97D' THEN C.SUPER_ID WHEN C.REGION_LEVEL = '97C' THEN C.REGION_ID ELSE 2 END AS LANT_ID, C.REGION_TELNO \r\n"
        		+ "FROM TSM_STAFF A, TSM_ORGANIZATION B, TRM_REGION C WHERE A.ORG_ID = B.ORG_ID AND A.STAFF_ID = ? \r\n"
        		+ "AND C.REGION_ID = B.REGION_ID";
        List tmpList = jdbcTemplate.queryForList(strsql, staffId);
        if (tmpList.isEmpty()) {
            return lantInfo;
        } else {
            lantInfo = (Map) tmpList.get(0);
        }
        return lantInfo;
    }

    /**
     * 根据 orgId获取本地网信息
     * 
     * @param orgId
     *            机构id
     * @return Map<LANT_ID,LANT_NAME> 本地网id,本地网名称
     */
    public Map getLantInfoByOrgId(String orgId) {
        Map lantInfo = null;
        String strsql = "SELECT REGION_ID LANT_ID, REGION_NAME LANT_NAME FROM TRM_REGION T WHERE T.REGION_ID=("
        		+ "SELECT CASE WHEN C.REGION_LEVEL = '97D' THEN C.SUPER_ID WHEN C.REGION_LEVEL = '97C' THEN C.REGION_ID ELSE 2 END AS REGION_ID "
        		+ "FROM TSM_ORGANIZATION O,TRM_REGION C WHERE O.REGION_ID=C.REGION_ID AND O.ORG_ID = ?)";
        List tmpList = jdbcTemplate.queryForList(strsql, orgId);
        if (tmpList.isEmpty()) {
            return lantInfo;
        } else {
            lantInfo = (Map) tmpList.get(0);
        }
        return lantInfo;
    }

    /**
     * 根据部门ID取得该部门所属的LINKId
     * 
     * @param orgId 部门ID
     * @return 部门所属的地域。如果没有查询到结果，返回"10"
     */
    public String getOrgLink(String orgId) {
		Map orgMap = this.getOrgMapNew(orgId);
    	if(orgMap == null) {
    		return "10";
    	}
    	return orgMap.get("LINKID") == null ? "10" : orgMap.get("LINKID").toString();
    }

	public String getOrgWater(String orgId) {
		String linkid = getOrgLink(orgId);
		if ("10".equals(linkid)) {
			return getOrgName(linkid);
		} else {
			String[] orgIds = linkid.split("-");
			StringBuilder orgWater = new StringBuilder();
			for (int i = 1; i < orgIds.length; i++) {
				if (i == orgIds.length - 1) {
					orgWater.append(getOrgName(orgIds[i]));
				} else {
					orgWater.append(getOrgName(orgIds[i]) + "-");
				}
			}
			return orgWater.toString();
		}
	}

    /**
     * 查询静态数名
     * @param referId
     * @return 静态数名
     */
    public String getStaticName(long referId) {
    	Map columnMap = this.getColumnMap(String.valueOf(referId));
    	if(columnMap == null) {
    		return "";
    	}
    	return columnMap.get("COL_VALUE_NAME") == null ? "" : columnMap.get("COL_VALUE_NAME").toString();
    }
    
    public String getStringSize(int size) {
        //获取到的size为：
        int gb = 1024 * 1024 * 1024;//定义GB的计算常量
        int mb = 1024 * 1024;//定义MB的计算常量
        int kb = 1024;//定义KB的计算常量
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String resultSize = "";
        if (size / gb >= 1) {
            //如果当前Byte的值大于等于1GB
            resultSize = df.format(size / (float) gb) + "GB   ";
        } else if (size / mb >= 1) {
            //如果当前Byte的值大于等于1MB
            resultSize = df.format(size / (float) mb) + "MB   ";
        } else if (size / kb >= 1) {
            //如果当前Byte的值大于等于1KB
            resultSize = df.format(size / (float) kb) + "KB   ";
        } else {
            resultSize = size + "B   ";
        }
        return resultSize;
    }

	public Map getColumnMap(String referId) {
		Map tmpMap = null;
    	try {
    		String tmpString = this.redisUtils.get("PUB_COLUMN_REFERENCE.Map__"+referId, RedisType.WORKSHEET);
    		tmpMap = JSON.parseObject(tmpString, Map.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：PUB_COLUMN_REFERENCE.Map {}", e.getMessage());
    	}
    	
    	if(tmpMap == null) {
    		String strsql = "SELECT REFER_ID,TABLE_CODE,TABLE_CODE,COL_VALUE_NAME,COL_NAME,COL_VALUE,COL_VALUE_HANDLING,ENTITY_ID FROM PUB_COLUMN_REFERENCE WHERE REFER_ID=?";
    		List tmpList = jdbcTemplate.queryForList(strsql, referId);
    		if(!tmpList.isEmpty()) {
    			tmpMap = (Map)tmpList.get(0);
    			String jsonString = JSON.toJSONString(tmpMap);
                this.redisUtils.setex("PUB_COLUMN_REFERENCE.Map__"+referId,86400,jsonString,RedisType.WORKSHEET);
    		}
    	}
    	return tmpMap;
    }

	public String getTableColName(String referId) {
    	Map columnMap = this.getColumnMap(referId);
    	if(columnMap == null) {
    		return "";
    	}
    	String tableCode = columnMap.get("TABLE_CODE") == null ? "" : columnMap.get("TABLE_CODE").toString();
    	String colCode = columnMap.get("COL_CODE") == null ? "" : columnMap.get("COL_CODE").toString();
    	return tableCode + "@" + colCode;
    }
	
	/**
	 * 查询上级id
	 * @param referId
	 * @return 上级id
	 */
	public String getSuperStaticId(long referId) {
		Map columnMap = this.getColumnMap(String.valueOf(referId));
    	if(columnMap == null) {
    		return "";
    	}
    	return columnMap.get("ENTITY_ID") == null ? "" : columnMap.get("ENTITY_ID").toString();
	}
 
    /**
     * 得到两者时间差，时间格式为yyyy-MM-dd HH:mm:ss
     * 
     * @param beginTime
     * @param endTime
     * @param timeBetweenFlag
     *            时间差标识，1、标识小时差；2标识分钟差；3、标识秒时间差；4、标识毫秒时间差
     * @return 两者时间差
     */
    public static long getTimeBetween(String beginTime, String endTime, int timeBetweenFlag) {
        long betweenDate = 0;
        try {
            // 计算两者时间差
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            // 开始时间
            Date beginDate = simpleDateFormat.parse(beginTime);
            // 结束时间
            Date endDate = simpleDateFormat.parse(endTime);

            switch (timeBetweenFlag) {
                case 1:// 小时差
                    betweenDate = (endDate.getTime() - beginDate.getTime()) / (1000 * 60 * 60);// 除以1000*60*60是为了转换成小时
                    break;
                case 2:// 分钟差
                    betweenDate = (endDate.getTime() - beginDate.getTime()) / (1000 * 60);// 除以1000*60是为了转换成分
                    break;
                case 3:// 秒差
                    betweenDate = (endDate.getTime() - beginDate.getTime()) / 1000;// 除以1000是为了转换成秒
                    break;
                case 4:// 毫秒差
                    betweenDate = endDate.getTime() - beginDate.getTime();
                    break;
                default:
                	break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return betweenDate;
    }

    /**
     * 查询系统时间
     * 
     * @return 系统时间
     */
    @SuppressWarnings("unchecked")
	public String getSysDate() {
        String strsql = "SELECT NOW()";
        String sysdate = jdbcTemplate.query(strsql, new KeyRowMapper()).get(0).toString();
        return dbDateToStr(sysdate);
    }
    /**
     * 查询系统时间
     * 
     * @return 系统时间
     */
    @SuppressWarnings("unchecked")
	public String getAddMinutes(int minutes) {
        String strsql = "SELECT date_add(now(),interval ? MINUTE)";
        String sysdate = jdbcTemplate.query(strsql, new Object[] { minutes }, new KeyRowMapper()).get(0).toString();
        return dbDateToStr(sysdate);
    }
    public String addDate(String strDate, Map map) {
        SimpleDateFormat forma = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = forma.parse(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            // 增加小时
            if (map.containsKey("HOUR")) {
                calendar.add(Calendar.HOUR, Integer.parseInt(map.get("HOUR").toString()));
            }
            strDate = forma.format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return strDate;
    }

    /**
     * 将数据查询时返回的日期格式后的豪称去掉
     * 
     * @param date
     * @return
     */
    public static String dbDateToStr(String date) {
        String retStr = "";
        if (StringUtils.isNotEmpty(date) && date.length()>=19) {
            retStr = date.substring(0, 19);
        }
        return retStr;
    }

    /**
     * 查询一个地域的区号
     * 
     * @param regionId
     *            地域id
     * @return 区号
     */
    public String getRegionTelNo(int regionId) {
    	Map regionMap = this.getRegionMap(String.valueOf(regionId));
    	if(regionMap.isEmpty()) {
    		return "";
    	}
    	return regionMap.get("REGION_TELNO") == null ? "" : regionMap.get("REGION_TELNO").toString();
    }
    
    @SuppressWarnings({ "unchecked" })
	public Map getRegionMap(String regionId) {
    	List tmpList = null;
    	try {
    		String tmpString = this.redisUtils.get("TRM_REGION.List", RedisType.WORKSHEET);
    		tmpList = JSON.parseObject(tmpString, List.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：TRM_REGION.List {}", e.getMessage());
    	}
    	
    	if(tmpList == null) {
    		String strsql = "SELECT T.REGION_ID,T.REGION_LEVEL,T.REGION_NAME,T.SUPER_ID,T.REGION_TELNO FROM TRM_REGION T";
    		tmpList = jdbcTemplate.queryForList(strsql);
            JSONArray jsonArray = JSONArray.fromObject(tmpList);
            this.redisUtils.setex("TRM_REGION.List",86400*30,jsonArray.toString(),RedisType.WORKSHEET);
    	}
    	List<Map> list = tmpList;
    	list = list.stream().filter(item -> regionId.equals(item.get("REGION_ID") == null ? "" : item.get("REGION_ID").toString())).collect(Collectors.toList());
    	if(list.isEmpty()) {
    		return Collections.emptyMap();
    	}
    	return list.get(0);
    }

	/**
	 * 查询一个地域的regionId
	 * 
	 * @param regionTelNo
	 * @return regionId
	 */
    @SuppressWarnings({ "unchecked" })
	public String getRegionId(String regionTelNo) {
		String strsql = "SELECT region_id FROM trm_region WHERE region_level = '97C' AND region_telno = ?";
		List tmpList = jdbcTemplate.query(strsql, new Object[] { regionTelNo }, new KeyRowMapper());
		if (tmpList.isEmpty()) {
			log.warn("没有查询到regionTelNo为" + regionTelNo + "的regionId");
			return "";
		}
		String regionId = "";
		if (null != tmpList.get(0)) {
			regionId = tmpList.get(0).toString();
		}
		if (regionId.length() < 2) {
			regionId = "0" + regionId;
		}
		tmpList.clear();
		tmpList = null;
		return regionId;
	}

    /**
     * 得到97D级(县市)的上级本地网地域id
     * 
     * @param regionId
     *            地域id
     * @return 97D级(县市)的上级本地网地域id
     */
    @SuppressWarnings({ "unchecked" })
    public int getUpRegionId(int regionId) {
        String strsql = "SELECT B.REGION_ID "
                + "FROM TRM_REGION A, TRM_REGION B WHERE A.REGION_ID = ? "
                + "AND A.SUPER_ID = B.REGION_ID AND B.REGION_LEVEL='97C'";
        List tmpList = jdbcTemplate.query(strsql, new Object[] {regionId}, new KeyRowMapper());

        if (tmpList.isEmpty()) {
            log.warn("没有查询到regionId为" + regionId + "上级地域");
            return regionId;
        }

        int upRegionId = Integer.parseInt(tmpList.get(0).toString());
        tmpList.clear();
        tmpList = null;
        return upRegionId;

    }

    /**
     * 查询地域级别
     * 
     * @param regionId
     *            地域id
     * @return 地域级别
     */
    public String getRegionLevel(int regionId) {
    	Map regionMap = this.getRegionMap(String.valueOf(regionId));
    	if(regionMap.isEmpty()) {
    		return "";
    	}
    	return regionMap.get("REGION_LEVEL") == null ? "" : regionMap.get("REGION_LEVEL").toString();
    }

    /**
     * 查询地域名
     * 
     * @param regionId
     *            地域id
     * @return 地域名
     */
    public String getRegionName(int regionId) {
    	Map regionMap = this.getRegionMap(String.valueOf(regionId));
    	if(regionMap.isEmpty()) {
    		return "";
    	}
    	return regionMap.get("REGION_NAME") == null ? "" : regionMap.get("REGION_NAME").toString();
    }

    /**
     * 查询link_id
     * 
     * @param regionId
     *            地域id
     * @return 地域名
     */
    public String getRegionLinkId(int regionId) {
    	Map regionMap = this.getRegionMap(String.valueOf(regionId));
    	if(regionMap.isEmpty()) {
    		return "";
    	}
    	return regionMap.get("LINK_ID") == null ? "" : regionMap.get("LINK_ID").toString();
    }
    
    @SuppressWarnings({ "unchecked" })
    public int getCrmrefId(String table, String colcode, String entiyId) {
        String strSql = "  SELECT C.REFER_ID\n" + " FROM PUB_COLUMN_REFERENCE C\n"
                + "WHERE C.TABLE_CODE = ?\n" + "  AND C.COL_CODE = ?\n" + "  AND C.ENTITY_ID=?";
        List tmpList = jdbcTemplate.query(strSql, new Object[] {table, colcode, entiyId}, new KeyRowMapper());

        if (tmpList.isEmpty()) {
            log.warn("没有查询到custGroupId为" + entiyId + "客户品牌大类!");
            return 0;
        }

        int custBrandId = Integer.parseInt(tmpList.get(0).toString());
        tmpList.clear();
        tmpList = null;
        return custBrandId;
    }

    /**
     * 根据环节得到工单类型
     * 
     * @param tacheId
     *            环节id
     * @return 工单类型数组信息
     */
    public static int[] getSheetTypeByTacheId(int tacheId) {
        int[] sheetType = new int[3];
        switch (tacheId) {
            case 700000085:// 后台派单
                sheetType[0] = StaticData.SHEET_TYPE_TS_ASSING;
                sheetType[1] = StaticData.SHEET_TYPE_JY_ASSING;
                sheetType[2] = StaticData.SHEET_TYPE_TZ_ASSING;
                break;
            case 700000086:// 部门处理
                sheetType[0] = StaticData.SHEET_TYPE_TS_DEAL;
                sheetType[1] = StaticData.SHEET_TYPE_JY_DEAL;
                sheetType[2] = StaticData.SHEET_TYPE_TZ_DEAL;
                break;
            case 700000087:// 回访客户
                sheetType[0] = StaticData.SHEET_TYPE_TS_REPLY;
                sheetType[1] = StaticData.SHEET_TYPE_JY_REPLY;
                sheetType[2] = StaticData.SHEET_TYPE_TZ_REPLY;
                break;
            case 700000088:// 后台审核
                sheetType[0] = StaticData.SHEET_TYPE_TS_AUIT;
                sheetType[1] = StaticData.SHEET_TYPE_JY_AUIT;
                sheetType[2] = StaticData.SHEET_TYPE_TZ_AUIT;
                break;
            default:
            	break;
        }
        return sheetType;
    }

    /**
     * 根据服务类型和环节得到工单类型
     * 
     * @param servType
     *            服务类型
     * @param tacheId
     *            环节id
     * @return 工单类型
     */
    public static int getSheetType(int servType, int tacheId) {
        int sheetType = 0;
        if(StaticData.SERV_TYPE_NEWTS == servType){// 2013-02 新投诉
            switch (tacheId) {
                case StaticData.TACHE_ORDER_BACK:
                    sheetType = StaticData.SHEET_TYPE_BACK;
                    break;
                case StaticData.TACHE_FINISH_NEW:
                    sheetType = StaticData.SHEET_TYPE_TS_FINISH_NEW;
                    break;
                case StaticData.TACHE_ASSIGN_NEW:
                    sheetType = StaticData.SHEET_TYPE_TS_ASSING_NEW;
                    break;
                case StaticData.TACHE_DEAL_NEW:
                    sheetType = StaticData.SHEET_TYPE_TS_IN_DEAL;
                    break;                    
                case StaticData.TACHE_DINGXING_NEW:
                    sheetType = StaticData.SHEET_TYPE_PREASSESS;
                    break;  
                case StaticData.TACHE_ZHONG_DINGXING_NEW:
                    sheetType = StaticData.SHEET_TYPE_FINASSESS;
                    break;
                case StaticData.TACHE_RGHF:
                    sheetType = StaticData.SHEET_TYPE_RGHF;
                    break;
                default:
                    break;
            }
        }else{
            switch (servType) {
                case StaticData.SERV_TYPE_ZX:
                case StaticData.SERV_TYPE_CX:
                case StaticData.SERV_TYPE_TD:
                case StaticData.SERV_TYPE_XT:
                case StaticData.SERV_TYPE_GZ:
                case StaticData.SERV_TYPE_JY:
                case StaticData.SERV_TYPE_BY:
                case StaticData.SERV_TYPE_ZW:
                case StaticData.SERV_TYPE_BA:
                    servType = StaticData.SERV_TYPE_TS;
                    break;
                case StaticData.SERV_TYPE_QT:
                    servType = StaticData.SERV_TYPE_TZ;
                    break;
                default:
            }
            
            // 环节工单修改退回
            if(StaticData.TACHE_ORDER_ASK == tacheId){
            	return StaticData.SHEET_TYPE_ANY_BACK;
            }
        }
        
        if (tacheId == StaticData.TACHE_DEAL) {// 部门处理
            switch (servType) {
                case StaticData.SERV_TYPE_TS:
                    sheetType = StaticData.SHEET_TYPE_TS_DEAL;
                    break;
                case StaticData.SERV_TYPE_YS:
                    sheetType = StaticData.SHEET_TYPE_YS_DEAL;
                    break;
                case StaticData.SERV_TYPE_SJ:
                    sheetType = StaticData.SHEET_TYPE_SJ_DEAL;
                    break;
                case StaticData.SERV_TYPE_TZ:
                    sheetType = StaticData.SHEET_TYPE_TZ_DEAL;
                    break;
                case StaticData.SERV_TYPE_QQ:
                    sheetType = StaticData.SHEET_TYPE_QQ_DEAL;
                    break;
                default:
            }
        }else if (tacheId == StaticData.TACHE_ASSIGN) { // 后台派单
            switch (servType) {
                case StaticData.SERV_TYPE_TS:
                    sheetType = StaticData.SHEET_TYPE_TS_ASSING;
                    break;
                case StaticData.SERV_TYPE_YS:
                    sheetType = StaticData.SHEET_TYPE_YS_ASSING;
                    break;
                case StaticData.SERV_TYPE_SJ:
                    sheetType = StaticData.SHEET_TYPE_SJ_ASSING;
                    break;
                case StaticData.SERV_TYPE_TZ:
                    sheetType = StaticData.SHEET_TYPE_TZ_ASSING;
                    break;
                case StaticData.SERV_TYPE_QQ:
                    sheetType = StaticData.SHEET_TYPE_QQ_ASSING;
                    break;
                default:
            }
        }else if (tacheId == StaticData.TACHE_AUIT) { // 后台审核
            switch (servType) {
                case StaticData.SERV_TYPE_TS:
                    sheetType = StaticData.SHEET_TYPE_TS_AUIT;
                    break;
                case StaticData.SERV_TYPE_YS:
                    sheetType = StaticData.SHEET_TYPE_YS_AUD;
                    break;
                case StaticData.SERV_TYPE_SJ:
                    sheetType = StaticData.SHEET_TYPE_SJ_AUD;
                    break;
                case StaticData.SERV_TYPE_TZ:
                    sheetType = StaticData.SHEET_TYPE_TZ_AUIT;
                    break;
                case StaticData.SERV_TYPE_QQ:
                    sheetType = StaticData.SHEET_TYPE_QQ_AUD;
                    break;
                default:
            }
        }else if (tacheId == 1) {// 部门审批单虚拟环节为1
            switch (servType) {
                case StaticData.SERV_TYPE_TS:
                    sheetType = StaticData.SHEET_TYPE_TS_DEAL;
                    break;
                case StaticData.SERV_TYPE_TZ:
                    sheetType = StaticData.SHEET_TYPE_TZ_CHECK_DEAL;
                    break;
                default:
            }
        }else if (tacheId == StaticData.TACHE_TSQUALITATIVE) {// 定性环节
            switch (servType) {
                case StaticData.SERV_TYPE_TS:// 投诉
                    sheetType = StaticData.SHEET_TYPE_TS_QUALITATIVE;
                    break;
                case StaticData.SERV_TYPE_TZ:
                    break;
                default:
            }
        }else if (tacheId == StaticData.TACHE_PIGEONHOLE) { // 归档
            switch (servType) {
                case StaticData.SERV_TYPE_TS:
                    sheetType = StaticData.SHEET_TYPE_TS_PIGEONHOLE;
                    break;
                case StaticData.SERV_TYPE_TZ:
                    break;
                default:
            }
        }else if (tacheId == StaticData.TACHE_FINISH) { // 竣工
            switch (servType) {
                case StaticData.SERV_TYPE_TS:
                    sheetType = StaticData.SHEET_TYPE_TS_FINISH;
                    break;
                case StaticData.SERV_TYPE_YS:
                    sheetType = StaticData.SHEET_TYPE_YS_FINIAH;
                    break;
                case StaticData.SERV_TYPE_TZ:
                    sheetType = StaticData.SHEET_TYPE_TZ_FINISH;
                    break;
                case StaticData.SERV_TYPE_QQ:
                    sheetType = StaticData.SHEET_TYPE_QQ_FINIAH;
                    break;
                default:
            }
        }else if (tacheId == StaticData.TACHE_REPLY) {// 回访
            switch (servType) {
                case StaticData.SERV_TYPE_TS:// 投诉
                    sheetType = StaticData.SHEET_TYPE_TS_REPLY;
                    break;
                case StaticData.SERV_TYPE_TZ:// 投诉咨询
                    sheetType = StaticData.SHEET_TYPE_TZ_REPLY;
                    break;
                default:
            }
        }
        return sheetType;
    }

    /**
     * 提取和释放的时候得到工单状态
     * @param tachId 环节ID
     * @param lockFlag 0表示在部门工单池；1表示在个人工单池
     * @param sheetType 工单类型ID
     * @return 工单状态ID
     */
    public int getSheetStatu(int tachId, int lockFlag, int sheetType) {
        int statu = 0;
        if (lockFlag == 1) {
            switch (tachId) {
                case StaticData.TACHE_ASSIGN:// 后台派单
                    statu = StaticData.WKST_DEALING_STATE;
                    break;
                case StaticData.TACHE_DEAL:// 部门处理审批
                    if (sheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
                        statu = StaticData.WKST_ORGAUDING_STATE;// 审批中
                    } else {
                        statu = StaticData.WKST_ORGDEALING_STATE;// 部门处理中
                    }
                    break;
                case StaticData.TACHE_AUIT:// 后台审核
                    statu = StaticData.WKST_AUDING_STATE;
                    break;
                case StaticData.TACHE_ASSIGN_NEW:      //派单
                case StaticData.TACHE_ORDER_BACK:
                case StaticData.TACHE_DEAL_NEW:      //部门处理
                case StaticData.TACHE_DINGXING_NEW:  //预定性
                case StaticData.TACHE_ZHONG_DINGXING_NEW:  //终定性
                case StaticData.TACHE_RGHF:
                    statu = StaticData.WKST_DEALING_STATE_NEW;
                    break;
                case StaticData.TACHE_REPLY:// 回访
                    statu = StaticData.WKST_REPLYING_STATE;
                    break;
                case StaticData.TACHE_PIGEONHOLE:// 归档
                    statu = StaticData.WKST_PRGEONHOLE_STATE;
                    break;
                case StaticData.TACHE_TSASSESS:// 考核
                    statu = StaticData.WKST_TSASSESS_STATE;
                    break;
                case StaticData.TACHE_TSQUALITATIVE:// 定性
                    statu = StaticData.WKST_TSQUALIATIVE_STATE;
                    break;
                case StaticData.TACHE_YS_DEAL:// 预受理处理环节
                    statu = StaticData.WKST_DEALING_STATE;
                    break;
                case StaticData.TACHE_SEPARAT:// 分拣环节
                    statu = StaticData.WKST_SEPARATING;
                    break;
                default:
                    break;
            }
        }else if (lockFlag == 0) {
            switch (tachId) {
                case StaticData.TACHE_ASSIGN:
                    statu = StaticData.WKST_WAIT_DEAL_STATE;
                    break;
                case StaticData.TACHE_DEAL:
                    if (sheetType == StaticData.SHEET_TYPE_TS_CHECK_DEAL) {
                        statu = StaticData.WKST_ORGAUD_STATE;// 待审批
                    } else {
                        statu = StaticData.WKST_REPEAL_STATE;// 部门待处理
                    }
                    break;
                case StaticData.TACHE_AUIT:
                    statu = StaticData.WKST_AUD_STATE;
                    break;
                case StaticData.TACHE_ASSIGN_NEW:      //派单
                case StaticData.TACHE_ORDER_BACK:
                case StaticData.TACHE_DEAL_NEW:      //部门处理
                case StaticData.TACHE_DINGXING_NEW:  //预定性
                case StaticData.TACHE_ZHONG_DINGXING_NEW:  //终定性
                case StaticData.TACHE_RGHF:
                    statu = StaticData.WKST_REPEAL_STATE_NEW;
                    break;
                case StaticData.TACHE_REPLY:
                    statu = StaticData.WKST_REPLY_STATE;
                    break;
                case StaticData.TACHE_PIGEONHOLE:
                    statu = StaticData.WKST_PRGEONHOLE;
                    break;
                case StaticData.TACHE_TSASSESS:
                    statu = StaticData.WKST_TSASSESS;
                    break;
                case StaticData.TACHE_TSQUALITATIVE:
                    statu = StaticData.WKST_TSQUALIATIVE;
                    break;
                case StaticData.TACHE_YS_DEAL:
                    statu = StaticData.WKST_WAIT_DEAL_STATE;
                    break;
                case StaticData.TACHE_SEPARAT:
                    statu = StaticData.WKST_FOR_SEPARA;
                    break;
                case StaticData.TACHE_FINISH_NEW:
                    statu = StaticData.WKST_FINISH_STATE_NEW;
                    break;
                default:
                    break;
            }
        }
        return statu;
    }

    /**
     * 判断部门lowerOrgId是否是部门upperOrgId的下级单位<br>
     * 
     * @author LiJiahui
     * @date 2011-08-26
     * @param lowerOrgId
     *            下级部门
     * @param upperOrgId
     *            上级部门
     * @return 如果lowerOrgId是upperOrgId的下属单位，则返回true，否则返回false
     */
    public boolean isAffiliated(String lowerOrgId, String upperOrgId) {
        String sql = "SELECT COUNT(*) FROM TSM_ORGANIZATION A WHERE A.ORG_ID = ? AND A.LINKID LIKE CONCAT((SELECT B.LINKID FROM TSM_ORGANIZATION B WHERE B.ORG_ID = ?) , '%')";
        int num = jdbcTemplate.queryForObject(sql, new Object[] { lowerOrgId, upperOrgId }, Integer.class);
        return num > 0;
    }

    /**
     * 获取同一个本地网收到同一张订单派单的处理单次数
     * 
     * @date 2013-01-24
     * @param serverOrderId
     *            订单号
     * @param receiveOrgId
     *            收单部门
     * @return
     */
    public int getSendCount(String serverOrderId, String receiveOrgId) {
		String sql = "SELECT COUNT(DISTINCT A.WORK_SHEET_ID) FROM CC_WORK_SHEET A, TSM_ORGANIZATION B WHERE A.TACHE_ID = 720130023 " +
				"AND A.DEAL_DESC = '部门处理退单' AND A.SERVICE_ORDER_ID = ? AND " +
				"A.RECEIVE_ORG_ID = B.ORG_ID " +
				"AND (CASE if(LENGTH(B.LINKID)-LENGTH(REPLACE(B.LINKID,'-',''))>=2, LENGTH(SUBSTRING_INDEX(B.LINKID,'-',2))+1, 0) WHEN 0 THEN B.LINKID ELSE SUBSTR( B.LINKID, 1, if(LENGTH(B.LINKID)-LENGTH(REPLACE(B.LINKID,'-',''))>=2, LENGTH(SUBSTRING_INDEX(B.LINKID,'-',2))+1, 0)-1) END) = (SELECT CASE if(LENGTH(C.LINKID)-LENGTH(REPLACE(C.LINKID,'-',''))>=1, LENGTH(SUBSTRING_INDEX(C.LINKID,'-',1))+1, 0) WHEN 0 THEN '0' ELSE (CASE if(LENGTH(C.LINKID)-LENGTH(REPLACE(C.LINKID,'-',''))>=2, LENGTH(SUBSTRING_INDEX(C.LINKID,'-',2))+1, 0) WHEN 0 THEN C.LINKID ELSE SUBSTR(C.LINKID,1,if(LENGTH(C.LINKID)-LENGTH(REPLACE(C.LINKID,'-',''))>=2, LENGTH(SUBSTRING_INDEX(C.LINKID,'-',2))+1, 0)-1) END) END " +
				"FROM TSM_ORGANIZATION C WHERE C.ORG_ID = ?)";
		return jdbcTemplate.queryForObject(sql, new Object[] { serverOrderId, receiveOrgId }, Integer.class);
    }

	// 获取转派次数：工单转派的次数
	public int getRedirctTimes(String orderId) {
		String sql = "SELECT COUNT(1)FROM cc_work_sheet WHERE sheet_type IN(700000127,720130013)AND main_sheet_flag=1 AND lock_flag<>'9'"
				+ "AND service_order_id=?";
		return jdbcTemplate.queryForObject(sql, new Object[] { orderId }, Integer.class);
	}

    /**
     * 2012年1月版本，为了兼容旧的工单，使用受理渠道信息，判断是否是越级单
     * 
     * @author 李佳慧
     * @date 2012-1-10
     * @param channel
     * @return
     */
    public boolean isYueJi(int channel) {
        int len = StaticData.getNewYueJiChannelId().length;
        for (int i = 0; i < len; i++) {
            if (channel == StaticData.getNewYueJiChannelId()[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * 使用受理渠道信息，判断是否是省集中10000号的单子
     * 
     * @author 李佳慧
     * @date 2012-1-11
     * @param channel
     * @return
     */
    public boolean isS1W(int channel) {
        return channel == StaticData.NEW_1W_CHANNEL_ID;
    }

    /**
     * 设置cc_service_order_ask的hangup_time_count字段
     * 设置CC_WORK_SHEET的hangup_time_count字段
     * @return
     */
    public int updateOrderAndSheetHang(String workSheetId, String serviceOrderId) {
        String sql = "UPDATE CC_WORK_SHEET SET HANGUP_TIME_COUNT = HANGUP_TIME_COUNT + IFNULL(TIMESTAMPDIFF(SECOND, CREAT_DATE, NOW()) / 60, 0) WHERE WORK_SHEET_ID = ?";
        jdbcTemplate.update(sql, workSheetId);
        sql =
"UPDATE cc_service_order_ask a\n" +
"   SET hangup_time_count = hangup_time_count + (SELECT IFNULL(TIMESTAMPDIFF(SECOND, CREAT_DATE, NOW()) / 60, 0) FROM cc_work_sheet WHERE work_sheet_id = ?)\n" +
" WHERE service_order_id = ?\n" + 
"   AND EXISTS (SELECT 1\n" + 
"          FROM cc_service_label b\n" + 
"         WHERE b.service_order_id = a.service_order_id\n" + 
"           AND b.dx_finish_date IS NULL)";
        return jdbcTemplate.update(sql, workSheetId, serviceOrderId);
    }

	/**
	 * 设置订单表cc_service_order_ask的hangup_time_count字段
	 * @return
	 */
	public int updateOrderHang(String workSheetId, String serviceOrderId) {
        String sql = "UPDATE cc_work_sheet SET hangup_time_count = hangup_time_count + IFNULL(TIMESTAMPDIFF(SECOND, respond_date, NOW()) / 60, 0) WHERE work_sheet_id = ?";
        jdbcTemplate.update(sql, workSheetId);
		sql =
"UPDATE cc_service_order_ask a\n" +
"   SET hangup_time_count = hangup_time_count + (SELECT IFNULL(TIMESTAMPDIFF(SECOND, respond_date, NOW()) / 60, 0) FROM cc_work_sheet WHERE work_sheet_id = ?)\n" +
" WHERE service_order_id = ?\n" + 
"   AND EXISTS (SELECT 1\n" + 
"          FROM cc_service_label b\n" + 
"         WHERE b.service_order_id = a.service_order_id\n" + 
"           AND b.dx_finish_date IS NULL)";
		return jdbcTemplate.update(sql, workSheetId, serviceOrderId);
	}

    private String getAllSecDutySql=
    		"SELECT P.COL_VALUE_NAME AS ORG_NAME, SUBSTR(P.COL_VALUE_HANDLING,4) AS ORG_ID\n" +
    				"        FROM PUB_COLUMN_REFERENCE P\n" + 
    				"       WHERE P.TABLE_CODE = 'CC_RESPONSIBILITY_ORG'\n" + 
    				"         AND P.COL_CODE = 'DUTY_ORG_SEC'\n" + 
    				"         AND P.COL_VALUE_HANDLING IS NOT NULL\n" + 
    				"       ORDER BY P.COL_NAME DESC, P.REFER_ID";

    public List getAllSecDuty(){
        return jdbcTemplate.queryForList(getAllSecDutySql);//CodeSec未验证的SQL注入；CodeSec误报：1
    }

    /**
     * 获取除了指定部门所属二级部门之外的其他所有二级部门
     * @param orgid
     * @return
     */
    public List getSecDutyExceptSelf(String orgid){
    	List all = getAllSecDuty();
    	String selfLink = getOrgLink(orgid);
    	if(null==selfLink){
    		return all;
    	}
    	String[] ary = selfLink.split("-");
    	if(ary.length<2){
    		return all;
    	}
    	String self = ary[1];
    	int len = all.size();
    	Map one = null;
    	int locate = -1;
    	for(int i = 0; i < len; i++){
    		one = (Map)all.get(i);
    		if(self.equals(one.get("ORG_ID").toString())){
    			locate = i;
    			break;
    		}
    	}
    	if(locate>-1){
    		all.remove(locate);
    	}
    	one = null;
    	return all;
    }
    
    /**
     * 环节流转时，生成新工单的flow sequence值
     * @param flowSeq 源工单的flow sequence值
     * @param dfSeq 默认值
     * @param add 增幅
     * @return 返回生成的flow sequence
     */
    public String crtFlowSeq(String flowSeq, String dfSeq, int add){
        String tmp = dfSeq;
        if(flowSeq!=null && flowSeq.length()>0){
            tmp=flowSeq;
        }
        int len=tmp.length();
        int last=Integer.parseInt(tmp.substring(len-1))+ add;
        return tmp.substring(0,len-1)+last;
    }
 
	@SuppressWarnings({ "unchecked" })
	public int getPersonalityJudge(String judgeId, String param) {
		int rtn = 0;
		List list = jdbcTemplate.query("SELECT sql_content FROM cc_personality_judge WHERE judge_id = ?", new Object[] { judgeId }, new KeyRowMapper());
		if (list.isEmpty()) {
			return 0;
		}
		String sqlContent = "";
		if (null != list.get(0)) {
			sqlContent = list.get(0).toString();
		}
		if (!sqlContent.equals("")) {
			rtn = this.jdbcTemplate.queryForObject(sqlContent, new Object[] { param },Integer.class);//CodeSec未验证的SQL注入；CodeSec误报：1
		}
		return rtn;
	}
	
	/**
	 * 查询系统控制标识
	 * @return
	 */
	public String querySysContolFlag(String ctrId) {
		String ctrFlag = "";
		try {
			String tmpString = this.redisUtils.get(ctrId, RedisType.WORKSHEET);
			if(tmpString != null) {
				ctrFlag = tmpString;
			}
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：{} {}", ctrId, e.getMessage());
    	}
		return ctrFlag;
	}
	
	/**
	 * 更新系统控制标识
	 * @param ctrId
	 * @param allotFlag
	 */
	public void updateSysContolFlag(String ctrId, String allotFlag, int expiration) {
		try {
			this.redisUtils.setex(ctrId, expiration, allotFlag, RedisType.WORKSHEET);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：{} {}", ctrId, e.getMessage());
    	}
	}

	/*
	 * 查询系统控制标识
	 */
	@SuppressWarnings({ "unchecked" })
	public String querySysContolSwitch(String switchId) {
		
		List tmpList = null;
    	try {
    		String tmpString = this.redisUtils.get("TSM_SYS_CONTOL_SWITCH.List", RedisType.WORKSHEET);
    		tmpList = JSON.parseObject(tmpString, List.class);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：TSM_SYS_CONTOL_SWITCH.List {}", e.getMessage());
    	}
    	
    	if(tmpList == null) {
    		String strsql = "SELECT SWITCH_ID,SWITCH_STATE FROM TSM_SYS_CONTOL_SWITCH";
    		tmpList = jdbcTemplate.queryForList(strsql);
            JSONArray jsonArray = JSONArray.fromObject(tmpList);
            this.redisUtils.setex("TSM_SYS_CONTOL_SWITCH.List",86400,jsonArray.toString(),RedisType.WORKSHEET);
    	}
    	List<Map> list = tmpList;
    	list = list.stream().filter(item -> switchId.equals(item.get("SWITCH_ID") == null ? "" : item.get("SWITCH_ID").toString())).collect(Collectors.toList());
    	if(list.isEmpty()) {
    		return "";
    	}
    	return list.get(0).get("SWITCH_STATE") == null ? "" : list.get(0).get("SWITCH_STATE").toString();
	}
	
	/**
	 * 查询系统控制标识
	 */
	public String querySysContolSwitchNew(String switchId) {
		String tmpString = null;
    	try {
    		tmpString = this.redisUtils.get("TSM_SYS_CONTOL_SWITCH.SWITCH_ID__"+switchId, RedisType.WORKSHEET);
    	}
    	catch(Exception e) {
    		log.error("redisUtils异常：TSM_SYS_CONTOL_SWITCH.SWITCH_ID__{} {}", switchId, e.getMessage(), e);
    	}
    	
    	if(tmpString == null) {
    		String strsql = "SELECT SWITCH_STATE FROM TSM_SYS_CONTOL_SWITCH WHERE SWITCH_ID=?";
    		List tmpList = jdbcTemplate.queryForList(strsql, switchId);
    		if(!tmpList.isEmpty()) {
    			Map tmpMap = (Map)tmpList.get(0);
    			tmpString = tmpMap.get("SWITCH_STATE") == null ? "" : tmpMap.get("SWITCH_STATE").toString();
    			
                this.redisUtils.setex("TSM_SYS_CONTOL_SWITCH.SWITCH_ID__"+switchId,86400,tmpString,RedisType.WORKSHEET);
    		}
    	}
    	
    	if(tmpString == null) {
    		return "";
    	}
    	return tmpString;
	}

	/*
	 * 修改系统控制标识
	 */
	public void updateSysContolSwitch(String switchId, String switchState) {
		String sql = "UPDATE tsm_sys_contol_switch SET switch_state = ? WHERE switch_id = ?";
		jdbcTemplate.update(sql, switchState, switchId);
	}

	/**
	 * 查询人员信息
	 * 
	 * @return
	 */
	public String queryJtFtpStaff() {
		String str = "";
		String sql = "SELECT txt_data FROM ccs_jt_ftp WHERE biz_code = 'BUS43010'";
		List list = jdbcTemplate.queryForList(sql);
		if (list.size() == 1) {
			Map map = (Map) list.get(0);
			str = map.get("TXT_DATA").toString();
			list.clear();
			list = null;
		}
		return str;
	}

	/**
	 * 更新人员信息
	 * 
	 * @return
	 */
	public int updateJtFtpStaff(String txtData) {
		String sql = "UPDATE CCS_JT_FTP SET FTP_DATE=DATE_FORMAT(LAST_DAY(NOW()),'%Y%m%d'),"
				+ "TXT_DATA=CONCAT(DATE_FORMAT(LAST_DAY(NOW()),'%Y%m%d'),'|#|',?),UPDATE_DATE=NOW() WHERE BIZ_CODE='BUS43010'";
		return jdbcTemplate.update(sql, txtData);
	}

	class KeyRowMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int arg1) throws SQLException {
            return rs.getString(1);
        }
    }

    @SuppressWarnings("unchecked")
	public List getTuiFeitType() {
    	List<Map<String, Object>> result = this.getHotPointKeyList(13);
    	TsmStaff staff = this.getLogonStaff();
    	if (StringUtils.startsWith(staff.getLinkId(), "10-182")) {
    		result.addAll(this.getHotPointKeyList(18));//退费方式（南京专用）
    		result = result.stream().sorted//按HOT_ID升序
					((c1, c2) -> MapUtils.getInteger(c1, "HOT_ID") - MapUtils.getInteger(c2, "HOT_ID")).collect(Collectors.toList());
		}
    	return result;
	}

	public List getHotPointKeyList(int key) {
		String strsql = "select * from ccs_hotpoint_key c where c.key_id=? and c.status='0' order by c.hot_id";
		return jdbcTemplate.queryForList(strsql, key);
	}
	
	public List<Map<String, Object>> getSSOZDRL(String ssoId,String staffId) {
		String sql="SELECT T.SSO_ID," + 
				"       T.URL_ADDRESS," + 
				"       T.BROWSER," + 
				"       T.SYS_ID," + 
				"       T.SYS_NAME," + 
				"       T.USER_NAME," + 
				"       T.USER_PWD," + 
				"       T.USER_OTHER," + 
				"       T.SCRIPT_JOB," + 
				"       T.FORM_ID," + 
				"       T.IMAGE_PATH," + 
				"       U.AREA_CENTER," + 
				"       U.SSO_APP_NAME," + 
				"       U.DOMAIN_USER AS STAFF_ID," + 
				"       U.EXTERNAL_USER AS LOGIN_NAME," + 
				"       U.EXTERNAL_USER_PWD AS LOGIN_PWD" + 
				"  FROM SSO_URL_LIST T" + 
				"  LEFT JOIN CCS_ST_SSOCREDENTIALMAPPING U " + 
				"    ON (U.SSO_ID = T.SSO_ID OR U.SSO_ID = T.SYS_ID)" + 
				"   AND U.DOMAIN_USER IN (?) WHERE 1=1     AND T.SSO_ID IN (?)   AND  U.SSO_APP_NAME IS NOT NULL " + 
				"   AND IF(U.ISENABLE=0, 0, 1) = 1 ";
		return jdbcTemplate.queryForList(sql,staffId,ssoId);
	}
	
    /**
	 * 根据省内投诉单ID，查询
	 */
	private String sqlQueryByOrderId=
			"SELECT RELA_GUID,\r\n" + 
			"COMPLAINT_WORKSHEET_ID,\r\n" + 
			"SERVICE_ORDER_ID,\r\n" + 
			"STATU,\r\n" + 
			"ASSIGN_TYPE,\r\n" + 
			"ALARM_DESC,\r\n" + 
			"SEC_FLAG,ASK_SOURCE_SRL\r\n" + 
			"FROM CC_CMP_RELATION\r\n" + 
			"WHERE SERVICE_ORDER_ID = ?";

	/**
	 * 根据省内投诉单ID，查询符合条件的记录
	 * 
	 * @param serviceOrderId
	 *            省内投诉单ID
	 * @return 符合条件的记录列表。元素是表记录对应的对象实例。如果没有查询到记录，返回null。
	 */
	@SuppressWarnings("unchecked")
	public ComplaintRelation queryListByOid(String serviceOrderId) {
		List list = jdbcTemplate.query(sqlQueryByOrderId,
				new Object[] { serviceOrderId }, new ComplaintRelationRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (ComplaintRelation) list.get(0);
	}

	/*
	 * 本地归档集团客服工单
	 */
	public int archiveCmpRelationByOrderId(String orderId) {
		String sql = "UPDATE cc_cmp_relation SET statu=10,alarm_desc='本地归档'WHERE service_order_id=?";
		return jdbcTemplate.update(sql, orderId);
	}

	public String getComplaintOrderDetail(String serviceOrderId) {
		String str = "";
		String sql = "select count(1) count from cc_complaint_order_detail where COMPLAINT_ORDER_SN = ? AND ORDER_STATUS > 0";
		List list = jdbcTemplate.queryForList(sql,serviceOrderId);
		if (!list.isEmpty()) {
			Map map = (Map) list.get(0);
			str = map.get("count").toString();
		}
		return str;
	}
	

    /**
     * @return the systemAuthorization
     */
    public ISystemAuthorization getSystemAuthorization() {
        return systemAuthorization;
    }

    /**
     * @param systemAuthorization
     *            the systemAuthorization to set
     */
    public void setSystemAuthorization(ISystemAuthorization systemAuthorization) {
        this.systemAuthorization = systemAuthorization;
    }

	/**
	 * 获取版本清单
	 */
    public List getVersionList(String system) {
    	String strSql = "select c.VERSION_ID,DATE_FORMAT(c.publish_date,'%Y-%m-%d') PUBLISH_DATE,c.VERSION_CONTENT from cc_version_history c where c.system=? order by c.publish_date desc";
    	return this.jdbcTemplate.queryForList(strSql, system);
    }
    
	/**
	 * 获取最新版本记录
	 */
    public Map getVersionObject(String system) {
    	String strSql = "select r.VERSION_ID,DATE_FORMAT(r.publish_date,'%Y-%m-%d') PUBLISH_DATE,r.VERSION_CONTENT from cc_version_history r where r.system=? order by r.publish_date desc limit 1";
    	return this.jdbcTemplate.queryForMap(strSql, system);
    }

	public Map isShowCallOut(int regionId, int serviceType, int tacheId) {
    	Map map = null;
    	String strSql = "select * from cc_deal_config c where c.region_id=? and c.service_type=? and c.tache_id=?";
    	List list = this.jdbcTemplate.queryForList(strSql, regionId, serviceType, tacheId);
    	if(!list.isEmpty()) {
    		map = (Map) list.get(0);
    	}
    	return map;
    }

	public int getWorkingTime(String beginDate, String orderDate, String endDate, int hangupTimeCount, int serviceType, String sysDate) {
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date bDate = sdf1.parse(beginDate);
			Date eDate = sdf1.parse(endDate.equals("") ? sysDate : endDate);
			long bTime = bDate.getTime();
			long eTime = eDate.getTime();
			log.debug(orderDate + "," + serviceType);
			return (int) ((eTime - bTime) / 1000 - hangupTimeCount);
		} catch (Exception e) {
			e.getMessage();
		}
		return 0;
	}

	public String getWorkingEnd(String beginDate, String orderDate, int dealLimitTime, int hangupTimeCount, int serviceType, String sysDate) {
		try {
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date bDate = sdf1.parse(beginDate);
			long bTime = bDate.getTime();
			log.debug(orderDate + "," + serviceType);
			long eTime = bTime + dealLimitTime * 1000 * 60 * 60 + hangupTimeCount * 1000;
			return sdf1.format(eTime);
		} catch (Exception e) {
			e.getMessage();
		}
		return sysDate;
	}
	
	public Map<String,Object> getAcceptChannel() {
		List<ChannelPojo> channelPojos = new ArrayList<>();
		List<Map<String, Object>> allChannelMap = new ArrayList<>();
		String firstSql = "SELECT REFER_ID,ENTITY_ID,COL_VALUE_NAME FROM PUB_COLUMN_REFERENCE P WHERE 1=1 " +
				"AND P.TABLE_CODE = 'CC_SERVICE_ORDER_ASK' AND P.COL_CODE = 'COME_CATEGORY' AND P.ENTITY_ID = '201112' ORDER BY P.COL_ORDER";
		List<Map<String, Object>> maps = jdbcTemplate.queryForList(firstSql);
		for (Map<String, Object> map : maps) {
			allChannelMap.add(map);
			ChannelPojo channelPojo = new ChannelPojo();
			channelPojo.setValue(String.valueOf(map.get("REFER_ID")));
			channelPojo.setLabel((String)map.get("COL_VALUE_NAME"));

			List<ChannelPojo> channel = new ArrayList<>();
			String secondSql = "SELECT REFER_ID,ENTITY_ID,COL_VALUE_NAME FROM PUB_COLUMN_REFERENCE P WHERE 1=1 " +
					"AND P.TABLE_CODE = 'CC_SERVICE_ORDER_ASK' AND P.COL_CODE = 'ACCEPT_COME_FROM' AND P.ENTITY_ID = ? ORDER BY P.COL_ORDER";
			List<Map<String, Object>> mapTwoList = jdbcTemplate.queryForList(secondSql, map.get("REFER_ID"));
			for (Map<String, Object> mapTwo : mapTwoList) {
				allChannelMap.add(mapTwo);
				ChannelPojo channelPojoTwo = new ChannelPojo();
				channelPojoTwo.setValue(String.valueOf(mapTwo.get("REFER_ID")));
				channelPojoTwo.setLabel((String)mapTwo.get("COL_VALUE_NAME"));
				List<ChannelPojo> channelChildren = new ArrayList<>();
				String threeSql = "SELECT REFER_ID,ENTITY_ID,COL_VALUE_NAME FROM PUB_COLUMN_REFERENCE P WHERE 1=1 " +
						"AND P.TABLE_CODE = 'CC_SERVICE_ORDER_ASK' AND P.COL_CODE = 'ACCEPT_CHANNEL_ID' AND P.ENTITY_ID = ? ORDER BY P.COL_ORDER";
				List<Map<String, Object>> mapThreeList = jdbcTemplate.queryForList(threeSql, mapTwo.get("REFER_ID"));
				for (Map<String, Object> mapThree : mapThreeList) {
					allChannelMap.add(mapThree);
					ChannelPojo channelPojoThree = new ChannelPojo();
					channelPojoThree.setValue(String.valueOf(mapThree.get("REFER_ID")));
					channelPojoThree.setLabel((String)mapThree.get("COL_VALUE_NAME"));
					List<ChannelPojo> channelChildrens = new ArrayList<>();
					String fouSql = "SELECT REFER_ID,ENTITY_ID,COL_VALUE_NAME FROM PUB_COLUMN_REFERENCE P WHERE 1=1 "
							+ "AND P.TABLE_CODE = 'CC_SERVICE_ORDER_ASK' AND P.COL_CODE = 'CHANNEL_DETAIL_ID' AND P.ENTITY_ID = ? ORDER BY P.COL_ORDER";
					List<Map<String, Object>> mapFourList = jdbcTemplate.queryForList(fouSql, mapThree.get("REFER_ID"));
					for (Map<String, Object> mapFour : mapFourList){
						allChannelMap.add(mapFour);
						ChannelPojo channelPojoFour = new ChannelPojo();
						channelPojoFour.setValue(String.valueOf(mapFour.get("REFER_ID")));
						channelPojoFour.setLabel((String)mapFour.get("COL_VALUE_NAME"));
						channelChildrens.add(channelPojoFour);
					}
					channelPojoThree.setChildren(channelChildrens);
					channelChildren.add(channelPojoThree);
				}
				channelPojoTwo.setChildren(channelChildren);
				channel.add(channelPojoTwo);
			}
			channelPojo.setChildren(channel);
			channelPojos.add(channelPojo);
		}
		Map<String,Object> resultMap = new HashMap<>();
		resultMap.put("channelPojos", channelPojos);
		resultMap.put("channelMap", allChannelMap);
		return resultMap;
	}

	// 最末级渠道
	public int getLastChannel(OrderAskInfo askInfo) {
		if (askInfo.getChannelDetailId() > 0) {
			return askInfo.getChannelDetailId();
		}
		return askInfo.getAskChannelId();
	}

	// 越级倾向，这里加100，用来区分全部1
	public int convertUpTendency(ServiceLabel label, OrderAskInfo askInfo) {
		if (null == label) {// 刚受理时，没有ServiceLabel对象
			if (null != askInfo.getEmergency() && !"".equals(askInfo.getEmergency())) {
				return Integer.parseInt(askInfo.getEmergency());
			} else {
				return 100;
			}
		}
		if (null == label.getUpTendencyFlag()) {
			return 100;
		}
		return label.getUpTendencyFlag() + 100;
	}

	public int getLastXX(ServiceContent ct) {
		if (ct.getSixCatalog() > 0) {
			return ct.getSixCatalog();
		}
		if (ct.getFiveCatalog() > 0) {
			return ct.getFiveCatalog();
		}
		if (ct.getFouGradeCatalog() > 0) {
			return ct.getFouGradeCatalog();
		}
		if (ct.getAppealChild() > 0) {
			return ct.getAppealChild();
		}
		return ct.getAppealReasonId();
	}

	public Long getLastYY(TsSheetQualitative qlt) {
		if (qlt.getSixCatalog() > 0) {
			return qlt.getSixCatalog();
		}
		if (qlt.getFiveCatalog() > 0) {
			return qlt.getFiveCatalog();
		}
		if (qlt.getThourCatalog() > 0) {
			return qlt.getThourCatalog();
		}
		return qlt.getThreeCatalog();
	}

	public String getConcatXXDesc(ServiceContent ct) {
		String res = ct.getAppealProdName() + " > " + ct.getAppealReasonDesc();
		if (0 == ct.getAppealChild()) {
			return res;
		}
		res += " > " + ct.getAppealChildDesc();
		if (0 == ct.getFouGradeCatalog()) {
			return res;
		}
		res += " > " + ct.getFouGradeDesc();
		if (0 == ct.getFiveCatalog()) {
			return res;
		}
		res += " > " + ct.getFiveGradeDesc();
		if (0 == ct.getSixCatalog()) {
			return res;
		}
		res += " > " + ct.getSixGradeDesc();
		return res;
	}

	public String getConcatYYDesc(TsSheetQualitative qlt) {
		String res = qlt.getTsKeyWordDesc() + " > " + qlt.getSubKeyWordDesc() + " > " + qlt.getThreeCatalogDesc();
		if (0 == qlt.getThourCatalog()) {
			return res;
		}
		res += " > " + qlt.getThourCatalogDesc();
		if (0 == qlt.getFiveCatalog()) {
			return res;
		}
		res += " > " + qlt.getFiveCatalogDesc();
		if (0 == qlt.getSixCatalog()) {
			return res;
		}
		res += " > " + qlt.getSixCatalogDesc();
		return res;
	}

	public int getSplitIdByIdx(String allStr, int idx) {
		String[] ress = allStr.split("-");
		if (ress.length <= idx) {
			return 0;
		}
		return Integer.parseInt(ress[idx]);
	}

	public String getSplitNameByIdx(String allStr, int idx) {
		String[] ress = allStr.split(" > ");
		if (ress.length <= idx) {
			return "";
		}
		return ress[idx];
	}
	
	public List<Map<String, Object>> getBusinessDirNew(String referId) {
    	String strsql = "SELECT REFER_ID, DIR_TYPE, FIRST_DIR_ID, FIRST_DIR_NAME, SECOND_DIR_ID, SECOND_DIR_NAME, THIRD_DIR_ID, THIRD_DIR_NAME, "
    			+ "LAST_DIR_ID, FULL_DIR_NAME, TEMPLATE_FIELDS FROM cc_business_dir_old2new WHERE REFER_ID = ?";
    	return jdbcTemplate.queryForList(strsql, referId);
	}
	
	public String getFullOrgName(String orgId, String orgName) {
		Map orgMap = this.getOrgMapNew(orgId);
    	if(orgMap == null || orgMap.get("LINKID") == null) {
    		return orgName;
    	}
    	String linkid = orgMap.get("LINKID").toString();
    	
    	if ("10".equals(linkid)) {
			return this.getOrgName(linkid);
		} else {
			String[] orgIds = linkid.split("-");
			StringBuilder orgWater = new StringBuilder();
			for (int i = 1; i < orgIds.length; i++) {
				if (i == orgIds.length - 1) {
					orgWater.append(getOrgName(orgIds[i]));
				} else {
					orgWater.append(getOrgName(orgIds[i]) + "-");
				}
			}
			return orgWater.toString();
		}
	}

	public String getKeyById(String id) {
		String sql = "SELECT T.TEMPLATE_FIELDS FROM CC_SERVICE_DIR_TEMPLATE T WHERE REFER_ID = ? and STATUS = '1'";
		try {
			List list = this.jdbcTemplate.queryForList(sql, id);
	    	if(!list.isEmpty()) {
	    		Map map = (Map) list.get(0);
	    		return map.get("TEMPLATE_FIELDS") == null ? "" : map.get("TEMPLATE_FIELDS").toString();
	    	}
		} catch (Exception e) {
			log.error("数据查询异常 error: {}", e.getMessage(), e);
		}
		return null;
	}
	
	public List getRepeatedBestOrderId(String orderId, String repeatType) {
		String strSql = 
"select c.SERVICE_ORDER_ID,\n" +
"       (SELECT UNIFIED_COMPLAINT_CODE\n" + 
"          FROM cc_cmp_unified_return\n" + 
"         WHERE COMPLAINT_WORKSHEET_ID = c.SERVICE_ORDER_ID\n" + 
"        UNION ALL\n" + 
"        SELECT UNIFIED_COMPLAINT_CODE\n" + 
"          FROM cc_cmp_unified_return_his\n" + 
"         WHERE COMPLAINT_WORKSHEET_ID = c.SERVICE_ORDER_ID LIMIT 1) UNIFIED_COMPLAINT_CODE,\n" + 
"       IFNULL((SELECT 1 FROM cc_service_order_ask d WHERE d.SERVICE_ORDER_ID = c.SERVICE_ORDER_ID LIMIT 1), 2) his_flag\n" + 
"  from cc_service_connection c\n" + 
" where c.CONNECTION_GUID = (select c.CONNECTION_GUID\n" + 
"                              from cc_service_connection c\n" + 
"                             where c.SERVICE_ORDER_ID = ?\n" + 
"                               and c.CONNECTION_TYPE = ?\n" + 
"                               and c.CONNECTION_STATE = 1 LIMIT 1)\n" + 
"   and c.CONNECTION_TYPE = ?\n" + 
"   and c.CONNECTION_STATE = 0\n" + 
" order by c.ACCEPT_DATE desc limit 1";
		return jdbcTemplate.queryForList(strSql, orderId, repeatType, repeatType);
	}
	
	public Map<String, String> getRepeatedOrderInfo(String orderId, String repeatType) {
		List list = getRepeatedBestOrderId(orderId, repeatType);//获取前期重复工单
		if(list.isEmpty()) {
			return Collections.emptyMap();
		}
		StringBuilder sb = new StringBuilder();
		Map<String, String> resultMap = new HashMap<>();
		String repeatedId = getStringByKey(((Map) list.get(0)), "SERVICE_ORDER_ID");//获取前期重复工单
		sb.append("最严工单号：").append(repeatedId).append("\n");
		String ucc = getStringByKey(((Map) list.get(0)), "UNIFIED_COMPLAINT_CODE");//集团统一编码
		sb.append("集团编码：").append(ucc).append("\n");
		String hisflagStr = getStringByKey(((Map) list.get(0)), "HIS_FLAG");//1、当前，2、历史
		boolean hisFlag = "2".equals(hisflagStr);
		List tmpList = new ArrayList();
		if(hisFlag) {
			tmpList = sheetQualitative.getOrderQualitativeHis(repeatedId);//查询历史单
		} else {
			tmpList = sheetQualitative.getOrderQualitative(repeatedId);//查询当前单
		}
		if(tmpList.isEmpty()) {
			resultMap.put("CONTROL_AREA_FIR_DESC", "");
			resultMap.put("DUTY_ORG_NAME", "");
		} else {
			Map qMap = (Map) tmpList.get(tmpList.size() - 1);
			String controlAreaFir = qMap.get("CONTROL_AREA_FIR").toString();
			String controlAreaFirDesc = "707907132".equals(controlAreaFir) ? "企业无责" : "企业有责";
			String dutyOrgName = this.getStringByKey(qMap, "DUTY_ORG_NAME");
			String dutyOrgThirdName = this.getStringByKey(qMap, "DUTY_ORG_THIRD_DESC");
			dutyOrgName = dutyOrgName + ("".equals(dutyOrgThirdName) ? "" : "-" + dutyOrgThirdName);
			resultMap.put("CONTROL_AREA_FIR_DESC", controlAreaFirDesc);
			resultMap.put("DUTY_ORG_NAME", dutyOrgName);
		}
		if(hisFlag) {
			sb.append("处理结果：").append(getLastDealContentHis(repeatedId));
		} else {
			Map lastDealInfo = this.getLastDealInfo(repeatedId);
			if(lastDealInfo != null) {
				sb.append("处理结果：").append(getStringByKey(lastDealInfo, "DEAL_CONTENT"));
			}
		}
		resultMap.put("LAST_DEAL_CONTENT", sb.toString());
		return resultMap;
	}
	
	public Map<String, Object> getDeductions(String orderId) {
    	Map<String,Object> deductionsInfo = new HashMap<>();
		String strsql = "select o.DEDUCTIONS_ID deductionsId, o.DEDUCTIONS_NAME deductionsName " +
				"from cc_sheet_outlets o where o.SERVICE_ORDER_ID = ?";
		List<Map<String, Object>> maps = jdbcTemplate.queryForList(strsql, orderId);
		if (!maps.isEmpty()){
			deductionsInfo = maps.get(0);
		}
		return deductionsInfo;
	}
	
	public void saveOrderOperation(String orderId, int operType) {
		int num = 0;
		try {
			int staffId = 0;
			String staffName = null;
			if(this.isLogonFlag()) {
				TsmStaff staff = this.getLogonStaff();
				staffId = Integer.parseInt(staff.getId());
				staffName = staff.getName();
			}
			String sql = "INSERT INTO cc_order_operation(SERVICE_ORDER_ID, OPER_STAFF, STAFF_NAME, OPER_TYPE, CREATE_DATE) "
					+ "VALUES (?, ?, ?, ?, now())";
			num = jdbcTemplate.update(sql, orderId, staffId, staffName, operType);
		}
		catch(Exception e) {
			log.error("saveOrderOperation error: {}", e.getMessage(), e);
		}
		log.info("saveOrderOperation result: {}", num);
	}
	
	private static final String QUERY_COMPLETION_REASON = "SELECT A.ID,A.N,A.N_ID,A.F_N, A.IS_CHANNEL,A.V_NUM FROM CCS_ST_MAPPING_ALL_B_NEW A WHERE 1 = 1 ";
	
	/**
	 * 根据办结原因ID查找办结原因
	 */
	public String getCompletionReason(String queryN, String queryNID,String queryID) {
		String strsql = QUERY_COMPLETION_REASON;
		List<String> argList = new ArrayList<>();
		if ( com.transfar.common.utils.StringUtils.isNotEmpty(queryN) ) {
			strsql += " AND A.N like concat('%',?,'%')";
	        argList.add(queryN);
		}
		if ( com.transfar.common.utils.StringUtils.isNotEmpty(queryNID) ) {
			strsql += " AND A.N_ID like concat('%',?,'%')";
			argList.add(queryNID);
		}
		if ( com.transfar.common.utils.StringUtils.isNotEmpty(queryID) ) {
			strsql += " AND A.ID like concat('%',?,'%')";
			argList.add(queryID);
		}
		strsql += " ORDER BY A.N DESC";
		Object[] args = argList.toArray();
		List<Map<String,Object>> list = jdbcTemplate.queryForList(strsql, args);

		if(!list.isEmpty()) {
			JSONArray array = new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				Map<String,Object> temp = list.get(i);
				JSONObject item = new JSONObject();
				item.put("N", temp.get("N"));
				item.put("N_ID", temp.get("N_ID"));
				item.put("ID", temp.get("ID"));
				item.put("F_N", temp.get("F_N"));
				item.put("IS_CHANNEL", temp.get("IS_CHANNEL"));
				item.put("V_NUM", temp.get("V_NUM"));
				array.add(item);
				item.clear();
			}
			JSONObject json = new JSONObject();
			json.put("array", array);
			return json.toString();
		}
		return "";
	}

	public int addCompletionReason(String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String querySql = "SELECT 1 FROM CCS_ST_MAPPING_ALL_B_NEW WHERE N_ID=?";
		List<Map<String,Object>> list = jdbcTemplate.queryForList(querySql,json.optString("N_ID"));
		if(!list.isEmpty()){
			return 101;
		}
		String sql = "INSERT INTO CCS_ST_MAPPING_ALL_B_NEW (N, N_ID, ID, F_N, IS_CHANNEL, V_NUM) VALUES (?, ?, ?, ?, ?, ?)";
		try {
			return this.jdbcTemplate.update(sql, json.optString("N"), json.optString("N_ID"),
					json.optString("ID"), json.optString("F_N"),json.optString("IS_CHANNEL"), json.optString("V_NUM"));
		} catch (Exception e) {
			e.printStackTrace();
			return 100;
		}
	}

	public int updateCompletionReason(String parm){
		JSONObject json = JSONObject.fromObject(parm);
		String sql = "UPDATE CCS_ST_MAPPING_ALL_B_NEW SET N=?, ID=?, F_N=?, IS_CHANNEL=?, V_NUM=? WHERE N_ID=? ";
		try {
			return this.jdbcTemplate.update(sql, json.optString("N"), json.optString("ID"), json.optString("F_N"),
					json.optString("IS_CHANNEL"), json.optString("V_NUM"), json.optString("N_ID"));
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	public int delCompletionReason(String id){
		String sql = "DELETE FROM CCS_ST_MAPPING_ALL_B_NEW WHERE N_ID=? ";
		try {
			return this.jdbcTemplate.update(sql, id);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	public int judgeRoleByMenuId(String menuId) {
		TsmStaff staff = this.getLogonStaff();
		String sql = "select f.menu_id, f.menu_name, b.logonname, b.staffname "
				+ "from tsm_staff_role_rela a, tsm_staff b, tsm_role_func_rela d, tsm_mainmenu f "
				+ "where a.staff_id = b.staff_id "
				+ "and a.role_id = d.role_id "
				+ "and d.func_id = f.menu_id "
				+ "and b.logonname = ? "
				+ "and f.menu_id = ?";
		
		List list = jdbcTemplate.queryForList(sql, staff.getLogonName(), menuId);
		return list.size();
	}

	// 判断入参部门是否属于特殊部门，orgType：1、投诉流程改变，不经过预定性环节，2、协查派单显示违约金金额，3、审批回流
	public boolean checkSpecialOrg(String orgId, int orgType) {
		String sql = "SELECT COUNT(1) FROM cc_flow_org WHERE org_type = ? AND org_id = ?";
		if (jdbcTemplate.queryForObject(sql, new Object[] { orgType, orgId }, Integer.class) > 0) {
			return true;
		}
		return false;
	}
	
	public String getConfigMapValue(String key) {
		String sql = "select c.VALUE1 from cc_config_map c where c.KEY1 = ?";
		List valList = this.jdbcTemplate.queryForList(sql, key);
		String value = "";
		if(!valList.isEmpty()){
			value = ((Map)valList.get(0)).get("VALUE1").toString();
		}
		return value;
	}
	
	/**
	 * 保存操作记录
	 */
    public int saveOperationRecord(String uuid, String orderId, String sheetId, String prodNum, String opAction, String opMenu) {
    	int num = 0;
    	try {
    		TsmStaff staff = this.getLogonStaff();
    		String sql = "INSERT INTO cc_operation_record(GUID,SERVICE_ORDER_ID,WORK_SHEET_ID,PROD_NUM,OP_START_DATE,OP_ACTION,OP_MENU,"
    				+ "OP_STAFF_ID,OP_LOGONNAME,OP_STAFF_NAME,OP_ORG_ID,OP_ORG_NAME) "
    				+ "VALUES(?,?,?,?,now(),?,?,?,?,?,?,?)";
    		num = jdbcTemplate.update(sql, uuid, orderId, sheetId, prodNum, opAction, opMenu, 
    				staff.getId(), staff.getLogonName(), staff.getName(), staff.getOrganizationId(), staff.getOrgName());
    	} catch (Exception e) {
    		log.error("saveOperationRecord error: {}", e.getMessage(), e);
		}
    	log.info("saveOperationLog orderId: {} uuid: {} result: {}", orderId, uuid, num);
    	return num;
	}

	/**
	 * 根据servOrderId更新日志表中时间最新的数据
	 * */
	public int upSummaryCallbackLog(String servOrderId,String sheetId){
		String qrySql = "SELECT USE_DATE FROM CS_SUMMARY_CALLBACK_LOG C WHERE C.SERVICE_ORDER_ID = ? AND C.ORDER_ID = ? ORDER BY CALLBACK_DATE DESC LIMIT 1";
		String upSql = "UPDATE CS_SUMMARY_CALLBACK_LOG SET IS_BACKFILL = 1,USE_DATE = NOW() WHERE ORDER_ID = ? AND SERVICE_ORDER_ID = ? AND USE_DATE IS NULL ORDER BY CALLBACK_DATE DESC LIMIT 1";
		int result = 0;
		try{
			String useDate = this.jdbcTemplate.queryForObject(qrySql, new Object[]{servOrderId, sheetId}, String.class);
			if(StringUtils.isBlank(useDate)){//useDate为空更新数据
				result = this.jdbcTemplate.update(upSql, sheetId,servOrderId);
			}
		}catch (Exception e){
			log.error("upSummaryCallbackLog error: {}",e.getMessage(),e);
		}
		return result;
	}

	/**
	 * 根据hasFetchedData获取小结内容
	 * */
	public String qrySummaryCallbackLog(String servOrderId,String sheetId,String uuid){
		String summary = "";
		String dataType = "";
		try{
			String s = redisUtils.get("summaryQueryId" + uuid, RedisType.WORKSHEET);
			if(StringUtils.isNotBlank(s)){
				String s1 = redisUtils.get("summary_" + sheetId+servOrderId, RedisType.WORKSHEET);
				if(StringUtils.isNotBlank(s1)){
					com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(s1);
					jsonObject.put("hasFetchedData","2");
					summary = jsonObject.toJSONString();
				}
				dataType = "redis";
				log.info("qrySummaryCallbackLog redis value: {}","summary_" + sheetId+servOrderId);
			}else {
				redisUtils.setex("summaryQueryId"+uuid,3600,uuid, RedisType.WORKSHEET);
				String sql = "SELECT USER_COMPLAINT_ISSUE AS '用户投诉问题',VERIFICATION_STATUS AS '核查情况',HANDLING_RESULT AS '处理结果'," +
						"USER_APPROVAL_OF_SOLUTION AS '用户是否认可处理方案',ESCALATION_TENDENCY AS '是否存在向集团/媒体/政府监管部门越级的倾向'," +
						"CONTACT_PLAN AS '下一步与客户的联系计划'  FROM CS_SUMMARY_CALLBACK_LOG WHERE SERVICE_ORDER_ID = ? AND ORDER_ID = ? " +
						"ORDER BY CALLBACK_DATE DESC LIMIT 1";
				List<Map<String, Object>> list = this.jdbcTemplate.queryForList(sql,servOrderId,sheetId);
				Map<String, Object> map = null;
				if(!list.isEmpty()){
					map = list.get(0);
				}
				if(map!=null){
					map.put("hasFetchedData","1");
					summary = JSON.toJSONString(map);
				}
				dataType = "database";
			}
		}catch (Exception e){
			log.error("qrySummaryCallbackLog error: {}",e.getMessage(),e);
		}
		log.info("qrySummaryCallbackLog servOrderId: {} DataType: {}",servOrderId,dataType);
		return summary;
	}
    /**
     * 更新操作结束时间
     */
    public int updateOperationRecord(String uuid, String orderId) {
    	int num = 0;
    	try {
    		String sql = "update cc_operation_record set OP_END_DATE = now() where GUID = ? and SERVICE_ORDER_ID = ?";
    		num = jdbcTemplate.update(sql, uuid, orderId);
    	} catch (Exception e) {
    		log.error("updateOperationRecord error: {}", e.getMessage(), e);
		}
    	log.info("updateOperationRecord orderId: {} uuid: {} result: {}", orderId, uuid, num);
    	return num;
	}

	// 查询登录员工所属分公司列表，省投为全省
	public List getCompanyByLogonStaff() {
		StringBuilder sql = new StringBuilder("SELECT org_id,org_name FROM tsm_organization");
		sql.append(" WHERE org_id IN('182','284','285','286','287','288','289','290','291','292','293','294','97')");// 各个分公司
		TsmStaff staff = getLogonStaff();
		if (staff.getLinkId().startsWith("10-11-361143")) {// 如果是省投员工就查看全部
			return jdbcTemplate.queryForList(sql.toString());
		} else {// 分公司员工查看自己所属分公司
			sql.append("AND org_id IN(SELECT z.org_id FROM tsm_staff x,tsm_organization y,tsm_organization z WHERE x.org_id=y.org_id AND");
			sql.append(" IF(SUBSTRING_INDEX(y.linkid,'-',2)='10-11',SUBSTRING_INDEX(y.linkid,'-',3),SUBSTRING_INDEX(y.linkid,'-',2))=z.linkid");
			sql.append(" AND x.staff_id=?)");
			return jdbcTemplate.queryForList(sql.toString(), staff.getId());
		}
	}
	
	/**
	 * 判断是否符合场景
	 * @param thirdLevel 分类码三级
	 * @return
	 */
	public boolean judgeThirdLevel(String thirdLevel) {
		if(StringUtils.equals("无法停机或销号", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("强制增加业务", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("业务无法退订", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("限制用户更改套餐", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("携出方阻挠携出", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("用户提前解约", StringUtils.trim(thirdLevel))) {// 分类码三级
			return true;
		}
		
		if(StringUtils.equals("隐瞒模糊夸大宣传", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("擅自改变套餐收费", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("错收多收乱收费", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("套餐到期后争议", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("超套餐费用", StringUtils.trim(thirdLevel))) {// 分类码三级
			return true;
		}
		
		if(StringUtils.equals("最低消费", StringUtils.trim(thirdLevel))) {// 分类码三级
			return true;
		}
		
		if(StringUtils.equals("非本人号码争议", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("资费水平", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("不明增值业务", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("非套餐类业务办理", StringUtils.trim(thirdLevel))
				|| StringUtils.equals("无法办理过户", StringUtils.trim(thirdLevel))) {// 分类码三级
			return true;
		}
		
		if(StringUtils.equals("业务被关停", StringUtils.trim(thirdLevel))) {// 分类码三级
			return true;
		}
		
		if(StringUtils.equals("号码被回收", StringUtils.trim(thirdLevel))) {// 分类码三级
			return true;
		}
		
		if(StringUtils.equals("流量清零与扣费顺序", StringUtils.trim(thirdLevel))) {// 分类码三级
			return true;
		}
		return false;
	}

	public List querySensitiveData(String staffId){
		log.info("querySensitiveData staffId: {}",staffId);
		List<Map<String,Object>> list = null;
		try{
			String sql = "SELECT W.SERVICE_ORDER_ID, W.WORK_SHEET_ID, DATE_FORMAT(W.CREAT_DATE,'%Y-%m-%d %H:%i:%s') CREAT_DATE,L.SENSITIVE_NUM" +
					" FROM CC_WORK_SHEET W, CC_SERVICE_LABEL L " +
					"WHERE 1 = 1 AND W.SERVICE_ORDER_ID = L.SERVICE_ORDER_ID AND W.SERVICE_TYPE = 720130000" +
					" AND W.LOCK_FLAG = 1 AND L.SENSITIVE_NUM > 0 AND W.DEAL_STAFF = ?" +
					"ORDER BY W.CREAT_DATE";
			list = this.jdbcTemplate.queryForList(sql, staffId);
		}catch (Exception e){
			log.error("querySensitiveData error: {}",e.getMessage(),e);
		}
		return list;
	}
	
	public boolean isAutoAuditSheet(String sheetId) {
		List<Map<String,Object>> list = null;
		try{
			String sql = "SELECT \n"
					+ " C.TRACK_ORDER_ID,\n"
					+ " C.AUTO_STATUS\n"
					+ "FROM\n"
					+ " CC_ORDER_REFUND C, CC_WORK_SHEET W\n"
					+ "WHERE\n"
					+ " 1 = 1\n"
					+ " AND C.TRACK_ORDER_ID = W.SERVICE_ORDER_ID\n"
					+ " AND W.TACHE_ID = 700000085\n"
					+ " AND W.SHEET_TYPE = 700000126\n"
					+ " AND W.MAIN_SHEET_FLAG = 0\n"
					+ " AND (W.LOCK_FLAG = 0 AND W.SHEET_STATU = 700000046)\n"
					+ " AND C.AUTO_STATUS = 1\n"
					+ " AND C.ORDER_STATUS = 0\n"
					+ " AND W.WORK_SHEET_ID = ?";
			list = this.jdbcTemplate.queryForList(sql, sheetId);
		}catch (Exception e){
			log.error("isAutoAuditSheet error: {}",e.getMessage(),e);
		}
		log.info("isAutoAuditSheet: {}", JSON.toJSON(list));
		if(list == null || list.isEmpty()) {
			return false;
		}
		return true;
	}
	
	public boolean isAutoFinishSheet(String sheetId) {
		List<Map<String,Object>> list = null;
		try{
			String sql = "SELECT \n"
					+ " C.TRACK_ORDER_ID,\n"
					+ " C.RECHAG_STATUS\n"
					+ "FROM\n"
					+ " CC_ORDER_REFUND C, CC_WORK_SHEET W, CC_SERVICE_CONTENT_SAVE S \n"
					+ " LEFT JOIN CC_SERVICE_CONTENT_SAVE A ON (S.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID AND A.ELEMENT_ID = 'd86fc07758818171bb2b48ff64b4dec2')\n"
					+ "WHERE\n"
					+ " 1 = 1\n"
					+ " AND (A.ANSWER_ID IS NULL OR A.ANSWER_ID = 'radio_002')\n"
					+ " AND C.TRACK_ORDER_ID = S.SERVICE_ORDER_ID\n"
					+ " AND S.ELEMENT_ID = '79e7a4b3ae2009b277285e0c8d13dd80' AND S.ANSWER_ID = 'radio_0052'\n"
					+ " AND C.TRACK_ORDER_ID = W.SERVICE_ORDER_ID\n"
					+ " AND W.TACHE_ID = 700000085\n"
					+ " AND W.SHEET_TYPE = 700000126\n"
					+ " AND W.MAIN_SHEET_FLAG = 0\n"
					+ " AND (W.LOCK_FLAG = 0 AND W.SHEET_STATU = 700000046)\n"
					+ " AND C.REFUND_STATUS = 3\n"
					+ " AND C.RECHAG_STATUS = 2\n"
					+ " AND C.ORDER_STATUS = 0\n"
					+ " AND W.WORK_SHEET_ID = ?\n"
					+ "UNION\n"
					+ "SELECT \n"
					+ " C.TRACK_ORDER_ID,\n"
					+ " C.RECHAG_STATUS\n"
					+ "FROM\n"
					+ " CC_ORDER_REFUND C, CC_WORK_SHEET W, CC_SERVICE_CONTENT_SAVE S\n"
					+ " LEFT JOIN CC_SERVICE_CONTENT_SAVE A ON (S.SERVICE_ORDER_ID = A.SERVICE_ORDER_ID AND A.ELEMENT_ID = 'd86fc07758818171bb2b48ff64b4dec2')\n"
					+ "WHERE\n"
					+ " 1 = 1\n"
					+ " AND (A.ANSWER_ID IS NULL OR A.ANSWER_ID = 'radio_002')\n"
					+ " AND C.TRACK_ORDER_ID = S.SERVICE_ORDER_ID\n"
					+ " AND S.ELEMENT_ID = '79e7a4b3ae2009b277285e0c8d13dd80' AND S.ANSWER_ID = 'radio_0052'\n"
					+ " AND C.TRACK_ORDER_ID = W.SERVICE_ORDER_ID\n"
					+ " AND W.TACHE_ID = 700000085\n"
					+ " AND W.SHEET_TYPE = 700000126\n"
					+ " AND W.MAIN_SHEET_FLAG = 0\n"
					+ " AND (W.LOCK_FLAG = 0 AND W.SHEET_STATU = 700000046)\n"
					+ " AND C.REFUND_STATUS = 3\n"
					+ " AND C.RECHAG_STATUS = 5\n"
					+ " AND C.ORDER_STATUS = 0\n"
					+ " AND W.WORK_SHEET_ID = ?";
			list = this.jdbcTemplate.queryForList(sql, sheetId, sheetId);
		}catch (Exception e){
			log.error("isAutoFinishSheet error: {}",e.getMessage(),e);
		}
		log.info("isAutoFinishSheet: {}", JSON.toJSON(list));
		if(list == null || list.isEmpty()) {
			return false;
		}
		return true;
	}

	//根据受理单号查询申诉报告页面保存的销售品信息
	public List getOfferDetailByOrderId(String orderId) {
		String sql = "SELECT offer_id,offer_name FROM cc_complaint_offer_detail WHERE SERVICE_ORDER_ID=? GROUP BY OFFER_ID ORDER BY SYS_DATE DESC,OFFER_ID";
		return jdbcTemplate.queryForList(sql, orderId);
	}

	//根据销售品ID查询销售品级别，默认省内集约
	public String getOfferGradeByOfferId(String offerId) {
		String sql = "SELECT offer_grade FROM pub_offer_grade WHERE offer_id=?";
		List list = jdbcTemplate.queryForList(sql, offerId);
		if (list.isEmpty()) {// 销售品等级字段默认省内集约
			return "B类";
		} else {
			Map map = (Map) list.get(0);
			return map.get("OFFER_GRADE").toString();
		}
	}
}