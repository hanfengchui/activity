package com.timesontransfar.common.workflow.model.pojo;

public class InterfaceInfo implements Cloneable, java.io.Serializable {
	private String wfno;

	private String wfname;

	private String remark;

	private long regionId;

	private long productId;

	private long actionId;

	private long entityId;

	private String interfaceGuid;

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getWfname() {
		return wfname;
	}

	public void setWfname(String wfname) {
		this.wfname = wfname;
	}

	public String getWfno() {
		return wfno;
	}

	public void setWfno(String wfno) {
		this.wfno = wfno;
	}

	protected void finalize() throws Throwable {
		super.finalize();
		this.clear();
	}

	private void clear() { //空白方法
	}

	public Object clone() {
		InterfaceInfo copy = null;
		try {
			copy = (InterfaceInfo)super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return copy;
	}

	public long getActionId() {
		return actionId;
	}

	public void setActionId(long actionId) {
		this.actionId = actionId;
	}

	public long getEntityId() {
		return entityId;
	}

	public void setEntityId(long entityId) {
		this.entityId = entityId;
	}

	public String getInterfaceGuid() {
		return interfaceGuid;
	}

	public void setInterfaceGuid(String interfaceGuid) {
		this.interfaceGuid = interfaceGuid;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public long getRegionId() {
		return regionId;
	}

	public void setRegionId(long regionId) {
		this.regionId = regionId;
	}
}
