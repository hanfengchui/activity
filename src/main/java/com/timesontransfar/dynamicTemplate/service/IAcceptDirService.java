package com.timesontransfar.dynamicTemplate.service;

public interface IAcceptDirService {
	/**
	 * 加载1到二级目录
	 * @return
	 */
	String loadDirOne2Two();
	/**
	 * 加载三级到六级目录
	 * @return
	 */
	String loadSixDirList(String id);
	
	String loadBanjieDirOne2Two();
	
	String loadBanjieSixDirList(String id);
}
