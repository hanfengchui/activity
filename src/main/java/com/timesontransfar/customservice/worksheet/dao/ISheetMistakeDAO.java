/*
 * 文 件 名：ISheetMistakeDAO.java
 * 版    权：
 * 描    述：
 * 修 改 人：Administrator
 * 修改时间：2013-3-12
 * 修改内容：新增
 */
package com.timesontransfar.customservice.worksheet.dao;

import com.timesontransfar.customservice.worksheet.pojo.TSOrderMistake;

/**
 * 操作表CC_SHEET_MISTAKE、CC_SHEET_MISTAKE_HIS的方法定义
 * 
 * @author LiJiahui
 * @version 1.0
 * @since 2013-3-12
 */
public interface ISheetMistakeDAO {

	public int insertOrderMistake(TSOrderMistake orderMistake);

	public int insertOrderMistakeHisByOrderId(String serviceOrderId);
}