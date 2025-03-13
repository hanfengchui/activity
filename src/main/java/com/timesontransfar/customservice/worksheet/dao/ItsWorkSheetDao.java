/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.dao;

import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.worksheet.pojo.RetVisitResult;
import com.timesontransfar.customservice.worksheet.pojo.SheetOperation;
import com.timesontransfar.customservice.worksheet.pojo.SheetReadRecordInfo;
import com.timesontransfar.customservice.worksheet.pojo.TsSheetDealType;
import com.timesontransfar.customservice.worksheet.pojo.WorkSheetStatuApplyInfo;



/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface ItsWorkSheetDao {
	/**
	 * 投诉保存处理类型
	 * @param bean 
	 * @return
	 */
	public int saveSheetDealType(TsSheetDealType bean);
	/**
	 * 动作表：增加唯一值派单
	 * Description: <br> 
	 * @author huangbaijun<br>
	 * @taskId <br>
	 * @param bean
	 * @return <br>
	 * @CreateDate 2020年10月14日 下午8:07:50 <br>
	 */
	public int querySheetDealType(TsSheetDealType bean);
	
	/**
	 * 根据定单号删除当前表中处理类型
	 * @param orderId 定单号
	 * @param month 月分区
	 * @return 删除的数目
	 */
	public int deleteOrderDealType(String orderId,Integer month);
	
	/**
	 * 根据工单号删除记录
	 * @param sheetId
	 * @return
	 */
	public int deleteDealTypeBySheetid(String sheetId);
	
	/**
	 * 根据定号，月分区，把当前表中的数据迁移到历史中
	 * @param orderId 定单号
	 * @param month 月分区 
	 * @return 保存数
	 */
	public int saveSheetDealTypeHis(String orderId,int region);
	
	/**
	 * 保存投诉回访客户内容
	 * @param retVisitResult
	 * @return
	 */
	public int saveResVisitResult(RetVisitResult retVisitResult);
	
	/**
	 * 定单竣工的时候，从当前表中删除客户回访
	 * @param orderId 定单号
	 * @param month 月分区
	 * @return 删除的记录数
	 */
	public int deleOrderResVisitResult(String orderId,Integer month);
	
	/**
	 * 工单挂起,释放提出申请
	 * @param sheetApply 申请工单对象
	 * @boo true 为保存当前,false保存历史
	 * @return
	 */
	public int saveSheetApply(WorkSheetStatuApplyInfo sheetApply,boolean boo);
	
	/**
	 * 根据定单号,删除与该定单关联的挂起,释放申请信息
	 * @param orderId
	 * @return
	 */
	public int deleteSheetApply(String orderId);
	
	/**
	 * 根据工单号得到该工单所有的申请信息
	 * @param sheetId 工单号
	 * @boo 为true查询当前
	 * @return
	 */
	public WorkSheetStatuApplyInfo[] getsheetApplyObj(String condition,boolean boo);
	
	/**
	 * 更新申请审批状态
	 * @param sheetApply
	 * @return
	 */
	public int updateSheetApply(WorkSheetStatuApplyInfo sheetApply);
	
	/**
	 * 根据工单号、旧状态值，更新申请记录的状态值
	 * @param worksheetId 工单号
	 * @param oldStatu 旧状态值
	 * @param newStatu 新状态值
	 * @return
	 */
	public int updateSheetApplyStatu(String worksheetId, int oldStatu, int newStatu);
	/**
	 * @param orderId 定单号
	 * @param regionId 业务归属地
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public int getTsSheetDealCount(String orderId,int regionId,boolean boo);
	/**
	 * 查询投诉处理类型
	 * @param orderId 定单号
	 * @param regionId 业务归属地
	 * @param boo true 查询当前 false 查询历史
	 * @return
	 */
	public List getTsSheetDealObj(String orderId,int regionId,boolean boo);
	
	public List getTsSheetDealObjNew(String orderId,boolean boo);

	public List getXcSheetDealObj(String orderId,int regionId,boolean boo);
	/**
	 * 得到跟录音文件有关系的定单信息
	 * @param orderId 定单号
	 * @param regionId 地域
	 * @param boo true 得到当前 false 得到历史
	 * @return
	 */
	public List getRecordOrderInfo(String orderId,int regionId,boolean boo);
	
	/**
	 * 得到录音FTP信息
	 * @param flowId 录音流水号
	 * @return
	 */
	public List getFtpFilesByFlowId(String flowId);
		
	public String getAreaId(String orgId);
	
	/**
	 * 得到录音文件的FTP服务器
	 * @param regionId FTP服务器唯一ID
	 * @return
	 */
	public List getRecordFtp(int regionId,String wasHost);
	
	/**
	 * 根据客户号码和地域
	 * @param regionId 地域
	 * @param custNum 客户号码
	 * @return
	 */
	public List getTsEspeciallyCustInfo(int regionId,String custNum);
	
	/**
	 * 得到特殊客户审核定性记录
	 * @param regionId 地域
	 * @param custNum 客户号码
	 * @return
	 */
	public List getAudQualitative(int regionId,String custNum);
	
	/**
	 * 根据审核工单或审批工单得相关联的完成工单
	 * @param sheetId
	 * @param regionId
	 * @return
	 */
	public List getRelatingSheet(String sheetId,int regionId);
	
	/**
	 * 得到该定单下是否有考核或归档的工单
	 * @param orderId
	 * @return
	 */
	public int getOrderDealData(String orderId);
	
	/**
	 * 得到工单状态审批数据
	 * @param sheetId 工单号
	 */
	public List getSheetStatuAud(String sheetId);
	
	/**
	 * 保存工单阅读记录
	 * @param bean 阅读记录对象
	 * @return
	 */
	public int saveSheetReadRecord(SheetReadRecordInfo bean);
	
	/**
	 * 保存工单操作记录
	 * @param operation
	 * @return
	 */
	public int insertSheetOperation(SheetOperation operation);
	
	/**
	 * 获取直派转派信息
	 * @param orderId
	 * @param remark
	 * @return
	 */
	public Map getDispatchOrgMap(String orderId, String remark);
}