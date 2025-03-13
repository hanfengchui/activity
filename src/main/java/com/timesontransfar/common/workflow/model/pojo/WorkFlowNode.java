/*
 * 创建日期 2006-8-15
 *
 *  要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author ationr
 *
 *  要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
@SuppressWarnings("rawtypes")
public class WorkFlowNode implements Cloneable,java.io.Serializable{

	/*
	 * create table TSP_WORKFLOW_TACHE_RELA ( TACHE_ID NUMBER(9) 节点编号 not null,
	 * VERSION NUMBER(9,2) 节点版本号 not null, WFL_ID NUMBER(9), 流程编号 TSP_VERSION
	 * NUMBER(9,2), 工作流_流程版本号 NDGUID VARCHAR2(32) 节点唯一标示 not null, WFGUID
	 * VARCHAR2(32) 流程唯一标示 not null, NDNAME VARCHAR2(36) 节点名称 not null, NDTYPE
	 * NUMBER(9), 节点类型 NDSTATE NUMBER(9), 节点状态 OUTCONDITION varchar2(32), 退出条件
	 * INMETHOD VARCHAR2(200), 进入业务方法 OUTMETHOD VARCHAR2(200), 退出业务方法 AUTO_FLAG
	 * NUMBER(1), 是否自动 SPEC_FLAG NUMBER(1), 流程是否可中止 FORCE_CTRL_FLAG NUMBER(1),
	 * 可否强制调度 ROLLBACK_FLAG NUMBER(1), DRAWBACK_FLAG NUMBER(1), OVERRUNTIME
	 * NUMBER(30), OVERSTEPTTIME NUMBER(30), OUTSYSTEMID VARCHAR2(32),
	 * OUTMSGTYPE VARCHAR2(32), SUBWFID VARCHAR2(32), ALT_INFO VARCHAR2(200),
	 * WFL_SEQ_NBR NUMBER(9), WORKSHEET_TYPE Number(9), REMARK VARCHAR2(1000),
	 * CREATESTAFF NUMBER(9), CREATETIME DATE, MODIFYSTAFF NUMBER(9), MODIFYTIME
	 * DATE, constraint PK_TSP_WORKFLOW_TACHE_RELA primary key (TACHE_ID,
	 * VERSION) );
	 */

	private long wflId;

	private long wflSeqNbr;

	private long worksheetType;

	private long tacheId;

	private String ndguid;

	private String wfguid;

	private String ndname;

	private short ndtype; //节点类型

	private short ndstate; //节点状态

	private WorkFlowExecMethod inmethod;

	private WorkFlowExecMethod outmethod;

	private boolean autoFlag;//是否为自动

	private long specFlag;

	private long forceCtrlFlag;

	private long rollbackFlag;

	private long drawbackFlag;

	private long overruntime;

	private long overstepttime;

	private String outsystemid;

	private String outmsgtype;

	private String subwfid;

	private String altInfo;

	private String remark;

	private long createStaff;

	private Timestamp createDate;

	private long modifyStaff;

	private Timestamp modifyDate;

	private WorkFlowSchema workFlowSchema;

	private List inCondition;

	private List outCondition;

	private int xPos;

	private int yPos;

	public final int getXPos() {
		return xPos;
	}

	public final void setXPos(int pos) {
		xPos = pos;
	}

	public final int getYPos() {
		return yPos;
	}

	public final void setYPos(int pos) {
		yPos = pos;
	}

	public List getInCondition() {
		return inCondition;
	}

	public void setInCondition(List inCondition) {
		this.inCondition = inCondition;
	}

	public List getOutCondition() {
		return outCondition;
	}

	public void setOutCondition(List outCondition) {
		this.outCondition = outCondition;
	}

	public String getAltInfo() {
		return altInfo;
	}

	public void setAltInfo(String altInfo) {
		this.altInfo = altInfo;
	}

	public boolean getAutoFlag() {
		return autoFlag;
	}

	public void setAutoFlag(boolean autoFlag) {
		this.autoFlag = autoFlag;
	}

	public Timestamp getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Timestamp createDate) {
		this.createDate = createDate;
	}

	public long getCreateStaff() {
		return createStaff;
	}

	public void setCreateStaff(long createStaff) {
		this.createStaff = createStaff;
	}

	public long getDrawbackFlag() {
		return drawbackFlag;
	}

	public void setDrawbackFlag(long drawbackFlag) {
		this.drawbackFlag = drawbackFlag;
	}

	public long getForceCtrlFlag() {
		return forceCtrlFlag;
	}

	public void setForceCtrlFlag(long forceCtrlFlag) {
		this.forceCtrlFlag = forceCtrlFlag;
	}

	public WorkFlowExecMethod getInmethod() {
		return inmethod;
	}

	public void setInmethod(WorkFlowExecMethod inmethod) {
		this.inmethod = inmethod;
	}

	public Timestamp getModifyDate() {
		return modifyDate;
	}

	public void setModifyDate(Timestamp modifyDate) {
		this.modifyDate = modifyDate;
	}

	public long getModifyStaff() {
		return modifyStaff;
	}

	public void setModifyStaff(long modifyStaff) {
		this.modifyStaff = modifyStaff;
	}

	public String getNdguid() {
		return ndguid;
	}

	public void setNdguid(String ndguid) {
		this.ndguid = ndguid;
	}

	public String getNdname() {
		return ndname;
	}

	public void setNdname(String ndname) {
		this.ndname = ndname;
	}

	public short getNdstate() {
		return ndstate;
	}

	public void setNdstate(short ndstate) {
		this.ndstate = ndstate;
	}

	public short getNdtype() {
		return ndtype;
	}

	public void setNdtype(short ndtype) {
		this.ndtype = ndtype;
	}


	public WorkFlowExecMethod getOutmethod() {
		return outmethod;
	}

	public void setOutmethod(WorkFlowExecMethod outmethod) {
		this.outmethod = outmethod;
	}

	public String getOutmsgtype() {
		return outmsgtype;
	}

	public void setOutmsgtype(String outmsgtype) {
		this.outmsgtype = outmsgtype;
	}

	public String getOutsystemid() {
		return outsystemid;
	}

	public void setOutsystemid(String outsystemid) {
		this.outsystemid = outsystemid;
	}

	public long getOverruntime() {
		return overruntime;
	}

	public void setOverruntime(long overruntime) {
		this.overruntime = overruntime;
	}

	public long getOverstepttime() {
		return overstepttime;
	}

	public void setOverstepttime(long overstepttime) {
		this.overstepttime = overstepttime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public long getRollbackFlag() {
		return rollbackFlag;
	}

	public void setRollbackFlag(long rollbackFlag) {
		this.rollbackFlag = rollbackFlag;
	}

	public long getSpecFlag() {
		return specFlag;
	}

	public void setSpecFlag(long specFlag) {
		this.specFlag = specFlag;
	}

	public String getSubwfid() {
		return subwfid;
	}

	public void setSubwfid(String subwfid) {
		this.subwfid = subwfid;
	}

	public long getTacheId() {
		return tacheId;
	}

	public void setTacheId(long tacheId) {
		this.tacheId = tacheId;
	}

	public String getWfguid() {
		return wfguid;
	}

	public void setWfguid(String wfguid) {
		this.wfguid = wfguid;
	}

	public long getWflId() {
		return wflId;
	}

	public void setWflId(long wflId) {
		this.wflId = wflId;
	}

	public long getWflSeqNbr() {
		return wflSeqNbr;
	}

	public void setWflSeqNbr(long wflSeqNbr) {
		this.wflSeqNbr = wflSeqNbr;
	}

	public WorkFlowSchema getWorkFlowSchema() {
		return workFlowSchema;
	}

	public void setWorkFlowSchema(WorkFlowSchema workFlowSchema) {
		this.workFlowSchema = workFlowSchema;
	}

	public long getWorksheetType() {
		return worksheetType;
	}

	public void setWorksheetType(long worksheetType) {
		this.worksheetType = worksheetType;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() {
		if(inCondition!=null) inCondition.clear();
		if(outCondition!=null) outCondition.clear();
	}

	public Object clone() {
		WorkFlowNode copy = null;
		try {
			copy = (WorkFlowNode)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
}
