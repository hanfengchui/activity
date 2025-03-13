/**
 * @author 万荣伟
 */
package com.timesontransfar.staffSkill.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.staffSkill.StaffSkillInfo;
import com.timesontransfar.staffSkill.dao.StaffSkillDao;

/**
 * @author 万荣伟
 *
 */
public class StaffSkillDaoImpl implements StaffSkillDao {
	private static final Logger log = LoggerFactory.getLogger(StaffSkillDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jt;
	
	private String saveStaffSkillSql;
	
	private String updateStaffSkillSql;
	
	/**
	 * 根据GUID删除员工技能ID,设置为无效
	 * @param guid
	 * @return
	 */
	public int deleteStaffSkill(String guid) {
		String strSql = "UPDATE CC_STAFF_SKILL A SET A.SKILL_STATE=0 WHERE A.GUID=?";
		int size = this.jt.update(strSql, guid);
		if(log.isDebugEnabled()) {
			log.debug("更新员工专业技能"+size+"条");
		}
		return size;
	}
	
    public int saveStaffSkillBatch(final StaffSkillInfo[] beans) {
        if(null == beans || beans.length == 0){
            return 0;
        }
        // 覆盖旧数据
        jt.batchUpdate(updateStaffSkillSql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                ps.setInt(1, beans[j].getStaffId());
                ps.setString(2, StringUtils.defaultIfEmpty(beans[j].getFlowOrgId(),null));
                ps.setInt(3, beans[j].getTacheId());
                ps.setInt(4, beans[j].getSkillId());
                ps.setString(5, beans[j].getServiceDate());
            }
            public int getBatchSize() {
                return beans.length;
            }
        });
        
        // 保存新数据
        int[] n = jt.batchUpdate(saveStaffSkillSql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int j) throws SQLException {
            	ps.setString(1, beans[j].getGuid());
                ps.setInt(2, beans[j].getStaffId());
                ps.setString(3, beans[j].getStaffName());
                ps.setString(4, beans[j].getOrgId());
                ps.setString(5, beans[j].getOrgName());
                ps.setString(6, beans[j].getFlowOrgId());
                ps.setString(7, beans[j].getFlowOrgName());
                ps.setInt(8, beans[j].getSkillId());
                ps.setString(9, beans[j].getSkillName());
                ps.setInt(10, beans[j].getCreatStaff());
                ps.setInt(11, beans[j].getTacheId());
                ps.setInt(12, beans[j].getSkillState());
                ps.setString(13, beans[j].getServiceDate());
            }
            public int getBatchSize() {
                return beans.length;
            }
        });
        return n.length;
    }
    
    public int deleteStaffSkillBatch(final String[] guids) {
        if(null == guids || guids.length == 0){
            return 0;
        }
        String strSql = "UPDATE CC_STAFF_SKILL A SET A.SKILL_STATE=0 WHERE A.GUID=?";
        int[] n = jt.batchUpdate(strSql, new BatchPreparedStatementSetter() {
            
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                ps.setString(1, guids[j]);
            }
            
            public int getBatchSize() {
                return guids.length;
            }
        });
        return n.length;
    }
    
    public int deleteSkillsByFlowOrgId(String flowOrgId, String serviceDate){
    	String sql="update cc_staff_skill c set c.skill_state=0 where c.skill_state=1 and c.flow_org_id=? and c.service_date=?";
    	int size = this.jt.update(sql, flowOrgId, serviceDate);
		if(log.isDebugEnabled()) {
			log.debug("删除流向部门："+flowOrgId+"所属员工技能"+size+"条");
		}
		return size;
    	
    }
	
	/**
	 * @return jt
	 */
	public JdbcTemplate getJt() {
		return jt;
	}

	/**
	 * @param jt 要设置的 jt
	 */
	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}

	/**
	 * @return saveStaffSkillSql
	 */
	public String getSaveStaffSkillSql() {
		return saveStaffSkillSql;
	}

	/**
	 * @param saveStaffSkillSql 要设置的 saveStaffSkillSql
	 */
	public void setSaveStaffSkillSql(String saveStaffSkillSql) {
		this.saveStaffSkillSql = saveStaffSkillSql;
	}
	/**
	 * @return updateStaffSkillSql
	 */
	public String getUpdateStaffSkillSql() {
		return updateStaffSkillSql;
	}
	/**
	 * @param updateStaffSkillSql 要设置的 updateStaffSkillSql
	 */
	public void setUpdateStaffSkillSql(String updateStaffSkillSql) {
		this.updateStaffSkillSql = updateStaffSkillSql;
	}
}
