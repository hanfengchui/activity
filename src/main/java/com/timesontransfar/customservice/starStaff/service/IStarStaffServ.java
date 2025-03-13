package com.timesontransfar.customservice.starStaff.service;

import java.util.List;

import net.sf.json.JSONArray;

@SuppressWarnings("rawtypes")
public interface IStarStaffServ {

	public List getStarTop(String orgId);
	
	public int[] saveStarStaff(JSONArray arr,String orgId);
	
	public int editStaffTop(String staffId,String staffTop);
	
	public int[] deleteStarStaff(String staffList);
	
	public int editStaffTitle(String title,String linkId);
}
