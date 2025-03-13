package com.timesontransfar.customservice.common.message.service;

import java.util.List;

import com.timesontransfar.customservice.common.message.pojo.MessagePrompt;
import com.timesontransfar.customservice.common.message.pojo.TipsPojo;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
@SuppressWarnings("rawtypes")
public interface IMessageManager {
	
	public GridDataInfo getAllMsgSheet(int begion, int pageSize);
	/**
	 * 获取通知
	 * @return
	 */
	public String getNotifyMsg();
	/**
	 * 返回当前员工未读消息数目
	 * @return
	 */
	public int getAllMegCount();
	/**
	 * 根据GUID进历史
	 * @param 
	 * @return
	 */
	public int gotoHisById(String guid);
	/**
	 * 更新当前员工已读标示
	 * @param 
	 * @return
	 */
	public int updateReadedFlagByStaff();
	
	public int updateReadedFlagByGuid(String guid);
	
	/**
	 * 生成消息方法
	 * @param typeId 类型ID
	 * @return
	 */
	public String createMsgPrompt(MessagePrompt m);
	
	/**
	 * 保存个人提醒
	 */
	public int savePersonRemind(TipsPojo t );
	/**
	 * 获取个人提醒列表
	 * @param tipsPojo
	 * @return
	 */
	public List getPersonRemind(int staffId);
	
	
	/**
	 * 保存业务提醒
	 * @param tipsPojo
	 * @return
	 */
	public int saveBusinessRemind(TipsPojo tipsPojo,String orgId,String selecteOrg);
	
	/**
	 * 查询业务提醒列表
	 * @param tipsPojo
	 * @return
	 */
	public List getBusinessRemind(String staffId);
	
	/**
	 * 删除提醒
	 * @param tipsPojo
	 * @return
	 */
	public int removeRemind(String remindId, String flag);
}
