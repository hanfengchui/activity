package com.timesontransfar.common.web.system;

public interface IRemarkPassword {

	/**
	 * 修改密码
	 *
	 * @param logonname
	 * @param newPs
	 */
	public boolean updateStaffPs(String logonname, String newPs);

	public boolean isBeforePassword(String logonname, String password);
}
