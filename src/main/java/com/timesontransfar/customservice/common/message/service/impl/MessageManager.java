package com.timesontransfar.customservice.common.message.service.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.model.TsmStaff;
import com.timesontransfar.common.authorization.service.ISystemAuthorization;
import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.customservice.common.StaticData;
import com.timesontransfar.customservice.common.message.dao.IMessagePromptDao;
import com.timesontransfar.customservice.common.message.pojo.MessagePrompt;
import com.timesontransfar.customservice.common.message.pojo.TipsPojo;
import com.timesontransfar.customservice.common.message.service.IMessageManager;
import com.timesontransfar.customservice.dbgridData.DbgridStatic;
import com.timesontransfar.customservice.dbgridData.GridDataInfo;
import com.timesontransfar.customservice.dbgridData.IdbgridDataPub;

@Component("messageManager")
@SuppressWarnings("rawtypes")
public class MessageManager implements IMessageManager {
	protected static Logger log = LoggerFactory.getLogger(MessageManager.class);
	
	@Autowired
	private ISystemAuthorization systemAuthorization;
	@Autowired
	private IMessagePromptDao msgDao;
	@Autowired
	private PubFunc pubFunc;
	@Autowired
	private IdbgridDataPub dbgridDataPub;
	
	/**
     * 取得所有消息数据
     * @param begion 开始
     * @param strWhere WHERE条件
     * @return
     */
	@SuppressWarnings("unchecked")
	@Override
	public GridDataInfo getAllMsgSheet(int begion, int pageSize) {
        TsmStaff logonStaff = pubFunc.getLogonStaff();
        String strSql = "SELECT \r\n" 
                + "      A.GUID ,\r\n" 
                + "      A.STAFF_ID ,\r\n"
                + "      A.STAFF_NAME,\r\n" 
                + "      A.ORG_ID,\r\n" 
                + "      A.ORG_NAME,\r\n"
                + "      A.TYPE_ID,\r\n" 
                + "      A.TYPE_NAME,\r\n" 
                + "      A.MESSAGE_CONTENT,\r\n"
                + "      A.READED,\r\n"
                + "      DATE_FORMAT(A.CREAT_DATE, '%Y-%m-%d %H:%i:%s') AS CREAT_DATE   ,\r\n"
                + "      DATE_FORMAT(A.READED_DATE, '%Y-%m-%d %H:%i:%s') AS READED_DATE ,\r\n"
                + "      A.URL_ADDR\r\n" 
                + "  FROM CC_MESSAGE_PROMPT A WHERE 1 = 1 \r\n"
                + "      AND A.STAFF_ID = '" + logonStaff.getId()+ "' AND A.CREAT_DATE>DATE_ADD(now(),INTERVAL -2 DAY) ";
        Map tableMap = new HashMap();
        tableMap.put("CC_MESSAGE_PROMPT", "A");
        strSql = this.systemAuthorization.getAuthedSql(tableMap, strSql, "900018326");// 消息实体
        strSql = "select * from (" + strSql + ") T where 1 = 1 ";
        return this.dbgridDataPub.getResultBySize(strSql, begion, pageSize, " order by readed asc,creat_date desc", DbgridStatic.GRID_FUNID_MSG_PROMPT_LIST);
    }

	
	/**
	 * 生成消息方法
	 * @param typeId 类型ID
	 * @return
	 */
	public String createMsgPrompt(MessagePrompt m){		
		String returnStr = "ERROR";
		MessagePrompt po = new MessagePrompt();
		if(!pubFunc.isLogonFlag()){
		    po.setOrgId("10");
            po.setOrgName("江苏省电信公司");
		}else{
		    TsmStaff s = pubFunc.getLogonStaff();
		    po.setOrgId(s.getOrganizationId());
	        po.setOrgName(s.getOrgName());
		}
		po.setStaffId(m.getStaffId());
		po.setStaffName(pubFunc.getStaffName(m.getStaffId()));
		po.setTypeId(m.getTypeId());
		po.setTypeName(m.getTypeName());		
		po.setMsgContent(m.getMsgContent());
		po.setReaded(0);
		po.setReadDate("");
		po.setUrlAddr("");
		po.setGuid(pubFunc.crtGuid());
		int i = msgDao.insertMessage(po);
		if(i>0){
			returnStr=  "SUCCESS";
		}
	  return returnStr;			
	}	
	
	/**
	 * 返回当前员工未读消息数目
	 * @return
	 */
	public int getMegCount(int typeId){ 
		TsmStaff staff = pubFunc.getLogonStaff();
		return msgDao.getNotReadedList(new Integer(staff.getId()),typeId).size();
	}
	
	/**
	 * 返回当前员工未读消息数目
	 * @return
	 */
	public int getAllMegCount(){ 
		TsmStaff staff = pubFunc.getLogonStaff();
		return msgDao.getAllNotReadedList(new Integer(staff.getId())).size();
	}
	
	public String getNotifyMsg(){
		int ctn = this.getAllMegCount();
		if(ctn == 0) {
			return "";
		}
		return this.getMegTemplate();
	}

	
	/**
	 * 返回模版消息
	 * @param typeId 类型ID
	 * @return
	 */
	public String getMegTemplate() {
		String str = "";
		String msgType="";		
		int err = getMegCount(StaticData.MESSAGE_PROMPT_ERROR);
		if(err > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_ERROR);
			str +="有" + err + "条未读"+msgType +"\r\n";
		}
		int auit = getMegCount(StaticData.MESSAGE_PROMPT_ERROR_AUIT);
		if(auit > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_ERROR_AUIT);
			str +="有" + auit + "条未读"+msgType +"\r\n";
		}
		int quality = getMegCount(StaticData.MESSAGE_PROMPT_QUALITY);
		if(quality > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_QUALITY);
			str +="有" + quality + "条未读"+msgType +"\r\n";
		}
		int verify = getMegCount(StaticData.MESSAGE_PROMPT_QUALITY_VERIFY);
		if(verify > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_QUALITY_VERIFY);
			str +="有" + verify + "条未读"+msgType +"\r\n";
		}
		int chinatelecom = getMegCount(StaticData.MESSAGE_PROMPT_CHINATELECOM);
		if(chinatelecom > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_CHINATELECOM);
			str +="有" + chinatelecom + "条未读"+msgType +"\r\n";
		}
		int tyxrw = getMegCount(StaticData.MESSAGE_PROMPT_TYXRW);
		if(tyxrw > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_TYXRW);
			str +="有" + tyxrw + "条未读"+msgType +"\r\n";
		}
		int shxgyj = getMegCount(StaticData.MESSAGE_PROMPT_SHXGYJ);
		if(shxgyj > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_SHXGYJ);
			str +="有" + shxgyj + "条未读"+msgType +"\r\n";
		}
		int hidden = getMegCount(StaticData.MESSAGE_PROMPT_HIDDEN);
		if(hidden > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_HIDDEN);
			str +="有" + hidden + "条未读"+msgType +"\r\n";
		}
		int jtxcsq = getMegCount(StaticData.MESSAGE_PROMPT_JTXCSQ);
		if(jtxcsq > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_JTXCSQ);
			str +="有" + jtxcsq + "条未读"+msgType +"\r\n";
		}
		int jtxcbty = getMegCount(StaticData.MESSAGE_PROMPT_JTXCBTY);
		if(jtxcbty > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_JTXCBTY);
			str +="有" + jtxcbty + "条未读"+msgType +"\r\n";
		}
		int snxcsq = getMegCount(StaticData.MESSAGE_PROMPT_SNXCSQ);
		if(snxcsq > 0){
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_SNXCSQ);
			str +="有" + snxcsq + "条未读"+msgType +"\r\n";
		}
		int cdtz = getMegCount(StaticData.MESSAGE_PROMPT_CDTZ);
		if (cdtz > 0) {
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_CDTZ);
			str += "有" + cdtz + "条未读" + msgType + "\r\n";
		}
		int scfjtz = getMegCount(StaticData.MESSAGE_PROMPT_SCFJTZ);
		if (scfjtz > 0) {
			msgType = pubFunc.getStaticName(StaticData.MESSAGE_PROMPT_SCFJTZ);
			str += "有" + scfjtz + "条未读" + msgType + "\r\n";
		}
		return str;
	}
	/**
	 * 更新当前员工已读标示
	 * @param 
	 * @return
	 */
	public int updateReadedFlagByStaff() {
		int ret=0;
		TsmStaff staff = pubFunc.getLogonStaff();		
		ret = msgDao.updateReadFlagByStaff(new Integer(staff.getId())) ;
		return ret;
	}
	
	public int updateReadedFlagByGuid(String guid) {
		int ret=0;
		ret = msgDao.updateReadedFlagByGuid(guid);
		return ret;
	}
	
	/**
	 * 根据GUID进历史
	 * @param 
	 * @return
	 */
	public int gotoHisById(String guid) {
		return msgDao.gotoHisById(guid) ;
	}
	/**
	 * 更新已读标示
	 * @param guid
	 * @return
	 */
	public int updateReadedFlag(String guid) {
		int ret=0;
		Calendar cal  = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		String mDateTime=formatter.format(cal.getTime());		
		MessagePrompt mp=msgDao.getMessageByGuid(guid);
		mp.setReaded(1);
		mp.setReadDate(mDateTime);
		ret=msgDao.updateReadFlag(mp);
		return ret;
	}

	@Override
	public int savePersonRemind(TipsPojo t) {
		return msgDao.savePersonRemind(t);
	}
	
	@Override
	public List getPersonRemind(int staffId) {
		TipsPojo t = new TipsPojo();
		t.setStaffId(staffId);
		return msgDao.getPersonRemind(t);
	}

	@Override
	public int saveBusinessRemind(TipsPojo t,String orgId,String selecteOrg) {
		List<Map<String, Object>> upOrgList = msgDao.getUpOrg();//查询能提醒全省的重点部门列表
		t.setDisplay(1);//提醒默认展示
		t.setLinkOrg(orgId);//默认发布本地
		//如果是10  提醒到全省
		if("10".contentEquals(selecteOrg)) {
			for(int i=0;i<upOrgList.size();i++) {
				String linkid=upOrgList.get(i).get("LINKID").toString();
				if(linkid.contentEquals(orgId)) {//员工的linkid前三位如果和重点单位likdid相同，就通过
					t.setLinkOrg(selecteOrg);
					break;
				}
			}
		}
		return msgDao.saveBusinessRemind(t);
	}

	@Override
	public List getBusinessRemind(String linkOrg) {
		return msgDao.getBusinessRemind(linkOrg);
	}

	@Override
	public int removeRemind(String remindId, String flag) {
		return msgDao.removeRemind(remindId, flag);
	}
}
