package com.timesontransfar.complaintservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.complaintservice.service.ICliqueService;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.worksheet.dao.ISheetPubInfoDao;
import com.timesontransfar.customservice.worksheet.dao.ItsWorkSheetDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetPubInfo;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetStatuApplyInfo;

@Component(value="cliqueService")
public class CliqueServiceImpl implements ICliqueService{
	
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private ItsWorkSheetDao tsWorkSheetDaoImpl;
	@Autowired
	private ISheetPubInfoDao sheetPubInfoDao;

	@Override
	public int applyAuto(String applyGuid, String sheetId, int auditStatu, String auditReason) {
		TsmStaff staff = pubFunc.getLogonStaff();
		WorkSheetStatuApplyInfo info = new WorkSheetStatuApplyInfo();
        info.setApplyGuid(applyGuid);
        info.setAudResult(auditReason);
        info.setApplyStatu(auditStatu);
        info.setAudStaff(Integer.parseInt(staff.getId()));
        info.setAudStaffName(staff.getName());
        info.setAudOrg(staff.getOrganizationId());
        info.setAudOrgName(staff.getOrgName());
        tsWorkSheetDaoImpl.updateSheetApply(info);

        SheetPubInfo sheetPubInfo = sheetPubInfoDao.getSheetPubInfo(sheetId, false);
        int sheetStatu = pubFunc.getSheetStatu(sheetPubInfo.getTacheId(), 1,sheetPubInfo.getSheetType());
        String stateDesc = pubFunc.getStaticName(sheetStatu);
        sheetPubInfoDao.updateSheetState(sheetId, sheetStatu, stateDesc, sheetPubInfo.getMonth(), 1);
		return 1;
	}

}
