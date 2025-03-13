package com.timesontransfar.customservice.dbgridData.impl;
import com.alibaba.fastjson.JSON;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.dbgridData.SatisfactionService;
import com.timesontransfar.feign.custominterface.InterfaceFeign;
import com.transfar.common.utils.IdUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.stream.Collectors;

@Component(value="SatisfactionServiceImpl")
public class SatisfactionServiceImpl implements SatisfactionService {

	private static final Logger log = LoggerFactory.getLogger(SatisfactionServiceImpl.class);

	@Autowired
	private PubFunc pubFunc;

	@Autowired
	private JdbcTemplate jt;

	@Autowired
	private IdbgridDataPub dbgridDataPub;

	@Autowired
	private InterfaceFeign interfaceFeign;

	@SuppressWarnings({ "all" })
	@Override
	@Transactional
	public String importFile(InputStream file, String fileName, String logonName) {
		JSONArray jsonArray = new JSONArray();
		String errorInfo = "success";
		try {
			Workbook workbook = new XSSFWorkbook(file);
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rows = sheet.iterator();
			rows.next();
			// 获取所有合并单元格的范围
			List<CellRangeAddress> mergedRegions = sheet.getMergedRegions();
			while (rows.hasNext()) {
				Row currentRow = rows.next();
				if (isRowEmpty(currentRow)) {
					continue;
				}
				JSONObject jsonObject = new JSONObject();
				try {
					String prodNum = getCellValueAsString(currentRow.getCell(3), mergedRegions, sheet).trim();
					String rechargeNumber = getCellValueAsString(currentRow.getCell(4), mergedRegions, sheet).trim();
					String rechargeAmount = getCellValueAsString(currentRow.getCell(7), mergedRegions, sheet).trim();
					String faceValue = getCellValueAsString(currentRow.getCell(16), mergedRegions, sheet).trim();
					String rechargeNumberType = getCellValueAsString(currentRow.getCell(5), mergedRegions, sheet).trim();
					String destinationAttr = getDestinationAttr(rechargeNumberType);
					String cardSecret = getCellValueAsString(currentRow.getCell(15), mergedRegions, sheet).trim();
					String rechargeMethod = getCellValueAsString(currentRow.getCell(11), mergedRegions, sheet).trim();
					if(StringUtils.isBlank(destinationAttr)){
						errorInfo = "第" + currentRow.getRowNum() + "行，充值号码类型不符合要求";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}

					if(StringUtils.isBlank(rechargeMethod)){
						errorInfo = "第" + currentRow.getRowNum() + "行，充值方式不能为空";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(!("全额充值".equals(rechargeMethod) || "拆分充值".equals(rechargeMethod))){
						errorInfo = "第" + currentRow.getRowNum() + "行，充值方式内容不符合字段要求，请检查";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(StringUtils.isBlank(prodNum)){
						errorInfo = "第" + currentRow.getRowNum() + "行，产品号码不能为空";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(StringUtils.isBlank(rechargeNumber)){
						errorInfo = "第" + currentRow.getRowNum() + "行，充值号码不能为空";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if (!isNumeric(prodNum) || !isNumeric(rechargeNumber)) {
						errorInfo = "第" + currentRow.getRowNum() + "行，产品号码、充值号码需要为纯数字";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(prodNum.length()>20){
						errorInfo = "第" + currentRow.getRowNum() + "行，产品号码长度不能超过20";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(rechargeNumber.length()>20){
						errorInfo = "第" + currentRow.getRowNum() + "行，充值号码长度不能超过20";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(StringUtils.isBlank(rechargeAmount)){
						errorInfo = "第" + currentRow.getRowNum() + "行，满意度金额不能为空";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(StringUtils.isBlank(faceValue)){
						errorInfo = "第" + currentRow.getRowNum() + "行，面值不能为空";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(rechargeAmount.length()>10 || faceValue.length()>10){
						errorInfo = "第" + currentRow.getRowNum() + "行，满意度金额和面值数额过大";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if (!isValidAmount(rechargeAmount) || !isValidAmount(faceValue)) {
						errorInfo = "第" + currentRow.getRowNum() + "行，满意度金额和面值需要为有效的金额";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					if(StringUtils.isBlank(cardSecret)){
						errorInfo = "第" + currentRow.getRowNum() + "行，卡密不能为空";
						log.error("importFile 导入数据失败,error:{}",errorInfo);
						return errorInfo;
					}
					jsonObject.put("orderId", StringUtils.substring(getCellValueAsString(currentRow.getCell(1), mergedRegions, sheet).trim(),0,20));  // 工单号
					jsonObject.put("region", StringUtils.substring(getCellValueAsString(currentRow.getCell(2), mergedRegions, sheet).trim(),0,20));  // 地市
					jsonObject.put("prodNum", prodNum);  // 产品号码
					jsonObject.put("rechargeNumber", rechargeNumber);  // 充值号码
					jsonObject.put("rechargeNumberType", rechargeNumberType);  // 号码类型
					jsonObject.put("rechargeNumberUserName", StringUtils.substring(getCellValueAsString(currentRow.getCell(6), mergedRegions, sheet).trim(),0,30));   // 充值号码对应的用户姓名
					jsonObject.put("rechargeAmount", rechargeAmount);  // 满意度金额
					jsonObject.put("rechargeClassification", StringUtils.substring(getCellValueAsString(currentRow.getCell(8), mergedRegions, sheet).trim(),0,100));  // 分类
					jsonObject.put("rechargeReason", StringUtils.substring(getCellValueAsString(currentRow.getCell(9), mergedRegions, sheet).trim(),0,100));  // 充值原因
					jsonObject.put("rechargeDescription", StringUtils.substring(getCellValueAsString(currentRow.getCell(10), mergedRegions, sheet).trim(),0,100));  // 具体场景描述
					jsonObject.put("rechargeMethod", rechargeMethod);  // 充值方式
					jsonObject.put("handlingPersonnel", getCellValueAsString(currentRow.getCell(12), mergedRegions, sheet).trim());  // 处理人
					jsonObject.put("examiner", getCellValueAsString(currentRow.getCell(13), mergedRegions, sheet).trim());  // 审批人
					jsonObject.put("cardNumber", getCellValueAsString(currentRow.getCell(14), mergedRegions, sheet).trim());  // 卡号
					jsonObject.put("cardSecret", cardSecret);  // 卡密
					jsonObject.put("faceValue", faceValue);  // 面值
					jsonObject.put("receiver", StringUtils.substring(getCellValueAsString(currentRow.getCell(17), mergedRegions, sheet).trim(),0,30));  // 领用人
					jsonObject.put("arrivalStatus", getCellValueAsString(currentRow.getCell(18), mergedRegions, sheet).trim());  // 是否到账
					jsonArray.add(jsonObject);
				} catch (Exception e) {
					errorInfo = "第" + currentRow.getRowNum() + "行，数据有误请核实：" + e.getMessage();
					log.error("importFile 导入数据失败,error:{}",errorInfo,e.getMessage());
					return errorInfo;
				}
			}
			if (addData(jsonArray, fileName, logonName) < 1) {
				errorInfo = "插入数据失败，请检查文件数据";
				log.error("importFile 插入数据失败,error:{}",errorInfo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorInfo = "文件解析异常";
		}
		return errorInfo;
	}

	private boolean isNumeric(String str) {
		if (str == null) {
			return false;
		}
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	private boolean isValidAmount(String str) {
		if (str == null) {
			return false;
		}
		try {
			Double.parseDouble(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return str.matches("^[0-9]+(\\.[0-9]{1,2})?$");
	}

	private String getCellValueAsString(Cell cell, List<CellRangeAddress> mergedRegions, Sheet sheet) {
		if (cell == null) {
			return "";
		}
		for (CellRangeAddress range : mergedRegions) {
			if (range.isInRange(cell.getRowIndex(), cell.getColumnIndex())) {
				// 如果单元格在合并单元格范围内，返回合并单元格的起始单元格的值
				Row mergedRow = sheet.getRow(range.getFirstRow());
				Cell mergedCell = mergedRow.getCell(range.getFirstColumn());
				return getCellValueAsString(mergedCell);
			}
		}
		return getCellValueAsString(cell);
	}

	private String getCellValueAsString(Cell cell) {
		if (cell == null) {
			return "";
		}
		switch (cell.getCellTypeEnum()) {
			case STRING:
				return cell.getStringCellValue();
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {
					Date date = cell.getDateCellValue();
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					return dateFormat.format(date);
				} else {
					// 确保读取的数值为字符串
					cell.setCellType(CellType.STRING);
					return cell.getStringCellValue();
				}
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case FORMULA:
				return cell.getCellFormula();
			case BLANK:
				return "";
			default:
				return "";
		}
	}

	//检查空行
	private boolean isRowEmpty(Row row) {
		if (row == null) {
			return true;
		}
		for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
			Cell cell = row.getCell(cellNum);
			if (cell != null && cell.getCellTypeEnum() != CellType.BLANK) {
				return false;
			}
		}
		return true;
	}

	@Override
	public GridDataInfo getRechargeData(int begion, int pageSize, String strWhere){
		StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% FROM CC_RECHARGE_TASK_INFORMATION");
		sql1.append(strWhere);
		sql1.append(" ORDER BY RELEASE_TIME DESC ");
		String s1 = sql1.toString();
		String strSql = s1.replace("%PARAM%"," UNIQUE_FLOW,PUBLISHER,INITIATOR,RECHARGE_ID,STATUS,FILE_NAME,DATE_FORMAT(RELEASE_TIME, '%Y-%m-%d %H:%i:%s') AS RELEASE_TIME," +
				" DATE_FORMAT(START_TIME, '%Y-%m-%d %H:%i:%s') AS START_TIME,DATE_FORMAT(END_TIME, '%Y-%m-%d %H:%i:%s') AS END_TIME,COMPLETION_FLAG") ;
		String countSql = s1.replace("%PARAM%", "COUNT(1)");
		return this.dbgridDataPub.getResultNewBySize(countSql, strSql, begion, pageSize, "", "");
	}

	@Override
	public GridDataInfo getRechargeItemData(int begion, int pageSize, String uniqueFlow){
		StringBuilder sql1 = new StringBuilder().append("SELECT %PARAM% FROM CC_RECHARGE_ITEM_INFORMATION");
		sql1.append(" WHERE TASK_INFORMATION_FLOW = '"+uniqueFlow+"'");
		sql1.append(" ORDER BY OPERATION_TIME ");
		String s1 = sql1.toString();
		String strSql = s1.replace("%PARAM%"," UNIQUE_FLOW, TASK_INFORMATION_FLOW, ORDER_ID, REGION, PROD_NUM, RECHARGE_NUMBER, RECHARGE_NUMBER_TYPE, RECHARGE_NUMBER_USER_NAME, " +
				"  RECHARGE_AMOUNT, RECHARGE_CLASSIFICATION, RECHARGE_REASON, RECHARGE_DESCRIPTION, RECHARGE_METHOD, HANDLING_PERSONNEL, EXAMINER, RECHARGE_CARD_NUMBER, " +
				"  CHARGE_CARD_PASSWORD, RECHARGE_CARD_VALUE, RECEIVER, RECEIVED_OR_NOT, RECHARGE_RESULT, FAILURE_REASON,DATE_FORMAT(OPERATION_TIME, '%Y-%m-%d %H:%i:%s') AS OPERATION_TIME ") ;
		String countSql = s1.replace("%PARAM%", "COUNT(1)");
		return this.dbgridDataPub.getResultNewBySize(countSql, strSql, begion, pageSize, "", "");
	}

	private int addData(JSONArray array,String fileName,String logonName) {
		int num = 0;
		String uuid = IdUtils.fastSimpleUUID();
		if(addTaskData(fileName,uuid,logonName)>0){
			num = addItemData(array,uuid);
		}
		return num;
	}

	private int addTaskData(String fileName,String uuid,String logonName) {
		int num = 0;
		try{
			TsmStaff logonStaffByLoginName = pubFunc.getLogonStaffByLoginName(logonName);
			String id = logonStaffByLoginName.getId();
			String name = logonStaffByLoginName.getName();
			String strSql = "INSERT INTO CC_RECHARGE_TASK_INFORMATION (UNIQUE_FLOW,EMPLOYEE_NAME,PUBLISHER, INITIATOR, RECHARGE_ID,STATUS,FILE_NAME,RELEASE_TIME) " +
					"VALUES (?,?, ?, ?, ?, '0', ?, now())";
			num = this.jt.update(
					strSql, uuid,StringUtils.defaultIfEmpty(name, null),
					StringUtils.defaultIfEmpty(logonName, null),
					StringUtils.defaultIfEmpty(logonName, null),
					StringUtils.defaultIfEmpty(id, null),
					StringUtils.defaultIfEmpty(fileName, null));
		}catch (Exception e){
			log.error("新增充值任务信息数据失败：error: {}",e.getMessage(),e);
		}
		return num;
	}

	private int addItemData(JSONArray array,String uuid) {
		int num = 0;
		try{
			String strSql = "INSERT INTO CC_RECHARGE_ITEM_INFORMATION (UNIQUE_FLOW,TASK_INFORMATION_FLOW, ORDER_ID, REGION, PROD_NUM, " +
					"RECHARGE_NUMBER, RECHARGE_NUMBER_TYPE, RECHARGE_NUMBER_USER_NAME, RECHARGE_AMOUNT, RECHARGE_CLASSIFICATION, RECHARGE_REASON, " +
					"RECHARGE_DESCRIPTION, RECHARGE_METHOD, HANDLING_PERSONNEL,EXAMINER,RECHARGE_CARD_NUMBER,CHARGE_CARD_PASSWORD,RECHARGE_CARD_VALUE," +
					"RECEIVER,RECEIVED_OR_NOT) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

			for (int i = 0; i < array.size(); i++) {
				JSONObject jsonObject = array.getJSONObject(i);
				String orderId = StringUtils.defaultIfEmpty(jsonObject.getString("orderId"), "");
				String region = StringUtils.defaultIfEmpty(jsonObject.getString("region"), "");
				String prodNum = StringUtils.defaultIfEmpty(jsonObject.getString("prodNum"), "");
				String rechargeNumber = StringUtils.defaultIfEmpty(jsonObject.getString("rechargeNumber"), "");
				String rechargeNumberType = StringUtils.defaultIfEmpty(jsonObject.getString("rechargeNumberType"), "");
				String rechargeNumberUserName = StringUtils.defaultIfEmpty(jsonObject.getString("rechargeNumberUserName"), "");
				String rechargeAmount = StringUtils.defaultIfEmpty(jsonObject.getString("rechargeAmount"), "");
				String rechargeClassification = StringUtils.defaultIfEmpty(jsonObject.getString("rechargeClassification"), "");
				String rechargeReason = StringUtils.defaultIfEmpty(jsonObject.getString("rechargeReason"), "");
				String rechargeDescription = StringUtils.defaultIfEmpty(jsonObject.getString("rechargeDescription"), "");
				String rechargeMethod = StringUtils.defaultIfEmpty(jsonObject.getString("rechargeMethod"), "");
				String handlingPersonnel = StringUtils.defaultIfEmpty(jsonObject.getString("handlingPersonnel"), "");
				String examiner = StringUtils.defaultIfEmpty(jsonObject.getString("examiner"), "");
				String cardNumber = StringUtils.defaultIfEmpty(jsonObject.getString("cardNumber"), "");
				String cardSecret = StringUtils.defaultIfEmpty(jsonObject.getString("cardSecret"), "");
				String faceValue = StringUtils.defaultIfEmpty(jsonObject.getString("faceValue"), "");
				String receiver = StringUtils.defaultIfEmpty(jsonObject.getString("receiver"), "");
				String arrivalStatus = StringUtils.defaultIfEmpty(jsonObject.getString("arrivalStatus"), "");
				this.jt.update(strSql,IdUtils.fastSimpleUUID(),
						StringUtils.defaultIfEmpty(uuid, null),StringUtils.defaultIfEmpty(orderId, null),
						StringUtils.defaultIfEmpty(region, null),StringUtils.defaultIfEmpty(prodNum, null),
						StringUtils.defaultIfEmpty(rechargeNumber, null),StringUtils.defaultIfEmpty(rechargeNumberType, null),
						StringUtils.defaultIfEmpty(rechargeNumberUserName, null),StringUtils.defaultIfEmpty(rechargeAmount, null),
						StringUtils.defaultIfEmpty(rechargeClassification, null),StringUtils.defaultIfEmpty(rechargeReason, null),
						StringUtils.defaultIfEmpty(rechargeDescription, null),StringUtils.defaultIfEmpty(rechargeMethod, null),
						StringUtils.defaultIfEmpty(handlingPersonnel, null),StringUtils.defaultIfEmpty(examiner, null),
						StringUtils.defaultIfEmpty(cardNumber, null),StringUtils.defaultIfEmpty(cardSecret, null),
						StringUtils.defaultIfEmpty(faceValue, null),StringUtils.defaultIfEmpty(receiver, null),
						StringUtils.defaultIfEmpty(arrivalStatus, null)
				);
				num++;
			}
		}catch (Exception e){
			log.error("新增充值任务项信息数据失败：error: {}",e.getMessage(),e);
		}
		return num;
	}

	@Override
	@Transactional
	public String startRechargeTask(String uniqueFlow) {
		String sql = "SELECT UNIQUE_FLOW, TASK_INFORMATION_FLOW, ORDER_ID, REGION, PROD_NUM, RECHARGE_NUMBER, RECHARGE_NUMBER_TYPE, " +
				"RECHARGE_NUMBER_USER_NAME, RECHARGE_AMOUNT, RECHARGE_CLASSIFICATION, RECHARGE_REASON, RECHARGE_DESCRIPTION, " +
				"RECHARGE_METHOD, HANDLING_PERSONNEL, EXAMINER, RECHARGE_CARD_NUMBER, CHARGE_CARD_PASSWORD, RECHARGE_CARD_VALUE, " +
				"RECEIVER, RECEIVED_OR_NOT, RECHARGE_RESULT, FAILURE_REASON, OPERATION_TIME " +
				"FROM CC_RECHARGE_ITEM_INFORMATION WHERE TASK_INFORMATION_FLOW = ?";

		List<Map<String, Object>> maps = jt.queryForList(sql, uniqueFlow);
		List<String> distinctPasswords = maps.stream()
				.map(map -> (String) map.get("CHARGE_CARD_PASSWORD"))
				.distinct()
				.collect(Collectors.toList());
		Set<String> rechargeResult = new HashSet<>();
		String msgResult = "";
		JSONArray rechageInfo = new JSONArray();
		JSONArray errorData = new JSONArray();
		for (String cardPin : distinctPasswords) {
			List<Map<String, Object>> matchingObjects = maps.stream()
					.filter(map -> cardPin.equals(map.get("CHARGE_CARD_PASSWORD")))
					.collect(Collectors.toList());
			if (matchingObjects.isEmpty()) {
				continue;
			}
				JSONObject req = buildRequest(matchingObjects);
				String response = interfaceFeign.startRechargeTask(req.toString());
				if (StringUtils.isNotBlank(response)) {
					JSONObject respData = JSON.parseObject(response);
					String status = respData.getString("status");
					rechargeResult.add(status);
					if (("2".equals(status) || "1".equals(status)) && respData.getJSONObject("errorData")!=null) {
						errorData.add(respData.getJSONObject("errorData"));
					}
					if(("2".equals(status) || "1".equals(status)) && respData.getJSONArray("rechageInfo")!=null){
						rechageInfo.add(respData.getJSONArray("rechageInfo"));
					}
				}
		}
		updateStatusAndTime(uniqueFlow, "1", "start");
		msgResult = determineFinalResult(uniqueFlow, rechargeResult, rechageInfo, errorData);
		return msgResult;
	}

	private JSONObject buildRequest(List<Map<String, Object>> matchingObjects) {
		JSONObject req = new JSONObject();
		Map<String, Object> firstMatch = matchingObjects.get(0);
		req.put("accNbr", firstMatch.get("RECHARGE_NUMBER"));
		req.put("appId", "93277448");
		req.put("areaCode", "025");
		req.put("businessType", 53);
		JSONArray offers = new JSONArray();
		JSONObject offer = new JSONObject();
		offer.put("id", "410000000007");
		offers.add(offer);
		req.put("offers", offers);

		JSONObject uocCardRechargeOrder = new JSONObject();
		String rechargeMethod = (String) firstMatch.get("RECHARGE_METHOD");
		uocCardRechargeOrder.put("operationCode", "全额充值".equals(rechargeMethod) ? "0" : "2");

		JSONArray uocCardRechargeOrderDetailList = new JSONArray();
		for (Map<String, Object> matchingObject : matchingObjects) {
			JSONObject detail = new JSONObject();
			detail.put("cardPin", matchingObject.get("CHARGE_CARD_PASSWORD"));
			String rechargeNumberType = (String) matchingObject.get("RECHARGE_NUMBER_TYPE");
			detail.put("destinationAttr", getDestinationAttr(rechargeNumberType));
			detail.put("destinationId", matchingObject.get("RECHARGE_NUMBER"));
			detail.put("uniqueFlow", matchingObject.get("UNIQUE_FLOW"));
			detail.put("rechargeAmount", Integer.parseInt(matchingObject.get("RECHARGE_AMOUNT").toString()) * 100);
			uocCardRechargeOrderDetailList.add(detail);
		}
		uocCardRechargeOrder.put("uocCardRechargeOrderDetailList", uocCardRechargeOrderDetailList);
		req.put("uocCardRechargeOrder", uocCardRechargeOrder);

		return req;
	}

	private String determineFinalResult(String uniqueFlow, Set<String> rechargeResult, JSONArray rechageInfo, JSONArray errorData) {
		String msgResult;
		if (rechargeResult.size() == 1) {
			String result = rechargeResult.iterator().next();
			switch (result) {
				case "0":
					msgResult = "全部成功";
					updateStatusAndTime(uniqueFlow, "2", "end");
					updateCompletionFlag(uniqueFlow, "0");
					updateRechageItemInfo(uniqueFlow);
					break;
				case "1":
					msgResult = "全部失败";
					updateStatusAndTime(uniqueFlow, "2", "end");
					updateCompletionFlag(uniqueFlow, "1");
					updateRechageItem(uniqueFlow, rechageInfo, errorData, true);
					break;
				default:
					msgResult = "部分成功";
					updateStatusAndTime(uniqueFlow, "2", "end");
					updateCompletionFlag(uniqueFlow, "2");
					updateRechageItem(uniqueFlow, rechageInfo, errorData, true);
					break;
			}
		} else if (rechargeResult.isEmpty()) {
			msgResult = "全部失败";
			updateStatusAndTime(uniqueFlow, "2", "end");
			updateCompletionFlag(uniqueFlow, "1");
			updateRechageItem(uniqueFlow, rechageInfo, errorData, false);
		} else {
			msgResult = "部分成功";
			updateStatusAndTime(uniqueFlow, "2", "end");
			updateCompletionFlag(uniqueFlow, "2");
			updateRechageItem(uniqueFlow, rechageInfo, errorData, true);
		}
		return msgResult;
	}

	private void updateRechageItem(String uniqueFlow, JSONArray info,JSONArray errorData,boolean successFlag) {
		try {
			if(!successFlag){//接口调用失败更新状态
				String strSql = "UPDATE CC_RECHARGE_ITEM_INFORMATION SET RECHARGE_RESULT = '1', RECEIVED_OR_NOT = '1',FAILURE_REASON = '接口调用错误',OPERATION_TIME = now() " +
						"WHERE TASK_INFORMATION_FLOW = ?";
				this.jt.update(strSql, uniqueFlow);
				return;
			}
			// 先将这条流水下的全部任务改为充值成功，再将失败的改为失败
			String strSql1 = "UPDATE CC_RECHARGE_ITEM_INFORMATION SET RECHARGE_RESULT = '0', RECEIVED_OR_NOT = '0',OPERATION_TIME = now() " +
					"WHERE TASK_INFORMATION_FLOW = ?";
			this.jt.update(strSql1, uniqueFlow);
			for (int i = 0; i < info.size(); i++) {
				JSONArray jsonArray = JSON.parseArray(info.get(i).toString());
				for (int i1 = 0; i1 < jsonArray.size(); i1++) {
					JSONObject jsonObject = JSON.parseObject(jsonArray.get(i1).toString());
					String phoneNumber = jsonObject.getString("destinationId"); // 充值手机号码
					String cardPin = jsonObject.getString("cardPin"); // 充值卡密
					String msg = jsonObject.getString("msg"); // 充值失败错误信息
					String errorCode = jsonObject.getString("errorCode"); // 充值失败错误编码
					String failureReason = errorCode + "-" + msg;
					String strSql2 = "UPDATE CC_RECHARGE_ITEM_INFORMATION SET RECHARGE_RESULT = '1', RECEIVED_OR_NOT = '1',FAILURE_REASON = ?, OPERATION_TIME = now() " +
							"WHERE TASK_INFORMATION_FLOW = ? AND RECHARGE_NUMBER = ? AND CHARGE_CARD_PASSWORD = ?";
					this.jt.update(strSql2, failureReason, uniqueFlow, phoneNumber, cardPin);
				}
			}
			if(!errorData.isEmpty()){
				for (int i1 = 0; i1 < errorData.size(); i1++) {
					JSONObject jsonObject = errorData.getJSONObject(i1);
					String errorCode = jsonObject.getString("errorCode");
					String errorMsg = jsonObject.getString("errorMsg");
					String failureReason = errorCode + "-" + errorMsg;
					JSONArray uniqueFlows = jsonObject.getJSONArray("uniqueFlows");
					for (int i = 0; i < uniqueFlows.size(); i++) {
						String errorUniqueFlow = uniqueFlows.getString(i);
						String strSql = "UPDATE CC_RECHARGE_ITEM_INFORMATION SET RECHARGE_RESULT = '1', RECEIVED_OR_NOT = '1',FAILURE_REASON = ?,OPERATION_TIME = now() " +
								"WHERE TASK_INFORMATION_FLOW = ? AND UNIQUE_FLOW = ?";
						this.jt.update(strSql, failureReason,uniqueFlow,errorUniqueFlow);
					}
				}
			}
		} catch (Exception e) {
			log.error("updateRechageItem error: {}", e.getMessage(), e);
		}
	}

	private int updateStatusAndTime(String uniqueFlow,String status,String timeFlag){
		int result = 0;
		String strSql = "";
		try{
			if("start".equals(timeFlag)){
				strSql = "UPDATE CC_RECHARGE_TASK_INFORMATION SET STATUS = ?,START_TIME = now() where UNIQUE_FLOW = ?";
			}else {
				strSql = "UPDATE CC_RECHARGE_TASK_INFORMATION SET STATUS = ?,END_TIME = now() where UNIQUE_FLOW = ?";
			}
			result = this.jt.update(strSql,status,uniqueFlow);
		}catch (Exception e){
			log.error("updateStatusAndTime error: {}",e.getMessage(),e);
		}
		return result;
	}

	private int updateCompletionFlag(String uniqueFlow,String flag){
		int result = 0;
		try{
			String strSql = "UPDATE CC_RECHARGE_TASK_INFORMATION SET COMPLETION_FLAG = ? where UNIQUE_FLOW = ?";
			result = this.jt.update(strSql,flag,uniqueFlow);
		}catch (Exception e){
			log.error("updateCompletionFlag error: {}",e.getMessage(),e);
		}
		return result;
	}

	private int updateRechageItemInfo(String uniqueFlow){
		int result = 0;
		try{
			String strSql = "UPDATE CC_RECHARGE_ITEM_INFORMATION SET RECHARGE_RESULT = '0' ,RECEIVED_OR_NOT = '0',OPERATION_TIME = now() where TASK_INFORMATION_FLOW = ?";
			result = this.jt.update(strSql,uniqueFlow);
		}catch (Exception e){
			log.error("updateRechageItemInfo error: {}",e.getMessage(),e);
		}
		return result;
	}

	private String getDestinationAttr(String rechargeNumberType) {
		String type = "";
		switch (rechargeNumberType) {
			case "固话":
				type =  "0";
				break;
			case "手机":
				type =   "2";
				break;
			case "宽带":
				type =   "3";
				break;
			case "翼支付帐号":
				type =   "6";
				break;
			case "学子E行":
				type =   "54";
				break;
			case "C+W账号":
				type =   "55";
				break;
			default:
				break;
		}
		return type;
	}

}