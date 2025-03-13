/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.dao;

import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.NoteSendList;
import com.timesontransfar.customservice.worksheet.pojo.ReturnBlackList;

/**
 * @author 万荣伟
 *
 */
@SuppressWarnings("rawtypes")
public interface InoteSenList {
	
	/**
	 * 发送短信，将短信的基本信息保存的到短信表中
	 * @param bean
	 * @return
	 */
	public int saveNoteContent(NoteSeand bean);
	
	/**
	 * 保存要发送的短信
	 * @param bean
	 * @return
	 */
	public int saveNoteSendList(NoteSendList bean);
	
	/**
	 * 得到短信发送号码
	 * @param orgId 部门id 
	 * @param tachId 环节id
	 * @param relaId 联系人ID
	 * @param queryType 0为派单到工单池,1为派单到个人
	 * @return
	 */
	public List getNoteSendNum(String orgId,String relaId,int tachId,int queryType);

	
	/**
	 * 更新短信发送号码
	 * @param noteGuid 唯一ID
	 * @param relaPhone 发送电话
	 * @param clientType 终端号码
	 * @return
	 */
	public int updateNoteList(NoteSendList bean);
	
	/**
	 * 得到号码信息
	 * @param sourcenum
	 * @return
	 */
	public Map getNumInfo(String sourcenum); 
	
	/**
	 * 根据主健删除短信列表
	 * @param guid
	 * @return
	 */
	public int deleteNoteList(String guid);

	public int updateReturnList(ReturnBlackList bean);
	public int saveReturnList(ReturnBlackList bean);

	public int getReturnExist(ReturnBlackList bean);
}
