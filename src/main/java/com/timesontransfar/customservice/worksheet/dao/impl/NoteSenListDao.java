/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.timesontransfar.customservice.worksheet.pojo.ReturnBlackList;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.alibaba.fastjson.JSON;
import com.timesontransfar.customservice.worksheet.dao.InoteSenList;
import com.timesontransfar.customservice.worksheet.pojo.NoteSeand;
import com.timesontransfar.customservice.worksheet.pojo.NoteSendList;
import com.timesontransfar.feign.custominterface.PortalInterfaceFeign;

/**
 * @author 万荣伟
 *
 */
public class NoteSenListDao implements InoteSenList {
	
	private static final Logger log = LoggerFactory.getLogger(NoteSenListDao.class);
	
	@Autowired
	private JdbcTemplate jt;
    
    @Autowired
	private PortalInterfaceFeign portalFeign;
    
	private String saveNoteListSql;//保存短信列表
	private String updateNoteListSql;//更新联系信息
	
	/**
	 * 得到短信发送号码
	 * @param orgId 部门id 
	 * @param tachId 环节id
	 * @param relaId 联系人ID
	 * @param queryType 0为部门工单池,1为我的任务
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public List getNoteSendNum(String orgId, String relaId, int tachId, int queryType) {
		//部门
		String orgSql = "SELECT * FROM CC_NOTE_SEND_LIST A WHERE A.ORG_ID=? AND A.TACH_ID=? " +
				"AND DATE_FORMAT(NOW(), '%H%i%s')>A.START_TIME AND DATE_FORMAT(NOW(), '%H%i%s')<A.END_TIME";
		//个人
		String staffSql = "SELECT * FROM CC_NOTE_SEND_LIST A WHERE (A.TACH_ID=? AND A.RELA_ID=? AND A.ROLE_TYPE=0) "
				+ "OR (A.ROLE_TYPE=1 AND A.ORG_ID=? "
				+ "AND DATE_FORMAT(NOW(), '%H%i%s')>A.START_TIME AND DATE_FORMAT(NOW(), '%H%i%s')<A.END_TIME)";
		List tmp = null;
		if(queryType == 0) {
			tmp = this.jt.queryForList(orgSql, orgId, tachId);
		} else {
			tmp = this.jt.queryForList(staffSql, tachId, relaId, orgId);
		}
		return tmp;
	}

	public int saveNoteContent(NoteSeand bean) {
		int size = 0;
		try {
			String json = JSON.toJSONString(bean);
			size = Integer.parseInt(portalFeign.saveNoteContent(json));
			log.info("保存短信发送内容数为: {}", size);
		} catch (Exception e) {
			log.error("saveNoteContent error: {}", e.getMessage(), e);
		}
		return size;
	}

	public int saveNoteSendList(NoteSendList bean) {
		String strSql = this.saveNoteListSql;
		int size = this.jt.update(strSql,
				bean.getNoteGuid(),
				StringUtils.defaultIfEmpty(bean.getOrgId(),null),
				StringUtils.defaultIfEmpty(bean.getOrgName(),null),
				StringUtils.defaultIfEmpty(bean.getRelaPerson(),null),
				StringUtils.defaultIfEmpty(bean.getRelaPhone(),null),
				bean.getTachId(),
				bean.getClientType(),
				bean.getRelaId(),
				bean.getRoleType(),
				bean.getStartTime(),
				bean.getEndTime()
		);
		if(log.isDebugEnabled()) {
			log.debug("保存短信联系列表数为：{}", size);
		}		
		return size;
	}

	public int updateNoteList(NoteSendList bean) {
		String strSql = this.updateNoteListSql;
		int size = this.jt.update(strSql,
				bean.getNoteGuid(),
				StringUtils.defaultIfEmpty(bean.getOrgId(),null),
				StringUtils.defaultIfEmpty(bean.getOrgName(),null),
				StringUtils.defaultIfEmpty(bean.getRelaPerson(),null),
				StringUtils.defaultIfEmpty(bean.getRelaPhone(),null),
				bean.getTachId(),
				bean.getClientType(),
				bean.getRelaId(),
				bean.getRoleType(),
				bean.getStartTime(),
				bean.getEndTime(),
				bean.getNoteGuid()
		);
		if(log.isDebugEnabled()) {
			log.debug("更新短信联系列表数为：{}", size);
		}		
		return size;
	}

	@Override
	public int updateReturnList(ReturnBlackList bean) {
		String strSql = "UPDATE cc_return_blacklist SET MOBILE_PHONE = ?,MODIFIED_TIME = NOW(), REGION_NAME = ?, REMARK = ?, STAFF_ID = ?, STAFF_NAME = ?,STATE = ?,ORG_NAME = ?,ORG_ID = ?" +
				" where ID = ?";
		int size = this.jt.update(strSql,
				bean.getMobilePhone(),
				bean.getRegionId(),
				bean.getRemark(),
				bean.getStaffId(),
				bean.getStaffName(),
				bean.getState(),
				bean.getOrgName(),
				bean.getOrgId(),
				bean.getId());
		if(log.isDebugEnabled()) {
			log.debug("更新回访黑名单列表数为：{}", size);
		}
		return size;
	}
	@Override
	public int saveReturnList(ReturnBlackList bean) {
		String strSql = "INSERT INTO cc_return_blacklist (MOBILE_PHONE,STATE,MODIFIED_TIME,REGION_NAME,REMARK,STAFF_ID,STAFF_NAME,ORG_NAME,ORG_ID) " +
				"VALUES (?,'1',NOW(),?,?,?,?,?,?)";
		int size = this.jt.update(strSql,
				bean.getMobilePhone(),
				bean.getRegionId(),
				bean.getRemark(),
				bean.getStaffId(),
				bean.getStaffName(),
				bean.getOrgName(),
				bean.getOrgId());
		if(log.isDebugEnabled()) {
			log.debug("新增回访黑名单列表数为：{}", size);
		}
		return size;
	}

	@Override
	public int getReturnExist(ReturnBlackList bean) {
		String sql = "select count(1) from cc_return_blacklist r where r.MOBILE_PHONE = ? ";
		int num = this.jt.queryForObject(sql,new Object[] { bean.getMobilePhone() }, Integer.class);
		return num;
	}

	/**
	 * 根据主健删除短信列表
	 * @param guid
	 * @return
	 */
	public int deleteNoteList(String guid) {
		String strSql = "DELETE FROM CC_NOTE_SEND_LIST WHERE NOTE_GUID=?";
    	return this.jt.update(strSql, guid);
	}
	/**
	 * 得到号码信息 1本省电信固话;2本省电信手机;4外省电信移动手机;6外省或其它
	 * @param sourcenum 
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Map getNumInfo(String sourcenum) {
		log.info("getNumInfo sourcenum：{}", sourcenum);
		String sourceNum = "";
		int proviceFlag = 1; //0-省内；1-省外
		String outNumtype = "6"; //1本省电信固话；2本省电信手机；3本省电信小灵通；4外省电信手机；6外省或其它
		String outAreacode = "000"; //样例025，0511
		String outSourcenum = sourcenum.substring(2); //手机去0，固话去区号
		String outRegionid = "000";
		try {
			if (sourcenum.length() < 4) {
				return this.getNumMap(outNumtype, outAreacode, outSourcenum, outRegionid);
			}
			
			/*前提条件固话加区号，手机加0。手机2、3位大于10，小于20：去除北京010的判断*/
		    /*手机客户根据H码判断是否本省用户；固话手机根据区号判断是否本省用*/
		    /*针对目前本地手机未加前缀0的处理*/
		    if(!"0".equals(sourcenum.substring(0, 1))) {
		    	sourceNum = "0" + sourcenum;
		    }
		    else {
		    	sourceNum = sourcenum;
		    }
		    
		    if(StringUtils.isNumeric(sourceNum)) {
		    	if(Integer.parseInt(sourceNum.substring(1, 3)) >= 10 && Integer.parseInt(sourceNum.substring(1, 3)) <= 19) {
		    		//处理国内手机号码
		    		Map map = portalFeign.qryHcodeOfNation(sourceNum.substring(1, 8));
		    		if(map != null) {
		    			outNumtype = this.getMapValue(map, "out_numtype");
		    			outAreacode = this.getMapValue(map, "out_areacode");
		    			outSourcenum = sourceNum.substring(1);
		    			outRegionid = this.getMapValue(map, "out_regionid");
		    			proviceFlag = Integer.parseInt(StringUtils.defaultIfBlank(this.getMapValue(map, "ln_provice_flag"), "1"));
		    		}
		    	}
		    	else {
		    		//处理固话小灵通 区号号头取7位
		    		//固话小灵通 暂做固话处理
		    		outNumtype = "1";
		    	}
		    	
		    	if(StringUtils.defaultIfEmpty(outRegionid, "N").equals("N") && Integer.parseInt(outNumtype) > 3 && proviceFlag == 0) {
		    		String sql = "SELECT region_id as out_regionid FROM trm_region WHERE region_telno = ? AND region_level = '97C' limit 1";
		    		List tmp = this.jt.queryForList(sql, outAreacode);
		    		if(!tmp.isEmpty()) {
		    			Map map = (Map)tmp.get(0);
		    			outRegionid = map.get("out_regionid").toString();
		    		}
		    	}
		    	if(proviceFlag == 1) {
		    		outRegionid = "000";
		    	}
		    }
		}
		catch(Exception e) {
			log.error("getNumInfo 异常：{}", e.getMessage(), e);
		}
		return this.getNumMap(outNumtype, outAreacode, outSourcenum, outRegionid);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Map getNumMap(String outNumtype, String outAreacode, String outSourcenum, String outRegionid) {
		Map map = new HashMap<String, String>();
		map.put("out_numtype", outNumtype);
		map.put("out_areacode", outAreacode);
		map.put("out_sourcenum", outSourcenum);
		map.put("out_regionid", outRegionid);
		log.info("getNumInfo result: {}", map.toString());
		return map;
	}
	
	@SuppressWarnings("rawtypes")
	private String getMapValue(Map map, String key) {
		return map.get(key) == null ? "" : map.get(key).toString();
	}

	public String getSaveNoteListSql() {
		return saveNoteListSql;
	}

	public void setSaveNoteListSql(String saveNoteListSql) {
		this.saveNoteListSql = saveNoteListSql;
	}

	public String getUpdateNoteListSql() {
		return updateNoteListSql;
	}

	public void setUpdateNoteListSql(String updateNoteListSql) {
		this.updateNoteListSql = updateNoteListSql;
	}
	
}
