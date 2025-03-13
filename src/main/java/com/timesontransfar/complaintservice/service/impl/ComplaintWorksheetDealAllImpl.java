package com.timesontransfar.complaintservice.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cliqueWorkSheetWebService.pojo.ComplaintInfo;
import com.cliqueWorkSheetWebService.pojo.ComplaintRelation;
import com.cliqueWorkSheetWebService.util.ReasultUtil;
import com.labelLib.pojo.LabelInstance;
import com.timesontransfar.async.WorkSheetToITSMAsync;
import com.timesontransfar.complaintservice.pojo.FinAssessInfo;
import com.timesontransfar.complaintservice.pojo.PreAssessInfo;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDeal;
import com.timesontransfar.complaintservice.service.IComplaintWorksheetDealAll;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.labelmanage.service.ILabelManageService;
import com.timesontransfar.customservice.orderask.dao.IorderAskInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.TScustomerVisit;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.feign.clique.AccessCliqueServiceFeign;
import com.timesontransfar.labelLib.service.ILabelService;
import com.transfar.common.web.ResultUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component(value="complaintWorksheetDealAll")
public class ComplaintWorksheetDealAllImpl implements IComplaintWorksheetDealAll{
	protected Logger log = LoggerFactory.getLogger(ComplaintWorksheetDealAllImpl.class);
	@Autowired
	private IComplaintWorksheetDeal complaintWorksheetDealImpl;
	@Autowired 
	private ILabelService labelServiceImpl;
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	@Autowired
	private WorkSheetToITSMAsync workSheetToITSMAsync;
	@Autowired
	private ILabelManageService labelManageService;
	@Autowired
    private ISheetPubInfoDao sheetPubInfoDaoImpl;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private AccessCliqueServiceFeign accessCliqueServiceFeign;
    @Autowired
	private IorderAskInfoDao orderAskInfoDao;
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED,rollbackFor=Exception.class)
	public String autoSheetFinish(String models) {
		JSONObject json=JSONObject.fromObject(models);
		
		int regionId=json.optInt("regionId");
		int month=json.optInt("month");
		String dealContent=json.optString("dealContent");
		int flag=json.optInt("flag");
		int valiFlag=json.optInt("valiFlag");
		int upgradeIncline=json.optInt("upgradeIncline");
		String contactStatus=json.optString("contactStatus");
		String requireUninvited=json.optString("requireUninvited");
		String unifiedCode=json.optString("unifiedCode");
		String uccJTSS=json.optString("uccJTSS");
		JSONObject receiptJson=new JSONObject();
		if(json.containsKey("receiptEval")) {
			receiptJson=json.getJSONObject("receiptEval");
		}
		TsSheetQualitative tsdealQualitative = (TsSheetQualitative)JSONObject.toBean(json.optJSONObject("tsSheetQualitative"),TsSheetQualitative.class);
		TScustomerVisit tscustomerVisit = (TScustomerVisit)JSONObject.toBean(json.optJSONObject("tscustomerVisit"),TScustomerVisit.class);
		//执行调用完成方法之前处理两个统一编码的方法,
		complaintWorksheetDealImpl.setUnifiedContact(tsdealQualitative.getOrderId(), tsdealQualitative.getSheetId(), contactStatus, "1");
		complaintWorksheetDealImpl.setUnifiedContact(tsdealQualitative.getOrderId(), tsdealQualitative.getSheetId(), requireUninvited, "2");
		complaintWorksheetDealImpl.setUnifiedRepeat(tsdealQualitative.getOrderId(), tsdealQualitative.getSheetId(), unifiedCode, "1");
		complaintWorksheetDealImpl.setUnifiedRepeat(tsdealQualitative.getOrderId(), tsdealQualitative.getSheetId(), uccJTSS, "2");
		//投诉申诉信息
		this.updateOtherComplaintInfo(json);
		
		FinAssessInfo finAssessInfo = new FinAssessInfo();
		finAssessInfo.setQualitative(tsdealQualitative);
		finAssessInfo.setCustomerVisit(tscustomerVisit);
		finAssessInfo.setRegionId(regionId);
		finAssessInfo.setMonth(month);
		finAssessInfo.setDealContent(dealContent);
		finAssessInfo.setLogonFlag(flag);
		finAssessInfo.setValiFlag(valiFlag);
		finAssessInfo.setUpgradeIncline(upgradeIncline);
		finAssessInfo.setContactStatus(contactStatus);
		finAssessInfo.setRequireUninvited(requireUninvited);
		finAssessInfo.setUnifiedCode(unifiedCode);
		finAssessInfo.setUccJTSS(uccJTSS);
		String result = complaintWorksheetDealImpl.submitFinAssess(finAssessInfo);
		
		boolean man = false;
		JSONObject osj = new JSONObject();
		if("SUCCESS".equals(result)){
			String batchFinish = getBatchFinish(json);
			if(receiptJson!=null && !receiptJson.isEmpty()) {
				//保存定性环节 工单处理质量评价
				complaintWorksheetDealImpl.saveReceiptEval(receiptJson);	
			}
			osj.put("code", "0000");
			osj.put("message", "提交成功！");
			//执行保存面板操作
			JSONArray labelList=json.optJSONArray("labelArr");
			if(null != labelList) {
				LabelInstance[] labelArray = new LabelInstance[labelList.size()];
				for (int i = 0; i < labelList.size(); i++) {
					labelArray[i] = (LabelInstance) JSONObject.toBean(labelList.optJSONObject(i),LabelInstance.class);
				}
				labelServiceImpl.saveLabelInstance(labelArray);
			}
			/**
			 * 后期在完善集团处理场景规则
			 */
			ComplaintRelation cmpRelaOper = pubFunc.queryListByOid(tsdealQualitative.getOrderId());
			if(cmpRelaOper != null) {
				if (cmpRelaOper.getAssignType() == 2) {
					accessCliqueServiceFeign.updateRelaStatu(cmpRelaOper.getRelaGuid(), 20, "省已处理");
					osj.put("message", "请尽快回复集团！");
					man=true;
				}else if(checkJTFinish(cmpRelaOper, batchFinish)){
					JSONObject info = new JSONObject();
					info.put("complaintWorksheetId", cmpRelaOper.getComplaintWorksheetId());
					info.put("serviceOrderId", tsdealQualitative.getOrderId());
					info.put("reason", "省内已处理，请集团归档");
					info.put("type", "FINISH");
					String[] res = accessCliqueServiceFeign.accessCliqueNew(info.toString());
					if("SUCCESS".equals(res[0])){
						osj.put("message", "已自动通知集团归档！");
					} else {
						String str=res[1];
						if("10".equals(str.substring(0,2))){
							str=str.substring(3);
							osj.put("message", str);
						} else {
							man=true;
						}
					}
				}
				//通知集团归档操作失败
				if(man){
					osj.put("code", "openJiTuan");
					osj.put("message","relaGuid");
				}
			} else {
				log.info("非集团单提交");
				osj.put("message","提交成功");
			}
			
			return osj.toString();
		}else {
			return ReasultUtil.toResult(result);
		}
	}

	private String getBatchFinish(JSONObject json) {
		String batchFinish = "";
		if (json.containsKey("batchFinish")) {
			batchFinish = json.optString("batchFinish");
		}
		return batchFinish;
	}

	private boolean checkJTFinish(ComplaintRelation cmpRelaOper, String batchFinish) {
		return cmpRelaOper.getAssignType() == 3 || "C003021".equals(cmpRelaOper.getAskSourceSrl())
				|| "1".equals(batchFinish) 
				//新增工信部立案、工信部预处理集团工单，省内处理后可自行归档集团
				|| "C003002".equals(cmpRelaOper.getAskSourceSrl()) || "C003004".equals(cmpRelaOper.getAskSourceSrl());
	}

	private void updateOtherComplaintInfo(JSONObject json) {
		if(json.containsKey("complaintInfo")) {//投诉申诉信息
			ComplaintInfo info = (ComplaintInfo)JSONObject.toBean(json.optJSONObject("complaintInfo"), ComplaintInfo.class);
			int num = orderAskInfoDao.updateOtherComplaintInfo(info);
			log.info("updateOtherComplaintInfo 更新结果: {}", (num > 0 ? "成功" : "失败"));
		}
	}

	/**
	 * Description: 后台派单-工单审核-重新派单<br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param workSheetObj
	 * @param acceptContent 处理内容
	 * @param dealType 处理类型 0为部门转派工单，1为后台派单 5 审核重新派单 3为审核退单，4为部门审批退单
	 * @return <br>
	 * @CreateDate 2020年6月9日 上午10:05:49 <br>
	 */
	@Override
	//@Transactional
	public String submitAuitSheetToDeal(String sheetInfo, String acceptContent, int dealType) {
		JSONObject json=JSONObject.fromObject(sheetInfo);
		JSONArray workSheetObjArr=json.optJSONArray("workSheetObj");
		SheetPubInfo[] workSheetObj=new SheetPubInfo[workSheetObjArr.size()];
		for (int i = 0; i < workSheetObj.length; i++) {
			workSheetObj[i]=(SheetPubInfo) JSONObject.toBean(workSheetObjArr.getJSONObject(i),SheetPubInfo.class);
		}
		String result= tsWorkSheetDeal.submitAuitSheetToDeal(workSheetObj, acceptContent, dealType);
		if("SUCCESS".equals(result)){
			JSONArray labelInstance=json.optJSONArray("labelInstance");
			LabelInstance[] instanceInfo=new LabelInstance[labelInstance.size()];
			for (int i = 0; i < labelInstance.size(); i++) {
				LabelInstance instance=(LabelInstance) JSONObject.toBean(labelInstance.getJSONObject(i));
				instanceInfo[i]=instance;
			}
			labelServiceImpl.saveLabelInstance(instanceInfo);
			String itsmFlag=json.optString("itsm_flag");
			if("1".equals(itsmFlag)){
				workSheetToITSMAsync.toITSMInfo(workSheetObj[0].getServiceOrderId());
			}
			
		}
		return ReasultUtil.toResult(result);
	}

	@Override
	@Transactional
	public String submitPreAssessYdx(String json) {
		// 补充方法
		JSONObject requestJson = JSONObject.fromObject(json);
		int regionId=requestJson.optInt("regionId");
		int month=requestJson.optInt("month");
		String dealContent=requestJson.optString("dealContent");
		int upgradeIncline=requestJson.optInt("upgradeIncline");
		String contactStatus=requestJson.optString("contactStatus");
		String requireUninvited = requestJson.optString("requireUninvited");
		String unifiedCode=requestJson.optString("unifiedCode");
		String uccJTSS=requestJson.optString("uccJTSS");
		
		//投诉申诉信息
		this.updateOtherComplaintInfo(requestJson);
				
		TsSheetQualitative bean = (TsSheetQualitative) JSONObject.toBean(requestJson.optJSONObject("tsSheetQualitative"), TsSheetQualitative.class);
		// 执行调用完成方法之前处理两个统一编码的方法
		complaintWorksheetDealImpl.setUnifiedContact(bean.getOrderId(), bean.getSheetId(), contactStatus, "1");
		complaintWorksheetDealImpl.setUnifiedContact(bean.getOrderId(), bean.getSheetId(), requireUninvited, "2");
		complaintWorksheetDealImpl.setUnifiedRepeat(bean.getOrderId(), bean.getSheetId(), unifiedCode, "1");
		complaintWorksheetDealImpl.setUnifiedRepeat(bean.getOrderId(), bean.getSheetId(), uccJTSS, "2");
		PreAssessInfo preInfo = new PreAssessInfo();
		preInfo.setBean(bean);
		preInfo.setRegionId(regionId);
		preInfo.setMonth(month);
		preInfo.setDealContent(dealContent);
		preInfo.setUpgradeIncline(upgradeIncline);
		preInfo.setContactStatus(contactStatus);
		preInfo.setRequireUninvited(requireUninvited);
		preInfo.setUnifiedCode(unifiedCode);
		preInfo.setUccJTSS(uccJTSS);
		String result = complaintWorksheetDealImpl.submitPreAssess(requestJson, preInfo);
		if ("SUCCESS".equals(result)) {
			JSONArray labelList = requestJson.optJSONArray("labelInstance");
			LabelInstance[] labelArray = new LabelInstance[labelList.size()];
			for (int i = 0; i < labelList.size(); i++) {
				labelArray[i] = (LabelInstance) JSONObject.toBean(labelList.optJSONObject(i),LabelInstance.class);
			}
			labelServiceImpl.saveLabelInstance(labelArray);
		}
		return ReasultUtil.toResult(result);
	}

	@Override
	//@Transactional
	public String sumbitOrgBackNew(String worksheetId, int regionId, int month, String backReason) {
		SheetPubInfo sheetPubInfo=sheetPubInfoDaoImpl.getSheetObj(worksheetId, regionId, month, true);
		int sendFlag=pubFunc.getSendCount(sheetPubInfo.getServiceOrderId(), sheetPubInfo.getRcvOrgId());
		if(sendFlag>0){
			return ResultUtil.error("该订单在本部门已经退单一次，请联系省投诉中心－林飞，电话号码为18951719608！");
		}
		return complaintWorksheetDealImpl.sumbitOrgBack(worksheetId, regionId, month, backReason);
	}

	@Override
	//@Transactional
	public String dispatchAssessNew(String models) {
		JSONObject obj=JSONObject.fromObject(models);
		String worksheetId=obj.optString("worksheetId");
		String orderId=obj.optString("orderId");
		String dealContent=obj.optString("dealContent");
		int dealLimit=obj.optInt("dealLimit");
		int upgradeIncline=obj.optInt("upgradeIncline");
		JSONArray main=obj.optJSONArray("main");
		String [] mainNew=new String[main.size()];
		for (int i = 0; i < main.size(); i++) {
			mainNew[i]=main.get(i).toString();
		}
		JSONArray sub=obj.optJSONArray("sub");
		String [] subNew=new String[sub.size()];
		for (int i = 0; i < sub.size(); i++) {
			subNew[i]=sub.get(i).toString();
		}
		String itsmFlag=obj.optString("itsm_flag");
		
		String result = "";
		try {
			result= complaintWorksheetDealImpl.dispatchAssess(worksheetId, mainNew, subNew, dealContent, dealLimit, upgradeIncline);
		} catch (Exception e) {
			JSONObject failObj = new JSONObject();
			failObj.put("code", "9999");
			failObj.put("message", "提交失败");
			return failObj.toString();
		}
		
		if("SUCCESS".equals(result)){//调用itsm 接口
			if("1".equals(itsmFlag)){
				workSheetToITSMAsync.toITSMInfo(orderId);
			}
			if(obj.optBoolean("modifyUnusualFlag")){
				labelManageService.modifyUnusualFlag(orderId);
			}
			JSONArray labelInstance = obj.optJSONArray("labelInstance");
			if(null != labelInstance) {
				LabelInstance[] labelArray = new LabelInstance[labelInstance.size()];
				for (int i = 0; i < labelInstance.size(); i++) {
					labelArray[i] = (LabelInstance) JSONObject.toBean(labelInstance.optJSONObject(i),LabelInstance.class);
				}
				labelServiceImpl.saveLabelInstance(labelArray);
			}
		}
		return ReasultUtil.toResult(result);
	}
}