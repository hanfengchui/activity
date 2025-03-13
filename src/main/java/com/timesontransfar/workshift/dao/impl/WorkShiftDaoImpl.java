package com.timesontransfar.workshift.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.impl.SystemAuthorizationWas;
import com.timesontransfar.workshift.dao.IWorkShiftDao;
import com.timesontransfar.workshift.pojo.WorkShift;
import com.timesontransfar.workshift.pojo.WorkShiftRmp;

@Component(value="workShiftDao")
public class WorkShiftDaoImpl implements IWorkShiftDao {
	private static final Logger log = LoggerFactory.getLogger(WorkShiftDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jt;
	
	@Autowired
	private SystemAuthorizationWas systemAuthorization;
	
	String addWorkShiftSql = "INSERT INTO CC_WORK_SHIFT(WS_ID,WS_NAME,WS_TIME,WS_PERCENT,WS_DESC,WS_CREATE_STAFF_ID,WS_CREATE_TIME,WS_USEABLE,WS_CREATE_ORG_ID,WS_CREATE_ORG_NAME,WS_CREATE_LOGONNAME) VALUES(?,?,?,?,?,?,NOW(),?,?,?,?)";
	String modifyWorkShiftSql = "UPDATE CC_WORK_SHIFT E SET E.WS_NAME=?,E.WS_TIME=?,E.WS_PERCENT=?,E.WS_DESC=?, E.WS_CREATE_STAFF_ID=?,E.WS_CREATE_TIME=NOW(),E.WS_USEABLE=?,E.WS_CREATE_ORG_ID=?,E.WS_CREATE_ORG_NAME=?,E.WS_CREATE_LOGONNAME=? WHERE E.WS_ID=? ";
	
	String addWorkShiftSqlO = "INSERT INTO CC_WORK_SHIFT(WS_ID,WS_NAME,WS_TIME,WS_PERCENT,WS_DESC,WS_CREATE_STAFF_ID,WS_CREATE_TIME,WS_USEABLE,WS_CREATE_ORG_ID,WS_CREATE_ORG_NAME,WS_CREATE_LOGONNAME) VALUES(?,?,?,?,?,?,SYSDATE,?,?,?,?)";
	String modifyWorkShiftSqlO = "UPDATE CC_WORK_SHIFT E SET E.WS_NAME=?,E.WS_TIME=?,E.WS_PERCENT=?,E.WS_DESC=?, E.WS_CREATE_STAFF_ID=?,E.WS_CREATE_TIME=SYSDATE,E.WS_USEABLE=?,E.WS_CREATE_ORG_ID=?,E.WS_CREATE_ORG_NAME=?,E.WS_CREATE_LOGONNAME=? WHERE E.WS_ID=? ";

	public int addWorkShift(WorkShift workShift) {
		int size = this.jt.update(this.addWorkShiftSql, 
			workShift.getId(),
			workShift.getName(),
			workShift.getTime(),
			workShift.getPercent(),
			workShift.getDesc(),
			workShift.getCreateStaffId(),
			workShift.getUseable(),
			workShift.getCreateOrgId(),
			workShift.getCreateOrgName(),
			workShift.getCreateLogonname()
		);
		if(log.isDebugEnabled()) {
			log.debug("新增工作班次"+size+"条");
		}
		return size;
	}

	public int modifyWorkShift(WorkShift workShift) {
		int size = this.jt.update(this.modifyWorkShiftSql, 
				workShift.getName(),
				workShift.getTime(),
				workShift.getPercent(),
				workShift.getDesc(),
				workShift.getCreateStaffId(),
				workShift.getUseable(),
				workShift.getCreateOrgId(),
				workShift.getCreateOrgName(),
				workShift.getCreateLogonname(),
				workShift.getId()
		);
		if(log.isDebugEnabled()) {
			log.debug("修改工作班次"+size+"条");
		}
		return size;
	}

	@SuppressWarnings("unchecked")
	public WorkShift getWorkShiftById(String id) {
		String sql = "SELECT * FROM CC_WORK_SHIFT A WHERE A.WS_ID=? AND A.WS_USEABLE <> 1";
		return (WorkShift) this.jt.queryForObject(sql, new Object[]{id}, new WorkShiftRmp());
	}

	@SuppressWarnings("unchecked")
	public WorkShift getWorkShiftByName(String name) {
		String sql = "SELECT * FROM CC_WORK_SHIFT A WHERE A.WS_USEABLE <> 1 AND A.WS_NAME=? LIMIT 1";
		WorkShift shift = null;
		try{
		    shift = (WorkShift) this.jt.queryForObject(sql, new Object[]{name}, new WorkShiftRmp());
		}catch (Exception e) {
		    log.debug("Not found such WorkShift. WorkShift name = ["+name+"]",e);
        }
		return shift;
	}

	public int shiftNameExist(String id, String name) {
		String sql = "SELECT * FROM CC_WORK_SHIFT A WHERE A.WS_NAME=? AND A.WS_USEABLE <> 1";
		int size = 0;
		if(null==id||id.equals("")){
			size = this.jt.queryForList(sql, name).size();
		}else{
			sql += " AND A.WS_ID <> ?";
			size = this.jt.queryForList(sql, name, id).size();
		}
		
		return size;
	}

	@SuppressWarnings("rawtypes")
	public List getWorkShift() {
		String sql = "SELECT CC_WORK_SHIFT.WS_ID,\n" +
						"       CC_WORK_SHIFT.WS_NAME,\n" + 
						"       CC_WORK_SHIFT.WS_TIME,\n" + 
						"       CC_WORK_SHIFT.WS_PERCENT,\n" + 
						"       CC_WORK_SHIFT.WS_DESC,\n" + 
						"       CC_WORK_SHIFT.WS_CREATE_STAFF_ID,\n" + 
						"       CC_WORK_SHIFT.WS_CREATE_TIME,\n" + 
						"       CC_WORK_SHIFT.WS_USEABLE\n" + 
						"  FROM CC_WORK_SHIFT, TSM_ORGANIZATION B, TSM_ORGANIZATION\n" + 
						" WHERE CC_WORK_SHIFT.WS_USEABLE <> 1\n" + 
						"   AND CC_WORK_SHIFT.WS_CREATE_ORG_ID = B.ORG_ID\n" + 
						"   AND B.LINKID LIKE CONCAT(TSM_ORGANIZATION.LINKID , '%') ";
        String s1 = this.systemAuthorization.getAuthedSql(null, sql, "900018302");
		return this.jt.queryForList(s1);//CodeSec未验证的SQL注入；CodeSec误报：2
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List getWorkShiftByStaff(String loginName){
        String sql = "SELECT CC_WORK_SHIFT.WS_ID,\n" +
				"       CC_WORK_SHIFT.WS_NAME\n" + 
				"  FROM CC_WORK_SHIFT, TSM_ORGANIZATION B, TSM_ORGANIZATION\n" + 
				" WHERE CC_WORK_SHIFT.WS_USEABLE <> 1\n" + 
				"   AND CC_WORK_SHIFT.WS_CREATE_ORG_ID = B.ORG_ID\n" + 
				"   AND B.LINKID LIKE CONCAT(TSM_ORGANIZATION.LINKID , '%') ";
        sql = this.systemAuthorization.getAuthedSql(null, sql, "900018302", loginName);
        List workShiftList = this.jt.queryForList(sql);//CodeSec未验证的SQL注入；CodeSec误报：1
        List wlist=new ArrayList();
        for(int i=0;i<workShiftList.size();i++) {
			Map map = (Map)workShiftList.get(i);
			wlist.add(map.get("WS_ID").toString());
		}
		return wlist;
	}

}
