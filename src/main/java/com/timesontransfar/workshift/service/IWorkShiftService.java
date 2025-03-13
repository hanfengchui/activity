package com.timesontransfar.workshift.service;

import java.util.List;

import com.timesontransfar.workshift.pojo.WorkShift;

@SuppressWarnings("rawtypes")
public interface IWorkShiftService {
	public String addWorkShift(WorkShift workShift);

	public int modifyWorkShift(WorkShift workShift);

	public WorkShift getWorkShiftById(String id);

	public WorkShift getWorkShiftByName(String name);

	public int shiftNameExist(String id, String name);

	public List getWorkShift();
}
