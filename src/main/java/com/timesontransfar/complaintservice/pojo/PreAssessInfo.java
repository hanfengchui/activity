package com.timesontransfar.complaintservice.pojo;

import com.timesontransfar.customservice.worksheet.pojo.TsSheetQualitative;

public class PreAssessInfo {
	private TsSheetQualitative bean;
	private int regionId;
	private int month;
	private String dealContent;
	private int upgradeIncline;
	private String contactStatus;
	private String requireUninvited;
	private String unifiedCode;
	private String uccJTSS;

	public TsSheetQualitative getBean() {
		return bean;
	}

	public void setBean(TsSheetQualitative bean) {
		this.bean = bean;
	}

	public int getRegionId() {
		return regionId;
	}

	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public String getDealContent() {
		return dealContent;
	}

	public void setDealContent(String dealContent) {
		this.dealContent = dealContent;
	}

	public int getUpgradeIncline() {
		return upgradeIncline;
	}

	public void setUpgradeIncline(int upgradeIncline) {
		this.upgradeIncline = upgradeIncline;
	}

	public String getContactStatus() {
		return contactStatus;
	}

	public void setContactStatus(String contactStatus) {
		this.contactStatus = contactStatus;
	}

	public String getRequireUninvited() {
		return requireUninvited;
	}

	public void setRequireUninvited(String requireUninvited) {
		this.requireUninvited = requireUninvited;
	}

	public String getUnifiedCode() {
		return unifiedCode;
	}

	public void setUnifiedCode(String unifiedCode) {
		this.unifiedCode = unifiedCode;
	}

	public String getUccJTSS() {
		return uccJTSS;
	}

	public void setUccJTSS(String uccJTSS) {
		this.uccJTSS = uccJTSS;
	}
}