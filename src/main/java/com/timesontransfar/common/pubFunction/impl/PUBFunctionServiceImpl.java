package com.timesontransfar.common.pubFunction.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.common.framework.core.dynamicdisplay.model.pojo.PubQueryData;
import com.timesontransfar.common.framework.core.dynamicdisplay.service.IWebDynamicDisplay;
import com.timesontransfar.common.pubFunction.IPUBFunctionService;
import com.timesontransfar.common.utils.DataUtils;
import com.timesontransfar.customservice.common.DESEncryptUtil;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.conditions.GridDateConditions;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.service.ServiceOrderQuery;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.tablePage.PageResVO;
import com.transfar.common.utils.StringUtils;
import com.util.date.DateHelper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@SuppressWarnings("rawtypes")
@Component(value="pubFunction")
public class PUBFunctionServiceImpl implements IPUBFunctionService {
	private static Logger log = LoggerFactory.getLogger(PUBFunctionServiceImpl.class);
	
	@Autowired
	private IWebDynamicDisplay webDynamicDisplay;
	@Autowired
	private GridDateConditions gridDateConditions;
	@Autowired
	private JdbcTemplate jt;
	@Autowired
    private ISystemAuthorization systemAuthorization;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private  CustomerServiceFeign customerServiceFeign;
    @Autowired
	private DataUtils dataUtil;
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;
    @Autowired
	private ServiceOrderQuery orderQury;
	
	@Override
	@SuppressWarnings({ "unchecked" })
	public PageResVO qryData(String funcId, Map inParams) {
		int currentPage = Integer.parseInt(inParams.get("currentPage").toString());
		if(currentPage > 1) {
			String position = String.valueOf(currentPage - 1) ;
			inParams.put("WEB_DYNAMICDISPLAY_POSITION",position);
			inParams.put("WEB_DYNAMICDISPLAY_OPERATION","PREV");
		}
		
		String strWhere=inParams.get("strWhere")==null?"":inParams.get("strWhere").toString();
		String conditions=gridDateConditions.getStrWhere(funcId, strWhere);
		
		if(StringUtils.isNotEmpty(conditions)){
			inParams.put("WEB_ADDITIONALCONDITION", conditions);
		}
		PubQueryData ls = webDynamicDisplay.getData(funcId, inParams);
		log.info("总记录：{}", ls.getTotalCount());
		JSONArray returnList = change2List(ls);
		
		PageResVO vo = new PageResVO();
		vo.setResultList(returnList);
		vo.setTotalCount(ls.getTotalCount());
		return vo;
	}
	
	private JSONArray change2List(PubQueryData data) {
		Map<String,String> km = jsonObjectToMap(data.getKeyMap());
		JSONArray arr = JSONArray.fromObject(data.getResultData());
		JSONArray ls = new JSONArray();
		for(int i=0;i<arr.size();i++) {
			JSONObject o = new JSONObject();
			JSONArray tmp = arr.optJSONArray(i);
			for(int j=0;j<tmp.size();j++) {
				String k = km.get(String.valueOf(j));
				String v = "";
				if(k.toLowerCase().contains("date")) {
					String newTemp=tmp.get(j)==null?"":tmp.get(j).toString();
					if(StringUtils.isNotEmpty(newTemp) && !"null".contentEquals(newTemp)) {
						String time = JSONObject.fromObject(tmp.get(j).toString()).optString("time");
						v =  DateHelper.parseLongTime(time);
					}
					
				} else {
					v = tmp.get(j).equals("null") ? "" : tmp.get(j).toString();
				}
				o.put(k,v);
			}
			ls.add(o);
		}
		return ls;
	}
	
	private Map<String, String> jsonObjectToMap(Map m){
		HashMap<String, String> data = new HashMap<String, String>();  
		Iterator it = m.entrySet().iterator();
		while(it.hasNext()){
			Map.Entry entry = (Map.Entry)it.next();
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			data.put(value , key);
		}
		return data;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map addOrderStr(Map param) {
		String hisFlg=param.get("hisFlg").toString();
		String tempSheettab="CC_WORK_SHEET";
		String tempOrderTab="CC_SERVICE_ORDER_ASK";
		String tempcontenttab="CC_SERVICE_CONTENT_ASK";
		//历史
		if("1".equals(hisFlg)) {
			tempSheettab="CC_WORK_SHEET_HIS";
			tempOrderTab="CC_SERVICE_ORDER_ASK_HIS";
			tempcontenttab="CC_SERVICE_CONTENT_ASK_HIS";
		}
		String whereStr="";
		String proNum=param.get("proNum").toString();
		String sheetId=param.get("sheetId").toString();
		String appealProdId=param.get("select_appealProdId").toString();
		String appealReasonId=param.get("select_appealReasonId").toString();
		String returnOrgId=param.get("returnOrgId").toString();
		String regionid=param.get("regionid").toString();
		String custEmotionDesc=param.get("cust_emotion_desc").toString();
		String telNo=param.get("telNo").toString();
		String subComeFrom=param.get("select_subComeFrom").toString();
		String comeFrom=param.get("select_comeFrom").toString();
		String askChannelId=param.get("select_askChannelId").toString();
		String tachid=param.get("select_tachid").toString();
		String serviceType=param.get("select_serviceType").toString();
		String homeSheet=param.get("select_homeSheet").toString(); 
		String sheetStatu=param.get("select_orderStatu").toString(); 
		String rcvOrgId=param.get("rcvOrgId").toString(); 
		String dealOrgId=param.get("dealOrgId").toString();
		String startTime=param.get("startTime").toString();
		String endTime=param.get("endTime").toString();
		
		if("0".equals(hisFlg) && "".equals(proNum) && "".equals(sheetId)) {//产品号 工单号
			whereStr +=" ( "+tempOrderTab+".SERVICE_TYPE IN (SELECT P.REFER_ID FROM PUB_COLUMN_REFERENCE P where 1 = 1 AND P.TABLE_CODE = '"+tempOrderTab+"' AND P.COL_CODE = 'SERVICE_TYPE' AND P.REFER_ID <> '600000074') OR " + 
			"("+tempOrderTab+".SERVICE_TYPE = '600000074' AND "+tempOrderTab+".ORDER_STATU IN ('700000099', '700000101')))";
		}
		if(!"0".equals(appealProdId)) { //一级目录
			whereStr +=getAnd(whereStr)+tempcontenttab+".APPEAL_PROD_ID="+appealProdId;
		}
		if(!"0".equals(appealReasonId)) {//二级目录
			whereStr +=getAnd(whereStr)+tempcontenttab+".APPEAL_REASON_ID="+appealReasonId;
		}
	    if(!"".equals(returnOrgId)){//派发部门
	    	whereStr +=getAnd(whereStr)+tempSheettab+".RETURN_ORG_ID='"+returnOrgId+"'";
	    }
	    if(!"0".equals(regionid)){//地域
	    	whereStr +=getAnd(whereStr)+regionid;
	    }
	    if(!"".equals(custEmotionDesc)){//被投诉号码
	    	whereStr +=getAnd(whereStr)+tempOrderTab+".CUST_EMOTION_DESC='"+custEmotionDesc+"'";   
	    }
	    if(!"0".equals(telNo)){//区号
	    	whereStr +=getAnd(whereStr)+tempOrderTab+".REGION_ID="+telNo;   
	    }
	    if(!"0".equals(subComeFrom)){//投诉级别
	    	whereStr +=getAnd(whereStr)+tempOrderTab+".COME_CATEGORY="+subComeFrom;   
	    }
	    if(!"0".equals(comeFrom)){//受理来源
	    	whereStr +=getAnd(whereStr)+tempOrderTab+".ACCEPT_COME_FROM="+comeFrom;   
	    }
	    if(!"0".equals(askChannelId)){//受理渠道
	    	whereStr +=getAnd(whereStr)+tempOrderTab+".ACCEPT_CHANNEL_ID="+askChannelId;   
	    }
	    if(!"0".equals(tachid)){//所处环节
	    	whereStr +=getAnd(whereStr)+tempSheettab+".TACHE_ID="+tachid;   
	    }
	    if(!"".equals(sheetId)){//工单号
	    	whereStr +=getAnd(whereStr)+tempSheettab+".WORK_SHEET_ID='"+sheetId+"'"; 
	    }
	    if(!"".equals(proNum)){//产品号
	    	whereStr +=getAnd(whereStr)+"DECODE(SUBSTR("+tempOrderTab+".PROD_NUM, 1, 1), '0', "+tempOrderTab+".PROD_NUM, '0' || "+tempOrderTab+".PROD_NUM) = '" + proNum + "'";
	    }
	    if(!"0".equals(serviceType)){//性质类别
	    	whereStr +=getAnd(whereStr)+tempOrderTab+".SERVICE_TYPE="+serviceType;
	    } 
	    if(!"0".equals(homeSheet)){//工单所属
	    	whereStr +=getAnd(whereStr)+tempSheettab+".HOME_SHEET="+homeSheet;
	    } 
	    if(!"0".equals(sheetStatu)){//工单状态
	    	whereStr +=getAnd(whereStr)+" sheetStatu.SHEET_STATU="+sheetStatu;
	    } 
	    if(!"".equals(rcvOrgId)){//收单部门id
	    	whereStr +=getAnd(whereStr)+tempSheettab+".RECEIVE_ORG_ID='"+rcvOrgId+"'";
	    } 
	    if(!"".equals(dealOrgId)){//处理部门id
	    	whereStr +=getAnd(whereStr)+tempSheettab+".DEAL_ORG_ID='"+dealOrgId+"'";
	    } 
	    whereStr +=getAnd(whereStr)+tempSheettab+".CREAT_DATE BETWEEN TO_DATE('"+startTime+"','YYYY-MM-DD HH24:MI:SS')";
	    whereStr +=getAnd(whereStr)+" TO_DATE('"+endTime+"','YYYY-MM-DD HH24:MI:SS')";
	    whereStr="".equals(whereStr) ? "1=1" :" 1=1 AND( "+whereStr+" )";
	    param.put("WEB_ADDITIONALCONDITION", whereStr);
		return param;
	}
	
	public String getAnd(String in) {
		
		in="".equals(in) ? " ": " AND ";
		return in;
	}

	@Override
	public String addAccpetStr(Map param, String funcId) {
		//默认查当前，funcId==WF500021977就查历史
		String tempcusttab="CC_ORDER_CUST_INFO";
		String tempordertab="CC_SERVICE_ORDER_ASK";
		String tempcontenttab="CC_SERVICE_CONTENT_ASK";
		String tempcmpunified="CC_CMP_UNIFIED_RETURN";
		if("WF500021977".contentEquals(funcId)){
			tempcusttab="CC_ORDER_CUST_INFO_HIS";
			tempordertab="CC_SERVICE_ORDER_ASK_HIS";
			tempcontenttab="CC_SERVICE_CONTENT_ASK_HIS";
			tempcmpunified="CC_CMP_UNIFIED_RETURN_HIS";
		}
		String currentPage=param.get("currentPage").toString();
		int current=Integer.parseInt(currentPage);
		String where=" limit 0, 10";
		if(current>1){
			int minNum=(current-1)*10;
			int pageSize=10;
			where=" limit "+minNum+", "+pageSize;
		}
		String webCondition="";

		boolean authFlag = false;//是否通过权限查询
		if (!"".contentEquals(param.get("curType").toString())) {
			TsmStaff ts = pubFunc.getLogonStaff();
			String curOrder = orderAskInfoDao.queryCurOrder(ts.getId(), Integer.parseInt(param.get("curType").toString()));
			curOrder = curOrder.replace(",", "','");
			curOrder = "('" + curOrder + "')";
			webCondition += getAnd(webCondition) + " " + tempordertab + ".SERVICE_ORDER_ID IN " + curOrder;
		} else {
		//地域	
		if(!"".contentEquals(param.get("regionId").toString())){
			webCondition += getAnd(webCondition)+" "+tempordertab+".REGION_ID="+param.get("regionId").toString();
		}
		if(!"".contentEquals(param.get("trAppoid").toString())){//一级目录
			webCondition += getAnd(webCondition)+" "+tempcontenttab+".APPEAL_PROD_ID="+param.get("trAppoid").toString();
		}
		if(!"".contentEquals(param.get("trReasonId").toString())){//二级目录
			webCondition += getAnd(webCondition)+" "+tempcontenttab+".APPEAL_REASON_ID="+param.get("trReasonId").toString();
		}
		if(!"".contentEquals(param.get("trAskSour").toString())){//投诉级别
			webCondition += getAnd(webCondition)+" "+tempordertab+".COME_CATEGORY="+param.get("trAskSour").toString();
		}
		if(!"".contentEquals(param.get("trSubFrom").toString())){//受理来源
			webCondition += getAnd(webCondition)+" "+tempordertab+".ACCEPT_COME_FROM="+param.get("trSubFrom").toString();
		}
		if(!"".contentEquals(param.get("trChannel").toString())){//受理渠道
			webCondition += getAnd(webCondition)+" "+tempordertab+".ACCEPT_CHANNEL_ID="+param.get("trChannel").toString();
		}
		if(!"".contentEquals(param.get("trSerType").toString())){//服务类型
			webCondition += getAnd(webCondition)+" "+tempordertab+".SERVICE_TYPE="+param.get("trSerType").toString();
		}
		if(!"".contentEquals(param.get("trServTypeDt").toString())){//类型细项
			webCondition += getAnd(webCondition)+" "+tempcontenttab+".SERVICE_TYPE_DETAIL='"+param.get("trServTypeDt").toString()+"'";
		}
		if(!"".contentEquals(param.get("trOrderStatu").toString())){//状态
			webCondition += getAnd(webCondition)+" "+tempordertab+".ORDER_STATU="+param.get("trOrderStatu").toString();
		}
		String callNumber=param.get("proNum").toString();
		if(!"".contentEquals(callNumber)){//产品号码
			webCondition += getAnd(webCondition)+" "+tempordertab+".PROD_NUM='"+callNumber+"'";
		}
		if(!"".contentEquals(param.get("sheetId").toString())){//服务单号
			webCondition += getAnd(webCondition)+" "+tempordertab+".SERVICE_ORDER_ID='"+param.get("sheetId").toString()+"'";
		}
		if(!"".contentEquals(param.get("unifiedCode").toString())){//集团编号
			webCondition += getAnd(webCondition)+" "+tempcmpunified+".UNIFIED_COMPLAINT_CODE='"+param.get("unifiedCode").toString()+"'";
		}
		if(!"".contentEquals(param.get("sourceNum").toString())){//主叫号码
			webCondition += getAnd(webCondition)+" "+tempordertab+".SOURCE_NUM='"+param.get("sourceNum").toString()+"'";
		}
		if(!"".contentEquals(param.get("relaInfo").toString())){//联系电话
			webCondition += getAnd(webCondition)+" "+tempordertab+".RELA_INFO='"+param.get("relaInfo").toString()+"'";
		}
		if(null!=param.get("cutName") && !"".contentEquals(param.get("cutName").toString())){//客户姓名
			webCondition += getAnd(webCondition)+" "+tempcusttab+".CUST_NAME='"+param.get("cutName").toString()+"'";
		}
		if(null!=param.get("channelDescId") && !"".contentEquals(param.get("channelDescId").toString())){//渠道细项
			webCondition += getAnd(webCondition)+" "+tempordertab+".channel_detail_id="+Integer.parseInt(param.get("channelDescId").toString());
		}
		String logoName=param.get("trAcceptName").toString();
		if(!"".contentEquals(logoName)){//受理员工登录号
			int staffId = pubFunc.getStaffId(logoName);//获取员工id
			webCondition += getAnd(webCondition)+" "+tempordertab+".ACCEPT_STAFF_ID="+staffId;
		}
		if(!"".contentEquals(param.get("askOrgId").toString())){//受理部门
			webCondition += getAnd(webCondition)+" "+tempordertab+".accept_org_id='"+param.get("askOrgId").toString()+"'";
		}
		if("".contentEquals(param.get("sheetId").toString())){
			if(param.get("endTime").toString().length() > 0 && param.get("startTime").toString().length() > 0) {
				webCondition += getAnd(webCondition)+" "+tempordertab+".ACCEPT_DATE between str_to_date('"+param.get("startTime").toString()+"', '%Y-%m-%d %H:%i:%s')";
				webCondition += getAnd(webCondition)+"str_to_date('"+param.get("endTime").toString()+"' , '%Y-%m-%d %H:%i:%s')";
			}
		}

		if("".contentEquals(param.get("sheetId").toString()) && "".contentEquals(callNumber) && "".contentEquals(param.get("relaInfo").toString())){//服务单号、产品号码、联系电话
			authFlag = true;
		}
		
		//部/局编码
		webCondition = this.setComplaintMiitCode(webCondition, param, tempordertab);
		}
		
		String sql = "";
		String sql2 = "";
		String serviceTypeWhere = this.getServiceTypeWhere(param, tempordertab);
		if("WF500021977".contentEquals(funcId)) {
			sql = "SELECT   CC_CMP_UNIFIED_RETURN_HIS.UNIFIED_COMPLAINT_CODE AS UNIFIED_COMPLAINT_CODE,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.SERVICE_ORDER_ID        AS SERVICE_ORDER_ID,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.ORDER_STATU             AS ORDER_STATU,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.ORDER_STATU_DESC        AS ORDER_STATU_DESC,\n" + 
					"       DATE_FORMAT(CC_SERVICE_ORDER_ASK_HIS.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.PROD_NUM                AS PROD_NUM,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.SERVICE_TYPE_DESC       AS SERVICE_TYPE_DESC,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.ACCEPT_STAFF_NAME       AS ACCEPT_STAFF_NAME,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.ACCEPT_COUNT            AS ACCEPT_COUNT,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.ORDER_LIMIT_TIME        AS ORDER_LIMIT_TIME,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.MONTH_FLAG              AS MONTH_FLAG,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.CUST_GUID               AS CUST_GUID,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.REGION_ID               AS REGION_ID," + 
					"       CC_SERVICE_ORDER_ASK_HIS.REGION_NAME             AS REGION_NAME, "+
					"       CC_SERVICE_ORDER_ASK_HIS.SERVICE_TYPE            AS SERVICE_TYPE,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.ORDER_VESION            AS ORDER_VESION,\n" + 
					"       CC_SERVICE_ORDER_ASK_HIS.ACCEPT_ORG_NAME         AS ACCEPT_ORG_NAME,\n" + 
					"       CC_SERVICE_CONTENT_ASK_HIS.APPEAL_PROD_NAME      AS APPEAL_PROD_NAME,\n" + 
					"       CC_ORDER_CUST_INFO_HIS.CUST_SERV_GRADE_NAME      AS CUST_GRADE_NAME,\n" + 
					"       CC_ORDER_CUST_INFO_HIS.CUST_BRAND_DESC           AS CUST_BRAND_DESC,\n" + 
					"       (SELECT COUNT(1) FROM CC_HASTEN_SHEET_HIS WHERE CC_HASTEN_SHEET_HIS.SERVICE_ORDER_ID = CC_SERVICE_ORDER_ASK_HIS.SERVICE_ORDER_ID) AS CUIDANCOUNT, " + 
					" (select b.logonname from tsm_staff b where b.staff_id=ACCEPT_STAFF_ID) as LOGONNAME,"+
					"       DATE_FORMAT(CC_SERVICE_ORDER_ASK_HIS.MODIFY_DATE,'%Y-%m-%d %H:%i:%s') AS MODIFY_DATE,\n" + 
					"       CC_SERVICE_CONTENT_ASK_HIS.SERVICE_TYPE_DETAIL   AS SERVICE_TYPE_DETAIL, " +
					"       CC_SERVICE_ORDER_ASK_HIS.RELA_INFO   AS RELA_INFO, " +
					"       CC_SERVICE_CONTENT_ASK_HIS.BEST_ORDER_DESC   AS BEST_ORDER_DESC, " +
					"       CC_SERVICE_ORDER_ASK_HIS.ACCEPT_CHANNEL_ID "+
					"  FROM CC_SERVICE_CONTENT_ASK_HIS, CC_SERVICE_ORDER_ASK_HIS " + 
					"LEFT JOIN CC_ORDER_CUST_INFO_HIS ON CC_SERVICE_ORDER_ASK_HIS.CUST_GUID = CC_ORDER_CUST_INFO_HIS.CUST_GUID " + 
					"LEFT JOIN CC_CMP_UNIFIED_RETURN_HIS ON CC_SERVICE_ORDER_ASK_HIS.SERVICE_ORDER_ID = CC_CMP_UNIFIED_RETURN_HIS.COMPLAINT_WORKSHEET_ID " + 
					"WHERE CC_SERVICE_ORDER_ASK_HIS.SERVICE_ORDER_ID = CC_SERVICE_CONTENT_ASK_HIS.SERVICE_ORDER_ID\n" + 
					" AND CC_SERVICE_ORDER_ASK_HIS.ORDER_VESION = CC_SERVICE_CONTENT_ASK_HIS.ORDER_VESION\n" + serviceTypeWhere +
					" AND ( "+webCondition+" ) ";
			if(authFlag) {
				sql = systemAuthorization.getAuthedSql(null, sql, "900018274");
			}
			sql += " ORDER BY CC_SERVICE_ORDER_ASK_HIS.ACCEPT_DATE DESC " + where;
			
			sql2 = "SELECT count(CC_SERVICE_ORDER_ASK_HIS.SERVICE_ORDER_ID) counts " + 
					"  FROM CC_SERVICE_CONTENT_ASK_HIS, CC_SERVICE_ORDER_ASK_HIS " + 
					( null!=param.get("cutName") && !"".contentEquals(param.get("cutName").toString()) ?
					" LEFT JOIN CC_ORDER_CUST_INFO_HIS ON CC_SERVICE_ORDER_ASK_HIS.CUST_GUID = CC_ORDER_CUST_INFO_HIS.CUST_GUID " : "") + 
					( !"".contentEquals(param.get("unifiedCode").toString()) ?
					" LEFT JOIN CC_CMP_UNIFIED_RETURN_HIS ON CC_SERVICE_ORDER_ASK_HIS.SERVICE_ORDER_ID = CC_CMP_UNIFIED_RETURN_HIS.COMPLAINT_WORKSHEET_ID " : "") + 
					" WHERE CC_SERVICE_ORDER_ASK_HIS.SERVICE_ORDER_ID = CC_SERVICE_CONTENT_ASK_HIS.SERVICE_ORDER_ID\n" + 
					"   AND CC_SERVICE_ORDER_ASK_HIS.ORDER_VESION = CC_SERVICE_CONTENT_ASK_HIS.ORDER_VESION\n" + serviceTypeWhere +
					"   AND ( "+webCondition+" ) ";
			if(authFlag) {
				sql2 = systemAuthorization.getAuthedSql(null, sql2, "900018274");
			}
			
		}else {
			sql = "SELECT CC_CMP_UNIFIED_RETURN.UNIFIED_COMPLAINT_CODE AS UNIFIED_COMPLAINT_CODE,\n" + 
					"                       CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID AS SERVICE_ORDER_ID,\n" + 
					"                       CC_SERVICE_ORDER_ASK.ORDER_STATU AS ORDER_STATU,\n" + 
					"                       CC_SERVICE_ORDER_ASK.ORDER_STATU_DESC AS ORDER_STATU_DESC,\n" + 
					"                       DATE_FORMAT(CC_SERVICE_ORDER_ASK.ACCEPT_DATE,'%Y-%m-%d %H:%i:%s') AS ACCEPT_DATE,\n" + 
					"                       CC_SERVICE_ORDER_ASK.PROD_NUM AS PROD_NUM,\n" + 
					"                       CC_SERVICE_ORDER_ASK.SERVICE_TYPE_DESC AS SERVICE_TYPE_DESC,\n" + 
					"                       CC_SERVICE_ORDER_ASK.ACCEPT_STAFF_NAME AS ACCEPT_STAFF_NAME,\n" + 
					"                       CC_SERVICE_ORDER_ASK.ACCEPT_COUNT AS ACCEPT_COUNT,\n" + 
					"                       CC_SERVICE_ORDER_ASK.ORDER_LIMIT_TIME AS ORDER_LIMIT_TIME,\n" + 
					"                       CC_SERVICE_ORDER_ASK.MONTH_FLAG AS MONTH_FLAG,\n" + 
					"                       CC_SERVICE_ORDER_ASK.CUST_GUID AS CUST_GUID,\n" + 
					"                       CC_SERVICE_ORDER_ASK.REGION_ID AS REGION_ID,\n" + 
					"                       CC_SERVICE_ORDER_ASK.REGION_NAME AS REGION_NAME, "+
					"                       CC_SERVICE_ORDER_ASK.SERVICE_TYPE AS SERVICE_TYPE,\n" + 
					"                       CC_SERVICE_ORDER_ASK.ORDER_VESION AS ORDER_VESION,\n" + 
					"                       CC_SERVICE_ORDER_ASK.ACCEPT_ORG_NAME AS ACCEPT_ORG_NAME,\n" + 
					"                       CC_SERVICE_CONTENT_ASK.APPEAL_PROD_NAME AS APPEAL_PROD_NAME,\n" + 
					"                       CC_ORDER_CUST_INFO.CUST_SERV_GRADE_NAME AS CUST_GRADE_NAME,\n" + 
					"                       CC_ORDER_CUST_INFO.CUST_BRAND_DESC AS CUST_BRAND_DESC,\n" + 
					"                       DATE_FORMAT(CC_SERVICE_ORDER_ASK.MODIFY_DATE,'%Y-%m-%d %H:%i:%s') AS MODIFY_DATE,\n" + 
					"                       (SELECT COUNT(1) " + 
					"                          FROM CC_HASTEN_SHEET\n" + 
					"                         WHERE CC_HASTEN_SHEET.SERVICE_ORDER_ID =\n" + 
					"                               CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID) as CUIDANCOUNT, " + 
					" (select b.logonname from tsm_staff b where b.staff_id=ACCEPT_STAFF_ID)  as LOGONNAME,  "+
					"                       CC_SERVICE_CONTENT_ASK.SERVICE_TYPE_DETAIL   AS SERVICE_TYPE_DETAIL, " +
					"                       CC_SERVICE_ORDER_ASK.RELA_INFO   AS RELA_INFO, " +
					"                       CC_SERVICE_CONTENT_ASK.BEST_ORDER_DESC   AS BEST_ORDER_DESC, " +
					"                       CC_SERVICE_ORDER_ASK.ACCEPT_CHANNEL_ID "+
					"  FROM CC_SERVICE_CONTENT_ASK, CC_SERVICE_ORDER_ASK " + 
					"LEFT JOIN CC_ORDER_CUST_INFO ON CC_SERVICE_ORDER_ASK.CUST_GUID = CC_ORDER_CUST_INFO.CUST_GUID " + 
					"LEFT JOIN CC_CMP_UNIFIED_RETURN ON CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID = CC_CMP_UNIFIED_RETURN.COMPLAINT_WORKSHEET_ID " + 
					"WHERE CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID = CC_SERVICE_CONTENT_ASK.SERVICE_ORDER_ID\n" + serviceTypeWhere +
					"                   AND ( "+webCondition+" ) ";
			if(authFlag) {
				sql = systemAuthorization.getAuthedSql(null, sql, "900018275");
			}
			sql += "  ORDER BY CC_SERVICE_ORDER_ASK.ACCEPT_DATE DESC " + where;
			
			sql2 = "SELECT count(CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID) counts " + 
					"  FROM CC_SERVICE_CONTENT_ASK, CC_SERVICE_ORDER_ASK " + 
					( null!=param.get("cutName") && !"".contentEquals(param.get("cutName").toString()) ?
					" LEFT JOIN CC_ORDER_CUST_INFO ON CC_SERVICE_ORDER_ASK.CUST_GUID = CC_ORDER_CUST_INFO.CUST_GUID " : "") + 
					( !"".contentEquals(param.get("unifiedCode").toString()) ?
					" LEFT JOIN CC_CMP_UNIFIED_RETURN ON CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID = CC_CMP_UNIFIED_RETURN.COMPLAINT_WORKSHEET_ID " : "") + 
					"WHERE CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID = CC_SERVICE_CONTENT_ASK.SERVICE_ORDER_ID\n" + serviceTypeWhere +
					"   AND ( "+webCondition+" ) ";
			if(authFlag) {
				sql2 = systemAuthorization.getAuthedSql(null, sql2, "900018275");
			}
		}
		
		return sql+"@"+sql2;
	}
	
	private String setComplaintMiitCode(String webCondition, Map param, String tempordertab) {
		String miitCode = "";
		if(param.containsKey("miitCode")) {
			miitCode = param.get("miitCode").toString();
		}
		if(org.apache.commons.lang3.StringUtils.isNotBlank(miitCode)) {
			boolean hisFlag = "CC_SERVICE_ORDER_ASK_HIS".contentEquals(tempordertab);
			String orderIdStr = orderQury.getOrderIdStrByMiitCode(miitCode, hisFlag);
			log.info("miitCode: {} whereStr: {}", miitCode, orderIdStr);
			if(orderIdStr == null) {
				webCondition += " AND 1 <> 1";
			} else {
				webCondition += " AND " + tempordertab + ".SERVICE_ORDER_ID in " + orderIdStr;
			}
		}
		return webCondition;
	}
	
	private String getServiceTypeWhere(Map map, String tempordertab) {
		if(map.get("cliqueFlag") != null && !"".equals(map.get("cliqueFlag").toString())) {
			return " AND " + tempordertab+".SERVICE_TYPE IN (720130000, 700006312, 720200003, 700001171) ";
		}
		return "";
	}
	
	@SuppressWarnings("unchecked")
	private String getRegion(String id) {
		Map m = new HashMap();
		m.put("3","025");
		m.put("4","0511");
		m.put("15","0510");
		m.put("20","0512");
		m.put("26","0513");
		m.put("33","0514");
		m.put("39","0515");
		m.put("48","0516");
		m.put("60","0517");
		m.put("63","0518");
		m.put("69","0519");
		m.put("79","0523");
		m.put("84","0527");
		m.put("2","2");
		m.put("999","999");//非江苏电信用户
		return m.get(id).toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map addLinkStr(Map param, String funcId) {
		//当前
		if("WF500021786".equals(funcId)) {
			String orderId = param.get("orderId").toString();
			String monthFlag = param.get("monthFlag").toString();
			String regionId = param.get("regionId").toString();
			String webCondition = "CC_WORK_SHEET.SERVICE_ORDER_ID = '" + orderId + "'";
			webCondition += " AND CC_WORK_SHEET.MONTH_FLAG = " + monthFlag + " AND CC_WORK_SHEET.REGION_ID = " + regionId; 
			param.put("WEB_ADDITIONALCONDITION", webCondition);
		}else if("WF520025305".equals(funcId)) {//历史
			String orderId = param.get("orderId").toString();
			String monthFlag = param.get("monthFlag").toString();
			String regionId = param.get("regionId").toString();
			String webCondition = "CC_WORK_SHEET_HIS.SERVICE_ORDER_ID = '" + orderId + "'";
			webCondition += " AND CC_WORK_SHEET_HIS.MONTH_FLAG = " + monthFlag + " AND CC_WORK_SHEET_HIS.REGION_ID = " + regionId; 
			param.put("WEB_ADDITIONALCONDITION", webCondition);
		}
		return param;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public Map addActionWhere(Map param, String funcId) {
		String table = "CC_WORK_SHEET";
		String shid = param.get("shid").toString();
		String month = param.get("month").toString();
		String region = param.get("region").toString();
		if("WF500021969".contentEquals(funcId)) {
			table = "CC_WORK_SHEET_HIS";
		}else if("WF500021686".contentEquals(funcId)) {
			table = "CC_WORK_SHEET";
		}
		String where = table+".WORK_SHEET_ID='"+shid+"' AND "+table+".MONTH_FLAG="+month+" AND "+table+".REGION_ID="+region;
		param.put("WEB_ADDITIONALCONDITION", where);
		return param;
	}

	@Override
	public String getQryClieque(Map param) {
		String currentPage = param.get("currentPage").toString();
		int current = Integer.parseInt(currentPage);
		Map in = (Map)param.get("poolForm");
		String where = "";
		String begion = " limit 0,100";
		if(current>1) {
			int minNum = (current-1)*100;
			int pageSize = 100;
			begion = " limit "+minNum+","+pageSize;
		}
		String complaintworksheetid = in.get("complaintworksheetid").toString();
		String orderId = in.get("orderId").toString();
		String prodNum = in.get("prodNum").toString();
		String cliqueOrderStatu = in.get("cliqueOrderStatu").toString();
		String assigntype = in.get("assigntype").toString();
		String comeFrom = in.get("comeFrom").toString();
		String isTimeFlag = in.get("isTimeFlag").toString();
		String assigncode = in.get("assigncode").toString();
		String sponsorId = in.get("sponsorId").toString();
		String recvCode = in.get("receivecode").toString();
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(complaintworksheetid)) {
			where += " and r.complaint_worksheet_id='"+complaintworksheetid+"'";
		}
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(orderId)) {
			where += " and r.service_order_id='"+orderId+"'";
		}
		if(prodNum.length()>0) {
			where += " and g.prod_num='"+prodNum+"'";
		}
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(cliqueOrderStatu) && !"selected".equals(cliqueOrderStatu)) {
			where += " and r.statu='"+cliqueOrderStatu+"'";
		}
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(assigntype) && !"selected".equals(assigntype)) {
			where += " and r.assign_type='"+assigntype+"'";
		}
		if(org.apache.commons.lang3.StringUtils.isNotEmpty(comeFrom) && !"[]".equals(comeFrom)) {
			String newFrom = comeFrom.replace("[", "").replace("]", "");
			String[] formArr = newFrom.split(",");
			StringBuilder fromStr = new StringBuilder("");
			for(int i=0;i<formArr.length;i++) {
				fromStr.append("'" + formArr[i].trim() + "', ");
			}
			where += " and w.ask_source_srl in ("+org.apache.commons.lang3.StringUtils.removeEnd(fromStr.toString(), ", ")+")";
		}
		if("true".equals(isTimeFlag)){
			JSONArray timeArr = JSONArray.fromObject(in.get("acceptDate"));
			where += " and t.excutetime between str_to_date('"+timeArr.optString(0)+"','%Y-%m-%d %H:%i:%s')"
				 		+" and str_to_date('"+timeArr.optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(!"selected".equals(assigncode)){
			where+=" and a.assign_code='"+sponsorId+"'";
		}
		if(!"selected".equals(recvCode)){
			where+=" and a.receiver_code='"+recvCode+"'";
		}
		String sql=
				"SELECT r.RELA_GUID," +
				"       r.COMPLAINT_WORKSHEET_ID," +
				"       r.SERVICE_ORDER_ID," +
				"       r.STATU," +
				"       r.ASSIGN_TYPE," +
				"       t.TRANSACTIONID," +
				"       w.COMPLAINT_PHONE," +
				"       w.ASK_SOURCE_SRL," +
				"       w.ASK_SOURCE_SRL_DESC," +
				"       a.ASSIGN_CODE," +
				"       a.ASSIGN_NAME," +
				"       a.RECEIVER_CODE," +
				"       a.RECEIVER_NAME," +
				"       g.REGION_ID," +
				"       g.REGION_NAME" +
				"  FROM " +
				"       CC_CMP_RELATION  r LEFT JOIN CC_SERVICE_ORDER_ASK g ON r.SERVICE_ORDER_ID = g.SERVICE_ORDER_ID," +
				"       CC_CMP_TCPCONT   t," +
				"       CC_CMP_ASSIGN    a," +
				"       CC_CMP_WORKSHEET w" +
				" WHERE r.RELA_GUID = t.RELA_GUID" +
				"   AND t.OPER_TYPE = 'ASSIGN'" +
				"   AND t.TRANSACTIONID = a.TRANSACTIONID" +
				"   AND t.TRANSACTIONID = w.TRANSACTIONID " + where + begion;
		String sql2="SELECT count(1) as COUNTS" + 
				"  FROM " + 
				"       cc_cmp_relation  r" + this.getRelationSql(prodNum) + 
				"       cc_cmp_tcpcont   t," + 
				"       cc_cmp_assign    a," + 
				"       cc_cmp_worksheet w" +
				" where r.rela_guid = t.rela_guid" +
				"   and t.oper_type = 'ASSIGN'" + 
				"   and t.transactionid = a.transactionid " + 
				"   and t.transactionid = w.transactionid " + where;
		return sql+"@"+sql2;
	}
	
	private String getRelationSql(String prodNum) {
		return (prodNum.length()>0 ? " LEFT JOIN CC_SERVICE_ORDER_ASK g ON r.SERVICE_ORDER_ID = g.SERVICE_ORDER_ID," : ",");
	}

	public JSONObject addFeedbackInfo(String orderId) {
		String sql = 
"SELECT IF(IS_SLV = '0', '否', '是') isApprove,\n" +
"       IF(CMPLNT_PROD_LEVEL_1 IS NULL OR CMPLNT_PROD_LEVEL_1 IN ('', '0', '-1'), '501', CMPLNT_PROD_LEVEL_1) productCategory1,\n" + 
"       IF(CMPLNT_PROD_LEVEL_2 IS NULL OR CMPLNT_PROD_LEVEL_2 IN ('', '0', '-1'),\n" + 
"          '501010',\n" + 
"          IF(CHAR_LENGTH(CMPLNT_PROD_LEVEL_2) = 6,\n" + 
"             CMPLNT_PROD_LEVEL_2,\n" + 
"             IFNULL((SELECT DST_CODE\n" + 
"                      FROM cc_cmp_data_map\n" + 
"                     WHERE FLAG = 1\n" + 
"                       AND FEATURE_CODE = 'PRODUCT_CATEGORY'\n" + 
"                       AND SRC_CODE = CMPLNT_PROD_LEVEL_2 LIMIT 1),\n" + 
"                    '501010'))) productCategory2,\n" + 
"       IF((SELECT COUNT(1)\n" + 
"             FROM CCS_ST_MAPPING_JT_CHANNEL\n" + 
"            WHERE CHANNEL_1 = DVLP_CHNL_LEVEL_1\n" + 
"              AND CHANNEL_2 = DVLP_CHNL_LEVEL_2\n" + 
"              AND CHANNEL_3 = DVLP_CHNL_LEVEL_3) > 0,\n" + 
"          DVLP_CHNL_LEVEL_1,\n" + 
"          '') channelCategory1,\n" + 
"       IF((SELECT COUNT(1)\n" + 
"             FROM CCS_ST_MAPPING_JT_CHANNEL\n" + 
"            WHERE CHANNEL_1 = DVLP_CHNL_LEVEL_1\n" + 
"              AND CHANNEL_2 = DVLP_CHNL_LEVEL_2\n" + 
"              AND CHANNEL_3 = DVLP_CHNL_LEVEL_3) > 0,\n" + 
"          DVLP_CHNL_LEVEL_2,\n" + 
"          '') channelCategory2,\n" + 
"       IF((SELECT COUNT(1)\n" + 
"             FROM CCS_ST_MAPPING_JT_CHANNEL\n" + 
"            WHERE CHANNEL_1 = DVLP_CHNL_LEVEL_1\n" + 
"              AND CHANNEL_2 = DVLP_CHNL_LEVEL_2\n" + 
"              AND CHANNEL_3 = DVLP_CHNL_LEVEL_3) > 0,\n" + 
"          DVLP_CHNL_LEVEL_3,\n" + 
"          '') channelCategory3,\n" + 
"       IF((SELECT COUNT(1)\n" + 
"             FROM CCS_ST_MAPPING_JT_CHANNEL\n" + 
"            WHERE CHANNEL_1 = DVLP_CHNL_LEVEL_1\n" + 
"              AND CHANNEL_2 = DVLP_CHNL_LEVEL_2\n" + 
"              AND CHANNEL_3 = DVLP_CHNL_LEVEL_3) > 0,\n" + 
"          IF(DVLP_CHNL_NM IS NULL OR DVLP_CHNL_NM = '-1', '', DVLP_CHNL_NM),\n" + 
"          '') developChannel,\n" + 
"       RES_CON_CHANNEL controlChannel,\n" + 
"       IF(CUST_TYPE IS NULL OR CUST_TYPE IN ('', '-1'), '无', CUST_TYPE) custType,\n" + 
"       IF(MEMBER_LEVEL IS NULL OR MEMBER_LEVEL IN ('', '-1'), '无', MEMBER_LEVEL) userStarLevel,\n" + 
"       IF(SERV_AGE IS NULL OR SERV_AGE IN ('', '0', '-1'), '无', SERV_AGE) netAge,\n" + 
"       IF(OFFER_NBR IS NULL OR CHAR_LENGTH(TRIM(OFFER_NBR)) < 2 OR OFFER_NBR NOT REGEXP '^[0-9]+$', '', OFFER_NBR) sceneFieldExplain,\n" + 
"       IF(GROUP_OFFER_NBR IS NULL OR CHAR_LENGTH(TRIM(GROUP_OFFER_NBR)) < 2 OR GROUP_OFFER_NBR NOT REGEXP '^[0-9]+$', '', GROUP_OFFER_NBR) sceneFieldExplainJt,\n" + 
"       IF(MAIN_OFFER_ID IS NULL OR CHAR_LENGTH(TRIM(MAIN_OFFER_ID)) < 2 OR MAIN_OFFER_ID NOT REGEXP '^[0-9]+$', '', MAIN_OFFER_ID) mainId,\n" + 
"       IF(MAIN_OFFER_NM IS NULL OR MAIN_OFFER_ID IS NULL OR CHAR_LENGTH(TRIM(MAIN_OFFER_ID)) < 2 OR MAIN_OFFER_ID NOT REGEXP '^[0-9]+$', '', MAIN_OFFER_NM) mainName,\n" + 
"       IF((SELECT COUNT(1)\n" + 
"             FROM CCS_ST_MAPPING_JT_CHANNEL\n" + 
"            WHERE CHANNEL_1 = DISPUTE_CHNL_1\n" + 
"              AND CHANNEL_2 = DISPUTE_CHNL_2\n" + 
"              AND CHANNEL_3 = DISPUTE_CHNL_3) > 0,\n" + 
"          IF(DISPUTE_CHNL_NM IS NULL OR DISPUTE_CHNL_NM IN ('0', '-1'), '', DISPUTE_CHNL_NM),\n" + 
"          '') disputeChannel,\n" + 
"       IF((SELECT COUNT(1)\n" + 
"             FROM CCS_ST_MAPPING_JT_CHANNEL\n" + 
"            WHERE CHANNEL_1 = DISPUTE_CHNL_1\n" + 
"              AND CHANNEL_2 = DISPUTE_CHNL_2\n" + 
"              AND CHANNEL_3 = DISPUTE_CHNL_3) > 0,\n" + 
"          DISPUTE_CHNL_1,\n" + 
"          '') disputeChannel1,\n" + 
"       IF((SELECT COUNT(1)\n" + 
"             FROM CCS_ST_MAPPING_JT_CHANNEL\n" + 
"            WHERE CHANNEL_1 = DISPUTE_CHNL_1\n" + 
"              AND CHANNEL_2 = DISPUTE_CHNL_2\n" + 
"              AND CHANNEL_3 = DISPUTE_CHNL_3) > 0,\n" + 
"          DISPUTE_CHNL_2,\n" + 
"          '') disputeChannel2,\n" + 
"       IF((SELECT COUNT(1)\n" + 
"             FROM CCS_ST_MAPPING_JT_CHANNEL\n" + 
"            WHERE CHANNEL_1 = DISPUTE_CHNL_1\n" + 
"              AND CHANNEL_2 = DISPUTE_CHNL_2\n" + 
"              AND CHANNEL_3 = DISPUTE_CHNL_3) > 0,\n" + 
"          DISPUTE_CHNL_3,\n" + 
"          '') disputeChannel3,\n" + 
"       IF(OFFER_TYPE IS NULL OR OFFER_TYPE = '-1', '', OFFER_TYPE) businessType,\n" + 
"       IFNULL((SELECT '重单'\n" + 
"                FROM CC_CMP_UNIFIED_REPEAT\n" + 
"               WHERE REPEAT_STATUS = 1\n" + 
"                 AND CUR_SOI = SHEET_ID_PROV\n" + 
"                 AND REPEAT_TYPE = 1 LIMIT 1),\n" + 
"              IFNULL((SELECT '重单'\n" + 
"                       FROM CC_CMP_UNIFIED_REPEAT_HIS\n" + 
"                      WHERE REPEAT_STATUS = 1\n" + 
"                        AND CUR_SOI = SHEET_ID_PROV\n" + 
"                        AND REPEAT_TYPE = 1 LIMIT 1),\n" + 
"                     IF(IS_SLV = '0', '否', '是'))) settleFlag,\n" + 
"       IFNULL(SAT_EVAL, '无') custAssess,\n" + 
"       IFNULL((SELECT IF(IS_REASONABLE = 1, '是', '否') FROM CC_SHEET_QUALITATIVE WHERE SERVICE_ORDER_ID = SHEET_ID_PROV ORDER BY CREAT_DATA DESC LIMIT 1),\n" + 
"              IFNULL((SELECT IF(IS_REASONABLE = 1, '是', '否') FROM CC_SHEET_QUALITATIVE_HIS WHERE SERVICE_ORDER_ID = SHEET_ID_PROV ORDER BY CREAT_DATA DESC LIMIT 1),\n" + 
"                     '否')) complaintRational,\n" + 
"       IFNULL((SELECT IF(CONTROL_AREA_FIR_DESC = '企业有责', '企业有责', '企业无责')\n" + 
"                FROM CC_SHEET_QUALITATIVE\n" + 
"               WHERE SERVICE_ORDER_ID = SHEET_ID_PROV\n" + 
"               ORDER BY CREAT_DATA DESC LIMIT 1),\n" + 
"              IFNULL((SELECT IF(CONTROL_AREA_FIR_DESC = '企业有责', '企业有责', '企业无责')\n" + 
"                       FROM CC_SHEET_QUALITATIVE_HIS\n" + 
"                      WHERE SERVICE_ORDER_ID = SHEET_ID_PROV\n" + 
"                      ORDER BY CREAT_DATA DESC LIMIT 1),\n" + 
"                     '企业无责')) dutyQualit,\n" + 
"       DATE_FORMAT(IFNULL((SELECT CALL_ANSWER\n" + 
"                            FROM CC_ORDER_CALLOUT_REC\n" + 
"                           WHERE CALLOUT_TYPE = 1\n" + 
"                             AND REC_TYPE = 0\n" + 
"                             AND TALKLONGTIME IS NOT NULL\n" + 
"                             AND SERVICE_ORDER_ID = SHEET_ID_PROV\n" + 
"                           ORDER BY CALL_ANSWER DESC LIMIT 1),\n" + 
"                          IFNULL((SELECT CREAT_DATA\n" + 
"                                   FROM CC_CUSTOMER_VISIT\n" + 
"                                  WHERE VISIT_TYPE = 1\n" + 
"                                    AND SERVICE_ORDER_ID = SHEET_ID_PROV\n" + 
"                                  ORDER BY CREAT_DATA DESC LIMIT 1),\n" + 
"                                 IFNULL((SELECT CREAT_DATA\n" + 
"                                          FROM CC_CUSTOMER_VISIT_HIS\n" + 
"                                         WHERE VISIT_TYPE = 1\n" + 
"                                           AND SERVICE_ORDER_ID = SHEET_ID_PROV\n" + 
"                                         ORDER BY CREAT_DATA DESC LIMIT 1),\n" + 
"                                        NOW()))),\n" + 
"                   '%Y%m%d%H%i%s') recentlyContactTime\n" + 
"  FROM dapd_sheet_info\n" + 
" WHERE SHEET_ID_PROV = ?";
		List list = jt.queryForList(sql, orderId);
		if (!list.isEmpty()) {
			return JSONObject.fromObject(list.get(0));
		}
		return new JSONObject();
	}

	@Override
	public String getQrySn(Map param) {
		String currentPage=param.get("currentPage").toString();
		int current=Integer.parseInt(currentPage);
		Map in=(Map) param.get("poolForm");
		String where="";
		String begion=" limit 0,100";
		if(current>1){
			int minNum=(current-1)*100;
			int pageSize=100;
			begion=" limit "+minNum+","+pageSize;
		}
		String orderId=in.get("orderId").toString();
		String worksheetid=in.get("worksheetid").toString();
		String prodNum=in.get("prodNum").toString();
		String servType=in.get("servType").toString();
		String areaId=in.get("areaId").toString();
		String acceptCome=in.get("acceptCome").toString();
		String askChannelId=in.get("askChannelId").toString();
		String isTimeFlag=in.get("isTimeFlag").toString();
		String applyStaff=in.get("applyStaff").toString();
		if(orderId!= null && orderId.length()>0 && !"selected".equals(orderId)) {
			where+=" and s.service_order_id='"+orderId+"'";
		}
		if(worksheetid!= null && worksheetid.length()>0 && !"selected".equals(worksheetid)) {
			where+=" and w.work_sheet_id='"+worksheetid+"'";
		}
		if(prodNum!= null && prodNum.length()>0 && !"selected".equals(prodNum)) {
			where+=" and s.prod_num='"+prodNum+"'";
		}
		if(servType!= null && servType.length()>0 && !"selected".equals(servType)) {
			where+=" and S.SERVICE_TYPE='"+servType+"'";
		}
		if(areaId!= null && areaId.length()>0 && !"selected".equals(areaId)) {
			where+=" and S.REGION_ID='"+areaId+"'";
		}
		if(acceptCome!= null && acceptCome.length()>0 && !"selected".equals(acceptCome)) {
			where+=" and S.ACCEPT_COME_FROM='"+acceptCome+"'";
		}
		if(askChannelId!= null && askChannelId.length()>0 && !"selected".equals(askChannelId)) {
			where+=" and S.ACCEPT_CHANNEL_ID='"+askChannelId+"'";
		}
		if("true".equals(isTimeFlag)){
			String time=in.get("acceptDate").toString();
			String [] timeArr=time.replace("\\[","").replace("\\]","").split(",");
			where += " and a.aud_date between str_to_date('"+timeArr[0]+"','%Y-%m-%d %H:%i:%s')"
                    +" and str_to_date('"+timeArr[1]+"','%Y-%m-%d %H:%i:%s')";
		}
		if(applyStaff.length()>0) {
			List result=jt.queryForList("select staff_id from tsm_staff where logonname=?", applyStaff);
			if(!result.isEmpty()){
				where+=" and A.APPLY_STAFF='"+applyStaff+"'";
			}
		}
		String sql=" SELECT S.SERVICE_ORDER_ID," +
				"       S.SERVICE_TYPE," + 
				"       S.SERVICE_TYPE_DESC," + 
				"       S.REGION_ID," + 
				"       S.REGION_NAME," + 
				"       W.WORK_SHEET_ID," + 
				"       S.PROD_NUM," + 
				"       S.ACCEPT_CHANNEL_ID, " + 
				"       S.ACCEPT_CHANNEL_DESC, " + 
				"       S.ORDER_STATU, " + 
				"       S.MONTH_FLAG, " + 
				"       S.CUST_GUID, " + 
				"       A.APPLY_STAFF,  " + 
				"       A.APPLY_STAFF_NAME," + 
				"       A.APPLY_ORG, " + 
				"       A.APPLY_ORG_NAME, " + 
				"       DATE_FORMAT(A.AUD_DATE, '%Y-%m-%d %H:%i:%s') AUD_DATE, " +
				"       A.APPLY_GUID, " + 
				"       W.SHEET_TYPE, " + 
				"       A.APPLY_AUD_STATU " + 
				"  FROM CC_SERVICE_ORDER_ASK S, CC_WORK_SHEET W, CC_SHEET_STATU_APPLY A " + 
				" WHERE A.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID " + 
				"   AND A.WORKSHEET_ID = W.WORK_SHEET_ID " + 
				"   AND A.APPLY_AUD_STATU IN (0, 1) " +
				"   AND A.APPLY_TYPE = 2 "+where+
				" "+begion;
		String sql2="SELECT count(1) as COUNTS " + 
				"  FROM CC_SERVICE_ORDER_ASK S, CC_WORK_SHEET W, CC_SHEET_STATU_APPLY A " + 
				" WHERE A.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID " + 
				"   AND A.WORKSHEET_ID = W.WORK_SHEET_ID " + 
				"   AND A.APPLY_AUD_STATU IN (0, 1) " + 
				"   AND A.APPLY_TYPE = 2 "+where;
		return sql+"@"+sql2;
	}

	@Override
	public String getSpecialInfoStr(String tableType, Map param) {
		String sourceNum=param.get("sourceNum").toString();
		String regionStr=param.get("regionId").toString();
		String currentPage=param.get("currentPage").toString();
		int current=Integer.parseInt(currentPage);
		String where="";
		String sql1="";
		String sql2="";
		if("1".equals(tableType)) {//ivr特殊用户表 1
			String begion=" limit 0 , 10";
			if(current>1){
				int minNum=(current-1)*10;
				int pageSize=10;
				begion=" limit "+minNum+" , "+pageSize;
			}
			if(StringUtils.isNotEmpty(sourceNum)) {
				where+=" and t.prod_number like '%"+sourceNum+"%'";
			}
			if(StringUtils.isNotEmpty(regionStr)) {
				int regionId=Integer.parseInt(regionStr);
				where+=" and t.region_id="+regionId;
			}
			sql1=   "select  t.PROD_NUMBER, " + 
					"        t.AREA_CODE, " + 
					"        t.STATE, " + 
					"        t.TYPE, " + 
					"        t.CREATE_STAFF, " + 
					"  (select a.logonname from tsm_staff a where a.staff_id=t.create_staff) LOGONAME, " +
					"  DATE_FORMAT(t.create_date, '%Y-%m-%d %H:%i:%s') CREATE_DATE, " + 
					"  t.REGION_ID " +
					"  from jscsc_ct_ivr.ccs_st_special_customer t " + 
					" where t.state = '1' " + where +
					" order by t.create_date desc " + begion;
			sql2="select count(1) as COUNTS from jscsc_ct_ivr.ccs_st_special_customer t where t.state='1' " + where;
		}else if("0".equals(tableType)) {//坐席特殊用户表 0
			String begion=" limit 0 , 10";
			if(current>1){
				int minNum=(current-1)*10;
				int pageSize=10;
				begion=" limit "+minNum+" , "+pageSize;
			}
			if(StringUtils.isNotEmpty(sourceNum)) {
				where+=" and c.source_num like '%"+sourceNum+"%'";
			}
			if(StringUtils.isNotEmpty(regionStr)) {
				int regionId=Integer.parseInt(regionStr);
				where+=" and c.region_id="+regionId;
			}
			sql1="SELECT ifnull(c.ACTIVE_TYPE,'') as ACTIVE_TYPE," +
					"       c.SOURCE_NUM," +
					"       date_format(c.create_date,'%Y-%m-%d %H:%i:%s') CREATE_DATE," +
					"       c.SPECIAL_INFO," +
					"       c.AREA_CODE," +
					"       c.REGION_ID," +
					"       c.CREATE_STAFF," + 
					"       (select a.logonname from tsm_staff a where a.staff_id=c.create_staff) LOGONAME" +
					"  from cs_custom_special_info c " +
					" where c.state = '0' " + where + 
					" order by c.create_date desc " + begion;
			sql2="SELECT count(1) as COUNTS from cs_custom_special_info c where c.state='0' "+where;
		}
		return sql1+"@"+sql2;
	}

	@Override
	public String qryZDSSOToken(JSONObject body) {
		String staffId=body.getString("staffId");
		String logonName=body.getString("logonName");
		String ssoId=body.getString("sso_Id");
		String flag=body.getString("flag");
		String areaName=body.getString("areaName");
		String phone=body.getString("phone");
		String regionId=body.getString("regionId");
		String accType=body.getString("accType");
		String linkId=body.getString("linkId");
		String param="";
		StringBuilder str=new StringBuilder();
		String operId="";
		List<Map<String, Object>> result=pubFunc.getSSOZDRL(ssoId, staffId);
		String pageType="";
		String token ="";
		String roleId ="0";//0 普通 1高级 调账班为1
		if("10-11-361143-363843".equals(linkId)) {
			roleId="1";
		}
		if("finance".equals(flag)) {
			pageType="201";
		}else if("vae".equals(flag)) {
			pageType="301";
		}else if("dispute".equals(flag)) {
			pageType="1";
		}
		if(!result.isEmpty()) {
			operId=result.get(0).get("LOGIN_NAME").toString();
			if("".equals(operId)) {
				operId="jsnoc_chenchen";
			}
		}
		JSONObject typeAndUrl=getTypeAndUrl(phone, regionId,accType,flag);
		String obstacleNum=typeAndUrl.getString("obstacleNum");
		accType=typeAndUrl.getString("accType");
		long now=new Date().getTime();
		switch(flag) {
			case "yd":
				str.append("phone="+obstacleNum).append("&operID="+operId).append("&acceptStaffNumber="+logonName).append("&regionalCenter="+areaName);
				String entryStr=DESEncryptUtil.encrypt(str.toString(), "JSnocCdmaVolte2018!@#cstobsInternet9");
				param="param="+entryStr+"&timestamp="+now;
				break;
			case "zq"://政企业务
				if(!result.isEmpty()) {
					operId=result.get(0).get("LOGIN_NAME").toString();
					if("".equals(operId)) {
						operId=staffId;
					}
				}
				str.append("key=1A1DBFEF3D1DF3B8B352950F9637929395379BEB198FEBCC&").append("roleCode=2&").append("zdJobNum="+operId).append("&callPhone="+obstacleNum);
				param=str.toString();
				break;
			case "itv": case "kd" :case "zh": //itv和智慧家庭业务跳转
				String linkUrl="";
				linkUrl=typeAndUrl.getString("linkUrl");
				String areaCode="";
				if("itv".equals(flag)){
					areaCode="ITV";
				}else if ("kd".equals(flag)){
					areaCode="BROADBAND";
				}else if ("zh".equals(flag)){
					areaCode="SMART";
				}
				String hashCode = DESEncryptUtil.getMD5(operId+now+"js025abcdefghijklm");
				str.append("areaCode="+areaCode).append("&hashCode="+hashCode).append("&operID="+operId).append("&argutsname="+now).append("&callPhone="+phone)
				.append("&areaID="+getRegion(regionId)).append("&acceptStaffNumber="+logonName).append(linkUrl);
				param=str.toString();
				break;
			case "tel"://固话
				hashCode = DESEncryptUtil.getMD5(operId+now+"js025abcdefghijklm");
				str.append("hashCode="+hashCode).append("&operID="+operId).append("&argutsname="+now).append("&callPhone="+obstacleNum)
				.append("&areaID="+getRegion(regionId));
				param=str.toString();
				break;
			case "vae"://增值业务跳转
				token=getOnekeyToken(logonName, getRegion(regionId), phone, pageType);
				str.append(token).append("&custNbr="+obstacleNum).append("&servType="+accType).append("&roleId="+roleId);
				param=str.toString();
				break;
			case "finance":
			case "dispute"://财务和手机上网争议跳转
				token=getOnekeyToken(logonName, getRegion(regionId), phone, pageType);
				str.append(token).append("&custNbr="+obstacleNum);
				param=str.toString();
				break;
			default:
			    break;
		}
		return param;
	}
	
	public JSONObject getTypeAndUrl(String phone,String regionId,String accType,String flag) {
		JSONObject result=new JSONObject();
		String obstacleNum=phone;
		String linkUrl="";
		com.alibaba.fastjson.JSONObject getJson=getProdInst(phone, regionId);
		String prodId="";
		String prodInstId="";
		if("0".equals(getJson.getString("code"))) {
			com.alibaba.fastjson.JSONObject dataJson=getJson.getJSONObject("data");
			com.alibaba.fastjson.JSONObject svcCont=dataJson.getJSONObject("contractRoot").getJSONObject("svcCont");
			if("0".equals(svcCont.getString("resultCode"))) {
				com.alibaba.fastjson.JSONArray detailList=svcCont.getJSONObject("resultObject").getJSONArray("accProdInstDetailList");
				String oldProdId="";
				switch (flag){
				case "itv":
					oldProdId="100000881";
					break;
				case "tel":
					oldProdId="100000002";
					break;
				case "kd":
					oldProdId="100000009,100000011,100000012,100000014";
					break;
				default :
					oldProdId="100000379";
					break;
				}
				com.alibaba.fastjson.JSONObject o=new com.alibaba.fastjson.JSONObject();
				String lastProdId="";
				for(int i=0;i<detailList.size();i++) {
					o=detailList.getJSONObject(i).getJSONObject("accProdInstDetail");
					prodId=o.getString("prodId");
					if(oldProdId.contains(prodId)){
						prodInstId=o.getString("prodInstId");
						lastProdId=prodId;
						if(!"100000379".equals(prodId)) {
							obstacleNum=o.getString("accNum");
						}
					}
				}
				switch (lastProdId){
				case "100000009":
				case "100000012":
				case "100000025":
					accType = "9";
					break;
				case "100000002":
					accType = "2";
					break;
				case "100000379":
					accType = "379";
					break;
				case "100000881":
					accType="881";
					break;
				default:
					break;
				}
				if(("itv,tel,kd,zh").contains(flag)) {
					String returnCode=dataUtil.getLocalCode(getRegion(regionId));
					String businessCode=dataUtil.getZDbusinessCodes(getRegion(regionId), prodId);
					String businessCname=dataUtil.getZDBusinessName(businessCode);
					StringBuilder str=new StringBuilder();
					str.append("&singleDealCode="+obstacleNum).append("&singleSpecialtyId="+businessCname).append("&singleNativenetId="+returnCode)
					.append("&singleOdsSpecType="+prodId).append("&singleProdId="+prodInstId).append("&singleAccount="+obstacleNum);
					linkUrl=str.toString();
				}
			}
		}

		result.put("obstacleNum", obstacleNum);
		result.put("linkUrl", linkUrl);
		result.put("accType", accType);
		return result;
	}
	
	public com.alibaba.fastjson.JSONObject getProdInst(String phone, String regionId) {
		JSONObject in=new JSONObject();
		in.put("accNbr", phone);
		in.put("areaCode", regionId);
		in.put("qryMode", "2");
		in.put("scopeInfos", "accProdInst");
		in.put("accType", "");
		String info=customerServiceFeign.getProdInst(in.toString());
		return com.alibaba.fastjson.JSON.parseObject(info);
	}
	
	public String getOnekeyToken(String staffcode, String areaCode, String callerNo, String pageType) {
        Map intfMap = pubFunc.getIntfMap("diagnosis");
        if(null == intfMap){
        	return "";
        }
		String oneGetTokenURI=intfMap.get("ADDRESS_IP").toString() + "/diag/diagnosis/do_user.do?jsondata=";
		String checkKey="123456";
		StringBuilder b=new StringBuilder();
		Date now=new Date();
		SimpleDateFormat s=new SimpleDateFormat("yyyyMMddHHmmss");
		String time=s.format(now);
		b.append(staffcode+"|").append(areaCode+"|").append(checkKey+"|").append(time+"|").append(pageType);
		String randKey=DESEncryptUtil.getMD5(b.toString());
		JSONObject j=new JSONObject();
		j.put("areaCode", areaCode);
		j.put("channelId", "10000");
		j.put("staffcode", staffcode);
		j.put("callNo", callerNo);
		j.put("randKey", randKey.toLowerCase());
		j.put("checkKey", checkKey);
		j.put("timestamp", time);
		j.put("pageType", pageType);
		String sb=j.toString();
		String oneDesKey = "__AUTH_HANDLE_$#*&@!&%$#@_)#)(_1030|";
		String entry=DESEncryptUtil.encrypt(sb, oneDesKey.substring(0, 8));
		b.delete(0, b.length());
		b.append(oneGetTokenURI).append(entry);
		JSONObject stJson=new JSONObject();
		stJson.put("url", b.toString());
		String token=customerServiceFeign.getZDSSOURL(stJson.toString());
		
		return intfMap.get("ADDRESS_IP").toString() + "/diag/diagnosis/do_auth.do?ticket=" + token;
	}

	@Override
	public PageResVO getReturnBlackList(Map inParams) {
		String strWhere = inParams.get("strWhere") == null ? "" : inParams.get("strWhere").toString();
		int begion = Integer.parseInt(inParams.get("begion").toString());
		int pageSize = Integer.parseInt(inParams.get("pageSize").toString());
		String realSql = "";
		JSONObject obj = JSONObject.fromObject(strWhere);
		String sqlCondition = " 1=1 ";
		if (StringUtils.isNotEmpty(obj.optString("mobilePhone"))) {
			sqlCondition += "AND r.MOBILE_PHONE='" + obj.optString("mobilePhone") + "'";
		}
		if (StringUtils.isNotEmpty(obj.optString("regionId"))) {
			sqlCondition += "AND r.REGION_NAME='" + obj.optString("regionId") + "'";
		}
		if (StringUtils.isNotEmpty(obj.optString("status"))) {
			sqlCondition += "AND r.STATE='" + obj.optString("status") + "'";
		}
		if (StringUtils.isNotEmpty(obj.optString("staffId"))) {
			sqlCondition += "AND r.STAFF_ID='" + obj.optString("staffId") + "'";
		}
		if (StringUtils.isNotEmpty(obj.optString("staffName"))) {
			sqlCondition += "AND r.STAFF_NAME='" + obj.optString("staffName") + "'";
		}
		if (StringUtils.isNotEmpty(obj.optString("effDate"))) {
			sqlCondition += " and r.MODIFIED_TIME between str_to_date('" + obj.optString("effDate") + "','%Y-%m-%d %H:%i:%s') and str_to_date('" + obj.optString("expDate") + "','%Y-%m-%d %H:%i:%s')";
		}
		String sql = "SELECT" +
				" r.ID," +
				" r.MOBILE_PHONE," +
				" (case when r.STATE = '1' then '有效/不回访' when r.STATE = '2' then '已删除/失效' end) STATE," +
				" DATE_FORMAT(r.MODIFIED_TIME,'%Y-%m-%d %H:%i:%s') MODIFIED_TIME," +
				" r.REGION_NAME," +
				" r.REMARK," +
				" r.STAFF_ID," +
				" r.STAFF_NAME," +
				" r.ORG_NAME," +
				" r.ORG_ID " +
				" FROM" +
				" cc_return_blacklist r  WHERE ";
		realSql = sql + sqlCondition;
		List returnBlackList = jt.queryForList(realSql);
		if(returnBlackList.size() > 10) {
			if(begion == 0) {
				pageSize = pageSize == 0 ? 10 : pageSize;
				realSql =  realSql+ " LIMIT " + begion + ", " + pageSize;
			}else {
				realSql =  realSql+" LIMIT " + (begion-1)*pageSize + ", " + pageSize;
			}
		}
		List realBlackList = jt.queryForList(realSql);
		PageResVO vo = new PageResVO();
		vo.setResultList(JSONArray.fromObject(JSON.parseArray(JSON.toJSONString(realBlackList))));
		vo.setTotalCount(returnBlackList.size());
		return vo;
	}
}