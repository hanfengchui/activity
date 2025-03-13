package com.timesontransfar.customservice.chiefdesk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IDbgridDataChiefDeskService;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component(value="chiefDeskService")
public class ChiefOperatorDeskService implements IChiefOperatorDeskServiceService{

	@Autowired
	private IDbgridDataChiefDeskService deskGrid;
	@Autowired
	private ILabelManageDAO labelManageDAO;

	/**
	 * 申请审批页面， "申请审批" 列表查询
	 * @param params
	 * @return
	 */
	@Override
	public GridDataInfo querySingleList(Map<String, String> params){
		StringBuffer strWhere = new StringBuffer();
		String orderId = parseStr(params.get("serviceOrderId"));
		strWhere.append(orderId == null ? "" : " AND S.SERVICE_ORDER_ID = '"+orderId+"'");
		String prodNum = parseStr(params.get("proNum"));
		strWhere.append(prodNum == null ? "" : " AND S.PROD_NUM = '"+prodNum+"'");
		String region = parseStr(params.get("areaId"));
		strWhere.append(region == null ? "" : " AND S.REGION_ID = "+region);
		String statu = parseStr(params.get("dealStatu"));
		strWhere.append(statu == null ? "" : " AND A.APPLY_AUD_STATU = "+statu);
		String logName = parseStr(params.get("applyStaff"));
		strWhere.append(logName == null ? "" : " AND A.APPLY_STAFF = (SELECT F.STAFF_ID FROM TSM_STAFF F WHERE F.LOGONNAME = '"+logName+"')");
		
		String applyType = params.get("applyType");
		strWhere.append("&&").append(applyType);
		int begin = Integer.parseInt(parseStr(params.get("begin")));
		return deskGrid.querySheetStatuApply(begin, strWhere.toString());
	}
	
	/**
	 * 申请审批页面，批量列表查询
	 * @param params
	 * @return                
	 */
	@Override
	public GridDataInfo queryPatchList(Map<String, String> params){
		StringBuffer strWhere = new StringBuffer();
		String applyType = parseStr(params.get("applyType"));
		strWhere.append("selected".equals(applyType) ? "" : " AND A.APPLY_TYPE =" + applyType);
		String statu = parseStr(params.get("dealStatu"));
		strWhere.append(statu == null ? "" : " AND A.APPLY_AUD_STATU = "+statu);
		String logName = parseStr(params.get("applyStaff"));
		strWhere.append(logName == null ? "" : " AND A.APPLY_STAFF = (SELECT F.STAFF_ID FROM TSM_STAFF F WHERE F.LOGONNAME = '"+logName+"')");
		int begin = Integer.parseInt(parseStr(params.get("begin")));
		return deskGrid.queryPatch(begin, strWhere.toString());
	}
	
	/**
	 * 班长台页面，查询列表数据
	 * @param params
	 * @return
	 */
	@Override
	public GridDataInfo queryChiefDesk(Map<String, String> params) {
		StringBuilder where = new StringBuilder();
		String serviceOrderId = parseStr(params.get("serviceOrderId"));
		if (serviceOrderId != null) {
			where.append(" AND S.SERVICE_ORDER_ID='").append(serviceOrderId).append("'");
		}
		String prodNum = parseStr(params.get("prodNum"));
		if (prodNum != null) {
			where.append(" AND S.PROD_NUM='").append(prodNum).append("'");
		}
		String relaPhone = parseStr(params.get("relaPhone"));
		if (relaPhone != null) {
			where.append(" AND RELA_INFO='").append(relaPhone).append("'");
		}
		String dealLogonName = parseStr(params.get("dealLogonName"));
		if (dealLogonName != null) {
			where.append(" AND DEAL_STAFF=(SELECT TS.STAFF_ID FROM TSM_STAFF TS WHERE TS.LOGONNAME='").append(dealLogonName).append("')");
		}
		String regionId = parseStr(params.get("regionId"));
		if (regionId != null) {
			where.append(" AND S.REGION_ID=").append(regionId);
		}
		String serviceType = parseStr(params.get("serviceType"));
		if (serviceType != null && !"selected".equals(serviceType)) {
			where.append(" AND S.SERVICE_TYPE=").append(serviceType);
		}
		String sheetTach = parseStr(params.get("sheetTach"));
		if (sheetTach != null && !"selected".equals(sheetTach)) {
			where.append(" AND TACHE_ID=").append(sheetTach);
		}
		String sheetFlag = params.get("sheetFlag");
		if (sheetFlag != null) {
			if ("repeatts".equals(sheetFlag)) {// 重复投诉
				where.append(" AND REPEAT_AUTO_FLAG=1");
			} else if (sheetFlag.startsWith("uplevel")) {// 管控等级
				where.append(" AND UP_LEVEL=").append(sheetFlag.charAt(sheetFlag.length() - 1));
			}
		}
		String custGrade = parseStr(params.get("custGrade"));
		if (custGrade != null && !"selected".equals(custGrade)) {
			where.append(" AND CUST_SERV_GRADE=").append(custGrade);
		}

		String sheetType = parseStr(params.get("sheetType"));
		if (sheetType != null && !"selected".equals(sheetType)) {
			where.append(" AND SHEET_TYPE=").append(sheetType);
		}

		String sheetStatu = parseStr(params.get("sheetStatu"));
		if (sheetStatu != null && !"selected".equals(sheetStatu)) {
			where.append(" AND SHEET_STATU=").append(sheetStatu);
		}
		String orderHasten = parseStr(params.get("orderHasten"));
		if (orderHasten != null) {
			where.append(" AND (SELECT COUNT(1) FROM CC_HASTEN_SHEET PP WHERE PP.SERVICE_ORDER_ID = S.SERVICE_ORDER_ID) >=").append(orderHasten);
		}
		String acceptStart = parseStr(params.get("acceptStart"));
		String acceptEnd = parseStr(params.get("acceptEnd"));
		if (acceptStart != null && acceptEnd != null) {
			where.append(" AND ACCEPT_DATE BETWEEN DATE_FORMAT('");
			where.append(acceptStart);
			where.append("','%Y-%m-%d %H:%i:%s')AND DATE_FORMAT('");
			where.append(acceptEnd);
			where.append("','%Y-%m-%d %H:%i:%s')");
		}
		String lockStart = parseStr(params.get("lockStart"));
		String lockEnd = parseStr(params.get("lockEnd"));
		if (lockStart != null) {
			where.append(" AND LOCK_DATE BETWEEN DATE_FORMAT('");
			where.append(lockStart);
			where.append("','%Y-%m-%d %H:%i:%s')AND DATE_FORMAT('");
			where.append(lockEnd);
			where.append("','%Y-%m-%d %H:%i:%s')");
		}
		if (params.get("dataType") != null) {
			where.append("&&").append(params.get("dataType"));
		}
		int begin = Integer.parseInt(params.get("begin"));
		return deskGrid.queryChiefDesk(begin, where.toString());
	}

	/**
	 * 根据受理单号查询出该工单的管控级别，并组装出选项html字符串
	 * @param serviceOrderId
	 * @return
	 */
	public String buildLevleOption(String serviceOrderId){
		JSONArray arr = new JSONArray();
		if(null != serviceOrderId && serviceOrderId.length() > 0){
			ServiceLabel label = labelManageDAO.queryServiceLabelById(serviceOrderId, false);
			if(label != null){
				Integer upL = label.getUpLevel();
				int level = upL == null ? 1 : upL.intValue();
				level = level == 0 ? 1 : level;
				
				for(int i = level+1; i < 5; i++){
					JSONObject o = new JSONObject();
					o.put("label",  i + " 级");
					o.put("value", i);
					arr.add(o);
				}
			}
		}
		return arr.toString();
	}
	
	/**
	 * 解析请求中携带的参数
	 * @param request HttpServletRequest
	 * @param type    1 申请审批 列表查询;2 批量申请 列表查询
	 * @return
	 */
	public Map<String, String> buildParams(Map<String, String> maps, int type) {
		Map<String, String> map = new HashMap<String, String>();
		switch (type) {
		case 1:
			map.put("applyType",maps.get("applyType") == null ? null :  maps.get("applyType"));
			map.put("serviceOrderId",maps.get("serviceOrderId") == null ? null :  maps.get("serviceOrderId"));
			map.put("proNum",maps.get("proNum") == null ? null :  maps.get("proNum"));
			map.put("areaId",maps.get("areaId") == null ? null :  maps.get("areaId"));
			map.put("dealStatu",maps.get("dealStatu") == null ? null :  maps.get("dealStatu"));
			map.put("applyStaff", maps.get("applyStaff") == null ? null : maps.get("applyStaff"));
			map.put("begin", maps.get("begin") == null ? null : maps.get("begin"));
			break;
		case 2:
			map.put("applyType", maps.get("applyType") == null ? null : maps.get("applyType"));
			map.put("dealStatu", maps.get("dealStatu") == null ? null : maps.get("dealStatu"));
			map.put("applyStaff", maps.get("applyStaff") == null ? null : maps.get("applyStaff"));
			map.put("begin", maps.get("begin") == null ? null : maps.get("begin"));
			break;
		case 3:
			map.put("serviceOrderId", maps.get("serviceOrderId") == null ? null : maps.get("serviceOrderId"));
			map.put("prodNum", maps.get("prodNum") == null ? null : maps.get("prodNum"));
			map.put("relaPhone", maps.get("relaPhone") == null ? null : maps.get("relaPhone"));
			map.put("dealLogonName", maps.get("dealLogonName") == null ? null :maps.get("dealLogonName"));
			map.put("regionId", maps.get("regionId") == null ? null :maps.get("regionId"));
			map.put("serviceType", maps.get("serviceType") == null ? null :maps.get("serviceType"));
			map.put("sheetTach", maps.get("sheetTach") == null ? null :maps.get("sheetTach"));
			map.put("sheetFlag", maps.get("sheetFlag") == null ? null :maps.get("sheetFlag"));
			map.put("custGrade", maps.get("custGrade") == null ? null :maps.get("custGrade"));
			map.put("urgencyGrade", maps.get("urgencyGrade") == null ? null :maps.get("urgencyGrade"));
			map.put("sheetStatu", maps.get("sheetStatu") == null ? null :maps.get("sheetStatu"));
			map.put("sheetType", maps.get("sheetType") == null ? null :maps.get("sheetType"));
			map.put("acceptStart", maps.get("acceptStart") == null ? null :maps.get("acceptStart"));
			map.put("acceptEnd", maps.get("acceptEnd") == null ? null :maps.get("acceptEnd"));
			map.put("lockStart", maps.get("lockStart") == null ? null :maps.get("lockStart"));
			map.put("lockEnd", maps.get("lockEnd") == null ? null :maps.get("lockEnd"));
			map.put("orderHasten", maps.get("orderHasten") == null ? null :maps.get("orderHasten"));
			map.put("begin", maps.get("begin") == null ? null :maps.get("begin"));
			map.put("dataType", maps.get("dataType") == null ? null : maps.get("dataType"));
			break;
		default:
			break;
		}
		return map;
	}
	
	private static String wherePatchds = " AND W.SERVICE_TYPE = '%s' AND W.TACHE_ID = '%s' ";
	
	/**
	 * @param dspnum 需要分派的工单数量
	 * @param servType 服务类型
	 * @param tachid 当前环节
	 * @return 获取的工单号数组；没有查询到符合条件的结果时返回null
	 */
	@SuppressWarnings("rawtypes")
	public String[] getPatchdspSheets(int dspnum,int servType,int tachid){
		String where = String.format(wherePatchds, servType, tachid);
		List list = deskGrid.queryPatchdsp(where, dspnum);
		if(null == list || list.isEmpty()){
			return new String[0];
		}
		int len = list.size();
		String[] rst = new String[len];
		Map tmp = null;
		for(int i = 0; i < len; i++){
			tmp = (Map)list.get(i);
			rst[i] = tmp.get("WORK_SHEET_ID").toString()+"@"+tmp.get("REGION_ID").toString()+"@"+tmp.get("MONTH_FLAG").toString();
		}
		return rst;
	}
	
	/**
	 * 解析字符串
	 * @param str
	 * @return 如果str为null或去除首尾空格后长度为0，则返回null；否则返回去除首尾空格后的值
	 */
	private String parseStr(String str){
		if(null == str){
			return null;
		}
		String tmp = str.trim();
		if(tmp.length() == 0){
			return null;
		}
		return tmp;
	}

}
