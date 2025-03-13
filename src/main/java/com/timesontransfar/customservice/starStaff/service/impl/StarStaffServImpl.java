package com.timesontransfar.customservice.starStaff.service.impl;

import net.sf.json.JSONArray;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.timesontransfar.customservice.starStaff.service.IStarStaffServ;
import com.timesontransfar.customservice.starStaff.dao.IStarStaffDao;

@Component("starStaffServImpl")
@SuppressWarnings("rawtypes")
public class StarStaffServImpl implements IStarStaffServ {
	
	@Autowired
	private IStarStaffDao starStaffDao;
	

	@Override
	public List getStarTop(String orgId) {
		return this.starStaffDao.getStarTop(orgId);
	}

	@Override
	public int[] saveStarStaff(JSONArray arr, String orgId) {
		Map orgMap=this.starStaffDao.getLinkOrg(orgId);
		return this.starStaffDao.saveStarStaff(arr,orgMap);
	}

	@Override
	public int editStaffTop(String staffId, String staffTop) {
		return this.starStaffDao.editStaffTop(staffId, staffTop);
	}

	@Override
	public int[] deleteStarStaff(String staffList) {
		return this.starStaffDao.deleteStarStaff(staffList);
	}

	@Override
	public int editStaffTitle(String title, String linkId) {
		return this.starStaffDao.editStaffTitle(title,linkId);
	}
}
