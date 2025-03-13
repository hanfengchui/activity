package com.timesontransfar.customservice.dbgridData.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.dbgridData.IdbgridDataTs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IDbgridDataFaceService;
import com.timesontransfar.customservice.dbgridData.IdbgridDataFace;
import com.timesontransfar.customservice.dbgridData.conditions.GridDateConditions;
import com.transfar.common.utils.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component(value="dbgridDataFaceService")
@SuppressWarnings("rawtypes")
public class DbgridDataFaceServiceImpl implements IDbgridDataFaceService {
	protected Logger log = LoggerFactory.getLogger(DbgridDataFaceServiceImpl.class);
	
	@Autowired
	private IdbgridDataFace dbgridDataFaceImpl;
	@Autowired
	private GridDateConditions gridDateConditions;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IdbgridDataTs dbgridDataTs;//投诉列表
	
	@Override
	public GridDataInfo getGridDataSunli(String funId,String strWhere,int begion) {
		GridDataInfo res = dbgridDataFaceImpl.getGridData(begion, strWhere, funId);
		return res;
	}
	
	public GridDataInfo getGridDataBySize(String funId,String strWhere,int begion,int pageSize) {
		GridDataInfo res = dbgridDataFaceImpl.getGridDataBySize(begion, pageSize, strWhere, funId);
		List list=getLabelImg(res);
		res.setList(list);
		return res;
	}

	public GridDataInfo getBatchGridDataBySize(String funId,String strWhere,int begion,int pageSize) {
		GridDataInfo res = dbgridDataFaceImpl.getBatchGridDataBySize(begion, pageSize, strWhere, funId);
		List list=getLabelImg(res);
		res.setList(list);
		return res;
	}

	public GridDataInfo getCallBackGridDataBySize(String funId,String strWhere,int begion,int pageSize) {
		GridDataInfo res = dbgridDataTs.getCallBackDealingSheetTs(begion, pageSize,strWhere);
		List list=getLabelImg(res);
		res.setList(list);
		return res;
	}

	@Override
	public GridDataInfo queryValue(String funId, String strWhere, int begion, int pageSize) {
		return dbgridDataFaceImpl.queryValue(funId, strWhere, begion, pageSize);
	}


	@Override
	public int deleteKey(List<String> redisKeys) {
		return dbgridDataFaceImpl.deleteKey(redisKeys);
	}

	@SuppressWarnings("unchecked")
	public List getLabelImg(GridDataInfo res) {
		String forceDesc = "{'70010102':'强制捆绑搭售业务','70010103':'强制单方面关停业务','70010104':'强制订制增值业务','70010107':'强制发送短信','70010105':'无资源'}";
		JSONObject forJson = JSONObject.fromObject(forceDesc);
		
		List<Map<String, Object>> list = res.getList();
		for(int i=0; i<list.size(); i++) {
			String beginDate = this.getStringByKey(list.get(i), "BEGIN_DATE");
			String orderDate = this.getStringByKey(list.get(i), "ACCEPT_DATE");
			String endDate = this.getStringByKey(list.get(i), "END_DATE");
			int hangupTimeCount = this.getIntByKey(list.get(i), "HANGUP_TIME_COUNT");
			int serviceType = this.getIntByKey(list.get(i), "SERVICE_TYPE");
			String sysDate = this.getStringByKey(list.get(i), "SYS_DATE");
			int dealLimitTime = this.getIntByKey(list.get(i), "DEAL_LIMIT_TIME");
			int orderLimitTime = this.getIntByKey(list.get(i), "ORDER_LIMIT_TIME");
			int orderTimeCount = this.getIntByKey(list.get(i), "HANGUP_ORDER_COUNT");
			int comeCategory = this.getIntByKey(list.get(i), "COME_CATEGORY");
			int orderType = comeCategory == 707907001 ? serviceType : 0;
			
			int workTime = pubFunc.getWorkingTime(beginDate, orderDate, endDate, hangupTimeCount * 60, serviceType, sysDate);
			String workingEnd = pubFunc.getWorkingEnd(beginDate, orderDate, dealLimitTime, hangupTimeCount * 60, serviceType, sysDate);
			String nextHour = "";//服务单预计完成时间
			if(orderLimitTime > 0) {
				nextHour = pubFunc.getWorkingEnd(orderDate, orderDate, orderLimitTime, orderTimeCount * 60, orderType, sysDate);
			}
			
			String pdBeginDate = this.getStringByKey(list.get(i), "PD_BEGIN_DATE");
			int pdlimit = this.getIntByKey(list.get(i), "PDLIMIT");
			int locallefttime = 0;
			if (!StringUtils.isEmpty(pdBeginDate) && 0 != pdlimit) {
				int pdTime = pubFunc.getWorkingTime(pdBeginDate, orderDate, sysDate, 0, serviceType, sysDate);
				if (pdlimit * 60 > pdTime) {
					locallefttime = (pdlimit * 60 - pdTime) / 60;
				}
			}
			
			int acceptChannelId = this.getIntByKey(list.get(i), "ACCEPT_CHANNEL_ID");
			String acceptComeFromDesc = this.getStringByKey(list.get(i), "ACCEPT_COME_FROM_DESC");
			boolean flag = pubFunc.isYueJi(acceptChannelId);
			if (flag && (acceptComeFromDesc.indexOf("★") == -1)) {
				acceptComeFromDesc = "★" + acceptComeFromDesc;
			}
			
			JSONArray arr = this.getJSONArray(list.get(i), forJson);
			String titleObj = this.getTitleObj(list.get(i), workTime);
			
			Map m = new HashMap();
			m.putAll(list.get(i));
			m.put("imgObj", arr);
			m.put("titleObj", titleObj);
			m.put("COME_FROM_DESC_MAIN", acceptComeFromDesc);
			m.put("WORKTIME", workTime);
			m.put("WORKING_END", workingEnd);
			m.put("LOCALLEFTTIME", locallefttime);
			m.put("NEXT_HOUR", nextHour);
			list.set(i, m);
		}
		res.setList(list);
		return list;
	}
	
	private JSONArray getJSONArray(Map<String, Object> map, JSONObject forJson) {
		String forcePreFlag = this.getStringByKey(map, "FORCE_PRE_FLAG");
		String upGradeIncine = this.getStringByKey(map, "UPGRADE_INCLINE");
		String hotlineFlag = this.getStringByKey(map, "HOTLINE_FLAG");
		int secFlag = this.getIntByKey(map, "SEC_FLAG");
		String xhzwFlag = this.getStringByKey(map, "XHZW_FLAG");
		int xcIngNum = this.getIntByKey(map, "XC_ING_NUM");
		String sheetType = this.getStringByKey(map, "SHEET_TYPE");
		
		JSONArray arr = new JSONArray();
		JSONObject json = new JSONObject();
		if(!"".equals(forcePreFlag) && !"70010106".contentEquals(forcePreFlag) && !"null".contentEquals(forcePreFlag)){
			json.put("title", forJson.optString(forcePreFlag));
			json.put("url", "force.gif");
			arr.add(json);
		}
		if("1".equals(upGradeIncine)){
			json.put("title", "升级倾向");
			json.put("url", "upgrade_2.gif");
			arr.add(json);
		}
		if("1".equals(hotlineFlag)){
			json.put("title", "致电省市总");
			json.put("url", "hotline.gif");
			arr.add(json);
		}
		if(secFlag > 0){
			json.put("title", "集团二次派单");
			json.put("url", "secFlag.png");
			arr.add(json);
		}
		if("1".equals(xhzwFlag)){
			json.put("title", "携号转网");
			json.put("url", "xhzwFlag.png");
			arr.add(json);
		}
		if (xcIngNum == 0) {
			json.put("title", "发起协查已回复");
			json.put("url", "xcgr.png");
			arr.add(json);
		} else if (xcIngNum > 0) {
			json.put("title", "发起协查未回复");
			json.put("url", "xcrr.png");
			arr.add(json);
		}
		if("720130028".equals(sheetType) || "720130029".equals(sheetType)){
			json.put("title", "收到协查未回复");
			json.put("url", "xcrl.png");
			arr.add(json);
		}
		return arr;
	}
	
	private String getTitleObj(Map<String, Object> map, int workTime) {
		String adjustAccountFlag = this.getStringByKey(map, "ADJUST_ACCOUNT_FLAG");
		String directDispatchFlag = this.getStringByKey(map, "DIRECT_DISPATCH_FLAG");
		String repeatNewFlag = this.getStringByKey(map, "REPEAT_NEW_FLAG");
		String xhzwFlag = this.getStringByKey(map, "XHZW_FLAG");
		String upTendencyFlag = this.getStringByKey(map, "UP_TENDENCY_FLAG");
		String appealReasonId = this.getStringByKey(map, "APPEAL_REASON_ID");
		int dealLimitTime = this.getIntByKey(map, "DEAL_LIMIT_TIME");
		int hastenNum = this.getIntByKey(map, "HASTENT_NUM");
		String custType = this.getStringByKey(map, "CUST_TYPE");
		String prodTwo = this.getStringByKey(map, "PROD_TWO");
		String ruyiFlag = this.getStringByKey(map, "RUYI_FLAG");
		String orderType = this.getStringByKey(map, "ORDER_TYPE");
		String returnCode = this.getStringByKey(map, "RETURN_CODE");
		int refundFlag = this.getIntByKey(map, "REFUND_FLAG");
		int sensitiveNum = this.getIntByKey(map, "SENSITIVE_NUM");

		String title = "";
		if (checkZPDH(appealReasonId)) {
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">诈</font>";
		}
		if(!"0".equals(adjustAccountFlag)) {
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">调</font>";
		}
		if(!"0".equals(directDispatchFlag)) {
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">直</font>";
		}
		if("1".equals(repeatNewFlag)){
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">重</font>";
		}
		if("1".equals(orderType)){
			//最严改急
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">急</font>";
		}
		if("1".equals(ruyiFlag)){
			//如意用户
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">如</font>";
		}
		if("1".equals(returnCode)){
			//已回传
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">回</font>";
		}
		String refundFlagStr = this.getRefundFlag(refundFlag);
		if(!"".equals(refundFlagStr)){
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">" + refundFlagStr + "</font>";
		}
		if(sensitiveNum > 0){
			String sensitiveNumLabel = this.getSensitiveNumLabel(sensitiveNum);
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">" + sensitiveNumLabel + "</font>";
		}
		if("1".equals(xhzwFlag) || !"0".equals(upTendencyFlag) || check5G(prodTwo) || checkZPDH(appealReasonId)) {
			//1.有“携”标识 2.越级倾向 3.受理目录为5G的工单
			//2021-6-1，新增防诈
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">③</font>";
		}
		else if("1".equals(repeatNewFlag) || dealLimitTime*3600 < workTime || hastenNum > 1) {
			//1.重复投诉 2.超时工单 3.催单次数达到2次
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">②</font>";
		}
		else if("1".equals(custType)) {
			//有特殊用户标识
			title += "&nbsp;<font style=\"color: #ff0000;font-weight:bold\">①</font>";
		}
		return title;
	}
	
	private String getSensitiveNumLabel(int sensitiveNum) {
		if(sensitiveNum == 1) {
			return "敏1";
		} else if(sensitiveNum == 2) {
			return "敏2";
		} else if(sensitiveNum >= 3) {
			return "敏3+";
		}
		return "";
	}
	
	// 退费状态 1：未审核、2：审核不通过、3：审核通过（退）、4：24小时未到账（超）
	private String getRefundFlag(int refundFlag) {
		String flag = "";
		if(refundFlag == 3) {
			flag = "退";
		} else if(refundFlag == 4) {
			flag = "超";
		}
		return flag;
	}

	// 判断是否是诈骗电话
	private boolean checkZPDH(String appealReasonId) {
		return "11504".equals(appealReasonId) || "11505".equals(appealReasonId);
	}

	// 判断是否是5G业务
	private boolean check5G(String prodTwo) {
		return "50101".equals(prodTwo) || "501002".equals(prodTwo);
	}

	private String getStringByKey(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}
	
	private int getIntByKey(Map map, String key) {
		return map.get(key) == null ? 0 : Integer.parseInt(map.get(key).toString());
	}

	@Override
	public GridDataInfo getGridData(String funId, String strWhere, int begion) {
		String conditions = gridDateConditions.getStrWhere(funId, strWhere);
		GridDataInfo res = dbgridDataFaceImpl.getGridData(begion, conditions, funId);
		return res;
	}

	@Override
	public int addRow(Map<String, String> newRowData) {
		return dbgridDataFaceImpl.addRow(newRowData);
	}

	@Override
	public int getApportion(String staffId) {
		return dbgridDataFaceImpl.getApportion(staffId);
	}

	@Override
	public GridDataInfo getApportionData(String staffId,String orgId,String status,String appStatus,int begin,int pageSize) {
		return dbgridDataFaceImpl.getApportionData(staffId,orgId,status,appStatus,begin,pageSize);
	}

	@Override
	public int saveApportion(String staffId, String staffName, String orgId, String orgName, String apportionNumber) {
		return dbgridDataFaceImpl.saveApportion(staffId,staffName,orgId,orgName,apportionNumber);
	}

	@Override
	public String updateApportion(String param) {
		return dbgridDataFaceImpl.updateApportion(param);
	}
}