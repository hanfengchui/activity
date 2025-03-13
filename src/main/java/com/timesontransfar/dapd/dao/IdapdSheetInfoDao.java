package com.timesontransfar.dapd.dao;

import com.dapd.pojo.DapdSheetInfo;

public interface IdapdSheetInfoDao {
	public int insertDapdSheetInfo(DapdSheetInfo dapd);

	public int updateDapdSheet(DapdSheetInfo dapd);

	public int updateAcceptTemplate(DapdSheetInfo dapd);

	public int updateDapdFlag(DapdSheetInfo dapd);

	public int updateCompInfo(DapdSheetInfo dapd);

	public int updateDealTemplate(DapdSheetInfo dapd);

	public int updateDapdEndDate(DapdSheetInfo dapd);

	public int updateDapdSatEval(DapdSheetInfo dapd);

	public int updateDapdArchiveDate(DapdSheetInfo dapd);

	public DapdSheetInfo selectDapdSheetInfoBySheetIdProv(String sheetIdProv);
}