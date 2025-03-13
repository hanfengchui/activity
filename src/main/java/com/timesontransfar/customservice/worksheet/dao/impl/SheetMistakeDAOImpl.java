/*
 * 文 件 名：SheetMistakeDAOImpl.java
 * 版    权：
 * 描    述：
 * 修 改 人：Administrator
 * 修改时间：2013-3-12
 * 修改内容：新增
 */
package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.worksheet.dao.ISheetMistakeDAO;
import com.timesontransfar.customservice.worksheet.pojo.TSOrderMistake;
import com.timesontransfar.customservice.worksheet.pojo.TSOrderMistakeRmp;

/**
 * 操作表CC_SHEET_MISTAKE、CC_SHEET_MISTAKE_HIS的方法实现
 * 
 * @author LiJiahui
 * @version 1.0
 * @since 2013-3-12
 */
public class SheetMistakeDAOImpl implements ISheetMistakeDAO {
	@Autowired
    private JdbcTemplate jt;
	
    private String insertOrderMistakeSql;

    private String deleteOrderMistakeByOrderIdSql;

    private String selectOrderMistakeByOrderIdSql;

    private String insertOrderMistakeHisByOrderIdSql;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public int insertOrderMistake(TSOrderMistake omn) {
		List omos = jt.query(selectOrderMistakeByOrderIdSql,
				new Object[] { omn.getServiceOrderId(), omn.getMistakeType() }, new TSOrderMistakeRmp());
		if (!omos.isEmpty()) {
			TSOrderMistake omo = (TSOrderMistake) omos.get(0);
			omos.clear();
			omn.setMistakeOrgId(omo.getCheckOrgId());
			omn.setMistakeStaffId(omo.getCheckStaffId());
			omn.setOldInfo(omo.getNewInfo() == null ? "" : omo.getNewInfo());
		}
		if (omn.getOldInfo().equals(omn.getNewInfo())) {
			return 0;
		}
		return jt.update(insertOrderMistakeSql, omn.getServiceOrderId(), omn.getWorkSheetId(), omn.getMistakeOrgId(),
						omn.getMistakeStaffId(), omn.getMistakeType(), omn.getCheckOrgId(), omn.getCheckStaffId(),
						omn.getOldInfo(), omn.getNewInfo());
	}

	public int insertOrderMistakeHisByOrderId(String serviceOrderId) {
		if (jt.update(insertOrderMistakeHisByOrderIdSql, serviceOrderId) > 0) {
			return jt.update(deleteOrderMistakeByOrderIdSql, serviceOrderId);
		}
		return 0;
	}

    /**
     * 取得jt
     * 
     * @return 返回jt。
     */
    public JdbcTemplate getJt() {
        return jt;
    }

    /**
     * 设置jt
     * 
     * @param jt
     *            要设置的jt。
     */
    public void setJt(JdbcTemplate jt) {
        this.jt = jt;
    }

	public String getInsertOrderMistakeSql() {
		return insertOrderMistakeSql;
	}

	public void setInsertOrderMistakeSql(String insertOrderMistakeSql) {
		this.insertOrderMistakeSql = insertOrderMistakeSql;
	}

	public String getDeleteOrderMistakeByOrderIdSql() {
		return deleteOrderMistakeByOrderIdSql;
	}

	public void setDeleteOrderMistakeByOrderIdSql(String deleteOrderMistakeByOrderIdSql) {
		this.deleteOrderMistakeByOrderIdSql = deleteOrderMistakeByOrderIdSql;
	}

	public String getSelectOrderMistakeByOrderIdSql() {
		return selectOrderMistakeByOrderIdSql;
	}

	public void setSelectOrderMistakeByOrderIdSql(String selectOrderMistakeByOrderIdSql) {
		this.selectOrderMistakeByOrderIdSql = selectOrderMistakeByOrderIdSql;
	}

	public String getInsertOrderMistakeHisByOrderIdSql() {
		return insertOrderMistakeHisByOrderIdSql;
	}

	public void setInsertOrderMistakeHisByOrderIdSql(String insertOrderMistakeHisByOrderIdSql) {
		this.insertOrderMistakeHisByOrderIdSql = insertOrderMistakeHisByOrderIdSql;
	}
}