package com.timesontransfar.customservice.worksheet.dao.impl;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

import com.timesontransfar.customservice.worksheet.dao.ISheetTodispatchDao;
import com.timesontransfar.customservice.worksheet.pojo.SheetTodispatch;
import com.timesontransfar.customservice.worksheet.pojo.SheetTodispatchRmp;

public class SheetTodispatchDaoImpl implements ISheetTodispatchDao {
	
	private JdbcTemplate jt;
	
	private String saveObjSql;
	private String saveObjHisSql;
	private String deleteObjSql;
	private String queryLatestSql;
	private String queryLatestTSSql;

	@Override
	public int saveObj(SheetTodispatch obj) {
		return jt.update(saveObjSql, obj.getWorkSheetId(), obj.getServiceOrderId(), obj.getToOrgId(), obj.getReason());
	}

	@Override
	public int saveObjHis(String serviceOrderId) {
		return jt.update(saveObjHisSql, serviceOrderId);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SheetTodispatch queryLatest(String serviceOrderId) {
		List<SheetTodispatch> lst = jt.query(queryLatestSql, new Object[] { serviceOrderId }, new SheetTodispatchRmp());
		if (lst.isEmpty()) {
			return null;
		}
		return lst.get(0);
	}

	@Override
	public int delete(String serviceOrderId) {
		return jt.update(deleteObjSql, serviceOrderId);
	}

	@Override
	public int queryCountObj(String workSheetId, String serviceOrderId) {
		String sql = "SELECT COUNT(1) FROM cc_sheet_todispatch t WHERE t.work_sheet_id = '" + workSheetId + "' AND t.service_order_id = '" + serviceOrderId
				+ "'";
		return jt.queryForObject(sql,Integer.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SheetTodispatch queryLatestTS(String workSheetId) {
		List<SheetTodispatch> lst = jt.query(queryLatestTSSql, new Object[] { workSheetId }, new SheetTodispatchRmp());
		if (lst.isEmpty()) {
			return null;
		}
		return lst.get(0);
	}

	public void setJt(JdbcTemplate jt) {
		this.jt = jt;
	}

	public void setSaveObjSql(String saveObjSql) {
		this.saveObjSql = saveObjSql;
	}

	public void setSaveObjHisSql(String saveObjHisSql) {
		this.saveObjHisSql = saveObjHisSql;
	}

	public void setDeleteObjSql(String deleteObjSql) {
		this.deleteObjSql = deleteObjSql;
	}

	public void setQueryLatestSql(String queryLatestSql) {
		this.queryLatestSql = queryLatestSql;
	}

	public void setQueryLatestTSSql(String queryLatestTSSql) {
		this.queryLatestTSSql = queryLatestTSSql;
	}
}
