/*
 * 创建日期 2006-8-15
 *
 *  要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

import java.sql.Date;

/**
 * @author ationr
 *
 *  要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class WorkFlowRoute implements Cloneable,java.io.Serializable{
	private String rtno;

	private double version;

	private String rtguid;

	private String rtname;

	private long rttype;

	private long rtstate;

	private String nextNode;

	private long priLevel;

	private String remark;

	private long createstaff;

	private Date createtime;

	private long modifystaff;

	private Date modifytime;

	private long tacheId;

	private double tspVersion;

	public long getCreatestaff() {
		return createstaff;
	}

	public void setCreatestaff(long createstaff) {
		this.createstaff = createstaff;
	}

	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	public long getModifystaff() {
		return modifystaff;
	}

	public void setModifystaff(long modifystaff) {
		this.modifystaff = modifystaff;
	}

	public Date getModifytime() {
		return modifytime;
	}

	public void setModifytime(Date modifytime) {
		this.modifytime = modifytime;
	}

	public String getNextNode() {
		return nextNode;
	}

	public void setNextNode(String nextNode) {
		this.nextNode = nextNode;
	}

	public long getPriLevel() {
		return priLevel;
	}

	public void setPriLevel(long priLevel) {
		this.priLevel = priLevel;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getRtguid() {
		return rtguid;
	}

	public void setRtguid(String rtguid) {
		this.rtguid = rtguid;
	}

	public String getRtname() {
		return rtname;
	}

	public void setRtname(String rtname) {
		this.rtname = rtname;
	}

	public String getRtno() {
		return rtno;
	}

	public void setRtno(String rtno) {
		this.rtno = rtno;
	}

	public long getRtstate() {
		return rtstate;
	}

	public void setRtstate(long rtstate) {
		this.rtstate = rtstate;
	}

	public long getRttype() {
		return rttype;
	}

	public void setRttype(long rttype) {
		this.rttype = rttype;
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}


	public long getTacheId() {
		return tacheId;
	}

	public void setTacheId(long tacheId) {
		this.tacheId = tacheId;
	}

	public double getTspVersion() {
		return tspVersion;
	}

	public void setTspVersion(double tspVersion) {
		this.tspVersion = tspVersion;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		WorkFlowRoute copy = null;
		try {
			copy = (WorkFlowRoute)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}

}
