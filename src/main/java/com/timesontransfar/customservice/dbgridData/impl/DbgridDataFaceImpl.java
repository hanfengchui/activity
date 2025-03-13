/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.dbgridData.impl;

import com.timesontransfar.labelLib.service.ILabelService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IDepartmentCount;
import com.timesontransfar.customservice.dbgridData.IdbGridStarStaff;
import com.timesontransfar.customservice.dbgridData.IdbgridCmpWorksheet;
import com.timesontransfar.customservice.dbgridData.IdbgridDataFace;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;
import com.timesontransfar.customservice.dbgridData.IdbgridDataTs;
import com.timesontransfar.customservice.dbgridData.IdbgridDataYn;
import com.timesontransfar.customservice.dbgridData.IsheetCheckData;

import java.util.List;
import java.util.Map;

@Component(value="dbgridDataFaceImpl")
public class DbgridDataFaceImpl implements IdbgridDataFace {
	@Autowired
	private IdbgridDataYn dbgridDataYn;//疑难列表
	
	@Autowired
	private IdbgridDataTs dbgridDataTs;//投诉列表
	
	@Autowired
	private IdbgridDataPub dbgridDataPub;//Grid公共类
	
	@Autowired
	private IsheetCheckData sheetCheckData;//工单质检查询列表

	@Autowired
	private IdbGridStarStaff dbGridStarStaff ;// 星级客户查询列表对象
	
	@Autowired
	private IDepartmentCount departmentCount;//工单监控
	
	@Autowired
	private IdbgridCmpWorksheet cmpWorksheetImpl;
	
	@Autowired
	private ILabelService labelServiceImpl;
	
	/**
	 * 得到列表数据
	 * @param begion 开始点
	 * @param strWhere 
	 * @param funId
	 * @return
	 */
	
	public GridDataInfo getGridDataBySize(int begion,int pageSize,String strWhere,String funId) {
		if(funId.equals(DbgridStatic.GRID_FUNID_TS_SHEET)) {
            return this.dbgridDataTs.getWaitDealSheetTs(begion, pageSize, strWhere);//投诉工单工单池
        }
        if(funId.equals(DbgridStatic.GRID_FUNID_TS_MYSHEET)) {
            return this.dbgridDataTs.getDealingSheetTs(begion, pageSize, strWhere);//投诉工单工单我的任务
        }
		return null;
	}

	public GridDataInfo getBatchGridDataBySize(int begion,int pageSize,String strWhere,String funId) {
		if(funId.equals(DbgridStatic.GRID_FUNID_TS_SHEET)) {
			return this.dbgridDataTs.getBatchWaitDealSheetTs(begion, pageSize, strWhere);//人工批量分派工单池
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_TS_MYSHEET)) {
			return this.dbgridDataTs.getBatchDealingSheetTs(begion, pageSize, strWhere);//人工批量分派我的任务
		}
		return null;
	}


	@Override
	public GridDataInfo queryValue(String funId, String strWhere, int begion, int pageSize) {
		return this.dbgridDataTs.queryValue(begion, pageSize, strWhere);//查询redis数据
	}


	@Override
	public int deleteKey(List<String> redisKeys) {
		return this.dbgridDataTs.deleteKey(redisKeys);//查询redis数据
	}


	@Override
	public int addRow(Map<String, String> newRowData) {
		return this.dbgridDataTs.addRow(newRowData);//新增数据
	}

	@Override
	public int getApportion(String staffId) {
		return this.dbgridDataTs.getApportion(staffId);
	}

	@Override
	public GridDataInfo getApportionData(String staffId,String orgId,String status,String appStatus,int begin,int pageSize) {
		return this.dbgridDataTs.getApportionData(staffId,orgId,status,appStatus,begin,pageSize);
	}

	@Override
	public int saveApportion(String staffId, String staffName, String orgId, String orgName, String apportionNumber) {
		return this.dbgridDataTs.saveApportion(staffId,staffName,orgId,orgName,apportionNumber);
	}

	@Override
	public String updateApportion(String param) {
		return this.dbgridDataTs.updateApportion(param);
	}

	public GridDataInfo getGridData(int begion,String strWhere,String funId) {
		if(funId.equals(DbgridStatic.GRID_FUNID_SJGRID)) {
			return this.dbgridDataTs.getSheetPoolSJ(begion, strWhere);//预受理商机处理工单池列表
		}
		
		
		if(funId.equals(DbgridStatic.GRID_FUNID_TS_ALREADYSHEET)) {
			return this.dbgridDataTs.getAlreadySendSheetTs(begion, strWhere);//投诉工单已派发列表
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_MNT_ORDERINFO)){
			return this.dbgridDataTs.getMonitorOrderInfo(begion, strWhere);//投诉工单已派发列表
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_TS_RELEASESHEET)) {
			return this.dbgridDataTs.getForceReleaseTs(begion, strWhere);//投诉强制释放列表
		}
	    if(funId.equals(DbgridStatic.GRID_FUNID_LABLE_CANCEL)) {
	        return labelServiceImpl.lableCancel(begion, strWhere);//标签取消列表
	    }
	    if(funId.equals(DbgridStatic.GRID_FUNID_LABLE_TEMPLATE)) {
	        return labelServiceImpl.lableRightGrid(begion, strWhere);//标签补打右边列表
	    }
		if(funId.equals(DbgridStatic.GRID_FUNID_CMP_CWORKSHEET)){
		    // 集团工单列表
		    return this.cmpWorksheetImpl.getCmpWorksheet(begion, strWhere);
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_CMP_PWORKSHEET)){
            // 集团工单列表
            return this.cmpWorksheetImpl.getWorksheet(begion, strWhere);
        }
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEET_MONITOR)) {
			return this.departmentCount.getMonitorExport(begion, strWhere);//工单监控部门
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEET_SATISFY)) {
			return this.departmentCount.getSatisfyExport(begion, strWhere);//满意度工单监控部门
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEET_MONITOR_STAFF)) {
			return this.departmentCount.getMonitorExportStaff(begion, strWhere);//工单监控员工
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEET_SATISFY_STAFF)) {
			return this.departmentCount.getSatisfyExportStaff(begion, strWhere);//满意度工单监控员工
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_YN_SHEET)) {
			return this.dbgridDataYn.getWaitDealSheetYn(begion, strWhere);//疑难工单工单池
		}	
		if(funId.equals(DbgridStatic.GRID_FUNID_YN_MYSHEET)) {
			return this.dbgridDataYn.getDealingSheetYn(begion, strWhere);//疑难工单工单我的任务
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_YN_ALREADYSHEET)) {
			return this.dbgridDataYn.getAlreadySendSheetYn(begion, strWhere);//疑难工单已派发列表
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK)) {
			return this.sheetCheckData.getSheetCheck(begion, strWhere);//工单质检查询列表
		}		
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEETCHECK_ONROAD)) {
			return this.sheetCheckData.queryOnRoadSheets(begion, strWhere);//工单质检查询列表--在途工单
		}		
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_FINISH)) {
			return this.sheetCheckData.queryFinishSheets(begion, strWhere);//工单质检查询列表--竣工工单
		}		
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_APPEALLIST)) {
			return this.sheetCheckData.queryAppealCheckSheets(begion, strWhere);//获取质检列表-申诉列表
		}		
		if(funId.equals(DbgridStatic.GRID_FUNID_QUERY_SHEET_CHECK_UPDATES)) {
			return this.sheetCheckData.queryUpdateCheckSheets(begion, strWhere);//获取质检列表-修改列表
		}		
		
		if(funId.equals(DbgridStatic.GRID_FUNID_VERIFY_SHEET)) {
			return this.dbgridDataTs.getAppealSheet(begion, strWhere);//申诉确认列表
		}	
		if(funId.equals(DbgridStatic.GRID_FUNID_APPEAL_SHEET)) {
			return this.dbgridDataTs.getAppealTimeOutSheet(begion, strWhere);//申诉超时列表
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_WAITQUALIFY_SHEET)) {
			return this.dbgridDataTs.getWaitQualifySheet(begion, strWhere);//待考核列表
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_QUALIFY_SHEET)) {
			return this.dbgridDataTs.getQualifingSheet(begion, strWhere);//考核中列表
		}

		if(funId.equals(DbgridStatic.GRID_FUNID_STAR_STAFF_LIST)) {
			return this.dbGridStarStaff.queryStarStaffList(begion, strWhere);//星级话务员查询
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_ACCEPT_HISTORY)) {
			return this.dbgridDataTs.getAcceptHistory(begion, strWhere);//受理页面查询历史信息
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_ZQ_CUST_DATA)) {
			return this.dbgridDataTs.getZqSheet(begion, strWhere);//政企传真待办工单
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_DEAL_PATCH_DATA)) {
			return this.dbgridDataTs.getDealPatchData(begion, strWhere);//工单处理分派列表
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_YNDEALDATA)) {
			return this.dbgridDataTs.getYNDealData(begion, strWhere);//疑难工单
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_ZCDEALDATA)) {
			return this.dbgridDataTs.getZCOrderData(begion, strWhere);//预受理暂存工单列表
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_BACKDEALDATA)) {
			return this.dbgridDataTs.getBackOrderData(begion, strWhere);//预受理退回工单列表
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_TS_SPECLIAL_DATA)) {
			return this.dbgridDataTs.getTSSpeciaData(begion, strWhere);//投诉特殊客户
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_MONITORS_EXPORT)) {
			return this.departmentCount.monitorExport(begion, strWhere);
		}
		if(funId.equals(DbgridStatic.GRID_FUNID_SATISFY_EXPORT)) {
			return this.departmentCount.satisfyExport(begion, strWhere);
		}
		return null;
	}


	//==========================
	/**
	 * @return dbgridDataYn
	 */
	public IdbgridDataYn getDbgridDataYn() {
		return dbgridDataYn;
	}

	/**
	 * @param dbgridDataYn 要设置的 dbgridDataYn
	 */
	public void setDbgridDataYn(IdbgridDataYn dbgridDataYn) {
		this.dbgridDataYn = dbgridDataYn;
	}
	/**
	 * @return dbgridDataTs
	 */
	public IdbgridDataTs getDbgridDataTs() {
		return dbgridDataTs;
	}
	/**
	 * @param dbgridDataTs 要设置的 dbgridDataTs
	 */
	public void setDbgridDataTs(IdbgridDataTs dbgridDataTs) {
		this.dbgridDataTs = dbgridDataTs;
	}
	/**
	 * @return dbgridDataPub
	 */
	public IdbgridDataPub getDbgridDataPub() {
		return dbgridDataPub;
	}
	/**
	 * @param dbgridDataPub 要设置的 dbgridDataPub
	 */
	public void setDbgridDataPub(IdbgridDataPub dbgridDataPub) {
		this.dbgridDataPub = dbgridDataPub;
	}
	/**
	 * @return sheetCheckData
	 */
	public IsheetCheckData getSheetCheckData() {
		return sheetCheckData;
	}
	/**
	 * @param sheetCheckData 要设置的 sheetCheckData
	 */
	public void setSheetCheckData(IsheetCheckData sheetCheckData) {
		this.sheetCheckData = sheetCheckData;
	}

	public IdbGridStarStaff getDbGridStarStaff() {
		return dbGridStarStaff;
	}
	public void setDbGridStarStaff(IdbGridStarStaff dbGridStarStaff) {
		this.dbGridStarStaff = dbGridStarStaff;
	}
	
    /**
     * 设置cmpWorksheetImpl
     * @param cmpWorksheetImpl 要设置的cmpWorksheetImpl。
     */
    public void setCmpWorksheetImpl(IdbgridCmpWorksheet cmpWorksheetImpl) {
        this.cmpWorksheetImpl = cmpWorksheetImpl;
    }
}