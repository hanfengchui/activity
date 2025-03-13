/**
 * <p>类名：HastenSheetImpl.java</p>
 * <p>功能描叙：受理信息pojo</p>
 * <p>设计依据：TT-RD1-CRM10000号综合客户数据模型.pdm,及评估版180系统</p>
 * <p>开发者：万荣伟>
 * <p>开发/维护历史：</p>
 * <p>  Create by  万荣伟 2008-6-13</p> 
 */
package com.timesontransfar.customservice.worksheet.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.labelmanage.dao.ILabelManageDAO;
import com.timesontransfar.customservice.labelmanage.pojo.ServiceLabel;
import com.timesontransfar.customservice.worksheet.dao.IHastenSheetInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.pojo.HastenSheetInfo;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.service.IhastenSheet;
import com.timesontransfar.feign.custominterface.WorkSheetFeign;

import net.sf.json.JSONObject;

/**
 * @author 万荣伟
 *
 */
@Component("hastenSheetServ")
@SuppressWarnings("rawtypes")
public class HastenSheetImpl implements IhastenSheet {
	private static final Logger logger = LoggerFactory.getLogger(HastenSheetImpl.class);
	
	@Autowired
	private IHastenSheetInfoDao hastenSheetInfoDaoImpl;
	@Autowired
	private PubFunc pubFunc; 
	@Autowired
	private InoteSenList noteSen;
	@Autowired
    private ISheetPubInfoDao sheetPubInfoDao;
	@Autowired
    private ILabelManageDAO labelManageDAO;
	@Autowired
	@Qualifier("WorkSheetFeign")
	private WorkSheetFeign workSheetFeign;

	public static final String RELAPHONE = "RELAPHONE";

	/**
	 * 业务接口 根据工单号生成催单工单
	 * @param hasten
	 * @return
	 */	
	@Transactional
	public int sendHastenSheet(HastenSheetInfo hasten, boolean syncFlag) {
		//取得发起催单员工的组织机构和员工信息
		TsmStaff staff = this.pubFunc.getLogonStaff();
		int staffId = Integer.parseInt(staff.getId());
		String phone = staff.getRelaPhone();
		String info = "催单部门:"+staff.getOrgName()+"; 催单员工联系电话:"+phone+
		"\n催单内容："+hasten.getHastenInfo();
		hasten.setHastenGuid(this.pubFunc.crtGuid());
		hasten.setSendOrgId(staff.getOrganizationId());
		hasten.setSendOrgName(staff.getOrgName());
		hasten.setSendStaffId(staffId);
		hasten.setSendStaffName(staff.getName());
		hasten.setHastenInfo(info);
		if(StringUtils.isEmpty(hasten.getRegionName())){
			hasten.setRegionName(pubFunc.getRegionName(hasten.getRegionId()));
		}
		//催单发短信
        SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(hasten.getWorkSheetId(),hasten.getRegionId(),hasten.getMonth(), true);
        hasten.setValidFlag(0);
        int servType = sheetInfo.getServType();
        if(isSendHastenType(servType)){
        	// 投诉、咨询、建议、表扬、增值退订，判断是否有效催单
			int flag = hastenSheetInfoDaoImpl.checkIsValid(hasten.getOrderId());
			hasten.setValidFlag(flag);
			if(flag==1){
				ServiceLabel label = labelManageDAO.queryServiceLabelById(hasten.getOrderId(), false);
				if(null!=label){
					int newNum = label.getValidHastenNum()+1;
					labelManageDAO.updateValidHastenNum(hasten.getOrderId(), newNum);
				}
			}
		}
        
        String resultCode = "1";//成功
        if(syncFlag) {
        	String jsonResult = workSheetFeign.orderRemind(hasten.getOrderId(), staff.getName(), phone, hasten.getHastenInfo(), true);
        	JsonObject result = new Gson().fromJson(jsonResult, JsonObject.class);
        	resultCode = result.get("code") == null ? "" : result.get("code").getAsString();
        }
        else {
        	this.sendNoteCont(sheetInfo, sheetInfo.getLockFlag());
        }
        
        int count = 0;
        if("1".equals(resultCode)) {
			hastenSheetInfoDaoImpl.updateSheetHastentNum(hasten.getWorkSheetId(),hasten.getMonth());
        	count = hastenSheetInfoDaoImpl.saveHastenSheet(hasten);
        }
		return count;
	}

	@Override
	public int getHastenCondition(String orderId) {
		return hastenSheetInfoDaoImpl.getHastenCondition(orderId);
	}

	@Override
	public String getRefunded(String orderId) {
		return hastenSheetInfoDaoImpl.getRefunded(orderId);
	}

	// 2308新增查询
	private boolean isSendHastenType(int servType) {
		return StaticData.SERV_TYPE_NEWTS == servType || StaticData.SERV_TYPE_ZX == servType
				|| StaticData.SERV_TYPE_CX == servType || StaticData.SERV_TYPE_XT == servType
				|| StaticData.SERV_TYPE_GZ == servType;
	}

	/**
	 * 业务接口 坐席商机管理平台催单
	 * @return
	 */	
	@Transactional
	public String sendSyncHasten(String hastenJson) {
		logger.info("orderRemind：{}", hastenJson);
		JsonObject returnJson = new JsonObject();
		try {
			JSONObject json = JSONObject.fromObject(hastenJson);
			String logonName = json.getString("logonName");
			String orderId = json.getString("orderId");
			String hastenInfo = json.getString("hastenInfo");
			int hastenReasonId = json.getInt("hastenReasonId");
			String hastenReasonDesc = json.getString("hastenReasonDesc");
			
			//取得发起催单员工的组织机构和员工信息
			TsmStaff staff = this.pubFunc.getLogonStaffByLoginName(logonName);
			int staffId = Integer.parseInt(staff.getId());
			String phone = staff.getRelaPhone();
			String info = "催单部门:"+staff.getOrgName()+"; 催单员工联系电话:"+phone+
			"\n催单内容："+hastenInfo;
			
			SheetPubInfo sheetInfo = sheetPubInfoDao.getLatestSheetByType(orderId, StaticData.SHEET_TYPE_SJ_ASSING, 0);
			HastenSheetInfo hasten = new HastenSheetInfo();
			hasten.setHastenGuid(this.pubFunc.crtGuid());
			hasten.setOrderId(orderId);
			hasten.setWorkSheetId(sheetInfo.getWorkSheetId());
			hasten.setRegionId(sheetInfo.getRegionId());
			hasten.setRegionName(sheetInfo.getRegionName());
			hasten.setSendOrgId(staff.getOrganizationId());
			hasten.setSendOrgName(staff.getOrgName());
			hasten.setSendStaffId(staffId);
			hasten.setSendStaffName(staff.getName());
			hasten.setHastenReasonId(hastenReasonId);
			hasten.setHastenReasonDesc(hastenReasonDesc);
			hasten.setHastenInfo(info);
			hasten.setMonth(sheetInfo.getMonth());
			hasten.setValidFlag(0);

			String jsonResult = workSheetFeign.orderRemind(hasten.getOrderId(), staff.getName(), phone, hasten.getHastenInfo(), false);
        	JsonObject result = new Gson().fromJson(jsonResult, JsonObject.class);
        	String resultCode = result.get("code") == null ? "" : result.get("code").getAsString();
        	
	        if("1".equals(resultCode)) {
				hastenSheetInfoDaoImpl.updateSheetHastentNum(hasten.getWorkSheetId(),hasten.getMonth());
	        	int count = hastenSheetInfoDaoImpl.saveHastenSheet(hasten);
	        	if(count > 0) {
		        	return result.toString();
		        }
	        	else {
	        		returnJson.addProperty("code", "0");
	    			returnJson.addProperty("message", "更新催单记录失败");
	        	}
	        }
	        else {
	        	return result.toString();
	        }
		}
        catch(Exception e) {
        	logger.error("orderRemind 异常：{}", e.getMessage());
        	returnJson.addProperty("code", "-2");
			returnJson.addProperty("message", "坐席商机催单异常");
        }
		return returnJson.toString();
	}
	
	/**
	 * 提供催单集中查询
	 * @param strWhere 查询条件
	 * @param boo TRUE 为查询历史催单，false为查询在读催单
	 * @return HastenSheetInfo
	 */
	public HastenSheetInfo[] getListHasten(String strWhere,boolean boo) {
		return hastenSheetInfoDaoImpl.getListHastenInfo(strWhere,boo);
	}
	
	/**
	 * 根据服务单号,把当前催单信息移到历史表中并删除当前表的记录
	 * @param orderId 服务单号
	 * @return
	 */
	public int saveHastenSheetInfoHis(String orderId,Integer month){
		//根据服务单号查询
		hastenSheetInfoDaoImpl.savHastenSheetInfoHis(orderId,month);//保存到历史表中
		return hastenSheetInfoDaoImpl.delHastenSheet(orderId,month);//删除当前表信息
	}
	
	/**
	 * 发送催单信息
	 * @param bean 工单信息
	 * @param type 0-工单池；1、3-我的任务
	 * @return
	 */
	private String sendNoteCont(SheetPubInfo bean,int type) {
        //type 为0发送部门 为1发送个人
        NoteSeand noteBean = null;
        String phone = "";
        String client;
        String sheetGuid;
        List tmp = null;
        if(type==0) {
            tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(),null, bean.getTacheId(),0);
        }
        if(type==1 || type==3) {
            tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(),String.valueOf(bean.getDealStaffId()), bean.getTacheId(),1);
        }           
        Map map = null;
        if(tmp != null && !tmp.isEmpty()) {
            //取当前登录员工信息
            TsmStaff staff = this.pubFunc.getLogonStaff();
            int staffId = Integer.parseInt(staff.getId());
            String staffName = staff.getName();
            String orgId = staff.getOrganizationId();
            String orgName = staff.getOrgName();

            for(int i=0;i<tmp.size();i++) {
                map = (Map) tmp.get(i);
                noteBean = new NoteSeand();
                sheetGuid = this.pubFunc.crtGuid();
                phone = map.get("RELAPHONE").toString();
                client = map.get("CLIENT_TYPE").toString();
                noteBean.setSheetGuid(sheetGuid);
                noteBean.setRegionId(bean.getRegionId());
                noteBean.setDestteRmid(phone);
                noteBean.setClientType(Integer.parseInt(client));
                
                if(type==1 || type==3){
                    noteBean.setSendContent("您的岗位上有一张工单已经被客户第"+(bean.getHastentNum()+1)+"次催单，服务单号为："+bean.getServiceOrderId()+"，请尽快处理！");
                }else if(type==0){
                    noteBean.setSendContent("您的部门中有一张工单已经被客户第"+(bean.getHastentNum()+1)+"次催单，服务单号为："+bean.getServiceOrderId()+"，请尽快处理！");
                }                       
                noteBean.setOrgId(orgId);
                noteBean.setOrgName(orgName);
                noteBean.setStaffId(staffId);
                noteBean.setStaffName(staffName);
                noteBean.setBusiId(bean.getWorkSheetId());
                this.noteSen.saveNoteContent(noteBean);
            }
        }       
        return "";
    }

	/**
	 * 微信接口催单
	 * 
	 * @param hasten
	 * @return
	 */
	public int weiXinHastenSheet(String orderId, String ucc, String newRelaInfo, int allNum, boolean invalidFlag, boolean unchangeFlag, String whoWhere) {
		List list = sheetPubInfoDao.queryCurDealSheetByOrderId(orderId);
		if (list.isEmpty()) {
			return 0;
		}
		Map map = (Map) list.get(0);
		HastenSheetInfo hasten = new HastenSheetInfo();
		hasten.setOrderId(orderId);
		hasten.setWorkSheetId(map.get("WORK_SHEET_ID").toString());
		hasten.setRegionId(Integer.parseInt(map.get("REGION_ID").toString()));
		hasten.setRegionName(map.get("REGION_NAME").toString());
		if (unchangeFlag) {
			hasten.setHastenInfo(whoWhere + "催单！请尽快答复！");
		} else {
			hasten.setHastenInfo("联系电话:" + newRelaInfo + "\n" + whoWhere + "催单！请尽快答复！");
		}
		hasten.setHastenReasonId(600000091);
		hasten.setHastenReasonDesc("用户原因");
		hasten.setMonth(Integer.parseInt(map.get("MONTH_FLAG").toString()));
		hasten.setHastenGuid(this.pubFunc.crtGuid());
		hasten.setSendOrgId("");
		hasten.setSendOrgName("");
		hasten.setSendStaffId(0);
		hasten.setSendStaffName("手机用户");
		if (invalidFlag) {
			hasten.setValidFlag(0);
		} else {
			hasten.setValidFlag(1);
			ServiceLabel label = labelManageDAO.queryServiceLabelById(hasten.getOrderId(), false);
			if (null != label) {
				int newNum = label.getValidHastenNum() + 1;
				labelManageDAO.updateValidHastenNum(hasten.getOrderId(), newNum);
			}
		}
		hastenSheetInfoDaoImpl.updateSheetHastentNum(hasten.getWorkSheetId(), hasten.getMonth());
		int count = hastenSheetInfoDaoImpl.saveHastenSheet(hasten);
		// 催单发短信
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(hasten.getWorkSheetId(), hasten.getRegionId(), hasten.getMonth(), true);
		int type = sheetInfo.getLockFlag();
		String sendContent = "";
		if (type == 1 || type == 3) {
			sendContent = "您的岗位上有一张服务单被" + whoWhere + "第" + sheetInfo.getHastentNum() + "次催单，服务单号为：" + sheetInfo.getServiceOrderId() + "，请尽快处理！";
		} else if (type == 0) {
			sendContent = "您的部门中有一张服务单被" + whoWhere + "第" + sheetInfo.getHastentNum() + "次催单，服务单号为：" + sheetInfo.getServiceOrderId() + "，请尽快处理！";
		}
		this.sendNoteForWX(sheetInfo, type, sendContent);
		if (allNum > 0) {
			List firstList = sheetPubInfoDao.querytFirstDealSheetByOrderId(orderId);
			if (firstList.isEmpty()) {
				return count;
			}
			allNum = allNum + 1;
			sendContent = "集团编码为“" + ucc + "”工单，" + whoWhere + "催单" + allNum + "次，请尽快给予答复！";
			Map firstMap = (Map) firstList.get(0);
			sendNoteToFirstDeal(firstMap.get("RELAPHONE").toString(), sendContent, firstMap.get("SERVICE_ORDER_ID").toString());
		}
		return count;
	}

	/**
	 * 微信接口撤单
	 * 
	 * @param hasten
	 * @return
	 */
	public int weiXinCancelSheet(String orderId) {
		List list = sheetPubInfoDao.queryCurDealSheetByOrderId(orderId);
		if (list.isEmpty()) {
			return 0;
		}
		Map map = (Map) list.get(0);
		HastenSheetInfo hasten = new HastenSheetInfo();
		hasten.setOrderId(orderId);
		hasten.setWorkSheetId(map.get("WORK_SHEET_ID").toString());
		hasten.setRegionId(Integer.parseInt(map.get("REGION_ID").toString()));
		hasten.setRegionName(map.get("REGION_NAME").toString());
		hasten.setHastenInfo("用户电子渠道撤单！");
		hasten.setHastenReasonId(600000091);
		hasten.setHastenReasonDesc("用户原因");
		hasten.setMonth(Integer.parseInt(map.get("MONTH_FLAG").toString()));
		hasten.setHastenGuid(this.pubFunc.crtGuid());
		hasten.setSendOrgId("");
		hasten.setSendOrgName("");
		hasten.setSendStaffId(0);
		hasten.setSendStaffName("手机用户");
		hasten.setValidFlag(1);
		ServiceLabel label = labelManageDAO.queryServiceLabelById(hasten.getOrderId(), false);
		if (null != label) {
			int newNum = label.getValidHastenNum() + 1;
			labelManageDAO.updateValidHastenNum(hasten.getOrderId(), newNum);
		}
		hastenSheetInfoDaoImpl.updateSheetHastentNum(hasten.getWorkSheetId(), hasten.getMonth());
		int count = hastenSheetInfoDaoImpl.saveHastenSheet(hasten);
		SheetPubInfo sheetInfo = this.sheetPubInfoDao.getSheetObj(hasten.getWorkSheetId(), hasten.getRegionId(), hasten.getMonth(), true);
		int type = sheetInfo.getLockFlag();
		String sendContent = "";
		if (type == 1 || type == 3) {
			sendContent = "您的岗位上有一张服务单被用户电子渠道撤单，服务单号为：" + sheetInfo.getServiceOrderId() + "，请尽快处理！";
		} else if (type == 0) {
			sendContent = "您的部门中有一张服务单被用户电子渠道撤单，服务单号为：" + sheetInfo.getServiceOrderId() + "，请尽快处理！";
		}
		this.sendNoteForWX(sheetInfo, sheetInfo.getLockFlag(), sendContent);
		return count;
	}

	private String sendNoteForWX(SheetPubInfo bean, int type, String sendContent) {
		NoteSeand noteBean = null;
		String phone = "";
		String client;
		String sheetGuid;
		List tmp = null;
		if (type == 0) {
			tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(), null, bean.getTacheId(), 0);
		}
		if (type == 1 || type == 3) {
			tmp = this.noteSen.getNoteSendNum(bean.getRcvOrgId(), String.valueOf(bean.getDealStaffId()), bean.getTacheId(), 1);
		}
		Map map = null;
		if (tmp != null && !tmp.isEmpty()) {
			for (int i = 0; i < tmp.size(); i++) {
				map = (Map) tmp.get(i);
				noteBean = new NoteSeand();
				sheetGuid = this.pubFunc.crtGuid();
				phone = map.get("RELAPHONE").toString();
				client = map.get("CLIENT_TYPE").toString();
				noteBean.setSheetGuid(sheetGuid);
				noteBean.setRegionId(bean.getRegionId());
				noteBean.setDestteRmid(phone);
				noteBean.setClientType(Integer.parseInt(client));
				noteBean.setSendContent(sendContent);
				noteBean.setBusiId(bean.getWorkSheetId());
				this.noteSen.saveNoteContent(noteBean);
			}
		}
		return "";
	}

	private void sendNoteToFirstDeal(String soureNum, String sendContent, String serviceId) {
		if ("".equals(soureNum)) {
			soureNum = " ";
		}
		String checkNum = soureNum;
		if (!"0".equals(soureNum.substring(0, 1))) {
			checkNum = "0" + soureNum;
		}
		Map map = this.noteSen.getNumInfo(checkNum);
		String numType = map.get("out_numtype").toString();// 1本省电信固话;2本省电信手机;4外省电信移动手机;6外省或其它
		if (!numType.equals("1")) {
			NoteSeand noteBean = new NoteSeand();
			noteBean.setSheetGuid(this.pubFunc.crtGuid());
			noteBean.setRegionId(Integer.parseInt(map.get("out_regionid").toString()));
			noteBean.setDestteRmid(soureNum);
			if ("2".equals(numType) || "4".equals(numType)) {
				noteBean.setClientType(1);
			} else if ("3".equals(numType)) {
				noteBean.setClientType(0);// 终端标志 3本省电信小灵通
			} else if (checkNum.length() == 12) {
				noteBean.setClientType(1);
			} else {
				return;
			}
			noteBean.setSendContent(sendContent);
			noteBean.setBusiId(serviceId);
			this.noteSen.saveNoteContent(noteBean);
		}
	}

	@Override
	public HastenSheetInfo[] qryHastenList(String serId, String flag) {
		String where =" AND CC_WORK_SHEET.SERVICE_ORDER_ID = '" + serId + "'";
		Boolean b=Boolean.valueOf(flag);
		if(Boolean.TRUE.equals(b)) {
			where="  AND CC_HASTEN_SHEET_HIS.SERVICE_ORDER_ID = '" + serId + "'";
		}
		return getListHasten(where,b);
	}
	
}
