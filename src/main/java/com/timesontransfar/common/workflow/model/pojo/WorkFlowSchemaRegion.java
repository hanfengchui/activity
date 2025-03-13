package com.timesontransfar.common.workflow.model.pojo;

public class WorkFlowSchemaRegion implements Cloneable,java.io.Serializable{
	  private String wfguid  ;
	  private long regionid;

	public WorkFlowSchemaRegion() {
		super();
		// 自动生成构造函数存根
	}

	public long getRegionid() {
		return regionid;
	}

	public void setRegionid(long regionid) {
		this.regionid = regionid;
	}

	public String getWfguid() {
		return wfguid;
	}

	public void setWfguid(String wfguid) {
		this.wfguid = wfguid;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		WorkFlowSchemaRegion copy = null;
		try {
			copy = (WorkFlowSchemaRegion)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}

}
