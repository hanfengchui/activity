/**
 * @author 万荣伟
 */
package com.timesontransfar.customservice.especiallyCust;

/**
 * @author 万荣伟
 *
 */
public class TsEspeciallyCustInfo {
	
	int regionId;//地域ID
	String regionName="";//地域名
	String custNum="";//客户号码,不带区号
	String custName="";//客户姓名
	String tsEspecially="";//投诉特征
	String remark="";//备注
	String meetProceeding="";//接续事项
	int statu;//记录状态，0为无效，1为有效
	String modifiData="";//修改时间
	int staffId;//修改员工
	String regionTel="";//区号
	int modelFlag=0;//如果为0插双数据库
	
	
	
	/**
	 * @return modifiData修改时间
	 */
	public String getModifiData() {
		return modifiData;
	}
	/**
	 * @param modifiData 要设置的 修改时间
	 */
	public void setModifiData(String modifiData) {
		this.modifiData = modifiData;
	}
	/**
	 * @return staffId修改员工
	 */
	public int getStaffId() {
		return staffId;
	}
	/**
	 * @param staffId 要设置的 修改员工
	 */
	public void setStaffId(int staffId) {
		this.staffId = staffId;
	}
	/**
	 * @return statu记录状态，0为无效，1为有效
	 */
	public int getStatu() {
		return statu;
	}
	/**
	 * @param statu 要设置的 记录状态，0为无效，1为有效
	 */
	public void setStatu(int statu) {
		this.statu = statu;
	}
	/**
	 * @return custName客户姓名
	 */
	public String getCustName() {
		return custName;
	}
	/**
	 * @param custName 要设置的 客户姓名
	 */
	public void setCustName(String custName) {
		this.custName = custName;
	}
	/**
	 * @return custNum客户号码,不带区号
	 */
	public String getCustNum() {
		return custNum;
	}
	/**
	 * @param custNum 要设置的 客户号码,不带区号
	 */
	public void setCustNum(String custNum) {
		this.custNum = custNum;
	}
	/**
	 * @return meetProceeding接续事项
	 */
	public String getMeetProceeding() {
		return meetProceeding;
	}
	/**
	 * @param meetProceeding 要设置的 接续事项
	 */
	public void setMeetProceeding(String meetProceeding) {
		this.meetProceeding = meetProceeding;
	}
	/**
	 * @return regionId地域ID
	 */
	public int getRegionId() {
		return regionId;
	}
	/**
	 * @param regionId 要设置的 地域ID
	 */
	public void setRegionId(int regionId) {
		this.regionId = regionId;
	}
	/**
	 * @return regionName地域名
	 */
	public String getRegionName() {
		return regionName;
	}
	/**
	 * @param regionName 要设置的 地域名
	 */
	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}
	/**
	 * @return remark备注
	 */
	public String getRemark() {
		return remark;
	}
	/**
	 * @param remark 要设置的 备注
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	/**
	 * @return tsEspecially投诉特征
	 */
	public String getTsEspecially() {
		return tsEspecially;
	}
	/**
	 * @param tsEspecially 要设置的 投诉特征
	 */
	public void setTsEspecially(String tsEspecially) {
		this.tsEspecially = tsEspecially;
	}
	/**
	 * @return regionTel
	 */
	public String getRegionTel() {
		return regionTel;
	}
	/**
	 * @param regionTel 要设置的 regionTel
	 */
	public void setRegionTel(String regionTel) {
		this.regionTel = regionTel;
	}
	/**
	 * @return modelFlag
	 */
	public int getModelFlag() {
		return modelFlag;
	}
	/**
	 * @param modelFlag 要设置的 modelFlag
	 */
	public void setModelFlag(int modelFlag) {
		this.modelFlag = modelFlag;
	}
	
	

}
