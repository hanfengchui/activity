package com.timesontransfar.common.web.system;

import javax.servlet.http.HttpServletRequest;

public interface ILoginDAO {
	
	/**
	 * 判断用户登陆信息
	 */
	public String validateUserNew(String userName, String password,HttpServletRequest request);

	/**
	 * 判断密码符合集团标准
	 */
	public String checkPassWithGroup(String userName, String password);

	public boolean isStaffState(String logonname);
	
	public String loadNewAuthorization(String logonname);

}
