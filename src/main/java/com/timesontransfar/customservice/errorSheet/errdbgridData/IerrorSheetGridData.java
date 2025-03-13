package com.timesontransfar.customservice.errorSheet.errdbgridData;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;

public interface IerrorSheetGridData {
	/**
	 * 取得错单当前列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrSheetList(int begion,String strWhere);
	/**
	 * 取得错单历史列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrSheetHisList(int begion,String strWhere);
	/**
	 * 取得错单填写列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrAcceptSheet(int begion,String strWhere);
	/**
	 * 取得错单审核列表数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrAuditSheet(int begion,String strWhere);
	
	/**
	 * 取得错单当前列表所有数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrAllSheetList(int begion,String strWhere,String flag);
	
	/**
	 * 取得错单历史列表所有数据
	 * @param begion 开始
	 * @param strWhere WHERE条件
	 * @return
	 */
	public GridDataInfo getErrAllSheetHisList(int begion,String strWhere,String flag);
}
