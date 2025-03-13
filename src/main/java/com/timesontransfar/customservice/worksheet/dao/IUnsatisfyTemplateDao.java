package com.timesontransfar.customservice.worksheet.dao;

import java.util.List;

@SuppressWarnings("rawtypes")
public interface IUnsatisfyTemplateDao {
	public int insertUnsatisfyTemplate(String reason, String template, int colOrder);

	public int deleteUnsatisfyTemplate(String unsatisfyId);

	public int updateUnsatisfyTemplate(String reason, String template, int colOrder, String unsatisfyId);

	public List selectUnsatisfyTemplate();
}
