package com.timesontransfar.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.autoAccept.dao.IAutoAcceptDao;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IDbgridDataFaceService;
import com.timesontransfar.customservice.orderask.service.ServiceOrderQuery;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;

import net.sf.json.JSONObject;

@RestController
public class DbgridDataFaceController {
	protected Logger log = LoggerFactory.getLogger(DbgridDataFaceController.class);
	
	@Autowired
	private IAutoAcceptDao autoAcceptDao;
	@Autowired
	private IDbgridDataFaceService dbgridDataFaceService;
	@Autowired
	private IworkSheetBusi workSheetBusi;
	@Autowired
	private ServiceOrderQuery orderQury;

    @SuppressWarnings("rawtypes")
	@PostMapping(value = "/workflow/dbgridDataFace/getGridData")
	public GridDataInfo getGridData(@RequestBody(required=true) Map param) {
		String funId = param.get("funId").toString();
		String strWhere = param.get("strWhere")==null?"":param.get("strWhere").toString();
		int begion = Integer.parseInt(param.get("begion").toString());
		return dbgridDataFaceService.getGridData(funId,strWhere,begion);
	}
    
    /**
     * 商机预受理处理工单池
     */
    @SuppressWarnings({ "all" })
	@PostMapping(value = "/workflow/dbgridDataFace/getSJGridData")
	public GridDataInfo getSJGridData(
			@RequestParam(value="qryType", required=true)int qryType,
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="pageSize", required=true)int pageSize,
			@RequestParam(value="regionId", required=false)String regionId,
			@RequestParam(value="orderId", required=false)String orderId,
			@RequestParam(value="prodNum", required=false)String prodNum,
			@RequestParam(value="relaInfo", required=false)String relaInfo,
			@RequestParam(value="returnStaff", required=false)String returnStaff,
			@RequestParam(value="custBrand", required=false)String custBrand,
			@RequestParam(value="urgencyGrade", required=false)String urgencyGrade,
			@RequestParam(value="juXiang", required=false)String juXiang,
			@RequestParam(value="sheetType", required=false)String sheetType,
			@RequestParam(value="firstDir", required=false)String firstDir,
			@RequestParam(value="secondDir", required=false)String secondDir,
			@RequestParam(value="chaoShi", required=false)String chaoShi,
			@RequestParam(value="sheetStatus", required=false)String sheetStatus,
			@RequestParam(value="custStar", required=false)String custStar,
			@RequestParam(value="serviceType", required=false)String serviceType,
			@RequestParam(value="orderDate", required=false)String orderDate) {
    	StringBuffer sb = new StringBuffer();
    	if(qryType == 0) {
    		//工单池
    		sb.append(" AND CC_WORK_SHEET.LOCK_FLAG = 0 ");    
    	} else {
    		//我的任务
    		sb.append(" AND CC_WORK_SHEET.LOCK_FLAG IN (1, 3) ");
    	}
    	
    	if(StringUtils.isNotEmpty(serviceType)) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.SERVICE_TYPE = " + serviceType);
		}
    	if(StringUtils.isNotEmpty(orderId)) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.SERVICE_ORDER_ID='" + orderId + "'") ;
		}
    	if(StringUtils.isNotEmpty(regionId)) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.REGION_ID='" + regionId + "'") ;
		}
    	if(StringUtils.isNotEmpty(prodNum)) {
			sb.append(" AND CC_SERVICE_ORDER_ASK.PROD_NUM='" + prodNum + "'") ;
		}
    	if(StringUtils.isNotEmpty(relaInfo)){
			sb.append(" AND CC_SERVICE_ORDER_ASK.RELA_INFO='" + relaInfo +"'") ;
		}
    	if(StringUtils.isNotEmpty(returnStaff)){
    		int staffId = workSheetBusi.getStaffId(returnStaff);
    		sb.append(" AND CC_WORK_SHEET.RETURN_STAFF=" + staffId ) ;
		}
    	if(StringUtils.isNotEmpty(custBrand)){
			sb.append(" AND CC_ORDER_CUST_INFO.CUST_BRAND='" + custBrand +"'") ;
		}
    	if(StringUtils.isNotEmpty(urgencyGrade)){
			sb.append(" AND CC_SERVICE_ORDER_ASK.URGENCY_GRADE=" + urgencyGrade ) ;
		}
    	if(StringUtils.isNotEmpty(juXiang)){
			sb.append(" AND CC_ORDER_CUST_INFO.BRANCH_NO='" + juXiang +"'") ;
		}
    	if(StringUtils.isNotEmpty(sheetType)) {
			sb.append(" AND CC_WORK_SHEET.SHEET_TYPE=" + sheetType ) ;
		}
    	if(StringUtils.isNotEmpty(firstDir)) {
			sb.append(" AND CC_SERVICE_CONTENT_ASK.APPEAL_PROD_ID='" + firstDir + "'") ;
		}
    	if(StringUtils.isNotEmpty(secondDir)) {
			sb.append(" AND CC_SERVICE_CONTENT_ASK.APPEAL_REASON_ID='" + secondDir + "'") ;
		}
    	if(StringUtils.isNotEmpty(orderDate)) {
			String[] ar = orderDate.split(",");
			sb.append(" AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE > '" + ar[0] + "'") ;
			sb.append(" AND CC_SERVICE_ORDER_ASK.ACCEPT_DATE < '" + ar[1] + "'") ;
		}
    	if(StringUtils.isNotEmpty(chaoShi)) {
			if(chaoShi.equals("1")) {
				sb.append(" AND ( CC_WORK_SHEET.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, CC_WORK_SHEET.CREAT_DATE, NOW())/60 - CC_WORK_SHEET.HANGUP_TIME_COUNT ) < 0) ");
			}
			if(chaoShi.equals("2")) {
				sb.append(" AND ( CC_WORK_SHEET.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, CC_WORK_SHEET.CREAT_DATE, NOW())/60 - CC_WORK_SHEET.HANGUP_TIME_COUNT ) > 0) ");
			}
		}
    	if(StringUtils.isNotEmpty(sheetStatus)) {
			sb.append(" AND CC_WORK_SHEET.SHEET_STATU=" + sheetStatus) ;
		}
    	if(StringUtils.isNotEmpty(custStar)) {
			sb.append(" AND CC_ORDER_CUST_INFO.CUST_SERV_GRADE=" + custStar) ;
		}
    	
    	String strWhere = sb.toString();
		log.info("getSJGridData strWhere:\n{}",strWhere);
		GridDataInfo info =dbgridDataFaceService.getGridData("YUSHOULI_SJ",strWhere,begion);
		if(info != null){
			log.info("商机预受理返回:\n{}",JSONObject.fromObject(info));
			//加预警超时逻辑
			List ls = info.getList();
			List tmpList = new ArrayList();
			for(int i=0;i<ls.size();i++) {
				Map m = (Map)ls.get(i);
				int dealDate = m.get("DEAL_DATE") == null ? 0:  Integer.valueOf(m.get("DEAL_DATE").toString());
				int dealLimitTime = m.get("DEAL_LIMIT_TIME") == null ? 0: Integer.valueOf(m.get("DEAL_LIMIT_TIME").toString());
				int preValue = m.get("SHEET_PRI_VALUE") == null ? 0:  Integer.valueOf(m.get("SHEET_PRI_VALUE").toString());
				int tx = dealDate / 60;//历时时长
				if(tx < 0)tx = 0;
				
				int finDate = dealLimitTime - tx;//剩余时长
				if(finDate < 0)finDate = 0;
				
				if( 0 < (dealDate - dealLimitTime * 60)  ){
					//实际处理时间大于工单处理时限
				    m.put("CHAO_SHI_FLAG", "超时(" + finDate +"小时)");
				}else if((dealLimitTime * 60 - dealDate) < (preValue * 60)) {
					//处理时限 - 实际处理时间 小于预警时间
					m.put("CHAO_SHI_FLAG", "预警(" + finDate +"小时)");
				}else {
					m.put("CHAO_SHI_FLAG", "未超时(" + finDate +"小时)");
				}
				tmpList.add(m);
			}
			ls.clear();
			info.setList(tmpList);
		}
    	return info;
    }

	/**
	 * 人工批量分派工单池
	 */
	@PostMapping(value = "/workflow/dbgridDataFace/getPoolGridDataFP")
	public GridDataInfo getPoolGridDataFP(@RequestParam(value = "begion", required = true) int begion,
			@RequestParam(value = "pageSize", required = true) int pageSize,
			@RequestParam(value = "funId", required = true) String funId,
			@RequestParam(value = "orderId", required = false) String orderId,
			@RequestParam(value = "prodNum", required = false) String prodNum,
			@RequestParam(value = "realInfo", required = false) String realInfo,
			@RequestParam(value = "returnStaff", required = false) String returnStaff,
			@RequestParam(value = "regionId", required = false) String regionId,
			@RequestParam(value = "chaoShi", required = false) String chaoShi,
			@RequestParam(value = "servType", required = false) String servType,
			@RequestParam(value = "sheetStatu", required = false) String sheetStatu,
			@RequestParam(value = "trAppoid", required = false) String trAppoid,
			@RequestParam(value = "secondDir", required = false) String secondDir,
			@RequestParam(value = "thirdDir", required = false) String thirdDir,
			@RequestParam(value = "bestOrder", required = false) String bestOrder,
			@RequestParam(value = "trSubFrom", required = false) String trSubFrom,
			@RequestParam(value = "trChannel", required = false) String trChannel,
			@RequestParam(value = "channelDetailId", required = false) String channelDetailId,
			@RequestParam(value = "areaName", required = false) String areaName,
			@RequestParam(value = "subStationName", required = false) String subStationName,
			@RequestParam(value = "acceptContent", required = false) String acceptContent,
			@RequestParam(value = "channel", required = false) String channel,
			@RequestParam(value = "products", required = false) String products,
			@RequestParam(value = "orderDate", required = false) String orderDate,
			@RequestParam(value = "shuniu", required = false) String shuniu,
			@RequestParam(value = "trChannelDetail", required = false) String trChannelDetail,
			@RequestParam(value = "sheetType", required = false) String sheetType,
			@RequestParam(value = "areaId", required = false) String areaId,
			@RequestParam(value = "custServGrade", required = false) String custServGrade,
			@RequestParam(value = "hastenCount", required = false) String hastenCount,
			@RequestParam(value = "upTendencyFlag", required = false) String upTendencyFlag) {
		StringBuffer sb = new StringBuffer();
		this.setServiceOrderAskWhere1(sb, shuniu, trSubFrom, trChannel, trChannelDetail, orderId, regionId);
		this.setServiceOrderAskWhere2(sb, prodNum, servType, realInfo, orderDate, areaName, subStationName);
		this.setServiceOrderAskWhere4(sb, chaoShi, acceptContent, trAppoid, secondDir);
		setMultipleWhere(sb, bestOrder, areaId, custServGrade, hastenCount, upTendencyFlag);// 多选
		if (StringUtils.isNotEmpty(thirdDir)) {
			sb.append(" AND D.APPEAL_CHILD=" + thirdDir);
		}
		if (StringUtils.isNotEmpty(channelDetailId)) {
			sb.append(" AND A.CHANNEL_DETAIL_ID=" + channelDetailId);
		}
		if (StringUtils.isNotEmpty(sheetType)) {
			sb.append(" AND W.SHEET_TYPE=" + sheetType);
			if ("720130013".equals(sheetType) || "700000127".equals(sheetType)) {// 分工批量分派，增加第一次部门处理单查询、分派
				sb.append(" AND EXISTS(SELECT 1 FROM cc_work_sheet z WHERE z.work_sheet_id=w.source_sheet_id AND z.sheet_type IN(720130011,700000126))");
			}
		}
		if (StringUtils.isNotEmpty(returnStaff)) {
			int staffId = workSheetBusi.getStaffId(returnStaff);
			sb.append(" AND W.RETURN_STAFF=" + staffId);
		}
		if (StringUtils.isNotEmpty(sheetStatu)) {
			sb.append(" AND W.SHEET_STATU=" + sheetStatu);
		}
		if (StringUtils.isNotEmpty(channel)) {
			String[] sr = channel.split(",");
			sb.append(" AND D.DISPUTE_CHNL_1=" + sr[0]);
			sb.append(" AND D.DISPUTE_CHNL_2=" + sr[1]);
			sb.append(" AND D.DISPUTE_CHNL_3=" + sr[2]);
		}
		if (StringUtils.isNotEmpty(products)) {
			String[] sr = products.split(",");
			sb.append(" AND D.PROD_ONE=" + sr[0]);
			sb.append(" AND D.PROD_TWO=" + sr[1]);
		}
		String strWhere = sb.toString();
		log.info("getPoolGridDataFP strWhere:\n{}", strWhere);
		GridDataInfo info = dbgridDataFaceService.getBatchGridDataBySize(funId, strWhere, begion, pageSize);
		return info;
	}

	private void setMultipleWhere(StringBuffer sb, String bestOrder, String areaId, String custServGrade,
			String hastenCount, String upTendencyFlag) {
		if (StringUtils.isNotEmpty(bestOrder)) {
			sb.append(" AND D.BEST_ORDER IN(" + bestOrder + ")");
		}
		if (StringUtils.isNotEmpty(areaId)) {
			sb.append(" AND A.AREA_ID IN(" + areaId + ")");
		}
		if (StringUtils.isNotEmpty(custServGrade)) {
			sb.append(" AND C.CUST_SERV_GRADE IN(" + custServGrade + ")");
		}
		if (StringUtils.isNotEmpty(hastenCount)) {
			sb.append(" AND(");
			boolean first = true;
			String[] strs = hastenCount.split(",");
			for (String str : strs) {
				if (!first) {
					sb.append(" OR ");
				}
				first = false;
				if ("3".equals(str)) {
					sb.append("(SELECT COUNT(1)FROM CC_HASTEN_SHEET H WHERE H.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID)>=3");
				} else {
					sb.append("(SELECT COUNT(1)FROM CC_HASTEN_SHEET H WHERE H.SERVICE_ORDER_ID=A.SERVICE_ORDER_ID)=" + str);
				}
			}
			sb.append(")");
		}
		if (StringUtils.isNotEmpty(upTendencyFlag)) {
			sb.append(" AND L.UP_TENDENCY_FLAG IN(" + upTendencyFlag + ")");
		}
	}

	/**
	 * 人工批量办结工单池
	 */
	@PostMapping(value = "/workflow/dbgridDataFace/getPoolGridDataBJ")
	public GridDataInfo getPoolGridDataBJ(
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="pageSize", required=true)int pageSize,
			@RequestParam(value="funId", required=true)String funId,
			@RequestParam(value="orderId", required=false)String orderId,
			@RequestParam(value="prodNum", required=false)String prodNum,
			@RequestParam(value="realInfo", required=false)String realInfo,
			@RequestParam(value="returnStaff", required=false)String returnStaff,
			@RequestParam(value="regionId", required=false)String regionId,
			@RequestParam(value="chaoShi", required=false)String chaoShi,
			@RequestParam(value="servType", required=false)String servType,
			@RequestParam(value="sheetStatu", required=false)String sheetStatu,
			@RequestParam(value="trAppoid", required=false)String trAppoid,
			@RequestParam(value="secondDir", required=false)String secondDir,
			@RequestParam(value="thirdDir", required=false)String thirdDir,
			@RequestParam(value="bestOrder", required=false)String bestOrder,
			@RequestParam(value="trSubFrom", required=false)String trSubFrom,
			@RequestParam(value="trChannel", required=false)String trChannel,
			@RequestParam(value="channelDetailId", required=false)String channelDetailId,
			@RequestParam(value="areaName", required=false)String areaName,
			@RequestParam(value="subStationName", required=false)String subStationName,
			@RequestParam(value="acceptContent", required=false)String acceptContent,
			@RequestParam(value="channel", required=false)String channel,
			@RequestParam(value="products", required=false)String products,
			@RequestParam(value="orderDate", required=false)String orderDate,
			@RequestParam(value="shuniu", required=false)String shuniu,
			@RequestParam(value="trChannelDetail", required=false)String trChannelDetail,
			@RequestParam(value="sheetType", required=false)String sheetType
	) {
		StringBuffer sb = new StringBuffer();
		this.setServiceOrderAskWhere1(sb, shuniu, trSubFrom, trChannel, trChannelDetail, orderId, regionId);
		this.setServiceOrderAskWhere2(sb, prodNum, servType, realInfo, orderDate, areaName, subStationName);
		this.setServiceOrderAskWhere4(sb, chaoShi, acceptContent, trAppoid, secondDir);
		if(StringUtils.isNotEmpty(thirdDir)) {
			sb.append(" AND D.APPEAL_CHILD=" + thirdDir);
		}
		if(StringUtils.isNotEmpty(channelDetailId)) {
			sb.append(" AND A.CHANNEL_DETAIL_ID=" + channelDetailId);
		}
		if(StringUtils.isNotEmpty(bestOrder)) {
			sb.append(" AND D.BEST_ORDER=" + bestOrder);
		}
    	if(StringUtils.isNotEmpty(sheetType)) {
			sb.append(" AND W.SHEET_TYPE=" + sheetType);
		}
		if(StringUtils.isNotEmpty(returnStaff)) {
			int staffId = workSheetBusi.getStaffId(returnStaff);
			sb.append(" AND W.RETURN_STAFF=" + staffId);
		}
		if(StringUtils.isNotEmpty(sheetStatu)){
			sb.append(" AND W.SHEET_STATU=" + sheetStatu);
		}
		if(StringUtils.isNotEmpty(channel)) {
			String[] sr = channel.split(",");
			sb.append(" AND D.DISPUTE_CHNL_1=" + sr[0]);
			sb.append(" AND D.DISPUTE_CHNL_2=" + sr[1]);
			sb.append(" AND D.DISPUTE_CHNL_3=" + sr[2]);
		}
		if(StringUtils.isNotEmpty(products)) {
			String[] sr = products.split(",");
			sb.append(" AND D.PROD_ONE=" + sr[0]);
			sb.append(" AND D.PROD_TWO=" + sr[1]);
		}

		String strWhere = sb.toString();
		log.info("getPoolGridDataBJ strWhere:\n{}", strWhere);
		GridDataInfo info = dbgridDataFaceService.getBatchGridDataBySize(funId, strWhere, begion, pageSize);
		return info;
	}

	/**
	 * 人工批量分派我的任务
	 */
	@PostMapping(value = "/workflow/dbgridDataFace/getMyTaskGridDataBatchFP")
	public GridDataInfo getMyTaskGridDataBatchFP(@RequestParam(value = "begion", required = true) int begion,
			@RequestParam(value = "pageSize", required = true) int pageSize,
			@RequestParam(value = "funId", required = true) String funId,
			@RequestParam(value = "orderId", required = false) String orderId,
			@RequestParam(value = "prodNum", required = false) String prodNum,
			@RequestParam(value = "realInfo", required = false) String realInfo,
			@RequestParam(value = "returnStaff", required = false) String returnStaff,
			@RequestParam(value = "regionId", required = false) String regionId,
			@RequestParam(value = "chaoShi", required = false) String chaoShi,
			@RequestParam(value = "servType", required = false) String servType,
			@RequestParam(value = "sheetStatu", required = false) String sheetStatu,
			@RequestParam(value = "trAppoid", required = false) String trAppoid,
			@RequestParam(value = "secondDir", required = false) String secondDir,
			@RequestParam(value = "thirdDir", required = false) String thirdDir,
			@RequestParam(value = "bestOrder", required = false) String bestOrder,
			@RequestParam(value = "trSubFrom", required = false) String trSubFrom,
			@RequestParam(value = "trChannel", required = false) String trChannel,
			@RequestParam(value = "channelDetailId", required = false) String channelDetailId,
			@RequestParam(value = "areaName", required = false) String areaName,
			@RequestParam(value = "subStationName", required = false) String subStationName,
			@RequestParam(value = "acceptContent", required = false) String acceptContent,
			@RequestParam(value = "channel", required = false) String channel,
			@RequestParam(value = "products", required = false) String products,
			@RequestParam(value = "shuniu", required = false) String shuniu,
			@RequestParam(value = "trChannelDetail", required = false) String trChannelDetail,
			@RequestParam(value = "orderDate", required = false) String orderDate,
			@RequestParam(value = "sheetType", required = false) String sheetType,
			@RequestParam(value = "areaId", required = false) String areaId,
			@RequestParam(value = "custServGrade", required = false) String custServGrade,
			@RequestParam(value = "hastenCount", required = false) String hastenCount,
			@RequestParam(value = "upTendencyFlag", required = false) String upTendencyFlag) {
		StringBuffer sb = new StringBuffer();
		this.setServiceOrderAskWhere1(sb, shuniu, trSubFrom, trChannel, trChannelDetail, orderId, regionId);
		this.setServiceOrderAskWhere2(sb, prodNum, servType, realInfo, orderDate, areaName, subStationName);
		this.setServiceOrderAskWhere4(sb, chaoShi, acceptContent, trAppoid, secondDir);
		setMultipleWhere(sb, bestOrder, areaId, custServGrade, hastenCount, upTendencyFlag);// 多选
		if (StringUtils.isNotEmpty(thirdDir)) {
			sb.append(" AND D.APPEAL_CHILD=" + thirdDir);
		}
		if (StringUtils.isNotEmpty(channelDetailId)) {
			sb.append(" AND A.CHANNEL_DETAIL_ID=" + channelDetailId);
		}
		if (StringUtils.isNotEmpty(sheetType)) {
			sb.append(" AND W.SHEET_TYPE=" + sheetType);
		}
		if (StringUtils.isNotEmpty(returnStaff)) {
			int staffId = workSheetBusi.getStaffId(returnStaff);
			sb.append(" AND W.RETURN_STAFF=" + staffId);
		}
		if (StringUtils.isNotEmpty(sheetStatu)) {
			sb.append(" AND W.SHEET_STATU=" + sheetStatu);
		}
		if (StringUtils.isNotEmpty(channel)) {
			String[] sr = channel.split(",");
			sb.append(" AND D.DISPUTE_CHNL_1=" + sr[0]);
			sb.append(" AND D.DISPUTE_CHNL_2=" + sr[1]);
			sb.append(" AND D.DISPUTE_CHNL_3=" + sr[2]);
		}
		if (StringUtils.isNotEmpty(products)) {
			String[] sr = products.split(",");
			sb.append(" AND D.PROD_ONE=" + sr[0]);
			sb.append(" AND D.PROD_TWO=" + sr[1]);
		}
		String strWhere = sb.toString();
		log.info("getMyTaskGridDataBatch strWhere:\n{}", strWhere);
		GridDataInfo info = dbgridDataFaceService.getBatchGridDataBySize(funId, strWhere, begion, pageSize);
		return info;
	}

	/**
	 * 人工批量办结我的任务
	 */
	@PostMapping(value = "/workflow/dbgridDataFace/getMyTaskGridDataBatchBJ")
	public GridDataInfo getMyTaskGridDataBatchBJ(
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="pageSize", required=true)int pageSize,
			@RequestParam(value="funId", required=true)String funId,
			@RequestParam(value="orderId", required=false)String orderId,
			@RequestParam(value="prodNum", required=false)String prodNum,
			@RequestParam(value="realInfo", required=false)String realInfo,
			@RequestParam(value="returnStaff", required=false)String returnStaff,
			@RequestParam(value="regionId", required=false)String regionId,
			@RequestParam(value="chaoShi", required=false)String chaoShi,
			@RequestParam(value="servType", required=false)String servType,
			@RequestParam(value="sheetStatu", required=false)String sheetStatu,
			@RequestParam(value="trAppoid", required=false)String trAppoid,
			@RequestParam(value="secondDir", required=false)String secondDir,
			@RequestParam(value="thirdDir", required=false)String thirdDir,
			@RequestParam(value="bestOrder", required=false)String bestOrder,
			@RequestParam(value="trSubFrom", required=false)String trSubFrom,
			@RequestParam(value="trChannel", required=false)String trChannel,
			@RequestParam(value="channelDetailId", required=false)String channelDetailId,
			@RequestParam(value="areaName", required=false)String areaName,
			@RequestParam(value="subStationName", required=false)String subStationName,
			@RequestParam(value="acceptContent", required=false)String acceptContent,
			@RequestParam(value="channel", required=false)String channel,
			@RequestParam(value="products", required=false)String products,
			@RequestParam(value="shuniu", required=false)String shuniu,
			@RequestParam(value="trChannelDetail", required=false)String trChannelDetail,
			@RequestParam(value="orderDate", required=false)String orderDate,
			@RequestParam(value="sheetType", required=false)String sheetType
	) {
		StringBuffer sb = new StringBuffer();
		this.setServiceOrderAskWhere1(sb, shuniu, trSubFrom, trChannel, trChannelDetail, orderId, regionId);
		this.setServiceOrderAskWhere2(sb, prodNum, servType, realInfo, orderDate, areaName, subStationName);
		this.setServiceOrderAskWhere4(sb, chaoShi, acceptContent, trAppoid, secondDir);
		if(StringUtils.isNotEmpty(thirdDir)) {
			sb.append(" AND D.APPEAL_CHILD=" + thirdDir);
		}
		if(StringUtils.isNotEmpty(channelDetailId)) {
			sb.append(" AND A.CHANNEL_DETAIL_ID=" + channelDetailId);
		}
		if(StringUtils.isNotEmpty(bestOrder)) {
			sb.append(" AND D.BEST_ORDER=" + bestOrder);
		}
    	if(StringUtils.isNotEmpty(sheetType)) {
			sb.append(" AND W.SHEET_TYPE=" + sheetType);
		}
		if(StringUtils.isNotEmpty(returnStaff)) {
			int staffId = workSheetBusi.getStaffId(returnStaff);
			sb.append(" AND W.RETURN_STAFF=" + staffId);
		}
		if(StringUtils.isNotEmpty(sheetStatu)){
			sb.append(" AND W.SHEET_STATU=" + sheetStatu);
		}
		if(StringUtils.isNotEmpty(channel)) {
			String[] sr = channel.split(",");
			sb.append(" AND D.DISPUTE_CHNL_1=" + sr[0]);
			sb.append(" AND D.DISPUTE_CHNL_2=" + sr[1]);
			sb.append(" AND D.DISPUTE_CHNL_3=" + sr[2]);
		}
		if(StringUtils.isNotEmpty(products)) {
			String[] sr = products.split(",");
			sb.append(" AND D.PROD_ONE=" + sr[0]);
			sb.append(" AND D.PROD_TWO=" + sr[1]);
		}

		String strWhere = sb.toString();
		log.info("getMyTaskGridDataBatch strWhere:\n{}", strWhere);
		GridDataInfo info = dbgridDataFaceService.getBatchGridDataBySize(funId, strWhere, begion, pageSize);
		return info;
	}

	@PostMapping(value = "/workflow/dbgridDataFace/getSheetPoolGridData")
	public GridDataInfo getSheetPoolGridData(@RequestBody String param) {
		try {
			com.alibaba.fastjson.JSONObject paramMap = JSON.parseObject(param);
			int begion = paramMap.getInteger("begion");
			int pageSize = paramMap.getInteger("pageSize");
			String regionId = paramMap.getString("regionId");
			String orderId = paramMap.getString("orderId");
			String prodNum = paramMap.getString("prodNum");
			String custStart = paramMap.getString("custStart");
			String returnStaff = paramMap.getString("returnStaff");
			String servType = paramMap.getString("servType");
			String sheetStatus = paramMap.getString("sheetStatus");
			String worksheetType = paramMap.getString("worksheetType");
			String orderDate = paramMap.getString("orderDate");
			String sheetDate = paramMap.getString("sheetDate");
			String trAppoid = paramMap.getString("trAppoid");
			String reasonId = paramMap.getString("reasonId");
			String trSubFrom = paramMap.getString("trSubFrom");
			String trChannel = paramMap.getString("trChannel");
			String trChannelDetail = paramMap.getString("trChannelDetail");
			String shuniu = paramMap.getString("shuNiu");
			String relaInfo = paramMap.getString("relaInfo");
			String upTendencyFlag = paramMap.getString("upTendencyFlag");
			String hastentNum = paramMap.getString("hastentNum");
			String areaName = paramMap.getString("areaName");
			String subStationName = paramMap.getString("subStationName");
			String secFlag = paramMap.getString("secFlag");
			String tacheId = paramMap.getString("tacheId");
			String miitCode = paramMap.getString("miitCode");
			String ruyiFlag = paramMap.getString("ruyiFlag");
			String labelFlag = paramMap.getString("labelFlag");

			StringBuffer sb = new StringBuffer();
			this.setServiceOrderAskWhere1(sb, shuniu, trSubFrom, trChannel, trChannelDetail, orderId, regionId);
			this.setServiceOrderAskWhere2(sb, prodNum, servType, relaInfo, orderDate, areaName, subStationName);
			this.setComplaintMiitCode(sb, miitCode, false);
			this.setRuyiFlag(sb, ruyiFlag);
			this.setLabelFlag(sb, labelFlag);

			if (StringUtils.isNotEmpty(upTendencyFlag)) {
				sb.append(" AND L.UP_TENDENCY_FLAG = " + Integer.parseInt(upTendencyFlag));
			}
			if (StringUtils.isNotEmpty(hastentNum)) {
				sb.append(" AND (SELECT COUNT(1) FROM CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID = W.SERVICE_ORDER_ID) = " + Integer.parseInt(hastentNum));
			}
			if (StringUtils.isNotEmpty(secFlag)) {
				if (secFlag.equals("否")) {
					sb.append(" AND L.SEC_FLAG IS NULL ");
				} else {
					sb.append(" AND L.SEC_FLAG > 0 ");
				}
			}
			if (StringUtils.isNotEmpty(trAppoid)) {
				sb.append(" AND D.APPEAL_PROD_ID = " + trAppoid);
			}
			if (StringUtils.isNotEmpty(reasonId)) {
				sb.append(" AND D.APPEAL_REASON_ID = " + reasonId);
			}
			if (StringUtils.isNotEmpty(custStart)) {
				sb.append(" AND C.CUST_SERV_GRADE = " + custStart);
			}
			if (StringUtils.isNotEmpty(returnStaff)) {
				int staffId = workSheetBusi.getStaffId(returnStaff);
				sb.append(" AND W.RETURN_STAFF = " + staffId);
			}
			if (StringUtils.isNotEmpty(tacheId)) {
				sb.append(" AND W.TACHE_ID = " + tacheId);
			}
			this.setWorksheetType(sb, worksheetType);
			if (StringUtils.isNotEmpty(sheetStatus)) {
				sb.append(" AND W.SHEET_STATU = " + sheetStatus);
			}
			if (StringUtils.isNotEmpty(sheetDate)) {
				JSONArray dateArray = JSON.parseArray(sheetDate);
				sb.append(" AND W.LOCK_DATE > '" + dateArray.get(0) + "'");
				sb.append(" AND W.LOCK_DATE < '" + dateArray.get(1) + "'");
			}
			// 查询单、小额退赔、省投诉调帐班 隐藏
			sb.append(" AND ( W.SERVICE_TYPE <> 720200003 OR D.APPEAL_CHILD <> 2020501 OR W.TACHE_ID <> 700000085 OR W.RECEIVE_ORG_ID <> '363843' )");

			String strWhere = sb.toString();
			log.info("getSheetPoolGridData strWhere:\n{}", strWhere);
			return dbgridDataFaceService.getGridDataBySize("TSSHEET10003", strWhere, begion, pageSize);
		} catch (Exception e) {
			log.error("getSheetPoolGridData Error :{}", e.getMessage());
			return null;
		}
	}
	
	private void setLabelFlag(StringBuffer sb, String labelFlag) {
		if(StringUtils.isNotEmpty(labelFlag)) {
			String[] labels = labelFlag.split("-");
			if(labelFlag.startsWith("refundFlag") && labels.length > 1) {
				String refundFlag = labels[1];
				sb.append(" AND L.REFUND_FLAG = " + refundFlag);
			}
		}
	}

	private void setRuyiFlag(StringBuffer sb, String ruyiFlag) {
		if(StringUtils.isNotEmpty(ruyiFlag)) {
			if("1".equals(ruyiFlag)){
				sb.append(" AND A.SERVICE_ORDER_ID IN(SELECT SERVICE_ORDER_ID FROM cc_ruyi_label)");
			} else if ("2".equals(ruyiFlag)){
				sb.append(" AND A.SERVICE_ORDER_ID NOT IN(SELECT SERVICE_ORDER_ID FROM cc_ruyi_label)");
			}
		}
	}

	private void setRouterParam(StringBuffer sb, String hastent, String bestOrder,String upTendency){
		if(StringUtils.isNotEmpty(hastent)) {
			sb.append(" AND (SELECT COUNT(1)FROM CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID) > "+Integer.parseInt(hastent)) ;
		}
		if (StringUtils.isNotBlank(bestOrder)){
			sb.append( "AND D.BEST_ORDER != 0 AND D.BEST_ORDER != 100122410") ;
		}
		if(StringUtils.isNotEmpty(upTendency)) {
			sb.append(" AND (L.UP_TENDENCY_FLAG IS NOT NULL AND L.UP_TENDENCY_FLAG != 0)") ;
		}
	}

	private void setComplaintMiitCode(StringBuffer sb, String miitCode, boolean hisFlag) {
		if(StringUtils.isNotEmpty(miitCode)) {
			String orderIdStr = orderQury.getOrderIdStrByMiitCode(miitCode, hisFlag);
			log.info("miitCode: {} whereStr: {}", miitCode, orderIdStr);
			if(orderIdStr == null) {
				sb.append(" AND 1 <> 1");
			} else {
				sb.append(" AND A.SERVICE_ORDER_ID in " + orderIdStr);
			}
		}
	}

	private void setWorksheetType(StringBuffer sb, String worksheetType) {
		if(StringUtils.isNotEmpty(worksheetType)) {
			if("0".equals(worksheetType)) {
				sb.append(" AND W.SHEET_TYPE not in (720130028, 720130029)");
			} else {
				sb.append(" AND W.SHEET_TYPE=" + worksheetType);
			}
		}
	}

	private void setServiceOrderAskWhere1(StringBuffer sb, String shuniu, String trSubFrom, String trChannel, String trChannelDetail,
			String orderId, String regionId) {
		if(StringUtils.isNotEmpty(shuniu)) {
			sb.append(" AND A.ACCEPT_ORG_ID " + ("1".equals(shuniu)?"=":"!=")+"'100000JT'");
		}
		if(StringUtils.isNotEmpty(trSubFrom)) {
			sb.append(" AND A.ACCEPT_COME_FROM=" + trSubFrom) ;
		}
		if(StringUtils.isNotEmpty(trChannel)) {
			sb.append(" AND A.ACCEPT_CHANNEL_ID=" + trChannel) ;
		}
		if(StringUtils.isNotEmpty(trChannelDetail)) {
			sb.append(" AND A.CHANNEL_DETAIL_ID=" + trChannelDetail) ;
		}
		if(StringUtils.isNotEmpty(orderId)) {
			sb.append(" AND A.SERVICE_ORDER_ID='" + orderId + "'") ;
		}
		if(StringUtils.isNotEmpty(regionId)) {
			sb.append(" AND A.REGION_ID=" + regionId) ;
		}
	}

	private void setServiceOrderAskWhere2(StringBuffer sb, String prodNum, String servType, String relaInfo, String orderDate, String areaName, String subStationName) {
		if(StringUtils.isNotEmpty(prodNum)) {
			sb.append(" AND A.PROD_NUM='" + prodNum + "'") ;
		}
		if(StringUtils.isNotEmpty(servType)) {
			sb.append(" AND A.SERVICE_TYPE=" + servType ) ;
		}
		if(StringUtils.isNotEmpty(relaInfo)){
			sb.append(" AND A.rela_info='" + relaInfo +"'") ;
		}
		if(StringUtils.isNotEmpty(orderDate)) {
			JSONArray dateArray = JSON.parseArray(orderDate);
			sb.append(" AND A.ACCEPT_DATE > '" + dateArray.get(0) + "'") ;
			sb.append(" AND A.ACCEPT_DATE < '" + dateArray.get(1) + "'") ;
		}
		if(StringUtils.isNotEmpty(areaName)) {
			sb.append(" AND A.AREA_NAME like '%"+areaName+"%'") ;
		}
		if(StringUtils.isNotEmpty(subStationName)) {
			sb.append(" AND A.SUB_STATION_NAME like '%"+subStationName+"%'") ;
		}
	}
	private void setServiceOrderAskWhere3(StringBuffer sb, String upTendencyFlag, String hastentNum, String areaName, String subStationName,
										  String secFlag, String sheetState) {
		if(StringUtils.isNotEmpty(upTendencyFlag)) {
			sb.append(" AND L.UP_TENDENCY_FLAG = "+Integer.parseInt(upTendencyFlag)) ;
		}
		if(StringUtils.isNotEmpty(hastentNum)) {
			sb.append(" AND (SELECT COUNT(1)FROM CC_HASTEN_SHEET HS WHERE HS.SERVICE_ORDER_ID=W.SERVICE_ORDER_ID) = "+Integer.parseInt(hastentNum)) ;
		}
		if(StringUtils.isNotEmpty(areaName)) {
			sb.append(" AND A.AREA_NAME like '%"+areaName+"%'") ;
		}
		if(StringUtils.isNotEmpty(subStationName)) {
			sb.append(" AND A.SUB_STATION_NAME like '%"+subStationName+"%'") ;
		}
		if(StringUtils.isNotEmpty(secFlag)) {
			if(secFlag.equals("否")){
				sb.append(" AND L.SEC_FLAG IS NULL");
			}else {
				sb.append(" AND L.SEC_FLAG >0 ");
			}
		}
		if(StringUtils.isNotEmpty(sheetState)) {
			if(sheetState.equals("是")){
				sb.append(" AND W.SHEET_STATU IN ('720130035','700000046') ");
			}else {
				sb.append(" AND W.SHEET_STATU NOT IN ('720130035','700000046') ");
			}
		}
	}

	private void setServiceOrderAskWhere4(StringBuffer sb,String chaoShi,String acceptContent,String trAppoid,String secondDir) {
		if(StringUtils.isNotEmpty(chaoShi)) {
			if(chaoShi.equals("1")) {
				sb.append(" AND ( W.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, W.CREAT_DATE, NOW())/60 - W.HANGUP_TIME_COUNT ) < 0) ");
			}
			if(chaoShi.equals("2")) {
				sb.append(" AND ( W.DEAL_LIMIT_TIME * 60 -( TIMESTAMPDIFF(SECOND, W.CREAT_DATE, NOW())/60 - W.HANGUP_TIME_COUNT ) > 0) ");
			}
		}
		if(StringUtils.isNotEmpty(acceptContent)) {
			sb.append(" AND D.ACCEPT_CONTENT LIKE '%" + acceptContent + "%'");
		}
		if(StringUtils.isNotEmpty(trAppoid)) {
			sb.append(" AND D.APPEAL_PROD_ID=" + trAppoid) ;
		}
		if(StringUtils.isNotEmpty(secondDir)) {
			sb.append(" AND D.APPEAL_REASON_ID=" + secondDir) ;
		}
	}

	@PostMapping(value = "/workflow/dbgridDataFace/getMyTaskGridData")
	public GridDataInfo getMyTaskGridData(@RequestBody String param){
		try {
			com.alibaba.fastjson.JSONObject paramMap = JSON.parseObject(param);
			int begion = paramMap.getInteger("begion");
			int pageSize = paramMap.getInteger("pageSize");
			String orderId = paramMap.getString("orderId");
			String regionId = paramMap.getString("regionId");
			String prodNum = paramMap.getString("prodNum");
			String custStart = paramMap.getString("custStart");
			String returnStaff = paramMap.getString("returnStaff");
			String servType = paramMap.getString("servType");
			String sheetStatus = paramMap.getString("sheetStatus");
			String worksheetType = paramMap.getString("worksheetType");
			String startTime = paramMap.getString("startTime");
			String endTime = paramMap.getString("endTime");
			String trAppoid = paramMap.getString("trAppoid");
			String reasonId = paramMap.getString("reasonId");
			String trSubFrom = paramMap.getString("trSubFrom");
			String trChannel = paramMap.getString("trChannel");
			String trChannelDetail = paramMap.getString("trChannelDetail");
			String shuniu = paramMap.getString("shuNiu");
			String relaInfo = paramMap.getString("relaInfo");
			String receiveStaff = paramMap.getString("receiveStaff");
			String upTendencyFlag = paramMap.getString("upTendencyFlag");
			String hastentNum = paramMap.getString("hastentNum");
			String areaName = paramMap.getString("areaName");
			String subStationName = paramMap.getString("subStationName");
			String secFlag = paramMap.getString("secFlag");
			String sheetState = paramMap.getString("sheetState");
			String tacheId = paramMap.getString("tacheId");
			String miitCode = paramMap.getString("miitCode");
			String ruyiFlag = paramMap.getString("ruyiFlag");
			String hastent = paramMap.getString("hastent");
			String bestOrder = paramMap.getString("bestOrder");
			String upTendency = paramMap.getString("upTendency");
			String labelFlag = paramMap.getString("labelFlag");
			String hiddenState = paramMap.getString("hiddenState");

			StringBuffer sb = new StringBuffer();
			this.setServiceOrderAskWhere1(sb, shuniu, trSubFrom, trChannel, trChannelDetail, orderId, regionId);
			this.setServiceOrderAskWhere3(sb, upTendencyFlag, hastentNum, areaName, subStationName, secFlag, sheetState);
			this.setComplaintMiitCode(sb, miitCode, false);
			this.setRuyiFlag(sb, ruyiFlag);
			this.setRouterParam(sb, hastent, bestOrder, upTendency);
			this.setLabelFlag(sb, labelFlag);
			
			if (StringUtils.isNotEmpty(trAppoid)) {
				sb.append(" AND D.APPEAL_PROD_ID=" + trAppoid);
			}
			if (StringUtils.isNotEmpty(reasonId)) {
				sb.append(" AND D.APPEAL_REASON_ID=" + reasonId);
			}
			if (StringUtils.isNotEmpty(prodNum)) {
				sb.append(" AND A.PROD_NUM='" + prodNum + "'");
			}
			if (StringUtils.isNotEmpty(custStart)) {
				sb.append(" AND C.CUST_SERV_GRADE=" + custStart);
			}
			if (StringUtils.isNotEmpty(returnStaff)) {
				int staffId = workSheetBusi.getStaffId(returnStaff);
				sb.append(" AND W.RETURN_STAFF=" + staffId);
			}
			if (StringUtils.isNotEmpty(receiveStaff)) {
				int receiveStaffId = workSheetBusi.getStaffId(receiveStaff);
				sb.append(" AND W.RECEIVE_STAFF=" + receiveStaffId);
			}
			if (StringUtils.isNotEmpty(servType)) {
				sb.append(" AND A.SERVICE_TYPE=" + servType);
			}
			this.setWorksheetType(sb, worksheetType);
			if (StringUtils.isNotEmpty(tacheId)) {
				sb.append(" AND W.TACHE_ID=" + tacheId);
			}
			if (StringUtils.isNotEmpty(sheetStatus)) {
				sb.append(" AND W.SHEET_STATU=" + sheetStatus);
			}
			if (StringUtils.isNotEmpty(relaInfo)) {
				sb.append(" AND A.rela_info='" + relaInfo + "'");
			}
			if (StringUtils.isNotEmpty(startTime) && StringUtils.isNotEmpty(endTime)) {
				sb.append("  AND A.ACCEPT_DATE > '" + startTime + "'");
				sb.append("  AND A.ACCEPT_DATE < '" + endTime + "'");
			}
			if(StringUtils.equals("是", hiddenState)){
				sb.append(" AND HO.HIDDEN_STATE = 0");
			}
			if(StringUtils.equals("否", hiddenState)){
				sb.append(" AND (HO.HIDDEN_STATE != 0 OR HO.WORK_SHEET_ID IS NULL)");
			}

			String strWhere = sb.toString();
			log.info("getMyTaskGridData 我的任务 strWhere:\n{}", strWhere);
			return dbgridDataFaceService.getGridDataBySize("TSSHEET10004", strWhere, begion, pageSize);
		} catch (Exception e) {
			log.error("getMyTaskGridData Error:{} ", e.getMessage());
			return null;
		}
	}

	@PostMapping(value = "/workflow/dbgridDataFace/getCallBackMyTaskGridData")
	public GridDataInfo getCallBackMyTaskGridData(@RequestBody String param){
		try {
			com.alibaba.fastjson.JSONObject paramMap = JSON.parseObject(param);
			int begion = paramMap.getInteger("begion");
			int pageSize = paramMap.getInteger("pageSize");
			String receiveStaff = paramMap.getString("receiveStaff");
			String sheetState = paramMap.getString("sheetState");

			StringBuffer sb = new StringBuffer();
			if (StringUtils.isNotEmpty(receiveStaff)) {
				sb.append(" AND W.DEAL_STAFF=" + receiveStaff);
				sb.append(" AND T.DEAL_STAFFID=" + receiveStaff);
			}
			if(StringUtils.isNotEmpty(sheetState)) {
				if(sheetState.equals("是")){
					sb.append(" AND W.SHEET_STATU IN ('720130035','700000046') ");
				}else {
					sb.append(" AND W.SHEET_STATU NOT IN ('720130035','700000046') ");
				}
			}
			String strWhere = sb.toString();
			log.info("getCallBackMyTaskGridData 我的任务 strWhere:\n{}", strWhere);
			return dbgridDataFaceService.getCallBackGridDataBySize("TSSHEET10004", strWhere, begion, pageSize);
		} catch (Exception e) {
			log.error("getCallBackMyTaskGridData Error:{} ", e.getMessage());
			return null;
		}
	}
	
	@PostMapping(value = "/workflow/dbgridDataFace/getHasReturnGridData")
	public GridDataInfo getHasReturnGridData(
			@RequestParam(value="begion", required=true)int begion,
			@RequestParam(value="orderId", required=false)String orderId,
			@RequestParam(value="prodNum", required=false)String prodNum,
			@RequestParam(value="custStart", required=false)String custStart,
			@RequestParam(value="returnStaff", required=false)String returnStaff,
			@RequestParam(value="servType", required=false)String servType,
			@RequestParam(value="sheetStatus", required=false)String sheetStatus,
			@RequestParam(value="worksheetType", required=false)String worksheetType,
			@RequestParam(value="miitCode", required=false)String miitCode) {
		String strWhere = "";
		
		StringBuffer sb = new StringBuffer();
		this.setComplaintMiitCode(sb, miitCode, false);

		if(StringUtils.isNotEmpty(orderId)) {
			sb.append(" AND A.SERVICE_ORDER_ID='" + orderId + "'") ;
		}
		if(StringUtils.isNotEmpty(prodNum)) {
			sb.append(" AND A.PROD_NUM='" + prodNum + "'") ;
		}
		if(StringUtils.isNotEmpty(custStart)) {
			sb.append(" AND C.CUST_SERV_GRADE=" + custStart ) ;
		}
		if(StringUtils.isNotEmpty(returnStaff)) {
			int staffId = workSheetBusi.getStaffId(returnStaff);
			sb.append(" AND W.RETURN_STAFF=" + staffId ) ;
		}		
		if(StringUtils.isNotEmpty(servType)) {
			sb.append(" AND A.SERVICE_TYPE=" + servType ) ;
		}
		if(StringUtils.isNotEmpty(worksheetType)) {
			sb.append(" AND W.SHEET_TYPE=" + worksheetType ) ;
		}
		if(StringUtils.isNotEmpty(sheetStatus)){
			sb.append(" AND W.SHEET_STATU=" + sheetStatus ) ;
		}
		strWhere = sb.toString();
		log.info("已派发查询条件：{}" , strWhere);
		return dbgridDataFaceService.getGridData("TSSHEET10005",strWhere,begion);
	}
	
	@PostMapping(value = "/workflow/dbgridDataFace/deleteAutoAcceptOrder")
	public int deleteAutoAcceptOrder() {
		int size=autoAcceptDao.deleteAutoAcceptOrder();
		return size;
	}

	@PostMapping(value = "/workflow/dbgridDataFace/queryValue")
	public GridDataInfo queryValue(
			@RequestParam(value="begion", required=false)int begion,
			@RequestParam(value="pageSize", required=false)int pageSize
	) {
		return dbgridDataFaceService.queryValue(null, null, begion, pageSize);
	}


	@PostMapping(value = "/workflow/dbgridDataFace/deleteKey")
	public int deleteKey(@RequestParam(value="redisKeys", required=false)List<String> redisKeys) {
		log.info("deleteKey: {}", JSON.toJSON(redisKeys));
		return dbgridDataFaceService.deleteKey(redisKeys);
	}

	@PostMapping(value = "/workflow/dbgridDataFace/addRow")
	public int addRow(@RequestBody Map<String, String> newRowData) {
		StringBuffer sb = new StringBuffer();
		String strWhere = sb.toString();
		log.info("addRow strWhere:\n{}", strWhere);
		return dbgridDataFaceService.addRow(newRowData);
	}

	@PostMapping(value = "/workflow/dbgridDataFace/getApportion")
	public int getApportion(@RequestParam(value="staffId", required=true)String staffId){
		return dbgridDataFaceService.getApportion(staffId);
	}

	@PostMapping(value = "/workflow/dbgridDataFace/getApportionData")
	public GridDataInfo getApportionData(@RequestParam(value = "staffId", required = false) String staffId,
										 @RequestParam(value = "orgId", required = false) String orgId,
										 @RequestParam(value = "status", required = false) String status,
										 @RequestParam(value = "appStatus", required = false) String appStatus,
										 @RequestParam(value = "begin", required = true) int begin,
										 @RequestParam(value = "pageSize", required = true) int pageSize) {
		return dbgridDataFaceService.getApportionData(staffId, orgId, status, appStatus, begin, pageSize);
	}

	@PostMapping(value = "/workflow/dbgridDataFace/saveApportion")
	public int saveApportion(@RequestParam(value = "staffId", required = true) String staffId,
							 @RequestParam(value = "staffName", required = true) String staffName,
							 @RequestParam(value = "orgId", required = true) String orgId,
							 @RequestParam(value = "orgName", required = true) String orgName,
							 @RequestParam(value = "apportionNumber", required = true) String apportionNumber){
		return dbgridDataFaceService.saveApportion(staffId,staffName,orgId,orgName,apportionNumber);
	}
	@PostMapping(value = "/workflow/dbgridDataFace/updateApportion")
	public String updateApportion(@RequestBody(required=false) String param){
		return dbgridDataFaceService.updateApportion(param);
	}

	
}
