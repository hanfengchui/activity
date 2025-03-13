package com.timesontransfar.customservice.starStaff.dao;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;

@SuppressWarnings("rawtypes")
public interface IStarStaffDao {
	
	public List getStarTop(String orgId);
	
	public int[] saveStarStaff(JSONArray arr,Map orgMap);
	
	public Map  getLinkOrg(String orgId);
	
	public int editStaffTop(String staffId,String staffTop);
	
	public int[] deleteStarStaff(String staffList);
	
	public int editStaffTitle(String title,String linkId);
}
