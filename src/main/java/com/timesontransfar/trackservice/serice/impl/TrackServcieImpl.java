package com.timesontransfar.trackservice.serice.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.trackservice.dao.TrackDao;
import com.timesontransfar.trackservice.serice.ITrackServcie;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@SuppressWarnings("rawtypes")
@Component(value="trackServcie")
public class TrackServcieImpl implements ITrackServcie{
	
	@Autowired
	private TrackDao trackDao;
	
	@Override
	public String findOrderHasten(String orderId,boolean boo) {
		List hastenInfo=this.trackDao.findOrderHasten(orderId,boo);
		if(!hastenInfo.isEmpty()){
			JSONArray arrary=JSONArray.fromObject(hastenInfo);
			return arrary.toString();
		}
		return "[]";
	}
	
	@Override
	public String queryWorkSheetHis(String prodNum, String relaInfo, int regionId, String orderId, boolean cliqueFlag) {
		List sheetInfo = this.trackDao.workSheetHisInfo(prodNum, relaInfo, regionId, orderId, cliqueFlag);
		if(!sheetInfo.isEmpty()){
			JSONArray arrary = JSONArray.fromObject(sheetInfo);
			return arrary.toString();
		}
		return "[]";
	}
	
	public int queryWorkSheetHisCount(String prodNum, String relaInfo, int regionId, String orderId, boolean cliqueFlag) {
		return this.trackDao.workSheetHisCount(prodNum, relaInfo, regionId, orderId, cliqueFlag);
	}
	
	@Override
	public String sheetLimiteInfo(String sheetId) {
		List list = this.trackDao.querySheetLimite(sheetId);
		if(!list.isEmpty()){
			return JSONObject.fromObject(list.get(0)).toString();
		}else {
			return list.toString();
		}
	}
	
	public int getCallOutCount(String orderId) {
		return trackDao.getCallOutCount(orderId);
	}

	public List getCallOutRecord(String orderId, boolean curFlag) {
		return trackDao.getCallOutRecord(orderId, curFlag);
	}

	@Override
	public List getCallOutForOrderInfo(String orderId,String orgId, boolean curFlag) {
		String tableName = curFlag ? "cc_work_sheet" : "cc_work_sheet_his";
		return trackDao.getCallOutForOrderInfo(orderId,orgId, tableName);
	}

	public List getSatisfyCallOutRecord(String orderId, boolean curFlag) {
		return trackDao.getSatisfyCallOutRecord(orderId, curFlag);
	}
	
	public boolean getPlayVoiceFlag(String orderId, boolean curFlag) {
		return trackDao.getPlayVoiceFlag(orderId, curFlag);
	}
	
	public boolean getSatisfyPlayVoiceFlag(String orderId, boolean curFlag) {
		return trackDao.getSatisfyPlayVoiceFlag(orderId, curFlag);
	}

	@Override
	public boolean savePromise(String staffId, String staffName, int type) {
		return trackDao.savePromise(staffId, staffName, type);
	}

	@Override
	public boolean getPromise(String staffId, int type) {
		return trackDao.getPromise(staffId, type);
	}

	@Override
	public String getWorkSheetHisByPordNum(String prodNum, String beginTime,String endTime,String orderId) {
		List sheetInfo = this.trackDao.getWorkSheetHisByPordNum(prodNum, beginTime,endTime,orderId);
		if(!sheetInfo.isEmpty()){
			JSONArray arrary = JSONArray.fromObject(sheetInfo);
			return arrary.toString();
		}
		return "[]";
	}
}
