/*
 * 创建日期 2006-8-15
 *
 *  要更改此生成的文件的模板，请转至
 * 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
package com.timesontransfar.common.workflow.model.pojo;

/**
 * @author ationr
 *
 *  要更改此生成的类型注释的模板，请转至 窗口 － 首选项 － Java － 代码样式 － 代码模板
 */
import java.sql.Timestamp;
import java.util.List;
@SuppressWarnings("rawtypes")
public class WorkFlowSchema implements Cloneable,java.io.Serializable{
	private long wflId;

	private long wflType;

	private String wflName;

	private String wflDesc;

	private String wflFile;

	private String wflXmlSchema;

	private Timestamp wflEffDate;

	private Timestamp wflExpDate;

	private String wfguid;

	private long wfstate;

	private long specFlag;

	private String wfStartNode;

	private String wfEndNode;

	private long createStaff;

	private Timestamp createDate;

	private long modifyStaff;

	private Timestamp modifyDate;

	private long wflTypeEx;

	private List curNodes;

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

	public long getSpecFlag() {
		return specFlag;
	}

	public void setSpecFlag(long specFlag) {
		this.specFlag = specFlag;
	}

	public String getWfEndNode() {
		return wfEndNode;
	}

	public void setWfEndNode(String wfEndNode) {
		this.wfEndNode = wfEndNode;
	}

	public String getWfguid() {
		return wfguid;
	}

	public void setWfguid(String wfguid) {
		this.wfguid = wfguid;
	}

	public String getWflDesc() {
		return wflDesc;
	}

	public void setWflDesc(String wflDesc) {
		this.wflDesc = wflDesc;
	}

	public Timestamp getWflEffDate() {
		return wflEffDate;
	}

	public void setWflEffDate(Timestamp wflEffDate) {
		this.wflEffDate = wflEffDate;
	}

	public Timestamp getWflExpDate() {
		return wflExpDate;
	}

	public void setWflExpDate(Timestamp wflExpDate) {
		this.wflExpDate = wflExpDate;
	}

	public String getWflFile() {
		return wflFile;
	}

	public void setWflFile(String wflFile) {
		this.wflFile = wflFile;
	}

	public long getWflId() {
		return wflId;
	}

	public void setWflId(long wflId) {
		this.wflId = wflId;
	}

	public String getWflName() {
		return wflName;
	}

	public void setWflName(String wflName) {
		this.wflName = wflName;
	}

	public long getWflType() {
		return wflType;
	}

	public void setWflType(long wflType) {
		this.wflType = wflType;
	}

	public long getWflTypeEx() {
		return wflTypeEx;
	}

	public void setWflTypeEx(long wflTypeEx) {
		this.wflTypeEx = wflTypeEx;
	}

	public String getWflXmlSchema() {
		return wflXmlSchema;
	}

	public void setWflXmlSchema(String wflXmlSchema) {
		this.wflXmlSchema = wflXmlSchema;
	}

	public String getWfStartNode() {
		return wfStartNode;
	}

	public void setWfStartNode(String wfStartNode) {
		this.wfStartNode = wfStartNode;
	}

	public long getWfstate() {
		return wfstate;
	}

	public void setWfstate(long wfstate) {
		this.wfstate = wfstate;
	}

	public List getCurNodes() {
		return curNodes;
	}

	public void setCurNodes(List curNodes) {
		this.curNodes = curNodes;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() {
		if(this.curNodes!=null) this.curNodes.clear();
	}

	public Object clone() {
		WorkFlowSchema copy = null;
		try {
			copy = (WorkFlowSchema)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}
}
