package com.timesontransfar.customservice.common.message.dao.impl;


import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.message.dao.IMessagePromptDao;
import com.timesontransfar.customservice.common.message.pojo.MessagePrompt;
import com.timesontransfar.customservice.common.message.pojo.MessagePromptRmp;
import com.timesontransfar.customservice.common.message.pojo.TipsPojo;

@Component("msgDao")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MessagePromptDao implements IMessagePromptDao {
	
	@Autowired
	private JdbcTemplate jt;

	/**
	 * 根据GUID进历史
	 * @param 
	 * @return
	 */
	public int gotoHisById(String guid){
		//进历史
		int rtn = moveMessageToHis(guid);
		String sql = "DELETE FROM CC_MESSAGE_PROMPT WHERE GUID = ? ";
		//删当前	
	    rtn += this.jt.update(sql,guid);
		return rtn ;
	}
	
	/**
	 * 插入新消息
	 * @param messagePrompt 
	 * @return
	 */
	public int insertMessage(MessagePrompt messagePrompt) {
		String sql = "INSERT INTO CC_MESSAGE_PROMPT(GUID,STAFF_ID,STAFF_NAME,ORG_ID,ORG_NAME,\n"
				+ "TYPE_ID,TYPE_NAME,MESSAGE_CONTENT,READED,CREAT_DATE,READED_DATE,\n"
				+ "URL_ADDR)VALUES(?,?,?,?,?,?,?,?,?,\n"
				+ "NOW(), STR_TO_DATE(?,'%Y-%m-%d %H:%i:%s'),?)";

		return this.jt.update(sql,
				messagePrompt.getGuid(),
				messagePrompt.getStaffId(),
				messagePrompt.getStaffName(),
				new Integer(messagePrompt.getOrgId()),
				messagePrompt.getOrgName(),
				messagePrompt.getTypeId(),
				messagePrompt.getTypeName(),
				messagePrompt.getMsgContent(),
				messagePrompt.getReaded(),
				StringUtils.defaultIfEmpty(messagePrompt.getReadDate(),null),
				StringUtils.defaultIfEmpty(messagePrompt.getUrlAddr(), null)
		);
	}
	
	/**
	 * 根据员工ID获取该员工未阅读消息
	 * @param staffId
	 * @return
	 */
	public List getNotReadedList(Integer staffId, int typeId){		
		String sql = "SELECT GUID,STAFF_ID,STAFF_NAME,ORG_ID,ORG_NAME,TYPE_ID,TYPE_NAME,MESSAGE_CONTENT,READED,DATE_FORMAT(CREAT_DATE,'%Y-%m-%d %H:%i:%s') CREAT_DATE,DATE_FORMAT(READED_DATE,'%Y-%m-%d %H:%i:%s') READED_DATE,URL_ADDR "
				+ "FROM CC_MESSAGE_PROMPT where STAFF_ID=? and READED=0 and type_id=? and CREAT_DATE>date_sub(now(),interval 2 day)";
       return this.jt.query(sql, new Object[]{staffId, typeId}, new MessagePromptRmp());
	}

	/**
	 * 根据员工ID获取该员工所有未阅读消息
	 * @param staffId
	 * @return
	 */
	public List getAllNotReadedList(Integer staffId){		
		String sql = " SELECT GUID,STAFF_ID,STAFF_NAME,ORG_ID,ORG_NAME,TYPE_ID,TYPE_NAME," +
		"MESSAGE_CONTENT,READED,DATE_FORMAT(CREAT_DATE,'%Y-%m-%d %H:%i:%s') CREAT_DATE," +
		"DATE_FORMAT(READED_DATE,'%Y-%m-%d %H:%i:%s') READED_DATE,URL_ADDR " +
		"FROM CC_MESSAGE_PROMPT where STAFF_ID=? and READED=0 and CREAT_DATE>date_sub(now(),interval 2 day)";
		return this.jt.query(sql, new Object[]{staffId}, new MessagePromptRmp());
	}
	
	/**
	 * 根据员工ID获取该员工消息
	 * @param staffId
	 * @return
	 */
	public List getMessageByStaffId(Integer staffId){
		String sql = "SELECT GUID,STAFF_ID,STAFF_NAME,ORG_ID,ORG_NAME,TYPE_ID,TYPE_NAME," +
				"MESSAGE_CONTENT,READED,DATE_FORMAT(CREAT_DATE,'%Y-%m-%d %H:%i:%s') CREAT_DATE," +
				" DATE_FORMAT(READED_DATE,'%Y-%m-%d %H:%i:%s') READED_DATE,URL_ADDR " +
				"FROM CC_MESSAGE_PROMPT where STAFF_ID=?";
		return this.jt.query(sql, new Object[]{staffId}, new MessagePromptRmp());
	}
	
	/**
	 * 根据guid获取消息
	 * @param staffId
	 * @return
	 */
	public MessagePrompt getMessageByGuid(String guid){
		String sql = "SELECT GUID,STAFF_ID,STAFF_NAME,ORG_ID,ORG_NAME,TYPE_ID,TYPE_NAME," +
				"MESSAGE_CONTENT,READED,DATE_FORMAT(CREAT_DATE,'%Y-%m-%d %H:%i:%s') CREAT_DATE," +
				"DATE_FORMAT(READED_DATE,'%Y-%m-%d %H:%i:%s') READED_DATE,URL_ADDR " +
				"FROM CC_MESSAGE_PROMPT where GUID=?";
		List list =  this.jt.query(sql, new Object[]{guid}, new MessagePromptRmp()); 
		return (!list.isEmpty() ? ((MessagePrompt)list.get(0)) : null);
	}

	/**
	 * 将编号为guid的消息移到历史库
	 * @param guid
	 * @return
	 */
	public int moveMessageToHis(String guid){
		String sql = 
			"insert into cc_message_prompt_his\n" +
			"  (guid,\n" + 
			"   staff_id,\n" + 
			"   staff_name,\n" + 
			"   org_id,\n" + 
			"   org_name,\n" + 
			"   type_id,\n" + 
			"   type_name,\n" + 
			"   message_content,\n" + 
			"   readed,\n" + 
			"   creat_date,\n" + 
			"   readed_date,\n" + 
			"   url_addr)\n" + 
			"  select guid,\n" + 
			"         staff_id,\n" + 
			"         staff_name,\n" + 
			"         org_id,\n" + 
			"         org_name,\n" + 
			"         type_id,\n" + 
			"         type_name,\n" + 
			"         message_content,\n" + 
			"         readed,\n" + 
			"         creat_date,\n" + 
			"         readed_date,\n" + 
			"         url_addr\n" + 
			"    from cc_message_prompt\n" + 
			"   where guid = ?";
		return this.jt.update(sql, guid);
	}
	/**
	 * 更新当前员工已读标识
	 * @param staffId 
	 * @return
	 */
	public int updateReadFlagByStaff(Integer staffId){
		String sql = "UPDATE CC_MESSAGE_PROMPT SET READED = 1, READED_DATE = NOW() WHERE STAFF_ID = ?" ;
		return this.jt.update(sql, staffId);
	}
	
	public int updateReadedFlagByGuid(String guid){
		String sql = "UPDATE CC_MESSAGE_PROMPT SET READED = 1, READED_DATE = NOW() WHERE GUID = ?" ;
		return this.jt.update(sql, guid);
	}
	
	/**
	 * 更新已读标识及阅读时间
	 * @param guid
	 * @return
	 */
	public int updateReadFlag(MessagePrompt messagePrompt){
		String sql = "UPDATE CC_MESSAGE_PROMPT SET STAFF_ID=?,STAFF_NAME=?,ORG_ID=?," +
				"ORG_NAME=?,TYPE_ID=?,TYPE_NAME=?,MESSAGE_CONTENT=?,READED=?," +
				"CREAT_DATE=str_to_date(?,'%Y-%m-%d %H:%i:%s'),READED_DATE=str_to_date(?,'%Y-%m-%d %H:%i:%s')," +
				"URL_ADDR=? where GUID=?";
		return this.jt.update(sql,
				messagePrompt.getStaffId(),
				StringUtils.defaultIfEmpty(messagePrompt.getStaffName(),null),
				new Integer(messagePrompt.getOrgId()),
				StringUtils.defaultIfEmpty(messagePrompt.getOrgName(),null),
				messagePrompt.getTypeId(),
				StringUtils.defaultIfEmpty(messagePrompt.getTypeName(),null),
				StringUtils.defaultIfEmpty(messagePrompt.getMsgContent(),null),
				messagePrompt.getReaded(),
				StringUtils.defaultIfEmpty(messagePrompt.getCrtDate(),null),
				StringUtils.defaultIfEmpty(messagePrompt.getReadDate(),null),
				StringUtils.defaultIfEmpty(messagePrompt.getUrlAddr(),null),
				StringUtils.defaultIfEmpty(messagePrompt.getGuid(),null)
		);
	}

	@Override
	public int savePersonRemind(TipsPojo t) {
		String sql = "insert into CC_PERSONAL_REMIND(REMIND_ID,STAFF_ID,REMIND_TITLE,REMIND_CONTENT,CREATETIME,REMIND_TIME,REMIND_TYPE) " + 
				" values(UPPER(replace(UUID(), '-', '')),?,?,?,NOW(),str_to_date(?, '%Y-%m-%d %H:%i:%s'),?)";
		return this.jt.update(sql, t.getStaffId(), t.getRemindTitle(), t.getRemindContent(), 
				t.getRemindTime(), t.getRemindType());
	}

	@Override
	public List getPersonRemind(TipsPojo t) {
		String sql = "select a.REMIND_ID,a.REMIND_TITLE,a.REMIND_CONTENT,DATE_FORMAT(a.CREATETIME,'%Y-%m-%d %H:%i') CREATETIME," + 
				"DATE_FORMAT(a.REMIND_TIME,'%Y-%m-%d %H:%i') REMIND_TIME,a.REMIND_TYPE from CC_PERSONAL_REMIND a" + 
				" where a.staff_id = ? and a.remind_time >= now() order by a.remind_time asc"; 
		return this.jt.queryForList(sql, t.getStaffId());
	}

	@Override
	public int saveBusinessRemind(TipsPojo t) {
		String sql="insert into CC_BUSINESS_REMIND" +
				"(REMIND_ID,CREATE_STAFF_ID,CREATE_STAFF_NAME,REMIND_TITLE,REMIND_CONTENT,REMIND_TYPE," + 
				"CREATETIME,TITLE_SORT,DISPLAY,LINK_ORG) values (UPPER(replace(UUID(), '-', '')), ?, ?, ?, ?, ?, now(), ?, ?, ?) ";
		String qrySort="select max(title_sort)as SORT from CC_BUSINESS_REMIND";
		List<Map<String, Object>> getSort=this.jt.queryForList(qrySort);
		
		int sort=getSort.get(0).get("SORT")==null? 0 :Integer.parseInt(getSort.get(0).get("SORT").toString());
		return this.jt.update(sql, t.getCreateStaffId(), t.getCreateStaffName(), t.getRemindTitle(), t.getRemindContent(), 
				t.getRemindType(), sort+1, t.getDisplay(), t.getLinkOrg());
	}

	@Override
	public List<Map<String, Object>> getUpOrg() {
		String sql = "select LINKID,ORG_NAME from TSM_ORGANIZATION where linkid='10-11' ";
		return this.jt.queryForList(sql);
	}

	@Override
	public List getBusinessRemind(String linkOrg) {
		String departsql="select REMIND_ID,REMIND_TITLE,REMIND_CONTENT,REMIND_TYPE,DATE_FORMAT(CREATETIME,'%Y-%m-%d %H:%i') CREATETIME,TITLE_SORT from CC_BUSINESS_REMIND where link_org=? and display=1 ";
		
		String proviceSql="select REMIND_ID,REMIND_TITLE,REMIND_CONTENT,REMIND_TYPE,DATE_FORMAT(CREATETIME,'%Y-%m-%d %H:%i') CREATETIME,TITLE_SORT from CC_BUSINESS_REMIND where link_org='10' and display=1 order by title_sort asc";
		List list1=this.jt.queryForList(departsql, linkOrg);
		List list2=this.jt.queryForList(proviceSql);
		if(!list2.isEmpty()) {
			list1.addAll(0,list2);
		}
		return list1;
	}

	@Override
	public int removeRemind(String remindId, String flag) {
		String sql = "";
		if("1".contentEquals(flag)){
			sql = "delete from CC_PERSONAL_REMIND where remind_id=?";
		}else {
			sql = "delete from CC_BUSINESS_REMIND where remind_id=?";
		}
		return this.jt.update(sql, remindId);
	}
}
