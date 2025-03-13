package com.timesontransfar.customservice.dbgridData.conditions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;
import com.transfar.common.utils.StringUtils;

import net.sf.json.JSONObject;

@Component(value="gridDateConditions")
@SuppressWarnings("rawtypes")
public class GridDateConditions {
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IworkSheetBusi workSheetBusi;

	@SuppressWarnings("all")
	public String getStrWhere(String funId, String map) {
		String strWhere = "";
		if(StringUtils.isEmpty(map)) return strWhere;
		switch (funId) {
			case DbgridStatic.GRID_FUNID_ACCEPT_HISTORY:
				strWhere = this.acceptHistoryConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_HASTEN_SHEET:
				strWhere = this.hastenConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_CMP_CWORKSHEET:
				strWhere = this.cliquePoolConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_CMP_PWORKSHEET:
				strWhere = this.provinceSheetConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEETCHECK_ONROAD:
				strWhere = this.qualitySheetConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_APPEALLIST:
				strWhere = this.qualitySheetConditionsVerify(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_UPDATES:
				strWhere = this.qualitySheetConditionsCheckAppeal(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_FINISH:
				strWhere = this.qualitySheetConditionsHis(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK:
				strWhere = this.qualitySheetCheckConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_LABLE_TEMPLATE:
				strWhere = this.getLabelWhere(map);
				break;
			case DbgridStatic.GRID_FUNID_BANCICONIG_SHEET:
				strWhere = this.getBanCiConfigWhere(map);
				break;
			case DbgridStatic.GRID_FUNID_STAFFABILITY_SHEET:
				strWhere = this.getStaffAbilityWhere(map);
				break;
			case DbgridStatic.GRID_FUNID_STAFFSKILLLOCALNET_RULE:
				strWhere = this.getStaffSkillLocalNetWhere(map);
				break;
			case DbgridStatic.GRID_FUNID_STAFFWORKSHIFT:
				strWhere = this.qualityStaffWorkShiftConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_FLOWSKILLRELA:
				strWhere = this.qualityFlowSkillRelaConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_ZQ_CUST_DATA:
				strWhere = this.zqySheetCheckConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_LABLE_CANCEL:
				strWhere = this.labelCancelConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_RECALLSHEET:
				strWhere = this.recallSheetConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_SHEETSTATUAUTO:
				strWhere = this.sheetStatuAutoConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_SHEETSTATUAUTOBATCH:
				strWhere = this.sheetStatuAutoConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_SHEETSTATUAUTOBATCHHANG:
				strWhere = this.sheetStatuAutoConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_YN_SHEET:
				strWhere = this.dealHandSheetPoolConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_YN_MYSHEET:
				strWhere = this.dealHandMyTaskConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_YN_ALREADYSHEET:
				strWhere = this.dealHandReturnStaffConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_DEAL_PATCH_DATA:
				strWhere = this.getDealPatchStr(map);
				break;	
			case DbgridStatic.GRID_FUNID_ERR_ACCEPTSHEET:
				strWhere = this.errSheetAcceptConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_ERR_SHEETLIST:
				strWhere = this.errSheetListConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_YNDEALDATA:
				strWhere = this.YNDealWhere(map);
				break;	
			case DbgridStatic.GRID_FUNID_BACKDEALDATA:
				strWhere = this.getBackWhere(map);
				break;		
			case DbgridStatic.GRID_FUNID_SMS_SENDDATA:
				strWhere = this.smsSendListConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_TS_SPECLIAL_DATA:
				strWhere = this.getTSSpecialStr(map);
				break;
			case DbgridStatic.GRID_FUNID_JDRX_ZTDDATA:
				strWhere = this.jdrxSheetConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_SJMYTASKSHEETDATA:
				strWhere = this.sjSheetSheetConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEET_MONITOR:
				strWhere = this.monitorConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEET_SATISFY:
				strWhere = this.monitorSatisfyConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEET_MONITOR_STAFF:
				strWhere = this.monitorStaffConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_QUERY_SHEET_SATISFY_STAFF:
				strWhere = this.satisfyStaffConditions(map);
				break;
			case DbgridStatic.GRID_FUNID_MONITORS_EXPORT:
				strWhere = this.monitorExport(map);
				break;
			case DbgridStatic.GRID_FUNID_SATISFY_EXPORT:
				strWhere = this.satisfyExport(map);
				break;
			case DbgridStatic.GRID_FUNID_ZCQRYLIST:
				strWhere = this.getZcList(map);
				break;
			default:
				strWhere=map;
				break;
		}
		return strWhere;
	}
	
	private String monitorConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		StringBuffer strWhere=new StringBuffer();
		strWhere.append(" AND RECEIVE_ORG_ID = '" + obj.optString("orgId") + "'");
		if(StringUtils.isNotEmpty(obj.optString("areaId")) && !"2".equals(obj.optString("areaId"))){
			strWhere.append(" AND b.RECEIVE_REGION_ID = " + obj.optString("areaId"));
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strWhere.append("  AND b.service_type = " + obj.optString("servType"));
		}
		if(StringUtils.isNotEmpty(obj.optString("returnOrg"))){
			strWhere.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			strWhere.append(" CONCAT_WS('',(SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + obj.optString("returnOrg") + "'),'%')) ");
		}
		strWhere.append(" ORDER BY b.CREAT_DATE " + obj.optString("orderType"));
		return strWhere.toString();
	}

	private String monitorSatisfyConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		StringBuffer strWhere=new StringBuffer();
		strWhere.append(" AND RECEIVE_ORG_ID = '" + obj.optString("orgId") + "'");
		if(StringUtils.isNotEmpty(obj.optString("areaId")) && !"2".equals(obj.optString("areaId"))){
			strWhere.append(" AND b.REGION_ID = " + obj.optString("areaId"));
		}
		if(StringUtils.isNotEmpty(obj.optString("returnOrg"))){
			strWhere.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			strWhere.append(" CONCAT_WS('',(SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + obj.optString("returnOrg") + "'),'%')) ");
		}
		strWhere.append(" ORDER BY b.CREATE_DATE " + obj.optString("orderType"));
		return strWhere.toString();
	}
	
	private String monitorStaffConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		StringBuffer strWhere=new StringBuffer();
		strWhere.append(" AND RECEIVE_STAFF = '" + obj.optString("staffId") + "'");
		if(StringUtils.isNotEmpty(obj.optString("areaId")) && !"2".equals(obj.optString("areaId"))){
			strWhere.append(" AND b.RECEIVE_REGION_ID = " + obj.optString("areaId"));
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strWhere.append("  AND b.service_type = " + obj.optString("servType"));
		}
		if(StringUtils.isNotEmpty(obj.optString("returnOrg"))){
			strWhere.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			strWhere.append(" CONCAT_WS('',(SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + obj.optString("returnOrg") + "'),'%')) ");
		}
		strWhere.append(" ORDER BY b.CREAT_DATE " + obj.optString("orderType"));
		return strWhere.toString();
	}

	private String satisfyStaffConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		StringBuffer strWhere=new StringBuffer();
		strWhere.append(" AND RECEIVE_STAFF = '" + obj.optString("staffId") + "'");
		if(StringUtils.isNotEmpty(obj.optString("areaId")) && !"2".equals(obj.optString("areaId"))){
			strWhere.append(" AND b.region_id = " + obj.optString("areaId"));
		}
		if(StringUtils.isNotEmpty(obj.optString("returnOrg"))){
			strWhere.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			strWhere.append(" CONCAT_WS('',(SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + obj.optString("returnOrg") + "'),'%')) ");
		}
		strWhere.append(" ORDER BY b.create_date " + obj.optString("orderType"));
		return strWhere.toString();
	}
	
	private String sjSheetSheetConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		StringBuffer sb = new StringBuffer();
		sb.append(" 1=1 ");
		if(StringUtils.isNotEmpty(obj.optString("serviceType"))) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.SERVICE_TYPE = " + obj.optString("serviceType"));
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID='" + obj.optString("orderId") + "'") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("regionId"))) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.REGION_ID='" + obj.optString("regionId") + "'") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.PROD_NUM='" + obj.optString("prodNum") + "'") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("realInfo"))) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.RELA_INFO='" + obj.optString("realInfo") + "'") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("trAppoid"))) {
			sb.append(" AND CC_SERVICE_CONTENT_ASK.APPEAL_PROD_ID=" + obj.optString("trAppoid")) ;
		}
		if(StringUtils.isNotEmpty(obj.optString("secondDir"))) {
			sb.append(" AND CC_SERVICE_CONTENT_ASK.APPEAL_REASON_ID=" + obj.optString("secondDir"))  ;
		}
		if(StringUtils.isNotEmpty(obj.optString("custStar"))) {
			sb.append(" AND CC_ORDER_CUST_INFO.CUST_SERV_GRADE=" + obj.optString("custStar"))  ;
		}
		if(StringUtils.isNotEmpty(obj.optString("chaoShi"))) {
			if("1".equals(obj.optString("chaoShi"))){
				sb.append("  AND ( CC_WORK_SHEET.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, CC_WORK_SHEET.CREAT_DATE, NOW())/60 - CC_WORK_SHEET.HANGUP_TIME_COUNT ) < 0) ");
			}
			if("2".equals(obj.optString("chaoShi"))){
				sb.append("  AND ( CC_WORK_SHEET.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, CC_WORK_SHEET.CREAT_DATE, NOW())/60 - CC_WORK_SHEET.HANGUP_TIME_COUNT ) > 0) ");
			}
		}
		if(obj.optBoolean("isDealTimeFlag")){
			sb.append( " AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE > STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')");
			sb.append("  AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE < STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')");
		}
		return sb.toString();
	}
	
	private String jdrxSheetConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" AND 1 = 1 ";
		String  flag = obj.optString("acceptDate");
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			strwhere += " AND a.service_order_id = '" + obj.optString("orderId") +"'";
		}else if(flag!=null&&!flag.equals("null")){
			strwhere += " AND accept_date BETWEEN str_to_date('" + obj.optJSONArray("acceptDate").optString(0) + "', '%Y-%m-%d %H:%i:%s') AND str_to_date('" + obj.optJSONArray("acceptDate").optString(1) + "', '%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			String prodNum=obj.optString("prodNum");
			if (!prodNum.substring(0,1).equals("0")) {
				prodNum = "0" + prodNum;
			}
			strwhere += " AND (prod_num = '" + prodNum + "' OR prod_num = SUBSTR('" + prodNum + "',2))";
		}
		if(StringUtils.isNotEmpty(obj.optString("logonname"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("logonname"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND (a.accept_staff_id = " + tsmStaff.getId() + " OR b.hotline_staff = " + tsmStaff.getId() + ")";
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += " AND accept_org_id = '" + obj.optString("orgId") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("regionId"))){
			strwhere += " AND a.region_id = " + obj.optString("regionId");
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere += " AND service_type = " + obj.optString("servType");
		}
		return strwhere;
	}
	
	private String smsSendListConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" 1=1 ";
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += " AND A.ORG_ID= '" + obj.optString("orgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("relaPhone"))){
			strwhere += " AND A.RELAPHONE= '" + obj.optString("relaPhone") +"'";
		}
		return strwhere;
	}
	
	private String errSheetListConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" ";
		if(StringUtils.isNotEmpty(obj.optString("regionId"))){
			strwhere += " AND W.REGION_ID = " + obj.optString("regionId");
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			strwhere += " AND W.SERVICE_ORDER_ID = '" + obj.optString("orderId") +"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetId"))){
			strwhere += " AND W.WORK_SHEET_ID = '" + obj.optString("sheetId") +"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetStatu"))){
			strwhere += " AND W.SHEET_STATU="+Integer.parseInt(obj.optString("sheetStatu")); 
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND W.DEAL_DESC='"+obj.optString("prodNum")+"'"; 
		}
		if(StringUtils.isNotEmpty(obj.optString("startTime"))){
		    strwhere += " AND W.CREAT_DATE BETWEEN STR_TO_DATE('"+obj.optString("startTime")+"','%Y-%m-%d %H:%i:%s') "
		    		+ "AND STR_TO_DATE('"+obj.optString("endTime")+"','%Y-%m-%d %H:%i:%s')";
		}
		return strwhere;
	}
	
	private String errSheetAcceptConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" ";
		if(StringUtils.isNotEmpty(obj.optString("regionId"))){
			strwhere += " AND W.REGION_ID = " + obj.optString("regionId");
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			strwhere += " AND W.SERVICE_ORDER_ID = '" + obj.optString("orderId") +"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetId"))){
			strwhere += " AND W.WORK_SHEET_ID = '" + obj.optString("sheetId") +"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND W.DEAL_DESC = '" + obj.optString("prodNum") +"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetStatu"))){
			strwhere += " AND W.SHEET_STATU = " + obj.optString("sheetStatu");
		}
		if(obj.optBoolean("isDealTimeFlag")){
			strwhere += " AND W.STATU_DATE >= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +="  AND W.STATU_DATE <= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		return strwhere;
	}
	
	@SuppressWarnings("all")
	private String dealHandReturnStaffConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" ";
		if(StringUtils.isNotEmpty(obj.optString("trReasonId"))){
			strwhere += " AND A.APPEAL_REASON_ID=" + obj.optString("trReasonId");
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetStatus"))){
			strwhere += " AND B.SHEET_STATU=" + obj.optString("sheetStatus") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere += " AND A.SERVICE_TYPE=" + obj.optString("servType") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("trAppoid"))){
			strwhere += " AND A.APPEAL_PROD_ID=" + obj.optString("trAppoid") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("worksheetType"))){
			strwhere += " AND C.SHEET_TYPE="+obj.optString("worksheetType");
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			strwhere += " AND A.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderStatu"))){
			strwhere += " AND B.ORDER_STATU='"+obj.optString("orderStatu")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND B.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("realPhone"))){
			strwhere += " AND B.RELA_INFO='"+obj.optString("realPhone")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("urgencyGrade"))){
			 strwhere += " AND B.URGENCY_GRADE="+obj.optString("urgencyGrade");
		}
		if(StringUtils.isNotEmpty(obj.optString("acceptCome"))){
			strwhere += " AND B.ACCEPT_CHANNEL_ID="+obj.optString("acceptCome");
		}
		if(StringUtils.isNotEmpty(obj.optString("custBrand"))){
			strwhere += " AND D.CUST_BRAND="+obj.optString("custBrand");
		}
		if(StringUtils.isNotEmpty(obj.optString("regionBox"))){
			strwhere += " AND A.REGION_ID="+obj.optString("regionBox");
		}
		if(obj.optBoolean("isDealTimeFlag")){
			strwhere += " AND B.ACCEPT_DATE >= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +="  AND B.ACCEPT_DATE <= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("returnStaff"))){
			TsmStaff staff=pubFunc.getLogonStaffByLoginName(obj.optString("returnStaff"));
			if(StringUtils.isNotNull(staff)){
				strwhere += " AND C.RETURN_STAFF=" + staff.getId() ;
			}
		}
		return strwhere;
	}
	
	@SuppressWarnings("all")
	private String dealHandMyTaskConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" ";
		if(StringUtils.isNotEmpty(obj.optString("trReasonId"))){
			strwhere += " AND D.APPEAL_REASON_ID = " + obj.optString("trReasonId");
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetStatus"))){
			strwhere += " AND B.SHEET_STATU=" + obj.optString("sheetStatus") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere += " AND A.SERVICE_TYPE=" + obj.optString("servType") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("trAppoid"))){
			strwhere += " AND D.APPEAL_PROD_ID=" + obj.optString("trAppoid") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("worksheetType"))){
			strwhere += " AND B.SHEET_TYPE="+obj.optString("worksheetType");
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			strwhere += " AND A.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderStatu"))){
			strwhere += " AND A.ORDER_STATU='"+obj.optString("orderStatu")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND A.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("realPhone"))){
			strwhere += " AND A.RELA_INFO='"+obj.optString("realPhone")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("urgencyGrade"))){
			 strwhere += " AND A.URGENCY_GRADE ="+obj.optString("urgencyGrade");
		}
		if(StringUtils.isNotEmpty(obj.optString("acceptCome"))){
			strwhere += " AND A.ACCEPT_CHANNEL_ID="+obj.optString("acceptCome");
		}
		if(StringUtils.isNotEmpty(obj.optString("custBrand"))){
			strwhere += " AND C.CUST_BRAND="+obj.optString("custBrand");
		}
		if(StringUtils.isNotEmpty(obj.optString("regionBox"))){
			strwhere += " AND A.REGION_ID="+obj.optString("regionBox");
		}
		if(StringUtils.isNotEmpty(obj.optString("urgencyGradeFlag")) && 1==obj.optInt("urgencyGradeFlag")){
			strwhere += "  AND(B.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, B.CREAT_DATE, NOW())/60 - B.HANGUP_TIME_COUNT ) < 0) ";
		}
		if(StringUtils.isNotEmpty(obj.optString("urgencyGradeFlag")) && 2==obj.optInt("urgencyGradeFlag")){
			strwhere += "  AND(B.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, B.CREAT_DATE, NOW())/60 - B.HANGUP_TIME_COUNT ) > 0) ";
		}
		if(obj.optBoolean("isDealTimeFlag")){
			strwhere += " AND A.ACCEPT_DATE  >= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +="  AND A.ACCEPT_DATE  <= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("returnStaff"))){
			TsmStaff staff=pubFunc.getLogonStaffByLoginName(obj.optString("returnStaff"));
			if(StringUtils.isNotNull(staff)){
				strwhere += " AND B.RETURN_STAFF=" + staff.getId() ;
			}
		}
		return strwhere;
	}
	
	@SuppressWarnings("all")
	private String dealHandSheetPoolConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" ";
		if(StringUtils.isNotEmpty(obj.optString("trReasonId"))){
			strwhere += " AND D.APPEAL_REASON_ID = " + obj.optString("trReasonId");
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetStatus"))){
			strwhere += " AND B.SHEET_STATU=" + obj.optString("sheetStatus") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere += " AND A.SERVICE_TYPE=" + obj.optString("servType") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("trAppoid"))){
			strwhere += " AND D.APPEAL_PROD_ID=" + obj.optString("trAppoid") ;
		}
		if(StringUtils.isNotEmpty(obj.optString("worksheetType"))){
			strwhere += " AND B.SHEET_TYPE="+obj.optString("worksheetType");
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			strwhere += " AND A.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderStatu"))){
			strwhere += " AND A.ORDER_STATU='"+obj.optString("orderStatu")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND A.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("realPhone"))){
			strwhere += " AND A.RELA_INFO='"+obj.optString("realPhone")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("urgencyGrade"))){
			 strwhere += " AND A.URGENCY_GRADE ="+obj.optString("urgencyGrade");
		}
		if(StringUtils.isNotEmpty(obj.optString("acceptCome"))){
			strwhere += " AND A.ACCEPT_CHANNEL_ID="+obj.optString("acceptCome");
		}
		if(StringUtils.isNotEmpty(obj.optString("custBrand"))){
			strwhere += " AND C.CUST_BRAND="+obj.optString("custBrand");
		}
		if(StringUtils.isNotEmpty(obj.optString("regionBox"))){
			strwhere += " AND A.REGION_ID="+obj.optString("regionBox");
		}
		if(StringUtils.isNotEmpty(obj.optString("urgencyGradeFlag")) && 1==obj.optInt("urgencyGradeFlag")){
			strwhere += "  AND(B.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, B.CREAT_DATE, NOW())/60 - B.HANGUP_TIME_COUNT ) < 0) ";
		}
		if(StringUtils.isNotEmpty(obj.optString("urgencyGradeFlag")) && 2==obj.optInt("urgencyGradeFlag")){
			strwhere += "  AND(B.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, B.CREAT_DATE, NOW())/60 - B.HANGUP_TIME_COUNT ) > 0) ";
		}
		if(obj.optBoolean("isDealTimeFlag")){
			strwhere += " AND A.ACCEPT_DATE  >= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +="  AND A.ACCEPT_DATE  <= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("returnStaff"))){
			TsmStaff staff=pubFunc.getLogonStaffByLoginName(obj.optString("returnStaff"));
			if(StringUtils.isNotNull(staff)){
				strwhere += " AND B.RETURN_STAFF=" + staff.getId() ;
			}
		}
		return strwhere;
	}
	private String sheetStatuAutoConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" 1=1 ";
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += " AND CC_SHEET_STATU_APPLY.APPLY_ORG='" + obj.optString("orgId") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			strwhere += " AND CC_WORK_SHEET.SERVICE_ORDER_ID='" + obj.optString("orderId") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetId"))){
			strwhere += " AND CC_WORK_SHEET.WORK_SHEET_ID='" + obj.optString("sheetId") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.PROD_NUM='" + obj.optString("prodNum") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("regionId"))){
			strwhere += " AND CC_WORK_SHEET.REGION_ID=" + obj.optString("regionId");
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetStatu"))){
			strwhere += " AND CC_WORK_SHEET.SHEET_STATU = " + obj.optString("sheetStatu");
		}
		if(obj.optBoolean("isDealTimeFlag") && !obj.optJSONArray("dealDate").isEmpty()){
			strwhere += " AND CC_WORK_SHEET.CREAT_DATE >= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere += " AND CC_WORK_SHEET.CREAT_DATE <= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		return strwhere;
	}
	private String recallSheetConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" 1=1 ";
		if(StringUtils.isNotEmpty(obj.optString("comeCategory"))){
			strwhere +=  " AND CC_SERVICE_ORDER_ASK.COME_CATEGORY = '" + obj.optString("comeCategory") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("comeFrom"))){
			strwhere +=  " AND CC_SERVICE_ORDER_ASK.ACCEPT_COME_FROM = '" + obj.optString("comeFrom") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("channel"))){
			strwhere +=  " AND CC_SERVICE_ORDER_ASK.ACCEPT_CHANNEL_ID = '" + obj.optString("channel") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("realPhone"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.RELA_INFO='"+obj.optString("realPhone")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.SERVICE_TYPE="+obj.optString("servType");
		}
		if(StringUtils.isNotEmpty(obj.optString("regionId"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.REGION_ID="+obj.optString("regionId");
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += " AND CC_WORK_SHEET.RECEIVE_ORG_ID='"+obj.optString("orgId")+"'";
		}
		if(obj.optBoolean("isDealTimeFlag")){
			strwhere += " AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE >= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +="   AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE <= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		return strwhere;
	}
	private String labelCancelConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" ";
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere +=  " AND a.prod_num = '" + obj.optString("prodNum") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("instanceId"))){
			strwhere +=  " AND a.instance_id = '" + obj.optString("instanceId") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("acceptType"))){
			strwhere +=  " AND a.instance_type = '" + obj.optString("acceptType") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere +=  " AND FIND_IN_SET('" + obj.optString("orgId") + "',b.region_id) = 1";
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere +=  " AND a.service_type = '" +  obj.optString("servType") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("comeCategory"))){
			strwhere +=  " AND a.come_category = '" + obj.optString("comeCategory") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("comeFrom"))){
			strwhere +=  " AND a.accept_come_from = '" + obj.optString("comeFrom") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("channel"))){
			strwhere +=  " AND a.accept_channel_id = '" + obj.optString("channel") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("labelClass"))){
			strwhere +=  " AND b.label_class_id = '" + obj.optString("labelClass") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("labelGroup"))){
			strwhere +=  " AND b.label_group_id = '" + obj.optString("labelGroup") + "'";
		}
		if(StringUtils.isNotEmpty(obj.optString("labelInfo"))){
			strwhere +=  " AND b.label_id = '" + obj.optString("labelInfo") + "'";
		}
		if(obj.optBoolean("isDealTimeFlag")){
			strwhere += "AND a.update_time >= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +=" AND a.update_time <= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		return strwhere;
	}
	private String qualityFlowSkillRelaConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" ";
		if(obj.optString("serviceType").equals("1")){
			strwhere += " cc_flow_skill_rela.service_date = '1'";
		}else{
			strwhere += " cc_flow_skill_rela.service_date = '3'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += "  AND cc_flow_skill_rela.flow_org_id ='" + obj.optString("orgId") +"'";
		}
		return strwhere;
	}
	private String qualityStaffWorkShiftConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" 1=1";
		if(obj.optBoolean("isDealTime")){
			strwhere += " AND CC_STAFF_WORK_SHIFT.WSS_WORK_DATE >= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +=" AND CC_STAFF_WORK_SHIFT.WSS_WORK_DATE <= STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += "  AND TSM_STAFF.ORG_ID = '" + obj.optString("orgId") +"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("loginName"))){
			strwhere += "  AND TSM_STAFF.LOGONNAME LIKE '" + obj.optString("loginName") +"%'";
		}
		return strwhere;
	}
	private String getStaffSkillLocalNetWhere(String map) {
		JSONObject obj=JSONObject.fromObject(map);
		String where=" 1=1 ";
		if(StringUtils.isNotEmpty(obj.optString("staffId"))){
			int staffId=pubFunc.getStaffId(obj.optString("staffId"));
			where += " AND cc_staff_skill.staff_id = " + staffId;
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			where += "  AND cc_staff_skill.org_id = '" + obj.optString("orgId") +"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("flowOrgId"))){
			where += "  AND cc_staff_skill.flow_org_id ='" + obj.optString("flowOrgId") +"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("tachId")) && !"SELECTED".equals(obj.optString("tachId"))){
			where += "  AND cc_staff_skill.tache_id = " + obj.optString("tachId");
		}
		if(StringUtils.isNotEmpty(obj.optString("skillType")) && !"SELECTED".equals(obj.optString("skillType"))){
			if (obj.optInt("serviceType")==1) {
				where += " AND cc_staff_skill.flow_org_id IN (SELECT flow_org_id FROM cc_flow_skill_rela WHERE service_date = '1' AND status = 'Y' AND skill_type = '" + obj.optString("skillType") + "')";
			} else {
				where += " AND cc_staff_skill.flow_org_id IN (SELECT flow_org_id FROM cc_flow_skill_rela WHERE service_date = '3' AND status = 'Y' AND skill_type = '" + obj.optString("skillType") + "')";
			}
		}
		if(StringUtils.isNotEmpty(where)){
			if (obj.optInt("serviceType")==1) {
				where += " AND cc_staff_skill.service_date = '1'";
			} else {
				where += " AND cc_staff_skill.service_date = '3'";
			}
		}
		return where;
	}
	private String getZcList(String map) {
		JSONObject obj=JSONObject.fromObject(map);
		String where=" 1=1 ";
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			where+=" AND  CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			where+="AND CC_SERVICE_ORDER_ASK.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		return where;
	}
	
	private String getLabelWhere(String map) {
		JSONObject obj=JSONObject.fromObject(map);
		String where="";
		if(StringUtils.isNotEmpty(obj.optString("areaId"))){
			where=  " AND A.REGION_ID LIKE '%" + obj.optString("areaId") + "%'";
		}else if(StringUtils.isNotEmpty(obj.optString("labelName"))){
			where += " AND A.LABEL_NAME LIKE '%" + obj.optString("labelName") + "%'";
		}
		return where;
	}
	private String getBanCiConfigWhere(String map) {
		JSONObject obj=JSONObject.fromObject(map);
		String where="";
		if(StringUtils.isNotEmpty(obj.optString("shiftName"))){
			where += " CC_WORK_SHIFT.WS_NAME like'" + obj.optString("shiftName") +"%'";
		}
		return where;
	}
	private String getStaffAbilityWhere(String map) {
		JSONObject obj=JSONObject.fromObject(map);
		String where=" 1=1 ";
		if(StringUtils.isNotEmpty(obj.optString("staffId"))){
			where += " AND TSM_STAFF.LOGONNAME like'" + obj.optString("staffId") +"%'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			where += " AND TSM_STAFF.ORG_ID = '" + obj.optString("orgId") +"'";
		}
		return where;
	}
	
	/**
	 * 组合受理单查询历史单查询条件
	 * @CreateDate 2020年6月5日 下午2:51:09
	 */
	private String acceptHistoryConditions(String map){
		Map m = new Gson().fromJson(map, HashMap.class);
		JSONObject obj = JSONObject.fromObject(m);
		return " AND (prod_num = '" + obj.optString("prodnum") + "' OR prod_num = SUBSTRING('" + obj.optString("prodnum") + "', 2)) AND region_id = " + obj.optString("quryRegion");
	}
	
	/**
	 * 查询催单详情
	 * Description: <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param map
	 * @return <br>
	 * @CreateDate 2020年6月5日 下午4:07:31 <br>
	 */
	private String hastenConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere="1=1";
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			 strwhere += " AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("acceptStaff"))){
			String logonName = obj.optString("acceptStaff");
			int staffId = pubFunc.getStaffId(logonName);
			strwhere += " AND CC_SERVICE_ORDER_ASK.ACCEPT_STAFF_ID='"+staffId+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("serviceTache"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.ORDER_STATU='"+obj.optString("serviceTache")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("acceptOrg"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.ACCEPT_ORG_ID='"+obj.optString("acceptOrg")+"'";
		}
		return strwhere;
	}
	private String cliquePoolConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere="";
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			 strwhere += " AND r.service_order_id='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND w.complaint_phone='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("complaintworksheetid"))){
			strwhere += " AND r.complaint_worksheet_id='"+obj.optString("complaintworksheetid")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("assigncode"))){
			strwhere += " and a.assign_code='"+obj.optString("assigncode")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("receivecode"))){
			strwhere += " and a.receiver_code='"+obj.optString("receivecode")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("custGroup"))){
			strwhere += "  and w.cust_type='"+obj.optString("custGroup")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("brandType"))){
			strwhere += "  and w.brand_type='"+obj.optString("brandType")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("selectCome"))){
			strwhere += "  and w.ask_source_srl='"+obj.optString("selectCome")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("assigntype"))){
			strwhere += " and r.assign_type ='"+obj.optString("assigntype")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("statu"))){
			strwhere += " and r.statu  ='"+obj.optString("statu")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("startTime"))){
			strwhere+=" and t.excutetime between str_to_date('"+obj.optString("startTime")+"','%Y-%m-%d %H:%i:%s') and str_to_date('"+obj.optString("endTime")+"','%Y-%m-%d %H:%i:%s')";
		}
		return strwhere;
	}
	private String provinceSheetConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere="";
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			 strwhere += " AND s.service_order_id='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND s.prod_num='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("worksheetid"))){
			strwhere += " AND w.work_sheet_id='"+obj.optString("worksheetid")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))){
			strwhere += " AND S.SERVICE_TYPE='"+obj.optString("servType")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("areaId")) && ! obj.optString("areaId").equals("0")){
			strwhere += " AND S.REGION_ID='"+obj.optString("areaId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("acceptCome")) && ! obj.optString("acceptCome").equals("0")){
			strwhere += " AND S.ACCEPT_COME_FROM='"+obj.optString("acceptCome")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("askChannelId")) && ! obj.optString("askChannelId").equals("0")){
			strwhere += " AND S.ACCEPT_CHANNEL_ID='"+obj.optString("askChannelId")+"'";
		}
		//S.SERVICE_TYPE
		if(StringUtils.isNotEmpty(obj.optString("applyStaff"))){
			strwhere +=" and A.APPLY_STAFF = (SELECT s.staff_id FROM tsm_staff s where s.logonname='"+obj.optString("applyStaff")+"')";
		}
		if(obj.optBoolean("isTimeFlag")){
			strwhere += " and a.aud_date between to_date('"+obj.optString("startTimes")+"','YYYY-MM-DD HH24:MI:SS')"
			 		+" and to_date('"+obj.optString("endTimes")+"','YYYY-MM-DD HH24:MI:SS')";
		}
		return strwhere;
	}
	
	@SuppressWarnings("all")
	private String qualitySheetConditions(String map){
		JSONObject obj = JSONObject.fromObject(map);
		String strwhere = "";
		if(StringUtils.isNotNull(obj.optJSONArray("acceptDate")) && !obj.optJSONArray("acceptDate").isEmpty()){
			strwhere += " AND S.ACCEPT_DATE > STR_TO_DATE('"+obj.optJSONArray("acceptDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
			strwhere += " AND S.ACCEPT_DATE < STR_TO_DATE('"+obj.optJSONArray("acceptDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotNull(obj.optJSONArray("dealDate")) && !obj.optJSONArray("dealDate").isEmpty()){
			strwhere += " AND C.CREAT_DATE > STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
			strwhere += " AND C.CREAT_DATE < STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			 strwhere += " AND s.service_order_id='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sfyzj2"))){//
			if (obj.optString("sfyzj2").equals("1")) {
		        strwhere += " AND EXISTS (SELECT 1 FROM CC_SHEET_CHECK C WHERE C.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID)";
		    } else if (obj.optString("sfyzj2").equals("2")) {
		        strwhere += " AND NOT EXISTS (SELECT 1 FROM CC_SHEET_CHECK C WHERE C.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID)";
		    }
		}
		if(StringUtils.isNotEmpty(obj.optString("tsDealResult")) && !obj.optString("tsDealResult") .equals("selected")){//
			strwhere += " AND L.TS_DEAL_RESULT="+obj.optString("tsDealResult");
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetId"))){
			strwhere += " AND C.WORK_SHEET_ID='"+obj.optString("sheetId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND S.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("REGION_ID"))){
			strwhere += " AND S.REGION_ID="+obj.optString("REGION_ID");
		}
		if(StringUtils.isNotEmpty(obj.optString("dealTachid"))){
			strwhere += " AND C.TACHE_ID ="+obj.optString("dealTachid");
		}
		if(StringUtils.isNotEmpty(obj.optString("trSubFrom"))){
			strwhere += " AND S.ACCEPT_COME_FROM ="+obj.optString("trSubFrom");
		}
		if(StringUtils.isNotEmpty(obj.optString("trChannel"))){
			strwhere += " AND S.ACCEPT_CHANNEL_ID ="+obj.optString("trChannel");
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += " AND S.ACCEPT_ORG_ID = '"+obj.optString("orgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("dealOrgId"))){
			strwhere += " AND C.DEAL_ORG_ID = '"+obj.optString("dealOrgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("accpstaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("accpstaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				 strwhere += " AND S.ACCEPT_STAFF_ID = '"+tsmStaff.getId()+"'";
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("dealstaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("dealstaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND C.DEAL_STAFF=" + tsmStaff.getId() ;
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))) {	
			 strwhere += " AND S.SERVICE_TYPE = "+obj.optString("servType");
		}  
		if(StringUtils.isNotEmpty(obj.optString("appealProd"))){
			strwhere += " AND A.APPEAL_PROD_ID="+obj.optString("appealProd");
		}
		if(StringUtils.isNotEmpty(obj.optString("trReasonId"))){
			strwhere += " AND A.APPEAL_REASON_ID="+obj.optString("trReasonId");
		}
		if(StringUtils.isNotEmpty(obj.optString("prodOne"))){
			strwhere += " AND A.PROD_ONE="+obj.optString("prodOne");
		}
		if(StringUtils.isNotEmpty(obj.optString("prodTwo"))){
			strwhere += " AND A.PROD_TWO="+obj.optString("prodTwo");
		}
		return strwhere;
	}
	
	@SuppressWarnings("all")
	private String qualitySheetConditionsHis(String map){
		JSONObject obj = JSONObject.fromObject(map);
		String strwhere = "";
		if(StringUtils.isNotNull(obj.optJSONArray("acceptDate")) && !obj.optJSONArray("acceptDate").isEmpty()){
			strwhere += " AND S.ACCEPT_DATE > STR_TO_DATE('"+obj.optJSONArray("acceptDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
			strwhere += " AND S.ACCEPT_DATE < STR_TO_DATE('"+obj.optJSONArray("acceptDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotNull(obj.optJSONArray("dealDate")) && !obj.optJSONArray("dealDate").isEmpty()){
			strwhere += " AND C.CREAT_DATE > STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
			strwhere += " AND C.CREAT_DATE < STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			 strwhere += " AND s.service_order_id='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sfyzj2"))){//
			if (obj.optString("sfyzj2").equals("1")) {
		        strwhere += " AND EXISTS (SELECT 1 FROM CC_SHEET_CHECK C WHERE C.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID)";
		    } else if (obj.optString("sfyzj2").equals("2")) {
		        strwhere += " AND NOT EXISTS (SELECT 1 FROM CC_SHEET_CHECK C WHERE C.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID)";
		    }
		}
		if(StringUtils.isNotEmpty(obj.optString("tsDealResult")) && !obj.optString("tsDealResult") .equals("selected")){//
			strwhere += " AND L.TS_DEAL_RESULT="+obj.optString("tsDealResult");
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetId"))){
			strwhere += " AND C.WORK_SHEET_ID='"+obj.optString("sheetId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("prodNum"))){
			strwhere += " AND S.PROD_NUM='"+obj.optString("prodNum")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("REGION_ID"))){
			strwhere += " AND S.REGION_ID="+obj.optString("REGION_ID");
		}
		if(StringUtils.isNotEmpty(obj.optString("dealTachid"))){
			strwhere += " AND C.TACHE_ID ="+obj.optString("dealTachid");
		}
		if(StringUtils.isNotEmpty(obj.optString("trSubFrom"))){
			strwhere += " AND S.ACCEPT_COME_FROM ="+obj.optString("trSubFrom");
		}
		if(StringUtils.isNotEmpty(obj.optString("trChannel"))){
			strwhere += " AND S.ACCEPT_CHANNEL_ID ="+obj.optString("trChannel");
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += " AND S.ACCEPT_ORG_ID = '"+obj.optString("orgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("dealOrgId"))){
			strwhere += " AND C.DEAL_ORG_ID = '"+obj.optString("dealOrgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("accpstaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("accpstaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				 strwhere += " AND S.ACCEPT_STAFF_ID = '"+tsmStaff.getId()+"'";
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("dealstaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("dealstaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND C.DEAL_STAFF=" + tsmStaff.getId() ;
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("servType"))) {	
			 strwhere += " AND S.SERVICE_TYPE = "+obj.optString("servType");
		} 
		if(StringUtils.isNotEmpty(obj.optString("appealProd"))){
			strwhere += " AND A.APPEAL_PROD_ID="+obj.optString("appealProd");
		}
		if(StringUtils.isNotEmpty(obj.optString("trReasonId"))){
			strwhere += " AND A.APPEAL_REASON_ID="+obj.optString("trReasonId");
		}
		if(StringUtils.isNotEmpty(obj.optString("prodOne"))){
			strwhere += " AND A.PROD_ONE="+obj.optString("prodOne");
		}
		if(StringUtils.isNotEmpty(obj.optString("prodTwo"))){
			strwhere += " AND A.PROD_TWO="+obj.optString("prodTwo");
		}
		return strwhere;
	}
	private String qualitySheetConditionsVerify(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere="";
		if(obj.optBoolean("timeChkFlag")){
			strwhere += " AND T.CREAT_DATE > STR_TO_DATE('"+obj.optJSONArray("checkDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +=" AND T.CREAT_DATE < STR_TO_DATE('"+obj.optJSONArray("checkDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			 strwhere += " AND T.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetId"))){
			 strwhere += " AND T.WORK_SHEET_ID='"+obj.optString("sheetId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkType"))){
			 strwhere += " AND T.TYPE_ID='"+obj.optString("checkType")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			 strwhere += " AND T.ORG_ID='"+obj.optString("orgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkOrgId"))){
			 strwhere += " AND T.CHECK_ORG_ID='"+obj.optString("checkOrgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkStaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("checkStaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND T.CHECK_STAFF_ID=" + tsmStaff.getId() ;
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("staff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("staff"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND T.STAFF_ID=" + tsmStaff.getId() ;
			}
		}
		return strwhere;
	}
	private String qualitySheetConditionsCheckAppeal(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere="";
		if(obj.optBoolean("tabFlag")){
			strwhere=" AND T.CHECK_STATE='600006271' "; // 质检单状态为：申诉修改";
		}else{
			strwhere=" AND T.CHECK_STATE='600006272' "; // 质检单状态为：申诉修改";
		}
		if(obj.optBoolean("timeChkFlag")){
			strwhere += " AND T.CREAT_DATE > STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere +=" AND T.CREAT_DATE < STR_TO_DATE('"+obj.optJSONArray("dealDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			 strwhere += " AND T.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetId"))){
			 strwhere += " AND T.WORK_SHEET_ID='"+obj.optString("sheetId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("qualityType"))){
			 strwhere += " AND T.TYPE_ID='"+obj.optString("qualityType")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("dealOrgId"))){
			 strwhere += " AND T.ORG_ID='"+obj.optString("dealOrgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkOrgId"))){
			 strwhere += " AND T.CHECK_ORG_ID='"+obj.optString("checkOrgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkStaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("checkStaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND T.CHECK_STAFF_ID=" + tsmStaff.getId() ;
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("dealstaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("dealstaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND T.STAFF_ID=" + tsmStaff.getId() ;
			}
		}
		return strwhere;
	}
	
	@SuppressWarnings("all")
	private String qualitySheetCheckConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere="";
//		if(obj.optBoolean("tabFlag")){
//			strwhere=" AND T.CHECK_STATE='600006271' "; // 质检单状态为：申诉修改";
//		}else{
//			strwhere=" AND T.CHECK_STATE='600006272' "; // 质检单状态为：申诉修改";
//		}
		if(obj.optBoolean("timeChkFlag")){
			strwhere += " AND A.CREAT_DATE > STR_TO_DATE('"+obj.optJSONArray("checkDate").optString(0)+"','%Y-%m-%d %H:%i:%s')";
		    strwhere += " AND A.CREAT_DATE < STR_TO_DATE('"+obj.optJSONArray("checkDate").optString(1)+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("orderId"))){
			 strwhere += " AND A.SERVICE_ORDER_ID='"+obj.optString("orderId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkType"))){
			 strwhere += " AND A.TYPE_ID='"+obj.optString("checkType")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkId"))){
			 strwhere += " AND A.CHECK_ID='"+obj.optString("checkId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("sheetId"))){
			 strwhere += " AND A.WORK_SHEET_ID='"+obj.optString("sheetId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("qualityType"))){
			 strwhere += " AND A.TYPE_ID='"+obj.optString("qualityType")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("orgId"))){
			strwhere += " AND A.ORG_ID='"+obj.optString("orgId")+"'";
		}

		if(StringUtils.isNotEmpty(obj.optString("dealOrgId"))){
			 strwhere += " AND A.ORG_ID='"+obj.optString("dealOrgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkOrgId"))){
			 strwhere += " AND A.CHECK_ORG_ID='"+obj.optString("checkOrgId")+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("checkStaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("checkStaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND A.CHECK_STAFF_ID=" + tsmStaff.getId() ;
			}else{
				strwhere += " AND A.CHECK_STAFF_ID is null";//decimal类型 当被检员工查询不到时，加入where条件为null 这样会有无数据显示
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("dealstaff"))){
			TsmStaff tsmStaff=pubFunc.getLogonStaffByLoginName(obj.optString("dealstaff"));
			if(StringUtils.isNotNull(tsmStaff)){
				strwhere += " AND A.STAFF_ID=" + tsmStaff.getId() ;
			}else{
				strwhere += " AND A.STAFF_ID is null";//当检员工查询不到时，加入where条件为null 这样会有无数据显示
			}
		}
		return strwhere;
	}
	
	private String zqySheetCheckConditions(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String where="";
		if(StringUtils.isNotEmpty(obj.optString("startTime"))){
			where+=	" AND CC_RECEIVE_CUSTINFO.UPLOADDATE >= str_to_date('"+obj.optString("startTime")+"','%Y-%m-%d %H:%i:%s') " +
					" AND CC_RECEIVE_CUSTINFO.UPLOADDATE <= str_to_date('"+obj.optString("endTime")+"','%Y-%m-%d %H:%i:%s') ";
		}
		if(StringUtils.isNotEmpty(obj.optString("callLoginName"))){
			where+=" AND CC_RECEIVE_CUSTINFO.CALLSTAFFCODE = '" + obj.optString("callLoginName") + "' ";
		}
		if(StringUtils.isNotEmpty(obj.optString("realPhone"))){
			where+=" AND CC_RECEIVE_CUSTINFO.REALPHONE = '" + obj.optString("realPhone") + "' ";	
		}
		if(StringUtils.isNotEmpty(obj.optString("dealLoginName"))){
			where+=" AND CC_RECEIVE_CUSTINFO.DEALSTAFFCODE = '" + obj.optString("dealLoginName") + "' ";
		}
		if(StringUtils.isNotEmpty(obj.optString("loginAreaId"))){
			switch (obj.optString("loginAreaId")) {
			case "10-11-30":
				where+=" AND (TSM_ORGANIZATION.LINKID NOT LIKE '10-11-92%' AND TSM_ORGANIZATION.LINKID NOT LIKE '10-11-362991%') ";
				break;
			case "10-11-92":
				where+=" AND (TSM_ORGANIZATION.LINKID NOT LIKE '10-11-30%' AND TSM_ORGANIZATION.LINKID NOT LIKE '10-11-362991%') ";
				break;
			case "10-11-362991":
				where+=" AND (TSM_ORGANIZATION.LINKID NOT LIKE '10-11-30%' AND TSM_ORGANIZATION.LINKID NOT LIKE '10-11-92%') ";
				break;
			default:
				break;
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("initFlag"))){
			
			where+= "  AND CC_RECEIVE_CUSTINFO.DEALSTATUS='"+obj.optString("initFlag")+"' ";

		}
		return where;	
	}
	
	@SuppressWarnings("all")
	private String getDealPatchStr(String map) {
		JSONObject obj=JSONObject.fromObject(map);
		String where=" 1=1 ";
		String servOrderId=obj.optString("servOrderId");
		String proNum=obj.optString("proNum");
		String realPhone=obj.optString("realPhone");
		String returnStaff=obj.optString("returnStaff");
		String worksheetType=obj.optString("worksheetType");
		String orderStatu=obj.optString("orderStatu");
		String areaId=obj.optString("areaId");
		String appealProdId=obj.optString("appealProdId");
		String appealReasonId=obj.optString("appealReasonId");
		String startTime=obj.optString("startTime");
		String endTime=obj.optString("endTime");
		String serviceType=obj.optString("serviceType");
		String areaName=obj.optString("areaName");
		String subStationName=obj.optString("subStationName");
		String custServGrade=obj.optString("custServGrade");
		if(!"".equals(custServGrade)) {
			where+=" AND CC_ORDER_CUST_INFO.CUST_SERV_GRADE ="+Integer.parseInt(custServGrade);
		}
		if(!"".equals(areaName)) {
			where+=" AND CC_SERVICE_ORDER_ASK.AREA_NAME like '%"+areaName+"%'";
		}
		if(!"".equals(subStationName)) {
			where+=" AND CC_SERVICE_ORDER_ASK.SUB_STATION_NAME like '%"+subStationName+"%'";
		}
		if(StringUtils.isNotEmpty(serviceType)){
			where+=" AND CC_SERVICE_ORDER_ASK.SERVICE_TYPE="+serviceType;
		}
		if(StringUtils.isNotEmpty(appealProdId)){
			where+=" AND CC_SERVICE_CONTENT_ASK.APPEAL_PROD_ID= "+Integer.parseInt(appealProdId);
		}
		if(StringUtils.isNotEmpty(appealReasonId)){
			where+=" AND CC_SERVICE_CONTENT_ASK.APPEAL_REASON_ID="+Integer.parseInt(appealReasonId);
		}
		if(StringUtils.isNotEmpty(startTime)){
			where+=" AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE > STR_TO_DATE('" + startTime + "','%Y-%m-%d %H:%i:%s')";
			where+=" AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE < STR_TO_DATE('" + endTime + "','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(servOrderId)){
			where+=" AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID='"+servOrderId+"'";
		}
		if(StringUtils.isNotEmpty(worksheetType)){
			where+=" AND CC_WORK_SHEET.SHEET_TYPE="+Integer.parseInt(worksheetType);
		}
		if(StringUtils.isNotEmpty(orderStatu)){
			where+=" AND CC_WORK_SHEET.SHEET_STATU="+Integer.parseInt(orderStatu);
		}
		if(StringUtils.isNotEmpty(proNum)){
			where+=" AND CC_SERVICE_ORDER_ASK.PROD_NUM='"+proNum+"'";
		}
		if(StringUtils.isNotEmpty(realPhone)){
			where+=" AND CC_SERVICE_ORDER_ASK.RELA_INFO='"+realPhone+"'";
		}
		if(StringUtils.isNotEmpty(areaId)){
			where+=" AND CC_SERVICE_ORDER_ASK.REGION_ID="+Integer.parseInt(areaId);
		}
		if(!"".equals(returnStaff)) {
			int staffId=workSheetBusi.getStaffId(returnStaff);
			if(staffId!=0) {
				where+=" AND CC_WORK_SHEET.RETURN_STAFF="+staffId;
			}
		}
		return where;
	}
	
	@SuppressWarnings("all")
	private String YNDealWhere(String map){
		JSONObject obj = JSONObject.fromObject(map);
		String strwhere = " 1=1 ";
		
		String orderStatu = obj.optString("orderStatu");
		String dealStaff = obj.optString("dealStaff");
		String appealProdId = obj.optString("appealProdId");
		String appealReasonId = obj.optString("appealReasonId");
		String startTime = obj.optString("startTime");
		String endTime = obj.optString("endTime");
		String worksheetType = obj.optString("worksheetType");
		String servOrderId = obj.optString("servOrderId");
		String orderLink = obj.optString("order_link");
		String proNum = obj.optString("proNum");
		String realPhone = obj.optString("realPhone");
		String urgencyGrade = obj.optString("urgencyGrade");
		String areaId = obj.optString("areaId");
		String urgencyGradeFlag = obj.optString("urgencyGradeFlag");
		if(StringUtils.isNotEmpty(obj.optString("orderStatu"))){
			strwhere += " AND CC_WORK_SHEET.SHEET_STATU=" + Integer.parseInt(orderStatu);
		}
		if(!"".equals(dealStaff)) {
			int staffId = workSheetBusi.getStaffId(dealStaff);
			if(staffId!=0) {
				strwhere += " AND CC_WORK_SHEET.DEAL_STAFF=" + staffId;
			}
		}
		if(StringUtils.isNotEmpty(obj.optString("appealProdId"))){
			strwhere += " AND CC_SERVICE_CONTENT_ASK.APPEAL_PROD_ID=" + Integer.parseInt(appealProdId);
		}
		if(StringUtils.isNotEmpty(obj.optString("appealReasonId"))){
			strwhere += " AND CC_SERVICE_CONTENT_ASK.APPEAL_REASON_ID =" + Integer.parseInt(appealReasonId);
		}
		if(StringUtils.isNotEmpty(obj.optString("startTime"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE > STR_TO_DATE('"+startTime+"','%Y-%m-%d %H:%i:%s')";
			strwhere += " AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE < STR_TO_DATE('"+endTime+"','%Y-%m-%d %H:%i:%s')";
		}
		if(StringUtils.isNotEmpty(obj.optString("worksheetType"))){
			strwhere += " AND CC_WORK_SHEET.SHEET_TYPE=" + Integer.parseInt(worksheetType);
		}
		if(StringUtils.isNotEmpty(obj.optString("servOrderId"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID='" + servOrderId+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("order_link"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.ORDER_STATU='" + orderLink+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("proNum"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.PROD_NUM='" + proNum+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("realPhone"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.RELA_INFO='" + realPhone+"'";
		}
		if(StringUtils.isNotEmpty(obj.optString("urgencyGrade"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.URGENCY_GRADE ="+ Integer.parseInt(urgencyGrade);
		}
		if(StringUtils.isNotEmpty(obj.optString("areaId"))){
			strwhere += " AND CC_SERVICE_ORDER_ASK.REGION_ID="+ Integer.parseInt(areaId);
		}
		if("1".equals(urgencyGradeFlag)) {
			strwhere += " AND (CC_WORK_SHEET.DEAL_LIMIT_TIME * 60 -(TIMESTAMPDIFF(SECOND,CC_WORK_SHEET.CREAT_DATE,NOW())/60 - CC_WORK_SHEET.HANGUP_TIME_COUNT) < 0) ";
		}else if("2".equals(urgencyGradeFlag)){
			strwhere += " AND (CC_WORK_SHEET.DEAL_LIMIT_TIME * 60 -(TIMESTAMPDIFF(SECOND,CC_WORK_SHEET.CREAT_DATE,NOW())/60 - CC_WORK_SHEET.HANGUP_TIME_COUNT) > 0) ";
		}
		return strwhere;
	}
	
	private String getBackWhere(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String strwhere=" 1=1 ";
		String servOrderId=obj.optString("servOrderId");
		String proNum=obj.optString("proNum");
		String accpStaff=obj.optString("accpStaff");
		String accpOrgid=obj.optString("accpOrgid");
		
		if(StringUtils.isNotEmpty(servOrderId)) {
			strwhere+=" AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID='"+servOrderId+"'";
		}
		if(StringUtils.isNotEmpty(proNum)) {
			strwhere+=" AND CC_SERVICE_ORDER_ASK.PROD_NUM='"+proNum+"'";
		}
		if(StringUtils.isNotEmpty(accpStaff)) {
			strwhere+=" AND CC_SERVICE_ORDER_ASK.ACCEPT_STAFF_NAME='"+accpStaff+"'";
		}
		if(StringUtils.isNotEmpty(accpOrgid)) {
			//String[] arr=accpOrgid.split(",");
			
			strwhere+=" AND CC_SERVICE_ORDER_ASK.ACCEPT_ORG_ID in ("+accpOrgid+")";
		}
		return strwhere;
	}
	
	private String monitorExport(String map){
		JSONObject getJson=JSONObject.fromObject(map);
		
		StringBuffer strWhere=new StringBuffer();
		String flag=getJson.optString("flag");
		if("staff".equals(flag)) {
			strWhere.append(" AND RECEIVE_STAFF in(" + getJson.optString("staffId") + ")");
		}else if("org".equals(flag)) {
			strWhere.append(" AND RECEIVE_ORG_ID in(" + getJson.optString("orgId") + ")");
		}
		if(StringUtils.isNotEmpty(getJson.optString("areaId")) && !"2".equals(getJson.optString("areaId"))){
			strWhere.append(" AND b.RECEIVE_REGION_ID = " + getJson.optString("areaId"));
		}
		if(StringUtils.isNotEmpty(getJson.optString("servType"))){
			strWhere.append("  AND b.service_type = " + getJson.optString("servType"));
		}
		if(StringUtils.isNotEmpty(getJson.optString("returnOrg"))){
			strWhere.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			strWhere.append(" CONCAT_WS('',( SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + getJson.optString("returnOrg") + "' ),'%')) ");
		}
		strWhere.append(" ORDER BY b.CREAT_DATE " + getJson.optString("orderType"));
		return strWhere.toString();
	}

	private String satisfyExport(String map){
		JSONObject getJson=JSONObject.fromObject(map);

		StringBuffer strWhere=new StringBuffer();
		String flag=getJson.optString("flag");
		if("staff".equals(flag)) {
			strWhere.append(" AND RECEIVE_STAFF in(" + getJson.optString("staffId") + ")");
		}else if("org".equals(flag)) {
			strWhere.append(" AND RECEIVE_ORG_ID in(" + getJson.optString("orgId") + ")");
		}
		if(StringUtils.isNotEmpty(getJson.optString("areaId")) && !"2".equals(getJson.optString("areaId"))){
			strWhere.append(" AND b.region_id = " + getJson.optString("areaId"));
		}
		if(StringUtils.isNotEmpty(getJson.optString("returnOrg"))){
			strWhere.append(" AND RETURN_ORG_ID IN( SELECT ORG_ID FROM TSM_ORGANIZATION A WHERE A.LINKID LIKE ");
			strWhere.append(" CONCAT_WS('',( SELECT LINKID FROM TSM_ORGANIZATION WHERE ORG_ID = '" + getJson.optString("returnOrg") + "' ),'%')) ");
		}
		strWhere.append(" ORDER BY b.create_date " + getJson.optString("orderType"));
		return strWhere.toString();
	}
	
	private String getTSSpecialStr(String map){
		JSONObject obj=JSONObject.fromObject(map);
		String  strwhere="";
		String regionId=obj.optString("regionId");
		String userName=obj.optString("userName");
		String acc=obj.optString("acc");
		if(StringUtils.isNotEmpty(regionId)) {
			strwhere+=" and  CC_ESPECIALLY_CUST.REGION_ID ="+Integer.parseInt(regionId);
		}
		if(StringUtils.isNotEmpty(userName)) {
			strwhere+=" and   CC_ESPECIALLY_CUST.CUST_NAME = '"+userName+"'";
		}
		if(StringUtils.isNotEmpty(acc)) {
			strwhere+="  and CC_ESPECIALLY_CUST.CUST_NUM = '"+acc+"'";
		}
		return strwhere;
	}
	public String getAnd(String in) {
		
		in="".equals(in) ? " ": " AND ";
		return in;
	}
	
}
