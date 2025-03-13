/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.SheetTodispatch;
import com.timesontransfar.customservice.worksheet.service.ItsSheetDealAction;
import com.timesontransfar.customservice.worksheet.service.ItsSheetSumbit;
import com.timesontransfar.customservice.worksheet.service.ItsWorkSheetDeal;
import com.timesontransfar.customservice.worksheet.service.IworkSheetBusi;

/**
 * @author 万荣伟
 *
 */
@Component("tsSheetSumbitImpl__FACADE__")
public class TsSheetSumbitImpl implements ItsSheetSumbit {
	protected Logger log = LoggerFactory.getLogger(TsSheetSumbitImpl.class);
	
	@Autowired
	private ItsWorkSheetDeal tsWorkSheetDeal;
	@Autowired
	private ItsSheetDealAction tsSheetDealService;
	@Autowired
	private IworkSheetBusi workSheetBusi;
	@Autowired
	private PubFunc pubFunc;

	/**
	 * 高权限的员工批量分派工单到
	 * @param sheetId 格式为 sheetId@regionId@month sheetId:工单号,regionId地域;month月分区
	 * @param staffId 员工ID
	 * @type 处理类型 0为分派 1为释放
	 * @return
	 */
	public String allotBatchSheet(String[] sheetId,int staffId,int type) {
		return this.tsSheetDealService.allotBatchSheet(sheetId, staffId, type);
	}

	@Override
	public String audSheetFinishAuto(String orderId, String logonName) {
		log.info("audSheetFinishAuto  ======> \n{}", orderId);
		String result = "0";//自动完结服务单成功
		if(null == orderId || orderId.trim().length() == 0){
			return "-1";//未提供工单编号
		}
		/*获取审核单*/
		SheetPubInfo audSheet = tsWorkSheetDeal.getAudsheet(orderId);
		log.info("audSheet  ======> \n{}", audSheet);
		if(null == audSheet){
			result = "-1";//不满足自动完结条件，请继续手动完结服务单
		}else{
			boolean flag = true;
			/*提取审核单*/
			if(audSheet.getSheetStatu() == StaticData.WKST_AUD_STATE){
				String workSheetId = audSheet.getWorkSheetId();
				if (!"0".equals(logonName)) {
					workSheetId = workSheetId + "@" + logonName;
					audSheet.setDealContent("不成立");
				} else {
					audSheet.setDealContent("已处理");
				}
				String rst = workSheetBusi.fetchWorkSheet(workSheetId, audSheet.getRegionId(), audSheet.getMonth());
				log.info("fetchWorkSheet  ======> \n{}", rst);
				flag = "SUCCESS".equals(rst);
			}
			if(flag){
				/*完结审核单*/
				tsWorkSheetDeal.finishAudSheetAuto(audSheet);
			}else{
				result = "-1";//自动完结服务单失败，请继续手动完结服务单
			}
			
		}
		return result;
	}
	
	public String saveDispReason(SheetTodispatch todispatch){
		int i = workSheetBusi.saveSheetTodispatch(todispatch);
		if(i==0){
			return "ERROR";
		}
		return "SUCCESS";
	}

	public void autoDispatchSheet(String orderId, int receiveStaff, String receiveOrg, int autoPdStaff) {
		SheetPubInfo pdSheet = tsWorkSheetDeal.getTheLastSheetInfo(orderId);
		if (null != pdSheet) {
			SheetPubInfo clSheet = new SheetPubInfo();
			clSheet.setDealContent("请核实处理并回复用户。");
			clSheet.setDealLimitTime(pdSheet.getDealLimitTime());
			clSheet.setDealRequire("请核实处理并回复用户。");
			clSheet.setMainType(1);
			clSheet.setMonth(pdSheet.getMonth());
			if (receiveStaff != 0) {
				TsmStaff clStaff = pubFunc.getStaff(receiveStaff);
				clSheet.setRcvOrgId("STFFID");
				clSheet.setRcvOrgName("");
				clSheet.setRcvStaffId(receiveStaff);
				clSheet.setRcvStaffName(clStaff.getName() + "(" + clStaff.getLogonName() + ")");
			} else {
				clSheet.setRcvOrgId(receiveOrg);
				clSheet.setRcvOrgName(pubFunc.getOrgName(receiveOrg));
			}
			clSheet.setRegionId(pdSheet.getRegionId());
			clSheet.setServiceOrderId(orderId);
			clSheet.setStationLimit(pdSheet.getStationLimit());
			clSheet.setWorkSheetId(pdSheet.getWorkSheetId());
			clSheet.setDealStaffId(autoPdStaff);
			clSheet.setDealStaffName("AUTOPD");
			SheetPubInfo[] sheets = new SheetPubInfo[1];
			sheets[0] = clSheet;
			tsWorkSheetDeal.dispatchSheet(sheets);
		}
	}
}