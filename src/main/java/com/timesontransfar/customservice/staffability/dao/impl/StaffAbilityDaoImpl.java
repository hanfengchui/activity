package com.timesontransfar.customservice.staffability.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.common.authorization.service.impl.SystemAuthorizationWas;
import com.timesontransfar.customservice.staffability.dao.IStaffAbilityDao;
import com.timesontransfar.customservice.staffability.pojo.StaffAbility;
import com.timesontransfar.customservice.staffability.pojo.StaffAbilityRmp;

@Component(value="staffAbilityDao")
public class StaffAbilityDaoImpl implements IStaffAbilityDao {
    private static final Logger log = LoggerFactory.getLogger(StaffAbilityDaoImpl.class);
    
    @Autowired
    private JdbcTemplate jt;
	
    @Autowired
    private SystemAuthorizationWas systemAuthorization;

    private String saveSql=
    		"INSERT INTO CC_STAFF_ABILITY\n" +
    				"  (GUID,STAFF_ID,STAFF_NAME,SKILL_LEVEL,THRESHOLD,CREATE_STAFF_ID,CREATE_TIME,SS_USEABLE)\n" + 
    				"VALUES\n" + 
    				"  (?, ?, ?, ?, ?, ?, NOW(), 0)";

    private String updateSql=
    		"UPDATE CC_STAFF_ABILITY E\n" +
    				"   SET E.SKILL_LEVEL     = ?,\n" + 
    				"       E.THRESHOLD       = ?,\n" + 
    				"       E.CREATE_STAFF_ID = ?,\n" + 
    				"       E.CREATE_TIME     = NOW()\n" + 
    				" WHERE E.GUID = ?";

    /**
     * 通过员工ID获取有效记录<br>
     * SELECT * FROM CC_STAFF_ABILITY A WHERE A.STAFF_ID = ? AND A.SS_USEABLE =
     * 0
     */
    private String queryByStaffId="SELECT * FROM CC_STAFF_ABILITY A WHERE A.STAFF_ID = ? AND A.SS_USEABLE = 0";


    @SuppressWarnings("rawtypes")
	public int saveStaffAbilityBatch(final List objects, final List modify) {
        int[] i = this.jt.batchUpdate(this.saveSql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return objects.size();
            }

            public void setValues(PreparedStatement ps, int j) throws SQLException {
            	ps.setString(1, ((StaffAbility) objects.get(j)).getGuid());
                ps.setInt(2, ((StaffAbility) objects.get(j)).getStaffId());
                ps.setString(3, ((StaffAbility) objects.get(j)).getStaffName());
                ps.setInt(4, ((StaffAbility) objects.get(j)).getSkillLevel());
                ps.setInt(5, ((StaffAbility) objects.get(j)).getThreshold());
                ps.setInt(6, ((StaffAbility) objects.get(j)).getCreateStaffId());
            }
        });
        int[] l = this.jt.batchUpdate(this.updateSql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return modify.size();
            }

            public void setValues(PreparedStatement ps, int k) throws SQLException {
                ps.setInt(1, ((StaffAbility) modify.get(k)).getSkillLevel());
                ps.setInt(2, ((StaffAbility) modify.get(k)).getThreshold());
                ps.setInt(3, ((StaffAbility) modify.get(k)).getCreateStaffId());
                ps.setString(4, ((StaffAbility) modify.get(k)).getGuid());
            }
        });
        log.info("导入员工技能熟练度{}条", i.length + l.length);
        return i.length + l.length;
    }

    public int updateStaffAbility(StaffAbility bean) {
        int size = this.jt.update(updateSql,
                bean.getSkillLevel(), bean.getThreshold(),
                        bean.getCreateStaffId(), bean.getGuid());
        if (log.isDebugEnabled()) {
            log.debug("修改员工技能熟练度" + size + "条");
        }
        return size;
    }

    public int deleteStaffAbility(String guid) {
        String sql = "UPDATE CC_STAFF_ABILITY A SET A.SS_USEABLE=1 WHERE A.GUID=?";
        int size = this.jt.update(sql, guid);
        if (log.isDebugEnabled()) {
            log.debug("删除员工技能熟练度" + size + "条");
        }
        return size;
    }

    @SuppressWarnings("unchecked")
	public StaffAbility getStaffAbilityById(String guid) {
        String sql = "SELECT * FROM CC_STAFF_ABILITY A WHERE A.GUID=?";
        return (StaffAbility) this.jt.queryForObject(sql, new Object[] {guid},
                new StaffAbilityRmp());
    }

    @SuppressWarnings("unchecked")
	public StaffAbility isStaffAbilityExists(int staffId) {
        String sql = "SELECT * FROM CC_STAFF_ABILITY A WHERE A.STAFF_ID=? AND A.SS_USEABLE=0";
        if (!(this.jt.queryForList(sql, staffId)).isEmpty()) {
            return (StaffAbility) this.jt.queryForObject(sql, new Object[] {staffId},
                    new StaffAbilityRmp());
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
	public StaffAbility queryByStaffId(int staffId) {
        StaffAbility ability = null;
        try {
            ability = (StaffAbility) this.jt.queryForObject(queryByStaffId,
                    new Object[] {staffId}, new StaffAbilityRmp());
        } catch (Exception e) {
            log.debug("Not found such staff's ability record. StaffId = [" + staffId + "]. ", e);
        }
        return ability;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List getOrgList(String loginName) {
        String sql = "SELECT B.ORG_ID FROM TSM_ORGANIZATION B,TSM_ORGANIZATION WHERE B.LINKID LIKE CONCAT(TSM_ORGANIZATION.LINKID , '%') ";
		sql = this.systemAuthorization.getAuthedSql(null, sql, "900018302", loginName);
        List orgList = this.jt.queryForList(sql);//CodeSec未验证的SQL注入；CodeSec误报：1
        List olist=new ArrayList();
        for(int i=0;i<orgList.size();i++) {
			Map map = (Map)orgList.get(i);
			olist.add(map.get("ORG_ID").toString());
		}
		return olist;
	}
	
}
