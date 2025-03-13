package com.timesontransfar.common.framework.core.config;

public interface IConfigLog {
	public String getContent();

	public String getSystemVersion();

	public String getSysUpgradeStaffName();

	public String getSysUpgradeTime();

	public String getSysUpgradeCause();

	public String getSysUpgradePurpose();
	
	public boolean isClearFlag();
}
