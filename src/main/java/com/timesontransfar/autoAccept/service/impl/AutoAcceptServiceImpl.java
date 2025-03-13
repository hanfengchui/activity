package com.timesontransfar.autoAccept.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.timesontransfar.autoAccept.dao.IAutoAcceptDao;
import com.timesontransfar.autoAccept.pojo.AutoAcceptOrder;
import com.timesontransfar.autoAccept.pojo.ZQCustInfoData;
import com.timesontransfar.autoAccept.service.IAutoAcceptService;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.OrderCustomerInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.feign.custominterface.PortalInterfaceFeign;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;

@Service
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AutoAcceptServiceImpl implements IAutoAcceptService {
	private static final Logger log = LoggerFactory.getLogger(AutoAcceptServiceImpl.class);

	@Autowired
    private PubFunc pubFun;
	
	@Autowired
    private IAutoAcceptDao autoAcceptDao;
	
	@Autowired
    private IserviceOrderAsk serviceOrderAskImplFACADE;
	
	@Autowired
	@Qualifier("customerServiceFeign")
	private CustomerServiceFeign customerServiceFeign;
	
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	
	@Autowired
	private IorderAskInfoDao orderAskInfoDao;
	
	@Autowired
	private PortalInterfaceFeign portalFeign;
	
	private static Map askChannels = new HashMap();
	static {
			askChannels.put("工信部", "707907026");
			askChannels.put("省管局", "707907027");
	}
	
	private static Map regions = new HashMap();
	static {
			regions.put("南京市", "3");
			regions.put("镇江", "4");
			regions.put("无锡", "15");
			regions.put("苏州", "20");
			regions.put("南通", "26");
			regions.put("扬州", "33");
			regions.put("盐城", "39");
			regions.put("徐州", "48");
			regions.put("淮安", "60");
			regions.put("连云港", "63");
			regions.put("常州", "69");
			regions.put("泰州", "79");
			regions.put("宿迁", "84");
	}
	
	private static Map prodTypes = new HashMap();
	static {
		prodTypes.put("固话", "1");
		prodTypes.put("宽带", "2");
		prodTypes.put("CDMA", "9");
		prodTypes.put("ITV", "881");
	}
    

    public String saveOrderInfoBatch(String username, InputStream file) {
        int length = 0;
        String errorInfo = "";
        TsmStaff staff = pubFun.getLogonStaffByLoginName(username);
        
        try {
            Workbook book = Workbook.getWorkbook(file);
            Sheet sheet = book.getSheet(0);
            int rows = sheet.getRows(); // 行数
            
            List orderList = new ArrayList();
            for (int i = 1; i < rows; i++) { // 逐行分析
            	
            	String askChannelId = "";
            	String askChannelDesc = sheet.getCell(0, i).getContents(); // 受理渠道
                if(null == askChannelDesc || "".equals(askChannelDesc)){
                	errorInfo = "第"+i+"行受理渠道为空 <br> 请重新配置！";
                    return errorInfo;
                }else if(null == askChannels.get(askChannelDesc)){
                	errorInfo = "第"+i+"行受理渠道不正确 <br> 请重新配置！";
                    return errorInfo;
                }else{
                	askChannelId = askChannels.get(askChannelDesc).toString();
                }
                
                String regionId = "0";
                String regionName = sheet.getCell(1, i).getContents(); // 归属地
                if("工信部".equals(askChannelDesc)){
                	regionName = "";
                }else if("省管局".equals(askChannelDesc)){
                	 if(null == regionName || "".equals(regionName)){
                		 errorInfo = "第"+i+"行归属地为空 <br> 请重新配置！";
                         return errorInfo;
                     }else if(null == regions.get(regionName)){
                     	errorInfo = "第"+i+"行归属地不正确 <br> 请重新配置！";
                         return errorInfo;
                     }else{
                    	 regionId = regions.get(regionName).toString();
                     }
                }
                
                String prodNum = sheet.getCell(2, i).getContents(); // 产品号码
                if(null == prodNum || "".equals(prodNum)){
           		 	errorInfo = "第"+i+"行产品号码为空 <br> 请重新配置！";
                    return errorInfo;
                }
                
                String prodType = "";
                String prodTypeDesc = sheet.getCell(3, i).getContents(); //产品类型
                if(null == prodTypeDesc || "".equals(prodTypeDesc)){
           		 	errorInfo = "第"+i+"行产品类型为空 <br> 请重新配置！";
                    return errorInfo;
                }else if(null == prodTypes.get(prodTypeDesc)){
                	errorInfo = "第"+i+"行产品类型不正确 <br> 请重新配置！";
                    return errorInfo;
                }else{
                	prodType = prodTypes.get(prodTypeDesc).toString();
                }
                
                String custName = sheet.getCell(4, i).getContents(); // 客户姓名
                if(null == custName || "".equals(custName)){
           		 	errorInfo = "第"+i+"行客户姓名为空 <br> 请重新配置！";
                    return errorInfo;
                }
                
                String relaMan = sheet.getCell(5, i).getContents(); // 联系人
                if(null == relaMan || "".equals(relaMan)){
           		 	errorInfo = "第"+i+"行联系人为空 <br> 请重新配置！";
                    return errorInfo;
                }
                
                String relaInfo = sheet.getCell(6, i).getContents(); // 联系电话
                if(null == relaInfo || "".equals(relaInfo)){
           		 	errorInfo = "第"+i+"行联系电话为空 <br> 请重新配置！";
                    return errorInfo;
                }
                
                String ssFlow = sheet.getCell(7, i).getContents(); // 申诉信息流水号
                if(null == ssFlow || "".equals(ssFlow)){
           		 	errorInfo = "第"+i+"行申诉信息流水号为空 <br> 请重新配置！";
                    return errorInfo;
                }
                
                String acceptContent = sheet.getCell(8, i).getContents(); // 受理内容
                if(null == acceptContent || "".equals(acceptContent)){
           		 	errorInfo = "第"+i+"行受理内容为空 <br> 请重新配置！";
                    return errorInfo;
                }
                
                AutoAcceptOrder order = new AutoAcceptOrder();
                order.setAskChannelId(Integer.parseInt(askChannelId));
                order.setAskChannelDesc(askChannelDesc);
                order.setRegionId(Integer.parseInt(regionId));
                order.setRegionName(regionName);
                order.setProdNum(prodNum);
                order.setProdType(prodType);
                order.setProdTypeDesc(prodTypeDesc);
                order.setCustName(custName);
                order.setRelaMan(relaMan);
                order.setRelaInfo(relaInfo);
                order.setSsFlow(ssFlow);
                order.setAcceptContent(acceptContent);
                order.setCreateStaff(staff.getId());
                order.setStatu(0);
                order.setStatuDesc("待分析");
                orderList.add(order);
            }
            book.close();
            length = autoAcceptDao.saveAutoAcceptOrderPatch(orderList);
            errorInfo = "导入申诉单"+length+"条";
        } catch (BiffException | IOException e) {
            errorInfo = "文件解析异常";
        }
        return errorInfo;
    }
    
    public int analyseData(String staffId){
    	String condition = " AND STATU IN (0,1)";
    	List orderList = autoAcceptDao.getAutoAcceptOrderListByCondition(condition);
    	int size = 0;
    	if(null != orderList){
    		for(int i=0;i<orderList.size();i++){
    			AutoAcceptOrder order = (AutoAcceptOrder)orderList.get(i);
    			order.setServType(720130000);//性质类别
    			order.setServTypeDesc("投诉");
    			order.setComeCategory(707907003);//投诉级别
    			order.setCategoryName("申诉");
    			order.setAskSource(707907006);//受理来源
    			order.setAskSourceDesc("政府监管渠道");
    			order.setStatu(1);
				order.setStatuDesc("分析失败");
    			if(order.getAskChannelId()==707907026){//工信部
    				order.setChannelDetailId(707907124);
        			order.setChannelDetailDesc("部自行处理");
        			
        			boolean flag = false;
        			if("9".equals(order.getProdType()) && order.getProdNum().length()>=7){
        				String regionName = this.getRegionName(order.getProdNum().substring(0, 7));
        				if(StringUtils.isNotEmpty(regionName)){
        					order.setRegionId(Integer.parseInt(regions.get(regionName).toString()));
        					order.setRegionName(regionName);
        					order.setStatu(2);
        					order.setStatuDesc("待录单");
        					flag = true;
        				}
        			}
        			if(!flag && order.getRelaInfo().length()>=7){
        				String regionName = this.getRegionName(order.getRelaInfo().substring(0, 7));
        				if(StringUtils.isNotEmpty(regionName)){
        					order.setRegionId(Integer.parseInt(regions.get(regionName).toString()));
        					order.setRegionName(regionName);
        					order.setStatu(2);
        					order.setStatuDesc("待录单");
            			}
        			}
        			
    			}else if(order.getAskChannelId()==707907027){//省管局
    				order.setChannelDetailId(707907085);
        			order.setChannelDetailDesc("其它协办");
        			order.setStatu(2);
    				order.setStatuDesc("待录单");
    			}
    			order.setAcceptContent(order.getSsFlow()+";"+order.getAcceptContent());
    			order.setModifyStaff(staffId);
    		}
    		size = autoAcceptDao.updateAutoAcceptOrderList(orderList);
    	}
    	
    	return size;
    }
    
    public String submitOrder(int staffId){
    	TsmStaff staff = pubFun.getStaff(staffId);
    	String condition = " AND STATU IN (2,3)";
    	List orderList = autoAcceptDao.getAutoAcceptOrderListByCondition(condition);
    	int success = 0;
    	int fail = 0;
    	if(null != orderList){
    		for(int i=0;i<orderList.size();i++){
    			AutoAcceptOrder order = (AutoAcceptOrder)orderList.get(i);
    			String prodType = order.getProdType();
    			String prodNum = order.getProdNum();
    			int regionId = order.getRegionId();
    			
    			String getReulst = customerServiceFeign.getCustInfo(Integer.parseInt(prodType), prodNum, regionId);
    			JSONObject custInfo = JSONObject.fromObject(getReulst);
    			OrderCustomerInfo orderCustInfo = (OrderCustomerInfo)JSONObject.toBean(custInfo.getJSONObject("resultObj"),OrderCustomerInfo.class);
    			if (orderCustInfo == null || orderCustInfo.getCustName().equals("")) {
    				order.setModifyStaff(staff.getId());
    				order.setStatu(3);
    				order.setStatuDesc("录单失败");
    				fail ++;
    				continue;
    			}
    			try {
    				int areaId = orderCustInfo.getRegionId();
    				
    				orderCustInfo.setRegionId(regionId);
    				orderCustInfo.setRegionName(order.getRegionName());
    				orderCustInfo.setCustTypeName(pubFun.getStaticName(orderCustInfo.getCustType()));
    				orderCustInfo.setCustServGradeName(pubFun.getStaticName(orderCustInfo.getCustServGrade()));
    				orderCustInfo.setCustBrandDesc(pubFun.getStaticName(orderCustInfo.getCustBrand()));
    				
    				ServiceContent servContent = new ServiceContent();
    				servContent.setOrderVer(0);
    				servContent.setRegionId(regionId);
    				servContent.setRegionName(order.getRegionName());
    				servContent.setServType(order.getServType());
    				servContent.setServTypeDesc(order.getServTypeDesc());
    				//移动业务 > 规则政策类 > 业务规则不合理 > 业务规则不合理 > 业务规则不合理 > 业务规则不合理
    				//10000001-23000073-23000348-23000920-23001610-23002300
    				/* 2023-07 替换新现象
    				servContent.setAppealProdId(10000001);
    				servContent.setAppealProdName("移动业务");
    				servContent.setAppealReasonId(23000073);
    				servContent.setAppealReasonDesc("规则政策类");
    				servContent.setAppealChild(23000348);
    				servContent.setAppealChildDesc("业务规则不合理");
    				servContent.setFouGradeCatalog(23000920);
    				servContent.setFouGradeDesc("业务规则不合理");
    				servContent.setFiveCatalog(23001610);
    				servContent.setFiveGradeDesc("业务规则不合理");
    				servContent.setSixCatalog(23002300);
    				servContent.setSixGradeDesc("业务规则不合理");
    				*/
    				servContent.setAppealProdId(105);
    				servContent.setAppealProdName("规则政策类");
    				servContent.setAppealReasonId(10599);
    				servContent.setAppealReasonDesc("省自定2");
    				servContent.setAppealChild(0);
    				servContent.setAppealChildDesc("");
    				servContent.setFouGradeCatalog(0);
    				servContent.setFouGradeDesc("");
    				servContent.setFiveCatalog(0);
    				servContent.setFiveGradeDesc("");
    				servContent.setSixCatalog(0);
    				servContent.setSixGradeDesc("");
    				servContent.setProdOne(501);
    				servContent.setProdOneDesc("手机");
    				servContent.setProdTwo(501010);
    				servContent.setProdTwoDesc("其他移动业务");
    				servContent.setDevtChsOne(120000);
    				servContent.setDevtChsOneDesc("电子渠道");
    				servContent.setDevtChsTwo(120500);
    				servContent.setDevtChsTwoDesc("客服型电子渠道");
    				servContent.setDevtChsThree(120516);
    				servContent.setDevtChsThreeDesc("其他");
    				
    				servContent.setProdNum(prodNum);
    				servContent.setAcceptContent(order.getAcceptContent());
    				
    				OrderAskInfo orderAskInfo = new OrderAskInfo();
    				orderAskInfo.setRelaType(10599);
    				orderAskInfo.setRegionId(regionId);
    				orderAskInfo.setOrderVer(0);
    				orderAskInfo.setRegionName(order.getRegionName());
    				orderAskInfo.setServType(order.getServType());
    				orderAskInfo.setServTypeDesc(order.getServTypeDesc());
    				orderAskInfo.setRelaMan(order.getRelaMan());
    				orderAskInfo.setProdNum(prodNum);
    				orderAskInfo.setRelaInfo(order.getRelaInfo());
    				orderAskInfo.setIsOwner(1);
    				orderAskInfo.setCustEmotion(700000025);
    				orderAskInfo.setCustEmotionDesc("平和");
    				orderAskInfo.setComeCategory(order.getComeCategory());
    				orderAskInfo.setCategoryName(order.getCategoryName());
    				orderAskInfo.setAskSource(order.getAskSource());
    				orderAskInfo.setAskSourceDesc(order.getAskSourceDesc());
    				orderAskInfo.setAskChannelId(order.getAskChannelId());
    				orderAskInfo.setAskChannelDesc(order.getAskChannelDesc());
    				orderAskInfo.setChannelDetailId(order.getChannelDetailId());
    				orderAskInfo.setChannelDetailDesc(order.getChannelDetailDesc());
    				orderAskInfo.setAskStaffId(Integer.parseInt(staff.getId()));
    				orderAskInfo.setAskStaffName(staff.getName());
    				orderAskInfo.setAskOrgId(staff.getOrganizationId());
    				orderAskInfo.setAskOrgName(staff.getOrgName());
    				orderAskInfo.setCustServGrade(orderCustInfo.getCustServGrade());
    				orderAskInfo.setCustServGradeDesc(orderCustInfo.getCustServGradeName());
    				orderAskInfo.setUrgencyGrade(700000145);
    				orderAskInfo.setUrgencyGradeDesc("普通");
    				orderAskInfo.setComment("规则政策类 > 省自定2");
    				orderAskInfo.setCustGroup(orderCustInfo.getCustStratagemId());
    				orderAskInfo.setCustGroupDesc(pubFun.getStaticName(orderCustInfo.getCustStratagemId()));
    				orderAskInfo.setServiceDate(3);
    				orderAskInfo.setServiceDateDesc("投诉");
    				orderAskInfo.setAreaId(areaId);
    				orderAskInfo.setAreaName(pubFun.getAreaName(areaId));
    				orderAskInfo.setSendToOrgId("");
    				orderAskInfo.setSendToOrgName("");
    				orderAskInfo.setAskCount(1);
    				orderAskInfo.setMoreRelaInfo("批量受理申诉单");
    				
    				Map audMap = new HashMap();
    				audMap.put("ORGIDSTR", "");
    				audMap.put("STRFLOW", "DISPATCHSHEET");
    				audMap.put("SENDFLAG", "false");
    				audMap.put("DEALREQUIE", "");

    				String resultInfo = serviceOrderAskImplFACADE.submitServiceOrderInstanceLabelNew(orderCustInfo, servContent, orderAskInfo, audMap, null, null, null);
    				String code = JSONObject.fromObject(resultInfo).optString("code");
    				if ("0000".equals(code)) {
    					String servId = JSONObject.fromObject(resultInfo).optString("resultObj");
    					serviceOrderAskImplFACADE.autoPdAsync(servId);// 自动转派
    				}
    				order.setModifyStaff(staff.getId());
    				order.setStatu(4);
    				order.setStatuDesc("录单成功");
    				success ++;
    			} catch (Exception e) {
    				e.printStackTrace();
    				order.setModifyStaff(staff.getId());
    				order.setStatu(3);
    				order.setStatuDesc("录单失败");
    				fail ++;
    			}
    		}
    		autoAcceptDao.updateAutoAcceptOrderListStatu(orderList);
    	}
    	return success+"-"+fail;
    }
    
    public int deleteAutoAcceptOrder(){
    	return autoAcceptDao.deleteAutoAcceptOrder();
    }
    
    public int submitDealContent(ZQCustInfoData data){
    	TsmStaff staff = pubFun.getLogonStaffByLoginName(data.getDealStaffCode());
    	data.setDealStaffName(staff.getName());
    	data.setDealOrgId(staff.getOrganizationId());
    	data.setDealOrgName(staff.getOrgName());
    	data.setRegionId(data.getRegionId());
    	data.setRegionName(data.getRegionName());
    	data.setBusinessDesc(data.getBusinessDesc());
    	data.setProdNum(data.getProdNum());
    	return autoAcceptDao.updateZQCustInfoData(data);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveComplaintInfo(InputStream file, String operator) {
    	log.info("申诉信息导入 operator: {}", operator);
    	
        String errorInfo = "success";
        try {
        	List<ComplaintInfo> modifyList = new ArrayList<>();
        	List<ComplaintInfo> addList = new ArrayList<>();
        	
            ExcelReader reader = ExcelUtil.getReader(file, 0);
            List<Map<String, Object>> mapList = reader.readAll();
            for (int i = 0; i < mapList.size(); i++) {
                Map<String, Object> stringObjectMap = mapList.get(i);
                String mitCode = String.valueOf(stringObjectMap.get("申诉信息流水号"));
                String firstLevel = String.valueOf(stringObjectMap.get("分类码一级"));
                String secondLevel = String.valueOf(stringObjectMap.get("分类码二级"));
                String thirdLevel = String.valueOf(stringObjectMap.get("分类码三级"));
                String complaintSource = String.valueOf(stringObjectMap.get("申诉来源"));
                String complaintDate = String.valueOf(stringObjectMap.get("申诉日期"));
                String ipAddress = String.valueOf(stringObjectMap.get("IP地址"));
                if (StringUtils.isBlank(mitCode) && StringUtils.isBlank(complaintDate)) {
                    errorInfo = "第" + (i+1) + "行“申诉信息流水号”或“申诉日期”有空数据，请补全信息！";
                    break;
                } else {
                	ComplaintInfo info = new ComplaintInfo();
                	info.setMiitCode(mitCode);
                	info.setFirstLevel(firstLevel);
                	info.setSecondLevel(secondLevel);
                	info.setThirdLevel(thirdLevel);
                	info.setComplaintSource(complaintSource);
                	info.setComplaintDate(complaintDate);
                	info.setIpAddress(ipAddress);
                	
                    int complaint = autoAcceptDao.getComplaint(mitCode);
                    if (complaint > 0) {
                    	modifyList.add(info);
                    } else {
                    	addList.add(info);
                    }
                }
            }
            autoAcceptDao.updateComplaintListPatch(modifyList, operator);
            autoAcceptDao.saveComplaintListPatch(addList, operator);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("导入申诉信息时发生异常: {}", e.getMessage(), e);
            errorInfo = "文件解析异常";
        }
        return errorInfo;
    }
    
    public GridDataInfo getComplaintSheet(int currentPage, int pageSize, JSONArray createDate, String state, boolean forRelation) {
		String strWhere = "";
		if (createDate != null && !createDate.isEmpty()) {
			strWhere += " AND c.create_date > STR_TO_DATE('" + createDate.optString(0) + "','%Y-%m-%d %H:%i:%s')";
			strWhere += " AND c.create_date < STR_TO_DATE('" + createDate.optString(1) + "','%Y-%m-%d %H:%i:%s')";
		}
		if (StringUtils.isNotEmpty(state)) {
			strWhere += " AND c.state = " + state;
		}
		if (forRelation) {
			strWhere = "AND c.state in (0,2) AND c.create_date > date_sub(now(), interval 7 day) " +
					"AND c.create_date < now()";
		}
		String sql = "SELECT" +
				" c.MIIT_CODE," +
				" c.COMPLAINT_SOURCE," +
				" DATE_FORMAT(c.COMPLAINT_DATE,'%Y-%m-%d %H:%i:%s') COMPLAINT_DATE," +
				" c.FIRST_LEVEL," +
				" c.SECOND_LEVEL," +
				" c.THIRD_LEVEL," +
				" c.IP_ADDRESS," +
				" CASE c.state " +
				" WHEN 0 THEN " +
				" '待处理' " +
				" WHEN 1 THEN " +
				" '成功' " +
				" WHEN 2 THEN " +
				" '失败' " +
				" ELSE '' END AS STATE,"+
				" DATE_FORMAT(c.CREATE_DATE,'%Y-%m-%d %H:%i:%s') CREATE_DATE," +
				" DATE_FORMAT(c.MODIFY_DATE,'%Y-%m-%d %H:%i:%s') MODIFY_DATE," +
				" CASE c.RELATION_STATUS " +
				" WHEN 0 THEN" +
				" '否'" +
				" WHEN 1 THEN" +
				" '是'" +
				" ELSE" +
				" ''" +
				"END AS RELATION_STATUS " +
				"FROM" +
				" cc_complaint_list c " +
				"WHERE 1=1 ";
		if (!"".equals(strWhere)) {
			sql = sql + strWhere;
		}
		return dbgridDataPub.getResultBySize(sql, currentPage, pageSize, " ORDER BY c.MODIFY_DATE", "申诉信息列表");
	}
    
    @Transactional(rollbackFor = Exception.class)
    public int updateComplaintSheet(int currentPage, int pageSize) {
    	String operator = "";
    	if(pubFun.isLogonFlag()) {
    		TsmStaff staff = pubFun.getLogonStaff();
        	operator = staff.getLogonName()+"("+staff.getName()+")";
    	} else {
    		operator = "系统自动执行";
    	}
    	log.info("申诉信息关联 operator: {}", operator);
    	
		GridDataInfo complaintList = this.getComplaintSheet(currentPage, pageSize, null, null, true);
		List list = complaintList.getList();
		int updateNum = 0;
		try {
			for (Object l : list){
				com.alibaba.fastjson.JSONObject obj = (com.alibaba.fastjson.JSONObject)JSON.toJSON(l);
				ComplaintInfo info = new ComplaintInfo();
	        	info.setMiitCode(obj.getString("MIIT_CODE"));
	        	info.setFirstLevel(obj.getString("FIRST_LEVEL"));
	        	info.setSecondLevel(obj.getString("SECOND_LEVEL"));
	        	info.setThirdLevel(obj.getString("THIRD_LEVEL"));
	        	info.setComplaintSource(obj.getString("COMPLAINT_SOURCE"));
	        	info.setComplaintDate(obj.getString("COMPLAINT_DATE"));
	        	info.setIpAddress(obj.getString("IP_ADDRESS"));
				
	        	int num = orderAskInfoDao.updateComplaintInfoByList(info);
	        	int status = 0;//是否成功关联申诉单（0-否；1-是）
	        	int state = 2;//处理结果 0：待处理；1：处理成功；2：处理失败
	        	if(num > 0) {
	        		status = 1;
	        		state = 1;
	        		updateNum++;
	        	}
	        	autoAcceptDao.updateComplaintList(status, state, operator, info.getMiitCode());
			}
		} catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("updateComplaintSheet异常: {}", e.getMessage(), e);
        }
		log.info("本次查询待处理申诉信息: {} 成功关联: {}", list.size(), updateNum);
		return list.size();
	}
    
    private String getRegionName(String hcode) {
    	String regionId = "";
    	Map map = portalFeign.qryHcodeOfProvincial(hcode);
		if(map != null) {
			regionId = map.get("REGIONID") == null ? "" : map.get("REGIONID").toString();
		}
		String regionName = "";
		if(StringUtils.isNotEmpty(regionId)) {
			regionName = pubFun.getRegionName(Integer.parseInt(regionId));
		}
		return regionName;
    }
}