package com.timesontransfar.common.authorization.role.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.timesontransfar.common.authorization.model.TsmEntityPermit;
import com.timesontransfar.common.authorization.model.TsmFunction;
import com.timesontransfar.common.authorization.model.TsmPopupMenu;
import com.timesontransfar.common.authorization.web.webobject.WebMenuInfo;
import com.timesontransfar.common.framework.core.persist.AbstractRowMapper;

public class RoleRowMapper extends AbstractRowMapper {
	
	public Object mapRow(ResultSet rs, int index) throws SQLException {
		Object object = null;
		switch (this.getIntClassType()) {
			case 0:
				object = this.mappingParentMenuId(rs);
				break;
			case 1:
				object = this.mappingDataPermit(rs);
				break;
			case 2:
				object = this.mappingMenuIdByRoleId(rs);
				break;
			case 3:
				object = this.mappingPopPermit(rs);
				break;
			default:
				break;
		}
		return object;
	}

	public Object mappingParentMenuId(ResultSet rs) throws SQLException {
		TsmFunction func = new TsmFunction();
		func.setId(rs.getString(1));
		return func;
	}
	public Object mappingDataPermit(ResultSet rs) throws SQLException {
		TsmEntityPermit tEntity = new TsmEntityPermit();
		tEntity.setId(rs.getString("DATAOPERID"));
		if(rs.getString("ISPRIVATE")!=null&&rs.getString("ISPRIVATE").equals("0")){
			tEntity.setPrivate(true);
		} else{
			tEntity.setPrivate(false);
		}
		if(rs.getString("ORG_STATE")!=null){
			tEntity.setOrgState(Integer.parseInt(rs.getString("ORG_STATE")));
		}
		tEntity.setObjId(rs.getString("OBJ_ID"));
		if(rs.getString("STATE")!=null){
			tEntity.setState(Integer.parseInt(rs.getString("STATE")));
		}

		return tEntity;
	}
	
	public Object mappingPopPermit(ResultSet rs) throws SQLException {
		TsmPopupMenu tsmPopupMenu = new TsmPopupMenu();
		tsmPopupMenu.setMenuId(rs.getString(1));
		tsmPopupMenu.setMenuName(rs.getString(2));
		return tsmPopupMenu;
	}

	public Object mappingMenuIdByRoleId(ResultSet rs) throws SQLException {
		WebMenuInfo web = new WebMenuInfo();
		web.setMenuId(rs.getString(1));
		return web;
	}
}
