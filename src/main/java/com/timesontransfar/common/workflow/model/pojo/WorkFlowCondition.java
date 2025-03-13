/*
 * 创建日期 2006-8-15
 *
 * 要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

import java.sql.Date;

/**
 * @author ationr
 *
 *  要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
public class WorkFlowCondition implements Cloneable,java.io.Serializable {
	private long condno;

	private long seq;

	private double version;

	private String condguid;

	private String condname;

	private long condtype;

	private long rtstate;

	private long operateId;

	private String condValue;

	private short relation;

	private String remark;

	private long createstaff;

	private Date createtime;

	private long modifystaff;

	private Date modifytime;

	private WorkFlowRoute workFlowRoute;

	private String rtno;

	private double twfVersion;

	private WorkFlowExecMethod execMethod;

	public WorkFlowExecMethod getExecMethod() {
		return execMethod;
	}

	public void setExecMethod(WorkFlowExecMethod execMethod) {
		this.execMethod = execMethod;
	}


	public String getCondguid() {
		return condguid;
	}

	public void setCondguid(String condguid) {
		this.condguid = condguid;
	}

	public String getCondname() {
		return condname;
	}

	public void setCondname(String condname) {
		this.condname = condname;
	}

	public long getCondno() {
		return condno;
	}

	public void setCondno(long condno) {
		this.condno = condno;
	}

	public long getCondtype() {
		return condtype;
	}

	public void setCondtype(long condtype) {
		this.condtype = condtype;
	}

	public String getCondValue() {
		return condValue;
	}

	public void setCondValue(String condValue) {
		this.condValue = condValue;
	}

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

	public long getOperateId() {
		return operateId;
	}

	public void setOperateId(long operateId) {
		this.operateId = operateId;
	}

	public short getRelation() {
		return relation;
	}

	public void setRelation(short relation) {
		this.relation = relation;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public long getRtstate() {
		return rtstate;
	}

	public void setRtstate(long rtstate) {
		this.rtstate = rtstate;
	}

	public long getSeq() {
		return seq;
	}

	public void setSeq(long seq) {
		this.seq = seq;
	}

	public double getVersion() {
		return version;
	}

	public void setVersion(double version) {
		this.version = version;
	}

	public WorkFlowRoute getWorkFlowRoute() {
		return workFlowRoute;
	}

	public void setWorkFlowRoute(WorkFlowRoute workFlowRoute) {
		this.workFlowRoute = workFlowRoute;
	}

	public String getRtno() {
		return rtno;
	}

	public void setRtno(String rtno) {
		this.rtno = rtno;
	}

	public double getTwfVersion() {
		return twfVersion;
	}

	public void setTwfVersion(double twfVersion) {
		this.twfVersion = twfVersion;
	}
	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		WorkFlowCondition copy = null;
		try {
			copy = (WorkFlowCondition)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}

}
