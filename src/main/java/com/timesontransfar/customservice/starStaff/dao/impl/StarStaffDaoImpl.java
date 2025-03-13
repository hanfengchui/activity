package com.timesontransfar.customservice.starStaff.dao.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.starStaff.dao.IStarStaffDao;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Component("starStaffDao")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class StarStaffDaoImpl implements IStarStaffDao {
	
	@Autowired
	private JdbcTemplate jt;
	
	
	@Override
	public List getStarTop(String orgId) {
		String sql=" select b.GENDER, a.STAFF_ID,a.STAFF_NAME,a.ORG_NAME,a.START_TOP,a.IMG_URL,a.TITLE from TSM_STAFF_START_TOP a,tsm_staff b where a.org_id=? and a.staff_id=b.staff_id order by a.start_top asc";
		return this.jt.queryForList(sql, orgId);
	}

	@Override
	public int[] saveStarStaff(JSONArray arr,Map orgMap) {
		String sql = "insert into TSM_STAFF_START_TOP(staff_id,staff_name,org_id,org_name,start_top) select ?,?,?,?,"
				+ "CASE WHEN max(start_top) is null THEN 1 ELSE max(start_top)+1 END "
				+ "from TSM_STAFF_START_TOP t where t.org_id=?";
		List<Object[]> batchArgs = new ArrayList<>();
		if (!arr.isEmpty()) {
			for (int i = 0; i < arr.size(); i++) {
				JSONObject o = arr.getJSONObject(i);
				batchArgs.add(new Object[] { o.getString("STAFF_ID"), o.getString("STAFFNAME"),
						orgMap.get("linkId").toString(), orgMap.get("org_name").toString(),
						orgMap.get("linkId").toString() });
			}
		}
		return this.jt.batchUpdate(sql, batchArgs);
	}

	@Override
	public Map getLinkOrg(String orgId) {
		String sql=" select linkid,org_name from tsm_organization where org_id=? ";
		List<Map<String, Object>> list=this.jt.queryForList(sql, orgId);
		String linkid=list.get(0).get("LINKID").toString();
		String orgName=list.get(0).get("ORG_NAME").toString();
		String[] arr=linkid.split("-");
		if(arr.length>2) {
			linkid=arr[0]+"-"+arr[1];
		}
		Map<String, String> map=new HashMap();
		map.put("org_name", orgName);
		map.put("linkId", linkid);
		return map;
	}

	@Override
	public int editStaffTop(String staffId, String staffTop) {
		String sql="update TSM_STAFF_START_TOP set start_top=? where staff_id=?";
		return this.jt.update(sql, staffTop, staffId);
	}

	@Override
	public int[] deleteStarStaff(String staffList) {
		String sql="delete from TSM_STAFF_START_TOP where staff_id=?";
		JSONArray arr=JSONArray.fromObject(staffList);
		
		List<Object[]> batchArgs = new ArrayList<Object[]>();
		if (!arr.isEmpty()) {
			for (int i = 0; i < arr.size(); i++) {
				JSONObject o = arr.getJSONObject(i);
				batchArgs.add(new Object[] { o.getString("STAFF_ID") });
			}
		}
		return this.jt.batchUpdate(sql, batchArgs);
	}

	@Override
	public int editStaffTitle(String title, String linkId) {
		String sql="update TSM_STAFF_START_TOP set title=? where org_id=?";
		return this.jt.update(sql, title, linkId);
	}

}
