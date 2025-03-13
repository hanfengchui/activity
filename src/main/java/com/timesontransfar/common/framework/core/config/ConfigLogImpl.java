package com.timesontransfar.common.framework.core.config;

import java.io.Serializable;

public class ConfigLogImpl implements IConfigLog,Serializable {

	private String showPicCS;

	private String version;

	private String upgradeStaffName;

	private String upgradeTime;

	private String upgradeCause;

	private String upgradePurpose;
	
	private boolean clearFlag;

	public boolean isClearFlag() {
		return clearFlag;
	}

	public void setClearFlag(boolean clearFlag) {
		this.clearFlag = clearFlag;
	}

	public String getContent() {
		return showPicCS.toUpperCase();
	}

	public String getSystemVersion() {
		return this.version;
	}

	public String getSysUpgradeStaffName() {
		return this.upgradeStaffName;
	}

	public String getSysUpgradeTime() {
		return this.upgradeTime;
	}

	public String getSysUpgradeCause() {
		return this.upgradeCause;
	}

	public String getSysUpgradePurpose() {
		return this.upgradePurpose;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getShowPicCS() {
		return showPicCS;
	}

	public void setShowPicCS(String showPicCS) {
		this.showPicCS = showPicCS;
	}

	public String getUpgradeCause() {
		return upgradeCause;
	}

	public void setUpgradeCause(String upgradeCause) {
		this.upgradeCause = upgradeCause;
	}

	public String getUpgradePurpose() {
		return upgradePurpose;
	}

	public void setUpgradePurpose(String upgradePurpose) {
		this.upgradePurpose = upgradePurpose;
	}

	public String getUpgradeStaffName() {
		return upgradeStaffName;
	}

	public void setUpgradeStaffName(String upgradeStaffName) {
		this.upgradeStaffName = upgradeStaffName;
	}

	public String getUpgradeTime() {
		return upgradeTime;
	}

	public void setUpgradeTime(String upgradeTime) {
		this.upgradeTime = upgradeTime;
	}

}