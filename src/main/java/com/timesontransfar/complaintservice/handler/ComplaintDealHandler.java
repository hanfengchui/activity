/*
 * 文 件 名：ComplaintDealHandler.java
 * 版    权：
 * 描    述：
 * 修 改 人：LiJiahui
 * 修改时间：2013-1-29
 * 修改内容：新增
 */
package com.timesontransfar.complaintservice.handler;

import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.StaticData;

/**
 * 帮助类
 * 
 * @author LiJiahui
 * @version 1.0
 * @since 2013-02
 */
@Component("cmpDealHandler")
public class ComplaintDealHandler {

    /**
     * 判断工单当前是否可以处理。如果挂起、审批中等等，是不允许进行正常处理的。
     * @param sheetStatu 工单状态
     * @return true 不在处理中状态；false 在处理中状态
     */
    public boolean notInDeal(int sheetStatu, int lockFlag){
    	boolean flag = false;
    	if(lockFlag == 1){
    		switch (sheetStatu) {
				case StaticData.WKST_HOLD_STATE:
				case StaticData.WKST_HOLD_STATE_NEW:
				case StaticData.WKST_ARBIY_APPLY:
				case StaticData.WKST_APPLY_ASSIST:
					flag = true;
					break;
				default:
					break;
			}    		
    	}else{
    		flag = true;
    	}
    	return flag;
    }
    
    /**
	 * 判断工单状态是否为挂起
	 * @param sheetStatu 工单状态
	 * @return true表示挂起
	 */
    public static boolean isHoldSheet(int sheetStatu){
        return StaticData.WKST_HOLD_STATE == sheetStatu || StaticData.WKST_HOLD_STATE_NEW == sheetStatu; 
    }
    
}