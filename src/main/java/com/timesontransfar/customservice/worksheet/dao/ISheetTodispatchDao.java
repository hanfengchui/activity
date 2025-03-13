package com.timesontransfar.customservice.worksheet.dao;

import com.timesontransfar.customservice.worksheet.pojo.SheetTodispatch;

public interface ISheetTodispatchDao {

	public int saveObj(SheetTodispatch obj);

	public int saveObjHis(String serviceOrderId);

	public int delete(String serviceOrderId);

	public SheetTodispatch queryLatest(String serviceOrderId);

	public int queryCountObj(String workSheetId, String serviceOrderId);

	public SheetTodispatch queryLatestTS(String workSheetId);
}
