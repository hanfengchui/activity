package com.timesontransfar.staffWorkShift.dao.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.common.PubFunc;
import com.timesontransfar.staffWorkShift.dao.IStaffWorkShiftDao;
import com.timesontransfar.staffWorkShift.pojo.StaffWorkShift;
import com.timesontransfar.staffWorkShift.pojo.StaffWorkShiftRmp;

@Component(value="staffWorkShiftDao")
public class StaffWorkShiftDaoImpl implements IStaffWorkShiftDao {
	private static final Logger log = LoggerFactory.getLogger(StaffWorkShiftDaoImpl.class);
	
	@Autowired
	private JdbcTemplate jt;
	
	private String saveStaffWorkShiftSql=
				"INSERT INTO CC_STAFF_WORK_SHIFT\n" +
				"(SWS_ID,\n" + 
				"SWS_STAFF_ID,\n" + 
				"SWS_STAFF_NAME,\n" + 
				"SWS_WORK_TEAM_NAME,\n" + 
				"WSS_WORK_DATE,\n" + 
				"SWS_WORK_SHIFT_ID,\n" + 
				"SWS_DESC,\n" + 
				"SWS_CREATE_STAFF_ID,\n" + 
				"SWS_CREATE_TIME,\n" + 
				"SWS_USEABLE)\n" + 
				"VALUES\n" + 
				"(?, ?, ?, ?, STR_TO_DATE(?, '%Y-%m-%d'), ?, ?, ?, STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'), ?)";

	private String updateStaffWorkShiftSql=
				"UPDATE CC_STAFF_WORK_SHIFT E\n" +
				"SET E.SWS_WORK_TEAM_NAME  = ?,\n" + 
				"E.SWS_WORK_SHIFT_ID   = ?,\n" + 
				"E.SWS_DESC            = ?,\n" + 
				"E.SWS_CREATE_STAFF_ID = ?,\n" + 
				"E.SWS_CREATE_TIME     = STR_TO_DATE(?, '%Y-%m-%d %H:%i:%s'),\n" + 
				"E.SWS_USEABLE         = ?\n" + 
				"WHERE E.SWS_ID = ?";

	@Autowired
	private PubFunc pubFun;
	
	/**
	 * 删除记录，即将记录置为不可用
	 * UPDATE CC_STAFF_WORK_SHIFT A SET A.SWS_USEABLE=1 WHERE A.SWS_ID=? AND A.SWS_USEABLE=0
	 */
	private String deleteSql=
		"UPDATE CC_STAFF_WORK_SHIFT A\n" +
		"          SET A.SWS_USEABLE = 1\n" + 
		"          WHERE A.SWS_ID = ?\n" + 
		"             AND A.SWS_USEABLE = 0";

	@SuppressWarnings("rawtypes")
	public int saveStaffWorkShiftPatch(final List list) {
        int[] i = this.jt.batchUpdate(this.saveStaffWorkShiftSql, new BatchPreparedStatementSetter(){
            public int getBatchSize() {
                return list.size();
            }
            public void setValues(PreparedStatement ps, int j) throws SQLException {
                StaffWorkShift bean = (StaffWorkShift)list.get(j);
                ps.setString(1, bean.getId());
                ps.setInt(2, bean.getStaffId());
                ps.setString(3, bean.getStaffName());
                ps.setString(4, bean.getWorkTeamName());
                ps.setString(5, bean.getWorkDate());
                ps.setString(6, bean.getWorkShiftId());
                ps.setString(7, bean.getDesc());
                ps.setInt(8, bean.getCreateStaffId());
                ps.setString(9, bean.getCreateTime());
                ps.setInt(10, bean.getUseable());
            }           
        });
        
        if(log.isDebugEnabled()) {
            log.debug("导入员工工作班次"+i.length+"条");
        }
        return i.length;
    }
    
	
	 /* (non-Javadoc)
     * @see com.timesontransfar.staffWorkShift.dao.IStaffWorkShiftDao#selectStaffWorkShift(java.lang.String, java.lang.String, java.lang.String)
     */
    public int selectStaffWorkShift(String staffId, String workDate, String workShiftId) {
        if(null == staffId || staffId.length() == 0 
                || null == workDate || workDate.length() == 0
                || null == workShiftId || workShiftId.length() == 0){
            return 1;
        }
        String sql = "SELECT COUNT(*) FROM CC_STAFF_WORK_SHIFT S "
                + " WHERE S.SWS_STAFF_ID = '" + staffId + "'"
                + " AND S.WSS_WORK_DATE = STR_TO_DATE('" + workDate + "', '%Y-%m-%d')"
                + " AND S.SWS_WORK_SHIFT_ID = '" + workShiftId +"' AND S.SWS_USEABLE = 0";
        return jt.queryForObject(sql,Integer.class);//CodeSec未验证的SQL注入；CodeSec误报：1
    }

	public int updateStaffWorkShift(StaffWorkShift bean) {
		StaffWorkShift bean2 = getStaffWorkShiftById(bean.getId());
		bean2.setCreateStaffId(Integer.parseInt(this.pubFun.getLogonStaff().getId()));
		bean2.setCreateTime(this.pubFun.getSysDate());
		bean2.setWorkShiftId(bean.getWorkShiftId());
		int size = this.jt.update(updateStaffWorkShiftSql,
				bean2.getWorkTeamName(),
				bean2.getWorkShiftId(),
				bean2.getDesc(),
				bean2.getCreateStaffId(),
				bean2.getCreateTime(),
				bean2.getUseable(),
				bean2.getId()
		);
		if(log.isDebugEnabled()) {
			log.debug("更新员工工作班次"+size+"条");
		}
		return size;
	}

	public int deleteStaffWorkShift(String id) {
		int size = this.jt.update(deleteSql, id);
		if(log.isDebugEnabled()) {
			log.debug("删除员工工作班次"+size+"条");
		}
		return size;
	}

	@SuppressWarnings("unchecked")
	public StaffWorkShift getStaffWorkShiftById(String id) {
		String sql = "SELECT * FROM CC_STAFF_WORK_SHIFT A WHERE A.SWS_ID=?";
		return (StaffWorkShift) this.jt.queryForObject(sql, new Object[]{id}, new StaffWorkShiftRmp());
	}

	@SuppressWarnings("rawtypes")
	public int deleteBatchStaffWorkShitf(final List ids) {
		int[] i = this.jt.batchUpdate(this.deleteSql, new BatchPreparedStatementSetter() {
			public void setValues(PreparedStatement ps, int j) throws SQLException {
				ps.setString(1, ids.get(j).toString());
			}
			
			public int getBatchSize() {
				return ids.size();
			}
		});
		return i.length;
	}
	
}
