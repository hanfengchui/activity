package com.timesontransfar.customservice.common.message.dao;

import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.common.message.pojo.MessagePrompt;
import com.timesontransfar.customservice.common.message.pojo.TipsPojo;
@SuppressWarnings("rawtypes")
public interface IMessagePromptDao {
	/**
	 * 根据员工ID获取该员工所有未阅读消息
	 * @param staffId
	 * @return
	 */
	public List getAllNotReadedList(Integer staffId);
	/**
	 * 根据GUID进历史
	 * @param 
	 * @return
	 */
	public int gotoHisById(String guid);
	/**
	 * 更新当前员工已读标识
	 * @param staffId 
	 * @return
	 */
	public int updateReadFlagByStaff(Integer staffId);
	
	public int updateReadedFlagByGuid(String guid);
	/**
	 * 插入新消息
	 * @param messagePrompt
	 * @return
	 */
	public int insertMessage(MessagePrompt messagePrompt);

	/**
	 * 根据员工ID获取该员工未阅读消息
	 * @param staffId
	 * @return
	 */
	public List getNotReadedList(Integer staffId,int typeId);

	/**
	 * 根据员工ID获取该员工消息
	 * @param staffId
	 * @return
	 */
	public List getMessageByStaffId(Integer staffId);

	/**
	 * 将编号为guid的消息移到历史库
	 * @param guid
	 * @return
	 */
	public int moveMessageToHis(String guid);

	/**
	 * 更新已读标识及阅读时间
	 * @param guid
	 * @return
	 */
	public int updateReadFlag(MessagePrompt messagePrompt);

	/**
	 * 根据guid获取消息
	 * 
	 * @param staffId
	 * @return
	 */
	public MessagePrompt getMessageByGuid(String guid);
	
	/**
	 * 保存个人提醒
	 */
	public int savePersonRemind(TipsPojo tipsPojo);
	
	/**
	 * 获取个人提醒列表
	 * @param tipsPojo
	 * @return
	 */
	public List getPersonRemind(TipsPojo tipsPojo);
	
	/**
	 * 保存业务提醒
	 * @param tipsPojo
	 * @return
	 */
	public int saveBusinessRemind(TipsPojo tipsPojo);
	
	/**
	 * 查询重点部门列表
	 * @param tipsPojo
	 * @return
	 */
	public List<Map<String, Object>> getUpOrg();
	
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
