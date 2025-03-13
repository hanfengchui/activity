/*
 * 2011-08-25 LiJiahui新增该实现类
 */
package com.timesontransfar.staffSkill.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.staffSkill.StaffWorkloadInfo;
import com.timesontransfar.staffSkill.StaffWorkloadInfoRmp;
import com.timesontransfar.staffSkill.dao.IStaffWorkloadDao;

/**
 * 提供表 CC_STAFF_WORKLOAD操作方法的具体实现
 * 
 * @author LiJiahui
 * 
 */
@Component(value="staffWorkloadDao")
public class StaffWorkloadDaoImpl implements IStaffWorkloadDao {
    /**
     * 日志实例
     */
    private static final Logger log = LoggerFactory.getLogger(StaffWorkloadDaoImpl.class);

    /**
     * 更新一条数据的SQL
     */
    private String updateRowSql=
    		"UPDATE CC_STAFF_WORKLOAD SW " +
    		"SET SW.CUR_WORKLOAD = ?, SW.CUR_RATE = ?, SW.STATE = ? " + 
    		"WHERE SW.GUID = ? AND SW.STATE = 0";

    /**
     * 数据库操作实例，由Spring注入
     */
    @Autowired
    private JdbcTemplate jt;

    /**
     * 删除员工工班时删除该表记录的语句 <br>
     */
    private String delSql="UPDATE CC_STAFF_WORKLOAD SW SET SW.STATE = 1 WHERE SW.Ws_Id = ? AND SW.STATE != '1'";

    @SuppressWarnings("unchecked")
	public StaffWorkloadInfo queryBySWSId(String swsId) {
        String query = "SELECT * FROM CC_STAFF_WORKLOAD W WHERE W.WS_ID = ?";
        return (StaffWorkloadInfo) jt.queryForObject(query, new Object[]{swsId}, new StaffWorkloadInfoRmp());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public StaffWorkloadInfo queryByWhere(String where) {
    	List list = jt.query("SELECT * FROM CC_STAFF_WORKLOAD WHERE 1 = 1" + where, new StaffWorkloadInfoRmp());
    	if(list.isEmpty()) {
    		return null;
    	}
    	return (StaffWorkloadInfo)list.get(0);
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public StaffWorkloadInfo selectStaffWorkloadInfoAfterByStaffId(int staffId) {
		String sql = "SELECT*FROM cc_staff_workload WHERE threshold>0 AND NOW()<start_moment AND state<>1 AND staff_id=? ORDER BY start_moment";
		List list = jt.query(sql, new Object[] { staffId }, new StaffWorkloadInfoRmp());
		if (list.isEmpty()) {
			return null;
		}
		return (StaffWorkloadInfo) list.get(0);
	}

    /**
     * 修改一条记录
     * 
     * @param bean
     *            一条数据记录的封装对象
     * 
     * @return 被更新的数据记录数
     */
    public int updateStaffWorkload(StaffWorkloadInfo bean) {
    	try {
	        Object[] params = new Object[] {bean.getCurWorkload(),
	                String.valueOf(bean.getCurRate()), bean.getState(), bean.getGuid()};
	        return jt.update(updateRowSql, params);
    	}
    	catch(Exception e) {
    		log.error("updateStaffWorkload bean:{}", bean);
    	}
    	return 0;
    }

    @SuppressWarnings("rawtypes")
	public int saveBatch(final List workLoads) {
        String save = "INSERT INTO CC_STAFF_WORKLOAD" +
        		" (GUID, STAFF_ID, WS_ID, SKILL_LEVEL, START_MOMENT, END_MOMENT, THRESHOLD, STATE, CUR_RATE, CUR_WORKLOAD)" +
        		" VALUES" +
        		"(?, ?, ?, ?, STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), ?, 0, 0, 0)";
  
        int[] l = this.jt.batchUpdate(save, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return workLoads.size();
            }

            public void setValues(PreparedStatement ps, int j) throws SQLException {
            	ps.setString(1, ((StaffWorkloadInfo) workLoads.get(j)).getGuid());
                ps.setInt(2, ((StaffWorkloadInfo) workLoads.get(j)).getStaffId());
                ps.setString(3, ((StaffWorkloadInfo) workLoads.get(j)).getWsId());
                ps.setInt(4, ((StaffWorkloadInfo) workLoads.get(j)).getSkillLevel());
                ps.setString(5, ((StaffWorkloadInfo) workLoads.get(j)).getStartMoment());
                ps.setString(6, ((StaffWorkloadInfo) workLoads.get(j)).getEndMoment());
                ps.setInt(7, ((StaffWorkloadInfo) workLoads.get(j)).getThreshold());
            }
        });
        if (log.isDebugEnabled()) {
            log.debug("导入员工工作班次" + l.length + "条");
        }
        return l.length;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List getAllListBySql(String sql) {
        return this.jt.query(sql, new StaffWorkloadInfoRmp());
    }

    @SuppressWarnings("rawtypes")
	public void updateAbility(final List list) {
        String update = "UPDATE CC_STAFF_WORKLOAD W SET W.SKILL_LEVEL = ?,"
                + " W.THRESHOLD = ?, W.CUR_RATE = ? WHERE W.GUID = ?";
        int[] i = this.jt.batchUpdate(update, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return list.size();
            }

            public void setValues(PreparedStatement ps, int j) throws SQLException {
                ps.setInt(1, ((StaffWorkloadInfo) list.get(j)).getSkillLevel());
                ps.setInt(2, ((StaffWorkloadInfo) list.get(j)).getThreshold());
                ps.setDouble(3, ((StaffWorkloadInfo) list.get(j)).getCurRate());
                ps.setString(4, ((StaffWorkloadInfo) list.get(j)).getGuid());
            }
        });
        if (log.isDebugEnabled()) {
            log.debug("修改员工工班阀值信息" + i.length + "条");
        }
    }

    public void deleteByStaffAbility(int staffId) {
        String sql = "UPDATE CC_STAFF_WORKLOAD A SET A.STATE=1 WHERE A.STAFF_ID=" + staffId;
        int i = this.jt.update(sql);
        if (log.isDebugEnabled()) {
            log.debug("删除员工工班阀值信息" + i + "条");
        }
    }

    @SuppressWarnings("rawtypes")
	public int deleteBatchStaffShift(final List guids) {
        int[] i = this.jt.batchUpdate(this.delSql, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                ps.setString(1, (String) guids.get(j));
            }

            public int getBatchSize() {
                return guids.size();
            }
        });
        return i.length;
    }

    public int deleteStaffShift(String guid) {
        return this.jt.update(delSql, guid);
    }

    @SuppressWarnings("rawtypes")
	public int updateStaffWorkShift(final List workloads) {
        String update = "UPDATE CC_STAFF_WORKLOAD W SET W.START_MOMENT = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), " +
        		"W.END_MOMENT = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), W.THRESHOLD = ?, W.CUR_RATE = ? WHERE W.GUID = ?";
        
        int[] i = this.jt.batchUpdate(update, new BatchPreparedStatementSetter() {
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                ps.setString(1, ((StaffWorkloadInfo)workloads.get(j)).getStartMoment());
                ps.setString(2, ((StaffWorkloadInfo)workloads.get(j)).getEndMoment());
                ps.setInt(3, ((StaffWorkloadInfo)workloads.get(j)).getThreshold());
                ps.setDouble(4, ((StaffWorkloadInfo)workloads.get(j)).getCurRate());
                ps.setString(5, ((StaffWorkloadInfo)workloads.get(j)).getGuid());
            }

            public int getBatchSize() {
                return workloads.size();
            }
        });
        return i.length;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public List queryByWSId(String wsId) {
        String query = "SELECT * FROM CC_STAFF_WORKLOAD W WHERE W.WS_ID IN" +
        		" (SELECT S.SWS_ID FROM CC_STAFF_WORK_SHIFT S WHERE S.SWS_WORK_SHIFT_ID = ? AND S.SWS_USEABLE != '1')";
        Object[] params = {wsId};
        return jt.query(query, params, new StaffWorkloadInfoRmp());
    }

}
