package com.timesontransfar.customservice.tuschema.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dapd.pojo.DapdSheetInfo;
import com.timesontransfar.customservice.tuschema.dao.IserviceContentTypeDao;
import com.timesontransfar.customservice.tuschema.pojo.ServiceContentSave;
import com.timesontransfar.customservice.tuschema.pojo.ServiceOfferInfo;
import com.timesontransfar.customservice.tuschema.service.IserviceContentSchem;
import com.timesontransfar.dapd.service.IdapdSheetInfoService;

@Component
public class ServiceContentSchem implements IserviceContentSchem {
	
	protected Logger log = LoggerFactory.getLogger(ServiceContentSchem.class);
	
	@Autowired
    private IserviceContentTypeDao serviceContentType;
	@Autowired
	private IdapdSheetInfoService dapdSheetService;

    public int insertServiceContentSaveHis(String serviceOrderId) {
        int num = serviceContentType.insertContentSaveHis(serviceOrderId);
        num += serviceContentType.deleteContentSaveByOrderId(serviceOrderId);
		serviceContentType.insertServiceOfferInfoHis(serviceOrderId);
        return num;
    }
    
    public int insertDealContentSaveHis(String serviceOrderId) {
        int num = serviceContentType.insertDealContentSaveHis(serviceOrderId);
        num += serviceContentType.deleteDealContentSaveByOrderId(serviceOrderId);
        return num;
    }
    
	public ServiceContentSave[] filterRefundData(ServiceContentSave[] saves) {
		try {
			if(saves == null || saves.length == 0) {
				return saves;
			}
			List<ServiceContentSave> saveList = new ArrayList<>();
			for(int i=0;i<saves.length;i++) {
				ServiceContentSave s = saves[i];
				if(!"c2f9995733b843c8393cc78629cd9220".equals(s.getElementId())){//过滤退费数据
					saveList.add(s);
				}
			}
			ServiceContentSave[] array = new ServiceContentSave[saveList.size()];
			return saveList.toArray(array);
		} catch(Exception e) {
			log.error("filterRefundData error: {}", e.getMessage(), e);
		}
		return saves;
	}

    public String saveOrderContents(ServiceContentSave[] saves, String serviceId) {
    	if(StringUtils.isEmpty(serviceId))return "idIsNull";
    	//1: 根据服务单号删除老模板
    	serviceContentType.deleteContentSaveByOrderId(serviceId);
    	
    	//2: 保存新模板逻辑 
    	if (null != saves && saves.length > 0) {
    		JSONObject obj = new JSONObject();
    		List<ServiceContentSave> saveList = new ArrayList<>();
    		for(int i=0;i<saves.length;i++) {
    			saves[i].setServiceOrderId(serviceId);
    			saves[i].setIsCompare("9");
    			saves[i].setIsStat("0");
    			obj.put(saves[i].getAliasName(), saves[i].getAnswerName());
    			if(StringUtils.isNotBlank(saves[i].getComplaintsId()) && StringUtils.isNotBlank(saves[i].getElementId())) {
    				saveList.add(saves[i]);
    			}
    		}
    		if(!saveList.isEmpty()) {//过滤无效的模板元素
    			ServiceContentSave[] newSaves = saveList.toArray(new ServiceContentSave[saveList.size()]);
    			serviceContentType.insertContentSave(newSaves);
    		}
    		//这里还要加一个全模板元素字段的保存，因为元素还不全暂时不加
    		ServiceOfferInfo soi = JSON.parseObject(obj.toJSONString(), ServiceOfferInfo.class);
    		saveServiceOfferInfo(soi, serviceId);
			this.addRefundBusiness(saveList, serviceId);//为报表添加退费业务
        }
        return "SUCCESS";
    }

	private void addRefundBusiness(List<ServiceContentSave> saveList, String serviceId){
		log.info("addRefundBusiness param: {}", JSON.toJSONString(saveList));
		String refundBusinessId = "";
		try{
			boolean refundFlag = false;
			for (ServiceContentSave serviceContentSave : saveList) {
				if("69dd56faf686085b7349dbb0f7129a11".equals(serviceContentSave.getComplaintsId())){
					refundFlag = true;
					if("017df8368d3bcb35f8b8313f8e3d72e9".equals(serviceContentSave.getElementId())){
						refundBusinessId = serviceContentSave.getAnswerName();
					}
				}
			}
			if(refundFlag) {
				serviceContentType.upRefundBusiness(serviceId);
			}
			if(StringUtils.isNotBlank(refundBusinessId)){
				String[] split = refundBusinessId.split(",");
				for (int i = 0; i < split.length; i++) {
					serviceContentType.saveRefundBusiness(serviceId, split[i]);
				}
			}
		}catch (Exception e){
			log.error("addRefundBusiness error: {}",e.getMessage(),e);
		}
	}

	// 保存销售品信息
	private void saveServiceOfferInfo(ServiceOfferInfo soi, String serviceOrderId) {
		soi.setServiceOrderId(serviceOrderId);
		String offerNbr = StringUtils.defaultIfEmpty(soi.getOfferNbr(), null);
		String offerNm = StringUtils.defaultIfEmpty(soi.getOfferNm(), null);
		int count = serviceContentType.countServiceOfferInfo(serviceOrderId);
		if (0 == count) {// 首次提交
			if (offerNbr != null && offerNm != null) {// 销售品编码和名称不为空则新增
				serviceContentType.insertServiceOfferInfo(soi);
			}
		} else {// 非首次提交，则先清空
			serviceContentType.clearServiceOfferInfo(serviceOrderId);
			if (offerNbr != null && offerNm != null) {// 销售品编码和名称不为空则更新
				serviceContentType.updateServiceOfferInfo(soi);
			}
		}
	}

	public String saveDealContentSave(List<ServiceContentSave> saveList, String serviceId) {
		if (StringUtils.isEmpty(serviceId)) return "idIsNull";
		// 1: 根据服务单号删除旧结案模板
		serviceContentType.deleteDealContentSaveByOrderId(serviceId);

		// 2: 保存新结案模板
		if (null == saveList || saveList.isEmpty()) {
			return "Null";
		}
		
		JSONObject obj = new JSONObject();
		String complaintsId = "";
		String verificationInfoNew = "";//核查情况
		String processingResultNew = "";//处理结果
		String processingResultMore = "";//处理结果补充
		String approve = "";//是否认可处理方案
		for (int i = 0; i < saveList.size(); i++) {
			obj.put(saveList.get(i).getAliasName(), saveList.get(i).getAnswerName());
			saveList.get(i).setServiceOrderId(serviceId);
			complaintsId = saveList.get(i).getComplaintsId();
			if("7867b245dd4fdac63208fd34f9953f16".equals(saveList.get(i).getElementId())) {//核查情况元素ID
				verificationInfoNew = saveList.get(i).getAnswerName();
			}
			else if("2f7e74159964926a96b8ea1309e75ce1".equals(saveList.get(i).getElementId())) {//处理结果元素ID
				processingResultNew = saveList.get(i).getAnswerName();
			}
			else if("e55ab28a06a47b1a917a9376c6c5a8fd".equals(saveList.get(i).getElementId())) {//处理结果补充元素ID
				processingResultMore = saveList.get(i).getAnswerName();
			}else if("CB7892676F855F76E05392EAE0845CA7".equals(saveList.get(i).getElementId())){//是否认可处理方案元素ID
				approve = saveList.get(i).getAnswerName();
			}
		}
		if(StringUtils.isNotBlank(complaintsId)) {
			serviceContentType.insertDealContentSave(saveList);
			//更新雅典娜分析工单处理结果
			this.updateAnalysisInfo(serviceId, verificationInfoNew, processingResultNew);
			//更新大模型分析处理结果
			this.updateBigModel(serviceId,verificationInfoNew,processingResultNew,processingResultMore,approve);
		}
		
		this.setDealTemplate(obj, serviceId, complaintsId);
		return "SUCCESS";
	}
	
	private void updateAnalysisInfo(String serviceId, String verificationInfoNew, String processingResultNew) {
		log.info("updateAnalysisInfo: {}\nverificationInfoNew: {}\nprocessingResultNew: {}", serviceId, verificationInfoNew, processingResultNew);
		if(StringUtils.isNotEmpty(verificationInfoNew) || StringUtils.isNotEmpty(processingResultNew)) {
			serviceContentType.updateAnalysisInfo(serviceId, verificationInfoNew, processingResultNew);
		}
	}

	private void updateBigModel(String serviceId, String verificationInfoNew,String processingResultNew,String processingResultMore,String approve) {
		log.info("updateBigModel: {}\nverificationInfoNew: {}\nprocessingResultNew: {}\nprocessingResultMore: {}\napprove: {}", serviceId, verificationInfoNew, processingResultNew,processingResultMore,approve);
		try{
			if(StringUtils.isNotEmpty(verificationInfoNew) || StringUtils.isNotEmpty(processingResultNew) || StringUtils.isNotEmpty(processingResultMore) || StringUtils.isNotEmpty(approve)) {
				serviceContentType.updateBigModel(serviceId, verificationInfoNew, processingResultNew,processingResultMore,approve);
			}
		}catch (Exception e){
			log.error("updateBigModel error: {}",e.getMessage(),e);
		}
	}
	
	private void setDealTemplate(JSONObject obj, String serviceId, String complaintsId) {
		// 3: 根据模板元素别名，转换成工单字段
		DapdSheetInfo dapd = JSON.parseObject(obj.toJSONString(), DapdSheetInfo.class);
		dapd.setSheetIdProv(serviceId);
		//指定场景结案模板
		if (this.bestOrderFlag(complaintsId)) {//最严结案模板、集团服务监督热线
			String isSlv = dapd.getIsSlv();//解决情况
			this.setIsSlv(isSlv, dapd);
		} else if("f44f62645f554c68c874e3268c882889".equals(complaintsId)) {//集团网站
			String isSlv = dapd.getIsSlv();//用户问题是否解决
			dapd.setIsSlv(isSlv.replace("全部", "已").replace("部分", "已"));
		} else {//集团其他来源、部立案、申诉除立案外、其他模板
			String isSlv = dapd.getIsAprvl();//是否认可处理方案
			if(StringUtils.isNotBlank(isSlv)) {
				dapd.setIsSlv("不认可".equals(isSlv) ? "未解决" : "已解决");
			}
		}
		dapdSheetService.setDealTemplate(dapd);
	}
	
	private void setIsSlv(String isSlv, DapdSheetInfo dapd) {
		if(isSlv.startsWith("未解决-")) {
			dapd.setIsSlv("未解决");
			dapd.setUnrslvRsn(StringUtils.removeStart(isSlv, "未解决-"));
		} else {
			dapd.setIsSlv("已解决");
		}
	}
	
	private boolean bestOrderFlag(String complaintsId) {
		//最严结案模板
		if ("e004846616f69f1bcf4a542843b5805b".equals(complaintsId) || "498e2c764e582f5a1a0646eb37ac95f9".equals(complaintsId) || "b0154fccbc030fe3f606057c52c15186".equals(complaintsId)) {
			return true;
		}
		//集团服务监督热线
		if ("bc363be370c21c647128c418f461ab21".equals(complaintsId)) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void setRefundInfo(Map map, String orderId, boolean hisFlag) {
		ServiceContentSave[] saves = null;
		if(hisFlag) {
			saves = serviceContentType.selectContentSaveHis(orderId);
		} else {
			saves = serviceContentType.selectContentSave(orderId);
		}
		if(saves == null || saves.length == 0) {
			return;
		}
		Map template = new HashMap<>();
		for(int i=0; i<saves.length; i++) {
			ServiceContentSave save = saves[i];
			template.put(save.getElementId(), save.getAnswerName());
		}
		map.put("TEMPLATE_SAVE", template);
	}
}