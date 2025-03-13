/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.service;

import java.util.List;

/**
 * @author 万荣伟
 * 
 */
public interface ItsSheetDealPub {

    /**
     * 批量派单工单
     * 根据查询条件，系统批量分派工单池中的工单<br>
     * @param str
     * @author LiJiahui
     * @date 2017-3-1改动
     * @return 操作结果的描述
     */
    public String autoAllotWorkSheet(String str, List<String> paramList);
}
