package com.timesontransfar.sheetCase.service.ipml;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.dbgridData.CaseDao;
import com.timesontransfar.customservice.orderask.pojo.OrderAskInfo;
import com.timesontransfar.customservice.orderask.pojo.ServiceContent;
import com.timesontransfar.customservice.orderask.pojo.ServiceOrderInfo;
import com.timesontransfar.customservice.orderask.service.impl.ServiceOrderAskImpl;
import com.timesontransfar.customservice.worksheet.pojo.CaseDataEntity;
import com.timesontransfar.sheetCase.entity.CaseData;
import com.timesontransfar.sheetCase.entity.CaseEntity;
import com.timesontransfar.sheetCase.service.CaseService;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
@Component(value="CaseService")
public class CaseServiceImpl implements CaseService {

    private static final Logger log = LoggerFactory.getLogger(CaseServiceImpl.class);

    public static final String VALUE = "10-11";
    public static final String MESSAGE = "message";
    public static final String LEVEL = "level";
    @Resource
    public CaseDao caseDao;

    @Autowired
    public ServiceOrderAskImpl serviceOrderAsk;

    @Autowired
    public PubFunc pubFunc;

    /*
    新增案例
     */
    @Override
    public String saveCase(CaseEntity sheetCase) {
    	JSONObject jsonObject = new JSONObject();
        
        String orderId = sheetCase.getOrderId();  //服务单号
        int num = caseDao.getCaseNum(orderId);
        if (num > 0){
            jsonObject.put("code","0");
            jsonObject.put(MESSAGE,"该单号已存在案例！");
            return jsonObject.toString();
        }

        String creator = sheetCase.getCreator();  //提交人
        //查询对应的城市编码和部门名称
        Map<String, Object> cityDepartment = caseDao.getCreator(creator);

        String city = (String) cityDepartment.get("city");  //城市编码
        String orgName = (String) cityDepartment.get("orgName");  //部门名称

        String level = sheetCase.getLevel();  //案例级别:1：个人，2：地市，3省份
        if (level == null){
            level = "1";
        }
        if (city.contains(VALUE)){
            sheetCase.setEncode(VALUE);
        }else {
            sheetCase.setEncode(city);  //城市编码
        }

        if ("1".equals(level)){
            sheetCase.setStatus("1");  //案例状态1草稿
        } else {
            sheetCase.setStatus("2");  //案例状态2审批
        }

        sheetCase.setCreatorDepartment(orgName);  //提交员工部门名称
        sheetCase.setAuditStatus("0");  //案例审核状态 0:待审核 1：已审核

        String caseId = crtCaseId();

        int result = 0;
        try {
        	sheetCase.setCaseId(caseId);
        	result = caseDao.addCase(sheetCase);
        } catch (Exception e) {
        	jsonObject.put("code","0");
        	jsonObject.put(MESSAGE,"添加异常，请联系管理员！");
        	log.error("saveCase 异常：{}", e.getMessage(), e);
        	return jsonObject.toString();
        }

        if (result > 0){
		    jsonObject.put("code","1");
		    jsonObject.put(MESSAGE,caseId);
		}else {
			jsonObject.put("code","0");
		    jsonObject.put(MESSAGE,"添加失败！");
		}
        return jsonObject.toString();
    }

    public String crtCaseId (){
        String seqNum = pubFunc.getSeqVal("SEQ_CASE_ID", 8);
        if(seqNum == null) {
            return null;
        }
        return "AL"+seqNum;
    }

    @Override
    public List<Map<String, Object>> getCaseData(CaseEntity caseEntity) {
        List<Map<String, Object>> caseEntities = new ArrayList<>();
        try {

            caseEntities = caseDao.getCaseData(caseEntity);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return caseEntities;
    }

    @Override
    public List<Map<String, Object>> getCaseDataByAudit(CaseEntity caseEntity) {
        List<Map<String, Object>> caseEntities = new ArrayList<>();
        try {
            caseEntities = caseDao.getCaseDataByAudit(caseEntity);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return caseEntities;
    }



    @Override
    public CaseDataEntity getCase(CaseEntity caseEntity) {
        CaseDataEntity caseEntities = new CaseDataEntity();
        try {
            caseEntities = caseDao.getCase(caseEntity);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return caseEntities;
    }


    @Override
    public List<Map<String,Object>> searchCase(String caseId) {
        List<Map<String,Object>> caseEntities = new ArrayList<>();
        try {
            caseEntities = caseDao.searchCase(caseId);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return caseEntities;
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public List getCityCode() {
        List<Object> list = new ArrayList<>();
        try {
            list = caseDao.getCityCode();
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return list;
    }

    @Override
    public int getCaseCount(CaseEntity caseEntity) {
        int i = 0;
        try {
            i = caseDao.getCaseCount(caseEntity);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return i;
    }

    @Override
    public int getCaseByAuditCount(CaseEntity caseEntity) {
        int i = 0;
        try {
            i = caseDao.getCaseByAuditCount(caseEntity);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return i;
    }


    /*
    审核案例
     */
    @Override
    public String updateCase(JSONObject jsonObject) {
        int result = 0;
        JSONObject jsonResult = new JSONObject();
        String notPassCause = (String) jsonObject.get("notPassCause"); //不通过原因
        String caseId = (String) jsonObject.get("caseId");  //案例编号
        String caseDetail = (String) jsonObject.get("caseDetail");  //案例亮点/差错点
        String level = (String) jsonObject.get(LEVEL);  //案例级别:1：个人，2：地市，3省份
        String approver = (String) jsonObject.get("approver");//审核人
        String caseReview = (String) jsonObject.get("caseReview"); //审核是否通过

        Map<String, Object> creator = caseDao.getCreator(approver);
        String orgName = (String) creator.get("orgName"); //审核部门
        String city = (String) creator.get("city");
        Map<String, Object> lsMap = caseDao.getLevelStatus(caseId);
        String levelMysql = (String) lsMap.get(LEVEL);

        if (Integer.parseInt(level) == Integer.valueOf(levelMysql)){
            if (caseReview.equals("0")) {
                result = caseDao.updateCase("1", jsonObject, orgName, caseDetail, level, notPassCause, caseReview);
            } else {
                result = caseDao.updateCase("3", jsonObject, orgName, caseDetail, level, notPassCause, caseReview);
            }
        }else {
            if (city.contains(VALUE) && Integer.parseInt(level) > Integer.valueOf(levelMysql)) {
                if (caseReview.equals("0")) {
                    result = caseDao.updateCase("1", jsonObject, orgName, caseDetail, level, notPassCause, caseReview);
                } else {
                    result = caseDao.updateCase("3", jsonObject, orgName, caseDetail, level, notPassCause, caseReview);
                }
            } else {
                jsonResult.put("code", "0");
                jsonResult.put(MESSAGE, "案例级别不允许降级!!!");
                return jsonResult.toString();
            }
        }
        if (result != 0) {
            jsonResult.put("code", "1");
            jsonResult.put(MESSAGE, "操作成功");
        } else {
            jsonResult.put("code", "0");
            jsonResult.put(MESSAGE, "操作失败");
        }
        return jsonResult.toString();
    }

    /*
    我的案例
     */
    @Override
    public String updateSheetCase(JSONObject jsonObject) {
        JSONObject jsonResult = new JSONObject();
        int result = 0;
        String caseId = (String) jsonObject.get("caseId"); //案例编号
        String caseDetail = (String) jsonObject.get("caseDetail");  //案例亮点/差错点
        String level = (String) jsonObject.get(LEVEL);  //案例级别:1：个人，2：地市，3省份
        String status = "1";  //案例状态、
        if (!level.equals("1")){
            Map<String,Object> lsMap = caseDao.getLevelStatus(caseId);
            String levelMysql = (String) lsMap.get(LEVEL);
            String statusMysql = (String) lsMap.get("status");

            if (Integer.valueOf(level) >= Integer.valueOf(levelMysql)){
                if (statusMysql.equals("1")  || statusMysql.equals("3")){
                    status = "2";
                }else {
                    status = statusMysql;
                }
                 result = caseDao.updateSheetCase(level,caseId,caseDetail,status);
            } else {
                jsonResult.put("code","0");
                jsonResult.put(MESSAGE,"不能修改案例级别");
                return jsonResult.toString();
            }
        }else {
            result = caseDao.updateSheetCase(level,caseId,caseDetail,status);
        }
        if (result != 0){
            jsonResult.put("code","1");
            jsonResult.put(MESSAGE,"修改成功");
        }else {
            jsonResult.put("code","0");
            jsonResult.put(MESSAGE,"修改失败");
        }
        return jsonResult.toString();
    }

    /*
    删除案例
     */
    @Override
    public String deleteCase(String caseId) {

        int result = 0;

        try {
            result = caseDao.deleteCase(caseId);
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }

        JSONObject jsonResult = new JSONObject();
        if (result != 0){
            jsonResult.put("code","1");
            jsonResult.put(MESSAGE,"删除成功");
        }else {
            jsonResult.put("code","0");
            jsonResult.put(MESSAGE,"删除失败");
        }


        return jsonResult.toString();
    }

    @Override
    public String endCase(String caseId,String logonName) {
        int result = 0;
        JSONObject jsonResult = new JSONObject();

        try {
            if (caseDao.getLevel(caseId,logonName)){
                result = caseDao.endCase(caseId,logonName);
            }else {
                jsonResult.put("code","0");
                jsonResult.put(MESSAGE,"您无权停用该案例!");
                return jsonResult.toString();
            }
            if (result != 0){
                jsonResult.put("code","1");
                jsonResult.put(MESSAGE,"停用成功");
            }else {
                jsonResult.put("code","0");
                jsonResult.put(MESSAGE,"请确认案例是否已发布");
            }
        } catch (Exception exception) {
            jsonResult.put("code","0");
            jsonResult.put(MESSAGE,"停用失败");
            log.error(exception.getMessage());
        }
        return jsonResult.toString();
    }

    /*
       根据单号获取数据
        */
    @Override
    public CaseData caseData(String orderId) {
        boolean hisFlag = false;
        ServiceOrderInfo servOrderInfo = serviceOrderAsk.getServOrderInfo(orderId, false);
        if (servOrderInfo == null){
            hisFlag = true;
            servOrderInfo = serviceOrderAsk.getServOrderInfo(orderId, true);
        }

        CaseData caseData = new CaseData();
        caseData.setHisFlag(hisFlag);
        if (servOrderInfo != null){
            OrderAskInfo orderAskInfo = servOrderInfo.getOrderAskInfo();
            if (ObjectUtils.isNotEmpty(orderAskInfo)){
                String servOrderId = orderAskInfo.getServOrderId();
                caseData.setServOrderId(servOrderId);

                String prodNum = orderAskInfo.getProdNum();
                caseData.setProdNum(prodNum);

                String serviceTypeDesc = orderAskInfo.getServTypeDesc();
                caseData.setServiceTypeDesc(serviceTypeDesc);

                String comment = orderAskInfo.getComment();
                caseData.setComment(comment);
                caseData.setRegionName(orderAskInfo.getRegionName());
                
                ServiceContent servContent = servOrderInfo.getServContent();
                String acceptContent = servContent.getAcceptContent();
                caseData.setAcceptContent(acceptContent);
                
                String tsReasonName = caseDao.getTsReasonName(orderId, hisFlag);
                caseData.setTsReasonName(tsReasonName);
            }
        }
        return caseData;
    }

}
