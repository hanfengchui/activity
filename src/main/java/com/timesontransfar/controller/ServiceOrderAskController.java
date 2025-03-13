package com.timesontransfar.controller;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import com.alibaba.fastjson.JSON;
import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.orderask.pojo.*;
import com.timesontransfar.customservice.orderask.service.RegistrationService;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.transfar.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.autoAccept.pojo.ZQCustInfoData;
import com.timesontransfar.autoAccept.service.IAutoAcceptService;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.uploadFile.IAccessories;
import com.timesontransfar.customservice.common.uploadFile.pojo.FileRelatingInfo;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.orderask.service.ITrackService;
import com.timesontransfar.customservice.orderask.service.IserviceOrderAsk;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSaveSJ;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.customservice.worksheet.pojo.ServiceWorkSheetInfo;
import com.timesontransfar.feign.custominterface.CustomerServiceFeign;
import com.timesontransfar.feign.custominterface.InterfaceFeign;
import com.timesontransfar.feign.custominterface.PortalInterfaceFeign;
import com.timesontransfar.feign.custominterface.WorkSheetFeign;
import com.timesontransfar.pubWebservice.WsKfWorkSheetByITSM;
import com.timesontransfar.pubWebservice.ZHDDService;
import com.timesontransfar.pubWebservice.ZHZDService;
import com.transfar.api.entitys.vo.CommonResult;
import com.transfar.common.aspect.annotation.LogInfo;
import com.transfar.common.enums.ResultEnum;

import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@RestController
@LogInfo
@SuppressWarnings("rawtypes")
public class ServiceOrderAskController {

	private static Logger log = LoggerFactory.getLogger(ServiceOrderAskController.class);

	@Autowired
	private IserviceOrderAsk serviceOrderAskImpl;
	@Autowired
	private IAccessories accessoriesImpl;
	@Autowired
	private IAutoAcceptService autoAcceptServiceImpl;
    @Autowired
    private IserviceContentSchem serviceContentSchem;
	@Autowired
	private ITrackService trackServiceImpl;
	@Autowired
	private RegistrationService registrationService;
	@Autowired
	@Qualifier("customerServiceFeign")
	private CustomerServiceFeign customerServiceFeign;
	@Autowired
	private ZHZDService zhzdService;
	@Autowired
	private WsKfWorkSheetByITSM itsmService;
	@Autowired
	private ZHDDService zhddService;
    @Autowired
    private WorkSheetFeign workSheetFeign;
    @Autowired
    private InterfaceFeign interfaceFeign;
    @Autowired
    private IorderAskInfoDao orderAskInfoDao;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private PortalInterfaceFeign portalFeign;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;


	@PostMapping(value = "/workflow/serviceOrderAsk/checkSelect")
	public String checkSelect(@RequestBody String ins) {
		JSONObject json=JSONObject.fromObject(ins);
		JSONObject servObj=json.getJSONObject("serContent");
		String date=json.getString("serviceDate");
		ServiceContent servContent=(ServiceContent)JSONObject.toBean(servObj, ServiceContent.class);
		return serviceOrderAskImpl.checkSelect(servContent, Integer.parseInt(date));
	}
	/**
	 * 新暂存工单方法
	 * @param minorRepairOrder
	 * @return
	 */
	@SuppressWarnings("serial")
	@PostMapping(value = "/workflow/serviceOrderAsk/allocMinorRepairOrder")
	public String allocMinorRepairOrder(@RequestBody String minorRepairOrder) {
		log.info("新暂存工单入口参数：{}", minorRepairOrder);
		JSONObject zancen = JSONObject.fromObject(minorRepairOrder);
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(zancen.optJSONObject("orderCustInfo"),OrderCustomerInfo.class);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(zancen.optJSONObject("serviceContents"),ServiceContent.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(zancen.optJSONObject("orderAskInfos"),OrderAskInfo.class);
		int serviceDate = zancen.optInt("serviceDate");
		
		String checkFlag = serviceOrderAskImpl.checkSelect(servContent, serviceDate);
		String result = ResultUtil.error(checkFlag);
		if(!checkFlag.equals("SUCCESS")){
			return result;
		}
		
		try {
			result = serviceOrderAskImpl.holdServiceOrderInstance(custInfo, servContent, orderAskInfo);
		} catch (Exception e) {
			log.error("holdServiceOrderInstance 异常：{}", e.getMessage(), e);
			result = ResultUtil.error(e.getMessage());
		}
		String code = JSONObject.fromObject(result).optString("code");
		if("0000".equals(code)){
			String fileListStr = zancen.getString("fileList");
			List<FileRelatingInfo> fileList = new Gson().fromJson(fileListStr, new TypeToken<List<FileRelatingInfo>>(){}.getType());
			if(!fileList.isEmpty()) {
				FileRelatingInfo[] fileArray = new FileRelatingInfo[fileList.size()];
				for(int i=0;i<fileList.size();i++) {
					FileRelatingInfo file = fileList.get(i);
					file.setOrderId(orderAskInfo.getServOrderId());
					file.setRegionId(String.valueOf(orderAskInfo.getRegionId()));
					fileArray[i]=file;
				}
				accessoriesImpl.insertFile(fileArray);
			}

			String serviceContentSave = zancen.optString("serviceContentSave");
			saveZcContentSave(orderAskInfo.getServOrderId(), serviceContentSave);
		}
		return result;
	}

	private void saveZcContentSave(String orderId, String serviceContentSave) {
		ServiceContentSave[] saves = null;
		if (StringUtils.isNotEmpty(serviceContentSave)) {
			JSONArray sas = JSONArray.fromObject(serviceContentSave);
			if (!sas.isEmpty()) {
				saves = new ServiceContentSave[sas.size()];
				for (int i = 0; i < sas.size(); i++) {
					ServiceContentSave ww = (ServiceContentSave) JSONObject.toBean(JSONObject.fromObject(sas.get(i)),
							ServiceContentSave.class);
					saves[i] = ww;
				}
			}
		}
		try {
			serviceContentSchem.saveOrderContents(saves, orderId);
		} catch (Exception e) {
			log.error("saveOrderContents error:{}", e.getMessage(), e);
		}
	}

	private ComplaintInfo getCompInfo(JSONObject json) {
		if (json.has("complaintInfo")) {
			return (ComplaintInfo) JSONObject.toBean(json.getJSONObject("complaintInfo"), ComplaintInfo.class);
		} else {
			return new ComplaintInfo();
		}
	}

	@PostMapping(value = "/workflow/serviceOrderAsk/submitServiceOrderInstance")
	public String submitServiceOrderInstance(@RequestBody String models) {
		log.info("受理工单入口参数：{}", models);
		JSONObject culiqueJson = JSONObject.fromObject(models);
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(culiqueJson.optJSONObject("custInfo"),OrderCustomerInfo.class);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(culiqueJson.optJSONObject("servContent"),ServiceContent.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(culiqueJson.optJSONObject("orderAskInfo"),OrderAskInfo.class);
		Map otherInfo = (Map)JSONObject.toBean(culiqueJson.optJSONObject("otherInfo"),Map.class);
		ComplaintInfo compInfo = getCompInfo(culiqueJson);
		JSONArray arr = culiqueJson.optJSONArray("instance");
		ServContentInstance[] instance = null;
		if(StringUtils.isNotNull(arr)){
			instance = new ServContentInstance[arr.size()];
			for(int i=0;i<arr.size();i++) {
				ServContentInstance tmp = (ServContentInstance)JSONObject.toBean(JSONObject.fromObject(arr.get(i)),ServContentInstance.class);
				instance[i] = tmp;
			}
		}
		
		String result = "";
		try {
			result = serviceOrderAskImpl.submitServiceOrderInstance(instance, custInfo, servContent, orderAskInfo, otherInfo, compInfo);
		} catch (Exception e) {
			log.error("submitServiceOrderInstance 异常：{}", e.getMessage(), e);
		}

		if(!"".equals(result) && !"ORDERNOTFINISH".equals(result)){
			this.cliqueSuccessSubmit(culiqueJson, orderAskInfo, custInfo, result);
		}
		return result;
	}
	
	@SuppressWarnings("serial")
	private void cliqueSuccessSubmit(JSONObject culiqueJson, OrderAskInfo orderAskInfo, OrderCustomerInfo custInfo, String orderId) {
		try {
			//自动转派地市直派工单
			if(StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType()) {
				serviceOrderAskImpl.autoPdAsync(orderId);
			}
			
			if(culiqueJson.has("fileList")) {
				String fileListStr = culiqueJson.getString("fileList");
				List<FileRelatingInfo> fileList = new Gson().fromJson(fileListStr, new TypeToken<List<FileRelatingInfo>>(){}.getType());
				if(!fileList.isEmpty()) {
					FileRelatingInfo[] fileArray = new FileRelatingInfo[fileList.size()];
					for(int i=0; i<fileList.size(); i++) {
						FileRelatingInfo file = fileList.get(i);
						file.setOrderId(orderAskInfo.getServOrderId());
						file.setRegionId(String.valueOf(orderAskInfo.getRegionId()));
						fileArray[i] = file;
					}
					accessoriesImpl.insertFile(fileArray);
				}
			}
			
			//集团来单保存申诉信息
			serviceOrderAskImpl.saveComplaintInfo(culiqueJson, orderId);
			
			//申诉拆机销户匹配销售品
			trackServiceImpl.saveComplaintOfferInfo(true, culiqueJson, orderAskInfo, custInfo);
		} catch (Exception e) {
			log.error("submitServiceOrderInstance doSuccess 异常：{}", e.getMessage(), e);
		}
	}

	@PostMapping(value = "/workflow/serviceOrderAsk/submitServiceOrderInstanceWithLabel")
	public String submitServiceOrderInstanceWithLabel(@RequestBody Map<String, Object> model) {
		JSONObject models = JSONObject.fromObject(model);
		log.info("商机单受理入口参数: {}", models);
		
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(JSONObject.fromObject(models.get("custInfo").toString()),OrderCustomerInfo.class);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(JSONObject.fromObject(models.get("servContent").toString()),ServiceContent.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(JSONObject.fromObject(models.get("orderAskInfo").toString()),OrderAskInfo.class);
		Map otherInfo = (Map)JSONObject.toBean(JSONObject.fromObject(models.get("otherInfo").toString()),Map.class);
		YNSJOrder order = (YNSJOrder)JSONObject.toBean(JSONObject.fromObject(models.get("ynsjOrder").toString()),YNSJOrder.class);
		BuopSheetInfo info = (BuopSheetInfo)JSONObject.toBean(JSONObject.fromObject(models.get("businessInfo").toString()),BuopSheetInfo.class);
		
		try {
			String result = serviceOrderAskImpl.submitServiceOrderInstanceWithLabel(custInfo, servContent, orderAskInfo, otherInfo, order, info);
			log.info("result: {}", result);
			JSONObject resultObj = JSONObject.fromObject(result);
			String code = resultObj.optString("code");
			String msg = resultObj.optString("message");//失败消息
			String data = resultObj.optString("resultObj");//服务单号
			if("0000".equals(code)) {
				if(StringUtils.isNotEmpty(order.getOrderType())) {
					//异步调用商机同步接口
					workSheetFeign.sendBusinessOrder(data);
				}
				//商机意向工单打标
				this.trafficLabel(orderAskInfo);
				
				//保存一键录单信息
				String loginId = models.optString("loginId");
				trackServiceImpl.saveAcceptOrderInfo(loginId, orderAskInfo);
				return data;
			}
			return msg;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw, true));
			log.error("商机单受理出现异常，出现错误信息：{}", sw);
		}
		return "";
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/saveContentSaveSJ")
	public int saveContentSaveSJ(@RequestBody String json) {
		JSONObject obj=JSONObject.fromObject(json);
		String serviceOrderId=obj.getString("serviceOrderId");
		JSONArray arr = JSONArray.fromObject(obj.get("serviceContentSaveSJs").toString());
		ServiceContentSaveSJ[] serviceContentSaveSJs = new ServiceContentSaveSJ[arr.size()];
		for(int i=0;i<arr.size();i++) {
			ServiceContentSaveSJ tmp = (ServiceContentSaveSJ)JSONObject.toBean(JSONObject.fromObject(arr.get(i)),ServiceContentSaveSJ.class);
			serviceContentSaveSJs[i] = tmp;
		}
		return serviceOrderAskImpl.saveContentSaveSJ(serviceOrderId, serviceContentSaveSJs);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/removeContentSaveSJ")
	public int removeContentSaveSJ(@RequestParam(value="serviceOrderId", required=true) String serviceOrderId) {
		return serviceOrderAskImpl.removeContentSaveSJ(serviceOrderId);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/queryContentSaveSJ")
	public ServiceContentSaveSJ[] queryContentSaveSJ(@RequestParam(value="serviceOrderId", required=true) String serviceOrderId) {
		return serviceOrderAskImpl.queryContentSaveSJ(serviceOrderId);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/getServOrderInfo")
	public ServiceOrderInfo getServOrderInfo(@RequestParam(value="orderId", required=true) String orderId, @RequestParam(value="hisFlag", required=true)  boolean hisFlag) {
		return serviceOrderAskImpl.getServOrderInfo(orderId, hisFlag);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/deleteHoldOrder")
	//@Transactional
	public String deleteHoldOrder(@RequestParam(value="orderId", required=true) String orderId,
			@RequestParam(value="month", required=true)  Integer month) {
		boolean resultFlag = serviceOrderAskImpl.deleteHoldOrder(orderId, month);
		return (resultFlag ? ResultUtil.success() : ResultUtil.fail(ResultEnum.FALL));
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/getLogonStaff")
	public JSONObject  getLogonStaff() {
		TsmStaff staff = serviceOrderAskImpl.getLogonStaff();
		return JSONObject.fromObject(staff);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/getServOrderProdNumHis")
	public OrderAskInfo[] getServOrderProdNumHis(
			@RequestParam(value="prodNum", required=true)String prodNum,
			@RequestParam(value="regionId", required=false) String regionId) {
		String sql = "";
		if(StringUtils.isNotEmpty(regionId)) {
			sql="AND REGION_ID =" +regionId+" order by ACCEPT_DATE desc";
		}
		return serviceOrderAskImpl.getServOrderProdNumHis(prodNum, sql);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/gettsForceReleaseProdNumHis")
	public OrderAskInfo[] gettsForceReleaseProdNumHis(@RequestParam(value="prodNum", required=true)String prodNum,
			@RequestParam(value="sql", required=true) String sql) {

		return serviceOrderAskImpl.getServOrderProdNumHis(prodNum, sql);
	}
	
	@RequestMapping(value = "/workflow/dynamic/getServOrderProdNumHis")
	public Object getServOrderProdNumHis(@RequestBody(required = false) String parm) {
		return serviceOrderAskImpl.getServOrderProdNumHis(parm);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/getServiceOrderById")
	public ServiceOrderInfo getServiceOrderById(
			@RequestParam(value="serOrdId", required=true)String serOrdId,
			@RequestParam(value="custId", required=true)String custId,
			@RequestParam(value="monthFlg", required=true)Integer monthFlg,
			@RequestParam(value="regionId", required=true)String regionId,
			@RequestParam(value="status", required=true)int status, 
			@RequestParam(value="vesion", required=true)int vesion) {
		return serviceOrderAskImpl.getServiceOrderById(serOrdId, custId, monthFlg, regionId, status, vesion);
	}
	
	@SuppressWarnings("serial")
	@PostMapping(value = "/workflow/serviceOrderAsk/holdServiceOrderInstance")
	public String holdServiceOrderInstance(@RequestBody String models) {
		JSONObject json = JSONObject.fromObject(models);
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(JSONObject.fromObject(json.optString("custInfo")),OrderCustomerInfo.class);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(JSONObject.fromObject(json.optString("servContent")),ServiceContent.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(JSONObject.fromObject(json.optString("orderAskInfo")),OrderAskInfo.class);
		
		String result = serviceOrderAskImpl.holdServiceOrderInstance(custInfo, servContent, orderAskInfo);
		
		String code = JSONObject.fromObject(result).optString("code");
		if("0000".equals(code)){
			String fileListStr = json.getString("fileList");
			List<FileRelatingInfo> fileList= new Gson().fromJson(fileListStr, new TypeToken<List<FileRelatingInfo>>(){}.getType());
			if(!fileList.isEmpty()) {
				FileRelatingInfo[] fileArray = new FileRelatingInfo[fileList.size()];
				for(int i=0;i<fileList.size();i++) {
					FileRelatingInfo file = fileList.get(i);
					file.setOrderId(orderAskInfo.getServOrderId());
					file.setRegionId(String.valueOf(orderAskInfo.getRegionId()));
					fileArray[i]=file;
				}
				accessoriesImpl.insertFile(fileArray);
			}
		}
		return result;
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/reatAccepte")
	public boolean reatAccepte(@RequestBody String models) {
		JSONObject json=JSONObject.fromObject(models);
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(json.optJSONObject("custInfo"),OrderCustomerInfo.class);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(json.optJSONObject("servContent"),ServiceContent.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(json.optJSONObject("orderAskInfo"),OrderAskInfo.class);
		Map otherInfo = (Map)JSONObject.toBean(json.optJSONObject("otherInfo"),Map.class);
		
		JSONArray arr = JSONArray.fromObject(json.optJSONArray("instance"));
		ServContentInstance[] instance = new ServContentInstance[arr.size()];
		for(int i=0;i<arr.size();i++) {
			ServContentInstance tmp = (ServContentInstance)JSONObject.toBean(JSONObject.fromObject(arr.get(i)),ServContentInstance.class);
			instance[i] = tmp;
		}
		return serviceOrderAskImpl.reatAccepte(instance, custInfo, servContent, orderAskInfo, otherInfo);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/submitServiceOrderInstanceLabelNewJs")
	public String submitServiceOrderInstanceLabelNewJs(@RequestBody String modelsJson) {
		log.info("新受理工单入口参数: {}", modelsJson);
		JSONObject models = JSONObject.fromObject(modelsJson);
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(models.optJSONObject("orderCustInfo"),OrderCustomerInfo.class);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(models.optJSONObject("serviceContents"),ServiceContent.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(models.optJSONObject("orderAskInfos"),OrderAskInfo.class);
		
		//服务受理工单提交校验
		String acceptFlag = pubFunc.querySysContolFlag("SERVICE_ACCEPT__STAFF_ID_" + orderAskInfo.getAskStaffId());
		log.info("acceptFlag: {}", acceptFlag);
		if("1".equals(acceptFlag)){//工单受理中
			return ResultUtil.error("同一工号5秒内只能受理一张工单！");
		}
        pubFunc.updateSysContolFlag("SERVICE_ACCEPT__STAFF_ID_" + orderAskInfo.getAskStaffId(), "1", 5);
		
		Map otherInfo = (Map)JSONObject.toBean(models.optJSONObject("otherInfo"),Map.class);
		JSONArray lab = JSONArray.fromObject(models.optJSONArray("labelInstance"));
		LabelInstance[] ls = new LabelInstance[lab.size()];
		for(int i=0;i<lab.size();i++) {
			LabelInstance ss = (LabelInstance)JSONObject.toBean(JSONObject.fromObject(lab.get(i)),LabelInstance.class);
			ls[i] = ss;
		}
		String serviceContentSave = models.optString("serviceContentSave");
		ServiceContentSave[] saves = null;
		if(StringUtils.isNotEmpty(serviceContentSave)){
			JSONArray sas = JSONArray.fromObject(serviceContentSave);
			if(!sas.isEmpty()){
				saves = new ServiceContentSave[sas.size()];
				for(int i=0;i<sas.size();i++) {
					ServiceContentSave ww = (ServiceContentSave)JSONObject.toBean(JSONObject.fromObject(sas.get(i)),ServiceContentSave.class);
					saves[i] = ww;
				}
			}
		}
		ComplaintInfo compInfo = getCompInfo(models);
		String result = ResultUtil.fail(ResultEnum.FAIL_SUBMIT);
		try {
			result = serviceOrderAskImpl.submitServiceOrderInstanceLabelNew(custInfo, servContent, orderAskInfo, otherInfo, ls, saves, compInfo);
		} catch (Exception e) {
			log.error("submitServiceOrderInstanceLabelNewJs 异常：{}", e.getMessage(), e);
			result = ResultUtil.error("受理异常！");
		}
		String code = JSONObject.fromObject(result).optString("code");
		if("0000".equals(code)) {
			String orderId = JSONObject.fromObject(result).optString("resultObj");
			this.doSuccess(orderId, orderAskInfo, servContent, custInfo, models);
		}
		return result;
	}
	
	@SuppressWarnings("serial")
	private void doSuccess(String orderId, OrderAskInfo orderAskInfo, ServiceContent servContent, OrderCustomerInfo custInfo, JSONObject models) {
		try {
			//自动转派地市直派工单
			serviceOrderAskImpl.autoPdAsync(orderId);
			
			//最严场景的投诉单对接云脑系统
			trackServiceImpl.syncComplaintOrder(orderAskInfo, servContent, custInfo);
			
			//向智慧预警kafka发送数据
			if(StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType()) {
				workSheetFeign.sendServiceOrder(orderId);
			}
			
			String fileListStr = models.getString("fileList");
			List<FileRelatingInfo> fileList = new Gson().fromJson(fileListStr, new TypeToken<List<FileRelatingInfo>>(){}.getType());
			if(!fileList.isEmpty()) {
				FileRelatingInfo[] fileArray = new FileRelatingInfo[fileList.size()];
				for(int i=0; i<fileList.size(); i++) {
					FileRelatingInfo file = fileList.get(i);
					file.setOrderId(orderAskInfo.getServOrderId());
					file.setRegionId(String.valueOf(orderAskInfo.getRegionId()));
					fileArray[i] = file;
				}
				accessoriesImpl.insertFile(fileArray);
			}
			
			//服务受理保存申诉信息
			serviceOrderAskImpl.saveComplaintInfo(models, orderId);
			
			//小额退赔跟踪单
			this.submitTrackOrder(models, orderAskInfo.getServType(), orderId);
			
			//申诉拆机销户匹配销售品
			trackServiceImpl.saveComplaintOfferInfo(false, models, orderAskInfo, custInfo);
			
			//雅典娜智能分析
			trackServiceImpl.analysisOrder(orderAskInfo, servContent, custInfo);
			
			//保存一键录单信息
			String loginId = models.optString("loginId");
			trackServiceImpl.saveAcceptOrderInfo(loginId, orderAskInfo);
		} catch (Exception e) {
			log.error("submitServiceOrderInstanceLabelNewJs doSuccess 异常：{}", e.getMessage(), e);
		}
	}
	
	private void submitTrackOrder(JSONObject models, int servType, String orderId) {
		Map otherInfo = (Map)JSONObject.toBean(models.optJSONObject("otherInfo"), Map.class);
		if (otherInfo.containsKey("WORKSHEET_ALLOT") && otherInfo.get("WORKSHEET_ALLOT") != null) {
			try {
				String allot = otherInfo.get("WORKSHEET_ALLOT").toString();
				if (servType == 720200003 && "adjust_account".equals(allot)) {//查询单 全渠道 小额退赔 直接调账
					JSONObject track = new JSONObject();
					track.put("oldOrderId", orderId);
					track.put("trackType", "1");
					track.put("createType", 2);
					track.put("refundMode", 99);
					track.put("refundModeDesc", "无");
					
					JSONObject parm = new JSONObject();
					parm.put("isDispatch", "1");
					parm.put("sendToObjId", "363843");
					parm.put("track", track);
					//退费数据
					this.setRefundData(models, parm);
					String result = trackServiceImpl.createTrackServiceTZ(parm.toString());
					log.info("createTrackServiceTZ result: {}", result);
				}
			}
			catch(Exception e) {
				log.error("submitTrackOrder error: {}", e.getMessage(), e);
			}
		}
	}
	
	private void setRefundData(JSONObject models, JSONObject parm) {
		String refundData = "";
		String refundsAccNum = "";
		String refundAmount = "";
		String prmRefundAmount = "";
		try {
			JSONArray sas = models.optJSONArray("serviceContentSave");
			if(!sas.isEmpty()){
				for(int i=0;i<sas.size();i++) {
					ServiceContentSave ww = (ServiceContentSave)JSONObject.toBean(sas.getJSONObject(i), ServiceContentSave.class);
					if("c2f9995733b843c8393cc78629cd9220".equals(ww.getElementId())) {//退费数据
						//去掉特殊符号
						refundData = org.apache.commons.lang3.StringUtils.removeEnd(org.apache.commons.lang3.StringUtils.removeStart(ww.getAnswerName(), "【"), "】");
					}
					if("b6b2882c9e1811ee89ee005056b35a1f".equals(ww.getElementId())) {//承诺退费金额
						prmRefundAmount = ww.getAnswerName();
					}
				}
			}
			if(org.apache.commons.lang3.StringUtils.isNotBlank(refundData)) {
				JSONObject json = JSONObject.fromObject(refundData);
				refundsAccNum = json.optString("refundsAccNum");
				refundAmount = json.optString("cashAmount");
			}
		} catch (Exception e) {
			log.error("setRefundData error: {}", e.getMessage(), e);
		}
		log.info("refundData: {}", refundData);
		
		parm.put("refundData", refundData);
		parm.put("refundsAccNum", refundsAccNum);
		parm.put("refundAmount", refundAmount);
		parm.put("prmRefundAmount", prmRefundAmount);
	}
	
	/**
	 * 四类工单打标
	 * @param orderAskInfo
	 */
	private void trafficLabel(OrderAskInfo orderAskInfo) {
		log.info("四类工单打标");
		try {
			if((StaticData.SERV_TYPE_NEWTS == orderAskInfo.getServType() || StaticData.SERV_TYPE_CX == orderAskInfo.getServType() 
					|| StaticData.SERV_TYPE_SJ == orderAskInfo.getServType() || StaticData.SERV_TYPE_YS == orderAskInfo.getServType())
					&& orderAskInfo.getRelaType() > 0 && (!"N".equals(orderAskInfo.getCallSerialNo()))) {
				TsmStaff staff = pubFunc.getStaff(orderAskInfo.getAskStaffId());
				Map<String, String> codeMap = new HashMap<>();
				codeMap.put("CODE_ID", String.valueOf(orderAskInfo.getRelaType()));
				codeMap.put("CODE_SOURCE", "3");
				codeMap.put("MARK_TYPE", "2");
				List<Object> codeList = new ArrayList<>();
				codeList.add(codeMap);
				Map<String, Object> trafficData = new HashMap<>();
				trafficData.put("SERVICE_TYPE", orderAskInfo.getServType());
				trafficData.put("REGION_ID", orderAskInfo.getRegionId());
				trafficData.put("CALLER_NUM", orderAskInfo.getSourceNum());
				trafficData.put("DEST_NUM", "");
				trafficData.put("PROD_NUM", orderAskInfo.getProdNum());
				trafficData.put("CALL_ANSWER", orderAskInfo.getAskDate());
				trafficData.put("CALL_ID", orderAskInfo.getCallId());
				trafficData.put("FLOW_ID", orderAskInfo.getCallSerialNo());
				trafficData.put("STAFF_ID", staff.getLogonName());
				trafficData.put("codeList", codeList);
				
				String jsonString = JSON.toJSONString(trafficData);
				portalFeign.trafficLabelListLog(jsonString, orderAskInfo.getServOrderId());
			}
		} catch (Exception e) {
			log.error("trafficLabel 异常：{}", e.getMessage(), e);
		}
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/getLastCallSummary")
	public CallSummary getLastCallSummary(@RequestParam(value="orderId", required=true)String orderId) {
		return serviceOrderAskImpl.getLastCallSummary(orderId);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/saveCallSummaryUpdate")
	public int saveCallSummaryUpdate(@RequestParam(value="jsonStr", required=true)String jsonStr) {
		CallSummary summary = new Gson().fromJson(jsonStr, CallSummary.class);
		return serviceOrderAskImpl.saveCallSummaryUpdate(summary);
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/checkSjContent")
	public String checkSjContent(@RequestBody String ins) {
		JSONObject result = new JSONObject();
		String code = "999";
		String message = "请检查目录级别";
		JSONObject insJson = JSONObject.fromObject(ins);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(insJson.optJSONObject("servContent"), ServiceContent.class);
		int serviceDate = Integer.parseInt(insJson.getString("serviceDate"));
		String flag = serviceOrderAskImpl.checkSelect(servContent, serviceDate);
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(insJson.optJSONObject("custInfo"), OrderCustomerInfo.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(insJson.optJSONObject("orderAskInfo"), OrderAskInfo.class);
		if("SUCCESS".equals(flag)) {
			String orderObj = serviceOrderAskImpl.holdServiceOrderInstance(custInfo, servContent, orderAskInfo);
			JSONObject o = JSONObject.fromObject(orderObj);
			code = o.getString("code");
			message = o.getString("resultObj");
			if(!"0000".equals(code)) {
				message = o.getString("message");
			}
		}
		result.put("code", code);
		result.put("message", message);
		return result.toString();
	}
	
	@RequestMapping(value = "/workflow/serviceOrderAsk/submitZQDealContent")
	public int submitZQDealContent(@RequestBody(required=true) String ins) {
		JSONObject json=JSONObject.fromObject(ins);
		ZQCustInfoData data =(ZQCustInfoData)JSONObject.toBean(JSONObject.fromObject(json.optJSONObject("ins").toString()),ZQCustInfoData.class);
		return autoAcceptServiceImpl.submitDealContent(data);
	}

	/**
	 * 入库登记数据
	 */
	@PostMapping(value = "/workflow/serviceOrderAsk/createRegistration")
	public com.alibaba.fastjson.JSONObject createRegistration(@RequestBody String parm) {
		return registrationService.createRegistration(parm);
	}

	/**
	 * 获取登记数据
	 */
	@PostMapping(value = "/workflow/serviceOrderAsk/getHarassmentScene")
	public GridDataInfo getHarassmentScene(
			@RequestParam(value = "begion", required = true)int begion,
			@RequestParam(value = "pageSize", required = true)int pageSize,
			@RequestParam(value = "submissionTime", required = true) String submissionTime,
			@RequestParam(value = "downloadStatus", required = true) String downloadStatus) {
		return registrationService.getHarassmentScene(begion,pageSize,submissionTime,downloadStatus);
	}

	/**
	 * 更新下载状态
	 */
	@PostMapping(value = "/workflow/serviceOrderAsk/setDownloadTime")
	public int setDownloadTime(@RequestParam(value = "ids", required = true)String ids) {
		 return registrationService.setDownloadTime(ids);
	}

	/**
	 * 直接生成调账跟踪单
	 */
	@PostMapping(value = "/workflow/serviceOrderAsk/createTrackServiceTZ")
	public String createTrackServiceTZ(@RequestBody String parm) {
		return trackServiceImpl.createTrackServiceTZ(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/createTrackService")
	public Object createTrackService(@RequestBody(required=false) String parm) {
		return trackServiceImpl.createTrackService(parm);
	}

	// 智慧诊断受理
	@RequestMapping(value = "/workflow/serviceOrderAsk/saveZHOrderId")
	public String saveZHOrderId(@RequestBody Map<String, Object> map) {
		return zhzdService.saveZHOrderId(map.get("in").toString());
	}

	// 智慧诊断暂存
	@RequestMapping(value = "/workflow/serviceOrderAsk/saveZCOrderId")
	public String saveZCOrderId(@RequestBody Map<String, Object> map) {
		return zhzdService.saveZCOrderId(map.get("in").toString());
	}

	// ISTM回单
	@RequestMapping(value = "/workflow/serviceOrderAsk/executeForRevist")
	public String executeForRevist(@RequestBody Map<String, Object> map) {
		return itsmService.executeForRevist(map.get("in").toString());
	}

	@PostMapping(value = "/workflow/serviceOrderAsk/getPersonas")
	public String getPersonas(@RequestParam(value = "serviceOrderId", required = true) String serviceOrderId,
			@RequestParam(value = "regionId", required = true) String regionId,
			@RequestParam(value = "prodNum", required = true) String prodNum,
			@RequestParam(value = "curFlag", required = true) boolean curFlag) {
		return serviceOrderAskImpl.getPersonas(serviceOrderId, regionId, prodNum, curFlag);
	}
	
	// 综调查询
	@RequestMapping(value = "/workflow/serviceOrderAsk/executeXML")
	public String executeXML(@RequestBody Map<String, Object> map) {
		return zhddService.executeXML(map.get("in").toString());
	}
	
	@RequestMapping(value = "/workflow/dynamic/getServOrderInfo", produces="application/json;charset=utf-8")
	public Object getServOrderInfo(@RequestBody(required=false) String parm) {
		JSONObject json = JSONObject.fromObject(parm);
		String orderId = json.optString("orderId");
		
		JsonObject object = new JsonObject();
		ServiceWorkSheetInfo info = serviceOrderAskImpl.getServiceInfo(orderId, false);
		if(info != null) {
			String tmpInfo = new Gson().toJson(info, ServiceWorkSheetInfo.class);
			JsonObject tmpObject = new Gson().fromJson(tmpInfo, JsonObject.class);
			object.addProperty("hisFlag", false);
			object.add("info", tmpObject);
		}
		else {
			info = serviceOrderAskImpl.getServiceInfo(orderId, true);
			if(info != null) {
				String tmpInfo = new Gson().toJson(info, ServiceWorkSheetInfo.class);
				JsonObject tmpObject = new Gson().fromJson(tmpInfo, JsonObject.class);
				object.addProperty("hisFlag", true);
				object.add("info", tmpObject);
			}
			else {
				object.add("info", null);
			}
		}
		return new Gson().toJson(object);
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/workflow/dynamic/getComplaintInfo", produces="application/json;charset=utf-8")
	public Object getComplaintInfo(@RequestBody(required=false) String param) {
		JSONObject json = JSONObject.fromObject(param);
		String orderId = json.optString("orderId");
		String hisFlag = json.optString("hisFlag");
		Map info = serviceOrderAskImpl.getComplaintInfo(orderId, hisFlag);
		
		Map result = new HashMap<>();
		result.put("isExist", info.isEmpty() ? "0" : "1");
		result.put("info", info);
		return result;
	}
	
	@PostMapping(value = "/workflow/serviceOrderAsk/submitServiceOrderInstanceYS")
	public String submitServiceOrderInstanceYS(@RequestBody String models) {
		log.info("业务预受理工单入口参数: {}", models);
		JSONObject requestJson = JSONObject.fromObject(models);
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(requestJson.optJSONObject("custInfo"), OrderCustomerInfo.class);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(requestJson.optJSONObject("servContent"), ServiceContent.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(requestJson.optJSONObject("orderAskInfo"), OrderAskInfo.class);
		Map otherInfo = (Map)JSONObject.toBean(requestJson.optJSONObject("otherInfo"), Map.class);
		BuopSheetInfo info = (BuopSheetInfo) JSONObject.toBean(requestJson.optJSONObject("businessInfo"),BuopSheetInfo.class);
		
		String result = "";
		try {
			result = serviceOrderAskImpl.submitServiceOrderInstanceYS(custInfo, servContent, orderAskInfo, otherInfo, true, info);
		} catch (Exception e) {
			log.error("submitServiceOrderInstanceYS 异常：{}", e.getMessage(), e);
		}
		
		if("ORDERBACK".equals(result)) {//后台退回工单提交
			//提交并办结
			String autoFinish = requestJson.optString("autoFinish");
			if("1".equals(autoFinish)){
				this.autoFinish(orderAskInfo);
			}
			return orderAskInfo.getServOrderId();
		}
		if(!"".equals(result) && !"ORDERNOTFINISH".equals(result)){//成功受理
			//商机预受理工单打标
			this.trafficLabel(orderAskInfo);
			//领取万象券
			trackServiceImpl.getCouponByPhone(requestJson, result);
			//保存一键录单信息
			String loginId = requestJson.optString("loginId");
			trackServiceImpl.saveAcceptOrderInfo(loginId, orderAskInfo);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	@PostMapping(value = "/workflow/serviceOrderAsk/submitPreOrder")
	public String submitPreOrder(@RequestBody String models) {
		log.info("业务预受理中台工单入口参数: {}", models);
		JSONObject requestJson = JSONObject.fromObject(models);
		OrderCustomerInfo custInfo = (OrderCustomerInfo)JSONObject.toBean(requestJson.optJSONObject("custInfo"), OrderCustomerInfo.class);
		ServiceContent servContent = (ServiceContent)JSONObject.toBean(requestJson.optJSONObject("servContent"), ServiceContent.class);
		OrderAskInfo orderAskInfo = (OrderAskInfo)JSONObject.toBean(requestJson.optJSONObject("orderAskInfo"), OrderAskInfo.class);
		Map otherInfo = (Map)JSONObject.toBean(requestJson.optJSONObject("otherInfo"), Map.class);
		BuopSheetInfo info = (BuopSheetInfo) JSONObject.toBean(requestJson.optJSONObject("businessInfo"),BuopSheetInfo.class);
		
		//校验是否有在途单
		boolean checkFlag = serviceOrderAskImpl.checkOrderNotFinish(orderAskInfo);
		if(checkFlag) {
			log.info("该产品号码有在途工单: {}", orderAskInfo.getProdNum());
			return new CommonResult<>().setBody(0, "该产品号码有在途工单", null);
		}
		
		int resultCode = 0;
		String resultMsg = "";
		String result = "";
		try {
			JSONObject reqBody = new JSONObject();
			reqBody.put("preOrder", requestJson.optJSONObject("ynsjOrder"));
			reqBody.put("goodsList", requestJson.optJSONArray("goodsList"));
			String orderResult = interfaceFeign.sendPreOrder(reqBody.toString());
			log.info("sendPreOrder result: {}", orderResult);
			
			JSONObject resultJson = JSONObject.fromObject(orderResult);
			int code = resultJson.optInt("code");
			if(code == 1) {//受理成功
				otherInfo.put("ORDER_FLAG", "ZTYS");//中台预受理单流向配置部门
				
				String orderId = resultJson.optString("data");//订单号码
				result = serviceOrderAskImpl.submitServiceOrderInstanceYS(custInfo, servContent, orderAskInfo, otherInfo, false, info);
				log.info("submitServiceOrderInstanceYS result: {}", result);
				
				if(!"".equals(result)){
					log.info("预受理单受理成功");
					resultCode = 1;
					resultMsg = "受理成功";
					this.savePreOrderResult(orderAskInfo.getServOrderId(), orderId, requestJson.optJSONArray("goodsList"));
					
					if(!"ORDERBACK".equals(result)) {//后台退回工单提交
						//商机预受理工单打标
						this.trafficLabel(orderAskInfo);
						//领取万象券
						trackServiceImpl.getCouponByPhone(requestJson, result);
						//保存一键录单信息
						String loginId = requestJson.optString("loginId");
						trackServiceImpl.saveAcceptOrderInfo(loginId, orderAskInfo);
					}
				}
			}
			else {//受理失败
				resultMsg = resultJson.optString("msg");//响应消息
			}
		} catch (Exception e) {
			log.error("submitPreOrder 异常：{}", e.getMessage(), e);
		}
		resultMsg = org.apache.commons.lang3.StringUtils.defaultIfBlank(resultMsg, "受理失败");
		
		return new CommonResult<>().setBody(resultCode, resultMsg, result);
	}
	
	private void savePreOrderResult(String servOrderId, String orderId, JSONArray goodsList) {
		String goodsId = null;
		String goodsName = null;
		try {
			if(goodsList != null && !goodsList.isEmpty()) {
				JSONObject goods = goodsList.getJSONObject(0);
				goodsId = goods.optString("goodsId");
				goodsName = goods.optString("goodsName");
			}
		} catch (Exception e) {
			log.error("savePreOrderResult 异常：{}", e.getMessage(), e);
		}
		int num = orderAskInfoDao.savePreOrderResult(servOrderId, orderId, goodsId, goodsName);
		log.info("savePreOrderResult result: {}", num);
	}

	private void autoFinish(OrderAskInfo orderAskInfo) {
		String orderId = orderAskInfo.getServOrderId();
		int staffId = orderAskInfo.getAskStaffId();
		
		SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(orderId, StaticData.SHEET_TYPE_YS_ASSING, 0);
		sheetInfo.setDealContent("已联系确认，无需办理");
		sheetInfo.setRcvOrgId(orderAskInfo.getAskOrgId());
		
		OrderAskInfo orderInfo = new OrderAskInfo();
		orderInfo.setAskChannelId(0);
		orderInfo.setAskChannelDesc("");
		orderInfo.setServOrderId(orderId);
		orderInfo.setMonth(orderAskInfo.getMonth());
		orderInfo.setRelaInfo(orderAskInfo.getRelaInfo());
		
		BuopSheetInfo buopSheetInfo = new BuopSheetInfo();
		buopSheetInfo.setSatasfi("0");
		buopSheetInfo.setIsTrans("1");
		buopSheetInfo.setDealResult("办理失败");
		buopSheetInfo.setReplyRemark("");
		buopSheetInfo.setSheetStatus("1");
		buopSheetInfo.setFailResult("暂时不办");
		buopSheetInfo.setServiceOrderId(orderId);
		serviceOrderAskImpl.autoFinish(sheetInfo, orderInfo, buopSheetInfo, false, null, staffId);
	}
	
	@RequestMapping(value = "/workflow/dynamic/createRefundTrackOrder")
	public Object createRefundTrackOrder(@RequestBody(required=false) String parm) {
		return trackServiceImpl.createRefundTrackOrder(parm);
	}
	
	@RequestMapping(value = "/workflow/dynamic/qryRepeatOrderByProdNum")
	public Object qryRepeatOrderByProdNum(@RequestBody(required=false) String param) {
		return trackServiceImpl.qryRepeatOrderByProdNum(param);
	}
	
	@RequestMapping(value = "/workflow/dynamic/isExistRefundOrder")
	public Object isExistRefundOrder(@RequestBody(required=false) String param) {
		return trackServiceImpl.isExistRefundOrder(param);
	}
	
	@RequestMapping(value = "/workflow/dynamic/isSensitiveFlag")
	public Object isSensitiveFlag() {
		return ResultUtil.success(trackServiceImpl.isSensitiveFlag());
	}
	
	@RequestMapping(value = "/workflow/dynamic/createMultiRefundTrackOrder")
	public Object createMultiRefundTrackOrder(@RequestBody(required=false) String parm) {
		return trackServiceImpl.createMultiRefundTrackOrder(parm);
	}
}