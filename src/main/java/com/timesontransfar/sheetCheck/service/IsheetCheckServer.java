/**
 * @author 万荣伟
 */
package com.timesontransfar.sheetCheck.service;

import java.util.List;

import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.sheetCheck.pojo.SheetCheckAdju;
import com.timesontransfar.sheetCheck.pojo.SheetCheckAppeal;
import com.timesontransfar.sheetCheck.pojo.SheetCheckInfo;
import com.timesontransfar.sheetCheck.pojo.SheetCheckObj;
import com.timesontransfar.sheetCheck.pojo.SheetCheckSchem;

/**
 * @author 万荣伟
 * 
 */
@SuppressWarnings("rawtypes")
public interface IsheetCheckServer {
    /**
     * 保存质检模板
     * 
     * @param bean模板对象
     * @return
     */
    public String saveCheckSchem(SheetCheckSchem bean);

    /**
     * 更新模板
     * 
     * @param bean
     * @return
     */
    public String updateCheckSchem(SheetCheckSchem bean);

    /**
     * 保存工单质检
     * 
     * @param bean
     *            质检对象
     * @param beanObj
     *            质检评判对象
     * @return
     */
    public String saveSheetCheetObj(SheetCheckInfo bean, SheetCheckAdju[] beanObj);

    /**
     * 根据质检类型得到质检模板
     * 
     * @param typeId
     * @return
     */
    public List getSheetCheckSchem(int typeId);

    /**
     * 得到工单质检记录和评判标准
     * 
     * @param checkId
     *            质检ID
     * @return
     */
    public SheetCheckObj getSheetCheckObj(String checkId);

    /**
     * 申诉修改 被质检人员对质检单进行申诉后进行修改的动作,称之为申诉修改
     * 
     * @param bean
     *            质检对象
     * @param beanObj
     *            质检评判对象
     * @return
     */
    public String updateSheetCheetObj(SheetCheckInfo bean, SheetCheckAdju[] beanObj);

    /**
     * 质检修改 对质检第一次质检的并还没有进行申诉确认单子 可以进行修改,这个修改修改操作称之为 质检修改
     * 
     * @param bean
     *            质检对象
     * @param beanObj
     *            质检评判对象
     * @return
     */
    public String updateSheetCheetForResave(SheetCheckInfo bean, SheetCheckAdju[] beanObj);

    /**
     * 保存申诉回复内容
     * 
     * @param sheetCheckInfo
     * @return
     */
    public String submitCheckReply(SheetCheckInfo sheetCheckInfo);

    /**
     * 质检申诉
     * 
     * @return
     */
    public String doAppealCheckSheet(SheetCheckAppeal appealBean);

    /**
     * 提交确认--质检确认
     * 
     * @param checkId
     * @return
     */
    public String submitAffirmance(String checkId);

    /**
     * 得到工单质检记录和评判标准历史
     * 
     * @param checkId
     *            质检ID
     * @return
     */
    public SheetCheckObj getSheetCheckObjHis(String checkId);

    /**
     * 根据质检单流水号，获取历史质检信息的列表，包含质检记录和评判标准
     * 
     * @author LiJiahui
     * @date 2012-4-10
     * @param checkId
     *            质检单流水号
     * @return 质检信息列表
     */
    public List getSheetCheckObjHisList(String checkId);

    /**
     * 检查可否质检
     * 
     * @param orderId
     *            定单id
     * @param sheetId
     *            工单id
     * @param checkType
     *            质检类型
     * @return
     */
    public String validateCheckable(String orderId, String sheetId, int checkType);
    
    public GridDataInfo getQualitySheetList(String parm);
    
	public List getTemplateList(int tacheId);
    
    public int allotQualitySheet(String parm);
    
    public GridDataInfo getQualityReturnList(String parm);
    
    public GridDataInfo getCheckSheetList(String parm);
    
    public String getTemplateInfo(String templateId);
    
    public int saveEleAnswer(String parm);
    
    public GridDataInfo getCheckResultList(String parm);
    
    public String getTemplateResult(String sheetId,String templateId);
    
    public int saveQualityAppeal(String parm);
    
    public GridDataInfo getCheckAppealList(String parm);
    
    public int updateQualityContent(String parm);
    
    public GridDataInfo getQualityQueryList(String parm);
}
