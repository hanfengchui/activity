/*
 * 文 件 名：IdbgridCmpWorksheet.java
 * 版    权：
 * 描    述：
 * 修 改 人：Administrator
 * 修改时间：2013-9-9
 * 修改内容：新增
 */
package com.timesontransfar.customservice.dbgridData;

/**
 *  添加类的描述
 * 
 * @author Administrator
 * @version
 * @since
 */
public interface IdbgridCmpWorksheet {
    public GridDataInfo getCmpWorksheet(int begin, String where);
    public GridDataInfo getWorksheet(int begin, String strWhere);
}
